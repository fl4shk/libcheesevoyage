package libcheesevoyage.gfx
import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2
import libcheesevoyage.general.FifoIo
import libcheesevoyage.general.AsyncReadFifo

import spinal.core._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer
import scala.math._

object LcvVideoDithererPopPayloadNoCol {
  def ditherDeltaWidth: Int = log2Up(4)
  def coordT(): Vec2[UInt] = Vec2(UInt(16 bits))
}
class LcvVideoDithererPopPayloadNoCol(
  //rgbConfig: RgbConfig
) extends Bundle {
  //--------
  //val outpCol = out(Rgb(rgbConfig))

  //val col = Rgb(rgbConfig)
  val frameCnt = UInt(ditherDeltaWidth bits)
  //val nextPos = coordT()
  val pos = coordT()
  val pastPos = coordT()
  //--------
  def ditherDeltaWidth = LcvVideoDithererPopPayloadNoCol.ditherDeltaWidth
  def coordT() = LcvVideoDithererPopPayloadNoCol.coordT()
  //--------
}
case class LcvVideoDithererPopPayload(
  rgbConfig: RgbConfig
) extends LcvVideoDithererPopPayloadNoCol {
  val col = Rgb(rgbConfig)
}

case class LcvVideoDithererIo(
  //fbSize2d: ElabVec2[Int],
  rgbConfig: RgbConfig,
) extends Bundle with IMasterSlave
{
  //val en = in port Bool()
  //val flow = slave Flow(Rgb(rgbConfig))
  //val fifoIo = FifoIo(Rgb(rgbConfig))
  //val push = fifoIo.push
  //val pop = fifoIo.pop
  //val inpCol = push.payload.col
  //val outpCol = pop.payload
  val push = slave Stream(Rgb(rgbConfig))
  //val pop = master Stream(LcvVideoDithererPopPayload(rgbConfig=rgbConfig))
  val pop = master Stream(LcvVideoDithererPopPayload(
    rgbConfig=outRgbConfig
  ))
  val inpCol = push.payload
  val outpPayload = pop.payload
  val outpCol = outpPayload.col
  val outpNextPos = out(LcvVideoDithererPopPayloadNoCol.coordT())
  //val outpCol = misc.col
  //val pop = master Stream(Rgb(rgbConfig))
  //val misc = out(LcvVideoDithererMiscIo(rgbConfig=rgbConfig))

  //val col = out port RgbColorLayt(
  //  CHAN_WIDTH=VideoDithererBus.OUT_CHAN_WIDTH(CHAN_WIDTH)
  //)
  //val col = in port RgbColorLayt(
  //  CHAN_WIDTH=CHAN_WIDTH
  //)
  //val inpValid = inpFlow.valid
  //val inpCol = inpFlow.payload
  //--------
  def asMaster(): Unit = {
    master(push)
    slave(pop)
  }
  //def asSlave(): Unit = {
  //  slave(push)
  //  master(pop)
  //}
  //--------
  //def ditherDeltaWidth(): Int = log2Up(4)
  def ditherDeltaWidth = outpPayload.ditherDeltaWidth
  def outChanWidth(chanWidth: Int): Int = (chanWidth - chanWidthDelta)

  def outRChanWidth: Int = outChanWidth(rgbConfig.rWidth)
  def outGChanWidth: Int = outChanWidth(rgbConfig.gWidth)
  def outBChanWidth: Int = outChanWidth(rgbConfig.bWidth)
  def outRgbConfig: RgbConfig = RgbConfig(
    rWidth=outRChanWidth,
    gWidth=outGChanWidth,
    bWidth=outBChanWidth,
  )

  def chanWidthDelta: Int = 2
  //def coordT(): Vec2[UInt] = Vec2(UInt(16 bits))
  def coordT() = outpPayload.coordT()
  ////def tagDct(self):
  //// return self._TagDct
  ////def inpTag(self):
  //// //return self._InpTag
  //// return self.tagDct()["inp"]
  ////def outpTag(self):
  //// //return self._OutpTag
  //// return self.tagDct()["outp"]
  //--------
}
//case class LcvVideoDitherer(
//  fbSize2d: ElabVec2[Int],
//  rgbConfig: RgbConfig,
//  //fifoDepth: Int=1
//) extends Component {
//  //--------
//  val io = LcvVideoDithererIo(rgbConfig=rgbConfig)
//  val push = io.push
//  val pop = io.pop
//  //--------
//  val fifoDepth = 4
//  val fifo = AsyncReadFifo(
//    //dataType=Rgb(rgbConfig),
//    dataType=LcvVideoDithererPopPayload(rgbConfig=rgbConfig),
//    depth=fifoDepth,
//  )
//  val fifoPush = fifo.io.push
//  val fifoPop = fifo.io.pop
//  //val sbPush = PipeSkidBuf(
//  //  dataType=LcvVideoDithererPopPayload(rgbConfig=rgbConfig),
//  //  optIncludeBusy=false,
//  //)
//  //val sbPop = PipeSkidBuf(
//  //  dataType=LcvVideoDithererPopPayload(rgbConfig=rgbConfig),
//  //  optIncludeBusy=false,
//  //)
//
//  //val fifoPushPayload = fifoPush.payload
//  //val rPopPayload = Reg(LcvVideoDithererPopPayload(rgbConfig=rgbConfig))
//  //rPopPayload.init(rPopPayload.getZero)
//  fifoPush.valid := push.valid
//  val tempPayload = fifoPush.payload
//  //tempPayload.col := push.payload
//
//  //tempPayload.col := push.payload
//  //rPopPayload
//  push.ready := fifoPush.ready
//
//  pop << fifoPop
//
//  //push << io.fifoIo
//  //io.fifoIo.push << push
//  //io.fifoIo.pop << pop
//  //push >> fifoPush
//  //pop << fifoPop
//  //io.fifoIo <> fifo.io
//  //--------
//  //def outChanWidth(): Int = 
//  def outRgbConfig: RgbConfig = io.outRgbConfig
//  def ditherDeltaWidth: Int = io.ditherDeltaWidth
//  def patVal(value: Int): UInt = {
//    //return Const(val, self.bus().DITHER_DELTA_WIDTH())
//    return U(f"$ditherDeltaWidth'd$value")
//  }
//  val pattern = Vec(
//    Vec(
//      Vec(patVal(0), patVal(1)),
//      Vec(patVal(3), patVal(2)),
//    ),
//    Vec(
//      Vec(patVal(1), patVal(0)),
//      Vec(patVal(2), patVal(3)),
//    ),
//    Vec(
//      Vec(patVal(3), patVal(2)),
//      Vec(patVal(0), patVal(1)),
//    ),
//    Vec(
//      Vec(patVal(2), patVal(3)),
//      Vec(patVal(1), patVal(0)),
//    ),
//  )
//  //--------
//  //--------
//  val loc = new Area {
//    //val pastPopFire = Reg(Bool()) init(False)
//    //pastPopFire := pop.fire
//
//    val rPastTempPayload = Reg(LcvVideoDithererPopPayload(
//      rgbConfig=rgbConfig
//    ))
//    rPastTempPayload.init(rPastTempPayload.getZero)
//    when (push.fire) {
//      rPastTempPayload := tempPayload
//    }
//    //when (pop.fire) {
//    //  pastPopPayload := pop.payload
//    //}
//    //val pastColOut = pastPopPayload.col
//
//    ////pastColOut = Splitrec(RgbColorLayt(
//    ////  CHAN_WIDTH=bus.OUT_CHAN_WIDTH(self.CHAN_WIDTH())
//    ////))
//    //val pastColOut = Reg(Rgb(outRgbConfig))
//    //pastColOut.init(pastColOut.getZero)
//
//    // Update `io.misc.pos` and `io.misc.frameCnt`
//    //posPlus1 = {"x": io.misc.pos.x + 0x1, "y": io.misc.pos.y + 0x1}
//    //val posPlus1 = ElabVec2(
//    //  x=io.outpPayload.pos.x + 0x1,
//    //  y=io.outpPayload.pos.y + 0x1
//    //)
//    val posPlus1 = ElabVec2(
//      x=rPastTempPayload.pos.x + 0x1,
//      y=rPastTempPayload.pos.y + 0x1
//    )
//
//    // Perform dithering
//    //dicol = Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH()))
//    val dicol = Rgb(rgbConfig)
//    //chanDelta
//    //  = self.PATTERN()[io.misc.frameCnt]
//    //    [Value.cast(io.misc.pos.y[0])][Value.cast(io.misc.pos.x[0])]
//
//    //val chanDelta = (
//    //  pattern
//    //    (io.outpPayload.frameCnt)
//    //    (io.outpPayload.pos.y(0 downto 0))
//    //    (io.outpPayload.pos.x(0 downto 0))
//    //)
//    val chanDelta = (
//      pattern
//        (rPastTempPayload.frameCnt)
//        (rPastTempPayload.pos.y(0 downto 0))
//        (rPastTempPayload.pos.x(0 downto 0))
//    )
//
//    //val chanDeltaTempFc = UInt()
//    //val chanDeltaTempFrameCnt = UInt(ditherDeltaWidth() bits)
//    //switch (io.misc.frameCnt) {
//    //  for (idx <- 0 to pattern.size - 1) {
//    //    is (idx) {
//    //      chanDeltaTempFrameCnt := pattern()
//    //    }
//    //  }
//    //}
//    //colInPlusDelta
//    //  = Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH() + 1))
//    val colInPlusDelta = Rgb(RgbConfig(
//      rWidth=rgbConfig.rWidth + 1,
//      gWidth=rgbConfig.gWidth + 1,
//      bWidth=rgbConfig.bWidth + 1,
//    ))
//
//    //val rFrameCnt = Reg(UInt(ditherDeltaWidth bits)) init(0x0)
//    //io.outpPayload.frameCnt := rFrameCnt
//
//    //val rPos = Reg(io.outpPayload.coordT())
//    //rPos.init(rPos.getZero)
//    //io.outpPayload.pos := rPos
//
//    //val rPastPos = Reg(io.outpPayload.coordT())
//    //rPastPos.init(rPastPos.getZero)
//    //io.outpPayload.pastPos := rPastPos
//  }
//
//  //when (~push.fire) {
//  //  tempPayload := loc.rPastTempPayload
//  //}
//  //tempPayload := loc.rPastTempPayload
//
//  when (loc.posPlus1.x < fbSize2d.x) {
//    //m.d.sync += io.misc.pos.x := (loc.posPlus1.x)
//    //m.d.comb += [
//    //tempPayload.nextPos.x := loc.posPlus1.x
//    //tempPayload.nextPos.y := loc.rPastTempPayload.pos.y
//    tempPayload.pos.x := loc.posPlus1.x
//    tempPayload.pos.y := loc.rPastTempPayload.pos.y
//    //]
//  } otherwise { // when (loc.posPlus1.x >= fbSize2d.x):
//    //m.d.sync += io.misc.pos.x := (0x0)
//    //m.d.comb +=
//    //tempPayload.nextPos.x := 0x0
//    tempPayload.pos.x := 0x0
//    when (loc.posPlus1.y < fbSize2d.y) {
//      //m.d.comb +=
//      //tempPayload.nextPos.y := loc.posPlus1.y
//      tempPayload.pos.y := loc.posPlus1.y
//    } otherwise {
//      // when (loc.posPlus1.y >= fbSize2d.y):
//      //m.d.comb += 
//      //tempPayload.nextPos.y := 0x0
//      tempPayload.pos.y := 0x0
//
//      // This wraps around to zero automatically due to
//      // modular arithmetic, so we don't need another mux just
//      // for this.
//      //when (io.en)
//      //when (push.fire) {
//      //  //m.d.sync += io.misc.frameCnt := (io.misc.frameCnt + 0x1)
//      //  //loc.rFrameCnt := loc.rFrameCnt + 0x1
//      //  tempPayload.frameCnt := loc.rPastTempPayload.frameCnt + 0x1
//      //}
//    }
//  }
//
//  tempPayload.frameCnt := loc.rPastTempPayload.frameCnt + 0x1
//  tempPayload.pastPos := loc.rPastTempPayload.pos
//  //tempPayload.pos := tempPayload.nextPos
//  //when (io.en)
//  when (push.fire) {
//    ////m.d.sync += [
//    ////loc.pastColOut := io.outpCol
//    ////io.misc.pastPos := (io.misc.pos)
//    ////loc.rPastPos := io.misc.pos
//    //tempPayload.pastPos := loc.rPastTempPayload.pos
//    ////]
//
//    ////m.d.sync += [
//    ////io.misc.pos := (io.misc.nextPos)
//    ////loc.rPos := io.misc.nextPos
//    //tempPayload.pos := tempPayload.nextPos
//    ////]
//
//    //m.d.comb += [
//    loc.colInPlusDelta.r := (io.inpCol.r + loc.chanDelta).resized
//    loc.colInPlusDelta.g := (io.inpCol.g + loc.chanDelta).resized
//    loc.colInPlusDelta.b := (io.inpCol.b + loc.chanDelta).resized
//    //]
//
//    // Saturating arithmetic to prevent an artifact
//    when (loc.colInPlusDelta.r.msb
//      //[len(loc.colInPlusDelta.r) - 1]
//    ) {
//      //m.d.comb += loc.dicol.r := (-1)
//      //loc.dicol.r := -1
//      loc.dicol.r := (default -> True)
//    } otherwise {
//      //m.d.comb += loc.dicol.r := (loc.colInPlusDelta.r
//      //  [:len(loc.dicol.r)])
//      loc.dicol.r := loc.colInPlusDelta.r(loc.dicol.r.bitsRange)
//    }
//    //m.d.comb += loc.dicol.r := (loc.colInPlusDelta.r
//    //  [:len(loc.dicol.r)])
//
//    when (loc.colInPlusDelta.g.msb
//      //[len(loc.colInPlusDelta.g) - 1]):
//    ) {
//      //m.d.comb += loc.dicol.g := (-1)
//      //loc.dicol.g := -1
//      loc.dicol.g := (default -> True)
//    } otherwise {
//      //m.d.comb += loc.dicol.g := (loc.colInPlusDelta.g
//      //  [:len(loc.dicol.g)])
//      loc.dicol.g := loc.colInPlusDelta.g(loc.dicol.g.bitsRange)
//    }
//    //m.d.comb += loc.dicol.g := (loc.colInPlusDelta.g
//    //  [:len(loc.dicol.g)])
//
//    when (loc.colInPlusDelta.b.msb
//      //[len(loc.colInPlusDelta.b) - 1]):
//    ) {
//      //m.d.comb += loc.dicol.b := (-1)
//      //loc.dicol.b := -1
//      loc.dicol.b := (default -> True)
//    } otherwise {
//      //m.d.comb += loc.dicol.b := (loc.colInPlusDelta.b
//      //  [:len(loc.dicol.b)])
//      loc.dicol.b := loc.colInPlusDelta.b(loc.dicol.b.bitsRange)
//    }
//    //m.d.comb += loc.dicol.b := (loc.colInPlusDelta.b
//    //  [:len(loc.dicol.b)])
//
//    ////m.d.comb += [
//    ////loc.dicol.r := (loc.COL_IN_PLUS_DELTA["r"]),
//    ////loc.dicol.g := (loc.COL_IN_PLUS_DELTA["g"]),
//    ////loc.dicol.b := (loc.COL_IN_PLUS_DELTA["b"]),
//
//    //io.misc.col.r := (loc.dicol.r[bus.CHAN_WIDTH_DELTA():]),
//    //io.misc.col.g := (loc.dicol.g[bus.CHAN_WIDTH_DELTA():]),
//    //io.misc.col.b := (loc.dicol.b[bus.CHAN_WIDTH_DELTA():]),
//    tempPayload.col.r := loc.dicol.r(
//      loc.dicol.r.high downto io.chanWidthDelta
//    ).resized
//    tempPayload.col.g := loc.dicol.g(
//      loc.dicol.g.high downto io.chanWidthDelta
//    ).resized
//    tempPayload.col.b := loc.dicol.b(
//      loc.dicol.b.high downto io.chanWidthDelta
//    ).resized
//    ////io.misc.col.r := (loc.dicol.r[bus.CHAN_WIDTH_DELTA():]),
//    ////io.misc.col.g := (-1),
//    ////io.misc.col.b := (loc.dicol.b[bus.CHAN_WIDTH_DELTA():]),
//    ////]
//
//  } otherwise { // when (~push.fire)
//    //m.d.comb += [
//    //loc.colInPlusDelta := 0x0
//    loc.colInPlusDelta := loc.colInPlusDelta.getZero
//    //loc.dicol := 0x0
//    loc.dicol := loc.dicol.getZero
//    //io.misc.col := loc.pastColOut
//    //tempPayload.col := loc.pastColOut
//    tempPayload.col := loc.rPastTempPayload.col
//    //io.misc.nextPos := io.misc.pos
//    //tempPayload.nextPos := loc.rPastTempPayload.nextPos
//    //]
//  }
//
//  //--------
//}
case class LcvVideoDitherer(
  fbSize2d: ElabVec2[Int],
  rgbConfig: RgbConfig,
) extends Component {
  //--------
  val io = LcvVideoDithererIo(rgbConfig=rgbConfig)
  val push = io.push
  val pop = io.pop
  //--------
  def outRgbConfig: RgbConfig = io.outRgbConfig
  def ditherDeltaWidth: Int = io.ditherDeltaWidth
  def patVal(value: Int): UInt = {
    //return Const(val, self.bus().DITHER_DELTA_WIDTH())
    return U(f"$ditherDeltaWidth'd$value")
  }
  val pattern = Vec(
    Vec(
      Vec(patVal(0), patVal(1)),
      Vec(patVal(3), patVal(2)),
    ),
    Vec(
      Vec(patVal(1), patVal(0)),
      Vec(patVal(2), patVal(3)),
    ),
    Vec(
      Vec(patVal(3), patVal(2)),
      Vec(patVal(0), patVal(1)),
    ),
    Vec(
      Vec(patVal(2), patVal(3)),
      Vec(patVal(1), patVal(0)),
    ),
  )
  //--------
  val fifoDepth = 4
  val fifo = AsyncReadFifo(
    //dataType=Rgb(rgbConfig),
    dataType=LcvVideoDithererPopPayload(rgbConfig=rgbConfig),
    depth=fifoDepth,
  )
  val fifoPush = fifo.io.push
  val fifoPop = fifo.io.pop
  //val sbPush = PipeSkidBuf(
  //  dataType=LcvVideoDithererPopPayload(rgbConfig=rgbConfig),
  //  optIncludeBusy=false,
  //)
  //val sbPop = PipeSkidBuf(
  //  dataType=LcvVideoDithererPopPayload(rgbConfig=rgbConfig),
  //  optIncludeBusy=false,
  //)

  //val fifoPushPayload = fifoPush.payload
  //val rPopPayload = Reg(LcvVideoDithererPopPayload(rgbConfig=rgbConfig))
  //rPopPayload.init(rPopPayload.getZero)
  fifoPush.valid := push.valid
  val tempPayload = fifoPush.payload
  //tempPayload.col := push.payload

  //tempPayload.col := push.payload
  //rPopPayload
  push.ready := fifoPush.ready

  pop << fifoPop

  //push << io.fifoIo
  //io.fifoIo.push << push
  //io.fifoIo.pop << pop
  //push >> fifoPush
  //pop << fifoPop
  //io.fifoIo <> fifo.io
  //--------
  //val loc = new Area {
  //past_col_out = Splitrec(RgbColorLayt(
  //  CHAN_WIDTH=bus.OUT_CHAN_WIDTH(self.CHAN_WIDTH())
  //))
  val rPastTempPayload = Reg(LcvVideoDithererPopPayload(
    rgbConfig=rgbConfig
  ))
  rPastTempPayload.init(rPastTempPayload.getZero)
  when (push.fire) {
    rPastTempPayload := tempPayload
  }
  // Update `outp.pos` and `outp.frame_cnt`
  //POS_PLUS_1 = {"x": outp.pos.x + 0x1, "y": outp.pos.y + 0x1}
  //val posPlus1 = ElabVec2(
  //  x=io.outpPayload.pos.x + 0x1,
  //  y=io.outpPayload.pos.y + 0x1
  //)
  val posPlus1 = LcvVideoDithererPopPayloadNoCol.coordT()
  posPlus1.x.resized := io.outpPayload.pos.x.resized + 0x1
  posPlus1.y.resized := io.outpPayload.pos.y.resized + 0x1

  // Perform dithering
  //dicol = Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH()))
  val dicol = Rgb(rgbConfig)
  //CHAN_DELTA 
  //  = pattern(io.outpPayload.frame_cnt)
  //    [Value.cast(outp.pos.y[0])][Value.cast(outp.pos.x[0])]
  val chanDelta = (
    pattern
      (rPastTempPayload.frameCnt)
      (rPastTempPayload.pos.y(0 downto 0))
      (rPastTempPayload.pos.x(0 downto 0))
  )
  //col_in_plus_delta \
  //  = Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH() + 1))
  val colInPlusDelta = Rgb(RgbConfig(
    rWidth=rgbConfig.rWidth + 1,
    gWidth=rgbConfig.gWidth + 1,
    bWidth=rgbConfig.bWidth + 1,
  ))
  when (posPlus1.x < fbSize2d.x) {
    //m.d.sync += tempPayload.pos.x := (posPlus1.x)
    //m.d.comb += [
      io.outpNextPos.x := posPlus1.x
      io.outpNextPos.y := tempPayload.pos.y
    //]
  } otherwise { // when (posPlus1.x >= fbSize2d.x):
    //m.d.sync += tempPayload.pos.x := (0x0)
    //m.d.comb +=
    io.outpNextPos.x := 0x0
    when (posPlus1.y < fbSize2d.y) {
      //m.d.comb += 
      io.outpNextPos.y := posPlus1.y
    } otherwise {
      // when (posPlus1.y >= fbSize2d.y):
      //m.d.comb +=
      io.outpNextPos.y := 0x0

      // This wraps around to zero automatically due to
      // modular arithmetic, so we don't need another mux just
      // for this.
      //when (inp.en) {
      //  //m.d.sync += 
      //  io.frameCnt := tempPayload.frameCnt + 0x1
      //}
    }
  }
  // This wraps around to zero automatically due to
  // modular arithmetic, so we don't need another mux just
  // for this.
  tempPayload.frameCnt := rPastTempPayload.frameCnt + 0x1
  tempPayload.pastPos := rPastTempPayload.pos
  //when (io.en)
  when (push.fire) {
    ////m.d.sync += [
    ////pastColOut := io.outpCol
    ////io.misc.pastPos := (io.misc.pos)
    ////rPastPos := io.misc.pos
    //tempPayload.pastPos := rPastTempPayload.pos
    ////]

    ////m.d.sync += [
    ////io.misc.pos := (io.misc.nextPos)
    ////rPos := io.misc.nextPos
    //tempPayload.pos := tempPayload.nextPos
    ////]

    //m.d.comb += [
    colInPlusDelta.r := (io.inpCol.r + chanDelta).resized
    colInPlusDelta.g := (io.inpCol.g + chanDelta).resized
    colInPlusDelta.b := (io.inpCol.b + chanDelta).resized
    //]

    // Saturating arithmetic to prevent an artifact
    when (colInPlusDelta.r.msb
      //[len(colInPlusDelta.r) - 1]
    ) {
      //m.d.comb += dicol.r := (-1)
      //dicol.r := -1
      dicol.r := (default -> True)
    } otherwise {
      //m.d.comb += dicol.r := (colInPlusDelta.r
      //  [:len(dicol.r)])
      dicol.r := colInPlusDelta.r(dicol.r.bitsRange)
    }
    //m.d.comb += dicol.r := (colInPlusDelta.r
    //  [:len(dicol.r)])

    when (colInPlusDelta.g.msb
      //[len(colInPlusDelta.g) - 1]):
    ) {
      //m.d.comb += dicol.g := (-1)
      //dicol.g := -1
      dicol.g := (default -> True)
    } otherwise {
      //m.d.comb += dicol.g := (colInPlusDelta.g
      //  [:len(dicol.g)])
      dicol.g := colInPlusDelta.g(dicol.g.bitsRange)
    }
    //m.d.comb += dicol.g := (colInPlusDelta.g
    //  [:len(dicol.g)])

    when (colInPlusDelta.b.msb
      //[len(colInPlusDelta.b) - 1]):
    ) {
      //m.d.comb += dicol.b := (-1)
      //dicol.b := -1
      dicol.b := (default -> True)
    } otherwise {
      //m.d.comb += dicol.b := (colInPlusDelta.b
      //  [:len(dicol.b)])
      dicol.b := colInPlusDelta.b(dicol.b.bitsRange)
    }
    //m.d.comb += dicol.b := (colInPlusDelta.b
    //  [:len(dicol.b)])

    ////m.d.comb += [
    ////dicol.r := (COL_IN_PLUS_DELTA["r"]),
    ////dicol.g := (COL_IN_PLUS_DELTA["g"]),
    ////dicol.b := (COL_IN_PLUS_DELTA["b"]),

    //io.misc.col.r := (dicol.r[bus.CHAN_WIDTH_DELTA():]),
    //io.misc.col.g := (dicol.g[bus.CHAN_WIDTH_DELTA():]),
    //io.misc.col.b := (dicol.b[bus.CHAN_WIDTH_DELTA():]),
    tempPayload.col.r := dicol.r(
      dicol.r.high downto io.chanWidthDelta
    ).resized
    tempPayload.col.g := dicol.g(
      dicol.g.high downto io.chanWidthDelta
    ).resized
    tempPayload.col.b := dicol.b(
      dicol.b.high downto io.chanWidthDelta
    ).resized
    ////io.misc.col.r := (dicol.r[bus.CHAN_WIDTH_DELTA():]),
    ////io.misc.col.g := (-1),
    ////io.misc.col.b := (dicol.b[bus.CHAN_WIDTH_DELTA():]),
    ////]

  } otherwise { // when (~push.fire)
    //m.d.comb += [
    //colInPlusDelta := 0x0
    colInPlusDelta := colInPlusDelta.getZero
    //dicol := 0x0
    dicol := dicol.getZero
    //io.misc.col := pastColOut
    //tempPayload.col := pastColOut
    tempPayload.col := rPastTempPayload.col
    //io.misc.nextPos := io.misc.pos
    //tempPayload.nextPos := rPastTempPayload.nextPos
    //]
  }
  //}
  //--------
}
