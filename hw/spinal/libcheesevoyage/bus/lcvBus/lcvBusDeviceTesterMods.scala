package libcheesevoyage.bus.lcvBus

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._
import spinal.lib.io._


sealed trait LcvBusDeviceTesterKind
object LcvBusDeviceTesterKind {
  case object DualBurstRandData extends LcvBusDeviceTesterKind
  case object NoBurstRandData extends LcvBusDeviceTesterKind
}
case class LcvBusDeviceTesterConfig(
  busCfg: LcvBusConfig,
  kind: LcvBusDeviceTesterKind,
) {
}

case class LcvBusDeviceTester(
  cfg: LcvBusDeviceTesterConfig,
) extends Component {
  //--------
  def busCfg = cfg.busCfg
  //--------
  val io = master(LcvBusIo(cfg=busCfg))
  //--------
  val rH2dValid = Reg(Bool(), init=False)
  val rH2dSendData = {
    val temp = Reg(LcvBusH2dPayload(cfg=cfg.busCfg))
    temp.init(temp.getZero)
    temp
  }
  val myD2hReady = Bool()
  myD2hReady := False
  val myD2hSendData = (
    //RegNext(
    //  next=io.d2hBus.payload,
    //  init=io.d2hBus.payload.getZero,
    //)
    io.d2hBus.payload
  )

  io.h2dBus.valid := rH2dValid
  io.h2dBus.payload := (
    rH2dSendData
  )
  io.d2hBus.ready := myD2hReady //rD2hReady

  object State extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      WRITE_START,
      WRITE_WAIT_TXN,
      READ_START,
      READ_WAIT_TXN,
      DO_COMPARE_TEST_DATA,
      FAILED_TEST
      = newElement();
  }
  val rState = (
    Reg(State())
    init(State.WRITE_START)
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

  val myPrngArea = new Area {
    // credit:
    // http://www.retroprogramming.com/2017/07/xorshift-pseudorandom-numbers-in-z80.html
    def myXsWidth = (
      32
    )
    //def myCntWidth = 3
    def myCntMax = 2
    def myCntWidth = log2Up(myCntMax + 1) + 1
    val rCnt = (
      Reg(SInt(myCntWidth bits))
      init(myCntMax)
    )
    val rXsVec = (
      Vec.fill(2)(
        Vec.fill(
          //hostBusCfg.maxBurstSizeMinus1 + 1
          cfg.busCfg.maxBurstSizeMinus1 + 1
        )(
          Vec.fill(3)(
            Reg(UInt(myXsWidth bits))
            //init(1)
          )
        )
      )
    )
    for (idx <- 0 until rXsVec.size) {
      //rXsVec(idx).foreach(
      //  item => item.init(idx + 1)
      //  //outerItem => outerItem.foreach(
      //  //  item => item.init(idx + 1)
      //  //)
      //)
      for (jdx <- 0 until rXsVec(idx).size) {
        rXsVec(idx)(jdx).foreach(item => {
          item.init(idx * rXsVec(idx).size + jdx + 1)
        })
      }
    }
    rXsVec.foreach(outerXs => outerXs.foreach(
      rXs => {
        rXs(2) := (
          (rXs(0) ^ ((rXs(0) << 7)(rXs(0).bitsRange))).resize(myXsWidth)
        )
        rXs(1) := (
          (rXs(2) ^ (rXs(2) >> 9).resize(myXsWidth)).resize(myXsWidth)
        )
        when (!rCnt.msb) {
          rCnt := rCnt - 1
        } otherwise {
          rXs(0) := (
            (rXs(1) ^ ((rXs(1) << 8)(rXs(0).bitsRange))).resize(myXsWidth)
          )
          rCnt := myCntMax
        }
      }
    ))
    def getCurrRand(idx: Int): UInt = {
      //val a = (rXs ^ ((rXs << 7)(rXs.bitsRange))).resize(rXs.getWidth)
      //val b = (a ^ (a >> 9).resize(rXs.getWidth)).resize(rXs.getWidth)
      //val c = (b ^ ((b << 8)(rXs.bitsRange))).resize(rXs.getWidth)
      //rXs := c
      //rXs(0) := r
      //c(15 downto 0)
      Cat(
        //rXsVec(0)(idx)(0)(15 downto 0),
        //rXsVec(1)(idx)(0)(15 downto 0),
        rXsVec.head(idx)(0)(15 downto 0),
        rXsVec.last(idx)(0)(15 downto 0),
      ).asUInt
    }
  }

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
  val rTestData = (
    Vec.fill(
      //(myMaxBaseAddr >> 2) + 1
      //(myMaxBaseAddr >> 1) // intentionally leave some always-zero elements
      //myNumTests
      //hostBusCfg.maxBurstSizeMinus1 + 1
      cfg.busCfg.maxBurstSizeMinus1 + 1
    )(
      {
        val temp = Reg(Flow(
          //Vec.fill(hostBusCfg.maxBurstSizeMinus1 + 1)(
            Vec.fill(2)(
              UInt(myPrngArea.myXsWidth bits)
            )
          //)
        ))
        temp.init(temp.getZero)
        KeepAttribute(temp)
      }
    )
  )
  //--------
  val dualBurstRandDataArea = (
    cfg.kind == LcvBusDeviceTesterKind.DualBurstRandData
  ) generate (new Area {
    // BEGIN: state machine that does maximum-byte-amount bus bursts
    switch (rState) {
      is (State.WRITE_START) {
        rHadH2dFinish := False
        rHadD2hFinish := False

        for (testIdx <- 0 until cfg.busCfg.maxBurstSizeMinus1 + 1) {
          rTestData(testIdx).payload.head := (
            myPrngArea.getCurrRand(idx=testIdx)
          )
          rTestData(testIdx).payload.last := 0x0
        }

        rH2dValid := True
        rH2dSendData.data := (
          //rTestData.head.payload.head
          myPrngArea.getCurrRand(idx=0)
        )
        rH2dSendData.addr := 0x0
        rH2dSendData.byteEn := U(
          rH2dSendData.byteEn.getWidth bits, default -> True
        )
        rH2dSendData.isWrite := True
        rH2dSendData.src := 0x0

        rH2dSendData.burstCnt := cfg.busCfg.maxBurstSizeMinus1
        rH2dSendData.burstFirst := True
        rH2dSendData.burstLast := False
        rBurstCnt := (
          //0x0
          0x1
        )

        rState := State.WRITE_WAIT_TXN
      }
      is (State.WRITE_WAIT_TXN) {
        when (
          rH2dValid
          && io.h2dBus.ready
        ) {
          rH2dSendData.data := rTestData(rBurstCnt).payload.head
          rBurstCnt := rBurstCnt + 1

          rH2dSendData.burstFirst := False
          rH2dSendData.burstCnt := rH2dSendData.burstCnt - 1
          when (rH2dSendData.burstCnt === 1) {
            rH2dSendData.burstLast := True
          }
          when (rH2dSendData.burstLast) {
            rH2dValid := False
            rH2dSendData.burstLast := False
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
          rState := State.READ_START
        }
      }
      is (State.READ_START) {
        rBurstCnt := 0x0
        rHadH2dFinish := False
        rHadD2hFinish := False

        rH2dValid := True

        rH2dSendData.addr := 0x0
        rH2dSendData.byteEn := U(
          rH2dSendData.byteEn.getWidth bits, default -> True
        )
        rH2dSendData.isWrite := False
        rH2dSendData.src := 0x0

        rH2dSendData.burstCnt := cfg.busCfg.maxBurstSizeMinus1
        rH2dSendData.burstFirst := True
        rH2dSendData.burstLast := False

        rState := State.READ_WAIT_TXN
      }
      is (State.READ_WAIT_TXN) {
        when (
          rH2dValid
          && io.h2dBus.ready
        ) {
          rH2dValid := False
          rH2dSendData.burstFirst := False
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
          rTestData(rBurstCnt).payload.last := myD2hSendData.data
          when (myD2hSendData.burstLast) {
            rHadD2hFinish := True
          }
        }
        when (rHadH2dFinish && rHadD2hFinish) {
          rState := State.DO_COMPARE_TEST_DATA
        }
      }
      is (State.DO_COMPARE_TEST_DATA) {
        val cmpVec = (
          Vec.fill(cfg.busCfg.maxBurstSizeMinus1 + 1)(
            Bool()
          )
        )
        for (testIdx <- 0 until cfg.busCfg.maxBurstSizeMinus1 + 1) {
          cmpVec(testIdx) := (
            rTestData(testIdx).payload.head
            =/= rTestData(testIdx).payload.last
          )
        }
        when (cmpVec.orR) {
          rState := State.FAILED_TEST
        } otherwise {
          rState := State.WRITE_START
        }
      }
      is (State.FAILED_TEST) {
      }
    }
    // END: state machine that does maximum-byte-amount bus bursts
  })
  //--------
  val noBurstRandDataArea = (
    cfg.kind == LcvBusDeviceTesterKind.NoBurstRandData
  ) generate (new Area {
    // BEGIN: previous state machine, which lacks bursts
    val tempCnt = (
      KeepAttribute(
        (rH2dSendData.addr(rH2dSendData.addr.high downto 2))(
          log2Up(rTestData.size) - 1 downto 0
        )
      )
    )
    
    switch (rState) {
      is (State.WRITE_START) {
        //--------
        rHadH2dFinish := False
        rHadD2hFinish := False
        //--------
        rH2dValid := True
        switch (tempCnt) {
          for (testIdx <- 0 until rTestData.size) {
            is (testIdx) {
              rH2dSendData.data := myPrngArea.getCurrRand(testIdx)
              rTestData(tempCnt).payload.head := (
                myPrngArea.getCurrRand(testIdx)
              )
            }
          }
        }
        rTestData(tempCnt).payload.last := (
          U(s"${myPrngArea.myXsWidth}'d0")
        )
        rH2dSendData.byteEn := U(
          rH2dSendData.byteEn.getWidth bits, default -> True
        )
        //rH2dSendData.burstCnt := 1
        rH2dSendData.isWrite := True
        rH2dSendData.src := 0x0
        //--------
        rState := State.WRITE_WAIT_TXN
        //--------
      }
      is (State.WRITE_WAIT_TXN) {
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
          when ((rH2dSendData.addr + 4) < myMaxBaseAddr) {
            rState := State.WRITE_START
            rH2dSendData.addr := rH2dSendData.addr + 4
          } otherwise {
            rState := State.READ_START
            rH2dSendData.addr := 0x0
          }
        }
      }
      is (State.READ_START) {
        //--------
        rHadH2dFinish := False
        rHadD2hFinish := False
        //--------
        rH2dValid := True
        rH2dSendData.isWrite := False
        //--------
        rState := State.READ_WAIT_TXN
        //--------
      }
      is (State.READ_WAIT_TXN) {
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
          rTestData(tempCnt).payload.last := io.d2hBus.payload.data
        }
        when (rHadH2dFinish && rHadD2hFinish) {
          when ((rH2dSendData.addr + 4) < myMaxBaseAddr) {
            rState := State.READ_START
            rH2dSendData.addr := rH2dSendData.addr + 4
          } otherwise {
            //rState := State.WRITE_START
            rState := State.DO_COMPARE_TEST_DATA
            rH2dSendData.addr := 0x0
          }
        }
      }
      is (State.DO_COMPARE_TEST_DATA) {
        when (rH2dSendData.addr < myMaxBaseAddr) {
          rH2dSendData.addr := rH2dSendData.addr + 4
        } otherwise {
          rH2dSendData.addr := 0x0
          rState := State.WRITE_START
        }
        val failure = (
          rTestData(tempCnt).payload.head
          =/= rTestData(tempCnt).payload.last
        )
        when (failure) {
          rState := State.FAILED_TEST
        }
        rTestData(tempCnt).valid := (
          rTestData(tempCnt).payload.head
          === rTestData(tempCnt).payload.last
        )
      }
      is (State.FAILED_TEST) {
      }
    }
    // END: previous state machine, which lacks bursts
  })
  //--------
}
