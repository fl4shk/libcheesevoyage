package libcheesevoyage.general
import libcheesevoyage._

import spinal.core._
import spinal.lib._
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer

case class TestPsbIncrIo
//[
//  T <: Data
//]
(
  //dataType: HardType[T],
  dataWidth: Int,
) extends Bundle {
  val next = master Stream(UInt(dataWidth bits))
  val prev = slave Stream(UInt(dataWidth bits))
}

case class TestPsbIncr
//[
//  T <: Data
//]
(
  //dataType: HardType[T],
  dataWidth: Int,
) extends Component {
  //val io = TestPsbIncrIo(dataType())
  val io = TestPsbIncrIo(dataWidth=dataWidth)

  //io.next.payload := io.prev.payload + U"1'b1"
  //val skidBuf = new Stream(dataType())
  val skidBuf = new Stream(UInt(dataWidth bits))
  io.prev >/-> skidBuf
  //val tempPayload = UInt(io.prev.payload.asBits.getWidth bits)
  //tempPayload := io.prev.payload.asBits.asUInt + 1
  skidBuf.translateInto(io.next){
    //(o, i) => o.assignFromBits((i.asBits.asUInt + 1).asBits)
    (o, i) => {
      when (clockDomain.isResetActive) {
        o := 0
      } elsewhen (io.prev.fire) { // when (~clockDomain.isResetActive)
        o := i + 1
      } otherwise {
        o := 0
      }
    }
  }
  //io.next.payload.assignFromBits(tempPayload.asBits)
  //io.next <-/< io.prev
  //io.next >/-> io.prev
}

case class TestPsbIncrChainIo
//[
//  T <: Data
//]
(
  //dataType: HardType[T]
  dataWidth: Int,
) extends Bundle {
  //val inpData = in port dataType()
  //val outpData = out port dataType()
  val inpData = in port UInt(dataWidth bits)
  val outpData = out port UInt(dataWidth bits)
}

case class TestPsbIncrChain
//[
//  T <: Data
//]
(
  //dataType: HardType[T],
  dataWidth: Int,
  numStages: Int,
) extends Component {
  //val io = TestPsbIncrChainIo(dataType())
  val io = TestPsbIncrChainIo(dataWidth=dataWidth)

  //val m = new ArrayBuffer[TestPsbIncr[T]]()
  val m = new ArrayBuffer[TestPsbIncr]()
  for (idx <- 0 to numStages - 1) {
    //m += TestPsbIncr(dataType()) setName(f"TestPsbIncr_$idx")
    m += TestPsbIncr(dataWidth=dataWidth) setName(f"TestPsbIncr_$idx")
  }
  val firstMPrev = m(0).io.prev
  val lastMNext = m.last.io.next
  //m(0).io.prev.payload := io.inpData
  //io.outpData := m.last.io.next.payload
  firstMPrev.valid := True
  firstMPrev.payload := io.inpData
  io.outpData := lastMNext.payload

  //val prevLastReady = Reg(Bool()) init(False)
  //prevLastReady := lastMNext.ready
  //when (lastMNext.valid) {
  //  //when (!prevLastReady) {
  //  //  lastMNext.ready := True
  //  //} otherwise {
  //  //  lastMNext.ready := False
  //  //}
  //  lastMNext.ready := !prevLastReady
  //} otherwise {
  //  lastMNext.ready := False
  //}
  lastMNext.ready := True
  for (currIdx <- 0 to m.size - 2) {
    val nextIdx = currIdx + 1
    m(nextIdx).io.prev.connectFrom(m(currIdx).io.next)
  }
  GenerationFlags.formal {
    when (pastValidAfterReset) {
      //val oracleInpArr = new ArrayBuffer[T]()
      //val oracleOutpArr = new ArrayBuffer[T]()
      //val captInpDataArr = new ArrayBuffer[T]()
      val captInpDataArr = new ArrayBuffer[UInt]()
      val addedArr = new ArrayBuffer[UInt]()
      val nextFireArr = new ArrayBuffer[Bool]()
      for (idx <- 0 to m.size - 1) {
        //captInpDataArr += Reg(dataType()) init(dataType().getZero)
        captInpDataArr += Reg(UInt(dataWidth bits)) init(0x0)
        captInpDataArr.last.setName(f"captInpDataArr_$idx")
        captInpDataArr.last.addAttribute("keep")

        addedArr += UInt(dataWidth bits)
        addedArr.last.setName(f"addedArr_$idx")
        addedArr.last.addAttribute("keep")
        addedArr.last := captInpDataArr.last + idx + 1

        nextFireArr += Bool()
        nextFireArr.last.setName(f"nextFireArr_$idx")
        nextFireArr.last.addAttribute("keep")
        nextFireArr.last := m(idx).io.next.fire
      }
      for (idx <- 0 to m.size - 1) {
        if (idx == 0) {
          captInpDataArr(idx) := io.inpData
        } else {
          captInpDataArr(idx) := captInpDataArr(idx - 1)
        }
        when (m(idx).io.next.fire) {
          assert(
            m(idx).io.next.payload
            === addedArr(idx) //captInpDataArr(idx) + idx
          )
        }

        //if (idx == 0) {
        //  when (m(idx).io.prev.fire) {
        //    captInpDataArr(idx) := io.inpData
        //  }
        //} else {
        //  when (m(idx).io.prev.fire) {
        //    captInpDataArr(idx) := captInpDataArr(idx - 1)
        //  }
        //  when (m(idx).io.next.fire) {
        //    //if (idx == m.size - 1) {
        //    //  assert(
        //    //    m(idx).io.next.payload.asBits.asUInt
        //    //    === captInpDataArr(idx).asBits.asUInt + numStages
        //    //  )
        //    //} else { // if (idx > 0 && idx < m.size - 1)
        //      //assert(
        //      //  //m(idx).io.next.payload.asBits.asUInt
        //      //  //=== captInpDataArr(idx).asBits.asUInt + idx
        //      //  //m(idx).io.next.payload
        //      //  //=== captInpDataArr(idx) + idx
        //      //  m(idx).io.next.payload
        //      //  === addedArr(idx)
        //      //)
        //    //}
        //  }
        //}
      }
    }
  }
}

//case class TestPsbIncrChainTopLevel[
//  T <: Data
//](
//  dataType: HardType[T],
//  numStages: Int,
//) extends Component {
//}
//object TestPsbIncrChainTopLevelVerilog extends App {
//  //val report = SpinalVerilog(TestPsbIncrChain(
//  //  dataType=UInt(3 bits),
//  //  numStages=3,
//  //))
//  //report.printPruned()
//  Config.spinal.generateVerilog(TestPsbIncrChain(
//    dataType=UInt(3 bits),
//    numStages=3,
//  ))
//}
object TestPsbIncrChainFormal extends App {
  //type dataType = HardType[UInt]
  //val dataTypeWidthBits = 3 bits
  val dataWidth = 3
  val numStages = 3
  new SpinalFormalConfig(_keepDebugInfo=true)
    .withBMC(10)
    .withProve(10)
    .doVerify(new Component {
      val dut = FormalDut(TestPsbIncrChain(
        //dataType=UInt(dataTypeWidthBits),
        dataWidth=dataWidth,
        numStages=numStages
      ))
      val inpData = dut.io.inpData
      val outData = dut.io.outpData

      assumeInitial(clockDomain.isResetActive)
      assumeInitial(inpData === inpData.getZero)
      anyseq(inpData)
    })
}
