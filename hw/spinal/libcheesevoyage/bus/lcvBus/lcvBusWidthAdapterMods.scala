package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


case class LcvBusDataWidthAdapterConfig(
  //--------
  loBusMainCfg: LcvBusMainConfig,
  hiBusDataWidth: Int,
  //--------
  loBusCacheCfg: Option[LcvBusCacheConfig],
  hiBusCacheCfg: Option[LcvBusCacheConfig],
  //--------
) {
  val loBusCfg = LcvBusConfig(
    mainCfg=loBusMainCfg,
    cacheCfg=loBusCacheCfg
  )
  val hiBusCfg = LcvBusConfig(
    mainCfg=loBusMainCfg.mkCopyWithNewDataWidth(dataWidth=hiBusDataWidth),
    cacheCfg=hiBusCacheCfg,
  )
  require(
    loBusCfg.dataWidth
    != hiBusCfg.dataWidth
  )
}

case class LcvBusDataWidthAdapterIo(
  cfg: LcvBusDataWidthAdapterConfig
) extends Bundle {
  //--------
  val loBus = slave(
    LcvBusIo(cfg=cfg.loBusCfg)
  )
  val hiBus = master(
    LcvBusIo(cfg=cfg.hiBusCfg)
  )
  //--------
}

// for instruction caches and other things that are read-only
// later,
// perhaps it would be wise to copy/modify this module's contents
// to another module to support write bursts
// and/or non-burst bus transactions
case class LcvBusSimpleReadBurstOnlyDataWidthAdapter(
  cfg: LcvBusDataWidthAdapterConfig
) extends Component {
  //--------
  val io = LcvBusDataWidthAdapterIo(cfg=cfg)
  //--------
  require(
    cfg.loBusCfg.dataWidth > cfg.hiBusCfg.dataWidth
  )
  def myDataWidthRatio = (
    (cfg.loBusCfg.dataWidth / cfg.hiBusCfg.dataWidth).toInt
  )
  require(
    myDataWidthRatio == 2
  )
  //--------
  io.loBus.h2dBus.ready := False
  io.loBus.d2hBus.valid := False
  io.loBus.d2hBus.payload := (
    io.loBus.d2hBus.payload.getZero
  )
  io.loBus.d2hBus.data.allowOverride
  io.loBus.d2hBus.data := (
    RegNext(io.loBus.d2hBus.data)
  )

  io.hiBus.d2hBus.ready := False
  io.hiBus.h2dBus.valid := False
  io.hiBus.h2dBus.payload := (
    io.hiBus.h2dBus.payload.getZero
  )
  //--------
  object State
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      //READ_NON_BURST,
      //WRITE_NON_BURST,
      READ_BURST
      //WRITE_BURST
      //START_HI_READ_BURST,
      = newElement()
  }
  val rState = (
    Reg(State())
    init(State.IDLE)
  )
  //--------
  val rSavedLoH2dPayload = (
    Reg(cloneOf(io.loBus.h2dBus.payload))
    init(io.loBus.h2dBus.payload.getZero)
  )

  val hiD2hFifo = (
    StreamFifo(
      dataType=cloneOf(io.hiBus.d2hBus.payload),
      depth=(
        1 << cfg.hiBusCfg.burstCntWidth
      ),
      latency=(
        2
      ),
      forFMax=true,
    )
  )
  hiD2hFifo.io.push << io.hiBus.d2hBus
  val rSeenHiH2dFire = Reg(Bool())
  val rLoD2hBurstCnt = Reg(UInt(cfg.loBusCfg.burstCntWidth bits))

  switch (rState) {
    is (State.IDLE) {
      rSavedLoH2dPayload := io.loBus.h2dBus.payload
      io.loBus.h2dBus.ready := True
      rSeenHiH2dFire := False
      rLoD2hBurstCnt := (1 << cfg.loBusCfg.burstCntWidth) - 1
      when (io.loBus.h2dBus.valid) {
        rState := State.READ_BURST
      }
    }
    is (State.READ_BURST) {
      when (!rSeenHiH2dFire) {
        io.hiBus.h2dBus.valid := True
      }
      when (io.hiBus.h2dBus.ready) {
        rSeenHiH2dFire := True
        val myMaybeReptD2hStm = Vec.fill(2)(
          Stream(cloneOf(hiD2hFifo.io.pop.payload))
        )
        myMaybeReptD2hStm.head <-/< hiD2hFifo.io.pop
        myMaybeReptD2hStm.last <-/< myMaybeReptD2hStm.head.repeat(
          times=(myDataWidthRatio)
        )._1
        myMaybeReptD2hStm.last.translateInto(io.loBus.d2hBus)(
          dataAssignment=(outp, inp) => {
            outp.mainNonBurstInfo := inp.mainNonBurstInfo
            outp.data.allowOverride
            outp.data := RegNext(outp.data)

            switch (rLoD2hBurstCnt.lsb) {
              is (True) {
                outp.data(
                  cfg.hiBusCfg.dataWidth - 1
                  downto 0
                ) := (
                  inp.data
                )
              }
              is (False) {
                outp.data(
                  cfg.loBusCfg.dataWidth - 1
                  downto cfg.hiBusCfg.dataWidth
                ) := (
                  inp.data
                )
              }
            }
          }
        )
        when (io.loBus.d2hBus.fire) {
          rLoD2hBurstCnt := rLoD2hBurstCnt
        }

        io.loBus.d2hBus.burstCnt := rLoD2hBurstCnt

        io.loBus.d2hBus.burstFirst := (
          io.loBus.d2hBus.valid
          && rLoD2hBurstCnt.andR
        )
        io.loBus.d2hBus.burstLast := (
          io.loBus.d2hBus.valid
          && !rLoD2hBurstCnt.orR
        )

        when (
          io.loBus.d2hBus.fire
          && !rLoD2hBurstCnt.orR
        ) {
          rState := State.IDLE
        }
      }
    }
  }
  //--------
}
