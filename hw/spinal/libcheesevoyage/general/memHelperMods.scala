package libcheesevoyage.general

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.sim._
import spinal.lib.misc.pipeline._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.math._

case class RamSdpPipeConfig[
  WordT <: Data
](
  wordType: HardType[WordT],
  depth: Int,
  optIncludeWrByteEn: Boolean=false,
  init: Option[Seq[WordT]]=None,
  initBigInt: Option[Seq[BigInt]]=None,
  arrRamStyleAltera: String="M10K",
  arrRamStyleXilinx: String="block",
  arrRwAddrCollisionXilinx: String="",
) {
}

private[libcheesevoyage] case class RamSdpPipeNonDataIo[
  WordT <: Data
](
  cfg: RamSdpPipeConfig[WordT],
) extends Bundle /*with IMasterSlave*/ {
  //--------
  val wrEn = in(Bool())
  val wrAddr = in(UInt(log2Up(cfg.depth) bits))
  val wrByteEn = (
    cfg.optIncludeWrByteEn
  ) generate (
    in(
      Bits(ceil(cfg.wordType().asBits.getWidth.toDouble / 8).toInt bits)
    )
  )
  //--------
  //val rdEnReg = in(Bool())
  //val rdEnForWr = in(Bool())
  val rdEn = in(Bool())
  val rdAddr = in(UInt(log2Up(cfg.depth) bits))
  //--------
  //def asMaster(): Unit = {
  //  out(wrEn)
  //  out(wrAddr)
  //  if (cfg.optIncludeWrByteEn) {
  //    out(wrByteEn)
  //  }
  //  out(rdEn)
  //  out(rdAddr)
  //}
}
case class RamSdpPipeIo[
  WordT <: Data
](
  //wordType: HardType[WordT],
  //depth: Int,
  //optIncludeWrByteEn: Boolean=false
  cfg: RamSdpPipeConfig[WordT],
  useBitsData: Boolean,
) extends Bundle /*with IMasterSlave*/ {
  //val wrEnReg = in(Bool())
  val nonDataIo = RamSdpPipeNonDataIo(cfg=cfg)
  def wrEn = nonDataIo.wrEn
  def wrAddr = nonDataIo.wrAddr
  def wrByteEn = nonDataIo.wrByteEn

  val wrDataBits = (
    useBitsData
  ) generate (
    in(Bits(cfg.wordType().asBits.getWidth bits))
  )
  val wrData = (
    !useBitsData
  ) generate (
    in(cfg.wordType())
  )

  def rdEn = nonDataIo.rdEn
  def rdAddr = nonDataIo.rdAddr
  //val rdData = out(Bits(cfg.wordType().asBits.getWidth bits))
  val rdDataBits = (
    useBitsData
  ) generate (
    out(Bits(cfg.wordType().asBits.getWidth bits))
  )
  val rdData = (
    !useBitsData
  ) generate (
    out(cfg.wordType())
  )
  //val rdDataFromWrAddr = out(Bits(wordType().asBits.getWidth bits))
  //def asMaster(): Unit = {
  //  master(nonDataIo)
  //  if (useBitsData) {
  //    out(wrDataBits)
  //    in(rdDataBits)
  //  } else {
  //    out(wrData)
  //    in(rdData)
  //  }
  //}
}
case class RamSdpPipeImpl[
  WordT <: Data
](
  cfg: RamSdpPipeConfig[WordT],
) extends Component {
  def wordType = cfg.wordType
  def depth = cfg.depth
  def optIncludeWrByteEn = cfg.optIncludeWrByteEn

  def init = cfg.init
  def initBigInt = cfg.initBigInt
  def arrRamStyleAltera = cfg.arrRamStyleAltera
  def arrRamStyleXilinx = cfg.arrRamStyleXilinx
  def arrRwAddrCollisionXilinx = cfg.arrRwAddrCollisionXilinx

  val io = RamSdpPipeIo(
    cfg=cfg,
    useBitsData=true,
  )
  val arr = Mem(
    wordType=wordType(),
    wordCount=depth,
  )
    .addAttribute("ramstyle", arrRamStyleAltera)
    .addAttribute("ram_style", arrRamStyleXilinx)
    .addAttribute("rw_addr_collision", arrRwAddrCollisionXilinx)

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
        io.wrDataBits.asBits
      )
      tempWrData
    },
    enable=io.wrEn,
    mask=(
      if (optIncludeWrByteEn) (
        io.wrByteEn
      ) else (
        null.asInstanceOf[Bits]
      )
    )
  )

  // do1
  //val myDataOutFromWr = (
  //  /*Reg*/(
  //    Bits(io.rdData.getWidth bits)
  //  )
  //)

  //myDataOutFromWr := (
  //  arr.readSync(
  //    address=io.wrAddr,
  //  ).asBits
  //)
  //io.rdDataFromWrAddr.setAsReg()
  //when (io.rdEnForWr) {
  //  io.rdDataFromWrAddr := myDataOutFromWr
  //}

  // do2
  val myDataOutFromRd = (
    /*Reg*/(
      //Bits(io.rdData.getWidth bits)
      Bits(io.rdDataBits.getWidth bits)
    )
  )
  myDataOutFromRd := (
    arr.readSync(
      address=io.rdAddr,
      //enable=io.rdEn,
    ).asBits
  )

  io.rdDataBits.setAsReg() init(io.rdDataBits.getZero)
  when (io.rdEn) {
    io.rdDataBits := myDataOutFromRd
  }

  //if (optDblRdReg) {
  //  io.rdData.setAsReg() //init(io.rdData.getZero)
  //}
  //io.rdData := {
  //  val tempRdData = (
  //      arr.readSync(
  //        address=io.rdAddr,
  //        enable=io.rdEn,
  //      )
  //  )
  //  tempRdData.asBits
  //}
}
case class RamSdpPipe[
  WordT <: Data,
](
  cfg: RamSdpPipeConfig[WordT]
) extends Component {
  val io = RamSdpPipeIo(
    cfg=cfg,
    useBitsData=false,
  )
  val ram = RamSdpPipeImpl(cfg=cfg)
  ram.io.nonDataIo <> io.nonDataIo
  ram.io.wrDataBits := io.wrData.asBits
  io.rdData.assignFromBits(ram.io.rdDataBits)
}

case class RamSdpPipeReadFifoThingConfig[
  WordT <: Data
](
  ramCfg: RamSdpPipeConfig[WordT],
  //fifoDepthMain: Int=8,
  //fifoDepthSub: Int,
) {
}

//case class RamSdpPipeReadFifoThingPayload[
//  WordT <: Data
//](
//  cfg: RamSdpPipeReadFifoThingConfig[WordT]
//) extends Bundle {
//  val ramIo = RamSdpPipeIo(
//    cfg=cfg.ramCfg,
//    useBitsData=false,
//  )
//}
//case class RamSdpPipeReadFifoThingPopPayload[
//  WordT <: Data
//](
//  cfg: RamSdpPipeReadFifoThingConfig[WordT]
//) extends Bundle {
//}

//case class RamSdpPipeReadFifoThingIo[
//  WordT <: Data,
//](
//  cfg: RamSdpPipeReadFifoThingConfig[WordT]
//) extends Bundle {
//  //--------
//  val push = slave(Stream(
//    //UInt(log2Up(cfg.ramCfg.depth) bits)
//    cfg.ramCfg.wordType()
//  ))
//  val pop = master(Stream(
//    cfg.ramCfg.wordType()
//    //UInt(log2Up(cfg.ramCfg.depth) bits)
//  ))
//  val delay = in(Bool())
//
//  //val ramRdEn = out(Bool())
//  //val ramRdAddr = out(UInt(log2Up(cfg.ramCfg.depth) bits))
//  //val ramRdData = in(cfg.ramCfg.wordType())
//  //--------
//}
//case class RamSdpPipeReadFifoThing[
//  WordT <: Data,
//](
//  cfg: RamSdpPipeReadFifoThingConfig[WordT]
//) extends Component {
//  //--------
//  val io = RamSdpPipeReadFifoThingIo(cfg=cfg)
//  //--------
//  //def fifoDepthMain = cfg.fifoDepthMain
//  def fifoDepth = (
//    //cfg.fifoDepth
//    4
//  )
//
//  //val mainFifo = StreamFifo(
//  //  dataType=(
//  //    //UInt(log2Up(cfg.ramCfg.depth) bits)
//  //    cfg.ramCfg.wordType()
//  //  ),
//  //  depth=fifoDepthMain,
//  //  latency=0,
//  //  forFMax=true,
//  //)
//  val fifo = StreamFifo(
//    dataType=(
//      //UInt(log2Up(cfg.ramCfg.depth) bits)
//      cfg.ramCfg.wordType()
//    ),
//    depth=fifoDepth,
//    latency=0,
//    forFMax=true,
//  )
//
//  def fifoCntMax = fifoDepth - 2 //- 1 //- 2 
//  val rFifoCnt = (
//    Reg(SInt((log2Up(fifoDepth + 1) + 1) bits))
//    init(fifoCntMax)
//  )
//
//  //mainFifo.io.push.valid := False
//  //mainFifo.io.push.payload := mainFifo.io.push.payload.getZero
//  //mainFifo.io.pop.ready := False
//  //mainFifo.io.flush := False
//
//  fifo.io.push.valid := False
//  fifo.io.push.payload := fifo.io.push.payload.getZero
//  fifo.io.flush := False
//  //fifo.io.pop.ready := False
//
//  io.push.ready := False
//
//  object State extends SpinalEnum(defaultEncoding=binaryOneHot) {
//    val
//      IDLE,
//      POST_DELAY
//      = newElement();
//  }
//  val rState = (
//    Reg(State())
//    init(State.IDLE)
//  )
//  switch (rState) {
//    is (State.IDLE) {
//      //mainFifo.io.push << io.push //pushForkMain
//      //io.pop << mainFifo.io.pop
//
//      fifo.io.push.valid := True
//      fifo.io.push.payload := io.push.payload
//      // This should make `fifo` act like a circular FIFO that we
//      // keep only the most recent contents of
//      fifo.io.pop.ready := False
//
//      when (fifo.io.push.fire) {
//        when (!rFifoCnt.msb) {
//          rFifoCnt := rFifoCnt - 1
//        } otherwise {
//          fifo.io.pop.ready := True
//        }
//      }
//      when (io.delay) {
//        rState := State.POST_DELAY
//      }
//    }
//    is (State.POST_DELAY) {
//      rFifoCnt := fifoCntMax
//      
//      //mainFifo.io.flush := True
//      io.pop << fifo.io.pop
//      when (
//        !fifo.io.pop.valid
//        && !io.delay
//      ) {
//        rState := State.IDLE
//      }
//    }
//  }
//  //--------
//}

case class RamSimpleDualPortConfig[
  WordT <: Data
](
  wordType: HardType[WordT],
  depth: Int,
  init: Option[Seq[WordT]]=None,
  initBigInt: Option[Seq[BigInt]]=None,
  arrRamStyleAltera: String="M10K",
  arrRamStyleXilinx: String="block",
  arrRwAddrCollisionXilinx: String="",
  //doFwdDel1: Boolean=false,
) {
}
case class RamSimpleDualPortIo[
  WordT <: Data
](
  //wordType: HardType[WordT],
  //depth: Int,
  cfg: RamSimpleDualPortConfig[WordT],
) extends Bundle {
  //val ramIo = FpgacpuRamSimpleDualPortIo(
  //  wordWidth=(cfg.wordType().asBits.getWidth),
  //  addrWidth=log2Up(cfg.depth),
  //)
  val ramIo = RamSdpPipeIo(
    cfg=RamSdpPipeConfig(
      wordType=cfg.wordType(),
      depth=cfg.depth,
      optIncludeWrByteEn=false,
      init=cfg.init,
      initBigInt=cfg.initBigInt,
      arrRamStyleAltera=cfg.arrRamStyleAltera,
      arrRamStyleXilinx=cfg.arrRamStyleXilinx,
      arrRwAddrCollisionXilinx=cfg.arrRwAddrCollisionXilinx,
    ),
    useBitsData=false,
  )

  //def setAggWrData(
  //  aggWrData: WordT
  //): Unit = {
  //  ramIo
  //}
  //val fwdCondDel1 = out(Bool())
  //val fwdDataDel1 = out(Bits(wordType().asBits.getWidth bits))
  //val cmpRdWrAddrEtc = in(Bool())
}
case class RamSimpleDualPort[
  WordT <: Data
](
  cfg: RamSimpleDualPortConfig[WordT],
) extends Component {
  
  def wordType = cfg.wordType
  def depth = cfg.depth
  def init = cfg.init
  def initBigInt = cfg.initBigInt
  def arrRamStyleAltera = cfg.arrRamStyleAltera
  def arrRamStyleXilinx = cfg.arrRamStyleXilinx
  def arrRwAddrCollisionXilinx = cfg.arrRwAddrCollisionXilinx

  val io = RamSimpleDualPortIo(
    //wordType=wordType(),
    //depth=depth,
    cfg=cfg
  )
  val myRam = FpgacpuRamSimpleDualPort(
    cfg=FpgacpuRamSimpleDualPortConfig(
      wordType=wordType(),
      depth=depth,
      init=init,
      initBigInt=initBigInt,
      arrRamStyleAltera=arrRamStyleAltera,
      arrRamStyleXilinx=arrRamStyleXilinx,
      arrRwAddrCollisionXilinx=arrRwAddrCollisionXilinx,
    )
  )
  myRam.io.wrEn := io.ramIo.wrEn
  myRam.io.wrAddr := io.ramIo.wrAddr
  myRam.io.wrData := io.ramIo.wrData.asBits
  myRam.io.rdEn := io.ramIo.rdEn
  myRam.io.rdAddr := io.ramIo.rdAddr
  io.ramIo.rdData.assignFromBits(myRam.io.rdData)
}

case class PipeSimpleDualPortMemDrivePayload[
  //WordT <: Data
  T <: Data
](
  //wordType: HardType[WordT],
  dataType: HardType[T],
  wordCount: Int,
) extends Bundle {
  def addrWidth = log2Up(wordCount)
  val addr = UInt(addrWidth bits)
  val data = dataType()
  //setName(
  //  name=(
  //    s"PipeSimpleDualPortMemDrivePayload_"
  //    + s"${dataType().getClass.getName}_"
  //    + s"${wordCount}"
  //  )
  //)
  //setAsSVstruct()
}
//case class PipeSimpleDualPortMemIo[
//  //WordT <: Data
//  T <: Data
//](
//  //wordType: HardType[WordT],
//  dataType: HardType[T],
//  depth: Int,
//  unionIdxWidth: Int=1,
//) extends Bundle {
//  //--------
//  val unionIdx = in UInt(unionIdxWidth bits)
//  //--------
//  def addrWidth = log2Up(depth)
//  val wrPipe = slave Stream(
//    PipeSimpleDualPortMemDrivePayload(
//      //wordType=wordType(),
//      dataType=dataType,
//      wordCount=depth,
//    )
//  )
//  //val rdAddrPipe = Stream(UInt(addrWidth bits))
//  val rdAddrPipe = slave Stream(
//    PipeSimpleDualPortMemDrivePayload(
//      //wordType=wordType(),
//      dataType=dataType,
//      wordCount=depth,
//    )
//  )
//  val rdDataPipe = master Stream(
//    //wordType()
//    dataType()
//  )
//  //--------
//}
//case class PipeSimpleDualPortMem[
//  T <: Data,
//  WordT <: Data
//](
//  dataType: HardType[T],
//  wordType: HardType[WordT],
//  depth: Int,
//  initBigInt: Option[ArrayBuffer[BigInt]]=None,
//  //latency: Int=1,
//  arrRamStyleXilinx: String="block",
//  arrRwAddrCollisionXilinx: String="",
//  unionIdxWidth: Int=1,
//)(
//  getWordFunc: (
//    T           // input pipeline payload
//  ) => WordT,   // 
//  setWordFunc: (
//    UInt,   // `io.unionIdx`
//    T,      // pass through pipeline payload (output)
//    T,      // pass through pipeline payload (input)
//    WordT,  // data read from the RAM
//  ) => Unit,
//) extends Component {
//  //object Testificate extends TaggedUnion {
//  //  val uint = UInt(3 bits)
//  //  val sint = SInt(3 bits)
//  //}
//  //val testificate = dataType() aliasAs(Bits(3 bits))
//  //val testificate = HardType.union(Bits(3 bits), UInt(3 bits))
//  //testificate.aliasAs(UInt(3 bits)) := 3
//  //--------
//  //assert(latency >= 1)
//  //--------
//  val io = PipeSimpleDualPortMemIo(
//    //wordType=wordType(),
//    dataType=dataType(),
//    depth=depth,
//    unionIdxWidth=unionIdxWidth,
//  )
//  def addrWidth = io.addrWidth
//  //--------
//  val wrPipeToPulse = FpgacpuPipeToPulse(
//    dataType=PipeSimpleDualPortMemDrivePayload(
//      //wordType=wordType(),
//      dataType=dataType(),
//      wordCount=depth,
//    )
//  )
//  wrPipeToPulse.io.clear := False
//  val rdAddrPipeToPulse = FpgacpuPipeToPulse(
//    //dataType=UInt(addrWidth bits)
//    dataType=PipeSimpleDualPortMemDrivePayload(
//      //wordType=wordType(),
//      dataType=dataType(),
//      wordCount=depth,
//    )
//  )
//  rdAddrPipeToPulse.io.pipe << io.rdAddrPipe
//  rdAddrPipeToPulse.io.clear := False
//  val rdDataPulseToPipe = FpgacpuPulseToPipe(
//    //dataType=wordType()
//    dataType=dataType()
//  )
//  rdDataPulseToPipe.io.clear := False
//  rdAddrPipeToPulse.io.moduleReady := rdDataPulseToPipe.io.moduleReady
//  //--------
//  val arr = FpgacpuRamSimpleDualPort(
//    wordType=wordType(),
//    depth=depth,
//    initBigInt=initBigInt,
//    arrRamStyleXilinx=arrRamStyleXilinx,
//    arrRwAddrCollisionXilinx=arrRwAddrCollisionXilinx,
//  )
//
//  arr.io.wrEn := wrPipeToPulse.io.pulse.valid
//  arr.io.wrAddr := wrPipeToPulse.io.pulse.addr
//  arr.io.wrData := getWordFunc(wrPipeToPulse.io.pulse.data)
//
//  arr.io.rdEn := rdAddrPipeToPulse.io.pulse.valid
//  arr.io.rdAddr := rdAddrPipeToPulse.io.pulse.payload.addr
//  //rdDataPulseToPipe.io.pulse.payload := arr.io.rdData
//  //--------
//  wrPipeToPulse.io.pipe << io.wrPipe
//  wrPipeToPulse.io.moduleReady := True
//  rdAddrPipeToPulse.io.pipe << io.rdAddrPipe
//
//  io.rdDataPipe << rdDataPulseToPipe.io.pipe
//  //--------
//  //val rRdPulseValidVec = Vec.fill(latency)(Reg(Bool()) init(False))
//  //val rRdPulsePipePayloadVec = Vec.fill(latency)(
//  //  Reg(dataType()) init(dataType().getZero)
//  //)
//  setWordFunc(
//    io.unionIdx,
//    rdDataPulseToPipe.io.pulse.payload,
//    //rRdPulsePipePayloadVec(latency - 1),
//    rdAddrPipeToPulse.io.pulse.payload.data,
//    arr.io.rdData
//  )
//  rdDataPulseToPipe.io.pulse.valid := (
//    //rRdPulseValidVec(latency - 1)
//    rdAddrPipeToPulse.io.pulse.valid
//  )
//  //for (idx <- 0 until latency) {
//  //  if (idx == 0) {
//  //    rRdPulsePipePayloadVec(idx) := rdAddrPipeToPulse.io.pulse.data
//  //    //rRdPulseValidVec(idx) := rdAddrPipeToPulse.io.moduleReady
//  //    rRdPulseValidVec(idx) := rdAddrPipeToPulse.io.pulse.valid
//  //  } else {
//  //    rRdPulsePipePayloadVec(idx) := rRdPulsePipePayloadVec(idx - 1)
//  //    rRdPulseValidVec(idx) := rRdPulseValidVec(idx - 1)
//  //  }
//  //}
//  //--------
//  //--------
//}
//--------
//case class RdPipeSimpleDualPortMemDrivePayload[
//  //WordT <: Data
//  T <: Data
//](
//  //wordType: HardType[WordT],
//  dataType: HardType[T],
//  depth: Int,
//) extends Bundle {
//  def addrWidth = log2Up(depth)
//  val addr = UInt(addrWidth bits)
//  val data = dataType()
//}
case class WrPulseRdPipeRamSdpPipeConfig[
  ModT <: Data,
  WordT <: Data,
](
  modType: HardType[ModT],
  wordType: HardType[WordT],
  wordCount: Int,
  pipeName: String,
  setWordFunc: (
    ModT,      // pass through pipeline payload (output)
    ModT,      // pass through pipeline payload (input)
    WordT,  // data read from the RAM
  ) => Unit,
  initBigInt: Option[Seq[Seq[BigInt]]]=None,
  arrRamStyleAltera: String="M10K",
  arrRamStyleXilinx: String="block",
  arrRwAddrCollisionXilinx: String="",
) {
  def addrWidth = log2Up(wordCount)
  def modRdPortCnt = 1
  def modStageCnt = 0//1

  val pmCfg = PipeMemRmwConfig[
    WordT,
    Bool,
  ](
    wordType=wordType(),
    wordCountArr=Array.fill(1)(wordCount).toSeq,
    hazardCmpType=Bool(),
    modRdPortCnt=modRdPortCnt,
    modStageCnt=modStageCnt,
    pipeName=pipeName,
    optIncludePreMid0Front=(
      //false
      true
    ),
    linkArr=(
      //Some(PipeMemRmw.mkLinkArr()),
      //linkArr
      None
    ),
    memArrIdx=0,
    //dualRdType=PmRmwModType(),
    optDualRd=false,
    initBigInt=initBigInt,
    //optEnableModDuplicate=false,
    optModHazardKind=(
      //PipeMemRmw.ModHazardKind.Dont
      //PipeMemRmw.ModHazardKind.Dupl
      PipeMemRmw.ModHazardKind.Fwd
    ),
    vivadoDebug=false,
    optEnableWrPulse=true,
    memRamStyleAltera=arrRamStyleAltera,
    memRamStyleXilinx=arrRamStyleXilinx,
    memRwAddrCollisionXilinx=arrRwAddrCollisionXilinx,
    optIncludeModFrontStageLink=false,
    optIncludeModFrontS2MLink=false,
  )
}

case class WrPulseRdPipeRamSdpPipeIo[
  ModT <: Data,
  WordT <: Data,
](
  cfg: WrPulseRdPipeRamSdpPipeConfig[ModT, WordT],
) extends Bundle {
  val wrPulse = slave Flow(
    PipeSimpleDualPortMemDrivePayload(
      dataType=cfg.wordType(),
      wordCount=cfg.wordCount,
    )
  )
  //val rdAddrPipe = Stream(UInt(addrWidth bits))
  val rdAddrPipe = slave Stream(
    PipeSimpleDualPortMemDrivePayload(
      dataType=cfg.modType(),
      wordCount=cfg.wordCount,
    )
  )
  val rdDataPipe = master Stream(
    //wordType()
    cfg.modType()
  )
}
case class WrPulseRdPipeRamSdpPipe[
  ModT <: Data,
  WordT <: Data,
](
  cfg: WrPulseRdPipeRamSdpPipeConfig[ModT, WordT],
) extends Component {
  //--------
  val io = WrPulseRdPipeRamSdpPipeIo(cfg=cfg)
  //--------
  def mkExt() = {
    val ret = PipeMemRmwPayloadExt(
      cfg=cfg.pmCfg,
      wordCount=cfg.wordCount,
    )
    ret
  }
  case class PmRmwModType(
  ) extends Bundle with PipeMemRmwPayloadBase[WordT, Bool] {
    //setName(
    //  name=pmRmwModTypeName,
    //)
    val data = cfg.modType()
    val myExt = mkExt()
    /*override*/ def setPipeMemRmwExt(
      inpExt: PipeMemRmwPayloadExt[WordT, Bool],
      ydx: Int,
      memArrIdx: Int,
    ): Unit = {
      myExt := inpExt
    }
    /*override*/ def getPipeMemRmwExt(
      outpExt: PipeMemRmwPayloadExt[WordT, Bool],
      ydx: Int,
      memArrIdx: Int,
    ): Unit = {
      outpExt := myExt
    }
    //def optFormalFwdFuncs(
    //): Option[PipeMemRmwPayloadBaseFormalFwdFuncs[WordT, Bool]] = (
    //  None
    //)
    /*override*/ def formalSetPipeMemRmwFwd(
      outpFwd: PipeMemRmwFwd[WordT, Bool],
      memArrIdx: Int,
    ): Unit = {
    }

    /*override*/ def formalGetPipeMemRmwFwd(
      inpFwd: PipeMemRmwFwd[WordT, Bool],
      memArrIdx: Int,
    ): Unit = {
    }
  }
  val pipeMem = PipeMemRmw[
    WordT,
    Bool,
    PmRmwModType,
    PipeMemRmwDualRdTypeDisabled[
      WordT, Bool,
    ]
  ](
    cfg=cfg.pmCfg,
    modType=PmRmwModType(),
  )(
    doHazardCmpFunc=None,
    doModInMid0FrontFunc={
      def myFunc(
        params: PipeMemRmwDoModInMid0FrontFuncParams[
          WordT,
          Bool,
          PmRmwModType,
          PipeMemRmwDualRdTypeDisabled[
            WordT, Bool,
          ]
        ]
      ): Area = new Area {
        when (params.cMid0Front.up.isValid) {
          params.outp := params.inp
        }
        params.outp.myExt.modMemWord := params.getMyRdMemWordFunc(0, 0)
        params.outp.myExt.fwdCanDoIt.foreach(item => {
          item := False
        })
        params.outp.myExt.modMemWordValid.foreach(mmwValidItem => {
          mmwValidItem := False
        })
      }
      Some(myFunc)
      //None
    }
  )
  pipeMem.io.front.driveFrom(io.rdAddrPipe)(
    con=(node, inp) => {
      def rdAddrPayload = node(pipeMem.io.frontPayload)
      rdAddrPayload := rdAddrPayload.getZero
      rdAddrPayload.allowOverride
      //rdAddrPayload.data := io.rdAddrPipe
      rdAddrPayload.myExt.memAddr.head := inp.addr
      //rdAddrPayload.myExt.memAddrFwdMmw.head.foreach(item => {
      //  item := inp.addr
      //})
      rdAddrPayload.myExt.rdMemWord.foreach(rdMemWord => {
        rdMemWord := rdMemWord.getZero
      })
      rdAddrPayload.data := inp.data

      //rdAddrPayload.myExt.rdMemWord.last := (
      //  rdAddrPipePayload.data
      //)
      //rdAddrPayload.myExt.modMemWord := rdAddrPipePayload.data //rdAddrPayload.myExt.rdMemWord

    }
  )
  val sMid0FrontToModFront = StageLink(
    up=pipeMem.mod.front.cMid0Front.head.down,
    down=pipeMem.io.modFront,
  )
  pipeMem.myLinkArr += sMid0FrontToModFront
  pipeMem.io.modFront(pipeMem.io.modBackPayload) := (
    pipeMem.io.modFront(pipeMem.mod.front.outpPipePayload)
  )
  val fModFrontToModBack = ForkLink(
    up=pipeMem.io.modFront,
    downs=List(
      pipeMem.io.modBack,
      pipeMem.io.modBackFwd,
    ),
  )
  pipeMem.myLinkArr += fModFrontToModBack
  pipeMem.io.back.driveTo(io.rdDataPipe)(
    con=(outp, node) => {
      //rdDataPipePayload := node(pipeMem.io.backPayload).myExt.modMemWord
      cfg.setWordFunc(
        outp,
        node(pipeMem.io.backPayload).data,
        node(pipeMem.io.backPayload).myExt.rdMemWord.last,
      )
    }
  )
  pipeMem.io.wrPulse.valid := io.wrPulse.valid
  pipeMem.io.wrPulse.addr := io.wrPulse.addr
  pipeMem.io.wrPulse.modMemWord := io.wrPulse.data
  Builder(pipeMem.myLinkArr.toSeq)
  //--------
}

case class WrPulseRdPipeSimpleDualPortMemIo[
  T <: Data,
  WordT <: Data,
](
  dataType: HardType[T],
  wordType: HardType[WordT],
  wordCount: Int,
  unionIdxWidth: Int=1
) extends Bundle {
  //--------
  val unionIdx = in UInt(unionIdxWidth bits)
  //--------
  def addrWidth = log2Up(wordCount)
  val wrPulse = slave Flow(
    PipeSimpleDualPortMemDrivePayload(
      //wordType=wordType(),
      //dataType=dataType,
      dataType=wordType(),
      wordCount=wordCount,
    )
  )
  //val rdAddrPipe = Stream(UInt(addrWidth bits))
  val rdAddrPipe = slave Stream(
    PipeSimpleDualPortMemDrivePayload(
      //wordType=wordType(),
      dataType=dataType(),
      wordCount=wordCount,
    )
  )
  val rdDataPipe = master Stream(
    //wordType()
    dataType()
  )
  //--------
}

//--------
// TODO: rework this to not use PipeMemRmw's "dual read" stuff as that is
// mostly not a thing any mroe
case class WrPulseRdPipeSimpleDualPortMem[
  T <: Data,
  WordT <: Data
](
  dataType: HardType[T],
  wordType: HardType[WordT],
  wordCount: Int,
  pipeName: String,
  initBigInt: Option[Seq[Seq[BigInt]]]=None,
  linkArr: Option[ArrayBuffer[Link]]=None,
  //latency: Int=1,
  //pmRmwModTypeName: String,
  arrRamStyleAltera: String="M10K",
  arrRamStyleXilinx: String="block",
  arrRwAddrCollisionXilinx: String="",
  unionIdxWidth: Int=1,
  vivadoDebug: Boolean=false,
)(
  //getWordFunc: (
  //  T           // input pipeline payload
  //) => WordT,   // 
  setWordFunc: (
    UInt,   // union type indicator
    T,      // pass through pipeline payload (output)
    T,      // pass through pipeline payload (input)
    WordT,  // data read from the RAM
  ) => Unit,
) //extends Area 
extends Component 
{
  //object Testificate extends TaggedUnion {
  //  val uint = UInt(3 bits)
  //  val sint = SInt(3 bits)
  //}
  //val testificate = dataType() aliasAs(Bits(3 bits))
  //val testificate = HardType.union(Bits(3 bits), UInt(3 bits))
  //testificate.aliasAs(UInt(3 bits)) := 3
  //--------
  //assert(latency >= 1)
  //--------
  val io = WrPulseRdPipeSimpleDualPortMemIo(
    dataType=dataType(),
    wordType=wordType(),
    wordCount=wordCount,
    unionIdxWidth=unionIdxWidth,
  )
  def addrWidth = io.addrWidth
  val pmCfg = PipeMemRmwConfig[
    WordT,
    Bool,
    //PmRmwModType,
    //PmRmwModType,
  ](
    wordType=wordType(),
    wordCountArr=Array.fill(1)(wordCount).toSeq,
    hazardCmpType=Bool(),
    modRdPortCnt=modRdPortCnt,
    modStageCnt=modStageCnt,
    pipeName=pipeName,
    optIncludePreMid0Front=(
      false
      //true
    ),
    //linkArr=Some(PipeMemRmw.mkLinkArr()),
    linkArr=linkArr,
    memArrIdx=0,
    //dualRdType=PmRmwModType(),
    optDualRd=true,
    initBigInt=initBigInt,
    //optEnableModDuplicate=false,
    optModHazardKind=(
      PipeMemRmw.ModHazardKind.Dont
      //PipeMemRmw.ModHazardKind.Dupl
    ),
    vivadoDebug=vivadoDebug,
    memRamStyleAltera=arrRamStyleAltera,
    memRamStyleXilinx=arrRamStyleXilinx,
    memRwAddrCollisionXilinx=arrRwAddrCollisionXilinx,
  )
  //--------
  def modRdPortCnt = 1
  def modStageCnt = 1
  def mkExt() = {
    val ret = PipeMemRmwPayloadExt(
      cfg=pmCfg,
      //wordType=wordType(),
      wordCount=wordCount,
      //hazardCmpType=Bool(),
      //modRdPortCnt=modRdPortCnt,
      //modStageCnt=modStageCnt,
      //memArrSize=1,
      ////optEnableModDuplicate=false,
      //optModHazardKind=(
      //  PipeMemRmw.ModHazardKind.Dont
      //),
      ////optReorder=true,
    )
    //if (vivadoDebug) {
    //  ret.addAttribute("MARK_DEBUG", "TRUE")
    //}
    ret
  }
  //if (vivadoDebug) {
  //  io.addAttribute("MARK_DEBUG", "TRUE")
  //}
  case class PmRmwModType(
  ) extends Bundle with PipeMemRmwPayloadBase[WordT, Bool] {
    //setName(
    //  name=pmRmwModTypeName,
    //)
    val data = dataType()
    val myExt = mkExt()
    /*override*/ def setPipeMemRmwExt(
      inpExt: PipeMemRmwPayloadExt[WordT, Bool],
      ydx: Int,
      memArrIdx: Int,
    ): Unit = {
      myExt := inpExt
    }
    /*override*/ def getPipeMemRmwExt(
      outpExt: PipeMemRmwPayloadExt[WordT, Bool],
      ydx: Int,
      memArrIdx: Int,
    ): Unit = {
      outpExt := myExt
    }
    //def optFormalFwdFuncs(
    //): Option[PipeMemRmwPayloadBaseFormalFwdFuncs[WordT, Bool]] = (
    //  None
    //)
    /*override*/ def formalSetPipeMemRmwFwd(
      outpFwd: PipeMemRmwFwd[WordT, Bool],
      memArrIdx: Int,
    ): Unit = {
    }

    /*override*/ def formalGetPipeMemRmwFwd(
      inpFwd: PipeMemRmwFwd[WordT, Bool],
      memArrIdx: Int,
    ): Unit = {
    }
  }
  //val linkArr = PipeMemRmw.mkLinkArr()
  val pipeMem = PipeMemRmw[
    WordT,
    Bool,
    PmRmwModType,
    PmRmwModType,
  ](
    cfg=pmCfg,
    modType=PmRmwModType(),
    dualRdType=PmRmwModType(),
  )(
    doHazardCmpFunc=None
  )
  //val wrPipe = Stream(cloneOf(io.wrPulse.payload))
  //val pipeMemModFrontStm = cloneOf(pipeMem.io.modFront)
  //val pipeMemModBackStm = cloneOf(pipeMem.io.modBack)
  //pipeMemModFrontStm << pipeMem.io.modFront
  //pipeMem.io.modBack <-/< pipeMemModBackStm
  //pipeMemModFrontStm.translateInto(pipeMemModBackStm)(
  //  dataAssignment
  //)

  //--------
  val sMyMod = StageLink(
    up=pipeMem.io.modFront,
    down=pipeMem.io.modBack,
  )
    .setName("sMyMod")
  pipeMem.myLinkArr += sMyMod
  //val cMyMod = CtrlLink(
  //  up=sMyMod.down,
  //  down=pipeMem.io.modBack,
  //)
  //pipeMem.myLinkArr += cMyMod

  val tempModBackPayload = PmRmwModType()
  //pipeMem.io.modBack
  sMyMod.up(pipeMem.io.modBackPayload) := tempModBackPayload
  tempModBackPayload := (
    RegNext(tempModBackPayload) init(tempModBackPayload.getZero)
  )
  when (pipeMem.io.modFront.isValid) {
    tempModBackPayload := (
      pipeMem.io.modFront(pipeMem.io.modFrontAfterPayload)
    )
  }
  //--------
  //pipeMem.io.modBack <-/< pipeMem.io.modFront
  val tempWrPulseStm = io.wrPulse.toStream
  val tempWrPulseStm1 = cloneOf(tempWrPulseStm)
  tempWrPulseStm1 <-/< tempWrPulseStm
  pipeMem.io.front.driveFrom(tempWrPulseStm1)(
  //tempWrPulseStm1.translateInto(pipeMem.io.front)
    con=(/*wrPipePayload*/ node, wrPulsePayload) => {
      def wrPipePayload = node(pipeMem.io.frontPayload)
      wrPipePayload := wrPipePayload.getZero
      wrPipePayload.allowOverride
      //wrPipePayload.data := io.wrPulse
      wrPipePayload.myExt.memAddr.last := (
        wrPulsePayload.addr
      )
      wrPipePayload.myExt.rdMemWord.last := (
        wrPulsePayload.data
      )
      wrPipePayload.myExt.modMemWord := wrPulsePayload.data //wrPipePayload.myExt.rdMemWord
    }
  )
  pipeMem.io.back.ready := True
  //io.rdAddrPipe.translateInto(pipeMem.io.dualRdFront)
  val tempRdAddrPipe = cloneOf(io.rdAddrPipe)
  tempRdAddrPipe << io.rdAddrPipe
  pipeMem.io.dualRdFront.driveFrom(tempRdAddrPipe)(
    con=(/*dualRdPipePayload*/node, rdAddrPipePayload) => {
      def dualRdPipePayload = node(pipeMem.io.dualRdFrontPayload)
      dualRdPipePayload := dualRdPipePayload.getZero
      dualRdPipePayload.allowOverride
      dualRdPipePayload.data := rdAddrPipePayload.data
      dualRdPipePayload.myExt.memAddr.last := (
        rdAddrPipePayload.addr
      )
    }
  )
  //pipeMem.io.dualRdBack.translateInto(io.rdDataPipe)
  val tempRdDataPipe = cloneOf(io.rdDataPipe)
  io.rdDataPipe << tempRdDataPipe
  pipeMem.io.dualRdBack.driveTo(tempRdDataPipe)(
    con=(rdDataPipePayload, node/*dualRdPipePayload*/) => {
      def dualRdPipePayload = node(pipeMem.io.dualRdBackPayload)
      //val rTempWord = (
      //  Reg(
      //    cloneOf(dualRdPipePayload)
      //  )
      //)
      setWordFunc(
        io.unionIdx,
        rdDataPipePayload,
        dualRdPipePayload.data,
        dualRdPipePayload.myExt.rdMemWord.last
      )
      //rdDataPipePayload := dualRdPipePayload.myExt.modMemWord
    }
  )
  Builder(pipeMem.myLinkArr.toSeq)
  //--------
  //val mem = Mem(
  //  wordType=wordType(),
  //  wordCount=wordCount,
  //)
  //  .addAttribute("ram_style", arrRamStyleXilinx)
  //  .addAttribute("rw_addr_collision", arrRwAddrCollisionXilinx)
  //initBigInt match {
  //  case Some(myInitBigInt) => {
  //    mem.initBigInt(myInitBigInt)
  //  }
  //  case None => {
  //  }
  //}
  //mem.write(
  //  address=io.wrPulse.addr,
  //  data=io.wrPulse.data,
  //  enable=io.wrPulse.valid,
  //)
  ////def pipeWordType() = PipeSimpleDualPortMemDrivePayload(
  ////  dataType=dataType(),
  ////  depth=wordCount,
  ////)
  //val linkArr = PipeHelper.mkLinkArr()
  //val read = new Area {
  //  val myRdMemWord = wordType()
  //  val pipe = PipeHelper(linkArr=linkArr)
  //  val cFront = pipe.addStage(
  //    name="ReadFront"
  //  )
  //  val cMid0 = pipe.addStage(
  //    name="ReadMid0",
  //    //optIncludeS2M=false,
  //  )
  //  val cMid1 = pipe.addStage(
  //    name="ReadMid1",
  //    optIncludeS2M=false,
  //  )
  //  val cBack = pipe.addStage(
  //    name="ReadBack",
  //    //finish=true,
  //  )
  //  val cPreLast = pipe.addStage(
  //    name="PreLast"
  //  )
  //  val cLast = pipe.addStage(
  //    name="ReadLast",
  //    finish=true,
  //  )
  //  val inpPipePayload = Payload(cloneOf(io.rdAddrPipe.payload))
  //  //val midPipePayload = Payload(cloneOf(io.rdDataPipe.payload))
  //  val outpPipePayload = Payload(cloneOf(io.rdDataPipe.payload))
  //  pipe.first.up.driveFrom(io.rdAddrPipe)(
  //    con=(node, payload) => {
  //      node(inpPipePayload) := payload
  //    }
  //  )
  //  pipe.last.down.driveTo(io.rdDataPipe)(
  //    con=(payload, node) => {
  //      payload := node(outpPipePayload)
  //    }
  //  )
  //  val cReadFrontArea = new cFront.Area {
  //    myRdMemWord := mem.readSync(
  //      address=up(inpPipePayload).addr,
  //      enable=up.isFiring
  //    )
  //  }
  //  val cReadMid0Area = new cMid0.Area {
  //    val rDoIt = Reg(Bool()) init(False)
  //    when (
  //      up.isValid
  //      && !rDoIt
  //    ) {
  //      rDoIt := True
  //    }
  //    when (up.isFiring) {
  //      rDoIt := False
  //    }
  //    val tempOutpPayload = cloneOf(io.rdDataPipe.payload)
  //    setWordFunc(
  //      io.unionIdx,
  //      //up(outpPipePayload),
  //      tempOutpPayload,
  //      up(inpPipePayload).data,
  //      Mux[WordT](
  //        up.isValid && !rDoIt,
  //        myRdMemWord,
  //        (RegNext(myRdMemWord) init(myRdMemWord.getZero)),
  //      )
  //    )
  //    up(outpPipePayload) := tempOutpPayload
  //  }
  //  //val front = new Area {
  //  //  val pipe = PipeHelper(linkArr=linkArr)
  //  //  val outpStm = cloneOf(io.rdDataPipe)
  //  //  //val inpPipePayload = Payload(pipeWordType())
  //  //  //val outpPipePayload = Payload(pipeWordType())
  //  //  val inpPipePayload = Payload(cloneOf(io.rdAddrPipe.payload))
  //  //  //val midPipePayload = Payload(cloneOf(io.rdDataPipe.payload))
  //  //  val outpPipePayload = Payload(cloneOf(io.rdDataPipe.payload))

  //  //  val myRdMemWord = wordType()
  //  //  val cFront = pipe.addStage("Front")
  //  //  val cLastFront = pipe.addStage(
  //  //    name="LastFront",
  //  //    finish=true,
  //  //  )
  //  //  //val cBack = pipe.addStage(
  //  //  //  name="Back",
  //  //  //  finish=true,
  //  //  //)
  //  //  pipe.first.up.driveFrom(io.rdAddrPipe)(
  //  //    con=(node, payload) => {
  //  //      node(inpPipePayload) := payload
  //  //    }
  //  //  )
  //  //  pipe.last.down.driveTo(
  //  //    outpStm
  //  //    //io.rdDataPipe
  //  //  )(
  //  //    con=(payload, node) => {
  //  //      payload := node(outpPipePayload)
  //  //    }
  //  //  )
  //  //}
  //  ////val back = new Area {
  //  ////  val pipe = PipeHelper(linkArr=linkArr)
  //  ////  val inpStm = cloneOf(io.rdDataPipe)
  //  ////}
  //  //val back = new Area {
  //  //  val pipe = PipeHelper(linkArr=linkArr)
  //  //  val inpStm = cloneOf(io.rdDataPipe)
  //  //  //inpStm <-/< front.outpStm
  //  //  inpStm <-/< front.outpStm
  //  //  //val inpPipePayload = Payload(pipeWordType())
  //  //  //val outpPipePayload = Payload(pipeWordType())
  //  //  val myPipePayload = Payload(cloneOf(io.rdDataPipe.payload))
  //  //  //val outpPipePayload = Payload(cloneOf(io.rdDataPipe.payload))

  //  //  //val cMid = pipe.addStage("Mid")
  //  //  val cBack = pipe.addStage("Back")
  //  //  val cLastBack = pipe.addStage(
  //  //    name="LastBack",
  //  //    finish=true
  //  //  )
  //  //  pipe.first.up.driveFrom(inpStm)(
  //  //    con=(node, payload) => {
  //  //      node(myPipePayload) := payload
  //  //    }
  //  //  )
  //  //  pipe.last.down.driveTo(io.rdDataPipe)(
  //  //    con=(payload, node) => {
  //  //      payload := node(myPipePayload)
  //  //    }
  //  //  )
  //  //  //when (cBack.up.isValid) {
  //  //  //}
  //  //  //pipe.last.down.driveTo(io.rdDataPipe)(
  //  //  //  con=(payload, node) => {
  //  //  //    //payload := node(outpPipePayload)
  //  //  //    setWordFunc(
  //  //  //      io.unionIdx,
  //  //  //      node(outpPipePayload).data,
  //  //  //      node(inpPipePayload).data,
  //  //  //      myRdMemWord
  //  //  //    )
  //  //  //  }
  //  //  //)
  //  //}
  //  //val myRdMemWord = wordType()
  //  //val frontStm = cloneOf(io.rdAddrPipe)
  //  ////val midStm = Vec.fill(2)(cloneOf(io.rdDataPipe))
  //  //val midFrontStm = cloneOf(io.rdAddrPipe)
  //  //val midMidStm = cloneOf(io.rdDataPipe)
  //  //val midBackStm = cloneOf(io.rdDataPipe)
  //  //val backStm = cloneOf(io.rdDataPipe)
  //  ////val backStm = cloneOf(io.rdDataPipe)
  //  //val rDoIt = Reg(Bool()) init(False)
  //  ////frontStm <-/< io.rdAddrPipe
  //  //frontStm <-/< io.rdAddrPipe
  //  //midFrontStm <-/< frontStm
  //  //midFrontStm.translateInto(midMidStm)(
  //  //  dataAssignment=(midMidPayload, midFrontPayload) => {
  //  //    midMidPayload := midFrontPayload.data
  //  //    //midFrontPayload.allowOverride
  //  //  }
  //  //)
  //  //myRdMemWord := mem.readSync(
  //  //  address=io.rdAddrPipe.addr,
  //  //  enable=frontStm.fire,
  //  //)
  //  ////myRdMemWord := mem.readAsync(address=frontStm.addr)
  //  ////myRdMemWord := mem.readAsync(address=midFrontStm.addr)
  //  ////midFrontStm.translateInto(backStm)(
  //  ////  dataAssignment=(backPayload, midFrontPayload) => {
  //  ////    backPayload
  //  ////  }
  //  ////)
  //  //def condStm = (
  //  //  //midBackStm
  //  //  //midMidStm
  //  //  midFrontStm
  //  //  //io.rdDataPipe
  //  //)
  //  ////midBackStm <-/< midMidStm
  //  ////midBackStm <-/< midFrontStm
  //  ////midBackStm << midFrontStm
  //  ////midBackStm <-/< midMidStm
  //  //midBackStm << midMidStm
  //  ////midBackStm <-/< midMidStm
  //  ////backStm << midBackStm
  //  //backStm.arbitrationFrom(midBackStm)
  //  //////backStm.payload := midBackStm.payload
  //  //////backStm << midBackStm
  //  //////backStm.payload.allowOverride
  //  ////backStm.valid := midBackStm.valid
  //  ////midBackStm.ready := backStm.ready
  //  //setWordFunc(
  //  //  io.unionIdx,
  //  //  backStm.payload,
  //  //  //RegNext(midBackStm.payload),
  //  //  midBackStm.payload,
  //  //  myRdMemWord,
  //  //  //Mux[WordT](
  //  //  //  condStm.valid && !rDoIt,
  //  //  //  myRdMemWord,
  //  //  //  (RegNext(myRdMemWord) init(myRdMemWord.getZero))
  //  //  //),
  //  //)
  //  //when (
  //  //  condStm.valid
  //  //  && !rDoIt
  //  //) {
  //  //  //setWordFunc(
  //  //  //  io.unionIdx,
  //  //  //  backStm.payload,
  //  //  //  midBackStm.payload,
  //  //  //  myRdMemWord,
  //  //  //)
  //  //  rDoIt := True
  //  //}
  //  //when (condStm.fire) {
  //  //  rDoIt := False
  //  //}
  //  ////backStm <-/< backStm
  //  //io.rdDataPipe <-/< backStm
  //  ////io.rdDataPipe << backStm
  //}
  //--------
  //val cFront = read.front.cFront
  //val cFrontArea = new cFront.Area {
  //  read.front.myRdMemWord := mem.readSync(
  //    address=up(read.front.inpPipePayload).addr,
  //    enable=up.isFiring
  //  )
  //}
  //--------
  //val cLastFront = read.front.cLastFront
  //val cLastFrontArea = new cLastFront.Area {
  //  val rSetRdId = Reg(Bool()) init(False)
  //  //val tempMidPayload = dataType()
  //  //tempOutpPayload := (
  //  //  RegNext(tempOutpPayload)
  //  //  init(tempOutpPayload.getZero)
  //  //)
  //  //setWordFunc(
  //  //  io.unionIdx,
  //  //  tempMidPayload,
  //  //  up(read.front.inpPipePayload).data,
  //  //  //Mux[WordT](
  //  //  //  up.isValid && !rSetRdId,
  //  //    //RegNextWhen(
  //  //      read.front.myRdMemWord,
  //  //    //  read.front.cBack.up.isFiring,
  //  //    //)
  //  //  //  RegNext(read.front.myRdMemWord),
  //  //  //),
  //  //)
  //  //read.front.myRdMemWord
  //  //when (
  //  //  up.isValid
  //  //  && !rSetRdId
  //  //) {
  //  //  rSetRdId := True
  //  //}
  //  //when (
  //  //  up.isFiring
  //  //) {
  //  //  rSetRdId := False
  //  //}
  //  //up(read.front.outpPipePayload) := tempMidPayload
  //  //up(read.front.midPipePayload) := tempMidPayload
  //}
  //--------
  //val cLastFront = read.front.cLastFront
  //val cLastFrontArea = new cLastFront.Area {
  //  val tempOutpPayload = dataType()
  //  val rDoIt = Reg(Bool()) init(False)
  //  when (
  //    up.isValid
  //    && !rDoIt
  //  ) {
  //    rDoIt := True
  //  }
  //  when (
  //    up.isFiring
  //  ) {
  //    rDoIt := False
  //  }
  //  setWordFunc(
  //    io.unionIdx,
  //    tempOutpPayload,
  //    up(read.front.inpPipePayload).data,
  //    //up(read.front.myPipeRdMemWord)
  //    //read.front.myRdMemWord
  //    Mux[WordT](
  //      up.isValid && !rDoIt,
  //      //RegNextWhen(
  //        read.front.myRdMemWord,
  //      //  read.front.cLastFront.up.isFiring,
  //      //)
  //      RegNext(read.front.myRdMemWord),
  //    )
  //  )
  //  up(read.front.outpPipePayload) := tempOutpPayload
  //}
  //--------
  //Builder(pipeMem.myLinkArr.toSeq)
  //--------
  ////val wrPipeToPulse = FpgacpuPipeToPulse(
  ////  dataType=PipeSimpleDualPortMemDrivePayload(
  ////    //wordType=wordType(),
  ////    dataType=dataType(),
  ////    wordCount=depth,
  ////  )
  ////)
  //val rdAddrPipeToPulse = FpgacpuPipeToPulse(
  //  //dataType=UInt(addrWidth bits)
  //  dataType=PipeSimpleDualPortMemDrivePayload(
  //    //wordType=wordType(),
  //    dataType=dataType(),
  //    wordCount=wordCount,
  //  )
  //)
  //rdAddrPipeToPulse.io.pipe << io.rdAddrPipe
  //rdAddrPipeToPulse.io.clear := False
  //val rdDataPulseToPipe = FpgacpuPulseToPipe(
  //  //dataType=wordType()
  //  dataType=dataType()
  //)
  //rdDataPulseToPipe.io.clear := False
  //rdAddrPipeToPulse.io.moduleReady := rdDataPulseToPipe.io.moduleReady
  ////--------
  //val arr = FpgacpuRamSimpleDualPort(
  //  wordType=wordType(),
  //  depth=wordCount,
  //  initBigInt=initBigInt,
  //  arrRamStyleXilinx=arrRamStyleXilinx,
  //  arrRwAddrCollisionXilinx=arrRwAddrCollisionXilinx,
  //)

  ////arr.io.wrEn := wrPipeToPulse.io.pulse.valid
  ////arr.io.wrAddr := wrPipeToPulse.io.pulse.addr
  ////arr.io.wrData := getWordFunc(wrPipeToPulse.io.pulse.data)
  ////--------
  //// BEGIN: debug comment this out; later
  //arr.io.wrEn := io.wrPulse.valid
  //// END: debug comment this out; later
  ////arr.io.wrEn := False
  //arr.io.wrAddr := io.wrPulse.addr
  ////arr.io.wrData := getWordFunc(io.wrPulse.data)
  //arr.io.wrData := io.wrPulse.data
  ////--------
  //arr.io.rdEn := rdAddrPipeToPulse.io.pulse.valid
  //arr.io.rdAddr := rdAddrPipeToPulse.io.pulse.payload.addr
  ////rdDataPulseToPipe.io.pulse.payload := arr.io.rdData
  ////--------
  ////wrPipeToPulse.io.pipe << io.wrPipe
  ////wrPipeToPulse.io.moduleReady := True
  ////rdAddrPipeToPulse.io.pipe << io.rdAddrPipe

  //io.rdDataPipe << rdDataPulseToPipe.io.pipe
  ////--------
  //def latency = 1
  //val rRdPulseValidVec = Vec.fill(latency)(Reg(Bool()) init(False))
  //val rRdPulsePipePayloadVec = Vec.fill(latency)(
  //  Reg(dataType()) init(dataType().getZero)
  //)
  //setWordFunc(
  //  io.unionIdx,
  //  rdDataPulseToPipe.io.pulse.payload,
  //  rRdPulsePipePayloadVec(latency - 1),
  //  //rdAddrPipeToPulse.io.pulse.payload.data,
  //  arr.io.rdData
  //)
  //rdDataPulseToPipe.io.pulse.valid := (
  //  rRdPulseValidVec(latency - 1)
  //  //rdAddrPipeToPulse.io.pulse.valid
  //)
  //for (idx <- 0 until latency) {
  //  if (idx == 0) {
  //    rRdPulsePipePayloadVec(idx) := rdAddrPipeToPulse.io.pulse.data
  //    //rRdPulseValidVec(idx) := rdAddrPipeToPulse.io.moduleReady
  //    rRdPulseValidVec(idx) := rdAddrPipeToPulse.io.pulse.valid
  //  } else {
  //    rRdPulsePipePayloadVec(idx) := rRdPulsePipePayloadVec(idx - 1)
  //    rRdPulseValidVec(idx) := rRdPulseValidVec(idx - 1)
  //  }
  //}
  //--------
  //--------
}
//case class SimplePipeMemRmwIo[
//  WordT <: Data,
//  ModT <: Data,
//](
//  wordType: HardType[WordT],
//  wordCount: Int,
//  modType: HardType[ModT],
//) extends Area {
//}
//case class SimplePipeMemRmw[
//  WordT <: Data,
//  ModT <: Data,
//](
//  wordType: HardType[WordT],
//  wordCount: Int,
//  modType: HardType[ModT],
//  pipeName: String,
//  initBigInt: Option[ArrayBuffer[BigInt]]=None,
//  linkArr: Option[ArrayBuffer[Link]]=None,
//  arrRamStyleXilinx: String="block",
//  arrRwAddrCollisionXilinx: String="",
//)(
//  setWordFunc: (
//    ModT,   // pass-through pipeline outp
//    ModT,   // pass-through pipeline inp
//    WordT,  // data read from the RAM
//  ) => Unit
//) extends Area {
//}
//--------
case class WrPulseRdPipeSimpleDualPortMemFpgacpu[
  T <: Data,
  WordT <: Data
](
  dataType: HardType[T],
  wordType: HardType[WordT],
  wordCount: Int,
  pipeName: String,
  initBigInt: Option[Seq[BigInt]]=None,
  linkArr: Option[ArrayBuffer[Link]]=None,
  //latency: Int=1,
  arrRamStyleAltera: String="M10K",
  arrRamStyleXilinx: String="block",
  arrRwAddrCollisionXilinx: String="",
  unionIdxWidth: Int=1,
  vivadoDebug: Boolean=false,
)
(
  //getWordFunc: (
  //  T           // input pipeline payload
  //) => WordT,   // 
  setWordFunc: (
    UInt,   // union type indicator
    T,      // pass through pipeline payload (output)
    T,      // pass through pipeline payload (input)
    WordT,  // data read from the RAM
  ) => Unit,
) //extends Area 
extends Component 
{
  //object Testificate extends TaggedUnion {
  //  val uint = UInt(3 bits)
  //  val sint = SInt(3 bits)
  //}
  //val testificate = dataType() aliasAs(Bits(3 bits))
  //val testificate = HardType.union(Bits(3 bits), UInt(3 bits))
  //testificate.aliasAs(UInt(3 bits)) := 3
  //--------
  //assert(latency >= 1)
  //--------
  val io = WrPulseRdPipeSimpleDualPortMemIo(
    dataType=dataType(),
    wordType=wordType(),
    wordCount=wordCount,
    unionIdxWidth=unionIdxWidth,
  )
  def addrWidth = io.addrWidth
  //--------
  //--------
  //val wrPipeToPulse = FpgacpuPipeToPulse(
  //  dataType=PipeSimpleDualPortMemDrivePayload(
  //    //wordType=wordType(),
  //    dataType=dataType(),
  //    wordCount=depth,
  //  )
  //)
  val rdAddrPipeToPulse = FpgacpuPipeToPulse(
    //dataType=UInt(addrWidth bits)
    dataType=PipeSimpleDualPortMemDrivePayload(
      //wordType=wordType(),
      dataType=dataType(),
      wordCount=wordCount,
    )
  )
  rdAddrPipeToPulse.io.pipe << io.rdAddrPipe
  rdAddrPipeToPulse.io.clear := False
  val rdDataPulseToPipe = FpgacpuPulseToPipe(
    //dataType=wordType()
    dataType=dataType()
  )
  rdDataPulseToPipe.io.clear := False
  rdAddrPipeToPulse.io.moduleReady := rdDataPulseToPipe.io.moduleReady
  //--------
  val mem = FpgacpuRamSimpleDualPort(
    cfg=FpgacpuRamSimpleDualPortConfig(
      wordType=wordType(),
      depth=wordCount,
      init=None,
      initBigInt=initBigInt,
      arrRamStyleAltera=arrRamStyleAltera,
      arrRamStyleXilinx=arrRamStyleXilinx,
      arrRwAddrCollisionXilinx=arrRwAddrCollisionXilinx,
    )
  )

  //arr.io.wrEn := wrPipeToPulse.io.pulse.valid
  //arr.io.wrAddr := wrPipeToPulse.io.pulse.addr
  //arr.io.wrData := getWordFunc(wrPipeToPulse.io.pulse.data)
  //--------
  // BEGIN: debug comment this out; later
  mem.io.wrEn := io.wrPulse.valid
  // END: debug comment this out; later
  //arr.io.wrEn := False
  mem.io.wrAddr := io.wrPulse.addr
  //arr.io.wrData := getWordFunc(io.wrPulse.data)
  mem.io.wrData.assignFromBits(io.wrPulse.data.asBits)
  //--------
  mem.io.rdEn := rdAddrPipeToPulse.io.pulse.valid
  mem.io.rdAddr := rdAddrPipeToPulse.io.pulse.payload.addr
  //rdDataPulseToPipe.io.pulse.payload := arr.io.rdData
  //--------
  //wrPipeToPulse.io.pipe << io.wrPipe
  //wrPipeToPulse.io.moduleReady := True
  //rdAddrPipeToPulse.io.pipe << io.rdAddrPipe

  io.rdDataPipe << rdDataPulseToPipe.io.pipe
  //--------
  def latency = 1
  val rRdPulseValidVec = Vec.fill(latency)(Reg(Bool()) init(False))
  val rRdPulsePipePayloadVec = Vec.fill(latency)(
    Reg(dataType()) init(dataType().getZero)
  )
  setWordFunc(
    io.unionIdx,
    rdDataPulseToPipe.io.pulse.payload,
    rRdPulsePipePayloadVec(latency - 1),
    //rdAddrPipeToPulse.io.pulse.payload.data,
    {
      val tempRdData = wordType()
      tempRdData.assignFromBits(mem.io.rdData.asBits)
      tempRdData
    }
  )
  rdDataPulseToPipe.io.pulse.valid := (
    rRdPulseValidVec(latency - 1)
    //rdAddrPipeToPulse.io.pulse.valid
  )
  for (idx <- 0 until latency) {
    if (idx == 0) {
      rRdPulsePipePayloadVec(idx) := rdAddrPipeToPulse.io.pulse.data
      //rRdPulseValidVec(idx) := rdAddrPipeToPulse.io.moduleReady
      rRdPulseValidVec(idx) := rdAddrPipeToPulse.io.pulse.valid
    } else {
      rRdPulsePipePayloadVec(idx) := rRdPulsePipePayloadVec(idx - 1)
      rRdPulseValidVec(idx) := rRdPulseValidVec(idx - 1)
    }
  }
  //--------
  //--------
}
//object WrPulseRdPipeSimpleDualPortMemSim extends App {
//  def wordWidth = 8
//  val dataType = HardType(UInt(wordWidth bits))
//  def depth = 8
//  SimConfig
//    //.withConfig(config=simSpinalConfig)
//    .withVcdWave
//    .compile {
//      val dutInitBigInt = new ArrayBuffer
//      val dut = WrPulseRdPipeSimpleDualPortMem(
//        dataType=dataType(),
//        wordType=dataType(),
//        wordCount=depth,
//      )(
//        setWordFunc=(
//          unionIdx,
//          oPayload,
//          iPayload,
//          iWord,
//        ) => {
//          oPayload := iWord
//        }
//      )
//      //dut.mem.impl.arr.simPublic()
//      dut.mem.simPublic()
//      //dut.testDut.mem.simPublic()
//      //dut.testDut.io.data.simPublic()
//      dut
//    }
//    .doSim("blub") { dut =>
//      for (i <- 0 until depth) {
//        //dut.arr.impl.arr.setBigInt(i, i)
//        dut.mem.setBigInt(i, i)
//      }
//
//      val scoreboard = ScoreboardInOrder[Int]()
//      //dut.io.wrPulse.valid #= false
//      //StreamDriver(
//      //  stream
//      //)(
//      //  driver={payload =>
//      //    payload.addr=0,
//      //    payload.data=0,
//      //    true
//      //  }
//      //)
//
//      //StreamValidRandomizer(dut.io.data, dut.clockDomain)
//      StreamDriver(
//        stream=dut.io.rdAddrPipe,
//        clockDomain=dut.clockDomain
//      )(
//        driver={payload =>
//          payload.randomize
//          true
//        }
//      )
//      StreamReadyRandomizer(dut.io.rdDataPipe, dut.clockDomain)
//      StreamMonitor(dut.io.rdDataPipe, dut.clockDomain){payload =>
//        scoreboard.pushRef(payload.toInt)
//      }
//      dut.clockDomain.forkStimulus(10)
//      for (
//        i <- 0 until 2048 //memSize * 5 //10
//      ) {
//        dut.clockDomain.waitSampling(scala.util.Random.nextInt(10))
//        //dut.io.rdAllowed #= !dut.io.rdAllowed.toBoolean
//        //dut.clockDomain.waitRisingEdge()
//        //dut.io.rdAllowed #= true
//      }
//      simSuccess()
//    }
//}
//case class DualMem_WrPulseRdPipeSimpleDualPortMemIo[
//  T <: Data,
//  WordT0 <: Data,
//  WordT1 <: Data,
//](
//  dataType: HardType[T],
//  wordType0: HardType[WordT0],
//  wordType1: HardType[WordT1],
//  depth: Int,
//  unionIdxWidth: Int=1
//) extends Bundle {
//  //--------
//  val unionIdx = in UInt(unionIdxWidth bits)
//  val wrPulse0 = slave Flow(
//    PipeSimpleDualPortMemDrivePayload(
//      dataType=wordType0(),
//      depth=depth,
//    )
//  )
//  val wrPulse1 = slave Flow(
//    PipeSimpleDualPortMemDrivePayload(
//      dataType=wordType1(),
//      depth=depth,
//    )
//  )
//  val rdAddrPipe = slave Stream(
//    PipeSimpleDualPortMemDrivePayload(
//      dataType=dataType(),
//      depth=depth,
//    )
//  )
//  val rdDataPipe = master Stream(dataType())
//  //--------
//}
//case class DualMem_WrPulseRdPipeSimpleDualPortMem[
//  T <: Data,
//  WordT0 <: Data,
//  WordT1 <: Data,
//](
//  dataType: HardType[T],
//  wordType0: HardType[WordT0],
//  wordType1: HardType[WordT1],
//  depth: Int,
//  unionIdxWidth: Int=1
//) extends Bundle {
//  //--------
//  val unionIdx = in UInt(unionIdxWidth bits)
//  val wrPulse0 = slave Flow(
//    PipeSimpleDualPortMemDrivePayload(
//      dataType=wordType0(),
//      depth=depth,
//    )
//  )
//  val wrPulse1 = slave Flow(
//    PipeSimpleDualPortMemDrivePayload(
//      dataType=wordType1(),
//      depth=depth,
//    )
//  )
//  val rdAddrPipe = slave Stream(
//    PipeSimpleDualPortMemDrivePayload(
//      dataType=dataType(),
//      depth=depth,
//    )
//  )
//  val rdDataPipe = master Stream(dataType())
//  //--------
//}
//case class MultiRdForkBlockingJoin_WrPulseRdPipeSimpleDualPortMemIo[
//  T <: Data,
//  WordT <: Data,
//](
//  dataType: HardType[T],
//  wordType: HardType[WordT],
//  depth: Int,
//  numRd: Int,
//  unionIdxWidth: Int=1
//) extends Bundle {
//  //--------
//  assert(numRd >= 1)
//  //--------
//  val unionIdx = in(Vec.fill(numRd)(UInt(unionIdxWidth bits)))
//  //--------
//  def addrWidth = log2Up(depth)
//  // All ports besides `wrPulse` are vectorized 
//  val wrPulse = slave Flow(
//    PipeSimpleDualPortMemDrivePayload(
//      //wordType=wordType(),
//      //dataType=dataType,
//      dataType=wordType(),
//      depth=depth,
//    )
//  )
//  //--------
//  val rdAddrPipeVec = Vec.fill(numRd)(
//    slave Stream(
//      PipeSimpleDualPortMemDrivePayload(
//        //wordType=wordType(),
//        dataType=dataType(),
//        depth=depth,
//      )
//    )
//  )
//  val rdDataPipeVec = Vec.fill(numRd)(master Stream(dataType()))
//  //--------
//}
//case class MultiRdForkBlockingJoin_WrPulseRdPipeSimpleDualPortMem[
//  T <: Data,
//  WordT <: Data,
//](
//  dataType: HardType[T],
//  wordType: HardType[WordT],
//  depth: Int,
//  numRd: Int,
//  initBigInt: Option[ArrayBuffer[BigInt]]=None,
//  latency: Int=1,
//  arrRamStyleXilinx: String="block",
//  arrRwAddrCollisionXilinx: String="",
//  unionIdxWidth: Int=1,
//)(
//  //--------
//  setWordFunc: (
//    UInt,   // union type indicator
//    T,      // pass through pipeline payload (output)
//    T,      // pass through pipeline payload (input)
//    WordT,  // data read from the RAM
//  ) => Unit,
//  //--------
//) extends Component {
//  //--------
//  assert(latency >= 1)
//  assert(numRd >= 1)
//  //--------
//  val io = MultiRdForkBlockingJoin_WrPulseRdPipeSimpleDualPortMemIo(
//    dataType=dataType(),
//    wordType=wordType(),
//    depth=depth,
//    numRd=numRd,
//    unionIdxWidth=unionIdxWidth,
//  )
//  def addrWidth = io.addrWidth
//  //--------
//  val arr = Array.fill(numRd)(
//    WrPulseRdPipeSimpleDualPortMem(
//      dataType=dataType(),
//      wordType=wordType(),
//      depth=depth,
//      initBigInt=initBigInt,
//      latency=latency,
//      arrRamStyleXilinx=arrRamStyleXilinx,
//      arrRwAddrCollisionXilinx=arrRwAddrCollisionXilinx,
//      unionIdxWidth=unionIdxWidth,
//    )(
//      setWordFunc=setWordFunc,
//    )
//  )
//
//  val rdAddrFork = FpgacpuPipeForkBlocking(
//    dataType=dataType(),
//    oSize=numRd,
//  )
//
//  val rdDataJoin = FpgacpuPipeJoin(
//    dataType=dataType(),
//    size=numRd,
//  )
//
//  for (idx <- 0 until numRd) {
//    rdAddrFork.io.pipeIn
//  }
//  //--------
//}
//--------

//case class MultiRdPipeSimpleDualPortMem[
//  T <: Data,
//  WordT <: Data
//](
//  dataType: HardType[T],
//  wordType: HardType[WordT],
//  memDepth: Int,
//  numMems: Int,
//  initBigInt: Option[ArrayBuffer[BigInt]]=None,
//  latency: Int=1,
//  arrRamStyleXilinx: String="block",
//  arrRwAddrCollisionXilinx: String="",
//) extends Component {
//}

//object PipeSimpleDualPortMemSim extends App {
//}

//case class MultiMemReadSync[
//  T <: Data
//](
//  someMem: Mem[T],
//  //dataType: HardType[T],
//  //addrWidth: Int,
//  numReaders: Int=1,
//  alwaysEn: Boolean=false,
//) extends Area {
//  assert(numReaders > 0)
//
//  def addrWidth = log2Up(someMem.wordCount)
//  def dataType() = someMem.wordType()
//
//  val addrVec = Vec.fill(numReaders)(UInt(addrWidth bits))
//  val dataVec = Vec.fill(numReaders)(dataType())
//  val enVec = (!alwaysEn) generate Vec.fill(numReaders)(Bool())
//  //val rdAllowedVec = Vec.fill(numReaders)(Bool())
//
//  def readSync(
//    //addr: UInt,
//    idx: Int,
//  ): T = {
//    //addrVec(idx) := addr
//    dataVec(idx) := (
//      if (!alwaysEn) {
//        someMem.readSync(
//          address=addrVec(idx),
//          enable=enVec(idx),
//        )
//      } else { // if (alwaysEn)
//        someMem.readSync(
//          address=addrVec(idx),
//        )
//      }
//    )
//    dataVec(idx)
//  }
//}
//case class MultiMemReadSync() extends Component {
//  val io = new Bundle {
//    val readAllowed = in port Bool()
//    val data = master port Stream(Bits(10 bit))
//  }
//  val mem = Mem(Bits(10 bit), 128)
//  val address = Reg(UInt(log2Up(128) bit))
//
//  val doRead = io.readAllowed && (!io.data.valid || io.data.ready)
//  when(doRead) { address := address + 1 }
//  io.data.payload := mem.readSync(address, enable=doRead)
//  io.data.valid.setAsReg().clearWhen(io.data.fire).setWhen(doRead)
//}
// If the address comes from a stream then you have to stall that stream until you can update the output buffer. When you can (no data on the output or receiver ready,...) do the read and signal ready (if the address stream is valid)

//object MultiMemReadSync extends App {
//  SimConfig.withVcdWave
//    .compile {
//      val dut = MultiMemReadSync()
//      dut.mem.simPublic()
//      dut
//    }
//    .doSim("blub") { dut =>
//      for (i <- 0 until 128) { dut.mem.setBigInt(i, i) }
//      StreamReadyRandomizer(dut.io.data, dut.clockDomain)
//      dut.clockDomain.forkStimulus(10)
//      for(i <- 0 until 10) {
//        dut.clockDomain.waitSampling(scala.util.Random.nextInt(10))
//        dut.io.readAllowed #= !dut.io.readAllowed.toBoolean
//      }
//    }
//}

// For when `multiRd.addrVec(rdIdx)` is driven non-synchronously but
// `multiRd.readSync()` is still used
//case class MemReadSyncIntoPipe[
//  PipeInPayloadT <: Data,
//  PipeOutPayloadT <: Data,
//  MemWordT <: Data,
//](
//  pipeIn: Stream[PipeInPayloadT],
//  pipeOut: Stream[PipeOutPayloadT],
//  someMem: Mem[MemWordT],
//  //jdx: Int,
//)(
//  getInpAddrFunc: (
//    PipeInPayloadT
//  ) => UInt,
//  getOutpRdDataFunc: (
//    PipeOutPayloadT
//  ) => MemWordT,
//  //getOutpAddrFunc: (
//  //  PipeOutPayloadT
//  //) => UInt,
//) extends Area {
//
//  val inpAddr = KeepAttribute(
//    cloneOf(getInpAddrFunc(pipeIn.payload))
//  )
//    //.setName("inpAddr")
//  val outpRdData = KeepAttribute(
//    cloneOf(getOutpRdDataFunc(pipeOut.payload))
//  )
//    //.setName("outpRdData")
//  //val outpAddr = KeepAttribute(
//  //  cloneOf(getOutpAddrFunc(pipeOut.payload))
//  //)
//
//  inpAddr := getInpAddrFunc(pipeIn.payload)
//  getOutpRdDataFunc(pipeOut.payload) := outpRdData
//  //getOutpAddrFunc(pipeOut.payload) := inpAddr
//
//  val tempRdEn = Bool()
//
//  // (!pipeOutRdData.valid || pipeOutRdData.ready)
//  // same as `!isStall` (through DeMorgan's Theorem)
//  //tempRdEn := pipeIn.valid && rdAllowed && !pipeOut.isStall 
//  tempRdEn := pipeIn.valid && !pipeOut.isStall 
//
//  // If the address comes from a stream then you have to stall that
//  // stream until you can update the output buffer. When you can
//  // (no data on the output or receiver ready,... (!isStall))
//  // do the read and signal ready (if the address stream is valid)
//  //tempRdEn := rdAllowed && !pipeOut.isStall 
//  outpRdData := someMem.readSync(
//    address=inpAddr,
//    enable=tempRdEn,
//  )
//  pipeOut.valid
//    .setAsReg()
//    .clearWhen(pipeOut.fire)
//    .setWhen(
//      //En
//      tempRdEn
//    )
//  pipeIn.ready := (
//    //pipeIn.valid
//    //&&
//    tempRdEn
//  )
//
//  //rdEn := tempRdEn
//
//  //rdAddr := inpAddr
//}
//case class MemReadSyncIntoPipeTestDutIo(
//  memSize: Int=128,
//) extends Bundle {
//  //val rdAllowed = in Bool()
//  val addr = slave Stream(UInt(log2Up(memSize) bits))
//  val data = master Stream(UInt(dataWidth bits))
//  def dataWidth = log2Up(memSize)
//}
//case class MemReadSyncIntoPipeTestDut(
//  memSize: Int=128,
//) extends Component {
//  //def dataWidth = log2Up(memSize) + 1
//  //val io = new Bundle {
//  //}
//  val io = MemReadSyncIntoPipeTestDutIo(
//    memSize=memSize,
//  )
//  val mem = Mem(UInt(io.dataWidth bits), memSize)
//  //val multiRd = MultiMemReadSync(
//  //  someMem=mem,
//  //  numReaders=1,
//  //  alwaysEn=false,
//  //)
//  //multiRd.rdAllowedVec(0) := io.rdAllowed
//  val rdIntoPipe = MemReadSyncIntoPipe(
//    //--------
//    pipeIn=io.addr,
//    pipeOut=io.data,
//    someMem=mem,
//    //--------
//  )(
//    getInpAddrFunc=(payload => payload),
//    getOutpRdDataFunc=(payload => payload),
//  )
//  //val rdIntoPipe = MemReadSyncIntoPipe(
//  //  //--------
//  //  pipeIn=io.addr,
//  //  inpAddr=io.addr.payload,
//  //  //--------
//  //  pipeOut=io.data,
//  //  outpRdData=io.data.payload,
//  //  //--------
//  //  multiRd=multiRd,
//  //  rdIdx=0,
//  //  //--------
//  //)
//}
//object MemReadSyncIntoPipeSim extends App {
//  val simSpinalConfig = SpinalConfig(
//    defaultClockDomainFrequency=FixedFrequency(100 MHz)
//  )
//  def memSize = 128
//  SimConfig
//    .withConfig(config=simSpinalConfig)
//    .withVcdWave
//    .compile {
//      val dut = MemReadSyncIntoPipeTestDut(
//        memSize=memSize,
//      )
//      dut.mem.simPublic()
//      //dut.testDut.mem.simPublic()
//      //dut.testDut.io.data.simPublic()
//      dut
//    }
//    .doSim("blub") { dut =>
//      for (i <- 0 until memSize) {
//        dut.mem.setBigInt(i, i)
//      }
//
//      val scoreboard = ScoreboardInOrder[Int]()
//
//      //StreamValidRandomizer(dut.io.data, dut.clockDomain)
//      StreamDriver(
//        stream=dut.io.addr,
//        clockDomain=dut.clockDomain
//      )(
//        driver={ payload =>
//          //if (dut.io.addr.valid.toBoolean && dut.io.addr.ready.toBoolean) {
//          //  payload.randomize()
//          //}
//          //payload := RegNext(payload) + 1
//          //if (
//          //  //dut.io.addr.valid.toBoolean && dut.io.addr.ready.toBoolean
//          //  !dut.io.addr.valid.toBoolean
//          //  || dut.io.addr.ready.toBoolean
//          //) {
//          //  payload #= payload.toInt + 1
//          //}
//          payload.randomize
//          true
//        }
//      )
//      StreamReadyRandomizer(dut.io.data, dut.clockDomain)
//      StreamMonitor(dut.io.data, dut.clockDomain){payload =>
//        scoreboard.pushRef(payload.toInt)
//      }
//      dut.clockDomain.forkStimulus(10)
//      for (
//        i <- 0 until 2048 //memSize * 5 //10
//      ) {
//        //dut.clockDomain.waitSampling(scala.util.Random.nextInt(10))
//        //dut.io.rdAllowed #= !dut.io.rdAllowed.toBoolean
//        dut.clockDomain.waitRisingEdge()
//        //dut.io.rdAllowed #= true
//      }
//      simSuccess()
//    }
//}
//
//////object MemReadSyncIntoStreamHaltVecs {
//////  def checkV2dIndices(
//////    haltV2d: Vec[MemReadSyncIntoStreamHaltVecs],
//////    haltV2dJdx: Int,
//////    haltV2dIdx: Int,
//////  ): Unit = {
//////  }
//////}
////case class MemReadSyncIntoStreamHaltVecs(
////  size: Int
////) extends Bundle {
////  assert(size > 0)
////  val pipe = Vec.fill(size)(Bool())
////  val fifoPush = Vec.fill(size)(Bool())
////  val fifoPop = Vec.fill(size)(Bool())
////
////  //def implReduce(
////  //  someHaltVec: Vec[Bool]
////  //): Bool = {
////  //  someHaltVec.reduceBalancedTree(_ || _)
////  //}
////  //def reducePipe(): Bool = implReduce(someHaltVec=pipe)
////  //def reduceFifoPush(): Bool = implReduce(someHaltVec=fifoPush)
////  //def reduceFifoPop(): Bool = implReduce(someHaltVec=fifoPop)
////
////  //def implReduceV2d(
////  //  someV2d: Vec[MemReadSyncIntoStreamHaltVecs],
////  //)(
////  //  pick
////  //): Bool  
////  //def reduceV2dPipe(
////  //)
////
////  def implCheckIdx[
////    T <: Data
////  ](
////    someVec: Vec[T],
////    idx: Int,
////  ): Unit = {
////    assert(idx >= 0 && idx < someVec.size)
////  }
////  //def checkV2dIdx[
////  //  T <: Data
////  //](
////  //  v2d: Vec[Vec[T]],
////  //  outerIdx: Int,
////  //  innerIdx: Int,
////  //): Unit = {
////  //  implCheckIdx(
////  //    someVec=v2d,
////  //    idx=outerIdx,
////  //  )
////  //  implCheckIdx(
////  //    someVec=v2d(outerIdx),
////  //    idx=innerIdx,
////  //  )
////  //}
////  def checkIdx(
////    idx: Int,
////  ): Unit = {
////    //assert(idx >= 0 && idx < size)
////    implCheckIdx(
////      someVec=pipe,
////      idx=idx,
////    )
////  }
////}
//
////// For when `multiRd.addrVec(rdIdx)` is driven non-synchronously but
////// `multiRd.readSync()` is still used
////case class MemReadSyncIntoStream[
////  //PipeInPayloadT <: Data,
////  //PipeOutPayloadT <: Data,
////  //FifoWordT <: Data,
////  MemWordT <: Data,
////](
////  //pipeIn: Stream[PipeInPayloadT],
////  //pipeOut: Stream[PipeOutPayloadT],
////  //fifoWordType: HardType[FifoWordT],
////  memWordType: HardType[MemWordT],
////  multiRd: MultiMemReadSync[MemWordT],
////  rdIdx: Int,
////  haltVecs: MemReadSyncIntoStreamHaltVecs,
////  haltIdx: Int,
////  //haltV2d: Vec[MemReadSyncIntoStreamHaltVecs],
////  //haltJdx: Int,
////  //haltIdx: Int,
////) extends Area {
////  multiRd.checkIdx(idx=rdIdx)
////  haltVecs.checkIdx(idx=haltIdx)
////
////  //def fifoDepth = (
////  //  //8
////  //  3
////  //)
////  //def fifoHaltWhenGeAmountCanPop = (
////  //  //4
////  //  1
////  //)
////  //val fifo = AsyncReadFifo(
////  //  //dataType=fifoElemT(),
////  //  dataType=memWordType(),
////  //  //dataType=fifoWordType(),
////  //  depth=fifoDepth,
////  //  arrRamStyleXilinx="auto",
////  //)
////
////  //val myFifoPush = cloneOf(fifo.io.push)
////  //fifo.io.push << myFifoPush.haltWhen(haltVecs.fifoPush(haltIdx))
////  //myFifoPush.valid := True
////
////  //val myFifoPop = cloneOf(fifo.io.pop)
////  //myFifoPop << fifo.io.pop.haltWhen(haltVecs.fifoPop(haltIdx))
////  //myFifoPop.ready := True
////}
//
////case class MemReadSyncParams[
////  MemWordT <: Data
////](
////  wordType: HardType[MemWordT],
////  wordCount: Int,
////) extends Bundle {
////}
////
////case class MemReadSyncIntoStreamIo[
////  PipeInPayloadT <: Data,
////  PipeOutPayloadT <: Data,
////  MemWordT <: Data,
////](
////  pipeInPayloadType: HardType[PipeInPayloadT],
////  pipeOutPayloadType: HardType[PipeOutPayloadT],
////  memWordType: HardType[MemWordT],
////  //memSize: Int,
////  //fifoSize: Int,
////) extends Bundle {
////  //--------
////  val pipeIn = master Stream(pipeInPayloadType())
////  val pipeOut = slave Stream(pipeOutPayloadType())
////  //val memPop = master Stream(MemWordType()) 
////  //--------
////}
////case class MemReadSyncIntoStream[
////  PipeInPayloadT <: Data,
////  PipeOutPayloadT <: Data,
////  MemWordT <: Data,
////](
////  pipeInPayloadType: HardType[PipeInPayloadT],
////  pipeOutPayloadType: HardType[PipeOutPayloadT],
////  memWordType: HardType[MemWordT],
////  //memSize: Int,
////  //fifoSize: Int,
////) extends Component {
////  val io = MemReadSyncIntoStreamIo(
////    pipeInPayloadType=pipeInPayloadType(),
////    pipeOutPayloadType=pipeOutPayloadType(),
////    memWordType=memWordType(),
////    //memSize=memSize,
////  )
////}
