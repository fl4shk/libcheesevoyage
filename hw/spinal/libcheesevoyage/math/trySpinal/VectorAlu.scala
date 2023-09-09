package libcheesevoyage.math.trySpinal

import spinal.core._
import spinal.lib._

//case class VectorAluInp(width: Int, size: Int) extends Bundle {
//  val a_arr = Vec(dataType=UInt(width), size=size)
//  val b_arr = Vec(dataType=UInt(width), size=size)
//  val carry_vec = Bits(size)
//  val op_arr = Vec(dataType=AluOp(), size=size)
//}
//case class VectorAluOutp(width: Int, size: Int) extends Bundle {
//  val result_arr = Vec(dataType=UInt(width), size=size)
//  val carry_vec = Bits(size)
//}

//case class VectorAluIo(width: Int, size: Int) extends Bundle {
//  //val inp = in(VectorAluInp(width=width, size=size))
//  //val outp = out(VectorAluOutp(width=width, size=size))
//  val inp = in(Vec(AluInp(width=width), size))
//  val outp = out(Vec(AluOutp(width=width), size))
//}
//def VectorAluIo(width: Int, size: Int): Bundle {
//  return new Vec(AluIo(width=width), size)
//}
case class VectorAluIo(width: Int, size: Int) extends Bundle {
  val vec = Vec(AluIo(width=width), size)
}

case class VectorAlu(width: Int, size: Int) extends Component {
  //val io = new VectorAluIo(width=width, size=size)
  //val io = new Vec(AluIo(width=width), size)
  //val io = VectorAluIo(width=width, size=size)
  val io = VectorAluIo(width=width, size=size)

  val aluArr = Array.fill(size)(new Alu(width=width))

  for (idx <- 0 to size - 1) {
    //aluArr.io.inp.a := io.inp.a_arr(idx)
    //aluArr.io.inp.b := io.inp.b_arr(idx)
    //aluArr.io.inp.carry := io.inp.carry_vec(idx)
    //aluArr.io.inp.op := io.inp.op_arr(idx)

    //aluArr(idx).io.inp := io.inp(idx)
    //io.outp(idx) := aluArr(idx).io.outp 
    io.vec(idx) <> aluArr(idx).io
    //aluArr(idx).io.inp := io.vec(idx).inp
    //io.vec(idx).outp := aluArr(idx).io.outp
  }
}
