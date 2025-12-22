package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


import libcheesevoyage.general.RamSdpPipeConfig
import libcheesevoyage.general.RamSdpPipeIo
import libcheesevoyage.general.RamSdpPipe

// A bus-attached BRAM, LUTRAM, etc.
case class LcvBusMemConfig(
  busCfg: LcvBusConfig,
  //mmapCfg: LcvBusMemMapConfig,
  depth: Int,
  init: Option[Seq[Bits]]=None,
  initBigInt: Option[Seq[BigInt]]=None,
  arrRamStyleAltera: String="no_rw_check, M10K",
  arrRamStyleXilinx: String="block",
  arrRwAddrCollisionXilinx: String="",
) {
  val ramCfg = RamSdpPipeConfig(
    wordType=Bits(busCfg.dataWidth bits),
    depth=depth,
    optIncludeWrByteEn=true,
    init=init,
    initBigInt=initBigInt,
    arrRamStyleAltera=arrRamStyleAltera,
    arrRamStyleXilinx=arrRamStyleXilinx,
    arrRwAddrCollisionXilinx=arrRwAddrCollisionXilinx,
  )
  //def busCfg = mmapCfg.busCfg
  //def depth = mmapCfg.addrSliceSize
}

case class LcvBusMemIo(
  cfg: LcvBusMemConfig,
) extends Bundle {
  val bus = slave(LcvBusIo(cfg=cfg.busCfg))
}

case class LcvBusMem(
  cfg: LcvBusMemConfig,
) extends Component {
  //--------
  def busCfg = cfg.busCfg
  //--------
  val io = LcvBusMemIo(cfg=cfg)
  //--------
  val ram = RamSdpPipe(
    cfg=cfg.ramCfg
  )
  //--------
  object State
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      NON_BURST_READ_PIPE_1,
      NON_BURST,
      READ_BURST_PIPE_2,
      READ_BURST_PIPE_1,
      READ_BURST,
      READ_BURST_POST_1,
      WRITE_BURST,
      WRITE_BURST_POST_1,
      WAIT_D2H_FIFO_EMPTY
      = newElement()
  }
  val rState = (
    Reg(State())
    init(State.IDLE)
  )
  //--------
  //val rH2dPayload = (
  //  RegNext(io.bus.h2dBus.payload, init=io.bus.h2dBus.payload.getZero)
  //)
  def myH2dPayload = io.bus.h2dBus.payload
  val rSavedH2dPayload = (
    Reg(cloneOf(io.bus.h2dBus.payload))
    init(io.bus.h2dBus.payload.getZero)
  )
  //io.bus.h2dBus.ready.setAsReg() init(False)
  //io.bus.d2hBus.valid.setAsReg() init(False)
  //io.bus.d2hBus.payload.setAsReg() init(io.bus.d2hBus.payload.getZero)
  val rH2dReady = Reg(Bool(), init=False)
  val rD2hValid = Reg(Bool(), init=False)
  val rD2hPayload = (
    Reg(cloneOf(io.bus.d2hBus.payload), init=io.bus.d2hBus.payload.getZero)
  )
  io.bus.h2dBus.ready := rH2dReady
  //io.bus.d2hBus.valid := rD2hValid
  //io.bus.d2hBus.payload := rD2hPayload

  val myD2hFifo = (
    StreamFifo(
      dataType=(
        //UInt(busCfg.dataWidth bits)
        cloneOf(io.bus.d2hBus.payload)
      ),
      depth=(busCfg.maxBurstSizeMinus1 + 1),
      latency=2,
      forFMax=true,
    )
  )
  io.bus.d2hBus << myD2hFifo.io.pop
  myD2hFifo.io.push.valid := rD2hValid
  myD2hFifo.io.push.payload := rD2hPayload

  def myRamAddrRshift = log2Up(busCfg.dataWidth / 8)
  def myRamAddrRange = (
    ram.io.rdAddr.high + myRamAddrRshift downto myRamAddrRshift
  )

  //val rRamRdEn = Reg(Bool(), init=True)
  //ram.io.rdEn := rRamRdEn

  ram.io.rdEn := False
  ram.io.rdAddr := io.bus.h2dBus.addr(myRamAddrRange)

  ram.io.wrEn := False
  ram.io.wrAddr := io.bus.h2dBus.addr(myRamAddrRange)
  ram.io.wrData := io.bus.h2dBus.data.asBits
  ram.io.wrByteEn := io.bus.h2dBus.byteEn.asBits
  //val rRamWrEn = Reg(Bool(), init=False)
  //val rRamWrAddr = (
  //  Reg(cloneOf(ram.io.wrAddr), init=ram.io.wrAddr.getZero)
  //)
  //val rRamWrData = (
  //  Reg(cloneOf(ram.io.wrData), init=ram.io.wrData.getZero)
  //)
  //val rRamWrByteEn = (
  //  Reg(cloneOf(ram.io.wrByteEn), init=ram.io.wrByteEn.getZero)
  //)
  //ram.io.wrEn := rRamWrEn
  //ram.io.wrAddr := rRamWrAddr
  //ram.io.wrData := rRamWrData
  //ram.io.wrByteEn := rRamWrByteEn

  def myRdBurstCnt0InitVal: UInt = (
    // -2
    //U(rRdBurstCnt(1).getWidth bits, 0 -> False, default -> True)
    (-S(s"${busCfg.burstCntWidth}'d2")).asUInt
  )
  val rRdBurstCnt = (
    Vec[UInt](List(
      (Reg(UInt(busCfg.burstCntWidth bits)) init(myRdBurstCnt0InitVal)),
      (Reg(UInt(busCfg.burstCntWidth bits)) init(0x0))
    ))
  )
  def doInitRdBurstCnt(): Unit = {
    // `rRdBurstCnt(0)` is the lagging counter, used to determine when
    // we're done filling `myD2hFifo`
    // `rRdBurstCnt(0)` is lagging behind by two because it takes two
    // cycles to read a value from a `RamSdpPipe`, and this allows us to
    // pipeline said reads during a an `LcvBus` read burst.
    rRdBurstCnt(0) := myRdBurstCnt0InitVal
    // `rRdBurstCnt(1)` is the leading counter, used to determine when
    // we're done reading from `ram` during an `LcvBus` read burst.
    rRdBurstCnt(1) := 0x0
  }
  def doIncrRdBurstCnt(): Unit = {
    rRdBurstCnt.foreach(item => {
      item := item + 1
    })
  }

  val rWrBurstCnt = (
    Reg(UInt(busCfg.burstCntWidth bits))
    init(0x0)
  )
  rD2hPayload.src.allowOverride

  switch (rState) {
    is (State.IDLE) {
      rSavedH2dPayload := io.bus.h2dBus.payload

      rWrBurstCnt := 0x0

      rD2hValid := False
      rD2hPayload := rD2hPayload.getZero

      switch (
        io.bus.h2dBus.valid
        ## io.bus.h2dBus.isWrite
        ## io.bus.h2dBus.burstFirst
      ) {
        is (B"100") {
          // read, non-burst
          rState := State.NON_BURST_READ_PIPE_1
          //io.bus.h2dBus.ready := True
          //ram.io.rdEn := (
          //  //False
          //  //True
          //  False
          //)
          rH2dReady := (
            //True
            False
          )
        }
        is (B"101") {
          // read, burst
          rState := (
            State.READ_BURST_PIPE_2
            //State.READ_BURST_PIPE_1
          )
          //io.bus.h2dBus.ready := True
          ram.io.rdAddr := (
            io.bus.h2dBus.burstAddr(
              rRdBurstCnt(1).getZero,
              incrBurstCnt=false,
            )(myRamAddrRange)
          )
          //rRdBurstCnt := 1
          doIncrRdBurstCnt()
          rH2dReady := True
          //rD2hPayload.burstFirst := False
        }
        is (B"110") {
          // write, non-burst
          rState := State.NON_BURST
          //io.bus.h2dBus.ready := True
          rH2dReady := True
          ram.io.rdEn := False
          ram.io.wrEn := True
        }
        is (B"111") {
          // write, burst
          rState := State.WRITE_BURST
          //io.bus.h2dBus.ready := False
          rH2dReady := False
        }
        default {
          // no active transaction
          //io.bus.h2dBus.ready := False
          rH2dReady := False
        }
      }
    }
    is (State.NON_BURST_READ_PIPE_1) {
      ram.io.rdEn := True
      rH2dReady := True
      rState := State.NON_BURST
    }
    is (State.NON_BURST) {
      //io.bus.h2dBus.ready := False
      //when (io.bus.h2dBus.fire) {
      //  rD2hPayload.src := io.bus.h2dBus.src
      //}
      ram.io.rdEn := False
      ram.io.wrEn := False
      when (io.bus.h2dBus.fire) {
        rD2hPayload.src := io.bus.h2dBus.src
        rH2dReady := False
      }
      when (
        //!rH2dReady
        //||
        io.bus.h2dBus.fire
      ) {
        rD2hValid := True
        rD2hPayload.data.assignFromBits(ram.io.rdData)
      }
      when (myD2hFifo.io.push.fire) {
        rD2hValid := False
        rState := State.WAIT_D2H_FIFO_EMPTY
      }
    }
    is (State.READ_BURST_PIPE_2) {
      rState := State.READ_BURST_PIPE_1

      ram.io.rdEn := True
      ram.io.rdAddr := (
        rSavedH2dPayload.burstAddr(
          rRdBurstCnt(1),
          incrBurstCnt=false
        )(myRamAddrRange)
      )
      //rRdBurstCnt.foreach(item => item := item + 1)

      doIncrRdBurstCnt()
    }
    is (State.READ_BURST_PIPE_1) {
      //io.bus.h2dBus.ready := False
      rState := State.READ_BURST
      ram.io.rdEn := True
      ram.io.rdAddr := (
        rSavedH2dPayload.burstAddr(
          rRdBurstCnt(1),
          incrBurstCnt=false,
        )(myRamAddrRange)
      )
      //rRdBurstCnt := rRdBurstCnt + 1
      doIncrRdBurstCnt()

      rD2hValid := True
      rD2hPayload.burstFirst := True
      rD2hPayload.data := ram.io.rdData.asUInt
    }
    is (State.READ_BURST) {
      //io.bus.h2dBus.ready := False
      rH2dReady := False
      ram.io.rdEn := RegNext(ram.io.rdEn, init=ram.io.rdEn.getZero)
      ram.io.rdAddr := (
        rSavedH2dPayload.burstAddr(
          rRdBurstCnt(1),
          incrBurstCnt=false,
        )(myRamAddrRange)
      )
      //rD2hPayload.burstFirst := False
      //rRdBurstCnt := rRdBurstCnt + 1

      doIncrRdBurstCnt()
      rD2hPayload.data := ram.io.rdData.asUInt

      when (rD2hPayload.burstFirst) {
        rD2hPayload.burstFirst := False
      }
      //when (rRdBurstCnt === (1 << rRdBurstCnt.getWidth) - 2) {
      //  rD2hPayload.burstLast := True
      //}
      when (rRdBurstCnt(1) === 0x1) {
        ram.io.rdEn := False
      }
      when (
        rRdBurstCnt(1) === 0x1
        ////=== -1
        //=== U(rRdBurstCnt(0).getWidth bits, default -> True)
      ) {
        rD2hPayload.burstLast := True
      }
      when (rRdBurstCnt(1) === 0x2) {
        rD2hValid := False
        rD2hPayload.burstLast := False
        rState := State.WAIT_D2H_FIFO_EMPTY
      } otherwise {
      }
      //when (rD2hPayload.burstLast) {
      //  rD2hValid := False
      //  rD2hPayload.burstLast := False
      //  rState := State.WAIT_D2H_FIFO_EMPTY
      //}
    }
    //is (State.READ_BURST_POST_1) {
    //  
    //}
    is (State.WRITE_BURST) {
      rH2dReady := True
      ram.io.wrAddr := (
        rSavedH2dPayload.burstAddr(
          rWrBurstCnt,
          incrBurstCnt=false,
        )(myRamAddrRange)
      )
      ram.io.wrData := myH2dPayload.data.asBits
      ram.io.wrByteEn := myH2dPayload.byteEn.asBits

      when (io.bus.h2dBus.fire) {
        ram.io.wrEn := True
        rWrBurstCnt := rWrBurstCnt + 1
        when (myH2dPayload.burstLast) {
          rState := State.WRITE_BURST_POST_1
          rH2dReady := False
        }
      }
    }
    is (State.WRITE_BURST_POST_1) {
      //io.bus.h2dBus.ready := False
      rH2dReady := False
      rD2hValid := True
      rWrBurstCnt := 0x0
      when (myD2hFifo.io.push.fire) {
        rD2hValid := False
        rState := State.WAIT_D2H_FIFO_EMPTY
      }
    }
    is (State.WAIT_D2H_FIFO_EMPTY) {
      doInitRdBurstCnt()
      rD2hValid := False
      when (myD2hFifo.io.occupancy === 0) {
        rState := State.IDLE
      }
    }
  }
}

object LcvBusMemSpinalConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    )
  )
}


case class LcvBusMemSimDut(
  cfg: LcvBusMemConfig=LcvBusMemTestConfig.cfg,
) extends Component {
  //--------
  val io = new Bundle {
  }
  //--------
  //val memCfg = LcvBusMemTestConfig.cfg

  val myMem = LcvBusMem(cfg=cfg)
  val myMemTester = LcvBusDeviceRamTester(
    cfg=LcvBusDeviceRamTesterConfig(
      busCfg=cfg.busCfg,
      kind=(
        LcvBusDeviceRamTesterKind.DualBurstRandDataSemiRandAddr
        //(
        //  optNonCoherentCacheSetWidth=(
        //    //None
        //    Some(8)
        //  )
        //)
        //LcvBusDeviceRamTesterKind.DualBurstRandData
        //LcvBusDeviceRamTesterKind.NoBurstRandData
      ),
    )
  )
  myMem.io.bus <> myMemTester.io.busVec.head
}

object LcvBusMemTestConfig {
  val depth = 1024
  val cfg = LcvBusMemConfig(
    busCfg=LcvBusConfig(
      mainCfg=LcvBusMainConfig(
        dataWidth=32,
        addrWidth=32,
        allowBurst=true,
        burstAlwaysMaxSize=true,
        srcWidth=1,
      ),
      cacheCfg=None,
    ),
    depth=depth,
    initBigInt=Some(Array.fill(depth)(BigInt(0)))
  )
}
object LcvBusMemToVerilog extends App {
  LcvBusMemSpinalConfig.spinal.generateVerilog{
    LcvBusMem(
      cfg=LcvBusMemTestConfig.cfg
    )
  }
}

object LcvBusMemSim extends App {
  def clkRate = 100.0 MHz
  val simSpinalConfig = SpinalConfig(
    defaultClockDomainFrequency=FixedFrequency(clkRate)
  )
  SimConfig
    .withConfig(config=simSpinalConfig)
    .withFstWave
    .compile(
      //LcvSdramSimDut(clkRate=clkRate)
      LcvBusMemSimDut()
    )
    .doSim { dut =>
      dut.clockDomain.forkStimulus(period=10)
      def simNumClks = (
        200000
      )
      for (idx <- 0 until simNumClks) {
        dut.clockDomain.waitRisingEdge()
      }
      simSuccess()
    }
}
