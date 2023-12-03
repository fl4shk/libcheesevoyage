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

object Gpu2dSimDutConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    ),
    onlyStdLogicVectorAtTopLevelIo=true,
  )
}
object Gpu2dSimDutParams {
  //def clkRate = 125.0 MHz
  def clkRate = 50.0 MHz
  //def clkRate = 100.0 MHz
  //def clkRate = 100.7 MHz
  def pixelClk = 25.0 MHz
  //def ctrlFifoDepth = 20
  //def ctrlFifoDepth = 256
  //def ctrlFifoDepth = 100
  //def ctrlFifoDepth = 128
  def ctrlFifoDepth = 10
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
      //visib=1 << 7,
      visib=320,
      //visib=1 << 8,
      //visib=4,
      //visib=8,
      front=1,
      sync=1,
      back=1,
    ),
    vtiming=LcvVgaTimingHv(
      //visib=1 << 3,
      //visib=1 << 4,
      //visib=1 << 7,
      visib=240,
      //visib=4,
      //visib=8,
      front=1,
      sync=1,
      back=1,
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
  //    x=log2Up(8),
  //    y=log2Up(8),
  //    //x=log2Up(4),
  //    //y=log2Up(4),
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
  //  objTileWidthRshift=1,
  //  objAffineTileSize2dPow=ElabVec2[Int](
  //    //x=log2Up(32),
  //    //y=log2Up(32),
  //    //x=log2Up(16),
  //    //y=log2Up(16),
  //    x=log2Up(8),
  //    y=log2Up(8),
  //  ),
  //  objAffineTileWidthRshift=0,
  //  //objAffineTileWidthRshift=log2Up(4),
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
  //  numObjsPow=log2Up(
  //    //16
  //    8
  //  ),
  //  numObjsAffinePow=log2Up(
  //    //32
  //    //8
  //    4
  //  ),
  //  //numBgTilesPow=Some(log2Up(256)),
  //  //numBgTilesPow=Some(log2Up(2)),
  //  numBgTiles=Some(
  //    //16
  //    64
  //  ),
  //  //numObjTilesPow=None,
  //  numObjTiles=Some(
  //    //8
  //    16
  //  ),
  //  numObjAffineTiles=Some(
  //    //32
  //    //8
  //    4
  //  ),
  //  numColsInBgPalPow=log2Up(64),
  //  numColsInObjPalPow=log2Up(64),
  //  //--------
  //  noColorMath=true,
  //  noAffineBgs=true,
  //  //noAffineObjs=true,
  //  noAffineObjs=false,
  //  fancyObjPrio=true,
  //  //--------
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
      //x=log2Up(8),
      //y=log2Up(8),
      x=log2Up(4),
      y=log2Up(4),
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
    //objTileWidthRshift=0,
    objTileWidthRshift=1,
    objAffineTileSize2dPow=ElabVec2[Int](
      x=log2Up(8),
      y=log2Up(8),
      //x=log2Up(4),
      //y=log2Up(4),
      //x=log2Up(2),
      //y=log2Up(2),
    ),
    //objAffineTileWidthRshift=0,
    objAffineTileWidthRshift=1,
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
    numObjsPow=log2Up(16),
    numObjsAffinePow=log2Up(16),
    //numBgTilesPow=Some(log2Up(256)),
    //numBgTilesPow=Some(log2Up(2)),
    numBgTiles=(
      //Some(16)
      //Some(320 * 240)
      // for double buffering
      Some(
        vgaTimingInfo.fbSize2d.x
        * vgaTimingInfo.fbSize2d.y
        * 2
      )
    ),
    //numObjTilesPow=None,
    numObjTiles=Some(16),
    numObjAffineTiles=Some(16),
    numColsInBgPalPow=log2Up(64),
    numColsInObjPalPow=log2Up(64),
    noColorMath=true,
    noAffineBgs=true,
    //noAffineObjs=true,
    noAffineObjs=false,
    //fancyObjPrio=false,
    fancyObjPrio=true,
  )
}

object Gpu2dSimDutToVerilog extends App {
  Gpu2dSimDutConfig.spinal.generateVerilog(Gpu2dSimDut(
    clkRate=Gpu2dSimDutParams.clkRate,
    rgbConfig=Gpu2dSimDutParams.rgbConfig,
    vgaTimingInfo=Gpu2dSimDutParams.vgaTimingInfo,
    gpu2dParams=Gpu2dSimDutParams.gpu2dParams,
    ctrlFifoDepth=Gpu2dSimDutParams.ctrlFifoDepth,
  ))
}

object Gpu2dSimDutToVhdl extends App {
  Gpu2dSimDutConfig.spinal.generateVhdl(Gpu2dSimDut(
    clkRate=Gpu2dSimDutParams.clkRate,
    rgbConfig=Gpu2dSimDutParams.rgbConfig,
    vgaTimingInfo=Gpu2dSimDutParams.vgaTimingInfo,
    gpu2dParams=Gpu2dSimDutParams.gpu2dParams,
    ctrlFifoDepth=Gpu2dSimDutParams.ctrlFifoDepth,
  ))
  //val report = SpinalVhdl(new Gpu2dTo())
  //report.printPruned()
  //val test = PipeSkidBuf(UInt(3 bits))
}

