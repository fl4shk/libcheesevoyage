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

  val rSeenHostH2dFireEtc = (
    Vec.fill(3)(
      Reg(Bool(), init=False)
    )
  )
  //val rSeenDevH2dFireEtc = (
  //  Vec.fill(3)(
  //    Reg(Bool(), init=False)
  //  )
  //)
  //val rSeenHostD2hFireEtc = (
  //  Vec.fill(3)(
  //    Reg(Bool(), init=False)
  //  )
  //)
  //val rSeenDevD2hFireEtc = (
  //  Vec.fill(3)(
  //    Reg(Bool(), init=False)
  //  )
  //)

  //def maybeSetSeenHostH2dFireEtc(
  //  idx: Int
  //): Unit = {
  //  if (idx == 0) {
  //    when (
  //      !io.softReset
  //      && io.host.h2dBus.valid
  //      && io.host.h2dBus.ready
  //    ) {
  //      rSeenHostH2dFireEtc(idx) := True
  //    }
  //  } else if (idx == 1) {
  //    when (
  //      !io.softReset
  //      && io.host.h2dBus.valid
  //      && io.host.h2dBus.ready
  //    ) {
  //      rSeenHostH2dFireEtc(idx) := True
  //    }
  //  } else if (idx == 2) {
  //    when (
  //      !io.softReset
  //      && io.host.h2dBus.valid
  //      && io.host.h2dBus.ready
  //      && io.host.h2dBus.burstLast
  //    ) {
  //      rSeenHostH2dFireEtc(idx) := True
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
        rWrBurstCnt := io.host.h2dBus.burstCnt
        rSavedSoftReset := False

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
        when (!rSavedSoftReset) {
          when (!rSeenHostH2dFireEtc(0)) {
            io.dev.h2dBus << io.host.h2dBus
          }
          when (io.host.h2dBus.fire) {
            rSeenHostH2dFireEtc(0) := True
          }

          io.host.d2hBus << io.dev.d2hBus

          when (
            rSeenHostH2dFireEtc(0)
            && io.host.d2hBus.fire
          ) {
            rAllowBurstState := AllowBurstState.IDLE
            rSeenHostH2dFireEtc(0) := False
          }
        } otherwise { // when (rSavedSoftReset)
          io.dev.d2hBus.ready := True

          when (
            !rSeenHostH2dFireEtc(0)
            || io.dev.d2hBus.valid
              // we can just check for `valid === True` since we already 
              // have `ready === True`
          ) {
            rAllowBurstState := AllowBurstState.IDLE
            rSeenHostH2dFireEtc(0) := False
          }
        }
      }
      is (AllowBurstState.READ_BURST) {
        when (!rSavedSoftReset) {
          when (!rSeenHostH2dFireEtc(1)) {
            io.dev.h2dBus << io.host.h2dBus
          }
          when (io.host.h2dBus.fire) {
            rSeenHostH2dFireEtc(1) := True
          }

          io.host.d2hBus << io.dev.d2hBus

          when (
            rSeenHostH2dFireEtc(1)
            && io.host.d2hBus.fire
            && io.host.d2hBus.burstLast
          ) {
            rAllowBurstState := AllowBurstState.IDLE
            rSeenHostH2dFireEtc(1) := False
          }
        } otherwise { // when (rSavedSoftReset)
          io.dev.d2hBus.ready := True

          when (
            !rSeenHostH2dFireEtc(1)
            || (
              io.dev.d2hBus.valid
              // we can just check for `valid === True` since we already 
              // have `ready === True`
              //&& rRdBurstCnt.msb
              && io.dev.d2hBus.burstLast
            )
          ) {
            rAllowBurstState := AllowBurstState.IDLE
            rSeenHostH2dFireEtc(1) := False
          }
        }
      }
      is (AllowBurstState.WRITE_BURST) {
        when (
          io.dev.h2dBus.fire
          && rWrBurstCnt.orR
        ) {
          rWrBurstCnt := rWrBurstCnt - 1
        }
        when (
          io.dev.h2dBus.fire
          //&& rWrBurstCnt.orR
          //&& !(rWrBurstCnt - 1).orR
          && !rWrBurstCnt.orR
        ) {
          rSeenHostH2dFireEtc(2) := True
        }

        when (!rSavedSoftReset) {
          when (!rSeenHostH2dFireEtc(2)) {
            io.dev.h2dBus << io.host.h2dBus
          }
          //when (
          //  io.host.h2dBus.fire
          //  && io.host.h2dBus.burstLast
          //) {
          //  rSeenHostH2dFireEtc(2) := True
          //}

          io.host.d2hBus << io.dev.d2hBus.haltWhen(
            !rSeenHostH2dFireEtc(2)
          )

          when (
            rSeenHostH2dFireEtc(2)
            && io.host.d2hBus.fire
          ) {
            rAllowBurstState := AllowBurstState.IDLE
            rSeenHostH2dFireEtc(2) := False
          }
        } otherwise { // when (rSavedSoftReset)
          when (!rSeenHostH2dFireEtc(2)) {
            io.dev.h2dBus.valid := True
          }
          io.dev.h2dBus.burstLast := !rWrBurstCnt.orR
          io.dev.d2hBus.ready := (
            rSeenHostH2dFireEtc(2)
            && io.dev.h2dBus.burstLast //True
          )

          when (
            //rSeenHostH2dFireEtc(2)
            //&& 
            io.dev.d2hBus.fire
          ) {
            rAllowBurstState := AllowBurstState.IDLE
            rSeenHostH2dFireEtc(2) := False
          }
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
