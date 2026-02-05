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
  vgaTimingInfo: LcvVgaTimingInfo,
) {
  require(
    fbMmapCfg.optSliceSize == None
  )
  require(
    fbMmapCfg.optAddrSliceVal != None
  )
  def busCfg = fbMmapCfg.busCfg
  def fbSize2d = vgaTimingInfo.fbSize2d
  val myFbSize2dMult = fbSize2d.x * fbSize2d.y

  //val myFbCntOverflow = myFbSize2dMult
  val myBusBurstSizeMax = busCfg.maxBurstSizeMinus1 + 1
  val myFbCntMax = (
    myFbSize2dMult - 1
    + myBusBurstSizeMax
    - ((myFbSize2dMult - 1) % myBusBurstSizeMax)
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
    >= (1 << log2Up(myFbCntMax + 1))
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

case class LcvBusFramebufferCtrl(
  cfg: LcvBusFramebufferConfig
) extends Component {
  //--------
  val io = LcvBusFramebufferCtrlIo(cfg=cfg)
  //--------
  val rCnt = {
    val temp = Vec.fill(2)({
      val innerTemp = Reg(
        UInt(log2Up(cfg.myFbCntMax + 1) bits)
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
      cfg.fbMmapCfg.addrSliceValUInt,
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
    cfg.myFbSize2dMult != cfg.myFbCntMax
  ) generate (
    Bool()
  )
  if (cfg.myFbSize2dMult == cfg.myFbCntMax) {
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
      ## (rCnt.last >= cfg.myFbCntMax - 1)
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
