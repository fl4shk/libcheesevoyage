package libcheesevoyage.bus.lcvStall

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

case class LcvStallBusSlicerConfig(
  busCfg: LcvStallBusConfig,
  //addrSliceWidth: Int,
  addrSliceStart: Int,
  addrSliceEnd: Int,
  optAddrSliceSize: Option[Int]=None,
) {
  //def addrSliceEnd = addrSliceStart + addrSliceWidth - 1
  def addrSliceWidth = addrSliceEnd - addrSliceStart + 1
  def addrSliceSize = (
    optAddrSliceSize match {
      case Some(myAddrSliceSize) => {
        assert(
          myAddrSliceSize > 0
        )
        assert(
          myAddrSliceSize <= (1 << addrSliceWidth)
        )
        myAddrSliceSize
      }
      case None => {
        (1 << addrSliceWidth)
      }
    }
  )


  def addrSliceRange = addrSliceEnd downto addrSliceStart

  assert(
    addrSliceWidth > 0
  )
  assert(
    addrSliceWidth <= busCfg.addrWidth
  )
  assert(
    addrSliceStart >= 0
  )
  assert(
    addrSliceStart < busCfg.addrWidth
  )
  assert(
    addrSliceEnd >= 0
  )
  assert(
    addrSliceEnd < busCfg.addrWidth
  )
}

case class LcvStallBusSlicerIo(
  cfg: LcvStallBusSlicerConfig,
) extends Bundle /*with IMasterSlave*/ {
  val hostBus = (
    slave(new LcvStallIo[
      LcvStallBusSendPayload,
      LcvStallBusRecvPayload,
    ](
      sendPayloadType=Some(
        LcvStallBusSendPayload(cfg=cfg.busCfg)
      ),
      recvPayloadType=Some(
        LcvStallBusRecvPayload(cfg=cfg.busCfg)
      ),
    ))
  )
  val connBusVec = (
    Vec[LcvStallIo[
      LcvStallBusSendPayload,
      LcvStallBusRecvPayload,
    ]]{
      val tempArr = new ArrayBuffer[LcvStallIo[
        LcvStallBusSendPayload,
        LcvStallBusRecvPayload,
      ]]()

      for (idx <- 0 until cfg.addrSliceSize) {
        tempArr += new LcvStallIo[
          LcvStallBusSendPayload,
          LcvStallBusRecvPayload
        ](
          sendPayloadType=Some(
            LcvStallBusSendPayload(cfg=cfg.busCfg)
          ),
          recvPayloadType=Some(
            LcvStallBusRecvPayload(cfg=cfg.busCfg)
          ),
        )
      }
      tempArr
    }
  )
  for (connBus <- connBusVec) {
    master(connBus)
  }
  //def asMaster(): Unit = {
  //  master(hostBus)
  //  for (connBus <- connBusArr.view) {
  //    slave(connBus)
  //  }
  //}
}
case class LcvStallBusSlicer(
  cfg: LcvStallBusSlicerConfig,
) extends Component {
  val io = LcvStallBusSlicerIo(cfg=cfg)

  switch (
    io.hostBus.sendData.addr(cfg.addrSliceRange)
  ) {
    for (connIdx <- 0 until cfg.addrSliceSize) {
      is (connIdx) {
        val myConnBus = io.connBusVec(connIdx)
        myConnBus.nextValid := io.hostBus.nextValid
        myConnBus.sendData := io.hostBus.sendData
        io.hostBus.recvData := myConnBus.recvData
        io.hostBus.ready := myConnBus.ready
      }
    }
  }
}
