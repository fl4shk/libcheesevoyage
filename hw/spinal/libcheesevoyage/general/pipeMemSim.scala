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
import spinal.core.formal._

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
    if (optModHazardKind != PipeMemRmw.modHazardKindFwd) (
      //2
      1
    ) else (
      //0
      1
    )
  )
  def modRdPortCnt = (
    1
    //2
  )
  //def formalHaltItCnt = (
  //  1
  //)
  //def formalHaltItCnt = (
  //)
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
    //PipeMemRmw.modHazardKindDupl
    PipeMemRmw.modHazardKindFwd
  )
  //def optModFwdToFront = (
  //  //optModHazardKind == PipeMemRmw.modHazardKindFwd
  //  //true
  //  false
  //)
  def doRandFront = (
    true
    //false
  )
  def memAddrPlusAmount = (
      //0x8
      //0x4
      //0x3
      0x2
      //0x1
  )
}
//case class PipeMemRmwSimDutIo() extends Area {
//  //val front = slave(Stream(PipeMemRmwSimDut.modType()))
//  //val modFront = master(Stream(PipeMemRmwSimDut.modType()))
//  //val modBack = slave(Stream(PipeMemRmwSimDut.modType()))
//  //val back = master(Stream(PipeMemRmwSimDut.modType()))
//  val front = Node()
//  val modFront = Node()
//  val modBack = Node()
//  val back = Node()
//
//  val midModStages = (PipeMemRmwSimDut.modStageCnt > 0) generate (
//    /*in*/(Vec.fill(PipeMemRmwSimDut.modStageCnt)(
//      PipeMemRmwSimDut.modType())
//    )
//  )
//}
//case class PipeMemRmwDoModFuncSimDut(
//) extends Component {
//  def wordWidth = 
//}

case class PipeMemRmwSimDut(
) extends Area {
  def doFormal: Boolean = {
    GenerationFlags.formal {
      return true
    }
    return false
  }
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
  //def optModFwdToFront = (
  //  PipeMemRmwSimDut.optModFwdToFront
  //)
  def modType() = PipeMemRmwSimDut.modType()
  //val io = PipeMemRmwSimDutIo()
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
        //val toAdd: Int = idx * 2
        val toAdd: Int = 0x0
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
    //optModFwdToFront=(
    //  optModFwdToFront
    //),
    //forFmax=forFmax,
  )(
    doHazardCmpFunc=None,
    doPrevHazardCmpFunc=false,
    doModInModFrontFunc=(
      if (optModHazardKind != PipeMemRmw.modHazardKindFwd) (
        None
      ) else (
        Some(
          (
            outp,
            inp,
            cMid0Front,
          ) => {
            //outp.myExt := RegNext(outp.myExt) init(outp.myExt.getZero)
            outp.myExt := inp.myExt
            outp.myExt.allowOverride
            when (
              //cMid0Front.up.isFiring
              //cMid0Front.up.isValid
              //&& cMid0Front.down.isReady
              True
              //cMid0Front.down.isFiring
            ) {
              //outp.myExt := inp.myExt
              when (
                if (optModHazardKind == PipeMemRmw.modHazardKindDupl) (
                  outp.myExt.hazardId.msb
                ) else (
                  True
                )
              ) {
                outp.myExt.modMemWord := (
                  //modFrontPayload.myExt.rdMemWord(0) + 0x1
                  inp.myExt.rdMemWord(0) + 0x1
                  //inp.myExt.rdMemWord(0) + inp.myExt.rdMemWord(1) + 0x1
                )
              }
            }
          }
        )
      )
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
  //GenerationFlags.formal 
  val rSavedModArr = (
    Vec.fill(
      PipeMemRmwSimDut.wordCount
    )(
      Reg(PipeMemRmwSimDut.wordType())
    )
  )
  when (!pastValidAfterReset) {
    for (idx <- 0 until rSavedModArr.size) {
      rSavedModArr(idx).init(
        //idx * 2
        //dut.dut.pipeMem.modMem(PipeMemRmw.modWrIdx).readAsync(
        //  address=idx
        //)
        0x0
      )
      rSavedModArr(idx) := (
        pipeMem.modMem(PipeMemRmw.modWrIdx).readAsync(
          address=U{
            val width = log2Up(wordCount)
            s"${width}'d${idx}"
          }
        )
      )
    }
  }
  def front = pipeMem.io.front
  def frontPayload = pipeMem.io.frontPayload
  def modFront = pipeMem.io.modFront
  def modFrontPayload = pipeMem.io.modFrontPayload
  def modBack = pipeMem.io.modBack
  def modBackPayload = pipeMem.io.modBackPayload
  def back = pipeMem.io.back
  def backPayload = pipeMem.io.backPayload

  //val tempMemAddrPlus = (
  //  (
  //    (
  //      RegNextWhen(
  //        modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
  //        modBack.isFiring,
  //      ) init(0x0)
  //    ) + 1 
  //  ) % PipeMemRmwSimDut.memAddrPlusAmount
  //)
  val tempLeft = (
    //RegNextWhen(
      modBack(modBackPayload).myExt.modMemWord,
    //  modBack.isFiring,
    //) init(0x0)
  )
  val tempRight = rSavedModArr(
    //modBack.myExt.memAddr(PipeMemRmw.modWrIdx)
    //RegNextWhen(
      modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx),
    //  modBack.isFiring,
    //) init(0x0)
  ) + 1

  when (pastValidAfterReset) {
    when (
      modBack.isFiring
      //&& (
      //  RegNextWhen(True, back.isFiring) init(False)
      //)
    ) {
      def mySavedMod = (
        rSavedModArr(
          modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx)
        )
      )
      // := (
      //  //modBack.myExt.rdMemWord(PipeMemRmw.modWrIdx)
      //)
      mySavedMod := mySavedMod + 1
      //when (
      //  RegNextWhen(True, back.isFiring) init(False)
      //) {
      //  assert(
      //    (
      //      modBack(modBackPayload).myExt.memAddr(PipeMemRmw.modWrIdx)
      //    ) === (
      //      tempMemAddrPlus
      //    )
      //  )
      //}
      when (
        //(
        //  RegNextWhen(True, modBack.isFiring) init(False)
        //) && (
          RegNextWhen(True, back.isFiring) init(False)
        //)
      ) {
        assert(
          tempLeft === tempRight
        )
      }
    }
    cover(
      back.isFiring
      && (
        back(backPayload).myExt.modMemWord === 0x3
      )
      && (
        RegNextWhen(
          (
            RegNextWhen(
              True,
              back.isFiring,
            ) init(False)
          ),
          back.isFiring,
        ) init(False)
      )
    )
  }
  //if (modStageCnt > 0) {
  //  pipeMem.io.midModStages := io.midModStages
  //}
  //pipeMem.io.front.driveFrom(io.front)(
  //  con=(node, payload) => {
  //    node(pipeMem.io.frontPayload) := payload
  //  }
  //)
  //pipeMem.io.modFront.driveTo(io.modFront)(
  //  con=(payload, node) => {
  //    payload := node(pipeMem.io.modFrontPayload)
  //  }
  //)
  //pipeMem.io.modBack.driveFrom(io.modBack)(
  //  con=(node, payload) => {
  //    node(pipeMem.io.modBackPayload) := payload
  //  }
  //)
  //pipeMem.io.back.driveTo(io.back)(
  //  con=(payload, node) => {
  //    payload := node(pipeMem.io.backPayload)
  //  }
  //)
  //Builder(pipeMem.myLinkArr)
}
case class PipeMemRmwTester() extends Component {
  def doFormal: Boolean = {
    GenerationFlags.formal {
      return true
    }
    return false
  }
  //assert(doFormal)
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
  val io = new Bundle {
    //val front = (
    //  PipeMemRmwSimDut.doRandFront
    //  || doFormal
    //) generate (
    //  slave(Stream(modType()))
    //)
    val front = slave(Stream(modType()))
    val back = master(Stream(modType()))
  }
  val dut: PipeMemRmwSimDut = PipeMemRmwSimDut()
  def pipeMem = dut.pipeMem
  def pmIo = pipeMem.io
  def frontPayload = dut.frontPayload
  def modFrontPayload = dut.modFrontPayload
  def modBackPayload = dut.modBackPayload
  def backPayload = dut.backPayload

  //def memAddrFracWidth = (
  //  0
  //  //1
  //)
  //val rMemAddr = (
  //  //!PipeMemRmwSimDut.doRandFront
  //  !doFormal
  //) generate (
  //  Reg(
  //    UInt(
  //      (
  //        pmIo.front(frontPayload).myExt.memAddr(0).getWidth
  //        + memAddrFracWidth
  //      ) bits
  //    )
  //  ) init(0x0)
  //)
  //def memAddrFracRange = memAddrFracWidth - 1 downto 0
  //def memAddrIntRange = rMemAddr.high downto memAddrFracWidth

  //def incMemAddr(
  //  outpMemAddr: Vec[UInt],
  //): Unit = {
  //  when (
  //    //Cat(False, rMemAddr).asUInt + 1
  //    rMemAddr
  //    === (
  //      PipeMemRmwSimDut.memAddrPlusAmount
  //    ) - 1
  //  ) {
  //    rMemAddr := 0x0
  //  } otherwise {
  //    rMemAddr := rMemAddr + 1
  //  }
  //  for (zdx <- 0 until outpMemAddr.size) {
  //    outpMemAddr(zdx) := (rMemAddr + zdx) >> memAddrFracWidth
  //  }
  //}

  //if (
  //  PipeMemRmwSimDut.doRandFront
  //  && !doFormal
  //) {
  //  //io.front.translateInto(dut.io.front)(
  //  //  dataAssignment=(outp, inp) => {
  //  //    //outp.myExt.memAddr := rMemAddr
  //  //    outp.myExt := inp.myExt
  //  //    outp.myExt.allowOverride
  //  //    when (io.front.fire) {
  //  //      incMemAddr(outpMemAddr=outp.myExt.memAddr)
  //  //    }
  //  //  }
  //  //)
  //  pmIo.front.driveFrom(io.front)(
  //    con=(node, payload) => {
  //      ////outp.myExt.memAddr := rMemAddr
  //      //outp.myExt := inp.myExt
  //      //outp.myExt.allowOverride
  //      //when (io.front.fire) {
  //      //  incMemAddr(outpMemAddr=outp.myExt.memAddr)
  //      //}
  //    }
  //  )
  //  //dut.io.front << io.front
  //}
  //GenerationFlags.formal {
  //  //dut.io.front << io.front
  //}

  pmIo.front.driveFrom(io.front)(
    con=(node, myFrontPayload) => {
      node(frontPayload) := myFrontPayload
    }
  )

  pmIo.back.driveTo(io.back)(
    con=(myBackPayload, node) => {
      myBackPayload := node(backPayload)
    }
  )
  //io.back << dut.io.back
  //def back = dut.io.back



  //val nextFrontValid = (
  //  !PipeMemRmwSimDut.doRandFront
  //  && !doFormal
  //) generate (
  //  Bool()
  //)
  //val rFrontValid = (
  //  !PipeMemRmwSimDut.doRandFront
  //  && !doFormal
  //) generate (
  //  RegNext(nextFrontValid) init(nextFrontValid.getZero)
  //)

  //if (
  //  !PipeMemRmwSimDut.doRandFront
  //  && !doFormal
  //) {
  //  //front.valid := True
  //  //nextFrontValid := !rFrontValid
  //  nextFrontValid := True
  //  front.valid := nextFrontValid

  //  //def front = dut.io.front
  //  front.payload := (
  //    RegNext(front.payload) init(front.payload.getZero)
  //  )
  //  front.myExt.allowOverride
  //  for (zdx <- 0 until modRdPortCnt) {
  //    front.myExt.memAddr(zdx) := (rMemAddr + zdx) >> memAddrFracWidth
  //  }
  //  when (front.fire) {
  //    incMemAddr(outpMemAddr=front.myExt.memAddr)
  //    //rMemAddr := rMemAddr + 1
  //    //rMemAddr(0 downto 0) := rMemAddr(0 downto 0) + 1
  //    //rMemAddr(1 downto 0) := rMemAddr(1 downto 0) + 1
  //    //when (rMemAddr(memAddrFracRange) === 2) {
  //    //  rMemAddr(memAddrIntRange) := (
  //    //    rMemAddr(memAddrIntRange) + 1
  //    //  )
  //    //  rMemAddr(memAddrFracRange) := 0
  //    //}
  //  }
  //}
  //back.ready := True
  //--------

  //val modFrontStm = Stream(modType())
  //val modMidStm = Stream(modType())
  //val modBackStm = Stream(modType())
  ////modFrontStm <-/< dut.io.modFront
  //modFrontStm << dut.io.modFront
  if (
    optModHazardKind == PipeMemRmw.modHazardKindDupl
    || (
      optModHazardKind == PipeMemRmw.modHazardKindFwd
      && modStageCnt > 0
    )
  ) {
    //if (optModHazardKind == PipeMemRmw.modHazardKindFwd) {
      assert(modStageCnt == 1)
    //}
    val midModPayload = PipeMemRmwSimDut.modType()
    midModPayload := (
      RegNext(midModPayload) init(midModPayload.getZero)
    )
    when (pmIo.modFront.isFiring) {
      midModPayload := pmIo.modFront(modFrontPayload)
    }

    def setMidModStages(): Unit = {
      pmIo.midModStages(0) := (
        RegNext(pmIo.midModStages(0))
        init(pmIo.midModStages(0).getZero)
      )
      pmIo.midModStages(0).myExt.valid.allowOverride
      when (
        //modFrontStm.valid
        //&& modMidStm.ready
        //modFrontStm.fire
        pmIo.modFront.isFiring
      ) {
        //pmIo.midModStages(0) := modFrontStm.payload
        //pmIo.midModStages(0) := pmIo.modFront(modFrontPayload)
        pmIo.midModStages(0) := midModPayload
      }
      //pmIo.midModStages(0).myExt.valid := modFrontStm.valid
      pmIo.midModStages(0).myExt.valid := pmIo.modFront.isValid
      //modMidStm << modFrontStm
    }
    setMidModStages()

    pmIo.modFront(modBackPayload) := midModPayload

    val cMidMod = CtrlLink(
      up=pmIo.modFront,
      down=Node(),
    )
    pipeMem.myLinkArr += cMidMod
    val sMidMod = StageLink(
      up=cMidMod.down,
      down=Node()
    )
    pipeMem.myLinkArr += sMidMod
    val s2mMidMod = S2MLink(
      up=sMidMod.down,
      down=pmIo.modBack,
    )
    pipeMem.myLinkArr += s2mMidMod
    if (optModHazardKind == PipeMemRmw.modHazardKindDupl) {
      //modFrontStm.translateInto(
      //  into=modMidStm
      //)(
      //  dataAssignment=(
      //    modMidPayload,
      //    modFrontPayload,
      //  ) => {
      //    //modMidPayload.myExt := modFrontPayload.myExt
      //    modMidPayload := modFrontPayload
      //    modMidPayload.myExt.allowOverride
      //    when (modMidPayload.myExt.hazardId.msb) {
      //      modMidPayload.myExt.modMemWord := (
      //        //modFrontPayload.myExt.rdMemWord + 0x1
      //        modFrontPayload.myExt.rdMemWord(PipeMemRmw.modWrIdx) + 0x1
      //      )
      //    }
      //    //when (
      //    //  modFrontPayload.myExt.hazardId.msb
      //    //) {
      //    //  modMidPayload.myExt.dbgModMemWord := (
      //    //    modMidPayload.myExt.modMemWord
      //    //  )
      //    //} otherwise {
      //    //  modMidPayload.myExt.dbgModMemWord := 0x0
      //    //}
      //    if (modStageCnt > 0) {
      //      ////dut.io.midModStages(0) := (
      //      ////  RegNext(dut.io.midModStages(0))
      //      ////  init(dut.io.midModStages(0).getZero)
      //      ////)
      //      ////dut.io.midModStages(0).myExt.valid.allowOverride
      //      ////dut.io.midModStages(0).myExt.valid := modFrontStm.valid
      //      ////when (modFrontStm.valid) {
      //      ////  dut.io.midModStages(0) := modMidPayload
      //      ////  //dut.io.midModStages(0).valid.allowOverride
      //      ////  //dut.io.midModStages(0).valid := True
      //      ////}
      //      ////dut.io.midModStages(0) := (
      //      ////  RegNext(dut.io.midModStages(0))
      //      ////  init(dut.io.midModStages(0).getZero)
      //      ////)
      //      //dut.io.midModStages(0).myExt.valid.allowOverride
      //      ////when (modFrontStm.valid) {
      //      //  dut.io.midModStages(0) := modFrontStm.payload
      //      ////}
      //      //dut.io.midModStages(0).myExt.valid := modFrontStm.valid
      //      dut.io.midModStages(0) := (
      //        RegNext(dut.io.midModStages(0))
      //        init(dut.io.midModStages(0).getZero)
      //      )
      //      dut.io.midModStages(0).myExt.valid.allowOverride
      //      when (
      //        //modFrontStm.valid
      //        //&& modMidStm.ready
      //        modFrontStm.fire
      //      ) {
      //        dut.io.midModStages(0) := modFrontStm.payload
      //      }
      //      dut.io.midModStages(0).myExt.valid := modFrontStm.valid
      //    }
      //  }
      //)
      when (pmIo.modFront(modFrontPayload).myExt.hazardId.msb) {
        midModPayload.myExt.modMemWord := (
          pmIo.modFront(modFrontPayload).myExt.rdMemWord(
            PipeMemRmw.modWrIdx
          ) + 0x1
        )
      }
    } else {
      //dut.io.midModStages(0) := (
      //  RegNext(dut.io.midModStages(0))
      //  init(dut.io.midModStages(0).getZero)
      //)
      //dut.io.midModStages(0).myExt.valid.allowOverride
      //when (
      //  //modFrontStm.valid
      //  //&& modMidStm.ready
      //  modFrontStm.fire
      //) {
      //  dut.io.midModStages(0) := modFrontStm.payload
      //}
      //dut.io.midModStages(0).myExt.valid := modFrontStm.valid
      //modMidStm << modFrontStm
    }
    //modBackStm <-/< modMidStm
  } else if (
    optModHazardKind == PipeMemRmw.modHazardKindFwd
  ) {
    assert(modStageCnt == 0)
    //modMidStm << modFrontStm
    //modBackStm << modMidStm
    //val pipe = PipeHelper(linkArr=pipeMem.myLinkArr)
    pipeMem.myLinkArr += DirectLink(
      up=pmIo.modFront,
      down=pmIo.modBack,
    )
  }
  //dut.io.modBack << modBackStm
  Builder(pipeMem.myLinkArr)
}
//object PipeMemRmwSim extends App {
//  def clkRate = 25.0 MHz
//  val simSpinalConfig = SpinalConfig(
//    defaultClockDomainFrequency=FixedFrequency(clkRate)
//  )
//  SimConfig
//    .withConfig(config=simSpinalConfig)
//    //.withFstWave
//    .withVcdWave
//    //.withGhdl
//    //.withVerilator
//    .compile(
//      PipeMemRmwTester()
//    )
//    .doSim/*("PipeMemRmwSim")*/({ dut =>
//      //dut.io
//      def simNumClks = (
//        //1000
//        //32
//        100
//      )
//      ////SimTimeout(32)
//      ////val scoreboard = ScoreboardInOrder[Int]()
//      if (PipeMemRmwSimDut.doRandFront) {
//        StreamDriver(dut.io.front, dut.clockDomain) { payload =>
//          //payload.myExt #= payload.myExt.getZero
//          ////payload.allowOverride
//          //payload.myExt.memAddr.randomize
//          //payload.randomize()
//          //payload.myExt.memAddr #= 
//          //if (
//          //  (
//          //    dut.io.front.valid.toBoolean
//          //  ) && (
//          //    dut.io.front.ready.toBoolean
//          //  )
//          //) {
//          //  def myMemAddr = payload.myExt.memAddr(PipeMemRmw.modWrIdx)
//          //  val tempMemAddr = (
//          //    myMemAddr.toInt
//          //  )
//          //  if (
//          //    tempMemAddr + 1
//          //    > (
//          //      (
//          //        //0x8
//          //        0x3
//          //      )
//          //      //- 1
//          //    )
//          //  ) {
//          //    myMemAddr #= 0
//          //  } else {
//          //    myMemAddr #= tempMemAddr + 1
//          //  }
//          //}
//          payload.randomize
//          true
//        }
//      } else {
//        //dut.io.front.valid #= true
//        //dut.io.front.valid #= dut.io.front.valid.randomize
//      }
//      ////dut.io.front.payload.randomize()
//
//      ////StreamMonitor(dut.io.front, dut.clockDomain) { payload => 
//      ////  scoreboard.pushRef(payload.toInt)
//      ////}
//      StreamReadyRandomizer(dut.io.back, dut.clockDomain)
//      //dut.io.back.ready #= true
//      //dut.io.back.ready #= true
//      //dut.clockDomain.forkStimulus(period=10)
//      ////dut.clockDomain.waitActiveEdgeWhere(scoreboard.matches == 100)
//
//      //def tempAddrFracWidth = 2
//      //val tempAddr = 0 << tempAddrFracWidth
//      dut.clockDomain.forkStimulus(period=10)
//
//      for (idx <- 0 until simNumClks) {
//        //dut.io.front.myExt.memAddr #= 
//        dut.clockDomain.waitRisingEdge()
//      }
//      simSuccess()
//    })
//}
