package libcheesevoyage.bus.lcvStall

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

case class LcvStallBusSlicerConfig(
  //busCfg: LcvStallBusConfig,
  //addrSliceWidth: Int,
  mmapCfg: LcvStallBusMemMapConfig,
  //addrSliceStart: Int,
  //addrSliceEnd: Int,
  //optNumDevs: Option[Int]=None,
) {
  def busCfg = mmapCfg.busCfg
  def numDevs = mmapCfg.addrSliceSize
  def addrSliceStart = mmapCfg.addrSliceStart
  def addrSliceEnd = mmapCfg.addrSliceEnd
  def addrSliceRange = mmapCfg.addrSliceRange
}

case class LcvStallBusSlicerIo(
  cfg: LcvStallBusSlicerConfig,
) extends Bundle /*with IMasterSlave*/ {
  val host = slave(LcvStallBusIo(cfg=cfg.busCfg))
  val devVec = (
    Vec[LcvStallBusIo]{
      val tempArr = new ArrayBuffer[LcvStallBusIo]()
      for (idx <- 0 until cfg.numDevs) {
        tempArr += LcvStallBusIo(cfg=cfg.busCfg)
      }
      tempArr
    }
  )
  for (dev <- devVec.view) {
    master(dev)
  }
}
case class LcvStallBusSlicer(
  cfg: LcvStallBusSlicerConfig,
) extends Component {
  val io = LcvStallBusSlicerIo(cfg=cfg)

  for (devIdx <- 0 until cfg.numDevs) {
    io.host.h2dBus.ready := False
    io.host.d2hBus.nextValid := False
    io.host.d2hBus.sendData := (
      io.host.d2hBus.sendData.getZero
    )

    val myDevH2dBus = io.devVec(devIdx).h2dBus
    val myDevD2hBus = io.devVec(devIdx).d2hBus
    myDevH2dBus.nextValid := False
    myDevH2dBus.sendData := (
      myDevH2dBus.sendData.getZero
    )
    myDevD2hBus.ready := False
  }
  switch (
    io.host.h2dBus.sendData.addr(cfg.addrSliceRange)
  ) {
    for (devIdx <- 0 until cfg.numDevs) {
      is (devIdx) {
        val myDevH2dBus = io.devVec(devIdx).h2dBus
        myDevH2dBus << io.host.h2dBus

        val myDevD2hBus = io.devVec(devIdx).d2hBus
        io.host.d2hBus << myDevD2hBus
      }
    }
  }
}
