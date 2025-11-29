//
// sdram - Adapted/modified starting from SystemVerilog code in the
//  MiSTer FPGA project, which is...
// Copyright (c) 2015-2019 Sorgelig
//
// Some parts of SDRAM code used from project:
// http://hamsterworks.co.nz/mediawiki/index.php/Simple_SDRAM_Controller
//
// Finally, some information was obtained from 
// https://www.hackster.io/salvador-canas/a-practical-introduction-to-sdr-sdram-memories-using-an-fpga-8f5949
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

//object SdramCmd
//extends SpinalEnum(defaultEncoding=binarySequential) {
//  val
//    LoadMode,       // 3'b000
//    AutoRefresh,    // 3'b001
//    Precharge,      // 3'b010
//    Active,         // 3'b011
//    Write,          // 3'b100
//    Read,           // 3'b101
//    Bad,            // 3'b110
//    Nop             // 3'b111
//    = newElement()
//}

case class LcvStallBusSdramCtrlConfig(
  clkRate: HertzNumber,
  //burstLen: Int=2, // 32-bit
  useAltddioOut: Boolean=true,
) {
  //--------
  // idea for later: change these to *parameters* of
  // `LcvStallBusSdramCtrlConfig`
  def sdramDqWidth: Int = 16
  def sdramAWidth: Int = 13
  def sdramBaWidth: Int = 2
  def idleCntMax: Int = 5
  val busCfg = LcvStallBusConfig(
    mainCfg=LcvStallBusMainConfig(
      dataWidth=(
        32
      ),
      addrWidth=(
        //27
        32
      ),
      burstSizeWidth=(
        1
      ),
      srcWidth=(
        1
      ),
    ),
    mesiCfg=LcvStallBusMesiConfig(
      numCpus=1
    )
  )
  def addrWidth: Int = (
    //busCfg
    //27
    busCfg.mainCfg.addrWidth
  )
  def wordWidth: Int = (
    //16
    //32
    busCfg.dataWidth
  )
  val altddioOutCfg: AltddioOutConfig = AltddioOutConfig() 
  //--------

  //def burstLen = 4
  def burstLen = 2
  //def burstLen = 1
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
  //def noWriteBurst = U"1'b1"  // 0=write burst enable, 1=only single acc wr
  def doWriteBurst = U"1'b0"
  def mode = Cat(
    List(
      U"3'b000",
      //noWriteBurst,
      doWriteBurst,
      opMode,
      casLatency._1,
      accessType,
      burstCode,
    ).reverse
  ).asUInt

  ////def sdramStartupCyclesBase = (
  ////  (100.0 us) * clkRate
  ////)
  //def sdramStartupCycles = (
  //  //(sdramStartupCyclesBase + 1.21).toInt // 12100 cycles at 100 MHz
  //  (((100.0 us) * clkRate) + 1.21).toInt // 12100 cycles at 100 MHz
  //)
  //def cyclesPerRefresh = (
  //  //(((64 ms) * (64 MHz)) / 8192) - 1
  //  //(((64 ms) * clkRate) / 8192) - 1

  //  // 545.875.toInt = 545
  //  ((((64 ms) * (clkRate * 0.7)) / 8192) - 1).toInt
  //)
  ////print(
  ////  cyclesPerRefresh
  ////)
  //def rfshCntWidth = (
  //  14 max log2Up(sdramStartupCycles) max log2Up(cyclesPerRefresh)
  //)
  //print(s"rfshCntWidth:${rfshCntWidth}")
  //def startupRfshMax = (
  //  (1 << rfshCntWidth) - 1
  //)
  //def rfshCntInit = (
  //  startupRfshMax - sdramStartupCycles
  //)
  object Cycles {
    def tRC: (Option[BigDecimal], Option[BigDecimal]) = (
      // Row cycle time (same bank)
      Some((63 ns) * clkRate),
      None,
    )

    def tRFC: (Option[BigDecimal], Option[BigDecimal]) = (
      // Refresh cycle time
      Some((63 ns) * clkRate),
      None,
    )

    def tRCD: (Option[BigDecimal], Option[BigDecimal]) = (
      // RAS# to CAS# delay (same bank)
      Some((21 ns) * clkRate),
      None,
    )

    def tRP: (Option[BigDecimal], Option[BigDecimal]) = (
      // Precharge to refresh/row activate command (same bank)
      Some((21 ns) * clkRate),
      None,
    )

    def tRRD: (Option[BigDecimal], Option[BigDecimal]) = (
      // Row activate to row activate delay (different banks)
      Some((14 ns) * clkRate),
      None,
    )

    def tMRD: (Option[BigDecimal], Option[BigDecimal]) = (
      // Mode register set cycle time
      Some((14 ns) * clkRate),
      None,
    )

    def tRAS: (Option[BigDecimal], Option[BigDecimal]) = (
      // Row activate to precharge time (same bank)
      Some((42 ns) * clkRate),
      Some((120e3 ns) * clkRate),
    )

    def tWR: (Option[BigDecimal], Option[BigDecimal]) = (
      // Write recovery time
      Some((14 ns) * clkRate),
      None,
    )

    def tCK = (
      Array[(Option[BigDecimal], Option[BigDecimal])](
        // Clock cycle time
        (
          // CAS Latency == 2
          Some((10 ns) * clkRate),
          None,
        ),
        (
          // CAS Latency == 3
          Some((7 ns) * clkRate),
          None,
        ),
      )
    )

    def tCH: (Option[BigDecimal], Option[BigDecimal]) = (
      // Clock high time
      Some((2.5 ns) * clkRate),
      None,
    )

    def tCL: (Option[BigDecimal], Option[BigDecimal]) = (
      // Clock low time
      Some((2.5 ns) * clkRate),
      None,
    )

    def tAC = (
      Array[(Option[BigDecimal], Option[BigDecimal])](
        // Access time from CLK (positive edge)
        (
          // CAS Latency == 2
          Some((6 ns) * clkRate),
          None,
        ),
        (
          // CAS Latency == 3
          Some((5.4 ns) * clkRate),
          None,
        ),
      )
    )

    def tOH: (Option[BigDecimal], Option[BigDecimal]) = (
      // Data output hold timae
      Some((2.5 ns) * clkRate),
      None,
    )

    def tREFI: (Option[BigDecimal], Option[BigDecimal]) = (
      // Average refresh interval time
      Some((7.8 us) * clkRate),
      None,
    )
    def tREFIthresh = (
      (7 us) * clkRate
    )
  }

}


case class LcvStallBusSdramIo(
  cfg: LcvStallBusSdramCtrlConfig,
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

  private[libcheesevoyage] def doSetAsReg(
  ): Unit = {
    a.setAsReg() init(a.getZero)
    dqml.setAsReg() init(True)
    dqmh.setAsReg() init(True)
    ba.setAsReg() init(ba.getZero)
    nCs.setAsReg() init(False)        // \
    nWe.setAsReg() init(True)         // | start off with a NOP
    nRas.setAsReg() init(True)        // |
    nCas.setAsReg() init(True)        // / 
    cke.setAsReg() init(False)
    //clk.setAsReg() init(clk.getZero)
  }

  //noIoPrefix()
  private def _sendCmdBase(
    cmdBase: UInt
  ): Unit = {
    assert(
      cmdBase.getWidth == 4
    )
    nCs := cmdBase(3)
    nRas := cmdBase(2)
    nCas := cmdBase(1)
    nWe := cmdBase(0)
  }
  private[libcheesevoyage] def sendCmdInhibit(
  ): Unit = {
    nCs := True
  }
  private[libcheesevoyage] def sendCmdNop(): Unit = {
    this._sendCmdBase(U"4'b0111")
  }
  private[libcheesevoyage] def sendCmdBurstTerm(): Unit = {
    this._sendCmdBase(U"4'b0110")
  }
  private[libcheesevoyage] def sendCmdRead(
    bank: UInt,
    column: UInt,
    autoPrecharge: Bool,
    someDqTriState: TriState[UInt],
  ): Unit = {
    this._sendCmdBase(U"4'b0101")
    ba := bank
    a(9 downto 0) := column
    a(10) := autoPrecharge
    dqmh := False
    dqml := False
    someDqTriState.writeEnable := False
  }
  private[libcheesevoyage] def sendCmdWrite(
    bank: UInt,
    column: UInt,
    autoPrecharge: Bool,
    someDqTriState: TriState[UInt],
    wrData: UInt,
    wrByteEn: UInt,
    firstWrite: Boolean,
  ): Unit = {
    if (firstWrite) {
      this._sendCmdBase(U"4'b0100")
      ba := bank
      a(9 downto 0) := column
      a(10) := autoPrecharge
    } else {
      this.sendCmdNop()
    }
    dqmh := ~wrByteEn(1)
    dqml := ~wrByteEn(0)
    someDqTriState.writeEnable := True
    someDqTriState.write := wrData
  }
  private[libcheesevoyage] def sendCmdActive(
    bank: UInt,
    row: UInt,
  ): Unit = {
    this._sendCmdBase(U"4'b0011")
    ba := bank
    a := row
  }
  private[libcheesevoyage] def sendCmdPrecharge(
    bank: UInt,
    prechargeAll: Bool,
  ): Unit = {
    this._sendCmdBase(U"4'b0010")
    ba := bank
      // we can do this even when `prechargeAll === True`
      // because `ba` is ignored in that case
    a(10) := prechargeAll
  }
  private[libcheesevoyage] def sendCmdAutoRefresh(
  ): Unit = {
    this._sendCmdBase(U"4'b0001")
  }
  private[libcheesevoyage] def sendCmdLoadMode(
  ): Unit = {
    this._sendCmdBase(U"4'b0000")
    ba := ba.getZero
    a := cfg.mode
  }
}

case class LcvStallBusSdramCtrlIo(
  cfg: LcvStallBusSdramCtrlConfig,
) extends Bundle {
  val bus = slave(LcvStallBusIo(cfg=cfg.busCfg))
  val sdram = LcvStallBusSdramIo(cfg=cfg)
}
case class LcvStallBusSdramCtrl(
  cfg: LcvStallBusSdramCtrlConfig,
) extends Component {
  //--------
  val io = LcvStallBusSdramCtrlIo(cfg=cfg)
  io.bus.h2dBus.ready.setAsReg() init(False)
  io.bus.h2dBus.ready := False
  val rSavedH2dSendData = (
    Reg(cloneOf(io.bus.h2dBus.sendData))
    init(io.bus.h2dBus.sendData.getZero)
  )

  //def myD2hNextValid = (
  //  io.bus.d2hBus.nextValid
  //)
  //myD2hNextValid.setAsReg() init(myD2hNextValid.getZero)
  //def myD2hSendData = (
  //  io.bus.d2hBus.sendData
  //)
  //myD2hSendData.setAsReg() init(myD2hSendData.getZero)
  val rD2hValid = (
    Reg(cloneOf(io.bus.d2hBus.nextValid))
    init(io.bus.d2hBus.nextValid.getZero)
  )
  val rD2hSendData = (
    Reg(cloneOf(io.bus.d2hBus.sendData))
    init(io.bus.d2hBus.sendData.getZero)
  )
  io.bus.d2hBus.nextValid := rD2hValid
  io.bus.d2hBus.sendData := rD2hSendData

  //--------
  // Power up Sequence:
  //  1. Power must be applied to V_DD and V_DDQ (simultaneously) when
  //    A. `io.sdram.cke === False`
  //    B. `io.sdram.dqmh === True`
  //    C. `io.sdram.dqml === True`
  //    D. All input signals are held "NOP" state.
  //  2. Start clock and maintain stable condition for minimum 200 us,
  //    then bring `io.sdram.cke === True` and, it is recommended that
  //    `io.sdram.dqmh === True` and `io.sdram.dqml === True`
  //    to ensure that DQ output is in high impedance.
  //  3. All banks must be precharged.
  //    A. I should be able to do that with
  //      `io.sdram.sendCmdPrecharge(bank=dontcare, prechargeAll=True)`
  //  4. Mode Register Set command must be asserted to initialize the Mode
  //    register.
  //  5. A minimum of 2 Auto-Refresh dummy cycles must be required to
  //    stabilize the internal circuitry of the device.
  //    A. NOTE: The Auto Refresh command can be issued before or after the
  //      Mode Register Set command.
  //--------
  //--------
  //--------
  //  12. AutoRefresh command:
  //    A. The AutoRefresh command must be performed 
  //      8192 times within 64 ms.
  //    B. The time required to complete the auto refresh operation is
  //      specified by `tRC(min.)`
  //    C. To provide the AutoRefresh command, all banks need to be in the 
  //      idle state and the device must NOT be in power down mode
  //      (`io.sdram.cke === True`).
  //    D. This command must be followed by NOPs until the auto-refresh
  //      operation is completed.
  //    E. The precharge time requirement, `tRP(min)`, must be met before
  //      successive auto refresh operations are performed.
  //--------
  //  * `tCK`, Clock Cycle time (min.), is 7 ns for me
  //--------
  //  * at 100 MHz, we have 100e6 cycles / s
  //    * then we have (x e-9 s) * (100e6 cycles / s) = No. of cycles we
  //    need to wait for given a time constant
  //--------

  io.sdram.doSetAsReg()

  //val myDqTriState = (
  //  TriState(UInt(cfg.sdramDqWidth bits))
  //)
  val rDqTriState = {
    val temp = Reg(TriState(UInt(cfg.sdramDqWidth bits)))
    temp.init(temp.getZero)
    temp
  }
  //rDqTriState.writeEnable.setAsReg() init(rDqTriState.writeEnable.getZero)
  rDqTriState.writeEnable := (
    False
  )
  rDqTriState.read := io.sdram.dq
  when (rDqTriState.writeEnable) {
    //io.dq
    io.sdram.dq := rDqTriState.write
  }
  def myPwrOnCntNumCycles = (
    ((200 us) * cfg.clkRate) + 10 // 10 extra cycles for good measure 
  )
  def myPwrOnCntNumCyclesHalf = (
    (myPwrOnCntNumCycles / 2).toInt
  )
  val rPwrOnInitCnt = (
    Vec.fill(2)(
      Reg(UInt((log2Up(myPwrOnCntNumCyclesHalf.toInt) + 3) bits))
      init(myPwrOnCntNumCyclesHalf.toInt)
    )
  )
  val rPwrOnRpCnt = (
    //Vec.fill(2)(
      Reg(UInt((log2Up(cfg.Cycles.tRP._1.get.toInt) + 3) bits))
      init(cfg.Cycles.tRP._1.get.toInt)
    //)
  )
  val rPwrOnRfcCnt = (
    Vec.fill(2)(
      Reg(UInt((log2Up(cfg.Cycles.tRFC._1.get.toInt) + 3) bits))
      init(cfg.Cycles.tRFC._1.get.toInt)
    )
  )
  val rPwrOnMrdCnt = (
    Reg(UInt((log2Up(cfg.Cycles.tMRD._1.get.toInt) + 3) bits))
    init(cfg.Cycles.tMRD._1.get.toInt)
  )
  //--------
  val rNeedRfshCnt = (
    Reg(SInt((log2Up(cfg.Cycles.tREFIthresh.toInt) + 3) bits))
    init(
      //cfg.Cycles.tREFIthresh.toInt
      -1
    )
  )
  when (!rNeedRfshCnt.msb) {
    rNeedRfshCnt := rNeedRfshCnt - 1
  }
  val rRfshNopWaitCnt = (
    Reg(UInt((log2Up(cfg.Cycles.tRFC._1.get.toInt) + 3) bits))
    init(cfg.Cycles.tRFC._1.get.toInt)
  )
  val rActiveNopWaitCnt = (
    Reg(UInt((log2Up(cfg.Cycles.tRCD._1.get.toInt) + 3) bits))
    init(cfg.Cycles.tRCD._1.get.toInt)
  )
  def myRdNopWaitCntNumCycles = (
    // this is with auto-precharge
    (
      (cfg.burstLen /*- 1*/)
      + cfg.Cycles.tRP._1.get
    ).toInt + 1
  )
  val rRdNopWaitCnt = (
    Reg(UInt((log2Up(myRdNopWaitCntNumCycles.toInt) + 3) bits))
    init(myRdNopWaitCntNumCycles.toInt)
  )
  def myWrNopWaitCntNumCycles = (
    // this is with auto-precharge
    (
      (cfg.burstLen - 1)
      + cfg.Cycles.tWR._1.get
      + cfg.Cycles.tRP._1.get
    ).toInt + 1
  )
  val rWrNopWaitCnt = (
    Reg(UInt((log2Up(myWrNopWaitCntNumCycles.toInt) + 3) bits))
    init(myWrNopWaitCntNumCycles.toInt)
  )

  def myRdCasLatencyCntNumCycles = (
    cfg.casLatency._2
    + cfg.burstLen
    + 1
    //+ 1
  )
  val rRdCasLatencyCnt = (
    Reg(SInt((log2Up(myRdCasLatencyCntNumCycles) + 3) bits))
    init(
      myRdCasLatencyCntNumCycles - 1
      //cfg.casLatency._2 - 1
      //- 1
    )
  )

  object State extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      PWR_ON_INIT,
      PWR_ON_CNT_DO_CKE_HI,

      PWR_ON_SEND_PRECHARGE_ALL,
      PWR_ON_WAIT_T_RP, // send NOPs

      PWR_ON_SEND_AUTO_REFRESH_0,
      PWR_ON_WAIT_T_RFC_0, // send NOPs or CMD INHIBITs

      PWR_ON_SEND_AUTO_REFRESH_1,
      PWR_ON_WAIT_T_RFC_1, // send NOPs or CMD INHIBITs

      PWR_ON_LOAD_MODE_REGISTER,
      PWR_ON_WAIT_T_MRD,   // send NOPs

      IDLE,
      SEND_RFSH,
      RFSH_POST_NOPS,
      SEND_ACTIVE,
      ACTIVE_POST_NOPS,
      PRE_READ_WRITE,
      SEND_READ,
      READ_POST_NOPS,
      SEND_WRITE_0,
      SEND_WRITE_1,
      WRITE_POST_NOPS

      = newElement();
  }
  val rState = (
    Reg(State())
    init(State.PWR_ON_INIT)
  )
  switch (rState) {
    is (State.PWR_ON_INIT) {
      io.sdram.sendCmdNop()
      when (!rPwrOnInitCnt(0).msb) {
        rPwrOnInitCnt(0) := rPwrOnInitCnt(0) - 1
        io.sdram.cke := False
        io.sdram.dqml := True
        io.sdram.dqmh := True
      } otherwise {
        rState := State.PWR_ON_CNT_DO_CKE_HI
      }
    }
    is (State.PWR_ON_CNT_DO_CKE_HI) {
      io.sdram.sendCmdNop()
      when (!rPwrOnInitCnt(1).msb) {
        rPwrOnInitCnt(1) := rPwrOnInitCnt(1) - 1
        io.sdram.cke := True
        io.sdram.dqml := True
        io.sdram.dqmh := True
      } otherwise {
        rState := (
          State.PWR_ON_SEND_PRECHARGE_ALL
        )
      }
    }

    is (State.PWR_ON_SEND_PRECHARGE_ALL) {
      io.sdram.sendCmdPrecharge(
        bank=io.sdram.ba.getZero,
        prechargeAll=True
      )
      rState := State.PWR_ON_WAIT_T_RP
    }
    is (State.PWR_ON_WAIT_T_RP) { // send NOPs
      io.sdram.sendCmdNop()
      when (!rPwrOnRpCnt.msb) {
        rPwrOnRpCnt := rPwrOnRpCnt - 1
      } otherwise {
        rState := State.PWR_ON_SEND_AUTO_REFRESH_0
      }
    }

    is (State.PWR_ON_SEND_AUTO_REFRESH_0) {
      io.sdram.sendCmdAutoRefresh()
      rState := State.PWR_ON_WAIT_T_RFC_0
    }
    is (State.PWR_ON_WAIT_T_RFC_0) { // send NOPs or CMD INHIBITs
      io.sdram.sendCmdNop()
      when (!rPwrOnRfcCnt(0).msb) {
        rPwrOnRfcCnt(0) := rPwrOnRfcCnt(0) - 1
      } otherwise {
        rState := State.PWR_ON_SEND_AUTO_REFRESH_1
      }
    }

    is (State.PWR_ON_SEND_AUTO_REFRESH_1) {
      io.sdram.sendCmdAutoRefresh()
      rState := State.PWR_ON_WAIT_T_RFC_1
    }
    is (State.PWR_ON_WAIT_T_RFC_1) { // send NOPs or CMD INHIBITs
      io.sdram.sendCmdNop()
      when (!rPwrOnRfcCnt(1).msb) {
        rPwrOnRfcCnt(1) := rPwrOnRfcCnt(1) - 1
      } otherwise {
        rState := State.PWR_ON_LOAD_MODE_REGISTER
      }
    }

    is (State.PWR_ON_LOAD_MODE_REGISTER) {
      io.sdram.sendCmdLoadMode()
      rState := State.PWR_ON_WAIT_T_MRD
    }
    is (State.PWR_ON_WAIT_T_MRD) {   // send NOPs
      io.sdram.sendCmdNop()
      when (!rPwrOnMrdCnt.msb) {
        rPwrOnMrdCnt := rPwrOnMrdCnt - 1
      } otherwise {
        rState := State.IDLE
        rNeedRfshCnt := cfg.Cycles.tREFIthresh.toInt
      }
    }

    is (State.IDLE) {
      when (rNeedRfshCnt.msb) {
        rNeedRfshCnt := cfg.Cycles.tREFIthresh.toInt
        rState := State.SEND_RFSH
      } elsewhen (
        RegNext(
          next=io.bus.h2dBus.nextValid,
          init=io.bus.h2dBus.nextValid.getZero,
        )
      ) {
        io.bus.h2dBus.ready := True
        rSavedH2dSendData := io.bus.h2dBus.sendData
        rState := State.SEND_ACTIVE
      }
    }
    is (State.SEND_RFSH) {
      io.sdram.sendCmdAutoRefresh()
      rState := State.RFSH_POST_NOPS
    }
    is (State.RFSH_POST_NOPS) {
      io.sdram.sendCmdNop()
      when (!rRfshNopWaitCnt.msb) {
        rRfshNopWaitCnt := rRfshNopWaitCnt - 1
      } otherwise {
        rState := State.IDLE
        rRfshNopWaitCnt := cfg.Cycles.tRFC._1.get.toInt
      }
    }
    is (State.SEND_ACTIVE) {
      io.sdram.sendCmdActive(
        bank=rSavedH2dSendData.addr(25 downto 24),
        row=rSavedH2dSendData.addr(23 downto 11),
      )
      rState := State.ACTIVE_POST_NOPS
    }
    is (State.ACTIVE_POST_NOPS) {
      io.sdram.sendCmdNop()
      when (!rActiveNopWaitCnt.msb) {
        rActiveNopWaitCnt := rActiveNopWaitCnt - 1
      } otherwise {
        rActiveNopWaitCnt := cfg.Cycles.tRCD._1.get.toInt
        rState := State.PRE_READ_WRITE
      }
    }
    is (State.PRE_READ_WRITE) {
      io.sdram.sendCmdNop()
      when (!rSavedH2dSendData.isWrite) {
        rState := State.SEND_READ
      } otherwise {
        rState := State.SEND_WRITE_0
      }
    }
    is (State.SEND_READ) {
      io.sdram.sendCmdRead(
        bank=rSavedH2dSendData.addr(25 downto 24),
        column=rSavedH2dSendData.addr(10 downto 1),
        autoPrecharge=True,
        someDqTriState=rDqTriState,
      )
      rState := State.READ_POST_NOPS
      rRdCasLatencyCnt := (
        //cfg.casLatency._2 - 1
        myRdCasLatencyCntNumCycles - 1
      )
      rRdNopWaitCnt := myRdNopWaitCntNumCycles
      //when (!rRdCasLatencyCnt.msb) {
      //  rRdCasLatencyCnt := rRdCasLatencyCnt - 1
      //  when (rRdCasLatencyCnt === 0) {
      //    rD2hSendData.data(31 downto 16) := rDqTriState.read
      //  } elsewhen (rRdCasLatencyCnt === 1) {
      //    rD2hSendData.data(15 downto 0) := rDqTriState.read
      //  }
      //  //switch (rRdCasLatencyCnt.lsb) {
      //  //  is (False) {
      //  //    rD2hSendData.data(15 downto 0) := rDqTriState.read
      //  //  }
      //  //  is (True) {
      //  //    rD2hSendData.data(31 downto 16) := rDqTriState.read
      //  //  }
      //  //}
      //} otherwise {
      //  rState := State.READ_POST_NOPS
      //}
    }
    is (State.READ_POST_NOPS) {
      io.sdram.sendCmdNop()
      when (!rRdCasLatencyCnt.msb) {
        rRdCasLatencyCnt := rRdCasLatencyCnt - 1
        when (rRdCasLatencyCnt === 0) {
          rD2hSendData.data(31 downto 16) := rDqTriState.read
        } elsewhen (rRdCasLatencyCnt === 1) {
          rD2hSendData.data(15 downto 0) := rDqTriState.read
        }
        //switch (rRdCasLatencyCnt.lsb) {
        //  is (False) {
        //    rD2hSendData.data(15 downto 0) := rDqTriState.read
        //  }
        //  is (True) {
        //    rD2hSendData.data(31 downto 16) := rDqTriState.read
        //  }
        //}
      }
      when (!rRdNopWaitCnt.msb) {
        rRdNopWaitCnt := rRdNopWaitCnt - 1
      }
      when (
        rRdCasLatencyCnt.msb 
        && rRdNopWaitCnt.msb
      ) {
        //rRdCasLatencyCnt := myRdCasLatencyCntNumCycles - 1
        rState := State.IDLE
        rD2hValid := True
      }
    }
    is (State.SEND_WRITE_0) {
      io.sdram.sendCmdWrite(
        bank=rSavedH2dSendData.addr(25 downto 24),
        column=rSavedH2dSendData.addr(10 downto 1),
        autoPrecharge=True,
        someDqTriState=rDqTriState,
        wrData=rSavedH2dSendData.data(15 downto 0),
        wrByteEn=rSavedH2dSendData.byteEn(1 downto 0),
        firstWrite=true,
      )
      rWrNopWaitCnt := myWrNopWaitCntNumCycles
      rState := State.SEND_WRITE_1
    }
    is (State.SEND_WRITE_1) {
      io.sdram.sendCmdWrite(
        bank=rSavedH2dSendData.addr(25 downto 24),
        column=rSavedH2dSendData.addr(10 downto 1),
        autoPrecharge=True,
        someDqTriState=rDqTriState,
        wrData=rSavedH2dSendData.data(31 downto 16),
        wrByteEn=rSavedH2dSendData.byteEn(3 downto 2),
        firstWrite=true,
      )
      rState := State.WRITE_POST_NOPS
      rD2hValid := True
    }
    is (State.WRITE_POST_NOPS) {
      io.sdram.sendCmdNop()
      when (!rWrNopWaitCnt.msb) {
        rWrNopWaitCnt := rWrNopWaitCnt - 1
      } otherwise {
        rState := State.IDLE
      }
    }
  }
  //elsewhen () {
  //}
  //otherwise {
  //  rD2hValid := True
  //}

  when (
    rD2hValid
    && io.bus.d2hBus.ready
  ) {
    rD2hValid := False
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
  val sdramclkDdr = (cfg.useAltddioOut) generate (
    altddio_out(
      cfg=cfg.altddioOutCfg
    )
  )
  if (cfg.useAltddioOut) {
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
  } else {
    io.sdram.clk := ClockDomain.current.readClockWire
  }
  //--------
}
case class as4c32m16sb(
) extends BlackBox {
  val io = new Bundle {
    val DQ = inout(Analog(UInt(16 bits))) // 16 bit bidirectional data bus
	  val A = in(UInt(13 bits)) // 13 bit multiplexed address bus
    val DQML = in(Bool())     // byte mask
    val DQMH = in(Bool())     // byte mask
    val BA = in(UInt(2 bits)) // two banks
    val nCS = in(Bool()) // a single chip select
    val nWE = in(Bool()) // write enable
    val nRAS = in(Bool())// row address select
    val nCAS = in(Bool()) // columns address select
    val CLK = in(Bool())
    val CKE = in(Bool())
  }
  noIoPrefix()
  addRTLPath("./hw/verilog/as4c32m16sb.sv")
}

//case class LcvSdramCtrlSimDutIo(
//  clkRate: HertzNumber,
//) extends Bundle {
//}
case class LcvSdramCtrlSimDut(
  clkRate: HertzNumber,
  useAltddioOut: Boolean,
) extends Component {
  //--------
  //val io = LcvSdramCtrlSimDutIo(clkRate=clkRate)
  val cfg = LcvStallBusSdramCtrlConfig(
    clkRate=clkRate,
    useAltddioOut=useAltddioOut,
  )
  val io = LcvStallBusSdramIo(cfg=cfg)
  //--------
  val mySdramCtrl = LcvStallBusSdramCtrl(cfg=cfg)
  io <> mySdramCtrl.io.sdram
  val rH2dValid = Reg(Bool(), init=False)
  val rH2dSendData = {
    val temp = Reg(cloneOf(mySdramCtrl.io.bus.h2dBus.sendData))
    temp.init(temp.getZero)
    temp
  }
  //val rD2hReady = Reg(Bool(), init=False)
  val myD2hReady = Bool()
  myD2hReady := False
  mySdramCtrl.io.bus.h2dBus.nextValid := rH2dValid
  mySdramCtrl.io.bus.h2dBus.sendData := rH2dSendData
  mySdramCtrl.io.bus.d2hBus.ready := myD2hReady //rD2hReady

  //val rD2hSendData = {
  //  val temp = Reg(cloneOf(mySdramCtrl.io.bus.d2hBus.sendData))
  //  temp.init(temp.getZero)
  //  temp
  //}

  //--------
  object State extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      WRITE_START,
      WRITE_WAIT_TXN,
      READ_START,
      READ_WAIT_TXN,
      DO_COMPARE_TEST_DATA
      = newElement();
  }
  val rState = (
    Reg(State())
    init(State.WRITE_START)
  )

  //val rCnt = (
  //  Reg(UInt((mySdram.io.DQ.getWidth + 1) bits))
  //  init(0x0)
  //)
  //val rHadFirstTxn = (
  //  Reg(Bool(), init=False)
  //)
  val rHadH2dFire = (
    Reg(Bool(), init=False)
  )
  val rHadD2hFire = (
    Reg(Bool(), init=False)
  )

  val myPrngArea = new Area {
    // credit:
    // http://www.retroprogramming.com/2017/07/xorshift-pseudorandom-numbers-in-z80.html
    def myXsWidth = (
      32
    )
    //def myCntWidth = 3
    def myCntMax = 2
    def myCntWidth = log2Up(myCntMax + 1) + 1
    val rCnt = (
      Reg(SInt(myCntWidth bits))
      init(myCntMax)
    )
    val rXsVec = (
      Vec.fill(2)(
        Vec.fill(3)(
          Reg(UInt(myXsWidth bits))
          //init(1)
        )
      )
    )
    for (idx <- 0 until rXsVec.size) {
      rXsVec(idx).foreach(item => item.init(idx + 1))
    }
    rXsVec.foreach(rXs => {
      rXs(2) := (
        (rXs(0) ^ ((rXs(0) << 7)(rXs(0).bitsRange))).resize(myXsWidth)
      )
      rXs(1) := (
        (rXs(2) ^ (rXs(2) >> 9).resize(myXsWidth)).resize(myXsWidth)
      )
      when (!rCnt.msb) {
        rCnt := rCnt - 1
      } otherwise {
        rXs(0) := (
          (rXs(1) ^ ((rXs(1) << 8)(rXs(0).bitsRange))).resize(myXsWidth)
        )
        rCnt := myCntMax
      }
    })
    def getCurrRand(): UInt = {
      //val a = (rXs ^ ((rXs << 7)(rXs.bitsRange))).resize(rXs.getWidth)
      //val b = (a ^ (a >> 9).resize(rXs.getWidth)).resize(rXs.getWidth)
      //val c = (b ^ ((b << 8)(rXs.bitsRange))).resize(rXs.getWidth)
      //rXs := c
      //rXs(0) := r
      //c(15 downto 0)
      Cat(
        rXsVec.head(0)(15 downto 0),
        rXsVec.last(0)(15 downto 0),
      ).asUInt
    }
  }
  def myMaxAddr = (
    //8
    //0x10
    8 << 2
  )
  val rTestData = (
    Vec.fill(
      //(myMaxAddr >> 2) + 1
      (myMaxAddr >> 1) // intentionally leave some always-zero elements
    )({
      val temp = Reg(Flow(
        Vec.fill(2)(
          UInt(myPrngArea.myXsWidth bits)
        )
      ))
      temp.init(temp.getZero)
      KeepAttribute(temp)
    })
  )
  val tempCnt = (
    (rH2dSendData.addr(rH2dSendData.addr.high downto 2))(
      log2Up(rTestData.size) - 1 downto 0
    )
  )

  switch (rState) {
    is (State.WRITE_START) {
      //--------
      rHadH2dFire := False
      rHadD2hFire := False
      //--------
      rH2dValid := True
      //when (rHadFirstTxn) {
      //  rH2dSendData.addr := rH2dSendData.addr + 1
      //}
      //rH2dSendData.data(31 downto 16) := myPrngArea.rXs(0)(31 downto 16)
      //rH2dSendData.data(15 downto 0) := myPrngArea.rand()
      rH2dSendData.data := myPrngArea.getCurrRand()
      //rTestData(tempCnt).valid := False
      rTestData(tempCnt).payload.head := (
        myPrngArea.getCurrRand()
      )
      rTestData(tempCnt).payload.last := (
        U(s"${myPrngArea.myXsWidth}'d0")
      )
      rH2dSendData.byteEn := U(
        rH2dSendData.byteEn.getWidth bits, default -> True
      )
      rH2dSendData.burstSize := 1
      rH2dSendData.isWrite := True
      rH2dSendData.src := 0x0
      //--------
      rState := State.WRITE_WAIT_TXN
      //--------
    }
    is (State.WRITE_WAIT_TXN) {
      //rHadFirstTxn := True
      when (mySdramCtrl.io.bus.h2dBus.fire) {
        rH2dValid := False
        rHadH2dFire := True
      }
      when (mySdramCtrl.io.bus.d2hBus.rValid) {
        rHadD2hFire := True
        myD2hReady := True
      }
      when (rHadH2dFire && rHadD2hFire) {
        when (rH2dSendData.addr < myMaxAddr) {
          rState := State.WRITE_START
          rH2dSendData.addr := rH2dSendData.addr + 4
        } otherwise {
          rState := State.READ_START
          rH2dSendData.addr := 0x0
        }
      }
    }
    is (State.READ_START) {
      //--------
      rHadH2dFire := False
      rHadD2hFire := False
      //--------
      rH2dValid := True
      rH2dSendData.isWrite := False
      //--------
      rState := State.READ_WAIT_TXN
      //--------
    }
    is (State.READ_WAIT_TXN) {
      when (mySdramCtrl.io.bus.h2dBus.fire) {
        rH2dValid := False
        rHadH2dFire := True
      }
      when (mySdramCtrl.io.bus.d2hBus.rValid) {
        rHadD2hFire := True
        myD2hReady := True
      }
      when (rHadH2dFire && rHadD2hFire) {
        when (rH2dSendData.addr < myMaxAddr) {
          rState := State.READ_START
          rH2dSendData.addr := rH2dSendData.addr + 4
        } otherwise {
          //rState := State.WRITE_START
          rState := State.DO_COMPARE_TEST_DATA
          rH2dSendData.addr := 0x0
        }
      }
      rTestData(tempCnt).payload.last := (
        mySdramCtrl.io.bus.d2hBus.sendData.data
      )
    }
    is (State.DO_COMPARE_TEST_DATA) {
      when (rH2dSendData.addr < myMaxAddr) {
        rH2dSendData.addr := rH2dSendData.addr + 4
      } otherwise {
        rH2dSendData.addr := 0x0
        rState := State.WRITE_START
      }
      rTestData(tempCnt).valid := (
        rTestData(tempCnt).payload.head
        === rTestData(tempCnt).payload.last
      )
    }
  }
}
case class LcvSdramSimDut(
  clkRate: HertzNumber,
) extends Component {
  //--------
  val io = new Bundle {
  }
  //--------
  val mySdramCtrlSimDut = LcvSdramCtrlSimDut(
    clkRate=clkRate,
    useAltddioOut=false,
  )
  //--------
  val mySdram = as4c32m16sb()
  //mySdram.io.DQ <> mySdramCtrl.io.sdram.dq
  //mySdram.io.A := mySdramCtrl.io.sdram.a
  //mySdram.io.DQML := mySdramCtrl.io.sdram.dqml
  //mySdram.io.DQMH := mySdramCtrl.io.sdram.dqmh
  //mySdram.io.BA := mySdramCtrl.io.sdram.ba
  //mySdram.io.nCS := mySdramCtrl.io.sdram.nCs
  //mySdram.io.nWE := mySdramCtrl.io.sdram.nWe
  //mySdram.io.nRAS := mySdramCtrl.io.sdram.nRas
  //mySdram.io.nCAS := mySdramCtrl.io.sdram.nCas
  //mySdram.io.CLK := mySdramCtrl.io.sdram.clk
  //mySdram.io.CKE := mySdramCtrl.io.sdram.cke
  mySdram.io.DQ <> mySdramCtrlSimDut.io.dq
  mySdram.io.A := mySdramCtrlSimDut.io.a
  mySdram.io.DQML := mySdramCtrlSimDut.io.dqml
  mySdram.io.DQMH := mySdramCtrlSimDut.io.dqmh
  mySdram.io.BA := mySdramCtrlSimDut.io.ba
  mySdram.io.nCS := mySdramCtrlSimDut.io.nCs
  mySdram.io.nWE := mySdramCtrlSimDut.io.nWe
  mySdram.io.nRAS := mySdramCtrlSimDut.io.nRas
  mySdram.io.nCAS := mySdramCtrlSimDut.io.nCas
  mySdram.io.CLK := mySdramCtrlSimDut.io.clk
  mySdram.io.CKE := mySdramCtrlSimDut.io.cke
  //--------
}

object LcvSdramSim extends App {
  def clkRate = 100.0 MHz
  val simSpinalConfig = SpinalConfig(
    defaultClockDomainFrequency=FixedFrequency(clkRate)
  )
  SimConfig
    .withConfig(config=simSpinalConfig)
    .withFstWave
    .compile(
      LcvSdramSimDut(clkRate=clkRate)
    )
    .doSim { dut =>
      dut.clockDomain.forkStimulus(period=10)
      def simNumClks = (
        200000
      )
      for (idx <- 0 until simNumClks) {
        dut.clockDomain.waitRisingEdge()
      }
      simSuccess()
    }
}
//case class LcvStallBusSdramCtrlH2dPayload(
//  cfg: LcvStallBusSdramCtrlConfig,
//) extends Bundle {
//  val isWrite = Bool()
//  val addr = UInt(cfg.addrWidth bits)
//  val data = UInt(cfg.wordWidth bits)
//  val byteEn = UInt(((cfg.wordWidth / 8).toInt) bits)
//}
//case class LcvStallBusSdramCtrlD2hPayload(
//  cfg: LcvStallBusSdramCtrlConfig,
//) extends Bundle {
//  val data = UInt(cfg.wordWidth bits)
//}

//case class LcvStallBusSdramCtrlIo(
//  cfg: LcvStallBusSdramCtrlConfig,
//) extends Bundle /*with IMasterSlave*/ {
//  val lcvStall = slave(new LcvStallIo[
//    LcvStallBusH2dSendPayload,
//    LcvStallBusD2hSendPayload,
//  ](
//    sendPayloadType=Some(LcvStallBusSendPayload(cfg=cfg.busCfg)),
//    recvPayloadType=Some(LcvStallBusD2hSendPayload(cfg=cfg.busCfg)),
//  ))
//  //val doRefresh = in(Bool())
//  val sdram = LcvStallBusSdramIo(cfg=cfg)
//  //def asMaster(): Unit = {
//  //  master(lcvStall)
//  //}
//}
//
////case class LcvStallBusSdramCtrl(
////  cfg: LcvStallBusSdramCtrlConfig,
////) extends Component {
////  assert(cfg.clkRate >= (99.2 MHz))
////  assert(cfg.clkRate <= (100.7 MHz))
////  //--------
////  val io = LcvStallBusSdramCtrlIo(cfg=cfg)
////
////  io.lcvStall.ready.setAsReg() init(io.lcvStall.ready.getZero)
////    // copy the MiSTer SDRAM controllers for the above ^
////
////  io.lcvStall.recvData.setAsReg() init(io.lcvStall.recvData.getZero)
////  //io.sdram.dq.setAsReg()
////    // I'm not sure how SpinalHDL deals with `inout`
////    // registers, so see the set of signals called `rDqTriState` instead.
////  //io.sdram.a.setAsReg() init(io.sdram.a.getZero)
////  //io.sdram.ba.setAsReg() init(io.sdram.ba.getZero)
////  //val rSdramA = Reg(cloneOf(io.sdram.a)) init(io.sdram.a.getZero)
////  //val rSdramBa = Reg(cloneOf(io.sdram.ba)) init(io.sdram.ba.getZero)
////  val rSdramA = Vec.fill(2)(
////    Reg(cloneOf(io.sdram.a))
////    init(io.sdram.a.getZero)
////  )
////  val rSdramBa = Vec.fill(2)(
////    Reg(cloneOf(io.sdram.ba))
////    init(io.sdram.ba.getZero)
////  )
////  io.sdram.a := rSdramA.last
////  io.sdram.ba := rSdramBa.last
////  rSdramA.last := rSdramA.head
////  rSdramBa.last := rSdramBa.head
////
////  //--------
////  object State extends SpinalEnum(defaultEncoding=binaryOneHot) {
////    val
////      Startup,
////      //StartRdWr,
////      WaitRdWr,
////      DoRdWr0,
////      DoWr1,
////      WaitIdleCnt,
////      Idle,
////      Rfsh
////      = newElement()
////  }
////
////  //val rSavedWr = Reg(Bool()) init(False)
////  val rSavedH2d = Reg(
////    //LcvStallBusSdramCtrlH2dPayload(cfg=cfg)
////    LcvStallBusSendPayload(cfg=cfg.busCfg)
////  )
////  rSavedH2d.init(rSavedH2d.getZero)
////
////  val rCasAddr = Reg(UInt(cfg.sdramAWidth bits)) init(0x0)
////
////  val rDqTriState = {
////    val temp = Reg(TriState(UInt(cfg.sdramDqWidth bits)))
////    temp.init(temp.getZero)
////    temp
////  }
////  rDqTriState.writeEnable.setAsReg() init(rDqTriState.writeEnable.getZero)
////
////  rDqTriState.read := io.sdram.dq
////  when (rDqTriState.writeEnable) {
////    //io.dq
////    io.sdram.dq := rDqTriState.write
////  }
////
////  def rFullAddr = (
////    rCasAddr(12 downto 9),
////    rSdramBa.head,
////    rSdramA.head,
////    rCasAddr(8 downto 0)
////  )
////
////  val rState = Reg(State()) init(State.Startup)
////  val rHaveValid = Reg(Vec.fill(2)(Bool())) //init(False)
////  for (idx <- 0 until rHaveValid.size) {
////    rHaveValid(idx).init(False)
////  }
////
////  val rRfshCnt = (
////    Reg(UInt(cfg.rfshCntWidth bits))
////    init(cfg.rfshCntInit)
////  )
////  //val temp = UInt(3 bits)
////  val rIdleCnt = (
////    Reg(SInt(log2Up(cfg.idleCntMax + 1) + 1 bits))
////    init(cfg.idleCntMax)
////  )
////  val rCmd = Reg(SdramCmd()) init(SdramCmd.Nop)
////  val rChip = Reg(Bool()) init(False)
////  val rDataReadyDelay = (
////    Reg(
////      UInt(
////        (cfg.casLatency._2 + cfg.burstLen + cfg.casLatency._3 + 2) bits
////      )
////    )
////    init(0x0)
////  )
////  //--------
////  io.sdram.nCs := rChip
////  io.sdram.nRas := rCmd.asBits(2)
////  io.sdram.nCas := rCmd.asBits(1)
////  io.sdram.nWe := rCmd.asBits(0)
////  io.sdram.cke := True
////  (io.sdram.dqmh, io.sdram.dqml) := rSdramA.last(12 downto 11)
////
////  rHaveValid.head := RegNext(io.lcvStall.nextValid) init(False)
////  when (RegNext(io.lcvStall.nextValid) =/= rHaveValid.head) {
////    rHaveValid.last := True
////  }
////
////  rRfshCnt := rRfshCnt + 1
////  rDqTriState.writeEnable := False
////  rDataReadyDelay := Cat(False, (rDataReadyDelay >> 1)).asUInt
////
////  for (burstIdx <- 0 until cfg.burstLen) {
////    when (rDataReadyDelay(cfg.burstLen - burstIdx)) {
////      io.lcvStall.recvData.data(
////        (
////          (cfg.sdramDqWidth * (burstIdx + 1)) - 1
////        ) downto (
////          cfg.sdramDqWidth * burstIdx
////        )
////      ) := (
////        RegNext(rDqTriState.read) init(rDqTriState.read.getZero)
////      )
////    }
////  }
////  io.lcvStall.ready := False
////  when (rDataReadyDelay(1)) {
////    io.lcvStall.ready := True
////  }
////  val mode = cfg.mode
////
////  switch (rState) {
////    is (State.Startup) {
////      rSdramA.head := 0x0
////      rSdramBa.head := 0x0
////      when (rRfshCnt === cfg.startupRfshMax - 64) {
////        rChip := False
////      }
////      when (rRfshCnt === cfg.startupRfshMax - 32) {
////        rChip := True
////      }
////      // All the commands during the startup are NOPs, except these
////      when (
////        (
////          rRfshCnt === cfg.startupRfshMax - 63
////        ) || (
////          rRfshCnt === cfg.startupRfshMax - 31
////        )
////      ) {
////        // Ensure all rows are closed.
////        rCmd := SdramCmd.Precharge
////        rSdramA.head(10) := True // all banks
////        rSdramBa.head := 0x0
////      }
////      when (
////        (
////          rRfshCnt === cfg.startupRfshMax - 55
////        ) || (
////          rRfshCnt === cfg.startupRfshMax - 23
////        )
////      ) {
////        // These refreshes need to be at least tREF (66ns) apart.
////        rCmd := SdramCmd.AutoRefresh
////      }
////      when (
////        (
////          rRfshCnt === cfg.startupRfshMax - 47
////        ) || (
////          rRfshCnt === cfg.startupRfshMax - 15
////        )
////      ) {
////        rCmd := SdramCmd.AutoRefresh
////      }
////      when (
////        (
////          rRfshCnt === cfg.startupRfshMax - 39
////        ) || (
////          rRfshCnt === cfg.startupRfshMax - 7
////        )
////      ) {
////        rCmd := SdramCmd.LoadMode
////        rSdramA.head := mode
////      }
////      when (rRfshCnt === 0x0) {
////        rState := State.Idle
////        rRfshCnt := 0x0
////      }
////    }
////    is (State.WaitRdWr) {
////      rState := State.DoRdWr0
////    }
////    is (State.DoRdWr0) {
////      rSdramA.head := rCasAddr
////      when (rSavedH2d.isWrite) {
////        rCmd := SdramCmd.Write
////        rDqTriState.writeEnable := True
////        rDqTriState.write := rSavedH2d.data(15 downto 0)
////        rState := State.DoWr1
////      } otherwise {
////        rCmd := SdramCmd.Read
////        rIdleCnt := cfg.idleCntMax
////        rState := State.WaitIdleCnt
////
////        //rDataReadyDelay(
////        //  cfg.casLatency._2 + cfg.burstLen + cfg.casLatency._3
////        //) := True
////        rDataReadyDelay.msb := True
////      }
////    }
////    is (State.DoWr1) {
////      rState := State.WaitIdleCnt
////
////      rSdramA.head(10) := True
////      rSdramA.head(0) := True
////      rCmd := SdramCmd.Write
////      rDqTriState.writeEnable := True
////      rDqTriState.write := rSavedH2d.data(31 downto 16)
////      rCasAddr(rCasAddr.high downto rCasAddr.high - 1) := (
////        ~rSavedH2d.byteEn(3 downto 2)
////      )
////      rIdleCnt := 2
////
////      io.lcvStall.ready := True
////    }
////    is (State.WaitIdleCnt) {
////      when ((rIdleCnt - 1).msb) {
////        rIdleCnt := cfg.idleCntMax
////        rState := State.Idle
////      } otherwise {
////        rIdleCnt := rIdleCnt - 1
////      }
////    }
////    is (State.Idle) {
////      when (rRfshCnt > cfg.cyclesPerRefresh) {
////        // This SDRAM controller's main way of doing refresh
////        rState := State.Rfsh
////        rCmd := SdramCmd.AutoRefresh
////        rRfshCnt := rRfshCnt - cfg.cyclesPerRefresh + 1
////        rChip := False
////      } elsewhen (
////        //io.lcvStall.rValid
////        rHaveValid.last
////      ) {
////        //(
////        //  rCasAddr(12 downto 9),
////        //  rSdramBa,
////        //  rSdramA,
////        //  rCasAddr(8 downto 0),
////        //)
////        rFullAddr := Cat(
////          List(
////            //U"2'b00",
////            Mux[UInt](
////              io.lcvStall.sendData.isWrite,
////              ~io.lcvStall.sendData.byteEn(1 downto 0),
////              U"2'b00",
////            ),
////            ~io.lcvStall.sendData.isWrite, 
////            //io.lcvStall.sendData.addr(25 downto 1),
////            io.lcvStall.sendData.addr(25 downto 2),
////            U"1'b0"
////          ).reverse
////        )
////        rSavedH2d := io.lcvStall.sendData
////        rChip := io.lcvStall.sendData.addr(26)
////        rHaveValid.last := False
////        rCmd := SdramCmd.Active
////        rState := State.WaitRdWr
////      }
////    }
////    is (State.Rfsh) {
////      rState := State.WaitIdleCnt
////      rCmd := SdramCmd.AutoRefresh
////      rChip := True
////    }
////  }
////  //--------
////  //.datain_h(1'b0),
////  //.datain_l(1'b1),
////  //.outclock(clk),
////  //.dataout(SDRAM_CLK),
////  //.aclr(1'b0),
////  //.aset(1'b0),
////  //.oe(1'b1),
////  //.outclocken(1'b1),
////  //.sclr(1'b0),
////  //.sset(1'b0)
////  val sdramclkDdr = altddio_out(
////    cfg=cfg.altddioOutCfg
////  )
////  sdramclkDdr.io.datain_h := 0x0
////  sdramclkDdr.io.datain_l := 0x1
////  sdramclkDdr.io.outclock := ClockDomain.current.readClockWire
////  io.sdram.clk := sdramclkDdr.io.dataout.lsb
////  sdramclkDdr.io.aclr := False
////  sdramclkDdr.io.aset := False
////  sdramclkDdr.io.oe := True
////  sdramclkDdr.io.outclocken := True
////  sdramclkDdr.io.sclr := False
////  sdramclkDdr.io.sset := False
////  //--------
////}
//
////case class LcvSdramWrapIo(
////  cfg: LcvStallBusSdramCtrlConfig,
////) extends Bundle {
////  val start = in(Bool())
////  val done = out(Bool())
////  val isWrite = in(Bool())
////  val ready = out(Bool())
////    // strobe. when writing, one means that data from wdat written to 
////    // memory
////    //
////    // when reading, one means that data read from memroy is on rdat
////    // output
////  val wdat = in(UInt(16 bits)) // input, data to be written to memory
////  val rdat = out(UInt(16 bits)) // output, data last read from memory 
////  val sz = in(UInt(2 bits))
////  val chip = in(UInt(2 bits))
////
////  val sdram = LcvStallBusSdramIo(cfg=cfg)
////}
//
////case class LcvSdramWrap(
////  cfg: LcvStallBusSdramCtrlConfig
////) extends Component {
////  val io = LcvSdramWrapIo(cfg=cfg)
////
////  io.done.setAsReg() init(io.done.getZero)
////  io.ready.setAsReg() init(io.ready.getZero)
////  io.rdat.setAsReg() init(io.rdat.getZero)
////  //io.sdram.dq.setAsReg() init(io.sdram.dq.getZero)
////
////  val sdramCtrl = LcvStallBusSdramCtrl(cfg=cfg)
////  io.sdram <> sdramCtrl.io.sdram
////
////  def scLcvStall = sdramCtrl.io.lcvStall
////  //scLcvStall.nextValid := True
////  //scLcvStall.sendData.setAsReg() init(scLcvStall.sendData.getZero)
////  val rSendData = {
////    val temp = Reg(
////      //LcvStallBusSdramCtrlH2dPayload(cfg=cfg)
////      LcvStallBusSendPayload(cfg=cfg.busCfg)
////    )
////    temp.init(temp.getZero)
////    temp
////  }
////  scLcvStall.sendData := rSendData
////  io.ready := (
////    RegNext(scLcvStall.nextValid, init=False)
////    && scLcvStall.ready
////  )
////  when (!rSendData.addr(1)) {
////    io.rdat := scLcvStall.recvData.data(15 downto 0)
////  } otherwise {
////    io.rdat := scLcvStall.recvData.data(31 downto 16)
////  }
////  def szUnset = U"2'b00"
////  def sz32M = U"2'b01"
////  def sz64M = U"2'b10"
////  def sz128M = U"2'b11"
////
////  //object State extends SpinalEnum(defaultEncoding=binarySequential) {
////  //  val
////  //    //Startup,
////  //    Idle,
////  //    Running
////  //    = newElement()
////  //}
////  //val rState = Reg(State()) init(State.Idle)
////
////  //switch (rState) {
////  //  //is (State.Startup) {
////  //  //}
////  //  is (State.Idle) {
////  //    io.done := False
////  //  }
////  //  is (State.Running) {
////  //  }
////  //}
////  //val rDone3 = Reg(Bool()) init(False)
////  val nextDone3 = Bool()
////  val rDone3 = RegNext(nextDone3) init(nextDone3.getZero)
////  nextDone3 := rDone3
////
////  rSendData.byteEn := U(rSendData.byteEn.getWidth bits, default -> True)
////
////  scLcvStall.nextValid := False
////  when (
////    io.start
////    || io.done
////  ) {
////    io.done := False
////    nextDone3 := False
////    //rState := State.Idle
////    rSendData.addr := 0x0
////    rSendData.isWrite := io.isWrite
////    //io.ready := False
////  } otherwise {
////    rSendData.isWrite := io.isWrite
////    when (
////      (io.chip === 0)
////      && (io.sz === sz128M)
////      && rSendData.addr(
////        25 downto 2
////      ).andR
////    ) {
////      scLcvStall.nextValid := False
////      nextDone3 := True
////    }
////    when (
////      (io.chip === 1)
////      && (io.sz === sz128M)
////      && rSendData.addr(
////        //rSendData.addr.high - 1 downto 2
////        27 downto 2
////      ).andR
////    ) {
////      scLcvStall.nextValid := False
////      nextDone3 := True
////    }
////    when (
////      (io.chip === 2)
////      && (io.sz === sz128M)
////      && rSendData.addr(
////        //rSendData.addr.high downto 2
////        27 downto 2
////      ).andR
////    ) {
////      scLcvStall.nextValid := False
////      nextDone3 := True
////      //io.done := 
////    }
////    when (
////      (io.sz === sz64M)
////      && rSendData.addr(
////        //rSendData.addr.high - 1 downto 2
////        26 downto 2
////      ).andR
////    ) {
////      scLcvStall.nextValid := False
////      nextDone3 := True
////    }
////    when (
////      (io.sz === sz32M)
////      && rSendData.addr(
////        //rSendData.addr.high - 2 downto 2
////        25 downto 2
////      ).andR
////    ) {
////      scLcvStall.nextValid := False
////      nextDone3 := True
////    }
////    //when (nextDone3) {
////    //  scLcvStall.nextValid := False
////    //}
////    when (rDone3) {
////      scLcvStall.nextValid := False
////      io.done := True
////    } otherwise {
////      scLcvStall.nextValid := True
////      when (
////        RegNext(scLcvStall.nextValid, init=False)
////        && scLcvStall.ready
////      ) {
////        rSendData.data := io.wdat.resize(rSendData.data.getWidth)
////        val myAddrRangeLo = 2
////        val myAddrRange = (
////          //rSendData.addr.high downto myAddrRangeLo
////          27 downto myAddrRangeLo
////        )
////        rSendData.addr(myAddrRange) := rSendData.addr(myAddrRange) + 1
////      }
////    }
////  }
////  //val rAddr = Reg(UInt(cfg.addrWidth bits)) init(0x0)
////}
//
object LcvStallBusSdramCtrlConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    )
  )
}

object LcvStallBusSdramCtrlToVerilog extends App {
  LcvStallBusSdramCtrlConfig.spinal.generateVerilog{
    val top = LcvStallBusSdramCtrl(
      cfg=LcvStallBusSdramCtrlConfig(
        clkRate=(100.0 MHz)
      )
    )
    top
  }
}

////object LcvSdramWrapToVerilog extends App {
////  LcvStallBusSdramCtrlConfig.spinal.generateVerilog{
////    val top = LcvSdramWrap(
////      cfg=LcvStallBusSdramCtrlConfig(
////        clkRate=(
////          //167.0 MHz
////          //120.0 MHz
////          100.0 MHz
////        )
////      )
////    )
////    top
////  }
////}
