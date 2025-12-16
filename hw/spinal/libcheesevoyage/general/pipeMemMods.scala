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
](
  wordType: HardType[WordT],
  wordCountArr: Seq[Int],
  hazardCmpType: HardType[HazardCmpT],
  //modType: HardType[ModT],
  modRdPortCnt: Int,
  modStageCnt: Int,
  pipeName: String,
  optIncludePreMid0Front: Boolean,
  var linkArr: Option[ArrayBuffer[Link]]=None,
  memArrIdx: Int=0,
  optDualRd: Boolean=false,
  optReorder: Boolean=false,
  init: Option[Seq[Seq[WordT]]]=None,
  initBigInt: Option[Seq[Seq[BigInt]]]=None,
  optIncludeOtherMmw: Boolean=false,
  optModHazardKind: PipeMemRmw.ModHazardKind=PipeMemRmw.ModHazardKind.Dupl,
  //optFwdUseMmwValidLaterStages: Boolean=false,
  optFwdHaveZeroReg: Option[Int]=Some(0x0),
  optEnableClear: Boolean=false,
  memRamStyleAltera: String="MLAB",
  memRamStyleXilinx: String="auto",
  memRwAddrCollisionXilinx: String="",
  vivadoDebug: Boolean=false,
  optIncludeModFrontStageLink: Boolean=true,
  optIncludeModFrontS2MLink: Boolean=true,
  optFormal: Boolean=false,
  numForkJoin: Int=1,
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
  def numMyUpExtDel2 = (
    PipeMemRmw.numMyUpExtDel2(
      optModHazardKind=optModHazardKind,
      modStageCnt=modStageCnt,
      optIncludePreMid0Front=optIncludePreMid0Front,
    )
    //cfg.numMyUpExtDel2
  )
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

  val modMemWordValidSize: Int = (
    PipeMemRmw.modMemWordValidSize
    //modRdPortCnt
    //1
    //2
  )
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
  val valid = /*KeepAttribute*/(Vec.fill(
    //PipeMemRmw.modMemWordValidSize
    //4
    1
  )(
    Bool()
  ))
  val ready = /*KeepAttribute*/(Bool())
  val fire = /*KeepAttribute*/(Bool())
}
case class PipeMemRmwPayloadExtMainMemAddr[
  WordT <: Data,
  HazardCmpT <: Data
](
  cfg: PipeMemRmwConfig[
    WordT,
    HazardCmpT,
  ],
  wordCount: Int,
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
  def numMyUpExtDel2 = cfg.numMyUpExtDel2
  val memAddrFwdMmw = Vec.fill(modRdPortCnt)(
    Vec.fill(numMyUpExtDel2)(
      UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
    )
  )
  val memAddrFwd = Vec.fill(modRdPortCnt)(
    Vec.fill(numMyUpExtDel2)(
      UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
    )
  )
  val memAddrFwdCmp = Vec.fill(modRdPortCnt)(
    Vec.fill(numMyUpExtDel2)(
      UInt(1 bits)
      //Bool()
    )
  )
  val memAddr = Vec.fill(modRdPortCnt + 1)(
    UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
  )
  val memAddrAlt = Vec.fill(modRdPortCnt)(
    UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
  )
}
case class PipeMemRmwPayloadExtMainNonMemAddrMost[
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
  val fwdIdx = Vec.fill(modRdPortCnt)(
    UInt(log2Up(cfg.numMyUpExtDel2 + 1) bits)
  )
  val otherModMemWord = (
    //cfg.optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
    cfg.optIncludeOtherMmw
  ) generate (
    wordType()
  )
  val modMemWordValid = (
    Vec.fill(cfg.modMemWordValidSize)(
      Bool()
    )
  )
  val rdMemWord = Vec.fill(modRdPortCnt)(wordType())
  val joinIdx = UInt(log2Up(cfg.numForkJoin) bits)
  val fwdCanDoIt = Vec.fill(
    modRdPortCnt
  )(
    Bool()
  )
  // hazard for when an address is already in the pipeline 
  val hazardId = (
    optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
  ) generate (
      SInt(
        log2Up(
          PipeMemRmw.numPostFrontStages(
            //doModInModFront=doModInModFront,
            optModHazardKind=optModHazardKind,
            //optModFwdToFront=false,
            modStageCnt=modStageCnt,
            optIncludePreMid0Front=cfg.optIncludePreMid0Front,
          )
        ) + 4 bits
    )
  )
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
) extends Bundle {
  
  def wordType() = cfg.wordType()
  //def wordCount = cfg.wordCount
  def hazardCmpType() = cfg.hazardCmpType()
  def modRdPortCnt = cfg.modRdPortCnt
  def modStageCnt = cfg.modStageCnt
  def memArrSize = cfg.memArrSize
  def optModHazardKind: PipeMemRmw.ModHazardKind = cfg.optModHazardKind
  def optReorder = cfg.optReorder 
  def numMyUpExtDel2 = cfg.numMyUpExtDel2
  //--------
  //val hadActiveUpFire = /*KeepAttribute*/(Bool())

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
  val memAddr = PipeMemRmwPayloadExtMainMemAddr(
    cfg=cfg,
    wordCount=wordCount,
  )
  val nonMemAddrMost = PipeMemRmwPayloadExtMainNonMemAddrMost(
    cfg=cfg,
    wordCount=wordCount,
  )
  val modMemWord = wordType()
}

case class PipeMemRmwPayloadExt[
  WordT <: Data,
  HazardCmpT <: Data,
](
  cfg: PipeMemRmwConfig[
    WordT,
    HazardCmpT,
  ],
  wordCount: Int,
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

  val pipeFlags = /*KeepAttribute*/(
    PipeMemRmwPayloadExtPipeFlags()
  )
  def valid = pipeFlags.valid
  def ready = pipeFlags.ready
  def fire = pipeFlags.fire

  val main = /*KeepAttribute*/(
    PipeMemRmwPayloadExtMain(
      cfg=cfg,
      wordCount=wordCount,
    )
  )
  //def doEnableFwdIfHaveZeroReg(
  //  ydx: Int,
  //  zdx: Int,
  //): Unit = {
  //  modMemWordValid.foreach(current => {
  //    current := True
  //  })
  //}
  def memAddrFwdMmw = main.memAddr.memAddrFwdMmw
  def memAddrFwd = main.memAddr.memAddrFwd
  def memAddrFwdCmp = main.memAddr.memAddrFwdCmp
  def memAddr = main.memAddr.memAddr
  def memAddrAlt = main.memAddr.memAddrAlt
  def fwdIdx = main.nonMemAddrMost.fwdIdx
  def modMemWord = main.modMemWord
  def otherModMemWord = main.nonMemAddrMost.otherModMemWord
  def modMemWordValid = main.nonMemAddrMost.modMemWordValid
  def rdMemWord = main.nonMemAddrMost.rdMemWord
  def fwdCanDoIt = main.nonMemAddrMost.fwdCanDoIt
  def joinIdx = main.nonMemAddrMost.joinIdx
  //def reqReorderCommit = main.nonMemAddrMost.reqReorderCommit
  //def didReorderCommit = main.nonMemAddrMost.didReorderCommit
  def hazardCmp = main.nonMemAddrMost.hazardCmp
  def hazardId = main.nonMemAddrMost.hazardId
  def getHazardIdIdleVal() = (
    -1
  )
  def doInitHazardId(): Unit = {
    //for (zdx <- 0 until modRdPortCnt) {
      hazardId/*(zdx)*/ := getHazardIdIdleVal()
    //}
  }
}

trait PipeMemRmwPayloadBase[
  WordT <: Data,
  HazardCmpT <: Data,
] extends Bundle {
  //--------
  def setPipeMemRmwExt(
    inpExt: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    ydx: Int,
    memArrIdx: Int,
  ): Unit

  def getPipeMemRmwExt(
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
  def formalSetPipeMemRmwFwd(
    inpFwd: PipeMemRmwFwd[
      WordT,
      HazardCmpT,
      //ModT,
      //DualRdT,
    ],
    memArrIdx: Int,
  ): Unit// = { //= ???
  //} 

  def formalGetPipeMemRmwFwd(
    outpFwd: PipeMemRmwFwd[
      WordT,
      HazardCmpT,
      //ModT,
      //DualRdT,
    ],
    memArrIdx: Int,
  ): Unit// = { //= ???
  //}
}
object PipeMemRmw {
  def modMemWordValidSize: Int = 4
  def extMainSize = 2
  def addrWidth(
    wordCount: Int,
  ) = log2Up(wordCount)
  def numPostFrontStages(
    optModHazardKind: PipeMemRmw.ModHazardKind,
    modStageCnt: Int,
    optIncludePreMid0Front: Boolean,
  ) = (
    numPostFrontPreWriteStages(
      optModHazardKind=optModHazardKind,
      modStageCnt=modStageCnt,
      optIncludePreMid0Front=optIncludePreMid0Front,
    )
    + 1
  )
  def numPostFrontPreWriteStages(
    optModHazardKind: PipeMemRmw.ModHazardKind,
    modStageCnt: Int,
    optIncludePreMid0Front: Boolean
  ) = (
    (
      if (
        optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
      ) (
        1
      ) else (
        1
      )
    )
    + modStageCnt //+ 1
    //+ (if (optIncludePreMid0Front) (1) else (0))
  )
  def numMyUpExtDel2(
    optModHazardKind: PipeMemRmw.ModHazardKind,
    modStageCnt: Int,
    optIncludePreMid0Front: Boolean,
  ) = (
    PipeMemRmw.numPostFrontPreWriteStages(
      optModHazardKind=optModHazardKind,
      modStageCnt=modStageCnt,
      optIncludePreMid0Front=optIncludePreMid0Front,
    )
    + 1
    + (if (optIncludePreMid0Front) (1) else (0))
  )
  //--------
  sealed trait ModHazardKind
  object ModHazardKind {
    case object Dont extends ModHazardKind
    case object Dupl extends ModHazardKind
    case object Fwd extends ModHazardKind
  }
  //--------
  // TODO (*Maybe*): Implement `FwdSubKind`
  //sealed trait FwdSubKind
  //object FwdSubKind {
  //  case object Mid0FrontOnly extends FwdSubKind
  //  case object UseLaterPipeStages extends FwdSubKind
  //}
  //--------
  //def modWrIdx = 0
  //def modRdIdxStart = 1
  //--------
  def extIdxUp = 0
  def extIdxSaved = 1
  def extIdxLim = 2
  def extIdxSingle = extIdxUp
  //--------
}
case class PipeMemRmwDualRdTypeDisabled[
  WordT <: Data,
  HazardCmpT <: Data,
](
) extends Bundle with PipeMemRmwPayloadBase[WordT, HazardCmpT]
{
  //--------
  def setPipeMemRmwExt(
    inpExt: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
  }
  def getPipeMemRmwExt(
    outpExt: PipeMemRmwPayloadExt[WordT, HazardCmpT],
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
  }
  def formalSetPipeMemRmwFwd(
    outpFwd: PipeMemRmwFwd[WordT, HazardCmpT],
    memArrIdx: Int,
  ): Unit = {
  }

  def formalGetPipeMemRmwFwd(
    inpFwd: PipeMemRmwFwd[
      WordT,
      HazardCmpT,
    ],
    memArrIdx: Int,
  ): Unit = {
  }
  //--------
}
case class PipeMemRmwFwd[
  WordT <: Data,
  HazardCmpT <: Data,
](
  cfg: PipeMemRmwConfig[
    WordT,
    HazardCmpT,
  ],
) extends Bundle {
  def wordType() = cfg.wordType()
  def wordCount = cfg.wordCountMax
  def memArrSize = cfg.memArrSize
  def hazardCmpType() = cfg.hazardCmpType()
  def modRdPortCnt = cfg.modRdPortCnt
  def modStageCnt = cfg.modStageCnt
  def optModHazardKind: PipeMemRmw.ModHazardKind = (
    cfg.optModHazardKind
  )
  def optReorder = cfg.optReorder 
  //--------
  val myFindFirst_0 = (
    Vec.fill(memArrSize)(
      Vec.fill(modRdPortCnt)(
        Vec.fill(PipeMemRmw.extIdxLim)(
          Bool()
        )
      )
    )
  )
  //--------
  val myFindFirst_1 = (
    Vec.fill(memArrSize)(
      Vec.fill(modRdPortCnt)(
        Vec.fill(PipeMemRmw.extIdxLim)(
          UInt(log2Up(
            ////mod.front.myUpExtDel2.size
            numMyUpExtDel2 + 1
          ) bits)
        )
      )
    )
  )
  //--------
  val myFwdIdx = (
    Vec.fill(memArrSize)(
      Vec.fill(modRdPortCnt)(
        UInt(log2Up(cfg.numMyUpExtDel2 + 1) bits)
      )
    )
  )
  val myFwdMmwValidUp = (
    Vec.fill(memArrSize)(
      Vec.fill(modRdPortCnt)(
        Bool()
      )
    )
  )
  val myFwdDataUp = (
    Vec.fill(memArrSize)(
      Vec.fill(modRdPortCnt)(
        cfg.wordType()
      )
    )
  )
  val myUpIsValid = (
    Bool()
    //Vec.fill(memArrSize)(
    //  Vec.fill(modRdPortCnt)(
    //    Bool()
    //  )
    //)
  )
  val myUpIsFiring = (
    Bool()
    //Vec.fill(memArrSize)(
    //  Vec.fill(modRdPortCnt)(
    //    Bool()
    //  )
    //)
  )
  //--------
  def numMyUpExtDel2 = (
    PipeMemRmw.numMyUpExtDel2(
      optModHazardKind=optModHazardKind,
      modStageCnt=modStageCnt,
      optIncludePreMid0Front=cfg.optIncludePreMid0Front,
    )
  )
  //println(
  //  //f"numMyUpExtDel:${numMyUpExtDel }"
  //  //+ 
  //  f"numMyUpExtDel2:${numMyUpExtDel2}"
  //)
  def mkExt(
  ): Vec[Vec[PipeMemRmwPayloadExt[
    WordT,
    HazardCmpT,
  ]]] = {
    val ret = Vec.fill(memArrSize)(
      Vec.fill(PipeMemRmw.extIdxLim)(
        PipeMemRmwPayloadExt[
          WordT,
          HazardCmpT,
        ](
          cfg=cfg,
          wordCount=wordCount,
        )
      )
    )
    ret
  }

  val myFwdData = (
    Vec.fill(memArrSize)(
      Vec.fill(modRdPortCnt)(
        wordType()
      )
    )
  )

  val myFwdStateData = (
    Vec.fill(memArrSize)(
      Vec.fill(modRdPortCnt)(
        Vec.fill(numMyUpExtDel2 + 1)(
          cfg.wordType()
        )
      )
    )
  )

  val myUpExtDel2 = (
    Vec.fill(numMyUpExtDel2)(
      mkExt()
    )
  )
  val myUpExtDel2FindFirstVec = (
    Vec.fill(cfg.numForkJoin)(
      Vec.fill(memArrSize)(
        Vec.fill(modRdPortCnt)(
          Vec.fill(PipeMemRmw.extIdxLim)(
            Vec.fill(numMyUpExtDel2 + 1)(
              //Bool()
              Flow(Flow(cfg.wordType()))
            )
          )
        )
      )
    )
  )
  //--------
}

case class PipeMemRmwDoFwdArea[
  WordT <: Data,
  HazardCmpT <: Data,
](
  fjIdx: Int,
  fwdAreaName: String,
  fwd: PipeMemRmwFwd[
    WordT,
    HazardCmpT,
  ],
  setToMyFwdDataFunc: (
    Int,      // ydx
    Int,      // zdx
    WordT,    // myFwdData
  ) => Unit,
  optFirstFwdRdMemWord: Option[Vec[Vec[WordT]]]=None,
  link: CtrlLink,
  //upIsFiring: Bool
  pipeName: String,
) extends Area {
  def extIdxUp = (
    PipeMemRmw.extIdxUp
  )
  def extIdxSaved = (
    PipeMemRmw.extIdxSaved
  )
  def extIdxSingle = (
    PipeMemRmw.extIdxSingle
  )
  object FwdState extends SpinalEnum(
    defaultEncoding=binaryOneHot
  ) {
    val
      WAIT_FIRST_UP_VALID,
      WAIT_DATA,
      WAIT_UP_FIRE
      = newElement();
  }
  val rFwdState = {
    val temp = Reg(
      Vec.fill(fwd.memArrSize)(
        Vec.fill(fwd.modRdPortCnt)(
          Vec.fill(fwd.numMyUpExtDel2 + 1)(
            FwdState()
          )
        )
      )
    )
    for (ydx <- 0 until fwd.memArrSize) {
      for (zdx <- 0 until fwd.modRdPortCnt) {
        for (kdx <- 0 until fwd.numMyUpExtDel2 + 1) {
          temp(ydx)(zdx)(kdx).init(
            FwdState.WAIT_FIRST_UP_VALID
          )
        }
      }
    }
    temp.setName(
      fwdAreaName
      + s"_rFwdState"
    )
    temp
  }
  for (ydx <- 0 until fwd.memArrSize) {
    for (zdx <- 0 until fwd.modRdPortCnt) {
      val firstFwdRdMemWord: (Boolean, WordT) = (
        optFirstFwdRdMemWord match {
          case Some(myFirstFwdRdMemWord) => {
            (
              true,
              myFirstFwdRdMemWord(ydx)(zdx)
            )
          }
          case None => {
            (false, fwd.wordType())
          }
        }
      )
      def firstFwd = firstFwdRdMemWord._1
      val myFindFirstUp = (
        (True, fwd.myFwdIdx(ydx)(zdx))
        .setName(
          s"${fwdAreaName}_myFindFirstUp_${fjIdx}_${ydx}_${zdx}"
        )
      )
      def tempMyFindFirstUp_0 = fwd.myFindFirst_0(ydx)(zdx)(extIdxUp)
      def tempMyFindFirstUp_1 = fwd.myFindFirst_1(ydx)(zdx)(extIdxUp)
      def tempMyFwdData = fwd.myFwdData(ydx)(zdx)
      tempMyFindFirstUp_1.allowOverride
      val myFwdCondUp = (
        firstFwd
      ) generate (
        tempMyFindFirstUp_0
        .setName(s"${fwdAreaName}_myFwdCondUp_${ydx}_${zdx}")
      )

      if (firstFwd) {
        tempMyFindFirstUp_0 := myFindFirstUp._1
        tempMyFindFirstUp_1 := myFindFirstUp._2.resized
      }
      if (firstFwd) {
        def mySetToMyFwdUp(): Unit = {
          tempMyFwdData := (
            fwd.myFwdStateData(ydx)(zdx)(tempMyFindFirstUp_1)
          )
          for (kdx <- 0 until fwd.numMyUpExtDel2 + 1) {
            if (kdx < fwd.numMyUpExtDel2 /*- 1*/) {
              if (kdx <= 1) {
                fwd.myFwdStateData(ydx)(zdx)(kdx) := (
                  fwd.myUpExtDel2FindFirstVec(fjIdx)(ydx)(zdx)(
                    extIdxUp
                  )(
                    kdx
                  ).payload.payload
                )
              } else {
                println(
                  s"find me: kdx != 0: ${kdx} ${fwd.numMyUpExtDel2}"
                )
                fwd.myFwdStateData(ydx)(zdx)(kdx) := (
                  RegNext(
                    next=fwd.myFwdStateData(ydx)(zdx)(kdx),
                    init=fwd.myFwdStateData(ydx)(zdx)(kdx).getZero,
                  )
                )
                when (
                  //fwd.myUpIsValid
                  //&&
                  rFwdState(ydx)(zdx)(kdx) === FwdState.WAIT_DATA
                ) {
                  //tempMyFwdData := myFwdDataUp
                  fwd.myFwdStateData(ydx)(zdx)(kdx) := (
                    fwd.myUpExtDel2FindFirstVec(fjIdx)(ydx)(zdx)(
                      extIdxUp
                    )(
                      kdx
                    ).payload.payload
                  )
                }
              }
              when (
                fwd.myUpIsValid
                //&& rFwdState(ydx)(zdx)(kdx) === FwdState.WAIT_DATA
                && fwd.myUpExtDel2FindFirstVec(fjIdx)(ydx)(zdx)(
                  extIdxUp
                )(
                  kdx
                ).payload.valid
              ) {
                //rFwdStateValid(ydx)(zdx)(kdx) := True
                rFwdState(ydx)(zdx)(kdx) := FwdState.WAIT_UP_FIRE
                //fwd.myFwdStateData(ydx)(zdx)(kdx) := (
                //  fwd.myUpExtDel2FindFirstVec(fjIdx)(ydx)(zdx)(
                //    extIdxUp
                //  )(
                //    kdx
                //  ).payload.payload
                //)
              }
              when (
                fwd.myUpIsFiring
                //&& rFwdState(ydx)(zdx)(kdx) === FwdState.WAIT_UP_FIRE
              ) {
                //rFwdStateValid(ydx)(zdx)(kdx) := False
                rFwdState(ydx)(zdx)(kdx) := FwdState.WAIT_DATA
              }
            } else {
              fwd.myFwdStateData(ydx)(zdx)(kdx) := (
                fwd.myUpExtDel2FindFirstVec(fjIdx)(ydx)(zdx)(
                  extIdxUp
                )(
                  kdx
                ).payload.payload
              )
            }
          }
        }
        mySetToMyFwdUp()
      }
      setToMyFwdDataFunc(ydx, zdx, tempMyFwdData)
    }
  }
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
  modType: HardType[ModT],
  dualRdType: HardType[DualRdT]=PipeMemRmwDualRdTypeDisabled[
    WordT, HazardCmpT
  ](),
) extends Area {
  def wordType() = cfg.wordType()
  def wordCountMax = cfg.wordCountMax
  def hazardCmpType() = cfg.hazardCmpType()
  def modRdPortCnt = cfg.modRdPortCnt
  def modStageCnt = cfg.modStageCnt
  def pipeName = cfg.pipeName
  def memArrIdx = cfg.memArrIdx
  def memArrSize = cfg.memArrSize
  def optDualRd = cfg.optDualRd
  def optReorder = cfg.optReorder
  def optModHazardKind: PipeMemRmw.ModHazardKind = (
    cfg.optModHazardKind
  )
  def optEnableClear = cfg.optEnableClear
  def optFormal = cfg.optFormal 
  def vivadoDebug = cfg.vivadoDebug 
  //--------
  val clear = (optEnableClear) generate (
    (Flow(
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
  // Use `modFront` and `modBack` to insert a pipeline stage for modifying
  // the `WordT`
  val modFront = Node() //new ArrayBuffer[Node]()
  val modFrontBeforePayload = new ArrayBuffer[Payload[ModT]]()
  for (fjIdx <- 0 until cfg.numForkJoin) {
    modFrontBeforePayload += (
      Payload(modType())
      .setName(s"${pipeName}_io_modFrontBeforePayload_${fjIdx}")
    )
  }
  val modFrontAfterPayload = (
    Payload(modType())
    .setName(s"${pipeName}_io_modFrontAfterPayload")
  )
  val tempModFrontPayload = Vec.fill(/*memArrSize*/ cfg.numForkJoin)(
    modType()
  )
  val modBack = Node() //new ArrayBuffer[Node]()

  val modBackFwd = (
    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
  ) generate (
    Node()
  )
  val modBackPayload = (
    Payload(modType())
    .setName(s"${pipeName}_io_modBackPayload")
  )

  val midModStages = (
    (
      modStageCnt > 0
      && optModHazardKind != PipeMemRmw.ModHazardKind.Dont
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
  val back = Node()
  val backPayload = (
    Payload(modType())
    .setName(s"${pipeName}_io_backPayload")
  )
  //--------
  val dualRdFront = (optDualRd) generate (
    Node()
  )
  val dualRdFrontPayload = (
    Payload(dualRdType())
    .setName(s"${pipeName}_io_dualRdFrontPayload")
  )
  val dualRdBack = (optDualRd) generate (
    Node()
  )
  val dualRdBackPayload = (
    Payload(dualRdType())
    .setName(s"${pipeName}_io_dualRdBackPayload")
  )
  //--------
}

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
case class PipeMemRmwDoModInPreMid0FrontFuncParams[
  WordT <: Data,
  HazardCmpT <: Data,
  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
](
  pipeMemIo: PipeMemRmwIo[
    WordT,
    HazardCmpT,
    ModT,
    DualRdT,
  ],
  outp: ModT,                 // tempUpMod(2)
  inp: ModT,                  // tempUpMod(1)
  cPreMid0Front: CtrlLink,    // mod.front.cPreMid0Front
) {
}
case class PipeMemRmwDoModInMid0FrontFuncParams[
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
  outp: ModT,                           // tempUpMod(2),
  inp: ModT,                            // tempUpMod(1),
  cMid0Front: CtrlLink,                 // mod.front.cMid0Front
  modFront: Node,                       // io.modFront
  tempModFrontPayload: ModT,            // io.tempModFrontPayload
  //myModMemWord: WordT,                // myModMemWord
  getMyRdMemWordFunc: (
    //UInt,
    Int,
    Int,
  ) => WordT,  // getMyRdMemWordFunc
  //Vec[WordT],  // myRdMemWord
  //ydx: Int,                             // ydx
  fjIdx: Int,
  myFwd: PipeMemRmwFwd[WordT, HazardCmpT],
) {
}
// A Read-Modify-Write pipelined BRAM (or LUTRAM)
case class PipeMemRmw[
  WordT <: Data,
  HazardCmpT <: Data,
  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
](
  cfg: PipeMemRmwConfig[
    WordT,
    HazardCmpT,
  ],
  modType: HardType[ModT],
  dualRdType: HardType[DualRdT]=PipeMemRmwDualRdTypeDisabled[
    WordT, HazardCmpT,
  ](),
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
  doModInPreMid0FrontFunc: Option[
    (
      PipeMemRmwDoModInPreMid0FrontFuncParams[
        WordT,
        HazardCmpT,
        ModT,
        DualRdT,
      ],
    ) => Area
  ]=None,
  //--------
  doModInMid0FrontFunc: Option[
    (
      PipeMemRmwDoModInMid0FrontFuncParams[
        WordT,
        HazardCmpT,
        ModT,
        DualRdT,
      ],
    ) => Area
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
  //--------
)
extends Area {
  //--------
  def wordType() = cfg.wordType()
  def wordCountArr = cfg.wordCountArr 
  def hazardCmpType() = cfg.hazardCmpType()
  def modRdPortCnt = cfg.modRdPortCnt
  def modStageCnt = cfg.modStageCnt
  def pipeName = cfg.pipeName 
  def linkArr = cfg.linkArr
  def memArrIdx = cfg.memArrIdx
  def optDualRd = cfg.optDualRd
  def optReorder = cfg.optReorder
  def init = cfg.init 
  def initBigInt = cfg.initBigInt
  def optModHazardKind = cfg.optModHazardKind
  def optEnableClear = cfg.optEnableClear 
  def memRamStyleAltera = cfg.memRamStyleAltera
  def memRamStyleXilinx = cfg.memRamStyleXilinx
  def memRwAddrCollisionXilinx = cfg.memRwAddrCollisionXilinx
  def vivadoDebug = cfg.vivadoDebug 
  def optIncludePreMid0Front = (
    cfg.optIncludePreMid0Front
  )
  //if (optIncludePreMid0Front) {
  //  assert(
  //    doModInPreMid0FrontFunc != None,
  //    (
  //      s"`doModInPreMid0FrontFunc` must *not* be `None` "
  //      + s"if `cfg.optIncludePreMid0Front == true`"
  //    )
  //  )
  //}
  def optIncludeModFrontStageLink = cfg.optIncludeModFrontStageLink
  def optIncludeModFrontS2MLink = cfg.optIncludeModFrontS2MLink
  if (optIncludeModFrontS2MLink) {
    assert(
      optIncludeModFrontStageLink
    )
  }
  def optFormal = cfg.optFormal
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
  //--------
  def debug: Boolean = {
    //GenerationFlags.formal {
    //  return true
    //}
    //return false
    optFormal
  }
  def wordCountMax = cfg.wordCountMax
  //--------
  val io = PipeMemRmwIo[
    WordT,
    HazardCmpT,
    ModT,
    DualRdT,
  ](
    cfg=cfg,
    modType=modType(),
    dualRdType=dualRdType(),
  )
  //--------
  def mkPreFwdArea(
    upIsFiring: Bool,
    upExtElem: Vec[Vec[PipeMemRmwPayloadExt[WordT, HazardCmpT]]]
  ) = new Area {
    def extIdxUp = PipeMemRmw.extIdxUp
    for (ydx <- 0 until memArrSize) {
      def myMemAddrFwdCmp = (
        upExtElem(ydx)(extIdxUp).memAddrFwdCmp
      )
      def myFwdIdx = (
        upExtElem(ydx)(extIdxUp).fwdIdx
      )
      val myHistMemAddr = (
        /*KeepAttribute*/(
          History[UInt](
            that=upExtElem(ydx)(extIdxUp).memAddr.last,
            // `length=numMyUpExtDel2 + 1` because `History` includes the
            // current value of `that`.
            // This might not be relevant any more?
            length=2,//mod.front.myUpExtDel2.size + 1, 
            when=upIsFiring,
            init=upExtElem(ydx)(extIdxUp).memAddr.last.getZero,
          )
        )
        .setName(
          s"${pipeName}_PreFwdArea_myHistMemAddr_${ydx}"
        )
      )
      for (zdx <- 0 until modRdPortCnt) {
        val toFindFirst = Vec.fill(
          cfg.numMyUpExtDel2  + 1
        )(
          Flow(
            UInt(PipeMemRmw.addrWidth(wordCountArr(ydx)) bits)
          )
        )
        for ((item, itemIdx) <- toFindFirst.view.zipWithIndex) {
          val tempMyUpExtDel = mod.front.myUpExtDel(itemIdx)(ydx)(extIdxUp)
          if (itemIdx != cfg.numMyUpExtDel2) {
            item.valid := myMemAddrFwdCmp(zdx)(itemIdx)(0)
            item.payload := tempMyUpExtDel.memAddrFwdMmw(zdx)(itemIdx)
          } else {
            item.valid := True
            item.payload := upExtElem(ydx)(extIdxUp).memAddr(zdx)
          }
        }
        myFwdIdx(zdx) := (
          toFindFirst.sFindFirst(
            current => (current.fire === True)
          )._2
        )
        upExtElem(ydx)(extIdxUp).memAddrFwdMmw(zdx).foreach(current => {
          current := (
            upExtElem(ydx)(extIdxUp).memAddr.last
          )
        })
        upExtElem(ydx)(extIdxUp).memAddrFwd(zdx).foreach(current => {
          current := upExtElem(ydx)(extIdxUp).memAddr(zdx)
        })
        for (idx <- 0 until mod.front.myUpExtDel2.size + 1) {
          println(
            f"myHistMemAddr debug: ${zdx} ${idx} ${idx - 1} "
            + f"${mod.front.myUpExtDel2.size}"
          )
          if (idx > 0) {
            def tempMemAddrFwdCmp = myMemAddrFwdCmp(zdx)(idx - 1)
            tempMemAddrFwdCmp.allowOverride
            for (jdx <- 0 until tempMemAddrFwdCmp.getWidth) {
              val myZeroRegCond = (
                cfg.optFwdHaveZeroReg match {
                  case Some(myZeroRegIdx) => {
                    (
                      upExtElem(ydx)(extIdxUp).memAddr(zdx)
                      =/= myZeroRegIdx
                    )
                  }
                  case None => {
                    True
                  }
                }
              )
              val tempMyUpExtDel = (
                mod.front.myUpExtDel(
                  idx - 1
                  //+ (if (optIncludePreMid0Front) (1) else (0))
                )(ydx)(extIdxUp)
              )
              if (idx == 1) {
                //val tempMyUpExtDel = (
                //  mod.front.myUpExtDel(
                //    idx - 1
                //  )(ydx)(extIdxUp)
                //)
                tempMemAddrFwdCmp(jdx) := (
                  (
                    upExtElem(ydx)(extIdxUp).memAddr(
                      zdx
                    ) === (
                      myHistMemAddr(idx)
                    )
                  )
                  && (
                    myZeroRegCond
                  )
                  //&& (
                  //  //tempMyUpExtDel.modMemWordValid({
                  //  //  if (
                  //  //    idx < tempMyUpExtDel.modMemWordValid.size
                  //  //  ) (
                  //  //    idx
                  //  //  ) else (
                  //  //    tempMyUpExtDel.modMemWordValid.size - 1 
                  //  //  )
                  //  //})
                  //)
                  && (
                    tempMyUpExtDel.fwdCanDoIt(
                      zdx
                    )
                  )
                )
              } else {
                //val tempMyUpExtDel = (
                //  mod.front.myUpExtDel(idx - 1)(ydx)(extIdxUp)
                //)
                println(
                  s"tempMyUpExtDel debug: (${idx - 1})(${ydx})  "
                  + s"${jdx}"
                )
                tempMemAddrFwdCmp(
                  jdx
                  //0
                ) := (
                  (
                    upExtElem(ydx)(extIdxUp).memAddr(
                      zdx
                    ) === (
                      //myHistMemAddr(idx)
                      // `idx - 1` is because we want the stage *before*
                      // the one we're actually interested in.
                      if (idx == mod.front.myUpExtDel2.size) (
                        tempMyUpExtDel.memAddrFwdMmw(zdx).last
                      ) else (
                        tempMyUpExtDel.memAddrFwdMmw(zdx)(idx)
                      )
                    )
                  )
                  && myZeroRegCond
                  && (
                    //if (idx == 1) (
                    //  True
                    //) else (
                      tempMyUpExtDel.fwdCanDoIt(
                        zdx
                      )
                    //)
                  )
                  //&& (
                  //  //if (idx == 1) (
                  //    True
                  //  //) else (
                  //  //  tempMyUpExtDel.modMemWordValid({
                  //  //    if (
                  //  //      idx < tempMyUpExtDel.modMemWordValid.size
                  //  //    ) (
                  //  //      idx
                  //  //    ) else (
                  //  //      tempMyUpExtDel.modMemWordValid.size - 1 
                  //  //    )
                  //  //  })
                  //  //)
                  //)
                  //&& (
                  //  tempMyUpExtDel.valid(0)
                  //)
                )
              }
            }
          }
        }
      }
    }
  }

  def mkMem(ydx: Int) = {
    val ret = RamSimpleDualPort(
      cfg=RamSimpleDualPortConfig(
        wordType=wordType(),
        depth=wordCountArr(ydx),
        init=(
          init match {
            case Some(myInit) => {
              Some(myInit(ydx))
            }
            case None => {
              None
            }
          }
        ),
        initBigInt=(
          initBigInt match {
            case Some(myInitBigInt) => {
              Some(myInitBigInt(ydx))
            }
            case None => {
              None
            }
          }
        ),
        arrRamStyleAltera=memRamStyleAltera,
        arrRamStyleXilinx=memRamStyleXilinx,
        arrRwAddrCollisionXilinx=memRwAddrCollisionXilinx,
      )
    )
    ret
  }
  def mkMemSdpPipeCfg(ydx: Int) = (
    RamSdpPipeConfig(
      wordType=wordType(),
      depth=wordCountArr(ydx),
      init=(
        init match {
          case Some(myInit) => {
            Some(myInit(ydx))
          }
          case None => {
            None
          }
        }
      ),
      initBigInt=(
        initBigInt match {
          case Some(myInitBigInt) => {
            Some(myInitBigInt(ydx))
          }
          case None => {
            None
          }
        }
      ),
      arrRamStyleAltera=memRamStyleAltera,
      arrRamStyleXilinx=memRamStyleXilinx,
      arrRwAddrCollisionXilinx=memRwAddrCollisionXilinx,
    )
  )
  def mkMemSdpPipe(ydx: Int) = {
    val ret = RamSdpPipe(
      cfg=mkMemSdpPipeCfg(ydx=ydx)
    )
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
    optModHazardKind != PipeMemRmw.ModHazardKind.Dont
    && !optIncludePreMid0Front
  ) generate {
    val myArr = new ArrayBuffer[Array[
      RamSimpleDualPort[WordT]
    ]]()
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
  val modMemSdpPipe = (
    optModHazardKind != PipeMemRmw.ModHazardKind.Dont
    && optIncludePreMid0Front
  ) generate {
    val myArr = new ArrayBuffer[Array[
      RamSdpPipe[WordT]
    ]]
    for (ydx <- 0 until memArrSize) {
      myArr += (
        Array.fill(modRdPortCnt)(
          mkMemSdpPipe(ydx=ydx)
        )
      )
    }
    myArr
  }
  val modMemSdpPipeFifoThing = (
    optModHazardKind != PipeMemRmw.ModHazardKind.Dont
    && optIncludePreMid0Front
  ) generate {
    val myArr = new ArrayBuffer[ArrayBuffer[
      RamSdpPipeReadFifoThing[WordT]
    ]]()
    for (ydx <- 0 until memArrSize) {
      myArr += {
        //Array.fill(modRdPortCnt)(
        //  RamSdpPipeReadFifoThing
        //)
        val myInnerArr = new ArrayBuffer[RamSdpPipeReadFifoThing[WordT]]()
        for (zdx <- 0 until modRdPortCnt) {
          myInnerArr += RamSdpPipeReadFifoThing(
            cfg=RamSdpPipeReadFifoThingConfig(
              ramCfg=modMemSdpPipe(ydx)(zdx).cfg,
              fifoDepthMain=8,
            )
          )
        }
        myInnerArr
      }
    }
    myArr
  }

  val dualRdMem = (io.optDualRd) generate {
    val myArr = new ArrayBuffer[RamSimpleDualPort[WordT]]()
    for (ydx <- 0 until memArrSize) {
      myArr += mkMem(ydx=ydx)
    }
    myArr
  }
  def memWriteIterate(
    writeFunc: (RamSimpleDualPort[WordT]) => Unit,
    ydx: Int,
  ): Unit = {
    if (optModHazardKind != PipeMemRmw.ModHazardKind.Dont) {
      for (zdx <- 0 until modRdPortCnt) {
        writeFunc(modMem(ydx)(zdx))
      }
    }
    if (io.optDualRd) {
      writeFunc(dualRdMem(ydx))
    }
  }
  def memSdpPipeWriteIterate(
    writeFunc: (RamSdpPipe[WordT]) => Unit,
    ydx: Int,
  ): Unit = {
    for (zdx <- 0 until modRdPortCnt) {
      writeFunc(modMemSdpPipe(ydx)(zdx))
    }
  }
  def memWriteAll(
    address: Vec[Vec[UInt]],
    data: Vec[Vec[WordT]],
    enable: Vec[Bool]=null,
    //mask: Vec[Bits]=null,
  ): Unit = {
    assert(address.size == memArrSize)
    assert(data.size == memArrSize)
    assert(enable.size == memArrSize)
    for (ydx <- 0 until memArrSize) {
      if (!optIncludePreMid0Front) {
        memWriteIterate(
          writeFunc=(
            item: RamSimpleDualPort[WordT],
          ) => {
            item.io.ramIo.wrEn := enable(ydx)
            item.io.ramIo.wrAddr := address(ydx).head(
              PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
              downto 0
            )
            item.io.ramIo.wrData := data(ydx).head
          },
          ydx=ydx
        )
      } else {
        memSdpPipeWriteIterate(
          writeFunc=(
            item: RamSdpPipe[WordT],
          ) => {
            item.io.wrEn := enable(ydx)
            item.io.wrAddr := address(ydx).head(
              PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
              downto 0
            )
            item.io.wrData := data(ydx).head
          },
          ydx=ydx
        )
      }
    }
  }
  //--------
  def myHaveFwd = (
    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
  )
  def myHaveFormalFwd = (
    (
      optFormal
    ) && (
      myHaveFwd
    )
  )
  def mkFwd(): PipeMemRmwFwd[WordT, HazardCmpT] = (
    PipeMemRmwFwd[WordT, HazardCmpT](cfg=cfg)
  )
  def mkOneExt(): PipeMemRmwPayloadExt[WordT, HazardCmpT] = (
    PipeMemRmwPayloadExt(
      cfg=cfg,
      wordCount=wordCountMax,
    )
  )
  def mkExt(): Vec[Vec[PipeMemRmwPayloadExt[WordT, HazardCmpT]]] = {
    val ret = Vec.fill(memArrSize)(
      Vec.fill(PipeMemRmw.extIdxLim)(
        mkOneExt(
        )
      )
    )
    ret
  }
  val myFwd = (
    mkFwd()
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
        idx: Int,
        isPostDelay: Boolean,
        doValidCheck: Boolean=(
          //false
          true
        ),
        forceFalse: Boolean=false,
      ): Flow[Flow[WordT]] = {
        val ret = Flow(Flow(cfg.wordType()))
        ret.valid := (
          if (!forceFalse) {
          //(upExt(1).memAddr === prev.memAddr)
            if (
              //forFwd
              cfg.optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
            ) (
              if (idx == 0) (
                currMemAddr(0)
                //&& (
                //  prev.modMemWordValid(
                //    if (zdx < prev.modMemWordValid.size) (
                //      zdx
                //    ) else (
                //      prev.modMemWordValid.size - 1
                //    )
                //  )
                //)
              ) else (
                (
                  currMemAddr(0)
                  //currMemAddr === prevMemAddr
                )
                //&& (
                //  prev.modMemWordValid(
                //    if (zdx < prev.modMemWordValid.size) (
                //      zdx
                //    ) else (
                //      prev.modMemWordValid.size - 1
                //    )
                //  )
                //)
              )
              //else (
              //  cfg.optFwdHaveZeroReg match {
              //    case Some(myZeroRegIdx) => {
              //      currMemAddr === prevMemAddr
              //    }
              //    case None => {
              //      (
              //        currMemAddr === prevMemAddr
              //      ) && (
              //        prev.modMemWordValid(
              //          if (zdx < prev.modMemWordValid.size) (
              //            zdx
              //          ) else (
              //            prev.modMemWordValid.size - 1
              //          )
              //        )
              //      )
              //    }
              //  }
              //)
            ) else (
              (
                if (!isPostDelay) (
                  (
                    ////if (!forFwd) (
                    //  currMemAddr
                    //  === prevMemAddr
                    ////) else (
                    ////  currMemAddr(0)
                    ////  //currMemAddr =/= 0x0
                    ////)
                    if (idx == 0) (
                      currMemAddr(0)
                    ) else (
                      (
                        currMemAddr
                        === prevMemAddr
                      )
                    )
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
                (
                  prev.modMemWordValid(
                    0
                    //zdx
                    //3
                  )
                  //True
                ) && (
                  if (doValidCheck) (
                    prev.valid.last
                  ) else (
                    True
                  )
                )
                //curr.valid && prev.valid
              )
            )
          } else {
            False
          }
        )
        ret.payload.valid := {
          //if (idx == 0) (
          //  True
          //  //False
          //) else (
            prev.modMemWordValid(
              if (zdx < prev.modMemWordValid.size) (
                zdx
              ) else (
                prev.modMemWordValid.size - 1
              )
            )
          //)
        }
        ret.payload.payload := (
          prev.modMemWord
        )
        ret
      }
      def inpPipePayload = io.frontPayload
      //val midPipePayloadArr = /*Array.fill(memArrSize)*/(Payload(modType()))
      //for (idx <- 0 until memArrSize) {
      //  midPipePayloadArr(idx)
      //  .setName(s"${pipeName}_${idx}_io_midPipePayloadArr")
      //}
      //def midPipePayload = midPipePayloadArr//(memArrIdx)
      val midPipePayload = {
        Array.fill(2)(
          Payload(Vec.fill(2)(modType()))
        )
      }
      for (idx <- 0 until midPipePayload.size) {
        midPipePayload(idx).setName(
          s"${pipeName}_io_midPipePayload_${idx}"
        )
      }
      //val outpPipePayload = Payload(modType())
      def outpPipePayload = io.modFrontAfterPayload
      val myRdMemWord = Vec.fill(memArrSize)(
        Vec.fill(modRdPortCnt)(
          wordType()
        )
      )
      val myNonFwdRdMemWord = Vec.fill(
        //if (optIncludePreMid0Front) (
        //  2
        //) else (
          1
        //)
      )(
        Vec.fill(memArrSize)(
          Vec.fill(modRdPortCnt)(
            /*Reg*/(
              wordType()
            )
          )
        )
      )
      val myUpExtDel = /*KeepAttribute*/(
        Vec.fill(
          PipeMemRmw.numPostFrontStages(
            optModHazardKind=optModHazardKind,
            modStageCnt=modStageCnt,
            optIncludePreMid0Front=optIncludePreMid0Front,
          )
          + 1 
          + (if (optIncludePreMid0Front) (1) else (0))
        )(
          mkExt()
        )
      )
      println(
        s"myUpExtDel.size: ${myUpExtDel.size}"
      )
      //--------
      val myUpExtDel2 = (
        myFwd.myUpExtDel2
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
          }
        }
      }
      val myUpExtDelFullFindFirstVecNotPostDelay = (
        Vec.fill(memArrSize)(
          Vec.fill(modRdPortCnt)(
            Vec.fill(extIdxLim)(
              Vec.fill(myUpExtDel.size)(
                Flow(Flow(cfg.wordType()))
              )
            )
          )
        )
      )
      if (
        optModHazardKind != PipeMemRmw.ModHazardKind.Dont
        && modStageCnt > 0
      ) {
        for (idx <- 0 until io.midModStages.size) {
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
                  optModHazardKind=optModHazardKind,
                  modStageCnt=modStageCnt,
                  optIncludePreMid0Front=optIncludePreMid0Front,
                )
                - modStageCnt
                + idx 
                //- (
                //  if (optIncludePreMid0Front) (
                //    1
                //  ) else (
                //    0
                //  )
                //)
              )
              myUpExtDel(tempIdx)(ydx)(extIdx) := myExt(ydx)(extIdx)
            }
          }
        }
      }

      def myIncludeForkJoin = (
        optIncludeModFrontStageLink
        && cfg.numForkJoin > 1
      )
      val cFront = CtrlLink(
        up=io.front,
        down={
          //if (myIncludeForkJoin) {
            val temp = Node()
            temp.setName(s"${pipeName}_cFront_down")
            temp
          //} else {
          //  
          //}
        }
      )
        //.setName(s"${pipeName}_Front")
      myLinkArr += cFront

      val nfFrontArr = (
        myIncludeForkJoin
      ) generate (
        new ArrayBuffer[Node]()
      )
      if (myIncludeForkJoin) (
        for (fjIdx <- 0 until cfg.numForkJoin) {
          nfFrontArr += (
            Node()
            .setName(f"${pipeName}_nfFrontArr_${fjIdx}")
          )
        }
      )
      val fFront = (
        myIncludeForkJoin
      ) generate (
        ForkLink(
          up=(
            cFront.down,
          ),
          downs=nfFrontArr,
          synchronous=(
            // TODO: determine correct value of `synchronous`
            true
            //false
          ),
        )
      )
      if (myIncludeForkJoin) {
        myLinkArr += fFront
      }

      val sFront = new ArrayBuffer[StageLink]()
      for (fjIdx <- 0 until cfg.numForkJoin) {
        sFront += StageLink(
          up=(
            //cFront.down
            if (myIncludeForkJoin) (
              nfFrontArr(fjIdx)
            ) else (
              cFront.down
            )
          ),
          down={
            val temp = Node()
            temp.setName(s"${pipeName}_sFront_down_${fjIdx}")
            temp
          },
        )
        myLinkArr += sFront(fjIdx)
      }
      val cPreMid0Front = new ArrayBuffer[CtrlLink]()
      val sPreMid0Front = new ArrayBuffer[StageLink]()
      if (optIncludePreMid0Front) {
        for (fjIdx <- 0 until cfg.numForkJoin) {
          cPreMid0Front += CtrlLink(
            up=sFront(fjIdx).down,
            down={
              val temp = Node()
              temp.setName(s"${pipeName}_cPreMid0Front_down_${fjIdx}")
              temp
            }
          )
          myLinkArr += cPreMid0Front(fjIdx)
          sPreMid0Front += StageLink(
            up=cPreMid0Front(fjIdx).down,
            down={
              val temp = Node()
              temp.setName(s"${pipeName}_sPreMid0Front_down_${fjIdx}")
              temp
            }
          )
          myLinkArr += sPreMid0Front(fjIdx)
        }
      }

      val cMid0Front = new ArrayBuffer[CtrlLink]()
      for (fjIdx <- 0 until cfg.numForkJoin) {
        cMid0Front += CtrlLink(
          up=(
            //if (optLinkFrontToModFront) (
            //if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
            if (!optIncludePreMid0Front) (
              sFront(fjIdx).down
            ) else (
              sPreMid0Front(fjIdx).down
            )
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
            temp.setName(s"${pipeName}_cMid0Front_down_${fjIdx}")
            temp
            //) else (
            //  io.modFront
            //)
          },
        )
        myLinkArr += cMid0Front(fjIdx)
      }
      val myIncludeS2mMid0Front = (
        optIncludeModFrontStageLink
        && optIncludeModFrontS2MLink
        && (
          optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
        )
        //false
      )
      println(
        s"optIncludeModFrontStageLink:${optIncludeModFrontStageLink} "
        + s"optIncludeModFrontS2MLink:${optIncludeModFrontS2MLink} "
        + s"myIncludeS2mMid0Front:${myIncludeS2mMid0Front}"
      )
      val sMid0Front = (optIncludeModFrontStageLink) generate (
        new ArrayBuffer[StageLink]()
      )
      var njMid0Front = (
        //optIncludeModFrontStageLink
        //&& cfg.numForkJoin > 1
        myIncludeForkJoin
      ) generate (
        new ArrayBuffer[Node]()
      )
      if (
        //optIncludeModFrontStageLink
        //&& cfg.numForkJoin > 1
        myIncludeForkJoin
      ) {
        for (fjIdx <- 0 until cfg.numForkJoin) {
          njMid0Front += (
            Node()
            .setName(s"${pipeName}_njMid0Front_${fjIdx}")
          )
        }
      }
      if (optIncludeModFrontStageLink) {
        for (fjIdx <- 0 until cfg.numForkJoin) {
          sMid0Front += StageLink(
            up=cMid0Front(fjIdx).down,
            down=(
              //Node()
              if (!myIncludeS2mMid0Front) (
                ////io.modFront
                //njMid0Front(fjIdx)
                if (cfg.numForkJoin > 1) (
                  njMid0Front(fjIdx)
                ) else (
                  io.modFront
                )
              ) else {
                val temp = Node()
                temp.setName(s"${pipeName}_sMid0Front_${fjIdx}_down")
                temp
              }
            ),
          )
          //println(
          //  s"optIncludeModFrontS2MLink: ${optIncludeModFrontS2MLink}"
          //)
          myLinkArr += sMid0Front(fjIdx)
        }
      }
      val s2mMid0Front = (
        myIncludeS2mMid0Front
      ) generate (
        new ArrayBuffer[S2MLink]()
      )
      if (myIncludeS2mMid0Front) {
        for (fjIdx <- 0 until cfg.numForkJoin) {
          s2mMid0Front += S2MLink(
            up=sMid0Front(fjIdx).down,
            down={
              if (cfg.numForkJoin > 1) (
                njMid0Front(fjIdx)
              ) else (
                io.modFront
              )
            },
          )
          myLinkArr += s2mMid0Front(fjIdx)
        }
      }
      val njStmMid0Front = (
        //optIncludeModFrontStageLink
        //&& cfg.numForkJoin > 1
        myIncludeForkJoin
      ) generate (
        new ArrayBuffer[Stream[ModT]]()
      )
      val jStmMid0Front = (
        //optIncludeModFrontStageLink
        //&& cfg.numForkJoin > 1
        myIncludeForkJoin
      ) generate (
        Stream(
          Vec.fill(cfg.numForkJoin)(
            modType()
          )
        )
        .setName(f"${pipeName}_jStmMid0Front")
      )
      if (
        //optIncludeModFrontStageLink
        //&& cfg.numForkJoin > 1
        myIncludeForkJoin
      ) {
        for (fjIdx <- 0 until cfg.numForkJoin) {
          njStmMid0Front += (
            Stream(modType())
            .setName(f"${pipeName}_njMid0FrontStm_${fjIdx}")
          )
          njMid0Front(fjIdx).driveTo(
            njStmMid0Front(fjIdx)
          )(
            (mod, node) => {
              mod := node(io.modFrontBeforePayload(fjIdx))
            }
          )
        }
        //jStmMid0Front.arbitrationFrom(StreamJoin(
        //  sources=njStmMid0Front
        //))
      }
      val tempJoin = (
        //optIncludeModFrontStageLink
        //&& cfg.numForkJoin > 1
        myIncludeForkJoin
      ) generate (
        StreamJoin.vec(
          sources=njStmMid0Front
        )
        .setName(s"${pipeName}_tempJoin")
      )
      if (
        //optIncludeModFrontStageLink
        //&& cfg.numForkJoin > 1
        myIncludeForkJoin
      ) {
        //jStmMid0Front << tempJoin
        io.modFront.driveFrom(
          //jStmMid0Front
          tempJoin
        )(
          (node, mod) => {
            val tempExt = (
              mkOneExt()
              .setName(s"${pipeName}_tempExt")
            )
            mod.head.getPipeMemRmwExt(
              outpExt=tempExt,
              ydx=0,
              memArrIdx=cfg.memArrIdx,
            )
            node(outpPipePayload) := (
              if (mod.size == 1) (
                mod(0)
              ) else (
                mod(tempExt.joinIdx)
              )
            )
          }
        )
      }
      //val jMid0Front = (optIncludeModFrontStageLink) generate (
      //  JoinLink(
      //    ups=njMid0Front,
      //    down=(
      //      io.modFront
      //    )
      //  )
      //)
      //if (optIncludeModFrontStageLink) {
      //  myLinkArr += jMid0Front
      //}

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
            cond=(
              if (!optIncludePreMid0Front) (
                cFront.down.isFiring
              ) else (
                cPreMid0Front(0).down.isFiring
              )
            )
            ////cFront.down.isValid
            ////cMid0Front.up.isValid
          )
          //init(nextDidFwd.getZero)
        )
      )
      //val cTempMid0Front = (
      //  if (optIncludePreMid0Front) (
      //    cPreMid0Front(0)
      //  ) else (
      //    cMid0Front(0)
      //  )
      //)
      if (optIncludePreMid0Front) {
        //for (ydx <- 0 until memArrSize) {
        //  for (zdx <- 0 until modRdPortCnt) {
        //    //val myModMem = modMem(ydx)(zdx)
        //    //val rTempRdData = (
        //    //  RegNextWhen(
        //    //    next=myModMem.io.ramIo.rdData,
        //    //    cond=cPreMid0Front(0).down.isFiring,
        //    //    init=myModMem.io.ramIo.rdData.getZero,
        //    //  )
        //    //)
        //    //myRdMemWord(ydx)(zdx).assignFromBits(
        //    //  rTempRdData.asBits
        //    //)
        //    def myModMemSdpPipe = modMemSdpPipe(ydx)(zdx)
        //    def myFifoThing = modMemSdpPipeFifoThing(ydx)(zdx)
        //    myRdMemWord(ydx)(zdx) := (
        //      RegNext(
        //        myRdMemWord(ydx)(zdx),
        //        init=myRdMemWord(ydx)(zdx).getZero
        //      )
        //    )
        //    when (
        //      RegNext(
        //        RegNext(
        //          next=myFifoThing.io.pop.valid,
        //          init=False,
        //        ),
        //        init=False,
        //      )
        //    ) {
        //      myRdMemWord(ydx)(zdx) := myModMemSdpPipe.io.rdData
        //    }
        //  }
        //}
      } else {
        myRdMemWord := /*RegNext*/(myNonFwdRdMemWord.last)//(0)
      }

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
      //--------
      val myUpExtDel2FindFirstVec = /*KeepAttribute*/(
        myFwd.myUpExtDel2FindFirstVec
      )
      //--------
    }
    val back = new Area {
      //val dbgDoClear = (optEnableClear) generate (
      //  /*KeepAttribute*/(Bool())
      //)
      val dbgDoWrite = /*(debug) generate*/ (
        /*KeepAttribute*/(
          Vec.fill(memArrSize)(
            Bool()
          )
        )
      )
      val myWriteAddr = /*KeepAttribute*/(
        Vec.fill(2)(
          Vec.fill(memArrSize)(
            Vec.fill(modRdPortCnt)(
              cloneOf(front.myUpExtDel(0)(0)(0).memAddr.last)
            )
          )
        )
      )
      val myWriteData = /*KeepAttribute*/(
        Vec.fill(2)(
          Vec.fill(memArrSize)(
            Vec.fill(modRdPortCnt)(
              cloneOf(front.myUpExtDel(0)(0)(0).modMemWord)
            )
          )
        )
      )
      val myWriteEnable = /*KeepAttribute*/(
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
      val cBackFwd = (
        optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
      ) generate (
        CtrlLink(
          up=io.modBackFwd,
          down=Node()
        )
      )
      if (optModHazardKind == PipeMemRmw.ModHazardKind.Fwd) {
        myLinkArr += cBackFwd
        cBackFwd.down.ready := True
      }

      val dIoModBack = DirectLink(
        up=io.modBack,
        down=cBack.up,
      )
      myLinkArr += dIoModBack
      //val cPreLastBackExtraFwd = (
      //  optIncludePreMid0Front
      //) generate (
      //  pipe.addStage(
      //    name=pipeName + "_PreLastBackExtraFwd",
      //    optIncludeS2M=false,
      //  )
      //)
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
      dIoBack.up(io.backPayload) := dIoBack.up(pipePayload)
      val rTempWord = /*(debug) generate*/ /*KeepAttribute*/(
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
    val tempCond = /*KeepAttribute*/(Bool())
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
    val nextHazardId = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      /*KeepAttribute*/(cloneOf(upExt(1)(0)(0).hazardId))
    )
    val rHazardId = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      /*KeepAttribute*/(
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
      /*KeepAttribute*/(State())
    )
    val rState = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      /*KeepAttribute*/(RegNext(nextState) init(State.IDLE))
    )
    val rPrevStateWhen = (
      //optEnableModDuplicate
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      /*KeepAttribute*/(
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
      /*KeepAttribute*/(Bool())
    )
    val rDidChangeState = (
      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    ) generate (
      /*KeepAttribute*/(
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
                idx=idx,
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
              val toAdd = (
                myUpExtDelFullFindFirstVecNotPostDelay(ydx)(zdx)(extIdx)
                .sFindFirst(
                  _.fire === True
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

      upExt(1)(ydx)(extIdxUp).valid.foreach(current => {
        current := up.isValid
      })
      upExt(1)(ydx)(extIdxUp).ready := up.isReady
      upExt(1)(ydx)(extIdxUp).fire := up.isFiring
    }
    if (optModHazardKind == PipeMemRmw.ModHazardKind.Dupl) {
      // BEGIN: stalling-based code; fix later
      nextState := rState
      switch (rState) {
        is (State.IDLE) {
          when (up.isValid) {
            for (ydx <- 0 until memArrSize) {
              upExt(1)(ydx)(extIdxSingle).hazardId := nextHazardId
              for (zdx <- 0 until modRdPortCnt) {
                val myUpCmp = /*KeepAttribute*/(
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
                val mySavedCmp = /*KeepAttribute*/(
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
    val tempSharedEnable = /*KeepAttribute*/(
      //down.isReady
      Vec.fill(modRdPortCnt + 1)(
        //if (optIncludePreMid0Front) (
        //  down.isFiring
        //) else (
          down.isReady
        //)
      )
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
      && tempSharedEnable.last
      //--------
    )

    if (optModHazardKind != PipeMemRmw.ModHazardKind.Dont) {
      if (optModHazardKind == PipeMemRmw.ModHazardKind.Dupl) {
        for (ydx <- 0 until memArrSize) {
          // BEGIN: previous `duplicateIt` code; fix later
          //myNonFwdRdMemWord(ydx)(PipeMemRmw.modWrIdx) := (
          //  modMem(ydx)(PipeMemRmw.modWrIdx).readSync(
          //    address=(
          //      //upExtRealMemAddr(PipeMemRmw.modWrIdx)
          //      upExt(1)(ydx)(extIdxUp).memAddr(PipeMemRmw.modWrIdx)(
          //        PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
          //        downto 0
          //      )
          //    ),
          //    //address=myDownExt.memAddr,
          //    enable=(
          //      tempCond
          //    ),
          //  )
          //)
          val myModMem = modMem(ydx).last//(PipeMemRmw.modWrIdx)
          myModMem.io.ramIo.rdEn := tempCond
          myModMem.io.ramIo.rdAddr := (
            //upExtRealMemAddr(PipeMemRmw.modWrIdx)
            upExt(1)(ydx)(extIdxUp).memAddr.last(
              PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
              downto 0
            )
          )
          myNonFwdRdMemWord.head(ydx).last.assignFromBits(
            myModMem.io.ramIo.rdData.asBits
          )
        }
        // END: previous `duplicateIt` code; fix later
      } else { // if (optModHazardKind == PipeMemRmw.ModHazardKind.Fwd)
        for (ydx <- 0 until memArrSize) {
          for (zdx <- 0 until modRdPortCnt) {
            def myModMem = modMem(ydx)(zdx)
            def myModMemSdpPipe = modMemSdpPipe(ydx)(zdx)
            def myFifoThing = modMemSdpPipeFifoThing(ydx)(zdx)
            val myRamIo = (
              if (!optIncludePreMid0Front) (
                myModMem.io.ramIo
              ) else (
                myModMemSdpPipe.io
              )
            )
            val tempAddrWidth = log2Up(wordCountArr(ydx))
            if (!optIncludePreMid0Front) {
              myRamIo.rdEn := (
                True
                //tempSharedEnable.last
                //RegNext(tempSharedEnable.last, init=False)
                //True
              )
              myRamIo.rdAddr := (
                RegNext(myRamIo.rdAddr, init=myRamIo.rdAddr.getZero)
              )
              when (tempSharedEnable.last) {
                myRamIo.rdAddr := upExt(1)(ydx)(extIdxUp).memAddr(zdx)(
                  tempAddrWidth - 1 downto 0
                )
              }
            } else {
              myFifoThing.io.push.valid := (
                //tempSharedEnable.last
                //up.isFiring
                //down.isFiring
                //up.isFiring
                //down.isReady
                //up.isFiring
                down.isReady
              )
              myFifoThing.io.push.payload := (
                upExt(1)(ydx)(extIdxUp).memAddr(zdx)(
                  tempAddrWidth - 1 downto 0
                )
              )
              //myRamIo.rdAddr := (
              //  RegNext(myRamIo.rdAddr, init=myRamIo.rdAddr.getZero)
              //)
              //when (
              //  //up.isFiring
              //  down.isReady
              //) {
              myRamIo.rdAddr := myFifoThing.io.pop.payload
              //}
              //myRamIo.rdEn := (
              //  RegNext(myFifoThing.io.pop.valid, init=False)
              //)
              //myFifoThing.io.pop.ready := up.isReady
              myFifoThing.io.pop.ready := (
                True
                //down.isReady
              )
              myFifoThing.io.delay := False
            }
            //--------
            // BEGIN: working, non-two-cycle-read BRAM
            val tempRdData = myRamIo.rdData
            if (optIncludePreMid0Front) {
            } else {
              myNonFwdRdMemWord.head(ydx)(zdx) := tempRdData
            }
            // END: working, non-two-cycle-read BRAM
            //--------
            //if (optIncludePreMid0Front) {
            //  myNonFwdRdMemWord.last(ydx)(zdx) := (
            //    RegNext/*When*/(
            //      next=myNonFwdRdMemWord.head(ydx)(zdx),
            //      //cond=down.isReady,//RegNext(down.isReady, init=False),
            //      init=myNonFwdRdMemWord.head(ydx)(zdx).getZero,
            //    )
            //  )
            //}
            //myModMem.io.cmpRdWrAddrEtc := (
            //  //tempSharedEnable.last
            //  //&& 
            //  (
            //    RegNext(
            //      upExt(1)(ydx)(extIdxUp).memAddr(zdx)(
            //        tempAddrWidth - 1 downto 0
            //      ) === (
            //        //mod.back.myWriteAddr(1)(ydx)(zdx)
            //        mod.front.myUpExtDel2(
            //          //mod.front.myUpExtDel2.size - 3
            //          0
            //        )(ydx)(PipeMemRmw.extIdxUp).memAddr(
            //          PipeMemRmw.modWrIdx
            //        )(
            //          tempAddrWidth - 1 downto 0
            //        )
            //      )
            //      //&& (
            //      //  tempSharedEnable.last
            //      //)
            //      && mod.front.myUpExtDel2(
            //        0
            //      )(ydx)(PipeMemRmw.extIdxUp).modMemWordValid(0)
            //    )
            //    init(False)
            //  )
            //  //init(False)
            //)
            //myNonFwdRdMemWord(ydx)(zdx) := (
            //  RegNext(
            //    next=myNonFwdRdMemWord(ydx)(zdx),
            //    init=myNonFwdRdMemWord(ydx)(zdx).getZero
            //  )
            //)
            //when (
            //  //RegNext(
            //  //  next=tempSharedEnable.last,
            //  //  init=tempSharedEnable.last.getZero,
            //  //)
            //  down.isReady
            //  //down.isValid
            //) {
            //  myNonFwdRdMemWord(ydx)(zdx).assignFromBits(
            //    RegNext(myModMem.io.ramIo.rdData)
            //  )
            //}

            //def tempWidth = (
            //  mod.back.myWriteAddr(1)(ydx)(zdx).getWidth
            //)
            //val rTempVec = (
            //  Vec.fill(tempWidth + 2)(
            //    Reg(Bool(), init=False)
            //  )
            //)
            //for (kdx <- 0 until tempWidth) {
            //  rTempVec(kdx) := (
            //    upExt(1)(ydx)(extIdxUp).memAddr(zdx)(kdx)
            //    === mod.back.myWriteAddr(1)(ydx)(zdx)(kdx)
            //  )
            //}
            //rTempVec(rTempVec.size - 2) := (
            //  tempSharedEnable.last
            //)
            //rTempVec(rTempVec.size - 1) := (
            //  mod.back.myWriteEnable(ydx)
            //)

            //--------
            // BEGIN: old attempt at forwarding ahead of time
            //when (
            //  ///*LcvFastAndR*/
            //  //tempSharedEnable.last
            //  ////mod.front.myUpExtDel(0)(0)(0).ready
            //  //&&

            //  //up.isFiring
            //  //down.isReady
            //  //&&
            //  RegNextWhen(
            //    next=(
            //      Vec[Bool](
            //        //up.isFiring,
            //        ///*RegNext*/(
            //        //  ///*next=*/tempSharedEnable.last/*, init=False*/
            //        //  down.isFiring
            //        //),
            //        /*RegNext*/(
            //          //next=LcvFastCmpEq(
            //          //  left=upExt(1)(ydx)(extIdxUp).memAddr(zdx),
            //          //  right=mod.back.myWriteAddr(1)(ydx)(zdx),
            //          //),
            //          /*next=*/(
            //            (upExt(1)(ydx)(extIdxUp).memAddr(zdx))
            //            === (mod.back.myWriteAddr(1)(ydx)(zdx))
            //          )//,
            //          //init=False,
            //        ),
            //        /*RegNext*/(
            //          /*next=*/mod.back.myWriteEnable(ydx)/*, init=False*/
            //        )
            //      ).asBits.asUInt.andR
            //    ),
            //    cond=up.isFiring,
            //    init=False
            //  )
            //  //rTempVec.asBits.asUInt.andR
            //  ///*LcvFastAndR*/(rTempVec.asBits.asUInt.andR)
            //) {
            //  //myNonFwdRdMemWord(ydx)(zdx) := modMem(ydx)(zdx).readAsync(
            //  //  address=(
            //  //    //upExtRealMemAddr(zdx)
            //  //    upExt(1)(ydx)(extIdxUp).memAddr(zdx)(
            //  //      PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
            //  //      downto 0
            //  //    )
            //  //  ),
            //  //  //enable=(
            //  //  //  //tempCond
            //  //  //  //!mod.front.nextDidFwd(zdx)(0)
            //  //  //  //&& 
            //  //  //  tempSharedEnable
            //  //  //  //down.isReady
            //  //  //),
            //  //)
            //  //when (
            //  //  (
            //  //    RegNext(
            //  //      upExt(1)(ydx)(extIdxUp).memAddr(zdx)
            //  //      === mod.back.myWriteAddr(ydx)
            //  //    )
            //  //  ) && (
            //  //    RegNext(mod.back.myWriteEnable(ydx))
            //  //  )
            //  //  //&& tempSharedEnable
            //  //  //&& down.isReady
            //  //) {
            //    myNonFwdRdMemWord(ydx)(zdx) := RegNext(
            //      //mod.back.myWriteData(ydx)
            //      next=mod.back.myWriteData(1)(ydx)(zdx),
            //      init=mod.back.myWriteData(1)(ydx)(zdx).getZero,
            //    )
            //  //}
            //}
            // END: old attempt at forwarding ahead of time
            //--------
          }
        }
      }
    }
    val tempCondDown = (
      down.isFiring
    )
    //--------
    tempUpMod(1) := (
      RegNext(
        next=tempUpMod(1),
        init=tempUpMod(1).getZero,
      )
    )
    //when (up.isFiring) {
      tempUpMod(1) := tempUpMod(0)
    //}
    tempUpMod(1).allowOverride
    for (ydx <- 0 until memArrSize) {
      //for (extIdx <- 0 until extIdxLim) {
      //}
      //when (up.isFiring) {
        upExt(1)(ydx)(extIdxUp).memAddrAlt.allowOverride
        upExt(1)(ydx)(extIdxUp).memAddrAlt.foreach(current => {
          current := (
            upExt(1)(ydx)(extIdxUp).memAddr.last
          )
        })
        //tempUpMod(1).setPipeMemRmwExt(
        //  inpExt=RegNextWhen(upExt(1)(ydx)(extIdxUp)),
        //  ydx=ydx,
        //  memArrIdx=memArrIdx,
        //)
        //when (up.isFiring) {
          tempUpMod(1).setPipeMemRmwExt(
            inpExt=upExt(1)(ydx)(extIdxUp),
            ydx=ydx,
            memArrIdx=memArrIdx,
          )
        //}
      //}
      if (ydx == 0) {
        for (idx <- 0 until up(mod.front.midPipePayload(0)).size) {
          up(mod.front.midPipePayload(0))(idx) := tempUpMod(1)
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
    val myPreFwdArea = (
      !optIncludePreMid0Front
    ) generate (
      mkPreFwdArea(
        upIsFiring=up.isFiring,
        upExtElem=upExt(1),
      )
    )
  }
  for (fjIdx <- 0 until mod.front.cPreMid0Front.size) {
    val cPreMid0Front = mod.front.cPreMid0Front(fjIdx)
    val cPreMid0FrontArea = (
      optIncludePreMid0Front
    ) generate (new cPreMid0Front.Area {
      setName(s"${pipeName}_cPreMid0FrontArea_${fjIdx}")
      val upExt = (
        Vec.fill(3)(mkExt())
        .setName(s"${pipeName}_cPreMid0FrontArea_${fjIdx}_upExt")
      )
      val tempUpMod = (
        Vec.fill(3)(
          modType()
        )
        .setName(s"${pipeName}_cPreMid0FrontArea_${fjIdx}_tempUpMod")
      )
      tempUpMod(0).allowOverride
      tempUpMod(0) := up(mod.front.midPipePayload(0))(0)
      tempUpMod(1).allowOverride
      tempUpMod(1) := tempUpMod(0)
      tempUpMod(2).allowOverride
      tempUpMod(2) := (
        RegNext(
          next=tempUpMod(2),
          init=tempUpMod(2).getZero
        )
      )
      upExt.foreach(outerOuterItem => {
        outerOuterItem.foreach(outerItem => {
          outerItem.foreach(item => {
            item := RegNext(item, init=item.getZero)
            item.allowOverride
          })
        })
      })
      for (ydx <- 0 until memArrSize) {
        upExt(1)(ydx)(extIdxUp) := (
          RegNext(
            next=upExt(1)(ydx)(extIdxUp),
            init=upExt(1)(ydx)(extIdxUp).getZero,
          )
        )
        upExt(1)(ydx)(extIdxUp).allowOverride
        when (
          up.isValid
          //up.isFiring
        ) {
          upExt(1)(ydx)(extIdxUp) := upExt(0)(ydx)(extIdxSingle)
          //for (zdx <- 0 until modRdPortCnt) {
          //  upExt(1)(ydx)(extIdxUp).rdMemWord(zdx) := (
          //    RegNext(
          //      next=upExt(1)(ydx)(extIdxUp).rdMemWord(zdx),
          //      init=upExt(1)(ydx)(extIdxUp).rdMemWord(zdx).getZero,
          //    )
          //  )
          //}
        }
        upExt(1)(ydx)(extIdxSaved) := (
          RegNextWhen(
            next=upExt(1)(ydx)(extIdxUp),
            cond=up.isFiring,
            init=upExt(1)(ydx)(extIdxSaved).getZero,
          )
        )

        upExt(1)(ydx)(extIdxUp).valid.foreach(current => {
          current := up.isValid
        })
        upExt(1)(ydx)(extIdxUp).ready := up.isReady
        upExt(1)(ydx)(extIdxUp).fire := up.isFiring
      }
      val myDoModInPreMid0FrontAreaArr = new ArrayBuffer[Area]()
      doModInPreMid0FrontFunc match {
        case Some(myDoModInPreMid0FrontFunc) => {
          myDoModInPreMid0FrontAreaArr += (
            myDoModInPreMid0FrontFunc(
              PipeMemRmwDoModInPreMid0FrontFuncParams(
                pipeMemIo=io,
                outp=tempUpMod(2),
                inp=tempUpMod(1),
                cPreMid0Front=cPreMid0Front,
              )
            )
            .setName(s"${pipeName}_myDoModInPreMid0FrontAreaArr")
          )
        }
        case None => {
          tempUpMod(1) := tempUpMod(0)
          tempUpMod(2) := tempUpMod(1)
          //assert(
          //  false,
          //  "eek!"
          //)
        }
      }
      for (ydx <- 0 until memArrSize) {
        tempUpMod(0).getPipeMemRmwExt(
          outpExt=upExt(0)(ydx)(extIdxUp),
          ydx=ydx,
          memArrIdx=memArrIdx,
        )
        tempUpMod(1).setPipeMemRmwExt(
          inpExt=upExt(1)(ydx)(extIdxUp),
          ydx=ydx,
          memArrIdx=memArrIdx,
        )
        //upExt(2)(ydx)(extIdxUp) := (
        //  upExt(1)(ydx)(extIdxUp)
        //)
        tempUpMod(2).getPipeMemRmwExt(
          outpExt=upExt(2)(ydx)(extIdxUp),
          ydx=ydx,
          memArrIdx=memArrIdx,
        )
      }
      for (idx <- 0 until up(mod.front.midPipePayload(1)).size) {
        up(mod.front.midPipePayload(1))(idx) := tempUpMod(2)
      }
      //upExt(0) := up(mod.front.midPipePayload)(0)
      val myPreFwdArea = (
        mkPreFwdArea(
          upIsFiring=up.isFiring,
          upExtElem=upExt(1),
        )
      )
      for (ydx <- 0 until memArrSize) {
        for (zdx <- 0 until modRdPortCnt) {
          def myModMemSdpPipe = modMemSdpPipe(ydx)(zdx)
          def myFifoThing = modMemSdpPipeFifoThing(ydx)(zdx)
          val myRamIo = myModMemSdpPipe.io
          //myRamIo.rdEn := (
          //  down.isReady
          //)
        }
      }
      for (ydx <- 0 until memArrSize) {
        for (zdx <- 0 until modRdPortCnt) {
          def myModMemSdpPipe = modMemSdpPipe(ydx)(zdx)
          def myFifoThing = modMemSdpPipeFifoThing(ydx)(zdx)
          val myRamIo = myModMemSdpPipe.io
          //val rReadyCnt = (
          //  Reg(UInt(2 bits)) init(
          //)
          myModMemSdpPipe.io.rdEn := (
            //True
            RegNext(
              next=(
                myFifoThing.io.pop.valid
              ),
              init=False,
            )
            ////&& down.isReady
            //&& up.isFiring
          )
          //val rPopState = Reg(Bool(), init=False)
          //myFifoThing.io.pop.ready := (
          //  up.isReady
          //  //True
          //  //up.isValid
          //  //up.isValid
          //  //up.isReady
          //  //!up.isValid
          //  //|| !up.isReady
          //  //True //up.isReady
          //)
          //myFifoThing.io.delay := (
          //  False
          //  //!up.isValid
          //  //|| 
          //  //!up.isReady
          //  //False
          //  //!up.isValid
          //  //|| !up.isReady
          //)
          //myRamIo.rdEn := (
          //  down.isReady
          //)
        }
      }
    })
  }
  for (fjIdx <- 0 until mod.front.cMid0Front.size) {
    val cMid0Front = mod.front.cMid0Front(fjIdx)
    val cMid0FrontArea = new cMid0Front.Area {
      setName(s"${pipeName}_cMidFrontArea_${fjIdx}")
      //--------
      val dbgUpIsValid = /*KeepAttribute*/(
        cMid0Front.up.isValid
      )
        .setName(s"${pipeName}_cMid0FrontArea_dbgUpIsValid_${fjIdx}")
      val dbgUpIsReady = /*KeepAttribute*/(
        cMid0Front.up.isReady
      )
        .setName(s"${pipeName}_cMid0FrontArea_dbgUpIsReady_${fjIdx}")
      val dbgUpIsFiring = /*KeepAttribute*/(
        cMid0Front.up.isFiring
      )
        .setName(s"${pipeName}_cMid0FrontArea_dbgUpIsFiring_${fjIdx}")
      val dbgDownIsValid = /*KeepAttribute*/(
        cMid0Front.down.isValid
      )
        .setName(s"${pipeName}_cMid0FrontArea_dbgDownIsValid_${fjIdx}")
      val dbgDownIsReady = /*KeepAttribute*/(
        cMid0Front.down.isReady
      )
        .setName(s"${pipeName}_cMid0FrontArea_dbgDownIsReady_${fjIdx}")
      val dbgDownIsFiring = /*KeepAttribute*/(
        cMid0Front.down.isFiring
      )
        .setName(s"${pipeName}_cMid0FrontArea_dbgDownIsFiring_${fjIdx}")
      //--------
      val rSaveMemRdDataState = Reg(Bool(), init=False)
      if (optIncludePreMid0Front) {
        val myRdMemWord = mod.front.myRdMemWord
        for (ydx <- 0 until memArrSize) {
          for (zdx <- 0 until modRdPortCnt) {
            def myModMemSdpPipe = modMemSdpPipe(ydx)(zdx)
            def myFifoThing = modMemSdpPipeFifoThing(ydx)(zdx)
            val myRamIo = myModMemSdpPipe.io
            //val rReadyCnt = (
            //  Reg(UInt(2 bits)) init(
            //)
            //val rPopState = Reg(Bool(), init=False)
            myRdMemWord(ydx)(zdx) := (
              RegNext(
                myRdMemWord(ydx)(zdx),
                init=myRdMemWord(ydx)(zdx).getZero
              )
            )
            when (
              RegNext(
                RegNext(
                  next=myFifoThing.io.pop.valid,
                  init=False,
                ),
                init=False,
              )
            ) {
              when (!rSaveMemRdDataState) {
                myRdMemWord(ydx)(zdx) := myModMemSdpPipe.io.rdData
                if (ydx == 0 && zdx == 0) {
                  rSaveMemRdDataState := True
                }
              }
              //when (up.isValid) {
              //  myRdMemWord(ydx)(zdx) := myModMemSdpPipe.io.rdData
              //}
            }
            if (ydx == 0 && zdx == 0) {
              when (up.isFiring) {
                rSaveMemRdDataState := False
              }
            }
            //when (
            //  up.isReady
            //) {
            //  //myRdMemWord(ydx)(zdx) := myModMemSdpPipe.io.rdData
            //} otherwise {
            //  myFifoThing.io.pop.ready := False
            //  //myFifoThing.io.delay := True
            //}

            //myFifoThing.io.pop.ready := (
            //  up.isReady
            //  //True
            //  //up.isValid
            //  //up.isValid
            //  //up.isReady
            //  //!up.isValid
            //  //|| !up.isReady
            //  //True //up.isReady
            //)
            //myFifoThing.io.delay := (
            //  False
            //  //!up.isValid
            //  //|| 
            //  //!up.isReady
            //  //False
            //  //!up.isValid
            //  //|| !up.isReady
            //)
            //myRamIo.rdEn := (
            //  down.isReady
            //)
          }
        }
      }
      //--------
      val upExt = Vec.fill(3)(
        mkExt()
      ).setName(s"${pipeName}_cMid0FrontArea_upExt_${fjIdx}")
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
        /*KeepAttribute*/(
          Vec.fill(memArrSize)(
            Bool()
          )
        )
        .setName(s"${pipeName}_nextPrevTxnWasHazardVec_${fjIdx}")
      )
      val rPrevTxnWasHazardVec = (
        /*KeepAttribute*/(
          RegNextWhen/*RegNext*/(
            next=nextPrevTxnWasHazardVec,
            cond=up.isFiring,
          )
          //init(False)
          //init(nextPrevTxnWasHazardVec.getZero)
        )
        .setName(s"${pipeName}_rPrevTxnWasHazardVec_${fjIdx}")
      )
      val nextPrevTxnWasHazardAny = (
        /*KeepAttribute*/(
          Bool()
        )
        .setName(s"${pipeName}_nextPrevTxnWasHazardAny_${fjIdx}")
      )
      val rPrevTxnWasHazardAny = (
        //if (nextPrevTxnWasHazardVec.size > 0) {
          /*KeepAttribute*/(
            //Reg(Bool()) init(False)
            RegNextWhen/*RegNext*/(
              next=nextPrevTxnWasHazardAny,
              cond=up.isFiring,
              init=False,
            )
          )
        //} else {
        //  False
        //}
        .setName(s"${pipeName}_rPrevTxnWasHazardAny_${fjIdx}")
      )
      if (nextPrevTxnWasHazardVec.size > 0) {
        nextPrevTxnWasHazardAny := (
          nextPrevTxnWasHazardVec.sFindFirst(
            _ === True
          )._1
          //LcvFastOrR(nextPrevTxnWasHazardVec.asBits.asUInt)
        )
      } else {
        nextPrevTxnWasHazardAny := nextPrevTxnWasHazardVec.head
      }
      for (ydx <- 0 until memArrSize) {
        rPrevTxnWasHazardVec(ydx).init(
          nextPrevTxnWasHazardVec(ydx).getZero
        )
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

        upExt(1)(ydx)(extIdxSingle).valid.foreach(current => {
          current := up.isValid
        })
        upExt(1)(ydx)(extIdxSingle).ready := up.isReady
        upExt(1)(ydx)(extIdxSingle).fire := up.isFiring
        myUpExtDel(0)(ydx)(extIdxUp).valid := upExt(2)(ydx)(extIdxUp).valid
        myUpExtDel(0)(ydx)(extIdxUp).ready := upExt(2)(ydx)(extIdxUp).ready
        myUpExtDel(0)(ydx)(extIdxUp).fire := upExt(2)(ydx)(extIdxUp).fire
        //upExt(1)(ydx) := upExt(0)(ydx)
      }

      val tempUpMod = (
        Vec.fill(3)(
          modType()
        )
        .setName(s"${pipeName}_cMid0FrontArea_tempUpMod_${fjIdx}")
      )
      tempUpMod(2) := (
        RegNext(
          next=tempUpMod(2),
          init=tempUpMod(2).getZero,
        )
      )
      tempUpMod(2).allowOverride
      def myMidPipePayloadIdx = (
        if (!optIncludePreMid0Front) (
          0
        ) else (
          1
        )
      )
      tempUpMod(0) := up(mod.front.midPipePayload(myMidPipePayloadIdx))(0)
      val tempUpMod0a = modType()
      tempUpMod0a := up(mod.front.midPipePayload(myMidPipePayloadIdx))(1)
      for (ydx <- 0 until memArrSize) {
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
      val tempMyUpExtDelFrontFindFirstV2d = /*KeepAttribute*/(
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
      if (fjIdx != 0) {
        assert(false, "temporarily, only support one `fjIdx`")
      }
      for (ydx <- 0 until memArrSize) {
        for (zdx <- 0 until modRdPortCnt) {
          for (idx <- 0 until mod.front.myUpExtDel2.size + 1) {
            for (extIdx <- 0 until extIdxLim) {
              if (
                idx == 0
                && extIdx == extIdxUp
              ) {
                myFwd.myFwdIdx(ydx)(zdx) := (
                  //upExt(1)(ydx)(extIdx).fwdIdx(zdx)
                  upExt(0)(ydx)(extIdx).fwdIdx(zdx)
                )
              }
              //println(
              //  s"mod.front.myUpExtDel2.size: "
              //  + s"${mod.front.myUpExtDel2.size}"
              //)
              if (idx < mod.front.myUpExtDel2.size /*- 1*/) {
                mod.front.myUpExtDel2FindFirstVec(fjIdx)(ydx)(zdx)(
                  extIdx
                )(
                  idx
                ) := (
                  (
                    mod.front.findFirstFunc(
                      currMemAddr=(
                        //if (idx == 0) (
                          upExt(1)(ydx)(extIdx).memAddrFwdCmp(zdx)(idx)
                        //) else (
                        //  upExt(1)(ydx)(extIdx).memAddrFwd(zdx)(idx)(
                        //    PipeMemRmw.addrWidth(
                        //      wordCount=wordCountArr(ydx)
                        //    ) - 1
                        //    downto 0
                        //  )
                        //)
                      ),
                      prevMemAddr=(
                        mod.front.myUpExtDel2(idx)(ydx)(extIdx)
                          .memAddrFwdMmw(zdx)
                        (
                          //PipeMemRmw.modWrIdx
                          idx
                        )(
                          PipeMemRmw.addrWidth(
                            wordCount=wordCountArr(ydx)
                          ) - 1
                          downto 0
                        )
                      ),
                      curr=(
                        upExt(1)(ydx)(
                          //extIdxSingle
                          extIdx
                        )
                      ),
                      prev=(
                        mod.front.myUpExtDel2(idx)(ydx)(extIdx)
                      ),
                      ydx=ydx,
                      zdx=zdx,
                      idx=idx,
                      isPostDelay=false,
                      doValidCheck=false,
                      forceFalse=(
                        //idx == 0 && extIdx == extIdxUp
                        false
                      ),
                      //forFwd=(
                      //  true
                      //),
                    )
                  )
                )
              } else {
                mod.front.myUpExtDel2FindFirstVec(fjIdx)(ydx)(zdx)(
                  extIdx
                )(
                  idx
                ) := {
                  val temp = Flow(Flow(cfg.wordType()))
                  temp.valid := True
                  temp.payload.valid := True//False//True
                  temp.payload.payload := (
                    //upExt(1)(ydx)(
                    //  //extIdxSingle
                    //  extIdx
                    //).modMemWord
                    /*RegNext*/(
                      /*next=*/myRdMemWord(ydx)(zdx)//,
                      //init=myRdMemWord(ydx)(zdx).getZero,
                    )
                    //upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx)
                  )
                  temp
                }
              }
            }
          }
        }
      }
      //--------
      for (ydx <- 0 until memArrSize) {
        for (zdx <- 0 until modRdPortCnt) {
          //when (
          //  RegNext/*When*/(
          //    next=cFrontArea.tempSharedEnable(zdx),
          //    //cond=down.isReady,
          //    init=cFrontArea.tempSharedEnable(zdx).getZero//False,
          //  )//(zdx)
          //) {
          //  upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
          //    myRdMemWord(ydx)(zdx)
          //  )
          //}
          //upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
          //  myRdMemWord(ydx)(zdx)
          //)
        }
        upExt(1)(ydx)(extIdxSingle).modMemWordValid.foreach(current => {
          current := True
        })
      }
      val myDoModInMid0FrontAreaArr = new ArrayBuffer[Area]()
      doModInMid0FrontFunc match {
        case Some(myDoModInMid0FrontFunc) => {
          //assert(modStageCnt == 0)
          myDoModInMid0FrontAreaArr += (
            myDoModInMid0FrontFunc(
              PipeMemRmwDoModInMid0FrontFuncParams(
                pipeMemIo=io,
                nextPrevTxnWasHazardVec=nextPrevTxnWasHazardVec,
                rPrevTxnWasHazardVec=rPrevTxnWasHazardVec,
                rPrevTxnWasHazardAny=rPrevTxnWasHazardAny,
                outp=tempUpMod(2),
                inp=tempUpMod(1),
                cMid0Front=cMid0Front,
                modFront=io.modFront,
                tempModFrontPayload=io.tempModFrontPayload(fjIdx),
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
                fjIdx=fjIdx,
                myFwd=myFwd,
              )
            )
            .setName(s"${pipeName}_myDoModInMid0FrontAreaArr")
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
      val myFindFirst_0 = (
        myHaveFwd
      ) generate (
        myFwd.myFindFirst_0
      )
      //val myFindFirst_1 = (
      //  myHaveFwd
      //) generate (
      //  myFwd.myFindFirst_1
      //)
      myFwd.myUpIsValid := cMid0Front.up.isValid
      myFwd.myUpIsFiring := cMid0Front.up.isFiring

      val doFwd = (myHaveFwd) generate (
        PipeMemRmwDoFwdArea(
          fjIdx=fjIdx,
          fwdAreaName=s"${pipeName}_cMid0FrontArea_doFwd_${fjIdx}",
          fwd=myFwd,
          setToMyFwdDataFunc=(
            ydx: Int,
            zdx: Int,
            myFwdData: WordT,
          ) => {
            //upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
            //  //myFwdData
            //  RegNext(
            //    next=upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx),
            //    init=upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx).getZero,
            //  )
            //)
            //when (down.isReady) {
              upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
                myFwdData
              )
            //}
          },
          optFirstFwdRdMemWord=Some(
            //upExt(2)
            myRdMemWord
          ),
          link=cMid0Front,
          pipeName=pipeName,
        )
      )
      //val doFormalFwdSavedMyFwd = (myHaveFormalFwd) generate (
      //  /*KeepAttribute*/(
      //    RegNextWhen(
      //      next=myFwd,
      //      cond=up.isFiring,
      //      init=myFwd.getZero,
      //    )
      //  )
      //  .setName(s"${pipeName}_doFormalFwdSavedMyFwd")
      //)
      //if (myHaveFormalFwd) {
      //  when (pastValidAfterReset) {
      //    when (
      //      past(up.isFiring) init(False)
      //    ) {
      //      assert(
      //        doFormalFwdSavedMyFwd
      //        === past(myFwd)
      //      )
      //    } otherwise {
      //      assert(
      //        stable(doFormalFwdSavedMyFwd)
      //      )
      //    }
      //  }
      //}
      //val doFormalFwdSaved = (myHaveFormalFwd) generate (
      //  PipeMemRmwDoFwdArea(
      //    fwdAreaName=s"${pipeName}_cMid0FrontArea_doFwdFormalSaved",
      //    fwd=doFormalFwdSavedMyFwd,
      //    setToMyFwdDataFunc=(
      //      ydx: Int,
      //      zdx: Int,
      //      myFwdData: WordT,
      //    ) => {
      //      //upExt(1)(ydx)(extIdxSingle).rdMemWord(zdx) := (
      //      //  myFwdData
      //      //)
      //      val myTempUpExt2 = (
      //        upExt(2)(ydx)(extIdxUp)//.rdMemWord(zdx)
      //      )
      //      val rTempSavedUpExt2 = (
      //        RegNextWhen(
      //          next=myTempUpExt2,
      //          cond=up.isFiring,
      //          init=myTempUpExt2.getZero,
      //        )
      //      )
      //      when (pastValidAfterReset) {
      //        assert(
      //          rTempSavedUpExt2.rdMemWord(zdx)
      //          === (
      //            myFwdData
      //          )
      //        )
      //        when (
      //          past(up.isFiring) init(False)
      //        ) {
      //          assert(
      //            rTempSavedUpExt2.main
      //            === (
      //              past(myTempUpExt2.main)
      //              //init(myTempSavedRdMemWord.getZero)
      //            )
      //          )
      //        }
      //      }
      //    },
      //    //firstFwd=false,
      //  )
      //)
      //--------
      val myDbgUpIsValid = /*KeepAttribute*/(
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
        if (ydx == 0) {
          val myTempUpMod = modType()
          myTempUpMod := RegNext(
            //tempUpMod(2)
            next=myTempUpMod,
            init=myTempUpMod.getZero,
          )
          up(
            if (cfg.numForkJoin > 1) (
              io.modFrontBeforePayload(fjIdx)
            ) else (
              //io.modFrontAfterPayload
              mod.front.outpPipePayload
            )
          ) := (
            tempUpMod(2)//myTempUpMod
          )
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
  }
  val cBackFwdArea = (
    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
  ) generate {
    val cBackFwd = mod.back.cBackFwd
    new cBackFwd.Area {
      val upFwd = Vec.fill(
        //2
        extIdxLim
        //1
      )(
        mkFwd(
          //myVivadoDebug=true
        )
      ).setName(s"${pipeName}_cBackFwdArea_upFwd")
      //--------
      val upExt = Vec.fill(
        2
        //1
      )(
        mkExt(
          //myVivadoDebug=true
        )
      ).setName(s"${pipeName}_cBackFwdArea_upExt")
      //--------
      val tempUpMod = Vec.fill(3)(
        //Vec.fill(extIdxLim)(
        //Vec.fill(memArrSize)(
          modType()
        //)
        //)
      ).setName(s"${pipeName}_cBackFwdArea_tempUpMod")
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
        //upExt(1)(ydx)(extIdxUp) := (
        //  RegNext(
        //    next=upExt(1)(ydx)(extIdxUp),
        //    init=upExt(1)(ydx)(extIdxUp).getZero,
        //  )
        //)
        //when (
        //  up.isValid
        //  //up.isFiring
        //  //True
        //) {
          upExt(1)(ydx)(extIdxUp) := upExt(0)(ydx)(extIdxSingle)
        //}
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
        upExt(1)(ydx)(extIdxUp).valid.foreach(current => {
          current := up.isValid
        })
        upExt(1)(ydx)(extIdxUp).ready := up.isReady
        upExt(1)(ydx)(extIdxUp).fire := up.isFiring
        if (cfg.optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) {
          for (kdx <- 0 until upExt(1)(ydx)(extIdxUp).valid.size) {
            when (
              !upExt(1)(ydx)(extIdxUp).modMemWordValid(kdx)
            ) {
              upExt(1)(ydx)(extIdxUp).valid(kdx) := False
            }
          }
        } else {
        }
      }
      //--------
      def tempMyUpExtDelPenLast = (
        myUpExtDel(
          myUpExtDel.size
          - (
            if (optIncludePreMid0Front) (
              3
            ) else (
              2
            )
            //if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
            //  2
            //) else (
            //  2//1
            //)
          )
        )
        //myUpExtDel(myUpExtDel.size - 2)
        //myUpExtDel.last
      )
      if (
        optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
      ) {
        tempMyUpExtDelPenLast := upExt(1)
      }
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
        //when (up.isValid) {
          tempUpMod(1).setPipeMemRmwExt(
            inpExt=upExt(1)(ydx)(extIdxUp),
            ydx=ydx,
            memArrIdx=memArrIdx,
          )
        //}
        //if (cfg.optModHazardKind == PipeMemRmw.ModHazardKind.Fwd) {
        //  when (
        //    !up.isValid
        //  ) {
        //    upExt(1)(ydx)(extIdxUp).modMemWordValid.foreach(_ := False)
        //  }
        //}
      }
      for (ydx <- 0 until memArrSize) {
        for (zdx <- 0 until modRdPortCnt) {
          mod.back.myWriteData(1)(ydx)(zdx).assignFromBits(
            //upExt(0)(ydx).modMemWord
            if (optEnableClear) (
              Mux[WordT](
                io.clear.fire,
                wordType().getZero,
                upExt(0)(ydx)(extIdxSingle).modMemWord,
              ).asBits
            ) else if (
              //cfg.optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
              cfg.optIncludeOtherMmw
            ) (
              //Mux[WordT](
              //  upExt(0)(ydx)(extIdxSingle).fwdCanDoIt(
              //    PipeMemRmw.modWrIdx
              //  ),
              //  upExt(0)(ydx)(extIdxSingle).modMemWord,
              //  upExt(0)(ydx)(extIdxSingle).otherModMemWord,
              //)
              (
                upExt(0)(ydx)(extIdxSingle).modMemWord.asBits
                | upExt(0)(ydx)(extIdxSingle).otherModMemWord.asBits
              )
            ) else (
              upExt(0)(ydx)(extIdxSingle).modMemWord.asBits
            )
          )
        }
      }
      val myWriteAddr = mod.back.myWriteAddr
      for (ydx <- 0 until memArrSize) {
        for (zdx <- 0 until modRdPortCnt) {
          if (optEnableClear) {
            when (io.clear.fire) {
              myWriteAddr(1)(ydx)(zdx) := (
                io.clear.payload
              )
            } otherwise { // when (!io.clear.fire)
              myWriteAddr(1)(ydx)(zdx) := (
                upExt(0)(ydx)(extIdxSingle).memAddrAlt(
                  //PipeMemRmw.modWrIdx
                  zdx
                ).resized
              )
            }
          } else { // if (!optEnableClear)
            myWriteAddr(1)(ydx)(zdx) := (
              upExt(0)(ydx)(extIdxSingle).memAddrAlt(
                //PipeMemRmw.modWrIdx
                zdx
              ).resized
            )
          }
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
    }
  }
  val cBack = mod.back.cBack
  val cBackArea = new cBack.Area {
    val upFwd = Vec.fill(
      extIdxLim
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
      modType()
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
      //when (
      //  up.isValid
      //  //up.isFiring
      //) {
        upExt(1)(ydx)(extIdxUp) := upExt(0)(ydx)(extIdxSingle)
      //}
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
      upExt(1)(ydx)(extIdxUp).valid.foreach(current => {
        current := up.isValid
      })
      upExt(1)(ydx)(extIdxUp).ready := up.isReady
      upExt(1)(ydx)(extIdxUp).fire := up.isFiring
      if (cfg.optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) {
        for (kdx <- 0 until upExt(1)(ydx)(extIdxUp).valid.size) {
          when (!upExt(1)(ydx)(extIdxUp).modMemWordValid(kdx)) {
            upExt(1)(ydx)(extIdxUp).valid(kdx) := False
          }
        }
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
            2//1
          )
        )
        - (
          if (optIncludePreMid0Front) (
            1
          ) else (
            0
          )
        )
      )
      //myUpExtDel(myUpExtDel.size - 2)
      //myUpExtDel.last
    )
    if (
      optModHazardKind != PipeMemRmw.ModHazardKind.Fwd
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
      for (zdx <- 0 until modRdPortCnt) {
        if (optEnableClear) {
          when (io.clear.fire) {
            myWriteAddr(0)(ydx)(zdx) := (
              io.clear.payload
            )
          } otherwise { // when (!io.clear.fire)
            myWriteAddr(0)(ydx)(zdx) := (
              upExt(0)(ydx)(extIdxSingle).memAddrAlt(
                //PipeMemRmw.modWrIdx
                zdx
              ).resized
            )
          }
        } else { // if (!optEnableClear)
          myWriteAddr(0)(ydx)(zdx) := (
            upExt(0)(ydx)(extIdxSingle).memAddrAlt(
              //PipeMemRmw.modWrIdx
              zdx
            ).resized
          )
        }
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
      for (zdx <- 0 until modRdPortCnt) {
        myWriteData(0)(ydx)(zdx).assignFromBits(
          //upExt(0)(ydx).modMemWord
          if (optEnableClear) (
            Mux[WordT](
              io.clear.fire,
              wordType().getZero,
              upExt(0)(ydx)(extIdxSingle).modMemWord,
            ).asBits
          ) else if (
            //cfg.optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
            cfg.optIncludeOtherMmw
          ) (
            //Mux[WordT](
            //  upExt(0)(ydx)(extIdxSingle).fwdCanDoIt(PipeMemRmw.modWrIdx),
            //  upExt(0)(ydx)(extIdxSingle).modMemWord,
            //  upExt(0)(ydx)(extIdxSingle).otherModMemWord,
            //)
            (
              upExt(0)(ydx)(extIdxSingle).modMemWord.asBits
              | upExt(0)(ydx)(extIdxSingle).otherModMemWord.asBits
            )
          ) else (
            upExt(0)(ydx)(extIdxSingle).modMemWord.asBits
          )
        )
      }
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
        /*LcvFastAndR*/(
          Vec[Bool](
            (
              dbgDoWrite(ydx)
              || (
                if (optEnableClear) (
                  io.clear.fire
                ) else (
                  False
                )
              )
            ),
            //!ClockDomain.isResetActive,
            upExt(1)(ydx)(extIdxUp).modMemWordValid.last,
          ).asBits.asUInt.andR
        )
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
      address=myWriteAddr(0),
      data=myWriteData(0),
      enable=myWriteEnable,
    )
    //--------
  }
  //val cPreLastBackExtraFwd = (
  //  optIncludePreMid0Front
  //) generate (
  //  mod.back.cPreLastBackExtraFwd
  //)
  //val cPreLastBackExtraFwdArea = (
  //  optIncludePreMid0Front
  //) generate (new cPreLastBackExtraFwd.Area {
  //  val upExt = Vec.fill(2)(
  //    mkExt()
  //  )
  //  val myUpExtDel = mod.front.myUpExtDel
  //  for (ydx <- 0 until memArrSize) {
  //    if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) {
  //      for (extIdx <- 0 until extIdxLim) {
  //        upExt(1)(ydx)(extIdx) := (
  //          RegNext(
  //            next=upExt(1)(ydx)(extIdx),
  //            init=upExt(1)(ydx)(extIdx).getZero,
  //          ) 
  //        )
  //        upExt(1)(ydx)(extIdx).allowOverride
  //      }
  //      when (
  //        //up.isFiring
  //        up.isValid
  //      ) {
  //        upExt(1)(ydx)(extIdxUp) := upExt(0)(ydx)(extIdxSingle)
  //      }
  //    } else {
  //      upExt(1)(ydx)(extIdxUp) := upExt(0)(ydx)(extIdxSingle)
  //    }
  //    if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) {
  //      for (extIdx <- 0 to extIdxLim) {
  //        upExt(1)(ydx)(extIdx).modMemWordValid.foreach(current => {
  //          current := False
  //        })
  //      }
  //    }
  //    //--------
  //    //if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) {
  //    if (optIncludePreMid0Front) {
  //      myUpExtDel(myUpExtDel.size - 2)(ydx) := upExt(1)(ydx)
  //      myUpExtDel.last(ydx) := (
  //        RegNextWhen(
  //          next=myUpExtDel(myUpExtDel.size - 2)(ydx),
  //          cond=up.isFiring,
  //          init=myUpExtDel(myUpExtDel.size - 2)(ydx).getZero,
  //        )
  //      )
  //      //when (up.isFiring) {
  //      //  myUpExtDel.last(ydx) := myUpExtDel(myUpExtDel.size - 2)(ydx)
  //      //}
  //    } else {
  //      myUpExtDel.last(ydx) := upExt(1)(ydx)
  //    }
  //    //}
  //  }

  //  val tempUpMod = Vec.fill(2)(
  //    //Vec.fill(memArrSize)(
  //      modType()
  //    //)
  //  )
  //  for (ydx <- 0 until memArrSize) {
  //    if (ydx == 0) {
  //      tempUpMod(0).allowOverride
  //      tempUpMod(0) := up(mod.back.pipePayload)
  //    }
  //    tempUpMod(0).getPipeMemRmwExt(
  //      outpExt=upExt(0)(ydx)(extIdxSingle),
  //      ydx=ydx,
  //      memArrIdx=memArrIdx,
  //    )
  //  }
  //})
  val cLastBack = mod.back.cLastBack
  val cLastBackArea = new cLastBack.Area {
    val upExt = Vec.fill(2)(
      mkExt()
    )
    val myUpExtDel = mod.front.myUpExtDel
    for (ydx <- 0 until memArrSize) {
      if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) {
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
      } else {
        upExt(1)(ydx)(extIdxUp) := upExt(0)(ydx)(extIdxSingle)
      }
      if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) {
        for (extIdx <- 0 to extIdxLim) {
          upExt(1)(ydx)(extIdx).modMemWordValid.foreach(current => {
            current := False
          })
        }
      }
      //--------
      //if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) {
      if (optIncludePreMid0Front) {
        myUpExtDel(myUpExtDel.size - 2)(ydx) := upExt(1)(ydx)
        myUpExtDel.last(ydx) := (
          RegNextWhen(
            next=myUpExtDel(myUpExtDel.size - 2)(ydx),
            cond=up.isFiring,
            init=myUpExtDel(myUpExtDel.size - 2)(ydx).getZero,
          )
        )
        //when (up.isFiring) {
        //  myUpExtDel.last(ydx) := myUpExtDel(myUpExtDel.size - 2)(ydx)
        //}
      } else {
        myUpExtDel.last(ydx) := upExt(1)(ydx)
      }
      //}
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
    }
    //myUpExtDel.last
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
        //myRdMemWord := dualRdMem(ydx).readSync(
        //  address=(
        //    myInpUpExt(ydx)(extIdxSingle).memAddr(PipeMemRmw.modWrIdx)(
        //      PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
        //      downto 0
        //    )
        //  ),
        //  enable=up.isFiring,
        //)
        dualRdMem(ydx).io.ramIo.rdEn := (
          up.isFiring
        )
        dualRdMem(ydx).io.ramIo.rdAddr := (
          myInpUpExt(ydx)(extIdxSingle).memAddr.last(
            PipeMemRmw.addrWidth(wordCount=wordCountArr(ydx)) - 1
            downto 0
          )
        )

        myRdMemWord := (
          dualRdMem(ydx).io.ramIo.rdData
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
        myOutpUpExt(ydx)(extIdxSingle).rdMemWord.last := (
          RegNext(
            next=(
              myOutpUpExt(ydx)(extIdxSingle).rdMemWord.last
            ),
            init=(
              myOutpUpExt(ydx)(extIdxSingle).rdMemWord.last.getZero
            ),
          )
        )
        myOutpUpExt(ydx)(extIdxSingle).modMemWord := (
          myOutpUpExt(ydx)(extIdxSingle).rdMemWord.last
        )
        if (ydx == 0) {
          up(outpPipePayload) := myOutpDualRd
        }
        when (
          up.isValid
          && !rDoIt
        ) {
          rDoIt := True
          myOutpUpExt(ydx)(extIdxSingle).rdMemWord.last := (
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
}


case class StmFwdPipeMemRmwConfig[
  WordT <: Data,
](
  wordType: HardType[WordT],
  wordCountArr: Seq[Int],
  modRdPortCnt: Int,
  modStageCnt: Int,
  init: Option[Seq[Seq[WordT]]]=None,
  initBigInt: Option[Seq[Seq[BigInt]]]=None,
  optHaveZeroReg: Option[Int]=Some(0x0),
  memRamStyleAltera: String="no_rw_check, M10K",
  memRamStyleXilinx: String="block",
  memRwAddrCollisionXilinx: String="",
  memArrIdx: Int=0
) {
  def optModHazardKind = PipeMemRmw.ModHazardKind.Fwd
  val optIncludePreMid0Front = true

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
  def numMyUpExtDel2 = (
    PipeMemRmw.numMyUpExtDel2(
      optModHazardKind=optModHazardKind,
      modStageCnt=modStageCnt,
      optIncludePreMid0Front=optIncludePreMid0Front,
    )
    //cfg.numMyUpExtDel2
  )
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
  val modMemWordValidSize: Int = (
    PipeMemRmw.modMemWordValidSize
    //modRdPortCnt
    //1
    //2
  )
}

trait StmFwdPipeMemRmwPayloadBase[
  WordT <: Data,
] extends Bundle {
  //--------
  def setPipeMemRmwExt(
    inpExt: StmFwdPipeMemRmwPayloadExt[WordT],
    ydx: Int,
    memArrIdx: Int,
  ): Unit

  def getPipeMemRmwExt(
    outpExt: StmFwdPipeMemRmwPayloadExt[WordT],
      // this is essentially a return value
    ydx: Int,
    memArrIdx: Int,
  ): Unit
  //--------
}

case class StmFwdPipeMemRmwPayloadExtMainMemAddr[
  WordT <: Data
](
  cfg: StmFwdPipeMemRmwConfig[WordT],
  wordCount: Int,
) extends Bundle {
  //--------
  val memAddrFwdMmw = Vec.fill(cfg.modRdPortCnt)(
    Vec.fill(cfg.numMyUpExtDel2)(
      UInt(log2Up(wordCount) bits)
    )
  )
  val memAddrFwd = Vec.fill(cfg.modRdPortCnt)(
    Vec.fill(cfg.numMyUpExtDel2)(
      UInt(log2Up(wordCount) bits)
    )
  )
  val memAddrFwdCmp = Vec.fill(cfg.modRdPortCnt)(
    Vec.fill(cfg.numMyUpExtDel2)(
      UInt(1 bits)
    )
  )
  val memAddr = Vec.fill(cfg.modRdPortCnt + 1)(
    UInt(log2Up(wordCount) bits)
  )
  //--------
}

case class StmFwdPipeMemRmwPayloadExtMainNonMemAddrMost[
  WordT <: Data
](
  cfg: StmFwdPipeMemRmwConfig[WordT],
  wordCount: Int,
) extends Bundle {
  //--------
  val fwdIdx = Vec.fill(cfg.modRdPortCnt)(
    UInt(log2Up(cfg.numMyUpExtDel2 + 1) bits)
  )
  val fwdCanDoIt = Vec.fill(cfg.modRdPortCnt)(
    Bool()
  )
  //--------
  val modMemWordValid = Vec.fill(cfg.modMemWordValidSize)(
    Bool()
  )
  val rdMemWord = Vec.fill(cfg.modRdPortCnt)(
    cfg.wordType()
  )
  //--------
}
case class StmFwdPipeMemRmwPayloadExt[
  WordT <: Data
](
  cfg: StmFwdPipeMemRmwConfig[WordT],
  wordCount: Int,
) extends Bundle {
  val mainNonMemAddrMost = StmFwdPipeMemRmwPayloadExtMainNonMemAddrMost(
    cfg=cfg,
    wordCount=wordCount,
  )
  val mainMemAddr = StmFwdPipeMemRmwPayloadExtMainMemAddr(
    cfg=cfg,
    wordCount=wordCount,
  )
  val modMemWord = cfg.wordType()
  def memAddrFwdMmw = mainMemAddr.memAddrFwdMmw
  def memAddrFwd = mainMemAddr.memAddrFwd
  def memAddrFwdCmp = mainMemAddr.memAddrFwdCmp
  def memAddr = mainMemAddr.memAddr
  def fwdIdx = mainNonMemAddrMost.fwdIdx
  def fwdCanDoIt = mainNonMemAddrMost.fwdCanDoIt
  def modMemWordValid = mainNonMemAddrMost.modMemWordValid
  def rdMemWord = mainNonMemAddrMost.rdMemWord
}

case class StmFwdPipeMemRmwFwd[
  WordT <: Data,
](
  cfg: StmFwdPipeMemRmwConfig[WordT],
) extends Bundle {
  def wordCount = cfg.wordCountMax
  //--------
  val myFindFirst_0 = (
    Vec.fill(cfg.memArrSize)(
      Vec.fill(cfg.modRdPortCnt)(
        Bool()
      )
    )
  )
  //--------
  val myFindFirst_1 = (
    Vec.fill(cfg.memArrSize)(
      Vec.fill(cfg.modRdPortCnt)(
        UInt(log2Up(cfg.numMyUpExtDel2 + 1) bits)
      )
    )
  )
  //--------
  val myFwdIdx = (
    Vec.fill(cfg.memArrSize)(
      Vec.fill(cfg.modRdPortCnt)(
        UInt(log2Up(cfg.numMyUpExtDel2 + 1) bits)
      )
    )
  )
  val myFwdMmwValidUp = (
    Vec.fill(cfg.memArrSize)(
      Vec.fill(cfg.modRdPortCnt)(
        Bool()
      )
    )
  )
  val myFwdDataUp = (
    Vec.fill(cfg.memArrSize)(
      Vec.fill(cfg.modRdPortCnt)(
        cfg.wordType()
      )
    )
  )
  val myUpIsValid = (
    Bool()
    //Vec.fill(memArrSize)(
    //  Vec.fill(modRdPortCnt)(
    //    Bool()
    //  )
    //)
  )
  val myUpIsFiring = (
    Bool()
    //Vec.fill(memArrSize)(
    //  Vec.fill(modRdPortCnt)(
    //    Bool()
    //  )
    //)
  )
  //--------
  def mkExt(
  ): Vec[StmFwdPipeMemRmwPayloadExt[WordT]] = {
    val ret = Vec.fill(cfg.memArrSize)(
      StmFwdPipeMemRmwPayloadExt(
        cfg=cfg,
        wordCount=wordCount
      )
    )
    ret
  }

  val myFwdData = (
    Vec.fill(cfg.memArrSize)(
      Vec.fill(cfg.modRdPortCnt)(
        cfg.wordType()
      )
    )
  )

  val myFwdStateData = (
    Vec.fill(cfg.memArrSize)(
      Vec.fill(cfg.modRdPortCnt)(
        Vec.fill(cfg.numMyUpExtDel2 + 1)(
          cfg.wordType()
        )
      )
    )
  )

  val myUpExtDel2 = (
    Vec.fill(cfg.numMyUpExtDel2)(
      mkExt()
    )
  )
  val myUpExtDel2FindFirstVec = (
    Vec.fill(cfg.memArrSize)(
      Vec.fill(cfg.modRdPortCnt)(
        Vec.fill(cfg.numMyUpExtDel2 + 1)(
          //Bool()
          Flow(Flow(cfg.wordType()))
        )
      )
    )
  )
}
case class StmFwdPipeMemRmwDoFwdArea[
  WordT <: Data
](
  fwd: StmFwdPipeMemRmwFwd[WordT],
  fwdAreaName: String,
  setToMyFwdDataFunc: (
    Int,      // ydx
    Int,      // zdx
    WordT,    // myFwdData
  ) => Unit,
  optFirstFwdRdMemWord: Option[Vec[Vec[WordT]]]=None,
) extends Area {
  def cfg = fwd.cfg
  object FwdState extends SpinalEnum(
    defaultEncoding=binaryOneHot
  ) {
    val
      WAIT_FIRST_UP_VALID,
      WAIT_DATA,
      WAIT_UP_FIRE
      = newElement();
  }
  val rFwdState = {
    val temp = Reg(
      Vec.fill(cfg.memArrSize)(
        Vec.fill(cfg.modRdPortCnt)(
          Vec.fill(cfg.numMyUpExtDel2 + 1)(
            FwdState()
          )
        )
      )
    )
    for (ydx <- 0 until cfg.memArrSize) {
      for (zdx <- 0 until cfg.modRdPortCnt) {
        for (kdx <- 0 until cfg.numMyUpExtDel2 + 1) {
          temp(ydx)(zdx)(kdx).init(
            FwdState.WAIT_FIRST_UP_VALID
          )
        }
      }
    }
    temp.setName(
      fwdAreaName
      + s"_rFwdState"
    )
    temp
  }
  for (ydx <- 0 until cfg.memArrSize) {
    for (zdx <- 0 until cfg.modRdPortCnt) {
      val firstFwdRdMemWord: (Boolean, WordT) = (
        optFirstFwdRdMemWord match {
          case Some(myFirstFwdRdMemWord) => {
            (
              true,
              myFirstFwdRdMemWord(ydx)(zdx)
            )
          }
          case None => {
            (false, cfg.wordType())
          }
        }
      )
      def firstFwd = firstFwdRdMemWord._1
      val myFindFirstUp = (
        (True, fwd.myFwdIdx(ydx)(zdx))
        .setName(
          s"${fwdAreaName}_myFindFirstUp_${ydx}_${zdx}"
        )
      )
      def tempMyFindFirstUp_0 = fwd.myFindFirst_0(ydx)(zdx)
      def tempMyFindFirstUp_1 = fwd.myFindFirst_1(ydx)(zdx)
      def tempMyFwdData = fwd.myFwdData(ydx)(zdx)
      tempMyFindFirstUp_1.allowOverride
      val myFwdCondUp = (
        firstFwd
      ) generate (
        tempMyFindFirstUp_0
        .setName(s"${fwdAreaName}_myFwdCondUp_${ydx}_${zdx}")
      )

      if (firstFwd) {
        tempMyFindFirstUp_0 := myFindFirstUp._1
        tempMyFindFirstUp_1 := myFindFirstUp._2.resized
      }
      if (firstFwd) {
        def mySetToMyFwdUp(): Unit = {
          tempMyFwdData := (
            fwd.myFwdStateData(ydx)(zdx)(tempMyFindFirstUp_1)
          )
          for (kdx <- 0 until cfg.numMyUpExtDel2 + 1) {
            if (kdx < cfg.numMyUpExtDel2 /*- 1*/) {
              if (
                //kdx <= 1
                //kdx == 0
                false
              ) {
                fwd.myFwdStateData(ydx)(zdx)(kdx) := (
                  fwd.myUpExtDel2FindFirstVec(ydx)(zdx)(
                    kdx
                  ).payload.payload
                )
              } else {
                println(
                  s"find me: kdx != 0: ${kdx} ${cfg.numMyUpExtDel2}"
                )
                fwd.myFwdStateData(ydx)(zdx)(kdx) := (
                  RegNext(
                    next=fwd.myFwdStateData(ydx)(zdx)(kdx),
                    init=fwd.myFwdStateData(ydx)(zdx)(kdx).getZero,
                  )
                )
                when (
                  //fwd.myUpIsValid
                  //&&
                  rFwdState(ydx)(zdx)(kdx) === FwdState.WAIT_DATA
                ) {
                  //tempMyFwdData := myFwdDataUp
                  fwd.myFwdStateData(ydx)(zdx)(kdx) := (
                    fwd.myUpExtDel2FindFirstVec(ydx)(zdx)(
                      kdx
                    ).payload.payload
                  )
                }
              }
              when (
                fwd.myUpIsValid
                //&& rFwdState(ydx)(zdx)(kdx) === FwdState.WAIT_DATA
                && fwd.myUpExtDel2FindFirstVec(ydx)(zdx)(kdx).payload.valid
              ) {
                //rFwdStateValid(ydx)(zdx)(kdx) := True
                rFwdState(ydx)(zdx)(kdx) := FwdState.WAIT_UP_FIRE
                //fwd.myFwdStateData(ydx)(zdx)(kdx) := (
                //  fwd.myUpExtDel2FindFirstVec(ydx)(zdx)(
                //    kdx
                //  ).payload.payload
                //)
              }
              when (
                fwd.myUpIsFiring
                //&& rFwdState(ydx)(zdx)(kdx) === FwdState.WAIT_UP_FIRE
              ) {
                //rFwdStateValid(ydx)(zdx)(kdx) := False
                rFwdState(ydx)(zdx)(kdx) := FwdState.WAIT_DATA
              }
            } else {
              fwd.myFwdStateData(ydx)(zdx)(kdx) := (
                fwd.myUpExtDel2FindFirstVec(ydx)(zdx)(kdx).payload.payload
              )
            }
          }
        }
        mySetToMyFwdUp()
      }
      setToMyFwdDataFunc(ydx, zdx, tempMyFwdData)
    }
  }
}
case class StmFwdPipeMemRmwIo[
  WordT <: Data,
  ModT <: StmFwdPipeMemRmwPayloadBase[WordT],
](
  cfg: StmFwdPipeMemRmwConfig[WordT],
  modType: HardType[ModT],
) extends Bundle {
  // front of the pipeline
  val push = slave(Stream(modType()))
  val modPop = master(Stream(modType()))
  val modPush = slave(Stream(modType()))

  // back of the pipeline (output)
  val pop = master(Stream(modType()))

  val midModStages = (
    cfg.modStageCnt > 0
  ) generate (
    in(
      Vec.fill(cfg.modStageCnt)(
        modType()
      )
    )
  )
}

case class StmFwdPipeMemRmw[
  WordT <: Data,
  ModT <: StmFwdPipeMemRmwPayloadBase[WordT],
](
  cfg: StmFwdPipeMemRmwConfig[WordT],
  modType: HardType[ModT],
) extends Component {
  //--------
  val io = StmFwdPipeMemRmwIo(
    cfg=cfg,
    modType=modType,
  )
  //--------
  val modMemArr = {
    val temp = new ArrayBuffer[Array[
      RamSdpPipe[WordT]
    ]]()
    for (ydx <- 0 until cfg.memArrSize) {
      temp += (
        Array.fill(cfg.modRdPortCnt)(
          RamSdpPipe(
            cfg=RamSdpPipeConfig(
              wordType=cfg.wordType(),
              depth=cfg.wordCountArr(ydx),
              optIncludeWrByteEn=false,
              init=(
                cfg.init match {
                  case Some(myInit) => {
                    Some(myInit(ydx))
                  }
                  case None => {
                    None
                  }
                }
              ),
              initBigInt=(
                cfg.initBigInt match {
                  case Some(myInitBigInt) => {
                    Some(myInitBigInt(ydx))
                  }
                  case None => {
                    None
                  }
                }
              ),
              arrRamStyleAltera=cfg.memRamStyleAltera,
              arrRamStyleXilinx=cfg.memRamStyleXilinx,
              arrRwAddrCollisionXilinx=cfg.memRwAddrCollisionXilinx,
            )
          )
        )
      )
    }
    temp
  }
  def mkOneExt(): StmFwdPipeMemRmwPayloadExt[WordT] = (
    StmFwdPipeMemRmwPayloadExt(
      cfg=cfg,
      wordCount=cfg.wordCountMax,
    )
  )
  def mkExt(): Vec[StmFwdPipeMemRmwPayloadExt[WordT]] = {
    val ret = Vec.fill(cfg.memArrSize)(
      mkOneExt()
    )
    ret
  }
  def mkFwd(): StmFwdPipeMemRmwFwd[WordT] = {
    StmFwdPipeMemRmwFwd(cfg=cfg)
  }
  val myFwd = mkFwd()
  //--------
  def findFirstFunc(
    currMemAddr: UInt,
    prevMemAddr: UInt,
    curr: StmFwdPipeMemRmwPayloadExt[WordT],
    prev: StmFwdPipeMemRmwPayloadExt[WordT],
    ydx: Int,
    zdx: Int,
    idx: Int,
    forceFalse: Boolean=false,
  ): Flow[Flow[WordT]] = {
    val ret = Flow(Flow(cfg.wordType()))
    ret.valid := (
      if (!forceFalse) (
        currMemAddr(0)
      ) else (
        False
      )
    )
    ret.payload.valid := {
      //if (idx == 0) (
      //  True
      //  //False
      //) else (
        prev.modMemWordValid(
          if (zdx < prev.modMemWordValid.size) (
            zdx
          ) else (
            prev.modMemWordValid.size - 1
          )
        )
      //)
    }
    ret.payload.payload := (
      prev.modMemWord
    )
    ret
  }
  //--------

  //val midPipePayload = Vec.fill(2)(
  //  Vec.fill(2)(
  //    modType()
  //  )
  //)
  val myRdMemWord = Vec.fill(cfg.memArrSize)(
    Vec.fill(cfg.modRdPortCnt)(
      cfg.wordType()
    )
  )
  val myNonFwdRdMemWord = Vec.fill(cfg.memArrSize)(
    Vec.fill(cfg.modRdPortCnt)(
      cfg.wordType()
    )
  )
  val myUpExtDel = /*KeepAttribute*/(
    Vec.fill(
      PipeMemRmw.numPostFrontStages(
        optModHazardKind=cfg.optModHazardKind,
        modStageCnt=cfg.modStageCnt,
        optIncludePreMid0Front=cfg.optIncludePreMid0Front,
      )
      + 1 
    )(
      mkExt()
    )
  )
  println(
    s"myUpExtDel.size: ${myUpExtDel.size}"
  )
  def myUpExtDel2 = (
    myFwd.myUpExtDel2
    //.setName(s"${pipeName}_mod_front_myUpExtDel2")
  )
  println(
    s"myUpExtDel2.size: ${myUpExtDel2.size}"
  )

  def mkPreFwdArea(
    upIsFiring: Bool,
    upExtElem: Vec[StmFwdPipeMemRmwPayloadExt[WordT]]
  ) = new Area {
    for (ydx <- 0 until cfg.memArrSize) {
      def myMemAddrFwdCmp = upExtElem(ydx).memAddrFwdCmp
      def myFwdIdx = upExtElem(ydx).fwdIdx
      val myHistMemAddr = (
        /*KeepAttribute*/(
          History[UInt](
            that=upExtElem(ydx).memAddr.last,
            // `length=numMyUpExtDel2 + 1` because `History` includes the
            // current value of `that`.
            // This might not be relevant any more?
            length=2,//mod.front.myUpExtDel2.size + 1, 
            when=upIsFiring,
            init=upExtElem(ydx).memAddr.last.getZero,
          )
        )
        .setName(s"myPreFwdArea_myHistMemAddr_${ydx}")
      )
      for (zdx <- 0 until cfg.modRdPortCnt) {
        val toFindFirst = Vec.fill(cfg.numMyUpExtDel2  + 1)(
          Flow(
            UInt(PipeMemRmw.addrWidth(cfg.wordCountArr(ydx)) bits)
          )
        )
        for ((item, itemIdx) <- toFindFirst.view.zipWithIndex) {
          val tempMyUpExtDel = myUpExtDel(itemIdx)(ydx)
          if (itemIdx != cfg.numMyUpExtDel2) {
            item.valid := myMemAddrFwdCmp(zdx)(itemIdx)(0)
            item.payload := tempMyUpExtDel.memAddrFwdMmw(zdx)(itemIdx)
          } else {
            item.valid := True
            item.payload := upExtElem(ydx).memAddr(zdx)
          }
        }
        myFwdIdx(zdx) := (
          toFindFirst.sFindFirst(
            current => (current.fire === True)
          )._2
        )
        upExtElem(ydx).memAddrFwdMmw(zdx).foreach(current => {
          current := (
            upExtElem(ydx).memAddr.last
          )
        })
        upExtElem(ydx).memAddrFwd(zdx).foreach(current => {
          current := upExtElem(ydx).memAddr(zdx)
        })
        for (idx <- 0 until myUpExtDel2.size + 1) {
          println(
            f"myHistMemAddr debug: ${zdx} ${idx} ${idx - 1} "
            + f"${myUpExtDel2.size}"
          )
          if (idx > 0) {
            def tempMemAddrFwdCmp = myMemAddrFwdCmp(zdx)(idx - 1)
            tempMemAddrFwdCmp.allowOverride
            for (jdx <- 0 until tempMemAddrFwdCmp.getWidth) {
              val myZeroRegCond = (
                cfg.optHaveZeroReg match {
                  case Some(myZeroRegIdx) => {
                    (
                      upExtElem(ydx).memAddr(zdx)
                      =/= myZeroRegIdx
                    )
                  }
                  case None => {
                    True
                  }
                }
              )
              if (idx == 1) {
                val tempMyUpExtDel = (
                  myUpExtDel(idx - 1)(ydx)
                )
                tempMemAddrFwdCmp(jdx) := (
                  (
                    upExtElem(ydx).memAddr(
                      zdx
                    ) === (
                      myHistMemAddr(idx)
                    )
                  )
                  && (
                    myZeroRegCond
                  )
                  //&& (
                  //  //tempMyUpExtDel.modMemWordValid({
                  //  //  if (
                  //  //    idx < tempMyUpExtDel.modMemWordValid.size
                  //  //  ) (
                  //  //    idx
                  //  //  ) else (
                  //  //    tempMyUpExtDel.modMemWordValid.size - 1 
                  //  //  )
                  //  //})
                  //)
                  && (
                    tempMyUpExtDel.fwdCanDoIt(
                      zdx
                    )
                  )
                )
              } else {
                val tempMyUpExtDel = (
                  myUpExtDel(idx - 1)(ydx)
                )
                println(
                  s"tempMyUpExtDel debug: (${idx - 1})(${ydx})  "
                  + s"${jdx}"
                )
                tempMemAddrFwdCmp(
                  jdx
                  //0
                ) := (
                  (
                    upExtElem(ydx).memAddr(
                      zdx
                    ) === (
                      //myHistMemAddr(idx)
                      // `idx - 1` is because we want the stage *before*
                      // the one we're actually interested in.
                      if (idx == myUpExtDel2.size) (
                        tempMyUpExtDel.memAddrFwdMmw(zdx).last
                      ) else (
                        tempMyUpExtDel.memAddrFwdMmw(zdx)(idx)
                      )
                    )
                  )
                  && myZeroRegCond
                  && tempMyUpExtDel.fwdCanDoIt(zdx)
                  //&& (
                  //  //if (idx == 1) (
                  //    True
                  //  //) else (
                  //  //  tempMyUpExtDel.modMemWordValid({
                  //  //    if (
                  //  //      idx < tempMyUpExtDel.modMemWordValid.size
                  //  //    ) (
                  //  //      idx
                  //  //    ) else (
                  //  //      tempMyUpExtDel.modMemWordValid.size - 1 
                  //  //    )
                  //  //  })
                  //  //)
                  //)
                  //&& (
                  //  tempMyUpExtDel.valid(0)
                  //)
                )
              }
            }
          }
        }
      }
    }
  }
  myRdMemWord := myNonFwdRdMemWord
  for (idx <- 0 until myUpExtDel2.size) {
    for (ydx <- 0 until cfg.memArrSize) {
      val tempUpExt = myUpExtDel2(idx)(ydx)
      tempUpExt := myUpExtDel(idx + 1)(ydx)
    }
  }
  //val myUpExtDelFullFindFirstVecNotPostDelay = (
  //  Vec.fill(cfg.memArrSize)(
  //    Vec.fill(cfg.modRdPortCnt)(
  //      Vec.fill(myUpExtDel.size)(
  //        Flow(Flow(cfg.wordType()))
  //      )
  //    )
  //  )
  //)
  val myUpExtDel2FindFirstVec = /*KeepAttribute*/(
    myFwd.myUpExtDel2FindFirstVec
  )
  //--------
  val dbgDoWrite = Vec.fill(cfg.memArrSize)(
    Bool()
  )
  val myWriteAddr = Vec.fill(cfg.memArrSize)(
    Vec.fill(cfg.modRdPortCnt)(
      cloneOf(myUpExtDel(0)(0).memAddr.last)
    )
  )
  val myWriteData = Vec.fill(cfg.memArrSize)(
    Vec.fill(cfg.modRdPortCnt)(
      cloneOf(myUpExtDel(0)(0).modMemWord)
    )
  )
  val myWriteEnable = Vec.fill(cfg.memArrSize)(
    Bool()
  )

  val rTempWord = Vec.fill(cfg.memArrSize)(
    Reg(dataType=cfg.wordType())
    init(cfg.wordType().getZero)
  )

  if (cfg.modStageCnt > 0) {
    for (idx <- 0 until io.midModStages.size) {
      for (ydx <- 0 until cfg.memArrSize) {
        val myExt = mkExt()
        io.midModStages(idx).getPipeMemRmwExt(
          outpExt=myExt(ydx),
          ydx=ydx,
          memArrIdx=cfg.memArrIdx,
        )
        val tempIdx = (
          PipeMemRmw.numPostFrontPreWriteStages(
            optModHazardKind=cfg.optModHazardKind,
            modStageCnt=cfg.modStageCnt,
            optIncludePreMid0Front=cfg.optIncludePreMid0Front,
          )
          - cfg.modStageCnt
          + idx 
          - (
            if (cfg.optIncludePreMid0Front) (
              1
            ) else (
              0
            )
          )
        )
        myUpExtDel(tempIdx)(ydx) := myExt(ydx)
      }
    }
  }
  //--------
  val midPushStmVec = Vec.fill(2)(
    Stream(modType())
  )
  midPushStmVec.head <-< io.push
  io.modPop <-< midPushStmVec.last
  val myPushArea = new Area {
    val upExt = Vec.fill(2)(
      mkExt()
    )
    val tempCond = Bool()
    for (ydx <- 0 until cfg.memArrSize) {
      upExt(1)(ydx) := (
        RegNext(
          next=upExt(1)(ydx),
          init=upExt(1)(ydx).getZero,
        )
      )
    }
    upExt(1).allowOverride

    val tempUpMod = Vec.fill(upExt.size)(
      modType()
    )
    tempUpMod(0).allowOverride
    tempUpMod(0) := (
      //up(mod.front.inpPipePayload)
      io.push.payload
    )
    for (ydx <- 0 until cfg.memArrSize) {
      tempUpMod(0).getPipeMemRmwExt(
        outpExt=upExt(0)(ydx),
        ydx=ydx,
        memArrIdx=cfg.memArrIdx,
      )
    }
    for (ydx <- 0 until cfg.memArrSize) {
      when (
        io.push.valid
        //up.isValid
        //up.isFiring
      ) {
        upExt(1)(ydx) := upExt(0)(ydx)
      }

      //upExt(1)(ydx).valid.foreach(current => {
      //  current := up.isValid
      //})
      //upExt(1)(ydx).ready := up.isReady
      //upExt(1)(ydx).fire := up.isFiring
    }
    val tempSharedEnable = Vec.fill(
      cfg.modRdPortCnt 
    )(
      midPushStmVec.head.ready
    )
    tempCond := tempSharedEnable.last
    for (ydx <- 0 until cfg.memArrSize) {
      for (zdx <- 0 until cfg.modRdPortCnt) {
        val modMem = modMemArr(ydx)(zdx)
        modMem.io.rdEn := True
        val tempAddrWidth = log2Up(cfg.wordCountArr(ydx))
        modMem.io.rdAddr := (
          RegNext(
            next=modMem.io.rdAddr,
            init=modMem.io.rdAddr.getZero,
          )
        )
        when (tempSharedEnable.last) {
          modMem.io.rdAddr := (
            upExt(1)(ydx).memAddr(zdx)(
              tempAddrWidth - 1 downto 0
            )
          )
        }
        //val tempRdData = modMem.io.rdData
        //myNonFwdRdMemWord
      }
    }
    val tempCondDown = midPushStmVec.head.fire
    //tempUpMod(1) := RegNext(tempUpMod(1), init=tempUpMod(1).getZero)
    tempUpMod(1) := tempUpMod(0)
    tempUpMod(1).allowOverride
    for (ydx <- 0 until cfg.memArrSize) {
      //upExt(1)(ydx).memAddrAlt.allowOverride
      //upExt(1)(ydx).memAddrAlt.foreach(item => {
      //  item := (
      //    upExt(1)(ydx).memAddr.last
      //  )
      //})
      tempUpMod(1).setPipeMemRmwExt(
        inpExt=upExt(1)(ydx),
        ydx=ydx,
        memArrIdx=cfg.memArrIdx,
      )
      if (ydx == 0) {
        //for (idx <- 0 until up(midPipePayload(0)).size) {
        //  up(midPipePayload(0))(idx) := tempUpMod(1)
        //}
        midPushStmVec.head.payload := tempUpMod(1)
      }
    }
  }
  val myMidPushArea = new Area {
    val upExt = Vec.fill(3)(mkExt())
    val tempUpMod = Vec.fill(3)(
      modType()
    )
    tempUpMod(0).allowOverride
    //tempUpMod(0) := up(mod.front.midPipePayload(0))(0)
    tempUpMod(0) := midPushStmVec.head.payload
    tempUpMod(1).allowOverride
    tempUpMod(1) := tempUpMod(0)
    tempUpMod(2).allowOverride
    tempUpMod(2) := RegNext(tempUpMod(2), init=tempUpMod(2).getZero)
    upExt.foreach(outerItem => {
      outerItem.foreach(item => {
        item := RegNext(item, init=item.getZero)
        item.allowOverride
      })
    })
    for (ydx <- 0 until cfg.memArrSize) {
      //upExt(1)(ydx) := (
      //  RegNext(
      //    next=upExt(1)(ydx),
      //    init=upExt(1)(ydx).getZero,
      //  )
      //)
      upExt(1)(ydx).allowOverride
      when (midPushStmVec.head.valid) {
        upExt(1)(ydx) := upExt(0)(ydx)
      }

      //upExt(1)(ydx).valid.foreach(current => {
      //  current := up.isValid
      //})
      //upExt(1)(ydx).ready := up.isReady
      //upExt(1)(ydx).fire := up.isFiring
    }
    for (ydx <- 0 until cfg.memArrSize) {
      tempUpMod(0).getPipeMemRmwExt(
        outpExt=upExt(0)(ydx),
        ydx=ydx,
        memArrIdx=cfg.memArrIdx,
      )
      tempUpMod(1).setPipeMemRmwExt(
        inpExt=upExt(1)(ydx),
        ydx=ydx,
        memArrIdx=cfg.memArrIdx,
      )
      //upExt(2)(ydx) := (
      //  upExt(1)(ydx)
      //)
      tempUpMod(2).getPipeMemRmwExt(
        outpExt=upExt(2)(ydx),
        ydx=ydx,
        memArrIdx=cfg.memArrIdx,
      )
    }
    //for (idx <- 0 until up(mod.front.midPipePayload(1)).size) {
    //  up(mod.front.midPipePayload(1))(idx) := tempUpMod(2)
    //}
    //upExt(0) := up(mod.front.midPipePayload)(0)
    midPushStmVec.head.translateInto(
      into=midPushStmVec.last
    )(
      dataAssignment=(
        (myLast, myHead) => {
          myLast := tempUpMod.last
        }
      )
    )
    val myPreFwdArea = mkPreFwdArea(
      upIsFiring=midPushStmVec.head.fire,
      upExtElem=upExt(1),
    )
    
    for (ydx <- 0 until cfg.memArrSize) {
      for (zdx <- 0 until cfg.modRdPortCnt) {
        val modMem = modMemArr(ydx)(zdx)
        //when (midPushStmVec.head.valid) {
        //}
        val tempRdData = modMem.io.rdData
        myNonFwdRdMemWord(ydx)(zdx) := tempRdData
      }
    }
  }
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
//  memRamStyleXilinx: String="auto",
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
