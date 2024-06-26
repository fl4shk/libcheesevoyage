package libcheesevoyage.math
import libcheesevoyage.general._
import libcheesevoyage._

import spinal.core._
import spinal.lib._
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer

object BarrelShifter {
  def kindLsl = 0
  def kindLsr = 1
  def kindAsr = 2
  def kindLim = 3
}

case class BarrelShifterIo(
  wordWidth: Int
) extends Bundle {
  val inpData = in UInt(wordWidth bits)
  val inpAmount = in UInt(wordWidth bits)
  val outpData = out UInt(wordWidth bits)
}
case class BarrelShifter(
  wordWidth: Int,
  kind: Int,
) extends Component {
  //--------
  assert(kind >= 0)
  assert(kind < BarrelShifter.kindLim)
  //--------
  val io = BarrelShifterIo(wordWidth=wordWidth)
  //--------
  def wordType() = UInt(wordWidth bits)
  def getModdedI(i: Int): Int = 1 << (i - 1)
  //--------
  //`define MAKE_BIT_SHIFT_PROLOG(wordWidth)
  //  localparam __WIDTH__DATA_INOUT = wordWidth;
  //  localparam __MSB_POS__DATA_INOUT = `WIDTH2MP(__WIDTH__DATA_INOUT);
  def dataIoMsbPos = wordWidth - 1
 
  //  localparam __LOG2__WIDTH__DATA_INOUT = $clog2(__WIDTH__DATA_INOUT);
  def dataIoLog2Width = log2Up(dataIoMsbPos)
  //  localparam __ARR_SIZE__TEMP = __LOG2__WIDTH__DATA_INOUT + 1;
  def tempArrSize = dataIoLog2Width + 1
  //  localparam __LAST_INDEX__TEMP
  //    = `ARR_SIZE_TO_LAST_INDEX(__ARR_SIZE__TEMP);
  def tempLastIdx = tempArrSize - 1
 
  //  localparam __INDEX__OUT_DATA = __LAST_INDEX__TEMP;
  def outpDataIdx = tempLastIdx
 
  //  logic [__MSB_POS__DATA_INOUT:0] __temp[0 : __LAST_INDEX__TEMP];
  val temp = Vec.fill(tempLastIdx + 1)(UInt(wordWidth bits))
 
  //  always @(*)
  //  begin
  //    __temp[0] = in_to_shift;
  //  end
  temp(0) := io.inpData
 
  //  assign out_data = __temp[__INDEX__OUT_DATA];
  io.outpData := temp(outpDataIdx)

  //`define SET_TEMP_LSL(i) \
  //  __temp[i] = in_amount[i - 1] \
  //    ? {__temp[i - 1][__MSB_POS__DATA_INOUT - `GET_MODDED_I(i) : 0], \
  //    {`GET_MODDED_I(i){1'b0}}} \
  //    : __temp[i - 1]
  def setTempLsl(i: Int): Unit = {
    temp(i) := Mux(
      io.inpAmount(i - 1),
      Cat(
        temp(i - 1)(dataIoMsbPos - getModdedI(i) downto 0),
        U(getModdedI(i) bits, default -> False)
      ).asUInt,
      temp(i - 1)
    )
  }

  //`define SET_TEMP_LSR(i) \
  //  __temp[i] = in_amount[i - 1] \
  //    ? {{`GET_MODDED_I(i){1'b0}}, \
  //    __temp[i - 1][__MSB_POS__DATA_INOUT : `GET_MODDED_I(i)]} \
  //    : __temp[i - 1][__MSB_POS__DATA_INOUT : 0]
  def setTempLsr(i: Int): Unit = {
    temp(i) := Mux(
      io.inpAmount(i - 1),
      Cat(
        //{`GET_MODDED_I(i){1'b0}},
        //__temp[i - 1][__MSB_POS__DATA_INOUT : `GET_MODDED_I(i)]
        U(getModdedI(i) bits, default -> False),
        temp(i - 1)(dataIoMsbPos downto getModdedI(i)),
      ).asUInt,
      temp(i - 1)(dataIoMsbPos downto 0)
    )
  }

  //`define SET_TEMP_ASR(i) \
  //  __temp[i] = in_amount[i - 1] \
  //    ? {{`GET_MODDED_I(i){in_to_shift[__MSB_POS__DATA_INOUT]}}, \
  //    __temp[i - 1][__MSB_POS__DATA_INOUT : `GET_MODDED_I(i)]} \
  //    : __temp[i - 1][__MSB_POS__DATA_INOUT : 0]
  def setTempAsr(i: Int): Unit = {
    temp(i) := Mux[UInt](
      io.inpAmount(i - 1),
      Cat(
        //{{`GET_MODDED_I(i){in_to_shift[__MSB_POS__DATA_INOUT]}}, \
        //__temp[i - 1][__MSB_POS__DATA_INOUT : `GET_MODDED_I(i)]}
        //U(getModdedI(i) bits, default -> io.inpData.msb),
        Mux(
          io.inpData.msb,
          U(getModdedI(i) bits, default -> True),
          U(getModdedI(i) bits, default -> False),
        ),
        temp(i - 1)(dataIoMsbPos downto getModdedI(i)),
      ).asUInt,
      temp(i - 1)(dataIoMsbPos downto 0)
    )
  }

  
  //`define COMPARE_BIT_SHIFT_IN_AMOUNT \
  //  in_amount[__MSB_POS__DATA_INOUT:__LOG2__WIDTH__DATA_INOUT]
  //def cmpBitShiftInpAmount = (
  //  io.inpAmount(dataIoMsbPos downto dataIoLog2Width) =/= 0x0
  //)
  when (io.inpAmount(dataIoMsbPos downto dataIoLog2Width) =/= 0) {
    for (i <- 1 until outpDataIdx) {
      temp(i) := 0x0
    }
    temp(outpDataIdx) := 0x0
  } otherwise {
    for (i <- 1 until dataIoLog2Width + 1) {
      if (kind == BarrelShifter.kindLsl) {
        setTempLsl(i)
      } else if (kind == BarrelShifter.kindLsr) {
        setTempLsr(i)
      } else /* if (kind == BarrelShifter.kindAsr) */ {
        setTempAsr(i)
      }
    }
  }
}
object LslToSystemVerilog extends App {
  def wordWidth = 32 
  SpinalConfig(svInterface=true).generateSystemVerilog(BarrelShifter(
    wordWidth=wordWidth,
    kind=BarrelShifter.kindLsl,
  ))
}
object LsrToSystemVerilog extends App {
  def wordWidth = 32 
  SpinalConfig(svInterface=true).generateSystemVerilog(BarrelShifter(
    wordWidth=wordWidth,
    kind=BarrelShifter.kindLsr,
  ))
}
object AsrToSystemVerilog extends App {
  def wordWidth = 32 
  SpinalConfig(svInterface=true).generateSystemVerilog(BarrelShifter(
    wordWidth=wordWidth,
    kind=BarrelShifter.kindAsr,
  ))
}

case class ClzIo(
  wordWidth: Int
) extends Bundle {
  //--------
  val inpData = in UInt(wordWidth bits)
  val outpData = out UInt(log2Up(wordWidth) + 1 bits)
  //--------
}
case class Clz(
  wordWidth: Int,
) extends Component {
  //--------
  assert(
    wordWidth == 8
    || wordWidth == 16
    || wordWidth == 32
    || wordWidth == 64
  )
  //--------
  val io = ClzIo(wordWidth=wordWidth)
  //--------
  //if (wordWidth == 8) {
  //} else if (wordWidth == 16) {
  //} else if (wordWidth == 32) {
  //} else /* if (wordWidth == 64) */ {
  //}
  //val temp = Vec.fill(
  //  log2Up(wordWidth) //+ 1
  //)(
  //  UInt(wordWidth + 1 bits)
  //)
  //val temp = new ArrayBuffer[UInt]()
  def tempFinalSize = log2Up(wordWidth) + 1 //- 1
  val temp = Vec.fill(tempFinalSize)(
    UInt(wordWidth bits)
  )

  for (idx <- 0 until temp.size) {
    temp(idx) := 0x0
  }

  io.outpData := 0x0
  when (io.inpData === 0) {
    io.outpData := wordWidth
  } otherwise {
    // binary search for the leading one
    var idx: Int = (
      //temp.size
      tempFinalSize - 1 //+ 1
    )
    //def prevTemp = temp(idx)
    //def currTemp = temp(idx - 1)
    def currOutp = io.outpData(idx)
    while (idx >= 0) {
      //if (idx - 1 > 0) {
      //  temp += (
      //    UInt(wordWidth bits)
      //    .setName(s"temp_${idx}")
      //  )
      //}
      def hiRange = (
        (1 << (idx + 1)) - 1,
        1 << (idx + 1 - 1),
      )
      def loRange = (
        hiRange._2 - 1,
        0,
      )
      if (idx == tempFinalSize - 1 /*+ 1*/) {
        temp(idx - 1) := io.inpData
        currOutp := False
      } else if (idx == 0) {
        currOutp := !temp(idx)(1)
      } else {
        //println(s"${hiRange}, ${loRange}; ${idx} ${1 << idx}")
        def prevTemp = temp(idx) //temp(temp.size - 2)
        temp(idx - 1) := Mux(
          prevTemp(hiRange._1 downto hiRange._2) =/= 0,
          prevTemp(hiRange._1 downto hiRange._2),
          Cat(True, prevTemp(loRange._1 downto loRange._2)).asUInt,
        ).resized
        currOutp := temp(idx - 1)(1 << idx)
      }
      idx -= 1
    }
  }
  //for (idx <- 0 until temp.size) {
  //  
  //}
}

object ClzToSystemVerilog extends App {
  def wordWidth = 32 
  Config.spinal.generateSystemVerilog(Clz(
    wordWidth=wordWidth
  ))
}
