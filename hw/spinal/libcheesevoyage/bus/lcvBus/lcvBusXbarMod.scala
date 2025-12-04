package libcheesevoyage.bus.lcvBus

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

// TODO: finish this

//case class LcvBusXbarConfig(
//  addrWidth: Int,
//  dataWidth: Int,
//  numHosts: Int,
//  numDevs: Int,
//) {
//}
//case class LcvBusXbarHostPayload(
//  cfg: LcvBusXbarConfig
//) extends Bundle {
//  val addr = UInt(cfg.addrWidth bits)
//  val data = UInt(cfg.dataWidth bits)
//  val isWrite = Bool()
//}
//case class LcvBusXbarDevPayload(
//  cfg: LcvBusXbarConfig
//) extends Bundle {
//  val data = UInt(cfg.dataWidth bits)
//}
//
////case class LcvBusXbarIo(
////  cfg: LcvBusXbarConfig
////) extends Bundle {
////  val h2dIo = Vec.fill(cfg.numHosts)(
////    slave(new LcvStallIo(
////      hostPayloadType=Some(HardType(LcvBusXbarHostPayload(cfg=cfg))),
////      devPayloadType=Some(HardType(LcvBusXbarDevPayload(cfg=cfg))),
////    ))
////  )
////  val d2hIo = Vec.fill(cfg.numDevs)(
////    master(new LcvStallIo(
////      hostPayloadType=Some(HardType(LcvBusXbarHostPayload(cfg=cfg))),
////      devPayloadType=Some(HardType(LcvBusXbarDevPayload(cfg=cfg))),
////    ))
////  )
////}
case class LcvBusXbarConfig(
  busCfg: LcvBusConfig,
  numNonCpuHosts: Int,
  numDevs: Int,
) {
  //val numHosts = busCfg.mesiCfg.numCpus + numNonCpuHosts
}

case class LcvBusXbarIo(
  cfg: LcvBusXbarConfig
) extends Bundle {
}

case class LcvBusXbar(
  cfg: LcvBusXbarConfig
) extends Component {
  //--------
  val io = LcvBusXbarIo(cfg=cfg)
  //--------
}
