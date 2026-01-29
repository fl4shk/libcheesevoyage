//package libcheesevoyage.bus.lcvBus
//
//import spinal.core._
//import spinal.core.formal._
//import spinal.core.sim._
//import spinal.lib._
//import spinal.lib.misc.pipeline._
//
//case class LcvBusByteEnAdapterConfig(
//  loBusCfg: LcvBusConfig,
//) {
//  require(!loBusCfg.haveByteEn)
//  val hiBusCfg = LcvBusConfig(
//    mainCfg=(
//      loBusCfg.mainCfg.mkCopyWithByteEn()
//      //if (!loBusCfg.haveByteEn) (
//      //  loBusCfg.mainCfg.mkCopyWithByteEn()
//      //) else (
//      //  loBusCfg.mainCfg.mkCopyWithoutByteEn()
//      //)
//    ),
//    cacheCfg=loBusCfg.cacheCfg
//  )
//}
//
//case class LcvBusByteEnAdapterIo(
//  cfg: LcvBusByteEnAdapterConfig
//) extends Bundle {
//  //val loBus = slave(LcvBusIo(cfg=cfg.loBusCfg))
//  //val hiBus = master(LcvBusIo(cfg=cfg.hiBusCfg))
//  val loH2dBus = slave(Stream(LcvBusH2dPayload(cfg=cfg.loBusCfg)))
//  val hiH2dBus = master(Stream(LcvBusH2dPayload(cfg=cfg.hiBusCfg)))
//}
//
////private[libcheesevoyage] case class LcvBusByteEnToNonByteEnAdapter(
////  cfg: LcvBusByteEnAdapterConfig
////) extends Component {
////  //--------
////  val io = LcvBusByteEnAdapterIo(cfg=cfg)
////  //--------
////}
//
////private[libcheesevoyage] case class LcvBusNonByteEnToByteEnAdapter(
////  cfg: LcvBusByteEnAdapterConfig
////) extends Component {
////  //--------
////  val io = LcvBusByteEnAdapterIo(cfg=cfg)
////  //--------
////}
//
//case class LcvBusNonBurstByteEnAdapter(
//  cfg: LcvBusByteEnAdapterConfig
//) extends Component {
//  //--------
//  val io = LcvBusByteEnAdapterIo(cfg=cfg)
//  //--------
//  object State
//  extends SpinalEnum(defaultEncoding=binaryOneHot) {
//    val
//      IDLE_OR_HAVE_FULL_WORD,
//      SMALL_READ_WAIT_LO_H2D_FIRE,
//      = newElement();
//  }
//  val rState = (
//    Reg(State())
//    init(State.IDLE_OR_HAVE_FULL_WORD)
//  )
//  switch (rState) {
//    when 
//  }
//}
