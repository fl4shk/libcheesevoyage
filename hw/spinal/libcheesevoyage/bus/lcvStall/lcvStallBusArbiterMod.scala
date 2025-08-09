package libcheesevoyage.bus.lcvStall

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


case class LcvStallBusArbiterConfig(
  busCfg: LcvStallBusConfig,
  numHosts: Int,
  //isPriority: Boolean,
) {
}

case class LcvStallBusArbiterIo(
  cfg: LcvStallBusArbiterConfig
) extends Bundle {
  val hostVec = (
    Vec[LcvStallBusIo]{
      val tempArr = new ArrayBuffer[LcvStallBusIo]()
      for (idx <- 0 until cfg.numHosts) {
        tempArr += LcvStallBusIo(cfg=cfg.busCfg)
      }
      tempArr
    }
  )
  for (host <- hostVec.view) {
    slave(host)
  }
  val dev = (
    master(LcvStallBusIo(cfg=cfg.busCfg))
  )
}

case class LcvStallBusArbiter(
  cfg: LcvStallBusArbiterConfig,
) extends Component {
  // (for now) round robin bus arbiter
  val io = LcvStallBusArbiterIo(cfg=cfg)

  for (hostIdx <- 0 until cfg.numHosts) {
    io.dev.d2hBus.ready := False
    io.dev.h2dBus.nextValid := False
    io.dev.h2dBus.sendData := (
      io.dev.h2dBus.sendData.getZero
    )
    val myHostH2dBus = io.hostVec(hostIdx).h2dBus
    val myHostD2hBus = io.hostVec(hostIdx).d2hBus
    myHostH2dBus.nextValid := False
    myHostH2dBus.sendData := (
      myHostH2dBus.sendData.getZero
    )
    myHostD2hBus.ready := False
  }

  val rArbitCnt = (
    Reg(UInt(log2Up(cfg.numHosts) bits))
    init(0x0)
  )
  val myHostH2dBus = io.hostVec(rArbitCnt).h2dBus
  val myHostD2hBus = io.hostVec(rArbitCnt).d2hBus

  def doIncrCnt(): Unit = {
    if ((1 << log2Up(cfg.numHosts)) == cfg.numHosts) {
      rArbitCnt := rArbitCnt + 1
    } else {
      when (rArbitCnt + 1 < cfg.numHosts) {
        rArbitCnt := rArbitCnt + 1
      } otherwise {
        rArbitCnt := 0x0
      }
    }
  }

  when (RegNext(next=myHostH2dBus.nextValid, init=False)) {
    io.dev.h2dBus << myHostH2dBus 
    myHostD2hBus << io.dev.d2hBus
    when (myHostH2dBus.ready) {
      doIncrCnt()
    }
  } otherwise {
    doIncrCnt()
  }
}
