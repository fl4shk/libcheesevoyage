package libcheesevoyage.gfx
import libcheesevoyage.general.AsyncReadFifo
import libcheesevoyage.general.FifoIo
import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2
import libcheesevoyage.general.PipeSkidBuf
import libcheesevoyage.general.PipeSkidBufIo

//import scala.math._
import spinal.core._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer
import scala.math._

object LcvVgaState extends SpinalEnum(defaultEncoding=binarySequential) {
  val
    front,
    sync,
    back,
    visib
    = newElement();
}

case class LcvVgaStateCnt(
  vgaTimingHv: LcvVgaTimingHv
) //extends Bundle
{
  //--------
  val s = Reg(LcvVgaState()) init(LcvVgaState.front)
  val c = Reg(UInt(vgaTimingHv.cntWidth() bits)) init(0x0)
  val cP1Width = vgaTimingHv.cntWidth() + 1
  val cP1 = UInt(cP1Width bits)
  cP1 := c.resized + U(f"$cP1Width'd1")
  val nextS = LcvVgaState()
  //--------
  def noChangeUpdateNextS(): Unit = {
    nextS := s
  }
  def updateStateCnt(
    vgaTimingHv: LcvVgaTimingHv
  ): Unit = {
    def mkCase(
      stateSize: Int,
      nextState: LcvVgaState.C,
    ): Unit = {
      //val counterP1 = c.resized + U("1").resized
      val tempSSWidth = log2Up(stateSize + 1)
      //println(f"in mkCase(): $tempSSWidth, $stateSize")
      val tempStateSize = U(f"$tempSSWidth'd$stateSize")

      //val tempWidth1 = max(c.getWidth + 1, tempSSWidth)
      val tempWidth1 = max(
        log2Up((1 << c.getWidth) + 1),
        tempSSWidth
      )
      val counterP1 = UInt(tempWidth1 bits)
      counterP1 := c.resized + U(f"$tempWidth1'd1")
      //val tempSSWidth1 = max(cWidthP1, tempSSWidth)
      val tempStateSize1 = UInt(tempWidth1 bits)
      if (tempSSWidth == tempWidth1) {
        tempStateSize1 := tempStateSize
      } else {
        //tempStateSize1 := (
        //  tempSSWidth - 1 downto 0 -> tempStateSize,
        //  default -> False,
        //)
        tempStateSize1(tempStateSize1.high downto tempSSWidth) := 0x0 
        tempStateSize1(tempSSWidth - 1 downto 0) := tempStateSize
      }

      //val cWidthP2 = c.getWidth + 2
      //val counterP2 = UInt(cWidthP2 bits)
      //counterP2 := c.resized + U(f"$cWidthP2'd1")
      ////val tempSSWidth2 = max(cWidthP2, tempSSWidth)
      //val tempStateSize2 = UInt(cWidthP2 bits)
      //tempStateSize2 := stateSize.resized
      //val counterP2 = c.resized + U("1").resized
      // based upon my calculations, `tempWidth2` will be the same as
      // `tempWidth1` even in the case of `c.getWidth == 1`
      val tempWidth2 = max(
        //BigInt((1 << c.getWidth) + 2).bitLength,
        log2Up((1 << c.getWidth) + 2),
        tempSSWidth
      )
      val counterP2 = UInt(tempWidth2 bits)
      counterP2 := c.resized + U(f"$tempWidth2'd2")
      //val tempSSWidth2 = max(cWidthP2, tempSSWidth)
      val tempStateSize2 = UInt(tempWidth2 bits)
      //tempStateSize2 := stateSize
      if (tempSSWidth == tempWidth2) {
        tempStateSize2 := tempStateSize
      } else {
        //tempStateSize2 := (
        //  tempSSWidth - 1 downto 0 -> tempStateSize,
        //  default -> False,
        //)
        tempStateSize2(tempStateSize2.high downto tempSSWidth) := 0x0 
        tempStateSize2(tempSSWidth - 1 downto 0) := tempStateSize
      }

      //val tempStateSize = U(stateSize)
      //val tempStateSize = U(f"$tempWidth'd$stateSize")
      when (counterP1 >= tempStateSize1) {
        s := nextState
        c := 0x0
        //m.d.comb += nextS.eq(nextState)
      } otherwise {
        c := counterP1.resized
        //self.noChangeUpdateNextS(m, stateCnt)
      }

      //when ((c + 0x2).resized >= stateSize) {
      when (counterP2 >= tempStateSize2) {
        nextS := nextState
      } otherwise {
        nextS := s
      }
    }

    //State = VgaTiming.State
    switch (s) {
      is (LcvVgaState.front) {
        val stateSize = vgaTimingHv.front
        mkCase(stateSize=stateSize, nextState=LcvVgaState.sync)
        //println(f"front, sync: $stateSize")
      }
      is (LcvVgaState.sync) {
        val stateSize = vgaTimingHv.sync
        mkCase(stateSize=stateSize, nextState=LcvVgaState.back)
        //println(f"sync, back: $stateSize")
      }
      is (LcvVgaState.back) {
        val stateSize = vgaTimingHv.back
        mkCase(stateSize=stateSize, nextState=LcvVgaState.visib)
        //println(f"back, visib: $stateSize")
      }
      is (LcvVgaState.visib) {
        val stateSize = vgaTimingHv.visib
        mkCase(stateSize=stateSize, nextState=LcvVgaState.front)
        //println(f"visib, front: $stateSize")
      }
    }
    
  }
}

//case class LcvVgaCtrlBufInp(rgbConfig: RgbConfig) extends Bundle {
//  //val prep = Bool()
//  //val col = Rgb(rgbConfig)
//  val valid = Bool()
//  val payload = Rgb(rgbConfig)
//}
//case class LcvVgaCtrlBufOutp() extends Bundle {
//  //val canPrep = Bool()
//  val ready = Bool()
//}
case class LcvVgaPhys(rgbConfig: RgbConfig) extends Bundle {
  val col = Rgb(rgbConfig)
  val hsync = Bool()
  val vsync = Bool()
}
//case class LcvVgaCtrlInp(rgbConfig: RgbConfig) extends Bundle {
//  // Global VGA driving enable (white screen when off)
//  val en = Bool()
//
//  //// Pixel buffer
//  //val buf = LcvVgaCtrlBufInp(rgbConfig=rgbConfig)
//}
object LcvVgaCtrlMiscIo {
  //--------
  def coordElemKindT(): UInt = UInt(16 bits)
  //--------
}
case class LcvVgaCtrlMiscIo() extends Bundle {
  // VGA physical pins
  //self.outpCol = ColorT()
  //val col = Rgb(rgbConfig)
  //val hsync = Bool()
  //val vsync = Bool()
  //val vga = LcvVga(rgbConfig=rgbConfig)

  //val buf = port VgaDriverBufOutpLayt()//.shape
  //val buf = LcvVgaCtrlBufOutp()

  // Debug
  val dbgFifoEmpty = Bool()
  val dbgFifoFull = Bool()

  // Misc.
  val pixelEn = Bool()
  val nextVisib = Bool()
  val visib = Bool()
  val pastVisib = Bool()
  val drawPos = Vec2(LcvVgaCtrlMiscIo.coordElemKindT())
  val pastDrawPos = Vec2(LcvVgaCtrlMiscIo.coordElemKindT())
  val size = Vec2(LcvVgaCtrlMiscIo.coordElemKindT())
  //--------
  //--------
}
case class LcvVgaCtrlIo(
  rgbConfig: RgbConfig
) extends Bundle with IMasterSlave {
  //--------
  val en = in Bool()
  //val inp = in(LcvVgaCtrlInp(rgbConfig=rgbConfig))
  //val outp = out(LcvVgaCtrlOutp(rgbConfig=rgbConfig))
  val push = slave Stream(Rgb(rgbConfig))
  val phys = out(LcvVgaPhys(rgbConfig=rgbConfig))
  val misc = out(LcvVgaCtrlMiscIo())
  //--------
  def asMaster(): Unit = {
    out(en)
    master(push)
    in(phys, misc)
  }
  //def asSlave(): Unit = {
  //  in(en)
  //  slave(push)
  //  out(phys, misc)
  //}
  //--------
}
//--------
case class LcvVgaCtrl(
  clkRate: Double,
  timingInfo: LcvVgaTimingInfo,
  fifoDepth: Int,
  rgbConfig: RgbConfig,
) extends Component {
  //--------
  val io = LcvVgaCtrlIo(rgbConfig=rgbConfig)
  val push = io.push
  val phys = io.phys
  val misc = io.misc
  //--------
  // Clocks per pixel
  def cpp(): Int = {
    return scala.math.floor(
      clkRate / timingInfo.pixelClk
    ).toInt
  }
  def htiming(): LcvVgaTimingHv = {
    return timingInfo.htiming
  }
  def vtiming(): LcvVgaTimingHv = {
    return timingInfo.vtiming
  }
  //def numBufScanlines():
  //  return numBufScanlines
  //def fifoDepth():
  //  return (fbSize().x * numBufScanlines())
  //def fifoDepth():
  //  return fifoDepth
  def fbSize(): ElabVec2[Int] = {
    //ret = blank()
    //ret.x, ret.y = htiming().visib(), vtiming().visib()
    //return ret
    return ElabVec2(htiming().visib, vtiming().visib)
  }
  def clkCntWidth(): Int = {
    return log2Up(cpp())
  }
  //--------
  //val inp = io.inp
  //val outp = io.outp
  //--------
  // Pixel FIFO
  val fifo = AsyncReadFifo(
    dataType=Rgb(rgbConfig),
    depth=fifoDepth,
  )
  //val fifoInp = fifo.io.inp
  //val fifoOutp = fifo.io.outp
  val fifoPush = fifo.io.push
  val fifoPop = fifo.io.pop
  val fifoEmpty = fifo.io.misc.empty
  val fifoFull = fifo.io.misc.full
  //--------
  val rPhys = Reg(LcvVgaPhys(rgbConfig=rgbConfig))
  rPhys.init(rPhys.getZero)
  phys := rPhys
  //--------
  //val loc = new Area {
  val col = Rgb(rgbConfig)
  // Implement the clock enable
  //val clkCntWidth = self.clkCntWidth()
  val clkCnt = Reg(UInt(clkCntWidth() bits)) init(0x0)

  // Force this addition to be of width `CLK_CNT_WIDTH + 1` to
  // prevent wrap-around
  val clkCntP1 = UInt((clkCntWidth() + 1) bits)
  //clkCntP1 := Cat(False, clkCnt).asUInt + 0x1
  clkCntP1 := (clkCnt.resized + 0x1).resized

  // Implement wrap-around for the clock counter
  when (clkCntP1.resized < cpp()) {
    clkCnt := clkCntP1.resized
  } otherwise {
    clkCnt := 0x0
  }

  // outp.pixelEn = (clkCnt === 0x0)
  misc.pixelEn := clkCnt.resized === 0x0
  val pixelEnNextCycle = clkCntP1.resized === cpp()
  //--------
  //type Tstate = LcvVgaState.type;
  //loc.hsc = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.HTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  //loc.vsc = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.VTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  val hsc = LcvVgaStateCnt(vgaTimingHv=htiming())
  val vsc = LcvVgaStateCnt(vgaTimingHv=vtiming())
  //--------
  // Implement HSYNC and VSYNC logic
  when (misc.pixelEn) {
    //self.HTIMING().updateStateCnt(m, loc.hsc)
    hsc.updateStateCnt(vgaTimingHv=htiming())

    switch (hsc.s) {
      is (LcvVgaState.front) {
        //m.d.sync += outp.hsync.eq(0b1)
        //self.VTIMING().noChangeUpdateNextS(m, loc.vsc)
        rPhys.hsync := True
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.sync) {
        //m.d.sync += outp.hsync.eq(0b0)
        //self.VTIMING().noChangeUpdateNextS(m, loc.vsc)
        rPhys.hsync := False
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.back) {
        //m.d.sync += outp.hsync.eq(0b1)
        //self.VTIMING().noChangeUpdateNextS(m, loc.vsc)
        rPhys.hsync := True
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.visib) {
        //m.d.sync += outp.hsync.eq(0b1),
        //when ((hsc.c + 0x1) >= self.FB_SIZE().x) {
        //  self.VTIMING().updateStateCnt(m, loc.vsc)
        //} otherwise {
        //  self.VTIMING().noChangeUpdateNextS(m, loc.vsc)
        //}
        rPhys.hsync := True
        when (hsc.c + 0x1 >= fbSize().x) {
          //self.VTIMING().updateStateCnt(m, loc.vsc)
          vsc.updateStateCnt(vgaTimingHv=vtiming())
        } otherwise {
          //self.VTIMING().noChangeUpdateNextS(m, loc.vsc)
          vsc.noChangeUpdateNextS()
        }
      }
    }

    switch (vsc.s) {
      is (LcvVgaState.front) {
        //m.d.sync += outp.vsync.eq(0b1)
        rPhys.vsync := True
      }
      is (LcvVgaState.sync) {
        //m.d.sync += outp.vsync.eq(0b0)
        rPhys.vsync := False
      }
      is (LcvVgaState.back) {
        //m.d.sync += outp.vsync.eq(0b1)
        rPhys.vsync := True
      }
      is (LcvVgaState.visib) {
        //m.d.sync += outp.vsync.eq(0b1)
        rPhys.vsync := True
      }
    }
  } otherwise { // when (~outp.pixelEn)
    //htiming().noChangeUpdateNextS(m, loc.hsc)
    //vtiming().noChangeUpdateNextS(m, loc.vsc)
    hsc.noChangeUpdateNextS()
    vsc.noChangeUpdateNextS()
  }
  //--------
  //--------
  // Implement drawing the picture

  when (misc.pixelEn) {
    // Visible area
    when (misc.visib) {
      when (~io.en) {
      //when (~push.fire) 
        //m.d.sync += [
        // white
        //rPhys.col.r := -1
        //rPhys.col.g := -1
        //rPhys.col.b := -1
        rPhys.col.r := (default -> True)
        rPhys.col.g := (default -> True)
        rPhys.col.b := (default -> True)
        //]
      } otherwise { // when (io.en)
        //m.d.sync += [
        rPhys.col := col
        //]
      }
    // Black border
    } otherwise { // when (~outp.visib)
      //m.d.sync += [
      rPhys.col.r := 0x0
      rPhys.col.g := 0x0
      rPhys.col.b := 0x0
      //]
    }
  }
  //--------
  // Implement LcvVgaCtrl bus to Fifo push transaction
  //m.d.comb += [
    //outp.buf.canPrep.eq(~fifoOutp.full),
    //fifoInp.wrEn.eq(inp.buf.prep),
    //fifoInp.wrData.eq(inp.buf.col.asValue()),
    fifoPush << push
    //fifoPush.connectFrom(push)
  //]
  //--------
  // Implement grabbing pixels from the FIFO.

  when (misc.pixelEn & misc.visib & ~fifoEmpty) {
    //m.d.comb += fifoInp.rdEn.eq(0b1)
    fifoPop.ready := True
  } otherwise {
    //m.d.comb += fifoInp.rdEn.eq(0b0)
    fifoPop.ready := False
  }
  //when (PIXEL_EN_NEXT_CYCLE & outp.nextVisib
  // & (~fifoOutp.empty)):
  // m.d.sync += fifoInp.rdEn.eq(0b1)
  //otherwise
  // m.d.sync += fifoInp.rdEn.eq(0b0)

  //m.d.comb += [
  col := fifoPop.payload
  misc.dbgFifoEmpty := fifoEmpty
  misc.dbgFifoFull := fifoFull
  //]
  //--------
  //m.d.comb += [
  //outp.visib := ((hsc.s == Tstate.VISIB)
  // & (vsc.s == Tstate.VISIB)),
  misc.drawPos.x := hsc.c.resized
  misc.drawPos.y := vsc.c.resized
  misc.size.x := fbSize().x
  misc.size.y := fbSize().y
  //]
  //m.d.sync += [
  val rNextVisib = Reg(Bool()) init(False)
  rNextVisib := (
    (hsc.nextS === LcvVgaState.visib)
    & (vsc.nextS === LcvVgaState.visib)
  )
  misc.nextVisib := rNextVisib

  val rVisib = Reg(Bool()) init(False)
  rVisib := misc.nextVisib
  misc.visib := rVisib

  val rPastVisib = Reg(Bool()) init(False)
  rPastVisib := misc.visib
  misc.pastVisib := rPastVisib

  val rPastDrawPos = Reg(Vec2(LcvVgaCtrlMiscIo.coordElemKindT()))
  rPastDrawPos.init(rPastDrawPos.getZero)
  rPastDrawPos := misc.drawPos
  misc.pastDrawPos := rPastDrawPos
  //]
  //--------

  //}
  //--------
}

case class LcvVgaCtrlNoFifoIo(
  rgbConfig: RgbConfig
) extends Bundle with IMasterSlave {
  //--------
  val en = in Bool()
  //val inp = in(LcvVgaCtrlInp(rgbConfig=rgbConfig))
  //val outp = out(LcvVgaCtrlOutp(rgbConfig=rgbConfig))
  //val push = slave Stream(Rgb(rgbConfig))
  val inpCol = in(Rgb(rgbConfig))
  val phys = out(LcvVgaPhys(rgbConfig=rgbConfig))
  val misc = out(LcvVgaCtrlMiscIo())
  //--------
  def asMaster(): Unit = {
    out(en, inpCol)
    //master(push)
    in(phys, misc)
  }
  //def asSlave(): Unit = {
  //  in(en)
  //  slave(push)
  //  out(phys, misc)
  //}
  //--------
}
//--------
case class LcvVgaCtrlNoFifo(
  clkRate: Double,
  timingInfo: LcvVgaTimingInfo,
  //fifoDepth: Int,
  rgbConfig: RgbConfig,
) extends Component {
  //--------
  val io = LcvVgaCtrlNoFifoIo(rgbConfig=rgbConfig)
  //val push = io.push
  val inpCol = io.inpCol
  val phys = io.phys
  val misc = io.misc
  //--------
  // Clocks per pixel
  def cpp(): Int = {
    return scala.math.floor(
      clkRate / timingInfo.pixelClk
    ).toInt
  }
  def htiming(): LcvVgaTimingHv = {
    return timingInfo.htiming
  }
  def vtiming(): LcvVgaTimingHv = {
    return timingInfo.vtiming
  }
  //def numBufScanlines():
  //  return numBufScanlines
  //def fifoDepth():
  //  return (fbSize().x * numBufScanlines())
  //def fifoDepth():
  //  return fifoDepth
  def fbSize(): ElabVec2[Int] = {
    //ret = blank()
    //ret.x, ret.y = htiming().visib(), vtiming().visib()
    //return ret
    return ElabVec2(htiming().visib, vtiming().visib)
  }
  def clkCntWidth(): Int = {
    return log2Up(cpp())
  }
  //--------
  val rPhys = Reg(LcvVgaPhys(rgbConfig=rgbConfig))
  rPhys.init(rPhys.getZero)
  phys := rPhys

  // Implement the clock enable
  val clkCnt = Reg(UInt(clkCntWidth() bits)) init(0x0)
  // Force this addition to be of width `CLK_CNT_WIDTH + 1` to
  // prevent wrap-around
  val clkCntP1Width = clkCntWidth() + 1
  val clkCntP1 = UInt(clkCntP1Width bits)
  clkCntP1 := clkCnt.resized + U(f"$clkCntP1Width'd1")

  // Implement wrap-around for the clock counter
  when (clkCntP1 < cpp()) {
    //m.d.sync += 
    clkCnt := clkCntP1(clkCnt.bitsRange)
  } otherwise {
    //m.d.sync +=
    clkCnt := 0x0
  }
  // Since this is an alias, use ALL_CAPS for its name.
  // outp.pixelEn = (clkCnt == 0x0)
  //m.d.comb += 
  misc.pixelEn := clkCnt === 0x0
  val pixelEnNextCycle = Bool()
  pixelEnNextCycle := clkCntP1.resized === cpp()
  //--------
  // Implement the State/Counter stuff
  //loc.Tstate = VgaTiming.State
  //loc.hsc = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.HTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  //loc.vsc = {
  //  "s": Signal(width_from_len(loc.Tstate)),
  //  "c": Signal(self.VTIMING().COUNTER_WIDTH()),
  //  "next_s": Signal(width_from_len(loc.Tstate)),
  //}
  val hsc = LcvVgaStateCnt(htiming)
  val vsc = LcvVgaStateCnt(vtiming)
  //--------
  //// Implement HSYNC and VSYNC logic
  when (misc.pixelEn) {
    //htiming().updateStateCnt(m, hsc)
    hsc.updateStateCnt(htiming)

    switch (hsc.s) {
      is (LcvVgaState.front) {
        //m.d.sync += outp.hsync := (0b1)
        rPhys.hsync := True
        //vtiming().noChangeUpdateNextS(m, vsc)
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.sync) {
        //m.d.sync += outp.hsync := (0b0)
        rPhys.hsync := False
        //vtiming().noChangeUpdateNextS(m, vsc)
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.back) {
        //m.d.sync += outp.hsync := (0b1)
        rPhys.hsync := True
        //vtiming().noChangeUpdateNextS(m, vsc)
        vsc.noChangeUpdateNextS()
      }
      is (LcvVgaState.visib) {
        //m.d.sync += outp.hsync := (0b1)
        rPhys.hsync := True
        //when ((hsc["c"] + 0x1) >= FB_SIZE().x) 
        //when ((hsc.c + 0x1) >= fbSize().x)
        when (hsc.cP1 >= fbSize().x) {
          //vtiming().updateStateCnt(m, vsc)
          vsc.updateStateCnt(vtiming)
        } otherwise {
          //vtiming().noChangeUpdateNextS(m, vsc)
          vsc.noChangeUpdateNextS()
        }
      }
    }

    switch (vsc.s) {
      is (LcvVgaState.front) {
        //m.d.sync += outp.vsync := (0b1)
        rPhys.vsync := True
      }
      is (LcvVgaState.sync) {
        //m.d.sync += outp.vsync := (0b0)
        rPhys.vsync := False
      }
      is (LcvVgaState.back) {
        //m.d.sync += outp.vsync := (0b1)
        rPhys.vsync := True
      }
      is (LcvVgaState.visib) {
        //m.d.sync += outp.vsync := (0b1)
        rPhys.vsync := True
      }
    }
  } otherwise { // when (~misc.pixelEn)
    //htiming().noChangeUpdateNextS(m, hsc)
    //vtiming().noChangeUpdateNextS(m, vsc)
    hsc.noChangeUpdateNextS()
    vsc.noChangeUpdateNextS()
  }
  //--------
  // Implement drawing the picture

  when (misc.pixelEn) {
    // Visible area
    when (misc.visib) {
      when (~io.en) {
        //m.d.sync += [
          //rPhys.col.r := (0xf),
          //rPhys.col.g := (0xf),
          //rPhys.col.b := (0xf),
        //]
        rPhys.col.r := (default -> True)
        rPhys.col.g := (default -> True)
        rPhys.col.b := (default -> True)
      } otherwise { // when (io.en)
        //m.d.sync += [
          rPhys.col := inpCol
        //]
      }
    // Black border
    } otherwise { // when (~misc.visib)
      //m.d.sync += [
        rPhys.col.r := 0x0
        rPhys.col.g := 0x0
        rPhys.col.b := 0x0
      //]
    }
  }
  //--------
  //val rMisc = Reg(LcvVgaCtrlMiscIo())
  //rMisc.init(rMisc.getZero)
  //misc := rMisc
    //m.d.comb += [
      //misc.visib := ((hsc.s == Tstate.VISIB)
      // & (vsc.s == Tstate.VISIB)),
    misc.drawPos.x := hsc.c.resized
    misc.drawPos.y := vsc.c.resized
    misc.size.x := fbSize().x
    misc.size.y := fbSize().y
    //]
    //m.d.sync += [
    val rNextVisib = Reg(Bool()) init(False)
    rNextVisib := ((hsc.nextS === LcvVgaState.visib)
      & (vsc.nextS === LcvVgaState.visib))
    misc.nextVisib := rNextVisib

    val rVisib = Reg(Bool()) init(False)
    rVisib := misc.nextVisib
    misc.visib := rVisib

    val rPastVisib = Reg(Bool()) init(False)
    rPastVisib := misc.visib
    misc.pastVisib := rPastVisib

    val rPastDrawPos = Reg(Vec2(LcvVgaCtrlMiscIo.coordElemKindT()))
    rPastDrawPos.init(rPastDrawPos.getZero)
    rPastDrawPos := misc.drawPos
    misc.pastDrawPos := rPastDrawPos
    //]

  //--------
}
