package libcheesevoyage

import spinal.core._
import spinal.lib._

// Hardware definition
//case class MyTopLevel() extends Component {
//  val io = new Bundle {
//    val cond0 = in  Bool()
//    val cond1 = in  Bool()
//    val flag  = out Bool()
//    val state = out UInt(8 bits)
//  }
//
//  val counter = Reg(UInt(8 bits)) init 0
//
//  when(io.cond0) {
//    counter := counter + 1
//  }
//
//  io.state := counter
//  io.flag := (counter === 0) | io.cond1
//}
case class MyTopLevel(width: Int=2, size: Int=4) extends Component {
  val io = new Bundle {
    val va = VectorAluIo(width=width, size=size)
    //val va = new Bundle {
    //  val vec = Vec(AluIo(width=width), size)
    //}
    val sel = in UInt(log2Up(size) bits)
    val result = out UInt(width bits)
  }

  val dut = VectorAlu(width=width, size=size)
  io.va.vec <> dut.io.vec
  //dut.io.inp := io.va.inp
  //io.va.outp := dut.io.outp
  //io.result := io.va.outp(io.sel).result
  io.result := io.va.vec(io.sel).outp.result

  //dut.io.inp := io.inp
  //io.outp := dut.io.outp
}
//case class Vec2UI(
//  width: Int,
//) extends Bundle {
//  val x, y = UInt(width bits)
//}
//
//case class MyTopLevel(
//  payloadWidth: Int=4,
//  optIncludeValidBusy: Boolean=false,
//  optIncludeReadyBusy: Boolean=false,
//  optPassthrough: Boolean=false,
//  optTieIfwdValid: Boolean=false,
//) extends Component {
//  val io = new PipeSkidBufIo(
//    dataType=Vec2UI(width=payloadWidth),
//    optIncludeValidBusy=optIncludeValidBusy,
//    optIncludeReadyBusy=optIncludeReadyBusy,
//  )
//  val myPipeSkidBuf = PipeSkidBuf(
//    dataType=Vec2UI(width=payloadWidth),
//    optIncludeValidBusy=optIncludeValidBusy,
//    optIncludeReadyBusy=optIncludeReadyBusy,
//    optPassthrough=optPassthrough,
//    optTieIfwdValid=optTieIfwdValid,
//  )
//  io <> myPipeSkidBuf.io
//}
//
object MyTopLevelVerilog extends App {
  Config.spinal.generateVerilog(MyTopLevel())
}

object MyTopLevelVhdl extends App {
  Config.spinal.generateVhdl(MyTopLevel())
  //val report = SpinalVhdl(new MyTopLevel())
  //report.printPruned()
}
