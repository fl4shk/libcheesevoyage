package libcheesevoyage.gfx
import libcheesevoyage._

import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2

import spinal.core._
//import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.graphic.vga._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import scala.collection.mutable.ArrayBuffer
import scala.math._

case class Gpu2dSimDut(
  clkRate: HertzNumber,
  rgbConfig: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  gpu2dParams: Gpu2dParams,
  ctrlFifoDepth: Int,
) extends Component {
  val io = new Bundle {
    //val phys = out(LcvVgaPhys(rgbConfig=physRgbConfig))
    val phys = out(LcvVgaPhys(rgbConfig=rgbConfig))
    val misc = out(LcvVgaCtrlMiscIo(
      clkRate=clkRate,
      vgaTimingInfo=vgaTimingInfo,
      fifoDepth=ctrlFifoDepth,
    ))
    //val error = out Bool()
  }
  val vgaCtrl = LcvVgaCtrl(
    clkRate=clkRate,
    //rgbConfig=physRgbConfig,
    rgbConfig=rgbConfig,
    vgaTimingInfo=vgaTimingInfo,
    fifoDepth=ctrlFifoDepth,
  )
  //val vidDith = LcvVideoDitherer(
  //  //fbSize2d=fbSize2d,
  //  //rgbConfig=rgbConfig,
  //  rgbConfig=RgbConfig(
  //    rWidth=rgbConfig.rWidth + 2,
  //    gWidth=rgbConfig.gWidth + 2,
  //    bWidth=rgbConfig.bWidth + 2,
  //  ),
  //  //vgaTimingInfo=vgaTimingInfo,
  //  fbSize2d=vgaTimingInfo.fbSize2d,
  //  //fbSize2d=fbSize2d,
  //)
  //val vgaGrad = LcvVgaGradient(
  //  clkRate=clkRate,
  //  rgbConfig=RgbConfig(
  //    rWidth=rgbConfig.rWidth + 2,
  //    gWidth=rgbConfig.gWidth + 2,
  //    bWidth=rgbConfig.bWidth + 2,
  //  ),
  //  vgaTimingInfo=vgaTimingInfo,
  //  ctrlFifoDepth=ctrlFifoDepth,
  //)
  //--------
  //val ctrlIo = vgaCtrl.io
  //val dithIo = vidDith.io
  //val gradIo = vgaGrad.io
  //ctrlIo.en := gradIo.vgaCtrlIo.en
  //ctrlIo.push << gradIo.vgaCtrlIo.push
  //gradIo.vgaCtrlIo.misc := ctrlIo.misc

  //dithIo.push << gradIo.vidDithIo.push
  //gradIo.vidDithIo.pop << dithIo.pop
  //gradIo.vidDithIo.info := dithIo.info
  //--------
  //def myVgaTimingsWidth = 12
  //val myVgaTimings = VgaTimings(timingsWidth=myVgaTimingsWidth)
  //def clksPerPixel = (
  //  clkRate / vgaTimingInfo.pixelClk
  //).toInt
  //myVgaTimings.setAs(
  //  //hPixels=vgaTimingInfo.htiming.visib * clksPerPixel,
  //  //hSync=vgaTimingInfo.htiming.sync * clksPerPixel,
  //  //hFront=vgaTimingInfo.htiming.front * clksPerPixel,
  //  //hBack=vgaTimingInfo.htiming.back * clksPerPixel,
  //  //hPolarity=false,
  //  //vPixels=vgaTimingInfo.vtiming.visib * clksPerPixel,
  //  //vSync=vgaTimingInfo.vtiming.sync * clksPerPixel,
  //  //vFront=vgaTimingInfo.vtiming.front * clksPerPixel,
  //  //vBack=vgaTimingInfo.vtiming.back * clksPerPixel,
  //  //vPolarity=false,
  //  hPixels=vgaTimingInfo.htiming.visib,
  //  hSync=vgaTimingInfo.htiming.sync,
  //  hFront=vgaTimingInfo.htiming.front,
  //  hBack=vgaTimingInfo.htiming.back,
  //  hPolarity=false,
  //  vPixels=vgaTimingInfo.vtiming.visib,
  //  vSync=vgaTimingInfo.vtiming.sync,
  //  vFront=vgaTimingInfo.vtiming.front,
  //  vBack=vgaTimingInfo.vtiming.back,
  //  vPolarity=false,
  //)

  //val vgaCtrl = VgaCtrl(
  //  rgbConfig=rgbConfig,
  //  timingsWidth=myVgaTimingsWidth,
  //)
  //--------
  val gpu2d = Gpu2d(
    params=gpu2dParams,
    inSim=true,
  )
  val gpu2dTest = Gpu2dTest(
    params=gpu2dParams,
  )

  val ctrlIo = vgaCtrl.io
  //ctrlIo.timings := myVgaTimings
  //val dithIo = vidDith.io
  val gpuIo = gpu2d.io

  //gpuIo <> gpu2dTest.io.gpuIo
  //gpuIo.push <> gpu2dTest.io.pop
  //gpu2dTest.io.pop <> gpuIo.push
  gpuIo.push << gpu2dTest.io.pop

  //ctrlIo.en := True
  //ctrlIo.push.valid := dithIo.pop.valid
  //ctrlIo.push.payload := dithIo.pop.payload.col
  //dithIo.pop.ready := ctrlIo.push.ready

  //dithIo.push.valid := gpuIo.pop.valid
  //dithIo.push.payload := gpuIo.pop.payload.col 
  //gpuIo.pop.ready := dithIo.push.ready
  //ctrlIo.en := True

  ctrlIo.en := gpuIo.ctrlEn
  //ctrlIo.en := False

  ctrlIo.push.valid := gpuIo.pop.valid
  ctrlIo.push.payload := gpuIo.pop.payload.col
  gpuIo.pop.ready := ctrlIo.push.ready
  //ctrlIo.pixels.valid := gpuIo.pop.valid
  //ctrlIo.pixels.payload := gpuIo.pop.payload.col
  //gpuIo.pop.ready := ctrlIo.pixels.ready
  //--------
  //ctrlIo.push.valid := gpu2dTest.io.pop.valid
  //ctrlIo.push.payload := gpu2dTest.io.pop.payload.col
  //gpu2dTest.io.pop.ready := ctrlIo.push.ready
  //ctrlIo.push.valid := False
  //ctrlIo.push.payload := ctrlIo.push.payload.getZero
  //gpuIo.pop.ready := True
  //--------

  io.phys := ctrlIo.phys
  io.misc := ctrlIo.misc
  //io.misc := io.misc.getZero
  //io.misc.allowOverride
  //io.misc.visib := ctrlIo.vga.colorEn
  //io.phys.col := ctrlIo.vga.color
  //io.phys.hsync := ctrlIo.vga.hSync
  //io.phys.vsync := ctrlIo.vga.vSync
  //io.error := ctrlIo.error
}

object Gpu2dSim extends App {
  //def clkRate = 125.0 MHz
  def clkRate = 50.0 MHz
  //def clkRate = 100.0 MHz
  //def clkRate = 100.7 MHz
  def pixelClk = 25.0 MHz
  //def ctrlFifoDepth = 20
  def ctrlFifoDepth = 256
  //def ctrlFifoDepth = 100
  //def ctrlFifoDepth = 128
  //def fbSize2d = ElabVec2[Int](640, 480)
  //def fbSize2d = ElabVec2[Int](1, 1)
  //def fbSize2d = ElabVec2[Int](20, 20)
  //def rgbConfig = RgbConfig(rWidth=6, gWidth=6, bWidth=6)
  def rgbConfig = RgbConfig(rWidth=4, gWidth=4, bWidth=4)
  ////def rgbConfig = RgbConfig(rWidth=4, gWidth=4, bWidth=4)
  //def physRgbConfig = LcvVideoDithererIo.outRgbConfig(rgbConfig=rgbConfig)
  ////def vgaTimingInfo = LcvVgaTimingInfoMap.map("640x480@60")
  def vgaTimingInfo=LcvVgaTimingInfo(
    pixelClk=pixelClk,
    //pixelClk=25.175 MHz,
    htiming=LcvVgaTimingHv(
      //visib=1 << 6,
      //visib=64,
      //visib=1 << 8,
      //visib=1 << 8,
      //visib=4,
      //visib=8,
      visib=320,
      front=1,
      sync=1,
      back=1,
      //front=5,
      //sync=5,
      //back=5,
    ),
    vtiming=LcvVgaTimingHv(
      //visib=1 << 3,
      //visib=1 << 4,
      //visib=1 << 7,
      //visib=4,
      //visib=8,
      visib=240,
      front=1,
      sync=1,
      back=1,
      //front=5,
      //sync=5,
      //back=5,
    ),
  )

  def fbSize2d = vgaTimingInfo.fbSize2d
  //def gpu2dParams = DefaultGpu2dParams(
  //  rgbConfig=rgbConfig,
  //  intnlFbSize2d=ElabVec2[Int](
  //    x=vgaTimingInfo.fbSize2d.x,
  //    y=vgaTimingInfo.fbSize2d.y,
  //  ),
  //  physFbSize2dScalePow=ElabVec2[Int](
  //    x=log2Up(1),
  //    y=log2Up(1),
  //    //x=log2Up(2),
  //    ////y=log2Up(2),
  //    //y=log2Up(2),
  //  ),
  //  bgTileSize2dPow=ElabVec2[Int](
  //    //x=log2Up(8),
  //    //y=log2Up(8),
  //    x=log2Up(4),
  //    y=log2Up(4),
  //    //x=log2Up(2),
  //    //y=log2Up(2),
  //  ),
  //  objTileSize2dPow=ElabVec2[Int](
  //    x=log2Up(8),
  //    y=log2Up(8),
  //    //x=log2Up(4),
  //    //y=log2Up(4),
  //    //x=log2Up(2),
  //    //y=log2Up(2),
  //  ),
  //  //objTileWidthRshift=1,
  //  objTileWidthRshift=0,
  //  objAffineTileSize2dPow=ElabVec2[Int](
  //    //x=log2Up(8),
  //    //y=log2Up(8),
  //    x=log2Up(4),
  //    y=log2Up(4),
  //    //x=log2Up(2),
  //    //y=log2Up(2),
  //  ),
  //  //objAffineTileWidthRshift=0,
  //  objAffineTileWidthRshift=1,
  //  //numBgsPow=log2Up(4),
  //  numBgsPow=log2Up(2),
  //  //numObjsPow=log2Up(64),
  //  //numObjsPow=log2Up(32),
  //  //numObjsPow=log2Up(2),
  //  //numObjsPow=log2Up(32),
  //  //numObjsPow=log2Up(16),
  //  //numObjsPow=log2Up(2),
  //  //numObjsPow=log2Up(4),
  //  //numObjsPow=log2Up(8),
  //  numObjsPow=log2Up(16),
  //  numObjsAffinePow=log2Up(16),
  //  //numBgTilesPow=Some(log2Up(256)),
  //  //numBgTilesPow=Some(log2Up(2)),
  //  numBgTiles=Some(16),
  //  //numObjTilesPow=None,
  //  numObjTiles=Some(16),
  //  numObjAffineTiles=Some(16),
  //  numColsInBgPalPow=log2Up(64),
  //  numColsInObjPalPow=log2Up(64),
  //  noColorMath=true,
  //  noAffineBgs=true,
  //  noAffineObjs=false,
  //  //fancyObjPrio=false,
  //  fancyObjPrio=true,
  //)
  def gpu2dParams = DefaultGpu2dParams(
    rgbConfig=rgbConfig,
    intnlFbSize2d=ElabVec2[Int](
      x=vgaTimingInfo.fbSize2d.x,
      y=vgaTimingInfo.fbSize2d.y,
    ),
    physFbSize2dScalePow=ElabVec2[Int](
      x=log2Up(1),
      y=log2Up(1),
      //x=log2Up(2),
      ////y=log2Up(2),
      //y=log2Up(2),
    ),
    bgTileSize2dPow=ElabVec2[Int](
      x=log2Up(8),
      y=log2Up(8),
      //x=log2Up(4),
      //y=log2Up(4),
      //x=log2Up(2),
      //y=log2Up(2),
    ),
    objTileSize2dPow=ElabVec2[Int](
      x=log2Up(8),
      y=log2Up(8),
      //x=log2Up(4),
      //y=log2Up(4),
      //x=log2Up(2),
      //y=log2Up(2),
    ),
    objAffineTileSize2dPow=ElabVec2[Int](
      //x=log2Up(32),
      //y=log2Up(32),
      x=log2Up(8),
      y=log2Up(8),
    ),
    objAffineTileWidthRshift=0,
    //objAffineTileWidthRshift=log2Up(4),
    //numBgsPow=log2Up(4),
    numBgsPow=log2Up(2),
    //numObjsPow=log2Up(64),
    //numObjsPow=log2Up(32),
    //numObjsPow=log2Up(2),
    //numObjsPow=log2Up(32),
    //numObjsPow=log2Up(16),
    //numObjsPow=log2Up(2),
    //numObjsPow=log2Up(4),
    //numObjsPow=log2Up(8),
    numObjsPow=log2Up(
      16
    ),
    numObjsAffinePow=log2Up(
      32
    ),
    //numBgTilesPow=Some(log2Up(256)),
    //numBgTilesPow=Some(log2Up(2)),
    numBgTiles=Some(
      //16
      64
    ),
    //numObjTilesPow=None,
    numObjTiles=Some(
      //8
      16
    ),
    numObjAffineTiles=Some(
      32
    ),
    numColsInBgPalPow=log2Up(64),
    numColsInObjPalPow=log2Up(64),
    //--------
    noColorMath=true,
    noAffineBgs=true,
    noAffineObjs=false,
    fancyObjPrio=true,
    //--------
  )

  val simSpinalConfig = SpinalConfig(
    //defaultClockDomainFrequency=FixedFrequency(100 MHz)
    defaultClockDomainFrequency=FixedFrequency(clkRate)
  )
  SimConfig
    .withConfig(config=simSpinalConfig)
    .withVcdWave
    .compile(Gpu2dSimDut(
      clkRate=clkRate,
      rgbConfig=rgbConfig,
      vgaTimingInfo=vgaTimingInfo,
      gpu2dParams=gpu2dParams,
      ctrlFifoDepth=ctrlFifoDepth,
    ))
    .doSim { dut =>
      dut.clockDomain.forkStimulus(period=10)
      //SimTimeout(1000)
      //for (idx <- 0 to 4000) {
      //  //sleep(1)
      //  dut.clockDomain.waitRisingEdge()
      //}
      def simNumClks = (
        //16000
        32000
      )
      for (idx <- 0 to simNumClks - 1) {
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
