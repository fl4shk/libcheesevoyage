package libcheesevoyage.general
import spinal.core._
//import spinal.lib.bus.tilelink
import spinal.lib._
import spinal.lib.misc.pipeline._
import spinal.lib.bus.amba4.axi._
//import spinal.lib.bus.avalon._
//import spinal.lib.bus.tilelink
//import spinal.core.fiber.Fiber
import scala.collection.mutable.ArrayBuffer
import libcheesevoyage.general._
//import libcheesevoyage.general.PipeSkidBuf
//import libcheesevoyage.general.PipeSkidBufIo
////import libcheesevoyage.general.PipeSimpleDualPortMem
//import libcheesevoyage.general.FpgacpuRamSimpleDualPort
import libcheesevoyage.math.LongDivPipelined

case class AddWithCarryIo() extends Bundle {
  //--------
  val inpA = in Bool()
  val inpB = in Bool()
  val inpCarry = in Bool()
  //--------
  val outpSum = out Bool()
  val outpCarry = out Bool()
  //--------
}

case class AddWithCarry() extends Component {
  //--------
  val io = AddWithCarryIo()
  //--------
  val tempSum = UInt(2 bits)
  tempSum := (
    Cat(False, io.inpA).asUInt
    + Cat(False, io.inpB).asUInt
    + Cat(False, io.inpCarry).asUInt
  )
  io.outpCarry := tempSum(1)
  io.outpSum := tempSum(0)
  //Cat(outpCarry, outpSum).asUInt := 
  //--------
}

case class PipelinedAdderIo(
  dataWidth: Int,
) extends Bundle {
  //--------
  val inpA = in UInt(dataWidth bits)
  val inpB = in UInt(dataWidth bits)
  val inpCarry = in Bool()
  //--------
  val outpSum = out UInt(dataWidth bits)
  val outpCarry = out Bool()
  //--------
}

case class PipelinedAdder(
  dataWidth: Int,
) extends Component {
  //--------
  val io = PipelinedAdderIo(dataWidth=dataWidth)
  //--------
  val adcArr = Array.fill(dataWidth)(AddWithCarry())
  val tempA = Payload(UInt(dataWidth bits))
  val tempB = Payload(UInt(dataWidth bits))
  val tempSum = Array.fill(dataWidth)(Payload(Bool()))
  val tempCarry = Array.fill(dataWidth + 1)(Payload(Bool()))

  //val n0, n1, n2, n3 = Node()
  //val s01 = StageLink(n0, n1)
  //val s12 = StageLink(n1, n2)
  //val s23 = StageLink(n2, n3)

  //n0.setAlwaysValid()
  //n3.setAlwaysReady()
  //n0(tempA) := io.inpA
  //n0(tempB) := io.inpB
  //n0(tempSum) := 0x0
  //n0(tempCarry) := io.inpCarry
  //io.outpSum := n3(tempSum)
  //io.outpCarry := n3(tempCarry)
  //Builder(s01, s12, s23)

  val nArr = Array.fill(dataWidth + 1)(Node())
  val sArr = new ArrayBuffer[StageLink]()
  val s2mArr = new ArrayBuffer[S2MLink]()
  val cArr = new ArrayBuffer[CtrlLink]()
  val linkArr = new ArrayBuffer[Link]()
  for (idx <- 0 until dataWidth) {
    sArr += StageLink(
      up=nArr(idx),
      down=Node()
    )
    linkArr += sArr.last
    s2mArr += S2MLink(
      up=sArr.last.down,
      down=Node(),
    )
    linkArr += s2mArr.last
    cArr += CtrlLink(
      up=s2mArr.last.down,
      down=nArr(idx + 1),
    )
    linkArr += cArr.last
  }

  nArr(0).valid := True
  nArr.last.ready := True
  nArr(0)(tempA) := io.inpA
  nArr(0)(tempB) := io.inpB
  //nArr(0)(tempSum(0)) := U(
  //  dataWidth bits,
  //  0 -> adcArr(0).io.outpSum,
  //  default -> False
  //)
  nArr(0)(tempCarry(0)) := io.inpCarry
  //val tempSum2 = UInt(dataWidth bits)
  //io.outpSum := tempSum2 //nArr.last(tempSum.last)
  for (idx <- 0 until dataWidth) {
    io.outpSum(idx) := nArr(idx + 1)(tempSum(idx))
  }
  io.outpCarry := nArr.last(tempCarry.last)
  for (idx <- 0 until nArr.size - 1) {
    //--------
    adcArr(idx).io.inpA := nArr(idx)(tempA)(idx)
    adcArr(idx).io.inpB := nArr(idx)(tempB)(idx)
    adcArr(idx).io.inpCarry := (
      //if (idx == 0) {
      //  //nArr(idx)(tempCarry(idx))
      //  io.inpCarry
      //} else { // if (idx > 0)
      //  nArr(idx)(
      //    //// previous stage's output carry
      //    //tempCarry(idx - 1)
      //  )
      //}
      nArr(idx)(tempCarry(idx))
    )
    nArr(idx + 1)(tempSum(idx)) := adcArr(idx).io.outpSum
    nArr(idx + 1)(tempCarry(idx + 1)) := adcArr(idx).io.outpCarry
    //--------
  }
  //--------
  //Builder(linkArr(0), linkArr.last)
  //Builder(linkArr(0))
  Builder(linkArr.toSeq)
  //--------
}
object PipelinedAdderConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    ),
    onlyStdLogicVectorAtTopLevelIo=true,
  )
}

object PipelinedAdderToVerilog extends App {
  PipelinedAdderConfig.spinal.generateVerilog(
    PipelinedAdder(
      dataWidth=(
        //4
        3
      ),
    )
  )
}
