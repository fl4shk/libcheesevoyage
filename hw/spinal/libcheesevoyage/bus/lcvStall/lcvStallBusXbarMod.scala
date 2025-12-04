package libcheesevoyage.bus.lcvStall

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

// TODO: finish this

//case class LcvStallXbarConfig(
//  addrWidth: Int,
//  dataWidth: Int,
//  numHosts: Int,
//  numDevs: Int,
//) {
//}
//case class LcvStallXbarHostPayload(
//  cfg: LcvStallXbarConfig
//) extends Bundle {
//  val addr = UInt(cfg.addrWidth bits)
//  val data = UInt(cfg.dataWidth bits)
//  val isWrite = Bool()
//}
//case class LcvStallXbarDevPayload(
//  cfg: LcvStallXbarConfig
//) extends Bundle {
//  val data = UInt(cfg.dataWidth bits)
//}
//
////case class LcvStallXbarIo(
////  cfg: LcvStallXbarConfig
////) extends Bundle {
////  val h2dIo = Vec.fill(cfg.numHosts)(
////    slave(new LcvStallIo(
////      hostPayloadType=Some(HardType(LcvStallXbarHostPayload(cfg=cfg))),
////      devPayloadType=Some(HardType(LcvStallXbarDevPayload(cfg=cfg))),
////    ))
////  )
////  val d2hIo = Vec.fill(cfg.numDevs)(
////    master(new LcvStallIo(
////      hostPayloadType=Some(HardType(LcvStallXbarHostPayload(cfg=cfg))),
////      devPayloadType=Some(HardType(LcvStallXbarDevPayload(cfg=cfg))),
////    ))
////  )
////}
case class LcvStallBusXbarConfig(
  busCfg: LcvStallBusConfig,
  numNonCpuHosts: Int,
  numDevs: Int,
) {
  //val numHosts = busCfg.mesiCfg.numCpus + numNonCpuHosts
}

case class LcvStallBusXbarIo(
  cfg: LcvStallBusXbarConfig
) extends Bundle {
}

case class LcvStallBusXbar(
  cfg: LcvStallBusXbarConfig
) extends Component {
  //--------
  val io = LcvStallBusXbarIo(cfg=cfg)
  //--------
}
