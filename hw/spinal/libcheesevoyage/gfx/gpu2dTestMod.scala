package libcheesevoyage.gfx
import libcheesevoyage._

import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2
import libcheesevoyage.general.DualTypeNumVec2

import spinal.core._
//import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import scala.collection.mutable.ArrayBuffer
import scala.math._

case class Gpu2dTestIo(
  params: Gpu2dParams
) extends Bundle {
  //--------
  //val gpuIo = master(Gpu2dIo(params=params))
  val pop = master(Gpu2dPushInp(params=params))
  //--------
  //val ctrlEn = out Bool()
  //val pop = master Stream(Gpu2dPopPayload(params=params))
  //--------
}
case class Gpu2dTest(
  params: Gpu2dParams
) extends Component {
  //--------
  val io = Gpu2dTestIo(
    params=params
  )
  //def gpuIo = io.gpu2dPush
  //io.ctrlEn := gpuIo.ctrlEn
  ////io.pop <> gpuIo.pop
  ////io.pop <> gpuIo.pop
  //io.pop << io.gpuIo.pop
  def pop = io.pop
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
  def mkTile(
    //tempTile: Gpu2dTileFull,
    tempTileSlice: Gpu2dTileSlice,
    //pxCoordStart: ElabVec2[Int],
    pxCoordXStart: UInt,
    pxCoordY: UInt,
    palEntryMemIdxWidth: Int,
    colIdx0: Int,
    colIdx1: Int,
    colIdx2: Option[Int]=None,
    colIdx3: Option[Int]=None,
    //onePx: Boolean=false,
  ): Unit = {
    switch (pxCoordY) {
      for (jdx <- 0 until tempTileSlice.fullPxsSize2d.y) {
        is (jdx) {
          //def jdx = pxCoordY
          //println(tempTileSlice.pxsSliceWidth)
          //println(tempTileSlice.pxsSliceWidth)
          for (
            //idx <- until to tempTileSlice.pxsSize2d.x
            idx <- 0 until tempTileSlice.pxsSliceWidth
          ) {
            //def pxCoord = ElabVec2[Int](idx, jdx)
            def pxCoordX = (
              //if (!onePx) {
                idx + pxCoordXStart
              //} else {
              //  //pxCoordXStart
              //  (
              //    idx
              //    + U{
              //      def tempWidthPow = pxCoordXStart.getWidth
              //      f"$tempWidthPow'd0"
              //    }
              //  )
              //}
            )
            //print(pxCoordX.getWidth)
            //print(" ")
            //println(f"$idx")
            if (jdx % 2 == 0) {
              val myColIdx = colIdx2 match {
                case Some(tempColIdx) => {
                  if (idx % 2 == 0) {
                    colIdx0
                  } else {
                    tempColIdx
                  }
                }
                case None => colIdx0
              }
              tempTileSlice.setPx(
                pxCoordX=pxCoordX,//.resized,
                colIdx=U(f"$palEntryMemIdxWidth'd$myColIdx"),
              )
            } else { // if (jdx % 2 == 1)
              val myColIdx = colIdx3 match {
                case Some(tempColIdx) => {
                  if (idx % 2 == 0) {
                    colIdx1
                  } else {
                    tempColIdx
                  }
                }
                case None => colIdx1
              }
              tempTileSlice.setPx(
                pxCoordX=pxCoordX,//.resized,
                colIdx=U(f"$palEntryMemIdxWidth'd$myColIdx"),
              )
            }
          }
        }
      }
    }
  }
  //--------
  val nextColorMathTileCnt = SInt(
    //params.numColorMathTilesPow + 2 bits
    params.colorMathTileMemIdxWidth + 5 bits
  )
  val rColorMathTileCnt = RegNext(nextColorMathTileCnt) init(-1)
  val rColorMathTilePushValid = Reg(Bool()) init(True)
  //val tempColorMathTile = Gpu2dTileFull(
  //  params=params,
  //  isObj=false,
  //  isAffine=false,
  //)
  val tempColorMathTileSlice = Gpu2dTileSlice(
    params=params,
    isObj=false,
    isAffine=false,
  )
  def mkColorMathTile(
    colIdx0: Int,
    colIdx1: Int,
    colIdx2: Option[Int]=None,
    colIdx3: Option[Int]=None,
  ): Unit = {
    mkTile(
      //tempTile=tempColorMathTile,
      tempTileSlice=tempColorMathTileSlice,
      pxCoordXStart={
        //rColorMathTileCnt(params.bgTileSize2dPow.x - 1 downto 0).asUInt
        def tempWidthPow = params.bgTileSize2dPow.x
        U(f"$tempWidthPow'd0")
      },
      pxCoordY=(
        rColorMathTileCnt(
          params.bgTileSize2dPow.y - 1
          //downto params.bgTileSize2dPow.x
          downto 0
        ).asUInt
      ),
      palEntryMemIdxWidth=params.bgPalEntryMemIdxWidth,
      colIdx0=colIdx0,
      colIdx1=colIdx1,
      colIdx2=colIdx2,
      colIdx3=colIdx3,
    )
  }

  def tempColorMathTileCnt = (
    rColorMathTileCnt
    //>> (params.bgTileSize2dPow.y + params.bgTileSize2dPow.x)
    >> params.bgTileSize2dPow.y
  )
  tempColorMathTileSlice := tempColorMathTileSlice.getZero
  tempColorMathTileSlice.allowOverride
  when (
    //rColorMathTileCnt < params.numColorMathTiles
    tempColorMathTileCnt < params.numColorMathTiles
  ) {
    when (pop.colorMathTilePush.fire) {
      when (tempColorMathTileCnt === 0) {
        mkColorMathTile(0, 0)
      } elsewhen (tempColorMathTileCnt === 1) {
        mkColorMathTile(1, 1)
      } elsewhen (tempColorMathTileCnt === 2) {
        mkColorMathTile(2, 2)
      } elsewhen (tempColorMathTileCnt === 3) {
        mkColorMathTile(3, 3)
      } elsewhen (tempColorMathTileCnt === 4) {
        mkColorMathTile(4, 4)
      } otherwise {
        //tempColorMathTileSlice := tempColorMathTileSlice.getZero
      }
      nextColorMathTileCnt := rColorMathTileCnt + 1
    } otherwise {
      //tempColorMathTileSlice := tempColorMathTileSlice.getZero
      nextColorMathTileCnt := rColorMathTileCnt
    }
  } otherwise {
    //tempColorMathTileSlice := tempColorMathTileSlice.getZero
    nextColorMathTileCnt := rColorMathTileCnt
  }
  when (tempColorMathTileCnt >= params.numColorMathTiles - 1) {
    rColorMathTilePushValid := False
  }

  pop.colorMathTilePush.valid := rColorMathTilePushValid
  //pop.colorMathTilePush.payload.tile := tempColorMathTile
  pop.colorMathTilePush.payload.memIdx := (
    rColorMathTileCnt.asUInt(
      pop.colorMathTilePush.payload.memIdx.bitsRange
    )
  )
  //pop.colorMathTilePush.valid := False
  //pop.colorMathTilePush.payload := pop.colorMathTilePush.payload.getZero

  //pop.colorMathEntryPush.valid := False
  //pop.colorMathEntryPush.payload := pop.colorMathEntryPush.payload.getZero
  val rColorMathEntryPushValid = Reg(Bool()) init(True)
  val tempColorMathEntry = Gpu2dBgEntry(
    params=params,
    isColorMath=true,
  )
  //val colorMathEntryCntWidth = params.numColorMathsPow + 2
  val colorMathEntryCntWidth = params.bgEntryMemIdxWidth + 2
  val nextColorMathEntryCnt = SInt(colorMathEntryCntWidth bits)
  val rColorMathEntryCnt = RegNext(nextColorMathEntryCnt) init(-1)
  //val rColorMathEntryPushValid = Reg(Bool()) init(True)
  // we're only changing one tile
  //tempColorMathEntry.tileMemIdx := 1
  //tempColorMathEntry.dispFlip.x := False
  //tempColorMathEntry.dispFlip.y := False

  val rColorMathEntryMemIdx = Reg(SInt(
    (params.bgEntryMemIdxWidth + 1) bits
  ))
    .init((1 << params.bgEntryMemIdxWidth) - 1)
  when (rColorMathEntryCnt < (1 << params.bgEntryMemIdxWidth)) {
    when (pop.colorMathEntryPush.fire) {
      when (rColorMathEntryCnt < 5) {
        tempColorMathEntry.tileIdx := rColorMathEntryCnt.asUInt.resized
        tempColorMathEntry.dispFlip.x := False
        tempColorMathEntry.dispFlip.y := False
      } otherwise {
        tempColorMathEntry := tempColorMathEntry.getZero
        //when (rColorMathEntryCnt >= params.numColorMathEntrys) {
        //  rColorMathEntryPushValid := False
        //}
      }
      nextColorMathEntryCnt := rColorMathEntryCnt + 1
    } otherwise {
      tempColorMathEntry := tempColorMathEntry.getZero
      nextColorMathEntryCnt := rColorMathEntryCnt
    }
  } otherwise {
    tempColorMathEntry := tempColorMathEntry.getZero
    nextColorMathEntryCnt := rColorMathEntryCnt
  }
  when (rColorMathEntryCnt + 1
    >= (1 << params.bgEntryMemIdxWidth)) {
    rColorMathEntryPushValid := False
  }

  pop.colorMathEntryPush.valid := rColorMathEntryPushValid
  //tempColorMathEntryPush.payload.colorMathEntry.tileMemIdx := (
  //  rColorMathEntryMemIdx.asUInt(
  //    params.bgEntryMemIdxWidth - 1 downto 0
  //  )
  //)
  pop.colorMathEntryPush.payload.bgEntry := tempColorMathEntry
  pop.colorMathEntryPush.payload.memIdx := (
    rColorMathEntryCnt.asUInt(
      pop.colorMathEntryPush.payload.memIdx.bitsRange
    )
  )

  pop.colorMathAttrsPush.valid := True
  pop.colorMathAttrsPush.payload := pop.colorMathAttrsPush.payload.getZero
  //--------
  def colorMathPalCntWidth = params.numColsInBgPalPow + 1
  val rColorMathPalCnt = Reg(UInt(colorMathPalCntWidth bits)) init(0x0)
  val rColorMathPalEntry = Reg(Gpu2dPalEntry(params=params))
  rColorMathPalEntry.init(rColorMathPalEntry.getZero)
  val rColorMathPalEntryPushValid = Reg(Bool()) init(True)

  //pop.colorMathPalEntryPush.valid := True
  pop.colorMathPalEntryPush.valid := rColorMathPalEntryPushValid
  pop.colorMathPalEntryPush.payload.bgPalEntry := rColorMathPalEntry
  //pop.colorMathPalEntryPush.payload.memIdx := 1
  pop.colorMathPalEntryPush.payload.memIdx := rColorMathPalCnt.resized

  //otherwise {
  //}
  palPush(
    numColsInPal=params.numColsInBgPal,
    rPalCnt=rColorMathPalCnt,
    rPalEntry=rColorMathPalEntry,
    rPalEntryPushValid=rColorMathPalEntryPushValid,
    palPushFire=pop.bgPalEntryPush.fire,
  )
  //pop.colorMathPalEntryPush.valid := False
  //pop.colorMathPalEntryPush.payload := (
  //  pop.colorMathPalEntryPush.payload.getZero
  //)
  //--------
  //val tempBgTile = Gpu2dTileFull(
  //  params=params,
  //  isObj=false,
  //  isAffine=false,
  //)
  val tempBgTileSlice = Gpu2dTileSlice(
    params=params,
    isObj=false,
    isAffine=false,
  )

  val nextBgTileCnt = SInt(
    //params.numBgTilesPow + 2 bits
    params.bgTileMemIdxWidth + 5 bits
  )
  val rBgTileCnt = RegNext(nextBgTileCnt) init(-1)
  val rBgTilePushValid = Reg(Bool()) init(True)
  def mkBgTile(
    colIdx0: Int,
    colIdx1: Int,
    colIdx2: Option[Int]=None,
    colIdx3: Option[Int]=None,
  ): Unit = {
    //mkTile(
    //  //tempTile=tempBgTile,
    //  colIdx0=colIdx0,
    //  colIdx1=colIdx1,
    //  colIdx2=colIdx2,
    //  colIdx3=colIdx3,
    //)
    mkTile(
      //tempTile=tempColorMathTile,
      tempTileSlice=tempBgTileSlice,
      pxCoordXStart={
        //rBgTileCnt(params.bgTileSize2dPow.x - 1 downto 0).asUInt
        //0
        def tempWidthPow = params.bgTileSize2dPow.x
        U(f"$tempWidthPow'd0")
      },
      pxCoordY=(
        rBgTileCnt(
          params.bgTileSize2dPow.y - 1
          //downto params.bgTileSize2dPow.x
          downto 0
        ).asUInt
      ),
      palEntryMemIdxWidth=params.bgPalEntryMemIdxWidth,
      colIdx0=colIdx0,
      colIdx1=colIdx1,
      colIdx2=colIdx2,
      colIdx3=colIdx3,
    )
  }

  def tempBgTileCnt = (
    rBgTileCnt
    >> params.bgTileSize2dPow.y
  )
  tempBgTileSlice := tempBgTileSlice.getZero
  tempBgTileSlice.allowOverride

  when (
    //rBgTileCnt < params.numBgTiles
    tempBgTileCnt < params.numBgTiles
    //rBgTileCnt < 
  ) {
    when (pop.bgTilePush.fire) {
      when (tempBgTileCnt === 0) {
        //mkBgTile(0, 1)
        mkBgTile(0, 0)
      } elsewhen (tempBgTileCnt === 1) {
        //mkBgTile(1, 2)
        mkBgTile(1, 2, Some(3), Some(4))
        //mkBgTile(1, 1)
        //mkBgTile(3, 3)
        //mkBgTile(2, 3)
      } elsewhen (tempBgTileCnt === 2) {
        //mkBgTile(2, 3)
        //mkBgTile(3, 4)
        mkBgTile(2, 2)
        //mkBgTile(2, 2)
      } elsewhen (tempBgTileCnt === 3) {
        //mkBgTile(3, 4)
        mkBgTile(3, 3)
        //mkBgTile(0, 1)
      } elsewhen (tempBgTileCnt === 4) {
        //mkBgTile(4, 5)
        mkBgTile(4, 4)
      } elsewhen (
        tempBgTileCnt
        === (
          //params.bgSize2dInTiles.x / params.bgTileSize2d.x
          //params.bgSize2dInPxs.x
          params.intnlFbSize2d.x
          / (
            params.bgTileSize2d.x * params.bgTileSize2d.y
          )
        )
      ) {
        // test that there's a tile at the second row
        //mkBgTile(1, 2, Some(3), Some(4))
        mkBgTile(1, 1)
      } elsewhen (
        tempBgTileCnt
        === (
          //params.bgSize2dInTiles.x / params.bgTileSize2d.x
          (
            //params.bgSize2dInPxs.x
            params.intnlFbSize2d.x
            / (
              params.bgTileSize2d.x * params.bgTileSize2d.y
            )
          ) + 1
        )
      ) {
        // test that there's a tile at the second row
        //mkBgTile(1, 2, Some(3), Some(4))
        mkBgTile(2, 2)
      } otherwise {
        //tempBgTileSlice := tempBgTileSlice.getZero
        //when (rBgTileCnt >= params.numBgTiles) {
        //  rBgTilePushValid := False
        //}
      }
      nextBgTileCnt := rBgTileCnt + 1
    } otherwise {
      //tempBgTileSlice := tempBgTileSlice.getZero
      nextBgTileCnt := rBgTileCnt
    }
  } otherwise {
    //tempBgTileSlice := tempBgTileSlice.getZero
    nextBgTileCnt := rBgTileCnt
  }
  when (tempBgTileCnt >= params.numBgTiles - 1) {
    rBgTilePushValid := False
  }

  pop.bgTilePush.valid := rBgTilePushValid
  pop.bgTilePush.payload.tileSlice := tempBgTileSlice
  pop.bgTilePush.payload.memIdx := (
    rBgTileCnt.asUInt(pop.bgTilePush.payload.memIdx.bitsRange)
  )
  ////--------
  //pop.bgTilePush.valid := rBgTilePushValid
  ////--------
  ////pop.bgTilePush.valid := True
  //// when (!rBgTileCnt.msb) {
  //  pop.bgTilePush.payload.tile := tempBgTileToPush
  //  pop.bgTilePush.payload.memIdx := (
  //    rBgTileCnt.asUInt(params.numBgTilesPow - 1 downto 0)
  //  )
  ////} otherwise {
  ////  pop.bgTilePush.payload.tile := rBgTile
  ////  pop.bgTilePush.payload.memIdx := (
  ////    rBgTileCnt(params.numBgTilesPow - 1 downto 0)
  ////  )
  ////}
  ////pop.bgTilePush.payload.memIdx := params.intnlFbSize2d.x
  //--------
  val tempBgAttrs = Gpu2dBgAttrs(
    params=params,
    isColorMath=false,
  )
  //tempBgAttrs.colorMathInfo := tempBgAttrs.colorMathInfo.getZero
  if (!params.noColorMath) {
    tempBgAttrs.colorMathInfo.doIt := True
    tempBgAttrs.colorMathInfo.kind := (
      Gpu2dColorMathKind.add
      //Gpu2dColorMathKind.sub
      //Gpu2dColorMathKind.avg
      //Gpu2dColorMathKind.avg
    )
  } else {
    //tempBgAttrs.colorMathInfo := tempBgAttrs.colorMathInfo.getZero
  }
  //val tempBgScroll = DualTypeNumVec2(
  //  dataTypeX=SInt(tempBgAttrs.scroll.x.getWidth bits),
  //  dataTypeY=SInt(tempBgAttrs.scroll.y.getWidth bits),
  //)
  //tempBgAttrs.scroll := tempBgAttrs.scroll.getZero
  //tempBgAttrs.scroll.x := 0
  //tempBgAttrs.scroll.x := 1
  tempBgAttrs.scroll.x := (
    //0
    ////2
    //params.bgTileSize2d.x
    + 2
    //+ 1
  )
  //tempBgAttrs.scroll.x := 3
  //tempBgScroll.x := (-params.bgTileSize2d.x) + 1
  //tempBgScroll.x := 1
  //tempBgAttrs.scroll.x := tempBgScroll.x.asUInt
  //tempBgAttrs.scroll.x := 0
  //tempBgAttrs.scroll.x := (default -> True)
  tempBgAttrs.scroll.y := (
    //2
    //0
    1
  )
  //tempBgAttrs.scroll.x := 6
  //tempBgAttrs.scroll.y := 5
  //tempBgAttrs.visib := True
  ////tempBgAttrs.visib := False
  //tempBgAttrs.fbAttrs := tempBgAttrs.fbAttrs.getZero
  tempBgAttrs.fbAttrs.doIt := (
    True
    //False
  )
  tempBgAttrs.fbAttrs.tileMemBaseAddr := 0
  for (idx <- 0 to pop.bgAttrsPushArr.size - 1) {
    def tempBgAttrsPush = pop.bgAttrsPushArr(idx)
    if (idx == 0) {
      tempBgAttrsPush.valid := True
      tempBgAttrsPush.payload.bgAttrs := tempBgAttrs
    } else {
      //tempBgAttrsPush.valid := False
      tempBgAttrsPush.valid := True
      tempBgAttrsPush.payload.bgAttrs := tempBgAttrs.getZero
    }
  }

  val rBgEntryPushValid = Reg(Bool()) init(True)
  val tempBgEntry = Gpu2dBgEntry(
    params=params,
    isColorMath=false,
  )
  //val bgEntryCntWidth = params.numBgsPow + 2
  val bgEntryCntWidth = params.bgEntryMemIdxWidth + 2
  val nextBgEntryCnt = SInt(bgEntryCntWidth bits)
  val rBgEntryCnt = RegNext(nextBgEntryCnt) init(-1)
  //val rBgEntryPushValid = Reg(Bool()) init(True)
  // we're only changing one tile
  //tempBgEntry.tileMemIdx := 1
  //tempBgEntry.dispFlip.x := False
  //tempBgEntry.dispFlip.y := False

  val rBgEntryMemIdx = Reg(SInt((params.bgEntryMemIdxWidth + 1) bits))
    .init((1 << params.bgEntryMemIdxWidth) - 1)

  //for (idx <- 0 to pop.bgEntryPushArr.size - 1) {
  //  val tempBgEntryPush = pop.bgEntryPushArr(idx)
  //  if (idx == 0) {
  //    when (rBgEntryMemIdx === 0) {
  //      tempBgEntryPush.payload.bgEntry := tempBgEntry
  //    } otherwise {
  //      tempBgEntryPush.payload.bgEntry := (
  //        tempBgEntryPush.payload.bgEntry.getZero
  //      )
  //    }
  //    //tempBgEntryPush.payload.memIdx := 0x1
  //    //tempBgEntryPush.payload.memIdx := 0x0
  //    tempBgEntryPush.payload.memIdx := rBgEntryMemIdx.asUInt.resized
  //    when (!rBgEntryMemIdx.msb) {
  //      tempBgEntryPush.valid := True
  //      when (tempBgEntryPush.fire) {
  //        rBgEntryMemIdx := rBgEntryMemIdx  - 1
  //      }
  //    } otherwise {
  //      tempBgEntryPush.valid := False
  //    }
  //  } else {
  //    //tempBgEntryPush.valid := False
  //    tempBgEntryPush.valid := True
  //    tempBgEntryPush.payload.bgEntry := tempBgEntry.getZero
  //    tempBgEntryPush.payload.memIdx := 0x0
  //  }
  //}
  for (idx <- 0 until pop.bgEntryPushArr.size) {
    def tempBgEntryPush = pop.bgEntryPushArr(idx)
    if (idx == 0) {
      when (rBgEntryCnt < (1 << params.bgEntryMemIdxWidth)) {
        when (tempBgEntryPush.fire) {
          //when (rBgEntryCnt === 0) {
          //  //mkBgEntry(0, 1)
          //  //mkBgEntry(0, 0)
          //} elsewhen (rBgEntryCnt === 1) {
          //  //mkBgEntry(1, 2)
          //  //mkBgEntry(1, 1)
          //  //mkBgEntry(3, 3)
          //  //mkBgEntry(2, 3)
          //} elsewhen (rBgEntryCnt === 2) {
          //  //mkBgEntry(2, 3)
          //  //mkBgEntry(3, 4)
          //  //mkBgEntry(2, 2)
          //  //mkBgEntry(2, 2)
          //} elsewhen (rBgEntryCnt === 3) {
          //  //mkBgEntry(3, 4)
          //  //mkBgEntry(3, 3)
          //  //mkBgEntry(0, 1)
          //} elsewhen (rBgEntryCnt === 4) {
          //  //mkBgEntry(4, 5)
          //  //mkBgEntry(4, 4)
          //} otherwise 
          when (rBgEntryCnt < 5) {
            tempBgEntry.tileIdx := rBgEntryCnt.asUInt.resized
            tempBgEntry.dispFlip.x := False
            tempBgEntry.dispFlip.y := False
          } otherwise {
            tempBgEntry := tempBgEntry.getZero
            //when (rBgEntryCnt >= params.numBgEntrys) {
            //  rBgEntryPushValid := False
            //}
          }
          nextBgEntryCnt := rBgEntryCnt + 1
        } otherwise {
          tempBgEntry := tempBgEntry.getZero
          nextBgEntryCnt := rBgEntryCnt
        }
      } otherwise {
        tempBgEntry := tempBgEntry.getZero
        nextBgEntryCnt := rBgEntryCnt
      }
      when (rBgEntryCnt + 1 >= (1 << params.bgEntryMemIdxWidth)) {
        rBgEntryPushValid := False
      }

      tempBgEntryPush.valid := rBgEntryPushValid
      //tempBgEntryPush.payload.bgEntry.tileMemIdx := (
      //  rBgEntryMemIdx.asUInt(
      //    params.bgEntryMemIdxWidth - 1 downto 0
      //  )
      //)
      tempBgEntryPush.payload.bgEntry := tempBgEntry
      tempBgEntryPush.payload.memIdx := (
        rBgEntryCnt.asUInt(pop.bgEntryPushArr(0).payload.memIdx.bitsRange)
      )
    } else {
      tempBgEntryPush.valid := True
      tempBgEntryPush.payload.bgEntry := tempBgEntry.getZero
      tempBgEntryPush.payload.memIdx := 0x0
    }
  }
  //--------
  def bgPalCntWidth = params.numColsInBgPalPow + 1
  val rBgPalCnt = Reg(UInt(bgPalCntWidth bits)) init(0x0)
  val rBgPalEntry = Reg(Gpu2dPalEntry(params=params))
  rBgPalEntry.init(rBgPalEntry.getZero)
  val rBgPalEntryPushValid = Reg(Bool()) init(True)

  //pop.bgPalEntryPush.valid := True
  pop.bgPalEntryPush.valid := rBgPalEntryPushValid
  pop.bgPalEntryPush.payload.bgPalEntry := rBgPalEntry
  //pop.bgPalEntryPush.payload.memIdx := 1
  pop.bgPalEntryPush.payload.memIdx := rBgPalCnt.resized

  //otherwise {
  //}
  palPush(
    numColsInPal=params.numColsInBgPal,
    rPalCnt=rBgPalCnt,
    rPalEntry=rBgPalEntry,
    rPalEntryPushValid=rBgPalEntryPushValid,
    palPushFire=pop.bgPalEntryPush.fire,
  )
  //--------
  val tempObjTileSlice = Gpu2dTileSlice(
    params=params,
    isObj=true,
    isAffine=false,
  )

  val nextObjTileCnt = SInt(
    //params.numObjTilesPow + 2 bits
    params.objTileSliceMemIdxWidth + 5 bits
  )
  val rObjTileCnt = RegNext(nextObjTileCnt) init(-1)
  val rObjTilePushValid = Reg(Bool()) init(True)

  def mkObjTile(
    colIdx0: Int,
    colIdx1: Int,
    colIdx2: Option[Int]=None,
    colIdx3: Option[Int]=None,
  ): Unit = {
    //mkTile(
    //  tempTile=tempObjTile,
    //  colIdx0=colIdx0,
    //  colIdx1=colIdx1,
    //  colIdx2=colIdx2,
    //  colIdx3=colIdx3,
    //)
    mkTile(
      //tempTile=tempColorMathTile,
      tempTileSlice=tempObjTileSlice,
      pxCoordXStart={
        if (params.objTileWidthRshift == 0) {
          def tempWidthPow = params.objTileSize2dPow.x
          U(f"$tempWidthPow'd0")
        } else {
          //rObjTileCnt(
          //  //params.objTileSize2dPow.x - 1
          //  ////downto 0
          //  ////downto params.objSliceTileWidthPow
          //  //downto params.objTileWidthRshift
          //  //params.objTileWidthRshift - 1 downto 0
          //  params.objSliceTileWidthPow - 1 downto 0
          //).asUInt
          //Cat(
          //  rObjTileCnt(
          //    params.objTileWidthRshift - 1 downto 0
          //  ),
          //  B(
          //    params.objSliceTileWidthPow bits,
          //    default -> False,
          //  )
          //).asUInt
          rObjTileCnt(
            //params.objTileWidthRshift - 1 downto 0
            params.objTileSize2dPow.x - 1
            downto params.objTileWidthRshift
          ).asUInt
        }
      },
      pxCoordY=(
        rObjTileCnt(
          (
            params.objTileSize2dPow.y
            //+ params.objSliceTileWidthPow
            + params.objTileWidthRshift
            - 1
          )
          downto (
            //params.objTileSize2dPow.x
            //params.objSliceTileWidthPow
            params.objTileWidthRshift
          )
        ).asUInt
      ),
      palEntryMemIdxWidth=params.objPalEntryMemIdxWidth,
      colIdx0=colIdx0,
      colIdx1=colIdx1,
      colIdx2=colIdx2,
      colIdx3=colIdx3,
    )
  }
  def tempObjTileCnt = (
    rObjTileCnt(
      rObjTileCnt.high
      downto (
        params.objTileSize2dPow.y
        //+ params.objSliceTileWidthPow
        + params.objTileWidthRshift
      )
    )
    //>> (
    //  params.objTileSize2dPow.y
    //  + params.objSliceTileWidthPow
    //  //+ params.objTileWidthRshift
    //)
  )
  tempObjTileSlice := tempObjTileSlice.getZero
  tempObjTileSlice.allowOverride

  when (tempObjTileCnt < params.numObjTiles) {
    when (pop.objTilePush.fire) {
      //when (tempObjTileCnt === 0) {
        //mkObjTile(0, 1)
        mkObjTile(0, 0)
        //tempObjTileSlice := tempObjTileSlice.getZero
      //} elsewhen (tempObjTileCnt === 1) {
      //  //tempObjTile := tempObjTile.getZero
      //  mkObjTile(1, 2, Some(3), Some(4))
      //  //mkObjTile(1, 1)
      //  //mkObjTile(3, 3)
      //  //mkObjTile(2, 3)
      //} elsewhen (tempObjTileCnt === 2) {
      //  //mkObjTile(2, 3)
      //  //mkObjTile(3, 4)
      //  mkObjTile(2, 2)
      //  //mkObjTile(2, 2)
      //} elsewhen (tempObjTileCnt === 3) {
      //  //mkObjTile(3, 4)
      //  mkObjTile(3, 3)
      //  //mkObjTile(0, 1)
      //} elsewhen (tempObjTileCnt === 4) {
      //  //mkObjTile(4, 5)
      //  mkObjTile(4, 4)
      //} otherwise {
      //  tempObjTileSlice := tempObjTileSlice.getZero
      //  //when (tempObjTileCnt >= params.numObjTiles) {
      //  //  rObjTilePushValid := False
      //  //}
      //}
      nextObjTileCnt := rObjTileCnt + 1
    } otherwise {
      //tempObjTileSlice := tempObjTileSlice.getZero
      nextObjTileCnt := rObjTileCnt
    }
  } otherwise {
    //tempObjTileSlice := tempObjTileSlice.getZero
    nextObjTileCnt := rObjTileCnt
  }
  when (tempObjTileCnt >= params.numObjTiles - 1) {
    rObjTilePushValid := False
  }

  pop.objTilePush.valid := rObjTilePushValid
  pop.objTilePush.payload.tileSlice := tempObjTileSlice
  pop.objTilePush.payload.memIdx := (
    rObjTileCnt.asUInt(pop.objTilePush.payload.memIdx.bitsRange)
  )
  //--------
  def objPalCntWidth = params.numColsInObjPalPow + 1
  val rObjPalCnt = Reg(UInt(objPalCntWidth bits)) init(0x0)
  val rObjPalEntry = Reg(Gpu2dPalEntry(params=params))
  rObjPalEntry.init(rObjPalEntry.getZero)
  val rObjPalEntryPushValid = Reg(Bool()) init(True)

  //pop.objPalEntryPush.valid := True
  pop.objPalEntryPush.valid := rObjPalEntryPushValid
  pop.objPalEntryPush.payload.objPalEntry := rObjPalEntry
  //pop.objPalEntryPush.payload.memIdx := 1
  pop.objPalEntryPush.payload.memIdx := rObjPalCnt.resized

  //otherwise {
  //}
  palPush(
    numColsInPal=params.numColsInObjPal,
    rPalCnt=rObjPalCnt,
    rPalEntry=rObjPalEntry,
    rPalEntryPushValid=rObjPalEntryPushValid,
    palPushFire=pop.objPalEntryPush.fire,
  )


  //pop.objTilePush.valid := True
  ////pop.objTilePush.payload := pop.objTilePush.payload.getZero

  ////pop.objAttrsPush.valid := True
  ////pop.objAttrsPush.payload := pop.objAttrsPush.payload.getZero
  //pop.objPalEntryPush.valid := True
  //pop.objPalEntryPush.payload := pop.objPalEntryPush.payload.getZero


  val tempObjAttrs = Gpu2dObjAttrs(
    params=params,
    isAffine=false,
  )
  val objAttrsCntWidth = params.numObjsPow + 2
  //val rObjAttrsCnt = Reg(UInt(objAttrsCntWidth bits)) init(0x0)
  //val nextObjAttrsCnt = UInt(objAttrsCntWidth bits)
  //val rObjAttrsCnt = RegNext(nextObjAttrsCnt) init(0x0)
  val nextObjAttrsCnt = SInt(objAttrsCntWidth bits)
  val rObjAttrsCnt = RegNext(nextObjAttrsCnt) init(-1)
  //val rObjAttrs = Reg(Gpu2dObjAttrs(params=params))
  //rObjAttrs.init(rObjAttrs.getZero)
  val rObjAttrsEntryPushValid = Reg(Bool()) init(True)
  if (!params.noColorMath) {
    tempObjAttrs.colorMathInfo := tempObjAttrs.colorMathInfo.getZero
  }

  when (rObjAttrsCnt < params.numObjs) {
    when (
      rObjAttrsCnt
      ===
      //1 //0
      0
    ) {
      tempObjAttrs.tileIdx := 1
      //tempObjAttrs.tileMemIdx := 2
      //tempObjAttrs.pos.x := 16
      //tempObjAttrs.pos.x := -1
      //tempObjAttrs.pos.x := 0
      //tempObjAttrs.pos.x := 1
      //tempObjAttrs.pos.x := 6
      //tempObjAttrs.pos.x := 7
      tempObjAttrs.pos.x := (
        //params.intnlFbSize2d.x - params.objTileSize2d.x //- 1
        //params.intnlFbSize2d.x - params.objTileSize2d.x - 5
        //params.intnlFbSize2d.x >> 1
        //0x3e
        //3
        //2
        //1
        //0
        7
        //8
      )
      //tempObjAttrs.pos.x := -1
      //tempObjAttrs.pos.x := 8
      //tempObjAttrs.pos.x := 3
      tempObjAttrs.pos.y := 8
      //tempObjAttrs.pos.y := 0
      //tempObjAttrs.prio := 0
      tempObjAttrs.prio := (
        1
        //0
      )
      tempObjAttrs.size2d.x := params.objTileSize2d.x
      tempObjAttrs.size2d.y := params.objTileSize2d.y
      //tempObjAttrs.size2d.y := params.objTileSize2d.y - 1
      tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //tempObjAttrs.affine := tempObjAttrs.affine.getZero
      //tempObjAttrs.affine.doIt := True
      //tempObjAttrs.affine.mat(0)(0) := (
      //  //1 << (tempObjAttrs.affine.fracWidth - 1)
      //  //2 << tempObjAttrs.affine.fracWidth
      //  (1 << Gpu2dAffine.fracWidth)
      //  //| (1 << (Gpu2dAffine.fracWidth - 1))
      //  | (1 << (Gpu2dAffine.fracWidth - 2))
      //)
      //tempObjAttrs.affine.mat(0)(1) := 0
      //tempObjAttrs.affine.mat(1)(0) := 0
      //tempObjAttrs.affine.mat(1)(1) := (
      //  //1 << (tempObjAttrs.affine.fracWidth - 1)
      //  //2 << tempObjAttrs.affine.fracWidth
      //  (1 << Gpu2dAffine.fracWidth)
      //  //| (1 << (Gpu2dAffine.fracWidth - 1))
      //  | (1 << (Gpu2dAffine.fracWidth - 2))
      //)
      //tempObjAttrs := tempObjAttrs.getZero
    } elsewhen (
      rObjAttrsCnt
      ===
      1
      //2
    ) {
      //tempObjAttrs.tileMemIdx := 1
      tempObjAttrs.tileIdx := (
        //2
        3
      )
      //tempObjAttrs.tileMemIdx := 0
      //tempObjAttrs.pos.x := 1
      //tempObjAttrs.pos.x := 16
      //tempObjAttrs.pos.x := 2
      //tempObjAttrs.pos.x := 16
      tempObjAttrs.pos.x := (
        1
        //2
      )
      //tempObjAttrs.pos.x := 9
      //tempObjAttrs.pos.x := 9
      //tempObjAttrs.pos.y := -1
      //tempObjAttrs.pos.y := 8
      tempObjAttrs.pos.y := 9
      tempObjAttrs.prio := (
        //0
        1
      )
      tempObjAttrs.size2d.x := params.objTileSize2d.x
      tempObjAttrs.size2d.y := params.objTileSize2d.y
      tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //tempObjAttrs.affine := tempObjAttrs.affine.getZero
      //tempObjAttrs.affine.doIt := True
      //tempObjAttrs.affine.mat(0)(0) := (
      //  //1 << (tempObjAttrs.affine.fracWidth - 1)
      //  //2 << tempObjAttrs.affine.fracWidth
      //  //(1 << Gpu2dAffine.fracWidth)
      //  //| 
      //  (1 << (Gpu2dAffine.fracWidth - 1))
      //  //| (1 << (Gpu2dAffine.fracWidth - 2))
      //)
      //tempObjAttrs.affine.mat(0)(1) := 0
      //tempObjAttrs.affine.mat(1)(0) := 0
      //tempObjAttrs.affine.mat(1)(1) := (
      //  //1 << (tempObjAttrs.affine.fracWidth - 1)
      //  //2 << tempObjAttrs.affine.fracWidth
      //  //(1 << Gpu2dAffine.fracWidth)
      //  //| 
      //  (1 << (Gpu2dAffine.fracWidth - 1))
      //  //| (1 << (Gpu2dAffine.fracWidth - 2))
      //)
      //tempObjAttrs := tempObjAttrs.getZero
    } elsewhen (rObjAttrsCnt === 2) {
      tempObjAttrs.tileIdx := (
        //3
        2
      )
      tempObjAttrs.pos.x := 2
      //tempObjAttrs.pos.x := 7
      tempObjAttrs.pos.y := 8
      tempObjAttrs.prio := (
        //0
        1
      )
      tempObjAttrs.size2d.x := params.objTileSize2d.x
      tempObjAttrs.size2d.y := params.objTileSize2d.y
      tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //tempObjAttrs.affine := tempObjAttrs.affine.getZero
      //tempObjAttrs := tempObjAttrs.getZero
    ////} elsewhen (rObjAttrsCnt === 3) {
    ////  //tempObjAttrs.tileMemIdx := 0
    ////  //tempObjAttrs.pos.x := 8
    ////  //tempObjAttrs.pos.y := 0 //+ params.objTileSize2d.y - 1
    ////  //tempObjAttrs.prio := 0
    ////  //tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
    ////  tempObjAttrs := tempObjAttrs.getZero
    } otherwise {
      //tempObjAttrs := tempObjAttrs.getZero
      tempObjAttrs.tileIdx := 0
      tempObjAttrs.pos.x := -params.objTileSize2d.x
      //tempObjAttrs.pos.x := 16
      tempObjAttrs.pos.y := 0
      tempObjAttrs.prio := (
        0
        //1
      )
      tempObjAttrs.size2d.x := params.objTileSize2d.x
      tempObjAttrs.size2d.y := params.objTileSize2d.y
      tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //tempObjAttrs.affine := tempObjAttrs.affine.getZero
      when (nextObjAttrsCnt >= params.numObjs) {
        rObjAttrsEntryPushValid := False
      }
      //rObjAttrsEntryPushValid := False
    }
    when (pop.objAttrsPush.fire) {
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

  pop.objAttrsPush.valid := rObjAttrsEntryPushValid
  //pop.objAttrsPush.payload.objAttrs := (
  //  Gpu2dObjAttrs(params=params).getZero
  //)
  pop.objAttrsPush.payload.objAttrs := tempObjAttrs
  pop.objAttrsPush.payload.memIdx := (
    rObjAttrsCnt.asUInt(params.objAttrsMemIdxWidth - 1 downto 0)
  )
  //--------
  val tempObjAffineTileSlice = Gpu2dTileSlice(
    params=params,
    isObj=true,
    isAffine=true,
  )

  val nextObjAffineTileCnt = SInt(
    //params.numObjAffineTilesPow + 2 bits
    //params.objAffineTilePxMemIdxWidth + 5 bits
    (
      params.objAffineTilePxMemIdxWidth
      //+ params.objAffineSliceTileWidthPow
      + 5
    ) bits
  )
  val rObjAffineTileCnt = RegNext(nextObjAffineTileCnt) init(-1)
  val rObjAffineTilePushValid = Reg(Bool()) init(True)

  def mkObjAffineTile(
    colIdx0: Int,
    colIdx1: Int,
    colIdx2: Option[Int]=None,
    colIdx3: Option[Int]=None,
  ): Unit = {
    //mkTile(
    //  tempTile=tempObjAffineTile,
    //  colIdx0=colIdx0,
    //  colIdx1=colIdx1,
    //  colIdx2=colIdx2,
    //  colIdx3=colIdx3,
    //)
    mkTile(
      //tempTile=tempColorMathTile,
      tempTileSlice=tempObjAffineTileSlice,
      pxCoordXStart={
        if (params.objAffineTileWidthRshift == 0) {
          def tempWidthPow = params.objAffineTileSize2dPow.x
          U(f"$tempWidthPow'd0")
        } else {
          def tempWidthPow = params.objAffineSliceTileWidthPow
          U(f"$tempWidthPow'd0")
          //rObjAffineTileCnt(
          //  //params.objAffineTileSize2dPow.x - 1
          //  ////downto params.objAffineSliceTileWidthPow
          //  //downto params.objAffineTileWidthRshift
          //  ////params.objAffineSliceTileWidthPow - 1 downto 0
          //  ////params.objAffineTileWidthRshift - 1 downto 0
          //  //params.objAffineTileSize2dPow.x - 1 downto 0
          //  params.objAffineSliceTileWidthPow - 1 downto 0
          //  //params.objAffineTileSize2dPow.x - 1
          //  //downto params.objAffineTileWidthRshift
          //).asUInt
        }
      },
      pxCoordY=(
        //rObjAffineTileCnt(
        //  params.objAffineTileSize2dPow.y - 1
        //  downto params.objAffineTileSize2dPow.x
        //).asUInt
        rObjAffineTileCnt(
          (
            params.objAffineTileSize2dPow.y
            ////+ params.objAffineSliceTileWidthPow
            //+ params.objAffineTileWidthRshift
            + params.objAffineTileSize2dPow.x
            - 1
          )
          downto (
            //params.objAffineTileSize2dPow.x
            //params.objAffineSliceTileWidthPow
            //params.objAffineTileWidthRshift
            params.objAffineTileSize2dPow.x
          )
        ).asUInt
      ),
      palEntryMemIdxWidth=params.objPalEntryMemIdxWidth,
      colIdx0=colIdx0,
      colIdx1=colIdx1,
      colIdx2=colIdx2,
      colIdx3=colIdx3,
      //onePx=true,
    )
  }
  def tempObjAffineTileCnt = (
    rObjAffineTileCnt(
      rObjAffineTileCnt.high
      downto (
        params.objAffineTileSize2dPow.y
        //+ params.objAffineSliceTileWidthPow
        //+ params.objAffineTileWidthRshift
        + params.objAffineTileSize2dPow.x
      )
    )
    //>> (
    //  params.objAffineTileSize2dPow.y
    //  + params.objAffineSliceTileWidthPow
    //  //+ params.objAffineTileWidthRshift
    //)
  )
  tempObjAffineTileSlice := tempObjAffineTileSlice.getZero
  tempObjAffineTileSlice.allowOverride

  when (tempObjAffineTileCnt < params.numObjAffineTiles) {
    when (pop.objAffineTilePush.fire) {
      when (tempObjAffineTileCnt === 0) {
        //mkObjAffineTile(0, 1)
        mkObjAffineTile(0, 0)
      } elsewhen (tempObjAffineTileCnt === 1) {
        //mkObjAffineTile(1, 2, Some(3), Some(4))
        mkObjAffineTile(1, 2, Some(3), Some(4))
        //mkObjAffineTile(1, 1, Some(3), Some(4))
        //mkObjAffineTile(1, 1)
        //mkObjAffineTile(3, 3)
        //mkObjAffineTile(2, 2)
      } elsewhen (tempObjAffineTileCnt === 2) {
        //mkObjAffineTile(2, 2)
        //mkObjAffineTile(2, 3)
        //mkObjAffineTile(3, 4)
        mkObjAffineTile(1, 2, Some(3), Some(4))
        //mkObjAffineTile(3, 3)
        //mkObjAffineTile(2, 2)
      } elsewhen (tempObjAffineTileCnt === 3) {
        //mkObjAffineTile(3, 4)
        mkObjAffineTile(3, 3)
        //mkObjAffineTile(0, 1)
      } elsewhen (tempObjAffineTileCnt === 4) {
        //mkObjAffineTile(4, 5)
        mkObjAffineTile(4, 4)
      } otherwise {
        //tempObjAffineTileSlice := tempObjAffineTileSlice.getZero
        //when (tempObjAffineTileCnt >= params.numObjAffineTiles) {
        //  rObjAffineTilePushValid := False
        //}
      }
      nextObjAffineTileCnt := rObjAffineTileCnt + 1
    } otherwise {
      //tempObjAffineTileSlice := tempObjAffineTileSlice.getZero
      nextObjAffineTileCnt := rObjAffineTileCnt
    }
  } otherwise {
    //tempObjAffineTileSlice := tempObjAffineTileSlice.getZero
    nextObjAffineTileCnt := rObjAffineTileCnt
  }
  when (tempObjAffineTileCnt >= params.numObjAffineTiles - 1) {
    rObjAffineTilePushValid := False
  }

  pop.objAffineTilePush.valid := rObjAffineTilePushValid
  //pop.objAffineTilePush.payload.tileSlice := tempObjAffineTileSlice
  pop.objAffineTilePush.payload.tilePx := tempObjAffineTileSlice.getPx(
    rObjAffineTileCnt(
      //params.objAffineTileSize2dPow.x - 1
      ////downto 0
      //downto params.objAffineTileWidthRshift
      params.objAffineSliceTileWidthPow - 1 downto 0
    ).asUInt
  )
  pop.objAffineTilePush.payload.memIdx := (
    rObjAffineTileCnt.asUInt(
      //pop.objAffineTilePush.payload.memIdx.bitsRange
      (
        pop.objAffineTilePush.payload.memIdx.getWidth
        //+ params.objAffineTileSize2dPow.x
        - 1
      ) downto 0 //params.objAffineTileSize2dPow.x
    )
  )
  //--------
  val tempObjAffineAttrs = Gpu2dObjAttrs(
    params=params,
    isAffine=true,
  )
  //val objAffineAttrsCntWidth = params.numObjsAffinePow + 2
  val objAffineAttrsCntWidth = params.objAffineTilePxMemIdxWidth + 2
  //val rObjAffineAttrsCnt = Reg(UInt(objAffineAttrsCntWidth bits)) init(0x0)
  //val nextObjAffineAttrsCnt = UInt(objAffineAttrsCntWidth bits)
  //val rObjAffineAttrsCnt = RegNext(nextObjAffineAttrsCnt) init(0x0)
  val nextObjAffineAttrsCnt = SInt(objAffineAttrsCntWidth bits)
  val rObjAffineAttrsCnt = RegNext(nextObjAffineAttrsCnt) init(-1)
  //val rObjAffineAttrs = Reg(Gpu2dObjAffineAttrs(params=params))
  //rObjAffineAttrs.init(rObjAffineAttrs.getZero)
  val rObjAffineAttrsEntryPushValid = Reg(Bool()) init(True)
  if (!params.noColorMath) {
    tempObjAffineAttrs.colorMathInfo := (
      tempObjAffineAttrs.colorMathInfo.getZero
    )
  }

  when (rObjAffineAttrsCnt < params.numObjsAffine) {
    when (rObjAffineAttrsCnt === 0) {
      tempObjAffineAttrs.tileIdx := 1
      //tempObjAffineAttrs.tileMemIdx := 2
      tempObjAffineAttrs.pos.x := (
        //params.intnlFbSize2d.x - params.objAffineTileSize2d.x //- 1
        //params.intnlFbSize2d.x - params.objAffineTileSize2d.x - 5
        //params.intnlFbSize2d.x >> 1
        //0x3e
        //3
        //2
        //1
        //0
        //7
        //40
        48
        //32
        //64
        //- params.objAffineTileSize2d.x
        - (params.objAffineTileSize2d.x / 2)
        //8
      )
      tempObjAffineAttrs.pos.y := (
        //8
        //0
        -params.objAffineTileSize2d.y / 2
      )
      //tempObjAffineAttrs.pos.y := 0
      //tempObjAffineAttrs.prio := 0
      tempObjAffineAttrs.prio := (
        //1
        0
      )
      tempObjAffineAttrs.size2d.x := params.objAffineTileSize2d.x
      tempObjAffineAttrs.size2d.y := params.objAffineTileSize2d.y
      //tempObjAffineAttrs.size2d.y := params.objAffineTileSize2d.y - 1
      tempObjAffineAttrs.dispFlip := tempObjAffineAttrs.dispFlip.getZero
      //tempObjAffineAttrs.affine := tempObjAffineAttrs.affine.getZero
      tempObjAffineAttrs.affine.doIt := True
      tempObjAffineAttrs.affine.mat(0)(0) := (
        //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
        //2 << tempObjAffineAttrs.affine.fracWidth
        (1 << Gpu2dAffine.fracWidth)
        |
        (1 << (Gpu2dAffine.fracWidth - 1))
        ////| (1 << (Gpu2dAffine.fracWidth - 2))
      )
      tempObjAffineAttrs.affine.mat(0)(1) := 0
      tempObjAffineAttrs.affine.mat(1)(0) := 0
      tempObjAffineAttrs.affine.mat(1)(1) := (
        //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
        //2 << tempObjAffineAttrs.affine.fracWidth
        (1 << Gpu2dAffine.fracWidth)
        |
        (1 << (Gpu2dAffine.fracWidth - 1))
        ////| (1 << (Gpu2dAffine.fracWidth - 2))
      )
      //tempObjAffineAttrs.affine.mat(0)(0) := (
      //  //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
      //  //2 << tempObjAffineAttrs.affine.fracWidth
      //  //(1 << Gpu2dAffine.fracWidth)
      //  //| 
      //  //(1 << (Gpu2dAffine.fracWidth - 1))
      //  //| (1 << (Gpu2dAffine.fracWidth - 2))
      //  1
      //) * (255)
      //tempObjAffineAttrs.affine.mat(0)(1) := (
      //  //0
      //  -5
      //)
      //tempObjAffineAttrs.affine.mat(1)(0) := (
      //  //0
      //  5
      //)
      //tempObjAffineAttrs.affine.mat(1)(1) := (
      //  //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
      //  //2 << tempObjAffineAttrs.affine.fracWidth
      //  //(1 << Gpu2dAffine.fracWidth)
      //  //| 
      //  //(1 << (Gpu2dAffine.fracWidth - 1))
      //  //| (1 << (Gpu2dAffine.fracWidth - 2))
      //  1
      //) * (255)
      //tempObjAffineAttrs := tempObjAffineAttrs.getZero
    } elsewhen (rObjAffineAttrsCnt === 1) {
      //tempObjAffineAttrs.tileMemIdx := 1
      tempObjAffineAttrs.tileIdx := 2
      //tempObjAffineAttrs.tileMemIdx := 0
      //tempObjAffineAttrs.pos.x := 1
      //tempObjAffineAttrs.pos.x := 16
      //tempObjAffineAttrs.pos.x := 2
      //tempObjAffineAttrs.pos.x := 16
      tempObjAffineAttrs.pos.x := (
        //1
        //2
        //7
        32
        //48
        - (params.objAffineTileSize2d.x / 2)
        //-params.objAffineDblTileSize2d.x
      )
      //tempObjAffineAttrs.pos.x := 9
      //tempObjAffineAttrs.pos.x := 9
      //tempObjAffineAttrs.pos.y := -1
      //tempObjAffineAttrs.pos.y := 8
      tempObjAffineAttrs.pos.y := (
        //9
        //0
        -params.objAffineTileSize2d.y / 2
      )
      tempObjAffineAttrs.prio := (
        0
        //1
      )
      //tempObjAffineAttrs.size2d.x := params.objAffineTileSize2d.x / 2
      //tempObjAffineAttrs.size2d.y := params.objAffineTileSize2d.y / 2
      tempObjAffineAttrs.size2d.x := params.objAffineTileSize2d.x
      tempObjAffineAttrs.size2d.y := params.objAffineTileSize2d.y
      tempObjAffineAttrs.dispFlip := tempObjAffineAttrs.dispFlip.getZero
      //tempObjAffineAttrs.affine := tempObjAffineAttrs.affine.getZero
      tempObjAffineAttrs.affine.doIt := True
      tempObjAffineAttrs.affine.mat(0)(0) := (
        //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
        //2 << tempObjAffineAttrs.affine.fracWidth
        //(1 << Gpu2dAffine.fracWidth)
        //| 
        //(1 << (Gpu2dAffine.fracWidth - 1))
        //| (1 << (Gpu2dAffine.fracWidth - 2))
        1
      ) * (255)
      tempObjAffineAttrs.affine.mat(0)(1) := (
        //0
        -5
      )
      tempObjAffineAttrs.affine.mat(1)(0) := (
        //0
        5
      )
      tempObjAffineAttrs.affine.mat(1)(1) := (
        //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
        //2 << tempObjAffineAttrs.affine.fracWidth
        //(1 << Gpu2dAffine.fracWidth)
        //| 
        //(1 << (Gpu2dAffine.fracWidth - 1))
        //| (1 << (Gpu2dAffine.fracWidth - 2))
        1
      ) * (255)
      //tempObjAffineAttrs := tempObjAffineAttrs.getZero
    //} elsewhen (rObjAffineAttrsCnt === 2) {
    ////  tempObjAffineAttrs.tileMemIdx := 3
    ////  tempObjAffineAttrs.pos.x := 8
    ////  //tempObjAffineAttrs.pos.x := 7
    ////  tempObjAffineAttrs.pos.y := 8
    ////  tempObjAffineAttrs.prio := (
    ////    0
    ////    //1
    ////  )
    ////  tempObjAffineAttrs.size2d.x := params.objAffineTileSize2d.x
    ////  tempObjAffineAttrs.size2d.y := params.objAffineTileSize2d.y
    ////  tempObjAffineAttrs.dispFlip := tempObjAffineAttrs.dispFlip.getZero
    ////  tempObjAffineAttrs.affine := tempObjAffineAttrs.affine.getZero
    ////  //tempObjAffineAttrs := tempObjAffineAttrs.getZero
    //////} elsewhen (rObjAffineAttrsCnt === 3) {
    //////  //tempObjAffineAttrs.tileMemIdx := 0
    //////  //tempObjAffineAttrs.pos.x := 8
    //////  //tempObjAffineAttrs.pos.y := 0 //+ params.objAffineTileSize2d.y - 1
    //////  //tempObjAffineAttrs.prio := 0
    //////  //tempObjAffineAttrs.dispFlip := tempObjAffineAttrs.dispFlip.getZero
    //////  tempObjAffineAttrs := tempObjAffineAttrs.getZero
    } otherwise {
      //tempObjAffineAttrs := tempObjAffineAttrs.getZero
      tempObjAffineAttrs.tileIdx := 0
      tempObjAffineAttrs.pos.x := -params.objAffineTileSize2d.x
      tempObjAffineAttrs.pos.y := 0
      tempObjAffineAttrs.prio := (
        0
        //1
      )
      tempObjAffineAttrs.size2d.x := params.objAffineTileSize2d.x
      tempObjAffineAttrs.size2d.y := params.objAffineTileSize2d.y
      tempObjAffineAttrs.dispFlip := tempObjAffineAttrs.dispFlip.getZero
      tempObjAffineAttrs.affine := tempObjAffineAttrs.affine.getZero
      when (nextObjAffineAttrsCnt >= params.numObjsAffine) {
        rObjAffineAttrsEntryPushValid := False
      }
      //rObjAffineAttrsEntryPushValid := False
    }
    when (pop.objAffineAttrsPush.fire) {
      nextObjAffineAttrsCnt := rObjAffineAttrsCnt + 1
    } otherwise {
      nextObjAffineAttrsCnt := rObjAffineAttrsCnt
    }
    //} otherwise {
    //  tempObjAffineAttrs := tempObjAffineAttrs.getZero
    //  nextObjAffineAttrsCnt := rObjAffineAttrsCnt
    //}
  } otherwise {
    tempObjAffineAttrs := tempObjAffineAttrs.getZero
    nextObjAffineAttrsCnt := rObjAffineAttrsCnt
  }

  pop.objAffineAttrsPush.valid := rObjAffineAttrsEntryPushValid
  //pop.objAffineAttrsPush.payload.objAffineAttrs := (
  //  Gpu2dObjAffineAttrs(params=params).getZero
  //)
  pop.objAffineAttrsPush.payload.objAttrs := tempObjAffineAttrs
  pop.objAffineAttrsPush.payload.memIdx := (
    rObjAffineAttrsCnt.asUInt(params.objAffineAttrsMemIdxWidth - 1 downto 0)
  )

  //--------
}
