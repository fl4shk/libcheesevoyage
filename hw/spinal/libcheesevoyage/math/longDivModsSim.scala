//package libcheesevoyage.math
//import libcheesevoyage.general._
//
//import scala.math._
//import spinal.core._
//import spinal.core.sim._
//import spinal.lib._
//import spinal.core.formal._
//import scala.collection.mutable.ArrayBuffer
//
//case class LongDivPipelinedTester(
//  mainWidth: Int,
//  denomWidth: Int,
//  chunkWidth: Int,
//  signedReset: BigInt=0x0,
//) extends Component {
//  val dut = LongDivPipelined(
//    mainWidth=mainWidth,
//    denomWidth=denomWidth,
//    chunkWidth=chunkWidth,
//    signedReset=signedReset,
//  )
//}
//object LongDivPipelinedSim extends App {
//  def clkRate = 25 MHz
//  def mainWidth = 4
//  def denomWidth = 4
//  def chunkWidth = 2
//  val simSpinalConfig = SpinalConfig(
//    //defaultClockDomainFrequency=FixedFrequency(100 MHz)
//    defaultClockDomainFrequency=FixedFrequency(clkRate)
//  )
//  SimConfig
//    .withConfig(config=simSpinalConfig)
//    .withFstWave
//    .compile(
//      LongDivPipelinedTester(
//        mainWidth=mainWidth,
//        denomWidth=denomWidth,
//        chunkWidth=chunkWidth,
//      )
//    )
//    .doSim { dut =>
//      dut.clockDomain.forkStimulus(period=10)
//      def simNumClks = (
//        100
//      )
//      for (idx <- 0 until simNumClks) {
//        dut.clockDomain.waitRisingEdge()
//      }
//      simSuccess()
//    }
//}
