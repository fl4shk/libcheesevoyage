package libcheesevoyage.gfx

import scala.collection.mutable._

import libcheesevoyage.general.WrPulseRdPipeRamSdpPipeConfig
import libcheesevoyage.general.WrPulseRdPipeRamSdpPipe
import libcheesevoyage.general.DualTypeNumVec2
import libcheesevoyage.general.Vec2
import libcheesevoyage.general.MkVec2
import libcheesevoyage.general.ElabVec2

import spinal.core._
import spinal.lib._

import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig

object LcvVideoPosInfo {
  def coordElemT(
    someWidthOrHeight: Int,
    plus: Int=0,
  ): UInt = LcvVgaCtrlMiscIo.coordElemT(
    fbWidthOrHeight=someWidthOrHeight,
    plus=plus,
  )
  def coordT(
    //vgaTimingInfo: LcvVgaTimingInfo,
    someSize2d: ElabVec2[Int],
    plus: ElabVec2[Int]=ElabVec2[Int](
      x=0,
      y=0,
    )
  ): DualTypeNumVec2[UInt, UInt] = LcvVgaCtrlMiscIo.coordT(
    //vgaTimingInfo=vgaTimingInfo
    fbSize2d=someSize2d,
    plus=plus
  )
}

case class LcvVideoPosSlice(
  someSize2d: ElabVec2[Int]
  //someWidthOrHeight: Int,
) extends Bundle {
  //--------
  //val hposWillOverflow = Bool()
  //val crossingGrid = Vec2(Bool())
  //val crossingGridX = Bool()
  //val pastCrossingGrid = Vec2(Bool())
  val nextPos = coordT()
  //val posPlus1 = coordT()
  val pos = coordT()
  val pastPos = coordT()
  val nonSliceChangingRow = Bool()
  //--------
  //def coordElemT() = LcvVideoPosInfo.coordElemT(
  //  //someWidthOrHeight=someSize2d.x
  //  someWidthOrHeight=someWidthOrHeight,
  //)
  def coordT() = LcvVideoPosInfo.coordT(
    someSize2d=someSize2d,
    plus=ElabVec2[Int](x=1, y=1)
  )
  // crossing the grid 
  def crossingGridX = Mux[Bool](
    !nonSliceChangingRow,
    (pos.x + 1 === nextPos.x),
    (pos.x =/= 0 && nextPos.x === 0)
  )
  //--------
}
case class LcvVideoPosInfo(
  //rgbConfig: RgbConfig
  //vgaTimingInfo: LcvVgaTimingInfo,
  someSize2d: ElabVec2[Int]
) extends Bundle {
  //--------
  //val outpCol = out(Rgb(rgbConfig))

  //val col = Rgb(rgbConfig)
  //val frameCnt = UInt(ditherDeltaWidth bits)
  //val changedFrameCnt = Bool()
  //val posPlus1 = coordT()
  //val posPlus2 = coordT()
  val posWillOverflow = Vec2(Bool())
  val nextPos = coordT()
  val pos = coordT()
  val pastPos = coordT()
  val changingRow = Bool()
  //--------
  def coordT() = LcvVideoPosInfo.coordT(
    someSize2d=someSize2d,
    plus=ElabVec2[Int](
      x=1,
      y=1,
    )
  )
  //--------
  //def posSlice(
  //  //thisSize2dPow: ElabVec2[Int], // left shift amounts for `this`
  //  //thatSize2dPow: ElabVec2[Int], // left shift amounts for `that`
  //  someSize2d: ElabVec2[Int],
  //  //someScalePow: ElabVec2[Int],
  //  someScale: ElabVec2[Int],
  //  //thatSize2dScalePow: ElabVec2[Int],
  //): LcvVideoPosSlice = {
  //  //assert(thisWidthPow > thatWidthPow)

  //  //assert(thisSize2dPow.x >= thatSize2dPow.x)
  //  //assert(thisSize2dPow.y >= thatSize2dPow.y)
  //  //assert(thisSize2dScalePow.x >= thatSize2dScalePow.x)
  //  //assert(thisSize2dScalePow.y >= thatSize2dScalePow.y)

  //  //def sliceLo = thisWidthPow - thatWidthPow
  //  //def sliceLo = ElabVec2[Int](
  //  //  //x=1 << (thisSize2dPow.x - thatSize2dPow.x),
  //  //  //y=1 << (thisSize2dPow.y - thatSize2dPow.y),
  //  //  //x=1 << (thisSize2dScalePow.x - thatSize2dScalePow.x),
  //  //  //y=1 << (thisSize2dScalePow.y - thatSize2dScalePow.y),
  //  //  x=1 << someScalePow.x,
  //  //  y=1 << someScalePow.y,
  //  //)
  //  //def sliceLo = someScalePow
  //  def sliceLo = ElabVec2[Int](
  //    x=log2Up(someScale.x),
  //    y=log2Up(someScale.y),
  //  )

  //  val that = LcvVideoPosSlice(
  //    //someWidthOrHeight=1 << thatWidthPow
  //    //someSize2d=ElabVec2[Int](
  //    //  //x=1 << thatSize2dPow.x,
  //    //  //y=1 << thatSize2dPow.y,
  //    //  //x=1 << thatSize2dScalePow.x,
  //    //  //y=1 << thatSize2dScalePow.y,
  //    //)
  //    someSize2d=someSize2d,
  //  )
  //  //that.nextHpos := 
  //  //that.posWillOverflow := 
  //  //that.posWillOverflow := MkVec2(Bool(), x=True, y=True)
  //  //that.posWillOverflow.x := True
  //  //that.posWillOverflow.y := True

  //  //when (!changingRow) {
  //  //  //that.crossingGrid.x := that.pos.x + 1 === that.nextPos.x
  //  //  //that.crossingGrid.y := that.pos.y + 1 === that.nextPos.y
  //  //  //that.pastCrossingGrid.x := that.pastPos.x + 1 === that.pos.x
  //  //  //that.pastCrossingGrid.y := that.pastPos.y + 1 === that.pos.y

  //  //  // crossing the grid 
  //  //  that.crossingGridX := that.pos.x + 1 === that.nextPos.x
  //  //  //that.crossingGrid.y := 0
  //  //} otherwise {
  //  //  that.crossingGridX := (that.pos.x =/= 0 && that.nextPos.x === 0)
  //  //  //that.crossingGrid.y := 
  //  //}
  //  that.nextPos.x := this.nextPos.x(this.nextPos.x.high downto sliceLo.x)
  //  that.nextPos.y := this.nextPos.y(this.nextPos.y.high downto sliceLo.y)
  //  that.pos.x := this.pos.x(this.pos.x.high downto sliceLo.x)
  //  that.pos.y := this.pos.y(this.pos.y.high downto sliceLo.y)
  //  that.pastPos.x := this.pastPos.x(this.pastPos.x.high downto sliceLo.x)
  //  that.pastPos.y := this.pastPos.y(this.pastPos.y.high downto sliceLo.y)
  //  that.nonSliceChangingRow := this.changingRow
  //  that
  //}
}
case class LcvVideoCalcPosIo(
  someSize2d: ElabVec2[Int],
) extends Bundle with IMasterSlave {
  //--------
  val en = in Bool()
  val info = out(LcvVideoPosInfo(someSize2d=someSize2d))
  //--------
  def asMaster(): Unit = {
    out(en)
    in(info)
  }
  //--------
}
case class LcvVideoCalcPos(
  someSize2d: ElabVec2[Int],
) extends Component {
  //--------
  val io = LcvVideoCalcPosIo(someSize2d=someSize2d)
  val info = io.info
  //--------
  val rPosPlus1 = Reg(cloneOf(info.pos))
  rPosPlus1.x.init(1)
  rPosPlus1.y.init(1)

  val rPos = Reg(cloneOf(info.pos))
  rPos.init(rPos.getZero)
  info.pos := rPos

  val rPastPos = Reg(cloneOf(info.pastPos))
  rPastPos.init(rPastPos.getZero)
  info.pastPos := rPastPos

  val rPosWillOverflow = Reg(cloneOf(info.posWillOverflow))
  //val rPosWillOverflow = Reg(Vec2(Bool()))
  rPosWillOverflow.init(rPosWillOverflow.getZero)
  info.posWillOverflow := rPosWillOverflow
  val rPosWillOverflowDual = Reg(Bool())

  val rChangingRow = Reg(cloneOf(info.changingRow))
  rChangingRow.init(rChangingRow.getZero)
  info.changingRow := rChangingRow

  val rPastInfo = RegNext(io.info) init(io.info.getZero)

  when (io.en) {
    val dithConcat = rPosWillOverflow.asBits
    //dithConcat(0) := rPosWillOverflow.x
    //dithConcat(1) := rPosWillOverflow.y
    switch (dithConcat) {
      // overflowX=0, overflowY=don't care
      //is (B"-0")
      //is (M"1-0")
      is (M"-0") {
        info.nextPos.x := info.pos.x + 1
        info.nextPos.y := info.pos.y
        rPosWillOverflow.x := info.pos.x === someSize2d.x - 2
        //rPosWillOverflow.y := info.pos.y === someSize2d.y - 1
        rChangingRow := False
      }
      // overflowX=1, overflowY=0
      //is (B"101")
      is (B"01") {
        info.nextPos.x := 0
        rPosWillOverflow.x := False
        rChangingRow := True
        info.nextPos.y := info.pos.y + 1
        rPosWillOverflow.y := info.pos.y === someSize2d.y - 2
      }
      // overflowX=1, overflowY=1
      //is (B"111")
      is (B"11") {
        info.nextPos.x := 0
        rPosWillOverflow.x := False
        rChangingRow := True
        info.nextPos.y := 0x0
        rPosWillOverflow.y := False
      }
      //is (M"0--") {
      //  info.nextPos := info.pos
      //}
      default {
        info.nextPos := info.pos
      }
    }
    // BEGIN: working code with lower FMax
    //when (!rPosWillOverflow.x) 
    //when (info.pos.x + 1 < someSize2d.x) {
    //  info.nextPos.x := info.pos.x + 1
    //  info.nextPos.y := info.pos.y
    //  //rPosWillOverflow.x := info.pos.x === someSize2d.x - 2
    //  ////rPosWillOverflow.y := info.pos.y === someSize2d.y - 1
    //  rChangingRow := False
    //} otherwise {
    //  info.nextPos.x := 0
    //  //rPosWillOverflow.x := False
    //  rChangingRow := True
    //  //when (info.pos.y =/= someSize2d.y - 1)
    //  when (
    //    //!rPosWillOverflow.y
    //    info.pos.y + 1 < someSize2d.y
    //  ) {
    //    info.nextPos.y := info.pos.y + 1
    //    //rPosWillOverflow.y := info.pos.y === someSize2d.y - 2
    //  } otherwise {
    //    info.nextPos.y := 0x0
    //    //rPosWillOverflow.y := False
    //  }
    //}
    rPos := info.nextPos
    rPastPos := rPos
  } otherwise {
    io.info := rPastInfo
  }
}

case class LcvVideoCalcPosStreamAdapterIo(
  someSize2d: ElabVec2[Int]
) extends Bundle {
  val infoPop = master(
    Stream(LcvVideoPosInfo(someSize2d=someSize2d))
  )
}
case class LcvVideoCalcPosStreamAdapter(
  someSize2d: ElabVec2[Int]
) extends Component {
  //--------
  val io = LcvVideoCalcPosStreamAdapterIo(someSize2d=someSize2d)
  //--------
  val calcPos = LcvVideoCalcPos(someSize2d=someSize2d)
  //--------
  io.infoPop.valid := True
  calcPos.io.en := io.infoPop.ready
  io.infoPop.payload := (
    calcPos.io.info
    //RegNext(
    //  io.infoPop.payload,
    //  init=io.infoPop.payload.getZero
    //)
  )
}

case class LcvVideoDblLineBufWithCalcPosConfig(
  rgbCfg: RgbConfig,
  someSize2d: ElabVec2[Int],
  cnt2dShift: ElabVec2[Int],
  //bufElemSize: Int=1,
) {
  val myCalcPosSize2d = ElabVec2[Int](
    x=(someSize2d.x * (1 << cnt2dShift.x)),
    y=(someSize2d.y * (1 << cnt2dShift.y)),
  )
  val myMemWordCnt = (
    //someSize2d.x //* (1 << cnt2dShift.x)
    //* 2

    // This *may* waste space but maybe not? It does round up to the
    // nearest power of two, but I have a few comments about that:
    // (1) It allows us to avoid using a multiplier for the address
    //    calculation
    // (2) FPGA Block RAM primitives are large enough
    //    that maybe it's not a problem anyway?
    // (3) I did some math, and even with a 1920x1080 resolution
    //    (i.e. 1080p widescreen),
    //    the calculation for a double-buffered line buffer only uses
    //    4096 addresses. This becomes 16 kiB with 32 bpp colors
    //    though. That's a big chunk of block RAM I guess? On the other
    //    hand, you probably only need one of these double-buffered
    //    line buffers.
    (1 << log2Up(someSize2d.x))
    * 2
  )
  val myMemCfg = WrPulseRdPipeRamSdpPipeConfig(
    modType=Rgb(rgbCfg),
    wordType=Rgb(rgbCfg),
    wordCount=myMemWordCnt,
    pipeName="LcvVideoDblLineBufWithCalcPos",
    initBigInt={
      val tempArr = new ArrayBuffer[BigInt]()
      for (idx <- 0 until myMemWordCnt) {
        tempArr += BigInt(0)
      }
      Some(Array.fill(1)(tempArr))
    },
    setWordFunc=(
      outp: Rgb,
      inp: Rgb,
      rdMemWord: Rgb,
    ) => {
      outp := rdMemWord
    }
  )
}

case class LcvVideoDblLineBufWithCalcPosIo(
  cfg: LcvVideoDblLineBufWithCalcPosConfig
) extends Bundle {
  //val wrEn = in(Bool())
  //val wrData = in(Rgb(cfg.rgbCfg))
  def rgbCfg = cfg.rgbCfg

  val push = slave(
    //Flow(Rgb(rgbCfg))
    Stream(Rgb(rgbCfg))
  )
  val pop = master(Stream(Rgb(rgbCfg)))

  val infoPop = master(
    Stream(LcvVideoPosInfo(someSize2d=cfg.myCalcPosSize2d))
  )
  //val info = out(LcvVideoPosInfo(someSize2d=cfg.myCalcPosSize2d))
}

case class LcvVideoDblLineBufWithCalcPos(
  cfg: LcvVideoDblLineBufWithCalcPosConfig
) extends Component {
  def rgbCfg = cfg.rgbCfg
  def someSize2d = cfg.someSize2d
  def cnt2dShift = cfg.cnt2dShift
  def myCalcPosSize2d = cfg.myCalcPosSize2d
  //--------
  val io = LcvVideoDblLineBufWithCalcPosIo(cfg=cfg)
  //--------
  //val calcPos = LcvVideoCalcPos(
  //  someSize2d=myCalcPosSize2d
  //)
  val calcPosStmAdapter = LcvVideoCalcPosStreamAdapter(
    someSize2d=(
      //someSize2d
      cfg.myCalcPosSize2d
    )
  )
  io.infoPop << calcPosStmAdapter.io.infoPop
  //--------
  val mem = WrPulseRdPipeRamSdpPipe(cfg=cfg.myMemCfg)
  val myWrPulse = cloneOf(mem.io.wrPulse)

  val myForkStmVec = StreamFork(
    input=io.push,
    portCount=2,
    synchronous=false,
  )

  myForkStmVec.head.ready := True

  myWrPulse.valid := (
    //io.push.valid
    //io.push.fire
    myForkStmVec.head.valid
  )
  myWrPulse.data := (
    //io.push.payload
    myForkStmVec.head.payload
  )
  //println(
  //  s"test: "
  //  + s"${someSize2d} ${log2Up(someSize2d.x)} "
  //  + s"${mem.cfg.wordCount} "
  //  + s"${calcPos.io.info.pos.x.getWidth} "
  //  + s"${calcPos.io.info.pos.x.getWidth - cnt2dShift.x} "
  //  + s"${calcPos.io.info.pos.y.getWidth} "
  //  + s"${mem.io.wrPulse.addr.getWidth}"
  //)
  myWrPulse.addr := (
    //Cat(
    //  calcPos.io.info.pos.y(cnt2dShift.y),
    //  calcPos.io.info.pos.x(
    //    //calcPos.io.info.pos.x.high
    //    log2Up(someSize2d.x) + cnt2dShift.x - 1
    //    downto cnt2dShift.x
    //  ),
    //).asUInt//.resize(myWrPulseStm.addr.getWidth)
    Cat(
      calcPosStmAdapter.io.infoPop.pos.y(cnt2dShift.y),
      calcPosStmAdapter.io.infoPop.pos.x(
        //calcPos.io.info.pos.x.high
        log2Up(someSize2d.x) + cnt2dShift.x - 1
        downto cnt2dShift.x
      ),
    ).asUInt//.resize(myWrPulseStm.addr.getWidth)
  )

  mem.io.wrPulse <-< myWrPulse

  val myRdAddrPipeStm = Vec.fill(2)(
    cloneOf(mem.io.rdAddrPipe)
  )
  if (cnt2dShift.x > 0) {
    myRdAddrPipeStm.last <-/< myRdAddrPipeStm.head.repeat(
      times=((1 << cnt2dShift.x) - 1)
    )._1
  } else {
    myRdAddrPipeStm.last << myRdAddrPipeStm.head
  }
  mem.io.rdAddrPipe <-/< myRdAddrPipeStm.last
  myForkStmVec.last.translateInto(myRdAddrPipeStm.head)(
    dataAssignment=(outp, inp) => {
      outp.data := outp.data.getZero
      outp.addr := (
        //Cat(
        //  (!calcPos.io.info.pos.y(cnt2dShift.y)),
        //  calcPos.io.info.pos.x(
        //    //calcPos.io.info.pos.x.high
        //    log2Up(someSize2d.x) + cnt2dShift.x - 1
        //    downto cnt2dShift.x
        //  ),
        //).asUInt//.resize(myRdAddrPipeStm.addr.getWidth)
        Cat(
          (!calcPosStmAdapter.io.infoPop.pos.y(cnt2dShift.y)),
          calcPosStmAdapter.io.infoPop.pos.x(
            //calcPos.io.info.pos.x.high
            log2Up(someSize2d.x) + cnt2dShift.x - 1
            downto cnt2dShift.x
          ),
        ).asUInt//.resize(myWrPulseStm.addr.getWidth)
      )
    }
  )
  io.pop <-/< mem.io.rdDataPipe

  //myRdAddrPipeStm.valid := (
  //  //io.push.valid
  //  //|| calcPos.io.en
  //  io.push.fire
  //)

  //myRdAddrPipeStm.data := myRdAddrPipeStm.data.getZero
  //myRdAddrPipeStm.addr := (
  //  //Cat(
  //  //  (!calcPos.io.info.pos.y(cnt2dShift.y)),
  //  //  calcPos.io.info.pos.x(
  //  //    //calcPos.io.info.pos.x.high
  //  //    log2Up(someSize2d.x) + cnt2dShift.x - 1
  //  //    downto cnt2dShift.x
  //  //  ),
  //  //).asUInt//.resize(myRdAddrPipeStm.addr.getWidth)
  //  Cat(
  //    (!calcPosStmAdapter.io.infoPop.pos.y(cnt2dShift.y)),
  //    calcPosStmAdapter.io.infoPop.pos.x(
  //      //calcPos.io.info.pos.x.high
  //      log2Up(someSize2d.x) + cnt2dShift.x - 1
  //      downto cnt2dShift.x
  //    ),
  //  ).asUInt//.resize(myWrPulseStm.addr.getWidth)
  //)

  //io.pop <-/< mem.io.rdDataPipe
  //--------
  //val mySeenWrPulseFire = Bool()
  //val rSavedSeenWrPulseFire = Reg(Bool(), init=False)
  //val stickySeenWrPulseFire = (
  //  mySeenWrPulseFire
  //  || rSavedSeenWrPulseFire
  //)

  //mySeenWrPulseFire := mem.io.wrPulse.fire //io.push.fire
  //when (mySeenWrPulseFire) {
  //  rSavedSeenWrPulseFire := True
  //}

  //val mySeenRdAddrPipeFire = Bool()
  //val rSavedSeenRdAddrPipeFire = Reg(Bool(), init=False)
  //val stickySeenRdAddrPipeFire = (
  //  mySeenRdAddrPipeFire
  //  || rSavedSeenRdAddrPipeFire
  //)

  //mySeenRdAddrPipeFire := mem.io.rdAddrPipe.fire
  //when (mySeenRdAddrPipeFire) {
  //  rSavedSeenRdAddrPipeFire := True
  //}
  //val myInfoPopStm = cloneOf(io.infoPop)

  //myInfoPopStm.valid := (
  //  //stickySeenWrPulseFire
  //  //&& 
  //  stickySeenRdAddrPipeFire
  //)
  //myInfoPopStm.payload := calcPos.io.info
  //io.infoPop <-/< myInfoPopStm
  ////io.infoPop << myInfoPopStm

  //calcPos.io.en := (
  //  //History[Bool](
  //  //  that=False,
  //  //  length=3,
  //  //  when=stickySeenWrPulseFire,
  //  //  init=True,
  //  //).last
  //  //|| 
  //  io.infoPop.fire
  //)
  ////calcPos.io.en := (
  ////  RegNext(
  ////    (
  ////      stickySeenWrPulseFire
  ////      && stickySeenRdAddrPipeFire
  ////    ),
  ////    init=False
  ////  )
  ////)

  //when (calcPos.io.en) {
  //  rSavedSeenWrPulseFire := False
  //  rSavedSeenRdAddrPipeFire := False
  //}
  //--------
  //--------
}

