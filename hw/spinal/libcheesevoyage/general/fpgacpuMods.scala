package libcheesevoyage.general
import libcheesevoyage._

import spinal.core._
import spinal.core.Interface
import spinal.lib._
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer

// http://fpgacpu.ca/fpga/Register.html
case class FpgacpuRegisterIo[
  T <: Data
](
  dataType: HardType[T],
  //resetVal: HardType[T],
) extends Bundle {
  //--------
  val clkEn = in Bool()
  val clear = in Bool()
  val inpData = in(dataType())
  val outpData = out(dataType())
  //--------
}
case class FpgacpuRegister[
  T <: Data
](
  dataType: HardType[T],
  //resetVal: HardType[T],
  //initVal: T,
) extends Component {
  //--------
  val io = FpgacpuRegisterIo(
    dataType=dataType()
  )
  val rOutpData = Reg(cloneOf(io.outpData))
  rOutpData.init(rOutpData.getZero)
  io.outpData := rOutpData

  when (io.clkEn) {
    rOutpData := io.inpData
  }
  when (io.clear) {
    rOutpData := rOutpData.getZero
  }
}
//--------
// http://fpgacpu.ca/fpga/Pulse_Latch.html
case class FpgacpuPulseLatchIo() extends Bundle {
  //--------
  val clear = in Bool()
  val inpPulse = in Bool()
  val outpLevel = out Bool()
  //--------
}
case class FpgacpuPulseLatch(
  //initVal: Bool=False,
) extends Component {
  //--------
  val io = FpgacpuPulseLatchIo()
  //--------
  val latch = FpgacpuRegister(
    dataType=Bool(),
    //initVal=initVal,
  )
  latch.io.clkEn := io.inpPulse
  latch.io.clear := io.clear
  latch.io.inpData := True
  io.outpLevel := latch.io.outpData
  //--------
}
//--------
// http://fpgacpu.ca/fpga/Pipe_to_Pulse.html
case class FpgacpuPipeToPulseIo[
  T <: Data
](
  dataType: HardType[T],
) extends Bundle {
  val clear = in Bool()

  // Pipe input
  val pipe = slave Stream(dataType())

  // Pulse interface to connected module input (such as a block RAM)
  val pulse = master Flow(dataType())

  // Signal that the module can accept the next input
  val moduleReady = in Bool()
}

case class FpgacpuPipeToPulse[
  T <: Data
](
  //--------
  dataType: HardType[T],
  //--------
) extends Component {
  //--------
  val io = FpgacpuPipeToPulseIo(
    dataType=dataType()
  )
  def pipeValid = io.pipe.valid
  def pipeReady = io.pipe.ready
  def pipeData = io.pipe.payload

  def pulseData = io.pulse.payload
  def pulseValid = io.pulse.valid
  def moduleReady = io.moduleReady
  //--------
  val inputHandshakeDone = Bool()
  inputHandshakeDone := io.pipe.fire

  pulseData := pipeData
  pulseValid := inputHandshakeDone
  //--------
  val initialReadyIn = Bool()
  val generateInitialReadyIn = FpgacpuPulseLatch(
    //initVal=False
  )
  generateInitialReadyIn.io.clear := io.clear
  generateInitialReadyIn.io.inpPulse := inputHandshakeDone
  initialReadyIn := generateInitialReadyIn.io.outpLevel
  //--------
  val clearReadyInLatched = Bool()
  clearReadyInLatched := inputHandshakeDone || io.clear

  val readyInLatched = Bool()
  val generateReadyInLatched = FpgacpuPulseLatch(
    //initVal=False
  )
  generateReadyInLatched.io.clear := clearReadyInLatched
  generateReadyInLatched.io.inpPulse := moduleReady
  readyInLatched := generateReadyInLatched.io.outpLevel
  //--------
  pipeReady := (
    !initialReadyIn
    || readyInLatched
    || moduleReady
  )
  //--------
}
//--------
// http://fpgacpu.ca/fpga/Pulse_to_Pipe.html
case class FpgacpuPulseToPipeIo[
  T <: Data
](
  //--------
  dataType: HardType[T],
  //--------
) extends Bundle {
  //--------
  val clear = in Bool()
  //--------
  // Pipe output
  val pipe = master Stream(dataType())

  // Pulse interface from connected module
  val pulse = slave Flow(dataType())

  // Signal that the module can accept the next input
  val moduleReady = out Bool()
  //--------
}
case class FpgacpuPulseToPipe[
  T <: Data
](
  //--------
  dataType: HardType[T],
  //fifoDepth: Int=0, // positive to use the FIFO
  //fifoArrRamStyle: String="auto",
  //--------
) extends Component {
  //--------
  val io = FpgacpuPulseToPipeIo(
    dataType=dataType()
  )
  //def pipeValid = io.pipe.valid
  //def pipeReady = io.pipe.ready
  //def pipeData = io.pipe.payload

  //def pulseData = io.pulse.payload
  //def pulseValid = io.pulse.valid
  //def moduleReady = io.moduleReady

  def pipeValid = io.pipe.valid
  def pipeReady = io.pipe.ready
  def pipeData = io.pipe.payload

  def pulseData = io.pulse.payload
  def pulseValid = io.pulse.valid
  def moduleReady = io.moduleReady
  //--------
  val validOutInternal = Bool()
  val readyOutInternal = Bool()
  val outputHandshakeDone = Bool()
  outputHandshakeDone := io.pipe.fire
  moduleReady := outputHandshakeDone
  //--------
  val validOutLatched = Bool()

  val generateValidOutLatched = FpgacpuPulseLatch(
    //initVal=False
  )
  generateValidOutLatched.io.clear := outputHandshakeDone
  generateValidOutLatched.io.inpPulse := pulseValid
  validOutLatched := generateValidOutLatched.io.outpLevel
  //--------
  validOutInternal := validOutLatched || pulseValid
  //--------
  //val prevOrPush = Stream(dataType())
  //val nextOrPop = Stream(dataType())

  //prevOrPush.valid := validOutInternal
  //readyOutInternal := prevOrPush.ready
  //prevOrPush.payload := pulseData

  //nextOrPop.valid := pipeValid
  //pipeReady := nextOrPop.ready
  //nextOrPop.payload := pipeData

  val buf = (
    //if (fifoDepth <= 0) {
      PipeSkidBuf(
        dataType=dataType(),
        optUseOldCode=false,
      )
    //} else { // if (fifoDepth > 0)
    //  AsyncReadFifo(
    //    dataType=dataType(),
    //    depth=fifoDepth,
    //    arrRamStyle=fifoArrRamStyle,
    //  )
    //}
  )
  buf.io.prev.valid := validOutInternal
  readyOutInternal := buf.io.prev.ready
  buf.io.prev.payload := pulseData

  pipeValid := buf.io.next.valid
  buf.io.next.ready := pipeReady
  pipeData := buf.io.next.payload
  //if (fifoDepth <= 0) {
    //buf.io.prev << prevOrPush
    //nextOrPop << buf.io.next
  //} else {
  //  buf.io.push << prevOrPush
  //  nextOrPop << buf.io.pop
  //}
  //--------
}
//--------
// http://fpgacpu.ca/fpga/RAM_Simple_Dual_Port.html
case class FpgacpuRamSimpleDualPortIo
//[
//  WordT <: Data
//]
(
  //wordType: HardType[WordT],
  wordWidth: Int,
  //addrWidth: Int,
  //depth: Int,
  addrWidth: Int,
) extends Bundle {
  //val addrWidth = log2Up(depth)
  //--------
  // Writes
  val wrEn = in Bool()
  val wrAddr = in UInt(addrWidth bits)
  val wrData = in(
    Bits(
      //wordType().asBits.getWidth 
      wordWidth
      bits
    )
  )
  //--------
  // Reads
  val rdEn = in Bool()
  val rdAddr = in UInt(addrWidth bits)
  val rdData = out(
    Bits(
      //wordType().asBits.getWidth
      wordWidth
      bits
    )
  )
  ////--------
  //addGeneric(
  //  name="addrWidth",
  //  that=addrWidth,
  //  default="8",
  //)
  //tieGeneric(
  //  signal=wrAddr,
  //  generic="addrWidth",
  //)
  //tieGeneric(
  //  signal=rdAddr,
  //  generic="addrWidth",
  //)
  //addGeneric(
  //  name="wordWidth",
  //  that=wordWidth,
  //  default="8",
  //)
  //tieGeneric(
  //  signal=wrData,
  //  generic="wordWidth",
  //)
  //tieGeneric(
  //  signal=rdData,
  //  generic="wordWidth",
  //)
  ////--------
  //notSVmodport()
  ////--------
}
case class FpgacpuRamSimpleDualPortImpl[
  WordT <: Data
](
  //wordWidth: Int,
  io: FpgacpuRamSimpleDualPortIo,
  wordType: HardType[WordT],
  depth: Int,
  //init: Option[Seq[Bits]]=None,
  init: Option[Seq[WordT]]=None,
  initBigInt: Option[Seq[BigInt]]=None,
  arrRamStyle: String="block",
  arrRwAddrCollision: String="",
  optDblRdReg: Boolean=false,
) extends Area {
  //val io = FpgacpuRamSimpleDualPortIo(
  //  //wordType=Bits(wordWidth bits),
  //  wordType=wordType(),
  //  depth=depth,
  //)
  val arr = Mem(
    //wordType=Bits(wordWidth bits),
    wordType=wordType(),
    wordCount=depth,
  )
    //.initBigInt(Array.fill(depth)(BigInt(0)).toSeq)
    //.addAttribute("ramstyle", arrRamStyle)
    .addAttribute("ram_style", arrRamStyle)
    .addAttribute("rw_addr_collision", arrRwAddrCollision)
    //.generateAsBlackBox()
  //arr.setTechnology(ramBlock)

  init match {
    case Some(_) => {
      arr.init(init.get)
      assert(initBigInt == None)
    }
    case None => {
    }
  }
  initBigInt match {
    case Some(_) => {
      arr.initBigInt(initBigInt.get, allowNegative=true)
      assert(init == None)
    }
    case None => {
    }
  }

  arr.write(
    address=io.wrAddr,
    data={
      val tempWrData = wordType()
      tempWrData.assignFromBits(
        io.wrData
      )
      tempWrData
    },
    enable=io.wrEn,
  )
  if (optDblRdReg) {
    io.rdData.setAsReg() //init(io.rdData.getZero)
  }
  io.rdData := {
    val tempRdData = arr.readSync(
      address=io.rdAddr,
      enable=io.rdEn,
    )
    //if (!optDblRdReg) (
      tempRdData.asBits
    //) else (
    //  RegNext(
    //    next=tempRdData.asBits,
    //    init=tempRdData.asBits.getZero,
    //  )
    //)
  }
}
case class FpgacpuRamSimpleDualPort[
  WordT <: Data
](
  wordType: HardType[WordT],
  depth: Int,
  init: Option[Seq[WordT]]=None,
  initBigInt: Option[Seq[BigInt]]=None,
  arrRamStyle: String="block",
  arrRwAddrCollision: String="",
  optDblRdReg: Boolean=false,
) extends Component {
  //--------
  val io = FpgacpuRamSimpleDualPortIo(
    //wordType=wordType(),
    wordWidth=(wordType().asBits.getWidth),
    //depth=depth,
    addrWidth=log2Up(depth),
  )
  def addrWidth = io.addrWidth
  //--------
  val impl = FpgacpuRamSimpleDualPortImpl(
    io=io,
    //wordWidth=wordType().asBits.getWidth,
    wordType=wordType(),
    depth=depth,
    //initBigInt=initBigInt,
    init={
      init match {
        case Some(_) => {
          //val tempArr = new ArrayBuffer[WordT]()
          //if (myInit.size < depth) {
          //  for (idx <- 0 until myInit.size) {
          //    tempArr += myInit(idx)//.asBits
          //  }
          //  for (idx <- myInit.size until depth) {
          //    tempArr += tempArr.last.getZero 
          //    //BigInt(0)//tempArr.last.getZero
          //  }
          //} else {
          //  for (idx <- 0 until depth) {
          //    tempArr += myInit(idx)//.asBits
          //  }
          //}
          //Some(tempArr.toSeq)
          //Some(myInit)
          init
        }
        case None => {
          None
        }
      }
    },
    initBigInt={
      initBigInt match {
        case Some(_) => {
          //val tempArr = new ArrayBuffer[BigInt]()
          //if (myInit.size < depth) {
          //  for (idx <- 0 until myInit.size) {
          //    tempArr += myInit(idx)//.asBits
          //  }
          //  for (idx <- myInit.size until depth) {
          //    tempArr += BigInt(0)//tempArr.last.getZero
          //  }
          //} else {
          //  for (idx <- 0 until depth) {
          //    tempArr += myInit(idx)//.asBits
          //  }
          //}
          //Some(tempArr.toSeq)
          //Some(myInit)
          initBigInt
        }
        case None => {
          None
        }
      }
    },
    arrRamStyle=arrRamStyle,
    arrRwAddrCollision=arrRwAddrCollision,
    optDblRdReg=optDblRdReg,
  )
  //--------
  //impl.io.wrEn := io.wrEn
  //impl.io.wrAddr := io.wrAddr
  //impl.io.wrData := io.wrData//.asBits

  //impl.io.rdEn := io.rdEn
  //impl.io.rdAddr := io.rdAddr
  ////io.rdData.assignFromBits(impl.io.rdData)
  //io.rdData := impl.io.rdData
  //--------
  //val arr = Mem(
  //  wordType=wordType(),
  //  wordCount=depth,
  //)
  //  //.initBigInt(Array.fill(depth)(BigInt(0)).toSeq)
  //  .addAttribute("ramstyle", arrRamStyle)
  //  .addAttribute("ram_style", arrRamStyle)
  //  .addAttribute("rw_addr_collision", arrRwAddrCollision)

  //initBigInt match {
  //  case Some(myInitBigInt) => {
  //    arr.initBigInt(myInitBigInt.toSeq)
  //  }
  //  case None => {
  //  }
  //}

  //arr.write(
  //  address=io.wrAddr,
  //  data=io.wrData,
  //  enable=io.wrEn,
  //)
  //io.rdData := (
  //  arr.readSync
  //  (
  //    address=io.rdAddr,
  //    enable=io.rdEn,
  //  )
  //)
}
//--------
// http://fpgacpu.ca/fpga/Pipeline_Fork_Lazy.html
case class FpgacpuPipeForkIo[
  T <: Data
](
  dataType: HardType[T],
  oSize: Int,
) extends Bundle {
  val pipeIn = slave Stream(dataType())
  val pipeOutVec = Vec.fill(oSize)(master Stream(dataType()))
}
case class FpgacpuPipeForkLazy[
  T <: Data
](
  dataType: HardType[T],
  oSize: Int,
) extends Component {
 //--------
 val io = FpgacpuPipeForkIo(
  dataType=dataType(),
  oSize=oSize,
 )
 //--------
 val outputValidGated = Bool()
 outputValidGated := io.pipeIn.fire

 val outputReadyVec = SInt(oSize bits)
 io.pipeIn.ready := outputReadyVec === S(oSize bits, default -> True)

 for (idx <- 0 until oSize) {
  outputReadyVec(idx) := io.pipeOutVec(idx).ready
  io.pipeOutVec(idx).valid := outputValidGated
  io.pipeOutVec(idx).payload := io.pipeIn.payload
 }
 //--------
}

// http://fpgacpu.ca/fpga/Pipeline_Fork_Blocking.html
case class FpgacpuPipeForkBlocking[
  T <: Data,
](
  dataType: HardType[T],
  oSize: Int,
) extends Component {
  //--------
  val io = FpgacpuPipeForkIo(
    dataType=dataType(),
    oSize=oSize,
  )
  //--------
  //val input_valid_buffered = Bool()
  //val input_ready_buffered = Bool()
  //val input_data_buffered = dataType()
  val pipeInBuffered = Stream(dataType())

  val skidBuf = PipeSkidBuf(
    dataType=dataType()
  )
  skidBuf.io.prev << io.pipeIn
  pipeInBuffered << skidBuf.io.next
  //--------
  val outputFork = FpgacpuPipeForkLazy(
    dataType=dataType(),
    oSize=oSize,
  )
  outputFork.io.pipeIn << pipeInBuffered

  for (idx <- 0 until oSize) {
    io.pipeOutVec(idx) << outputFork.io.pipeOutVec(idx)
  }
  //--------
}
//--------
case class FpgacpuPipeJoinIo[
  T <: Data,
](
  dataType: HardType[T],
  size: Int,
) extends Bundle {
  //--------
  val pipeInVec = Vec.fill(size)(slave Stream(dataType()))
  val pipeOut = master Stream(Vec.fill(size)(dataType()))
  //--------
}

case class FpgacpuPipeJoin[
  T <: Data,
](
  dataType: HardType[T],
  size: Int,
) extends Component {
  //--------
  val io = FpgacpuPipeJoinIo(
    dataType=dataType(),
    size=size,
  )
  //--------
  val tempValidVec = SInt(size bits)
  val inputBuffered = Vec.fill(size)(Stream(dataType()))
  val inputBufArr = Array.fill(size)(PipeSkidBuf(dataType=dataType()))

  for (idx <- 0 until size) {
    inputBufArr(idx).io.prev << io.pipeInVec(idx)
    inputBuffered(idx) << inputBufArr(idx).io.next
    tempValidVec(idx) := inputBuffered(idx).valid
    io.pipeOut.payload(idx) := inputBuffered(idx).payload

    when (io.pipeOut.valid) {
      inputBuffered(idx).ready := io.pipeOut.ready 
    } otherwise { // when (!io.pipeOut.valid)
      inputBuffered(idx).ready := False
    }
  }
  io.pipeOut.valid := tempValidVec === S(size bits, default -> True)
  //--------
}

//--------
//case class FpgacpuPipeForkJoinCombinedPayload(
//  typeArr: ArrayBuffer[HardType[Data]],
//) extends Interface {
//  val data = typeArr.map(ht => ht.craft())
//}
//// http://fpgacpu.ca/fpga/Pipe_Fork_Lazy.html
//case class FpgacpuPipeForkLazyIo(
//  typeArr: ArrayBuffer[HardType[Data]],
//) extends Interface {
//  //--------
//  val clear = in Bool()
//  //--------
//  val pipeIn = slave Stream(FpgacpuPipeForkJoinCombinedPayload(
//    typeArr=typeArr
//  ))
//  val pipeOutArr = typeArr.map(ht => master Stream(ht.craft()))
//  //--------
//}
//case class FpgacpuPipeForkLazy(
//  typeArr: ArrayBuffer[HardType[Data]],
//) extends Component {
//  //--------
//  val io = FpgacpuPipeForkLazyIo(
//    typeArr=typeArr
//  )
//  //--------
//  def inputValid = io.pipeIn.valid
//  def inputReady = io.pipeIn.ready
//  def inputData = io.pipeIn.payload
//
//  def outputArr = io.pipeOutArr
//  //--------
//  val outputValidGated = Bool()
//  outputValidGated := inputValid && inputReady
//  //--------
//  val outputReadyVec = SInt(typeArr.size bits)
//  for (idx <- 0 until typeArr.size) {
//    outputReadyVec(idx) := outputArr(idx).ready
//    outputArr(idx).valid := outputValidGated
//    outputArr(idx).payload := inputData
//  }
//  inputReady := outputReadyVec === S(default -> True)
//  //--------
//}
////--------
//// http://fpgacpu.ca/fpga/Pipe_Join.html
//case class FpgacpuPipeJoinIo(
//  typeArr: ArrayBuffer[HardType[Data]],
//) extends Interface {
//  //--------
//  val clear = in Bool()
//  //--------
//  val pipeInArr = typeArr.map(ht => slave Stream(ht.craft()))
//
//  val pipeOut = master Stream(FpgacpuPipeForkJoinCombinedPayload(
//    typeArr=typeArr
//  ))
//  //--------
//}
//case class FpgacpuPipeJoin(
//  typeArr: ArrayBuffer[HardType[Data]],
//) extends Component {
//  //--------
//  val io = FpgacpuPipeJoinIo(
//    typeArr=typeArr
//  )
//  //--------
//  val skidBufArr = typeArr.map(ht => PipeSkidBuf(
//    dataType=ht.craft(),
//  ))
//  val inputValidBuffered = SInt(typeArr.size bits)
//  val inputReadyBuffered = Bits(typeArr.size bits)
//  for (idx <- 0 until typeArr.size) {
//    def skidBuf = skidBufArr(idx)
//    //skidBuf.io.prev.valid := io.pipeIn.valid
//    //:= skidBuf.io.prev.ready
//    skidBuf.io.prev << io.pipeInArr(idx)
//    //outpValidVec(idx) := skidBuf.io.next.valid
//    inputValidBuffered(idx) := skidBuf.io.next.valid
//    skidBuf.io.next.ready := inputReadyBuffered(idx)
//    io.pipeOut.payload.data(idx) := skidBuf.io.next.payload
//  }
//  io.pipeOut.valid := inputValidBuffered === S(default -> True)
//  inputReadyBuffered := Mux[Bits](
//    io.pipeOut.valid ,
//    B(default -> io.pipeOut.ready),
//    B(default -> False)
//  )
//  //--------
//}
//--------
