package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

case class LcvBusSlicerConfig(
  //busCfg: LcvBusConfig,
  //addrSliceWidth: Int,
  mmapCfg: LcvBusMemMapConfig,
  //addrSliceStart: Int,
  //addrSliceEnd: Int,
  //optNumDevs: Option[Int]=None,
) {
  def busCfg = mmapCfg.busCfg
  def numDevs = mmapCfg.addrSliceSize
  def addrSliceStart = mmapCfg.addrSliceLo
  def addrSliceEnd = mmapCfg.addrSliceHi
  def addrSliceRange = mmapCfg.addrSliceRange
}

case class LcvBusSlicerIo(
  cfg: LcvBusSlicerConfig,
) extends Bundle {
  val host = slave(LcvBusIo(cfg=cfg.busCfg))
  val devVec = (
    Vec[LcvBusIo]{
      val tempArr = new ArrayBuffer[LcvBusIo]()
      for (idx <- 0 until cfg.numDevs) {
        tempArr += LcvBusIo(cfg=cfg.busCfg)
      }
      tempArr
    }
  )
  for (dev <- devVec.view) {
    master(dev)
  }
}
case class LcvBusSlicer(
  cfg: LcvBusSlicerConfig,
) extends Component {
  val io = LcvBusSlicerIo(cfg=cfg)

  io.host.h2dBus.ready := False
  io.host.d2hBus.valid := False
  io.host.d2hBus.payload := io.host.d2hBus.payload.getZero

  for (
    //devIdx <- 0 until cfg.numDevs
    dev <- io.devVec
  ) {
    //io.host.h2dBus.ready := False
    //io.host.d2hBus.valid := False
    //io.host.d2hBus.payload := (
    //  io.host.d2hBus.payload.getZero
    //)

    //val dev.h2dBus = io.devVec(devIdx).h2dBus
    //val dev.d2hBus = io.devVec(devIdx).d2hBus
    dev.h2dBus.valid := False
    dev.h2dBus.payload := (
      dev.h2dBus.payload.getZero
    )
    dev.d2hBus.ready := False
  }
  val stickyH2dAddrSlice = (
    UInt(cfg.mmapCfg.addrSliceWidth bits)
  )
  stickyH2dAddrSlice := (
    RegNext(stickyH2dAddrSlice, init=stickyH2dAddrSlice.getZero)
  )
  when (io.host.h2dBus.valid) {
    stickyH2dAddrSlice := (
      io.host.h2dBus.payload.addr(cfg.addrSliceRange)
    )
  }
  //when (io.host.h2dBus.valid) {
    switch (
      //io.host.h2dBus.payload.addr(cfg.addrSliceRange)
      stickyH2dAddrSlice
    ) {
      for (devIdx <- 0 until cfg.numDevs) {
        is (devIdx) {
          //val dev.h2dBus = io.devVec(devIdx).h2dBus
          def dev = io.devVec(devIdx)
          dev.h2dBus << io.host.h2dBus

          //val dev.d2hBus = io.devVec(devIdx).d2hBus
          io.host.d2hBus << dev.d2hBus
        }
      }
    }
  //}
}
