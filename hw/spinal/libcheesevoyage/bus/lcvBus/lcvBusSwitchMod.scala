package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


case class LcvBusSwitchConfig(
  //busCfg: LcvBusConfig,
  mmapCfg: LcvBusMemMapConfig,
  numHosts: Int,
  //numDevs: Int,
  //isPriority: Boolean,
) {
  val busCfg = mmapCfg.busCfg
  val numDevs = mmapCfg.addrSliceSize
}

case class LcvBusSwitchIo(
  cfg: LcvBusSwitchConfig
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
  //val dev = (
  //  master(LcvBusIo(cfg=cfg.busCfg))
  //)
  val devVec = (
    Vec[LcvBusIo]{
      val tempArr = new ArrayBuffer[LcvBusIo]()
      for (idx <- 0 until cfg.numDevs) {
        tempArr += LcvBusIo(cfg=cfg.busCfg)
      }
      tempArr
    }
  )
  for (dev <- devVec) {
    master(dev)
  }
}

case class LcvBusSwitch(
  cfg: LcvBusSwitchConfig,
) extends Component {
  // (for now) round robin bus arbiter
  val io = LcvBusSwitchIo(cfg=cfg)

  for (dev <- io.devVec) {
    dev.d2hBus.ready := False
    dev.h2dBus.valid := False
    dev.h2dBus.payload := (
      dev.h2dBus.payload.getZero
    )
  }
  for (
    //hostIdx <- 0 until cfg.numHosts
    host <- io.hostVec
  ) {
    val myHostH2dBus = host.h2dBus
    val myHostD2hBus = host.d2hBus
    myHostD2hBus.valid := False
    myHostD2hBus.payload := (
      myHostD2hBus.payload.getZero
    )
    myHostH2dBus.ready := False
  }

  val rSeenHostH2dFireEtcVec = (
    Vec.fill(cfg.numDevs) {
      Vec.fill(3)(
        Reg(Bool(), init=False)
      )
    }
  )


  object State extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      IDLE,
      NON_BURST,
      READ_BURST,
      WRITE_BURST
      = newElement();
  }
  val rStateVec = (
    Vec.fill(cfg.numDevs)(
      Reg(State())
      init(State.IDLE)
    )
  )
  val rArbitCntVec = (
    Vec.fill(cfg.numDevs)(
      Reg(UInt(log2Up(cfg.numHosts) bits))
      init(0x0)
    )
  )
  for (devIdx <- 0 until cfg.numDevs) {
    val rArbitCnt = rArbitCntVec(devIdx)

    val myHostH2dBus = io.hostVec(rArbitCnt).h2dBus
    val myHostD2hBus = io.hostVec(rArbitCnt).d2hBus

    val rState = rStateVec(devIdx)
    val dev = io.devVec(devIdx)
    val rSeenHostH2dFireEtc = rSeenHostH2dFireEtcVec(devIdx)

    def doIncrCntEtc(
      seenHostH2dFireEtcIdx: Option[Int]
    ): Unit = {
      seenHostH2dFireEtcIdx match {
        case Some(myIdx) => {
          rSeenHostH2dFireEtcVec(devIdx)(myIdx) := False
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

    switch (rState) {
      is (State.IDLE) {
        //rSeenHostH2dFireEtc.foreach(_ := False)
        switch (
          RegNext(
            (
              myHostH2dBus.valid
              && myHostH2dBus.addr(cfg.mmapCfg.addrSliceRange) === devIdx
            )
            ## myHostH2dBus.burstFirst
            ## myHostH2dBus.isWrite
          )
          init(0x0)
        ) {
          is (M"10-") {
            // either read or write, but *NOT* a burst
            rState := State.NON_BURST
            dev.h2dBus << myHostH2dBus 
            myHostD2hBus << dev.d2hBus
          }
          is (M"110") {
            // read burst
            rState := State.READ_BURST
            dev.h2dBus << myHostH2dBus 
            myHostD2hBus << dev.d2hBus
          }
          is (M"111") {
            // write burst
            rState := State.WRITE_BURST
            dev.h2dBus << myHostH2dBus 
            myHostD2hBus << dev.d2hBus
          }
          default {
            // the current host is not requesting a transaction
            doIncrCntEtc(None)
          }
        }
      }
      is (State.NON_BURST) {
        dev.h2dBus << myHostH2dBus 
        myHostD2hBus << dev.d2hBus
        when (
          RegNext(myHostH2dBus.valid, init=False)
          && myHostH2dBus.ready
        ) {
          rSeenHostH2dFireEtc(0) := True
        }
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
        dev.h2dBus << myHostH2dBus 
        myHostD2hBus << dev.d2hBus
        when (
          RegNext(myHostH2dBus.valid, init=False)
          && myHostH2dBus.ready
        ) {
          rSeenHostH2dFireEtc(1) := True
        }
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
        dev.h2dBus << myHostH2dBus 
        myHostD2hBus << dev.d2hBus
        when (
          RegNext(myHostH2dBus.valid, init=False)
          && myHostH2dBus.ready
          && RegNext(myHostH2dBus.burstLast, init=False)
        ) {
          rSeenHostH2dFireEtc(2) := True
        }

        when (
          rSeenHostH2dFireEtc(2)
          && RegNext(myHostD2hBus.valid, init=False)
          && myHostD2hBus.ready
        ) {
          rState := State.IDLE
          doIncrCntEtc(Some(2))
        }
      }
    }
  }
}

object LcvBusSwitchTestSpinalConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    )
  )
}
object LcvBusSwitchTestToVerilog extends App {
  LcvBusSwitchTestSpinalConfig.spinal.generateVerilog{
    //val cfg = LcvBusSwitchConfig(
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
    val cfg = LcvBusSwitchConfig(
      mmapCfg=LcvBusMemMapConfig(
        busCfg=LcvBusConfig(
          mainCfg=LcvBusMainConfig(
            dataWidth=32,
            addrWidth=32,
            burstAlwaysMaxSize=true,
            srcWidth=1,
          ),
          cacheCfg=None,
        ),
        addrSliceHi=24,
        addrSliceLo=23,
      ),
      numHosts=2,
    )
    LcvBusSwitch(cfg=cfg)
  }
}
