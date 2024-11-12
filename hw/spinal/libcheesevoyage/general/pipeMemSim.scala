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
  val dcacheHit = (
    // for the purposes of testing `PipeMemRmw`, this can just be the value
    // that was driven into the `io.front(io.frontPayload)`, as it is
    // determined in `MEM` in the real CPU anyway.
    (PipeMemRmwSimDut.doTestModOp) generate (
      KeepAttribute(Bool())
    )
  )
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
                < PipeMemRmwSimDut.ModOp.LIM.asBits.asUInt
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
            def myRdMemWord = (
              doModInModFrontParams.getMyRdMemWordFunc(ydx)
            )
            def ydx = doModInModFrontParams.ydx
            assume(
              inp.op.asBits.asUInt
              < PipeMemRmwSimDut.ModOp.LIM.asBits.asUInt
            )

            outp := inp
            outp.allowOverride
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
            def doMulHaltItFsmIdleInnards(
              doDuplicateIt: Boolean
            ): Unit = {
              if (PipeMemRmwSimDut.doTestModOp) {
                def myInitMulHaltItCnt = 0x1
                cMid0Front.duplicateIt()
                when (
                  //cMid0Front.down.isFiring
                  modFront.isFiring
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
                outp := (
                  RegNext(outp)
                  init(outp.getZero)
                )
                when ((rMulHaltItCnt - 1).msb) {
                  when (
                    //cMid0Front.down.isFiring
                    modFront.isFiring
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
              doCheckHazard: Boolean//=false
            ): Unit = {
              val myFindFirstHazardAddr = (doCheckHazard) generate (
                KeepAttribute(
                  inp.myExt.memAddr.sFindFirst(
                    _ === rPrevOutp.myExt.memAddr(PipeMemRmw.modWrIdx)
                  )
                  //(
                  //  // Only check one register.
                  //  // This will work fine for testing the different
                  //  // categories of stalls, but the real CPU will need to
                  //  /// be tested for *all* registers
                  //  inp.myExt.memAddr(PipeMemRmw.modWrIdx)
                  //  === rPrevOutp.myExt.memAddr(PipeMemRmw.modWrIdx)
                  //)
                  .setName("myFindFirstHazardAddr")
                )
              )
              def doHandleHazardWithDcacheMiss(
                haveCurrLoad: Boolean,
              ): Unit = {
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
                  actuallyDuplicateIt: Boolean=true
                ): Unit = {
                  outp := (
                    RegNext(outp) init(outp.getZero)
                  )
                  outp.myExt.valid := False
                  outp.myExt.modMemWordValid := (
                    False
                  )
                  if (actuallyDuplicateIt) {
                    cMid0Front.duplicateIt()
                  }
                }
                val rState = KeepAttribute(
                  Reg(Bool())
                  init(False)
                )
                  .setName(
                    s"doHandleHazardWithDcacheMiss"
                    + s"_${doCheckHazard}_${haveCurrLoad}"
                    + s"_rState"
                  )
                val rSavedRdMemWord1 = (
                  (
                    //Reg(cloneOf(myRdMemWord))
                    //init(myRdMemWord.getZero)
                    Reg(
                      Flow(
                        PipeMemRmwSimDut.wordType()
                      )
                    )
                  )
                  .setName(
                    s"doModInModFrontFunc"
                    + s"_${doCheckHazard}_${haveCurrLoad}"
                    + s"_rSavedRdMemWord1"
                  )
                )
                rSavedRdMemWord1.init(rSavedRdMemWord1.getZero)
                val rTempPrevOp = (
                  KeepAttribute(
                    RegNextWhen(
                      inp.op, cMid0Front.up.isFiring
                    )
                  )
                  .setName(
                    s"doHandleHazardWithDcacheMiss"
                    + s"_${doCheckHazard}_${haveCurrLoad}"
                    + s"_rTempPrevOp"
                  )
                )
                if (haveCurrLoad) {
                  cover(rState === True)
                }
                switch (rState) {
                  is (False) {
                    //when (cMid0Front.up.isValid) {
                      when (
                        !tempModFrontPayload.dcacheHit
                        //&& (
                        //  if (haveCurrLoad) (
                        //    modFront.isValid
                        //  ) else (
                        //    False
                        //  )
                        //)
                      ) {
                        //if (haveCurrLoad) {
                        //  when (
                        //    modFront.isValid
                        //  ) {
                        //  } otherwise {
                        //    handleDuplicateIt()
                        //  }
                        //} else {

                        when (
                          modFront.isValid
                        ) {
                          //when (
                          //   rTempPrevOp
                          //   === (
                          //    PipeMemRmwSimDut.ModOp.LDR_RA_RB
                          //  )
                          //) {
                          if (haveCurrLoad) {
                            //cMid0Front.duplicateIt()
                            handleDuplicateIt()
                            //assert(!rSavedRdMemWord1.valid)
                            rSavedRdMemWord1.valid := False
                            rSavedRdMemWord1.payload := myRdMemWord
                            rState := True
                          } else {  // if (!haveCurrLoad)
                          //} otherwise {
                            when (modFront.isFiring) {
                              handleCurrFire()
                            }
                          }
                        } otherwise {
                          handleDuplicateIt()
                        }
                      } otherwise { // when (tempModFrontPayload.dcacheHit)
                        when (cMid0Front.up.isFiring) {
                          //when (
                          //  (
                          //    (
                          //      RegNext(rState) init(False)
                          //    ) === False
                          //  ) && (
                          //    rTempPrevOp
                          //    === PipeMemRmwSimDut.ModOp.LDR_RA_RB
                          //  )
                          //) {
                          //  handleCurrFire()
                          //} otherwise {
                          //  handleCurrFire(
                          //  )
                          //}
                          when (rSavedRdMemWord1.valid) {
                            rSavedRdMemWord1.valid := False
                            handleCurrFire(
                              someRdMemWord=rSavedRdMemWord1.payload
                            )
                          } otherwise {
                            handleCurrFire()
                          }
                        }
                      }
                    //}
                    when (pastValidAfterReset) {
                      when (past(rState) === True) {
                        assert(stable(rSavedRdMemWord1.payload))
                        if (haveCurrLoad) {
                          assert(
                            rSavedRdMemWord1.valid
                          )
                        }
                      }
                    }
                  }
                  is (True) {
                    //when (cMid0Front.down.isFiring) {
                    //}
                    //when (
                    //  cMid0Front.down.isFiring
                    //) {
                    //  when (cMid0Front.up.isFiring) {
                    //    rState := False
                    //    handleCurrFire(
                    //      someRdMemWord=rSavedRdMemWord1
                    //    )
                    //  }
                    //} otherwise {
                    //  handleDuplicateIt(actuallyDuplicateIt=true)
                    //}

                    //when (
                    //  cMid0Front.down.isFiring
                    //)
                    when (pastValidAfterReset) {
                      when (past(rState) === False) {
                        assert(!past(tempModFrontPayload.dcacheHit))
                        assert(!rSavedRdMemWord1.valid)
                      } otherwise {
                        assert(
                          stable(rSavedRdMemWord1)
                        )
                      }
                    }
                    //val nextHadDownFire = (
                    //  KeepAttribute(
                    //    Bool()
                    //  )
                    //  .setName(s"${}")
                    //)
                    if (haveCurrLoad) {
                      cover(
                        cMid0Front.down.isFiring
                      )
                    }
                    when (
                      cMid0Front.down.isFiring
                    ) {
                      rState := True
                      when (cMid0Front.up.isFiring) {
                        handleCurrFire(
                          someRdMemWord=rSavedRdMemWord1.payload
                        )
                      } otherwise {
                        rSavedRdMemWord1.valid := True
                      }
                    } otherwise {
                      handleDuplicateIt(actuallyDuplicateIt=true)
                    }

                    //when (
                    //  cMid0Front.up.isFiring
                    //) {
                    //  rState := False
                    //  handleCurrFire(
                    //    someRdMemWord=rSavedRdMemWord1
                    //  )
                    //}
                    //.otherwise 
                    ////when (!cMid0Front.down.isReady) 
                    //{
                    //  handleDuplicateIt(actuallyDuplicateIt=false)
                    //  //outp := (
                    //  //  RegNext(outp) init(outp.getZero)
                    //  //)
                    //  //cMid0Front.duplicateIt()
                    //}
                  }
                }
              }
              when (cMid0Front.up.isValid) {
                switch (inp.op) {
                  is (PipeMemRmwSimDut.ModOp.ADD_RA_RB) {
                    if (!doCheckHazard) {
                      //when (cMid0Front.up.isValid) {
                        setOutpModMemWord()
                      //}
                    } else { // if (doCheckHazard)
                      doHandleHazardWithDcacheMiss(
                        haveCurrLoad=false,
                      )
                    }
                  }
                  is (PipeMemRmwSimDut.ModOp.LDR_RA_RB) {
                    if (!doCheckHazard) {
                      when (cMid0Front.up.isFiring) {
                        setOutpModMemWord()
                        nextPrevTxnWasHazard := True
                      }
                    } else { // if (doCheckHazard)
                      nextPrevTxnWasHazard := True
                      doHandleHazardWithDcacheMiss(
                        haveCurrLoad=true,
                      )
                    }
                  }
                  is (PipeMemRmwSimDut.ModOp.MUL_RA_RB) {
                    // we should stall `EX` in this case until the
                    // calculation is done. The same stalling logic
                    // will be used for `divmod`, etc.
                    switch (rHaltItState) {
                      is (PipeMemRmwSimDutHaltItState.IDLE) {
                        doMulHaltItFsmIdleInnards(
                          doDuplicateIt=(
                            //true
                            doCheckHazard
                          )
                        )
                      }
                      is (PipeMemRmwSimDutHaltItState.HALT_IT) {
                        doMulHaltItFsmHaltItInnards()
                        when (
                          nextHaltItState
                          === PipeMemRmwSimDutHaltItState.IDLE
                        ) {
                          nextPrevTxnWasHazard := False
                        }
                      }
                    }
                  }
                }
              }
            }
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
              doTestModOpMain(
                doCheckHazard=true
              )
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
                  doTestModOpMain(
                    doCheckHazard=false
                  )
                } else {
                  when (cMid0Front.up.isValid) {
                    setOutpModMemWord()
                  }
                }
              }
            }
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
  val tempLeft = (
    RegNextWhen(
      modBack(modBackPayload).myExt.modMemWord,
      modBack.isFiring,
    ) init(0x0)
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

  //val savedModMemWord = Payload(PipeMemRmwSimDut.wordType())
  val myHaveCurrWrite = (
    KeepAttribute(
      pastValidAfterReset
      && modBack.isFiring
      && pipeMem.mod.back.myWriteEnable(0)
    )
    .setName(s"myHaveCurrWrite")
  )

  when (pastValidAfterReset) {
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

      RegNextWhen(
        pipeMem.modMem(0)(0).readAsync(
          address=(
            //RegNextWhen(
              //modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx)
              pipeMem.cBackArea.upExt(1)(0)(
                PipeMemRmw.extIdxSingle
              ).memAddr(0),
            //  modBack.isFiring,
            //) init(0x0)
          )
          //RegNextWhen(
          //  modFront(modFrontPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
          //  modFront.isFiring
          //)
        ),
        modBack.isFiring
      ) init(rSavedModArr(0).getZero)
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
    val rMyCoverVec = (
      Vec.fill(myCoverVecSize)(
        Reg(/*Flow*/(PipeMemRmwSimDutModType()))
      )
    )
    for (idx <- 0 until rMyCoverVec.size) {
      val tempMyCoverInit = PipeMemRmwSimDutModType()
      tempMyCoverInit.allowOverride
      tempMyCoverInit := tempMyCoverInit.getZero
      tempMyCoverInit.op := PipeMemRmwSimDut.ModOp.LIM
      rMyCoverVec(idx).init(
        //rMyCoverVec
        //PipeMemRmwSimDut.ModOp.LIM
        //rMyCoverVec(idx).getZero
        tempMyCoverInit
      )
      when (myCoverCond) {
        if (idx == 0) {
          rMyCoverVec(idx) := modBack(modBackPayload)
        } else {
          rMyCoverVec(idx) := rMyCoverVec(idx - 1)
        }
      }
    }
    cover(
      (
        rMyCoverVec(0).op === PipeMemRmwSimDut.ModOp.ADD_RA_RB
      ) && (
        rMyCoverVec(1).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        && !rMyCoverVec(1).dcacheHit
      ) && (
        rMyCoverVec(2).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        && !rMyCoverVec(2).dcacheHit
      )
    )
    cover(
      (
        rMyCoverVec(0).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        && !rMyCoverVec(0).dcacheHit
      ) && (
        rMyCoverVec(1).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        && rMyCoverVec(1).dcacheHit
      ) && (
        rMyCoverVec(2).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        && !rMyCoverVec(2).dcacheHit
      )
    )
    cover(
      (
        rMyCoverVec(0).op === PipeMemRmwSimDut.ModOp.MUL_RA_RB
        //&& !rMyCoverVec(0).dcacheHit
      ) && (
        rMyCoverVec(1).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        && !rMyCoverVec(1).dcacheHit
      ) && (
        rMyCoverVec(2).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        && rMyCoverVec(2).dcacheHit
      )
    )
    cover(
      (
        rMyCoverVec(0).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        && !rMyCoverVec(0).dcacheHit
      ) && (
        rMyCoverVec(1).op === PipeMemRmwSimDut.ModOp.MUL_RA_RB
        //&& !rMyCoverVec(0).dcacheHit
      ) && (
        rMyCoverVec(2).op === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        && !rMyCoverVec(2).dcacheHit
      )
    )
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
    val nextDidFirstOpCntInc = (
      KeepAttribute(
        Bool()
      )
      .setName("nextDidFirstOpCntInc")
    )
    val rDidFirstOpCntInc = (
      KeepAttribute(
        RegNext(
          //Bool()
          nextDidFirstOpCntInc
        )
        init(False)
      )
      .setName("rDidFirstOpCntInc")
    )
    when (pastValidAfterReset()) {
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
        past(myHaveCurrWrite)
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
        when (pipeMem.cBackArea.up.isValid) {
          assert(
            /*past*/(tempLeft) //+ 1
            === (
              //mySavedMod
              /*past*//*past*/(tempRight) + 1 //+ 1
              /*past*///(mySavedMod)
              //mySavedMod + 1
            )
          )
        }
      }
    } otherwise {
      //assume(
      //  RegNext(rDidFirstOpCntInc) === False
      //)
      assume(
        nextDidFirstOpCntInc === False
      )
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
      ) && (
        RegNextWhen(
          (
            RegNextWhen(
              True,
              back.isFiring,
            ) init(False)
          ),
          back.isFiring,
        ) init(False)
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
    < PipeMemRmwSimDut.ModOp.LIM.asBits.asUInt
  )
  assume(
    pmIo.modBack(modBackPayload).op.asBits.asUInt
    < PipeMemRmwSimDut.ModOp.LIM.asBits.asUInt
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
        Node()
      ),
    )
    pipeMem.myLinkArr += sMidModFront
    val s2mMidModFront = S2MLink(
      up=sMidModFront.down,
      down=(
        pmIo.modBack
        //Node()
      ),
    )
    pipeMem.myLinkArr += s2mMidModFront
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
    pmIo.tempModFrontPayload(0) := midModPayload(0)
    //midModPayload(extIdxUp).allowOverride
    val rDidFirstMidModRegNext = (
      Reg(Bool()) init(False)
    )
    for (extIdx <- 0 until extIdxLim) {
      if (extIdx != extIdxSaved) {
        //when (
        //  //RegNext(midModPayload)(extIdx).myExt.modMemWordValid
        //  cMidModFront.up.isValid
        //  && pmIo.modFront(modFrontPayload).myExt.modMemWordValid
        //  //|| !rDidFirstMidModRegNext
        //) {
          midModPayload(extIdx) := (
            RegNext(midModPayload)(extIdx)
            init(midModPayload(extIdx).getZero)
          )
        //} otherwise {
        //}
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
    when (cMidModFront.up.isValid) {
      //when (nextHaltItState === HaltItState.IDLE) {
        when (rHaltItState === HaltItState.IDLE) {
          midModPayload(extIdxUp) := pmIo.modFront(modFrontPayload)
        }
        //--------
        // BEGIN: TODO: verify that the below `when` statement works!
        when (
          pmIo.modFront(modFrontPayload).op
          === PipeMemRmwSimDut.ModOp.LDR_RA_RB
        ) {
          midModPayload(extIdxUp).myExt.modMemWord := (
            RegNext(midModPayload(extIdxUp).myExt.modMemWord)
            init(midModPayload(extIdxUp).myExt.modMemWord.getZero)
          )
        }
        // END: TODO: verify that the below line works!
        //--------
      //}
      if (PipeMemRmwSimDut.doTestModOp) {
        when (rHaltItState === HaltItState.IDLE) {
          //when (
          //  RegNext(midModPayload(extIdxUp)).myExt.memAddr(
          //    PipeMemRmw.modWrIdx
          //  ) === 0x1
          //  && RegNext(rHaltItState) === HaltItState.HALT_IT
          //  //&& cMidModFront
          //) {
          //  //cMidModFront.throwIt()
          //  midModPayload(extIdxUp).myExt.memAddr(
          //    PipeMemRmw.modWrIdx
          //  ) := RegNext(
          //    midModPayload(extIdxUp).myExt.memAddr(
          //      PipeMemRmw.modWrIdx
          //    )
          //  )
          //  midModPayload(extIdxUp).myExt.modMemWord := (
          //    //pmIo.modFront(modFrontPayload).myExt.rdMemWord(
          //    //  PipeMemRmw.modWrIdx
          //    //) + 2
          //    rSavedModMemWord
          //  )
          //  midModPayload(extIdxUp).myExt.valid := False
          //  midModPayload(extIdxUp).myExt.ready := False
          //  midModPayload(extIdxUp).myExt.fire := False
          //} else
          when (
            (
              pmIo.modFront(modFrontPayload).op
              === PipeMemRmwSimDut.ModOp.LDR_RA_RB
            ) && (
              pmIo.modFront(modFrontPayload).myExt.modMemWordValid
            )
            //&& (
            //)
            ////&& RegNext(rHaltItState) =/= HaltItState.HALT_IT
            //&& rHaltItState === HaltItState.IDLE
            ////&& RegNext(rHaltItState) === HaltItState.IDLE
            ////RegNext(rAddrOneHaltItCnt.msb)
          ) {
            when (
              !pmIo.modFront(modFrontPayload).dcacheHit
              //&& (
              //  pmIo.modFront(modFrontPayload).myExt.memAddr(
              //    PipeMemRmw.modWrIdx
              //  ) === 0x1
              //)
            ) {
              nextHaltItState := HaltItState.HALT_IT
              nextAddrOneHaltItCnt := 0x1
              cMidModFront.haltIt()
              //cMidModFront.
              //midModPayload(extIdxUp).myExt.valid := False
              rSavedModMemWord := (
                myModMemWord
              )
              // prevent forwarding when we're switching states.
              midModPayload(extIdxUp).myExt.modMemWordValid := False
            } otherwise {
              midModPayload(extIdxUp).myExt.modMemWord := (
                myModMemWord
              )
              midModPayload(extIdxUp).myExt.modMemWordValid := True

              assert(stable(rSavedModMemWord))
            }
          }
        }
      }
      //midModPayload(extIdxSaved) := (
      //  RegNext(midModPayload(extIdxUp))
      //)
    } otherwise {
    }
    if (PipeMemRmwSimDut.doTestModOp) {
      when (rHaltItState === HaltItState.HALT_IT) {
        when (pastValidAfterReset) {
          when (past(rHaltItState) === HaltItState.IDLE) {
            assert(rSavedModMemWord === myModMemWord)
          }
        }
        midModPayload(extIdxUp) := (
          RegNext(midModPayload(extIdxUp))
          init(midModPayload(extIdxUp).getZero)
        )
        //midModPayload(extIdxUp).myExt.valid := True

        //when (cMidModFront.down.isFiring) {
          nextAddrOneHaltItCnt := rAddrOneHaltItCnt - 1
        //}

        //cMidModFront.haltIt()
        when ((rAddrOneHaltItCnt - 1).msb) {
          //midModPayload(extIdxUp).myExt.valid := True
          //when (cMidModFront.up.isFiring) {
            midModPayload(extIdxUp).myExt.modMemWord := (
              rSavedModMemWord //+ 0x1
            )
            //when (
            //  pmIo.modFront(modFrontPayload).myExt.modMemWordValid
            //) {
              midModPayload(extIdxUp).myExt.modMemWordValid := True
            //}
            nextHaltItState := HaltItState.IDLE
          //}
        } otherwise {
          //midModPayload(extIdxUp).myExt.valid := False
          cMidModFront.haltIt()
        }
      }
    }
    if (PipeMemRmwSimDut.doTestModOp) {
      when (
        //cMidModFront.up.isValid
        //&& RegNext(
        //  midModPayload(extIdxUp).myExt.memAddr(
        //    PipeMemRmw.modWrIdx
        //  ),
        //  //cMidModFront.up.isFiring
        //) === 0x1
        //&& 
        RegNext(rHaltItState) === HaltItState.HALT_IT
        && rHaltItState === HaltItState.IDLE
      ) {
        // let one through
        nextHaltItState := HaltItState.IDLE
        //midModPayload(extIdxUp).myExt.valid := False
        //midModPayload(extIdxUp).myExt.ready := False
        //midModPayload(extIdxUp).myExt.fire := False
      } otherwise {
        //midModPayload(extIdxUp).myExt.modMemWordValid := (
        //  True
        //  //midModPayload(extIdxUp).myExt.valid
        //)
      }
      when (
        rHaltItState === HaltItState.IDLE
        && cMidModFront.up.isValid
        && !pmIo.modFront(modFrontPayload).myExt.modMemWordValid
      ) {
        midModPayload(extIdxUp).myExt.modMemWordValid := False
      }
    }
    midModPayload(extIdxSaved) := (
      RegNextWhen(midModPayload(extIdxUp), cMidModFront.up.isFiring)
      init(midModPayload(extIdxSaved).getZero)
    )

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
      //modFrontStm.translateInto(
      //  into=modMidStm
      //)(
      //  dataAssignment=(
      //    modMidPayload,
      //    modFrontPayload,
      //  ) => {
      //    //modMidPayload.myExt := modFrontPayload.myExt
      //    modMidPayload := modFrontPayload
      //    modMidPayload.myExt.allowOverride
      //    when (modMidPayload.myExt.hazardId.msb) {
      //      modMidPayload.myExt.modMemWord := (
      //        //modFrontPayload.myExt.rdMemWord + 0x1
      //        modFrontPayload.myExt.rdMemWord(PipeMemRmw.modWrIdx) + 0x1
      //      )
      //    }
      //    //when (
      //    //  modFrontPayload.myExt.hazardId.msb
      //    //) {
      //    //  modMidPayload.myExt.dbgModMemWord := (
      //    //    modMidPayload.myExt.modMemWord
      //    //  )
      //    //} otherwise {
      //    //  modMidPayload.myExt.dbgModMemWord := 0x0
      //    //}
      //    if (modStageCnt > 0) {
      //      ////dut.io.midModStages(0)(0) := (
      //      ////  RegNext(dut.io.midModStages(0)(0))
      //      ////  init(dut.io.midModStages(0)(0).getZero)
      //      ////)
      //      ////dut.io.midModStages(0)(0).myExt.valid.allowOverride
      //      ////dut.io.midModStages(0)(0).myExt.valid := modFrontStm.valid
      //      ////when (modFrontStm.valid) {
      //      ////  dut.io.midModStages(0)(0) := modMidPayload
      //      ////  //dut.io.midModStages(0)(0).valid.allowOverride
      //      ////  //dut.io.midModStages(0)(0).valid := True
      //      ////}
      //      ////dut.io.midModStages(0)(0) := (
      //      ////  RegNext(dut.io.midModStages(0)(0))
      //      ////  init(dut.io.midModStages(0)(0).getZero)
      //      ////)
      //      //dut.io.midModStages(0)(0).myExt.valid.allowOverride
      //      ////when (modFrontStm.valid) {
      //      //  dut.io.midModStages(0)(0) := modFrontStm.payload
      //      ////}
      //      //dut.io.midModStages(0)(0).myExt.valid := modFrontStm.valid
      //      dut.io.midModStages(0)(0) := (
      //        RegNext(dut.io.midModStages(0)(0))
      //        init(dut.io.midModStages(0)(0).getZero)
      //      )
      //      dut.io.midModStages(0)(0).myExt.valid.allowOverride
      //      when (
      //        //modFrontStm.valid
      //        //&& modMidStm.ready
      //        modFrontStm.fire
      //      ) {
      //        dut.io.midModStages(0)(0) := modFrontStm.payload
      //      }
      //      dut.io.midModStages(0)(0).myExt.valid := modFrontStm.valid
      //    }
      //  }
      //)
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
