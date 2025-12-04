package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


case class LcvBusArbiterConfig(
  busCfg: LcvBusConfig,
  numHosts: Int,
  //isPriority: Boolean,
) {
}

case class LcvBusArbiterIo(
  cfg: LcvBusArbiterConfig
) extends Bundle {
  val hostVec = (
    Vec[LcvBusIo]{
      val tempArr = new ArrayBuffer[LcvBusIo]()
      for (idx <- 0 until cfg.numHosts) {
        tempArr += LcvBusIo(cfg=cfg.busCfg)
      }
      tempArr
    }
  )
  for (host <- hostVec.view) {
    slave(host)
  }
  val dev = (
    master(LcvBusIo(cfg=cfg.busCfg))
  )
}

case class LcvBusArbiter(
  cfg: LcvBusArbiterConfig,
) extends Component {
  // (for now) round robin bus arbiter
  val io = LcvBusArbiterIo(cfg=cfg)

  for (hostIdx <- 0 until cfg.numHosts) {
    io.dev.d2hBus.ready := False
    io.dev.h2dBus.valid := False
    io.dev.h2dBus.payload := (
      io.dev.h2dBus.payload.getZero
    )
    val myHostH2dBus = io.hostVec(hostIdx).h2dBus
    val myHostD2hBus = io.hostVec(hostIdx).d2hBus
    myHostH2dBus.valid := False
    myHostH2dBus.payload := (
      myHostH2dBus.payload.getZero
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

  when (RegNext(next=myHostH2dBus.valid, init=False)) {
    io.dev.h2dBus << myHostH2dBus 
    myHostD2hBus << io.dev.d2hBus
    when (myHostH2dBus.ready) {
      doIncrCnt()
    }
  } otherwise {
    doIncrCnt()
  }
}
