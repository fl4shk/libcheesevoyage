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
  val mainWidth = 4
  val denomWidth = 4
  val chunkWidth = 2
  new SpinalFormalConfig(_keepDebugInfo=true)
  //FormalConfig
    .withBMC(10)
    .withProve(10)
    .doVerify(new Component {
      val dut = FormalDut(LongDivMultiCycle(
        //params=params
        mainWidth=mainWidth,
        denomWidth=denomWidth,
        chunkWidth=chunkWidth,
      ))
      //val itdIn = dut.io.itdIn
      //val chunkStart = dut.io.chunkStart
      //val itdOut = dut.io.itdOut
      val inp = dut.io.inp
      val outp = dut.io.outp

      assumeInitial(clockDomain.isResetActive)
      //assumeInitial(~pastValid)
      assumeInitial(inp === inp.getZero)
      anyseq(inp)
      //when (clockDomain.isResetActive) {
      //  pastValid := False
      //} otherwise {
      //  pastValid := True
      //}
      //anyseq(clockDomain.isResetActive)
    })
}
object LongDivPipelinedFormal extends App {
  //val params = LongDivParams(
  //  mainWidth=4,
  //  denomWidth=4,
  //  chunkWidth=2,
  //  tagWidth=8,
  //  pipelined=false,
  //  usePipeSkidBuf=false,
  //)
  val mainWidth = 4
  val denomWidth = 4
  val chunkWidth = 1
  val usePipeSkidBuf = true
  new SpinalFormalConfig(_keepDebugInfo=true)
  //FormalConfig
    .withBMC(10)
    .withProve(10)
    .doVerify(new Component {
      val dut = FormalDut(LongDivPipelined(
        //params=params
        mainWidth=mainWidth,
        denomWidth=denomWidth,
        chunkWidth=chunkWidth,
        usePipeSkidBuf=usePipeSkidBuf,
      ))
      //val itdIn = dut.io.itdIn
      //val chunkStart = dut.io.chunkStart
      //val itdOut = dut.io.itdOut
      val inp = dut.io.inp
      val outp = dut.io.outp

      assumeInitial(clockDomain.isResetActive)
      //assumeInitial(~pastValid)
      assumeInitial(inp === inp.getZero)
      anyseq(inp)
      //when (clockDomain.isResetActive) {
      //  pastValid := False
      //} otherwise {
      //  pastValid := True
      //}
      //anyseq(clockDomain.isResetActive)
    })
}
