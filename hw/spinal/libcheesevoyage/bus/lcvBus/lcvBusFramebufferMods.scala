package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

import spinal.lib.graphic.vga._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig

import libcheesevoyage.general._
import libcheesevoyage.gfx._


case class LcvBusSmallMemPalFramebufferConfig(
  busCfg: LcvBusConfig,
  fbSize2d: ElabVec2[Int],
  rgbCfg: RgbConfig,
  cnt2dShift: ElabVec2[Int],
  //palIdxWidth: Int=log2Up(256),
) {
  val palIdxWidth = log2Up(256)
  val palDepth = (1 << palIdxWidth)
}

//case class LcvBusSmallMemPalFramebufferIo(
//  cfg: LcvBusSmallMemPalFramebufferConfig
//) extends Bundle {
//  //--------
//  val palBus = (
//    // palette RAM bus device port 
//    slave(LcvBusIo(cfg=cfg.busCfg))
//  )
//  val fbBus = (
//    // framebuffer RAM bus device port
//    slave(LcvBusIo(cfg=cfg.busCfg))
//  )
//  val csrBus = (
//    // control/status registers bus device port
//    slave(LcvBusIo(cfg=cfg.busCfg))
//  )
//  //--------
//  val pop = (
//    master(Stream(Rgb(c=cfg.rgbCfg)))
//  )
//  //--------
//}
//
//case class LcvBusSmallMemPalDblBufFramebuffer(
//  cfg: LcvBusSmallMemPalFramebufferConfig
//) extends Component {
//  //--------
//  val io = LcvBusSmallMemPalFramebufferIo(cfg=cfg)
//  //--------
//  def busCfg = cfg.busCfg
//
//  def rgbCfg = cfg.rgbCfg
//  def rgbUpWidth = 1 << log2Up(Rgb(c=rgbCfg).asBits.getWidth)
//  def rgbBusRatio = (busCfg.dataWidth / rgbUpWidth).toInt
//
//  def fbSize2d = cfg.fbSize2d
//  def cnt2dShift = cfg.cnt2dShift
//
//  def palIdxUpWidth = (
//    cfg.palIdxWidth
//  )
//  def palIdxBusRatio = (busCfg.dataWidth / palIdxUpWidth).toInt
//
//  require(
//    busCfg.dataWidth
//    >= palIdxUpWidth
//    //== rgbUpWidth
//  )
//  //--------
//  val myVideoCfg = LcvVideoDblLineBufWithCalcPosConfig(
//    rgbCfg=rgbCfg,
//    someSize2d=ElabVec2[Int](
//      x=fbSize2d.x,
//      y=(
//        fbSize2d.y //* cfg.myFbSize2dDblBufFactor
//      ),
//    ),
//    cnt2dShift=cfg.cnt2dShift,
//  )
//  //--------
//  val palIdxPop = (
//    Stream(
//      UInt(palIdxUpWidth bits)
//    )
//  )
//  val myPalMemDepth = (cfg.palDepth / rgbBusRatio).toInt
//
//  val myPalMem = LcvBusMem(
//    cfg=LcvBusMemConfig(
//      busCfg=(
//        cfg.busCfg
//      ),
//      depth=(
//        myPalMemDepth
//      ),
//      initBigInt=Some(
//        Array.fill(myPalMemDepth)(
//          BigInt(0)
//        )
//      ),
//      optHaveExtraRamRdPort=true,
//    )
//  )
//  myPalMem.io.bus << io.palBus
//  myPalMem.io.extraRamRdEn := (
//    RegNext(palIdxPop.fire)
//  )
//  //if (rgbBusRatio == 1) {
//    myPalMem.io.extraRamRdAddr := (
//      palIdxPop.payload.resize(
//        myPalMem.io.extraRamRdAddr.getWidth
//      )
//    )
//  //}
//  val myColFifo = StreamFifo(
//    dataType=(
//      Rgb(rgbCfg)
//    ),
//    depth=16,
//    latency=2,
//    forFMax=true,
//  )
//  io.pop <-/< myColFifo.io.pop
//
//  palIdxPop.ready := (
//    myColFifo.io.occupancy < myColFifo.depth - 4//3
//  )
//  myColFifo.io.push.valid := (
//    RegNext(
//      myPalMem.io.extraRamRdEn,
//      init=False
//    )
//  )
//
//  //if (rgbBusRatio == 1) {
//    myColFifo.io.push.payload.assignFromBits(
//      myPalMem.io.extraRamRdData.asBits
//    )
//  //}
//  //--------
//  val myFbSizeMult = (
//    fbSize2d.y * fbSize2d.x
//  )
//  val myFbDepth = (
//    myFbSizeMult
//    >> log2Up((busCfg.dataWidth / cfg.palIdxWidth).toInt)
//  )
//  val myFbMemArr = Array.fill(2)(
//    LcvBusMem(
//      cfg=LcvBusMemConfig(
//        busCfg=cfg.busCfg,
//        depth=myFbDepth,
//        initBigInt=(
//          Some(
//            Array.fill(myFbDepth)(
//              BigInt(0)
//            )
//          )
//        ),
//        optHaveExtraRamRdPort=true,
//      )
//    )
//  )
//  val rFbBusPageIdxVec = {
//    val temp = Vec.fill(myFbMemArr.size)(
//      Reg(UInt(log2Up(myFbMemArr.size) bits))
//    )
//    for (idx <- 0 until temp.size) {
//      temp.init(idx)
//    }
//    temp
//  }
//
//  myFbMemArr.foreach(fbItem => {
//    fbItem.io.bus.h2dBus.ready := False
//    fbItem.io.bus.d2hBus.payload := (
//      fbItem.io.bus.d2hBus.payload.getZero
//    )
//    fbItem.io.bus.d2hBus.valid := False
//
//    fbItem.io.extraRamRdEn.allowOverride
//    fbItem.io.extraRamRdAddr.allowOverride
//
//    fbItem.io.extraRamRdEn := False
//    fbItem.io.extraRamRdAddr := 0x0
//  })
//
//  switch (rFbBusPageIdxVec.head) {
//    for (idx <- 0 until myFbMemArr.size) {
//      is (idx) {
//        myFbMemArr(idx).io.bus << io.fbBus
//      }
//    }
//  }
//  //--------
//  val rFbColCnt = (
//    Reg(UInt(
//      (log2Up((myVideoCfg.someSize2d.x) + 1) + 1) bits
//    ))
//    init(0x0)
//  )
//  val rFbAddrCnt = (
//    Reg(UInt(
//      log2Up(
//        ((myVideoCfg.someSize2d.y * myVideoCfg.someSize2d.x) + 1) + 1
//      ) bits
//    ))
//    init(0x0)
//  )
//  val rMyFinishedFetchingLine = (
//    cnt2dShift.y > 0
//  ) generate (
//    Reg(Bool(), init=False)
//  )
//
//  //when (myH2dMaybeThrownStm.fire) {
//  //  when (
//  //    rFbColCnt < (
//  //      myVideoCfg.someSize2d.x
//  //      - myBusBurstSizeMaxMult
//  //    )
//  //  ) {
//  //    rFbColCnt := (
//  //      rFbColCnt + myBusBurstSizeMaxMult
//  //    )
//  //  } otherwise {
//  //    rMyFinishedFetchingLine := True
//  //    rFbColCnt := 0x0
//  //  }
//  //}
//  //when (myH2dMaybeThrownStm.fire) {
//  //  val tempCond = (
//  //    rFbAddrCnt
//  //    < (
//  //      (
//  //        (
//  //          myVideoCfg.someSize2d.y * myVideoCfg.someSize2d.x
//  //          / palIdxBusRatio
//  //        )
//  //      )
//  //      - cfg.myBusBurstSizeMax
//  //    )
//  //  )
//  //  when (tempCond) {
//  //    rFbAddrCnt := (
//  //      rFbAddrCnt + cfg.myBusBurstSizeMax
//  //    )
//  //  } otherwise {
//  //    if (
//  //      cfg.dblBuf
//  //      && (cfg.optDblBufAddrSliceVal != None)
//  //    ) {
//  //      rMyH2dAddrSliceVal.valid := !rMyH2dAddrSliceVal.fire
//  //    }
//  //    rFbAddrCnt := 0x0
//  //  }
//  //  if (
//  //    cfg.dblBuf
//  //    && (cfg.optDblBufAddrSliceVal != None)
//  //  ) {
//  //    switch (
//  //      tempCond
//  //      ## rMyH2dAddrSliceVal.fire
//  //    ) {
//  //      is (M"00") {
//  //        rMyH2dAddrSliceVal.payload := (
//  //          cfg.optDblBufAddrSliceVal.get
//  //        )
//  //      }
//  //      is (M"01") {
//  //        rMyH2dAddrSliceVal.payload := (
//  //          cfg.fbMmapCfg.addrSliceValUInt
//  //        )
//  //      }
//  //      default {
//  //      }
//  //    }
//  //  }
//  //}
//
//  val myPushStm = (
//    Stream(UInt(busCfg.dataWidth bits))
//  )
//  //val myPalIdxFifo = StreamFifo(
//  //  dataType=(
//  //    //Rgb(rgbCfg)
//  //    UInt(cfg.palIdxWidth bits)
//  //  ),
//  //  depth=16,
//  //  latency=2,
//  //  forFMax=true,
//  //)
//
//  //myPalMem.io.nonBusRamRdEn := (
//  //  RegNext(palIdxPop.fire)
//  //)
//  ////if (rgbBusRatio == 1) {
//  //  myPalMem.io.nonBusRamRdAddr := (
//  //    palIdxPop.payload.resize(
//  //      myPalMem.io.nonBusRamRdAddr.getWidth
//  //    )
//  //  )
//  ////}
//  //myColFifo.io.push.valid := (
//  //  RegNext(
//  //    myPalMem.io.nonBusRamRdEn,
//  //    init=False
//  //  )
//  //)
//  //if (rgbBusRatio == 1) {
//    //myColFifo.io.push.payload.assignFromBits(
//    //  myPalMem.io.nonBusRamRdData.asBits
//    //)
//  //}
//
//  switch (rFbBusPageIdxVec.last) {
//    for (idx <- 0 until myFbMemArr.size) {
//      is (idx) {
//        def fbItem = myFbMemArr(idx)
//
//        //fbItem.io.nonBusRamRdEn := (
//        //  RegNext(myPushStm.fire)
//        //)
//        //fbItem.io.nonBusRamRdAddr := (
//        //  myPushStm.payload.resize(
//        //    fbItem.io.nonBusRamRdAddr.getWidth
//        //  )
//        //)
//        if (palIdxBusRatio == 4) {
//          // 32-bit busCfg.dataWidth
//          //switch () {
//          //}
//        } else if (palIdxBusRatio == 8) {
//          // 64-bit busCfg.dataWidth
//          require(
//            false,
//            "Not yet implemented"
//          )
//        } else {
//          require(
//            false,
//            s"probably *won't* be implemented, "
//            + s"and not implemented *currently*"
//          )
//        }
//        //myPalIdxFifo.io.push.valid := (
//        //  RegNext(
//        //    fbItem.io.nonBusRamRdEn,
//        //    init=False
//        //  )
//        //)
//        //myPalIdxFifo.io.push.payload.assignFromBits(
//        //  fbItem.io.nonBusRamRdData.asBits
//        //)
//      }
//    }
//  }
//  //io.pop <-/< myColFifo.io.pop
//  //myPushStm <-/< myPalIdxFifo.io.pop
//
//  //palIdxPop.ready := (
//  //  myColFifo.io.occupancy < myColFifo.depth - 4//3
//  //)
//  //myPushStm.ready := (
//  //  myPalIdxFifo.io.occupancy < myPalIdxFifo.depth - 4//3
//  //)
//
//  object MyLineDuplState
//  extends SpinalEnum(defaultEncoding=binaryOneHot) {
//    val
//      FIRST_FETCH,
//      MAIN
//      = newElement();
//  }
//  val myLineDuplArea = (
//    cnt2dShift.y > 0
//  ) generate new Area {
//    val rState = (
//      Reg(MyLineDuplState())
//      init(MyLineDuplState.FIRST_FETCH)
//    )
//    val myLineBufMemWordCnt = (
//      (1 << log2Up(myVideoCfg.someSize2d.x / palIdxBusRatio))
//      * 2
//    )
//    val myLineBufMemCfg = WrPulseRdPipeRamConfig(
//      modType=(
//        Vec.fill(palIdxBusRatio)(
//          UInt(palIdxUpWidth bits)
//        )
//      ),
//      wordType=(
//        Vec.fill(palIdxBusRatio)(
//          UInt(palIdxUpWidth bits)
//        )
//      ),
//      wordCount=myLineBufMemWordCnt,
//      initBigInt={
//        val tempArr = new ArrayBuffer[BigInt]()
//        for (idx <- 0 until myLineBufMemWordCnt) {
//          tempArr += BigInt(0)
//        }
//        Some(Array.fill(1)(tempArr))
//      },
//      setWordFunc=(
//        outp: Vec[UInt],
//        inp: Vec[UInt],
//        rdMemWord: Vec[UInt],
//        upIsFiring: Bool,
//        myExternalInpCond: Bool,
//        wrPulseVec: Vec[Flow[
//          PipeSimpleDualPortMemDrivePayload[
//            Vec[UInt]
//          ]
//        ]],
//      ) => {
//        outp := rdMemWord
//      }
//    )
//    val myLineBufArrSize = 2
//    val rMyLineBufArrIdxVec = {
//      val temp = Vec.fill(myLineBufArrSize)(
//        Reg(UInt(log2Up(myLineBufArrSize) bits))
//      )
//      for (idx <- 0 until temp.size) {
//        temp(idx).init(temp(idx).getZero)
//      }
//      temp
//    }
//
//    val myDblLineBuf = (
//      WrPulseRdPipeRamSdpPipe(cfg=myLineBufMemCfg)
//    )
//
//    val myWrPulse = (
//      cloneOf(myDblLineBuf.io.wrPulse)
//    )
//
//    myDblLineBuf.io.wrPulse <-< myWrPulse
//
//    val rWrLineBufAddrCnt = (
//      Reg(UInt(
//        myWrPulse.addr.getWidth - 1 
//        + cnt2dShift.x
//        bits
//      ))
//      init(0x0)
//    )
//    val rSeenWrPulseFinish = Reg(Bool(), init=False)
//    val myMaybeReptPushStm = cloneOf(myPushStm)
//    myMaybeReptPushStm <-/< myPushStm
//    myMaybeReptPushStm.ready := True
//
//    myWrPulse.valid := (
//      if (cnt2dShift.x == 0) (
//        myMaybeReptPushStm.fire
//      ) else (
//        myMaybeReptPushStm.fire
//      )
//    )
//    for (idx <- 0 until myWrPulse.data.size) {
//      myWrPulse.data(idx).assignFromBits(
//        myMaybeReptPushStm.payload(
//          (idx + 1) * palIdxUpWidth - 1
//          downto idx * palIdxUpWidth
//        ).resize(myWrPulse.data(idx).asBits.getWidth).asBits
//      )
//    }
//    val mySeenRdPipeFinishRstVal = (
//      (1 << cnt2dShift.y) - 1//2//1//2
//    )
//    val rSeenRdAddrPipeFinish = (
//      Reg(UInt((cnt2dShift.y + 2) bits))
//      init(mySeenRdPipeFinishRstVal)
//    )
//    val rSeenRdDataPipeFinish = (
//      Reg(UInt((cnt2dShift.y + 2) bits))
//      init(
//        mySeenRdPipeFinishRstVal
//      )
//    )
//
//    val rRdLineBufAddrCnt = (
//      Vec[UInt](
//        List[UInt](
//          (
//            Reg(UInt(
//              myDblLineBuf.io.rdAddrPipe.addr.getWidth - 1
//              bits
//            ))
//            init(0x0)
//          ),
//          (
//            Reg(UInt(
//              myDblLineBuf.io.rdAddrPipe.addr.getWidth - 1
//              bits
//            ))
//            init(0x0)
//          )
//        )
//      )
//    )
//    val myRdAddrPipeStm = (
//      cloneOf(myDblLineBuf.io.rdAddrPipe)
//    )
//    val myRdDataPipeStm = (
//      cloneOf(myDblLineBuf.io.rdDataPipe)
//    )
//
//    val myMaybeReptRdDataPipeStm = Vec.fill(2)(
//      cloneOf(myRdDataPipeStm)
//    )
//    myMaybeReptRdDataPipeStm := myMaybeReptRdDataPipeStm.getZero
//
//    myRdAddrPipeStm.valid := (
//      !rSeenRdAddrPipeFinish.msb
//      && (
//        rState === MyLineDuplState.MAIN
//      )
//    )
//    myRdAddrPipeStm.payload := myRdAddrPipeStm.payload.getZero
//    myRdAddrPipeStm.addr.allowOverride
//    myRdAddrPipeStm.addr := (
//      Cat(
//        rMyLineBufArrIdxVec.head,
//        rRdLineBufAddrCnt.head(
//          rRdLineBufAddrCnt.head.high
//          downto 0
//        )
//      ).asUInt
//    )
//    myDblLineBuf.io.rdAddrPipe <-/< myRdAddrPipeStm
//    myRdDataPipeStm <-/< myDblLineBuf.io.rdDataPipe
//    myRdDataPipeStm.ready := False
//
//    palIdxPop.valid := False
//    palIdxPop.payload := (
//      RegNext(palIdxPop.payload, init=palIdxPop.payload.getZero)
//    )
//
//    myWrPulse.addr := (
//      Cat(
//        rMyLineBufArrIdxVec.last,
//        rWrLineBufAddrCnt(
//          rWrLineBufAddrCnt.high - 1
//          downto 0
//        ),
//      ).asUInt
//    )
//    switch (
//      rSeenWrPulseFinish
//      ## myMaybeReptPushStm.fire
//      ## (
//        rWrLineBufAddrCnt
//        < (myVideoCfg.someSize2d.x / palIdxBusRatio) - 1 
//      )
//    ) {
//      is (M"011") {
//        // fire, rWrLineBufAddrCnt < width
//        rWrLineBufAddrCnt := rWrLineBufAddrCnt + 1
//      }
//      is (M"010") {
//        // fire, !(rWrLineBufAddrCnt < width)
//        rSeenWrPulseFinish := True
//      }
//      default {
//      }
//    }
//
//    switch (
//      rSeenRdAddrPipeFinish.msb
//      ## myRdAddrPipeStm.fire
//      ## (
//        rRdLineBufAddrCnt.head
//        < (myVideoCfg.someSize2d.x / palIdxBusRatio) - 1
//      )
//    ) {
//      is (M"011") {
//        // fire, rRdLineBufAddrCnt.head < width
//        rRdLineBufAddrCnt.head := rRdLineBufAddrCnt.head + 1
//      }
//      is (M"010") {
//        // fire, !(rRdLineBufAddrCnt < width)
//        rSeenRdAddrPipeFinish := rSeenRdAddrPipeFinish - 1
//        when (!(rSeenRdAddrPipeFinish - 1).msb) {
//          rRdLineBufAddrCnt.head := 0x0
//        }
//      }
//      default {
//      }
//    }
//
//    switch (
//      rSeenRdDataPipeFinish.msb
//      ## myMaybeReptRdDataPipeStm.head.fire
//      ## (
//        rRdLineBufAddrCnt.last
//        < (myVideoCfg.someSize2d.x / palIdxBusRatio) - 1
//      )
//    ) {
//      is (M"011") {
//        // fire, rRdLineBufAddrCnt.last < width
//        rRdLineBufAddrCnt.last := rRdLineBufAddrCnt.last + 1
//      }
//      is (M"010") {
//        // fire, !(rRdLineBufAddrCnt < width)
//        rSeenRdDataPipeFinish := rSeenRdDataPipeFinish - 1
//        when (
//          !(rSeenRdDataPipeFinish - 1).msb
//        ) {
//          rRdLineBufAddrCnt.last := 0x0
//        }
//      }
//      default {
//      }
//    }
//    val rMyPopVecIdx = (
//      Reg(UInt(
//        cnt2dShift.x + log2Up(palIdxBusRatio) bits
//      ))
//      init(0x0)
//    )
//
//    switch (rState) {
//      is (MyLineDuplState.FIRST_FETCH) {
//        when (rSeenWrPulseFinish) {
//          rState := MyLineDuplState.MAIN
//
//          rSeenWrPulseFinish := False
//          rWrLineBufAddrCnt := 0x0
//          rMyFinishedFetchingLine := False
//          rMyLineBufArrIdxVec.last := (
//            rMyLineBufArrIdxVec.last + 1
//          )
//        }
//      }
//      is (MyLineDuplState.MAIN) {
//        when (
//          rSeenWrPulseFinish
//          && rSeenRdAddrPipeFinish.msb
//          && rSeenRdDataPipeFinish.msb
//        ) {
//          rSeenWrPulseFinish := False
//          rSeenRdAddrPipeFinish := mySeenRdPipeFinishRstVal
//          rSeenRdDataPipeFinish := mySeenRdPipeFinishRstVal
//          rWrLineBufAddrCnt := 0x0
//          rRdLineBufAddrCnt.foreach(item => {
//            item := 0
//          })
//          rMyFinishedFetchingLine := False
//          rMyLineBufArrIdxVec.foreach(item => {
//            item := item + 1
//          })
//        } otherwise {
//          if (cnt2dShift.x == 0) {
//            require(
//              false,
//              "not yet implemented"
//            )
//          } else {
//            myMaybeReptRdDataPipeStm.head <-/< myRdDataPipeStm.repeat(
//              times=((1 << cnt2dShift.x) * palIdxBusRatio)
//            )._1
//            myMaybeReptRdDataPipeStm.last <-/< (
//              myMaybeReptRdDataPipeStm.head
//            )
//            myMaybeReptRdDataPipeStm.last.translateInto(palIdxPop)(
//              dataAssignment=(outp, inp) => {
//                outp := inp(rMyPopVecIdx(
//                  rMyPopVecIdx.high
//                  downto rMyPopVecIdx.getWidth - log2Up(palIdxBusRatio)
//                ))
//              }
//            )
//            when (myMaybeReptRdDataPipeStm.last.fire) {
//              rMyPopVecIdx := rMyPopVecIdx + 1
//            }
//          }
//        }
//      }
//    }
//  }
//  //--------
//  object CsrBusState
//  extends SpinalEnum(defaultEncoding=binaryOneHot) {
//    val
//      IDLE,
//      DO_READ_CSR,
//      DO_WRITE_CSR
//      = newElement();
//  }
//  val rCsrBusState = (
//    Reg(CsrBusState())
//    init(CsrBusState.IDLE)
//  )
//  val rSavedCsrBusH2dPayload = {
//    val temp = Reg(cloneOf(io.csrBus.h2dBus.payload))
//    temp.init(temp.getZero)
//    temp
//  }
//
//  io.csrBus.h2dBus.ready := False
//  io.csrBus.d2hBus.payload := io.csrBus.d2hBus.payload.getZero
//  io.csrBus.d2hBus.valid := False
//
//  io.csrBus.d2hBus.src.allowOverride
//  io.csrBus.d2hBus.src := rSavedCsrBusH2dPayload.src
//
//  io.csrBus.d2hBus.data.allowOverride
//  io.csrBus.d2hBus.data := (
//    rFbBusPageIdxVec.head.resize(io.csrBus.d2hBus.data.getWidth)
//  )
//
//  switch (rCsrBusState) {
//    is (CsrBusState.IDLE) {
//      rSavedCsrBusH2dPayload := io.csrBus.h2dBus.payload
//
//      io.csrBus.h2dBus.ready := True
//
//      switch (
//        io.csrBus.h2dBus.valid
//        ## io.csrBus.h2dBus.isWrite
//      ) {
//        is (M"10") {
//          rCsrBusState := CsrBusState.DO_READ_CSR
//        }
//        is (M"11") {
//          rCsrBusState := CsrBusState.DO_WRITE_CSR
//          rFbBusPageIdxVec.last := rFbBusPageIdxVec.head
//        }
//        default {
//        }
//      }
//    }
//    is (CsrBusState.DO_READ_CSR) {
//      io.csrBus.d2hBus.valid := True
//      when (io.csrBus.d2hBus.ready) {
//        rCsrBusState := CsrBusState.IDLE
//      }
//    }
//    is (CsrBusState.DO_WRITE_CSR) {
//      rFbBusPageIdxVec.head := (
//        rSavedCsrBusH2dPayload.data.resize(rFbBusPageIdxVec.head.getWidth)
//      )
//
//      io.csrBus.d2hBus.valid := True
//      when (io.csrBus.d2hBus.ready) {
//        rCsrBusState := CsrBusState.IDLE
//      }
//    }
//  }
//}

case class LcvBusFramebufferConfig(
  //busCfg: LcvBusConfig,
  fbMmapCfg: LcvBusMemMapConfig,
  optPalIdxWidth: Option[Int]=Some(log2Up(256)),
  rgbCfg: RgbConfig,
  //vgaTimingInfo: LcvVgaTimingInfo,
  fbSize2d: ElabVec2[Int],
  dblBuf: Boolean,
  optDblBufAddrSliceVal: Option[Int],
  //dblBufMmapCfg: Option[LcvBusMemMapConfig],

  cnt2dShift: ElabVec2[Int],
  //cnt2dShiftOne: ElabVec2[Boolean], // for line/pixel doubling
) {
  //val dblBuf = (dblBufMmapCfg != None)
  //val cnt2dShift = ElabVec2[Int](
  //  x=(if (cnt2dShiftOne.x) (1) else (0)),
  //  y=(if (cnt2dShiftOne.y) (1) else (0)),
  //)
  require(
    fbMmapCfg.optSliceSize == None
  )
  require(
    fbMmapCfg.optAddrSliceVal != None
  )

  //val optPalIdxPopWidth = (
  //  optPalIdxWidth match {
  //    case Some(myPalIdxWidth) => (
  //      (1 << log2Up(myPalIdxWidth)).max(8)
  //    )
  //    case None => (
  //      0
  //    )
  //  }
  //)

  val optPalDepth = (
    optPalIdxWidth match {
      case Some(myPalIdxWidth) => (
        1 << myPalIdxWidth
      )
      case None => (
        0 
      )
    }
  )
  //if (!dblBuf) {
  //  require(
  //    fbMmapCfg.addrSliceWidth >= 1
  //  )
  //} else {
  //  require(
  //    fbMmapCfg.addrSliceWidth >= 2
  //  )
  //}
  require(
    cnt2dShift.x >= 0
  )
  require(
    cnt2dShift.y >= 0
  )
  def busCfg = fbMmapCfg.busCfg
  //def fbSize2d = vgaTimingInfo.fbSize2d
  val myFbSize2dDblBufFactor = (
    if (dblBuf && (optDblBufAddrSliceVal == None)) (2) else (1)
  )
  val myFbSize2dMult = (
    fbSize2d.x * fbSize2d.y
    * myFbSize2dDblBufFactor
  )

  //val myFbCntOverflow = myFbSize2dMult
  val myBusBurstSizeMax = busCfg.maxBurstSizeMinus1 + 1
  def calcAlignedFbCntMax(
    someFbCntMax: Int
  ) = (
    someFbCntMax - 1
    + myBusBurstSizeMax
    - ((someFbCntMax - 1) % myBusBurstSizeMax)
  )
  val myAlignedFbCntMax = (
    //myFbSize2dMult - 1
    //+ myBusBurstSizeMax
    //- ((myFbSize2dMult - 1) % myBusBurstSizeMax)
    calcAlignedFbCntMax(someFbCntMax=myFbSize2dMult)
  )
  val myAlignedFbCnt2dMax = ElabVec2[Int](
    x=calcAlignedFbCntMax(someFbCntMax=fbSize2d.x),
    y=calcAlignedFbCntMax(someFbCntMax=fbSize2d.y),
  )
  //println(
  //  s"${fbSize2d} "
  //  + s"${myAlignedFbCntMax} ${myAlignedFbCnt2dMax} "
  //  + s"${myBusBurstSizeMax}"
  //)


  //require(
  //  busCfg.allowBurst
  //)

  //require(
  //  busCfg.dataWidth
  //  >= Rgb(c=rgbCfg).asBits.getWidth
  //)

  require(
    (1 << (busCfg.addrWidth - log2Up(busCfg.dataWidth / 8)))
    >= (1 << log2Up(myAlignedFbCntMax + 1))
  )
}


case class LcvBusFramebufferCtrlIo(
  cfg: LcvBusFramebufferConfig
) extends Bundle {
  //--------
  val palLoBus = (
    cfg.optPalIdxWidth != None
  ) generate (
    slave(LcvBusIo(cfg=cfg.busCfg))
  )
  val csrLoBus = (
    cfg.optPalIdxWidth != None
    && cfg.dblBuf
    && cfg.optDblBufAddrSliceVal != None
  ) generate (
    slave(LcvBusIo(cfg=cfg.busCfg))
  )
  val hiBus = master(LcvBusIo(cfg=cfg.busCfg))
  val pop = (
    master(Stream(Rgb(c=cfg.rgbCfg)))
  )
  //val palIdxPop = (
  //  cfg.optPalIdxWidth != None
  //) generate (
  //  master(Stream(
  //    UInt(
  //      //cfg.busCfg.dataWidth bits
  //      cfg.optPalIdxPopWidth bits
  //    )
  //  ))
  //)
  //--------
}

case class LcvBusFramebufferCtrl(
  cfg: LcvBusFramebufferConfig
) extends Component {
  val io = LcvBusFramebufferCtrlIo(cfg=cfg)

  val myNonPalArea = (
    cfg.optPalIdxWidth == None
  ) generate new Area {
    val myFbCtrl = LcvBusFramebufferCtrlNonPal(cfg=cfg)
    io <> myFbCtrl.io
  }

  val myPalArea = (
    cfg.optPalIdxWidth != None
  ) generate new Area {
    val myFbCtrl = LcvBusFramebufferCtrlPal(cfg=cfg)
    io <> myFbCtrl.io
  }
}

private[libcheesevoyage] case class LcvBusFramebufferCtrlPal(
  cfg: LcvBusFramebufferConfig
) extends Component {
  def busCfg = cfg.busCfg

  def rgbCfg = cfg.rgbCfg
  def rgbUpWidth = 1 << log2Up(Rgb(c=rgbCfg).asBits.getWidth)
  def rgbBusRatio = (busCfg.dataWidth / rgbUpWidth).toInt

  require(
    rgbBusRatio == 1
    //|| rgbBusRatio == 2
  )

  def fbSize2d = cfg.fbSize2d
  def cnt2dShift = cfg.cnt2dShift
  def palIdxUpWidth = (
    //cfg.optPalIdxPopWidth
    cfg.optPalIdxWidth.get
    //1 << log2Up(Rgb(c=rgbCfg).asBits.getWidth)
  )
  def palIdxBusRatio = (busCfg.dataWidth / palIdxUpWidth).toInt
  def myBusBurstSizeMaxMult = cfg.myBusBurstSizeMax * palIdxBusRatio

  val myVideoCfg = LcvVideoDblLineBufWithCalcPosConfig(
    rgbCfg=rgbCfg,
    someSize2d=ElabVec2[Int](
      x=fbSize2d.x,
      y=(
        //(fbSize2d.y * (if (cfg.dblBuf) (2) else (1)))
        //fbSize2d.y
        //fbSize2d.y * (if (cfg.dblBuf) (2) else (1))
        fbSize2d.y * cfg.myFbSize2dDblBufFactor
      ),
    ),
    cnt2dShift=cfg.cnt2dShift,
  )

  require(
    busCfg.allowBurst
  )
  require(
    busCfg.dataWidth
    >= palIdxUpWidth
    //== rgbUpWidth
  )
  require(
    (fbSize2d.x % myBusBurstSizeMaxMult) == 0,
    s"fbSize2d.x:${fbSize2d.x} must be an exact integer multiple "
    + s"of myBusBurstSizeMaxMult:${myBusBurstSizeMaxMult}"
  )

  val io = LcvBusFramebufferCtrlIo(cfg=cfg)

  //--------
  val rFbBusPageIdxVec = (
    io.csrLoBus != null
  ) generate {
    val temp = Vec.fill(2)(
      Reg(UInt(log2Up(2) bits))
    )
    for (idx <- 0 until temp.size) {
      temp(idx).init(idx)
    }
    temp
  }
  //--------

  val palIdxPop = (
    Stream(
      UInt(
        //cfg.busCfg.dataWidth bits
        //cfg.optPalIdxPopWidth bits
        palIdxUpWidth bits
      )
    )
  )
  val myPalMemDepth = (cfg.optPalDepth / rgbBusRatio).toInt

  val myPalMem = LcvBusMem(
    cfg=LcvBusMemConfig(
      busCfg=(
        cfg.busCfg
      ),
      depth=(
        myPalMemDepth
      ),
      initBigInt=Some(
        Array.fill(myPalMemDepth)(
          BigInt(0)
        )
      ),
      optHaveExtraRamRdPort=true,
    )
  )
  myPalMem.io.bus << io.palLoBus
  myPalMem.io.extraRamRdEn := (
    //True
    //RegNext(
    //  myPalMem.io.nonBusRamRdEn,
    //  init=myPalMem.io.nonBusRamRdEn.getZero
    //)
    RegNext(palIdxPop.fire)
  )
  if (rgbBusRatio == 1) {
    myPalMem.io.extraRamRdAddr := (
      palIdxPop.payload.resize(
        myPalMem.io.extraRamRdAddr.getWidth
      )
      //palIdxPop.payload(
      //  myPalMem.io.nonBusRamRdAddr.getWidth
      //)
    )
  } else {
    myPalMem.io.extraRamRdAddr := (
      palIdxPop.payload(
        myPalMem.io.extraRamRdAddr.high + 1
        downto 1
      )
      //palIdxPop.payload.resize(
      //  myPalMem.io.nonBusRamRdAddr.getWidth
      //)
      //palIdxPop.payload(
      //  myPalMem.io.nonBusRamRdAddr.getWidth
      //)
    )
  }
  val myColFifo = StreamFifo(
    dataType=(
      Rgb(rgbCfg)
      //UInt(cfg.optPalIdxWidth.get bits)
    ),
    depth=16,
    latency=2,
    forFMax=true,
  )
  io.pop <-/< myColFifo.io.pop

  palIdxPop.ready := (
    myColFifo.io.occupancy < myColFifo.depth - 4//3
  )
  myColFifo.io.push.valid := (
    RegNext(
      myPalMem.io.extraRamRdEn,
      init=False
    )
  )

  if (rgbBusRatio == 1) {
    myColFifo.io.push.payload.assignFromBits(
      myPalMem.io.extraRamRdData.asBits
    )
  } else {
    myColFifo.io.push.payload.assignFromBits(
      Mux(
        //RegNext(
        //  RegNextWhen(
        //    palIdxPop.payload.lsb,
        //    cond=myPalMem.io.nonBusRamRdEn,
        //  )
        //),
        //RegNext(
        //  RegNextWhen(
        //    palIdxPop.payload.lsb,
        //    cond=palIdxPop.fire,
        //    init=False,
        //  ),
        //  init=False
        //),
        //palIdxPop.payload.lsb,
        RegNextWhen(
          RegNext(
            palIdxPop.payload.lsb,
          ),
          cond=myPalMem.io.extraRamRdEn,
        ),
        myPalMem.io.extraRamRdData(
          myPalMem.io.extraRamRdData.high
          downto (myPalMem.io.extraRamRdData.getWidth >> 1)
        ).asBits,
        myPalMem.io.extraRamRdData(
          (myPalMem.io.extraRamRdData.getWidth >> 1) - 1
          downto 0
        ).asBits,
      )
    )
    //switch (myPalMem.io.nonBusRamRdAddr.lsb) {
    //  is (False) {
    //    myColFifo.io.push.payload.assignFromBits(
    //      myPalMem.io.nonBusRamRdData.asBits
    //    )
    //  }
    //  is (True) {
    //  }
    //}
  }

  val myH2dStm = Vec.fill(3)(
    cloneOf(io.hiBus.h2dBus)
  )

  myH2dStm.head.valid := True
  io.hiBus.h2dBus << myH2dStm.last
  val myH2dThrowCond = Bool()
  val myH2dMaybeThrownStm = (
    if (cnt2dShift.y == 0) (
      myH2dStm.head//.throwWhen(myH2dThrowCond)
    ) else (
      myH2dStm.head.throwWhen(myH2dThrowCond)
    )
  )
  val rMyFinishedFetchingLine = (
    cnt2dShift.y > 0
  ) generate (
    Reg(Bool(), init=False)
  )
  if (cnt2dShift.y > 0) {
    myH2dThrowCond := rMyFinishedFetchingLine
  }

  val rMyH2dAddrSliceVal = (
    cfg.dblBuf
    && (cfg.optDblBufAddrSliceVal != None)
  ) generate {
    val temp = Reg(Flow(UInt(cfg.fbMmapCfg.addrSliceWidth bits)))
    temp.valid.init(False)
    temp.payload.init(cfg.fbMmapCfg.addrSliceValUInt)
    temp
  }

  myH2dStm(1) <-/< myH2dMaybeThrownStm
  myH2dStm(1).translateInto(myH2dStm.last)(
    dataAssignment=(outp, inp) => {
      outp := inp
      outp.addr.allowOverride
      if (
        cfg.dblBuf
        && (cfg.optDblBufAddrSliceVal != None)
      ) {
        outp.addr(cfg.fbMmapCfg.addrSliceRange) := (
          //cfg.fbMmapCfg.addrSliceValUInt
          rMyH2dAddrSliceVal.payload
        )
      } else {
        outp.addr(cfg.fbMmapCfg.addrSliceRange) := (
          cfg.fbMmapCfg.addrSliceValUInt
        )
      }
    }
  )

  val rFbColCnt = (
    Reg(UInt(
      (log2Up((myVideoCfg.someSize2d.x) + 1) + 1) bits
    ))
    init(0x0)
  )
  val rFbAddrCnt = (
    Reg(UInt(
      log2Up(
        ((myVideoCfg.someSize2d.y * myVideoCfg.someSize2d.x) + 1) + 1
      ) bits
    ))
    init(0x0)
  )
  when (myH2dMaybeThrownStm.fire) {
    when (
      rFbColCnt < (
        myVideoCfg.someSize2d.x
        - myBusBurstSizeMaxMult
      )
    ) {
      rFbColCnt := (
        rFbColCnt + myBusBurstSizeMaxMult
      )
    } otherwise {
      rMyFinishedFetchingLine := True
      rFbColCnt := 0x0
    }
  }
  when (myH2dMaybeThrownStm.fire) {
    val tempCond = (
      rFbAddrCnt
      < (
        (
          (
            myVideoCfg.someSize2d.y * myVideoCfg.someSize2d.x
            / palIdxBusRatio
          )
        )
        - cfg.myBusBurstSizeMax
      )
    )
    when (tempCond) {
      rFbAddrCnt := (
        rFbAddrCnt + cfg.myBusBurstSizeMax
      )
    } otherwise {
      if (
        cfg.dblBuf
        && (cfg.optDblBufAddrSliceVal != None)
      ) {
        //rMyH2dAddrSliceVal.valid := !rMyH2dAddrSliceVal.fire
        rMyH2dAddrSliceVal.valid := rFbBusPageIdxVec.last.lsb
      }
      rFbAddrCnt := 0x0
    }
    if (
      cfg.dblBuf
      && (cfg.optDblBufAddrSliceVal != None)
    ) {
      switch (
        tempCond
        ## rMyH2dAddrSliceVal.fire
      ) {
        is (M"00") {
          rMyH2dAddrSliceVal.payload := (
            cfg.optDblBufAddrSliceVal.get
          )
        }
        is (M"01") {
          rMyH2dAddrSliceVal.payload := (
            cfg.fbMmapCfg.addrSliceValUInt
          )
        }
        default {
        }
      }
    }
  }
  def myDataAssignmentH2d(
    outp: LcvBusH2dPayload,
    inp: Data,
  ): Unit = {
    outp.addr := (
      Cat(
        rFbAddrCnt(rFbAddrCnt.high downto 0),
        U(s"${log2Up(busCfg.dataWidth / 8)}'d0")
      ).asUInt.resize(outp.addr.getWidth)
    )
    outp.src := 0x0
    outp.data := 0x0
    outp.byteSize := log2Up(busCfg.dataWidth / 8)
    outp.isWrite := False

    if (busCfg.allowBurst) {
      outp.burstFirst := True
      outp.burstCnt := busCfg.maxBurstSizeMinus1
      outp.burstLast := True//False
    }
  }
  myDataAssignmentH2d(
    outp=myH2dStm.head.payload,
    inp=null,
  )

  //--------
  val myPushStm = (
    Stream(UInt(busCfg.dataWidth bits))
  )
  val myD2hStm = Vec.fill(2)(
    cloneOf(io.hiBus.d2hBus)
  )
  myD2hStm.head <-/< io.hiBus.d2hBus
  myD2hStm.last <-/< myD2hStm.head

  myD2hStm.last.translateInto(myPushStm)(
    dataAssignment=(outp, inp) => {
      outp.assignFromBits(inp.data.asBits.resize(outp.asBits.getWidth))
    }
  )
  //--------
  object MyLineDuplState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      FIRST_FETCH,
      MAIN
      = newElement();
  }
  val myLineDuplArea = (
    cnt2dShift.y > 0
  ) generate new Area {
    val rState = (
      Reg(MyLineDuplState())
      init(MyLineDuplState.FIRST_FETCH)
    )
    val myLineBufMemWordCnt = (
      ////someSize2d.x //* (1 << cnt2dShift.x)
      ////* 2

      //// This *may* waste space but maybe not? It does round up to the
      //// nearest power of two, but I have a few comments about that:
      //// (1) It allows us to avoid using a multiplier for the address
      ////    calculation
      //// (2) FPGA Block RAM primitives are large enough
      ////    that maybe it's not a problem anyway?
      //// (3) I did some math, and even with a 1920x1080 resolution
      ////    (i.e. 1080p widescreen),
      ////    the calculation for a double-buffered line buffer only uses
      ////    4096 addresses. This becomes 16 kiB with 32 bpp colors
      ////    though. That's a big chunk of block RAM I guess? On the other
      ////    hand, you probably only need one of these double-buffered
      ////    line buffers (i.e. two line buffers).
      //(1 << log2Up(someSize2d.x))
      //* 2

      (1 << log2Up(myVideoCfg.someSize2d.x / palIdxBusRatio))
      * 2
    )
    val myLineBufMemCfg = WrPulseRdPipeRamConfig(
      modType=(
        Vec.fill(palIdxBusRatio)(
          //Rgb(rgbCfg)
          UInt(palIdxUpWidth bits)
        )
      ),
      wordType=(
        Vec.fill(palIdxBusRatio)(
          //Rgb(rgbCfg)
          UInt(palIdxUpWidth bits)
        )
      ),
      wordCount=myLineBufMemWordCnt,
      //pipeName="LcvVideoDblLineBufWithCalcPos",
      initBigInt={
        val tempArr = new ArrayBuffer[BigInt]()
        for (idx <- 0 until myLineBufMemWordCnt) {
          tempArr += BigInt(0)
        }
        Some(Array.fill(1)(tempArr))
      },
      setWordFunc=(
        outp: Vec[UInt],
        inp: Vec[UInt],
        rdMemWord: Vec[UInt],
        upIsFiring: Bool,
        myExternalInpCond: Bool,
        wrPulseVec: Vec[Flow[
          PipeSimpleDualPortMemDrivePayload[
            Vec[UInt]
          ]
        ]],
      ) => {
        outp := rdMemWord
      }
    )
    val myLineBufArrSize = 2
    val rMyLineBufArrIdxVec = {
      val temp = Vec.fill(myLineBufArrSize)(
        Reg(UInt(log2Up(myLineBufArrSize) bits))
      )
      for (idx <- 0 until temp.size) {
        temp(idx).init(temp(idx).getZero)
      }
      temp
    }

    val myDblLineBuf = (
      WrPulseRdPipeRamSdpPipe(cfg=myLineBufMemCfg)
    )

    val myWrPulse = (
      cloneOf(myDblLineBuf.io.wrPulse)
    )

    myDblLineBuf.io.wrPulse <-< myWrPulse

    val rWrLineBufAddrCnt = (
      Reg(UInt(
        myWrPulse.addr.getWidth - 1 
        + cnt2dShift.x
        bits
      ))
      init(0x0)
    )
    val rSeenWrPulseFinish = Reg(Bool(), init=False)
    val myMaybeReptPushStm = cloneOf(myPushStm)
    myMaybeReptPushStm <-/< myPushStm
    myMaybeReptPushStm.ready := True

    myWrPulse.valid := (
      if (cnt2dShift.x == 0) (
        myMaybeReptPushStm.fire
      ) else (
        myMaybeReptPushStm.fire
      )
    )
    for (idx <- 0 until myWrPulse.data.size) {
      myWrPulse.data(idx).assignFromBits(
        myMaybeReptPushStm.payload(
          (idx + 1) * palIdxUpWidth - 1
          downto idx * palIdxUpWidth
        ).resize(myWrPulse.data(idx).asBits.getWidth).asBits
      )
    }
    val mySeenRdPipeFinishRstVal = (
      (1 << cnt2dShift.y) - 1//2//1//2
    )
    val rSeenRdAddrPipeFinish = (
      Reg(UInt((cnt2dShift.y + 2) bits))
      init(mySeenRdPipeFinishRstVal)
    )
    val rSeenRdDataPipeFinish = (
      Reg(UInt((cnt2dShift.y + 2) bits))
      init(
        mySeenRdPipeFinishRstVal
      )
    )

    val rRdLineBufAddrCnt = (
      Vec[UInt](
        List[UInt](
          (
            Reg(UInt(
              myDblLineBuf.io.rdAddrPipe.addr.getWidth - 1
              bits
            ))
            init(0x0)
          ),
          (
            Reg(UInt(
              myDblLineBuf.io.rdAddrPipe.addr.getWidth - 1
              bits
            ))
            init(0x0)
          )
        )
      )
    )
    val myRdAddrPipeStm = (
      cloneOf(myDblLineBuf.io.rdAddrPipe)
    )
    val myRdDataPipeStm = (
      cloneOf(myDblLineBuf.io.rdDataPipe)
    )

    val myMaybeReptRdDataPipeStm = Vec.fill(2)(
      cloneOf(myRdDataPipeStm)
    )
    myMaybeReptRdDataPipeStm := myMaybeReptRdDataPipeStm.getZero

    myRdAddrPipeStm.valid := (
      !rSeenRdAddrPipeFinish.msb
      && (
        rState === MyLineDuplState.MAIN
      )
    )
    myRdAddrPipeStm.payload := myRdAddrPipeStm.payload.getZero
    myRdAddrPipeStm.addr.allowOverride
    myRdAddrPipeStm.addr := (
      Cat(
        rMyLineBufArrIdxVec.head,
        rRdLineBufAddrCnt.head(
          rRdLineBufAddrCnt.head.high
          downto 0
        )
      ).asUInt
    )
    myDblLineBuf.io.rdAddrPipe <-/< myRdAddrPipeStm
    myRdDataPipeStm <-/< myDblLineBuf.io.rdDataPipe
    myRdDataPipeStm.ready := False

    palIdxPop.valid := False
    palIdxPop.payload := (
      RegNext(palIdxPop.payload, init=palIdxPop.payload.getZero)
    )

    myWrPulse.addr := (
      Cat(
        rMyLineBufArrIdxVec.last,
        rWrLineBufAddrCnt(
          rWrLineBufAddrCnt.high - 1
          downto 0
        ),
      ).asUInt
    )
    switch (
      rSeenWrPulseFinish
      ## myMaybeReptPushStm.fire
      ## (
        rWrLineBufAddrCnt
        < (myVideoCfg.someSize2d.x / palIdxBusRatio) - 1 
      )
    ) {
      is (M"011") {
        // fire, rWrLineBufAddrCnt < width
        rWrLineBufAddrCnt := rWrLineBufAddrCnt + 1
      }
      is (M"010") {
        // fire, !(rWrLineBufAddrCnt < width)
        rSeenWrPulseFinish := True
      }
      default {
      }
    }

    switch (
      rSeenRdAddrPipeFinish.msb
      ## myRdAddrPipeStm.fire
      ## (
        rRdLineBufAddrCnt.head
        < (myVideoCfg.someSize2d.x / palIdxBusRatio) - 1
      )
    ) {
      is (M"011") {
        // fire, rRdLineBufAddrCnt.head < width
        rRdLineBufAddrCnt.head := rRdLineBufAddrCnt.head + 1
      }
      is (M"010") {
        // fire, !(rRdLineBufAddrCnt < width)
        rSeenRdAddrPipeFinish := rSeenRdAddrPipeFinish - 1
        when (!(rSeenRdAddrPipeFinish - 1).msb) {
          rRdLineBufAddrCnt.head := 0x0
        }
      }
      default {
      }
    }

    switch (
      rSeenRdDataPipeFinish.msb
      ## myMaybeReptRdDataPipeStm.head.fire
      ## (
        rRdLineBufAddrCnt.last
        < (myVideoCfg.someSize2d.x / palIdxBusRatio) - 1
      )
    ) {
      is (M"011") {
        // fire, rRdLineBufAddrCnt.last < width
        rRdLineBufAddrCnt.last := rRdLineBufAddrCnt.last + 1
      }
      is (M"010") {
        // fire, !(rRdLineBufAddrCnt < width)
        rSeenRdDataPipeFinish := rSeenRdDataPipeFinish - 1
        when (
          !(rSeenRdDataPipeFinish - 1).msb
        ) {
          rRdLineBufAddrCnt.last := 0x0
        }
      }
      default {
      }
    }
    val rMyPopVecIdx = (
      Reg(UInt(
        cnt2dShift.x + log2Up(palIdxBusRatio) bits
      ))
      init(0x0)
    )

    switch (rState) {
      is (MyLineDuplState.FIRST_FETCH) {
        when (rSeenWrPulseFinish) {
          rState := MyLineDuplState.MAIN

          rSeenWrPulseFinish := False
          rWrLineBufAddrCnt := 0x0
          rMyFinishedFetchingLine := False
          rMyLineBufArrIdxVec.last := (
            rMyLineBufArrIdxVec.last + 1
          )
        }
      }
      is (MyLineDuplState.MAIN) {
        when (
          rSeenWrPulseFinish
          && rSeenRdAddrPipeFinish.msb
          && rSeenRdDataPipeFinish.msb
        ) {
          rSeenWrPulseFinish := False
          rSeenRdAddrPipeFinish := mySeenRdPipeFinishRstVal
          rSeenRdDataPipeFinish := mySeenRdPipeFinishRstVal
          rWrLineBufAddrCnt := 0x0
          rRdLineBufAddrCnt.foreach(item => {
            item := 0
          })
          rMyFinishedFetchingLine := False
          rMyLineBufArrIdxVec.foreach(item => {
            item := item + 1
          })
        } otherwise {
          if (cnt2dShift.x == 0) {
            require(
              false,
              "not yet implemented"
            )
          } else {
            myMaybeReptRdDataPipeStm.head <-/< myRdDataPipeStm.repeat(
              times=((1 << cnt2dShift.x) * palIdxBusRatio)
            )._1
            myMaybeReptRdDataPipeStm.last <-/< (
              myMaybeReptRdDataPipeStm.head
            )
            myMaybeReptRdDataPipeStm.last.translateInto(palIdxPop)(
              dataAssignment=(outp, inp) => {
                outp := inp(rMyPopVecIdx(
                  rMyPopVecIdx.high
                  downto rMyPopVecIdx.getWidth - log2Up(palIdxBusRatio)
                ))
              }
            )
            when (myMaybeReptRdDataPipeStm.last.fire) {
              rMyPopVecIdx := rMyPopVecIdx + 1
            }
          }
        }
      }
    }
  }
  //--------
  object CsrBusState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      DO_READ_CSR,
      DO_WRITE_CSR
      = newElement();
  }
  val myCsrArea = (
    io.csrLoBus != null
  ) generate new Area {
    val rCsrBusState = (
      Reg(CsrBusState())
      init(CsrBusState.IDLE)
    )
    val rSavedCsrBusH2dPayload = {
      val temp = Reg(cloneOf(io.csrLoBus.h2dBus.payload))
      temp.init(temp.getZero)
      temp
    }

    io.csrLoBus.h2dBus.ready := False
    io.csrLoBus.d2hBus.payload := io.csrLoBus.d2hBus.payload.getZero
    io.csrLoBus.d2hBus.valid := False

    io.csrLoBus.d2hBus.src.allowOverride
    io.csrLoBus.d2hBus.src := rSavedCsrBusH2dPayload.src

    io.csrLoBus.d2hBus.data.allowOverride
    io.csrLoBus.d2hBus.data := (
      rFbBusPageIdxVec.head.resize(io.csrLoBus.d2hBus.data.getWidth)
    )

    switch (rCsrBusState) {
      is (CsrBusState.IDLE) {
        rSavedCsrBusH2dPayload := io.csrLoBus.h2dBus.payload

        io.csrLoBus.h2dBus.ready := True

        switch (
          io.csrLoBus.h2dBus.valid
          ## io.csrLoBus.h2dBus.isWrite
        ) {
          is (M"10") {
            rCsrBusState := CsrBusState.DO_READ_CSR
          }
          is (M"11") {
            rCsrBusState := CsrBusState.DO_WRITE_CSR
            rFbBusPageIdxVec.last := rFbBusPageIdxVec.head
          }
          default {
          }
        }
      }
      is (CsrBusState.DO_READ_CSR) {
        io.csrLoBus.d2hBus.valid := True
        when (io.csrLoBus.d2hBus.ready) {
          rCsrBusState := CsrBusState.IDLE
        }
      }
      is (CsrBusState.DO_WRITE_CSR) {
        rFbBusPageIdxVec.head := (
          rSavedCsrBusH2dPayload.data.resize(rFbBusPageIdxVec.head.getWidth)
        )

        io.csrLoBus.d2hBus.valid := True
        when (io.csrLoBus.d2hBus.ready) {
          rCsrBusState := CsrBusState.IDLE
        }
      }
    }
  }
  //--------
}

private[libcheesevoyage] case class LcvBusFramebufferCtrlNonPal(
  cfg: LcvBusFramebufferConfig
) extends Component {
  def rgbCfg = cfg.rgbCfg
  def busCfg = cfg.busCfg
  def fbSize2d = cfg.fbSize2d
  def cnt2dShift = cfg.cnt2dShift
  def rgbUpWidth = 1 << log2Up(Rgb(c=rgbCfg).asBits.getWidth)
  def rgbBusRatio = (busCfg.dataWidth / rgbUpWidth).toInt
  def myBusBurstSizeMaxMult = cfg.myBusBurstSizeMax * rgbBusRatio

  val myVideoCfg = LcvVideoDblLineBufWithCalcPosConfig(
    rgbCfg=rgbCfg,
    someSize2d=ElabVec2[Int](
      x=fbSize2d.x,
      y=(
        //fbSize2d.y * (if (cfg.dblBuf) (2) else (1))
        //(fbSize2d.y * (if (
        //  cfg.dblBuf
        //) (2) else (1)))
        fbSize2d.y * cfg.myFbSize2dDblBufFactor
        //fbSize2d.y
      ),
    ),
    cnt2dShift=cfg.cnt2dShift,
  )

  require(
    busCfg.allowBurst
  )
  require(
    busCfg.dataWidth
    >= rgbUpWidth
    //== rgbUpWidth
  )
  require(
    (fbSize2d.x % myBusBurstSizeMaxMult) == 0,
    s"fbSize2d.x:${fbSize2d.x} must be an exact integer multiple "
    + s"of myBusBurstSizeMaxMult:${myBusBurstSizeMaxMult}"
  )

  val io = LcvBusFramebufferCtrlIo(cfg=cfg)

  val myH2dStm = Vec.fill(3)(
    cloneOf(io.hiBus.h2dBus)
  )

  myH2dStm.head.valid := True
  io.hiBus.h2dBus << myH2dStm.last
  val myH2dThrowCond = Bool()
  val myH2dMaybeThrownStm = (
    if (cnt2dShift.y == 0) (
      myH2dStm.head//.throwWhen(myH2dThrowCond)
    ) else (
      myH2dStm.head.throwWhen(myH2dThrowCond)
    )
  )
  val rMyFinishedFetchingLine = (
    cnt2dShift.y > 0
  ) generate (
    Reg(Bool(), init=False)
  )
  if (cnt2dShift.y > 0) {
    myH2dThrowCond := rMyFinishedFetchingLine
  }

  val rMyH2dAddrSliceVal = (
    cfg.dblBuf
    && (cfg.optDblBufAddrSliceVal != None)
  ) generate {
    val temp = Reg(Flow(UInt(cfg.fbMmapCfg.addrSliceWidth bits)))
    temp.valid.init(False)
    temp.payload.init(cfg.fbMmapCfg.addrSliceValUInt)
    temp
  }

  myH2dStm(1) <-/< myH2dMaybeThrownStm
  myH2dStm(1).translateInto(myH2dStm.last)(
    dataAssignment=(outp, inp) => {
      outp := inp
      outp.addr.allowOverride
      if (
        cfg.dblBuf
        && (cfg.optDblBufAddrSliceVal != None)
      ) {
        outp.addr(cfg.fbMmapCfg.addrSliceRange) := (
          //cfg.fbMmapCfg.addrSliceValUInt
          rMyH2dAddrSliceVal.payload
        )
      } else {
        outp.addr(cfg.fbMmapCfg.addrSliceRange) := (
          cfg.fbMmapCfg.addrSliceValUInt
        )
      }
    }
  )

  val rFbColCnt = (
    Reg(UInt(
      (log2Up((myVideoCfg.someSize2d.x) + 1) + 1) bits
    ))
    init(0x0)
  )
  val rFbAddrCnt = (
    Reg(UInt(
      log2Up(
        ((myVideoCfg.someSize2d.y * myVideoCfg.someSize2d.x) + 1) + 1
      ) bits
    ))
    init(0x0)
  )
  when (myH2dMaybeThrownStm.fire) {
    when (
      rFbColCnt < (
        myVideoCfg.someSize2d.x
        - myBusBurstSizeMaxMult
      )
    ) {
      rFbColCnt := (
        rFbColCnt + myBusBurstSizeMaxMult
      )
    } otherwise {
      rMyFinishedFetchingLine := True
      rFbColCnt := 0x0
    }
  }
  when (myH2dMaybeThrownStm.fire) {
    val tempCond = (
      rFbAddrCnt
      < (
        (
          (
            myVideoCfg.someSize2d.y * myVideoCfg.someSize2d.x
            / rgbBusRatio
          )
        )
        - cfg.myBusBurstSizeMax
      )
    )
    when (tempCond) {
      rFbAddrCnt := (
        rFbAddrCnt + cfg.myBusBurstSizeMax
      )
    } otherwise {
      if (
        cfg.dblBuf
        && (cfg.optDblBufAddrSliceVal != None)
      ) {
        rMyH2dAddrSliceVal.valid := !rMyH2dAddrSliceVal.fire
      }
      rFbAddrCnt := 0x0
    }
    if (
      cfg.dblBuf
      && (cfg.optDblBufAddrSliceVal != None)
    ) {
      switch (
        tempCond
        ## rMyH2dAddrSliceVal.fire
      ) {
        is (M"00") {
          rMyH2dAddrSliceVal.payload := (
            cfg.optDblBufAddrSliceVal.get
          )
        }
        is (M"01") {
          rMyH2dAddrSliceVal.payload := (
            cfg.fbMmapCfg.addrSliceValUInt
          )
        }
        default {
        }
      }
    }
  }
  def myDataAssignmentH2d(
    outp: LcvBusH2dPayload,
    inp: Data,
  ): Unit = {
    outp.addr := (
      Cat(
        rFbAddrCnt(rFbAddrCnt.high downto 0),
        U(s"${log2Up(busCfg.dataWidth / 8)}'d0")
      ).asUInt.resize(outp.addr.getWidth)
    )
    outp.src := 0x0
    outp.data := 0x0
    outp.byteSize := log2Up(busCfg.dataWidth / 8)
    outp.isWrite := False

    if (busCfg.allowBurst) {
      outp.burstFirst := True
      outp.burstCnt := busCfg.maxBurstSizeMinus1
      outp.burstLast := True//False
    }
  }
  myDataAssignmentH2d(
    outp=myH2dStm.head.payload,
    inp=null,
  )

  //--------
  val myPushStm = (
    Stream(UInt(busCfg.dataWidth bits))
  )
  val myD2hStm = Vec.fill(2)(
    cloneOf(io.hiBus.d2hBus)
  )
  myD2hStm.head <-/< io.hiBus.d2hBus
    myD2hStm.last <-/< myD2hStm.head

  myD2hStm.last.translateInto(myPushStm)(
    dataAssignment=(outp, inp) => {
      outp.assignFromBits(inp.data.asBits.resize(outp.asBits.getWidth))
    }
  )
  //--------
  object MyLineDuplState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      FIRST_FETCH,
      MAIN
      = newElement();
  }
  val myLineDuplArea = (
    cnt2dShift.y > 0
  ) generate new Area {
    val rState = (
      Reg(MyLineDuplState())
      init(MyLineDuplState.FIRST_FETCH)
    )
    val myLineBufMemWordCnt = (
      ////someSize2d.x //* (1 << cnt2dShift.x)
      ////* 2

      //// This *may* waste space but maybe not? It does round up to the
      //// nearest power of two, but I have a few comments about that:
      //// (1) It allows us to avoid using a multiplier for the address
      ////    calculation
      //// (2) FPGA Block RAM primitives are large enough
      ////    that maybe it's not a problem anyway?
      //// (3) I did some math, and even with a 1920x1080 resolution
      ////    (i.e. 1080p widescreen),
      ////    the calculation for a double-buffered line buffer only uses
      ////    4096 addresses. This becomes 16 kiB with 32 bpp colors
      ////    though. That's a big chunk of block RAM I guess? On the other
      ////    hand, you probably only need one of these double-buffered
      ////    line buffers (i.e. two line buffers).
      //(1 << log2Up(someSize2d.x))
      //* 2

      (1 << log2Up(myVideoCfg.someSize2d.x / rgbBusRatio))
      * 2
    )
    val myLineBufMemCfg = WrPulseRdPipeRamConfig(
      modType=(
        Vec.fill(rgbBusRatio)(Rgb(rgbCfg))
      ),
      wordType=(
        Vec.fill(rgbBusRatio)(Rgb(rgbCfg))
      ),
      wordCount=myLineBufMemWordCnt,
      //pipeName="LcvVideoDblLineBufWithCalcPos",
      initBigInt={
        val tempArr = new ArrayBuffer[BigInt]()
        for (idx <- 0 until myLineBufMemWordCnt) {
          tempArr += BigInt(0)
        }
        Some(Array.fill(1)(tempArr))
      },
      setWordFunc=(
        outp: Vec[Rgb],
        inp: Vec[Rgb],
        rdMemWord: Vec[Rgb],
        upIsFiring: Bool,
        myExternalInpCond: Bool,
        wrPulseVec: Vec[Flow[
          PipeSimpleDualPortMemDrivePayload[
            Vec[Rgb]
          ]
        ]],
      ) => {
        outp := rdMemWord
      }
    )
    val myLineBufArrSize = 2
    val rMyLineBufArrIdxVec = {
      val temp = Vec.fill(myLineBufArrSize)(
        Reg(UInt(log2Up(myLineBufArrSize) bits))
      )
      for (idx <- 0 until temp.size) {
        temp(idx).init(temp(idx).getZero)
      }
      temp
    }

    val myDblLineBuf = (
      WrPulseRdPipeRamSdpPipe(cfg=myLineBufMemCfg)
    )

    val myWrPulse = (
      cloneOf(myDblLineBuf.io.wrPulse)
    )

    myDblLineBuf.io.wrPulse <-< myWrPulse

    val rWrLineBufAddrCnt = (
      Reg(UInt(
        myWrPulse.addr.getWidth - 1 
        + cnt2dShift.x
        bits
      ))
      init(0x0)
    )
    val rSeenWrPulseFinish = Reg(Bool(), init=False)
    val myMaybeReptPushStm = cloneOf(myPushStm)
    myMaybeReptPushStm <-/< myPushStm
    myMaybeReptPushStm.ready := True

    myWrPulse.valid := (
      if (cnt2dShift.x == 0) (
        myMaybeReptPushStm.fire
      ) else (
        myMaybeReptPushStm.fire
      )
    )
    for (idx <- 0 until myWrPulse.data.size) {
      myWrPulse.data(idx).assignFromBits(
        myMaybeReptPushStm.payload(
          (idx + 1) * rgbUpWidth - 1
          downto idx * rgbUpWidth
        ).resize(myWrPulse.data(idx).asBits.getWidth).asBits
      )
    }
    val mySeenRdPipeFinishRstVal = (
      (1 << cnt2dShift.y) - 1//2//1//2
    )
    val rSeenRdAddrPipeFinish = (
      Reg(UInt((cnt2dShift.y + 2) bits))
      init(mySeenRdPipeFinishRstVal)
    )
    val rSeenRdDataPipeFinish = (
      Reg(UInt((cnt2dShift.y + 2) bits))
      init(
        mySeenRdPipeFinishRstVal
      )
    )

    val rRdLineBufAddrCnt = (
      Vec[UInt](
        List[UInt](
          (
            Reg(UInt(
              myDblLineBuf.io.rdAddrPipe.addr.getWidth - 1
              bits
            ))
            init(0x0)
          ),
          (
            Reg(UInt(
              myDblLineBuf.io.rdAddrPipe.addr.getWidth - 1
              bits
            ))
            init(0x0)
          )
        )
      )
    )
    val myRdAddrPipeStm = (
      cloneOf(myDblLineBuf.io.rdAddrPipe)
    )
    val myRdDataPipeStm = (
      cloneOf(myDblLineBuf.io.rdDataPipe)
    )

    val myMaybeReptRdDataPipeStm = Vec.fill(2)(
      cloneOf(myRdDataPipeStm)
    )
    myMaybeReptRdDataPipeStm := myMaybeReptRdDataPipeStm.getZero

    myRdAddrPipeStm.valid := (
      !rSeenRdAddrPipeFinish.msb
      && (
        rState === MyLineDuplState.MAIN
      )
    )
    myRdAddrPipeStm.payload := myRdAddrPipeStm.payload.getZero
    myRdAddrPipeStm.addr.allowOverride
    myRdAddrPipeStm.addr := (
      Cat(
        rMyLineBufArrIdxVec.head,
        rRdLineBufAddrCnt.head(
          rRdLineBufAddrCnt.head.high
          downto 0
        )
      ).asUInt
    )
    myDblLineBuf.io.rdAddrPipe <-/< myRdAddrPipeStm
    myRdDataPipeStm <-/< myDblLineBuf.io.rdDataPipe
    myRdDataPipeStm.ready := False

    io.pop.valid := False
    io.pop.payload := RegNext(io.pop.payload, init=io.pop.payload.getZero)

    myWrPulse.addr := (
      Cat(
        rMyLineBufArrIdxVec.last,
        rWrLineBufAddrCnt(
          rWrLineBufAddrCnt.high - 1
          downto 0
        ),
      ).asUInt
    )
    switch (
      rSeenWrPulseFinish
      ## myMaybeReptPushStm.fire
      ## (
        rWrLineBufAddrCnt
        < (myVideoCfg.someSize2d.x / rgbBusRatio) - 1 
      )
    ) {
      is (M"011") {
        // fire, rWrLineBufAddrCnt < width
        rWrLineBufAddrCnt := rWrLineBufAddrCnt + 1
      }
      is (M"010") {
        // fire, !(rWrLineBufAddrCnt < width)
        rSeenWrPulseFinish := True
      }
      default {
      }
    }

    switch (
      rSeenRdAddrPipeFinish.msb
      ## myRdAddrPipeStm.fire
      ## (
        rRdLineBufAddrCnt.head
        < (myVideoCfg.someSize2d.x / rgbBusRatio) - 1
      )
    ) {
      is (M"011") {
        // fire, rRdLineBufAddrCnt.head < width
        rRdLineBufAddrCnt.head := rRdLineBufAddrCnt.head + 1
      }
      is (M"010") {
        // fire, !(rRdLineBufAddrCnt < width)
        rSeenRdAddrPipeFinish := rSeenRdAddrPipeFinish - 1
        when (!(rSeenRdAddrPipeFinish - 1).msb) {
          rRdLineBufAddrCnt.head := 0x0
        }
      }
      default {
      }
    }

    switch (
      rSeenRdDataPipeFinish.msb
      ## myMaybeReptRdDataPipeStm.head.fire
      ## (
        rRdLineBufAddrCnt.last
        < (myVideoCfg.someSize2d.x / rgbBusRatio) - 1
      )
    ) {
      is (M"011") {
        // fire, rRdLineBufAddrCnt.last < width
        rRdLineBufAddrCnt.last := rRdLineBufAddrCnt.last + 1
      }
      is (M"010") {
        // fire, !(rRdLineBufAddrCnt < width)
        rSeenRdDataPipeFinish := rSeenRdDataPipeFinish - 1
        when (
          !(rSeenRdDataPipeFinish - 1).msb
        ) {
          rRdLineBufAddrCnt.last := 0x0
        }
      }
      default {
      }
    }
    val rMyPopVecIdx = (
      Reg(UInt(
        cnt2dShift.x + log2Up(rgbBusRatio) bits
      ))
      init(0x0)
    )

    switch (rState) {
      is (MyLineDuplState.FIRST_FETCH) {
        when (rSeenWrPulseFinish) {
          rState := MyLineDuplState.MAIN

          rSeenWrPulseFinish := False
          rWrLineBufAddrCnt := 0x0
          rMyFinishedFetchingLine := False
          rMyLineBufArrIdxVec.last := (
            rMyLineBufArrIdxVec.last + 1
          )
        }
      }
      is (MyLineDuplState.MAIN) {
        when (
          rSeenWrPulseFinish
          && rSeenRdAddrPipeFinish.msb
          && rSeenRdDataPipeFinish.msb
        ) {
          rSeenWrPulseFinish := False
          rSeenRdAddrPipeFinish := mySeenRdPipeFinishRstVal
          rSeenRdDataPipeFinish := mySeenRdPipeFinishRstVal
          rWrLineBufAddrCnt := 0x0
          rRdLineBufAddrCnt.foreach(item => {
            item := 0
          })
          rMyFinishedFetchingLine := False
          rMyLineBufArrIdxVec.foreach(item => {
            item := item + 1
          })
        } otherwise {
          if (cnt2dShift.x == 0) {
            require(
              false,
              "not yet implemented"
            )
          } else {
            myMaybeReptRdDataPipeStm.head <-/< myRdDataPipeStm.repeat(
              times=((1 << cnt2dShift.x) * rgbBusRatio)
            )._1
            myMaybeReptRdDataPipeStm.last <-/< (
              myMaybeReptRdDataPipeStm.head
            )
            myMaybeReptRdDataPipeStm.last.translateInto(io.pop)(
              dataAssignment=(outp, inp) => {
                outp := inp(rMyPopVecIdx(
                  rMyPopVecIdx.high
                  downto rMyPopVecIdx.getWidth - log2Up(rgbBusRatio)
                ))
              }
            )
            when (myMaybeReptRdDataPipeStm.last.fire) {
              rMyPopVecIdx := rMyPopVecIdx + 1
            }
          }
        }
      }
    }
  }
  //--------
}
