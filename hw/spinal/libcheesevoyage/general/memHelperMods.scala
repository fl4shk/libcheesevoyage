package libcheesevoyage.general

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.sim._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

case class MultiMemReadSync[
  T <: Data
](
  someMem: Mem[T],
  //dataType: HardType[T],
  //addrWidth: Int,
  numReaders: Int=1,
  alwaysEn: Boolean=false,
) extends Area {
  assert(numReaders > 0)

  def addrWidth = log2Up(someMem.wordCount)
  def dataType() = someMem.wordType()

  val addrVec = Vec.fill(numReaders)(UInt(addrWidth bits))
  val dataVec = Vec.fill(numReaders)(dataType())
  val enVec = (!alwaysEn) generate Vec.fill(numReaders)(Bool())
  //val rdAllowedVec = Vec.fill(numReaders)(Bool())

  def readSync(
    //addr: UInt,
    idx: Int,
  ): T = {
    //addrVec(idx) := addr
    dataVec(idx) := (
      if (!alwaysEn) {
        someMem.readSync(
          address=addrVec(idx),
          enable=enVec(idx),
        )
      } else { // if (alwaysEn)
        someMem.readSync(
          address=addrVec(idx),
        )
      }
    )
    dataVec(idx)
  }
}
//case class MultiMemReadSync() extends Component {
//  val io = new Bundle {
//    val readAllowed = in port Bool()
//    val data = master port Stream(Bits(10 bit))
//  }
//  val mem = Mem(Bits(10 bit), 128)
//  val address = Reg(UInt(log2Up(128) bit))
//
//  val doRead = io.readAllowed && (!io.data.valid || io.data.ready)
//  when(doRead) { address := address + 1 }
//  io.data.payload := mem.readSync(address, enable=doRead)
//  io.data.valid.setAsReg().clearWhen(io.data.fire).setWhen(doRead)
//}
// If the address comes from a stream then you have to stall that stream until you can update the output buffer. When you can (no data on the output or receiver ready,...) do the read and signal ready (if the address stream is valid)

//object MultiMemReadSync extends App {
//  SimConfig.withVcdWave
//    .compile {
//      val dut = MultiMemReadSync()
//      dut.mem.simPublic()
//      dut
//    }
//    .doSim("blub") { dut =>
//      for (i <- 0 until 128) { dut.mem.setBigInt(i, i) }
//      StreamReadyRandomizer(dut.io.data, dut.clockDomain)
//      dut.clockDomain.forkStimulus(10)
//      for(i <- 0 until 10) {
//        dut.clockDomain.waitSampling(scala.util.Random.nextInt(10))
//        dut.io.readAllowed #= !dut.io.readAllowed.toBoolean
//      }
//    }
//}

// For when `multiRd.addrVec(rdIdx)` is driven non-synchronously but
// `multiRd.readSync()` is still used
case class MemReadSyncIntoPipe[
  PipeInPayloadT <: Data,
  PipeOutPayloadT <: Data,
  MemWordT <: Data,
](
  pipeIn: Stream[PipeInPayloadT],
  pipeOut: Stream[PipeOutPayloadT],
  someMem: Mem[MemWordT],
)(
  getInpAddrFunc: (
    PipeInPayloadT
  ) => UInt,
  getOutpRdDataFunc: (
    PipeOutPayloadT
  ) => MemWordT,
) extends Area {

  def inpAddr = getInpAddrFunc(pipeIn.payload)
  def outpRdData = getOutpRdDataFunc(pipeOut.payload)

  val tempRdEn = Bool()

  // (!pipeOutRdData.valid || pipeOutRdData.ready)
  // same as `!isStall` (through DeMorgan's Theorem)
  //tempRdEn := pipeIn.valid && rdAllowed && !pipeOut.isStall 
  tempRdEn := pipeIn.valid && !pipeOut.isStall 

  // If the address comes from a stream then you have to stall that
  // stream until you can update the output buffer. When you can
  // (no data on the output or receiver ready,... (!isStall))
  // do the read and signal ready (if the address stream is valid)
  //tempRdEn := rdAllowed && !pipeOut.isStall 
  outpRdData := someMem.readSync(
    address=inpAddr,
    enable=tempRdEn,
  )
  pipeOut.valid
    .setAsReg()
    .clearWhen(pipeOut.fire)
    .setWhen(
      //En
      tempRdEn
    )
  pipeIn.ready := (
    //pipeIn.valid
    //&&
    tempRdEn
  )

  //rdEn := tempRdEn

  //rdAddr := inpAddr
}
case class MemReadSyncIntoPipeTestDutIo(
  memSize: Int=128,
) extends Bundle {
  //val rdAllowed = in Bool()
  val addr = slave Stream(UInt(log2Up(memSize) bits))
  val data = master Stream(UInt(dataWidth bits))
  def dataWidth = log2Up(memSize)
}
case class MemReadSyncIntoPipeTestDut(
  memSize: Int=128,
) extends Component {
  //def dataWidth = log2Up(memSize) + 1
  //val io = new Bundle {
  //}
  val io = MemReadSyncIntoPipeTestDutIo(
    memSize=memSize,
  )
  val mem = Mem(UInt(io.dataWidth bits), memSize)
  //val multiRd = MultiMemReadSync(
  //  someMem=mem,
  //  numReaders=1,
  //  alwaysEn=false,
  //)
  //multiRd.rdAllowedVec(0) := io.rdAllowed
  val rdIntoPipe = MemReadSyncIntoPipe(
    //--------
    pipeIn=io.addr,
    pipeOut=io.data,
    someMem=mem,
    //--------
  )(
    getInpAddrFunc=(payload => payload),
    getOutpRdDataFunc=(payload => payload),
  )
  //val rdIntoPipe = MemReadSyncIntoPipe(
  //  //--------
  //  pipeIn=io.addr,
  //  inpAddr=io.addr.payload,
  //  //--------
  //  pipeOut=io.data,
  //  outpRdData=io.data.payload,
  //  //--------
  //  multiRd=multiRd,
  //  rdIdx=0,
  //  //--------
  //)
}
object MemReadSyncIntoPipeSim extends App {
  val simSpinalConfig = SpinalConfig(
    defaultClockDomainFrequency=FixedFrequency(100 MHz)
  )
  def memSize = 128
  SimConfig
    .withConfig(config=simSpinalConfig)
    .withVcdWave
    .compile {
      val dut = MemReadSyncIntoPipeTestDut(
        memSize=memSize,
      )
      dut.mem.simPublic()
      //dut.testDut.mem.simPublic()
      //dut.testDut.io.data.simPublic()
      dut
    }
    .doSim("blub") { dut =>
      for (i <- 0 until memSize) {
        dut.mem.setBigInt(i, i)
      }

      val scoreboard = ScoreboardInOrder[Int]()

      //StreamValidRandomizer(dut.io.data, dut.clockDomain)
      StreamDriver(
        stream=dut.io.addr,
        clockDomain=dut.clockDomain
      )(
        driver={ payload =>
          //if (dut.io.addr.valid.toBoolean && dut.io.addr.ready.toBoolean) {
          //  payload.randomize()
          //}
          //payload := RegNext(payload) + 1
          //if (
          //  //dut.io.addr.valid.toBoolean && dut.io.addr.ready.toBoolean
          //  !dut.io.addr.valid.toBoolean
          //  || dut.io.addr.ready.toBoolean
          //) {
          //  payload #= payload.toInt + 1
          //}
          payload.randomize
          true
        }
      )
      StreamReadyRandomizer(dut.io.data, dut.clockDomain)
      StreamMonitor(dut.io.data, dut.clockDomain){payload =>
        scoreboard.pushRef(payload.toInt)
      }
      dut.clockDomain.forkStimulus(10)
      for (
        i <- 0 until 2048 //memSize * 5 //10
      ) {
        //dut.clockDomain.waitSampling(scala.util.Random.nextInt(10))
        //dut.io.rdAllowed #= !dut.io.rdAllowed.toBoolean
        dut.clockDomain.waitRisingEdge()
        //dut.io.rdAllowed #= true
      }
      simSuccess()
    }
}

////object MemReadSyncIntoStreamHaltVecs {
////  def checkV2dIndices(
////    haltV2d: Vec[MemReadSyncIntoStreamHaltVecs],
////    haltV2dJdx: Int,
////    haltV2dIdx: Int,
////  ): Unit = {
////  }
////}
//case class MemReadSyncIntoStreamHaltVecs(
//  size: Int
//) extends Bundle {
//  assert(size > 0)
//  val pipe = Vec.fill(size)(Bool())
//  val fifoPush = Vec.fill(size)(Bool())
//  val fifoPop = Vec.fill(size)(Bool())
//
//  //def implReduce(
//  //  someHaltVec: Vec[Bool]
//  //): Bool = {
//  //  someHaltVec.reduceBalancedTree(_ || _)
//  //}
//  //def reducePipe(): Bool = implReduce(someHaltVec=pipe)
//  //def reduceFifoPush(): Bool = implReduce(someHaltVec=fifoPush)
//  //def reduceFifoPop(): Bool = implReduce(someHaltVec=fifoPop)
//
//  //def implReduceV2d(
//  //  someV2d: Vec[MemReadSyncIntoStreamHaltVecs],
//  //)(
//  //  pick
//  //): Bool  
//  //def reduceV2dPipe(
//  //)
//
//  def implCheckIdx[
//    T <: Data
//  ](
//    someVec: Vec[T],
//    idx: Int,
//  ): Unit = {
//    assert(idx >= 0 && idx < someVec.size)
//  }
//  //def checkV2dIdx[
//  //  T <: Data
//  //](
//  //  v2d: Vec[Vec[T]],
//  //  outerIdx: Int,
//  //  innerIdx: Int,
//  //): Unit = {
//  //  implCheckIdx(
//  //    someVec=v2d,
//  //    idx=outerIdx,
//  //  )
//  //  implCheckIdx(
//  //    someVec=v2d(outerIdx),
//  //    idx=innerIdx,
//  //  )
//  //}
//  def checkIdx(
//    idx: Int,
//  ): Unit = {
//    //assert(idx >= 0 && idx < size)
//    implCheckIdx(
//      someVec=pipe,
//      idx=idx,
//    )
//  }
//}

//// For when `multiRd.addrVec(rdIdx)` is driven non-synchronously but
//// `multiRd.readSync()` is still used
//case class MemReadSyncIntoStream[
//  //PipeInPayloadT <: Data,
//  //PipeOutPayloadT <: Data,
//  //FifoWordT <: Data,
//  MemWordT <: Data,
//](
//  //pipeIn: Stream[PipeInPayloadT],
//  //pipeOut: Stream[PipeOutPayloadT],
//  //fifoWordType: HardType[FifoWordT],
//  memWordType: HardType[MemWordT],
//  multiRd: MultiMemReadSync[MemWordT],
//  rdIdx: Int,
//  haltVecs: MemReadSyncIntoStreamHaltVecs,
//  haltIdx: Int,
//  //haltV2d: Vec[MemReadSyncIntoStreamHaltVecs],
//  //haltJdx: Int,
//  //haltIdx: Int,
//) extends Area {
//  multiRd.checkIdx(idx=rdIdx)
//  haltVecs.checkIdx(idx=haltIdx)
//
//  //def fifoDepth = (
//  //  //8
//  //  3
//  //)
//  //def fifoHaltWhenGeAmountCanPop = (
//  //  //4
//  //  1
//  //)
//  //val fifo = AsyncReadFifo(
//  //  //dataType=fifoElemT(),
//  //  dataType=memWordType(),
//  //  //dataType=fifoWordType(),
//  //  depth=fifoDepth,
//  //  arrRamStyle="auto",
//  //)
//
//  //val myFifoPush = cloneOf(fifo.io.push)
//  //fifo.io.push << myFifoPush.haltWhen(haltVecs.fifoPush(haltIdx))
//  //myFifoPush.valid := True
//
//  //val myFifoPop = cloneOf(fifo.io.pop)
//  //myFifoPop << fifo.io.pop.haltWhen(haltVecs.fifoPop(haltIdx))
//  //myFifoPop.ready := True
//}

//case class MemReadSyncParams[
//  MemWordT <: Data
//](
//  wordType: HardType[MemWordT],
//  wordCount: Int,
//) extends Bundle {
//}
//
//case class MemReadSyncIntoStreamIo[
//  PipeInPayloadT <: Data,
//  PipeOutPayloadT <: Data,
//  MemWordT <: Data,
//](
//  pipeInPayloadType: HardType[PipeInPayloadT],
//  pipeOutPayloadType: HardType[PipeOutPayloadT],
//  memWordType: HardType[MemWordT],
//  //memSize: Int,
//  //fifoSize: Int,
//) extends Bundle {
//  //--------
//  val pipeIn = master Stream(pipeInPayloadType())
//  val pipeOut = slave Stream(pipeOutPayloadType())
//  //val memPop = master Stream(MemWordType()) 
//  //--------
//}
//case class MemReadSyncIntoStream[
//  PipeInPayloadT <: Data,
//  PipeOutPayloadT <: Data,
//  MemWordT <: Data,
//](
//  pipeInPayloadType: HardType[PipeInPayloadT],
//  pipeOutPayloadType: HardType[PipeOutPayloadT],
//  memWordType: HardType[MemWordT],
//  //memSize: Int,
//  //fifoSize: Int,
//) extends Component {
//  val io = MemReadSyncIntoStreamIo(
//    pipeInPayloadType=pipeInPayloadType(),
//    pipeOutPayloadType=pipeOutPayloadType(),
//    memWordType=memWordType(),
//    //memSize=memSize,
//  )
//}
