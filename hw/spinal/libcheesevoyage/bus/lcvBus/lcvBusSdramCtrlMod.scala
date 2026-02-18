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

package libcheesevoyage.bus.lcvBus

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

case class LcvBusSdramCtrlConfig(
  clkRate: HertzNumber,
  //burstLen: Int=2, // 32-bit
  useAltddioOut: Boolean=true,
  srcWidth: Int=2,
) {
  //--------
  // idea for later: change these to *parameters* of
  // `LcvBusSdramCtrlConfig`
  def sdramDqWidth: Int = 16
  def sdramAWidth: Int = 13
  def sdramBaWidth: Int = 2
  def idleCntMax: Int = 5
  val busCfg = LcvBusConfig(
    mainCfg=LcvBusMainConfig(
      dataWidth=32,
      addrWidth=(
        //27
        32
      ),
      //burstSizeWidth=(
      //  1
      //),
      //burstCntWidth=(
      //  log2Up(((16 / 8) * burstLen /*64*/) / 4)
      //    // the div by 4 is because of 32-bit `dataWidth`
      //  //0
      //  //1
      //  //None
      //  //Some(log2Up(64))
      //),
      //alwaysDoBurst=(
      //  //true
      //  false
      //),
      allowBurst=true,
      burstAlwaysMaxSize=(
        true
      ),
      srcWidth=(
        //1
        //None
        //1
        srcWidth
      ),
      haveByteEn=true,
      keepByteSize=false,
    ),
    //mesiCfg=LcvBusMesiConfig(
    //  numCpus=1
    //)
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

  //def burstLen = 8
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
      //(7 us) * clkRate
      (6.75 us) * clkRate
    )
  }

}


case class LcvBusSdramIo(
  cfg: LcvBusSdramCtrlConfig,
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
    dqmh := False
    dqml := False
  }
  private[libcheesevoyage] def sendCmdNop(
    inPostFirstBurstElem: Boolean=false
  ): Unit = {
    this._sendCmdBase(U"4'b0111")
    if (!inPostFirstBurstElem) {
      dqmh := False
      dqml := False
    }
  }
  private[libcheesevoyage] def sendCmdBurstTerm(): Unit = {
    this._sendCmdBase(U"4'b0110")
    dqmh := False
    dqml := False
  }
  private[libcheesevoyage] def sendCmdRead(
    bank: UInt,
    column: UInt,
    autoPrecharge: Bool,
    someDqTriState: TriState[UInt],
    firstRead: Boolean,
  ): Unit = {
    if (firstRead) {
      this._sendCmdBase(U"4'b0101")
      ba := bank
      a(9 downto 0) := column
      a(10) := autoPrecharge
    } else {
      this.sendCmdNop(inPostFirstBurstElem=true)
    }
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
      this.sendCmdNop(inPostFirstBurstElem=true)
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
    dqmh := False
    dqml := False
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
    dqmh := False
    dqml := False
  }
  private[libcheesevoyage] def sendCmdAutoRefresh(
  ): Unit = {
    this._sendCmdBase(U"4'b0001")
    dqmh := False
    dqml := False
  }
  private[libcheesevoyage] def sendCmdLoadMode(
  ): Unit = {
    this._sendCmdBase(U"4'b0000")
    ba := ba.getZero
    a := cfg.mode
    dqmh := False
    dqml := False
  }
}

case class LcvBusSdramCtrlIo(
  cfg: LcvBusSdramCtrlConfig,
) extends Bundle {
  val bus = slave(LcvBusIo(cfg=cfg.busCfg))
  val sdram = LcvBusSdramIo(cfg=cfg)
}
case class LcvBusSdramCtrl(
  cfg: LcvBusSdramCtrlConfig,
) extends Component {
  //--------
  val io = LcvBusSdramCtrlIo(cfg=cfg)
  //io.bus.h2dBus.ready.setAsReg() init(False)
  //io.bus.h2dBus.ready := False
  val rSavedH2dSendData = (
    Reg(cloneOf(io.bus.h2dBus.payload))
    init(io.bus.h2dBus.payload.getZero)
  )
  val rTempAddr = (
    Reg(cloneOf(rSavedH2dSendData.addr))
    init(rSavedH2dSendData.addr.getZero)
  )
  val rSavedHaveBusWrite = (
    Reg(Bool(), init=False)
  )
  val rHaveBurst = (
    Reg(Bool(), init=False)
  )
  def myBankSliceRange = (
    25 downto 24
    //2 downto 1
  )
  def myRowSliceRange = (
    23 downto 11
    //25 downto 13
  )
  def myColumnSliceRange = (
    10 downto 1
    //12 downto 3
  )

  //def myD2hNextValid = (
  //  io.bus.d2hBus.valid
  //)
  //myD2hNextValid.setAsReg() init(myD2hNextValid.getZero)
  //def myD2hSendData = (
  //  io.bus.d2hBus.payload
  //)
  //myD2hSendData.setAsReg() init(myD2hSendData.getZero)
  val rD2hFifoPushValid = (
    Reg(cloneOf(io.bus.d2hBus.valid))
    init(io.bus.d2hBus.valid.getZero)
  )
  val rD2hWriteValid = (
    Reg(cloneOf(io.bus.d2hBus.valid))
    init(io.bus.d2hBus.valid.getZero)
  )
  val rD2hSendData = (
    Reg(cloneOf(io.bus.d2hBus.payload))
    init(io.bus.d2hBus.payload.getZero)
  )
  //io.bus.d2hBus.valid := rD2hValid
  //io.bus.d2hBus.payload := rD2hSendData

  val h2dFifo = StreamFifo(
    dataType=cloneOf(io.bus.h2dBus.payload),
    depth=(
      //cfg.burstLen
      cfg.busCfg.maxBurstSizeMinus1 + 1
    ),
    latency=(
      2
      //0
      //1
    ),
    forFMax=(
      true
      //false
    ),
  )
  h2dFifo.io.push.valid := (
    io.bus.h2dBus.valid
    //&& io.bus.h2dBus.payload.isWrite
  )
  //h2dFifo.io.push.valid := io.bus.h2dBus.valid
  h2dFifo.io.push.payload := io.bus.h2dBus.payload
  io.bus.h2dBus.ready := h2dFifo.io.push.ready
  val rH2dFifoPopReady = Reg(Bool(), init=False)
  //h2dFifo.io.pop.ready.setAsReg() init(False)
  h2dFifo.io.pop.ready := rH2dFifoPopReady //False

  val d2hFifo = StreamFifo(
    dataType=cloneOf(io.bus.d2hBus.payload),
    depth=(
      //cfg.burstLen
      cfg.busCfg.maxBurstSizeMinus1 + 1
    ),
    latency=(
      2
      //0
      //1
    ),
    forFMax=(
      true
      //false
    ),
  )
  d2hFifo.io.push.valid := rD2hFifoPushValid
  d2hFifo.io.push.payload := rD2hSendData

  io.bus.d2hBus.valid := (
    d2hFifo.io.pop.valid
    || rD2hWriteValid
  )
  io.bus.d2hBus.payload := d2hFifo.io.pop.payload
  d2hFifo.io.pop.ready := io.bus.d2hBus.ready

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
  val rChipBurstCnt = (
    Reg(SInt((log2Up(cfg.burstLen) + 3) bits))
    init(
      //cfg.burstLen - 1
      -1
    )
  )
  when (!rChipBurstCnt.msb) {
    rChipBurstCnt := rChipBurstCnt - 1
  }
  //def myBusBurstBankCntWidth = (
  //  //log2Up(
  //  //  (1 << cfg.busCfg.burstCntWidth)
  //  //+ 
  //  //- log2Up(cfg.burstLen * 2)
  //  cfg.sdramBaWidth
  //)
  val rBusBurstOuterCnt = (
    Reg(SInt((cfg.sdramBaWidth + 3) bits))
    init(-1)
  )
  val rBusBurstInnerCnt = (
    Reg(SInt((log2Up(cfg.burstLen / 2) + 3) bits))
    init(-1)
  )
  val rStartBusBurst = (
    Reg(Bool(), init=False)
  )
  //val rTempBurstFirst = (
  //  Reg(Bool(), init=False)
  //)
  val rTempBurstLast = (
    Reg(Bool(), init=False)
  )

  //def STATE_PWR_ON_INIT = 0x0

  //def STATE_PWR_ON_CNT_DO_CKE_HI = 0x1

  //def STATE_PWR_ON_SEND_PRECHARGE_ALL = 0x2
  //def STATE_PWR_ON_WAIT_T_RP = 0x3    // send NOPs

  //def STATE_PWR_ON_SEND_AUTO_REFRESH_0 = 0x4
  //def STATE_PWR_ON_WAIT_T_RFC_0 = 0x5  // send NOPs or CMD INHIBITs

  //def STATE_PWR_ON_SEND_AUTO_REFRESH_1 = 0x6
  //def STATE_PWR_ON_WAIT_T_RFC_1 = 0x7   // send NOPs or CMD INHIBITs

  //def STATE_PWR_ON_LOAD_MODE_REGISTER = 0x8
  //def STATE_PWR_ON_WAIT_T_MRD = 0x9     // send NOPs

  //def STATE_IDLE = 0x10
  //def STATE_SEND_RFSH = 0x11
  //def STATE_RFSH_POST_NOPS = 0x12
  //def STATE_SEND_ACTIVE = 0x13
  //def STATE_ACTIVE_POST_NOPS = 0x14
  //def STATE_PRE_READ_WRITE = 0x15
  //def STATE_SEND_READ_0 = 0x16
  //def STATE_SEND_READ_N = STATE_SEND_READ_0 + cfg.burstLen - 1 + 1
  //def STATE_READ_POST_NOPS = STATE_SEND_READ_N + 0x1
  //def STATE_SEND_WRITE_0 = STATE_READ_POST_NOPS + 0x1
  //def STATE_SEND_WRITE_N = STATE_SEND_WRITE_0 + cfg.burstLen - 1 + 1
  //def STATE_WRITE_POST_NOPS = STATE_SEND_WRITE_N + 1
  //def LIM_STATE = STATE_WRITE_POST_NOPS + 1

  object State extends SpinalEnum(defaultEncoding=binarySequential) {
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
      SEND_READ_0,
      SEND_READ_N,
      READ_POST_NOPS,
      SEND_WRITE_0,
      SEND_WRITE_HI_N,
      SEND_WRITE_LO_N,
      WRITE_POST_NOPS

      = newElement();
  }

  val rState = (
    KeepAttribute(
      Reg(
        State()
        //UInt(log2Up(LIM_STATE) bits)
      )
      init(
        //STATE_PWR_ON_INIT
        State.PWR_ON_INIT
      )
    )
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
      rH2dFifoPopReady := False
      when (rNeedRfshCnt.msb) {
        rNeedRfshCnt := cfg.Cycles.tREFIthresh.toInt
        rState := State.SEND_RFSH
      } elsewhen (
        // wait for the d2hFifo to be emptied so that any urst is
        // guaranteed to be completed
        RegNext(
          next=(
            d2hFifo.io.occupancy === 0
            //&& h2dFifo.io.availability === 0

            && h2dFifo.io.pop.valid
            && (
              !h2dFifo.io.pop.isWrite
              //!rSavedH2dSendData.isWrite
              //|| h2dFifo.io.availability === 0
              || h2dFifo.io.occupancy === cfg.busCfg.maxBurstSizeMinus1 + 1
              || !h2dFifo.io.pop.burstFirst
            )
            //&& h2dFifo.io.occupancy === cfg.busCfg.maxBurstSizeMinus1 + 1
          ),
          init=False
        ) || (
          RegNext(
            next=(!rBusBurstOuterCnt.msb),
            init=False
          )
        )
      ) {
        //io.bus.h2dBus.ready := True
        //h2dFifo.io.pop.ready := True

        rStartBusBurst := False

        when (
          rBusBurstOuterCnt.msb
          ////&& rHaveBurst
          //|| rSavedH2dSendData.isWrite
        ) {
          rH2dFifoPopReady := True
          rTempAddr := (
            RegNext(
              next=h2dFifo.io.pop.addr,
              init=h2dFifo.io.pop.addr.getZero,
            ),
          )
          rSavedH2dSendData := (
            //io.bus.h2dBus.payload
            RegNext(
              next=h2dFifo.io.pop.payload,
              init=h2dFifo.io.pop.payload.getZero,
            ),
          )
          rHaveBurst := (
            //rSavedH2dSendData.burstFirst
            RegNext(
              next=h2dFifo.io.pop.burstFirst,
              init=False,
            )
          )
        } otherwise {
          rTempAddr := (
            rTempAddr + ((cfg.burstLen / 2) * 4)
          )
        }
        when (
          rBusBurstOuterCnt.msb
          && (
            RegNext(
              next=(
                h2dFifo.io.pop.burstFirst
                //|| !h2dFifo.io.pop.isWrite
              ),
              init=False,
            )
            //|| !rSavedH2dSendData.isWrite
          )
        ) {
          rBusBurstOuterCnt := (
            //cfg.busCfg.maxBurstSizeMinus1
            (1 << cfg.sdramBaWidth) - 2
          )
          rStartBusBurst := True
        }
        when (!rBusBurstOuterCnt.msb) {
          rBusBurstOuterCnt := rBusBurstOuterCnt - 1
        }
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
      rH2dFifoPopReady := False
      io.sdram.sendCmdActive(
        bank=rTempAddr(myBankSliceRange),
        row=rTempAddr(myRowSliceRange),
      )
      rState := State.ACTIVE_POST_NOPS
    }
    is (State.ACTIVE_POST_NOPS) {
      io.sdram.sendCmdNop()

      rH2dFifoPopReady := (
        False
        //True
      )

      when (!rActiveNopWaitCnt.msb) {
        rActiveNopWaitCnt := rActiveNopWaitCnt - 1
      } otherwise {
        rActiveNopWaitCnt := cfg.Cycles.tRCD._1.get.toInt
        rState := State.PRE_READ_WRITE
        //rH2dFifoPopReady := (
        //  False
        //  //True
        //)
      }
    }
    is (State.PRE_READ_WRITE) {
      io.sdram.sendCmdNop()

      rH2dFifoPopReady := False
      //rSavedH2dSendData := (
      //  //io.bus.h2dBus.payload
      //  h2dFifo.io.pop.payload
      //)
      //switch (
      //  rSavedH2dSendData.burstFirst
      //  ## rSavedH2dSendData.isWrite
      //) {
      //  is (B"00") {
      //    // !burstFirst, !isWrite
      //  }
      //  is (B"01") {
      //    // !burstFirst, isWrite
      //  }
      //  is (B"10") {
      //    // burstFirst, !isWrite
      //  }
      //  is (B"11") {
      //    // burstFirst, isWrite
      //  }
      //}
      //rHaveBurst := rSavedH2dSendData.burstFirst
      when (!rSavedH2dSendData.isWrite) {
        rState := State.SEND_READ_0
      } otherwise {
        //when (!rBusBurstOuterCnt.msb) {
        //  rH2dFifoPopReady := True
        //}
        //rH2dFifoPopReady := True
        //rSavedH2dSendData := (
        //  //io.bus.h2dBus.payload
        //  h2dFifo.io.pop.payload
        //)
        rState := State.SEND_WRITE_0
      }
    }
    is (State.SEND_READ_0) {
      rH2dFifoPopReady := False
      io.sdram.sendCmdRead(
        bank=rTempAddr(myBankSliceRange),
        column=rTempAddr(myColumnSliceRange),
        autoPrecharge=True,
        someDqTriState=rDqTriState,
        firstRead=true,
      )
      //rState := State.READ_POST_NOPS
      rState := State.SEND_READ_N
      rRdCasLatencyCnt := (
        //cfg.casLatency._2 - 1
        myRdCasLatencyCntNumCycles - 1
      )
      rRdNopWaitCnt := myRdNopWaitCntNumCycles

      rD2hFifoPushValid := False//True
      when (rStartBusBurst) {
        rD2hSendData.burstCnt := (
          //cfg.burstLen - 1
          cfg.busCfg.maxBurstSizeMinus1 //+ 1
        )
        rD2hSendData.burstFirst := rHaveBurst
      } otherwise {
        rD2hSendData.burstFirst := False
      }
      rD2hSendData.burstLast := False
      //rChipBurstCnt := cfg.burstLen - 2
    }
    is (State.SEND_READ_N) {
      io.sdram.sendCmdRead(
        bank=rTempAddr(myBankSliceRange),
        column=rTempAddr(myColumnSliceRange),
        autoPrecharge=True,
        someDqTriState=rDqTriState,
        firstRead=false,
      )
      rD2hFifoPushValid := False
      val nextD2hValid = Bool()
      nextD2hValid := False
      when (!rRdCasLatencyCnt.msb) {
        rRdCasLatencyCnt := rRdCasLatencyCnt - 1
        when (rRdCasLatencyCnt(0) === False) {
          when (rHaveBurst) {
            nextD2hValid := (
              //rRdCasLatencyCnt.asUInt <= cfg.busCfg.maxBurstSizeMinus1 * 2
              rRdCasLatencyCnt.asUInt <= cfg.burstLen - 1//* 2
            )
            when (nextD2hValid && !rD2hSendData.burstFirst) {
              rD2hSendData.burstCnt := rD2hSendData.burstCnt - 1
            }
          } otherwise {
            nextD2hValid := (
              rRdCasLatencyCnt.asUInt === (
                //cfg.busCfg.maxBurstSizeMinus1 * 2
                cfg.burstLen - 2 //- 1
              )
            )
          }
          rD2hFifoPushValid := (
            //rRdCasLatencyCnt.asUInt < cfg.burstLen
            //rRdCasLatencyCnt.asUInt <= cfg.busCfg.maxBurstSizeMinus1 * 2//+ 1
            nextD2hValid
          )
          rD2hSendData.data(31 downto 16) := rDqTriState.read
        } elsewhen (rRdCasLatencyCnt(0) === True) {
          when (rD2hFifoPushValid) {
            rD2hSendData.burstFirst := False
          }
          rD2hSendData.data(15 downto 0) := rDqTriState.read
        }
        when (
          rRdCasLatencyCnt === 0
          && rBusBurstOuterCnt.msb
        ) {
          rD2hSendData.burstLast := True
        }
      } otherwise {
        rD2hFifoPushValid := False
        rState := State.READ_POST_NOPS
      }
      when (!rRdNopWaitCnt.msb) {
        rRdNopWaitCnt := rRdNopWaitCnt - 1
      }
      when (rRdCasLatencyCnt.msb && rRdNopWaitCnt.msb) {
        rD2hFifoPushValid := False
        rState := State.IDLE
      }
    }
    is (State.READ_POST_NOPS) {
      io.sdram.sendCmdNop()
      rD2hFifoPushValid := False

      when (!rRdNopWaitCnt.msb) {
        rRdNopWaitCnt := rRdNopWaitCnt - 1
      }
      when (rRdNopWaitCnt.msb) {
        rState := State.IDLE
      }
    }
    is (State.SEND_WRITE_0) {
      io.sdram.sendCmdWrite(
        bank=rTempAddr(myBankSliceRange),
        column=rTempAddr(myColumnSliceRange),
        autoPrecharge=True,
        someDqTriState=rDqTriState,
        wrData=rSavedH2dSendData.data(15 downto 0),
        wrByteEn=rSavedH2dSendData.byteEn(1 downto 0),
        firstWrite=true,
      )
      rWrNopWaitCnt := myWrNopWaitCntNumCycles
      rState := State.SEND_WRITE_HI_N
      rChipBurstCnt := (
        //cfg.burstLen - 6//- 4 //- 2
        //cfg.burstLen - 5
        //cfg.burstLen - 2
        cfg.burstLen - 3
        //(cfg.burstLen / 2) - 2
      )
      //when (rStartBusBurst) {
        rBusBurstInnerCnt := (
          (cfg.burstLen / 2) - 3
        )
      //} otherwise {
      //  rBusBurstInnerCnt := (
      //    (cfg.burstLen / 2) - 2
      //  )
      //}
      rTempBurstLast := False
    }
    is (State.SEND_WRITE_HI_N) {
      io.sdram.sendCmdWrite(
        bank=rTempAddr(myBankSliceRange),
        column=rTempAddr(myColumnSliceRange),
        autoPrecharge=True,
        someDqTriState=rDqTriState,
        wrData=rSavedH2dSendData.data(31 downto 16),
        wrByteEn=rSavedH2dSendData.byteEn(3 downto 2),
        firstWrite=false,
      )
      when (
        rSavedH2dSendData.burstLast
        || rTempBurstLast
        //|| 
        //(!rHaveBurst && rTempBurstLast)
        //|| (rHaveBurst && rChipBurstCnt.msb)
        //|| (rHaveBurst && RegNext(rChipBurstCnt === 0)
      ) {
        rState := State.WRITE_POST_NOPS
        when (rBusBurstOuterCnt.msb) {
          rD2hWriteValid := True
        }
        rH2dFifoPopReady := False
      } otherwise {
        rState := State.SEND_WRITE_LO_N
      }
      when (rHaveBurst) {
        rH2dFifoPopReady := True
      }
      rSavedH2dSendData := (
        //io.bus.h2dBus.payload
        h2dFifo.io.pop.payload
      )
      when (!rHaveBurst) {
        rSavedH2dSendData.byteEn := 0x0
        //when (rChipBurstCnt.msb) {
        //  //rSavedH2dSendData.burstLast := True
        //  rTempBurstLast := True
        //}
        when (rChipBurstCnt.msb) {
          rSavedH2dSendData.burstLast := True
          rState := State.WRITE_POST_NOPS
          rD2hWriteValid := True
          //rTempBurstLast := True
        }
      } otherwise { // when (rHaveBurst)
      }
      //when (rChipBurstCnt.msb) {
      //  //rSavedH2dSendData.burstLast := True
      //  rTempBurstLast := True
      //}
    }
    is (State.SEND_WRITE_LO_N) {
      io.sdram.sendCmdWrite(
        bank=rTempAddr(myBankSliceRange),
        column=rTempAddr(myColumnSliceRange),
        autoPrecharge=True,
        someDqTriState=rDqTriState,
        wrData=rSavedH2dSendData.data(15 downto 0),
        wrByteEn=rSavedH2dSendData.byteEn(1 downto 0),
        firstWrite=false,
      )
      rH2dFifoPopReady := False
      rState := State.SEND_WRITE_HI_N
      when (rHaveBurst) {
        when (!rBusBurstInnerCnt.msb) {
          rBusBurstInnerCnt := rBusBurstInnerCnt - 1
        } otherwise {
          rTempBurstLast := True
        }
      }
    }
    is (State.WRITE_POST_NOPS) {
      rH2dFifoPopReady := False
      io.sdram.sendCmdNop()
      when (
        //rD2hFifoPushValid
        rD2hWriteValid
        //&& io.bus.d2hBus.ready
        && d2hFifo.io.push.ready
      ) {
        rD2hWriteValid := False
      }
      when (!rWrNopWaitCnt.msb) {
        rWrNopWaitCnt := rWrNopWaitCnt - 1
      } elsewhen (!rD2hFifoPushValid) {
        rState := State.IDLE
      }
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
  val cfg = LcvBusSdramCtrlConfig(
    clkRate=clkRate,
    useAltddioOut=useAltddioOut,
  )
  val io = LcvBusSdramIo(cfg=cfg)
  //--------
  val mySdramCtrl = LcvBusSdramCtrl(cfg=cfg)
  io <> mySdramCtrl.io.sdram
  //--------
  val sdramCtrlTester = LcvBusDeviceRamTester(
    cfg=LcvBusDeviceRamTesterConfig(
      busCfg=cfg.busCfg,
      kind=(
        //LcvBusDeviceRamTesterKind.DualBurstRandData
        //LcvBusDeviceRamTesterKind.NoBurstRandData
        LcvBusDeviceRamTesterKind.DualBurstRandDataSemiRandAddr
        //(
        //  optNonCoherentCacheSetWidth=(
        //    //None
        //    Some(8)
        //  )
        //)
      ),
    )
  )
  sdramCtrlTester.io.busVec.head <> mySdramCtrl.io.bus
  //--------
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

object LcvBusSdramCtrlConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    )
  )
}

object LcvBusSdramCtrlToVerilog extends App {
  LcvBusSdramCtrlConfig.spinal.generateVerilog{
    val top = LcvBusSdramCtrl(
      cfg=LcvBusSdramCtrlConfig(
        clkRate=(100.0 MHz)
      )
    )
    top
  }
}
