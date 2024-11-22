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
  // Fake instruction opcodes, just for testing different kinds of stalls
  // that may occur 
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
      BEQ_RA_SIMM,
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
  //--------
  //val setRegPc = (
  //  (PipeMemRmwSimDut.doTestModOp) generate (
  //    KeepAttribute(
  //      Flow(
  //        UInt(PipeMemRmwSimDut.pcWidth bits)
  //      )
  //    )
  //  )
  //)
  //--------
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
  optFormal: Boolean,
) extends Area {
  //def doFormal: Boolean = {
  //  GenerationFlags.formal {
  //    return true
  //  }
  //  return false
  //}
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
  //val psMemStallHost = new Bundle {
  //  //val valid = Bool()
  //  val ready = Bool()
  //}
  //val io = new Area {
  //  def mkMainPsExStallIo() = (
  //    LcvStallIo[
  //      Bool,
  //      Bool,
  //    ](
  //      hostDataType=Some(Bool()),
  //      devDataType=Some(Bool()),
  //    )
  //  )
  //  def mkMainPsMemStallIo() = (
  //    LcvStallIo[
  //      Bool,
  //      Bool,
  //    ](
  //      hostDataType=Some(Bool()),
  //      devDataType=Some(Bool()),
  //    )
  //  )
  //  val psExStallIo = (
  //    PipeMemRmwSimDut.haveModOpMul
  //  ) generate (
  //    /*master*/(
  //      LcvStallIo[
  //        Bool, // HostDataT
  //        Bool, // DevDataT
  //      ](
  //        //hostDataType=(false, Bool()),
  //        //devDataType=(false, Bool()),
  //        hostDataType=Some(Bool()),
  //        devDataType=Some(Bool()),
  //      )
  //    )
  //  )
  //  val psMemStallIo = (
  //    master(
  //      LcvStallIo[
  //        Bool, // HostDataT
  //        Bool, // DevDataT
  //      ](
  //        //hostDataType=(false, Bool()),
  //        //devDataType=(false, Bool()),
  //        hostDataType=Some(Bool()),
  //        devDataType=Some(Bool()),
  //      )
  //    )
  //  )
  //}
  def mkPipeMemRmwSimDutStallHost[
    HostDataT <: Data,
    DevDataT <: Data,
  ](
    stallIo: Option[LcvStallIo[
      HostDataT,
      DevDataT,
    ]]
  ) = {
    LcvStallHost[
      HostDataT,
      DevDataT,
    ](
      //hostDataType=(false, Bool()),
      //devDataType=(false, Bool()),
      stallIo=stallIo,
      optFormalJustHost=optFormal,
    )
  }
  val psExStallHost = (
    PipeMemRmwSimDut.haveModOpMul
  ) generate (
    mkPipeMemRmwSimDutStallHost[
      Bool,
      Bool,
    ](
      stallIo=(
        //io.psExStallIo
        None
      ),
    )
  )
  val psMemStallHost = (
    mkPipeMemRmwSimDutStallHost[
      Bool,
      Bool,
    ](
      stallIo=(
        //io.psMemStallIo
        None,
      ),
    )
  )
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
    optFormal=optFormal,
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
                && (
                  RegNextWhen(
                    next=True,
                    cond=cFront.up.isFiring,
                    init=False,
                  )
                )
              ) {
                assume(
                  inp.opCnt
                  === RegNextWhen(
                    next=outp.opCnt,
                    cond=cFront.up.isFiring,
                    init=outp.opCnt.getZero,
                  ) + 1
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
                RegNext(
                  next=nextSetOutpState,
                  init=nextSetOutpState.getZero,
                )
              )
              .setName(s"rSetOutpState")
            )
            nextSetOutpState := rSetOutpState

            outp := (
              RegNext(
                next=outp,
                init=outp.getZero,
              )
            )
            outp.allowOverride
            val myCurrOp = (
              KeepAttribute(
                cloneOf(inp.op)
              )
              .setName(s"myCurrOp")
            )
            myCurrOp := (
              RegNext(
                next=myCurrOp,
              )
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
            when (cMid0Front.up.isValid ) {
              when (!rSetOutpState) {
                outp := inp
                myCurrOp := inp.op
                nextSetOutpState := True
              }
              when (cMid0Front.up.isFiring) {
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
                val savedPsExStallHost = (
                  PipeMemRmwSimDut.haveModOpMul
                ) generate (
                  LcvStallHostSaved(
                    stallHost=psExStallHost,
                    someLink=cMid0Front,
                  )
                )
                val savedPsMemStallHost = (
                  LcvStallHostSaved(
                    stallHost=psMemStallHost,
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
                  RegNext(
                    next=doCheckHazard,
                    init=doCheckHazard.getZero,
                  )
                )
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
                    //!savedPsMemStallHost
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
                      next=myCurrOp,
                      cond=cMid0Front.up.isFiring,
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
              someRdMemWord: UInt=myRdMemWord,
            ): Unit = {
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
            }
            val rSavedRdMemWord = (
              Reg(cloneOf(myRdMemWord))
              init(myRdMemWord.getZero)
            )
              .setName("doModInModFrontFunc_rSavedRdMemWord")
            val rPrevOutp = KeepAttribute(
              RegNextWhen(
                next=outp,
                cond=cMid0Front.up.isFiring,
                init=outp.getZero,
              )
            )
              .setName("doModInModFrontFunc_rPrevOutp")

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
              def handleCurrFire(
                someRdMemWord: UInt=myRdMemWord,
              ): Unit = {
                outp.myExt.valid := True
                nextPrevTxnWasHazard := False
                setOutpModMemWord(
                  someRdMemWord=someRdMemWord
                )
              }
              def handleDuplicateIt(
                someModMemWordValid: Bool=False,
              ): Unit = {
                outp.myExt.valid := False
                outp.myExt.modMemWordValid := (
                  someModMemWordValid
                )
                cMid0Front.duplicateIt()
              }
              def myDoHaveHazardAddrCheck = (
                doTestModOpMainArea.myDoHaveHazardAddrCheck
              )
              def myDoHaveHazardValidCheck = (
                doTestModOpMainArea.myDoHaveHazardValidCheck
              )
              def myDoHaveHazard = (
                doTestModOpMainArea.myDoHaveHazard
              )
              def rTempPrevOp = (
                doTestModOpMainArea.rTempPrevOp
              )
              def savedPsExStallHost = (
                doTestModOpMainArea.savedPsExStallHost
              )
              def savedPsMemStallHost = (
                doTestModOpMainArea.savedPsMemStallHost
              )
              //--------
              when (doCheckHazard) {
                when (myDoHaveHazard) {
                  currDuplicateIt := True
                }
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
              //--------
              when (
                //savedPsMemStallHost.eitherSavedFire
                psMemStallHost.fire
                //psMemStallHost.rValid
                //&& psMemStallHost.ready
                //psMemStallHost.valid
                //&& RegNext(
                //  next=psMemStallHost.ready,
                //  init=psMemStallHost.ready.getZero,
                //)
              ) {
                psMemStallHost.nextValid := False
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
                      //psMemStallHost.valid := True
                      psMemStallHost.nextValid := (
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
                        //savedPsExStallHost
                        doCheckHazard
                      ) {
                        when (!currDuplicateIt) {
                          psExStallHost.nextValid := (
                            True
                          )
                        }
                      } otherwise { // when (!doCheckHazard)
                        psExStallHost.nextValid := (
                          True
                        )
                      }
                      //--------
                      when (savedPsExStallHost.myDuplicateIt) {
                        currDuplicateIt := True
                      }
                    }
                  }
                  //is (PipeMemRmwSimDut.ModOp.BEQ_RA_SIMM) {
                  //}
                }
              }
              if (PipeMemRmwSimDut.haveModOpMul) {
                when (
                  psExStallHost.fire
                  //psExStallHost.rValid
                  //&& psExStallHost.ready
                  //psExStallHost.valid
                  //&& RegNext(
                  //  next=psExStallHost.ready,
                  //  init=psExStallHost.ready.getZero,
                  //)
                ) {
                  psExStallHost.nextValid := False
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
              .otherwise {
                when (
                  if (optModHazardKind == PipeMemRmw.modHazardKindDupl) (
                    outp.myExt.hazardId.msb
                  ) else (
                    True
                  )
                ) {
                  if (
                    PipeMemRmwSimDut.doTestModOp
                  ) {
                    doTestModOpMainArea.doCheckHazard := False
                  } else {
                    when (cMid0Front.up.isValid) {
                      setOutpModMemWord()
                    }
                  }
                }
              }
              if (PipeMemRmwSimDut.doTestModOp) {
                doTestModOpMain(
                  //doCheckHazard=true
                )
              }
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
      Reg(dataType=PipeMemRmwSimDut.wordType())
    )
  )
  val rDidInitstate = Reg(
    dataType=Bool(),
    init=False,
  )
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
          && !myDbgInitstate
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
          && !myDbgInitstate
          && (
            myResetCond
          )
        ),
        length=myProveNumCycles,
        when=(
          tempHadFrontIsFiring._1
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
          && !myDbgInitstate
          && (
            myResetCond
          )
        ),
        length=myProveNumCycles,
        when=(
          tempHadMid0FrontUpIsFiring._1
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
    )
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
    //--------
    val tempRight = (
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
            (tempHaveCurrWritePostFirst._2)
          ),
        )
        //init(pipeMem.mod.back.myWriteData(0).getZero)
      )
      .setName(s"tempRight")
    )
    val rPrevOpCnt = (
      RegNextWhen(
        next=modBack(modBackPayload).opCnt,
        //modBack.isFiring && pipeMem.mod.back.myWriteEnable(0)
        cond=myHaveCurrWrite,
      )
      init(0x0)
    )
      .setName("rPrevOpCnt")
    assumeInitial(
      rPrevOpCnt === 0x0
    )
    val myCoverCond = (
      //modBack.isFiring
      //&& pipeMem.mod.back.myWriteEnable(0)
      myHaveCurrWrite
    )
    def myCoverVecSize = 8
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

    when (pastValidAfterReset()) {
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
}
case class PipeMemRmwTester(
  optFormal: Boolean,
) extends Component {
  //def doFormal: Boolean = {
  //  GenerationFlags.formal {
  //    return true
  //  }
  //  return false
  //}
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
  val dut: PipeMemRmwSimDut = (
    PipeMemRmwSimDut(
      optFormal=(
        //doFormal
        optFormal
      )
    )
  )
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
  //--------
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

    val midModPayload = (
      Vec.fill(extIdxLim)(
        PipeMemRmwSimDutModType()
      )
      .setName("midModPayload")
    )
    pmIo.tempModFrontPayload(0) := midModPayload(extIdxUp)
    midModPayload(extIdxSaved) := (
      RegNextWhen(
        next=midModPayload(extIdxUp),
        cond=cMidModFront.up.isFiring,
        init=midModPayload(extIdxSaved).getZero,
      )
    )
    for (extIdx <- 0 until extIdxLim) {
      if (extIdx != extIdxSaved) {
        midModPayload(extIdx) := (
          RegNext(
            next=midModPayload(extIdx),
            init=midModPayload(extIdx).getZero,
          )
        )
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
        RegNext(
          next=nextSetMidModPayloadState,
          init=nextSetMidModPayloadState.getZero,
        )
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
    val savedPsMemStallHost = (
      PipeMemRmwSimDut.doTestModOp
    ) generate (
      //dut.psMemStallHost.mkSaved(
      //  someLink=cMidModFront,
      //  myName=s"cMidModFront",
      //)
      LcvStallHostSaved(
        stallHost=dut.psMemStallHost,
        someLink=cMidModFront,
      )
    )
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
      Reg(
        dataType=cloneOf(pmIo.modFront(modFrontPayload).myExt.modMemWord),
        init=pmIo.modFront(modFrontPayload).myExt.modMemWord.getZero,
      )
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
    def myProveNumCycles = PipeMemRmwFormal.myProveNumCycles
    if (PipeMemRmwSimDut.doTestModOp) {
      cover(
        (
          RegNextWhen(
            next=RegNextWhen(
              next=True,
              cond=(
                (
                  midModPayload(extIdxUp).op
                  //pmIo.modFront(modFrontPayload).op
                  === PipeMemRmwSimDut.ModOp.LDR_RA_RB
                ) && (
                  cMidModFront.up.isFiring
                )
              ),
              init=False,
            ),
            cond=(
              (
                midModPayload(extIdxUp).op
                //pmIo.modFront(modFrontPayload).op
                === PipeMemRmwSimDut.ModOp.ADD_RA_RB
              ) && (
                cMidModFront.up.isFiring
              )
            ),
            init=False,
          )
        )
      )

      when (
        cMidModFront.up.isValid
        && (
          midModPayload(extIdxUp).op
          //pmIo.modFront(modFrontPayload).op
          === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        )
      ) {
          //--------
        when (savedPsMemStallHost.myDuplicateIt) {
          //--------
          cMidModFront.duplicateIt()
          //--------
          midModPayload(extIdxUp).myExt.modMemWordValid := False
          //--------
        } otherwise {
          //--------
          midModPayload(extIdxUp).myExt.modMemWordValid := True
          //--------
        }
        //--------
      }
      when (
        dut.psMemStallHost.fire
        && dut.psMemStallHost.rValid
      ) {
        midModPayload(extIdxUp).myExt.modMemWordValid := True
        midModPayload(extIdxUp).myExt.modMemWord := (
          if (PipeMemRmwSimDut.allModOpsSameChange) (
            midModPayload(extIdxUp).myExt.rdMemWord(0) + 1
          ) else (
            midModPayload(extIdxUp).myExt.rdMemWord(0) - 1
          )
        )
      }
      cover(
        dut.psMemStallHost.fire
        && dut.psMemStallHost.rValid
        //&& pmIo.tempModFrontPayload(0).myExt.modMemWordValid
      )
    }

    def setMidModStages(): Unit = {
      pmIo.midModStages(0)(0) := midModPayload
    }
    setMidModStages()

    pmIo.modFront(modBackPayload) := midModPayload(extIdxUp)

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
