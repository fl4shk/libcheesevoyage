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

object PipeMemRmwSimDut {
  def wordWidth = 8
  def wordType() = UInt(wordWidth bits)
  def wordCount = (
    //4
    8
  )
  def hazardCmpType() = UInt(
    PipeMemRmw.addrWidth(wordCount=wordCount) bits
  )
  def modStageCnt = (
    //2
    1
  )
  def modType() = SamplePipeMemRmwModType(
    wordType=wordType(),
    wordCount=wordCount,
    hazardCmpType=hazardCmpType(),
    modStageCnt=modStageCnt,
  )
  //def forFmax = (
  //  //true
  //  false
  //)
}
case class PipeMemRmwSimDutIo() extends Bundle {
  val front = slave(Stream(PipeMemRmwSimDut.modType()))
  val modFront = master(Stream(PipeMemRmwSimDut.modType()))
  val modBack = slave(Stream(PipeMemRmwSimDut.modType()))
  val back = master(Stream(PipeMemRmwSimDut.modType()))
  val midModStages = in(Vec.fill(PipeMemRmwSimDut.modStageCnt)(
    PipeMemRmwSimDut.modType())
  )
}
case class PipeMemRmwSimDut(
) extends Component {
  def wordWidth = PipeMemRmwSimDut.wordWidth
  def wordType() = PipeMemRmwSimDut.wordType()
  def wordCount = PipeMemRmwSimDut.wordCount
  def hazardCmpType() = PipeMemRmwSimDut.hazardCmpType()
  def modStageCnt = (
    //2
    1
  )
  def modType() = PipeMemRmwSimDut.modType()
  val io = PipeMemRmwSimDutIo()
  val pipeMem = PipeMemRmw[
    UInt,
    UInt,
    SamplePipeMemRmwModType[UInt, UInt],
    PipeMemRmwDualRdTypeDisabled[UInt, UInt],
  ](
    wordType=wordType(),
    wordCount=wordCount,
    hazardCmpType=hazardCmpType(),
    modType=modType(),
    modStageCnt=modStageCnt,
    pipeName="PipeMemRmw_FormalDut",
    dualRdType=(
      //modType()
      PipeMemRmwDualRdTypeDisabled[UInt, UInt](),
    ),
    optDualRd=(
      //true
      false
    ),
    init=None,
    initBigInt=Some(Array.fill(wordCount)(BigInt(0)).toSeq),
    optEnableModDuplicate=true,
    //forFmax=forFmax,
  )(
    doHazardCmpFunc=None,
    doPrevHazardCmpFunc=false,
  )
  pipeMem.io.midModStages := io.midModStages
  pipeMem.io.front.driveFrom(io.front)(
    con=(node, payload) => {
      node(pipeMem.io.frontPayload) := payload
    }
  )
  pipeMem.io.modFront.driveTo(io.modFront)(
    con=(payload, node) => {
      payload := node(pipeMem.io.modFrontPayload)
    }
  )
  pipeMem.io.modBack.driveFrom(io.modBack)(
    con=(node, payload) => {
      node(pipeMem.io.modBackPayload) := payload
    }
  )
  pipeMem.io.back.driveTo(io.back)(
    con=(payload, node) => {
      payload := node(pipeMem.io.backPayload)
    }
  )
  Builder(pipeMem.myLinkArr)
}
case class PipeMemRmwTester() extends Component {
  def wordWidth = PipeMemRmwSimDut.wordWidth
  def wordType() = PipeMemRmwSimDut.wordType()
  def wordCount = PipeMemRmwSimDut.wordCount
  def hazardCmpType() = PipeMemRmwSimDut.hazardCmpType()
  def modStageCnt = (
    //2
    1
  )
  def modType() = PipeMemRmwSimDut.modType()
  val io = new Bundle {
    //val front = slave(Stream(modType()))
    val back = master(Stream(modType()))
  }
  val dut = PipeMemRmwSimDut()
  //dut.io.front << io.front
  io.back << dut.io.back
  def front = dut.io.front
  //def back = dut.io.back

  def memAddrFracWidth = 0
  val rMemAddr = Reg(
    UInt((front.myExt.memAddr.getWidth + memAddrFracWidth) bits)
  ) init(0x0)
  def memAddrFracRange = memAddrFracWidth - 1 downto 0
  def memAddrIntRange = rMemAddr.high downto memAddrFracWidth

  front.valid := True
  front.payload := (
    RegNext(front.payload) init(front.payload.getZero)
  )
  front.myExt.allowOverride
  front.myExt.memAddr := rMemAddr >> memAddrFracWidth
  when (front.fire) {
    rMemAddr := rMemAddr  + 1
    //rMemAddr(0 downto 0) := rMemAddr(0 downto 0) + 1
    //when (rMemAddr(memAddrFracRange) === 2) {
    //  rMemAddr(memAddrIntRange) := (
    //    rMemAddr(memAddrIntRange) + 1
    //  )
    //  rMemAddr(memAddrFracRange) := 0
    //}
  }
  //back.ready := True
  //--------

  val modFrontStm = Stream(modType())
  val modMidStm = Stream(modType())
  val modBackStm = Stream(modType())
  modFrontStm <-/< dut.io.modFront
  modFrontStm.translateInto(
    into=modMidStm
  )(
    dataAssignment=(
      modMidPayload,
      modFrontPayload,
    ) => {
      //modMidPayload.myExt := modFrontPayload.myExt
      modMidPayload := modFrontPayload
      modMidPayload.myExt.allowOverride
      when (modMidPayload.myExt.hazardId.msb) {
        modMidPayload.myExt.modMemWord := (
          //modFrontPayload.myExt.rdMemWord + 0x1
          modFrontPayload.myExt.rdMemWord + 0x1
        )
      }
      //when (
      //  modFrontPayload.myExt.hazardId.msb
      //) {
      //  modMidPayload.myExt.dbgModMemWord := (
      //    modMidPayload.myExt.modMemWord
      //  )
      //} otherwise {
      //  modMidPayload.myExt.dbgModMemWord := 0x0
      //}
      dut.io.midModStages(0) := modMidPayload
    }
  )
  modBackStm <-/< modMidStm
  //modBackStm << modMidStm
  dut.io.modBack << modBackStm
}
object PipeMemRmwSim extends App {
  def clkRate = 25.0 MHz
  val simSpinalConfig = SpinalConfig(
    defaultClockDomainFrequency=FixedFrequency(clkRate)
  )
  SimConfig
    .withConfig(config=simSpinalConfig)
    .withFstWave
    .compile(
      PipeMemRmwTester()
    )
    .doSim/*("PipeMemRmwSim")*/({ dut =>
      //dut.io
      def simNumClks = (
        //1000
        //32
        100
      )
      ////SimTimeout(32)
      ////val scoreboard = ScoreboardInOrder[Int]()
      ////StreamDriver(dut.io.front, dut.clockDomain) { payload =>
      ////  payload.randomize()
      ////  true
      ////}
      //dut.io.front.valid #= true
      ////dut.io.front.payload.randomize()

      ////StreamMonitor(dut.io.front, dut.clockDomain) { payload => 
      ////  scoreboard.pushRef(payload.toInt)
      ////}
      //StreamReadyRandomizer(dut.io.back, dut.clockDomain)
      dut.io.back.ready #= true
      //dut.io.back.ready #= true
      //dut.clockDomain.forkStimulus(period=10)
      ////dut.clockDomain.waitActiveEdgeWhere(scoreboard.matches == 100)

      //def tempAddrFracWidth = 2
      //val tempAddr = 0 << tempAddrFracWidth
      dut.clockDomain.forkStimulus(period=10)

      for (idx <- 0 until simNumClks) {
        //dut.io.front.myExt.memAddr #= 
        dut.clockDomain.waitRisingEdge()
      }
      simSuccess()
    })
}
