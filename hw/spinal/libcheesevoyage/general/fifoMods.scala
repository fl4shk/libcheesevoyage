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
  //def ptrWidth(depth: Int) = log2Up(depth)
  //def amountWidth(depth: Int) = log2Up(depth + 1)
  //def tempDepth(depth: Int) = depth + 1
  //def ptrWidth(depth: Int) = log2Up(tempDepth(depth) + 1)
  //def amountWidth(depth: Int) = ptrWidth(depth=depth + 1)
  def ptrWidth(depth: Int) = log2Up(depth + 1)
  def amountWidth(depth: Int) = ptrWidth(depth=depth + 1)

  def calcNextAmountCanPushPop(
    depth: Int,
    someHead: UInt,
    someTail: UInt,
    someAmountCanPush: UInt,
    someAmountCanPop: UInt,
  ): Unit = {
    def tempAmountWidth = amountWidth(depth=depth)
    def uintDepth = U(f"$tempAmountWidth'd$depth")
    def depthMinus1 = depth - 1
    def uintDepthMinus1AmtW = U(f"$tempAmountWidth'd$depthMinus1")

    someAmountCanPush := (
      //U(f"$amountWidth'd$tempDepth") - nextAmountCanPop
      //U(f"$amountWidth'd$depth")
      uintDepthMinus1AmtW
      - someAmountCanPop
    )
    
    //someAmountCanPop = (
    //  head - tail
    //  if (head >= tail)
    //  else (depth - (tail - head))
    //)

    someAmountCanPop := Mux[UInt](
      someHead >= someTail,
      someHead - someTail,
      //U(f"$amountWidth'd$tempDepth") - (someTail - someHead),
      //U(f"$amountWidth'd$depth")
      uintDepth
      - (someTail - someHead),
    )
  }
}
case class FifoMiscIo(
  depth: Int,
) extends Bundle {
  //val flush = in port Bool()
  val empty = out port Bool()
  val full = out port Bool()

  //def ptrWidth = log2Up(depth)
  //def amountWidth = log2Up(depth + 1)
  //def tempDepth = FifoMiscIo.tempDepth(depth=depth)
  def ptrWidth = FifoMiscIo.ptrWidth(depth=depth)
  def amountWidth = FifoMiscIo.amountWidth(depth=depth)

  // How many elements can be pushed
  val amountCanPush = out port UInt(amountWidth bits)
  // How many elements can be popped
  val amountCanPop = out port UInt(amountWidth bits)
}
object FifoIo {
  def ptrWidth(depth: Int) = FifoMiscIo.ptrWidth(depth=depth)
  def amountWidth(depth: Int) = FifoMiscIo.amountWidth(depth=depth)
  def calcNextAmountCanPushPop(
    depth: Int,
    someHead: UInt,
    someTail: UInt,
    someAmountCanPush: UInt,
    someAmountCanPop: UInt,
  ): Unit = FifoMiscIo.calcNextAmountCanPushPop(
    depth=depth,
    someHead=someHead,
    someTail=someTail,
    someAmountCanPush=someAmountCanPush,
    someAmountCanPop=someAmountCanPop,
  )
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
  //def tempDepth = misc.tempDepth
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

//case class Fifo[
//  T <: Data
//](
//  dataType: HardType[T],
//  depth: Int,
//  //arrRamStyle: String="ultra",
//  arrRamStyle: String="block",
//) extends Component {
//  //--------
//  val io = FifoIo(
//    dataType=dataType(),
//    depth=depth,
//  )
//  //--------
//  // Local variables
//  //val inp = io.inp
//  ////val outp = io.outp
//  //val outp = Reg(FifoOutp(dataType()))
//  //outp.init(outp.getZero)
//  //io.outp := outp
//  //val push = io.push
//  //val pop = io.pop
//  val push = io.push
//  val pop = io.pop
//  val misc = io.misc
//  //--------
//  def tempDepth = io.tempDepth
//  def ptrWidth = io.ptrWidth
//  def amountWidth = io.amountWidth
//  //--------
//  val sbPush = PipeSkidBuf(
//    dataType=dataType(),
//    optIncludeBusy=true,
//    //optIncludeBusy=true,
//  )
//  val sbPop = PipeSkidBuf(
//    dataType=dataType(),
//    optIncludeBusy=true,
//    //optIncludeBusy=true,
//  )
//  //val sbPushIo = sbPush.io
//  //val sbPopIo = sbPop.io
//  //sbPush.io.prev <> push
//  //sbPush.io.prev >> push
//
//  sbPush.io.misc.busy := misc.full
//  sbPop.io.misc.busy := misc.empty
//
//  push >> sbPush.io.prev
//  //sbPush.io.prev.payload := push.payload
//  //sbPush.io.prev.valid := push.valid
//  //push.ready := sbPush.io.prev.ready
//
//  //sbPush.io.next.ready := True
//  //val sbPushNextReady = Reg(Bool()) init(True)
//  //sbPush.io.next.ready := sbPushNextReady
//  sbPush.io.next.ready := True
//  val wrData = sbPush.io.prev.payload
//  val wrEn = sbPush.io.prev.fire
//  //val pushBusy = sbPush.io.misc.busy
//
//  //sbPop.io.next << pop
//  pop << sbPop.io.next
//  //pop.payload := sbPop.io.next.payload
//  //pop.valid := sbPop.io.next.valid
//  //sbPop.io.next.ready := pop.ready
//
//  //sbPop.io.prev.valid := True
//  val rdValid = Bool()
//  sbPop.io.prev.valid := rdValid
//  val rdData = sbPop.io.prev.payload
//  val rdDataPrev = Reg(dataType()) init(dataType().getZero)
//  //rdData \= rdData.getZero
//  //val rdData = sbPop.io.next.payload
//  //val popBusy = sbPop.io.misc.busy
//  //val rdEn = sbPop.io.next.fire
//  val rdEn = KeepAttribute(Bool())
//  rdEn := sbPop.io.next.fire
//
//  //val sbIo = skidBuf.io
//
//  //loc = Blank()
//
//  val loc = new Area {
//    //loc.arr = Array([
//    //  Signal(io.shape()) for _ in range(io.SIZE())
//    //])
//    //val arr = Vec(dataType(), tempDepth)
//    //loc.arr = Array(
//    //  [
//    //    Signal(io.shape()) for _ in range(io.SIZE())
//    //  ],
//    //  attrs={"ram_style": "ultra"}
//    //)
//    val arr = new Mem(dataType(), tempDepth)
//      .addAttribute("ram_style", arrRamStyle)
//      .addAttribute("keep")
//
//    //ptrWidth = width_from_arg(io.SIZE())
//    //val ptrWidth = log2Up(tempDepth)
//    //val ptrWidth = tempDepth
//
//    val rTail = Reg(UInt(ptrWidth bits)) init(0x0)
//    val rHead = Reg(UInt(ptrWidth bits)) init(0x0)
//
//
//    //misc.amountCanPop := rHead - rTail
//
//    //val tailPlus1 = rTail + 0x1
//    //val headPlus1 = rHead + 0x1
//    val tailPlus1 = UInt((ptrWidth + 1) bits)
//    tailPlus1 := (rTail + U"1'b1".resized).resized
//    val headPlus1 = UInt((ptrWidth + 1) bits)
//    headPlus1 := (rHead + U"1'b1".resized).resized
//
//    val incrTail = UInt(ptrWidth bits)
//    val incrHead = UInt(ptrWidth bits)
//
//    val nextEmpty = Bool()
//    val nextFull = Bool()
//    val rEmpty = Reg(Bool()) init(True)
//    val rFull = Reg(Bool()) init(False)
//
//    val nextTail = UInt(ptrWidth bits)
//    val nextHead = UInt(ptrWidth bits)
//
//    //clk, loc.rst = ClockSignal(), ResetSignal()
//    //loc.rst = io.rst
//
//    //loc.curr_en_cat = Signal(2)
//
//    val rAmountCanPush = Reg(UInt(amountWidth bits)) init(0x0)
//    val rAmountCanPop = Reg(UInt(amountWidth bits)) init(0x0)
//    //val nextAmountCanPush = rAmountCanPush.wrapNext()
//    //val nextAmountCanPop = rAmountCanPop.wrapNext()
//    val nextAmountCanPush = UInt(amountWidth bits)
//    val nextAmountCanPop = UInt(amountWidth bits)
//    rAmountCanPush := nextAmountCanPush
//    rAmountCanPop := nextAmountCanPop
//    nextAmountCanPush := (
//      U(f"$amountWidth'd$tempDepth") - nextAmountCanPop
//    )
//    val tempNextTail = UInt(amountWidth bits)
//    tempNextTail := nextTail.resized
//    val tempNextHead = UInt(amountWidth bits)
//    tempNextHead := nextHead.resized
//    //nextAmountCanPop := tempNextHead - tempNextTail
//    //nextAmountCanPop := Mux(
//    //  rHead >= rTail,
//    //  rHead - rTail,
//    //  U(f"$amountWidth'd$tempDepth") + (rHead - rTail),
//    //)
//    nextAmountCanPop := Mux(
//      rHead >= rTail,
//      rHead - rTail,
//      U(f"$amountWidth'd$tempDepth") - (rTail - rHead),
//    )
//    //misc.amountCanPush := rAmountCanPush
//    //misc.amountCanPop := rAmountCanPop
//    misc.amountCanPush := nextAmountCanPush
//    misc.amountCanPop := nextAmountCanPop
//  }
//
//  //GenerationFlags.formal {
//  //val haveFormal = {
//  //  GenerationFlags.formal {
//  //    true
//  //  }
//  //  false
//  //}
//    //loc.formal = Blank()
//  val locFormal = new Area {
//    val lastTailVal = Reg(dataType()) init(dataType().getZero)
//    //val lastTailVal = Reg(UInt(ptrWidth bits)) init(0x0)
//    val testHead = UInt(ptrWidth bits)
//    //val empty = Bool()
//    //val full = Bool()
//    //val wdCnt = Reg(dataType()) init(dataType().getZero)
//    //val wdCnt = Reg(UInt(ptrWidth bits)) init(0x0)
//    val wdCnt = Reg(UInt(32 bits)) init(0x0)
//  }
//  //}
//  //--------
//  GenerationFlags.formal {
//    //m.d.sync
//    //+= [
//      locFormal.lastTailVal := loc.arr.readAsync(address=loc.rTail.resized)
//      locFormal.wdCnt := locFormal.wdCnt - 0x10
//    //]
//    //m.d.comb
//    //+= [
//      locFormal.testHead := (loc.rHead + 0x1) % tempDepth
//    //]
//  }
//  //--------
//  // Combinational logic
//
//  //m.d.comb 
//  //+= [
//    //loc.incrTail := Mux[UInt](
//    //  loc.tailPlus1 < tempDepth, (loc.rTail + 0x1), 0x0
//    //)
//    //loc.incrHead := Mux[UInt](
//    //  loc.headPlus1 < tempDepth, (loc.rHead + 0x1), 0x0
//    //)
//    //val tempDepth = UInt((ptrWidth + 1) bits) 
//    //val tempDe
//    //tempDepth := tempDepth & ((1 << (ptrWidth + 1)) - 1)
//    //when (loc.tailPlus1 < tempDepth) 
//    when (loc.tailPlus1 < tempDepth) {
//      loc.incrTail := loc.rTail + 0x1
//    } otherwise {
//      loc.incrTail := 0x0
//    }
//    //when (loc.headPlus1.resized < tempDepth.resized) 
//    when (loc.headPlus1 < tempDepth) {
//      loc.incrHead := loc.rHead + 0x1
//    } otherwise {
//      loc.incrHead := 0x0
//    }
//
//    loc.nextEmpty := loc.nextHead === loc.nextTail
//    //val nextEmpty = loc.nextHead === loc.nextTail
//    //loc.nextFull := ((loc.nextHead + 0x1) === loc.nextTail)
//
//    //loc.currEnCat := (Cat(rdEn, wrEn))
//  //]
//
//  //when (rdEn & ~misc.empty) 
//  when (rdEn & ~misc.empty) {
//    //m.d.comb += 
//    loc.nextTail := loc.incrTail
//  } otherwise {
//    //m.d.comb +=
//    loc.nextTail := loc.rTail
//  }
//
//  when (wrEn & ~misc.full) {
//    //m.d.comb += [
//    loc.nextHead := loc.incrHead
//    loc.nextFull := (loc.incrHead + 0x1) === loc.nextTail
//    //]
//  } otherwise {
//    //m.d.comb += [
//    loc.nextHead := loc.rHead
//    loc.nextFull := loc.incrHead === loc.nextTail
//    //]
//  }
//
//  //GenerationFlags.formal {
//  //  //m.d.comb += [
//  //    cover(misc.empty)
//  //    cover(misc.full)
//  //    cover((~misc.empty) & (~misc.full))
//  //  //]
//  //}
//  //--------
//  // Clocked behavioral code
//  //GenerationFlags.formal {
//  //  locFormal.pastValid = Signal()
//  //}
//
//  rdDataPrev := rdData
//  misc.empty := loc.rEmpty
//  misc.full := loc.rFull
//
//  //when (loc.rst):
//  when (clockDomain.isResetActive) {
//    //for elem in loc.arr:
//    //  m.d.sync += elem := (io.shape()())
//
//    //m.d.sync += [
//      loc.rTail := 0x0
//      loc.rHead := 0x0
//
//      //rdData := (io.shape()()),
//
//      //misc.empty := True
//      //misc.full := False
//    //]
//    //if self.FORMAL():
//      //m.d.sync += locFormal.pastValid := (0b1)
//      rdData := rdData.getZero
//      rdValid := rdValid.getZero
//  } otherwise { // when (~clockDomain.isResetActive)
//    //--------
//    //m.d.sync += [
//      //misc.empty := loc.nextEmpty
//      //misc.full := loc.nextFull
//      //misc.empty := loc.rEmpty
//      //misc.full := loc.rFull
//      loc.rEmpty := loc.nextEmpty
//      loc.rFull := loc.nextFull
//      loc.rTail := loc.nextTail
//      loc.rHead := loc.nextHead
//    //]
//
//    //rdValid := False
//
//    rdValid := True
//    //rdValid := ~misc.empty
//    //rdValid := rdEn || ~misc.empty
//
//    when (rdEn & ~misc.empty) {
//      //m.d.sync += 
//      rdData := loc.arr.readAsync(address=loc.rTail.resized)
//      //rdValid := True
//      //rdValid := True
//    } otherwise {
//      rdData := rdDataPrev
//      //rdValid := False
//    }
//
//    when (wrEn & ~misc.full) {
//      //m.d.sync += 
//      //loc.arr(loc.rHead) := wrData
//      loc.arr.write(address=loc.rHead.resized, data=wrData)
//    }
//    //--------
//    //if self.FORMAL():
//    GenerationFlags.formal {
//      when (pastValidAfterReset) {
//        //assert(loc.rTail < tempDepth)
//        //assert(loc.rHead < tempDepth)
//        val tempTailWidth = loc.rTail.getWidth + 1
//        val tempHeadWidth = loc.rHead.getWidth + 1
//        val tempTailVec = Vec(UInt(tempTailWidth bits), 2)
//        val tempHeadVec = Vec(UInt(tempHeadWidth bits), 2)
//        tempTailVec(0) := loc.rTail.resized
//        tempTailVec(1) := U(f"$tempTailWidth'd$tempDepth")
//        assert(tempTailVec(0) < tempTailVec(1))
//        tempHeadVec(0) := loc.rHead.resized
//        tempHeadVec(1) := U(f"$tempHeadWidth'd$tempDepth")
//        assert(tempHeadVec(0) < tempHeadVec(1))
//
//        //m.d.sync += [
//          assert(misc.empty === past(loc.nextEmpty))
//          assert(misc.full === past(loc.nextFull))
//          assert(loc.rTail === past(loc.nextTail))
//          assert(loc.rHead === past(loc.nextHead))
//        //]
//        when (past(rdEn)) {
//          when (past(misc.empty)) {
//            //m.d.sync += [
//              //assert(stable(misc.empty))
//              assert(stable(loc.rTail))
//            //]
//          } otherwise { // when (~past(misc.empty)):
//            //when (~past(wrEn)):
//            //m.d.sync += [
//              //assert(rdData
//              //  === loc.arr.readAsync(past(loc.rTail).resized))
//              //assert(rdData === locFormal.lastTailVal)
//              assert(rdDataPrev === locFormal.lastTailVal)
//            //]
//          }
//        }
//        when (past(wrEn)) {
//          when (past(misc.full)) {
//            //m.d.sync += [
//              assert(stable(loc.rHead))
//            //]
//          }
//          //otherwise {// when (~past(misc.full)):
//          //  //m.d.sync += [
//          //    assert(past(wrData))
//          //  //]
//          //}
//        }
//        //val catVal = Bits(2 bits)
//        //catVal(0) := misc.empty
//        //catVal(1) := misc.full
//        switch (Cat(misc.full, misc.empty)) 
//        //switch (catVal) 
//        {
//          is (B"00") {
//          //m.d.sync \
//          //+= [
//            assume(loc.rHead =/= loc.rTail)
//            assume(locFormal.testHead =/= loc.rTail)
//          //]
//          }
//          is (B"01") {
//          //m.d.sync \
//          //+= [
//            assert(loc.rHead === loc.rTail)
//          //]
//          }
//          is (B"10") {
//          //m.d.sync \
//          //+= [
//            assert(locFormal.testHead === loc.rTail)
//          //]
//          }
//        }
//        //m.d.sync += [
//          assert(~(misc.empty & misc.full))
//          //assume(~stable(wrData))
//          //assume(wrData === locFormal.wdCnt)
//        //]
//      }
//    }
//    //--------
//  }
//  ////--------
//}

//case class Fifo[
//  T <: Data
//](
//  dataType: HardType[T],
//  depth: Int,
//  //arrRamStyle: String="ultra",
//  arrRamStyle: String="block",
//) extends Component {
//  //--------
//  val io = FifoIo(
//    dataType=dataType(),
//    depth=depth,
//  )
//  //--------
//  val push = io.push
//  val pop = io.pop
//  val misc = io.misc
//  //--------
//  def tempDepth = io.tempDepth
//  def ptrWidth = io.ptrWidth
//  def ptrWidthPlus1 = ptrWidth + 1
//  def amountWidth = io.amountWidth
//  //--------
//  val sbPush = PipeSkidBuf(
//    dataType=dataType(),
//    optIncludeBusy=true,
//    //optIncludeBusy=true,
//  )
//  val sbPop = PipeSkidBuf(
//    dataType=dataType(),
//    optIncludeBusy=true,
//    //optIncludeBusy=true,
//  )
//  //val sbPushIo = sbPush.io
//  //val sbPopIo = sbPop.io
//  //sbPush.io.prev <> push
//  //sbPush.io.prev >> push
//  //val sbPushBusy = sbPush.io.busy
//  //val sbPopBusy = sbPop.io.busy
//  sbPush.io.misc.busy := misc.full
//  sbPop.io.misc.busy := misc.empty
//
//  push >> sbPush.io.prev
//  //sbPush.io.prev.payload := push.payload
//  //sbPush.io.prev.valid := push.valid
//  //push.ready := sbPush.io.prev.ready
//
//  sbPush.io.next.ready := True
//  val wrData = sbPush.io.prev.payload
//  val wrEn = sbPush.io.prev.fire
//  //val pushBusy = sbPush.io.misc.busy
//
//  //sbPop.io.next << pop
//  pop << sbPop.io.next
//  //pop.payload := sbPop.io.next.payload
//  //pop.valid := sbPop.io.next.valid
//  //sbPop.io.next.ready := pop.ready
//
//  //sbPop.io.prev.valid := True
//  val rdValid = Bool()
//  sbPop.io.prev.valid := rdValid
//  val rdData = sbPop.io.prev.payload
//  val rdDataPrev = Reg(dataType()) init(dataType().getZero)
//  //rdData \= rdData.getZero
//  //val rdData = sbPop.io.next.payload
//  //val popBusy = sbPop.io.misc.busy
//  //val rdEn = sbPop.io.next.fire
//  val rdEn = Bool() addAttribute("keep")
//  rdEn := sbPop.io.next.fire
//  //--------
//  val loc = new Area {
//    val arr = Mem(dataType(), tempDepth)
//
//    val rTail = Reg(UInt(ptrWidth bits)) init(0x0)
//    val rHead = Reg(UInt(ptrWidth bits)) init(0x0)
//
//    //val tailPlus1 = rTail + 0x1
//    //val headPlus1 = rHead + 0x1
//    val tailPlus1 = UInt(ptrWidthPlus1 bits)
//    tailPlus1 := rTail.resized + U(f"$ptrWidthPlus1'd1")
//    val headPlus1 = UInt(ptrWidthPlus1 bits)
//    headPlus1 := rHead.resized + U(f"$ptrWidthPlus1'd1")
//
//    val incrTail = UInt(ptrWidth bits)
//    val incrHead = UInt(ptrWidth bits)
//
//    val nextEmpty = Bool()
//    val nextFull = Bool()
//    val rEmpty = Reg(Bool()) init(False)
//    val rFull = Reg(Bool()) init(False)
//
//    val nextTail = UInt(ptrWidth bits)
//    val nextHead = UInt(ptrWidth bits)
//
//    val rRdData = Reg(dataType())
//    rRdData.init(rRdData.getZero)
//    rdData := rRdData
//    //--------
//    val rAmountCanPush = Reg(UInt(amountWidth bits)) init(0x0)
//    val rAmountCanPop = Reg(UInt(amountWidth bits)) init(0x0)
//    //val nextAmountCanPush = rAmountCanPush.wrapNext()
//    //val nextAmountCanPop = rAmountCanPop.wrapNext()
//    val nextAmountCanPush = UInt(amountWidth bits)
//    val nextAmountCanPop = UInt(amountWidth bits)
//    rAmountCanPush := nextAmountCanPush
//    rAmountCanPop := nextAmountCanPop
//    nextAmountCanPush := (
//      U(f"$amountWidth'd$tempDepth") - nextAmountCanPop
//    )
//    val tempNextTail = UInt(amountWidth bits)
//    tempNextTail := nextTail.resized
//    val tempNextHead = UInt(amountWidth bits)
//    tempNextHead := nextHead.resized
//    //nextAmountCanPop := Mux(
//    //  tempNextHead > tempNextTail,
//    //  tempNextHead - tempNextTail,
//    //  U(f"$amountWidth'd$tempDepth") + (tempNextHead - tempNextTail),
//    //)
//    nextAmountCanPop := Mux(
//      rHead >= rTail,
//      rHead - rTail,
//      U(f"$amountWidth'd$tempDepth") - (rTail - rHead),
//    )
//    //misc.amountCanPush := rAmountCanPush
//    //misc.amountCanPop := rAmountCanPop
//    misc.amountCanPush := nextAmountCanPush
//    misc.amountCanPop := nextAmountCanPop
//    //--------
//    val formal = new Area {
//      val lastTailVal = Reg(dataType())
//      lastTailVal.init(lastTailVal.getZero)
//
//      val testHead = UInt(ptrWidth bits)
//      //val empty = Bool()
//      //val full = Bool()
//      val wdCnt = Reg(UInt(amountWidth bits)) init(0x0)
//    }
//    //--------
//  }
//  //--------
//  GenerationFlags.formal {
//    //m.d.sync += [
//    loc.formal.lastTailVal := loc.arr.readAsync(loc.rTail)
//    loc.formal.wdCnt := loc.formal.wdCnt - 0x10
//    //]
//    //m.d.comb += [
//    loc.formal.testHead := (loc.rHead + 0x1) % tempDepth
//    //]
//  }
//  //--------
//  // Combinational logic
//  misc.empty := loc.rEmpty
//  misc.full := loc.rFull
//  rdValid := True
//
//  //m.d.comb += [
//  loc.incrTail := Mux[UInt](
//    loc.tailPlus1 < tempDepth,
//    (loc.rTail + 0x1),
//    0x0
//  )
//  loc.incrHead := Mux[UInt](
//    loc.headPlus1 < tempDepth, (loc.rHead + 0x1), 0x0
//  )
//
//  loc.nextEmpty := loc.nextHead === loc.nextTail
//  //loc.nextFull := (loc.nextHead + 0x1) === loc.nextTail
//
//  // the below is based on an old nMigen `Cat`, so it might not be correct
//  // now!
//  //loc.currEnCat := Cat(inp.rdEn, inp.wrEn)
//  //]
//  when (rdEn & ~misc.empty) {
//    //m.d.comb +=
//    loc.nextTail := loc.incrTail
//  } otherwise {
//    //m.d.comb += 
//    loc.nextTail := loc.rTail
//  }
//
//  when (wrEn & ~misc.full) {
//    //m.d.comb += [
//    loc.nextHead := loc.incrHead
//    loc.nextFull := (loc.incrHead + 0x1) === loc.nextTail
//    //]
//  } otherwise {
//    //m.d.comb += [
//    loc.nextHead := loc.rHead
//    loc.nextFull := loc.incrHead === loc.nextTail
//    //]
//  }
//  //--------
//  // Clocked behavioral code
//  //GenerationFlags.formal {
//  //  //loc.formal.pastValid = Signal()
//  //}
//
//  when (clockDomain.isResetActive) {
//    //for elem in loc.arr:
//    //	m.d.sync += elem := (bus.shape()())
//
//    //m.d.sync += [
//    loc.rTail := (0x0)
//    loc.rHead := (0x0)
//
//    //outp.rdData := (bus.shape()())
//
//    //misc.empty := True
//    //misc.full := False
//    loc.rEmpty := True
//    loc.rFull := False
//
//    loc.rRdData := loc.rRdData.getZero
//    //]
//    //GenerationFlags.formal {
//    //  m.d.sync +=
//    //  loc.formal.pastValid := (0b1)
//    //}
//
//  } otherwise { // when (~clockDomain.isResetActive)
//    //--------
//    //m.d.sync += [
//    loc.rEmpty := loc.nextEmpty
//    loc.rFull := loc.nextFull
//    loc.rTail := loc.nextTail
//    loc.rHead := loc.nextHead
//    //]
//
//    when (rdEn & ~misc.empty) {
//      //m.d.sync +=
//      loc.rRdData := loc.arr.readSync(address=loc.rTail)
//    }
//    when (wrEn & ~misc.full) {
//      //m.d.sync += 
//      loc.arr.write(address=loc.rHead.resized, data=wrData)
//    }
//    //--------
//    //GenerationFlags.formal:
//    //  when (loc.formal.pastValid):
//    //    m.d.sync \
//    //    += [
//    //      assert(outp.empty == past(loc.nextEmpty)),
//    //      assert(outp.full == past(loc.nextFull)),
//    //      assert(loc.rTail == past(loc.nextTail)),
//    //      assert(loc.rHead == past(loc.nextHead)),
//    //    ]
//    //    when (past(inp.rdEn)):
//    //      when (past(outp.empty)):
//    //        m.d.sync \
//    //        += [
//    //          //assert(stable(outp.empty)),
//    //          assert(stable(loc.rTail)),
//    //        ]
//    //      otherwise: // If(~past(outp.empty)):
//    //        //when (~past(inp.wrEn)):
//    //        m.d.sync \
//    //        += [
//    //          assert(outp.rdData
//    //            == loc.arr[past(loc.rTail)])
//    //        ]
//    //    when (past(inp.wrEn)):
//    //      when (past(outp.full)):
//    //        m.d.sync \
//    //        += [
//    //          assert(stable(loc.rHead)),
//    //        ]
//    //      //otherwise: // If(~past(outp.full)):
//    //      //	m.d.sync \
//    //      //	+= [
//    //      //		assert(past(inp.wrData))
//    //      //	]
//    //    switch (Cat(outp.empty, outp.full)):
//    //      is (0b00):
//    //        m.d.sync \
//    //        += [
//    //          assume(loc.rHead != loc.rTail),
//    //          assume(loc.formal.testHead != loc.rTail),
//    //        ]
//    //      is (0b01):
//    //        m.d.sync \
//    //        += [
//    //          assert(loc.rHead == loc.rTail)
//    //        ]
//    //      is (0b10):
//    //        m.d.sync \
//    //        += [
//    //          assert(loc.formal.testHead == loc.rTail),
//    //        ]
//    //    m.d.sync \
//    //    += [
//    //      assert(~(outp.empty & outp.full)),
//    //      //assume(~stable(inp.wrData)),
//    //      //assume(inp.wrData == loc.formal.wdCnt),
//    //    ]
//  }
//
//
//
//  //--------
//}
case class AsyncReadFifo[
  T <: Data
](
  dataType: HardType[T],
  depth: Int,
  //arrRamStyle: String="ultra",
  arrRamStyle: String="block",
) extends Component {
  val io = FifoIo(
    dataType=dataType(),
    depth=depth,
  )
  //--------
  val push = io.push
  val pop = io.pop
  val misc = io.misc
  //--------
  def ptrWidth = io.ptrWidth
  def amountWidth = io.amountWidth
  def uintDepthAmtW = U(f"$amountWidth'd$depth")
  def depthMinus1 = depth - 1
  def uintDepthMinus1AmtW = U(f"$amountWidth'd$depthMinus1")
  def uintOnePtrW = U(f"$ptrWidth'd1")
  def uintOneAmtW = U(f"$amountWidth'd1")
  def uintTwoAmtW = U(f"$amountWidth'd2")
  //--------
  val loc = new Area {
    val wrEn = KeepAttribute(Bool())
    wrEn := push.fire
    val wrData = KeepAttribute(dataType())
    wrData := push.payload

    val rdEn = KeepAttribute(Bool())
    rdEn := pop.fire
    val rdData = KeepAttribute(dataType())
    val rPastRdData = KeepAttribute(RegNext(rdData))
    rPastRdData.init(rPastRdData.getZero)
    //rdData := pop.payload
    pop.payload := rdData

    val arr = Mem(
      dataType(),
      depth,
    )
      .addAttribute("ram_style", arrRamStyle)
      .addAttribute("keep")

    val nextHead = KeepAttribute(UInt(ptrWidth bits))
    val rHead = KeepAttribute(RegNext(nextHead)) init(0x0)
    val nextTail = KeepAttribute(UInt(ptrWidth bits))
    val rTail = KeepAttribute(RegNext(nextTail)) init(0x0)

    val headAmtW = UInt(amountWidth bits)
    headAmtW := rHead.resized
    val headPlus1 = UInt(amountWidth bits)
    headPlus1 := headAmtW + uintOneAmtW

    val tailAmtW = UInt(amountWidth bits)
    tailAmtW := rTail.resized
    val tailPlus1 = UInt(amountWidth bits)
    tailPlus1 := tailAmtW + uintOneAmtW

    val formal = new Area {
      //val testHead = (rHead + 0x1) % depth
      val testHead = (
        //(rHead.resized + uintOneAmtW)
        //% U(f"$amountWidth'd$depth")
        headPlus1 % uintDepthAmtW
      )
      val testTail = (
        //(rTail.resized + uintOneAmtW)
        //% U(f"$amountWidth'd$depth")
        tailPlus1 % uintDepthAmtW
      )
    }

    val nextEmpty = KeepAttribute(Bool())
    val rEmpty = KeepAttribute(RegNext(nextEmpty)) init(True)
    val nextFull = KeepAttribute(Bool())
    val rFull = KeepAttribute(RegNext(nextFull)) init(False)
    //val nextNotEmpty = KeepAttribute(Bool())
    //val rNotEmpty = KeepAttribute(RegNext(nextNotEmpty)) init(False)
    //val nextNotFull = KeepAttribute(Bool())
    //val rNotFull = KeepAttribute(RegNext(nextNotFull)) init(True)
    val rTempNotEmpty = KeepAttribute(RegNext(!nextEmpty)) init(False)
    val rTempNotFull = KeepAttribute(RegNext(!nextFull)) init(True)

    val nextAmountCanPush = UInt(amountWidth bits)
    val rAmountCanPush = KeepAttribute(RegNext(nextAmountCanPush))
      .init(depth - 1)
    val nextAmountCanPop = UInt(amountWidth bits)
    val rAmountCanPop = KeepAttribute(RegNext(nextAmountCanPop)) init(0x0)

    FifoIo.calcNextAmountCanPushPop(
      depth=depth,
      someHead=nextHead,
      someTail=nextTail,
      someAmountCanPush=nextAmountCanPush,
      someAmountCanPop=nextAmountCanPop,
    )

    ////val rHead2 = KeepAttribute(RegNext(nextHead)) init(0x0)
    ////val rTail2 = KeepAttribute(RegNext(nextTail)) init(0x0)

    nextEmpty := nextHead === nextTail
    //nextFull := nextAmountCanPop === 0
    //nextFull := nextAmountCanPop === uintDepthMinus1AmtW

    nextFull := nextAmountCanPush === 0

    //nextFull := rAmountCanPush === 0

    //nextNotEmpty := !nextEmpty
    //nextNotFull := !nextFull
    //nextNotEmpty := nextHead =/= nextTail
    //nextNotFull := nextAmountCanPush =/= 0
    //nextNotEmpty := rHead =/= rTail
    //nextNotFull := rAmountCanPush =/= 0
  }
  //--------
  //push.ready := !misc.full

  ////push.ready := !loc.nextFull
  //pop.valid := !misc.empty
  ////pop.valid := !loc.nextEmpty
  push.ready := loc.rTempNotFull
  pop.valid := loc.rTempNotEmpty

  //push.ready := loc.rNotFull
  //pop.valid := loc.rNotEmpty
  //push.ready := loc.nextNotFull
  //pop.valid := loc.nextNotEmpty
  misc.empty := loc.rEmpty
  misc.full := loc.rFull
  misc.amountCanPush := loc.rAmountCanPush
  misc.amountCanPop := loc.rAmountCanPop
  //--------
  //when (clockDomain.isResetActive) {
  //  loc.nextHead := 0x0
  //  loc.nextTail := 0x0
  //  loc.nextEmpty := True
  //  loc.nextFull := False
  //  loc.nextAmountCanPush := uintDepthMinus1AmtW
  //  loc.nextAmountCanPop := 0x0
  //} otherwise { // when (!clockDomain.isResetActive)
    when (loc.wrEn) {
      loc.arr.write(
        address=loc.rHead.resized,
        data=loc.wrData,
      )
      when (loc.headPlus1 === uintDepthAmtW) {
        loc.nextHead := 0x0
      } otherwise {
        loc.nextHead := loc.headPlus1
      }
    } otherwise {
      loc.nextHead := loc.rHead
    }
    when (loc.rdEn) {
      loc.rdData := loc.arr.readAsync(address=loc.rTail.resized)
      when (loc.tailPlus1 === uintDepthAmtW) {
        loc.nextTail := 0x0
      } otherwise {
        loc.nextTail := loc.tailPlus1
      }
    } otherwise {
      loc.rdData := loc.rPastRdData
      loc.nextTail := loc.rTail
    }
  //}
  //--------
  GenerationFlags.formal {
    when (pastValidAfterReset) {
      // assert that the FIFO doesn't overflow
      //when (past(loc.rTail) === past(loc.rHead)) {
      //  when (stable(loc.rHead)) {
      //    assert(stable(loc.rTail))
      //  }
      //}
      //val pastTailResized = KeepAttribute(UInt(amountWidth bits))
      //  .setName("pastTailResized")
      //pastTailResized := past(loc.rTail).resized
      //val pastTailPlus1 = KeepAttribute(UInt(amountWidth bits))
      //  .setName("pastTailPlus1")
      //pastTailPlus1 := pastTailResized + uintOneAmtW
      //val tailEqPastPlus1ModDepth = KeepAttribute(Bool())
      //  .setName("tailEqPastPlus1ModDepth")
      //tailEqPastPlus1ModDepth := (
      //  loc.rTail === (pastTailPlus1 % uintDepthAmtW).resized
      //)

      //val pastHeadResized = KeepAttribute(UInt(amountWidth bits))
      //  .setName("pastHeadResized")
      //pastHeadResized := past(loc.rHead).resized
      //val pastHeadPlus1 = KeepAttribute(UInt(amountWidth bits))
      //  .setName("pastHeadPlus1")
      //pastHeadPlus1 := pastHeadResized + uintOneAmtW
      //val headEqPastPlus1ModDepth = KeepAttribute(Bool())
      //  .setName("headEqPastPlus1ModDepth")
      //headEqPastPlus1ModDepth := (
      //  loc.rHead === (pastHeadPlus1 % uintDepthAmtW).resized
      //)
      assert(loc.rHead < uintDepthAmtW)
      assert(loc.rTail < uintDepthAmtW)

      val rFormalPastRdData = Reg(dataType())
      rFormalPastRdData.init(rFormalPastRdData.getZero)
      when (loc.rdEn) {
        rFormalPastRdData := loc.arr.readAsync(address=loc.rTail.resized)
      }

      when (
        past(loc.rdEn)
        //past(loc.rPastRdEn)
      ) {
        //when (
        //  past(misc.empty)
        //  //misc.empty
        //  //past(loc.nextEmpty)
        //)
        //{
        //  //m.d.sync +=
        //  assert(stable(loc.rTail))
        //} otherwise { // when (~past(misc.empty))
        //  //m.d.sync += 
        //  //assert(loc.rTail === ((past(loc.rTail) + 1) % depth))
        //  assert(tailEqPastPlus1ModDepth)
        //}
        when (past(loc.rTail) + uintOneAmtW === uintDepthAmtW) {
          assert(loc.rTail === 0x0)
        } otherwise {
          assert(loc.rTail === past(loc.rTail) + uintOneAmtW)
        }
        assert(loc.rPastRdData === rFormalPastRdData)
        //assert(
        //  loc.rPastRdData
        //  === past(loc.arr.readAsync(address=past(loc.rTail).resized))
        //)
      } otherwise { // when (~past(loc.rdEn)):
        //m.d.sync += [
        //assert(stable(misc.empty)),
        assert(stable(loc.rTail))
        //assert(stable(loc.rdData))
        assert(stable(loc.rPastRdData))
        //]
      }

      when (
        past(loc.wrEn)
        //past(loc.rPastWrEn)
      ) {
        //when (
        //  past(misc.full)
        //  //misc.full
        //  //past(loc.nextFull)
        //) {
        //  //m.d.sync += 
        //  assert(stable(loc.rHead))
        //} otherwise { // when (~past(misc.full)) 
        //  //m.d.sync +=
        //  //assert(
        //  //  loc.rHead.resized
        //  //  === ((past(loc.rHead).resized + U(f"$ptrWidth'd1")) % depth)
        //  //)
        //  assert(headEqPastPlus1ModDepth)
        //}
        when (past(loc.rHead) + uintOneAmtW === uintDepthAmtW) {
          assert(loc.rHead === 0x0)
        } otherwise {
          assert(loc.rHead === past(loc.rHead) + uintOneAmtW)
        }
        assert(
          loc.arr.readAsync(address=past(loc.rHead).resized)
          === past(loc.wrData)
        )
      } otherwise { // when (~past(loc.wrEn))
        //m.d.sync += [
        //assert(stable(misc.full))
        assert(stable(loc.rHead))
        //]
      }

      //switch (Cat(misc.full, misc.empty)) {
      //  // neither full nor empty
      //  is (B"00") {
      //    //m.d.sync += [
      //    assert(loc.rHead =/= loc.rTail)
      //    assert(loc.formal.testHead =/= loc.rTail)
      //    //]
      //  }

      //  // empty
      //  is (B"01") {
      //    //m.d.sync += [
      //    assert(loc.rHead === loc.rTail)
      //    //]
      //  }

      //  // full
      //  is (B"10") {
      //    //m.d.sync += [
      //    assert(loc.formal.testHead === loc.rTail)
      //    //]
      //  }
      //}

      // neither full nor empty
      when (!misc.full && !misc.empty) {
        assert(loc.rHead =/= loc.rTail)
        assert(loc.formal.testHead =/= loc.rTail)
      }
      // empty
      when (!misc.full && misc.empty) {
        assert(loc.rHead === loc.rTail)
      }
      // full
      when (misc.full && !misc.empty) {
        assert(loc.formal.testHead === loc.rTail)
      }

      // empty
      //when (loc.rHead === loc.rTail) {
      //  //m.d.sync +=
      //  //assert(misc.empty & (~misc.full))
      //  assert(misc.empty)
      //  assert(~misc.full)
      //} elsewhen (loc.formal.testHead === loc.rTail) {
      //  //m.d.sync +=
      //  //assert((~misc.empty) & misc.full)
      //  assert(~misc.empty)
      //  assert(misc.full)
      //} otherwise {
      //  //m.d.sync +=
      //  //assert((~misc.empty) & (~misc.full))
      //  assert(~misc.empty)
      //  assert(~misc.full)
      //}
      when (loc.rHead === loc.rTail) {
        assert(misc.empty)
        assert(~misc.full)
      }
      when (loc.formal.testHead === loc.rTail) {
        assert(~misc.empty)
        assert(misc.full)
      }
    }
  }
  //--------
}
//case class AsyncReadFifo[
//  T <: Data
//](
//  dataType: HardType[T],
//  depth: Int,
//  //arrRamStyle: String="ultra",
//  arrRamStyle: String="block",
//) extends Component {
//  val io = FifoIo(
//    dataType=dataType(),
//    depth=depth,
//  )
//  //--------
//  val push = io.push
//  val pop = io.pop
//  val misc = io.misc
//  //--------
//  def ptrWidth = io.ptrWidth
//  def amountWidth = io.amountWidth
//  def uintDepthAmtW = U(f"$amountWidth'd$depth")
//  def uintOnePtrW = U(f"$ptrWidth'd1")
//  def uintOneAmtW = U(f"$amountWidth'd1")
//  def uintTwoAmtW = U(f"$amountWidth'd2")
//  //--------
//  val loc = new Area {
//    //val sbPush = PipeSkidBuf(
//    //  dataType=dataType(),
//    //  optIncludeBusy=true,
//    //  optUseOldCode=false,
//    //  //optUseOldCode=true,
//    //)
//    //val sbPop = PipeSkidBuf(
//    //  dataType=dataType(),
//    //  optIncludeBusy=true,
//    //  optUseOldCode=false,
//    //  //optUseOldCode=true,
//    //)
//    //sbPush.io.prev << push
//    //pop << sbPop.io.next
//
//    ////val wrBusy = KeepAttribute(Bool())
//    ////sbPush.io.misc.busy := wrBusy
//    //sbPush.io.misc.busy := misc.full
//    //sbPush.io.misc.clear := False
//    //sbPush.io.next.ready := True
//    ////sbPush.io.next.ready := !misc.full
//
//    ////val rdBusy = KeepAttribute(Bool())
//    ////sbPop.io.misc.busy := rdBusy
//    //sbPop.io.misc.busy := misc.empty
//    //sbPop.io.misc.clear := False
//    //sbPop.io.prev.valid := True
//    ////sbPop.io.prev.valid := !misc.empty
//
//    //val wrEn = KeepAttribute(Bool())
//    //wrEn := sbPush.io.next.fire
//    ////wrEn := sbPush.io.prev.fire
//    //val rPastWrEn = KeepAttribute(RegNext(wrEn)) init(False)
//    ////rPastWrEn := wrEn
//    //val wrData = KeepAttribute(dataType())
//    //wrData := sbPush.io.next.payload
//
//    //val rdEn = KeepAttribute(Bool())
//    ////rdEn := sbPop.io.prev.fire
//    //rdEn := sbPop.io.prev.fire
//    ////rdEn := sbPop.io.next.fire
//    //val rPastRdEn = KeepAttribute(RegNext(rdEn)) init(False)
//    ////rPastRdEn := rdEn
//    //val rdData = KeepAttribute(dataType())
//    //sbPop.io.prev.payload := rdData 
//
//    val arr = Mem(
//      dataType(),
//      depth,
//    )
//      .addAttribute("ram_style", arrRamStyle)
//      .addAttribute("keep")
//
//    val nextHead = KeepAttribute(UInt(ptrWidth bits))
//    val rHead = KeepAttribute(RegNext(nextHead)) init(0x0)
//    val nextTail = KeepAttribute(UInt(ptrWidth bits))
//    val rTail = KeepAttribute(RegNext(nextTail)) init(0x0)
//
//    //val incrNextHead = KeepAttribute(UInt(ptrWidth bits))
//    val incrNextHead = KeepAttribute(UInt(amountWidth bits))
//
//    val formal = new Area {
//      //val testHead = (rHead + 0x1) % depth
//      val testHead = (
//        (rHead.resized + U(f"$amountWidth'd1"))
//        //% U(f"$amountWidth'd$depth")
//        % uintDepthAmtW
//      )
//    }
//
//    val dbgHeadPlus1 = KeepAttribute(UInt(amountWidth bits))
//    val headPlus1 = (
//      rHead.resized
//      //+ U(f"$amountWidth'd1")
//      + uintOneAmtW
//    )
//    dbgHeadPlus1 := headPlus1.resized
//
//    val dbgHeadPlus2 = KeepAttribute(UInt(amountWidth bits))
//    val headPlus2 = (
//      rHead.resized
//      //+ U(f"$amountWidth'd1")
//      + uintTwoAmtW
//    )
//    dbgHeadPlus2 := headPlus2.resized
//
//    val dbgTailPlus1 = KeepAttribute(UInt(amountWidth bits))
//    val tailPlus1 = (
//      rTail.resized
//      //+ U(f"$amountWidth'd1")
//      + uintOneAmtW
//    )
//    dbgTailPlus1 := tailPlus1.resized
//
//    //val nextHeadPlus1 = nextHead + 0x1
//    //val dbgNextHeadPlus1 = KeepAttribute(UInt(amountWidth bits))
//    val nextHeadPlus1 = KeepAttribute(UInt(ptrWidth bits))
//    //val nextHeadPlus1 = (
//    //  nextHead.resized
//    //  //+ U(f"$ptrWidth'd1")
//    //  + uintOneAmtW
//    //)
//    //dbgNextHeadPlus1 := nextHeadPlus1
//
//    val nextEmpty = KeepAttribute(Bool())
//    val rEmpty = KeepAttribute(RegNext(nextEmpty)) init(False)
//    val nextFull = KeepAttribute(Bool())
//    val rFull = KeepAttribute(RegNext(nextFull)) init(False)
//
//    val nextAmountCanPush = UInt(amountWidth bits)
//    val nextAmountCanPop = UInt(amountWidth bits)
//    val rAmountCanPush = RegNext(nextAmountCanPush) init(0x0)
//    val rAmountCanPop = RegNext(nextAmountCanPop) init(0x0)
//
//    //val nextAmountCanPush = rAmountCanPush.wrapNext()
//    //val nextAmountCanPop = rAmountCanPop.wrapNext()
//    //rAmountCanPush := nextAmountCanPush
//    //rAmountCanPop := nextAmountCanPop
//    //nextAmountCanPush := (
//    //  //U(f"$amountWidth'd$tempDepth") - nextAmountCanPop
//    //  U(f"$amountWidth'd$depth") - nextAmountCanPop
//    //)
//    val tempNextTail = KeepAttribute(UInt(amountWidth bits))
//    tempNextTail := nextTail.resized
//    val tempNextHead = KeepAttribute(UInt(amountWidth bits))
//    tempNextHead := nextHead.resized
//    ////nextAmountCanPop := Mux(
//    ////  tempNextHead > tempNextTail,
//    ////  tempNextHead - tempNextTail,
//    ////  U(f"$amountWidth'd$tempDepth") + (tempNextHead - tempNextTail),
//    ////)
//    //nextAmountCanPop := Mux(
//    //  rHead >= rTail,
//    //  rHead - rTail,
//    //  //U(f"$amountWidth'd$tempDepth") - (rTail - rHead),
//    //  U(f"$amountWidth'd$depth") - (rTail - rHead),
//    //)
//    FifoIo.calcNextAmountCanPushPop(
//      depth=depth - 1,
//      someHead=tempNextHead,
//      someTail=tempNextTail,
//      //someHead=rHead,
//      //someTail=rTail,
//      someAmountCanPush=nextAmountCanPush,
//      someAmountCanPop=nextAmountCanPop,
//    )
//    //calcNextAmountCanPushPop(
//    //  someHead=rHead,
//    //  someTail=rTail,
//    //  someAmountCanPush=rAmountCanPush,
//    //  someAmountCanPop=rAmountCanPop,
//    //)
//    //misc.amountCanPush := rAmountCanPush
//    //misc.amountCanPop := rAmountCanPop
//  }
//  //--------
//  // Combinational logic
//
//  //when ((~outp.empty) & inp.rdEn):
//  //	m.d.comb += outp.rdData.eq(loc.arr[loc.tail])
//
//  //m.d.comb +=
//  loc.rdData := loc.arr.readAsync(address=loc.rTail.resized)
//
//  // Compute `loc.nextHead` and write into the FIFO if it's not full
//  //loc.headPlus1 = loc.head + 0x1
//  when (clockDomain.isResetActive) {
//    //m.d.comb += [
//    loc.nextHead := 0x0
//    loc.nextTail := 0x0
//    loc.incrNextHead := 0x0
//    loc.nextEmpty := True
//    loc.nextFull := False
//    //]
//  } otherwise { // when (~clockDomain.isResetActive):
//    when (
//      //(!misc.full)
//      //loc.rHead =/= loc.rTail
//      //loc.rHead =/= loc.rTail
//      !loc.rFull
//      && loc.wrEn
//    ) {
//      //m.d.sync += loc.arr[loc.head] := (inp.wrData)
//
//      when (loc.dbgHeadPlus1 =/= uintDepthAmtW) {
//        //m.d.comb +=
//        loc.nextHead := 0x0
//      } otherwise {
//        //m.d.comb += 
//        loc.nextHead := loc.headPlus1
//      }
//    } otherwise {
//      //m.d.comb += 
//      loc.nextHead := loc.rHead
//    }
//
//    // Compute `loc.nextTail`
//    //loc.tailPlus1 = loc.tail + 0x1
//    when (
//      //(!misc.empty)
//      //!loc.nextEmpty
//      //(loc.rTail =/= loc.rHead)
//      !loc.rEmpty
//      && loc.rdEn
//    ) {
//      when (loc.dbgTailPlus1 >= uintDepthAmtW) {
//        //m.d.comb +=
//        loc.nextTail := 0x0
//      } otherwise {
//        //m.d.comb += 
//        loc.nextTail := loc.tailPlus1
//      }
//    } otherwise {
//      //m.d.comb += 
//      loc.nextTail := loc.rTail
//    }
//    when (loc.nextTail === loc.nextHead) {
//      loc.nextFull := False
//      loc.nextEmpty := True 
//    } elsewhen (
//      (loc.nextHead.resized + uintOneAmtW === uintDepthAmtW)
//      && (loc.nextTail.resized === 0x0)
//    ) {
//      loc.nextFull := 
//    } otherwise {
//    }
//
//    // Compute `loc.nextEmpty` and `loc.nextFull`
//    //loc.nextHeadPlus1 = loc.nextHead + 0x1
//
//    //when (loc.nextHeadPlus1 >= depth) {
//    //  //m.d.comb += 
//    //  loc.incrNextHead := 0x0
//    //} otherwise {
//    //  //m.d.comb += 
//    //  loc.incrNextHead := loc.nextHeadPlus1
//    //}
//
//    //when (
//    //  loc.incrNextHead === loc.nextTail
//    //) {
//    //  //m.d.comb += [
//    //  loc.nextEmpty := False
//    //  loc.nextFull := True
//    //  //]
//    //} elsewhen (loc.nextHead === loc.nextTail) {
//    //  //m.d.comb += [
//    //  loc.nextEmpty := True
//    //  loc.nextFull := False
//    //  //]
//    //} otherwise {
//    //  //m.d.comb += [
//    //  loc.nextEmpty := False
//    //  loc.nextFull := False
//    //  //]
//    //}
//    //when (
//    //  loc.nextHead + 1 === loc.nextTail
//    //) {
//    //} elsewhen (loc.nextHead === loc.nextTail) {
//    //} otherwise {
//    //}
//  }
//  //if self.FORMAL():
//  //	m.d.comb \
//  //	+= [
//  //		Cover(loc.formal.pastValid)
//  //		//Cover(loc.formal.pastValid & outp.empty),
//  //		//Cover(loc.formal.pastValid & outp.full),
//  //		//Cover(loc.formal.pastValid & (~outp.empty)
//  //		//	& (~outp.full)),
//  //	]
//  //--------
//  // Sequential logic
//
//  //m.d.sync += [
//  //loc.rHead.eq(loc.next_head),
//  //loc.rTail.eq(loc.next_tail),
//  //outp.empty.eq(loc.next_empty),
//  //outp.full.eq(loc.next_full),
//  //]
//  misc.empty := loc.rEmpty
//  misc.full := loc.rFull
//  //with m.If(loc.rst):
//  //	//--------
//  //	m.d.sync \
//  //	+= [
//  //		loc.rHead.eq(0x0),
//  //		loc.rTail.eq(0x0),
//
//  //		outp.empty.eq(True),
//  //		outp.full.eq(False),
//  //	]
//  //	//--------
//  //with m.Else(): // If(~loc.rst):
//  //with m.If(~loc.rst):
//    //--------
//  // Write into the FIFO
//  when(
//    (~misc.full)
//    & loc.wrEn
//    //& loc.rPastWrEn
//  ) {
//    loc.arr.write(
//      address=loc.rHead.resized,
//      data=loc.wrData,
//    )
//  }
//  GenerationFlags.formal {
//    //m.d.comb += Cover(loc.formal.past_valid)
//    when (pastValidAfterReset) {
//      //m.d.sync \
//      //+= [
//      //	Cover(outp.empty),
//      //	Cover(outp.full),
//      //	Cover(~(outp.empty & outp.full)),
//      //]
//
//      //m.d.sync += [
//      //assert(loc.head == past(loc.nextHead)),
//      //assert(loc.tail == past(loc.nextTail)),
//      //assert(misc.empty == past(loc.nextEmpty)),
//      //assert(misc.full == past(loc.nextFull)),
//      assert(~(misc.empty & misc.full))
//
//      assert(loc.rdData === loc.arr.readAsync(address=loc.rTail.resized))
//      //]
//
//      val pastTailResized = KeepAttribute(UInt(amountWidth bits))
//        .setName("pastTailResized")
//      pastTailResized := past(loc.rTail).resized
//      val pastTailPlus1 = KeepAttribute(UInt(amountWidth bits))
//        .setName("pastTailPlus1")
//      pastTailPlus1 := pastTailResized + uintOneAmtW
//      val tailEqPastPlus1ModDepth = KeepAttribute(Bool())
//        .setName("tailEqPastPlus1ModDepth")
//      tailEqPastPlus1ModDepth := (
//        loc.rTail === (pastTailPlus1 % uintDepthAmtW).resized
//      )
//
//      val pastHeadResized = KeepAttribute(UInt(amountWidth bits))
//        .setName("pastHeadResized")
//      pastHeadResized := past(loc.rHead).resized
//      val pastHeadPlus1 = KeepAttribute(UInt(amountWidth bits))
//        .setName("pastHeadPlus1")
//      pastHeadPlus1 := pastHeadResized + uintOneAmtW
//      val headEqPastPlus1ModDepth = KeepAttribute(Bool())
//        .setName("headEqPastPlus1ModDepth")
//      headEqPastPlus1ModDepth := (
//        loc.rHead === (pastHeadPlus1 % uintDepthAmtW).resized
//      )
//      // assert that the FIFO doesn't overflow
//      when (past(loc.rTail) === past(loc.rHead)) {
//        when (stable(loc.rHead)) {
//          assert(stable(loc.rTail))
//        }
//      }
//
//      when (
//        past(loc.rdEn)
//        //past(loc.rPastRdEn)
//      ) {
//        when (
//          past(misc.empty)
//          //misc.empty
//          //past(loc.nextEmpty)
//        )
//        {
//          //m.d.sync +=
//          assert(stable(loc.rTail))
//        } otherwise { // when (~past(misc.empty))
//          //m.d.sync += 
//          //assert(loc.rTail === ((past(loc.rTail) + 1) % depth))
//          assert(tailEqPastPlus1ModDepth)
//        }
//      } otherwise { // when (~past(loc.rdEn)):
//        //m.d.sync += [
//        //assert(stable(misc.empty)),
//        assert(stable(loc.rTail))
//        //]
//      }
//
//      when (
//        past(loc.wrEn)
//        //past(loc.rPastWrEn)
//      ) {
//        when (
//          past(misc.full)
//          //misc.full
//          //past(loc.nextFull)
//        ) {
//          //m.d.sync += 
//          assert(stable(loc.rHead))
//        } otherwise { // when (~past(misc.full)) 
//          //m.d.sync +=
//          //assert(
//          //  loc.rHead.resized
//          //  === ((past(loc.rHead).resized + U(f"$ptrWidth'd1")) % depth)
//          //)
//          assert(headEqPastPlus1ModDepth)
//        }
//      } otherwise { // when (~past(loc.wrEn))
//        //m.d.sync += [
//        //assert(stable(misc.full))
//        assert(stable(loc.rHead))
//        //]
//      }
//
//      switch (Cat(misc.full, misc.empty)) {
//        // neither full nor empty
//        is (B"00") {
//          //m.d.sync += [
//          assert(loc.rHead =/= loc.rTail)
//          assert(loc.formal.testHead =/= loc.rTail)
//          //]
//        }
//
//        // empty
//        is (B"01") {
//          //m.d.sync += [
//          assert(loc.rHead === loc.rTail)
//          //]
//        }
//
//        // full
//        is (B"10") {
//          //m.d.sync += [
//          assert(loc.formal.testHead === loc.rTail)
//          //]
//        }
//      }
//
//      // empty
//      when (loc.rHead === loc.rTail) {
//        //m.d.sync +=
//        assert(misc.empty & (~misc.full))
//      } elsewhen (loc.formal.testHead === loc.rTail) {
//        //m.d.sync +=
//        assert((~misc.empty) & misc.full)
//      } otherwise {
//        //m.d.sync +=
//        assert((~misc.empty) & (~misc.full))
//      }
//    }
//  }
//}

//case class AsyncReadFifo[
//  T <: Data
//](
//  dataType: HardType[T],
//  depth: Int,
//  //arrRamStyle: String="ultra",
//  arrRamStyle: String="block",
//) extends Component {
//  //val test = StreamFifo(dataType())
//  //--------
//  //def tempDepth = depth + 1
//  val io = FifoIo(
//    dataType=dataType(),
//    depth=depth,
//  )
//  //--------
//  // Local variables
//  //val inp = io.inp
//  //val outp = io.outp
//  //--------
//  val push = io.push
//  val pop = io.pop
//  val misc = io.misc
//  //--------
//  //def tempDepth = io.tempDepth
//  def ptrWidth = io.ptrWidth
//  def amountWidth = io.amountWidth
//  //--------
//  val tempPushBusy = KeepAttribute(Bool())
//  val tempPopBusy = KeepAttribute(Bool())
//
//  val sbPush = PipeSkidBuf(
//    dataType=dataType(),
//    //optIncludeBusy=true,
//    optIncludeBusy=true,
//    //optIncludeBusy=false,
//    //optIncludeNextBusy=false,
//    //optIncludeNextBusy=true,
//    //optIncludePrevBusy=true,
//    optUseOldCode=false,
//    //optUseOldCode=true,
//  )
//  val sbPop = PipeSkidBuf(
//    dataType=dataType(),
//    //optIncludeBusy=true,
//    optIncludeBusy=true,
//    //optIncludeBusy=false,
//    //optIncludeNextBusy=true,
//    ////optIncludePrevBusy=false,
//    //optIncludePrevBusy=true,
//    optUseOldCode=false,
//    //optUseOldCode=true,
//  )
//  //val sbPushIo = sbPush.io
//  //val sbPopIo = sbPop.io
//  //sbPush.io.prev <> push
//  //sbPush.io.prev >> push
//  //val sbPushBusy = sbPush.io.busy
//  //val sbPopBusy = sbPop.io.busy
//  //sbPush.io.misc.busy := misc.full
//  //sbPush.io.misc.busy := tempPushBusy
//  sbPush.io.misc.clear := False
//  //sbPop.io.misc.busy := misc.empty
//  //sbPop.io.misc.busy := tempPopBusy
//  sbPop.io.misc.clear := False
//
//  push >> sbPush.io.prev
//  //sbPush.io.prev.payload := push.payload
//  //sbPush.io.prev.valid := push.valid
//  //push.ready := sbPush.io.prev.ready
//
//  //sbPush.io.next.ready := True
//  //val wrData = sbPush.io.prev.payload
//  //val wrEn = sbPush.io.prev.fire
//  //val wrData = push.payload
//
//  //val wrReady = KeepAttribute(Bool())
//  //sbPush.io.next.ready := wrReady
//
//  val nextWrData = KeepAttribute(cloneOf(push.payload))
//  val wrData = KeepAttribute(RegNext(nextWrData))
//  wrData.init(wrData.getZero)
//  //wrData := push.payload
//  nextWrData := sbPush.io.next.payload
//
//  val wrBusy = KeepAttribute(Bool())
//  sbPush.io.misc.busy := wrBusy
//  sbPush.io.next.ready := True
//  //sbPush.io.next.ready := !wrBusy
//  //sbPush.io.next.ready := !misc.full
//
//  val nextWrEn = Bool()
//  val wrEn = KeepAttribute(RegNext(nextWrEn)) init(False)
//  nextWrEn := sbPush.io.next.fire
//  //wrEn := sbPush.io.next.fire
//  //wrEn := push.fire
//  //val pushBusy = sbPush.io.misc.busy
//
//  pop << sbPop.io.next
//  //pop.payload := sbPop.io.next.payload
//  //pop.valid := sbPop.io.next.valid
//  //sbPop.io.next.ready := pop.ready
//
//  //sbPop.io.prev.valid := True
//
//  //val rdValid = Bool()
//  //sbPop.io.prev.valid := rdValid
//  val rdBusy = KeepAttribute(Bool())
//  sbPop.io.misc.busy := rdBusy
//  sbPop.io.prev.valid := True
//  //sbPop.io.prev.valid := !rdBusy
//  //sbPop.io.prev.valid := !misc.empty
//
//  //val rdData = sbPop.io.prev.payload
//
//  //val rdDataPrev = Reg(dataType()) init(dataType().getZero)
//  //rdData \= rdData.getZero
//  val rdData = KeepAttribute(cloneOf(pop.payload))
//  val rRdData = KeepAttribute(RegNext(rdData))
//  rRdData.init(rRdData.getZero)
//  sbPop.io.prev.payload := rRdData
//  //val rdData = sbPop.io.next.payload
//  //val popBusy = sbPop.io.misc.busy
//  //val rdEn = sbPop.io.next.fire
//  val nextRdEn = KeepAttribute(Bool())
//  val rdEn = KeepAttribute(RegNext(nextRdEn)) init(False)
//  //nextRdEn := sbPop.io.prev.fire
//  nextRdEn := sbPop.io.next.fire
//  //rdEn := sbPop.io.prev.fire
//
//  //val wrData = io.push.payload
//  //val wrEn = io.push.fire
//
//  //val rdEn = KeepAttribute(Bool())
//  //rdEn := io.pop.fire
//
//  ////val tempPushBusy = Bool()
//  ////val tempIoPush = io.push.haltWhen(tempPushBusy)
//  //tempPushBusy := misc.full
//  //tempPopBusy := misc.empty
//  ////val tempPush = new Stream(dataType()).haltWhen(tempPushBusy)
//  ////val tempPop = new Stream(dataType()).haltWhen(tempPopBusy)
//  ////val tempIoPush = push.haltWhen(tempPushBusy)
//  //val tempIoPush = push
//  //val myPush = new Stream(dataType())
//  //tempIoPush >/-> myPush
//  ////tempPush.ready := True
//  //myPush.ready := True
//  //val myPop = new Stream(dataType())
//  ////val tempIoPop = myPop.haltWhen(tempPopBusy)
//  //val tempIoPop = myPop
//  //pop <-/< tempIoPop
//  ////myPop.valid := True
//  //myPop.valid := !misc.empty
//
//  //val wrData = myPush.payload
//  //val wrEn = KeepAttribute(Bool())
//  //wrEn := myPush.fire
//  ////val rWrReady = Reg(Bool()) init(False)
//
//  //val rdEn = KeepAttribute(Bool())
//  //rdEn := myPop.fire
//  //val rdData = myPop.payload
//  ////val rRdValid = Reg(Bool()) init(False)
//
//  ////val wrData = cloneOf(io.push.payload)
//
//  val loc = new Area {
//    //val rEmpty = Reg(Bool()) init(True)
//    //val rFull = Reg(Bool()) init(False)
//    val nextEmpty = Bool()
//    val nextFull = Bool()
//    val rEmpty = RegNext(nextEmpty) init(True)
//    val rFull = RegNext(nextFull) init(False)
//    //arr = Array([
//    //  Signal(bus.shape()) for _ in range(bus.SIZE())
//    //])
//    val arr = Mem(
//      dataType(),
//      //tempDepth
//      depth
//    )
//      .addAttribute("ram_style", arrRamStyle)
//      .addAttribute("keep")
//    //arr = Array(
//    //  [
//    //    Signal(bus.shape()) for _ in range(bus.SIZE())
//    //  ],
//    //  attrs={"ram_style": "ultra"}
//    //)
//    //m = Blank()
//    //m.submodules.mwrap = m.mwrap = MemWrapper(
//    //  shape=bus.shape(),
//    //  tempDepth=bus.SIZE(),
//    //  init=[0x0 for _ in range(bus.SIZE())],
//    //  attrs=mem_attrs,
//    //)
//
//    //val ptrWidth = log2Up(tempDepth)
//    def ptrWidthPlus1 = ptrWidth + 1
//    //val ptrWidthPlus1 = ptrWidthPlus1
//    //def uintDepth = U(f"$ptrWidthPlus1'd$tempDepth")
//    def uintDepth = U(f"$ptrWidthPlus1'd$depth")
//
//    val nextHead = UInt(ptrWidth bits)
//    val nextTail = UInt(ptrWidth bits)
//
//    //val rHead = Reg(UInt(ptrWidth bits)) init(0x0)
//    //val rTail = Reg(UInt(ptrWidth bits)) init(0x0)
//    val rHead = RegNext(nextHead) init(0x0)
//    val rTail = RegNext(nextTail) init(0x0)
//
//    val tempTailPlus1 = KeepAttribute(UInt(ptrWidthPlus1 bits))
//    //val tailPlus1 = (rTail.resized + U(f"$ptrWidthPlus1'b1")).resized
//    //tempTailPlus1 := tailPlus1
//    tempTailPlus1 := rTail.resized + U(f"$ptrWidthPlus1'b1")
//
//    //val tempHeadPlus1 = UInt(ptrWidthPlus1 bits) addAttribute("keep")
//    //val headPlus1 = (rHead.resized + U(f"$ptrWidthPlus1'b1")).resized
//    //tempHeadPlus1 := headPlus1 
//    val tempHeadPlus1 = KeepAttribute(UInt(ptrWidthPlus1 bits))
//    tempHeadPlus1 := rHead.resized + U(f"$ptrWidthPlus1'b1")
//
//    case class NextEmptyFull(
//      tempNextEmpty: Bool,
//      tempNextFull: Bool,
//    ) {
//    }
//    //val nextEmptyFull = NextEmptyFull(
//    //  //tempNextEmpty=Mux[Bool](
//    //  //  incrNextHead === nextTail,
//    //  //  False,
//    //  //  Mux(
//    //  //    nextHead === nextTail,
//    //  //    True,
//    //  //    False,
//    //  //  )
//    //  //),
//    //  //tempNextFull=Mux[Bool](
//    //  //  incrNextHead === nextTail,
//    //  //  True,
//    //  //  Mux(
//    //  //    nextHead === nextTail,
//    //  //    False,
//    //  //    False,
//    //  //  )
//    //  //)
//    //  tempNextEmpty=(
//    //    (incrNextHead =/= nextTail)
//    //    && (nextHead === nextTail)
//    //  ),
//    //  tempNextFull=(
//    //    (incrNextHead === nextTail)
//    //  )
//    //)
//    val nextEmptyFull = NextEmptyFull(
//      tempNextEmpty=(
//        //incrNextHead =/= nextTail
//        tempHeadPlus1 =/= nextTail
//        && nextHead === nextTail
//      ),
//      tempNextFull=(
//        //incrNextHead === nextTail
//        tempHeadPlus1 === nextTail
//      ),
//    )
//    nextEmpty := nextEmptyFull.tempNextEmpty
//    nextFull := nextEmptyFull.tempNextFull
//
//    //rdValid := (
//    //  tempHeadPlus1 =/= nextTail
//    //  && nextHead === nextTail
//    //)
//
//    //when (
//    //  //tempHeadPlus1 >= uintDepth
//    //  rHead === uintDepth - 1
//    //) {
//    //  //rdValid := (
//    //  //)
//    //  //rdValid := rTail
//    //  rdValid := rTail =/= rHead
//    //} otherwise {
//    //  //rdValid := tempHeadPlus1
//    //}
//
//    //when (
//    //  rTail === uintDepth - 1
//    //) {
//    //  rdValid := (
//    //    True
//    //  )
//    //} otherwise {
//    //  rdValid := (
//    //    True
//    //  )
//    //}
//
//    //when (rdEn) {
//    //} otherwise {
//    //}
//    //rdValid := !misc.empty
//
//    //val nextEmpty = nextEmptyFull.tempNextEmpty
//    //val nextFull = nextEmptyFull.tempNextFull
//    //val nextEmpty = Bool()
//    //val nextFull = Bool()
//
//    val nextAmountCanPush = UInt(amountWidth bits)
//    val nextAmountCanPop = UInt(amountWidth bits)
//    val rAmountCanPush = RegNext(nextAmountCanPush) init(0x0)
//    val rAmountCanPop = RegNext(nextAmountCanPop) init(0x0)
//
//    val nextNonRstNextHead = UInt(ptrWidth bits)
//    val rNonRstNextHead = RegNext(nextNonRstNextHead) init(0x0)
//    nextNonRstNextHead := Mux[UInt](
//      !misc.full 
//      //!nextFull
//      && wrEn,
//      //&& !sbPush.io.misc.busy,
//      //&& nextWrEn,
//      Mux[UInt](
//        tempHeadPlus1 >= uintDepth,
//        U(f"$ptrWidth'd0"),
//        tempHeadPlus1(rHead.bitsRange),
//      ),
//      rHead,
//    )
//    val nextNonRstNextTail = UInt(ptrWidth bits)
//    val rNonRstNextTail = RegNext(nextNonRstNextTail) init(0x0)
//    nextNonRstNextTail := Mux[UInt](
//      !misc.empty
//      //!nextEmpty
//      //!sbPop.io.misc.busy
//      && 
//      rdEn,
//      //&& nextRdEn,
//      Mux[UInt](
//        tempTailPlus1 >= uintDepth,
//        U(f"$ptrWidth'd0"),
//        tempTailPlus1(rTail.bitsRange),
//      ),
//      rTail,
//    )
//    def calcNextAmountCanPushPop(
//      someHead: UInt,
//      someTail: UInt,
//      someAmountCanPush: UInt,
//      someAmountCanPop: UInt,
//    ): Unit = {
//
//      someAmountCanPush := (
//        //U(f"$amountWidth'd$tempDepth") - nextAmountCanPop
//        U(f"$amountWidth'd$depth") - someAmountCanPop
//      )
//      
//      someAmountCanPop := Mux[UInt](
//        someHead >= someTail,
//        someHead - someTail,
//        //U(f"$amountWidth'd$tempDepth") - (someTail - someHead),
//        U(f"$amountWidth'd$depth") - (someTail - someHead),
//      )
//    }
//
//    //val nextAmountCanPush = rAmountCanPush.wrapNext()
//    //val nextAmountCanPop = rAmountCanPop.wrapNext()
//    //rAmountCanPush := nextAmountCanPush
//    //rAmountCanPop := nextAmountCanPop
//    //nextAmountCanPush := (
//    //  //U(f"$amountWidth'd$tempDepth") - nextAmountCanPop
//    //  U(f"$amountWidth'd$depth") - nextAmountCanPop
//    //)
//    val tempNextTail = KeepAttribute(UInt(amountWidth bits))
//    tempNextTail := nextTail.resized
//    val tempNextHead = KeepAttribute(UInt(amountWidth bits))
//    tempNextHead := nextHead.resized
//    ////nextAmountCanPop := Mux(
//    ////  tempNextHead > tempNextTail,
//    ////  tempNextHead - tempNextTail,
//    ////  U(f"$amountWidth'd$tempDepth") + (tempNextHead - tempNextTail),
//    ////)
//    //nextAmountCanPop := Mux(
//    //  rHead >= rTail,
//    //  rHead - rTail,
//    //  //U(f"$amountWidth'd$tempDepth") - (rTail - rHead),
//    //  U(f"$amountWidth'd$depth") - (rTail - rHead),
//    //)
//    calcNextAmountCanPushPop(
//      someHead=tempNextHead,
//      someTail=tempNextTail,
//      //someHead=rHead,
//      //someTail=rTail,
//      someAmountCanPush=nextAmountCanPush,
//      someAmountCanPop=nextAmountCanPop,
//    )
//    //calcNextAmountCanPushPop(
//    //  someHead=rHead,
//    //  someTail=rTail,
//    //  someAmountCanPush=rAmountCanPush,
//    //  someAmountCanPop=rAmountCanPop,
//    //)
//    misc.amountCanPush := rAmountCanPush
//    misc.amountCanPop := rAmountCanPop
//
//    //nextEmpty := nextAmountCanPush === depth
//    //nextFull := nextAmountCanPop === depth
//    //rdBusy := nextAmountCanPush === depth
//    //wrBusy := nextAmountCanPop === depth
//
//    rdBusy := nextAmountCanPush === depth
//    wrBusy := nextAmountCanPop === depth
//
//    //sbPop.io.misc.busy := nextEmpty
//    //sbPush.io.misc.busy := nextFull
//    //sbPop.io.misc.busy := misc.empty
//    //sbPush.io.misc.busy := misc.full
//
//    //misc.amountCanPush := nextAmountCanPush
//    //misc.amountCanPop := nextAmountCanPop
//    //nextEmpty := nextAmountCanPop === 0
//    //nextFull := nextAmountCanPush === 0
//
//    //nextEmpty := True
//    //nextFull := True
//
//    //nextEmpty := nextAmountCanPop === 0
//    //nextFull := nextAmountCanPush === 0
//    //tempPushBusy := nextFull
//    //tempPopBusy := nextEmpty
//    //sbPush.io.misc.busy := nextFull
//    //sbPop.io.misc.busy := nextEmpty
//    //sbPush.io.misc.busy := misc.full
//    //sbPop.io.misc.busy := misc.empty
//    //sbPush.io.misc.prevBusy := misc.full
//    //sbPop.io.misc.nextBusy := misc.empty
//
//    //sbPush.io.misc.nextBusy := misc.full
//    //sbPop.io.misc.prevBusy := misc.empty
//  }
//  val locFormal = new Area {
//    val lastTailVal = Reg(dataType()) init(dataType().getZero)
//    lastTailVal.addAttribute("keep")
//    //val testHead = ((loc.rHead + 0x1) % tempDepth)
//    val testHead = ((loc.rHead + 0x1) % depth)
//  }
//  GenerationFlags.formal {
//    locFormal.lastTailVal := loc.arr.readAsync(address=loc.rTail.resized)
//  }
//  //--------
//  // Combinational logic
//
//  //when((~loc.rEmpty) & rdEn):
//  // m.d.comb += rdData := (loc.arr(loc.rTail))
//
//  //m.d.comb += [
//  val rPastRdData = Reg(cloneOf(rdData))
//  rPastRdData.init(rPastRdData.getZero)
//  rPastRdData := rdData
//  when (
//    //rdEn 
//    nextRdEn
//    && !rdBusy
//  ) {
//    rdData := loc.arr.readAsync(address=loc.rTail.resized)
//  } otherwise {
//    rdData := rPastRdData 
//  }
//  //rdValid := True
//  //wrReady := True
//
//  //wrReady := !misc.full
//  //rdValid := !misc.empty
//  //wrReady := !loc.nextFull
//  //rdValid := !loc.nextEmpty
//  //wrReady := (loc.nextHead =/= loc.nextTail)
//  //wrReady := !loc.nextFull
//  //rdValid := !loc.nextEmpty
//  //wrReady := !loc.rFull
//  //rdValid := !loc.rEmpty
//  misc.empty := loc.rEmpty
//  misc.full := loc.rFull
//  //]
//
//  // Compute `loc.nextHead` and write into the FIFO if it's not full
//  //loc.headPlus1 = loc.rHead + 0x1
//  when (clockDomain.isResetActive) {
//    //m.d.comb += [
//    loc.nextHead := 0x0
//    loc.nextTail := 0x0
//    //]
//  } otherwise { // when (~loc.rst)
//    loc.nextHead := loc.nextNonRstNextHead
//    //when (
//    //  //~misc.full 
//    //  ~loc.nextFull
//    //  & wrEn
//    //) {
//    //  //m.d.sync += loc.arr(loc.rHead) := wrData
//
//    //  when (loc.headPlus1 >= loc.uintDepth) {
//    //  //when (loc.tempHeadPlus1 >= loc.uintDepth)
//    //    //m.d.comb += 
//    //    loc.nextHead := 0x0
//    //  } otherwise {
//    //    //m.d.comb += 
//    //    loc.nextHead := loc.headPlus1.resized
//    //  }
//    //} otherwise {
//    //  //m.d.comb += 
//    //  loc.nextHead := loc.rHead
//    //}
//
//    // Compute `loc.nextTail`
//    //loc.tailPlus1 = loc.rTail + 0x1
//    //when (
//    //  //~misc.empty
//    //  ~loc.nextEmpty
//    //  & rdEn
//    //) {
//    //  when (loc.tailPlus1 >= loc.uintDepth) {
//    //  //when (loc.tempTailPlus1 >= loc.uintDepth) 
//    //  
//    //    //m.d.comb +=
//    //    loc.nextTail := 0x0
//    //  } otherwise {
//    //    //m.d.comb += 
//    //    loc.nextTail := loc.tailPlus1.resized
//    //  }
//    //} otherwise {
//    //  //m.d.comb += 
//    //  loc.nextTail := loc.rTail
//    //}
//    loc.nextTail := loc.nextNonRstNextTail
//
//    // Compute `loc.nextEmpty` and `loc.nextFull`
//    //loc.nextHeadPlus1 = loc.nextHead + 0x1
//    //when (loc.nextHeadPlus1 >= loc.uintDepth) {
//    //  //m.d.comb += 
//    //  loc.incrNextHead := 0x0
//    //} otherwise {
//    //  //m.d.comb += 
//    //  loc.incrNextHead := loc.nextHeadPlus1.resized
//    //}
//
//    //when (loc.incrNextHead === loc.nextTail) {
//    //  //m.d.comb += [
//    //  loc.nextEmpty := False
//    //  loc.nextFull := True
//    //  //]
//    //} elsewhen (loc.nextHead === loc.nextTail) {
//    //  //m.d.comb += [
//    //  loc.nextEmpty := True
//    //  loc.nextFull := False
//    //  //]
//    //} otherwise {
//    //  //m.d.comb += [
//    //  loc.nextEmpty := False
//    //  loc.nextFull := False
//    //  //]
//    //}
//  }
//  ////if self.FORMAL():
//  //// m.d.comb \
//  //// += [
//  ////   cover(pastValid)
//  ////   //cover(pastValid & misc.empty),
//  ////   //cover(pastValid & misc.full),
//  ////   //cover(pastValid & (~misc.empty)
//  ////   // & (~misc.full)),
//  //// )
//  ////--------
//  //// Sequential logic
//  ////when (loc.rst):
//  //// if self.FORMAL():
//  ////   m.d.sync += pastValid := (0b1)
//  ////if self.FORMAL():
//  ////GenerationFlags.formal {
//  ////  m.d.sync += pastValid := (0b1)
//  ////}
//
//  //m.d.sync += [
//  //loc.rHead := loc.nextHead
//  //loc.rTail := loc.nextTail
//  //loc.rEmpty := loc.nextEmpty
//  //loc.rFull := loc.nextFull
//  //]
//  //when (clockDomain.isResetActive) {
//  // //--------
//  // //m.d.sync += [
//  //   loc.rHead := 0x0
//  //   loc.rTail := 0x0
//
//  //   misc.empty := True
//  //   misc.full := False
//  // //]
//  //}
//  // //--------
//  //otherwise // when(~clockDomain.isResetActive):
//  when (~clockDomain.isResetActive) {
//    //--------
//    // Write into the FIFO
//    when (
//      //!misc.full
//      //!loc.nextFull
//      //&& 
//      //wrEn
//      !wrBusy
//      && nextWrEn
//    ) {
//      //m.d.sync += 
//      //loc.arr.write(address=loc.rHead.resized, data=wrData)
//      loc.arr.write(address=loc.rHead.resized, data=nextWrData)
//    }
//    //--------
//    //if self.FORMAL():
//    GenerationFlags.formal {
//      //m.d.comb += cover(pastValid)
//      when (pastValidAfterReset) {
//        val tempTailWidth = loc.rTail.getWidth + 1
//        val tempHeadWidth = loc.rHead.getWidth + 1
//        val tempTailVec = Vec(UInt(tempTailWidth bits), 2)
//        val tempHeadVec = Vec(UInt(tempHeadWidth bits), 2)
//        tempTailVec(0) := loc.rTail.resized
//        //tempTailVec(1) := U(f"$tempTailWidth'd$tempDepth")
//        tempTailVec(1) := U(f"$tempTailWidth'd$depth")
//        assert(tempTailVec(0) < tempTailVec(1))
//        tempHeadVec(0) := loc.rHead.resized
//        //tempHeadVec(1) := U(f"$tempHeadWidth'd$tempDepth")
//        tempHeadVec(1) := U(f"$tempHeadWidth'd$depth")
//        assert(tempHeadVec(0) < tempHeadVec(1))
//        //assert(loc.rTail < tempDepth)
//        //assert(loc.rHead < tempDepth)
//
//        //m.d.sync += [
//        // cover(misc.empty)
//        // cover(misc.full)
//        // cover(~(misc.empty & misc.full))
//        //)
//
//        //m.d.sync += [
//        //assert(loc.rHead === past(loc.nextHead)),
//        //assert(loc.rTail === past(loc.nextTail)),
//        //assert(misc.empty === past(loc.nextEmpty)),
//        //assert(misc.full === past(loc.nextFull)),
//        assert(~(misc.empty & misc.full))
//
//        assert(rdData === loc.arr(loc.rTail.resized))
//        //assert(past(rdData) === locFormal.lastTailVal)
//        //]
//
//        when (past(rdEn)) {
//          when (past(misc.empty)) {
//            //m.d.sync +=
//            assert(stable(loc.rTail))
//            //assert(stable(loc.nextTail))
//          } otherwise { // when (~past(misc.empty))
//            //m.d.sync += 
//            //val tempTailPlus1Width = loc.rTail.getWidth + 1
//            //val tempTailPlus1 = Vec(UInt(tempTailPlus1Width bits), 4)
//            //tempTailPlus1(0) := past(loc.rTail).resized
//            //tempTailPlus1(1) := U(f"$tempTailPlus1Width'd1")
//            //tempTailPlus1(2) := tempTailPlus1(0) + tempTailPlus1(1)
//            //tempTailPlus1(3) := U(f"$tempTailPlus1Width'd$tempDepth")
//            //when (tempTailPlus1(2) >= tempTailPlus1(3)) {
//            //  assert(loc.rTail === 0)
//            //} otherwise {
//            //  assert(loc.rTail === tempTailPlus1(2))
//            //}
//            //when ((past(loc.rTail) + 1) === tempDepth) {
//            //}
//            //assert(loc.rTail
//            //  === ((past(loc.rTail) + 1) % tempDepth))
//            assert(loc.rTail
//              === ((past(loc.rTail) + 1) % depth))
//          }
//        } otherwise { // when (~past(rdEn))
//          //m.d.sync += [
//          //assert(stable(misc.empty))
//          assert(stable(loc.rTail))
//          //]
//        }
//
//        when (past(
//          wrEn
//          //nextWrEn
//        )) {
//          when (past(misc.full)) {
//            //m.d.sync +=
//            assert(stable(loc.rHead))
//          } otherwise { // when (~past(misc.full))
//            //m.d.sync +=
//            //assert(loc.rHead === ((past(loc.rHead) + 1) % tempDepth))
//            assert(loc.rHead === ((past(loc.rHead) + 1) % depth))
//            //val tempHeadPlus1Width = loc.rHead.getWidth + 1
//            //val tempHeadPlus1 = Vec(UInt(tempHeadPlus1Width bits), 4)
//            //tempHeadPlus1(0) := past(loc.rHead).resized
//            //tempHeadPlus1(1) := U(f"$tempHeadPlus1Width'd1")
//            //tempHeadPlus1(2) := tempHeadPlus1(0) + tempHeadPlus1(1)
//            //tempHeadPlus1(3) := U(f"$tempHeadPlus1Width'd$tempDepth")
//            //when (tempHeadPlus1(2) >= tempHeadPlus1(3)) {
//            //  assert(loc.rHead === 0)
//            //} otherwise {
//            //  assert(loc.rHead === tempHeadPlus1(2))
//            //}
//          }
//        } otherwise { // when (~past(wrEn))
//          //m.d.sync += [
//          //assert(stable(misc.full))
//          assert(stable(loc.rHead))
//          //]
//        }
//
//
//        switch (Cat(misc.full, misc.empty)) {
//          // neither full nor empty
//          is (B"00") {
//            //m.d.sync += [
//            assert(loc.rHead =/= loc.rTail)
//            assert(locFormal.testHead =/= loc.rTail)
//            //]
//          }
//          // empty
//          is (B"01") {
//            //m.d.sync += [
//            assert(loc.rHead === loc.rTail)
//            //]
//          }
//          // full
//          is (B"10") {
//            //m.d.sync += [
//            assert(locFormal.testHead === loc.rTail)
//            //]
//          }
//        }
//
//        // empty
//        when (loc.rHead === loc.rTail) {
//          //m.d.sync += 
//          //assert(misc.empty & (~misc.full))
//          assert(misc.empty)
//          assert(~misc.full)
//        } elsewhen (locFormal.testHead === loc.rTail) {
//          //m.d.sync += 
//          //assert((~misc.empty) & misc.full)
//          assert(~misc.empty)
//          assert(misc.full)
//        } otherwise {
//          //m.d.sync += 
//          //assert((~misc.empty) & (~misc.full))
//          assert(~misc.empty)
//          assert(~misc.full)
//        }
//      }
//    //--------
//    }
//  }
//  //--------
//
//  //--------
//}
