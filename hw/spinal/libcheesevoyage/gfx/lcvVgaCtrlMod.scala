package libcheesevoyage.gfx
import libcheesevoyage.general.FifoMiscIo
import libcheesevoyage.general.FifoIo
import libcheesevoyage.general.Fifo
import libcheesevoyage.general.AsyncReadFifo
import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2
import libcheesevoyage.general.PipeSkidBuf
import libcheesevoyage.general.PipeSkidBufIo

//import scala.math._
import spinal.core._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer
import scala.math._

object LcvVgaState extends SpinalEnum(defaultEncoding=binarySequential) {
  val
    front,
    sync,
    back,
    visib
    = newElement();
}

class LcvVgaStateCnt(
  vgaTimingHv: LcvVgaTimingHv
) //extends Bundle
{
  //--------
  val s = Reg(LcvVgaState()) init(LcvVgaState.front)
  //val sWrapNext = s.wrapNext()
  val c = Reg(UInt(vgaTimingHv.cntWidth() bits)) init(0x0)
  val cPWidth = vgaTimingHv.cntWidth() + 1
  //val cP1 = UInt(cPWidth bits)
  //cP1 := c.resized + U(f"$cPWidth'd1")
  //val cP2 = UInt(cPWidth bits)
  //cP2 := c.resized + U(f"$cPWidth'd2")
  //val cP1 = c.resized + U(f"$cPWidth'd1")
  //val cP2 = c.resized + U(f"$cPWidth'd2")
  val nextS = LcvVgaState()
  //val nextS = s.wrapNext()
  //--------
  def noChangeUpdateNextS(): Unit = {
    nextS := s
  }
  def updateStateCnt(
    vgaTimingHv: LcvVgaTimingHv,
  ): Unit = {
    def mkCase(
      stateSize: Int,
      nextState: LcvVgaState.C,
    ): Unit = {
      val counterP1 = c + 0x1
			when (counterP1 >= stateSize) {
				//m.d.sync += stateCnt.s := (nextState)
				//m.d.sync += stateCnt.c := (0x0)
				////m.d.comb += stateCnt.nextS := (nextState)
				//sWrapNext := nextState
				//s := nextState
				nextS := nextState
				s := nextState
				c := c.getZero
			} otherwise {
				//m.d.sync += stateCnt.c := (counterP1)
				////self.noChangeUpdateNextS(m, stateCnt)
				nextS := s
				c := counterP1
			}
			//s := nextS

			//when ((c + 0x2) >= stateSize) {
			//	//m.d.comb += stateCnt.nextS := (nextState)
			//	nextS := nextState
			//} otherwise {
			//	//m.d.comb += stateCnt.nextS := (stateCnt.s)
			//	nextS := s
			//}

    }

    //State = VgaTiming.State
    switch (s) {
      is (LcvVgaState.front) {
        val stateSize = vgaTimingHv.front
        mkCase(stateSize=stateSize, nextState=LcvVgaState.sync)
        //println(f"front, sync: $stateSize")
      }
      is (LcvVgaState.sync) {
        val stateSize = vgaTimingHv.sync
        mkCase(stateSize=stateSize, nextState=LcvVgaState.back)
        //println(f"sync, back: $stateSize")
      }
      is (LcvVgaState.back) {
        val stateSize = vgaTimingHv.back
        mkCase(stateSize=stateSize, nextState=LcvVgaState.visib)
        //println(f"back, visib: $stateSize")
      }
      is (LcvVgaState.visib) {
        val stateSize = vgaTimingHv.visib
        mkCase(stateSize=stateSize, nextState=LcvVgaState.front)
        //println(f"visib, front: $stateSize")
      }
    }
    
  }
}

//case class LcvVgaCtrlBufInp(rgbConfig: RgbConfig) extends Bundle {
//  //val prep = Bool()
//  //val col = Rgb(rgbConfig)
//  val valid = Bool()
//  val payload = Rgb(rgbConfig)
//}
//case class LcvVgaCtrlBufOutp() extends Bundle {
//  //val canPrep = Bool()
//  val ready = Bool()
//}
case class LcvVgaPhys(rgbConfig: RgbConfig) extends Bundle {
  val col = Rgb(rgbConfig)
  val hsync = Bool()
  val vsync = Bool()
}
//case class LcvVgaCtrlInp(rgbConfig: RgbConfig) extends Bundle {
//  // Global VGA driving enable (white screen when off)
//  val en = Bool()
//
//  //// Pixel buffer
//  //val buf = LcvVgaCtrlBufInp(rgbConfig=rgbConfig)
//}
object LcvVgaCtrlMiscIo {
  //--------
  def coordElemT(): UInt = UInt(16 bits)
  def cpp(
    clkRate: Double,
    vgaTimingInfo: LcvVgaTimingInfo,
  ): Int = {
    return scala.math.floor(
      clkRate / vgaTimingInfo.pixelClk
    ).toInt
  }
  def clkCntWidth(
    clkRate: Double,
    vgaTimingInfo: LcvVgaTimingInfo,
  ): Int = {
    return log2Up(cpp(
      clkRate=clkRate, vgaTimingInfo=vgaTimingInfo
    ))
  }
  //--------
}
case class LcvVgaCtrlMiscIo(
  clkRate: Double,
  vgaTimingInfo: LcvVgaTimingInfo,
  fifoDepth: Int,
) extends Bundle {
  // VGA physical pins
  //self.outpCol = ColorT()
  //val col = Rgb(rgbConfig)
  //val hsync = Bool()
  //val vsync = Bool()
  //val vga = LcvVga(rgbConfig=rgbConfig)

  //val buf = port VgaDriverBufOutpLayt()//.shape
  //val buf = LcvVgaCtrlBufOutp()
  val hscS = LcvVgaState()
  val hscC = UInt(vgaTimingInfo.htiming.cntWidth() bits)
  //val hscCP1 = hscC.resized + U(f"$cPWidth'd1")
  //val hscCP2 = hscC.resized + U(f"$cPWidth'd2")
  val hscNextS = LcvVgaState()

  val vscS = LcvVgaState()
  val vscC = UInt(vgaTimingInfo.vtiming.cntWidth() bits)
  //val vscCP1 = vscC.resized + U(f"$cPWidth'd1")
  //val vscCP2 = vscC.resized + U(f"$cPWidth'd2")
  val vscNextS = LcvVgaState()

  val fifoEmpty = Bool()
  val fifoFull = Bool()
  val fifoAmountCanPush = UInt(FifoMiscIo.amountWidth(depth=fifoDepth) bits)
  val fifoAmountCanPop = UInt(FifoMiscIo.amountWidth(depth=fifoDepth) bits)
  val fifoPopReady = Bool()

  // Misc.
  val clkCnt = UInt(LcvVgaCtrlMiscIo.clkCntWidth(
    clkRate=clkRate,
    vgaTimingInfo=vgaTimingInfo,
  ) bits)
  val nextClkCnt = UInt(LcvVgaCtrlMiscIo.clkCntWidth(
    clkRate=clkRate,
    vgaTimingInfo=vgaTimingInfo,
  ) bits)
  val nextNextPixelEn = Bool()
  val nextPixelEn = Bool()
  val pixelEn = Bool()
  val nextNextVisib = Bool()
  val nextVisib = Bool()
  val visib = Bool()
  val pastVisib = Bool()
  val drawPos = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  //val nextDrawPos = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  val pastDrawPos = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  val size = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  //--------
  //--------
}
case class LcvVgaCtrlIo(
  clkRate: Double,
  rgbConfig: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  fifoDepth: Int,
) extends Bundle with IMasterSlave {
  //--------
  val en = in Bool()
  //val inp = in(LcvVgaCtrlInp(rgbConfig=rgbConfig))
  //val outp = out(LcvVgaCtrlOutp(rgbConfig=rgbConfig))
  val push = slave Stream(Rgb(rgbConfig))
  val phys = out(LcvVgaPhys(rgbConfig=rgbConfig))
  val misc = out(LcvVgaCtrlMiscIo(
    clkRate=clkRate,
    vgaTimingInfo=vgaTimingInfo,
    fifoDepth=fifoDepth,
  ))
  //--------
  def asMaster(): Unit = {
    out(en)
    master(push)
    in(phys, misc)
  }
  //def asSlave(): Unit = {
  //  in(en)
  //  slave(push)
  //  out(phys, misc)
  //}
  //--------
}
//--------
//  //--------
//}
object LcvVgaCtrl {
  def cpp(
    clkRate: Double,
    vgaTimingInfo: LcvVgaTimingInfo,
  ): Int = {
    //return scala.math.floor(
    //  clkRate / vgaTimingInfo.pixelClk
    //).toInt
    return LcvVgaCtrlMiscIo.cpp(
      clkRate=clkRate,
      vgaTimingInfo=vgaTimingInfo,
    )
  }
  def clkCntWidth(
    clkRate: Double,
    vgaTimingInfo: LcvVgaTimingInfo,
  ): Int = {
    //return log2Up(cpp(
    //  clkRate=clkRate, vgaTimingInfo=vgaTimingInfo
    //))
    return LcvVgaCtrlMiscIo.clkCntWidth(
      clkRate=clkRate,
      vgaTimingInfo=vgaTimingInfo,
    )
  }
}
case class LcvVgaCtrl(
  clkRate: Double,
  rgbConfig: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  fifoDepth: Int,
) extends Component {
  //--------
  val io = LcvVgaCtrlIo(
    clkRate=clkRate,
    rgbConfig=rgbConfig,
    vgaTimingInfo=vgaTimingInfo,
    fifoDepth=fifoDepth,
  )
  val push = io.push
  //val inpCol = io.inpCol
  val phys = io.phys
  val misc = io.misc
  //--------
  // Clocks per pixel
  //def cpp: Int = {
  //  return scala.math.floor(
  //    clkRate / vgaTimingInfo.pixelClk
  //  ).toInt
  //}
  def cpp = LcvVgaCtrl.cpp(clkRate=clkRate, vgaTimingInfo=vgaTimingInfo)
  def htiming: LcvVgaTimingHv = {
    return vgaTimingInfo.htiming
  }
  def vtiming: LcvVgaTimingHv = {
    return vgaTimingInfo.vtiming
  }
  //def numBufScanlines():
  //  return numBufScanlines
  //def fifoDepth():
  //  return (fbSize2d.x * numBufScanlines())
  //def fifoDepth():
  //  return fifoDepth
  //def fbSize2d: ElabVec2[Int] = {
  //  //ret = blank()
  //  //ret.x, ret.y = htiming.visib(), vtiming.visib()
  //  //return ret
  //  return ElabVec2(htiming.visib, vtiming.visib)
  //}
  def fbSize2d: ElabVec2[Int] = vgaTimingInfo.fbSize2d
  //def clkCntWidth: Int = {
  //  return log2Up(cpp)
  //}
  def clkCntWidth = LcvVgaCtrl.clkCntWidth(
    clkRate=clkRate,
    vgaTimingInfo=vgaTimingInfo
  )
  //--------
  //val fifo = Fifo
  //val fifo = AsyncReadFifo(
  //  dataType=Rgb(rgbConfig),
  //  depth=fifoDepth,
  //  arrRamStyle="auto"
  //)
  val fifo = StreamFifo(
    dataType=Rgb(rgbConfig),
    depth=fifoDepth,
    //latency=2,
    //latency=1,
    latency=0,
    forFMax=true,
  )
  val fifoPush = fifo.io.push
  val fifoPop = fifo.io.pop
  //val fifoEmpty = fifo.io.misc.empty
  //val fifoFull = fifo.io.misc.full
  //val fifoAmountCanPush = fifo.io.misc.amountCanPush
  //val fifoAmountCanPop = fifo.io.misc.amountCanPop
  val fifoEmpty = fifo.io.availability === fifoDepth
  val fifoFull = fifo.io.occupancy === fifoDepth
  val fifoAmountCanPush = fifo.io.availability
  val fifoAmountCanPop = fifo.io.occupancy
  
  //fifoPush << push
  fifoPush <-/< push
  //--------
  val tempCol = Rgb(rgbConfig) addAttribute("keep")
  tempCol := fifoPop.payload
  //--------
  val rPhys = Reg(LcvVgaPhys(rgbConfig=rgbConfig))
  rPhys.init(rPhys.getZero)
  //val rPastPhys = Reg(LcvVgaPhys(rgbConfig=rgbConfig))
  //rPastPhys.init(rPastPhys.getZero)
  //rPastPhys := phys
  phys := rPhys
  //val rHsync = Reg(Bool()) init(False)
  //val rVsync = Reg(Bool()) init(False)
  //phys.hsync := rHsync
  //phys.vsync := rHsync
  val rHsync = rPhys.hsync
  val rVsync = rPhys.vsync

  // Implement the clock enable
  val clkCnt = Reg(UInt(clkCntWidth bits)) init(0x0)
  //val nextClkCnt = clkCnt.wrapNext()
  val nextClkCnt = UInt(clkCntWidth bits)
  // Force this addition to be of width `CLK_CNT_WIDTH + 1` to
  // prevent wrap-around
  val clkCntP1Width = clkCntWidth + 1
  val clkCntP1 = UInt(clkCntP1Width bits)
  clkCntP1 := clkCnt.resized + U(f"$clkCntP1Width'd1")
  misc.clkCnt := clkCnt
  misc.nextClkCnt := nextClkCnt

  // Implement wrap-around for the clock counter
  when (clkCntP1 < cpp) {
    //m.d.sync += 
    nextClkCnt := clkCntP1(clkCnt.bitsRange)
  } otherwise {
    //m.d.sync +=
    nextClkCnt := 0x0
  }
  clkCnt := nextClkCnt
  // Since this is an alias, use ALL_CAPS for its name.
  // outp.pixelEn = (clkCnt == 0x0)
  //m.d.comb += 
  misc.pixelEn := clkCnt === 0x0
  val pixelEnNextCycle = Bool()
  pixelEnNextCycle := clkCntP1.resized === cpp

  //val nextPixelEn = Bool()
  misc.nextPixelEn := nextClkCnt === 0x0

  //val rPastFifoPopReady = Reg(Bool()) init(False)
  //rPastFifoPopReady := fifoPop.ready
  //fifoPop.ready := misc.nextPixelEn & misc.nextVisib & ~rPastFifoPopReady
  //fifoPop.ready := misc.nextPixelEn & misc.nextVisib & ~rPastFifoPopReady
  //fifoPop.ready := misc.nextPixelEn & misc.nextVisib
  //val rFifoPopReady = Reg(Bool()) init(False)

  //fifoPop.ready := (
  //  //pixelEnNextCycle
  //  //&& (misc.nextDrawPos.x === 0)
  //  misc.nextPixelEn && misc.nextVisib
  //  && !misc.fifoEmpty
  //)
  //fifoPop.ready 
  //fifoPop.ready := rFifoPopReady
  //misc.fifoPopReady := rFifoPopReady
  val pastPixelEn = Reg(Bool()) init(False)
  pastPixelEn := misc.pixelEn
  //fifoPop.ready := (
  //  //pastPixelEn && misc.pastVisib && !fifoEmpty
  //  //misc.pixelEn && misc.visib && !fifoEmpty
  //  misc.nextPixelEn && misc.nextVisib && !fifoEmpty
  //  //misc.nextNextPixelEn && misc.nextNextVisib && !fifoEmpty
  //)
  val rFifoPopReady = Reg(Bool()) init(False)
  val rNextNextPixelEn = Reg(Bool()) init(False)
  //val rNextNextVisib = Reg(Bool()) init(False)
  //val rInvFifoEmpty = Reg(Bool()) init(False)
  rNextNextPixelEn := misc.nextNextPixelEn
  //rNextNextVisib := misc.nextNextVisib
  //rInvFifoEmpty := !fifoEmpty

  // `hscCOffs` pipeline stage delays
  //val hscCOffs = 7
  //val hscCPlusOffsWidth = log2Up(vgaTimingInfo.htiming.back + hscCOffs)
  //val rHscCPlusOffs = Reg(UInt(hscCPlusOffsWidth bits)) init(0x0)
  //val rWillBeHscCVisib = Reg(Bool()) init(False)
  //val rWillBeHscSVisib = Reg(UInt(2 bits)) init(0x0)
  //val rWillBeVscSVisib = Reg(Bool()) init(False)
  //val rNextNextVisib = Reg(Bool()) init(False)
  ////rWillBeHscCVisib := misc.hscC + 3 === vgaTimingInfo.htiming.back
  //rHscCPlusOffs := misc.hscC.resized + U(f"$hscCPlusOffsWidth'd$hscCOffs")
  //rWillBeHscCVisib := rHscCPlusOffs === vgaTimingInfo.htiming.back
  //rWillBeHscSVisib(0) := misc.hscS === LcvVgaState.back 
  //rWillBeHscSVisib(1) := misc.hscS === LcvVgaState.visib
  //rWillBeVscSVisib := misc.vscS === LcvVgaState.visib
  //rNextNextVisib := (
  //  (rWillBeHscCVisib && rWillBeHscSVisib(0))
  //  || rWillBeHscSVisib(1)
  //  && rWillBeVscSVisib
  //)
  //val rHscCIsLastBack = Reg(Bool()) init(False)
  //val rHscSIsBack = Reg(Bool()) init(False)
  //val rHscSIsVisib = Reg(Bool()) init(False)
  //val rVscSIsVisib = Reg(Bool()) init(False)
  //val fifoPopDelay = 3
  //// 3 delay cycles
  //// delay 3
  //val rInvFifoEmptyArr = ArrayBuffer[Bool]()
  //val rNextNextPixelEnArr = ArrayBuffer[Bool]()

  //for (idx <- 0 to fifoPopDelay - 1) {
  //  rInvFifoEmptyArr += Reg(Bool()) init(False)
  //  rInvFifoEmptyArr(idx).setName(f"rInvFifoEmptyArr_$idx")
  //  rNextNextPixelEnArr += Reg(Bool()) init(False)
  //  rNextNextPixelEnArr(idx).setName(f"rNextPixelEnArr_$idx")
  //  if (idx == 0) {
  //    rInvFifoEmptyArr(idx) := !fifoEmpty
  //    rNextNextPixelEnArr(idx) := clkCntP1 === cpp - 2 - fifoPopDelay
  //    //rNextNextPixelEnArr(idx) := clkCntP1 === cpp - 2 - (fifoPopDelay - 1)
  //    //rNextNextPixelEnArr(idx) := clkCntP1 === cpp - fifoPopDelay
  //  } else {
  //    rInvFifoEmptyArr(idx) := rInvFifoEmptyArr(idx - 1)
  //    rNextNextPixelEnArr(idx) := rNextNextPixelEnArr(idx - 1)
  //  }
  //}

  // delay 3
  //rHscCIsLastBack := (
  //  misc.hscC === (vgaTimingInfo.htiming.back - fifoPopDelay - 1)
  //)
  //rHscSIsBack := misc.hscS === LcvVgaState.back
  //rHscSIsVisib := misc.hscS === LcvVgaState.visib
  //rVscSIsVisib := misc.vscS === LcvVgaState.visib

  ////val rNextNextPixelEn = Reg(Bool()) init(False)
  //val rNextNextVisib = Reg(Bool()) init(False)
  //// delay 2
  //rNextNextVisib := (
  //  (
  //    (rHscCIsLastBack && rHscSIsBack)
  //    || rHscSIsVisib
  //  ) && rVscSIsVisib
  //)

  ////rFifoPopReady := 
  ////fifoPop.ready := 
  //// delay 1
  ////rFifoPopReady :=
  //fifoPop.ready := (
  //  //pastPixelEn && misc.pastVisib && !fifoEmpty
  //  //misc.pixelEn && misc.visib && !fifoEmpty
  //  //misc.nextPixelEn && misc.nextVisib && !fifoEmpty
  //  //misc.nextNextPixelEn && misc.nextNextVisib && !fifoEmpty
  //  // delay 1
  //  rNextNextPixelEnArr.last && rNextNextVisib && rInvFifoEmptyArr.last
  //)
  //fifoPop.ready := (
  //  //misc.nextNextPixelEn && misc.nextNextVisib && !fifoEmpty
  //  misc.nextPixelEn && misc.nextVisib && !fifoEmpty
  //)
  //val rFifoPopReady = Reg(Bool()) init(False)
  //val nextRegDelay = 2
  val rTempNextVisib = Reg(Bool()) init(False)
  val rTempNextPixelEn = Reg(Bool()) init(False)
  //rTempNextVisib := (
  //  (
  //    (
  //      //(misc.hscC + 1 === vgaTimingInfo.htiming.back)
  //      (misc.hscC >= vgaTimingInfo.htiming.back - 2)
  //      && (misc.hscS === LcvVgaState.back)
  //    ) || (
  //      //(misc.hscC < vgaTiming.htiming.visib - 2)
  //      //&& 
  //      (misc.hscC <= vgaTimingInfo.htiming.visib - 2)
  //      && (misc.hscS === LcvVgaState.visib)
  //    )
  //  ) && (
  //    //(
  //    //  ((misc.vscC + 2) >= vgaTimingInfo.vtiming.back)
  //    //  && (misc.vscS === LcvVgaState.back)
  //    //)
  //    //||
  //    //(misc.vscC < vgaTimingInfo.vtiming.visib - 2)
  //    //&& (misc.vscNextS === LcvVgaState.visib)
  //    misc.vscS === LcvVgaState.visib
  //  )
  //  //|| misc.visib
  //)
  rTempNextVisib := (
    (
      (
        (misc.hscC >= vgaTimingInfo.htiming.back - 2)
        && (misc.hscS === LcvVgaState.back)
      ) || (
        (misc.hscC < fbSize2d.x - 2)
        && (misc.hscS === LcvVgaState.visib)
      )
    ) && (
      //misc.vscS === LcvVgaState.visib
      misc.vscS === LcvVgaState.visib
    )
  )
  //tempNextPixelEn := clkCntP1 === cpp - nextRegDelay + 1
  //jtempNextPixelEn := nextClkCnt === (cpp - nextRegDelay - 1)
  //val rTempNextPixelEn = Reg(Bool()) init(False)
  rTempNextPixelEn := nextClkCnt === (cpp - 1)
  //rFifoPopReady :=
  // BEGIN: working
  //fifoPop.ready := (
  //  misc.nextPixelEn && misc.nextVisib && !fifoEmpty
  //)
  //fifoPop.ready := (
  //  rTempNextPixelEn && misc.nextVisib && !fifoEmpty
  //)
  // END: working
  fifoPop.ready := (
    rTempNextPixelEn && rTempNextVisib && !fifoEmpty
  )
  //rFifoPopReady := (
  //  //misc.nextNextPixelEn && misc.nextNextVisib && !fifoEmpty
  //  //rTempNextVisib && rTempNextPixelEn && !fifoEmpty
  //)
  //fifoPop.ready := rFifoPopReady
  misc.fifoPopReady := fifoPop.ready
  misc.nextNextPixelEn := clkCntP1 === cpp - 2
  //misc.nextNextVisib := rNextNextVisib

  misc.nextNextVisib := (
    (
      (
        //(misc.hscC + 1 === vgaTimingInfo.htiming.back)
        (misc.hscC + 2 === vgaTimingInfo.htiming.back)
        && (misc.hscS === LcvVgaState.back)
      ) || (
        misc.hscS === LcvVgaState.visib
      )
    ) && (
      //(
      //  ((misc.vscC + 2) >= vgaTimingInfo.vtiming.back)
      //  && (misc.vscS === LcvVgaState.back)
      //)
      //||
      misc.vscS === LcvVgaState.visib
    )
    //|| misc.visib
  )

  //rFifoPopReady := (
  ////fifoPop.ready 
  //  //misc.nextPixelEn
  //  //misc.nextNextPixelEn && misc.nextNextVisib
  //  //fifoPop.valid 
  //  //(clkCnt === (cpp - 3))
  //  //(nextClkCnt === (cpp - 3))
  //  //(nextClkCnt === cpp - 1)
  //  //(clkCntP1 === cpp)
  //  misc.nextNextPixelEn
  //  && misc.nextNextVisib
  //)
  //rFifoPopReady := (
  //  misc.nextPixelEn && misc.nextVisib
  //)
  //fifoPop.ready := rFifoPopReady

  //fifoPop.ready := rFifoPopReady
  //rFifoPopReady := False
  //when (fifoPop.valid) {
  //  when (misc.nextPixelEn) {
  //    rFifoPopReady := True
  //  }
  //}
  misc.fifoEmpty := fifoEmpty
  misc.fifoFull := fifoFull
  misc.fifoAmountCanPush := fifoAmountCanPush
  misc.fifoAmountCanPop := fifoAmountCanPop
  //--------
  // Implement the State/Counter stuff
  //loc.Tstate = VgaTiming.jkState
  //loc.hsc = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.HTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  //loc.vsc = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.VTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  val hsc = new LcvVgaStateCnt(vgaTimingHv=htiming)
  val vsc = new LcvVgaStateCnt(vgaTimingHv=vtiming)

  misc.hscS := hsc.s
  misc.hscC := hsc.c
  misc.hscNextS := hsc.nextS
  misc.vscS := vsc.s
  misc.vscC := vsc.c
  misc.vscNextS := vsc.nextS
  //--------
  // Implement HSYNC and VSYNC logic
  when (misc.pixelEn) {
    //htiming.updateStateCnt(m, hsc)
    hsc.updateStateCnt(htiming)

    switch (hsc.s) {
      is (LcvVgaState.front) {
        //m.d.sync += outp.hsync := (0b1)
        rHsync := True
        //vtiming.noChangeUpdateNextS(m, vsc)
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.sync) {
        //m.d.sync += outp.hsync := (0b0)
        rHsync := False
        //vtiming.noChangeUpdateNextS(m, vsc)
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.back) {
        //m.d.sync += outp.hsync := (0b1)
        rHsync := True
        //vtiming.noChangeUpdateNextS(m, vsc)
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.visib) {
        //m.d.sync += outp.hsync := (0b1)
        rHsync := True
        //when ((hsc["c"] + 0x1) >= FB_SIZE().x) 
        //when ((hsc.c + 0x1) >= fbSize2d.x)
        when ((hsc.c + 0x1) >= fbSize2d.x) {
          //vtiming.updateStateCnt(m, vsc)
          vsc.updateStateCnt(vtiming)
        } otherwise {
          //vtiming.noChangeUpdateNextS(m, vsc)
          vsc.noChangeUpdateNextS()
        }
      }
    }

    switch (vsc.s) {
      is (LcvVgaState.front) {
        //m.d.sync += outp.vsync := (0b1)
        rVsync := True
      }
      is (LcvVgaState.sync) {
        //m.d.sync += outp.vsync := (0b0)
        rVsync := False
      }
      is (LcvVgaState.back) {
        //m.d.sync += outp.vsync := (0b1)
        rVsync := True
      }
      is (LcvVgaState.visib) {
        //m.d.sync += outp.vsync := (0b1)
        rVsync := True
      }
    }
  } otherwise { // when (~misc.pixelEn)
    //htiming.noChangeUpdateNextS(m, hsc)
    //vtiming.noChangeUpdateNextS(m, vsc)
    hsc.noChangeUpdateNextS()
    vsc.noChangeUpdateNextS()
  }
  //--------
  // Implement drawing the picture

  when (misc.pixelEn) {
    // Visible area
    when (misc.visib) {
      when (~io.en) {
        //m.d.sync += [
          //phys.col.r := (0xf),
          //phys.col.g := (0xf),
          //phys.col.b := (0xf),
        //]
        rPhys.col.r := (default -> True)
        rPhys.col.g := (default -> True)
        rPhys.col.b := (default -> True)
      } otherwise { // when (io.en)
        //m.d.sync += [
          rPhys.col := tempCol
        //]
      }
    // Black border
    } otherwise { // when (~misc.visib)
      //m.d.sync += [
        //phys.col.r := 0x0
        //phys.col.g := 0x0
        //phys.col.b := 0x0
        rPhys.col := rPhys.col.getZero
      //]
    }
  } //otherwise {
  //  rPhys.col := rPastPhys.col
  //}
  //--------
  //val rMisc = Reg(LcvVgaCtrlMiscIo())
  //rMisc.init(rMisc.getZero)
  //misc := rMisc
    //m.d.comb += [
      //misc.visib := ((hsc.s == Tstate.VISIB)
      // & (vsc.s == Tstate.VISIB)),
    misc.drawPos.x := hsc.c.resized
    misc.drawPos.y := vsc.c.resized
    misc.size.x := fbSize2d.x
    misc.size.y := fbSize2d.y
    //]
    //m.d.sync += [
    val rNextVisib = Reg(Bool()) init(False)
    rNextVisib := ((hsc.nextS === LcvVgaState.visib)
      & (vsc.nextS === LcvVgaState.visib))
    misc.nextVisib := rNextVisib
    //misc.nextVisib := ((hsc.nextS === LcvVgaState.visib)
    //  & (vsc.nextS === LcvVgaState.visib))
    //misc.nextVisib := (
    //  hsc.nextS === LcvVgaState.visib
    //  && vsc.nextS === LcvVgaState.visib
    //)
    //cover(hsc.nextS === LcvVgaState.sync)

    val rVisib = Reg(Bool()) init(False)
    rVisib := misc.nextVisib
    misc.visib := rVisib

    val rPastVisib = Reg(Bool()) init(False)
    rPastVisib := misc.visib
    misc.pastVisib := rPastVisib

    val rPastDrawPos = Reg(Vec2(LcvVgaCtrlMiscIo.coordElemT()))
    rPastDrawPos.init(rPastDrawPos.getZero)
    rPastDrawPos := misc.drawPos
    misc.pastDrawPos := rPastDrawPos
    //]

  //val hscNextSVisib = Bool() addAttribute("keep")
  //hscNextSVisib := hsc.nextS === LcvVgaState.visib
  //val vscNextSVisib = Bool() addAttribute("keep")
  //vscNextSVisib := vsc.nextS === LcvVgaState.visib
  ////--------
  //GenerationFlags.formal {
  //  when (pastValidAfterReset) {
  //    when (
  //      past(misc.pixelEn)
  //      && hscNextSVisib
  //      && vscNextSVisib
  //      && misc.drawPos.x.resized < htiming.visib
  //      && misc.drawPos.y.resized < vtiming.visib
  //    ) {
  //      assert(misc.nextVisib)
  //    }
  //  }
  //}
}
//--------

case class LcvVgaCtrlNoFifoIo(
  clkRate: Double,
  rgbConfig: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
) extends Bundle with IMasterSlave {
  //--------
  val en = in Bool()
  //val inp = in(LcvVgaCtrlInp(rgbConfig=rgbConfig))
  //val outp = out(LcvVgaCtrlOutp(rgbConfig=rgbConfig))
  //val push = slave Stream(Rgb(rgbConfig))
  val inpCol = in(Rgb(rgbConfig))
  val phys = out(LcvVgaPhys(rgbConfig=rgbConfig))
  val misc = out(LcvVgaCtrlMiscIo(
    clkRate=clkRate,
    vgaTimingInfo=vgaTimingInfo,
    fifoDepth=1,
  ))
  //--------
  def asMaster(): Unit = {
    out(en, inpCol)
    //master(push)
    in(phys, misc)
  }
  //def asSlave(): Unit = {
  //  in(en)
  //  slave(push)
  //  out(phys, misc)
  //}
  //--------
}
//--------
case class LcvVgaCtrlNoFifo(
  clkRate: Double,
  rgbConfig: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  //fifoDepth: Int,
) extends Component {
  //--------
  val io = LcvVgaCtrlNoFifoIo(
    clkRate=clkRate,
    rgbConfig=rgbConfig,
    vgaTimingInfo=vgaTimingInfo,
  )
  //val push = io.push
  val inpCol = io.inpCol
  val phys = io.phys
  val misc = io.misc
  //--------
  // Clocks per pixel
  def cpp: Int = {
    return scala.math.floor(
      clkRate / vgaTimingInfo.pixelClk
    ).toInt
  }
  def htiming: LcvVgaTimingHv = {
    return vgaTimingInfo.htiming
  }
  def vtiming: LcvVgaTimingHv = {
    return vgaTimingInfo.vtiming
  }
  //def numBufScanlines():
  //  return numBufScanlines
  //def fifoDepth():
  //  return (fbSize2d.x * numBufScanlines())
  //def fifoDepth():
  //  return fifoDepth
  def fbSize2d: ElabVec2[Int] = {
    //ret = blank()
    //ret.x, ret.y = htiming.visib(), vtiming.visib()
    //return ret
    return ElabVec2(htiming.visib, vtiming.visib)
  }
  def clkCntWidth: Int = {
    return log2Up(cpp)
  }
  //--------
  misc.fifoEmpty := False
  misc.fifoFull := False
  //--------
  val rPhys = Reg(LcvVgaPhys(rgbConfig=rgbConfig))
  rPhys.init(rPhys.getZero)
  phys := rPhys

  // Implement the clock enable
  val clkCnt = Reg(UInt(clkCntWidth bits)) init(0x0)
  // Force this addition to be of width `CLK_CNT_WIDTH + 1` to
  // prevent wrap-around
  val clkCntP1Width = clkCntWidth + 1
  val clkCntP1 = UInt(clkCntP1Width bits)
  clkCntP1 := clkCnt.resized + U(f"$clkCntP1Width'd1")

  // Implement wrap-around for the clock counter
  when (clkCntP1 < cpp) {
    //m.d.sync += 
    clkCnt := clkCntP1(clkCnt.bitsRange)
  } otherwise {
    //m.d.sync +=
    clkCnt := 0x0
  }
  // Since this is an alias, use ALL_CAPS for its name.
  // outp.pixelEn = (clkCnt == 0x0)
  //m.d.comb += 
  misc.pixelEn := clkCnt === 0x0
  val pixelEnNextCycle = Bool()
  pixelEnNextCycle := clkCntP1.resized === cpp
  //--------
  // Implement the State/Counter stuff
  //loc.Tstate = VgaTiming.State
  //loc.hsc = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.HTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  //loc.vsc = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.VTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  val hsc = new LcvVgaStateCnt(vgaTimingHv=htiming)
  val vsc = new LcvVgaStateCnt(vgaTimingHv=vtiming)
  misc.hscS := hsc.s
  misc.hscC := hsc.c
  misc.hscNextS := hsc.nextS
  misc.vscS := vsc.s
  misc.vscC := vsc.c
  misc.vscNextS := vsc.nextS
  //--------
  // Implement HSYNC and VSYNC logic
  when (misc.pixelEn) {
    //htiming.updateStateCnt(m, hsc)
    hsc.updateStateCnt(htiming)

    switch (hsc.s) {
      is (LcvVgaState.front) {
        //m.d.sync += outp.hsync := (0b1)
        rPhys.hsync := True
        //vtiming.noChangeUpdateNextS(m, vsc)
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.sync) {
        //m.d.sync += outp.hsync := (0b0)
        rPhys.hsync := False
        //vtiming.noChangeUpdateNextS(m, vsc)
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.back) {
        //m.d.sync += outp.hsync := (0b1)
        rPhys.hsync := True
        //vtiming.noChangeUpdateNextS(m, vsc)
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.visib) {
        //m.d.sync += outp.hsync := (0b1)
        rPhys.hsync := True
        //when ((hsc["c"] + 0x1) >= FB_SIZE().x) 
        //when ((hsc.c + 0x1) >= fbSize2d.x)
        when ((hsc.c + 0x1) >= fbSize2d.x) {
          //vtiming.updateStateCnt(m, vsc)
          vsc.updateStateCnt(vtiming)
        } otherwise {
          //vtiming.noChangeUpdateNextS(m, vsc)
          vsc.noChangeUpdateNextS()
        }
      }
    }

    switch (vsc.s) {
      is (LcvVgaState.front) {
        //m.d.sync += outp.vsync := (0b1)
        rPhys.vsync := True
      }
      is (LcvVgaState.sync) {
        //m.d.sync += outp.vsync := (0b0)
        rPhys.vsync := False
      }
      is (LcvVgaState.back) {
        //m.d.sync += outp.vsync := (0b1)
        rPhys.vsync := True
      }
      is (LcvVgaState.visib) {
        //m.d.sync += outp.vsync := (0b1)
        rPhys.vsync := True
      }
    }
  } otherwise { // when (~misc.pixelEn)
    //htiming.noChangeUpdateNextS(m, hsc)
    //vtiming.noChangeUpdateNextS(m, vsc)
    hsc.noChangeUpdateNextS()
    vsc.noChangeUpdateNextS()
  }
  //--------
  // Implement drawing the picture

  when (misc.pixelEn) {
    // Visible area
    when (misc.visib) {
      when (~io.en) {
        //m.d.sync += [
          //rPhys.col.r := (0xf),
          //rPhys.col.g := (0xf),
          //rPhys.col.b := (0xf),
        //]
        rPhys.col.r := (default -> True)
        rPhys.col.g := (default -> True)
        rPhys.col.b := (default -> True)
      } otherwise { // when (io.en)
        //m.d.sync += [
          rPhys.col := inpCol
        //]
      }
    // Black border
    } otherwise { // when (~misc.visib)
      //m.d.sync += [
        rPhys.col.r := 0x0
        rPhys.col.g := 0x0
        rPhys.col.b := 0x0
      //]
    }
  }
  //--------
  //val rMisc = Reg(LcvVgaCtrlMiscIo())
  //rMisc.init(rMisc.getZero)
  //misc := rMisc
    //m.d.comb += [
      //misc.visib := ((hsc.s == Tstate.VISIB)
      // & (vsc.s == Tstate.VISIB)),
    misc.drawPos.x := hsc.c.resized
    misc.drawPos.y := vsc.c.resized
    misc.size.x := fbSize2d.x
    misc.size.y := fbSize2d.y
    //]
    //m.d.sync += [
    //val rNextVisib = Reg(Bool()) init(False)
    //rNextVisib := ((hsc.nextS === LcvVgaState.visib)
    //  & (vsc.nextS === LcvVgaState.visib))
    //misc.nextVisib := rNextVisib
    misc.nextVisib := ((hsc.nextS === LcvVgaState.visib)
      & (vsc.nextS === LcvVgaState.visib))
    //cover(hsc.nextS === LcvVgaState.sync)

    val rVisib = Reg(Bool()) init(False)
    rVisib := misc.nextVisib
    misc.visib := rVisib

    val rPastVisib = Reg(Bool()) init(False)
    rPastVisib := misc.visib
    misc.pastVisib := rPastVisib

    val rPastDrawPos = Reg(Vec2(LcvVgaCtrlMiscIo.coordElemT()))
    rPastDrawPos.init(rPastDrawPos.getZero)
    rPastDrawPos := misc.drawPos
    misc.pastDrawPos := rPastDrawPos
    //]

  //val hscNextSVisib = Bool() addAttribute("keep")
  //hscNextSVisib := hsc.nextS === LcvVgaState.visib
  //val vscNextSVisib = Bool() addAttribute("keep")
  //vscNextSVisib := vsc.nextS === LcvVgaState.visib
  ////--------
  //GenerationFlags.formal {
  //  when (pastValidAfterReset) {
  //    when (
  //      past(misc.pixelEn)
  //      && hscNextSVisib
  //      && vscNextSVisib
  //      && misc.drawPos.x.resized < htiming.visib
  //      && misc.drawPos.y.resized < vtiming.visib
  //    ) {
  //      assert(misc.nextVisib)
  //    }
  //  }
  //}
}
