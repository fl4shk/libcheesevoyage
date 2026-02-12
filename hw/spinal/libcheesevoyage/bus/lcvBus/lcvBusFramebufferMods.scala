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
  cnt2dShift: ElabVec2[Int],
  //cnt2dShiftOne: ElabVec2[Boolean], // for line/pixel doubling
) {
  //val cnt2dShift = ElabVec2[Int](
  //  x=(if (cnt2dShiftOne.x) (1) else (0)),
  //  y=(if (cnt2dShiftOne.y) (1) else (0)),
  //)
  require(
    fbMmapCfg.optSliceSize == None
  )
  require(
    fbMmapCfg.optAddrSliceVal != None
  )
  //if (!dblBuf) {
  //  require(
  //    fbMmapCfg.addrSliceWidth >= 1
  //  )
  //} else {
  //  require(
  //    fbMmapCfg.addrSliceWidth >= 2
  //  )
  //}
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
  ) = (
    someFbCntMax - 1
    + myBusBurstSizeMax
    - ((someFbCntMax - 1) % myBusBurstSizeMax)
  )
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
  println(
    s"${fbSize2d} "
    + s"${myAlignedFbCntMax} ${myAlignedFbCnt2dMax} "
    + s"${myBusBurstSizeMax}"
  )


  //require(
  //  busCfg.allowBurst
  //)

  //require(
  //  busCfg.dataWidth
  //  >= Rgb(c=rgbCfg).asBits.getWidth
  //)

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

case class LcvBusFramebufferCtrl(
  cfg: LcvBusFramebufferConfig
) extends Component {
  def rgbCfg = cfg.rgbCfg
  def busCfg = cfg.busCfg
  def fbSize2d = cfg.fbSize2d
  def cnt2dShift = cfg.cnt2dShift
  def rgbUpWidth = 1 << log2Up(Rgb(c=rgbCfg).asBits.getWidth)
  def rgbBusRatio = (busCfg.dataWidth / rgbUpWidth).toInt
  def myBusBurstSizeMaxMult = cfg.myBusBurstSizeMax * rgbBusRatio

  val myD2hShiftedDataStmAdapter = (
    rgbBusRatio > 1
  ) generate (
    LcvBusD2hShiftedDataEtcStreamAdapter(
      cfg=LcvBusD2hShiftedDataEtcStreamAdapterConfig(busCfg=cfg.busCfg)
    )
  )

  val myVideoCfg = LcvVideoDblLineBufWithCalcPosConfig(
    rgbCfg=rgbCfg,
    someSize2d=ElabVec2[Int](
      x=fbSize2d.x,
      y=(fbSize2d.y * (if (cfg.dblBuf) (2) else (1))),
    ),
    cnt2dShift=cfg.cnt2dShift,
    //cnt2dShiftOne=cfg.cnt2dShiftOne
  )
  //val myVideoCfg = LcvVideoDblLineBufWithCalcPosConfig(
  //  rgbCfg=rgbCfg,
  //  someSize2d=ElabVec2[Int](
  //    x=fbSize2d.x,
  //    y=(fbSize2d.y * (if (cfg.dblBuf) (2) else (1))),
  //  ),
  //  cnt2dShift=cfg.cnt2dShift,
  //  //cnt2dShiftOne=cfg.cnt2dShiftOne
  //)

  require(
    busCfg.allowBurst
  )
  require(
    busCfg.dataWidth
    >= rgbUpWidth
    //== rgbUpWidth
  )
  require(
    (fbSize2d.x % myBusBurstSizeMaxMult) == 0,
    s"fbSize2d.x:${fbSize2d.x} must be an exact integer multiple "
    + s"of myBusBurstSizeMaxMult:${myBusBurstSizeMaxMult}"
  )

  val io = LcvBusFramebufferCtrlIo(cfg=cfg)

  val myH2dStm = Vec.fill(3)(
    cloneOf(io.bus.h2dBus)
  )

  //val calcPosStmAdapterArr = Array.fill(
  //  //2 // maybe add this back when there's a line buffer?
  //  1
  //)(
  //  LcvVideoCalcPosStreamAdapter(
  //    someSize2d=(
  //      //someSize2d
  //      myVideoCfg.myCalcPosSize2d
  //    )
  //  )
  //)
  //val myInfoPopStm = Vec.fill(calcPosStmAdapterArr.size)(
  //  cloneOf(calcPosStmAdapterArr.head.io.infoPop)
  //)
  //val myH2dRawCnt2d = calcPosStmAdapterArr.head.io.infoPop.pos
  //val myH2dNextCnt2d = calcPosStmAdapterArr.head.io.infoPop.nextPos
  //val myCnt2dRange = (
  //  ElabVec2(
  //    x=(myH2dRawCnt2d.x.high downto cnt2dShift.x),
  //    y=(myH2dRawCnt2d.y.high downto cnt2dShift.y),
  //  )
  //  //ElabVec2(
  //  //  x=(rH2dRawCnt2d.x.high downto cnt2dShift.x),
  //  //  y=(rH2dRawCnt2d.y.high downto cnt2dShift.y),
  //  //)
  //)
  //val myH2dMainCnt = ElabDualTypeVec2(
  //  x=myH2dRawCnt2d.x(myCnt2dRange.x),
  //  y=myH2dRawCnt2d.y(myCnt2dRange.y),
  //)
  //val myH2dMainNextCnt = ElabDualTypeVec2(
  //  x=myH2dNextCnt2d.x(myCnt2dRange.x),
  //  y=myH2dNextCnt2d.y(myCnt2dRange.y),
  //)
  ////val rH2dMainCnt = ElabDualTypeVec2(
  ////  x=rH2dRawCnt2d.x(myCnt2dRange.x),
  ////  y=rH2dRawCnt2d.y(myCnt2dRange.y),
  ////)
  //for (idx <- 0 until calcPosStmAdapterArr.size) {
  //  myInfoPopStm(idx) <-/< calcPosStmAdapterArr(idx).io.infoPop
  //}

  myH2dStm.head.valid := True
  io.bus.h2dBus << myH2dStm.last
  val myH2dThrowCond = Bool()
  val myH2dMaybeThrownStm = (
    if (cnt2dShift.y == 0) (
      myH2dStm.head//.throwWhen(myH2dThrowCond)
    ) else (
      myH2dStm.head.throwWhen(myH2dThrowCond)
    )
  )
  val rMyDblLineBufIdx = (
    cnt2dShift.y > 0
  ) generate (
    Reg(Bool(), init=False)
  )
  if (cnt2dShift.y > 0) {
    //myH2dThrowCond := False
    myH2dThrowCond := rMyDblLineBufIdx
  }
  myH2dStm(1) <-/< myH2dMaybeThrownStm //myH2dStm.head
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

  val rFbRowCnt = (
    Reg(UInt(
      (
        log2Up(
          (
            myVideoCfg.someSize2d.x
            // / rgbBusRatio
          ) 
          + 1
        )
        + 1
      )
      bits
    ))
    init(0x0)
  )
  val rFbAddrCnt = (
    Reg(UInt(
      log2Up(
        (
          (
            myVideoCfg.someSize2d.y * myVideoCfg.someSize2d.x
            // / rgbBusRatio
          ) //<< (cnt2dShift.x + cnt2dShift.y)
          + 1
        )
        + 1
      ) bits
    ))
    init(0x0)
  )
  when (
    myH2dMaybeThrownStm.fire
    && !rMyDblLineBufIdx
  ) {
    when (
      rFbRowCnt < (
        myVideoCfg.someSize2d.x
        //- 1
        //- cfg.myBusBurstSizeMax
        - myBusBurstSizeMaxMult
      )
    ) {
      rFbRowCnt := (
        rFbRowCnt
        //+ 1
        //+ cfg.myBusBurstSizeMax
        + myBusBurstSizeMaxMult
      )
    } otherwise {
      rMyDblLineBufIdx := True
      rFbRowCnt := 0x0
    }
  }
  when (
    //myH2dStm.head.fire
    //myH2dMaybeThrownStm.fire
    myH2dMaybeThrownStm.fire
  ) {
    //when (
    //  rFbRowCnt < (
    //    myVideoCfg.someSize2d.x
    //    //- 1
    //    - cfg.myBusBurstSizeMax
    //  )
    //) {
    //  rFbRowCnt := (
    //    rFbRowCnt
    //    //+ 1
    //    + cfg.myBusBurstSizeMax
    //  )
    //} otherwise {
    //  rMyDblLineBufIdx := True
    //  rFbRowCnt := 0x0
    //}
    when (
      rFbAddrCnt
      < (
        (
          (
            myVideoCfg.someSize2d.y * myVideoCfg.someSize2d.x
            // / rgbBusRatio
          ) //<< (cnt2dShift.x + cnt2dShift.y)
        )
        //- 1
        //- cfg.myBusBurstSizeMax
        - myBusBurstSizeMaxMult
      )
    ) {
      rFbAddrCnt := (
        rFbAddrCnt
        //+ 1
        //+ cfg.myBusBurstSizeMax
         + myBusBurstSizeMaxMult
      )
    } otherwise {
      rFbAddrCnt := 0x0
    }
  }
  def myDataAssignmentH2d(
    outp: LcvBusH2dPayload,
    inp: Data,
  ): Unit = {
    //myH2dStm.head.valid := (
    //  True // temporary!
    //)
    //outp.addr := (
    //  Cat(
    //    //(rCnt2d.head.y(myCnt2dRange.y) * fbSize2d.x)
    //    //+ rCnt2d.head.x(myCnt2dRange.x)
    //    myHistH2dAddrMult.last,
    //    //U(s"${log2Up(busCfg.dataWidth / 8)}'d0"),
    //    //U(s"${log2Up(Rgb(rgbCfg).asBits.getWidth / 8)}'d0"),
    //    U(s"${log2Up(rgbUpWidth / 8)}'d0"),
    //  ).asUInt.resize(myH2dStm.head.addr.getWidth)
    //  //Cat(
    //  //  cfg.fbMmapCfg.addrSliceValUInt,
    //  //  rCnt.x,
    //  //  U(s"${busCfg.dataWidth / 8}'d0"),
    //  //).asUInt.resize(myH2dStm.head.addr.getWidth)
    //)
    //when (myH2dStm.head.fire) {
    //}
    outp.addr := (
      Cat(
        rFbAddrCnt(
          rFbAddrCnt.high
          downto 0 //cnt2dShift.x + cnt2dShift.y
        ),
        U(s"${log2Up(rgbUpWidth / 8)}'d0"),
      ).asUInt.resize(outp.addr.getWidth)
    )
    outp.src := (
      0x0
      //Cat(
      //  rMyDblLineBufIdx
      //).asUInt.resize(outp.src.getWidth)
    )
    outp.data := 0x0
    outp.byteSize := (
      log2Up(busCfg.dataWidth / 8)
      //log2Up(Rgb(rgbCfg).asBits.getWidth / 8)
      //log2Up(rgbUpWidth / 8)
    )
    outp.isWrite := False

    if (busCfg.allowBurst) {
      outp.burstFirst := True//False//True
      outp.burstCnt := busCfg.maxBurstSizeMinus1//0x0//
      outp.burstLast := False
    }
  }
  myDataAssignmentH2d(
    outp=myH2dStm.head.payload,
    inp=null,
  )

  //myInfoPopStm.head.translateInto(myH2dStm.head)(
  //  dataAssignment=myDataAssignmentH2d
  //)
  //--------
  val myPushStm = Stream(Rgb(rgbCfg))
  //myPushStm.ready := True
  val myD2hStm = Vec.fill(
    //2
    if (rgbBusRatio == 1) (
      2
    ) else (
      //3
      4
    )
  )(
    cloneOf(io.bus.d2hBus)
  )
  myD2hStm.head << io.bus.d2hBus
  //myD2hStm.last <-/< myD2hStm.head.repeat(
  //  times=(
  //    (1 << cnt2dShift.x) //- 1
  //  )
  //)._1
  val rMyD2hCnt = (
    rgbBusRatio > 1
  ) generate (
    Reg(UInt(log2Up(rgbBusRatio) bits))
    init(0x0)
  )
  if (rgbBusRatio == 1) {
    myD2hStm.last << myD2hStm.head
  } else {
    myD2hStm(1) <-/< myD2hStm.head.repeat(
      times=rgbBusRatio
    )._1
    myD2hStm(1).translateInto(myD2hShiftedDataStmAdapter.io.loD2hBus)(
      dataAssignment=(outp, inp) => {
        outp.data := inp.data
        //outp.src := inp.src
        outp.byteSize := log2Up(rgbUpWidth / 8)
        outp.addrLo := 0x0
        outp.addrLo.allowOverride
        outp.addrLo(
          outp.addrLo.high
          downto rMyD2hCnt.getWidth
        ) := rMyD2hCnt
      }
    )
    when (myD2hStm(1).fire) {
      rMyD2hCnt := rMyD2hCnt + 1
    }
    myD2hShiftedDataStmAdapter.io.hiD2hBus.translateInto(myD2hStm(2))(
      dataAssignment=(outp, inp) => {
        outp.data := inp.data
      }
    )
    myD2hStm.last <-/< myD2hStm(2)
  }
  myD2hStm.last.translateInto(
    //io.pop
    myPushStm
  )(
    dataAssignment=(outp, inp) => {
      outp.assignFromBits(inp.data.asBits.resize(outp.asBits.getWidth))
    }
  )
  //--------
  val myNonLineDoublingArea = (
    cnt2dShift.y == 0
  ) generate (new Area {
    if (cnt2dShift.x == 0) {
      io.pop << myPushStm
    } else {
      io.pop <-/< myPushStm.repeat(
        times=(
          (1 << cnt2dShift.x) //- 1
          //* rgbBusRatio
        )
      )._1
    }
  })
  object MyPopState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      USE_D2H_BUS,
      READ_LINE_BUF
      = newElement();
  }
  //val myLineDuplArea = (
  //  cnt2dShift.y == 1
  //) generate (new Area {
  //  val myDblLineBufArr = (
  //  )
  //})
  val myLineDoublingArea = (
    cnt2dShift.y == 1
  ) generate (new Area {
    //myPushStm.ready := True
    val myDblLineBuf = (
      //Array.fill(rgbBusRatio)(
        WrPulseRdPipeRamSdpPipe(
          cfg=myVideoCfg.myMemCfg
        )
      //)
    )
    val myWrPulse = cloneOf(myDblLineBuf.io.wrPulse)
    myDblLineBuf.io.wrPulse <-< myWrPulse
    val rWrLineBufAddrCnt = (
      Reg(UInt(
        myWrPulse.addr.getWidth //- 1 
        + cnt2dShift.x
        bits
      ))
      init(0x0)
    )
    val myMaybeReptPushStm = cloneOf(myPushStm)
    //myMaybeReptPushStm.valid := False
    //myMaybeReptPushStm.payload := myMaybeReptPushStm.payload.getZero
    //myMaybeReptPushStm.ready := False
    myMaybeReptPushStm := myMaybeReptPushStm.getZero

    myWrPulse.valid := (
      if (cnt2dShift.x == 0) (
        myMaybeReptPushStm.fire
      ) else (
        myMaybeReptPushStm.fire
        //&& (
        //  rWrLineBufAddrCnt(
        //    cnt2dShift.x - 1 downto 0
        //  )
        //  === 0x0
        //)
      )
    )
    myWrPulse.data := myMaybeReptPushStm.payload //myPushStm.payload

    val rRdLineBufAddrCnt = (
      Vec[UInt](
        List[UInt](
          (
            Reg(UInt(
              //myWrPulse.addr.getWidth //- 1 
              //bits
              myDblLineBuf.io.rdAddrPipe.addr.getWidth
              //+ cnt2dShift.x
              bits
            ))
            init(0x0)
          ),
          (
            Reg(UInt(
              //myWrPulse.addr.getWidth //- 1 
              //bits
              myDblLineBuf.io.rdAddrPipe.addr.getWidth
              + cnt2dShift.x
              bits
            ))
            init(0x0)
          )
        )
      )
    )
    val myRdAddrPipeStm = cloneOf(myDblLineBuf.io.rdAddrPipe)
    val myRdDataPipeStm = cloneOf(myDblLineBuf.io.rdDataPipe)

    val myMaybeReptRdDataPipeStm = cloneOf(myRdDataPipeStm)
    myMaybeReptRdDataPipeStm := myMaybeReptRdDataPipeStm.getZero

    myRdAddrPipeStm.valid := False
    myRdAddrPipeStm.payload := (
      //RegNext(
      //  myRdAddrPipeStm.payload,
      //  init=myRdAddrPipeStm.payload.getZero
      //)
      myRdAddrPipeStm.payload.getZero.getZero
    )
    myRdAddrPipeStm.addr.allowOverride
    myRdAddrPipeStm.addr := (
      rRdLineBufAddrCnt.head(
        rRdLineBufAddrCnt.head.high
        downto 0//cnt2dShift.x
      )
    )

    myDblLineBuf.io.rdAddrPipe <-/< myRdAddrPipeStm
    myRdDataPipeStm <-/< myDblLineBuf.io.rdDataPipe

    io.pop.valid := False
    io.pop.payload := RegNext(io.pop.payload, init=io.pop.payload.getZero)

    //val myStickyD2hSrc = cloneOf(myD2hStm.last.src)
    //myStickyD2hSrc := RegNext(myStickyD2hSrc, init=myStickyD2hSrc.getZero)

    //when (myD2hStm.last.valid) {
    //  myStickyD2hSrc := myD2hStm.last.src
    //}
    val rSeenRdAddrPipeFinish = Reg(Bool(), init=False)
    val rSeenRdDataPipeFinish = Reg(Bool(), init=False)

    myWrPulse.addr := (
      //Cat(
        //myStickyD2hSrc,
        rWrLineBufAddrCnt(
          rWrLineBufAddrCnt.high
          downto cnt2dShift.x
        ),
      //).asUInt
    )
    val rMyPopState = (
      Reg(MyPopState())
      init(MyPopState.USE_D2H_BUS)
    )
    switch (rMyPopState) {
      is (MyPopState.USE_D2H_BUS) {
        //io.pop <-/< myPushStm
        myRdDataPipeStm.ready := False
        if (cnt2dShift.x == 0) {
          //io.pop <-/< myPushStm
          myMaybeReptPushStm << myPushStm
          io.pop << myMaybeReptPushStm
        } else {
          //io.pop <-/< myPushStm.repeat(
          //  times=(
          //    (1 << cnt2dShift.x) //- 1
          //  )
          //)._1
          myMaybeReptPushStm << myPushStm.repeat(
            times=(
              (1 << cnt2dShift.x) //- 1
              //* rgbBusRatio
            )
          )._1
          io.pop << myMaybeReptPushStm
        }

        switch (
          //myPushStm.fire
          myMaybeReptPushStm.fire
          ## (
            rWrLineBufAddrCnt
            < ((myVideoCfg.someSize2d.x << cnt2dShift.x) - 1)
          )
        ) {
          is (M"11") {
            // fire, rWrLineBufAddrCnt < width
            rWrLineBufAddrCnt := rWrLineBufAddrCnt + 1
          }
          is (M"10") {
            // fire, !(rWrLineBufAddrCnt < width)
            rWrLineBufAddrCnt := 0x0
            rMyPopState := MyPopState.READ_LINE_BUF
          }
          default {
          }
        }
        //when (
        //  //myD2hStm.last.fire
        //  myPushStm.fire
        //  //myPushStm.valid
        //  //myWrPulse.fire
        //  //io.pop.fire
        //) {
        //  when (rWrLineBufAddrCnt < myVideoCfg.someSize2d.x - 1) {
        //    rWrLineBufAddrCnt := rWrLineBufAddrCnt + 1
        //  } otherwise {
        //    rWrLineBufAddrCnt := 0x0
        //    rMyPopState := MyPopState.READ_LINE_BUF
        //  }
        //}
      }
      is (MyPopState.READ_LINE_BUF) {
        //io.pop <-/< myRdDataPipeStm
        myRdDataPipeStm.ready := False
        myPushStm.ready := False
        myRdAddrPipeStm.valid := (
          //rRdLineBufAddrCnt.head
          ////< (myVideoCfg.someSize2d.x << cnt2dShift.x) - 1
          //< myVideoCfg.someSize2d.x  - 1
          !rSeenRdAddrPipeFinish
        )

        switch (
          //myPushStm.fire
          //myDblLineBuf.io.rdAddrPipe.fire
          myRdAddrPipeStm.fire
          ## (
            rRdLineBufAddrCnt.head
            //< (myVideoCfg.someSize2d.x << cnt2dShift.x) - 1
            < myVideoCfg.someSize2d.x - 1
          )
        ) {
          is (M"11") {
            // fire, rRdLineBufAddrCnt.head < width
            rRdLineBufAddrCnt.head := rRdLineBufAddrCnt.head + 1
          }
          is (M"10") {
            // fire, !(rRdLineBufAddrCnt < width)
            //rRdLineBufAddrCnt := 0x0
            //rMyPopState := MyPopState.READ_LINE_BUF
            //rRdLineBufAddrCnt.head := rRdLineBufAddrCnt.head + 1
            rSeenRdAddrPipeFinish := True
          }
          default {
          }
        }

        switch (
          //myPushStm.fire
          //myDblLineBuf.io.rdDataPipe.fire
          //myRdDataPipeStm.fire
          myMaybeReptRdDataPipeStm.fire
          ## (
            rRdLineBufAddrCnt.last
            < (myVideoCfg.someSize2d.x << cnt2dShift.x) - 1
          )
        ) {
          is (M"11") {
            // fire, rRdLineBufAddrCnt.last < width
            rRdLineBufAddrCnt.last := rRdLineBufAddrCnt.last + 1
          }
          is (M"10") {
            // fire, !(rRdLineBufAddrCnt < width)
            //rRdLineBufAddrCnt := 0x0
            //rMyPopState := MyPopState.READ_LINE_BUF
            //rRdLineBufAddrCnt.last := rRdLineBufAddrCnt.last + 1
            rSeenRdDataPipeFinish := True
          }
          default {
          }
        }
        when (rSeenRdAddrPipeFinish && rSeenRdDataPipeFinish) {
          rSeenRdAddrPipeFinish := False
          rSeenRdDataPipeFinish := False
          rRdLineBufAddrCnt.foreach(item => {
            item := 0
          })
          rMyPopState := MyPopState.USE_D2H_BUS
          rMyDblLineBufIdx := False
        } otherwise {
          if (cnt2dShift.x == 0) {
            //io.pop <-/< myRdDataPipeStm
            myMaybeReptRdDataPipeStm << myRdDataPipeStm
            io.pop << myMaybeReptRdDataPipeStm
          } else {
            //io.pop <-/< myRdDataPipeStm.repeat(
            //  times=(
            //    (1 << cnt2dShift.x) //- 1
            //  )
            //)._1
            myMaybeReptRdDataPipeStm << myRdDataPipeStm.repeat(
              times=(
                (1 << cnt2dShift.x) //- 1
              )
            )._1
            io.pop << myMaybeReptRdDataPipeStm
          }

        }
      }
    }
  })

  //val myForkStmVec = StreamFork(
  //  input=(
  //    //io.push
  //    myPushStm
  //  ),
  //  portCount=2,
  //  synchronous=(
  //    //true
  //    false
  //  ),
  //)

  ////myForkStmVec.head.ready := True
  ////calcPosStmAdapterArr.last.io.infoPop.ready := (
  ////  //io.push.fire
  ////  myForkStmVec.last.fire
  ////)

  //myWrPulse.valid := (
  //  //io.push.valid
  //  //io.push.fire
  //  myForkStmVec.head.valid
  //)
  //myWrPulse.data := (
  //  //io.push.payload
  //  myForkStmVec.head.payload
  //)
  ////println(
  ////  s"test: "
  ////  + s"${someSize2d} ${log2Up(someSize2d.x)} "
  ////  + s"${myDblLineBuf.cfg.wordCount} "
  ////  + s"${calcPos.io.info.pos.x.getWidth} "
  ////  + s"${calcPos.io.info.pos.x.getWidth - cnt2dShift.x} "
  ////  + s"${calcPos.io.info.pos.y.getWidth} "
  ////  + s"${myDblLineBuf.io.wrPulse.addr.getWidth}"
  ////)
  //myWrPulse.addr := (
  //  Cat(
  //    //calcPosStmAdapterArr.last.io.infoPop.pos.y(cnt2dShift.y),
  //    //(!rMyDblLineBufIdx),
  //    //rMyDblLineBufIdx,
  //    myInfoPopStm.last.pos.y(cnt2dShift.y),
  //    myInfoPopStm.last.pos.x(
  //      //calcPos.io.info.pos.x.high
  //      //log2Up(someSize2d.x) + cnt2dShift.x - 1
  //      log2Up(fbSize2d.x) + cnt2dShift.x - 1
  //      downto cnt2dShift.x
  //    ),
  //  ).asUInt//.resize(myWrPulseStm.addr.getWidth)
  //)
  //myDblLineBuf.io.wrPulse <-< myWrPulse
  //--------

}

//case class LcvBusFramebufferCtrlWithDblLineBuf(
//  cfg: LcvBusFramebufferConfig
//) extends Component {
//  //--------
//  def rgbCfg = cfg.rgbCfg
//  def busCfg = cfg.busCfg
//  def fbSize2d = cfg.fbSize2d
//  def cnt2dShift = cfg.cnt2dShift
//  def rgbUpWidth = 1 << log2Up(Rgb(c=rgbCfg).asBits.getWidth)
//  require(
//    busCfg.allowBurst
//  )
//  require(
//    busCfg.dataWidth
//    >= rgbUpWidth
//  )
//  //require(
//  //  (fbSize2d.x % cfg.myBusBurstSizeMax) == 0,
//  //  s"fbSize2d.x:${fbSize2d.x} must be an exact integer multiple "
//  //  + s"of cfg.myBusBurstSizeMax:${cfg.myBusBurstSizeMax}"
//  //)
//  //--------
//  val io = LcvBusFramebufferCtrlIo(cfg=cfg)
//  //--------
//  //val myH2dCalcPos = LcvVideoCalcPos(
//  //  someSize2d=fbSize2d
//  //)
//  val myDblLineBufEtc = LcvVideoDblLineBufWithCalcPos(
//    cfg=LcvVideoDblLineBufWithCalcPosConfig(
//      rgbCfg=rgbCfg,
//      someSize2d=ElabVec2[Int](
//        x=fbSize2d.x,
//        y=(fbSize2d.y * (if (cfg.dblBuf) (2) else (1))),
//      ),
//      cnt2dShiftOne=cfg.cnt2dShiftOne
//    )
//  )
//  //val rH2dRawCnt2d = {
//  //  //Vec.fill(2)({
//  //    val temp = Reg(
//  //      DualTypeNumVec2(
//  //        dataTypeX=UInt(log2Up(fbSize2d.x + 1) + cnt2dShift.x bits),
//  //        dataTypeY=(
//  //          UInt(
//  //            (if (cfg.dblBuf) (1) else (0))
//  //            + (
//  //              log2Up(fbSize2d.y + 1) + cnt2dShift.y 
//  //            )
//  //            bits
//  //          )
//  //        ),
//  //      )
//  //    )
//  //    temp.init(temp.getZero)
//  //    temp
//  //  //})
//  //}
//  val myH2dRawCnt2d = myDblLineBufEtc.io.infoPop.pos
//  val myH2dNextCnt2d = myDblLineBufEtc.io.infoPop.nextPos
//  val myCnt2dRange = (
//    ElabVec2(
//      x=(myH2dRawCnt2d.x.high downto cnt2dShift.x),
//      y=(myH2dRawCnt2d.y.high downto cnt2dShift.y),
//    )
//    //ElabVec2(
//    //  x=(rH2dRawCnt2d.x.high downto cnt2dShift.x),
//    //  y=(rH2dRawCnt2d.y.high downto cnt2dShift.y),
//    //)
//  )
//  val myH2dMainCnt = ElabDualTypeVec2(
//    x=myH2dRawCnt2d.x(myCnt2dRange.x),
//    y=myH2dRawCnt2d.y(myCnt2dRange.y),
//  )
//  val myH2dMainNextCnt = ElabDualTypeVec2(
//    x=myH2dNextCnt2d.x(myCnt2dRange.x),
//    y=myH2dNextCnt2d.y(myCnt2dRange.y),
//  )
//  //val rH2dMainCnt = ElabDualTypeVec2(
//  //  x=rH2dRawCnt2d.x(myCnt2dRange.x),
//  //  y=rH2dRawCnt2d.y(myCnt2dRange.y),
//  //)
//
//  //--------
//  val myH2dStm = Vec.fill(4)(
//    cloneOf(io.bus.h2dBus)
//  )
//  val myHistH2dFire = History[Bool](
//    that=False,
//    when=myH2dStm.head.fire,
//    length=2,
//    init=True,
//  )
//  val myLongerHistH2dFire = History[Bool](
//    that=False,
//    when=myH2dStm.head.fire,
//    length=3,
//    init=True,
//  )
//  val myH2dThrowCond = Bool()
//  myH2dThrowCond := (
//    myHistH2dFire.last
//    //myLongerHistH2dFire.last
//  )
//  io.bus.h2dBus <-/< myH2dStm.last
//  myH2dStm(1) <-/< myH2dStm.head
//  myH2dStm(2) <-/< myH2dStm(1).throwWhen(myH2dThrowCond)
//  myH2dStm(2).translateInto(myH2dStm.last)(
//    dataAssignment=(outp, inp) => {
//      outp := inp
//      outp.addr.allowOverride
//      outp.addr(cfg.fbMmapCfg.addrSliceRange) := (
//        cfg.fbMmapCfg.addrSliceValUInt
//      )
//      //outp.addr(
//      //)
//    }
//  )
//  val myH2dAddrMult = (
//    //(rH2dMainCnt.y * fbSize2d.x) + rH2dMainCnt.x
//    (myH2dMainCnt.y * fbSize2d.x) + myH2dMainCnt.x
//    //(
//    //  myDblLineBufEtc.io.infoPop.pos.y * fbSize2d.y
//    //)
//    //+ myDblLineBufEtc.io.infoPop.pos.x
//  )
//  //val rSavedH2dAddrMult = (
//  //  Reg(cloneOf(myH2dAddrMult), init=myH2dAddrMult.getZero)
//  //)
//  val myHistH2dAddrMult = History[UInt](
//    that=myH2dAddrMult,
//    length=(
//      //3
//      1
//    ),
//    init=myH2dAddrMult.getZero,
//  )
//  val myCntH2dMax = (
//    fbSize2d.x * (1 << cnt2dShift.x)
//  )
//  val myCntH2dFireRstVal = (
//    //fbSize2d.x + 1 //- 2 //- 2
//    //fbSize2d.x - 1 //2 //+ (1 << cnt2dShift.x) //- 2 //- 2
//    //fbSize2d.x - 1//2//1//2 //+ (1 << cnt2dShift.x) //- 2 //- 2
//    myCntH2dMax - 1
//  )
//  val rCntH2dFire = (
//    Reg(UInt(
//      log2Up(
//      //fbSize2d.x + 1
//        myCntH2dMax
//      ) + 1 bits
//    ))
//    init(
//      myCntH2dFireRstVal
//      //fbSize2d.x - 1
//      //fbSize2d.x - 2
//    )
//  )
//  val myHistInfoPopFire = (
//    History[Bool](
//      that=True,
//      when=myDblLineBufEtc.io.infoPop.fire,
//      length=2,
//      init=False,
//    )
//  )
//  val myCntH2dFireRstCond = (
//    //myH2dMainCnt.y
//    //=== RegNextWhen(
//    //  (myH2dMainCnt.y + 1),
//    //  cond=myDblLineBufEtc.io.infoPop.fire,
//    //  init=myH2dMainCnt.y.getZero,
//    //)
//    (
//      myH2dMainNextCnt.y
//      === RegNextWhen(
//        (myH2dMainNextCnt.y + 1),
//        cond=myDblLineBufEtc.io.infoPop.fire,
//        init=myH2dMainNextCnt.y.getZero,
//      )
//      || myDblLineBufEtc.io.infoPop.posWillOverflow.y
//    )
//    && myDblLineBufEtc.io.infoPop.valid
//    //&& myDblLineBufEtc.io.infoPop.posWillOverflow.x
//    && myHistInfoPopFire.last
//  )
//
//  val mySeenH2dFire = Bool()
//  val rSavedSeenH2dFire = Reg(Bool(), init=False)
//  val stickySeenH2dFire = (
//    mySeenH2dFire
//    || rSavedSeenH2dFire
//  )
//  mySeenH2dFire := myH2dStm.head.fire//io.bus.h2dBus.fire //
//  when (mySeenH2dFire) {
//    rSavedSeenH2dFire := True
//  }
//  val mySeenInfoPopFire = Bool()
//  val rSavedSeenInfoPopFire = Reg(Bool(), init=False)
//  val stickySeenInfoPopFire = (
//    mySeenInfoPopFire
//    || rSavedSeenInfoPopFire
//  )
//  mySeenInfoPopFire := myDblLineBufEtc.io.infoPop.fire
//  when (mySeenInfoPopFire) {
//    rSavedSeenInfoPopFire := True
//  }
//
//  val mySeenPopFire = Bool()
//  val rSavedSeenPopFire = Reg(Bool(), init=False)
//  val stickySeenPopFire = (
//    mySeenPopFire
//    || rSavedSeenPopFire
//  )
//  mySeenPopFire := io.pop.fire
//  when (mySeenPopFire) {
//    rSavedSeenPopFire := True
//  }
//
//  when (  
//    //myH2dStm.head.fire
//    stickySeenH2dFire
//    && stickySeenInfoPopFire
//    && !rCntH2dFire.msb
//  ) {
//    rSavedSeenH2dFire := False
//    rSavedSeenInfoPopFire := False
//    rCntH2dFire := rCntH2dFire - 1
//  }
//  when (
//    myCntH2dFireRstCond
//    && myDblLineBufEtc.io.infoPop.fire
//  ) {
//    //rSavedSeenH2dFire := False
//    //rSavedSeenInfoPopFire := False
//    rCntH2dFire := myCntH2dFireRstVal //fbSize2d.x - 2//1
//  }
//
//  //val myTempH2dValidCond = (
//  //  myH2d
//  //)
//
//  val myTempH2dValidCond = (
//    myH2dMainCnt.x
//    === RegNextWhen(
//      myH2dMainCnt.x + 1,
//      cond=myDblLineBufEtc.io.infoPop.fire,
//      init=myH2dMainCnt.x.getZero,
//    )
//    //|| myH2dMainCnt.x === 0x0
//    || myHistInfoPopFire.last
//  )
//
//  val myTempH2dValidMux = (
//    Mux[Bool](
//      myDblLineBufEtc.io.infoPop.valid,
//      myCntH2dFireRstCond,
//      True
//      //False
//    )
//  )
//  myH2dStm.head.valid := (
//    //(
//    //  //True//False
//    //  //rose(
//    //    //myDblLineBufEtc.io.infoPop.valid
//    //    myDblLineBufEtc.io.infoPop.fire
//    //  //)
//    //  //|| (
//    //  //  myDblLineBufEtc.io.infoPop.valid
//    //  //  && !myH2dStm.head.ready
//    //  //)
//    //  || 
//    //  History[Bool](
//    //    that=False,
//    //    when=myH2dStm.head.fire,
//    //    length=2,
//    //    init=True,
//    //  ).last
//    //)
//    myHistH2dFire.last
//    //!rCntH2dFire.msb
//    ||
//    (
//      (
//        !rCntH2dFire.msb
//      )
//      && stickySeenInfoPopFire//myDblLineBufEtc.io.infoPop.valid
//      && myTempH2dValidCond
//      //&& myDblLineBufEtc.io.infoPop.fire
//      //&& myTempH2dValidMux
//    )
//  )
//  //--------
//  val myD2hStm = Vec.fill(2)(
//    cloneOf(io.bus.d2hBus)
//  )
//  //val myHistD2hLastFire = (
//  //  History[Bool](
//  //    that=True
//  //    when=myD2hStm.last.fire,
//  //    length=2
//  //  )
//  //)
//  val myHistD2hFire = (
//    History[Bool](
//      that=False,
//      when=myD2hStm.head.fire,
//      length=2,
//      init=True,
//    )
//  )
//  //--------
//  myDblLineBufEtc.io.infoPop.ready := (
//    //myH2dStm.head.fire
//    //io.pop.fire
//    myHistD2hFire.last
//    || stickySeenPopFire
//  )
//  when (myDblLineBufEtc.io.infoPop.fire) {
//    rSavedSeenPopFire := False
//  }
//
//  myH2dStm.head.addr := (
//    Cat(
//      //(rCnt2d.head.y(myCnt2dRange.y) * fbSize2d.x)
//      //+ rCnt2d.head.x(myCnt2dRange.x)
//      myHistH2dAddrMult.last,
//      //U(s"${log2Up(busCfg.dataWidth / 8)}'d0"),
//      //U(s"${log2Up(Rgb(rgbCfg).asBits.getWidth / 8)}'d0"),
//      U(s"${log2Up(rgbUpWidth / 8)}'d0"),
//    ).asUInt.resize(myH2dStm.head.addr.getWidth)
//    //Cat(
//    //  cfg.fbMmapCfg.addrSliceValUInt,
//    //  rCnt.x,
//    //  U(s"${busCfg.dataWidth / 8}'d0"),
//    //).asUInt.resize(myH2dStm.head.addr.getWidth)
//  )
//  myH2dStm.head.data := 0x0
//  myH2dStm.head.byteSize := (
//    //log2Up(busCfg.dataWidth / 8)
//    //log2Up(Rgb(rgbCfg).asBits.getWidth / 8)
//    log2Up(rgbUpWidth / 8)
//  )
//  myH2dStm.head.isWrite := False
//
//  if (busCfg.allowBurst) {
//    myH2dStm.head.burstFirst := False//True
//    myH2dStm.head.burstCnt := 0x0//busCfg.maxBurstSizeMinus1
//    myH2dStm.head.burstLast := False
//  }
//  //--------
//  myD2hStm.head <-/< io.bus.d2hBus
//  //myD2hStm.ready := True
//
//  //val myD2hThrowCond = Bool()
//  myD2hStm.last <-/< myD2hStm.head//.throwWhen(myD2hThrowCond)
//  myD2hStm.last.ready := True
//
//  //myD2hThrowCond := myHistD2hFire.last
//
//  myDblLineBufEtc.io.push.valid := (
//    myD2hStm.last.valid
//  )
//
//  myDblLineBufEtc.io.push.payload.assignFromBits(
//    myD2hStm.last.data.resize(
//      myDblLineBufEtc.io.push.payload.asBits.getWidth
//    ).asBits
//  )
//  //io.pop <-/< myDblLineBufEtc.io.pop
//  io.pop << myDblLineBufEtc.io.pop
//  //--------
//}

//case class LcvBusFramebufferCtrlDualCntWithBurstWithLineBuf(
//  cfg: LcvBusFramebufferConfig
//) extends Component {
//  //--------
//  def busCfg = cfg.busCfg
//  def rgbCfg = cfg.rgbCfg
//  def fbSize2d = cfg.fbSize2d
//  def cnt2dShift = cfg.cnt2dShift
//  def rgbUpWidth = 1 << log2Up(Rgb(c=rgbCfg).asBits.getWidth)
//  def rgbBusRatio = (busCfg.dataWidth / rgbUpWidth).toInt
//
//  require(
//    busCfg.allowBurst
//  )
//
//  require(
//    busCfg.dataWidth
//    //>= Rgb(c=rgbCfg).asBits.getWidth
//    >= rgbUpWidth
//    //== rgbUpWidth,
//    //s"Perhaps temporarily, it is required that "
//    //+ s"busCfg.dataWidth:${busCfg.dataWidth} "
//    //+ s"== rgbUpWidth:${rgbUpWidth}"
//  )
//  require(
//    //busCfg.dataWidth
//    /// Rgb(rgbCfg).asBits.getWidth
//    //(fbSize2d.x % (cfg.myBusBurstSizeMax * rgbBusRatio)) == 0
//    (cfg.myFbSize2dMult % (cfg.myBusBurstSizeMax * rgbBusRatio)) == 0
//  )
//  require(
//    cfg.myFbSize2dMult == cfg.myAlignedFbCntMax
//  )
//  //--------
//  val io = LcvBusFramebufferCtrlIo(cfg=cfg)
//  val myDblLineBufEtc = LcvVideoDblLineBufWithCalcPos(
//    cfg=LcvVideoDblLineBufWithCalcPosConfig(
//      rgbCfg=rgbCfg,
//      someSize2d=fbSize2d,
//      cnt2dShift=cnt2dShift,
//      //bufElemSize
//    )
//  )
//  //--------
//  //val rD2hCnt = {
//  //  val temp = Reg(
//  //    UInt(log2Up(cfg.myAlignedFbCntMax + 1) bits)
//  //  )
//  //  temp.init(temp.getZero)
//  //  temp
//  //}
//
//  //val myH2dCalcPosSize2d = (
//  //  ElabVec2[Int](
//  //    x=(fbSize2d.x / (cfg.myBusBurstSizeMax * rgbBusRatio)),
//  //    y=(
//  //      fbSize2d.y
//  //      * (if (cfg.dblBuf) (2) else (1))
//  //    ),
//  //  )
//  //)
//  ////val myD2hCalcPosSize2d = (
//  ////  ElabVec2[Int](
//  ////    x=(fbSize2d.x * (1 << cnt2dShift.x)),
//  ////    y=(
//  ////      fbSize2d.y * (1 << cnt2dShift.y)
//  ////      * (if (cfg.dblBuf) (2) else (1))
//  ////    ),
//  ////  )
//  ////)
//
//  //val myH2dCalcPos = LcvVideoCalcPos(someSize2d=myH2dCalcPosSize2d)
//  ////val myD2hCalcPos = LcvVideoCalcPos(someSize2d=myD2hCalcPosSize2d)
//  //val myH2dMainPosRange = ElabDualTypeVec2(
//  //  x=(myH2dCalcPos.io.info.pos.x.high downto cnt2dShift.x),
//  //  y=(myH2dCalcPos.io.info.pos.y.high downto cnt2dShift.y),
//  //)
//  ////val myD2hMainPosRange = ElabDualTypeVec2(
//  ////  x=(myD2hCalcPos.io.info.pos.x.high downto cnt2dShift.x),
//  ////  y=(myD2hCalcPos.io.info.pos.y.high downto cnt2dShift.y),
//  ////)
//  //val myH2dMainPos = ElabDualTypeVec2(
//  //  x=myH2dCalcPos.io.info.pos.x(myH2dMainPosRange.x),
//  //  y=myH2dCalcPos.io.info.pos.y(myH2dMainPosRange.y),
//  //)
//  ////val myD2hMainPos = ElabDualTypeVec2(
//  ////  x=myD2hCalcPos.io.info.pos.x(myD2hMainPosRange.x),
//  ////  y=myD2hCalcPos.io.info.pos.y(myD2hMainPosRange.y),
//  ////)
//  //--------
//  val myH2dStm = Vec.fill(3)(
//    cloneOf(io.bus.h2dBus)
//  )
//  io.bus.h2dBus <-/< myH2dStm.last
//  // The multiplication to calculate the address is why we use `<-<`.
//  // Hopefully it will be seen as a purely registered multiply
//  // by synth + pnr tools!
//  // As of this writing, I haven't tried yet.
//  myH2dStm(1) <-< myH2dStm.head
//  myH2dStm(1).translateInto(myH2dStm.last)(
//    dataAssignment=(outp, inp) => {
//      outp := inp
//      outp.addr.allowOverride
//      outp.addr(cfg.fbMmapCfg.addrSliceRange) := (
//        cfg.fbMmapCfg.addrSliceValUInt
//      )
//      //outp.addr(
//      //)
//    }
//  )
//  myH2dStm.head.valid := True
//
//  //myH2dStm.head.addr := (
//  //  Cat(
//  //    //(rCnt2d.head.y(myCnt2dRange.y) * fbSize2d.x)
//  //    //+ rCnt2d.head.x(myCnt2dRange.x)
//  //    //(rH2dMainCnt.y * fbSize2d.x) + rH2dMainCnt.x
//  //    (
//  //      //myH2dCalcPos.io.info.pos.y(myMainPosRange.y) * fbSize2d.x
//  //      //+ myH2dCalcPos.io.info.pos.x(myMainPosRange.x)
//  //      (myH2dMainPos.y * fbSize2d.x) + myH2dMainPos.x
//  //    ),
//  //    U(s"${log2Up(rgbUpWidth / 8)}'d0"),
//  //  ).asUInt.resize(myH2dStm.head.addr.getWidth)
//  //)
//  val myTempH2dAddrRange = (
//    //myH2dStm.head.addr.high
//    //log2Up(fbSize2d.x + 1)
//    log2Up(cfg.myAlignedFbCntMax)
//    downto log2Up(busCfg.burstCntMaxNumBytes)
//  )
//  myH2dStm.head.addr.allowOverride
//  myH2dStm.head.addr := 0x0
//  myH2dStm.head.addr(myTempH2dAddrRange) := (
//    RegNextWhen(
//      (myH2dStm.head.addr(myTempH2dAddrRange) + 1),
//      cond=myH2dStm.head.fire,
//      init=myH2dStm.head.addr(myTempH2dAddrRange).getZero,
//    )
//  )
//  myH2dStm.head.data := 0x0
//  myH2dStm.head.byteSize := (
//    log2Up(busCfg.dataWidth / 8)
//    //log2Up(Rgb(rgbCfg).asBits.getWidth / 8)
//    //log2Up(rgbUpWidth / 8)
//  )
//  myH2dStm.head.isWrite := False
//
//  myH2dStm.head.burstFirst := True
//  myH2dStm.head.burstCnt := busCfg.maxBurstSizeMinus1
//  myH2dStm.head.burstLast := False
//  //if (busCfg.allowBurst) {
//  //  myH2dStm.head.burstFirst := False//True
//  //  myH2dStm.head.burstCnt := 0x0//busCfg.maxBurstSizeMinus1
//  //  myH2dStm.head.burstLast := False
//  //}
//
//  //--------
//  //val myD2hStm = Vec.fill(2)(
//  //  cloneOf(io.bus.d2hBus)
//  //)
//  //myD2hStm.head <-/< io.bus.d2hBus
//  val myD2hStm = cloneOf(io.bus.d2hBus)
//  myD2hStm <-/< io.bus.d2hBus
//  myD2hStm.ready := True
//  myDblLineBufEtc.io.push.valid := myD2hStm.fire
//  myDblLineBufEtc.io.push.payload.assignFromBits(
//    myD2hStm.data.resize(
//      myDblLineBufEtc.io.push.payload.asBits.getWidth
//    ).asBits
//  )
//  io.pop <-/< myDblLineBufEtc.io.pop
//  //--------
//  //val myRgbFifo = StreamFifo(
//  //  dataType=(
//  //    //LcvBusD2hPayload(cfg=busCfg)
//  //    Rgb(rgbCfg)
//  //  ),
//  //  depth=(
//  //    //busCfg.maxBurstSizeMinus1 + 1
//  //    cfg.myBusBurstSizeMax
//  //  ),
//  //  latency=(
//  //    //0
//  //    2
//  //  ),
//  //  forFMax=true,
//  //)
//
//  ////myD2hCalcPos.io.en := False
//
//  //myD2hStm.last.translateInto(myRgbFifo.io.push)(
//  //  dataAssignment=(outp, inp) => {
//  //    outp.assignFromBits(inp.data.asBits.resize(outp.asBits.getWidth))
//  //  }
//  //)
//
//  //io.pop <-/< myRgbFifo.io.pop //myPopStm
//  //myRgbFifo.io.pop.ready := True
//  //myRgbFifo.io
//  //--------
//  //--------
//  //val myD2hThrowCond = Bool()
//  //myD2hStm.last << myD2hStm.head.throwWhen(myD2hThrowCond)
//}

//case class LcvBusFramebufferCtrlDualCntNonBurstWithLineBuf(
//  cfg: LcvBusFramebufferConfig
//) extends Component {
//  //--------
//  def busCfg = cfg.busCfg
//  def rgbCfg = cfg.rgbCfg
//  def fbSize2d = cfg.fbSize2d
//  def cnt2dShift = cfg.cnt2dShift
//  def rgbUpWidth = 1 << log2Up(Rgb(c=rgbCfg).asBits.getWidth)
//  def rgbBusRatio = (busCfg.dataWidth / rgbUpWidth).toInt
//
//  require(
//    !busCfg.allowBurst
//  )
//
//  require(
//    busCfg.dataWidth
//    //>= Rgb(c=rgbCfg).asBits.getWidth
//    >= rgbUpWidth
//    //== rgbUpWidth,
//    //s"Perhaps temporarily, it is required that "
//    //+ s"busCfg.dataWidth:${busCfg.dataWidth} "
//    //+ s"== rgbUpWidth:${rgbUpWidth}"
//  )
//  //require(
//  //  //busCfg.dataWidth
//  //  /// Rgb(rgbCfg).asBits.getWidth
//  //  //(fbSize2d.x % (cfg.myBusBurstSizeMax * rgbBusRatio)) == 0
//  //  (cfg.myFbSize2dMult % (cfg.myBusBurstSizeMax * rgbBusRatio)) == 0
//  //)
//  //require(
//  //  cfg.myFbSize2dMult == cfg.myAlignedFbCntMax
//  //)
//  //--------
//  val io = LcvBusFramebufferCtrlIo(cfg=cfg)
//  val myDblLineBufEtc = LcvVideoDblLineBufWithCalcPos(
//    cfg=LcvVideoDblLineBufWithCalcPosConfig(
//      rgbCfg=rgbCfg,
//      someSize2d=fbSize2d,
//      cnt2dShift=cnt2dShift,
//      //bufElemSize
//    )
//  )
//  //--------
//  //val rD2hCnt = {
//  //  val temp = Reg(
//  //    UInt(log2Up(cfg.myAlignedFbCntMax + 1) bits)
//  //  )
//  //  temp.init(temp.getZero)
//  //  temp
//  //}
//
//  //val myH2dCalcPosSize2d = (
//  //  ElabVec2[Int](
//  //    x=(fbSize2d.x / (cfg.myBusBurstSizeMax * rgbBusRatio)),
//  //    y=(
//  //      fbSize2d.y
//  //      * (if (cfg.dblBuf) (2) else (1))
//  //    ),
//  //  )
//  //)
//  ////val myD2hCalcPosSize2d = (
//  ////  ElabVec2[Int](
//  ////    x=(fbSize2d.x * (1 << cnt2dShift.x)),
//  ////    y=(
//  ////      fbSize2d.y * (1 << cnt2dShift.y)
//  ////      * (if (cfg.dblBuf) (2) else (1))
//  ////    ),
//  ////  )
//  ////)
//
//  //val myH2dCalcPos = LcvVideoCalcPos(someSize2d=myH2dCalcPosSize2d)
//  ////val myD2hCalcPos = LcvVideoCalcPos(someSize2d=myD2hCalcPosSize2d)
//  //val myH2dMainPosRange = ElabDualTypeVec2(
//  //  x=(myH2dCalcPos.io.info.pos.x.high downto cnt2dShift.x),
//  //  y=(myH2dCalcPos.io.info.pos.y.high downto cnt2dShift.y),
//  //)
//  ////val myD2hMainPosRange = ElabDualTypeVec2(
//  ////  x=(myD2hCalcPos.io.info.pos.x.high downto cnt2dShift.x),
//  ////  y=(myD2hCalcPos.io.info.pos.y.high downto cnt2dShift.y),
//  ////)
//  //val myH2dMainPos = ElabDualTypeVec2(
//  //  x=myH2dCalcPos.io.info.pos.x(myH2dMainPosRange.x),
//  //  y=myH2dCalcPos.io.info.pos.y(myH2dMainPosRange.y),
//  //)
//  ////val myD2hMainPos = ElabDualTypeVec2(
//  ////  x=myD2hCalcPos.io.info.pos.x(myD2hMainPosRange.x),
//  ////  y=myD2hCalcPos.io.info.pos.y(myD2hMainPosRange.y),
//  ////)
//  //--------
//  val myH2dStm = Vec.fill(3)(
//    cloneOf(io.bus.h2dBus)
//  )
//  io.bus.h2dBus <-/< myH2dStm.last
//  // The multiplication to calculate the address is why we use `<-<`.
//  // Hopefully it will be seen as a purely registered multiply
//  // by synth + pnr tools!
//  // As of this writing, I haven't tried yet.
//  myH2dStm(1) <-< myH2dStm.head
//  myH2dStm(1).translateInto(myH2dStm.last)(
//    dataAssignment=(outp, inp) => {
//      outp := inp
//      outp.addr.allowOverride
//      outp.addr(cfg.fbMmapCfg.addrSliceRange) := (
//        cfg.fbMmapCfg.addrSliceValUInt
//      )
//      //outp.addr(
//      //)
//    }
//  )
//  myH2dStm.head.valid := True
//
//  //myH2dStm.head.addr := (
//  //  Cat(
//  //    //(rCnt2d.head.y(myCnt2dRange.y) * fbSize2d.x)
//  //    //+ rCnt2d.head.x(myCnt2dRange.x)
//  //    //(rH2dMainCnt.y * fbSize2d.x) + rH2dMainCnt.x
//  //    (
//  //      //myH2dCalcPos.io.info.pos.y(myMainPosRange.y) * fbSize2d.x
//  //      //+ myH2dCalcPos.io.info.pos.x(myMainPosRange.x)
//  //      (myH2dMainPos.y * fbSize2d.x) + myH2dMainPos.x
//  //    ),
//  //    U(s"${log2Up(rgbUpWidth / 8)}'d0"),
//  //  ).asUInt.resize(myH2dStm.head.addr.getWidth)
//  //)
//  val myTempH2dAddrRange = (
//    //myH2dStm.head.addr.high
//    //log2Up(fbSize2d.x + 1)
//    log2Up(cfg.myAlignedFbCntMax)
//    downto log2Up(busCfg.burstCntMaxNumBytes)
//  )
//  myH2dStm.head.addr.allowOverride
//  myH2dStm.head.addr := 0x0
//  myH2dStm.head.addr(myTempH2dAddrRange) := (
//    RegNextWhen(
//      (myH2dStm.head.addr(myTempH2dAddrRange) + 1),
//      cond=myH2dStm.head.fire,
//      init=myH2dStm.head.addr(myTempH2dAddrRange).getZero,
//    )
//  )
//  myH2dStm.head.data := 0x0
//  myH2dStm.head.byteSize := (
//    //log2Up(busCfg.dataWidth / 8)
//    //log2Up(Rgb(rgbCfg).asBits.getWidth / 8)
//    log2Up(rgbUpWidth / 8)
//  )
//  myH2dStm.head.isWrite := False
//
//  //myH2dStm.head.burstFirst := True
//  //myH2dStm.head.burstCnt := busCfg.maxBurstSizeMinus1
//  //myH2dStm.head.burstLast := False
//  if (busCfg.allowBurst) {
//    myH2dStm.head.burstFirst := False//True
//    myH2dStm.head.burstCnt := 0x0//busCfg.maxBurstSizeMinus1
//    myH2dStm.head.burstLast := False
//  }
//
//  //--------
//  //val myD2hStm = Vec.fill(2)(
//  //  cloneOf(io.bus.d2hBus)
//  //)
//  //myD2hStm.head <-/< io.bus.d2hBus
//  val myD2hStm = cloneOf(io.bus.d2hBus)
//  myD2hStm <-/< io.bus.d2hBus
//  myD2hStm.ready := True
//  myDblLineBufEtc.io.push.valid := myD2hStm.fire
//  myDblLineBufEtc.io.push.payload.assignFromBits(
//    myD2hStm.data.resize(
//      myDblLineBufEtc.io.push.payload.asBits.getWidth
//    ).asBits
//  )
//  io.pop <-/< myDblLineBufEtc.io.pop
//  //--------
//  //val myRgbFifo = StreamFifo(
//  //  dataType=(
//  //    //LcvBusD2hPayload(cfg=busCfg)
//  //    Rgb(rgbCfg)
//  //  ),
//  //  depth=(
//  //    //busCfg.maxBurstSizeMinus1 + 1
//  //    cfg.myBusBurstSizeMax
//  //  ),
//  //  latency=(
//  //    //0
//  //    2
//  //  ),
//  //  forFMax=true,
//  //)
//
//  ////myD2hCalcPos.io.en := False
//
//  //myD2hStm.last.translateInto(myRgbFifo.io.push)(
//  //  dataAssignment=(outp, inp) => {
//  //    outp.assignFromBits(inp.data.asBits.resize(outp.asBits.getWidth))
//  //  }
//  //)
//
//  //io.pop <-/< myRgbFifo.io.pop //myPopStm
//  //myRgbFifo.io.pop.ready := True
//  //myRgbFifo.io
//  //--------
//  //--------
//  //val myD2hThrowCond = Bool()
//  //myD2hStm.last << myD2hStm.head.throwWhen(myD2hThrowCond)
//}

//case class LcvBusFramebufferCtrlDualCntWithBurstWithLineBuf(
//  cfg: LcvBusFramebufferConfig
//) extends Component {
//  //--------
//  def busCfg = cfg.busCfg
//  def rgbCfg = cfg.rgbCfg
//  def fbSize2d = cfg.fbSize2d
//  def cnt2dShift = cfg.cnt2dShift
//  def rgbUpWidth = 1 << log2Up(Rgb(c=rgbCfg).asBits.getWidth)
//  def rgbBusRatio = (busCfg.dataWidth / rgbUpWidth).toInt
//
//  require(
//    busCfg.dataWidth
//    //>= Rgb(c=rgbCfg).asBits.getWidth
//    >= rgbUpWidth
//  )
//  require(
//    //busCfg.dataWidth
//    /// Rgb(rgbCfg).asBits.getWidth
//    (fbSize2d.x % (cfg.myBusBurstSizeMax * rgbBusRatio)) == 0
//  )
//  //--------
//  val io = LcvBusFramebufferCtrlIo(cfg=cfg)
//  //--------
//  //val rD2hCnt = {
//  //  val temp = Reg(
//  //    UInt(log2Up(cfg.myAlignedFbCntMax + 1) bits)
//  //  )
//  //  temp.init(temp.getZero)
//  //  temp
//  //}
//  val myH2dCalcPosSize2d = (
//    ElabVec2[Int](
//      x=(
//        fbSize2d.x /// (1 << ()) //* (1 << cnt2dShift.x)
//      ),
//      y=(
//        fbSize2d.y //* (1 << cnt2dShift.y)
//        * (if (cfg.dblBuf) (2) else (1))
//      ),
//    )
//  )
//  val myD2hCalcPosSize2d = (
//    ElabVec2[Int](
//      x=(fbSize2d.x * (1 << cnt2dShift.x)),
//      y=(
//        fbSize2d.y * (1 << cnt2dShift.y)
//        * (if (cfg.dblBuf) (2) else (1))
//      ),
//    )
//  )
//  val myH2dCalcPos = LcvVideoCalcPos(someSize2d=myH2dCalcPosSize2d)
//  val myD2hCalcPos = LcvVideoCalcPos(someSize2d=myD2hCalcPosSize2d)
//  val myH2dMainPosRange = ElabDualTypeVec2(
//    x=(myH2dCalcPos.io.info.pos.x.high downto cnt2dShift.x),
//    y=(myH2dCalcPos.io.info.pos.y.high downto cnt2dShift.y),
//  )
//  val myD2hMainPosRange = ElabDualTypeVec2(
//    x=(myD2hCalcPos.io.info.pos.x.high downto cnt2dShift.x),
//    y=(myD2hCalcPos.io.info.pos.y.high downto cnt2dShift.y),
//  )
//  val myH2dMainPos = ElabDualTypeVec2(
//    x=myH2dCalcPos.io.info.pos.x(myH2dMainPosRange.x),
//    y=myH2dCalcPos.io.info.pos.y(myH2dMainPosRange.y),
//  )
//  val myD2hMainPos = ElabDualTypeVec2(
//    x=myD2hCalcPos.io.info.pos.x(myD2hMainPosRange.x),
//    y=myD2hCalcPos.io.info.pos.y(myD2hMainPosRange.y),
//  )
//  //--------
//  val myH2dStm = Vec.fill(3)(
//    cloneOf(io.bus.h2dBus)
//  )
//  io.bus.h2dBus <-/< myH2dStm.last
//  // The multiplication to calculate the address is why we use `<-<`.
//  // Hopefully it will be seen as a purely registered multiply
//  // by synth + pnr tools!
//  // As of this writing, I haven't tried yet.
//  myH2dStm(1) <-< myH2dStm.head
//  myH2dStm(1).translateInto(myH2dStm.last)(
//    dataAssignment=(outp, inp) => {
//      outp := inp
//      outp.addr.allowOverride
//      outp.addr(cfg.fbMmapCfg.addrSliceRange) := (
//        cfg.fbMmapCfg.addrSliceValUInt
//      )
//      //outp.addr(
//      //)
//    }
//  )
//  myH2dStm.head.valid := False
//  myH2dStm.head.addr := (
//    Cat(
//      //(rCnt2d.head.y(myCnt2dRange.y) * fbSize2d.x)
//      //+ rCnt2d.head.x(myCnt2dRange.x)
//      //(rH2dMainCnt.y * fbSize2d.x) + rH2dMainCnt.x
//      (
//        //myH2dCalcPos.io.info.pos.y(myMainPosRange.y) * fbSize2d.x
//        //+ myH2dCalcPos.io.info.pos.x(myMainPosRange.x)
//        (myH2dMainPos.y * fbSize2d.x) + myH2dMainPos.x
//      ),
//      U(s"${log2Up(Rgb(rgbCfg).asBits.getWidth / 8)}'d0"),
//    ).asUInt.resize(myH2dStm.head.addr.getWidth)
//  )
//  myH2dStm.head.data := 0x0
//  myH2dStm.head.byteSize := (
//    //log2Up(busCfg.dataWidth / 8)
//    log2Up(Rgb(rgbCfg).asBits.getWidth / 8)
//  )
//  myH2dStm.head.isWrite := False
//
//  if (busCfg.allowBurst) {
//    myH2dStm.head.burstFirst := False//True
//    myH2dStm.head.burstCnt := 0x0//busCfg.maxBurstSizeMinus1
//    myH2dStm.head.burstLast := False
//  }
//
//  //--------
//  val myD2hStm = Vec.fill(2)(
//    cloneOf(io.bus.d2hBus)
//  )
//  myD2hStm.head <-/< io.bus.d2hBus
//  //--------
//  val myRgbFifo = StreamFifo(
//    dataType=(
//      //LcvBusD2hPayload(cfg=busCfg)
//      Rgb(rgbCfg)
//    ),
//    depth=(
//      //busCfg.maxBurstSizeMinus1 + 1
//      cfg.myBusBurstSizeMax
//    ),
//    latency=(
//      //0
//      2
//    ),
//    forFMax=true,
//  )
//
//  myD2hCalcPos.io.en := False
//
//  myD2hStm.last.translateInto(myRgbFifo.io.push)(
//    dataAssignment=(outp, inp) => {
//      outp.assignFromBits(inp.data.asBits.resize(outp.asBits.getWidth))
//    }
//  )
//
//  io.pop <-/< myRgbFifo.io.pop //myPopStm
//  //--------
//  //--------
//  //val myD2hThrowCond = Bool()
//  //myD2hStm.last << myD2hStm.head.throwWhen(myD2hThrowCond)
//}

//case class LcvBusFramebufferCtrlDualCntNonBurstWithLineBuf(
//  cfg: LcvBusFramebufferConfig
//) extends Component {
//  //--------
//  def rgbCfg = cfg.rgbCfg
//  def fbSize2d = cfg.fbSize2d
//  def cnt2dShift = cfg.cnt2dShift
//  require(
//    cfg.busCfg.dataWidth
//    >= Rgb(c=rgbCfg).asBits.getWidth
//  )
//  //--------
//  val io = LcvBusFramebufferCtrlIo(cfg=cfg)
//  //--------
//  //val rD2hCnt = {
//  //  val temp = Reg(
//  //    UInt(log2Up(cfg.myAlignedFbCntMax + 1) bits)
//  //  )
//  //  temp.init(temp.getZero)
//  //  temp
//  //}
//  val myCalcPosSize2d = (
//    ElabVec2[Int](
//      x=(fbSize2d.x * (1 << cnt2dShift.x)),
//      y=(
//        fbSize2d.y * (1 << cnt2dShift.y)
//        * (if (cfg.dblBuf) (2) else (1))
//      ),
//    )
//  )
//  val myH2dCalcPos = LcvVideoCalcPos(someSize2d=myCalcPosSize2d)
//  val myD2hCalcPos = LcvVideoCalcPos(someSize2d=myCalcPosSize2d)
//  val myMainPosRange = ElabDualTypeVec2(
//    x=(myH2dCalcPos.io.info.pos.x.high downto cnt2dShift.x),
//    y=(myH2dCalcPos.io.info.pos.y.high downto cnt2dShift.y),
//  )
//  val myH2dMainPos = ElabDualTypeVec2(
//    x=myH2dCalcPos.io.info.pos.x(myMainPosRange.x),
//    y=myH2dCalcPos.io.info.pos.y(myMainPosRange.y),
//  )
//  val myD2hMainPos = ElabDualTypeVec2(
//    x=myD2hCalcPos.io.info.pos.x(myMainPosRange.x),
//    y=myD2hCalcPos.io.info.pos.y(myMainPosRange.y),
//  )
//  //--------
//  val myH2dStm = Vec.fill(3)(
//    cloneOf(io.bus.h2dBus)
//  )
//  io.bus.h2dBus <-/< myH2dStm.last
//  // The multiplication to calculate the address is why we use `<-<`.
//  // Hopefully it will be seen as a purely registered multiply
//  // by synth + pnr tools!
//  // As of this writing, I haven't tried yet.
//  myH2dStm(1) <-< myH2dStm.head
//  myH2dStm(1).translateInto(myH2dStm.last)(
//    dataAssignment=(outp, inp) => {
//      outp := inp
//      outp.addr.allowOverride
//      outp.addr(cfg.fbMmapCfg.addrSliceRange) := (
//        cfg.fbMmapCfg.addrSliceValUInt
//      )
//      //outp.addr(
//      //)
//    }
//  )
//  myH2dStm.head.valid := False
//  myH2dStm.head.addr := (
//    Cat(
//      //(rCnt2d.head.y(myCnt2dRange.y) * fbSize2d.x)
//      //+ rCnt2d.head.x(myCnt2dRange.x)
//      //(rH2dMainCnt.y * fbSize2d.x) + rH2dMainCnt.x
//      (
//        //myH2dCalcPos.io.info.pos.y(myMainPosRange.y) * fbSize2d.x
//        //+ myH2dCalcPos.io.info.pos.x(myMainPosRange.x)
//        (myH2dMainPos.y * fbSize2d.x) + myH2dMainPos.x
//      ),
//      U(s"${log2Up(Rgb(rgbCfg).asBits.getWidth / 8)}'d0"),
//    ).asUInt.resize(myH2dStm.head.addr.getWidth)
//  )
//  myH2dStm.head.data := 0x0
//  myH2dStm.head.byteSize := (
//    //log2Up(cfg.busCfg.dataWidth / 8)
//    log2Up(Rgb(rgbCfg).asBits.getWidth / 8)
//  )
//  myH2dStm.head.isWrite := False
//
//  if (cfg.busCfg.allowBurst) {
//    myH2dStm.head.burstFirst := False//True
//    myH2dStm.head.burstCnt := 0x0//cfg.busCfg.maxBurstSizeMinus1
//    myH2dStm.head.burstLast := False
//  }
//
//  //--------
//  val myD2hStm = Vec.fill(2)(
//    cloneOf(io.bus.d2hBus)
//  )
//  myD2hStm.head <-/< io.bus.d2hBus
//  //--------
//  val myRgbFifo = StreamFifo(
//    dataType=(
//      //LcvBusD2hPayload(cfg=cfg.busCfg)
//      Rgb(cfg.rgbCfg)
//    ),
//    depth=(
//      //cfg.busCfg.maxBurstSizeMinus1 + 1
//      cfg.myBusBurstSizeMax
//    ),
//    latency=(
//      //0
//      2
//    ),
//    forFMax=true,
//  )
//
//  myD2hCalcPos.io.en := False
//
//  myD2hStm.last.translateInto(myRgbFifo.io.push)(
//    dataAssignment=(outp, inp) => {
//      outp.assignFromBits(inp.data.asBits.resize(outp.asBits.getWidth))
//    }
//  )
//
//  io.pop <-/< myRgbFifo.io.pop //myPopStm
//  //--------
//  //--------
//  //val myD2hThrowCond = Bool()
//  //myD2hStm.last << myD2hStm.head.throwWhen(myD2hThrowCond)
//}

//case class LcvBusFramebufferCtrlDualCntNonBurstBasic(
//  cfg: LcvBusFramebufferConfig
//) extends Component {
//  //--------
//  def rgbCfg = cfg.rgbCfg
//  def fbSize2d = cfg.fbSize2d
//  def cnt2dShift = cfg.cnt2dShift
//  require(
//    cfg.busCfg.dataWidth
//    >= Rgb(c=rgbCfg).asBits.getWidth
//  )
//  //--------
//  val io = LcvBusFramebufferCtrlIo(cfg=cfg)
//  //--------
//  //val rD2hCnt = {
//  //  val temp = Reg(
//  //    UInt(log2Up(cfg.myAlignedFbCntMax + 1) bits)
//  //  )
//  //  temp.init(temp.getZero)
//  //  temp
//  //}
//  val rH2dRawCnt2d = {
//    //Vec.fill(2)({
//      val temp = Reg(
//        DualTypeNumVec2(
//          dataTypeX=UInt(log2Up(fbSize2d.x + 1) + cnt2dShift.x bits),
//          dataTypeY=(
//            UInt(
//              (if (cfg.dblBuf) (1) else (0))
//              + (
//                log2Up(fbSize2d.y + 1) + cnt2dShift.y 
//              )
//              bits
//            )
//          ),
//        )
//      )
//      temp.init(temp.getZero)
//      temp
//    //})
//  }
//  val myCnt2dRange = (
//    ElabVec2(
//      x=(rH2dRawCnt2d.x.high downto cnt2dShift.x),
//      y=(rH2dRawCnt2d.y.high downto cnt2dShift.y),
//    )
//  )
//  val rH2dMainCnt = ElabDualTypeVec2(
//    x=rH2dRawCnt2d.x(myCnt2dRange.x),
//    y=rH2dRawCnt2d.y(myCnt2dRange.y),
//  )
//  //val rD2hMainCnt = ElabDualTypeVec2(
//  //  x=rH2dRawCnt2d.last.x(myCnt2dRange.x),
//  //  y=rH2dRawCnt2d.last.y(myCnt2dRange.y),
//  //)
//  //--------
//  val myH2dStm = Vec.fill(3)(
//    cloneOf(io.bus.h2dBus)
//  )
//  io.bus.h2dBus <-/< myH2dStm.last
//  // The multiplication to calculate the address is why we use `<-<`.
//  // Hopefully it will be seen as a purely registered multiply
//  // by synth + pnr tools!
//  // As of this writing, I haven't tried yet.
//  myH2dStm(1) <-< myH2dStm.head
//  myH2dStm(1).translateInto(myH2dStm.last)(
//    dataAssignment=(outp, inp) => {
//      outp := inp
//      outp.addr.allowOverride
//      outp.addr(cfg.fbMmapCfg.addrSliceRange) := (
//        cfg.fbMmapCfg.addrSliceValUInt
//      )
//      //outp.addr(
//      //)
//    }
//  )
//  myH2dStm.head.valid := True
//  myH2dStm.head.addr := (
//    Cat(
//      //(rCnt2d.head.y(myCnt2dRange.y) * fbSize2d.x)
//      //+ rCnt2d.head.x(myCnt2dRange.x)
//      (rH2dMainCnt.y * fbSize2d.x) + rH2dMainCnt.x
//    ).asUInt.resize(myH2dStm.head.addr.getWidth)
//    //Cat(
//    //  cfg.fbMmapCfg.addrSliceValUInt,
//    //  rCnt.x,
//    //  U(s"${cfg.busCfg.dataWidth / 8}'d0"),
//    //).asUInt.resize(myH2dStm.head.addr.getWidth)
//  )
//  myH2dStm.head.data := 0x0
//  myH2dStm.head.byteSize := (
//    //log2Up(cfg.busCfg.dataWidth / 8)
//    log2Up(Rgb(rgbCfg).asBits.getWidth / 8)
//  )
//  myH2dStm.head.isWrite := False
//
//  if (cfg.busCfg.allowBurst) {
//    myH2dStm.head.burstFirst := False//True
//    myH2dStm.head.burstCnt := 0x0//cfg.busCfg.maxBurstSizeMinus1
//    myH2dStm.head.burstLast := False
//  }
//
//  //when (myH2dStm.head.fire) {
//  //}
//  switch (
//    myH2dStm.head.fire
//    //## (rCnt2d.head.x(myCnt2dRange.x) >= fbSize2d.x)
//    //## (rH2dMainCnt.x >= fbSize2d.x - cfg.myBusBurstSizeMax)
//    //## (rH2dMainCnt.x >= fbSize2d.x - 1)
//
//    ## (rH2dRawCnt2d.x >= (fbSize2d.x * (1 << cnt2dShift.x) - 1))
//    //## (rH2dMainCnt.y >= fbSize2d.y - 1)
//    ## (rH2dRawCnt2d.y >= (fbSize2d.y * (1 << cnt2dShift.y) - 1))
//  ) {
//    is (M"10-") {
//      rH2dRawCnt2d.x := rH2dRawCnt2d.x + 1
//    }
//    is (M"110") {
//      rH2dRawCnt2d.x := 0x0
//      rH2dRawCnt2d.y := rH2dRawCnt2d.y + 1
//      //when (
//      //  //rH2dMainCnt.y >= fbSize2d.y - 1
//      //  (rH2dRawCnt2d.y >= (fbSize2d.y * (1 << cnt2dShift.y) - 1))
//      //) {
//      //  rH2dRawCnt2d.y := 0x0
//      //} otherwise {
//      //  rH2dRawCnt2d
//      //}
//    }
//    is (M"111") {
//      rH2dRawCnt2d.x := 0x0
//      rH2dRawCnt2d.y := 0x0
//    }
//    default {
//    }
//  }
//  //--------
//  val myD2hStm = Vec.fill(2)(
//    cloneOf(io.bus.d2hBus)
//  )
//  myD2hStm.head <-/< io.bus.d2hBus
//  //--------
//  val myRgbFifo = StreamFifo(
//    dataType=(
//      //LcvBusD2hPayload(cfg=cfg.busCfg)
//      Rgb(cfg.rgbCfg)
//    ),
//    depth=(
//      //cfg.busCfg.maxBurstSizeMinus1 + 1
//      cfg.myBusBurstSizeMax
//    ),
//    latency=(
//      //0
//      2
//    ),
//    forFMax=true,
//  )
//  myD2hStm.last.translateInto(myRgbFifo.io.push)(
//    dataAssignment=(outp, inp) => {
//      outp.assignFromBits(inp.data.asBits.resize(outp.asBits.getWidth))
//    }
//  )
//
//  //val myPopStm = cloneOf(io.pop)
//  //val myPopThrowCond = Bool()
//  //myPopThrowCond := False
//  //val myPopStm = myRgbFifo.io.pop.throwWhen(
//  //  //rCnt2d.x 
//  //  myPopThrowCond
//  //)
//  io.pop <-/< myRgbFifo.io.pop //myPopStm
//  //--------
//  //--------
//  //val myD2hThrowCond = (
//  //  cfg.myFbSize2dMult != cfg.myAlignedFbCntMax
//  //) generate (
//  //  Bool()
//  //)
//  myD2hStm.last << myD2hStm.head
//  //if (cfg.myFbSize2dMult == cfg.myAlignedFbCntMax) {
//  //  // no alignment needed for burst reads
//  //  myD2hStm.last << myD2hStm.head
//  //  when (myD2hStm.last.fire) {
//  //    rD2hCnt := rD2hCnt + 1
//  //  }
//  //} else {
//  //  // alignment needed for burst reads
//  //  myD2hStm.last << myD2hStm.head.throwWhen(myD2hThrowCond)
//  //  switch (
//  //    myD2hStm.head.fire
//  //    ## (rD2hCnt >= cfg.myFbSize2dMult - 1)
//  //    ## (rD2hCnt >= cfg.myAlignedFbCntMax - 1)
//  //  ) {
//  //    is (M"100") {
//  //      rD2hCnt := rD2hCnt + 1
//  //      myD2hThrowCond := False
//  //    }
//  //    is (M"110") {
//  //      rD2hCnt := rD2hCnt + 1
//  //      myD2hThrowCond := True
//  //    }
//  //    is (M"111") {
//  //      rD2hCnt := 0x0
//  //      myD2hThrowCond := True
//  //    }
//  //    default {
//  //      myD2hThrowCond := False
//  //    }
//  //  }
//  //  //when (myD2hStm.fire) {
//  //  //  when  {
//  //  //  } elsewhen  {
//  //  //  }
//  //  //}
//  //}
//}
//
//case class LcvBusFramebufferCtrlSingleCntBasic(
//  cfg: LcvBusFramebufferConfig
//) extends Component {
//  require(
//    cfg.busCfg.allowBurst
//  )
//  //--------
//  val io = LcvBusFramebufferCtrlIo(cfg=cfg)
//  //--------
//  val rCnt = {
//    val temp = Vec.fill(2)({
//      val innerTemp = Reg(
//        UInt(log2Up(cfg.myAlignedFbCntMax + 1) bits)
//      )
//      innerTemp.init(innerTemp.getZero)
//    })
//    temp
//  }
//  //--------
//  val myH2dStm = cloneOf(io.bus.h2dBus)
//  io.bus.h2dBus <-/< myH2dStm
//  myH2dStm.valid := True
//  myH2dStm.addr := (
//    Cat(
//      //cfg.fbMmapCfg.addrSliceValUInt, // TODO: figure this out
//      rCnt.head,
//      U(s"${log2Up(cfg.busCfg.dataWidth / 8)}'d0"),
//    ).asUInt.resize(myH2dStm.addr.getWidth)
//  )
//  myH2dStm.data := 0x0
//  myH2dStm.byteSize := log2Up(cfg.busCfg.dataWidth / 8)
//  myH2dStm.isWrite := False
//
//  myH2dStm.burstFirst := True
//  myH2dStm.burstCnt := cfg.busCfg.maxBurstSizeMinus1
//  myH2dStm.burstLast := False
//  //when (myH2dStm.fire) {
//  //  
//  //}
//  switch (
//    myH2dStm.fire
//    ## (rCnt.head >= cfg.myFbSize2dMult - cfg.myBusBurstSizeMax)
//  ) {
//    is (M"10") {
//      rCnt.head(rCnt.head.high downto cfg.busCfg.burstCntWidth) := (
//        rCnt.head(rCnt.head.high downto cfg.busCfg.burstCntWidth) + 1
//      )
//    }
//    is (M"11") {
//      rCnt.head(rCnt.head.high downto cfg.busCfg.burstCntWidth) := 0x0
//    }
//    default {
//    }
//  }
//  //--------
//  //val myD2hFifo = StreamFifo(
//  //  dataType=LcvBusD2hPayload(cfg=cfg.busCfg),
//  //  depth=(cfg.busCfg.maxBurstSizeMinus1 + 1),
//  //  latency=2,
//  //  forFMax=true,
//  //)
//  //myD2hFifo.io.push <-/< io.bus.d2hBus
//  val myD2hStm = Vec.fill(2)(
//    cloneOf(io.bus.d2hBus)
//  )
//  myD2hStm.head <-/< io.bus.d2hBus
//  //--------
//  val myRgbFifo = StreamFifo(
//    dataType=(
//      //LcvBusD2hPayload(cfg=cfg.busCfg)
//      Rgb(cfg.rgbCfg)
//    ),
//    depth=(
//      //cfg.busCfg.maxBurstSizeMinus1 + 1
//      cfg.myBusBurstSizeMax
//    ),
//    latency=(
//      //0
//      2
//    ),
//    forFMax=true,
//  )
//  myD2hStm.last.translateInto(myRgbFifo.io.push)(
//    dataAssignment=(outp, inp) => {
//      outp.assignFromBits(inp.data.asBits.resize(outp.asBits.getWidth))
//    }
//  )
//
//  //val myPopStm = cloneOf(io.pop)
//  //val myPopThrowCond = Bool()
//  //myPopThrowCond := False
//  //val myPopStm = myRgbFifo.io.pop.throwWhen(
//  //  //rCnt2d.x 
//  //  myPopThrowCond
//  //)
//  io.pop <-/< myRgbFifo.io.pop //myPopStm
//  //--------
//  //--------
//  val myD2hThrowCond = (
//    cfg.myFbSize2dMult != cfg.myAlignedFbCntMax
//  ) generate (
//    Bool()
//  )
//  if (cfg.myFbSize2dMult == cfg.myAlignedFbCntMax) {
//    // no alignment needed for burst reads
//    myD2hStm.last << myD2hStm.head
//    when (myD2hStm.last.fire) {
//      rCnt.last := rCnt.last + 1
//    }
//  } else {
//    // alignment needed for burst reads
//    myD2hStm.last << myD2hStm.head.throwWhen(myD2hThrowCond)
//    switch (
//      myD2hStm.head.fire
//      ## (rCnt.last >= cfg.myFbSize2dMult - 1)
//      ## (rCnt.last >= cfg.myAlignedFbCntMax - 1)
//    ) {
//      is (M"100") {
//        rCnt.last := rCnt.last + 1
//        myD2hThrowCond := False
//      }
//      is (M"110") {
//        rCnt.last := rCnt.last + 1
//        myD2hThrowCond := True
//      }
//      is (M"111") {
//        rCnt.last := 0x0
//        myD2hThrowCond := True
//      }
//      default {
//        myD2hThrowCond := False
//      }
//    }
//    //when (myD2hStm.fire) {
//    //  when  {
//    //  } elsewhen  {
//    //  }
//    //}
//  }
//  //myRgbFifo
//  //myD2hFifo
//
//  //io.pop.valid := False
//  //io.pop.payload := io.pop.payload.getZero
//  //--------
//}
