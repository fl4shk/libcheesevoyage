package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

//case class LcvBusL1DataCacheIo(
//)

//case class LcvCacheDirectoryIo(
//  cfg: LcvCacheConfig,
//) extends Bundle {
//  val hostBusVec = Vec.fill(cfg.busMesiCfg.numCpus)(
//    slave(LcvBusIo(cfg=cfg.busCfg))
//  )
//}
//case class LcvCacheDirectory(
//  cfg: LcvCacheConfig,
//) extends Component {
//  val io = LcvCacheDirectoryIo(cfg=cfg)
//}
object LcvCacheConfigTest extends App {
  val busCfg = LcvBusConfig(
    mainCfg=LcvBusMainConfig(
      dataWidth=32,
      addrWidth=32,
      burstAlwaysMaxSize=false,
      srcWidth=1,
    ),
    cacheCfg=Some(
      LcvCacheConfig(
        level=1,
        isIcache=false,
        lineSizeBytes=64,
        depthWords=1024,
        numL1CacheHosts=4,
      )
    )
  )
}

//case class LcvCacheAttr(
//)
