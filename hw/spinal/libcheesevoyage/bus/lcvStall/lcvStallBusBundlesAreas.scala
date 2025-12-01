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
  //burstSizeWidth: Int,
  burstCntWidth: Int,//Option[Int],
  srcWidth: Int, //Option[Int],
) {
}

case class LcvStallBusConfig(
  mainCfg: LcvStallBusMainConfig,
  mesiCfg: LcvStallBusMesiConfig,
) {
  def dataWidth = mainCfg.dataWidth
  def addrWidth = mainCfg.addrWidth
  //def burstSizeWidth = mainCfg.burstSizeWidth
  def burstCntWidth = mainCfg.burstCntWidth
  def maxBurstSize = (
    (1 << burstCntWidth) - 1
  )
  //def burstSize: Option[Int] = (
  //  burstCntWidth match {
  //    case Some(burstCntWidth) => {
  //      Some(1 << burstCntWidth)
  //    }
  //    case None => {
  //      None
  //    }
  //  }
  //)
  def srcWidth = mainCfg.srcWidth

  def byteEnWidth: Int = (dataWidth / 8).toInt
  def addrByteWidth: Int = (addrWidth / 8).toInt
  require(
    (byteEnWidth * 8) == dataWidth,
    (
      f"It is required that "
      + f"(byteEnWidth:${byteEnWidth}) * 8 == dataWidth:${dataWidth}"
    )
  )
  require(
    (addrByteWidth * 8) == addrWidth,
    (
      f"It is required that "
      + f"(addrByteWidth:${addrByteWidth}) * 8) == addrWidth:${addrWidth}"
    )
  )
  def needSameDataWidth(
    that: LcvStallBusConfig
  ): Unit = {
    require(
      this.dataWidth == that.dataWidth,
      s"It is required that "
      + s"dataWidth:${this.dataWidth} == that.dataWidth:${that.dataWidth}"
    )
  }
  def needSameAddrWidth(
    that: LcvStallBusConfig
  ): Unit = {
    require(
      this.addrWidth == that.addrWidth,
      s"It is required that "
      + s"addrWidth:${this.addrWidth} == that.addrWidth:${that.addrWidth}"
    )
  }
  def needSameBurstCntWidth(
    that: LcvStallBusConfig
  ): Unit = {
    require(
      this.burstCntWidth == that.burstCntWidth,
      s"It is required that "
      + s"burstCntWidth:${this.burstCntWidth} "
      + s"== that.burstCntWidth:${that.burstCntWidth}"
    )
  }
  //def needSameBurstSizeWidth(
  //  that: LcvStallBusConfig
  //): Unit = {
  //  require(
  //    this.burstSizeWidth == that.burstSizeWidth,
  //    s"It is required that "
  //    + s"burstSizeWidth:${this.burstSizeWidth} "
  //    + s"== that.burstSizeWidth:${that.burstSizeWidth}"
  //  )
  //}
  def needSameSrcWidth(
    that: LcvStallBusConfig
  ): Unit = {
    require(
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
        require(
          mySliceSize > 0
        )
        require(
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

  require(
    addrSliceWidth > 0,
    s"need addrSliceWidth:${addrSliceWidth} > 0"
  )
  require(
    addrSliceWidth <= busCfg.addrWidth,
    s"need addrSliceWidth:${addrSliceWidth} "
    + s"<= busCfg.addrWidth:${busCfg.addrWidth}"
  )
  require(
    addrSliceStart >= 0,
    s"need addrSliceStart:${addrSliceStart} >= 0"
  )
  require(
    addrSliceStart < busCfg.addrWidth,
    s"need addrSliceStart:${addrSliceStart} "
    + s"< busCfg.addrWidth:${busCfg.addrWidth}"
  )
  require(
    addrSliceEnd >= 0,
    s"need addrSliceEnd:${addrSliceEnd} >= 0"
  )
  require(
    addrSliceEnd < busCfg.addrWidth,
    s"need addrSliceEnd:${addrSliceEnd} "
    + s"< busCfg.addrWidth:${busCfg.addrWidth}"
  )
}

case class LcvStallBusH2dMesiInfo(
  cfg: LcvStallBusConfig,
) extends Bundle {
  //require(cfg.coherent)
}


case class LcvStallBusH2dSendPayloadNonBurstInfo(
  cfg: LcvStallBusConfig
) extends Bundle {
  val addr = UInt(cfg.addrWidth bits)
  val data = UInt(cfg.dataWidth bits)
  val byteEn = UInt(cfg.byteEnWidth bits)
  val isWrite = Bool()
  val src = UInt(cfg.srcWidth bits)
}
case class LcvStallBusH2dSendPayloadBurstInfo(
  cfg: LcvStallBusConfig,
) extends Bundle {
  val burstCnt = UInt(cfg.burstCntWidth bits)
  val burstFirst = Bool()
  val burstLast = Bool()
}
case class LcvStallBusH2dSendPayload(
  cfg: LcvStallBusConfig,
) extends Bundle {
  //--------
  val nonBurstInfo = LcvStallBusH2dSendPayloadNonBurstInfo(cfg=cfg)
  def addr = nonBurstInfo.addr
  def data = nonBurstInfo.data
  def byteEn = nonBurstInfo.byteEn
  def isWrite = nonBurstInfo.isWrite
  def src = nonBurstInfo.src
  //--------
  val burstInfo = (cfg.burstCntWidth > 0) generate (
    LcvStallBusH2dSendPayloadBurstInfo(cfg=cfg)
  )
  //def burstSize = burstInfo.burstSize
  def burstCnt = burstInfo.burstCnt
  def burstFirst = burstInfo.burstFirst
  def burstLast = burstInfo.burstLast
  //--------
  def burstAddr(
    someBurstCnt: UInt,
  ) = (
    Cat(
      addr(addr.high downto someBurstCnt.getWidth),
      someBurstCnt,
    ).asUInt
  )
  //def selfBurstAddr() = this.burstAddr(someBurstCnt=burstCnt)
  //--------
}

case class LcvStallBusD2hSendPayloadNonBurstInfo(
  cfg: LcvStallBusConfig,
) extends Bundle {
  val data = UInt(cfg.dataWidth bits)
  val src = UInt(cfg.srcWidth bits)
}
case class LcvStallBusD2hSendPayloadBurstInfo(
  cfg: LcvStallBusConfig,
) extends Bundle {
  val burstCnt = UInt(cfg.burstCntWidth bits)
  val burstFirst = Bool()
  val burstLast = Bool()
}
case class LcvStallBusD2hSendPayload(
  cfg: LcvStallBusConfig,
) extends Bundle {
  //--------
  val nonBurstInfo = LcvStallBusD2hSendPayloadNonBurstInfo(cfg=cfg)
  def data = nonBurstInfo.data
  def src = nonBurstInfo.src
  //--------
  val burstInfo = (cfg.burstCntWidth > 0) generate (
    LcvStallBusD2hSendPayloadBurstInfo(cfg=cfg)
  )
  def burstCnt = burstInfo.burstCnt
  def burstFirst = burstInfo.burstFirst
  def burstLast = burstInfo.burstLast
  //--------
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
