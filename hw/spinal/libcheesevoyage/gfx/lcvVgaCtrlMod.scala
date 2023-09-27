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
  val idxPipe1 = 1
  val idxPipe2 = 2
  val idxPipe3 = 3

  //val cntWidth = vgaTimingHv.cntWidth(offs=maxAhead) 
  val cntWidth = vgaTimingHv.cntWidth(offs=2)
  val rCPipe = new ArrayBuffer[UInt]()
  val cToDrive = UInt(cntWidth bits) // next value of `rCPipe.last`
  //val cPipe1 = new ArrayBuffer[UInt]()
  val rSPipe = new ArrayBuffer[LcvVgaState.C]()
  val sToDrive = LcvVgaState() // next value of `rSPipe.last`

  //val rNextSPipe = new ArrayBuffer[LcvVgaState.C]()
  //val nextSToDrive = LcvVgaState()

  //val sPipe1 = new ArrayBuffer[LcvVgaState.C]()
  val rVisibPipe = new ArrayBuffer[Bool]()
  val visibToDrive = Bool() // next value of `rVisibPipe.last`

  //val rPixelEnArr = new ArrayBuffer[Bool]()
  //val pixelEnToDrive = Bool()
  //val rVisibPipe = Reg(UInt(pipeSize bits)) init(0x0)
  //val visibPipe1 = new ArrayBuffer[Bool]()

  for (idx <- 0 to pipeSize - 1) {
    //rCPipe += Reg(UInt(cntWidth bits)) init(pipeSize - 1 - idx)
    // old code used `init(idx)`, but I now believe that was incorrect
    //rCPipe += Reg(UInt(cntWidth bits)) init(idx)
    rCPipe += Reg(UInt(cntWidth bits)) init(0x0)
    //cPipe1 += rCPipe.last.wrapNext()
    //cPipe1 += UInt(cntWidth bits)

    rSPipe += Reg(LcvVgaState()) init(LcvVgaState.front)
    //sPipe1 += rSPipe.last.wrapNext()
    //sPipe1 += LcvVgaState()
    //rNextSPipe += Reg(LcvVgaState()) init(LcvVgaState.front)

    rVisibPipe += Reg(Bool()) init(False)
    //visibPipe1 += rVisibPipe.last.wrapNext()
    //visibPipe1 += Bool()

    //rPixelEnArr += Reg(Bool()) init(False)
    if (!isVert) {
      rCPipe.last.setName(f"hpipe_rCPipe_$idx")
      rSPipe.last.setName(f"hpipe_rSPipe_$idx")
      rVisibPipe.last.setName(f"hpipe_rVisibPipe_$idx")
      //rPixelEnPipe.last.setName(f"hpipe_rPixelEnPipe_$idx")
    } else { // if (isVert)
      rCPipe.last.setName(f"vpipe_rCPipe_$idx")
      rSPipe.last.setName(f"vpipe_rSPipe_$idx")
      rVisibPipe.last.setName(f"vpipe_rVisibPipe_$idx")
      //rPixelEnPipe.last.setName(f"vpipe_rPixelEnPipe_$idx")
    }

    if (vivadoDebug) {
      rCPipe.last.addAttribute("MARK_DEBUG", "TRUE")
      rSPipe.last.addAttribute("MARK_DEBUG", "TRUE")
      rVisibPipe.last.addAttribute("MARK_DEBUG", "TRUE")
      //rPixelEnArr.last.addAttribute("MARK_DEBUG", "TRUE")
    }
  }
  //for (idx <- 1 to pipeSize - 1) {
  //  //cPipe1(idx) := rCPipe(idx + 1)
  //  //rCPipe(idx) := cPipe1(idx)
  //  when (pixelEnToDrive) {
  //    rCPipe(idx - 1) := rCPipe(idx)

  //    //sPipe1(idx) := rSPipe(idx + 1)
  //    //rSPipe(idx) := sPipe1(idx)
  //    rSPipe(idx - 1) := rSPipe(idx)
  //  }

  //  //visibPipe1(idx) := rVisibPipe(idx + 1)
  //  //rVisibPipe(idx) := visibPipe1(idx)
  //  rVisibPipe(idx - 1) := rVisibPipe(idx)
  //}
  rCPipe.last := cToDrive
  rSPipe.last := sToDrive
  rVisibPipe.last := visibToDrive
  //rPixelEnArr.last := pixelEnToDrive

  // `<whatever>ToDrive` are the inputs to the pipeline
  val c = rCPipe(currIdx)
  val rCPipe1 = rCPipe(1)
  val rCPipe2 = rCPipe(2)
  val rCPipe3 = rCPipe(3)

  //val rCPlus1 = Reg(UInt(c.getWidth bits)) init(0x1)
  //val cToDrive = cPipe1(3 - minAhead)

  //val rSPipe1 = rSPipe(1 - minAhead)
  //val rSPipe2 = rSPipe(2 - minAhead)
  //val rSPipe3 = rSPipe(3 - minAhead)

  val s = rSPipe(currIdx)
  val rSPipe1 = rSPipe(idxPipe1)
  val rSPipe2 = rSPipe(idxPipe2)
  val rSPipe3 = rSPipe(idxPipe3)
  //val sToDrive = sPipe1(idxPipe3)

  //val nextS = rNextSPipe(currIdx)
  //val rNextSPipe1 = rNextSPipe(idxPipe1)
  //val rNextSPipe2 = rNextSPipe(idxPipe2)
  //val rNextSPipe3 = rNextSPipe(idxPipe3)

  //val rVisib = rAheadVisibArr(1 - minAhead)
  //val rVisibPipe1 = rAheadVisibArr(2 - minAhead)
  //val rVisibPipe2 = rAheadVisibArr(3 - minAhead)

  val rVisib = rVisibPipe(currIdx)
  val rVisibPipe1 = rVisibPipe(idxPipe1)
  val rVisibPipe2 = rVisibPipe(idxPipe2)
  val rVisibPipe3 = rVisibPipe(idxPipe3)
  //val visibToDrive = visibPipe1(idxPipe3)

  //val rPixelEn = rPixelEnArr(currIdx)
  //val rPixelEnPipe1 = rPixelEnArr(idxPipe1)
  //val rPixelEnPipe2 = rPixelEnArr(idxPipe2)
  //val rPixelEnPipe3 = rPixelEnArr(idxPipe3)

  //val cP1Width = vgaTimingHv.cntWidth(offs=1)
  //val cP2Width = vgaTimingHv.cntWidth(offs=2)
  //val cP3Width = vgaTimingHv.cntWidth(offs=3)

  //val rCPipe1 = UInt(cP1Width bits)
  //rCPipe1 := c.resized + U(f"$cP1Width'd1")
  //val rCPipe2 = UInt(cP2Width bits)
  //rCPipe2 := c.resized + U(f"$cP2Width'd2")
  //val rCPipe3 = UInt(cP3Width bits)
  //rCPipe3 := c.resized + U(f"$cP3Width'd3")

  //val cP1 = UInt(cPWidth bits)
  //cP1 := c.resized + U(f"$cPWidth'd1")
  //val cP2 = UInt(cPWidth bits)
  //cP2 := c.resized + U(f"$cPWidth'd2")
  //val cP1 = c.resized + U(f"$cPWidth'd1")
  //val cP2 = c.resized + U(f"$cPWidth'd2")

  //val sPipe1 = LcvVgaState()
  ////val sPipe1 = s.wrapNext()
  //val sPipe2 = LcvVgaState()
  //val rSPipe2 = Reg(LcvVgaState()) init(LcvVgaState.front)
  ////rSPipe2.init(rSPipe2.getZero) // this didn't compile!
  //rSPipe2 := sPipe2
  //val rVisibPipe1 = Reg(Bool()) init(False)
  //val visibPipe2 = rVisibPipe1.wrapNext()

  //if (vivadoDebug) {
  //  s.addAttribute("MARK_DEBUG", "TRUE")
  //  c.addAttribute("MARK_DEBUG", "TRUE")
  //  rCPipe1.addAttribute("MARK_DEBUG", "TRUE")
  //  rCPipe2.addAttribute("MARK_DEBUG", "TRUE")
  //  sPipe1.addAttribute("MARK_DEBUG", "TRUE")
  //  sPipe2.addAttribute("MARK_DEBUG", "TRUE")
  //  rSPipe2.addAttribute("MARK_DEBUG", "TRUE")
  //}
  //--------
  //def sendDownPipe(
  //  //activePixelEn: Bool
  //): Unit = {
  //  for (idx <- 1 to pipeSize - 1) {
  //    //cPipe1(idx) := rCPipe(idx + 1)
  //    //rCPipe(idx) := cPipe1(idx)
  //    //when (activePixelEn) {
  //      rCPipe(idx - 1) := rCPipe(idx)

  //      //sPipe1(idx) := rSPipe(idx + 1)
  //      //rSPipe(idx) := sPipe1(idx)
  //      rSPipe(idx - 1) := rSPipe(idx)

  //      rVisibPipe(idx - 1) := rVisibPipe(idx)
  //    //}

  //    //visibPipe1(idx) := rVisibPipe(idx + 1)
  //    //rVisibPipe(idx) := visibPipe1(idx)
  //    //rVisibPipe(idx - 1) := rVisibPipe(idx)

  //    //rPixelEnArr(idx - 1) := rPixelEnArr(idx)
  //  }
  //}
  def runMkCaseFunc(
    vgaTimingHv: LcvVgaTimingHv,
    somePixelEn: Bool,
    someState: LcvVgaState.C,
  )(
    mkCaseFunc: (
      Bool, // `somePixelEn`
      LcvVgaState.E, // `someState` (`is (someState)`)
      Int, // `stateSize`
      LcvVgaState.E, // `nextState`
    ) => Unit
  ): Unit = {
    switch (someState) {
      is (LcvVgaState.front) {
        val stateSize = vgaTimingHv.front
        mkCaseFunc(
          somePixelEn,
          LcvVgaState.front,
          stateSize,
          LcvVgaState.sync
        )
        //println(f"front, sync: $stateSize")
      }
      is (LcvVgaState.sync) {
        val stateSize = vgaTimingHv.sync
        mkCaseFunc(
          somePixelEn,
          LcvVgaState.sync,
          stateSize,
          LcvVgaState.back
        )
        //println(f"sync, back: $stateSize")
      }
      is (LcvVgaState.back) {
        val stateSize = vgaTimingHv.back
        mkCaseFunc(
          somePixelEn,
          LcvVgaState.back,
          stateSize,
          LcvVgaState.visib
        )
        //println(f"back, visib: $stateSize")
      }
      is (LcvVgaState.visib) {
        val stateSize = vgaTimingHv.visib
        mkCaseFunc(
          somePixelEn,
          LcvVgaState.visib,
          stateSize,
          LcvVgaState.front
        )
        //println(f"visib, front: $stateSize")
      }
    }
  }
  //def noChangeUpdateNextS(): Unit = {
  //  sPipe1 := s
  //  //sPipe2 := s
  //}
  def noChangeUpdateToDrive(): Unit = {
    cToDrive := rCPipe.last
    sToDrive := rSPipe.last
    visibToDrive := rVisibPipe.last
  }
  def updateStateCnt(
    vgaTimingHv: LcvVgaTimingHv,
    somePixelEn: Bool,
  ): Unit = {
    def mkCase(
      //s: LcvVgaState.C,
      somePixelEn: Bool,
      currState: LcvVgaState.E,
      stateSize: Int,
      nextState: LcvVgaState.E,
    ): Unit = {
      ////val rCPipe1 = c + 0x1
			//when (rCPipe1 >= stateSize) {
			//	//m.d.sync += stateCnt.s := (nextState)
			//	//m.d.sync += stateCnt.c := (0x0)
			//	////m.d.comb += stateCnt.sPipe1 := (nextState)
			//	//sWrapNext := nextState
			//	//s := nextState
			//	sPipe1 := nextState
			//	s := nextState
			//	c := c.getZero
			//	//when (rCPipe2 >= stateSize) {
			//	//  sPipe2 := 
			//	//} otherwise {
			//	//}
			//} otherwise {
			//	//m.d.sync += stateCnt.c := (rCPipe1)
			//	////self.noChangeUpdateNextS(m, stateCnt)
			//	sPipe1 := s
			//	//sPipe2 := s
			//	c := rCPipe1(c.bitsRange)
			//}
			////s := sPipe1

			////val counteP2 = c + 0x2
			////when (rCPipe2 >= stateSize) {
			////  sPipe2 := nextState
			////} otherwise {
			////  sPipe2 := s
			////}

			////when ((c + 0x2) >= stateSize) {
			////  // `m.d.comb` may have only worked here becaue of `visibPipe1`
			////  // being originally registered.
			////	//m.d.comb += stateCnt.sPipe1 := (nextState)
			////	sPipe1 := nextState
			////} otherwise {
			////	//m.d.comb += stateCnt.sPipe1 := (stateCnt.s)
			////	sPipe1 := s
			////}
      //for (idx <- 1 to pipeSize - 1) {
      //  rCPipe(idx - 1) := rCPipe(idx)

      //  //sPipe1(idx) := rSPipe(idx + 1)
      //  //rSPipe(idx) := sPipe1(idx)
      //  rSPipe(idx - 1) := rSPipe(idx)

      //  rVisibPipe(idx - 1) := rVisibPipe(idx)
      //}

			// We might have some off-by-one errors here
			when (somePixelEn) {
        for (idx <- 1 to pipeSize - 1) {
          rCPipe(idx - 1) := rCPipe(idx)

          //sPipe1(idx) := rSPipe(idx + 1)
          //rSPipe(idx) := sPipe1(idx)
          rSPipe(idx - 1) := rSPipe(idx)

          rVisibPipe(idx - 1) := rVisibPipe(idx)
        }
			  //val cmpWidth = max(
			  //  log2Up(1 << (rCPipe3.getWidth + 1)), log2Up(stateSize + 1)
			  //)
        when (
          rCPipe3.resized + U(f"$cntWidth'd1")
          >= U(f"$cntWidth'd$stateSize")
        ) {
          sToDrive := nextState
          cToDrive := cToDrive.getZero
          visibToDrive := (
            //if (nextState == LcvVgaState.visib) {True} else {False}
            if (currState == LcvVgaState.back) {True} else {False}
          )
        } otherwise {
          sToDrive := rSPipe.last
          cToDrive := rCPipe.last + 1
          visibToDrive := (
            //if (nextState == LcvVgaState.front) {True} else {False}
            if (currState == LcvVgaState.visib) {True} else {False}
          )
        }
      } otherwise {
        sToDrive := rSPipe.last
        cToDrive := rCPipe.last
        //cToDrive := rCPipe.last + 1
        visibToDrive := rVisibPipe.last
      }
    }

    runMkCaseFunc(
      vgaTimingHv=vgaTimingHv,
      somePixelEn=somePixelEn,
      //someState=s,
      someState=rSPipe.last
    )(
      mkCaseFunc=mkCase
    )
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
  val pixelEnPipe3 = Bool()
  val pixelEnPipe2 = Bool()
  val pixelEnPipe1 = Bool()
  val pixelEn = Bool()
  val visibPipe3 = Bool()
  val visibPipe2 = Bool()
  val visibPipe1 = Bool()
  val visib = Bool()
  val pastVisib = Bool()
  val drawPos = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  //val drawPosPipe1 = Vec2(LcvVgaCtrlMiscIo.coordElemT())
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

  //val pixelEnPipe1 = Bool()
  //misc.pixelEnPipe1 := nextClkCnt === 0x0
  val rPixelEnPipe1 = Reg(Bool()) init(False)
  rPixelEnPipe1 := nextClkCnt === cpp - 1
  misc.pixelEnPipe1 := rPixelEnPipe1

  val rPixelEnPipe2 = Reg(Bool()) init(False)
  rPixelEnPipe2 := nextClkCnt === cpp - 2
  misc.pixelEnPipe2 := rPixelEnPipe2

  val rPixelEnPipe3 = Reg(Bool()) init(False)
  // "- 3": with this basic solution, this means there will be a minimum of
  // a 100 MHz `clk` rate for a 25 MHz pixel clock  
  val nextPixelEnPipe3 = nextClkCnt === cpp - 3
  //rPixelEnPipe3 := nextClkCnt === cpp - 3 
  rPixelEnPipe3 := nextPixelEnPipe3
  misc.pixelEnPipe3 := rPixelEnPipe3
  if (vivadoDebug) {
    rPixelEn.addAttribute("MARK_DEBUG", "TRUE")
    rPixelEnPipe1.addAttribute("MARK_DEBUG", "TRUE")
    rPixelEnPipe2.addAttribute("MARK_DEBUG", "TRUE")
    rPixelEnPipe3.addAttribute("MARK_DEBUG", "TRUE")
  }

  //val rPastFifoPopReady = Reg(Bool()) init(False)
  //rPastFifoPopReady := fifoPop.ready
  //fifoPop.ready := misc.pixelEnPipe1 & misc.visibPipe1 & ~rPastFifoPopReady
  //fifoPop.ready := misc.pixelEnPipe1 & misc.visibPipe1 & ~rPastFifoPopReady
  //fifoPop.ready := misc.pixelEnPipe1 & misc.visibPipe1
  //val rFifoPopReady = Reg(Bool()) init(False)

  //fifoPop.ready := (
  //  //pixelEnNextCycle
  //  //&& (misc.drawPosPipe1.x === 0)
  //  misc.pixelEnPipe1 && misc.visibPipe1
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
  //  misc.pixelEnPipe1 && misc.visibPipe1 && !fifoEmpty
  //  //misc.pixelEnPipe2 && misc.visibPipe2 && !fifoEmpty
  //)
  val rFifoPopReady = Reg(Bool()) init(False)
  //val rPixelEnPipe2 = Reg(Bool()) init(False)
  ////val rVisibPipe2 = Reg(Bool()) init(False)
  ////val rInvFifoEmpty = Reg(Bool()) init(False)
  //rPixelEnPipe2 := misc.pixelEnPipe2
  ////rVisibPipe2 := misc.visibPipe2
  ////rInvFifoEmpty := !fifoEmpty

  // `hpipeCOffs` pipeline stage delays
  //val hpipeCOffs = 7
  //val hpipeCPlusOffsWidth = log2Up(vgaTimingInfo.htiming.back + hpipeCOffs)
  //val rHscCPlusOffs = Reg(UInt(hpipeCPlusOffsWidth bits)) init(0x0)
  //val rWillBeHscCVisib = Reg(Bool()) init(False)
  //val rWillBeHscSVisib = Reg(UInt(2 bits)) init(0x0)
  //val rWillBeVscSVisib = Reg(Bool()) init(False)
  //val rVisibPipe2 = Reg(Bool()) init(False)
  ////rWillBeHscCVisib := misc.hpipeC + 3 === vgaTimingInfo.htiming.back
  //rHscCPlusOffs := misc.hpipeC.resized + U(f"$hpipeCPlusOffsWidth'd$hpipeCOffs")
  //rWillBeHscCVisib := rHscCPlusOffs === vgaTimingInfo.htiming.back
  //rWillBeHscSVisib(0) := misc.hpipeS === LcvVgaState.back 
  //rWillBeHscSVisib(1) := misc.hpipeS === LcvVgaState.visib
  //rWillBeVscSVisib := misc.vpipeS === LcvVgaState.visib
  //rVisibPipe2 := (
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
  //val rPixelEnPipe2 = new ArrayBuffer[Bool]()

  //for (idx <- 0 to fifoPopDelay - 1) {
  //  rInvFifoEmptyArr += Reg(Bool()) init(False)
  //  rInvFifoEmptyArr(idx).setName(f"rInvFifoEmptyArr_$idx")
  //  rPixelEnPipe2 += Reg(Bool()) init(False)
  //  rPixelEnPipe2(idx).setName(f"rPixelEnArr_Pipe1$idx")
  //  if (idx == 0) {
  //    rInvFifoEmptyArr(idx) := !fifoEmpty
  //    rPixelEnPipe2(idx) := clkCntP1 === cpp - 2 - fifoPopDelay
  //    //rPixelEnPipe2(idx) := clkCntP1 === cpp - 2 - (fifoPopDelay - 1)
  //    //rPixelEnPipe2(idx) := clkCntP1 === cpp - fifoPopDelay
  //  } else {
  //    rInvFifoEmptyArr(idx) := rInvFifoEmptyArr(idx - 1)
  //    rPixelEnPipe2(idx) := rPixelEnPipe2(idx - 1)
  //  }
  //}

  // delay 3
  //rHscCIsLastBack := (
  //  misc.hpipeC === (vgaTimingInfo.htiming.back - fifoPopDelay - 1)
  //)
  //rHscSIsBack := misc.hpipeS === LcvVgaState.back
  //rHscSIsVisib := misc.hpipeS === LcvVgaState.visib
  //rVscSIsVisib := misc.vpipeS === LcvVgaState.visib

  ////val rPixelEnPipe2 = Reg(Bool()) init(False)
  //val rVisibPipe2 = Reg(Bool()) init(False)
  //// delay 2
  //rVisibPipe2 := (
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
  //  //misc.pixelEnPipe1 && misc.visibPipe1 && !fifoEmpty
  //  //misc.pixelEnPipe2 && misc.visibPipe2 && !fifoEmpty
  //  // delay 1
  //  rPixelEnPipe2.last && rVisibPipe2 && rInvFifoEmptyArr.last
  //)
  //fifoPop.ready := (
  //  //misc.pixelEnPipe2 && misc.visibPipe2 && !fifoEmpty
  //  misc.pixelEnPipe1 && misc.visibPipe1 && !fifoEmpty
  //)
  //val rFifoPopReady = Reg(Bool()) init(False)
  //val regDelayPipe1 = 2

  //jtempNextPixelEn := nextClkCnt === (cpp - regDelayPipe1 - 1)
  //val rTempNextPixelEn = Reg(Bool()) init(False)
  //rTempNextPixelEn := nextClkCnt === (cpp - 1)
  // BEGIN: pipelined working (?)
  //rFifoPopReady := 
  fifoPop.ready := (
    //misc.pixelEnPipe2 && misc.visibPipe2 && !fifoEmpty
    //misc.pixelEnPipe3 && misc.visibPipe3 && !fifoEmpty
    //hpipe.visibToDrive
    //misc.pixelEnPipe1 && misc.visibPipe1 && !fifoEmpty
    misc.pixelEn && misc.visib && !fifoEmpty
  )
  // END: pipelined working (?)
  //fifoPop.ready := rFifoPopReady
  //fifoPop.ready := (
  //  misc.pixelEnPipe1 && misc.visibPipe1 && !fifoEmpty
  //)
  // BEGIN: working
  //fifoPop.ready := (
  //  misc.pixelEnPipe1 && misc.visibPipe1 && !fifoEmpty
  //)
  //fifoPop.ready := (
  //  rTempNextPixelEn && misc.visibPipe1 && !fifoEmpty
  //)
  // END: working
  // BEGIN: test

  //fifoPop.ready := (
  //  //rTempNextPixelEn && rTempNextVisib && !fifoEmpty
  //  //rPixelEnPipe1 && rVisibPipe1 && !fifoEmpty
  //  rPixelEnPipe1 && misc.visibPipe1 && !fifoEmpty
  //)

  // END: test
  //rFifoPopReady := (
  //  //misc.pixelEnPipe2 && misc.visibPipe2 && !fifoEmpty
  //  //rTempNextVisib && rTempNextPixelEn && !fifoEmpty
  //)
  //fifoPop.ready := rFifoPopReady
  misc.fifoPopReady := fifoPop.ready
  //misc.pixelEnPipe2 := clkCntP1 === cpp - 2
  //val rPixelEnPipe2 = Reg(Bool()) init(False)
  //rPixelEnPipe2 := nextClkCnt === cpp - 2
  //misc.pixelEnPipe2 := rPixelEnPipe2
  //misc.visibPipe2 := rVisibPipe2

  //misc.visibPipe2 := (
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
  //  //misc.pixelEnPipe1
  //  //misc.pixelEnPipe2 && misc.visibPipe2
  //  //fifoPop.valid 
  //  //(clkCnt === (cpp - 3))
  //  //(nextClkCnt === (cpp - 3))
  //  //(nextClkCnt === cpp - 1)
  //  //(clkCntP1 === cpp)
  //  misc.pixelEnPipe2
  //  && misc.visibPipe2
  //)
  //rFifoPopReady := (
  //  misc.pixelEnPipe1 && misc.visibPipe1
  //)
  //fifoPop.ready := rFifoPopReady

  //fifoPop.ready := rFifoPopReady
  //rFifoPopReady := False
  //when (fifoPop.valid) {
  //  when (misc.pixelEnPipe1) {
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
  //  "_sPipe1": Signal(width_from_len(loc.Tstate)),
  //}
  //loc.vpipe = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.VTIMING().COUNTER_WIDTH()),
  //  "_sPipe1": Signal(width_from_len(loc.Tstate)),
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
  misc.hpipeNextS := hpipe.rSPipe1
  misc.hpipeNextNextS := hpipe.rSPipe2
  misc.vpipeS := vpipe.s
  misc.vpipeC := vpipe.c
  misc.vpipeNextS := vpipe.rSPipe1
  misc.vpipeNextNextS := vpipe.rSPipe2

  misc.visibPipe3 := (
    hpipe.rVisibPipe3 && vpipe.rVisibPipe3
  )
  //--------
  // Implement HSYNC and VSYNC logic
  //when (misc.pixelEnPipe2) {
  //  hpipe.updateNextNextNextS(vgaTimingHv=htiming)
  //  //when (vpipe.rSPipe3 
  //  //switch (hpipe.rSPipe3) {
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
  //  //    when (hpipe.rCPipe3 >= fbSize2d.x) {
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
  //when (misc.pixelEnPipe1) {
  //  hpipe.updateNextNextS(vgaTimingHv=htiming)
  //  switch (hpipe.rSPipe2) {
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
  //      when (hpipe.rCPipe2 >= fbSize2d.x) {
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
  //when (hpipe.pixelEnPipe2) {
  //  hpipe.updateStateCnt(vgaTimingHv=htiming)
  //  when (hpipe.rSPipe3 === LcvVgaState.visib) {
  //  }
  //}
  // Implement HSYNC and VSYNC logic
  //hpipe.sendDownPipe(
  //  //activePixelEn=misc.pixelEnPipe3
  //  activePixelEn=misc.pixelEnPipe2
  //)
  //vpipe.sendDownPipe(
  //  //activePixelEn=misc.pixelEnPipe3
  //  activePixelEn=misc.pixelEnPipe2
  //)

  //when (
  //  misc.pixelEnPipe3
  //  //misc.pixelEnPipe2
  //) {
  //  hpipe.updateStateCnt(
  //    vgaTimingHv=htiming,
  //    somePixelEn=misc.pixelEnPipe3
  //  )
  //  when (
  //    //hpipe.rSPipe2 === LcvVgaState.visib
  //    //hpipe.rVisibPipe3
  //    //&& !hpipe.visibToDrive
  //    //hpipe.rVisibPipe1
  //    //&& !hpipe.rVisibPipe2
  //    //hpipe.rVisibPipe3
  //    // BEGIN: first guess
  //    //hpipe.rSPipe.last === LcvVgaState.visib
  //    //&& hpipe.sToDrive =/= LcvVgaState.visib
  //    // END: first guess
  //    // BEGIN: more optimized version
  //    //hpipe.rVisibPipe.last
  //    //&& !hpipe.visibToDrive
  //    // END: more optimized version
  //    // BEGIN: possibly correct? 
  //    hpipe.rVisibPipe(hpipe.currIdx)
  //    && !hpipe.rVisibPipe(hpipe.idxPipe1)
  //    // ENG: possibly correct? 

  //    // BEGIN: also try this one 
  //    //hpipe.rSPipe.last =/= LcvVgaState.visib
  //    //&& hpipe.rSPipe(hpipe.rSPipe.size - 2) == LcvVgaState.visib
  //    // END: also try this one 
  //  ) {
  //    vpipe.updateStateCnt(vgaTimingHv=vtiming)
  //  } otherwise {
  //    vpipe.noChangeUpdateToDrive()
  //  }
  //} otherwise {
  //  hpipe.noChangeUpdateToDrive()
  //  vpipe.noChangeUpdateToDrive()
  //}
  hpipe.updateStateCnt(
    vgaTimingHv=htiming,
    somePixelEn=misc.pixelEnPipe3
    //somePixelEn=nextPixelEnPipe3
  )
  //when (nextPixelEnPipe3) 
  when (misc.pixelEnPipe3)
  {
    when (
      //hpipe.rVisibPipe(hpipe.rVisibPipe.size - 2)
      //&& !hpipe.rVisibPipe.last
      //hpipe.sToDrive =/= LcvVgaState.visib
      //&& hpipe.rSPipe.last === LcvVgaState.visib
      //hpipe.rSPipe.last =/= LcvVgaState.visib
      //&& hpipe.rSPipe(hpipe.rSPipe.size - 2) === LcvVgaState.visib
      //hpipe.rSPipe.last === LcvVgaState.visib
      //&& hpipe.cToDrive >= fbSize2d.x
      hpipe.rSPipe.last === LcvVgaState.visib
      && hpipe.rCPipe.last + 1 >= fbSize2d.x
    ) {
      vpipe.updateStateCnt(
        vgaTimingHv=vtiming,
        somePixelEn=misc.pixelEnPipe3
        //somePixelEn=nextPixelEnPipe3
      )
    } otherwise {
      vpipe.noChangeUpdateToDrive()
    }
  } otherwise {
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
        //when (hpipe.rCPipe1 >= fbSize2d.x) 
        // BEGIN: old, non-pipelined version
        //when (hpipe.rCPipe1 >= fbSize2d.x) {
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
          //rPhys.col := tempCol
          rPhys.col.r := (default -> True)
          rPhys.col.g := rPhys.col.g + 1
          rPhys.col.b := 0x0
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
      //misc.visib := ((hpipe.s == Tstate.VISIB)
      // & (vpipe.s == Tstate.VISIB)),
    misc.drawPos.x := hpipe.c.resized
    misc.drawPos.y := vpipe.c.resized
    misc.size.x := fbSize2d.x
    misc.size.y := fbSize2d.y
    //]
    //m.d.sync += [
    //val rVisibPipe1 = Reg(Bool()) init(False)
    //rVisibPipe1 := ((hpipe.sPipe1 === LcvVgaState.visib)
    //  & (vpipe.sPipe1 === LcvVgaState.visib))
    //rVisibPipe1 := ((hpipe.rSPipe2 === LcvVgaState.visib)
    //  && (vpipe.rSPipe2 === LcvVgaState.visib))
    //rVisibPipe1 := ((hpipe.sPipe2 === LcvVgaState.visib)
    //  && (vpipe.sPipe2 === LcvVgaState.visib))
    //rVisibPipe1 := hpipe.rVisibPipe2 && vpipe.rVisibPipe2
    //rVisibPipe1 := hpipe.rVisibPipe1 && vpipe.rVisibPipe1

    //misc.visibPipe1 := rVisibPipe1
    // BEGIN: stuff
    //misc.visibPipe1 := hpipe.rVisibPipe1 && vpipe.rVisibPipe1
    //misc.visibPipe2 := hpipe.rVisibPipe2 && vpipe.rVisibPipe2
    misc.visib := hpipe.rVisib && vpipe.rVisib
    misc.visibPipe1 := hpipe.rVisibPipe1 && vpipe.rVisibPipe1
    misc.visibPipe2 := hpipe.rVisibPipe2 && vpipe.rVisibPipe2
    // END: stuff

    //val rVisibPipe1 = Reg(Bool()) init(False)
    //rVisibPipe1 := hpipe.visibPipe2 && vpipe.visibPipe2
    //misc.visibPipe1 := rVisibPipe1

    //misc.visibPipe1 := ((hpipe.rSPipe2 === LcvVgaState.visib)
    //  && (vpipe.rSPipe2 === LcvVgaState.visib))
    //misc.visibPipe1 := ((hpipe.sPipe1 === LcvVgaState.visib)
    //  & (vpipe.sPipe1 === LcvVgaState.visib))
    //misc.visibPipe1 := ((hpipe.rSPipe2 === LcvVgaState.visib)
    //  && (vpipe.rSPipe2 === LcvVgaState.visib))
    //misc.visibPipe1 := (
    //  hpipe.sPipe1 === LcvVgaState.visib
    //  && vpipe.sPipe1 === LcvVgaState.visib
    //)
    //cover(hpipe.sPipe1 === LcvVgaState.sync)

    //val rVisib = Reg(Bool()) init(False)
    //rVisib := misc.visibPipe1
    //misc.visib := rVisib

    val rPastVisib = Reg(Bool()) init(False)
    rPastVisib := misc.visib
    misc.pastVisib := rPastVisib

    val rPastDrawPos = Reg(Vec2(LcvVgaCtrlMiscIo.coordElemT()))
    rPastDrawPos.init(rPastDrawPos.getZero)
    rPastDrawPos := misc.drawPos
    misc.pastDrawPos := rPastDrawPos
    //]

  //val hpipeNextSVisib = Bool() addAttribute("keep")
  //hpipeNextSVisib := hpipe.sPipe1 === LcvVgaState.visib
  //val vpipeNextSVisib = Bool() addAttribute("keep")
  //vpipeNextSVisib := vpipe.sPipe1 === LcvVgaState.visib
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
  //      assert(misc.visibPipe1)
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

  val rPixelEnPipe2 = Reg(Bool()) init(False)
  rPixelEnPipe2 := nextClkCnt === cpp - 2
  misc.pixelEnPipe2 := rPixelEnPipe2

  val rPixelEnPipe3 = Reg(Bool()) init(False)
  rPixelEnPipe3 := nextClkCnt === cpp - 3
  misc.pixelEnPipe3 := rPixelEnPipe3
  //--------
  // Implement the State/Counter stuff
  //loc.Tstate = VgaTiming.State
  //loc.hpipe = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.HTIMING().COUNTER_WIDTH()),
  //  "_sPipe1": Signal(width_from_len(loc.Tstate)),
  //}
  //loc.vpipe = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.VTIMING().COUNTER_WIDTH()),
  //  "_sPipe1": Signal(width_from_len(loc.Tstate)),
  //}
  val hpipe = new LcvVgaPipe(
    vgaTimingHv=htiming,
    isVert=false,
  )
  val vpipe = new LcvVgaPipe(
    vgaTimingHv=vtiming,
    isVert=true,
  )
  //hpipe.sendDownPipe(
  //  activePixelEn=misc.pixelEnPipe3
  //)
  //vpipe.sendDownPipe(
  //  activePixelEn=misc.pixelEnPipe3
  //)
  misc.hpipeS := hpipe.s
  misc.hpipeC := hpipe.c
  misc.hpipeNextS := hpipe.rSPipe1
  misc.hpipeNextNextS := hpipe.rSPipe2
  misc.vpipeS := vpipe.s
  misc.vpipeC := vpipe.c
  misc.vpipeNextS := vpipe.rSPipe1
  misc.vpipeNextNextS := vpipe.rSPipe2
  //--------
  // Implement HSYNC and VSYNC logic
  //when (
  //  misc.pixelEnPipe3
  //  //misc.pixelEnPipe2
  //) {
  //  hpipe.updateStateCnt(vgaTimingHv=htiming)
  //  when (
  //    //hpipe.rSPipe2 === LcvVgaState.visib
  //    //hpipe.rVisibPipe3
  //    //&& !hpipe.visibToDrive
  //    //hpipe.rVisibPipe1
  //    //&& !hpipe.rVisibPipe2
  //    //hpipe.rVisibPipe3
  //    // BEGIN: first guess
  //    //hpipe.rSPipe.last === LcvVgaState.visib
  //    //&& hpipe.sToDrive =/= LcvVgaState.visib
  //    // END: first guess
  //    // BEGIN: more optimized version
  //    hpipe.rVisibPipe.last
  //    && !hpipe.visibToDrive
  //    // END: more optimized version

  //    // BEGIN: also try this one 
  //    //hpipe.rSPipe.last =/= LcvVgaState.visib
  //    //&& hpipe.rSPipe(hpipe.rSPipe.size - 2) == LcvVgaState.visib
  //    // END: also try this one 
  //  ) {
  //    vpipe.updateStateCnt(vgaTimingHv=vtiming)
  //  } otherwise {
  //    vpipe.noChangeUpdateToDrive()
  //  }
  //} otherwise {
  //  hpipe.noChangeUpdateToDrive()
  //  vpipe.noChangeUpdateToDrive()
  //}
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
    //val rVisibPipe1 = Reg(Bool()) init(False)
    //rVisibPipe1 := ((hpipe.sPipe1 === LcvVgaState.visib)
    //  & (vpipe.sPipe1 === LcvVgaState.visib))
    //misc.visibPipe1 := rVisibPipe1
    //misc.visibPipe1 := ((hpipe.sPipe1 === LcvVgaState.visib)
    //  & (vpipe.sPipe1 === LcvVgaState.visib))
    misc.visibPipe1 := (
      (hpipe.rSPipe1 === LcvVgaState.visib)
      && (vpipe.rSPipe1 === LcvVgaState.visib)
    )
    //cover(hpipe.sPipe1 === LcvVgaState.sync)

    val rVisib = Reg(Bool()) init(False)
    rVisib := misc.visibPipe1
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
  //hpipeNextSVisib := hpipe.sPipe1 === LcvVgaState.visib
  //val vpipeNextSVisib = Bool() addAttribute("keep")
  //vpipeNextSVisib := vpipe.sPipe1 === LcvVgaState.visib
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
  //      assert(misc.visibPipe1)
  //    }
  //  }
  //}
}
