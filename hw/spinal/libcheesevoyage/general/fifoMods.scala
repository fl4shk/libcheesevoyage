package libcheesevoyage

import scala.math
import spinal.core._
import spinal.lib._
import spinal.core.formal._

case class FifoInp[
  T <: Data
](
  dataType: HardType[T],
) extends Bundle {
  //--------
  val wrEn = Bool()
  val wrData = dataType()
  val rdEn = Bool()
  //--------
}
case class FifoOutp[
  T <: Data
](
  dataType: HardType[T],
) extends Bundle {
  //--------
  val rdData = dataType()

  val empty = Bool()
  val full = Bool()
  //--------
}
case class FifoIo[
  T <: Data
](
  dataType: HardType[T],
) extends Bundle {
  //--------
  val inp = in(FifoInp(dataType()))
  val outp = out(FifoOutp(dataType()))
  //--------
}

case class Fifo[
  T <: Data
](
  dataType: HardType[T],
  depth: Int,
) extends Component {
  //--------
  val io = FifoIo(dataType=dataType())
  //--------
  // Local variables
  val inp = io.inp
  //val outp = io.outp
  val outp = Reg(FifoOutp(dataType()))
  outp.init(outp.getZero)
  io.outp := outp

  //loc = Blank()

  val loc = new Area {
    //loc.arr = Array([
    //  Signal(io.shape()) for _ in range(io.SIZE())
    //])
    //val arr = Vec(dataType(), depth)
    //loc.arr = Array(
    //	[
    //		Signal(io.shape()) for _ in range(io.SIZE())
    //	],
    //	attrs={"ram_style": "ultra"}
    //)
    val arr = new Mem(dataType(), depth) addAttribute("ram_style", "ultra")
    arr.addAttribute("keep")

    //loc.ptrWidth = width_from_arg(io.SIZE())
    val ptrWidth = log2Up(depth)
    //val ptrWidth = depth

    val tail = Reg(UInt(ptrWidth bits)) init(0x0)
    val head = Reg(UInt(ptrWidth bits)) init(0x0)

    //val tailPlus1 = tail + 0x1
    //val headPlus1 = head + 0x1
    val tailPlus1 = UInt((ptrWidth + 1) bits)
    tailPlus1 := (tail + U"1'b1".resized).resized
    val headPlus1 = UInt((ptrWidth + 1) bits)
    headPlus1 := (head + U"1'b1".resized).resized

    val incrTail = UInt(ptrWidth bits)
    val incrHead = UInt(ptrWidth bits)

    val nextEmpty = Bool()
    val nextFull = Bool()

    val nextTail = UInt(ptrWidth bits)
    val nextHead = UInt(ptrWidth bits)

    //clk, loc.rst = ClockSignal(), ResetSignal()
    //loc.rst = io.rst

    //loc.curr_en_cat = Signal(2)
  }

  //GenerationFlags.formal {
  //val haveFormal = {
  //  GenerationFlags.formal {
  //    true
  //  }
  //  false
  //}
    //loc.formal = Blank()
  val locFormal = new Area {
    val lastTailVal = Reg(dataType()) init(dataType().getZero)
    //val lastTailVal = Reg(UInt(loc.ptrWidth bits)) init(0x0)
    val testHead = UInt(loc.ptrWidth bits)
    //val empty = Bool()
    //val full = Bool()
    //val wdCnt = Reg(dataType()) init(dataType().getZero)
    //val wdCnt = Reg(UInt(loc.ptrWidth bits)) init(0x0)
    val wdCnt = Reg(UInt(32 bits)) init(0x0)
  }
  //}
  //--------
  GenerationFlags.formal {
    //m.d.sync
    //+= [
      locFormal.lastTailVal := loc.arr.readAsync(loc.tail)
      locFormal.wdCnt := locFormal.wdCnt - 0x10
    //]
    //m.d.comb
    //+= [
      locFormal.testHead := (loc.head + 0x1) % depth
    //]
  }
  //--------
  // Combinational logic

  //m.d.comb 
  //+= [
    //loc.incrTail := Mux[UInt](
    //  loc.tailPlus1 < depth, (loc.tail + 0x1), 0x0
    //)
    //loc.incrHead := Mux[UInt](
    //  loc.headPlus1 < depth, (loc.head + 0x1), 0x0
    //)
    //val tempDepth = UInt((loc.ptrWidth + 1) bits) 
    //val tempDe
    //tempDepth := depth & ((1 << (loc.ptrWidth + 1)) - 1)
    //when (loc.tailPlus1 < tempDepth) 
    when (loc.tailPlus1 < depth) {
      loc.incrTail := loc.tail + 0x1
    } otherwise {
      loc.incrTail := 0x0
    }
    //when (loc.headPlus1.resized < tempDepth.resized) 
    when (loc.headPlus1 < depth) {
      loc.incrHead := loc.head + 0x1
    } otherwise {
      loc.incrHead := 0x0
    }

    loc.nextEmpty := loc.nextHead === loc.nextTail
    //loc.nextFull := ((loc.nextHead + 0x1) === loc.nextTail)

    //loc.currEnCat := (Cat(inp.rdEn, inp.wrEn))
  //]

  when (inp.rdEn & ~outp.empty) {
    //m.d.comb += 
    loc.nextTail := loc.incrTail
  } otherwise {
    //m.d.comb +=
    loc.nextTail := loc.tail
  }

  when (inp.wrEn & ~outp.full) {
    //m.d.comb += [
    loc.nextHead := loc.incrHead
    loc.nextFull := (loc.incrHead + 0x1) === loc.nextTail
    //]
  } otherwise {
    //m.d.comb += [
    loc.nextHead := loc.head
    loc.nextFull := loc.incrHead === loc.nextTail
    //]
  }

  //GenerationFlags.formal {
  //	//m.d.comb += [
  //		cover(outp.empty)
  //		cover(outp.full)
  //		cover((~outp.empty) & (~outp.full))
  //	//]
  //}
  //--------
  // Clocked behavioral code
  //GenerationFlags.formal {
  //  locFormal.pastValid = Signal()
  //}

  //when (loc.rst):
  when (clockDomain.isResetActive) {
    //for elem in loc.arr:
    //	m.d.sync += elem := (io.shape()())

    //m.d.sync += [
      loc.tail := 0x0
      loc.head := 0x0

      //outp.rdData := (io.shape()()),

      outp.empty := True
      outp.full := False
    //]
    //if self.FORMAL():
      //m.d.sync += locFormal.pastValid := (0b1)

  } otherwise { // when (~clockDomain.isResetActive)
    //--------
    //m.d.sync += [
      outp.empty := loc.nextEmpty
      outp.full := loc.nextFull
      loc.tail := loc.nextTail
      loc.head := loc.nextHead
    //]

    when (inp.rdEn & (~outp.empty)) {
      //m.d.sync += 
      outp.rdData := loc.arr.readAsync(loc.tail.resized)
    }
    when (inp.wrEn & (~outp.full)) {
      //m.d.sync += 
      //loc.arr(loc.head) := inp.wrData
      loc.arr.write(loc.head, inp.wrData)
    }
    //--------
    //if self.FORMAL():
    GenerationFlags.formal {
      when (pastValidAfterReset) {
        //m.d.sync += [
          assert(outp.empty === past(loc.nextEmpty))
          assert(outp.full === past(loc.nextFull))
          assert(loc.tail === past(loc.nextTail))
          assert(loc.head === past(loc.nextHead))
        //]
        when (past(inp.rdEn)) {
          when (past(outp.empty)) {
            //m.d.sync += [
              //assert(stable(outp.empty))
              assert(stable(loc.tail))
            //]
          } otherwise { // when (~past(outp.empty)):
            //when (~past(inp.wrEn)):
            //m.d.sync += [
              //assert(outp.rdData
              //  === loc.arr.readAsync(past(loc.tail).resized))
              assert(outp.rdData === locFormal.lastTailVal)
            //]
          }
        }
        when (past(inp.wrEn)) {
          when (past(outp.full)) {
            //m.d.sync += [
              assert(stable(loc.head))
            //]
          }
          //otherwise {// when (~past(outp.full)):
          //	//m.d.sync += [
          //		assert(past(inp.wrData))
          //	//]
          //}
        }
        //val catVal = Bits(2 bits)
        //catVal(0) := outp.empty
        //catVal(1) := outp.full
        switch (Cat(outp.full, outp.empty)) 
        //switch (catVal) 
        {
          is (B"00") {
          //m.d.sync \
          //+= [
            assume(loc.head =/= loc.tail)
            assume(locFormal.testHead =/= loc.tail)
          //]
          }
          is (B"01") {
          //m.d.sync \
          //+= [
            assert(loc.head === loc.tail)
          //]
          }
          is (B"10") {
          //m.d.sync \
          //+= [
            assert(locFormal.testHead === loc.tail)
          //]
          }
        }
        //m.d.sync += [
          assert(~(outp.empty & outp.full))
          //assume(~stable(inp.wrData))
          //assume(inp.wrData === locFormal.wdCnt)
        //]
      }
    }
    //--------
  }
  ////--------
}

case class AsyncReadFifo[
  T <: Data
](
  dataType: HardType[T],
  depth: Int,
) extends Component {
  //--------
  val io = FifoIo(dataType=dataType())
  //--------
  // Local variables
  val inp = io.inp
  val outp = io.outp
  //--------
  val loc = new Area {
    val empty = Reg(Bool())
    val full = Reg(Bool())
    //arr = Array([
    //  Signal(bus.shape()) for _ in range(bus.SIZE())
    //])
    val arr = Mem(dataType(), depth) addAttribute("ram_style", "ultra")
    arr.addAttribute("keep")
    //arr = Array(
    //  [
    //    Signal(bus.shape()) for _ in range(bus.SIZE())
    //  ],
    //  attrs={"ram_style": "ultra"}
    //)
    //m = Blank()
    //m.submodules.mwrap = m.mwrap = MemWrapper(
    //  shape=bus.shape(),
    //  depth=bus.SIZE(),
    //  init=[0x0 for _ in range(bus.SIZE())],
    //  attrs=mem_attrs,
    //)

    val ptrWidth = log2Up(depth)
    val ptrWidthPlus1 = ptrWidth + 1
    //val ptrWidthPlus1 = loc.ptrWidthPlus1
    val uintDepth = U(f"$ptrWidthPlus1'd$depth")

    val head = Reg(UInt(ptrWidth bits)) init(0x0)
    val tail = Reg(UInt(ptrWidth bits)) init(0x0)

    val nextHead = UInt(ptrWidth bits)
    val nextTail = UInt(ptrWidth bits)

    val tempTailPlus1 = UInt(ptrWidthPlus1 bits) addAttribute("keep")
    val tailPlus1 = (tail.resized + U(f"$ptrWidthPlus1'b1")).resized
    tempTailPlus1 := tailPlus1

    val tempHeadPlus1 = UInt(ptrWidthPlus1 bits) addAttribute("keep")
    val headPlus1 = (head.resized + U(f"$ptrWidthPlus1'b1")).resized
    tempHeadPlus1 := headPlus1 

    val tempNextHeadPlus1 = UInt(ptrWidthPlus1 bits) addAttribute("keep")
    val nextHeadPlus1 = (nextHead.resized + U(f"$ptrWidthPlus1'b1")).resized
    tempNextHeadPlus1 := nextHeadPlus1

    val tempOorNextHeadPlus1 = Bool() addAttribute("keep")
    val oorNextHeadPlus1 = nextHeadPlus1 >= uintDepth
    tempOorNextHeadPlus1 := oorNextHeadPlus1

    //val incrNextHead = UInt(ptrWidth bits)
    val tempIncrNextHead = UInt(ptrWidthPlus1 bits) addAttribute("keep")
    //val incrNextHead = Mux[UInt](
    //  oorNextHeadPlus1,
    //  0x0, nextHeadPlus1.resized
    //).resized
    //tempIncrNextHead := incrNextHead
    when (oorNextHeadPlus1) {
      tempIncrNextHead := 0x0
    } otherwise {
      tempIncrNextHead := nextHeadPlus1
    }
    val incrNextHead = tempIncrNextHead

    //val nextEmpty = Bool()
    //val nextFull = Bool()
    case class NextEmptyFull(
      tempNextEmpty: Bool,
      tempNextFull: Bool,
    ) {
    }
    val nextEmptyFull = NextEmptyFull(
      //tempNextEmpty=Mux[Bool](
      //  incrNextHead === nextTail,
      //  False,
      //  Mux(
      //    nextHead === nextTail,
      //    True,
      //    False,
      //  )
      //),
      //tempNextFull=Mux[Bool](
      //  incrNextHead === nextTail,
      //  True,
      //  Mux(
      //    nextHead === nextTail,
      //    False,
      //    False,
      //  )
      //)
      tempNextEmpty=(
        (incrNextHead =/= nextTail)
        & (nextHead === nextTail)
      ),
      tempNextFull=(
        (incrNextHead === nextTail)
      )
    )
    val nextEmpty = nextEmptyFull.tempNextEmpty
    val nextFull = nextEmptyFull.tempNextFull
    //when (loc.incrNextHead === loc.nextTail) {
    //  //m.d.comb += [
    //  loc.nextEmpty := False
    //  loc.nextFull := True
    //  //]
    //} elsewhen (loc.nextHead === loc.nextTail) {
    //  //m.d.comb += [
    //  loc.nextEmpty := True
    //  loc.nextFull := False
    //  //]
    //} otherwise {
    //  //m.d.comb += [
    //  loc.nextEmpty := False
    //  loc.nextFull := False
    //  //]
    //}

  }
  val locFormal = new Area {
    val lastTailVal = Reg(dataType()) init(dataType().getZero)
    lastTailVal.addAttribute("keep")
    val testHead = ((loc.head + 0x1) % depth)
  }
  GenerationFlags.formal {
    locFormal.lastTailVal := loc.arr.readAsync(loc.tail)
  }
  //--------
  // Combinational logic

  //when((~loc.empty) & inp.rdEn):
  // m.d.comb += outp.rdData := (loc.arr(loc.tail))

  //m.d.comb += [
  outp.rdData := loc.arr.readAsync(loc.tail)
  outp.empty := loc.empty
  outp.full := loc.full
  //]

  // Compute `loc.nextHead` and write into the FIFO if it's not full
  //loc.headPlus1 = loc.head + 0x1
  when (clockDomain.isResetActive) {
    //m.d.comb += [
    loc.nextHead := 0x0
    loc.nextTail := 0x0
    //]
  } otherwise { // when (~loc.rst)
    when ((~outp.full) & inp.wrEn) {
      //m.d.sync += loc.arr(loc.head) := (inp.wrData)

      when (loc.headPlus1 >= loc.uintDepth) {
        //m.d.comb += 
        loc.nextHead := 0x0
      } otherwise {
        //m.d.comb += 
        loc.nextHead := loc.headPlus1.resized
      }
    } otherwise {
      //m.d.comb += 
      loc.nextHead := loc.head
    }

    // Compute `loc.nextTail`
    //loc.tailPlus1 = loc.tail + 0x1
    when ((~outp.empty) & inp.rdEn) {
      when (loc.tailPlus1 >= loc.uintDepth) {
        //m.d.comb +=
        loc.nextTail := 0x0
      } otherwise {
        //m.d.comb += 
        loc.nextTail := loc.tailPlus1.resized
      }
    } otherwise {
      //m.d.comb += 
      loc.nextTail := loc.tail
    }

    // Compute `loc.nextEmpty` and `loc.nextFull`
    //loc.nextHeadPlus1 = loc.nextHead + 0x1
    //when (loc.nextHeadPlus1 >= loc.uintDepth) {
    //  //m.d.comb += 
    //  loc.incrNextHead := 0x0
    //} otherwise {
    //  //m.d.comb += 
    //  loc.incrNextHead := loc.nextHeadPlus1.resized
    //}

    //when (loc.incrNextHead === loc.nextTail) {
    //  //m.d.comb += [
    //  loc.nextEmpty := False
    //  loc.nextFull := True
    //  //]
    //} elsewhen (loc.nextHead === loc.nextTail) {
    //  //m.d.comb += [
    //  loc.nextEmpty := True
    //  loc.nextFull := False
    //  //]
    //} otherwise {
    //  //m.d.comb += [
    //  loc.nextEmpty := False
    //  loc.nextFull := False
    //  //]
    //}
  }
  ////if self.FORMAL():
  //// m.d.comb \
  //// += [
  ////   cover(pastValid)
  ////   //cover(pastValid & outp.empty),
  ////   //cover(pastValid & outp.full),
  ////   //cover(pastValid & (~outp.empty)
  ////   // & (~outp.full)),
  //// )
  ////--------
  //// Sequential logic
  ////when (loc.rst):
  //// if self.FORMAL():
  ////   m.d.sync += pastValid := (0b1)
  ////if self.FORMAL():
  ////GenerationFlags.formal {
  ////  m.d.sync += pastValid := (0b1)
  ////}

  //m.d.sync += [
  loc.head := loc.nextHead
  loc.tail := loc.nextTail
  loc.empty := loc.nextEmpty
  loc.full := loc.nextFull
  //]
  //when (clockDomain.isResetActive) {
  // //--------
  // //m.d.sync += [
  //   loc.head := 0x0
  //   loc.tail := 0x0

  //   outp.empty := True
  //   outp.full := False
  // //]
  //}
  // //--------
  //otherwise // when(~clockDomain.isResetActive):
  when (~clockDomain.isResetActive) {
    //--------

    // Write into the FIFO
    when ((~outp.full) & inp.wrEn) {
      //m.d.sync += 
      loc.arr.write(loc.head, inp.wrData)
    }
    //--------
    //if self.FORMAL():
    GenerationFlags.formal {
      //m.d.comb += cover(pastValid)
      when (pastValidAfterReset) {
        //m.d.sync += [
        // cover(outp.empty)
        // cover(outp.full)
        // cover(~(outp.empty & outp.full))
        //)

        //m.d.sync += [
        //assert(loc.head === past(loc.nextHead)),
        //assert(loc.tail === past(loc.nextTail)),
        //assert(outp.empty === past(loc.nextEmpty)),
        //assert(outp.full === past(loc.nextFull)),
        assert(~(outp.empty & outp.full))

        assert(outp.rdData === loc.arr(loc.tail))
        //assert(past(outp.rdData) === locFormal.lastTailVal)
        //]

        when (past(inp.rdEn)) {
          when (past(outp.empty)) {
            //m.d.sync +=
            assert(stable(loc.tail))
          } otherwise { // when (~past(outp.empty))
            //m.d.sync += 
            assert(loc.tail
              === ((past(loc.tail) + 1) % depth))
          }
        } otherwise { // when (~past(inp.rdEn))
          //m.d.sync += [
          //assert(stable(outp.empty))
          assert(stable(loc.tail))
          //]
        }

        when (past(inp.wrEn)) {
          when (past(outp.full)) {
            //m.d.sync +=
            assert(stable(loc.head))
          } otherwise { // when (~past(outp.full))
            //m.d.sync +=
            assert(loc.head === ((past(loc.head) + 1) % depth))
          }
        } otherwise { // when (~past(inp.wrEn))
          //m.d.sync += [
          //assert(stable(outp.full))
          assert(stable(loc.head))
          //]
        }


        switch (Cat(outp.full, outp.empty)) {
          // neither full nor empty
          is (B"00") {
            //m.d.sync += [
            assert(loc.head =/= loc.tail)
            assert(locFormal.testHead =/= loc.tail)
            //]
          }
          // empty
          is (B"01") {
            //m.d.sync += [
            assert(loc.head === loc.tail)
            //]
          }
          // full
          is (B"10") {
            //m.d.sync += [
            assert(locFormal.testHead === loc.tail)
            //]
          }
        }

        // empty
        when (loc.head === loc.tail) {
          //m.d.sync += 
          //assert(outp.empty & (~outp.full))
          assert(outp.empty)
          assert(~outp.full)
        } elsewhen (locFormal.testHead === loc.tail) {
          //m.d.sync += 
          //assert((~outp.empty) & outp.full)
          assert(~outp.empty)
          assert(outp.full)
        } otherwise {
          //m.d.sync += 
          //assert((~outp.empty) & (~outp.full))
          assert(~outp.empty)
          assert(~outp.full)
        }
      }
    //--------
    }
  }
  //--------

  //--------
}
