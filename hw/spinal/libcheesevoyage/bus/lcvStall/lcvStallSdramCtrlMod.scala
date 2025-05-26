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
  def rfshCntWidth = 14
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
  assert(cfg.clkRate >= (99.2 MHz))
  assert(cfg.clkRate <= (100.7 MHz))
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

  rDqTriState.read := io.sdram.dq
  when (rDqTriState.writeEnable) {
    //io.dq
    io.sdram.dq := rDqTriState.write
  }

  def rFullAddr = (
    rCasAddr(12 downto 9), io.sdram.ba, io.sdram.a, rCasAddr(8 downto 0)
  )

  //val nextState = State()
  //val rState = RegNext(nextState) init(State.Startup)
  //nextState := rState
  val rState = Reg(State()) init(State.Startup)
  //val rReq1 = Reg(Bool()) init(False)
  val rHaveValid = Reg(Vec.fill(2)(Bool())) //init(False)
  for (idx <- 0 until rHaveValid.size) {
    rHaveValid(idx).init(False)
  }
  //val rCh = Reg(UInt(2 bits)) init(0x0)
  //(rReq1, rRq) := B"2'b00"

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
          U"2'b00",
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
        clkRate=100.0 MHz
      )
    )
    top
  }
}
