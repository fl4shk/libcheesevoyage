package libcheesevoyage.general
import libcheesevoyage._

import scala.math
import spinal.core._
import spinal.lib._
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer

//case class FifoInp[
//  T <: Data
//](
//  dataType: HardType[T],
//) extends Bundle {
//  //--------
//  val wrEn = Bool()
//  val wrData = dataType()
//  val rdEn = Bool()
//  //--------
//}
//case class FifoOutp[
//  T <: Data
//](
//  dataType: HardType[T],
//) extends Bundle {
//  //--------
//  val rdData = dataType()
//
//  val empty = Bool()
//  val full = Bool()
//  //--------
//}
object FifoMiscIo {
  def ptrWidth(depth: Int) = log2Up(depth)
  def amountWidth(depth: Int) = log2Up(depth + 1)
}
case class FifoMiscIo(
  depth: Int,
) extends Bundle {
  //val flush = in port Bool()
  val empty = out port Bool()
  val full = out port Bool()

  //def ptrWidth = log2Up(depth)
  //def amountWidth = log2Up(depth + 1)
  def ptrWidth = FifoMiscIo.ptrWidth(depth=depth)
  def amountWidth = FifoMiscIo.amountWidth(depth=depth)

  // How many elements can be pushed
  val amountCanPush = out port UInt(amountWidth bits)
  // How many elements can be popped
  val amountCanPop = out port UInt(amountWidth bits)
}
case class FifoIo[
  T <: Data
](
  dataType: HardType[T],
  depth: Int,
) extends Bundle //with IMasterSlave 
{
  //--------
  //val inp = in(FifoInp(dataType()))
  //val outp = out(FifoOutp(dataType()))

  val push = slave Stream(dataType()) // writes
  val pop = master Stream(dataType()) // reads

  //val sbIo = PipeSkidBufIo(
  //  dataType=dataType(),
  //  optIncludeBusy=false,
  //)
  //val push = sbIo.prev
  //val pop = sbIo.next
  //val misc = out(FifoMiscIo(depth=depth))
  val misc = FifoMiscIo(depth=depth)
  def ptrWidth = misc.ptrWidth
  def amountWidth = misc.amountWidth
  //--------
  //def asMaster(): Unit = {
  //  master(push)
  //  slave(pop)
  //  out(misc)
  //}
  //def asSlave(): Unit = {
  //  slave(push)
  //  master(pop)
  //  in(misc)
  //}
  //--------
}

case class Fifo[
  T <: Data
](
  dataType: HardType[T],
  depth: Int,
  arrRamStyle: String="ultra",
) extends Component {
  //--------
  val io = FifoIo(
    dataType=dataType(),
    depth=depth,
  )
  //--------
  // Local variables
  //val inp = io.inp
  ////val outp = io.outp
  //val outp = Reg(FifoOutp(dataType()))
  //outp.init(outp.getZero)
  //io.outp := outp
  //val push = io.push
  //val pop = io.pop
  val push = io.push
  val pop = io.pop
  val misc = io.misc
  //--------
  def ptrWidth = io.ptrWidth
  def amountWidth = io.amountWidth
  //--------
  val sbPush = PipeSkidBuf(
    dataType=dataType(),
    optIncludeBusy=true,
    //optIncludeBusy=true,
  )
  val sbPop = PipeSkidBuf(
    dataType=dataType(),
    optIncludeBusy=true,
    //optIncludeBusy=true,
  )
  //val sbPushIo = sbPush.io
  //val sbPopIo = sbPop.io
  //sbPush.io.prev <> push
  //sbPush.io.prev >> push

  sbPush.io.misc.busy := misc.full
  sbPop.io.misc.busy := misc.empty

  push >> sbPush.io.prev
  //sbPush.io.prev.payload := push.payload
  //sbPush.io.prev.valid := push.valid
  //push.ready := sbPush.io.prev.ready

  //sbPush.io.next.ready := True
  val sbPushNextReady = Reg(Bool()) init(True)
  sbPush.io.next.ready := sbPushNextReady
  val wrData = sbPush.io.prev.payload
  val wrEn = sbPush.io.prev.fire
  //val pushBusy = sbPush.io.misc.busy

  //sbPop.io.next << pop
  pop << sbPop.io.next
  //pop.payload := sbPop.io.next.payload
  //pop.valid := sbPop.io.next.valid
  //sbPop.io.next.ready := pop.ready

  //sbPop.io.prev.valid := True
  val rdValid = Bool()
  sbPop.io.prev.valid := rdValid
  val rdData = sbPop.io.prev.payload
  val rdDataPrev = Reg(dataType()) init(dataType().getZero)
  //rdData \= rdData.getZero
  //val rdData = sbPop.io.next.payload
  //val popBusy = sbPop.io.misc.busy
  val rdEn = sbPop.io.next.fire

  //val sbIo = skidBuf.io

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
    val arr = new Mem(dataType(), depth)
      .addAttribute("ram_style", arrRamStyle)
      .addAttribute("keep")

    //ptrWidth = width_from_arg(io.SIZE())
    //val ptrWidth = log2Up(depth)
    //val ptrWidth = depth

    val rTail = Reg(UInt(ptrWidth bits)) init(0x0)
    val rHead = Reg(UInt(ptrWidth bits)) init(0x0)


    //misc.amountCanPop := rHead - rTail

    //val tailPlus1 = rTail + 0x1
    //val headPlus1 = rHead + 0x1
    val tailPlus1 = UInt((ptrWidth + 1) bits)
    tailPlus1 := (rTail + U"1'b1".resized).resized
    val headPlus1 = UInt((ptrWidth + 1) bits)
    headPlus1 := (rHead + U"1'b1".resized).resized

    val incrTail = UInt(ptrWidth bits)
    val incrHead = UInt(ptrWidth bits)

    val nextEmpty = Bool()
    val nextFull = Bool()
    val rEmpty = Reg(Bool()) init(True)
    val rFull = Reg(Bool()) init(False)

    val nextTail = UInt(ptrWidth bits)
    val nextHead = UInt(ptrWidth bits)

    //clk, loc.rst = ClockSignal(), ResetSignal()
    //loc.rst = io.rst

    //loc.curr_en_cat = Signal(2)

    val rAmountCanPush = Reg(UInt(amountWidth bits)) init(0x0)
    val rAmountCanPop = Reg(UInt(amountWidth bits)) init(0x0)
    //val nextAmountCanPush = rAmountCanPush.wrapNext()
    //val nextAmountCanPop = rAmountCanPop.wrapNext()
    val nextAmountCanPush = UInt(amountWidth bits)
    val nextAmountCanPop = UInt(amountWidth bits)
    rAmountCanPush := nextAmountCanPush
    rAmountCanPop := nextAmountCanPop
    nextAmountCanPush := (
      U(f"$amountWidth'd$depth") - nextAmountCanPop
    )
    val tempNextTail = UInt(amountWidth bits)
    tempNextTail := nextTail.resized
    val tempNextHead = UInt(amountWidth bits)
    tempNextHead := nextHead.resized
    //nextAmountCanPop := tempNextHead - tempNextTail
    //nextAmountCanPop := Mux(
    //  rHead >= rTail,
    //  rHead - rTail,
    //  U(f"$amountWidth'd$depth") + (rHead - rTail),
    //)
    nextAmountCanPop := Mux(
      rHead >= rTail,
      rHead - rTail,
      U(f"$amountWidth'd$depth") - (rTail - rHead),
    )
    //misc.amountCanPush := rAmountCanPush
    //misc.amountCanPop := rAmountCanPop
    misc.amountCanPush := nextAmountCanPush
    misc.amountCanPop := nextAmountCanPop
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
    //val lastTailVal = Reg(UInt(ptrWidth bits)) init(0x0)
    val testHead = UInt(ptrWidth bits)
    //val empty = Bool()
    //val full = Bool()
    //val wdCnt = Reg(dataType()) init(dataType().getZero)
    //val wdCnt = Reg(UInt(ptrWidth bits)) init(0x0)
    val wdCnt = Reg(UInt(32 bits)) init(0x0)
  }
  //}
  //--------
  GenerationFlags.formal {
    //m.d.sync
    //+= [
      locFormal.lastTailVal := loc.arr.readAsync(loc.rTail)
      locFormal.wdCnt := locFormal.wdCnt - 0x10
    //]
    //m.d.comb
    //+= [
      locFormal.testHead := (loc.rHead + 0x1) % depth
    //]
  }
  //--------
  // Combinational logic

  //m.d.comb 
  //+= [
    //loc.incrTail := Mux[UInt](
    //  loc.tailPlus1 < depth, (loc.rTail + 0x1), 0x0
    //)
    //loc.incrHead := Mux[UInt](
    //  loc.headPlus1 < depth, (loc.rHead + 0x1), 0x0
    //)
    //val tempDepth = UInt((ptrWidth + 1) bits) 
    //val tempDe
    //tempDepth := depth & ((1 << (ptrWidth + 1)) - 1)
    //when (loc.tailPlus1 < tempDepth) 
    when (loc.tailPlus1 < depth) {
      loc.incrTail := loc.rTail + 0x1
    } otherwise {
      loc.incrTail := 0x0
    }
    //when (loc.headPlus1.resized < tempDepth.resized) 
    when (loc.headPlus1 < depth) {
      loc.incrHead := loc.rHead + 0x1
    } otherwise {
      loc.incrHead := 0x0
    }

    loc.nextEmpty := loc.nextHead === loc.nextTail
    //val nextEmpty = loc.nextHead === loc.nextTail
    //loc.nextFull := ((loc.nextHead + 0x1) === loc.nextTail)

    //loc.currEnCat := (Cat(rdEn, wrEn))
  //]

  //when (rdEn & ~misc.empty) 
  when (rdEn & ~misc.empty) {
    //m.d.comb += 
    loc.nextTail := loc.incrTail
  } otherwise {
    //m.d.comb +=
    loc.nextTail := loc.rTail
  }

  when (wrEn & ~misc.full) {
    //m.d.comb += [
    loc.nextHead := loc.incrHead
    loc.nextFull := (loc.incrHead + 0x1) === loc.nextTail
    //]
  } otherwise {
    //m.d.comb += [
    loc.nextHead := loc.rHead
    loc.nextFull := loc.incrHead === loc.nextTail
    //]
  }

  //GenerationFlags.formal {
  //	//m.d.comb += [
  //		cover(misc.empty)
  //		cover(misc.full)
  //		cover((~misc.empty) & (~misc.full))
  //	//]
  //}
  //--------
  // Clocked behavioral code
  //GenerationFlags.formal {
  //  locFormal.pastValid = Signal()
  //}

  rdDataPrev := rdData
  misc.empty := loc.rEmpty
  misc.full := loc.rFull

  //when (loc.rst):
  when (clockDomain.isResetActive) {
    //for elem in loc.arr:
    //	m.d.sync += elem := (io.shape()())

    //m.d.sync += [
      loc.rTail := 0x0
      loc.rHead := 0x0

      //rdData := (io.shape()()),

      //misc.empty := True
      //misc.full := False
    //]
    //if self.FORMAL():
      //m.d.sync += locFormal.pastValid := (0b1)
      rdData := rdData.getZero
      rdValid := rdValid.getZero

  } otherwise { // when (~clockDomain.isResetActive)
    //--------
    //m.d.sync += [
      //misc.empty := loc.nextEmpty
      //misc.full := loc.nextFull
      //misc.empty := loc.rEmpty
      //misc.full := loc.rFull
      loc.rEmpty := loc.nextEmpty
      loc.rFull := loc.nextFull
      loc.rTail := loc.nextTail
      loc.rHead := loc.nextHead
    //]

    //rdValid := False

    when (rdEn & ~misc.empty) {
      //m.d.sync += 
      rdData := loc.arr.readAsync(loc.rTail.resized)
      //rdValid := True
      rdValid := True
    } otherwise {
      rdData := rdDataPrev
      rdValid := False
    }

    when (wrEn & ~misc.full) {
      //m.d.sync += 
      //loc.arr(loc.rHead) := wrData
      loc.arr.write(loc.rHead, wrData)
    }
    //--------
    //if self.FORMAL():
    GenerationFlags.formal {
      when (pastValidAfterReset) {
        //assert(loc.rTail < depth)
        //assert(loc.rHead < depth)
        val tempTailWidth = loc.rTail.getWidth + 1
        val tempHeadWidth = loc.rHead.getWidth + 1
        val tempTailVec = Vec(UInt(tempTailWidth bits), 2)
        val tempHeadVec = Vec(UInt(tempHeadWidth bits), 2)
        tempTailVec(0) := loc.rTail.resized
        tempTailVec(1) := U(f"$tempTailWidth'd$depth")
        assert(tempTailVec(0) < tempTailVec(1))
        tempHeadVec(0) := loc.rHead.resized
        tempHeadVec(1) := U(f"$tempHeadWidth'd$depth")
        assert(tempHeadVec(0) < tempHeadVec(1))

        //m.d.sync += [
          assert(misc.empty === past(loc.nextEmpty))
          assert(misc.full === past(loc.nextFull))
          assert(loc.rTail === past(loc.nextTail))
          assert(loc.rHead === past(loc.nextHead))
        //]
        when (past(rdEn)) {
          when (past(misc.empty)) {
            //m.d.sync += [
              //assert(stable(misc.empty))
              assert(stable(loc.rTail))
            //]
          } otherwise { // when (~past(misc.empty)):
            //when (~past(wrEn)):
            //m.d.sync += [
              //assert(rdData
              //  === loc.arr.readAsync(past(loc.rTail).resized))
              //assert(rdData === locFormal.lastTailVal)
              assert(rdDataPrev === locFormal.lastTailVal)
            //]
          }
        }
        when (past(wrEn)) {
          when (past(misc.full)) {
            //m.d.sync += [
              assert(stable(loc.rHead))
            //]
          }
          //otherwise {// when (~past(misc.full)):
          //	//m.d.sync += [
          //		assert(past(wrData))
          //	//]
          //}
        }
        //val catVal = Bits(2 bits)
        //catVal(0) := misc.empty
        //catVal(1) := misc.full
        switch (Cat(misc.full, misc.empty)) 
        //switch (catVal) 
        {
          is (B"00") {
          //m.d.sync \
          //+= [
            assume(loc.rHead =/= loc.rTail)
            assume(locFormal.testHead =/= loc.rTail)
          //]
          }
          is (B"01") {
          //m.d.sync \
          //+= [
            assert(loc.rHead === loc.rTail)
          //]
          }
          is (B"10") {
          //m.d.sync \
          //+= [
            assert(locFormal.testHead === loc.rTail)
          //]
          }
        }
        //m.d.sync += [
          assert(~(misc.empty & misc.full))
          //assume(~stable(wrData))
          //assume(wrData === locFormal.wdCnt)
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
  //val test = StreamFifo(dataType())
  //--------
  val io = FifoIo(
    dataType=dataType(),
    depth=depth,
  )
  //--------
  // Local variables
  //val inp = io.inp
  //val outp = io.outp
  //--------
  val push = io.push
  val pop = io.pop
  val misc = io.misc
  //--------
  def ptrWidth = io.ptrWidth
  def amountWidth = io.amountWidth
  //--------
  val sbPush = PipeSkidBuf(
    dataType=dataType(),
    optIncludeBusy=true,
    //optIncludeBusy=true,
  )
  val sbPop = PipeSkidBuf(
    dataType=dataType(),
    optIncludeBusy=true,
    //optIncludeBusy=true,
  )
  //val sbPushIo = sbPush.io
  //val sbPopIo = sbPop.io
  //sbPush.io.prev <> push
  //sbPush.io.prev >> push
  //val sbPushBusy = sbPush.io.busy
  //val sbPopBusy = sbPop.io.busy
  sbPush.io.misc.busy := misc.full
  sbPop.io.misc.busy := misc.empty

  push >> sbPush.io.prev
  //sbPush.io.prev.payload := push.payload
  //sbPush.io.prev.valid := push.valid
  //push.ready := sbPush.io.prev.ready

  sbPush.io.next.ready := True
  val wrData = sbPush.io.prev.payload
  val wrEn = sbPush.io.prev.fire
  //val pushBusy = sbPush.io.misc.busy

  //sbPop.io.next << pop
  pop << sbPop.io.next
  //pop.payload := sbPop.io.next.payload
  //pop.valid := sbPop.io.next.valid
  //sbPop.io.next.ready := pop.ready

  //sbPop.io.prev.valid := True
  val rdValid = Bool()
  sbPop.io.prev.valid := rdValid
  val rdData = sbPop.io.prev.payload
  val rdDataPrev = Reg(dataType()) init(dataType().getZero)
  //rdData \= rdData.getZero
  //val rdData = sbPop.io.next.payload
  //val popBusy = sbPop.io.misc.busy
  //val rdEn = sbPop.io.next.fire
  val rdEn = Bool() addAttribute("keep")
  rdEn := sbPop.io.next.fire

  val loc = new Area {
    val empty = Reg(Bool()) init(True)
    val full = Reg(Bool()) init(False)
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

    //val ptrWidth = log2Up(depth)
    def ptrWidthPlus1 = ptrWidth + 1
    //val ptrWidthPlus1 = ptrWidthPlus1
    def uintDepth = U(f"$ptrWidthPlus1'd$depth")

    val rHead = Reg(UInt(ptrWidth bits)) init(0x0)
    val rTail = Reg(UInt(ptrWidth bits)) init(0x0)

    val nextHead = UInt(ptrWidth bits)
    val nextTail = UInt(ptrWidth bits)

    val tempTailPlus1 = UInt(ptrWidthPlus1 bits) addAttribute("keep")
    val tailPlus1 = (rTail.resized + U(f"$ptrWidthPlus1'b1")).resized
    tempTailPlus1 := tailPlus1

    val tempHeadPlus1 = UInt(ptrWidthPlus1 bits) addAttribute("keep")
    val headPlus1 = (rHead.resized + U(f"$ptrWidthPlus1'b1")).resized
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

    val rAmountCanPush = Reg(UInt(amountWidth bits)) init(0x0)
    val rAmountCanPop = Reg(UInt(amountWidth bits)) init(0x0)
    //val nextAmountCanPush = rAmountCanPush.wrapNext()
    //val nextAmountCanPop = rAmountCanPop.wrapNext()
    val nextAmountCanPush = UInt(amountWidth bits)
    val nextAmountCanPop = UInt(amountWidth bits)
    rAmountCanPush := nextAmountCanPush
    rAmountCanPop := nextAmountCanPop
    nextAmountCanPush := (
      U(f"$amountWidth'd$depth") - nextAmountCanPop
    )
    val tempNextTail = UInt(amountWidth bits)
    tempNextTail := nextTail.resized
    val tempNextHead = UInt(amountWidth bits)
    tempNextHead := nextHead.resized
    //nextAmountCanPop := Mux(
    //  tempNextHead > tempNextTail,
    //  tempNextHead - tempNextTail,
    //  U(f"$amountWidth'd$depth") + (tempNextHead - tempNextTail),
    //)
    nextAmountCanPop := Mux(
      rHead >= rTail,
      rHead - rTail,
      U(f"$amountWidth'd$depth") - (rTail - rHead),
    )
    //misc.amountCanPush := rAmountCanPush
    //misc.amountCanPop := rAmountCanPop
    misc.amountCanPush := nextAmountCanPush
    misc.amountCanPop := nextAmountCanPop
  }
  val locFormal = new Area {
    val lastTailVal = Reg(dataType()) init(dataType().getZero)
    lastTailVal.addAttribute("keep")
    val testHead = ((loc.rHead + 0x1) % depth)
  }
  GenerationFlags.formal {
    locFormal.lastTailVal := loc.arr.readAsync(loc.rTail)
  }
  //--------
  // Combinational logic

  //when((~loc.empty) & rdEn):
  // m.d.comb += rdData := (loc.arr(loc.rTail))

  //m.d.comb += [
  rdData := loc.arr.readAsync(loc.rTail)
  rdValid := True
  misc.empty := loc.empty
  misc.full := loc.full
  //]

  // Compute `loc.nextHead` and write into the FIFO if it's not full
  //loc.headPlus1 = loc.rHead + 0x1
  when (clockDomain.isResetActive) {
    //m.d.comb += [
    loc.nextHead := 0x0
    loc.nextTail := 0x0
    //]
  } otherwise { // when (~loc.rst)
    when (~misc.full & wrEn) {
      //m.d.sync += loc.arr(loc.rHead) := wrData

      when (loc.headPlus1 >= loc.uintDepth) {
        //m.d.comb += 
        loc.nextHead := 0x0
      } otherwise {
        //m.d.comb += 
        loc.nextHead := loc.headPlus1.resized
      }
    } otherwise {
      //m.d.comb += 
      loc.nextHead := loc.rHead
    }

    // Compute `loc.nextTail`
    //loc.tailPlus1 = loc.rTail + 0x1
    when (~misc.empty & rdEn) {
      when (loc.tailPlus1 >= loc.uintDepth) {
        //m.d.comb +=
        loc.nextTail := 0x0
      } otherwise {
        //m.d.comb += 
        loc.nextTail := loc.tailPlus1.resized
      }
    } otherwise {
      //m.d.comb += 
      loc.nextTail := loc.rTail
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
  ////   //cover(pastValid & misc.empty),
  ////   //cover(pastValid & misc.full),
  ////   //cover(pastValid & (~misc.empty)
  ////   // & (~misc.full)),
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
  loc.rHead := loc.nextHead
  loc.rTail := loc.nextTail
  loc.empty := loc.nextEmpty
  loc.full := loc.nextFull
  //]
  //when (clockDomain.isResetActive) {
  // //--------
  // //m.d.sync += [
  //   loc.rHead := 0x0
  //   loc.rTail := 0x0

  //   misc.empty := True
  //   misc.full := False
  // //]
  //}
  // //--------
  //otherwise // when(~clockDomain.isResetActive):
  when (~clockDomain.isResetActive) {
    //--------

    // Write into the FIFO
    when ((~misc.full) & wrEn) {
      //m.d.sync += 
      loc.arr.write(loc.rHead, wrData)
    }
    //--------
    //if self.FORMAL():
    GenerationFlags.formal {
      //m.d.comb += cover(pastValid)
      when (pastValidAfterReset) {
        val tempTailWidth = loc.rTail.getWidth + 1
        val tempHeadWidth = loc.rHead.getWidth + 1
        val tempTailVec = Vec(UInt(tempTailWidth bits), 2)
        val tempHeadVec = Vec(UInt(tempHeadWidth bits), 2)
        tempTailVec(0) := loc.rTail.resized
        tempTailVec(1) := U(f"$tempTailWidth'd$depth")
        assert(tempTailVec(0) < tempTailVec(1))
        tempHeadVec(0) := loc.rHead.resized
        tempHeadVec(1) := U(f"$tempHeadWidth'd$depth")
        assert(tempHeadVec(0) < tempHeadVec(1))
        //assert(loc.rTail < depth)
        //assert(loc.rHead < depth)

        //m.d.sync += [
        // cover(misc.empty)
        // cover(misc.full)
        // cover(~(misc.empty & misc.full))
        //)

        //m.d.sync += [
        //assert(loc.rHead === past(loc.nextHead)),
        //assert(loc.rTail === past(loc.nextTail)),
        //assert(misc.empty === past(loc.nextEmpty)),
        //assert(misc.full === past(loc.nextFull)),
        assert(~(misc.empty & misc.full))

        assert(rdData === loc.arr(loc.rTail))
        //assert(past(rdData) === locFormal.lastTailVal)
        //]

        when (past(rdEn)) {
          when (past(misc.empty)) {
            //m.d.sync +=
            assert(stable(loc.rTail))
            //assert(stable(loc.nextTail))
          } otherwise { // when (~past(misc.empty))
            //m.d.sync += 
            //val tempTailPlus1Width = loc.rTail.getWidth + 1
            //val tempTailPlus1 = Vec(UInt(tempTailPlus1Width bits), 4)
            //tempTailPlus1(0) := past(loc.rTail).resized
            //tempTailPlus1(1) := U(f"$tempTailPlus1Width'd1")
            //tempTailPlus1(2) := tempTailPlus1(0) + tempTailPlus1(1)
            //tempTailPlus1(3) := U(f"$tempTailPlus1Width'd$depth")
            //when (tempTailPlus1(2) >= tempTailPlus1(3)) {
            //  assert(loc.rTail === 0)
            //} otherwise {
            //  assert(loc.rTail === tempTailPlus1(2))
            //}
            //when ((past(loc.rTail) + 1) === depth) {
            //}
            assert(loc.rTail
              === ((past(loc.rTail) + 1) % depth))
          }
        } otherwise { // when (~past(rdEn))
          //m.d.sync += [
          //assert(stable(misc.empty))
          assert(stable(loc.rTail))
          //]
        }

        when (past(wrEn)) {
          when (past(misc.full)) {
            //m.d.sync +=
            assert(stable(loc.rHead))
          } otherwise { // when (~past(misc.full))
            //m.d.sync +=
            assert(loc.rHead === ((past(loc.rHead) + 1) % depth))
            //val tempHeadPlus1Width = loc.rHead.getWidth + 1
            //val tempHeadPlus1 = Vec(UInt(tempHeadPlus1Width bits), 4)
            //tempHeadPlus1(0) := past(loc.rHead).resized
            //tempHeadPlus1(1) := U(f"$tempHeadPlus1Width'd1")
            //tempHeadPlus1(2) := tempHeadPlus1(0) + tempHeadPlus1(1)
            //tempHeadPlus1(3) := U(f"$tempHeadPlus1Width'd$depth")
            //when (tempHeadPlus1(2) >= tempHeadPlus1(3)) {
            //  assert(loc.rHead === 0)
            //} otherwise {
            //  assert(loc.rHead === tempHeadPlus1(2))
            //}
          }
        } otherwise { // when (~past(wrEn))
          //m.d.sync += [
          //assert(stable(misc.full))
          assert(stable(loc.rHead))
          //]
        }


        switch (Cat(misc.full, misc.empty)) {
          // neither full nor empty
          is (B"00") {
            //m.d.sync += [
            assert(loc.rHead =/= loc.rTail)
            assert(locFormal.testHead =/= loc.rTail)
            //]
          }
          // empty
          is (B"01") {
            //m.d.sync += [
            assert(loc.rHead === loc.rTail)
            //]
          }
          // full
          is (B"10") {
            //m.d.sync += [
            assert(locFormal.testHead === loc.rTail)
            //]
          }
        }

        // empty
        when (loc.rHead === loc.rTail) {
          //m.d.sync += 
          //assert(misc.empty & (~misc.full))
          assert(misc.empty)
          assert(~misc.full)
        } elsewhen (locFormal.testHead === loc.rTail) {
          //m.d.sync += 
          //assert((~misc.empty) & misc.full)
          assert(~misc.empty)
          assert(misc.full)
        } otherwise {
          //m.d.sync += 
          //assert((~misc.empty) & (~misc.full))
          assert(~misc.empty)
          assert(~misc.full)
        }
      }
    //--------
    }
  }
  //--------

  //--------
}
