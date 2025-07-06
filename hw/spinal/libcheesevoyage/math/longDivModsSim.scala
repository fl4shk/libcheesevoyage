package libcheesevoyage.math
import libcheesevoyage.general._

import scala.math._
import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer

case class LongDivMultiCycleTester(
  mainWidth: Int,
  denomWidth: Int,
  chunkWidth: Int,
  signedReset: BigInt=0x0,
) extends Component {
  val dut = LongDivMultiCycle(
    mainWidth=mainWidth,
    denomWidth=denomWidth,
    chunkWidth=chunkWidth,
    signedReset=signedReset,
  )
  val rState = Reg(Bool(), init=False)

  val nextNumerDenom = UInt((mainWidth + denomWidth) bits)
  val rNumerDenom = (
    RegNext(
      next=nextNumerDenom,
      init=nextNumerDenom.getZero,
    )
  )
  nextNumerDenom := rNumerDenom
  val rSavedQuot = (
    Reg(UInt(mainWidth bits))
    init(0x0)
  )
  val rSavedRema = (
    Reg(UInt(mainWidth bits))
    init(0x0)
  )
  val rOracleQuot = (
    Reg(UInt(mainWidth bits))
    init(0x0)
  )
  val rOracleRema = (
    Reg(UInt(mainWidth bits))
    init(0x0)
  )
  //val rHaveAnyResult = Reg(Bool(), init=False)
  //when (rHaveAnyResult) {
  //}
  val rHaveCorrectQuot = {
    val temp = Reg(UInt(2 bits))
    temp.init(temp.getZero)
    temp
  }
  val rHaveCorrectRema = {
    val temp = Reg(UInt(2 bits))
    temp.init(temp.getZero)
    temp
  }

  dut.io.inp.valid := False
  dut.io.inp.numer := rNumerDenom(nextNumerDenom.high downto denomWidth)
  dut.io.inp.denom := rNumerDenom(denomWidth - 1 downto 0)
  //dut.io.inp.numer := 0xc // 12
  //dut.io.inp.denom := 0x4 // 4
  dut.io.inp.signed := False

  when (!rState) {
    rState := True
    dut.io.inp.valid := True
    rOracleQuot := (dut.io.inp.numer / dut.io.inp.denom).resized
    rOracleRema := (dut.io.inp.numer % dut.io.inp.denom).resized
  } otherwise {
    when (dut.io.outp.ready) {
      nextNumerDenom := rNumerDenom + 1
      rState := False
      rSavedQuot := dut.io.outp.quot
      rSavedRema := dut.io.outp.rema

      rHaveCorrectQuot := 0x2
      rHaveCorrectRema := 0x2
      when (dut.io.inp.denom =/= 0x0) {
        when (dut.io.outp.quot === rOracleQuot) {
          rHaveCorrectQuot := 0x1
        } otherwise {
          rHaveCorrectQuot := 0x0
        }
        when (dut.io.outp.rema === rOracleRema) {
          rHaveCorrectRema := 0x1
        } otherwise {
          rHaveCorrectRema := 0x0
        }
      }
      //rHaveAnyResult := True
    }
  }
}
object LongDivMultiCycleSim extends App {
  //def clkRate = 25 MHz
  def mainWidth = 4
  def denomWidth = 4
  def chunkWidth = 1
  //val simSpinalConfig = SpinalConfig(
  //  //defaultClockDomainFrequency=FixedFrequency(100 MHz)
  //  defaultClockDomainFrequency=FixedFrequency(clkRate)
  //)
  SimConfig
    //.withConfig(config=simSpinalConfig)
    .withFstWave
    .compile(
      LongDivMultiCycleTester(
        mainWidth=mainWidth,
        denomWidth=denomWidth,
        chunkWidth=chunkWidth,
      )
      //LongDivMultiCycle(
      //  mainWidth=mainWidth,
      //  denomWidth=denomWidth,
      //  chunkWidth=chunkWidth,
      //  //signedReset=signedReset,
      //)
    )
    .doSim(dut => {
      dut.clockDomain.forkStimulus(period=10)
      //def simNumClks = (
      //  //1024
      //  (1 << 8) * (1 << 8) * 2 * 2 * 2
      //)
      //for (idx <- 0 until simNumClks) {
      //  dut.clockDomain.waitRisingEdge()
      //}
      //simSuccess()
      val simAmount = (
        (1 << mainWidth) * (1 << denomWidth) * 2
      )
      for (i <- 0 until simAmount) {
        dut.clockDomain.waitSampling()
        //for (gprIdx <- 0 until cfg.numGprs) {
        //  printf(
        //    "r%i=%x ",
        //    gprIdx,
        //    dut.cpu.regFile.modMem(0)(0).readAsync(
        //      address=gprIdx
        //    ).toInt
        //  )
        //  if (gprIdx % 4 == 3) {
        //    printf("\n")
        //  }
        //}
      }
    })
}
