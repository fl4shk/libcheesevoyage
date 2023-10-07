package libcheesevoyage.gfx
import libcheesevoyage.general.DualTypeNumVec2
import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2

import spinal.core._
import spinal.lib._

object LcvVideoCalcPosInfo {
  def coordT(
    //vgaTimingInfo: LcvVgaTimingInfo,
    fbSize2d: ElabVec2[Int]
  ): DualTypeNumVec2[UInt, UInt] = LcvVgaCtrlMiscIo.coordT(
    //vgaTimingInfo=vgaTimingInfo
    fbSize2d=fbSize2d,
  )
}

case class LcvVideoCalcPosInfo(
  //rgbConfig: RgbConfig
  //vgaTimingInfo: LcvVgaTimingInfo,
  fbSize2d: ElabVec2[Int]
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
  val changingScanline = Bool()
  //--------
  def coordT() = LcvVideoCalcPosInfo.coordT(
    fbSize2d=fbSize2d,
  )
  //--------
}
case class LcvVideoCalcPosIo(
  fbSize2d: ElabVec2[Int],
) extends Bundle with IMasterSlave {
  //--------
  val en = in Bool()
  val info = out(LcvVideoCalcPosInfo(fbSize2d=fbSize2d))
  //--------
  def asMaster(): Unit = {
    out(en)
    in(info)
  }
  //--------
}
case class LcvVideoCalcPos(
  fbSize2d: ElabVec2[Int],
) extends Component {
  //--------
  val io = LcvVideoCalcPosIo(fbSize2d=fbSize2d)
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

  val rChangingScanline = Reg(cloneOf(info.changingScanline))
  rChangingScanline.init(rChangingScanline.getZero)
  info.changingScanline := rChangingScanline

  val rPastInfo = RegNext(io.info) init(io.info.getZero)

  when (io.en) {
    val dithConcat = rPosWillOverflow.asBits
    //dithConcat(0) := rPosWillOverflow.x
    //dithConcat(1) := rPosWillOverflow.y
    switch (dithConcat) {
      // overflowX=0, overflowY=don't care
      //is (B"-0") 
      //is(M"1-0") 
      is(M"-0") {
        info.nextPos.x := info.pos.x + 1
        info.nextPos.y := info.pos.y
        rPosWillOverflow.x := info.pos.x === fbSize2d.x - 2
        //rPosWillOverflow.y := info.pos.y === fbSize2d.y - 1
        rChangingScanline := False
      }
      // overflowX=1, overflowY=0
      //is (B"101")
      is (B"01") {
        info.nextPos.x := 0
        rPosWillOverflow.x := False
        rChangingScanline := True
        info.nextPos.y := info.pos.y + 1
        rPosWillOverflow.y := info.pos.y === fbSize2d.y - 2
      }
      // overflowX=1, overflowY=1
      //is (B"111")
      is (B"11") {
        info.nextPos.x := 0
        rPosWillOverflow.x := False
        rChangingScanline := True
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
    //when (!rPosWillOverflow.x) {
    //  info.nextPos.x := info.pos.x + 1
    //  info.nextPos.y := info.pos.y
    //  rPosWillOverflow.x := info.pos.x === fbSize2d.x - 2
    //  //rPosWillOverflow.y := info.pos.y === fbSize2d.y - 1
    //  rChangingScanline := False
    //} otherwise {
    //  info.nextPos.x := 0
    //  rPosWillOverflow.x := False
    //  rChangingScanline := True
    //  //when (info.pos.y =/= fbSize2d.y - 1)
    //  when (!rPosWillOverflow.y) {
    //    info.nextPos.y := info.pos.y + 1
    //    rPosWillOverflow.y := info.pos.y === fbSize2d.y - 2
    //  } otherwise {
    //    info.nextPos.y := 0x0
    //    rPosWillOverflow.y := False
    //  }
    //}
    rPos := info.nextPos
    rPastPos := rPos
  } otherwise {
    io.info := rPastInfo
  }
}
