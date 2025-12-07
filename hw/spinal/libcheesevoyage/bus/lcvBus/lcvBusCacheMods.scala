package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

import libcheesevoyage.general._
import libcheesevoyage.math._

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
  val seqlock = (
    cfg.cacheCfg.get.coherent
  ) generate (
    LcvBusCacheSeqlock(cfg=cfg)
  )
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
          require(false)
        }
        case _ => {
          require(hiBusCacheCfg.kind == LcvCacheKind.Shared)
        }
      }
    }
    case None => {
      if (loBusCacheCfg.coherent) {
        require(loBusCacheCfg.kind == LcvCacheKind.Shared)
      } else {
        require(loBusCacheCfg.kind != LcvCacheKind.Shared)
      }
    }
  }
}

case class LcvBusCacheIo(
  //loBusCfg: LcvBusConfig,
  //hiBusCfg: LcvBusConfig,
  cfg: LcvBusCacheBusPairConfig
) extends Bundle {
  val loBus = slave(LcvBusIo(cfg=cfg.loBusCfg))
  val hiBus = master(LcvBusIo(cfg=cfg.hiBusCfg))
}

private[libcheesevoyage] case class LcvBusCacheBaseArea(
  //cfg: LcvBusCacheBusPairConfig,
  io: LcvBusCacheIo,
) extends Area {
  //--------
  def cfg = io.cfg
  //--------
  def loBusCfg = cfg.loBusCfg
  def loBusCacheCfg = cfg.loBusCacheCfg
  def myLineRamAddrRshift = loBusCacheCfg.myLineRamAddrRshift
  def wordWidth = loBusCacheCfg.wordWidth
  def depthWords = loBusCacheCfg.depthWords
  def depthLines = loBusCacheCfg.depthLines

  val lineWordRam = RamSdpPipe(
    wordType=UInt(wordWidth bits),
    depth=depthWords,
    optIncludeWrByteEn=true,
    initBigInt=Some(Array.fill(depthWords)(BigInt(0))),
    arrRamStyle=cfg.loBusCacheCfg.lineWordMemRamStyle,
  )
  val lineAttrsRam = RamSdpPipe(
    wordType=LcvBusCacheLineAttrs(cfg=loBusCfg),
    depth=depthLines,
    initBigInt=Some(Array.fill(depthLines)(BigInt(0))),
    arrRamStyle=cfg.loBusCacheCfg.lineAttrsMemRamStyle,
  )

  val rdLineWord = UInt(wordWidth bits)
  val rdLineAttrs = LcvBusCacheLineAttrs(cfg=loBusCfg)

  val wrLineWord = UInt(wordWidth bits)
  val wrLineAttrs = LcvBusCacheLineAttrs(cfg=loBusCfg)

  val rMyLineAttrsRamRdEn = Reg(Bool(), init=False)
  lineAttrsRam.io.rdEn := rMyLineAttrsRamRdEn

  val rMyLineWordRamRdEn = Reg(Bool(), init=False)
  lineWordRam.io.rdEn := rMyLineWordRamRdEn

  lineWordRam.io.wrEn := False
  lineAttrsRam.io.wrEn := False
  //--------
  val rLoBusH2dPayload = (
    RegNext(
      next=io.loBus.h2dBus.payload,
      init=io.loBus.h2dBus.payload.getZero,
    )
  )
  val rDel2LoBusH2dPayload = (
    RegNext(
      next=rLoBusH2dPayload,
      init=rLoBusH2dPayload.getZero,
    )
  )
  def rDel2LoBusAddr = rDel2LoBusH2dPayload.addr
  def rDel2LoBusAddrTag = rDel2LoBusAddr(loBusCacheCfg.tagRange)
  def rDel2LoBusAddrSet = rDel2LoBusAddr(loBusCacheCfg.setRange)

  val rSavedLoBusH2dPayload = (
    Reg(cloneOf(rDel2LoBusH2dPayload))
    init(rDel2LoBusH2dPayload.getZero)
  )

  def haveHit = (
    rdLineAttrs.fire
    //&& rdLineAttrs.tag === rDel2LoBusAddrTag
    && LcvFastCmpEq(
      left=rdLineAttrs.tag,
      right=rDel2LoBusAddrTag,
      cmpEqIo=null,
    )._1
  )
  //--------
  def doAllLineRamsReadSync(
    busAddr: UInt,
  ): Unit = {
    doLineWordRamReadSync(busAddr=busAddr)
    doLineAttrsRamReadSync(busAddr=busAddr)
  }
  def doLineWordRamReadSync(
    busAddr: UInt,
  ): Unit = {
    //lineWordRam.io.rdEn := True
    rMyLineWordRamRdEn := True
    lineWordRam.io.rdAddr := (
      //(busAddr >> myLineRamAddrRshift)
      (busAddr(busAddr.high downto myLineRamAddrRshift))
      .resize(lineWordRam.io.rdAddr.getWidth)
    )
  }
  def doLineWordRamWrite(
    busAddr: UInt,
    lineWord: Option[UInt],
    byteEn: Option[UInt],
    setEn: Boolean=true,
  ): Unit = {
    if (setEn) {
      lineWordRam.io.wrEn := True
    }
    lineWordRam.io.wrAddr := (
      //(busAddr >> myLineRamAddrRshift)
      (busAddr(busAddr.high downto myLineRamAddrRshift))
      .resize(lineWordRam.io.wrAddr.getWidth)
    )
    byteEn match {
      case Some(byteEn) => {
        lineWordRam.io.wrByteEn := byteEn.asBits
      }
      case None => {
        lineWordRam.io.wrByteEn := B(
          lineWordRam.io.wrByteEn.getWidth bits, default -> True
        )
      }
    }
    lineWord match {
      case Some(lineWord) => {
        lineWordRam.io.wrData := lineWord.asBits
      }
      case None => {
        lineWordRam.io.wrData := (
          //myD2hBus.sendData.data.asBits
          io.hiBus.d2hBus.data.asBits
        )
      }
    }
  }
  def doLineAttrsRamReadSync(
    busAddr: UInt,
  ): Unit = {
    //lineAttrsRam.io.rdEn := True
    rMyLineAttrsRamRdEn := True
    lineAttrsRam.io.rdAddr := (
      //(busAddr >> log2Up(loBusCacheCfg.lineSizeBytes))
      (busAddr(busAddr.high downto log2Up(loBusCacheCfg.lineSizeBytes)))
      .resize(lineAttrsRam.io.rdAddr.getWidth)
    )
  }
  def doLineAttrsRamWrite(
    busAddr: UInt,
    lineAttrs: LcvBusCacheLineAttrs=wrLineAttrs,
    setEn: Boolean=true,
  ): Unit = {
    if (setEn) {
      lineAttrsRam.io.wrEn := True
    }
    lineAttrsRam.io.wrAddr := (
      //(busAddr >> log2Up(loBusCacheCfg.lineSizeBytes))
      (busAddr(busAddr.high downto log2Up(loBusCacheCfg.lineSizeBytes)))
      .resize(lineAttrsRam.io.wrAddr.getWidth)
    )
    lineAttrsRam.io.wrData := lineAttrs.asBits
  }
  //--------
  doLineAttrsRamReadSync(
    busAddr=rLoBusH2dPayload.addr,
  )
  doLineWordRamReadSync(
    busAddr=rLoBusH2dPayload.addr,
  )
  doLineAttrsRamWrite(
    busAddr=(
      //RegNext(next=rBusAddr, init=rBusAddr.getZero)
      rDel2LoBusAddr
    ),
    setEn=false,
  )
  doLineWordRamWrite(
    busAddr=(
      //RegNext(next=rBusAddr, init=rBusAddr.getZero)
      rDel2LoBusAddr
    ),
    lineWord=(
      //Some(rPastBusSendDataData)
      Some(rSavedLoBusH2dPayload.data)
    ),
    byteEn=Some(rSavedLoBusH2dPayload.byteEn),
    setEn=false,
  )
}

// `LcvBusNonCoherentDataCache` is mostly exists for the purposes of
// developing the use of `LcvBus` for `libsnowhouse`'s `SnowHouse` module
// (i.e. for communication between the `SnowHouse` module and the `LcvBus`
// bus).
//
// `LcvBusNonCoherentDataCache` may also be useful
// for something like a fixed-function GPU's texture cache, perhaps.
// However, even in that case, it might make more sense to have several
// texture caches, perhaps such as one per tiled renderer, which brings us
// back to *maybe* needing coherent caches, though I'm not 100% certain
// about that since textures are to be loaded into the GPU RAM by the CPU.
// Thus maybe `LcvBusNonCoherentDataCache` has a use after all?
private[libcheesevoyage] case class LcvBusNonCoherentDataCache(
  cfg: LcvBusCacheBusPairConfig,
) extends Component {
  //--------
  val io = LcvBusCacheIo(cfg=cfg)
  //--------
  val base = LcvBusCacheBaseArea(io=io)
  //--------
  object State extends SpinalEnum(
    defaultEncoding=(
      //binarySequential
      binaryOneHot
    )
  ) {
    val
      IDLE,
      //LOAD_HIT,
      //STORE_HIT,
      SEND_LINE_TO_BUS_PIPE_1,
      SEND_LINE_TO_BUS,
      RECV_LINE_FROM_BUS_PIPE_1,
      RECV_LINE_FROM_BUS,
      POST_LOAD_MISS,
      POST_STORE_MISS
      //NON_CACHED_BUS_ACCESS,
        // this will probably be covered with an `LcvBusSlicer`
      = newElement();
  }
  //--------
  val rState = (
    Reg(State())
    init(State.IDLE)
  )

  switch (rState) {
    is (State.IDLE) {
      switch (
        base.haveHit
        ## base.rDel2LoBusH2dPayload.isWrite
      ) {
        is (B"11") {
          // store hit
          //base.rSavedLoBusH2dPayload := base.rDel2LoBusH2dPayload
          //rState := State.STORE_HIT
        }
        is (B"00") {
          // load miss
          base.rSavedLoBusH2dPayload := base.rDel2LoBusH2dPayload
        }
        is (B"01") {
          // store miss
          base.rSavedLoBusH2dPayload := base.rDel2LoBusH2dPayload
        }
        default {
          // load hit
        }
      }
    }
    //is (State.LOAD_HIT) {
    //}
    //is (State.STORE_HIT) {
    //}
    is (State.SEND_LINE_TO_BUS_PIPE_1) {
    }
    is (State.SEND_LINE_TO_BUS) {
    }
    is (State.RECV_LINE_FROM_BUS_PIPE_1) {
    }
    is (State.RECV_LINE_FROM_BUS) {
    }
    is (State.POST_LOAD_MISS) {
    }
    is (State.POST_STORE_MISS) {
    }
  }
  //--------
}
private[libcheesevoyage] case class LcvBusCoherentInstrCache(
  cfg: LcvBusCacheBusPairConfig,
) extends Component {
  //--------
  val io = LcvBusCacheIo(cfg=cfg)
  //--------
}
private[libcheesevoyage] case class LcvBusCoherentDataCache(
  cfg: LcvBusCacheBusPairConfig,
) extends Component {
  //--------
  val io = LcvBusCacheIo(cfg=cfg)
  //--------
  val base = LcvBusCacheBaseArea(io=io)
  //--------
}
private[libcheesevoyage] case class LcvBusSharedCache(
  cfg: LcvBusCacheBusPairConfig,
) extends Component {
  //--------
  val io = LcvBusCacheIo(cfg=cfg)
  //--------
  val base = LcvBusCacheBaseArea(io=io)
  //--------
  //--------
  //--------
  //--------
  //object State extends SpinalEnum(defaultEncoding=binaryOneHot) {
  //  val
  //    IDLE,
  //    CMP_SEQLOCK
  //    = newElement();
  //}
  //val rState = (
  //  Reg(State())
  //  init(State.IDLE)
  //)
  //switch (rState) {
  //  is (State.IDLE) {
  //  }
  //  is (State.CMP_SEQLOCK) {
  //  }
  //}
}

case class LcvBusCache(
  cfg: LcvBusCacheBusPairConfig,
) extends Component {
  //--------
  val io = LcvBusCacheIo(cfg=cfg)
  //--------
  //def haveNonCoherentInstrCache = (
  //  !cfg.loBusCacheCfg.coherent
  //  && cfg.loBusCacheCfg.kind == LcvCacheKind.I
  //  && cfg.hiBusCacheCfg == None
  //)
  def haveNonCoherentDataCache = (
    !cfg.loBusCacheCfg.coherent
    && cfg.loBusCacheCfg.kind == LcvCacheKind.D
    && cfg.hiBusCacheCfg == None
  )
  def haveCoherentInstrCache = (
    cfg.loBusCacheCfg.coherent
    && cfg.loBusCacheCfg.kind == LcvCacheKind.I
    && cfg.hiBusCacheCfg != None
    && cfg.hiBusCacheCfg.get.coherent
    && cfg.hiBusCacheCfg.get.kind == LcvCacheKind.Shared
  )
  def haveCoherentDataCache = (
    cfg.loBusCacheCfg.coherent
    && cfg.loBusCacheCfg.kind == LcvCacheKind.D
    && cfg.hiBusCacheCfg != None
    && cfg.hiBusCacheCfg.get.coherent
    && cfg.hiBusCacheCfg.get.kind == LcvCacheKind.Shared
  )
  def haveSharedCache = (
    cfg.loBusCacheCfg.coherent
    && cfg.loBusCacheCfg.kind == LcvCacheKind.Shared
    && cfg.hiBusCacheCfg == None
  )

  val nonCoherentDataCache = (
    haveNonCoherentDataCache
  ) generate (
    LcvBusNonCoherentDataCache(cfg=cfg)
  )
  val coherentInstrCache = (
    haveCoherentInstrCache
  ) generate (
    LcvBusCoherentInstrCache(cfg=cfg)
  )
  val coherentDataCache = (
    haveCoherentDataCache
  ) generate (
    LcvBusCoherentDataCache(cfg=cfg)
  )
  val sharedCache = (
    haveSharedCache
  ) generate (
    LcvBusSharedCache(cfg=cfg)
  )
  //if (haveNonCoherentInstrCache) {
  //} else 
  if (haveNonCoherentDataCache) {
    io <> nonCoherentDataCache.io
  } else if (haveCoherentInstrCache) {
    io <> coherentInstrCache.io
  } else if (haveCoherentDataCache) {
    io <> coherentDataCache.io
  } else if (haveSharedCache) {
    io <> sharedCache.io
  } else {
    require(
      false
    )
  }
  //--------
}

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
          kind=LcvCacheKind.D,
          lineSizeBytes=64,
          depthWords=1024,
          numCpus=1,
        ),
        hiBusCacheCfg=(
          //Some(LcvBusCacheConfig(
          //  kind=LcvCacheKind.Shared,
          //  lineSizeBytes=64,
          //  depthWords=2048,
          //  numCpus=2,
          //))
          None
        )
      )
    )
    top
  }
}
