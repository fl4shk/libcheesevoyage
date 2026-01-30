package libcheesevoyage.bus.lcvBus

//import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._
import spinal.lib.io._

import libcheesevoyage.general._

case class LcvXorShift16Config(
  //xsWidthMul: Int=2,
  xsInitS2d: Seq[Seq[BigInt]],
  //xsVecSize: Int=1,
  //includeInpCnt: Boolean=false,
) {
  def xsWidthMul = xsInitS2d.head.size
  require(xsWidthMul >= 1)
  def xsVecSize = xsInitS2d.size
  require(xsVecSize >= 1)
  def myXsWidth = 16 * xsWidthMul

  for (idx <- 1 until xsInitS2d.size) {
    require(
      xsInitS2d(idx).size == xsInitS2d.head.size,
      s"It is required that every element of `xsInitS2d` "
      + s"is the same size. "
      + s"This index is the first size different from `head`: "
      + s"index:${idx} size:${xsInitS2d(idx).size}"
    )
  }

  def myCntMax = 2
  def myCntWidth = log2Up(myCntMax + 1) + 1
}

case class LcvXorShift16Io(
  cfg: LcvXorShift16Config,
) extends Bundle {

  //val inpCnt = (
  //  cfg.includeInpCnt
  //) generate (
  //  in(SInt(cfg.myCntWidth bits))
  //)
  val outpXs = out(
    Vec.fill(cfg.xsVecSize)(
      UInt(cfg.myXsWidth bits)
    )
  )
}

case class LcvXorShift16(
  cfg: LcvXorShift16Config
) extends Component {
  //--------
  val io = LcvXorShift16Io(cfg=cfg)
  //--------
  def myXsWidth = cfg.myXsWidth
  def myCntWidth = cfg.myCntWidth
  def myCntMax = cfg.myCntMax
  val rCnt = (
    Reg(SInt(cfg.myCntWidth bits)) init(cfg.myCntMax)
  )
  val rXsVec = (
    Vec.fill(cfg.xsVecSize)(
      Vec.fill(cfg.xsWidthMul)(
        Vec.fill(3)(
          Reg(UInt(32 bits))
        )
      )
    )
  )
  for (idx <- 0 until rXsVec.size) {
    for (jdx <- 0 until rXsVec(idx).size) {
      //println(
      //  s"debug: sizes: ${rXsVec.size} ${rXsVec(idx).size}"
      //)
      def rXs = rXsVec(idx)(jdx)
      rXs.foreach(item => item.init(cfg.xsInitS2d(idx)(jdx)))

      io.outpXs(idx)(
        (jdx + 1) * 16 - 1
        downto jdx * 16
      ) := rXs(0)(15 downto 0)

      rXs(2) := (
        (rXs(0) ^ ((rXs(0) << 7)(rXs(0).bitsRange))).resize(
          myXsWidth
        )
      )
      rXs(1) := (
        (rXs(2) ^ (rXs(2) >> 9).resize(myXsWidth)).resize(myXsWidth)
      )
      when (rCnt.msb) {
        rXs(0) := (
          (rXs(1) ^ ((rXs(1) << 8)(rXs(0).bitsRange))).resize(myXsWidth)
        )
      }
    }
  }
  when (!rCnt.msb) {
    rCnt := rCnt - 1
  } otherwise {
    rCnt := myCntMax
  }
}

//case class LcvXorShift16ArrayIo(
//  xsCfg: LcvXorShift16Config,
//  arrSize: Int,
//) extends Bundle {
//  //val io = Vec.fill(arrSize)(
//  //  LcvXorShift16Io(cfg=xsCfg)
//  //)
//}
//case class LcvXorShift16Array(
//  xsCfg: LcvXorShift16Config,
//  arrSize: Int,
//) extends Component {
//}


sealed trait LcvBusDeviceRamTesterKind {
  def _hasRandData: Boolean  
  def _hasRandAddr: Boolean
  def _isCoherent: Boolean
  //def optCacheLineSizeBytes: Option[Int]
  def _optDirectMappedCacheSetRangeHi: Option[Int]
  //def _busVecSize: Int
  //assert(
  //  _busVecSize > 0
  //)
}
object LcvBusDeviceRamTesterKind {
  case class NoBurstRandDataSemiRandAddr(
    optDirectMappedCacheSetRangeHi: Option[Int],
    //busVecSize: Int=1,
  ) extends LcvBusDeviceRamTesterKind {
    def _hasRandData: Boolean = true
    def _hasRandAddr: Boolean = true
    def _isCoherent: Boolean = false
    def _optDirectMappedCacheSetRangeHi: Option[Int] = (
      optDirectMappedCacheSetRangeHi
    )
    //def _busVecSize: Int = busVecSize
  }
  case object DualBurstRandDataSemiRandAddr
  extends LcvBusDeviceRamTesterKind {
    def _hasRandData: Boolean = true
    def _hasRandAddr: Boolean = true
    def _isCoherent: Boolean = false
    def _optDirectMappedCacheSetRangeHi: Option[Int] = None
    //def _busVecSize: Int = 1
  }
  case object DualBurstRandData extends LcvBusDeviceRamTesterKind {
    def _hasRandData: Boolean = true
    def _hasRandAddr: Boolean = false
    def _isCoherent: Boolean = false
    def _optDirectMappedCacheSetRangeHi: Option[Int] = None
    //def _busVecSize: Int = 1
  }
  case object NoBurstRandData extends LcvBusDeviceRamTesterKind {
    def _hasRandData: Boolean = true
    def _hasRandAddr: Boolean = false
    def _isCoherent: Boolean = false
    def _optDirectMappedCacheSetRangeHi: Option[Int] = None
    //def _busVecSize: Int = 1
  }
}
case class LcvBusDeviceRamTesterConfig(
  busCfg: LcvBusConfig,
  kind: LcvBusDeviceRamTesterKind,
  testDataRamStyleAltera: String="M10K",
  testDataRamStyleXilinx: String="block",
) {
  def busVecSize = (
    busCfg.cacheCfg match {
      case Some(cacheCfg) => (
        cacheCfg.numCpus
      )
      case None => (
        1
      )
    }
  )
}

case class LcvBusDeviceRamTesterIo(
  cfg: LcvBusDeviceRamTesterConfig,
) extends Bundle {
  val busVec = (
    Vec.fill(
      cfg.busVecSize
    )(
      master(LcvBusIo(cfg=cfg.busCfg))
    )
  )
}

private[libcheesevoyage] case class LcvBusDeviceRamTesterNonCoherent(
  cfg: LcvBusDeviceRamTesterConfig,
) extends Component {
  //--------
  def busCfg = cfg.busCfg
  busCfg.cacheCfg match {
    case Some(cacheCfg) => {
      require(!cacheCfg.coherent)
    }
    case None => {
    }
  }
  //--------
  val io = master(LcvBusIo(cfg=busCfg))
  //val io = LcvBusDeviceRamTesterIo(cfg=cfg)
  //--------
  val rH2dValid = Reg(Bool(), init=False)
  val nextH2dMainBurstInfo = LcvBusH2dPayloadMainBurstInfo(cfg=busCfg)
  val rH2dMainBurstInfo = (
    RegNext(nextH2dMainBurstInfo, init=nextH2dMainBurstInfo.getZero)
  )
  nextH2dMainBurstInfo := rH2dMainBurstInfo
  val rH2dPayload = {
    val temp = Reg(LcvBusH2dPayload(cfg=busCfg))
    temp.init(temp.getZero)
    temp
  }
  if (busCfg.allowBurst) {
    rH2dPayload.mainBurstInfo := nextH2dMainBurstInfo
  }
  val myH2dSendFifo = (
    cfg.kind._hasRandAddr
  ) generate (
    StreamFifo(
      dataType=LcvBusH2dPayload(cfg=busCfg),
      depth=(busCfg.maxBurstSizeMinus1 + 1),
      latency=2,
      forFMax=true,
    )
  )
  val myD2hReady = Bool()
  myD2hReady := False
  val myD2hSendData = (
    //RegNext(
    //  next=io.d2hBus.payload,
    //  init=io.d2hBus.payload.getZero,
    //)
    io.d2hBus.payload
  )

  if (!cfg.kind._hasRandAddr) {
    io.h2dBus.valid := rH2dValid
    io.h2dBus.payload := rH2dPayload
  } else {
    io.h2dBus << myH2dSendFifo.io.pop
    myH2dSendFifo.io.push.valid := rH2dValid
    myH2dSendFifo.io.push.payload := rH2dPayload
  }
  io.d2hBus.ready := myD2hReady //rD2hReady

  object StateIncrAddr
  extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      WRITE_START,
      WRITE_WAIT_TXN,
      READ_START,
      READ_WAIT_TXN,
      DO_COMPARE_TEST_DATA,
      FAILED_TEST
      = newElement();
  }
  val rStateIncrAddr = (
    !cfg.kind._hasRandAddr
  ) generate(
    Reg(StateIncrAddr())
    init(StateIncrAddr.WRITE_START)
  )
  object StateRandAddr
  extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      WRITE_START,
      WRITE_WAIT_TXN_PIPE_3,
      WRITE_WAIT_TXN_PIPE_2,
      WRITE_WAIT_TXN_PIPE_1,
      WRITE_WAIT_TXN,
      READ_START_PIPE_3,
      READ_START_PIPE_2,
      READ_START_PIPE_1,
      READ_START,
      READ_WAIT_TXN,
      DO_COMPARE_TEST_DATA,
      FAILED_TEST
      = newElement();
  }
  val rStateRandAddr = (
    cfg.kind._hasRandAddr
  ) generate (
    Reg(StateRandAddr())
    init(StateRandAddr.WRITE_START)
  )

  val rRamWrAddrCnt = (
    Reg(UInt(log2Up(cfg.busCfg.maxBurstSizeMinus1 + 1) bits))
    init(0x0)
  )
  val rRamRdAddrCnt = (
    Reg(UInt(log2Up(cfg.busCfg.maxBurstSizeMinus1 + 1) bits))
    init(0x0)
  )
  val rBurstCntVec = (
    Vec.fill(2)(
      Reg(UInt(log2Up(cfg.busCfg.maxBurstSizeMinus1 + 1) bits))
      init(0x0)
    )
  )
  def rH2dBurstCnt = rBurstCntVec.head
  def rD2hBurstCnt = rBurstCntVec.last

  val rHadH2dFinish = (
    Reg(Bool(), init=False)
  )
  val rHadD2hFinish = (
    Reg(Bool(), init=False)
  )

  val myPrngCfg = LcvXorShift16Config(
    xsInitS2d={
      val tempA2d = new ArrayBuffer[Seq[BigInt]]()
      val outerSize = (
        cfg.busCfg.maxBurstSizeMinus1 + 1
        + (if (cfg.kind._hasRandAddr) (1) else (0))
      )
      val innerSize = 2
      for (idx <- 0 until outerSize) {
        val tempArr = new ArrayBuffer[BigInt]()
        for (jdx <- 0 until innerSize) {
          tempArr += (
            BigInt(idx) * BigInt(innerSize) + BigInt(jdx) + 1
          )
          //println(
          //  s"${tempArr.last}"
          //)
        }
        tempA2d += tempArr
      }
      tempA2d
    }
  )
  val myPrng = LcvXorShift16(cfg=myPrngCfg)
  def myNumTests = (
    // 8
    4
  )
  def myMaxBaseAddr = (
    //8
    //0x10
    //myNumTests << 2 // NOTE: this was for the burst-less accessess
    //(myNumTests * (hostBusCfg.maxBurstSizeMinus1 + 1)) << 2
    //(hostBusCfg.maxBurstSizeMinus1 + 1) << 2
    (cfg.busCfg.maxBurstSizeMinus1 + 1) << 2
  )
  def myTestDataVecOuterSize = (
    if (cfg.kind._hasRandAddr) (
      //if (cfg.kind._optDirectMappedCacheSetRangeHi != None) (
      //  
      //) else (
        //2
        //4
      //)
      16
      //1
    ) else (
      1
    )
  )
  case class TestDataElem(
  ) extends Bundle {
    // these are reversed from how I'd prefer so we can use `io.wrByteEn`
    // for `myTestDataRamArr`
    //val recvData = UInt(myPrngCfg.myXsWidth bits)
    val sendData = UInt(myPrngCfg.myXsWidth bits)
    //val valid = Bool()
    //val flags = UInt(8 bits)
    //def valid = flags.lsb
  }

  val rTestData = (
    //!cfg.kind._hasRandAddr
    true
  ) generate (
    //Vec.fill(myTestDataVecOuterSize)(
      Vec.fill(
        //(myMaxBaseAddr >> 2) + 1
        //(myMaxBaseAddr >> 1) // intentionally leave some always-zero elements
        //myNumTests
        //hostBusCfg.maxBurstSizeMinus1 + 1
        cfg.busCfg.maxBurstSizeMinus1 + 1
      )({
        val temp = Reg(
          TestDataElem()
          //Flow(
          //  //Vec.fill(hostBusCfg.maxBurstSizeMinus1 + 1)(
          //    Vec.fill(2)(
          //      UInt(myPrngCfg.myXsWidth bits)
          //    )
          //  //)
          //)
        )
        temp.init(temp.getZero)
        KeepAttribute(temp)
      })
    //)
  )
  val rRecvCmpDataIncrAddr = (
    !cfg.kind._hasRandAddr
  ) generate (
    Vec.fill(
      cfg.busCfg.maxBurstSizeMinus1 + 1
    )({
      val temp = Reg(Flow(UInt(myPrngCfg.myXsWidth bits)))
      temp.init(temp.getZero)
      temp
    })
  )
  case class RecvCmpDataRandAddr(
  ) extends Bundle {
    val recvDataVec = Vec.fill(
      cfg.busCfg.maxBurstSizeMinus1 + 1
    )(
      UInt(myPrngCfg.myXsWidth bits)
    )
    val validVec = Vec.fill(
      myTestDataVecOuterSize
    )(
      Bool()
    )
  }
  val rRecvCmpDataRandAddr = (
    cfg.kind._hasRandAddr
  ) generate {
    val temp = Reg(RecvCmpDataRandAddr())
    temp.init(temp.getZero)
    temp
  }
  def myTestDataRamDepthInner = cfg.busCfg.maxBurstSizeMinus1 + 1
  def myTestDataRamDepthOuter = (1 << log2Up(myTestDataVecOuterSize))
  val myTestDataRam/*Arr*/ = (
    cfg.kind._hasRandAddr
  ) generate {
    val depthInner = myTestDataRamDepthInner
    val depthOuter = myTestDataRamDepthOuter
    val depth = depthInner * depthOuter
    //Array.fill(myTestDataVecOuterSize)(
      RamSdpPipe(
        cfg=RamSdpPipeConfig(
          wordType=(
            //Flow(
            //  Vec.fill(2)(
            //    UInt(myPrngCfg.myXsWidth bits)
            //  )
            //)
            TestDataElem()
          ),
          depth=depth,
          optIncludeWrByteEn=false,
          initBigInt=Some(Array.fill(depth)(BigInt(0))),
          arrRamStyleAltera=cfg.testDataRamStyleAltera,
          arrRamStyleXilinx=cfg.testDataRamStyleXilinx,
        )
      )
      //FpgacpuRamSimpleDualPort(
      //  wordType=Flow(
      //    Vec.fill(2)(
      //      UInt(myPrngCfg.myXsWidth bits)
      //    )
      //  ),
      //  depth=depth,
      //  initBigInt=Some(Array.fill(depth)(BigInt(0))),
      //  arrRamStyle=cfg.testDataRamStyleAltera,
      //)
    //)
  }
  def myPrngDirectMappedCacheRangeWidth = (
    cfg.kind._optDirectMappedCacheSetRangeHi match {
      case Some(setRangeHi) => (
        // use `setRangeHi` so that both cache hits and cache misses
        // will be tested for direct-mapped caches
        //setRangeHi + 2 //- 1
        //setRangeHi + 1
        setRangeHi //- 1
        //cfg.busCfg.burstCntWidth
        //+ log2Up(busCfg.dataWidth / 8)
      )
      case None => (
        cfg.busCfg.burstCntWidth
        + log2Up(busCfg.dataWidth / 8)
      )
    }
  )
  def myPrngTestDataRange = (
    myPrngDirectMappedCacheRangeWidth - 1 + log2Up(myTestDataVecOuterSize)
    downto (
      //log2Up(rTestDataVec.size)
      //cfg.busCfg.burstCntWidth
      myPrngDirectMappedCacheRangeWidth
    )
  )
  println(
    s"myPrngDirectMappedCacheRangeWidth: "
      + s"${myPrngDirectMappedCacheRangeWidth} "
    + s"myPrngTestDataRange: ${myPrngTestDataRange}"
  )
  //--------
  //val noBurstRandDataSemiRandAddrArea = (
  //  cfg.kind match {
  //    case LcvBusDeviceRamTesterKind.NoBurstRandDataSemiRandAddr(_) => (
  //      true
  //    )
  //    case _ => (
  //      false
  //    )
  //  }
  //) generate (new Area {
  //})
  val myRandDataSemiRandAddrArea = (
    (cfg.kind match {
      case LcvBusDeviceRamTesterKind.NoBurstRandDataSemiRandAddr(_) => (
        true
      )
      case _ => (
        false
      )
    })
    || cfg.kind == LcvBusDeviceRamTesterKind.DualBurstRandDataSemiRandAddr
    //cfg.kind match {
    //  case LcvBusDeviceRamTesterKind.DualBurstRandDataSemiRandAddr(_) => {
    //    true
    //  }
    //  case _ => {
    //    false
    //  }
    //}
  ) generate (new Area {
    //val haveBursts = (
    //  cfg.kind == LcvBusDeviceRamTesterKind.DualBurstRandDataSemiRandAddr
    //)
    // BEGIN: state machine that does maximum-byte-amount bus bursts
    // starting from semi-random addresses

    //def rTestData = rTestDataVec.head
    //def myHaveSemiRandAddr = (
    //  cfg.kind == LcvBusDeviceRamTesterKind.DualBurstRandDataSemiRandAddr
    //)

    val nextSavedTestDataVecIdx = (
      UInt(log2Up(myTestDataVecOuterSize) bits)
    )
    val rSavedTestDataVecIdx = (
      //Reg(UInt(busCfg.addrWidth bits))
      //init(0x0)
      RegNext(
        next=nextSavedTestDataVecIdx,
        init=nextSavedTestDataVecIdx.getZero
      )
    )
    nextSavedTestDataVecIdx := rSavedTestDataVecIdx
    def doInitH2dSendAddr(): Unit = {
      nextSavedTestDataVecIdx := (
        myPrng.io.outpXs.last(
          myPrngTestDataRange
        )
        //0x0
      )
      rH2dPayload.addr(
        myPrngTestDataRange
      ) := (
        //nextSavedTestDataVecIdx.resize(rH2dPayload.addr.getWidth)
        nextSavedTestDataVecIdx
      )
    }

    //myTestDataRamArr.foreach(item => {
    //  item.io.rdEn := False
    //  item.io.rdAddr := 0x0
    //  item.io.wrEn := False
    //  item.io.wrAddr := 0x0
    //  item.io.wrData := 0x0
    //  //item.io.wrByteEn := 0x0
    //})
    myTestDataRam.io.rdEn := False
    myTestDataRam.io.rdAddr := 0x0
    myTestDataRam.io.wrEn := False
    myTestDataRam.io.wrAddr := 0x0
    myTestDataRam.io.wrData := myTestDataRam.io.wrData.getZero
    //myTestDataRam.io.wrByteEn := 0x0
    //val wrTestDataElem = TestDataElem()
    //wrTestDataElem := wrTestDataElem.getZero

    def getRamIdxRange(
      ramIdx: UInt
    ) = {
      val ram = myTestDataRam
      (
        ram.io.wrAddr.high
        downto ram.io.wrAddr.getWidth - ramIdx.getWidth
        //myPrngDirectMappedCacheRangeWidth - 1
        //downto myPrngDirectMappedCacheRangeWidth - ramIdx.getWidth
      )
    }
    def doWriteTestDataElem(
      ramIdx: UInt,
      wrAddr: Option[UInt],
      wrData: TestDataElem,
      //wrByteEn: Option[Bits]=None,
    ): Unit = {
      val ram = myTestDataRam
      ram.io.wrEn := True
      wrAddr match {
        case Some(wrAddr) => {
          ram.io.wrAddr := Cat(ramIdx, wrAddr).asUInt
          wrAddr := wrAddr + 1
        }
        case None => {
          //ram.io.wrAddr := ram.io.wrAddr.getZero
          ram.io.wrAddr(getRamIdxRange(ramIdx)) := ramIdx
        }
      }
      ram.io.wrData := wrData
    }
    def doInitWriteTestDataElem(
    ): Unit = {
      doWriteTestDataElem(
        ramIdx=rSavedTestDataVecIdx,
        wrAddr=Some(rRamWrAddrCnt),
        wrData={
          val temp = TestDataElem()
          temp.sendData := myPrng.io.outpXs(rRamWrAddrCnt.resized)
          temp
        }
      )
    }

    def doPipe2ReadTestDataElem(
      ramIdx: UInt,
      rdAddr: UInt
    ): Unit = {
      val ram = myTestDataRam
      ram.io.rdAddr := Cat(ramIdx, rdAddr).asUInt
      rdAddr := rdAddr + 1
    }
    def doPipe1ReadTestDataElem(
      ramIdx: UInt,
      rdAddr: UInt
    ): Unit = {
      val ram = myTestDataRam
      ram.io.rdEn := True
      ram.io.rdAddr := Cat(ramIdx, rdAddr).asUInt
      rdAddr := rdAddr + 1
    }
    def doFinishReadTestDataElem(
      //ramIdx: UInt,
      rdData: TestDataElem,
    ): Unit = {
      val ram = myTestDataRam
      //val ramIdx = rSavedTestDataVecIdx
      rdData.assignFromBits(ram.io.rdData.asBits)
    }

    val rdTestDataElem = TestDataElem()
    rdTestDataElem := RegNext(rdTestDataElem, init=rdTestDataElem.getZero)
    val rMyTestDataIdx = (
      RegNext(
        RegNext(rRamRdAddrCnt, init=rRamRdAddrCnt.getZero),
        init=rRamRdAddrCnt.getZero,
      )
    )
    val rSeenLastRamWrAddrCnt = Reg(Bool(), init=False)

    switch (rStateRandAddr) {
      is (StateRandAddr.WRITE_START) {
        rHadH2dFinish := False
        rHadD2hFinish := False

        doWriteTestDataElem(
          ramIdx=(
            //myPrng.io.outpXs.last(myPrngTestDataRange)
            nextSavedTestDataVecIdx
          ),
          wrAddr=None,
          wrData={
            val temp = TestDataElem()
            temp.sendData := myPrng.io.outpXs(0x0)
            temp
          }
        )
        rRecvCmpDataRandAddr.recvDataVec.foreach(item => {
          item := item.getZero
        })
        rRecvCmpDataRandAddr.validVec(nextSavedTestDataVecIdx) := True

        rSeenLastRamWrAddrCnt := False
        rRamWrAddrCnt := 0x1 
        rRamRdAddrCnt := 0x0

        //rH2dValid := True
        rH2dValid := False
        rH2dPayload.data := (
          myPrng.io.outpXs(0)
        )
        doInitH2dSendAddr()
        //rH2dPayload.byteEn := U(
        //  rH2dPayload.byteEn.getWidth bits, default -> True
        //)
        //rH2dPayload.byteSize := (
        //  0x0
        //)
        rH2dPayload.haveFullWord := (
          True
          //False
        )
        rH2dPayload.isWrite := True
        rH2dPayload.src := 0x0

        nextH2dMainBurstInfo.burstCnt := cfg.busCfg.maxBurstSizeMinus1
        nextH2dMainBurstInfo.burstFirst := True
        nextH2dMainBurstInfo.burstLast := False

        rH2dBurstCnt := (
          //0x0
          0x1
        )
        rD2hBurstCnt := 0x0

        rStateRandAddr := StateRandAddr.WRITE_WAIT_TXN_PIPE_3
      }
      is (StateRandAddr.WRITE_WAIT_TXN_PIPE_3) {
        rStateRandAddr := StateRandAddr.WRITE_WAIT_TXN_PIPE_2
        doInitWriteTestDataElem()
        doPipe2ReadTestDataElem(
          ramIdx=rSavedTestDataVecIdx,
          rdAddr=rRamRdAddrCnt
        )
      }
      is (StateRandAddr.WRITE_WAIT_TXN_PIPE_2) {
        rStateRandAddr := StateRandAddr.WRITE_WAIT_TXN_PIPE_1
        //rRamWrAddrCnt := 0x2
        //rRamWrAddrCnt := rRamWrAddrCnt + 1
        //doWriteTestDataElem(
        //  ramIdx=rSavedTestDataVecIdx,
        //  wrAddr=rRamWrAddrCnt,
        //  wrData={
        //    val temp = TestDataElem()
        //    temp.sendData := myPrng.io.outpXs(rRamWrAddrCnt)
        //    temp.recvData := temp.recvData.getZero
        //    temp.valid := True
        //    temp
        //  }
        //)
        doInitWriteTestDataElem()
        doPipe1ReadTestDataElem(
          ramIdx=rSavedTestDataVecIdx,
          rdAddr=rRamRdAddrCnt,
        )
        //doPipe2ReadTestDataElem(
        //  ramIdx=rSavedTestDataVecIdx,
        //  rdAddr=rRamRdAddrCnt
        //)
      }
      is (StateRandAddr.WRITE_WAIT_TXN_PIPE_1) {
        rStateRandAddr := StateRandAddr.WRITE_WAIT_TXN
        //rRamWrAddrCnt := rRamWrAddrCnt + 1
        doInitWriteTestDataElem()
        doPipe1ReadTestDataElem(
          ramIdx=rSavedTestDataVecIdx,
          rdAddr=rRamRdAddrCnt,
        )
        rH2dValid := True
        rHadH2dFinish := False
        rHadD2hFinish := False
      }
      is (StateRandAddr.WRITE_WAIT_TXN) {
        //def rTestData = (
        //  rTestDataVec(rSavedTestDataVecIdx)
        //)
        //if (!busCfg.allowBurst) {
        //  doInitWriteTestDataElem()
        //} else {
          when (
            //(~rRamWrAddrCnt).orR
            (rRamWrAddrCnt + 1)(rRamWrAddrCnt.bitsRange)
            === rRamWrAddrCnt.getZero
          ) {
            rSeenLastRamWrAddrCnt := True
          }
          when (
            //rRamWrAddrCnt > 0
            !rSeenLastRamWrAddrCnt
          ) {
            doInitWriteTestDataElem()
          }
        //}
        when (myH2dSendFifo.io.push.fire) {
          doPipe1ReadTestDataElem(
            ramIdx=rSavedTestDataVecIdx,
            rdAddr=rRamRdAddrCnt,
          )
          //rH2dPayload.data := rTestData(rBurstCnt).sendData
          doFinishReadTestDataElem(rdTestDataElem)

          rH2dPayload.data := rdTestDataElem.sendData
          rH2dPayload.addr := rH2dPayload.burstAddr(
            rH2dBurstCnt,
            incrBurstCnt=true
          )

          //rH2dBurstCnt := rH2dBurstCnt + 1

          nextH2dMainBurstInfo.burstFirst := False
          nextH2dMainBurstInfo.burstCnt := rH2dMainBurstInfo.burstCnt - 1
          when (rH2dMainBurstInfo.burstCnt === 1) {
            nextH2dMainBurstInfo.burstLast := True
          }
          when (rH2dMainBurstInfo.burstLast) {
            rH2dValid := False
            nextH2dMainBurstInfo.burstLast := False
            //rState := State.READ_START
            rHadH2dFinish := True
          }
        }
        when (
          //RegNext(
          //  next=io.d2hBus.valid,
          //  init=False
          //)
          io.d2hBus.valid
        ) {
          myD2hReady := True

          if (!busCfg.allowBurst) {
            rD2hBurstCnt := rD2hBurstCnt + 1
            when (!(rD2hBurstCnt + 1).orR) {
              rHadD2hFinish := True
            }
          } else {
            rHadD2hFinish := True
          }
        }
        when (
          RegNext(
            (
              rHadH2dFinish && rHadD2hFinish
              && !myH2dSendFifo.io.occupancy.orR
            ),
            init=False
          )
        ) {
          rStateRandAddr := StateRandAddr.READ_START_PIPE_3
        }
      }
      is (StateRandAddr.READ_START_PIPE_3) {
        rStateRandAddr := StateRandAddr.READ_START_PIPE_2
        doInitH2dSendAddr()
        rRamRdAddrCnt := 0x0
      }
      is (StateRandAddr.READ_START_PIPE_2) {
        rStateRandAddr := StateRandAddr.READ_START_PIPE_1
        doPipe2ReadTestDataElem(
          ramIdx=rSavedTestDataVecIdx,
          rdAddr=rRamRdAddrCnt
        )
      }
      is (StateRandAddr.READ_START_PIPE_1) {
        rStateRandAddr := StateRandAddr.READ_START
        doPipe1ReadTestDataElem(
          ramIdx=rSavedTestDataVecIdx,
          rdAddr=rRamRdAddrCnt
        )
        rHadH2dFinish := False
        rHadD2hFinish := False
      }
      is (StateRandAddr.READ_START) {
        //rBurstCnt := 0x0
        rBurstCntVec.foreach(_ := 0x0)
        rHadH2dFinish := False
        rHadD2hFinish := False

        rH2dValid := True

        //rH2dPayload.addr := 0x0
        //rH2dPayload.byteEn := (
        //  U(rH2dPayload.byteEn.getWidth bits, default -> True)
        //)
        //rH2dPayload.byteSize := (
        //  0x0
        //)
        rH2dPayload.haveFullWord := (
          True
          //False
        )
        rH2dPayload.isWrite := False
        rH2dPayload.src := 0x0

        nextH2dMainBurstInfo.burstCnt := cfg.busCfg.maxBurstSizeMinus1
        nextH2dMainBurstInfo.burstFirst := True
        nextH2dMainBurstInfo.burstLast := False

        rStateRandAddr := StateRandAddr.READ_WAIT_TXN
        doPipe1ReadTestDataElem(
          ramIdx=rSavedTestDataVecIdx,
          rdAddr=rRamRdAddrCnt
        )
        doFinishReadTestDataElem(
          rdData=rTestData(rMyTestDataIdx)
        )
      }
      is (StateRandAddr.READ_WAIT_TXN) {
        //def rTestData = (
        //  rTestDataVec(rSavedTestDataVecIdx)
        //)
        //when (rRamRdAddrCnt.orR) {
        //  doPipe1ReadTestDataElem(
        //    ramIdx=rSavedTestDataVecIdx,
        //    rdAddr=rRamRdAddrCnt,
        //  )
        //}
        doPipe1ReadTestDataElem(
          ramIdx=rSavedTestDataVecIdx,
          rdAddr=rRamRdAddrCnt,
        )
        doFinishReadTestDataElem(
          rdData=rTestData(
            //if (!busCfg.allowBurst) (
            //  rD2hBurstCnt
            //) else (
              rMyTestDataIdx
            //)
          )
        )

        when (
          //rH2dValid
          //&& io.h2dBus.ready
          myH2dSendFifo.io.push.fire
        ) {
          if (!busCfg.allowBurst) {
            rH2dPayload.addr := rH2dPayload.burstAddr(
              rH2dBurstCnt + 1,
              incrBurstCnt=false,
            )
            when ((rH2dBurstCnt + 1).orR) {
              rH2dBurstCnt := rH2dBurstCnt + 1
            } otherwise {
              rH2dValid := False
              rHadH2dFinish := True
            }
          } else {
            rH2dValid := False
            //nextH2dMainBurstInfo.burstFirst := False
            rHadH2dFinish := True
          }
          nextH2dMainBurstInfo.burstFirst := False
        }
        when (
          //RegNext(
          //  next=io.d2hBus.valid,
          //  init=False,
          //)
          io.d2hBus.valid
        ) {
          myD2hReady := True
          rD2hBurstCnt := rD2hBurstCnt + 1
          //rTestData(rBurstCnt).recvData := myD2hSendData.data
          rRecvCmpDataRandAddr.recvDataVec(
            //if (!busCfg.allowBurst) (
            //  rD2hBurstCnt - 3
            //) else (
              rD2hBurstCnt
            //)
          ) := (
            myD2hSendData.data
          )

          //val wrTestDataElem = TestDataElem()
          //wrTestDataElem.recvData.allowOverride
          //wrTestDataElem := wrTestDataElem.getZero
          //wrTestDataElem.recvData := myD2hSendData.data
          //doWriteTestDataElem(
          //  ramIdx=rSavedTestDataVecIdx,
          //  wrAddr=Some(rBurstCnt),
          //  wrData=wrTestDataElem,
          //  wrByteEn={
          //    val tempWrByteEn = (
          //      Bits((wrTestDataElem.asBits.getWidth / 8) bits)
          //    )
          //    tempWrByteEn := tempWrByteEn.getZero
          //    tempWrByteEn.allowOverride
          //    for (bitIdx <- 0 until (myPrngCfg.myXsWidth / 8)) {
          //      tempWrByteEn(bitIdx) := True
          //    }
          //    Some(tempWrByteEn)
          //  }
          //)
          if (!busCfg.allowBurst) {
            //when (
            //  !(rBurstCnt + 1).orR
            //) {
            //  rHadD2hFinish := True
            //}
          } else {
            when (myD2hSendData.burstLast) {
              rHadD2hFinish := True
            }
          }
          if (!busCfg.allowBurst) {
            when (!(rD2hBurstCnt + 1).orR) {
              rHadD2hFinish := True
            }
          }
        }
        //if (!busCfg.allowBurst) {
        //  when (!(rD2hBurstCnt /*+ 1*/).orR) {
        //    rHadD2hFinish := True
        //  }
        //}
        when (rHadH2dFinish && rHadD2hFinish) {
          rStateRandAddr := StateRandAddr.DO_COMPARE_TEST_DATA
        }
      }
      is (StateRandAddr.DO_COMPARE_TEST_DATA) {
        //def rTestData = (
        //  rTestDataVec(rSavedTestDataVecIdx)
        //)
        val cmpVec = (
          Vec.fill(cfg.busCfg.maxBurstSizeMinus1 + 1)(
            Bool()
          )
        )
        for (testIdx <- 0 until cfg.busCfg.maxBurstSizeMinus1 + 1) {
          //when (rRecvCmpDataRandAddr(testIdx).valid) {
          //  cmpVec(testIdx) := (
          //    rTestData(testIdx).sendData
          //    =/= rRecvCmpDataRandAddr(testIdx).payload
          //  )
          //} otherwise {
          //  cmpVec(testIdx) := False
          //}
          when (rRecvCmpDataRandAddr.validVec(rSavedTestDataVecIdx)) {
            cmpVec(testIdx) := (
              rTestData(testIdx).sendData
              =/= rRecvCmpDataRandAddr.recvDataVec(testIdx)
            )
          } otherwise {
            cmpVec(testIdx) := False
          }
        }
        when (cmpVec.orR) {
          rStateRandAddr := StateRandAddr.FAILED_TEST
        } otherwise {
          rStateRandAddr := StateRandAddr.WRITE_START
        }
      }
      is (StateRandAddr.FAILED_TEST) {
      }
    }
    // END: state machine that does maximum-byte-amount bus bursts
    // starting from semi-random addresses
  })
  //--------
  val dualBurstRandDataArea = (
    cfg.kind == LcvBusDeviceRamTesterKind.DualBurstRandData
    //|| cfg.kind == LcvBusDeviceRamTesterKind.DualBurstRandDataSemiRandAddr
  ) generate (new Area {
    //def rTestData = rTestDataVec.head
    //def testData = myTestDataRamArr.head
    //def myHaveSemiRandAddr = (
    //  cfg.kind == LcvBusDeviceRamTesterKind.DualBurstRandDataSemiRandAddr
    //)
    //def doInitH2dSendAddr(): Unit = {
    //  rH2dPayload.addr := (
    //    if (myHaveSemiRandAddr) (
    //      myPrng.io.outpXs.last(
    //        cfg.busCfg
    //      ).resize(rH2dPayload.addr.getWidth)
    //    ) else (
    //      U(s"${rH2dPayload.addr.getWidth}'d0")
    //    )
    //  )
    //}
    def rBurstCnt = rBurstCntVec.head
    // BEGIN: state machine that does maximum-byte-amount bus bursts
    switch (rStateIncrAddr) {
      is (StateIncrAddr.WRITE_START) {
        rHadH2dFinish := False
        rHadD2hFinish := False

        for (testIdx <- 0 until cfg.busCfg.maxBurstSizeMinus1 + 1) {
          rTestData(testIdx).sendData := (
            //myPrngArea.getCurrRand(idx=testIdx)
            myPrng.io.outpXs(testIdx)
          )
          rRecvCmpDataIncrAddr(testIdx) := (
            rRecvCmpDataIncrAddr(testIdx).getZero
          )
          //rTestData(testIdx).recvData := 0x0
        }

        rH2dValid := True
        rH2dPayload.data := (
          //rTestData.head.sendData
          //myPrngArea.getCurrRand(idx=0)
          myPrng.io.outpXs(0)
        )
        //doInitH2dSendAddr()
        rH2dPayload.addr := 0x0
        //rH2dPayload.addr := (
        //  //if (myHaveSemiRandAddr) (
        //  //  myPrng.io.outpXs.last(
        //  //    cfg.busCfg
        //  //  ).resize(rH2dPayload.addr.getWidth)
        //  //) else (
        //  //  U(s"${rH2dPayload.addr.getWidth}'d0")
        //  //)
        //  0x0
        //)
        //rH2dPayload.byteEn := U(
        //  rH2dPayload.byteEn.getWidth bits, default -> True
        //)
        rH2dPayload.haveFullWord := True
        rH2dPayload.isWrite := True
        rH2dPayload.src := 0x0

        nextH2dMainBurstInfo.burstCnt := cfg.busCfg.maxBurstSizeMinus1
        nextH2dMainBurstInfo.burstFirst := True
        nextH2dMainBurstInfo.burstLast := False
        rBurstCnt := (
          //0x0
          0x1
        )

        rStateIncrAddr := StateIncrAddr.WRITE_WAIT_TXN
      }
      is (StateIncrAddr.WRITE_WAIT_TXN) {
        when (
          rH2dValid
          && io.h2dBus.ready
        ) {
          rH2dPayload.data := rTestData(rBurstCnt).sendData
          rBurstCnt := rBurstCnt + 1

          nextH2dMainBurstInfo.burstFirst := False
          nextH2dMainBurstInfo.burstCnt := rH2dMainBurstInfo.burstCnt - 1
          when (rH2dMainBurstInfo.burstCnt === 1) {
            nextH2dMainBurstInfo.burstLast := True
          }
          when (rH2dMainBurstInfo.burstLast) {
            rH2dValid := False
            nextH2dMainBurstInfo.burstLast := False
            //rState := State.READ_START
            rHadH2dFinish := True
          }
        }
        when (
          //RegNext(
          //  next=io.d2hBus.valid,
          //  init=False
          //)
          io.d2hBus.valid
        ) {
          myD2hReady := True
          rHadD2hFinish := True
        }
        when (rHadH2dFinish && rHadD2hFinish) {
          rStateIncrAddr := StateIncrAddr.READ_START
        }
      }
      is (StateIncrAddr.READ_START) {
        rBurstCnt := 0x0
        rHadH2dFinish := False
        rHadD2hFinish := False

        rH2dValid := True

        rH2dPayload.addr := 0x0
        //rH2dPayload.byteEn := U(
        //  rH2dPayload.byteEn.getWidth bits, default -> True
        //)
        rH2dPayload.haveFullWord := True
        rH2dPayload.isWrite := False
        rH2dPayload.src := 0x0

        nextH2dMainBurstInfo.burstCnt := cfg.busCfg.maxBurstSizeMinus1
        nextH2dMainBurstInfo.burstFirst := True
        nextH2dMainBurstInfo.burstLast := False

        rStateIncrAddr := StateIncrAddr.READ_WAIT_TXN
      }
      is (StateIncrAddr.READ_WAIT_TXN) {
        when (
          rH2dValid
          && io.h2dBus.ready
        ) {
          rH2dValid := False
          nextH2dMainBurstInfo.burstFirst := False
          rHadH2dFinish := True
        }
        when (
          //RegNext(
          //  next=io.d2hBus.valid,
          //  init=False,
          //)
          io.d2hBus.valid
        ) {
          myD2hReady := True
          rBurstCnt := rBurstCnt + 1
          //rTestData(rBurstCnt).recvData := myD2hSendData.data
          rRecvCmpDataIncrAddr(rBurstCnt).payload := myD2hSendData.data
          when (myD2hSendData.burstLast) {
            rHadD2hFinish := True
          }
        }
        when (rHadH2dFinish && rHadD2hFinish) {
          rStateIncrAddr := StateIncrAddr.DO_COMPARE_TEST_DATA
        }
      }
      is (StateIncrAddr.DO_COMPARE_TEST_DATA) {
        val cmpVec = (
          Vec.fill(cfg.busCfg.maxBurstSizeMinus1 + 1)(
            Bool()
          )
        )
        for (testIdx <- 0 until cfg.busCfg.maxBurstSizeMinus1 + 1) {
          cmpVec(testIdx) := (
            rTestData(testIdx).sendData
            //=/= rTestData(testIdx).recvData
            =/= rRecvCmpDataIncrAddr(testIdx).payload
          )
        }
        when (cmpVec.orR) {
          rStateIncrAddr := StateIncrAddr.FAILED_TEST
        } otherwise {
          rStateIncrAddr := StateIncrAddr.WRITE_START
        }
      }
      is (StateIncrAddr.FAILED_TEST) {
      }
    }
    // END: state machine that does maximum-byte-amount bus bursts
  })
  //--------
  //--------
  //--------
  val noBurstRandDataArea = (
    cfg.kind == LcvBusDeviceRamTesterKind.NoBurstRandData
  ) generate (new Area {
    // BEGIN: previous state machine, which lacks bursts

    def rBurstCnt = rBurstCntVec.head

    //def rTestData = rTestDataVec.head
    val tempCnt = (
      KeepAttribute(
        (rH2dPayload.addr(rH2dPayload.addr.high downto 2))(
          log2Up(rTestData.size) - 1 downto 0
        )
      )
    )
    
    switch (rStateIncrAddr) {
      is (StateIncrAddr.WRITE_START) {
        //--------
        rHadH2dFinish := False
        rHadD2hFinish := False
        //--------
        rH2dValid := True
        switch (tempCnt) {
          for (testIdx <- 0 until rTestData.size) {
            is (testIdx) {
              rH2dPayload.data := (
                //myPrngArea.getCurrRand(testIdx)
                myPrng.io.outpXs(testIdx)
              )
              rTestData(tempCnt).sendData := (
                //myPrngArea.getCurrRand(testIdx)
                myPrng.io.outpXs(testIdx)
              )
            }
          }
        }
        rRecvCmpDataIncrAddr(tempCnt) := rRecvCmpDataIncrAddr(tempCnt).getZero
        //rTestData(tempCnt).recvData := (
        //  U(s"${myPrngCfg.myXsWidth}'d0")
        //)
        //rH2dPayload.byteEn := U(
        //  rH2dPayload.byteEn.getWidth bits, default -> True
        //)
        rH2dPayload.haveFullWord := True
        //nextH2dMainBurstInfo.burstCnt := 1
        rH2dPayload.isWrite := True
        rH2dPayload.src := 0x0
        //--------
        rStateIncrAddr := StateIncrAddr.WRITE_WAIT_TXN
        //--------
      }
      is (StateIncrAddr.WRITE_WAIT_TXN) {
        when (
          //io.h2dBus.fire
          rH2dValid
          && io.h2dBus.ready
        ) {
          rH2dValid := False
          rHadH2dFinish := True
        }
        when (io.d2hBus.valid) {
          rHadD2hFinish := True
          myD2hReady := True
        }
        when (rHadH2dFinish && rHadD2hFinish) {
          when ((rH2dPayload.addr + 4) < myMaxBaseAddr) {
            rStateIncrAddr := StateIncrAddr.WRITE_START
            rH2dPayload.addr := rH2dPayload.addr + 4
          } otherwise {
            rStateIncrAddr := StateIncrAddr.READ_START
            rH2dPayload.addr := 0x0
          }
        }
      }
      is (StateIncrAddr.READ_START) {
        //--------
        rHadH2dFinish := False
        rHadD2hFinish := False
        //--------
        rH2dValid := True
        rH2dPayload.isWrite := False
        //--------
        rStateIncrAddr := StateIncrAddr.READ_WAIT_TXN
        //--------
      }
      is (StateIncrAddr.READ_WAIT_TXN) {
        when (
          //io.h2dBus.fire
          rH2dValid
          && io.h2dBus.ready
        ) {
          rH2dValid := False
          rHadH2dFinish := True
        }
        when (
          io.d2hBus.valid
          && !rHadD2hFinish
        ) {
          rHadD2hFinish := True
          myD2hReady := True
          //rTestData(tempCnt).recvData := io.d2hBus.payload.data
          rRecvCmpDataIncrAddr(tempCnt).payload := io.d2hBus.payload.data
        }
        when (rHadH2dFinish && rHadD2hFinish) {
          when ((rH2dPayload.addr + 4) < myMaxBaseAddr) {
            rStateIncrAddr := StateIncrAddr.READ_START
            rH2dPayload.addr := rH2dPayload.addr + 4
          } otherwise {
            //rState := State.WRITE_START
            rStateIncrAddr := StateIncrAddr.DO_COMPARE_TEST_DATA
            rH2dPayload.addr := 0x0
          }
        }
      }
      is (StateIncrAddr.DO_COMPARE_TEST_DATA) {
        when (rH2dPayload.addr < myMaxBaseAddr) {
          rH2dPayload.addr := rH2dPayload.addr + 4
        } otherwise {
          rH2dPayload.addr := 0x0
          rStateIncrAddr := StateIncrAddr.WRITE_START
        }
        val failure = (
          rTestData(tempCnt).sendData
          //=/= rTestData(tempCnt).recvData
          =/= rRecvCmpDataIncrAddr(tempCnt).payload
        )
        when (failure) {
          rStateIncrAddr := StateIncrAddr.FAILED_TEST
        }
        rRecvCmpDataIncrAddr(tempCnt).valid := (
          rTestData(tempCnt).sendData
          //=== rTestData(tempCnt).recvData
          === rRecvCmpDataIncrAddr(tempCnt).payload
        )
      }
      is (StateIncrAddr.FAILED_TEST) {
      }
    }
    // END: previous state machine, which lacks bursts
  })
  //--------
}
case class LcvBusDeviceRamTester(
  cfg: LcvBusDeviceRamTesterConfig,
) extends Component {
  def busCfg = cfg.busCfg
  val io = LcvBusDeviceRamTesterIo(cfg=cfg)
  val nonCoherentInnerTesterArea = (
    (
      busCfg.cacheCfg == None
      || !busCfg.cacheCfg.get.coherent
    )
    && !cfg.kind._isCoherent
  ) generate (new Area {
    val nonCoherentInnerTester = LcvBusDeviceRamTesterNonCoherent(cfg=cfg)
    nonCoherentInnerTester.io <> io.busVec.head
  })
}
