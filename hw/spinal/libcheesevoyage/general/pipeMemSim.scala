package libcheesevoyage.general

import spinal.core._
//import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._
import spinal.lib.sim.{
  StreamMonitor, StreamDriver, StreamReadyRandomizer,ScoreboardInOrder
}
import scala.collection.mutable.ArrayBuffer
import scala.math._
import spinal.core.formal._



object PipeMemRmwSimDutHaltItState extends SpinalEnum(
  defaultEncoding=binarySequential
) {
  val
    IDLE,
    HALT_IT
    = newElement();
}
object PipeMemRmwSimDut {
  def wordWidth = 8
  def wordType() = UInt(wordWidth bits)
  def wordCount = (
    //4
    8
  )
  def hazardCmpType() = UInt(
    PipeMemRmw.addrWidth(wordCount=wordCount) bits
  )
  def modStageCnt = (
    if (optModHazardKind != PipeMemRmw.modHazardKindFwd) (
      //2
      1
    ) else (
      //0
      1
    )
  )
  def modRdPortCnt = (
    1
    //2
  )
  //def doAddrOneHaltIt = (
  //  optModHazardKind == PipeMemRmw.modHazardKindFwd
  //  //false
  //)
  def doTestModOp = (
    optModHazardKind == PipeMemRmw.modHazardKindFwd
    //false
  )
  def modOpCntWidth = (
    8
  )
  object ModOp extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      //NONE,
      ADD_RA_RB,
      //BRANCH_MISPREDICT,     // `EX` should send a bubble downstream  
      //LDR_RA_RB_DCACHE_HIT,
      //  // no MEM stall for this load instruction, but could cause a 
      //  // stall in EX for the next instruction if the next instruction
      //  // uses this instruction's `rA`
      //LDR_RA_RB_DCACHE_MISS,
      //  // Stall in MEM if 
      LDR_RA_RB,
      MUL_RA_RB,               // stall in EX
      LIM
      = newElement();
  }
  val postMaxModOp = (
    ModOp.MUL_RA_RB
    //ModOp.LIM
  )
  //def formalHaltItCnt = (
  //  1
  //)
  //def formalHaltItCnt = (
  //)
  // Fake instruction opcodes, just for testing different kinds of stalls
  // that may occur 
  //def modType() = SamplePipeMemRmwModType(
  //  wordType=wordType(),
  //  wordCount=wordCount,
  //  hazardCmpType=hazardCmpType(),
  //  modRdPortCnt=modRdPortCnt,
  //  modStageCnt=modStageCnt,
  //  optModHazardKind=optModHazardKind,
  //  //doModInModFront=(
  //  //  true
  //  //  //false
  //  //),
  //)
  def optModHazardKind = (
    //PipeMemRmw.modHazardKindDupl
    PipeMemRmw.modHazardKindFwd
  )
  //def optModFwdToFront = (
  //  //optModHazardKind == PipeMemRmw.modHazardKindFwd
  //  //true
  //  false
  //)
  def doRandFront = (
    //true
    false
  )
  def memAddrPlusAmount = (
      //0x8
      //0x4
      //0x3
      0x2
      //0x1
  )
}
case class PipeMemRmwSimDutModType(
  //optIncludeDcacheHit: Boolean=false
) extends Bundle with PipeMemRmwPayloadBase[UInt, UInt]
{
  val opCnt = (
    (PipeMemRmwSimDut.doTestModOp) generate (
      KeepAttribute(UInt(PipeMemRmwSimDut.modOpCntWidth bits))
    )
  )
  val op = (
    (PipeMemRmwSimDut.doTestModOp) generate (
      KeepAttribute(PipeMemRmwSimDut.ModOp())
    )
  )
  //val dcacheHit = (
  //  // for the purposes of testing `PipeMemRmw`, this can just be the value
  //  // that was driven into the `io.front(io.frontPayload)`, as it is
  //  // determined in `MEM` in the real CPU anyway.
  //  (PipeMemRmwSimDut.doTestModOp) generate (
  //    KeepAttribute(
  //      Bool()
  //    )
  //  )
  //)
  //val myDcacheHit = (
  //  (
  //    PipeMemRmwSimDut.doTestModOp
  //    && optIncludeDcacheHit
  //  ) generate (
  //    KeepAttribute(Bool())
  //  )
  //)
  val myExt = PipeMemRmwPayloadExt(
    wordType=PipeMemRmwSimDut.wordType(),
    wordCount=PipeMemRmwSimDut.wordCount,
    hazardCmpType=PipeMemRmwSimDut.hazardCmpType(),
    modRdPortCnt=PipeMemRmwSimDut.modRdPortCnt,
    modStageCnt=PipeMemRmwSimDut.modStageCnt,
    optModHazardKind=PipeMemRmwSimDut.optModHazardKind
  )
  def setPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[UInt, UInt],
    memArrIdx: Int,
  ): Unit = {
    myExt := ext
  }
  def getPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[UInt, UInt],
    memArrIdx: Int,
  ): Unit = {
    ext := myExt
  }
}
//case class PipeMemRmwSimDutIo() extends Area {
//  //val front = slave(Stream(PipeMemRmwSimDut.modType()))
//  //val modFront = master(Stream(PipeMemRmwSimDut.modType()))
//  //val modBack = slave(Stream(PipeMemRmwSimDut.modType()))
//  //val back = master(Stream(PipeMemRmwSimDut.modType()))
//  val front = Node()
//  val modFront = Node()
//  val modBack = Node()
//  val back = Node()
//
//  val midModStages = (PipeMemRmwSimDut.modStageCnt > 0) generate (
//    /*in*/(Vec.fill(PipeMemRmwSimDut.modStageCnt)(
//      PipeMemRmwSimDut.modType())
//    )
//  )
//}
//case class PipeMemRmwDoModFuncSimDut(
//) extends Component {
//  def wordWidth = 
//}

case class PipeMemRmwSimDut(
) extends Area {
  def doFormal: Boolean = {
    GenerationFlags.formal {
      return true
    }
    return false
  }
  def wordWidth = PipeMemRmwSimDut.wordWidth
  def wordType() = PipeMemRmwSimDut.wordType()
  def wordCount = PipeMemRmwSimDut.wordCount
  def hazardCmpType() = PipeMemRmwSimDut.hazardCmpType()
  def modRdPortCnt = (
    PipeMemRmwSimDut.modRdPortCnt
  )
  def modStageCnt = (
    //2
    //1
    //0
    PipeMemRmwSimDut.modStageCnt
  )
  def optModHazardKind = (
    PipeMemRmwSimDut.optModHazardKind
  )
  //def optModFwdToFront = (
  //  PipeMemRmwSimDut.optModFwdToFront
  //)
  def ModType() = PipeMemRmwSimDutModType()
  //val io = PipeMemRmwSimDutIo()
  //val dcacheHitIo = new Bundle {
  //  //val valid = Bool()
  //  val ready = Bool()
  //}
  case class PipeMemRmwSimDutDcacheHitIo(
  ) extends Area {
    val nextValid = Bool()
    val rValid = (
      RegNext(nextValid)
      init(nextValid.getZero)
    )
    nextValid := rValid
    val ready = Bool()
    //val rReady = (
    //  RegNext(nextReady)
    //  init(nextReady.getZero)
    //)
    val fire = rValid && ready
    anyseq(ready)
    //val savedDcacheHitIo = PipeMemRmwSimDutDcacheHitIo()
    //savedDcacheHitIo.nextValid.allowOverride
    //savedDcacheHitIo.ready.allowOverride
    def mkSaved(
      someLink: CtrlLink,
      myName: String,
    ) = new Area {
      //println(
      //  s"${myName}"
      //)
      val nextCaptureState = (
        KeepAttribute(
          Vec.fill(2)(
            Bool()
          )
        )
        .setName(
          s"${myName}"
          + s"_nextCaptureState"
        )
      )
      val rCaptureState = (
        KeepAttribute(
          RegNext(nextCaptureState)
          //init(nextCaptureState.getZero)
        )
        .setName(
          s"${myName}"
          + s"_rCaptureState"
        )
      )
      for (idx <- 0 until rCaptureState.size) {
        rCaptureState(idx).init(nextCaptureState(idx).getZero)
      }
      nextCaptureState := rCaptureState
      val savedNextValid = (
        KeepAttribute(
          Bool()
        )
        .setName(
          s"$myName"
          + s"_savedNextValid"
        )
      )
      val savedReady = (
        KeepAttribute(
          Bool()
        )
        .setName(
          s"$myName"
          + s"_savedReady"
        )
      )
      val rSavedFire = (
        KeepAttribute(
          Reg(
            Bool()
          )
          init(
            False
          )
        )
        .setName(
          s"$myName"
          + s"_savedFire"
        )
      )
      val eitherFire = (
        KeepAttribute(
          fire
          || rSavedFire
        )
        .setName(
          s"$myName"
          + s"_eitherFire"
        )
      )
      savedNextValid := (
        RegNext(savedNextValid)
        init(savedNextValid.getZero)
      )
      savedReady := (
        RegNext(savedReady)
        init(savedReady.getZero)
      )
      //savedFire := (
      //  RegNext(savedFire)
      //  init(savedFire.getZero)
      //)
      when (
        someLink.up.isValid 
      ) {
        when (
          !rCaptureState(0)
        ) {
          //outp := inp
          //myCurrOp := inp.op
          savedNextValid := nextValid
          savedReady := ready
          //savedFire := fire
          nextCaptureState(0) := True
        }
        when (fire) {
          rSavedFire := True
        }
        when (
          someLink.up.isFiring
        ) {
          nextCaptureState(0) := False
          rSavedFire := False
        }
      }
      //when (
      //  !rCaptureState(1)
      //  && fire
      //) {
      //  nextCaptureState(1) := True
      //  savedFire := True
      //}
      //when (
      //  //rCaptureState(1)
      //  !ready
      //) {
      //  nextCaptureState(1) := False
      //  savedFire := False
      //}

      //when (
      //  (
      //    !rCaptureState(1)
      //  ) && (
      //    fire
      //  )
      //) {
      //  nextCaptureState(1) := True
      //  savedFire := True
      //}
      //when (
      //  someLink.up.isFiring
      //) {
      //  nextCaptureState(1) := False
      //  savedFire := False
      //}
    }
    //when (
    //  !valid
    //) {
    //  assume(ready === False)
    //}
    //when (
    //  valid
    //)
    //--------
    //val myHistValid = (
    //  KeepAttribute(
    //    History[Bool](
    //      that=rValid,
    //      length=myProveNumCycles,
    //      init=False,
    //    )
    //  )
    //  .setName(s"dcacheHitIo_myHistValid")
    //)
    //val tempValidFindFirst = (
    //  myHistValid.sFindFirst(
    //    _ === True
    //  )
    //)
    ////--------
    //val myHistReady = (
    //  KeepAttribute(
    //    History[Bool](
    //      that=ready,
    //      length=myProveNumCycles,
    //      init=False,
    //    )
    //  )
    //  .setName(s"dcacheHitIo_myHistReady")
    //)
    //val tempReadyFindFirst = (
    //  myHistReady.sFindFirst(
    //    _ === True
    //  )
    //)
    //--------
    when (pastValidAfterReset) {
      when (fire) {
        assume(!RegNext(ready))
      }
      //when (!rValid) {
      //  assume(!ready)
      //}
      when (!nextValid) {
        assume(!RegNext(ready))
      }
      ////when (!nextValid) {
      ////  assume(!RegNext(ready))
      ////}
      ////--------
      //when (stable(rValid)) {
      //  //assume(!RegNext(ready))
      //  assume(
      //    stable(ready)
      //  )
      //}
      ////when (rValid) {
      ////  when (ready) {
      ////    when (!past(ready)) {
      ////      assume(stable(ready))
      ////    }
      ////  }
      ////}
      ////--------
      //when (rValid) {
      //  when (ready) {
      //    when (!nextValid) {
      //      assume(!past(ready))
      //    }
      //  }
      //}
      ////when (!nextValid) {
      ////  assume(
      ////    !RegNext(ready)
      ////  )
      ////}
      ////when (
      ////  (
      ////    tempValidFindFirst._1
      ////  )
      ////  //&& (
      ////  //  !tempReadyFindFirst._1
      ////  //)
      ////) {
      ////}
    }
  }
  val dcacheHitIo = PipeMemRmwSimDutDcacheHitIo()
  //anyseq(valid)
  //val dcacheHitReady = Bool()
  //anyseq(dcacheHitReady)
  val pipeMem = PipeMemRmw[
    UInt,
    UInt,
    //SamplePipeMemRmwModType[UInt, UInt],
    PipeMemRmwSimDutModType,
    PipeMemRmwDualRdTypeDisabled[UInt, UInt],
  ](
    wordType=wordType(),
    wordCountArr=Array.fill(1)(wordCount).toSeq,
    hazardCmpType=hazardCmpType(),
    modType=ModType(),
    modRdPortCnt=modRdPortCnt,
    modStageCnt=modStageCnt,
    pipeName="PipeMemRmw_FormalDut",
    dualRdType=(
      //modType()
      PipeMemRmwDualRdTypeDisabled[UInt, UInt](),
    ),
    optDualRd=(
      //true
      false
    ),
    init=None,
    initBigInt={
      val tempArr = new ArrayBuffer[BigInt]()
      for (idx <- 0 until wordCount) {
        //val toAdd: Int = idx * 2
        val toAdd: Int = 0x0
        //println(s"${toAdd} ${idx} ${wordCount}")
        //tempArr += idx * 2
        tempArr += toAdd
        //println(tempArr.last)
      }
      //Some(Array.fill(wordCount)(BigInt(0)).toSeq)
      Some(Array.fill(1)(tempArr.toSeq).toSeq)
    },
    //optEnableModDuplicate=(
    //  true,
    //  //false,
    //),
    optModHazardKind=(
      optModHazardKind
    ),
    //optModFwdToFront=(
    //  optModFwdToFront
    //),
    //forFmax=forFmax,
    optFormal=doFormal,
  )(
    doHazardCmpFunc=None,
    doPrevHazardCmpFunc=false,
    doModInFrontFunc=(
      if (!(
        optModHazardKind == PipeMemRmw.modHazardKindFwd
        && PipeMemRmwSimDut.doTestModOp
      )) {
        None
      } else {
        Some(
          (
            outp,
            inp,
            cFront,
            ydx,
          ) => new Area {
            //GenerationFlags.formal {
              when (
                cFront.up.isValid
                && (RegNextWhen(True, cFront.up.isFiring) init(False))
              ) {
                assume(
                  inp.opCnt
                  === RegNextWhen(outp.opCnt, cFront.up.isFiring) + 1
                )
              }
              //println(
              //  "doModInFrontFunc(): Don't forget about the `assume` "
              //  + "statement for `inp.op`!"
              //)
              assume(
                inp.op.asBits.asUInt
                //< PipeMemRmwSimDut.ModOp.MUL_RA_RB.asBits.asUInt
                < PipeMemRmwSimDut.postMaxModOp.asBits.asUInt
              )
              when (pastValidAfterReset) {
                assert(
                  stable(inp.myExt.hazardCmp)
                )
                assert(
                  stable(inp.myExt.modMemWord)
                )
                assert(
                  stable(inp.myExt.rdMemWord)
                  //=== inp.myExt.rdMemWord.getZero
                )
                assert(
                  inp.myExt.rdMemWord
                  === inp.myExt.rdMemWord.getZero
                )
                assert(
                  stable(inp.myExt.hazardCmp)
                  //=== inp.myExt.hazardCmp.getZero
                )
                assert(
                  stable(inp.myExt.modMemWordValid) //=== True
                )
              }
            //}
          }
        )
      }
    ),
    doModInModFrontFunc=(
      if (optModHazardKind != PipeMemRmw.modHazardKindFwd) (
        None
      ) else (
        Some(
          (
            doModInModFrontParams
            //nextPrevTxnWasHazardVec,
            //rPrevTxnWasHazardVec,
            //rPrevTxnWasHazardAny,
            //outpVec,
            //inpVec,
            //cMid0Front,
            //modFront,
            //tempModFrontPayloadVec,
            ////myRdMemWord,
            //getMyRdMemWordFunc,
            //ydx,
          ) => new Area {
            def nextPrevTxnWasHazard = (
              doModInModFrontParams.nextPrevTxnWasHazardVec(ydx)
            )
            def rPrevTxnWasHazard = (
              doModInModFrontParams.rPrevTxnWasHazardVec(ydx)
            )
            def rPrevTxnWasHazardAny = (
              doModInModFrontParams.rPrevTxnWasHazardAny
            )
            def outp = doModInModFrontParams.outpVec(ydx)
            def inp = doModInModFrontParams.inpVec(ydx)
            def cMid0Front = doModInModFrontParams.cMid0Front
            def modFront = doModInModFrontParams.modFront
            def tempModFrontPayload = (
              doModInModFrontParams.tempModFrontPayloadVec(ydx)
            )
            def ydx = doModInModFrontParams.ydx
            assume(
              inp.op.asBits.asUInt
              //< PipeMemRmwSimDut.ModOp.MUL_RA_RB.asBits.asUInt
              < PipeMemRmwSimDut.postMaxModOp.asBits.asUInt
            )

            val nextSetOutpState = (
              KeepAttribute(
                Bool()
              )
              .setName(s"nextSetOutpState")
            )
            val rSetOutpState = (
              KeepAttribute(
                RegNext(nextSetOutpState)
                init(nextSetOutpState.getZero)
              )
              .setName(s"rSetOutpState")
            )
            nextSetOutpState := rSetOutpState

            outp := (
              RegNext(outp)
              init(outp.getZero)
            )
            outp.allowOverride
            val myCurrOp = (
              KeepAttribute(
                cloneOf(inp.op)
              )
              .setName(s"myCurrOp")
            )
            myCurrOp := (
              RegNext(myCurrOp)
              //init(myCurrOp.getZero)
            )
            def myRdMemWord = (
              doModInModFrontParams.getMyRdMemWordFunc(
                //Mux[UInt](
                //  (
                //    cMid0Front.up.isValid
                //    && !rSetOutpState
                //  ),
                  U(s"2'd1"),
                  //U(s"2'd2"),
                //),
                ydx
              )
            )
            when (
              cMid0Front.up.isValid 
            ) {
              when (
                !rSetOutpState
              ) {
                outp := inp
                myCurrOp := inp.op
                nextSetOutpState := True
              }
              when (
                cMid0Front.up.isFiring
              ) {
                nextSetOutpState := False
              }
            }
            //when (
            //  cMid0Front.up.isReady
            //) {
            //  outp := inp
            //}
            val nextHaltItState = KeepAttribute(
              PipeMemRmwSimDutHaltItState()
            ).setName("doModInModFrontFunc_nextHaltItState")
            val rHaltItState = KeepAttribute(
              RegNext(nextHaltItState)
              init(PipeMemRmwSimDutHaltItState.IDLE)
            )
              .setName("doModInModFrontFunc_rHaltItState")
            val nextMulHaltItCnt = SInt(4 bits)
              .setName("doModInModFrontFunc_nextMulHaltItCnt")
            val rMulHaltItCnt = (
              RegNext(nextMulHaltItCnt)
              init(-1)
            )
              .setName("doModInModFrontFunc_rMulHaltItCnt")
            nextHaltItState := rHaltItState
            nextMulHaltItCnt := rMulHaltItCnt
            def setOutpModMemWord(
              someRdMemWord: UInt=myRdMemWord
            ): Unit = {
              outp.myExt.modMemWord := (
                someRdMemWord + 0x1
              )
              outp.myExt.modMemWordValid := True
            }
            val rSavedRdMemWord = (
              Reg(cloneOf(myRdMemWord))
              init(myRdMemWord.getZero)
            )
              .setName("doModInModFrontFunc_rSavedRdMemWord")
            val rPrevOutp = KeepAttribute(
              RegNextWhen(
                outp,
                cMid0Front.up.isFiring
              )
              init(outp.getZero)
            )
              .setName("doModInModFrontFunc_rPrevOutp")

            //def doCheckHazardDim = 2
            //def haveCurrLoadDim = 2
            val doTestModOpMainArea = (
              PipeMemRmwSimDut.doTestModOp
            ) generate (
              new Area {
                val doCheckHazard = (
                  KeepAttribute(
                    Bool()
                  )
                  .setName(
                    s"doTestModOpMainArea"
                    + s"_doCheckHazard"
                  )
                )
                doCheckHazard := (
                  RegNext(doCheckHazard)
                  init(doCheckHazard.getZero)
                )
                val haveCurrLoad = (
                  KeepAttribute(
                    Bool()
                  )
                  .setName(
                    s"doTestModOpMainArea"
                    + s"_haveCurrLoad"
                  )
                )
                haveCurrLoad := (
                  RegNext(haveCurrLoad)
                  init(haveCurrLoad.getZero)
                )
                //val doIt = (
                //  KeepAttribute(
                //    Bool()
                //  )
                //  .setName(
                //    s"doTestModOpMainArea"
                //    + s"_doIt"
                //  )
                //)
                //doIt := (
                //  RegNext(doIt)
                //  init(doIt.getZero)
                //)
                val nextState = (
                  KeepAttribute(
                    Bool()
                  )
                  .setName(
                    s"doTestModOpMainArea"
                     + s"_nextState"
                  )
                )
                val rState = (
                  KeepAttribute(
                    RegNext(
                      //Vec.fill(doCheckHazardDim)(
                      //  Vec.fill(haveCurrLoadDim)(
                          //Bool()
                          nextState
                      //  )
                      //)
                    )
                    //init(nextState.getZero)
                  )
                  .setName(
                    s"doTestModOpMainArea"
                    + s"_rState"
                  )
                )
                nextState := rState
                val nextSavedRdMemWord1 = (
                  KeepAttribute
                  (
                    //Vec.fill(doCheckHazardDim)(
                    //  Vec.fill(haveCurrLoadDim)(
                        Flow(
                          PipeMemRmwSimDut.wordType()
                        )
                    //  )
                    //)
                  )
                  .setName(
                    s"doTestModOpMainArea"
                    + s"_nextSavedRdMemWord1"
                  )
                )
                val rSavedRdMemWord1 = (
                  KeepAttribute
                  (
                    //Reg(cloneOf(myRdMemWord))
                    //init(myRdMemWord.getZero)
                    RegNext(
                      nextSavedRdMemWord1
                    )
                  )
                  .setName(
                    s"doTestModOpMainArea"
                    + s"_rSavedRdMemWord1"
                  )
                )
                nextSavedRdMemWord1 := rSavedRdMemWord1
                val rTempPrevOp = (
                  KeepAttribute(
                    RegNextWhen(
                      //inp.op,
                      //outp.op,
                      myCurrOp,
                      cMid0Front.up.isFiring
                    )
                    init(
                      PipeMemRmwSimDut.ModOp.ADD_RA_RB
                    )
                  )
                  .setName(
                    s"doTestModOpMainArea"
                    + s"_rTempPrevOp"
                  )
                )
                //for (myYdx <- 0 until doCheckHazardDim) {
                //  for (myXdx <- 0 until haveCurrLoadDim) {
                    rState/*(myYdx)(myXdx)*/.init(
                      rState/*(myYdx)(myXdx)*/.getZero
                    )
                    rSavedRdMemWord1/*(myYdx)(myXdx)*/.init(
                      rSavedRdMemWord1/*(myYdx)(myXdx)*/.getZero
                    )
                //  }
                //}
                //rTempPrevOp.init(
                //  //rTempPrevOp.getZero
                //  PipeMemRmwSimDut.ModOp.ADD_RA_RB
                //)
                //val doIt = (
                //  KeepAttribute(
                //    Bool()
                //  )
                //  .setName(
                //    s"doHandleHazardWithDcacheMissArea"
                //    + s"_doIt"
                //  )
                //)
                //doIt := (
                //  RegNext(doIt)
                //  init(doIt.getZero)
                //)
                //doIt := True
                val savedDcacheHitIo = (
                  dcacheHitIo.mkSaved(
                    someLink=cMid0Front,
                    myName=s"cMid0Front",
                  )
                )
              }
            )
            //val doHandleHazardWithDcacheMissArea = (
            //  PipeMemRmwSimDut.doTestModOp
            //) generate (
            //  new Area {
            //  }
            //)
            def doMulHaltItFsmIdleInnards(
              //doDuplicateIt: Boolean
            ): Unit = {
              if (PipeMemRmwSimDut.doTestModOp) {
                def myInitMulHaltItCnt = 0x1
                cMid0Front.duplicateIt()
                //outp := (
                //  RegNext(outp)
                //  init(outp.getZero)
                //)
                when (
                  //cMid0Front.down.isFiring
                  //modFront.isFiring
                  modFront.isValid
                ) {
                  nextHaltItState := (
                    PipeMemRmwSimDutHaltItState.HALT_IT
                  )
                  nextMulHaltItCnt := myInitMulHaltItCnt
                }
                outp.myExt.modMemWordValid := False
                rSavedRdMemWord := myRdMemWord
              }
            }
            def doMulHaltItFsmHaltItInnards(): Unit = {
              if (PipeMemRmwSimDut.doTestModOp) {
                //outp := (
                //  RegNext(outp)
                //  init(outp.getZero)
                //)
                when ((rMulHaltItCnt - 1).msb) {
                  when (
                    cMid0Front.down.isFiring
                    //modFront.isFiring
                  ) {
                    setOutpModMemWord(rSavedRdMemWord)
                    nextHaltItState := PipeMemRmwSimDutHaltItState.IDLE
                  }
                } otherwise {
                  nextMulHaltItCnt := rMulHaltItCnt - 1
                  //cMid0Front.haltIt()
                  cMid0Front.duplicateIt()
                  outp.myExt.modMemWordValid := False
                }
              }
            }
            def doTestModOpMain(
              //doCheckHazard: Bool//ean//=false
            ) = new Area {
              def doCheckHazard = (
                doTestModOpMainArea.doCheckHazard
              )
              def haveCurrLoad = (
                doTestModOpMainArea.haveCurrLoad
              )
              //val myFindFirstHazardAddr = (doCheckHazard) generate (
              //  KeepAttribute(
              //    inp.myExt.memAddr.sFindFirst(
              //      _ === rPrevOutp.myExt.memAddr(PipeMemRmw.modWrIdx)
              //    )
              //    //(
              //    //  // Only check one register.
              //    //  // This will work fine for testing the different
              //    //  // categories of stalls, but the real CPU will need to
              //    //  /// be tested for *all* registers
              //    //  inp.myExt.memAddr(PipeMemRmw.modWrIdx)
              //    //  === rPrevOutp.myExt.memAddr(PipeMemRmw.modWrIdx)
              //    //)
              //    .setName("myFindFirstHazardAddr")
              //  )
              //)
              def handleCurrFire(
                someRdMemWord: UInt=myRdMemWord
              ): Unit = {
                outp.myExt.valid := True
                nextPrevTxnWasHazard := False
                setOutpModMemWord(
                  someRdMemWord=someRdMemWord
                )
              }
              def handleDuplicateIt(
                //actuallyDuplicateIt: Boolean=true
                someModMemWordValid: Bool
              ): Unit = {
                //outp := (
                //  RegNext(outp) init(outp.getZero)
                //)
                //nextPrevTxnWasHazard := True
                outp.myExt.valid := False
                outp.myExt.modMemWordValid := (
                  //False
                  someModMemWordValid
                )
                //if (actuallyDuplicateIt) {
                  cMid0Front.duplicateIt()
                //}
              }
              when (
                cMid0Front.up.isValid
                //|| (
                //  nextSetOutpState
                //  //rSetOutpState
                //)
              ) {
                switch (
                  //inp.op
                  myCurrOp
                ) {
                  is (PipeMemRmwSimDut.ModOp.ADD_RA_RB) {
                    ////if (!doCheckHazard) {
                    //when (!doCheckHazard) {
                    //  //when (cMid0Front.up.isValid) {
                    //    setOutpModMemWord()
                    //  //}

                    //  doHandleHazardWithDcacheMissArea.doIt := False
                    //  //haveCurrLoad := (
                    //  //  RegNext(haveCurrLoad)
                    //  //  init
                    //  //)
                    ////} else { // if (doCheckHazard)
                    //} otherwise {
                      haveCurrLoad := False
                      //doHandleHazardWithDcacheMissArea.doIt := True
                      //doHandleHazardWithDcacheMiss(
                      //  haveCurrLoad=false,
                      //)
                    //}
                    //}
                  }
                  is (PipeMemRmwSimDut.ModOp.LDR_RA_RB) {
                    //if (!doCheckHazard) {
                    //when (!doCheckHazard) {
                    //  when (cMid0Front.up.isFiring) {
                    //    setOutpModMemWord()
                    //    nextPrevTxnWasHazard := True
                    //  }
                    //  doHandleHazardWithDcacheMissArea.doIt := False
                    ////} else { // if (doCheckHazard)
                    //} otherwise {
                      //nextPrevTxnWasHazard := True
                      haveCurrLoad := True
                      //doHandleHazardWithDcacheMissArea.doIt := True
                      //doHandleHazardWithDcacheMiss(
                      //  haveCurrLoad=true,
                      //)
                    //}
                    //}
                  }
                  is (PipeMemRmwSimDut.ModOp.MUL_RA_RB) {
                    // we should stall `EX` in this case until the
                    // calculation is done. The same stalling logic
                    // will be used for `divmod`, etc.
                    //doHandleHazardWithDcacheMissArea.doIt := False
                    haveCurrLoad := False
                    //switch (rHaltItState) {
                    //  is (PipeMemRmwSimDutHaltItState.IDLE) {
                    //    doMulHaltItFsmIdleInnards(
                    //      //doDuplicateIt=(
                    //      //  //true
                    //      //  doCheckHazard
                    //      //)
                    //    )
                    //  }
                    //  is (PipeMemRmwSimDutHaltItState.HALT_IT) {
                    //    doMulHaltItFsmHaltItInnards()
                    //    when (
                    //      nextHaltItState
                    //      === PipeMemRmwSimDutHaltItState.IDLE
                    //    ) {
                    //      nextPrevTxnWasHazard := False
                    //    }
                    //  }
                    //}
                  }
                }
                //when (
                //  //doHandleHazardWithDcacheMissArea.doIt
                //) {
                //  doHandleHazardWithDcacheMiss()
                //}
              }
              def nextState = (
                doTestModOpMainArea.nextState
              )
              def rState = (
                doTestModOpMainArea.rState
              )
              def nextSavedRdMemWord1 = (
                doTestModOpMainArea.nextSavedRdMemWord1
              )
              def rSavedRdMemWord1 = (
                doTestModOpMainArea.rSavedRdMemWord1
              )
              def rTempPrevOp = (
                doTestModOpMainArea.rTempPrevOp
              )
              def savedDcacheHitIo = (
                doTestModOpMainArea.savedDcacheHitIo
              )
              //switch (
              //  rState
              //) {
              //  is (False) {
                  //when (cMid0Front.up.isValid) {
                    //--------
                    when (doCheckHazard) {
                      when (
                        //(
                        //  !savedDcacheHitIo.eitherFire
                        //) && 
                        (
                          (
                            outp.myExt.memAddr(0)
                            === tempModFrontPayload.myExt.memAddr(0)
                          ) && (
                            !tempModFrontPayload.myExt.modMemWordValid
                          )
                        )
                      ) {
                        handleDuplicateIt(
                          someModMemWordValid=(
                            //!savedDcacheHitIo.eitherFire
                            False
                          )
                        )
                      }
                      cover(
                        //(
                        //  savedDcacheHitIo.eitherFire
                        //) 
                        //&& 
                        (
                          outp.myExt.memAddr(0)
                          === tempModFrontPayload.myExt.memAddr(0)
                        ) 
                        //&& (
                        //  //tempModFrontPayload.myExt.modMemWordValid
                        //) 
                        && (
                          cMid0Front.up.isFiring
                        )
                      )
                    }
                    //--------
                    when (
                      //savedDcacheHitIo.eitherFire
                      dcacheHitIo.fire
                    ) {
                      dcacheHitIo.nextValid := False
                    }
                    //--------
                    when (cMid0Front.up.isFiring) {
                      //when (dcacheHitIo.fire) {
                      //  dcacheHitIo.nextValid := False
                      //}
                      handleCurrFire()
                      //switch (
                      //  myCurrOp
                      //) {
                      //  is (PipeMemRmwSimDut.ModOp.ADD_RA_RB) {
                      //  }
                      //  is (PipeMemRmwSimDut.ModOp.LDR_RA_RB) {
                      //    nextPrevTxnWasHazard := True
                      //    dcacheHitIo.nextValid := (
                      //      //!outp.dcacheHit
                      //      True
                      //    )
                      //  }
                      //  is (PipeMemRmwSimDut.ModOp.MUL_RA_RB) {
                      //  }
                      //}
                      //--------
                      // BEGIN: Old, working code (without ModOp.MUL_RA_RB)
                      //when (haveCurrLoad) {
                      //  nextPrevTxnWasHazard := True
                      //  //dcacheHitIo.valid := True
                      //  dcacheHitIo.nextValid := (
                      //    //!outp.dcacheHit
                      //    True
                      //  )
                      //} otherwise {
                      //  //dcacheHitIo.nextValid := (
                      //  //  False
                      //  //)
                      //}
                      // END: Old, working code (without ModOp.MUL_RA_RB)
                      //--------
                    }
                    when (
                      //cMid0Front.up.isValid
                      cMid0Front.up.isFiring
                    ) {
                      when (haveCurrLoad) {
                        nextPrevTxnWasHazard := True
                        //dcacheHitIo.valid := True
                        when (cMid0Front.up.isFiring) {
                          dcacheHitIo.nextValid := (
                            //!outp.dcacheHit
                            True
                          )
                        }
                      } otherwise {
                        //dcacheHitIo.nextValid := (
                        //  False
                        //)
                      }
                    }
                    //--------
                  //}
              //  }
              //  is (True) {
              //  }
              //}
            }
            //when (cMid0Front.up.isValid) {
              when (
                (
                  if (
                    //PipeMemRmwSimDut.doAddrOneHaltIt
                    PipeMemRmwSimDut.doTestModOp
                  ) (
                    rPrevTxnWasHazard
                    //rPrevTxnWasHazardAny
                  ) else (
                    False
                  )
                )
              ) {
                assert(PipeMemRmwSimDut.modRdPortCnt == 1)
                //doTestModOpMainArea.doIt := True
                doTestModOpMainArea.doCheckHazard := True
              }
              //elsewhen (
              //  cMid0Front.up.isValid
              //)
              .otherwise {
                //when (
                //  False
                //) {
                //  //cMid0Front.haltIt()
                //} else
                when (
                  if (optModHazardKind == PipeMemRmw.modHazardKindDupl) (
                    outp.myExt.hazardId.msb
                  ) else (
                    True
                  )
                ) {
                  if (
                    //PipeMemRmwSimDut.doAddrOneHaltIt
                    PipeMemRmwSimDut.doTestModOp
                  ) {
                    //doTestModOpMain(
                    //  doCheckHazard=false
                    //)
                    //doTestModOpMainArea.doIt := True
                    doTestModOpMainArea.doCheckHazard := False
                  } else {
                    when (cMid0Front.up.isValid) {
                      setOutpModMemWord()
                    }
                  }
                } otherwise {
                  //doTestModOpMainArea.doIt := False
                }
              }
              //when (doTestModOpMainArea.doIt) {
              if (
                PipeMemRmwSimDut.doTestModOp
              ) {
                doTestModOpMain(
                  //doCheckHazard=true
                )
              }
              //}
            //}
          }
        )
      )
    ),
    //doFwdFunc=(
    //  //None
    //  Some(
    //    (
    //      stageIdx,
    //      myUpExtDel2,
    //      zdx,
    //    ) => {
    //      //myUpExtDel.last.modMemWord
    //      //myUpExtDel(
    //      //  Mux[UInt](stageIdx === 0, 1, stageIdx)
    //      //).modMemWord
    //      myUpExtDel2(stageIdx).modMemWord
    //    }
    //  )
    //),
  )
  //GenerationFlags.formal 
  val rSavedModArr = (
    Vec.fill(
      PipeMemRmwSimDut.wordCount
    )(
      Reg(PipeMemRmwSimDut.wordType())
    )
  )
  val rDidInitstate = Reg(Bool()) init(False)
  when (
    //!pastValidAfterReset
    initstate()
  ) {
    assume(rDidInitstate === False)
    rDidInitstate := True
    for (idx <- 0 until rSavedModArr.size) {
      rSavedModArr(idx).init(
        //idx * 2
        //dut.dut.pipeMem.modMem(PipeMemRmw.modWrIdx).readAsync(
        //  address=idx
        //)
        0x0
      )
      rSavedModArr(idx) := (
        pipeMem.modMem(0)(PipeMemRmw.modWrIdx).readAsync(
          address=U{
            val width = log2Up(wordCount)
            s"${width}'d${idx}"
          }
        )
      )
    }
  } otherwise {
    when (
      //pastValidAfterReset
      pastValidAfterReset
      && past(initstate())
    ) {
      //rDidInitstate := False
      assert(
        //False
        rDidInitstate
      )
    }
  }
  def front = pipeMem.io.front
  def frontPayload = pipeMem.io.frontPayload(0)
  def modFront = pipeMem.io.modFront
  def modFrontPayload = pipeMem.io.modFrontPayload(0)
  def modBack = pipeMem.io.modBack
  def modBackPayload = pipeMem.io.modBackPayload(0)
  def back = pipeMem.io.back
  def backPayload = pipeMem.io.backPayload(0)

  //val tempMemAddrPlus = (
  //  (
  //    (
  //      RegNextWhen(
  //        modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
  //        modBack.isFiring,
  //      ) init(0x0)
  //    ) + 1 
  //  ) % PipeMemRmwSimDut.memAddrPlusAmount
  //)

  //val savedModMemWord = Payload(PipeMemRmwSimDut.wordType())
  val myHaveCurrWrite = (
    KeepAttribute(
      pastValidAfterReset
      && modBack.isFiring
      && pipeMem.mod.back.myWriteEnable(0)
    )
    .setName(s"myHaveCurrWrite")
  )
  val tempLeft = (
    /*RegNextWhen*//*RegNext*/(
      //modBack(modBackPayload).myExt.modMemWord,
      //modBack.isFiring,
      pipeMem.mod.back.myWriteData(0),
      //myHaveCurrWrite,
    )
    //init(pipeMem.mod.back.myWriteData(0).getZero)
  )

  //val nextSavedMod = 

  //val next
  //val tempRight = rSavedModArr(
  //  //modBack.myExt.memAddr(PipeMemRmw.modWrIdx)
  //  //RegNextWhen(
  //    //modFront(modFrontPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
  //      RegNextWhen(
  //        modFront(modFrontPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
  //        modFront.isFiring
  //      )
  //    //modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
  //  //  modBack.isFiring,
  //  //) init(0x0)
  //) + 1
  //val tempHistMyWriteAddr = History[UInt](
  //  that=pipeMem.mod.back.myWriteAddr(0),
  //  length=PipeMemRmwFormal.myProveNumCycles,
  //  when=myHaveCurrWrite,
  //  init=0x0,
  //)
  //--------
  def myProveNumCycles = PipeMemRmwFormal.myProveNumCycles
  //val myWriteAt = (
  //  KeepAttribute(
  //    Flow(cloneOf(pipeMem.mod.back.myWriteAddr(0)))
  //  )
  //  .setName(s"myWriteAt")
  //)
  //myWriteAt.valid := myHaveCurrWrite
  //myWriteAt.payload := pipeMem.mod.back.myWriteAddr(0)
  //val tempHistWriteAt = History[Flow[UInt]](
  //  that=myWriteAt,
  //  length=myProveNumCycles,
  //  when=null,
  //  init=myWriteAt.getZero
  //)
  //def tempHistWriteAtFunc(
  //  x: Flow[UInt],
  //  //ydx: Int
  //  //someMemAddr: UInt,
  //) = (
  //  x.valid
  //  && (
  //    x.payload
  //    === (
  //      past(
  //        pipeMem.cBackArea.upExt(1)(0)(
  //          PipeMemRmw.extIdxSingle
  //        ).memAddr(0),
  //      )
  //    )
  //  )
  //    //someMemAddr
  //)
  //val myTempHistWriteAtFindFirst = (
  //  tempHistWriteAt.sFindFirst(
  //    tempHistWriteAtFunc
  //  )
  //)
  //val myDbgTempHistWriteAtFindFirst_1 = (
  //  KeepAttribute(
  //    myTempHistWriteAtFindFirst._1
  //  )
  //  .setName(s"myDbgTempHistWriteAtFindFirst_1")
  //)
  //val myDbgTempHistWriteAtFindFirst_2 = (
  //  KeepAttribute(
  //    myTempHistWriteAtFindFirst._2
  //  )
  //  .setName(s"myDbgTempHistWriteAtFindFirst_2")
  //)
  //val tempHistWriteAtPostFirst = cloneOf(tempHistWriteAt)

  //for (ydx <- 0 until tempHistWriteAtPostFirst.size) {
  //  tempHistWriteAtPostFirst(ydx) := (
  //    tempHistWriteAtPostFirst(ydx).getZero
  //  )
  //}
  //when (myDbgTempHistWriteAtFindFirst_1) {
  //  switch (myDbgTempHistWriteAtFindFirst_2) {
  //    for (ydx <- 0 until tempHistWriteAtPostFirst.size) {
  //      is (ydx) {
  //        //for (idx <- 0 to ydx) {
  //        //  tempHistWriteAtPostFirst(idx) := (
  //        //    tempHistWriteAtPostFirst(idx).getZero
  //        //  )
  //        //}
  //        if (ydx + 1 < tempHistWriteAtPostFirst.size) {
  //          for (idx <- ydx + 1 until tempHistWriteAtPostFirst.size) {
  //            tempHistWriteAtPostFirst(idx) := (
  //              tempHistWriteAt(idx)
  //            )
  //          }
  //        }
  //      }
  //    }
  //  }
  //} otherwise {
  //  for (ydx <- 0 until wordCount) {
  //    assume(
  //      pipeMem.modMem(0)(PipeMemRmw.modWrIdx).readAsync(
  //        address=U(s"${log2Up(wordCount)}'d${ydx}")
  //      ) === (
  //        wordType().getZero
  //      )
  //    )
  //  }
  //}
  //val myTempHistWriteAtPostFirstFindFirst = (
  //  tempHistWriteAtPostFirst.sFindFirst(
  //    tempHistWriteAtFunc
  //  )
  //)
  //val myDbgTempHistWriteAtPostFirstFindFirst_1 = (
  //  KeepAttribute(
  //    myTempHistWriteAtFindFirst._1
  //  )
  //  .setName(s"myDbgTempHistWriteAtPostFirstFindFirst_1")
  //)
  //val myDbgTempHistWriteAtPostFirstFindFirst_2 = (
  //  KeepAttribute(
  //    myTempHistWriteAtFindFirst._2
  //  )
  //  .setName(s"myDbgTempHistWriteAtPostFirstFindFirst_2")
  //)
  //--------
  //val myTempHistWriteAtRevFindFirst = (
  //  tempHistWriteAt.reverse.sFindFirst(
  //    tempHistWriteAtFunc
  //  )
  //)
  //val myDbgTempHistWriteAtRevFindFirst_1 = (
  //  KeepAttribute(
  //    myTempHistWriteAtRevFindFirst._1
  //  )
  //  .setName(s"myDbgTempHistWriteAtRevFindFirst_1")
  //)
  //val myDbgTempHistWriteAtRevFindFirst_2 = (
  //  KeepAttribute(
  //    myTempHistWriteAtRevFindFirst._2
  //  )
  //  .setName(s"myDbgTempHistWriteAtRevFindFirst_2")
  //)

  //val tempHistWroteAtFindFirst1 = (
  //  KeepAttribute(
  //    Vec.fill(tempHistWroteAt.size)(Bool())
  //  )
  //  .setName(s"tempHistWroteAtFindFirst1")
  //)
  //val tempHistWroteAtFindFirst2 = (
  //  KeepAttribute(
  //    Vec.fill(tempHistWroteAt.size)(
  //      UInt(log2Up(wordCount) bits)
  //    )
  //  )
  //  .setName(s"tempHistWroteAtFindFirst2")
  //)
  //val tempHistWroteAtVec
  //val tempHistWriteAtHadWriteVec = (
  //  KeepAttribute(
  //    Vec.fill(wordCount)(Bool())
  //  )
  //  .setName(s"tempHistWriteAtHadWriteVec")
  //)
  //val tempHistWriteAtHadWriteRevVec = (
  //  KeepAttribute(
  //    Vec.fill(wordCount)(Bool())
  //  )
  //  .setName(s"tempHistWriteAtHadWriteRevVec")
  //)
  //for (ydx <- 0 until tempHistWriteAtHadWriteVec.size) {
  //  def tempFunc(
  //    x: Flow[UInt],
  //  ) = (
  //    tempHistWriteAtFunc(
  //      x=x,
  //      someMemAddr=U(s"${log2Up(wordCount)}'d${ydx}")
  //    )
  //  )
  //  val tempFindFirst = tempHistWriteAt.sFindFirst(
  //    tempFunc
  //  )
  //  //tempHistWroteAtFindFirst1(ydx) := tempFindFirst._1
  //  //tempHistWroteAtFindFirst2(ydx) := tempFindFirst._2
  //  tempHistWriteAtHadWriteVec(ydx) := tempFindFirst._1
  //  //val tempHistWriteAtRev = tempHistWriteAt.reverse

  //  //val tempRevFindFirst = tempHistWriteAtRev.sFindFirst(
  //  //  tempFunc
  //  //)
  //}
  val myDbgInitstate = (
    KeepAttribute(
      initstate()
    )
    .setName(s"myDbgInitstate")
  )
  assume(
    myDbgInitstate === initstate()
  )
  when (!pastValid()) {
    assert(
      //myDbgInitstate
      initstate()
    )
  }
  when (initstate()) {
    assert(
      !pastValid()
    )
    //assume(
    //  ClockDomain.isResetActive
    //)
  }
  val myHistReset = (
    KeepAttribute(
      History[Bool](
        that=ClockDomain.isResetActive,
        length=myProveNumCycles,
        init=False,
      )
    )
    .setName(s"myHistReset")
  )
  val tempHadReset = (
    myHistReset.sFindFirst(
      _ === True
    )
  )
  //val myHistNotReset = (
  //  KeepAttribute(
  //    History[Bool](
  //      that=(
  //        !ClockDomain.isResetActive
  //      ),
  //      length=myProveNumCycles,
  //      init=True,
  //    )
  //  )
  //  .setName(s"myHistReset")
  //)
  val tempHadNotReset = (
    myHistReset.sFindFirst(
      _ === False
    )
  )
  val myResetCond = (
    //KeepAttribute(
    //  tempHadReset._1
    //  && tempHadNotReset._1
    //  //&& (
    //  //  tempHadNotReset._2 //+ 1
    //  //  === tempHadReset._2 + 1
    //  //)
    //)
    True
    .setName(
      s"myResetCond"
    )
  )
  //for (idx <- 0 until myProveNumCycles) {
  //  when (
  //    //myResetCond
  //    tempHadReset._1
  //    && (
  //      tempHadReset._2 === 0
  //    )
  //  ) {
  //    assume(
  //      reset
  //    )
  //  }
  //}
  val myHistFrontIsFiring = (
    KeepAttribute(
      History[Bool](
        that=(
          front.isFiring
          //True
          && !myDbgInitstate
          //&& (
          //  tempHadReset._1
          //  && tempHadNotReset._1
          //)
          && (
            myResetCond
          )
        ),
        length=myProveNumCycles,
        when=(
          front.isFiring
          //&& (
          //  myResetCond
          //)
        ),
        init=False
      )
    )
    .setName("myHistFrontIsFiring")
  )
  val tempHadFrontIsFiring = (
    myHistFrontIsFiring.sFindFirst(
      _ === True
    )
  )
  when (
    !tempHadFrontIsFiring._1
  ) {
    for (ydx <- 1 until myHistFrontIsFiring.size) {
      assume(
        myHistFrontIsFiring(ydx) === myHistFrontIsFiring(ydx).getZero
      )
    }
  }
  val myHistMid0FrontUpIsFiring = (
    KeepAttribute(
      History[Bool](
        that=(
          pipeMem.cMid0FrontArea.up.isFiring
          //&& tempHadFrontIsFiring._1
          && !myDbgInitstate
          //&& (
          //  tempHadReset._1
          //  && tempHadNotReset._1
          //)
          && (
            myResetCond
          )
        ),
        length=myProveNumCycles,
        when=(
          //pipeMem.mod.front.cMid0Front.up.isFiring
          //&& 
          tempHadFrontIsFiring._1
          //&& (
          //  myResetCond
          //)
        ),
        init=False
      )
      .setName(s"myHistMid0FrontUpIsFiring")
    )
  )
  val tempHadMid0FrontUpIsFiring = (
    myHistMid0FrontUpIsFiring.sFindFirst(
      _ === True
    )
  )
  when (pastValidAfterReset) {
    when (!tempHadMid0FrontUpIsFiring._1) {
      for (ydx <- 1 until myHistMid0FrontUpIsFiring.size) {
        assume(
          myHistMid0FrontUpIsFiring(ydx)
          === myHistMid0FrontUpIsFiring(ydx).getZero
        )
      }
      assume(
        pipeMem.cMid0FrontArea.tempUpMod(0)(0).opCnt === 0x0
      )
    }
  }
  val myHistModFrontIsFiring = (
    KeepAttribute(
      History[Bool](
        that=(
          modFront.isFiring
          //&& tempHadMid0FrontUpIsFiring._1
          && !myDbgInitstate
          //&& (
          //  //tempHadReset._1
          //  //&& tempHadNotReset._1
          //  //&& (
          //  //  tempHadNotReset._2
          //  //  > tempHadReset._2
          //  //)
          //  myResetCond
          //)
          && (
            myResetCond
          )
        ),
        length=myProveNumCycles,
        when=(
          //modFront.isFiring
          //&& 
          tempHadMid0FrontUpIsFiring._1
          //&& (
          //  //tempHadReset._1
          //  //&& tempHadNotReset._1
          //  //&& (
          //  //  tempHadNotReset._2
          //  //  > tempHadReset._2
          //  //)
          //  myResetCond
          //)
        ),
        init=False
      )
    )
    .setName(s"myHistModFrontIsFiring")
  )
  val tempHadModFrontIsFiring = (
    myHistModFrontIsFiring.sFindFirst(
      _ === True
    )
  )
  when (!tempHadModFrontIsFiring._1) {
    for (ydx <- 1 until myHistModFrontIsFiring.size) {
      assume(
        myHistModFrontIsFiring(ydx)
        === myHistModFrontIsFiring(ydx).getZero
      )
    }
  }
  val myHistHaveCurrWrite = (
    KeepAttribute(
      History[Bool](
        that=(
          //modBack.isFiring
          myHaveCurrWrite
          //&& tempHadModFrontIsFiring._1
          && !myDbgInitstate
          //&& (
          //  //tempHadReset._1
          //  //&& tempHadNotReset._1
          //  myResetCond
          //)
          && (
            myResetCond
          )
        ),
        length=myProveNumCycles,
        when=(
          //modBack.isFiring
          //myHaveCurrWrite
          //&& 
          tempHadModFrontIsFiring._1
          //&& (
          //  //tempHadReset._1
          //  //&& tempHadNotReset._1
          //  //&& (
          //  //  tempHadNotReset._2
          //  //  > tempHadReset._2
          //  //)
          //  myResetCond
          //)
        ),
        init=False
      )
    )
    .setName(s"myHistHaveCurrWrite")
  )
  val myHistMyWriteAddr = (
    KeepAttribute(
      History[UInt](
        that=(
          pipeMem.mod.back.myWriteAddr(0)
        ),
        length=myProveNumCycles,
        when=(
          tempHadModFrontIsFiring._1
        ),
        init=(
          U"1'd0".resized
        )
      )
    )
    .setName(s"myHistMyWriteAddr")
  )
  val myHistMyWriteData = (
    KeepAttribute(
      History[UInt](
        that=(
          pipeMem.mod.back.myWriteData(0)
        ),
        length=myProveNumCycles,
        when=(
          tempHadModFrontIsFiring._1
        ),
        init=(
          U"1'd0".resized
        )
      )
    )
    .setName(s"myHistMyWriteData")
  )
  when (!tempHadModFrontIsFiring._1) {
    for (ydx <- 1 until myHistHaveCurrWrite.size) {
      assume(
        myHistHaveCurrWrite(ydx) === False
      )
    }
  }
  val tempHaveCurrWrite = (
    myHistHaveCurrWrite.sFindFirst(
      _ === True
    )
  )
  val myHistHaveCurrWritePostFirst = (
    KeepAttribute(
      cloneOf(myHistHaveCurrWrite)
    )
    .setName(s"myHistHaveCurrWritePostFirst")
  )
  val myHistHaveCurrWritePostSecond = (
    KeepAttribute(
      cloneOf(myHistHaveCurrWrite)
    )
    .setName(s"myHistHaveCurrWritePostSecond")
  )
  //myHistHaveCurrWritePostFirst := myHistHaveCurrWrite
  for (ydx <- 0 until myHistHaveCurrWrite.size) {
    myHistHaveCurrWritePostFirst(ydx) := (
      myHistHaveCurrWritePostFirst(ydx).getZero
    )
    myHistHaveCurrWritePostSecond(ydx) := (
      myHistHaveCurrWritePostSecond(ydx).getZero
    )
  }
  when (
    tempHaveCurrWrite._1
  ) {
    switch (tempHaveCurrWrite._2) {
      for (ydx <- 0 until myHistHaveCurrWritePostFirst.size) {
        is (ydx) {
          for (zdx <- ydx + 1 until myHistHaveCurrWritePostFirst.size) {
            myHistHaveCurrWritePostFirst(zdx) := (
              myHistHaveCurrWrite(zdx)
            )
          }
        }
      }
    }
  }
  val tempHaveCurrWritePostFirst = (
    myHistHaveCurrWritePostFirst.sFindFirst(
      _ === True
    )
  )
  when (
    tempHaveCurrWritePostFirst._1
  ) {
    switch (tempHaveCurrWritePostFirst._2) {
      for (ydx <- 0 until myHistHaveCurrWritePostSecond.size) {
        is (ydx) {
          for (zdx <- ydx + 1 until myHistHaveCurrWritePostSecond.size) {
            myHistHaveCurrWritePostSecond(zdx) := (
              myHistHaveCurrWrite(zdx)
            )
          }
        }
      }
    }
  }
  val tempHaveCurrWritePostSecond = (
    myHistHaveCurrWritePostSecond.sFindFirst(
      _ === True
    )
  )
  val myHistModBackOpCnt = (
    KeepAttribute(
      History[UInt](
        that=(
          modBack(modBackPayload).opCnt
        ),
        length=myProveNumCycles,
        when=(
          //myHaveCurrWrite
          tempHaveCurrWrite._1
          //&& myHaveCurrWrite
          //&& !myDbgInitstate
          //&& (
          //  myResetCond
          //)
        ),
        init=U(s"${PipeMemRmwSimDut.modOpCntWidth}'d0"),
      )
    )
    .setName(s"myHistModBackOpCnt")
  )
  when (
    !(
      tempHaveCurrWrite._1
      //&& myHaveCurrWrite

      //&& !myDbgInitstate
    )
    //|| (
    //  myDbgInitstate
    //)
  ) {
    for (ydx <- 1 until myHistModBackOpCnt.size) {
      assume(
        myHistModBackOpCnt(ydx) === myHistModBackOpCnt(ydx).getZero
      )
    }
  }

  when (pastValidAfterReset) {
    //when (past(pipeMem.mod.back.myWriteEnable(0))) {
    //  assume(
    //    pipeMem.modMem(0)(0).readAsync(
    //      address=past(pipeMem.mod.back.myWriteAddr(0))
    //    ) === (
    //      past(pipeMem.mod.back.myWriteData(0))
    //    )
    //  )
    //}
    //def mySavedMod = (
    //  rSavedModArr(
    //    modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx)
    //    //RegNextWhen(
    //    //  modFront(modFrontPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
    //    //  modFront.isFiring
    //    //)
    //  )
    //)
    //when (
    //  ////modFront.isFiring
    //  ////modBack.isFiring
    //  ////modFront.isFiring
    //  ////&& (
    //  ////  RegNextWhen(True, back.isFiring) init(False)
    //  ////)
    //  ////(
    //  ////  RegNextWhen(
    //  ////    pipeMem.mod.back.myWriteEnable,
    //  ////    modBack.isFiring
    //  ////  ) init(False)
    //  ////) && (
    //  ////  back.isFiring
    //  ////)
    //  //pipeMem.mod.back.myWriteEnable
    //  //&& modBack.isFiring
    //  ////&& back.isReady
    //  //True
    //  modFront.isFiring
    //  //&& modBack.isReady
    //) {
    //  // := (
    //  //  //modBack.myExt.rdMemWord(PipeMemRmw.modWrIdx)
    //  //)
    //  def mySavedMod = (
    //    rSavedModArr(
    //      //modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx)
    //      //modFront
    //      RegNextWhen(
    //        modFront(modFrontPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
    //        modFront.isFiring
    //      )
    //    )
    //  )
    //  mySavedMod := mySavedMod + 1
    //  //when (
    //  //  RegNextWhen(True, back.isFiring) init(False)
    //  //) {
    //  //  assert(
    //  //    (
    //  //      modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx)
    //  //    ) === (
    //  //      tempMemAddrPlus
    //  //    )
    //  //  )
    //  //}
    //}
    //--------
    //val myHadPastWriteVec = (
    //  KeepAttribute(
    //    Vec.fill(
    //      PipeMemRmwFormal.myProveNumCycles - 1
    //    )(
    //      Bool()
    //    )
    //  )
    //  .setName(s"myHadPastWriteVec")
    //)
    //val nextTempCnt = (
    //  KeepAttribute(
    //    UInt(log2Up(myHadPastWriteVec.size) bits)
    //  )
    //  .setName("dbg_nextTempCnt")
    //)
    //val rTempCnt = (
    //  KeepAttribute(
    //    RegNext(nextTempCnt)
    //    init(0x0)
    //  )
    //  .setName("dbg_rTempCnt")
    //)
    ////for (zdx <- 0 until myHadPastWriteVec.size) {
    ////  myHadPastWriteVec(zdx) := (
    ////    RegNext(myHadPastWriteVec(zdx))
    ////    init(False)
    ////  )
    ////}
    ////rTempCnt := rTempCnt + 1
    //val myHadPastWriteFindFirst = (
    //  myHadPastWriteVec.sFindFirst(
    //    _ === True
    //  )
    //)
    //val dbg_myHadPastWriteFindFirst_1 = (
    //  KeepAttribute(
    //    myHadPastWriteFindFirst._1
    //  )
    //  .setName(s"dbg_myHadPastWriteFindFirst_1")
    //)
    //val dbg_myHadPastWriteFindFirst_2 = (
    //  KeepAttribute(
    //    myHadPastWriteFindFirst._2
    //  )
    //  .setName(s"dbg_myHadPastWriteFindFirst_2")
    //)
    //val myHadPastWritePostFirstVec = (
    //  KeepAttribute(
    //    Vec.fill(
    //      myHadPastWriteVec.size
    //    )(
    //      Bool()
    //    )
    //  )
    //  .setName(s"myHadPastWritePostFirstVec")
    //)
    //val myHadPastWritePostFirstFindFirst = (
    //  myHadPastWritePostFirstVec.sFindFirst(
    //    _ === True
    //  )
    //)
    //val dbg_myHadPastWritePostFirstFindFirst_1 = (
    //  KeepAttribute(
    //    myHadPastWritePostFirstFindFirst._1
    //  )
    //  .setName(s"dbg_myHadPastWritePostFirstFindFirst_1")
    //)
    //val dbg_myHadPastWritePostFirstFindFirst_2 = (
    //  KeepAttribute(
    //    myHadPastWritePostFirstFindFirst._2
    //  )
    //  .setName(s"dbg_myHadPastWritePostFirstFindFirst_2")
    //)
    //when (pastValidAfterReset) {
    //  //rTempCnt := rTempCnt + 1
    //  //assume(RegNext(rTempCnt) === rTempCnt + 1)
    //  when (rTempCnt + 1 < myHadPastWriteVec.size) {
    //    assume(nextTempCnt === rTempCnt + 1)
    //    //myHadPastWriteVec(rTempCnt) := (
    //    //  past(myHaveCurrWrite) init(False)
    //    //)
    //    for (idx <- 0 until myHadPastWriteVec.size) {
    //      when (rTempCnt === idx) {
    //        assume(
    //          myHadPastWriteVec(idx)
    //          === (
    //            past(myHaveCurrWrite) init(False)
    //          )
    //        )
    //      } otherwise {
    //        assume(
    //          myHadPastWriteVec(idx)
    //          === (
    //            RegNext(myHadPastWriteVec(idx)) init(False)
    //          )
    //        )
    //      }
    //    }
    //    //--------
    //    when (
    //      myHadPastWriteFindFirst._1
    //      && (
    //        myHadPastWriteFindFirst._2
    //        === rTempCnt
    //      )
    //      //&& !myHadPastWritePostFirstFindFirst._1
    //    ) {
    //      //assume(
    //      //)
    //      assume(
    //        myHadPastWritePostFirstVec(rTempCnt)
    //        === False
    //      )
    //    } otherwise {
    //      assume(
    //        myHadPastWritePostFirstVec(rTempCnt)
    //        === (
    //          past(myHaveCurrWrite) init(False)
    //        )
    //      )
    //    }
    //    //--------
    //    cover(
    //      myHadPastWriteVec(rTempCnt)
    //    )
    //    //--------
    //  } otherwise {
    //    for (zdx <- 0 until myHadPastWriteVec.size) {
    //      assume(
    //        myHadPastWriteVec(zdx) 
    //        === (
    //          RegNext(myHadPastWriteVec(zdx)) init(False)
    //        )
    //      )
    //      assume(
    //        myHadPastWritePostFirstVec(zdx)
    //        === (
    //          RegNext(myHadPastWritePostFirstVec(zdx)) init(False)
    //        )
    //      )
    //    }
    //    assume(nextTempCnt === rTempCnt)
    //  }
    //} otherwise {
    //  for (zdx <- 0 until myHadPastWriteVec.size) {
    //    //assume(
    //    //  myHadPastWriteVec(zdx) === False
    //    //)
    //    assume(
    //      myHadPastWriteVec(zdx) 
    //      === (
    //        RegNext(myHadPastWriteVec(zdx)) init(False)
    //      )
    //    )
    //    assume(
    //      myHadPastWritePostFirstVec(zdx)
    //      === (
    //        RegNext(myHadPastWritePostFirstVec(zdx)) init(False)
    //      )
    //    )
    //  }
    //  assume(nextTempCnt === rTempCnt)
    //}
    //for (zdx <- 0 until myHadPastWritePostFirstVec.size) {
    //  //myHadPastWritePostFirstVec(zdx) := (
    //  //  RegNext(myHadPastWritePostFirstVec(zdx))
    //  //)
    //  when (pastValidAfterReset) {
    //    when (myHadPastWriteFindFirst._1) {
    //      when (zdx > myHadPastWriteFindFirst._2) {
    //        myHadPastWritePostFirstVec(zdx) := (
    //          myHadPastWriteVec(zdx)
    //        )
    //      } otherwise {
    //        myHadPastWritePostFirstVec(zdx) := False
    //      }
    //    } otherwise {
    //      myHadPastWritePostFirstVec(zdx) := False
    //    }
    //  }
    //}
    //--------
    //def mySavedMod = (
    //  //rSavedModArr
    //  pipeMem.modMem(0)(0).readSync(
    //    address=(
    //      modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx)
    //    )
    //    //RegNextWhen(
    //    //  modFront(modFrontPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
    //    //  modFront.isFiring
    //    //)
    //  )
    //)
    val tempRight = (
      ////RegNext(
      //  rSavedModArr(
      //    modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx)
      //  )
      ////)
      //mySavedMod
      //--------
      KeepAttribute(
        //RegNextWhen(
        //  pipeMem.modMem(0)(0).readAsync(
        //    address=(
        //      //RegNextWhen(
        //        //modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx)
        //        //pipeMem.cBackArea.upExt(1)(0)(
        //        //  PipeMemRmw.extIdxSingle
        //        //).memAddr(0),
        //        pipeMem.mod.back.myWriteAddr(0)
        //      //  modBack.isFiring,
        //      //) init(0x0)
        //    )
        //    //RegNextWhen(
        //    //  modFront(modFrontPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
        //    //  modFront.isFiring
        //    //)
        //  ),
        //  //modBack.isFiring
        //  myHaveCurrWrite
        //) init(rSavedModArr(0).getZero)
        /*RegNextWhen*//*RegNext*/(
          myHistMyWriteData(
            //tempHaveCurrWritePostFirst._2
            /*RegNext*/(tempHaveCurrWritePostFirst._2)
          ),
          //myHaveCurrWrite
        )
        //init(pipeMem.mod.back.myWriteData(0).getZero)
      )
      .setName(s"tempRight")
      //+ 1
      //+ (
      //  if (PipeMemRmwSimDut.doTestModOp) (
      //    Mux(
      //      //modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx)
      //      //  === 0x1,
      //      modBack(modBackPayload).op
      //        === PipeMemRmwSimDut.ModOp.LDR_RA_RB,
      //      U"1'd1",
      //      U"1'd0",
      //    )
      //  ) else (
      //    0
      //  )
      //)
    )
    val rPrevOpCnt = (
      RegNextWhen(
        modBack(modBackPayload).opCnt,
        //modBack.isFiring && pipeMem.mod.back.myWriteEnable(0)
        myHaveCurrWrite
      )
      init(0x0)
    )
      .setName("rPrevOpCnt")
    assumeInitial(
      rPrevOpCnt === 0x0
    )
    //when (pastValidAfterReset) {
    //  when (
    //    (
    //      modBack.isFiring
    //      || (
    //        RegNextWhen(True, modBack.isFiring) init(False)
    //      )
    //    ) && (
    //      !past(pipeMem.mod.back.myWriteEnable(0))
    //    )
    //  ) {
    //    //assert(stable(rPrevOpCnt))
    //    for (idx <- 0 until rSavedModArr.size) {
    //      assume(stable(rSavedModArr(idx)))
    //    }
    //  }
    //}
    //when (
    //  past(modBack.isFiring)
    //  && past(pipeMem.mod.back.myWriteEnable)
    //) {
    //  assert(
    //    RegNext(modBack(modBackPayload).opCnt)
    //    === rPrevOpCnt
    //  )
    //}
    val myCoverCond = (
      //modBack.isFiring
      //&& pipeMem.mod.back.myWriteEnable(0)
      myHaveCurrWrite
    )
    def myCoverVecSize = 8
    //val rMyCoverVec = (
    //  Vec.fill(myCoverVecSize)(
    //    Reg(/*Flow*/(PipeMemRmwSimDutModType()))
    //  )
    //)
    val tempMyCoverInit = PipeMemRmwSimDutModType()
    tempMyCoverInit.allowOverride
    tempMyCoverInit := tempMyCoverInit.getZero
    tempMyCoverInit.op := PipeMemRmwSimDut.ModOp.LIM
    val rMyCoverVec = (
      KeepAttribute(
        History(
          that=modBack(modBackPayload),
          length=myCoverVecSize,
          when=myCoverCond,
          init=tempMyCoverInit,
        )
      )
    )
    //for (idx <- 0 until rMyCoverVec.size) {
    //  val tempMyCoverInit = PipeMemRmwSimDutModType()
    //  tempMyCoverInit.allowOverride
    //  tempMyCoverInit := tempMyCoverInit.getZero
    //  tempMyCoverInit.op := PipeMemRmwSimDut.ModOp.LIM
    //  rMyCoverVec(idx).init(
    //    //rMyCoverVec
    //    //PipeMemRmwSimDut.ModOp.LIM
    //    //rMyCoverVec(idx).getZero
    //    tempMyCoverInit
    //  )
    //  when (myCoverCond) {
    //    if (idx == 0) {
    //      rMyCoverVec(idx) := modBack(modBackPayload)
    //    } else {
    //      rMyCoverVec(idx) := rMyCoverVec(idx - 1)
    //    }
    //  }
    //}
    when (myHaveCurrWrite) {
      cover(
        (
          rMyCoverVec(0).op === PipeMemRmwSimDut.ModOp.ADD_RA_RB
        ) && (
          rMyCoverVec(1).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& !rMyCoverVec(1).dcacheHit
        ) && (
          rMyCoverVec(2).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& !rMyCoverVec(2).dcacheHit
        )
      )
      cover(
        (
          rMyCoverVec(0).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& !rMyCoverVec(0).dcacheHit
        ) && (
          rMyCoverVec(1).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& rMyCoverVec(1).dcacheHit
        ) && (
          rMyCoverVec(2).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& !rMyCoverVec(2).dcacheHit
        )
      )
      //cover(
      //  (
      //    rMyCoverVec(0).op === PipeMemRmwSimDut.ModOp.MUL_RA_RB
      //    //&& !rMyCoverVec(0).dcacheHit
      //  ) && (
      //    rMyCoverVec(1).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
      //    //&& !rMyCoverVec(1).dcacheHit
      //  ) && (
      //    rMyCoverVec(2).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
      //    //&& rMyCoverVec(2).dcacheHit
      //  )
      //)
      //cover(
      //  (
      //    rMyCoverVec(0).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
      //    //&& !rMyCoverVec(0).dcacheHit
      //  ) && (
      //    rMyCoverVec(1).op === PipeMemRmwSimDut.ModOp.MUL_RA_RB
      //    //&& !rMyCoverVec(0).dcacheHit
      //  ) && (
      //    rMyCoverVec(2).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
      //    //&& !rMyCoverVec(2).dcacheHit
      //  )
      //)
    }
    //cover(
    //  RegNextWhen
    //  myCoverCond
    //  && 
    //)
    //def hadWriteCntWidth = log2Up(32)
    //val rHadWriteCntVec = (
    //  Reg(
    //    Vec.fill(1 << hadWriteCntWidth)(
    //      UInt(hadWriteCntWidth bits)
    //    )
    //  )
    //  //init(0x0)
    //)
    //val rMyDidHaveFirstWrite = (
    //  Reg(Bool())
    //)
    //assumeInitial(
    //  rMyDidHaveFirstWrite === False
    //)

    //when (
    //  //myHaveFirstWrite
    //  initstate()
    //) {
    //  rMyDidHaveFirstWrite.init(False)
    //  //when (!rMyDidHaveFirstWrite) {
    //  //  rMyDidHaveFirstWrite := (
    //  //    True
    //  //  )
    //  //}
    //}

    //val rMyDidHaveFirstWrite = (
    //  KeepAttribute(
    //    Reg(Bool()) init(False)
    //  )
    //)

    //for (zdx <- 0 until rHadWriteCntVec.size) {
    //  rHadWriteCntVec.init(rHadWriteCntVec(zdx).getZero)
    //  when (myHadWriteCntCond) {
    //    if (zdx == 0) {
    //      rHadWriteCntVec(zdx) := (
    //        rHadWriteCntVec(zdx) + 1
    //      )
    //    } else {
    //      rHadWriteCntVec(zdx) := rHadWriteCntVec(zdx - 1)
    //    }
    //  } otherwise {
    //    assume(stable(rHadWriteCntVec(zdx)))
    //  }
    //  assumeInitial(
    //    rHadWriteCntVec(zdx) === rHadWriteCntVec(zdx).getZero
    //  )
    //}
    //val nextDidFirstOpCntInc = (
    //  KeepAttribute(
    //    Bool()
    //  )
    //  .setName("nextDidFirstOpCntInc")
    //)
    //val rDidFirstOpCntInc = (
    //  KeepAttribute(
    //    RegNext(
    //      //Bool()
    //      nextDidFirstOpCntInc
    //    )
    //    init(False)
    //  )
    //  .setName("rDidFirstOpCntInc")
    //)
    when (pastValidAfterReset()) {
      when (/*past*/(myHaveCurrWrite)) {
        //when (
        //  //modBack.isValid
        //  //&& myHaveCurrWrite
        //  //tempHadFrontIsFiring._1
        //  //&& tempHadMid0FrontUpIsFiring._1
        //  //tempHaveCurrWrite._1
        //  //&& myHistHaveCurrWrite
        //  tempHaveCurrWrite._1
        //  && tempHaveCurrWritePostFirst._1
        //  //&& (
        //  //  tempHadReset._1
        //  //)
        //  //&& (
        //  //  tempHadReset._1
        //  //  && tempHadNotReset._1
        //  //)
        //) {
        //  assert(
        //    ////myHistModBackOpCnt(0) + 1 === myHistModBackOpCnt(1)
        //    ///*RegNext*/(myHistModBackOpCnt(0))
        //    //=== /*RegNext*/(myHistModBackOpCnt(1)) + 1
        //    ////modBack(modBackPayload).opCnt
        //    ////=== myHistModBackOpCnt(0) + 1
        //    myHistModBackOpCnt(tempHaveCurrWrite._2)
        //    === myHistModBackOpCnt(tempHaveCurrWritePostFirst._2) + 1
        //  )
        //}
        //when (
        //  myHaveCurrWrite
        //  && (
        //    RegNextWhen(
        //      myHaveCurrWrite,
        //      myHaveCurrWrite
        //    )
        //    init(False)
        //  )
        //) {
        //  assert
        //}
      }
      //when (myHadPastWriteFindFirst._1) {
      //}
      //when (myHaveFirstWrite) {
      //}
      //when (
      //  myHadWriteCond
      //  && !past(myHadWriteCntCond)
      //) {
      //}
      //when (
      //  //myHaveCurrWrite
      //  //myHadWriteCntCond
      //  //RegNextWhen(
      //  //  True,
      //  //  modBack.isFiring && pipeMem.mod.back.myWriteEnable(0)
      //  //) init(False)
      //  //&&
      //  //past(modBack.isFiring)
      //  //&& past(pipeMem.mod.back.myWriteEnable(0))
      //  myHadPastWriteFindFirst._1
      //) {
      //  //assume(rDidFirstOpCntInc === True)
      //  //rDidFirstOpCntInc := (
      //  //  myHadPastWritePostFirstFindFirst._1
      //  //)
      //  assume(
      //    nextDidFirstOpCntInc === myHadPastWritePostFirstFindFirst._1
      //  )
      //  when (
      //    past(myHaveCurrWrite) init(False)
      //  ) {
      //    assert(
      //      rPrevOpCnt
      //      === past(modBack(modBackPayload).opCnt)
      //    )
      //    //cover(rDidFirstOpCntInc)
      //    //when (
      //    //  rDidFirstOpCntInc
      //    //) {
      //    //  assert(
      //    //    modBack(modBackPayload).opCnt
      //    //    === rPrevOpCnt + 1
      //    //  )
      //    //  //assert(
      //    //  //  rPrevOpCnt === past(rPrevOpCnt) + 1
      //    //  //)
      //    //  //cover(
      //    //  //  rPrevOpCnt === past(rPrevOpCnt) + 1
      //    //  //)
      //    //}
      //  }
      //  when (rDidFirstOpCntInc) {
      //    when (past(myHaveCurrWrite)) {
      //      when (modBack.isValid) {
      //        assert(
      //          modBack(modBackPayload).opCnt
      //          === rPrevOpCnt + 1
      //        )
      //      }
      //    }
      //    //assert(
      //    //  rPrevOpCnt === past(rPrevOpCnt) + 1
      //    //)
      //    //cover(
      //    //  rPrevOpCnt === past(rPrevOpCnt) + 1
      //    //)
      //  }
      //  //switch (myHadPastWriteFindFirst._2) {
      //  //  //assert(
      //  //  //  //past(modBack(modBackPayload).opCnt)
      //  //  //  //=== rPrevOpCnt //past(rPrevOpCnt) + 1
      //  //  //  //rPrevOpCnt === past
      //  //  //)
      //  //  for (zdx <- 0 until myHadPastWriteVec.size) {
      //  //    is (zdx) {
      //  //      assert(
      //  //      )
      //  //    }
      //  //  }
      //  //}
      //  //when (modBack.isValid) {
      //  //  assert(
      //  //    //past(rPrevOpCnt) + 1 === rPrevOpCnt
      //  //    modBack(modBackPayload).opCnt
      //  //    === (
      //  //      RegNextWhen(
      //  //        modBack(modBackPayload).opCnt,
      //  //        (modBack.isFiring && pipeMem.mod.back.myWriteEnable(0))
      //  //      ) + 1
      //  //    )
      //  //  )
      //  //}
      //  //assert(
      //  //  rPrevOpCnt === past(modBack(modBackPayload).opCnt) - 1
      //  //  //RegNext(modBack(modBackPayload).opCnt)
      //  //  //=== rPrevOpCnt + 1
      //  //)
      //} otherwise {
      //  assume(
      //    nextDidFirstOpCntInc === False
      //  )
      //  //rDidFirstOpCntInc := False
      //  //assume(
      //  //  RegNext(rDidFirstOpCntInc) === False
      //  //)
      //  //assume(
      //  //)
      //}
      //when (
      //  past(myHadPastWriteFindFirst._1)
      //  && past(myHadPastWritePostFirstFindFirst._1)
      //) {
      //  switch (past(myHadPastWriteFindFirst._2)) {
      //    for (zdx <- 0 until myHadPastWritePostFirstVec.size) {
      //      is (zdx) {
      //        //if (zdx != 0) {
      //          assert(
      //            rPrevOpCnt
      //            === (
      //              past(rPrevOpCnt, zdx + 1) init(0x0)
      //            ) + 1
      //          )
      //        //}
      //      }
      //    }
      //  }
      //}
      when (
        ///*past*/(modBack.isFiring)
        //&& /*past*/(pipeMem.mod.back.myWriteEnable(0))
        /*past*/(myHaveCurrWrite)
        //pipeMem.mod.back.myWriteEnable(0)
      ) {
        //def mySavedMod = (
        //  rSavedModArr(
        //    modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx)
        //    //RegNextWhen(
        //    //  modFront(modFrontPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
        //    //  modFront.isFiring
        //    //)
        //  )
        //)

        //for (
        //  zdx <- 0 until PipeMemRmwSimDut.modRdPortCnt
        //) {
        //  assert(
        //    rSavedModArr(modBack(modBackPayload).myExt.memAddr(zdx))
        //    === modBack(modBackPayload).myExt.rdMemWord(zdx)
        //  )
        //}
        //val temp = History(
        //)
        //val myTempHistWriteAt 
        when (
          //pipeMem.cBackArea.up.isValid
          //tempHistWriteAtHadWriteVec(
          //  past(
          //    pipeMem.cBackArea.upExt(1)(0)(
          //      PipeMemRmw.extIdxSingle
          //    ).memAddr(0),
          //  )
          //)
          //&& 
          (
            //myTempHistWriteAtFindFirst._1
            //myTempHistWriteAtPostFirstFindFirst._1
            /*past*/(tempHaveCurrWrite._1)
            && /*past*/(tempHaveCurrWritePostFirst._1)
            //&& tempHaveCurrWritePostSecond._1
          ) && (
            (
              /*past*/(
                myHistMyWriteAddr(/*past*/(tempHaveCurrWrite._2))
              ) === /*past*/(
                myHistMyWriteAddr(/*past*/(tempHaveCurrWritePostFirst._2))
              )
            ) 
            //&& (
            //  myHistMyWriteAddr(tempHaveCurrWritePostFirst._2)
            //  === myHistMyWriteAddr(tempHaveCurrWritePostSecond._2)
            //)
          ) && (
            //tempHadReset._1
            //&& tempHadNotReset._1
            True
          )
          //&& (
          //  myTempHistWriteAtRevFindFirst._1
          //) && (
          //  myTempHistWriteAtFindFirst._2
          //  =/= myTempHistWriteAtRevFindFirst._2
          //)
        ) {
          //when (tempLeft =/= tempRight) {
            assert(
              /*past*/(tempLeft) //+ 1
              === (
                //mySavedMod
                /*past*//*past*/(tempRight) + 1 //+ 1
                /*past*///(mySavedMod)
                //mySavedMod + 1
              )
            )
          //}
        }
      }
    } otherwise {
      //assume(
      //  RegNext(rDidFirstOpCntInc) === False
      //)
      //assume(
      //  nextDidFirstOpCntInc === False
      //)
    }
    //when (
    //  ////(
    //  ////  RegNextWhen(True, modBack.isFiring) init(False)
    //  ////) && 
    //  //(
    //  //  RegNextWhen(True, back.isFiring) init(False)
    //  //) && (
    //  //  //back.isValid
    //  //  True
    //  //)
    //  //back.isFiring
    //  //back.isValid
    //  //modBack.isValid
    //  //modBack.isFiring
    //  ////modBack.isReady
    //  //&& pipeMem.mod.back.myWriteEnable(0)
    //  myHaveCurrWrite
    //  //pipeMem.mod.back.myWriteEnable(0)
    //) {
    //  mySavedMod := tempRight
    //}
    //when (initstate()) {
    //  for (idx <- 0 until rSavedModArr.size) {
    //    assume(
    //      rSavedModArr(idx)
    //      === (
    //        pipeMem.modMem(0)(0).readAsync(
    //          address=idx
    //        )
    //      )
    //    )
    //  }
    //}
    cover(
      back.isFiring
      && (
        back(backPayload).myExt.modMemWord === 0x3
      ) && (
        back(backPayload).myExt.memAddr(PipeMemRmw.modWrIdx) === 0x2
      ) 
      //&& (
      //  RegNextWhen(
      //    (
      //      RegNextWhen(
      //        True,
      //        back.isFiring,
      //      ) init(False)
      //    ),
      //    back.isFiring,
      //  ) init(False)
      //)
    )
  }
  //if (modStageCnt > 0) {
  //  pipeMem.io.midModStages := io.midModStages
  //}
  //pipeMem.io.front.driveFrom(io.front)(
  //  con=(node, payload) => {
  //    node(pipeMem.io.frontPayload(0)) := payload
  //  }
  //)
  //pipeMem.io.modFront.driveTo(io.modFront)(
  //  con=(payload, node) => {
  //    payload := node(pipeMem.io.modFrontPayload(0))
  //  }
  //)
  //pipeMem.io.modBack.driveFrom(io.modBack)(
  //  con=(node, payload) => {
  //    node(pipeMem.io.modBackPayload(0)) := payload
  //  }
  //)
  //pipeMem.io.back.driveTo(io.back)(
  //  con=(payload, node) => {
  //    payload := node(pipeMem.io.backPayload(0))
  //  }
  //)
  //Builder(pipeMem.myLinkArr)
}
case class PipeMemRmwTester() extends Component {
  def doFormal: Boolean = {
    GenerationFlags.formal {
      return true
    }
    return false
  }
  //assert(doFormal)
  def wordWidth = PipeMemRmwSimDut.wordWidth
  def wordType() = PipeMemRmwSimDut.wordType()
  def wordCount = PipeMemRmwSimDut.wordCount
  def hazardCmpType() = PipeMemRmwSimDut.hazardCmpType()
  def modRdPortCnt = (
    PipeMemRmwSimDut.modRdPortCnt
  )
  def modStageCnt = (
    //2
    //1
    //0
    PipeMemRmwSimDut.modStageCnt
  )
  def optModHazardKind = (
    PipeMemRmwSimDut.optModHazardKind
  )
  def modType() = PipeMemRmwSimDutModType()
  val io = new Bundle {
    //val front = (
    //  PipeMemRmwSimDut.doRandFront
    //  || doFormal
    //) generate (
    //  slave(Stream(modType()))
    //)
    val front = slave(Stream(modType()))
    val back = master(Stream(modType()))
  }
  val dut: PipeMemRmwSimDut = PipeMemRmwSimDut()
  def pipeMem = dut.pipeMem
  def pmIo = pipeMem.io
  def frontPayload = dut.frontPayload
  def modFrontPayload = dut.modFrontPayload
  def modBackPayload = dut.modBackPayload
  def backPayload = dut.backPayload
  assume(
    pmIo.modFront(modFrontPayload).op.asBits.asUInt
    //< PipeMemRmwSimDut.ModOp.MUL_RA_RB.asBits.asUInt
    < PipeMemRmwSimDut.postMaxModOp.asBits.asUInt
  )
  assume(
    pmIo.modBack(modBackPayload).op.asBits.asUInt
    //< PipeMemRmwSimDut.ModOp.MUL_RA_RB.asBits.asUInt
    < PipeMemRmwSimDut.postMaxModOp.asBits.asUInt
  )

  //def memAddrFracWidth = (
  //  0
  //  //1
  //)
  //val rMemAddr = (
  //  //!PipeMemRmwSimDut.doRandFront
  //  !doFormal
  //) generate (
  //  Reg(
  //    UInt(
  //      (
  //        pmIo.front(frontPayload).myExt.memAddr(0).getWidth
  //        + memAddrFracWidth
  //      ) bits
  //    )
  //  ) init(0x0)
  //)
  //def memAddrFracRange = memAddrFracWidth - 1 downto 0
  //def memAddrIntRange = rMemAddr.high downto memAddrFracWidth

  //def incMemAddr(
  //  outpMemAddr: Vec[UInt],
  //): Unit = {
  //  when (
  //    //Cat(False, rMemAddr).asUInt + 1
  //    rMemAddr
  //    === (
  //      PipeMemRmwSimDut.memAddrPlusAmount
  //    ) - 1
  //  ) {
  //    rMemAddr := 0x0
  //  } otherwise {
  //    rMemAddr := rMemAddr + 1
  //  }
  //  for (zdx <- 0 until outpMemAddr.size) {
  //    outpMemAddr(zdx) := (rMemAddr + zdx) >> memAddrFracWidth
  //  }
  //}

  //if (
  //  PipeMemRmwSimDut.doRandFront
  //  && !doFormal
  //) {
  //  //io.front.translateInto(dut.io.front)(
  //  //  dataAssignment=(outp, inp) => {
  //  //    //outp.myExt.memAddr := rMemAddr
  //  //    outp.myExt := inp.myExt
  //  //    outp.myExt.allowOverride
  //  //    when (io.front.fire) {
  //  //      incMemAddr(outpMemAddr=outp.myExt.memAddr)
  //  //    }
  //  //  }
  //  //)
  //  pmIo.front.driveFrom(io.front)(
  //    con=(node, payload) => {
  //      ////outp.myExt.memAddr := rMemAddr
  //      //outp.myExt := inp.myExt
  //      //outp.myExt.allowOverride
  //      //when (io.front.fire) {
  //      //  incMemAddr(outpMemAddr=outp.myExt.memAddr)
  //      //}
  //    }
  //  )
  //  //dut.io.front << io.front
  //}
  //GenerationFlags.formal {
  //  //dut.io.front << io.front
  //}

  pmIo.front.driveFrom(io.front)(
    con=(node, myFrontPayload) => {
      node(frontPayload) := myFrontPayload
    }
  )

  pmIo.back.driveTo(io.back)(
    con=(myBackPayload, node) => {
      myBackPayload := node(backPayload)
    }
  )
  //io.back << dut.io.back
  //def back = dut.io.back



  //val nextFrontValid = (
  //  !PipeMemRmwSimDut.doRandFront
  //  && !doFormal
  //) generate (
  //  Bool()
  //)
  //val rFrontValid = (
  //  !PipeMemRmwSimDut.doRandFront
  //  && !doFormal
  //) generate (
  //  RegNext(nextFrontValid) init(nextFrontValid.getZero)
  //)

  //if (
  //  !PipeMemRmwSimDut.doRandFront
  //  && !doFormal
  //) {
  //  //front.valid := True
  //  //nextFrontValid := !rFrontValid
  //  nextFrontValid := True
  //  front.valid := nextFrontValid

  //  //def front = dut.io.front
  //  front.payload := (
  //    RegNext(front.payload) init(front.payload.getZero)
  //  )
  //  front.myExt.allowOverride
  //  for (zdx <- 0 until modRdPortCnt) {
  //    front.myExt.memAddr(zdx) := (rMemAddr + zdx) >> memAddrFracWidth
  //  }
  //  when (front.fire) {
  //    incMemAddr(outpMemAddr=front.myExt.memAddr)
  //    //rMemAddr := rMemAddr + 1
  //    //rMemAddr(0 downto 0) := rMemAddr(0 downto 0) + 1
  //    //rMemAddr(1 downto 0) := rMemAddr(1 downto 0) + 1
  //    //when (rMemAddr(memAddrFracRange) === 2) {
  //    //  rMemAddr(memAddrIntRange) := (
  //    //    rMemAddr(memAddrIntRange) + 1
  //    //  )
  //    //  rMemAddr(memAddrFracRange) := 0
  //    //}
  //  }
  //}
  //back.ready := True
  //--------

  //val modFrontStm = Stream(modType())
  //val modMidStm = Stream(modType())
  //val modBackStm = Stream(modType())
  ////modFrontStm <-/< dut.io.modFront
  //modFrontStm << dut.io.modFront
  def extIdxUp = PipeMemRmw.extIdxUp
  def extIdxSaved = PipeMemRmw.extIdxSaved
  def extIdxLim = PipeMemRmw.extIdxLim
  if (
    optModHazardKind == PipeMemRmw.modHazardKindDupl
    || (
      optModHazardKind == PipeMemRmw.modHazardKindFwd
      && modStageCnt > 0
    )
  ) {
    //if (optModHazardKind == PipeMemRmw.modHazardKindFwd) {
      assert(modStageCnt == 1)
    //}
    val cMidModFront = CtrlLink(
      up=pmIo.modFront,
      down=Node(),
    )
    pipeMem.myLinkArr += cMidModFront
    val sMidModFront = StageLink(
      up=cMidModFront.down,
      down=(
        //pmIo.modBack
        //Node()

        pmIo.modBack
      ),
    )
    pipeMem.myLinkArr += sMidModFront
    //val s2mMidModFront = S2MLink(
    //  up=sMidModFront.down,
    //  down=(
    //    pmIo.modBack
    //    //Node()
    //  ),
    //)
    //pipeMem.myLinkArr += s2mMidModFront

    //val cMidModBack = CtrlLink(
    //  up=(
    //    //pmIo.modBack
    //    s2mMidModFront.down
    //  ),
    //  down=Node(),
    //)
    //pipeMem.myLinkArr += cMidModBack
    //val sMidModBack = StageLink(
    //  up=cMidModBack.down,
    //  down=Node()
    //)
    //pipeMem.myLinkArr += sMidModBack
    //val s2mMidModBack = S2MLink(
    //  up=sMidModBack.down,
    //  //down=pmIo.modBack,
    //  down=pmIo.modBack,
    //)
    //pipeMem.myLinkArr += s2mMidModBack

    val midModPayload = (
      Vec.fill(extIdxLim)(
        PipeMemRmwSimDutModType()
      )
      .setName("midModPayload")
    )
    pmIo.tempModFrontPayload(0) := midModPayload(extIdxUp)
    midModPayload(extIdxSaved) := (
      RegNextWhen(midModPayload(extIdxUp), cMidModFront.up.isFiring)
      init(midModPayload(extIdxSaved).getZero)
    )
    //midModPayload(extIdxUp).allowOverride
    //val rDidFirstMidModRegNext = (
    //  Reg(Bool()) init(False)
    //)
    for (extIdx <- 0 until extIdxLim) {
      if (extIdx != extIdxSaved) {
        //when (
        //  ////RegNext(midModPayload)(extIdx).myExt.modMemWordValid
        //  //cMidModFront.up.isValid
        //  //&& pmIo.modFront(modFrontPayload).myExt.modMemWordValid
        //  ////|| !rDidFirstMidModRegNext
        //  !cMidModFront.up.isValid
        //) {
          midModPayload(extIdx) := (
            RegNext(midModPayload)(extIdx)
            init(midModPayload(extIdx).getZero)
          )
        //}
        //otherwise {
        //}
      }
    }
    val nextSetMidModPayloadState = (
      KeepAttribute(
        Bool()
      )
      .setName(s"nextSetMidModPayloadState")
    )
    val rSetMidModPayloadState = (
      KeepAttribute(
        RegNext(nextSetMidModPayloadState)
        init(nextSetMidModPayloadState.getZero)
      )
      .setName(s"rSetMidModPayloadState")
    )
    nextSetMidModPayloadState := rSetMidModPayloadState
    when (
      cMidModFront.up.isValid 
    ) {
      when (
        !rSetMidModPayloadState
      ) {
        //outp := inp
        midModPayload(extIdxUp) := pmIo.modFront(modFrontPayload)
        nextSetMidModPayloadState := True
      }
      when (
        cMidModFront.up.isFiring
      ) {
        nextSetMidModPayloadState := False
      }
    }
    //--------
    val nextAddrOneHaltItCnt = KeepAttribute(
      SInt(4 bits)

      .setName("nextAddrOneHaltItCnt")
    )
    val rAddrOneHaltItCnt = KeepAttribute(
      RegNext(nextAddrOneHaltItCnt)
      init(
        //0x0
        -1
      )
    )
      .setName("rAddrOneHaltItCnt")
    nextAddrOneHaltItCnt := rAddrOneHaltItCnt
    //--------
    object HaltItState extends SpinalEnum(
      defaultEncoding=binarySequential
    ) {
      val
        IDLE,
        HALT_IT
        = newElement();
    }
    val nextHaltItState = KeepAttribute(
      HaltItState()
    ).setName("nextHaltItState")
    val rHaltItState = KeepAttribute(
      RegNext(nextHaltItState)
      init(HaltItState.IDLE)
    )
      .setName("rHaltItState")

    nextHaltItState := rHaltItState
    //--------
    midModPayload(extIdxUp).myExt.allowOverride
    midModPayload(extIdxUp).myExt.valid := (
      cMidModFront.up.isValid
    )
    midModPayload(extIdxUp).myExt.ready := (
      cMidModFront.up.isReady
    )
    midModPayload(extIdxUp).myExt.fire := (
      cMidModFront.up.isFiring
    )
    val rSavedModMemWord = KeepAttribute(
      Reg(cloneOf(pmIo.modFront(modFrontPayload).myExt.modMemWord))
      init(pmIo.modFront(modFrontPayload).myExt.modMemWord.getZero)
    )
      .setName("rSavedModMemWord")
    val myModMemWord = (
      pmIo.modFront(modFrontPayload).myExt.rdMemWord
      (
        PipeMemRmw.modWrIdx
      )
      + 1
      //+ 2
    )
    //val myModFrontSendingModMemWordValid = (
    //  pmIo.modFront.isFiring
    //  && midModPayload(extIdxUp).myExt.modMemWordValid
    //  //pipeMem.cMid0FrontArea.tempUpMod(2)(0).myExt.modMemWordValid
    //  //modFront(modFrontPayload).myExt.modMemWordValid
    //)
    def myProveNumCycles = PipeMemRmwFormal.myProveNumCycles
    when (cMidModFront.up.isValid) {
      //when (nextHaltItState === HaltItState.IDLE) {
        //when (rHaltItState === HaltItState.IDLE) {
          //midModPayload(extIdxUp) := pmIo.modFront(modFrontPayload)
        //}
        //--------
        // BEGIN: TODO: verify that the below `when` statement works!
        //when (
        //  pmIo.modFront(modFrontPayload).op
        //  === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        //) {
        //  midModPayload(extIdxUp).myExt.modMemWord := (
        //    RegNext(midModPayload(extIdxUp).myExt.modMemWord)
        //    init(midModPayload(extIdxUp).myExt.modMemWord.getZero)
        //  )
        //}
        // END: TODO: verify that the below line works!
        //--------
      //}
      if (PipeMemRmwSimDut.doTestModOp) {
        //when (rHaltItState === HaltItState.IDLE) {
        //  //when (
        //  //  RegNext(midModPayload(extIdxUp)).myExt.memAddr(
        //  //    PipeMemRmw.modWrIdx
        //  //  ) === 0x1
        //  //  && RegNext(rHaltItState) === HaltItState.HALT_IT
        //  //  //&& cMidModFront
        //  //) {
        //  //  //cMidModFront.throwIt()
        //  //  midModPayload(extIdxUp).myExt.memAddr(
        //  //    PipeMemRmw.modWrIdx
        //  //  ) := RegNext(
        //  //    midModPayload(extIdxUp).myExt.memAddr(
        //  //      PipeMemRmw.modWrIdx
        //  //    )
        //  //  )
        //  //  midModPayload(extIdxUp).myExt.modMemWord := (
        //  //    //pmIo.modFront(modFrontPayload).myExt.rdMemWord(
        //  //    //  PipeMemRmw.modWrIdx
        //  //    //) + 2
        //  //    rSavedModMemWord
        //  //  )
        //  //  midModPayload(extIdxUp).myExt.valid := False
        //  //  midModPayload(extIdxUp).myExt.ready := False
        //  //  midModPayload(extIdxUp).myExt.fire := False
        //  //} else
        //  when (
        //    (
        //      pmIo.modFront(modFrontPayload).op
        //      === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        //    ) 
        //    //&& (
        //    //  pmIo.modFront(modFrontPayload).myExt.modMemWordValid
        //    //)
        //    ////&& (
        //    ////)
        //    //////&& RegNext(rHaltItState) =/= HaltItState.HALT_IT
        //    ////&& rHaltItState === HaltItState.IDLE
        //    //////&& RegNext(rHaltItState) === HaltItState.IDLE
        //    //////RegNext(rAddrOneHaltItCnt.msb)
        //  ) {
        //    when (
        //      //--------
        //      //(
        //      //  !pmIo.modFront(modFrontPayload).dcacheHit
        //      //  && dut.dcacheHitIo.fire
        //      //) || (
        //      //  (
        //      //    pmIo.modFront(modFrontPayload).dcacheHit
        //      //  )
        //      //  //&& (
        //      //  //  dut.dcacheHitIo.fire
        //      //  //)
        //      //)
        //      dut.dcacheHitIo.fire
        //      //--------
        //      //&& (
        //      //  pmIo.modFront(modFrontPayload).myExt.memAddr(
        //      //    PipeMemRmw.modWrIdx
        //      //  ) === 0x1
        //      //)
        //      //--------
        //    ) {
        //      nextHaltItState := HaltItState.HALT_IT
        //      nextAddrOneHaltItCnt := 0x1
        //      cMidModFront.haltIt()
        //      //cMidModFront.
        //      //midModPayload(extIdxUp).myExt.valid := False
        //      rSavedModMemWord := (
        //        myModMemWord
        //      )
        //      // prevent forwarding when we're switching states.
        //      midModPayload(extIdxUp).myExt.modMemWordValid := False
        //    } otherwise {
        //      midModPayload(extIdxUp).myExt.modMemWord := (
        //        myModMemWord
        //      )
        //      midModPayload(extIdxUp).myExt.modMemWordValid := True

        //      assert(stable(rSavedModMemWord))
        //    }
        //  }
        //}
      }
      //midModPayload(extIdxSaved) := (
      //  RegNext(midModPayload(extIdxUp))
      //)
    } otherwise {
    }
    val savedDcacheHitIo = (
      PipeMemRmwSimDut.doTestModOp
    ) generate (
      dut.dcacheHitIo.mkSaved(
        someLink=cMidModFront,
        myName=s"cMidModFront",
      )
    )
    if (PipeMemRmwSimDut.doTestModOp) {
      when (
        cMidModFront.up.isValid
      ) {
        //midModPayload(extIdxUp) := pmIo.modFront(modFrontPayload)
        when (
          (
            midModPayload(extIdxUp).op
            //pmIo.modFront(modFrontPayload).op
            === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          )
          //&& !dut.dcacheHitIo.fire
        ) {
          //--------
          //when (pastValidAfterReset()) {
          //  when (past(dut.dcacheHitIo.fire)) {
          //    assert(
          //      !dut.dcacheHitIo.rValid
          //    )
          //  }
          //}
          //--------
          cover(
            !dut.dcacheHitIo.fire
          )
          when (
            !dut.dcacheHitIo.fire
            //!savedDcacheHitIo.eitherFire
          ) {
            //--------
            cMidModFront.haltIt()
            //--------
            for (extIdx <- 0 until extIdxLim) {
              midModPayload(extIdx).myExt.modMemWordValid := False
            }
            //--------
          } otherwise {
            when (cMidModFront.up.isFiring) {
              for (extIdx <- 0 until extIdxLim) {
                midModPayload(extIdx).myExt.modMemWordValid := True
              }
            }
            //--------
          }
        } otherwise {
          when (cMidModFront.up.isFiring) {
            for (extIdx <- 0 until extIdxLim) {
              midModPayload(extIdx).myExt.modMemWordValid := True
            }
          }
        }
        //--------
      } otherwise {
      }
      //switch (rHaltItState) {
      //  is (HaltItState.IDLE) {
      //  }
      //  is (HaltItState.HALT_IT) {
      //    //--------
      //    //when (pastValidAfterReset) {
      //    //  when (past(rHaltItState) === HaltItState.IDLE) {
      //    //    assert(rSavedModMemWord === myModMemWord)
      //    //  }
      //    //}
      //    ////--------
      //    //midModPayload(extIdxUp) := (
      //    //  RegNext(midModPayload(extIdxUp))
      //    //  init(midModPayload(extIdxUp).getZero)
      //    //)
      //    ////--------
      //    //when ((rAddrOneHaltItCnt - 1).msb) {
      //    //  //midModPayload(extIdxUp).myExt.valid := True
      //    //  //when (cMidModFront.up.isFiring) {
      //    //    midModPayload(extIdxUp).myExt.modMemWord := (
      //    //      rSavedModMemWord //+ 0x1
      //    //    )
      //    //    //when (
      //    //    //  pmIo.modFront(modFrontPayload).myExt.modMemWordValid
      //    //    //) {
      //    //      midModPayload(extIdxUp).myExt.modMemWordValid := True
      //    //    //}
      //    //    nextHaltItState := HaltItState.IDLE
      //    //  //}
      //    //} otherwise {
      //    //  //midModPayload(extIdxUp).myExt.valid := False
      //    //  cMidModFront.haltIt()
      //    //}
      //    //--------
      //  }
      //}
    }
    if (PipeMemRmwSimDut.doTestModOp) {
      //when (rHaltItState === HaltItState.HALT_IT) {
      //  when (pastValidAfterReset) {
      //    when (past(rHaltItState) === HaltItState.IDLE) {
      //      assert(rSavedModMemWord === myModMemWord)
      //    }
      //  }
      //  midModPayload(extIdxUp) := (
      //    RegNext(midModPayload(extIdxUp))
      //    init(midModPayload(extIdxUp).getZero)
      //  )
      //  //midModPayload(extIdxUp).myExt.valid := True

      //  //when (cMidModFront.down.isFiring) {
      //    nextAddrOneHaltItCnt := rAddrOneHaltItCnt - 1
      //  //}

      //  //cMidModFront.haltIt()
      //  when ((rAddrOneHaltItCnt - 1).msb) {
      //    //midModPayload(extIdxUp).myExt.valid := True
      //    //when (cMidModFront.up.isFiring) {
      //      midModPayload(extIdxUp).myExt.modMemWord := (
      //        rSavedModMemWord //+ 0x1
      //      )
      //      //when (
      //      //  pmIo.modFront(modFrontPayload).myExt.modMemWordValid
      //      //) {
      //        midModPayload(extIdxUp).myExt.modMemWordValid := True
      //      //}
      //      nextHaltItState := HaltItState.IDLE
      //    //}
      //  } otherwise {
      //    //midModPayload(extIdxUp).myExt.valid := False
      //    cMidModFront.haltIt()
      //  }
      //}
    }
    if (PipeMemRmwSimDut.doTestModOp) {
      //when (
      //  //cMidModFront.up.isValid
      //  //&& RegNext(
      //  //  midModPayload(extIdxUp).myExt.memAddr(
      //  //    PipeMemRmw.modWrIdx
      //  //  ),
      //  //  //cMidModFront.up.isFiring
      //  //) === 0x1
      //  //&& 
      //  RegNext(rHaltItState) === HaltItState.HALT_IT
      //  && rHaltItState === HaltItState.IDLE
      //) {
      //  // let one through
      //  nextHaltItState := HaltItState.IDLE
      //  //midModPayload(extIdxUp).myExt.valid := False
      //  //midModPayload(extIdxUp).myExt.ready := False
      //  //midModPayload(extIdxUp).myExt.fire := False
      //} otherwise {
      //  //midModPayload(extIdxUp).myExt.modMemWordValid := (
      //  //  True
      //  //  //midModPayload(extIdxUp).myExt.valid
      //  //)
      //}
      //when (
      //  rHaltItState === HaltItState.IDLE
      //  && cMidModFront.up.isValid
      //  && !pmIo.modFront(modFrontPayload).myExt.modMemWordValid
      //) {
      //  midModPayload(extIdxUp).myExt.modMemWordValid := False
      //}
    }
    //midModPayload(extIdxSaved) := (
    //  RegNextWhen(midModPayload(extIdxUp), cMidModFront.up.isFiring)
    //  init(midModPayload(extIdxSaved).getZero)
    //)

    def setMidModStages(): Unit = {
      //pmIo.midModStages(0)(0) := (
      //  RegNext(pmIo.midModStages(0)(0))
      //  init(pmIo.midModStages(0)(0).getZero)
      //)
      //pmIo.midModStages(0)(0).myExt.valid.allowOverride
      //pmIo.midModStages(0)(0).myExt.ready.allowOverride
      //when (
      //  //modFrontStm.valid
      //  //&& modMidStm.ready
      //  //modFrontStm.fire
      //  //cMidModFront.down.isFiring
      //  //cMidModFront.down.isValid
      //  True
      //) {
        //pmIo.midModStages(0)(0) := modFrontStm.payload
        //pmIo.midModStages(0)(0) := pmIo.modFront(modFrontPayload)
        pmIo.midModStages(0)(0) := midModPayload
      //}
      //pmIo.midModStages(0)(0).myExt.valid := modFrontStm.valid
      //modMidStm << modFrontStm
    }
    setMidModStages()

    pmIo.modFront(modBackPayload) := midModPayload(extIdxUp)

    //pipeMem.myLinkArr += DirectLink(
    //  up=pmIo.modFront,
    //  down=pmIo.modBack,
    //)
    if (optModHazardKind == PipeMemRmw.modHazardKindDupl) {
      when (cMidModFront.down(modFrontPayload).myExt.hazardId.msb) {
        midModPayload(extIdxUp).myExt.modMemWord := (
          cMidModFront.down(modFrontPayload).myExt.rdMemWord(
            PipeMemRmw.modWrIdx
          ) + 0x1
        )
      }
    } else {
      //dut.io.midModStages(0)(0) := (
      //  RegNext(dut.io.midModStages(0)(0))
      //  init(dut.io.midModStages(0)(0).getZero)
      //)
      //dut.io.midModStages(0)(0).myExt.valid.allowOverride
      //when (
      //  //modFrontStm.valid
      //  //&& modMidStm.ready
      //  modFrontStm.fire
      //) {
      //  dut.io.midModStages(0)(0) := modFrontStm.payload
      //}
      //dut.io.midModStages(0)(0).myExt.valid := modFrontStm.valid
      //modMidStm << modFrontStm
    }
    //modBackStm <-/< modMidStm
  } else if (
    optModHazardKind == PipeMemRmw.modHazardKindFwd
  ) {
    assert(modStageCnt == 0)
    //modMidStm << modFrontStm
    //modBackStm << modMidStm
    //val pipe = PipeHelper(linkArr=pipeMem.myLinkArr)
    pipeMem.myLinkArr += DirectLink(
      up=pmIo.modFront,
      down=pmIo.modBack,
    )
  }
  if (
    optModHazardKind == PipeMemRmw.modHazardKindFwd
  ) {
    def cMid0FrontArea = pipeMem.cMid0FrontArea
    def myNonFwdRdMemWord = pipeMem.mod.front.myNonFwdRdMemWord
    //when (
    //  past(cMid0FrontArea.up.isFiring) init(False)
    //) {
    //  assert(
    //  )
    //}
    when (
      (
        pastValidAfterReset
      )
      //&& (
      //  past(pipeMem.cFrontArea.up.isValid) init(False)
      //) 
      && (
        past(pipeMem.cFrontArea.tempSharedEnable) init(False)
      ) && (
        past(dut.myHaveCurrWrite) init(False)
      )
    ) {
      for (ydx <- 0 until pipeMem.memArrSize) {
        for (zdx <- 0 until modRdPortCnt) {
          assert(
            myNonFwdRdMemWord(ydx)(zdx)
            === (
              (
                past(
                  //dut.rSavedModArr(
                  //  //ydx
                  //  pipeMem.cFrontArea.upExt(0)(ydx)(extIdxUp).memAddr(
                  //    zdx
                  //  )(
                  //    PipeMemRmw.addrWidth(
                  //      wordCount=pipeMem.wordCountArr(ydx)
                  //    ) - 1
                  //    downto 0
                  //  )
                  //) //+ 1
                  pipeMem.modMem(ydx)(zdx).readAsync(
                    pipeMem.cFrontArea.upExt(1)(ydx)(extIdxUp).memAddr(
                      zdx
                    )(
                      PipeMemRmw.addrWidth(
                        wordCount=pipeMem.wordCountArr(ydx)
                      ) - 1
                      downto 0
                    )
                  )
                ) init(dut.rSavedModArr(0).getZero)
              ) //+ 1
            )
          )
        }
      }
    }
  }
  //dut.io.modBack << modBackStm
  Builder(pipeMem.myLinkArr)
}
//object PipeMemRmwSim extends App {
//  def clkRate = 25.0 MHz
//  val simSpinalConfig = SpinalConfig(
//    defaultClockDomainFrequency=FixedFrequency(clkRate)
//  )
//  SimConfig
//    .withConfig(config=simSpinalConfig)
//    //.withFstWave
//    .withVcdWave
//    //.withGhdl
//    //.withVerilator
//    .compile(
//      PipeMemRmwTester()
//    )
//    .doSim/*("PipeMemRmwSim")*/({ dut =>
//      //dut.io
//      def simNumClks = (
//        //1000
//        //32
//        100
//      )
//      ////SimTimeout(32)
//      ////val scoreboard = ScoreboardInOrder[Int]()
//      if (PipeMemRmwSimDut.doRandFront) {
//        StreamDriver(dut.io.front, dut.clockDomain) { payload =>
//          //payload.myExt #= payload.myExt.getZero
//          ////payload.allowOverride
//          //payload.myExt.memAddr.randomize
//          //payload.randomize()
//          //payload.myExt.memAddr #= 
//          //if (
//          //  (
//          //    dut.io.front.valid.toBoolean
//          //  ) && (
//          //    dut.io.front.ready.toBoolean
//          //  )
//          //) {
//          //  def myMemAddr = payload.myExt.memAddr(PipeMemRmw.modWrIdx)
//          //  val tempMemAddr = (
//          //    myMemAddr.toInt
//          //  )
//          //  if (
//          //    tempMemAddr + 1
//          //    > (
//          //      (
//          //        //0x8
//          //        0x3
//          //      )
//          //      //- 1
//          //    )
//          //  ) {
//          //    myMemAddr #= 0
//          //  } else {
//          //    myMemAddr #= tempMemAddr + 1
//          //  }
//          //}
//          payload.randomize
//          true
//        }
//      } else {
//        //dut.io.front.valid #= true
//        //dut.io.front.valid #= dut.io.front.valid.randomize
//      }
//      ////dut.io.front.payload.randomize()
//
//      ////StreamMonitor(dut.io.front, dut.clockDomain) { payload => 
//      ////  scoreboard.pushRef(payload.toInt)
//      ////}
//      StreamReadyRandomizer(dut.io.back, dut.clockDomain)
//      //dut.io.back.ready #= true
//      //dut.io.back.ready #= true
//      //dut.clockDomain.forkStimulus(period=10)
//      ////dut.clockDomain.waitActiveEdgeWhere(scoreboard.matches == 100)
//
//      //def tempAddrFracWidth = 2
//      //val tempAddr = 0 << tempAddrFracWidth
//      dut.clockDomain.forkStimulus(period=10)
//
//      for (idx <- 0 until simNumClks) {
//        //dut.io.front.myExt.memAddr #= 
//        dut.clockDomain.waitRisingEdge()
//      }
//      simSuccess()
//    })
//}
