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

class LcvVgaPipe(
  vgaTimingHv: LcvVgaTimingHv,
  isVert: Boolean,
  vivadoDebug: Boolean=false,
) //extends Bundle
{
  //--------
  //val s = Reg(LcvVgaState()) init(LcvVgaState.front)
  ////val sWrapNext = s.wrapNext()
  //val c = Reg(UInt(vgaTimingHv.cntWidth() bits)) init(0x0)

  val minAhead = 1
  val maxAhead = 3 
  val numAhead = maxAhead - minAhead + 1
  val pipeSize = numAhead + 1

  val currIdx = 0
  val nextIdx = 1
  val nextNextIdx = 2
  val nextNextNextIdx = 3

  val cntWidth = vgaTimingHv.cntWidth(offs=maxAhead) 
  val rCArr = new ArrayBuffer[UInt]()
  val cToDrive = UInt(cntWidth bits)
  //val nextCArr = new ArrayBuffer[UInt]()
  val rSArr = new ArrayBuffer[LcvVgaState.C]()
  val sToDrive = LcvVgaState()
  //val nextSArr = new ArrayBuffer[LcvVgaState.C]()
  val rVisibArr = new ArrayBuffer[Bool]()
  val visibToDrive = Bool()

  //val rPixelEnArr = new ArrayBuffer[Bool]()
  //val pixelEnToDrive = Bool()
  //val rVisibArr = Reg(UInt(pipeSize bits)) init(0x0)
  //val nextVisibArr = new ArrayBuffer[Bool]()

  for (idx <- 0 to pipeSize - 1) {
    //rCArr += Reg(UInt(cntWidth bits)) init(pipeSize - 1 - idx)
    rCArr += Reg(UInt(cntWidth bits)) init(idx)
    //nextCArr += rCArr.last.wrapNext()
    //nextCArr += UInt(cntWidth bits)

    rSArr += Reg(LcvVgaState()) init(LcvVgaState.front)
    //nextSArr += rSArr.last.wrapNext()
    //nextSArr += LcvVgaState()

    rVisibArr += Reg(Bool()) init(False)
    //nextVisibArr += rVisibArr.last.wrapNext()
    //nextVisibArr += Bool()

    //rPixelEnArr += Reg(Bool()) init(False)

    if (vivadoDebug) {
      if (!isVert) {
        rCArr.last.setName(f"hpipe_rCArr_$idx")
        rSArr.last.setName(f"hpipe_rSArr_$idx")
        rVisibArr.last.setName(f"hpipe_rVisibArr_$idx")
        //rPixelEnArr.last.setName(f"hpipe_rPixelEnArr_$idx")
      } else { // if (isVert)
        rCArr.last.setName(f"vpipe_rCArr_$idx")
        rSArr.last.setName(f"vpipe_rSArr_$idx")
        rVisibArr.last.setName(f"vpipe_rVisibArr_$idx")
        //rPixelEnArr.last.setName(f"vpipe_rPixelEnArr_$idx")
      }
      rCArr.last.addAttribute("MARK_DEBUG", "TRUE")
      rSArr.last.addAttribute("MARK_DEBUG", "TRUE")
      rVisibArr.last.addAttribute("MARK_DEBUG", "TRUE")
      //rPixelEnArr.last.addAttribute("MARK_DEBUG", "TRUE")
    }
  }
  //for (idx <- 1 to pipeSize - 1) {
  //  //nextCArr(idx) := rCArr(idx + 1)
  //  //rCArr(idx) := nextCArr(idx)
  //  when (pixelEnToDrive) {
  //    rCArr(idx - 1) := rCArr(idx)

  //    //nextSArr(idx) := rSArr(idx + 1)
  //    //rSArr(idx) := nextSArr(idx)
  //    rSArr(idx - 1) := rSArr(idx)
  //  }

  //  //nextVisibArr(idx) := rVisibArr(idx + 1)
  //  //rVisibArr(idx) := nextVisibArr(idx)
  //  rVisibArr(idx - 1) := rVisibArr(idx)
  //}
  rCArr.last := cToDrive
  rSArr.last := sToDrive
  rVisibArr.last := visibToDrive
  //rPixelEnArr.last := pixelEnToDrive

  // `<whatever>ToDrive` are the inputs to the pipeline
  val c = rCArr(currIdx)
  val counterP1 = rCArr(1)
  val counterP2 = rCArr(2)
  val counterP3 = rCArr(3)
  //val cToDrive = nextCArr(3 - minAhead)

  //val rNextS = rSArr(1 - minAhead)
  //val rNextNextS = rSArr(2 - minAhead)
  //val rNextNextNextS = rSArr(3 - minAhead)

  val s = rSArr(currIdx)
  val rNextS = rSArr(nextIdx)
  val rNextNextS = rSArr(nextNextIdx)
  val rNextNextNextS = rSArr(nextNextNextIdx)
  //val sToDrive = nextSArr(nextNextNextIdx)

  //val rVisib = rAheadVisibArr(1 - minAhead)
  //val rNextVisib = rAheadVisibArr(2 - minAhead)
  //val rNextNextVisib = rAheadVisibArr(3 - minAhead)

  val rVisib = rVisibArr(currIdx)
  val rNextVisib = rVisibArr(nextIdx)
  val rNextNextVisib = rVisibArr(nextNextIdx)
  val rNextNextNextVisib = rVisibArr(nextNextNextIdx)
  //val visibToDrive = nextVisibArr(nextNextNextIdx)

  //val rPixelEn = rPixelEnArr(currIdx)
  //val rNextPixelEn = rPixelEnArr(nextIdx)
  //val rNextNextPixelEn = rPixelEnArr(nextNextIdx)
  //val rNextNextNextPixelEn = rPixelEnArr(nextNextNextIdx)

  //val cP1Width = vgaTimingHv.cntWidth(offs=1)
  //val cP2Width = vgaTimingHv.cntWidth(offs=2)
  //val cP3Width = vgaTimingHv.cntWidth(offs=3)

  //val counterP1 = UInt(cP1Width bits)
  //counterP1 := c.resized + U(f"$cP1Width'd1")
  //val counterP2 = UInt(cP2Width bits)
  //counterP2 := c.resized + U(f"$cP2Width'd2")
  //val counterP3 = UInt(cP3Width bits)
  //counterP3 := c.resized + U(f"$cP3Width'd3")

  //val cP1 = UInt(cPWidth bits)
  //cP1 := c.resized + U(f"$cPWidth'd1")
  //val cP2 = UInt(cPWidth bits)
  //cP2 := c.resized + U(f"$cPWidth'd2")
  //val cP1 = c.resized + U(f"$cPWidth'd1")
  //val cP2 = c.resized + U(f"$cPWidth'd2")

  //val nextS = LcvVgaState()
  ////val nextS = s.wrapNext()
  //val nextNextS = LcvVgaState()
  //val rNextNextS = Reg(LcvVgaState()) init(LcvVgaState.front)
  ////rNextNextS.init(rNextNextS.getZero) // this didn't compile!
  //rNextNextS := nextNextS
  //val rNextVisib = Reg(Bool()) init(False)
  //val nextNextVisib = rNextVisib.wrapNext()

  //if (vivadoDebug) {
  //  s.addAttribute("MARK_DEBUG", "TRUE")
  //  c.addAttribute("MARK_DEBUG", "TRUE")
  //  counterP1.addAttribute("MARK_DEBUG", "TRUE")
  //  counterP2.addAttribute("MARK_DEBUG", "TRUE")
  //  nextS.addAttribute("MARK_DEBUG", "TRUE")
  //  nextNextS.addAttribute("MARK_DEBUG", "TRUE")
  //  rNextNextS.addAttribute("MARK_DEBUG", "TRUE")
  //}
  //--------
  def sendDownPipe(
    activePixelEn: Bool
  ): Unit = {
    for (idx <- 1 to pipeSize - 1) {
      //nextCArr(idx) := rCArr(idx + 1)
      //rCArr(idx) := nextCArr(idx)
      when (activePixelEn) {
        rCArr(idx - 1) := rCArr(idx)

        //nextSArr(idx) := rSArr(idx + 1)
        //rSArr(idx) := nextSArr(idx)
        rSArr(idx - 1) := rSArr(idx)
      }

      //nextVisibArr(idx) := rVisibArr(idx + 1)
      //rVisibArr(idx) := nextVisibArr(idx)
      rVisibArr(idx - 1) := rVisibArr(idx)

      //rPixelEnArr(idx - 1) := rPixelEnArr(idx)
    }
  }
  def runMkCaseFunc(
    vgaTimingHv: LcvVgaTimingHv,
    someState: LcvVgaState.C,
  )(
    mkCaseFunc: (
      LcvVgaState.C, // `someState` (`is (someState)`)
      Int, // `stateSize`
      LcvVgaState.C, // `nextState`
    ) => Unit
  ): Unit = {
    switch (someState) {
      is (LcvVgaState.front) {
        val stateSize = vgaTimingHv.front
        mkCaseFunc(
          LcvVgaState.front,
          stateSize,
          LcvVgaState.sync
        )
        //println(f"front, sync: $stateSize")
      }
      is (LcvVgaState.sync) {
        val stateSize = vgaTimingHv.sync
        mkCaseFunc(
          LcvVgaState.sync,
          stateSize,
          LcvVgaState.back
        )
        //println(f"sync, back: $stateSize")
      }
      is (LcvVgaState.back) {
        val stateSize = vgaTimingHv.back
        mkCaseFunc(
          LcvVgaState.back,
          stateSize,
          LcvVgaState.visib
        )
        //println(f"back, visib: $stateSize")
      }
      is (LcvVgaState.visib) {
        val stateSize = vgaTimingHv.visib
        mkCaseFunc(
          LcvVgaState.visib,
          stateSize,
          LcvVgaState.front
        )
        //println(f"visib, front: $stateSize")
      }
    }
  }
  //def noChangeUpdateNextS(): Unit = {
  //  nextS := s
  //  //nextNextS := s
  //}
  def noChangeUpdateToDrive(): Unit = {
    cToDrive := rCArr.last
    sToDrive := rSArr.last
    visibToDrive := rVisibArr.last
  }
  def updateStateCnt(
    vgaTimingHv: LcvVgaTimingHv,
  ): Unit = {
    def mkCase(
      //s: LcvVgaState.C,
      currState: LcvVgaState.C,
      stateSize: Int,
      nextState: LcvVgaState.C,
    ): Unit = {
      ////val counterP1 = c + 0x1
			//when (counterP1 >= stateSize) {
			//	//m.d.sync += stateCnt.s := (nextState)
			//	//m.d.sync += stateCnt.c := (0x0)
			//	////m.d.comb += stateCnt.nextS := (nextState)
			//	//sWrapNext := nextState
			//	//s := nextState
			//	nextS := nextState
			//	s := nextState
			//	c := c.getZero
			//	//when (counterP2 >= stateSize) {
			//	//  nextNextS := 
			//	//} otherwise {
			//	//}
			//} otherwise {
			//	//m.d.sync += stateCnt.c := (counterP1)
			//	////self.noChangeUpdateNextS(m, stateCnt)
			//	nextS := s
			//	//nextNextS := s
			//	c := counterP1(c.bitsRange)
			//}
			////s := nextS

			////val counteP2 = c + 0x2
			////when (counterP2 >= stateSize) {
			////  nextNextS := nextState
			////} otherwise {
			////  nextNextS := s
			////}

			////when ((c + 0x2) >= stateSize) {
			////  // `m.d.comb` may have only worked here becaue of `nextVisib`
			////  // being originally registered.
			////	//m.d.comb += stateCnt.nextS := (nextState)
			////	nextS := nextState
			////} otherwise {
			////	//m.d.comb += stateCnt.nextS := (stateCnt.s)
			////	nextS := s
			////}

			// We might have some off-by-one errors here
			//when (counterP3 + 1 >= stateSize) 
			when (counterP3 >= stateSize) {
			  sToDrive := nextState
			  cToDrive := cToDrive.getZero
        visibToDrive := (
          //if (nextState == LcvVgaState.visib) {True} else {False}
          if (currState == LcvVgaState.back) {True} else {False}
        )
			} otherwise {
			  sToDrive := rSArr.last
			  cToDrive := counterP3 + 1
        visibToDrive := (
          //if (nextState == LcvVgaState.front) {True} else {False}
          if (currState == LcvVgaState.visib) {True} else {False}
        )
			}
    }

    runMkCaseFunc(
      vgaTimingHv=vgaTimingHv,
      //someState=s,
      someState=rSArr.last
    )(
      mkCaseFunc=mkCase
    )

    //State = VgaTiming.State
    //switch (s) {
    //  is (LcvVgaState.front) {
    //    val stateSize = vgaTimingHv.front
    //    mkCase(stateSize=stateSize, nextState=LcvVgaState.sync)
    //    //println(f"front, sync: $stateSize")
    //  }
    //  is (LcvVgaState.sync) {
    //    val stateSize = vgaTimingHv.sync
    //    mkCase(stateSize=stateSize, nextState=LcvVgaState.back)
    //    //println(f"sync, back: $stateSize")
    //  }
    //  is (LcvVgaState.back) {
    //    val stateSize = vgaTimingHv.back
    //    mkCase(stateSize=stateSize, nextState=LcvVgaState.visib)
    //    //println(f"back, visib: $stateSize")
    //  }
    //  is (LcvVgaState.visib) {
    //    val stateSize = vgaTimingHv.visib
    //    mkCase(stateSize=stateSize, nextState=LcvVgaState.front)
    //    //println(f"visib, front: $stateSize")
    //  }
    //}
  }
  //def noChangeUpdateNextNextS(): Unit = {
  //  //nextS := s
  //  nextNextS := s
  //}
  //def updateNextNextS(
  //  vgaTimingHv: LcvVgaTimingHv,
  //): Unit = {
  //  def mkCase(
  //    stateSize: Int,
  //    nextNextState: LcvVgaState.C,
  //  ): Unit = {
  //    //when (counterP2 >= stateSize) {
  //    //  nextNextS := nextNextState
  //    //} elsewhen (counterP1 >= stateSize) {
  //    //  nextNextS := nextS
  //    //} otherwise {
  //    //  nextNextS := s
  //    //}
  //    when (counterP2 >= stateSize) {
  //      nextNextS := nextNextState
  //      
  //      nextNextVisib := (
  //        if (nextNextState == LcvVgaState.visib) {True} else {False}
  //      )
  //    } otherwise {
  //      nextNextS := s
  //      nextNextVisib := (
  //        if (nextNextState == LcvVgaState.front) {True} else {False}
  //      )
  //    }
  //  }
  //  //runMkCaseFunc(vgaTimingHv=vgaTimingHv)(mkCaseFunc=mkCase)
  //  runMkCaseFunc(
  //    vgaTimingHv=vgaTimingHv,
  //    //someState=nextS,
  //    // I think this will work? 
  //    // Here's my thinking behind why I think this will work.
  //    // As long as `vgaTimingHv`'s members are larger than 2, which is
  //    // almost certainly true for any real VGA signal, then `s` will 
  //    someState=s, 
  //  )(
  //    mkCaseFunc=mkCase
  //  )
  //  //switch (s) {
  //  //  is (LcvVgaState.front) {
  //  //    val stateSize = vgaTimingHv.front
  //  //    mkCase(stateSize=stateSize, nextNextState=LcvVgaState.sync)
  //  //    //println(f"front, sync: $stateSize")
  //  //  }
  //  //  is (LcvVgaState.sync) {
  //  //    val stateSize = vgaTimingHv.sync
  //  //    mkCase(stateSize=stateSize, nextNextState=LcvVgaState.back)
  //  //    //println(f"sync, back: $stateSize")
  //  //  }
  //  //  is (LcvVgaState.back) {
  //  //    val stateSize = vgaTimingHv.back
  //  //    mkCase(stateSize=stateSize, nextNextState=LcvVgaState.visib)
  //  //    //println(f"back, visib: $stateSize")
  //  //  }
  //  //  is (LcvVgaState.visib) {
  //  //    val stateSize = vgaTimingHv.visib
  //  //    mkCase(stateSize=stateSize, nextNextState=LcvVgaState.front)
  //  //    //println(f"visib, front: $stateSize")
  //  //  }
  //  //}
  //}
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
  val hpipeS = LcvVgaState()
  val hpipeC = UInt(vgaTimingInfo.htiming.cntWidth() bits)
  //val hpipeCP1 = hpipeC.resized + U(f"$cPWidth'd1")
  //val hpipeCP2 = hpipeC.resized + U(f"$cPWidth'd2")
  val hpipeNextS = LcvVgaState()
  val hpipeNextNextS = LcvVgaState()

  val vpipeS = LcvVgaState()
  val vpipeC = UInt(vgaTimingInfo.vtiming.cntWidth() bits)
  //val vpipeCP1 = vpipeC.resized + U(f"$cPWidth'd1")
  //val vpipeCP2 = vpipeC.resized + U(f"$cPWidth'd2")
  val vpipeNextS = LcvVgaState()
  val vpipeNextNextS = LcvVgaState()

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
  val nextNextNextPixelEn = Bool()
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
  //vivadoDebug: Boolean=false,
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
  //if (vivadoDebug) {
  //  en.addAttribute("MARK_DEBUG", "TRUE")
  //  push.addAttribute("MARK_DEBUG", "TRUE")
  //  phys.addAttribute("MARK_DEBUG", "TRUE")
  //  misc.addAttribute("MARK_DEBUG", "TRUE")
  //}
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
  vivadoDebug: Boolean=false,
) extends Component {
  //--------
  val io = LcvVgaCtrlIo(
    clkRate=clkRate,
    rgbConfig=rgbConfig,
    vgaTimingInfo=vgaTimingInfo,
    fifoDepth=fifoDepth,
    //vivadoDebug=vivadoDebug,
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
  if (vivadoDebug) {
    rPhys.addAttribute("MARK_DEBUG", "TRUE")
  }
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
  //val nextClkCnt = UInt(clkCntWidth bits)
  val nextClkCnt = clkCnt.wrapNext()
  if (vivadoDebug) {
    clkCnt.addAttribute("MARK_DEBUG", "TRUE")
    nextClkCnt.addAttribute("MARK_DEBUG", "TRUE")
  }
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
  //clkCnt := nextClkCnt
  // Since this is an alias, use ALL_CAPS for its name.
  // outp.pixelEn = (clkCnt == 0x0)
  //m.d.comb += 
  //misc.pixelEn := clkCnt === 0x0
  val pixelEnNextCycle = Bool()
  pixelEnNextCycle := clkCntP1.resized === cpp
  val rPixelEn = Reg(Bool()) init(False)
  rPixelEn := pixelEnNextCycle
  misc.pixelEn := rPixelEn

  //val nextPixelEn = Bool()
  //misc.nextPixelEn := nextClkCnt === 0x0
  val rNextPixelEn = Reg(Bool()) init(False)
  rNextPixelEn := nextClkCnt === cpp - 1
  misc.nextPixelEn := rNextPixelEn

  val rNextNextPixelEn = Reg(Bool()) init(False)
  rNextNextPixelEn := nextClkCnt === cpp - 2
  misc.nextNextPixelEn := rNextNextPixelEn

  val rNextNextNextPixelEn = Reg(Bool()) init(False)
  // "- 3": with this basic solution, this means there will be a minimum of
  // a 100 MHz `clk` rate for a 25 MHz pixel clock  
  rNextNextNextPixelEn := nextClkCnt === cpp - 3 
  misc.nextNextNextPixelEn := rNextNextNextPixelEn
  if (vivadoDebug) {
    rPixelEn.addAttribute("MARK_DEBUG", "TRUE")
    rNextPixelEn.addAttribute("MARK_DEBUG", "TRUE")
    rNextNextPixelEn.addAttribute("MARK_DEBUG", "TRUE")
    rNextNextNextPixelEn.addAttribute("MARK_DEBUG", "TRUE")
  }

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
  val rPastPixelEn = Reg(Bool()) init(False)
  rPastPixelEn := misc.pixelEn
  //fifoPop.ready := (
  //  //rPastPixelEn && misc.pastVisib && !fifoEmpty
  //  //misc.pixelEn && misc.visib && !fifoEmpty
  //  misc.nextPixelEn && misc.nextVisib && !fifoEmpty
  //  //misc.nextNextPixelEn && misc.nextNextVisib && !fifoEmpty
  //)
  val rFifoPopReady = Reg(Bool()) init(False)
  //val rNextNextPixelEn = Reg(Bool()) init(False)
  ////val rNextNextVisib = Reg(Bool()) init(False)
  ////val rInvFifoEmpty = Reg(Bool()) init(False)
  //rNextNextPixelEn := misc.nextNextPixelEn
  ////rNextNextVisib := misc.nextNextVisib
  ////rInvFifoEmpty := !fifoEmpty

  // `hpipeCOffs` pipeline stage delays
  //val hpipeCOffs = 7
  //val hpipeCPlusOffsWidth = log2Up(vgaTimingInfo.htiming.back + hpipeCOffs)
  //val rHscCPlusOffs = Reg(UInt(hpipeCPlusOffsWidth bits)) init(0x0)
  //val rWillBeHscCVisib = Reg(Bool()) init(False)
  //val rWillBeHscSVisib = Reg(UInt(2 bits)) init(0x0)
  //val rWillBeVscSVisib = Reg(Bool()) init(False)
  //val rNextNextVisib = Reg(Bool()) init(False)
  ////rWillBeHscCVisib := misc.hpipeC + 3 === vgaTimingInfo.htiming.back
  //rHscCPlusOffs := misc.hpipeC.resized + U(f"$hpipeCPlusOffsWidth'd$hpipeCOffs")
  //rWillBeHscCVisib := rHscCPlusOffs === vgaTimingInfo.htiming.back
  //rWillBeHscSVisib(0) := misc.hpipeS === LcvVgaState.back 
  //rWillBeHscSVisib(1) := misc.hpipeS === LcvVgaState.visib
  //rWillBeVscSVisib := misc.vpipeS === LcvVgaState.visib
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
  //val rInvFifoEmptyArr = new ArrayBuffer[Bool]()
  //val rNextNextPixelEnArr = new ArrayBuffer[Bool]()

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
  //  misc.hpipeC === (vgaTimingInfo.htiming.back - fifoPopDelay - 1)
  //)
  //rHscSIsBack := misc.hpipeS === LcvVgaState.back
  //rHscSIsVisib := misc.hpipeS === LcvVgaState.visib
  //rVscSIsVisib := misc.vpipeS === LcvVgaState.visib

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
  //  //rPastPixelEn && misc.pastVisib && !fifoEmpty
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
  //val rTempNextVisib = Reg(Bool()) init(False)
  //val rTempNextPixelEn = Reg(Bool()) init(False)
  //if (vivadoDebug) {
  //  rTempNextVisib.addAttribute("MARK_DEBUG", "TRUE")
  //  rTempNextPixelEn.addAttribute("MARK_DEBUG", "TRUE")
  //}
  //rTempNextVisib := (
  //  (
  //    (
  //      //(misc.hpipeC + 1 === vgaTimingInfo.htiming.back)
  //      (misc.hpipeC >= vgaTimingInfo.htiming.back - 2)
  //      && (misc.hpipeS === LcvVgaState.back)
  //    ) || (
  //      //(misc.hpipeC < vgaTiming.htiming.visib - 2)
  //      //&& 
  //      (misc.hpipeC <= vgaTimingInfo.htiming.visib - 2)
  //      && (misc.hpipeS === LcvVgaState.visib)
  //    )
  //  ) && (
  //    //(
  //    //  ((misc.vpipeC + 2) >= vgaTimingInfo.vtiming.back)
  //    //  && (misc.vpipeS === LcvVgaState.back)
  //    //)
  //    //||
  //    //(misc.vpipeC < vgaTimingInfo.vtiming.visib - 2)
  //    //&& (misc.vpipeNextS === LcvVgaState.visib)
  //    misc.vpipeS === LcvVgaState.visib
  //  )
  //  //|| misc.visib
  //)

  // BEGIN: Mostly working
  //val tempNextVisib = Bool()
  //rTempNextVisib := 
  //tempNextVisib := (
  //  (
  //    (
  //      (misc.hpipeC < vgaTimingInfo.htiming.back)
  //      &&
  //      //(misc.hpipeC >= vgaTimingInfo.htiming.back - 2)
  //      //(misc.hpipeC >= vgaTimingInfo.htiming.back - 3)
  //      //(misc.hpipeC >= vgaTimingInfo.htiming.back - 2)
  //      (misc.hpipeC >= vgaTimingInfo.htiming.back - 1)
  //      && (misc.hpipeS === LcvVgaState.back)
  //    ) || (
  //      //(misc.hpipeC < fbSize2d.x - 2)
  //      //(misc.hpipeC < fbSize2d.x - 2)
  //      //(misc.hpipeC < fbSize2d.x - 1)
  //      (misc.hpipeC < fbSize2d.x - 1)
  //      && (misc.hpipeS === LcvVgaState.visib)
  //    )
  //  ) && (
  //    //misc.vpipeS === LcvVgaState.visib
  //    misc.vpipeS === LcvVgaState.visib
  //  )
  //)
  //val tempHscNextNextVisib = Bool()
  //val tempVscNextNextVisib = Bool()
  //if (vivadoDebug) {
  //  tempHscNextNextVisib.addAttribute("MARK_DEBUG", "TRUE")
  //  tempVscNextNextVisib.addAttribute("MARK_DEBUG", "TRUE")
  //}
  //switch (misc.hpipeS) {
  //  is (LcvVgaState.front) {
  //    tempHscNextNextVisib := False
  //  }
  //  is (LcvVgaState.sync) {
  //    tempHscNextNextVisib := False
  //  }
  //  is (LcvVgaState.back) {
  //    tempHscNextNextVisib := (
  //      (misc.hpipeC === vgaTimingInfo.htiming.back - 2)
  //      || (misc.hpipeC === vgaTimingInfo.htiming.back - 1)
  //    )
  //  }
  //  is (LcvVgaState.visib) {
  //    tempHscNextNextVisib := (
  //      //misc.hpipeC + 0x2 < fbSize2d.x
  //      (misc.hpipeC =/= fbSize2d.x - 2)
  //      && (misc.hpipeC =/= fbSize2d.x - 1)
  //    )
  //  }
  //}
  //switch (misc.vpipeS) {
  //  is (LcvVgaState.front) {
  //    tempVscNextNextVisib := False
  //  }
  //  is (LcvVgaState.sync) {
  //    tempVscNextNextVisib := False
  //  }
  //  is (LcvVgaState.back) {
  //    //tempVscNextNextVisib := (
  //    //  (misc.hpipeC === vgaTimingInfo.htiming.back - 2)
  //    //  || (misc.hpipeC === vgaTimingInfo.htiming.back - 1)
  //    //)
  //    tempVscNextNextVisib := False
  //  }
  //  is (LcvVgaState.visib) {
  //    //tempHscNextNextVisib := (
  //    //  //misc.hpipeC + 0x2 < fbSize2d.x
  //    //  (misc.hpipeC === fbSize2d.x - 2)
  //    //  || (misc.hpipeC === fbSize2d.x - 1)
  //    //)
  //    tempVscNextNextVisib := True
  //  }
  //}

  ////val rTempNextVisib = Reg(Bool()) init(False)
  //rTempNextVisib := tempHscNextNextVisib && tempVscNextNextVisib
  //rTempNextPixelEn := nextClkCnt === cpp - 1

  // END: Mostly working

  //tempNextPixelEn := clkCntP1 === cpp - nextRegDelay + 1
  //jtempNextPixelEn := nextClkCnt === (cpp - nextRegDelay - 1)
  //val rTempNextPixelEn = Reg(Bool()) init(False)
  //rTempNextPixelEn := nextClkCnt === (cpp - 1)
  //rFifoPopReady :=
  rFifoPopReady := (
    misc.nextNextPixelEn && misc.nextNextVisib && !fifoEmpty
  )
  fifoPop.ready := rFifoPopReady
  //fifoPop.ready := (
  //  misc.nextPixelEn && misc.nextVisib && !fifoEmpty
  //)
  // BEGIN: working
  //fifoPop.ready := (
  //  misc.nextPixelEn && misc.nextVisib && !fifoEmpty
  //)
  //fifoPop.ready := (
  //  rTempNextPixelEn && misc.nextVisib && !fifoEmpty
  //)
  // END: working
  // BEGIN: test

  //fifoPop.ready := (
  //  //rTempNextPixelEn && rTempNextVisib && !fifoEmpty
  //  //rNextPixelEn && rNextVisib && !fifoEmpty
  //  rNextPixelEn && misc.nextVisib && !fifoEmpty
  //)

  // END: test
  //rFifoPopReady := (
  //  //misc.nextNextPixelEn && misc.nextNextVisib && !fifoEmpty
  //  //rTempNextVisib && rTempNextPixelEn && !fifoEmpty
  //)
  //fifoPop.ready := rFifoPopReady
  misc.fifoPopReady := fifoPop.ready
  //misc.nextNextPixelEn := clkCntP1 === cpp - 2
  //val rNextNextPixelEn = Reg(Bool()) init(False)
  //rNextNextPixelEn := nextClkCnt === cpp - 2
  //misc.nextNextPixelEn := rNextNextPixelEn
  //misc.nextNextVisib := rNextNextVisib

  //misc.nextNextVisib := (
  //  (
  //    (
  //      //(misc.hpipeC + 1 === vgaTimingInfo.htiming.back)
  //      (misc.hpipeC + 2 === vgaTimingInfo.htiming.back)
  //      && (misc.hpipeS === LcvVgaState.back)
  //    ) || (
  //      misc.hpipeS === LcvVgaState.visib
  //    )
  //  ) && (
  //    //(
  //    //  ((misc.vpipeC + 2) >= vgaTimingInfo.vtiming.back)
  //    //  && (misc.vpipeS === LcvVgaState.back)
  //    //)
  //    //||
  //    misc.vpipeS === LcvVgaState.visib
  //  )
  //  //|| misc.visib
  //)

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
  //loc.hpipe = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.HTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  //loc.vpipe = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.VTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  val hpipe = new LcvVgaPipe(
    vgaTimingHv=htiming,
    isVert=false,
    vivadoDebug=vivadoDebug,
  )
  val vpipe = new LcvVgaPipe(
    vgaTimingHv=vtiming,
    isVert=true,
    vivadoDebug=vivadoDebug,
  )

  misc.hpipeS := hpipe.s
  misc.hpipeC := hpipe.c
  misc.hpipeNextS := hpipe.rNextS
  misc.hpipeNextNextS := hpipe.rNextNextS
  misc.vpipeS := vpipe.s
  misc.vpipeC := vpipe.c
  misc.vpipeNextS := vpipe.rNextS
  misc.vpipeNextNextS := vpipe.rNextNextS
  //--------
  // Implement HSYNC and VSYNC logic
  //when (misc.nextNextPixelEn) {
  //  hpipe.updateNextNextNextS(vgaTimingHv=htiming)
  //  //when (vpipe.rNextNextNextS 
  //  //switch (hpipe.rNextNextNextS) {
  //  //  is (LcvVgaState.front) {
  //  //    vpipe.noChangeUpdateNextNextNextS()
  //  //  }
  //  //  is (LcvVgaState.sync) {
  //  //    vpipe.noChangeUpdateNextNextNextS()
  //  //  }
  //  //  is (LcvVgaState.back) {
  //  //    vpipe.noChangeUpdateNextNextNextS()
  //  //  }
  //  //  is (LcvVgaState.visib) {
  //  //    when (hpipe.counterP3 >= fbSize2d.x) {
  //  //      vpipe.updateNextNextNextS(vgaTimingHv=vtiming)
  //  //    }
  //  //    //otherwise {
  //  //    //  vpipe.noChangeUpdateNextNextNextS()
  //  //    //}
  //  //  }
  //  //}
  //}
  ////otherwise {
  ////  hpipe.noChangeUpdateNextNextNextS()
  ////  vpipe.noChangeUpdateNextNextNextS()
  ////}
  //when (misc.nextPixelEn) {
  //  hpipe.updateNextNextS(vgaTimingHv=htiming)
  //  switch (hpipe.rNextNextS) {
  //    is (LcvVgaState.front) {
  //      vpipe.noChangeUpdateNextNextS()
  //    }
  //    is (LcvVgaState.sync) {
  //      vpipe.noChangeUpdateNextNextS()
  //    }
  //    is (LcvVgaState.back) {
  //      vpipe.noChangeUpdateNextNextS()
  //    }
  //    is (LcvVgaState.visib) {
  //      when (hpipe.counterP2 >= fbSize2d.x) {
  //        vpipe.updateNextNextS(vgaTimingHv=vtiming)
  //      } otherwise {
  //        vpipe.noChangeUpdateNextNextS()
  //      }
  //    }
  //  }
  //} otherwise {
  //  hpipe.noChangeUpdateNextNextS()
  //  vpipe.noChangeUpdateNextNextS()
  //}
  //when (hpipe.nextNextPixelEn) {
  //  hpipe.updateStateCnt(vgaTimingHv=htiming)
  //  when (hpipe.rNextNextNextS === LcvVgaState.visib) {
  //  }
  //}
  // Implement HSYNC and VSYNC logic
  hpipe.sendDownPipe(
    //activePixelEn=misc.nextNextNextPixelEn
    activePixelEn=misc.nextNextPixelEn
  )
  vpipe.sendDownPipe(
    //activePixelEn=misc.nextNextNextPixelEn
    activePixelEn=misc.nextNextPixelEn
  )
  when (
    misc.nextNextNextPixelEn
    //misc.nextNextPixelEn
  ) {
    hpipe.updateStateCnt(vgaTimingHv=htiming)
    when (
      //hpipe.rNextNextS === LcvVgaState.visib
      //hpipe.rNextNextNextVisib
      //&& !hpipe.visibToDrive
      hpipe.rNextVisib
      && !hpipe.rNextNextVisib
    ) {
      vpipe.updateStateCnt(vgaTimingHv=vtiming)
    } otherwise {
      vpipe.noChangeUpdateToDrive()
    }
  } otherwise {
    hpipe.noChangeUpdateToDrive()
    vpipe.noChangeUpdateToDrive()
  }
  when (misc.pixelEn) {
    //hpipe.updateNextNextS(vgaTimingHv=htiming)
    //htiming.updateStateCnt(m, hpipe)
    //hpipe.updateStateCnt(vgaTimingHv=htiming)

    // BEGIN: need this
    //hpipe.updateStateCnt(vgaTimingHv=htiming)

    switch (hpipe.s) {
      is (LcvVgaState.front) {
        //m.d.sync += outp.hsync := (0b1)
        rHsync := True
        ////vtiming.noChangeUpdateNextS(m, vpipe)
        //vpipe.noChangeUpdateNextS()
      }
      is (LcvVgaState.sync) {
        //m.d.sync += outp.hsync := (0b0)
        rHsync := False
        ////vtiming.noChangeUpdateNextS(m, vpipe)
        //vpipe.noChangeUpdateNextS()
      }
      is (LcvVgaState.back) {
        //m.d.sync += outp.hsync := (0b1)
        rHsync := True
        ////vtiming.noChangeUpdateNextS(m, vpipe)
        //vpipe.noChangeUpdateNextS()
      }
      is (LcvVgaState.visib) {
        //m.d.sync += outp.hsync := (0b1)
        rHsync := True
        //when ((hpipe["c"] + 0x1) >= FB_SIZE().x) 
        //when ((hpipe.c + 0x1) >= fbSize2d.x)
        //when ((hpipe.c + 0x1) >= fbSize2d.x) 
        //when (hpipe.counterP1 >= fbSize2d.x) 
        // BEGIN: old, non-pipelined version
        //when (hpipe.counterP1 >= fbSize2d.x) {
        //  //vtiming.updateStateCnt(m, vpipe)
        //  vpipe.updateStateCnt(vgaTimingHv=vtiming)
        //}
        // END: old, non-pipelined version
        //otherwise {
        //  //vtiming.noChangeUpdateNextS(m, vpipe)
        //  //vpipe.noChangeUpdateNextS()
        //  vpipe.noChange
        //}
      }
    }

    switch (vpipe.s) {
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
    // END: need this
  }
  //.otherwise { // when (~misc.pixelEn)
  //  // BEGIN: need this
  //  //htiming.noChangeUpdateNextS(m, hpipe)
  //  //vtiming.noChangeUpdateNextS(m, vpipe)
  //  //hpipe.noChangeUpdateNextS()
  //  //vpipe.noChangeUpdateNextS()
  //  // END: need this
  //}
  //--------
  // Implement drawing the picture

  when (misc.pixelEn) {
    // Visible area
    when (misc.visib) {
      //when (~io.en) {
        //m.d.sync += [
          //phys.col.r := (0xf),
          //phys.col.g := (0xf),
          //phys.col.b := (0xf),
        //]
        rPhys.col.r := (default -> True)
        rPhys.col.g := (default -> True)
        rPhys.col.b := (default -> True)
      //} otherwise { // when (io.en)
      //  //m.d.sync += [
      //    rPhys.col := tempCol
      //  //]
      //}
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
      //misc.visib := ((hpipe.s == Tstate.VISIB)
      // & (vpipe.s == Tstate.VISIB)),
    misc.drawPos.x := hpipe.c.resized
    misc.drawPos.y := vpipe.c.resized
    misc.size.x := fbSize2d.x
    misc.size.y := fbSize2d.y
    //]
    //m.d.sync += [
    //val rNextVisib = Reg(Bool()) init(False)
    //rNextVisib := ((hpipe.nextS === LcvVgaState.visib)
    //  & (vpipe.nextS === LcvVgaState.visib))
    //rNextVisib := ((hpipe.rNextNextS === LcvVgaState.visib)
    //  && (vpipe.rNextNextS === LcvVgaState.visib))
    //rNextVisib := ((hpipe.nextNextS === LcvVgaState.visib)
    //  && (vpipe.nextNextS === LcvVgaState.visib))
    //rNextVisib := hpipe.rNextNextVisib && vpipe.rNextNextVisib
    //rNextVisib := hpipe.rNextVisib && vpipe.rNextVisib

    //misc.nextVisib := rNextVisib
    // BEGIN: stuff
    //misc.nextVisib := hpipe.rNextVisib && vpipe.rNextVisib
    //misc.nextNextVisib := hpipe.rNextNextVisib && vpipe.rNextNextVisib
    misc.nextVisib := hpipe.rNextVisib && vpipe.rNextVisib
    misc.nextNextVisib := hpipe.rNextNextVisib && vpipe.rNextNextVisib
    // END: stuff

    //val rNextVisib = Reg(Bool()) init(False)
    //rNextVisib := hpipe.nextNextVisib && vpipe.nextNextVisib
    //misc.nextVisib := rNextVisib

    //misc.nextVisib := ((hpipe.rNextNextS === LcvVgaState.visib)
    //  && (vpipe.rNextNextS === LcvVgaState.visib))
    //misc.nextVisib := ((hpipe.nextS === LcvVgaState.visib)
    //  & (vpipe.nextS === LcvVgaState.visib))
    //misc.nextVisib := ((hpipe.rNextNextS === LcvVgaState.visib)
    //  && (vpipe.rNextNextS === LcvVgaState.visib))
    //misc.nextVisib := (
    //  hpipe.nextS === LcvVgaState.visib
    //  && vpipe.nextS === LcvVgaState.visib
    //)
    //cover(hpipe.nextS === LcvVgaState.sync)

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

  //val hpipeNextSVisib = Bool() addAttribute("keep")
  //hpipeNextSVisib := hpipe.nextS === LcvVgaState.visib
  //val vpipeNextSVisib = Bool() addAttribute("keep")
  //vpipeNextSVisib := vpipe.nextS === LcvVgaState.visib
  ////--------
  //GenerationFlags.formal {
  //  when (pastValidAfterReset) {
  //    when (
  //      past(misc.pixelEn)
  //      && hpipeNextSVisib
  //      && vpipeNextSVisib
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
  val nextClkCnt = clkCnt.wrapNext()
  // Force this addition to be of width `CLK_CNT_WIDTH + 1` to
  // prevent wrap-around
  val clkCntP1Width = clkCntWidth + 1
  val clkCntP1 = UInt(clkCntP1Width bits)
  clkCntP1 := clkCnt.resized + U(f"$clkCntP1Width'd1")

  // Implement wrap-around for the clock counter
  when (clkCntP1 < cpp) {
    //m.d.sync += 
    nextClkCnt := clkCntP1(clkCnt.bitsRange)
  } otherwise {
    //m.d.sync +=
    nextClkCnt := 0x0
  }
  // Since this is an alias, use ALL_CAPS for its name.
  // outp.pixelEn = (clkCnt == 0x0)
  //m.d.comb += 
  misc.pixelEn := clkCnt === 0x0
  val pixelEnNextCycle = Bool()
  pixelEnNextCycle := clkCntP1.resized === cpp

  val rNextNextPixelEn = Reg(Bool()) init(False)
  rNextNextPixelEn := nextClkCnt === cpp - 2
  misc.nextNextPixelEn := rNextNextPixelEn

  val rNextNextNextPixelEn = Reg(Bool()) init(False)
  rNextNextNextPixelEn := nextClkCnt === cpp - 3
  misc.nextNextNextPixelEn := rNextNextNextPixelEn
  //--------
  // Implement the State/Counter stuff
  //loc.Tstate = VgaTiming.State
  //loc.hpipe = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.HTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  //loc.vpipe = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.VTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  val hpipe = new LcvVgaPipe(
    vgaTimingHv=htiming,
    isVert=false,
  )
  val vpipe = new LcvVgaPipe(
    vgaTimingHv=vtiming,
    isVert=true,
  )
  hpipe.sendDownPipe(
    activePixelEn=misc.nextNextNextPixelEn
  )
  vpipe.sendDownPipe(
    activePixelEn=misc.nextNextNextPixelEn
  )
  misc.hpipeS := hpipe.s
  misc.hpipeC := hpipe.c
  misc.hpipeNextS := hpipe.rNextS
  misc.hpipeNextNextS := hpipe.rNextNextS
  misc.vpipeS := vpipe.s
  misc.vpipeC := vpipe.c
  misc.vpipeNextS := vpipe.rNextS
  misc.vpipeNextNextS := vpipe.rNextNextS
  //--------
  // Implement HSYNC and VSYNC logic
  //when (misc.nextNextNextPixelEn) {
  //  hpipe.updateStateCnt(vgaTimingHv=htiming)
  //  when (
  //    //hpipe.rNextNextS === LcvVgaState.visib
  //    hpipe.rNextNextNextVisib
  //    && !hpipe.visibToDrive
  //  ) {
  //    vpipe.updateStateCnt(vgaTimingHv=vtiming)
  //  }
  //}
  when (misc.nextNextNextPixelEn) {
    hpipe.updateStateCnt(vgaTimingHv=htiming)
    when (
      //hpipe.rNextNextS === LcvVgaState.visib
      hpipe.rNextNextNextVisib
      && !hpipe.visibToDrive
    ) {
      vpipe.updateStateCnt(vgaTimingHv=vtiming)
    } otherwise {
      vpipe.noChangeUpdateToDrive()
    }
  } otherwise {
    hpipe.noChangeUpdateToDrive()
    vpipe.noChangeUpdateToDrive()
  }
  when (misc.pixelEn) {
    //htiming.updateStateCnt(m, hpipe)
    //hpipe.updateStateCnt(vgaTimingHv=htiming)

    switch (hpipe.s) {
      is (LcvVgaState.front) {
        //m.d.sync += outp.hsync := (0b1)
        rPhys.hsync := True
        //vtiming.noChangeUpdateNextS(m, vpipe)
        //vpipe.noChangeUpdateNextS()
      }
      is (LcvVgaState.sync) {
        //m.d.sync += outp.hsync := (0b0)
        rPhys.hsync := False
        //vtiming.noChangeUpdateNextS(m, vpipe)
        //vpipe.noChangeUpdateNextS()
      }
      is (LcvVgaState.back) {
        //m.d.sync += outp.hsync := (0b1)
        rPhys.hsync := True
        //vtiming.noChangeUpdateNextS(m, vpipe)
        //vpipe.noChangeUpdateNextS()
      }
      is (LcvVgaState.visib) {
        //m.d.sync += outp.hsync := (0b1)
        rPhys.hsync := True
        //when ((hpipe["c"] + 0x1) >= FB_SIZE().x) 
        //when ((hpipe.c + 0x1) >= fbSize2d.x)
        //when ((hpipe.c + 0x1) >= fbSize2d.x) {
        //  //vtiming.updateStateCnt(m, vpipe)
        //  vpipe.updateStateCnt(vtiming)
        //} otherwise {
        //  //vtiming.noChangeUpdateNextS(m, vpipe)
        //  vpipe.noChangeUpdateNextS()
        //}
      }
    }

    switch (vpipe.s) {
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
  }
  //.otherwise { // when (~misc.pixelEn)
  //  //htiming.noChangeUpdateNextS(m, hpipe)
  //  //vtiming.noChangeUpdateNextS(m, vpipe)
  //  hpipe.noChangeUpdateNextS()
  //  vpipe.noChangeUpdateNextS()
  //}
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
      //misc.visib := ((hpipe.s == Tstate.VISIB)
      // & (vpipe.s == Tstate.VISIB)),
    misc.drawPos.x := hpipe.c.resized
    misc.drawPos.y := vpipe.c.resized
    misc.size.x := fbSize2d.x
    misc.size.y := fbSize2d.y
    //]
    //m.d.sync += [
    //val rNextVisib = Reg(Bool()) init(False)
    //rNextVisib := ((hpipe.nextS === LcvVgaState.visib)
    //  & (vpipe.nextS === LcvVgaState.visib))
    //misc.nextVisib := rNextVisib
    //misc.nextVisib := ((hpipe.nextS === LcvVgaState.visib)
    //  & (vpipe.nextS === LcvVgaState.visib))
    misc.nextVisib := ((hpipe.rNextS === LcvVgaState.visib)
      & (vpipe.rNextS === LcvVgaState.visib))
    //cover(hpipe.nextS === LcvVgaState.sync)

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

  //val hpipeNextSVisib = Bool() addAttribute("keep")
  //hpipeNextSVisib := hpipe.nextS === LcvVgaState.visib
  //val vpipeNextSVisib = Bool() addAttribute("keep")
  //vpipeNextSVisib := vpipe.nextS === LcvVgaState.visib
  ////--------
  //GenerationFlags.formal {
  //  when (pastValidAfterReset) {
  //    when (
  //      past(misc.pixelEn)
  //      && hpipeNextSVisib
  //      && vpipeNextSVisib
  //      && misc.drawPos.x.resized < htiming.visib
  //      && misc.drawPos.y.resized < vtiming.visib
  //    ) {
  //      assert(misc.nextVisib)
  //    }
  //  }
  //}
}
