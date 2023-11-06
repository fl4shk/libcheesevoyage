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
  val tempBgTile = Gpu2dTile(params=params, isObj=false)
  //val tempBgTileRow0Vec = (
  //  tempBgTile.colIdxRowVec(0).subdivideIn(
  //    params.bgTileSize2d.x slices
  //  )
  //)
  //tempBgTileRow0Vec(0) := 1
  //for (idx <- 0 to tempBgTile.colIdxRowVec.size - 1) {
  //  tempBgTile.colIdxRowVec(0)(
  //    params.bgPalEntryMemIdxWidth - 1 downto 0
  //  ) := 1
  //  tempBgTile
  //}
  for (jdx <- 0 to tempBgTile.pxsSize2d.y - 1) {
    for (idx <- 0 to tempBgTile.pxsSize2d.x - 1) {
      def pxCoord = ElabVec2[Int](idx, jdx)
      //// checkerboard pattern
      //if (jdx % 2 == 0) {
      //  tempBgTile.setPx(
      //    pxCoord=pxCoord,
      //    colIdx=(idx % 2) + 1,
      //  )
      //} else { // if (jdx % 2 == 1)
      //  tempBgTile.setPx(
      //    pxCoord=pxCoord,
      //    colIdx=((idx + 1) % 2) + 1,
      //  )
      //}
      tempBgTile.setPx(
        pxCoord=pxCoord,
        //colIdx=(jdx % 2) + 1,
        //colIdx=(idx % 2) + 1,
        //colIdx=(idx % 4) + 1,
        colIdx=(
          (idx % 4) >> log2Up(2),
        )
      )

      //if (jdx % 4 == 0) {
      //} else if (jdx % 4 == 1) {
      //} else if (jdx % 4 == 2) {
      //}
      //if (idx == 0) {
      //  tempBgTile.setPx(
      //    pxCoord=pxCoord,
      //    colIdx=(jdx % 4) + 1,
      //  )
      //} else if (idx + 1 < tempBgTile.pxsSize2d.x) {
      //  tempBgTile.setPx(
      //    pxCoord=pxCoord,
      //    colIdx=jdx % 2,
      //  )
      //} else {
      //  tempBgTile.setPx(
      //    pxCoord=pxCoord,
      //    colIdx=(jdx % 4) + 2
      //  )
      //}

      //tempBgTile.setPx(
      //  pxCoord=pxCoord,
      //  colIdx=1,
      //  //colIdx=0,
      //)
    }
  }
  //tempBgTile.colIdxRowVec.assignFromBits(
  //  //tempBgTile.colIdxRowVec.getZero.asBits
  //)

  //val rBgTileCnt = Reg(UInt(params.numBgTilesPow + 2 bits)) init(-1)
  val nextBgTileCnt = SInt(params.numBgTilesPow + 2 bits)
  val rBgTileCnt = RegNext(nextBgTileCnt) init(-1)
  //val rBgTile = Reg(cloneOf(tempBgTile)) init(tempBgTile.getZero)
  val rBgTilePushValid = Reg(Bool()) init(True)
  val tempBgTileToPush = cloneOf(tempBgTile)

  //when (!rBgTileCnt.msb)
  when (rBgTileCnt < params.numBgTiles) {
    //when (rBgTileCnt + 1 === 1) {
    //  //pop.bgTilePush.payload.tile := tempBgTile.getZero
    //  tempBgTile := tempBgTile
    //} otherwise {
    //  //pop.bgTilePush.payload.tile := tempBgTile.getZero
    //  rBgTile := rBgTile.getZero
    //}
    when (rBgTileCnt === 1) {
      tempBgTileToPush := tempBgTile
    } otherwise {
      tempBgTileToPush := tempBgTileToPush.getZero
      when (nextBgTileCnt >= params.numBgTiles) {
        rBgTilePushValid := False
      }
    }
    when (pop.bgTilePush.fire) {
      nextBgTileCnt := rBgTileCnt + 1
    } otherwise {
      nextBgTileCnt := rBgTileCnt
    }
  } otherwise {
    tempBgTileToPush := tempBgTileToPush.getZero
    nextBgTileCnt := rBgTileCnt
  }
  //when (rBgTileCnt + 1 >= params.numBgTiles) {
  //  rBgTilePushValid := False
  //}
  //pop.bgTilePush.payload.memIdx := 1
  //--------
  pop.bgTilePush.valid := rBgTilePushValid
  //--------
  //pop.bgTilePush.valid := True
  // when (!rBgTileCnt.msb) {
    pop.bgTilePush.payload.tile := tempBgTileToPush
    pop.bgTilePush.payload.memIdx := (
      rBgTileCnt.asUInt(params.numBgTilesPow - 1 downto 0)
    )
  //} otherwise {
  //  pop.bgTilePush.payload.tile := rBgTile
  //  pop.bgTilePush.payload.memIdx := (
  //    rBgTileCnt(params.numBgTilesPow - 1 downto 0)
  //  )
  //}
  //pop.bgTilePush.payload.memIdx := params.intnlFbSize2d.x
  //--------
  val tempBgAttrs = Gpu2dBgAttrs(params=params)
  val tempBgScroll = DualTypeNumVec2(
    dataTypeX=SInt(tempBgAttrs.scroll.x.getWidth bits),
    dataTypeY=SInt(tempBgAttrs.scroll.y.getWidth bits),
  )
  //tempBgAttrs.scroll := tempBgAttrs.scroll.getZero
  //tempBgAttrs.scroll.x := 0
  //tempBgAttrs.scroll.x := 1
  tempBgAttrs.scroll.x := (
    0
    //2
  )
  //tempBgAttrs.scroll.x := 3
  //tempBgScroll.x := (-params.bgTileSize2d.x) + 1
  //tempBgScroll.x := 1
  //tempBgAttrs.scroll.x := tempBgScroll.x.asUInt
  //tempBgAttrs.scroll.x := 0
  //tempBgAttrs.scroll.x := (default -> True)
  //tempBgAttrs.scroll.y := 2
  //tempBgAttrs.scroll.y := 0
  tempBgAttrs.scroll.y := 1
  //tempBgAttrs.scroll.x := 6
  //tempBgAttrs.scroll.y := 5
  tempBgAttrs.visib := True
  //tempBgAttrs.visib := False
  for (idx <- 0 to pop.bgAttrsPushArr.size - 1) {
    val tempBgAttrsPush = pop.bgAttrsPushArr(idx)
    if (idx == 0) {
      tempBgAttrsPush.valid := True
      tempBgAttrsPush.payload.bgAttrs := tempBgAttrs
    } else {
      //tempBgAttrsPush.valid := False
      tempBgAttrsPush.valid := True
      tempBgAttrsPush.payload.bgAttrs := tempBgAttrs.getZero
    }
  }

  val tempBgEntry = Gpu2dBgEntry(params=params)
  // we're only changing one tile
  tempBgEntry.tileMemIdx := 1
  tempBgEntry.dispFlip.x := False
  tempBgEntry.dispFlip.y := False

  val rBgEntryMemIdx = Reg(SInt((params.bgEntryMemIdxWidth + 1) bits))
    .init((1 << params.bgEntryMemIdxWidth) - 1)

  for (idx <- 0 to pop.bgEntryPushArr.size - 1) {
    val tempBgEntryPush = pop.bgEntryPushArr(idx)
    if (idx == 0) {
      when (rBgEntryMemIdx === 0) {
        tempBgEntryPush.payload.bgEntry := tempBgEntry
      } otherwise {
        tempBgEntryPush.payload.bgEntry := (
          tempBgEntryPush.payload.bgEntry.getZero
        )
      }
      //tempBgEntryPush.payload.memIdx := 0x1
      //tempBgEntryPush.payload.memIdx := 0x0
      tempBgEntryPush.payload.memIdx := rBgEntryMemIdx.asUInt.resized
      when (!rBgEntryMemIdx.msb) {
        tempBgEntryPush.valid := True
        when (tempBgEntryPush.fire) {
          rBgEntryMemIdx := rBgEntryMemIdx  - 1
        }
      } otherwise {
        tempBgEntryPush.valid := False
      }
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
  val tempObjTile = Gpu2dTile(
    params=params,
    isObj=true,
  )
  def mkObjTile(
    colIdx0: Int,
    colIdx1: Int,
  ): Unit = {
    for (jdx <- 0 to tempObjTile.pxsSize2d.y - 1) {
      for (idx <- 0 to tempObjTile.pxsSize2d.x - 1) {
        def pxCoord = ElabVec2[Int](idx, jdx)
        if (jdx % 2 == 0) {
          tempObjTile.setPx(
            pxCoord=pxCoord,
            //colIdx=(idx % 2) + 1,
            //colIdx=(idx % 2) + colIdx0,
            //colIdx=3,
            colIdx=colIdx0,
          )
        } else { // if (jdx % 2 == 1)
          tempObjTile.setPx(
            pxCoord=pxCoord,
            //colIdx=((idx + 1) % 2) + 1,
            //colIdx=4,
            //colIdx=(idx % 2) + colIdx1,
            colIdx=colIdx1,
          )
        }
      }
    }
  }
  val nextObjTileCnt = SInt(params.numObjTilesPow + 2 bits)
  val rObjTileCnt = RegNext(nextObjTileCnt) init(-1)
  val rObjTilePushValid = Reg(Bool()) init(True)

  when (rObjTileCnt < params.numObjTiles) {
    when (pop.objTilePush.fire) {
      when (rObjTileCnt === 0) {
        //mkObjTile(0, 1)
        mkObjTile(0, 0)
      } elsewhen (rObjTileCnt === 1) {
        //mkObjTile(1, 2)
        mkObjTile(1, 1)
        //mkObjTile(3, 3)
        //mkObjTile(2, 3)
      } elsewhen (rObjTileCnt === 2) {
        //mkObjTile(2, 3)
        //mkObjTile(3, 4)
        mkObjTile(2, 2)
        //mkObjTile(2, 2)
      } elsewhen (rObjTileCnt === 3) {
        //mkObjTile(3, 4)
        mkObjTile(3, 3)
        //mkObjTile(0, 1)
      } elsewhen (rObjTileCnt === 4) {
        //mkObjTile(4, 5)
        mkObjTile(4, 4)
      } otherwise {
        tempObjTile := tempObjTile.getZero
        //when (rObjTileCnt >= params.numObjTiles) {
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
  when (rObjTileCnt + 1 >= params.numObjTiles) {
    rObjTilePushValid := False
  }

  pop.objTilePush.valid := rObjTilePushValid
  pop.objTilePush.payload.tile := tempObjTile
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


  val tempObjAttrs = Gpu2dObjAttrs(params=params)
  val objAttrsCntWidth = params.numObjsPow + 2
  //val rObjAttrsCnt = Reg(UInt(objAttrsCntWidth bits)) init(0x0)
  //val nextObjAttrsCnt = UInt(objAttrsCntWidth bits)
  //val rObjAttrsCnt = RegNext(nextObjAttrsCnt) init(0x0)
  val nextObjAttrsCnt = SInt(objAttrsCntWidth bits)
  val rObjAttrsCnt = RegNext(nextObjAttrsCnt) init(-1)
  //val rObjAttrs = Reg(Gpu2dObjAttrs(params=params))
  //rObjAttrs.init(rObjAttrs.getZero)
  val rObjAttrsEntryPushValid = Reg(Bool()) init(True)

  when (rObjAttrsCnt < params.numObjs) {
    when (rObjAttrsCnt === 0) {
      tempObjAttrs.tileMemIdx := 1
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
        3
        //2
        //1
        //0
      )
      //tempObjAttrs.pos.x := -1
      //tempObjAttrs.pos.x := 8
      //tempObjAttrs.pos.x := 3
      tempObjAttrs.pos.y := 8
      //tempObjAttrs.pos.y := 0
      //tempObjAttrs.prio := 0
      tempObjAttrs.prio := (
        //1
        0
      )
      tempObjAttrs.size2d.x := params.objTileSize2d.x
      tempObjAttrs.size2d.y := params.objTileSize2d.y
      //tempObjAttrs.size2d.y := params.objTileSize2d.y - 1
      tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //tempObjAttrs := tempObjAttrs.getZero
    } elsewhen (rObjAttrsCnt === 1) {
      //tempObjAttrs.tileMemIdx := 1
      tempObjAttrs.tileMemIdx := 2
      //tempObjAttrs.tileMemIdx := 0
      //tempObjAttrs.pos.x := 1
      //tempObjAttrs.pos.x := 16
      //tempObjAttrs.pos.x := 2
      //tempObjAttrs.pos.x := 16
      tempObjAttrs.pos.x := (
        //1
        1
      )
      //tempObjAttrs.pos.x := 9
      //tempObjAttrs.pos.x := 9
      //tempObjAttrs.pos.y := -1
      //tempObjAttrs.pos.y := 8
      tempObjAttrs.pos.y := 9
      tempObjAttrs.prio := (
        0
        //1
      )
      tempObjAttrs.size2d.x := params.objTileSize2d.x
      tempObjAttrs.size2d.y := params.objTileSize2d.y
      tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //tempObjAttrs := tempObjAttrs.getZero
    } elsewhen (rObjAttrsCnt === 2) {
      tempObjAttrs.tileMemIdx := 3
      tempObjAttrs.pos.x := 8
      //tempObjAttrs.pos.x := 7
      tempObjAttrs.pos.y := 8
      tempObjAttrs.prio := (
        0
        //1
      )
      tempObjAttrs.size2d.x := params.objTileSize2d.x
      tempObjAttrs.size2d.y := params.objTileSize2d.y
      tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //tempObjAttrs := tempObjAttrs.getZero
    //} elsewhen (rObjAttrsCnt === 3) {
    //  //tempObjAttrs.tileMemIdx := 0
    //  //tempObjAttrs.pos.x := 8
    //  //tempObjAttrs.pos.y := 0 //+ params.objTileSize2d.y - 1
    //  //tempObjAttrs.prio := 0
    //  //tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
    //  tempObjAttrs := tempObjAttrs.getZero
    } otherwise {
      //tempObjAttrs := tempObjAttrs.getZero
      tempObjAttrs.tileMemIdx := 0
      tempObjAttrs.pos.x := -params.objTileSize2d.x
      tempObjAttrs.pos.y := 0
      tempObjAttrs.prio := (
        0
        //1
      )
      tempObjAttrs.size2d.x := params.objTileSize2d.x
      tempObjAttrs.size2d.y := params.objTileSize2d.y
      tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
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
}
