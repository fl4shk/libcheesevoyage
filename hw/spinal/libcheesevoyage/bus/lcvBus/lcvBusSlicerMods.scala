package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


import libcheesevoyage.math._

case class LcvBusSlicerConfig(
  //busCfg: LcvBusConfig,
  //addrSliceWidth: Int,
  mmapCfg: LcvBusMemMapConfig,
  //addrSliceStart: Int,
  //addrSliceEnd: Int,
  //optNumDevs: Option[Int]=None,
  maxNumOutstandingTxns: Int=8,
) {
  def busCfg = mmapCfg.busCfg
  def numDevs = mmapCfg.addrSliceSize
  def addrSliceStart = mmapCfg.addrSliceLo
  def addrSliceEnd = mmapCfg.addrSliceHi
  def addrSliceRange = mmapCfg.addrSliceRange
}

case class LcvBusSlicerIo(
  cfg: LcvBusSlicerConfig,
) extends Bundle {
  val host = slave(LcvBusIo(cfg=cfg.busCfg))
  val devVec = (
    Vec[LcvBusIo]{
      val tempArr = new ArrayBuffer[LcvBusIo]()
      for (idx <- 0 until cfg.numDevs) {
        tempArr += LcvBusIo(cfg=cfg.busCfg)
      }
      tempArr
    }
  )
  for (dev <- devVec.view) {
    master(dev)
  }
}
private[libcheesevoyage] case class LcvBusSlicerWithoutBursts(
  cfg: LcvBusSlicerConfig,
) extends Component {
  require(
    !cfg.busCfg.allowBurst,
    s"`LcvBusSlicerWithoutBursts` doesn't support bursts."
  )
  val io = LcvBusSlicerIo(cfg=cfg)

  io.host.h2dBus.ready := False
  io.host.d2hBus.valid := False
  io.host.d2hBus.payload := io.host.d2hBus.payload.getZero

  for (dev <- io.devVec) {
    dev.h2dBus.valid := False
    dev.h2dBus.payload := (
      dev.h2dBus.payload.getZero
    )
    dev.d2hBus.ready := False
  }
  val rSavedH2dAddrSlice = (
    Reg(UInt(cfg.mmapCfg.addrSliceWidth bits))
    init(0x0)
  )

  val rTxnCnt = (
    Reg(UInt(log2Up(cfg.maxNumOutstandingTxns + 1) bits))
    init(0x0)
  )

  object State
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      START_NEW_ADDR_SLICE,
      MAIN,
      CHANGED_ADDR_SLICE_WAIT_REMAINING_D2H_RESPONSES
      = newElement();
  }
  val rState = (
    Reg(State())
    init(State.START_NEW_ADDR_SLICE)
  )

  def doConnect(
    whichBusIsH2d: Boolean,
    devIdx: Int,
  ): Unit = {
    //val dev.h2dBus = io.devVec(devIdx).h2dBus
    def dev = io.devVec(devIdx)
    if (whichBusIsH2d) {
      dev.h2dBus << io.host.h2dBus
    } else {
      io.host.d2hBus << dev.d2hBus

      // at this point, we can just check `dev.h2dBus.ready` to
      // determine if an h2d bus request is being sent
      // because we know that `io.host.h2dBus.valid === True`
      // from the outer-`switch`.

      // this math here gets rid of the `switch` statement and produces
      // identical results to the `switch` statement.
      // I am not sure this is actually that great of an option but it
      // might be???
      //rTxnCnt := (
      //    rTxnCnt 
      //    + U(
      //      dev.h2dBus.ready
      //    )
      //    - U(
      //      dev.d2hBus.fire
      //    )
      //  )

      switch (
        //dev.h2dBus.ready
        dev.h2dBus.fire
        ## dev.d2hBus.fire
      ) {
        is (
          //B"10"
          0x2
        ) {
          // dev.h2dBus.fire, !dev.d2hBus.fire
          rTxnCnt := rTxnCnt + 1
        }
        is (
          //B"01"
          0x1
        ) {
          rTxnCnt := rTxnCnt - 1
        }
        default {
        }
      }
    }
  }

  val stickyHostH2dAddrSlice = UInt(cfg.mmapCfg.addrSliceWidth bits)
  stickyHostH2dAddrSlice := (
    RegNext(
      stickyHostH2dAddrSlice,
      init=stickyHostH2dAddrSlice.getZero
    )
  )
  when (io.host.h2dBus.valid) {
    stickyHostH2dAddrSlice := io.host.h2dBus.addr(cfg.addrSliceRange)
  }

  when (
    //rState === State.MAIN
    rState.asBits(1)
    && io.host.h2dBus.valid
    //&& !LcvFastCmpEq(
    //  rSavedH2dAddrSlice,
    //  io.host.h2dBus.addr(cfg.addrSliceRange),
    //)
    && (
      rSavedH2dAddrSlice
      =/= io.host.h2dBus.addr(cfg.addrSliceRange)
      //=/= rPrevHostH2dAddrSlice
    )
  ) {
    rState := State.CHANGED_ADDR_SLICE_WAIT_REMAINING_D2H_RESPONSES
  }


  def outerDoConnect(
    whichBusIsH2d: Boolean
  ): Unit = {
    switch (
      (
        if (whichBusIsH2d) (
          //rState === State.MAIN
          rState.asBits(1)
          && (rSavedH2dAddrSlice === stickyHostH2dAddrSlice)
        ) else (
          //rState === State.MAIN
          rState.asBits(1)
        )
      )
      ## rSavedH2dAddrSlice
    ) {
      for (devIdx <- 0 until cfg.numDevs) {
        is (
          (
            (1 << (rSavedH2dAddrSlice.getWidth + 0))
            | devIdx
          )
        ) {
          doConnect(
            whichBusIsH2d=whichBusIsH2d,
            devIdx=devIdx
          )
        }
      }
      default {
      }
    }
  }
  outerDoConnect(whichBusIsH2d=true)
  outerDoConnect(whichBusIsH2d=false)

  switch (rState) {
    is (State.START_NEW_ADDR_SLICE) {
      when (io.host.h2dBus.valid) {
        rSavedH2dAddrSlice := io.host.h2dBus.addr(cfg.addrSliceRange)
        rState := State.MAIN
      }
    }
    is (State.MAIN) {
      //switch (
      //  rSavedH2dAddrSlice === io.host.h2dBus.addr(cfg.addrSliceRange)
      //) {
      //}
    }
    is (State.CHANGED_ADDR_SLICE_WAIT_REMAINING_D2H_RESPONSES) {
      //def dev = io.devVec(rSavedH2dAddrSlice)
      switch (
        //(rTxnCnt > 0)
        rTxnCnt.orR // same as `rTxn =/= 0`
        //rTxnCnt
        ## rSavedH2dAddrSlice
      ) {
        for (devIdx <- 0 until cfg.numDevs) {
          is (
            //B"1'b1"
            //## U(s"${rSavedH2dAddrSlice.getWidth}'d${devIdx}")
            (1 << rSavedH2dAddrSlice.getWidth)
            | devIdx
          ) {
            def dev = io.devVec(devIdx)
            io.host.d2hBus << dev.d2hBus
            when (dev.d2hBus.fire) {
              rTxnCnt := rTxnCnt - 1
            }
          }
        }
        default {
          rState := State.START_NEW_ADDR_SLICE
        }
      }
      //when (rTxnCnt === 0) {
      //}
    }
  }
  //when (io.host.h2dBus.valid) {
  //  switch (
  //    //io.host.h2dBus.payload.addr(cfg.addrSliceRange)
  //    stickyH2dAddrSlice
  //  ) {
  //    for (devIdx <- 0 until cfg.numDevs) {
  //      is (devIdx) {
  //        //val dev.h2dBus = io.devVec(devIdx).h2dBus
  //        def dev = io.devVec(devIdx)
  //        dev.h2dBus << io.host.h2dBus

  //        //val dev.d2hBus = io.devVec(devIdx).d2hBus
  //        io.host.d2hBus << dev.d2hBus
  //      }
  //    }
  //  }
  //}
}

private[libcheesevoyage] case class LcvBusSlicerAllowBursts(
  cfg: LcvBusSlicerConfig,
) extends Component {
  require(
    cfg.busCfg.allowBurst,
    s"`LcvBusSlicerAllowBursts` requires support for bursts."
  )
  //require(
  //  cfg.maxNumOutstandingTxns
  //  >= cfg.busCfg.maxBurstSizeMinus1 + 1 + 4
  //)
  val io = LcvBusSlicerIo(cfg=cfg)

  io.host.h2dBus.ready := False
  io.host.d2hBus.valid := False
  io.host.d2hBus.payload := io.host.d2hBus.payload.getZero

  for (dev <- io.devVec) {
    dev.h2dBus.valid := False
    dev.h2dBus.payload := (
      dev.h2dBus.payload.getZero
    )
    dev.d2hBus.ready := False
  }
  val rSavedH2dAddrSlice = (
    Reg(UInt(cfg.mmapCfg.addrSliceWidth bits))
    init(0x0)
  )

  val rTxnCnt = (
    Reg(UInt(log2Up(cfg.maxNumOutstandingTxns + 1) bits))
    init(0x0)
  )

  object State
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      START_NEW_ADDR_SLICE_ETC,
      MAIN_NON_BURST,
      MAIN_BURST,
      CHANGED_ADDR_SLICE_ETC_WAIT_REMAINING_D2H_RESPONSES
      = newElement();
  }
  val rState = (
    Reg(State())
    init(State.START_NEW_ADDR_SLICE_ETC)
  )

  //val rSavedInBurst = Reg(Bool(), init=False)

  val stickyHostH2dAddrSlice = UInt(cfg.mmapCfg.addrSliceWidth bits)
  stickyHostH2dAddrSlice := (
    RegNext(
      stickyHostH2dAddrSlice,
      init=stickyHostH2dAddrSlice.getZero
    )
  )
  when (io.host.h2dBus.valid) {
    stickyHostH2dAddrSlice := io.host.h2dBus.addr(cfg.addrSliceRange)
  }

  //val stickyHostH2dBurstFirst = Bool()
  //stickyHostH2dBurstFirst := (
  //  RegNext(
  //    stickyHostH2dBurstFirst,
  //    init=stickyHostH2dBurstFirst.getZero
  //  )
  //)
  //when (io.host.h2dBus.valid) {
  //  stickyHostH2dBurstFirst := io.host.h2dBus.burstFirst
  //}
  val rSavedIsWrite = Reg(Bool())
  val rSeenH2dLastFire = Reg(Bool())
  val rSeenD2hLastFire = Reg(Bool())

  def doConnectNonBurst(
    whichBusIsH2d: Boolean,
    devIdx: Int,
  ): Unit = {
    //val dev.h2dBus = io.devVec(devIdx).h2dBus
    def dev = io.devVec(devIdx)
    if (whichBusIsH2d) {
      dev.h2dBus << io.host.h2dBus
    } else {
      io.host.d2hBus << dev.d2hBus

      // at this point, we can just check `dev.h2dBus.ready` to
      // determine if an h2d bus request is being sent
      // because we know that `io.host.h2dBus.valid === True`
      // from the outer-`switch`.

      // this math here gets rid of the `switch` statement and produces
      // identical results to the `switch` statement.
      // I am not sure this is actually that great of an option but it
      // might be???
      //rTxnCnt := (
      //    rTxnCnt 
      //    + U(
      //      dev.h2dBus.ready
      //    )
      //    - U(
      //      dev.d2hBus.fire
      //    )
      //  )

      switch (
        //dev.h2dBus.ready
        dev.h2dBus.fire
        ## dev.d2hBus.fire
      ) {
        is (
          //B"10"
          0x2
        ) {
          // dev.h2dBus.fire, !dev.d2hBus.fire
          rTxnCnt := rTxnCnt + 1
        }
        is (
          //B"01"
          0x1
        ) {
          rTxnCnt := rTxnCnt - 1
        }
        default {
        }
      }
    }
  }

  def doConnectBurst(
    whichBusIsH2d: Boolean,
    devIdx: Int,
  ): Unit = {
    //val dev.h2dBus = io.devVec(devIdx).h2dBus
    def dev = io.devVec(devIdx)

    //if (whichBusIsH2d) {
    //  dev.h2dBus << io.host.h2dBus
    //} else {
    //  io.host.d2hBus << dev.d2hBus
    //}

    if (whichBusIsH2d) {
      when (
        io.host.h2dBus.fire
        && io.host.h2dBus.burstLast
      ) {
        rSeenH2dLastFire := True
      }
      dev.h2dBus << io.host.h2dBus.haltWhen(rSeenH2dLastFire)
    } else {
      when (
        dev.d2hBus.fire
        && dev.d2hBus.burstLast
      ) {
        rSeenD2hLastFire := True
      }
      io.host.d2hBus << dev.d2hBus.haltWhen(rSeenD2hLastFire)
    }
  }

  when (
    //rState === State.MAIN_NON_BURST
    rState.asBits(1)
    && io.host.h2dBus.valid
    //&& !LcvFastCmpEq(
    //  rSavedH2dAddrSlice,
    //  io.host.h2dBus.addr(cfg.addrSliceRange),
    //)
    && (
      (
        rSavedH2dAddrSlice
        =/= io.host.h2dBus.addr(cfg.addrSliceRange)
        //=/= rPrevHostH2dAddrSlice
      )
      //|| (
      //  rSavedInBurst
      //  //=/= io.host.h2dBus.burstFirst
      //)
    )
  ) {
    rState := State.CHANGED_ADDR_SLICE_ETC_WAIT_REMAINING_D2H_RESPONSES
  }

  when (
    //rState === State.MAIN_BURST
    rState.asBits(2)
    && rSeenH2dLastFire
    && rSeenD2hLastFire
  ) {
    rState := State.START_NEW_ADDR_SLICE_ETC
    rSeenH2dLastFire := False
    rSeenD2hLastFire := False
  }

  def outerDoConnect(
    whichBusIsH2d: Boolean,
    isBurst: Boolean,
  ): Unit = {
    switch ({
      val tempStateBitIdx = (
        if (!isBurst) (1) else (2)
      )
      val myTempStateBit = rState.asBits(tempStateBitIdx)
      //stickyHostH2dBurstFirst
      //## 
      (
        (
          if (whichBusIsH2d) (
            //rState === State.MAIN
            //rState.asBits(1)
            myTempStateBit
            && (rSavedH2dAddrSlice === stickyHostH2dAddrSlice)
          ) else (
            //rState === State.MAIN
            //rState.asBits(1)
            myTempStateBit
          )
        )
        ## rSavedH2dAddrSlice
      )
    }) {
      for (devIdx <- 0 until cfg.numDevs) {
        is (
          (
            //(1 << (rSavedH2dAddrSlice.getWidth + 1))
            //| 
            (1 << (rSavedH2dAddrSlice.getWidth + 0))
            | devIdx
          )
        ) {
          if (!isBurst) {
            doConnectNonBurst(
              whichBusIsH2d=whichBusIsH2d,
              devIdx=devIdx
            )
          } else { // if (isBurst)
            doConnectBurst(
              whichBusIsH2d=whichBusIsH2d,
              devIdx=devIdx
            )
          }
        }
      }
      default {
      }
    }
  }
  outerDoConnect(whichBusIsH2d=true, isBurst=false)
  outerDoConnect(whichBusIsH2d=false, isBurst=false)
  outerDoConnect(whichBusIsH2d=true, isBurst=true)
  outerDoConnect(whichBusIsH2d=false, isBurst=true)

  switch (rState) {
    is (State.START_NEW_ADDR_SLICE_ETC) {
      when (io.host.h2dBus.valid) {
        rSavedH2dAddrSlice := io.host.h2dBus.addr(cfg.addrSliceRange)
      }
      switch (
        io.host.h2dBus.valid
        ## io.host.h2dBus.burstFirst
      ) {
        is (M"10") {
          rState := State.MAIN_NON_BURST
        }
        is (M"11") {
          rState := State.MAIN_BURST
        }
      }
      rSavedIsWrite := io.host.h2dBus.isWrite
      rSeenH2dLastFire := False
      rSeenD2hLastFire := False
    }
    is (State.MAIN_NON_BURST) {
    }
    is (State.MAIN_BURST) {
    }
    is (State.CHANGED_ADDR_SLICE_ETC_WAIT_REMAINING_D2H_RESPONSES) {
      //def dev = io.devVec(rSavedH2dAddrSlice)
      switch (
        //(rTxnCnt > 0)
        rTxnCnt.orR // same as `rTxn =/= 0`
        //rTxnCnt
        ## rSavedH2dAddrSlice
      ) {
        for (devIdx <- 0 until cfg.numDevs) {
          is (
            //B"1'b1"
            //## U(s"${rSavedH2dAddrSlice.getWidth}'d${devIdx}")
            (1 << rSavedH2dAddrSlice.getWidth)
            | devIdx
          ) {
            def dev = io.devVec(devIdx)
            io.host.d2hBus << dev.d2hBus
            when (dev.d2hBus.fire) {
              rTxnCnt := rTxnCnt - 1
            }
          }
        }
        default {
          rState := State.START_NEW_ADDR_SLICE_ETC
        }
      }
      //when (rTxnCnt === 0) {
      //}
    }
  }
  //when (io.host.h2dBus.valid) {
  //  switch (
  //    //io.host.h2dBus.payload.addr(cfg.addrSliceRange)
  //    stickyH2dAddrSlice
  //  ) {
  //    for (devIdx <- 0 until cfg.numDevs) {
  //      is (devIdx) {
  //        //val dev.h2dBus = io.devVec(devIdx).h2dBus
  //        def dev = io.devVec(devIdx)
  //        dev.h2dBus << io.host.h2dBus

  //        //val dev.d2hBus = io.devVec(devIdx).d2hBus
  //        io.host.d2hBus << dev.d2hBus
  //      }
  //    }
  //  }
  //}
}

case class LcvBusSlicer(
  cfg: LcvBusSlicerConfig,
) extends Component {
  //--------
  val io = LcvBusSlicerIo(cfg=cfg)
  //--------
  val noBurstsArea = (
    !cfg.busCfg.allowBurst
  ) generate new Area {
    val impl = LcvBusSlicerWithoutBursts(cfg=cfg)
    io <> impl.io
  }
  val allowBurstsArea = (
    cfg.busCfg.allowBurst
  ) generate new Area {
    val impl = LcvBusSlicerAllowBursts(cfg=cfg)
    io <> impl.io
  }
}

//case class LcvBusSlicer(
//  cfg: LcvBusSlicerConfig,
//) extends Component {
//  val myBusSlicerImpl = LcvBusSlicerImpl(cfg=cfg)
//  val myDeburster = (
//    cfg.busCfg.allowBurst
//  ) generate (LcvBusDeburster(cfg=LcvBusDebursterConfig(
//    loBusCfg=cfg.busCfg
//  )))
//  if (cfg.busCfg.allowBurst) {
//    io.bus <> myDeburster.io.loBus
//    //myDeburster.io.hiBus <> myMemImpl.io.bus
//    myDeburster.io.hiBus.h2dBus.translateInto(
//      myMemImpl.io.bus.h2dBus
//    )(
//      dataAssignment=(
//        outp, inp
//      ) => {
//        outp.mainNonBurstInfo := inp.mainNonBurstInfo
//        outp.mainBurstInfo := outp.mainBurstInfo.getZero
//      }
//    )
//    myMemImpl.io.bus.d2hBus.translateInto(
//      myDeburster.io.hiBus.d2hBus
//    )(
//      dataAssignment=(
//        outp, inp
//      ) => {
//        outp.mainNonBurstInfo := inp.mainNonBurstInfo
//      }
//    )
//  } else {
//    io.bus <> myMemImpl.io.bus
//  }
//}
