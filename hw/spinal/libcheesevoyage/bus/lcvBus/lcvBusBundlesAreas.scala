package libcheesevoyage.bus.lcvBus

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

//case class LcvBusMesiConfig(
//  numCpus: Int,
//) {
//}

//object LcvBusMesiMsg
//extends SpinalEnum(defaultEncoding=binaryOneHot) {
//}

//object LcvMesiState extends SpinalEnum(defaultEncoding=binarySequential) {
//  val
//    M,  // Modified (M) - Core has modified data; not yet written back
//        // (multicore "dirty")
//    E,  // Exclusive (E) - Cache line is the same as main memory and is the
//        // only cached copy
//    S,  // Shared (S) - Same as main memory but copies may exist in other
//        // caches
//    I   // Invalid (I) - Line data is not valid (as in simple cache)
//    = newElement();
//}

sealed trait LcvCacheKind
object LcvCacheKind {
  case object I extends LcvCacheKind
  case object D extends LcvCacheKind
  //case object Tlb extends LcvCacheKind
  case object Shared extends LcvCacheKind
}

case class LcvBusMainConfig(
  dataWidth: Int,
  addrWidth: Int,
  //burstSizeWidth: Int,
  //burstCntWidth: Int,//Option[Int],
  //alwaysDoBurst: Boolean,
  allowBurst: Boolean,
  burstAlwaysMaxSize: Boolean,
  srcWidth: Int, //Option[Int],
) {
  def mkCopyWithAllowingBurst(): LcvBusMainConfig = (
    LcvBusMainConfig(
      dataWidth=this.dataWidth,
      addrWidth=this.addrWidth,
      allowBurst=true,
      burstAlwaysMaxSize=true,
      srcWidth=this.srcWidth
    )
  )
  def mkCopyWithoutAllowingBurst(): LcvBusMainConfig = (
    LcvBusMainConfig(
      dataWidth=this.dataWidth,
      addrWidth=this.addrWidth,
      allowBurst=false,
      burstAlwaysMaxSize=false,
      srcWidth=this.srcWidth
    )
  )

  val burstCntMaxNumBytes = 64
  val burstCntWidth = log2Up(burstCntMaxNumBytes / (dataWidth / 8))
  if (!allowBurst) {
    require(!burstAlwaysMaxSize)
  }
}

case class LcvBusCacheConfig(
  //level: Int, 
  //isIcache: Boolean,
  kind: LcvCacheKind,
  lineSizeBytes: Int,
  depthWords: Int, // this is in number of words
  numCpus: Int,
  lineWordMemRamStyleAltera: String=(
    //"MLAB"
    //"no_rw_check, M10K"
    "M10K"
  ),
  lineWordMemRamStyleXilinx: String=(
    //"auto"
    //"distributed"
    "block"
    //"ultra"
  ),
  lineAttrsMemRamStyleAltera: String=(
    //"MLAB"
    //"no_rw_check, M10K"
    "M10K"
  ),
  lineAttrsMemRamStyleXilinx: String=(
    //"auto"
    //"distributed"
    "block"
    //"ultra"
  ),
  //private[libcheesevoyage] var busCfg: LcvBusConfig=null,
  private[libcheesevoyage] var busMainCfg: LcvBusMainConfig=null
) {
  //private[libcheesevoyage] var busMainCfg: LcvBusMainConfig = 
  def seqlockWidth = 32
  def seqlockGlobalCntWidth = 41
  //def busMainCfg = busCfg.mainCfg
  //def busMesiCfg = busCfg.mesiCfg

  def wordWidth = busMainCfg.dataWidth
  def addrWidth = busMainCfg.addrWidth
  def coherent: Boolean = (numCpus > 1)
  def myLineRamAddrRshift = log2Up(wordSizeBytes)

  private[libcheesevoyage] def doRequires() {
    require(numCpus >= 1)

    require(
      addrWidth == (1 << log2Up(addrWidth)),
      s"addrWidth: need power of two: "
      + s"${addrWidth} != ${(1 << log2Up(addrWidth))}"
    )
    require(
      addrWidth == (addrWidth / 8).toInt * 8,
      s"addrWidth: need multiple of 8: "
      + s"${addrWidth} != ${(addrWidth / 8).toInt * 8}"
    )
    require(
      wordWidth == (1 << log2Up(wordWidth)),
      s"wordWidth: need power of two: "
      + s"${wordWidth} != ${(1 << log2Up(wordWidth))}"
    )
    require(
      wordWidth == (wordWidth / 8).toInt * 8,
      s"wordWidth: need multiple of 8: "
      + s"${wordWidth} != ${(wordWidth / 8).toInt * 8}"
    )
    require(
      lineSizeBytes == (1 << log2Up(lineSizeBytes)),
      s"lineSizeBytes: need power of two: "
      + s"${lineSizeBytes} != ${(1 << log2Up(lineSizeBytes))}"
    )
    require(
      depthWords == (1 << log2Up(depthWords)),
      s"depthWords: need power of two: "
      + s"${depthWords} != ${(1 << log2Up(depthWords))}"
    )
  }
  //--------
  def wordSizeBytes = wordWidth / 8
  def lineSizeWords = (
    lineSizeBytes / wordSizeBytes
  )
  def depthBytes = depthWords * wordSizeBytes
  def depthLines = (
    // this is the number of cache lines
    depthBytes / lineSizeBytes
  )
  def tagWidth = (
    //addrWidth - log2Up(depthBytes)
    //tag bits = addr bits - index bits - offset bits
    //index bits = log2(lines)
    //offset bits = log2(words per line)
    //(assuming your addresses are word-based ofc) (edited)
    addrWidth - log2Up(depthLines) - log2Up(lineSizeWords) - 1
  )
  def tagRange = addrWidth - 2 downto (addrWidth - 1 - tagWidth)
  def nonCachedRange = addrWidth - 1 downto addrWidth - 1
  def setWidth = addrWidth - tagWidth - 1
  def mySetRangeHi = addrWidth - 1 - tagWidth - 1
  def mySetRangeLo = log2Up(lineSizeBytes)
  def setRange = mySetRangeHi downto mySetRangeLo
}

case class LcvBusConfig(
  mainCfg: LcvBusMainConfig,
  //mesiCfg: LcvBusMesiConfig,
  var cacheCfg: Option[LcvBusCacheConfig]=None,
) {
  def dataWidth = mainCfg.dataWidth
  def addrWidth = mainCfg.addrWidth
  //def burstSizeWidth = mainCfg.burstSizeWidth
  def allowBurst = mainCfg.allowBurst
  def burstCntMaxNumBytes = mainCfg.burstCntMaxNumBytes
  def burstCntWidth = mainCfg.burstCntWidth
  def maxBurstSizeMinus1 = (
    (1 << burstCntWidth) - 1
  )

  def burstAddr(
    someAddr: UInt,
    someBurstCnt: UInt,
    incrBurstCnt: Boolean,
  ) = {
    require(
      someBurstCnt.getWidth == burstCntWidth
    )
    if (incrBurstCnt) {
      someBurstCnt := someBurstCnt + 1
    }
    Cat(
      someAddr(
        someAddr.high
        downto someBurstCnt.getWidth + log2Up(dataWidth / 8)
      ),
      someBurstCnt,
      U(s"${log2Up(dataWidth / 8)}'d0"),
    ).asUInt
  }

  cacheCfg match {
    case Some(cacheCfg) => {
      cacheCfg.busMainCfg = mainCfg
      cacheCfg.doRequires()

      println(
        s"kind:"
        //+ s"${cacheCfg.kind}cache
        + (
          cacheCfg.kind match {
            case LcvCacheKind.I => {
              s"Icache"
            }
            case LcvCacheKind.D => {
              s"Dcache"
            }
            case _ => {
              s"${cacheCfg.kind}"
            }
          }
        )
        + s": \n"
        + s"  tagWidth:${cacheCfg.tagWidth}\n"
        + s"  tagRange:${cacheCfg.tagRange}\n"
        + s"  nonCachedRange:${cacheCfg.nonCachedRange}\n"
        + s"  setWidth:${cacheCfg.setWidth}\n"
        + s"  setRange:${cacheCfg.setRange}\n"
      )
    }
    case None => {
    }
  }

  //def burstSize: Option[Int] = (
  //  burstCntWidth match {
  //    case Some(burstCntWidth) => {
  //      Some(1 << burstCntWidth)
  //    }
  //    case None => {
  //      None
  //    }
  //  }
  //)
  def srcWidth = mainCfg.srcWidth

  def byteEnWidth: Int = (dataWidth / 8).toInt
  def addrByteWidth: Int = (addrWidth / 8).toInt
  require(
    (byteEnWidth * 8) == dataWidth,
    (
      f"It is required that "
      + f"(byteEnWidth:${byteEnWidth}) * 8 == dataWidth:${dataWidth}"
    )
  )
  require(
    (addrByteWidth * 8) == addrWidth,
    (
      f"It is required that "
      + f"(addrByteWidth:${addrByteWidth}) * 8) == addrWidth:${addrWidth}"
    )
  )
  def needSameDataWidth(
    that: LcvBusConfig
  ): Unit = {
    require(
      this.dataWidth == that.dataWidth,
      s"It is required that "
      + s"dataWidth:${this.dataWidth} == that.dataWidth:${that.dataWidth}"
    )
  }
  def needSameAddrWidth(
    that: LcvBusConfig
  ): Unit = {
    require(
      this.addrWidth == that.addrWidth,
      s"It is required that "
      + s"addrWidth:${this.addrWidth} == that.addrWidth:${that.addrWidth}"
    )
  }
  def needSameBurstCntWidth(
    that: LcvBusConfig
  ): Unit = {
    require(
      this.burstCntWidth == that.burstCntWidth,
      s"It is required that "
      + s"burstCntWidth:${this.burstCntWidth} "
      + s"== that.burstCntWidth:${that.burstCntWidth}"
    )
  }
  //def needSameBurstSizeWidth(
  //  that: LcvBusConfig
  //): Unit = {
  //  require(
  //    this.burstSizeWidth == that.burstSizeWidth,
  //    s"It is required that "
  //    + s"burstSizeWidth:${this.burstSizeWidth} "
  //    + s"== that.burstSizeWidth:${that.burstSizeWidth}"
  //  )
  //}
  def needSameSrcWidth(
    that: LcvBusConfig
  ): Unit = {
    require(
      this.srcWidth == that.srcWidth,
      s"It is required that "
      + s"srcWidth:${this.srcWidth} == that.srcWidth:${that.srcWidth}"
    )
  }
}

case class LcvBusMemMapConfig(
  busCfg: LcvBusConfig,
  addrSliceHi: Int,
  addrSliceLo: Int,
  //optNumDevs: Option[Int]=None,
  optSliceSize: Option[Int]=None,
) {
  //def addrSliceEnd = addrSliceStart + addrSliceWidth - 1
  def addrSliceWidth = addrSliceHi - addrSliceLo + 1
  def addrSliceSize = (
    optSliceSize match {
      case Some(mySliceSize) => {
        require(
          mySliceSize > 0
        )
        require(
          mySliceSize <= (1 << addrSliceWidth)
        )
        mySliceSize
      }
      case None => {
        (1 << addrSliceWidth)
      }
    }
  )


  def addrSliceRange = addrSliceHi downto addrSliceLo

  require(
    addrSliceWidth > 0,
    s"need addrSliceWidth:${addrSliceWidth} > 0"
  )
  require(
    addrSliceWidth <= busCfg.addrWidth,
    s"need addrSliceWidth:${addrSliceWidth} "
    + s"<= busCfg.addrWidth:${busCfg.addrWidth}"
  )
  require(
    addrSliceLo >= 0,
    s"need addrSliceStart:${addrSliceLo} >= 0"
  )
  require(
    addrSliceLo < busCfg.addrWidth,
    s"need addrSliceStart:${addrSliceLo} "
    + s"< busCfg.addrWidth:${busCfg.addrWidth}"
  )
  require(
    addrSliceHi >= 0,
    s"need addrSliceEnd:${addrSliceHi} >= 0"
  )
  require(
    addrSliceHi < busCfg.addrWidth,
    s"need addrSliceEnd:${addrSliceHi} "
    + s"< busCfg.addrWidth:${busCfg.addrWidth}"
  )
}

case class LcvBusCacheSeqlock(
  cfg: LcvBusConfig,
) extends Bundle {
  require(
    cfg.cacheCfg != None
  )
  require(
    cfg.cacheCfg.get.coherent
  )

  val data = UInt(cfg.cacheCfg.get.seqlockWidth bits)

  def isLocked(): Bool = data.lsb

  def lock(
    prev: UInt=data
  ): Unit = {
    // if not passing in `prev` at the call site,
    // make sure to `setAsReg()`, etc.
    data := prev | 0x1
  }
  def unlock(
    prev: UInt=data
  ): Unit = {
    // if not passing in `prev` at the call site,
    // make sure to `setAsReg()`, etc.
    data := prev + 1
  }
}

//object LcvBusH2dCacheMsg //LcvBusH2dDataCacheMsg
//extends SpinalEnum(defaultEncoding=binarySequential) {
//  // this is from L1 to shared L2 
//  val
//    //--------
//    CMP_SEQLOCK,
//    //--------
//    // When you read a cache line from a lower tier cache, you check if
//    // your seqlock is the same as the next upper tier.
//    // If not, you fetch from the higher tier, provided it's not locked.
//    // FL4SHK NOTE: If it's locked, I'll just wait until it's unlocked
//    // (i.e. the other core finished its write)
//    //READ_CHECK_SEQLOCK,
//    START_READ_LINE,
//    FINISH_READ_LINE,
//    //--------
//    // Any access is guarded by the low-order bit of the seqlock.
//    // When it's 1, the lock is locked.
//    // When a write begins, you set this bit.
//    // When a write completes, you increment it (clearing the lock bit and
//    // updating the sequence number/marking the cache line as dirty).
//    //WRITE_CHECK_SEQLOCK,
//    START_WRITE_LINE,    // seqlock.lock()
//    FINISH_WRITE_LINE,   // seqlock.unlock()
//    //--------
//    // For an RMW, you either fail or retry if the sequence number from the
//    // read differs from the sequence number when you go to lock it.
//    ATOMIC_LL,
//    ATOMIC_SC
//    //--------
//    = newElement();
//}

//object LcvBusD2hCacheMsg
//extends SpinalEnum(defaultEncoding=binarySequential) {
//  val
//    A
//    = newElement();
//}

//object LcvBusH2dInstrCacheMsg
//extends SpinalEnum(defaultEncoding=binarySequential) {
//  val
//    READ_CHECK_SEQLOCK,
//    START_READ_LINE,
//    FINISH_READ_LINE
//    = newElement();
//}

case class LcvBusH2dPayloadMainNonBurstInfo(
  cfg: LcvBusConfig
) extends Bundle {
  val addr = UInt(cfg.addrWidth bits)
  val data = UInt(cfg.dataWidth bits)
  val byteEn = UInt(cfg.byteEnWidth bits)
  val isWrite = Bool()
  val src = UInt(cfg.srcWidth bits)
}

case class LcvBusH2dPayloadMainBurstInfo(
  cfg: LcvBusConfig,
) extends Bundle {
  val burstCnt = UInt(cfg.burstCntWidth bits)
  val burstFirst = Bool()
  val burstLast = Bool()
}

case class LcvBusH2dPayloadCacheInfo(
  cfg: LcvBusConfig,
) extends Bundle {
  val seqlock = LcvBusCacheSeqlock(cfg=cfg)
  //val cacheFirst = Bool()       // starting cache-related operations
  val cacheLast = Bool()        // ending cache-related operations
  val lineValid = Bool()
  val lineDirty = Bool()
  //val msg = LcvBusH2dCacheMsg()
}

case class LcvBusH2dPayload(
  cfg: LcvBusConfig,
) extends Bundle {
  //--------
  val mainNonBurstInfo = LcvBusH2dPayloadMainNonBurstInfo(cfg=cfg)
  def addr = mainNonBurstInfo.addr
  def data = mainNonBurstInfo.data
  def byteEn = mainNonBurstInfo.byteEn
  def isWrite = mainNonBurstInfo.isWrite
  def src = mainNonBurstInfo.src
  //--------
  val mainBurstInfo = (cfg.allowBurst) generate (
    LcvBusH2dPayloadMainBurstInfo(cfg=cfg)
  )
  //def burstSize = mainBurstInfo.burstSize
  def burstCnt = mainBurstInfo.burstCnt
  def burstFirst = mainBurstInfo.burstFirst
  def burstLast = mainBurstInfo.burstLast
  //--------
  //def atLastBurstAddr(
  //  someBurstCnt: UInt
  //)
  def burstAddr(
    someBurstCnt: UInt,
    incrBurstCnt: Boolean,
  ) = {
    cfg.burstAddr(
      someAddr=addr,
      someBurstCnt=someBurstCnt,
      incrBurstCnt=incrBurstCnt,
    )
    //require(
    //  someBurstCnt.getWidth == cfg.burstCntWidth
    //)
    //Cat(
    //  addr(
    //    addr.high
    //    downto someBurstCnt.getWidth + log2Up(cfg.dataWidth / 8)
    //  ),
    //  someBurstCnt,
    //  U(s"${log2Up(cfg.dataWidth / 8)}'d0"),
    //).asUInt
  }
  //def selfBurstAddr() = this.burstAddr(someBurstCnt=burstCnt)
  //--------
  val cacheInfo = (
    cfg.cacheCfg != None
    && cfg.cacheCfg.get.coherent
  ) generate (
    LcvBusH2dPayloadCacheInfo(cfg=cfg)
  )
  def cacheSeqlock = cacheInfo.seqlock
  //def cacheFirst = cacheInfo.cacheFirst
  def cacheLast = cacheInfo.cacheLast
  def cacheLineValid = cacheInfo.lineValid
  def cacheLineDirty = cacheInfo.lineDirty
  //def cacheMsg = cacheInfo.msg
  //--------
  //--------
}

case class LcvBusD2hPayloadMainNonBurstInfo(
  cfg: LcvBusConfig,
) extends Bundle {
  val data = UInt(cfg.dataWidth bits)
  val src = UInt(cfg.srcWidth bits)
}

case class LcvBusD2hPayloadMainBurstInfo(
  cfg: LcvBusConfig,
) extends Bundle {
  val burstCnt = UInt(cfg.burstCntWidth bits)
  val burstFirst = Bool()
  val burstLast = Bool()
}

case class LcvBusD2hPayloadCacheInfo(
  cfg: LcvBusConfig,
) extends Bundle {
  val seqlock = LcvBusCacheSeqlock(cfg=cfg)
  //val cacheFirst = Bool()       // starting cache-related operations
  val cacheLast = Bool()        // ending cache-related operations
  val lineValid = Bool()
  val lineDirty = Bool()
  //val msg = LcvBusH2dCacheMsg()
}

case class LcvBusD2hPayload(
  cfg: LcvBusConfig,
) extends Bundle {
  //--------
  val mainNonBurstInfo = LcvBusD2hPayloadMainNonBurstInfo(cfg=cfg)
  def data = mainNonBurstInfo.data
  def src = mainNonBurstInfo.src
  //--------
  val mainBurstInfo = (cfg.allowBurst) generate (
    LcvBusD2hPayloadMainBurstInfo(cfg=cfg)
  )
  def burstCnt = mainBurstInfo.burstCnt
  def burstFirst = mainBurstInfo.burstFirst
  def burstLast = mainBurstInfo.burstLast
  //--------
  val cacheInfo = (
    cfg.cacheCfg != None
    && cfg.cacheCfg.get.coherent
  ) generate (
    LcvBusD2hPayloadCacheInfo(cfg=cfg)
  )
  def cacheSeqlock = cacheInfo.seqlock
  //def cacheFirst = cacheInfo.cacheFirst
  def cacheLast = cacheInfo.cacheLast
  def cacheLineValid = cacheInfo.lineValid
  def cacheLineDirty = cacheInfo.lineDirty
  //def cacheMsg = cacheInfo.msg
  //--------
}

case class LcvBusIo(
  cfg: LcvBusConfig,
) extends Bundle with IMasterSlave {
  val h2dBus = (
    slave(Stream(LcvBusH2dPayload(cfg=cfg)))
  )
  val d2hBus = (
    master(Stream(LcvBusD2hPayload(cfg=cfg)))
  )

  def asMaster(): Unit = {
    master(h2dBus)
    slave(d2hBus)
    //if (cfg.cacheCfg != None) {
    //  
    //}
  }

  def <<(
    that: LcvBusIo,
  ): Unit = {
    this.h2dBus << that.h2dBus
    that.d2hBus << this.d2hBus
  }
  def >>(
    that: LcvBusIo,
  ): Unit = {
    that << this
  }
  def <-<(
    that: LcvBusIo,
  ): Unit = {
    this.h2dBus <-< that.h2dBus
    that.d2hBus <-< this.d2hBus
  }
  def >->(
    that: LcvBusIo,
  ): Unit = {
    that <-< this
  }
  def <-/<(
    that: LcvBusIo,
  ): Unit = {
    this.h2dBus <-/< that.h2dBus
    that.d2hBus <-/< this.d2hBus
  }
  def >/->(
    that: LcvBusIo,
  ): Unit = {
    that <-/< this
  }
}
