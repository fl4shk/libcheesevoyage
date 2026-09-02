package libcheesevoyage.general

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.sim._
import spinal.lib.misc.pipeline._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.math._

import libcheesevoyage.math._

case class LcvUartCtrlConfig(
  clkRate: HertzNumber, // 
  bitRate: HertzNumber=115200 Hz, // bits / sec
  wordWidth: Int=8,
) {
  //val numStopBits = 1

  val clkParam = (1 ns) * ((1.0 Hz) / clkRate)
  val bitParam = (1 ns) * ((1.0 Hz) / bitRate)
  val cyclesPerBit = (bitParam / clkParam).toInt
  val cntWidth = 1 + log2Up(cyclesPerBit)
}

case class LcvUartIo(
  cfg: LcvUartCtrlConfig
) extends Bundle with IMasterSlave {
  val tx = in(Bool())
    // the TX payload line
    // (includes the serialized data word and start/stop bits)

  //val rx = out(Bool())
  //  // TODO
  //  // the RX payload line
  //  // (includes the serialized data word and start/stop bits)

  val dtr = in(Bool())
    // Data Terminal Ready
    // Driven by the TX.

  val dsr = out(Bool())
    // Data Set Ready
    // The mirror of DTR. Asserted when the DCE is powered on and ready.
    // Driven by the RX.

  val rts = in(Bool())
    // Request to Send
    // TX's "I'm ready for data."
    // Driven by the TX.

  val cts = out(Bool())
    // Clear to Send
    // RX's "I'm ready for data."
    // Driven by the RX.

  //val dcd = out(Bool())
  //  // Data Carrier Detect
  //  // On non-modem links, DCD is often forced high by jumpering it to DSR
  //  // Or ignored entirely
  //  // Driven by the RX.

  def asMaster(): Unit = {
    out(tx)
    //in(rx)
    out(dtr)
    in(dsr)
    out(rts)
    in(cts)
    //in(dcd)
  }
}
case class LcvUartCtrlIo(
  cfg: LcvUartCtrlConfig,
) extends Bundle with IMasterSlave {
  val uartIo = slave(
    LcvUartIo(cfg=cfg)
  )
  def tx = uartIo.tx

  // TODO:
  //def rx = uartIo.rx

  def dtr = uartIo.dtr
  def dsr = uartIo.dsr
  def rts = uartIo.rts
  def cts = uartIo.cts

  val push = master(
    // what data do we send?
    Stream(UInt(cfg.wordWidth bits))
  )

  //val pop = slave(
  //  // TODO:
  //  // output interface for received data.
  //  Stream(UInt(cfg.wordWidth bits))
  //)

  def asMaster(): Unit = {
    master(uartIo)
    slave(push)
    //master(pop)
  }
}

case class LcvUartCtrlTx(
  cfg: LcvUartCtrlConfig,
) extends Component {
  //--------
  val io = master(
    LcvUartCtrlIo(cfg=cfg)
  )
  //--------
  io.tx.setAsReg() init(True)
  //io.dtr.setAsReg() init(True)
  io.dtr := True
  io.rts := True // TODO: implement support for RX
  //--------
  val rDidInit = Reg(Bool(), init=False)

  when (
    //RegNextWhen(
    //  True,
    //  cond=(
    //    RegNext(io.dsr, init=False)
    //  ),
    //  init=False
    //)
    RegNext(io.dsr, init=False)
  ) {
    rDidInit := True
  }
  when (
    //rDidInit
    //&&
    RegNext(!io.cts, init=False)
  ) {
    rDidInit := False
  }

  val rSavedPushWord = (
    Reg(Flow(
      UInt(cfg.wordWidth bits)
    ))
  )
  val rBitCnt = (
    Reg(UInt(log2Up(cfg.wordWidth) bits))
    init(0x0)
  )

  val rCyclesCnt = (
    Reg(UInt(cfg.cntWidth bits))
    init(cfg.cyclesPerBit - 1)
  )
  //val myCyclesCntRstVal = (
  //  cfg.cyclesPerBit - 1
  //)

  val rNextBitIsStop = Reg(Bool(), init=False)
  val rSeenTxFinish = Reg(Bool(), init=False)


  io.push.ready := (
    !rSavedPushWord.fire
    && rDidInit
    && RegNext(io.cts, init=False)
  )

  when (io.push.fire) {
    rSavedPushWord.valid := True
    rSavedPushWord.payload := io.push.payload

    rBitCnt := 0x0
    rCyclesCnt := (
      //(1 << cfg.cntWidth) - 1
      //0x0
      cfg.cyclesPerBit - 1
    )

    rNextBitIsStop := False
    rSeenTxFinish := False
    io.tx := False // `start` bit is `False`
  }

  when (
    rSavedPushWord.fire
    && rBitCnt === cfg.wordWidth - 1
  ) {
    rNextBitIsStop := True
  }

  switch (
    (rDidInit && rSavedPushWord.fire)
    ## rCyclesCnt.msb
    ## rNextBitIsStop
    ## rSeenTxFinish
  ) {
    is (
      //M"110"
      M"1100"
    ) {
      // changing from either a start bit or a word bit to the next word bit
      rBitCnt := rBitCnt + 1
      io.tx := rSavedPushWord.payload(rBitCnt)
      rCyclesCnt := (
        //0x0
        cfg.cyclesPerBit - 1
      )
    }
    is (
      //M"111"
      M"1110"
    ) {
      // changing to a stop bit
      rBitCnt := rBitCnt + 1
      io.tx := True // the `stop` bit is `True`
      rSeenTxFinish := True
      rCyclesCnt := (
        //0x0
        cfg.cyclesPerBit - 1
      )
    }
    is (
      M"1111"
    ) {
      // done transmitting a stop bit
      rSavedPushWord.valid := False
      rCyclesCnt := (
        //0x0
        cfg.cyclesPerBit - 1
      )
    }
    is (
      //M"10--"
      M"10--"
    ) {
      // in-progress of sending a bit of any sort, i.e. not switching to a
      // new bit index.
      rCyclesCnt := rCyclesCnt - 1
    }

    default {
      // no change for these other conditions
    }
  }
  //--------
}
