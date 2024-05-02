package libcheesevoyage.gfx
import libcheesevoyage.general.FifoMiscIo
import libcheesevoyage.general.FifoIo
//import libcheesevoyage.general.Fifo
import libcheesevoyage.general.AsyncReadFifo
//import libcheesevoyage.general.Vec2
import libcheesevoyage.general.DualTypeVec2
import libcheesevoyage.general.DualTypeNumVec2
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

object LcvVgaState extends SpinalEnum(
  defaultEncoding=binarySequential
  //defaultEncoding=binaryOneHot
) {
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

  def minAhead = 1
  //def maxAhead = 3 
  def maxAhead = 2
  def numAhead = maxAhead - minAhead + 1
  def pipeSize = numAhead + 1

  def currIdx = 0
  def pipe1Idx = 1
  def pipe2Idx = 2
  //def pipe3Idx = 3

  //def cntWidth = vgaTimingHv.cntWidth(offs=maxAhead) 
  def cntWidth = vgaTimingHv.cntWidth(offs=2)
  val rCPipe = new ArrayBuffer[UInt]()
  val cToDrive = UInt(cntWidth bits) // next value of `rCPipe.last`
  val rSPipe = new ArrayBuffer[LcvVgaState.C]()
  val sToDrive = LcvVgaState() // next value of `rSPipe.last`

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
      //rCPipe3Plus1GeStateSize.addAttribute("MARK_DEBUG", "TRUE")
      rCPipe.last.addAttribute("MARK_DEBUG", "TRUE")
      rSPipe.last.addAttribute("MARK_DEBUG", "TRUE")
      rVisibPipe.last.addAttribute("MARK_DEBUG", "TRUE")
      //rPixelEnArr.last.addAttribute("MARK_DEBUG", "TRUE")
    }
  }
  rCPipe.last := cToDrive
  rSPipe.last := sToDrive
  rVisibPipe.last := visibToDrive
  //rPixelEnArr.last := pixelEnToDrive

  // `<whatever>ToDrive` are the inputs to the pipeline
  def c = rCPipe(currIdx)
  def rCPipe1 = rCPipe(1)
  def rCPipe2 = rCPipe(2)
  val rCPipe2Plus1GeStateSize = Reg(Bool()) init(False)
  val rCPipe2Plus1 = Reg(UInt(cntWidth bits)) init(0x0)
  //val rCPipe3 = rCPipe(3)

  def s = rSPipe(currIdx)
  def rSPipe1 = rSPipe(pipe1Idx)
  def rSPipe2 = rSPipe(pipe2Idx)
  //val rSPipe3 = rSPipe(pipe3Idx)

  def rVisib = rVisibPipe(currIdx)
  def rVisibPipe1 = rVisibPipe(pipe1Idx)
  def rVisibPipe2 = rVisibPipe(pipe2Idx)
  //val rVisibPipe3 = rVisibPipe(pipe3Idx)

  //val rPixelEn = rPixelEnArr(currIdx)
  //val rPixelEnPipe1 = rPixelEnArr(pipe1Idx)
  //val rPixelEnPipe2 = rPixelEnArr(pipe2Idx)
  ////val rPixelEnPipe3 = rPixelEnArr(pipe3Idx)
  //--------
  def runMkCaseFunc(
    vgaTimingHv: LcvVgaTimingHv,
    //somePixelEn: Bool,
    someState: LcvVgaState.C,
  )(
    mkCaseFunc: (
      //Bool, // `somePixelEn`
      LcvVgaTimingHv, // vgaTimingHv
      LcvVgaState.E, // `someState` (`is (someState)`)
      Int, // `stateSize`
      LcvVgaState.E, // `nextState`
    ) => Unit
  ): Unit = {
    switch (someState) {
      is (LcvVgaState.front) {
        val stateSize = vgaTimingHv.front
        mkCaseFunc(
          //somePixelEn,
          vgaTimingHv,
          LcvVgaState.front,
          stateSize,
          LcvVgaState.sync
        )
        //println(f"front, sync: $stateSize")
      }
      is (LcvVgaState.sync) {
        val stateSize = vgaTimingHv.sync
        mkCaseFunc(
          //somePixelEn,
          vgaTimingHv,
          LcvVgaState.sync,
          stateSize,
          LcvVgaState.back
        )
        //println(f"sync, back: $stateSize")
      }
      is (LcvVgaState.back) {
        val stateSize = vgaTimingHv.back
        mkCaseFunc(
          //somePixelEn,
          vgaTimingHv,
          LcvVgaState.back,
          stateSize,
          LcvVgaState.visib
        )
        //println(f"back, visib: $stateSize")
      }
      is (LcvVgaState.visib) {
        val stateSize = vgaTimingHv.visib
        mkCaseFunc(
          //somePixelEn,
          vgaTimingHv,
          LcvVgaState.visib,
          stateSize,
          LcvVgaState.front
        )
        //println(f"visib, front: $stateSize")
      }
    }
  }
  def noChangeUpdateToDrive(): Unit = {
    cToDrive := rCPipe.last
    sToDrive := rSPipe.last
    visibToDrive := rVisibPipe.last
  }
  def updateCPipe2Plus1Etc(
    vgaTimingHv: LcvVgaTimingHv,
  ): Unit = {
    def mkCase(
      vgaTimingHv: LcvVgaTimingHv,
      currState: LcvVgaState.E,
      stateSize: Int,
      nextState: LcvVgaState.E,
    ): Unit = {
      rCPipe2Plus1 := rCPipe2 + 1
      // delayed by one, so this code requires at least 2x the pixel clock
      // for `clockDomain`
      rCPipe2Plus1GeStateSize := rCPipe2 >= stateSize - 1
      //// delayed by two, so this code requires at least 3x the pixel clock
      //// for `clockDomain`
      //rCPipe2Plus1GeStateSize := rCPipe2Plus1 >= stateSize
    }
    runMkCaseFunc(
      vgaTimingHv=vgaTimingHv,
      someState=rSPipe.last
    )(
      mkCaseFunc=mkCase
    )
  }
  def updateStateCnt(
    vgaTimingHv: LcvVgaTimingHv,
    //somePixelEn: Bool,
  ): Unit = {
    def mkCase(
      vgaTimingHv: LcvVgaTimingHv,
      currState: LcvVgaState.E,
      stateSize: Int,
      nextState: LcvVgaState.E,
    ): Unit = {
      for (idx <- 1 to pipeSize - 1) {
        rCPipe(idx - 1) := rCPipe(idx)

        //sPipe1(idx) := rSPipe(idx + 1)
        //rSPipe(idx) := sPipe1(idx)
        rSPipe(idx - 1) := rSPipe(idx)

        rVisibPipe(idx - 1) := rVisibPipe(idx)
      }
      when (
        //rCPipe2.resized + U(f"$cntWidth'd1")
        //>= U(f"$cntWidth'd$stateSize")

        //rCPipe3Plus1GeStateSize
        rCPipe2Plus1GeStateSize
      ) {
        sToDrive := nextState
        cToDrive := cToDrive.getZero
        //rCPipe2Plus1 := 1
        visibToDrive := (
          if (currState == LcvVgaState.back) {True} else {False}
        )
      } otherwise {
        sToDrive := rSPipe.last
        cToDrive := rCPipe2Plus1
        visibToDrive := (
          if (currState == LcvVgaState.visib) {True} else {False}
        )
      }
    }

    runMkCaseFunc(
      vgaTimingHv=vgaTimingHv,
      //somePixelEn=somePixelEn,
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
  //def coordElemT(
  //  //vgaTimingInfo: LcvVgaTimingInfo
  //  fbWidthOrHeight: Int
  //): UInt = UInt(
  //  //16 bits
  //  (
  //    //(log2Up(vgaTimingInfo.fbSize2d.x))
  //    //.max(log2Up(vgaTimingInfo.fbSize2d.y))
  //    log2Up(fbWidthOrHeight)
  //  ) bits
  //)
  def coordElemT(
    fbWidthOrHeight: Int,
    plus: Int=0,
  ): UInt = UInt(log2Up(fbWidthOrHeight) + plus bits)
  def coordT(
    //vgaTimingInfo: LcvVgaTimingInfo,
    fbSize2d: ElabVec2[Int],
    plus: ElabVec2[Int]=ElabVec2[Int](
      x=0,
      y=0,
    )
  ): DualTypeNumVec2[UInt, UInt] = DualTypeNumVec2(
    //dataTypeX=UInt(log2Up(fbSize2d.x) bits),
    //dataTypeY=UInt(log2Up(fbSize2d.y) bits),
    dataTypeX=coordElemT(fbWidthOrHeight=fbSize2d.x, plus=plus.x),
    dataTypeY=coordElemT(fbWidthOrHeight=fbSize2d.y, plus=plus.y),
  )
  //def sCoordElemT(
  //  fbWidthOrHeight: Int
  //): SInt = SInt(log2Up(fbWidthOrHeight) bits)
  //def sCoordT(
  //  //vgaTimingInfo: LcvVgaTimingInfo,
  //  fbSize2d: ElabVec2[Int]
  //): DualTypeNumVec2[SInt, SInt] = DualTypeNumVec2(
  //  //dataTypeX=UInt(log2Up(fbSize2d.x) bits),
  //  //dataTypeY=UInt(log2Up(fbSize2d.y) bits),
  //  dataTypeX=sCoordElemT(fbWidthOrHeight=fbSize2d.x),
  //  dataTypeY=sCoordElemT(fbWidthOrHeight=fbSize2d.y),
  //)
  def sCoordElemT(
    fbWidthOrHeight: Int,
    plus: Int=0,
  ): SInt = SInt(log2Up(fbWidthOrHeight) + plus bits)
  def sCoordT(
    //vgaTimingInfo: LcvVgaTimingInfo,
    fbSize2d: ElabVec2[Int],
    plus: ElabVec2[Int]=ElabVec2[Int](
      x=0,
      y=0,
    )
  ): DualTypeNumVec2[SInt, SInt] = DualTypeNumVec2(
    //dataTypeX=SInt(log2Up(fbSize2d.x) bits),
    //dataTypeY=SInt(log2Up(fbSize2d.y) bits),
    dataTypeX=sCoordElemT(fbWidthOrHeight=fbSize2d.x, plus=plus.x),
    dataTypeY=sCoordElemT(fbWidthOrHeight=fbSize2d.y, plus=plus.y),
  )
  // clocks per pixel
  def cpp(
    clkRate: HertzNumber,
    vgaTimingInfo: LcvVgaTimingInfo,
  ): Int = {
    return scala.math.floor(
      (clkRate / vgaTimingInfo.pixelClk).toDouble
    ).toInt
  }
  def clkCntWidth(
    clkRate: HertzNumber,
    vgaTimingInfo: LcvVgaTimingInfo,
  ): Int = {
    return log2Up(cpp(
      clkRate=clkRate, vgaTimingInfo=vgaTimingInfo
    ))
  }
  //--------
}
case class LcvVgaCtrlMiscIo(
  clkRate: HertzNumber,
  vgaTimingInfo: LcvVgaTimingInfo,
  fifoDepth: Int,
) extends Bundle {
  // VGA physical pins
  //self.outpCol = ColorT()
  //val col = Rgb(rgbConfig)
  //val hsync = Bool()
  //val vsync = Bool()
  //val vga = LcvVga(rgbConfig=rgbConfig)
  //val fifoPopReady = Bool()

  //val buf = port VgaDriverBufOutpLayt()//.shape
  //val buf = LcvVgaCtrlBufOutp()
  val hpipeS = LcvVgaState()
  val hpipeC = UInt(vgaTimingInfo.htiming.cntWidth() bits)
  //val hpipeCP1 = hpipeC.resized + U(f"$cPWidth'd1")
  //val hpipeCP2 = hpipeC.resized + U(f"$cPWidth'd2")
  val hpipeSPipe1 = LcvVgaState()
  val hpipeSPipe2 = LcvVgaState()

  val vpipeS = LcvVgaState()
  val vpipeC = UInt(vgaTimingInfo.vtiming.cntWidth() bits)
  //val vpipeCP1 = vpipeC.resized + U(f"$cPWidth'd1")
  //val vpipeCP2 = vpipeC.resized + U(f"$cPWidth'd2")
  val vpipeSPipe1 = LcvVgaState()
  val vpipeSPipe2 = LcvVgaState()

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
  //val pixelEnPipe3 = Bool()
  //val pixelEnPipe2 = Bool()
  val pixelEnPipe1 = Bool()
  val pixelEn = Bool()
  val pastPixelEn = Bool()
  //val visibPipe3 = Bool()
  //val visibPipe2 = Bool()
  val visibPipe1 = Bool()
  val visib = Bool()
  val pastVisib = Bool()
  //val drawPos = Vec2(LcvVgaCtrlMiscIo.coordElemT(
  //))
  val drawPos = LcvVgaCtrlMiscIo.coordT(fbSize2d=vgaTimingInfo.fbSize2d)
  //val drawPosPipe1 = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  val pastDrawPos = LcvVgaCtrlMiscIo.coordT(
    fbSize2d=vgaTimingInfo.fbSize2d
  )
  val size = LcvVgaCtrlMiscIo.coordT(
    fbSize2d=ElabVec2[Int](
      x=vgaTimingInfo.fbSize2d.x + 1,
      y=vgaTimingInfo.fbSize2d.y + 1,
    )
  )
  //--------
  //--------
}
case class LcvVgaCtrlIo(
  clkRate: HertzNumber,
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
    clkRate: HertzNumber,
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
    clkRate: HertzNumber,
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
  clkRate: HertzNumber,
  rgbConfig: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  fifoDepth: Int,
  fifoArrRamStyle: String="auto",
  //fifoArrRamStyle: String="block",
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
  misc.pastPixelEn := RegNext(misc.pixelEn) init(misc.pixelEn.getZero)
  //--------
  //val fifo = Fifo
  val fifo = AsyncReadFifo(
    dataType=Rgb(rgbConfig),
    depth=fifoDepth,
    arrRamStyle=fifoArrRamStyle,
  )
  //val fifo = StreamFifo(
  //  dataType=Rgb(rgbConfig),
  //  depth=fifoDepth,
  //  latency=2,
  //  //latency=1,
  //  //latency=0,
  //  forFMax=true,
  //)
  val fifoPush = fifo.io.push
  val fifoPop = fifo.io.pop
  val fifoEmpty = fifo.io.misc.empty
  val fifoFull = fifo.io.misc.full
  val fifoAmountCanPush = fifo.io.misc.amountCanPush
  val fifoAmountCanPop = fifo.io.misc.amountCanPop
  //val fifoEmpty = fifo.io.availability === fifoDepth
  //val fifoFull = fifo.io.occupancy === fifoDepth
  //val fifoAmountCanPush = fifo.io.availability
  //val fifoAmountCanPop = fifo.io.occupancy
  
  //val tempFifoPush = fifoPush.haltWhen(fifoAmountCanPush <= 1)
  //tempFifoPush << push
  //val tempPush = push.haltWhen(fifoAmountCanPush <= 1)
  //fifoPush << tempPush

  //fifoPush << push

  fifoPush <-/< push

  //fifoPush.valid := push.valid
  //push.ready := fifoPush.ready
  //push.ready := fifo.amountCanPush > 0
  //--------
  //val tempCol = Rgb(rgbConfig) addAttribute("keep")
  ////val tempCol = cloneOf(fifoPop.payload) addAttribute("keep")
  //tempCol := fifoPop.payload
  val tempCol = fifoPop.payload
  //val rPastFifoPopFire = Reg(Bool()) init(False)
  //rPastFifoPopFire := fifoPop.fire
  val rPastFifoPopReady = Reg(Bool()) init(False)
  rPastFifoPopReady := fifoPop.ready
  //val rTempColBuf = Reg(Rgb(rgbConfig))
  val rTempColBuf = Reg(cloneOf(tempCol))
  rTempColBuf.init(rTempColBuf.getZero)
  when (
    //rPastFifoPopFire
    //fifoPop.valid
    rPastFifoPopReady
  ) {
  //when (misc.pixelEn)
  //when (misc.pixelEnPipe1) 
  //when (fifoPop.fire) 
  //when (misc.pixelEnPipe1)
  //when (misc.pixelEn) 
  //when (fifoPop.fire)
    rTempColBuf := tempCol
  }
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
  //val clkCnt = Reg(UInt(clkCntWidth bits)) init(0x0)
  val nextClkCnt = UInt(clkCntWidth bits)
  val clkCnt = RegNext(nextClkCnt) init(0x0)
  //val nextClkCnt = clkCnt.wrapNext()
  //val nextClkCnt = UInt(clkCntWidth bits)
  //val nextClkCnt = clkCnt.wrapNext()
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
  when (io.en) {
    when (clkCntP1 < cpp) {
      //m.d.sync += 
      nextClkCnt := clkCntP1(clkCnt.bitsRange)
    } otherwise {
      //m.d.sync +=
      nextClkCnt := 0x0
    }
  } otherwise { // when (!io.en)
    nextClkCnt := clkCnt
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

  //val rPixelEnPipe2 = Reg(Bool()) init(False)
  //val nextPixelEnPipe2 = Bool()
  //nextPixelEnPipe2 := nextClkCnt === cpp - 2
  //rPixelEnPipe2 := nextPixelEnPipe2
  //misc.pixelEnPipe2 := rPixelEnPipe2

  ////val rPixelEnPipe3 = Reg(Bool()) init(False)
  //// "- 3": with this basic solution, this means there will be a minimum of
  //// a 100 MHz `clk` rate for a 25 MHz pixel clock  
  //val nextPixelEnPipe3 = nextClkCnt === cpp - 3
  //rPixelEnPipe3 := nextClkCnt === cpp - 3 
  //rPixelEnPipe3 := nextPixelEnPipe3
  //misc.pixelEnPipe3 := rPixelEnPipe3
  if (vivadoDebug) {
    rPixelEn.addAttribute("MARK_DEBUG", "TRUE")
    rPixelEnPipe1.addAttribute("MARK_DEBUG", "TRUE")
    //rPixelEnPipe2.addAttribute("MARK_DEBUG", "TRUE")
    //rPixelEnPipe3.addAttribute("MARK_DEBUG", "TRUE")
  }
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
  misc.hpipeSPipe1 := hpipe.rSPipe1
  misc.hpipeSPipe2 := hpipe.rSPipe2
  misc.vpipeS := vpipe.s
  misc.vpipeC := vpipe.c
  misc.vpipeSPipe1 := vpipe.rSPipe1
  misc.vpipeSPipe2 := vpipe.rSPipe2

  //misc.visibPipe3 := (
  //  hpipe.rVisibPipe3 && vpipe.rVisibPipe3
  //)
  //--------

  //val rPastFifoPopReady = Reg(Bool()) init(False)
  //rPastFifoPopReady := fifoPop.ready
  //fifoPop.ready := misc.pixelEnPipe1 & misc.visibPipe1 & ~rPastFifoPopReady
  //fifoPop.ready := misc.pixelEnPipe1 & misc.visibPipe1 & ~rPastFifoPopReady
  //fifoPop.ready := misc.pixelEnPipe1 & misc.visibPipe1
  val rFifoPopReady = Reg(Bool()) init(False)
  //val rFifoPopReady = Reg(Bool()) init(True)

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
  //val rFifoPopReady = Reg(Bool()) init(False)
  //rFifoPopReady := (
  //  //misc.pixelEn
  //  misc.pixelEnPipe1
  //  && misc.visib
  //  && !fifoEmpty
  //)
  // BEGIN: pipelined working (?)
  //rFifoPopReady := 

  //fifoPop.ready :=

  //fifoPop.ready := (
  //  //misc.pixelEnPipe2 && misc.visibPipe2 && !fifoEmpty
  //  //misc.pixelEnPipe3 && misc.visibPipe3 && !fifoEmpty
  //  //hpipe.visibToDrive
  //  //misc.pixelEnPipe1 && misc.visibPipe1 && !fifoEmpty
  //  //misc.pixelEn && misc.visib && !fifoEmpty
  //  //misc.pixelEnPipe1 && misc.visib && !fifoEmpty
  //  //misc.pixelEnPipe1
  //  //(nextClkCnt === cpp - 3) && misc.visib && !fifoEmpty
  //  //rPastPixelEn &&
  //  misc.visib && !fifoEmpty
  //)

  //when (misc.pixelEnPipe1 && misc.visib && !fifoEmpty) {
  //  rFifoPopReady := True
  //}
  //when (fifoPop.fire) {
  //  //fifoPop.ready := 
  //  rFifoPopReady := False
  //}

  when (
    //fifoPop.valid
    //&& 
    //misc.pixelEnPipe1
    //&& rPastPixelEn
    misc.pixelEn
    //&& misc.visib
    //&& hpipe.rVisibPipe1 && vpipe.rVisib
    && hpipe.rVisib && vpipe.rVisib
    //&& !fifoEmpty
  ) {
    //rFifoPopReady := True
    fifoPop.ready := True
  } otherwise {
    //rFifoPopReady := False
    fifoPop.ready := False
  }
  //fifoPop.ready := rFifoPopReady
  //fifoPop.ready := (
  //  //misc.pixelEnPipe1
  //  misc.pixelEn
  //  && misc.visib
  //  && !fifoEmpty
  //)

  //fifoPop.ready := True
  //fifoPop.ready := rFifoPopReady
  //fifoPop.ready := 
  //rFifoPopReady := (
  //  misc.pixelEnPipe2
  //  //misc.pixelEn
  //  //&& misc.visibPipe2
  //  //&& misc.visibPipe1
  //  //&& misc.visib
  //  //&& hpipe.rSPipe1 === LcvVgaState.visib
  //  //&& vpipe.s === LcvVgaState.visib
  //  && (
  //    //hpipe.rVisib
  //    //|| 
  //    //hpipe.rVisibPipe1
  //    //hpipe.rVisib
  //    hpipe.rVisibPipe1
  //  )
  //  && vpipe.rVisib
  //  && !fifoEmpty
  //)
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
  // Implement HSYNC and VSYNC logic
  hpipe.updateCPipe2Plus1Etc(vgaTimingHv=htiming)
  vpipe.updateCPipe2Plus1Etc(vgaTimingHv=vtiming)

  // This assumes the FPGA is running at a higher clock rate than the pixel
  // clock, but that is required anyway.
  val rUpdateVpipe = Reg(Bool()) init(False)
  rUpdateVpipe := (
    misc.pixelEnPipe1
    && hpipe.c + 1 >= fbSize2d.x
    && hpipe.rVisib
  )
  when (rUpdateVpipe) {
    vpipe.updateStateCnt(vgaTimingHv=vtiming)
  } otherwise {
    vpipe.noChangeUpdateToDrive()
  }
  when (misc.pixelEn) {
    hpipe.updateStateCnt(vgaTimingHv=htiming)
    //when (
    //  //hpipe.s === LcvVgaState.visib
    //  //&& hpipe.c + 1 >= fbSize2d.x
    //  //hpipe.rVisib
    //) {
    //  vpipe.updateStateCnt(vgaTimingHv=vtiming)
    //} otherwise {
    //  vpipe.noChangeUpdateToDrive()
    //}
  } otherwise {
    hpipe.noChangeUpdateToDrive()
    //vpipe.noChangeUpdateToDrive()
  }
  when (misc.pixelEn) {
    //hpipe.updateNextNextS(vgaTimingHv=htiming)
    //htiming.updateStateCnt(m, hpipe)
    //hpipe.updateStateCnt(vgaTimingHv=htiming)

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
  }
  //.otherwise { // when (~misc.pixelEn)
  //  //htiming.noChangeUpdateNextS(m, hpipe)
  //  //vtiming.noChangeUpdateNextS(m, vpipe)
  //  //hpipe.noChangeUpdateNextS()
  //  //vpipe.noChangeUpdateNextS()
  //}
  //--------
  // Implement drawing the picture

  //val rHpipeCWillBe0 = Reg(Bool()) init(False)
  //rHpipeCWillBe0 := hpipe.rCPipe1 === 0x0
  val rPhysColGPipe1 = Reg(UInt(rgbConfig.gWidth bits)) init(0x0)
  when (rPastPixelEn) {
    when (hpipe.rCPipe1 === 0x0) {
      rPhysColGPipe1 := 0x0
    } otherwise {
      rPhysColGPipe1 := rPhys.col.g + 1
    }
  }
  when (misc.pixelEn) {
    // Visible area
    when (misc.visib) {
      //when (~io.en) {
      //  //m.d.sync += [
      //    //phys.col.r := (0xf),
      //    //phys.col.g := (0xf),
      //    //phys.col.b := (0xf),
      //  //]
      //  rPhys.col.r := (default -> True)
      //  rPhys.col.g := (default -> True)
      //  rPhys.col.b := (default -> True)
      //} otherwise { // when (io.en)
        //m.d.sync += [
          rPhys.col := tempCol
          //rPhys.col := rTempColBuf
          //rPhys.col.r := (default -> True)
          ////when (hpipe.c === 0x0) {
          ////  rPhys.col.g := 0x0
          ////} otherwise {
          ////  rPhys.col.g := rPhys.col.g + 1
          ////}
          //rPhys.col.g := rPhysColGPipe1
          //rPhys.col.b := 0x0
        //]
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

    //misc.visib := hpipe.rVisib && vpipe.rVisib
    //misc.visibPipe1 := hpipe.rVisibPipe1 && vpipe.rVisibPipe1
    //misc.visibPipe2 := hpipe.rVisibPipe2 && vpipe.rVisibPipe2
    //rVisib := hpipe.rVisibPipe
    //val rVisibPipe = new ArrayBuffer[Bool]()
    //val visibToDrive = Bool()
    //for (idx <- 0 to hpipe.pipeSize - 1) {
    //  rVisibPipe += Reg(Bool()) init(False)
    //}
    //for (idx <- 1 to hpipe.pipeSize - 1) {
    //  rVisibPipe(idx - 1) := rVisibPipe(idx)
    //}
    //rVisibPipe.last := visibToDrive
    //visibToDrive := hpipe.visibToDrive && vpipe.visibToDrive
    //misc.visib := rVisibPipe(hpipe.currIdx)
    //misc.visibPipe1 := rVisibPipe(hpipe.pipe1Idx)
    //misc.visibPipe2 := rVisibPipe(hpipe.pipe2Idx)
    //misc.visibPipe3 := rVisibPipe(hpipe.pipe3Idx)

    //misc.visib := hpipe.rVisib && vpipe.rVisib
    //misc.visib := (
    //  hpipe.s === LcvVgaState.visib
    //  && vpipe.s === LcvVgaState.visib
    //)
    //misc.visibPipe1 := hpipe.rVisibPipe1 && vpipe.rVisib
    misc.visib := hpipe.rVisib && vpipe.rVisib
    misc.visibPipe1 := hpipe.rVisibPipe1 && vpipe.rVisibPipe1
    //misc.visibPipe2 := False
    //misc.visibPipe2 := hpipe.rVisibPipe2 && vpipe.rVisibPipe2
    //misc.visibPipe3 := False
    //val rVisib = Reg(Bool()) init(False)
    //rVisib := hpipe.rVisib && 

    //misc.visib := hpipe.rVisib && vpipe.rVisib
    //val rVisib = Reg(Bool()) init(False)
    //rVisib := 
    //misc.visib := (
    //  hpipe.s === LcvVgaState.visib
    //  && vpipe.s === LcvVgaState.visib
    //)
    //val rVisibPipe1 = Reg(Bool()) init(False)
    //rVisibPipe1 := (
    //  hpipe.rSPipe1 === LcvVgaState.visib
    //  //&& vpipe.rSPipe1 === LcvVgaState.visib
    //  && vpipe.s === LcvVgaState.visib
    //)
    //val rVisib = Reg(Bool()) init(False)
    //rVisib := rVisibPipe1
    //misc.visib := rVisib
    //misc.visibPipe1 := rVisibPipe1
    //misc.visibPipe2 := False
    //misc.visibPipe3 := False

    //misc.visibPipe1 := 
    //misc.visib := hpipe.rVisib && vpipe.rVisib
    //misc.visibPipe1 := hpipe.rVisibPipe1 && vpipe.rVisib

    //misc.visibPipe2 := hpipe.rVisibPipe2 && vpipe.rVisib
    //misc.visibPipe3 := hpipe.rVisibPipe3 && vpipe.rVisib

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

    val rPastDrawPos = Reg(LcvVgaCtrlMiscIo.coordT(
      fbSize2d=vgaTimingInfo.fbSize2d
    ))
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
  clkRate: HertzNumber,
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
  clkRate: HertzNumber,
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
      (clkRate / vgaTimingInfo.pixelClk).toDouble
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
  val nextClkCnt = UInt(clkCntWidth bits)
  val clkCnt = RegNext(nextClkCnt) init(0x0)
  //val nextClkCnt = clkCnt.wrapNext()
  // Force this addition to be of width `CLK_CNT_WIDTH + 1` to
  // prevent wrap-around
  val clkCntP1Width = clkCntWidth + 1
  val clkCntP1 = UInt(clkCntP1Width bits)
  clkCntP1 := clkCnt.resized + U(f"$clkCntP1Width'd1")

  // Implement wrap-around for the clock counter
  when (io.en) {
    when (clkCntP1 < cpp) {
      //m.d.sync += 
      nextClkCnt := clkCntP1(clkCnt.bitsRange)
    } otherwise {
      //m.d.sync +=
      nextClkCnt := 0x0
    }
  } otherwise { // when (!io.en)
    nextClkCnt := clkCnt
  }
  // Since this is an alias, use ALL_CAPS for its name.
  // outp.pixelEn = (clkCnt == 0x0)
  //m.d.comb += 
  misc.pixelEn := clkCnt === 0x0
  val pixelEnNextCycle = Bool()
  pixelEnNextCycle := clkCntP1.resized === cpp

  //val rPixelEnPipe2 = Reg(Bool()) init(False)
  //rPixelEnPipe2 := nextClkCnt === cpp - 2
  //misc.pixelEnPipe2 := rPixelEnPipe2

  //val rPixelEnPipe3 = Reg(Bool()) init(False)
  //rPixelEnPipe3 := nextClkCnt === cpp - 3
  //misc.pixelEnPipe3 := rPixelEnPipe3
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
  misc.hpipeS := hpipe.s
  misc.hpipeC := hpipe.c
  misc.hpipeSPipe1 := hpipe.rSPipe1
  misc.hpipeSPipe2 := hpipe.rSPipe2
  misc.vpipeS := vpipe.s
  misc.vpipeC := vpipe.c
  misc.vpipeSPipe1 := vpipe.rSPipe1
  misc.vpipeSPipe2 := vpipe.rSPipe2
  //--------
  // Implement HSYNC and VSYNC logic
  // This assumes the FPGA is running at a higher clock rate than the pixel
  // clock, but that is required anyway.
  hpipe.updateCPipe2Plus1Etc(vgaTimingHv=htiming)
  vpipe.updateCPipe2Plus1Etc(vgaTimingHv=vtiming)
  val rUpdateVpipe = Reg(Bool()) init(False)
  rUpdateVpipe := (
    misc.pixelEnPipe1
    && hpipe.c + 1 >= fbSize2d.x
    && hpipe.rVisib
  )
  when (rUpdateVpipe) {
    vpipe.updateStateCnt(vgaTimingHv=vtiming)
  } otherwise {
    vpipe.noChangeUpdateToDrive()
  }
  when (misc.pixelEn) {
    hpipe.updateStateCnt(vgaTimingHv=htiming)
    //when (
    //  //hpipe.s === LcvVgaState.visib
    //  //&& hpipe.c + 1 >= fbSize2d.x
    //  //hpipe.rVisib
    //) {
    //  vpipe.updateStateCnt(vgaTimingHv=vtiming)
    //} otherwise {
    //  vpipe.noChangeUpdateToDrive()
    //}
  } otherwise {
    hpipe.noChangeUpdateToDrive()
    //vpipe.noChangeUpdateToDrive()
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
      //when (~io.en) {
      //  //m.d.sync += [
      //    //rPhys.col.r := (0xf),
      //    //rPhys.col.g := (0xf),
      //    //rPhys.col.b := (0xf),
      //  //]
      //  rPhys.col.r := (default -> True)
      //  rPhys.col.g := (default -> True)
      //  rPhys.col.b := (default -> True)
      //} otherwise { // when (io.en)
        //m.d.sync += [
          rPhys.col := inpCol
        //]
      //}
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
    misc.visib := hpipe.rVisib && vpipe.rVisib
    misc.visibPipe1 := hpipe.rVisibPipe1 && vpipe.rVisibPipe1
    //misc.visibPipe1 := (
    //  (hpipe.rSPipe1 === LcvVgaState.visib)
    //  && (vpipe.rSPipe1 === LcvVgaState.visib)
    //)
    //misc.visibPipe2 := (
    //  (hpipe.rSPipe2 === LcvVgaState.visib)
    //  && (vpipe.rSPipe2 === LcvVgaState.visib)
    //)
    //cover(hpipe.sPipe1 === LcvVgaState.sync)

    //val rVisib = Reg(Bool()) init(False)
    //rVisib := misc.visibPipe1
    //misc.visib := rVisib

    val rPastVisib = Reg(Bool()) init(False)
    rPastVisib := misc.visib
    misc.pastVisib := rPastVisib

    val rPastDrawPos = Reg(LcvVgaCtrlMiscIo.coordT(
      fbSize2d=vgaTimingInfo.fbSize2d,
    ))
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
