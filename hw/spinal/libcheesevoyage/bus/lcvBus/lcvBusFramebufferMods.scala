package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

import spinal.lib.graphic.vga._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig

import libcheesevoyage.general._
import libcheesevoyage.gfx._

case class LcvBusFramebufferConfig(
  //busCfg: LcvBusConfig,
  fbMmapCfg: LcvBusMemMapConfig,
  rgbCfg: RgbConfig,
  //vgaTimingInfo: LcvVgaTimingInfo,
  fbSize2d: ElabVec2[Int],
  dblBuf: Boolean,
  cnt2dShift: ElabVec2[Int], // for line/pixel doubling
) {
  require(
    fbMmapCfg.optSliceSize == None
  )
  require(
    fbMmapCfg.optAddrSliceVal != None
  )
  require(
    cnt2dShift.x >= 0
  )
  require(
    cnt2dShift.y >= 0
  )
  def busCfg = fbMmapCfg.busCfg
  //def fbSize2d = vgaTimingInfo.fbSize2d
  val myFbSize2dMult = (
    fbSize2d.x * fbSize2d.y
    * (
      if (dblBuf) (2) else (1)
    )
  )

  //val myFbCntOverflow = myFbSize2dMult
  val myBusBurstSizeMax = busCfg.maxBurstSizeMinus1 + 1
  def calcAlignedFbCntMax(
    someFbCntMax: Int
  ) = {
    someFbCntMax - 1
    + myBusBurstSizeMax
    - ((someFbCntMax - 1) % myBusBurstSizeMax)
  }
  val myAlignedFbCntMax = (
    //myFbSize2dMult - 1
    //+ myBusBurstSizeMax
    //- ((myFbSize2dMult - 1) % myBusBurstSizeMax)
    calcAlignedFbCntMax(someFbCntMax=myFbSize2dMult)
  )
  val myAlignedFbCnt2dMax = ElabVec2[Int](
    x=calcAlignedFbCntMax(someFbCntMax=fbSize2d.x),
    y=calcAlignedFbCntMax(someFbCntMax=fbSize2d.y),
  )


  require(
    busCfg.allowBurst
  )
  require(
    busCfg.dataWidth
    >= Rgb(c=rgbCfg).asBits.getWidth
  )
  require(
    (1 << (busCfg.addrWidth - log2Up(busCfg.dataWidth / 8)))
    >= (1 << log2Up(myAlignedFbCntMax + 1))
  )
}

case class LcvBusFramebufferCtrlIo(
  cfg: LcvBusFramebufferConfig
) extends Bundle {
  //--------
  val bus = master(LcvBusIo(cfg=cfg.busCfg))
  val pop = master(Stream(Rgb(c=cfg.rgbCfg)))
  //--------
}
case class LcvBusFramebufferCtrlDualCntNonBurstBasic(
  cfg: LcvBusFramebufferConfig
) extends Component {
  //--------
  def rgbCfg = cfg.rgbCfg
  def fbSize2d = cfg.fbSize2d
  def cnt2dShift = cfg.cnt2dShift
  //--------
  val io = LcvBusFramebufferCtrlIo(cfg=cfg)
  //--------
  //val rD2hCnt = {
  //  val temp = Reg(
  //    UInt(log2Up(cfg.myAlignedFbCntMax + 1) bits)
  //  )
  //  temp.init(temp.getZero)
  //  temp
  //}
  val rH2dRawCnt2d = {
    //Vec.fill(2)({
      val temp = Reg(
        DualTypeNumVec2(
          dataTypeX=UInt(log2Up(fbSize2d.x + 1) + cnt2dShift.x bits),
          dataTypeY=(
            UInt(
              (if (cfg.dblBuf) (1) else (0))
              + (
                log2Up(fbSize2d.y + 1) + cnt2dShift.y 
              )
              bits
            )
          ),
        )
      )
      temp.init(temp.getZero)
      temp
    //})
  }
  val myCnt2dRange = (
    ElabVec2(
      x=(rH2dRawCnt2d.x.high downto cnt2dShift.x),
      y=(rH2dRawCnt2d.y.high downto cnt2dShift.y),
    )
  )
  val rH2dMainCnt = ElabDualTypeVec2(
    x=rH2dRawCnt2d.x(myCnt2dRange.x),
    y=rH2dRawCnt2d.y(myCnt2dRange.y),
  )
  //val rD2hMainCnt = ElabDualTypeVec2(
  //  x=rH2dRawCnt2d.last.x(myCnt2dRange.x),
  //  y=rH2dRawCnt2d.last.y(myCnt2dRange.y),
  //)
  //--------
  val myH2dStm = Vec.fill(3)(
    cloneOf(io.bus.h2dBus)
  )
  io.bus.h2dBus <-/< myH2dStm.last
  // The multiplication to calculate the address is why we use `<-<`.
  // Hopefully it will be seen as a purely registered multiply
  // by synth + pnr tools!
  // As of this writing, I haven't tried yet.
  myH2dStm(1) <-< myH2dStm.head
  myH2dStm(1).translateInto(myH2dStm.last)(
    dataAssignment=(outp, inp) => {
      outp := inp
      outp.addr.allowOverride
      outp.addr(cfg.fbMmapCfg.addrSliceRange) := (
        cfg.fbMmapCfg.addrSliceValUInt
      )
      //outp.addr(
      //)
    }
  )
  myH2dStm.head.valid := True
  myH2dStm.head.addr := (
    Cat(
      //(rCnt2d.head.y(myCnt2dRange.y) * fbSize2d.x)
      //+ rCnt2d.head.x(myCnt2dRange.x)
      (rH2dMainCnt.y * fbSize2d.x) + rH2dMainCnt.x
    ).asUInt.resize(myH2dStm.head.addr.getWidth)
    //Cat(
    //  cfg.fbMmapCfg.addrSliceValUInt,
    //  rCnt.x,
    //  U(s"${cfg.busCfg.dataWidth / 8}'d0"),
    //).asUInt.resize(myH2dStm.head.addr.getWidth)
  )
  myH2dStm.head.data := 0x0
  myH2dStm.head.byteSize := (
    //log2Up(cfg.busCfg.dataWidth / 8)
    log2Up(Rgb(rgbCfg).asBits.getWidth / 8)
  )
  myH2dStm.head.isWrite := False

  myH2dStm.head.burstFirst := False//True
  myH2dStm.head.burstCnt := 0x0//cfg.busCfg.maxBurstSizeMinus1
  myH2dStm.head.burstLast := False

  //when (myH2dStm.head.fire) {
  //}
  switch (
    myH2dStm.head.fire
    //## (rCnt2d.head.x(myCnt2dRange.x) >= fbSize2d.x)
    //## (rH2dMainCnt.x >= fbSize2d.x - cfg.myBusBurstSizeMax)
    //## (rH2dMainCnt.x >= fbSize2d.x - 1)

    ## (rH2dRawCnt2d.x >= (fbSize2d.x * (1 << cnt2dShift.x) - 1))
    //## (rH2dMainCnt.y >= fbSize2d.y - 1)
    ## (rH2dRawCnt2d.y >= (fbSize2d.y * (1 << cnt2dShift.y) - 1))
  ) {
    is (M"10-") {
      rH2dRawCnt2d.x := rH2dRawCnt2d.x + 1
    }
    is (M"110") {
      rH2dRawCnt2d.x := 0x0
      rH2dRawCnt2d.y := rH2dRawCnt2d.y + 1
      //when (
      //  //rH2dMainCnt.y >= fbSize2d.y - 1
      //  (rH2dRawCnt2d.y >= (fbSize2d.y * (1 << cnt2dShift.y) - 1))
      //) {
      //  rH2dRawCnt2d.y := 0x0
      //} otherwise {
      //  rH2dRawCnt2d
      //}
    }
    is (M"111") {
      rH2dRawCnt2d.x := 0x0
      rH2dRawCnt2d.y := 0x0
    }
    default {
    }
  }
  //--------
  val myD2hStm = Vec.fill(2)(
    cloneOf(io.bus.d2hBus)
  )
  myD2hStm.head <-/< io.bus.d2hBus
  //--------
  val myRgbFifo = StreamFifo(
    dataType=(
      //LcvBusD2hPayload(cfg=cfg.busCfg)
      Rgb(cfg.rgbCfg)
    ),
    depth=(
      //cfg.busCfg.maxBurstSizeMinus1 + 1
      cfg.myBusBurstSizeMax
    ),
    latency=(
      //0
      2
    ),
    forFMax=true,
  )
  myD2hStm.last.translateInto(myRgbFifo.io.push)(
    dataAssignment=(outp, inp) => {
      outp.assignFromBits(inp.data.asBits.resize(outp.asBits.getWidth))
    }
  )

  //val myPopStm = cloneOf(io.pop)
  //val myPopThrowCond = Bool()
  //myPopThrowCond := False
  //val myPopStm = myRgbFifo.io.pop.throwWhen(
  //  //rCnt2d.x 
  //  myPopThrowCond
  //)
  io.pop <-/< myRgbFifo.io.pop //myPopStm
  //--------
  //--------
  //val myD2hThrowCond = (
  //  cfg.myFbSize2dMult != cfg.myAlignedFbCntMax
  //) generate (
  //  Bool()
  //)
  myD2hStm.last << myD2hStm.head
  //if (cfg.myFbSize2dMult == cfg.myAlignedFbCntMax) {
  //  // no alignment needed for burst reads
  //  myD2hStm.last << myD2hStm.head
  //  when (myD2hStm.last.fire) {
  //    rD2hCnt := rD2hCnt + 1
  //  }
  //} else {
  //  // alignment needed for burst reads
  //  myD2hStm.last << myD2hStm.head.throwWhen(myD2hThrowCond)
  //  switch (
  //    myD2hStm.head.fire
  //    ## (rD2hCnt >= cfg.myFbSize2dMult - 1)
  //    ## (rD2hCnt >= cfg.myAlignedFbCntMax - 1)
  //  ) {
  //    is (M"100") {
  //      rD2hCnt := rD2hCnt + 1
  //      myD2hThrowCond := False
  //    }
  //    is (M"110") {
  //      rD2hCnt := rD2hCnt + 1
  //      myD2hThrowCond := True
  //    }
  //    is (M"111") {
  //      rD2hCnt := 0x0
  //      myD2hThrowCond := True
  //    }
  //    default {
  //      myD2hThrowCond := False
  //    }
  //  }
  //  //when (myD2hStm.fire) {
  //  //  when  {
  //  //  } elsewhen  {
  //  //  }
  //  //}
  //}
}

case class LcvBusFramebufferCtrlSingleCntBasic(
  cfg: LcvBusFramebufferConfig
) extends Component {
  //--------
  val io = LcvBusFramebufferCtrlIo(cfg=cfg)
  //--------
  val rCnt = {
    val temp = Vec.fill(2)({
      val innerTemp = Reg(
        UInt(log2Up(cfg.myAlignedFbCntMax + 1) bits)
      )
      innerTemp.init(innerTemp.getZero)
    })
    temp
  }
  //--------
  val myH2dStm = cloneOf(io.bus.h2dBus)
  io.bus.h2dBus <-/< myH2dStm
  myH2dStm.valid := True
  myH2dStm.addr := (
    Cat(
      //cfg.fbMmapCfg.addrSliceValUInt, // TODO: figure this out
      rCnt.head,
      U(s"${cfg.busCfg.dataWidth / 8}'d0"),
    ).asUInt.resize(myH2dStm.addr.getWidth)
  )
  myH2dStm.data := 0x0
  myH2dStm.byteSize := log2Up(cfg.busCfg.dataWidth / 8)
  myH2dStm.isWrite := False

  myH2dStm.burstFirst := True
  myH2dStm.burstCnt := cfg.busCfg.maxBurstSizeMinus1
  myH2dStm.burstLast := False
  //when (myH2dStm.fire) {
  //  
  //}
  switch (
    myH2dStm.fire
    ## (rCnt.head >= cfg.myFbSize2dMult - cfg.myBusBurstSizeMax)
  ) {
    is (M"10") {
      rCnt.head(rCnt.head.high downto cfg.busCfg.burstCntWidth) := (
        rCnt.head(rCnt.head.high downto cfg.busCfg.burstCntWidth) + 1
      )
    }
    is (M"11") {
      rCnt.head(rCnt.head.high downto cfg.busCfg.burstCntWidth) := 0x0
    }
    default {
    }
  }
  //--------
  //val myD2hFifo = StreamFifo(
  //  dataType=LcvBusD2hPayload(cfg=cfg.busCfg),
  //  depth=(cfg.busCfg.maxBurstSizeMinus1 + 1),
  //  latency=2,
  //  forFMax=true,
  //)
  //myD2hFifo.io.push <-/< io.bus.d2hBus
  val myD2hStm = Vec.fill(2)(
    cloneOf(io.bus.d2hBus)
  )
  myD2hStm.head <-/< io.bus.d2hBus
  //--------
  val myRgbFifo = StreamFifo(
    dataType=(
      //LcvBusD2hPayload(cfg=cfg.busCfg)
      Rgb(cfg.rgbCfg)
    ),
    depth=(
      //cfg.busCfg.maxBurstSizeMinus1 + 1
      cfg.myBusBurstSizeMax
    ),
    latency=(
      //0
      2
    ),
    forFMax=true,
  )
  myD2hStm.last.translateInto(myRgbFifo.io.push)(
    dataAssignment=(outp, inp) => {
      outp.assignFromBits(inp.data.asBits.resize(outp.asBits.getWidth))
    }
  )

  //val myPopStm = cloneOf(io.pop)
  //val myPopThrowCond = Bool()
  //myPopThrowCond := False
  //val myPopStm = myRgbFifo.io.pop.throwWhen(
  //  //rCnt2d.x 
  //  myPopThrowCond
  //)
  io.pop <-/< myRgbFifo.io.pop //myPopStm
  //--------
  //--------
  val myD2hThrowCond = (
    cfg.myFbSize2dMult != cfg.myAlignedFbCntMax
  ) generate (
    Bool()
  )
  if (cfg.myFbSize2dMult == cfg.myAlignedFbCntMax) {
    // no alignment needed for burst reads
    myD2hStm.last << myD2hStm.head
    when (myD2hStm.last.fire) {
      rCnt.last := rCnt.last + 1
    }
  } else {
    // alignment needed for burst reads
    myD2hStm.last << myD2hStm.head.throwWhen(myD2hThrowCond)
    switch (
      myD2hStm.head.fire
      ## (rCnt.last >= cfg.myFbSize2dMult - 1)
      ## (rCnt.last >= cfg.myAlignedFbCntMax - 1)
    ) {
      is (M"100") {
        rCnt.last := rCnt.last + 1
        myD2hThrowCond := False
      }
      is (M"110") {
        rCnt.last := rCnt.last + 1
        myD2hThrowCond := True
      }
      is (M"111") {
        rCnt.last := 0x0
        myD2hThrowCond := True
      }
      default {
        myD2hThrowCond := False
      }
    }
    //when (myD2hStm.fire) {
    //  when  {
    //  } elsewhen  {
    //  }
    //}
  }
  //myRgbFifo
  //myD2hFifo

  //io.pop.valid := False
  //io.pop.payload := io.pop.payload.getZero
  //--------
}
