package libcheesevoyage.math
import libcheesevoyage.general._

import spinal.core._
import spinal.lib._
import scala.collection.mutable.ArrayBuffer

object LcvPriorityMux {
  def apply(
    data: Vec[UInt],
    select: UInt,
  ): UInt = (
    //Verilog: ((~data ^ select) + data) >> $bits(select)
    // returns msb data[i] bit that have select[i] == 1.
    //  if none select bits are high then returns 0
    (
      ((~data.asBits.asUInt ^ select) + data.asBits.asUInt)
      >> select.getWidth
    )
  )
}
object LcvSFindFirstElem {
  def apply[
    T <: Data
  ](
    self: Seq[T],
    condition: T => Bool,
  ): (Bool, T) = {
    if (self.size == 2) {
      val hits = self.map(condition(_))
      (
        //hitValid,
        hits.sFindFirst(_ === True)._1,
        Mux[T](
          hits(0),
          self(0),
          self(1),
        ),
      )
    } else if (self.size == 3) {
      val hits = self.map(condition(_))
      (
        //hitValid,
        hits.sFindFirst(_ === True)._1,
        //{
        //  val data = Vec[UInt](
        //    //self(0).asBits.asUInt,
        //    //self(1).asBits.asUInt,
        //    //self(2).asBits.asUInt,
        //    //U"2'd2",
        //    //U"2'd1",
        //    //U"2'd0",
        //  )
        //  val select = (
        //    Cat(hits(2), hits(1), hits(0)).asUInt
        //  )
        //  val myMux = LcvPriorityMux(
        //    data=data,
        //    select=select,
        //  )
        //  println(
        //    s"data.size: ${data.size} "
        //    + s"select.getWidth: ${select.getWidth} "
        //    + s"myMux.getWidth: ${myMux.getWidth}"
        //  )
        //  self(myMux.resized)
        //}
        Mux[T](
          hits(0),
          self(0),
          Mux[T](
            hits(1),
            self(1),
            self(2),
          )
        ),
      )
    } else if (self.size == 4) {
      val hits = self.map(condition(_))
      (
        //hitValid,
        hits.sFindFirst(_ === True)._1,
        {
          Mux[T](
            hits(0),
            self(0),
            Mux[T](
              hits(1),
              self(1),
              Mux[T](
                hits(2),
                self(2),
                self(3),
              )
            )
          )
        }
      )
    } else {
      //(hitValid, self(self.sFindFirst(condition)._2))
      val tempFindFirst = self.sFindFirst(condition)
      (tempFindFirst._1, self(tempFindFirst._2))
    }
  }
  //def apply[
  //  T <: Data
  //](
  //  self: Seq[T],
  //  condition: T => Bool,
  //  //includeLast: Boolean=false,
  //): (Bool, T) = {
  //  //val hitValid = self.map(condition(_)).reduceLeft(_ || _)
  //  //val hitValid
  //  val myHitValidMap = self.map(condition(_))
  //  val myHitValidVec = Vec.fill(self.size - 1)(Bool())
  //  for (idx <- 0 until self.size - 1) {
  //    myHitValidVec(idx) := myHitValidMap(idx) //self.map(condition(idx))
  //  }
  //  //val hitValid = Bool()
  //  val hitValid = (
  //    if (self.size > 4) (
  //      LcvFastOrR(
  //        myHitValidVec.asBits.asUInt
  //      )
  //    ) else (
  //      //myHitValidVec.orR
  //      myHitValidVec.reduceBalancedTree(_ || _)
  //    )
  //  )
  //  if (self.size == 2) {
  //    val hits = self.map(condition(_))
  //    (
  //      hitValid,
  //      Mux[T](
  //        hits(0),
  //        self(0),
  //        self(1),
  //      ),
  //    )
  //  } else if (self.size == 3) {
  //    val hits = self.map(condition(_))
  //    (
  //      hitValid,
  //      //{
  //      //  val data = Vec[UInt](
  //      //    //self(0).asBits.asUInt,
  //      //    //self(1).asBits.asUInt,
  //      //    //self(2).asBits.asUInt,
  //      //    //U"2'd2",
  //      //    //U"2'd1",
  //      //    //U"2'd0",
  //      //  )
  //      //  val select = (
  //      //    Cat(hits(2), hits(1), hits(0)).asUInt
  //      //  )
  //      //  val myMux = LcvPriorityMux(
  //      //    data=data,
  //      //    select=select,
  //      //  )
  //      //  println(
  //      //    s"data.size: ${data.size} "
  //      //    + s"select.getWidth: ${select.getWidth} "
  //      //    + s"myMux.getWidth: ${myMux.getWidth}"
  //      //  )
  //      //  self(myMux.resized)
  //      //}
  //      Mux[T](
  //        hits(0),
  //        self(0),
  //        Mux[T](
  //          hits(1),
  //          self(1),
  //          self(2),
  //        )
  //      ),
  //    )
  //  } else if (self.size == 4) {
  //    val hits = self.map(condition(_))
  //    (
  //      hitValid,
  //      {
  //        Mux[T](
  //          hits(0),
  //          self(0),
  //          Mux[T](
  //            hits(1),
  //            self(1),
  //            Mux[T](
  //              hits(2),
  //              self(2),
  //              self(3),
  //            )
  //          )
  //        )
  //      }
  //    )
  //  } else {
  //    (hitValid, self(self.sFindFirst(condition)._2))
  //  }
  //}
}
object LcvSFindFirst {
  def apply[
    T <: Data
  ](
    self: Seq[T],
    condition: T => Bool,
  ): (Bool, UInt) = {
    //val hitValid = self.map(condition(_)).reduceLeft(_ || _)
    //val hitValid
    //val myHitValidMap = self.map(condition(_))
    val myHitValidVec = Vec.fill(self.size - 1)(Bool())
    for (idx <- 0 until self.size - 1) {
      myHitValidVec(idx) := (
        condition(self(idx))
        //myHitValidMap(idx) //self.map(condition(idx))
      )
    }
    //val hitValid = Bool()
    val hitValid = (
      if (self.size > 4) (
        LcvFastOrR(
          myHitValidVec.asBits.asUInt
        )
      ) else (
        //myHitValidVec.orR
        myHitValidVec.reduceBalancedTree(_ || _)
      )
    )
    if (self.size == 2) {
      val hits = self.map(condition(_))
      (
        hitValid,
        {
          Mux[UInt](
            hits(0),
            U"1'd0",
            U"1'd1",
          )
        }
      )
    } else if (self.size == 3) {
      val hits = self.map(condition(_))
      (
        hitValid,
        {
          Mux[UInt](
            hits(0),
            U"2'd0",
            Mux[UInt](
              hits(1),
              U"2'd1",
              U"2'd2",
            )
          )
        }
      )
    } else if (self.size == 4) {
      val hits = self.map(condition(_))
      (
        hitValid,
        {
          Mux[UInt](
            hits(0),
            U"2'd0",
            Mux[UInt](
              hits(1),
              U"2'd1",
              Mux[UInt](
                hits(2),
                U"2'd2",
                U"2'd3",
              )
            )
          )
        }
      )
    } else {
      (hitValid, self.sFindFirst(condition)._2)
    }
  }
}

case class LcvMulAcc32Io(
  //aWidth: Int=27,
  //bWidth: Int=18,
  //otherWidth: Int=48,
  optIncludeClkRst: Boolean,
) extends Bundle {
  val clk = (optIncludeClkRst) generate (
    in(Bool())
  )
  val rst = (optIncludeClkRst) generate (
    in(Bool())
  )
  val a = in(SInt(/*27*/ 16 bits))
  val b = in(SInt(/*18*/ 16 bits))
  val c = in(SInt(/*48*/ 33 bits))
  val d = in(SInt(/*48*/ 33 bits))
  val e = in(SInt(/*48*/ 33 bits))
  val outp = out(SInt(/*48*/ 33 bits))
}
case class LcvMulAcc32(
  //aWidth: Int=27,
  //bWidth: Int=18,
  //otherWidth: Int=48,
) extends BlackBox {
  //this.addAttribute("use_dsp", "yes")
  val io = LcvMulAcc32Io(
    //aWidth=aWidth,
    //bWidth=bWidth,
    //otherWidth=otherWidth,
    optIncludeClkRst=false,
  )
  noIoPrefix()
  addRTLPath("./hw/verilog/LcvMulAcc.sv")
  //val pcout = SInt(48 bits)
  //pcout := ((io.a * io.b).resize(io.c.getWidth) + io.c)
  ////io.outp := (io.a * io.b + io.c).resize(io.outp.getWidth)
  //io.outp := (pcout + io.d + io.e)//.resize(io.outp.getWidth)
}

//case class LcvMulAcc32Del1Io(
//  //aWidth: Int=27,
//  //bWidth: Int=18,
//  //otherWidth: Int=48,
//) extends Bundle {
//  val clk = in(Bool())
//  val a = in(SInt(/*27*/ 16 bits))
//  val b = in(SInt(/*18*/ 16 bits))
//  val c = in(SInt(/*48*/ 33 bits))
//  val d = in(SInt(/*48*/ 33 bits))
//  val e = in(SInt(/*48*/ 33 bits))
//  val outp = out(SInt(/*48*/ 33 bits))
//}

case class LcvMulAcc32Del1(
) extends BlackBox {
  val io = LcvMulAcc32Io(optIncludeClkRst=true)
  noIoPrefix()
  addRTLPath("./hw/verilog/LcvMulAcc.sv")
  mapCurrentClockDomain(clock=io.clk, reset=io.rst)
  //ClockDomainTag(this.clockDomain)(
  //  io.a,
  //  io.b,
  //  io.c,
  //  io.d,
  //  io.e,
  //  io.outp
  //)
  setIoCd()
}

case class LcvAdcDel1Io(
  wordWidth: Int=32,
) extends Bundle {
  val clk = in(Bool())
  val inp = new Bundle {
    val a = in(SInt(wordWidth bits))
    val b = in(SInt(wordWidth bits))
    val carry = in(Bool())
  }
  //val do_inv = in(Bool())
  val outp = new Bundle {
    val sum_carry = out(SInt(wordWidth + 1 bits))
    //val carry = out(Bool())
  }
}
case class LcvAdcDel1(
  wordWidth: Int=32,
) extends BlackBox {
  val io = LcvAdcDel1Io(wordWidth=wordWidth)
  addGeneric("WIDTH", wordWidth)
  noIoPrefix()
  addRTLPath("./hw/verilog/LcvMulAcc.sv")
  mapCurrentClockDomain(clock=io.clk/*, reset=io.rst*/)
  setIoCd()
}
case class LcvSubDel1Io(
  wordWidth: Int=32,
) extends Bundle {
  val clk = in(Bool())
  val inp = new Bundle {
    val a = in(SInt(wordWidth bits))
    val b = in(SInt(wordWidth bits))
    //val carry = in(Bool())
  }
  //val do_inv = in(Bool())
  val outp = new Bundle {
    val sum_carry = out(SInt(wordWidth + 1 bits))
    //val carry = out(Bool())
  }
}
case class LcvSubDel1(
  wordWidth: Int=32,
) extends BlackBox {
  val io = LcvSubDel1Io(wordWidth=wordWidth)
  addGeneric("WIDTH", wordWidth)
  noIoPrefix()
  addRTLPath("./hw/verilog/LcvMulAcc.sv")
  mapCurrentClockDomain(clock=io.clk/*, reset=io.rst*/)
  setIoCd()
}
case class LcvCmpEqDel1Io(
  wordWidth: Int=32,
) extends Bundle {
  val clk = in(Bool())
  val a = in(SInt(wordWidth bits))
  val b = in(SInt(wordWidth bits))
  //val carry_in = in(Bool())
  //val do_inv = in(Bool())
  val outp_data = out(SInt(wordWidth + 1 bits))
}
case class LcvCmpEqDel1(
  wordWidth: Int=32,
) extends BlackBox {
  val io = LcvCmpEqDel1Io(wordWidth=wordWidth)
  addGeneric("WIDTH", wordWidth)
  noIoPrefix()
  addRTLPath("./hw/verilog/LcvMulAcc.sv")
  mapCurrentClockDomain(clock=io.clk/*, reset=io.rst*/)
  setIoCd()
}

object LcvFastOrR {
  def apply(
    self: UInt,
    optDsp: Boolean=false,
  ): Bool = {
    val q = Bool()
    val unusedSumOut = UInt(self.getWidth bits)
    val temp0 = (
      Cat(False, self).asUInt
    )
    val temp1 = (
      U(self.getWidth bits, default -> True)
    )
    val mulAcc = (optDsp) generate (
      LcvMulAcc32(
        //aWidth=self.getWidth + 1,
        //bWidth=temp0.getWidth + 1,
        //otherWidth=temp1.getWidth + 1
        //otherWidth=(temp1.getWidth + 1).max(48)
      )
    )
    if (optDsp) {
      mulAcc.io.a := (
        0x0
        //Cat(False, U(s"${self.getWidth}'d1")).asSInt.resize(
        //  mulAcc.io.a.getWidth
        //)
        //Cat(True).asSInt.resize(mulAcc.io.a.getWidth)
      )
      mulAcc.io.b := (
        //Cat(False, temp0).asSInt
        0x0
        //Cat(False, U(s"${self.getWidth}'d1")).asSInt.resize(
        //  mulAcc.io.b.getWidth
        //)
        //Cat(True).asSInt.resize(mulAcc.io.b.getWidth)
      )
      mulAcc.io.c := (
        //Cat(False, temp1).asSInt
        0x0
        //S(mulAcc.io.c.getWidth bits, default -> True)
        //Cat(False, temp0).asSInt.resize(mulAcc.io.d.getWidth)
      )
      mulAcc.io.d := (
        //Cat(False, temp0).asSInt.resize(mulAcc.io.d.getWidth)
        Cat(False, temp0).asSInt.resize(mulAcc.io.d.getWidth)
      )
      mulAcc.io.e := (
        Cat(False, temp1).asSInt.resize(mulAcc.io.e.getWidth)
      )
      (q, unusedSumOut) := (
        mulAcc.io.outp.asUInt.resize(unusedSumOut.getWidth + 1)
      )
    } else {
      (q, unusedSumOut) := (
        temp0 + temp1
      )
    }
    //(q, unusedSumOut) := (
    //  if (optDsp) (
    //    (
    //      U(s"${self.getWidth}'d1")
    //      * temp0
    //      + temp1
    //    ).resized
    //  ) else (
    //    temp0
    //    + temp1
    //  )
    //)
    q
  }
}
object LcvFastAndR {
  def apply(
    self: UInt,
    optDsp: Boolean=false,
  ): Bool = {
    val q = Bool()
    val unusedSumOut = UInt(self.getWidth bits)
    val temp0 = (
      Cat(False, self).asUInt
    )
    val temp1 = (
      U(self.getWidth + 1 bits, 0 -> True, default -> False)
    )
    val mulAcc = (optDsp) generate (
      LcvMulAcc32(
        //aWidth=self.getWidth + 1,
        //bWidth=temp0.getWidth + 1,
        //otherWidth=temp1.getWidth + 1
        //otherWidth=(temp1.getWidth + 1).max(48)
      )
    )
    if (optDsp) {
      mulAcc.io.a := (
        0x0
        //Cat(False, U(s"${self.getWidth}'d1")).asSInt.resize(
        //  mulAcc.io.a.getWidth
        //)
        //Cat(True).asSInt.resize(mulAcc.io.a.getWidth)
      )
      mulAcc.io.b := (
        //Cat(False, temp0).asSInt
        0x0
        //Cat(False, U(s"${self.getWidth}'d1")).asSInt.resize(
        //  mulAcc.io.b.getWidth
        //)
        //Cat(True).asSInt.resize(mulAcc.io.b.getWidth)
      )
      mulAcc.io.c := (
        //Cat(False, temp1).asSInt
        0x0
        //S(mulAcc.io.c.getWidth bits, default -> True)
        //Cat(False, temp0).asSInt.resize(mulAcc.io.d.getWidth)
      )
      mulAcc.io.d := (
        //Cat(False, temp0).asSInt.resize(mulAcc.io.d.getWidth)
        Cat(False, temp0).asSInt.resize(mulAcc.io.d.getWidth)
      )
      mulAcc.io.e := (
        Cat(False, temp1).asSInt.resize(mulAcc.io.e.getWidth)
      )
      (q, unusedSumOut) := (
        mulAcc.io.outp.asUInt.resize(unusedSumOut.getWidth + 1)
      )
    } else {
      (q, unusedSumOut) := (
        temp0 + temp1
      )
    }
    //(q, unusedSumOut) := (
    //  if (optDsp) (
    //    (
    //      U(s"${self.getWidth}'d1")
    //      * temp0
    //      + temp1
    //    ).resized
    //  ) else (
    //    temp0
    //    + temp1
    //  )
    //)
    q
  }
}
//object LcvFastCmp {
//  sealed trait Kind
//}

//case class LcvAddSubInp(
//  wordWidth: Int=32
//) extends Bundle {
//  val a = UInt(wordWidth bits)
//  val b = UInt(wordWidth bits)
//  val carry = Bool()
//}
//case class LcvAddSubCmp(
//) extends Bundle {
//  val eq = Bool()
//  val ne = Bool()
//
//  val ltu = Bool()
//  val geu = Bool()
//  val gtu = Bool()
//  val leu = Bool()
//
//  val lts = Bool()
//  val ges = Bool()
//  val gts = Bool()
//  val les = Bool()
//}
//
//case class LcvAddSubOutp(
//  wordWidth: Int=32
//) extends Bundle {
//  val sum = UInt(wordWidth bits)
//  val cmp = LcvAddSubCmp()
//}
//case class LcvAddSubDel1Io(
//  wordWidth: Int=32,
//) extends Bundle {
//  val inp = in(LcvAddSubInp(wordWidth=wordWidth))
//  val outp = out(LcvAddSubOutp(wordWidth=wordWidth))
//}
//
//object LcvCmpDel1 {
//  sealed trait Kind
//  object Kind {
//    case object UseFastCarryChain extends Kind
//    case object SubOrR extends Kind 
//  }
//}
//
//case class LcvCmpDel1(
//  wordWidth: Int=32
//) extends Component {
//  val io = LcvAddSubDel1Io(
//    wordWidth=wordWidth
//  )
//  def inp = io.inp
//  def outp = io.outp
//  val tempWidth = (
//    wordWidth + 1
//  )
//  val addDel1 = LcvAddDel1(
//    wordWidth=tempWidth
//  )
//}
object LcvFastCmpEq {
  //sealed trait Kind
  //object Kind {
  //  case object UseFastCarryChain extends Kind
  //  case object SubOrR extends Kind 
  //}
  def apply(
    left: UInt,
    right: UInt,
    //mulAccIo: LcvMulAcc32Io,
    //addIo: LcvAddDel1Io,
    cmpEqIo: LcvCmpEqDel1Io,
    optDsp: Boolean=false,
    optReg: Boolean=false,
    //kind: Kind=Kind.SubOrR
  ): (Bool, UInt) = {
    assert(
      left.getWidth == right.getWidth,
      f"leftWidth:${left.getWidth} != rightWidth:${right.getWidth}"
    )
    if (optDsp) {
      assert(
        optReg,
        f"with `optDsp == true`, `optReg == true` is required"
      )
    }
    //val q = Bool()
    //val unusedSumOut = UInt(left.getWidth bits)
    val tempWidth = (
      left.getWidth + 1
      //+ (
      //  kind match {
      //    case Kind.UseFastCarryChain => (
      //      1
      //    )
      //    case Kind.SubOrR => (
      //      0
      //    )
      //  }
      //)
    )
    val q = UInt(tempWidth bits)
    //val q = UInt(tempWidth bits)
    val temp0 = (
      if (optDsp) (
        left
      ) else (
        Cat(False, left ^ (~right)).asUInt
      )
      //kind match {
      //  case Kind.UseFastCarryChain => {
      //    Cat(False, left ^ (~right)).asUInt
      //  }
      //  case Kind.SubOrR => {
      //    Cat(False, left).asUInt
      //    //left
      //  }
      //}
    )
    val temp1 = (
      if (optDsp) (
        right
      ) else (
        U(tempWidth bits, 0 -> True, default -> False)
      )
      //kind match {
      //  case Kind.UseFastCarryChain => {
      //    U(tempWidth bits, 0 -> True, default -> False)
      //  }
      //  case Kind.SubOrR => {
      //    Cat(False, ~right).asUInt
      //    //~right
      //  }
      //}
    )
    val tempCarryIn = (
      if (optDsp) (
        False
      ) else (
        True
      )
      //kind match {
      //  case Kind.UseFastCarryChain => {
      //    False
      //  }
      //  case Kind.SubOrR => {
      //    True
      //  }
      //}
    )
    //val mulAcc = (optDsp && !optReg) generate (
    //  //if (!optReg) (
    //    LcvMulAcc32(
    //      //aWidth=self.getWidth + 1,
    //      //bWidth=temp0.getWidth + 1,
    //      //otherWidth=temp1.getWidth + 1
    //      //otherWidth=(temp1.getWidth + 1).max(48)
    //    )
    //  //) else (
    //  //  LcvMulAcc32Del1(
    //  //  )
    //  //)
    //)
    //val mulAccDel1 = (optDsp && optReg) generate (
    //  LcvMulAcc32Del1(
    //  )
    //)
    //val mulAccIo = (
    //  LcvMulAcc32Io(optIncludeClk=optReg)
    //)
    if (optDsp) {
      //mulAccIo.a := (
      //  0x0
      //)
      //mulAccIo.b := (
      //  0x0
      //)
      //mulAccIo.c := (
      //  0x0
      //)
      cmpEqIo.a := (
        temp0.asSInt.resize(cmpEqIo.a.getWidth)
      )
      cmpEqIo.b := (
        temp1.asSInt.resize(cmpEqIo.b.getWidth)
      )
      //addIo.carry_in := (
      //  tempCarryIn
      //)
      val tempOutp = (
        cmpEqIo.outp_data.asUInt.resize(q.getWidth)
      )
      //(q, unusedSumOut)
      q := (
        //if (optReg) (
        //  RegNext(next=tempOutp, init=tempOutp.getZero)
        //) else (
          tempOutp
        //)
      )
    }
    //else if (optDsp && optReg) {
    //  //mulAccDel1.mapCurrentClockDomain(mulAccDel1.io.clk)
    //  mulAccDel1.io.a := (
    //    0x0
    //  )
    //  mulAccDel1.io.b := (
    //    0x0
    //  )
    //  mulAccDel1.io.c := (
    //    0x0
    //  )
    //  mulAccDel1.io.d := (
    //    Cat(False, temp0).asSInt.resize(mulAccDel1.io.d.getWidth)
    //  )
    //  mulAccDel1.io.e := (
    //    Cat(False, temp1).asSInt.resize(mulAccDel1.io.e.getWidth)
    //  )
    //  val tempOutp = (
    //    mulAccDel1.io.outp.asUInt.resize(q.getWidth)
    //  )
    //  //(q, unusedSumOut)
    //  q := (
    //    //if (optReg) (
    //    //  RegNext(next=tempOutp, init=tempOutp.getZero)
    //    //) else (
    //      tempOutp
    //    //)
    //  )
    //}
    else {
      val tempOutp = (
        //Cat(
          //U(s"${temp0.getWidth}'d0"),
          (
            temp0
            + temp1
            + Cat(False, tempCarryIn).asUInt.resize(temp0.getWidth)
          )(temp0.getWidth - 1 downto 0).orR.asSInt.resize(
            q.getWidth
          ).asUInt
        //).asUInt
      )
      q := (
        if (optReg) (
          RegNext(next=tempOutp, init=tempOutp.getZero)
        ) else (
          tempOutp
        )
      )
    }
    //(q, unusedSumOut) := (
    //  if (optDsp) (
    //    (
    //      U(s"${left.getWidth}'d1")
    //      * temp0
    //      + temp1
    //    ).resized
    //  ) else (
    //    temp0
    //    + temp1
    //  )
    //)
    (q.msb, q)
    //kind match {
    //  case Kind.UseFastCarryChain => {
    //    (q.msb, q)
    //  }
    //  case Kind.SubOrR => {
    //    (!q(q.high - 1 downto 0).orR, q)
    //  }
    //}
  }
}
