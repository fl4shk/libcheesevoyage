package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


case class LcvBusArbiterConfig(
  busCfg: LcvBusConfig,
  //mmapCfg: LcvBusMemMapConfig,
  numHosts: Int,
  //isPriority: Boolean,
) {
  //val busCfg = mmapCfg.busCfg
}

case class LcvBusArbiterIo(
  cfg: LcvBusArbiterConfig
) extends Bundle {
  val hostVec = (
    Vec[LcvBusIo]{
      val tempArr = new ArrayBuffer[LcvBusIo]()
      for (idx <- 0 until cfg.numHosts) {
        tempArr += LcvBusIo(cfg=cfg.busCfg)
      }
      tempArr
    }
  )
  for (host <- hostVec.view) {
    slave(host)
  }
  val dev = (
    master(LcvBusIo(cfg=cfg.busCfg))
  )
}

case class LcvBusArbiter(
  cfg: LcvBusArbiterConfig,
) extends Component {
  // (for now) round robin bus arbiter
  val io = LcvBusArbiterIo(cfg=cfg)

  io.dev.d2hBus.ready := False
  io.dev.h2dBus.valid := False
  io.dev.h2dBus.payload := (
    io.dev.h2dBus.payload.getZero
  )
  for (hostIdx <- 0 until cfg.numHosts) {
    val myHostH2dBus = io.hostVec(hostIdx).h2dBus
    val myHostD2hBus = io.hostVec(hostIdx).d2hBus
    myHostD2hBus.valid := False
    myHostD2hBus.payload := (
      myHostD2hBus.payload.getZero
    )
    myHostH2dBus.ready := False
  }

  val rArbitCnt = (
    Reg(UInt(log2Up(cfg.numHosts) bits))
    init(0x0)
  )
  val myHostH2dBus = io.hostVec(rArbitCnt).h2dBus
  val myHostD2hBus = io.hostVec(rArbitCnt).d2hBus

  val rSeenHostH2dFireEtc = (
    Vec.fill(3)(
      Reg(Bool(), init=False)
    )
  )
  def maybeSetSeenHostH2dFireEtc(
    idx: Int
  ): Unit = {
    if (idx == 0) {
      when (
        RegNext(myHostH2dBus.valid, init=False)
        && myHostH2dBus.ready
      ) {
        rSeenHostH2dFireEtc(idx) := True
      }
    } else if (idx == 1) {
      when (
        RegNext(myHostH2dBus.valid, init=False)
        && myHostH2dBus.ready
      ) {
        rSeenHostH2dFireEtc(idx) := True
      }
    } else if (idx == 2) {
      when (
        RegNext(myHostH2dBus.valid, init=False)
        && myHostH2dBus.ready
        && RegNext(myHostH2dBus.burstLast, init=False)
      ) {
        rSeenHostH2dFireEtc(idx) := True
      }
    } else {
      require(false)
    }
  }

  def doIncrCntEtc(seenHostH2dFireEtcIdx: Option[Int]): Unit = {
    seenHostH2dFireEtcIdx match {
      case Some(myIdx) => {
        rSeenHostH2dFireEtc(myIdx) := False
      }
      case None => {
      }
    }
    if ((1 << log2Up(cfg.numHosts)) == cfg.numHosts) {
      rArbitCnt := rArbitCnt + 1
    } else {
      when (rArbitCnt + 1 < cfg.numHosts) {
        rArbitCnt := rArbitCnt + 1
      } otherwise {
        rArbitCnt := 0x0
      }
    }
  }

  object State extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      IDLE,
      NON_BURST,
      READ_BURST,
      WRITE_BURST
      = newElement();
  }
  val rState = (
    Reg(State())
    init(State.IDLE)
  )
  switch (rState) {
    is (State.IDLE) {
      //rSeenHostH2dFireEtc.foreach(_ := False)
      switch (
        RegNext(
          myHostH2dBus.valid
          ## myHostH2dBus.burstFirst
          ## myHostH2dBus.isWrite
        )
        init(0x0)
      ) {
        is (M"10-") {
          // either read or write, but *NOT* a burst
          rState := State.NON_BURST
          io.dev.h2dBus << myHostH2dBus 
          myHostD2hBus << io.dev.d2hBus
          maybeSetSeenHostH2dFireEtc(0)
        }
        is (M"110") {
          // read burst
          rState := State.READ_BURST
          io.dev.h2dBus << myHostH2dBus 
          myHostD2hBus << io.dev.d2hBus
          maybeSetSeenHostH2dFireEtc(1)
        }
        is (M"111") {
          // write burst
          rState := State.WRITE_BURST
          io.dev.h2dBus << myHostH2dBus 
          myHostD2hBus << io.dev.d2hBus
          maybeSetSeenHostH2dFireEtc(2)
        }
        default {
          // the current host is not requesting a transaction
          doIncrCntEtc(None)
        }
      }
    }
    is (State.NON_BURST) {
      io.dev.h2dBus << myHostH2dBus 
      myHostD2hBus << io.dev.d2hBus
      //when (
      //  RegNext(myHostH2dBus.valid, init=False)
      //  && myHostH2dBus.ready
      //) {
      //  rSeenHostH2dFireEtc(0) := True
      //}
      maybeSetSeenHostH2dFireEtc(0)
      when (
        rSeenHostH2dFireEtc(0)
        && RegNext(myHostD2hBus.valid, init=False)
        && myHostD2hBus.ready
      ) {
        rState := State.IDLE
        doIncrCntEtc(Some(0))
      }
    }
    is (State.READ_BURST) {
      io.dev.h2dBus << myHostH2dBus 
      myHostD2hBus << io.dev.d2hBus

      //when (
      //  RegNext(myHostH2dBus.valid, init=False)
      //  && myHostH2dBus.ready
      //) {
      //  rSeenHostH2dFireEtc(1) := True
      //}
      maybeSetSeenHostH2dFireEtc(1)

      when (
        rSeenHostH2dFireEtc(1)
        && RegNext(myHostD2hBus.valid, init=False)
        && myHostD2hBus.ready
        && RegNext(myHostD2hBus.burstLast)
      ) {
        rState := State.IDLE
        doIncrCntEtc(Some(1))
      }
    }
    is (State.WRITE_BURST) {
      io.dev.h2dBus << myHostH2dBus 
      myHostD2hBus << io.dev.d2hBus

      //when (
      //  RegNext(myHostH2dBus.valid, init=False)
      //  && myHostH2dBus.ready
      //  && RegNext(myHostH2dBus.burstLast, init=False)
      //) {
      //  rSeenHostH2dFireEtc(2) := True
      //}
      maybeSetSeenHostH2dFireEtc(2)

      when (
        rSeenHostH2dFireEtc(2)
        && RegNext(myHostD2hBus.valid, init=False)
        && myHostD2hBus.ready
        //&& RegNext(myHostD2hBus.burstLast)
      ) {
        rState := State.IDLE
        doIncrCntEtc(Some(2))
      }
    }
  }
}

object LcvBusArbiterTestSpinalConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    )
  )
}
object LcvBusArbiterTestToVerilog extends App {
  LcvBusArbiterTestSpinalConfig.spinal.generateVerilog{
    //val cfg = LcvBusArbiterConfig(
    //  mmapCfg=LcvBusMemMapConfig(
    //    busCfg=LcvBusConfig(
    //      mainCfg=LcvBusMainConfig(
    //        dataWidth=32,
    //        addrWidth=32,
    //        burstAlwaysMaxSize=true,
    //        srcWidth=1,
    //      ),
    //      cacheCfg=None,
    //    ),
    //    addrSliceHi=30,
    //    addrSliceLo=29,
    //    optSliceSize=None,
    //  ),
    //  numHosts=2,
    //)
    val cfg = LcvBusArbiterConfig(
      busCfg=LcvBusConfig(
        mainCfg=LcvBusMainConfig(
          dataWidth=32,
          addrWidth=32,
          burstAlwaysMaxSize=true,
          srcWidth=1,
        ),
        cacheCfg=None,
      ),
      numHosts=2,
    )
    LcvBusArbiter(cfg=cfg)
  }
}
