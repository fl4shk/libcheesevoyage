package libcheesevoyage.math

import spinal.core._
import spinal.core.formal._

object LongDivMultiCycleFormal extends App {
  //val params = LongDivParams(
  //  mainWidth=4,
  //  denomWidth=4,
  //  chunkWidth=2,
  //  tagWidth=8,
  //  pipelined=false,
  //  usePipeSkidBuf=false,
  //)
  val mainWidth = 8
  val denomWidth = 8
  val chunkWidth = 1
  case class MyFormalDutLongDivMultiCycle(
  ) extends Component {
    val dut = FormalDut(LongDivMultiCycle(
      //params=params
      mainWidth=mainWidth,
      denomWidth=denomWidth,
      chunkWidth=chunkWidth,
      formal=true,
    ))
    //val itdIn = dut.io.itdIn
    //val chunkStart = dut.io.chunkStart
    //val itdOut = dut.io.itdOut
    def inp = dut.io.inp
    def outp = dut.io.outp
    //assume(inp.denom =/= 0)
    //assume(inp.denom =/= 1)
    //assume(inp.signed =/= True)
    //cover(
    //  (
    //    RegNextWhen(
    //      (
    //        inp.valid && inp.numer > 1 && inp.denom > 1
    //        //&& !outp.ready
    //      ),
    //      RegNextWhen(
    //        True,
    //        inp.valid && inp.numer > 1 && inp.denom > 1
    //        && !outp.ready
    //      ) init(False)
    //    ) init(False)
    //  ) 
    //  //&& (
    //  //  RegNextWhen(True, inp.denom > 1) init(False)
    //  //)
    //  && (
    //    outp.ready
    //  ) && (
    //    outp.quot =/= 0x0
    //    && outp.quot =/= 0xc
    //  )
    //)

    assumeInitial(clockDomain.isResetActive)
    //assumeInitial(~pastValid)
    //assumeInitial(inp === inp.getZero)
    //assumeInitial(inp.valid === inp.valid.getZero)

    anyseq(inp)
    //cover(inp.valid === True)
    //when (!clockDomain.isResetActive) {
    //  assume(inp.valid === True)
    //  assume(inp.numer === 8)
    //  assume(inp.denom === 4)
    //  assume(inp.signed === False)
    //}

    //when (clockDomain.isResetActive) {
    //  pastValid := False
    //} otherwise {
    //  pastValid := True
    //}
    //anyseq(clockDomain.isResetActive)
  }
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
  //FormalConfig
    .withBMC(10)
    //.withProve(10)
    .withCover(10)
    //.withConfig(config=SpinalConfig(
    //  defaultConfigForClockDomains=ClockDomainConfig(
    //    resetActiveLevel = HIGH,
    //    resetKind = SYNC,
    //  ),
    //  formalAsserts=true,
    //))
    .doVerify(MyFormalDutLongDivMultiCycle())
}
//object LongDivPipelinedFormal extends App {
//  //val params = LongDivParams(
//  //  mainWidth=4,
//  //  denomWidth=4,
//  //  chunkWidth=2,
//  //  tagWidth=8,
//  //  pipelined=false,
//  //  usePipeSkidBuf=false,
//  //)
//  val mainWidth = 4
//  val denomWidth = 4
//  val chunkWidth = 1
//  val usePipeSkidBuf = (
//    //true
//    false
//  )
//  new SpinalFormalConfig(
//    _spinalConfig=SpinalConfig(
//      defaultConfigForClockDomains=ClockDomainConfig(
//        resetActiveLevel=HIGH,
//        resetKind=SYNC,
//      ),
//      formalAsserts=true,
//    ),
//    _keepDebugInfo=true,
//  )
//  //FormalConfig
//    .withBMC(10)
//    .withProve(10)
//    .withCover(20)
//    //.withConfig(config=SpinalConfig(
//    //  defaultConfigForClockDomains=ClockDomainConfig(
//    //    resetActiveLevel = HIGH,
//    //    resetKind = SYNC,
//    //  ),
//    //  formalAsserts=true,
//    //))
//    .doVerify(new Component {
//      val dut = FormalDut(LongDivPipelined(
//        //params=params
//        mainWidth=mainWidth,
//        denomWidth=denomWidth,
//        chunkWidth=chunkWidth,
//        usePipeSkidBuf=usePipeSkidBuf,
//      ))
//      //val itdIn = dut.io.itdIn
//      //val chunkStart = dut.io.chunkStart
//      //val itdOut = dut.io.itdOut
//      val inp = dut.io.inp
//      val outp = dut.io.outp
//
//      assumeInitial(clockDomain.isResetActive)
//      //assumeInitial(~pastValid)
//      assumeInitial(inp === inp.getZero)
//      anyseq(inp)
//      cover(outp.quot =/= 0x0)
//      //when (clockDomain.isResetActive) {
//      //  pastValid := False
//      //} otherwise {
//      //  pastValid := True
//      //}
//      //anyseq(clockDomain.isResetActive)
//    })
//}
