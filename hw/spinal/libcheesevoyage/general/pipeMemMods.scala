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
//) extends Interface {
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
//--------
//case class PipeMemRmwExtConfig[
//  WordT <: Data,
//  HazardCmpT <: Data,
//](
//) {
//}
case class PipeMemRmwConfig[
  WordT <: Data,
  HazardCmpT <: Data,
  //ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  //DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
](
  wordType: HardType[WordT],
  wordCountArr: Seq[Int],
  hazardCmpType: HardType[HazardCmpT],
  //modType: HardType[ModT],
  modRdPortCnt: Int,
  modStageCnt: Int,
  pipeName: String,
  var linkArr: Option[ArrayBuffer[Link]]=None,
  memArrIdx: Int=0,
  //dualRdType: HardType[DualRdT]=PipeMemRmwDualRdTypeDisabled[
  //  WordT, HazardCmpT,
  //](),
  optDualRd: Boolean=false,
  optReorder: Boolean=false,
  init: Option[Seq[Seq[WordT]]]=None,
  initBigInt: Option[Seq[Seq[BigInt]]]=None,
  optModHazardKind: PipeMemRmw.ModHazardKind=PipeMemRmw.ModHazardKind.Dupl,
  optEnableClear: Boolean=false,
  memRamStyle: String="auto",
  vivadoDebug: Boolean=false,
  optIncludeModFrontStageLink: Boolean=true,
  optIncludeModFrontS2MLink: Boolean=true,
  optFormal: Boolean=false,
  //--------
  //doHazardCmpFunc: Option[
  //  (
  //    PipeMemRmwPayloadExt[WordT, HazardCmpT],  // curr
  //    PipeMemRmwPayloadExt[WordT, HazardCmpT],  // prev
  //    Int,                                      // zdx
  //    Boolean,                                  // isPostDelay
  //  ) => Bool
  //]=None,
  //doPrevHazardCmpFunc: Boolean=false,
  ////--------
  //doModInFrontFunc: Option[
  //  (
  //    ModT, // outp
  //    ModT, // inp
  //    CtrlLink, // mod.front.cFront
  //    //Vec[UInt], // upExt(1)(extIdxUp).memAddr
  //    //Int, // ydx
  //  ) => Area
  //]=None,
  ////--------
  //doModInModFrontFunc: Option[
  //  //[
  //  //  ModT <: Data,
  //  //]
  //  (
  //    PipeMemRmwDoModInModFrontFuncParams[
  //      WordT,
  //      HazardCmpT,
  //      ModT,
  //    ],
  //  ) => Area
  //]=None,
  ////doFwdFunc: Option[
  ////  (
  ////    //ModT, //
  ////    UInt, // stage index
  ////    Vec[PipeMemRmwPayloadExt[WordT, HazardCmpT]], // myUpExtDel
  ////    Int,  // zdx
  ////  ) => WordT
  ////]=None,
  ////--------
  //reorderFtable: Option[PipeMemRmwReorderFtable[WordT]]=None,
  ////setReorderBusyFunc: Option[
  ////  (
  ////    WordT,    // self
  ////    Bool,     // busy
  ////  ) => Unit
  ////]=None,
  ////--------
  ////formalFwdStallKind: Int=0,
  ////formalFwdStallCnt: Int=0,
  ////formalFwdStallAddr: Option[UInt]=None,
  ////--------
) {
  assert(
    wordCountArr.size > 0,
    s"wordCountArr.size (${wordCountArr.size}) must be greater than zero"
  )
  for ((wordCount, wordCountIdx) <- wordCountArr.view.zipWithIndex) {
    assert(
      wordCount > 0,
      s"wordCount (value:${wordCount} index:${wordCountIdx}) "
      + s"must be greater than zero"
    )
  }
  def memArrSize = wordCountArr.size
  val (wordCountSum, wordCountMax): (Int, Int) = {
    var mySum: Int = 0
    var myMax: Int = 0
    for (ydx <- 0 until memArrSize) {
      mySum += wordCountArr(ydx)
      if (myMax < wordCountArr(ydx)) {
        myMax = wordCountArr(ydx)
      }
    }
    (mySum, myMax)
  }
}
//--------
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
case class PipeMemRmwPayloadExtPipeFlags(
) extends Bundle {
  val valid = KeepAttribute(Bool())
  val ready = KeepAttribute(Bool())
  val fire = KeepAttribute(Bool())
}
case class PipeMemRmwPayloadExtMainNonMemAddr[
  WordT <: Data,
  HazardCmpT <: Data
](
  cfg: PipeMemRmwConfig[
    WordT,
    HazardCmpT,
  ],
  wordCount: Int,
) extends Bundle {
  //--------
  def wordType() = cfg.wordType()
  //def wordCount = cfg.wordCount
  def hazardCmpType() = cfg.hazardCmpType()
  def modRdPortCnt = cfg.modRdPortCnt
  def modStageCnt = cfg.modStageCnt
  def memArrSize = cfg.memArrSize
  def optModHazardKind: PipeMemRmw.ModHazardKind = (
    cfg.optModHazardKind
  )
  def optReorder = cfg.optReorder 
  //myHaveFormalFwd: Boolean=false,
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
  //val modMemWordFwd = wordType()
  //val modMemWordValidFwd = Bool()
  val modMemWord = wordType()
  val modMemWordValid = Bool()
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
    optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
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
case class PipeMemRmwPayloadExtMain[
  WordT <: Data,
  HazardCmpT <: Data,
](
  cfg: PipeMemRmwConfig[
    WordT,
    HazardCmpT,
  ],
  //wordType: HardType[WordT],
  wordCount: Int,
  //hazardCmpType: HardType[HazardCmpT],
  //modRdPortCnt: Int,
  //modStageCnt: Int,
  //memArrSize: Int,
  ////optSimpleIsWr: Option[Boolean]=None,
  ////optUseModMemAddr: Boolean=false,
  ////doModInModFront: Boolean=false,
  ////optEnableModDuplicate: Boolean=true,
  //optModHazardKind: PipeMemRmw.ModHazardKind=PipeMemRmw.ModHazardKind.Dupl,
  //optReorder: Boolean=false,
  ////myHaveFormalFwd: Boolean=false,
) extends Bundle {
  
  def wordType() = cfg.wordType()
  //def wordCount = cfg.wordCount
  def hazardCmpType() = cfg.hazardCmpType()
  def modRdPortCnt = cfg.modRdPortCnt
  def modStageCnt = cfg.modStageCnt
  def memArrSize = cfg.memArrSize
  def optModHazardKind: PipeMemRmw.ModHazardKind = (
    cfg.optModHazardKind
  )
  def optReorder = cfg.optReorder 
  //myHaveFormalFwd: Boolean=false,
  //--------
  //val hadActiveUpFire = KeepAttribute(Bool())
  val memAddr = Vec.fill(modRdPortCnt)(
    UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
  )
  val memAddrFwd = Vec.fill(modRdPortCnt)(
    UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
  )
  //def modMemAddr = memAddr(0)
  //val didReorderCommit = (optReorder) generate (
  //  Bool()
  //)
  //--------
  //val reqReorderCommit = (optReorder) generate (
  //  /*Vec.fill(modRdPortCnt)*/(Bool())
  //)
  //val didReorderCommit = (optReorder) generate (
  //  /*Vec.fill(modRdPortCnt)*/(Bool())
  //)
  //--------

  //// When `True`, read from the address `memAddr`
  //// When `False`, read from the address `dualRd.rReorderCommitHead`, 
  //// which provides in-order reads
  //val memAddrReorderValid = (optReorder) generate (
  //  Bool()
  //)
  //--------
  val nonMemAddr = PipeMemRmwPayloadExtMainNonMemAddr(
    cfg=cfg,
    wordCount=wordCount,
  )
}

case class PipeMemRmwPayloadExt[
  WordT <: Data,
  HazardCmpT <: Data,
](
  cfg: PipeMemRmwConfig[
    WordT,
    HazardCmpT,
  ],
  //wordType: HardType[WordT],
  wordCount: Int,
  //hazardCmpType: HardType[HazardCmpT],
  //modRdPortCnt: Int,
  //modStageCnt: Int,
  //memArrSize: Int,
  ////optSimpleIsWr: Option[Boolean]=None,
  ////optUseModMemAddr: Boolean=false,
  ////doModInModFront: Boolean=false,
  ////optEnableModDuplicate: Boolean=true,
  //optModHazardKind: PipeMemRmw.ModHazardKind=PipeMemRmw.ModHazardKind.Dupl,
  //optReorder: Boolean=false,
  ////myHaveFormalFwd: Boolean=false,
) extends Bundle {
  //setDefinitionName(
  //  s"testificate_${cfg}_${wordCount}"
  //)
  def wordType() = cfg.wordType() 
  //def wordCount = cfg.wordCount 
  def hazardCmpType() = cfg.hazardCmpType()
  def modRdPortCnt = cfg.modRdPortCnt
  def modStageCnt = cfg.modStageCnt
  def memArrSize = cfg.memArrSize
  def optModHazardKind: PipeMemRmw.ModHazardKind = (
    cfg.optModHazardKind
  )
  def optReorder = cfg.optReorder
  //myHaveFormalFwd: Boolean=false,
  
  if (optModHazardKind == PipeMemRmw.ModHazardKind.Fwd) {
    assert(modRdPortCnt > 0)
    assert(!optReorder)
  } else {
    assert(modRdPortCnt == 1)
  }

  //val valid = KeepAttribute(Bool())
  //val ready = KeepAttribute(Bool())
  //val fire = KeepAttribute(Bool())
  val pipeFlags = KeepAttribute(
    PipeMemRmwPayloadExtPipeFlags()
  )
  def valid = pipeFlags.valid
  def ready = pipeFlags.ready
  def fire = pipeFlags.fire

  val main = KeepAttribute(
    PipeMemRmwPayloadExtMain(
      cfg=cfg,
      //wordType=wordType(),
      wordCount=wordCount,
      //hazardCmpType=hazardCmpType(),
      //modRdPortCnt=modRdPortCnt,
      //modStageCnt=modStageCnt,
      //memArrSize=memArrSize,
      //optModHazardKind=optModHazardKind,
      //optReorder=optReorder,
    )
  )
  def memAddr = main.memAddr
  def memAddrFwd = main.memAddrFwd
  def modMemWord = main.nonMemAddr.modMemWord
  //def modMemWordFwd = main.nonMemAddr.modMemWordFwd
  def modMemWordValid = main.nonMemAddr.modMemWordValid
  //def modMemWordValidFwd = main.nonMemAddr.modMemWordValidFwd
  def rdMemWord = main.nonMemAddr.rdMemWord
  //def reqReorderCommit = main.nonMemAddr.reqReorderCommit
  //def didReorderCommit = main.nonMemAddr.didReorderCommit
  def hazardCmp = main.nonMemAddr.hazardCmp
  def hazardId = main.nonMemAddr.hazardId
  def getHazardIdIdleVal() = (
    -1
  )
  def doInitHazardId(): Unit = {
    //for (zdx <- 0 until modRdPortCnt) {
      hazardId/*(zdx)*/ := getHazardIdIdleVal()
    //}
  }
}
//case class PipeMemRmwPayloadExt[
//  WordT <: Data,
//  HazardCmpT <: Data,
//](
//  cfg: PipeMemRmwConfig[
//    WordT,
//    HazardCmpT,
//  ],
//  //wordType: HardType[WordT],
//  wordCount: Int,
//  //hazardCmpType: HardType[HazardCmpT],
//  //modRdPortCnt: Int,
//  //modStageCnt: Int,
//  //memArrSize: Int,
//  ////optSimpleIsWr: Option[Boolean]=None,
//  ////optUseModMemAddr: Boolean=false,
//  ////doModInModFront: Boolean=false,
//  ////optEnableModDuplicate: Boolean=true,
//  //optModHazardKind: PipeMemRmw.ModHazardKind=PipeMemRmw.ModHazardKind.Dupl,
//  //optReorder: Boolean=false,
//  ////myHaveFormalFwd: Boolean=false,
//) extends PipeMemRmwPayloadExtBase(
//  cfg=cfg,
//  //wordType=wordType(),
//  wordCount=wordCount,
//  //hazardCmpType=hazardCmpType(),
//  //modRdPortCnt=modRdPortCnt,
//  //modStageCnt=modStageCnt,
//  //memArrSize=memArrSize,
//  //optModHazardKind=optModHazardKind,
//  //optReorder=optReorder,
//) {
//}
//case class PipeMemRmwPayloadBaseFormalFwdFuncs[
//  WordT <: Data,
//  HazardCmpT <: Data,
//](
//)(
//  setPipeMemRmwFwdFunc: (
//    PipeMemRmwFwd[WordT, HazardCmpT], // outpFwd
//    Int,                              // memArrIdx
//  ) => Unit,
//  getPipeMemRmwFwdFunc: (
//    PipeMemRmwFwd[WordT, HazardCmpT], // inpFwd
//    Int,                              // memArrIdx
//  ) => Unit,
//) extends Interface {
//}

trait PipeMemRmwPayloadBase[
  WordT <: Data,
  HazardCmpT <: Data,
] extends Bundle {
  //--------
  def setPipeMemRmwExt
  //[
  //  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  //  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  //]
  (
    inpExt: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    ydx: Int,
    memArrIdx: Int,
  ): Unit

  def getPipeMemRmwExt
  //[
  //  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  //  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  //]
  (
    outpExt: PipeMemRmwPayloadExt[WordT, HazardCmpT],
      // this is essentially a return value
    ydx: Int,
    memArrIdx: Int,
  ): Unit
  //--------
  // Optional methods for when 
  // (
  //  optFormal
  //  && (optModHazardKind == PipeMemRmw.ModHazardKind.Fwd)
  // )
  def formalSetPipeMemRmwFwd
  //[
  //  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  //  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  //]
  (
    inpFwd: PipeMemRmwFwd[
      WordT,
      HazardCmpT,
      //ModT,
      //DualRdT,
    ],
    memArrIdx: Int,
  ): Unit// = { //= ???
  //} 

  def formalGetPipeMemRmwFwd
  //[
  //  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  //  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  //]
  (
    outpFwd: PipeMemRmwFwd[
      WordT,
      HazardCmpT,
      //ModT,
      //DualRdT,
    ],
    memArrIdx: Int,
  ): Unit// = { //= ???
  //}

  //def optFormalFwdFuncs(
  //): Option[PipeMemRmwPayloadBaseFormalFwdFuncs[WordT, HazardCmpT]]
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
  def extMainSize = 2
  def addrWidth(
    wordCount: Int,
  ) = log2Up(wordCount)
  //def kindRmw = 0
  //def kindSimpleDualPort = 1
  def numPostFrontStages(
    //doModInModFront: Boolean,
    optModHazardKind: PipeMemRmw.ModHazardKind,
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
    optModHazardKind: PipeMemRmw.ModHazardKind,
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
        optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
        //&& optModFwdToFront
      ) (
        //1
        0
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
  def numMyUpExtDel2(
    optModHazardKind: PipeMemRmw.ModHazardKind,
    modStageCnt: Int,
  ) = (
    PipeMemRmw.numPostFrontPreWriteStages
    //PipeMemRmw.numPostFrontPreWriteStages
    (
      //doModInModFront=doModInModFrontFunc match {
      //  case Some(myDoModSingleStageFunc) => true
      //  case None => false
      //},
      optModHazardKind=optModHazardKind,
      modStageCnt=modStageCnt,
    )
    + 1
    //+ (
    //  if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
    //    0
    //  ) else (
    //    -1
    //  )
    //)
    //- 1
  )
  //--------
  sealed trait ModHazardKind
  object ModHazardKind {
    case object Dont extends ModHazardKind
    case object Dupl extends ModHazardKind
    case object Fwd extends ModHazardKind
  }
  //def modHazardKindDont = ModHazardKind.Dont
  //def modHazardKindDupl = ModHazardKind.Dupl
  //def modHazardKindFwd = ModHazardKind.Fwd
  //--------
  def modWrIdx = 0
  def modRdIdxStart = 1
  //--------
  def extIdxUp = 0
  def extIdxSaved = 1
  def extIdxLim = 2
  def extIdxSingle = extIdxUp
  //--------
  //def formalFwdStallKindNone = 0
  //def formalFwdStallKindHalt = 1
  //def formalFwdStallKindDuplicate = 2
  //--------
}
case class PipeMemRmwDualRdTypeDisabled[
  WordT <: Data,
  HazardCmpT <: Data,
](
) extends Bundle with PipeMemRmwPayloadBase[WordT, HazardCmpT]
{
  //--------
  /*override*/ def setPipeMemRmwExt(
    inpExt: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
  }
  /*override*/ def getPipeMemRmwExt(
    outpExt: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
  }
  //def optFormalFwdFuncs(
  //): Option[PipeMemRmwPayloadBaseFormalFwdFuncs[WordT, HazardCmpT]] = None
  /*override*/ def formalSetPipeMemRmwFwd(
    outpFwd: PipeMemRmwFwd[WordT, HazardCmpT],
    memArrIdx: Int,
  ): Unit = {
  }

  /*override*/ def formalGetPipeMemRmwFwd(
    inpFwd: PipeMemRmwFwd[
      WordT,
      HazardCmpT,
    ],
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
case class PipeMemRmwFwd[
  WordT <: Data,
  HazardCmpT <: Data,
  //ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  //DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
](
  cfg: PipeMemRmwConfig[
    WordT,
    HazardCmpT,
    //ModT,
    //DualRdT,
  ],
  //wordCountArrIdx: Int,
)
//(
//  mkFwdOneExtFunc: (
//    //Boolean
//  ) => /*Vec[Vec[*/PipeMemRmwPayloadExt[
//    WordT,
//    HazardCmpT,
//  ]/*]]*/
//) 
extends Bundle {
  
  def wordType() = cfg.wordType()
  def wordCount = cfg.wordCountMax
  def memArrSize = cfg.memArrSize
  def hazardCmpType() = cfg.hazardCmpType()
  def modRdPortCnt = cfg.modRdPortCnt
  def modStageCnt = cfg.modStageCnt
  //def memArrSize = cfg.memArrSize
  def optModHazardKind: PipeMemRmw.ModHazardKind = (
    cfg.optModHazardKind
  )
  def optReorder = cfg.optReorder 
  //myHaveFormalFwd: Boolean,
  //--------
  val myFindFirst_0 = (
    KeepAttribute(
      Vec.fill(memArrSize)(
        Vec.fill(modRdPortCnt)(
          Vec.fill(PipeMemRmw.extIdxLim)(
            Bool()
          )
        )
      )
    )
  )
  //--------
  val myFindFirst_1 = (
    KeepAttribute(
      Vec.fill(memArrSize)(
        Vec.fill(modRdPortCnt)(
          Vec.fill(PipeMemRmw.extIdxLim)(
            UInt(log2Up(
              ////mod.front.myUpExtDel2.size
              numMyUpExtDel2
            ) bits)
          )
        )
      )
    )
  )
  //--------
  def numMyUpExtDel2 = (
    PipeMemRmw.numMyUpExtDel2(
      optModHazardKind=optModHazardKind,
      modStageCnt=modStageCnt,
    )
  )
  def mkExt(
    //myHaveFormalFwd: Boolean,
    //myVivadoDebug: Boolean=false,
  ): Vec[Vec[PipeMemRmwPayloadExt[
    WordT,
    HazardCmpT,
  ]]] = {
    val ret = Vec.fill(memArrSize)(
      Vec.fill(PipeMemRmw.extIdxLim)(
        //PipeMemRmwPayloadExt(
        //  wordType=wordType(),
        //  wordCount=wordCountMax,
        //  hazardCmpType=hazardCmpType(),
        //  modRdPortCnt=modRdPortCnt,
        //  modStageCnt=modStageCnt,
        //  memArrSize=memArrSize,
        //  optModHazardKind=optModHazardKind,
        //  optReorder=optReorder,
        //  //myHaveFormalFwd=myHaveFormalFwd
        //)
        //mkFwdOneExtFunc(
        //  //myVivadoDebug
        //)
        PipeMemRmwPayloadExt[
          WordT,
          HazardCmpT,
        ](
          cfg=cfg,
          //wordType=wordType(),
          wordCount=wordCount,
          //hazardCmpType=hazardCmpType(),
          //modRdPortCnt=modRdPortCnt,
          //modStageCnt=modStageCnt,
          //memArrSize=memArrSize,
          //optModHazardKind=optModHazardKind,
          //optReorder=optReorder,
        )
      )
    )
    //if (vivadoDebug && myVivadoDebug) {
    //  //ret.addAttribute("MARK_DEBUG", "TRUE")
    //  //ret.memAddr.addAttribute("MARK_DEBUG", "TRUE")
    //  //ret.hazardCmp.addAttribute("MARK_DEBUG", "TRUE")
    //  //if (optEnableModDuplicate) {
    //  //  ret.hazardId.addAttribute("MARK_DEBUG", "TRUE")
    //  //}
    //  //ret.modMemWord.addAttribute("MARK_DEBUG", "TRUE")
    //}
    ret
  }

  val myFwdData = (
    //(
    //  myHaveFormalFwd
    //) generate 
    (
      KeepAttribute(
        //Vec.fill(numMyUpExtDel2)(
          Vec.fill(memArrSize)(
            //Vec.fill(PipeMemRmw.extIdxLim)(
            Vec.fill(modRdPortCnt)(
              wordType()
            )
            //)
          )
        //)
      )
    )
  )

  val myUpExtDel2 = (
    //(
    //  myHaveFormalFwd
    //) generate 
    (
      KeepAttribute(
        Vec.fill(
          numMyUpExtDel2
        )(
          //mkFwdExtFunc(
          //  false
          //)
          mkExt(
            //false
          )
        )
      )
    )
  )
  val myUpExtDel2FindFirstVec = (
    //(
    //  myHaveFormalFwd
    //) generate 
    (
      KeepAttribute(
        Vec.fill(memArrSize)(
          Vec.fill(modRdPortCnt)(
            Vec.fill(PipeMemRmw.extIdxLim)(
              Vec.fill(numMyUpExtDel2)(
                Bool()
              )
            )
          )
        )
      )
    )
  )
  //--------
  //--------
  //--------
  //def myFormalFwdGetRdMemWord(
  //  ydx: Int,
  //  zdx: Int,
  //) = new Area {
  //  assert(
  //    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
  //  )
  //}
  //--------
}
case class PipeMemRmwDoFwdArea[
  WordT <: Data,
  HazardCmpT <: Data,
  //ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  //DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
]
//(
//  //pipeName: String,
//  //wordType: HardType[WordT],
//  //wordCountMax: Int,
//  //hazardCmpType: HardType[HazardCmpT],
//  modRdPortCnt: Int,
//  modStageCnt: Int,
//  memArrSize: Int,
//  //optModHazardKind: PipeMemRmw.ModHazardKind,
//  //optReorder: Boolean,
//  //optFormal: Boolean,
//)
(
  //ydx: Int,
  fwdAreaName: String,
  fwd: PipeMemRmwFwd[
    WordT,
    HazardCmpT,
    //ModT,
    //DualRdT,
  ],
  setToMyFwdDataFunc: (
    Int,      // ydx
    Int,      // zdx
    WordT,    // myFwdData
  ) => Unit,
  optFirstFwdRdMemWord: Option[
    Vec[Vec[
      //PipeMemRmwPayloadExt[
      //  WordT,
      //  HazardCmpT,
      //]
      WordT
    ]]
  ]=None,
  //rdMemWord: Vec[Vec[WordT]],
  //firstFwd: Boolean=false,
  //setToMyFwdSavedFunc: (
  //  Int,      // ydx
  //  Int,      // zdx
  //  WordT,    // modMemWord
  //) => Unit,
) extends Area {
  //def myHaveFwd = (
  //  optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
  //)
  def extIdxUp = (
    PipeMemRmw.extIdxUp
  )
  def extIdxSaved = (
    PipeMemRmw.extIdxSaved
  )
  def extIdxSingle = (
    PipeMemRmw.extIdxSingle
  )
  //if (myHaveFwd) {
    for (ydx <- 0 until fwd.memArrSize) {
      for (zdx <- 0 until fwd.modRdPortCnt) {
        val firstFwdRdMemWord: (Boolean, WordT) = (
          optFirstFwdRdMemWord match {
            case Some(myFirstFwdRdMemWord) => {
              (
                true,
                //myFirstFwdExt(ydx)(PipeMemRmw.extIdxUp).rdMemWord(zdx),
                myFirstFwdRdMemWord(ydx)(zdx)
              )
            }
            case None => {
              (false, fwd.wordType())
            }
          }
        )
        def firstFwd = firstFwdRdMemWord._1
        val myFindFirstUp = KeepAttribute(
          //(
          //  //optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
          //  doOverrideFindFirst
          //) generate 
          (
            (
              fwd.myUpExtDel2FindFirstVec(ydx)(zdx)(extIdxUp)
              .sFindFirst(
                _ === True
              )
            )
            .setName(s"${fwdAreaName}_myFindFirstUp_${ydx}_${zdx}")
          )
        )
        val myFindFirstSaved = KeepAttribute(
          //(
          //  //optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
          //  doOverrideFindFirst
          //) generate 
          (
            (
              fwd.myUpExtDel2FindFirstVec(ydx)(zdx)(extIdxSaved)
              .sFindFirst(
                _ === True
              )
            )
            .setName(s"${fwdAreaName}_myFindFirstDown_${ydx}_${zdx}")
          )
        )
        def tempMyFindFirstUp_0 = (
          fwd.myFindFirst_0(ydx)(zdx)(extIdxUp)
        )
        def tempMyFindFirstUp_1 = (
          fwd.myFindFirst_1(ydx)(zdx)(extIdxUp)
        )
        def tempMyFindFirstSaved_0 = (
          fwd.myFindFirst_0(ydx)(zdx)(extIdxSaved)
        )
        def tempMyFindFirstSaved_1 = (
          fwd.myFindFirst_1(ydx)(zdx)(extIdxSaved)
        )
        def tempMyFwdData = (
          fwd.myFwdData(ydx)(zdx)
        )
        //tempMyFindFirstUp_0.allowOverride
        //tempMyFindFirstUp_1.allowOverride
        //tempMyFindFirstSaved_0.allowOverride
        //tempMyFindFirstSaved_1.allowOverride
        val myFwdCondUp = (
          firstFwd
        ) generate (
          KeepAttribute(
            //myFindFirstUp._1
            //fwd.myFindFirst_0(ydx)(zdx)(extIdxUp)
            tempMyFindFirstUp_0
          )
          .setName(s"${fwdAreaName}_myFwdCondUp_${ydx}_${zdx}")
        )
        val myFwdCondSaved = (
          firstFwd
        ) generate (
          KeepAttribute(
            //myFindFirstSaved._1
            //fwd.myFindFirst_0(ydx)(zdx)(extIdxSaved)
            tempMyFindFirstSaved_0
          )
          .setName(s"${fwdAreaName}_myFwdCondDown_${ydx}_${zdx}")
        )
        val myFwdDataUp = (
          firstFwd
        ) generate (
          KeepAttribute(
            fwd.myUpExtDel2(
              //myFindFirstUp._2
              //fwd.myFindFirst_1(ydx)(zdx)(extIdxUp)
              tempMyFindFirstUp_1
            )(ydx)(
              extIdxUp
            ).modMemWord
          )
          .setName(s"${fwdAreaName}_myFwdDataUp_${ydx}_${zdx}")
        )
        val myFwdDataSaved = (
          firstFwd
        ) generate (
          KeepAttribute(
            fwd.myUpExtDel2(
              //myFindFirstSaved._2
              //fwd.myFindFirst_1(ydx)(zdx)(extIdxSaved)
              tempMyFindFirstSaved_1
            )(ydx)(
              extIdxSaved
            ).modMemWord
          )
          .setName(s"${fwdAreaName}_myFwdDataDown_${ydx}_${zdx}")
        )
        if (firstFwd) {
          tempMyFindFirstUp_0 := (
            myFindFirstUp._1
          )
          tempMyFindFirstUp_1 := (
            myFindFirstUp._2
          )
          tempMyFindFirstSaved_0 := (
            myFindFirstSaved._1
          )
          tempMyFindFirstSaved_1 := (
            myFindFirstSaved._2
          )
        }
        if (
          //optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
          //myHaveFwd
          firstFwd
        ) {
          def mySetToMyFwdUp(): Unit = {
            //upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
            //  myFwdDataUp
            //)
            //setToMyFwdDataFunc(
            //  ydx,
            //  zdx,
            //  myFwdDataUp
            //)
            tempMyFwdData := myFwdDataUp
          }
          def mySetToMyFwdSaved(): Unit = {
            //upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
            //  myFwdDataSaved
            //)
            //setToMyFwdDataFunc(
            //  ydx,
            //  zdx,
            //  myFwdDataSaved
            //)
            tempMyFwdData := myFwdDataSaved
          }
          def innerFunc(): Unit = {
            when (
              tempMyFindFirstUp_0
            ) {
              mySetToMyFwdUp()
            } elsewhen (
              tempMyFindFirstSaved_0
            ) {
              mySetToMyFwdSaved()
            } otherwise {
              tempMyFwdData := firstFwdRdMemWord._2
            }
          }
          //when (
          //  tempMyFindFirstUp_0
          //  && tempMyFindFirstSaved_0
          //) {
          //  when (
          //    //myFindFirstUp._2 < myFindFirstSaved._2
          //    tempMyFindFirstUp_1 < tempMyFindFirstSaved_1
          //  ) {
          //    mySetToMyFwdUp()
          //  } elsewhen (
          //    //myFindFirstSaved._2 < myFindFirstUp._2
          //    tempMyFindFirstSaved_1 < tempMyFindFirstUp_1
          //  ) {
          //    mySetToMyFwdSaved()
          //  } otherwise {
          //    innerFunc()
          //  }
          //} otherwise {
          //  innerFunc()
          //}
          when (tempMyFindFirstUp_0) {
            mySetToMyFwdUp()
          } otherwise {
            tempMyFwdData := firstFwdRdMemWord._2
          }
        }
        setToMyFwdDataFunc(
          ydx,
          zdx,
          tempMyFwdData,
        )
      }
    }
  //}
}
//--------
case class PipeMemRmwIo[
  WordT <: Data,
  HazardCmpT <: Data,
  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
](
  cfg: PipeMemRmwConfig[
    WordT,
    HazardCmpT,
  ],
  //wordType: HardType[WordT],
  //wordCountMax: Int,
  //hazardCmpType: HardType[HazardCmpT],
  modType: HardType[ModT],
  //modRdPortCnt: Int,
  //modStageCnt: Int,
  ////optDualRdType: Option[HardType[DualRdT]]=None,
  ////optDualRdType: Option[HardType[DualRdT]]={
  ////  //Some(HardType[PipeMemRmwDualRdTypeDisabled[WordT]]())
  ////},
  //pipeName: String,
  //memArrIdx: Int=0,
  //memArrSize: Int=1,
  dualRdType: HardType[DualRdT]=PipeMemRmwDualRdTypeDisabled[
    WordT, HazardCmpT
  ](),
  ////optDualRdSize: Option[Int]=None,
  ////dualRdSize: Int=0,
  //optDualRd: Boolean=false,
  //optReorder: Boolean=false,
  ////optEnableModDuplicate: Boolean=true,
  //optModHazardKind: PipeMemRmw.ModHazardKind=PipeMemRmw.ModHazardKind.Dupl,
  //optEnableClear: Boolean=false,
  //optFormal: Boolean=false,
  //vivadoDebug: Boolean=false,
) extends Area {
  def wordType() = cfg.wordType()
  def wordCountMax = cfg.wordCountMax
  def hazardCmpType() = cfg.hazardCmpType()
  //def modType() = cfg.modType()
  def modRdPortCnt = cfg.modRdPortCnt
  def modStageCnt = cfg.modStageCnt
  //optDualRdType: Option[HardType[DualRdT]]=None,
  //optDualRdType: Option[HardType[DualRdT]]={
  //  //Some(HardType[PipeMemRmwDualRdTypeDisabled[WordT]]())
  //},
  def pipeName = cfg.pipeName
  def memArrIdx = cfg.memArrIdx
  def memArrSize = cfg.memArrSize
  //def dualRdType() = cfg.dualRdType()
  //optDualRdSize: Option[Int]=None,
  //dualRdSize: Int=0,
  def optDualRd = cfg.optDualRd
  def optReorder = cfg.optReorder
  //optEnableModDuplicate: Boolean=true,
  def optModHazardKind: PipeMemRmw.ModHazardKind = (
    cfg.optModHazardKind
  )
  def optEnableClear = cfg.optEnableClear
  def optFormal = cfg.optFormal 
  def vivadoDebug = cfg.vivadoDebug 
  //--------
  //val doHazardCheck = in(Bool())
  val clear = (optEnableClear) generate (
    /*slave*/(Flow(
      /*Vec.fill(modRdPortCnt)*/(
        UInt(PipeMemRmw.addrWidth(wordCount=wordCountMax) bits)
      )
    ))
  )

  // the commit head
  val reorderCommitHead = (optReorder) generate (
    //Reg(UInt(PipeMemRmw.addrWidth(wordCount=wordCountMax) bits)) init(0x0)
    UInt(PipeMemRmw.addrWidth(wordCount=wordCountMax) bits)
  )
  //val reorderCommitTail = (optReorder) generate (
  //  UInt(PipeMemRmw.addrWidth(wordCount=wordCountMax) bits)
  //)

  // front of the pipeline (push)
  val front = Node()
  //val frontPayloadArr = Array.fill(memArrSize)(Payload(modType()))
  //for (idx <- 0 until memArrSize) {
  //  frontPayloadArr(idx)
  //  .setName(s"${pipeName}_${idx}_io_frontPayloadArr")
  //}
  //def frontPayload = frontPayloadArr//(memArrIdx)
  val frontPayload = (
    Payload(modType())
    .setName(s"${pipeName}_io_frontPayload")
  )
  //--------
  //--------
  //val myFormalFwd = (
  //  (
  //    optFormal
  //  ) && (
  //    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
  //  )
  //) generate (
  //  Payload(FormalFwd())
  //)
  //def myHaveFormalFwd = (
  //  (
  //    optFormal
  //  ) && (
  //    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
  //  )
  //)
  //def mkFwd() = (
  //  PipeMemRmwFwd[
  //    WordT,
  //    HazardCmpT,
  //  ](
  //    wordType=wordType(),
  //    wordCountMax=wordCountMax,
  //    optModHazardKind=optModHazardKind,
  //    modRdPortCnt=modRdPortCnt,
  //    modStageCnt=modStageCnt,
  //    memArrSize=memArrSize,
  //    myHaveFormalFwd=myHaveFormalFwd,
  //  )(
  //    mkExtFunc=mkExt
  //  )
  //)
  //val myFormalFwdPayload = (
  //  myHaveFormalFwd
  //) generate (
  //  Payload(mkFwd())
  //  .setName(s"${pipeName}_io_myFormalFwdPayload")
  //)
  //--------
  //def mkExt(myVivadoDebug: Boolean=false) = {
  //  val ret = Vec.fill(memArrSize)(
  //    Vec.fill(PipeMemRmw.extIdxLim)(
  //      PipeMemRmwPayloadExt(
  //        wordType=wordType(),
  //        wordCount=wordCountMax,
  //        hazardCmpType=hazardCmpType(),
  //        modRdPortCnt=modRdPortCnt,
  //        modStageCnt=modStageCnt,
  //        //doModInModFront=doModInModFrontFunc match {
  //        //  case Some(myDoModSingleStageFunc) => true
  //        //  case None => false
  //        //},
  //        //optEnableModDuplicate=optEnableModDuplicate,
  //        optModHazardKind=optModHazardKind,
  //        optReorder=optReorder,
  //      )
  //    )
  //  )
  //  if (vivadoDebug && myVivadoDebug) {
  //    //ret.addAttribute("MARK_DEBUG", "TRUE")
  //    //ret.memAddr.addAttribute("MARK_DEBUG", "TRUE")
  //    //ret.hazardCmp.addAttribute("MARK_DEBUG", "TRUE")
  //    //if (optEnableModDuplicate) {
  //    //  ret.hazardId.addAttribute("MARK_DEBUG", "TRUE")
  //    //}
  //    //ret.modMemWord.addAttribute("MARK_DEBUG", "TRUE")
  //  }
  //  ret
  //}
  //--------

  // Use `modFront` and `modBack` to insert a pipeline stage for modifying
  // the`WordT`
  val modFront = Node()
  //val modFrontPayloadArr = Array.fill(memArrSize)(Payload(modType()))
  //for (idx <- 0 until memArrSize) {
  //  modFrontPayloadArr(idx)
  //  .setName(s"${pipeName}_${idx}_io_modFrontPayloadArr")
  //}
  //def modFrontPayload = modFrontPayloadArr//(memArrIdx)
  val modFrontPayload = (
    Payload(modType())
    .setName(s"${pipeName}_io_modFrontPayload")
  )
  val tempModFrontPayload = /*Vec.fill(memArrSize)*/(
    modType()
  )
  val modBack = Node()
  //val modBackPrePayloadArr = Array.fill(memArrSize)(Payload(modType()))
  //for (idx <- 0 until memArrSize) {
  //  modBackPrePayloadArr(idx)
  //  .setName(s"${pipeName}_${idx}_io_modBackPrePayloadArr")
  //}
  //def modBackPrePayload = modBackPrePayloadArr
  //val modBackPayload = Payload(modType())
  //  .setName(s"${pipeName}_io_modBackPayload")
  //val modBackPayloadArr = Array.fill(memArrSize)(Payload(modType()))
  //for (idx <- 0 until memArrSize) {
  //  modBackPayloadArr(idx)
  //  .setName(s"${pipeName}_${idx}_io_modBackPayloadArr")
  //}
  //def modBackPayload = modBackPayloadArr//(memArrIdx)
  val modBackPayload = (
    Payload(modType())
    .setName(s"${pipeName}_io_modBackPayload")
  )
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
        optModHazardKind != PipeMemRmw.ModHazardKind.Dont
      )
    ) generate (
      /*in*/(
        Vec.fill(
          modStageCnt //- 1 //- 2
        )(
          //Vec.fill(memArrSize)(
            Vec.fill(PipeMemRmw.extIdxLim)(
              modType()
            )
          //)
        )
      )
    )
  )

  // back of the pipeline (output)
  //val back = master(Stream(modType()))
  val back = Node()
  //val backPayloadArr = Array.fill(memArrSize)(Payload(modType()))
  //for (idx <- 0 until memArrSize) {
  //  backPayloadArr(idx)
  //  .setName(s"${pipeName}_${idx}_io_backPayloadArr")
  //}
  //def backPayload = backPayloadArr//(memArrIdx)
  val backPayload = (
    Payload(modType())
    .setName(s"${pipeName}_io_backPayload")
  )
  //--------
  //val optDualRd = (dualRdSize > 0)
  val dualRdFront = (optDualRd) generate (
    //slave(
    //  Stream(dualRdType())
    //)
    Node()
  )
  //val dualRdFrontPayloadArr = Array.fill(memArrSize)(Payload(dualRdType()))
  //for (idx <- 0 until memArrSize) {
  //  dualRdFrontPayloadArr(idx)
  //  .setName(s"${pipeName}_${idx}_io_dualRdFrontPayloadArr")
  //}
  //def dualRdFrontPayload = dualRdFrontPayloadArr//(memArrIdx)
  val dualRdFrontPayload = (
    Payload(dualRdType())
    .setName(s"${pipeName}_io_dualRdFrontPayload")
  )
  val dualRdBack = (optDualRd) generate (
    //master(
    //  Stream(dualRdType())
    //)
    Node()
  )
  //val dualRdBackPayloadArr = Array.fill(memArrSize)(Payload(dualRdType()))
  //for (idx <- 0 until memArrSize) {
  //  dualRdBackPayloadArr(idx)
  //  .setName(s"${pipeName}_${idx}_io_dualRdBackPayloadArr")
  //}
  //def dualRdBackPayload = dualRdBackPayloadArr//(memArrIdx)
  val dualRdBackPayload = (
    Payload(dualRdType())
    .setName(s"${pipeName}_io_dualRdBackPayload")
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
case class PipeMemRmwDoModInModFrontFuncParams[
  WordT <: Data,
  HazardCmpT <: Data,
  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
](
  //inp: PipeMemRmwPayloadExt[WordT, HazardCmpT],
  //outp: PipeMemRmwPayloadExt[WordT, HazardCmpT],
  pipeMemIo: PipeMemRmwIo[
    WordT,
    HazardCmpT,
    ModT,
    DualRdT,
  ],
  nextPrevTxnWasHazardVec: Vec[Bool],   // nextPrevTxnWasHazardVec
  rPrevTxnWasHazardVec: Vec[Bool],      // rPrevTxnWasHazardVec
  rPrevTxnWasHazardAny: Bool,           // rPrevTxnWasHazardAny
  outp: ModT,                   // tempUpMod(2),
  inp: ModT,                    // tempUpMod(1),
  cMid0Front: CtrlLink,                 // mod.front.cMid0Front
  modFront: Node,                       // io.modFront
  tempModFrontPayload: ModT,    // io.tempModFrontPayload
  //myModMemWord: WordT,                // myModMemWord
  getMyRdMemWordFunc: (
    //UInt,
    Int,
    Int,
  ) => WordT,  // getMyRdMemWordFunc
  //Vec[WordT],  // myRdMemWord
  //ydx: Int,                             // ydx
) {
}
// A Read-Modify-Write pipelined BRAM
case class PipeMemRmw[
  WordT <: Data,
  HazardCmpT <: Data,
  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
](
  cfg: PipeMemRmwConfig[
    WordT,
    HazardCmpT,
    //ModT,
    //DualRdT,
  ],
  modType: HardType[ModT],
  dualRdType: HardType[DualRdT]=PipeMemRmwDualRdTypeDisabled[
    WordT, HazardCmpT,
  ](),
//(
//  wordType: HardType[WordT],
//  wordCountArr: Seq[Int],
//  hazardCmpType: HardType[HazardCmpT],
//  modType: HardType[ModT],
//  modRdPortCnt: Int,
//  modStageCnt: Int,
//  pipeName: String,
//  linkArr: Option[ArrayBuffer[Link]]=None,
//  memArrIdx: Int=0,
//  //memArrSize: Int=1,
//  //optDualRdType: Option[HardType[DualRdT]]=None,
//  dualRdType: HardType[DualRdT]=PipeMemRmwDualRdTypeDisabled[
//    WordT, HazardCmpT,
//  ](),
//  optDualRd: Boolean=false,
//  optReorder: Boolean=false,
//  //dualRdSize: Int=0,
//  init: Option[Seq[Seq[WordT]]]=None,
//  initBigInt: Option[Seq[Seq[BigInt]]]=None,
//  //forFmax: Boolean=false,
//  //optExtraCycleLatency: Boolean=false,
//  //optDisableModRd: Boolean=false,
//  //optEnableModDuplicate: Boolean=true,
//  optModHazardKind: PipeMemRmw.ModHazardKind=PipeMemRmw.ModHazardKind.Dupl,
//  //optModFwdToFront: Boolean=false,
//  //optRegFileRdPorts: Int=0,
//  optEnableClear: Boolean=false,
//  memRamStyle: String="auto",
//  vivadoDebug: Boolean=false,
//  //optLinkFrontToModFront: Boolean=true,
//  optIncludeModFrontS2MLink: Boolean=true,
//  optFormal: Boolean=false,
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
  doModInFrontFunc: Option[
    (
      ModT, // outp
      ModT, // inp
      CtrlLink, // mod.front.cFront
      //Vec[UInt], // upExt(1)(extIdxUp).memAddr
      //Int, // ydx
    ) => Area
  ]=None,
  //--------
  doModInModFrontFunc: Option[
    //[
    //  ModT <: Data,
    //]
    (
      PipeMemRmwDoModInModFrontFuncParams[
        WordT,
        HazardCmpT,
        ModT,
        DualRdT,
      ],
    ) => Area
  ]=None,
  //doFwdFunc: Option[
  //  (
  //    //ModT, //
  //    UInt, // stage index
  //    Vec[PipeMemRmwPayloadExt[WordT, HazardCmpT]], // myUpExtDel
  //    Int,  // zdx
  //  ) => WordT
  //]=None,
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
extends Area {
  //--------
  def wordType() = cfg.wordType()
  def wordCountArr = cfg.wordCountArr 
  def hazardCmpType() = cfg.hazardCmpType()
  //def modType() = cfg.modType()
  def modRdPortCnt = cfg.modRdPortCnt
  def modStageCnt = cfg.modStageCnt
  def pipeName = cfg.pipeName 
  def linkArr = cfg.linkArr
  def memArrIdx = cfg.memArrIdx 
  //def dualRdType() = cfg.dualRdType()
  def optDualRd = cfg.optDualRd
  def optReorder = cfg.optReorder
  def init = cfg.init 
  def initBigInt = cfg.initBigInt
  def optModHazardKind = cfg.optModHazardKind
  def optEnableClear = cfg.optEnableClear 
  def memRamStyle = cfg.memRamStyle 
  def vivadoDebug = cfg.vivadoDebug 
  def optIncludeModFrontStageLink = cfg.optIncludeModFrontStageLink
  def optIncludeModFrontS2MLink = cfg.optIncludeModFrontS2MLink
  if (optIncludeModFrontS2MLink) {
    assert(
      optIncludeModFrontStageLink
    )
  }
  def optFormal = cfg.optFormal
  //--------
  //def doHazardCmpFunc = cfg.doHazardCmpFunc
  //def doPrevHazardCmpFunc = cfg.doPrevHazardCmpFunc 
  //def doModInFrontFunc = cfg.doModInFrontFunc
  //def doModInModFrontFunc = cfg.doModInModFrontFunc
  //def reorderFtable = cfg.reorderFtable
  //--------
  def memArrSize = cfg.memArrSize
  def extIdxUp = PipeMemRmw.extIdxUp
  def extIdxSaved = PipeMemRmw.extIdxSaved
  def extIdxLim = PipeMemRmw.extIdxLim
  def extIdxSingle = PipeMemRmw.extIdxSingle
  //--------
  if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) {
    assert(memArrSize == 1)
  } else {
    assert(memArrSize > 0)
  }
  //--------
  //if (optModFwdToFront) {
  //  assert(optModHazardKind == PipeMemRmw.ModHazardKind.Fwd)
  //} else (
  //  //assert(optModHazardKind != PipeMemRmw.ModHazardKind.Fwd)
  //)
  //--------
  def debug: Boolean = {
    //GenerationFlags.formal {
    //  return true
    //}
    //return false
    optFormal
  }
  def wordCountMax = cfg.wordCountMax
  //var wordCountMax: Int = 0
  //for (ydx <- 0 until memArrSize) {
  //  if (wordCountMax < wordCountArr(ydx)) {
  //    wordCountMax = wordCountArr(ydx)
  //  }
  //}
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
    cfg=cfg,
    //wordType=wordType(),
    //wordCountMax=wordCountMax,
    //hazardCmpType=hazardCmpType(),
    modType=modType(),
    //modRdPortCnt=modRdPortCnt,
    //modStageCnt=modStageCnt,
    //pipeName=pipeName,
    //memArrIdx=memArrIdx,
    //memArrSize=memArrSize,
    dualRdType=dualRdType(),
    ////dualRdSize=dualRdSize,
    //optDualRd=optDualRd,
    //optReorder=optReorder,
    ////optEnableModDuplicate=optEnableModDuplicate,
    //optModHazardKind=optModHazardKind,
    //optEnableClear=optEnableClear,
    //optFormal=optFormal,
    //vivadoDebug=vivadoDebug,
  )
  //--------
  def mkMem(ydx: Int) = {
    val ret = Mem(
      wordType=wordType(),
      wordCount=wordCountArr(ydx),
    )
      .addAttribute("ram_style", memRamStyle)
    init match {
      case Some(myInit) => {
        //assert(myInit.size == wordCount)
        assert(initBigInt == None)
        ret.init(myInit(ydx))
      }
      case None => {
      }
    }
    initBigInt match {
      case Some(myInitBigInt) => {
        //assert(myInitBigInt.size == wordCount)
        assert(init == None)
        ret.initBigInt(myInitBigInt(ydx))
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
    optModHazardKind != PipeMemRmw.ModHazardKind.Dont
  ) generate {
    val myArr = new ArrayBuffer[Array[Mem[WordT]]]()
    for (ydx <- 0 until memArrSize) {
      myArr += (
      //Array.fill(memArrSize)(
        Array.fill(modRdPortCnt)(
          mkMem(ydx=ydx)
        )
      //)
      )
    }
    myArr
  }
  if (
    optFormal
  ) {
    if (
      optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
    ) {
      for (ydx <- 0 until memArrSize) {
        for (zdx <- 0 until modRdPortCnt) {
          for (idx <- 0 until wordCountArr(ydx)) {
            assumeInitial(
              modMem(ydx)(zdx).readAsync(
                address=U(s"${log2Up(wordCountArr(ydx))}'d${idx}")
              ) === (
                wordType().getZero
              )
            )
          }
        }
      }
    }
  }
  //val dualRdMemArr = new ArrayBuffer[Mem[WordT]]()
  //for (idx <- 0 until dualRdSize) {
  //  dualRdMemArr += mkMem()
  //}
  val dualRdMem = (io.optDualRd) generate {
    val myArr = new ArrayBuffer[Mem[WordT]]()
    //Array.fill(memArrSize)(
    //  mkMem()
    //)
    for (ydx <- 0 until memArrSize) {
      myArr += mkMem(ydx=ydx)
    }
    myArr
  }
  def memWriteIterate(
    writeFunc: (Mem[WordT]) => Unit,
    ydx: Int,
  ): Unit = {
    if (
      //optEnableModDuplicate
      optModHazardKind != PipeMemRmw.ModHazardKind.Dont
    ) {
      for (zdx <- 0 until modRdPortCnt) {
        writeFunc(modMem(ydx)(zdx))
      }
    }
    if (io.optDualRd) {
      writeFunc(dualRdMem(ydx))
    }
    //for (idx <- 0 until dualRdSize) {
    //  writeFunc(dualRdMemArr(idx))
    //}
  }
  def memWriteAll(
    address: Vec[UInt],
    data: Vec[WordT],
    enable: Vec[Bool]=null,
    //mask: Vec[Bits]=null,
  ): Unit = {
    assert(address.size == memArrSize)
    assert(data.size == memArrSize)
    assert(enable.size == memArrSize)
    for (ydx <- 0 until memArrSize) {
      memWriteIterate(
        writeFunc=(
          item: Mem[WordT],
          //ydx: Int,
        ) => {
          item.write(
            address=address(ydx)(
              PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
              downto 0
            ),
            data=data(ydx),
            enable=enable(ydx),
            //mask=mask(ydx),
          )
        },
        ydx=ydx
      )
    }
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
  //def mkExt(myVivadoDebug: Boolean=false) = {
  //  //val ret = Vec.fill(memArrSize)(
  //  //  Vec.fill(extIdxLim)(
  //  //    PipeMemRmwPayloadExt(
  //  //      wordType=wordType(),
  //  //      wordCount=wordCountMax,
  //  //      hazardCmpType=hazardCmpType(),
  //  //      modRdPortCnt=modRdPortCnt,
  //  //      modStageCnt=modStageCnt,
  //  //      //doModInModFront=doModInModFrontFunc match {
  //  //      //  case Some(myDoModSingleStageFunc) => true
  //  //      //  case None => false
  //  //      //},
  //  //      //optEnableModDuplicate=optEnableModDuplicate,
  //  //      optModHazardKind=optModHazardKind,
  //  //      optReorder=optReorder,
  //  //    )
  //  //  )
  //  //)
  //  //if (vivadoDebug && myVivadoDebug) {
  //  //  //ret.addAttribute("MARK_DEBUG", "TRUE")
  //  //  //ret.memAddr.addAttribute("MARK_DEBUG", "TRUE")
  //  //  //ret.hazardCmp.addAttribute("MARK_DEBUG", "TRUE")
  //  //  //if (optEnableModDuplicate) {
  //  //  //  ret.hazardId.addAttribute("MARK_DEBUG", "TRUE")
  //  //  //}
  //  //  //ret.modMemWord.addAttribute("MARK_DEBUG", "TRUE")
  //  //}
  //  //ret
  //  io.mkExt(
  //    myVivadoDebug=myVivadoDebug
  //  )
  //}
  def myHaveFwd = (
    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
  )
  def myHaveFormalFwd = (
    (
      optFormal
    ) && (
      //optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
      myHaveFwd
    )
  )
  def mkFwd(): PipeMemRmwFwd[
    WordT,
    HazardCmpT,
    //ModT,
    //DualRdT,
  ] = (
    PipeMemRmwFwd[
      WordT,
      HazardCmpT,
      //ModT,
      //DualRdT,
    ](
      //wordType=wordType(),
      //wordCount=wordCountMax,
      //hazardCmpType=hazardCmpType(),
      //modRdPortCnt=modRdPortCnt,
      //modStageCnt=modStageCnt,
      //memArrSize=memArrSize,
      //optModHazardKind=optModHazardKind,
      //optReorder=optReorder,
      ////myHaveFormalFwd=myHaveFormalFwd,
      cfg=cfg,
    )
    //(
    //  mkFwdOneExtFunc=(
    //    //mkFwdExt
    //    mkOneExt
    //  )
    //)
  )
  def mkOneExt(
    //myVivadoDebug: Boolean=false
  ): PipeMemRmwPayloadExt[
    WordT,
    HazardCmpT,
  ] = (
    PipeMemRmwPayloadExt(
      cfg=cfg,
      //wordType=wordType(),
      wordCount=wordCountMax,
      //hazardCmpType=hazardCmpType(),
      //modRdPortCnt=modRdPortCnt,
      //modStageCnt=modStageCnt,
      //memArrSize=memArrSize,
      //optModHazardKind=optModHazardKind,
      //optReorder=optReorder,
      ////myHaveFormalFwd=myHaveFormalFwd
    )
  )
  //def mkExt(
  //  //myHaveFormalFwd: Boolean,
  //  myVivadoDebug: Boolean=false,
  //) = {
  //  val ret = Vec.fill(memArrSize)(
  //    Vec.fill(PipeMemRmw.extIdxLim)(
  //      //PipeMemRmwPayloadExt(
  //      //  wordType=wordType(),
  //      //  wordCount=wordCountMax,
  //      //  hazardCmpType=hazardCmpType(),
  //      //  modRdPortCnt=modRdPortCnt,
  //      //  modStageCnt=modStageCnt,
  //      //  memArrSize=memArrSize,
  //      //  optModHazardKind=optModHazardKind,
  //      //  optReorder=optReorder,
  //      //  //myHaveFormalFwd=myHaveFormalFwd
  //      //)
  //      mkOneExt(
  //        myVivadoDebug=myVivadoDebug
  //      )
  //    )
  //  )
  //  if (vivadoDebug && myVivadoDebug) {
  //    //ret.addAttribute("MARK_DEBUG", "TRUE")
  //    //ret.memAddr.addAttribute("MARK_DEBUG", "TRUE")
  //    //ret.hazardCmp.addAttribute("MARK_DEBUG", "TRUE")
  //    //if (optEnableModDuplicate) {
  //    //  ret.hazardId.addAttribute("MARK_DEBUG", "TRUE")
  //    //}
  //    //ret.modMemWord.addAttribute("MARK_DEBUG", "TRUE")
  //  }
  //  ret
  //}
  def mkExt(
    //myVivadoDebug: Boolean=false
  ): Vec[Vec[PipeMemRmwPayloadExt[WordT, HazardCmpT]]] = {
    //myFwd.mkExt()

    val ret = Vec.fill(memArrSize)(
      Vec.fill(PipeMemRmw.extIdxLim)(
        //PipeMemRmwPayloadExt(
        //  wordType=wordType(),
        //  wordCount=wordCountMax,
        //  hazardCmpType=hazardCmpType(),
        //  modRdPortCnt=modRdPortCnt,
        //  modStageCnt=modStageCnt,
        //  memArrSize=memArrSize,
        //  optModHazardKind=optModHazardKind,
        //  optReorder=optReorder,
        //  //myHaveFormalFwd=myHaveFormalFwd
        //)
        mkOneExt(
          //myVivadoDebug
        )
      )
    )
    //if (vivadoDebug && myVivadoDebug) {
    //  //ret.addAttribute("MARK_DEBUG", "TRUE")
    //  //ret.memAddr.addAttribute("MARK_DEBUG", "TRUE")
    //  //ret.hazardCmp.addAttribute("MARK_DEBUG", "TRUE")
    //  //if (optEnableModDuplicate) {
    //  //  ret.hazardId.addAttribute("MARK_DEBUG", "TRUE")
    //  //}
    //  //ret.modMemWord.addAttribute("MARK_DEBUG", "TRUE")
    //}
    ret
  }
  //def mkFwdExt(
  //  //myHaveFormalFwd: Boolean,
  //  myVivadoDebug: Boolean=false,
  //) = {
  //  val ret = Vec.fill(memArrSize)(
  //    Vec.fill(PipeMemRmw.extIdxLim)(
  //      PipeMemRmwPayloadExt(
  //        wordType=wordType(),
  //        wordCount=wordCountMax,
  //        hazardCmpType=hazardCmpType(),
  //        modRdPortCnt=modRdPortCnt,
  //        modStageCnt=modStageCnt,
  //        memArrSize=memArrSize,
  //        optModHazardKind=optModHazardKind,
  //        optReorder=optReorder,
  //        //myHaveFormalFwd=myHaveFormalFwd
  //      )
  //    )
  //  )
  //  //if (vivadoDebug && myVivadoDebug) {
  //  //  //ret.addAttribute("MARK_DEBUG", "TRUE")
  //  //  //ret.memAddr.addAttribute("MARK_DEBUG", "TRUE")
  //  //  //ret.hazardCmp.addAttribute("MARK_DEBUG", "TRUE")
  //  //  //if (optEnableModDuplicate) {
  //  //  //  ret.hazardId.addAttribute("MARK_DEBUG", "TRUE")
  //  //  //}
  //  //  //ret.modMemWord.addAttribute("MARK_DEBUG", "TRUE")
  //  //}
  //  ret
  //}
  //def mkExt(
  //  myVivadoDebug: Boolean=false
  //) = mkExtRaw(
  //  myHaveFormalFwd=false,
  //  myVivadoDebug=myVivadoDebug,
  //)
  //def mkExtFormalFwd(
  //  myVivadoDebug: Boolean=false
  //) = mkExtRaw(
  //  myHaveFormalFwd=myHaveFormalFwd,
  //  myVivadoDebug=myVivadoDebug,
  //)
  val myFwd = (
  //(
  //  //myHaveFwd
  //) generate 
    (
      KeepAttribute(
        mkFwd()
        //myUpExtDel(0)(0)(0).fwd
      )
    )
  )
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
      Reg(UInt(PipeMemRmw.addrWidth(wordCount=wordCountMax) bits))
      init(0x0)
    )
    if (optReorder) {
      io.reorderCommitHead := rReorderCommitHead
    }
    //--------
    val front = new Area {
      def myHazardCmpFunc(
        curr: PipeMemRmwPayloadExt[WordT, HazardCmpT],
        prev: PipeMemRmwPayloadExt[WordT, HazardCmpT],
        ydx: Int,
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
        ydx: Int,
        zdx: Int,
        isPostDelay: Boolean,
        doValidCheck: Boolean=(
          //false
          true
        ),
        //down: Node,
        //extValidCond: Bool=False,
        //fireCnt: UInt,
        //idx: Int,
        //memAddr: UInt,
        forceFalse: Boolean=false,
      ) = (
        if (!forceFalse) {
        //(upExt(1).memAddr === prev.memAddr)
          (
            if (!isPostDelay) (
              (
                currMemAddr
                === prevMemAddr
              )
              && (
                if (doPrevHazardCmpFunc) (
                  myHazardCmpFunc(
                    curr/*upExt(1)*/,
                    prev,
                    ydx,
                    zdx,
                    isPostDelay
                  )
                ) else (
                  True
                )
              )
            ) else (
              myHazardCmpFunc(
                curr/*upExt(1)*/,
                prev,
                ydx,
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
              //curr.valid
              True
            )
            && 
            (
              prev.modMemWordValid
              //True
            ) && (
              if (doValidCheck) (
                prev.valid
              ) else (
                True
              )
            )
            //curr.valid && prev.valid
          )
        } else {
          False
        }
      )
      def inpPipePayload = io.frontPayload
      //val midPipePayloadArr = /*Array.fill(memArrSize)*/(Payload(modType()))
      //for (idx <- 0 until memArrSize) {
      //  midPipePayloadArr(idx)
      //  .setName(s"${pipeName}_${idx}_io_midPipePayloadArr")
      //}
      //def midPipePayload = midPipePayloadArr//(memArrIdx)
      val midPipePayload = {
        Payload(Vec.fill(2)(modType()))
        .setName(s"${pipeName}_io_midPipePayload")
      }
      //val outpPipePayload = Payload(modType())
      def outpPipePayload = io.modFrontPayload
      val myRdMemWord = Vec.fill(memArrSize)(
        Vec.fill(modRdPortCnt)(
          wordType()
        )
      )
      val myNonFwdRdMemWord = Vec.fill(memArrSize)(
        Vec.fill(modRdPortCnt)(
          wordType()
        )
      )
      //val myFwdRdMemWord = Vec.fill(memArrSize)(
      //  Vec.fill(modRdPortCnt)(
      //  //Vec.fill(extIdxLim)(
      //    wordType()
      //  //)
      //  )
      //)
      //val rRdMemWord1 = Reg(wordType()) init(myRdMemWord.getZero)
      //val dbgRdMemWord = (debug) generate (
      //  Payload(wordType())
      //)
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
          + 1 
          //+ (
          //  if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
          //    0
          //  ) else (
          //    -1
          //  )
          //)
          //- 1
        )(
          /*Reg*/(
            //Vec.fill(extIdxLim)(
              mkExt(
                //myVivadoDebug=true
              )
            //)
          )
          //init(mkExt().getZero)
        )
      )
      println(
        s"myUpExtDel.size: ${myUpExtDel.size}"
      )
      //--------
      //--------
      //val myUpExtDel2MemAddr = Vec.fill(
      //  myFwd.numMyUpExtDel2
      //)(
      //  Vec.fill(memArrSize)(
      //    Vec.fill(PipeMemRmw.extIdxLim)(
      //      //Vec.fill(modRdPortCnt)(
      //        /*Reg*/(
      //          UInt(PipeMemRmw.addrWidth(wordCount=wordCountMax) bits)
      //        )
      //        //init(0x0)
      //      //)
      //    )
      //  )
      //)
      val myUpExtDel2 = (
        KeepAttribute(
          //if (myHaveFormalFwd) (
            myFwd.myUpExtDel2
          //) else (
          //  Vec.fill(
          //    //PipeMemRmw.numPostFrontPreWriteStages
          //    ////PipeMemRmw.numPostFrontPreWriteStages
          //    //(
          //    //  //doModInModFront=doModInModFrontFunc match {
          //    //  //  case Some(myDoModSingleStageFunc) => true
          //    //  //  case None => false
          //    //  //},
          //    //  optModHazardKind=optModHazardKind,
          //    //  modStageCnt=modStageCnt,
          //    //)
          //    //+ 1
          //    ////+ (
          //    ////  if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
          //    ////    0
          //    ////  ) else (
          //    ////    -1
          //    ////  )
          //    ////)
          //    ////- 1
          //    PipeMemRmw.numMyUpExtDel2(
          //      optModHazardKind=optModHazardKind,
          //      modStageCnt=modStageCnt,
          //    )
          //  )(
          //    mkExt(myVivadoDebug=true)
          //  )
          //)
        )
        //.setName(s"${pipeName}_mod_front_myUpExtDel2")
      )
      println(
        s"myUpExtDel2.size: ${myUpExtDel2.size}"
      )
      //println(
      //  s"myUpExtDel2: ${myUpExtDel2.size}"
      //)
      for (idx <- 0 until myUpExtDel2.size) {
        for (ydx <- 0 until memArrSize) {
          for (extIdx <- 0 until extIdxLim) {
            val tempUpExt = myUpExtDel2(idx)(ydx)(extIdx)
            tempUpExt := (
              myUpExtDel(idx + 1)(ydx)(extIdx)
            )
            //if (idx == 0 && extIdx == extIdxUp) {
            //  tempUpExt.modMemWordValid.allowOverride
            //  //tempUpExt := (
            //  //  myUpExtDel2(idx)(ydx)(extIdx).getZero
            //  //)
            //  tempUpExt.modMemWordValid := False
            //}
          }
        }
      }
      val myUpExtDelFullFindFirstVecNotPostDelay = KeepAttribute(
        Vec.fill(memArrSize)(
          Vec.fill(modRdPortCnt)(
            Vec.fill(extIdxLim)(
              Vec.fill(myUpExtDel.size)(
                Bool()
              )
            )
          )
        )
      )
      if (
        (
          //optEnableModDuplicate
          optModHazardKind != PipeMemRmw.ModHazardKind.Dont
        )
        && modStageCnt > 0
      ) {
        //assert(modStageCnt > 0)
        for (
          //idx <- 0 until modStageCnt - 1
          idx <- 0 until io.midModStages.size
        ) {
          for (ydx <- 0 until memArrSize) {
            val myExt = /*Vec.fill(extIdxLim)*/(mkExt())
            for (extIdx <- 0 until extIdxLim) {
              io.midModStages(idx)(extIdx).getPipeMemRmwExt(
                outpExt=myExt(ydx)(extIdx),
                ydx=ydx,
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
                + (
                  if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
                    0
                  ) else (
                    1
                  )
                )
              )
              //println(
              //  s"io.midModStages.size=${io.midModStages.size} "
              //  + s"modStageCnt=${modStageCnt} "
              //  + s"idx=${idx} tempIdx=${tempIdx}"
              //)
              myUpExtDel(
                tempIdx
              )(ydx)(extIdx) := myExt(ydx)(extIdx)
            }
          }
        }
      }


      //val cFront = pipe.addStage(
      //  name=pipeName + "_Front",
      //  optIncludeS2M=(
      //    false
      //    //true
      //    //optModHazardKind != PipeMemRmw.ModHazardKind.Fwd
      //    //|| (
      //    //  doFwdFunc match {
      //    //    case Some(myDoFwdFunc) => false
      //    //    case None => true
      //    //  }
      //    //)
      //  )
      //)
      val cFront = CtrlLink(
        up=io.front,
        down={
          val temp = Node()
          temp.setName(s"${pipeName}_cFront_down")
          temp
        }
      )
        //.setName(s"${pipeName}_Front")
      myLinkArr += cFront
      val sFront = StageLink(
        up=cFront.down,
        down={
          val temp = Node()
          temp.setName(s"${pipeName}_sFront_down")
          temp
        },
      )
      myLinkArr += sFront
      //val s2mFront = (
      //  optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
      //) generate (
      //  S2MLink(
      //    up=sFront.down,
      //    down={
      //      val temp = Node()
      //      temp.setName(s"${pipeName}_s2mFront_down")
      //      temp
      //    },
      //  )
      //)
      //if (optModHazardKind == PipeMemRmw.ModHazardKind.Fwd) {
      //  myLinkArr += s2mFront
      //}
      //val cIoFront = DirectLink(
      //  up=io.front,
      //  down=cFront.up,
      //)
      //val cMid0Front = pipe.addStage(
      //  name=pipeName + "_Mid0Front",
      //  //optIncludeStage=(
      //  //  doModInModFrontFunc match {
      //  //    case Some(myDoModSingleStageFunc) => false
      //  //    case None => true
      //  //  }
      //  //),
      //  optIncludeS2M=(
      //    false // needed to ensure it works with a second `PipeMemRmw`
      //          // running in parallel with this pipeline stage,
      //          // such as the data cache of the Flare CPU
      //    //true
      //    //optModHazardKind != PipeMemRmw.ModHazardKind.Fwd
      //    //|| (
      //    //  doFwdFunc match {
      //    //    case Some(myDoFwdFunc) => false
      //    //    case None => true
      //    //  }
      //    //)
      //  ),
      //  //finish=true,
      //)
      val cMid0Front = CtrlLink(
        up=(
          //if (optLinkFrontToModFront) (
          //if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
            sFront.down
          //) else (
          //  s2mFront.down
          //)
          //) else (
          //  Node()
          //)
        ),
        down={
          //if (optIncludeModFrontS2MLink) (
          val temp = Node()
          temp.setName(s"${pipeName}_cMid0Front_down")
          temp
          //) else (
          //  io.modFront
          //)
        },
      )
      myLinkArr += cMid0Front
      val myIncludeS2mMid0Front = (
        optIncludeModFrontStageLink
        && optIncludeModFrontS2MLink
        && (
          optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
        )
        //false
      )
      val sMid0Front = (optIncludeModFrontStageLink) generate (
        StageLink(
          up=cMid0Front.down,
          down=(
            //Node()
            if (
              //(
              //  !optIncludeModFrontS2MLink
              //) && (
              //  optModHazardKind != PipeMemRmw.ModHazardKind.Fwd
              //)
              //true
              !myIncludeS2mMid0Front
            ) (
              io.modFront
            ) else {
              val temp = Node()
              temp.setName(s"${pipeName}_sMid0Front_down")
              temp
            }
          ),
        )
      )
      if (optIncludeModFrontStageLink) {
        //println(
        //  s"optIncludeModFrontS2MLink: ${optIncludeModFrontS2MLink}"
        //)
        myLinkArr += sMid0Front
      }
      val s2mMid0Front = (
        myIncludeS2mMid0Front
      ) generate (
        S2MLink(
          up=sMid0Front.down,
          down={
            //val temp = Node()
            //temp.setName(s"${pipeName}_s2mMid0Front_down")
            //temp
            io.modFront
          },
        )
      )
      if (myIncludeS2mMid0Front) {
        myLinkArr += s2mMid0Front
      }
      // lack of `s2mMid0Front` (which would have been an `S2MLink`):
      //  needed to ensure it works with a second `PipeMemRmw`
      //  running in parallel with this pipeline stage,
      //  such as the data cache of the Flare CPU


      val nextDidFwd = (
        (
          optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
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
          optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
          //optModFwdToFront
        ) generate (
          //Array.fill(2)(Payload(Bool()))
          RegNextWhen
          //RegNext
          (
            next=nextDidFwd,
            //cFront.down.isReady
            cond=cFront.down.isFiring
            ////cFront.down.isValid
            ////cMid0Front.up.isValid
          )
          //init(nextDidFwd.getZero)
        )
      )
      myRdMemWord := myNonFwdRdMemWord
      //for (zdx <- 0 until modRdPortCnt) {
      //  //myRdMemWord(zdx) := myNonFwdRdMemWord(zdx)
      //  //myRdMemWord(zdx) := (
      //  //  RegNext(myRdMemWord(zdx)) init(myRdMemWord(zdx).getZero)
      //  //)
      //  //when (RegNext(cFront.down.isFiring)) {
      //    myRdMemWord(zdx) := /*Mux*/(
      //      //!rDidFwd(zdx)(0),
      //      //RegNext(!nextDidFwd(zdx)(0)) init(False),
      //      //!(
      //      //  //RegNext(nextDidFwd(zdx)(0)) init(False)
      //      //  nextDidFwd(zdx)(0)
      //      //),
      //      myNonFwdRdMemWord(zdx),
      //      //myFwdRdMemWord(zdx)
      //      //Mux(
      //      //  (
      //      //    myUpExtDel(0)(extIdxUp).memAddr(zdx)
      //      //    === myUpExtDel.last(extIdxUp).memAddr(PipeMemRmw.modWrIdx)
      //      //  ),
      //      //  myUpExtDel.last(extIdxUp).modMemWord,
      //      //  Mux(
      //      //    myUpExtDel(0)(extIdxUp).memAddr(zdx)
      //      //    === myUpExtDel.last(extIdxSaved).memAddr(
      //      //      PipeMemRmw.modWrIdx
      //      //    ),
      //      //    myUpExtDel.last(extIdxSaved).modMemWord,
      //      //    myNonFwdRdMemWord(zdx)
      //      //  )
      //      //)
      //    )
      //  //}
      //}
      if (
        optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
        //optModFwdToFront
      ) {
        for (zdx <- 0 until rDidFwd.size) {
          for (idx <- 0 until rDidFwd(zdx).size) {
            rDidFwd(zdx)(idx).init(nextDidFwd(zdx)(idx).getZero)
          }
        }
      }
      //myLinkArr += cIoFront
      //val cMidMinus1Front = pipe.addStage(
      //  name=pipeName + "_MidMinus1Front",
      //  optIncludeS2M=false,
      //)
      //println(myUpExtDelFull.size)
      //--------
      val myUpExtDel2FindFirstVec = KeepAttribute(
        myFwd.myUpExtDel2FindFirstVec
        //Vec.fill(memArrSize)(
        //  Vec.fill(modRdPortCnt)(
        //    Vec.fill(extIdxLim)(
        //      Vec.fill(
        //        //myUpExtDelFull.size
        //        myUpExtDel2.size
        //        //+ (
        //        //  if (myUpExtDel2.size > 1) (
        //        //    - 1
        //        //  ) else (
        //        //    0
        //        //  )
        //        //)
        //        //- 1
        //        //+ (
        //        //  doModInModFrontFunc match {
        //        //    //case Some(myDoModSingleStageFunc) => 0
        //        //    //case None => -1
        //        //    case Some(myDoModSingleStageFunc) => 0
        //        //    case None => (
        //        //      -1
        //        //    )
        //        //  }
        //        //)
        //      )(
        //        Bool()
        //      )
        //    )
        //  )
        //)
      )
      //--------
      //val tempMyUpExtDelFrontFindFirstVec = KeepAttribute(
      //  Vec.fill(modRdPortCnt)(
      //    Vec.fill(1)(
      //      //Vec.fill(extIdxLim)(
      //        Bool()
      //      //)
      //    )
      //  )
      //)
      //--------
    }
    val back = new Area {
      //val dbgDoClear = (optEnableClear) generate (
      //  KeepAttribute(Bool())
      //)
      val dbgDoWrite = /*(debug) generate*/ (
        KeepAttribute(
          Vec.fill(memArrSize)(
            Bool()
          )
        )
      )
      val myWriteAddr = KeepAttribute(
        Vec.fill(memArrSize)(
          cloneOf(front.myUpExtDel(0)(0)(0).memAddr(PipeMemRmw.modWrIdx))
        )
      )
      val myWriteData = KeepAttribute(
        Vec.fill(memArrSize)(
          cloneOf(front.myUpExtDel(0)(0)(0).modMemWord)
        )
      )
      val myWriteEnable = KeepAttribute(
        Vec.fill(memArrSize)(
          Bool()
        )
      )
      if (vivadoDebug) {
        //myWriteAddr.addAttribute("MARK_DEBUG", "TRUE")
        //myWriteData.addAttribute("MARK_DEBUG", "TRUE")
        //myWriteEnable.addAttribute("MARK_DEBUG", "TRUE")
      }
      val pipe = PipeHelper(linkArr=myLinkArr)
      //val pipePayload = Payload(modType())
      def pipePayload = io.modBackPayload

      //val cBackPre = pipe.addStage(
      //)

      //val cModBack = pipe.addStage("ModBack")
      val cBack = pipe.addStage(
        name=pipeName + "_Back",
        //optIncludeStage=true,
        optIncludeS2M=(
          //doModInModFrontFunc match {
          //  case Some(myDoModSingleStageFunc) => false
          //  case None => true
          //}
          //true
          optModHazardKind != PipeMemRmw.ModHazardKind.Fwd
          //|| (
          //  doFwdFunc match {
          //    case Some(myDoFwdFunc) => false
          //    case None => true
          //  }
          //)
        )
      )

      val dIoModBack = DirectLink(
        up=io.modBack,
        down=cBack.up,
      )
      myLinkArr += dIoModBack
      val cLastBack = pipe.addStage(
        name=pipeName + "_LastBack",
        finish=true,
      )
      val dIoBack = DirectLink(
        up=(
          cLastBack.down
          //cBack.down
        ),
        down=io.back,
      )
      myLinkArr += dIoBack
      //val tempModBackPostUpExt = (
      //  Vec.fill(2)(mkExt())
      //)
      //val tempModBackPostUpMod = (
      //  Vec.fill(2)(
      //    Vec.fill(memArrSize)(
      //      modType()
      //    )
      //  )
      //)
      dIoBack.up(io.backPayload) := dIoBack.up(pipePayload)
      //for (ydx <- 0 until memArrSize) {
      //  //tempModBackPostUpMod(0)(ydx) := (
      //  //  dIoModBack.up(io.modBackPayload(ydx))
      //  //)
      //  //tempModBackPostUpMod(0)(ydx).getPipeMemRmwExt(
      //  //  outpExt=tempModBackPostUpExt(0)(ydx)(extIdxSingle),
      //  //  memArrIdx=memArrIdx,
      //  //)
      //  //when (ClockDomain.isResetActive) {
      //  //  tempModBackPostUpExt(1)(ydx)(extIdxSingle) := (
      //  //    tempModBackPostUpExt(0)(ydx)(extIdxSingle)
      //  //  )
      //  //  tempModBackPostUpExt(1)(ydx)(extIdxSingle).modMemWordValid := (
      //  //    False
      //  //  )
      //  //  //dIoModBack.up(pipePayload(ydx)) := 
      //  //  //dIoModBack.up(pipePayload(ydx)).setPipeMemRmwExt(
      //  //  //)
      //  //}
      //  //tempModBackPostUpMod(1)(ydx) := tempModBackPostUpMod(0)(ydx)
      //  //tempModBackPostUpMod(1)(ydx).allowOverride
      //  //tempModBackPostUpMod(1)(ydx).setPipeMemRmwExt(
      //  //  inpExt=tempModBackPostUpExt(1)(ydx)(extIdxSingle),
      //  //  memArrIdx=memArrIdx,
      //  //)
      //  dIoBack.up(io.backPayload(ydx)) := dIoBack.up(pipePayload(ydx))
      //  //when (ClockDomain.isResetActive) {
      //  //  cIoBack.up(io.backPayload(ydx)).myExt.modMemWordValid := False
      //  //}
      //}
      //pipe.first.up.driveFrom(io.modBack)(
      //  con=(node, payload) => {
      //    node(pipePayload) := payload 
      //  }
      //)
      val rTempWord = /*(debug) generate*/ KeepAttribute(
        Vec.fill(memArrSize)(
          Reg(
            dataType=wordType()
          )
        )
      )
      if (
        //debug
        true
      ) {
        for (ydx <- 0 until memArrSize) {
          rTempWord(ydx).init(rTempWord(ydx).getZero)
        }
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
    val upExt = (
      Vec.fill(2)(mkExt())
        .setName(s"${pipeName}_cFrontArea_upExt")
    )
    //if (myHaveFormalFwd) {
    //  for 
    //}
    //val upExtRealMemAddr = /*Vec.fill(extIdxLim)*/(
    //  cloneOf(upExt(1)(0).memAddr)
    //)
    //upExt(1) := upExt(0)
    val tempCond = KeepAttribute(Bool())
    for (ydx <- 0 until memArrSize) {
      for (extIdx <- 0 until extIdxLim) {
        upExt(1)(ydx)(extIdx) := (
          RegNext(
            next=upExt(1)(ydx)(extIdx),
            init=upExt(1)(ydx)(extIdx).getZero,
          )
        )
      }
    }
    //--------
    val tempHadActiveUpFire = Bool()
    upExt(1).allowOverride
    //--------

    val tempUpMod = Vec.fill(2)(
      //Vec.fill(extIdxLim)(
      //Vec.fill(memArrSize)(
        modType()
      //)
      //)
    )
    tempUpMod(0).allowOverride
    tempUpMod(0) := up(mod.front.inpPipePayload)
    for (ydx <- 0 until memArrSize) {
      //tempUpMod(0).allowOverride
      //tempUpMod(0) := up(mod.front.inpPipePayload(ydx))
      for (extIdx <- 0 until extIdxLim) {
        tempUpMod(0).getPipeMemRmwExt(
          outpExt=upExt(0)(ydx)(extIdx),
          ydx=ydx,
          memArrIdx=memArrIdx,
        )
      }
    }
    //--------
    //--------
    val nextHazardId = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      KeepAttribute(cloneOf(upExt(1)(0)(0).hazardId))
    )
    val rHazardId = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      KeepAttribute(
        RegNext(
          next=nextHazardId
        )
        init(
          //S(nextHazardId.getWidth bits, default -> True)

          //-1
          upExt(1)(0)(extIdxSaved).getHazardIdIdleVal()
        )
      )
    )
    if (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) {
      nextHazardId := rHazardId
      //when (isValid) {
          //upExt(1)(extIdxUp).hazardId := nextHazardId
      //} otherwise {
        //upExt(1).hazardId := rHazardId
      //}
    }
    //--------
    val hazardIdMinusOne = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
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
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      KeepAttribute(State())
    )
    val rState = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      KeepAttribute(RegNext(nextState) init(State.IDLE))
    )
    val rPrevStateWhen = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      KeepAttribute(
        RegNextWhen(
          next=rState,
          cond=down.isFiring
        )
        init(State.IDLE)
      )
    )
    val nextDidChangeState = (
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      KeepAttribute(Bool())
    )
    val rDidChangeState = (
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      KeepAttribute(
        RegNext(
          next=nextDidChangeState,
          init=True,
        )
      )
    )
    val rDidDelayItIdle = Reg(
      dataType=Bool(),
      init=False,
    )
    //val myUpExtDelFull = mod.front.myUpExtDelFull
    val myUpExtDelFullFindFirstVecNotPostDelay = (
      mod.front.myUpExtDelFullFindFirstVecNotPostDelay
    )
    for (ydx <- 0 until memArrSize) {
      for (zdx <- 0 until modRdPortCnt) {
        for (extIdx <- 0 until extIdxLim) {
          for (
            idx <- 0
            until
              myUpExtDelFullFindFirstVecNotPostDelay(ydx)(zdx)(extIdx).size
          ) {
            val tempMyUpExt = /*Mux*/(
              myUpExtDel(idx)(ydx)(extIdx)
            )
            myUpExtDelFullFindFirstVecNotPostDelay(
              ydx
            )(zdx)(extIdx)(idx) := (
              mod.front.findFirstFunc(
                currMemAddr=(
                  //upExtRealMemAddr/*(extIdx)*/(zdx)
                  upExt(1)(ydx)(extIdxUp).memAddr(zdx)
                ),
                prevMemAddr=tempMyUpExt.memAddr(zdx),
                curr=upExt(1)(ydx)(extIdxUp),
                prev=tempMyUpExt,
                ydx=ydx,
                zdx=zdx,
                isPostDelay=false,
                //idx=idx,
              )
            )
          }
        }
      }
    }

    val tempMyUpExtDelFindFirstNotPostDelay = (
      (
        //optEnableModDuplicate
        optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
      ) generate (
        Array.fill(memArrSize)(
          Array.fill(extIdxLim)(
            new ArrayBuffer[(Bool, UInt)]()
          )
        )
      )
    )
    if (optModHazardKind == PipeMemRmw.ModHazardKind.Dupl) {
      for (ydx <- 0 until memArrSize) {
        for (extIdx <- 0 until extIdxLim) {
          for (zdx <- 0 until modRdPortCnt) {
            tempMyUpExtDelFindFirstNotPostDelay(ydx)(extIdx) += {
              //def myFindFirstFunc(
              //  toCmp: Vec[Bool]
              //): Bool = (
              //  toCmp(0) === True
              //  || toCmp(1) === True
              //)
              val toAdd = (
                myUpExtDelFullFindFirstVecNotPostDelay(ydx)(zdx)(extIdx)
                .sFindFirst(
                  _ === True
                  //myFindFirstFunc(_)
                )
              )
              toAdd.setName(
                s"cFrontArea_tempMyUpExtDelFindFirstNotPostDelay"
                + s"_${ydx}_${zdx}_${extIdx}"
              )
              toAdd
            }
          }
        }
      }
    }
    val myNonFwdRdMemWord = mod.front.myNonFwdRdMemWord
    for (ydx <- 0 until memArrSize) {
      when (
        up.isValid
        //up.isFiring
      ) {
        upExt(1)(ydx)(extIdxUp) := upExt(0)(ydx)(extIdxSingle)
      }
      upExt(1)(ydx)(extIdxSaved) := (
        RegNextWhen(
          next=upExt(1)(ydx)(extIdxUp),
          cond=up.isFiring,
          init=upExt(1)(ydx)(extIdxSaved).getZero,
        )
      )
      if (optFormal) {
        when (pastValidAfterReset) {
          when (
            past(up.isFiring) init(False)
          ) {
            assert(
              upExt(1)(ydx)(extIdxSaved)
              === (
                past(upExt(1)(ydx)(extIdxUp))
                init(upExt(1)(ydx)(extIdxSaved).getZero)
              )
            )
          }
        }
      }

      upExt(1)(ydx)(extIdxUp).valid := up.isValid
      upExt(1)(ydx)(extIdxUp).ready := up.isReady
      upExt(1)(ydx)(extIdxUp).fire := up.isFiring
    }
    if ( 
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) {
      // BEGIN: stalling-based code; fix later
      nextState := rState
      switch (rState) {
        is (State.IDLE) {
          when (up.isValid) {
            for (ydx <- 0 until memArrSize) {
              upExt(1)(ydx)(extIdxSingle).hazardId := nextHazardId
              for (zdx <- 0 until modRdPortCnt) {
                val myUpCmp = KeepAttribute(
                  tempMyUpExtDelFindFirstNotPostDelay(ydx)(extIdxUp)(
                    zdx
                  )._1
                  && (
                    if (!doPrevHazardCmpFunc) (
                      mod.front.myHazardCmpFunc(
                        curr=upExt(1)(ydx)(extIdxUp),
                        prev=(
                          //mod.front.myUpExtDel(0)(ydx)(extIdxSaved)
                          mod.front.myUpExtDel(0)(ydx)(extIdxUp)
                        ),
                        ydx=ydx,
                        zdx=zdx,
                        isPostDelay=false,
                      )
                    ) else ( // if (doPrevHazardCmpFunc)
                      True
                    )
                  )
                )
                  .setName(s"${pipeName}_Dupl_myUpCmp_${ydx}_${zdx}")
                val mySavedCmp = KeepAttribute(
                  tempMyUpExtDelFindFirstNotPostDelay(ydx)(extIdxSaved)(
                    zdx
                  )._1
                  && (
                    if (!doPrevHazardCmpFunc) (
                      mod.front.myHazardCmpFunc(
                        curr=upExt(1)(ydx)(extIdxSaved),
                        prev=(
                          //mod.front.myUpExtDel(0)(ydx)(extIdxSaved)
                          mod.front.myUpExtDel(0)(ydx)(extIdxSaved)
                        ),
                        ydx=ydx,
                        zdx=zdx,
                        isPostDelay=false,
                      )
                    ) else ( // if (doPrevHazardCmpFunc)
                      True
                    )
                  )
                )
                  .setName(s"${pipeName}_Dupl_myDownCmp_${ydx}_${zdx}")
                when (
                  //Mux[Bool](
                  //  rPrevStateWhen === State.IDLE,
                  myUpCmp
                  || mySavedCmp
                ) {
                  rDidDelayItIdle := False
                  duplicateIt()
                  //nextDidChangeState := False
                  nextState := State.DELAY
                  nextHazardId := (
                    (
                      //--------
                      //// this should be `myUpExtDel.size` if
                      //// `myUpExtDel.size` is the number of stages strictly
                      //// after `cFront` and strictly before `cBack`
                      //// if including `cBack` in `myUpExtDel`, then it should
                      //// be `myUpExtDel.size - 1`
                      //--------
                      //--------
                      //--------
                      (
                        S(
                          s"${nextHazardId.getWidth}"
                          + s"'d${myUpExtDel.size + 2}"
                        )
                        - Cat(
                          U"3'd0", 
                          (
                            Mux(
                              myUpCmp,
                              tempMyUpExtDelFindFirstNotPostDelay
                                (ydx)(extIdxUp)(zdx)._2,
                              tempMyUpExtDelFindFirstNotPostDelay
                                (ydx)(extIdxSaved)(zdx)._2,
                            )
                          ),
                          //  tempMyUpExtDelFindFirstIsPostDelay._2,
                          //)
                        ).asSInt
                      )
                    )
                  )
                }
              }
            }
          }
        }
        is (State.DELAY) {
          when (down.isFiring) {
            nextHazardId := hazardIdMinusOne
          }
          //myStopIt := True
          //duplicateIt()
          when (nextHazardId.msb) {
            nextState := State.IDLE
          } otherwise {
            duplicateIt()
          }
        }
      }
      // END: stalling-based code; fix later
    } else if (
      //!optEnableModDuplicate
      //optModHazardKind != PipeMemRmw.ModHazardKind.Dupl
      optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
    ) {
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
    val myDoModInFrontAreaArr = new ArrayBuffer[Area]()
    //for (ydx <- 0 until memArrSize) {
      doModInFrontFunc match {
        case Some(myDoModInFrontFunc) => {
          myDoModInFrontAreaArr += (
            myDoModInFrontFunc(
              tempUpMod(1)/*(ydx)*/,
              tempUpMod(0)/*(ydx)*/,
              cFront,
              //ydx,
            )
            //.setName(s"${pipeName}_myDoModInFrontAreaArr_${ydx}")
            .setName(s"${pipeName}_myDoModInFrontAreaArr")
          )
        }
        case None => {
        }
      }
    //}

    val rSetRdId = Reg(
      dataType=Bool(),
      init=False,
    )
    //--------
    //val rDidFirstTempSharedEnable = (
    //  Reg(Bool())
    //  init(False)
    //)
    val tempSharedEnable = KeepAttribute(
      //down.isReady
      down.isFiring
    )
      .setName(s"${pipeName}_tempSharedEnable")
    tempCond := (
      (
        if (
          //optEnableModDuplicate
          optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
        ) (
          nextHazardId.msb
        ) else (
          True
        )
      )
      //--------
      && tempSharedEnable
      //--------
    )

    if (
      //optEnableModDuplicate
      //optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
      optModHazardKind != PipeMemRmw.ModHazardKind.Dont
    ) {
      if (optModHazardKind == PipeMemRmw.ModHazardKind.Dupl) {
        for (ydx <- 0 until memArrSize) {
          // BEGIN: previous `duplicateIt` code; fix later
          myNonFwdRdMemWord(ydx)(PipeMemRmw.modWrIdx) := (
            modMem(ydx)(PipeMemRmw.modWrIdx).readSync(
              address=(
                //upExtRealMemAddr(PipeMemRmw.modWrIdx)
                upExt(1)(ydx)(extIdxUp).memAddr(PipeMemRmw.modWrIdx)(
                  PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
                  downto 0
                )
              ),
              //address=myDownExt.memAddr,
              enable=(
                tempCond
              ),
            )
          )
        }
        // END: previous `duplicateIt` code; fix later
      } else { // if (optModHazardKind == PipeMemRmw.ModHazardKind.Fwd)
        for (ydx <- 0 until memArrSize) {
          for (zdx <- 0 until modRdPortCnt) {
            myNonFwdRdMemWord(ydx)(zdx) := modMem(ydx)(zdx).readSync(
              address=(
                //upExtRealMemAddr(zdx)
                upExt(1)(ydx)(extIdxUp).memAddr(zdx)(
                  PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
                  downto 0
                )
              ),
              enable=(
                //tempCond
                //!mod.front.nextDidFwd(zdx)(0)
                //&& 
                tempSharedEnable
                //down.isReady
              ),
            )
          }
        }
      }
    }
    val tempCondDown = (
      down.isFiring
    )
    //--------
    //--------
    //--------
    tempUpMod(1) := (
      RegNext(
        next=tempUpMod(1),
        init=tempUpMod(1).getZero,
      )
    )
    tempUpMod(1) := tempUpMod(0)
    tempUpMod(1).allowOverride
    for (ydx <- 0 until memArrSize) {
      //for (extIdx <- 0 until extIdxLim) {
      //}
      //when (up.isFiring) {
        upExt(1)(ydx)(extIdxUp).memAddrFwd.allowOverride
        upExt(1)(ydx)(extIdxUp).memAddrFwd := (
          upExt(1)(ydx)(extIdxUp).memAddr
        )
        tempUpMod(1).setPipeMemRmwExt(
          inpExt=upExt(1)(ydx)(extIdxUp),
          ydx=ydx,
          memArrIdx=memArrIdx,
        )
      //}
      if (ydx == 0) {
        for (idx <- 0 until up(mod.front.midPipePayload).size) {
          up(mod.front.midPipePayload)(idx) := tempUpMod(1)
        }
      }
      //--------
      //val rIsFiringCnt = (debug) generate (
      //  Reg(UInt(8 bits)) init(0x0)
      //)
      //--------
      if (optModHazardKind == PipeMemRmw.ModHazardKind.Dupl) {
        upExt(1)(ydx)(extIdxUp).hazardId := nextHazardId
      }
    }
  }
  val cMid0Front = mod.front.cMid0Front
  val cMid0FrontArea = new cMid0Front.Area {
    //--------
    val dbgUpIsValid = KeepAttribute(
      cMid0Front.up.isValid
    )
      .setName(s"${pipeName}_cMid0FrontArea_dbgUpIsValid")
    val dbgUpIsReady = KeepAttribute(
      cMid0Front.up.isReady
    )
      .setName(s"${pipeName}_cMid0FrontArea_dbgUpIsReady")
    val dbgUpIsFiring = KeepAttribute(
      cMid0Front.up.isFiring
    )
      .setName(s"${pipeName}_cMid0FrontArea_dbgUpIsFiring")
    val dbgDownIsValid = KeepAttribute(
      cMid0Front.down.isValid
    )
      .setName(s"${pipeName}_cMid0FrontArea_dbgDownIsValid")
    val dbgDownIsReady = KeepAttribute(
      cMid0Front.down.isReady
    )
      .setName(s"${pipeName}_cMid0FrontArea_dbgDownIsReady")
    val dbgDownIsFiring = KeepAttribute(
      cMid0Front.down.isFiring
    )
      .setName(s"${pipeName}_cMid0FrontArea_dbgDownIsFiring")
    //--------
    //--------
    //val upFwd = (
    //  myHaveFormalFwd
    //) generate (
    //  Vec.fill(2)(mkFwd())
    //  .setName(s"${pipeName}_cMid0FrontArea_upFwd")
    //)
    val upExt = Vec.fill(3)(
      mkExt()
    ).setName(s"${pipeName}_cMid0FrontArea_upExt")
    for (ydx <- 0 until memArrSize) {
      for (extIdx <- 0 until extIdxLim) {
        upExt(0)(ydx)(extIdx) := (
          RegNext(
            next=upExt(0)(ydx)(extIdx),
            init=upExt(0)(ydx)(extIdx).getZero,
          )
        )
        upExt(1)(ydx)(extIdx) := (
          RegNext(
            next=upExt(1)(ydx)(extIdx),
            init=upExt(1)(ydx)(extIdx).getZero,
          )
        )
        upExt(2)(ydx)(extIdx) := (
          RegNext(
            next=upExt(2)(ydx)(extIdx),
            init=upExt(2)(ydx)(extIdx).getZero,
          )
        )
      }
      //upExt(1)(ydx) := upExt(0)(ydx)
      upExt(0)(ydx).allowOverride
      upExt(1)(ydx).allowOverride
      upExt(2)(ydx).allowOverride
    }
    val nextPrevTxnWasHazardVec = (
      //(PipeMemRmwSimDut.doAddrOneHaltIt) generate (
        KeepAttribute(
          Vec.fill(memArrSize)(
            Bool()
          )
        )
        .setName(s"${pipeName}_nextPrevTxnWasHazardVec")
      //)
    )
    val rPrevTxnWasHazardVec = (
      //(PipeMemRmwSimDut.doAddrOneHaltIt) generate (
        KeepAttribute(
          RegNextWhen/*RegNext*/(
            next=nextPrevTxnWasHazardVec,
            cond=up.isFiring,
          )
          //init(False)
          //init(nextPrevTxnWasHazardVec.getZero)
        )
        .setName(s"${pipeName}_rPrevTxnWasHazardVec")
      //)
    )
    val nextPrevTxnWasHazardAny = (
      KeepAttribute(
        Bool()
      )
      .setName(s"${pipeName}_nextPrevTxnWasHazardAny")
    )
    val rPrevTxnWasHazardAny = (
      KeepAttribute(
        //Reg(Bool()) init(False)
        RegNextWhen/*RegNext*/(
          next=nextPrevTxnWasHazardAny,
          cond=up.isFiring,
          init=False,
        )
      )
      .setName(s"${pipeName}_rPrevTxnWasHazardAny")
    )
    nextPrevTxnWasHazardAny := (
      nextPrevTxnWasHazardVec.sFindFirst(
        _ === True
      )._1
    )
    for (ydx <- 0 until memArrSize) {
      rPrevTxnWasHazardVec(ydx).init(nextPrevTxnWasHazardVec(ydx).getZero)
      nextPrevTxnWasHazardVec(ydx) := rPrevTxnWasHazardVec(ydx)
      for (extIdx <- 0 until extIdxLim) {
        myUpExtDel(0)(ydx)(extIdx).valid.allowOverride
        myUpExtDel(0)(ydx)(extIdx).ready.allowOverride
        myUpExtDel(0)(ydx)(extIdx).fire.allowOverride
      }
      when (
        up.isValid
      ) {
        for (extIdx <- 0 until extIdxLim) {
          upExt(1)(ydx)(extIdx) := upExt(0)(ydx)(extIdx)
        }
        for (zdx <- 0 until modRdPortCnt) {
          upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
            RegNext(
              next=upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx),
              init=upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx).getZero,
            )
          )
        }
        //upExt(1)(ydx)(extIdxSingle).modMemWord := upExt(2)(ydx)(extIdxUp).modMemWord
      }
      myUpExtDel(0)(ydx)(extIdxUp) := (
        upExt(2)(ydx)(extIdxUp)
      )
      myUpExtDel(0)(ydx)(extIdxSaved) := (
        RegNextWhen(
          //upExt(2)(ydx)(extIdxUp)
          //upExt(2)(ydx)(extIdxSaved)
          next=myUpExtDel(0)(ydx)(extIdxUp),
          cond=up.isFiring,
          init=myUpExtDel(0)(ydx)(extIdxSaved).getZero,
        )
      )

      upExt(1)(ydx)(extIdxSingle).valid := up.isValid
      upExt(1)(ydx)(extIdxSingle).ready := up.isReady
      upExt(1)(ydx)(extIdxSingle).fire := up.isFiring
      myUpExtDel(0)(ydx)(extIdxUp).valid := upExt(2)(ydx)(extIdxUp).valid
      myUpExtDel(0)(ydx)(extIdxUp).ready := upExt(2)(ydx)(extIdxUp).ready
      myUpExtDel(0)(ydx)(extIdxUp).fire := upExt(2)(ydx)(extIdxUp).fire
      //upExt(1)(ydx) := upExt(0)(ydx)
    }

    val tempUpMod = (
      Vec.fill(3)(
        //Vec.fill(extIdxLim)(
        //Vec.fill(memArrSize)(
          modType()
        //)
        //)
      )
      .setName(s"${pipeName}_cMid0FrontArea_tempUpMod")
    )
    //tempUpMod(2) := (
    //  RegNext(tempUpMod(2))
    //  init(tempUpMod(2).getZero)
    //)
    tempUpMod(2) := (
      RegNext(
        next=tempUpMod(2),
        init=tempUpMod(2).getZero,
      )
    )
    tempUpMod(2).allowOverride
    tempUpMod(0) := up(mod.front.midPipePayload)(0)
    val tempUpMod0a = modType() 
    tempUpMod0a := up(mod.front.midPipePayload)(1)
    for (ydx <- 0 until memArrSize) {
      //tempUpMod(2) := (
      //  RegNext(
      //    next=tempUpMod(2),
      //    init=tempUpMod(2).getZero,
      //  )
      //)
      //tempUpMod(2).allowOverride
      //tempUpMod(0) := up(mod.front.midPipePayload(ydx))
      for (extIdx <- 0 until extIdxLim) {
        val myMod = (
          if (extIdx == 0) {
            tempUpMod(0)
          } else {
            tempUpMod0a
          }
        )
        myMod.getPipeMemRmwExt(
          outpExt=upExt(0)(ydx)(extIdx),
          ydx=ydx,
          memArrIdx=memArrIdx,
        )
      }
    }
    val myRdMemWord = mod.front.myRdMemWord

    val rSetRdId = Reg(
      dataType=Bool(),
      init=False,
    )
    val tempMyUpExtDelFrontFindFirstV2d = KeepAttribute(
      Vec.fill(modRdPortCnt)(
        Vec.fill(1)(
          //Vec.fill(extIdxLim)(
            mkExt()
          //)
        )
      )
      .setName(
        s"${pipeName}_cMid0FrontArea_tempMyUpExtDelFrontFindFirstV2d"
      )
    )
    for (ydx <- 0 until memArrSize) {
      for (zdx <- 0 until modRdPortCnt) {
        for (idx <- 0 until mod.front.myUpExtDel2.size) {
          for (extIdx <- 0 until extIdxLim) {
            //if (zdx == 0) {
            //  val tempMemAddr = (
            //    mod.front.myUpExtDel2MemAddr(idx)(ydx)(extIdx)
            //  )
            //  if (idx == 0) {
            //    tempMemAddr := (
            //      mod.front.myUpExtDel2(idx)(ydx)(extIdx).memAddr(
            //        PipeMemRmw.modWrIdx
            //      )(
            //        PipeMemRmw.addrWidth(
            //          wordCount=wordCountArr(ydx)
            //        ) - 1
            //          downto 0
            //        )
            //      //upExt(1)(ydx)(extIdxSingle).memAddr(zdx)
            //    )
            //  } else {
            //    tempMemAddr := (
            //      RegNext(tempMemAddr)
            //      init(0x0)
            //    )
            //    when (
            //      //if (idx == 1) (
            //      //  down.isFiring
            //      //) else (
            //      //  up.isFiring
            //      //)
            //    ) {
            //      val tempMemAddr1 = (
            //        //mod.front.myUpExtDel2MemAddr(idx - 1)(ydx)(extIdx)
            //        mod.front.myUpExtDel2(idx - 1)(ydx)(extIdx).memAddr(
            //          PipeMemRmw.modWrIdx
            //        )(
            //          PipeMemRmw.addrWidth(
            //            wordCount=wordCountArr(ydx)
            //          ) - 1
            //          downto 0
            //        )
            //      )
            //      tempMemAddr := (
            //        RegNext(tempMemAddr1)
            //        init(0x0)
            //      )
            //    }
            //  }
            //}
            mod.front.myUpExtDel2FindFirstVec(ydx)(zdx)(extIdx)(idx) := (
              //(
              //  if (zdx == PipeMemRmw.modWrIdx) (
              //    True
              //  ) else (
              //    False
              //  )
              //) && 
              (
                mod.front.findFirstFunc(
                  currMemAddr=(
                    //if (extIdx == PipeMemRmw.extIdxUp) (
                      upExt(1)(ydx)(extIdx).memAddr(zdx)(
                        (
                          PipeMemRmw.addrWidth(
                            wordCount=wordCountArr(ydx)
                          ) - 1
                          downto 0
                        )
                      )
                    //) else (
                    //  upExt(1)(ydx)(extIdx).memAddrFwd(zdx)(
                    //    (
                    //      PipeMemRmw.addrWidth(
                    //        wordCount=wordCountArr(ydx)
                    //      ) - 1
                    //      downto 0
                    //    )
                    //  )
                    //)
                  ),
                  prevMemAddr=(
                    //if (idx > 0) {
                    //  mod.front.myUpExtDel2MemAddr(idx)(ydx)(extIdx)(
                    //    PipeMemRmw.addrWidth(
                    //      wordCount=wordCountArr(ydx)
                    //    ) - 1
                    //    downto 0
                    //  )
                    //} else {
                      mod.front.myUpExtDel2(idx)(ydx)(extIdx).memAddrFwd(
                        PipeMemRmw.modWrIdx
                      )(
                        PipeMemRmw.addrWidth(
                          wordCount=wordCountArr(ydx)
                        ) - 1
                        downto 0
                      )
                    //}
                  ),
                  curr=(
                    upExt(1)(ydx)(
                      //extIdxSingle
                      extIdx
                    )
                  ),
                  prev=(
                    //mod.front.myUpExtDel2(idx)(ydx)
                    //tempMyUpExtDel2
                    //mod.front.myUpExtDel2(idx)(ydx)
                    mod.front.myUpExtDel2(idx)(ydx)(extIdx)
                    //mod.front.myUpExtDel(idx + 1)
                  ),
                  ydx=ydx,
                  zdx=zdx,
                  isPostDelay=false,
                  doValidCheck=false,
                  forceFalse=(
                    //idx == 0 && extIdx == extIdxUp
                    false
                  )
                )
              )
            )
          }
        }
      }
    }
    //--------
    //println(myUpExtDel.size)
    //when (up.isFiring) {
    //  upExt(1).rdMemWord := myRdMemWord
    //}
    for (ydx <- 0 until memArrSize) {
      //GenerationFlags.formal {
        //when (
        //  //past(cMid0Front.up.isFiring) init(False)
        //  io.modFront.isValid
        //) {
        //  //for (zdx <- 0 until upExt(1)(ydx)(extIdxSingle).rdMemWord.size) {
        //    val tempMyExt = mkExt(myVivadoDebug=true)
        //    //io.tempModFrontPayload
        //    io.tempModFrontPayload(ydx).getPipeMemRmwExt(
        //      outpExt=tempMyExt(ydx)(extIdxUp),
        //      memArrIdx=memArrIdx,
        //    )
        //    for (zdx <- 0 until modRdPortCnt) {
        //      assert(
        //        (
        //          RegNextWhen(
        //            //inp.myExt.rdMemWord(ydx),
        //            upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx),
        //            up.isFiring,
        //          )
        //          init(upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx).getZero)
        //        ) === (
        //          tempMyExt(ydx)(extIdxSingle).rdMemWord(zdx)
        //          //io.tempModFrontPayload.myExt.rdMemWord(ydx)
        //          //myRdMemWord(ydx)
        //        )
        //      )
        //    }
        //  //}
        //}
      //}
      when (
        //RegNext(up.isReady)
        RegNext(
          next=cFrontArea.tempSharedEnable,
          init=False,
        )
      ) {
        upExt(1)(ydx)(extIdxSingle).rdMemWord := myRdMemWord(ydx)
        //upExt(2)(ydx)(extIdxUp).rdMemWord := myRdMemWord(ydx)
      }
      upExt(1)(ydx)(extIdxSingle).modMemWordValid := True
      //upExt(2)(ydx)(extIdxUp).rdMemWord := (
      //  upExt(1)(ydx)(extIdxSingle).rdMemWord
      //)

    }
    val myDoModInModFrontAreaArr = new ArrayBuffer[Area]()
    doModInModFrontFunc match {
      case Some(myDoModInModFrontFunc) => {
        //assert(modStageCnt == 0)
        myDoModInModFrontAreaArr += (
          myDoModInModFrontFunc(
            PipeMemRmwDoModInModFrontFuncParams(
              pipeMemIo=io,
              nextPrevTxnWasHazardVec=nextPrevTxnWasHazardVec,
              rPrevTxnWasHazardVec=rPrevTxnWasHazardVec,
              rPrevTxnWasHazardAny=rPrevTxnWasHazardAny,
              outp=tempUpMod(2),
              inp=tempUpMod(1),
              cMid0Front=cMid0Front,
              modFront=io.modFront,
              tempModFrontPayload=io.tempModFrontPayload,
              getMyRdMemWordFunc=(
                //someUpExtIdx: UInt,
                someYdx: Int,
                someModIdx: Int,
              ) => (
                //upExt(1)(someYdx)(extIdxSingle).rdMemWord(
                //  PipeMemRmw.modWrIdx
                //)
                upExt(
                  2
                )(someYdx)(extIdxSingle).rdMemWord(
                  //PipeMemRmw.modWrIdx
                  someModIdx
                )
              ),
              //myRdMemWord,
              //ydx=ydx,                      // ydx
            )
          )
            .setName(s"${pipeName}_myDoModInModFrontAreaArr")
        )
      }
      case None => {
        assert(
          modStageCnt > 0
          || !optIncludeModFrontStageLink
        )
        //tempUpMod(2)(ydx) := tempUpMod(1)(ydx)
        //tempUpMod(2)(ydx) := tempUpMod(1)(ydx)
        tempUpMod(2) := tempUpMod(1)
      }
    }
    //for (ydx <- 0 until memArrSize) {
    //  upExt(2)(ydx)(extIdxSingle).valid := up.isValid
    //  upExt(2)(ydx)(extIdxSingle).ready := up.isReady
    //  upExt(2)(ydx)(extIdxSingle).fire := up.isFiring
    //}
    //def myHaveFwd = (
    //  mod.front.myHaveFwd
    //)
    //val myFindFirst_0 = (
    //  myHaveFwd
    //) generate (
    //  KeepAttribute(
    //    Vec.fill(memArrSize)(
    //      Vec.fill(modRdPortCnt)(
    //        Vec.fill(extIdxLim)(
    //          Bool()
    //        )
    //      )
    //    )
    //  )
    //  .setName(s"${pipeName}_myFindFirst_0")
    //)
    //val myFindFirst_1 = (
    //  myHaveFwd
    //) generate (
    //  KeepAttribute(
    //    Vec.fill(memArrSize)(
    //      Vec.fill(modRdPortCnt)(
    //        Vec.fill(extIdxLim)(
    //          UInt(log2Up(mod.front.myUpExtDel2.size) bits)
    //        )
    //      )
    //    )
    //  )
    //  .setName(s"${pipeName}_myFindFirst_1")
    //)
    val myFindFirst_0 = (
      myHaveFwd
    ) generate (
      myFwd.myFindFirst_0
    )
    val myFindFirst_1 = (
      myHaveFwd
    ) generate (
      myFwd.myFindFirst_1
    )

    //if (io.myHaveFormalFwd) {
    //  //mod.front.myFwd.myUpExtDel2 := (
    //  //  mod.front.myUpExtDel2
    //  //)
    //  //mod.front.myFwd.myUpExtDel2FindFirstVec := (
    //  //  mod.front.myUpExtDel2FindFirstVec
    //  //)
    //  up(io.myFormalFwdPayload) := mod.front.myFwd
    //}
    val doFwd = (myHaveFwd) generate (
      PipeMemRmwDoFwdArea(
        fwdAreaName=s"${pipeName}_cMid0FrontArea_doFwd",
        fwd=myFwd,
        setToMyFwdDataFunc=(
          ydx: Int,
          zdx: Int,
          myFwdData: WordT,
        ) => {
          upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
            myFwdData
          )
        },
        optFirstFwdRdMemWord=Some(
          //upExt(2)
          myRdMemWord
        ),
      )
    )
    val doFormalFwdSavedMyFwd = (myHaveFormalFwd) generate (
      KeepAttribute(
        RegNextWhen(
          next=myFwd,
          cond=up.isFiring,
          init=myFwd.getZero,
        )
      )
      .setName(s"${pipeName}_doFormalFwdSavedMyFwd")
    )
    if (myHaveFormalFwd) {
      when (pastValidAfterReset) {
        when (
          past(up.isFiring) init(False)
        ) {
          assert(
            doFormalFwdSavedMyFwd
            === past(myFwd)
          )
        } otherwise {
          assert(
            stable(doFormalFwdSavedMyFwd)
          )
        }
      }
    }
    val doFormalFwdSaved = (myHaveFormalFwd) generate (
      PipeMemRmwDoFwdArea(
        fwdAreaName=s"${pipeName}_cMid0FrontArea_doFwdFormalSaved",
        fwd=doFormalFwdSavedMyFwd,
        setToMyFwdDataFunc=(
          ydx: Int,
          zdx: Int,
          myFwdData: WordT,
        ) => {
          //upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
          //  myFwdData
          //)
          val myTempUpExt2 = (
            upExt(2)(ydx)(extIdxUp)//.rdMemWord(zdx)
          )
          val rTempSavedUpExt2 = (
            RegNextWhen(
              next=myTempUpExt2,
              cond=up.isFiring,
              init=myTempUpExt2.getZero,
            )
          )
          when (pastValidAfterReset) {
            assert(
              rTempSavedUpExt2.rdMemWord(zdx)
              === (
                myFwdData
              )
            )
            when (
              past(up.isFiring) init(False)
            ) {
              assert(
                rTempSavedUpExt2.main
                === (
                  past(myTempUpExt2.main)
                  //init(myTempSavedRdMemWord.getZero)
                )
              )
            }
          }
        },
        //firstFwd=false,
      )
    )
    //if (myHaveFormalFwd) {
    //  when (pastValidAfterReset) {
    //    //when (
    //    //  
    //    //  && !RegNextWhen(
    //    //    next=True,
    //    //    cond=up.isValid,
    //    //    init=False,
    //    //  )
    //    //) {
    //    //  assert(
    //    //  )
    //    //}
    //    when (
    //      !RegNextWhen(
    //        next=True,
    //        cond=up.isFiring,
    //        init=False,
    //      )
    //    ) {
    //      when (!up.isValid) {
    //        for (ydx <- 0 until memArrSize) {
    //          assert(
    //            upExt(2)(ydx)(extIdxUp).main
    //            === upExt(2)(ydx)(extIdxUp).main.getZero
    //          )
    //        }
    //        assert(
    //          upFwd(extIdxUp)
    //          === upFwd(extIdxUp).getZero
    //        )
    //      }
    //      for (ydx <- 0 until memArrSize) {
    //        assert(
    //          upExt(1)(ydx)(extIdxSaved).main
    //          === upExt(1)(ydx)(extIdxSaved).main.getZero
    //        )
    //      }
    //      assert(
    //        upFwd(extIdxSaved)
    //        === upFwd(extIdxSaved).getZero
    //      )
    //    } 
    //    //when (!past(up.isValid) init(False)) {
    //    //  assert(
    //    //    upFwd
    //    //  )
    //    //}
    //    when (
    //      !up.isValid
    //      && !past(up.isValid)
    //    ) {
    //      for (ydx <- 0 until memArrSize) {
    //        assert(
    //          stable(upExt(1)(ydx)(extIdxUp).main)
    //        )
    //        assert(
    //          stable(upExt(1)(ydx)(extIdxSaved).main)
    //        )
    //      }
    //      assert(
    //        stable(upFwd(extIdxUp))
    //      )
    //      assert(
    //        stable(upFwd(extIdxSaved))
    //      )
    //    }
    //    when (
    //      past(up.isFiring) init(False)
    //    ) {
    //      for (ydx <- 0 until memArrSize) {
    //        assert(
    //          upExt(1)(ydx)(extIdxSaved)
    //          === (
    //            past(upExt(1)(ydx)(extIdxUp))
    //            init(upExt(1)(ydx)(extIdxUp).getZero)
    //          )
    //        )
    //      }
    //      assert(
    //        upFwd(extIdxSaved)
    //        === (
    //          past(upFwd(extIdxUp))
    //          init(upFwd(extIdxUp).getZero)
    //        )
    //      )
    //    }
    //  }
    //}
    //if (myHaveFwd) {
    //  //for (ydx <- 0 until memArrSize) {
    //  //  for (zdx <- 0 until modRdPortCnt) {
    //  //    val myFindFirstUp = KeepAttribute(
    //  //      (optModHazardKind == PipeMemRmw.ModHazardKind.Fwd) generate (
    //  //        mod.front.myUpExtDel2FindFirstVec(ydx)(zdx)(extIdxUp)
    //  //        .sFindFirst(
    //  //          _ === True
    //  //        )
    //  //      )
    //  //      .setName(s"${pipeName}_myFindFirstUp_${ydx}_${zdx}")
    //  //    )
    //  //    val myFindFirstSaved = KeepAttribute(
    //  //      (optModHazardKind == PipeMemRmw.ModHazardKind.Fwd) generate (
    //  //        mod.front.myUpExtDel2FindFirstVec(ydx)(zdx)(extIdxSaved)
    //  //        .sFindFirst(
    //  //          _ === True
    //  //        )
    //  //      )
    //  //      .setName(s"${pipeName}_myFindFirstDown_${ydx}_${zdx}")
    //  //    )
    //  //    myFindFirst_0(ydx)(zdx)(extIdxUp) := (
    //  //      myFindFirstUp._1
    //  //    )
    //  //    myFindFirst_1(ydx)(zdx)(extIdxUp) := (
    //  //      myFindFirstUp._2
    //  //    )
    //  //    myFindFirst_0(ydx)(zdx)(extIdxSaved) := (
    //  //      myFindFirstSaved._1
    //  //    )
    //  //    myFindFirst_1(ydx)(zdx)(extIdxSaved) := (
    //  //      myFindFirstSaved._2
    //  //    )
    //  //    val myFwdCondUp = (
    //  //      KeepAttribute(
    //  //        myFindFirstUp._1
    //  //      )
    //  //      .setName(s"${pipeName}_myFwdCondUp_${ydx}_${zdx}")
    //  //    )
    //  //    val myFwdCondSaved = (
    //  //      KeepAttribute(
    //  //        myFindFirstSaved._1
    //  //      )
    //  //      .setName(s"${pipeName}_myFwdCondDown_${ydx}_${zdx}")
    //  //    )
    //  //    val myFwdDataUp = (
    //  //      KeepAttribute(
    //  //        mod.front.myUpExtDel2(myFindFirstUp._2)(ydx)(
    //  //          extIdxUp
    //  //        ).modMemWord
    //  //      )
    //  //      .setName(s"${pipeName}_myFwdDataUp_${ydx}_${zdx}")
    //  //    )
    //  //    val myFwdDataSaved = (
    //  //      KeepAttribute(
    //  //        mod.front.myUpExtDel2(myFindFirstSaved._2)(ydx)(
    //  //          extIdxSaved
    //  //        ).modMemWord
    //  //      )
    //  //      .setName(s"${pipeName}_myFwdDataDown_${ydx}_${zdx}")
    //  //    )
    //  //    if (
    //  //      //optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
    //  //      myHaveFwd
    //  //    ) {
    //  //      def setToMyFwdUp(): Unit = {
    //  //        upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
    //  //          myFwdDataUp
    //  //        )
    //  //      }
    //  //      def setToMyFwdSaved(): Unit = {
    //  //        upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
    //  //          myFwdDataSaved
    //  //        )
    //  //      }
    //  //      def innerFunc(): Unit = {
    //  //        when (
    //  //          myFwdCondUp
    //  //        ) {
    //  //          setToMyFwdUp()
    //  //        } elsewhen (
    //  //          myFwdCondSaved
    //  //        ) {
    //  //          setToMyFwdSaved()
    //  //        } 
    //  //      }
    //  //      when (
    //  //        myFwdCondUp
    //  //        && myFwdCondSaved
    //  //      ) {
    //  //        when (myFindFirstUp._2 < myFindFirstSaved._2) {
    //  //          setToMyFwdUp()
    //  //        } elsewhen (myFindFirstSaved._2 < myFindFirstUp._2) {
    //  //          setToMyFwdSaved()
    //  //        } otherwise {
    //  //          innerFunc()
    //  //        }
    //  //      } otherwise {
    //  //        innerFunc()
    //  //      }
    //  //      //when (myFwdCondUp) {
    //  //      //  setToMyFwdUp()
    //  //      //}
    //  //    }
    //  //  }
    //  //}
    //}
    //--------
    val myDbgUpIsValid = KeepAttribute(
      up.isValid
    )
      .setName(s"${pipeName}_cMid0FrontArea_myDbgUpIsValid")
    tempUpMod(1) := tempUpMod(0)
    tempUpMod(1).allowOverride
    for (ydx <- 0 until memArrSize) {
      //tempUpMod(1)(ydx) := tempUpMod(0)(ydx)
      //tempUpMod(1)(ydx).allowOverride
      tempUpMod(1).setPipeMemRmwExt(
        inpExt=upExt(1)(ydx)(extIdxSingle),
        ydx=ydx,
        memArrIdx=memArrIdx,
      )
      tempUpMod(2).getPipeMemRmwExt(
        outpExt=upExt(2)(ydx)(extIdxUp),
        ydx=ydx,
        memArrIdx=memArrIdx,
      )
      if (
        myHaveFormalFwd
        && (ydx == 0)
      ) {
        tempUpMod(2).formalSetPipeMemRmwFwd(
          inpFwd=myFwd,
          memArrIdx=memArrIdx,
        )
      }
      //--------
      //up(mod.front.outpPipePayload) := RegNextWhen(
      //  next=tempUpMod(2),
      //  cond=myDbgUpIsValid,
      //  init=tempUpMod(2).getZero,
      //)
      //--------
      //upExt(3)(ydx) := upExt(2)(ydx)
      //tempUpMod(3)(ydx).setPipeMemRmwExt(
      //  inpExt=upExt(3)
      //)
        //up(mod.front.outpPipePayload(ydx)) := tempUpMod(2)(ydx)
      if (ydx == 0) {
        //up(mod.front.outpPipePayload) := RegNextWhen(
        //  next=tempUpMod(2),
        //  cond=myDbgUpIsValid,
        //  init=tempUpMod(2).getZero,
        //)
        //up(mod.front.outpPipePayload) := RegNext(
        //  next=tempUpMod(2),
        //  //cond=myDbgUpIsValid,
        //  init=tempUpMod(2).getZero,
        //)
        //when (myDbgUpIsValid) 
        //val myTempPayload = 
        //up(mod.front.outpPipePayload) := (
        //  up(mod.front.outpPipePayload)
        //)
        val myTempUpMod = modType()
        myTempUpMod := RegNext(
          //tempUpMod(2)
          next=myTempUpMod,
          init=myTempUpMod.getZero,
        )
        up(mod.front.outpPipePayload) := tempUpMod(2)//myTempUpMod
        //when (up.isFiring) {
        //  myTempUpMod := tempUpMod(2)
        //  // := tempUpMod(2)
        //}
        if (optFormal) {
          when (pastValidAfterReset) {
            when (past(up.isValid)) {
            }
          }
        }
      }
    }
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
    val upFwd = Vec.fill(
      //2
      extIdxLim
      //1
    )(
      mkFwd(
        //myVivadoDebug=true
      )
    ).setName(s"${pipeName}_cBackArea_upFwd")
    //--------
    val upExt = Vec.fill(
      2
      //1
    )(
      mkExt(
        //myVivadoDebug=true
      )
    ).setName(s"${pipeName}_cBackArea_upExt")
    //--------
    val tempUpMod = Vec.fill(3)(
      //Vec.fill(extIdxLim)(
      //Vec.fill(memArrSize)(
        modType()
      //)
      //)
    ).setName(s"${pipeName}_cBackArea_tempUpMod")
    //--------
    for (ydx <- 0 until memArrSize) {
      for (extIdx <- 0 until extIdxLim) {
        //--------
        upExt(1)(ydx)(extIdx) := (
          RegNext(
            next=upExt(1)(ydx)(extIdx),
            init=upExt(1)(ydx)(extIdx).getZero,
          )
        )
        upExt(1)(ydx)(extIdx).allowOverride
        //--------
        //--------
      }
      if (ydx == 0) {
        upFwd(extIdxUp) := (
          RegNext(
            next=upFwd(extIdxUp),
            init=upFwd(extIdxUp).getZero,
          )
        )
        upFwd(extIdxUp).allowOverride
        upFwd(extIdxSaved) := (
          RegNextWhen(
            next=upFwd(extIdxUp),
            cond=up.isFiring,
            init=upFwd(extIdxSaved).getZero,
          )
        )
      }
      when (
        up.isValid
        //up.isFiring
      ) {
        upExt(1)(ydx)(extIdxUp) := upExt(0)(ydx)(extIdxSingle)
      }
      if (ydx == 0) {
        if (myHaveFormalFwd) {
          tempUpMod(0).formalGetPipeMemRmwFwd(
            outpFwd=upFwd(extIdxUp),
            memArrIdx=memArrIdx,
          )
        }
      }
      //--------
      upExt(1)(ydx)(extIdxSaved) := (
        RegNextWhen(
          next=upExt(1)(ydx)(extIdxUp),
          cond=up.isFiring,
          init=upExt(1)(ydx)(extIdxSaved).getZero,
        )
      )
      upExt(1)(ydx)(extIdxUp).valid := up.isValid
      upExt(1)(ydx)(extIdxUp).ready := up.isReady
      upExt(1)(ydx)(extIdxUp).fire := up.isFiring
      when (
        !upExt(1)(ydx)(extIdxUp).modMemWordValid
      ) {
        upExt(1)(ydx)(extIdxUp).valid := False
      }
    }
    if (myHaveFormalFwd) {
      when (pastValidAfterReset) {
        //when (
        //  
        //  && !RegNextWhen(
        //    next=True,
        //    cond=up.isValid,
        //    init=False,
        //  )
        //) {
        //  assert(
        //  )
        //}
        when (
          !RegNextWhen(
            next=True,
            cond=up.isFiring,
            init=False,
          )
        ) {
          when (!up.isValid) {
            for (ydx <- 0 until memArrSize) {
              assert(
                upExt(1)(ydx)(extIdxUp).main
                === upExt(1)(ydx)(extIdxUp).main.getZero
              )
            }
            assert(
              upFwd(extIdxUp)
              === upFwd(extIdxUp).getZero
            )
          }
          for (ydx <- 0 until memArrSize) {
            assert(
              upExt(1)(ydx)(extIdxSaved).main
              === upExt(1)(ydx)(extIdxSaved).main.getZero
            )
          }
          assert(
            upFwd(extIdxSaved)
            === upFwd(extIdxSaved).getZero
          )
        } 
        //when (!past(up.isValid) init(False)) {
        //  assert(
        //    upFwd
        //  )
        //}
        //when (
        //  !up.isValid
        //  && !past(up.isValid)
        //) {
        //  for (ydx <- 0 until memArrSize) {
        //    assert(
        //      stable(upExt(1)(ydx)(extIdxUp).main)
        //    )
        //    assert(
        //      stable(upExt(1)(ydx)(extIdxSaved).main)
        //    )
        //  }
        //  assert(
        //    stable(upFwd(extIdxUp))
        //  )
        //  assert(
        //    stable(upFwd(extIdxSaved))
        //  )
        //}
        when (
          past(up.isFiring) init(False)
        ) {
          for (ydx <- 0 until memArrSize) {
            assert(
              upExt(1)(ydx)(extIdxSaved)
              === (
                past(upExt(1)(ydx)(extIdxUp))
                init(upExt(1)(ydx)(extIdxUp).getZero)
              )
            )
          }
          assert(
            upFwd(extIdxSaved)
            === (
              past(upFwd(extIdxUp))
              init(upFwd(extIdxUp).getZero)
            )
          )
        }
      }
    }
    //--------
    def tempMyUpExtDelPenLast = (
      myUpExtDel(
        myUpExtDel.size
        - (
          if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
            2
          ) else (
            1
          )
        )
      )
      //myUpExtDel(myUpExtDel.size - 2)
      //myUpExtDel.last
    )
    if (
      true
    ) {
      tempMyUpExtDelPenLast := upExt(1)
    }
    if (
      //myUpExtDel.size - 2 >= 0
      true
    ) {
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

    for (ydx <- 0 until memArrSize) {
      if (ydx == 0) {
        tempUpMod(0).allowOverride
        tempUpMod(0) := up(mod.back.pipePayload)
      }
      tempUpMod(0).getPipeMemRmwExt(
        outpExt=upExt(0)(ydx)(extIdxSingle),
        ydx=ydx,
        memArrIdx=memArrIdx,
      )
      if (ydx == 0) {
        //tempUpMod(1) := RegNextWhen(
        //  next=tempUpMod(0),
        //  cond=up.isValid,
        //  init=tempUpMod(1).getZero,
        //)
        //when (up.isValid) {
          tempUpMod(1) := tempUpMod(0)
        //}
        tempUpMod(1).allowOverride
      }
      tempUpMod(1).setPipeMemRmwExt(
        inpExt=upExt(1)(ydx)(extIdxUp),
        ydx=ydx,
        memArrIdx=memArrIdx,
      )
    }
    val dbgDoWrite = mod.back.dbgDoWrite
    if (
      //debug
      true
    ) {
      for (ydx <- 0 until memArrSize) {
        dbgDoWrite(ydx) := False
      }
    }
    val extDbgDoWriteCond = (
      Vec.fill(memArrSize)(
        Bool()
      )
    )
    for (ydx <- 0 until memArrSize) {
      extDbgDoWriteCond(ydx) := (
        (
          if (
            //optEnableModDuplicate
            optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
          ) (
            upExt(0)(ydx)(extIdxSingle).hazardId.msb
          ) else (
            True
          )
        )
      )
      //|| (
      //  if (optEnableClear) (
      //    io.clear.valid
      //  ) else (
      //    False
      //  )
      //)
      //)
    }
    val myWriteAddr = mod.back.myWriteAddr
    //myWriteAddr := (
    //  RegNext(myWriteAddr) init(myWriteAddr.getZero)
    //)
    //when (up.isValid) {
    for (ydx <- 0 until memArrSize) {
      if (optEnableClear) {
        when (io.clear.fire) {
          myWriteAddr(ydx) := (
            io.clear.payload
          )
        } otherwise { // when (!io.clear.fire)
          myWriteAddr(ydx) := (
            upExt(0)(ydx)(extIdxSingle).memAddr(
              PipeMemRmw.modWrIdx
            ).resized
          )
        }
      } else { // if (!optEnableClear)
        myWriteAddr(ydx) := (
          upExt(0)(ydx)(extIdxSingle).memAddr(
            PipeMemRmw.modWrIdx
          ).resized
        )
      }
      //myWriteAddr(ydx) := (
      //  if (optEnableClear) (
      //    Mux[UInt](
      //      io.clear.fire,
      //      io.clear.payload,
      //      upExt(0)(ydx)(extIdxSingle).memAddr(PipeMemRmw.modWrIdx)(
      //        PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
      //        downto 0
      //      ),
      //    )
      //  ) else (
      //    upExt(0)(ydx)(extIdxSingle).memAddr(PipeMemRmw.modWrIdx)(
      //      PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
      //      downto 0
      //    )
      //  )
      //)
    }
    //}
    val myWriteData = mod.back.myWriteData
    //myWriteData := (
    //  RegNext(myWriteData) init(myWriteData.getZero)
    //)
    //when (up.isValid) {
    for (ydx <- 0 until memArrSize) {
      myWriteData(ydx) := (
        //upExt(0)(ydx).modMemWord
        if (optEnableClear) (
          Mux[WordT](
            io.clear.fire,
            wordType().getZero,
            upExt(0)(ydx)(extIdxSingle).modMemWord,
          )
        ) else (
          upExt(0)(ydx)(extIdxSingle).modMemWord
        )
      )
    }
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

    for (ydx <- 0 until memArrSize) {
      myWriteEnable(ydx) := (
        (
          dbgDoWrite(ydx)
          || (
            if (optEnableClear) (
              io.clear.fire
            ) else (
              False
            )
          )
        )
        && !ClockDomain.isResetActive
        && upExt(1)(ydx)(extIdxUp).modMemWordValid
        //&& up.isValid
        //&& down.isReady
      )
    }
    //else {
    //  myUpExtDel(myUpExtDel.size - 1) := upExt(1)
    //}
    
    for (ydx <- 0 until memArrSize) {
      when (
        //!clockDomain.isResetActive

        //&& isValid
        //&& up.isFiring
        //&& 
        //up.isValid
        //(
        //  if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
            //up.isFiring
            //down.isFiring
            up.isValid
            //|| (
            //  RegNextWhen(True, up.isValid)
            //  init(False)
            //)
        //  ) else (
        //    up.isValid
        //  )
        //)
        //&& (
        //  //if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
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
        && extDbgDoWriteCond(ydx)
      ) {
        if (
          //debug
          true
        ) {
          mod.back.rTempWord(ydx) := (
            upExt(0)(ydx)(extIdxSingle).modMemWord
          )
          dbgDoWrite(ydx) := True
        }
        //modMem.write(
        //  address=upExt(0).memAddr,
        //  data=upExt(0).modMemWord,
        //)
      }
    }
    //for (ydx <- 0 until memArrSize) {
    //  if (optReorder) {
    //    when (
    //      myWriteEnable
    //      && myWriteAddr === io.reorderCommitHead
    //      && getReorderValid(myWriteData)
    //      && !getReorderBusyEtc(myWriteData)
    //      && upExt(0)(extIdxSingle).reqReorderCommit
    //    ) {
    //      mod.rReorderCommitHead := mod.rReorderCommitHead
    //      upExt(1)(extIdxUp).didReorderCommit := True
    //    }
    //  }
    //}
    memWriteAll(
      address=myWriteAddr,
      data=myWriteData,
      enable=myWriteEnable,
    )
    //--------
  }
  val cLastBack = mod.back.cLastBack
  val cLastBackArea = new cLastBack.Area {
    val upExt = Vec.fill(2)(
      mkExt()
    )
    for (ydx <- 0 until memArrSize) {
      for (extIdx <- 0 until extIdxLim) {
        upExt(1)(ydx)(extIdx) := (
          RegNext(
            next=upExt(1)(ydx)(extIdx),
            init=upExt(1)(ydx)(extIdx).getZero,
          ) 
        )
        upExt(1)(ydx)(extIdx).allowOverride
      }
      when (
        //up.isFiring
        up.isValid
      ) {
        upExt(1)(ydx)(extIdxUp) := upExt(0)(ydx)(extIdxSingle)
      }
      //val tempHadActiveUpFire = Bool()
      //when (
      //  //down.isFiring
      //  tempHadActiveUpFire 
      //) {
      //  upExt(1)(ydx)(extIdxSaved) := /*RegNext*/(upExt(1)(ydx)(extIdxUp))
      //}
      upExt(1)(ydx)(extIdxSaved) := (
        RegNextWhen(
          next=upExt(1)(ydx)(extIdxUp),
          cond=up.isFiring,
          init=upExt(1)(ydx)(extIdxSaved).getZero,
        )
      )
      for (extIdx <- 0 until extIdxLim) {
        upExt(1)(ydx)(extIdx).modMemWordValid := False
      }
      //--------
      if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) {
        mod.front.myUpExtDel.last(ydx) := upExt(1)(ydx)
      }
    }

    val tempUpMod = Vec.fill(2)(
      //Vec.fill(memArrSize)(
        modType()
      //)
    )
    for (ydx <- 0 until memArrSize) {
      if (ydx == 0) {
        tempUpMod(0).allowOverride
        tempUpMod(0) := up(mod.back.pipePayload)
      }
      tempUpMod(0).getPipeMemRmwExt(
        outpExt=upExt(0)(ydx)(extIdxSingle),
        ydx=ydx,
        memArrIdx=memArrIdx,
      )
      //upExt(1)(ydx)(extIdxSaved).modMemWordValid := False
      //tempUpMod(1)(ydx).allowOverride
      //tempUpMod(1)(ydx) := tempUpMod(0)(ydx)
      //tempUpMod(1)(ydx).setPipeMemRmwExt(
      //  inpExt=upExt(1)(ydx)(extIdxSingle),
      //  memArrIdx=memArrIdx,
      //)
      //up(io.backPayload(ydx)) := tempUpMod(1)(ydx)
    }
  }
  //--------
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
        false
        //true
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
      val myInpDualRd = /*Vec.fill(memArrSize)*/(
        dualRdType()
      )
      //val myMidDualRd = dualRdType()
      val myInpUpExt = mkExt()
      //val myMidUpExt = mkExt()
      //if (vivadoDebug) {
      //  myInpDualRd.addAttribute("MARK_DEBUG", "TRUE")
      //  //myOutpDualRd.addAttribute("MARK_DEBUG", "TRUE")
      //}

      for (ydx <- 0 until memArrSize) {
        if (ydx == 0) {
          myInpDualRd := up(inpPipePayload)
        }
        myInpDualRd.getPipeMemRmwExt(
          outpExt=myInpUpExt(ydx)(extIdxSingle),
          ydx=ydx,
          memArrIdx=memArrIdx,
        )
      }
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
      for (ydx <- 0 until memArrSize) {
        myRdMemWord := dualRdMem(ydx).readSync(
          address=(
            myInpUpExt(ydx)(extIdxSingle).memAddr(PipeMemRmw.modWrIdx)(
              PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
              downto 0
            )
          ),
          enable=up.isFiring,
        )
      }
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
      val myInpDualRd = /*Vec.fill(memArrSize)*/(
        dualRdType()
      )
      val myOutpDualRd = /*Vec.fill(memArrSize)*/(
        dualRdType()
      )
      val myInpUpExt = mkExt()
      val myOutpUpExt = mkExt()
      //if (vivadoDebug) {
      //  myInpDualRd.addAttribute("MARK_DEBUG", "TRUE")
      //  myOutpDualRd.addAttribute("MARK_DEBUG", "TRUE")
      //}

      for (ydx <- 0 until memArrSize) {
        if (ydx == 0) {
          myInpDualRd := up(inpPipePayload)
        }
        myInpDualRd.getPipeMemRmwExt(
          outpExt=myInpUpExt(ydx)(extIdxSingle),
          ydx=ydx,
          memArrIdx=memArrIdx,
        )
        myOutpUpExt(ydx) := myInpUpExt(ydx)
        myOutpUpExt(ydx).allowOverride
        if (ydx == 0) {
          myOutpDualRd := myInpDualRd
          myOutpDualRd.allowOverride
        }
        myOutpDualRd.setPipeMemRmwExt(
          inpExt=myOutpUpExt(ydx)(extIdxSingle),
          ydx=ydx,
          memArrIdx=memArrIdx,
        )
        //up(midPipePayload) := myOutpDualRd
        //when (up.isFiring) {
        //}
        val rDoIt = Reg(
          dataType=Bool(),
          init=False,
        ) 
        myOutpUpExt(ydx)(extIdxSingle).rdMemWord(PipeMemRmw.modWrIdx) := (
          RegNext(
            next=(
              myOutpUpExt(ydx)(extIdxSingle).rdMemWord(PipeMemRmw.modWrIdx)
            ),
            init=(
              myOutpUpExt(ydx)(extIdxSingle).rdMemWord(
                PipeMemRmw.modWrIdx
              ).getZero
            ),
          )
        )
        myOutpUpExt(ydx)(extIdxSingle).modMemWord := (
          myOutpUpExt(ydx)(extIdxSingle).rdMemWord(PipeMemRmw.modWrIdx)
        )
        if (ydx == 0) {
          up(outpPipePayload) := myOutpDualRd
        }
        when (
          up.isValid
          && !rDoIt
        ) {
          rDoIt := True
          myOutpUpExt(ydx)(extIdxSingle).rdMemWord(
            PipeMemRmw.modWrIdx
          ) := (
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
//  ) /*extends Interface*/ = {
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
//) extends Interface {
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
//) extends Interface {
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
//) extends Interface with PipeMemRmwPayloadBase[WordT, HazardCmpT] {
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
  cfg: PipeMemRmwConfig[
    WordT,
    HazardCmpT,
  ],
  ////wordWidth: Int,
  //wordType: HardType[WordT],
  wordCount: Int,
  //hazardCmpType: HardType[HazardCmpT],
  //modRdPortCnt: Int,
  //modStageCnt: Int,
  //memArrSize: Int,
  //optModHazardKind: PipeMemRmw.ModHazardKind,
  ////doModInModFront: Boolean/*=false*/,
  //optReorder: Boolean=false,
) extends Bundle with PipeMemRmwPayloadBase[WordT, HazardCmpT] {
  //--------
  val myExt = PipeMemRmwPayloadExt(
    cfg=cfg,
    //wordType=wordType(),
    wordCount=wordCount,
    //hazardCmpType=hazardCmpType(),
    //modRdPortCnt=modRdPortCnt,
    //modStageCnt=modStageCnt,
    //memArrSize=memArrSize,
    //optModHazardKind=optModHazardKind,
    ////doModInModFront=doModInModFront,
    //optReorder=optReorder,
  )
  //--------
  /*override*/ def setPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
    myExt := ext
  }
  /*override*/ def getPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
    ext := myExt
  }
  //--------
  /*override*/ def formalSetPipeMemRmwFwd(
    outpFwd: PipeMemRmwFwd[WordT, HazardCmpT],
    memArrIdx: Int,
  ): Unit = {
  }

  /*override*/ def formalGetPipeMemRmwFwd(
    inpFwd: PipeMemRmwFwd[WordT, HazardCmpT],
    memArrIdx: Int,
  ): Unit = {
  }
  //def optFormalFwdFuncs(
  //): Option[PipeMemRmwPayloadBaseFormalFwdFuncs[WordT, HazardCmpT]] = None
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
//) extends Interface {
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
////) extends Interface with IMasterSlave {
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
//) extends Interface {
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
//) extends Interface /*with IMasterSlave*/ {
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
//    //case class DbgUp() extends Interface {
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
//) extends Interface {
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
//  //val io = new Interface {
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
