package libcheesevoyage.bus.lcvStall

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

case class LcvStallBusConfig(
  dataWidth: Int,
  addrWidth: Int,
  burstSizeWidth: Int,
) {
  def byteEnWidth: Int = (dataWidth / 8).toInt
  def addrByteWidth: Int = (addrWidth / 8).toInt
  assert(
    (byteEnWidth * 8) == dataWidth,
    (
      f"It is required that "
      + f"(byteEnWidth:${byteEnWidth}) * 8 == dataWidth:${dataWidth}"
    )
  )
  assert(
    (addrByteWidth * 8) == addrWidth,
    (
      f"It is required that "
      + f"(addrByteWidth:${addrByteWidth}) * 8) === addrWidth:${addrWidth}"
    )
  )
}

case class LcvStallBusSendPayload(
  cfg: LcvStallBusConfig
) extends Bundle {
  val addr = UInt(cfg.addrWidth bits)
  val data = UInt(cfg.dataWidth bits)
  val byteEn = UInt(cfg.byteEnWidth bits)
  val burstSize = UInt(cfg.burstSizeWidth bits)
  val isWrite = Bool()
}

case class LcvStallBusRecvPayload(
  cfg: LcvStallBusConfig
) extends Bundle {
  val data = UInt(cfg.dataWidth bits)
  val burstSize = UInt(cfg.burstSizeWidth bits)
}
