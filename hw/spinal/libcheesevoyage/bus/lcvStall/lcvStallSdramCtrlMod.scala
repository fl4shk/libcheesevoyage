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
  //io.sdram.a.setAsReg() init(io.sdram.a.getZero)
  //io.sdram.ba.setAsReg() init(io.sdram.ba.getZero)
  val rSdramA = Reg(cloneOf(io.sdram.a)) init(io.sdram.a.getZero)
  val rSdramBa = Reg(cloneOf(io.sdram.ba)) init(io.sdram.ba.getZero)
  io.sdram.a := rSdramA
  io.sdram.ba := rSdramBa

  //--------
  object State extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      Startup,
      StartRdWr,
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
    rCasAddr(12 downto 9), rSdramBa, rSdramA, rCasAddr(8 downto 0)
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
        (cfg.casLatency._2 + cfg.burstLen + cfg.casLatency._3 + 2) bits
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
  (io.sdram.dqmh, io.sdram.dqml) := rSdramA(12 downto 11)

  rHaveValid.head := RegNext(io.lcvStall.nextValid) init(False)
  when (RegNext(io.lcvStall.nextValid) =/= rHaveValid.head) {
    rHaveValid.last := True
  }

  rRfshCnt := rRfshCnt + 1
  rDqTriState.writeEnable := False
  rDataReadyDelay := Cat(False, (rDataReadyDelay >> 1)).asUInt

  for (burstIdx <- 0 until cfg.burstLen) {
    when (rDataReadyDelay(cfg.burstLen - burstIdx)) {
      io.lcvStall.recvData.data(
        (
          (cfg.sdramDqWidth * (burstIdx + 1)) - 1
        ) downto (
          cfg.sdramDqWidth * burstIdx
        )
      ) := (
        RegNext(rDqTriState.read) init(rDqTriState.read.getZero)
      )
    }
  }
  io.lcvStall.ready := False
  when (rDataReadyDelay(1)) {
    io.lcvStall.ready := True
  }
  val mode = cfg.mode

  switch (rState) {
    is (State.Startup) {
      rSdramA := 0x0
      rSdramBa := 0x0
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
        rSdramA(10) := True // all banks
        rSdramBa := 0x0
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
        rSdramA := mode
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
      rSdramA := rCasAddr
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

      rSdramA(10) := True
      rSdramA(0) := True
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
        //  rSdramBa,
        //  rSdramA,
        //  rCasAddr(8 downto 0),
        //)
        rFullAddr := Cat(
          List(
            //U"2'b00",
            Mux[UInt](
              io.lcvStall.sendData.isWrite,
              ~io.lcvStall.sendData.byteEn(1 downto 0),
              U"2'b00",
            ),
            ~io.lcvStall.sendData.isWrite, 
            //io.lcvStall.sendData.addr(25 downto 1),
            io.lcvStall.sendData.addr(25 downto 2),
            U"1'b0"
          ).reverse
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

case class LcvSdramWrapIo(
  cfg: LcvStallSdramCtrlConfig,
) extends Bundle {
  val start = in(Bool())
  val done = out(Bool())
  val isWrite = in(Bool())
  val ready = out(Bool())
    // strobe. when writing, one means that data from wdat written to 
    // memory
    //
    // when reading, one means that data read from memroy is on rdat
    // output
  val wdat = in(UInt(16 bits)) // input, data to be written to memory
  val rdat = out(UInt(16 bits)) // output, data last read from memory 
  val sz = in(UInt(2 bits))
  val chip = in(UInt(2 bits))

  val sdram = LcvStallSdramIo(cfg=cfg)
}

case class LcvSdramWrap(
  cfg: LcvStallSdramCtrlConfig
) extends Component {
  val io = LcvSdramWrapIo(cfg=cfg)

  io.done.setAsReg() init(io.done.getZero)
  io.ready.setAsReg() init(io.ready.getZero)
  io.rdat.setAsReg() init(io.rdat.getZero)
  //io.sdram.dq.setAsReg() init(io.sdram.dq.getZero)

  val sdramCtrl = LcvStallSdramCtrl(cfg=cfg)
  io.sdram <> sdramCtrl.io.sdram

  def scLcvStall = sdramCtrl.io.lcvStall
  //scLcvStall.nextValid := True
  //scLcvStall.sendData.setAsReg() init(scLcvStall.sendData.getZero)
  val rSendData = {
    val temp = Reg(LcvStallSdramCtrlH2dPayload(cfg=cfg))
    temp.init(temp.getZero)
    temp
  }
  scLcvStall.sendData := rSendData
  io.ready := (
    RegNext(scLcvStall.nextValid, init=False)
    && scLcvStall.ready
  )
  when (!rSendData.addr(1)) {
    io.rdat := scLcvStall.recvData.data(15 downto 0)
  } otherwise {
    io.rdat := scLcvStall.recvData.data(31 downto 16)
  }
  def szUnset = U"2'b00"
  def sz32M = U"2'b01"
  def sz64M = U"2'b10"
  def sz128M = U"2'b11"

  //object State extends SpinalEnum(defaultEncoding=binarySequential) {
  //  val
  //    //Startup,
  //    Idle,
  //    Running
  //    = newElement()
  //}
  //val rState = Reg(State()) init(State.Idle)

  //switch (rState) {
  //  //is (State.Startup) {
  //  //}
  //  is (State.Idle) {
  //    io.done := False
  //  }
  //  is (State.Running) {
  //  }
  //}
  //val rDone3 = Reg(Bool()) init(False)
  val nextDone3 = Bool()
  val rDone3 = RegNext(nextDone3) init(nextDone3.getZero)
  nextDone3 := rDone3

  rSendData.byteEn := U(rSendData.byteEn.getWidth bits, default -> True)

  scLcvStall.nextValid := False
  when (
    io.start
    || io.done
  ) {
    io.done := False
    nextDone3 := False
    //rState := State.Idle
    rSendData.addr := 0x0
    rSendData.isWrite := io.isWrite
    //io.ready := False
  } otherwise {
    rSendData.isWrite := io.isWrite
    when (
      (io.chip === 0)
      && (io.sz === sz128M)
      && rSendData.addr(25 downto 2).andR
    ) {
      scLcvStall.nextValid := False
      nextDone3 := True
    }
    when (
      (io.chip === 1)
      && (io.sz === sz128M)
      && rSendData.addr(rSendData.addr.high - 1 downto 2).andR
    ) {
      scLcvStall.nextValid := False
      nextDone3 := True
    }
    when (
      (io.chip === 2)
      && (io.sz === sz128M)
      && rSendData.addr(rSendData.addr.high downto 2).andR
    ) {
      scLcvStall.nextValid := False
      nextDone3 := True
      //io.done := 
    }
    when (
      (io.sz === sz64M)
      && rSendData.addr(rSendData.addr.high - 1 downto 2).andR
    ) {
      scLcvStall.nextValid := False
      nextDone3 := True
    }
    when (
      (io.sz === sz32M)
      && rSendData.addr(rSendData.addr.high - 2 downto 2).andR
    ) {
      scLcvStall.nextValid := False
      nextDone3 := True
    }
    //when (nextDone3) {
    //  scLcvStall.nextValid := False
    //}
    when (rDone3) {
      scLcvStall.nextValid := False
      io.done := True
    } otherwise {
      scLcvStall.nextValid := True
      when (
        RegNext(scLcvStall.nextValid, init=False)
        && scLcvStall.ready
      ) {
        rSendData.data := io.wdat.resize(rSendData.data.getWidth)
        val myAddrRangeLo = 2
        val myAddrRange = rSendData.addr.high downto myAddrRangeLo
        rSendData.addr(myAddrRange) := rSendData.addr(myAddrRange) + 1
      }
    }
  }
  //val rAddr = Reg(UInt(cfg.addrWidth bits)) init(0x0)
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
        clkRate=(100.0 MHz)
      )
    )
    top
  }
}

object LcvSdramWrapToVerilog extends App {
  LcvStallSdramCtrlConfig.spinal.generateVerilog{
    val top = LcvSdramWrap(
      cfg=LcvStallSdramCtrlConfig(
        clkRate=(
          //167.0 MHz
          //120.0 MHz
          100.0 MHz
        )
      )
    )
    top
  }
}
