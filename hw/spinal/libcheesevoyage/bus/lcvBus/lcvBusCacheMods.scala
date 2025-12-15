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
  //require(!mainCfg.allowBurst)
  loBusCacheCfg.kind match {
    case LcvCacheKind.Shared => {
      require(mainCfg.allowBurst)
    }
    case _ => {
      require(!mainCfg.allowBurst)
    }
  }
  val loBusCfg = LcvBusConfig(
    mainCfg=mainCfg,
    cacheCfg=Some(loBusCacheCfg),
  )
  val hiBusCfg = LcvBusConfig(
    mainCfg=mainCfg.mkCopyWithAllowingBurst(),
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
  def haveNonCoherentInstrCache = (
    !loBusCacheCfg.coherent
    && loBusCacheCfg.kind == LcvCacheKind.I
    && hiBusCacheCfg == None
  )
  def haveNonCoherentDataCache = (
    !loBusCacheCfg.coherent
    && loBusCacheCfg.kind == LcvCacheKind.D
    && hiBusCacheCfg == None
  )
  def haveCoherentInstrCache = (
    loBusCacheCfg.coherent
    && loBusCacheCfg.kind == LcvCacheKind.I
    && hiBusCacheCfg != None
    && hiBusCacheCfg.get.coherent
    && hiBusCacheCfg.get.kind == LcvCacheKind.Shared
  )
  def haveCoherentDataCache = (
    loBusCacheCfg.coherent
    && loBusCacheCfg.kind == LcvCacheKind.D
    && hiBusCacheCfg != None
    && hiBusCacheCfg.get.coherent
    && hiBusCacheCfg.get.kind == LcvCacheKind.Shared
  )
  def haveSharedCache = (
    loBusCacheCfg.coherent
    && loBusCacheCfg.kind == LcvCacheKind.Shared
    && hiBusCacheCfg == None
  )

}

case class LcvBusCacheIo(
  //loBusCfg: LcvBusConfig,
  //hiBusCfg: LcvBusConfig,
  cfg: LcvBusCacheBusPairConfig
) extends Bundle {
  val loBus = slave(LcvBusIo(cfg=cfg.loBusCfg))
  val hiBus = master(LcvBusIo(cfg=cfg.hiBusCfg))
}

case class LcvBusCacheMissFifoPairIo(
  cfg: LcvBusCacheBusPairConfig
) extends Bundle {
  val push = slave(Stream(LcvBusH2dPayload(cfg=cfg.loBusCfg)))
  val pop = master(Stream(LcvBusH2dPayload(cfg=cfg.loBusCfg)))
  val cacheMiss = in(Bool())
}

case class LcvBusCacheMissFifoThing(
  cfg: LcvBusCacheBusPairConfig
) extends Component {
  val io = LcvBusCacheMissFifoPairIo(cfg=cfg)

  def fifoDepthMain = 8
  def fifoDepthSub = 4//5 //fifoDepthMain 4

  val mainFifo = StreamFifo(
    dataType=LcvBusH2dPayload(cfg=cfg.loBusCfg),
    depth=fifoDepthMain,
    latency=0,
    forFMax=true,
  )
  val subFifo = StreamFifo(
    dataType=LcvBusH2dPayload(cfg=cfg.loBusCfg),
    depth=fifoDepthSub,
    latency=0,
    forFMax=true,
  )

  //val popMain = Stream(LcvBusH2dPayload(cfg=cfg.loBusCfg))
  //val popSub = Stream(LcvBusH2dPayload(cfg=cfg.loBusCfg))

  //val popArbiter = StreamArbiterFactory.lowerFirst.transactionLock.onArgs(
  //  //subFifo.io.pop,
  //  //mainFifo.io.pop
  //  popSub,
  //  popMain,
  //)
  //io.pop << popArbiter
  def fifoCntSubMax = fifoDepthSub - 2 //- 1 //- 2 
  val rFifoCntSub = (
    Reg(SInt((log2Up(fifoDepthSub + 1) + 1) bits))
    init(fifoCntSubMax)
  )

  mainFifo.io.push.valid := False
  mainFifo.io.push.payload := mainFifo.io.push.payload.getZero
  mainFifo.io.pop.ready := False
  mainFifo.io.flush := False

  subFifo.io.push.valid := False
  subFifo.io.push.payload := subFifo.io.push.payload.getZero
  subFifo.io.flush := False

  //val (pushForkMain, pushForkSub) = StreamFork2(io.push, synchronous=true)
  //pushForkMain.ready := False
  //pushForkSub.ready := False
  io.push.ready := False

  object State extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      POST_CACHE_MISS
      = newElement();
  }
  val rState = (
    Reg(State())
    init(State.IDLE)
  )
  switch (rState) {
    is (State.IDLE) {
      mainFifo.io.push << io.push //pushForkMain
      io.pop << mainFifo.io.pop

      //subFifo.io.push << pushForkSub
      subFifo.io.push.valid := True
      subFifo.io.push.payload := io.push.payload
      // This should make `subFifo` act like a circular FIFO that we
      // keep only the most recent contents of
      subFifo.io.pop.ready := False
      when (subFifo.io.push.fire) {
        when (!rFifoCntSub.msb) {
          rFifoCntSub := rFifoCntSub - 1
        } otherwise {
          subFifo.io.pop.ready := True
        }
      }
      when (io.cacheMiss) {
        rState := State.POST_CACHE_MISS
      }
    }
    is (State.POST_CACHE_MISS) {
      rFifoCntSub := fifoCntSubMax
      
      //when (rose(rState === State.POST_CACHE_MISS)) {
        mainFifo.io.flush := True
      //} otherwise {
      //  mainFifo.io.push << io.push
      //}
      io.pop << subFifo.io.pop
      when (
        !subFifo.io.pop.valid
        && !io.cacheMiss
      ) {
        rState := State.IDLE
      }
    }
  }
}

private[libcheesevoyage] case class LcvBusCacheBaseArea(
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

  val lineWordRamCfg = RamSdpPipeConfig(
    wordType=UInt(wordWidth bits),
    depth=depthWords,
    optIncludeWrByteEn=true,
    initBigInt=Some(Array.fill(depthWords)(BigInt(0))),
    arrRamStyleAltera=cfg.loBusCacheCfg.lineWordMemRamStyleAltera,
    arrRamStyleXilinx=cfg.loBusCacheCfg.lineWordMemRamStyleXilinx,
  )
  val lineWordRam = RamSdpPipe(cfg=lineWordRamCfg)
  val lineAttrsRamCfg = RamSdpPipeConfig(
    wordType=LcvBusCacheLineAttrs(cfg=loBusCfg),
    depth=depthLines,
    optIncludeWrByteEn=false,
    initBigInt=Some(Array.fill(depthLines)(BigInt(0))),
    arrRamStyleAltera=cfg.loBusCacheCfg.lineAttrsMemRamStyleAltera,
    arrRamStyleXilinx=cfg.loBusCacheCfg.lineAttrsMemRamStyleXilinx,
  )
  val lineAttrsRam = RamSdpPipe(cfg=lineAttrsRamCfg)

  val rdLineWord = UInt(wordWidth bits)
  rdLineWord := lineWordRam.io.rdData

  val rdLineAttrs = LcvBusCacheLineAttrs(cfg=loBusCfg)
  rdLineAttrs := lineAttrsRam.io.rdData

  val wrLineAttrs = LcvBusCacheLineAttrs(cfg=loBusCfg)
  wrLineAttrs := RegNext(wrLineAttrs, init=wrLineAttrs.getZero)
  wrLineAttrs.allowOverride

  //val rMyLineAttrsRamRdEn = Reg(Bool(), init=False)
  //lineAttrsRam.io.rdEn := rMyLineAttrsRamRdEn
  //lineAttrsRam.io.rdEn := True

  //val rMyLineWordRamRdEn = Reg(Bool(), init=False)
  //lineWordRam.io.rdEn := rMyLineWordRamRdEn
  //lineWordRam.io.rdEn := True
  //lineWordRam.io.rdEn := False
  //lineAttrsRam.io.rdEn := False
  //lineAttrsRam.io.rdEn := rMyLineAttrsRamRdEn
  //lineWordRam.io.rdEn := rMyLineWordRamRdEn

  //lineWordRam.io.rdEn := True
  //lineAttrsRam.io.rdEn := True

  lineWordRam.io.wrEn := False
  lineAttrsRam.io.wrEn := False
  //--------
  val rHadLoH2dFinish = Reg(Bool(), init=False)
  val rHadLoD2hFinish = Reg(Bool(), init=False)
  val rHadHiH2dFinish = Reg(Bool(), init=False)
  val rHadHiD2hFinish = Reg(Bool(), init=False)
  //--------
  //val loH2dFifo = (
  //  StreamFifo(
  //    dataType=LcvBusH2dPayload(loBusCfg),
  //    depth=8,
  //    latency=0,
  //    forFMax=true,
  //  )
  //)
  //loH2dFifo.io.push << io.loBus.h2dBus
  //loH2dFifo.io.pop.ready := False

  //val loH2dFifo = (
  //  StreamFifo(
  //    dataType=LcvBusH2dPayload(loBusCfg),
  //    depth=(
  //      8
  //    ),
  //    latency=0,
  //    forFMax=true,
  //  )
  //)
  val loH2dCacheMissFifoThing = LcvBusCacheMissFifoThing(cfg=cfg)
  loH2dCacheMissFifoThing.io.push << io.loBus.h2dBus
  //loH2dCacheMissFifoThing.io.cacheMiss := False
  val myFifoThingCacheMiss = (
    //Reg(Bool(), init=False)
    Bool()
  )
  myFifoThingCacheMiss := RegNext(myFifoThingCacheMiss, init=False)
  loH2dCacheMissFifoThing.io.cacheMiss := myFifoThingCacheMiss
  //rFifoThingCacheMiss := False

  def loH2dPopStm = (
    //loH2dFifo.io.pop
    //io.loBus.h2dBus
    loH2dCacheMissFifoThing.io.pop
  )
  loH2dPopStm.ready := False

  val rLoH2dPayload = (
    RegNextWhen(
      next=(
        //io.loBus.h2dBus.payload
        loH2dPopStm.payload
      ),
      cond=(
        loH2dPopStm.valid
        //loH2dPopStm.fire
      ),
      init=(
        //io.loBus.h2dBus.payload.getZero
        loH2dPopStm.payload.getZero
      ),
    )
  )
  def rLoBusAddr = rLoH2dPayload.addr
  def rLoBusAddrTag = rLoBusAddr(loBusCacheCfg.tagRange)
  def rLoBusAddrSet = rLoBusAddr(loBusCacheCfg.setRange)
  val rDel2LoH2dPayload = (
    RegNext(
      next=rLoH2dPayload,
      init=rLoH2dPayload.getZero,
    )
  )
  def rDel2LoBusAddr = rDel2LoH2dPayload.addr
  def rDel2LoBusAddrTag = rDel2LoBusAddr(loBusCacheCfg.tagRange)
  def rDel2LoBusAddrSet = rDel2LoBusAddr(loBusCacheCfg.setRange)

  val rSavedLoH2dPayload = (
    Reg(cloneOf(rLoH2dPayload))
    init(rLoH2dPayload.getZero)
  )
  def rSavedLoBusAddr = rSavedLoH2dPayload.addr
  def rSavedLoBusAddrTag = rSavedLoBusAddr(loBusCacheCfg.tagRange)
  def rSavedLoBusAddrSet = rSavedLoBusAddr(loBusCacheCfg.setRange)

  val haveHit
  //(
  //  //useDel2: Bool,
  //) 
  = (
    rdLineAttrs.fire
    //&& rdLineAttrs.tag === rDel2LoBusAddrTag
    && LcvFastCmpEq(
      left=rdLineAttrs.tag,
      right=(
        //Mux[UInt](useDel2, rDel2LoBusAddrTag, rLoBusAddrTag)
        //rLoBusAddrTag
        rDel2LoBusAddrTag
        //loH2dPopStm.addr(loBusCacheCfg.tagRange)
        //RegNext(
        //  RegNext(rSavedLoBusAddrTag, init=rSavedLoBusAddrTag.getZero),
        //  init=rSavedLoBusAddrTag.getZero,
        //)
      ),
      cmpEqIo=null,
    )._1
  )
  //--------
  val rHiH2dBurstCnt = (
    Vec.fill(2)(
      Reg(UInt(loBusCfg.burstCntWidth bits))
      init(0x0)
    )
  )
  val rHiD2hBurstCnt = (
    Reg(UInt(loBusCfg.burstCntWidth bits))
    init(0x0)
  )
  //--------
  //def doAllLineRamsReadSync(
  //  busAddr: UInt,
  //): Unit = {
  //  doLineWordRamReadSync(busAddr=busAddr)
  //  doLineAttrsRamReadSync(busAddr=busAddr)
  //}
  def doLineWordRamReadSync(
    busAddr: UInt,
    setEn: Boolean=false,
  ): Unit = {
    if (setEn) {
      lineWordRam.io.rdEn := (
        //True
        RegNext(
          next=loH2dPopStm.valid,
          init=False,
        )
      )
    }
    //rMyLineWordRamRdEn := True
    lineWordRam.io.rdAddr := {
      //(busAddr >> myLineRamAddrRshift)
      println(
        s"test info: busAddr("
        + s"${busAddr.high} downto ${myLineRamAddrRshift}"
        + s")"
      )
      (
        (
          busAddr(busAddr.high downto myLineRamAddrRshift)
        )
        .resize(lineWordRam.io.rdAddr.getWidth)
      )
    }
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
    lineWord match {
      case Some(lineWord) => {
        lineWordRam.io.wrData := lineWord
      }
      case None => {
        lineWordRam.io.wrData := (
          //myD2hBus.sendData.data.asBits
          io.hiBus.d2hBus.data
        )
      }
    }
    byteEn match {
      case Some(byteEn) => {
        lineWordRam.io.wrByteEn := byteEn.asBits
      }
      case None => {
        lineWordRam.io.wrByteEn := (
          B(lineWordRam.io.wrByteEn.getWidth bits, default -> True)
        )
      }
    }
  }
  def doLineAttrsRamReadSync(
    busAddr: UInt,
    setEn: Boolean=false,
  ): Unit = {
    if (setEn) {
      lineAttrsRam.io.rdEn := (
        //True
        RegNext(
          next=loH2dPopStm.valid,
          init=False,
        )
      )
    }
    //lineAttrsRam.io.rdEn := True
    //rMyLineAttrsRamRdEn := True
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
    lineAttrsRam.io.wrData := lineAttrs
  }
  //--------
  doLineAttrsRamReadSync(
    busAddr=(
      //rLoH2dPayload.addr
      //io.loBus.h2dBus.addr
      loH2dPopStm.addr
    ),
    setEn=true,
  )
  doLineWordRamReadSync(
    busAddr={
      //rLoH2dPayload.addr
      //println(
      //  s"testificate: ${io.loBus.h2dBus.addr.bitsRange}"
      //)
      //io.loBus.h2dBus.addr
      loH2dPopStm.addr
    },
    setEn=true,
  )
  doLineAttrsRamWrite(
    busAddr=(
      loH2dPopStm.addr
      //RegNext(
      //  rDel2LoBusAddr
      //)
      //rSavedLoH2dPayload.addr
      //rSavedLoBusAddr
    ),
    setEn=false,
  )
  doLineWordRamWrite(
    busAddr=(
      loH2dPopStm.addr
      //RegNext(
      //  rDel2LoBusAddr
      //)
      //rSavedLoH2dPayload.addr
      //rSavedLoBusAddr
    ),
    lineWord=Some(
      //rSavedLoH2dPayload.data
      //RegNext(
      //  rDel2LoH2dPayload.data
      //)
      loH2dPopStm.data
    ),
    byteEn=Some(
      //rSavedLoH2dPayload.byteEn
      //RegNext(
      //  rDel2LoH2dPayload.byteEn
      //)
      loH2dPopStm.byteEn
    ),
    setEn=false,
  )
  //--------
  //val myHadHiH2dFinish = Bool()
  //val rHadHiH2dFinish = Reg(Bool(), init=False)
  //val stickyHadHiH2dFinish = (myHadHiH2dFinish || rHadHiH2dFinish)
  //myHadHiH2dFinish := False
  //when (myHadHiH2dFinish) {
  //  rHadHiH2dFinish := True
  //}

  //val myHadHiD2hFinish = Bool()
  //val rHadHiD2hFinish = Reg(Bool(), init=False)
  //val stickyHadHiD2hFinish = (myHadHiD2hFinish || rHadHiD2hFinish)
  //myHadHiD2hFinish := False
  //when (myHadHiD2hFinish) {
  //  rHadHiD2hFinish := True
  //}
  //--------
  val hiH2dFifo = (
    StreamFifo(
      dataType=(
        //UInt(loBusCacheCfg.wordWidth bits)
        LcvBusH2dPayload(cfg.hiBusCfg)
      ),
      depth=loBusCacheCfg.lineSizeWords,
      latency=2,
      forFMax=true,
    )
  )
  // It appears we *do not* need `hiD2hFifo`,
  // though perhaps only for the time being!
  //val hiD2hFifo = (
  //  StreamFifo(
  //    dataType=UInt(loBusCacheCfg.wordWidth bits),
  //    depth=loBusCacheCfg.lineSizeWords,
  //    latency=2,
  //    forFMax=true,
  //  )
  //)
  //--------
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
  def loBusCfg = cfg.loBusCfg
  def hiBusCfg = cfg.hiBusCfg
  //--------
  require(
    //hiBusCfg.maxBurstSizeMinus1 == (64 / 4) - 1,
    //cfg.loBusCacheCfg.lineSizeBytes == 64,
    cfg.loBusCacheCfg.lineSizeBytes == loBusCfg.burstCntMaxNumBytes,
    s"(Perhaps only temporarily), "
    + s"we need the number of bytes per cache line to be "
    + s"${loBusCfg.burstCntMaxNumBytes}. "
    + s"This permits an easier-to-implement design "
    + s"for `io.hiBus` bursting."
  )
  //--------
  val io = LcvBusCacheIo(cfg=cfg)

  io.loBus.d2hBus.valid := (
    False
    //RegNext(io.loBus.d2hBus.valid, init=False)
  )
  io.loBus.d2hBus.payload := (
    RegNext(io.loBus.d2hBus.payload, init=io.loBus.d2hBus.payload.getZero)
  )

  //--------
  val base = LcvBusCacheBaseArea(io=io)
  //io.loBus.h2dBus.ready.setAsReg() init(False)
  //io.loBus.h2dBus.ready := False
  //loH2dPopStm.ready := False
  //--------
  def lineWordRam = base.lineWordRam
  def lineAttrsRam = base.lineAttrsRam
  def rdLineWord = base.rdLineWord
  def rdLineAttrs = base.rdLineAttrs
  def wrLineAttrs = base.wrLineAttrs
  //def loH2dFifo = base.loH2dFifo
  def loH2dCacheMissFifoThing = base.loH2dCacheMissFifoThing
  def loH2dPopStm = base.loH2dPopStm
  def hiH2dFifo = base.hiH2dFifo
  def rLoH2dPayload = base.rLoH2dPayload
  def rDel2LoH2dPayload = base.rDel2LoH2dPayload
  def rSavedLoH2dPayload = base.rSavedLoH2dPayload
  def rHiH2dBurstCnt = base.rHiH2dBurstCnt
  def rHiD2hBurstCnt = base.rHiD2hBurstCnt

  def rHadLoH2dFinish = base.rHadLoH2dFinish
  def rHadLoD2hFinish = base.rHadLoD2hFinish
  def rHadHiH2dFinish = base.rHadHiH2dFinish
  def rHadHiD2hFinish = base.rHadHiD2hFinish

  val rHiH2dValid = Reg(Bool(), init=False)
  val rHiH2dPayload = (
    Reg(cloneOf(hiH2dFifo.io.push.payload))
    init(hiH2dFifo.io.push.payload.getZero)
  )
  val rHiD2hReady = Reg(Bool(), init=False)
  hiH2dFifo.io.push.valid := rHiH2dValid
  hiH2dFifo.io.push.payload := rHiH2dPayload
  io.hiBus.d2hBus.ready := rHiD2hReady

  rHiH2dPayload.byteEn := (
    U(hiBusCfg.byteEnWidth bits, default -> True)
  )
  io.hiBus.h2dBus <-/< hiH2dFifo.io.pop
  //--------
  //--------
  object State extends SpinalEnum(
    defaultEncoding=(
      //binarySequential
      binaryOneHot
    )
  ) {
    val
      IDLE,
      LOAD_HIT_LO_BUS_STALL,
      //LOAD_HIT,
      STORE_HIT,
      //STORE_HIT_MAYBE_POST,
      SEND_LINE_TO_HI_BUS_PIPE_3,
      SEND_LINE_TO_HI_BUS_PIPE_2,
      SEND_LINE_TO_HI_BUS_PIPE_1,
      SEND_LINE_TO_HI_BUS,
      RECV_LINE_FROM_HI_BUS_PIPE_1,
      RECV_LINE_FROM_HI_BUS,
      RECV_LINE_FROM_HI_BUS_POST_3,
      RECV_LINE_FROM_HI_BUS_POST_2,
      RECV_LINE_FROM_HI_BUS_POST_1,
      RECV_LINE_FROM_HI_BUS_POST
      //POST_LOAD_MISS_PIPE_2,
      //POST_LOAD_MISS_PIPE_1,
      //LOAD_MISS_POST,
      //STORE_MISS_POST
      //NON_CACHED_BUS_ACCESS,
        // this will probably be covered with an `LcvBusSlicer`
      = newElement();
  }

  val rState = (
    Reg(State())
    init(State.IDLE)
  )

  when (
    //(RegNext(rState) init(State.IDLE)) === State.IDLE
    rose(rState === State.STORE_HIT)
  ) {
    wrLineAttrs := RegNext(rdLineAttrs, init=rdLineAttrs.getZero)
    wrLineAttrs.dirty := True
    lineAttrsRam.io.wrEn := True
    lineWordRam.io.wrEn := True
    //rSavedLoH2dPayload := (
    //  //rLoH2dPayload
    //  //rDel2LoH2dPayload
    //)
  }

  rHiD2hReady := False
  //val rPastStateWasStoreHit = (
  //  (RegNext(rState === State.STORE_HIT) init(False))
  //)
  val rTempBurstAddr = (
    //RegNext(
      rSavedLoH2dPayload.burstAddr(
        someBurstCnt=rHiD2hBurstCnt,
        incrBurstCnt=false,
      )
    //)
    //init(0x0)
  )
  val tempBurstCntCmpEq = (
    rHiD2hBurstCnt
    === rSavedLoH2dPayload.addr(
      rHiD2hBurstCnt.high + log2Up(hiBusCfg.dataWidth / 8)
      downto log2Up(hiBusCfg.dataWidth / 8)
    )
  )
  //val rSeenStateIdle = Reg(Bool(), init=False)
  def doPopLoH2dFifo(): Unit = {
    when (
      loH2dPopStm.valid
      && !loH2dPopStm.isWrite
    ) {
      //io.loBus.h2dBus.ready := True
      loH2dPopStm.ready := True
    }
  }
  //lineWordRam.io.rdEn := (
  //  rState === State.IDLE
  //  || rState === State.STORE_HIT
  //)
  //lineAttrsRam.io.rdEn := (
  //  rState === State.IDLE
  //  || rState === State.STORE_HIT
  //)
  val maybeHaveHitStateCond = Vec[Bool]({
    List(
      (
        rState === State.IDLE
        || rState === State.LOAD_HIT_LO_BUS_STALL
        || rState === State.STORE_HIT
        || rState === State.RECV_LINE_FROM_HI_BUS_POST
      ),
      (
        rState === State.IDLE
        || rState === State.LOAD_HIT_LO_BUS_STALL
        || rState === State.STORE_HIT
        || rState === State.RECV_LINE_FROM_HI_BUS_POST_1
      )
    )
  })
  switch (rState) {
    is (State.IDLE) {
      base.myFifoThingCacheMiss := False
      //--------
      //io.loBus.h2dBus.ready := True
      io.loBus.d2hBus.valid := False
      //io.loBus.d2hBus.data := rdLineWord
      //when (io.loBus.h2dBus.valid) {
      //  rSavedLoH2dPayload := (
      //    //rDel2LoH2dPayload
      //    //rLoH2dPayload
      //    io.loBus.h2dBus.payload
      //  )
      //}

      rHiH2dValid := False
      //rHiD2hReady := False
      rHiH2dPayload.burstCnt := hiBusCfg.maxBurstSizeMinus1
      rHiH2dPayload.burstFirst := True
      rHiH2dPayload.burstLast := False

      rHadLoH2dFinish := False
      rHadLoD2hFinish := False
      rHadHiH2dFinish := False
      rHadHiD2hFinish := False

      //when (rPastStateWasStoreHit) {
        //rSavedLoH2dPayload := (
        //  //rDel2LoH2dPayload
        //  rLoH2dPayload
        //)
      //} otherwise {
      //  rSavedLoH2dPayload := (
      //    rDel2LoH2dPayload
      //    //rLoH2dPayload
      //  )
      //}
      rHiH2dBurstCnt.foreach(item => item := 0x0)
      rHiD2hBurstCnt := 0x0
      //lineWordRam.io.rdEn := True
      //lineAttrsRam.io.rdEn := True
      //base.rMyLineWordRamRdEn := True
      //base.rMyLineAttrsRamRdEn := True
      //base.doLineAttrsRamReadSync(
      //  busAddr=(
      //    //rLoH2dPayload.addr
      //    io.loBus.h2dBus.addr
      //  ),
      //)
      //base.doLineWordRamReadSync(
      //  busAddr={
      //    //rLoH2dPayload.addr
      //    //println(
      //    //  s"testificate: ${io.loBus.h2dBus.addr.bitsRange}"
      //    //)
      //    io.loBus.h2dBus.addr
      //  },
      //)
      doPopLoH2dFifo()
      //--------
      when (
        //loH2dPopStm.valid
        //&& 
        //RegNext(lineWordRam.io.rdEn, init=False)
        (
          //loH2dPopStm.valid
          //&& (
          //  (
          //    RegNext(
          //      next=(
          //        loH2dPopStm.valid
          //        && maybeHaveHitStateCond(0)
          //      ),
          //      init=False
          //    )
          //    && 
              RegNext(
                RegNext(
                  next=(
                    //io.loBus.h2dBus.valid
                    loH2dPopStm.valid
                    //&& maybeHaveHitStateCond(1)
                    //&& rState === State.IDLE
                    ////loH2dPopStm.valid
                    ////loH2dPopStm.fire
                    //&& maybeHaveHitStateCond
                  ),
                  init=False
                ),
                init=False
              )
          //  )
          //  //|| !loH2dPopStm.isWrite
          //)
        )
        ////io.loBus.h2dBus.valid
        ////&& RegNext(
        ////  io.loBus.h2dBus.valid,
        ////  init=False,
        ////)
        ////&&
        //RegNext(
        //  RegNext(
        //    (
        //      io.loBus.h2dBus.valid
        //      //&& rState === State.IDLE
        //    ),
        //    init=False
        //  ),
        //  init=False
        //)
      ) {
        rSavedLoH2dPayload := (
          rDel2LoH2dPayload
          //rLoH2dPayload
        )
        //io.loBus.h2dBus.ready := True
        //io.loBus.d2hBus.data := rdLineWord

        switch (
          base.haveHit
          ## rdLineAttrs.dirty
          ## (
            //rLoH2dPayload.isWrite
            rDel2LoH2dPayload.isWrite
            //loH2dPopStm.isWrite
          )
        ) {
          is (M"00-") {
            // cache miss, and line isn't dirty
            rState := State.RECV_LINE_FROM_HI_BUS_PIPE_1
            //io.loBus.h2dBus.ready := True

            //--------
            // BEGIN: debug `State.SEND_LINE_TO_HI_BUS...`
            //rState := State.SEND_LINE_TO_HI_BUS_PIPE_3
            // END: debug `State.SEND_LINE_TO_HI_BUS...`
            //--------
            //loH2dCacheMissFifoThing.io.cacheMiss := True
            base.myFifoThingCacheMiss := True
            //loH2dPopStm.ready := True
            //rSavedLoH2dPayload := rDel2LoH2dPayload
          }
          is (M"01-") {
            // cache miss, and line is dirty 
            rState := State.SEND_LINE_TO_HI_BUS_PIPE_3
            //loH2dCacheMissFifoThing.io.cacheMiss := True
            base.myFifoThingCacheMiss := True
            //loH2dPopStm.ready := True
            //rSavedLoH2dPayload := rDel2LoH2dPayload
          }
          is (M"1-0") {
            // cache hit, and we have a load 
            // Here we try to reduce the number of cycles for a load hit
            // to 2 total (but allowing pipelining load hits!)
            // to allow for the `SnowHouse` module
            // to have a better best-case number of cycles 
            // than the original `LcvStallIo`-based `SnowHouseDataCache`
            //io.loBus.h2dBus.ready := True
            loH2dPopStm.ready := True
            io.loBus.d2hBus.valid := True
            io.loBus.d2hBus.data := rdLineWord
            //rSavedLoH2dPayload := rDel2LoH2dPayload
            when (!io.loBus.d2hBus.fire) {
              rState := (
                State.LOAD_HIT_LO_BUS_STALL
                //State.LOAD_HIT
              )
            } otherwise {
              //rSeenStateIdle := False
            }
          }
          default {
            // cache hit, and we have a store
            rState := State.STORE_HIT
            //loH2dPopStm.ready := True
            //io.loBus.h2dBus.ready := True
            //rSavedLoH2dPayload := rDel2LoH2dPayload
            //when (
            //  RegNext(
            //    (rState =/= State.IDLE),
            //    init=False
            //  )
            //) {
            //  rSavedLoH2dPayload := io.loBus.h2dBus.payload
            //} otherwise {
            //  rSavedLoH2dPayload := rDel2LoH2dPayload
            //}
          }
        }
      }
      //--------
    }
    is (State.LOAD_HIT_LO_BUS_STALL) {
      io.loBus.d2hBus.valid := True
      when (io.loBus.d2hBus.fire) {
        base.myFifoThingCacheMiss := False
        rState := State.IDLE
        //rSeenStateIdle := False
      }
    }
    //is (State.LOAD_HIT) {
    //  io.loBus.d2hBus.valid := True

    //  when (rose(rState === State.LOAD_HIT)) {
    //    io.loBus.d2hBus.data := (
    //      //RegNext(
    //        rdLineWord//, init=rdLineWord.getZero
    //      //)
    //    )
    //  }
    //  when (io.loBus.d2hBus.fire) {
    //    rState := State.IDLE
    //  }
    //}
    is (State.STORE_HIT) {
      when (rose(rState === State.STORE_HIT)) {
        //io.loBus.h2dBus.ready := True
        loH2dPopStm.ready := True
      }
      io.loBus.d2hBus.valid := True
      //--------
      // BEGIN: moved to outside the state machine
      //when (
      //  //(RegNext(rState) init(State.IDLE)) === State.IDLE
      //  rose(rState === State.STORE_HIT)
      //) {
      //  wrLineAttrs := RegNext(rdLineAttrs, init=rdLineAttrs.getZero)
      //  wrLineAttrs.dirty := True
      //  lineAttrsRam.io.wrEn := True
      //  lineWordRam.io.wrEn := True
      //}
      // END: moved to outside the state machine
      //--------
      when (io.loBus.d2hBus.fire) {
        base.myFifoThingCacheMiss := False
        rState := State.IDLE
        //rSeenStateIdle := False
      }
    }
    //is (State.SEND_LINE_TO_HI_BUS_START_BURST) {
    //  rHiH2dValid := True
    //  rHiH2dPayload.addr := (
    //    rSavedLoH2dPayload.burstAddr(rHiH2dBurstCnt)
    //  )
    //  rHiH2dPayload.data := rdLineWord
    //  rHiH2dPayload.isWrite := True
    //  rHiH2dPayload.burstFirst := True
    //  rHiH2dPayload.burstCnt := hiBusCfg.maxBurstSizeMinus1

    //  when (hiH2dFifo.io.push.fire) {
    //    //rHiH2dValid := False
    //    rHiH2dBurstCnt := rHiH2dBurstCnt + 1
    //    rHiH2dPayload.burstFirst := False
    //    rHiH2dPayload.burstCnt := rHiH2dPayload.burstCnt - 1

    //    rState := State.SEND_LINE_TO_HI_BUS
    //  }
    //}
    is (State.SEND_LINE_TO_HI_BUS_PIPE_3) {
      rState := State.SEND_LINE_TO_HI_BUS_PIPE_2
      base.doLineWordRamReadSync(
        busAddr=rSavedLoH2dPayload.burstAddr(
          someBurstCnt=rHiH2dBurstCnt(0),
          incrBurstCnt=true,
        )
      )
    }
    is (State.SEND_LINE_TO_HI_BUS_PIPE_2) {
      rState := State.SEND_LINE_TO_HI_BUS_PIPE_1
      base.doLineWordRamReadSync(
        busAddr=rSavedLoH2dPayload.burstAddr(
          someBurstCnt=rHiH2dBurstCnt(0),
          incrBurstCnt=true,
        )
      )
      rHiH2dPayload.addr := (
        Cat(
          False,
          RegNext(rdLineAttrs.tag, init=rdLineAttrs.tag),
          base.rSavedLoBusAddrSet,
          U(s"${log2Up(loBusCfg.burstCntMaxNumBytes)}'d0"),
        ).asUInt
      )
    }
    is (State.SEND_LINE_TO_HI_BUS_PIPE_1) {
      rState := State.SEND_LINE_TO_HI_BUS
      base.doLineWordRamReadSync(
        busAddr=rSavedLoH2dPayload.burstAddr(
          someBurstCnt=rHiH2dBurstCnt(0),
          incrBurstCnt=true,
        )
      )
      rHiH2dValid := True

      //rHiH2dPayload.burstCnt := hiBusCfg.maxBurstSizeMinus1
      //rHiH2dPayload.burstFirst := True
      //rHiH2dPayload.burstLast := False

      rHiH2dPayload.addr := rHiH2dPayload.burstAddr(
        someBurstCnt=rHiH2dBurstCnt(1),
        incrBurstCnt=true,
      )
      rHiH2dPayload.data := rdLineWord
      rHiH2dPayload.byteEn := (
        U(rHiH2dPayload.byteEn.getWidth bits, default -> True)
      )
      rHiH2dPayload.isWrite := True
      rHiH2dPayload.src := rSavedLoH2dPayload.src
    }
    is (State.SEND_LINE_TO_HI_BUS) {
      base.doLineWordRamReadSync(
        busAddr=rSavedLoH2dPayload.burstAddr(
          someBurstCnt=rHiH2dBurstCnt(0),
          incrBurstCnt=false,
        )
      )
      rHiH2dPayload.burstFirst := False

      when (rHiH2dBurstCnt(0).orR) {
        // an OR reduce checks for non-zero
        rHiH2dBurstCnt(0) := rHiH2dBurstCnt(0) + 1
      }
      when (RegNext(!rHiH2dBurstCnt(0).orR, init=False)) {
        lineWordRam.io.rdEn := False
      }
      rHiH2dPayload.addr := rHiH2dPayload.burstAddr(
        someBurstCnt=rHiH2dBurstCnt(1),
        incrBurstCnt=false,
      )
      rHiH2dPayload.data := rdLineWord
      when (rHiH2dBurstCnt(1).orR) {
        rHiH2dBurstCnt(1) := rHiH2dBurstCnt(1) + 1
      }
      when (
        RegNext(
          next=(
            !rHiH2dBurstCnt(0).orR
            && (!(rHiH2dBurstCnt(1) + 2).orR)
          ),
          init=False
        )
      ) {
        rHiH2dPayload.burstLast := True
      }
      when (
        //RegNext(
        //  next=(
        //    RegNext(
        //      next=(
        //        !rHiH2dBurstCnt(0).orR
        //        && (!(rHiH2dBurstCnt(1) + 2).orR)
        //      ),
        //      init=False
        //    )
        //  ),
        //  init=False
        //)
        rHiH2dPayload.burstLast
      ) {
        rHiH2dValid := False
        //rState := State.RECV_LINE_FROM_HI_BUS_PIPE_1
        rHadHiH2dFinish := True
        rHiH2dPayload.burstLast := False
      }
      when (io.hiBus.d2hBus.valid) {
        rHiD2hReady := True
        rHadHiD2hFinish := True
      }
      when (rHadHiH2dFinish && rHadHiD2hFinish) {
        rState := State.RECV_LINE_FROM_HI_BUS_PIPE_1
      }
    }
    is (State.RECV_LINE_FROM_HI_BUS_PIPE_1) {
      rHadHiH2dFinish := False
      rHadHiD2hFinish := False

      rHiH2dPayload.burstFirst := True
      rHiH2dPayload.burstLast := False
      rHiH2dPayload.burstCnt := hiBusCfg.maxBurstSizeMinus1

      rHiH2dPayload.isWrite := False
      rHiH2dPayload.src := rSavedLoH2dPayload.src
      rHiH2dPayload.addr := rSavedLoH2dPayload.burstAddr(
        someBurstCnt=rHiH2dBurstCnt(1).getZero,
        incrBurstCnt=false
      )
      rHiD2hBurstCnt := 0x0
      //rHiD2hReady
      when (RegNext(!hiH2dFifo.io.occupancy.orR, init=False)) {
        rHiH2dValid := True
        rState := State.RECV_LINE_FROM_HI_BUS
      }

      wrLineAttrs.tag := (
        rSavedLoH2dPayload.addr(cfg.loBusCacheCfg.tagRange)
      )
      //wrLineAttrs.valid := True
      wrLineAttrs.dirty := (
        //False
        // if it's a store, this line should be marked dirty!
        //rSavedBusSendData.accKind.asBits(1)
        rSavedLoH2dPayload.isWrite
      )
      base.doLineAttrsRamWrite(
        busAddr=rSavedLoH2dPayload.addr,
        setEn=true,
      )
    }
    is (State.RECV_LINE_FROM_HI_BUS) {
      rHiH2dValid := False
      when (io.hiBus.d2hBus.valid) {
        rHiD2hReady := True
      }
      when (io.hiBus.d2hBus.fire) {
        rHiD2hBurstCnt := rHiD2hBurstCnt + 1
        when (tempBurstCntCmpEq) {
          io.loBus.d2hBus.data := (io.hiBus.d2hBus.data)
        }
        when (tempBurstCntCmpEq && rSavedLoH2dPayload.isWrite) {
          base.doLineWordRamWrite(
            busAddr=rTempBurstAddr,
            lineWord=Some(rSavedLoH2dPayload.data),
            byteEn=Some(rSavedLoH2dPayload.byteEn),
            setEn=true,
          )
        } otherwise {
          base.doLineWordRamWrite(
            busAddr=rTempBurstAddr,
            lineWord=Some((io.hiBus.d2hBus.data)),
            byteEn=None,
            setEn=true,
          )
        }
        when ((io.hiBus.d2hBus.burstLast)) {
          rState := State.RECV_LINE_FROM_HI_BUS_POST_3
        }
      }
    }
    is (State.RECV_LINE_FROM_HI_BUS_POST_3) {
      //when (rSeenStateIdle) {
      //  io.loBus.h2dBus.ready := True
      //}
      //when (io.loBus.h2dBus.fire) {
      //  rSeenStateIdle := False
      //}
      //io.loBus.h2dBus.ready := True
      //--------
      //loH2dPopStm.ready := True
      //--------
      rState := State.RECV_LINE_FROM_HI_BUS_POST_2
      //io.loBus.d2hBus.valid := True
      //when (io.loBus.d2hBus.fire) {
      //  rState := (
      //    //State.IDLE
      //    State.RECV_LINE_FROM_HI_BUS_POST_2
      //  )
      //  //rSeenStateIdle := False
      //}
      //when (!rSavedLoH2dPayload.isWrite) {
      //  rState := State.LOAD_MISS_POST
      //} otherwise {
      //  rState := State.STORE_MISS_POST
      //}
      base.myFifoThingCacheMiss := False
    }
    is (State.RECV_LINE_FROM_HI_BUS_POST_2) {
      //rState := State.RECV_LINE_FROM_HI_BUS_POST_1
      //io.loBus.d2hBus.valid := True
      //when (io.loBus.d2hBus.fire) {
      //  rState := State.RECV_LINE_FROM_HI_BUS_POST_1
      //  //rSeenStateIdle := False
      //}
      rState := State.RECV_LINE_FROM_HI_BUS_POST_1
    }
    is (State.RECV_LINE_FROM_HI_BUS_POST_1) {
      rState := State.RECV_LINE_FROM_HI_BUS_POST
      when (
        //io.loBus.h2dBus.valid
        loH2dPopStm.valid
        && !loH2dPopStm.isWrite
      ) {
        //io.loBus.h2dBus.ready := True
        loH2dPopStm.ready := True
      }
    }
    is (State.RECV_LINE_FROM_HI_BUS_POST) {
      //base.myFifoThingCacheMiss := False
      rState := State.IDLE
      when (
        RegNext(
          //io.loBus.h2dBus.fire
          loH2dPopStm.fire,
          init=False
        )
        && (
          //io.loBus.h2dBus.valid
          loH2dPopStm.valid
        )
      ) {
        //io.loBus.h2dBus.ready := True
        loH2dPopStm.ready := True
      }
    }
    //is (State.LOAD_MISS_POST) {
    //}
    //is (State.STORE_MISS_POST) {
    //}
  }
  //--------
  wrLineAttrs.valid := True
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
  val nonCoherentDataCache = (
    cfg.haveNonCoherentDataCache
  ) generate (
    LcvBusNonCoherentDataCache(cfg=cfg)
  )
  val coherentInstrCache = (
    cfg.haveCoherentInstrCache
  ) generate (
    LcvBusCoherentInstrCache(cfg=cfg)
  )
  val coherentDataCache = (
    cfg.haveCoherentDataCache
  ) generate (
    LcvBusCoherentDataCache(cfg=cfg)
  )
  val sharedCache = (
    cfg.haveSharedCache
  ) generate (
    LcvBusSharedCache(cfg=cfg)
  )
  //if (cfg.haveNonCoherentInstrCache) {
  //} else 
  if (cfg.haveNonCoherentDataCache) {
    io <> nonCoherentDataCache.io
  } else if (cfg.haveCoherentInstrCache) {
    io <> coherentInstrCache.io
  } else if (cfg.haveCoherentDataCache) {
    io <> coherentDataCache.io
  } else if (cfg.haveSharedCache) {
    io <> sharedCache.io
  } else {
    require(
      false
    )
  }
  //--------
}

case class LcvBusNonCoherentDataCacheWithSdramCtrl(
  //cacheCfg: LcvBusCacheBusPairConfig,
  sdramCtrlCfg: LcvBusSdramCtrlConfig,
) extends Component {
  //--------
  //val io = new Bundle {
  //  val sdram = LcvBusSdramIo(
  //    cfg=sdramCtrlCfg
  //  )
  //}
  //val io = LcvBusSdramCtrlIo(cfg=sdramCtrlCfg)
  val io = LcvBusSdramIo(cfg=sdramCtrlCfg)
  //--------
  val cacheCfg = (
    LcvBusCacheBusPairConfig(
      mainCfg=LcvBusMainConfig(
        dataWidth=32,
        addrWidth=32,
        allowBurst=false,
        burstAlwaysMaxSize=false,
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
  val testerCfg = (
    LcvBusDeviceRamTesterConfig(
      busCfg=cacheCfg.loBusCfg,
      kind=LcvBusDeviceRamTesterKind.NoBurstRandDataSemiRandAddr(
        optDirectMappedCacheSetRangeHi=Some({
          //cacheCfg.loBusCacheCfg.lineSizeBytes
          //cacheCfg.loBusCacheCfg.setWidth
          //cacheCfg.loBusCacheCfg.setWidth
          //cacheCfg.loBusCacheCfg.tagRange.end - 1
          val tempSetRangeHi = cacheCfg.loBusCacheCfg.setRange.start
          println(
            s"tempSetRangeHi: ${tempSetRangeHi}"
          )
          tempSetRangeHi
        })
      )
    )
  )
  //--------
  val myCache = LcvBusCache(cfg=cacheCfg)
  val myCacheTester = LcvBusDeviceRamTester(cfg=testerCfg)
  val mySdramCtrl = LcvBusSdramCtrl(cfg=sdramCtrlCfg)
  io <> mySdramCtrl.io.sdram
  //val myBusMem = {
  //  val tempDepth = (
  //    //cacheCfg.loBusCacheCfg.depthWords * 2
  //    //cacheCfg.loBusCacheCfg.depthWords * 4
  //    cacheCfg.loBusCacheCfg.depthWords * 16
  //  )
  //  //val tempDepth = 128
  //  LcvBusMem(
  //    //cfg=cacheCfg.loBusCfg
  //    cfg=LcvBusMemConfig(
  //      busCfg=cacheCfg.hiBusCfg,
  //      depth=tempDepth,
  //      initBigInt=Some({
  //        //Array.fill(tempDepth)(BigInt(0))
  //        val tempArr = new ArrayBuffer[BigInt]()
  //        for (idx <- 0 until tempDepth) {
  //          tempArr += BigInt(idx)
  //        }
  //        tempArr
  //      }),
  //    )
  //  )
  //}
  myCache.io.loBus << myCacheTester.io.busVec.head
  myCache.io.hiBus >> mySdramCtrl.io.bus
  //myCache.io.hiBus >> myBusMem.io.bus

  //myBusMem.io.bus <> myCacheTester.io.busVec.head
  //myBusMem.io.bus.h2dBus.mainBurstInfo := (
  //  myBusMem.io.bus.h2dBus.mainBurstInfo.getZero
  //)
  //myBusMem.io.bus.h2dBus.mainNonBurstInfo := (
  //  myCacheTester.io.busVec.head.h2dBus.mainNonBurstInfo
  //)
  //myBusMem.io.bus.h2dBus.valid := myCacheTester.io.busVec.head.h2dBus.valid
  //myCacheTester.io.busVec.head.h2dBus.ready := myBusMem.io.bus.h2dBus.ready 
  //myCacheTester.io.busVec.head.d2hBus << myBusMem.io.bus.d2hBus
  //--------
}

case class LcvBusNonCoherentDataCacheSimDut(
  clkRate: HertzNumber,
) extends Component {
  //--------
  val io = new Bundle {
  }
  //--------
  val myCacheAndTester = LcvBusNonCoherentDataCacheWithSdramCtrl(
    sdramCtrlCfg=LcvBusSdramCtrlConfig(
      clkRate=clkRate,
      useAltddioOut=false,
    )
  )
  val mySdram = as4c32m16sb()
  //mySdram.io.DQ <> mySdramCtrl.io.sdram.dq
  //mySdram.io.A := mySdramCtrl.io.sdram.a
  //mySdram.io.DQML := mySdramCtrl.io.sdram.dqml
  //mySdram.io.DQMH := mySdramCtrl.io.sdram.dqmh
  //mySdram.io.BA := mySdramCtrl.io.sdram.ba
  //mySdram.io.nCS := mySdramCtrl.io.sdram.nCs
  //mySdram.io.nWE := mySdramCtrl.io.sdram.nWe
  //mySdram.io.nRAS := mySdramCtrl.io.sdram.nRas
  //mySdram.io.nCAS := mySdramCtrl.io.sdram.nCas
  //mySdram.io.CLK := mySdramCtrl.io.sdram.clk
  //mySdram.io.CKE := mySdramCtrl.io.sdram.cke
  mySdram.io.DQ <> myCacheAndTester.io.dq
  mySdram.io.A := myCacheAndTester.io.a
  mySdram.io.DQML := myCacheAndTester.io.dqml
  mySdram.io.DQMH := myCacheAndTester.io.dqmh
  mySdram.io.BA := myCacheAndTester.io.ba
  mySdram.io.nCS := myCacheAndTester.io.nCs
  mySdram.io.nWE := myCacheAndTester.io.nWe
  mySdram.io.nRAS := myCacheAndTester.io.nRas
  mySdram.io.nCAS := myCacheAndTester.io.nCas
  mySdram.io.CLK := myCacheAndTester.io.clk
  mySdram.io.CKE := myCacheAndTester.io.cke
  //--------
}

object LcvBusNonCoherentDataCacheSim extends App {
  def clkRate = 100.0 MHz
  val simSpinalConfig = SpinalConfig(
    defaultClockDomainFrequency=FixedFrequency(clkRate)
  )
  SimConfig
    .withConfig(config=simSpinalConfig)
    .withFstWave
    .compile(
      //LcvSdramSimDut(clkRate=clkRate)
      LcvBusNonCoherentDataCacheSimDut(clkRate=clkRate)
    )
    .doSim { dut =>
      dut.clockDomain.forkStimulus(period=10)
      def simNumClks = (
        200000
      )
      for (idx <- 0 until simNumClks) {
        dut.clockDomain.waitRisingEdge()
      }
      simSuccess()
    }
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
      cfg=LcvBusCacheBusPairConfig(
        mainCfg=LcvBusMainConfig(
          dataWidth=32,
          addrWidth=32,
          allowBurst=false,
          burstAlwaysMaxSize=false,
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
