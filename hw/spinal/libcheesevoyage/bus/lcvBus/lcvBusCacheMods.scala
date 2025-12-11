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

  val lineWordRamCfg = RamSdpPipeConfig(
    wordType=UInt(wordWidth bits),
    depth=depthWords,
    optIncludeWrByteEn=true,
    initBigInt=Some(Array.fill(depthWords)(BigInt(0))),
    arrRamStyleAltera=cfg.loBusCacheCfg.lineWordMemRamStyleAltera,
    arrRamStyleXilinx=cfg.loBusCacheCfg.lineWordMemRamStyleXilinx,
  )
  val lineWordRam = RamSdpPipe(
    cfg=lineWordRamCfg
  )
  val lineAttrsRamCfg = RamSdpPipeConfig(
    wordType=LcvBusCacheLineAttrs(cfg=loBusCfg),
    depth=depthLines,
    initBigInt=Some(Array.fill(depthLines)(BigInt(0))),
    arrRamStyleAltera=cfg.loBusCacheCfg.lineAttrsMemRamStyleAltera,
    arrRamStyleXilinx=cfg.loBusCacheCfg.lineAttrsMemRamStyleXilinx,
  )
  val lineAttrsRam = RamSdpPipe(
    cfg=lineAttrsRamCfg
  )

  val rdLineWord = UInt(wordWidth bits)
  rdLineWord.assignFromBits(lineWordRam.io.rdData.asBits)

  val rdLineAttrs = LcvBusCacheLineAttrs(cfg=loBusCfg)
  rdLineAttrs.assignFromBits(lineAttrsRam.io.rdData.asBits)

  val wrLineWord = UInt(wordWidth bits)
  val wrLineAttrs = LcvBusCacheLineAttrs(cfg=loBusCfg)
  wrLineAttrs := RegNext(wrLineAttrs, init=wrLineAttrs.getZero)

  //val rMyLineAttrsRamRdEn = Reg(Bool(), init=False)
  //lineAttrsRam.io.rdEn := rMyLineAttrsRamRdEn
  lineAttrsRam.io.rdEn := True

  //val rMyLineWordRamRdEn = Reg(Bool(), init=False)
  //lineWordRam.io.rdEn := rMyLineWordRamRdEn
  lineWordRam.io.rdEn := True

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
  val rSendBurstCnt = (
    Reg(UInt(loBusCfg.burstCntWidth bits))
    init(0x0)
  )
  val rRecvBurstCnt = (
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
  ): Unit = {
    lineWordRam.io.rdEn := True
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
        lineWordRam.io.wrData := lineWord.asBits
      }
      case None => {
        lineWordRam.io.wrData := (
          //myD2hBus.sendData.data.asBits
          io.hiBus.d2hBus.data.asBits
        )
      }
    }
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
  }
  def doLineAttrsRamReadSync(
    busAddr: UInt,
  ): Unit = {
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
    lineAttrsRam.io.wrData := lineAttrs.asBits
  }
  //--------
  doLineAttrsRamReadSync(
    busAddr=(
      //rLoBusH2dPayload.addr
      io.loBus.h2dBus.addr
    ),
  )
  doLineWordRamReadSync(
    busAddr={
      //rLoBusH2dPayload.addr
      println(
        s"testificate: ${io.loBus.h2dBus.addr.bitsRange}"
      )
      io.loBus.h2dBus.addr
    },
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
  //--------
  val mySeenHiH2dFinish = Bool()
  val rSeenHiH2dFinish = Reg(Bool(), init=False)
  val stickySeenHiH2dFinish = (mySeenHiH2dFinish || rSeenHiH2dFinish)
  mySeenHiH2dFinish := False
  when (mySeenHiH2dFinish) {
    rSeenHiH2dFinish := True
  }

  val mySeenHiD2hFinish = Bool()
  val rSeenHiD2hFinish = Reg(Bool(), init=False)
  val stickySeenHiD2hFinish = (mySeenHiD2hFinish || rSeenHiD2hFinish)
  mySeenHiD2hFinish := False
  when (mySeenHiD2hFinish) {
    rSeenHiD2hFinish := True
  }
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
    cfg.loBusCacheCfg.lineSizeBytes == 64,
    s"Temporarily, we need the number of bytes per cache line to be 64. "
    + s"This permits an easier-to-implement design "
    + s"for `io.hiBus` bursting."
  )
  //--------
  val io = LcvBusCacheIo(cfg=cfg)

  io.loBus.d2hBus.valid := (
    //False
    RegNext(io.loBus.d2hBus.valid, init=False)
  )
  io.loBus.d2hBus.payload := (
    RegNext(io.loBus.d2hBus.payload, init=io.loBus.d2hBus.payload.getZero)
  )

  io.loBus.h2dBus.ready.setAsReg() init(False)
  //--------
  val base = LcvBusCacheBaseArea(io=io)
  //--------
  def hiH2dFifo = base.hiH2dFifo

  val rHiH2dFifoPushValid = Reg(Bool(), init=False)
  val rHiH2dFifoPushPayload = (
    Reg(cloneOf(hiH2dFifo.io.push.payload))
    init(hiH2dFifo.io.push.payload.getZero)
  )
  val rHiD2hReady = Reg(Bool(), init=False)
  hiH2dFifo.io.push.valid := rHiH2dFifoPushValid
  hiH2dFifo.io.push.payload := rHiH2dFifoPushPayload
  //hiH2dFifo.io.pop.ready := rHiD2hReady
  io.hiBus.d2hBus.ready := rHiD2hReady
  //rHiH2dFifoPushValid.setAsReg() init(False)
  //rHiH2dFifoPushPayload.payload.setAsReg()
  //  .init(rHiH2dFifoPushPayload.payload.getZero)
  //rHiD2hReady.setAsReg() init(False)

  //rHiH2dFifoPushPayload.byteEn := (
  //  U(hiBusCfg.byteEnWidth bits, default -> True)
  //)
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
      STORE_HIT,
      //SEND_LINE_TO_HI_BUS_START_BURST,
      SEND_LINE_TO_HI_BUS_PIPE_2,
      SEND_LINE_TO_HI_BUS_PIPE_1,
      SEND_LINE_TO_HI_BUS,
      //RECV_LINE_FROM_HI_BUS_START_BURST,
      RECV_LINE_FROM_HI_BUS,
      POST_LOAD_MISS,
      POST_STORE_MISS
      //NON_CACHED_BUS_ACCESS,
        // this will probably be covered with an `LcvBusSlicer`
      = newElement();
  }

  val rState = (
    Reg(State())
    init(State.IDLE)
  )

  switch (rState) {
    is (State.IDLE) {
      //--------
      io.loBus.h2dBus.ready := True
      io.loBus.d2hBus.valid := False
      io.loBus.d2hBus.data := base.rdLineWord

      rHiH2dFifoPushValid := False
      rHiD2hReady := False
      rHiH2dFifoPushPayload.burstCnt := hiBusCfg.maxBurstSizeMinus1
      rHiH2dFifoPushPayload.burstFirst := True
      rHiH2dFifoPushPayload.burstLast := False

      base.rSavedLoBusH2dPayload := base.rDel2LoBusH2dPayload
      base.rSendBurstCnt := 0x0
      base.rRecvBurstCnt := 0x0
      //--------
      switch (
        base.haveHit
        ## base.rdLineAttrs.dirty
        ## base.rDel2LoBusH2dPayload.isWrite
      ) {
        is (M"00-") {
          // cache miss, and line isn't dirty
          rState := (
            //State.RECV_LINE_FROM_HI_BUS_START_BURST
            State.RECV_LINE_FROM_HI_BUS
          )
        }
        is (M"01-") {
          // cache miss, and line is dirty 
          rState := (
            //State.SEND_LINE_TO_HI_BUS_START_BURST
            //State.SEND_LINE_TO_HI_BUS
            //State.SEND_LINE_TO_HI_BUS_PIPE_1
            State.SEND_LINE_TO_HI_BUS_PIPE_2
          )
        }
        is (M"1-0") {
          // cache hit, and we have a load 
          // Here we try to reduce the number of cycles for a load hit to 2
          // to allow for the `SnowHouse` module to have a better best-case
          // number of cycles than the original `LcvStallIo`-based
          // `SnowHouseDataCache`
          io.loBus.d2hBus.valid := True
          //io.loBus.d2hBus.data := base.rdLineWord
          when (!io.loBus.d2hBus.fire) {
            rState := State.LOAD_HIT_LO_BUS_STALL
          }
        }
        default {
          // cache hit, and we have a store
          rState := State.STORE_HIT
        }
      }
      //--------
    }
    is (State.LOAD_HIT_LO_BUS_STALL) {
      io.loBus.d2hBus.valid := True
      //io.loBus.d2hBus.data := 
      when (io.loBus.d2hBus.fire) {
        rState := State.IDLE
      }
    }
    is (State.STORE_HIT) {
      io.loBus.d2hBus.valid := True
      base.wrLineAttrs := (
        RegNext(
          next=base.rdLineAttrs,
          init=base.rdLineAttrs.getZero,
        )
      )
      base.wrLineAttrs.dirty := True
      base.lineAttrsRam.io.wrEn := True
      base.lineWordRam.io.wrEn := True

      when (io.loBus.d2hBus.fire) {
        rState := State.IDLE
      }
    }
    //is (State.SEND_LINE_TO_HI_BUS_START_BURST) {
    //  rHiH2dFifoPushValid := True
    //  rHiH2dFifoPushPayload.addr := (
    //    base.rSavedLoBusH2dPayload.burstAddr(base.rSendBurstCnt)
    //  )
    //  rHiH2dFifoPushPayload.data := base.rdLineWord
    //  rHiH2dFifoPushPayload.isWrite := True
    //  rHiH2dFifoPushPayload.burstFirst := True
    //  rHiH2dFifoPushPayload.burstCnt := hiBusCfg.maxBurstSizeMinus1

    //  when (hiH2dFifo.io.push.fire) {
    //    //rHiH2dFifoPushValid := False
    //    base.rSendBurstCnt := base.rSendBurstCnt + 1
    //    rHiH2dFifoPushPayload.burstFirst := False
    //    rHiH2dFifoPushPayload.burstCnt := rHiH2dFifoPushPayload.burstCnt - 1

    //    rState := State.SEND_LINE_TO_HI_BUS
    //  }
    //}
    is (State.SEND_LINE_TO_HI_BUS_PIPE_2) {
      rState := State.SEND_LINE_TO_HI_BUS_PIPE_1
      //base.doLineWordRamReadSync(
      //  busAddr=0
      //)
    }
    is (State.SEND_LINE_TO_HI_BUS_PIPE_1) {
      rState := State.SEND_LINE_TO_HI_BUS
      //base.doLineWordRamReadSync(
      //  busAddr=0
      //)
    }
    is (State.SEND_LINE_TO_HI_BUS) {
      //base.doLineWordRamReadSync(
      //  busAddr=(
      //    base.rSavedLoBusH2dPayload.burstAddr(base.rSendBurstCnt)
      //  )
      //)
      when (rHiH2dFifoPushPayload.burstFirst) {
        rHiH2dFifoPushValid := True
      }
      rHiH2dFifoPushPayload.addr := (
        base.rSavedLoBusH2dPayload.burstAddr(base.rSendBurstCnt)
      )
      when (hiH2dFifo.io.push.fire) {
        rHiH2dFifoPushPayload.burstFirst := False
        rHiH2dFifoPushPayload.burstCnt := rHiH2dFifoPushPayload.burstCnt - 1
        when (base.rSendBurstCnt =/= 0) {
          base.rSendBurstCnt := base.rSendBurstCnt + 1
        }

        when (rHiH2dFifoPushPayload.burstCnt === 0x1) {
          rHiH2dFifoPushPayload.burstLast := True
        }

        when (rHiH2dFifoPushPayload.burstLast) {
          rHiH2dFifoPushValid := False
          rHiH2dFifoPushPayload.burstLast := False
          base.mySeenHiH2dFinish := True
        }
      }
      when (hiH2dFifo.io.pop.fire) {
        base.mySeenHiD2hFinish := True
      }
      when (base.stickySeenHiH2dFinish && base.stickySeenHiD2hFinish) {
        rState := State.RECV_LINE_FROM_HI_BUS
        base.rSeenHiH2dFinish := False
        base.rSeenHiD2hFinish := False
      }
    }
    //is (State.RECV_LINE_FROM_HI_BUS_START_BURST) {
    //  rHiH2dFifoPushValid := True

    //  base.rRecvBurstCnt := 0x0

    //  rHiH2dFifoPushPayload.addr := (
    //    base.rSavedLoBusH2dPayload.burstAddr(base.rRecvBurstCnt.getZero)
    //  )
    //  rHiH2dFifoPushPayload.isWrite := False
    //  rHiH2dFifoPushPayload.burstFirst := True
    //  rHiH2dFifoPushPayload.burstCnt := hiBusCfg.maxBurstSizeMinus1

    //  when (hiH2dFifo.io.push.fire) {
    //    io.hiBus
    //    rHiH2dFifoPushValid := False
    //    rState := State.RECV_LINE_FROM_HI_BUS
    //  }
    //}
    is (State.RECV_LINE_FROM_HI_BUS) {
      //when (
      //  hiH2dFifo.io.pop.valid
      //  && hiH2dFifo.io.pop.burstLast
      //) {
      //  rHiD2hReady := True
      //  when (!base.rSavedLoBusH2dPayload.isWrite) {
      //    rState := State.POST_LOAD_MISS
      //  } otherwise {
      //    rState := State.POST_STORE_MISS
      //  }
      //}
    }
    is (State.POST_LOAD_MISS) {
    }
    is (State.POST_STORE_MISS) {
    }
  }
  //--------
  base.wrLineAttrs.valid := True
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

case class LcvBusNonCoherentDataCacheSimDut(
  //cacheCfg: LcvBusCacheBusPairConfig,
) extends Component {
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
  //val testerCfg = (
  //  LcvBusDeviceRamTesterConfig(
  //    busCfg=cacheCfg.loBusCfg,
  //    kind=LcvBusDeviceRamTesterKind.NoBurstRandDataSemiRandAddr(
  //      optDirectMappedCacheTagLsbMinus1=Some(
  //        //cacheCfg.loBusCacheCfg.lineSizeBytes
  //        cacheCfg.loBusCacheCfg.setWidth
  //      )
  //    )
  //  )
  //)
  //--------
  val io = new Bundle {
  }
  //--------
  val myCache = LcvBusCache(cfg=cacheCfg)
  //val myCacheTester = LcvBusDeviceRamTester(cfg=testerCfg)
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
      LcvBusNonCoherentDataCacheSimDut()
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
