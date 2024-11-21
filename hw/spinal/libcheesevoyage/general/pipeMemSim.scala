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
import libcheesevoyage.bus.lcvStall._



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
  def pcWidth = (
    8
  )
  object ModOp
  extends SpinalEnum(defaultEncoding=binarySequential) {
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
      //BEQ_RA_SIMM,
      LIM
      = newElement();
  }
  val haveModOpMul = (
    //postMaxModOp.asBits.asUInt
    //> ModOp.MUL_RA_RB.asBits.asUInt
    true
    //false
  )
  val allModOpsSameChange = (
    //true
    false
  )
  val postMaxModOp = (
    //if (!haveModOpMul) (
    //  ModOp.MUL_RA_RB
    //) else (
      ModOp.LIM
    //)
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
      KeepAttribute(
        UInt(PipeMemRmwSimDut.modOpCntWidth bits)
      )
    )
  )
  val op = (
    (PipeMemRmwSimDut.doTestModOp) generate (
      KeepAttribute(
        PipeMemRmwSimDut.ModOp()
      )
    )
  )
  val pc = (
    (PipeMemRmwSimDut.doTestModOp) generate (
      KeepAttribute(
        UInt(PipeMemRmwSimDut.pcWidth bits)
      )
    )
  )
  //val finishedOp = (
  //  (PipeMemRmwSimDut.doTestModOp) generate (
  //    KeepAttribute(Bool())
  //  )
  //)
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
  //val psMemStallIo = new Bundle {
  //  //val valid = Bool()
  //  val ready = Bool()
  //}
  //object PipeMemRmwSimDutStallFireState
  //extends SpinalEnum(defaultEncoding=binarySequential) {
  //  val
  //    IDLE,
  //    HAD_DOWN_FIRE
  //}
  //case class PipeMemRmwSimDutStallIo(
  //) extends Area {
  //  val nextValid = Bool()
  //  val rValid = (
  //    RegNext(nextValid)
  //    init(nextValid.getZero)
  //  )
  //  nextValid := rValid
  //  val ready = Bool()
  //  //val rReady = (
  //  //  RegNext(nextReady)
  //  //  init(nextReady.getZero)
  //  //)
  //  val fire = rValid && ready
  //  anyseq(ready)
  //  //--------
  //  when (pastValidAfterReset) {
  //    //when (fire) {
  //    //  assume(!RegNext(ready))
  //    //}
  //    //--------
  //    when (
  //      !rValid
  //      //&& ready
  //    ) {
  //      assume(!ready)
  //      //--------
  //      //when (!RegNext(rValid)) {
  //      //  assume(!RegNext(ready))
  //      //}
  //      //--------
  //      //assume(!RegNext(ready))
  //      //assume(!ready)
  //    }
  //    when (
  //      rValid
  //    ) {
  //      when (
  //        RegNextWhen(
  //          (
  //            RegNextWhen(
  //              True,
  //              fire,
  //            )
  //            init(False)
  //          ),
  //          fire,
  //        )
  //        init(False)
  //      ) {
  //        cover(
  //          !ready
  //          && RegNext(ready)
  //        )
  //      }
  //    }
  //    //--------
  //    //when (!nextValid) {
  //    //  assume(!RegNext(ready))
  //    //}
  //    //when (!past(rValid)) {
  //    //  assume(!past(ready)
  //    //}
  //    //when (rValid) {
  //    //  cover(
  //    //    (
  //    //      RegNext(rValid)
  //    //    ) && (
  //    //      !ready
  //    //    ) && (
  //    //      RegNext(ready)
  //    //    )
  //    //  )
  //    //}
  //    //--------
  //  }
  //  //--------
  //  //val savedPsMemStallIo = PipeMemRmwSimDutDcacheHitIo()
  //  //savedPsMemStallIo.nextValid.allowOverride
  //  //savedPsMemStallIo.ready.allowOverride
  //  def mkSaved(
  //    someLink: CtrlLink,
  //    myName: String,
  //    //optIncludeLinkFireStuff: Boolean=false,
  //  ) = PipeMemRmwSimDutStallIoSaved(
  //    stallIo=this,
  //    someLink=someLink,
  //    myName=myName,
  //  )
  //  //--------
  //}
  def mkPipeMemRmwSimDutStallIo() = {
    LcvStallIo[
      Bool,
      Bool,
    ](
      hostDataType=None,
      devDataType=None,
      optFormal=true,
    )
  }
  //case class PipeMemRmwSimDutStallIoSaved(
  //  stallIo: PipeMemRmwSimDutStallIo,
  //  someLink: CtrlLink,
  //  myName: String
  //) extends Area {
  //  //println(
  //  //  s"${myName}"
  //  //)
  //  //--------
  //  //--------
  //  val nextSavedFire = (
  //    KeepAttribute(
  //      //Vec.fill(PipeMemRmw.extIdxLim)(
  //        Bool()
  //      //)
  //    )
  //    .setName(
  //      s"${myName}_"
  //      + s"nextSavedFire"
  //    )
  //  )
  //  val rSavedFire = (
  //    KeepAttribute(
  //      RegNext(
  //        nextSavedFire
  //      )
  //      init(
  //        //False
  //        nextSavedFire.getZero
  //      )
  //    )
  //    .setName(
  //      s"${myName}_"
  //      + s"rSavedFire"
  //    )
  //  )
  //  //for (idx <- 0 until PipeMemRmw.extIdxLim) {
  //  //  rSavedFire(idx).init(nextSavedFire(idx).getZero)
  //  //}
  //  nextSavedFire := rSavedFire
  //  //--------
  //  //val eitherSavedFire = (
  //  //  KeepAttribute(
  //  //    Bool()
  //  //  )
  //  //  .setName(
  //  //    s"${myName}_"
  //  //    + s"eitherSavedFire"
  //  //  )
  //  //)
  //  //--------
  //}
  val psExStallIo = (
    PipeMemRmwSimDut.haveModOpMul
  ) generate (
    mkPipeMemRmwSimDutStallIo()
  )
  val psMemStallIo = mkPipeMemRmwSimDutStallIo()
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
            //myCurrOp := (
            //  inp.op
            //)
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
            outp.myExt.rdMemWord := inp.myExt.rdMemWord
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
            val doTestModOpMainArea = (
              PipeMemRmwSimDut.doTestModOp
            ) generate (
              new Area {
                //--------
                val savedPsExStallIo = (
                  PipeMemRmwSimDut.haveModOpMul
                ) generate (
                  //psExStallIo.mkSaved(
                  //  someLink=cMid0Front,
                  //  myName=s"cMid0Front_savedPsExStallIo",
                  //)
                  LcvStallIoSaved(
                    stallIo=psExStallIo,
                    someLink=cMid0Front,
                  )
                )
                val savedPsMemStallIo = (
                  //psMemStallIo.mkSaved(
                  //  someLink=cMid0Front,
                  //  myName=s"cMid0Front_savedPsMemStallIo",
                  //)
                  LcvStallIoSaved(
                    stallIo=psMemStallIo,
                    someLink=cMid0Front,
                  )
                )
                //--------
                val currDuplicateIt = (
                  KeepAttribute(
                    Bool()
                  )
                  .setName(
                    s"doTestModOpMainArea_"
                    + s"currDuplicateIt"
                  )
                )
                currDuplicateIt := False
                //--------
                val doCheckHazard = (
                  KeepAttribute(
                    Bool()
                  )
                  .setName(
                    s"doTestModOpMainArea_"
                    + s"doCheckHazard"
                  )
                )
                doCheckHazard := (
                  RegNext(doCheckHazard)
                  init(doCheckHazard.getZero)
                )
                //val haveCurrLoad = (
                //  KeepAttribute(
                //    Bool()
                //  )
                //  .setName(
                //    s"doTestModOpMainArea_"
                //    + s"haveCurrLoad"
                //  )
                //)
                //haveCurrLoad := (
                //  RegNext(haveCurrLoad)
                //  init(haveCurrLoad.getZero)
                //)
                //val haveCurrMul = (
                //  KeepAttribute(
                //    Bool()
                //  )
                //  .setName(
                //    s"doTestModOpMainArea_"
                //    + s"haveCurrMul"
                //  )
                //)
                //haveCurrMul := (
                //  RegNext(haveCurrMul)
                //  init(haveCurrMul.getZero)
                //)
                val myDoHaveHazardAddrCheck = (
                  KeepAttribute(
                    outp.myExt.memAddr(0)
                    === tempModFrontPayload.myExt.memAddr(0)
                  )
                  .setName(
                    s"doTestModOpMainArea_"
                    + s"myDoHaveHazardAddrCheck"
                  )
                )
                val myDoHaveHazardValidCheck = (
                  KeepAttribute(
                    !tempModFrontPayload.myExt.modMemWordValid
                    //!savedPsMemStallIo
                  )
                  .setName(
                    s"doTestModOpMainArea_"
                    + s"myDoHaveHazardValidCheck"
                  )
                )
                val myDoHaveHazard = (
                  KeepAttribute(
                    (
                      myDoHaveHazardAddrCheck
                    ) && (
                      myDoHaveHazardValidCheck
                    )
                  )
                  .setName(
                    s"doTestModOpMainArea_"
                    + s"myDoHaveHazard"
                  )
                )
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
              }
            )
            def setOutpModMemWord(
              someRdMemWord: UInt=myRdMemWord
            ): Unit = {
              //def haveCurrLoad = (
              //  doTestModOpMainArea.haveCurrLoad
              //)
              //def haveCurrMul = (
              //  doTestModOpMainArea.haveCurrMul
              //)
              outp.myExt.modMemWordValid := (
                True
              )
              //if (PipeMemRmwSimDut.allModOpsSameChange) {
              //} else {
                switch (myCurrOp) {
                  is (PipeMemRmwSimDut.ModOp.ADD_RA_RB) {
                    outp.myExt.modMemWord := (
                      someRdMemWord + 0x1
                    )
                    //outp.myExt.modMemWordValid := (
                    //  True
                    //)
                  }
                  is (PipeMemRmwSimDut.ModOp.LDR_RA_RB) {
                    outp.myExt.modMemWord := (
                      //someRdMemWord //+ 0x1
                      0x0
                    )
                    outp.myExt.modMemWordValid := (
                      False
                    )
                  }
                  is (PipeMemRmwSimDut.ModOp.MUL_RA_RB) {
                    outp.myExt.modMemWord := (
                      (
                        if (PipeMemRmwSimDut.allModOpsSameChange) (
                          someRdMemWord + 0x1
                        ) else (
                          someRdMemWord << 1
                        )
                      )(
                        outp.myExt.modMemWord.bitsRange
                      )
                    )
                    //outp.myExt.modMemWordValid := (
                    //  True
                    //)
                  }
                  //if (PipeMemRmwSimDut.ModOp.BEQ_RA_SIMM) {
                  //  outp.myExt.modMemWord := (
                  //    0x0
                  //  )
                  //  outp.myExt.modMemWordValid := (
                  //    False
                  //  )
                  //}
                }
              //}
              //when (haveCurrLoad) {
              //  outp.myExt.modMemWord := (
              //    someRdMemWord //+ 0x1
              //  )
              //  outp.myExt.modMemWordValid := False
              //} otherwise { // when (!haveCurrLoad)
              //  when (!haveCurrMul) {
              //    outp.myExt.modMemWord := (
              //      someRdMemWord + 0x1
              //    )
              //  } otherwise {
              //    outp.myExt.modMemWord := (
              //      someRdMemWord << 1
              //    )
              //  }
              //  outp.myExt.modMemWordValid := True
              //}
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
            //val doHandleHazardWithDcacheMissArea = (
            //  PipeMemRmwSimDut.doTestModOp
            //) generate (
            //  new Area {
            //  }
            //)
            //def doMulHaltItFsmIdleInnards(
            //  //doDuplicateIt: Boolean
            //): Unit = {
            //  if (PipeMemRmwSimDut.doTestModOp) {
            //    def myInitMulHaltItCnt = 0x1
            //    cMid0Front.duplicateIt()
            //    //outp := (
            //    //  RegNext(outp)
            //    //  init(outp.getZero)
            //    //)
            //    when (
            //      //cMid0Front.down.isFiring
            //      //modFront.isFiring
            //      modFront.isValid
            //    ) {
            //      nextHaltItState := (
            //        PipeMemRmwSimDutHaltItState.HALT_IT
            //      )
            //      nextMulHaltItCnt := myInitMulHaltItCnt
            //    }
            //    outp.myExt.modMemWordValid := False
            //    rSavedRdMemWord := myRdMemWord
            //  }
            //}
            //def doMulHaltItFsmHaltItInnards(): Unit = {
            //  if (PipeMemRmwSimDut.doTestModOp) {
            //    //outp := (
            //    //  RegNext(outp)
            //    //  init(outp.getZero)
            //    //)
            //    when ((rMulHaltItCnt - 1).msb) {
            //      when (
            //        cMid0Front.down.isFiring
            //        //modFront.isFiring
            //      ) {
            //        setOutpModMemWord(rSavedRdMemWord)
            //        nextHaltItState := PipeMemRmwSimDutHaltItState.IDLE
            //      }
            //    } otherwise {
            //      nextMulHaltItCnt := rMulHaltItCnt - 1
            //      //cMid0Front.haltIt()
            //      cMid0Front.duplicateIt()
            //      outp.myExt.modMemWordValid := False
            //    }
            //  }
            //}
            def doTestModOpMain(
              //doCheckHazard: Bool//ean//=false
            ) = new Area {
              //--------
              def currDuplicateIt = (
                doTestModOpMainArea.currDuplicateIt
              )
              //--------
              def doCheckHazard = (
                doTestModOpMainArea.doCheckHazard
              )
              //def haveCurrLoad = (
              //  doTestModOpMainArea.haveCurrLoad
              //)
              //def haveCurrMul = (
              //  doTestModOpMainArea.haveCurrMul
              //)
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
                someRdMemWord: UInt=myRdMemWord,
              ): Unit = {
                outp.myExt.valid := True
                nextPrevTxnWasHazard := False
                setOutpModMemWord(
                  someRdMemWord=someRdMemWord
                )
              }
              //def myStallKindDuplicateIt = 0
              //def myStallKindHaltIt = 1
              def handleDuplicateIt(
                //doDuplicateIt: Boolean,
                //stallKind: Int,
                someModMemWordValid: Bool=False,
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
                //if (
                //  //actuallyDuplicateIt
                //  //doDuplicateIt
                //  stallKind == myStallKindDuplicateIt
                //) {
                  cMid0Front.duplicateIt()
                  //doTestModOpMainArea.currDuplicateIt := True
                //} else if (
                //  stallKind == myStallKindHaltIt
                //) {
                //  cMid0Front.haltIt()
                //}
              }
              //def handleDuplicateIt(
              //  someModMemWordValid: Bool=False,
              //): Unit = {
              //  handleStall(
              //    stallKind=myStallKindDuplicateIt,
              //    someModMemWordValid=someModMemWordValid,
              //  )
              //}
              //def handleHaltIt(
              //  someModMemWordValid: Bool=False,
              //): Unit = {
              //  handleStall(
              //    stallKind=myStallKindHaltIt,
              //    someModMemWordValid=someModMemWordValid,
              //  )
              //}
              //when (
              //  cMid0Front.up.isValid
              //  //|| (
              //  //  nextSetOutpState
              //  //  //rSetOutpState
              //  //)
              //) {
              //  switch (
              //    //inp.op
              //    myCurrOp
              //  ) {
              //    is (PipeMemRmwSimDut.ModOp.ADD_RA_RB) {
              //      haveCurrLoad := False
              //      haveCurrMul := False
              //    }
              //    is (PipeMemRmwSimDut.ModOp.LDR_RA_RB) {
              //      haveCurrLoad := True
              //      haveCurrMul := False
              //    }
              //    is (PipeMemRmwSimDut.ModOp.MUL_RA_RB) {
              //      // we should stall `EX` in this case until the
              //      // calculation is done. The same stalling logic
              //      // will be used for `divmod`, etc.
              //      //doHandleHazardWithDcacheMissArea.doIt := False
              //      haveCurrLoad := False
              //      haveCurrMul := True
              //    }
              //  }
              //  //when (
              //  //  //doHandleHazardWithDcacheMissArea.doIt
              //  //) {
              //  //  doHandleHazardWithDcacheMiss()
              //  //}
              //}
              //def nextState = (
              //  doTestModOpMainArea.nextState
              //)
              //def rState = (
              //  doTestModOpMainArea.rState
              //)
              def myDoHaveHazardAddrCheck = (
                doTestModOpMainArea.myDoHaveHazardAddrCheck
              )
              def myDoHaveHazardValidCheck = (
                doTestModOpMainArea.myDoHaveHazardValidCheck
              )
              def myDoHaveHazard = (
                doTestModOpMainArea.myDoHaveHazard
              )
              //def nextSavedRdMemWord1 = (
              //  doTestModOpMainArea.nextSavedRdMemWord1
              //)
              //def rSavedRdMemWord1 = (
              //  doTestModOpMainArea.rSavedRdMemWord1
              //)
              def rTempPrevOp = (
                doTestModOpMainArea.rTempPrevOp
              )
              def savedPsExStallIo = (
                doTestModOpMainArea.savedPsExStallIo
              )
              def savedPsMemStallIo = (
                doTestModOpMainArea.savedPsMemStallIo
              )
              //switch (
              //  rState
              //) {
              //  is (False) {
                  //when (cMid0Front.up.isValid) {
                    //--------
                    //val nextMulState = (
                    //  KeepAttribute(
                    //    Bool()
                    //  )
                    //  .setName(s"nextMulState")
                    //)
                    //val rMulState = (
                    //  KeepAttribute(
                    //    RegNext(
                    //      nextMulState
                    //    )
                    //    init(nextMulState.getZero)
                    //  )
                    //  .setName(s"rMulState")
                    //)
                    //nextMulState := rMulState
                    when (doCheckHazard) {
                      //def doHandleStall(
                      //): Unit = {
                      //  //handleStall(
                      //  //  stallKind=myStallKindDuplicateIt
                      //  //  //doDuplicateIt=(
                      //  //  //  true
                      //  //  //),
                      //  //  //someModMemWordValid=(
                      //  //  //  False
                      //  //  //),
                      //  //)
                      //  handleDuplicateIt()
                      //}
                      //when (!haveCurrMul) {
                        when (
                          myDoHaveHazard
                          //|| !savedPsMemStallIo.eitherSavedFire
                        ) {
                          //doHandleStall()
                          //handleDuplicateIt()
                          currDuplicateIt := True
                        }
                      //} otherwise { // when (haveCurrMul)
                      //  //--------
                      //  //def tempFinishedOp = (
                      //  //  tempModFrontPayload.finishedOp
                      //  //)
                      //  //when (!tempFinishedOp) {
                      //  //  //doHandleStall()
                      //  //  handleDuplicateIt()
                      //  //} otherwise { // when (tempFinishedOp)
                      //  //  handleHaltIt()
                      //  //  //handleStall(
                      //  //  //  stallKind=myStallKindHaltIt
                      //  //  //  //doDuplicateIt=(
                      //  //  //  //  false
                      //  //  //  //),
                      //  //  //  //someModMemWordValid=(
                      //  //  //  //  False
                      //  //  //  //),
                      //  //  //)
                      //  //}
                      //  //--------
                      //}
                      //when (
                      //  haveCurrMul
                      //) {
                      //  when (
                      //    myDoHaveHazardAddrCheck
                      //    && !myDoHaveHazardValidCheck
                      //  ) {
                      //  }
                      //}
                      cover(
                        (
                          ///outp.myExt.memAddr(0)
                          //=== tempModFrontPayload.myExt.memAddr(0)
                          myDoHaveHazardAddrCheck
                        ) && (
                          cMid0Front.up.isFiring
                        )
                      )
                    } 
                    //when (!doCheckHazard) {
                      //when (
                      //  haveCurrMul
                      //) {
                      //  psExStallIo.nextValid := True
                      //  when (
                      //    psExStallIo.fire
                      //  ) {
                      //    psExStallIo.nextValid := False
                      //  } otherwise {
                      //    handleStall(
                      //      someModMemWordValid=(
                      //        False
                      //      ),
                      //      doDuplicateIt=(
                      //        false
                      //      ),
                      //    )
                      //  }
                      //}
                    //}
                    //--------
                    when (
                      //savedPsMemStallIo.eitherSavedFire
                      psMemStallIo.fire
                    ) {
                      psMemStallIo.nextValid := False
                    }
                    //--------
                    when (cMid0Front.up.isFiring) {
                      handleCurrFire()
                    }
                    when (cMid0Front.up.isValid) {
                      switch (myCurrOp) {
                        is (PipeMemRmwSimDut.ModOp.ADD_RA_RB) {
                        }
                        is (PipeMemRmwSimDut.ModOp.LDR_RA_RB) {
                          when (cMid0Front.up.isFiring) {
                            nextPrevTxnWasHazard := True
                            //psMemStallIo.valid := True
                            psMemStallIo.nextValid := (
                              //!outp.dcacheHit
                              True
                            )
                          }
                        }
                        is (PipeMemRmwSimDut.ModOp.MUL_RA_RB) {
                          if (PipeMemRmwSimDut.haveModOpMul) {
                            //when (
                            //) {
                            //}
                            //--------
                            when (
                              //savedPsExStallIo
                              doCheckHazard
                            ) {
                              when (!currDuplicateIt) {
                                psExStallIo.nextValid := (
                                  True
                                )
                              }
                            } otherwise { // when (!doCheckHazard)
                              psExStallIo.nextValid := (
                                True
                              )
                            }
                            //--------
                            when (
                              savedPsExStallIo.myDuplicateIt
                            ) {
                              currDuplicateIt := True
                            }
                          }
                        }
                        //is (PipeMemRmwSimDut.ModOp.BEQ_RA_SIMM) {
                        //}
                      }
                    }
                    //--------
                  //}
              //  }
              //  is (True) {
              //  }
              //}
              if (PipeMemRmwSimDut.haveModOpMul) {
                when (
                  psExStallIo.fire
                ) {
                  psExStallIo.nextValid := False
                }
              }
              when (currDuplicateIt) {
                handleDuplicateIt()
              }
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
  when (!tempHadModFrontIsFiring._1) {
    for (ydx <- 0 until wordCount) {
      assume(
        (
          pipeMem.modMem(0)(0).readAsync(
            address=U(s"${log2Up(wordCount)}'d${ydx}")
          )
        ) === (
          0x0
        )
      )
    }
  }
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
  val myHistModBackPayloadOp = (
    KeepAttribute(
      History(
        that=(
          pipeMem.io.modBack(pipeMem.io.modBackPayloadArr(0)).op
        ),
        length=myProveNumCycles,
        when=(
          tempHadModFrontIsFiring._1
          //&& myHaveCurrWrite
        ),
        //init=(
        //  PipeMemRmwSimDut.ModOp.ADD_RA_RB
        //)
      )
    )
    .setName(s"myHistModBackPayloadOp")
  )
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
  when (
    pastValidAfterReset()
  ) {
    when (
      (
        //myHaveCurrWrite
        tempHaveCurrWrite._1
      ) && (
        tempHaveCurrWritePostFirst._1
      )
    ) {
      assert(
        myHistModBackOpCnt(
          //0
          tempHaveCurrWrite._2
        )
        === myHistModBackOpCnt(tempHaveCurrWritePostFirst._2) + 1
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
    //val myHistCoverVec = (
    //  Vec.fill(myCoverVecSize)(
    //    Reg(/*Flow*/(PipeMemRmwSimDutModType()))
    //  )
    //)
    val tempMyCoverInit = PipeMemRmwSimDutModType()
    tempMyCoverInit.allowOverride
    tempMyCoverInit := tempMyCoverInit.getZero
    tempMyCoverInit.op := PipeMemRmwSimDut.ModOp.LIM
    val myHistCoverVec = (
      KeepAttribute(
        History(
          that=modBack(modBackPayload),
          length=myCoverVecSize,
          when=myCoverCond,
          init=tempMyCoverInit,
        )
      )
    )
    //for (idx <- 0 until myHistCoverVec.size) {
    //  val tempMyCoverInit = PipeMemRmwSimDutModType()
    //  tempMyCoverInit.allowOverride
    //  tempMyCoverInit := tempMyCoverInit.getZero
    //  tempMyCoverInit.op := PipeMemRmwSimDut.ModOp.LIM
    //  myHistCoverVec(idx).init(
    //    //myHistCoverVec
    //    //PipeMemRmwSimDut.ModOp.LIM
    //    //myHistCoverVec(idx).getZero
    //    tempMyCoverInit
    //  )
    //  when (myCoverCond) {
    //    if (idx == 0) {
    //      myHistCoverVec(idx) := modBack(modBackPayload)
    //    } else {
    //      myHistCoverVec(idx) := myHistCoverVec(idx - 1)
    //    }
    //  }
    //}
    when (myHaveCurrWrite) {
      cover(
        (
          myHistCoverVec(0).op === PipeMemRmwSimDut.ModOp.ADD_RA_RB
        ) && (
          myHistCoverVec(1).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& !myHistCoverVec(1).dcacheHit
        ) && (
          myHistCoverVec(2).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& !myHistCoverVec(2).dcacheHit
        )
      )
      cover(
        (
          myHistCoverVec(0).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& !myHistCoverVec(0).dcacheHit
        ) && (
          myHistCoverVec(1).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& myHistCoverVec(1).dcacheHit
        ) && (
          myHistCoverVec(2).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& !myHistCoverVec(2).dcacheHit
        )
      )
      cover(
        (
          myHistCoverVec(0).op === PipeMemRmwSimDut.ModOp.ADD_RA_RB
          //&& !myHistCoverVec(0).dcacheHit
        ) && (
          myHistCoverVec(1).op === PipeMemRmwSimDut.ModOp.MUL_RA_RB
          //&& !myHistCoverVec(1).dcacheHit
        ) && (
          myHistCoverVec(2).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& myHistCoverVec(2).dcacheHit
        )
      )
      cover(
        (
          myHistCoverVec(0).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
          //&& !myHistCoverVec(0).dcacheHit
        ) && (
          myHistCoverVec(1).op === PipeMemRmwSimDut.ModOp.MUL_RA_RB
          //&& !myHistCoverVec(0).dcacheHit
        ) && (
          myHistCoverVec(2).op === PipeMemRmwSimDut.ModOp.ADD_RA_RB
          //&& !myHistCoverVec(2).dcacheHit
        )
      )
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
      //when (/*past*/(myHaveCurrWrite)) {
      //  //when (
      //  //  //modBack.isValid
      //  //  //&& myHaveCurrWrite
      //  //  //tempHadFrontIsFiring._1
      //  //  //&& tempHadMid0FrontUpIsFiring._1
      //  //  //tempHaveCurrWrite._1
      //  //  //&& myHistHaveCurrWrite
      //  //  tempHaveCurrWrite._1
      //  //  && tempHaveCurrWritePostFirst._1
      //  //  //&& (
      //  //  //  tempHadReset._1
      //  //  //)
      //  //  //&& (
      //  //  //  tempHadReset._1
      //  //  //  && tempHadNotReset._1
      //  //  //)
      //  //) {
      //  //  assert(
      //  //    ////myHistModBackOpCnt(0) + 1 === myHistModBackOpCnt(1)
      //  //    ///*RegNext*/(myHistModBackOpCnt(0))
      //  //    //=== /*RegNext*/(myHistModBackOpCnt(1)) + 1
      //  //    ////modBack(modBackPayload).opCnt
      //  //    ////=== myHistModBackOpCnt(0) + 1
      //  //    myHistModBackOpCnt(tempHaveCurrWrite._2)
      //  //    === myHistModBackOpCnt(tempHaveCurrWritePostFirst._2) + 1
      //  //  )
      //  //}
      //  //when (
      //  //  myHaveCurrWrite
      //  //  && (
      //  //    RegNextWhen(
      //  //      myHaveCurrWrite,
      //  //      myHaveCurrWrite
      //  //    )
      //  //    init(False)
      //  //  )
      //  //) {
      //  //  assert
      //  //}
      //}
      when (
        ///*past*/(modBack.isFiring)
        //&& /*past*/(pipeMem.mod.back.myWriteEnable(0))
        ///*past*/(myHaveCurrWrite)
        //pipeMem.mod.back.myWriteEnable(0)
        True
      ) {
        def firstTempHaveCurrWrite = (
          tempHaveCurrWrite
          //tempHaveCurrWritePostFirst
        )
        def secondTempHaveCurrWrite = (
          tempHaveCurrWritePostFirst
          //tempHaveCurrWritePostSecond
        )
        when (
          (
            /*past*/(firstTempHaveCurrWrite._1)
            && /*past*/(secondTempHaveCurrWrite._1)
            && (tempHaveCurrWritePostSecond._1)
          ) && (
            (
              /*past*/(
                myHistMyWriteAddr(/*past*/(firstTempHaveCurrWrite._2))
              ) === /*past*/(
                myHistMyWriteAddr(/*past*/(secondTempHaveCurrWrite._2))
              )
            ) 
          )
        ) {
          def myTempLeft = (
            //if (
            //  firstTempHaveCurrWrite == tempHaveCurrWrite
            //) (
            //  Mux[UInt](
            //    
            //  )
            //) else (
              myHistMyWriteData(firstTempHaveCurrWrite._2)
            //)
          )
          def myTempRight = (
            //myHistMyWriteData(secondTempHaveCurrWrite._2)
            if (
              (
                firstTempHaveCurrWrite == tempHaveCurrWrite
              ) && (
                secondTempHaveCurrWrite == tempHaveCurrWritePostFirst
              )
            ) (
              //Mux[UInt](
              //  (
              //    //firstTempHaveCurrWrite._1
              //    //&& (
              //    //  firstTempHaveCurrWrite._2 === 0x0
              //    //)
              //    myHaveCurrWrite
              //  ),
                modBack(modBackPayload).myExt.rdMemWord(0),
              //  myHistMyWriteData(firstTempHaveCurrWrite._2)
              //)
            ) else (
              myHistMyWriteData(firstTempHaveCurrWrite._2)
            )
          )
          if (
            (
              optModHazardKind == PipeMemRmw.modHazardKindDupl
            ) || (
              (
                optModHazardKind == PipeMemRmw.modHazardKindFwd
              ) && (
                PipeMemRmwSimDut.allModOpsSameChange
              )
            )
          ) {
            when (myHaveCurrWrite) {
              assert(
                myTempLeft
                === myTempRight + 1
              )
            }
          } else if (
            //PipeMemRmwSimDut.doTestModOp
            optModHazardKind == PipeMemRmw.modHazardKindFwd
          ) {
            when (myHaveCurrWrite) {
              switch (
                //myHistModBackPayloadOp(firstTempHaveCurrWrite._2)
                modBack(modBackPayload).op
              ) {
                is (PipeMemRmwSimDut.ModOp.ADD_RA_RB) {
                  assert(
                    ///*past*/(tempLeft) //+ 1
                    //=== (
                    //  //mySavedMod
                    //  /*past*//*past*/(tempRight) + 1 //+ 1
                    //  /*past*///(mySavedMod)
                    //  //mySavedMod + 1
                    //)
                    myTempLeft
                    === myTempRight + 1
                  )
                }
                is (PipeMemRmwSimDut.ModOp.LDR_RA_RB) {
                  assert(
                    myTempLeft
                    === myTempRight - 1
                    //myTempLeft
                    //=== myTempRight + 1
                  )
                }
                is (PipeMemRmwSimDut.ModOp.MUL_RA_RB) {
                  val tempBitsRange = (
                    //wordType().bitsRange
                    modBack(modBackPayload).myExt.modMemWord.bitsRange
                  )
                  assert(
                    myTempLeft(tempBitsRange)
                    === (myTempRight << 1)(tempBitsRange)
                    //myTempLeft(tempBitsRange)
                    //=== (myTempRight + 1)(tempBitsRange)
                  )
                }
                //is (PipeMemRmwSimDut.ModOp.BEQ_RA_SIMM) {
                //}
              }
            }
          }
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
    cover(
      back.isFiring
      && (
        back(backPayload).myExt.modMemWord === 0x3
      ) && (
        back(backPayload).myExt.memAddr(PipeMemRmw.modWrIdx) === 0x2
      ) 
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
  //--------
  //val modFrontStm = Stream(modType())
  //val modMidStm = Stream(modType())
  //val modBackStm = Stream(modType())
  ////modFrontStm <-/< dut.io.modFront
  //modFrontStm << dut.io.modFront
  def extIdxUp = PipeMemRmw.extIdxUp
  def extIdxSaved = PipeMemRmw.extIdxSaved
  def extIdxLim = PipeMemRmw.extIdxLim
  //println(
  //  s"modStageCnt: ${modStageCnt}"
  //)
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
    //val finishedOp
    //val finishedOpArea = new Area {
    //  val finishedOp = pmIo.tempModFrontPayload(0).finishedOp
    //  //--------
    //  val nextState = (
    //    KeepAttribute(
    //      Bool()
    //    )
    //    .setName(
    //      s"finishedOpArea"
    //      + s"_nextState"
    //    )
    //  )
    //  val rState = (
    //    KeepAttribute(
    //      RegNext(nextState)
    //      init(nextState.getZero)
    //    )
    //    .setName(
    //      s"finishedOpArea"
    //      + s"_rState"
    //    )
    //  )
    //  nextState := rState
    //  //--------
    //  switch (rState) {
    //    is (False) {
    //      when (cMidModFront.up.isValid) {
    //      }
    //    }
    //    is (True) {
    //    }
    //  }
    //  //--------
    //}
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
            RegNext(midModPayload(extIdx))
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
    val savedPsMemStallIo = (
      PipeMemRmwSimDut.doTestModOp
    ) generate (
      //dut.psMemStallIo.mkSaved(
      //  someLink=cMidModFront,
      //  myName=s"cMidModFront",
      //)
      LcvStallIoSaved(
        stallIo=dut.psMemStallIo,
        someLink=cMidModFront,
      )
    )
    //when (
    //  //savedPsMemStallIo.eitherLinkFireState(extIdxUp)
    //  cMidModFront.up.isValid
    //) {
    //  midModPayload(extIdxUp) := pmIo.modFront(modFrontPayload)
    //}
    //--------
    //val nextAddrOneHaltItCnt = KeepAttribute(
    //  SInt(4 bits)

    //  .setName("nextAddrOneHaltItCnt")
    //)
    //val rAddrOneHaltItCnt = KeepAttribute(
    //  RegNext(nextAddrOneHaltItCnt)
    //  init(
    //    //0x0
    //    -1
    //  )
    //)
    //  .setName("rAddrOneHaltItCnt")
    //nextAddrOneHaltItCnt := rAddrOneHaltItCnt
    //--------
    //object HaltItState extends SpinalEnum(
    //  defaultEncoding=binarySequential
    //) {
    //  val
    //    IDLE,
    //    HALT_IT
    //    = newElement();
    //}
    //val nextHaltItState = KeepAttribute(
    //  HaltItState()
    //).setName("nextHaltItState")
    //val rHaltItState = KeepAttribute(
    //  RegNext(nextHaltItState)
    //  init(HaltItState.IDLE)
    //)
    //  .setName("rHaltItState")

    //nextHaltItState := rHaltItState
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
        //      //  && dut.psMemStallIo.fire
        //      //) || (
        //      //  (
        //      //    pmIo.modFront(modFrontPayload).dcacheHit
        //      //  )
        //      //  //&& (
        //      //  //  dut.psMemStallIo.fire
        //      //  //)
        //      //)
        //      dut.psMemStallIo.fire
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
    if (PipeMemRmwSimDut.doTestModOp) {
      //val nextHadPsMemStallFire = (
      //  KeepAttribute (
      //    Bool()
      //  )
      //  .setName(
      //    s"nextHadPsMemStallFire"
      //  )
      //)
      //val rHadPsMemStallFire = (
      //  KeepAttribute (
      //    RegNext(
      //      //Bool()
      //      nextHadPsMemStallFire
      //    )
      //    init(
      //      //False
      //      nextHadPsMemStallFire.getZero
      //    )
      //  )
      //  .setName(
      //    s"rHadPsMemStallFire"
      //  )
      //)
      //nextHadPsMemStallFire := rHadPsMemStallFire
      //val eitherPsMemStallIoFire = (
      //  KeepAttribute(
      //    dut.psMemStallIo.fire
      //    || rHadPsMemStallFire
      //  )
      //  .setName(s"eitherPsMemStallIoFire")
      //)
      //val nextHadDownFire = (
      //  KeepAttribute (
      //    Bool()
      //  )
      //  .setName(
      //    s"nextHadDownFire"
      //  )
      //)
      //val rHadDownFire = (
      //  KeepAttribute (
      //    RegNext(
      //      //Bool()
      //      nextHadDownFire
      //    )
      //    init(
      //      //False
      //      nextHadDownFire.getZero
      //    )
      //  )
      //  .setName(
      //    s"rHadDownFire"
      //  )
      //)
      //nextHadDownFire := rHadDownFire
      //val eitherDownFire = (
      //  KeepAttribute(
      //    cMidModFront.down.isFiring
      //    || rHadDownFire
      //  )
      //  .setName(s"eitherDownFire")
      //)
      cover(
        //!dut.psMemStallIo.fire
        (
          RegNextWhen(
            (
              RegNextWhen(
                True,
                (
                  midModPayload(extIdxUp).op
                  //pmIo.modFront(modFrontPayload).op
                  === PipeMemRmwSimDut.ModOp.LDR_RA_RB
                ) && (
                  cMidModFront.up.isFiring
                ),
              )
              init(False)
            ),
            (
              midModPayload(extIdxUp).op
              //pmIo.modFront(modFrontPayload).op
              === PipeMemRmwSimDut.ModOp.ADD_RA_RB
            ) && (
              cMidModFront.up.isFiring
            ),
          )
          init(False)
        )
        //&& (
        //  (
        //    !eitherPsMemStallIoFire
        //  ) || (
        //    !eitherDownFire
        //  )
        //)
      )

      when (
        cMidModFront.up.isValid
        //savedPsMemStallIo.eitherLinkFireState(
        //  PipeMemRmw.extIdxUp
        //)
        && (
          midModPayload(extIdxUp).op
          //pmIo.modFront(modFrontPayload).op
          === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        )
      ) {
        //midModPayload(extIdxUp) := pmIo.modFront(modFrontPayload)
        //when (
        //  (
        //    //midModPayload(extIdxUp).op
        //    pmIo.modFront(modFrontPayload).op
        //    === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        //  )
        //  //&& !dut.psMemStallIo.fire
        //) {
          //--------
          //when (pastValidAfterReset()) {
          //  when (past(dut.psMemStallIo.fire)) {
          //    assert(
          //      !dut.psMemStallIo.rValid
          //    )
          //  }
          //}
          //--------
          when (
            //!(
            //  (
            //    dut.psMemStallIo.fire
            //    && cMidModFront.down.isFiring
            //  ) || (
            //    dut.psMemStallIo.fire
            //    && rHadDownFire
            //  ) || (
            //    rHadPsMemStallFire
            //    && cMidModFront.down.isFiring
            //  )
            //)
            savedPsMemStallIo.myDuplicateIt
          ) {
            //--------
            //when (!rHadDownFire) {
            //  cMidModFront.duplicateIt()
            //} otherwise {
            //  cMidModFront.haltIt()
            //}
            cMidModFront.duplicateIt()
            //--------
            midModPayload(extIdxUp).myExt.modMemWordValid := False
            //--------
          } otherwise {
            //--------
            midModPayload(extIdxUp).myExt.modMemWordValid := True
            //--------
          }
        //} 
        //--------
        //midModPayload(extIdxUp).myExt.modMemWordValid := False
        //--------
      } otherwise {
        ////when (cMidModFront.up.isValid) {
        //  for (extIdx <- 0 until extIdxLim) {
        //    midModPayload(extIdx).myExt.modMemWordValid := True
        //  }
        ////}
      }
      //when (
      //  cMidModFront.up.isValid
      //  && (
      //    midModPayload(extIdxUp).op
      //    //pmIo.modFront(modFrontPayload).op
      //    //=/= PipeMemRmwSimDut.ModOp.LDR_RA_RB
      //    === PipeMemRmwSimDut.ModOp.ADD_RA_RB
      //  )
      //) {
      //  //for (extIdx <- 0 until extIdxLim) {
      //    midModPayload(extIdxUp).myExt.modMemWordValid := True
      //  //}
      //}
      when (
        dut.psMemStallIo.fire
        //savedPsMemStallIo.eitherSavedFire
      ) {
        //nextHadPsMemStallFire := True
        //when (cMidModFront.up.isValid) {
          //for (extIdx <- 0 until extIdxLim) {
            midModPayload(extIdxUp).myExt.modMemWordValid := True
            midModPayload(extIdxUp).myExt.modMemWord := (
              if (PipeMemRmwSimDut.allModOpsSameChange) (
                midModPayload(extIdxUp).myExt.rdMemWord(0) + 1
              ) else (
                midModPayload(extIdxUp).myExt.rdMemWord(0) - 1
              )
            )
          //}
        //}
        //--------
      }
      cover(
        dut.psMemStallIo.fire
        //&& pmIo.tempModFrontPayload(0).myExt.modMemWordValid
      )
      //when (
      //  cMidModFront.down.isFiring
      //) {
      //  nextHadDownFire := True
      //}
      //when (cMidModFront.up.isFiring) {
      //  ////nextHadPsMemStallFire := False
      //  //nextHadPsMemStallFire := False
      //  //nextHadDownFire := False
      //  //for (extIdx <- 0 until extIdxLim) {
      //    midModPayload(extIdxUp).myExt.modMemWordValid := True
      //  //}
      //}
    }
    //midModPayload(extIdxSaved) := (
    //  RegNextWhen(midModPayload(extIdxUp), cMidModFront.up.isFiring)
    //  init(midModPayload(extIdxSaved).getZero)
    //)

    def setMidModStages(): Unit = {
      pmIo.midModStages(0)(0) := midModPayload
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
    }
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
