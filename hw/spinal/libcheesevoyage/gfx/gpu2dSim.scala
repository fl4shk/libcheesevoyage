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

object Gpu2dSim extends App {
  //def clkRate = 125.0 MHz
  //def clkRate = 50.0 MHz
  def clkRate = 100.0 MHz
  //def clkRate = 100.7 MHz
  def pixelClk = 25.0 MHz
  //def ctrlFifoDepth = 20
  def ctrlFifoDepth = 16
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
      visib=1 << 7,
      front=5,
      sync=5,
      back=5,
    ),
    vtiming=LcvVgaTimingHv(
      //visib=1 << 3,
      visib=1 << 7,
      front=5,
      sync=5,
      back=5,
    ),
  )

  def fbSize2d = vgaTimingInfo.fbSize2d
  def gpu2dParams = DefaultGpu2dParams(
    rgbConfig=rgbConfig,
    intnlFbSize2d=ElabVec2[Int](
      x=vgaTimingInfo.fbSize2d.x,
      y=vgaTimingInfo.fbSize2d.y,
    ),
    physFbSize2dScalePow=ElabVec2[Int](
      x=log2Up(1),
      y=log2Up(1),
    ),
    bgTileSize2dPow=ElabVec2[Int](
      x=log2Up(8),
      y=log2Up(8),
    ),
    objTileSize2dPow=ElabVec2[Int](
      x=log2Up(8),
      y=log2Up(8),
    ),
    //numBgsPow=log2Up(4),
    numBgsPow=log2Up(2),
    numObjsPow=log2Up(64),
    numBgTilesPow=Some(log2Up(256)),
    numObjTilesPow=None,
    numColsInBgPalPow=log2Up(64),
    numColsInObjPalPow=log2Up(64),
  )

  case class Dut() extends Component {
    val io = new Bundle {
      //val phys = out(LcvVgaPhys(rgbConfig=physRgbConfig))
      val phys = out(LcvVgaPhys(rgbConfig=rgbConfig))
      val misc = out(LcvVgaCtrlMiscIo(
        clkRate=clkRate,
        vgaTimingInfo=vgaTimingInfo,
        fifoDepth=ctrlFifoDepth,
      ))
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
    //  rgbConfig=rgbConfig,
    //  //vgaTimingInfo=vgaTimingInfo,
    //  //fbSize2d=vgaTimingInfo.fbSize2d,
    //  fbSize2d=fbSize2d,
    //)
    val gpu2d = Gpu2d(
      params=gpu2dParams,
    )

    val ctrlIo = vgaCtrl.io
    //val dithIo = vidDith.io
    val gpuIo = gpu2d.io

    val tempBgTile = Gpu2dTile(params=gpu2dParams, isObj=false)
    //val tempBgTileRow0Vec = (
    //  tempBgTile.colIdxRowVec(0).subdivideIn(
    //    gpu2dParams.bgTileSize2d.x slices
    //  )
    //)
    //tempBgTileRow0Vec(0) := 1
    //for (idx <- 0 to tempBgTile.colIdxRowVec.size - 1) {
    //  tempBgTile.colIdxRowVec(0)(
    //    gpu2dParams.bgPalEntryMemIdxWidth - 1 downto 0
    //  ) := 1
    //  tempBgTile
    //}
    for (jdx <- 0 to tempBgTile.pxsSize2d.y - 1) {
      for (idx <- 0 to tempBgTile.pxsSize2d.x - 1) {
        if (jdx == 0 && idx == 0) {
          tempBgTile.setPx(
            pxsCoord=ElabVec2[Int](idx, jdx),
            colIdx=1,
          )
        } else {
          tempBgTile.setPx(
            pxsCoord=ElabVec2[Int](idx, jdx),
            colIdx=0,
          )
        }
      }
    }
    //tempBgTile.colIdxRowVec.assignFromBits(
    //  //tempBgTile.colIdxRowVec.getZero.asBits
    //)

    gpuIo.bgTilePush.valid := True
    gpuIo.bgTilePush.payload.tile := tempBgTile
    gpuIo.bgTilePush.payload.memIdx := 0

    val tempBgPalEntry = Gpu2dBgPalEntry(params=gpu2dParams)
    tempBgPalEntry.col.r := (default -> True)
    tempBgPalEntry.col.g.msb := True
    tempBgPalEntry.col.g(tempBgPalEntry.col.g.high - 1 downto 0) := 0x0
    tempBgPalEntry.col.b := (default -> False)
    gpuIo.bgPalEntryPush.valid := True
    gpuIo.bgPalEntryPush.payload.bgPalEntry := tempBgPalEntry
    gpuIo.bgPalEntryPush.payload.memIdx := 1

    //ctrlIo.en := True
    //ctrlIo.push.valid := dithIo.pop.valid
    //ctrlIo.push.payload := dithIo.pop.payload.col
    //dithIo.pop.ready := ctrlIo.push.ready

    //dithIo.push.valid := gpuIo.pop.valid
    //dithIo.push.payload := gpuIo.pop.payload.col 
    //gpuIo.pop.ready := dithIo.push.ready
    ctrlIo.en := True

    ctrlIo.push.valid := gpuIo.pop.valid
    ctrlIo.push.payload := gpuIo.pop.payload.col
    gpuIo.pop.ready := ctrlIo.push.ready

    io.phys := ctrlIo.phys
    io.misc := ctrlIo.misc
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
