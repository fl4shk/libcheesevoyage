package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


case class LcvBusXbarConfig(
  //busCfg: LcvBusConfig,
  mmapCfg: LcvBusMemMapConfig,
  numHosts: Int,
  //numDevs: Int,
  //isPriority: Boolean,
) {
  val busCfg = mmapCfg.busCfg
  val numDevs = mmapCfg.addrSliceSize
  if (busCfg.cacheCfg != None) {
    require(
      numHosts == busCfg.cacheCfg.get.numCpus
    )
  }
}

case class LcvBusXbarIo(
  cfg: LcvBusXbarConfig
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

case class LcvBusXbarCoherent(
  cfg: LcvBusXbarConfig,
) extends Component {
  //--------
  // round robin bus multi-arbiter, cache-coherent
  //--------
  def busCfg = cfg.busCfg
  def cacheCfg = cfg.busCfg.cacheCfg
  require(
    cacheCfg != None
    && cacheCfg.get.coherent
    && cacheCfg.get.kind == LcvCacheKind.Shared
  )
  //--------
  val io = LcvBusXbarIo(cfg=cfg)
  //--------
  for (dev <- io.devVec) {
    dev.d2hBus.ready := False
    dev.h2dBus.valid := False
    dev.h2dBus.payload := dev.h2dBus.payload.getZero
  }
  for (
    //hostIdx <- 0 until cfg.numHosts
    host <- io.hostVec
  ) {
    //val host.h2dBus = host.h2dBus
    //val myHostD2hBus = host.d2hBus
    host.d2hBus.valid := False
    host.d2hBus.payload := host.d2hBus.payload.getZero
    host.h2dBus.ready := False
  }
  //--------
  object State extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      IDLE,
      //NON_BURST,
      //READ_BURST,
      //WRITE_BURST
      ACTIVE
      = newElement();
  }
  val rStateVec = (
    Vec.fill(cfg.numDevs)(
      Reg(State())
      init(State.IDLE)
    )
  )

  val rSeenHostH2dCacheLastVec = (
    Vec.fill(cfg.numDevs) {
      Reg(Bool(), init=False)
    }
  )

  val rArbitCntVec = (
    Vec.fill(cfg.numDevs)(
      Reg(UInt(log2Up(cfg.numHosts) bits))
      init(0x0)
    )
  )
  for (devIdx <- 0 until cfg.numDevs) {
    val rArbitCnt = rArbitCntVec(devIdx)

    //val host.h2dBus = io.hostVec(rArbitCnt).h2dBus
    //val host.d2hBus = io.hostVec(rArbitCnt).d2hBus
    def host = io.hostVec(rArbitCnt)

    val rState = rStateVec(devIdx)
    val dev = io.devVec(devIdx)
    val rSeenHostH2dCacheLast = rSeenHostH2dCacheLastVec(devIdx)

    def doIncrCntEtc(
      doDeassertSeenHostH2dCacheLast: Boolean,
    ): Unit = {
      if (doDeassertSeenHostH2dCacheLast) {
        rSeenHostH2dCacheLastVec(devIdx) := False
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
    val mySeenHostH2dCacheLast = (
      //RegNext(host.h2dBus.valid, init=False)
      //&& host.h2dBus.ready
      host.h2dBus.fire
      //&& RegNext(host.h2dBus.cacheLast, init=False)
      && host.h2dBus.cacheLast
    )

    switch (rState) {
      is (State.IDLE) {
        when (
          //RegNext(
          //  next=(
              host.h2dBus.valid
              && host.h2dBus.addr(cfg.mmapCfg.addrSliceRange) === devIdx
              //&& host.h2dBus.cacheFirst
          //  ),
          //  init=False
          //)
        ) {
          dev.h2dBus << host.h2dBus 
          host.d2hBus << dev.d2hBus

          rState := State.ACTIVE

          when (mySeenHostH2dCacheLast) {
            rSeenHostH2dCacheLast := True
          }
        } otherwise {
          // the current host is not requesting a transaction
          doIncrCntEtc(false)
        }
      }
      is (State.ACTIVE) {
        dev.h2dBus << host.h2dBus 
        host.d2hBus << dev.d2hBus

        when (mySeenHostH2dCacheLast) {
          rSeenHostH2dCacheLast := True
        }

        when (
          (
            mySeenHostH2dCacheLast
            || rSeenHostH2dCacheLast
          )
          && host.d2hBus.fire
          //&& RegNext(host.d2hBus.valid, init=False)
          //&& host.d2hBus.ready
        ) {
          rState := State.IDLE
          doIncrCntEtc(true)
        }
      }
    }
  }
}

case class LcvBusXbarNonCoherent(
  cfg: LcvBusXbarConfig,
) extends Component {
  //--------
  // round robin bus multi-arbiter, non-cache-coherent
  //--------
  def busCfg = cfg.busCfg
  def cacheCfg = cfg.busCfg.cacheCfg
  require(
    cfg.busCfg.cacheCfg == None
  )
  //--------
  val io = LcvBusXbarIo(cfg=cfg)

  for (dev <- io.devVec) {
    dev.d2hBus.ready := False
    dev.h2dBus.valid := False
    dev.h2dBus.payload := dev.h2dBus.payload.getZero
  }
  for (
    //hostIdx <- 0 until cfg.numHosts
    host <- io.hostVec
  ) {
    //val host.h2dBus = host.h2dBus
    //val host.d2hBus = host.d2hBus
    host.d2hBus.valid := False
    host.d2hBus.payload := host.d2hBus.payload.getZero
    host.h2dBus.ready := False
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

    //val host.h2dBus = io.hostVec(rArbitCnt).h2dBus
    //val host.d2hBus = io.hostVec(rArbitCnt).d2hBus
    def host = io.hostVec(rArbitCnt)

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
    def maybeSetSeenHostH2dFireEtc(
      idx: Int
    ): Unit = {
      if (idx == 0) {
        when (
          RegNext(host.h2dBus.valid, init=False)
          && host.h2dBus.ready
        ) {
          rSeenHostH2dFireEtc(idx) := True
        }
      } else if (idx == 1) {
        when (
          RegNext(host.h2dBus.valid, init=False)
          && host.h2dBus.ready
        ) {
          rSeenHostH2dFireEtc(idx) := True
        }
      } else if (idx == 2) {
        when (
          RegNext(host.h2dBus.valid, init=False)
          && host.h2dBus.ready
          && RegNext(host.h2dBus.burstLast, init=False)
        ) {
          rSeenHostH2dFireEtc(idx) := True
        }
      } else {
        require(false)
      }
    }

    switch (rState) {
      is (State.IDLE) {
        //rSeenHostH2dFireEtc.foreach(_ := False)
        switch (
          RegNext(
            (
              host.h2dBus.valid
              && host.h2dBus.addr(cfg.mmapCfg.addrSliceRange) === devIdx
            )
            ## host.h2dBus.burstFirst
            ## host.h2dBus.isWrite
          )
          init(0x0)
        ) {
          is (M"10-") {
            // either read or write, but *NOT* a burst
            rState := State.NON_BURST
            dev.h2dBus << host.h2dBus 
            host.d2hBus << dev.d2hBus
            maybeSetSeenHostH2dFireEtc(0)
          }
          is (M"110") {
            // read burst
            rState := State.READ_BURST
            dev.h2dBus << host.h2dBus 
            host.d2hBus << dev.d2hBus

            maybeSetSeenHostH2dFireEtc(1)
          }
          is (M"111") {
            // write burst
            rState := State.WRITE_BURST
            dev.h2dBus << host.h2dBus 
            host.d2hBus << dev.d2hBus

            maybeSetSeenHostH2dFireEtc(2)
          }
          default {
            // the current host is not requesting a transaction
            doIncrCntEtc(None)
          }
        }
      }
      is (State.NON_BURST) {
        dev.h2dBus << host.h2dBus 
        host.d2hBus << dev.d2hBus

        //when (
        //  RegNext(host.h2dBus.valid, init=False)
        //  && host.h2dBus.ready
        //) {
        //  rSeenHostH2dFireEtc(0) := True
        //}
        maybeSetSeenHostH2dFireEtc(0)

        when (
          rSeenHostH2dFireEtc(0)
          && RegNext(host.d2hBus.valid, init=False)
          && host.d2hBus.ready
        ) {
          rState := State.IDLE
          doIncrCntEtc(Some(0))
        }
      }
      is (State.READ_BURST) {
        dev.h2dBus << host.h2dBus 
        host.d2hBus << dev.d2hBus

        //when (
        //  RegNext(host.h2dBus.valid, init=False)
        //  && host.h2dBus.ready
        //) {
        //  rSeenHostH2dFireEtc(1) := True
        //}
        maybeSetSeenHostH2dFireEtc(1)

        when (
          rSeenHostH2dFireEtc(1)
          && RegNext(host.d2hBus.valid, init=False)
          && host.d2hBus.ready
          && RegNext(host.d2hBus.burstLast)
        ) {
          rState := State.IDLE
          doIncrCntEtc(Some(1))
        }
      }
      is (State.WRITE_BURST) {
        dev.h2dBus << host.h2dBus 
        host.d2hBus << dev.d2hBus

        //when (
        //  RegNext(host.h2dBus.valid, init=False)
        //  && host.h2dBus.ready
        //  && RegNext(host.h2dBus.burstLast, init=False)
        //) {
        //  rSeenHostH2dFireEtc(2) := True
        //}
        maybeSetSeenHostH2dFireEtc(2)

        when (
          rSeenHostH2dFireEtc(2)
          && RegNext(host.d2hBus.valid, init=False)
          && host.d2hBus.ready
        ) {
          rState := State.IDLE
          doIncrCntEtc(Some(2))
        }
      }
    }
  }
}

object LcvBusXbarTestSpinalConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    )
  )
}
object LcvBusXbarCoherentTestToVerilog extends App {
  LcvBusXbarTestSpinalConfig.spinal.generateVerilog{
    //val cfg = LcvBusXbarConfig(
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
    val cfg = LcvBusXbarConfig(
      mmapCfg=LcvBusMemMapConfig(
        busCfg=LcvBusConfig(
          mainCfg=LcvBusMainConfig(
            dataWidth=32,
            addrWidth=32,
            allowBurst=true,
            burstAlwaysMaxSize=true,
            srcWidth=1,
          ),
          cacheCfg=Some(LcvBusCacheConfig(
            kind=LcvCacheKind.Shared,
            lineSizeBytes=64,
            depthWords=1024,
            numCpus=2,
          )),
        ),
        addrSliceHi=24,
        addrSliceLo=23,
      ),
      numHosts=2,
    )
    LcvBusXbarCoherent(cfg=cfg)
  }
}
object LcvBusXbarNonCoherentTestToVerilog extends App {
  LcvBusXbarTestSpinalConfig.spinal.generateVerilog{
    //val cfg = LcvBusXbarConfig(
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
    val cfg = LcvBusXbarConfig(
      mmapCfg=LcvBusMemMapConfig(
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
        addrSliceHi=24,
        addrSliceLo=23,
      ),
      numHosts=2,
    )
    LcvBusXbarNonCoherent(cfg=cfg)
  }
}
