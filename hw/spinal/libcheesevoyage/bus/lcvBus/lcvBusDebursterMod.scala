package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

case class LcvBusDebursterConfig(
  //mainCfg: LcvBusMainConfig,
  //cacheCfg: Option[LcvBusCacheConfig],
  loBusCfg: LcvBusConfig
) {
  require(loBusCfg.mainCfg.allowBurst)

  //val loBusCfg = LcvBusConfig(
  //  mainCfg=mainCfg,
  //  cacheCfg=cacheCfg,
  //)
  val hiBusCfg = LcvBusConfig(
    mainCfg=loBusCfg.mainCfg.mkCopyWithoutAllowingBurst(),
    cacheCfg=loBusCfg.cacheCfg
  )
}


case class LcvBusDebursterIo(
  cfg: LcvBusDebursterConfig
) extends Bundle {
  val loBus = slave(LcvBusIo(cfg=cfg.loBusCfg))
  val hiBus = master(LcvBusIo(cfg=cfg.hiBusCfg))
}

case class LcvBusDeburster(
  cfg: LcvBusDebursterConfig
) extends Component {
  //--------
  val io = LcvBusDebursterIo(cfg=cfg)
  //--------
  object State
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE_OR_NON_BURST,
      READ_BURST,
      WRITE_BURST
      = newElement();
  }
  val rState = (
    Reg(State())
    init(State.IDLE_OR_NON_BURST)
  )
  switch (rState) {
    is (State.IDLE_OR_NON_BURST) {
    }
    is (State.READ_BURST) {
    }
    is (State.WRITE_BURST) {
    }
  }
}
