package libcheesevoyage.gfx
import libcheesevoyage.general.PipeSkidBufIo
import libcheesevoyage.general.PipeSkidBuf
import libcheesevoyage.general.PsbIoParentData

import spinal.core._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer
import scala.math._


case class LcvVgaGradientIo(
  clkRate: Double,
  rgbConfig: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  ctrlFifoDepth: Int,
) extends Bundle //with IMasterSlave
{
  //--------
 // val vgaCtrlIo = master(LcvVgaCtrlIo(rgbConfig=rgbConfig))
 // val vidDithIo = master(LcvVideoDithererIo(rgbConfig=rgbConfig))
  val vgaCtrlIo = master(
    LcvVgaCtrlIo(
      clkRate=clkRate,
      rgbConfig=outRgbConfig,
      vgaTimingInfo=vgaTimingInfo,
      fifoDepth=ctrlFifoDepth,
    )
  )
  val vidDithIo = master(LcvVideoDithererIo(rgbConfig=rgbConfig))
  //--------
  //def asMaster(): Unit = {
  //  master(vgaCtrlIo)
  //  slave(vidDithIo)
  //}
  //def asSlave(): Unit = {
  //  slave(vgaCtrlIo)
  //  master(vidDithIo)
  //}
  //--------
  def outRgbConfig = LcvVideoDithererIo.outRgbConfig(rgbConfig=rgbConfig)
  //--------
}
case class LcvVgaGradient(
  clkRate: Double,
  rgbConfig: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  ctrlFifoDepth: Int,
) extends Component {
  //--------
  //val io = slave(LcvVgaGradientIo(rgbConfig=rgbConfig))
  val io = LcvVgaGradientIo(
    clkRate=clkRate,
    rgbConfig=rgbConfig,
    vgaTimingInfo=vgaTimingInfo,
    ctrlFifoDepth=ctrlFifoDepth,
  )
  val ctrlIo = io.vgaCtrlIo
  val dithIo = io.vidDithIo
  //--------
  ctrlIo.en := True
  //--------
  //val skidBuf = PipeSkidBuf(
  //  dataType=Rgb(io.outRgbConfig),
  //  optIncludeBusy=false,
  //  optPassthrough=false,
  //)
  //val sbIo = skidBuf.io
  ////val sbPrevFire = sbIo.prev.fire
  ////sbIo.prev.valid := True
  ////val sbPrevValid = sbIo.prev.valid
  ////val sbPrevReady = sbIo.prev.ready
  ////val sbNextValid = sbIo.next.valid
  ////val sbNextReady = sbIo.next.ready
  //ctrlIo.push << sbIo.next
  //val tempPush = new Stream(Rgb(io.outRgbConfig))
  //ctrlIo.push << tempPush
  //val rCtrlPushValid = Reg(Bool()) init(True)
  //ctrlIo.push.valid := rCtrlPushValid
  //ctrlIo.push.valid := True

  //val rCtrlPushValid = Reg(Bool()) init(False)
  //ctrlIo.push.valid := rCtrlPushValid
  //ctrlIo.push.valid := True

  //val rDithPushValid = Reg(Bool()) init(False)
  //dithIo.push.valid := rDithPushValid
  //rCtrlPushValid := rDithPushValid
  val dithPushValid = dithIo.push.valid
  val rDithPushValid = Reg(Bool()) init(True)
  dithPushValid := rDithPushValid
  //dithPushValid := True
  //rCtrlPushValid := dithPushValid
  //--------
  //val col = dithIo.inpCol
  //val rPastDithCol = Reg(Rgb(rgbConfig))
  //rPastDithCol.init(rPastDithCol.getZero)
  val rDithCol = Reg(Rgb(rgbConfig))
  val initDithColR = UInt(rgbConfig.rWidth bits)
  initDithColR := (default -> True)
  rDithCol.r.init(initDithColR)
  rDithCol.g.init(rDithCol.g.getZero)
  rDithCol.b.init(rDithCol.b.getZero)
  dithIo.inpCol := rDithCol
  rDithCol.r := initDithColR
  rDithCol.g := 0x0
  rDithCol.b := 0x0

  //val dithCol = dithIo.inpCol
  ////rPastDithCol := dithCol
  //val rDithCol = Reg(Rgb(rgbConfig))
  //rDithCol.init(rDithCol.getZero)
  //dithCol := rDithCol
  ////dithIo.inpCol := rDithCol
  ////val rDithCol = dithIo.inpCol
  ////rDithCol := rDithCol

  //ctrlIo.push.payload := dithIo.outp.col
  //--------
  //val rCtrlPushValid = Reg(Bool()) init(False)
  //ctrlIo.push.valid := rCtrlPushValid
  ctrlIo.push.valid := True
  val rDbgPhysCol = Reg(Rgb(io.outRgbConfig))
  rDbgPhysCol.init(rDbgPhysCol.getZero)
  ctrlIo.push.payload := rDbgPhysCol
  //--------
  // Gradient
  def cpp = LcvVgaCtrl.cpp(
    clkRate=clkRate,
    vgaTimingInfo=vgaTimingInfo,
  )
  def resetDbgPhysCol(): Unit = {
    rDbgPhysCol.r := (default -> True)
    rDbgPhysCol.g := 0x0
    rDbgPhysCol.b := 0x0
  }
  def incrDbgPhysCol(): Unit = {
    rDbgPhysCol.r := (default -> True)
    rDbgPhysCol.g := rDbgPhysCol.g + 1
    rDbgPhysCol.b := 0x0
  }
  //val pastPastVisib = Reg(Bool()) init(False)
  //pastPastVisib := ctrlIo.misc.pastVisib

  val rPosX = Reg(UInt(ctrlIo.misc.drawPos.x.getWidth bits)) init(0x0)
  when (ctrlIo.push.fire) {
    when (rPosX === 0x0) {
      resetDbgPhysCol()
    } otherwise {
      incrDbgPhysCol()
    }
    rPosX := rPosX + 1
  }

  //when (
  //  //(ctrlIo.misc.pastVisib && !pastPastVisib)
  //  //|| !ctrlIo.misc.pastVisib
  //  (ctrlIo.misc.visib && !ctrlIo.misc.pastVisib)
  //  || (!ctrlIo.misc.visib)
  //) {
  //  //rCtrlPushValid := False
  //  resetDbgPhysCol()
  //} otherwise {
  //  //when (!rCtrlPushValid) {
  //  //  rCtrlPushValid := True
  //  //  //when (
  //  //  //  ctrlIo.misc.fifoPopReady
  //  //  //) {
  //  //  //  incrDbgPhysCol
  //  //  //}
  //  //} otherwise { // when (rCtrlPushValid)
  //  //}
  //  //when (!rCtrlPushValid) {
  //  //  rCtrlPushValid := True
  //  //  incrDbgPhysCol()
  //  //} otherwise {
  //  //  when (ctrlIo.push.fire) {
  //  //    rCtrlPushValid := False
  //  //  }
  //  //}
  //  when (ctrlIo.push.fire) {
  //    incrDbgPhysCol()
  //  }

  //  //when (!rCtrlPushValid) {
  //  //  rCtrlPushValid
  //  //} otherwise {
  //  //}
  //  //when (!ctrlIo.push.fire) {
  //  //  rCtrlPushValid := True
  //  //} otherwise { // when (ctrlIo.push.fire)
  //  //  rCtrlPushValid := False
  //  //  //incrDbgPhysCol()
  //  //}
  //  //rCtrlPushValid := True
  //}

  //when (ctrlIo.push.fire) {
  //}
  //when (ctrlIo.misc.nextNextVisib && !ctrlIo.misc.nextVisib) {
  //}
  //when (ctrlIo.misc.visib && !ctrlIo.misc.pastVisib) {
  //}

  //when (ctrlIo.push.fire) {
  //}
  //when (ctrlIo.push.fire) {
  //}
  //when (ctrlIo.misc.nextVisib) {
  //  when (!ctrlIo.misc.visib) {
  //    //rDbgPhysCol := rDbgPhysCol.getZero
  //    rDbgPhysCol.r := (default -> True)
  //    rDbgPhysCol.g := 0x0
  //    rDbgPhysCol.b := 0x0
  //  } elsewhen (ctrlIo.push.fire) {
  //    rDbgPhysCol.r := (default -> True)
  //    rDbgPhysCol.g := rDbgPhysCol.g + 1
  //    rDbgPhysCol.b := 0x0
  //  }
  //}

  //when (ctrlIo.misc.fifoAmountCanPop < cpp) 
  //val rDidFirstAssertCtrlValid = Reg(Bool()) init(False)
  //val didInit = Reg(Bool()) init(False)
  //when (
  //  //ctrlIo.push.fire || !rDidFirstAssertCtrlValid
  //  //&& ctrlIo.misc.fifoAmountCanPush > (ctrlFifoDepth - cpp)
  //  //ctrlIo.misc.nextPixelEn
  //  //!rCtrlPushValid
  //  //!ctrlIo.misc.fifoFull
  //  //ctrlIo.misc.pixelEn
  //  //&& (ctrlIo.misc.fifoAmountCanPop < 10)
  //  ctrlIo.push.fire
  //  //&& (
  //  //  ctrlIo.misc.fifoAmountCanPush > (ctrlFifoDepth - 4)
  //  //)
  //  //&& (ctrlIo.misc.fifoAmountCanPop < cpp)
  //) {
  //  //rDidFirstAssertCtrlValid := True
  //  //rCtrlPushValid := True
  //  //dithPushValid := True
  //  rDithPushValid := True
  //  //rCtrlPushValid := True
  //  rDithCol.r := (default -> True)
  //  //rDithCol.r := 0
  //  when (
  //    (dithIo.outp.pos.x === 0x0)
  //    || !didInit
  //  ) {
  //    didInit := True
  //    rDithCol.g := 0x0
  //  } otherwise { // when (dithIo.outp.pos > 0x0)
  //    //rDithCol.g := rPastDithCol.g + 1
  //    //rDithCol.g := 0x3
  //    rDithCol.g := rDithCol.g + 1
  //  }
  //  rDithCol.b := 0x0
  //} otherwise {
  //  rDithPushValid := False
  //  //when (rCtrlPushValid && ctrlIo.push.fire) {
  //  //  rCtrlPushValid := False
  //  //}
  //  //rCtrlPushValid := False
  //  //dithPushValid := False
  //  //rDithPushValid := True
  //  //dithCol := rPastDithCol
  //  //dithCol := dithCol
  //}
  //--------
}

//case class LcvVgaGradient(
//  rgbConfig: RgbConfig,
//  vgaTimingInfo: LcvVgaTimingInfo,
//) extends Component {
//  //--------
//  //val io = slave(LcvVgaGradientIo(rgbConfig=rgbConfig))
//  val io = LcvVgaGradientIo(
//    rgbConfig=rgbConfig,
//    vgaTimingInfo=vgaTimingInfo,
//  )
//  val ctrlIo = io.vgaCtrlIo
//  val dithIo = io.vidDithIo
//  //--------
//  //val col = dithIo.inpCol
//  //val tempSbIo = PipeSkidBufIo(
//  //  dataType=Rgb(rgbConfig),
//  //  optIncludeBusy=false,
//  //)
//  val skidBuf = PipeSkidBuf(
//    dataType=Rgb(rgbConfig),
//    optIncludeBusy=false,
//    optPassthrough=false,
//    //optTieIfwdValid=true,
//  )
//  val sbIo = skidBuf.io
//  val sbPrevFire = sbIo.prev.fire
//  sbIo.prev.valid := True
//  val col = sbIo.prev.payload
//  val rPastCol = Reg(Rgb(rgbConfig))
//  rPastCol.init(rPastCol.getZero)
//  //--------
//  dithIo.push << sbIo.next
//  val dithOutpCol = dithIo.outpCol
//
//  //val dithToCtrlStm = master Stream(Rgb(rgbConfig))
//  //dithToCtrlStm.valid := dithIo.pop.valid
//  //dithToCtrlStm.payload := dithOutpCol
//  //dithIo.pop.ready := dithToCtrlStm.ready
//
//  ctrlIo.push.valid := dithIo.pop.valid
//  ctrlIo.push.payload := dithOutpCol
//  dithIo.pop.ready := ctrlIo.push.ready
//  //--------
//  // Enable VGA signal output
//  ctrlIo.en := True
//  //--------
//  rPastCol := col
//  when (sbPrevFire) {
//    //m.d.sync += [
//    //  drbus.inp.buf.prep.eq(0b1),
//    //  dibus.inp.en.eq(0b1),
//    //]
//
//    //when (dithIo.outpPayload.nextPos.x === 0x0) 
//    when (dithIo.outpPayload.pos.x === 0x0) {
//      //m.d.sync += col.r.eq(0x0)
//      col.r := 0x0
//    } otherwise { // when (dithIo.outpPayload..pos.x > 0x0)
//      //m.d.sync += col.r.eq(col.r + 0x1)
//      col.r := rPastCol.r + 0x1
//    }
//    //m.d.sync += col.r.eq(col.r + 0x1)
//
//    //m.d.sync += col.g.eq(0x0)
//    //m.d.sync += col.b.eq(0x0)
//    col.g := 0x0
//    col.b := 0x0
//  } otherwise { // when (~sbPrevFire)
//    col := rPastCol
//  }
//  //col.r := (default -> True)
//  //col.g := 0x0
//  //col.b := 0x0
//  //--------
//}
////--------
//case class LcvVgaGradientNoDithIo(
//  rgbConfig: RgbConfig,
//  vgaTimingInfo: LcvVgaTimingInfo,
//) extends Bundle {
//  //--------
//  //val vgaCtrlIo = master(
//  //  LcvVgaCtrlIo(
//  //    rgbConfig=LcvVideoDithererIo(rgbConfig=rgbConfig).outRgbConfig,
//  //    vgaTimingInfo=vgaTimingInfo,
//  //  )
//  //)
//  val vgaCtrlEn = out Bool()
//  val vgaCtrlPush = master Stream(Rgb(rgbConfig))
//  val vgaCtrlMisc = in(LcvVgaCtrlMiscIo(vgaTimingInfo=vgaTimingInfo))
//  //--------
//}
//case class LcvVgaGradientNoDith(
//  rgbConfig: RgbConfig,
//  vgaTimingInfo: LcvVgaTimingInfo,
//) extends Component {
//  //--------
//  val io = LcvVgaGradientNoDithIo(
//    rgbConfig=rgbConfig,
//    vgaTimingInfo=vgaTimingInfo,
//  )
//  //val ctrlIo = io.vgaCtrlIo
//  val ctrlEn = io.vgaCtrlEn
//  val ctrlPush = io.vgaCtrlPush
//  val ctrlMisc = io.vgaCtrlMisc
//  //--------
//  //val skidBuf = PipeSkidBuf(
//  //  dataType=Rgb(rgbConfig),
//  //  optIncludeBusy=false,
//  //  optPassthrough=false,
//  //)
//  //val sbIo = skidBuf.io
//  //val sbPrevFire = sbIo.prev.fire
//  //sbIo.prev.valid := True
//
//  //val col = sbIo.prev.payload
//  val rPastCol = Reg(Rgb(rgbConfig))
//  rPastCol.init(rPastCol.getZero)
//  //val rDrawPos = Reg(Vec2(LcvVgaCtrlMiscIo.coordElemKindT()))
//  //rDrawPos.init(rDrawPos.getZero)
//  //--------
//  //ctrl.push << sbIo.next
//  ctrlPush << sbIo.next
//  //--------
//  // Enable VGA signal output
//  ctrlEn := True
//  //--------
//  rPastCol := col
//  //when (sbPrevFire) 
//  when (rPastCol === rPastCol.getZero){
//    //m.d.sync += [
//    //  drbus.inp.buf.prep.eq(0b1),
//    //  dibus.inp.en.eq(0b1),
//    //]
//
//    //when (dithIo.outpPayload.nextPos.x === 0x0) 
//    //when (
//    //  //ctrlIo.misc.drawPos.x === 0x0
//    //  ctrlMisc.nextVisib
//    //  rDrawPos.x === 0x0
//    //) {
//    //  //m.d.sync += col.r.eq(0x0)
//    //  col.r := 0x0
//    //} otherwise {
//      //m.d.sync += col.r.eq(col.r + 0x1)
//      col.r := rPastCol.r + 0x1
//    //}
//    //m.d.sync += col.r.eq(col.r + 0x1)
//
//    //m.d.sync += col.g.eq(0x0)
//    //m.d.sync += col.b.eq(0x0)
//    col.g := 0x0
//    col.b := 0x0
//  } otherwise { // when (~sbPrevFire)
//    col := rPastCol
//  }
//  //--------
//}
