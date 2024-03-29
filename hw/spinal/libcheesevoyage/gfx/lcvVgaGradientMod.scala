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
  clkRate: HertzNumber,
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
  val vidDithIo = master(LcvVideoDithererIo(
    rgbConfig=rgbConfig,
    fbSize2d=vgaTimingInfo.fbSize2d,
  ))

  //val vidDithIo = LcvVideoDithererIo(rgbConfig=rgbConfig)
  //val vidDithSbIo = PipeSkidBufIo(
  //  dataType=LcvVideoDithererStmPayload(rgbConfig=rgbConfig)
  //)

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
  clkRate: HertzNumber,
  rgbConfig: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  ctrlFifoDepth: Int,
  vivadoDebug: Boolean=false,
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
  // BEGIN: temporary
  //ctrlIo.push.valid := True
  //ctrlIo.push.payload.r := (default -> True)
  //ctrlIo.push.payload.g := 0x0
  //ctrlIo.push.payload.b := 0x0
  // END: temporary
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
  //val rDithPushValid = Reg(Bool()) init(False)
  //dithPushValid := rDithPushValid
  dithPushValid := True
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
  //rDithCol.r := initDithColR
  //rDithCol.g := 0x0
  //rDithCol.b := 0x0

  //val dithCol = dithIo.inpCol
  ////rPastDithCol := dithCol
  //val rDithCol = Reg(Rgb(rgbConfig))
  //rDithCol.init(rDithCol.getZero)
  //dithCol := rDithCol
  ////dithIo.inpCol := rDithCol
  ////val rDithCol = dithIo.inpCol
  ////rDithCol := rDithCol

  //ctrlIo.push.payload := dithIo.outp.col

  // BEGIN: final form
  ctrlIo.push.valid := dithIo.pop.valid
  ctrlIo.push.payload := dithIo.pop.payload.col
  dithIo.pop.ready := ctrlIo.push.ready
  // END: final form
  ////--------
  // BEGIN: debug
  //val rCtrlPushValid = Reg(Bool()) init(False)
  //ctrlIo.push.valid := rCtrlPushValid
  ////ctrlIo.push.valid := True
  //val rDbgPhysCol = Reg(Rgb(io.outRgbConfig))
  //rDbgPhysCol.init(rDbgPhysCol.getZero)
  //ctrlIo.push.payload := rDbgPhysCol
  //// END: debug
  ////--------
  // Gradient
  def cpp = LcvVgaCtrl.cpp(
    clkRate=clkRate,
    vgaTimingInfo=vgaTimingInfo,
  )
  //def resetDbgPhysCol(): Unit = {
  //  rDbgPhysCol.r := (default -> True)
  //  //rDbgPhysCol.r := 0x0
  //  rDbgPhysCol.g := 0x0
  //  rDbgPhysCol.b := 0x0
  //}
  //def incrDbgPhysCol(): Unit = {
  //  rDbgPhysCol.r := (default -> True)
  //  rDbgPhysCol.g := rDbgPhysCol.g + 1
  //  rDbgPhysCol.b := 0x0
  //}
  def resetDithCol(): Unit = {
    rDithCol.r := (default -> True)
    rDithCol.g := 0x0
    rDithCol.b := 0x0
  }
  def incrDithCol(): Unit = {
    rDithCol.r := (default -> True)
    rDithCol.g := rDithCol.g + 1
    rDithCol.b := 0x0
  }
  val rPastDithPopFire = Reg(Bool())
  rPastDithPopFire := dithIo.pop.fire
  //when (clockDomain.isResetActive) {
  //  resetDithCol()
  //} otherwise {
  //  when (
  //    dithIo.push.fire
  //    //rPastDithPopFire
  //  ) {
  //    when (
  //      //dithIo.info.changingRow
  //      //dithIo.info.pos.x === fbSize2d.x - 1
  //      dithIo.info.posWillOverflow.x
  //    ) {
  //      resetDithCol()
  //    } otherwise {
  //      incrDithCol()
  //    }
  //  }
  //}
  val gradConcat = Bits(2 bits)
  gradConcat(1) := dithIo.push.fire
  gradConcat(0) := dithIo.info.posWillOverflow.x
  when (clockDomain.isResetActive) {
    resetDithCol()
  } otherwise {
    when (dithIo.push.fire) {
      when (dithIo.info.posWillOverflow.x) {
        resetDithCol()
      } otherwise {
        incrDithCol()
      }
    }
    // this was slower:
    //switch (gradConcat) {
    //  // dithIo.push.fire=0, dithIo.info.posWillOverflow.x=0
    //  is (M"0-") {
    //  }
    //  // dithIo.push.fire=1, dithIo.info.posWillOverflow.x=0
    //  is (B"10") {
    //    resetDithCol()
    //  }
    //  // dithIo.push.fire=1, dithIo.info.posWillOverflow.x=1
    //  is (B"11") {
    //    incrDithCol()
    //  }
    //  default {
    //  }
    //}
  }

  // BEGIN: debug
  //when (clockDomain.isResetActive) {
  //  resetDbgPhysCol()
  //} otherwise {
  //  rCtrlPushValid := True
  //  when (ctrlIo.push.fire) {
  //    incrDbgPhysCol()
  //  }
  //}
  // END: debug
  //val rPos = Reg(dithIo.pop.payload.coordT())
  //val rNextPos = dithIo.pop.payload.coordT())
  //when (dithIo.push.fire) 
  //when (dithIo.pop.fire) {
  //  //when (dithIo.pop.payload.nextPos.x === 0x0) 
  //  //when (dithIo.pop.payload.changingRow) {
  //  //  resetDithCol()
  //  //} otherwise {
  //    incrDithCol()
  //  //}
  //}
  ////val pastPastVisib = Reg(Bool()) init(False)
  ////pastPastVisib := ctrlIo.misc.pastVisib

  ////val rPosX = Reg(UInt(ctrlIo.misc.drawPos.x.getWidth bits))
  ////val rPosX = Reg(UInt(log2Up(vgaTimingInfo.htiming.visib + 1) bits))
  ////  .init(vgaTimingInfo.htiming.visib - 1)
  ////  //.init(0x0)
  //////val initPosX = UInt(rPosX.getWidth bits)
  //////initPosX := (default -> True)
  ////val nextPosX = UInt(rPosX.getWidth bits)
  ////
  ////when (rPosX + 1 < vgaTimingInfo.htiming.visib) {
  ////  nextPosX := rPosX + 1
  ////} otherwise {
  ////  nextPosX := 0x0
  ////}

  ////when (rPosX =/= vgaTimingInfo.htiming.visib - 1) {
  ////  nextPosX := rPosX + 1
  ////} otherwise {
  ////  nextPosX := 0x0
  ////}

  ////when (ctrlIo.push.fire) 
  ////object State extends SpinalEnum(
  ////  //defaultEncoding=binarySequential
  ////  defaultEncoding=binaryOneHot
  ////) {
  ////  val
  ////    updateDithCol,
  ////    otherState
  ////    = newElement();
  ////}
  ////val state = Reg(State) init(State.updateDithCol)

  ////switch (state) {
  ////  is (State.updateDithCol) {
  ////    rCtrlPushValid := False
  ////    rDithPushValid := True
  ////    //when (dithIo.outp.nextPos.x === 0x0)
  ////    //when (dithIo.outp.pos.x === vgaTimingInfo.htiming.visib)
  ////    when (dithIo.outp.changingRow) {
  ////      resetDithCol()
  ////    } otherwise {
  ////      incrDithCol()
  ////    }
  ////    state := State.otherState
  ////  }
  ////  is (State.otherState) {
  ////    rDithPushValid := False
  ////    when (!rCtrlPushValid) {
  ////      rCtrlPushValid := True
  ////    } otherwise {
  ////      when (ctrlIo.push.fire) {
  ////        rCtrlPushValid := False
  ////        //rDithPushValid := False
  ////        state := State.updateDithCol
  ////      }
  ////    }
  ////  }
  ////}

  ////when (!rDithPushValid) 
  ////when (!rCtrlPushValid) {
  ////  rCtrlPushValid := True
  ////  //rCtrlPushValid := False
  ////  //rDithPushValid := True
  ////  //when (dithIo.outp.nextPos.x === 0x0) {
  ////  //  resetDithCol()
  ////  //} otherwise {
  ////  //  incrDithCol()
  ////  //}
  ////  rPosX := nextPosX
  ////  switch (nextPosX) 
  ////  //switch (rPosX) 
  ////  {
  ////    is (0) {
  ////      rDbgPhysCol.r := (default -> True)
  ////      rDbgPhysCol.g := 0x0
  ////      rDbgPhysCol.b := 0x0
  ////    }
  ////    is (1) {
  ////      //rDbgPhysCol.r := (default -> True)
  ////      rDbgPhysCol.r := 0x0
  ////      //rDbgPhysCol.g := (rDbgPhysCol.g.high -> True, default -> False)
  ////      rDbgPhysCol.g := (default -> True)
  ////      rDbgPhysCol.b := 0x0
  ////    }
  ////    //is (2) {
  ////    //  rDbgPhysCol.r := (default -> True)
  ////    //  rDbgPhysCol.g := (default -> True)
  ////    //  rDbgPhysCol.b := 0x0
  ////    //}
  ////    //is (3) {
  ////    //  rDbgPhysCol.r := 0x0
  ////    //  rDbgPhysCol.g := (default -> True)
  ////    //  rDbgPhysCol.b := 0x0
  ////    //}
  ////    //is (4) {
  ////    //  rDbgPhysCol.r := 0x0
  ////    //  rDbgPhysCol.g := 0x0
  ////    //  rDbgPhysCol.b := (default -> True)
  ////    //}
  ////    default {
  ////      //resetDbgPhysCol()
  ////      rDbgPhysCol := rDbgPhysCol.getZero
  ////    }
  ////  }
  ////} otherwise {
  ////  when (ctrlIo.push.fire) {
  ////    rCtrlPushValid := False
  ////  }
  ////}
  ////.otherwise {
  ////}
  ////  when (!rCtrlPushValid) {
  ////    rCtrlPushValid := True
  ////  } otherwise {
  ////    when (ctrlIo.push.fire) {
  ////      rCtrlPushValid := False
  ////      rDithPushValid := False
  ////    }
  ////  }
  ////}

  ////--------
}
