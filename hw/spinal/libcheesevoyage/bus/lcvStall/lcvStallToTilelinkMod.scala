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
  //sinkWidth: Int,
  //optMemCoherency: Boolean=false,
  //withBCE: Boolean=false,
  //optAtomic: Boolean=false,
) {
  // TODO: support full TL-C
  val tlCfg = tilelink.BusParameter.simple(
    addressWidth=addrWidth,
    dataWidth=dataWidth,
    sizeBytes=sizeBytes,
    sourceWidth=srcWidth,
  )
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
case class LcvStallToTilelinkHostPayload(
  cfg: LcvStallToTilelinkConfig
) extends Bundle {
  val addr = UInt(cfg.addrWidth bits)
  val data = UInt(cfg.dataWidth bits)
  val src = UInt(cfg.srcWidth bits)
  val isWrite = Bool()
  //val lock = (
  //  cfg.optAtomic
  //) generate (
  //  Bool()
  //)
}
case class LcvStallToTilelinkDevPayload(
  cfg: LcvStallToTilelinkConfig
) extends Bundle {
  val data = UInt(cfg.dataWidth bits)
}
case class LcvStallToTilelinkIo(
  cfg: LcvStallToTilelinkConfig
) extends Bundle {
  val lcvStall = slave(new LcvStallIo[
    LcvStallToTilelinkHostPayload,
    LcvStallToTilelinkDevPayload,
  ](
    hostPayloadType=Some(LcvStallToTilelinkHostPayload(cfg=cfg)),
    devPayloadType=Some(LcvStallToTilelinkDevPayload(cfg=cfg)),
  ))
  val tlBus = master(tilelink.Bus(
    p=cfg.tlCfg
  ))
}
case class LcvStallToTilelinkHost(
  cfg: LcvStallToTilelinkConfig
) extends Component {
  //--------
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
    val temp = Reg(LcvStallToTilelinkDevPayload(cfg=cfg))
    temp.init(temp.getZero)
    temp
  }
  io.lcvStall.devData := rDevData
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
        when (io.lcvStall.hostData.isWrite) {
          io.tlBus.a.opcode := tilelink.Opcode.A.PUT_FULL_DATA
        } otherwise {
          io.tlBus.a.opcode := tilelink.Opcode.A.GET
        }
        io.tlBus.a.param := 0x0
        io.tlBus.a.size := 1//(cfg.dataWidth / 8)
        io.tlBus.a.source := io.lcvStall.hostData.src
        io.tlBus.a.address := io.lcvStall.hostData.addr
        io.tlBus.a.mask := B(io.tlBus.a.mask.getWidth bits, default -> True)
        io.tlBus.a.corrupt := False
        io.tlBus.a.data := io.lcvStall.hostData.data.asBits
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
