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
    //0
  )
  def modRdPortCnt = (
    1
    //2
  )
  def modType() = SamplePipeMemRmwModType(
    wordType=wordType(),
    wordCount=wordCount,
    hazardCmpType=hazardCmpType(),
    modRdPortCnt=modRdPortCnt,
    modStageCnt=modStageCnt,
    optModHazardKind=optModHazardKind,
    //doModInModFront=(
    //  true
    //  //false
    //),
  )
  def optModHazardKind = (
    PipeMemRmw.modHazardKindDupl
    //PipeMemRmw.modHazardKindFwd
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
  val midModStages = (PipeMemRmwSimDut.modStageCnt > 0) generate (
    in(Vec.fill(PipeMemRmwSimDut.modStageCnt)(
      PipeMemRmwSimDut.modType())
    )
  )
}
//case class PipeMemRmwDoModFuncSimDut(
//) extends Component {
//  def wordWidth = 
//}

case class PipeMemRmwSimDut(
) extends Component {
  def wordWidth = PipeMemRmwSimDut.wordWidth
  def wordType() = PipeMemRmwSimDut.wordType()
  def wordCount = PipeMemRmwSimDut.wordCount
  def hazardCmpType() = PipeMemRmwSimDut.hazardCmpType()
  def modRdPortCnt = (
    PipeMemRmwSimDut.modRdPortCnt
  )
  def modStageCnt = (
    //2
    //1
    //0
    PipeMemRmwSimDut.modStageCnt
  )
  def optModHazardKind = (
    PipeMemRmwSimDut.optModHazardKind
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
    modRdPortCnt=modRdPortCnt,
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
    initBigInt={
      val tempArr = new ArrayBuffer[BigInt]()
      for (idx <- 0 until wordCount) {
        val toAdd: Int = idx * 2
        //println(s"${toAdd} ${idx} ${wordCount}")
        //tempArr += idx * 2
        tempArr += toAdd
        //println(tempArr.last)
      }
      //Some(Array.fill(wordCount)(BigInt(0)).toSeq)
      Some(tempArr.toSeq)
    },
    //optEnableModDuplicate=(
    //  true,
    //  //false,
    //),
    optModHazardKind=(
      optModHazardKind
    ),
    //forFmax=forFmax,
  )(
    doHazardCmpFunc=None,
    doPrevHazardCmpFunc=false,
    doModInModFrontFunc=(
      None
      //Some(
      //  (
      //    outp,
      //    inp,
      //    cMid0Front,
      //  ) => {
      //    //outp.myExt := RegNext(outp.myExt) init(outp.myExt.getZero)
      //    outp.myExt.allowOverride
      //    //when (cMid0Front.up.isFiring) {
      //      outp.myExt := inp.myExt
      //      when (
      //        if (optModHazardKind == PipeMemRmw.modHazardKindDupl) (
      //          outp.myExt.hazardId.msb
      //        ) else (
      //          True
      //        )
      //      ) {
      //        outp.myExt.modMemWord := (
      //          //modFrontPayload.myExt.rdMemWord(0) + 0x1
      //          inp.myExt.rdMemWord(0) + 0x1
      //          //inp.myExt.rdMemWord(0) + inp.myExt.rdMemWord(1) + 0x1
      //        )
      //      }
      //    //}
      //  }
      //)
    ),
    doFwdFunc=Some(
      (
        stageIdx,
        myUpExtDel2,
        zdx,
      ) => {
        //myUpExtDel.last.modMemWord
        //myUpExtDel(
        //  Mux[UInt](stageIdx === 0, 1, stageIdx)
        //).modMemWord
        myUpExtDel2(stageIdx).modMemWord
      }
    ),
  )
  if (modStageCnt > 0) {
    pipeMem.io.midModStages := io.midModStages
  }
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
  def modRdPortCnt = (
    PipeMemRmwSimDut.modRdPortCnt
  )
  def modStageCnt = (
    //2
    //1
    //0
    PipeMemRmwSimDut.modStageCnt
  )
  def modType() = PipeMemRmwSimDut.modType()
  val io = new Bundle {
    //val front = slave(Stream(modType()))
    val back = master(Stream(modType()))
  }
  val dut: PipeMemRmwSimDut = PipeMemRmwSimDut()
  //dut.io.front << io.front
  io.back << dut.io.back
  def front = dut.io.front
  //def back = dut.io.back

  def memAddrFracWidth = (
    0
    //1
  )
  val rMemAddr = Reg(
    UInt((front.myExt.memAddr(0).getWidth + memAddrFracWidth) bits)
  ) init(0x0)
  def memAddrFracRange = memAddrFracWidth - 1 downto 0
  def memAddrIntRange = rMemAddr.high downto memAddrFracWidth

  //front.valid := True
  val nextFrontValid = Bool()
  val rFrontValid = RegNext(nextFrontValid) init(nextFrontValid.getZero)

  nextFrontValid := !rFrontValid
  //nextFrontValid := True
  front.valid := nextFrontValid

  front.payload := (
    RegNext(front.payload) init(front.payload.getZero)
  )
  front.myExt.allowOverride
  for (zdx <- 0 until modRdPortCnt) {
    front.myExt.memAddr(zdx) := (rMemAddr + zdx) >> memAddrFracWidth
  }
  when (front.fire) {
    rMemAddr := rMemAddr + 1
    //rMemAddr(0 downto 0) := rMemAddr(0 downto 0) + 1
    //rMemAddr(1 downto 0) := rMemAddr(1 downto 0) + 1
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
  //modFrontStm <-/< dut.io.modFront
  modFrontStm << dut.io.modFront
  //modMidStm << modFrontStm
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
          modFrontPayload.myExt.rdMemWord(PipeMemRmw.modWrIdx) + 0x1
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
      if (modStageCnt > 0) {
        dut.io.midModStages(0) := modMidPayload
      }
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
    //.withFstWave
    .withVcdWave
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
      StreamReadyRandomizer(dut.io.back, dut.clockDomain)
      //dut.io.back.ready #= true
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
