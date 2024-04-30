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
  def wordWidth = 8
  def wordType() = UInt(wordWidth bits)
  def wordCount = 4
  def modStageCnt = (
    //2
    1
  )
  def modType() = SamplePipeMemRmwModType(
    wordType=wordType(),
    wordCount=wordCount,
    modStageCnt=modStageCnt,
  )
  def forFmax = (
    //true
    false
  )
  //--------
  case class PipeMemRmwFormalDut() extends Component {
    val dut = FormalDut(PipeMemRmw[
      UInt,
      SamplePipeMemRmwModType[UInt],
      PipeMemRmwDualRdTypeDisabled[UInt],
    ](
      wordType=wordType(),
      wordCount=wordCount,
      modType=modType(),
      modStageCnt=modStageCnt,
      forFmax=forFmax,
    ))

    assumeInitial(clockDomain.isResetActive)
    def front = dut.io.front
    def modFront = dut.io.modFront
    def modBack = dut.io.modBack
    def back = dut.io.back

    assumeInitial(front.payload === front.payload.getZero)
    assumeInitial(front.valid === front.valid.getZero)
    anyseq(front.payload)
    anyseq(front.valid)
    //front.valid := True

    //assumeInitial(modFront.ready)
    //anyseq(modFront.ready)

    //assumeInitial(modBack.payload === modBack.payload.getZero)
    //assumeInitial(modBack.valid === modBack.valid.getZero)
    //anyseq(modBack.payload)
    //anyseq(modBack.valid)
    val modFrontStm = Stream(modType())
    val modBackStm = Stream(modType())
    modFrontStm <-/< modFront
    modFrontStm.translateInto(
      into=modBackStm
    )(
      dataAssignment=(
        modBackPayload,
        modFrontPayload,
      ) => {
        //modBackPayload.myExt := modFrontPayload.myExt
        modBackPayload := modFrontPayload
        modBackPayload.myExt.allowOverride
        modBackPayload.myExt.modMemWord := (
          modFrontPayload.myExt.rdMemWord + 0x1
        )
        when (
          modFrontPayload.myExt.hazardId.msb
        ) {
          modBackPayload.myExt.dbgModMemWord := (
            modBackPayload.myExt.modMemWord
          )
        } otherwise {
          modBackPayload.myExt.dbgModMemWord := 0x0
        }
      }
    )
    //modBack <-/< modBackStm
    modBack << modBackStm

    assumeInitial(back.ready)
    anyseq(back.ready)
    //back.ready := True
  }
  //--------
  new SpinalFormalConfig(
    _spinalConfig=SpinalConfig(
      defaultConfigForClockDomains=ClockDomainConfig(
        resetKind=SYNC,
      )
    ).includeFormal,
    _keepDebugInfo=true,
  )
    .withProve(40)
    .withCover(60)
    .doVerify(PipeMemRmwFormalDut())
  //--------
}

object PipeMemTestFormal extends App {
  def wordCount = 4
  new SpinalFormalConfig(
    _spinalConfig=SpinalConfig(
      defaultConfigForClockDomains=ClockDomainConfig(
        resetKind=SYNC,
      )
    ).includeFormal,
    _keepDebugInfo=true,
  )
    //.withBMC(40)
    .withProve(40)
    .withCover(60)
    .doVerify(
      //PipeMemTest(
      //  wordCount=wordCount
      //)
      new Component {
        val dut = FormalDut(PipeMemTest(
          wordCount=wordCount
        ))
        assumeInitial(clockDomain.isResetActive)
        def front = dut.io.front
        //val myFront = cloneOf(dut.io.front)
        def back = dut.io.back
        //front <-/< myFront

        //assumeInitial(myFront.payload === myFront.payload.getZero)
        //assumeInitial(myFront.valid === myFront.valid.getZero)
        ////anyseq(front.payload)
        //anyseq(myFront.valid)
        assumeInitial(front.payload === front.payload.getZero)
        assumeInitial(front.valid === front.valid.getZero)
        anyseq(front.payload)
        anyseq(front.valid)
        //myFront.valid := True
        //anyseq(myFront.data)
        //def addrExtWidth = 2
        //val rFrontAddrCnt = (
        //  Reg(
        //    UInt(log2Up(wordCount) + addrExtWidth bits)
        //  ) init(0x0)
        //)
        ////myFront.addr := RegNext(myFront.addr) init(myFront.addr.getZero)
        //myFront.addr := rFrontAddrCnt(
        //  rFrontAddrCnt.high downto addrExtWidth
        //)
        //when (myFront.fire) {
        //  rFrontAddrCnt := rFrontAddrCnt + 1
        //}
        assumeInitial(back.ready)
        anyseq(back.ready)
        //back.ready := True
      }
    )
}

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
