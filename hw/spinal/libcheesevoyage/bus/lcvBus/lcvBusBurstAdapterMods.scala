//package libcheesevoyage.bus.lcvBus
//
//import scala.collection.immutable
//import scala.collection.mutable._
//import spinal.core._
//import spinal.core.formal._
//import spinal.core.sim._
//import spinal.lib._
//import spinal.lib.misc.pipeline._
//
//case class LcvBusBurstAdapterConfig(
//  hostBusCfg: LcvBusConfig,
//  devBusCfg: LcvBusConfig,
//) {
//  hostBusCfg.needSameDataWidth(that=devBusCfg)
//  hostBusCfg.needSameAddrWidth(that=devBusCfg)
//  hostBusCfg.needSameSrcWidth(that=devBusCfg)
//  require(
//    hostBusCfg.burstCntWidth >= devBusCfg.burstCntWidth,
//    s"Required: hostBusCfg.burstCntWidth (${hostBusCfg.burstCntWidth}) "
//    + s">= devBusCfg.burstCntWidth (${devBusCfg.burstCntWidth})"
//  )
//  require(
//    hostBusCfg.burstCntWidth > 0,
//    s"Required: hostBusCfg.burstCntWidth (${hostBusCfg.burstCntWidth}) > 0"
//  )
//  //require(
//  //  devBusCfg.burstCntWidth == 0
//  //)
//}
//
//case class LcvBusBurstAdapterIo(
//  cfg: LcvBusBurstAdapterConfig
//) extends Bundle {
//  //--------
//  val hostBus = slave(LcvBusIo(cfg=cfg.hostBusCfg))
//  val devBus = master(LcvBusIo(cfg=cfg.devBusCfg))
//  //--------
//  //val hostH2dBus = (
//  //  slave(new Stream(
//  //    sendPayloadType=Some(LcvBusH2dPayload(cfg=cfg.hostBusCfg)),
//  //    recvPayloadType=None,
//  //  ))
//  //)
//  //val hostD2hBus = (
//  //  master(new Stream(
//  //    sendPayloadType=Some(LcvBusD2hPayload(cfg=cfg.hostBusCfg)),
//  //    recvPayloadType=None,
//  //  ))
//  //)
//
//  //val devH2dBus = (
//  //  master(new Stream(
//  //    sendPayloadType=Some(LcvBusH2dPayload(cfg=cfg.devBusCfg)),
//  //    recvPayloadType=None,
//  //  ))
//  //)
//  //val devD2hBus = (
//  //  slave(new Stream(
//  //    sendPayloadType=Some(LcvBusD2hPayload(cfg=cfg.devBusCfg)),
//  //    recvPayloadType=None,
//  //  ))
//  //)
//}
//
//case class LcvBusBurstAdapter(
//  cfg: LcvBusBurstAdapterConfig
//) extends Component {
//  //--------
//  val io = LcvBusBurstAdapterIo(cfg=cfg)
//  //--------
//  //io.hostBus.h2dBus
//  //--------
//
//  io.devBus.h2dBus.valid := (
//    //io.hostBus.h2dBus.valid
//    RegNext(
//      next=io.devBus.h2dBus.valid,
//      init=io.devBus.h2dBus.valid.getZero,
//    )
//  )
//  io.hostBus.h2dBus.ready := (
//    //io.devBus.h2dBus.ready
//    RegNext(
//      next=io.hostBus.h2dBus.ready,
//      init=io.hostBus.h2dBus.ready.getZero,
//    )
//  )
//  io.devBus.h2dBus.payload := (
//    //io.hostBus.h2dBus.payload.mainNonBurstInfo
//    RegNext(
//      next=io.devBus.h2dBus.payload,
//      init=io.devBus.h2dBus.payload.getZero,
//    )
//  )
//  //--------
//  io.hostBus.d2hBus.valid := (
//    //io.devBus.d2hBus.valid
//    RegNext(
//      next=io.hostBus.d2hBus.valid,
//      init=io.hostBus.d2hBus.valid.getZero,
//    )
//  )
//  io.devBus.d2hBus.ready := (
//    //io.hostBus.d2hBus.ready
//    RegNext(
//      next=io.devBus.d2hBus.ready,
//      init=io.devBus.d2hBus.ready.getZero,
//    )
//  )
//  io.hostBus.d2hBus.payload := (
//    //io.devBus.d2hBus.payload.mainNonBurstInfo
//    RegNext(
//      next=io.hostBus.d2hBus.payload,
//      init=io.hostBus.d2hBus.payload.getZero,
//    )
//  )
//  //--------
//  //def myHostH2dNextValid = io.hostBus.h2dBus.valid
//  //def myHostH2dReady = io.hostBus.h2dBus.ready
//  def myHostH2dNonBurstInfo = io.hostBus.h2dBus.payload.mainNonBurstInfo
//  def myHostH2dBurstInfo = io.hostBus.h2dBus.payload.mainBurstInfo
//
//  //def myHostD2hNextValid = io.hostBus.d2hBus.valid
//  //def myHostD2hReady = io.hostBus.d2hBus.ready
//  def myHostD2hNonBurstInfo = io.hostBus.d2hBus.payload.mainNonBurstInfo
//  def myHostD2hBurstInfo = io.hostBus.d2hBus.payload.mainBurstInfo
//  //--------
//  //def myDevH2dNextValid = io.devBus.h2dBus.valid
//  //def myDevH2dReady = io.devBus.h2dBus.ready
//  def myDevH2dNonBurstInfo = io.devBus.h2dBus.payload.mainNonBurstInfo
//  def myDevH2dBurstInfo = io.devBus.h2dBus.payload.mainBurstInfo
//
//  //def myDevD2hNextValid = io.devBus.d2hBus.valid
//  //def myDevD2hReady = io.devBus.d2hBus.ready
//  def myDevD2hNonBurstInfo = io.devBus.d2hBus.payload.mainNonBurstInfo
//  def myDevD2hBurstInfo = io.devBus.d2hBus.payload.mainBurstInfo
//  //--------
//  object DevBurstlessState
//  extends SpinalEnum(defaultEncoding=binarySequential) {
//    val
//      IDLE,
//      NON_BURST,
//      READ_BURST,
//      WRITE_BURST
//      = newElement();
//  }
//  val myDevBurstlessArea = (
//    cfg.devBusCfg.burstCntWidth == 0
//  ) generate (
//  new Area {
//    io.devBus.h2dBus.payload.mainNonBurstInfo.addr.allowOverride
//    val rSavedH2dSendData = {
//      val temp = (
//        //Reg(Flow(LcvBusH2dPayloadMainBurstInfo(cfg=cfg.hostBusCfg)))
//        Reg(LcvBusH2dPayload(cfg=cfg.hostBusCfg))
//      )
//      temp.init(temp.getZero)
//      temp
//    }
//    //val rDevBurstInfo = {
//    //  val temp = (
//    //    //Reg(Flow(LcvBusD2hPayloadMainBurstInfo(cfg=cfg.hostBusCfg)))
//    //    Reg(LcvBusD2hPayload(cfg=cfg.hostBusCfg))
//    //  )
//    //  temp.init(temp.getZero)
//    //  temp
//    //}
//    //--------
//    val rPrevHostD2hBurstInfo = (
//      RegNext(
//        next=myHostD2hBurstInfo,
//        init=myHostD2hBurstInfo.getZero,
//      )
//    )
//    val rSavedBurstCntForH2d = (
//      Reg(
//        //cloneOf(myHostD2hBurstInfo.burstCnt)
//        UInt((myHostD2hBurstInfo.burstCnt.getWidth + 1) bits)
//      )
//      init(0x0)
//    )
//    val rSavedBurstCntForD2h = (
//      Reg(
//        //cloneOf(myHostD2hBurstInfo.burstCnt)
//        UInt((myHostD2hBurstInfo.burstCnt.getWidth + 1) bits)
//      )
//      init(0x0)
//    )
//    val rMyH2dCnt = (
//      Reg(
//        //cloneOf(myHostD2hBurstInfo.burstCnt)
//        UInt((myHostD2hBurstInfo.burstCnt.getWidth + 1) bits)
//      )
//      init(0x0)
//    )
//    val rMyD2hCnt = (
//      Reg(
//        //cloneOf(myHostD2hBurstInfo.burstCnt)
//        UInt((myHostD2hBurstInfo.burstCnt.getWidth + 1) bits)
//      )
//      init(0x0)
//    )
//
//    //myHostD2hBurstInfo := rPrevHostD2hBurstInfo
//    //--------
//    val myHadH2dFire = (
//      Bool()
//    )
//    myHadH2dFire := False
//
//    val rSavedHadH2dFire = (
//      Reg(Bool(), init=False)
//    )
//    when (myHadH2dFire) {
//      rSavedHadH2dFire := True
//    }
//
//    val stickyHadH2dFire = (
//      myHadH2dFire
//      || rSavedHadH2dFire
//    )
//    //--------
//    val myHadD2hFire = (
//      Bool()
//    )
//    myHadD2hFire := False
//
//    val rSavedHadD2hFire = (
//      Reg(Bool(), init=False)
//    )
//    when (myHadD2hFire) {
//      rSavedHadD2hFire := True
//    }
//
//    val stickyHadD2hFire = (
//      myHadD2hFire
//      || rSavedHadD2hFire
//    )
//    //--------
//    val rState = (
//      Reg(DevBurstlessState())
//      init(DevBurstlessState.IDLE)
//    )
//    switch (rState) {
//      is (DevBurstlessState.IDLE) {
//        myHostD2hBurstInfo := (
//          myHostD2hBurstInfo.getZero
//        )
//
//        io.devBus.h2dBus.valid := io.hostBus.h2dBus.valid
//        io.hostBus.h2dBus.ready := False
//
//        io.hostBus.d2hBus.valid := False
//        io.devBus.d2hBus.ready := False
//
//        //io.hostBus.d2hBus.payload.mainBurstInfo := (
//        //  io.hostBus.d2hBus.payload.mainBurstInfo.getZero
//        //)
//
//        rSavedHadH2dFire := False
//        rSavedHadD2hFire := False
//        rSavedBurstCntForH2d := (
//          myHostH2dBurstInfo.burstCnt.resize(rSavedBurstCntForH2d.getWidth)
//        )
//        rSavedBurstCntForD2h := (
//          myHostH2dBurstInfo.burstCnt.resize(rSavedBurstCntForD2h.getWidth)
//        )
//        rMyH2dCnt := (
//          //myHostH2dBurstInfo.burstCnt
//          0x0
//        )
//        rMyD2hCnt := (
//          //myHostH2dBurstInfo.burstCnt
//          0x0
//        )
//        rSavedH2dSendData := io.hostBus.h2dBus.payload
//
//        io.devBus.h2dBus.payload.addr := (
//          //io.devBus.h2dBus.payload.burstAddr(someBurstCnt=rMyH2dCnt)
//          rSavedH2dSendData.burstAddr(someBurstCnt=rMyH2dCnt.getZero)
//        )
//
//        switch (
//          io.hostBus.h2dBus.valid
//          ## myHostH2dNonBurstInfo.isWrite
//          ## myHostH2dBurstInfo.burstFirst
//        ) {
//          is (M"1-0") {
//            // !burstFirst
//            rState := DevBurstlessState.NON_BURST
//
//            //io.devBus.h2dBus.valid := io.hostBus.h2dBus.valid
//            //io.hostBus.h2dBus.ready := io.devBus.h2dBus.ready
//
//            //io.hostBus.d2hBus.valid := io.devBus.d2hBus.valid
//            //io.devBus.d2hBus.ready := io.hostBus.d2hBus.ready
//
//            //rMyCnt := 1
//            rSavedHadH2dFire := False
//            rSavedHadD2hFire := False
//          }
//          is (B"101") {
//            // !isWrite, burstFirst
//            rState := DevBurstlessState.READ_BURST
//
//            myHostD2hBurstInfo.burstCnt := (
//              myHostH2dBurstInfo.burstCnt
//            )
//          }
//          is (B"111") {
//            // isWrite, burstFirst
//            rState := DevBurstlessState.WRITE_BURST
//
//            myHostD2hBurstInfo.burstCnt := (
//              myHostH2dBurstInfo.burstCnt
//            )
//          }
//          default {
//            // anything else
//          }
//        }
//      }
//      is (DevBurstlessState.NON_BURST) {
//        io.devBus.h2dBus.valid := io.hostBus.h2dBus.valid
//        io.devBus.h2dBus.payload := io.hostBus.h2dBus.payload
//        io.hostBus.h2dBus.ready := io.devBus.h2dBus.ready
//
//        io.hostBus.d2hBus.valid := io.devBus.d2hBus.valid
//        io.hostBus.d2hBus.payload.mainNonBurstInfo := (
//          io.devBus.d2hBus.payload.mainNonBurstInfo
//        )
//        io.devBus.d2hBus.ready := io.hostBus.d2hBus.ready
//
//        when (
//          RegNext(
//            next=io.hostBus.h2dBus.valid,
//            init=False,
//          )
//          && io.hostBus.h2dBus.ready
//        ) {
//          myHadH2dFire := True
//        }
//
//        when (
//          RegNext(
//            next=io.devBus.d2hBus.valid,
//            init=False,
//          )
//          && io.devBus.d2hBus.ready
//        ) {
//          myHadD2hFire := True
//        }
//
//        when (stickyHadH2dFire && stickyHadD2hFire) {
//          rState := DevBurstlessState.IDLE
//        }
//      }
//      is (DevBurstlessState.READ_BURST) {
//        io.hostBus.h2dBus.ready := False
//
//        io.devBus.h2dBus.payload := io.hostBus.h2dBus.payload
//
//        io.hostBus.d2hBus.valid := io.devBus.d2hBus.valid
//        io.hostBus.d2hBus.payload.mainNonBurstInfo := (
//          io.devBus.d2hBus.payload.mainNonBurstInfo
//        )
//        io.devBus.d2hBus.ready := io.hostBus.d2hBus.ready
//
//        io.devBus.h2dBus.valid := True
//        io.devBus.h2dBus.payload.addr := (
//          //io.devBus.h2dBus.payload.burstAddr(someBurstCnt=rMyH2dCnt)
//          rSavedH2dSendData.burstAddr(someBurstCnt=rMyH2dCnt)
//        )
//        when (
//          RegNext(
//            next=io.devBus.h2dBus.valid,
//            init=False,
//          )
//          && io.devBus.h2dBus.ready
//        ) {
//          when (rMyH2dCnt < rSavedBurstCntForH2d) {
//            rMyH2dCnt := rMyH2dCnt + 1
//          } otherwise {
//            myHadH2dFire := True
//          }
//        }
//
//        when (rose(
//          RegNext(
//            next=stickyHadH2dFire,
//            init=False,
//          )
//        )) {
//          io.hostBus.h2dBus.ready := True
//        }
//
//        when (stickyHadH2dFire) {
//          io.devBus.h2dBus.valid := False
//        }
//
//        when (
//          RegNext(
//            next=io.devBus.d2hBus.valid,
//            init=False,
//          )
//          && io.devBus.d2hBus.ready
//        ) {
//          when (rMyD2hCnt < rSavedBurstCntForD2h) {
//            rMyD2hCnt := rMyD2hCnt + 1
//          } otherwise {
//            myHadD2hFire := True
//          }
//        }
//        when (
//          RegNext(
//            next=io.hostBus.d2hBus.payload.mainBurstInfo.burstFirst,
//            init=False
//          )
//        ) {
//          io.hostBus.d2hBus.payload.mainBurstInfo.burstFirst := False
//        }
//        when (rMyD2hCnt >= rSavedBurstCntForD2h) {
//          io.hostBus.d2hBus.payload.mainBurstInfo.burstLast := True
//        }
//
//        when (stickyHadH2dFire && stickyHadD2hFire) {
//          rState := DevBurstlessState.IDLE
//        }
//
//        //when (
//        //  RegNext(
//        //    next=io.devBus.d2hBus.valid,
//        //    init=False,
//        //  )
//        //) {
//        //  myHostD2hBurstInfo.burstFirst := (
//        //    myHostD2hBurstInfo.burstCnt === rSavedBurstCnt
//        //  )
//        //  myHostD2hBurstInfo.burstLast := (
//        //    myHostD2hBurstInfo.burstCnt === 0x0
//        //  )
//        //  when (io.devBus.d2hBus.ready) {
//        //    when (myHostD2hBurstInfo.burstLast) {
//        //      rState := DevBurstlessState.IDLE
//        //    } otherwise {
//        //      myHostD2hBurstInfo.burstCnt := (
//        //        rPrevHostD2hBurstInfo.burstCnt - 1
//        //      )
//        //    }
//        //  }
//        //}
//      }
//      is (DevBurstlessState.WRITE_BURST) {
//      }
//    }
//  })
//}
