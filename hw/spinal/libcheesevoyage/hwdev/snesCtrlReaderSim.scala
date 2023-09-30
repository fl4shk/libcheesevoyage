package libcheesevoyage.hwdev

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer
import scala.math._

object SnesCtrlReaderSim extends App {
  ////def clkPeriodUs = 12.0 / 8.0
  //def clkPeriodUs = 
  //def clkPeriod = clkPeriodUs us
  ////def clkRate = (1.0 / (clkPeriodUs * 1e-6)) Hz
  //def clkRate = clkPeriod.toHertz
  val unitTime = 12.0 us
  val unitClkRateDouble = unitTime.toHertz.toDouble
  val clkRateHz: BigDecimal = 4 / (12.0e-6)
  val clkRate = HertzNumber(clkRateHz)
  println(clkRate.toDouble)
  //println(clkRate.value)
  case class Dut() extends Component {
    val io = SnesCtrlIo()
    val scr = SnesCtrlReader(
      clkRate=clkRate,
      vivadoDebug=false,
    )
    io <> scr.io.snesCtrl
    scr.io.pop.ready := True
  }
  val simSpinalConfig = SpinalConfig(
    defaultClockDomainFrequency=FixedFrequency(100 MHz)
    //defaultClockDomainFrequency=FixedFrequency(clkRate)
  )
  SimConfig
    .withConfig(config=simSpinalConfig)
    .withVcdWave
    .compile(Dut())
    .doSim { dut =>
      dut.clockDomain.forkStimulus(period=10)
      //SimTimeout(1000)
      //for (idx <- 0 to 4000) {
      //  //sleep(1)
      //  dut.clockDomain.waitRisingEdge()
      //}
      for (idx <- 0 to 8000 - 1) {
        dut.clockDomain.waitRisingEdge()
        //when (dut.io.misc.visib) {
        //  foundVisib := True
        //}
        //when (foundVisib) {
        //  
        //}
      }
      simSuccess()
    }
}
