package libcheesevoyage.hwdev

//import scala.math._
import spinal.core._
import spinal.lib._
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer
import scala.math._


case class ClkCnt(
  clkRate: HertzNumber,
  time: TimeNumber,
  overflowPipeSize: Int=1, 
  //numClks: Int,
  //width: Int,
) extends Bundle {
  def numClksDouble = scala.math.ceil((clkRate * time).toDouble)
  def numClks = numClksDouble.toInt
  def width = log2Up(numClks + 1)
  println(f"ClkCnt: $numClksDouble $width")

  val cnt = UInt(width bits)
  val overflowPipe = Bits(overflowPipeSize bits)

  def incr(
    //someCnt: UInt,
    //someCntOverflowPipe1: Bool,
    //someCntOverflowPipe2: Bool,
    //someCntNumClks: BigInt,
  ): Unit = {
    //someCntOverflowPipe1 := someCnt === someCntNumClks - 1
    //someCntOverflowPipe2 := someCnt === someCntNumClks - 2
    //when (someCntOverflowPipe1) {
    //  someCnt := 0
    //} otherwise {
    //  someCnt := someCnt + 1
    //}
    for (idx <- 0 to overflowPipeSize - 1) {
      overflowPipe(idx) := cnt === numClks - 1 - idx
    }
    when (overflowPipe(0)) {
      cnt := 0
    } otherwise {
      cnt := cnt + 1
    }
  }
}
case class SnesCtrlIo() extends Bundle {
  // FL4SHK notes on SNES Controller pins (only my SNES controller
  // extension cable that I stripped the wires of):
  //  Green (short)/Yellow (long): +5v
  //  Black (short)/Black (long): Ground
  //  Blue (short)/Blue (long): Data Clock (`outpClk`)
  //  White w/ Green Tint (short)/White (long): Data Latch (`outpLatch`)
  //  Red (short)/Red (long): Serial Data (`inpData`)
  val outpClk = out Bool()
  val outpLatch = out Bool()
  val inpData = in Bool()
}
object SnesButtons {
  val B = 0
  val Y = 1
  val Select = 2
  val Start = 3
  val DpadUp = 4
  val DpadDown = 5
  val DpadLeft = 6
  val DpadRight = 7
  val A = 8
  val X = 9
  val L = 10
  val R = 11
  val numReal = 12

  val rawButtonsWidth = 16
}
object SnesCtrlReaderIo {
  val unitCntLatchTime = 12.0 us
  val unitCntLatchOverflowPipeSize = 1
  val unitCntWaitToClkGenTime = 6.0 us
  val unitCntWaitToClkGenOverflowPipeSize = 1
  val unitCntDoClkGenTime = (12.0 / 2.0) us
  val unitCntDoClkGenOverflowPipeSize = 2
}
case class SnesCtrlReaderIo(
  clkRate: HertzNumber,
) extends Bundle {
  val snesCtrl = SnesCtrlIo()
  val unitCntLatch = out(ClkCnt(
    clkRate=clkRate,
    time=SnesCtrlReaderIo.unitCntLatchTime,
    overflowPipeSize=SnesCtrlReaderIo.unitCntLatchOverflowPipeSize,
  ))
  val unitCntWaitToClkGen = out(ClkCnt(
    clkRate=clkRate,
    time=SnesCtrlReaderIo.unitCntWaitToClkGenTime,
    overflowPipeSize=SnesCtrlReaderIo.unitCntWaitToClkGenOverflowPipeSize,
  ))
  val unitCntDoClkGen = out(ClkCnt(
    clkRate=clkRate,
    time=SnesCtrlReaderIo.unitCntDoClkGenTime,
    overflowPipeSize=SnesCtrlReaderIo.unitCntDoClkGenOverflowPipeSize,
  ))

  //val outpButtons = out UInt(buttonsWidth bits)
  //val outpValid
  val pop = master Stream(UInt(
    //SnesButtons.numReal bits
    SnesButtons.rawButtonsWidth bits
  ))
}

object SnesCtrlReaderState extends SpinalEnum(
  //defaultEncoding=binaryOneHot
  defaultEncoding=binarySequential // for debugging
) {
  val
    latch,
    waitToClkGen,
    clkGen,
    //beforeWaitPopReady,
    waitPopReady
    = newElement();
}
case class SnesCtrlReader(
  clkRate: HertzNumber,
  vivadoDebug: Boolean=false,
) extends Component {
  //--------
  val io = SnesCtrlReaderIo(clkRate=clkRate)
  val pop = io.pop
  //--------
  //// SNES controllers use a protocol of 12 us per full clock cycle
  //val rUnitCntMain = Reg(ClkCnt(
  //  clkRate=clkRate,
  //  time=12.0 us
  //))
  //val rUnitCntWaitToClkGen = Reg(ClkCnt(
  //  clkRate=clkRate,
  //  time=6.0 us,
  //))
  //val rUnitCntDoClkGen = Reg(ClkCnt(
  //  clkRate=clkRate,
  //  time=(12.0 / 2.0) us,
  //))
  //def unitCntInfoBeforeWaitReady = UnitCntInfo(17 ms)

  //def unitCntWidth = (
  //  (unitCntInfoMain.width)
  //  .max(unitCntInfoWaitToClkGen.width)
  //  .max(unitCntInfoDoClkGen.width)
  //  //.max(unitCntInfoBeforeWaitReady.width)
  //)

  //def clkGenCntInfo = CntInfo((12 * SnesButtons.rawButtonsWidth) us)
  //--------
  val loc = new Area {
    ////val rUnitCnt = Reg(UInt(unitCntWidth bits)) init(0x0)
    ////val rUnitCntOverflowPipe1 = Reg(Bool()) init(False)
    ////val rUnitCntOverflowPipe2 = Reg(Bool()) init(False)
    //val rUnitCntLatch = Reg(ClkCnt(
    //  clkRate=clkRate,
    //  //time=12.0 us,
    //  //overflowPipeSize=1,
    //  time=SnesCtrlReaderIo.unitCntLatchTime,
    //  overflowPipeSize=SnesCtrlReaderIo.unitCntLatchOverflowPipeSize,
    //))
    //val rUnitCntWaitToClkGen = Reg(ClkCnt(
    //  clkRate=clkRate,
    //  //time=6.0 us,
    //  //overflowPipeSize=1,
    //  time=SnesCtrlReaderIo.unitCntWaitToClkGenTime,
    //  overflowPipeSize=(
    //    SnesCtrlReaderIo.unitCntWaitToClkGenOverflowPipeSize
    //  ),
    //))
    //val rUnitCntDoClkGen = Reg(ClkCnt(
    //  clkRate=clkRate,
    //  //time=(12.0 / 2.0) us,
    //  //overflowPipeSize=2,
    //  time=SnesCtrlReaderIo.unitCntDoClkGenTime,
    //  overflowPipeSize=(
    //    SnesCtrlReaderIo.unitCntDoClkGenOverflowPipeSize
    //  ),
    //))
    val rUnitCntLatch = Reg(cloneOf(io.unitCntLatch))
    val rUnitCntWaitToClkGen = Reg(cloneOf(io.unitCntWaitToClkGen))
    val rUnitCntDoClkGen = Reg(cloneOf(io.unitCntDoClkGen))

    rUnitCntLatch.init(rUnitCntLatch.getZero)
    rUnitCntWaitToClkGen.init(rUnitCntWaitToClkGen.getZero)
    rUnitCntDoClkGen.init(rUnitCntDoClkGen.getZero)

    io.unitCntLatch := rUnitCntLatch
    io.unitCntWaitToClkGen := rUnitCntWaitToClkGen
    io.unitCntDoClkGen := rUnitCntDoClkGen

    // needed for clock signal generation
    def clkGenCntNumClks = SnesButtons.rawButtonsWidth * 2
    def clkGenCntWidth = log2Up(clkGenCntNumClks)
    val rClkGenCnt = Reg(UInt(clkGenCntWidth bits)) init(0x0)
    //val rClkGenCntAndUnitCntOverflowPipe1 = Reg(Bool()) init(False)
    val rClkGenCntOverflowPipe1 = Reg(Bool()) init(False)
    //val rClkGenCntOverflowPipe2 = Reg(Bool()) init(False)

    // `rClk` is held high outside of generating the clock 
    val rClk = Reg(Bool()) init(True)
    // `rLatch` is held low outside of the data latching
    val rLatch = Reg(Bool()) init(True)
    //val rData = Reg(Bool()) init(False)
    val data = Bool()

    io.snesCtrl.outpClk := rClk
    io.snesCtrl.outpLatch := rLatch
    data := io.snesCtrl.inpData

    val rPopValid = Reg(Bool()) init(False)
    val rButtons = Reg(UInt(SnesButtons.rawButtonsWidth bits)) init(0x0)
    pop.valid := rPopValid
    pop.payload := rButtons(pop.payload.bitsRange)

    val rState = Reg(SnesCtrlReaderState()) init(SnesCtrlReaderState.latch)

    if (vivadoDebug) {
      rUnitCntLatch.addAttribute("MARK_DEBUG", "TRUE")
      rUnitCntWaitToClkGen.addAttribute("MARK_DEBUG", "TRUE")
      rUnitCntDoClkGen.addAttribute("MARK_DEBUG", "TRUE")

      //rUnitCntOverflowPipe1.addAttribute("MARK_DEBUG", "TRUE")
      rClkGenCnt.addAttribute("MARK_DEBUG", "TRUE")
      //rClkGenCntAndUnitCntOverflowPipe1.addAttribute("MARK_DEBUG", "TRUE")
      rClkGenCntOverflowPipe1.addAttribute("MARK_DEBUG", "TRUE")
      //rClkGenCntOverflowPipe2.addAttribute("MARK_DEBUG", "TRUE")
      rClk.addAttribute("MARK_DEBUG", "TRUE")
      rLatch.addAttribute("MARK_DEBUG", "TRUE")
      data.addAttribute("MARK_DEBUG", "TRUE")

      rPopValid.addAttribute("MARK_DEBUG", "TRUE")
      rButtons.addAttribute("MARK_DEBUG", "TRUE")
      rState.addAttribute("MARK_DEBUG", "TRUE")
    }
    //def incrCnt(
    //  someCnt: UInt,
    //  someCntOverflowPipe1: Bool,
    //  someCntOverflowPipe2: Bool,
    //  someCntNumClks: BigInt,
    //): Unit = {
    //  someCntOverflowPipe1 := someCnt === someCntNumClks - 1
    //  someCntOverflowPipe2 := someCnt === someCntNumClks - 2
    //  when (someCntOverflowPipe1) {
    //    someCnt := 0
    //    //rState := nextState
    //  } otherwise {
    //    someCnt := someCnt + 1
    //  }
    //}
    //def incrUnitCnt(
    //  someCntNumClks: BigInt
    //): Unit = {
    //  incrCnt(
    //    someCnt=rUnitCnt,
    //    someCntOverflowPipe1=rUnitCntOverflowPipe1,
    //    someCntOverflowPipe2=rUnitCntOverflowPipe2,
    //    someCntNumClks=someCntNumClks,
    //  )
    //}

    def doStateLatch(): Unit = {
      is (SnesCtrlReaderState.latch) {
        //incrUnitCnt(someCntNumClks=unitCntInfoMain.numClks1)
        rUnitCntLatch.incr()
        rClk := True
        when (
          //rUnitCntOverflowPipe1
          rUnitCntLatch.overflowPipe(0)
        ) {
          rState := SnesCtrlReaderState.waitToClkGen
          rLatch := False
          rUnitCntLatch := rUnitCntLatch.getZero
          rUnitCntWaitToClkGen := rUnitCntWaitToClkGen.getZero
          rUnitCntDoClkGen := rUnitCntDoClkGen.getZero
        }
      }
    }
    def doStateWaitToClkGen(): Unit = {
      is (SnesCtrlReaderState.waitToClkGen) {
        //incrUnitCnt(someCntNumClks=unitCntInfoWaitToClkGen.numClks1)
        rUnitCntWaitToClkGen.incr()
        when (
          //rUnitCntOverflowPipe1
          rUnitCntWaitToClkGen.overflowPipe(0)
        ) {
          rState := SnesCtrlReaderState.clkGen
          rClkGenCnt := 0
          //rClk := False
          rClk := True
          rUnitCntLatch := rUnitCntLatch.getZero
          rUnitCntWaitToClkGen := rUnitCntWaitToClkGen.getZero
          rUnitCntDoClkGen := rUnitCntDoClkGen.getZero
        }
      }
    }
    def doStateClkGen(): Unit = {
      // also grab button values
      is (SnesCtrlReaderState.clkGen) {
        //incrUnitCnt(someCntNumClks=unitCntInfoDoClkGen.numClks2)
        rUnitCntDoClkGen.incr()
        //rClkGenCntOverflowPipe2 := (
        //  rClkGenCnt === clkGenCntNumClks - 1
        //)
        //rClkGenCntAndUnitCntOverflowPipe1 := (
        //  //rUnitCntOverflowPipe2
        //  rUnitCntDoClkGen.overflowPipe(1)
        //  && rClkGenCntOverflowPipe2
        //)
        rClkGenCntOverflowPipe1 := (
          rClkGenCnt === (1 << rClkGenCnt.getWidth) - 1
        )
        when (rUnitCntDoClkGen.overflowPipe(0)) {
          when (
            //rClkGenCntAndUnitCntOverflowPipe1
            //rClkGenCnt === (1 << rClkGenCnt.getWidth) - 1
            rClkGenCntOverflowPipe1
          ) {
            //rState := SnesCtrlReaderState.waitPopReady
            //rState := SnesCtrlReaderState.beforeWaitPopReady
            rState := SnesCtrlReaderState.waitPopReady
            rClk := True
            rPopValid := True
            rClkGenCnt := 0
          } otherwise {
            //when (!rClkGenCnt(0)) {
            //}
            rClk := !rClk
            when (!rClk)
            //when (rClk)
            {
              //rButtons(rClkGenCnt) := data
              rButtons(rClkGenCnt(rClkGenCnt.high downto 1)) := data
            }
            rClkGenCnt := rClkGenCnt + 1
            //incrCnt(
            //  someCnt=rClkGenCnt,
            //  someCntOverflowPipe1=rClkGenCntOverflowPipe1,
            //  someCntNumClks=SnesButtons.rawButtonsWidth
            //)
            //rState := SnesCtrlReaderState.waitPopReady
            //rPopValid := True
          }
        }
      }
    }
    //def doStateBeforeWaitPopReady(): Unit = {
    //  is (SnesCtrlReaderState.beforeWaitPopReady) {
    //    incrUnitCnt(
    //      someCntNumClks=unitCntInfoBeforeWaitReady.numClks
    //    )
    //    when (rUnitCntOverflowPipe1) {
    //      rState := SnesCtrlReaderState.waitPopReady
    //    }
    //  }
    //}
    def doStateWaitPopReady(): Unit = {
      is (SnesCtrlReaderState.waitPopReady) {
        when (pop.fire) {
          rState := SnesCtrlReaderState.latch
          rPopValid := False
          rLatch := True
        }
      }
    }
  }
  switch (loc.rState) {
    loc.doStateLatch()
    loc.doStateWaitToClkGen()
    loc.doStateClkGen()
    //loc.doStateBeforeWaitPopReady()
    loc.doStateWaitPopReady()
  }
  //when (loc.clkCnt =/= clkRate.toInt) {
  //  loc.clkCnt := loc.clkCnt + 1
  //} otherwise {
  //  loc.clkCnt := 0
  //  loc.rClk := !loc.rClk
  //}
  //--------
}
