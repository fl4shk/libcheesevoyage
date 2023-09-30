package libcheesevoyage.gfx
import libcheesevoyage._

import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2

import spinal.core._
//import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import scala.collection.mutable.ArrayBuffer
import scala.math._

object LcvVgaGradientSim extends App {

  //def clkRate = 125.0 MHz
  def clkRate = 50.0 MHz
  //def clkRate = 100.7 MHz
  def pixelClk = 25.0 MHz
  //def ctrlFifoDepth = 20
  def ctrlFifoDepth = 16
  //def ctrlFifoDepth = 100
  //def fbSize2d = ElabVec2[Int](640, 480)
  //def fbSize2d = ElabVec2[Int](1, 1)
  //def fbSize2d = ElabVec2[Int](20, 20)
  //def rgbConfig = RgbConfig(rWidth=6, gWidth=6, bWidth=6)
  def rgbConfig = RgbConfig(rWidth=4, gWidth=4, bWidth=4)
  def physRgbConfig = LcvVideoDithererIo.outRgbConfig(rgbConfig=rgbConfig)
  //def vgaTimingInfo = LcvVgaTimingInfoMap.map("640x480@60")
  def vgaTimingInfo=LcvVgaTimingInfo(
    pixelClk=pixelClk,
    //pixelClk=25.175 MHz,
    htiming=LcvVgaTimingHv(
      //visib=640,
      //front=16,
      //sync=96,
      //back=48
      //visib=8,
      //visib=1 << 6,
      ////visib=1 << 4,
      //front=1,
      //sync=1,
      //back=8,
      visib=1 << 3,
      front=5,
      sync=5,
      back=5,
    ),
    vtiming=LcvVgaTimingHv(
      //visib=480,
      //front=10,
      //sync=2,
      //back=33
      //visib=8,
      //visib=1 << 6,
      ////visib=1 << 4,
      //front=1,
      //sync=1,
      //back=8,
      visib=1 << 3,
      front=5,
      sync=5,
      back=5,
    ),
  )
  val fbSize2d = vgaTimingInfo.fbSize2d
  case class Dut() extends Component {
    val io = new Bundle {
      //val phys = out(LcvVgaPhys(rgbConfig=physRgbConfig))
      //val hscS = out(LcvVgaState())
      //val hscC = out(UInt(vgaTimingInfo.htiming.cntWidth() bits))
      //val hscNextS = out(LcvVgaState())

      //val vscS = out(LcvVgaState())
      //val vscC = out(UInt(vgaTimingInfo.vtiming.cntWidth() bits))
      //val vscNextS = out(LcvVgaState())
      //val ctrlIo = slave(LcvVgaCtrlIo(
      //  rgbConfig=physRgbConfig,
      //  vgaTimingInfo=vgaTimingInfo,
      //))
      val phys = out(LcvVgaPhys(rgbConfig=physRgbConfig))
      val misc = out(LcvVgaCtrlMiscIo(
        clkRate=clkRate,
        vgaTimingInfo=vgaTimingInfo,
        fifoDepth=ctrlFifoDepth,
      ))
    }
    val vgaCtrl = LcvVgaCtrl(
      clkRate=clkRate,
      rgbConfig=physRgbConfig,
      vgaTimingInfo=vgaTimingInfo,
      //vgaTimingInfo=LcvVgaTimingInfoMap.map("640x480@60"),
      //vgaTimingInfo=LcvVgaTimingInfo(
      //  pixelClk=25.0,
      //  //pixelClk=25.175,
      //  htiming=LcvVgaTimingHv(
      //    visib=640,
      //    front=16,
      //    sync=96,
      //    back=48
      //  ),
      //  vtiming=LcvVgaTimingHv(
      //    visib=480,
      //    front=10,
      //    sync=2,
      //    back=33
      //  ),
      //),
      fifoDepth=ctrlFifoDepth,
    )
    val vidDith = LcvVideoDitherer(
      //fbSize2d=fbSize2d,
      rgbConfig=rgbConfig,
      //vgaTimingInfo=vgaTimingInfo,
      //fbSize2d=vgaTimingInfo.fbSize2d,
      fbSize2d=fbSize2d,
    )
    val vgaGrad = LcvVgaGradient(
      clkRate=clkRate,
      rgbConfig=rgbConfig,
      vgaTimingInfo=vgaTimingInfo,
      ctrlFifoDepth=ctrlFifoDepth,
    )
    //val vgaGrad = LcvVgaGradientNoDith(
    //  rgbConfig=physRgbConfig,
    //  vgaTimingInfo=vgaTimingInfo,
    //)
    ////val vgaCtrlMisc = LcvVgaCtrlMiscIo()
    val ctrlIo = vgaCtrl.io
    //io.ctrlIo <> ctrlIo
    val dithIo = vidDith.io
    val gradIo = vgaGrad.io
    //ctrlIo.en := True
    //ctrlIo.push.valid := True
    //val inpCol = ctrlIo.push.payload
    ////val inpCol = ctrlIo.inpCol
    //inpCol.r := (default -> True)
    //inpCol.g := (default -> False)
    //inpCol.b := (default -> False)
    
    //cover(ctrlIo.misc.visib)

    //ctrlIo.en := gradIo.vgaCtrlEn
    //ctrlIo.push << gradIo.vgaCtrlPush
    //gradIo.vgaCtrlMisc := ctrlIo.misc
    //io.phys := ctrlIo.phys

    //dithIo.push << gradIo.vidDithIo.push
    //dithIo.pop >> gradIo.vidDithIo.pop
    //ctrlIo <> gradIo.vgaCtrlIo
    //dithIo <> gradIo.vidDithIo
    ctrlIo.en := gradIo.vgaCtrlIo.en
    ctrlIo.push << gradIo.vgaCtrlIo.push
    gradIo.vgaCtrlIo.misc := ctrlIo.misc

    dithIo.push << gradIo.vidDithIo.push
    gradIo.vidDithIo.outp := dithIo.outp

    //ctrlIo.en := True
    //val inpCol = ctrlIo.push.payload
    //val inpCol = ctrlIo.inpCol
    //ctrlIo.push.valid := True
    //val tempColR = Reg(UInt(physRgbConfig.rWidth bits)) init(0x0)
    //ctrlIo.push.valid := dithIo.pop.valid
    //ctrlIo.push.payload := dithIo.pop.payload.col
    //val inpCol = dithIo.inpCol
    //val tempColR = Reg(UInt(rgbConfig.rWidth bits)) init(0x0)
    //val rNextVisib = Reg(Bool()) init(False)

    //dithIo.push.valid := ctrlIo.misc.drawPos.x := 
    //dithIo.push.valid := rNextVisib & rPixelEn

    //when (ctrlIo.misc.nextVisib) {
    //  when (!rNextVisib) {
    //    rNextVisib := True
    //   //tempColR := 0x0 
    //  }
    //} otherwise {
    //  rNextVisib := False
    //  //tempColR := tempColR + 1
    //}
    //when (!rNextVisib) {
    //  tempColR := 0x0
    //} otherwise {
    //  tempColR := tempColR + 1
    //}

    //val rVisib = Reg(Bool()) init(False)
    //when (ctrlIo.misc.visib) {
    //  when (!rVisib) {
    //    rVisib := True
    //    tempColR := 0x0
    //  }
    //} otherwise {
    //  rVisib := False
    //  tempColR := tempColR + 1
    //}

    //when (ctrlIo.misc.nextVisib) {
    //  tempColR := 0x0
    //} 
    ////elsewhen (ctrlIo.push.fire) 
    //.otherwise {
    //  tempColR := tempColR + 1
    //}

    ////inpCol.r := (default -> True)
    //inpCol.r := tempColR
    //inpCol.g := 0x0
    //inpCol.b := 0x0
    io.phys := ctrlIo.phys
    io.misc := ctrlIo.misc
    //io.hscS := ctrlIo.hscS
    //io.hscC := ctrlIo.hscC
    //io.hscNextS := ctrlIo.hscNextS

    //io.vscS := ctrlIo.vscS
    //io.vscC := ctrlIo.vscC
    //io.vscNextS := ctrlIo.vscNextS
    //val rFoundVisib = Reg(Bool()) init(False)
    //val nextFoundVisib = Bool()
    //rFoundVisib := nextFoundVisib
    //nextFoundVisib := io.misc.visib | rFoundVisib

    //val cntWidth = 32
    //val cnt = Reg(UInt(cntWidth bits)) init(0x0)
    //when (nextFoundVisib) {
    //  cnt := cnt + 1
    //}
  }
  val simSpinalConfig = SpinalConfig(
    //defaultClockDomainFrequency=FixedFrequency(100 MHz)
    defaultClockDomainFrequency=FixedFrequency(clkRate)
  )
  SimConfig
    .withConfig(config=simSpinalConfig)
    .withVcdWave
    .compile(Dut())
    .doSim { dut =>
      dut.clockDomain.forkStimulus(period=10)
      //SimTimeout(1000)
      //for (idx <- 0 to 4000) {
      //  //sleep(1)
      //  dut.clockDomain.waitRisingEdge()
      //}
      for (idx <- 0 to 8000 - 1) {
        dut.clockDomain.waitRisingEdge()
        //when (dut.io.misc.visib) {
        //  foundVisib := True
        //}
        //when (foundVisib) {
        //  
        //}
      }
      simSuccess()
    }
}
