package libcheesevoyage.bus.lcvStall

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

case class LcvStallBusMesiConfig(
  numCpus: Int,
) {
}

object LcvStallBusMesiMsg
extends SpinalEnum(defaultEncoding=binaryOneHot) {
}

case class LcvStallBusMainConfig(
  dataWidth: Int,
  addrWidth: Int,
  burstSizeWidth: Int,
  srcWidth: Int,
) {
}

case class LcvStallBusConfig(
  mainCfg: LcvStallBusMainConfig,
  mesiCfg: LcvStallBusMesiConfig,
) {
  def dataWidth = mainCfg.dataWidth
  def addrWidth = mainCfg.addrWidth
  def burstSizeWidth = mainCfg.burstSizeWidth
  def maxBurstSize = (1 << burstSizeWidth) - 1
  def srcWidth = mainCfg.srcWidth

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
      + f"(addrByteWidth:${addrByteWidth}) * 8) == addrWidth:${addrWidth}"
    )
  )
  def needSameDataWidth(
    that: LcvStallBusConfig
  ): Unit = {
    assert(
      this.dataWidth == that.dataWidth,
      s"It is required that "
      + s"dataWidth:${this.dataWidth} == that.dataWidth:${that.dataWidth}"
    )
  }
  def needSameAddrWidth(
    that: LcvStallBusConfig
  ): Unit = {
    assert(
      this.addrWidth == that.addrWidth,
      s"It is required that "
      + s"addrWidth:${this.addrWidth} == that.addrWidth:${that.addrWidth}"
    )
  }
  def needSameBurstSizeWidth(
    that: LcvStallBusConfig
  ): Unit = {
    assert(
      this.burstSizeWidth == that.burstSizeWidth,
      s"It is required that "
      + s"burstSizeWidth:${this.burstSizeWidth} "
      + s"== that.burstSizeWidth:${that.burstSizeWidth}"
    )
  }
  def needSameSrcWidth(
    that: LcvStallBusConfig
  ): Unit = {
    assert(
      this.srcWidth == that.srcWidth,
      s"It is required that "
      + s"srcWidth:${this.srcWidth} == that.srcWidth:${that.srcWidth}"
    )
  }
}

case class LcvStallBusMemMapConfig(
  busCfg: LcvStallBusConfig,
  addrSliceStart: Int,
  addrSliceEnd: Int,
  //optNumDevs: Option[Int]=None,
  optSliceSize: Option[Int]=None,
) {
  //def addrSliceEnd = addrSliceStart + addrSliceWidth - 1
  def addrSliceWidth = addrSliceEnd - addrSliceStart + 1
  def addrSliceSize = (
    optSliceSize match {
      case Some(mySliceSize) => {
        assert(
          mySliceSize > 0
        )
        assert(
          mySliceSize <= (1 << addrSliceWidth)
        )
        mySliceSize
      }
      case None => {
        (1 << addrSliceWidth)
      }
    }
  )


  def addrSliceRange = addrSliceEnd downto addrSliceStart

  assert(
    addrSliceWidth > 0,
    s"need addrSliceWidth:${addrSliceWidth} > 0"
  )
  assert(
    addrSliceWidth <= busCfg.addrWidth,
    s"need addrSliceWidth:${addrSliceWidth} "
    + s"<= busCfg.addrWidth:${busCfg.addrWidth}"
  )
  assert(
    addrSliceStart >= 0,
    s"need addrSliceStart:${addrSliceStart} >= 0"
  )
  assert(
    addrSliceStart < busCfg.addrWidth,
    s"need addrSliceStart:${addrSliceStart} "
    + s"< busCfg.addrWidth:${busCfg.addrWidth}"
  )
  assert(
    addrSliceEnd >= 0,
    s"need addrSliceEnd:${addrSliceEnd} >= 0"
  )
  assert(
    addrSliceEnd < busCfg.addrWidth,
    s"need addrSliceEnd:${addrSliceEnd} "
    + s"< busCfg.addrWidth:${busCfg.addrWidth}"
  )
}

case class LcvStallBusH2dMesiInfo(
  cfg: LcvStallBusConfig,
) extends Bundle {
  //assert(cfg.coherent)
}

case class LcvStallBusH2dSendPayload(
  cfg: LcvStallBusConfig,
) extends Bundle {
  val addr = UInt(cfg.addrWidth bits)
  val data = UInt(cfg.dataWidth bits)
  val byteEn = UInt(cfg.byteEnWidth bits)
  val burstSize = UInt(cfg.burstSizeWidth bits)
  val isWrite = Bool()
  val src = UInt(cfg.srcWidth bits)
}

case class LcvStallBusD2hSendPayload(
  cfg: LcvStallBusConfig,
) extends Bundle {
  val data = UInt(cfg.dataWidth bits)
  val burstSize = UInt(cfg.burstSizeWidth bits)
}

case class LcvStallBusIo(
  cfg: LcvStallBusConfig,
) extends Bundle with IMasterSlave {
  val h2dBus = (
    slave(new LcvStallIo[LcvStallBusH2dSendPayload, Bool](
      sendPayloadType=Some(
        LcvStallBusH2dSendPayload(cfg=cfg)
      ),
      recvPayloadType=None,
    ))
  )
  val d2hBus = (
    master(new LcvStallIo[LcvStallBusD2hSendPayload, Bool](
      sendPayloadType=Some(
        LcvStallBusD2hSendPayload(cfg=cfg)
      ),
      recvPayloadType=None,
    ))
  )

  def asMaster(): Unit = {
    master(h2dBus)
    slave(d2hBus)
  }
}
