package libcheesevoyage.general

import spinal.core._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._
import scala.collection.mutable.ArrayBuffer
import scala.math._

import libcheesevoyage.Config

object PipeMemRmwFormal extends App {
  //--------
  //--------
  def myProveNumCycles = (
    //8
    10
    //15
    //16
  )
  case class PipeMemRmwFormalDutWithBranches() extends Component {
    val dut = FormalDut(
      PipeMemRmwTester(
        optFormal=true,
      )
    )

    assumeInitial(clockDomain.isResetActive)
    def front = dut.io.front //dut.pmIo.front
    //def modFront = dut.pmIo.modFront
    //def modBack = dut.dut.io.modBack
    def back = dut.io.back //dut.pmIo.back
    front.formalAssumesSlave(payloadInvariance=true)
    back.formalAssertsMaster(payloadInvariance=true)

    //def dualRdFront = dut.io.dualRdFront
    //def dualRdBack = dut.io.dualRdBack
    //val tempFrontPayload = dut.modType()

    assumeInitial(
      front.payload === front.payload.getZero
      //dut.pmIo.front(dut.frontPayload)
      //=== dut.pmIo.front(dut.frontPayload).getZero
      //tempFrontPayload === tempFrontPayload.getZero
    )
    //when (pastValidAfterReset) {
    //  when (front.fire) {
    //    assume(
    //      front.opCnt === past(front.opCnt) + 1
    //    )
    //  }
    //}
    //assumeInitial(
    //  front.valid === front.valid.getZero
    //)
    //--------
    //anyseq(
    //  front.dcacheHit
    //)
    anyseq(
      front.payload
      //front.op
      ////tempFrontPayload
    )
    //anyseq(
    //  front.opCnt
    //)
    //anyseq(
    //  front.myExt.memAddr
    //)
    //assume(
    //  front.op =/= PipeMemRmwSimDut.ModOp.MUL_RA_RB
    //)
    assume(
      front.op.asBits.asUInt
      //< PipeMemRmwSimDut.ModOp.MUL_RA_RB.asBits.asUInt
      < PipeMemRmwSimDut.postMaxModOp.asBits.asUInt
    )
    when (pastValidAfterReset) {
      when (front.fire) {
        assume(
          front.opCnt === past(front.opCnt) + 1
        )
      } otherwise {
        //assume(
        //  stable(front.dcacheHit)
        //)
        //assume(
        //  stable(front.finishedOp)
        //)
        assume(
          stable(front.op)
        )
        assume(
          stable(front.opCnt)
        )
        assume(
          stable(front.myExt(0).memAddr)
        )
      }
    }
    //assume(
    //  front.finishedOp
    //  === front.finishedOp.getZero
    //)
    assume(
      front.myExt(0).modMemWord
      === front.myExt(0).modMemWord.getZero
    )
    assume(
      front.myExt(0).rdMemWord
      === front.myExt(0).rdMemWord.getZero
    )
    assume(
      front.myExt(0).hazardCmp === front.myExt(0).hazardCmp.getZero
    )
    for (idx <- 0 until front.myExt(0).modMemWordValid.size) {
      
      //assume(
      //  front.myExt(0).modMemWordValid === True
      //)
      front.myExt(0).modMemWordValid.foreach(current => {
        assume(current === True)
      })
    }
    anyseq(front.valid)
    //front.valid := True
    //--------
    //front(dut.frontPayload) := tempFrontPayload
    //val rFrontMemAddrCnt = Reg(UInt(log2Up(wordCount) + 1 bits)) init(0x0)
    //front.payload.allowOverride
    //front.payload.myExt.memAddr := rFrontMemAddrCnt.resized
    //when (front.fire) {
    //  rFrontMemAddrCnt := rFrontMemAddrCnt + 1
    //}
    //assume(front.valid === True)
    ////front.valid := True
    //assumeInitial(dualRdFront.payload === dualRdFront.payload.getZero)
    //assumeInitial(dualRdFront.valid === dualRdFront.valid.getZero)
    //anyseq(dualRdFront.payload)
    //anyseq(dualRdFront.valid)
    ////dualRdFront.valid := True
    ////dualRdFront.valid := front.valid

    //assumeInitial(modFront.ready)
    //anyseq(modFront.ready)

    //assumeInitial(modBack.payload === modBack.payload.getZero)
    //assumeInitial(modBack.valid === modBack.valid.getZero)
    //anyseq(modBack.payload)
    //anyseq(modBack.valid)


    //--------
    // BEGIN: old stuff for `back.ready`
    //assumeInitial(back.ready)
    // END: old stuff for `back.ready`
    //--------
    anyseq(back.ready)
    //back.ready.allowOverride
    //back.ready := True
    //back.ready := !(RegNext(back.ready) init(False))

    //assumeInitial(dualRdBack.ready)
    //anyseq(dualRdBack.ready)

    //dualRdBack.ready := !(RegNext(dualRdBack.ready) init(False))
    //dualRdBack.ready := back.ready
  }
  //--------
  new SpinalFormalConfig(
    _spinalConfig=SpinalConfig(
      defaultConfigForClockDomains=ClockDomainConfig(
        resetActiveLevel=HIGH,
        resetKind=SYNC,
      ),
      formalAsserts=true,
    ),
    _keepDebugInfo=true,
  )
    //.withBMC(
    //  //20
    //  //15
    //  //16
    //  PipeMemRmwFormal.myProveNumCycles
    //)
    .withProve(
      //20
      //40
      //10
      PipeMemRmwFormal.myProveNumCycles
    )
    .withCover(
      //PipeMemRmwFormal.myProveNumCycles
      //20
      //60
      //20
      15
    )
    .doVerify(PipeMemRmwFormalDutWithBranches())
  //--------
}

//object PipeMemTestFormal extends App {
//  def wordCount = 4
//  new SpinalFormalConfig(
//    _spinalConfig=SpinalConfig(
//      defaultConfigForClockDomains=ClockDomainConfig(
//        resetKind=SYNC,
//      )
//    ).includeFormal,
//    _keepDebugInfo=true,
//  )
//    //.withBMC(40)
//    .withProve(40)
//    .withCover(60)
//    .doVerify(
//      //PipeMemTest(
//      //  wordCount=wordCount
//      //)
//      new Component {
//        val dut = FormalDut(PipeMemTest(
//          wordCount=wordCount
//        ))
//        assumeInitial(clockDomain.isResetActive)
//        def front = dut.io.front
//        //val myFront = cloneOf(dut.io.front)
//        def back = dut.io.back
//        //front <-/< myFront
//
//        //assumeInitial(myFront.payload === myFront.payload.getZero)
//        //assumeInitial(myFront.valid === myFront.valid.getZero)
//        ////anyseq(front.payload)
//        //anyseq(myFront.valid)
//        assumeInitial(front.payload === front.payload.getZero)
//        assumeInitial(front.valid === front.valid.getZero)
//        anyseq(front.payload)
//        anyseq(front.valid)
//        //myFront.valid := True
//        //anyseq(myFront.data)
//        //def addrExtWidth = 2
//        //val rFrontAddrCnt = (
//        //  Reg(
//        //    UInt(log2Up(wordCount) + addrExtWidth bits)
//        //  ) init(0x0)
//        //)
//        ////myFront.addr := RegNext(myFront.addr) init(myFront.addr.getZero)
//        //myFront.addr := rFrontAddrCnt(
//        //  rFrontAddrCnt.high downto addrExtWidth
//        //)
//        //when (myFront.fire) {
//        //  rFrontAddrCnt := rFrontAddrCnt + 1
//        //}
//        assumeInitial(back.ready)
//        anyseq(back.ready)
//        //back.ready := True
//      }
//    )
//}

//case class PipeMemTestSpinalFormalBugFrontPayload
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
//
//case class PipeMemTestSpinalFormalBugDut(
//  wordWidth: Int,
//  wordCount: Int,
//) extends Component {
//  //--------
//  def debug: Boolean = {
//    GenerationFlags.formal {
//      return true
//    }
//    return false
//  }
//  //--------
//  val io = new Bundle {
//    val front = slave(Stream(
//      PipeMemTestSpinalFormalBugFrontPayload(
//        wordWidth=wordWidth,
//        wordCount=wordCount,
//      )
//    ))
//    //val back = master(Stream(
//    //  PipeMemTestBackPayload(
//    //    wordWidth=wordWidth,
//    //    wordCount=wordCount,
//    //    debug=debug
//    //  )
//    //))
//  }
//  //--------
//  val mem = Mem(
//    wordType=UInt(wordWidth bits),
//    initialContent={
//      Array.fill(wordCount)(U(f"$wordWidth'd0"))
//    }
//  )
//  io.front.ready := True
//  when (io.front.fire) {
//    mem.write(
//      address=io.front.addr,
//      data=io.front.data,
//    )
//  }
//}
//object PipeMemTestSpinalFormalBug extends App {
//  def wordWidth = 8
//  def wordCount = 4
//  new SpinalFormalConfig(
//    _spinalConfig=SpinalConfig(
//      defaultConfigForClockDomains=ClockDomainConfig(
//        resetKind=SYNC,
//      )
//    ).includeFormal,
//    _keepDebugInfo=true,
//  )
//    .withBMC(40)
//    .withProve(40)
//    .withCover(40)
//    .doVerify(
//      //PipeMemTest(
//      //  wordCount=wordCount
//      //)
//      new Component {
//        val dut = FormalDut(PipeMemTestSpinalFormalBugDut(
//          wordWidth=wordWidth,
//          wordCount=wordCount,
//        ))
//        assumeInitial(clockDomain.isResetActive)
//        def front = dut.io.front
//        //def back = dut.io.back
//
//        assumeInitial(front.payload === front.payload.getZero)
//        assumeInitial(front.valid === front.valid.getZero)
//        anyseq(front.payload)
//        anyseq(front.valid)
//        //assumeInitial(back.ready)
//        //anyseq(back.ready)
//      }
//    )
//}
