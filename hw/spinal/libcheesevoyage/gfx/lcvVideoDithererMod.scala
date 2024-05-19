package libcheesevoyage.gfx
import libcheesevoyage.general.DualTypeNumVec2
import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2
import libcheesevoyage.general.FifoIo
import libcheesevoyage.general.AsyncReadFifo
import libcheesevoyage.general.PsbIoParentData
import libcheesevoyage.general.PipeSkidBufIo
import libcheesevoyage.general.PipeSkidBuf

import spinal.core._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer
import scala.math._

object LcvVideoDithererInfo {
  def ditherDeltaWidth: Int = log2Up(4)
  //def coordElemT(): UInt = UInt(16 bits)
  //def coordElemT(
  //  vgaTimingInfo: LcvVgaTimingInfo,
  //): UInt = LcvVgaCtrlMiscIo.coordElemT(
  //  vgaTimingInfo.
  //)
  def coordT(
    //vgaTimingInfo: LcvVgaTimingInfo,
    fbSize2d: ElabVec2[Int]
  ): DualTypeNumVec2[UInt, UInt] = LcvVideoPosInfo.coordT(
    //vgaTimingInfo=vgaTimingInfo
    someSize2d=fbSize2d,
  )
  //DualTypeVec2(
  //  //coordElemT(vgaTimingInfo=vgaTimingInfo)
  //)

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
case class LcvVideoDithererInfo(
  //rgbConfig: RgbConfig
  //vgaTimingInfo: LcvVgaTimingInfo,
  fbSize2d: ElabVec2[Int]
) extends Bundle {
  //--------
  //val outpCol = out(Rgb(rgbConfig))

  val posInfo = LcvVideoPosInfo(
    someSize2d=fbSize2d,
  )

  ////val col = Rgb(rgbConfig)
  val frameCnt = UInt(ditherDeltaWidth bits)
  ////val changedFrameCnt = Bool()
  ////val posPlus1 = coordT()
  ////val posPlus2 = coordT()
  //val posWillOverflow = Vec2(Bool())
  //val nextPos = coordT()
  //val pos = coordT()
  //val pastPos = coordT()
  //val changingRow = Bool()

  def posWillOverflow = posInfo.posWillOverflow
  def nextPos = posInfo.nextPos
  def pos = posInfo.pos
  def pastPos = posInfo.pastPos
  def changingRow = posInfo.changingRow
  //--------
  def ditherDeltaWidth = LcvVideoDithererInfo.ditherDeltaWidth
  def coordT() = LcvVideoDithererInfo.coordT(
    fbSize2d=fbSize2d,
  )
  //--------
}
case class LcvVideoDithererPopPayload(
  rgbConfig: RgbConfig,
  //outRgbConfig: RgbConfig
) extends Bundle {
  //val inpCol = Rgb(c=rgbConfig)
  //val outpCol = Rgb(c=outRgbConfig)
  //val outpPastCol = Rgb(c=outRgbConfig)
  //val inpCol = Rgb(c=rgbConfig)
  //val outpCol = Rgb(c=outRgbConfig)
  val col = Rgb(c=outRgbConfig)
  def outRgbConfig = LcvVideoDithererInfo.outRgbConfig(
    rgbConfig=rgbConfig
  )
}

object LcvVideoDithererIo {
  def outChanWidth(
    //rgbConfig: RgbConfig,
    chanWidth: Int,
  ): Int = {
    LcvVideoDithererInfo.outChanWidth(chanWidth=chanWidth)
  }

  def outRChanWidth(
    rgbConfig: RgbConfig
  ): Int = {
    LcvVideoDithererInfo.outRChanWidth(rgbConfig=rgbConfig)
  }
  def outGChanWidth(
    rgbConfig: RgbConfig
  ): Int = {
    LcvVideoDithererInfo.outGChanWidth(rgbConfig=rgbConfig)
  }
  def outBChanWidth(
    rgbConfig: RgbConfig
  ): Int = {
    LcvVideoDithererInfo.outBChanWidth(rgbConfig=rgbConfig)
  }
  def outRgbConfig(
    rgbConfig: RgbConfig
  ): RgbConfig = {
    LcvVideoDithererInfo.outRgbConfig(rgbConfig=rgbConfig)
  }

  def chanWidthDelta: Int = LcvVideoDithererInfo.chanWidthDelta
}

case class LcvVideoDithererIo(
  rgbConfig: RgbConfig,
  fbSize2d: ElabVec2[Int],
  //vgaTimingInfo: LcvVgaTimingInfo,
) extends Bundle with IMasterSlave {
  val push = slave Stream(Rgb(rgbConfig))
  //val push = slave Stream(LcvVideoDithererPopPayload(
  //  //outRgbConfig=outRgbConfig
  //  rgbConfig=rgbConfig
  //))
  ////val push = slave Flow(Rgb(rgbConfig))
  //val pop = master Stream(LcvVideoDithererPopPayload(rgbConfig=rgbConfig))
  val pop = master Stream(LcvVideoDithererPopPayload(rgbConfig=rgbConfig))
  val info = out(LcvVideoDithererInfo(fbSize2d=fbSize2d))
  //val pop = master Stream(LcvVideoDithererPopPayload(
  //  //outRgbConfig=outRgbConfig
  //  rgbConfig=rgbConfig
  //))
  ////val pop = master Flow(LcvVideoDithererPopPayload(
  ////  rgbConfig=outRgbConfig
  ////))
  ////val outpPayload = out(LcvVideoDithererPopPayload(
  ////  rgbConfig=outRgbConfig
  ////))
  ////val outp = out(LcvVideoDithererPopPayload(
  ////  rgbConfig=outRgbConfig
  ////))
  ////val inpEn = push.valid

  //val sbIo = PipeSkidBufIo(
  //  dataType=LcvVideoDithererPopPayload(rgbConfig=rgbConfig),
  //  optIncludeBusy=false,
  //)

  ////val inpEn = push.fire

  ////val inpValid = push.valid
  ////val inpCol = push.payload
  ////val outpCol = pop.payload.col
  //val inpValid = sbIo.prev.valid
  //val inpCol = sbIo.prev.payload.inpCol
  //val outpCol = sbIo.next.payload.outpCol
  val inpCol = push.payload
  val outpCol = pop.payload.col

  //val outpPayload = pop.payload
  //val outpCol = outpPayload.col
  //val outpNextPos = out(LcvVideoDithererPopPayloadNoCol.coordT())
  //--------
  def asMaster(): Unit = {
    master(push)
    //in(outpPayload)
    slave(pop)
    in(info)
    //in(outp)
    //in(outpNextPos)
  }
  //def asSlave(): Unit = {
  //  slave(push)
  //  master(pop)
  //}
  //--------
  //def ditherDeltaWidth(): Int = log2Up(4)
  def ditherDeltaWidth = info.ditherDeltaWidth
  //def ditherDeltaWidth = sbIo.next.payload.ditherDeltaWidth

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
  def coordT() = info.coordT()
  //def coordT() = sbIo.next.payload.coordT()
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
  val io = LcvVideoDithererIo(
    rgbConfig=rgbConfig,
    fbSize2d=fbSize2d,
  )
  val push = io.push
  val pop = io.pop
  //val inpEn = io.inpEn

  //val inpEn = push.fire
  //val inpEn = io.sbIo.prev.fire
  val inpCol = io.inpCol
  val info = io.info

  //val outp = io.outp
  //val outp = io.outp
  //val outp = pop.payload
  //--------
  def outRgbConfig: RgbConfig = io.outRgbConfig
  def ditherDeltaWidth: Int = io.ditherDeltaWidth
  def patVal(value: Int): UInt = {
    //return Const(val, self.bus().DITHER_DELTA_WIDTH())
    return U(f"$ditherDeltaWidth'd$value")
  }
  def chanWidthDelta: Int = LcvVideoDithererIo.chanWidthDelta
  def coordT() = LcvVideoDithererInfo.coordT(
    fbSize2d=fbSize2d
  )
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
  val skidBuf = PipeSkidBuf(
    dataType=LcvVideoDithererPopPayload(rgbConfig=rgbConfig),
    optIncludeBusy=false,
    //optIncludeNextBusy=false,
    //optIncludePrevBusy=false,
    optPassthrough=false,
    optUseOldCode=false,
    //optUseOldCode=true,
  )
  val sbIo = skidBuf.io
  //val sbIoPrev = sbIo.prev
  //val sbIoNext = sbIo.next

  //sbIo.prev.valid := push.valid
  //sbIo.prev.payload.inpCol := push.payload
  //push.ready := sbIo.prev.ready

  //pop << sbIo.next
  //val outp = cloneOf(sbIo.prev.payload)
  val outp = sbIo.prev.payload
  val rOutp = Reg(cloneOf(sbIo.prev.payload))
  rOutp.init(rOutp.getZero)
  //val rInfo = Reg(LcvVideoDithererInfo())
  //rInfo.init(rInfo.getZero)
  //val tempOutp = sbIo.next.payload
  val tempOutp = cloneOf(sbIo.next.payload)
  //sbIo.next.payload := tempOutp
  tempOutp := sbIo.next.payload

  //sbIo.prev.valid := push.valid
  //sbIo.prev.payload := outp
  //push.ready := sbIo.prev.ready

  //pop.valid := sbIo.next.valid
  //pop.payload := sbIo.next.payload
  //sbIo.next.ready := pop.ready

  sbIo.misc := sbIo.misc.getZero

  sbIo.connectParentStreams(
    push=push,
    pop=pop,
  )(
    (sbIoPrevPayload, pushPayload) => {
    }
  )
  //--------
  // Perform dithering
  //dicol = Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH()))
  val dicol = Rgb(rgbConfig)

  val chanDelta = (
    pattern
      (info.frameCnt)
      (info.pos.y(0 downto 0))
      (info.pos.x(0 downto 0))
  )
  //colInPlusDelta
  //  = Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH() + 1))
  val plusDeltaRgbConfig = RgbConfig(
    rWidth=rgbConfig.rWidth + 1,
    gWidth=rgbConfig.gWidth + 1,
    bWidth=rgbConfig.bWidth + 1,
  )
  val colInPlusDelta = Rgb(plusDeltaRgbConfig)
  val tempColInPlusDelta = Rgb(plusDeltaRgbConfig)
  tempColInPlusDelta.r := inpCol.r.resized
  tempColInPlusDelta.g := inpCol.g.resized
  tempColInPlusDelta.b := inpCol.b.resized

  val rFrameCnt = Reg(cloneOf(info.frameCnt))
  rFrameCnt.init(rFrameCnt.getZero)
  info.frameCnt := rFrameCnt

  val vidCalcPos = LcvVideoCalcPos(someSize2d=fbSize2d)
  vidCalcPos.io.en := sbIo.next.fire
  info.posInfo := vidCalcPos.io.info


  //when (sbIo.next.valid) {
  //rInfo := info
  //info := rInfo
  rOutp := outp
  //}
  when (clockDomain.isResetActive) {
    //info.nextPos := info.nextPos.getZero
    outp := outp.getZero
  } otherwise {
    
    when (sbIo.next.fire) {
      //--------
      rFrameCnt := rFrameCnt + 0x1
      //rPos := info.nextPos
      //rPastPos := rPos

      outp.col.r := dicol.r(dicol.r.high downto chanWidthDelta)
      outp.col.g := dicol.g(dicol.g.high downto chanWidthDelta)
      outp.col.b := dicol.b(dicol.b.high downto chanWidthDelta)
    } otherwise { // when (!sbIo.next.fire)
      //info.posWillOverflow := rPastOutp.
      //info := rInfo
      //info.nextPos := info.pos
      outp := rOutp
    }

    //outp.posPlus1.x := tempOutp.posPlus1.x + 1
    //outp.posPlus1.y := tempOutp.posPlus1.y + 1
    //outp.posPlus2.x := tempOutp.posPlus2.x + 1
    //outp.posPlus2.y := tempOutp.posPlus2.y + 1
    //when (tempOutp.posPlus1.x =/= fbSize2d.x) 

    //when (tempOutp.pos.x =/= fbSize2d.x - 1) {
    //  //m.d.sync += outp.pos.x := (posPlus1.x)
    //  //m.d.comb += [
    //  //outp.nextPos.x := tempOutp.posPlus1.x
    //  //outp.nextPos.y := tempOutp.pos.y
    //  outp.nextPos.x := tempOutp.pos.x + 1
    //  //]
    //} otherwise { // when (posPlus1.x >= fbSize2d.x):
    //  ////m.d.sync += outp.pos.x := (0x0)
    //  //m.d.comb +=
    //  outp.nextPos.x := 0x0
    //  //when (tempOutp.posPlus1.y < fbSize2d.y) 
    //  when (tempOutp.pos.y =/= fbSize2d.y - 1) {
    //    //m.d.comb +=
    //    //outp.nextPos.y := tempOutp.posPlus1.y
    //    outp.nextPos.y := tempOutp.pos.y + 1
    //  } otherwise {
    //    //// when (posPlus1.y >= fbSize2d.y):
    //    //m.d.comb +=
    //    outp.nextPos.y := 0x0

    //    // This wraps around to zero automatically due to
    //    // modular arithmetic, so we don't need another mux just
    //    // for this.
    //    //when (push.valid) 
    //    //when (inpEn) {
    //    //  //m.d.sync += outp.frameCnt := (outp.frameCnt + 0x1)
    //    //  rOutpFrameCnt := outp.frameCnt + 0x1
    //    //}
    //  }
    //}
  }

  //when (!tempOutp.changingRow) {
  //  //outp.nextPos.x := posPlus1.x
  //  outp.nextPos.x := tempOutp.posPlus1.x
  //  outp.nextPos.y := tempOutp.pos.y
  //} otherwise { // when (tempOutp.changingRow)
  //  outp.nextPos.x := 0
  //  when (!rPosYPlus2IsHeight) {
  //    outp.nextPos.y := rPosPlus2.y
  //  } otherwise { // when (rPosYPlus2IsHeight)
  //    outp.nextPos.y := 0x0
  //    when (inpEn) {
  //      rOutpFrameCnt := outp.frameCnt + 0x1
  //    }
  //  }
  //}

  //val rPushReady = Reg(Bool()) init(False)
  //io.push.ready := True
  //when (io.push.valid) {
  //  rPushReady := True
  //}
  //when (push.valid) 
  //when (inpEn) 
  //when (push.valid) {
    ////m.d.sync += [
    ////rPastColOut := outp.col
    ////rOutpPastPos := rOutpPos
    //outp.pastPos := tempOutp.pos
    ////]

    ////m.d.sync += [
    ////rOutpPos := outp.nextPos
    //outp.pos := tempOutp.nextPos
    ////]

    //m.d.comb += [
    //colInPlusDelta.r := (inpCol.r + chanDelta).resized
    //colInPlusDelta.g := (inpCol.g + chanDelta).resized
    //colInPlusDelta.b := (inpCol.b + chanDelta).resized
    colInPlusDelta.r := tempColInPlusDelta.r + chanDelta.resized
    colInPlusDelta.g := tempColInPlusDelta.g + chanDelta.resized
    colInPlusDelta.b := tempColInPlusDelta.b + chanDelta.resized
    //]

    // Saturating arithmetic to prevent an artifact
    //when (colInPlusDelta.r
    //  [len(colInPlusDelta.r) - 1]):
    when (colInPlusDelta.r.msb) {
      //m.d.comb += dicol.r := (-1)
      dicol.r := (default -> True)
    } otherwise {
      //m.d.comb += dicol.r := (colInPlusDelta.r
      //  [:len(dicol.r)])
      dicol.r := colInPlusDelta.r(dicol.r.bitsRange)
    }
    //m.d.comb += dicol.r := (colInPlusDelta.r
    //  [:len(dicol.r)])

    when (colInPlusDelta.g.msb) {
      //m.d.comb += dicol.g := (-1)
      dicol.g := (default -> True)
    } otherwise {
      //m.d.comb += dicol.g := (colInPlusDelta.g
      //  [:len(dicol.g)])
      dicol.g := colInPlusDelta.g(dicol.g.bitsRange)
    }
    //m.d.comb += dicol.g := (colInPlusDelta.g
    //  [:len(dicol.g)])

    when (colInPlusDelta.b.msb) {
      //m.d.comb += dicol.b := (-1)
      dicol.b := (default -> True)
    } otherwise {
      //m.d.comb += dicol.b := (colInPlusDelta.b
      //  [:len(dicol.b)])
      dicol.b := colInPlusDelta.b(dicol.b.bitsRange)
    }
    //m.d.comb += dicol.b := (colInPlusDelta.b
    //  [:len(dicol.b)])

    //m.d.comb += [
    //dicol.r := (COL_IN_PLUS_DELTA["r"]),
    //dicol.g := (COL_IN_PLUS_DELTA["g"]),
    //dicol.b := (COL_IN_PLUS_DELTA["b"]),

    ////outp.col.r := (dicol.r[chan_width_delta():]),
    ////outp.col.g := (dicol.g[chan_width_delta():]),
    ////outp.col.b := (dicol.b[chan_width_delta():]),
    //outp.col.r := dicol.r(dicol.r.high downto chanWidthDelta)
    //outp.col.g := dicol.g(dicol.g.high downto chanWidthDelta)
    //outp.col.b := dicol.b(dicol.b.high downto chanWidthDelta)
    ////outp.col.r := (dicol.r[bus.CHAN_WIDTH_DELTA():]),
    ////outp.col.g := (-1),
    ////outp.col.b := (dicol.b[bus.CHAN_WIDTH_DELTA():]),
    ////]
  //} otherwise { // when (~push.valid):
  //  //m.d.comb += [
  //  //colInPlusDelta := 0x0
  //  colInPlusDelta := colInPlusDelta.getZero
  //  //dicol := 0x0
  //  dicol := dicol.getZero
  //  outp.col := rPastColOut
  //  //outp.nextPos := rOutpPos
  //  //]
  //}
  //--------
}
