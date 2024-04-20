package libcheesevoyage.general

import spinal.core._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._
import scala.collection.mutable.ArrayBuffer
import scala.math._

import libcheesevoyage.Config

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
    .withCover(40)
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
        def back = dut.io.back

        assumeInitial(front.payload === front.payload.getZero)
        assumeInitial(front.valid === front.valid.getZero)
        anyseq(front.payload)
        anyseq(front.valid)
        assumeInitial(back.ready)
        anyseq(back.ready)
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
