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
      visib=1 << 7,
      front=3,
      sync=3,
      back=3,
    ),
    vtiming=LcvVgaTimingHv(
      //visib=1 << 3,
      visib=1 << 7,
      front=3,
      sync=3,
      back=3,
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
        if (jdx == 3) {
          if (idx == 0) {
            tempBgTile.setPx(
              pxsCoord=ElabVec2[Int](idx, jdx),
              //colIdx=1,
              colIdx=1,
            )
          }
          else if (idx == 1) {
            tempBgTile.setPx(
              pxsCoord=ElabVec2[Int](idx, jdx),
              colIdx=2,
            )
          }
          else {
            tempBgTile.setPx(
              pxsCoord=ElabVec2[Int](idx, jdx),
              //colIdx=3,
              colIdx=3,
            )
          }
        }
        else if (jdx == 4) {
          tempBgTile.setPx(
            pxsCoord=ElabVec2(idx, jdx),
            colIdx=4,
          )
        }

        else {
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
    gpuIo.bgTilePush.payload.memIdx := 1

    val tempBgAttrs = Gpu2dBgAttrs(params=gpu2dParams)
    tempBgAttrs.scroll := tempBgAttrs.scroll.getZero
    for (idx <- 0 to gpuIo.bgAttrsPushArr.size - 1) {
      val tempBgAttrsPush = gpuIo.bgAttrsPushArr(idx)
      if (idx == 0) {
        tempBgAttrsPush.valid := True
        tempBgAttrsPush.payload.bgAttrs := tempBgAttrs
      } else {
        tempBgAttrsPush.valid := False
        tempBgAttrsPush.payload.bgAttrs := tempBgAttrs.getZero
      }
    }

    val tempBgEntry = Gpu2dBgEntry(params=gpu2dParams)
    // we're only changing one tile
    tempBgEntry.tileMemIdx := 1
    tempBgEntry.dispFlip.x := False
    tempBgEntry.dispFlip.y := False

    for (idx <- 0 to gpuIo.bgEntryPushArr.size - 1) {
      val tempBgEntryPush = gpuIo.bgEntryPushArr(idx)
      if (idx == 0) {
        tempBgEntryPush.valid := True
        tempBgEntryPush.payload.bgEntry := tempBgEntry
        tempBgEntryPush.payload.memIdx := 0x0
      } else {
        //tempBgEntryPush.valid := False
        tempBgEntryPush.valid := True
        tempBgEntryPush.payload.bgEntry := tempBgEntry.getZero
        tempBgEntryPush.payload.memIdx := 0x0
      }
    }

    val bgPalCntWidth = gpu2dParams.numColsInBgPalPow + 1
    val rBgPalCnt = Reg(UInt(bgPalCntWidth bits)) init(0x0)
    val rBgPalEntry = Reg(Gpu2dBgPalEntry(params=gpu2dParams))
    rBgPalEntry.init(rBgPalEntry.getZero)
    val rBgPalEntryPushValid = Reg(Bool()) init(True)

    when (rBgPalCnt < gpu2dParams.numColsInBgPal) {
      when (gpuIo.bgPalEntryPush.fire) {
        when (rBgPalCnt + 1 === 1) {
          rBgPalEntry.col.r := 0
          rBgPalEntry.col.g := 2
          rBgPalEntry.col.b := 4
        } elsewhen (rBgPalCnt + 1 === 2) {
          rBgPalEntry.col.r := (default -> True)
          rBgPalEntry.col.g.msb := True
          rBgPalEntry.col.g(rBgPalEntry.col.g.high - 1 downto 0) := 0x0
          rBgPalEntry.col.b := (default -> False)
        } elsewhen (rBgPalCnt + 1 === 3) {
          rBgPalEntry.col.r := 0x0
          rBgPalEntry.col.g := (default -> True)
          rBgPalEntry.col.b := 0x0
        } elsewhen (rBgPalCnt + 1 === 4) {
          rBgPalEntry.col.r := 0x0
          rBgPalEntry.col.g := 0x0
          rBgPalEntry.col.b := (default -> True)
        } elsewhen (rBgPalCnt + 1 === 5) {
          rBgPalEntry.col.r.msb := True
          rBgPalEntry.col.r(rBgPalEntry.col.r.high - 1 downto 0) := 0x0
          rBgPalEntry.col.g := 0x0
          rBgPalEntry.col.b := (default -> True)
        } elsewhen (rBgPalCnt + 1 === 6) {
          rBgPalEntry.col.r := 0x0
          rBgPalEntry.col.g.msb := True
          rBgPalEntry.col.g(rBgPalEntry.col.g.high - 1 downto 0) := 0x0
          rBgPalEntry.col.b := 5
        } otherwise {
          rBgPalEntryPushValid := False
        }
        rBgPalCnt := rBgPalCnt + 1
      }
    }
    //otherwise {
    //}


    //gpuIo.bgPalEntryPush.valid := True
    gpuIo.bgPalEntryPush.valid := rBgPalEntryPushValid
    gpuIo.bgPalEntryPush.payload.bgPalEntry := rBgPalEntry
    //gpuIo.bgPalEntryPush.payload.memIdx := 1
    gpuIo.bgPalEntryPush.payload.memIdx := rBgPalCnt.resized

    gpuIo.objTilePush.valid := False
    gpuIo.objTilePush.payload := gpuIo.objTilePush.payload.getZero
    gpuIo.objAttrsPush.valid := False
    gpuIo.objAttrsPush.payload := gpuIo.objAttrsPush.payload.getZero
    gpuIo.objPalEntryPush.valid := False
    gpuIo.objPalEntryPush.payload := gpuIo.objPalEntryPush.payload.getZero

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
      def simNumClks = 16000
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
