package libcheesevoyage.bus.lcvBus

//import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._
import spinal.lib.io._

import libcheesevoyage.general._


sealed trait LcvBusDeviceTesterKind {
  def hasRandData: Boolean  
  def hasRandAddr: Boolean
}
object LcvBusDeviceTesterKind {
  case object DualBurstRandDataSemiRandAddr
  extends LcvBusDeviceTesterKind {
    def hasRandData: Boolean = true
    def hasRandAddr: Boolean = true
  }
  case object DualBurstRandData extends LcvBusDeviceTesterKind {
    def hasRandData: Boolean = true
    def hasRandAddr: Boolean = false
  }
  case object NoBurstRandData extends LcvBusDeviceTesterKind {
    def hasRandData: Boolean = true
    def hasRandAddr: Boolean = false
  }
  //case object DualBurstRandAddr extends LcvBusDeviceTesterKind {
  //  def hasRandData: Boolean = false
  //}
}
case class LcvBusDeviceTesterConfig(
  busCfg: LcvBusConfig,
  kind: LcvBusDeviceTesterKind,
  testDataRamStyle: String="M10K",
) {
}

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

case class LcvBusDeviceTester(
  cfg: LcvBusDeviceTesterConfig,
) extends Component {
  //--------
  def busCfg = cfg.busCfg
  //--------
  val io = master(LcvBusIo(cfg=busCfg))
  //--------
  val rH2dValid = Reg(Bool(), init=False)
  val rH2dPayload = {
    val temp = Reg(LcvBusH2dPayload(cfg=busCfg))
    temp.init(temp.getZero)
    temp
  }
  val myH2dSendFifo = (
    cfg.kind.hasRandAddr
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

  if (!cfg.kind.hasRandAddr) {
    io.h2dBus.valid := rH2dValid
    io.h2dBus.payload := (
      rH2dPayload
    )
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
    !cfg.kind.hasRandAddr
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
    cfg.kind.hasRandAddr
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
  val rBurstCnt = (
    Reg(UInt(log2Up(cfg.busCfg.maxBurstSizeMinus1 + 1) bits))
    init(0x0)
  )
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
        + (if (cfg.kind.hasRandAddr) (1) else (0))
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
  val myPrng = LcvXorShift16(
    cfg=myPrngCfg
  )

  //val myPrngArea = (
  //  cfg.kind.hasRandData
  //) generate (new Area {
  //  // credit:
  //  // http://www.retroprogramming.com/2017/07/xorshift-pseudorandom-numbers-in-z80.html
  //  def myXsWidth = (
  //    32
  //  )
  //  //def myCntWidth = 3
  //  def myCntMax = 2
  //  def myCntWidth = log2Up(myCntMax + 1) + 1
  //  val rCnt = (
  //    Reg(SInt(myCntWidth bits))
  //    init(myCntMax)
  //  )
  //  val rXsVec = (
  //    Vec.fill(2)(
  //      Vec.fill(
  //        //hostBusCfg.maxBurstSizeMinus1 + 1
  //        cfg.busCfg.maxBurstSizeMinus1 + 1
  //      )(
  //        Vec.fill(3)(
  //          Reg(UInt(myXsWidth bits))
  //          //init(1)
  //        )
  //      )
  //    )
  //  )
  //  for (idx <- 0 until rXsVec.size) {
  //    //rXsVec(idx).foreach(
  //    //  item => item.init(idx + 1)
  //    //  //outerItem => outerItem.foreach(
  //    //  //  item => item.init(idx + 1)
  //    //  //)
  //    //)
  //    for (jdx <- 0 until rXsVec(idx).size) {
  //      rXsVec(idx)(jdx).foreach(item => {
  //        item.init(idx * rXsVec(idx).size + jdx + 1)
  //      })
  //    }
  //  }
  //  rXsVec.foreach(outerXs => outerXs.foreach(
  //    rXs => {
  //      rXs(2) := (
  //        (rXs(0) ^ ((rXs(0) << 7)(rXs(0).bitsRange))).resize(myXsWidth)
  //      )
  //      rXs(1) := (
  //        (rXs(2) ^ (rXs(2) >> 9).resize(myXsWidth)).resize(myXsWidth)
  //      )
  //      when (!rCnt.msb) {
  //        rCnt := rCnt - 1
  //      } otherwise {
  //        rXs(0) := (
  //          (rXs(1) ^ ((rXs(1) << 8)(rXs(0).bitsRange))).resize(myXsWidth)
  //        )
  //        rCnt := myCntMax
  //      }
  //    }
  //  ))
  //  def getCurrRand(idx: Int): UInt = {
  //    //val a = (rXs ^ ((rXs << 7)(rXs.bitsRange))).resize(rXs.getWidth)
  //    //val b = (a ^ (a >> 9).resize(rXs.getWidth)).resize(rXs.getWidth)
  //    //val c = (b ^ ((b << 8)(rXs.bitsRange))).resize(rXs.getWidth)
  //    //rXs := c
  //    //rXs(0) := r
  //    //c(15 downto 0)
  //    Cat(
  //      //rXsVec(0)(idx)(0)(15 downto 0),
  //      //rXsVec(1)(idx)(0)(15 downto 0),
  //      rXsVec.head(idx)(0)(15 downto 0),
  //      rXsVec.last(idx)(0)(15 downto 0),
  //    ).asUInt
  //  }
  //})

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
    if (cfg.kind.hasRandAddr) (
      4
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
    //!cfg.kind.hasRandAddr
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
  val rRecvCmpData = (
    Vec.fill(
      cfg.busCfg.maxBurstSizeMinus1 + 1
    )({
      val temp = Reg(Flow(UInt(myPrngCfg.myXsWidth bits)))
      temp.init(temp.getZero)
      temp
    })
  )
  def myTestDataRamDepthInner = cfg.busCfg.maxBurstSizeMinus1 + 1
  def myTestDataRamDepthOuter = (1 << log2Up(myTestDataVecOuterSize))
  val myTestDataRam/*Arr*/ = (
    cfg.kind.hasRandAddr
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
          arrRamStyle=cfg.testDataRamStyle,
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
      //  arrRamStyle=cfg.testDataRamStyle,
      //)
    //)
  }
  //--------
  val dualBurstRandDataArea = (
    cfg.kind == LcvBusDeviceTesterKind.DualBurstRandData
    //|| cfg.kind == LcvBusDeviceTesterKind.DualBurstRandDataSemiRandAddr
  ) generate (new Area {
    //def rTestData = rTestDataVec.head
    //def testData = myTestDataRamArr.head
    //def myHaveSemiRandAddr = (
    //  cfg.kind == LcvBusDeviceTesterKind.DualBurstRandDataSemiRandAddr
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
          rRecvCmpData(testIdx) := rRecvCmpData(testIdx).getZero
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
        rH2dPayload.byteEn := U(
          rH2dPayload.byteEn.getWidth bits, default -> True
        )
        rH2dPayload.isWrite := True
        rH2dPayload.src := 0x0

        rH2dPayload.burstCnt := cfg.busCfg.maxBurstSizeMinus1
        rH2dPayload.burstFirst := True
        rH2dPayload.burstLast := False
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

          rH2dPayload.burstFirst := False
          rH2dPayload.burstCnt := rH2dPayload.burstCnt - 1
          when (rH2dPayload.burstCnt === 1) {
            rH2dPayload.burstLast := True
          }
          when (rH2dPayload.burstLast) {
            rH2dValid := False
            rH2dPayload.burstLast := False
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
        rH2dPayload.byteEn := U(
          rH2dPayload.byteEn.getWidth bits, default -> True
        )
        rH2dPayload.isWrite := False
        rH2dPayload.src := 0x0

        rH2dPayload.burstCnt := cfg.busCfg.maxBurstSizeMinus1
        rH2dPayload.burstFirst := True
        rH2dPayload.burstLast := False

        rStateIncrAddr := StateIncrAddr.READ_WAIT_TXN
      }
      is (StateIncrAddr.READ_WAIT_TXN) {
        when (
          rH2dValid
          && io.h2dBus.ready
        ) {
          rH2dValid := False
          rH2dPayload.burstFirst := False
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
          rRecvCmpData(rBurstCnt).payload := myD2hSendData.data
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
            =/= rRecvCmpData(testIdx).payload
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
  val dualBurstRandDataSemiRandAddrArea = (
    cfg.kind == LcvBusDeviceTesterKind.DualBurstRandDataSemiRandAddr
  ) generate (new Area {
    // BEGIN: state machine that does maximum-byte-amount bus bursts
    // starting from semi-random addresses

    //def rTestData = rTestDataVec.head
    //def myHaveSemiRandAddr = (
    //  cfg.kind == LcvBusDeviceTesterKind.DualBurstRandDataSemiRandAddr
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
    myTestDataRam.io.wrData := 0x0
    //myTestDataRam.io.wrByteEn := 0x0
    //val wrTestDataElem = TestDataElem()
    //wrTestDataElem := wrTestDataElem.getZero

    //def doApplyTestDataRam(
    //  ramIdx: UInt,
    //  ramDoItFunc: (UInt, RamSdpPipe[TestDataElem]) => Unit
    //) {
    //  switch (ramIdx) {
    //    for (myRamIdx <- 0 until myTestDataRamArr.size) {
    //      is (myRamIdx) {
    //        ramDoItFunc(ramIdx, myTestDataRamArr(myRamIdx))
    //      }
    //    }
    //  }
    //}
    def getRamIdxRange(
      ramIdx: UInt
    ) = {
      val ram = myTestDataRam
      (
        ram.io.wrAddr.high
        downto ram.io.wrAddr.getWidth - ramIdx.getWidth
      )
    }
    def doWriteTestDataElem(
      ramIdx: UInt,
      wrAddr: Option[UInt],
      wrData: TestDataElem,
      //wrByteEn: Option[Bits]=None,
    ): Unit = {
      val ram = myTestDataRam
      //def tempFunc(
      //  ramIdx: UInt
      //): Unit = {
      //}
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
      ram.io.wrData := wrData.asBits
      //doApplyTestDataRam(
      //  ramIdx,
      //  (ramIdx, ram) => {
      //    ram.io.wrEn := True
      //    wrAddr match {
      //      case Some(wrAddr) => {
      //        ram.io.wrAddr := wrAddr
      //        wrAddr := wrAddr + 1
      //      }
      //      case None => {
      //        ram.io.wrAddr := ram.io.wrAddr.getZero
      //      }
      //    }
      //    ram.io.wrData := wrData.asBits
      //    //wrByteEn match {
      //    //  case Some(wrByteEn) => {
      //    //    ram.io.wrByteEn := wrByteEn
      //    //  }
      //    //  case None => {
      //    //    ram.io.wrByteEn := (
      //    //      B(ram.io.wrByteEn.getWidth bits, default -> True)
      //    //    )
      //    //  }
      //    //}
      //  }
      //)
    }
    def doInitWriteTestDataElem(
    ): Unit = {
      doWriteTestDataElem(
        ramIdx=rSavedTestDataVecIdx,
        wrAddr=Some(rRamWrAddrCnt),
        wrData={
          val temp = TestDataElem()
          temp.sendData := myPrng.io.outpXs(rRamWrAddrCnt.resized)
          //temp.recvData := temp.recvData.getZero
          //temp.flags := 0x0
          //temp.valid.allowOverride
          //temp.valid := True
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
      //doApplyTestDataRam(
      //  ramIdx,
      //  (ramIdx, ram) => {
      //    //ram.io.rdEn := True
      //    ram.io.rdAddr := rdAddr
      //    rdAddr := rdAddr + 1
      //  }
      //)
    }
    def doPipe1ReadTestDataElem(
      ramIdx: UInt,
      rdAddr: UInt
    ): Unit = {
      val ram = myTestDataRam
      ram.io.rdEn := True
      ram.io.rdAddr := Cat(ramIdx, rdAddr).asUInt
      rdAddr := rdAddr + 1
      //doApplyTestDataRam(
      //  ramIdx,
      //  (ramIdx, ram) => {
      //    ram.io.rdEn := True
      //    ram.io.rdAddr := rdAddr
      //    rdAddr := rdAddr + 1
      //  }
      //)
    }
    def doFinishReadTestDataElem(
      //ramIdx: UInt,
      rdData: TestDataElem,
    ): Unit = {
      val ram = myTestDataRam
      //val ramIdx = rSavedTestDataVecIdx
      rdData.assignFromBits(ram.io.rdData)
      //doApplyTestDataRam(
      //  //ramIdx,
      //  rSavedTestDataVecIdx,
      //  (ramIdx, ram) => {
      //    rdData.assignFromBits(ram.io.rdData)
      //  }
      //)
    }

    def myTempBurstCntEtcWidth = (
      cfg.busCfg.burstCntWidth + log2Up(busCfg.dataWidth / 8)
    )
    def myPrngTestDataRange = (
      myTempBurstCntEtcWidth - 1 + log2Up(myTestDataVecOuterSize)
      downto (
        //log2Up(rTestDataVec.size)
        //cfg.busCfg.burstCntWidth
        myTempBurstCntEtcWidth
      )
    )
    println(
      s"myTempBurstCntEtcWidth: ${myTempBurstCntEtcWidth} "
      + s"myPrngTestDataRange: ${myPrngTestDataRange}"
    )
    val rdTestDataElem = TestDataElem()
    rdTestDataElem := RegNext(rdTestDataElem, init=rdTestDataElem.getZero)
    val rMyTestDataIdx = (
      RegNext(
        RegNext(rRamRdAddrCnt, init=rRamRdAddrCnt.getZero),
        init=rRamRdAddrCnt.getZero,
      )
    )

    switch (rStateRandAddr) {
      is (StateRandAddr.WRITE_START) {
        //def rTestData = (
        //  rTestDataVec(myPrng.io.outpXs.last(myPrngTestDataRange))
        //)
        rHadH2dFinish := False
        rHadD2hFinish := False

        doWriteTestDataElem(
          ramIdx=myPrng.io.outpXs.last(myPrngTestDataRange),
          wrAddr=None,
          wrData={
            val temp = TestDataElem()
            temp.sendData := myPrng.io.outpXs(0x0)
            //temp.recvData := temp.recvData.getZero
            //temp.flags := 0x0
            //temp.valid.allowOverride
            //temp.valid := True
            temp
          }
        )
        //rRecvCmpData(0x0).payload := 0x0
        //rRecvCmpData(0x0).valid := True
        rRecvCmpData.foreach(item => {
          //item := item.getZero
          item.payload := item.payload.getZero
          item.valid := True
        })
        rRamWrAddrCnt := 0x1 
        rRamRdAddrCnt := 0x0

        //for (testIdx <- 0 until cfg.busCfg.maxBurstSizeMinus1 + 1) {
        //  rTestData(testIdx).sendData := (
        //    //myPrngArea.getCurrRand(idx=testIdx)
        //    myPrng.io.outpXs(testIdx)
        //  )
        //  rTestData(testIdx).recvData := (
        //    //0x0
        //    rTestData(testIdx).recvData.getZero
        //  )
        //  rTestData(testIdx).valid := True
        //}

        //rH2dValid := True
        rH2dValid := False
        rH2dPayload.data := (
          //rTestData.head.sendData
          //myPrngArea.getCurrRand(idx=0)
          myPrng.io.outpXs(0)
        )
        //doInitH2dSendAddr()
        doInitH2dSendAddr()
        //rH2dPayload.addr := 0x0
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
        rH2dPayload.byteEn := U(
          rH2dPayload.byteEn.getWidth bits, default -> True
        )
        rH2dPayload.isWrite := True
        rH2dPayload.src := 0x0

        rH2dPayload.burstCnt := cfg.busCfg.maxBurstSizeMinus1
        rH2dPayload.burstFirst := True
        rH2dPayload.burstLast := False
        rBurstCnt := (
          //0x0
          0x1
        )

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
      }
      is (StateRandAddr.WRITE_WAIT_TXN) {
        //def rTestData = (
        //  rTestDataVec(rSavedTestDataVecIdx)
        //)
        when (rRamWrAddrCnt > 0) {
          doInitWriteTestDataElem()
        }
        when (
          //rH2dValid
          //&& io.h2dBus.ready
          myH2dSendFifo.io.push.fire
        ) {
          doPipe1ReadTestDataElem(
            ramIdx=rSavedTestDataVecIdx,
            rdAddr=rRamRdAddrCnt,
          )
          //rH2dPayload.data := rTestData(rBurstCnt).sendData
          doFinishReadTestDataElem(rdTestDataElem)

          rH2dPayload.data := rdTestDataElem.sendData

          rBurstCnt := rBurstCnt + 1

          rH2dPayload.burstFirst := False
          rH2dPayload.burstCnt := rH2dPayload.burstCnt - 1
          when (rH2dPayload.burstCnt === 1) {
            rH2dPayload.burstLast := True
          }
          when (rH2dPayload.burstLast) {
            rH2dValid := False
            rH2dPayload.burstLast := False
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
      }
      is (StateRandAddr.READ_START) {
        rBurstCnt := 0x0
        rHadH2dFinish := False
        rHadD2hFinish := False

        rH2dValid := True

        //rH2dPayload.addr := 0x0
        rH2dPayload.byteEn := (
          U(rH2dPayload.byteEn.getWidth bits, default -> True)
        )
        rH2dPayload.isWrite := False
        rH2dPayload.src := 0x0

        rH2dPayload.burstCnt := cfg.busCfg.maxBurstSizeMinus1
        rH2dPayload.burstFirst := True
        rH2dPayload.burstLast := False

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
          rdData=rTestData(rMyTestDataIdx)
        )

        when (
          //rH2dValid
          //&& io.h2dBus.ready
          myH2dSendFifo.io.push.fire
        ) {
          rH2dValid := False
          rH2dPayload.burstFirst := False
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
          rRecvCmpData(rBurstCnt).payload := myD2hSendData.data

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
          when (myD2hSendData.burstLast) {
            rHadD2hFinish := True
          }
        }
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
          when (rRecvCmpData(testIdx).valid) {
            cmpVec(testIdx) := (
              rTestData(testIdx).sendData
              =/= rRecvCmpData(testIdx).payload
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
  //--------
  val noBurstRandDataArea = (
    cfg.kind == LcvBusDeviceTesterKind.NoBurstRandData
  ) generate (new Area {
    // BEGIN: previous state machine, which lacks bursts
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
        rRecvCmpData(tempCnt) := rRecvCmpData(tempCnt).getZero
        //rTestData(tempCnt).recvData := (
        //  U(s"${myPrngCfg.myXsWidth}'d0")
        //)
        rH2dPayload.byteEn := U(
          rH2dPayload.byteEn.getWidth bits, default -> True
        )
        //rH2dPayload.burstCnt := 1
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
          rRecvCmpData(tempCnt).payload := io.d2hBus.payload.data
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
          =/= rRecvCmpData(tempCnt).payload
        )
        when (failure) {
          rStateIncrAddr := StateIncrAddr.FAILED_TEST
        }
        rRecvCmpData(tempCnt).valid := (
          rTestData(tempCnt).sendData
          //=== rTestData(tempCnt).recvData
          === rRecvCmpData(tempCnt).payload
        )
      }
      is (StateIncrAddr.FAILED_TEST) {
      }
    }
    // END: previous state machine, which lacks bursts
  })
  //--------
}
