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



//object PipeMemRmwSimDutHaltItState extends SpinalEnum(
//  defaultEncoding=binarySequential
//) {
//  val
//    IDLE,
//    HALT_IT
//    = newElement();
//}
object PipeMemRmwSimDut {
  def wordWidth = (
    //4
    //8
    //16
    32
  )
  def wordType() = UInt(wordWidth bits)
  def wordCount = (
    //4
    //8
    16
    //32
  )
  def hazardCmpType() = UInt(
    PipeMemRmw.addrWidth(wordCount=wordCount) bits
  )
  def modRdPortCnt = (
    1
    //2
  )
  def modStageCnt = (
    if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
      //2
      1
    ) else (
      //0
      1
    )
  )
  def memArrSize = (
    1
  )
  def pipeName = (
    "PipeMemRmw_FormalDut"
  )
  //def doAddrOneHaltIt = (
  //  optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
  //  //false
  //)
  def doTestModOp = (
    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
    //false
  )
  def modOpCntWidth = (
    4
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
      AddRaRb,
      //BRANCH_MISPREDICT,     // `EX` should send a bubble downstream  
      //LDR_RA_RB_DCACHE_HIT,
      //  // no MEM stall for this load instruction, but could cause a 
      //  // stall in EX for the next instruction if the next instruction
      //  // uses this instruction's `rA`
      //LDR_RA_RB_DCACHE_MISS,
      //  // Stall in MEM if
      LdrRaRb,
      //StrRaRb,
      MulRaRb,               // stall in EX
      //BeqRaSimm,
      //JzRaRb, // jump if (rA === zero) to address in rB
      //JnzRaRb, // jump if (rA =/= 0) to address in rB
      //BranchTaken,
      //BranchNotTaken,
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
  def optModHazardKind: PipeMemRmw.ModHazardKind = (
    //PipeMemRmw.ModHazardKind.Dupl
    PipeMemRmw.ModHazardKind.Fwd
  )
  def optReorder = (
    false
  )
  //def myHaveFormalFwd = (
  //  optFormal
  //  && (
  //    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
  //  )
  //)
  //def optModFwdToFront = (
  //  //optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
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
  case class PsIfStallIoHostPayload(
  ) extends Bundle {
    val addr = UInt(log2Up(wordCount) bits)
  }
  case class PsIfStallIoDevPayload(
  ) extends Bundle {
    val instr = ModOp()
  }
  //case class PsExStallIoHostPayload(
  //) extends Bundle {
  //}
  //case class PsExStallIoDevPayload(
  //) extends Bundle {
  //}
  case class PsMemStallIoHostPayload(
  ) extends Bundle {
  }
  case class PsMemStallIoDevPayload(
  ) extends Bundle {
  }
}
case class PipeMemRmwSimDutModType(
  //optIncludeDcacheHit: Boolean=false
  cfg: PipeMemRmwConfig[UInt, UInt],
  optModHazardKind: PipeMemRmw.ModHazardKind,
  optFormal: Boolean,
) extends /*Bundle with*/ PipeMemRmwPayloadBase[UInt, UInt]
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
  val finishedOp = (
    (PipeMemRmwSimDut.doTestModOp) generate (
      KeepAttribute(Bool())
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
  def mkOneExt() = (
    PipeMemRmwPayloadExt(
      //wordType=PipeMemRmwSimDut.wordType(),
      cfg=cfg,
      wordCount=PipeMemRmwSimDut.wordCount,
      //hazardCmpType=PipeMemRmwSimDut.hazardCmpType(),
      //modRdPortCnt=PipeMemRmwSimDut.modRdPortCnt,
      //modStageCnt=PipeMemRmwSimDut.modStageCnt,
      //memArrSize=PipeMemRmwSimDut.memArrSize,
      //optModHazardKind=PipeMemRmwSimDut.optModHazardKind,
      //optReorder=PipeMemRmwSimDut.optReorder,
    )
  )
  val myExt = Vec.fill(PipeMemRmwSimDut.memArrSize)(
    mkOneExt()
  )
  override def setPipeMemRmwExt(
    inpExt: PipeMemRmwPayloadExt[UInt, UInt],
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
    myExt(ydx) := inpExt
  }
  override def getPipeMemRmwExt(
    outpExt: PipeMemRmwPayloadExt[UInt, UInt],
    ydx: Int,
    memArrIdx: Int,
  ): Unit = {
    outpExt := myExt(ydx)
  }
  def myHaveFormalFwd = (
    optFormal
    && (
      optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
    )
  )
  val myFwd = (
    myHaveFormalFwd
  ) generate (
    PipeMemRmwFwd[
      UInt,
      UInt,
    ](
      //wordType=PipeMemRmwSimDut.wordType(),
      //wordCount=PipeMemRmwSimDut.wordCount,
      //hazardCmpType=PipeMemRmwSimDut.hazardCmpType(),
      //modRdPortCnt=PipeMemRmwSimDut.modRdPortCnt,
      //modStageCnt=PipeMemRmwSimDut.modStageCnt,
      //memArrSize=PipeMemRmwSimDut.memArrSize,
      //optModHazardKind=PipeMemRmwSimDut.optModHazardKind,
      //optReorder=PipeMemRmwSimDut.optReorder,
      cfg=cfg
    )
  )
  override def formalSetPipeMemRmwFwd(
    inpFwd: PipeMemRmwFwd[UInt, UInt],
    memArrIdx: Int,
  ): Unit = {
    assert(
      myHaveFormalFwd
    )
    myFwd := inpFwd
  }
  override def formalGetPipeMemRmwFwd(
    outpFwd: PipeMemRmwFwd[UInt, UInt],
    memArrIdx: Int,
  ): Unit = {
    assert(
      myHaveFormalFwd
    )
    outpFwd := myFwd
  }
  //def optFormalFwdFuncs(
  //): Option[PipeMemRmwPayloadBaseFormalFwdFuncs[UInt, UInt]] = (
  //  Some(
  //    myFormalFwdFuncs
  //  )
  //)
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
case class PipeMemRmwSimDutIo(
  optFormal: Boolean,
) extends Area {
}

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
  def pipeName = (
    PipeMemRmwSimDut.pipeName
  )
  def optModHazardKind: PipeMemRmw.ModHazardKind = (
    PipeMemRmwSimDut.optModHazardKind
  )
  //def optModFwdToFront = (
  //  PipeMemRmwSimDut.optModFwdToFront
  //)
  def extIdxUp = PipeMemRmw.extIdxUp
  def extIdxSaved = PipeMemRmw.extIdxSaved
  def extIdxLim = PipeMemRmw.extIdxLim
  val pmCfg = PipeMemRmwConfig[
    UInt,
    UInt,
    //PipeMemRmwSimDutModType,
    //PipeMemRmwDualRdTypeDisabled[UInt, UInt],
  ](
    wordType=wordType(),
    wordCountArr=Array.fill(1)(wordCount).toSeq,
    hazardCmpType=hazardCmpType(),
    //modType=ModType(),
    modRdPortCnt=modRdPortCnt,
    modStageCnt=modStageCnt,
    pipeName=pipeName,
    //dualRdType=(
    //  //modType()
    //  PipeMemRmwDualRdTypeDisabled[UInt, UInt](),
    //),
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
  )
  def ModType() = PipeMemRmwSimDutModType(
    cfg=pmCfg,
    optModHazardKind=optModHazardKind,
    optFormal=optFormal,
  )
  //val io = PipeMemRmwSimDutIo()
  //val psMemStallHost = new Bundle {
  //  //val valid = Bool()
  //  val ready = Bool()
  //}
  //val io = new Area {
  //  //val tempHadFrontIsFiring_0 = (
  //  //  KeepAttribute(
  //  //    Bool()
  //  //  )
  //  //)
  //  val tempHadMid0FrontUpIsFiring_0 = (
  //    KeepAttribute(
  //      Bool()
  //    )
  //  )
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
  //--------
  //case class PsExStallHostPayload
  //val io = new Area {
  //  val 
  //}
  //--------
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
    cfg=pmCfg,
    modType=ModType(),
    dualRdType=(
      //modType()
      PipeMemRmwDualRdTypeDisabled[UInt, UInt](),
    ),
  )(
    doHazardCmpFunc=None,
    doPrevHazardCmpFunc=false,
    doModInFrontFunc=(
      if (!(
        optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
        && PipeMemRmwSimDut.doTestModOp
      )) {
        None
      } else {
        Some(
          (
            outp,
            inp,
            cFront,
            //ydx,
          ) => new Area {
            //GenerationFlags.formal {
              if (optFormal) {
                when (pastValidAfterReset) {
                  when (
                    cFront.up.isValid
                    //&& (
                    //  RegNextWhen(
                    //    next=True,
                    //    cond=cFront.up.isFiring,
                    //    init=False,
                    //  )
                    //)
                    && past(cFront.up.isFiring)
                  ) {
                    assume(
                      inp.opCnt
                      //=== RegNextWhen(
                      //  next=outp.opCnt,
                      //  cond=cFront.up.isFiring,
                      //  init=outp.opCnt.getZero,
                      //) + 1
                      === past(inp.opCnt) + 1
                    )
                  }
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
                    stable(inp.myExt(0).hazardCmp)
                  )
                  assert(
                    stable(inp.myExt(0).modMemWord)
                  )
                  assert(
                    stable(inp.myExt(0).rdMemWord)
                    //=== inp.myExt.rdMemWord.getZero
                  )
                  assert(
                    inp.myExt(0).rdMemWord
                    === inp.myExt(0).rdMemWord.getZero
                  )
                  assert(
                    stable(inp.myExt(0).hazardCmp)
                    //=== inp.myExt.hazardCmp.getZero
                  )
                  assert(
                    stable(inp.myExt(0).modMemWordValid) //=== True
                  )
                }
              }
            //}
          }
        )
      }
    ),
    doModInModFrontFunc=(
      if (optModHazardKind != PipeMemRmw.ModHazardKind.Fwd) (
        None
      ) else (
        Some(
          (
            doModInModFrontParams
          ) => new Area {
            def nextPrevTxnWasHazard = (
              doModInModFrontParams.nextPrevTxnWasHazardVec(0)
            )
            def rPrevTxnWasHazard = (
              doModInModFrontParams.rPrevTxnWasHazardVec(0)
            )
            def rPrevTxnWasHazardAny = (
              doModInModFrontParams.rPrevTxnWasHazardAny
            )
            def outp = doModInModFrontParams.outp//Vec(ydx)
            def inp = doModInModFrontParams.inp//Vec(ydx)
            def cMid0Front = doModInModFrontParams.cMid0Front
            def modFront = doModInModFrontParams.modFront
            def tempModFrontPayload = (
              doModInModFrontParams.tempModFrontPayload//Vec(ydx)
            )
            //def ydx = doModInModFrontParams.ydx
            def ydx = 0
            assume(
              inp.op.asBits.asUInt
              //< PipeMemRmwSimDut.ModOp.MUL_RA_RB.asBits.asUInt
              < PipeMemRmwSimDut.postMaxModOp.asBits.asUInt
            )
            assume(
              outp.op.asBits.asUInt
              //< PipeMemRmwSimDut.ModOp.MUL_RA_RB.asBits.asUInt
              < PipeMemRmwSimDut.postMaxModOp.asBits.asUInt
            )
            val myCurrOp = (
              KeepAttribute(
                cloneOf(inp.op)
              )
              .setName(s"myCurrOp")
            )
            myCurrOp := (
              RegNext(
                next=myCurrOp,
                //init=PipeMemRmwSimDut
              )
              init(
                //myCurrOp.getZero
                PipeMemRmwSimDut.ModOp.AddRaRb
              )
            )
            assume(
              myCurrOp.asBits.asUInt
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
            //if (PipeMemRmwSimDut.myHaveFormalFwd) {
            //}

            outp := (
              RegNext(
                next=outp,
                init=outp.getZero,
              )
            )
            outp.allowOverride
            //myCurrOp := (
            //  inp.op
            //)
            def myRdMemWord = (
              doModInModFrontParams.getMyRdMemWordFunc(
                ////Mux[UInt](
                ////  (
                ////    cMid0Front.up.isValid
                ////    && !rSetOutpState
                ////  ),
                //  //U(s"2'd1"),
                //  U(s"2'd2"),
                ////),
                ydx,
                PipeMemRmw.modWrIdx,
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
            } otherwise {
            }
            if (optFormal) {
              when (pastValidAfterReset) {
                when (
                  past(cMid0Front.up.isFiring) init(False)
                ) {
                  assert(
                    !rSetOutpState
                  )
                }
                when (
                  !(past(cMid0Front.up.isValid) init(False))
                ) {
                  assert(
                    stable(
                      rSetOutpState
                    )
                  )
                }
                //when (
                //  cMidModFront.up.isValid
                //  //&& (
                //  //  !RegNextWhen(
                //  //    next=cMid0Front.up.isFiring,
                //  //  )
                //  //)
                //) {
                //  //assert
                //}
                when (
                  rSetOutpState
                ) {
                  assert(
                    cMid0Front.up.isValid
                  )
                }
              }
            }
            if (optFormal) {
              assert(
                myCurrOp === outp.op
              )
              when (pastValidAfterReset) {
                when (
                  rose(rSetOutpState)
                  //!past(rSetMidModPayloadState)
                ) {
                  //assert(
                  //  midModPayload(extIdxUp).myExt.rdMemWord
                  //  === past(pmIo.modFront(modFrontPayload).myExt.rdMemWord)
                  //)
                  assert(
                    myCurrOp
                    === past(inp.op)
                  )
                  assert(
                    //midModPayload(extIdxUp).op
                    //=== past(pmIo.modFront(modFrontPayload).op)
                    outp.op
                    === past(inp.op)
                  )
                  assert(
                    //midModPayload(extIdxUp).opCnt
                    //=== past(pmIo.modFront(modFrontPayload).opCnt)
                    outp.opCnt
                    === past(inp.opCnt)
                  )
                }
              }
            }
            //outp.opCnt := inp.opCnt
            //outp.myExt.valid := (
            //  inp.myExt.valid
            //)
            //outp.myExt.ready := (
            //  inp.myExt.ready
            //)
            //outp.myExt.fire := (
            //  inp.myExt.fire
            //)
            outp.myExt(0).rdMemWord := (
              //myRdMemWord
              inp.myExt(0).rdMemWord
            )
            //when (
            //  cMid0Front.up.isReady
            //) {
            //  outp := inp
            //}
            //--------
            //val nextHaltItState = KeepAttribute(
            //  PipeMemRmwSimDutHaltItState()
            //).setName("doModInModFrontFunc_nextHaltItState")
            //val rHaltItState = KeepAttribute(
            //  RegNext(nextHaltItState)
            //  init(PipeMemRmwSimDutHaltItState.IDLE)
            //)
            //  .setName("doModInModFrontFunc_rHaltItState")
            //val nextMulHaltItCnt = SInt(4 bits)
            //  .setName("doModInModFrontFunc_nextMulHaltItCnt")
            //val rMulHaltItCnt = (
            //  RegNext(nextMulHaltItCnt)
            //  init(-1)
            //)
            //  .setName("doModInModFrontFunc_rMulHaltItCnt")
            //nextHaltItState := rHaltItState
            //nextMulHaltItCnt := rMulHaltItCnt
            //--------
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
                    outp.myExt(0).memAddr(0)
                    === tempModFrontPayload.myExt(0).memAddr(0)
                  )
                  .setName(
                    s"doTestModOpMainArea_"
                    + s"myDoHaveHazardAddrCheck"
                  )
                )
                val myDoHaveHazardValidCheck = (
                  KeepAttribute(
                    !tempModFrontPayload.myExt(0).modMemWordValid
                    //!savedPsMemStallHost
                  )
                  .setName(
                    s"doTestModOpMainArea_"
                    + s"myDoHaveHazardValidCheck"
                  )
                )
                //--------
                val nextDoHaveHazardState = (
                  KeepAttribute(
                    Bool()
                  )
                  .setName(
                    s"doTestModOpMainArea_"
                    + s"nextDoHaveHazardState"
                  )
                )
                //val rDoHaveHazardState = (
                //  KeepAttribute(
                //    RegNext(
                //      next=nextDoHaveHazardState,
                //      init=nextDoHaveHazardState.getZero,
                //    )
                //  )
                //  .setName(
                //    s"doTestModOpMainArea_"
                //    + s"rDoHaveHazardState"
                //  )
                //)
                //nextDoHaveHazardState := rDoHaveHazardState
                //--------
                //val myDoHaveHazardIsReadyCheck = (
                //  KeepAttribute(
                //    !cMid0Front.down.isReady
                //  )
                //  .setName(
                //    s"doTestModOpMainArea_"
                //    + s"myDoHaveHazardIsReadyCheck"
                //  )
                //)
                val myDoHaveHazard = (
                  KeepAttribute(
                    (
                      myDoHaveHazardAddrCheck
                    ) && (
                      myDoHaveHazardValidCheck
                    )
                    //&& (
                    //  rDoHaveHazardState
                    //)
                    //&& (
                    //  myDoHaveHazardIsReadyCheck
                    //)
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
                      PipeMemRmwSimDut.ModOp.AddRaRb
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
              outp.myExt(0).modMemWordValid := (
                True
              )
              //if (PipeMemRmwSimDut.allModOpsSameChange) {
              //} else {
                switch (myCurrOp) {
                  is (PipeMemRmwSimDut.ModOp.AddRaRb) {
                    outp.myExt(0).modMemWord := (
                      someRdMemWord + 0x1
                    )
                    //outp.myExt(0).modMemWordValid := (
                    //  True
                    //)
                  }
                  is (PipeMemRmwSimDut.ModOp.LdrRaRb) {
                    outp.myExt(0).modMemWord := (
                      //someRdMemWord //+ 0x1
                      0x0
                    )
                    outp.myExt(0).modMemWordValid := (
                      False
                    )
                  }
                  is (PipeMemRmwSimDut.ModOp.MulRaRb) {
                    outp.myExt(0).modMemWord := (
                      (
                        if (PipeMemRmwSimDut.allModOpsSameChange) (
                          someRdMemWord + 0x1
                        ) else (
                          (someRdMemWord << 1)(
                            outp.myExt(0).modMemWord.bitsRange
                          )
                        )
                      )                    )
                    //outp.myExt(0).modMemWordValid := (
                    //  True
                    //)
                  }
                  //if (PipeMemRmwSimDut.ModOp.BEQ_RA_SIMM) {
                  //  outp.myExt(0).modMemWord := (
                  //    0x0
                  //  )
                  //  outp.myExt(0).modMemWordValid := (
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
                //outp.myExt(0).valid := True
                nextPrevTxnWasHazard := False
                setOutpModMemWord(
                  someRdMemWord=someRdMemWord
                )
                outp.myExt(0).valid := (
                  outp.myExt(0).modMemWordValid
                )
              }
              def handleDuplicateIt(
                someModMemWordValid: Bool=False,
              ): Unit = {
                outp.myExt(0).valid := False
                outp.myExt(0).modMemWordValid := (
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
              //def nextDoHaveHazardState = (
              //  doTestModOpMainArea.nextDoHaveHazardState
              //)
              //def rDoHaveHazardState = (
              //  doTestModOpMainArea.rDoHaveHazardState
              //)
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
                //currDuplicateIt := (
                //  RegNext(
                //    next=currDuplicateIt,
                //    init=currDuplicateIt.getZero,
                //  )
                //)
                //switch (rDoHaveHazardState) {
                //  is (False) {
                //    when (myDoHaveHazard) {
                //      currDuplicateIt := True
                //      nextDoHaveHazardState := True
                //    }
                //  }
                //  is (True) {
                //    when (
                //      !myDoHaveHazard
                //    ) {
                //      currDuplicateIt := False
                //      nextDoHaveHazardState := False
                //    }
                //  }
                //}
                cover(
                  (
                    ///outp.myExt(0).memAddr(0)
                    //=== tempModFrontPayload.myExt(0).memAddr(0)
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
              when (
                //cMid0Front.up.isValid
                True
              ) {
                switch (myCurrOp) {
                  is (PipeMemRmwSimDut.ModOp.AddRaRb) {
                    if (optFormal) {
                      when (cMid0Front.up.isValid) {
                        when (
                          //(
                          //  RegNextWhen(
                          //    next=myCurrOp,
                          //    cond=cMid0Front.down.isFiring,
                          //  )
                          //  init(PipeMemRmwSimDut.ModOp.ADD_RA_RB)
                          //) =/= (
                          //  PipeMemRmwSimDut.ModOp.LDR_RA_RB
                          //)
                          !doCheckHazard
                        ) {
                          //assert(!savedPsMemStallHost.myDuplicateIt)
                          assert(!currDuplicateIt)
                        }
                      }
                    }
                  }
                  is (PipeMemRmwSimDut.ModOp.LdrRaRb) {
                    when (cMid0Front.up.isFiring) {
                      nextPrevTxnWasHazard := True
                      //psMemStallHost.valid := True
                      psMemStallHost.nextValid := (
                        //!outp.dcacheHit
                        True
                      )
                    }
                  }
                  is (PipeMemRmwSimDut.ModOp.MulRaRb) {
                    if (PipeMemRmwSimDut.haveModOpMul) {
                      //if (optFormal) {
                      //  when (
                      //    (
                      //      RegNextWhen(
                      //        next=myCurrOp,
                      //        cond=cMid0Front.down.isFiring,
                      //      )
                      //      init(PipeMemRmwSimDut.ModOp.ADD_RA_RB)
                      //    ) =/= (
                      //      PipeMemRmwSimDut.ModOp.LDR_RA_RB
                      //    )
                      //  ) {
                      //    assert(!savedPsMemStallHost.myDuplicateIt)
                      //  }
                      //}
                      //when (
                      //) {
                      //}
                      //--------
                      when (cMid0Front.up.isValid) {
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
                      }
                      when (savedPsExStallHost.myDuplicateIt) {
                        currDuplicateIt := True
                      }
                      if (optFormal) {
                        when (!doCheckHazard) {
                          when (!savedPsExStallHost.myDuplicateIt) {
                            assert(
                              !currDuplicateIt
                            )
                          }
                        }
                      }
                    }
                  }
                  //is (PipeMemRmwSimDut.ModOp.BEQ_RA_SIMM) {
                  //}
                }
              } otherwise {
                //outp.myExt(0).modMemWordValid := False
              }
              //if (optFormal) {
              //  when 
              //  assert
              //}
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
                  if (optModHazardKind == PipeMemRmw.ModHazardKind.Dupl) (
                    outp.myExt(0).hazardId.msb
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
  def frontPayload = pipeMem.io.frontPayload
  def modFront = pipeMem.io.modFront
  def modFrontPayload = pipeMem.io.modFrontPayload
  def modBack = pipeMem.io.modBack
  def modBackPayload = pipeMem.io.modBackPayload
  def back = pipeMem.io.back
  def backPayload = pipeMem.io.backPayload

  //val tempMemAddrPlus = (
  //  (
  //    (
  //      RegNextWhen(
  //        modBack(modBackPayload).myExt(0).memAddr(PipeMemRmw.modWrIdx),
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
      //modBack(modBackPayload).myExt(0).modMemWord,
      //modBack.isFiring,
      pipeMem.mod.back.myWriteData(0),
      //myHaveCurrWrite,
    )
    //init(pipeMem.mod.back.myWriteData(0).getZero)
  )

  //val nextSavedMod = 

  //val next
  //val tempRight = rSavedModArr(
  //  //modBack.myExt(0).memAddr(PipeMemRmw.modWrIdx)
  //  //RegNextWhen(
  //    //modFront(modFrontPayload).myExt(0).memAddr(PipeMemRmw.modWrIdx),
  //      RegNextWhen(
  //        modFront(modFrontPayload).myExt(0).memAddr(PipeMemRmw.modWrIdx),
  //        modFront.isFiring
  //      )
  //    //modBack(modBackPayload).myExt(0).memAddr(PipeMemRmw.modWrIdx),
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
  //--------
  //val myHistFrontIsFiring = (
  //  KeepAttribute{
  //    val thatWhen = (
  //      front.isFiring
  //    )
  //    History[Bool](
  //      that=(
  //        thatWhen
  //        //front.isFiring
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        //front.isFiring
  //        ////&& (
  //        ////  myResetCond
  //        ////)
  //        thatWhen
  //      ),
  //      init=False
  //    )
  //  }
  //  .setName("myHistFrontIsFiring")
  //)
  //val tempHadFrontIsFiring = (
  //  myHistFrontIsFiring.sFindFirst(
  //    _ === True
  //  )
  //)
  val tempHadFrontIsFiring: (Bool, Bool) = (
    RegNextWhen[Bool](
      next=True,
      cond=front.isFiring,
      init=False,
    ),
    null
  )
  when (
    !tempHadFrontIsFiring._1
  ) {
    //for (ydx <- 1 until myHistFrontIsFiring.size) {
    //  assume(
    //    myHistFrontIsFiring(ydx) === myHistFrontIsFiring(ydx).getZero
    //  )
    //}
  }
  //val myHistMid0FrontUpIsFiring = (
  //  KeepAttribute{
  //    val thatWhen = (
  //      pipeMem.cMid0FrontArea.up.isFiring
  //      && tempHadFrontIsFiring._1
  //    )
  //    History[Bool](
  //      that=(
  //        //pipeMem.cMid0FrontArea.up.isFiring
  //        //&& tempHadFrontIsFiring._1
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //        //pipeMem.cMid0FrontArea.up.isFiring
  //        thatWhen
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        //tempHadFrontIsFiring._1
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //        thatWhen
  //      ),
  //      init=False
  //    )
  //  }
  //  .setName(s"myHistMid0FrontUpIsFiring")
  //)
  //val tempHadMid0FrontUpIsFiring = (
  //  myHistMid0FrontUpIsFiring.sFindFirst(
  //    _ === True
  //  )
  //)
  //when (pastValidAfterReset) {
  //  when (!tempHadMid0FrontUpIsFiring._1) {
  //    for (ydx <- 1 until myHistMid0FrontUpIsFiring.size) {
  //      assume(
  //        myHistMid0FrontUpIsFiring(ydx)
  //        === myHistMid0FrontUpIsFiring(ydx).getZero
  //      )
  //    }
  //    assume(
  //      pipeMem.cMid0FrontArea.tempUpMod(0)(0).opCnt === 0x0
  //    )
  //  }
  //}
  //val myHistMid0FrontUpIsValid = (
  //  KeepAttribute{
  //    val thatWhen = (
  //      pipeMem.cMid0FrontArea.up.isValid
  //      && tempHadFrontIsFiring._1
  //    )
  //    History[Bool](
  //      that=(
  //        //pipeMem.cMid0FrontArea.up.isValid
  //        //&& tempHadFrontIsValid._1
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //        //pipeMem.cMid0FrontArea.up.isValid
  //        thatWhen
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        //tempHadFrontIsValid._1
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //        thatWhen
  //      ),
  //      init=False
  //    )
  //  }
  //  .setName(s"myHistMid0FrontUpIsValid")
  //)
  //val tempHadMid0FrontUpIsValid = (
  //  myHistMid0FrontUpIsValid.sFindFirst(
  //    _ === True
  //  )
  //)
  val tempHadMid0FrontUpIsValid: (Bool, Bool) = (
    {
      val cond = (
        pipeMem.cMid0FrontArea.up.isValid
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False,
      )
    },
    null
  )
  //--------
  //when (pastValidAfterReset) {
  //  when (!tempHadMid0FrontUpIsValid._1) {
  //    //for (ydx <- 1 until myHistMid0FrontUpIsValid.size) {
  //    //  assume(
  //    //    myHistMid0FrontUpIsValid(ydx)
  //    //    === myHistMid0FrontUpIsValid(ydx).getZero
  //    //  )
  //    //}
  //    assume(
  //      pipeMem.cMid0FrontArea.tempUpMod(0).opCnt === 0x0
  //    )
  //  }
  //}
  //--------
  //val myHistMid0FrontDownIsFiring = (
  //  KeepAttribute{
  //    val thatWhen = (
  //      pipeMem.cMid0FrontArea.down.isFiring
  //      && tempHadFrontIsFiring._1
  //      && tempHadMid0FrontUpIsValid._1
  //    )
  //    History[Bool](
  //      that=(
  //        //pipeMem.cMid0FrontArea.up.isFiring
  //        //&& tempHadFrontIsFiring._1
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //        //pipeMem.cMid0FrontArea.up.isFiring
  //        thatWhen
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        //tempHadFrontIsFiring._1
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //        thatWhen
  //      ),
  //      init=False
  //    )
  //  }
  //  .setName(s"myHistMid0FrontDownIsFiring")
  //)
  //val tempHadMid0FrontDownIsFiring = (
  //  myHistMid0FrontDownIsFiring.sFindFirst(
  //    _ === True
  //  )
  //)
  val tempHadMid0FrontDownIsValid: (Bool, UInt) = (
    {
      val cond = (
        pipeMem.cMid0FrontArea.down.isValid
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || pipeMem.cMid0FrontArea.up.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  val tempHadMid0FrontDownIsFiring: (Bool, UInt) = (
    {
      val cond = (
        pipeMem.cMid0FrontArea.down.isFiring
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || pipeMem.cMid0FrontArea.up.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  //when (pastValidAfterReset) {
  //  when (!tempHadMid0FrontDownIsFiring._1) {
  //    for (ydx <- 1 until myHistMid0FrontDownIsFiring.size) {
  //      assume(
  //        myHistMid0FrontDownIsFiring(ydx)
  //        === myHistMid0FrontDownIsFiring(ydx).getZero
  //      )
  //    }
  //    assume(
  //      pipeMem.cMid0FrontArea.tempUpMod(0)(0).opCnt === 0x0
  //    )
  //  }
  //}
  //val myHistModFrontIsValid = (
  //  KeepAttribute{
  //    val thatWhen = (
  //      modFront.isValid
  //      && tempHadFrontIsFiring._1
  //      && tempHadMid0FrontUpIsValid._1
  //    )
  //    History[Bool](
  //      that=(
  //        //modFront.isValid
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //        thatWhen
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        thatWhen
  //      ),
  //      init=False
  //    )
  //  }
  //  .setName(s"myHistModFrontIsValid")
  //)
  //val tempHadModFrontIsValid = (
  //  myHistModFrontIsValid.sFindFirst(
  //    _ === True
  //  )
  //)
  val tempHadModFrontIsValid: (Bool, Bool) = (
    {
      val cond = (
        modFront.isValid
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || pipeMem.cMid0FrontArea.up.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  //val myHistModFrontIsValid = (
  //  KeepAttribute{
  //    val thatWhen = (
  //      modFront.isValid
  //      && tempHadFrontIsFiring._1
  //      && tempHadMid0FrontUpIsValid._1
  //    )
  //    History[Bool](
  //      that=(
  //        //modFront.isFiring
  //        ////&& !myDbgInitstate
  //        ////&& (
  //        ////  myResetCond
  //        ////)
  //        thatWhen
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        thatWhen
  //      ),
  //      init=False
  //    )
  //  }
  //  .setName(s"myHistModFrontIsValid")
  //)
  //val tempHadModFrontIsValid = (
  //  myHistModFrontIsValid.sFindFirst(
  //    _ === True
  //  )
  //)
  when (!tempHadModFrontIsValid._1) {
    //for (ydx <- 1 until myHistModFrontIsValid.size) {
    //  assume(
    //    myHistModFrontIsValid(ydx)
    //    === myHistModFrontIsValid(ydx).getZero
    //  )
    //}
  }
  when (!tempHadModFrontIsValid._1) {
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
  //val myHistModBackIsFiring = (
  //  KeepAttribute{
  //    val thatWhen = (
  //      modBack.isFiring
  //      && tempHadFrontIsFiring._1
  //      && tempHadMid0FrontUpIsValid._1
  //      && tempHadModFrontIsValid._1
  //    )
  //    History[Bool](
  //      that=(
  //        //modBack.isFiring
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //        thatWhen
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        thatWhen
  //      ),
  //      init=False
  //    )
  //  }
  //  .setName(s"myHistModBackIsFiring")
  //)
  //val tempHadModBackIsFiring = (
  //  myHistModBackIsFiring.sFindFirst(
  //    _ === True
  //  )
  //)

  val tempHadModBackIsFiring: (Bool, Bool) = (
    {
      val cond = (
        modBack.isFiring
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || pipeMem.cMid0FrontArea.up.isValid
        ) && (
          tempHadModFrontIsValid._1
          || modFront.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  when (!tempHadModBackIsFiring._1) {
    //for (ydx <- 1 until myHistModBackIsFiring.size) {
    //  assume(
    //    myHistModBackIsFiring(ydx)
    //    === myHistModBackIsFiring(ydx).getZero
    //  )
    //}
  }
  //val myHistModBackIsValid = (
  //  KeepAttribute{
  //    val thatWhen = (
  //      modBack.isValid
  //      && tempHadFrontIsFiring._1
  //      && tempHadMid0FrontUpIsValid._1
  //      && tempHadModFrontIsValid._1
  //    )
  //    History[Bool](
  //      that=(
  //        //modBack.isFiring
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //        thatWhen
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        thatWhen
  //      ),
  //      init=False
  //    )
  //  }
  //  .setName(s"myHistModBackIsValid")
  //)
  //val tempHadModBackIsValid = (
  //  myHistModBackIsValid.sFindFirst(
  //    _ === True
  //  )
  //)
  val tempHadModBackIsValid: (Bool, Bool) = (
    {
      val cond = (
        modBack.isValid
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || pipeMem.cMid0FrontArea.up.isValid
        ) && (
          tempHadModFrontIsValid._1
          || modFront.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  when (!tempHadModBackIsValid._1) {
    //for (ydx <- 1 until myHistModBackIsValid.size) {
    //  assume(
    //    myHistModBackIsValid(ydx)
    //    === myHistModBackIsValid(ydx).getZero
    //  )
    //}
  }
  //when (!tempHadModBackIsFiring._1) {
  //  for (ydx <- 0 until wordCount) {
  //    assume(
  //      (
  //        pipeMem.modMem(0)(0).readAsync(
  //          address=U(s"${log2Up(wordCount)}'d${ydx}")
  //        )
  //      ) === (
  //        0x0
  //      )
  //    )
  //  }
  //}
  //val myHistBackIsFiring = (
  //  KeepAttribute{
  //    val thatWhen = (
  //      back.isFiring
  //      && tempHadFrontIsFiring._1
  //      && tempHadMid0FrontUpIsValid._1
  //      && tempHadModFrontIsValid._1
  //      && tempHadModBackIsFiring._1
  //    )
  //    History[Bool](
  //      that=(
  //        //back.isFiring
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //        thatWhen
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        thatWhen
  //      ),
  //      init=False
  //    )
  //  }
  //  .setName(s"myHistBackIsFiring")
  //)
  //val tempHadBackIsFiring = (
  //  myHistBackIsFiring.sFindFirst(
  //    _ === True
  //  )
  //)
  val tempHadBackIsFiring: (Bool, Bool) = (
    {
      val cond = (
        back.isFiring
        //&& tempHadFrontIsFiring._1
        //&& tempHadMid0FrontUpIsValid._1
        //&& tempHadModFrontIsValid._1
        //&& tempHadModBackIsFiring._1
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || pipeMem.cMid0FrontArea.up.isValid
        ) && (
          tempHadModFrontIsValid._1
          || modFront.isValid
        ) && (
          tempHadModBackIsValid._1
          || modBack.isValid
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  //val myHistBackIsValid = (
  //  KeepAttribute{
  //    val thatWhen = (
  //      back.isValid
  //      && tempHadFrontIsFiring._1
  //      && tempHadMid0FrontUpIsValid._1
  //      && tempHadModFrontIsValid._1
  //      && tempHadModBackIsFiring._1
  //    )
  //    History[Bool](
  //      that=(
  //        //back.isFiring
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //        thatWhen
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        thatWhen
  //      ),
  //      init=False
  //    )
  //  }
  //  .setName(s"myHistBackIsValid")
  //)
  //val tempHadBackIsValid = (
  //  myHistBackIsValid.sFindFirst(
  //    _ === True
  //  )
  //)
  val tempHadBackIsValid: (Bool, Bool) = (
    {
      val cond = (
        back.isValid
        && (
          tempHadFrontIsFiring._1
          || front.isFiring
        ) && (
          tempHadMid0FrontUpIsValid._1
          || pipeMem.cMid0FrontArea.up.isValid
        ) && (
          tempHadModFrontIsValid._1
          || modFront.isValid
        ) && (
          tempHadModBackIsValid._1
          || modBack.isValid
        ) && (
          tempHadModBackIsFiring._1
          || modBack.isFiring
        )
      )
      RegNextWhen(
        next=True,
        cond=cond,
        init=False
      )
    },
    null
  )
  //when (!tempHadBackIsFiring._1) {
  //  for (ydx <- 1 until myHistBackIsFiring.size) {
  //    assume(
  //      myHistBackIsFiring(ydx)
  //      === myHistBackIsFiring(ydx).getZero
  //    )
  //  }
  //}
  //when (!tempHadBackIsFiring._1) {
  //  for (ydx <- 0 until wordCount) {
  //    assume(
  //      (
  //        pipeMem.modMem(0)(0).readAsync(
  //          address=U(s"${log2Up(wordCount)}'d${ydx}")
  //        )
  //      ) === (
  //        0x0
  //      )
  //    )
  //  }
  //}
  val myHaveSeenPipeToModFrontFire = (
    KeepAttribute(
      tempHadFrontIsFiring._1
      && tempHadMid0FrontUpIsValid._1
      && tempHadModFrontIsValid._1
    )
    .setName(s"myHaveSeenPipeToModFrontFire")
  )
  val myHaveSeenPipeToWrite = (
    KeepAttribute(
      myHaveSeenPipeToModFrontFire
      && myHaveCurrWrite
    )
    .setName(s"myHaveSeenPipeToWrite")
  )
  //val myHistMyWriteAddr = (
  //  KeepAttribute(
  //    History[UInt](
  //      that=(
  //        pipeMem.mod.back.myWriteAddr(0)
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        tempHadModFrontIsFiring._1
  //        && myHaveCurrWrite
  //      ),
  //      init=(
  //        U"1'd0".resized
  //      )
  //    )
  //  )
  //  .setName(s"myHistMyWriteAddr")
  //)
  //val myHistMyWriteData = (
  //  KeepAttribute(
  //    History[UInt](
  //      that=(
  //        pipeMem.mod.back.myWriteData(0)
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        tempHadModFrontIsFiring._1
  //        && myHaveCurrWrite
  //      ),
  //      init=(
  //        U"1'd0".resized
  //      )
  //    )
  //  )
  //  .setName(s"myHistMyWriteData")
  //)

  //val myHistModBackPayloadOp = (
  //  KeepAttribute(
  //    History(
  //      that=(
  //        pipeMem.io.modBack(pipeMem.io.modBackPayloadArr(0)).op
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        //tempHadModFrontIsFiring._1
  //        ////&& myHaveCurrWrite
  //        //&& myHaveCurrWrite
  //        myHaveSeenPipeToWrite
  //      ),
  //      //init=(
  //      //  PipeMemRmwSimDut.ModOp.ADD_RA_RB
  //      //)
  //    )
  //  )
  //  .setName(s"myHistModBackPayloadOp")
  //)
  //val myHistHaveCurrWrite = (
  //  KeepAttribute(
  //    History[Bool](
  //      that=(
  //        //modBack.isFiring
  //        myHaveCurrWrite
  //        ////&& tempHadModFrontIsFiring._1
  //        //&& !myDbgInitstate
  //        ////&& (
  //        ////  //tempHadReset._1
  //        ////  //&& tempHadNotReset._1
  //        ////  myResetCond
  //        ////)
  //        //&& (
  //        //  myResetCond
  //        //)
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        //modBack.isFiring
  //        //myHaveCurrWrite
  //        //&& 
  //        myHaveSeenPipeToWrite
  //        //&& (
  //        //  //tempHadReset._1
  //        //  //&& tempHadNotReset._1
  //        //  //&& (
  //        //  //  tempHadNotReset._2
  //        //  //  > tempHadReset._2
  //        //  //)
  //        //  myResetCond
  //        //)
  //      ),
  //      init=False
  //    )
  //  )
  //  .setName(s"myHistHaveCurrWrite")
  //)

  //when (
  //  !(
  //    //tempHadModFrontIsFiring._1
  //    myHaveSeenPipeToWrite
  //  )
  //) {
  //  for (ydx <- 1 until myHistHaveCurrWrite.size) {
  //    assume(
  //      myHistHaveCurrWrite(ydx) === False
  //    )
  //  }
  //}
  //val tempHaveCurrWrite = (
  //  myHistHaveCurrWrite.sFindFirst(
  //    _ === True
  //  )
  //)
  //val myHistHaveCurrWritePostFirst = (
  //  KeepAttribute(
  //    cloneOf(myHistHaveCurrWrite)
  //  )
  //  .setName(s"myHistHaveCurrWritePostFirst")
  //)
  //val myHistHaveCurrWritePostSecond = (
  //  KeepAttribute(
  //    cloneOf(myHistHaveCurrWrite)
  //  )
  //  .setName(s"myHistHaveCurrWritePostSecond")
  //)
  ////myHistHaveCurrWritePostFirst := myHistHaveCurrWrite
  //for (ydx <- 0 until myHistHaveCurrWrite.size) {
  //  myHistHaveCurrWritePostFirst(ydx) := (
  //    myHistHaveCurrWritePostFirst(ydx).getZero
  //  )
  //  myHistHaveCurrWritePostSecond(ydx) := (
  //    myHistHaveCurrWritePostSecond(ydx).getZero
  //  )
  //}
  //when (
  //  tempHaveCurrWrite._1
  //) {
  //  switch (tempHaveCurrWrite._2) {
  //    for (ydx <- 0 until myHistHaveCurrWritePostFirst.size) {
  //      is (ydx) {
  //        for (zdx <- ydx + 1 until myHistHaveCurrWritePostFirst.size) {
  //          myHistHaveCurrWritePostFirst(zdx) := (
  //            myHistHaveCurrWrite(zdx)
  //          )
  //        }
  //      }
  //    }
  //  }
  //}
  //val tempHaveCurrWritePostFirst = (
  //  myHistHaveCurrWritePostFirst.sFindFirst(
  //    _ === True
  //  )
  //)
  //when (
  //  tempHaveCurrWritePostFirst._1
  //) {
  //  switch (tempHaveCurrWritePostFirst._2) {
  //    for (ydx <- 0 until myHistHaveCurrWritePostSecond.size) {
  //      is (ydx) {
  //        for (zdx <- ydx + 1 until myHistHaveCurrWritePostSecond.size) {
  //          myHistHaveCurrWritePostSecond(zdx) := (
  //            myHistHaveCurrWrite(zdx)
  //          )
  //        }
  //      }
  //    }
  //  }
  //}
  //val tempHaveCurrWritePostSecond = (
  //  myHistHaveCurrWritePostSecond.sFindFirst(
  //    _ === True
  //  )
  //)
  //val myHistModBackOpCnt = (
  //  KeepAttribute(
  //    History[UInt](
  //      that=(
  //        modBack(modBackPayload).opCnt
  //      ),
  //      length=myProveNumCycles,
  //      when=(
  //        //myHaveCurrWrite
  //        //tempHaveCurrWrite._1
  //        //&& 
  //        //myHaveCurrWrite
  //        myHaveSeenPipeToWrite
  //        //&& !myDbgInitstate
  //        //&& (
  //        //  myResetCond
  //        //)
  //      ),
  //      init=U(s"${PipeMemRmwSimDut.modOpCntWidth}'d0"),
  //    )
  //  )
  //  .setName(s"myHistModBackOpCnt")
  //)
  //when (
  //  !(
  //    tempHaveCurrWrite._1
  //    //&& myHaveCurrWrite
  //  )
  //) {
  //  for (ydx <- 1 until myHistModBackOpCnt.size) {
  //    assume(
  //      myHistModBackOpCnt(ydx) === myHistModBackOpCnt(ydx).getZero
  //    )
  //  }
  //}
  def getMyHistHaveSeenPipeToWriteVecCond(
    idx: Int
  ) = (
    myHaveSeenPipeToWrite
    && (
      pipeMem.mod.back.myWriteAddr(0) === idx
    )
  )
  //val myHistHaveSeenPipeToWriteV2d = (
  //  KeepAttribute(
  //    //Vec.fill(myHistHaveSeenPipeToWriteV2dOuterDim)(
  //      Vec[Vec[Bool]]({
  //        val myArr = new ArrayBuffer[Vec[Bool]]()
  //        for (idx <- 0 until wordCount) {
  //          myArr += (
  //            History[Bool](
  //              that=(
  //                //myHaveCurrWriteCntVec(idx)
  //                getMyHistHaveSeenPipeToWriteVecCond(idx=idx)
  //              ),
  //              length=myProveNumCycles,
  //              //when=(
  //              //  getMyHistHaveSeenPipeToWriteVecCond(idx=idx)
  //              //),
  //              init=False,
  //            )
  //          )
  //        }
  //        myArr
  //      })
  //    //)
  //  )
  //)
  def tempHistHaveSeenPipeToWriteV2dOuterDim = (
    //3
    4
  )
  //val myHistHaveSeenPipeToWriteV2dPost = (
  //  Vec.fill(tempHistHaveSeenPipeToWriteV2dOuterDim - 1)(
  //    Vec.fill(wordCount)(
  //      Vec.fill(myProveNumCycles)(
  //        Bool()
  //      )
  //    )
  //  )
  //)
  //myHistHaveSeenPipeToWriteV2dPost(0).allowOverride
  //myHistHaveSeenPipeToWriteV2dPost(1).allowOverride

  val tempHaveSeenPipeToWriteV2dFindFirst_0 = (
    KeepAttribute(
      Vec.fill(tempHistHaveSeenPipeToWriteV2dOuterDim)(
        Vec.fill(wordCount)(
          Bool()
        )
      )
    )
    .setName(s"tempHaveSeenPipeToWriteV2dFindFirst_0")
  )
  //for (jdx <- 0 until tempHistHaveSeenPipeToWriteV2dOuterDim) {
    for (idx <- 0 until wordCount) {
      for (jdx <- 0 until tempHistHaveSeenPipeToWriteV2dOuterDim) {
        def tempFunc(
          someJdx: Int
        ) = (
          tempHaveSeenPipeToWriteV2dFindFirst_0(someJdx)
        )
        if (jdx == 0) {
          tempFunc(jdx)(idx) := (
            getMyHistHaveSeenPipeToWriteVecCond(idx=idx)
            //RegNextWhen(
            //)
          )
        } else {
          tempFunc(jdx)(idx) := (
            RegNext(
              next=tempFunc(jdx)(idx),
              init=tempFunc(jdx)(idx).getZero,
            )
          )
          when (tempFunc(jdx - 1)(idx)) {
            tempFunc(jdx)(idx) := (
              RegNext(
                next=tempFunc(jdx - 1)(idx),
                init=tempFunc(jdx)(idx).getZero,
              )
            )
          }
        }
      }
    }
  //}
  //val tempHaveSeenPipeToWriteV2dFindFirst_1 = (
  //  KeepAttribute(
  //    Vec.fill(tempHistHaveSeenPipeToWriteV2dOuterDim)(
  //      Vec.fill(wordCount)(
  //        UInt(log2Up(myProveNumCycles) bits)
  //      )
  //    )
  //  )
  //  .setName(s"tempHaveSeenPipeToWriteV2dFindFirst_1")
  //)
  //for (idx <- 0 until wordCount) {
  //  val tempFindFirst = (
  //    myHistHaveSeenPipeToWriteV2d(idx).sFindFirst(
  //      _ === True
  //    )
  //  )
  //  tempHaveSeenPipeToWriteV2dFindFirst_0(0)(idx) := tempFindFirst._1
  //  tempHaveSeenPipeToWriteV2dFindFirst_1(0)(idx) := tempFindFirst._2

  //  for (jdx <- 0 until myProveNumCycles) {
  //    myHistHaveSeenPipeToWriteV2dPost(0)(idx)(jdx) := (
  //      myHistHaveSeenPipeToWriteV2dPost(0)(idx)(jdx).getZero
  //    )
  //    myHistHaveSeenPipeToWriteV2dPost(1)(idx)(jdx) := (
  //      myHistHaveSeenPipeToWriteV2dPost(1)(idx)(jdx).getZero
  //    )
  //  }

  //  when (tempHaveSeenPipeToWriteV2dFindFirst_0(0)(idx)) {
  //    switch (tempHaveSeenPipeToWriteV2dFindFirst_1(0)(idx)) {
  //      for (jdx <- 0 until myProveNumCycles) {
  //        is (jdx) {
  //          for (kdx <- jdx + 1 until myProveNumCycles) {
  //            myHistHaveSeenPipeToWriteV2dPost(0)(idx)(kdx) := (
  //              myHistHaveSeenPipeToWriteV2d(idx)(kdx)
  //            )
  //          }
  //        }
  //      }
  //    }
  //  }
  //  val tempFindFirstPost0 = (
  //    myHistHaveSeenPipeToWriteV2dPost(0)(idx).sFindFirst(
  //      _ === True
  //    )
  //  )
  //  tempHaveSeenPipeToWriteV2dFindFirst_0(1)(idx) := tempFindFirstPost0._1
  //  tempHaveSeenPipeToWriteV2dFindFirst_1(1)(idx) := tempFindFirstPost0._2

  //  when (tempHaveSeenPipeToWriteV2dFindFirst_0(1)(idx)) {
  //    switch (tempHaveSeenPipeToWriteV2dFindFirst_1(1)(idx)) {
  //      for (jdx <- 0 until myProveNumCycles) {
  //        is (jdx) {
  //          for (kdx <- jdx + 1 until myProveNumCycles) {
  //            myHistHaveSeenPipeToWriteV2dPost(1)(idx)(kdx) := (
  //              myHistHaveSeenPipeToWriteV2d(idx)(kdx)
  //            )
  //          }
  //        }
  //      }
  //    }
  //  }
  //  val tempFindFirstPost1 = (
  //    myHistHaveSeenPipeToWriteV2dPost(1)(idx).sFindFirst(
  //      _ === True
  //    )
  //  )
  //  tempHaveSeenPipeToWriteV2dFindFirst_0(2)(idx) := tempFindFirstPost1._1
  //  tempHaveSeenPipeToWriteV2dFindFirst_1(2)(idx) := tempFindFirstPost1._2

  //  //tempHaveSeenPipeToWriteV2dFindFirst_0(1)(idx) := False
  //  //tempHaveSeenPipeToWriteV2dFindFirst_1(1)(idx) := 0x0

  //  //when (tempHaveSeenPipeToWriteV2dFindFirst_0(0)(idx)) {
  //  //}

  //  //tempHaveSeenPipeToWriteV2dFindFirst_0(2)(idx) := False
  //  //tempHaveSeenPipeToWriteV2dFindFirst_1(2)(idx) := 0x0
  //}
  for (idx <- 0 until wordCount) {
    //for (jdx <- 0 until myProveNumCycles) {
    //  myHistHaveSeenPipeToWriteV2d(1)(idx)(jdx) := (
    //    False
    //  )
    //  myHistHaveSeenPipeToWriteV2d(2)(idx)(jdx) := (
    //    False
    //  )
    //}
  }
  //val myHistHadWriteAt = (
  //  KeepAttribute(
  //    History[Bool](
  //      that=(
  //      )
  //    )
  //  )
  //  .setName(s"myHistHadWriteAt")
  //)
  //when (
  //  pastValidAfterReset()
  //) {
  //  when (
  //    myHaveSeenPipeToModFrontFire
  //    && (
  //      myHaveCurrWrite
  //    ) && (
  //      tempHaveCurrWrite._1
  //    ) && (
  //      tempHaveCurrWritePostFirst._1
  //    )
  //  ) {
  //    assert(
  //      myHistModBackOpCnt(
  //        //0
  //        tempHaveCurrWrite._2
  //      )
  //      === myHistModBackOpCnt(tempHaveCurrWritePostFirst._2) + 1
  //    )
  //  }
  //}

  when (pastValidAfterReset) {
    //--------
    //val tempRight = (
    //  //--------
    //  KeepAttribute(
    //    //RegNextWhen(
    //    //  pipeMem.modMem(0)(0).readAsync(
    //    //    address=(
    //    //      //RegNextWhen(
    //    //        //modBack(modBackPayload).myExt(0).memAddr(PipeMemRmw.modWrIdx)
    //    //        //pipeMem.cBackArea.upExt(1)(0)(
    //    //        //  PipeMemRmw.extIdxSingle
    //    //        //).memAddr(0),
    //    //        pipeMem.mod.back.myWriteAddr(0)
    //    //      //  modBack.isFiring,
    //    //      //) init(0x0)
    //    //    )
    //    //    //RegNextWhen(
    //    //    //  modFront(modFrontPayload).myExt(0).memAddr(PipeMemRmw.modWrIdx),
    //    //    //  modFront.isFiring
    //    //    //)
    //    //  ),
    //    //  //modBack.isFiring
    //    //  myHaveCurrWrite
    //    //) init(rSavedModArr(0).getZero)
    //    /*RegNextWhen*//*RegNext*/(
    //      myHistMyWriteData(
    //        (tempHaveCurrWritePostFirst._2)
    //      ),
    //    )
    //    //init(pipeMem.mod.back.myWriteData(0).getZero)
    //  )
    //  .setName(s"tempRight")
    //)
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
    val tempMyCoverInit = PipeMemRmwSimDutModType(
      cfg=pmCfg,
      optModHazardKind=optModHazardKind,
      optFormal=optFormal,
    )
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
          myHistCoverVec(0).op === PipeMemRmwSimDut.ModOp.AddRaRb
        ) && (
          myHistCoverVec(1).op === PipeMemRmwSimDut.ModOp.LdrRaRb
          //&& !myHistCoverVec(1).dcacheHit
        ) && (
          myHistCoverVec(2).op === PipeMemRmwSimDut.ModOp.LdrRaRb
          //&& !myHistCoverVec(2).dcacheHit
        )
      )
      cover(
        (
          myHistCoverVec(0).op === PipeMemRmwSimDut.ModOp.LdrRaRb
          //&& !myHistCoverVec(0).dcacheHit
        ) && (
          myHistCoverVec(1).op === PipeMemRmwSimDut.ModOp.LdrRaRb
          //&& myHistCoverVec(1).dcacheHit
        ) && (
          myHistCoverVec(2).op === PipeMemRmwSimDut.ModOp.LdrRaRb
          //&& !myHistCoverVec(2).dcacheHit
        )
      )
      cover(
        (
          myHistCoverVec(0).op === PipeMemRmwSimDut.ModOp.AddRaRb
          //&& !myHistCoverVec(0).dcacheHit
        ) && (
          myHistCoverVec(1).op === PipeMemRmwSimDut.ModOp.MulRaRb
          //&& !myHistCoverVec(1).dcacheHit
        ) && (
          myHistCoverVec(2).op === PipeMemRmwSimDut.ModOp.LdrRaRb
          //&& myHistCoverVec(2).dcacheHit
        )
      )
      cover(
        (
          myHistCoverVec(0).op === PipeMemRmwSimDut.ModOp.LdrRaRb
          //&& !myHistCoverVec(0).dcacheHit
        ) && (
          myHistCoverVec(1).op === PipeMemRmwSimDut.ModOp.MulRaRb
          //&& !myHistCoverVec(0).dcacheHit
        ) && (
          myHistCoverVec(2).op === PipeMemRmwSimDut.ModOp.AddRaRb
          //&& !myHistCoverVec(2).dcacheHit
        )
      )
    }
    val myHadWriteAt = (
      KeepAttribute(
        Vec.fill(wordCount)(
          Bool()
        )
      )
      .setName(s"myHadWriteAt")
    )
    val myPrevWriteData = (
      KeepAttribute(
        Vec[UInt]({
          val myArr = new ArrayBuffer[UInt]() 
          for (idx <- 0 until pipeMem.wordCountArr(0)) {
            myArr += (
              //RegNextWhen(
              //  next=pipeMem.mod.back.myWriteData(0),
              //  cond=(
              //    getMyHistHaveSeenPipeToWriteVecCond(idx=idx)
              //    //(
              //    //  //myHaveCurrWrite
              //    //  //pipeMem.mod.back.myWriteEnable(0)
              //    //  myHaveSeenPipeToWrite
              //    //) && (
              //    //  pipeMem.mod.back.myWriteAddr(0) === idx
              //    //)
              //  ),
              //  init=pipeMem.mod.back.myWriteData(0).getZero,
              //)
              /*Reg*/(
                wordType()
              )
            )
          }
          myArr
        })
      )
      .setName(s"myPrevWriteData")
    )
    for (idx <- 0 until wordCount) {
      myHadWriteAt(idx) := (
        RegNext(
          next=myHadWriteAt(idx),
          init=myHadWriteAt(idx).getZero,
        )
      )
      myPrevWriteData(idx) := (
        RegNext(
          next=myPrevWriteData(idx),
          init=myPrevWriteData(idx).getZero,
        )
      )
    }
    val formalFwdModBackArea = (pipeMem.myHaveFormalFwd) generate (
      new Area {
        val myExt = (
          KeepAttribute(
            pipeMem.mkExt()
          )
          .setName(
            s"formalFwdModBackArea_"
            + s"myExt"
          )
        )
        val myFwd = (
          KeepAttribute(
            Vec.fill(extIdxLim)(
              pipeMem.mkFwd()
            )
          )
          .setName(
            s"formalFwdModBackArea_"
            + s"myFwd"
          )
        )
        //def tempPayload = (
        //  //Ved.fill(extIdxLim)(
        //  //  modBack(modBackPayload)
        //  //)
        //  pipeMem.cBackArea.upFwd
        //)
        for (extIdx <- 0 until extIdxLim) {
          myExt(0)(extIdx) := pipeMem.cBackArea.upExt(1)(0)(extIdx)
          myFwd(extIdx) := pipeMem.cBackArea.upFwd(extIdx)
          //myFwd(extIdx).myFindFirst_0.allowOverride
          //myFwd(extIdx).myFindFirst_1.allowOverride
        }
        //if (pipeMem.myHaveFormalFwd) {
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
        //        cond=modBack.isFiring,
        //        init=False,
        //      )
        //    ) {
        //      when (!modBack.isValid) {
        //        assert(
        //          myExt(0)(extIdxUp).main
        //          === myExt(0)(extIdxUp).main.getZero
        //        )
        //        assert(
        //          myFwd(extIdxUp)
        //          === myFwd(extIdxUp).getZero
        //        )
        //      }
        //      assert(
        //        myExt(0)(extIdxSaved)
        //        === myExt(0)(extIdxSaved).getZero
        //      )
        //      assert(
        //        myFwd(extIdxSaved)
        //        === myFwd(extIdxSaved).getZero
        //      )
        //    } 
        //    //when (!past(up.isValid) init(False)) {
        //    //  assert(
        //    //    upFwd
        //    //  )
        //    //}
        //    when (
        //      !modBack.isValid
        //      && !past(modBack.isValid)
        //    ) {
        //      assert(
        //        stable(myExt(0)(extIdxUp).main)
        //      )
        //      assert(
        //        stable(myExt(0)(extIdxSaved))
        //      )
        //      assert(
        //        stable(myFwd(extIdxUp))
        //      )
        //      assert(
        //        stable(myFwd(extIdxSaved))
        //      )
        //    }
        //  }
        //}
        if (pipeMem.myHaveFormalFwd) {
          when (pastValidAfterReset) {
            //def myExt(
            //  someExtIdx: Int
            //) = (
            //  midModPayload(someExtIdx).myExt
            //)
            //def myFwd(
            //  someExtIdx: Int
            //) = (
            //  midModPayload(someExtIdx).myFwd
            //)
            when (
              !RegNextWhen(
                next=True,
                cond=modBack.isFiring,
                init=False,
              )
            ) {
              when (!modBack.isValid) {
                assert(
                  myExt(0)(extIdxUp).main
                  === myExt(0)(extIdxUp).main.getZero
                )
                assert(
                  myFwd(extIdxUp)
                  === myFwd(extIdxUp).getZero
                )
              }
              assert(
                myExt(0)(extIdxSaved)
                === myExt(0)(extIdxSaved).getZero
              )
              assert(
                myFwd(extIdxSaved)
                === myFwd(extIdxSaved).getZero
              )
            } 
            when (
              past(modBack.isFiring) init(False)
            ) {
              assert(
                myExt(0)(extIdxSaved)
                === (
                  past(myExt(0)(extIdxUp))
                  init(myExt(0)(extIdxUp).getZero)
                )
              )
              assert(
                myFwd(extIdxSaved)
                === (
                  past(myFwd(extIdxUp)) init(myFwd(extIdxUp).getZero)
                )
              )
              //assert(
              //  midModPayload(extIdxSaved)
              //  === past(midModPayload(extIdxUp))
              //)
            }
            //when (!past(up.isValid) init(False)) {
            //  assert(
            //    upFwd
            //  )
            //}
            //when (
            //  !modBack.isValid
            //  && !past(modBack.isValid)
            //) {
            //  assert(
            //    stable(myExt(0)(extIdxUp).memAddr(0))
            //  )
            //  assert(
            //    stable(myExt(0)(extIdxUp).rdMemWord(0))
            //  )
            //  //when (
            //  //  midModPayload(extIdxUp).op
            //  //  =/= PipeMemRmwSimDut.ModOp.LDR_RA_RB
            //  //) {
            //    //assert(
            //    //  stable(myExt(0)(extIdxUp).rdMemWord(0))
            //    //)
            //    assert(
            //      stable(myExt(0)(extIdxUp).modMemWord(0))
            //    )
            //  //}
            //  assert(
            //    stable(myExt(0)(extIdxSaved))
            //  )
            //  assert(
            //    stable(myFwd(extIdxUp))
            //  )
            //  assert(
            //    stable(myFwd(extIdxSaved))
            //  )
            //}
            when (
              modBack.isValid
              //&& !rSetMidModPayloadState
            ) {
              assert(
                myFwd(extIdxUp)
                === modBack(modBackPayload).myFwd
              )
              assert(
                myExt(0)(extIdxUp).main
                === modBack(modBackPayload).myExt(0).main
              )
              //assert(
              //  myExt(0)(extIdxUp).rdMemWord(0)
              //  === modBack(modBackPayload).myExt(0).rdMemWord(0)
              //)
              //assert(
              //  myExt(0)(extIdxUp).memAddr(0)
              //  === modBack(modBackPayload).myExt(0).memAddr(0)
              //)
              ////assert(
              ////  myExt(0)(extIdxUp).modMemWordValid
              ////  === modBack(modBackPayload).myExt(0).modMemWordValid
              ////)
              //when (
              //  modBack(modBackPayload).myExt(0).modMemWordValid
              //) {
              //  assert(
              //    myExt(0)(extIdxUp).modMemWordValid
              //  )
              //}
            }
          }
        }
        //myFwd(extIdxUp) := (
        //)
        val doFormalFwdUp = (
          PipeMemRmwDoFwdArea(
            fwdAreaName=s"formalFwdModBackArea_doFormalFwdUp",
            fwd=(
              myFwd(extIdxUp)
              //midModPayload(extIdxUp).myFwd
            ),
            setToMyFwdDataFunc=(
              ydx: Int,
              zdx: Int,
              myFwdData: UInt,
            ) => {
              //when (pastValidAfterReset) {
                //when (myHadWriteAt(myExt(0)(extIdxUp).memAddr(0))) {
                  assert(
                    myExt(0)(extIdxUp).rdMemWord(0)
                    === myFwdData
                  )
                //}
              //}
            }
          )
        )
        val doFormalFwdSaved =  (
          PipeMemRmwDoFwdArea(
            fwdAreaName=s"formalFwdModBackArea_doFormalFwdSaved",
            fwd=(
              myFwd(extIdxSaved)
              //midModPayload(extIdxSaved).myFwd
            ),
            setToMyFwdDataFunc=(
              ydx: Int,
              zdx: Int,
              myFwdData: UInt,
            ) => {
              //when (pastValidAfterReset) {
                //when (myHadWriteAt(myExt(0)(extIdxSaved).memAddr(0))) {
                  assert(
                    myExt(0)(extIdxSaved).rdMemWord(0)
                    === myFwdData
                  )
                //}
              //}
            }
          )
        )
        //val doFormalFwdSaved =  (
        //  PipeMemRmwDoFwdArea(
        //    fwd=(
        //      myFwd(extIdxSaved)
        //      //midModPayload(extIdxSaved).myFwd
        //    ),
        //    setToMyFwdDataFunc=(
        //      ydx: Int,
        //      zdx: Int,
        //      myFwdData: UInt,
        //    ) => {
        //      //when (pastValidAfterReset) {
        //        assert(
        //          midModPayload(extIdxSaved).myExt(0).rdMemWord(0)
        //          === myFwdData
        //        )
        //      //}
        //    }
        //  )
        //)
      }
    )
    when (pastValidAfterReset()) {
      when (
        ///*past*/(modBack.isFiring)
        //&& /*past*/(pipeMem.mod.back.myWriteEnable(0))
        ///*past*/(myHaveCurrWrite)
        //pipeMem.mod.back.myWriteEnable(0)
        True
      ) {
        //def firstTempHaveCurrWrite = (
        //  tempHaveCurrWrite
        //  //tempHaveCurrWritePostFirst
        //)
        //def secondTempHaveCurrWrite = (
        //  tempHaveCurrWritePostFirst
        //  //tempHaveCurrWritePostSecond
        //)
        //val rMyHadFirstWriteAt = (
        //  KeepAttribute(
        //    Vec[Bool]({
        //      val myArr = new ArrayBuffer[Bool]()
        //      for (idx <- 0 until pipeMem.wordCountArr(0)) {
        //        myArr += (
        //          RegNextWhen(
        //            next=(
        //              getMyHistHaveSeenPipeToWriteVecCond(idx=idx)
        //            ),
        //            cond=(
        //              getMyHistHaveSeenPipeToWriteVecCond(idx=idx)
        //              //(
        //              //  //myHaveCurrWrite
        //              //  myHaveSeenPipeToWrite
        //              //) && (
        //              //  pipeMem.mod.back.myWriteAddr(0) === idx
        //              //)
        //            ),
        //            init=False,
        //          )
        //        )
        //      }
        //      myArr
        //    })
        //  )
        //  .setName(s"rMyHadFirstWriteAt")
        //)
        //val rMyHadSecondWriteAt = (
        //  KeepAttribute(
        //    Vec[Bool]({
        //      val myArr = new ArrayBuffer[Bool]()
        //      for (idx <- 0 until pipeMem.wordCountArr(0)) {
        //        myArr += (
        //          RegNextWhen(
        //            next=(
        //              rMyHadFirstWriteAt(idx)
        //              && getMyHistHaveSeenPipeToWriteVecCond(idx=idx)
        //            ),
        //            cond=(
        //              rMyHadFirstWriteAt(idx)
        //              && getMyHistHaveSeenPipeToWriteVecCond(idx=idx)
        //              //(
        //              //  //myHaveCurrWrite
        //              //  myHaveSeenPipeToWrite
        //              //) && (
        //              //  pipeMem.mod.back.myWriteAddr(0) === idx
        //              //)
        //            ),
        //            init=False,
        //          )
        //        )
        //      }
        //      myArr
        //    })
        //  )
        //  .setName(s"rMyHadSecondWriteAt")
        //)
        //when (
        //  past(modFront.isFiring)
        //) {
        //  assert(
        //    modBack(modBackPayload).myExt(0).rdMemWord(0)
        //  )
        //}
        val tempCond = (
          (
            //myHaveCurrWrite
            //past(myHaveSeenPipeToWrite)
            //past(myHaveSeenPipeToModFrontFire)
            //&&
            ///*past*/(modBack.isValid)
            //&& /*past*/(pipeMem.mod.back.myWriteEnable(0))
            myHaveCurrWrite
          )
        )
        when (
          past(
            tempCond
          )
        ) {
          for (idx <- 0 until wordCount) {
            when (
              //getMyHistHaveSeenPipeToWriteVecCond(idx=idx)
              past(pipeMem.mod.back.myWriteAddr(0)) === idx
            ) {
              myHadWriteAt(idx) := (
                past(True) init(False)
              )
              myPrevWriteData(idx) := (
                past(pipeMem.mod.back.myWriteData(0))
              )
            }
          }
        }
        val tempCond1 = (
          //modBack.isValid
          //&& 
          /*past*/(pipeMem.mod.back.myWriteEnable(0))
          && (
            myHadWriteAt(
            /*past*/(pipeMem.mod.back.myWriteAddr(0))
            )
          ) && (
            tempHaveSeenPipeToWriteV2dFindFirst_0(0)(
              pipeMem.mod.back.myWriteAddr(0)
            )
          ) && (
            tempHaveSeenPipeToWriteV2dFindFirst_0(1)(
              pipeMem.mod.back.myWriteAddr(0)
            )
          ) && (
            tempHaveSeenPipeToWriteV2dFindFirst_0(2)(
              pipeMem.mod.back.myWriteAddr(0)
            )
          )
        )
        val myTempRight = (
          KeepAttribute(
            Vec[UInt]({
              val myArr = new ArrayBuffer[UInt]()
              myArr += (
                myPrevWriteData(
                  /*past*/(pipeMem.mod.back.myWriteAddr(0))
                )
              )
              myArr += (
                modBack(modBackPayload).myExt(0).rdMemWord(0)
              )
              myArr
            })
          )
          .setName(s"${pipeName}_myTempRight")
        )
        //for (zdx <- 0 until myTempRight.size) {
        //  if (zdx > 0) {
            //when (
            //  tempCond1
            //) {
            //  //assert(
            //  //  myTempRight(zdx)
            //  //  === myTempRight(zdx - 1)
            //  //)
            //  assert(
            //    (
            //      myPrevWriteData(
            //        pipeMem.mod.back.myWriteAddr(0)
            //      )
            //    ) === (
            //      modBack(modBackPayload).myExt(0).rdMemWord(0)
            //    )
            //  )
            //}
        //  }
        //}
        when (
          //(
          //  tempCond
          //)
          ////--------
          ////&& (
          ////  RegNextWhen(
          ////    next=True,
          ////    cond=(
          ////      myHaveCurrWrite
          ////      && pipeMem.mod.back.myWriteAddr(0)
          ////    ),
          ////    init=False,
          ////  )
          ////)
          ////--------
          ////&& (
          ////  //tempHaveSeenPipeToWriteVec(
          ////  //  pipeMem.mod.back.myWriteAddr(0)
          ////  //)
          ////  /*past*/(tempHaveSeenPipeToWriteV2dFindFirst_0(2)(
          ////    /*past*/(pipeMem.mod.back.myWriteAddr(0))
          ////  ))
          ////) && (
          ////  True
          ////  //rMyHadSecondWriteAt(
          ////  //  pipeMem.mod.back.myWriteAddr(0)
          ////  //)
          ////)
          //&& (
          //  myHadWriteAt(
          //    pipeMem.mod.back.myWriteAddr(0)
          //  )
          //)
          tempCond1
        ) {
          def myTempLeft = (
            /*past*/(pipeMem.mod.back.myWriteData(0))
          )
          //def myTempRight = (
          //  /*past*/(
          //    myPrevWriteData(
          //      /*past*/(pipeMem.mod.back.myWriteAddr(0))
          //    )
          //  )
          //  ///*past*/(
          //  //  //pipeMem.modMem(0)(0).readAsync(
          //  //  //  address=past(pipeMem.mod.back.myWriteAddr(0))
          //  //  //)
          //  //  //RegNextWhen(
          //  //  //  next=pipeMem.mod.back.myWriteData(0),
          //  //  //  cond=(
          //  //  //    pipeMem.mod.back.myWriteAddr(0)
          //  //  //  ),
          //  //  //  init=pipeMem.mod.back.myWriteData(0).getZero,
          //  //  //)
          //  //)
          //)
          for (zdx <- 0 until myTempRight.size) {
            if (
              zdx == 1
              //true
            ) {
              //if (zdx > 0) {
              //  assert(
              //    myTempRight(zdx)
              //    === myTempRight(zdx - 1)
              //  )
              //}
              if (
                (
                  optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
                ) || (
                  (
                    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
                  ) && (
                    PipeMemRmwSimDut.allModOpsSameChange
                  )
                )
              ) {
                assert(
                  myTempLeft
                  === myTempRight(zdx) + 1
                )
              } else {
                switch (
                  //myHistModBackPayloadOp(firstTempHaveCurrWrite._2)
                  /*past*/(modBack(modBackPayload).op)
                ) {
                  is (PipeMemRmwSimDut.ModOp.AddRaRb) {
                    assert(
                      ///*past*/(tempLeft) //+ 1
                      //=== (
                      //  //mySavedMod
                      //  /*past*//*past*/(tempRight) + 1 //+ 1
                      //  /*past*///(mySavedMod)
                      //  //mySavedMod + 1
                      //)
                      myTempLeft
                      === myTempRight(zdx) + 1
                    )
                  }
                  is (PipeMemRmwSimDut.ModOp.LdrRaRb) {
                    assert(
                      myTempLeft
                      === myTempRight(zdx) - 1
                      //myTempLeft
                      //=== myTempRight(zdx) + 1
                    )
                  }
                  is (PipeMemRmwSimDut.ModOp.MulRaRb) {
                    val tempBitsRange = (
                      //wordType().bitsRange
                      modBack(modBackPayload).myExt(0).modMemWord.bitsRange
                    )
                    assert(
                      myTempLeft(tempBitsRange)
                      === (myTempRight(zdx) << 1)(tempBitsRange)
                      //myTempLeft(tempBitsRange)
                      //=== (myTempRight(zdx) + 1)(tempBitsRange)
                    )
                  }
                  //is (PipeMemRmwSimDut.ModOp.BEQ_RA_SIMM) {
                  //}
                }
              }
            }
          }
        }
        //when (
        //  (
        //    ///*past*/(
        //    //  firstTempHaveCurrWrite._1
        //    //) && (
        //    //  firstTempHaveCurrWrite._2 === 0
        //    //)
        //    (
        //      myHaveCurrWrite
        //    )
        //    && /*past*/(secondTempHaveCurrWrite._1)
        //    && (tempHaveCurrWritePostSecond._1)
        //  ) && (
        //    (
        //      /*past*/(
        //        myHistMyWriteAddr(/*past*/(
        //          //firstTempHaveCurrWrite._2
        //          0
        //        ))
        //      ) === /*past*/(
        //        myHistMyWriteAddr(/*past*/(secondTempHaveCurrWrite._2))
        //      )
        //    ) 
        //  )
        //) {
        //  def myTempLeft = (
        //    //if (
        //    //  firstTempHaveCurrWrite == tempHaveCurrWrite
        //    //) (
        //    //  Mux[UInt](
        //    //    
        //    //  )
        //    //) else (
        //      myHistMyWriteData(
        //        //firstTempHaveCurrWrite._2
        //        0x0
        //      )
        //    //)
        //  )
        //  def myTempRight = (
        //    //myHistMyWriteData(secondTempHaveCurrWrite._2)
        //    //if (
        //    //  (
        //    //    firstTempHaveCurrWrite == tempHaveCurrWrite
        //    //  ) && (
        //    //    secondTempHaveCurrWrite == tempHaveCurrWritePostFirst
        //    //  )
        //    //) 
        //    (
        //      //Mux[UInt](
        //      //  (
        //      //    //firstTempHaveCurrWrite._1
        //      //    //&& (
        //      //    //  firstTempHaveCurrWrite._2 === 0x0
        //      //    //)
        //      //    myHaveCurrWrite
        //      //  ),
        //        //modBack(modBackPayload).myExt(0).rdMemWord(0),
        //        myHistMyWriteData(
        //          //firstTempHaveCurrWrite._2
        //          secondTempHaveCurrWrite._2
        //          //0x0
        //        )
        //      //)
        //    ) 
        //    //else (
        //    //  myHistMyWriteData(
        //    //    firstTempHaveCurrWrite._2
        //    //  )
        //    //)
        //  )
        //  if (
        //    (
        //      optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
        //    ) || (
        //      (
        //        optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
        //      ) && (
        //        PipeMemRmwSimDut.allModOpsSameChange
        //      )
        //    )
        //  ) {
        //    //when (myHaveCurrWrite) {
        //      assert(
        //        myTempLeft
        //        === myTempRight + 1
        //      )
        //    //}
        //  } else if (
        //    //PipeMemRmwSimDut.doTestModOp
        //    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
        //  ) {
        //    //when (myHaveCurrWrite) {
        //      switch (
        //        //myHistModBackPayloadOp(firstTempHaveCurrWrite._2)
        //        modBack(modBackPayload).op
        //      ) {
        //        is (PipeMemRmwSimDut.ModOp.ADD_RA_RB) {
        //          assert(
        //            ///*past*/(tempLeft) //+ 1
        //            //=== (
        //            //  //mySavedMod
        //            //  /*past*//*past*/(tempRight) + 1 //+ 1
        //            //  /*past*///(mySavedMod)
        //            //  //mySavedMod + 1
        //            //)
        //            myTempLeft
        //            === myTempRight + 1
        //          )
        //        }
        //        is (PipeMemRmwSimDut.ModOp.LDR_RA_RB) {
        //          assert(
        //            myTempLeft
        //            === myTempRight - 1
        //            //myTempLeft
        //            //=== myTempRight + 1
        //          )
        //        }
        //        is (PipeMemRmwSimDut.ModOp.MUL_RA_RB) {
        //          val tempBitsRange = (
        //            //wordType().bitsRange
        //            modBack(modBackPayload).myExt(0).modMemWord.bitsRange
        //          )
        //          assert(
        //            myTempLeft(tempBitsRange)
        //            === (myTempRight << 1)(tempBitsRange)
        //            //myTempLeft(tempBitsRange)
        //            //=== (myTempRight + 1)(tempBitsRange)
        //          )
        //        }
        //        //is (PipeMemRmwSimDut.ModOp.BEQ_RA_SIMM) {
        //        //}
        //      }
        //    //}
        //  }
        //  //}
        //}
      }
    }
    cover(
      back.isFiring
      && (
        back(backPayload).myExt(0).modMemWord === 0x3
      ) && (
        back(backPayload).myExt(0).memAddr(PipeMemRmw.modWrIdx) === 0x2
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
  def pipeName = (
    PipeMemRmwSimDut.pipeName
  )
  def optModHazardKind = (
    PipeMemRmwSimDut.optModHazardKind
  )
  //def modType() = PipeMemRmwSimDutModType(
  //  optModHazardKind=optModHazardKind,
  //  optFormal=optFormal,
  //)
  val dut: PipeMemRmwSimDut = (
    PipeMemRmwSimDut(
      optFormal=(
        //doFormal
        optFormal
      )
    )
  )
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
  def modType() = dut.ModType()
  def pipeMem = dut.pipeMem
  def pmIo = pipeMem.io
  def pmCfg = pipeMem.cfg
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
  def doMidMod = (
    optModHazardKind == PipeMemRmw.ModHazardKind.Dupl
    || (
      optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
      && modStageCnt > 0
    )
  )
  val midModPayload = (
    Vec.fill(extIdxLim)(
      PipeMemRmwSimDutModType(
        optModHazardKind=optModHazardKind,
        optFormal=optFormal,
        cfg=pmCfg,
      )
    )
    .setName("midModPayload")
  )
  if (
    pipeMem.myHaveFormalFwd
    && PipeMemRmwSimDut.doTestModOp
  ) {
    for (extIdx <- 0 until extIdxLim) {
      assert(
        midModPayload(extIdx).op.asBits.asUInt
        < PipeMemRmwSimDut.postMaxModOp.asBits.asUInt
      )
    }
  }
  val cMidModFront = (doMidMod) generate (
    CtrlLink(
      up=pmIo.modFront,
      down={
        val temp = Node()
        temp.setName(s"${pipeName}_cMidModFront_down")
        temp
      },
    )
  )
  val sMidModFront = (doMidMod) generate (
    StageLink(
      up=cMidModFront.down,
      down={
        //pmIo.modBack
        //Node()
        val temp = Node()
        temp.setName(s"${pipeName}_sMidModFront_down")
        temp

        //pmIo.modBack
      },
    )
  )
  val s2mMidModFront = (doMidMod) generate (
    S2MLink(
      up=sMidModFront.down,
      down=(
        pmIo.modBack
      ),
    )
  )
  val formalFwdMidModArea = (pipeMem.myHaveFormalFwd) generate (
    new Area {
      val myFwd = (
        KeepAttribute(
          Vec.fill(extIdxLim)(
            pipeMem.mkFwd()
          )
        )
        .setName(
          s"formalFwdMidModArea_"
          + s"myFwd"
        )
      )
      for (extIdx <- 0 until extIdxLim) {
        myFwd(extIdx) := midModPayload(extIdx).myFwd
        //myFwd(extIdx).myFindFirst_0.allowOverride
        //myFwd(extIdx).myFindFirst_1.allowOverride
      }
      val doFormalFwdUp =  (
        PipeMemRmwDoFwdArea(
          fwdAreaName=s"formalFwdMidModArea_doFormalFwdUp",
          fwd=(
            myFwd(extIdxUp)
            //midModPayload(extIdxUp).myFwd
          ),
          setToMyFwdDataFunc=(
            ydx: Int,
            zdx: Int,
            myFwdData: UInt,
          ) => {
            //when (pastValidAfterReset) {
              assert(
                midModPayload(extIdxUp).myExt(0).rdMemWord(0)
                === myFwdData
              )
            //}
          }
        )
      )
      val doFormalFwdSaved =  (
        PipeMemRmwDoFwdArea(
          fwdAreaName=s"formalFwdMidModArea_doFormalFwdSaved",
          fwd=(
            myFwd(extIdxSaved)
            //midModPayload(extIdxSaved).myFwd
          ),
          setToMyFwdDataFunc=(
            ydx: Int,
            zdx: Int,
            myFwdData: UInt,
          ) => {
            //when (pastValidAfterReset) {
              assert(
                midModPayload(extIdxSaved).myExt(0).rdMemWord(0)
                === myFwdData
              )
            //}
          }
        )
      )
    }
  )
  if (
    doMidMod
  ) {
    //if (optModHazardKind == PipeMemRmw.ModHazardKind.Fwd) {
      assert(modStageCnt == 1)
    //}
    pipeMem.myLinkArr += cMidModFront
    pipeMem.myLinkArr += sMidModFront
    pipeMem.myLinkArr += s2mMidModFront
    //--------
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
    //--------
    if (pipeMem.myHaveFormalFwd) {
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
        def myExt(
          someExtIdx: Int
        ) = (
          midModPayload(someExtIdx).myExt
        )
        def myFwd(
          someExtIdx: Int
        ) = (
          midModPayload(someExtIdx).myFwd
        )
        when (
          !RegNextWhen(
            next=True,
            cond=cMidModFront.up.isFiring,
            init=False,
          )
        ) {
          when (!cMidModFront.up.isValid) {
            assert(
              myExt(extIdxUp)(0).main
              === myExt(extIdxUp)(0).main.getZero
            )
            assert(
              myFwd(extIdxUp)
              === myFwd(extIdxUp).getZero
            )
          }
          assert(
            myExt(extIdxSaved)(0)
            === myExt(extIdxSaved)(0).getZero
          )
          assert(
            myFwd(extIdxSaved)
            === myFwd(extIdxSaved).getZero
          )
        } 
        when (
          past(cMidModFront.up.isFiring) init(False)
        ) {
          //assert(
          //  myExt(extIdxSaved)(0)
          //  === (past(myExt(extIdxUp)(0)) init(myExt(extIdxUp)(0).getZero))
          //)
          //assert(
          //  myFwd(extIdxSaved)
          //  === (past(myFwd(extIdxUp)) init(myFwd(extIdxUp).getZero))
          //)
          assert(
            midModPayload(extIdxSaved)
            === past(midModPayload(extIdxUp))
          )
        }
        //when (!past(up.isValid) init(False)) {
        //  assert(
        //    upFwd
        //  )
        //}
        when (
          !cMidModFront.up.isValid
          && !past(cMidModFront.up.isValid)
        ) {
          assert(
            stable(myExt(extIdxUp)(0).memAddr(0))
          )
          assert(
            stable(myExt(extIdxUp)(0).rdMemWord(0))
          )
          when (
            midModPayload(extIdxUp).op
            =/= PipeMemRmwSimDut.ModOp.LdrRaRb
          ) {
            //assert(
            //  stable(myExt(extIdxUp)(0).rdMemWord(0))
            //)
            assert(
              stable(myExt(extIdxUp)(0).modMemWord(0))
            )
          }
          assert(
            stable(myExt(extIdxSaved)(0))
          )
          assert(
            stable(myFwd(extIdxUp))
          )
          assert(
            stable(myFwd(extIdxSaved))
          )
        }
        when (
          cMidModFront.up.isValid
          && !rSetMidModPayloadState
        ) {
          assert(
            myFwd(extIdxUp)
            === cMidModFront.up(modFrontPayload).myFwd
          )
          assert(
            myExt(extIdxUp)(0).rdMemWord(0)
            === cMidModFront.up(modFrontPayload).myExt(0).rdMemWord(0)
          )
          assert(
            myExt(extIdxUp)(0).memAddr(0)
            === cMidModFront.up(modFrontPayload).myExt(0).memAddr(0)
          )
          //assert(
          //  myExt(extIdxUp)(0).modMemWordValid
          //  === cMidModFront.up(modFrontPayload).myExt(0).modMemWordValid
          //)
          when (
            cMidModFront.up(modFrontPayload).myExt(0).modMemWordValid
          ) {
            assert(
              myExt(extIdxUp)(0).modMemWordValid
            )
          }
        }
      }
    }

    pmIo.tempModFrontPayload := midModPayload(extIdxUp)
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
      .setName(s"psMem_savedPsMemStallHost")
    )
    if (optFormal) {
      when (pastValidAfterReset) {
        when (
          past(cMidModFront.up.isFiring) init(False)
        ) {
          assert(
            !rSetMidModPayloadState
          )
        }
        when (
          !(past(cMidModFront.up.isValid) init(False))
        ) {
          assert(
            stable(
              rSetMidModPayloadState
            )
          )
          //assert(
          //  past(
          //    !
          //  )
          //)
          //assert(
          //  
          //)
        } otherwise {
        }
        //when (
        //  cMidModFront.up.isValid
        //  //&& (
        //  //  !RegNextWhen(
        //  //    next=cMid0Front.up.isFiring,
        //  //  )
        //  //)
        //) {
        //  //assert
        //}
        when (
          rSetMidModPayloadState
        ) {
          assert(
            cMidModFront.up.isValid
          )
        }
      }
    }
    when (
      cMidModFront.up.isValid 
    ) {
      when (
        !rSetMidModPayloadState
        //|| !cMidModFront.up.isValid
      ) {
        //outp := inp
        midModPayload(extIdxUp) := pmIo.modFront(modFrontPayload)
        //midModPayload(extIdxUp).myExt(0).rdMemWord := (
        //  pmIo.modFront(modFrontPayload).myExt(0).rdMemWord
        //)
        //midModPayload(extIdxUp).myExt(0).valid := 
        nextSetMidModPayloadState := True

      }
      when (
        cMidModFront.up.isFiring
      ) {
        nextSetMidModPayloadState := False
      }
      //pmIo.tempModFrontPayload(0) := 
      //--------
      //--------
      if (PipeMemRmwSimDut.doTestModOp) {
        switch (
          //pmIo.modFront(modFrontPayload).op
          midModPayload(extIdxUp).op
        ) {
          is (PipeMemRmwSimDut.ModOp.AddRaRb) {
            //if (optFormal) {
            //  when (
            //    (
            //      RegNextWhen(
            //        next=pmIo.modFront(modFrontPayload).op,
            //        cond=pmIo.modFront.isFiring,
            //      )
            //      init(PipeMemRmwSimDut.ModOp.ADD_RA_RB)
            //    ) === (
            //      PipeMemRmwSimDut.ModOp.LDR_RA_RB
            //    )
            //  ) {
            //    assert(!savedPsMemStallHost.myDuplicateIt)
            //  }
            //}
            //when (!rSetMidModPayloadState) {
              midModPayload(extIdxUp).myExt(0).rdMemWord(0) := (
                pmIo.modFront(modFrontPayload).myExt(0).rdMemWord(0)
              )
              midModPayload(extIdxUp).myExt(0).memAddr(0) := (
                pmIo.modFront(modFrontPayload).myExt(0).memAddr(0)
              )
              midModPayload(extIdxUp).myExt(0).modMemWord := (
                pmIo.modFront(modFrontPayload).myExt(0).modMemWord
              )
              midModPayload(extIdxUp).myExt(0).modMemWordValid := (
                pmIo.modFront(modFrontPayload).myExt(0).modMemWordValid
              )
            //}
            if (optFormal) {
              //when (cMidModFront.up.isFiring) {
              //  assert(
              //    //!midModPayload(extIdxUp).myExt(0).modMemWordValid
              //    midModPayload(extIdxUp).myExt(0).modMemWordValid
              //    //=== pmIo.modFront(modFrontPayload).myExt(0).modMemWordValid
              //  )
              //}
              when (pastValidAfterReset) {
                when (
                  //(
                  //  !past(rSetMidModPayloadState)
                  //) && (
                  //  rSetMidModPayloadState
                  //)
                  rose(rSetMidModPayloadState)
                ) {
                  assert(
                    midModPayload(extIdxUp).myExt(0).memAddr(0)
                    === past(
                      pmIo.modFront(modFrontPayload).myExt(0).memAddr(0)
                    )
                  )
                  assert(
                    midModPayload(extIdxUp).myExt(0).rdMemWord
                    === past(
                      pmIo.modFront(modFrontPayload).myExt(0).rdMemWord
                    )
                  )
                  assert(
                    midModPayload(extIdxUp).myExt(0).modMemWord
                    === past(
                      pmIo.modFront(modFrontPayload).myExt(0).modMemWord
                    )
                  )
                  assert(
                    midModPayload(extIdxUp).myExt(0).modMemWordValid
                    === past(
                      pmIo.modFront(modFrontPayload).myExt(0)
                      .modMemWordValid
                    )
                  )
                } otherwise {
                }
              }
              //when (cMidModFront.up.isFiring) {
              //  when (
              //    //cMidModFront.up.isFiring
              //    //pmIo.modFront(modFrontPayload).myExt(0).valid
              //    //midModPayload(extIdxUp).myExt(0).valid
              //    //midModPayload(extIdxUp).myExt(0).modMemWordValid
              //    (
              //      (
              //        !rSetMidModPayloadState
              //      ) && (
              //        pmIo.modFront(modFrontPayload).myExt(0).valid
              //      )
              //    ) || (
              //      (
              //        rSetMidModPayloadState
              //      ) && (
              //        RegNextWhen(
              //          next=pmIo.modFront(modFrontPayload).myExt(0).valid,
              //          cond=pmIo.modFront.isFiring,
              //          init=False,
              //        )
              //      )
              //    )
              //  ) {
              //    //assert(
              //    //  pmIo.modFront(modFrontPayload).myExt(0).valid
              //    //)
              //    assert(
              //      //pmIo.modFront(modFrontPayload).myExt(0).modMemWordValid
              //      midModPayload(extIdxUp).myExt(0).modMemWordValid
              //      //midModPayload(extIdxUp).myExt(0).valid
              //    )
              //  }
              //}
            }
          }
          is (PipeMemRmwSimDut.ModOp.LdrRaRb) {
            when (!midModPayload(extIdxUp).myExt(0).modMemWordValid) {
              //if (optFormal) {
              //  when (
              //    RegNextWhen(
              //      pmIo.modFront(modFrontPayload).op,
              //      pmIo.modFront.isFiring,
              //    ) =/= (
              //      PipeMemRmwSimDut.ModOp.LDR_RA_RB
              //    )
              //  ) {
              //    assert(!savedPsMemStallHost.myDuplicateIt)
              //  }
              //}
              //midModPayload(extIdxUp).myExt(0).modMemWord := (
              //  //pmIo.modFront(modFrontPayload).myExt(0).modMemWord
              //  midModPayload(extIdxUp).myExt(0).modMemWord.getZero
              //)
            } otherwise {
              if (optFormal) {
                assert(!savedPsMemStallHost.myDuplicateIt)
              }
            }
            if (optFormal) {
              when (cMidModFront.up.isFiring) {
                assert(
                  midModPayload(extIdxUp).myExt(0).modMemWordValid
                )
              }
            }
          }
          is (PipeMemRmwSimDut.ModOp.MulRaRb) {
            //if (optFormal) {
            //  assert(!savedPsMemStallHost.myDuplicateIt)
            //}
            //if (optFormal) {
            //  when (cMidModFront.up.isFiring) {
            //    assert(
            //      //!midModPayload(extIdxUp).myExt(0).modMemWordValid
            //      midModPayload(extIdxUp).myExt(0).modMemWordValid
            //      //=== pmIo.modFront(modFrontPayload).myExt(0).modMemWordValid
            //    )
            //  }
            //  //when (
            //  //  (
            //  //    RegNextWhen(
            //  //      next=pmIo.modFront(modFrontPayload).op,
            //  //      cond=pmIo.modFront.isFiring,
            //  //    )
            //  //    init(PipeMemRmwSimDut.ModOp.ADD_RA_RB)
            //  //  ) === (
            //  //    PipeMemRmwSimDut.ModOp.LDR_RA_RB
            //  //  )
            //  //) {
            //  //  assert(!savedPsMemStallHost.myDuplicateIt)
            //  //}
            //}
            //midModPayload(extIdxUp).myExt(0).modMemWord := (
            //  pmIo.modFront(modFrontPayload).myExt(0).modMemWord
            //)
            //when (!rSetMidModPayloadState) {
              midModPayload(extIdxUp).myExt(0).memAddr(0) := (
                pmIo.modFront(modFrontPayload).myExt(0).memAddr(0)
              )
              midModPayload(extIdxUp).myExt(0).rdMemWord(0) := (
                pmIo.modFront(modFrontPayload).myExt(0).rdMemWord(0)
              )
              midModPayload(extIdxUp).myExt(0).modMemWord := (
                pmIo.modFront(modFrontPayload).myExt(0).modMemWord
              )
              midModPayload(extIdxUp).myExt(0).modMemWordValid := (
                pmIo.modFront(modFrontPayload).myExt(0).modMemWordValid
              )
            //}
            if (optFormal) {
              when (pastValidAfterReset) {
                when (
                  //(
                  //  !past(rSetMidModPayloadState)
                  //) && (
                  //  rSetMidModPayloadState
                  //)
                  rose(
                    rSetMidModPayloadState
                  )
                ) {
                  assert(
                    midModPayload(extIdxUp).myExt(0).memAddr(0)
                    === past(
                      pmIo.modFront(modFrontPayload).myExt(0).memAddr(0)
                    )
                  )
                  assert(
                    midModPayload(extIdxUp).myExt(0).rdMemWord
                    === past(
                      pmIo.modFront(modFrontPayload).myExt(0).rdMemWord
                    )
                  )
                  assert(
                    midModPayload(extIdxUp).myExt(0).modMemWord
                    === past(
                      pmIo.modFront(modFrontPayload).myExt(0).modMemWord
                    )
                  )
                  assert(
                    midModPayload(extIdxUp).myExt(0).modMemWordValid
                    === past(
                      pmIo.modFront(modFrontPayload).myExt(0).modMemWordValid
                    )
                  )
                } otherwise {
                }
              }
            }
          }
        }
      }
    } otherwise {
      //assert(
      //  midModPayload
      //)
    }
    if (optFormal) {
      when (pastValidAfterReset) {
        when (
          (
            ///*past*/(cMidModFront.up.isValid) //init(False)
            True
          )
          //&& (
          //  True
          //  //RegNextWhen(
          //  //  next=True,
          //  //  cond=cMidModFront.up.isValid,
          //  //  init=False,
          //  //)
          //)
        ) {
          when (
            //rose(rSetMidModPayloadState)
            (
              /*past*/(cMidModFront.up.isValid) //init(False)
            ) && (
              !(
                /*past*/(rSetMidModPayloadState) //init(False)
              )
            )
            //|| !nextSetMidModPayloadState
          ) {
            assert(
              midModPayload(extIdxUp).myExt(0).rdMemWord
              === /*past*/(
                pmIo.modFront(modFrontPayload).myExt(0).rdMemWord
              )
            )
            assert(
              midModPayload(extIdxUp).op
              === /*past*/(pmIo.modFront(modFrontPayload).op)
            )
            assert(
              midModPayload(extIdxUp).opCnt
              === /*past*/(pmIo.modFront(modFrontPayload).opCnt)
            )
          } 
          when (
            rose(rSetMidModPayloadState)
          ) {
            assert(
              stable(midModPayload(extIdxUp).myExt(0).rdMemWord)
            )
            switch (midModPayload(extIdxUp).op) {
              is (PipeMemRmwSimDut.ModOp.AddRaRb) {
                assert(
                  stable(
                    midModPayload(extIdxUp).myExt(0).modMemWord
                  )
                )
                when (
                  midModPayload(extIdxUp).myExt(0).modMemWordValid
                ) {
                  assert(
                    midModPayload(extIdxUp).myExt(0).modMemWord
                    === midModPayload(extIdxUp).myExt(0).rdMemWord(0) + 1
                  )
                }
              }
              //is (PipeMemRmwSimDut.ModOp.ADD_RA_RB) {
              //  //assert(
              //  //  stable(
              //  //    midModPayload(extIdxUp).myExt(0).modMemWord
              //  //  )
              //  //)
              //}
            }
            //assert(
            //  stable(midModPayload(extIdxUp).myExt(0).modMemWord)
            //)
            assert(
              stable(midModPayload(extIdxUp).op)
            )
            assert(
              stable(midModPayload(extIdxUp).opCnt)
            )
          }
        }
      }
      ////when (
      ////  !cMidModFront.up.isFiring
      ////) {
      //  assert(
      //    midModPayload(extIdxUp).myExt(0).rdMemWord
      //    === pmIo.modFront(modFrontPayload).myExt(0).rdMemWord
      //  )
      //  //assert(
      //  //  midModPayload(extIdxUp).myExt(0).rdMemWord
      //  //  === pmIo.modFront(modFrontPayload).myExt(0).rdMemWord
      //  //)
      //  //assert(
      //  //  //!midModPayload(extIdxUp).myExt(0).modMemWordValid
      //  //  midModPayload(extIdxUp).myExt(0).modMemWordValid
      //  //  === pmIo.modFront(modFrontPayload).myExt(0).modMemWordValid
      //  //)

      //  assert(
      //    midModPayload(extIdxUp).op
      //    === pmIo.modFront(modFrontPayload).op
      //  )
      //  assert(
      //    midModPayload(extIdxUp).opCnt
      //    === pmIo.modFront(modFrontPayload).opCnt
      //  )
      ////}
      ////assume(
      ////  midModPayload(extIdxUp).op
      ////  === pmIo.modFront(modFrontPayload).op
      ////)
    }
    if (optFormal) {
    }
    //midModPayload(extIdxUp).myExt(0).rdMemWord(0) := (
    //  pmIo.modFront(modFrontPayload).myExt(0).rdMemWord(0)
    //)
    //if (optFormal) {
    //  when (!rSetMidModPayloadState) {
    //  }
    //}
    //--------
    midModPayload(extIdxUp).myExt(0).allowOverride
    midModPayload(extIdxUp).myExt(0).valid := (
      cMidModFront.up.isValid
    )
    midModPayload(extIdxUp).myExt(0).ready := (
      cMidModFront.up.isReady
    )
    midModPayload(extIdxUp).myExt(0).fire := (
      cMidModFront.up.isFiring
    )
    //val rSavedModMemWord = KeepAttribute(
    //  Reg(
    //    dataType=(
    //      cloneOf(pmIo.modFront(modFrontPayload).myExt(0).modMemWord)
    //    ),
    //    init=pmIo.modFront(modFrontPayload).myExt(0).modMemWord.getZero,
    //  )
    //)
    //  .setName("rSavedModMemWord")
    val myModMemWord = (
      pmIo.modFront(modFrontPayload).myExt(0).rdMemWord
      (
        PipeMemRmw.modWrIdx
      )
      + 1
      //+ 2
    )
    def myProveNumCycles = PipeMemRmwFormal.myProveNumCycles
    if (PipeMemRmwSimDut.doTestModOp) {
      if (optFormal) {
        cover(
          (
            RegNextWhen(
              next=RegNextWhen(
                next=True,
                cond=(
                  (
                    //midModPayload(extIdxUp).op
                    pmIo.modFront(modFrontPayload).op
                    === PipeMemRmwSimDut.ModOp.LdrRaRb
                  ) && (
                    cMidModFront.up.isFiring
                  )
                ),
                init=False,
              ),
              cond=(
                (
                  //midModPayload(extIdxUp).op
                  pmIo.modFront(modFrontPayload).op
                  === PipeMemRmwSimDut.ModOp.AddRaRb
                ) && (
                  cMidModFront.up.isFiring
                )
              ),
              init=False,
            )
          )
        )
      }

      when (
        midModPayload(extIdxUp).op
        //pmIo.modFront(modFrontPayload).op
        === PipeMemRmwSimDut.ModOp.LdrRaRb
      ) {
        //when (cMidModFront.up.isValid) {
          //--------
          when (savedPsMemStallHost.myDuplicateIt) {
            //--------
            cMidModFront.duplicateIt()
            //--------
            midModPayload(extIdxUp).myExt(0).modMemWordValid := False
            //--------
          } otherwise {
            //--------
            midModPayload(extIdxUp).myExt(0).modMemWordValid := True
            //--------
            midModPayload(extIdxUp).myExt(0).modMemWord := (
              if (PipeMemRmwSimDut.allModOpsSameChange) (
                midModPayload(extIdxUp).myExt(0).rdMemWord(0) + 1
              ) else (
                midModPayload(extIdxUp).myExt(0).rdMemWord(0) - 1
              )
            )
          }
        //} otherwise {
        //  //midModPayload(extIdxUp).myExt(0).modMemWordValid := (
        //  //  RegNext(
        //  //    next=midModPayload(extIdxUp).myExt(0).modMemWordValid,
        //  //    init=False,
        //  //  )
        //  //)
        //}
        //--------
      }
      if (optFormal) {
        when (pastValidAfterReset) {
          val tempMyFindFirstUp = (
            pipeMem.cMid0FrontArea.myFindFirst_0(0)(0)(extIdxUp),
            pipeMem.cMid0FrontArea.myFindFirst_1(0)(0)(extIdxUp),
          )
          val tempMyFindFirstSaved = (
            pipeMem.cMid0FrontArea.myFindFirst_0(0)(0)(extIdxSaved),
            pipeMem.cMid0FrontArea.myFindFirst_1(0)(0)(extIdxSaved),
          )
          val myUpExtDel = (
            pipeMem.mod.front.myUpExtDel
          )
          val myUpExtDel2 = (
            pipeMem.mod.front.myUpExtDel2
          )
          println(
            s"${myUpExtDel2.size}"
          )
          when (
            past(pmIo.front.isFiring)
            && pmIo.front.isValid
          ) {
            assert(
              pmIo.front(frontPayload).opCnt
              === past(pmIo.front(frontPayload).opCnt) + 1
            )
          }
          //when (
          //  past(pipeMem.cMid0FrontArea.up.isFiring)
          //  && pipeMem.cMid0FrontArea.up.isValid
          //) {
          //  assert(
          //    pipeMem.cMid0FrontArea.tempUpMod(2)(0).opCnt
          //    === past(pipeMem.cMid0FrontArea.tempUpMod(2)(0).opCnt) + 1
          //  )
          //}
          //when (
          //  past(pmIo.modFront.isFiring)
          //  && pmIo.modFront.isValid
          //) {
          //  assert(
          //    pmIo.modFront(modFrontPayload).opCnt
          //    === past(pmIo.modFront(modFrontPayload).opCnt) + 1
          //  )
          //}
          //when (
          //  past(pmIo.modBack.isFiring)
          //  && pmIo.modBack.isValid
          //) {
          //  assert(
          //    pmIo.modBack(modBackPayload).opCnt
          //    === past(pmIo.modBack(modBackPayload).opCnt) + 1
          //  )
          //}
          val myTempUpMod = pipeMem.cMid0FrontArea.tempUpMod(2)
          when (
            !dut.tempHadFrontIsFiring._1
          ) {
            assert(
              !myTempUpMod.myExt(0).modMemWordValid
            )
            assert(
              !pmIo.modFront(modFrontPayload).myExt(0).modMemWordValid
            )
            assert(
              !pmIo.modBack(modBackPayload).myExt(0).modMemWordValid
            )
            //assert(
            //  !pmIo.back(backPayload).myExt(0).modMemWordValid
            //)
            //assert(
            //  midModPayload
            //  === midModPayload.getZero
            //)
            assert(
              !dut.myHaveCurrWrite
            )

            assert(
              !dut.tempHadMid0FrontUpIsValid._1
            )
            assert(
              !dut.tempHadMid0FrontDownIsFiring._1
            )
            assert(
              !dut.tempHadModFrontIsValid._1
            )
            assert(
              !dut.tempHadModBackIsFiring._1
            )
            assert(
              !dut.tempHadBackIsFiring._1
            )
            assert(
              !dut.myHaveSeenPipeToWrite
            )
            assert(
              !tempMyFindFirstUp._1
            )
            assert(
              !tempMyFindFirstSaved._1
            )
            assert(
              !pipeMem.cMid0FrontArea.up.isValid
            )
            assert(
              !pmIo.modFront.isValid
            )
            assert(
              !pmIo.modBack.isValid
            )
            assert(
              !pmIo.back.isValid
            )
          }
          when (
            !dut.tempHadMid0FrontUpIsValid._1
          ) {
            //when (!pipeMem.cMid0FrontArea.up.isValid) {
            //  assert(
            //    !myTempUpMod.myExt(0).modMemWordValid
            //  )
            //}
            assert(
              !dut.myHaveCurrWrite
            )
            when (
              !pipeMem.cMid0FrontArea.up.isValid
            ) {
              //assert(
              //  //!pmIo.modFront(modFrontPayload).myExt(0).modMemWordValid
              //  pmIo.modFront(modFrontPayload)
              //  === pmIo.modFront(modFrontPayload).getZero
              //)
              //assert(
              //  midModPayload
              //  === midModPayload.getZero
              //)
              assert(
                !pmIo.modFront(modFrontPayload).myExt(0).modMemWordValid
              )
              assert(
                !midModPayload(extIdxUp).myExt(0).modMemWordValid
              )
              assert(
                !tempMyFindFirstUp._1
              )
              assert(
                !tempMyFindFirstSaved._1
              )
            }
            assert(
              !pmIo.modBack(modBackPayload).myExt(0).modMemWordValid
            )
            //assert(
            //  !pmIo.back(backPayload).myExt(0).modMemWordValid
            //)
            assert(
              !dut.tempHadMid0FrontDownIsFiring._1
            )
            assert(
              !dut.tempHadModFrontIsValid._1
            )
            assert(
              !dut.tempHadModBackIsValid._1
            )
            assert(
              !dut.tempHadModBackIsFiring._1
            )
            assert(
              !dut.myHaveSeenPipeToWrite
            )
            assert(
              !dut.tempHadBackIsFiring._1
            )
            assert(
              !pmIo.modFront.isValid
            )
            assert(
              !pmIo.modBack.isValid
            )
            assert(
              !pmIo.back.isValid
            )
            //--------
            //--------
            //assert(
            //  !pipeMem.cMid0FrontArea.up.isValid
            //)
            //when (
            //  //!pipeMem.cMid0FrontArea.up.isReady
            //  !dut.tempHadMid0FrontDownIsFiring._1
            //) {
            //  //assert(
            //  //  !pmIo.modFront.isFiring
            //  //)
            //  assert(
            //    !pmIo.modBack.isValid
            //  )
            //  assert(
            //    !pmIo.back.isValid
            //  )
            //}
            //--------
            //when (
            //  pipeMem.cMid0FrontArea.up.isValid
            //) {
            //  val tempUpMod = (
            //    pipeMem.cMid0FrontArea.tempUpMod
            //  )
            //  when (
            //    tempUpMod(2)(0).op
            //    =/= PipeMemRmwSimDut.ModOp.LDR_RA_RB
            //  ) {
            //    assert(
            //      !tempMyFindFirstUp._1
            //    )
            //  }
            //}
            //--------
            //assert(
            //  tempUpMod(1).myExt(0).rdMemWord(0)
            //  === 
            //)
          } 
          when (
            !dut.tempHadModFrontIsValid._1
          ) {
            //--------
            assert(
              !dut.tempHadModBackIsValid._1
            )
            assert(
              !dut.tempHadModBackIsFiring._1
            )
            assert(
              !dut.myHaveSeenPipeToWrite
            )
            assert(
              !dut.tempHadBackIsFiring._1
            )
            //assert(
            //  !pmIo.modFront.isValid
            //)
            assert(
              !pmIo.modBack.isValid
            )
            assert(
              !pmIo.back.isValid
            )
            //when (!pmIo.modBack.isValid) {
            //  assert(
            //    !pmIo.modBack(modBackPayload).myExt(0).modMemWordValid
            //  )
            //}
            //--------
            when (
              pmIo.modFront.isValid
            ) {
              when (pastValidAfterReset) {
                assert(
                  dut.tempHadMid0FrontDownIsValid._1
                  || pipeMem.cMid0FrontArea.down.isValid
                  || (
                    past(pipeMem.cMid0FrontArea.down.isValid) init(False)
                  )
                )
              }
              //assert(
              //  pmIo.modFront(modFrontPayload).myExt(0).rdMemWord(0)
              //  === (
              //    //RegNextWhen(
              //    //  next=
              //      myUpExtDel(0)(0)(extIdxSaved).rdMemWord(0),
              //    //  cond=pmIo.modFront.isFiring
              //    //)
              //  )
              //)
              //--------
              //when (tempMyFindFirstUp._1) {
              //  when (
              //    //!pmIo.modFront.isFiring
              //    !midModPayload(extIdxUp).myExt(0).modMemWordValid
              //  ) {
              //    assert(
              //      tempMyFindFirstUp._2 === 0x0
              //    )
              //  } otherwise {
              //    assert(
              //      tempMyFindFirstUp._2 <= 0x1
              //    )
              //  }
              //}
              //--------
              //when (tempMyFindFirstSaved._1) {
              //  assert(
              //    tempMyFindFirstSaved._2 === 0x0
              //  )
              //}
            } otherwise {
              //when (!dut.tempHadMid0FrontUpIsFiring._1) {
              //}
              //assert(
              //  midModPayload
              //  === midModPayload.getZero
              //)
              //assert(
              //  //!pmIo.modFront(modFrontPayload).myExt(0).modMemWordValid
              //  pmIo.modFront(modFrontPayload)
              //  === pmIo.modFront(modFrontPayload).getZero
              //)
              //assert(
              //  pmIo.modBack(modBackPayload)
              //  === pmIo.modBack(modBackPayload).getZero
              //)
              assert(
                !dut.tempHadBackIsValid._1
              )
              assert(
                !dut.myHaveCurrWrite
              )
            }
            //--------
          }
          //when (
          //  !dut
          //)
          when (
            !dut.tempHadModBackIsValid._1
          ) {
            when (!pmIo.modBack.isValid) {
              //assert(
              //  pmIo.modBack(modBackPayload)
              //  === pmIo.modBack(modBackPayload).getZero
              //)
              assert(
                !pipeMem.mod.back.myWriteEnable(0)
              )
              assert(
                !dut.myHaveCurrWrite
              )
            }
            when (
              !pmIo.modBack.isValid
            ) {
              assert(
                !pmIo.back.isValid
              )
              assert(
                !dut.myHaveCurrWrite
              )
              assert(
                !dut.tempHadBackIsValid._1
              )
              assert(
                !pmIo.back.isValid
              )
              assert(
                !pmIo.back.isFiring
              )
              assert(
                !dut.tempHadBackIsFiring._1
              )
            }
            assert(
              !dut.tempHadModBackIsFiring._1
            )
          } otherwise {
            assert(
              dut.tempHadMid0FrontDownIsValid._1
            )
          }
          when (
            !dut.tempHadModBackIsFiring._1
          ) {
            //assert(
            //  !dut.tempHadMid0FrontDownIsFiring._1
            //)
            //--------
            //when (
            //  !pmIo.modBack.isValid
            //  && !dut.tempHadModBackIsValid._1
            //) {
            //  assert(
            //    !dut.myHaveSeenPipeToWrite
            //  )
            //}
            //--------
            //when (
            //  !pmIo.modBack.isValid
            //) {
            //  assert(
            //    !pmIo.back.isValid
            //  )
            //  assert(
            //    !dut.myHaveCurrWrite
            //  )
            //  assert(
            //    !dut.tempHadBackIsValid._1
            //  )
            //  assert(
            //    !pmIo.back.isValid
            //  )
            //  assert(
            //    !pmIo.back.isFiring
            //  )
            //  assert(
            //    !dut.tempHadBackIsFiring._1
            //  )
            //}
            //--------
            //when (
            //  (
            //    !pmIo.modBack.isValid
            //  ) && (
            //    !dut.tempHadModBackIsValid._1
            //  )
            //) {
            //  assert(
            //    !pmIo.modBack(modBackPayload).myExt(0).modMemWordValid
            //  )
            //  assert(
            //    !pipeMem.mod.back.myWriteEnable(0)
            //  )
            //}
            //--------
            //when (
            //  pmIo.modBack.isValid
            //  && (
            //    //pmIo.modBack.isFiring
            //    //!dut.myHaveSeenPipeToWrite
            //    !pipeMem.mod.back.myWriteEnable(0)
            //  )
            //) {
            //  //assert(
            //  //  pmIo.modBack(modBackPayload).myExt(0).rdMemWord(0)
            //  //  === (
            //  //    //RegNextWhen(
            //  //    //  next=
            //  //      myUpExtDel2(0)(0)(extIdxSaved).rdMemWord(0),
            //  //    //  cond=pmIo.modFront.isFiring
            //  //    //)
            //  //  )
            //  //)
            //  when (tempMyFindFirstUp._1) {
            //    assert(
            //      tempMyFindFirstUp._2 <= 0x1
            //    )
            //  }
            //  //when (tempMyFindFirstSaved._1) {
            //  //  assert(
            //  //    tempMyFindFirstSaved._2 <= 0x1
            //  //  )
            //  //}
            //}
          } otherwise {
            assert(
              dut.tempHadMid0FrontDownIsValid._1
              || pipeMem.cMid0FrontArea.down.isValid
              || (
                past(pipeMem.cMid0FrontArea.down.isValid) init(False)
              )
            )
          }
          //when (
          //  !dut.tempHadMid0FrontUpIsFiring
          //)
          //when (
          //  !(
          //    dut.tempHadFrontIsFiring._1
          //    && dut.tempHadMid0FrontUpIsFiring._1
          //  )
          //) {
          //  assert(
          //    !tempMyFindFirstUp._1
          //  )
          //  assert(
          //    !tempMyFindFirstSaved._1
          //  )
          //  assert(
          //    !dut.myHaveSeenPipeToWrite
          //  )
          //} elsewhen (
          //  !dut.tempHadModFrontIsValid._1
          //  //!dut.tempHadModFrontIsFiring._1
          //) {
          //  when (tempMyFindFirstUp._1) {
          //    assert(
          //      tempMyFindFirstUp._2 === 0x0
          //    )
          //  }
          //  when (tempMyFindFirstSaved._1) {
          //    assert(
          //      tempMyFindFirstSaved._2 === 0x0
          //    )
          //  }
          //  //for (idx <- 0 until myUpExtDel2.size) {
          //  //  assert(
          //  //  )
          //  //}
          //} elsewhen (
          //  dut.myHaveSeenPipeToModFrontFire
          //  && !(
          //    pmIo.modBack.isValid
          //  )
          //) {
          //  when (tempMyFindFirstUp._1) {
          //    assert(
          //      tempMyFindFirstUp._2 <= 0x1
          //    )
          //  }
          //  when (tempMyFindFirstSaved._1) {
          //    assert(
          //      tempMyFindFirstSaved._2 <= 0x1
          //    )
          //  }
          //}
          when (
            cMidModFront.up.isValid
            && dut.tempHadFrontIsFiring._1
            //&& dut.tempHadMid0FrontUpIsFiring._1
            && dut.tempHadMid0FrontUpIsValid._1
            && (
              dut.tempHadMid0FrontDownIsValid._1
            )
          ) {
            //when (
            //  //dut.tempHadMid0FrontDownIsFiring._1
            //  past(pipeMem.cMid0FrontArea.up.isFiring)
            //  && (
            //    pmIo.modFront.isFiring
            //  )
            //  //True
            //) {
            //  //val myTempUpMod = pipeMem.cMid0FrontArea.tempUpMod(2)(0)
            //  val myTempUpExt = (
            //    /*past*/(pipeMem.mod.front.myUpExtDel(0)(0)(extIdxSaved))
            //  )
            //  assert(
            //    //midModPayload(extIdxUp).myExt(0).rdMemWord(0)
            //    pmIo.modFront(modFrontPayload).myExt(0).rdMemWord(0)
            //    === (
            //      ////past(
            //      //  myTempUpMod.myExt(0).rdMemWord(0)
            //      ////)
            //      /*past*/(myTempUpExt.rdMemWord(0))
            //    )
            //  )
            //}
            for (extIdx <- 0 until extIdxLim) {
              when (
                if (extIdx == extIdxUp) (
                  //True
                  cMidModFront.up.isFiring
                ) else ( // if (extIdx == extIdxSaved)
                  //past(cMidModFront.up.isFiring)
                  False
                )
              ) {
                switch (
                  //pmIo.modFront(modFrontPayload).op
                  midModPayload(extIdx).op
                ) {
                  is (PipeMemRmwSimDut.ModOp.AddRaRb) {
                    when (
                      midModPayload(extIdx).myExt(0).modMemWordValid
                    ) {
                      assert(
                        midModPayload(extIdx).myExt(0).modMemWord
                        === midModPayload(extIdx).myExt(0).rdMemWord(0)
                        + 1
                      )
                    }
                  }
                  is (PipeMemRmwSimDut.ModOp.LdrRaRb) {
                    when (
                      midModPayload(extIdx).myExt(0).modMemWordValid
                    ) {
                      if (PipeMemRmwSimDut.allModOpsSameChange) {
                        assert(
                          midModPayload(extIdx).myExt(0).modMemWord
                          === (
                            midModPayload(extIdx).myExt(0).rdMemWord(0)
                            + 1
                          )
                        )
                      } else {
                        assert(
                          midModPayload(extIdx).myExt(0).modMemWord
                          === (
                            midModPayload(extIdx).myExt(0).rdMemWord(0)
                            - 1
                          )
                        )
                      }
                    }
                  }
                  is (PipeMemRmwSimDut.ModOp.MulRaRb) {
                    when (
                      //dut.tempHadMid0FrontUpIsValid._1
                      //&& 
                      midModPayload(extIdx).myExt(0).modMemWordValid
                    ) {
                      if (PipeMemRmwSimDut.allModOpsSameChange) {
                        assert(
                          midModPayload(extIdx).myExt(0).modMemWord
                          === (
                            midModPayload(extIdx).myExt(0).rdMemWord(0) 
                            + 1
                          )
                        )
                      } else {
                        val tempBitsRange = (
                          wordType().bitsRange
                        )
                        assert(
                          midModPayload(extIdx).myExt(0).modMemWord(
                            tempBitsRange
                          ) === (
                            midModPayload(extIdx).myExt(0).rdMemWord(0) 
                            << 1
                          )(
                            tempBitsRange
                          )
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      //when (
      //  dut.psMemStallHost.fire
      //  && dut.psMemStallHost.rValid
      //) {
      //  midModPayload(extIdxUp).myExt(0).modMemWordValid := True
      //  midModPayload(extIdxUp).myExt(0).modMemWord := (
      //    if (PipeMemRmwSimDut.allModOpsSameChange) (
      //      midModPayload(extIdxUp).myExt(0).rdMemWord(0) + 1
      //    ) else (
      //      midModPayload(extIdxUp).myExt(0).rdMemWord(0) - 1
      //    )
      //  )
      //}
      //cover(
      //  dut.psMemStallHost.fire
      //  && dut.psMemStallHost.rValid
      //  //&& pmIo.tempModFrontPayload(0).myExt(0).modMemWordValid
      //)
    }

    def setMidModStages(): Unit = {
      pmIo.midModStages(0) := midModPayload
    }
    setMidModStages()

    //pmIo.modFront(modBackPayload) := RegNext(
    //  next=midModPayload(extIdxUp),
    //  init=midModPayload(extIdxUp).getZero
    //)
    pmIo.modFront(modBackPayload) := midModPayload(extIdxUp)
    when (pmIo.modFront.isValid) {
      //pmIo.modFront(modBackPayload) := midModPayload(extIdxUp)
    } otherwise {
    }

    if (optModHazardKind == PipeMemRmw.ModHazardKind.Dupl) {
      when (cMidModFront.down(modFrontPayload).myExt(0).hazardId.msb) {
        midModPayload(extIdxUp).myExt(0).modMemWord := (
          cMidModFront.down(modFrontPayload).myExt(0).rdMemWord(
            PipeMemRmw.modWrIdx
          ) + 0x1
        )
      }
    }
  } else if (
    optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
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
  //val formalFwdArea = (pipeMem.myHaveFormalFwd) generate (
  //  new Area {
  //    val myFwd = (
  //      Vec.fill(extIdxLim)(
  //        pipeMem.mkFwd()
  //      )
  //    )
  //    for (extIdx <- 0 until extIdxLim) {
  //      myFwd(extIdx) := midModPayload(extIdx).myFwd
  //    }
  //    val doFormalFwdUp =  (
  //      PipeMemRmwDoFwdArea(
  //        fwd=(
  //          myFwd(extIdxUp)
  //          //midModPayload(extIdxUp).myFwd
  //        ),
  //        setToMyFwdDataFunc=(
  //          ydx: Int,
  //          zdx: Int,
  //          myFwdData: UInt,
  //        ) => {
  //          //when (pastValidAfterReset) {
  //            assert(
  //              midModPayload(extIdxUp).myExt(0).rdMemWord(0)
  //              === myFwdData
  //            )
  //          //}
  //        }
  //      )
  //    )
  //    val doFormalFwdSaved =  (
  //      PipeMemRmwDoFwdArea(
  //        fwd=(
  //          myFwd(extIdxSaved)
  //          //midModPayload(extIdxSaved).myFwd
  //        ),
  //        setToMyFwdDataFunc=(
  //          ydx: Int,
  //          zdx: Int,
  //          myFwdData: UInt,
  //        ) => {
  //          //when (pastValidAfterReset) {
  //            assert(
  //              midModPayload(extIdxSaved).myExt(0).rdMemWord(0)
  //              === myFwdData
  //            )
  //          //}
  //        }
  //      )
  //    )
  //  }
  //)
  if (
    (
      optModHazardKind == PipeMemRmw.ModHazardKind.Fwd
    ) && (
      optFormal
    )
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
//          //payload.myExt(0) #= payload.myExt(0).getZero
//          ////payload.allowOverride
//          //payload.myExt(0).memAddr.randomize
//          //payload.randomize()
//          //payload.myExt(0).memAddr #= 
//          //if (
//          //  (
//          //    dut.io.front.valid.toBoolean
//          //  ) && (
//          //    dut.io.front.ready.toBoolean
//          //  )
//          //) {
//          //  def myMemAddr = payload.myExt(0).memAddr(PipeMemRmw.modWrIdx)
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
//        //dut.io.front.myExt(0).memAddr #= 
//        dut.clockDomain.waitRisingEdge()
//      }
//      simSuccess()
//    })
//}
