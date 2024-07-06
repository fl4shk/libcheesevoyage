package libcheesevoyage.general

import spinal.core._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._
import scala.collection.mutable.ArrayBuffer
import scala.math._

import libcheesevoyage.Config
//--------
//case class PipeHazardHandlerIo[
//  T <: Data,
//](
//  dataType: HardType[T],
//  //wordCount: Int,
//) extends Bundle {
//  val front = slave(Stream(dataType()))
//  val back = master(Stream(dataType()))
//}
//case class PipeHazardHandler[
//  T <: Data,
//](
//  dataType: HardType[T],
//  //wordCount: Int,
//) extends Component {
//  //--------
//  val io = PipeHazardHandlerIo(
//    dataType=dataType(),
//  )
//  //--------
//}
//--------
//trait PipeMemRmwHazardCmpBase[
//  HazardCmpT <: Data
//] {
//  def doCmp(
//    other: HazardCmpT
//  ): Bool
//}
object PipeMemRmwPayloadExt {
  def defaultDoHazardCmpFunc[
    WordT <: Data,
    HazardCmpT <: Data,
  ](
    curr: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    prev: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    //idx: Int,
    zdx: Int,
    isPostDelay: Boolean,
  ): Bool = (
    True
    ////curr === b
    //curr.memAddr === prev.memAddr
    ////&& prev.hazardId.msb
  )
}
case class PipeMemRmwPayloadExt[
  WordT <: Data,
  HazardCmpT <: Data,
](
  wordType: HardType[WordT],
  wordCount: Int,
  hazardCmpType: HardType[HazardCmpT],
  modRdPortCnt: Int,
  modStageCnt: Int,
  //optSimpleIsWr: Option[Boolean]=None,
  //optUseModMemAddr: Boolean=false,
  //doModInModFront: Boolean=false,
  //optEnableModDuplicate: Boolean=true,
  optModHazardKind: Int=PipeMemRmw.modHazardKindDupl,
  optReorder: Boolean=false,
) extends Bundle {
  if (optModHazardKind == PipeMemRmw.modHazardKindFwd) {
    assert(modRdPortCnt > 0)
    assert(!optReorder)
  } else {
    assert(modRdPortCnt == 1)
  }
  //--------
  def debug: Boolean = {
    GenerationFlags.formal {
      return true
    }
    return false
  }
  //--------
  val duplState = (
    (optModHazardKind == PipeMemRmw.modHazardKindDupl) generate (
      KeepAttribute(Bool())
    )
  )

  val valid = KeepAttribute(Bool())
  val ready = KeepAttribute(Bool())
  val fire = KeepAttribute(Bool())
  val memAddr = Vec.fill(modRdPortCnt)(
    UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
  )
  //def modMemAddr = memAddr(0)
  //val didReorderCommit = (optReorder) generate (
  //  Bool()
  //)
  val reqReorderCommit = (optReorder) generate (
    /*Vec.fill(modRdPortCnt)*/(Bool())
  )
  val didReorderCommit = (optReorder) generate (
    /*Vec.fill(modRdPortCnt)*/(Bool())
  )

  //// When `True`, read from the address `memAddr`
  //// When `False`, read from the address `dualRd.rReorderCommitHead`, 
  //// which provides in-order reads
  //val memAddrReorderValid = (optReorder) generate (
  //  Bool()
  //)
  //--------
  val hazardCmp = hazardCmpType()
  //--------
  //val modMemAddrRaw = (optUseModMemAddr) generate cloneOf(memAddr)
  //def modMemAddr = (
  //  if (optUseModMemAddr) {
  //    modMemAddrRaw
  //  } else { // if (!optUseModMemAddr)
  //    memAddr
  //  }
  //)
  //--------
  //val helperForceWr = Bool()
  //--------
  //val modMemWrSel = (modRdPortCnt > 1) generate (
  //  UInt(log2Up(modRdPortCnt) bits)
  //)
  val modMemWord = wordType()
  val rdMemWord = Vec.fill(modRdPortCnt)(wordType())
  //val (modMemWord, rdMemWord) = optSimpleIsWr match {
  //  case Some(myIsWr) => (
  //    if (myIsWr) (
  //      
  //    ) else ( // if (!myIsWr)
  //    )
  //  )
  //  case None => (
  //    (wordType(), wordType())
  //  )
  //}
  //val modMemWord = optSimpleIsWr match {
  //  case Some(myIsWr) => (
  //    (myIsWr) generate (
  //      wordType()
  //    )
  //  )
  //  case None => (
  //    wordType()
  //  )
  //}
  //val rdMemWord = optSimpleIsWr match {
  //  case Some(myIsWr) => (
  //    (!myIsWr) generate (
  //      wordType()
  //    )
  //  )
  //  case None => (
  //    wordType()
  //  )
  //}

  //val wrMemWord = wordType()
  //val rdValid = Bool()

  //val fwdId = (
  //  SInt(log2Up(modStageCnt) + 3 bits)
  //)

  //val doCancelFront = (
  //  Bool()
  //)
  // hazard for when an address is already in the pipeline 
  val hazardId = (
    //optEnableModDuplicate
    optModHazardKind == PipeMemRmw.modHazardKindDupl
  ) generate (
    //Vec.fill(modRdPortCnt)
    (
      SInt(log2Up(
        PipeMemRmw.numPostFrontStages(
          //doModInModFront=doModInModFront,
          optModHazardKind=optModHazardKind,
          //optModFwdToFront=false,
          modStageCnt=modStageCnt,
        )
      ) + 4 bits)
      //UInt(log2Up(modStageCnt) bits)
    )
  )
  //if (optEnableModDuplicate) {
  //  println(
  //    s"hazardId.getWidth: ${hazardId.getWidth}"
  //  )
  //}
  def getHazardIdIdleVal() = (
    -1
  )
  def doInitHazardId(): Unit = {
    //for (zdx <- 0 until modRdPortCnt) {
      hazardId/*(zdx)*/ := getHazardIdIdleVal()
    //}
  }
  //val rdId = (
  //  UInt(3 bits)
  //)

  //val dbgNonFmaxFwdCnt = (debug) generate (
  //  Vec.fill(wordCount)(UInt(12 bits))
  //)
  //def maxFwdCnt = 20

  //val dbgWantNonFmaxFwd = (debug) generate (
  //  Bool()
  //)

  //val frontDuplicateIt = Bool()
  //--------
  //val dbgModMemWord = (debug) generate (
  //  wordType()
  //)
  //val dbgMemReadSync = (debug) generate (
  //  wordType()
  //)
  //--------
}
trait PipeMemRmwPayloadBase[
  WordT <: Data,
  HazardCmpT <: Data,
] extends Bundle {
  //--------
  //// get the address of the memory to modify
  //def getMemAddr(): UInt
  //--------
  def setPipeMemRmwExt(
    inpExt: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    memArrIdx: Int,
  ): Unit
  //--------
  def getPipeMemRmwExt(
    outpExt: PipeMemRmwPayloadExt[WordT, HazardCmpT],
      // this is essentially a return value
    memArrIdx: Int,
  ): Unit
  //--------
  //def doPipeMemRmwHazardCheck(
  //  inpExt: PipeMemRmwPayloadExt[WordT, HazardCmpT],
  //): Bool
  //--------
  //// function to set the `ModT`'s memory word
  //// sample functionality:
  //// mod.memWord := word
  //def setMemWord(
  //  word: WordT
  //): Unit
  ////--------
  //// function to get the `ModT`'s memory word
  //// sample functionality:
  //// word := mod.memWord
  //def getMemWord(
  //  word: WordT, // this is essentially a return value
  //): Unit
  ////--------
  //def setRdValid(
  //  rdValid: Bool,
  //): Unit
  ////--------
  //def getRdValid(
  //  rdValid: Bool,  // this is essentially a return value
  //): Unit
  ////--------
}
object PipeMemRmw {
  def addrWidth(
    wordCount: Int,
  ) = log2Up(wordCount)
  //def kindRmw = 0
  //def kindSimpleDualPort = 1
  def numPostFrontStages(
    //doModInModFront: Boolean,
    optModHazardKind: Int,
    //optModFwdToFront: Boolean,
    modStageCnt: Int,
  ) = (
    numPostFrontPreWriteStages(
      //doModInModFront=doModInModFront,
      optModHazardKind=optModHazardKind,
      //optModFwdToFront=optModFwdToFront,
      modStageCnt=modStageCnt,
    )
    + 1
    //+ (if (doModInModFront) (0) else (1))
    //+ (if (doModInModFront) (-1) else (0))
  )
  def numPostFrontPreWriteStages(
    //doModInModFront: Boolean,
    optModHazardKind: Int,
    //optModFwdToFront: Boolean,
    modStageCnt: Int
  ) = (
    //modStageCnt
    //3 + modStageCnt //+ 1
    //2 + modStageCnt //+ 1
    //1 + modStageCnt
    //(if (doModInModFront) (0) else (1)) + modStageCnt //+ 1
    (
      if (
        optModHazardKind == PipeMemRmw.modHazardKindFwd
        //&& optModFwdToFront
      ) (
        1
        //0
      ) else (
        //2
        1
        //0
      )
      //1
    )
      + modStageCnt //+ 1
    //(if (doModInModFront) (-1) else (1)) + modStageCnt //+ 1
    //- 1
    //+ 1
  )
  def modHazardKindDont = 0
  def modHazardKindDupl = 1
  def modHazardKindFwd = 2
  def modWrIdx = 0
  def modRdIdxStart = 1

  def extIdxUp = 0
  def extIdxDown = 1
  def extIdxLim = 2
  def extIdxSingle = extIdxUp

  //def formalFwdStallKindNone = 0
  //def formalFwdStallKindHalt = 1
  //def formalFwdStallKindDuplicate = 2
}
case class PipeMemRmwDualRdTypeDisabled[
  WordT <: Data,
  HazardCmpT <: Data,
](
) extends Bundle
  with PipeMemRmwPayloadBase[WordT, HazardCmpT]
{
  //--------
  def setPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    memArrIdx: Int,
  ): Unit = {
  }
  def getPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    memArrIdx: Int,
  ): Unit = {
  }
  //--------
  //def getMemAddr(): UInt = U"1'd0"
  //def setMemWord(
  //  word: WordT
  //): Unit = {
  //}
  //def getMemWord(
  //  word: WordT // this is essentially a return value
  //): Unit = {
  //}
  //def setRdValid(
  //  rdValid: Bool,
  //): Unit = {
  //}
  //def getRdValid(
  //  rdValid: Bool,
  //): Unit = {
  //}
}
case class PipeMemRmwIo[
  WordT <: Data,
  HazardCmpT <: Data,
  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
](
  wordType: HardType[WordT],
  wordCount: Int,
  modType: HardType[ModT],
  modRdPortCnt: Int,
  modStageCnt: Int,
  //optDualRdType: Option[HardType[DualRdT]]=None,
  //optDualRdType: Option[HardType[DualRdT]]={
  //  //Some(HardType[PipeMemRmwDualRdTypeDisabled[WordT]]())
  //},
  dualRdType: HardType[DualRdT]=PipeMemRmwDualRdTypeDisabled[
    WordT, HazardCmpT
  ](),
  //optDualRdSize: Option[Int]=None,
  //dualRdSize: Int=0,
  optDualRd: Boolean=false,
  optReorder: Boolean=false,
  //optEnableModDuplicate: Boolean=true,
  optModHazardKind: Int=PipeMemRmw.modHazardKindDupl,
  optEnableClear: Boolean=false,
  vivadoDebug: Boolean=false,
) extends Area {
  //--------
  //val front = slave(
  //  Stream(PipeMemRmwFrontPayload(
  //    wordCount=wordCount,
  //  ))
  //)
  //val doHazardCheck = in(Bool())
  val clear = (optEnableClear) generate (
    /*slave*/(Flow(
      /*Vec.fill(modRdPortCnt)*/(
        UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
      )
    ))
  )
  //if (optEnableClear) {
  //  if (vivadoDebug) {
  //    clear.addAttribute("MARK_DEBUG", "TRUE")
  //  }
  //}

  // the commit head
  val reorderCommitHead = (optReorder) generate (
    //Reg(UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)) init(0x0)
    UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
  )
  //val reorderCommitTail = (optReorder) generate (
  //  UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
  //)

  // front of the pipeline (push)
  //val front = slave(Stream(modType()))
  val front = Node()
  val frontPayload = Payload(modType())
  //if (vivadoDebug) {
  //  front(frontPayload).addAttribute(
  //    "MARK_DEBUG", "TRUE"
  //  )
  //}

  // Use `modFront` and `modBack` to insert a pipeline stage for modifying
  // the`WordT`
  //val modFront = master(Stream(modType()))
  //val modBack = slave(Stream(modType()))
  val modFront = Node()
  val modFrontPayload = Payload(modType())
  //if (vivadoDebug) {
  //  modFront(modFrontPayload).addAttribute(
  //    "MARK_DEBUG", "TRUE"
  //  )
  //}
  val modBack = Node()
  val modBackPayload = Payload(modType())
  //if (vivadoDebug) {
  //  modBack(modBackPayload).addAttribute(
  //    "MARK_DEBUG", "TRUE"
  //  )
  //}

  val midModStages = (
    (
      modStageCnt > 0
      && (
        //optEnableModDuplicate
        optModHazardKind != PipeMemRmw.modHazardKindDont
      )
    ) generate (
      /*in*/(
        Vec.fill(
          modStageCnt //- 1 //- 2
        )(
          Vec.fill(PipeMemRmw.extIdxLim)(
            modType()
          )
        )
      )
    )
  )
  //val midModStages = (
  //  (
  //    modStageCnt > 0
  //    && (
  //      //optEnableModDuplicate
  //      optModHazardKind != PipeMemRmw.modHazardKindDont
  //    )
  //  ) generate (
  //    Payload(modType())
  //  )
  //)
  if (
    //optEnableModDuplicate
    optModHazardKind != PipeMemRmw.modHazardKindDupl
  ) {
   //if (vivadoDebug) {
   // midModStages.addAttribute("MARK_DEBUG", "TRUE")
   //}
  }
  //if (vivadoDebug) {
  //  frontPayload.addAttribute("MARK_DEBUG", "TRUE")
  //}

  // back of the pipeline (output)
  //val back = master(Stream(modType()))
  val back = Node()
  val backPayload = Payload(modType())
  //if (vivadoDebug) {
  //  back(backPayload).addAttribute(
  //    "MARK_DEBUG", "TRUE"
  //  )
  //}
  //--------
  //val optDualRd: Boolean = optDualRdType match {
  //  case Some(myOptDualRdType) => true
  //  case None => false
  //}
  //def dualRdType() = optDualRdType match {
  //  case Some(myOptDualRdType) => myOptDualRdType()
  //  case None => PipeMemRmwDualRdTypeDisabled[WordT]()
  //}
  //--------
  //val optDualRd = (dualRdSize > 0)
  val dualRdFront = (optDualRd) generate (
    //slave(
    //  Stream(dualRdType())
    //)
    Node()
  )
  val dualRdFrontPayload = (optDualRd) generate (
    Payload(dualRdType())
  )
  val dualRdBack = (optDualRd) generate (
    //master(
    //  Stream(dualRdType())
    //)
    Node()
  )
  val dualRdBackPayload = (optDualRd) generate (
    Payload(dualRdType())
  )
  //if (optDualRd && vivadoDebug) {
  //  dualRdFront(dualRdFrontPayload).addAttribute(
  //    "MARK_DEBUG", "TRUE"
  //  )
  //  dualRdBack(dualRdBackPayload).addAttribute(
  //    "MARK_DEBUG", "TRUE"
  //  )
  //}
  //--------
  //val (dualRdPush, dualRdPop) = optDualRdType match {
  //  case Some(myDualRdType) => {
  //    (
  //      slave(
  //        Stream(myDualRdType())
  //      ), // dualRdPush
  //      master(
  //        Stream(myDualRdType())
  //      ) // dualRdPop
  //    )
  //  }
  //  case None => (None, None)
  //}
}
//case class PipeDuplicateItCnt[
//  T <: Data
//](
//  dataType: HardType[T],
//  cntWidth: Int,
//  cntOverflow: 
//)

trait PipeMemRmwReorderFtable[
  WordT <: Data,
  //ExceptT <: Data,
] {
  //def getReorderValidBusyExceptFunc: (
  //  WordT,                  // word
  //): (Bool, Bool, ExceptT),  // (valid, busy, except)
  //--------
  def getValid(
    word: WordT, // whether or not the ROB entry has valid data 
  ): Bool // Vec[Bool]
  //def setValid(
  //  word: WordT,
  //  valid: Bool,
  //): Unit
  //--------
  def getBusyOrExceptThrowing(
    // this function indicates when an exception is throwing, so if the
    // return value is `True`, we cannot commit
    word: WordT,
  ): Bool //Vec[Bool]
  //def setBusy(
  //  word: WordT,
  //  busy: Bool,
  //): Unit
  //--------
  //def getAnyExceptThrowing(
  //  // this function indicates when an exception is throwing, so if the
  //  // return value is `True`, we cannot commit
  //  word: WordT,
  //): Bool
  //--------
}
// A Read-Modify-Write pipelined BRAM
case class PipeMemRmw[
  WordT <: Data,
  HazardCmpT <: Data,
  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
](
  wordType: HardType[WordT],
  wordCount: Int,
  hazardCmpType: HardType[HazardCmpT],
  modType: HardType[ModT],
  modRdPortCnt: Int,
  modStageCnt: Int,
  pipeName: String,
  linkArr: Option[ArrayBuffer[Link]]=None,
  memArrIdx: Int=0,
  //optDualRdType: Option[HardType[DualRdT]]=None,
  dualRdType: HardType[DualRdT]=PipeMemRmwDualRdTypeDisabled[
    WordT, HazardCmpT,
  ](),
  optDualRd: Boolean=false,
  optReorder: Boolean=false,
  //dualRdSize: Int=0,
  init: Option[Seq[WordT]]=None,
  initBigInt: Option[Seq[BigInt]]=None,
  //forFmax: Boolean=false,
  //optExtraCycleLatency: Boolean=false,
  //optDisableModRd: Boolean=false,
  //optEnableModDuplicate: Boolean=true,
  optModHazardKind: Int=PipeMemRmw.modHazardKindDupl,
  //optModFwdToFront: Boolean=false,
  //optRegFileRdPorts: Int=0,
  optEnableClear: Boolean=false,
  memRamStyle: String="auto",
  vivadoDebug: Boolean=false,
)(
  //--------
  doHazardCmpFunc: Option[
    (
      PipeMemRmwPayloadExt[WordT, HazardCmpT],  // curr
      PipeMemRmwPayloadExt[WordT, HazardCmpT],  // prev
      Int,                                      // zdx
      Boolean,                                  // isPostDelay
    ) => Bool
  ]=None,
  doPrevHazardCmpFunc: Boolean=false,
  //--------
  doModInModFrontFunc: Option[
    //[
    //  ModT <: Data,
    //]
    (
      //PipeMemRmwPayloadExt[WordT, HazardCmpT],  // inp
      //PipeMemRmwPayloadExt[WordT, HazardCmpT],  // outp
      ModT, // outp
      ModT, // inp
      CtrlLink, // cMid0Front
      WordT,    // myModMemWord
      //Vec[WordT],  // myRdMemWord
    ) => Unit
  ]=None,
  doFwdFunc: Option[
    (
      //ModT, //
      UInt, // stage index
      Vec[PipeMemRmwPayloadExt[WordT, HazardCmpT]], // myUpExtDel
      Int,  // zdx
    ) => WordT
  ]=None,
  //--------
  reorderFtable: Option[PipeMemRmwReorderFtable[WordT]]=None,
  //setReorderBusyFunc: Option[
  //  (
  //    WordT,    // self
  //    Bool,     // busy
  //  ) => Unit
  //]=None,
  //--------
  //formalFwdStallKind: Int=0,
  //formalFwdStallCnt: Int=0,
  //formalFwdStallAddr: Option[UInt]=None,
  //--------
)
//(
//  getModAddr: (
//    ModT,   // mod
//  ) => UInt,
//  setModWord: (
//    ModT,   // mod
//    WordT,  // word
//    //Stream[ModT],
//  ) => Unit,
//  getModWord: (
//    ModT,   // mod
//    WordT,  // word
//  ) => Unit,
//)
extends Area {
  def extIdxUp = PipeMemRmw.extIdxUp
  def extIdxDown = PipeMemRmw.extIdxDown
  def extIdxLim = PipeMemRmw.extIdxLim
  def extIdxSingle = PipeMemRmw.extIdxSingle
  //--------
  //if (optModFwdToFront) {
  //  assert(optModHazardKind == PipeMemRmw.modHazardKindFwd)
  //} else (
  //  //assert(optModHazardKind != PipeMemRmw.modHazardKindFwd)
  //)
  //--------
  def debug: Boolean = {
    GenerationFlags.formal {
      return true
    }
    return false
  }
  //--------
  //val io = slave(
  //  PipeMemIo(
  //    wordType=wordType(),
  //    wordCount=wordCount,
  //  )
  //)
  //val io = PipeMemRmwIo(
  //  modType=modType(),
  //  wordCount=wordCount,
  //  optDualRdType=optDualRdType,
  //)
  val io = PipeMemRmwIo[
    WordT,
    HazardCmpT,
    ModT,
    DualRdT,
  ](
    wordType=wordType(),
    wordCount=wordCount,
    //hazardCmpType=hazardCmpType(),
    modType=modType(),
    modRdPortCnt=modRdPortCnt,
    modStageCnt=modStageCnt,
    dualRdType=dualRdType(),
    //dualRdSize=dualRdSize,
    optDualRd=optDualRd,
    optReorder=optReorder,
    //optEnableModDuplicate=optEnableModDuplicate,
    optModHazardKind=optModHazardKind,
    optEnableClear=optEnableClear,
    vivadoDebug=vivadoDebug,
  )
  //--------
  def mkMem() = {
    val ret = Mem(
      wordType=wordType(),
      wordCount=wordCount,
    )
      .addAttribute("ram_style", memRamStyle)
    init match {
      case Some(myInit) => {
        //assert(myInit.size == wordCount)
        assert(initBigInt == None)
        ret.init(myInit)
      }
      case None => {
      }
    }
    initBigInt match {
      case Some(myInitBigInt) => {
        //assert(myInitBigInt.size == wordCount)
        assert(init == None)
        ret.initBigInt(myInitBigInt)
      }
      case None => {
        //ret.initBigInt({
        //  //val tempArr = new ArrayBuffer[BigInt]()
        //  //for (idx <- 0 until wordCount) {
        //  //  tempArr += BigInt(0)
        //  //}
        //  //tempArr.toSeq
        //  Array.fill(wordCount)(BigInt(0)).toSeq
        //})
      }
    }

    //if (vivadoDebug) {
    //  ret.addAttribute("MARK_DEBUG", "TRUE")
    //}
    ret
  }

  def getReorderValid(
    word: WordT, // whether or not the ROB entry has valid data 
  ): Bool /* Vec[Bool]*/ = reorderFtable match {
    case Some(myReorderFtable) => {
      myReorderFtable.getValid(word)
    }
    case None => {
      True
    }
  }
  def getReorderBusyEtc(
    // this function indicates when an exception is throwing, so if the
    // return value is `True`, we cannot commit
    word: WordT,
  ): Bool = reorderFtable match {
    case Some(myReorderFtable) => {
      myReorderFtable.getBusyOrExceptThrowing(word)
    }
    case None => {
      False
    }
  }
  val modMem = (
    //optEnableModDuplicate
    optModHazardKind != PipeMemRmw.modHazardKindDont
  ) generate (
    Array.fill(modRdPortCnt)(
      mkMem()
    )
  )
  //val dualRdMemArr = new ArrayBuffer[Mem[WordT]]()
  //for (idx <- 0 until dualRdSize) {
  //  dualRdMemArr += mkMem()
  //}
  val dualRdMem = (io.optDualRd) generate (
    mkMem()
  )
  def memWriteIterate(
    writeFunc: (Mem[WordT]) => Unit
  ): Unit = {
    if (
      //optEnableModDuplicate
      optModHazardKind != PipeMemRmw.modHazardKindDont
    ) {
      for (zdx <- 0 until modRdPortCnt) {
        writeFunc(modMem(zdx))
      }
    }
    if (io.optDualRd) {
      writeFunc(dualRdMem)
    }
    //for (idx <- 0 until dualRdSize) {
    //  writeFunc(dualRdMemArr(idx))
    //}
  }
  def memWriteAll(
    address: UInt,
    data: WordT,
    enable: Bool=null,
    //mask: Bits=null,
  ): Unit = {
    memWriteIterate(
      (item: Mem[WordT]) => {
        item.write(
          address=address,
          data=data,
          enable=enable,
          //mask=mask,
        )
      }
    )
  }
  //def memIterate(
  //  readFunc: (Mem[WordT]) => WordT
  //): Unit = {
  //  readFunc(modMem)
  //  for (idx <- 0 until dualRdSize) {
  //    readFunc(dualRdMemArr(idx))
  //  }
  //}
  //--------
  def mkExt(myVivadoDebug: Boolean=false) = {
    val ret = Vec.fill(extIdxLim)(
      PipeMemRmwPayloadExt(
        wordType=wordType(),
        wordCount=wordCount,
        hazardCmpType=hazardCmpType(),
        modRdPortCnt=modRdPortCnt,
        modStageCnt=modStageCnt,
        //doModInModFront=doModInModFrontFunc match {
        //  case Some(myDoModSingleStageFunc) => true
        //  case None => false
        //},
        //optEnableModDuplicate=optEnableModDuplicate,
        optModHazardKind=optModHazardKind,
        optReorder=optReorder,
      )
    )
    if (vivadoDebug && myVivadoDebug) {
      //ret.addAttribute("MARK_DEBUG", "TRUE")
      //ret.memAddr.addAttribute("MARK_DEBUG", "TRUE")
      //ret.hazardCmp.addAttribute("MARK_DEBUG", "TRUE")
      //if (optEnableModDuplicate) {
      //  ret.hazardId.addAttribute("MARK_DEBUG", "TRUE")
      //}
      //ret.modMemWord.addAttribute("MARK_DEBUG", "TRUE")
    }
    ret
  }
  val myLinkArr = (
    linkArr match {
      case Some(theLinkArr) => {
        theLinkArr
      }
      case None => {
        PipeHelper.mkLinkArr()
      }
    }
  )
  val mod = new Area {
    //--------
    val rReorderCommitHead = (optReorder) generate (
      Reg(UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)) init(0x0)
    )
    if (optReorder) {
      io.reorderCommitHead := rReorderCommitHead
    }
    //--------
    val front = new Area {
      def myHazardCmpFunc(
        curr: PipeMemRmwPayloadExt[WordT, HazardCmpT],
        prev: PipeMemRmwPayloadExt[WordT, HazardCmpT],
        zdx: Int,
        isPostDelay: Boolean,
        //idx: Int,
        //hazardIdMsbCond: Boolean,
      ): Bool = (
        (
          doHazardCmpFunc match {
            case Some(myHazardCmpFuncA) => (
              myHazardCmpFuncA(
                //upExt(0),
                //rUpExtDel(0),
                curr,
                prev,
                zdx,
                isPostDelay,
              )
            )
            case None => (
              PipeMemRmwPayloadExt.defaultDoHazardCmpFunc(
                //upExt(0),
                //rUpExtDel(0),
                curr,
                prev,
                zdx,
                isPostDelay,
              )
            )
          }
        ) && (
          True
          //if (hazardIdMsbCond) (
          //  prev.hazardId.msb //^ hazardIdMsbCond
          //) else (
          //  True
          //)
        )
      )
      //def findFirstFunc2(
      //  currMemAddr: UInt,
      //  prevMemAddrPair: (UInt, UInt),
      //  curr: PipeMemRmwPayloadExt[
      //    WordT,
      //    HazardCmpT,
      //  ],
      //  prevPair: (
      //    PipeMemRmwPayloadExt[
      //      WordT,
      //      HazardCmpT,
      //    ],
      //    PipeMemRmwPayloadExt[
      //      WordT,
      //      HazardCmpT,
      //    ],
      //  ),
      //): Bool = (
      //  Mux(
      //    currMemAddr
      //    === prevMemAddrPair._1
      //  )
      //)
      def findFirstFunc(
        currMemAddr: UInt,
        prevMemAddr: UInt,
        curr: PipeMemRmwPayloadExt[
          WordT,
          HazardCmpT,
        ],
        prev: PipeMemRmwPayloadExt[
          WordT,
          HazardCmpT,
        ],
        zdx: Int,
        isPostDelay: Boolean,
        doValidCheck: Boolean=(
          //false
          true
        ),
        //fireCnt: UInt,
        //idx: Int,
        //memAddr: UInt,
      ) = (
      //(upExt(1).memAddr === prev.memAddr)
        (
          if (!isPostDelay) (
            (
              //upExtRealMemAddr
              currMemAddr
              //=== prev.memAddr
              === prevMemAddr
            )
            ////&& prev.hazardId.msb
            ////&& (prev.hazardId === 0)
            ////&& upExt(1).hazardId.msb
            && (
              if (doPrevHazardCmpFunc) (
                myHazardCmpFunc(
                  curr/*upExt(1)*/,
                  prev,
                  zdx,
                  isPostDelay
                )
              ) else (
                True
              )
            )
          ) else (
            //(upExtRealMemAddr === prev.memAddr)
            //|| 
            myHazardCmpFunc(
              curr/*upExt(1)*/,
              prev,
              zdx,
              isPostDelay
            )
          )
        ) && (
          //True
          (
            //curr.fire
            //|| (
            //  RegNextWhen(True, curr.fire) init(False)
            //)
            curr.valid
          )
          && 
          (
            if (doValidCheck) (
              //prev.valid
              prev.fire
              || (
                RegNextWhen(
                  //True
                  pastValidAfterReset,
                  //Mux(
                  //  ClockDomain.isResetActive,
                  //  False,
                  //  True
                  //),
                  (
                    prev.fire
                    //&& !ClockDomain.isResetActive
                    //&& fireCnt > 0
                  )
                ) init(False)
              )
            ) else (
              True
            )
          )
          //curr.valid && prev.valid
        )
      )
      val pipe = PipeHelper(linkArr=myLinkArr)
      //val inpPipePayload = Payload(modType())
      def inpPipePayload = io.frontPayload
      val midPipePayload = Payload(modType())
      //val outpPipePayload = Payload(modType())
      def outpPipePayload = io.modFrontPayload
      val myRdMemWord = Vec.fill(modRdPortCnt)(wordType())
      val myNonFwdRdMemWord = Vec.fill(modRdPortCnt)(wordType())
      val myFwdRdMemWord = Vec.fill(modRdPortCnt)(wordType())
      //val rRdMemWord1 = Reg(wordType()) init(myRdMemWord.getZero)
      val dbgRdMemWord = (debug) generate (
        Payload(wordType())
      )
      val myUpExtDel = KeepAttribute(
        Vec.fill(
          PipeMemRmw.numPostFrontStages
          //PipeMemRmw.numPostFrontPreWriteStages
          (
            //doModInModFront=doModInModFrontFunc match {
            //  case Some(myDoModSingleStageFunc) => true
            //  case None => false
            //},
            optModHazardKind=optModHazardKind,
            //optModFwdToFront=optModFwdToFront,
            modStageCnt=modStageCnt,
          )
          //+ 1 
          //+ (
          //  if (optModHazardKind != PipeMemRmw.modHazardKindFwd) (
          //    0
          //  ) else (
          //    -1
          //  )
          //)
          //- 1
        )(
          /*Reg*/(
            //Vec.fill(extIdxLim)(
              mkExt(myVivadoDebug=true)
            //)
          )
          //init(mkExt().getZero)
        )
      )
      //val myUpExtDelFwd = /*(optModFwdToFront) generate*/ (
      //  KeepAttribute(
      //    mkExt(myVivadoDebug=true)
      //  )
      //)
      val myUpExtDel2 = KeepAttribute(
        Vec.fill(
          PipeMemRmw.numPostFrontPreWriteStages
          //PipeMemRmw.numPostFrontPreWriteStages
          (
            //doModInModFront=doModInModFrontFunc match {
            //  case Some(myDoModSingleStageFunc) => true
            //  case None => false
            //},
            optModHazardKind=optModHazardKind,
            //optModFwdToFront=optModFwdToFront,
            modStageCnt=modStageCnt,
          )
          //+ 1
          //+ (
          //  if (optModHazardKind != PipeMemRmw.modHazardKindFwd) (
          //    0
          //  ) else (
          //    -1
          //  )
          //)
          //- 1
        )(
          /*Reg*/(
            //Vec.fill(extIdxLim)(
              mkExt(myVivadoDebug=true)
            //)
          )
          //init(mkExt().getZero)
        )
      )
      for (idx <- 0 until myUpExtDel2.size) {
        myUpExtDel2(idx) := myUpExtDel(idx + 1)
      }
      //val myUpExtDelFull = KeepAttribute(
      //  Vec.fill(myUpExtDel.size)(
      //    mkExt()
      //  )
      //)
      ////val rMyUpExtDelFinal = KeepAttribute(
      ////  Reg(mkExt())
      ////)
      ////rMyUpExtDelFinal.init(rMyUpExtDelFinal.getZero)
      //for (idx <- 0 until myUpExtDelFull.size) {
      //  myUpExtDelFull(idx) := myUpExtDel(idx)
      //}
      val myUpExtDelFullFindFirstVecNotPostDelay = KeepAttribute(
        Vec.fill(modRdPortCnt)(
          Vec.fill(myUpExtDel.size)(
            Vec.fill(extIdxLim)(
              Bool()
            )
          )
        )
      )
      val myUpExtDelFullFindFirstVecIsPostDelay = KeepAttribute(
        Vec.fill(modRdPortCnt)(
          Vec.fill(myUpExtDel.size)(
            Vec.fill(extIdxLim)(
              Bool()
            )
          )
        )
      )
      //if (vivadoDebug) {
      //  myUpExtDel.addAttribute("MARK_DEBUG", "TRUE")
      //}
      //println(s"myUpExtDelFull.size: ${myUpExtDelFull.size}")
      if (
        (
          //optEnableModDuplicate
          optModHazardKind != PipeMemRmw.modHazardKindDont
        )
        && modStageCnt > 0
      ) {
        //assert(modStageCnt > 0)
        for (
          //idx <- 0 until modStageCnt - 1
          idx <- 0 until io.midModStages.size
        ) {
          val myExt = /*Vec.fill(extIdxLim)*/(mkExt())
          for (extIdx <- 0 until extIdxLim) {
            io.midModStages(idx)(extIdx).getPipeMemRmwExt(
              outpExt=myExt(extIdx),
              memArrIdx=memArrIdx,
            )
            val tempIdx = (
              PipeMemRmw.numPostFrontPreWriteStages(
                //doModInModFront=doModInModFrontFunc match {
                //  case Some(myDoModSingleStageFunc) => true
                //  case None => false
                //},
                optModHazardKind=optModHazardKind,
                //optModFwdToFront=optModFwdToFront,
                modStageCnt=modStageCnt
              )
              - modStageCnt
              //- 1
              ////+ 1
              + idx 
            )
            //println(
            //  s"io.midModStages.size=${io.midModStages.size} "
            //  + s"modStageCnt=${modStageCnt} "
            //  + s"idx=${idx} tempIdx=${tempIdx}"
            //)
            myUpExtDel(
              tempIdx
            )(extIdx) := myExt(extIdx)
          }
        }
      }


      val cFront = pipe.addStage(
        name=pipeName + "_Front",
        optIncludeS2M=(
          //false
          true
          //optModHazardKind != PipeMemRmw.modHazardKindFwd
          //|| (
          //  doFwdFunc match {
          //    case Some(myDoFwdFunc) => false
          //    case None => true
          //  }
          //)
        )
      )
      val cIoFront = DirectLink(
        up=io.front,
        down=cFront.up,
      )
      val cMid0Front = pipe.addStage(
        name=pipeName + "_Mid0Front",
        //optIncludeStage=(
        //  doModInModFrontFunc match {
        //    case Some(myDoModSingleStageFunc) => false
        //    case None => true
        //  }
        //),
        optIncludeS2M=(
          //false
          true
          //optModHazardKind != PipeMemRmw.modHazardKindFwd
          //|| (
          //  doFwdFunc match {
          //    case Some(myDoFwdFunc) => false
          //    case None => true
          //  }
          //)
        ),
        //finish=true,
      )
      val nextDidFwd = (
        (
          optModHazardKind == PipeMemRmw.modHazardKindFwd
          //optModFwdToFront
        ) generate (
          Vec.fill(modRdPortCnt)(
            Vec.fill(
              //2 // old code, no longer need this...
              1
            )(Bool())
          )
        )
      )
      val rDidFwd = (
        (
          optModHazardKind == PipeMemRmw.modHazardKindFwd
          //optModFwdToFront
        ) generate (
          //Array.fill(2)(Payload(Bool()))
          //RegNextWhen
          RegNext
          (
            nextDidFwd,
            //cFront.down.isReady
            ////cFront.down.isFiring
            ////cFront.down.isValid
            ////cMid0Front.up.isValid
          )
          //init(nextDidFwd.getZero)
        )
      )
      for (zdx <- 0 until modRdPortCnt) {
        //myRdMemWord(zdx) := myNonFwdRdMemWord(zdx)
        myRdMemWord(zdx) := /*Mux*/(
          //!rDidFwd(zdx)(0),
          ////RegNext(!nextDidFwd(zdx)(0)) init(False),
          ////!(
          ////  //RegNext(nextDidFwd(zdx)(0)) init(False)
          ////  nextDidFwd(zdx)(0)
          ////),
          myNonFwdRdMemWord(zdx),
          //myFwdRdMemWord(zdx)
        )
      }
      if (
        optModHazardKind == PipeMemRmw.modHazardKindFwd
        //optModFwdToFront
      ) {
        for (zdx <- 0 until rDidFwd.size) {
          for (idx <- 0 until rDidFwd(zdx).size) {
            rDidFwd(zdx)(idx).init(nextDidFwd(zdx)(idx).getZero)
          }
        }
      }
      myLinkArr += cIoFront
      //val cMidMinus1Front = pipe.addStage(
      //  name=pipeName + "_MidMinus1Front",
      //  optIncludeS2M=false,
      //)
      //println(myUpExtDelFull.size)
      //--------
      val myUpExtDel2FindFirstVec = KeepAttribute(
        Vec.fill(modRdPortCnt)(
          Vec.fill(extIdxLim)(
            Vec.fill(
              //myUpExtDelFull.size
              myUpExtDel2.size
              //- 1
              //- 1
              //+ (
              //  doModInModFrontFunc match {
              //    //case Some(myDoModSingleStageFunc) => 0
              //    //case None => -1
              //    case Some(myDoModSingleStageFunc) => 0
              //    case None => (
              //      -1
              //    )
              //  }
              //)
            )(
              Bool()
            )
          )
        )
      )
      //--------
      val tempMyUpExtDelFrontFindFirstVec = KeepAttribute(
        Vec.fill(modRdPortCnt)(
          Vec.fill(1)(
            //Vec.fill(extIdxLim)(
              Bool()
            //)
          )
        )
      )
      //val tempMyUpExtDel2FindFirstVec = KeepAttribute(
      //  Vec.fill(modRdPortCnt)(
      //    Vec.fill(
      //      myUpExtDel2.size
      //      //myUpExtDel2.size
      //      //- 1
      //      //- 1
      //      //+ (
      //      //  doModInModFrontFunc match {
      //      //    //case Some(myDoModSingleStageFunc) => 0
      //      //    //case None => -1
      //      //    case Some(myDoModSingleStageFunc) => 0
      //      //    case None => (
      //      //      -1
      //      //    )
      //      //  }
      //      //)
      //    )(
      //      //Bool()
      //      //(Bool, UInt(1 bits))
      //      Vec.fill(extIdxLim)(
      //        UInt(2 bits)
      //      )
      //    )
      //  )
      //)
      //println(myUpExtDel2FindFirstVec.size)
      //for (idx <- 0 until myUpExtDel2FindFirstVec.size) {
      //  myUpExtDel2FindFirstVec(idx) := (
      //    //RegNextWhen(
      //      myUpExtDelFullFindFirstVecNotPostDelay(
      //        idx
      //        + 1
      //        //+ (
      //        //  doModInModFrontFunc match {
      //        //    case Some(myDoModSingleStageFunc) => 0
      //        //    case None => 1
      //        //  }
      //        //)
      //      ),
      //    //  cMid0Front.down.isFiring
      //    //)
      //    //init(myUpExtDel2FindFirstVec(idx).getZero)
      //  )
      //}
      //val cMid1Front = pipe.addStage(
      //  name=pipeName + "_Mid1Front",
      //  optIncludeS2M=false,
      //)
      //val cMid2Front = pipe.addStage(
      //  name=pipeName + "_Mid2Front",
      //  //optIncludeS2M=false,
      //)
      val cLastFront = pipe.addStage(
        name=pipeName + "_LastFront", 
        finish=true,
      )
      val cIoModFront = DirectLink(
        up=(
          cLastFront.down
          //cMid0Front.down,
        ),
        down=io.modFront,
      )
      myLinkArr += cIoModFront
      //--------
    }
    val back = new Area {
      //val dbgDoClear = (optEnableClear) generate (
      //  KeepAttribute(Bool())
      //)
      val dbgDoWrite = /*(debug) generate*/ (
        KeepAttribute(Bool())
      )
      val myWriteAddr = KeepAttribute(
        cloneOf(front.myUpExtDel(0)(0).memAddr(PipeMemRmw.modWrIdx))
      )
      val myWriteData = KeepAttribute(
        cloneOf(front.myUpExtDel(0)(0).modMemWord)
      )
      val myWriteEnable = KeepAttribute(Bool())
      if (vivadoDebug) {
        //myWriteAddr.addAttribute("MARK_DEBUG", "TRUE")
        //myWriteData.addAttribute("MARK_DEBUG", "TRUE")
        //myWriteEnable.addAttribute("MARK_DEBUG", "TRUE")
      }
      val pipe = PipeHelper(linkArr=myLinkArr)
      //val pipePayload = Payload(modType())
      def pipePayload = io.modBackPayload

      //val cModBack = pipe.addStage("ModBack")
      val cBack = pipe.addStage(
        name=pipeName + "_Back",
        //optIncludeStage=true,
        optIncludeS2M=(
          //doModInModFrontFunc match {
          //  case Some(myDoModSingleStageFunc) => false
          //  case None => true
          //}
          true
          //optModHazardKind != PipeMemRmw.modHazardKindFwd
          //|| (
          //  doFwdFunc match {
          //    case Some(myDoFwdFunc) => false
          //    case None => true
          //  }
          //)
        )
      )
      val cIoModBack = DirectLink(
        up=io.modBack,
        down=cBack.up,
      )
      myLinkArr += cIoModBack
      val cLastBack = pipe.addStage(
        name=pipeName + "_LastBack",
        finish=true,
      )
      val cIoBack = DirectLink(
        up=(
          cLastBack.down
          //cBack.down
        ),
        down=io.back,
      )
      myLinkArr += cIoBack
      cIoBack.up(io.backPayload) := cIoBack.up(pipePayload)
      //pipe.first.up.driveFrom(io.modBack)(
      //  con=(node, payload) => {
      //    node(pipePayload) := payload 
      //  }
      //)
      val rTempWord = /*(debug) generate*/ KeepAttribute(
        Reg(wordType())
      )
      if (
        //debug
        true
      ) {
        rTempWord.init(rTempWord.getZero)
      }
      if (vivadoDebug) {
        //rTempWord.addAttribute("MARK_DEBUG", "TRUE")
      }
      //when (cBack.up.isValid) {
      //}
      //val tempBackStm = cloneOf(io.back)

      //pipe.last.down.driveTo(io.back)(
      //  con=(payload, node) => {
      //    payload := node(pipePayload)
      //  }
      //)

      //io.back <-/< tempBackStm.haltWhen(
      //  !(RegNextWhen(True, io.front.fire) init(False))
      //)
      //io.back << tempBackStm
    }
    //--------
  }
  val myUpExtDel = mod.front.myUpExtDel
  println(myUpExtDel.size, myUpExtDel(0).size)
  val cFront = mod.front.cFront
  //--------
  val cFrontArea = new cFront.Area {
    //--------
    val upExt = Vec.fill(2)(mkExt()).setName("cFrontArea_upExt")
    val upExtRealMemAddr = /*Vec.fill(extIdxLim)*/(
      cloneOf(upExt(1)(0).memAddr)
    )
    //upExt(1) := upExt(0)
    val tempCond = KeepAttribute(Bool())
    for (extIdx <- 0 until extIdxLim) {
      upExt(1)(extIdx) := (
        RegNext(upExt(1)(extIdx)) init(upExt(1)(extIdx).getZero)
      )
    }
    for (zdx <- 0 until modRdPortCnt) {
      //for (extIdx <- 0 until extIdxLim) {
        upExtRealMemAddr/*(extIdx)*/(zdx) := (
          RegNext(upExtRealMemAddr/*(extIdx)*/(zdx))
          init(upExtRealMemAddr/*(extIdx)*/(zdx).getZero)
        )
      //}
    }
    //when (
    //  tempCond
    //  //up.isFiring
    //) {
    //  upExt(1) := upExt(0)
    //}
    upExt(1).allowOverride
    upExtRealMemAddr.allowOverride
    //def savedIdx = 2
    //val lastUpExt = mkExt().setName("cFrontArea_lastUpExt")
    //val backUpExt = mkExt().setName("cFrontArea_backUpExt")

    val tempUpMod = Vec.fill(2)(
      //Vec.fill(extIdxLim)(
        modType()
      //)
    )
    tempUpMod(0).allowOverride
    tempUpMod(0) := up(mod.front.inpPipePayload)
    tempUpMod(0).getPipeMemRmwExt(
      outpExt=upExt(0)(extIdxSingle),
      memArrIdx=memArrIdx,
    )
    //--------
    //--------
    val nextHazardId = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.modHazardKindDupl
    ) generate (
      KeepAttribute(cloneOf(upExt(1)(0).hazardId))
    )
    val rHazardId = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.modHazardKindDupl
    ) generate (
      KeepAttribute(
        RegNext(nextHazardId)
        init(
          //S(nextHazardId.getWidth bits, default -> True)

          //-1
          upExt(1)(extIdxDown).getHazardIdIdleVal()
        )
      )
    )
    if (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.modHazardKindDupl
    ) {
      nextHazardId := rHazardId
      //when (isValid) {
          upExt(1)(extIdxDown).hazardId := nextHazardId
      //} otherwise {
        //upExt(1).hazardId := rHazardId
      //}
    }
    //--------
    val hazardIdMinusOne = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.modHazardKindDupl
    ) generate (
      rHazardId - 1
    )
    //--------
    //--------
    object State extends SpinalEnum(
      defaultEncoding=binarySequential
    ) {
      val
        IDLE,
        DELAY
        //WAIT_CLEAR
        = newElement();
    }
    val nextState = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.modHazardKindDupl
    ) generate (
      KeepAttribute(State())
    )
    val rState = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.modHazardKindDupl
    ) generate (
      KeepAttribute(RegNext(nextState) init(State.IDLE))
    )
    //val rPostDuplicateCnt = Reg(cloneOf(upExt(0).hazardId))
    //val myUpExtDel = (optEnableModDuplicate) generate (
    //  mod.front.myUpExtDel
    //)
    val rPrevStateWhen = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.modHazardKindDupl
    ) generate (
      KeepAttribute(
        RegNextWhen(rState, down.isFiring) init(State.IDLE)
      )
    )
    //val tempMyUpExtDelFindFirst1 = (optEnableModDuplicate) generate (
    //  KeepAttribute(
    //    myUpExtDel.sFindFirst(
    //      //myHazardCmpFunc(upExt(0), _, true)
    //      //upExt(0).hazardId.msb
    //      //|| _.hazardId.msb
    //      (prev) => (
    //        //upExt(0).memAddr === prev.memAddr
    //        ////&& prev.hazardId.msb
    //        //////&& myHazardCmpFunc(upExt(0), prev)
    //        //&& 
    //        myHazardCmpFunc(upExt(1), prev)
    //      )
    //    )
    //    .setName("cFrontArea_tempMyUpExtDelFindFirst1")
    //  )
    //)
    val nextDidChangeState = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.modHazardKindDupl
    ) generate (
      KeepAttribute(Bool())
    )
    val rDidChangeState = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.modHazardKindDupl
    ) generate (
      KeepAttribute(RegNext(nextDidChangeState)) init(True)
    )
    val rDidDelayItIdle = Reg(Bool()) init(False)
    //val myUpExtDelFull = mod.front.myUpExtDelFull
    val myUpExtDelFullFindFirstVecNotPostDelay = (
      mod.front.myUpExtDelFullFindFirstVecNotPostDelay
    )
    val myUpExtDelFullFindFirstVecIsPostDelay = (
      mod.front.myUpExtDelFullFindFirstVecIsPostDelay
    )
    for (zdx <- 0 until modRdPortCnt) {
      for (
        idx <- 0 until myUpExtDelFullFindFirstVecNotPostDelay(zdx).size
      ) {
        for (extIdx <- 0 until extIdxLim) {
          val tempMyUpExt = /*Mux*/(
            myUpExtDel(idx)(extIdx)
          )
          myUpExtDelFullFindFirstVecNotPostDelay(zdx)(idx)(extIdx) := (
            mod.front.findFirstFunc(
              currMemAddr=upExtRealMemAddr/*(extIdx)*/(zdx),
              prevMemAddr=tempMyUpExt.memAddr(zdx),
              curr=upExt(extIdx)(extIdx),
              prev=tempMyUpExt,
              zdx=zdx,
              isPostDelay=false,
              //idx=idx,
            )
          )
        }
      }
    }

    //for (
    //  idx <- 0 until myUpExtDelFullFindFirstVecNotPostDelay.size
    //) {
    //  myUpExtDelFullFindFirstVecIsPostDelay(idx) := findFirstFunc(
    //    prev=myUpExtDel(idx),
    //    isPostDelay=true,
    //    //idx=idx,
    //  )
    //}
    val tempMyUpExtDelFindFirstNotPostDelay = (
      (
        //optEnableModDuplicate
        optModHazardKind == PipeMemRmw.modHazardKindDupl
      ) generate (
        new ArrayBuffer[(Bool, UInt)]()
      )
    )
    if (optModHazardKind == PipeMemRmw.modHazardKindDupl) {
      //for (extIdx <- 0 until extIdxLim) {
        for (zdx <- 0 until modRdPortCnt) {
          tempMyUpExtDelFindFirstNotPostDelay/*(extIdx)*/ += {
            def myFindFirstFunc(
              toCmp: Vec[Bool]
            ): Bool = (
              toCmp(0) === True
              || toCmp(1) === True
            )
            val toAdd = (
              //myUpExtDel
              ///*myUpExtDel*/.sFindFirst(
              //  ////myHazardCmpFunc(upExt(0), _, true)
              //  ////upExt(0).memAddr === _.memAddr
              //  ////&& myHazardCmpFunc(upExt(0), _)
              //  //(prev) => (
              //  //  //(upExt(1).memAddr === prev.memAddr)
              //  //  (upExtRealMemAddr === prev.memAddr)
              //  //  //&& prev.hazardId.msb
              //  //  //&& (prev.hazardId === 0)
              //  //  //&& upExt(1).hazardId.msb
              //  //  && myHazardCmpFunc(upExt(1), prev)
              //  //)
              //  findFirstFunc(_)
              //  //&& myHazardCmpFunc(upExt(0), prev)
              //)
              myUpExtDelFullFindFirstVecNotPostDelay(zdx).sFindFirst(
                //_ === True
                myFindFirstFunc(_)
              )
            )
            toAdd.setName(
              s"cFrontArea_tempMyUpExtDelFindFirstNotPostDelay_${zdx}"
            )
            toAdd
          }
        }
      //}
    }
    //val tempMyUpExtDelFindFirstIsPostDelay = (
    //  (optEnableModDuplicate) generate (
    //    KeepAttribute(
    //      myUpExtDelFullFindFirstVecIsPostDelay.sFindFirst(_ === True)
    //      .setName("cFrontArea_tempMyUpExtDelFindFirstIsPostDelay")
    //    )
    //  )
    //)
    //val tempMyUpExtDelFindFirst2 = (optEnableModDuplicate) generate (
    //  KeepAttribute(
    //    myUpExtDel.sFindFirst(
    //      //myHazardCmpFunc(upExt(0), _, true)
    //      //upExt(0).memAddr === _.memAddr
    //      //&& myHazardCmpFunc(upExt(0), _)
    //      (prev) => (
    //        //(upExt(1).memAddr === prev.memAddr)
    //        //(upExtRealMemAddr === prev.memAddr)
    //        //&& prev.hazardId.msb
    //        //&& 
    //        //(prev.hazardId === 0)
    //        //&& upExt(1).hazardId.msb
    //        && 
    //        myHazardCmpFunc(upExt(1), prev)
    //      )
    //      //&& myHazardCmpFunc(upExt(0), prev)
    //    )
    //    .setName("cFrontArea_tempMyUpExtDelFindFirst2")
    //  )
    //)
    //val tempMyUpExtDelDoCancelFrontFindFirst = (
    //  (optEnableModDuplicate) generate (
    //    myUpExtDel.sFindFirst(
    //      //(
    //      //  prev: PipeMemRmwPayloadExt[
    //      //    WordT,
    //      //    HazardCmpT,
    //      //  ]
    //      //) => (
    //        _.doCancelFront
    //      //)
    //    )
    //  )
    //)
    //val myStopIt = Bool()
    //myStopIt := False
    //val myRdMemWord = mod.front.myRdMemWord
    val myNonFwdRdMemWord = mod.front.myNonFwdRdMemWord
    val myFwdRdMemWord = mod.front.myFwdRdMemWord
    //val tempFwdWhen = KeepAttribute(
    //  //RegNext(RegNext(mod.back.cBack.up.isFiring)) init(False)
    //  //mod.back.cBack.up.isFiring
    //  mod.back.cBack.down.isFiring
    //  //mod.back.cLastBack.up.isFiring
    //  //mod.back.cLastBack.up.isValid
    //  //RegNext(io.back.isFiring) init(False)
    //  //mod.back.cLastBack.down.isFiring,
    //)
    //  .setName("tempFwdWhen")
        //val tempFwdPrevCond = KeepAttribute(
        //  mod.front.myUpExtDel2.last.valid
        //  //&& mod.front.myUpExtDel2(idx).ready
        //  && mod.front.myUpExtDel2.last.ready
        //)

        //--------
        // BEGIN: old forwarding code; update later
        //val tempFwdPrev = KeepAttribute(
        //  RegNextWhen(
        //    //mod.front.myUpExtDel2.last
        //    mod.front.myUpExtDel2(mod.front.myUpExtDel2.size - 2),
        //    //mod.front.myUpExtDel2(mod.front.myUpExtDel2.size - 2).valid
        //    //&& mod.front.myUpExtDel2(mod.front.myUpExtDel2.size - 2).ready
        //    mod.front.myUpExtDel2(mod.front.myUpExtDel2.size - 2).fire
        //    //mod.front.myUpExtDel2.last
        //  ) init(
        //    mod.front.myUpExtDel2(mod.front.myUpExtDel2.size - 2).getZero
        //  )
        //)
        //tempFwdPrev.valid.allowOverride
        //tempFwdPrev.ready.allowOverride
        //tempFwdPrev.fire.allowOverride
        //tempFwdPrev.valid := (
        //  mod.front.myUpExtDel2(mod.front.myUpExtDel2.size - 2).valid
        //)
        //tempFwdPrev.ready := (
        //  mod.front.myUpExtDel2(mod.front.myUpExtDel2.size - 2).ready
        //)
        //tempFwdPrev.fire := (
        //  mod.front.myUpExtDel2(mod.front.myUpExtDel2.size - 2).fire
        //)
        // END: old forwarding code; update later
        //--------
      //  val tempFwdPrevMemAddr = (
      //    (optModHazardKind == PipeMemRmw.modHazardKindFwd) generate (
      //      KeepAttribute(
      //        //myUpExtDel.last.memAddr
      //        //mod.back.myWriteAddr
      //        (
      //          //RegNextWhen(
      //            //mod.back.myWriteAddr,
      //            //myUpExtDel.last.memAddr(PipeMemRmw.modWrIdx),
      //            //mod.front.myUpExtDelFwd.memAddr(PipeMemRmw.modWrIdx)
      //            //myUpExtDel(myUpExtDel.size - 2).memAddr(
      //            //  PipeMemRmw.modWrIdx
      //            //),
      //            myUpExtDel.last.memAddr(
      //              PipeMemRmw.modWrIdx
      //            ),
      //            //myUpExtDel.last.memAddr(
      //            //  PipeMemRmw.modWrIdx
      //            //),
      //            //tempFwdWhen,
      //            //mod.back.cBack.up.isFiring,
      //            //tempFwdWhen
      //          //)
      //          //init(
      //          //  //mod.back.myWriteAddr.getZero
      //          //  //myUpExtDel.last.memAddr.getZero
      //          //  0x0
      //          //)
      //        )
      //        .setName("tempFwdPrevMemAddr")
      //    )
      //  )
      //)
      //--------
      // BEGIN: old `tempFwdPrev`
      //val tempFwdPrev = (
      //  (optModHazardKind == PipeMemRmw.modHazardKindFwd) generate (
      //    KeepAttribute(
      //      //myUpExtDel.last
      //      (
      //        //RegNextWhen(
      //          //myUpExtDel.last,
      //          Mux(
      //            tempFwdPrevCond,
      //            mod.front.myUpExtDel2.last,
      //            tempFwdPrevWhenFalse,
      //          )
      //          //myUpExtDel(myUpExtDel.size - 2),
      //          //mod.front.myUpExtDelFwd
      //          ////myUpExtDel(myUpExtDel.size - 2)
      //          //myUpExtDel.last,
      //          ////mod.back.cBack.down.isFiring,
      //          ////io.back.isFiring,
      //          //tempFwdWhen,
      //        //)
      //        //init(
      //        //  myUpExtDel.last.getZero
      //        //)
      //        ////mod.front.rMyUpExtDelFinal
      //      )
      //      .setName("tempFwdPrev")
      //    )
      //  )
      //)
      // END: old `tempFwdPrev`
      //--------
    if (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.modHazardKindDupl
    ) {
      // BEGIN: stalling-based code; fix later
      ////when (myStopIt) {
      ////  haltIt()
      ////}
      ////down.cancel := False
      ////upExt(1).doCancelFront := False
      //nextState := rState
      //upExt(1).duplState := nextState.asBits(0)
      //if (vivadoDebug) {
      //  //nextHazardId.addAttribute("MARK_DEBUG", "TRUE")
      //  //nextState.addAttribute("MARK_DEBUG", "TRUE")
      //  //rState.addAttribute("MARK_DEBUG", "TRUE")
      //  //rPrevStateWhen.addAttribute("MARK_DEBUG", "TRUE")
      //  //upExt.addAttribute("MARK_DEBUG", "TRUE")
      //  //upExtRealMemAddr.addAttribute("MARK_DEBUG", "TRUE")
      //  ////for (idx <- 0 until tempMyUpExtDelFindFirstNotPostDelay.size) {
      //  ////}
      //  //tempMyUpExtDelFindFirstNotPostDelay.addAttribute(
      //  //  "MARK_DEBUG", "TRUE"
      //  //)
      //  ////tempMyUpExtDelFindFirstIsPostDelay.addAttribute(
      //  ////  "MARK_DEBUG", "TRUE"
      //  ////)
      //}
      ////upExt(1).valid := up.isValid
      ////upExt(1).ready := up.isReady
      //switch (rState) {
      //  is (State.IDLE) {
      //    //when (down.isFiring) {
      //    //  upExtRealMemAddr := upExt(0).memAddr
      //    //}
      //    when (
      //      up.isValid
      //      && down.isReady
      //      //&& down.isFiring
      //      //&& down.isFiring
      //    ) {
      //      upExt(1) := upExt(0)
      //      upExt(1).valid := up.isValid
      //      upExt(1).ready := up.isReady
      //      upExt(1).fire := up.isFiring
      //      upExt(1).hazardId := nextHazardId
      //      upExtRealMemAddr := upExt(0).memAddr
      //      for (zdx <- 0 until modRdPortCnt) {
      //        when (
      //          //Mux[Bool](
      //          //  rPrevStateWhen === State.IDLE,
      //            tempMyUpExtDelFindFirstNotPostDelay(zdx)._1
      //            && (
      //              if (!doPrevHazardCmpFunc) (
      //                mod.front.myHazardCmpFunc(
      //                  curr=upExt(1),
      //                  prev=mod.front.myUpExtDel(0),
      //                  zdx=zdx,
      //                  isPostDelay=false,
      //                )
      //              ) else ( // if (doPrevHazardCmpFunc)
      //                True
      //              )
      //            )
      //        ) {
      //          rDidDelayItIdle := False
      //          duplicateIt()
      //          //nextDidChangeState := False
      //          nextState := State.DELAY
      //          nextHazardId := (
      //            (
      //              //--------
      //              //// this should be `myUpExtDel.size` if
      //              //// `myUpExtDel.size` is the number of stages strictly
      //              //// after `cFront` and strictly before `cBack`
      //              //// if including `cBack` in `myUpExtDel`, then it should
      //              //// be `myUpExtDel.size - 1`
      //              //--------
      //              //--------
      //              //--------
      //              (
      //                S(
      //                  s"${nextHazardId.getWidth}"
      //                  + s"'d${myUpExtDel.size}"
      //                )
      //                - Cat(
      //                  U"3'd0", 
      //                  //Mux[UInt](
      //                  //  rPrevStateWhen === State.IDLE,
      //                    (
      //                      tempMyUpExtDelFindFirstNotPostDelay(zdx)._2
      //                      //+ (
      //                      //  doModInModFrontFunc match {
      //                      //    case Some(myDoModSingleStageFunc) => -1
      //                      //    case None => 0
      //                      //  }
      //                      //)
      //                    ),
      //                  //  tempMyUpExtDelFindFirstIsPostDelay._2,
      //                  //)
      //                ).asSInt
      //              )
      //            )
      //          )
      //          //nextHaltItIdleCnt := (
      //          //  //1
      //          //  0
      //          //)
      //        }
      //      }
      //    }
      //  }
      //  is (State.DELAY) {
      //    when (down.isFiring) {
      //      nextHazardId := hazardIdMinusOne
      //    }
      //    //myStopIt := True
      //    //duplicateIt()
      //    when (nextHazardId.msb) {
      //      nextState := State.IDLE
      //    } otherwise {
      //      duplicateIt()
      //    }
      //  }
      //}
      // END: stalling-based code; fix later
    } else if (
      //!optEnableModDuplicate
      //optModHazardKind != PipeMemRmw.modHazardKindDupl
      optModHazardKind == PipeMemRmw.modHazardKindFwd
    ) {
      //upExt(1) := upExt(0)
      when (
        //up.isValid
        up.isFiring
      ) {
        upExt(1)(extIdxUp) := upExt(0)(extIdxSingle)
      }
      when (
        down.isFiring
      ) {
        upExt(1)(extIdxDown) := RegNext(upExt(1)(extIdxUp))
      }
      upExtRealMemAddr/*(extIdxUp)*/ := upExt(1)(extIdxUp).memAddr
      //upExtRealMemAddr(extIdxDown) := upExt(1)(extIdxDown).memAddr
      upExt(1)(extIdxUp).valid := up.isValid
      upExt(1)(extIdxUp).ready := up.isReady
      upExt(1)(extIdxUp).fire := up.isFiring
      //upExt(1)(extIdxDown).valid := RegNext(down.isValid)
      //upExt(1)(extIdxDown).ready := RegNext(down.isReady)
      //upExt(1)(extIdxDown).fire := RegNext(down.isFiring)
      //up(mod.front.didFwd(0)) := False
      //if (
      //  //optModFwdToFront
      //  optModHazardKind == PipeMemRmw.modHazardKindFwd
      //) {
      //  for (zdx <- 0 until modRdPortCnt) {
      //    mod.front.nextDidFwd(zdx)(0) := False
      //  }

      //  for (zdx <- 0 until modRdPortCnt) {
      //    when (
      //      //if (myUpExtDel.size > 2) (
      //        mod.front.findFirstFunc(
      //          currMemAddr=upExtRealMemAddr(zdx),
      //          prevMemAddr=(
      //            //tempFwdPrevMemAddr
      //            tempFwdPrev.memAddr(PipeMemRmw.modWrIdx),
      //          ),
      //          curr=upExt(1),
      //          prev=(
      //            tempFwdPrev
      //          ),
      //          zdx=zdx,
      //          isPostDelay=false,
      //          //doValidCheck=false,
      //        ) && (
      //          //RegNextWhen(True, mod.back.cBack.up.isValid) init(False)
      //          //mod.back.cBack.up.isValid
      //          //mod.back.cBack.up.isFiring
      //          //mod.back.cLastBack.up.isValid
      //          True
      //          //mod.back.myWriteEnable
      //        ) && (
      //          //RegNextWhen(
      //          //  mod.back.myWriteEnable,
      //          //  //mod.back.cBack.up.isFiring,
      //          //  tempFwdWhen
      //          //) init(False)
      //          True
      //          //up.isFiring
      //        )
      //      //) else (
      //      //  False
      //      //)
      //    ) {
      //      //println(myUpExtDel.size)
      //      //up(mod.front.didFwd(0)) := True
      //      //myRdMemWord := myUpExtDel.last.modMemWord
      //      mod.front.nextDidFwd(zdx)(0) := True
      //    } 
      //    //otherwise {
      //    //}
      //  }
      //}

      ////upExt(1).hazardId := nextHazardId
      //upExtRealMemAddr := upExt(0).memAddr
      //upExt(1) := upExt(0)
      //when (down.isFiring) 
      //when (
      //  //up.isValid //&& down.isReady
      //) {
      //  upExtRealMemAddr := upExt(1).memAddr
      //}
      //upExtRealMemAddr := upExt(1).memAddr
    } else {
      //--------
      // BEGIN: no hazard resolution code; fix later 
      ////upExt(1) := upExt(0)
      //when (
      //  //up.isValid
      //  up.isFiring
      //) {
      //  upExt(1) := upExt(0)
      //}
      //upExt(1).valid := up.isValid
      //upExt(1).ready := up.isReady
      //upExt(1).fire := up.isFiring
      ////upExt(1).hazardId := nextHazardId
      //upExtRealMemAddr := upExt(1).memAddr
      // END: no hazard resolution code; fix later 
      //--------
    }
    //setDoDuplicateIt(false)

    //--------
    //upExt(1).rdMemWord := (
    //  RegNext(upExt(1).rdMemWord) init(upExt(1).rdMemWord.getZero)
    //)
    //--------
    //--------
    //upExt(1).rdMemWord := (
    //  RegNext(upExt(1).rdMemWord) init(upExt(1).rdMemWord.getZero)
    //)
    //when (up.isValid) {
    //  upExt(1).rdMemWord := modMem.readSync(
    //    address=upExt(1).memAddr
    //  )
    //}
    //val cLastFront = mod.front.cLastFront
    //if (vivadoDebug) {
    //  myRdMemWord.addAttribute("MARK_DEBUG", "TRUE")
    //}
    //val rRdMemWord1 = mod.front.rRdMemWord1
    val dbgRdMemWord = (debug) generate (
      mod.front.dbgRdMemWord
    )
    //myRdMemWord := RegNext(myRdMemWord) init(myRdMemWord.getZero)
    //val myDownExt = mkExt().setName("cFrontArea_myDownExt")
    //myRdMemWord := (
    //  RegNext(myRdMemWord) init(myRdMemWord.getZero)
    //)

    val rSetRdId = Reg(Bool()) init(False)
    //val rRdId = Reg(cloneOf(upExt(1).rdId)) init(0x0)
    //val rPrevRdId = RegNext(upExt(1).rdId) init(0x0)
    //upExt(1).rdId := rPrevRdId
    //upExt(1).rdId := rRdId
    //--------
    tempCond := (
      (
        if (
          //optEnableModDuplicate
          optModHazardKind == PipeMemRmw.modHazardKindDupl
        ) (
          nextHazardId.msb
        ) else (
          True
        )
      )
      ////&& up.isValid
      ////////&& down.isReady
      ////&& down.isFiring
      ////&& (
      ////  rState === State.IDLE
      ////)
      && up.isValid
      ////&& down.isFiring
      && down.isReady
    )

    if (
      //optEnableModDuplicate
      //optModHazardKind == PipeMemRmw.modHazardKindDupl
      optModHazardKind != PipeMemRmw.modHazardKindDont
    ) {
      if (optModHazardKind == PipeMemRmw.modHazardKindDupl) {
        // BEGIN: previous `duplicateIt` code; fix later
        //myNonFwdRdMemWord(PipeMemRmw.modWrIdx) := (
        //  modMem(PipeMemRmw.modWrIdx).readSync(
        //    //address=RegNextWhen(
        //    //  upExt(1).memAddr, up.isFiring
        //    //) init(0x0)
        //    //address=RegNextWhen(
        //    //  upExt(1).memAddr, down.isFiring
        //    //) init(0x0)
        //    address=(
        //      //upExt(1).memAddr
        //      upExtRealMemAddr(PipeMemRmw.modWrIdx)
        //    ),
        //    //address=myDownExt.memAddr,
        //    enable=(
        //      //up.isFiring
        //      //&&
        //      tempCond
        //      //up.isFiring && tempCond
        //      //down.isFiring && tempCond
        //      //up.isFiring && tempCond
        //      //down.isFiring && tempCond
        //    ),
        //  )
        //)
        // END: previous `duplicateIt` code; fix later
      } else { // if (optModHazardKind == PipeMemRmw.modHazardKindFwd)
        val tempSharedEnable = KeepAttribute(
          //up.isValid
          //&& 
          down.isFiring
          //down.isReady
          //mod.front.cMid0Front.up.isReady
        )
          .setName("tempSharedEnable")
        for (zdx <- 0 until modRdPortCnt) {
          //myRdMemWord(zdx) := (
          //  RegNext(myRdMemWord(zdx))
          //  init(myRdMemWord(zdx).getZero)
          //)
          //myRdMemWord(zdx).allowOverride
          myNonFwdRdMemWord(zdx) := modMem(zdx).readSync(
            address=(
              upExtRealMemAddr(zdx)
            ),
            enable=(
              //tempCond
              //!mod.front.nextDidFwd(zdx)(0)
              //&& 
              tempSharedEnable
              //down.isReady
            ),
          )
          myFwdRdMemWord(zdx) := (
            RegNext(myFwdRdMemWord(zdx))
            init(myFwdRdMemWord(zdx).getZero)
          )
          when (
            tempSharedEnable
            //down.isFiring
            //up.isValid 
            //&& down.isFiring
            //down.isReady
            //True
            &&
            //(
            //  RegNext(
                mod.back.myWriteEnable
            //  )
            //  init(False)
            //)
          ) {
            myFwdRdMemWord(zdx) := (
            //myUpExtDel.last.modMemWord
            ////RegNext(
              /*RegNextWhen*/(
                //mod.front.myUpExtDelFwd.modMemWord
                //RegNext(mod.front.myUpExtDelFwd.modMemWord)
                //init(mod.front.myUpExtDelFwd.modMemWord.getZero)
                //mod.back.myWriteData
                (
                  //RegNext(
                  //  RegNextWhen(
                  //    (
                  //      //RegNext(mod.back.myWriteData)
                  //      //init(mod.back.myWriteData.getZero)
                  //      mod.back.myWriteData
                  //    ),
                  //    //down.isFiring
                  //    //&& 
                  //    mod.back.myWriteEnable
                  //  )
                  //)
                  //RegNextWhen(
                    //RegNext(
                    //  mod.back.myWriteData
                    //),
                    //RegNext(tempFwdPrev.modMemWord)
                    //RegNext(
                    //  Mux(
                    //    tempFwdPrev.valid,
                        //RegNext(tempFwdPrev.modMemWord)
                        //init(tempFwdPrev.modMemWord.getZero)
                        RegNext(
                          //RegNextWhen(
                            //tempFwdPrev.modMemWord,
                          //  mod.back.myWriteEnable
                          //)
                          //init(tempFwdPrev.modMemWord.getZero)
                          mod.back.myWriteData
                        )
                        init(
                          //tempFwdPrev.modMemWord.getZero
                          mod.back.myWriteData.getZero
                        )
                    //    RegNext(tempFwdPrev.modMemWord),
                    //  )
                    //)
                  //  mod.front.cMid0Front.down.isFiring,
                  //)
                )
                //RegNext(mod.front.myUpExtDelFwd.modMemWord)
                //init(mod.front.myUpExtDelFwd.modMemWord.getZero),
              //  io.back.isFiring,
              )
            )
          }
          //myFwdRdMemWord(zdx) := (
          //  RegNext(myFwdRdMemWord(zdx))
          //  init(myFwdRdMemWord(zdx).getZero)
          //)
          //when (
          //  //mod.front.nextDidFwd(zdx)(0)
          //  //&& 
          //  //up.isValid
          //  //&& 
          //  tempSharedEnable
          //  //&& mod.back.cBack.up.isValid
          //  //&& mod.back.cLastBack.up.isValid
          //  //&& (
          //  //  RegNext(mod.back.myWriteEnable)
          //  //  init(False)
          //  //)
          //  //&& up.isValid
          //  //&& down.isReady
          //  //&& mod.back.myWriteEnable
          //) {
          //  //myRdMemWord(zdx) := myUpExtDel.last.modMemWord
          //  myFwdRdMemWord(zdx) := (
          //    //myUpExtDel.last.modMemWord
          //    ////RegNext(
          //      /*RegNextWhen*/(
          //        //mod.front.myUpExtDelFwd.modMemWord
          //        //RegNext(mod.front.myUpExtDelFwd.modMemWord)
          //        //init(mod.front.myUpExtDelFwd.modMemWord.getZero)
          //        //mod.back.myWriteData
          //        RegNext(mod.back.myWriteData)
          //        init(mod.back.myWriteData.getZero)
          //        //RegNext(mod.front.myUpExtDelFwd.modMemWord)
          //        //init(mod.front.myUpExtDelFwd.modMemWord.getZero),
          //      //  io.back.isFiring,
          //      )
          //    ////)
          //    //RegNextWhen(
          //    //  /*RegNext*/(mod.back.myWriteData),
          //    //  //RegNext(myUpExtDel.last.modMemWord)
          //    //  //init(myUpExtDel.last.modMemWord.getZero)
          //    //  //io.back.isFiring,
          //    //  //io.modBack.isFiring,
          //    //  //mod.back.cBack.up.isValid
          //    //  //mod.back.cBack.down.isFiring
          //    //  tempFwdWhen
          //    //  //tempFwdWhen,
          //    //) init(mod.back.myWriteData.getZero)
          //  )
          //}
        }
      }
      //when (
      //  if (!optReorder) (
      //    True
      //  ) else (
      //    myInpUpExt.memAddrReorderValid
      //  )
      //) {
      //  //myRdMemWord := dualRdMem.readSync(
      //  //  address=myInpUpExt.memAddr,
      //  //  enable=up.isFiring,
      //  //)
      //  myRdMemWord := modMem.readSync(
      //    address=(
      //      //upExt(1).memAddr
      //      upExtRealMemAddr
      //    ),
      //    //address=myDownExt.memAddr,
      //    enable=(
      //      tempCond
      //    ),
      //  )
      //} otherwise {
      //  myRdMemWord := dualRdMem.readSync(
      //    address=rReorderCommitHead,
      //    enable=up.isFiring,
      //  )
      //  val reorderMemAddrPlus1 = (
      //    Cat(B"1'b0", rReorderCommitHead).asUInt + 1
      //  )
      //  when (reorderMemAddrPlus1 < wordCount) {
      //    rReorderCommitHead := 0x0
      //  } otherwise {
      //    rReorderCommitHead := (
      //      reorderMemAddrPlus1(rReorderCommitHead.bitsRange)
      //    )
      //  }
      //}
    }
    val tempCondDown = (
      down.isFiring
    )
    //--------
    //--------
    //--------
    for (extIdx <- 0 until extIdxLim) {
      tempUpMod(1) := (
        RegNext(tempUpMod(1)) init(tempUpMod(1).getZero)
      )
    }
    //when (up.isFiring) {
      tempUpMod(1) := tempUpMod(0)
      tempUpMod(1).allowOverride
      tempUpMod(1).setPipeMemRmwExt(
        inpExt=upExt(1)(extIdxUp),
        memArrIdx=memArrIdx,
      )
    //}
    up(mod.front.midPipePayload) := tempUpMod(1)
    //--------
    //bypass(mod.front.inpPipePayload).allowOverride
    //bypass(mod.front.inpPipePayload) := tempUpMod(1)
    //--------
    val rIsFiringCnt = (debug) generate (
      Reg(UInt(8 bits)) init(0x0)
    )
    //val rMyUpRdValidDelVec = (debug) generate Vec.fill(8)(
    //  Reg(Bool()) init(False)
    //)
    //--------
    //--------
  }
  val cMid0Front = mod.front.cMid0Front
  val cMid0FrontArea = new cMid0Front.Area {
    //--------
    val upExt = Vec.fill(3)(
      mkExt()
    ).setName("cMid0FrontArea_upExt")
    //mod.front.myUpExtDel(0) := upExt(1)
    //doModInModFrontFunc match {
    //  case Some(myDoModSingleStageFunc) => {
    //    myUpExtDel(0) := (
    //      RegNext(myUpExtDel(0)) init(myUpExtDel(0).getZero)
    //    )
    //  }
    //  case None => {
    //  }
    //}
    //myUpExtDel(0) := (
    //  RegNext(myUpExtDel(0)) init(myUpExtDel(0).getZero)
    //)
    for (extIdx <- 0 until extIdxLim) {
      upExt(1)(extIdx) := (
        RegNext(upExt(1)(extIdx)) init(upExt(1)(extIdx).getZero)
      )
    }
    //upExt(1) := upExt(0)
    upExt(1).allowOverride
    //when (
    //  up.isValid
    //  //up.isFiring
    //  //&& down.isReady
    //  //&& upExt(0).hazardId.msb
    //) {
    //  //doModInModFrontFunc match {
    //  //  case Some(myDoModSingleStageFunc) => {
    //  //    myUpExtDel(0) := upExt(2)
    //  //  }
    //  //  case None => {
    //  //  }
    //  //}
    //  upExt(1) := upExt(0)
    //}
    //myUpExtDel(0) := (
    //  upExt(2)
    //)
    for (extIdx <- 0 until extIdxLim) {
      myUpExtDel(0)(extIdx) := (
        RegNext(myUpExtDel(0)(extIdx)) init(myUpExtDel(0)(extIdx).getZero)
      )
      myUpExtDel(0)(extIdx).valid.allowOverride
      myUpExtDel(0)(extIdx).ready.allowOverride
      myUpExtDel(0)(extIdx).fire.allowOverride
    }
    when (
      //up.isValid
      up.isFiring
    ) {
      myUpExtDel(0)(extIdxUp) := (
        upExt(2)(extIdxUp)
      )
      upExt(1)(extIdxUp) := upExt(0)(extIdxUp)
    }
    when (
      down.isFiring
    ) {
      myUpExtDel(0)(extIdxDown) := RegNext(myUpExtDel(0)(extIdxUp))
      upExt(1)(extIdxDown) := RegNext(upExt(1)(extIdxUp))
    }
    upExt(1)(extIdxUp).valid := up.isValid
    upExt(1)(extIdxUp).ready := up.isReady
    upExt(1)(extIdxUp).fire := up.isFiring
    //upExt(1)(extIdxDown).valid := RegNext(down.isValid)
    //upExt(1)(extIdxDown).ready := RegNext(down.isReady)
    //upExt(1)(extIdxDown).fire := RegNext(down.isFiring)
    myUpExtDel(0)(extIdxUp).valid := upExt(1)(extIdxUp).valid
    myUpExtDel(0)(extIdxUp).ready := upExt(1)(extIdxUp).ready
    myUpExtDel(0)(extIdxUp).fire := upExt(1)(extIdxUp).fire
    myUpExtDel(0)(extIdxDown).valid := upExt(1)(extIdxDown).valid
    myUpExtDel(0)(extIdxDown).ready := upExt(1)(extIdxDown).ready
    myUpExtDel(0)(extIdxDown).fire := upExt(1)(extIdxDown).fire
    //upExt(1) := upExt(0)

    val tempUpMod = (
      Vec.fill(3)(
        //Vec.fill(extIdxLim)(
          modType()
        //)
      )
      .setName("cMid0FrontArea_tempUpMod")
    )
    tempUpMod(0) := up(mod.front.midPipePayload)
    tempUpMod(0).getPipeMemRmwExt(
      outpExt=upExt(0)(extIdxSingle),
      memArrIdx=memArrIdx,
    )
    //def doModInModFront
    ////[
    ////  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
    ////]
    //(
    //  outpMod: ModT,
    //  inpMod: ModT,
    //): Unit = {
    //  doModInModFrontFunc match {
    //    case Some(myDoModSingleStageFunc) => {
    //      //assert(modStageCnt == 0)
    //      myDoModSingleStageFunc(
    //        tempUpMod(2),
    //        tempUpMod(1),
    //      )
    //    }
    //    case None => {
    //      assert(modStageCnt > 0)
    //      tempUpMod(2) := tempUpMod(1)
    //    }
    //  }
    //}
    //val cMid0Front = mod.front.cMid0Front
    val myRdMemWord = mod.front.myRdMemWord
    //when (up.isValid) {
    //}
    //upExt(1).rdMemWord := (
    //  RegNext(upExt(1).rdMemWord) init(upExt(1).rdMemWord.getZero)
    //)

    //upExt(1).rdId.allowOverride
    //val rPrevRdId = RegNext(upExt(1).rdId) init(0x0)
    //upExt(1).rdId := rPrevRdId
    //val rRdId = Reg(cloneOf(upExt(0).rdId)) init(upExt(0).rdId.getZero)
    val rSetRdId = Reg(Bool()) init(False)
    //upExt(1).rdMemWord := myRdMemWord
    //val rSavedRdMemWord = (
    //  Reg(cloneOf(myRdMemWord)) init(myRdMemWord.getZero)
    //)

    //val tempMyUpExtDelFindFirstV2d = KeepAttribute(
    //  Vec.fill(modRdPortCnt)(
    //    Vec.fill(mod.front.myUpExtDel.size)(
    //      Vec.fill(extIdxLim)(
    //        mkExt()
    //      )
    //    )
    //  )
    //)
    //val tempMyUpExtDel2FindFirstV2d = KeepAttribute(
    //  Vec.fill(modRdPortCnt)(
    //    Vec.fill(mod.front.myUpExtDel2.size)(
    //      mkExt()
    //    )
    //  )
    //)
    //  .setName("tempMyUpExtDel2FindFirstV2d")
    val tempMyUpExtDelFrontFindFirstV2d = KeepAttribute(
      Vec.fill(modRdPortCnt)(
        Vec.fill(1)(
          //Vec.fill(extIdxLim)(
            mkExt()
          //)
        )
      )
    )
    for (zdx <- 0 until modRdPortCnt) {
      for (idx <- 0 until tempMyUpExtDelFrontFindFirstV2d(zdx).size) {
        tempMyUpExtDelFrontFindFirstV2d(zdx)(idx)(extIdxDown) := (
          mod.front.myUpExtDel(0)(extIdxDown)
        )
      }
      //for (idx <- 0 until mod.front.myUpExtDel.size) {
      //  val tempMyUpExtDel = tempMyUpExtDelFindFirstV2d(zdx)(idx)
      //    .setName(s"tempMyUpExtDel_${zdx}_${idx}")
      //  if (idx == 0) {
      //    tempMyUpExtDelFrontFindFirstV2d(zdx)(idx) := tempMyUpExtDel
      //  } else {
      //    tempMyUpExtDel2FindFirstV2d(zdx)(idx - 1) := tempMyUpExtDel
      //  }
      //  //tempMyUpExtDel2 := (
      //  //  RegNextWhen(
      //  //    mod.front.myUpExtDel(idx),
      //  //    //mod.front.myUpExtDel(idx).valid
      //  //    //&& mod.front.myUpExtDel(idx).ready
      //  //    if (idx + 1 == mod.front.myUpExtDel.size) (
      //  //      io.back.isFiring
      //  //    ) else (
      //  //      mod.front.myUpExtDel(idx).fire
      //  //    )
      //  //  ) init(mod.front.myUpExtDel(idx).getZero)
      //  //)
      //  //tempMyUpExtDel2.valid.allowOverride
      //  //tempMyUpExtDel2.ready.allowOverride
      //  //tempMyUpExtDel2.fire.allowOverride
      //  //tempMyUpExtDel2.valid := mod.front.myUpExtDel(idx).valid
      //  //tempMyUpExtDel2.ready := mod.front.myUpExtDel(idx).ready
      //  //tempMyUpExtDel2.fire := mod.front.myUpExtDel(idx).fire
      //  val tempValid = Bool()
      //  val tempReady = Bool()
      //  val tempFire = Bool()

      //  //tempValid := RegNext(tempValid) init(False)
      //  //tempReady := RegNext(tempReady) init(False)
      //  //tempFire := RegNext(tempFire) init(False)
      //  tempValid := False
      //  tempReady := False
      //  tempFire := False
      //  tempValid.allowOverride
      //  tempReady.allowOverride
      //  tempFire.allowOverride

      //  //if (idx + 1 == mod.front.myUpExtDel2.size) {
      //  //  when (mod.front.myUpExtDel2.last.fire) {
      //  //    tempValid := RegNext(io.back.isValid)
      //  //    tempReady := RegNext(io.back.isReady)
      //  //    tempFire := RegNext(io.back.isFiring)
      //  //  }
      //  //} else {
      //  //  tempValid := mod.front.myUpExtDel2(idx + 1).valid
      //  //  tempReady := mod.front.myUpExtDel2(idx + 1).ready
      //  //  tempFire := mod.front.myUpExtDel2(idx + 1).fire
      //  //}
      //  //tempValid := mod.front.myUpExtDel2(idx).valid
      //  //tempReady := mod.front.myUpExtDel2(idx).ready
      //  //tempFire := mod.front.myUpExtDel2(idx).fire
      //  //--------
      //  tempMyUpExtDel := (
      //    RegNextWhen(
      //      mod.front.myUpExtDel(idx),
      //      mod.front.myUpExtDel(idx).fire,
      //      //tempFire
      //      //if (idx + 1 == mod.front.myUpExtDel.size) (
      //      //  io.back.isFiring
      //      //) else (
      //      //  mod.front.myUpExtDel2(idx).fire
      //      //)
      //    )
      //    init(tempMyUpExtDel.getZero)
      //  )
      //  //tempMyUpExtDel2.valid.allowOverride
      //  //tempMyUpExtDel2.ready.allowOverride
      //  //tempMyUpExtDel2.fire.allowOverride
      //  //tempMyUpExtDel2.valid := RegNext(tempValid)
      //  //tempMyUpExtDel2.ready := RegNext(tempReady)
      //  //tempMyUpExtDel2.fire := RegNext(tempFire)

      //  //tempMyUpExtDel2 := mod.front.myUpExtDel(idx)

      //  //val tempMyUpExtDel2Cond = KeepAttribute(
      //  //  mod.front.myUpExtDel2(idx).valid
      //  //  && mod.front.myUpExtDel2(idx).ready
      //  //)
      //  //  .setName(s"tempMyUpExtDel2Cond_${zdx}_${idx}")
      //  //val tempMyUpExtDelWhenFalse = KeepAttribute(
      //  //  RegNextWhen(
      //  //    mod.front.myUpExtDel(idx),
      //  //    mod.front.myUpExtDel(idx).valid
      //  //    && mod.front.myUpExtDel(idx).ready
      //  //  ) init(mod.front.myUpExtDel(idx).getZero)
      //  //)
      //  //  .setName(s"tempMyUpExtDelWhenFalse_${zdx}_${idx}")
      //  //tempMyUpExtDel2 := Mux(
      //  //  ////RegNext(mod.front.myUpExtDel(idx).ready),
      //  //  ////mod.front.myUpExtDel2(idx).ready,
      //  //  (
      //  //    tempMyUpExtDel2Cond
      //  //    // TODO: replace this with `fire`, as we need to include
      //  //    // `isCancel` properly for CPU pipelines, for example
      //  //  ),
      //  //  mod.front.myUpExtDel2(idx),
      //  //  //////RegNext(mod.front.myUpExtDel(idx + 1))
      //  //  tempMyUpExtDelWhenFalse,
      //  //)
      //  //  .setName(s"tempMyUpExtDel2_${zdx}_${idx}")
      //}
      for (idx <- 0 until 1) {
        val tempMyUpExtDelFront = (
          tempMyUpExtDelFrontFindFirstV2d(zdx)(idx)(extIdxDown)
          .setName(s"tempMyUpExtDelFront_${zdx}_${idx}")
        )
        mod.front.tempMyUpExtDelFrontFindFirstVec(zdx)(idx) := (
          mod.front.findFirstFunc(
            currMemAddr=upExt(1)(extIdxSingle).memAddr(zdx),
            prevMemAddr=(
              tempMyUpExtDelFront.memAddr(
                PipeMemRmw.modWrIdx
              )
            ),
            curr=(
              upExt(1)(extIdxSingle)
            ),
            prev=(
              tempMyUpExtDelFront
            ),
            zdx=zdx,
            isPostDelay=false,
          )
        )
      }
      //for (idx <- 0 until mod.front.myUpExtDel2.size) {
      //  //val tempMyUpExtDel2 = tempMyUpExtDel2FindFirstV2d(zdx)(idx)
      //  //  .setName(s"tempMyUpExtDel2_${zdx}_${idx}")
      //  //mod.front.tempMyUpExtDel2FindFirstVec(zdx)(idx)(0) := (
      //  //  mod.front.findFirstFunc(
      //  //    currMemAddr=upExt(1).memAddr(zdx),
      //  //    prevMemAddr=(
      //  //      tempMyUpExtDel2.memAddr(
      //  //        PipeMemRmw.modWrIdx
      //  //      )
      //  //    ),
      //  //    curr=(
      //  //      upExt(1)
      //  //    ),
      //  //    prev=(
      //  //      tempMyUpExtDel2
      //  //    ),
      //  //    zdx=zdx,
      //  //    isPostDelay=false,
      //  //    //doValidCheck=false,
      //  //  )
      //  //  ////(
      //  //  ////  if (zdx == PipeMemRmw.modWrIdx) (
      //  //  ////    True
      //  //  ////  ) else (
      //  //  ////    False
      //  //  ////  )
      //  //  ////) && 
      //  //  //(
      //  //  //  mod.front.findFirstFunc2(
      //  //  //    currMemAddr=upExt(1).memAddr(zdx),
      //  //  //    prevMemAddr=(
      //  //  //      //mod.front.myUpExtDel2(idx).memAddr(
      //  //  //      //  PipeMemRmw.modWrIdx
      //  //  //      //)
      //  //  //      tempMyUpExtDel2.memAddr(
      //  //  //        PipeMemRmw.modWrIdx
      //  //  //      )
      //  //  //    ),
      //  //  //    curr=(
      //  //  //      upExt(1)
      //  //  //    ),
      //  //  //    prev=(
      //  //  //      //mod.front.myUpExtDel2(idx)
      //  //  //      tempMyUpExtDel2
      //  //  //      //mod.front.myUpExtDel(idx + 1)
      //  //  //    ),
      //  //  //    zdx=zdx,
      //  //  //    isPostDelay=false,
      //  //  //    doValidCheck=true,
      //  //  //  )
      //  //  //  //RegNextWhen(
      //  //  //    //mod.front.myUpExtDelFullFindFirstVecNotPostDelay(
      //  //  //    //  idx
      //  //  //    //  //+ 1
      //  //  //    //  //+ (
      //  //  //    //  //  doModInModFrontFunc match {
      //  //  //    //  //    case Some(myDoModSingleStageFunc) => 0
      //  //  //    //  //    case None => 1
      //  //  //    //  //  }
      //  //  //    //  //)
      //  //  //    //),
      //  //  //  //  cMid0Front.down.isFiring
      //  //  //  //)
      //  //  //  //init(mod.front.myUpExtDel2FindFirstVec(idx).getZero)
      //  //  //)
      //  //)
      //  mod.front.tempMyUpExtDel2FindFirstVec(zdx)(idx)(1) := (
      //    mod.front.findFirstFunc(
      //      currMemAddr=(
      //        upExt(1).memAddr(zdx)
      //      ),
      //      prevMemAddr=(
      //        mod.front.myUpExtDel2(idx).memAddr(
      //          PipeMemRmw.modWrIdx
      //        )
      //      ),
      //      curr=(
      //        upExt(1)
      //      ),
      //      prev=(
      //        //tempMyUpExtDel2
      //        mod.front.myUpExtDel2(idx)
      //      ),
      //      zdx=zdx,
      //      isPostDelay=false,
      //    )
      //  )

      //}
      for (idx <- 0 until mod.front.myUpExtDel2.size) {
        for (extIdx <- 0 until extIdxLim) {
          mod.front.myUpExtDel2FindFirstVec(zdx)(extIdx)(idx) := (
            //(
            //  if (zdx == PipeMemRmw.modWrIdx) (
            //    True
            //  ) else (
            //    False
            //  )
            //) && 
            (
              mod.front.findFirstFunc(
                currMemAddr=upExt(1)(extIdxSingle).memAddr(zdx),
                prevMemAddr=(
                  //mod.front.myUpExtDel2(idx).memAddr(
                  //  PipeMemRmw.modWrIdx
                  //)
                  //mod.front.myUpExtDel2(idx).memAddr(
                  //  PipeMemRmw.modWrIdx
                  //)
                  mod.front.myUpExtDel2(idx)(extIdx).memAddr(
                    PipeMemRmw.modWrIdx
                  )
                ),
                curr=(
                  upExt(1)(extIdxSingle)
                ),
                prev=(
                  //mod.front.myUpExtDel2(idx)
                  //tempMyUpExtDel2
                  //mod.front.myUpExtDel2(idx)
                  mod.front.myUpExtDel2(idx)(extIdx)
                  //mod.front.myUpExtDel(idx + 1)
                ),
                zdx=zdx,
                isPostDelay=false,
              )
              //RegNextWhen(
                //mod.front.myUpExtDelFullFindFirstVecNotPostDelay(
                //  idx
                //  //+ 1
                //  //+ (
                //  //  doModInModFrontFunc match {
                //  //    case Some(myDoModSingleStageFunc) => 0
                //  //    case None => 1
                //  //  }
                //  //)
                //),
              //  cMid0Front.down.isFiring
              //)
              //init(mod.front.myUpExtDel2FindFirstVec(idx).getZero)
            )
          )
        }
      }
    }
    //--------
    //println(myUpExtDel.size)
    //when (up.isFiring) {
    //  upExt(1).rdMemWord := myRdMemWord
    //}
    doModInModFrontFunc match {
      case Some(myDoModInModFrontFunc) => {
        //assert(modStageCnt == 0)
        myDoModInModFrontFunc(
          tempUpMod(2),
          tempUpMod(1),
          cMid0Front,
          upExt(1)(extIdxSingle).rdMemWord(PipeMemRmw.modWrIdx)
          //myRdMemWord,
        )
      }
      case None => {
        assert(modStageCnt > 0)
        //tempUpMod(2) := tempUpMod(1)
        tempUpMod(2) := tempUpMod(1)
      }
    }
    upExt(1)(extIdxSingle).rdMemWord := myRdMemWord
    when (
      up.isValid
      //up.isFiring
      //&& upExt(0).hazardId.msb
      //&& !rSetRdId
      && (
        if (
          //optEnableModDuplicate
          optModHazardKind == PipeMemRmw.modHazardKindDupl
          //optModHazardKind != PipeMemRmw.modHazardKindDont
        ) {
          upExt(1)(extIdxSingle).hazardId.msb
        } else {
          True
        }
      )
    ) {
      //myRdMemWord 
      rSetRdId := True
      //when (down.isReady) {
        //upExt(1).rdMemWord := myRdMemWord
        //upExt(1).rdMemWord := myRdMemWord
      //}
      //for (zdx <- 0 until modRdPortCnt) {
      //  val tempFindFirst = (
      //    (optModHazardKind == PipeMemRmw.modHazardKindFwd) generate (
      //      //KeepAttribute(
      //        mod.front.myUpExtDel2FindFirstVec(zdx).sFindFirst(
      //          _ === True
      //        )
      //        //.setName("cMid0FrontArea_tempFindFirst")
      //      //)
      //    )
      //    .setName(s"tempFindFirst_${zdx}")
      //  )
      //  val myFwdCond = (
      //    KeepAttribute(
      //      tempFindFirst._1
      //      && (
      //        if (
      //          !doPrevHazardCmpFunc
      //          //&& mod.front.myUpExtDel.size > 1
      //        ) (
      //          mod.front.myHazardCmpFunc(
      //            curr=mod.front.myUpExtDel(0),
      //            prev=mod.front.myUpExtDel(1),
      //            zdx=zdx,
      //            isPostDelay=false,
      //          )
      //        ) else (
      //          True
      //        )
      //      )
      //    )
      //    .setName(s"myFwdCond_${zdx}")
      //  )
      //  val myFwdData = (
      //    KeepAttribute(
      //      doFwdFunc match {
      //        case Some(myDoFwdFunc) => {
      //          myDoFwdFunc(
      //            tempFindFirst._2,
      //            mod.front.myUpExtDel2,
      //            zdx,
      //          )
      //        }
      //        case None => {
      //          mod.front.myUpExtDel2.last.modMemWord
      //        }
      //      }
      //    )
      //    .setName(s"myFwdData_${zdx}")
      //  )
      //  if (optModHazardKind == PipeMemRmw.modHazardKindFwd) {
      //    when (
      //      myFwdCond
      //    ) {
      //      upExt(1).rdMemWord(zdx) := myFwdData
      //      //case None => {
      //      //  assert(false)
      //      //}
      //    }
      //  }
      //}
      //rSavedRdMemWord := myRdMemWord
    }
    for (zdx <- 0 until modRdPortCnt) {
      val frontFindFirst = KeepAttribute(
        (optModHazardKind == PipeMemRmw.modHazardKindFwd) generate (
          mod.front.tempMyUpExtDelFrontFindFirstVec(zdx).sFindFirst(
            _ === True
          )
        )
        .setName("frontFindFirst_${zdx}")
      )
      val myFindFirstUp = KeepAttribute(
        (optModHazardKind == PipeMemRmw.modHazardKindFwd) generate (
          //KeepAttribute(
            mod.front.myUpExtDel2FindFirstVec(zdx)(extIdxUp).sFindFirst(
              _ === True
            )
            //.setName("cMid0FrontArea_myFindFirstUp")
            //mod.front.tempMyUpExtDel2FindFirstVec(zdx).sFindFirst(
            //  _ === M"1-"
            //  //_(0 downto 0) 
            //  //| _(1 downto 1)
            //)
          //)
        )
        .setName(s"myFindFirstUp_${zdx}")
      )
      val myFindFirstDown = KeepAttribute(
        (optModHazardKind == PipeMemRmw.modHazardKindFwd) generate (
          mod.front.myUpExtDel2FindFirstVec(zdx)(extIdxDown).sFindFirst(
            _ === True
          )
        )
        .setName(s"myFindFirstDown_${zdx}")
      )
      //val tempFindFirst = KeepAttribute(
      //  (optModHazardKind == PipeMemRmw.modHazardKindFwd) generate (
      //    //KeepAttribute(
      //      mod.front.tempMyUpExtDel2FindFirstVec(zdx).sFindFirst(
      //        _ === M"-1"
      //        //_(0 downto 0) 
      //        //| _(1 downto 1)
      //      )
      //      //.setName("cMid0FrontArea_tempFindFirst")
      //    //)
      //  )
      //  .setName(s"tempFindFirst_${zdx}")
      //)
      val frontFwdCond = (
        KeepAttribute(
          frontFindFirst._1
        )
        .setName(s"frontFwdCond_${zdx}")
      )
      val frontFwdData = (
        KeepAttribute(
          doFwdFunc match {
            case Some(myDoFwdFunc) => {
              tempMyUpExtDelFrontFindFirstV2d(PipeMemRmw.modWrIdx)(
                frontFindFirst._2
              )(
                extIdxDown
              ).modMemWord
            }
            case None => {
              mod.front.myUpExtDel(0)(extIdxDown).modMemWord
            }
          }
        )
        .setName(s"frontFwdData_${zdx}")
      )
      val myFwdCondUp = (
        KeepAttribute(
          myFindFirstUp._1
          //|| tempFindFirst._1
          //&& (
          //  if (
          //    !doPrevHazardCmpFunc
          //    //&& mod.front.myUpExtDel.size > 1
          //  ) (
          //    mod.front.myHazardCmpFunc(
          //      curr=mod.front.myUpExtDel(0),
          //      prev=mod.front.myUpExtDel(1),
          //      zdx=zdx,
          //      isPostDelay=false,
          //    )
          //  ) else (
          //    True
          //  )
          //)
        )
        .setName(s"myFwdCondUp_${zdx}")
      )
      val myFwdCondDown = (
        KeepAttribute(
          myFindFirstDown._1
        )
        .setName(s"myFwdCondDown_${zdx}")
      )
      val myFwdDataUp = (
        KeepAttribute(
          doFwdFunc match {
            case Some(myDoFwdFunc) => {
              mod.front.myUpExtDel2(myFindFirstUp._2)(
                extIdxUp
              ).modMemWord
            }
            case None => {
              mod.front.myUpExtDel2.last(extIdxUp).modMemWord
            }
          }
        )
        .setName(s"myFwdDataUp_${zdx}")
      )
      val myFwdDataDown = (
        KeepAttribute(
          doFwdFunc match {
            case Some(myDoFwdFunc) => {
              mod.front.myUpExtDel2(myFindFirstDown._2)(
                extIdxDown
              ).modMemWord
            }
            case None => {
              mod.front.myUpExtDel2.last(extIdxDown).modMemWord
            }
          }
        )
        .setName(s"myFwdDataDown_${zdx}")
      )
      //val tempFwdCond = (
      //  KeepAttribute(
      //    tempFindFirst._1
      //  )
      //  .setName(s"tempFwdCond_${zdx}")
      //)
      //val tempFwdData = (
      //  KeepAttribute(
      //    doFwdFunc match {
      //      case Some(myDoFwdFunc) => {
      //        //myDoFwdFunc(
      //        //  tempFindFirst._2,
      //        //  mod.front.myUpExtDel2,
      //        //  zdx,
      //        //)
      //        tempMyUpExtDel2FindFirstV2d(PipeMemRmw.modWrIdx)(
      //          tempFindFirst._2
      //        ).modMemWord
      //      }
      //      case None => {
      //        mod.front.myUpExtDel2.last.modMemWord
      //      }
      //    }
      //  )
      //  .setName(s"tempFwdData_${zdx}")
      //)
      if (optModHazardKind == PipeMemRmw.modHazardKindFwd) {
        //when (
        //  tempFwdCond
        //  //&& myFwdCond
        //) {
        //  upExt(1).rdMemWord(zdx) := (
        //    tempFwdData
        //  )
        //} elsewhen (
        //  myFwdCond
        //) {
        //  upExt(1).rdMemWord(zdx) := (
        //    myFwdData
        //  )
        //} 
        def setToMyFwdUp(): Unit = {
          upExt(1)(extIdxSingle).rdMemWord(zdx) := (
            myFwdDataUp
          )
        }
        def setToMyFwdDown(): Unit = {
          upExt(1)(extIdxSingle).rdMemWord(zdx) := (
            myFwdDataDown
          )
        }
        def innerFunc(): Unit = {
          when (
            myFwdCondUp
          ) {
            setToMyFwdUp()
          } elsewhen (
            myFwdCondDown
          ) {
            setToMyFwdDown()
          } 
        }
        when (frontFwdCond) {
          upExt(1)(extIdxUp).rdMemWord(zdx) := frontFwdData
        } elsewhen (
          myFwdCondUp
          && myFwdCondDown
        ) {
          when (myFindFirstUp._2 < myFindFirstDown._2) {
            //upExt(1).rdMemWord(zdx) := tempFwdData
            setToMyFwdUp()
          } elsewhen (myFindFirstDown._2 < myFindFirstUp._2) {
            //upExt(1).rdMemWord(zdx) := myFwdData
            setToMyFwdDown()
          } otherwise {
            innerFunc()
          }
        } otherwise {
          innerFunc()
        }
        //otherwise {
        //  innerFunc()
        //}
        //elsewhen (
        //  tempFwdCond
        //) {
        //  upExt(1).rdMemWord(zdx) := (
        //    tempFwdData
        //  )
        //}
      }
    }
    //when (
    //  //down.isFiring
    //  up.isFiring
    //) {
    //  rSetRdId := False
    //}
    //--------
    //when (
    //  down.isFiring
    //  //up.isFiring
    //) {
    //  //rRdId := rRdId + 1
    //  rSetRdId := False
    //}
    //when (up.isValid) {
      tempUpMod(1) := tempUpMod(0)
    //}
    tempUpMod(1).allowOverride
    tempUpMod(1).setPipeMemRmwExt(
      inpExt=upExt(1)(extIdxSingle),
      memArrIdx=memArrIdx,
    )
    tempUpMod(2).getPipeMemRmwExt(
      outpExt=upExt(2)(extIdxUp),
      memArrIdx=memArrIdx,
    )
    up(mod.front.outpPipePayload) := RegNext(tempUpMod(2))
    when (up.isValid) {
      up(mod.front.outpPipePayload) := tempUpMod(2)
    }
    //if (modStageCnt == 0) {
    //  doModInModFront(
    //    outpMod=tempUpMod(2),
    //    inpMod=tempUpMod(1),
    //  )
    //} else { // if (modStageCnt > 0)
    //  tempUpMod(2) := tempUpMod(1)
    //}
    //val rDbgSeenFrontUpFiring = (debug) generate (
    //  RegNextWhen(True, cFront.up.isFiring) init(False)
    //)
    //val rDbgPrevMemAddr = (debug) generate (
    //  RegNextWhen(
    //    cFrontArea.upExt(1).memAddr, cFront.up.isFiring
    //  ) init(0x0)
    //)
    //val tempModMemReadAsync = (debug) generate (
    //  //RegNextWhen(
    //  //  modMem.readAsync(
    //  //    address=upExt(1).memAddr,
    //  //  ),
    //  //  //cFront.down.isFiring,
    //  //  up.isFiring
    //  //) init(myRdMemWord.getZero)
    //  Reg(cloneOf(myRdMemWord)) init(myRdMemWord.getZero)
    //  //cloneOf(myRdMemWord)
    //)
    //val rPrevTempModMemReadAsync = (debug) generate (
    //  RegNext(tempModMemReadAsync) init(tempModMemReadAsync.getZero)
    //)
    //--------
    //--------
  }
  //val cMid1Front = mod.front.cMid1Front
  //val cMid1FrontArea = new cMid1Front.Area {
  //  val upExt = KeepAttribute(
  //    /*Vec.fill(2)*/(mkExt())
  //    .setName("cMid1FrontArea_upExt")
  //  )
  //  up(mod.front.outpPipePayload).getPipeMemRmwExt(
  //    outpExt=upExt,
  //    memArrIdx=memArrIdx,
  //  )
  //  //myUpExtDel(1) := upExt
  //  myUpExtDel(1) := (
  //    RegNext(myUpExtDel(1)) init(myUpExtDel(1).getZero)
  //  )
  //  when (up.isValid) {
  //    myUpExtDel(1) := upExt
  //  }
  //}
  //val cMid2Front = mod.front.cMid2Front
  //val cMid2FrontArea = new cMid2Front.Area {
  //  val upExt = KeepAttribute(
  //    /*Vec.fill(2)*/(mkExt())
  //    .setName("cMid2FrontArea_upExt")
  //  )
  //  up(mod.front.outpPipePayload).getPipeMemRmwExt(
  //    outpExt=upExt,
  //    memArrIdx=memArrIdx,
  //  )
  //  myUpExtDel(2) := (
  //    RegNext(myUpExtDel(2)) init(myUpExtDel(2).getZero)
  //  )
  //  when (up.isValid) {
  //    myUpExtDel(2) := upExt
  //  }
  //}
  val cBack = mod.back.cBack
  val cBackArea = new cBack.Area {
    //haltWhen(
    //  !(RegNextWhen(True, io.front.isFiring) init(False))
    //)
    val upExt = Vec.fill(
      2
      //1
    )(
      mkExt(myVivadoDebug=true)
    ).setName("cBackArea_upExt")
    for (extIdx <- 0 until extIdxLim) {
      upExt(1)(extIdx) := (
        RegNext(upExt(1)(extIdx)) init(upExt(1)(extIdx).getZero)
      )
      upExt(1)(extIdx).allowOverride
    }
    when (
      //up.isValid
      up.isFiring
    ) {
      upExt(1)(extIdxUp) := upExt(0)(extIdxSingle)
    }
    when (
      down.isFiring
    ) {
      upExt(1)(extIdxDown) := RegNext(upExt(1)(extIdxUp))
    }
    upExt(1)(extIdxUp).valid := up.isValid
    upExt(1)(extIdxUp).ready := up.isReady
    upExt(1)(extIdxUp).fire := up.isFiring
    //upExt(1)(extIdxDown).valid := RegNext(down.isValid)
    //upExt(1)(extIdxDown).ready := RegNext(down.isReady)
    //upExt(1)(extIdxDown).fire := RegNext(down.isFiring)
    def tempMyUpExtDelLast = (
      //if (!optModFwdToFront) (
        //myUpExtDel(myUpExtDel.size - 1)
        myUpExtDel.last
      //) else (
      //  mod.front.myUpExtDelFwd
      //)
    )
    //def tempMyUpExtDelPenLast = (
    //  myUpExtDel(myUpExtDel.size - 2)
    //)
    //def tempMyUpExtDelFrontFwd = (
    //  //if (!optModFwdToFront) (
    //    //myUpExtDel(myUpExtDel.size - 2)
    //    myUpExtDel.last
    //  //) else (
    //    //myUpExtDel(myUpExtDel.size - 1)
    //  //  myUpExtDel.last
    //  //)
    //)

    //doModInModFrontFunc match {
    //  case Some(myDoModSingleStageFunc) => {
    //  }
    //  case None => {
      if (
        //(
        //  !optModFwdToFront
        //) && (
          //myUpExtDel.size - 2 >= 1
        //)
        true
      ) {
        //tempMyUpExtDelFrontFwd := (
        //  RegNext(tempMyUpExtDelFrontFwd)
        //  init(tempMyUpExtDelFrontFwd.getZero)
        //)
        //when (
        //  up.isValid
        //) {
          //tempMyUpExtDelFrontFwd := upExt(1)
          //tempMyUpExtDelFrontFwd.allowOverride
          //tempMyUpExtDelFrontFwd.valid := upExt(1).valid
          //tempMyUpExtDelFrontFwd.ready := upExt(1).ready
          //tempMyUpExtDelFrontFwd.fire := upExt(1).fire
          //--------
          tempMyUpExtDelLast := upExt(1)
          //tempMyUpExtDelPenLast.allowOverride
          //tempMyUpExtDelPenLast.valid := upExt(1).valid
          //tempMyUpExtDelPenLast.ready := upExt(1).ready
          //tempMyUpExtDelPenLast.fire := upExt(1).fire
          //--------
          //for (extIdx <- 0 until extIdxLim) {
          //  tempMyUpExtDelLast(extIdx) := (
          //    RegNextWhen(
          //      tempMyUpExtDelPenLast(extIdx),
          //      down.isFiring,
          //    )
          //    init(tempMyUpExtDelLast.getZero)
          //  )
          //}
          //--------
          //tempMyUpExtDelLast := tempMyUpExtDelFrontFwd
        //}
      }
    //  }
    //}
    if (
      //myUpExtDel.size - 2 >= 0
      true
    ) {
      //myUpExtDel(myUpExtDel.size - 1) := (
      //  RegNextWhen(
      //    myUpExtDel(myUpExtDel.size - 2),
      //    //down.isFiring,
      //    up.isFiring,
      //    //down.isReady
      //    //down.isFiring
      //    //down.isReady
      //    //up.isValid
      //    //True
      //  )
      //  init(myUpExtDel(myUpExtDel.size - 1).getZero)
      //)

      //when (io.back.isValid) {
      //  io.back(io.backPayload).getPipeMemRmwExt(
      //    outpExt=myUpExtDel(myUpExtDel.size - 1),
      //    memArrIdx=memArrIdx,
      //  )
      //}
      //--------
      // BEGIN: old
      //tempMyUpExtDelLast := (
      //  RegNextWhen(
      //    tempMyUpExtDelFrontFwd,
      //    //up.isFiring,
      //    down.isFiring
      //    //down.isValid
      //    //mod.back.cLastBack.up.isFiring
      //  )
      //  init(tempMyUpExtDelLast.getZero)
      //)
      // END: old
      //mod.back.cLastBack.up(mod.back.pipePayload).getPipeMemRmwExt(
      //  outpExt=tempMyUpExtDelLast,
      //  memArrIdx=memArrIdx,
      //)
      //tempMyUpExtDelLast.ready.allowOverride
      //tempMyUpExtDelLast.valid.allowOverride
      //tempMyUpExtDelLast.ready := mod.back.cLastBack.up.ready
      //tempMyUpExtDelLast.valid := mod.back.cLastBack.up.valid
      //--------
      //mod.back.cLastBack.up(mod.back.pipePayload).getPipeMemRmwExt(
      //  outpExt=tempMyUpExtDelLast,
      //  memArrIdx=memArrIdx,
      //)
      //tempMyUpExtDelLast.valid.allowOverride
      //tempMyUpExtDelLast.valid := (
      //  //down.isValid
      //  //io.back.isValid
      //  mod.back.cLastBack.up.isValid
      //)
      //--------
      //when (
      //  //mod.back.cLastBack.up.isValid
      //  //up.isValid
      //  //myWriteEnable
      //  //down.isValid
      //  //up.isFiring
      //  down.isFiring
      //  //myWriteEnable
      //  //up.isFiring
      //) {
      //  //mod.back.cLastBack.up(mod.back.pipePayload).getPipeMemRmwExt(
      //  //  outpExt=tempMyUpExtDelLast,
      //  //  memArrIdx=memArrIdx,
      //  //)
      //  tempMyUpExtDelLast := (
      //    RegNext(tempMyUpExtDelFrontFwd)
      //    init(tempMyUpExtDelLast.getZero)
      //  )
      //}
    } 
    //when (down.isValid) {
    //  mod.front.myUpExtDelFwd := (
    //    RegNextWhen(
    //      tempMyUpExtDelLast,
    //      down.isFiring
    //    ) init(tempMyUpExtDelLast.getZero)
    //  )
    //}
    //--------
    //mod.front.myUpExtDelFwd := (
    //  //RegNextWhen(
    //    tempMyUpExtDelLast,
    //  //  mod.back.cLastBack.down.isFiring,
    //  //) init(mod.front.myUpExtDelFwd.getZero)
    //  //tempMyUpExtDelFrontFwd
    //)
    //--------

    val tempUpMod = Vec.fill(2)(
      //Vec.fill(extIdxLim)(
        modType()
      //)
    ).setName("cBackArea_tempUpMod")
    tempUpMod(0).allowOverride
    tempUpMod(0) := up(mod.back.pipePayload)
    tempUpMod(0).getPipeMemRmwExt(
      outpExt=upExt(0)(extIdxSingle),
      memArrIdx=memArrIdx,
    )
    tempUpMod(1) := tempUpMod(0)
    tempUpMod(1).allowOverride
    tempUpMod(1).setPipeMemRmwExt(
      inpExt=upExt(1)(extIdxUp),
      memArrIdx=memArrIdx,
    )
    //val dbgDoClear = (optEnableClear) generate (
    //  KeepAttribute(Bool())
    //)
    //val dbgDoWrite = /*(debug) generate*/ (
    //  KeepAttribute(Bool())
    //)
    val dbgDoWrite = mod.back.dbgDoWrite
    //val dbgDoClear = mod.back.dbgDoClear
    //when (
    //  dbgDoWrite
    //  && !dbgDoClear
    //) {
    //}
    if (
      //debug
      true
    ) {
      dbgDoWrite := False
    }
    val extDbgDoWriteCond = (
      (
        if (
          //optEnableModDuplicate
          optModHazardKind == PipeMemRmw.modHazardKindDupl
        ) (
          upExt(0)(extIdxSingle).hazardId.msb
        ) else (
          True
        )
      )
      //|| (
      //  if (optEnableClear) (
      //    io.clear.valid
      //  ) else (
      //    False
      //  )
      //)
    )
    val myWriteAddr = mod.back.myWriteAddr
    //myWriteAddr := (
    //  RegNext(myWriteAddr) init(myWriteAddr.getZero)
    //)
    //when (up.isValid) {
      myWriteAddr := (
        if (optEnableClear) (
          Mux[UInt](
            io.clear.fire,
            io.clear.payload,
            upExt(0)(extIdxSingle).memAddr(PipeMemRmw.modWrIdx),
          )
        ) else (
          upExt(0)(extIdxSingle).memAddr(PipeMemRmw.modWrIdx)
        )
      )
    //}
    val myWriteData = mod.back.myWriteData
    //myWriteData := (
    //  RegNext(myWriteData) init(myWriteData.getZero)
    //)
    //when (up.isValid) {
      myWriteData := (
        //upExt(0).modMemWord
        if (optEnableClear) (
          Mux[WordT](
            io.clear.fire,
            wordType().getZero,
            upExt(0)(extIdxSingle).modMemWord,
          )
        ) else (
          upExt(0)(extIdxSingle).modMemWord
        )
      )
    //}
    val myWriteEnable = mod.back.myWriteEnable
    //mod.front.myUpExtDelFwd := (
    //  RegNext(mod.front.myUpExtDelFwd)
    //  init(mod.front.myUpExtDelFwd.getZero)
    //)
    //--------
    //when (
    //  myWriteEnable
    //  //down.isFiring
    //) {
    //  mod.front.rMyUpExtDelFinal := mod.front.myUpExtDel.last
    //  //mod.front.myUpExtDelFwd := mod.front.myUpExtDel.last
    //}
    //--------
    //mod.front.myUpExtDelFwd := mod.front.rMyUpExtDelFinal

    myWriteEnable := (
      dbgDoWrite
      || (
        if (optEnableClear) (
          io.clear.fire
        ) else (
          False
        )
      )
      //&& up.isValid
      //&& down.isReady
    )
    //else {
    //  myUpExtDel(myUpExtDel.size - 1) := upExt(1)
    //}
    
    when (
      //!clockDomain.isResetActive

      //&& isValid
      //&& up.isFiring
      //&& 
      //up.isValid
      //(
      //  if (optModHazardKind != PipeMemRmw.modHazardKindFwd) (
          up.isFiring
          //down.isFiring
          //up.isValid
          //|| (
          //  RegNextWhen(True, up.isValid)
          //  init(False)
          //)
      //  ) else (
      //    up.isValid
      //  )
      //)
      //&& (
      //  //if (optModHazardKind != PipeMemRmw.modHazardKindFwd) (
      //  //  down.isReady
      //  //) else (
      //  //  True
      //  //)
      //  down.isReady
      //)
      //True
      //&& upExt.rdValid
      //&& up.isValid
      //&& (upExt(0).hazardId) === 0
      //&& upExt(0).rdValid
      && extDbgDoWriteCond
    ) {
      if (
        //debug
        true
      ) {
        mod.back.rTempWord := upExt(0)(extIdxSingle).modMemWord
        dbgDoWrite := True
      }
      //modMem.write(
      //  address=upExt(0).memAddr,
      //  data=upExt(0).modMemWord,
      //)
    }
    if (optReorder) {
      when (
        myWriteEnable
        && myWriteAddr === io.reorderCommitHead
        && getReorderValid(myWriteData)
        && !getReorderBusyEtc(myWriteData)
        && upExt(0)(extIdxSingle).reqReorderCommit
      ) {
        mod.rReorderCommitHead := mod.rReorderCommitHead
        upExt(1)(extIdxUp).didReorderCommit := True
      }
    }
    memWriteAll(
      address=myWriteAddr,
      data=myWriteData,
      enable=myWriteEnable,
    )
    //when (!myWriteEnable) {
    //  //throwIt()
    //  forgetOneNow()
    //}
    //when (
    //  up.isValid
    //  && !extDbgDoWriteCond
    //  && (
    //    if (optEnableClear) (
    //      !io.clear.fire
    //    ) else (
    //      True
    //    )
    //  )
    //) {
    //  throwIt()
    //}
    //--------
    //tempUpMod(1) := tempUpMod(0)
    //tempUpMod(1).setPipeMemRmwExt(
    //  ext=upExt,
    //  memArrIdx=memArrIdx,
    //)
    ////--------
    //bypass(mod.back.pipePayload) := tempUpMod(1)
    //--------
  }
  //--------
  //val dualRd = (io.optDualRd) generate new Area {
  //  val pipe = PipeHelper(linkArr=linkArr)
  //  val pipePayload = Payload(dualRdType())
  //  val cDualRdFront = pipe.addStage("DualRdFront")
  //  //val front = new Area {
  //  //}
  //  //val back = new Area {
  //  //  val pipe = PipeHelper(linkArr=linkArr)
  //  //  val pipePayload = Payload(dualRdType())
  //  //}
  //}
  //val cDualRdFront = 
  //--------
  //val dualRd = (io.optDualRd) generate new Area {
  //  //--------
  //  GenerationFlags.formal {
  //    when (pastValidAfterReset) {
  //      val myBackExt = mkExt().setName(
  //        "dbg_backExt"
  //      )
  //      val myDualRdBackExt = mkExt().setName(
  //        "dbg_dualRd_backExt"
  //      )
  //      io.back.payload.getPipeMemRmwExt(
  //        outpExt=myBackExt,
  //        memArrIdx=memArrIdx,
  //      )
  //      io.dualRdBack.payload.getPipeMemRmwExt(
  //        outpExt=myDualRdBackExt,
  //        memArrIdx=memArrIdx,
  //      )
  //      //val tempCoverBackVec = Vec.fill(wordCount)(
  //      //  Reg(Bool()) init(False)
  //      //)
  //      val rCoverBack = (
  //        Reg(SInt(wordCount bits)) init(0x0)
  //        setName("dualRd_rCoverBack")
  //      )
  //      val rCoverBackHazardIdGe0 = (
  //        Reg(SInt(wordCount bits)) init(0x0)
  //        setName("dualRd_rCoverBackHazardIdGe0")
  //      )
  //      val rCoverDualRdBack = (
  //        Reg(SInt(wordCount bits)) init(0x0)
  //        setName("dualRd_rCoverDualRdBack")
  //      )
  //      val rCoverDualRdBackNotReadyCnt = (
  //        Reg(UInt(8 bits)) init(0x0)
  //        setName("dualRd_rCoverDualRdBackNotReadyCnt")
  //      )
  //      when (
  //        RegNextWhen(True, io.dualRdBack.fire) init(False)
  //        && !io.dualRdBack.ready
  //      ) {
  //        rCoverDualRdBackNotReadyCnt := rCoverDualRdBackNotReadyCnt + 1
  //      }

  //      for (idx <- 0 until wordCount) {
  //        when (
  //          io.back.fire
  //          && myBackExt.memAddr === idx
  //          //&& myBackExt.modMemWord.asBits.asUInt > 0
  //        ) {
  //          when (myBackExt.hazardId.msb) {
  //            when (
  //              //myBackExt.modMemWord.asBits.asUInt === idx
  //              myBackExt.rdMemWord.asBits.asUInt === idx + 1
  //            ) {
  //              rCoverBack(idx) := True
  //            }
  //          } otherwise {
  //            rCoverBackHazardIdGe0(idx) := True
  //          }
  //        }
  //        when (
  //          io.dualRdBack.fire
  //          && myDualRdBackExt.memAddr === idx
  //          //&& myDualRdBackExt.modMemWord.asBits.asUInt > 0
  //          //&& myDualRdBackExt.modMemWord.asBits.asUInt === idx
  //          && myDualRdBackExt.rdMemWord.asBits.asUInt === idx + 1
  //        ) {
  //          rCoverDualRdBack(idx) := True
  //        }
  //      }
  //      cover(
  //        (
  //          rCoverBack === -1
  //          //=== B(rTempCoverBack.getWidth bits, default -> True)
  //        ) && (
  //          //rCoverBackHazardIdGe0 === -1
  //          True
  //        ) && (
  //          rCoverDualRdBack === -1
  //          //=== B(rTempCoverDualRdBack.getWidth bits, default -> True)
  //        ) && (
  //          rCoverDualRdBackNotReadyCnt > 2
  //        )
  //      )
  //      //cover(
  //      //  (
  //      //    //RegNextWhen(
  //      //    //  True,
  //      //    //  (
  //      //    //    io.back.fire
  //      //    //    && myBackExt.memAddr === 0
  //      //    //    && (
  //      //    //      myBackExt.modMemWord.asBits.asUInt > 0
  //      //    //    )
  //      //    //  )
  //      //    //) init(False)
  //      //  )
  //      //)

  //      //cover(
  //      //  io.back.valid
  //      //  && io.dualRdBack.valid
  //      //  && myDualRd
  //      //)
  //      //if (!forFmax) {
  //      //  //when (
  //      //  //  io.back.valid
  //      //  //  && io.dualRdBack.valid
  //      //  //  && myDualRdBackExt.dbgWantNonFmaxFwd
  //      //  //  && myBackExt.dbgWantNonFmaxFwd
  //      //  //  && myDualRdBackExt.hazardId === myBackExt.hazardId
  //      //  //  //&& myDualRdBackExt.hazardId.msb
  //      //  //  && (
  //      //  //    myDualRdBackExt.dbgNonFmaxFwdCnt
  //      //  //    === myBackExt.dbgNonFmaxFwdCnt
  //      //  //  ) && (
  //      //  //    myDualRdBackExt.memAddr === myBackExt.memAddr
  //      //  //  )
  //      //  //) {
  //      //  //  assert(myDualRdBackExt.rdMemWord === myBackExt.rdMemWord)
  //      //  //}
  //      //  cover(
  //      //    //(
  //      //    //  RegNextWhen(True, io.back.fire) init(False)
  //      //    //) && (
  //      //    //  RegNextWhen(True, io.dualRdBack.fire) init(False)
  //      //    //) 
  //      //    //&& 
  //      //    io.back.valid
  //      //    && io.dualRdBack.valid
  //      //    && myDualRdBackExt.dbgWantNonFmaxFwd
  //      //    && myBackExt.dbgWantNonFmaxFwd
  //      //    && (
  //      //      myDualRdBackExt.memAddr === myBackExt.memAddr
  //      //    ) && (
  //      //      myDualRdBackExt.rdMemWord === myBackExt.rdMemWord
  //      //    )
  //      //  )
  //      //  //cover(
  //      //  //  //(
  //      //  //  //  RegNextWhen(True, io.back.fire) init(False)
  //      //  //  //) && (
  //      //  //  //  RegNextWhen(True, io.dualRdBack.fire) init(False)
  //      //  //  //)
  //      //  //  //&& 
  //      //  //  //--------
  //      //  //  //io.back.valid
  //      //  //  //&& 
  //      //  //  //io.dualRdBack.valid
  //      //  //  //--------
  //      //  //  //&& 
  //      //  //  myDualRdBackExt.dbgWantNonFmaxFwd
  //      //  //  && myBackExt.dbgWantNonFmaxFwd
  //      //  //  && (
  //      //  //    myDualRdBackExt.memAddr === myBackExt.memAddr
  //      //  //  ) && (
  //      //  //    myDualRdBackExt.rdMemWord =/= myBackExt.rdMemWord
  //      //  //  )
  //      //  //)
  //      //}
  //      //when (
  //      //  io.dualRdBack.valid
  //      //  && myDualRdBackExt.dbgWantNonFmaxFwd
  //      //) {
  //      //}
  //    }
  //  }
  //}
  //--------
  val dualRd = (io.optDualRd) generate new Area {
    val pipe = PipeHelper(linkArr=myLinkArr)
    val myRdMemWord = wordType()
    //val rReorderCommitHead = (optReorder) generate (
    //  Reg(UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)) init(0x0)
    //)
    //io.reorderCommitHead := rReorderCommitHead

    //if (vivadoDebug) {
    //  myRdMemWord.addAttribute(
    //    "MARK_DEBUG", "TRUE",
    //  )
    //}

    val cFront = pipe.addStage(
      name=pipeName + "_DualRd_Front",
      optIncludeS2M=(
        //false
        true
      )
    )
    val cIoDualRdFront = DirectLink(
      up=io.dualRdFront,
      down=cFront.up,
    )
    myLinkArr += cIoDualRdFront
    //val cMidFrontFront = pipe.addStage(
    //  name=pipeName + "_DualRd_MidFrontFront",
    //)
    val cMid0 = pipe.addStage(
      name=pipeName + "_DualRd_Mid0",
      optIncludeS2M=(
        //false
        true
      ),
      //finish=true,
    )
    //val cMid1 = pipe.addStage(
    //  name=pipeName + "_DualRd_Mid1",
    //  optIncludeS2M=false,
    //  //finish=true,
    //)
    ////val cMid2 = pipe.addStage(
    ////  name=pipeName + "_DualRd_Mid2",
    ////  optIncludeS2M=false,
    ////)
    ////val rPrevMyRdMemWord = (
    ////  RegNextWhen(
    ////    myRdMemWord, cMid1.up.isFiring
    ////  ) init(myRdMemWord.getZero)
    ////)
    //val cBack = pipe.addStage(
    //  name=pipeName + "_DualRd_Back",
    //  //finish=true,
    //)
    val cLast = pipe.addStage(
      name=pipeName + "_DualRd_Last",
      finish=true,
    )
    val cIoDualRdBack = DirectLink(
      up=(
        //cBack.down
        cLast.down,
        //cMid0.down
      ),
      down=io.dualRdBack,
    )
    myLinkArr += cIoDualRdBack

    //val inpPipePayload = Payload(dualRdType())
    def inpPipePayload = io.dualRdFrontPayload
    //val midPipePayload = Payload(dualRdType())
    //val outpPipePayload = Payload(dualRdType())
    def outpPipePayload = io.dualRdBackPayload
    //pipe.first.up.driveFrom(io.dualRdFront)(
    //  con=(node, payload) => {
    //    node(inpPipePayload) := payload
    //  }
    //)
    //pipe.last.down.driveTo(io.dualRdBack)(
    //  con=(payload, node) => {
    //    payload := node(outpPipePayload)
    //  }
    //)
    val cDualRdFrontArea = new cFront.Area {
      //val upExt = Vec.fill(2)(mkExt())
      val myInpDualRd = dualRdType()
      //val myMidDualRd = dualRdType()
      val myInpUpExt = mkExt()
      //val myMidUpExt = mkExt()
      //if (vivadoDebug) {
      //  myInpDualRd.addAttribute("MARK_DEBUG", "TRUE")
      //  //myOutpDualRd.addAttribute("MARK_DEBUG", "TRUE")
      //}

      myInpDualRd := up(inpPipePayload)
      myInpDualRd.getPipeMemRmwExt(
        outpExt=myInpUpExt(extIdxSingle),
        memArrIdx=memArrIdx,
      )
      //myMidDualRd := myInpDualRd
      //myMidDualRd.allowOverride
      //myMidDualRd.setPipeMemRmwExt(
      //  inpExt=
      //  memArrIdx=memArrIdx,
      //)
      //up(midPipePayload) := myMidDualRd
      //myRdMemWord := dualRdMem.readSync(
      //  address=myInpUpExt.memAddr,
      //  enable=up.isFiring,
      //)
      myRdMemWord := dualRdMem.readSync(
        address=myInpUpExt(extIdxSingle).memAddr(PipeMemRmw.modWrIdx),
        enable=up.isFiring,
      )
      //when (
      //  if (!optReorder) (
      //    True
      //  ) else (
      //    myInpUpExt.memAddrReorderValid
      //  )
      //) {
      //  myRdMemWord := dualRdMem.readSync(
      //    address=myInpUpExt.memAddr,
      //    enable=up.isFiring,
      //  )
      //} otherwise {
      //  myRdMemWord := dualRdMem.readSync(
      //    address=rReorderCommitHead,
      //    enable=up.isFiring,
      //  )
      //  val reorderMemAddrPlus1 = (
      //    Cat(B"1'b0", rReorderCommitHead).asUInt + 1
      //  )
      //  when (reorderMemAddrPlus1 < wordCount) {
      //    rReorderCommitHead := 0x0
      //  } otherwise {
      //    rReorderCommitHead := (
      //      reorderMemAddrPlus1(rReorderCommitHead.bitsRange)
      //    )
      //  }
      //}
    }
    val cDualRdMid0Area = new cMid0.Area {
      val myInpDualRd = dualRdType()
      val myOutpDualRd = dualRdType()
      val myInpUpExt = mkExt()
      val myOutpUpExt = mkExt()
      //if (vivadoDebug) {
      //  myInpDualRd.addAttribute("MARK_DEBUG", "TRUE")
      //  myOutpDualRd.addAttribute("MARK_DEBUG", "TRUE")
      //}

      myInpDualRd := up(inpPipePayload)
      myInpDualRd.getPipeMemRmwExt(
        outpExt=myInpUpExt(extIdxSingle),
        memArrIdx=memArrIdx,
      )
      myOutpUpExt := myInpUpExt
      myOutpUpExt.allowOverride
      myOutpDualRd := myInpDualRd
      myOutpDualRd.allowOverride
      myOutpDualRd.setPipeMemRmwExt(
        inpExt=myOutpUpExt(extIdxSingle),
        memArrIdx=memArrIdx,
      )
      //up(midPipePayload) := myOutpDualRd
      //when (up.isFiring) {
      //}
      val rDoIt = Reg(Bool()) init(False)
      myOutpUpExt(extIdxSingle).rdMemWord(PipeMemRmw.modWrIdx) := (
        RegNext(myOutpUpExt(extIdxSingle).rdMemWord(PipeMemRmw.modWrIdx))
        init(myOutpUpExt(extIdxSingle).rdMemWord(
          PipeMemRmw.modWrIdx
        ).getZero)
      )
      myOutpUpExt(extIdxSingle).modMemWord := (
        myOutpUpExt(extIdxSingle).rdMemWord(PipeMemRmw.modWrIdx)
      )
      up(outpPipePayload) := myOutpDualRd
      when (
        up.isValid
        && !rDoIt
      ) {
        rDoIt := True
        myOutpUpExt(extIdxSingle).rdMemWord(PipeMemRmw.modWrIdx) := (
          myRdMemWord
          //rPrevMyRdMemWord
        )
      }
      when (
        up.isFiring
      ) {
        rDoIt := False
      }
    }
    //val cDualRdMidBackArea = new cMidBack.Area {
    //}
    //val cDualRdBackArea = new cBack.Area {
    //}
  }
  //--------
  // BEGIN: working dualRd?
  //val dualRd = (io.optDualRd) generate new Area {
  //  val rDoIt = Reg(Bool()) init(False)
  //  val midFrontStm = Stream(dualRdType())
  //  val midMidStm = Stream(dualRdType())
  //  val midBackStm = Stream(dualRdType())
  //  ////midFrontStm <-/< io.dualRdFront
  //  //midFrontStm << io.dualRdFront
  //  //midFrontStm
  //  //--------
  //  //val myPayload = Payload(dualRdType())
  //  //val midFrontStm = Stream(dualRdType())
  //  ////val midMidStm = Stream(dualRdType())
  //  //val midPipe = PipeHelper(linkArr=linkArr)
  //  //val cArr = new ArrayBuffer[CtrlLink]()
  //  //--------
  //  ////for (
  //  ////  // the + 3 is necessary
  //  ////  idx <- 0 until modStageCnt + 3
  //  ////) {
  //  ////  //println(idx)
  //  ////  cArr += midPipe.addStage(
  //  ////    name=f"DualRd_$idx",
  //  ////    finish=(idx + 1 >= modStageCnt + 3),
  //  ////  )
  //  ////}
  //  //cArr += midPipe.addStage(
  //  //  name=f"DualRd_0",
  //  //  //finish=true,
  //  //)
  //  ////cArr += midPipe.addStage(
  //  ////  name=f"DualRd_1",
  //  ////  //finish=true,
  //  ////)
  //  //cArr += midPipe.addStage(
  //  //  name=f"DualRdLast",
  //  //  finish=true,
  //  //)

  //  //val midBackStm = Stream(dualRdType())
  //  //midPipe.first.up.driveFrom(midFrontStm)(
  //  //  con=(node, payload) => {
  //  //    node(myPayload) := payload
  //  //  }
  //  //)
  //  val myFrontExt = mkExt().setName(
  //    "dualRd_frontExt"
  //  )
  //  val myMidFrontExt = mkExt().setName(
  //    "dualRd_midFrontExt"
  //  )
  //  val myMidMidExt = mkExt().setName(
  //    "dualRd_midMidExt"
  //  )
  //  val myMidBackExt = mkExt().setName(
  //    "dualRd_midBackExt"
  //  )
  //  //val myMidFrontExt = mkExt().setName(
  //  //  "dualRd_midFrontExt"
  //  //)
  //  //val rDbgNonFmaxFwdCnt = (debug) generate (
  //  //  Reg(cloneOf(myMidFrontExt.dbgNonFmaxFwdCnt)) //init(0x0)
  //  //)
  //  //for (idx <- 0 until rDbgNonFmaxFwdCnt.size) {
  //  //  rDbgNonFmaxFwdCnt(idx).init(rDbgNonFmaxFwdCnt(idx).getZero)
  //  //}
  //  //--------
  //  io.dualRdFront.getPipeMemRmwExt(
  //    outpExt=myFrontExt,
  //    memArrIdx=memArrIdx,
  //  )
  //  myMidFrontExt.allowOverride
  //  //val tempReadSync = Reg(wordType())
  //  //tempReadSync.init(tempReadSync.getZero)
  //  val tempReadSync = wordType()
  //  //tempReadSync := RegNext(tempReadSync) init(tempReadSync.getZero)
  //  tempReadSync := (
  //    dualRdMem.readSync(
  //      //address=myMidFrontExt.memAddr
  //      address=myFrontExt.memAddr,
  //      enable=midFrontStm.fire,
  //    )
  //  )
  //  //when (
  //  //  //io.dualRdFront.fire
  //  //  //&& midFrontStm.valid
  //  //  midFrontStm.fire
  //  //) {
  //  //  tempReadSync := dualRdMem.readSync(
  //  //    //address=myMidFrontExt.memAddr
  //  //    address=myFrontExt.memAddr
  //  //  )
  //  //}
  //  midFrontStm <-/< io.dualRdFront
  //  //frontPayload.getPipeMemRmwExt(
  //  //  outpExt=myMidFrontExt,
  //  //  memArrIdx=memArrIdx,
  //  //)
  //  //midFrontStm.getPipeMemRmwExt(
  //  //  outpExt=myMidFrontExt,
  //  //  memArrIdx=memArrIdx,
  //  //)
  //  when (
  //    midFrontStm.valid
  //    && !rDoIt
  //  ) {
  //    rDoIt := True
  //    myMidMidExt.rdMemWord := tempReadSync
  //  }
  //  when (
  //    midFrontStm.fire
  //  ) {
  //    rDoIt := False
  //  }
  //  midFrontStm.translateInto(
  //    into=midMidStm
  //  )(
  //    dataAssignment=(
  //      midMidPayload,
  //      midFrontPayload,
  //    ) => {
  //      midMidPayload := midFrontPayload
  //      midMidPayload.allowOverride
  //      midFrontPayload.getPipeMemRmwExt(
  //        outpExt=myMidFrontExt,
  //        memArrIdx=memArrIdx,
  //      )
  //      myMidMidExt := myMidFrontExt
  //      myMidMidExt.allowOverride
  //      //myMidMidExt.rdMemWord := (
  //      //  RegNext(myMidMidExt.rdMemWord)
  //      //  init(myMidMidExt.rdMemWord.getZero)
  //      //  //rTempReadSync
  //      //  //dualRdMem.readSync(
  //      //  //  address=myMidFrontExt.memAddr
  //      //  //)
  //      //)
  //      //when (midMidStm.valid) {
  //      //  myMidMidExt.rdMemWord := (
  //      //    rTempReadSync
  //      //    //dualRdMem.readSync(
  //      //    //  address=myMidMidExt.memAddr
  //      //    //)
  //      //  )
  //      //}
  //      myMidMidExt.modMemWord := myMidMidExt.rdMemWord
  //      midMidPayload.setPipeMemRmwExt(
  //        inpExt=myMidMidExt,
  //        memArrIdx=memArrIdx,
  //      )
  //    }
  //  )
  //  //midMidStm <-/< midFrontStm
  //  //midMidStm.translateInto(
  //  //  into=midBackStm
  //  //)(
  //  //  dataAssignment=(
  //  //    midBackPayload,
  //  //    midMidPayload,
  //  //  ) => {
  //  //    midBackPayload := midMidPayload
  //  //    midBackPayload.allowOverride
  //  //    midMidPayload.getPipeMemRmwExt(
  //  //      outpExt=myMidMidExt,
  //  //      memArrIdx=memArrIdx,
  //  //    )
  //  //    myMidBackExt := myMidMidExt
  //  //    myMidBackExt.allowOverride
  //  //    myMidBackExt.rdMemWord := (
  //  //      //rTempReadSync
  //  //      //dualRdMem.readAsync(
  //  //      //  address=myMidBackExt
  //  //      //)
  //  //    )
  //  //    myMidBackExt.modMemWord := myMidBackExt.rdMemWord
  //  //    midBackPayload.setPipeMemRmwExt(
  //  //      inpExt=myMidBackExt,
  //  //      memArrIdx=memArrIdx,
  //  //    )
  //  //  }
  //  //)
  //  midBackStm << midMidStm
  //  //midBackStm <-/< midMidStm
  //  io.dualRdBack <-/< midBackStm
  //  //io.dualRdBack << midBackStm
  //  //midBackStm.translateInto(
  //  //  into=io.dualRdBack
  //  //)(
  //  //  dataAssignment=(
  //  //    dualRdBackPayload,
  //  //    midBackPayload,
  //  //  ) => {
  //  //  }
  //  //)
  //  //midBackStm.duplicateWhen(True) << midFrontStm
  //  //when (cFrontArea.nextDuplicateIt) {
  //  //  midBackStm.setBlocked()
  //  //}
  //  //--------
  //  //val stmArr = Array.fill(modStageCnt)(Stream(dualRdType()))
  //  //for (
  //  //  idx <- 0 until stmArr.size
  //  //) {
  //  //  if (idx == 0) {
  //  //    stmArr(idx) <-/< midBackStm
  //  //  } else { // if (idx > 0)
  //  //    stmArr(idx) <-/< stmArr(idx - 1)
  //  //  }
  //  //}
  //  //io.dualRdBack << stmArr.last
  //  //io.dualRdBack << midBackStm
  //  ////io.dualRdBack <-/< midStm
  //  //--------
  //  GenerationFlags.formal {
  //    //when (pastValidAfterReset) {
  //    //  val myBackExt = mkExt().setName(
  //    //    "dbg_backExt"
  //    //  )
  //    //  val myDualRdBackExt = mkExt().setName(
  //    //    "dbg_dualRd_backExt"
  //    //  )
  //    //  io.back.payload.getPipeMemRmwExt(
  //    //    outpExt=myBackExt,
  //    //    memArrIdx=memArrIdx,
  //    //  )
  //    //  io.dualRdBack.payload.getPipeMemRmwExt(
  //    //    outpExt=myDualRdBackExt,
  //    //    memArrIdx=memArrIdx,
  //    //  )
  //    //  //val tempCoverBackVec = Vec.fill(wordCount)(
  //    //  //  Reg(Bool()) init(False)
  //    //  //)
  //    //  def myCoverCntWidth = log2Up(4)
  //    //  val rCoverBack = (
  //    //    //Reg(SInt(wordCount bits)) init(0x0)
  //    //    Vec.fill(wordCount)(
  //    //      Reg(UInt(myCoverCntWidth bits)) init(0x0)
  //    //    )
  //    //    setName("dualRd_rCoverBack")
  //    //  )
  //    //  val rCoverBackHazardIdGe0 = (
  //    //    Reg(SInt(wordCount bits)) init(0x0)
  //    //    //Vec.fill(wordCount)(
  //    //    //  Reg(UInt(myCoverCntWidth bits)) init(0x0)
  //    //    //)
  //    //    setName("dualRd_rCoverBackHazardIdGe0")
  //    //  )
  //    //  val rCoverDualRdBack = (
  //    //    //Reg(SInt(wordCount bits)) init(0x0)
  //    //    Vec.fill(wordCount)(
  //    //      Reg(UInt(myCoverCntWidth bits)) init(0x0)
  //    //    )
  //    //    setName("dualRd_rCoverDualRdBack")
  //    //  )
  //    //  val rCoverDualRdBackNotReadyCnt = (
  //    //    Reg(UInt(8 bits)) init(0x0)
  //    //    setName("dualRd_rCoverDualRdBackNotReadyCnt")
  //    //  )
  //    //  when (
  //    //    //RegNextWhen(True, io.dualRdBack.fire) init(False)
  //    //    //&& 
  //    //    (
  //    //      !io.dualRdBack.ready
  //    //      && !RegNext(io.dualRdBack.ready)
  //    //      && RegNext(RegNext(io.dualRdBack.ready))
  //    //    ) || (
  //    //      !io.dualRdBack.ready
  //    //      && RegNext(io.dualRdBack.ready)
  //    //      && RegNext(RegNext(io.dualRdBack.ready))
  //    //    )
  //    //  ) {
  //    //    rCoverDualRdBackNotReadyCnt := rCoverDualRdBackNotReadyCnt + 1
  //    //  }

  //    //  for (idx <- 0 until wordCount) {
  //    //    when (
  //    //      io.back.fire
  //    //      && myBackExt.memAddr === idx
  //    //      && myBackExt.modMemWord.asBits.asUInt > 0
  //    //    ) {
  //    //      when (myBackExt.hazardId.msb) {
  //    //        when (
  //    //          //myBackExt.modMemWord.asBits.asUInt === idx
  //    //          //myBackExt.rdMemWord.asBits.asUInt === idx + 1
  //    //          True
  //    //        ) {
  //    //          //rCoverBack(idx) := True
  //    //          rCoverBack(idx) := rCoverBack(idx) + 1
  //    //        }
  //    //      } otherwise {
  //    //        rCoverBackHazardIdGe0(idx) := True
  //    //      }
  //    //    }
  //    //    when (
  //    //      io.dualRdBack.fire
  //    //      && myDualRdBackExt.memAddr === idx
  //    //      && myDualRdBackExt.modMemWord.asBits.asUInt > 0
  //    //      //&& myDualRdBackExt.modMemWord.asBits.asUInt === idx
  //    //      //&& myDualRdBackExt.rdMemWord.asBits.asUInt === idx + 1
  //    //    ) {
  //    //      //rCoverDualRdBack(idx) := True
  //    //      rCoverDualRdBack(idx) := rCoverDualRdBack(idx) + 1
  //    //    }
  //    //  }
  //    //  cover(
  //    //    (
  //    //      rCoverBack.asBits.asSInt === -1
  //    //      //=== B(rTempCoverBack.getWidth bits, default -> True)
  //    //    ) && (
  //    //      //rCoverBackHazardIdGe0 === -1
  //    //      True
  //    //    ) && (
  //    //      rCoverDualRdBack.asBits.asSInt === -1
  //    //      //=== B(rTempCoverDualRdBack.getWidth bits, default -> True)
  //    //    ) && (
  //    //      rCoverDualRdBackNotReadyCnt > 5
  //    //    )
  //    //  )
  //    //  //cover(
  //    //  //  (
  //    //  //    //RegNextWhen(
  //    //  //    //  True,
  //    //  //    //  (
  //    //  //    //    io.back.fire
  //    //  //    //    && myBackExt.memAddr === 0
  //    //  //    //    && (
  //    //  //    //      myBackExt.modMemWord.asBits.asUInt > 0
  //    //  //    //    )
  //    //  //    //  )
  //    //  //    //) init(False)
  //    //  //  )
  //    //  //)

  //    //  //cover(
  //    //  //  io.back.valid
  //    //  //  && io.dualRdBack.valid
  //    //  //  && myDualRd
  //    //  //)
  //    //  //if (!forFmax) {
  //    //  //  //when (
  //    //  //  //  io.back.valid
  //    //  //  //  && io.dualRdBack.valid
  //    //  //  //  && myDualRdBackExt.dbgWantNonFmaxFwd
  //    //  //  //  && myBackExt.dbgWantNonFmaxFwd
  //    //  //  //  && myDualRdBackExt.hazardId === myBackExt.hazardId
  //    //  //  //  //&& myDualRdBackExt.hazardId.msb
  //    //  //  //  && (
  //    //  //  //    myDualRdBackExt.dbgNonFmaxFwdCnt
  //    //  //  //    === myBackExt.dbgNonFmaxFwdCnt
  //    //  //  //  ) && (
  //    //  //  //    myDualRdBackExt.memAddr === myBackExt.memAddr
  //    //  //  //  )
  //    //  //  //) {
  //    //  //  //  assert(myDualRdBackExt.rdMemWord === myBackExt.rdMemWord)
  //    //  //  //}
  //    //  //  cover(
  //    //  //    //(
  //    //  //    //  RegNextWhen(True, io.back.fire) init(False)
  //    //  //    //) && (
  //    //  //    //  RegNextWhen(True, io.dualRdBack.fire) init(False)
  //    //  //    //) 
  //    //  //    //&& 
  //    //  //    io.back.valid
  //    //  //    && io.dualRdBack.valid
  //    //  //    && myDualRdBackExt.dbgWantNonFmaxFwd
  //    //  //    && myBackExt.dbgWantNonFmaxFwd
  //    //  //    && (
  //    //  //      myDualRdBackExt.memAddr === myBackExt.memAddr
  //    //  //    ) && (
  //    //  //      myDualRdBackExt.rdMemWord === myBackExt.rdMemWord
  //    //  //    )
  //    //  //  )
  //    //  //  //cover(
  //    //  //  //  //(
  //    //  //  //  //  RegNextWhen(True, io.back.fire) init(False)
  //    //  //  //  //) && (
  //    //  //  //  //  RegNextWhen(True, io.dualRdBack.fire) init(False)
  //    //  //  //  //)
  //    //  //  //  //&& 
  //    //  //  //  //--------
  //    //  //  //  //io.back.valid
  //    //  //  //  //&& 
  //    //  //  //  //io.dualRdBack.valid
  //    //  //  //  //--------
  //    //  //  //  //&& 
  //    //  //  //  myDualRdBackExt.dbgWantNonFmaxFwd
  //    //  //  //  && myBackExt.dbgWantNonFmaxFwd
  //    //  //  //  && (
  //    //  //  //    myDualRdBackExt.memAddr === myBackExt.memAddr
  //    //  //  //  ) && (
  //    //  //  //    myDualRdBackExt.rdMemWord =/= myBackExt.rdMemWord
  //    //  //  //  )
  //    //  //  //)
  //    //  //}
  //    //  //when (
  //    //  //  io.dualRdBack.valid
  //    //  //  && myDualRdBackExt.dbgWantNonFmaxFwd
  //    //  //) {
  //    //  //}
  //    //}
  //  }
  //}
  // END: working dualRd?
  //--------
  //--------
  //Builder(myLinkArr.toSeq)
  //--------
}
//trait PipeMemRmwReorderDualRdTypeBase
//[
//  WordT <: Data,
//  HazardCmpT <: Data,
//  //ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
//  //DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT]
//]
////(
////  //wordType: HardType[WordT],
////  //wordCount: Int,
////)
//extends PipeMemRmwPayloadBase[WordT, HazardCmpT] {
//  //--------
//  def getReorderMemAddr(
//    
//  ): Unit
//  //--------
//}

//object PipeMemRmwReorderIo {
//  def apply[
//    WordT <: Data,
//    HazardCmpT <: Data,
//    ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
//    DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
//  ](
//    wordType: HardType[WordT],
//    wordCount: Int,
//    modType: HardType[ModT],
//    modStageCnt: Int,
//    dualRdType: HardType[DualRdT],
//    optEnableModDuplicate: Boolean=true,
//    optEnableClear: Boolean=false,
//    vivadoDebug: Boolean=false,
//  ) /*extends Bundle*/ = {
//    //val clear = (optEnableClear) generate (
//    //  /*slave*/(Flow(
//    //    UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
//    //  ))
//    //)
//    PipeMemRmwIo[
//      WordT,
//      HazardCmpT,
//      ModT,
//      DualRdT
//    ](
//      wordType=wordType(),
//      wordCount=wordCount,
//      modType=modType(),
//      modStageCnt=modStageCnt,
//      dualRdType=dualRdType(),
//      optDualRd=true,
//      optReorder=true,
//      optEnableModDuplicate=optEnableModDuplicate,
//      optEnableClear=optEnableClear,
//      vivadoDebug=vivadoDebug,
//    )
//    //val front = Node()
//    //val frontPayload = Payload(modType())
//    //val modFront = Node()
//    //val modFrontPayload = Payload(modType())
//    //val modBack = Node()
//    //val modBackPayload = Payload(modType())
//    //val midModStages = (
//    //  /*(modStageCnt > 1)*/ optEnableModDuplicate generate (
//    //    /*in*/(
//    //      Vec.fill(
//    //        modStageCnt //- 1 //- 2
//    //      )(modType())
//    //    )
//    //  )
//    //)
//    //val back = Node()
//    //val backPayload = Payload(modType())
//    //val dualRdFront = Node()
//    //val dualRdFrontPayload = Payload(dualRdType())
//    //val dualRdBack = Node()
//    //val dualRdBackPayload = Payload(dualRdType())
//  }
//}

//case class PipeRegFileIoWrPayload[
//  WordT <: Data
//](
//  wordType: HardType[WordT],
//  wordCount: Int,
//) extends Bundle {
//}
//case class PipeRegFileRmwIo[
//  WordT <: Data,
//  //HazardCmpT <: Data,
//](
//  wordType: HardType[WordT],
//  wordCount: Int,
//  numRdPorts: Int,
//) extends Area {
//  //val rWr = Reg(Flow(UInt(
//}
//case class PipeRegFileRmw[
//  WordT <: Data
//](
//  wordType: HardType[WordT],
//  wordCount: Int,
//  numRdPorts: Int,
//) extends Area {
//}

//class PipeMemRmwReorderDualRdExtBase
////[
////  //WordT <: Data,
////  //HazardCmpT <: Data,
////]
//(
//  //wordType: HardType[WordT],
//  wordCount: Int,
//  //hazardCmpType
//) extends Bundle {
//  //--------
//  //val head = UInt((PipeMemRmw.addrWidth(wordCount=wordCount) + 1) bits)
//  //val tail = UInt((PipeMemRmw.addrWidth(wordCount=wordCount) + 1) bits)
//  //--------
//}

//case class PipeMemRmwReorderDualRdType[
//  WordT <: Data,
//  HazardCmpT <: Data,
//  DualRdExtT <: Data,
//](
//  wordType: HardType[WordT],
//  wordCount: Int,
//  hazardCmpType: HardType[HazardCmpT],
//  modStageCnt: Int,
//  dualRdExtType: HardType[DualRdExtT],
//) extends Bundle with PipeMemRmwPayloadBase[WordT, HazardCmpT] {
//  //--------
//  val myDualRdExt = dualRdExtType()
//  //--------
//  val myExt = PipeMemRmwPayloadExt(
//    wordType=wordType(),
//    wordCount=wordCount,
//    hazardCmpType=hazardCmpType(),
//    modStageCnt=modStageCnt,
//  )
//  //--------
//  def setPipeMemRmwExt(
//    ext: PipeMemRmwPayloadExt[WordT, HazardCmpT],
//    memArrIdx: Int,
//  ): Unit = {
//    myExt := ext
//  }
//  def getPipeMemRmwExt(
//    ext: PipeMemRmwPayloadExt[WordT, HazardCmpT],
//    memArrIdx: Int,
//  ): Unit = {
//    ext := myExt
//  }
//  //--------
//}
// a generic pipelined reorder buffer, for use with OoOE CPUs and OoO buses
// (such as Tilelink)
//case class PipeMemRmwReorder[
//  WordT <: Data,
//  HazardCmpT <: Data,
//  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
//  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
//](
//  wordType: HardType[WordT],
//  wordCount: Int,
//  hazardCmpType: HardType[HazardCmpT],
//  modType: HardType[ModT],
//  modStageCnt: Int,
//  pipeName: String,
//  dualRdType: HardType[DualRdT],
//  linkArr: Option[ArrayBuffer[Link]]=None,
//  memArrIdx: Int=0,
//  init: Option[Seq[WordT]]=None,
//  initBigInt: Option[Seq[BigInt]]=None,
//  optEnableModDuplicate: Boolean=true,
//  optEnableClear: Boolean=false,
//  memRamStyle: String="auto",
//  vivadoDebug: Boolean=false,
//) extends Component {
//  //--------
//  val io = PipeMemRmwReorderIo[
//    WordT,
//    HazardCmpT,
//    ModT,
//    DualRdT,
//  ](
//    wordType=wordType(),
//    wordCount=wordCount,
//    modType=modType,
//    modStageCnt=modStageCnt,
//    dualRdType=dualRdType(),
//    optEnableModDuplicate=optEnableModDuplicate,
//    optEnableClear=optEnableClear,
//    vivadoDebug=vivadoDebug
//  )
//  //--------
//  //--------
//}
case class SamplePipeMemRmwModType[
  WordT <: Data,
  HazardCmpT <: Data,
](
  //wordWidth: Int,
  wordType: HardType[WordT],
  wordCount: Int,
  hazardCmpType: HardType[HazardCmpT],
  modRdPortCnt: Int,
  modStageCnt: Int,
  optModHazardKind: Int,
  //doModInModFront: Boolean/*=false*/,
  optReorder: Boolean=false,
) extends Bundle with PipeMemRmwPayloadBase[WordT, HazardCmpT] {
  //--------
  val myExt = PipeMemRmwPayloadExt(
    wordType=wordType(),
    wordCount=wordCount,
    hazardCmpType=hazardCmpType(),
    modRdPortCnt=modRdPortCnt,
    modStageCnt=modStageCnt,
    optModHazardKind=optModHazardKind,
    //doModInModFront=doModInModFront,
    optReorder=optReorder,
  )
  //--------
  def setPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    memArrIdx: Int,
  ): Unit = {
    myExt := ext
  }
  def getPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    memArrIdx: Int,
  ): Unit = {
    ext := myExt
  }
  //--------
}
//object PipeMemRmwToVerilog extends App {
//  //--------
//  def wordWidth = 8
//  def wordType() = UInt(wordWidth bits)
//  def wordCount = 4
//  def hazardCmpType() = UInt(
//    PipeMemRmw.addrWidth(wordCount=wordCount) bits
//  )
//  def modStageCnt = 1
//  //--------
//  Config.spinal.generateVerilog(
//    PipeMemRmw[
//      UInt,
//      UInt,
//      SamplePipeMemRmwModType[UInt, UInt],
//      PipeMemRmwDualRdTypeDisabled[UInt, UInt],
//    ](
//      wordType=wordType(),
//      wordCount=wordCount,
//      hazardCmpType=hazardCmpType(),
//      modType=SamplePipeMemRmwModType[UInt, UInt](
//        wordType=wordType(),
//        wordCount=wordCount,
//        hazardCmpType=hazardCmpType(),
//        modStageCnt=modStageCnt,
//      ),
//      modStageCnt=modStageCnt,
//      linkArr=None,
//    )(
//      doHazardCmpFunc=None
//    )
//  )
//  //--------
//}
//--------

//case class PipeMemTestFrontPayload
////[
////  T <: Data
////]
//(
//  //wordType: HardType[T],
//  wordWidth: Int,
//  wordCount: Int,
//  //memCount: Int,
//) extends Bundle {
//  //val addr = DualTypeNumVec2(
//  //  dataTypeX=UInt(log2Up(wordCount) bits),
//  //  dataTypeY=UInt(log2Up(memCount) bits),
//  //)
//  val addr = UInt(log2Up(wordCount) bits)
//  val data = UInt(wordWidth bits) ////wordType()
//}
////case class PipeMemRd[
////  T <: Data
////](
////  wordType: HardType[T],
////  wordCount: Int,
////) extends Bundle with IMasterSlave {
////  val addr = in UInt(log2Up(wordCount) bits)
////  val data = out(wordType())
////
////  def asMaster(): Unit = {
////    out(addr)
////    in(data)
////  }
////}
//case class PipeMemTestBackPayload(
//  wordWidth: Int,
//  wordCount: Int,
//  //memCount: Int,
//  debug: Boolean=false,
//) extends Bundle {
//  val dbgFront = (debug) generate PipeMemTestFrontPayload(
//    wordWidth=wordWidth,
//    wordCount=wordCount,
//  )
//  val dbgRd = (debug) generate PipeMemTest.wordType()
//  //val dbgMemReadSync = (debug) generate PipeMemTest.wordType()
//  val sum = (
//    //UInt((wordWidth + log2Up(memCount)) bits)
//    //UInt((wordWidth + 1) bits)
//    UInt((wordWidth) bits)
//  )
//  
//}
//
//case class PipeMemTestIo
////[
////  T <: Data
////]
//(
//  //wordType: HardType[T],
//  wordCount: Int,
//  //memCount: Int,
//  debug: Boolean=false,
//) extends Bundle /*with IMasterSlave*/ {
//
//  def wordWidth = PipeMemTest.wordWidth
//  def wordType() = PipeMemTest.wordType()
//
//  val front = slave(Stream(
//    PipeMemTestFrontPayload(
//      //wordType=wordType(),
//      wordWidth=wordWidth,
//      wordCount=wordCount,
//      //memCount=memCount,
//    )
//  ))
//  val back = master(Stream(
//    PipeMemTestBackPayload(
//      wordWidth=wordWidth,
//      wordCount=wordCount,
//      //memCount=memCount,
//      debug=debug,
//    )
//  ))
//
//  //def asMaster(): Unit = {
//  //  master(front)
//  //}
//}
//
//object PipeMemTest {
//  def wordWidth = 8
//  def wordType() = UInt(wordWidth bits)
//}
//
//case class PipeMemTest
////[
////  T <: Data
////]
//(
//  //wordType: HardType[T],
//  //wordWidth: Int,
//  wordCount: Int,
//  //memCount: Int,
//) extends Component {
//  //--------
//  def debug: Boolean = {
//    GenerationFlags.formal {
//      return true
//    }
//    return false
//  }
//  //--------
//  val io = PipeMemTestIo(
//    //wordType=wordType(),
//    //wordWidth=wordWidth,
//    wordCount=wordCount,
//    //memCount=memCount,
//    debug=debug
//  )
//  //val memArr = Array.fill(memCount)(
//  //  Mem(
//  //    wordType=PipeMemTest.wordType(),
//  //    wordCount=wordCount,
//  //  )
//  //)
//  val mem = Mem(
//    wordType=PipeMemTest.wordType(),
//    wordCount=wordCount,
//  )
//  //val formal = new Area {
//  //  val vec = Vec.fill(wordCount)(PipeMemTest.wordType())
//  //}
//  //--------
//  val linkArr = PipeHelper.mkLinkArr()
//  val pipe = PipeHelper(linkArr=linkArr)
//  //case class PipePayload() {
//  //  //--------
//  //  val front = Payload(PipeMemTestFrontPayload(
//  //    wordWidth=PipeMemTest.wordWidth,
//  //    wordCount=wordCount,
//  //    //memCount=memCount,
//  //  ))
//
//  //  //val rd = Vec.fill(memCount)(PipeMemTest.wordType())
//  //  val rd = Payload(PipeMemTest.wordType())
//
//  //  val back = Payload(PipeMemTestBackPayload(
//  //    wordWidth=PipeMemTest.wordWidth,
//  //    wordCount=wordCount,
//  //    //memCount=memCount,
//  //  ))
//  //  //--------
//  //}
//  //val pipePayload = (
//  //  //Payload(PipePayload())
//  //  PipePayload()
//  //)
//  val pipePayload = new Area {
//    //--------
//    def mkFront() = PipeMemTestFrontPayload(
//      wordWidth=PipeMemTest.wordWidth,
//      wordCount=wordCount,
//      //memCount=memCount,
//    )
//    val front = Payload(mkFront())
//
//    //val rd = Vec.fill(memCount)(PipeMemTest.wordType())
//    def mkRd() = PipeMemTest.wordType()
//    val rd = Payload(mkRd())
//
//    def mkBack() = PipeMemTestBackPayload(
//      wordWidth=PipeMemTest.wordWidth,
//      wordCount=wordCount,
//      //memCount=memCount,
//      debug=debug,
//    )
//    val back = Payload(mkBack())
//    val dbgMemReadSync = (debug) generate Payload(PipeMemTest.wordType())
//  }
//
//  //def printSize(): Unit = {
//  //  println({
//  //    val tempSize = pipe.cArr.size
//  //    f"$tempSize"
//  //  })
//  //}
//
//
//  //val cArr = new ArrayBuffer[CtrlLink]()
//  def cArr = pipe.cArr
//  val cFront = pipe.addStage("Front")
//  //printSize()
//
//  //for (stageIdx <- 0 until memCount) {
//  //  cArr += pipe.addStage()
//  //}
//  //def frontPipePayload = cFront.down(pipePayload)
//
//  val cSum = pipe.addStage("Sum")
//  //printSize()
//  //def wrPipePayload = cSum.down(pipePayload)
//
//  val cBack = pipe.addStage("Back")
//  //printSize()
//  //val testificate = pipe.addStage()
//  //def backPipePayload = cBack.down(pipePayload)
//  val cLast = pipe.addStage(
//    name=pipeName + "_Last",
//    finish=true,
//  )
//
//  val rdValid = (
//    //Array.fill(cArr.size - 1)(Payload(Bool()))
//    Payload(Bool())
//  )
//  //val frontDuplicateIt = (
//  //  Payload(Bool())
//  //)
//
//  //GenerationFlags.formal {
//  //  when (pastValidAfterReset) {
//  //    io.front.formalAssumesSlave(payloadInvariance=false)
//  //    io.back.formalAssertsMaster(payloadInvariance=false)
//  //  }
//  //}
//
//  pipe.first.up.driveFrom(io.front)(
//    con=(node, payload) => {
//      node(pipePayload.front) := payload
//      //node(pipePayload).rd := node(pipePayload).rd.getZero
//      //node(pipePayload).back := node(pipePayload).back.getZero
//    }
//  )
//  val tempBackStm = cloneOf(io.back)
//  pipe.last.down.driveTo(tempBackStm)(
//    con=(payload, node) => {
//      payload := node(pipePayload.back)
//    }
//  )
//  io.back << tempBackStm.haltWhen(
//    !(RegNextWhen(True, io.front.fire) init(False))
//  )
//  //--------
//  val cFrontArea = new cFront.Area {
//    //throwWhen(
//    //  !(RegNextWhen(True, io.front.fire) init(False))
//    //)
//    //--------
//    //def upFront = up(pipePayload.front)
//    //def upRd = up(pipePayload.rd)
//    //def upBack = up(pipePayload.back)
//    //def upRdValid = up(rdValid)
//
//    ////def downPipePayload = down(pipePayload)
//    //def downFront = down(pipePayload.front)
//    //def downRd = down(pipePayload.rd)
//    //def downBack = down(pipePayload.back)
//    //def downRdValid = down(rdValid)
//    //--------
//    //when (isValid) {
//      //switch (pipePayload.front.addr.y) {
//      //  for (memIdx <- 0 until memArr.size) {
//      //    is (memIdx) {
//      //      memArr(memIdx).write(
//      //        address=pipePayload.front.addr.x,
//      //        data=pipePayload.front.data,
//      //      )
//      //    }
//      //  }
//      //  default {
//      //  }
//      //}
//      //def myPipePayload = cFront.down(pipePayload)
//      //def front = myPipePayload.front
//      //def rd = pipePayload.rd
//      //mem.write(
//      //  address=front.addr,
//      //  data=front.data,
//      //)
//      val tempRdValid = (
//        cSum.up(pipePayload.front).addr =/= up(pipePayload.front).addr
//      )
//      val myUpRdValid = Bool()
//      val rPrevMyUpRdValid = (
//        //RegNextWhen(myUpRdValid, up.isFiring) init(False)
//        RegNext(myUpRdValid) init(False)
//      )
//      //--------
//      //val nextFlagUpRdValid = Bool()
//      //val rFlagUpRdValid = RegNext(nextFlagUpRdValid) init(False)
//      //--------
//      //val rUpRdValid = (
//      //  //RegNextWhen(myUpRdValid, up.isFiring) init(False)
//      //)
//      //val rSavedUpRdValid = Reg(Bool()) init(True)
//      //val rPastUpRdValid = RegNext(upRdValid) init(True)
//      //haltWhen(!downRdValid)
//      //terminateWhen(!downRdValid)
//
//      //upRdValid := rPastUpRdValid
//      //upRdValid := RegNext(upRdValid) init(True)
//      //when (isValid) {
//      //  myUpRdValid := tempRdValid
//      //} otherwise {
//      //  upRdValid := RegNext(upRdValid) init(upRdValid.getZero)
//      //}
//      myUpRdValid := rPrevMyUpRdValid
//      //--------
//      //when (isValid) {
//      //  when (tempRdValid) {
//      //    myUpRdValid := True
//      //    //nextFlagUpRdValid := False
//      //  } otherwise { // when (!tempRdValid)
//      //    //myUpRdValid
//      //    //val rSavedTempRdValid = (
//      //    //  RegNextWhen(tempRdValid, up.isFiring) init(False)
//      //    //)
//      //    //when (up.isFiring) {
//      //    //}
//      //    //--------
//      //    //when (
//      //    //  //up.isFiring
//      //    //  //up.isReady
//      //    //  up.isReady
//      //    //  //down.isFiring
//      //    //) {
//      //    //  //when (!rFlagUpRdValid) {
//      //    //  //  rFlagUpRdValid := True
//      //    //  //  //myUpRdValid := 
//      //    //  //}
//      //    //  //myUpRdValid := True
//      //    //  nextFlagUpRdValid := !rFlagUpRdValid
//      //    //  myUpRdValid := nextFlagUpRdValid
//      //    //} otherwise { // when (!up.isFiring)
//      //    //  nextFlagUpRdValid := rFlagUpRdValid
//      //    //  myUpRdValid := RegNext(myUpRdValid) init(myUpRdValid.getZero)
//      //    //}
//      //    when (
//      //      //cSum.up.isFiring
//      //      up.isFiring
//      //    ) {
//      //      //--------
//      //      //nextFlagUpRdValid := True
//      //      //myUpRdValid := True
//      //      //--------
//      //      //myUpRdValid := False
//      //      //--------
//      //      //when (!rPrevMyUpRdValid) {
//      //      //  myUpRdValid
//      //      //}
//      //      myUpRdValid := !rPrevMyUpRdValid
//      //    }
//      //    //otherwise {
//      //    //  myUpRdValid := rPrevMyUpRdValid
//      //    //}
//      //    //when (rPrevMyUpRdValid) {
//      //      //duplicateIt()
//      //    //}
//      //    //--------
//      //  }
//      //}
//      //--------
//      //otherwise {
//      //  myUpRdValid := rPrevMyUpRdValid
//      //}
//
//      when (isValid) {
//        up(rdValid) := (
//          //tempRdValid
//          //myUpRdValid
//          //rPrevMyUpRdValid
//          myUpRdValid
//        )
//      } otherwise {
//        up(rdValid) := rPrevMyUpRdValid
//      }
//      //val rDuplicateIt = Reg(Bool()) init(False)
//      //when (
//      //  //!up(rdValid)
//      //  //!rPrevMyUpRdValid
//      //  !myUpRdValid
//      //) {
//      //  when (up.isFiring) {
//      //    rDuplicateIt := True
//      //    //duplicateIt()
//      //  }
//      //  when (rDuplicateIt) {
//      //    duplicateIt()
//      //    rDuplicateIt := False
//      //  }
//      //  //when (!down.isFiring) {
//      //  //  duplicateIt()
//      //  //}
//      //}
//      //--------
//      //val rPrevTempRdValid = (
//      //  RegNextWhen(tempRdValid, up.isFiring) init(tempRdValid.getZero)
//      //)
//      //val prevTempRdValidAddrs = new Area {
//      //  def mkAddr() = (
//      //    UInt(log2Up(wordCount) bits)
//      //  )
//      //  val rUpAddr = Reg(mkAddr()) init(0x0)
//      //  val rSumUpAddr = Reg(mkAddr()) init(0x0)
//      //}
//
//      val nextDuplicateIt = Bool()
//      val rDuplicateIt = (
//        RegNext(nextDuplicateIt) init(nextDuplicateIt.getZero)
//      )
//      nextDuplicateIt := rDuplicateIt
//      //up(frontDuplicateIt) := nextDuplicateIt
//
//      //when (up.isValid) {
//      //  //when (
//      //  //  tempRdValid
//      //  //  //&& rPrevTempRdValid
//      //  //  && !rDidDuplicateIt
//      //  //) {
//      //  //  duplicateIt()
//      //  //  rDidDuplicateIt := True
//      //  //} otherwise {
//      //  //}
//      //}
//      //when (isValid) {
//        //--------
//        when (
//          !rDuplicateIt
//          //!cSum.up(frontDuplicateIt)
//          //!up(frontDuplicateIt)
//        ) {
//          myUpRdValid := tempRdValid
//          when (!tempRdValid) {
//            //nextDuplicateIt := True
//            nextDuplicateIt := True
//            duplicateIt()
//          }
//        } otherwise {
//          //nextDuplicateIt := False
//          //myUpRdValid := False
//          //duplicateIt()
//          myUpRdValid := True
//          when (up.isFiring) {
//            nextDuplicateIt := False
//            //nextDuplicateIt := False
//            //myUpRdValid := False
//            //myUpRdValid := True
//            //myUpRdValid := True
//          } 
//          //otherwise {
//          //  //myUpRdValid := False
//          //}
//          //otherwise {
//          //  duplicateIt()
//          //}
//        }
//        //--------
//      //}
//      //--------
//
//      //duplicateWhen(
//      //  //!cSum.up(rdValid)
//      //  //!up(rdValid)
//      //  //!down(rdValid)
//      //  //!rPastUpRdValid
//      //  //!upRdValid
//      //  //!down(rdValid)
//      //  //!cSum.up(rdValid)
//      //  //!down(rdValid)
//      //  //!rSavedUpRdValid
//      //  //!down(rdValid)
//      //  //!up.isFiring
//      //  !upRdValid
//      //)
//      //--------
//      //when (!myUpRdValid) {
//      //  //when (
//      //  //  //!down.isReady
//      //  //  //!RegNext(down.isFiring)
//      //  //  //!down.isFiring
//      //  //  //RegNext(up.isFiring)
//      //  //  True
//      //  //) {
//      //    duplicateIt()
//      //  //}
//      //}
//      //--------
//      //when (
//      //  cBack.up(pipePayload.front).addr
//      //  === down(pipePayload.front).addr
//      //) {
//      //  up(pipePayload.rd) := (
//      //    cBack.up(pipePayload.back).sum
//      //  )
//      //} otherwise {
//      //  up(pipePayload.rd) := (
//      //    //mem.readSync
//      //    mem.readAsync
//      //    (
//      //      address=up(pipePayload.front).addr,
//      //    )
//      //  )
//      //}
//      val upRd = pipePayload.mkRd()
//      when (
//        isValid
//        && myUpRdValid
//      ) {
//        when (
//          //cBack.up.isFiring
//          //&& (
//            cBack.up(pipePayload.front).addr
//            === up(pipePayload.front).addr
//          //)
//        ) {
//          upRd := (
//            cBack.up(pipePayload.back).sum
//          )
//        } otherwise {
//          upRd := (
//            mem.readSync
//            //mem.readAsync
//            (
//              address=up(pipePayload.front).addr,
//            )
//          )
//        }
//      } otherwise {
//        upRd := RegNext(upRd) init(upRd.getZero)
//      }
//      up(pipePayload.rd) := upRd
//      val rTempCnt = (debug) generate (
//        Reg(UInt(8 bits)) init(0x0)
//      )
//
//      //val rHadTempRdValid = (debug) generate (
//      //  Reg(Bool()) init(False)
//      //)
//      //if (debug) {
//      //  when (tempRdValid) {
//      //    rHadTempRdValid := True
//      //  }
//      //}
//      //when (cSum.up.isFiring) {
//      //  upRdValid := True
//      //}
//      //when (tempRdValid) {
//      //} otherwise {
//      //}
//
//      //when (up.isFiring) {
//      //  //when (
//      //  //  //!rPastUpRdValid
//      //  //  !rSavedUpRdValid
//      //  //  //&& !downRdValid
//      //  //  //!down(rdValid)
//      //  //) {
//      //  //  upRdValid := True
//      //  //  //rSavedUpRdValid := True
//
//      //  //  ////terminateIt() // clear down.valid
//      //  //  //haltIt()
//      //  //  ////throwIt()
//      //  //  ////downRdValid := (
//      //  //  ////  
//      //  //  ////)
//      //  //} otherwise {
//      //  //  upRdValid := tempRdValid
//      //  //  //rSavedUpRdValid := tempRdValid
//      //  //}
//      //  //when (
//      //  //  !rSavedUpRdValid
//      //  //) {
//      //  //  upRdValid := True
//      //  //  rSavedUpRdValid := True
//      //  //} otherwise {
//      //  //}
//      //  //when (
//      //  //  !rSavedUpRdValid
//      //  //) {
//      //  //} otherwise {
//      //  //}
//      //  when (!down(rdValid)) {
//      //    upRdValid := True
//      //  } otherwise {
//      //    upRdValid := tempRdValid
//      //  }
//      //} otherwise {
//      //  upRdValid := False
//      //}
//      //elsewhen (
//      //  down.isFiring
//      //) {
//      //}
//
//      {
//        if (debug) {
//          when (
//            //down.isFiring
//            ////&& tempRdValid
//            //&& 
//            //up(rdValid)
//            //tempRdValid
//            //|| past(tempRdValid)
//            //&& rHadTempRdValid
//            //&& 
//            myUpRdValid
//            && up(pipePayload.front).data > 0
//          ) {
//            //rHadTempRdValid := False
//            rTempCnt := rTempCnt + 1
//          }
//        }
//        //{
//        //  //haltWhen(
//        //  //  //(
//        //  //  //  front.addr === wrPipePayload.front.addr
//        //  //  //) || (
//        //  //  //  front.addr === backPipePayload.front.addr
//        //  //  //)
//        //  //  //!wrPipePayload.rd.valid
//        //  //  //!rd.valid
//        //  //  !cSum.up(rdValid)
//        //  //  //|| !cBack.up(rdValid)
//        //  //)
//        //  //when (cSum.up.valid) {
//        //  //}
//        //}
//      }
//    //}
//    val rIsFiringCnt = (debug) generate (
//      Reg(UInt(8 bits)) init(0x0)
//    )
//    val rMyUpRdValidDelVec = (debug) generate Vec.fill(8)(
//      Reg(Bool()) init(False)
//    )
//    //--------
//    GenerationFlags.formal {
//      when (up.isFiring) {
//        for (idx <- 0 until rMyUpRdValidDelVec.size) {
//          def tempUpRdValid = rMyUpRdValidDelVec(idx)
//          if (idx == 0) {
//            tempUpRdValid := myUpRdValid
//          } else {
//            tempUpRdValid := rMyUpRdValidDelVec(idx - 1)
//          }
//        }
//      }
//      //val rMyUpRdValidDel1 = (
//      //  RegNextWhen(myUpRdValid, up.isFiring)
//      //  init(myUpRdValid.getZero)
//      //)
//      //val rMyUpRdValidDel2 = (
//      //  RegNextWhen(rMyUpRdValidDel1, up.isFiring)
//      //  init(myUpRdValid.getZero)
//      //)
//      //val rMyUpRdValidDel3 = (
//      //  RegNextWhen(rMyUpRdValidDel2, up.isFiring)
//      //  init(myUpRdValid.getZero)
//      //)
//      //cover(
//      //  (
//      //    RegNextWhen(True, io.back.fire) init(False)
//      //  )
//      //  //&& myUpRdValid
//      //  //&& !rMyUpRdValidDel1
//      //  //&& rMyUpRdValidDel2
//      //  //&& !rMyUpRdValidDel1
//      //)
//      //cover(
//      //  !(
//      //    RegNextWhen(myUpRdValid, up.isFiring) init(myUpRdValid.getZero)
//      //  )
//      //  && myUpRdValid
//      //  && !rMyUpRdValidDelVec(0)
//      //  && rMyUpRdValidDelVec(1)
//      //  && !rMyUpRdValidDelVec(2)
//      //  && rMyUpRdValidDelVec(3)
//      //  && !rMyUpRdValidDelVec(4)
//      //)
//      val myDbgMemReadSync = PipeMemTest.wordType()
//      when (up.isValid) {
//        myDbgMemReadSync := (
//          mem.readSync
//          //mem.readAsync
//          (
//            address=up(pipePayload.front).addr,
//          )
//        )
//      } otherwise {
//        myDbgMemReadSync := (
//          RegNext(myDbgMemReadSync) init(myDbgMemReadSync.getZero)
//        )
//      }
//      up(pipePayload.dbgMemReadSync) := myDbgMemReadSync
//      //--------
//      //assumeInitial(
//      //  down(pipePayload.rd) === 0x0
//      //)
//      //assumeInitial(downRdValid === downRdValid.getZero)
//      //--------
//      //cover(
//      //  cSum.down(rdValid)
//      //  && Mux[Bool](
//      //    pastValidAfterReset,
//      //    !past(cSum.down(rdValid)),
//      //    True
//      //  ) && Mux[Bool](
//      //    past(pastValidAfterReset),
//      //    past(past(cSum.down(rdValid))),
//      //    True
//      //  )
//      //)
//      //val rCnt = Reg(UInt(8 bits)) init(0x0)
//      //--------
//      //--------
//      when (up.isFiring) {
//        rIsFiringCnt := rIsFiringCnt + 1
//      }
//      when (pastValidAfterReset) {
//        //when (
//        //  past(up.isFiring)
//        //) {
//        //  when (
//        //    past(cBack.up(pipePayload.front).addr)
//        //    === cSum.up(pipePayload.front).addr
//        //  ) {
//        //    assert(
//        //      past(cSum.up(pipePayload.rd))
//        //      === past(cBack.up(pipePayload.rd))
//        //    )
//        //  } otherwise {
//        //    assert(
//        //      cSum.up(pipePayload.rd) === past(
//        //        mem.readAsync(
//        //          address=down(pipePayload.front).addr
//        //        )
//        //      )
//        //    )
//        //  }
//        //}
//        //when (past(cSum.up.isFiring)) {
//        //  when (
//        //    cBack.up
//        //  ) {
//        //  } otherwise {
//        //  }
//        //}
//        val rPrevCSumFront = Reg(pipePayload.mkFront())
//        rPrevCSumFront.init(rPrevCSumFront.getZero)
//        val rPrevCBackFront = Reg(pipePayload.mkFront())
//        rPrevCBackFront.init(rPrevCBackFront.getZero)
//        //when (cSum.up.isFiring) {
//        //}
//
//        when (
//          //past(cSum.up.isFiring)
//          //&& past(cBack.up.isFiring)
//          //&& 
//          //myUpRdValid
//          up.isValid
//          && myUpRdValid
//          && cBack.up.isValid
//          && cBack.up(rdValid)
//          //&& (
//          //  (RegNextWhen(True, io.front.fire) init(False))
//          //  || io.front.fire
//          //) && (
//          //  (RegNextWhen(True, cFront.up.isFiring) init(False))
//          //  || cFront.up.isFiring
//          //) && (
//          //  (RegNextWhen(True, cSum.up.isFiring) init(False))
//          //  || cSum.up.isFiring
//          //) && (
//          //  (RegNextWhen(True, cBack.up.isFiring) init(False))
//          //  || cBack.up.isFiring
//          //) && (
//          //  //(RegNextWhen(True, io.back.fire) init(False))
//          //  //|| io.back.fire
//          //  True
//          //)
//        ) {
//          when (
//            //cSum.up(pipePayload.front).addr
//            cFront.up(pipePayload.front).addr
//            === cBack.up(pipePayload.front).addr
//          ) {
//            assert(
//              //cSum.up(pipePayload.rd)
//              //=== cBack.up(pipePayload.back).sum
//              //past(cFront.down(pipePayload.rd))
//              //=== past(cSum.down(pipePayload.back).sum)
//              (
//                //RegNextWhen(
//                //  cSum.up(pipePayload.rd), cSum.up.isFiring
//                //) init(cSum.up(pipePayload.rd).getZero)
//                //cBack.up(pipePayload.rd)
//                //cSum.down(pipePayload.rd)
//                cFront.down(pipePayload.rd)
//              )
//              === (
//                cBack.up(pipePayload.back).sum
//              )
//            )
//          } otherwise {
//            //assert(
//            //  //cFront.down(pipePayload.rd)
//            //  //=== cBack.up(pipePayload.dbgMemReadSync)
//            //  cFront.down(pipePayload.rd)
//            //  === myDbgMemReadSync
//            //)
//          }
//        }
//      }
//      //cover(
//      //  //up.isFiring
//      //  //&& 
//      //  //rTempCnt === 1
//      //  //rIsFiringCnt === 2
//      //  rIsFiringCnt >= 5
//      //  && rTempCnt >= 5
//      //  //&& rTempCnt > 3
//      //)
//    }
//    //--------
//  }
//  //--------
//  val cSumArea = new cSum.Area {
//    //--------
//    //haltWhen(
//    //  !(RegNextWhen(True, io.front.fire) init(False))
//    //)
//    //val temp = up(rdValid)
//
//    terminateWhen(
//      //!pipe.nArr(1)(rdValid)
//      //temp
//      !up(rdValid)
//    )
//    //--------
//    //val rDuplicateIt = Reg(Bool()) init(False)
//    //when (
//    //  !up(rdValid)
//    //) {
//    //  when (down.isFiring) {
//    //    rDuplicateIt := True
//    //    //duplicateIt()
//    //  }
//    //  when (rDuplicateIt) {
//    //    duplicateIt()
//    //    rDuplicateIt := False
//    //  }
//    //  //when (!down.isFiring) {
//    //  //  duplicateIt()
//    //  //}
//    //}
//    ////duplicateWhen(
//    ////  !up(rdValid)
//    ////  && 
//    ////)
//    //--------
//
//    //when (isValid) {
//    //  def front = pipePayload.front
//    //  def rd = pipePayload.rd
//    //  def back = pipePayload.back
//    //  //back
//    //  back.sum := 
//    //}
//    //val rReadyCnt = Reg(SInt(4 bits)) init(0x0)
//    //def upPipePayload = up(pipePayload)
//    def upFront = up(pipePayload.front)
//    def upRd = up(pipePayload.rd)
//    //def upBack = up(pipePayload.back)
//    val upBack = pipePayload.mkBack()
//    //def upRdValid = up(rdValid)
//
//    ////def downPipePayload = down(pipePayload)
//    //def downFront = down(pipePayload.front)
//    //def downRd = down(pipePayload.rd)
//    //def downBack = down(pipePayload.back)
//    ////def downRdValid = down(rdValid)
//
//    //when (
//    //  isValid
//    //  //&& !myRdValid
//    //) {
//    //}
//    //downRdValid := downFront.addr =/= upFront.addr
//    //val rPastDownRdValid = RegNext(downRdValid) init(False)
//    ////haltWhen(!downRdValid)
//    //terminateWhen(!downRdValid)
//    //when (
//    //  rPastDownRdValid
//    //  //&& !downRdValid
//    //) {
//    //  downRdValid := downFront.addr =/= upFront.addr
//
//    //  ////terminateIt() // clear down.valid
//    //  //haltIt()
//    //  ////throwIt()
//    //  ////downRdValid := (
//    //  ////  
//    //  ////)
//    //} otherwise {
//    //  downRdValid := True
//    //}
//    //throwWhen(!downRdValid)
//    val tempSum = up(pipePayload.rd) + up(pipePayload.front).data
//    when (
//      up.isValid
//      && up(rdValid)
//      //downRdValid
//    ) {
//      //up(pipePayload.back).sum 
//      upBack.sum := (
//        tempSum
//      )
//    } otherwise {
//      //up(pipePayload.back).sum 
//      upBack.sum := (
//        RegNext(upBack.sum) init(upBack.sum.getZero)
//        //upRd
//      )
//    }
//    up(pipePayload.back) := upBack
//    //val rDidFirstFire = Reg(Bool()) init(False)
//    val rDidFirstFire = (debug) generate (
//      RegNextWhen(True, up.isFiring) init(False)
//    )
//    //val rPastIsFiring = Reg(Bool()) init(False)
//
//    //rPastIsFiring := down.isFiring
//
//    //val rPrevUpFront = (
//    //  Reg(pipePayload.mkFront()) init(pipePayload.mkFront().getZero)
//    //)
//    //val rPrevDownFront = (
//    //  Reg(pipePayload.mkFront()) init(pipePayload.mkFront().getZero)
//    //)
//    //val rPrevUpRd = (
//    //  Reg(pipePayload.mkRd()) init(pipePayload.mkRd().getZero)
//    //)
//    //val rPrevDownRd = (
//    //  Reg(pipePayload.mkRd()) init(pipePayload.mkRd().getZero)
//    //)
//    //val rPrevUpBack = (
//    //  Reg(pipePayload.mkBack()) init(pipePayload.mkBack().getZero)
//    //)
//    ////val rPrevDownBack = (
//    ////  Reg(pipePayload.mkBack()) init(pipePayload.mkBack().getZero)
//    ////)
//    ////val rPrevDownRdValid = (
//    ////  Reg(Bool()) init(downRdValid.getZero)
//    ////)
//    //--------
//    GenerationFlags.formal {
//      //--------
//      if (debug) {
//        upBack.dbgFront := up(pipePayload.front)
//        upBack.dbgRd := up(pipePayload.rd)
//      }
//      //--------
//      //assumeInitial(downFront === downFront.getZero)
//      //assumeInitial(downRd === downRd.getZero)
//      //assumeInitial(downBack === downBack.getZero)
//      //assumeInitial(downRdValid === downRdValid.getZero)
//      //--------
//
//      when (pastValidAfterReset) {
//        //when (up.isFiring) {
//        //  rDidFirstFire := True
//        //  //rPrevUpFront := up(pipePayload.front)
//        //  ////rPrevDownFront := down(pipePayload.front)
//        //  //rPrevUpRd := up(pipePayload.rd)
//        //  ////rPrevDownRd := down(pipePayload.rd)
//        //  ////rPrevUpBack := up(pipePayload.back)
//        //  ////rPrevDownBack := down(pipePayload.back)
//        //  ////rPrevDownRdValid := down(rdValid)
//        //}
//        //when (rDidFirstFire) {
//          //when (
//          //  //isValid
//          //  past(down.isFiring)
//          //) {
//          //  assert(
//          //    downBack.sum === past(upRd) + past(upFront.data)
//          //  )
//          //} otherwise {
//          //  assert(
//          //    downBack.sum === past(downBack.sum)
//          //  )
//          //}
//          when (past(
//            up.isFiring
//            //up.isValid
//            && up(rdValid)
//          )) {
//            //assert(
//            //  down(pipePayload.back).sum
//            //    === past(up(pipePayload.rd))
//            //    + past(up(pipePayload.front).data)
//            //)
//            assert(
//              //upBack.sum === rPrevUpRd + rPrevUpFront.data
//              //past(
//              //upBack.sum
//              //)
//              past(upBack.sum)
//              === (
//                past(upBack.dbgFront.data)
//                + past(upBack.dbgRd)
//              )
//              //cBack.up(pipePayload.back).sum
//              //=== (
//              //  //upBack.dbgFront.data
//              //  //+ upBack.dbgRd
//              //  //past(tempSum)
//              //  cBack.up(pipePayload.back).dbgFront.data
//              //  + cBack.up(pipePayload.back).dbgRd
//              //)
//            )
//          } otherwise {
//            //assert(stable(downBack.sum))
//          }
//        //}
//      }
//    }
//    val rCoverDiffData = (debug) generate Vec.fill(8)(
//      Reg(PipeMemTest.wordType()) init(0x0)
//    )
//    val rCoverDiffDataCnt = (debug) generate (
//      Reg(UInt(8 bits)) init(0x0)
//    )
//    val rCoverAddr = (debug) generate (
//      Vec.fill(4.min(wordCount))(
//        Reg(UInt(log2Up(wordCount) bits)) init(0x0)
//      )
//    )
//    val rCoverAddrCnt = (debug) generate (
//      Reg(UInt(8 bits)) init(0x0)
//    )
//    val rCoverAddrLeastCnt = (debug) generate (
//      Reg(UInt(8 bits)) init(0x0)
//    )
//    //val rCoverAddrLastIdx = Reg(
//    //  UInt(log2Up(rCoverAddr.size) bits) init(0x0)
//    //) 
//    //val rCoverInvIsFiring = Reg(Bool()) init(False)
//    //val rCoverInvCnt = Reg(UInt(8 bits)) init(0x0)
//    //val rCoverSameIsFiring = Reg(Bool()) init(False)
//    //val rCoverSameCnt = Reg(UInt(8 bits)) init(0x0)
//
//    //--------
//    //case class DbgUp() extends Bundle {
//    //}
//    val rUpRdValidDelVec = (debug) generate (
//      Vec.fill(8)(
//        Reg(Bool()) init(False)
//      )
//    )
//    //--------
//    val myHadFlip = (debug) generate (
//      RegNextWhen(
//        True,
//        (
//          up(rdValid)
//          && !rUpRdValidDelVec(0)
//          && rUpRdValidDelVec(1)
//          && !rUpRdValidDelVec(2)
//          && rUpRdValidDelVec(3)
//        )
//      ) init(False)
//    )
//
//    GenerationFlags.formal {
//      //--------
//      when (up.isFiring) {
//        for (idx <- 0 until rUpRdValidDelVec.size) {
//          def tempUpRdValid = rUpRdValidDelVec(idx)
//          if (idx == 0) {
//            tempUpRdValid := up(rdValid)
//          } else {
//            tempUpRdValid := rUpRdValidDelVec(idx - 1)
//          }
//        }
//      }
//      //--------
//      when (pastValidAfterReset) {
//        //when (
//        //  past(pastValidAfterReset)
//        //) {
//          //--------
//          //when (up.isFiring) {
//          //  rCoverInvIsFiring := True
//          //  rCoverSameIsFiring := True
//          //}
//          //--------
//          //when (
//          //  down(rdValid)
//          //  && !past(down(rdValid))
//          //) {
//          //  //when (down.isFiring /*|| rCoverInvIsFiring*/) {
//          //    //cover(past(down(rdValid)))
//          //    rCoverInvCnt := rCoverInvCnt + 1
//          //    //rCoverInvIsFiring := False
//          //  //}
//          //}
//          when (
//            up.isFiring
//            //&& 
//            //io.back.valid
//            //up.valid
//            && rCoverDiffDataCnt < rCoverDiffData.size
//          ) {
//            val firstSame = rCoverDiffData.sFindFirst(
//              _ === up(pipePayload.front).data
//            )
//            when (!firstSame._1) {
//              rCoverDiffData(rCoverDiffDataCnt.resized) := (
//                up(pipePayload.front).data
//              )
//              rCoverDiffDataCnt := rCoverDiffDataCnt + 1
//            }
//          }
//          when (
//            up.isFiring
//            && rCoverAddrCnt < rCoverAddr.size
//          ) {
//            //--------
//            val leastPlusOne = rCoverAddrLeastCnt + 1
//            //--------
//            //val firstSame = rCoverAddr.sFindFirst(
//            //  _ === up(pipePayload.front).addr
//            //)
//            //when (!firstSame._1) {
//            //  rCoverAddr(rCoverAddrCnt.resized) := (
//            //    up(pipePayload.front).addr
//            //  )
//            //  rCoverAddrCnt := rCoverAddrCnt + 1
//            //}
//            //--------
//            when (!leastPlusOne(1)) {
//              //--------
//              when (
//                rCoverAddrCnt === 0
//                && leastPlusOne === 0
//              ) {
//                rCoverAddrLeastCnt := leastPlusOne
//                rCoverAddr(rCoverAddrCnt.resized) := (
//                  up(pipePayload.front).addr
//                )
//              } elsewhen (
//                rCoverAddr(rCoverAddrCnt.resized)
//                === up(pipePayload.front).addr
//              ) {
//                rCoverAddrLeastCnt := leastPlusOne
//              } otherwise {
//              }
//              //--------
//            } otherwise {
//              //--------
//              rCoverAddrLeastCnt := 0
//              //--------
//              rCoverAddrCnt := rCoverAddrCnt + 1
//              //--------
//              rCoverAddr((rCoverAddrCnt + 1).resized) := (
//                up(pipePayload.front).addr
//              )
//            }
//            //--------
//          }
//          cover(
//            rCoverDiffDataCnt === rCoverDiffData.size
//            && myHadFlip
//            //&& rCoverAddrCnt === rCoverAddr.size
//            //rCoverDiffDataCnt === 1
//          )
//          cover(
//            rCoverDiffDataCnt === rCoverDiffData.size
//            && rCoverAddrCnt === rCoverAddr.size
//            && myHadFlip
//            //--------
//            //up(rdValid)
//            //&& !rUpRdValidDelVec(0)
//            //&& rUpRdValidDelVec(1)
//            //&& !rUpRdValidDelVec(2)
//            //&& rUpRdValidDelVec(3)
//            //--------
//            //&& (RegNextWhen(True, up(rdValid)) init(False))
//            //&& (RegNextWhen(True, !rUpRdValidDelVec(0)) init(False))
//            //&& (RegNextWhen(True, rUpRdValidDelVec(1)) init(False))
//            //&& (RegNextWhen(True, !rUpRdValidDelVec(2)) init(False))
//            //&& (RegNextWhen(True, rUpRdValidDelVec(3)) init(False))
//            //rCoverDiffDataCnt === 1
//          )
//          //--------
//          //val rHadNonZeroData = Vec.fill(2){
//          //  val temp = Reg(Flow(PipeMemTest.wordType()))
//          //  temp.init(temp.getZero)
//          //  temp
//          //}
//
//          //when (
//          //  io.front.fire
//          //  && (
//          //    io.front.data =/= 0
//          //  )
//          //) {
//          //  rHadNonZeroData.valid := True
//          //  rHadNonZeroData.payload := io.front.data
//          //}
//          //cover(
//          //  rHadNonZeroData.valid
//          //  //&& io.back.valid
//          //  //&& io.back.sum === rHadNonZeroData.payload
//          //  && up.valid
//          //  && up(pipePayload.front).data === rHadNonZeroData.payload
//          //)
//
//          //when (
//          //  up(rdValid)
//          //  //&& past(up(rdValid))
//          //  //&& stable(up(pipePayload.front).addr)
//          //  //&& !stable(up(pipePayload.front).data)
//          //) {
//          //  //when (up.isFiring /*|| rCoverSameIsFiring*/) {
//          //    rCoverSameCnt := rCoverSameCnt + 1
//          //  //}
//          //}
//          //cover(rCoverInvCnt === 3)
//          //cover(rCoverSameCnt === 3)
//          //cover(
//          //  //rCoverInvCnt === 3
//          //  //&& 
//          //  rCoverSameCnt === 3
//          //)
//          //cover(!stable(up(rdValid)))
//          //cover(!up(rdValid))
//          //cover(up(rdValid))
//        //}
//      }
//    }
//    //--------
//  }
//  //--------
//  val cBackArea = new cBack.Area {
//    haltWhen(
//      !(RegNextWhen(True, io.front.fire) init(False))
//    )
//    //throwWhen(
//    //  //!pipe.nArr(1)(rdValid)
//    //  //temp
//    //  !up(rdValid)
//    //)
//    //--------
//    //def downFront = down(pipePayload.front)
//    //def downRd = down(pipePayload.rd)
//    //def downBack = down(pipePayload.back)
//    //assumeInitial(downFront === downFront.getZero)
//    //assumeInitial(downRd === downRd.getZero)
//    //assumeInitial(downBack === downBack.getZero)
//    //--------
//    //def downRdValid = down(rdValid)
//
//    //downRdValid := (
//    //  downFront.addr =/= up(pipePayload).front.addr
//    //)
//
//    when (
//      isValid
//      && up(rdValid)
//    ) {
//      //def front = pipePayload.front
//      //def rd = pipePayload.rd
//      //def back = pipePayload.back
//      mem.write(
//        address=up(pipePayload.front).addr,
//        data=up(pipePayload.back).sum,
//      )
//    }
//    //val rCoverInvIsFiringCnt = Reg(UInt(8 bits)) init(0x0)
//    //val rCoverDiffData = Vec.fill(8)(
//    //  Reg(PipeMemTest.wordType()) init(0x0)
//    //)
//    //val rCoverDiffDataCnt = Reg(UInt(8 bits)) init(0x0)
//    //val rCoverAddr = Vec.fill(4.min(wordCount))(
//    //  Reg(UInt(log2Up(wordCount) bits)) init(0x0)
//    //)
//    //val rCoverAddrCnt = Reg(UInt(8 bits)) init(0x0)
//    //val rCoverAddrLeastCnt = Reg(UInt(8 bits)) init(0x0)
//    ////val rCoverAddrLastIdx = Reg(
//    ////  UInt(log2Up(rCoverAddr.size) bits) init(0x0)
//    ////) 
//    ////val rCoverInvIsFiring = Reg(Bool()) init(False)
//    ////val rCoverInvCnt = Reg(UInt(8 bits)) init(0x0)
//    ////val rCoverSameIsFiring = Reg(Bool()) init(False)
//    ////val rCoverSameCnt = Reg(UInt(8 bits)) init(0x0)
//
//    ////--------
//    //val rUpRdValidDelVec = Vec.fill(8)(
//    //  Reg(Bool()) init(False)
//    //)
//    //when (up.isFiring) {
//    //  for (idx <- 0 until rUpRdValidDelVec.size) {
//    //    def tempUpRdValid = rUpRdValidDelVec(idx)
//    //    if (idx == 0) {
//    //      tempUpRdValid := up(rdValid)
//    //    } else {
//    //      tempUpRdValid := rUpRdValidDelVec(idx - 1)
//    //    }
//    //  }
//    //}
//    ////--------
//
//    //GenerationFlags.formal {
//    //  when (pastValidAfterReset) {
//    //    //when (
//    //    //  past(pastValidAfterReset)
//    //    //) {
//    //      //--------
//    //      //when (up.isFiring) {
//    //      //  rCoverInvIsFiring := True
//    //      //  rCoverSameIsFiring := True
//    //      //}
//    //      //--------
//    //      //when (
//    //      //  down(rdValid)
//    //      //  && !past(down(rdValid))
//    //      //) {
//    //      //  //when (down.isFiring /*|| rCoverInvIsFiring*/) {
//    //      //    //cover(past(down(rdValid)))
//    //      //    rCoverInvCnt := rCoverInvCnt + 1
//    //      //    //rCoverInvIsFiring := False
//    //      //  //}
//    //      //}
//    //      when (
//    //        up.isFiring
//    //        //&& 
//    //        //io.back.valid
//    //        //up.valid
//    //        && rCoverDiffDataCnt < rCoverDiffData.size
//    //      ) {
//    //        val firstSame = rCoverDiffData.sFindFirst(
//    //          _ === up(pipePayload.front).data
//    //        )
//    //        when (!firstSame._1) {
//    //          rCoverDiffData(rCoverDiffDataCnt.resized) := (
//    //            up(pipePayload.front).data
//    //          )
//    //          rCoverDiffDataCnt := rCoverDiffDataCnt + 1
//    //        }
//    //      }
//    //      when (
//    //        up.isFiring
//    //        && rCoverAddrCnt < rCoverAddr.size
//    //      ) {
//    //        val leastPlusOne = rCoverAddrLeastCnt + 1
//
//    //        //val firstSame = rCoverAddr.sFindFirst(
//    //        //  _ === up(pipePayload.front).addr
//    //        //)
//    //        //when (!firstSame._1) {
//    //        //  rCoverAddr(rCoverAddrCnt.resized) := (
//    //        //    up(pipePayload.front).addr
//    //        //  )
//    //        //  rCoverAddrCnt := rCoverAddrCnt + 1
//    //        //}
//    //        when (!leastPlusOne(1)) {
//    //          //--------
//    //          when (
//    //            rCoverAddrCnt === 0
//    //            && leastPlusOne === 0
//    //          ) {
//    //            rCoverAddrLeastCnt := leastPlusOne
//    //            rCoverAddr(rCoverAddrCnt.resized) := (
//    //              up(pipePayload.front).addr
//    //            )
//    //          } elsewhen (
//    //            rCoverAddr(rCoverAddrCnt.resized)
//    //            === up(pipePayload.front).addr
//    //          ) {
//    //            rCoverAddrLeastCnt := leastPlusOne
//    //          }
//    //          //--------
//    //        } otherwise {
//    //          //--------
//    //          rCoverAddrLeastCnt := 0
//    //          //--------
//    //          rCoverAddrCnt := rCoverAddrCnt + 1
//    //          //--------
//    //          rCoverAddr((rCoverAddrCnt + 1).resized) := (
//    //            up(pipePayload.front).addr
//    //          )
//    //        }
//    //      }
//    //      cover(
//    //        rCoverDiffDataCnt === rCoverDiffData.size
//    //        //&& rCoverAddrCnt === rCoverAddr.size
//    //        //rCoverDiffDataCnt === 1
//    //      )
//    //      //val rHadFlip = RegNext(
//    //      //  up(rdValid)
//    //      //  && !rUpRdValidDelVec(0)
//    //      //  && rUpRdValidDelVec(1)
//    //      //  && !rUpRdValidDelVec(2)
//    //      //  && rUpRdValidDelVec(3)
//    //      //) init(False)
//    //      cover(
//    //        rCoverDiffDataCnt === rCoverDiffData.size
//    //        && rCoverAddrCnt === rCoverAddr.size
//    //        //&& rHadFlip
//    //        //--------
//    //        //up(rdValid)
//    //        //&& !rUpRdValidDelVec(0)
//    //        //&& rUpRdValidDelVec(1)
//    //        //&& !rUpRdValidDelVec(2)
//    //        //&& rUpRdValidDelVec(3)
//    //        //--------
//    //        //&& (RegNextWhen(True, up(rdValid)) init(False))
//    //        //&& (RegNextWhen(True, !rUpRdValidDelVec(0)) init(False))
//    //        //&& (RegNextWhen(True, rUpRdValidDelVec(1)) init(False))
//    //        //&& (RegNextWhen(True, !rUpRdValidDelVec(2)) init(False))
//    //        //&& (RegNextWhen(True, rUpRdValidDelVec(3)) init(False))
//    //        //rCoverDiffDataCnt === 1
//    //      )
//    //      //val rHadNonZeroData = Vec.fill(2){
//    //      //  val temp = Reg(Flow(PipeMemTest.wordType()))
//    //      //  temp.init(temp.getZero)
//    //      //  temp
//    //      //}
//
//    //      //when (
//    //      //  io.front.fire
//    //      //  && (
//    //      //    io.front.data =/= 0
//    //      //  )
//    //      //) {
//    //      //  rHadNonZeroData.valid := True
//    //      //  rHadNonZeroData.payload := io.front.data
//    //      //}
//    //      //cover(
//    //      //  rHadNonZeroData.valid
//    //      //  //&& io.back.valid
//    //      //  //&& io.back.sum === rHadNonZeroData.payload
//    //      //  && up.valid
//    //      //  && up(pipePayload.front).data === rHadNonZeroData.payload
//    //      //)
//
//    //      //when (
//    //      //  up(rdValid)
//    //      //  //&& past(up(rdValid))
//    //      //  //&& stable(up(pipePayload.front).addr)
//    //      //  //&& !stable(up(pipePayload.front).data)
//    //      //) {
//    //      //  //when (up.isFiring /*|| rCoverSameIsFiring*/) {
//    //      //    rCoverSameCnt := rCoverSameCnt + 1
//    //      //  //}
//    //      //}
//    //      //cover(rCoverInvCnt === 3)
//    //      //cover(rCoverSameCnt === 3)
//    //      //cover(
//    //      //  //rCoverInvCnt === 3
//    //      //  //&& 
//    //      //  rCoverSameCnt === 3
//    //      //)
//    //      //cover(!stable(up(rdValid)))
//    //      //cover(!up(rdValid))
//    //      //cover(up(rdValid))
//    //    //}
//    //  }
//    //}
//  }
//  //--------
//  Builder(linkArr.toSeq)
//  //--------
//}
////--------
//object PipeMemTestToVerilog extends App {
//  Config.spinal.generateVerilog(
//    PipeMemTest(
//      wordCount=8
//    )
//  )
//}
////--------
//case class PipeMemTestSimDutIo(
//  wordCount: Int,
//) extends Bundle {
//  val sum = out(PipeMemTest.wordType())
//}
//object PipeMemTestSimDut {
//  def tempWidth = 32.max(PipeMemTest.wordWidth * 4)
//  def tempType() = UInt(tempWidth bits)
//  def mkTempUInt[
//    T
//  ](
//    value: T,
//  ) = U(f"$tempWidth'd$value")
//}
//case class PipeMemTestSimDut(
//  wordCount: Int,
//) extends Component {
//  //--------
//  //val io = new Bundle {
//  //  //val sum = out(PipeMemTest.wordType())
//  //}
//  //--------
//  val loc = new Area {
//    val nextCnt = PipeMemTestSimDut.tempType()
//    val rCnt = RegNext(nextCnt) init(0x0)
//  }
//  //--------
//}
//object PipeMemTestSim extends App {
//}
