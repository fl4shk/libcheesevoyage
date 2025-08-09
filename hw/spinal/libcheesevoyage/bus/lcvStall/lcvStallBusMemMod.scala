package libcheesevoyage.bus.lcvStall

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


import libcheesevoyage.general.RamSdpPipeIo
import libcheesevoyage.general.RamSdpPipe

// A bus-attached BRAM, LUTRAM, etc.
case class LcvStallBusMemConfig(
  //busCfg: LcvStallBusConfig,
  mmapCfg: LcvStallBusMemMapConfig,
  //depth: Int,
  init: Option[Seq[Bits]]=None,
  initBigInt: Option[Seq[BigInt]]=None,
  arrRamStyle: String="block",
  arrRwAddrCollision: String="",
) {
  def busCfg = mmapCfg.busCfg
  def depth = mmapCfg.addrSliceSize
}

case class LcvStallBusMemIo(
  cfg: LcvStallBusMemConfig,
) extends Bundle {
  val bus = slave(LcvStallBusIo(cfg=cfg.busCfg))
}

case class LcvStallBusMem(
  cfg: LcvStallBusMemConfig,
) extends Component {
  //--------
  val io = LcvStallBusMemIo(cfg=cfg)
  //--------
  val ramArr = RamSdpPipe(
    wordType=Bits(cfg.busCfg.dataWidth bits),
    depth=cfg.depth,
    init=cfg.init,
    initBigInt=cfg.initBigInt,
    arrRamStyle=cfg.arrRamStyle,
    arrRwAddrCollision=cfg.arrRwAddrCollision,
  )
  //--------
  object State
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      RD_MAIN,
      WR_MAIN
      = newElement()
  }
  val rState = (
    Reg(State())
    init(State.IDLE)
  )
  //--------
  def myH2dSendData = io.bus.h2dBus.sendData
  def myD2hSendData = io.bus.d2hBus.sendData
  myD2hSendData.setAsReg() init(myD2hSendData.getZero)

  def myH2dBusReady = io.bus.h2dBus.ready
  def myD2hBusNextValid = io.bus.d2hBus.nextValid
  myH2dBusReady.setAsReg() init(False)

  val rSavedBurstSize = Reg(cloneOf(myH2dSendData.burstSize))

  switch (rState) {
    is (State.IDLE) {
      when (RegNext(next=io.bus.h2dBus.nextValid, init=False)) {
        when (RegNext(next=myH2dSendData.isWrite, init=False)) {
          rState := State.WR_MAIN
        } otherwise {
          rState := State.RD_MAIN
        }
      }
      rSavedBurstSize := (
        RegNext(
          next=myH2dSendData.burstSize,
          init=myH2dSendData.burstSize.getZero,
        )
      )
      myD2hSendData.burstSize := 0x0
      myH2dBusReady := False
    }
    is (State.RD_MAIN) {
      //when (myD2hSendData.burstSize + 1 < myH2dSendData.burstS)
      //myD2hSendData.burstSize
    }
    is (State.WR_MAIN) {
    }
  }
}
