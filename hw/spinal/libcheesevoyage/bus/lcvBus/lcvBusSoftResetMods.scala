package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

case class LcvBusDevSoftResetConfig(
  busCfg: LcvBusConfig,
) {
}

case class LcvBusDevSoftResetIo(
  cfg: LcvBusDevSoftResetConfig
) extends Bundle {
  val softReset = in(Bool())

  val host = slave(LcvBusIo(
    cfg=cfg.busCfg
  ))
  val dev = master(LcvBusIo(
    cfg=cfg.busCfg
  ))
}

case class LcvBusDevSoftReset(
  cfg: LcvBusDevSoftResetConfig
) extends Component {
  //require(
  //  cfg.busCfg.allowBurst
  //)
  //--------
  val io = LcvBusDevSoftResetIo(cfg=cfg)
  //--------
  def doBlockBusTxn(
  ): Unit = {
    io.host.h2dBus.ready := False
    io.host.d2hBus.valid := False
    io.host.d2hBus.payload := io.host.d2hBus.payload.getZero

    io.dev.h2dBus.valid := False
    io.dev.h2dBus.payload := io.dev.h2dBus.payload.getZero
    io.dev.d2hBus.ready := False
  }
  doBlockBusTxn()
  //--------
  object AllowBurstState
  extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      //MAIN,
      IDLE,
      //SOFT_RESET_NON_BURST,
      //SOFT_RESET_READ_BURST,
      //SOFT_RESET_WRITE_BURST
      NON_BURST,
      READ_BURST,
      WRITE_BURST,
      SOFT_RESET_NON_BURST//,
      //SOFT_RESET_READ_BURST,
      = newElement();
  }

  val rSeenBusH2dFireEtc = (
    Vec.fill(4)(
      Reg(Bool(), init=False)
    )
  )
  val rSeenBusD2hFireEtc = (
    Vec.fill(3)(
      Reg(Bool(), init=False)
    )
  )
  val rSavedIsWrite = Reg(Bool(), init=False)

  //def maybeSetSeenHostH2dFireEtc(
  //  idx: Int
  //): Unit = {
  //  if (idx == 0) {
  //    when (
  //      !io.softReset
  //      && io.host.h2dBus.valid
  //      && io.host.h2dBus.ready
  //    ) {
  //      rSeenBusH2dFireEtc(idx) := True
  //    }
  //  } else if (idx == 1) {
  //    when (
  //      !io.softReset
  //      && io.host.h2dBus.valid
  //      && io.host.h2dBus.ready
  //    ) {
  //      rSeenBusH2dFireEtc(idx) := True
  //    }
  //  } else if (idx == 2) {
  //    when (
  //      !io.softReset
  //      && io.host.h2dBus.valid
  //      && io.host.h2dBus.ready
  //      && io.host.h2dBus.burstLast
  //    ) {
  //      rSeenBusH2dFireEtc(idx) := True
  //    }
  //  } else {
  //    require(false)
  //  }
  //}
  val rSavedSoftReset = (
    Reg(Bool(), init=False)
  )
  when (io.softReset) {
    rSavedSoftReset := True
  }

  val myAllowBurstArea = (
    cfg.busCfg.allowBurst
  ) generate (new Area {
    val rRdBurstCnt = (
      Reg(UInt(cfg.busCfg.burstCntWidth bits))
      init(0x0)
    )
    val rWrBurstCnt = (
      Reg(UInt(cfg.busCfg.burstCntWidth bits))
      init(0x0)
    )
    val rAllowBurstState = (
      Reg(AllowBurstState())
      init(AllowBurstState.IDLE)
    )

    switch (rAllowBurstState) {
      is (AllowBurstState.IDLE) {
        rRdBurstCnt := io.host.h2dBus.burstCnt
        rWrBurstCnt := io.host.h2dBus.burstCnt
        rSavedSoftReset := False

        rSavedIsWrite := io.host.h2dBus.isWrite

        switch (
          io.softReset
          ## io.host.h2dBus.valid
          ## io.host.h2dBus.burstFirst
          ## io.host.h2dBus.isWrite
        ) {
          is (M"010-") {
            // no soft reset, non-burst
            rAllowBurstState := AllowBurstState.NON_BURST
          }
          is (M"0110") {
            // no soft reset, read burst
            rAllowBurstState := AllowBurstState.READ_BURST
          }
          is (M"0111") {
            // no soft reset, write burst
            rAllowBurstState := AllowBurstState.WRITE_BURST
          }
          default {
            // everything else, maybe we've got a soft reset, so do nothing
          }
        }
      }
      is (AllowBurstState.NON_BURST) {
        when (io.dev.h2dBus.fire) {
          rSeenBusH2dFireEtc(0) := True
        }
        when (io.dev.d2hBus.fire) {
          rSeenBusD2hFireEtc(0) := True
        }
        when (!rSavedSoftReset) {
          when (!rSeenBusH2dFireEtc(0)) {
            io.dev.h2dBus << io.host.h2dBus
          }
          when (!rSeenBusD2hFireEtc(0)) {
            io.host.d2hBus << io.dev.d2hBus
          }
        } otherwise { // when (rSavedSoftReset)
          io.dev.h2dBus.valid := !rSeenBusH2dFireEtc(0)
          io.dev.h2dBus.isWrite := rSavedIsWrite
          io.dev.d2hBus.ready := (
            //rSeenBusH2dFireEtc(0)
            !rSeenBusD2hFireEtc(0)
          )
        }

        when (
          rSeenBusH2dFireEtc(0)
          && rSeenBusD2hFireEtc(0)
        ) {
          rAllowBurstState := AllowBurstState.IDLE
          rSeenBusH2dFireEtc(0) := False
          rSeenBusD2hFireEtc(0) := False
        }
      }
      is (AllowBurstState.READ_BURST) {

        when (io.dev.h2dBus.fire) {
          rSeenBusH2dFireEtc(1) := True
        }
        when (
          io.dev.d2hBus.fire
          && io.dev.d2hBus.burstLast
        ) {
          rSeenBusD2hFireEtc(1) := True
        }
        when (!rSavedSoftReset) {
          when (!rSeenBusH2dFireEtc(1)) {
            io.dev.h2dBus << io.host.h2dBus
          }
          when (!rSeenBusD2hFireEtc(1)) {
            io.host.d2hBus << io.dev.d2hBus
          }
        } otherwise { // when (rSavedSoftReset)
          io.dev.h2dBus.valid := !rSeenBusH2dFireEtc(1)
          io.dev.h2dBus.burstFirst := !rSeenBusH2dFireEtc(1)
          io.dev.h2dBus.burstCnt := rRdBurstCnt
          io.dev.d2hBus.ready := (
            !rSeenBusD2hFireEtc(1)
            && io.dev.d2hBus.burstLast
          )
        }

        when (
          rSeenBusH2dFireEtc(1)
          && rSeenBusD2hFireEtc(1)
        ) {
          rAllowBurstState := AllowBurstState.IDLE
          rSeenBusH2dFireEtc(1) := False
          rSeenBusD2hFireEtc(1) := False
        }
      }
      is (AllowBurstState.WRITE_BURST) {
        when (io.dev.h2dBus.fire) {
          rSeenBusH2dFireEtc(3) := True
        }
        when (
          io.dev.h2dBus.fire
          && rWrBurstCnt.orR
        ) {
          rWrBurstCnt := rWrBurstCnt - 1
        }
        when (
          io.dev.h2dBus.fire
          && !rWrBurstCnt.orR
        ) {
          rSeenBusH2dFireEtc(2) := True
        }
        when (io.dev.d2hBus.fire) {
          rSeenBusD2hFireEtc(2) := True
        }

        when (!rSavedSoftReset) {
          when (!rSeenBusH2dFireEtc(2)) {
            io.dev.h2dBus << io.host.h2dBus
          }
          when (!rSeenBusD2hFireEtc(2)) {
            io.host.d2hBus << io.dev.d2hBus
          }
        } otherwise { // when (rSavedSoftReset)
          io.dev.h2dBus.valid := !rSeenBusH2dFireEtc(2)
          io.dev.h2dBus.isWrite := True

          io.dev.h2dBus.burstCnt := rWrBurstCnt
          io.dev.h2dBus.burstFirst := (
            //rWrBurstCnt === maxBurstSizeMinus1
            !rSeenBusH2dFireEtc(2)
          )
          io.dev.h2dBus.burstLast := !rWrBurstCnt.orR
          io.dev.d2hBus.ready := (
            !rSeenBusD2hFireEtc(2)
            //&& io.dev.h2dBus.burstLast
          )
        }
        when (
          rSeenBusH2dFireEtc(2)
          && rSeenBusD2hFireEtc(2)
        ) {
          rAllowBurstState := AllowBurstState.IDLE
          rSeenBusH2dFireEtc(2) := False
          rSeenBusH2dFireEtc(3) := False
          rSeenBusD2hFireEtc(2) := False
        }
      }
    }
  })
  //--------
  val myNoBurstsArea = (
    !cfg.busCfg.allowBurst
  ) generate (new Area {
    require(
      false,
      "Not yet implemented"
    )
  })
  //--------
}
