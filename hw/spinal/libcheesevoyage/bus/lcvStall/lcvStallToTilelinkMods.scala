package libcheesevoyage.bus.lcvStall

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._
import spinal.lib.bus.tilelink
import libcheesevoyage.bus.tilelink.TlOpcode

case class LcvStallToTilelinkConfig(
  addrWidth: Int,
  dataWidth: Int,
  sizeBytes: Int,
  srcWidth: Int,
  isDual: Boolean,
  //sinkWidth: Int,
  //optMemCoherency: Boolean=false,
  //withBCE: Boolean=false,
  //optAtomic: Boolean=false,
) {
  // TODO: support full TL-C
  val tlCfg = tilelink.BusParameter.simple(
    addressWidth=addrWidth,
    dataWidth=dataWidth,
    // NOTE: `sizeBytes` is the maximal number of bytes in a burst
    sizeBytes=sizeBytes,
    sourceWidth=srcWidth,
  )
  val dualFifoDepth = (
    tlCfg.beatMax + 2
  )
  def doNeedIsDualEqTrue(): Unit = {
    assert(
      isDual,
      s"need `isDual == true`, but have `isDual == false`"
    )
  }
  def doNeedIsDualEqFalse(): Unit = {
    assert(
      !isDual,
      s"need `isDual == false`, but have `isDual == true`"
    )
  }
  //val tlCfg = tilelink.BusParameter(
  //  addressWidth=addrWidth,
  //  dataWidth=dataWidth,
  //  sourceWidth=1,
  //  sinkWidth=1,
  //  withBCE=false,
  //)
  //val tlM2sCfg = tilelink.M2sParameters(
  //)
}
case class LcvStallToTilelinkH2dSendPayload(
  cfg: LcvStallToTilelinkConfig,
) extends Bundle {
  //cfg.doNeedIsDualEqFalse()
  val isWrite = Bool()
  val addr = UInt(cfg.addrWidth bits)
  val data = UInt(cfg.dataWidth bits)
  val src = UInt(cfg.srcWidth bits)
  val size = (
    cfg.isDual
  ) generate (
    UInt(cfg.tlCfg.sizeWidth bits)
  )
  val mask = (
    cfg.isDual
  ) generate (
    UInt(cfg.tlCfg.dataBytes bits)
  )
  //--------
  //val lock = (
  //  cfg.optAtomic
  //) generate (
  //  Bool()
  //)
}
case class LcvStallToTilelinkD2hSendPayload(
  cfg: LcvStallToTilelinkConfig,
) extends Bundle {
  //cfg.doNeedIsDualEqFalse()
  val data = UInt(cfg.dataWidth bits)
  val src = (
    UInt(cfg.srcWidth bits)
  )
  val size = (
    cfg.isDual
  ) generate (
    UInt(cfg.tlCfg.sizeWidth bits)
  )
  //val burstCnt = (
  //  cfg.isDual
  //) generate (
  //  UInt(cfg.tlCfg.beatWidth bits)
  //)
}
case class LcvStallToTilelinkIo(
  cfg: LcvStallToTilelinkConfig
) extends Bundle {
  //cfg.doNeedIsDualEqFalse()
  val lcvStall = (
    !cfg.isDual
  ) generate (
    slave(new LcvStallIo[
      LcvStallToTilelinkH2dSendPayload, LcvStallToTilelinkD2hSendPayload,
    ](
      sendPayloadType=Some(LcvStallToTilelinkH2dSendPayload(cfg=cfg)),
      recvPayloadType=Some(LcvStallToTilelinkD2hSendPayload(cfg=cfg)),
    ))
  )
  val h2dBus = (
    cfg.isDual
  ) generate (
    slave(new LcvStallIo[LcvStallToTilelinkH2dSendPayload, Bool](
      sendPayloadType=Some(LcvStallToTilelinkH2dSendPayload(cfg=cfg)),
      recvPayloadType=None,
    ))
  )
  val d2hBus = (
    cfg.isDual
  ) generate (
    master(new LcvStallIo[LcvStallToTilelinkD2hSendPayload, Bool](
      sendPayloadType=Some(LcvStallToTilelinkD2hSendPayload(cfg=cfg)),
      recvPayloadType=None,
    ))
  )
  val tlBus = master(tilelink.Bus(
    p=cfg.tlCfg
  ))
}
case class LcvStallToTilelink(
  cfg: LcvStallToTilelinkConfig
) extends Component {
  //--------
  cfg.doNeedIsDualEqFalse()
  val io = LcvStallToTilelinkIo(cfg=cfg)
  //--------
  object State
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      //SEND_TL_CHAN_A_VALID,
      HANDLE_TL,
      //TL_CHAN_D,
      //SEND_TL_CHAN_D_READY,
      YIELD_RESULT
      = newElement()
  }
  val nextState = State()
  val rState = (
    RegNext(nextState)
    init(State.IDLE)
  )
  nextState := rState
  io.lcvStall.ready := False
  //val rSavedHostData = {
  //  val temp = Reg(LcvStallToTilelinkHostPayload(cfg=cfg))
  //  temp.init(temp.getZero)
  //  temp
  //}
  val rDevData = {
    val temp = Reg(LcvStallToTilelinkD2hSendPayload(cfg=cfg))
    temp.init(temp.getZero)
    temp
  }
  io.lcvStall.recvData := rDevData
  io.tlBus.a.valid.setAsReg
  io.tlBus.a.valid.init(io.tlBus.a.valid.getZero)
  io.tlBus.a.payload.setAsReg
  io.tlBus.a.payload.init(io.tlBus.a.payload.getZero)
  io.tlBus.d.ready := False
  //val rSavedTlChanD = Reg(cloneOf(io.tlBus.d.payload))
  //--------
  switch (rState) {
    is (State.IDLE) {
      when (io.lcvStall.rValid) {
        //rSavedHostData := io.lcvStall.hostData
        nextState := (
          //State.SEND_TL_CHAN_A_VALID
          State.HANDLE_TL
        )
        io.tlBus.a.valid := True
        when (!io.lcvStall.sendData.isWrite) {
          io.tlBus.a.opcode := tilelink.Opcode.A.GET
        } otherwise {
          io.tlBus.a.opcode := tilelink.Opcode.A.PUT_FULL_DATA
        }
        io.tlBus.a.param := 0x0
        io.tlBus.a.size := 1//(cfg.dataWidth / 8)
        io.tlBus.a.source := io.lcvStall.sendData.src
        io.tlBus.a.address := io.lcvStall.sendData.addr
        io.tlBus.a.mask := io.lcvStall.sendData.mask.asBits //B(io.tlBus.a.mask.getWidth bits, default -> True)
        io.tlBus.a.corrupt := False
        io.tlBus.a.data := io.lcvStall.sendData.data.asBits
      }
    }
    //is (State.SEND_TL_CHAN_A_VALID) {
    //  io.tlBus.a.valid := True
    //}
    is (State.HANDLE_TL) {
      when (io.tlBus.a.ready) {
        io.tlBus.a.valid := False
        //nextState := State.TL_CHAN_D
      }
      when (io.tlBus.d.valid) {
        io.tlBus.d.ready := True
        //rSavedTlChanD := io.tlBus.d
        rDevData.data := io.tlBus.d.data.asUInt
        rDevData.src := io.tlBus.d.source
        nextState := State.YIELD_RESULT
      }
    }
    //is (State.SEND_TL_CHAN_D_READY) {
    //}
    is (State.YIELD_RESULT) {
      when (io.lcvStall.rValid) {
        io.lcvStall.ready := True
        nextState := State.IDLE
      }
    }
  }
  //--------
}

case class LcvStallDualToTilelink(
  cfg: LcvStallToTilelinkConfig
) extends Component {
  //--------
  cfg.doNeedIsDualEqTrue()
  val io = LcvStallToTilelinkIo(cfg=cfg)
  //--------
  val h2dFifo = StreamFifo(
    dataType=LcvStallToTilelinkH2dSendPayload(cfg=cfg),
    depth=cfg.dualFifoDepth,
    forFMax=true,
  )
  val d2hFifo = StreamFifo(
    dataType=LcvStallToTilelinkD2hSendPayload(cfg=cfg),
    depth=cfg.dualFifoDepth,
    forFMax=true,
  )
  //--------
  val myH2dPushStm = Stream(h2dFifo.dataType())
  h2dFifo.io.push <-/< myH2dPushStm
  myH2dPushStm.valid := io.h2dBus.nextValid
  myH2dPushStm.payload := io.h2dBus.sendData
  io.h2dBus.ready := myH2dPushStm.ready
  //--------
  val myH2dPopStm = Stream(h2dFifo.dataType())
  myH2dPopStm <-/< h2dFifo.io.pop
  myH2dPopStm.translateInto(
    io.tlBus.a
  )(
    dataAssignment=(
      outp, inp
    ) => {
      when (!inp.isWrite) {
        outp.opcode := tilelink.Opcode.A.GET
      } otherwise {
        outp.opcode := tilelink.Opcode.A.PUT_FULL_DATA
      }
      outp.param := 0x0
      outp.size := inp.size
      outp.source := inp.src
      outp.address := inp.addr
      outp.mask := inp.mask.asBits //B(outp.mask.getWidth bits, default -> True)
      outp.corrupt := False
      outp.data := inp.data.asBits
    }
  )
  //--------
  val myD2hPushStm = Stream(d2hFifo.dataType())
  d2hFifo.io.push <-/< myD2hPushStm
  io.tlBus.d.translateInto(
    myD2hPushStm
  )(
    dataAssignment=(
      outp, inp
    ) => {
      outp.data := inp.data.asUInt
      outp.src := inp.source
      outp.size := inp.size
    }
  )
  //--------
  val myD2hPopStm = Stream(d2hFifo.dataType())
  myD2hPopStm <-/< d2hFifo.io.pop 
  myD2hPopStm.ready := io.d2hBus.ready
  io.d2hBus.sendData := myD2hPopStm.payload
  io.d2hBus.nextValid := myD2hPopStm.valid
  //--------
}
