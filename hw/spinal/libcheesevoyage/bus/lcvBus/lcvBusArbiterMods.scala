package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

sealed trait LcvBusArbiterKind
object LcvBusArbiterKind {
  case object Priority extends LcvBusArbiterKind
  case object RoundRobin extends LcvBusArbiterKind
}

case class LcvBusArbiterConfig(
  busCfg: LcvBusConfig,
  //mmapCfg: LcvBusMemMapConfig,
  numHosts: Int,
  //isPriority: Boolean,
  kind: LcvBusArbiterKind,
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
  cfg: LcvBusArbiterConfig
) extends Component {
  //--------
  //require(
  //  cfg.kind match {
  //    case LcvBusArbiterKind.Priority => true
  //    case _ => false
  //  }
  //)
  //--------
  // priority (bitscan) bus arbiter
  val io = LcvBusArbiterIo(cfg=cfg)
  //--------
  io.dev.d2hBus.ready := False
  io.dev.h2dBus.valid := False
  io.dev.h2dBus.payload := (
    io.dev.h2dBus.payload.getZero
  )
  for (
    //hostIdx <- 0 until cfg.numHosts
    host <- io.hostVec
  ) {
    //val host.h2dBus = io.hostVec(hostIdx).h2dBus
    //val host.d2hBus = io.hostVec(hostIdx).d2hBus
    host.d2hBus.valid := False
    host.d2hBus.payload := (
      host.d2hBus.payload.getZero
    )
    host.h2dBus.ready := False
  }
  //--------
  //val rArbitCnt = (
  //  Reg(UInt(log2Up(cfg.numHosts) bits))
  //  init(0x0)
  //)
  //val host.h2dBus = io.hostVec(rArbitCnt).h2dBus
  //val host.d2hBus = io.hostVec(rArbitCnt).d2hBus
  val myCurrHostValidVec = (
    cfg.kind == LcvBusArbiterKind.Priority
  ) generate (
    Vec.fill(cfg.numHosts)(
      Bool()
    )
  )
  val myPriorityVec = (
    cfg.kind == LcvBusArbiterKind.Priority
  ) generate (
    Vec.fill(cfg.numHosts)(
      Bool()
    )
  )
  if (cfg.kind == LcvBusArbiterKind.Priority) {
    for (hostIdx <- 0 until io.hostVec.size) {
      myCurrHostValidVec(hostIdx) := io.hostVec(hostIdx).h2dBus.valid
    }
    myPriorityVec.assignFromBits(
      // this is a bitscan arbiter, which acts as a priority arbiter
      (
        myCurrHostValidVec.asBits.asUInt
        & ~(myCurrHostValidVec.asBits.asUInt - 1)
      ).asBits
    )
  }

  val nextHostIdx = UInt(log2Up(cfg.numHosts) bits)
  val rHostIdx = (
    RegNext(nextHostIdx, init=nextHostIdx.getZero)
  )
  nextHostIdx := rHostIdx


  val rHostIdxValid = (
    cfg.kind == LcvBusArbiterKind.Priority
  ) generate (
    Reg(Bool(), init=False)
  )
  def host = io.hostVec(rHostIdx)

  //def prevHost = io.hostVec(RegNext(rArbitCnt, init=rArbitCnt.getZero))

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
        //RegNext(host.h2dBus.valid, init=False)
        host.h2dBus.valid
        && host.h2dBus.ready
      ) {
        rSeenHostH2dFireEtc(idx) := True
      }
    } else if (idx == 1) {
      when (
        //RegNext(host.h2dBus.valid, init=False)
        host.h2dBus.valid
        && host.h2dBus.ready
      ) {
        rSeenHostH2dFireEtc(idx) := True
      }
    } else if (idx == 2) {
      when (
        //RegNext(host.h2dBus.valid, init=False)
        host.h2dBus.valid
        && host.h2dBus.ready
        //&& RegNext(host.h2dBus.burstLast, init=False)
        && host.h2dBus.burstLast
      ) {
        rSeenHostH2dFireEtc(idx) := True
      }
    } else {
      require(false)
    }
  }

  def doCalcHostIdx(seenHostH2dFireEtcIdx: Option[Int]): Unit = {
    seenHostH2dFireEtcIdx match {
      case Some(myIdx) => {
        rSeenHostH2dFireEtc(myIdx) := False
      }
      case None => {
      }
    }

    cfg.kind match {
      case LcvBusArbiterKind.Priority => {
        nextHostIdx := (
          myPriorityVec.sFindFirst(_ === True)._2
        )
      }
      case LcvBusArbiterKind.RoundRobin => {
        if ((1 << log2Up(cfg.numHosts)) == cfg.numHosts) {
          nextHostIdx := rHostIdx + 1
        } else {
          when (rHostIdx + 1 < cfg.numHosts) {
            nextHostIdx := rHostIdx + 1
          } otherwise {
            nextHostIdx := 0x0
          }
        }
      }
    }
  }

  object AllowBurstState
  extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      IDLE,
      NON_BURST,
      READ_BURST,
      WRITE_BURST
      = newElement();
  }
  val myAllowBurstArea = (
    cfg.busCfg.allowBurst
  ) generate (new Area {
    val rAllowBurstState = (
      Reg(AllowBurstState())
      init(AllowBurstState.IDLE)
    )
    switch (rAllowBurstState) {
      is (AllowBurstState.IDLE) {
        //rSeenHostH2dFireEtc.foreach(_ := False)
        switch (
          //RegNext(
            host.h2dBus.valid
            ## host.h2dBus.burstFirst
            ## host.h2dBus.isWrite
          //)
          //init(0x0)
        ) {
          is (M"10-") {
            // either read or write, but *NOT* a burst
            rAllowBurstState := AllowBurstState.NON_BURST
            //io.dev.h2dBus << host.h2dBus 
            ////host.d2hBus << io.dev.d2hBus
            //maybeSetSeenHostH2dFireEtc(0)
          }
          is (M"110") {
            // read burst
            rAllowBurstState := AllowBurstState.READ_BURST
            //io.dev.h2dBus << host.h2dBus 
            ////host.d2hBus << io.dev.d2hBus
            //maybeSetSeenHostH2dFireEtc(1)
          }
          is (M"111") {
            // write burst
            rAllowBurstState := AllowBurstState.WRITE_BURST
            //io.dev.h2dBus << host.h2dBus 
            ////host.d2hBus << io.dev.d2hBus
            //maybeSetSeenHostH2dFireEtc(2)
          }
          default {
            // the current host is not requesting a transaction
            doCalcHostIdx(None)
          }
        }
      }
      is (AllowBurstState.NON_BURST) {
        when (!rSeenHostH2dFireEtc(0)) {
          io.dev.h2dBus << host.h2dBus 
          maybeSetSeenHostH2dFireEtc(0)
        }
        host.d2hBus << io.dev.d2hBus
        //when (
        //  RegNext(host.h2dBus.valid, init=False)
        //  && host.h2dBus.ready
        //) {
        //  rSeenHostH2dFireEtc(0) := True
        //}
        when (
          rSeenHostH2dFireEtc(0)
          //&& RegNext(host.d2hBus.valid, init=False)
          && host.d2hBus.valid
          && host.d2hBus.ready
        ) {
          rAllowBurstState := AllowBurstState.IDLE
          doCalcHostIdx(Some(0))
        }
      }
      is (AllowBurstState.READ_BURST) {
        when (!rSeenHostH2dFireEtc(1)) {
          io.dev.h2dBus << host.h2dBus 
          maybeSetSeenHostH2dFireEtc(1)
        }
        host.d2hBus << io.dev.d2hBus

        //when (
        //  RegNext(host.h2dBus.valid, init=False)
        //  && host.h2dBus.ready
        //) {
        //  rSeenHostH2dFireEtc(1) := True
        //}

        when (
          rSeenHostH2dFireEtc(1)
          //&& RegNext(host.d2hBus.valid, init=False)
          && host.d2hBus.valid
          && host.d2hBus.ready
          //&& RegNext(host.d2hBus.burstLast, init=False)
          && host.d2hBus.burstLast
        ) {
          rAllowBurstState := AllowBurstState.IDLE
          doCalcHostIdx(Some(1))
        }
      }
      is (AllowBurstState.WRITE_BURST) {
        when (!rSeenHostH2dFireEtc(2)) {
          io.dev.h2dBus << host.h2dBus 
          maybeSetSeenHostH2dFireEtc(2)
        }
        host.d2hBus << io.dev.d2hBus

        //when (
        //  RegNext(host.h2dBus.valid, init=False)
        //  && host.h2dBus.ready
        //  && RegNext(host.h2dBus.burstLast, init=False)
        //) {
        //  rSeenHostH2dFireEtc(2) := True
        //}

        when (
          rSeenHostH2dFireEtc(2)
          //&& RegNext(host.d2hBus.valid, init=False)
          && host.d2hBus.valid
          && host.d2hBus.ready
          //&& RegNext(host.d2hBus.burstLast, init=False)
        ) {
          rAllowBurstState := AllowBurstState.IDLE
          doCalcHostIdx(Some(2))
        }
      }
    }
  })
  val myNoBurstsArea = (
    !cfg.busCfg.allowBurst
  ) generate (new Area {
    switch (
      Cat(host.h2dBus.valid).asBits
      //## host.h2dBus.isWrite
    ) {
      is (
        //M"10"
        M"1"
      ) {
        when (!rSeenHostH2dFireEtc(0)) {
          io.dev.h2dBus << host.h2dBus 
          maybeSetSeenHostH2dFireEtc(0)
        }
        host.d2hBus << io.dev.d2hBus
        //when (
        //  RegNext(host.h2dBus.valid, init=False)
        //  && host.h2dBus.ready
        //) {
        //  rSeenHostH2dFireEtc(0) := True
        //}
        when (
          rSeenHostH2dFireEtc(0)
          //&& RegNext(host.d2hBus.valid, init=False)
          && host.d2hBus.valid
          && host.d2hBus.ready
        ) {
          //rAllowBurstState := AllowBurstState.IDLE
          doCalcHostIdx(Some(0))
        }
      }
      //is (M"11") {
      //}
      default {
        // the current host is not requesting a transaction
        doCalcHostIdx(None)
      }
    }
  })
}

//private[libcheesevoyage] case class LcvBusArbiterRoundRobin(
//  cfg: LcvBusArbiterConfig
//) extends Component {
//  // round robin bus arbiter
//  require(
//    cfg.kind match {
//      case LcvBusArbiterKind.RoundRobin(_) => true
//      case _ => false
//    }
//  )
//
//  val io = LcvBusArbiterIo(cfg=cfg)
//
//  io.dev.d2hBus.ready := False
//  io.dev.h2dBus.valid := False
//  io.dev.h2dBus.payload := (
//    io.dev.h2dBus.payload.getZero
//  )
//  for (
//    //hostIdx <- 0 until cfg.numHosts
//    host <- io.hostVec
//  ) {
//    //val host.h2dBus = io.hostVec(hostIdx).h2dBus
//    //val host.d2hBus = io.hostVec(hostIdx).d2hBus
//    host.d2hBus.valid := False
//    host.d2hBus.payload := (
//      host.d2hBus.payload.getZero
//    )
//    host.h2dBus.ready := False
//  }
//
//  val rArbitCnt = (
//    Reg(UInt(log2Up(cfg.numHosts) bits))
//    init(0x0)
//  )
//  //val host.h2dBus = io.hostVec(rArbitCnt).h2dBus
//  //val host.d2hBus = io.hostVec(rArbitCnt).d2hBus
//  def host = io.hostVec(rArbitCnt)
//  //def prevHost = io.hostVec(RegNext(rArbitCnt, init=rArbitCnt.getZero))
//
//  val rSeenHostH2dFireEtc = (
//    Vec.fill(3)(
//      Reg(Bool(), init=False)
//    )
//  )
//  def maybeSetSeenHostH2dFireEtc(
//    idx: Int
//  ): Unit = {
//    if (idx == 0) {
//      when (
//        //RegNext(host.h2dBus.valid, init=False)
//        host.h2dBus.valid
//        && host.h2dBus.ready
//      ) {
//        rSeenHostH2dFireEtc(idx) := True
//      }
//    } else if (idx == 1) {
//      when (
//        //RegNext(host.h2dBus.valid, init=False)
//        host.h2dBus.valid
//        && host.h2dBus.ready
//      ) {
//        rSeenHostH2dFireEtc(idx) := True
//      }
//    } else if (idx == 2) {
//      when (
//        //RegNext(host.h2dBus.valid, init=False)
//        host.h2dBus.valid
//        && host.h2dBus.ready
//        //&& RegNext(host.h2dBus.burstLast, init=False)
//        && host.h2dBus.burstLast
//      ) {
//        rSeenHostH2dFireEtc(idx) := True
//      }
//    } else {
//      require(false)
//    }
//  }
//
//  def doIncrCntEtc(seenHostH2dFireEtcIdx: Option[Int]): Unit = {
//    seenHostH2dFireEtcIdx match {
//      case Some(myIdx) => {
//        rSeenHostH2dFireEtc(myIdx) := False
//      }
//      case None => {
//      }
//    }
//    if ((1 << log2Up(cfg.numHosts)) == cfg.numHosts) {
//      rArbitCnt := rArbitCnt + 1
//    } else {
//      when (rArbitCnt + 1 < cfg.numHosts) {
//        rArbitCnt := rArbitCnt + 1
//      } otherwise {
//        rArbitCnt := 0x0
//      }
//    }
//  }
//
//  object AllowBurstState
//  extends SpinalEnum(defaultEncoding=binarySequential) {
//    val
//      IDLE,
//      NON_BURST,
//      READ_BURST,
//      WRITE_BURST
//      = newElement();
//  }
//  val myAllowBurstArea = (
//    cfg.busCfg.allowBurst
//  ) generate (new Area {
//    val rAllowBurstState = (
//      Reg(AllowBurstState())
//      init(AllowBurstState.IDLE)
//    )
//    switch (rAllowBurstState) {
//      is (AllowBurstState.IDLE) {
//        //rSeenHostH2dFireEtc.foreach(_ := False)
//        switch (
//          //RegNext(
//            host.h2dBus.valid
//            ## host.h2dBus.burstFirst
//            ## host.h2dBus.isWrite
//          //)
//          //init(0x0)
//        ) {
//          is (M"10-") {
//            // either read or write, but *NOT* a burst
//            rAllowBurstState := AllowBurstState.NON_BURST
//            //io.dev.h2dBus << host.h2dBus 
//            ////host.d2hBus << io.dev.d2hBus
//            //maybeSetSeenHostH2dFireEtc(0)
//          }
//          is (M"110") {
//            // read burst
//            rAllowBurstState := AllowBurstState.READ_BURST
//            //io.dev.h2dBus << host.h2dBus 
//            ////host.d2hBus << io.dev.d2hBus
//            //maybeSetSeenHostH2dFireEtc(1)
//          }
//          is (M"111") {
//            // write burst
//            rAllowBurstState := AllowBurstState.WRITE_BURST
//            //io.dev.h2dBus << host.h2dBus 
//            ////host.d2hBus << io.dev.d2hBus
//            //maybeSetSeenHostH2dFireEtc(2)
//          }
//          default {
//            // the current host is not requesting a transaction
//            doIncrCntEtc(None)
//          }
//        }
//      }
//      is (AllowBurstState.NON_BURST) {
//        when (!rSeenHostH2dFireEtc(0)) {
//          io.dev.h2dBus << host.h2dBus 
//          maybeSetSeenHostH2dFireEtc(0)
//        }
//        host.d2hBus << io.dev.d2hBus
//        //when (
//        //  RegNext(host.h2dBus.valid, init=False)
//        //  && host.h2dBus.ready
//        //) {
//        //  rSeenHostH2dFireEtc(0) := True
//        //}
//        when (
//          rSeenHostH2dFireEtc(0)
//          //&& RegNext(host.d2hBus.valid, init=False)
//          && host.d2hBus.valid
//          && host.d2hBus.ready
//        ) {
//          rAllowBurstState := AllowBurstState.IDLE
//          doIncrCntEtc(Some(0))
//        }
//      }
//      is (AllowBurstState.READ_BURST) {
//        when (!rSeenHostH2dFireEtc(1)) {
//          io.dev.h2dBus << host.h2dBus 
//          maybeSetSeenHostH2dFireEtc(1)
//        }
//        host.d2hBus << io.dev.d2hBus
//
//        //when (
//        //  RegNext(host.h2dBus.valid, init=False)
//        //  && host.h2dBus.ready
//        //) {
//        //  rSeenHostH2dFireEtc(1) := True
//        //}
//
//        when (
//          rSeenHostH2dFireEtc(1)
//          //&& RegNext(host.d2hBus.valid, init=False)
//          && host.d2hBus.valid
//          && host.d2hBus.ready
//          //&& RegNext(host.d2hBus.burstLast, init=False)
//          && host.d2hBus.burstLast
//        ) {
//          rAllowBurstState := AllowBurstState.IDLE
//          doIncrCntEtc(Some(1))
//        }
//      }
//      is (AllowBurstState.WRITE_BURST) {
//        when (!rSeenHostH2dFireEtc(2)) {
//          io.dev.h2dBus << host.h2dBus 
//          maybeSetSeenHostH2dFireEtc(2)
//        }
//        host.d2hBus << io.dev.d2hBus
//
//        //when (
//        //  RegNext(host.h2dBus.valid, init=False)
//        //  && host.h2dBus.ready
//        //  && RegNext(host.h2dBus.burstLast, init=False)
//        //) {
//        //  rSeenHostH2dFireEtc(2) := True
//        //}
//
//        when (
//          rSeenHostH2dFireEtc(2)
//          //&& RegNext(host.d2hBus.valid, init=False)
//          && host.d2hBus.valid
//          && host.d2hBus.ready
//          //&& RegNext(host.d2hBus.burstLast, init=False)
//        ) {
//          rAllowBurstState := AllowBurstState.IDLE
//          doIncrCntEtc(Some(2))
//        }
//      }
//    }
//  })
//  val myNoBurstsArea = (
//    !cfg.busCfg.allowBurst
//  ) generate (new Area {
//    switch (
//      Cat(host.h2dBus.valid).asBits
//      //## host.h2dBus.isWrite
//    ) {
//      is (
//        //M"10"
//        M"1"
//      ) {
//        when (!rSeenHostH2dFireEtc(0)) {
//          io.dev.h2dBus << host.h2dBus 
//          maybeSetSeenHostH2dFireEtc(0)
//        }
//        host.d2hBus << io.dev.d2hBus
//        //when (
//        //  RegNext(host.h2dBus.valid, init=False)
//        //  && host.h2dBus.ready
//        //) {
//        //  rSeenHostH2dFireEtc(0) := True
//        //}
//        when (
//          rSeenHostH2dFireEtc(0)
//          //&& RegNext(host.d2hBus.valid, init=False)
//          && host.d2hBus.valid
//          && host.d2hBus.ready
//        ) {
//          //rAllowBurstState := AllowBurstState.IDLE
//          doIncrCntEtc(Some(0))
//        }
//      }
//      //is (M"11") {
//      //}
//      default {
//        // the current host is not requesting a transaction
//        doIncrCntEtc(None)
//      }
//    }
//  })
//}

//case class LcvBusArbiter(
//  cfg: LcvBusArbiterConfig,
//) extends Component {
//  val io = LcvBusArbiterIo(cfg=cfg)
//  val myRoundRobinArea = (
//    cfg.kind match {
//      case LcvBusArbiterKind.RoundRobin(_) => true
//      case _ => false
//    }
//  ) generate (new Area {
//    val myArbiter = LcvBusArbiterRoundRobin(cfg=cfg)
//    myArbiter.io <> io
//  })
//  val myPriorityArea = (
//    cfg.kind match {
//      case LcvBusArbiterKind.Priority => true
//      case _ => false
//    }
//  ) generate (new Area {
//    val myArbiter = LcvBusArbiterPriority(cfg=cfg)
//    myArbiter.io <> io
//  })
//}

object LcvBusArbiterTestSpinalConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    )
  )
}
//object LcvBusArbiterTestToVerilog extends App {
//  LcvBusArbiterTestSpinalConfig.spinal.generateVerilog{
//    //val cfg = LcvBusArbiterConfig(
//    //  mmapCfg=LcvBusMemMapConfig(
//    //    busCfg=LcvBusConfig(
//    //      mainCfg=LcvBusMainConfig(
//    //        dataWidth=32,
//    //        addrWidth=32,
//    //        burstAlwaysMaxSize=true,
//    //        srcWidth=1,
//    //      ),
//    //      cacheCfg=None,
//    //    ),
//    //    addrSliceHi=30,
//    //    addrSliceLo=29,
//    //    optSliceSize=None,
//    //  ),
//    //  numHosts=2,
//    //)
//    val cfg = LcvBusArbiterConfig(
//      busCfg=LcvBusConfig(
//        mainCfg=LcvBusMainConfig(
//          dataWidth=32,
//          addrWidth=32,
//          allowBurst=true,
//          burstAlwaysMaxSize=true,
//          srcWidth=1,
//        ),
//        cacheCfg=None,
//      ),
//      numHosts=2,
//    )
//    LcvBusArbiter(cfg=cfg)
//  }
//}
