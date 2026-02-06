package libcheesevoyage.gfx
import libcheesevoyage.general.WrPulseRdPipeSimpleDualPortMemIo
import libcheesevoyage.general.WrPulseRdPipeSimpleDualPortMem
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

case class LcvVideoLineBufWithCalcPosConfig(
  someSize2d: ElabVec2[Int],
  rgbCfg: RgbConfig,
  cnt2dShift: ElabVec2[Int],
) {
}

case class LcvVideoLineBufWithCalcPosIo(
  cfg: LcvVideoLineBufWithCalcPosConfig
) extends Bundle {
  //val wrEn = in(Bool())
  //val wrData = in(Rgb(cfg.rgbCfg))

  val push = slave(Flow(Rgb(cfg.rgbCfg)))
  val pop = master(Stream(Rgb(cfg.rgbCfg)))

  val info = out(LcvVideoPosInfo(someSize2d=cfg.someSize2d))
}

case class LcvVideoLineBufWithCalcPos(
  cfg: LcvVideoLineBufWithCalcPosConfig
) extends Component {
  val io = LcvVideoLineBufWithCalcPosIo(cfg=cfg)

  //val mem = (
  //  WrPulseRdPipeSimpleDualPortMem(
  //    dataType=Rgb(cfg.rgbCfg),
  //    wordType=Rgb(cfg.rgbCfg),
  //    wordCount=(
  //      cfg.someSize2d.x * (1 << cfg.cnt2dShift.x)
  //    ),
  //    pipeName="LcvVideoLineBufWithCalcPos"
  //  )(
  //    setWordFunc=(myUnionIdx, outp, inp, rdMemWord) => {
  //      outp := rdMemWord
  //    }
  //  )
  //)
}

