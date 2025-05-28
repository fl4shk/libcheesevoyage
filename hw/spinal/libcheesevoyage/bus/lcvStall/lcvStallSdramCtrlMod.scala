//
// sdram - Adapted/modified starting from SystemVerilog code in the
//  MiSTer FPGA project, which is...
// Copyright (c) 2015-2019 Sorgelig
//
// Some parts of SDRAM code used from project:
// http://hamsterworks.co.nz/mediawiki/index.php/Simple_SDRAM_Controller
//
// This source file is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This source file is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package libcheesevoyage.bus.lcvStall

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._
import spinal.lib.io._



case class AltddioOutConfig(
  extend_oe_disable: String="OFF",
  intended_device_family: String="Cyclone V",
  invert_output: String="OFF",
  lpm_hint: String="UNUSED",
  lpm_type: String="altddio_out",
  oe_reg: String="UNREGISTERED",
  power_up_high: String="OFF",
  width: Int=1,
) {
}
case class altddio_out(
  cfg: AltddioOutConfig
) extends BlackBox {
  //addGeneric("wordCount", "asdf")

  addGeneric("extend_oe_disable", cfg.extend_oe_disable)
  addGeneric("intended_device_family", cfg.intended_device_family)
  addGeneric("invert_output", cfg.invert_output)
  addGeneric("lpm_hint", cfg.lpm_hint)
  addGeneric("lpm_type", cfg.lpm_type)
  addGeneric("oe_reg", cfg.oe_reg)
  addGeneric("power_up_high", cfg.power_up_high)
  addGeneric("width", cfg.width)

  val io = new Bundle {
    //.datain_h(1'b0),
    //.datain_l(1'b1),
    //.outclock(clk),
    //.dataout(SDRAM_CLK),
    //.aclr(1'b0),
    //.aset(1'b0),
    //.oe(1'b1),
    //.outclocken(1'b1),
    //.sclr(1'b0),
    //.sset(1'b0)
    val datain_h = in(UInt(cfg.width bits)) // 1'b0
    val datain_l = in(UInt(cfg.width bits)) // 1'b1
    val outclock = in(Bool()) // clk (clockDomain.clock)
    val dataout = out(UInt(cfg.width bits)) // io.sdram.clk
    val aclr = in(Bool()) // 1'b0
    val aset = in(Bool()) // 1'b0
    val oe = in(Bool()) // 1'b1
    val outclocken = in(Bool()) // 1'b1
    val sclr = in(Bool()) // 1'b0
    val sset = in(Bool()) // 1'b
  }
  noIoPrefix()
}

object SdramCmd
extends SpinalEnum(defaultEncoding=binarySequential) {
  val
    LoadMode,       // 3'b000
    AutoRefresh,    // 3'b001
    Precharge,      // 3'b010
    Active,         // 3'b011
    Write,          // 3'b100
    Read,           // 3'b101
    Bad,            // 3'b110
    Nop             // 3'b111
    = newElement()
}

case class LcvStallSdramCtrlConfig(
  clkRate: HertzNumber,
  //burstLen: Int=2, // 32-bit
) {
  //--------
  // idea for later: change these to *parameters* of
  // `LcvStallSdramCtrlConfig`
  def sdramDqWidth: Int = 16
  def sdramAWidth: Int = 13
  def sdramBaWidth: Int = 2
  def idleCntMax: Int = 5
  def addrWidth: Int = 27
  def wordWidth: Int = (
    //16
    32
  )
  val altddioOutCfg: AltddioOutConfig = AltddioOutConfig() 
  //--------

  //def burstLen = 4
  def burstLen = 2
  def burstCode: UInt = (
    // 000=1, 001=2, 010=4, 011=8
    if (burstLen == 8) (
      U"3'b011"
    ) else if (burstLen == 4) (
      U"3'b010"
    ) else if (burstLen == 2) (
      U"3'b001"
    ) else (
      U"3'b000"
    )
  )
  def accessType = U"1'b0" // 0=sequential, 1=interleaved
  def casLatency = (
    if (clkRate < (100.0 MHz)) (
      (U"3'd2", 2, 0)
    ) else (
      (U"3'd3", 3, 1)
    )
  )
  def opMode = U"2'b00" // only 00 (standard operation) allowed
  def noWriteBurst = U"1'b1"  // 0=write burst enable, 1=only single acc wr
  //def doWriteBurst = U"1'b0"
  def mode = Cat(
    List(
      U"3'b000",
      noWriteBurst,
      //doWriteBurst,
      opMode,
      casLatency._1,
      accessType,
      burstCode,
    ).reverse
  ).asUInt

  //def sdramStartupCyclesBase = (
  //  (100.0 us) * clkRate
  //)
  def sdramStartupCycles = (
    //(sdramStartupCyclesBase + 1.21).toInt // 12100 cycles at 100 MHz
    (((100.0 us) * clkRate) + 1.21).toInt // 12100 cycles at 100 MHz
  )
  def cyclesPerRefresh = (
    //(((64 ms) * (64 MHz)) / 8192) - 1
    //(((64 ms) * clkRate) / 8192) - 1

    // 545.875.toInt = 545
    ((((64 ms) * (clkRate * 0.7)) / 8192) - 1).toInt
  )
  //print(
  //  cyclesPerRefresh
  //)
  def rfshCntWidth = (
    14 max log2Up(sdramStartupCycles) max log2Up(cyclesPerRefresh)
  )
  print(s"rfshCntWidth:${rfshCntWidth}")
  def startupRfshMax = (
    (1 << rfshCntWidth) - 1
  )
  def rfshCntInit = (
    startupRfshMax - sdramStartupCycles
  )
}


case class LcvStallSdramIo(
  cfg: LcvStallSdramCtrlConfig,
) extends Bundle {
  //val dq = master(TriState(UInt(cfg.sdramDqWidth bits)))
  //  // bidirectional data bus
  val dq = inout(Analog(UInt(cfg.sdramDqWidth bits)))
  val a = out(UInt(cfg.sdramAWidth bits))     // addr bus
  val dqml = out(Bool())                      // \ two byte masks
  val dqmh = out(Bool())                      // /
  val ba = out(UInt(cfg.sdramBaWidth bits))   // two banks
  val nCs = out(Bool())                       // a single chip select
  val nWe = out(Bool())                       // write enable
  val nRas = out(Bool())                      // row address select
  val nCas = out(Bool())                      // columns address select
  val cke = out(Bool())                       // clock enable
  val clk = out(Bool())                       // clock

  //noIoPrefix()
}


case class LcvStallSdramCtrlH2dPayload(
  cfg: LcvStallSdramCtrlConfig,
) extends Bundle {
  val isWrite = Bool()
  val addr = UInt(cfg.addrWidth bits)
  val data = UInt(cfg.wordWidth bits)
  val byteEn = UInt(((cfg.wordWidth / 8).toInt) bits)
}
case class LcvStallSdramCtrlD2hPayload(
  cfg: LcvStallSdramCtrlConfig,
) extends Bundle {
  val data = UInt(cfg.wordWidth bits)
}

case class LcvStallSdramCtrlIo(
  cfg: LcvStallSdramCtrlConfig,
) extends Bundle /*with IMasterSlave*/ {
  val lcvStall = slave(new LcvStallIo[
    LcvStallSdramCtrlH2dPayload,
    LcvStallSdramCtrlD2hPayload,
  ](
    sendPayloadType=Some(LcvStallSdramCtrlH2dPayload(cfg=cfg)),
    recvPayloadType=Some(LcvStallSdramCtrlD2hPayload(cfg=cfg)),
  ))
  //val doRefresh = in(Bool())
  val sdram = LcvStallSdramIo(cfg=cfg)
  //def asMaster(): Unit = {
  //  master(lcvStall)
  //}
}

case class LcvStallSdramCtrl(
  cfg: LcvStallSdramCtrlConfig,
) extends Component {
  //assert(cfg.clkRate >= (99.2 MHz))
  //assert(cfg.clkRate <= (100.7 MHz))
  //--------
  val io = LcvStallSdramCtrlIo(cfg=cfg)

  io.lcvStall.ready.setAsReg() init(io.lcvStall.ready.getZero)
    // copy the MiSTer SDRAM controllers for the above ^

  io.lcvStall.recvData.setAsReg() init(io.lcvStall.recvData.getZero)
  //io.sdram.dq.setAsReg()
    // I'm not sure how SpinalHDL deals with `inout`
    // registers, so see the set of signals called `rDqTriState` instead.
  io.sdram.a.setAsReg() init(io.sdram.a.getZero)
  io.sdram.ba.setAsReg() init(io.sdram.ba.getZero)

  //--------
  object State extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      Startup,
      WaitRdWr,
      DoRdWr0,
      DoWr1,
      WaitIdleCnt,
      Idle,
      Rfsh
      = newElement()
  }

  //val rSavedWr = Reg(Bool()) init(False)
  val rSavedH2d = Reg(LcvStallSdramCtrlH2dPayload(cfg=cfg))
  rSavedH2d.init(rSavedH2d.getZero)

  val rCasAddr = Reg(UInt(cfg.sdramAWidth bits)) init(0x0)

  val rDqTriState = {
    val temp = Reg(TriState(UInt(cfg.sdramDqWidth bits)))
    temp.init(temp.getZero)
    temp
  }
  rDqTriState.writeEnable.setAsReg() init(rDqTriState.writeEnable.getZero)

  rDqTriState.read := io.sdram.dq
  when (rDqTriState.writeEnable) {
    //io.dq
    io.sdram.dq := rDqTriState.write
  }

  def rFullAddr = (
    rCasAddr(12 downto 9), io.sdram.ba, io.sdram.a, rCasAddr(8 downto 0)
  )

  val rState = Reg(State()) init(State.Startup)
  val rHaveValid = Reg(Vec.fill(2)(Bool())) //init(False)
  for (idx <- 0 until rHaveValid.size) {
    rHaveValid(idx).init(False)
  }

  val rRfshCnt = (
    Reg(UInt(cfg.rfshCntWidth bits))
    init(cfg.rfshCntInit)
  )
  //val temp = UInt(3 bits)
  val rIdleCnt = (
    Reg(SInt(log2Up(cfg.idleCntMax + 1) + 1 bits))
    init(cfg.idleCntMax)
  )
  val rCmd = Reg(SdramCmd()) init(SdramCmd.Nop)
  val rChip = Reg(Bool()) init(False)
  val rDataReadyDelay = (
    Reg(
      UInt(
        (cfg.casLatency._2 + cfg.burstLen + cfg.casLatency._3 + 1) bits
      )
    )
    init(0x0)
  )
  //--------
  io.sdram.nCs := rChip
  io.sdram.nRas := rCmd.asBits(2)
  io.sdram.nCas := rCmd.asBits(1)
  io.sdram.nWe := rCmd.asBits(0)
  io.sdram.cke := True
  (io.sdram.dqmh, io.sdram.dqml) := io.sdram.a(12 downto 11)
  //io.sdram.dqmh := io.sdram.a(12)
  //io.sdram.dqml := io.sdram.a(11)

  rHaveValid.head := RegNext(io.lcvStall.nextValid) init(False)
  when (RegNext(io.lcvStall.nextValid) =/= rHaveValid.head) {
    rHaveValid.last := True
  }

  rRfshCnt := rRfshCnt + 1
  rDqTriState.writeEnable := False
  rDataReadyDelay := Cat(False, (rDataReadyDelay >> 1)).asUInt

  //when (rDataReadyDelay) {
  //}
  for (burstIdx <- 0 until cfg.burstLen) {
    when (rDataReadyDelay(cfg.burstLen - 1 - burstIdx)) {
      io.lcvStall.recvData.data(
        (
          (cfg.sdramDqWidth * (burstIdx + 1)) - 1
        ) downto (
          cfg.sdramDqWidth * burstIdx
        )
      ) := rDqTriState.read
    }
  }
  io.lcvStall.ready := False
  when (rDataReadyDelay(1)) {
    io.lcvStall.ready := True
  }

  switch (rState) {
    is (State.Startup) {
      io.sdram.a := 0x0
      io.sdram.ba := 0x0
      when (rRfshCnt === cfg.startupRfshMax - 64) {
        rChip := False
      }
      when (rRfshCnt === cfg.startupRfshMax - 32) {
        rChip := True
      }
      // All the commands during the startup are NOPs, except these
      when (
        (
          rRfshCnt === cfg.startupRfshMax - 63
        ) || (
          rRfshCnt === cfg.startupRfshMax - 31
        )
      ) {
        // Ensure all rows are closed.
        rCmd := SdramCmd.Precharge
        io.sdram.a(10) := True // all banks
        io.sdram.ba := 0x0
      }
      when (
        (
          rRfshCnt === cfg.startupRfshMax - 55
        ) || (
          rRfshCnt === cfg.startupRfshMax - 23
        )
      ) {
        // These refreshes need to be at least tREF (66ns) apart.
        rCmd := SdramCmd.AutoRefresh
      }
      when (
        (
          rRfshCnt === cfg.startupRfshMax - 47
        ) || (
          rRfshCnt === cfg.startupRfshMax - 15
        )
      ) {
        rCmd := SdramCmd.AutoRefresh
      }
      when (
        (
          rRfshCnt === cfg.startupRfshMax - 39
        ) || (
          rRfshCnt === cfg.startupRfshMax - 7
        )
      ) {
        rCmd := SdramCmd.LoadMode
        io.sdram.a := cfg.mode
      }
      when (rRfshCnt === 0x0) {
        rState := State.Idle
        rRfshCnt := 0x0
      }
    }
    is (State.WaitRdWr) {
      rState := State.DoRdWr0
    }
    is (State.DoRdWr0) {
      io.sdram.a := rCasAddr
      when (rSavedH2d.isWrite) {
        rCmd := SdramCmd.Write
        rDqTriState.writeEnable := True
        rDqTriState.write := rSavedH2d.data(15 downto 0)
        rState := State.DoWr1
      } otherwise {
        rCmd := SdramCmd.Read
        rIdleCnt := cfg.idleCntMax
        rState := State.WaitIdleCnt

        //rDataReadyDelay(
        //  cfg.casLatency._2 + cfg.burstLen + cfg.casLatency._3
        //) := True
        rDataReadyDelay.msb := True
      }
    }
    is (State.DoWr1) {
      rState := State.WaitIdleCnt

      io.sdram.a(10) := True
      io.sdram.a(0) := True
      rCmd := SdramCmd.Write
      rDqTriState.writeEnable := True
      rDqTriState.write := rSavedH2d.data(31 downto 16)
      rCasAddr(rCasAddr.high downto rCasAddr.high - 1) := (
        ~rSavedH2d.byteEn(3 downto 2)
      )
      rIdleCnt := 2

      io.lcvStall.ready := True
    }
    is (State.WaitIdleCnt) {
      when ((rIdleCnt - 1).msb) {
        rIdleCnt := cfg.idleCntMax
        rState := State.Idle
      } otherwise {
        rIdleCnt := rIdleCnt - 1
      }
    }
    is (State.Idle) {
      when (rRfshCnt > cfg.cyclesPerRefresh) {
        // This SDRAM controller's main way of doing refresh
        rState := State.Rfsh
        rCmd := SdramCmd.AutoRefresh
        rRfshCnt := rRfshCnt - cfg.cyclesPerRefresh + 1
        rChip := False
      } elsewhen (
        //io.lcvStall.rValid
        rHaveValid.last
      ) {
        //(
        //  rCasAddr(12 downto 9),
        //  io.sdram.ba,
        //  io.sdram.a,
        //  rCasAddr(8 downto 0),
        //)
        rFullAddr := Cat(
          //U"2'b00",
          ~rSavedH2d.byteEn(1 downto 0),
          ~io.lcvStall.sendData.isWrite, 
          io.lcvStall.sendData.addr(25 downto 1),
        )
        rSavedH2d := io.lcvStall.sendData
        rChip := io.lcvStall.sendData.addr(26)
        rHaveValid.last := False
        rCmd := SdramCmd.Active
        rState := State.WaitRdWr
      }
    }
    is (State.Rfsh) {
      rState := State.WaitIdleCnt
      rCmd := SdramCmd.AutoRefresh
      rChip := True
    }
  }
  //--------
  //.datain_h(1'b0),
  //.datain_l(1'b1),
  //.outclock(clk),
  //.dataout(SDRAM_CLK),
  //.aclr(1'b0),
  //.aset(1'b0),
  //.oe(1'b1),
  //.outclocken(1'b1),
  //.sclr(1'b0),
  //.sset(1'b0)
  val sdramclkDdr = altddio_out(
    cfg=cfg.altddioOutCfg
  )
  sdramclkDdr.io.datain_h := 0x0
  sdramclkDdr.io.datain_l := 0x1
  sdramclkDdr.io.outclock := ClockDomain.current.readClockWire
  io.sdram.clk := sdramclkDdr.io.dataout.lsb
  sdramclkDdr.io.aclr := False
  sdramclkDdr.io.aset := False
  sdramclkDdr.io.oe := True
  sdramclkDdr.io.outclocken := True
  sdramclkDdr.io.sclr := False
  sdramclkDdr.io.sset := False
  //--------
}

//case class LcvSdramWrapIo(
//  cfg: LcvStallSdramCtrlConfig,
//) extends Bundle {
//  val start = in(Bool())
//  val done = out(Bool())
//  val isWrite = in(Bool())
//  val ready = out(Bool())
//    // strobe. when writing, one means that data from wdat written to 
//    // memory
//    //
//    // when reading, one means that data read from memroy is on rdat
//    // output
//  val wdat = in(UInt(16 bits)) // input, data to be written to memory
//  val rdat = out(UInt(16 bits)) // output, data last read from memory 
//  val sz = in(UInt(2 bits))
//  val chip = in(UInt(2 bits))
//
//  val sdram = LcvStallSdramIo(cfg=cfg)
//}
//
//case class LcvSdramWrap(
//  cfg: LcvStallSdramCtrlConfig
//) extends Component {
//  val io = LcvSdramWrapIo(cfg=cfg)
//
//  io.done.setAsReg() init(io.done.getZero)
//  io.ready.setAsReg() init(io.ready.getZero)
//  io.rdat.setAsReg() init(io.rdat.getZero)
//  //io.sdram.dq.setAsReg() init(io.sdram.dq.getZero)
//
//  val sdramCtrl = LcvStallSdramCtrl(cfg=cfg)
//  io.sdram <> sdramCtrl.io.sdram
//
//  def scLcvStall = sdramCtrl.io.lcvStall
//  //scLcvStall.nextValid := True
//  //scLcvStall.sendData.setAsReg() init(scLcvStall.sendData.getZero)
//  val rSendData = {
//    val temp = Reg(LcvStallSdramCtrlH2dPayload(cfg=cfg))
//    temp.init(temp.getZero)
//    temp
//  }
//  scLcvStall.sendData := rSendData
//  io.ready := (
//    RegNext(scLcvStall.nextValid, init=False)
//    && scLcvStall.ready
//  )
//  when (!rSendData.addr(1)) {
//    io.rdat := scLcvStall.recvData.data(15 downto 0)
//  } otherwise {
//    io.rdat := scLcvStall.recvData.data(31 downto 16)
//  }
//  def szUnset = U"2'b00"
//  def sz32M = U"2'b01"
//  def sz64M = U"2'b10"
//  def sz128M = U"2'b11"
//
//  //object State extends SpinalEnum(defaultEncoding=binarySequential) {
//  //  val
//  //    //Startup,
//  //    Idle,
//  //    Running
//  //    = newElement()
//  //}
//  //val rState = Reg(State()) init(State.Idle)
//
//  //switch (rState) {
//  //  //is (State.Startup) {
//  //  //}
//  //  is (State.Idle) {
//  //    io.done := False
//  //  }
//  //  is (State.Running) {
//  //  }
//  //}
//  //val rDone3 = Reg(Bool()) init(False)
//  val nextDone3 = Bool()
//  val rDone3 = RegNext(nextDone3) init(nextDone3.getZero)
//  nextDone3 := rDone3
//
//  rSendData.byteEn := U(rSendData.byteEn.getWidth bits, default -> True)
//
//  scLcvStall.nextValid := False
//  when (
//    io.start
//    || io.done
//  ) {
//    io.done := False
//    nextDone3 := False
//    //rState := State.Idle
//    rSendData.addr := 0x0
//    rSendData.isWrite := io.isWrite
//    //io.ready := False
//  } otherwise {
//    rSendData.isWrite := io.isWrite
//    when (
//      (io.chip === 0)
//      && (io.sz === sz128M)
//      && rSendData.addr(25 downto 2).andR
//    ) {
//      //scLcvStall.nextValid := False
//      nextDone3 := True
//    }
//    when (
//      (io.chip === 1)
//      && (io.sz === sz128M)
//      && rSendData.addr(rSendData.addr.high - 1 downto 2).andR
//    ) {
//      //scLcvStall.nextValid := False
//      nextDone3 := True
//    }
//    when (
//      (io.chip === 2)
//      && (io.sz === sz128M)
//      && rSendData.addr(rSendData.addr.high downto 2).andR
//    ) {
//      //scLcvStall.nextValid := False
//      nextDone3 := True
//      //io.done := 
//    }
//    when (
//      (io.sz === sz64M)
//      && rSendData.addr(rSendData.addr.high - 1 downto 2).andR
//    ) {
//      //scLcvStall.nextValid := False
//      nextDone3 := True
//    }
//    when (
//      (io.sz === sz32M)
//      && rSendData.addr(rSendData.addr.high - 2 downto 2).andR
//    ) {
//      //scLcvStall.nextValid := False
//      nextDone3 := True
//    }
//    when (nextDone3) {
//      scLcvStall.nextValid := False
//    }
//    when (rDone3) {
//      scLcvStall.nextValid := False
//      io.done := True
//    } otherwise {
//      scLcvStall.nextValid := True
//      when (
//        RegNext(scLcvStall.nextValid, init=False)
//        && scLcvStall.ready
//      ) {
//        rSendData.data := io.wdat.resize(rSendData.data.getWidth)
//        val myAddrRangeLo = 2
//        val myAddrRange = rSendData.addr.high downto myAddrRangeLo
//        rSendData.addr(myAddrRange) := rSendData.addr(myAddrRange) + 1
//      }
//    }
//  }
//  //val rAddr = Reg(UInt(cfg.addrWidth bits)) init(0x0)
//}

//case class LcvSdramWrap(
//  cfg: LcvStallSdramCtrlConfig,
//) extends Component {
//  val io = LcvSdramWrapIo(cfg=cfg)
//
//  io.done.setAsReg() init(io.done.getZero)
//  io.ready.setAsReg() init(io.ready.getZero)
//  io.rdat.setAsReg() init(io.rdat.getZero)
//  io.sdram.dq.setAsReg() init(io.sdram.dq.getZero)
//
//  val sdaddr = Reg(UInt(13 bits)) init(0x0)
//  val sdaddr2 = Reg(UInt(13 bits)) init(0x0)
//  val ba = Reg(UInt(2 bits)) init(0x0)
//  val ba2 = Reg(UInt(2 bits)) init(0x0)
//  val cmd = Reg(SdramCmd()) init(SdramCmd.Nop)
//  val cmd2 = Reg(SdramCmd()) init(SdramCmd.Nop)
//  val cs = Reg(Bool()) init(False)
//  val cs2 = Reg(Bool()) init(False)
//
//  val initstate = Reg(UInt(5 bits)) init(0x0)
//  val init_done = Reg(Bool()) init(False)
//
//  val ready2 = Reg(Bool()) init(False)
//
//  val state = Reg(UInt(3 bits)) init(0x0)
//  val wr = Reg(Bool()) init(False)
//  val rd = Reg(Bool()) init(False)
//  val wr2 = Reg(Bool()) init(False)
//  val done2 = Reg(Bool()) init(False)
//
//  val cas_addr = Reg(UInt(10 bits)) init(0x0)
//  val cas_addr2 = Reg(UInt(10 bits)) init(0x0)
//  val addr = Reg(UInt(24 bits)) init(0x0)
//  val addr2 = Reg(UInt(24 bits)) init(0x0)
//  val addr3 = Reg(UInt(24 bits)) init(0x0)
//  val rcnt = Reg(UInt(6 bits)) init(0x0)
//  val rnw_reg = Reg(Bool()) init(False)
//  val rfsh = Reg(UInt(2 bits)) init(0x0)
//  val rdat2 = Reg(UInt(16 bits)) init(0x0)
//  val rdat3 = Reg(UInt(16 bits)) init(0x0)
//  val done3 = Reg(Bool()) init(False)
//  val myIs = Reg(UInt(5 bits)) init(0x0)
//  val is2 = Reg(UInt(5 bits)) init(0x0)
//  val is3 = Reg(UInt(5 bits)) init(0x0)
//  val st = Reg(UInt(3 bits)) init(0x0)
//  val id = Reg(Bool()) init(False)
//  val cas_cmd = Reg(SdramCmd()) init(SdramCmd.Nop) //Reg(UInt(3 bits)) init(0x0)
//  val cas_cmd2 = Reg(SdramCmd()) init(SdramCmd.Nop) //Reg(UInt(3 bits)) init(0x0)
//  val wdat_req = Reg(Bool()) init(False)
//
//  //when (state === 5) {
//  //}
//  when (state === 5) {
//    when (
//      //~&initstate
//
//    // I looked it up online, and Verilog's `~&initstate` is the same as
//    // the below SpinalHDL code
//      ~initstate.andR 
//    ) {
//      initstate := initstate + U"4'd1"
//    } otherwise {
//      init_done := True
//    }
//  }
//
//  ready2 := False
//  when (wr) {
//    switch (state) {
//      is (3, 4, 5 ,6) {
//        ready2 := True
//      }
//    }
//  }
//  when (rd) {
//    switch (state) {
//      is (2, 3, 4, 5) {
//        ready2 := True
//      }
//    }
//  }
//
//  st    := st + U"1'd1"
//  state := st
//
//  //DRAM_DQ := 16'bZ;
//
//  wr := wr2
//  wdat_req := wr2 & ready2
//  when (wdat_req) {
//    //DRAM_DQ := wdat
//    io.sdram.dq := io.wdat
//  }
//
//  //rdat3 := DRAM_DQ
//  rdat3 := io.sdram.dq
//  rdat2 := rdat3
//  io.rdat := rdat2
//  cmd2 := SdramCmd.Nop //CMD_NOP
//
//  is3 := initstate
//  is2 := is3
//  myIs := is2
//  id := init_done
//
//  when (!id) {
//    cs2 := myIs(4)
//
//    when (state === 1) {
//      switch (myIs(3 downto 0)) {
//        is (2) {
//          sdaddr2(10) := True // all banks
//          cmd2 := SdramCmd.Precharge //CMD_PRECHARGE
//        }
//        is (4, 7) {
//          cmd2 := SdramCmd.AutoRefresh //CMD_AUTO_REFRESH
//        }
//        is (10, 13) {
//          cmd2 := SdramCmd.LoadMode //CMD_LOAD_MODE
//          sdaddr2 := U"13'b000_0_00_011_0_010" // WRITE BURST, LATENCY=3, BURST=4
//        }
//      }
//    }
//    wr2 := False
//    wr := False
//    rd := False
//    rcnt := 0x0
//    done2 := False
//  } elsewhen (done2) {
//    rd := False
//    wr2 := False
//    wr := False
//    when (io.chip === U"2'h2") {
//      addr := U"24'h800000"
//      addr2 := U"24'h800000"
//    } otherwise {
//      addr := 0
//      addr2 := 0
//    }
//    st := 0
//    done3 := False
//    when (io.start) {
//      done2 := False
//      rnw_reg := ~io.isWrite //True //rnw
//    }
//  } otherwise {
//    switch (state) {
//      is (0) {
//        rcnt := rcnt + U"1'd1"
//        when (rcnt === 50) {
//          rcnt := 0
//        }
//
//        rfsh := 0
//        when (rcnt >= 49) {
//          rfsh := Cat(List(U"1'b1", rcnt(0 downto 0)).reverse).asUInt
//        }
//        addr3 := addr
//      }
//
//      // RAS
//      is (1) {
//        cas_cmd2 := SdramCmd.Nop //CMD_NOP
//        wr2 := False //0
//        when (rfsh(1)) {
//          cmd2 := SdramCmd.AutoRefresh //CMD_AUTO_REFRESH
//          cs2 := rfsh(0)
//        } elsewhen (~done3) {
//          (
//            cs2,
//            cas_addr2(9),
//            cas_addr2(8 downto 2),
//            sdaddr2,
//            ba2,
//            cas_addr2(1 downto 0)
//          ) := Cat(List(addr3, U"2'b00").reverse)
//          wr2     := ~rnw_reg
//          //cas_cmd2:= rnw_reg ? CMD_READ : CMD_WRITE
//          //cmd2    := CMD_ACTIVE
//          when (rnw_reg) {
//            cas_cmd2 := SdramCmd.Read
//          } otherwise {
//            cas_cmd2 := SdramCmd.Write
//          }
//          cmd2 := SdramCmd.Active
//          addr2   := addr + U"1'd1"
//        }
//      }
//      
//      is (2) {
//        addr := addr2
//        cas_addr := cas_addr2
//        cas_cmd := cas_cmd2
//      }
//      // CAS
//      is (4) {
//        sdaddr2 := Cat(List(U"3'b001", cas_addr).reverse).asUInt // AUTO PRECHARGE
//        cmd2 := cas_cmd
//      }
//      
//      is (7) {
//          when (io.chip === 0 && io.sz === 3 && addr(23 downto 0).andR) {
//            done3 := True//1
//          }
//          when (io.chip === 1 && io.sz === 3 && addr(22 downto 0).andR) {
//            done3 := True //1
//          }
//          when (io.chip === 2 && io.sz === 3 && addr(23 downto 0).andR) {
//            done3 := True //1
//          }
//          when (io.sz === 2 && addr(22 downto 0).andR) {
//            done3 := True //1
//          }
//          when (io.sz <= 1 && addr(21 downto 0).andR) {
//            done3 := True //1
//          }
//          rd := (cas_cmd === SdramCmd.Read /*CMD_READ*/)
//          when (done3) {
//            done2 := True //1
//          }
//        }
//    }
//  }
//}

object LcvStallSdramCtrlConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    )
  )
}

object LcvStallSdramCtrlToVerilog extends App {
  LcvStallSdramCtrlConfig.spinal.generateVerilog{
    val top = LcvStallSdramCtrl(
      cfg=LcvStallSdramCtrlConfig(
        clkRate=(100.0 MHz)
      )
    )
    top
  }
}

//object LcvSdramWrapToVerilog extends App {
//  LcvStallSdramCtrlConfig.spinal.generateVerilog{
//    val top = LcvSdramWrap(
//      cfg=LcvStallSdramCtrlConfig(
//        clkRate=(167.0 MHz)
//      )
//    )
//    top
//  }
//}
