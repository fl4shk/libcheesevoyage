package libcheesevoyage.bus.lcvStall

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

case class LcvStallBusBurstAdapterConfig(
  hostBusCfg: LcvStallBusConfig,
  devBusCfg: LcvStallBusConfig,
) {
  hostBusCfg.needSameDataWidth(that=devBusCfg)
  hostBusCfg.needSameAddrWidth(that=devBusCfg)
  hostBusCfg.needSameSrcWidth(that=devBusCfg)
  assert(
    hostBusCfg.maxBurstSize >= devBusCfg.maxBurstSize,
    s"hostBusCfg.maxBurstSize (${hostBusCfg.maxBurstSize})"
    + s" < devBusCfg.maxBurstSize (${devBusCfg.maxBurstSize})"
  )
}

case class LcvStallBusBurstAdapterIo(
  cfg: LcvStallBusBurstAdapterConfig
) extends Bundle {
  //--------
  val hostBus = slave(LcvStallBusIo(cfg=cfg.hostBusCfg))
  val devBus = master(LcvStallBusIo(cfg=cfg.devBusCfg))
  //--------
  //val hostH2dBus = (
  //  slave(new LcvStallIo[LcvStallBusH2dSendPayload, Bool](
  //    sendPayloadType=Some(LcvStallBusH2dSendPayload(cfg=cfg.hostBusCfg)),
  //    recvPayloadType=None,
  //  ))
  //)
  //val hostD2hBus = (
  //  master(new LcvStallIo[LcvStallBusD2hSendPayload, Bool](
  //    sendPayloadType=Some(LcvStallBusD2hSendPayload(cfg=cfg.hostBusCfg)),
  //    recvPayloadType=None,
  //  ))
  //)

  //val devH2dBus = (
  //  master(new LcvStallIo[LcvStallBusH2dSendPayload, Bool](
  //    sendPayloadType=Some(LcvStallBusH2dSendPayload(cfg=cfg.devBusCfg)),
  //    recvPayloadType=None,
  //  ))
  //)
  //val devD2hBus = (
  //  slave(new LcvStallIo[LcvStallBusD2hSendPayload, Bool](
  //    sendPayloadType=Some(LcvStallBusD2hSendPayload(cfg=cfg.devBusCfg)),
  //    recvPayloadType=None,
  //  ))
  //)
}

case class LcvStallBusBurstAdapter(
  cfg: LcvStallBusBurstAdapterConfig
) extends Component {
  //--------
  val io = LcvStallBusBurstAdapterIo(cfg=cfg)
  //--------
  val h2dFifo = StreamFifo(
    dataType=LcvStallBusH2dSendPayload(
      cfg=cfg.hostBusCfg
    ),
    depth=(cfg.hostBusCfg.maxBurstSize),
    latency=2,
    forFMax=true,
  )
  val d2hFifo = StreamFifo(
    dataType=LcvStallBusD2hSendPayload(
      cfg=cfg.hostBusCfg
    ),
    depth=(cfg.hostBusCfg.maxBurstSize),
    latency=2,
    forFMax=true,
  )
  //--------
  //h2dFifo.io.push
  //--------
}
