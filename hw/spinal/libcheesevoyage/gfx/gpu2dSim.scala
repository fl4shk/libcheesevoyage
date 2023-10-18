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
      //visib=1 << 8,
      //visib=4,
      //visib=8,
      front=1,
      sync=1,
      back=1,
    ),
    vtiming=LcvVgaTimingHv(
      //visib=1 << 3,
      visib=1 << 4,
      //visib=1 << 7,
      //visib=4,
      //visib=8,
      front=1,
      sync=1,
      back=1,
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
      //x=log2Up(8),
      //y=log2Up(8),
      x=log2Up(2),
      y=log2Up(2),
    ),
    //numBgsPow=log2Up(4),
    numBgsPow=log2Up(2),
    //numObjsPow=log2Up(64),
    //numObjsPow=log2Up(32),
    //numObjsPow=log2Up(2),
    //numObjsPow=log2Up(32),
    //numObjsPow=log2Up(16),
    //numObjsPow=log2Up(2),
    numObjsPow=log2Up(4),
    //numBgTilesPow=Some(log2Up(256)),
    //numBgTilesPow=Some(log2Up(2)),
    numBgTilesPow=Some(log2Up(16)),
    //numObjTilesPow=None,
    numObjTilesPow=Some(log2Up(8)),
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
    //for (jdx <- 0 to tempBgTile.pxsSize2d.y - 1) {
    //  for (idx <- 0 to tempBgTile.pxsSize2d.x - 1) {
    //    def pxsCoord = ElabVec2[Int](idx, jdx)
    //    if (jdx == 3) {
    //      if (idx == 0) {
    //        tempBgTile.setPx(
    //          pxsCoord=pxsCoord,
    //          //colIdx=1,
    //          colIdx=1,
    //        )
    //      }
    //      else if (idx == 1) {
    //        tempBgTile.setPx(
    //          pxsCoord=pxsCoord,
    //          colIdx=2,
    //        )
    //      }
    //      else {
    //        tempBgTile.setPx(
    //          pxsCoord=pxsCoord,
    //          //colIdx=3,
    //          colIdx=3,
    //        )
    //      }
    //    } else if (jdx == 4) {
    //      tempBgTile.setPx(
    //        pxsCoord=pxsCoord,
    //        colIdx=4,
    //      )
    //    } else if (jdx == 5) {
    //      if (idx == 0) {
    //        tempBgTile.setPx(
    //          pxsCoord=pxsCoord,
    //          colIdx=5,
    //        )
    //      } else {
    //        tempBgTile.setPx(
    //          pxsCoord=pxsCoord,
    //          colIdx=0,
    //        )
    //      }
    //    } else {
    //      tempBgTile.setPx(
    //        pxsCoord=pxsCoord,
    //        colIdx=0,
    //      )
    //    }
    //  }
    //}
    for (jdx <- 0 to tempBgTile.pxsSize2d.y - 1) {
      for (idx <- 0 to tempBgTile.pxsSize2d.y - 1) {
        def pxsCoord = ElabVec2[Int](idx, jdx)
        //// checkerboard pattern
        //if (jdx % 2 == 0) {
        //  tempBgTile.setPx(
        //    pxsCoord=pxsCoord,
        //    colIdx=(idx % 2) + 1,
        //  )
        //} else { // if (jdx % 2 == 1)
        //  tempBgTile.setPx(
        //    pxsCoord=pxsCoord,
        //    colIdx=((idx + 1) % 2) + 1,
        //  )
        //}

        //if (jdx % 4 == 0) {
        //} else if (jdx % 4 == 1) {
        //} else if (jdx % 4 == 2) {
        //}
        //if (idx == 0) {
        //  tempBgTile.setPx(
        //    pxsCoord=pxsCoord,
        //    colIdx=(jdx % 4) + 1,
        //  )
        //} else if (idx + 1 < tempBgTile.pxsSize2d.x) {
        //  tempBgTile.setPx(
        //    pxsCoord=pxsCoord,
        //    colIdx=jdx % 2,
        //  )
        //} else {
        //  tempBgTile.setPx(
        //    pxsCoord=pxsCoord,
        //    colIdx=(jdx % 4) + 2
        //  )
        //}

        tempBgTile.setPx(
          pxsCoord=pxsCoord,
          colIdx=1,
          //colIdx=0,
        )
      }
    }
    //tempBgTile.colIdxRowVec.assignFromBits(
    //  //tempBgTile.colIdxRowVec.getZero.asBits
    //)

    //val rBgTileCnt = Reg(UInt(gpu2dParams.numBgTilesPow + 2 bits)) init(-1)
    val nextBgTileCnt = SInt(gpu2dParams.numBgTilesPow + 2 bits)
    val rBgTileCnt = RegNext(nextBgTileCnt) init(-1)
    //val rBgTile = Reg(cloneOf(tempBgTile)) init(tempBgTile.getZero)
    val rBgTilePushValid = Reg(Bool()) init(True)
    val tempBgTileToPush = cloneOf(tempBgTile)

    //when (!rBgTileCnt.msb)
    when (rBgTileCnt < gpu2dParams.numBgTiles) {
      //when (rBgTileCnt + 1 === 1) {
      //  //gpuIo.bgTilePush.payload.tile := tempBgTile.getZero
      //  tempBgTile := tempBgTile
      //} otherwise {
      //  //gpuIo.bgTilePush.payload.tile := tempBgTile.getZero
      //  rBgTile := rBgTile.getZero
      //}
      when (rBgTileCnt === 1) {
        tempBgTileToPush := tempBgTile
      } otherwise {
        tempBgTileToPush := tempBgTileToPush.getZero
        when (nextBgTileCnt >= gpu2dParams.numBgTiles) {
          rBgTilePushValid := False
        }
      }
      when (gpuIo.bgTilePush.fire) {
        nextBgTileCnt := rBgTileCnt + 1
      } otherwise {
        nextBgTileCnt := rBgTileCnt
      }
    } otherwise {
      tempBgTileToPush := tempBgTileToPush.getZero
      nextBgTileCnt := rBgTileCnt
    }
    //when (rBgTileCnt + 1 >= gpu2dParams.numBgTiles) {
    //  rBgTilePushValid := False
    //}
    //gpuIo.bgTilePush.payload.memIdx := 1
    gpuIo.bgTilePush.valid := rBgTilePushValid
    //gpuIo.bgTilePush.valid := True
   // when (!rBgTileCnt.msb) {
      gpuIo.bgTilePush.payload.tile := tempBgTileToPush
      gpuIo.bgTilePush.payload.memIdx := (
        rBgTileCnt.asUInt(gpu2dParams.numBgTilesPow - 1 downto 0)
      )
    //} otherwise {
    //  gpuIo.bgTilePush.payload.tile := rBgTile
    //  gpuIo.bgTilePush.payload.memIdx := (
    //    rBgTileCnt(gpu2dParams.numBgTilesPow - 1 downto 0)
    //  )
    //}
    //gpuIo.bgTilePush.payload.memIdx := gpu2dParams.intnlFbSize2d.x
    //--------
    val tempBgAttrs = Gpu2dBgAttrs(params=gpu2dParams)
    //tempBgAttrs.scroll := tempBgAttrs.scroll.getZero
    //tempBgAttrs.scroll.x := 1
    //tempBgAttrs.scroll.y := 1
    tempBgAttrs.scroll.x := 0
    //tempBgAttrs.scroll.y := 2
    tempBgAttrs.scroll.y := 0
    //tempBgAttrs.scroll.x := 6
    //tempBgAttrs.scroll.y := 5
    tempBgAttrs.visib := True
    //tempBgAttrs.visib := False
    for (idx <- 0 to gpuIo.bgAttrsPushArr.size - 1) {
      val tempBgAttrsPush = gpuIo.bgAttrsPushArr(idx)
      if (idx == 0) {
        tempBgAttrsPush.valid := True
        tempBgAttrsPush.payload.bgAttrs := tempBgAttrs
      } else {
        //tempBgAttrsPush.valid := False
        tempBgAttrsPush.valid := True
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
        //tempBgEntryPush.payload.memIdx := 0x1
        tempBgEntryPush.payload.memIdx := 0x0
      } else {
        //tempBgEntryPush.valid := False
        tempBgEntryPush.valid := True
        tempBgEntryPush.payload.bgEntry := tempBgEntry.getZero
        tempBgEntryPush.payload.memIdx := 0x0
      }
    }
    //--------

    def palPush(
      numColsInPal: Int,
      rPalCnt: UInt,
      rPalEntry: Gpu2dPalEntry,
      rPalEntryPushValid: Bool,
      palPushFire: Bool
    ): Unit = {
      when (rPalCnt < numColsInPal) {
        when (palPushFire) {
          when (rPalCnt + 1 === 1) {
            rPalEntry.col.r := 0
            rPalEntry.col.g := 2
            rPalEntry.col.b := 4
          } elsewhen (rPalCnt + 1 === 2) {
            rPalEntry.col.r := (default -> True)
            rPalEntry.col.g.msb := True
            rPalEntry.col.g(rPalEntry.col.g.high - 1 downto 0) := 0x0
            rPalEntry.col.b := (default -> False)
          } elsewhen (rPalCnt + 1 === 3) {
            rPalEntry.col.r := 0x0
            //rPalEntry.col.g := (default -> True)
            rPalEntry.col.g := 0x3
            //rPalEntry.col.b := 0x0
            //rPalEntry.col.b := 0x3
            rPalEntry.col.b := 0x6
          } elsewhen (rPalCnt + 1 === 4) {
            rPalEntry.col.r := 0x0
            rPalEntry.col.g := 0x0
            rPalEntry.col.b := (default -> True)
          } elsewhen (rPalCnt + 1 === 5) {
            rPalEntry.col.r.msb := True
            rPalEntry.col.r(rPalEntry.col.r.high - 1 downto 0) := 0x0
            rPalEntry.col.g := 0x0
            rPalEntry.col.b := (default -> True)
          } elsewhen (rPalCnt + 1 === 6) {
            rPalEntry.col.r := 0x0
            rPalEntry.col.g.msb := True
            rPalEntry.col.g(rPalEntry.col.g.high - 1 downto 0) := 0x0
            rPalEntry.col.b := 5
          } otherwise {
            rPalEntryPushValid := False
          }
          rPalCnt := rPalCnt + 1
        }
      }
    }
    def bgPalCntWidth = gpu2dParams.numColsInBgPalPow + 1
    val rBgPalCnt = Reg(UInt(bgPalCntWidth bits)) init(0x0)
    val rBgPalEntry = Reg(Gpu2dPalEntry(params=gpu2dParams))
    rBgPalEntry.init(rBgPalEntry.getZero)
    val rBgPalEntryPushValid = Reg(Bool()) init(True)

    //gpuIo.bgPalEntryPush.valid := True
    gpuIo.bgPalEntryPush.valid := rBgPalEntryPushValid
    gpuIo.bgPalEntryPush.payload.bgPalEntry := rBgPalEntry
    //gpuIo.bgPalEntryPush.payload.memIdx := 1
    gpuIo.bgPalEntryPush.payload.memIdx := rBgPalCnt.resized

    //otherwise {
    //}
    palPush(
      numColsInPal=gpu2dParams.numColsInBgPal,
      rPalCnt=rBgPalCnt,
      rPalEntry=rBgPalEntry,
      rPalEntryPushValid=rBgPalEntryPushValid,
      palPushFire=gpuIo.bgPalEntryPush.fire,
    )
    //--------
    val tempObjTile = Gpu2dTile(
      params=gpu2dParams,
      isObj=true,
    )
    def mkObjTile(
      colIdx0: Int,
      colIdx1: Int,
    ): Unit = {
      for (jdx <- 0 to tempObjTile.pxsSize2d.y - 1) {
        for (idx <- 0 to tempObjTile.pxsSize2d.y - 1) {
          def pxsCoord = ElabVec2[Int](idx, jdx)
          if (jdx % 2 == 0) {
            tempObjTile.setPx(
              pxsCoord=pxsCoord,
              //colIdx=(idx % 2) + 1,
              //colIdx=3,
              colIdx=colIdx0,
            )
          } else { // if (jdx % 2 == 1)
            tempObjTile.setPx(
              pxsCoord=pxsCoord,
              //colIdx=((idx + 1) % 2) + 1,
              //colIdx=4,
              colIdx=colIdx1,
            )
          }
        }
      }
    }
    val nextObjTileCnt = SInt(gpu2dParams.numObjTilesPow + 2 bits)
    val rObjTileCnt = RegNext(nextObjTileCnt) init(-1)
    val rObjTilePushValid = Reg(Bool()) init(True)

    when (rObjTileCnt < gpu2dParams.numObjTiles) {
      when (gpuIo.objTilePush.fire) {
        when (rObjTileCnt === 0) {
          //mkObjTile(0, 1)
          mkObjTile(0, 0)
        } elsewhen (rObjTileCnt === 1) {
          mkObjTile(1, 2)
          //mkObjTile(3, 3)
          //mkObjTile(2, 3)
        } elsewhen (rObjTileCnt === 2) {
          mkObjTile(2, 3)
          //mkObjTile(2, 2)
        } elsewhen (rObjTileCnt === 3) {
          mkObjTile(3, 4)
        } elsewhen (rObjTileCnt === 4) {
          mkObjTile(4, 5)
        } otherwise {
          tempObjTile := tempObjTile.getZero
          //when (rObjTileCnt >= gpu2dParams.numObjTiles) {
          //  rObjTilePushValid := False
          //}
        }
        nextObjTileCnt := rObjTileCnt + 1
      } otherwise {
        tempObjTile := tempObjTile.getZero
        nextObjTileCnt := rObjTileCnt
      }
    } otherwise {
      tempObjTile := tempObjTile.getZero
      nextObjTileCnt := rObjTileCnt
    }
    when (rObjTileCnt + 1 >= gpu2dParams.numObjTiles) {
      rObjTilePushValid := False
    }


    gpuIo.objTilePush.valid := rObjTilePushValid
    gpuIo.objTilePush.payload.tile := tempObjTile
    gpuIo.objTilePush.payload.memIdx := (
      rObjTileCnt.asUInt(gpuIo.objTilePush.payload.memIdx.bitsRange)
    )
    //--------
    def objPalCntWidth = gpu2dParams.numColsInObjPalPow + 1
    val rObjPalCnt = Reg(UInt(objPalCntWidth bits)) init(0x0)
    val rObjPalEntry = Reg(Gpu2dPalEntry(params=gpu2dParams))
    rObjPalEntry.init(rObjPalEntry.getZero)
    val rObjPalEntryPushValid = Reg(Bool()) init(True)

    //gpuIo.objPalEntryPush.valid := True
    gpuIo.objPalEntryPush.valid := rObjPalEntryPushValid
    gpuIo.objPalEntryPush.payload.objPalEntry := rObjPalEntry
    //gpuIo.objPalEntryPush.payload.memIdx := 1
    gpuIo.objPalEntryPush.payload.memIdx := rObjPalCnt.resized

    //otherwise {
    //}
    palPush(
      numColsInPal=gpu2dParams.numColsInObjPal,
      rPalCnt=rObjPalCnt,
      rPalEntry=rObjPalEntry,
      rPalEntryPushValid=rObjPalEntryPushValid,
      palPushFire=gpuIo.objPalEntryPush.fire,
    )


    //gpuIo.objTilePush.valid := True
    ////gpuIo.objTilePush.payload := gpuIo.objTilePush.payload.getZero

    ////gpuIo.objAttrsPush.valid := True
    ////gpuIo.objAttrsPush.payload := gpuIo.objAttrsPush.payload.getZero
    //gpuIo.objPalEntryPush.valid := True
    //gpuIo.objPalEntryPush.payload := gpuIo.objPalEntryPush.payload.getZero


    val tempObjAttrs = Gpu2dObjAttrs(params=gpu2dParams)
    val objAttrsCntWidth = gpu2dParams.numObjsPow + 2
    //val rObjAttrsCnt = Reg(UInt(objAttrsCntWidth bits)) init(0x0)
    //val nextObjAttrsCnt = UInt(objAttrsCntWidth bits)
    //val rObjAttrsCnt = RegNext(nextObjAttrsCnt) init(0x0)
    val nextObjAttrsCnt = SInt(objAttrsCntWidth bits)
    val rObjAttrsCnt = RegNext(nextObjAttrsCnt) init(-1)
    //val rObjAttrs = Reg(Gpu2dObjAttrs(params=gpu2dParams))
    //rObjAttrs.init(rObjAttrs.getZero)
    val rObjAttrsEntryPushValid = Reg(Bool()) init(True)

    when (rObjAttrsCnt < gpu2dParams.numObjs) {
      when (rObjAttrsCnt === 0) {
        tempObjAttrs.tileMemIdx := 1
        tempObjAttrs.pos.x := 16
        tempObjAttrs.pos.y := 3
        tempObjAttrs.prio := 0
        tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //} elsewhen (rObjAttrsCnt === 1) {
      //  tempObjAttrs.tileMemIdx := 2
      //  //tempObjAttrs.tileMemIdx := 2
      //  //tempObjAttrs.tileMemIdx := 0
      //  //tempObjAttrs.pos.x := 1
      //  //tempObjAttrs.pos.x := 16
      //  //tempObjAttrs.pos.x := 2
      //  tempObjAttrs.pos.x := 6
      //  //tempObjAttrs.pos.y := -1
      //  tempObjAttrs.pos.y := 6
      //  tempObjAttrs.prio := 0
      //  tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      ////} elsewhen (rObjAttrsCnt === 2) {
      ////  //tempObjAttrs.tileMemIdx := 1
      ////  //tempObjAttrs.pos.x := 8
      ////  //tempObjAttrs.pos.y := 0
      ////  //tempObjAttrs.prio := 1
      ////  //tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      ////  tempObjAttrs := tempObjAttrs.getZero
      ////} elsewhen (rObjAttrsCnt === 3) {
      ////  //tempObjAttrs.tileMemIdx := 0
      ////  //tempObjAttrs.pos.x := 8
      ////  //tempObjAttrs.pos.y := 0 //+ gpu2dParams.objTileSize2d.y - 1
      ////  //tempObjAttrs.prio := 0
      ////  //tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      ////  tempObjAttrs := tempObjAttrs.getZero
      } otherwise {
        //tempObjAttrs := tempObjAttrs.getZero
        tempObjAttrs.tileMemIdx := 0
        tempObjAttrs.pos.x := -gpu2dParams.objTileSize2d.x
        tempObjAttrs.pos.y := 0
        tempObjAttrs.prio := 1
        tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
        when (nextObjAttrsCnt >= gpu2dParams.numObjs) {
          rObjAttrsEntryPushValid := False
        }
        //rObjAttrsEntryPushValid := False
      }
      when (gpuIo.objAttrsPush.fire) {
        nextObjAttrsCnt := rObjAttrsCnt + 1
      } otherwise {
        nextObjAttrsCnt := rObjAttrsCnt
      }
      //} otherwise {
      //  tempObjAttrs := tempObjAttrs.getZero
      //  nextObjAttrsCnt := rObjAttrsCnt
      //}
    } otherwise {
      tempObjAttrs := tempObjAttrs.getZero
      nextObjAttrsCnt := rObjAttrsCnt
    }

    gpuIo.objAttrsPush.valid := rObjAttrsEntryPushValid
    //gpuIo.objAttrsPush.payload.objAttrs := (
    //  Gpu2dObjAttrs(params=gpu2dParams).getZero
    //)
    gpuIo.objAttrsPush.payload.objAttrs := tempObjAttrs
    gpuIo.objAttrsPush.payload.memIdx := (
      rObjAttrsCnt.asUInt(gpu2dParams.objAttrsMemIdxWidth - 1 downto 0)
    )

    //when (rObjPalCnt < gpu2dParams.numColsInBgPal) {
    //  rObjPalCnt := rObjPalCnt + 1
    //}

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
    //ctrlIo.push.valid := False
    //ctrlIo.push.payload := ctrlIo.push.payload.getZero
    //gpuIo.pop.ready := True

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
