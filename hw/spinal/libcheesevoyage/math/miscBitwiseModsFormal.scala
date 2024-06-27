package libcheesevoyage.math
import libcheesevoyage.general._

import spinal.core._
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer

case class BarrelShifterFormalDut(
  wordWidth: Int,
  kind: Int,
) extends Component {
  val io = BarrelShifterIo(
    wordWidth=wordWidth,
  )
  val dut = BarrelShifter(
    wordWidth=wordWidth,
    kind=kind,
  )
  val rOutpData = (
    Reg(UInt(wordWidth bits)) init(0x0)
  )
  io.outpData := rOutpData

  dut.io.inpData := io.inpData
  dut.io.inpAmount := io.inpAmount
  rOutpData := dut.io.outpData

  //GenerationFlags.formal {
    when (pastValidAfterReset) {
      when (
        past(io.inpAmount) > 0
        && past(io.inpAmount) < log2Up(wordWidth)
      ) {
        if (kind == BarrelShifter.kindLsl) {
          cover(rOutpData === 0x4 && past(dut.io.inpAmount) =/= 0)
          //println("testificate")
          assert(
            rOutpData
            === (
              (
                past(dut.io.inpData) << past(dut.io.inpAmount)
              )(wordWidth - 1 downto 0)
            )
          )
        } else if (kind == BarrelShifter.kindLsr) {
          assert(
            rOutpData
            === (
              (
                past(dut.io.inpData) >> past(dut.io.inpAmount)
              )(wordWidth - 1 downto 0)
            )
          )
        } else if (kind == BarrelShifter.kindAsr) {
          assert(
            rOutpData.asSInt
            === (
              (
                past(dut.io.inpData).asSInt >> past(dut.io.inpAmount)
              )(wordWidth - 1 downto 0)
            )
          )
        }
      }
    }
  //}
}
object BarrelShifterFormal extends App {
  def wordWidth = 8
  def kind = (
    //BarrelShifter.kindLsl
    //BarrelShifter.kindLsr
    BarrelShifter.kindAsr
  )
  new SpinalFormalConfig(_keepDebugInfo=true)
  //FormalConfig
    .withBMC(10)
    .withProve(10)
    .withCover(10)
    .withConfig(
      config=SpinalConfig(
        defaultConfigForClockDomains=ClockDomainConfig(
          resetActiveLevel = HIGH,
          resetKind = SYNC,
        ),
        formalAsserts=true,
      )
    )
    .doVerify(new Component {
      val dut = FormalDut(BarrelShifterFormalDut(
        //params=params
        wordWidth=wordWidth,
        kind=kind,
      ))
      //val itdIn = dut.io.itdIn
      //val chunkStart = dut.io.chunkStart
      //val itdOut = dut.io.itdOut
      //val inp = dut.io.inp
      //val outp = dut.io.outp

      assumeInitial(clockDomain.isResetActive)
      //assumeInitial(~pastValid)
      //assumeInitial(inp === inp.getZero)
      assumeInitial(dut.io.inpData === dut.io.inpData.getZero)
      assumeInitial(dut.io.inpAmount === dut.io.inpAmount.getZero)
      anyseq(dut.io.inpData)
      anyseq(dut.io.inpAmount)
      //when (clockDomain.isResetActive) {
      //  pastValid := False
      //} otherwise {
      //  pastValid := True
      //}
      //anyseq(clockDomain.isResetActive)
    })
}
