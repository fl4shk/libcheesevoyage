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
  def coordElemT(): UInt = UInt(16 bits)
  def coordT(): Vec2[UInt] = Vec2(coordElemT())
}
class LcvVideoDithererPopPayloadNoCol(
  //rgbConfig: RgbConfig
) extends Bundle {
  //--------
  //val outpCol = out(Rgb(rgbConfig))

  //val col = Rgb(rgbConfig)
  val frameCnt = UInt(ditherDeltaWidth bits)
  val nextPos = coordT()
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

object LcvVideoDithererIo {
  def outChanWidth(
    //rgbConfig: RgbConfig,
    chanWidth: Int,
  ): Int = (chanWidth - chanWidthDelta)

  def outRChanWidth(
    rgbConfig: RgbConfig
  ): Int = outChanWidth(chanWidth=rgbConfig.rWidth)
  def outGChanWidth(
    rgbConfig: RgbConfig
  ): Int = outChanWidth(chanWidth=rgbConfig.gWidth)
  def outBChanWidth(
    rgbConfig: RgbConfig
  ): Int = outChanWidth(chanWidth=rgbConfig.bWidth)
  def outRgbConfig(
    rgbConfig: RgbConfig
  ): RgbConfig = RgbConfig(
    rWidth=outRChanWidth(rgbConfig=rgbConfig),
    gWidth=outGChanWidth(rgbConfig=rgbConfig),
    bWidth=outBChanWidth(rgbConfig=rgbConfig),
  )

  def chanWidthDelta: Int = 2
}

case class LcvVideoDithererIo(
  //fbSize2d: ElabVec2[Int],
  rgbConfig: RgbConfig,
  //vgaTimingInfo: LcvVgaTimingInfo,
) extends Bundle with IMasterSlave
{
  //val push = slave Stream(Rgb(rgbConfig))
  val push = slave Flow(Rgb(rgbConfig))
  //val pop = master Stream(LcvVideoDithererPopPayload(rgbConfig=rgbConfig))
  //val pop = master Stream(LcvVideoDithererPopPayload(
  //  rgbConfig=outRgbConfig
  //))
  //val pop = master Flow(LcvVideoDithererPopPayload(
  //  rgbConfig=outRgbConfig
  //))
  //val outpPayload = out(LcvVideoDithererPopPayload(
  //  rgbConfig=outRgbConfig
  //))
  val outp = out(LcvVideoDithererPopPayload(
    rgbConfig=outRgbConfig
  ))
  val inpEn = push.valid
  val inpCol = push.payload
  //val outpPayload = pop.payload
  //val outpCol = outpPayload.col
  //val outpNextPos = out(LcvVideoDithererPopPayloadNoCol.coordT())
  //--------
  def asMaster(): Unit = {
    master(push)
    //in(outpPayload)
    //slave(pop)
    in(outp)
    //in(outpNextPos)
  }
  //def asSlave(): Unit = {
  //  slave(push)
  //  master(pop)
  //}
  //--------
  //def ditherDeltaWidth(): Int = log2Up(4)
  def ditherDeltaWidth = outp.ditherDeltaWidth

  //def chanWidthDelta: Int = 2
  def outChanWidth(chanWidth: Int): Int = LcvVideoDithererIo.outChanWidth(
    //rgbConfig=rgbConfig,
    chanWidth=chanWidth,
  )

  def outRChanWidth: Int = LcvVideoDithererIo.outRChanWidth(
    rgbConfig=rgbConfig,
  )
  def outGChanWidth: Int = LcvVideoDithererIo.outGChanWidth(
    rgbConfig=rgbConfig,
  )
  def outBChanWidth: Int = LcvVideoDithererIo.outBChanWidth(
    rgbConfig=rgbConfig,
  )
  def outRgbConfig: RgbConfig = LcvVideoDithererIo.outRgbConfig(
    rgbConfig=rgbConfig,
  )

  def chanWidthDelta: Int = LcvVideoDithererIo.chanWidthDelta

  //def coordT(): Vec2[UInt] = Vec2(UInt(16 bits))
  def coordT() = outp.coordT()
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
case class LcvVideoDitherer(
  rgbConfig: RgbConfig,
  fbSize2d: ElabVec2[Int],
) extends Component {
  //--------
  val io = LcvVideoDithererIo(rgbConfig=rgbConfig)
  //val push = io.push
  //val pop = io.pop
  val inpEn = io.inpEn
  val inpCol = io.inpCol
  //val outp = io.outp
  val outp = io.outp
  //--------
  def outRgbConfig: RgbConfig = io.outRgbConfig
  def ditherDeltaWidth: Int = io.ditherDeltaWidth
  def patVal(value: Int): UInt = {
    //return Const(val, self.bus().DITHER_DELTA_WIDTH())
    return U(f"$ditherDeltaWidth'd$value")
  }
  def chanWidthDelta: Int = LcvVideoDithererIo.chanWidthDelta
  def coordT() = LcvVideoDithererPopPayloadNoCol.coordT()
  //def htiming: LcvVgaTimingHv = {
  //  return vgaTimingInfo.htiming
  //}
  //def vtiming: LcvVgaTimingHv = {
  //  return vgaTimingInfo.vtiming
  //}
  //def fbSize2d: ElabVec2[Int] = {
  //  //ret = blank()
  //  //ret.x, ret.y = htiming.visib(), vtiming.visib()
  //  //return ret
  //  return ElabVec2(htiming.visib, vtiming.visib)
  //}
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
  //val rPopValid = Reg(Bool()) init(False)
  //rPopValid := push.valid
  //pop.valid := rPopValid

	//rPastColOut = Splitrec(RgbColorLayt(
	//	CHAN_WIDTH=bus.OUT_CHAN_WIDTH(self.CHAN_WIDTH())
	//))
	val rPastColOut = Reg(Rgb(outRgbConfig))
	// Update `outp.pos` and `outp.frameCnt`
	//posPlus1 = {"x": outp.pos.x + 0x1, "y": outp.pos.y + 0x1}
	val posPlus1 = ElabVec2(
	  x=outp.pos.x + 0x1,
	  y=outp.pos.y + 0x1,
	)
	//val dbgPosPlus1ElemWidth = outp.pos.x.getWidth + 1
	//val dbgPosPlus1 = Vec2(UInt(dbgPosPlus1ElemWidth bits))
	//dbgPosPlus1.addAttribute("keep")
	//dbgPosPlus1.x := (Cat(outp.pos.x, U"1'b0") + 0x1).resized
	//dbgPosPlus1.y := (Cat(outp.pos.y, U"1'b0") + 0x1).resized

	// Perform dithering
	//dicol = Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH()))
	val dicol = Rgb(rgbConfig)
	//chanDelta
	//	= PATTERN()[outp.frameCnt]
	//		[Value.cast(outp.pos.y[0])][Value.cast(outp.pos.x[0])]

  val rOutpFrameCnt = Reg(UInt(ditherDeltaWidth bits)) init(0x0)
  val rOutpPos = Reg(coordT())
  rOutpPos.init(rOutpPos.getZero)
  val rOutpPastPos = Reg(coordT())
  rOutpPastPos.init(rOutpPastPos.getZero)
  outp.pos := rOutpPos
  outp.pastPos := rOutpPastPos
  //val rOutpPayload = Reg(LcvVideoDithererPopPayload(
  //  rgbConfig=outRgbConfig
  //))
  //rOutpPayload.init(rOutpPayload.getZero)
  //outp := outp
  outp.frameCnt := rOutpFrameCnt

  val chanDelta = (
    pattern
      (outp.frameCnt)
      (outp.pos.y(0 downto 0))
      (outp.pos.x(0 downto 0))
  )
	//colInPlusDelta
	//	= Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH() + 1))
	val colInPlusDelta = Rgb(RgbConfig(
    rWidth=rgbConfig.rWidth + 1,
    gWidth=rgbConfig.gWidth + 1,
    bWidth=rgbConfig.bWidth + 1,
  ))

	when (posPlus1.x < fbSize2d.x) {
		//m.d.sync += outp.pos.x := (posPlus1.x)
		//m.d.comb += [
    outp.nextPos.x := posPlus1.x
    outp.nextPos.y := outp.pos.y
		//]
	} otherwise { // when (posPlus1.x >= fbSize2d.x):
		////m.d.sync += outp.pos.x := (0x0)
		//m.d.comb +=
		outp.nextPos.x := 0x0
		when (posPlus1.y < fbSize2d.y) {
			//m.d.comb +=
			outp.nextPos.y := posPlus1.y
		} otherwise {
			//// when (posPlus1.y >= fbSize2d.y):
			//m.d.comb +=
			outp.nextPos.y := (0x0)

			// This wraps around to zero automatically due to
			// modular arithmetic, so we don't need another mux just
			// for this.
			//when (push.valid) 
			when (inpEn) {
				//m.d.sync += outp.frameCnt := (outp.frameCnt + 0x1)
				rOutpFrameCnt := outp.frameCnt + 0x1
			}
		}
	}

	//when (push.valid) 
	when (inpEn) {
		//m.d.sync += [
    rPastColOut := outp.col
    rOutpPastPos := rOutpPos
		//]

		//m.d.sync += [
    rOutpPos := outp.nextPos
		//]

		//m.d.comb += [
    colInPlusDelta.r := (inpCol.r + chanDelta).resized
    colInPlusDelta.g := (inpCol.g + chanDelta).resized
    colInPlusDelta.b := (inpCol.b + chanDelta).resized
		//]

		// Saturating arithmetic to prevent an artifact
		//when (colInPlusDelta.r
		//	[len(colInPlusDelta.r) - 1]):
		when (colInPlusDelta.r.msb) {
			//m.d.comb += dicol.r := (-1)
			dicol.r := (default -> True)
		} otherwise {
			//m.d.comb += dicol.r := (colInPlusDelta.r
			//	[:len(dicol.r)])
			dicol.r := colInPlusDelta.r(dicol.r.bitsRange)
		}
		//m.d.comb += dicol.r := (colInPlusDelta.r
		//	[:len(dicol.r)])

		when (colInPlusDelta.g.msb) {
			//m.d.comb += dicol.g := (-1)
			dicol.g := (default -> True)
		} otherwise {
			//m.d.comb += dicol.g := (colInPlusDelta.g
			//	[:len(dicol.g)])
			dicol.g := colInPlusDelta.g(dicol.g.bitsRange)
		}
		//m.d.comb += dicol.g := (colInPlusDelta.g
		//	[:len(dicol.g)])

		when (colInPlusDelta.b.msb) {
			//m.d.comb += dicol.b := (-1)
			dicol.b := (default -> True)
		} otherwise {
			//m.d.comb += dicol.b := (colInPlusDelta.b
			//	[:len(dicol.b)])
			dicol.b := colInPlusDelta.b(dicol.b.bitsRange)
		}
		//m.d.comb += dicol.b := (colInPlusDelta.b
		//	[:len(dicol.b)])

		//m.d.comb += [
    //dicol.r := (COL_IN_PLUS_DELTA["r"]),
    //dicol.g := (COL_IN_PLUS_DELTA["g"]),
    //dicol.b := (COL_IN_PLUS_DELTA["b"]),

    //outp.col.r := (dicol.r[chan_width_delta():]),
    //outp.col.g := (dicol.g[chan_width_delta():]),
    //outp.col.b := (dicol.b[chan_width_delta():]),
    outp.col.r := dicol.r(dicol.r.high downto chanWidthDelta)
    outp.col.g := dicol.g(dicol.g.high downto chanWidthDelta)
    outp.col.b := dicol.b(dicol.b.high downto chanWidthDelta)
    //outp.col.r := (dicol.r[bus.CHAN_WIDTH_DELTA():]),
    //outp.col.g := (-1),
    //outp.col.b := (dicol.b[bus.CHAN_WIDTH_DELTA():]),
		//]
	} otherwise { // when (~push.valid):
		//m.d.comb += [
    //colInPlusDelta := 0x0
    colInPlusDelta := colInPlusDelta.getZero
    //dicol := 0x0
    dicol := dicol.getZero
    outp.col := rPastColOut
    //outp.nextPos := rOutpPos
		//]
	}
  //--------
}
//case class LcvVideoDitherer(
//  //fbSize2d: ElabVec2[Int],
//  rgbConfig: RgbConfig,
//  //vgaTimingInfo: LcvVgaTimingInfo,
//  fbSize2d: ElabVec2[Int],
//) extends Component {
//  //--------
//  val io = LcvVideoDithererIo(rgbConfig=rgbConfig)
//  val push = io.push
//  val pop = io.pop
//  //--------
//  def outRgbConfig: RgbConfig = io.outRgbConfig
//  def ditherDeltaWidth: Int = io.ditherDeltaWidth
//  def patVal(value: Int): UInt = {
//    //return Const(val, self.bus().DITHER_DELTA_WIDTH())
//    return U(f"$ditherDeltaWidth'd$value")
//  }
//  //def htiming: LcvVgaTimingHv = {
//  //  return vgaTimingInfo.htiming
//  //}
//  //def vtiming: LcvVgaTimingHv = {
//  //  return vgaTimingInfo.vtiming
//  //}
//  //def fbSize2d: ElabVec2[Int] = {
//  //  //ret = blank()
//  //  //ret.x, ret.y = htiming.visib(), vtiming.visib()
//  //  //return ret
//  //  return ElabVec2(htiming.visib, vtiming.visib)
//  //}
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
//  val fifoDepth = 4
//  val fifo = AsyncReadFifo(
//    //dataType=Rgb(rgbConfig),
//    dataType=LcvVideoDithererPopPayload(rgbConfig=outRgbConfig),
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
//  //val loc = new Area {
//  //past_col_out = Splitrec(RgbColorLayt(
//  //  CHAN_WIDTH=bus.OUT_CHAN_WIDTH(self.CHAN_WIDTH())
//  //))
//  val rPastTempPayload = Reg(LcvVideoDithererPopPayload(
//    rgbConfig=outRgbConfig
//  ))
//  rPastTempPayload.init(rPastTempPayload.getZero)
//  when (push.fire) {
//    rPastTempPayload := tempPayload
//  }
//  // Update `outp.pos` and `outp.frame_cnt`
//  //POS_PLUS_1 = {"x": outp.pos.x + 0x1, "y": outp.pos.y + 0x1}
//  //val posPlus1 = ElabVec2(
//  //  x=io.outpPayload.pos.x + 0x1,
//  //  y=io.outpPayload.pos.y + 0x1
//  //)
//  val posPlus1 = LcvVideoDithererPopPayloadNoCol.coordT()
//  posPlus1.x := io.outpPayload.pos.x + 0x1
//  posPlus1.y := io.outpPayload.pos.y + 0x1
//
//  // Perform dithering
//  //dicol = Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH()))
//  val dicol = Rgb(rgbConfig)
//  //CHAN_DELTA 
//  //  = pattern(io.outpPayload.frame_cnt)
//  //    [Value.cast(outp.pos.y[0])][Value.cast(outp.pos.x[0])]
//  val chanDelta = (
//    pattern
//      (rPastTempPayload.frameCnt)
//      (rPastTempPayload.pos.y(0 downto 0))
//      (rPastTempPayload.pos.x(0 downto 0))
//  )
//  //col_in_plus_delta \
//  //  = Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH() + 1))
//  val colInPlusDelta = Rgb(RgbConfig(
//    rWidth=rgbConfig.rWidth + 1,
//    gWidth=rgbConfig.gWidth + 1,
//    bWidth=rgbConfig.bWidth + 1,
//  ))
//  val rPastTempPayloadPosY = Reg(
//    LcvVideoDithererPopPayloadNoCol.coordElemT()
//  )
//  rPastTempPayloadPosY.init(rPastTempPayloadPosY.getZero)
//  rPastTempPayloadPosY := tempPayload.pos.y
//  when (posPlus1.x < fbSize2d.x) {
//    //m.d.sync += tempPayload.pos.x := (posPlus1.x)
//    //m.d.comb += [
//      io.outpNextPos.x := posPlus1.x
//      io.outpNextPos.y := rPastTempPayloadPosY
//    //]
//  } otherwise { // when (posPlus1.x >= fbSize2d.x):
//    //m.d.sync += tempPayload.pos.x := (0x0)
//    //m.d.comb +=
//    io.outpNextPos.x := 0x0
//    when (posPlus1.y < fbSize2d.y) {
//      //m.d.comb += 
//      io.outpNextPos.y := posPlus1.y
//    } otherwise {
//      // when (posPlus1.y >= fbSize2d.y):
//      //m.d.comb +=
//      io.outpNextPos.y := 0x0
//
//      // This wraps around to zero automatically due to
//      // modular arithmetic, so we don't need another mux just
//      // for this.
//      //when (inp.en) {
//      //  //m.d.sync += 
//      //  io.frameCnt := tempPayload.frameCnt + 0x1
//      //}
//    }
//  }
//  tempPayload.pos := io.outpNextPos
//  // This wraps around to zero automatically due to
//  // modular arithmetic, so we don't need another mux just
//  // for this.
//  tempPayload.frameCnt := rPastTempPayload.frameCnt + 0x1
//  tempPayload.pastPos := rPastTempPayload.pos
//  //when (io.en)
//  when (push.fire) {
//    ////m.d.sync += [
//    ////rPastColOut := io.outpCol
//    ////io.misc.pastPos := (io.misc.pos)
//    ////rPastPos := io.misc.pos
//    //tempPayload.pastPos := rPastTempPayload.pos
//    ////]
//
//    ////m.d.sync += [
//    ////io.misc.pos := (io.misc.nextPos)
//    ////rPos := io.misc.nextPos
//    //tempPayload.pos := tempPayload.nextPos
//    ////]
//
//    //m.d.comb += [
//    colInPlusDelta.r := (io.inpCol.r + chanDelta).resized
//    colInPlusDelta.g := (io.inpCol.g + chanDelta).resized
//    colInPlusDelta.b := (io.inpCol.b + chanDelta).resized
//    //]
//
//    // Saturating arithmetic to prevent an artifact
//    when (colInPlusDelta.r.msb
//      //[len(colInPlusDelta.r) - 1]
//    ) {
//      //m.d.comb += dicol.r := (-1)
//      //dicol.r := -1
//      dicol.r := (default -> True)
//    } otherwise {
//      //m.d.comb += dicol.r := (colInPlusDelta.r
//      //  [:len(dicol.r)])
//      dicol.r := colInPlusDelta.r(dicol.r.bitsRange)
//    }
//    //m.d.comb += dicol.r := (colInPlusDelta.r
//    //  [:len(dicol.r)])
//
//    when (colInPlusDelta.g.msb
//      //[len(colInPlusDelta.g) - 1]):
//    ) {
//      //m.d.comb += dicol.g := (-1)
//      //dicol.g := -1
//      dicol.g := (default -> True)
//    } otherwise {
//      //m.d.comb += dicol.g := (colInPlusDelta.g
//      //  [:len(dicol.g)])
//      dicol.g := colInPlusDelta.g(dicol.g.bitsRange)
//    }
//    //m.d.comb += dicol.g := (colInPlusDelta.g
//    //  [:len(dicol.g)])
//
//    when (colInPlusDelta.b.msb
//      //[len(colInPlusDelta.b) - 1]):
//    ) {
//      //m.d.comb += dicol.b := (-1)
//      //dicol.b := -1
//      dicol.b := (default -> True)
//    } otherwise {
//      //m.d.comb += dicol.b := (colInPlusDelta.b
//      //  [:len(dicol.b)])
//      dicol.b := colInPlusDelta.b(dicol.b.bitsRange)
//    }
//    //m.d.comb += dicol.b := (colInPlusDelta.b
//    //  [:len(dicol.b)])
//
//    ////m.d.comb += [
//    ////dicol.r := (COL_IN_PLUS_DELTA["r"]),
//    ////dicol.g := (COL_IN_PLUS_DELTA["g"]),
//    ////dicol.b := (COL_IN_PLUS_DELTA["b"]),
//
//    //io.misc.col.r := (dicol.r[bus.CHAN_WIDTH_DELTA():]),
//    //io.misc.col.g := (dicol.g[bus.CHAN_WIDTH_DELTA():]),
//    //io.misc.col.b := (dicol.b[bus.CHAN_WIDTH_DELTA():]),
//    tempPayload.col.r := dicol.r(
//      dicol.r.high downto io.chanWidthDelta
//    ).resized
//    tempPayload.col.g := dicol.g(
//      dicol.g.high downto io.chanWidthDelta
//    ).resized
//    tempPayload.col.b := dicol.b(
//      dicol.b.high downto io.chanWidthDelta
//    ).resized
//    ////io.misc.col.r := (dicol.r[bus.CHAN_WIDTH_DELTA():]),
//    ////io.misc.col.g := (-1),
//    ////io.misc.col.b := (dicol.b[bus.CHAN_WIDTH_DELTA():]),
//    ////]
//
//  } otherwise { // when (~push.fire)
//    //m.d.comb += [
//    //colInPlusDelta := 0x0
//    colInPlusDelta := colInPlusDelta.getZero
//    //dicol := 0x0
//    dicol := dicol.getZero
//    //io.misc.col := rPastColOut
//    //tempPayload.col := rPastColOut
//    tempPayload.col := rPastTempPayload.col
//    //io.misc.nextPos := io.misc.pos
//    //tempPayload.nextPos := rPastTempPayload.nextPos
//    //]
//  }
//  //}
//  //--------
//}
