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


case class LcvBusCacheLineAttrs(
  cfg: LcvBusConfig,
) extends Bundle {
  //--------
  def cacheCfg = (
    cfg.cacheCfg.get
  )
  //--------
  val valid = Bool()
  def fire = valid
  //--------
  val tag = UInt(
    cacheCfg.tagWidth bits
  )
  val dirty = (
    cacheCfg.kind == LcvCacheKind.D
    || cacheCfg.kind == LcvCacheKind.Shared
  ) generate (
    Bool()
  )
  val seqlock = LcvBusCacheSeqlock(cfg=cfg)
  //--------
}
case class LcvBusCacheBusPairConfig(
  mainCfg: LcvBusMainConfig,
  loBusCacheCfg: LcvBusCacheConfig,
  hiBusCacheCfg: Option[LcvBusCacheConfig],
) {
  val loBusCfg = LcvBusConfig(
    mainCfg=mainCfg,
    cacheCfg=Some(loBusCacheCfg),
  )
  val hiBusCfg = LcvBusConfig(
    mainCfg=mainCfg,
    cacheCfg=hiBusCacheCfg,
  )
  hiBusCacheCfg match {
    case Some(hiBusCacheCfg) => {
      loBusCacheCfg.kind match {
        case LcvCacheKind.Shared => {
          //require(
          //  loBusCacheCfg.kind != LcvCacheKind.Shared
          //)
          require(
            false
          )
        }
        case _ => {
          //require(
          //  hiBusCacheCfg.kind 
          //)
          require(
            hiBusCacheCfg.kind == LcvCacheKind.Shared
          )
        }
      }
    }
    case None => {
      require(
        loBusCacheCfg.kind != LcvCacheKind.Shared
      )
    }
  }
}

case class LcvBusCacheIo(
  //loBusCfg: LcvBusConfig,
  //hiBusCfg: LcvBusConfig,
  cfg: LcvBusCacheBusPairConfig
) extends Bundle {
  //require(loBusCfg.cacheCfg != None)
  ////require(cfg.cacheCfg.get.level == 1)
  //require(
  //  loBusCfg.mainCfg
  //  == hiBusCfg.mainCfg
  //)

  val loBus = slave(LcvBusIo(cfg=cfg.loBusCfg))
  val hiBus = master(LcvBusIo(cfg=cfg.hiBusCfg))

  //val hiBus = master(LcvBusIo(cfg=cfg))
}

private[libcheesevoyage] case class LcvBusNonCoherentDataCache(
  cfg: LcvBusCacheBusPairConfig,
) extends Component {
  //--------
  val io = LcvBusCacheIo(cfg=cfg)
  //--------
}

case class LcvBusCache(
  cfg: LcvBusCacheBusPairConfig
) extends Component {
  //--------
  val io = LcvBusCacheIo(cfg=cfg)
  //--------
}

//case class LcvCacheDirectoryIo(
//  cfg: LcvBusConfig,
//) extends Bundle {
//  require(cfg.cacheCfg != None)
//  val loBusVec = Vec.fill(cfg.cacheCfg.get.numCpus)(
//    slave(LcvBusIo(cfg=cfg))
//  )
//}
//case class LcvCacheDirectory(
//  cfg: LcvBusConfig,
//) extends Component {
//  require(cfg.cacheCfg != None)
//  val io = LcvCacheDirectoryIo(cfg=cfg)
//}


//object LcvCacheConfigTest extends App {
//  val busCfg = LcvBusConfig(
//    mainCfg=LcvBusMainConfig(
//      dataWidth=32,
//      addrWidth=32,
//      burstAlwaysMaxSize=false,
//      srcWidth=1,
//    ),
//    cacheCfg=Some(
//      LcvCacheConfig(
//        level=1,
//        isIcache=false,
//        lineSizeBytes=64,
//        depthWords=1024,
//        numL1DataCacheHosts=4,
//      )
//    )
//  )
//}

//case class LcvCacheAttr(
//)
object LcvBusCacheSpinalConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    )
  )
}

object LcvBusCacheToVerilog extends App {
  LcvBusCacheSpinalConfig.spinal.generateVerilog{
    val top = LcvBusCache(
      LcvBusCacheBusPairConfig(
        mainCfg=LcvBusMainConfig(
          dataWidth=32,
          addrWidth=32,
          burstAlwaysMaxSize=true,
          srcWidth=1,
        ),
        loBusCacheCfg=LcvBusCacheConfig(
          //isIcache=false,
          kind=LcvCacheKind.D,
          lineSizeBytes=64,
          depthWords=1024,
          numCpus=2,
        ),
        hiBusCacheCfg=(
          Some(LcvBusCacheConfig(
            kind=LcvCacheKind.Shared,
            lineSizeBytes=64,
            depthWords=2048,
            numCpus=2,
          ))
          //None
        )
      )
      //LcvBusConfig(
      //  mainCfg=LcvBusMainConfig(
      //    dataWidth=32,
      //    addrWidth=32,
      //    burstAlwaysMaxSize=true,
      //    srcWidth=1,
      //  ),
      //  cacheCfg=Some(LcvBusCacheConfig(
      //    //isIcache=false,
      //    kind=LcvCacheKind.I,
      //    lineSizeBytes=64,
      //    depthWords=1024,
      //    numCpus=2,
      //  ))
      //)
    )
    top
  }
}
