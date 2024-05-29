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
  modStageCnt: Int,
  //optSimpleIsWr: Option[Boolean]=None,
  //optUseModMemAddr: Boolean=false,
  optEnableModDuplicate: Boolean=true,
) extends Bundle {
  //--------
  def debug: Boolean = {
    GenerationFlags.formal {
      return true
    }
    return false
  }
  //--------
  val memAddr = UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
  val hazardCmp = hazardCmpType()
  //val modMemAddrRaw = (optUseModMemAddr) generate cloneOf(memAddr)
  //def modMemAddr = (
  //  if (optUseModMemAddr) {
  //    modMemAddrRaw
  //  } else { // if (!optUseModMemAddr)
  //    memAddr
  //  }
  //)
  val helperForceWr = Bool()
  val modMemWord = wordType()
  val rdMemWord = wordType()
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
  val hazardId = (optEnableModDuplicate) generate (
    SInt(log2Up(
      PipeMemRmw.numPostFrontStages(
        modStageCnt=modStageCnt,
      )
    ) + 3 bits)
    //UInt(log2Up(modStageCnt) bits)
  )
  def getHazardIdIdleVal() = (
    -1
  )
  def doInitHazardId(): Unit = {
    hazardId := getHazardIdIdleVal()
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
  val dbgModMemWord = (debug) generate (
    wordType()
  )
  val dbgMemReadSync = (debug) generate (
    wordType()
  )
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
    modStageCnt: Int,
  ) = (
    numPostFrontPreWriteStages(
      modStageCnt=modStageCnt
    )
    + 1
  )
  def numPostFrontPreWriteStages(
    modStageCnt: Int
  ) = (
    //modStageCnt
    //3 + modStageCnt //+ 1
    //2 + modStageCnt //+ 1
    1 + modStageCnt //+ 1
    //- 1
    //+ 1
  )
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
//case class PipeMemRmwClearPaylow(
//  wordCount: Int
//)
case class PipeMemRmwIo[
  WordT <: Data,
  HazardCmpT <: Data,
  ModT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
  DualRdT <: PipeMemRmwPayloadBase[WordT, HazardCmpT],
](
  wordType: HardType[WordT],
  wordCount: Int,
  modType: HardType[ModT],
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
  optEnableModDuplicate: Boolean=true,
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
      UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
    ))
  )
  //if (optEnableClear) {
  //  if (vivadoDebug) {
  //    clear.addAttribute("MARK_DEBUG", "TRUE")
  //  }
  //}

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
    /*(modStageCnt > 1)*/ optEnableModDuplicate generate (
      /*in*/(
        Vec.fill(
          modStageCnt //- 1 //- 2
        )(modType())
      )
    )
  )
  if (optEnableModDuplicate) {
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
  modStageCnt: Int,
  pipeName: String,
  linkArr: Option[ArrayBuffer[Link]]=None,
  memArrIdx: Int=0,
  //optDualRdType: Option[HardType[DualRdT]]=None,
  dualRdType: HardType[DualRdT]=PipeMemRmwDualRdTypeDisabled[
    WordT, HazardCmpT,
  ](),
  optDualRd: Boolean=false,
  //dualRdSize: Int=0,
  init: Option[Seq[WordT]]=None,
  initBigInt: Option[Seq[BigInt]]=None,
  //forFmax: Boolean=false,
  //optExtraCycleLatency: Boolean=false,
  //optDisableModRd: Boolean=false,
  optEnableModDuplicate: Boolean=true,
  optEnableClear: Boolean=false,
  memRamStyle: String="auto",
  vivadoDebug: Boolean=false,
)(
  doHazardCmpFunc: Option[
    (
      PipeMemRmwPayloadExt[WordT, HazardCmpT],
      PipeMemRmwPayloadExt[WordT, HazardCmpT],
      //Int,
      Boolean,
    ) => Bool
  ]=None,
  doPrevHazardCmpFunc: Boolean=false,
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
    modStageCnt=modStageCnt,
    dualRdType=dualRdType(),
    //dualRdSize=dualRdSize,
    optDualRd=optDualRd,
    optEnableModDuplicate=optEnableModDuplicate,
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
  val modMem = (optEnableModDuplicate) generate (
    mkMem()
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
    if (optEnableModDuplicate) {
      writeFunc(modMem)
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
    val ret = PipeMemRmwPayloadExt(
      wordType=wordType(),
      wordCount=wordCount,
      hazardCmpType=hazardCmpType(),
      modStageCnt=modStageCnt,
      optEnableModDuplicate=optEnableModDuplicate,
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
    val front = new Area {
      val pipe = PipeHelper(linkArr=myLinkArr)
      //val inpPipePayload = Payload(modType())
      def inpPipePayload = io.frontPayload
      val midPipePayload = Payload(modType())
      //val outpPipePayload = Payload(modType())
      def outpPipePayload = io.modFrontPayload
      val myRdMemWord = wordType()
      //val rRdMemWord1 = Reg(wordType()) init(myRdMemWord.getZero)
      val dbgRdMemWord = (debug) generate (
        Payload(wordType())
      )
      val myUpExtDel = KeepAttribute(
        Vec.fill(
          PipeMemRmw.numPostFrontStages
          //PipeMemRmw.numPostFrontPreWriteStages
          (
            modStageCnt=modStageCnt,
          ) + 1 //- 1
        )(
          /*Reg*/(mkExt(myVivadoDebug=true))
          //init(mkExt().getZero)
        )
      )
      val myUpExtDelPreBack = KeepAttribute(
        Vec.fill(myUpExtDel.size - 1)(
          mkExt()
        )
      )
      for (idx <- 0 until myUpExtDelPreBack.size) {
        myUpExtDelPreBack(idx) := myUpExtDel(idx)
      }
      val myUpExtDelPreBackFindFirstVecNotPostDelay = KeepAttribute(
        Vec.fill(myUpExtDelPreBack.size)(
          Bool()
        )
      )
      val myUpExtDelPreBackFindFirstVecIsPostDelay = KeepAttribute(
        Vec.fill(myUpExtDelPreBack.size)(
          Bool()
        )
      )
      //if (vivadoDebug) {
      //  myUpExtDel.addAttribute("MARK_DEBUG", "TRUE")
      //}
      //println(s"myUpExtDelPreBack.size: ${myUpExtDelPreBack.size}")
      if (optEnableModDuplicate) {
        for (
          //idx <- 0 until modStageCnt - 1
          idx <- 0 until io.midModStages.size
        ) {
          val myExt = mkExt()
          io.midModStages(idx).getPipeMemRmwExt(
            outpExt=myExt,
            memArrIdx=memArrIdx,
          )
          val tempIdx = (
            PipeMemRmw.numPostFrontPreWriteStages(
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
          ) := myExt
        }
      }


      val cFront = pipe.addStage(
        name=pipeName + "_Front"
      )
      val cIoFront = DirectLink(
        up=io.front,
        down=cFront.up,
      )
      myLinkArr += cIoFront
      val cMid0Front = pipe.addStage(
        name=pipeName + "_Mid0Front",
        optIncludeS2M=false,
      )
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
      ////val cModFront = pipe.addStage("ModFront")
      //pipe.first.up.driveFrom(io.front)(
      //  con=(node, payload) => {
      //    node(inpPipePayload) := payload 
      //  }
      //)
      ////when (cFront.up.isValid) {
      ////}

      ////--------
      //// This is equivalent to the following in `PipeMemTest`:
      ////  cSum.terminateWhen(
      ////    !cSum.up(rdValid)
      ////  )
      //val tempModFront = cloneOf(io.modFront)
      ////val modFrontTerminateCond = Bool()
      ////val modFrontTerminateMaybe = (
      ////  //tempModFront.clearValidWhen(modFrontTerminateCond)
      ////  //tempModFront.throwWhen(modFrontTerminateCond)
      ////  tempModFront
      ////)
      ////io.modFront << modFrontTerminateMaybe
      //io.modFront << tempModFront
      ////--------
      //pipe.last.down.driveTo(
      //  //io.modFront
      //  //modFrontTerminateMaybe
      //  tempModFront
      //)(
      //  con=(payload, node) => {
      //    payload := node(outpPipePayload)
      //  }
      //)
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
        cloneOf(front.myUpExtDel(0).memAddr)
      )
      val myWriteData = KeepAttribute(
        cloneOf(front.myUpExtDel(0).modMemWord)
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
        name=pipeName + "_Back"
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
  val cFront = mod.front.cFront
  //--------
  val cFrontArea = new cFront.Area {
    //--------
    val upExt = Vec.fill(2)(mkExt()).setName("cFrontArea_upExt")
    val upExtRealMemAddr = cloneOf(upExt(1).memAddr)
    //upExt(1) := upExt(0)
    val tempCond = KeepAttribute(Bool())
    upExt(1) := (
      RegNext(upExt(1)) init(upExt(1).getZero)
    )
    upExtRealMemAddr := (
      RegNext(upExtRealMemAddr) init(upExtRealMemAddr.getZero)
    )
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
    val backUpExt = mkExt().setName("cFrontArea_backUpExt")

    val tempUpMod = Vec.fill(2)(modType())
    tempUpMod(0).allowOverride
    tempUpMod(0) := up(mod.front.inpPipePayload)
    tempUpMod(0).getPipeMemRmwExt(
      outpExt=upExt(0),
      memArrIdx=memArrIdx,
    )
    //val tempBackUpMod = modType()
    //tempBackUpMod := mod.back.cBack.up(mod.back.pipePayload)
    //tempBackUpMod.getPipeMemRmwExt(
    //  outpExt=backUpExt,
    //  memArrIdx=memArrIdx,
    //)
    //--------
    //mod.front.pipe.last.up(mod.front.inpPipePayload).getPipeMemRmwExt(
    //  outpExt=lastUpExt,
    //  memArrIdx=memArrIdx,
    //)
    //val tempRdValid = (
    //  lastUpExt.memAddr =/= upExt(0).memAddr
    //  ////&& !lastUpExt.hazardId.msb
    //  ////&& upExt(1).hazardId.msb
    //  //|| !upExt(1).hazardId.msb
    //).setName("cFrontArea_tempRdValid")
    //--------
    val nextHazardId = (optEnableModDuplicate) generate (
      KeepAttribute(cloneOf(upExt(1).hazardId))
    )
    val rHazardId = (optEnableModDuplicate) generate (
      KeepAttribute(
        RegNext(nextHazardId) init(
          //S(nextHazardId.getWidth bits, default -> True)

          //-1
          upExt(1).getHazardIdIdleVal()
        )
      )
    )
    if (optEnableModDuplicate) {
      //nextHazardId := rHazardId
      //nextHazardId := modStageCnt - 1
      //nextHazardId := S(nextHazardId.getWidth bits, default -> True)
      //nextHazardId := -1
      //nextHazardId := upExt(1).getHazardIdIdleVal()
      nextHazardId := rHazardId
      //when (isValid) {
          upExt(1).hazardId := nextHazardId
      //} otherwise {
        //upExt(1).hazardId := rHazardId
      //}
    }
    //--------
    val hazardIdMinusOne = (optEnableModDuplicate) generate (
      rHazardId - 1
    )
    //for (idx <- 0 until rUpExtDel.size) {
    //  when (up.isFiring) {
    //    if (idx == 0) {
    //      rUpExtDel(idx) := upExt(1)
    //    } else {
    //      rUpExtDel(idx) := rUpExtDel(idx - 1)
    //    }
    //  }
    //}
    //--------
    //val nextDuplicateIt = (optEnableModDuplicate) generate (
    //  Bool()
    //)
    //val rDuplicateIt = (optEnableModDuplicate) generate (
    //  RegNext(nextDuplicateIt) init(False)
    //)
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
    val nextState = (optEnableModDuplicate) generate (
      KeepAttribute(State())
    )
    val rState = (optEnableModDuplicate) generate (
      KeepAttribute(RegNext(nextState) init(State.IDLE))
    )
    //val rPostDuplicateCnt = Reg(cloneOf(upExt(0).hazardId))
    def myHazardCmpFunc(
      curr: PipeMemRmwPayloadExt[WordT, HazardCmpT],
      prev: PipeMemRmwPayloadExt[WordT, HazardCmpT],
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
              isPostDelay,
            )
          )
          case None => (
            PipeMemRmwPayloadExt.defaultDoHazardCmpFunc(
              //upExt(0),
              //rUpExtDel(0),
              curr,
              prev,
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
    //val myUpExtDel = (optEnableModDuplicate) generate (
    //  mod.front.myUpExtDel
    //)
    val rPrevStateWhen = (optEnableModDuplicate) generate (
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
    val nextDidChangeState = (optEnableModDuplicate) generate (
      KeepAttribute(Bool())
    )
    val rDidChangeState = (optEnableModDuplicate) generate (
      KeepAttribute(RegNext(nextDidChangeState)) init(True)
    )
    //val nextHaltItIdleCnt = (optEnableModDuplicate) generate (
    //  KeepAttribute(
    //    //Bool()
    //    SInt(3 bits)
    //  )
    //)
    //val rHaltItIdleCnt = (optEnableModDuplicate) generate (
    //  KeepAttribute(
    //    RegNext(nextHaltItIdleCnt)
    //    init(
    //      //False
    //      -1
    //    )
    //  )
    //)
    val rDidDelayItIdle = Reg(Bool()) init(False)
    def findFirstFunc(
      prev: PipeMemRmwPayloadExt[
        WordT,
        HazardCmpT
      ],
      isPostDelay: Boolean,
      //idx: Int,
      //memAddr: UInt,
    ) = (
      //(upExt(1).memAddr === prev.memAddr)
      if (!isPostDelay) (
        (upExtRealMemAddr === prev.memAddr)
        ////&& prev.hazardId.msb
        ////&& (prev.hazardId === 0)
        ////&& upExt(1).hazardId.msb
        && (
          if (doPrevHazardCmpFunc) (
            myHazardCmpFunc(upExt(1), prev, isPostDelay)
          ) else (
            True
          )
        )
      ) else (
        //(upExtRealMemAddr === prev.memAddr)
        //|| 
        myHazardCmpFunc(upExt(1), prev, isPostDelay)
      )
    )
    val myUpExtDelPreBack = mod.front.myUpExtDelPreBack
    val myUpExtDelPreBackFindFirstVecNotPostDelay = (
      mod.front.myUpExtDelPreBackFindFirstVecNotPostDelay
    )
    val myUpExtDelPreBackFindFirstVecIsPostDelay = (
      mod.front.myUpExtDelPreBackFindFirstVecIsPostDelay
    )
    for (
      idx <- 0 until myUpExtDelPreBackFindFirstVecNotPostDelay.size
    ) {
      myUpExtDelPreBackFindFirstVecNotPostDelay(idx) := findFirstFunc(
        prev=myUpExtDelPreBack(idx),
        isPostDelay=false,
        //idx=idx,
      )
    }
    //for (
    //  idx <- 0 until myUpExtDelPreBackFindFirstVecNotPostDelay.size
    //) {
    //  myUpExtDelPreBackFindFirstVecIsPostDelay(idx) := findFirstFunc(
    //    prev=myUpExtDelPreBack(idx),
    //    isPostDelay=true,
    //    //idx=idx,
    //  )
    //}
    val tempMyUpExtDelFindFirstNotPostDelay = (
      (optEnableModDuplicate) generate (
        KeepAttribute(
          //myUpExtDelPreBack
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
          myUpExtDelPreBackFindFirstVecNotPostDelay.sFindFirst(_ === True)
          .setName("cFrontArea_tempMyUpExtDelFindFirstNotPostDelay")
        )
      )
    )
    //val tempMyUpExtDelFindFirstIsPostDelay = (
    //  (optEnableModDuplicate) generate (
    //    KeepAttribute(
    //      myUpExtDelPreBackFindFirstVecIsPostDelay.sFindFirst(_ === True)
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
    if (optEnableModDuplicate) {
      //when (myStopIt) {
      //  haltIt()
      //}
      //down.cancel := False
      //upExt(1).doCancelFront := False
      nextState := rState
      if (vivadoDebug) {
        //nextHazardId.addAttribute("MARK_DEBUG", "TRUE")
        //nextState.addAttribute("MARK_DEBUG", "TRUE")
        //rState.addAttribute("MARK_DEBUG", "TRUE")
        //rPrevStateWhen.addAttribute("MARK_DEBUG", "TRUE")
        //upExt.addAttribute("MARK_DEBUG", "TRUE")
        //upExtRealMemAddr.addAttribute("MARK_DEBUG", "TRUE")
        ////for (idx <- 0 until tempMyUpExtDelFindFirstNotPostDelay.size) {
        ////}
        //tempMyUpExtDelFindFirstNotPostDelay.addAttribute(
        //  "MARK_DEBUG", "TRUE"
        //)
        ////tempMyUpExtDelFindFirstIsPostDelay.addAttribute(
        ////  "MARK_DEBUG", "TRUE"
        ////)
      }
      //nextDidChangeState := rDidChangeState
      //nextHaltItIdleCnt := rHaltItIdleCnt
      switch (rState) {
        is (State.IDLE) {
          when (up.isValid) {
            //when (
            //  rPrevStateWhen === State.DELAY
            //  && !rDidDelayItIdle
            //) {
            //  //haltIt()
            //  //myStopIt := True
            //  haltIt()
            //  //down.cancel := True
            //  //upExt(1).doCancelFront := True
            //  //upExt
            //  rDidDelayItIdle := True
            //  //nextHazardId := (
            //  //  (
            //  //    S(s"${nextHazardId.getWidth}'d${myUpExtDel.size - 1}")
            //  //    - Cat(U"3'd0", tempMyUpExtDelFindFirst0._2).asSInt
            //  //  )
            //  //)
            //} otherwise {
              upExt(1) := upExt(0)
              upExt(1).hazardId := nextHazardId
              upExtRealMemAddr := upExt(0).memAddr
              when (
                //Mux[Bool](
                //  rPrevStateWhen === State.IDLE,
                  tempMyUpExtDelFindFirstNotPostDelay._1
                  && (
                    if (!doPrevHazardCmpFunc) (
                      myHazardCmpFunc(
                        curr=upExt(1),
                        prev=mod.front.myUpExtDel(0),
                        isPostDelay=false,
                      )
                    ) else ( // if (doPrevHazardCmpFunc)
                      True
                    )
                  )
                //  tempMyUpExtDelFindFirstIsPostDelay._1,
                //  //findFirstFunc(
                //  //  //myUpExtDel(myUpExtDel.size - 2)
                //  //  myUpExtDel(0)
                //  //)
                //  //True
                //  //tempMyUpExtDelFindFirst2._1,
                //  //True,
                //  //findFirstFunc(
                //  //  //myUpExtDel(myUpExtDel.size - 1)
                //  //)
                //  //(
                //  //  //mod.back.dbgDoWrite
                //  //  mod.back.myWriteEnable
                //  //  && 
                //  //  //!mod.back.dbgDoClear
                //  //  !io.clear.fire
                //  //  //&& (
                //  //  //  upExtRealMemAddr
                //  //  //  === mod.back.myWriteAddr 
                //  //  //)
                //  //  && findFirstFunc(
                //  //    prev=myUpExtDel(myUpExtDel.size - 1)
                //  //  )
                //  //)
                //)
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

                    S(
                      s"${nextHazardId.getWidth}"
                      + s"'d${myUpExtDelPreBack.size - 1}"
                    )
                    - Cat(
                      U"3'd0", 
                      //Mux[UInt](
                      //  rPrevStateWhen === State.IDLE,
                        tempMyUpExtDelFindFirstNotPostDelay._2,
                      //  tempMyUpExtDelFindFirstIsPostDelay._2,
                      //)
                    ).asSInt
                  )
                )
                //nextHaltItIdleCnt := (
                //  //1
                //  0
                //)
              }
            //}
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
            //myStopIt := False
            //myStopIt := True
            //nextDidChangeState := True

            //nextHaltItIdleCnt := (
            //  //True
            //  
            //)
            //haltIt()
            //duplicateIt()
            //terminateIt()
          } otherwise {
            duplicateIt()
            ////upExt(1).memAddr := (
            ////  upExt(0).memAddr + upExtRealMemAddr + 1
            ////)
            ////myStopIt := True
            ////haltIt()
            ////terminateIt()
          }
        }
      }
    } else { // if (!optEnableModDuplicate)
      upExt(1) := upExt(0)
      //upExt(1).hazardId := nextHazardId
      upExtRealMemAddr := upExt(0).memAddr
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
    val myRdMemWord = mod.front.myRdMemWord
    //if (vivadoDebug) {
    //  myRdMemWord.addAttribute("MARK_DEBUG", "TRUE")
    //}
    //val rRdMemWord1 = mod.front.rRdMemWord1
    val dbgRdMemWord = (debug) generate (
      mod.front.dbgRdMemWord
    )
    //myRdMemWord := RegNext(myRdMemWord) init(myRdMemWord.getZero)
    val myDownExt = mkExt().setName("cFrontArea_myDownExt")
    //down(mod.front.midPipePayload).getPipeMemRmwExt(
    //  outpExt=myDownExt,
    //  memArrIdx=memArrIdx,
    //)
    //myRdMemWord := (
    //  RegNext(myRdMemWord) init(myRdMemWord.getZero)
    //)

    //val myDbgRdMemWord = (debug) generate {
    //  val temp = Reg(
    //    wordType()
    //  )
    //  temp.init(temp.getZero)
    //  temp
    //}
    //if (debug) {
    //  up(dbgRdMemWord) := myDbgRdMemWord
    //  //myDbgRdMemWord := (
    //  //  RegNext(myDbgRdMemWord) init(myDbgRdMemWord.getZero)
    //  //)
    //}
    val rSetRdId = Reg(Bool()) init(False)
    //val rRdId = Reg(cloneOf(upExt(1).rdId)) init(0x0)
    //val rPrevRdId = RegNext(upExt(1).rdId) init(0x0)
    //upExt(1).rdId := rPrevRdId
    //upExt(1).rdId := rRdId
    //--------
    //when (
    //  ////mod.front.cLastFront.up.isReady
    //  ////mod.front.cLastFront.
    //  //up.isValid
    //  ////down.isFiring
    //  //True
    //  //up.isValid
    //  //down.isValid
    //  up.isFiring
    //  && upExt(1).hazardId.msb
    //  //up.isValid
    //  //upExt(1).hazardId.msb
    //  //&& down.isFiring
    //) {
    //  //upExt(1).rdId := rPrevRdId + 1
    //  rRdId := rRdId + 1
    //  myRdMemWord := modMem.readSync(
    //    //address=RegNextWhen(
    //    //  upExt(1).memAddr, up.isFiring
    //    //) init(0x0)
    //    //address=RegNextWhen(
    //    //  upExt(1).memAddr, down.isFiring
    //    //) init(0x0)
    //    address=upExt(1).memAddr,
    //    //address=myDownExt.memAddr,
    //  )
    //}
    tempCond := (
      //up.isValid
      //&& !rSetRdId
      //up.isFiring
      //up.isValid
      //&&
      //--------
      //!rSetRdId
      //&& 
      if (optEnableModDuplicate) (
        //up.isFiring
        //down.isReady
        down.isFiring
        //True
        && 
        //upExt(1).hazardId.msb
        (
          //nextState === State.IDLE
          //|| 
          //rState === State.IDLE
          //&& !rHaltItIdle
          //!myStopIt
          True
        )
        //&& !nextDidChangeState
        //&& rDidChangeState
        //nextState =/= State.DELAY
        //Mux[Bool](
        //  nextState === State.IDLE,
        //  //up.isFiring,
        //  //down.isFiring,
        //)
        //tempMyUpExtDelFindFirst1._1
        //&& myHazardCmpFunc(
        //  curr=upExt(1),
        //  prev=myUpExtDel(0),
        //)
        //|| myUpExtDel(myUpExtDel)
      ) else (
        //True
        //up.isFiring
        //down.isReady
        down.isFiring
        //True
      )
    )

    if (optEnableModDuplicate) {
      myRdMemWord := modMem.readSync(
        //address=RegNextWhen(
        //  upExt(1).memAddr, up.isFiring
        //) init(0x0)
        //address=RegNextWhen(
        //  upExt(1).memAddr, down.isFiring
        //) init(0x0)
        address=(
          //upExt(1).memAddr
          upExtRealMemAddr
        ),
        //address=myDownExt.memAddr,
        enable=(
          //up.isFiring
          //&&
          tempCond
          //up.isFiring && tempCond
          //down.isFiring && tempCond
          //up.isFiring && tempCond
          //down.isFiring && tempCond
        ),
      )
    }
    //when (
    //  up.isValid
    //  && tempCond
    //) {
    //  rSetRdId := True
    //  //rRdId := rRdId + 1
    //  //if (debug) {
    //  //  myDbgRdMemWord := /*RegNext*/(
    //  //    modMem.readAsync(
    //  //      address=upExt(1).memAddr,
    //  //    )
    //  //  )
    //  //}
    //}
    val tempCondDown = (
      down.isFiring
    )
    //if (debug) {
    //  myDbgRdMemWord := RegNext(
    //    modMem.readAsync(
    //      address=upExt(1).memAddr,
    //      //enable=tempCondDown,
    //    )
    //  )
    //}
    //when (
    //  //down.isFiring
    //  //up.isFiring
    //  tempCondDown
    //) {
    //  rSetRdId := False
    //  //if (debug) {
    //  //  myDbgRdMemWord := RegNext(
    //  //    modMem.readAsync(
    //  //      address=upExt(1).memAddr,
    //  //    )
    //  //  )
    //  //}
    //  //if (debug) {
    //  //  myDbgRdMemWord := /*RegNext*/(
    //  //    modMem.readAsync(
    //  //      address=upExt(1).memAddr,
    //  //    )
    //  //  )
    //  //}
    //}
    //when (
    //  up.isFiring
    //  && tempCond
    //) {
    //  if (debug) {
    //    myDbgRdMemWord := /*RegNext*/(
    //      modMem.readAsync(
    //        address=upExt(1).memAddr,
    //      )
    //    )
    //  }
    //}
    //--------
    //upExt(1).rdMemWord := modMem.readAsync(
    //  address=upExt(1).memAddr
    //)
    //val cLastFront = mod.front.cLastFront
    //val myRdMemWord = mod.front.myRdMemWord
    //myRdMemWord := RegNext(myRdMemWord) init(myRdMemWord.getZero)
    //when (
    //  //mod.front.cLastFront.up.isReady
    //  //mod.front.cLastFront.
    //  cLastFront.up.isValid
    //  //down.isValid
    //) {
    //  myRdMemWord := modMem.readSync(
    //    //address=RegNextWhen(
    //    //  upExt(1).memAddr, up.isFiring
    //    //) init(0x0)
    //    address=upExt(1).memAddr
    //  )
    //}
    //mod.front.myRdMemWord := modMem.readAsync(
    //  address=upExt(1).memAddr
    //)
    //upExt(2).rdMemWord := modMem.readSync(
    //  address=upExt(1).mem
    //)
    //--------
    //val rUpExtDel = Vec.fill(modStageCnt + 1)(
    //  Reg(cloneOf(upExt(1))) init(upExt(1).getZero)
    //)
    //for (idx <- 0 until rUpExtDel.size) {
    //  when (up.isFiring) {
    //    if (idx == 0) {
    //      rUpExtDel(idx) := upExt(1)
    //    } else {
    //      rUpExtDel(idx) := rUpExtDel(idx - 1)
    //    }
    //  }
    //}
    //def wantNonFmaxFwd(
    //  //someUpMemAddr: UInt//=upExt(1).memAddr
    //  //someNode: NodeApi,
    //  someExt: PipeMemRmwPayloadExt[WordT]
    //): Bool = (
    //  if (
    //    !forFmax
    //    && 
    //    optEnableModDuplicate
    //  ) (
    //    //tempCond
    //    //someExt.hazardId.msb
    //    ////backUpExt.hazardId === 0
    //    //&&
    //    //&& 
    //    backUpExt.hazardId.msb
    //    //&& mod.back.cBack.up.isFiring
    //    && mod.back.cBack.up.isValid
    //    //&& someNode.isValid
    //    //&& upExt(1).memAddr === backUpExt.memAddr
    //    //&& someUpMemAddr === backUpExt.memAddr
    //    && someExt.memAddr === backUpExt.memAddr
    //    //&& someExt.memAddr === rUpExtDel(rUpExtDel.size - 1).memAddr
    //    //&& someExt.fwdId === backUpExt.fwdId
    //  ) else (
    //    False
    //  )
    //)
    //if (debug) {
    //  upExt(1).dbgWantNonFmaxFwd := wantNonFmaxFwd(
    //    someExt=upExt(1)
    //  )
    //}
    //--------
    //def getNonFmaxFwd() = (
    //  backUpExt.modMemWord
    //)
    //def getNonFmaxFwdOutp(
    //  someExt: PipeMemRmwPayloadExt[WordT]
    //) = {
    //  //someExt.rdMemWord
    //  myRdMemWord
    //}
    //def perfNonFmaxFwd(
    //  someExt: PipeMemRmwPayloadExt[WordT]
    //): Unit = {
    //  getNonFmaxFwdOutp(someExt=someExt) := getNonFmaxFwd()
    //}
    ////val rDbgNonMaxFwdCnt = (
    ////  Reg(cloneOf(upExt(1).dbgNonFmaxFwdCnt)) //init(0x0)
    ////)
    ////for (idx <- 0 until rDbgNonMaxFwdCnt.size) {
    ////  rDbgNonMaxFwdCnt(idx).init(0x0)
    ////}
    ////upExt(1).dbgNonFmaxFwdCnt := (
    ////  //(rDbgNonMaxFwdCnt + 1)
    ////  rDbgNonMaxFwdCnt
    ////)
    //when (
    //  !clockDomain.isResetActive
    //  //&& up.isFiring
    //  && up.isValid
    //  && (
    //    if (optEnableModDuplicate) {
    //      upExt(1).hazardId.msb
    //    } else {
    //      True
    //    }
    //  )
    //) {
    //  //if (debug) {
    //  //  upExt(1).dbgWantNonFmaxFwd := wantNonFmaxFwd(
    //  //    //someNode=up,
    //  //    someExt=upExt(1),
    //  //  )
    //  //  //upExt(1).dbgNonMaxFwdCnt 
    //  //  when (
    //  //    //cArr(0).down.isFiring
    //  //    down.isFiring
    //  //  ) {
    //  //    def tempCnt = rDbgNonMaxFwdCnt(upExt(1).memAddr)
    //  //    tempCnt := tempCnt + 1
    //  //    //rDbgNonMaxFwdCnt := rDbgNonMaxFwdCnt + 1
    //  //  }
    //  //}
    //  //if (!forFmax) {
    //  //  when (wantNonFmaxFwd(
    //  //    //someNode=up,
    //  //    someExt=upExt(1),
    //  //  )) {
    //  //    //upExt(1).rdMemWord := getNonFmaxForward()
    //  //    perfNonFmaxFwd(someExt=upExt(1))
    //  //  } otherwise {
    //  //    //upExt(1).rdMemWord := modMem.readSync(
    //  //    //  address=upExt(1).memAddr
    //  //    //)
    //  //  }
    //  //} else { // if (forFmax)
    //  //  ////when (
    //  //  ////  !nextHazardId
    //  //  ////)
    //  //  ////when (nextDuplicateIt) {
    //  //  ////  upExt(1).rdMemWord := upExt(1).rdMemWord.getZero
    //  //  ////} otherwise {
    //  //  //  //--------
    //  //  //  upExt(1).rdMemWord := modMem.readSync(
    //  //  //    address=upExt(1).memAddr,
    //  //  //  )
    //  //  //  //--------
    //  //  ////}
    //  //}
    //}
    //--------
    tempUpMod(1) := (
      RegNext(tempUpMod(1)) init(tempUpMod(1).getZero)
    )
    //when (
    //  //if (optEnableModDuplicate) (
    //  //  //nextState === State.IDLE
    //  //  !down.cancel
    //  //) else (
    //  //  True
    //  //)
    //  //!myStopIt
    //  tempCond
    //) {
      tempUpMod(1) := tempUpMod(0)
    //}
    tempUpMod(1).allowOverride
    tempUpMod(1).setPipeMemRmwExt(
      inpExt=upExt(1),
      memArrIdx=memArrIdx,
    )
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
    GenerationFlags.formal {
      //val haveStableMemAddr = Bool()
      //when (!up.isValid) {
      //  haveStableMemAddr := 
      //}
      //when (
      //  up.isValid
      //  && !up.isFiring
      //) {
      //  assume(stable(upExt(0).memAddr))
      //}
      when (
        //up.isFiring
        (
          RegNextWhen(True, io.back.isFiring) init(False)
        ) && (
          RegNext(tempCond) init(False)
        )
      ) {
        assert(
          mod.front.myRdMemWord
          === (
            RegNext(
              modMem.readAsync(
                address=/*RegNext*/(upExtRealMemAddr) //init(0x0)
              )
            ) init(mod.front.myRdMemWord.getZero)
          )
        )
      }
      val myDownFireHazardId = (
        RegNextWhen(upExt(1).hazardId, down.isFiring)
        init(upExt(1).getHazardIdIdleVal())
      )
      switch (rState) {
        is (State.IDLE) {
          switch (rPrevStateWhen) {
            is (State.IDLE) {
              //assert()
              //when (RegNext(down.isFiring) init(False)) {
              //  //assert(myDownFireHazardId)
              //}
            }
            is (State.DELAY) {
              assert(myDownFireHazardId.msb)
            } 
          }
        }
        is (State.DELAY) {
        }
      }
      when (
        RegNextWhen(mod.back.myWriteEnable, io.back.isFiring)
        init(False)
      ) {
        when (
          //RegNextWhen(
          (
            RegNextWhen(mod.back.myWriteAddr, io.back.isFiring) init(0x0)
          ) === (
            RegNext(upExtRealMemAddr) init(0x0)
          )
        ) {
          when (RegNext(tempCond) init(False)) {
            cover(
              (
                RegNextWhen(
                  RegNextWhen(
                    RegNextWhen(
                      RegNextWhen(
                        RegNextWhen(True, io.back.isFiring) init(False),
                        io.back.isFiring
                      ) init(False),
                      io.back.isFiring
                    ) init(False),
                    io.back.isFiring
                  ) init(False),
                  io.back.isFiring
                ) init(False)
              ) && (
                mod.front.myRdMemWord
                === RegNextWhen(mod.back.myWriteData, io.back.isFiring)
              )
            )
          }
        }
      }
      //cover(
      //  //(
      //  //  //RegNextWhen(
      //  //  //  RegNextWhen(
      //  //  //    RegNextWhen(
      //  //  //      RegNextWhen(
      //  //  //        RegNextWhen(True, io.front.isFiring) init(False),
      //  //  //        io.front.isFiring
      //  //  //      ) init(False),
      //  //  //      io.front.isFiring
      //  //  //    ) init(False),
      //  //  //    io.front.isFiring
      //  //  //  ) init(False),
      //  //  //  io.front.isFiring
      //  //  //) init(False)
      //  //  RegNextWhen(True, io.front.isFiring) init(False)
      //  //) && (
      //  //  RegNextWhen(True, io.modFront.isFiring) init(False)
      //  //) && (
      //  //  RegNextWhen(True, io.modBack.isFiring) init(False)
      //  //) && 
      //  (
      //    //RegNextWhen(
      //    //  RegNextWhen(
      //    //    RegNextWhen(
      //    //      RegNextWhen(
      //    //        RegNextWhen(True, io.back.isFiring) init(False),
      //    //        io.back.isFiring
      //    //      ) init(False),
      //    //      io.back.isFiring
      //    //    ) init(False),
      //    //    io.back.isFiring
      //    //  ) init(False),
      //    //  io.back.isFiring
      //    //) init(False)
      //    //RegNextWhen(True, io.back.isFiring) init(False)
      //    True
      //  ) && (
      //    /*RegNextWhen*//*RegNext(RegNext*/(mod.back.myWriteEnable/*,
      //    io.back.isFiring*/)/*)*/ //init(False)
      //  ) && (
      //    //RegNext(up.isFiring)
      //    True
      //  ) && (
      //    (
      //      //RegNext(upExt(0).memAddr)
      //      /*RegNext(RegNext*/(upExtRealMemAddr)/*)*/
      //    ) === (
      //      /*RegNextWhen*//*RegNext(RegNext*/(mod.back.myWriteAddr/*,
      //      io.back.isFiring*/)/*)*/ //init(0x0)
      //    )
      //  ) && (
      //    //RegNext(upExt(0).memAddr) > 0
      //    True
      //  ) 
      //  //&& (
      //  //  io.back.isFiring
      //  //) && (
      //  //  RegNext(io.back.isFiring) init(False)
      //  //) && (
      //  //  RegNext(RegNext(io.back.isFiring))
      //  //) 
      //  && ({
      //    (
      //      mod.front.myRdMemWord
      //      === /*RegNext(RegNext*/(mod.back.myWriteData)/*)*/
      //    )
      //  }) && (
      //    //mod.front.myRdMemWord.asBits.asUInt > 0
      //    True
      //  ) && (
      //    //RegNextWhen(
      //    //  mod.front.myRdMemWord.asBits.asUInt,
      //    //  mod.front.cMid0Front.up.isFiring
      //    //) > 0
      //    True
      //  ) && (
      //    //mod.front.myRdMemWord
      //    //=/= RegNextWhen(
      //    //  mod.front.myRdMemWord,
      //    //  mod.front.cMid0Front.up.isFiring
      //    //)
      //    True
      //  )
      //)
    }
    //GenerationFlags.formal {
    //  //--------
    //  //when (up.isFiring) {
    //  //  for (idx <- 0 until rMyUpRdValidDelVec.size) {
    //  //    def tempUpRdValid = rMyUpRdValidDelVec(idx)
    //  //    if (idx == 0) {
    //  //      tempUpRdValid := nextUpRdValid
    //  //    } else {
    //  //      tempUpRdValid := rMyUpRdValidDelVec(idx - 1)
    //  //    }
    //  //  }
    //  //}
    //  //--------
    //  //val myDbgMemReadSync = wordType()
    //  //myDbgMemReadSync := (
    //  //  modMem.readSync
    //  //  //mem.readAsync
    //  //  (
    //  //    //address=up(pipePayload.front).addr,
    //  //    address=upExt(1).memAddr,
    //  //    enable=(
    //  //      up.isValid
    //  //      && !rSetRdId
    //  //      && upExt(1).hazardId.msb
    //  //    )
    //  //  )
    //  //)
    //  //when (
    //  //  up.isValid
    //  //) {
    //  //} otherwise {
    //  //  myDbgMemReadSync := (
    //  //    RegNext(myDbgMemReadSync) init(myDbgMemReadSync.getZero)
    //  //  )
    //  //}
    //  //up(pipePayload.dbgMemReadSync) := myDbgMemReadSync
    //  //upExt(1).dbgMemReadSync := myDbgRdMemWord //myDbgMemReadSync
    //  //--------
    //  //when (up.isFiring) {
    //  //  rIsFiringCnt := rIsFiringCnt + 1
    //  //}
    //  when (pastValidAfterReset) {
    //    ////when (
    //    ////  past(up.isFiring)
    //    ////) {
    //    ////  when (
    //    ////    past(cBack.up(pipePayload.front).addr)
    //    ////    === cSum.up(pipePayload.front).addr
    //    ////  ) {
    //    ////    assert(
    //    ////      past(cSum.up(pipePayload.rd))
    //    ////      === past(cBack.up(pipePayload.rd))
    //    ////    )
    //    ////  } otherwise {
    //    ////    assert(
    //    ////      cSum.up(pipePayload.rd) === past(
    //    ////        mem.readAsync(
    //    ////          address=down(pipePayload.front).addr
    //    ////        )
    //    ////      )
    //    ////    )
    //    ////  }
    //    ////}
    //    ////when (past(cSum.up.isFiring)) {
    //    ////  when (
    //    ////    cBack.up
    //    ////  ) {
    //    ////  } otherwise {
    //    ////  }
    //    ////}
    //    //val rPrevCSumFront = Reg(pipePayload.mkFront())
    //    //rPrevCSumFront.init(rPrevCSumFront.getZero)
    //    //val rPrevCBackFront = Reg(pipePayload.mkFront())
    //    //rPrevCBackFront.init(rPrevCBackFront.getZero)
    //    ////when (cSum.up.isFiring) {
    //    ////}
    //    //--------
    //    // BEGIN: add this back later
    //    //when (
    //    //  RegNextWhen(True, io.back.fire) init(False)
    //    //) {
    //    //  when (
    //    //    //up.isFiring
    //    //    up.isValid
    //    //    && !rSetRdId
    //    //    && (
    //    //      if (optEnableModDuplicate) {
    //    //        upExt(1).hazardId.msb
    //    //      } else {
    //    //        True
    //    //      }
    //    //    )
    //    //  ) {
    //    //    //if (!forFmax) {
    //    //    //  when (
    //    //    //    //backUpExt.hazardId.msb
    //    //    //    ////&& mod.back.cBack.up.isFiring
    //    //    //    //&& mod.back.cBack.up.isValid
    //    //    //    //&& upExt(1).memAddr === backUpExt.memAddr
    //    //    //    wantNonFmaxFwd(
    //    //    //      //someNode=up,
    //    //    //      someExt=upExt(1),
    //    //    //    )
    //    //    //  ) {
    //    //    //    assert(
    //    //    //      //upExt(1).rdMemWord === getNonFmaxForward() //backUpExt.modMemWord
    //    //    //      getNonFmaxFwdOutp(someExt=upExt(1))
    //    //    //      === getNonFmaxFwd()
    //    //    //    )
    //    //    //  } otherwise {
    //    //    //    assert(
    //    //    //      //upExt(1).rdMemWord === modMem.readSync(
    //    //    //      //  address=upExt(1).memAddr
    //    //    //      //)
    //    //    //      //myRdMemWord === modMem.readSync(
    //    //    //      //  address=upExt(1).memAddr
    //    //    //      //)
    //    //    //      //myRdMemWord === myDbgMemReadSync
    //    //    //      myRdMemWord === myDbgRdMemWord
    //    //    //    )
    //    //    //  }
    //    //    //} else { // if (forFmax)
    //    //      assert(
    //    //        //upExt(1).rdMemWord === modMem.readSync(
    //    //        //  address=upExt(1).memAddr,
    //    //        //)
    //    //        //myRdMemWord === modMem.readSync(
    //    //        //  address=upExt(1).memAddr
    //    //        //)
    //    //        //myRdMemWord === myDbgMemReadSync
    //    //        myRdMemWord === myDbgRdMemWord
    //    //      )
    //    //    //}
    //    //  } otherwise {
    //    //    //assert(
    //    //    //  /*past*/(upExt(1).rdMemWord)
    //    //    //  === /*past*/(RegNext(upExt(1).rdMemWord))
    //    //    //)
    //    //    assert(
    //    //      //upExt(1).rdMemWord === modMem.readSync(
    //    //      //  address=upExt(1).memAddr,
    //    //      //)
    //    //      //myRdMemWord === modMem.readSync(
    //    //      //  address=upExt(1).memAddr
    //    //      //)
    //    //      //myRdMemWord === myDbgMemReadSync
    //    //      myRdMemWord === myDbgRdMemWord
    //    //    )
    //    //  }
    //    //}
    //    // END: add this back later
    //    //--------

    //    //def myCoverFunc(
    //    //  //cond: Boolean,
    //    //  kind: Int,
    //    //): Bool = {
    //    //  val rSameAddrCnt = Reg(UInt(8 bits)) init(0x0)
    //    //  val rDiffAddrCnt = Reg(UInt(8 bits)) init(0x0)
    //    //  //val rSomeDuplicateItCnt = Reg(UInt(8 bits)) init(0x0)
    //    //  //val rUpNotFiringCnt = (cond) generate (
    //    //  //  Reg(UInt(8 bits)) init(0x0)
    //    //  //)
    //    //  val myModMemWordCond = backUpExt.modMemWord.asBits.asUInt > 0 
    //    //  when (
    //    //    myModMemWordCond
    //    //    //&& up.isValid
    //    //    ////&& mod.back.cBack.up.isValid
    //    //  ) {
    //    //    when (
    //    //      up.isFiring
    //    //      //up.isValid
    //    //      //down.isFiring
    //    //    ) {
    //    //      when (
    //    //        ////upExt.memAddr === backUpExt.memAddr
    //    //        ////&& 
    //    //        ////upExt(1).memAddr === rUpMemAddrDel(0)
    //    //        ////&& upExt.memAddr === rUpMemAddrDel2
    //    //        //upExt(1).memAddr
    //    //        ////=== (
    //    //        ////  RegNextWhen(upExt(1).memAddr, up.isFiring) init(0x0)
    //    //        ////)
    //    //        //=== rUpMemAddrDel(0)
    //    //        myHazardCmpFunc(
    //    //          upExt(1),
    //    //          rUpExtDel(0),
    //    //        )
    //    //      ) {
    //    //        kind match {
    //    //          case 0 => {
    //    //            when (past(up.isFiring)) {
    //    //              when (
    //    //                rSameAddrCnt(0)
    //    //              ) {
    //    //                rSameAddrCnt := rSameAddrCnt + 1
    //    //              }
    //    //            } otherwise {
    //    //              rSameAddrCnt := rSameAddrCnt + 1
    //    //            }
    //    //            //when (
    //    //            //  past(up.isFiring)
    //    //            //  && !past(past(up.isFiring))
    //    //            //) {
    //    //            //  rUpNotFiringCnt := rUpNotFiringCnt + 1
    //    //            //}
    //    //            //rSameAddrCnt := rSameAddrCnt + 1
    //    //          }
    //    //          case 1 | 2 | 3 => {
    //    //            rSameAddrCnt := rSameAddrCnt + 1
    //    //          }
    //    //          //case 3 => {
    //    //          //  when (
    //    //          //    rUpMemAddrDel(0) =/= rUpMemAddrDel(1)
    //    //          //  ) {
    //    //          //    rSameAddrCnt := rSameAddrCnt + 1
    //    //          //  }
    //    //          //}
    //    //          case _ => {
    //    //          }
    //    //        }
    //    //      }
    //    //      //otherwise 
    //    //      when (
    //    //        //upExt(1).memAddr =/= rUpMemAddrDel(0)
    //    //        !myHazardCmpFunc(
    //    //          upExt(1),
    //    //          rUpExtDel(0),
    //    //        ) && (
    //    //          kind match {
    //    //            case 0 => (
    //    //              True
    //    //            )
    //    //            //&& upExt(1).memAddr
    //    //            case 1 => (
    //    //              True
    //    //            )
    //    //            case 2 => (
    //    //              //rUpMemAddrDel(0) =/= rUpMemAddrDel(1)
    //    //              ////&& rUpMemAddrDel(1) =/= rUpMemAddrDel(2)
    //    //              !myHazardCmpFunc(
    //    //                rUpExtDel(0),
    //    //                rUpExtDel(1),
    //    //              )
    //    //            )
    //    //            case 3 => (
    //    //              //rUpMemAddrDel(0) =/= rUpMemAddrDel(1)
    //    //              //&& rUpMemAddrDel(1) =/= rUpMemAddrDel(2)
    //    //              (
    //    //                !myHazardCmpFunc(
    //    //                  rUpExtDel(0),
    //    //                  rUpExtDel(1),
    //    //                )
    //    //              ) && (
    //    //                !myHazardCmpFunc(
    //    //                  rUpExtDel(1),
    //    //                  rUpExtDel(2),
    //    //                )
    //    //              )
    //    //            )
    //    //            case _ => (
    //    //              True
    //    //            )
    //    //          }
    //    //        )
    //    //      ) {
    //    //        rDiffAddrCnt := rDiffAddrCnt + 1
    //    //      }
    //    //    }
    //    //  }
    //    //  (
    //    //    //(
    //    //    //  RegNextWhen(
    //    //    //    True,
    //    //    //  ) init(False)
    //    //    //) && (
    //    //    //  RegNextWhen(
    //    //    //    True,
    //    //    //    (
    //    //    //      upExt.memAddr =/= backUpExt.memAddr
    //    //    //      && backUpExt.modMemWord.asBits.asUInt > 0 
    //    //    //      && up.isFiring
    //    //    //      && mod.back.cBack.up.isFiring
    //    //    //    )
    //    //    //  ) init(False)
    //    //    //) 
    //    //    (
    //    //      rSameAddrCnt > 8
    //    //      //True
    //    //    ) && (
    //    //      rDiffAddrCnt > 8
    //    //      //True
    //    //    ) && (
    //    //      RegNextWhen(True, io.front.fire) init(False)
    //    //    ) && (
    //    //      RegNextWhen(True, io.modFront.fire) init(False)
    //    //    ) && (
    //    //      RegNextWhen(True, io.modBack.fire) init(False)
    //    //    ) && (
    //    //      RegNextWhen(True, io.back.fire) init(False)
    //    //    ) && (
    //    //      kind match {
    //    //        case 0 | 1 => (
    //    //          //rSomeDuplicateItCnt > 4
    //    //          True
    //    //        )
    //    //        case 2 => (
    //    //          True
    //    //        )
    //    //        case 3 => (
    //    //          True
    //    //        )
    //    //        case _ => (
    //    //          True
    //    //        )
    //    //      }
    //    //    )
    //    //  )
    //    //}
    //    ////cover(myCoverFunc(kind=0))
    //    ////cover(myCoverFunc(kind=1))

    //    ////cover(myCoverFunc(kind=2))
    //    //cover(myCoverFunc(kind=3))
    //    ////cover(io.back.fire)
    //  }
    //}
    //--------
  }
  val cMid0Front = mod.front.cMid0Front
  val cMid0FrontArea = new cMid0Front.Area {
    //--------
    val upExt = Vec.fill(2)(mkExt()).setName("cMid0FrontArea_upExt")
    //mod.front.myUpExtDel(0) := upExt(1)
    myUpExtDel(0) := (
      RegNext(myUpExtDel(0)) init(myUpExtDel(0).getZero)
    )
    upExt(1) := (
      RegNext(upExt(1)) init(upExt(1).getZero)
    )
    when (
      up.isValid
      //&& upExt(0).hazardId.msb
    ) {
      myUpExtDel(0) := upExt(1)
      upExt(1) := upExt(0)
    }
    upExt(1).rdMemWord.allowOverride
    val tempUpMod = (
      Vec.fill(2)(modType())
      .setName("cMid0FrontArea_tempUpMod")
    )
    tempUpMod(0) := up(mod.front.midPipePayload)
    tempUpMod(0).getPipeMemRmwExt(
      outpExt=upExt(0),
      memArrIdx=memArrIdx,
    )
    //val cMid0Front = mod.front.cMid0Front
    val myRdMemWord = mod.front.myRdMemWord
    //when (up.isValid) {
    //}
    //upExt(1).rdMemWord := (
    //  RegNext(upExt(1).rdMemWord) init(upExt(1).rdMemWord.getZero)
    //)

    //val myCancelIt = (optEnableModDuplicate) generate (
    //  KeepAttribute(Bool())
    //)
    //if (optEnableModDuplicate) {
    //  myCancelIt := False
    //  when (!upExt(0).hazardId.msb) {
    //    myCancelIt := True
    //    //throwIt()
    //  }
    //  up.cancel := myCancelIt
    //}

    //upExt(1).rdId.allowOverride
    //val rPrevRdId = RegNext(upExt(1).rdId) init(0x0)
    //upExt(1).rdId := rPrevRdId
    //val rRdId = Reg(cloneOf(upExt(0).rdId)) init(upExt(0).rdId.getZero)
    val rSetRdId = Reg(Bool()) init(False)
    //upExt(1).rdMemWord := myRdMemWord
    //val rSavedRdMemWord = (
    //  Reg(cloneOf(myRdMemWord)) init(myRdMemWord.getZero)
    //)
    //--------
    when (
      up.isValid
      //&& upExt(0).hazardId.msb
      && !rSetRdId
      && (
        //if (optEnableModDuplicate) {
        //  upExt(1).hazardId.msb
        //} else {
          True
        //}
      )
    ) {
      //myRdMemWord 
      rSetRdId := True
      upExt(1).rdMemWord := myRdMemWord
      //rSavedRdMemWord := myRdMemWord
    }
    when (
      //down.isFiring
      up.isFiring
    ) {
      rSetRdId := False
    }
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
      inpExt=upExt(1),
      memArrIdx=memArrIdx
    )
    up(mod.front.outpPipePayload) := tempUpMod(1)
    //val rDbgSeenFrontUpFiring = (debug) generate (
    //  RegNextWhen(True, cFront.up.isFiring) init(False)
    //)
    //val rDbgPrevMemAddr = (debug) generate (
    //  RegNextWhen(
    //    cFrontArea.upExt(1).memAddr, cFront.up.isFiring
    //  ) init(0x0)
    //)
    val tempModMemReadAsync = (debug) generate (
      //RegNextWhen(
      //  modMem.readAsync(
      //    address=upExt(1).memAddr,
      //  ),
      //  //cFront.down.isFiring,
      //  up.isFiring
      //) init(myRdMemWord.getZero)
      Reg(cloneOf(myRdMemWord)) init(myRdMemWord.getZero)
      //cloneOf(myRdMemWord)
    )
    //val rPrevTempModMemReadAsync = (debug) generate (
    //  RegNext(tempModMemReadAsync) init(tempModMemReadAsync.getZero)
    //)
    //--------
    //GenerationFlags.formal {
    //  //tempModMemReadAsync := rPrevTempModMemReadAsync
    //  //tempModMemReadAsync := up(mod.front.dbgRdMemWord)
    //  when (pastValidAfterReset) {
    //    //val tempCond = (
    //    //  past(cFront.up.isFiring)
    //    //  && !past(cFrontArea.rSetRdId)
    //    //  && past(cFrontArea.upExt(1).hazardId.msb)
    //    //)
    //    //def tempCond(
    //    //  someExt: PipeMemRmwPayloadExt[WordT, HazardCmpT]
    //    //) = (
    //    //  //if (!forFmax) {
    //    //  //  //!(
    //    //  //  //  RegNextWhen(
    //    //  //  //    cFrontArea.wantNonFmaxFwd(
    //    //  //  //      someExt=cFrontArea.upExt(1)
    //    //  //  //    ),
    //    //  //  //    cFront.up.isFiring
    //    //  //  //  ) init(False)
    //    //  //  //)
    //    //  //  //!upExt(1).dbgWantNonFmaxFwd
    //    //  //  !someExt.dbgWantNonFmaxFwd
    //    //  //} else {
    //    //    True
    //    //  //}
    //    //)
    //    ////when (
    //    ////  (cFront.up.isFiring)
    //    ////  && !(cFrontArea.rSetRdId)
    //    ////  && (cFrontArea.upExt(1).hazardId.msb)
    //    ////  //&& !cFrontArea.upExt(1).dbgWantNonFmaxFwd
    //    ////  ////&& cFrontArea.tempCond
    //    ////  && tempCond(someExt=cFrontArea.upExt(1))
    //    ////) {
    //    ////  //when (
    //    ////  //  //if (!forFmax) {
    //    ////  //  //  !cFrontArea.wantNonFmaxFwd(
    //    ////  //  //    someExt=cFrontArea.upExt(1)
    //    ////  //  //  )
    //    ////  //  //} else {
    //    ////  //  //  True
    //    ////  //  //}
    //    ////  //  True
    //    ////  //) {
    //    ////    tempModMemReadAsync := (
    //    ////      modMem.readAsync(
    //    ////        address=(cFrontArea.upExt(1).memAddr)
    //    ////        //address=upExt(1).memAddr,
    //    ////        //enable=tempCond,
    //    ////      )
    //    ////    )
    //    ////  //}
    //    ////}
    //    ////--------
    //    //when (
    //    //  (RegNextWhen(True, io.front.isFiring) init(False))
    //    //  && (RegNextWhen(True, io.modFront.isFiring) init(False))
    //    //  && (RegNextWhen(True, io.modBack.isFiring) init(False))
    //    //  && (RegNextWhen(True, io.back.isFiring) init(False))
    //    //  //&& up.isValid
    //    //  //up.isFiring
    //    //  //&& cFront.down.isValid
    //    //  //&& upExt(1).hazardId.msb
    //    //  //&& upExt(1).rdId.msb
    //    //  //&& !rSetRdId
    //    //  //--------
    //    //  //&& up.isFiring
    //    //  //&& rRdId === upExt(0).rdId
    //    //  //&& upExt(0).hazardId.msb
    //    //  //True
    //    //  && up.isValid
    //    //  && !rSetRdId
    //    //  && (
    //    //    if (optEnableModDuplicate) {
    //    //      upExt(1).hazardId.msb
    //    //    } else {
    //    //      True
    //    //    }
    //    //  )
    //    //  //&& !tempCond
    //    //  && tempCond(someExt=upExt(1))
    //    //) {
    //    //  //--------
    //    //  //tempModMemReadAsync := RegNextWhen(
    //    //  //  modMem.readAsync(
    //    //  //    address=upExt(1).memAddr,
    //    //  //  ),
    //    //  //  cFront.up.isFiring
    //    //  //)
    //    //  //tempModMemReadAsync
    //    //  assert(
    //    //    upExt(1).rdMemWord
    //    //    //=== tempModMemReadAsync
    //    //    //=== RegNextWhen(
    //    //    //  RegNextWhen(
    //    //    //    RegNextWhen(
    //    //    //      RegNextWhen(
    //    //    //        //cFrontArea.myDbgRdMemWord,
    //    //    //        modMem.readSync(
    //    //    //          address=cFrontArea.upExt(1).memAddr
    //    //    //        ),
    //    //    //        cFrontArea.up.isFiring,
    //    //    //      ),
    //    //    //      mod.front.cMid0Front.up.isFiring,
    //    //    //    ),
    //    //    //    mod.front.cMid1Front.up.isFiring,
    //    //    //  ),
    //    //    //  mod.front.cMid2Front.up.isFiring,
    //    //    //)
    //    //    === /*RegNextWhen*/(
    //    //      modMem.readAsync(
    //    //        address=upExt(1).memAddr
    //    //      )
    //    //    )
    //    //  )
    //    //  cover(
    //    //    upExt(1).rdMemWord
    //    //    === modMem.readAsync(
    //    //      address=upExt(1).memAddr
    //    //    )
    //    //    && upExt(1).rdMemWord.asBits =/= 0
    //    //    && (
    //    //      RegNextWhen(
    //    //        True, io.back.isFiring
    //    //      ) init(False)
    //    //    )
    //    //  )
    //    //  //--------
    //    //  //when (rDbgSeenFrontUpFiring) {
    //    //  //  assert(
    //    //  //    upExt(1).rdMemWord
    //    //  //    === (
    //    //  //      //RegNextWhen(
    //    //  //        modMem.readAsync(
    //    //  //          address=rDbgPrevMemAddr
    //    //  //        )
    //    //  //        //cFront.down.isFiring
    //    //  //      //) init(upExt(1).rdMemWord.getZero)
    //    //  //    )
    //    //  //  )
    //    //  //}
    //    //}
    //  }
    //  //cover(
    //  //  upExt(1).rdMemWord
    //  //  === cFrontArea.myDbgRdMemWord
    //  //)
    //}
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
    haltWhen(
      !(RegNextWhen(True, io.front.isFiring) init(False))
    )
    val upExt = Vec.fill(
      //2
      1
    )(
      mkExt(myVivadoDebug=true)
    ).setName("cBackArea_upExt")
    //upExt(1) := (
    //  RegNext(upExt(1)) init(upExt(1).getZero)
    //)
    //upExt(1).allowOverride
    //when (up.isValid) {
    //  upExt(1) := upExt(0)
    //}

    myUpExtDel(myUpExtDel.size - 2) := (
      RegNext(myUpExtDel(myUpExtDel.size - 2))
      init(myUpExtDel(myUpExtDel.size - 2).getZero)
    )
    when (up.isValid) {
      myUpExtDel(myUpExtDel.size - 2) := upExt(0)
    }

    val tempUpMod = modType().setName("cBackArea_tempUpMod")
    tempUpMod.allowOverride
    tempUpMod := up(mod.back.pipePayload)
    tempUpMod.getPipeMemRmwExt(
      outpExt=upExt(0),
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
        //if (optEnableModDuplicate) (
        //  upExt(0).hazardId.msb
        //) else (
          True
        //)
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
    myWriteAddr := (
      if (optEnableClear) (
        Mux[UInt](
          io.clear.fire,
          io.clear.payload,
          upExt(0).memAddr,
        )
      ) else (
        upExt(0).memAddr
      )
    )
    val myWriteData = mod.back.myWriteData
    myWriteData := (
      //upExt(0).modMemWord
      if (optEnableClear) (
        Mux[WordT](
          io.clear.fire,
          wordType().getZero,
          upExt(0).modMemWord,
        )
      ) else (
        upExt(0).modMemWord
      )
    )
    val myWriteEnable = mod.back.myWriteEnable
    myWriteEnable := (
      dbgDoWrite
      || (
        if (optEnableClear) (
          io.clear.fire
        ) else (
          False
        )
      )
    )
    myUpExtDel(myUpExtDel.size - 1) := (
      RegNextWhen(
        myUpExtDel(myUpExtDel.size - 2),
        down.isFiring,
      )
      init(myUpExtDel(myUpExtDel.size - 1).getZero)
    )
    
    when (
      //!clockDomain.isResetActive

      //&& isValid
      //&& up.isFiring
      //&& 
      up.isValid
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
        mod.back.rTempWord := upExt(0).modMemWord
        dbgDoWrite := True
      }
      //modMem.write(
      //  address=upExt(0).memAddr,
      //  data=upExt(0).modMemWord,
      //)
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

    //if (vivadoDebug) {
    //  myRdMemWord.addAttribute(
    //    "MARK_DEBUG", "TRUE",
    //  )
    //}

    val cFront = pipe.addStage(
      name=pipeName + "_DualRd_Front",
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
      optIncludeS2M=false,
    )
    val cMid1 = pipe.addStage(
      name=pipeName + "_DualRd_Mid1",
      optIncludeS2M=false,
      //finish=true,
    )
    //val cMid2 = pipe.addStage(
    //  name=pipeName + "_DualRd_Mid2",
    //  optIncludeS2M=false,
    //)
    //val rPrevMyRdMemWord = (
    //  RegNextWhen(
    //    myRdMemWord, cMid1.up.isFiring
    //  ) init(myRdMemWord.getZero)
    //)
    val cBack = pipe.addStage(
      name=pipeName + "_DualRd_Back",
      //finish=true,
    )
    val cLast = pipe.addStage(
      name=pipeName + "_DualRd_Last",
      finish=true,
    )
    val cIoDualRdBack = DirectLink(
      up=(
        //cBack.down
        cLast.down,
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
        outpExt=myInpUpExt,
        memArrIdx=memArrIdx,
      )
      //myMidDualRd := myInpDualRd
      //myMidDualRd.allowOverride
      //myMidDualRd.setPipeMemRmwExt(
      //  inpExt=
      //  memArrIdx=memArrIdx,
      //)
      //up(midPipePayload) := myMidDualRd
      myRdMemWord := dualRdMem.readSync(
        address=myInpUpExt.memAddr,
        enable=up.isFiring,
      )
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
        outpExt=myInpUpExt,
        memArrIdx=memArrIdx,
      )
      myOutpUpExt := myInpUpExt
      myOutpUpExt.allowOverride
      myOutpDualRd := myInpDualRd
      myOutpDualRd.allowOverride
      myOutpDualRd.setPipeMemRmwExt(
        inpExt=myOutpUpExt,
        memArrIdx=memArrIdx,
      )
      //up(midPipePayload) := myOutpDualRd
      //when (up.isFiring) {
      //}
      val rDoIt = Reg(Bool()) init(False)
      myOutpUpExt.rdMemWord := (
        RegNext(myOutpUpExt.rdMemWord) init(myOutpUpExt.rdMemWord.getZero)
      )
      myOutpUpExt.modMemWord := myOutpUpExt.rdMemWord
      up(outpPipePayload) := myOutpDualRd
      when (
        up.isValid
        && !rDoIt
      ) {
        rDoIt := True
        myOutpUpExt.rdMemWord := (
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
case class SamplePipeMemRmwModType[
  WordT <: Data,
  HazardCmpT <: Data,
](
  //wordWidth: Int,
  wordType: HardType[WordT],
  wordCount: Int,
  hazardCmpType: HardType[HazardCmpT],
  modStageCnt: Int,
) extends Bundle with PipeMemRmwPayloadBase[WordT, HazardCmpT] {
  //--------
  val myExt = PipeMemRmwPayloadExt(
    wordType=wordType(),
    wordCount=wordCount,
    hazardCmpType=hazardCmpType(),
    modStageCnt=modStageCnt,
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
