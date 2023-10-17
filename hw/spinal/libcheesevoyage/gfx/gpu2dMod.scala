package libcheesevoyage.gfx
import libcheesevoyage.general.FifoMiscIo
import libcheesevoyage.general.AsyncReadFifo
import libcheesevoyage.general.FifoIo
import libcheesevoyage.general.Vec2
import libcheesevoyage.general.MkVec2
import libcheesevoyage.general.DualTypeNumVec2
import libcheesevoyage.general.MkDualTypeNumVec2
//import libcheesevoyage.general.MkVec2
import libcheesevoyage.general.ElabVec2
import libcheesevoyage.general.PipeSkidBuf
import libcheesevoyage.general.PipeSkidBufIo
import libcheesevoyage.general.DualPipeStageData
import libcheesevoyage.general.HandleDualPipe
//import libcheesevoyage.general.GenericHandlePipe
//import libcheesevoyage.general.HandleStmPipe
//import libcheesevoyage.general.HandleFlowPipe

//import scala.math._
import spinal.core._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer
import scala.math._

case class Gpu2dParams(
  //vgaTimingInfo: LcvVgaTimingInfo,
  rgbConfig: RgbConfig,       // Bits per RGB channel
                              // (from the GPU's perspective. This may
                              // differ from the physical video signal if
                              // `LcvVideoDitherer` is used.)
  intnlFbSize2d: ElabVec2[Int],  // size of the internal resolution used 
                              // by the 2D GPU
                              // (in PIXELS, NOT tiles, though it is
                              // perhaps a good idea to ensure `fbSize2d`
                              // is integer multiples of `numTiles`, as
                              // that is how I'm used to 2D GPUs working)
  physFbSize2dScalePow: ElabVec2[Int],  // the integer value to logical
                              // left shift `intnlFbSize2d` by to obtain
                              // the physical resolution of the video
                              // signal.
                              // This is used to duplicate generated
                              // pixels so that the generated video
                              // signal can fill the screen.
  bgTileSize2dPow: ElabVec2[Int],  // power of two for width/height of a
                              // background tile 
                              // (in pixels)
  objTileSize2dPow: ElabVec2[Int],  // power of two for width/height of a
                              // sprite tile 
                              // (in pixels)
  numBgTilesPow: Int,         // power of two for total number of
                              // background tiles
                              // (how much memory to reserve for BG tiles)
  numObjTilesPow: Int,        // power of two for total number of
                              // sprite tiles
                              // (how much memory to reserve for sprite
                              // tiles)
  bgSize2dInTilesPow: ElabVec2[Int], // power of two for width/height of a
                              // background
                              // (in number of tiles)
  numBgsPow: Int,             // power of two for the total number of
                              // backgrounds
  numObjsPow: Int,            // power of two for the total number of 
                              // sprites
  //numObjsPerScanline: Int,  // how many sprites to process in one
  //                          // scanline (possibly one per cycle?)
  //numColsInPalPow: Int,       // power of two for how many colors in the
  //                            // palette
  numColsInBgPalPow: Int,     // power of two for how many colors in the
                              // background palette
  numColsInObjPalPow: Int,    // power of two for how many colors in the
                              // sprite palette
  //--------
  bgTileArrRamStyle: String="block",
  objTileArrRamStyle: String="block",
  bgEntryArrRamStyle: String="block",
  //bgAttrsArrRamStyle: String="block",
  objAttrsArrRamStyle: String="block",
  bgPalEntryArrRamStyle: String="block",
  objPalEntryArrRamStyle: String="block",

  //bgLineArrRamStyle: String="block",
  //objLineArrRamStyle: String="block",
  //combineLineArrRamStyle: String="block",
  lineArrRamStyle: String="block"
) {
  //--------
  def bgTileSize2d = ElabVec2[Int](
    x=1 << bgTileSize2dPow.x,
    y=1 << bgTileSize2dPow.y,
  )
  def objTileSize2d = ElabVec2[Int](
    x=1 << objTileSize2dPow.x,
    y=1 << objTileSize2dPow.y,
  )
  def numBgs = 1 << numBgsPow
  //def numTiles = 1 << numTilesPow
  def numBgTiles = 1 << numBgTilesPow
  def numColsInBgPal = 1 << numColsInBgPalPow
  def numObjs = 1 << numObjsPow
  def numObjTiles = 1 << numObjTilesPow
  def numColsInObjPal = 1 << numColsInObjPalPow
  //--------
  //def physToIntnlScalePow = ElabVec2[Int](
  //  x=physFbSize2dScalePow.x,
  //  y=physFbSize2dScalePow.y,
  //)
  def physToIntnlScalePow = physFbSize2dScalePow
  def physFbSize2d = ElabVec2[Int](
    //x=intnlFbSize2d.x * physFbSize2dScale.x,
    //y=intnlFbSize2d.y * physFbSize2dScale.y,
    //x=intnlFbSize2d.x << physFbSize2dScalePow.x,
    //y=intnlFbSize2d.y << physFbSize2dScalePow.y,
    x=intnlFbSize2d.x << physFbSize2dScalePow.x,
    y=intnlFbSize2d.y << physFbSize2dScalePow.y,
  )
  //def fbSize2dInPxs = ElabVec2[Int](
  //  x=intnlFbSize2d.x,
  //  y=intnlFbSize2d.y,
  //)
  def fbSize2dInPxs = intnlFbSize2d
  def physToBgTilesScalePow = ElabVec2[Int](
    x=physToIntnlScalePow.x + bgTileSize2dPow.x,
    y=physToIntnlScalePow.y + bgTileSize2dPow.y,
  )

  def fbSize2dInBgTiles = ElabVec2[Int](
    x=intnlFbSize2d.x >> bgTileSize2dPow.x,
    y=intnlFbSize2d.y >> bgTileSize2dPow.y,
  )

  //def lineMemSize = physFbSize2dScale.y * params.physFbSize2d.x

  //def halfLineMemSize = intnlFbSize2d.x
  //def lineMemSize = intnlFbSize2d.x * 2
  def lineMemSize = intnlFbSize2d.x
  //def lineFifoDepth = lineMemSize * 2 + 1
  def lineFifoDepth = lineMemSize + 1
  //def numLineMems = 2
  //def numLineMems = 1 << physFbSize2dScalePow.y
  //def numLineMems = 4
  //def numLineMems = 2
  //def numLineMemsPerBgObjRenderer = 4
  def numLineMemsPerBgObjRenderer = 2
  //def numLineMems = numBgs
  def wrBgObjStallFifoAmountCanPush = 8
  //--------
  def bgSize2dInTiles = ElabVec2[Int](
    x=1 << bgSize2dInTilesPow.x,
    y=1 << bgSize2dInTilesPow.y,
  )
  def bgSize2dInPxs = ElabVec2[Int](
    x=bgSize2dInTiles.x * bgTileSize2d.x,
    y=bgSize2dInTiles.y * bgTileSize2d.y,
  )
  //--------
  def numPxsPerBgTile = bgTileSize2d.x * bgTileSize2d.y
  def numPxsPerObjTile = objTileSize2d.x * objTileSize2d.y
  def numPxsForAllBgTiles = numBgTiles * numPxsPerBgTile
  def numPxsForAllObjTiles = numObjTiles * numPxsPerObjTile
  //--------
  def numTilesPerBg = bgSize2dInTiles.x * bgSize2dInTiles.y
  def numPxsPerBg = numTilesPerBg * numPxsPerBgTile
  def numPxsForAllBgs = numBgs * numPxsPerBg
  //--------
  //def objAttrsMemIdxWidth = log2Up(numObjs)
  //def objAttrsVecIdxWidth = log2Up(numObjs)
  def objAttrsMemIdxWidth = numObjsPow
  //def bgMemIdxWidth = log2Up(numBgs)
  //def tileIdxWidth = log2Up(numTiles)
  //def tileMemIdxWidth = log2Up(numTiles)
  //def tileMemIdxWidth = numTilesPow
  def bgTileMemIdxWidth = numBgTilesPow
  def objTileMemIdxWidth = numObjTilesPow

  //def tileMemIdxWidth = log2Up(numPxsForAllTiles)
  //def tileMemIdxWidth = log2Up(numTiles)

  //def bgEntryMemIdxWidth = log2Up(numTilesPerBg)
  //def bgAttrsMemIdxWidth = log2Up(numBgs)
  def bgEntryMemIdxWidth = bgSize2dInTilesPow.x + bgSize2dInTilesPow.y

  //def palEntryMemIdxWidth = log2Up(numColsInPal)
  //def palEntryMemIdxWidth = numColsInPalPow
  def bgPalEntryMemIdxWidth = numColsInBgPalPow
  def objPalEntryMemIdxWidth = numColsInObjPalPow
  //def tilePixelIdxWidth = log2Up(numPxsForAllTiles)
  //--------
  def coordT(
    someSize2d: ElabVec2[Int],
  ) = LcvVgaCtrlMiscIo.coordT(fbSize2d=someSize2d)
  def sCoordT(
    someSize2d: ElabVec2[Int],
  ) = LcvVgaCtrlMiscIo.sCoordT(fbSize2d=someSize2d)
  def physCoordT() = coordT(someSize2d=physFbSize2d)
  def physPosInfoT() = LcvVideoPosInfo(someSize2d=physFbSize2d)
  def bgPxsCoordT() = coordT(someSize2d=bgSize2dInPxs)
  def bgPxsPosSliceT() = LcvVideoPosSlice(
    //someWidthOrHeight=bgSize2dInPxs.x
    someSize2d=bgSize2dInPxs
  )
  //def bgPxsPosInfoT() = LcvVideoPosInfo(someSize2d=bgSize2dInPxs)
  def bgTilesCoordT() = coordT(someSize2d=bgSize2dInTiles)
  def bgTilesPosSliceT() = LcvVideoPosSlice(
    //someWidthOrHeight=bgSize2dInTiles.x
    someSize2d=bgSize2dInTiles
  )
  //def objPxsCoordT() = sCoordT(someSize2d=ElabVec2[Int](
  //  //x=intnlFbSize2d.x + 1,
  //  //y=intnlFbSize2d.y,
  //))

  // intnlFbSizeXOrY + 2 for on-current-line range checks
  // I think this still works?
  def objPxsCoordSize2dPow = ElabVec2[Int](
    x=log2Up(intnlFbSize2d.x + 1) + 2,
    y=log2Up(intnlFbSize2d.y + 1) + 2,
  )
  // `sCoordT()` is not used because the `log2Up()` calls are in
  // `objPxsCoordSize2d`
  //def objPxsCoordT() = sCoordT(someSize2d=objPxsCoordSize2d)
  def objPxsCoordT() = DualTypeNumVec2[SInt, SInt](
    dataTypeX=SInt(objPxsCoordSize2dPow.x bits),
    dataTypeY=SInt(objPxsCoordSize2dPow.y bits),
  )
  def bgTilePxsCoordT() = coordT(someSize2d=bgTileSize2d)
  def objTilePxsCoordT() = coordT(someSize2d=objTileSize2d)
  //def bgTilesPosInfoT() = LcvVideoPosInfo(someSize2d=bgSize2dInTiles)

  //def physToBgPxsSliceWidth = (
  //  log2Up(physFbSize2d.x / bgSize2dInPxs.x)
  //)

  //def bgPxsToBgTilesSliceHi = (
  //  log2Up(bgSize2dInPxs.x / bgSize2dInTiles.x) - 1
  //)
  //def physToBgPxsSliceHi = (
  //  log2Up(physFbSize2d.x / bgSize2dInPxs.x) - 1
  //)
  //def physToBgTilesSliceWidth = log2Up(
  //  physFbSize2d.x / bgSize2dInTiles.x
  //)
  //def bgScrollCoordT() = coordT(someSize2d=)
  //--------
}
object DefaultGpu2dParams {
  def apply(
    //--------
    rgbConfig: RgbConfig=RgbConfig(rWidth=6, gWidth=6, bWidth=6),
    intnlFbSize2d: ElabVec2[Int]=ElabVec2[Int](
      //x=(1920 / 3).toInt, // 640
      //y=(1080 / 3).toInt, // 360
      //x=(1920 / 4).toInt, // 480
      //y=(1080 / 4).toInt, // 270
      //x=(1920 / 4).toInt, // 480
      //y=(1080 / 4).toInt, // 270
      x=(1600 >> log2Up(4)).toInt, // 400
      y=(900 >> log2Up(4)).toInt, // 225
    ),
    //physFbSize2dScale: ElabVec2[Int]=ElabVec2[Int](
    //  //x=3, // 1920 / 4 = 640
    //  //y=3, // 1080 / 3 = 360
    //  x=4, // 1920 / 4 = 480
    //  y=4, // 1080 / 4 = 270
    //),
    physFbSize2dScalePow: ElabVec2[Int]=ElabVec2[Int](
      //x=log2Up(4), // 1920 / 4 = 480
      //y=log2Up(4), // 1080 / 4 = 270
      x=log2Up(4), // 1600 / 4 = 400
      y=log2Up(4), // 900 / 4 = 225
    ),
    //tileSize2d: ElabVec2[Int]=ElabVec2[Int](x=8, y=8),
    bgTileSize2dPow: ElabVec2[Int]=ElabVec2[Int](
      x=log2Up(8), // tile width: 8
      y=log2Up(8), // tile height: 8
    ),
    objTileSize2dPow: ElabVec2[Int]=ElabVec2[Int](
      x=log2Up(8), // tile width: 8
      y=log2Up(8), // tile height: 8
    ),
    numBgsPow: Int=log2Up(4), // 4 BGs
    numObjsPow: Int=log2Up(256), // 256 OBJs
    numBgTilesPow: Option[Int]=None,
    numObjTilesPow: Option[Int]=None,
    //numObjsPow: Int=log2Up(128), // 128 OBJs
    //numObjsPerScanline: Int=64,
    numColsInBgPalPow: Int=log2Up(256), // 256 colors per BG palette
    numColsInObjPalPow: Int=log2Up(256), // 256 colors per OBJ palette
    //--------
    bgTileArrRamStyle: String="block",
    objTileArrRamStyle: String="block",
    bgEntryArrRamStyle: String="block",
    //bgAttrsArrRamStyle: String="block",
    objAttrsArrRamStyle: String="block",
    bgPalEntryArrRamStyle: String="block",
    objPalEntryArrRamStyle: String="block",
    //bgLineArrRamStyle: String="block",
    //objLineArrRamStyle: String="block",
    //combineLineArrRamStyle: String="block",
    lineArrRamStyle: String="block",
    //--------
  ) = {
    def bgTileSize2d = ElabVec2[Int](
      x=1 << bgTileSize2dPow.x,
      y=1 << bgTileSize2dPow.y,
    )
    def objTileSize2d = ElabVec2[Int](
      x=1 << objTileSize2dPow.x,
      y=1 << objTileSize2dPow.y,
    )
    val tempNumBgTilesPow = numBgTilesPow match {
      case Some(myTempNumBgTilesPow) => myTempNumBgTilesPow
      case None => (
        //1024
        //2048 // (480 * 270) / (8 * 8) = 2025
        //1 <<
        log2Up(
          (intnlFbSize2d.x * intnlFbSize2d.y)
          / (bgTileSize2d.x * bgTileSize2d.y)
        )
      )
    }
    val tempNumObjTilesPow = numObjTilesPow match {
      case Some(myTempNumObjTilesPow) => myTempNumObjTilesPow
      case None => (
        //log2Up(1024)
        numObjsPow
      )
    }
    Gpu2dParams(
      //rgbConfig=RgbConfig(rWidth=6, gWidth=6, bWidth=6),
      //intnlFbSize2d=ElabVec2[Int](
      //  //x=(1920 / 3).toInt, // 640
      //  //y=(1080 / 3).toInt, // 360
      //  x=(1920 / 4).toInt, // 480
      //  y=(1080 / 4).toInt, // 270
      //),
      //physFbSize2dScale=ElabVec2[Int](
      //  //x=3, // 1920 / 4 = 640
      //  //y=3, // 1080 / 3 = 360
      //  x=4, // 1920 / 4 = 480
      //  y=4, // 1080 / 4 = 270
      //),
      //tileSize2d=ElabVec2[Int](x=8, y=8),
      rgbConfig=rgbConfig,
      intnlFbSize2d=intnlFbSize2d,
      //physFbSize2dScale=physFbSize2dScale,
      physFbSize2dScalePow=physFbSize2dScalePow,
      bgTileSize2dPow=bgTileSize2dPow,
      objTileSize2dPow=objTileSize2dPow,
      numBgTilesPow=tempNumBgTilesPow,
      numObjTilesPow=tempNumObjTilesPow,
      //bgSize2dInTiles=ElabVec2[Int](x=128, y=128),
      //bgSize2dInTiles=ElabVec2[Int](
      //  x=1 << log2Up(intnlFbSize2d.x / tileSize2d.x),
      //  y=1 << log2Up(intnlFbSize2d.y / tileSize2d.y),
      //),
      bgSize2dInTilesPow=ElabVec2[Int](
        x=log2Up(intnlFbSize2d.x / bgTileSize2d.x),
        y=log2Up(intnlFbSize2d.y / bgTileSize2d.y),
      ),
      //numBgs=4,
      //numObjs=256,
      ////numObjsPerScanline=64,
      //numColsInPal=256,
      numBgsPow=numBgsPow,
      numObjsPow=numObjsPow,
      //numObjsPerScanline=numObjsPerScanline,
      //numColsInPalPow=numColsInPalPow,
      numColsInBgPalPow=numColsInBgPalPow,
      numColsInObjPalPow=numColsInObjPalPow,
      //--------
      bgTileArrRamStyle=bgTileArrRamStyle,
      objTileArrRamStyle=objTileArrRamStyle,
      bgEntryArrRamStyle=bgEntryArrRamStyle,
      //bgAttrsArrRamStyle=bgAttrsArrRamStyle,
      objAttrsArrRamStyle=objAttrsArrRamStyle,
      bgPalEntryArrRamStyle=bgPalEntryArrRamStyle,
      objPalEntryArrRamStyle=objPalEntryArrRamStyle,
      //bgLineArrRamStyle=bgLineArrRamStyle,
      //objLineArrRamStyle=objLineArrRamStyle,
      //combineLineArrRamStyle=combineLineArrRamStyle,
      lineArrRamStyle=lineArrRamStyle,
      //--------
    )
  }
}

case class Gpu2dRgba(
  params: Gpu2dParams,
) extends Bundle {
  val rgb = Rgb(c=params.rgbConfig)
  val a = Bool()
}

case class Gpu2dTile(
  params: Gpu2dParams,
  isObj: Boolean,
) extends Bundle {
  //--------
  //val colIdx = UInt(params.palEntryMemIdxWidth bits)
  def colIdxWidth = (
    if (!isObj) {
      params.bgPalEntryMemIdxWidth
    } else {
      params.objPalEntryMemIdxWidth
    }
  )

  def pxsSize2d = (
    if (!isObj) {
      params.bgTileSize2d
    } else { // if (isObj)
      params.objTileSize2d
    }
  )

  // indices into `Gpu2d.loc.palEntryMem`
  val colIdxRowVec = (
    //Vec.fill(pxsSize2d.y)(
    //  UInt((pxsSize2d.x * colIdxWidth) bits)
    //)
    Vec.fill(pxsSize2d.y)(
      Vec.fill(pxsSize2d.x)(
        UInt(colIdxWidth bits)
      )
    )
    //if (!isObj) {
    //  //Vec.fill(params.bgTileSize2d.y)(
    //  //  Vec.fill(params.bgTileSize2d.x)(UInt(colIdxWidth bits))
    //  //)
    //  //UInt(
    //  //  (
    //  //    params.bgTileSize2dPow.y + params.bgTileSize2dPow.x
    //  //    + colIdxWidth
    //  //  )
    //  //  bits
    //  //)
    //  Vec.fill(params.bgTileSize2d.y)(
    //    UInt((params.bgTileSize2d.x * colIdxWidth) bits)
    //    //UInt((params.bgTileSize2d.x + (1 << colIdxWidth)) bits)
    //  )
    //} else { // if (isObj)
    //  //Vec.fill(params.objTileSize2d.y)(
    //  //  Vec.fill(params.objTileSize2d.x)(UInt(colIdxWidth bits))
    //  //)
    //  //UInt(
    //  //  (
    //  //    params.objTileSize2dPow.y + params.objTileSize2dPow.x
    //  //    + colIdxWidth
    //  //  ) bits
    //  //)
    //  Vec.fill(params.objTileSize2d.y)(
    //    //UInt((params.objTileSize2dPow.x + colIdxWidth) bits)
    //    UInt((params.objTileSize2d.x * colIdxWidth) bits)
    //  )
    //}
  )

  def getRowAsVec(
    idx: Int,
  ) = {
    //colIdxRowVec(idx).subdivideIn(
    //  //colIdxWidth slices
    //  pxsSize2d.x slices
    //)
    colIdxRowVec(idx)
  }

  def setPx(
    pxsCoord: ElabVec2[Int],
    colIdx: UInt,
  ): Unit = {
    assert(pxsCoord.x >= 0 && pxsCoord.x < pxsSize2d.x)
    assert(pxsCoord.y >= 0 && pxsCoord.y < pxsSize2d.y)
    //val rowVec = getRowAsVec(pxsCoord.y)
    //rowVec(pxsCoord.x) := colIdx
    colIdxRowVec(pxsCoord.y)(pxsCoord.x) := colIdx
    def pxsCoordX = pxsCoord.x
    def pxsCoordY = pxsCoord.y
    //println(f"$pxsCoordX $pxsCoordY $colIdx")

    //colIdxRowVec(pxsCoord.y)(
    //  ((pxsCoord.x + 1) * colIdxWidth - 1)
    //  downto (pxsCoord.x * colIdxWidth)
    //) := colIdx

    //colIdxRowVec(pxsCoord.y)(
    //  pxsCoord.x,
    //  //colIdxWidth bits
    //  log2Up(pxsSize2d.x) bits
    //) := colIdx

    //colIdxRowVec(pxsCoord.y).assignFromBits(rowVec.asBits)
  }
  //def setPx(
  //  pxsCoord: DualTypeNumVec2[UInt, UInt],
  //  colIdx: UInt,
  //): Unit = {
  //  //val row = colIdxRowVec(pxsCoord.y)
  //  //val colIdxVec = row.subdivideIn(pxsSize2d.x slices)
  //  //colIdxVec(pxsCoord.x) := colIdx
  //  //row.assignFromBits(colIdxVec.asBits)

  //  switch (pxsCoord.y) {
  //    for (jdx <- 0 to pxsSize2d.y - 1) {
  //      is (jdx) {
  //        switch (pxsCoord.x) {
  //          for (idx <- 0 to pxsSize2d.x - 1) {
  //            is (idx) {
  //              setPx(
  //                pxsCoord=ElabVec2[Int](x=idx, y=jdx),
  //                colIdx=colIdx
  //              )
  //            }
  //          }
  //        }
  //      }
  //    }
  //  }
  //}

  //def setPxsRow(
  //  pxsCoordY: UInt,
  //  colIdxRow: UInt,
  //): Unit = {
  //  colIdxRowVec(pxsCoordY.resized) := colIdxRow
  //}

  //def setRow(
  //  pxsCoordY: Int,
  //  row: UInt,
  //): Unit = {
  //  assert(pxsCoordY >= 0 && pxsCoordY < pxsSize2d.y)
  //}
  def getPx(
    pxsCoord: ElabVec2[Int],
  ) = {
    assert(pxsCoord.x >= 0 && pxsCoord.x < pxsSize2d.x)
    assert(pxsCoord.y >= 0 && pxsCoord.y < pxsSize2d.y)
    val rowVec = getRowAsVec(pxsCoord.y)
    //rowVec(pxsCoord.x) := colIdx
    //colIdxRowVec(pxsCoord.y).assignFromBits(rowVec.asBits)
    rowVec(pxsCoord.x)
  }
  def getPx(
    pxsCoord: DualTypeNumVec2[UInt, UInt]
  ) = {
    //val row = colIdxRowVec(pxsCoord.y.resized)
    //val colIdxVec = row.subdivideIn(pxsSize2d.x slices)
    ////colIdxVec(pxsCoord.x) := colIdx
    ////row.assignFromBits(colIdxVec.asBits)
    //colIdxVec(pxsCoord.x.resized)
    colIdxRowVec(pxsCoord.y)(pxsCoord.x)
  }
  //--------
}
case class Gpu2dBgTileStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  //val rgb = Rgb(params.rgbConfig)
  val tile = Gpu2dTile(params=params, isObj=false)

  // `Mem` index, so in units of pixels
  //val idx = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  //val idx = UInt(16 bits)
  val memIdx = UInt(params.bgTileMemIdxWidth bits)
  //--------
}
case class Gpu2dObjTileStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  //val rgb = Rgb(params.rgbConfig)
  val tile = Gpu2dTile(params=params, isObj=true)

  // `Mem` index, so in units of pixels
  //val idx = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  //val idx = UInt(16 bits)
  val memIdx = UInt(params.objTileMemIdxWidth bits)
  //--------
}

case class Gpu2dBgEntry(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  // The index, in tiles, of the tile represented by this tilemap entry
  val tileMemIdx = UInt(params.bgTileMemIdxWidth bits)

  // The priority for this tilemap entry
  //val prio = UInt(log2Up(params.numBgs) bits)

  // whether or not to visibly flip x/y during rendering
  val dispFlip = Vec2(dataType=Bool())
  //--------
}
case class Gpu2dBgEntryStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val bgEntry = Gpu2dBgEntry(params=params)

  // `Mem` index
  val memIdx = UInt(params.bgEntryMemIdxWidth bits)
  //--------
}

// Attributes for a whole background
case class Gpu2dBgAttrs(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  //// Whether or not to display this BG
  //val visib = Bool()

  // How much to scroll this background
  val scroll = params.bgPxsCoordT()
  //--------
}
case class Gpu2dBgAttrsStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val bgAttrs = Gpu2dBgAttrs(params=params)

  //val memIdx = UInt(params.bgAttrsMemIdxWidth bits)
  //--------
}

case class Gpu2dObjAttrs(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  // Actually this is handled with sufficiently negative `pos.x`
  ///// Whether or not to display this OBJ
  //val visib = Bool()

  // The index, in tiles, of the tile represented by this sprite
  val tileMemIdx = UInt(params.objTileMemIdxWidth bits)
  //val pos = params.coordT(fbSize2d=params.bgSize2dInPxs)

  //// position within the tilemap, in pixels
  // position on screen, in pixels
  val pos = params.objPxsCoordT()

  // the priority for the OBJ
  val prio = UInt(log2Up(params.numBgs) bits)

  // whether or not to visibly flip x/y during rendering
  val dispFlip = Vec2(dataType=Bool())
  //--------
}

case class Gpu2dObjAttrsStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val objAttrs = Gpu2dObjAttrs(params=params)
  // `Mem` index
  val memIdx = UInt(params.objAttrsMemIdxWidth bits)
  // `Vec` index
  //val vecIdx = UInt(params.objAttrsMemIdxWidth bits)
  //--------
}
case class Gpu2dPalEntry(
  params: Gpu2dParams
) extends Bundle {
  //--------
  val col = Rgb(params.rgbConfig)
  //--------
}
case class Gpu2dObjPalEntryStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val objPalEntry = Gpu2dPalEntry(params=params)
  val memIdx = UInt(params.objPalEntryMemIdxWidth bits)
  //--------
}

//case class Gpu2dPalEntry(
//  params: Gpu2dParams
//) extends Bundle {
//  //--------
//  val col = Rgb(params.rgbConfig)
//  //--------
//}
case class Gpu2dBgPalEntryStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val bgPalEntry = Gpu2dPalEntry(params=params)
  val memIdx = UInt(params.bgPalEntryMemIdxWidth bits)
  //--------
}
case class Gpu2dPopPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val col = Rgb(params.rgbConfig)
  //val physPxPos = params.physPxCoordT()
  //val intnlPxPos = params.bgPxsCoordT()
  //val tilePos = params.bgTilesCoordT()
  val physPosInfo = params.physPosInfoT()
  //val bgPxsPos = params.bgPxsCoordT()
  //val bgPxsPosInfo = params.bgPxsPosInfoT()
  val bgPxsPosSlice = params.bgPxsPosSliceT()
  //val bgTilesPos = params.bgTilesCoordT()
  val bgTilesPosSlice = params.bgTilesPosSliceT()
  //val bgTilesPosInfo = params.bgTilesPosInfoT()
  //--------
}

case class Gpu2dIo(
  params: Gpu2dParams=DefaultGpu2dParams(),
) extends Bundle {
  //--------
  //val en = in Bool()
  //val blank = in(Vec2(Bool()))
  //val en = in Bool()
  //--------
  //val bgTilePush = slave Stream()
  //val bgTilePush = slave Stream()
  val bgTilePush = slave Stream(Gpu2dBgTileStmPayload(params=params))
  val bgEntryPushArr = new ArrayBuffer[Stream[Gpu2dBgEntryStmPayload]]()
  val bgAttrsPushArr = new ArrayBuffer[Stream[Gpu2dBgAttrsStmPayload]]()
  for (idx <- 0 to params.numBgs - 1) {
    bgEntryPushArr += (
      slave Stream(Gpu2dBgEntryStmPayload(params=params))
        .setName(f"bgEntryPushArr_$idx")
    )
    bgAttrsPushArr += (
      slave Stream(Gpu2dBgAttrsStmPayload(params=params))
        .setName(f"bgAttrsPushArr_$idx")
    )
  }
  val bgPalEntryPush = slave Stream(Gpu2dBgPalEntryStmPayload(
    params=params
  ))
  val objTilePush = slave Stream(Gpu2dObjTileStmPayload(params=params))
  val objAttrsPush = slave Stream(Gpu2dObjAttrsStmPayload(params=params))
  val objPalEntryPush = slave Stream(Gpu2dObjPalEntryStmPayload(
    params=params
  ))
  //--------
  val ctrlEn = out Bool()
  val pop = master Stream(Gpu2dPopPayload(params=params))
  //--------
}
case class Gpu2d(
  params: Gpu2dParams=DefaultGpu2dParams(),
) extends Component {
  //--------
  val io = Gpu2dIo(params=params)
  //--------
  val bgTilePush = io.bgTilePush
  bgTilePush.ready := True
  val bgEntryPushArr = io.bgEntryPushArr
  val bgAttrsPushArr = io.bgAttrsPushArr
  for (idx <- 0 to params.numBgs - 1) {
    bgEntryPushArr(idx).ready := True
    bgAttrsPushArr(idx).ready := True
  }
  val objTilePush = io.objTilePush
  objTilePush.ready := True
  val objAttrsPush = io.objAttrsPush
  objAttrsPush.ready := True
  val bgPalEntryPush = io.bgPalEntryPush
  bgPalEntryPush.ready := True
  val objPalEntryPush = io.objPalEntryPush
  objPalEntryPush.ready := True
  //--------
  val ctrlEn = io.ctrlEn
  val rPastCtrlEn = RegNext(ctrlEn) init(False)
  val rCtrlEn = Reg(Bool()) init(False)
  ctrlEn := rCtrlEn
  //ctrlEn := True

  val pop = io.pop
  //val rPopValid = Reg(Bool()) init(True) //init(False)
  //rPopValid := True
  //pop.valid := rPopValid
  //////val rOutp = Reg(cloneOf(pop.payload))

  ////val nextOutp = cloneOf(pop.payload)
  ////val rOutp = RegNext(nextOutp) init(nextOutp.getZero)
  //////rOutp.init(rOutp.getZero)
  ////pop.payload := rOutp
  val outp = cloneOf(pop.payload)
  val rPastOutp = RegNext(outp) init(outp.getZero)
  //val popFifo = AsyncReadFifo(
  //  //dataType=Rgb(params.rgbConfig),
  //  dataType=Gpu2dPopPayload(params=params),
  //  //depth=params.physFbSize2d.x,
  //  depth=params.physFbSize2d.x * 4 + 1,
  //)
  //val fifoPush = popFifo.io.push
  ////fifoPush.payload := outp

  //val fifoPop = popFifo.io.pop
  //pop << fifoPop
  when (pop.fire) {
    //ctrlEn := True
    rCtrlEn := True
  }
  //.otherwise {
  //  //ctrlEn := rPastCtrlEn
  //}

  //pop.payload := outp
  //val popPsb = PipeSkidBuf(
  //  dataType=Gpu2dPopPayload(params=params)
  //)


  //pop.valid := True
  //val col = pop.payload.col
  //val physPxPos = pop.payload.physPxPos
  //val intnlPxPos = pop.payload.intnlPxPos
  //val tilePos = pop.payload.tilePos
  //--------
  val loc = new Area {
    //--------
    val bgTileMem = Mem(
      //wordType=UInt(params.palEntryMemIdxWidth bits),
      wordType=Gpu2dTile(params=params, isObj=false),
      //wordCount=params.numPxsForAllBgTiles,
      //wordCount=params.numTilesPerBg,
      wordCount=params.numBgTiles,
    )
      .initBigInt(Array.fill(params.numBgTiles)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.bgTileArrRamStyle)
    when (bgTilePush.fire) {
      bgTileMem.write(
        address=bgTilePush.payload.memIdx,
        data=bgTilePush.payload.tile,
      )
    }
    val objTileMem = Mem(
      //wordType=UInt(params.palEntryMemIdxWidth bits),
      wordType=Gpu2dTile(params=params, isObj=false),
      //wordCount=params.numPxsForAllObjTiles,
      wordCount=params.numObjTiles,
    )
      .initBigInt(Array.fill(params.numObjTiles)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.objTileArrRamStyle)
    when (objTilePush.fire) {
      objTileMem.write(
        address=objTilePush.payload.memIdx,
        data=objTilePush.payload.tile,
      )
    }
    //--------
    //val bgEntryMem = Mem(
    //  wordType=Gpu2dBgEntry(params=params),
    //  wordCount=params.numColsInPal,
    //)
    //  .addAttribute("ram_style", params.palEntryArrRamStyle)
    //--------
    val bgEntryMemArr = new ArrayBuffer[Mem[Gpu2dBgEntry]]()
    val bgAttrsArr = new ArrayBuffer[Gpu2dBgAttrs]()
    for (idx <- 0 to params.numBgs - 1) {
      //--------
      bgEntryMemArr += Mem(
        wordType=Gpu2dBgEntry(params=params),
        wordCount=params.numTilesPerBg,
      )
        .initBigInt(Array.fill(params.numTilesPerBg)(BigInt(0)).toSeq)
        .addAttribute("ram_style", params.bgEntryArrRamStyle)
        .setName(f"bgEntryMemArr_$idx")
      when (bgEntryPushArr(idx).fire) {
        bgEntryMemArr(idx).write(
          address=bgEntryPushArr(idx).payload.memIdx,
          data=bgEntryPushArr(idx).payload.bgEntry,
        )
      }
      //--------
      bgAttrsArr += Reg(Gpu2dBgAttrs(params=params))
      bgAttrsArr(idx).init(bgAttrsArr(idx).getZero)
        .setName(f"bgAttrsArr_$idx")
      when (bgAttrsPushArr(idx).fire) {
        bgAttrsArr(idx) := bgAttrsPushArr(idx).payload.bgAttrs
      }
      //--------
    }
    //val bgAttrsMem = Mem(
    //  wordType=Gpu2dBgAttrs(params=params),
    //  wordCount=params.numBgs,
    //)
    //  .initBigInt(Array.fill(params.numBgs)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", params.bgAttrsArrRamStyle)
    //--------
    val objAttrsMem = Mem(
      wordType=Gpu2dObjAttrs(params=params),
      wordCount=params.numObjs,
    )
      .initBigInt(Array.fill(params.numObjs)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.objAttrsArrRamStyle)
    when (objAttrsPush.fire) {
      objAttrsMem.write(
        address=objAttrsPush.payload.memIdx,
        data=objAttrsPush.payload.objAttrs,
      )
    }
    //val objAttrsVec = Vec.fill(params.numObjs)(
    //  Reg(Gpu2dObjAttrs(params=params))
    //)
    //for (idx <- 0 to objAttrsVec.size - 1) {
    //  objAttrsVec(idx).init(objAttrsVec(idx).getZero)
    //}
    //when (objAttrsPush.fire) {
    //  objAttrsVec(objAttrsPush.payload.vecIdx) := (
    //    objAttrsPush.payload.objAttrs
    //  )
    //}
    //--------
    val bgPalEntryMem = Mem(
      wordType=Gpu2dPalEntry(params=params),
      wordCount=params.numColsInBgPal,
    )
      .initBigInt(Array.fill(params.numColsInBgPal)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.bgPalEntryArrRamStyle)
      .addAttribute("keep")
    when (bgPalEntryPush.fire) {
      bgPalEntryMem.write(
        address=bgPalEntryPush.payload.memIdx,
        data=bgPalEntryPush.payload.bgPalEntry,
      )
    }

    val objPalEntryMem = Mem(
      wordType=Gpu2dPalEntry(params=params),
      wordCount=params.numColsInObjPal,
    )
      .initBigInt(Array.fill(params.numColsInObjPal)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.objPalEntryArrRamStyle)
    when (objPalEntryPush.fire) {
      objPalEntryMem.write(
        address=objPalEntryPush.payload.memIdx,
        data=objPalEntryPush.payload.objPalEntry,
      )
    }
    //--------
    //val rWrBgChangingRowCnt = KeepAttribute(
    //  Reg(UInt(log2Up(params.lineMemSize) + 1 bits))
    //  init(params.lineMemSize - 1)
    //)
    //val rIntnlChangingRow = Reg(Bool()) init(False)
    //val rIntnlChangingRow = Reg(Bool()) init(True)

    //val nextIntnlChangingRow = Bool()
    ////val rIntnlChangingRow = RegNext(nextIntnlChangingRow) init(True)
    //val rIntnlChangingRow = RegNext(nextIntnlChangingRow) init(False)

    //val nextWrBgChangingRow = Bool()
    //val rWrBgChangingRow = RegNext(nextWrBgChangingRow) init(False)
    //val nextWrObjChangingRow = Bool()
    //val rWrObjChangingRow = RegNext(nextWrObjChangingRow) init(False)
    //val nextCombineChangingRow = Bool()
    //val rCombineChangingRow = RegNext(nextCombineChangingRow) init(False)
    ////val nextRdChangingRow = Bool()
    ////val rRdChangingRow = RegNext(nextRdChangingRow) init(False)
    ////val nextRdChangingRowRe = Bool()

    //val rPastIntnlChangingRow = RegNext(rIntnlChangingRow) init(False)
    //val intnlChangingRowRe = KeepAttribute(Bool())
    //intnlChangingRowRe := rIntnlChangingRow && !rPastIntnlChangingRow
    ////val nextIntnlChangingRowRe = KeepAttribute(Bool())
    ////nextIntnlChangingRowRe := nextIntnlChangingRow && !rIntnlChangingRow
    //nextIntnlChangingRow := (
    //  // all of the pipelines must have finished the current scanline
    //  //outp.physPosInfo.nextPos.x === 0
    //  //outp.physPosInfo.changingRow
    //  rWrBgChangingRow
    //  && rWrObjChangingRow
    //  && rCombineChangingRow

    //  //&& rRdChangingRow

    //  //nextWrBgChangingRow
    //  //&& nextWrObjChangingRow
    //  //&& nextCombineChangingRow
    //  //&& nextRdChangingRow
    //  //(
    //  //  //rPastOutp.bgPxsPosSlice.pos.y
    //  //  //=/= rPastOutp.bgPxsPosSlice.pastPos.y
    //  //  //outp.physPosInfo.pos.y
    //  //  //=/= outp.physPosInfo.pastPos.y
    //  //  //outp.physPosInfo.changingRow
    //  //  outp.physPosInfo.nextPos.x === 0
    //  //)
    //  //&& (
    //  //  rPastOutp.bgPxsPosSlice.pos.y === 0
    //  //) && (
    //  //  rPastOutp.bgPxsPosSlice.pastPos.y === params.intnlFbSize2d.y - 1
    //  //)
    //  //(
    //  //  // since we're using `rPastOutp`, `nextPos` is registered, and thus
    //  //  // there is higher FMax
    //  //  //(
    //  //  //  rPastOutp.bgPxsPosSlice.nextPos.y
    //  //  //  =/= rPastOutp.bgPxsPosSlice.pos.y
    //  //  //) && (
    //  //  //  rPastOutp.bgPxsPosSlice.nonSliceChangingRow
    //  //  //)
    //  //  //rPastOutp.bgPxsPosSlice.nextPos.x === 0

    //  //  //pop.fire 
    //  //  ////fifoPush.fire
    //  //  rIntnlChangingRowCnt.msb
    //  //  && 
    //  //  rPastOutp.bgPxsPosSlice.pos.x === 0
    //  //  && rPastOutp.physPosInfo.changingRow

    //  //  ////rPastOutp.bgPxsPosSlice.pos.y
    //  //  ////=/= rPastOutp.bgPxsPosSlice.pastPos.y
    //  //  ////=== rPastOutp.bgPxsPosSlice.pastPos.y + 1
    //  //)
    //  //|| (
    //  //  rPastOutp.bgPxs
    //  //)
    //)
    //--------
    val wrBgCalcPos = LcvVideoCalcPos(
      someSize2d=params.intnlFbSize2d
    )
    val wrObjCalcPos = LcvVideoCalcPos(
      someSize2d=params.intnlFbSize2d
    )
    val combineCalcPos = LcvVideoCalcPos(
      someSize2d=params.intnlFbSize2d
    )

    //val wrBgIntnlCalcPos = LcvVideoCalcPos(
    //  someSize2d=params.intnlFbSize2d
    //)
    //wrIntnlCalcPos.io.en := !rWrBgChangingRow

    val rdPhysCalcPos = LcvVideoCalcPos(
      someSize2d=params.physFbSize2d
    )
    //physCalcPos.io.en := pop.fire
    //physCalcPos.io.en := True
    //val rRdPhysCalcPosEn = Reg(Bool()) init(False) //init(True)
    //rdPhysCalcPos.io.en := rRdPhysCalcPosEn

    val rdPhysCalcPosEn = Bool()
    val rPastRdPhysCalcPosEn = RegNext(rdPhysCalcPosEn) init(False)
    //val rRdPhysCalcPosEn = Reg(Bool()) init(True)
    //rdPhysCalcPos.io.en := pop.fire
    //rdPhysCalcPos.io.en := rdPhysCalcPosEn
    //rdPhysCalcPos.io.en := rRdPhysCalcPosEn
    //rdPhysCalcPos.io.en := !rIntnlChangingRow
    rdPhysCalcPos.io.en := rdPhysCalcPosEn

    //rdPhysCalcPos.io.en := 

    //rdPhysCalcPos.io.en := !rRdChangingRow

    //physCalcPos.io.en := fifoPush.fire
    //physCalcPos.io.en := True

    outp.physPosInfo := rdPhysCalcPos.io.info
    outp.bgPxsPosSlice := outp.physPosInfo.posSlice(
      someSize2d=params.fbSize2dInPxs,
      someScalePow=params.physToIntnlScalePow,
      //thatSomeSize2d=params.bg
    )
    outp.bgTilesPosSlice := outp.physPosInfo.posSlice(
      someSize2d=params.fbSize2dInBgTiles,
      someScalePow=params.physToBgTilesScalePow,
    )

    //case class LineMemEntry() extends Bundle {
    //  val bgColVec = Vec(Gpu2dRgba(params=params), params.numBgs)
    //  val objColVec = Vec(Gpu2dRgba(params=params), params.numBgs)
    //}
    //case class LineMemEntry() extends Bundle {
    //  val col = Vec.fill(params.numBgs)(Gpu2dRgba(params=params))
    //  //val prio = Vec.fill(params.numBgs)(UInt(params.numBgsPow bits))
    //  //val bgIdx = 
    //  val prio = Vec.fill(params.numBgs)(UInt(params.numBgsPow bits))
    //  //val col = Gpu2dRgba(params=params)
    //  //val prio = UInt(params.numBgsPow bits)
    //}
    case class BgLineMemEntry() extends Bundle {
      val col = Gpu2dRgba(params=params)
      val prio = UInt(params.numBgsPow bits)
    }
    case class ObjLineMemEntry() extends Bundle {
      val col = Gpu2dRgba(params=params)
      val prio = UInt(params.numBgsPow bits)
      //val written = Bool() // only used for prio
      //val prio = UInt((params.numBgsPow + 1) bits)
      //val objIdx = UInt(params.numObjsPow bits)
    }
    //// Which sprites have we drawn this scanline?
    //// This needs to be reset to 0x0 upon upon starting rendering the next 
    //// scanline. 
    //// Note that this may not scale well to very large numbers of sprites,
    //// but at that point you may be better off with a GPU for 3D graphics.
    //val rObjsDrawnThisLine = Reg(Bits(params.numObjs bits)) init(0x0)
    //val rObjDrawnPosXVec = Reg(
    //  //Vec(UInt(log2Up(params.intnlFbSize2d.x) bits), params.numObjs)
    //)
    //for (idx <- 0 to rObjDrawnPosXVec.size - 1) {
    //  rObjDrawnPosXVec(idx).init(rObjDrawnPosXVec(idx).getZero)
    //}

    //def LineMemEntry() = Vec.fill(params.numBgs)(Gpu2dRgba(params=params))
    //def LineMemEntry() = Gpu2dRgba(params=params)
    //val lineMemArr = new ArrayBuffer[Mem[BgLineMemEntry]]()

    case class LineFifoEntry() extends Bundle {
      val bgLineMemEntry = BgLineMemEntry()
      val objLineMemEntry = ObjLineMemEntry()
    }
    val lineFifo = AsyncReadFifo(
      dataType=LineFifoEntry(),
      depth=params.lineFifoDepth,
    )

    //val lineFifo = AsyncReadFifo(
    //  dataType=BgLineMemEntry(),
    //  //depth=params.lineMemSize * 2 + 1,
    //  depth=params.lineFifoDepth,
    //)
    //val objLineFifo = AsyncReadFifo(
    //  dataType=ObjLineMemEntry(),
    //  //depth=params.lineMemSize * 2 + 1,
    //  depth=params.lineFifoDepth,
    //)

    val bgLineMemArr = new ArrayBuffer[Mem[BgLineMemEntry]]()
    val objLineMemArr = new ArrayBuffer[Mem[ObjLineMemEntry]]()
    //def combineLineMemArr = bgLineMemArr
    //val combineLineMemArr = new ArrayBuffer[Mem[Gpu2dRgba]]()
    //val wrLineMemIdx

    for (idx <- 0 to params.numLineMemsPerBgObjRenderer - 1) {
      //lineMemArr += Mem(
      //  //wordType=Rgb(params.rgbConfig),
      //  wordType=LineMemEntry(),
      //  wordCount=params.lineMemSize,
      //)
      //  .initBigInt(Array.fill(params.lineMemSize)(BigInt(0)).toSeq)
      //  .addAttribute("ram_style", params.lineArrRamStyle)

      bgLineMemArr += Mem(
        //wordType=Rgb(params.rgbConfig),
        wordType=BgLineMemEntry(),
        wordCount=params.lineMemSize,
      )
        .initBigInt(Array.fill(params.lineMemSize)(BigInt(0)).toSeq)
        .addAttribute("ram_style", params.lineArrRamStyle)
      objLineMemArr += Mem(
        //wordType=Rgb(params.rgbConfig),
        wordType=ObjLineMemEntry(),
        wordCount=params.lineMemSize,
      )
        .initBigInt(Array.fill(params.lineMemSize)(BigInt(0)).toSeq)
        .addAttribute("ram_style", params.lineArrRamStyle)
    }

    //val nextLineIdx = KeepAttribute(UInt(log2Up(params.numLineMems) bits))
    //val rLineIdx = KeepAttribute(RegNext(nextLineIdx)) init(0x0)

    //val nextCol = Rgb(params.rgbConfig)
    //val rCol = RegNext(nextCol)
    //rCol.init(rCol.getZero)
    //--------
    //when (fifoPush.fire) {
    //  rPastOutp := outp
    //}

    // round-robin indexing into the `ArrayBuffer`s

    //val rPastWrLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits)) init(0x1)
    //)
    // Used to clear the written OBJ pixels
    //val rObjClearLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits)) init(0x1)
    //)
    //val rObjClearCnt = Reg(
    //  UInt(log2Up(params.intnlFbSize2d.x) bits)
    //) init(0x0)

    def lineMemArrIdxWidth = log2Up(params.numLineMemsPerBgObjRenderer)
    ////def rdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    ////def combineWrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd1")
    ////def combineRdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd2")
    ////def wrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd3")
    //def combineRdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    //def wrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd1")
    def sendIntoFifoLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    def wrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd1")

    //val rSendIntoFifoLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
    //  init(sendIntoFifoLineMemArrIdxInit)
    //)

    //val rWrLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
    //  //init(0x3)
    //  init(wrLineMemArrIdxInit)
    //)
    //def wrLineNumInit = 0x3
    //def wrLineNumInit = 0x2
    def wrLineNumInit = 0x1
    val rWrBgLineNum = KeepAttribute(
      Reg(UInt(log2Up(params.intnlFbSize2d.y) bits))
      //init(0x2)
      //init(0x3)
      init(wrLineNumInit)
    )
    val rWrObjLineNum = KeepAttribute(
      Reg(UInt(log2Up(params.intnlFbSize2d.y) bits))
      //init(0x2)
      //init(0x3)
      init(wrLineNumInit)
    )
    //val rWrLineNumPassed
    //when (rWrLineNum >= wrLineNumInit + 3) {
    //  rCtrlEn := True
    //}

    //val rCombineRdLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
    //  //init(0x2)
    //  init(combineRdLineMemArrIdxInit)
    //)
    //val rCombineWrLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
    //  //init(0x1)
    //  init(combineWrLineMemArrIdxInit)
    //)

    ////val rCombineLineMemCnt = KeepAttribute(
    ////  Reg(UInt(log2Up(params.intnlFbSize2d.x + 1) bits)) init(0x0)
    ////)

    //val rRdLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
    //  //init(0x0)
    //  init(rdLineMemArrIdxInit)
    //)
    //val rRdLineMemCnt = KeepAttribute(
    //  Reg(cloneOf(rCombineLineMemCnt)) init(0x0)
    //)
    //wrLineMemArrIdx := outp.bgPxsPosSlice.pos.y(wrLineMemArrIdx.bitsRange)
    ////rdLineMemArrIdx(0) := !outp.bgPxsPosSlice.pos.y(0)
    //combineLineMemArrIdx := wrLineMemArrIdx + 1
    //rdLineMemArrIdx := wrLineMemArrIdx + 
    val rWrBgLineNumCheckPipe2 = Reg(Bool()) init(False)
    val rWrBgLineNumPlus1Pipe2 = Reg(UInt(rWrBgLineNum.getWidth + 1 bits))
      .init(0x0)
    //val rWrBgLineNumPlus1Pipe2 := Reg(cloneOf(rWrBgLineNum)) init(0x0)
    val rWrBgLineNumPipe1 = Reg(UInt(rWrBgLineNum.getWidth + 1 bits))
      .init(0x0)
    rWrBgLineNumCheckPipe2 := (
      rWrBgLineNum.resized =/= params.intnlFbSize2d.y - 1
    )
    def tempWrBgLineNumPipeWidth = rWrBgLineNumPlus1Pipe2.getWidth
    //rWrBgLineNumPlus1Pipe2 := rWrBgLineNum.resized + 1
    rWrBgLineNumPlus1Pipe2 := (
      rWrBgLineNum.resized + U(f"$tempWrBgLineNumPipeWidth'd1")
    )
    when (rWrBgLineNumCheckPipe2) {
      rWrBgLineNumPipe1 := rWrBgLineNumPlus1Pipe2
    } otherwise {
      rWrBgLineNumPipe1 := 0
    }

    val rWrObjLineNumCheckPipe2 = Reg(Bool()) init(False)
    val rWrObjLineNumPlus1Pipe2 = Reg(
      UInt(rWrObjLineNum.getWidth + 1 bits)
    )
      .init(0x0)
    //val rWrObjLineNumPlus1Pipe2 := Reg(cloneOf(rWrObjLineNum)) init(0x0)
    val rWrObjLineNumPipe1 = Reg(UInt(rWrObjLineNum.getWidth + 1 bits))
      .init(0x0)
    rWrObjLineNumCheckPipe2 := (
      rWrObjLineNum.resized =/= params.intnlFbSize2d.y - 1
    )
    def tempWrObjLineNumPipeWidth = rWrObjLineNumPlus1Pipe2.getWidth
    //rWrObjLineNumPlus1Pipe2 := rWrObjLineNum.resized + 1
    rWrObjLineNumPlus1Pipe2 := (
      rWrObjLineNum.resized + U(f"$tempWrObjLineNumPipeWidth'd1")
    )
    when (rWrObjLineNumCheckPipe2) {
      rWrObjLineNumPipe1 := rWrObjLineNumPlus1Pipe2
    } otherwise {
      rWrObjLineNumPipe1 := 0
    }
    //val rPastChangingRow = RegNext(outp.physPosInfo.changingRow)
    //val changingRowRe = outp.physPosInfo.changingRow && !rPastChangingRow

    //when (
    //  //rIntnlChangingRow
    //  intnlChangingRowRe
    //  //nextIntnlChangingRowRe
    //  //outp.physPosInfo.changingRow
    //  //changingRowRe
    //) {
    //  //rPastWrLineMemArrIdx := rPastWrLineMemArrIdx + 1
    //  //rObjClearLineMemArrIdx := rObjClearLineMemArrIdx + 1

    //  //rObjLineMemClearCnt := 0x0

    //  //rPastWrLineMemArrIdx := rPastWrLineMemArrIdx + 1

    //  // BEGIN: old logic, may not be working properly
    //  //rWrLineMemArrIdx := rWrLineMemArrIdx + 1
    //  // END: old logic, may not be working properly

    //  //when (rWrLineNum =/= params.intnlFbSize2d.y - 1) {
    //  //  rWrLineNum := rWrLineNum + 1
    //  //} otherwise {
    //  //  rWrLineNum := 0
    //  //}
    //  rWrLineNum := rWrLineNumPipe1(rWrLineNum.bitsRange)
    //  //rWrLineNum := rWrLineNum + 1

    //  // BEGIN: old logic, may not be working properly
    //  //rPastCombineLineMemArrIdx := rPastCombineLineMemArrIdx + 1
    //  rCombineRdLineMemArrIdx := rCombineRdLineMemArrIdx + 1
    //  //rCombineWrLineMemArrIdx := rCombineWrLineMemArrIdx + 1
    //  ////rCombineLineMemCnt := 0

    //  ////rPastRdLineMemArrIdx := rPastRdLineMemArrIdx + 1
    //  //rRdLineMemArrIdx := rRdLineMemArrIdx + 1
    //  ////rRdLineMemCnt := 0
    //  //// END: old logic, may not be working properly
    //}

    //val rWrBgCnt = Reg(UInt(log2Up(params.numBgs) + 1 bits)) init(0x0)
    //val rWrObjCnt = Reg(UInt(log2Up(params.numObjs) + 1 bits)) init(0x0)

    //val wrLineMemIdx = outp.bgPxsPosSlice.pos.x
    //val wrLineMemIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.intnlFbSize2d.x) bits)) init(0x0)
    //)

    // BEGIN: OLD NOTES
    // The background-writing pipeline iterates through the `params.numBgs`
    // backgrounds, and increments counters for the pixel x-position
    // (`WrBgPipePayload.pxPosXVec`). This pipeline also writes the pixels
    // into `bgLineMemArr(someWrLineMemArrIdx)`
    // END: OLD NOTES

    // The background-writing pipeline iterates through the
    // `params.numBgs` backgrounds, and draws them into `bgLineMem`
    def wrBgPipeCntWidth = (
      log2Up(params.intnlFbSize2d.x + 1) + params.numBgsPow + 1
    )
    def wrBgPipeBakCntStart = (
      //(1 << (params.objTileSize2dPow.x + params.objAttrsMemIdxWidth))
      //- 1
      (params.intnlFbSize2d.x * (1 << params.numBgsPow)) - 1
    )
    //def wrBgPipeBgIdxWidth = params.numBgsPow + 1
    def wrBgPipeBgIdxWidth = params.numBgsPow
    //val rWrBgPipeFrontCntWidth = KeepAttribute(
    //  Reg(UInt(wrBgPipeFrontCntWidth bits)) init(0x0)
    //)
    case class WrBgPipePayload(
      //idx: Int
    ) extends Bundle {
      // pixel x-position
      //val pxPosXVec = Vec.fill(params.numBgs)(
      //  UInt(log2Up(params.intnlFbSize2d.x) bits)
      //)
      val stage0 = new Bundle {
        //val lineMemArrIdx = cloneOf(rWrLineMemArrIdx)
        val lineMemArrIdx = UInt(
          //log2Up(params.numLineMemsPerBgObjRenderer) bits
          lineMemArrIdxWidth bits
        )
        //val cnt = UInt(wrBgPipeFrontCntWidth bits)
        val cnt = UInt(wrBgPipeCntWidth bits)
        val bakCnt = UInt(wrBgPipeCntWidth bits)
        val bakCntMinus1 = UInt(wrBgPipeCntWidth bits)
        //val cnt = cloneOf(rWrBgPipeFrontCntWidth)
        // which background are we processing?
        // NOTE: we process backgrounds in reverse order
        //val bgIdx = SInt(wrBgPipeCntWidth bits)
        //val bgIdx = UInt(wrBgPipeBgIdxWidth bits)
        //def bgIdx = cnt(params.numBgsPow - 1 downto 0)
        // background scroll

        def bgIdx = (
          bakCnt(
            //cnt.high
            //downto (cnt.high - wrBgPipeBgIdxWidth + 1)
            wrBgPipeBgIdxWidth -1 downto 0
          )
        )
        //val scroll = cloneOf(bgAttrsArr(0).scroll)
        val bgAttrs = cloneOf(bgAttrsArr(0))
        //val tilePxsCoord = params.bgTilePxsCoordT()

        //def cntWillBeDone() = (
        //  // With more than one background, this should work
        //  //cnt(cnt.high downto params.numBgsPow)
        //  getCntPxPosX() >= params.intnlFbSize2d.x - 1
        //)

        //def cntWillBeDone() = (
        //  //cnt === params.intnlFbSize2d.x * params.numBgs - 1
        //  cntMinus1.msb
        //)
        def bakCntWillBeDone() = (
          //bakCntMinus1.msb
          //cnt + 1 === params.lineMemSize
          //bakCntMinus1 === 0
          bakCnt === 0
          //bakCnt === 0
        )

        //def cntDone() = (
        //  //cnt === params.intnlFbSize2d.x * params.numBgs
        //  cnt.msb
        //)

        // pixel x-position
        def getCntPxPosX() = cnt(
          (log2Up(params.bgSize2dInPxs.x) + params.numBgsPow - 1)
          downto params.numBgsPow
        )

        //def getCntBgIdx() = (
        //  params.numBgs - 1 - cnt(params.numBgsPow - 1 downto 0)
        //)
      }
      def lineMemArrIdx = stage0.lineMemArrIdx
      def cnt = stage0.cnt
      //def cntMinus1 = stage0.cntMinus1
      def bakCnt = stage0.bakCnt
      def bakCntMinus1 = stage0.bakCntMinus1
      def bgIdx = stage0.bgIdx
      def bakCntWillBeDone() = stage0.bakCntWillBeDone()

      //def cntDone() = stage0.cntDone()
      def getCntPxPosX() = stage0.getCntPxPosX()
      def bgAttrs = stage0.bgAttrs
      //def scroll = bgAttrs.scroll
      //def visib = stage0.bgAttrs.visib
      //def tilePxsCoord = stage0.tilePxsCoord

      // Stages after stage 0
      // NOTE: these pipeline stages are still separate
      case class Stage1() extends Bundle {
        val bgEntryMemIdx = UInt(params.bgEntryMemIdxWidth bits)
        val pxsPos = params.bgPxsCoordT()
      }
      case class Stage2() extends Bundle {
        // `Gpu2dBgEntry`s that have been read
        val bgEntry = Gpu2dBgEntry(params=params)
      }
      case class Stage3() extends Bundle {
        val tilePxsCoord = params.bgTilePxsCoordT()
        // `Gpu2dTile`s that have been read
        val tile = Gpu2dTile(params=params, isObj=false)
      }
      case class Stage4() extends Bundle {
        val palEntryMemIdx = UInt(params.objPalEntryMemIdxWidth bits)
      }
      case class Stage5() extends Bundle {
        // Whether `palEntryMemIdx(someBgIdx)` is non-zero
        val palEntryNzMemIdx = Bool()
      }
      // The following BG pipeline stages are only performed when
      // `palEntryNzMemIdx(someBgIdx)` is `True`
      // `Gpu2dPalEntry`s that have been read
      case class Stage6() extends Bundle {
        val palEntry = Gpu2dPalEntry(params=params)
      }
      case class Stage7() extends Bundle {
        //val doWrite = Bool()
        val lineMemEntry = BgLineMemEntry()
      }
      case class PostStage0() extends Bundle {
        //// scroll
        //val scroll = cloneOf(bgAttrsArr(0).scroll)
        // indices into `bgEntryMem`
        //val bgEntryMemIdxPart0 = Vec2(UInt(params.bgEntryMemIdxWidth bits))
        //val bgEntryMemIdxPart1 = UInt(params.bgEntryMemIdxWidth bits)
        val stage1 = Stage1()
        val stage2 = Stage2()
        //val tileMemIdx = UInt(params.bgTileMemIdxWidth bits)
        val stage3 = Stage3()
        val stage4 = Stage4()
        val stage5 = Stage5()
        val stage6 = Stage6()
        val stage7 = Stage7()
      }
      val postStage0 = PostStage0()

      //def scroll = postStage0.scroll
      def stage1 = postStage0.stage1
      def bgEntryMemIdx = stage1.bgEntryMemIdx
      def pxsPos = stage1.pxsPos
      //def bgEntryMemIdxPart0 = postStage0.bgEntryMemIdxPart0
      //def bgEntryMemIdxPart1 = postStage0.bgEntryMemIdxPart1

      def stage2 = postStage0.stage2
      def bgEntry = stage2.bgEntry

      def stage3 = postStage0.stage3
      def tilePxsCoord = stage3.tilePxsCoord
      def tile = stage3.tile

      //def stage4 = postStage0.stage4

      def stage4 = postStage0.stage4
      def palEntryMemIdx = stage4.palEntryMemIdx

      def stage5 = postStage0.stage5
      def palEntryNzMemIdx = stage5.palEntryNzMemIdx

      def stage6 = postStage0.stage6
      def palEntry = stage6.palEntry

      def stage7 = postStage0.stage7
      //def doWrite = stage7.doWrite
      def lineMemEntry = stage7.lineMemEntry
    }

    def doInitWrBgPipePayload(
      prevLineMemArrIdx: UInt
    ): WrBgPipePayload = {
      val ret = WrBgPipePayload()
      ////ret.lineMemArrIdx := rWrLineMemArrIdx
      ret.lineMemArrIdx := prevLineMemArrIdx + 1
      ret.cnt := 0
      ret.bakCnt := wrBgPipeBakCntStart
      ret.bakCntMinus1 := wrBgPipeBakCntStart - 1
      //ret.bgIdx := (default -> True)
      //ret.scroll := ret.scroll.getZero
      ret.bgAttrs := ret.bgAttrs.getZero
      //ret.tilePxsCoord := ret.tilePxsCoord.getZero
      ret.postStage0 := ret.postStage0.getZero
      ret
    }

    //def wrBgPipeCntStageIdx = 0
    //def wrBgPipeCntMinus1StageIdx = 0
    ////def wrBgPipeBgIdxStageIdx = 0
    //def wrBgPipeBgAttrsStageIdx = 0
    ////def wrBgPipeTilePxsCoordStageIdx = 0

    //def wrBgPipeBgEntryMemIdxPart0StageIdx = 1
    //def wrBgPipeBgEntryMemIdxPart1StageIdx = 2
    //def wrBgPipeBgEntryMemIdxStageIdx = 1
    //def wrBgPipeBgEntryStageIdx = 2
    //def wrBgPipeTilePxsCoordStageIdx = 3
    //def wrBgPipeTileStageIdx = 3
    //def wrBgPipePalEntryMemIdxStageIdx = 4
    //def wrBgPipePalEntryNzMemIdxStageIdx = 5
    //def wrBgPipePalEntryStageIdx = 6
    //def wrBgPipeLineMemEntryStageIdx = 7

    //def wrBgPipeNumMainStages = 8
    def wrBgPipeNumMainStages = 9

    //def wrBgPipeNumStagesPerBg = 8

    //def wrBgPipeSize = (wrBgPipeNumElems * params.numBgs)
    //def wrBgPipeSize = wrBgPipeNumElems + 1

    //def wrBgPipeSize = max(wrBgPipeNumElems, params.numBgs) + 1 
    //def wrBgPipeNumMainStages = wrBgPipeNumStagesPerBg + params.numBgs


    //def wrBgPipeNumElemsGtNumBgs = wrBgPipeNumElems > params.numBgs
    // The sprite-drawing pipeline iterates through the `params.numObjs`
    // sprites, and draws them into `objLineMem` if they're on the current
    // scanline 
    //def wrObjPipeObjAttrsMemIdxWidth = params.objAttrsMemIdxWidth + 1
    //val rWrObjPipeFrontObjAttrsMemIdx = KeepAttribute(
    //  Reg(SInt(wrObjPipeObjAttrsMemIdxWidth bits)) init(0x0)
    //)

    //def wrObjPipeCntWidth = (
    //  params.objTileSize2dPow.x + params.numObjsPow + 1
    //)
    def wrObjPipeBakCntWidth = (
      params.objTileSize2dPow.x + params.objAttrsMemIdxWidth + 1
    )
    def wrObjPipeBakCntStart = (
      (1 << (params.objTileSize2dPow.x + params.objAttrsMemIdxWidth))
      - 1
    )
    case class WrObjPipePayload() extends Bundle {
      // Which sprite are we processing?
      case class Stage0() extends Bundle {
        //val tilePxsCoordXCnt = UInt(params.objTileSize2dPow.x bits)
        //val tilePxsCoordXCntPlus1 = UInt(
        //  (params.objTileSize2dPow.x + 1) bits
        //)
        //val lineMemArrIdx = cloneOf(rWrLineMemArrIdx)
        val lineMemArrIdx = UInt(
          //log2Up(params.numLineMemsPerBgObjRenderer) bits
          lineMemArrIdxWidth bits
        )
        val cnt = UInt(wrObjPipeBakCntWidth bits)
        val bakCnt = UInt(wrObjPipeBakCntWidth bits)
        val bakCntMinus1 = UInt(wrObjPipeBakCntWidth bits)

        //val objAttrsMemIdx = SInt(params.objAttrsMemIdxWidth + 1 bits)
        //val objAttrsMemIdx = cloneOf(rWrObjPipeFrontObjAttrsMemIdx)
        //val objAttrsMemIdx = UInt(params.objAttrsMemIdxWidth bits)

        def objAttrsMemIdx() = bakCnt(
          bakCnt.high - 1
          downto (bakCnt.high - 1 - params.objAttrsMemIdxWidth + 1)
        )
        //def getBakCntTilePxsCoordX() = bakCnt(
        //  (bakCnt.high - 1 - params.objAttrsMemIdxWidth)
        //  downto 0
        //)
        def getCntTilePxsCoordX() = cnt(
          //(cnt.high - 1 - params.objAttrsMemIdxWidth)
          params.objTileSize2dPow.x - 1
          downto 0
        )
        //def getBakCntTilePxsCoordX() = bakCnt

        def bakCntWillBeDone() = (
          //bakCntMinus1.msb
          //cnt + 1 === params.lineMemSize
          //bakCntMinus1 === 0
          bakCnt === 0
        )
      }

      //def objAttrsMemIdxMinus1 = stage0.objAttrsMemIdxMinus1
      //def objAttrsMemIdxWillUnderflow() = (
      //  stage0.objAttrsMemIdxWillUnderflow()
      //)
      case class Stage1() extends Bundle {
        // What are the `Gpu2dObjAttrs` of our sprite? 
        val objAttrs = Gpu2dObjAttrs(params=params)
      }
      case class Stage2() extends Bundle {
        //val tilePxsCoordYPipe1 = UInt(params.objTileSize2dPow.y bits)
        val tilePxsCoord = params.objTilePxsCoordT()
        //val pxsCoordYRangeCheckPipe1 = SInt(
        //  log2Up(params.objPxsCoordSize2dPow.y) bits
        //)
        //val pxPosRangeCheckPipe1 = params.objPxsCoordT()
        val pxPos = params.objPxsCoordT()
        //val pxPosYMinusTileHeightMinus1 = SInt(
        //  log2Up(params.objPxsCoordSize2dPow.y) bits
        //)
        val objPosYShift = SInt(params.objPxsCoordSize2dPow.y bits)
        //val pxPosShiftTopLeft = params.objPxsCoordT()
        val tile = Gpu2dTile(params=params, isObj=true)
      }
      case class Stage3() extends Bundle {
        //val tilePxsCoord = params.objTilePxsCoordT()
        //val pxsCoordYRangeCheck = Bool()
        //val pxPosRangeCheck = Vec2(Bool())
        val pxPosRangeCheckGePipe1 = Vec2(Bool())
        val pxPosRangeCheckLtPipe1 = Vec2(Bool())
        val palEntryMemIdx = UInt(params.objPalEntryMemIdxWidth bits)
      }
      case class Stage4() extends Bundle {
        //val oldPxPosInLineCheckGePipe1 = Vec2(Bool())
        //val oldPxPosInLineCheckLtPipe1 = Vec2(Bool())

        // Whether `palEntryMemIdx` is non-zero
        val palEntryNzMemIdx = Bool()
        val pxPosRangeCheck = Vec2(Bool())

        //val oldPxPosInLineCheck = Vec2(Bool())
      }
      case class Stage5() extends Bundle {
        val palEntry = Gpu2dPalEntry(params=params)
        val pxPosInLine = Bool()

        // "rd..." here means it's been read from `objLineMemArr`
        val rdLineMemEntry = ObjLineMemEntry()
      }
      case class Stage6() extends Bundle {
        val overwriteLineMemEntry = Bool()
      }
      case class Stage7() extends Bundle {
        val wrLineMemEntry = ObjLineMemEntry()
      }
      //case class Stage7() extends Bundle {
      //}
      case class PostStage0() extends Bundle {
        val stage1 = Stage1()
        val stage2 = Stage2()
        val stage3 = Stage3()
        val stage4 = Stage4()
        val stage5 = Stage5()
        val stage6 = Stage6()
        val stage7 = Stage7()
        //val stage7 = Stage7()
      }

      val stage0 = Stage0()
      def lineMemArrIdx = stage0.lineMemArrIdx
      def cnt = stage0.cnt
      def bakCnt = stage0.bakCnt
      def bakCntMinus1 = stage0.bakCntMinus1
      def objAttrsMemIdx() = stage0.objAttrsMemIdx()
      //def getBakCntTilePxsCoordX() = stage0.getBakCntTilePxsCoordX()
      def getCntTilePxsCoordX() = stage0.getCntTilePxsCoordX()
      def bakCntWillBeDone() = stage0.bakCntWillBeDone()

      val postStage0 = PostStage0()

      def stage1 = postStage0.stage1
      def objAttrs = stage1.objAttrs

      def stage2 = postStage0.stage2
      //def tilePxsCoordYPipe1 = stage2.tilePxsCoordYPipe1
      //def pxsCoordYRangeCheckPipe1 = stage2.pxsCoordYRangeCheckPipe1
      def tilePxsCoord = stage2.tilePxsCoord
      //def pxPosRangeCheckPipe1 = stage2.pxPosRangeCheckPipe1
      def pxPos = stage2.pxPos
      //def pxPosYMinusTileHeightMinus1 = stage2.pxPosYMinusTileHeightMinus1
      //def pxPosShiftTopLeft = stage2.pxPosShiftTopLeft
      def objPosYShift = stage2.objPosYShift
      def tile = stage2.tile

      def stage3 = postStage0.stage3
      //def pxPosRangeCheck = stage3.pxPosRangeCheck
      //def tilePxsCoord = stage3.tilePxsCoord
      def pxPosRangeCheckGePipe1 = stage3.pxPosRangeCheckGePipe1
      def pxPosRangeCheckLtPipe1 = stage3.pxPosRangeCheckLtPipe1
      def palEntryMemIdx = stage3.palEntryMemIdx

      def stage4 = postStage0.stage4
      //def oldPxPosInLineCheckGePipe1 = stage4.oldPxPosInLineCheckGePipe1
      //def oldPxPosInLineCheckLtPipe1 = stage4.oldPxPosInLineCheckLtPipe1
      //def palEntryMemIdx = stage4.palEntryMemIdx
      def palEntryNzMemIdx = stage4.palEntryNzMemIdx
      def pxPosRangeCheck = stage4.pxPosRangeCheck

      def stage5 = postStage0.stage5
      //def palEntryNzMemIdx = stage5.palEntryNzMemIdx
      //def oldPxPosInLineCheck = stage5.oldPxPosInLineCheck
      def palEntry = stage5.palEntry
      def pxPosInLine = stage5.pxPosInLine
      def rdLineMemEntry = stage5.rdLineMemEntry

      def stage6 = postStage0.stage6
      def overwriteLineMemEntry = stage6.overwriteLineMemEntry

      def stage7 = postStage0.stage7
      def wrLineMemEntry = stage7.wrLineMemEntry
      //def palEntry = stage6.palEntry
      //def pxPosInLine = stage6.pxPosInLine
      //def rdLineMemEntry = stage6.rdLineMemEntry

      //def stage7 = postStage0.stage7
      //def wrLineMemEntry = stage7.wrLineMemEntry

      //def rdLineMemEntry = postStage0.rdLineMemEntry
      //def wrLineMemEntry = postStage0.wrLineMemEntry
    }
    def doInitWrObjPipePayload(
      prevLineMemArrIdx: UInt
    ): WrObjPipePayload = {
      val ret = WrObjPipePayload()
      ////ret.lineMemArrIdx := rWrLineMemArrIdx
      ret.lineMemArrIdx := prevLineMemArrIdx + 1
      ret.cnt := 0
      ret.bakCnt := wrObjPipeBakCntStart
      ret.bakCntMinus1 := wrObjPipeBakCntStart - 1
      //ret.objAttrsMemIdx := ret.cnt(
      //  ret.cnt.high
      //  downto (
      //    ret.cnt.high - ret.objAttrsMemIdx.getWidth + 1
      //  )
      //)
      //ret.objAttrsMemIdx := (1 << (ret.objAttrsMemIdx.getWidth - 1)) - 1
      //ret.objAttrsMemIdxMinus1 := (
      //  (1 << (ret.objAttrsMemIdx.getWidth - 1)) - 2
      //)
      ret.postStage0 := ret.postStage0.getZero
      ret
    }

    ////def wrObjPipeCntStageIdx = 0
    ////def wrObjPipeCntMinus1StageIdx = 0

    ////def wrObjPipeObjAttrsStageIdx = 1

    ////def wrObjPipeTilePxsCoordStageIdx = 2
    ////def wrObjPipeTileStageIdx = 2
    ////def wrObjPipePalEntryMemIdxStageIdx = 2

    ////def wrObjPipePxPosInLineCheckGePipe1StageIdx = 3
    ////def wrObjPipePxPosInLineCheckLtPipe1StageIdx = 3
    ////def wrObjPipePalEntryNzMemIdxStageIdx = 3

    ////def wrObjPipePxPosInLineCheckStageIdx = 4
    ////def wrObjPipePalEntryStageIdx = 4

    ////def wrObjPipePxPosInLineStageIdx = 5
    ////def wrObjPipeRdLineMemEntryStageIdx = 5

    ////def wrObjPipeWrLineMemEntryStageIdx = 6

    //def wrObjPipeCntStageIdx = 0
    //def wrObjPipeCntMinus1StageIdx = 0

    //def wrObjPipeObjAttrsStageIdx = 1

    //def wrObjPipeTilePxsCoordYPipe1StageIdx = 2
    //def wrObjPipeTileStageIdx = 2

    //def wrObjPipeTilePxsCoordStageIdx = 3

    //def wrObjPipePxPosInLineCheckGePipe1StageIdx = 4
    //def wrObjPipePxPosInLineCheckLtPipe1StageIdx = 4
    //def wrObjPipePalEntryMemIdxStageIdx = 4

    //def wrObjPipePalEntryNzMemIdxStageIdx = 5
    //def wrObjPipePxPosInLineCheckStageIdx = 5

    //def wrObjPipePalEntryStageIdx = 6
    //def wrObjPipePxPosInLineStageIdx = 6
    //def wrObjPipeRdLineMemEntryStageIdx = 6

    //def wrObjPipeWrLineMemEntryStageIdx = 7

    //def wrObjPipeNumMainStages = 8
    def wrObjPipeNumMainStages = 9

    def wrBgObjPipeNumStages = max(
      wrBgPipeNumMainStages,
      wrObjPipeNumMainStages
    ) + 1

    //def wrObjPipeSize = wrObjPipeNumElems * params.numObjsPow
    //def wrObjPipeSize = max(wrObjPipeNumElems, params.numObjsPow) + 1 
    //def wrObjPipeSize = wrObjPipeNumMainStages + params.numObjsPow
    //def wrObjPipeNumElemsGtNumObjsPow = (
    //  wrObjPipeNumElems > params.numObjsPow
    //)

    def sendIntoFifoCntWidth = log2Up(params.lineMemSize) + 2
    def sendIntoFifoBakCntStart = params.lineMemSize - 1

    case class SendIntoFifoPipePayload() extends Bundle {

      case class Stage0() extends Bundle {
        val lineMemArrIdx = UInt(
          //log2Up(params.numLineMemsPerBgObjRenderer) bits
          lineMemArrIdxWidth bits
        )
        val cnt = UInt(sendIntoFifoCntWidth bits)
        val bakCnt = UInt(sendIntoFifoCntWidth bits)
        val bgLineMemEntry = BgLineMemEntry()
        //val objLineMemEntry = ObjLineMemEntry()

        def bakCntWillBeDone() = (
          bakCnt === 0
        )
      }
      //case class Stage1() extends Bundle {
      //}

      //case class Stage1() extends Bundle {
      //  val 
      //}

      //case class PostStage0() extends Bundle {
      //  val stage1 = Stage1()
      //}

      val stage0 = Stage0()
      def lineMemArrIdx = stage0.lineMemArrIdx
      def cnt = stage0.cnt
      def bakCnt = stage0.bakCnt
      def bgLineMemEntry = stage0.bgLineMemEntry
      val objLineMemEntry = stage0.objLineMemEntry

      def bakCntWillBeDone() = stage0.bakCntWillBeDone()
    }

    //case class SendIntoObjFifoPipePayload() extends Bundle {

    //  case class Stage0() extends Bundle {
    //    val lineMemArrIdx = UInt(
    //      //log2Up(params.numLineMemsPerBgObjRenderer) bits
    //      lineMemArrIdxWidth bits
    //    )
    //    val cnt = UInt(sendIntoFifoCntWidth bits)
    //    val bakCnt = UInt(sendIntoFifoCntWidth bits)
    //    //val bgLineMemEntry = BgLineMemEntry()
    //    val objLineMemEntry = ObjLineMemEntry()

    //    def bakCntWillBeDone() = (
    //      bakCnt === 0
    //    )
    //  }
    //  //case class Stage1() extends Bundle {
    //  //}

    //  //case class Stage1() extends Bundle {
    //  //  val 
    //  //}

    //  //case class PostStage0() extends Bundle {
    //  //  val stage1 = Stage1()
    //  //}

    //  val stage0 = Stage0()
    //  def lineMemArrIdx = stage0.lineMemArrIdx
    //  def cnt = stage0.cnt
    //  def bakCnt = stage0.bakCnt
    //  //val bgLineMemEntry = stage0.bgLineMemEntry
    //  def objLineMemEntry = stage0.objLineMemEntry

    //  def bakCntWillBeDone() = stage0.bakCntWillBeDone()
    //}

    def doInitSendIntoFifoPipePayload(
      prevLineMemArrIdx: UInt,
    ): SendIntoFifoPipePayload = {
      //val ret = SendIntoFifoPipePayload()
      val ret = SendIntoFifoPipePayload()
      ret.lineMemArrIdx := prevLineMemArrIdx + 1
      ret.cnt := 0
      ret.bakCnt := sendIntoFifoBakCntStart
      ret.bgLineMemEntry := ret.bgLineMemEntry.getZero
      //ret.objLineMemEntry := ret.objLineMemEntry.getZero
      ret
    }
    //def doInitSendIntoObjFifoPipePayload(
    //  prevLineMemArrIdx: UInt,
    //): SendIntoObjFifoPipePayload = {
    //  //val ret = SendIntoObjFifoPipePayload()
    //  val ret = SendIntoObjFifoPipePayload()
    //  ret.lineMemArrIdx := prevLineMemArrIdx + 1
    //  ret.cnt := 0
    //  ret.bakCnt := sendIntoFifoBakCntStart
    //  //ret.bgLineMemEntry := ret.bgLineMemEntry.getZero
    //  ret.objLineMemEntry := ret.objLineMemEntry.getZero
    //  ret
    //}

    //def sendIntoFifoPipeNumMainStages = 2
    def sendIntoFifoPipeNumMainStages = 1


    def combinePipeCntWidth = (
      log2Up(params.intnlFbSize2d.x + 1) + 2
    )
    def combinePipeBakCntStart = (
      params.intnlFbSize2d.x - 1
    )
    case class CombinePipePayload() extends Bundle {
      //--------
      //val bg = WrBgPipePayload()
      //val obj = WrObjPipePayload()
      case class Stage0() extends Bundle {
        //--------
        //val rdLineMemArrIdx = cloneOf(rCombineRdLineMemArrIdx)
        //val wrLineMemArrIdx = cloneOf(rCombineWrLineMemArrIdx)
        //val rdLineMemArrIdx = UInt(
        //  log2Up(params.numLineMemsPerBgObjRenderer) bits
        //)
        //val wrLineMemArrIdx = cloneOf(rdLineMemArrIdx)
        // combining can be done backwards
        val cnt = UInt(combinePipeCntWidth bits)
        val bakCnt = UInt(combinePipeCntWidth bits)
        val bakCntMinus1 = UInt(combinePipeCntWidth bits)
        //--------
        def bakCntWillBeDone() = (
          //bakCntMinus1.msb
          //bakCntMinus1 === 0
          bakCnt === 0
          //cnt + 1 === params.lineMemSize
        )
        //--------
      }
      val stage0 = Stage0()
      //def rdLineMemArrIdx = stage0.rdLineMemArrIdx
      //def wrLineMemArrIdx = stage0.wrLineMemArrIdx
      def cnt = stage0.cnt
      def bakCnt = stage0.bakCnt
      def bakCntMinus1 = stage0.bakCntMinus1
      def bakCntWillBeDone() = stage0.bakCntWillBeDone()

      case class Stage1() extends Bundle {
        val bgRdLineMemEntry = BgLineMemEntry()
        val objRdLineMemEntry = ObjLineMemEntry()
      }
      case class Stage2() extends Bundle {
        val objHiPrio = Bool()
      }
      case class Stage3() extends Bundle {
        //val isObj = Bool()
        val col = Rgb(params.rgbConfig)
      }
      case class Stage4() extends Bundle {
        val combineWrLineMemEntry = BgLineMemEntry()
        //val objWrLineMemEntry = ObjLineMemEntry()
      }
      case class PostStage0() extends Bundle {
        val stage1 = Stage1()
        val stage2 = Stage2()
        val stage3 = Stage3()
        val stage4 = Stage4()
      }
      val postStage0 = PostStage0()

      def stage1 = postStage0.stage1
      def bgRdLineMemEntry = stage1.bgRdLineMemEntry
      def objRdLineMemEntry = stage1.objRdLineMemEntry

      def stage2 = postStage0.stage2
      def objHiPrio = stage2.objHiPrio

      def stage3 = postStage0.stage3
      def col = stage3.col

      def stage4 = postStage0.stage4
      def combineWrLineMemEntry = stage4.combineWrLineMemEntry
      //--------
    }
    def doInitCombinePipePayload(
      //prevRdLineMemArrIdx: UInt,
      //prevWrLineMemArrIdx: UInt,
    ): CombinePipePayload = {
      val ret = CombinePipePayload()
      ////ret.rdLineMemArrIdx := rCombineRdLineMemArrIdx
      ////ret.wrLineMemArrIdx := rCombineWrLineMemArrIdx
      //ret.rdLineMemArrIdx := prevRdLineMemArrIdx + 1
      //ret.wrLineMemArrIdx := prevWrLineMemArrIdx + 1
      ret.cnt := 0
      ret.bakCnt := combinePipeBakCntStart
      ret.bakCntMinus1 := combinePipeBakCntStart - 1
      ret.postStage0 := ret.postStage0.getZero
      ret
    }
    def combinePipeNumMainStages = 5


    //def rdPipeCntWidth = (
    //  log2Up(params.intnlFbSize2d.x + 1) + params.physFbSize2dScalePow.x
    //  + 2
    //)
    //def rdPipeBakCntStart = (
    //  //params.intnlFbSize2d.x - 1
    //  (
    //    1 
    //    << (
    //      log2Up(params.intnlFbSize2d.x) + params.physFbSize2dScalePow.x
    //    )
    //  ) - 1
    //)
    //case class RdPipePayload() extends Bundle {
    //  //--------
    //  //val bg = WrBgPipePayload()
    //  //val obj = WrObjPipePayload()
    //  case class Stage0() extends Bundle {
    //    //--------
    //    //val lineMemArrIdx = cloneOf(rRdLineMemArrIdx)
    //    //val lineMemArrIdx = UInt(
    //    //  log2Up(params.numLineMemsPerBgObjRenderer) bits
    //    //)
    //    val cnt = UInt(rdPipeCntWidth bits)
    //    val bakCnt = UInt(rdPipeCntWidth bits)
    //    val bakCntMinus1 = UInt(rdPipeCntWidth bits)
    //    val tempCntSlice = UInt(log2Up(params.lineMemSize) bits)
    //    //tmpCntSlice :=
    //    //tempCntSlice := cnt(
    //    //  (tempCntSlice.high + params.physFbSize2dScalePow.x)
    //    //  downto params.physFbSize2dScalePow.x
    //    //)
    //    //--------
    //    def getCntPxPosX() = {
    //      //cnt(
    //      //  (
    //      //    log2Up(params.intnlFbSize2d.x) + params.physFbSize2dScalePow.x
    //      //    - 1
    //      //  ) downto params.physFbSize2dScalePow.x
    //      //)
    //      //val tempCntSlice = UInt(log2Up(params.lineMemSize) bits)
    //      tempCntSlice
    //    }
    //    def bakCntWillBeDone() = (
    //      //bakCntMinus1.msb
    //      bakCnt === 0
    //    )
    //    //--------
    //  }
    //  val stage0 = Stage0()
    //  //def lineMemArrIdx = stage0.lineMemArrIdx
    //  def cnt = stage0.cnt
    //  //def cntMinus1 = stage0.cntMinus1
    //  def bakCnt = stage0.bakCnt
    //  def bakCntMinus1 = stage0.bakCntMinus1
    //  def tempCntSlice = stage0.tempCntSlice
    //  def getCntPxPosX() = stage0.getCntPxPosX()
    //  def bakCntWillBeDone() = stage0.bakCntWillBeDone()

    //  case class Stage1() extends Bundle {
    //    //val bgRdLineMemEntry = BgLineMemEntry()
    //    //val objRdLineMemEntry = ObjLineMemEntry()
    //    val bgLineMemEntry = BgLineMemEntry()
    //  }
    //  case class PostStage0() extends Bundle {
    //    val stage1 = Stage1()
    //  }
    //  val postStage0 = PostStage0()

    //  def stage1 = postStage0.stage1
    //  def bgLineMemEntry = stage1.bgLineMemEntry
    //}
    //def doInitRdPipePayload(
    //  prevLineMemArrIdx: UInt
    //): RdPipePayload = {
    //  val ret = RdPipePayload()
    //  ////ret.lineMemArrIdx := rRdLineMemArrIdx
    //  //ret.lineMemArrIdx := prevLineMemArrIdx + 1
    //  //ret.cnt := rdPipeCntStart
    //  //ret.cntMinus1 := rdPipeCntStart - 1
    //  ret.cnt := 0
    //  //ret.cnt := (default -> True)
    //  ret.bakCnt := rdPipeBakCntStart
    //  ret.bakCntMinus1 := rdPipeBakCntStart - 1
    //  ret.tempCntSlice := 0
    //  //ret.bakCnt := rdPipeBakCntStart
    //  //ret.bakCntMinus1 := rdPipeBakCntStart - 1
    //  ret.postStage0 := ret.postStage0.getZero
    //  ret
    //}
    //def rdPipeNumMainStages = 2
    //--------
    //def wrPipeSize = wrBgPipeSize + wrObjPipeSize
    val wrBgPipeIn = KeepAttribute(
      //Vec.fill(wrBgObjPipeNumStages)(Reg(WrBgPipePayload()))
      Vec.fill(wrBgObjPipeNumStages)(
        //Flow(WrBgPipePayload())
        Stream(WrBgPipePayload())
      )
    )
    val wrBgPipeOut = KeepAttribute(
      //Vec.fill(wrBgObjPipeNumStages)(Reg(WrBgPipePayload()))
      Vec.fill(wrBgObjPipeNumStages)(
        //Flow(WrBgPipePayload())
        Stream(WrBgPipePayload())
      )
    )
    wrBgPipeOut.last.ready := (
      // This is a heuristic!
      //lineFifo.io.misc.amountCanPush > 8
      lineFifo.io.misc.amountCanPush
      > params.wrBgObjStallFifoAmountCanPush
    )
    //val wrBgPipeLast = KeepAttribute(
    //  //Flow(WrBgPipePayload())
    //  Stream(WrBgPipePayload())
    //)

    //when (nextIntnlChangingRow)
    //when (rIntnlChangingRow)
    //when (intnlChangingRowRe)
    //{
    //  nextWrBgChangingRow := False
    //} elsewhen (
    //  wrBgPipeLast.bakCntWillBeDone() && wrBgPipeLast.fire
    //  //(wrBgPipeLast.cnt + 1 === params.lineMemSize) && wrBgPipeLast.fire
    //  //wrBgPipeLast.bakCnt.msb && wrBgPipeLast.fire
    //) {
    //  nextWrBgChangingRow := True
    //} otherwise {
    //  nextWrBgChangingRow := rWrBgChangingRow
    //}
    //elsewhen (
    //  wrBgPipeLast.bakCntWillBeDone()
    //  //wrBgPipeLast.bakCnt.msb
    //) {
    //  nextWrBgChangingRow := True
    //} otherwise {
    //  nextWrBgChangingRow := rWrBgChangingRow
    //}

    //val rWrBgPipePayloadVec = KeepAttribute(
    //  Vec.fill(wrBgObjPipeNumStages)(
    //    Reg(WrBgPipePayload()) init(wrBgPipe(0).payload.getZero)
    //  )
    //)
    //for (idx <- 0 to wrBgObjPipeNumStages - 1) {
    //  //rWrBgPipe(idx).init(
    //  //  //rWrBgPipe(idx).getZero
    //  //  doInitWrBgPipePayload()
    //  //)
    //}
    //wrBgPipe(0).valid := True
    //val rWrBgPipeFrontValid = Reg(Bool()) init(False)
    val rWrBgPipeFrontValid = Reg(Bool()) init(True)
    val rWrBgPipeFrontPayload = Reg(WrBgPipePayload())
    //rWrBgPipeFrontPayload.init(rWrBgPipeFrontPayload.getZero)
    rWrBgPipeFrontPayload.init(doInitWrBgPipePayload(
      prevLineMemArrIdx=wrLineMemArrIdxInit
    ))

    wrBgPipeIn(0).valid := rWrBgPipeFrontValid
    wrBgPipeIn(0).payload := rWrBgPipeFrontPayload
    for (idx <- 1 to wrBgPipeIn.size - 1) {
      // Create pipeline registering
      //wrBgPipeIn(idx) <-< wrBgPipeOut(idx - 1)
      wrBgPipeIn(idx) <-/< wrBgPipeOut(idx - 1)
    }
    for (idx <- 0 to wrBgPipeOut.size - 1) {
      // Connect output `valid` to input `valid`
      wrBgPipeOut(idx).valid := wrBgPipeIn(idx).valid
      wrBgPipeIn(idx).ready := wrBgPipeOut(idx).ready
    }

    //val wrBgPop = PipeSkidBuf(
    //  //dataType=WrBgPipePayload(),
    //  dataType=BgLineMemEntry(),
    //  //optIncludeBusy=true,
    //  //optUseOldCode=true,
    //)

    //wrBgPop.io.misc.clear := clockDomain.isResetActive
    //val tempBgLineFifoPush = Stream(BgLineMemEntry())
    //lineFifo.io.push << tempBgLineFifoPush

    ////sbPop.io.misc.busy := (
    ////  //!(combinePipeLast.fire || rCombinePipeLastDidFire)
    ////  !rCombinePipeOutLastDidFire
    ////)
    //wrBgPop.io.connectParentStreams(
    //  //push=combinePipeLast,
    //  //push=combinePipeOut.last,
    //  push=wrBgPipeOut.last,
    //  //pop=objLineFifo.io.push,
    //  pop=tempBgLineFifoPush,
    //)(
    //  payloadConnFunc=(
    //    bgLineFifoPushPayload,
    //    wrBgPipeOutLastPayload,
    //  ) => {
    //    //when (rPsbClear) {
    //    //  //popPayload.col := popPayload.col.getZero
    //    //  popPayload := popPayload.getZero
    //    //} otherwise {
    //      //popPayload := outp
    //    //}
    //    when (wrBgPipeOutLastPayload.doWrite) {
    //      bgLineFifoPushPayload := wrBgPipeOutLastPayload.lineMemEntry
    //    } otherwise {
    //      bgLineFifoPushPayload := bgLineFifoPushPayload.getZero
    //    }
    //  }
    //)
    // add one final register
    //wrBgPipeLast <-< wrBgPipeOut.last

    // Send data into the background pipeline
    //when (nextIntnlChangingRow) {
    //  rWrBgChangingRow := False
    //}
    //when (!rWrBgPipeFrontValid) {
    //  when (
    //    //rIntnlChangingRow
    //    intnlChangingRowRe


    //    //nextIntnlChangingRowRe
    //    //nextWrBgChangingRow && !rWrBgChangingRow
    //    //outp.physPosInfo.changingRow
    //    //changingRowRe
    //  ) {
    //    rWrBgPipeFrontValid := True
    //    rWrBgPipeFrontPayload := doInitWrBgPipePayload(
    //      //prevLineMemArrIdx=rWrBgPipeFrontPayload.lineMemArrIdx
    //      //prevLineMemArrIdx=0
    //    )
    //    //rWrBgChangingRow := False
    //  }
    //} otherwise { // when (rWrBgPipeFrontValid)
      when (wrBgPipeIn(0).fire) {
        ////rWrBgPipeFrontPayload.cnt := rWrBgPipeFrontPayload.cnt + 1
        //rWrBgPipeFrontPayload.cnt := rWrBgPipeFrontPayload.cnt + 1
        //rWrBgPipeFrontPayload.bakCnt := rWrBgPipeFrontPayload.bakCnt - 1
        //rWrBgPipeFrontPayload.bakCntMinus1 := (
        //  rWrBgPipeFrontPayload.bakCntMinus1 - 1
        //)
        ////rWrBgPipeFrontPayload.bgIdx := rWrBgPipeFrontPayload.bgIdx - 1
        ////rWrBgPipeFrontPayload.bgIdx := (
        ////  rWrBgPipeFrontPayload.cntMinus1(
        ////    rWrBgPipeFrontPayload.cnt.high
        ////    downto rWrBgPipeFrontPayload.cnt.high - wrBgPipeBgIdxWidth + 1
        ////  )
        ////)
        // BEGIN: test logic
        when (
          rWrBgPipeFrontPayload.bakCntWillBeDone()
          //rWrBgPipeFrontPayload.cnt + 1 === params.lineMemSize
        ) {
          //rWrBgPipeFrontValid := False
          //rWrBgChangingRow := wrBgPipeLast.bakCntWillBeDone()
          //rWrBgChangingRow := True
          rWrBgPipeFrontPayload := doInitWrBgPipePayload(
            prevLineMemArrIdx=rWrBgPipeFrontPayload.lineMemArrIdx,
          )
          rWrBgLineNum := rWrBgLineNumPipe1(rWrBgLineNum.bitsRange)
        } otherwise {
          rWrBgPipeFrontPayload.cnt := rWrBgPipeFrontPayload.cnt + 1
          rWrBgPipeFrontPayload.bakCnt := rWrBgPipeFrontPayload.bakCnt - 1
          rWrBgPipeFrontPayload.bakCntMinus1 := (
            rWrBgPipeFrontPayload.bakCntMinus1 - 1
          )
        }
        // END: test logic
      }
      // BEGIN: old logic
      //when (rWrBgPipeFrontPayload.bakCntWillBeDone()) {
      //  rWrBgPipeFrontValid := False
      //  //rWrBgChangingRow := wrBgPipeLast.bakCntWillBeDone()
      //  //rWrBgChangingRow := True
      //}
      // END: old logic
      //when (wrBgPipeIn(0).fire) {
      //  ////rWrBgPipeFrontPayload.cnt := rWrBgPipeFrontPayload.cnt + 1
      //  //rWrBgPipeFrontPayload.cnt := rWrBgPipeFrontPayload.cnt + 1
      //  //rWrBgPipeFrontPayload.bakCnt := rWrBgPipeFrontPayload.bakCnt - 1
      //  //rWrBgPipeFrontPayload.bakCntMinus1 := (
      //  //  rWrBgPipeFrontPayload.bakCntMinus1 - 1
      //  //)
      //  ////rWrBgPipeFrontPayload.bgIdx := rWrBgPipeFrontPayload.bgIdx - 1
      //  ////rWrBgPipeFrontPayload.bgIdx := (
      //  ////  rWrBgPipeFrontPayload.cntMinus1(
      //  ////    rWrBgPipeFrontPayload.cnt.high
      //  ////    downto rWrBgPipeFrontPayload.cnt.high - wrBgPipeBgIdxWidth + 1
      //  ////  )
      //  //)
      //  when (rWrBgPipeFrontPayload.bakCntWillBeDone()) {
      //    rWrBgPipeFrontValid := False
      //    //rWrBgChangingRow := wrBgPipeLast.bakCntWillBeDone()
      //    //rWrBgChangingRow := True
      //  } otherwise {
      //    rWrBgPipeFrontPayload.cnt := rWrBgPipeFrontPayload.cnt + 1
      //    rWrBgPipeFrontPayload.bakCnt := rWrBgPipeFrontPayload.bakCnt - 1
      //    rWrBgPipeFrontPayload.bakCntMinus1 := (
      //      rWrBgPipeFrontPayload.bakCntMinus1 - 1
      //    )
      //  }
      //}
    //}

    val wrObjPipeIn = KeepAttribute(
      //Vec.fill(wrBgObjPipeNumStages)(Reg(WrObjPipePayload()))
      Vec.fill(wrBgObjPipeNumStages)(
        //Flow(WrObjPipePayload())
        Stream(WrObjPipePayload())
      )
    )
    val wrObjPipeOut = KeepAttribute(
      //Vec.fill(wrBgObjPipeNumStages)(Reg(WrObjPipePayload()))
      Vec.fill(wrBgObjPipeNumStages)(
        //Flow(WrObjPipePayload())
        Stream(WrObjPipePayload())
      )
    )
    wrObjPipeOut.last.ready := (
      // This is a heuristic!
      objLineFifo.io.misc.amountCanPush
      > params.wrBgObjStallFifoAmountCanPush
    )


    //val wrObjPipeLast = KeepAttribute(
    //  //Flow(WrObjPipePayload())
    //  Stream(WrObjPipePayload())
    //)
    //when (nextIntnlChangingRow && rWrObjChangingRow) 
    //when (rIntnlChangingRow)
    //when (intnlChangingRowRe) 
    //{
    //  nextWrObjChangingRow := False
    //} elsewhen (
    //  wrObjPipeLast.bakCntWillBeDone() && wrObjPipeLast.fire
    //  //(wrObjPipeLast.cnt + 1 === params.lineMemSize) && wrObjPipeLast.fire
    //  //wrObjPipeLast.bakCnt.msb && wrObjPipeLast.fire
    //  //wrObjPipeLast.bakCnt.msb
    //) {
    //  nextWrObjChangingRow := True
    //} otherwise {
    //  nextWrObjChangingRow := rWrObjChangingRow
    //}
    //val rWrObjPipePayloadVec = KeepAttribute(
    //  Vec.fill(wrBgObjPipeNumStages)(
    //    Reg(WrObjPipePayload()) init(wrObjPipe(0).payload.getZero)
    //  )
    //)

    //for (idx <- 0 to wrBgObjPipeNumStages - 1) {
    //  //rWrObjPipe(idx).init(rWrObjPipe(idx).getZero)
    //}
    //wrObjPipe(0).valid := True
    val rWrObjPipeFrontValid = Reg(Bool()) init(True)
    //when (wrObjPipe(0).objAttrsMemIdxWillUnderflow()) {
    //  rWrObjPipeFrontValid := False
    //}
    val rWrObjPipeFrontPayload = Reg(WrObjPipePayload())
    //rWrObjPipeFrontPayload.init(rWrObjPipeFrontPayload.getZero)
    rWrObjPipeFrontPayload.init(doInitWrObjPipePayload(
      prevLineMemArrIdx=wrLineMemArrIdxInit
    ))

    wrObjPipeIn(0).valid := rWrObjPipeFrontValid
    wrObjPipeIn(0).payload := rWrObjPipeFrontPayload
    //for (idx <- 1 to wrObjPipeIn.size - 1) {
    //  wrObjPipeIn(idx) <-< wrObjPipeOut(idx - 1)
    //}
    for (idx <- 1 to wrObjPipeIn.size - 1) {
      // Create pipeline registering
      //wrObjPipeIn(idx) <-< wrObjPipeOut(idx - 1)
      wrObjPipeIn(idx) <-/< wrObjPipeOut(idx - 1)
    }
    for (idx <- 0 to wrObjPipeOut.size - 1) {
      // Connect output `valid` to input `valid`
      wrObjPipeOut(idx).valid := wrObjPipeIn(idx).valid
      wrObjPipeIn(idx).ready := wrObjPipeOut(idx).ready
    }
    // add one final register
    //wrObjPipeLast <-< wrObjPipeOut.last
    //wrObjPipe.last.ready := True

    // Send data into the sprite pipeline
    //when (nextIntnlChangingRow) {
    //  rWrObjChangingRow := False
    //}
    //when (!rWrObjPipeFrontValid) {
    //  when (
    //    //rIntnlChangingRow
    //    intnlChangingRowRe
    //    //outp.physPosInfo.changingRow
    //    //changingRowRe
    //    //nextIntnlChangingRowRe
    //    //nextWrObjChangingRow && !rWrObjChangingRow
    //  ) {
    //    rWrObjPipeFrontValid := True
    //    rWrObjPipeFrontPayload := doInitWrObjPipePayload(
    //      //prevLineMemArrIdx=rWrObjPipeFrontPayload.lineMemArrIdx
    //      //prevLineMemArrIdx=0
    //    )
    //    //rWrObjChangingRow := False
    //  }
    //} otherwise { // when (rWrObjPipeFrontValid)
      when (
        wrObjPipeIn(0).fire
      ) {
        //rWrObjPipeFrontPayload.cnt := rWrObjPipeFrontPayload.cnt + 1
        //rWrObjPipeFrontPayload.bakCnt := (
        //  rWrObjPipeFrontPayload.bakCnt - 1
        //)
        //rWrObjPipeFrontPayload.bakCntMinus1 := (
        //  rWrObjPipeFrontPayload.bakCntMinus1 - 1
        //)
        when (
          rWrObjPipeFrontPayload.bakCntWillBeDone()
          //rWrObjPipeFrontPayload.cnt + 1 === params.lineMemSize
        ) {
          //rWrObjPipeFrontValid := False
          //rWrObjChangingRow := True
          rWrObjPipeFrontPayload := doInitWrObjPipePayload(
            prevLineMemArrIdx=rWrObjPipeFrontPayload.lineMemArrIdx
          )
          rWrObjLineNum := rWrObjLineNumPipe1(rWrObjLineNum.bitsRange)
        } otherwise {
          rWrObjPipeFrontPayload.cnt := rWrObjPipeFrontPayload.cnt + 1
          rWrObjPipeFrontPayload.bakCnt := (
            rWrObjPipeFrontPayload.bakCnt - 1
          )
          rWrObjPipeFrontPayload.bakCntMinus1 := (
            rWrObjPipeFrontPayload.bakCntMinus1 - 1
          )
        }
      }
      //when (rWrObjPipeFrontPayload.objAttrsMemIdxWillUnderflow()) {
      //  rWrObjPipeFrontValid := False
      //}
    //}

    val sendIntoFifoPipeIn = KeepAttribute(
      Vec.fill(sendIntoFifoPipeNumMainStages)(
        Stream(SendIntoFifoPipePayload())
      )
    )
    val sendIntoFifoPipeOut = KeepAttribute(
      Vec.fill(sendIntoFifoPipeNumMainStages)(
        Stream(SendIntoFifoPipePayload())
      )
    )
    for (idx <- 1 to sendIntoFifoPipeNumMainStages - 1) {
      sendIntoFifoPipeIn(idx) <-/< sendIntoFifoPipeOut(idx - 1)
    }
    for (idx <- 0 to sendIntoFifoPipeNumMainStages - 1) {
      sendIntoFifoPipeOut(idx).valid := sendIntoFifoPipeIn(idx).valid
      sendIntoFifoPipeIn(idx).ready := sendIntoFifoPipeOut(idx).ready
    }
    val rSendIntoFifoPipeFrontValid = Reg(Bool()) init(True)
    val rSendIntoFifoPipeFrontPayload = Reg(SendIntoFifoPipePayload())
    rSendIntoFifoPipeFrontPayload.init(
      //rSendIntoFifoPipeFrontPayload.getZero
      doInitSendIntoFifoPipePayload(
        prevLineMemArrIdx=sendIntoFifoLineMemArrIdxInit
      )
    )

    sendIntoFifoPipeIn(0).valid := rSendIntoFifoPipeFrontValid
    sendIntoFifoPipeIn(0).payload := rSendIntoFifoPipeFrontPayload
    sendIntoFifoPipeOut.last.translateInto(
      lineFifo.io.push
    )(
      dataAssignment=(
        bgLineFifoPushPayload,
        sendIntoFifoPipePayload
      ) => {
        bgLineFifoPushPayload := (
          sendIntoFifoPipePayload.bgLineMemEntry
        )
      }
    )
    //lineFifo.io.
    //lineFifo.io.

    when (sendIntoFifoPipeIn(0).fire) {
      // BEGIN: test logic

      when (
        rSendIntoFifoPipeFrontPayload.bakCntWillBeDone()
        //rSendIntoFifoPipeFrontPayload.cnt + 1 === params.lineMemSize
      ) {
        //rSendIntoFifoPipeFrontValid := False
        //rSendIntoFifoChangingRow := True
        rSendIntoFifoPipeFrontPayload := doInitSendIntoFifoPipePayload(
          //prevLineMemArrIdx=0
          prevLineMemArrIdx=rSendIntoFifoPipeFrontPayload.lineMemArrIdx
        )
      } otherwise {
        rSendIntoFifoPipeFrontPayload.cnt := (
          rSendIntoFifoPipeFrontPayload.cnt + 1
        )
        rSendIntoFifoPipeFrontPayload.bakCnt := (
          rSendIntoFifoPipeFrontPayload.bakCnt - 1
        )
        rSendIntoFifoPipeFrontPayload.bgLineMemEntry := (
          rSendIntoFifoPipeFrontPayload.bgLineMemEntry.getZero
        )
        //rSendIntoFifoPipeFrontPayload.bakCntMinus1 := (
        //  rSendIntoFifoPipeFrontPayload.bakCntMinus1 - 1
        //)
      }
      // END: test logic
    }

    val sendIntoObjFifoPipeIn = KeepAttribute(
      Vec.fill(sendIntoFifoPipeNumMainStages)(
        Stream(SendIntoObjFifoPipePayload())
      )
    )
    val sendIntoObjFifoPipeOut = KeepAttribute(
      Vec.fill(sendIntoFifoPipeNumMainStages)(
        Stream(SendIntoObjFifoPipePayload())
      )
    )
    for (idx <- 1 to sendIntoFifoPipeNumMainStages - 1) {
      sendIntoObjFifoPipeIn(idx) <-/< sendIntoObjFifoPipeOut(idx - 1)
    }
    for (idx <- 0 to sendIntoFifoPipeNumMainStages - 1) {
      sendIntoObjFifoPipeOut(idx).valid := (
        sendIntoObjFifoPipeIn(idx).valid
      )
      sendIntoObjFifoPipeIn(idx).ready := (
        sendIntoObjFifoPipeOut(idx).ready
      )
    }
    val rSendIntoObjFifoPipeFrontValid = Reg(Bool()) init(True)
    val rSendIntoObjFifoPipeFrontPayload = Reg(
      SendIntoObjFifoPipePayload()
    )
    rSendIntoObjFifoPipeFrontPayload.init(
      //rSendIntoObjFifoPipeFrontPayload.getZero
      doInitSendIntoObjFifoPipePayload(
        prevLineMemArrIdx=sendIntoFifoLineMemArrIdxInit
      )
    )

    sendIntoObjFifoPipeIn(0).valid := rSendIntoObjFifoPipeFrontValid
    sendIntoObjFifoPipeIn(0).payload := rSendIntoObjFifoPipeFrontPayload
    sendIntoObjFifoPipeOut.last.translateInto(
      objLineFifo.io.push
    )(
      dataAssignment=(
        objLineFifoPushPayload,
        sendIntoObjFifoPipePayload
      ) => {
        objLineFifoPushPayload := (
          sendIntoObjFifoPipePayload.objLineMemEntry
        )
      }
    )
    //lineFifo.io.
    //lineFifo.io.

    when (sendIntoObjFifoPipeIn(0).fire) {
      // BEGIN: test logic

      when (
        rSendIntoObjFifoPipeFrontPayload.bakCntWillBeDone()
        //rSendIntoObjFifoPipeFrontPayload.cnt + 1 === params.lineMemSize
      ) {
        //rSendIntoObjFifoPipeFrontValid := False
        //rSendIntoObjFifoChangingRow := True
        rSendIntoObjFifoPipeFrontPayload := (
          doInitSendIntoObjFifoPipePayload(
            //prevLineMemArrIdx=0
            prevLineMemArrIdx=(
              rSendIntoObjFifoPipeFrontPayload.lineMemArrIdx
            )
          )
        )
      } otherwise {
        rSendIntoObjFifoPipeFrontPayload.cnt := (
          rSendIntoObjFifoPipeFrontPayload.cnt + 1
        )
        rSendIntoObjFifoPipeFrontPayload.bakCnt := (
          rSendIntoObjFifoPipeFrontPayload.bakCnt - 1
        )
        rSendIntoObjFifoPipeFrontPayload.objLineMemEntry := (
          rSendIntoObjFifoPipeFrontPayload.objLineMemEntry.getZero
        )
        //rSendIntoObjFifoPipeFrontPayload.bakCntMinus1 := (
        //  rSendIntoObjFifoPipeFrontPayload.bakCntMinus1 - 1
        //)
      }
      // END: test logic
    }

    //val wrCombinePipe = KeepAttribute(
    //  Vec.fill()(Stream(WrCombinePipeElem()))
    //)

    //if (!wrPipeBgNumElemsGtNumBgs) {
    //} else { // if (wrPipeBgNumElemsGtNumBgs)
    //}
    //if (!wrPipeObjNumElemsGtNumObjsPow) {
    //} else { // if (wrPipeObjNumElemsGtNumObjsPow)
    //}

    //val wrBgLineMemEntry = LineMemEntry()
    //val rPastWrBgLineMemEntry = Reg(LineMemEntry())
    //rPastWrBgLineMemEntry.init(rPastWrBgLineMemEntry.getZero)

    val combinePipeIn = KeepAttribute(
      Vec.fill(combinePipeNumMainStages)(
        //Flow(CombinePipePayload())
        Stream(CombinePipePayload())
      )
    )
    val combinePipeOut = KeepAttribute(
      Vec.fill(combinePipeNumMainStages)(
        //Flow(CombinePipePayload())
        Stream(CombinePipePayload())
      )
    )
    //val combinePipeLast = KeepAttribute(
    //  //Flow(CombinePipePayload())
    //  Stream(CombinePipePayload())
    //)
    //rCombineChangingRow := combinePipeLast.bakCntWillBeDone()
    ////when (nextIntnlChangingRow)
    //when (rIntnlChangingRow) 

    //when (
    //  intnlChangingRowRe
    //  //rIntnlChangingRow
    //  //outp.physPosInfo.changingRow
    //)
    //{
    //  nextCombineChangingRow := False
    //} elsewhen (
    //  //combinePipeLast.bakCntWillBeDone() && combinePipeLast.fire
    //  //combinePipeLast.bakCntWillBeDone() && !rCombineChangingRow
    //  combinePipeLast.bakCntWillBeDone()
    //  //(combinePipeLast.cnt + 1 === params.lineMemSize)
    //  //&& combinePipeLast.fire
    //  //combinePipeLast.bakCnt.msb && combinePipeLast.fire
    //  //combinePipeLast.bakCnt.msb
    //) {
    //  nextCombineChangingRow := True
    //} otherwise {
    //  nextCombineChangingRow := rCombineChangingRow
    //}

    val rCombinePipeFrontValid = Reg(Bool()) init(True)
    val rCombinePipeFrontPayload = Reg(CombinePipePayload())
    //rCombinePipeFrontPayload.init(rCombinePipeFrontPayload.getZero)
    rCombinePipeFrontPayload.init(doInitCombinePipePayload(
      //prevRdLineMemArrIdx=combineRdLineMemArrIdxInit,
      //prevWrLineMemArrIdx=combineWrLineMemArrIdxInit,
    ))

    combinePipeIn(0).valid := rCombinePipeFrontValid
    combinePipeIn(0).payload := rCombinePipeFrontPayload

    //val combinePipeStage1Busy = Reg(Bool()) init(False)
    val combinePipeStage1Busy = Bool()
    val combinePipeStage1HaltWhen = combinePipeIn(1).haltWhen(
      combinePipeStage1Busy
    )
    for (idx <- 0 to combinePipeNumMainStages - 1) {
      if (idx == 1) {
        combinePipeOut(idx).valid := combinePipeStage1HaltWhen.valid
        combinePipeStage1HaltWhen.ready := combinePipeOut(idx).ready
      } else {
        combinePipeOut(idx).valid := combinePipeIn(idx).valid
        combinePipeIn(idx).ready := combinePipeOut(idx).ready
      }
    }
    for (idx <- 1 to combinePipeNumMainStages - 1) {
      combinePipeIn(idx) <-/< combinePipeOut(idx - 1)
    }
    //val combinePipePsbArr = (
    //  new ArrayBuffer[PipeSkidBuf[CombinePipePayload]]()
    //)
    //for (idx <- 0 to combinePipeIn.size - 1) {
    //  if (idx != 1) {
    //    combinePipePsbArr += PipeSkidBuf(
    //      dataType=CombinePipePayload(),
    //      optIncludeBusy=false,
    //    )
    //  } else {
    //    combinePipePsbArr += PipeSkidBuf(
    //      dataType=CombinePipePayload(),
    //      optIncludeBusy=true,
    //    )
    //    combinePipePsbArr(idx).io.misc.busy := combinePipeStage1Busy
    //  }
    //  combinePipePsbArr(idx).io.prev << combinePipeOut(idx)

    //  combinePipeOut(idx).valid := combinePipeIn(idx).valid
    //  combinePipeIn(idx).ready := combinePipeOut(idx).ready

    //  combinePipePsbArr(idx).io.misc.clear := clockDomain.isResetActive
    //}
    //for (idx <- 1 to combinePipeIn.size - 1) {
    //  combinePipeIn(idx) << combinePipePsbArr(idx).io.next
    //}
    combinePipeOut.last.translateInto(
      pop
    )(
      dataAssignment=(
        popPayload,
        combinePipeOutLastPayload,
      ) => {
        popPayload := outp
      }
    )

    //for (idx <- 1 to combinePipeIn.size - 1) {
    //  // Create pipeline registering
    //  //combinePipeIn(idx) <-< combinePipeOut(idx - 1)
    //  if (idx !
    //  combinePipeIn(idx) <-/< combinePipeOut(idx - 1)
    //}
    //for (idx <- 0 to combinePipeOut.size - 1) {
    //  // Connect output `valid` to input `valid`
    //  combinePipeOut(idx).valid := combinePipeIn(idx).valid
    //  if (idx != 1) {
    //    combinePipeIn(idx).ready := combinePipeOut(idx).ready
    //  } otherwise {
    //    combinePipeIn(idx).ready := 
    //  }
    //}

    // add one final register
    ////combinePipeLast <-< combinePipeOut.last
    //combinePipeLast <-/< combinePipeOut.last
    //val rPastCombinePipeLastFire = RegNext(combinePipeLast.fire)
    //  .init(False)
    //val rCombinePipeOutLastDidFire = Reg(Bool()) init(False)
    //when (combinePipeLast.fire) {
    //  rCombinePipeLastDidFire := True
    //}
    //when (combinePipeOut.last.fire) {
    //  rCombinePipeOutLastDidFire := True
    //}

    //val sbPop = PipeSkidBuf(
    //  dataType=Gpu2dPopPayload(params=params),
    //  //optIncludeBusy=true,
    //  //optUseOldCode=true,
    //)
    //sbPop.io.misc.clear := clockDomain.isResetActive

    ////sbPop.io.misc.busy := (
    ////  //!(combinePipeLast.fire || rCombinePipeLastDidFire)
    ////  !rCombinePipeOutLastDidFire
    ////)
    //sbPop.io.connectParentStreams(
    //  //push=combinePipeLast,
    //  push=combinePipeOut.last,
    //  pop=pop,
    //)(
    //  payloadConnFunc=(
    //    popPayload,
    //    combinePipePayload,
    //  ) => {
    //    //when (rPsbClear) {
    //    //  //popPayload.col := popPayload.col.getZero
    //    //  popPayload := popPayload.getZero
    //    //} otherwise {
    //      popPayload := outp
    //    //}
    //  }
    //)

    // Send data into the combine pipeline
    //when (nextIntnlChangingRow) {
    //  rCombineChangingRow := False
    //} elsewhen (rCombinePipeFrontPayload.bakCntWillBeDone()) {
    //  rCombineChangingRow := True
    //}

    //when (!rCombinePipeFrontValid) {
    //  when (
    //    //rIntnlChangingRow
    //    intnlChangingRowRe
    //    //outp.physPosInfo.changingRow
    //    //changingRowRe
    //    //nextIntnlChangingRowRe
    //    //nextCombineChangingRow && !rCombineChangingRow
    //  ) {
    //    rCombinePipeFrontValid := True
    //    //rCombinePipeFrontValid := False
    //    rCombinePipeFrontPayload := doInitCombinePipePayload(
    //      //prevRdLineMemArrIdx=rCombinePipeFrontPayload.rdLineMemArrIdx,
    //      //prevWrLineMemArrIdx=rCombinePipeFrontPayload.wrLineMemArrIdx,
    //      prevRdLineMemArrIdx=0,
    //      //prevWrLineMemArrIdx=0,
    //    )
    //    //rCombineChangingRow := False
    //  }
    //} otherwise {
      //when (combinePipeIn(0).fire) {
      //  //rCombinePipeFrontPayload.cnt := rCombinePipeFrontPayload.cnt + 1
      //  when (rCombinePipeFrontPayload.bakCntWillBeDone()) {
      //    rCombinePipeFrontValid := False
      //    //rCombineChangingRow := True
      //  } otherwise {
      //    rCombinePipeFrontPayload.bakCnt := (
      //      rCombinePipeFrontPayload.bakCnt - 1
      //    )
      //    rCombinePipeFrontPayload.bakCntMinus1 := (
      //      rCombinePipeFrontPayload.bakCntMinus1 - 1
      //    )
      //  }
      //}
      when (combinePipeIn(0).fire) {
        //rCombinePipeFrontPayload.cnt := rCombinePipeFrontPayload.cnt + 1
        //rCombinePipeFrontPayload.bakCnt := (
        //  rCombinePipeFrontPayload.bakCnt - 1
        //)
        //rCombinePipeFrontPayload.bakCntMinus1 := (
        //  rCombinePipeFrontPayload.bakCntMinus1 - 1
        //)
        // BEGIN: test logic

        when (
          rCombinePipeFrontPayload.bakCntWillBeDone()
          //rCombinePipeFrontPayload.cnt + 1 === params.lineMemSize
        ) {
          //rCombinePipeFrontValid := False
          //rCombineChangingRow := True
          rCombinePipeFrontPayload := doInitCombinePipePayload(
            //prevLineMemArrIdx=0
          )
        } otherwise {
          rCombinePipeFrontPayload.cnt := rCombinePipeFrontPayload.cnt + 1
          rCombinePipeFrontPayload.bakCnt := (
            rCombinePipeFrontPayload.bakCnt - 1
          )
          rCombinePipeFrontPayload.bakCntMinus1 := (
            rCombinePipeFrontPayload.bakCntMinus1 - 1
          )
        }
        // END: test logic
      }
      //when (rCombinePipeFrontPayload.bakCntWillBeDone()) {
      //  rCombinePipeFrontValid := False
      //  //rCombineChangingRow := True
      //}
    //}


    //val rdPipeIn = KeepAttribute(
    //  Vec.fill(rdPipeNumMainStages)(Stream(RdPipePayload()))
    //)
    //val rdPipeOut = KeepAttribute(
    //  Vec.fill(rdPipeNumMainStages)(Stream(RdPipePayload()))
    //)
    ////val rdPipeSbArr = Array.fill(rdPipeNumMainStages)(
    ////  PipeSkidBuf(
    ////    dataType=RdPipePayload()
    ////  )
    ////)
    ////val rdPipeLastIn = KeepAttribute(Stream(RdPipePayload()))
    //val rdPipeLastIn = KeepAttribute(Stream(RdPipePayload()))
    ////val rdPipeLastOut = KeepAttribute(Stream(RdPipePayload()))

    //val rdPipeLastOut = KeepAttribute(
    //  //Stream(Gpu2dPopPayload(params=params))
    //  Stream(RdPipePayload())
    //)
    ////val rdPipeLastOutPop = KeepAttribute(
    ////  Stream(Gpu2dPopPayload(params=params))
    ////)

    //val sbRdNextFire = Bool()
    ////val sbRdNextCnt = cloneOf(rdPipeLastOut.cnt)
    ////val sbRdNextBakCnt = cloneOf(rdPipeLastOut.bakCnt)
    //val sbRdNextPayload = cloneOf(rdPipeLastOut.payload)

    //when (nextIntnlChangingRow)

    //when (
    //  //rIntnlChangingRow
    //  intnlChangingRowRe
    //)
    //{
    //  nextRdChangingRow := False
    //} elsewhen (
    //  //rdPipeLastIn.bakCntWillBeDone() && rdPipeLastIn.fire
    //  //rdPipeLastIn.bakCnt.msb && rdPipeLastOut.fire
    //  //rdPipeLastOut.bakCntWillBeDone() && rdPipeLastOut.fire
    //  //rdPipeLastOut.bakCntWillBeDone()
    //  //rdPipeLastOut.bakCnt.msb && pop.fire
    //  //(rdPipeLastOut.bakCnt === 0) && sbRd.io.next.fire
    //  //(rdPipeLastOut.bakCnt === 0) && sbRdNextFire
    //  //rdPhysCalcPos.io.info.nextPos.x === 0

    //  //(rdPipeLastOut.bakCnt === 0) && sbRdNextFire
    //  //(rdPipeLastOut.bakCnt.msb) && sbRdNextFire
    //  //rdPipeLastOut.bakCntWillBeDone() && rdPipeLastOut.fire
    //  //(sbRdNextCnt === 0) && sbRdNextFire
    //  //(sbRdNextCnt + 1 === params.lineMemSize) && sbRdNextFire

    //  //(sbRdNextCnt + 1 === params.lineMemSize) && sbRdNextFire

    //  //sbRdNextCnt + 1 === params.intnlFbSize2d.x && sbRdNextFire

    //  //(sbRdNextBakCnt === 0) && sbRdNextFire

    //  //(rdPipeLastOut.bakCnt === 0) && rdPipeLastOut.fire
    //  //rdPipeLastOut.bakCntWillBeDone() && rdPipeLastOut.fire

    //  sbRdNextPayload.bakCntWillBeDone() && sbRdNextFire
    //  //(sbRdNextPayload.cnt + 1 === params.lineMemSize) && sbRdNextFire

    //  //rdPipeLastOut.bakCnt.msb
    //  //(sbRdNextBakCnt === 0) && sbRdNextFire

    //  //(outp.physPosInfo.pos.x === 0)
    //  //&& (
    //  //  outp.bgPxsPosSlice.pos.y =/= outp.bgPxsPosSlice.pastPos.y
    //  //)

    //  //rdPhysCalcPos.io.info.pos.x === 0
    //  //rdPhysCalcPos.io.info.changingRow
    //) {
    //  nextRdChangingRow := True
    //} otherwise {
    //  nextRdChangingRow := rRdChangingRow
    //}

    //val rRdPipeFrontValid = Reg(Bool()) init(True)
    //val rRdPipeFrontPayload = Reg(RdPipePayload())

    ////val nextRdPipeFrontValid = Bool()
    ////val rRdPipeFrontValid = RegNext(nextRdPipeFrontValid) init(True)

    ////val nextRdPipeFrontPayload = RdPipePayload()
    ////val rRdPipeFrontPayload = RegNext(nextRdPipeFrontPayload)
    //////rRdPipeFrontPayload.init(rRdPipeFrontPayload.getZero)
    //rRdPipeFrontPayload.init(doInitRdPipePayload(rdLineMemArrIdxInit))

    ////for (idx <- 0 to rdPipeSbArr.size - 1) {
    ////  rdPipeSbArr(idx).io.prev << rdPipeIn(idx)
    ////  rdPipeOut(idx) << rdPipeSbArr(idx).io.next
    ////  //rdPipeSbArr(idx).io.prev << rdPipeOut(idx)
    ////  //rdPipeIn(idx) << rdPipeSbArr(idx).io.next
    ////}


    ////for (idx <- 1 to rdPipeSbArr.size - 1) {
    ////  //rdPipeSbArr(idx).io.prev << rdPipeIn(idx)
    ////  //rdPipeOut(idx - 1) << rdPipeSbArr(idx).io.next
    ////  rdPipeSbArr(idx).io.prev << rdPipeOut(idx - 1)
    ////  //rdPipeIn(idx) << rdPipeSbArr(idx).io.next
    ////}
    ////for (idx <- 0 to rdPipeNumMainStages - 1) {
    ////  rdPipeIn(idx) << rdPipeSbArr(idx).io.next
    ////  //rdPipeOut(idx).valid := rdPipeIn(idx).valid
    ////}

    //rdPipeIn(0).valid := rRdPipeFrontValid
    //rdPipeIn(0).payload := rRdPipeFrontPayload
    ////rdPipeIn(0).valid := nextRdPipeFrontValid
    ////rdPipeIn(0).payload := nextRdPipeFrontPayload
    ////rdPhysCalcPosEn := rdPipeIn(0).valid

    ////rdPhysCalcPosEn := pop.fire
    ////rdPhysCalcPosEn := !rIntnlChangingRow
    ////rdPhysCalcPosEn := True
    ////rdPhysCalcPosEn := rdPipeLastOut.fire

    ////rdPhysCalcPosEn := rdPipeLastOut.fire
    ////rdPhysCalcPosEn := pop.fire
    ////rdPhysCalcPosEn := sbRdNextFire
    ////rdPhysCalcPosEn := rdPipeIn(0).fire

    ////for (idx <- 0 to rdPipeIn.size - 1) {
    ////  rdPipeIn(idx).ready := rdPipeOut(idx).ready
    ////}
    //for (idx <- 1 to rdPipeIn.size - 1) {
    //  // Create pipeline registering
    //  //rdPipeIn(idx) <-/< rdPipeOut(idx - 1)
    //  rdPipeIn(idx) <-/< rdPipeOut(idx - 1)
    //  //rdPipeIn(idx) <-< rdPipeOut(idx - 1)
    //}
    //for (idx <- 0 to rdPipeOut.size - 1) {
    //  // Connect output `valid` to input `valid`
    //  //when (!popFifo.io.misc.full) 
    //  //when (
    //  //  popFifo.io.misc.amountCanPush > 2
    //  //  && fifoPush.ready
    //  //) {
    //    rdPipeOut(idx).valid := rdPipeIn(idx).valid
    //  //} otherwise {
    //  //  rdPipeOut(idx).valid := False
    //  //}

    //  rdPipeIn(idx).ready := rdPipeOut(idx).ready

    //  //rdPipeOut(idx).ready := rdPipeIn(idx).ready

    //  //rdPipeOut(idx).valid := 

    //  //if (idx < rdPipeOut.size - 1) {
    //  //  rdPipeOut(idx).ready := True
    //  //}
    //  //if (idx < rdPipeOut.size - 1) {
    //  //  rdPipeOut(idx).ready := 
    //  //}

    //  //if (idx > 0) {
    //  //  rdPipeOut(idx).ready := rdPipeIn(idx
    //  //}

    //  //rdPipeIn(idx).ready := rdPipeOut(idx).ready
    //}

    //// add one final register
    ////rdPipeLastIn <-/< rdPipeOut.last
    //rdPipeLastIn <-/< rdPipeOut.last
    ////rdPipeLastIn <-< rdPipeOut.last
    //rdPipeLastOut << rdPipeLastIn
    ////val rRdPipeLastInReady = Reg(Bool()) init(False)

    ////rdPipeLastOut 

    ////rdPipeLastIn.ready := pop.ready
    ////pop.valid := rdPipeLastIn.valid

    ////when (rdPipeLastIn.valid) {
    ////  rPopValid := True
    ////} otherwise {
    ////  rPopValid := False
    ////}

    ////pop.payload := outp

    //val sbRd = PipeSkidBuf(
    //  dataType=RdPipePayload(),
    //  //optIncludeBusy=true,
    //  //optUseOldCode=true,
    //)
    //val sbPop = PipeSkidBuf(
    //  dataType=Gpu2dPopPayload(params=params),
    //  //optIncludeBusy=true,
    //  //optUseOldCode=true,
    //)

    ////val sbFifoPush = PipeSkidBuf(
    ////  dataType=Gpu2dPopPayload(params=params),
    ////  //optIncludeBusy=true,
    ////  //optUseOldCode=true,
    ////)
    ////sbFifoPush.io.misc.clear := clockDomain.isResetActive

    //sbRdNextFire := sbRd.io.next.fire
    //sbRdNextPayload := sbRd.io.next.payload
    ////sbRdNextCnt := sbRd.io.next.payload.cnt
    ////sbRdNextBakCnt := sbRd.io.next.payload.bakCnt
    ////sbRd.io.prev << rdPipeLastOut
    //sbRd.io.prev.payload := rdPipeLastOut
    ////sbRd.io.prev.valid := rdPipeLastOut.valid
    //sbRd.io.prev.valid := sbPop.io.prev.valid

    ////pop << sbPop.io.next

    ////rdPipeLastIn.ready := rdPipeLastOut.ready

    ////val rRdPipeLastInReady = Reg(Bool()) init(False)
    ////rdPipeLastIn.ready := rRdPipeLastInReady

    ////rdPipeLastOut.valid := rdPipeLastIn.valid
    ////rdPipeLastOut.payload := rdPipeLastIn.payload

    ////val rPsbClear = Reg(Bool()) init(True)
    ////when (rRdLineMemArrIdx === wrLineNumInit) {
    ////  rPsbClear := False
    ////}

    ////val popFifoOverflow = Bool()
    //sbPop.io.connectParentStreams(
    //  push=rdPipeLastOut,
    //  //push=rdPipeFifo,
    //  //push=rdPipeLastOut.toStream(
    //  //  overflow=popFifoOverflow,
    //  //  fifoSize=params.physFbSize2d.x * 4,
    //  //  overflowOccupancyAt=params.physFbSize2d.x * 4 - 2
    //  //),
    //  pop=pop,
    //  //pop=fifoPush,
    //)(
    //  payloadConnFunc=(
    //    popPayload,
    //    rdPipePayload,
    //  ) => {
    //    //when (rPsbClear) {
    //    //  //popPayload.col := popPayload.col.getZero
    //    //  popPayload := popPayload.getZero
    //    //} otherwise {
    //      popPayload := outp
    //    //}
    //  }
    //)
    ////sbPop.io.misc.clear := False
    //sbPop.io.misc.clear := clockDomain.isResetActive
    ////sbPop.io.misc.clear := rWrLineNum >= rWr
    ////sbPop.io.misc.busy := rRdChangingRow
    ////sbPop.io.misc.busy := nextRdChangingRow
    ////sbPop.io.misc.busy := False

    ////sbRd.io.misc.clear := clockDomain.isResetActive
    //sbRd.io.misc.clear := sbPop.io.misc.clear
    ////sbRd.io.misc.busy := sbPop.io.misc.busy

    //sbRd.io.next.ready := sbPop.io.next.ready

    //val rdPsb = PipeSkidBuf(
    //  dataType=RdPipePayload()
    //)
    //rdPsb.io.connectParentStreams(
    //  push=pop,
    //  pop=rdPipeLastIn,
    //)(
    //)

    ////popPsb.io.connectParentStreams(
    ////  //push=rdPipeLastIn,
    ////  push=rdPipeOut.last,
    ////  pop=pop,
    ////)(
    ////  payloadConnFunc=(
    ////    popPsbIoPrevPayload,
    ////    rdPipeLastPayload
    ////  ) => {
    //  //outp.col := rdPipeLastIn.payload.bgLineMemEntry.col.rgb
    //  //outp.col := rdPipeOut.last.payload.bgLineMemEntry.col.rgb
    //  //outp.col := rdPipeLastIn.payload.bgLineMemEntry.col.rgb

    //  //outp.col := rdPipeLastOut
    //  outp.col := rdPipeLastOut.bgLineMemEntry.col.rgb
    //  //outp.col := sbRd.io.next.payload.bgLineMemEntry.col.rgb

    //  //when (rdPipeLastOut.fire) 
    //  //when (rdPipeLastOut.fire) 
    //  //when (rdPipeOut.last.fire) 
    //  //when (pop.fire)
    //  //when (rdPipeLastIn.fire)
    //  //when (rdPipeLastOut.fire)
    //  //when (rdPipeLastIn.fire) 
    //  //when (pop.fire)
    //  //when (sbRd.io.next.fire) 
    //  //val rTempCnt = Reg(
    //  //  UInt(log2Up(params.lineMemSize) bits)
    //  //) init(0x0)
    //  ////rTempCnt := (default -> True)
    //  //rTempCnt := params.lineMemSize - 1
    //  //when (rdPipeIn(0).fire)
    //  //when (pop.fire)
    //  //when (sbRd.io.next.fire) 
    //  //otherwise {
    //    //when (rdPipeLastIn.cnt === params.lineMemSize) 
    //  //}
    //  when (sbRdNextFire) {
    //    when (rdPipeLastIn.bakCntWillBeDone()) {
    //      rdPhysCalcPosEn := False
    //    } otherwise {
    //      rdPhysCalcPosEn := True
    //    }
    //    //when (sbRdNextPayload.cnt + 1 === params.lineMemSize) {
    //    //  rdPhysCalcPosEn := False
    //    //} otherwise {
    //    //  rdPhysCalcPosEn := True
    //    //}
    //    //rdPhysCalcPosEn := True
    //    //rdPhysCalcPosEn := True
    //    //outp.col := rdPipeLastOut.payload.bgLineMemEntry.col.rgb
    //    switch (
    //      rRdLineMemArrIdx
    //      //rdPipeLastIn.lineMemArrIdx
    //    ) {
    //      for (
    //        idx <- 0 to (1 << rRdLineMemArrIdx.getWidth) - 1
    //        //idx <- 0 to (1 << rdPipeLastIn.lineMemArrIdx.getWidth) - 1
    //      ) {
    //        is (idx) {
    //          //bgLineMemArr(idx).write(
    //          //  address=combinePipeLast.cnt(
    //          //    log2Up(params.lineMemSize) - 1 downto 0
    //          //  ),
    //          //  data=combinePipeLast.combineWrLineMemEntry,
    //          //)
    //          //outp.col := rdPipeLastPayload.bgLineMemEntry.col.rgb
    //          //popPsbIoPrevPayload.col := (
    //          //  rdPipeLastPayload.bgLineMemEntry.col.rgb
    //          //)
    //          //popPsbIoPrevPayload.physPosInfo
    //          //popPsbIoPrevPayload := outp

    //          objLineMemArr(idx).write(
    //            //address=rdPipeLastIn.payload.cnt(
    //            //  log2Up(params.lineMemSize) - 1 downto 0
    //            //),
    //            //address=rdPipeLastIn.payload.getCntPxPosX(),
    //            address=sbRdNextPayload.getCntPxPosX(),
    //            //address=rdPipeLastOut.payload.getCntPxPosX(),
    //            //address=sbRd.io.next.payload.getCntPxPosX(),
    //            //address=rTempCnt,
    //            //address=outp.bgPxsPosSlice.pos.x(
    //            //  log2Up(params.lineMemSize) - 1 downto 0
    //            //),
    //            //address=rdPipeLastOut.payload.getCntPxPosX(),
    //            //address=outp.physPosInfo.pos.x(
    //            //  log2Up(params.lineMemSize) - 1 downto 0
    //            //),
    //            data=ObjLineMemEntry().getZero,
    //          )
    //        }
    //      }
    //    }
    //  } otherwise {
    //    rdPhysCalcPosEn := rPastRdPhysCalcPosEn
    //  }
    //  //otherwise {
    //  //  outp.col := rPastOutp.col
    //  //}
    //  //pop.valid := rdPipeLastIn.valid
    //  //pop.payload := outp
    //  //rdPipeLastIn.ready := pop.ready

    //  //fifoPush.valid := rdPipeLastIn.valid
    //  //fifoPush.payload := outp
    //  //rdPipeLastIn.ready := fifoPush.ready

    //  //fifoPush.valid := rdPipeLastOut.valid
    //  //fifoPush.payload := outp
    //  //rdPipeLastOut.ready := fifoPush.ready

    //  //fifoPush.valid := rdPipeOut.last.valid
    //  //fifoPush.payload := outp
    //  //rdPipeOut.last.ready := fifoPush.ready
    ////  }
    ////)
    ////popPsb.io.prev.payload := outp
    ////popPsb.io.misc.clear := False

    //val popPsbIo = PipeSkidBufIo(
    //  dataType=UInt(3 bits),
    //  optIncludeBusy=false,
    //)

    // Send data into the read pipeline
    //rRdPipeFrontValid := True
    //when (nextIntnlChangingRow) {
    //  rRdChangingRow := False
    //}
    //rRdPipeFrontValid := True
    //nextRdChangingRow := outp.physPosInfo.changingRow
    //val rPastRdChangingRow = RegNext(rRdChangingRow) init(False)
    //when (!rRdPipeFrontValid) {
      //when (
      //  //rIntnlChangingRow
      //  intnlChangingRowRe
      //  //nextIntnlChangingRowRe
      //  //nextIntnlChangingRow && !rIntnlChangingRow
      //  //nextIntnlChangingRow
      //  //intnlChangingRowRe
      //  //nextRdChangingRow && !rRdChangingRow
      //  //rRdChangingRow && !rPastRdChangingRow
      //  //rRdChangingRow
      //  //outp.bgPxsPosSlice.nextPos.y =/= outp.bgPxsPosSlice.pos.y
      //  //&& outp.bgPxsPosSlice.nextPos.x === 0
      //  //outp.physPosInfo.nextPos.y =/= outp.physPosInfo.pos.y
      //  //&& outp.physPosInfo.nextPos.x === 0
      //  //outp.physPosInfo.changingRow
      //) {
      //  //rRdPipeFrontValid := True
      //  //rRdPipeFrontPayload := doInitRdPipePayload()
      //  nextRdPipeFrontValid := True
      //  nextRdPipeFrontPayload := doInitRdPipePayload(
      //    prevLineMemArrIdx=rRdPipeFrontPayload.lineMemArrIdx
      //  )
      //  //rRdChangingRow := False
      //} otherwise {
      ////.otherwise {
      ////when (rRdPipeFrontPayload.bakCntWillBeDone()) {
      ////  rRdPipeFrontValid := False
      ////  //rRdChangingRow := True
      ////  //rRdPipeFrontPayload := doInitRdPipePayload()
      ////} 
      //  when (rRdPipeFrontPayload.bakCntWillBeDone())
      //  {
      //    //rRdPipeFrontValid := False
      //    nextRdPipeFrontValid := False
      //    //rRdChangingRow := True
      //    //rRdPipeFrontPayload := doInitRdPipePayload()
      //  } otherwise {
      //    nextRdPipeFrontValid := rRdPipeFrontValid
      //  }
      //  when (rdPipeIn(0).fire) {
      //    //rRdPipeFrontPayload.cnt := rRdPipeFrontPayload.cnt + 1
      //    //when (rRdPipeFrontPayload.bakCntWillBeDone()) 
      //    //when (rRdPipeFrontPayload.cnt + 1 >= params.lineMemSize) 
      //    //when (rRdPipeFrontPayload.cnt + 2 === params.lineMemSize)
      //    //when (rRdPipeFrontPayload.cnt + 1 === params.lineMemSize)
      //    //otherwise {
      //      //rRdPipeFrontPayload.cnt := rRdPipeFrontPayload.cnt + 1
      //      //rRdPipeFrontPayload.bakCnt := rRdPipeFrontPayload.bakCnt - 1
      //      //rRdPipeFrontPayload.bakCntMinus1 := (
      //      //  rRdPipeFrontPayload.bakCntMinus1 - 1
      //      //)
      //      nextRdPipeFrontPayload.lineMemArrIdx := (
      //        rRdPipeFrontPayload.lineMemArrIdx
      //      )
      //      nextRdPipeFrontPayload.cnt := (
      //        rRdPipeFrontPayload.cnt + 1
      //      )
      //      nextRdPipeFrontPayload.bakCnt := (
      //        rRdPipeFrontPayload.bakCnt - 1
      //      )
      //      nextRdPipeFrontPayload.bakCntMinus1 := (
      //        rRdPipeFrontPayload.bakCntMinus1 - 1
      //      )
      //      nextRdPipeFrontPayload.tempCntSlice := (
      //        rRdPipeFrontPayload.tempCntSlice + 1
      //      )
      //      //nextRdPipeFrontPayload.postStage0 := (
      //      //  rRdPipeFrontPayload.postStage0
      //      //)
      //      nextRdPipeFrontPayload.postStage0 := (
      //        nextRdPipeFrontPayload.postStage0.getZero
      //      )
      //    //}
      //  } otherwise {
      //    nextRdPipeFrontPayload := rRdPipeFrontPayload
      //  }
      //}
      //when (
      //  //intnlChangingRowRe
      //  outp.physPosInfo.changingRow
      //) 
      // BEGIN: test logic
      //when (
      //  //!rRdPhysCalcPosEn
      //  //outp.physPosInfo.changingRow
      //  //changingRowRe
      //  intnlChangingRowRe
      //) {
      //  nextRdPipeFrontValid := True
      //  nextRdPipeFrontPayload := doInitRdPipePayload(
      //    //prevLineMemArrIdx=rRdPipeFrontPayload.lineMemArrIdx
      //    prevLineMemArrIdx=0
      //  )
      //  //rRdPhysCalcPosEn := True
      //} otherwise {
      //  when (rdPipeIn(0).fire) {
      //    //rRdPipeFrontPayload.cnt := rRdPipeFrontPayload.cnt + 1

      //    when (rRdPipeFrontPayload.bakCntWillBeDone()) {
      //      //rRdPhysCalcPosEn := False
      //      nextRdPipeFrontValid := False
      //      //rRdChangingRow := True
      //      //nextRdPipeFrontPayload := rRdPipeFrontPayload
      //    } otherwise {
      //      nextRdPipeFrontValid := rRdPipeFrontValid
      //    }

      //    //nextRdPipeFrontPayload.lineMemArrIdx := (
      //    //  rRdPipeFrontPayload.lineMemArrIdx
      //    //)
      //    nextRdPipeFrontPayload.cnt := (
      //      rRdPipeFrontPayload.cnt + 1
      //    )
      //    nextRdPipeFrontPayload.bakCnt := (
      //      rRdPipeFrontPayload.bakCnt - 1
      //    )
      //    nextRdPipeFrontPayload.bakCntMinus1 := (
      //      rRdPipeFrontPayload.bakCntMinus1 - 1
      //    )
      //    nextRdPipeFrontPayload.tempCntSlice := (
      //      rRdPipeFrontPayload.tempCntSlice + 1
      //    )
      //    nextRdPipeFrontPayload.postStage0 := (
      //      nextRdPipeFrontPayload.postStage0.getZero
      //    )
      //  } otherwise {
      //    nextRdPipeFrontValid := rRdPipeFrontValid
      //    nextRdPipeFrontPayload := rRdPipeFrontPayload
      //  }
      //}
    //}
    //}
    //when (!rRdPipeFrontValid) {
    //  when (
    //    //rIntnlChangingRow
    //    intnlChangingRowRe
    //  ) {
    //    rRdPipeFrontValid := True
    //    rRdPipeFrontPayload := doInitRdPipePayload(
    //      //prevLineMemArrIdx=rRdPipeFrontPayload.lineMemArrIdx,
    //      prevLineMemArrIdx=0,
    //    )
    //  }
    //} otherwise {
    //  when (rdPipeIn(0).fire) {
    //    when (rRdPipeFrontPayload.bakCntWillBeDone()) {
    //      rRdPipeFrontValid := False
    //    }

    //    //rRdPipeFrontPayload.lineMemArrIdx := (
    //    //  rRdPipeFrontPayload.lineMemArrIdx
    //    //)
    //    rRdPipeFrontPayload.cnt := rRdPipeFrontPayload.cnt + 1
    //    rRdPipeFrontPayload.bakCnt := rRdPipeFrontPayload.bakCnt - 1
    //    rRdPipeFrontPayload.bakCntMinus1 := (
    //      rRdPipeFrontPayload.bakCntMinus1 - 1
    //    )
    //    rRdPipeFrontPayload.tempCntSlice := (
    //      rRdPipeFrontPayload.tempCntSlice + 1
    //    )
    //    rRdPipeFrontPayload.postStage0 := (
    //      rRdPipeFrontPayload.postStage0.getZero
    //    )
    //  }
    //  //when (rdPipeIn(0).fire) {
    //  //  when (
    //  //    rRdPipeFrontPayload.bakCntWillBeDone()
    //  //    //rRdPipeFrontPayload.cnt + 1 === params.lineMemSize
    //  //  ) {
    //  //    //rRdPipeFrontValid := False
    //  //    rRdPipeFrontPayload := doInitRdPipePayload(
    //  //      prevLineMemArrIdx=0
    //  //    )
    //  //  }
    //  //  //.otherwise {
    //  //    //rRdPipeFrontPayload.lineMemArrIdx := (
    //  //    //  rRdPipeFrontPayload.lineMemArrIdx
    //  //    //)
    //  //    rRdPipeFrontPayload.cnt := rRdPipeFrontPayload.cnt + 1
    //  //    rRdPipeFrontPayload.bakCnt := rRdPipeFrontPayload.bakCnt - 1
    //  //    rRdPipeFrontPayload.bakCntMinus1 := (
    //  //      rRdPipeFrontPayload.bakCntMinus1 - 1
    //  //    )
    //  //    rRdPipeFrontPayload.tempCntSlice := (
    //  //      rRdPipeFrontPayload.tempCntSlice + 1
    //  //    )
    //  //    rRdPipeFrontPayload.postStage0 := (
    //  //      rRdPipeFrontPayload.postStage0.getZero
    //  //    )
    //  //  //}
    //  //}
    //}
    //--------
    //def clearObjLineMemEntries(): Unit = {
    //  val tempObjLineMemEntry = ObjLineMemEntry()
    //  tempObjLineMemEntry := tempObjLineMemEntry.getZero
    //  switch (rRdLineMemArrIdx) {
    //    for (idx <- 0 to (1 << rRdLineMemArrIdx.getWidth) - 1) {
    //      is (idx) {
    //        objLineMemArr(idx).write(
    //          address=rRdLineMemCnt,
    //          data=tempObjLineMemEntry,
    //        )
    //      }
    //    }
    //  }
    //}
    //val tempWrBgPxsPos = KeepAttribute(params.bgPxsCoordT())
    val tempWrBgPipeLineMemAddr = KeepAttribute(
      UInt(log2Up(params.lineMemSize) bits)
    )

    def writeBgLineMemEntries(
      //someWrLineMemArrIdx: Int,
    ): Unit = {
      //switch (rWrLineMemArrIdx) {
      //  for (idx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1) {
      //    is (idx) {
      //      //writeBgLineMemEntries(someWrLineMemArrIdx=idx)
      //      //writeObjLineMemEntries(someWrLineMemArrIdx=idx)
      //    }
      //  }
      //  //default {
      //  //  wrLineMemEntry := rPastWrLineMemEntry
      //  //}
      //}
      // Handle backgrounds
      //val bgLineMem = bgLineMemArr(someWrLineMemArrIdx)

      val stageData = DualPipeStageData[Stream[WrBgPipePayload]](
        pipeIn=wrBgPipeIn,
        pipeOut=wrBgPipeOut,
        pipeNumMainStages=wrBgPipeNumMainStages,
        pipeStageIdx=0,
      )

      // BEGIN: stage 0
      HandleDualPipe(
        stageData=stageData.craft(0)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          switch (tempInp.bgIdx) {
            for (tempBgIdx <- 0 to params.numBgs - 1) {
              is (tempBgIdx) {
                //tempOutp.scroll := bgAttrsArr(tempBgIdx).scroll
                tempOutp.bgAttrs := bgAttrsArr(tempBgIdx)
              }
            }
          }
          tempOutp.lineMemArrIdx := tempInp.lineMemArrIdx
          tempOutp.cnt := tempInp.cnt
          tempOutp.bakCnt := tempInp.bakCnt
          tempOutp.bakCntMinus1 := tempInp.bakCntMinus1
          tempOutp.bgIdx := tempInp.bgIdx
          //tempOut.stage0 := tempInp.stage0
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
          stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
        },
      )
      //HandleDualPipe(
      //  stageData=stageData.craft(wrBgPipeCntMinus1StageIdx)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).cntMinus1 := (
      //      stageData.pipeIn(idx).cntMinus1
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).cntMinus1 := (
      //      stageData.pipeIn(idx).cntMinus1
      //    )
      //  },
      //)
      //HandleDualPipe(
      //  stageData=stageData.craft(wrBgPipeBgIdxStageIdx)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).bgIdx := (
      //      stageData.pipeIn(idx).bgIdx
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).bgIdx := (
      //      stageData.pipeIn(idx).bgIdx
      //    )
      //  },
      //)

      //HandleDualPipe(
      //  stageData=stageData.craft(wrBgPipeBgAttrsStageIdx)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    switch (tempInp.bgIdx) {
      //      for (tempBgIdx <- 0 to params.numBgs - 1) {
      //        is (tempBgIdx) {
      //          //tempOutp.scroll := bgAttrsArr(tempBgIdx).scroll
      //          tempOutp.bgAttrs := bgAttrsArr(tempBgIdx)
      //        }
      //      }
      //    }
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).bgAttrs := (
      //      stageData.pipeIn(idx).bgAttrs
      //    )
      //  },
      //)
      // END: stage 0

      // BEGIN: post stage 0
      HandleDualPipe(
        stageData=stageData.craft(1)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          switch (tempInp.bgIdx) {
            for (tempBgIdx <- 0 to params.numBgs - 1) {
              is (tempBgIdx) {
                //tempOutp.scroll := bgAttrsArr(tempBgIdx).scroll
                //tempOutp.bgEntryMemIdxPart0.x := (
                //  //tempInp.scroll.x >> params.bgTileSize2dPow.x
                //  tempInp.scroll.x
                //  //(params.bgTileSize2dPow.x - 1 downto 0)
                //)
                //tempOutp.bgEntryMemIdxPart0.y := (
                //  (tempInp.scroll.y << log2Up(params.bgSize2dInPxs.y))
                //  //(params.bgTileSize2dPow.y - 1 downto 0)
                //)

                //val tempWrBgPxsPos = params.coordT(params.intnlFbSize2d)
                tempOutp.pxsPos.x := (
                  (
                    tempInp.getCntPxPosX()
                    //+ bgAttrsArr(tempBgIdx).scroll.x
                    - tempInp.bgAttrs.scroll.x
                  )
                  //(
                  //  params.bgTileSize2dPow.x - 1 downto 0
                  //)
                )
                tempOutp.pxsPos.y := (
                  (
                    //rWrLineNum.resized
                    //- bgAttrsArr(tempBgIdx).scroll.y
                    //bgAttrsArr(tempBgIdx).scroll.y
                    //- 
                    rWrBgLineNum.resized
                    - tempInp.bgAttrs.scroll.y
                  )
                  //(
                  //  params.bgTileSize2dPow.y - 1 downto 0
                  //)
                )
                //def inpScroll = tempInp.bgAttrs.scroll
                def tempSliceRange = ElabVec2(
                  //x=inpScroll.x.high downto params.bgTileSize2dPow.x,
                  //y=inpScroll.y.high downto params.bgTileSize2dPow.y,
                  x=(
                    (
                      params.bgSize2dInTilesPow.x
                      + params.bgTileSize2dPow.x - 1
                    )
                    downto params.bgTileSize2dPow.x
                  )
                  //(
                  //  params.bgTileSize2dPow.x - 1 downto 0
                  //)
                  ,
                  y=(
                    //(
                    //  //params.bgEntryMemIdxWidth + params.bgTileSize2dPow.y 
                    //  //- 1
                    //  0
                    //)
                    //inpScroll.y.high - 1
                    //downto params.bgTileSize2dPow.y

                    //inpScroll.y.high downto params.bgTileSize2dPow.y

                    (
                      params.bgSize2dInTilesPow.y
                      + params.bgTileSize2dPow.y - 1
                    )
                    downto params.bgTileSize2dPow.y
                    //tempWrBgPxsPos.y.high
                    //downto params.bgTileSize2dPow.y
                  )
                  //(
                  //  params.bgTileSize2dPow.y - 1 downto 0
                  //)
                  ,
                )
                tempOutp.bgEntryMemIdx := Cat(
                  //inpScroll.y(scrollSliceRange.y),
                  //inpScroll.x(scrollSliceRange.x),
                  tempOutp.pxsPos.y(tempSliceRange.y),
                  tempOutp.pxsPos.x(tempSliceRange.x),
                ).asUInt
              }
            }
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).bgEntryMemIdxPart0 := (
          //  stageData.pipeIn(idx).bgEntryMemIdxPart0
          //)
          //stageData.pipeOut(idx).bgEntryMemIdx := (
          //  stageData.pipeIn(idx).bgEntryMemIdx
          //)
          stageData.pipeOut(idx).stage1 := stageData.pipeIn(idx).stage1
        },
      )

      HandleDualPipe(
        stageData=stageData.craft(2)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          switch (tempInp.bgIdx) {
            for (tempBgIdx <- 0 to params.numBgs - 1) {
              is (tempBgIdx) {
                tempOutp.bgEntry := (
                  bgEntryMemArr(tempBgIdx).readAsync(
                    address=tempInp.bgEntryMemIdx
                  )
                )
              }
            }
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).bgEntry := (
          //  stageData.pipeIn(idx).bgEntry
          //)
          stageData.pipeOut(idx).stage2 := stageData.pipeIn(idx).stage2
        },
      )

      HandleDualPipe(
        stageData=stageData.craft(3)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          switch (tempInp.bgIdx) {
            for (tempBgIdx <- 0 to params.numBgs - 1) {
              is (tempBgIdx) {
                val tempTilePxsPos = params.bgTilePxsCoordT()
                tempTilePxsPos.x := (
                  //(
                  //  tempInp.getCntPxPosX()
                  //  //+ bgAttrsArr(tempBgIdx).scroll.x
                  //  - tempInp.bgAttrs.scroll.x
                  //)
                  tempInp.pxsPos.x
                  (
                    params.bgTileSize2dPow.x - 1 downto 0
                  )
                )
                tempTilePxsPos.y := (
                  //(
                  //  //rWrLineNum.resized
                  //  //- bgAttrsArr(tempBgIdx).scroll.y
                  //  //bgAttrsArr(tempBgIdx).scroll.y
                  //  //- 
                  //  rWrLineNum.resized
                  //  - tempInp.bgAttrs.scroll.y
                  //)
                  tempInp.pxsPos.y
                  (
                    params.bgTileSize2dPow.y - 1 downto 0
                  )
                )
                when (!tempInp.bgEntry.dispFlip.x) {
                  tempOutp.tilePxsCoord.x := tempTilePxsPos.x
                } otherwise {
                  tempOutp.tilePxsCoord.x := (
                    params.bgTileSize2d.x - 1 - tempTilePxsPos.x
                  )
                }
                when (!tempInp.bgEntry.dispFlip.y) {
                  tempOutp.tilePxsCoord.y := tempTilePxsPos.y
                } otherwise {
                  tempOutp.tilePxsCoord.y := (
                    params.bgTileSize2d.y - 1 - tempTilePxsPos.y
                  )
                }
              }
            }
          }
          tempOutp.tile := (
            bgTileMem.readAsync(address=tempInp.bgEntry.tileMemIdx)
          )
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).tilePxsCoord := (
          //  stageData.pipeIn(idx).tilePxsCoord
          //)
          stageData.pipeOut(idx).stage3 := stageData.pipeIn(idx).stage3
        },
      )
      //HandleDualPipe(
      //  stageData=stageData.craft(3)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    tempOutp.tile := (
      //      bgTileMem.readAsync(address=tempInp.bgEntry.tileMemIdx)
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).tile := (
      //      stageData.pipeIn(idx).tile
      //    )
      //  },
      //)
      HandleDualPipe(
        stageData=stageData.craft(4)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          //tempOutp.palEntryMemIdx := (
          //  tempInp.tile.colIdxV2d(
          //    //tempInp.scroll.y(params.bgTileSize2dPow.y - 1 downto 0)
          //    tempInp.tilePxsCoord.y
          //  )(
          //    //tempInp.scroll.x(params.bgTileSize2dPow.x - 1 downto 0)
          //    tempInp.tilePxsCoord.x
          //  )
          //)
          //val row = tempInp.tile.colIdxRowVec(tempInp.tilePxsCoord.y)
          //val colIdxVec = row.subdivideIn(tempInp.tile.pxsSize2d.x slices)
          //tempOutp.palEntryMemIdx := colIdxVec(tempInp.tilePxsCoord.x)
          tempOutp.palEntryMemIdx := tempInp.payload.tile.getPx(
            tempInp.payload.tilePxsCoord
          )
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage4 := (
            stageData.pipeIn(idx).stage4
          )
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(5)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          //tempOutp.palEntry := bgPalEntryMem.readAsync(
          //  address=tempInp.palEntryMemIdx
          //)
          tempOutp.palEntryNzMemIdx := tempInp.palEntryMemIdx =/= 0
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage5 := (
            stageData.pipeIn(idx).stage5
          )
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(6)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          // we can read from `bgPalEntryMem` even when
          tempOutp.palEntry := bgPalEntryMem.readAsync(
            address=tempInp.palEntryMemIdx
          )
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage6 := (
            stageData.pipeIn(idx).stage6
          )
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(7)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          // we can read from `bgPalEntryMem` even when
          //tempOutp.palEntry := bgPalEntryMem.readAsync(
          //  address=tempInp.palEntryMemIdx
          //)
          val bgIdx = tempInp.bgIdx
          //switch (bgIdx) {
          //  for (bgJdx <- 0 to params.numBgs - 1) {
          //    is (bgJdx) {
          //      if (bgJdx == params.numBgs - 1) {
          //      } else {
          //      }
          //    }
          //  }
          //}
          //val pastLineMemEntry = tempOutp.lineMemEntry
          val tempLineMemEntry = tempOutp.lineMemEntry
          val rPastLineMemEntry = KeepAttribute(
            Reg(cloneOf(tempLineMemEntry))
          )
          rPastLineMemEntry.setName("rPastWrBgLineMemEntry")
          rPastLineMemEntry.init(rPastLineMemEntry.getZero)
          rPastLineMemEntry := tempLineMemEntry
          when (
            // This could be split into more pipeline stages, but it might
            // not be necessary with 4 or fewer backgrounds
            (
              (bgIdx === (1 << bgIdx.getWidth) - 1)
              || (
                //rPastLineMemEntry.col.a === False
                !rPastLineMemEntry.col.a
                //&& pastLineMemEntry.prio === 
              )
            ) //&& tempInp.bgAttrs.visib
          ) {
            // Starting rendering a new pixel or overwrite the existing
            // pixel
            tempLineMemEntry.col.rgb := tempInp.palEntry.col
            tempLineMemEntry.col.a := tempInp.palEntryNzMemIdx
            tempLineMemEntry.prio := bgIdx
          } otherwise {
            tempLineMemEntry := rPastLineMemEntry
          }
          
          //tempOutp.doWrite := (bgIdx === 0)
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage7 := (
            stageData.pipeIn(idx).stage7
          )
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(8)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)
          //when (tempOutp.doWrite) {
          //} otherwise {
          //}
          switch (
            //rWrLineMemArrIdx
            //wrBgPipeLast.lineMemArrIdx
            tempInp.lineMemArrIdx
          ) {
            for (
              //idx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1
              idx <- 0 to (1 << tempInp.lineMemArrIdx.getWidth) - 1
              //idx <- 0 to (1 << wrBgPipeLast.lineMemArrIdx.getWidth) - 1
            ) {
              is (idx) {
                bgLineMemArr(idx).write(
                  //address=wrBgPipeLast.getCntPxPosX()(
                  //  log2Up(params.lineMemSize) - 1 downto 0
                  //),
                  address=tempWrBgPipeLineMemAddr,
                  data=tempOutp.lineMemEntry,
                )
              }
            }
            //default {
            //  wrLineMemEntry := rPastWrLineMemEntry
            //}
          }
          tempWrBgPipeLineMemAddr := tempInp.getCntPxPosX()(
            log2Up(params.lineMemSize) - 1 downto 0
          )
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrBgPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).stage7 := (
          //  stageData.pipeIn(idx).stage7
          //)
        },
      )
      //when (wrBgPipeLast.fire) {
      //  val tempLineMemEntry = LineMemEntry()
      //  val bgIdx = wrBgPipeLast.bgIdx
      //  switch (
      //    rWrLineMemArrIdx
      //    //wrBgPipeLast.lineMemArrIdx
      //  ) {
      //    for (
      //      idx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1
      //      //idx <- 0 to (1 << wrBgPipeLast.lineMemArrIdx.getWidth) - 1
      //    ) {
      //      is (idx) {
      //        bgLineMemArr(idx).write(
      //          //address=wrBgPipeLast.getCntPxPosX()(
      //          //  log2Up(params.lineMemSize) - 1 downto 0
      //          //),
      //          address=tempWrBgPipeLineMemAddr,
      //          data=wrBgPipeLast.lineMemEntry,
      //        )
      //      }
      //    }
      //    //default {
      //    //  wrLineMemEntry := rPastWrLineMemEntry
      //    //}
      //  }
      //}
      // END: post stage 0

      //--------
      //--------
    }

    //val wrObjLineMemEntry = LineMemEntry()
    //val rPastWrObjLineMemEntry = Reg(LineMemEntry())
    //rPastWrObjLineMemEntry.init(rPastWrObjLineMemEntry.getZero)

    def writeObjLineMemEntries(
      //someWrLineMemArrIdx: Int,
    ): Unit = {
      // Handle sprites
      val stageData = DualPipeStageData[Stream[WrObjPipePayload]](
        pipeIn=wrObjPipeIn,
        pipeOut=wrObjPipeOut,
        pipeNumMainStages=wrObjPipeNumMainStages,
        pipeStageIdx=0,
      )
      // BEGIN: stage 0
      HandleDualPipe(
        stageData=stageData.craft(0)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).cnt := (
          //  stageData.pipeIn(idx).cnt
          //)
          stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).cnt := (
          //  stageData.pipeIn(idx).cnt
          //)
          stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
        },
      )
      //HandleDualPipe(
      //  stageData=stageData.craft(0)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).cntMinus1 := (
      //      stageData.pipeIn(idx).cntMinus1
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).cntMinus1 := (
      //      stageData.pipeIn(idx).cntMinus1
      //    )
      //  },
      //)
      // END: Stage 0

      // BEGIN: Post stage 0
      // BEGIN: Stage 1
      HandleDualPipe(
        stageData=stageData.craft(1)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)
          //val objAttrsMemIdxIn = tempInp.objAttrsMemIdx
          tempOutp.objAttrs := objAttrsMem.readAsync(
            address=tempInp.objAttrsMemIdx(),
          )
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).objAttrs := (
          //  stageData.pipeIn(idx).objAttrs
          //)
          stageData.pipeOut(idx).stage1 := stageData.pipeIn(idx).stage1
        },
      )
      // END: Stage 1

      // BEGIN: Stage 2
      HandleDualPipe(
        stageData=stageData.craft(2)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          //tempOutp.tilePxsCoord.x := tempInp.getCntTilePxsCoordX()
          //tempOutp.tilePxsCoord.y := tempInp.objAttrs.pos
          //when (tempInp.pxPosInLine) {
          //  //tempOutp.tilePxsCoord.x 
          //} otherwise {
          //  tempOutp.tilePxsCoord := tempInp.tilePxsCoord
          //}
          tempOutp.tilePxsCoord.x := (
            //tempInp.getBakCntTilePxsCoordX()
            tempInp.getCntTilePxsCoordX()
          )
          tempOutp.tilePxsCoord.y := (
            //rWrLineNum - tempInp.objAttrs.pos.y.asUInt
            (rWrObjLineNum.asSInt.resized - tempInp.objAttrs.pos.y).asUInt
          )(
            tempOutp.tilePxsCoord.y.bitsRange
          )

          tempOutp.pxPos.x := (
            tempInp.objAttrs.pos.x.asUInt
            //tempInp.objAttrs.pos.x
            //+ tempInp.getBakCntTilePxsCoordX().asSInt.resized
            //+ tempInp.getBakCntTilePxsCoordX().resized
            //- 1
            //+ tempOutp.tilePxsCoord.x.asSInt
            + tempOutp.tilePxsCoord.x
            //- tempOutp.tilePxsCoord.x
          ).asSInt
          tempOutp.pxPos.y(rWrObjLineNum.bitsRange) := (
            //outp.bgPxsPosSlice.pos.y - tempInp.objAttrs.pos
            //rWrLineNum.asSInt.resized - tempInp.objAttrs.pos.y
            //tempInp.objAttrs.pos.y 
            //rWrLineNum.asSInt.resized
            rWrObjLineNum.asSInt
          )
          tempOutp.pxPos.y(
            tempOutp.pxPos.y.high downto rWrObjLineNum.high
          ) := 0x0

          //def tempPxsCoordSizeYPow = params.objPxsCoordSize2dPow.y
          //def tempMinusAmountY = params.objTileSize2d.y
          ////tempOutp.pxPosShiftTopLeft.y := (
          ////  rWrLineNum.asSInt.resized
          ////  - S(f"$tempMinusSizeYPow'd$tempPlusAmountY")
          ////)
          tempOutp.objPosYShift := (
            //rWrLineNum.asSInt.resized
            //+ S(f"$tempPxsCoordSizeYPow'd$tempMinusAmountY")
            tempInp.objAttrs.pos.y + params.objTileSize2d.y
          )
        
          tempOutp.tile := objTileMem.readAsync(
            address=tempInp.objAttrs.tileMemIdx
          )
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage2 := stageData.pipeIn(idx).stage2
          //stageData.pipeOut(idx).tilePxsCoordYPipe1 := (
          //  stageData.pipeIn(idx).tilePxsCoordYPipe1
          //)
        },
      )
      // END: Stage 2

      // BEGIN: Stage 3
      HandleDualPipe(
        stageData=stageData.craft(3)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          //tempOutp.oldPxPosInLineCheckGePipe1.x := (
          //  tempInp.objAttrs.pos.x + params.objTileSize2d.x - 1 >= 0
          //)
          //tempOutp.oldPxPosInLineCheckGePipe1.y := (
          //  outp.bgPxsPosSlice.pos.y.asSInt.resized
          //  >= tempInp.objAttrs.pos.y
          //)
          //tempOutp.tilePxs
          //tempOutp.tilePxsCoord.y := tempInp.tilePxsCoordYPipe1
          //tempOutp.pxPosRangeCheck.y := 

          tempOutp.pxPosRangeCheckGePipe1.x := (
            //tempInp.objAttrs.pos.x + params.objTileSize2d.x - 1 >= 0
            tempInp.pxPos.x >= 0
          )
          tempOutp.pxPosRangeCheckGePipe1.y := (
            ////outp.bgPxsPosSlice.pos.y.asSInt.resized
            ////>= tempInp.objAttrs.pos.y
            //tempInp.pxPos.y >= 0
            ////tempInp.pxPos.y >= tempInp.objAttrs.pos.y
            tempInp.pxPos.y >= tempInp.objAttrs.pos.y
          )

          tempOutp.pxPosRangeCheckLtPipe1.x := (
            tempInp.pxPos.x < params.intnlFbSize2d.x
          )
          tempOutp.pxPosRangeCheckLtPipe1.y := (
            ////tempInp.pxPos.y >= 0
            //tempInp.pxPos.y < params.intnlFbSize2d.y
            ////tempInp.pxPosMinusTileSize2d
            ////< params.intnlFbSize2d.y.resized
            //tempInp.pxPos.y <= 

            //tempInp.objPosYShift < params.intnlFbSize2d.y
            tempInp.pxPos.y < tempInp.objPosYShift
          )
          tempOutp.palEntryMemIdx := tempInp.tile.getPx(
            tempInp.tilePxsCoord
          )
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage3 := stageData.pipeIn(idx).stage3
        },
      )
      // BEGIN: Stage 4
      HandleDualPipe(
        stageData=stageData.craft(4)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          tempOutp.palEntryNzMemIdx := tempInp.palEntryMemIdx =/= 0
          tempOutp.pxPosRangeCheck.x := (
            //(tempInp.objAttrs.pos.x + params.objTileSize2d.x - 1 >= 0)
            //&& (tempInp.objAttrs.pos.x < params.intnlFbSize2d.x)
            tempInp.pxPosRangeCheckGePipe1.x
            && tempInp.pxPosRangeCheckLtPipe1.x
          )
          tempOutp.pxPosRangeCheck.y := (
            //(
            //  outp.bgPxsPosSlice.pos.y.asSInt.resized
            //  >= tempInp.objAttrs.pos.y
            //) && (
            //  outp.bgPxsPosSlice.pos.y.asSInt.resized
            //  < tempInp.objAttrs.pos.y + params.objTileSize2d.y
            //)
            tempInp.pxPosRangeCheckGePipe1.y
            && tempInp.pxPosRangeCheckLtPipe1.y
          )
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage4 := stageData.pipeIn(idx).stage4
        },
      )
      // END: Stage 4

      // BEGIN: Stage 5
      HandleDualPipe(
        stageData=stageData.craft(5)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          tempOutp.palEntry := objPalEntryMem.readAsync(
            address=tempInp.palEntryMemIdx
          )
          tempOutp.pxPosInLine := (
            tempInp.pxPosRangeCheck.x && tempInp.pxPosRangeCheck.y
          )
          // BEGIN: debug comment this out
          //switch (
          //  rWrLineMemArrIdx
          //  //tempInp.lineMemArrIdx
          //) {
          //  for (
          //    jdx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1
          //    //jdx <- 0 to (1 << tempInp.lineMemArrIdx.getWidth) - 1
          //  ) {
          //    is (jdx) {
          //      tempOutp.rdLineMemEntry := objLineMemArr(jdx).readAsync(
          //        //address=tempInp.pxPos.x.asUInt.resized,
          //        address=tempInp.pxPos.x.asUInt(
          //          log2Up(params.lineMemSize) - 1 downto 0
          //        ),
          //        //readUnderWrite=writeFirst,
          //        //readUnderWrite=writeFirst,
          //      )
          //    }
          //  }
          //}
          // END: debug comment this out
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage5 := stageData.pipeIn(idx).stage5
        },
      )
      // END: Stage 5

      // BEGIN: Stage 6
      HandleDualPipe(
        stageData=stageData.craft(6)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          //switch (rWrLineMemArrIdx) {
          //  for (jdx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1) {
          //    is (jdx) {
          //      
          //    }
          //  }
          //}

          //when (tempInp.rdLineMemEntry.col.a) {
          //when (tempInp.rdLineMemEntry.prio === 0) {
          //}
          when (tempInp.pxPosInLine) {
            when (
              tempInp.rdLineMemEntry.prio < tempInp.objAttrs.prio
            ) {
              tempOutp.overwriteLineMemEntry := True
            } otherwise {
              tempOutp.overwriteLineMemEntry := (
                !tempInp.rdLineMemEntry.col.a
              )
            }
          } otherwise {
            tempOutp.overwriteLineMemEntry := False
          }
          
          //tempOutp.overwriteLineMemEntry := (
          //  //!tempInp.rdLineMemEntry.prio.msb
          //  //tempInp.rdLineMemEntry.written
          //  //(
          //  //  !tempInp.rdLineMemEntry.col.a
          //  //  //tempInp.objAttrs.prio
          //  //  //|| (tempInp.rdLineMemEntry.prio < tempInp.objAttrs.prio)

          //  //  //&& tempInp.objAttrs.prio <= tempInp.rdLineMemEntry.prio
          //  //  //&& tempInp.pxPosInLine
          //  //)
          //  (
          //    tempInp.rdLineMemEntry.col.a
          //  )
          //  //|| (tempInp.pxPos.x < 0)
          //  //|| (tempInp.pxPos.x >= params.lineMemSize)
          //)
          //tempOutp.overwriteLineMemEntry := True
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage6 := stageData.pipeIn(idx).stage6
        },
      )
      // END: Stage 6

      // BEGIN: Stage 7
      HandleDualPipe(
        stageData=stageData.craft(7)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          when (tempInp.overwriteLineMemEntry) {
            tempOutp.wrLineMemEntry.col.rgb := tempInp.palEntry.col
            //tempOutp.wrLineMemEntry.col.a := True
            tempOutp.wrLineMemEntry.col.a := tempInp.palEntryNzMemIdx
            tempOutp.wrLineMemEntry.prio := tempInp.objAttrs.prio
          } otherwise {
            tempOutp.wrLineMemEntry := tempInp.rdLineMemEntry
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[WrObjPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage7 := stageData.pipeIn(idx).stage7
        },
      )
      // END: Stage 7

      //HandleDualPipe(
      //  stageData=stageData.craft(wrObjPipePxPosInLineCheckGePipe1StageIdx)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    tempOutp.oldPxPosInLineCheckGePipe1.x := (
      //      tempInp.objAttrs.pos.x + params.objTileSize2d.x - 1 >= 0
      //    )
      //    tempOutp.oldPxPosInLineCheckGePipe1.y := (
      //      outp.bgPxsPosSlice.pos.y.asSInt.resized
      //      >= tempInp.objAttrs.pos.y
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).oldPxPosInLineCheckGePipe1 := (
      //      stageData.pipeIn(idx).oldPxPosInLineCheckGePipe1
      //    )
      //  },
      //)
      //HandleDualPipe(
      //  stageData=stageData.craft(wrObjPipePxPosInLineCheckLtPipe1StageIdx)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    tempOutp.oldPxPosInLineCheckLtPipe1.x := (
      //      tempInp.objAttrs.pos.x < params.intnlFbSize2d.x
      //    )
      //    tempOutp.oldPxPosInLineCheckLtPipe1.y := (
      //      outp.bgPxsPosSlice.pos.y.asSInt.resized
      //      < tempInp.objAttrs.pos.y + params.objTileSize2d.y
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).oldPxPosInLineCheckLtPipe1 := (
      //      stageData.pipeIn(idx).oldPxPosInLineCheckLtPipe1
      //    )
      //  },
      //)
      //// END: Stage 3

      //// BEGIN: Stage 4
      //HandleDualPipe(
      //  stageData=stageData.craft(wrObjPipePxPosInLineCheckStageIdx)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    tempOutp.oldPxPosInLineCheck.x := (
      //      //(tempInp.objAttrs.pos.x + params.objTileSize2d.x - 1 >= 0)
      //      //&& (tempInp.objAttrs.pos.x < params.intnlFbSize2d.x)
      //      tempInp.oldPxPosInLineCheckGePipe1.x
      //      && tempInp.oldPxPosInLineCheckLtPipe1.x
      //    )
      //    tempOutp.oldPxPosInLineCheck.y := (
      //      //(
      //      //  outp.bgPxsPosSlice.pos.y.asSInt.resized
      //      //  >= tempInp.objAttrs.pos.y
      //      //) && (
      //      //  outp.bgPxsPosSlice.pos.y.asSInt.resized
      //      //  < tempInp.objAttrs.pos.y + params.objTileSize2d.y
      //      //)
      //      tempInp.oldPxPosInLineCheckGePipe1.y
      //      && tempInp.oldPxPosInLineCheckLtPipe1.y
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).oldPxPosInLineCheck := (
      //      stageData.pipeIn(idx).oldPxPosInLineCheck
      //    )
      //  },
      //)
      //// END: Stage 4

      //// BEGIN: Stage 5
      //HandleDualPipe(
      //  stageData=stageData.craft(wrObjPipePxPosInLineStageIdx)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    tempOutp.pxPosInLine := (
      //      tempInp.oldPxPosInLineCheck.x && tempInp.oldPxPosInLineCheck.y
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).pxPosInLine := (
      //      stageData.pipeIn(idx).pxPosInLine
      //    )
      //  },
      //)
      //// END: Stage 5

      ////HandleDualPipe(
      ////  stageData=stageData.craft(wrObjPipeTileMemIdxStageIdx)
      ////)(
      ////  pipeStageMainFunc=(
      ////    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    val tempInp = stageData.pipeIn(idx)
      ////    val tempOutp = stageData.pipeOut(idx)
      ////    //tempOutp.tileMemIdx := objTileMem.readAsync(
      ////    //  address=tempInp.objAttrs.tileMemIdx
      ////    //)
      ////    tempOutp.tileMemIdx := tempInp.objAttrs.tileMemIdx
      ////  },
      ////  copyOnlyFunc=(
      ////    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    stageData.pipeOut(idx).tileMemIdx := (
      ////      stageData.pipeIn(idx).tileMemIdx
      ////    )
      ////  },
      ////)
      ////HandleDualPipe(
      ////  stageData=stageData.craft(wrObjPipeTilePxsCoordStageIdx)
      ////)(
      ////  pipeStageMainFunc=(
      ////    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    val tempInp = stageData.pipeIn(idx)
      ////    val tempOutp = stageData.pipeOut(idx)

      ////    //tempOutp.tilePxsCoord.x := outp.bgPxsPosSlice
      ////    tempOutp.tilePxsCoord.x
      ////  },
      ////  copyOnlyFunc=(
      ////    stageData: DualPipeStageData[Stream[WrObjPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    stageData.pipeOut(idx).tilePxsCoord := (
      ////      stageData.pipeIn(idx).tilePxsCoord
      ////    )
      ////  },
      ////)
      //// END: Post stage 0
      //--------

      //val objLineMem = objLineMemArr(someWrLineMemArrIdx)
      //when (wrObjPipeLast.fire) {
      //  //val tempLineMemEntry = LineMemEntry()
      //  //val objIdx = wrObjPipeLast.objIdx
      //  switch (
      //    rWrLineMemArrIdx
      //    //wrObjPipeLast.lineMemArrIdx
      //  ) {
      //    for (
      //      idx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1
      //      //idx <- 0 to (1 << wrObjPipeLast.lineMemArrIdx.getWidth) - 1
      //    ) {
      //      is (idx) {
      //        //when (
      //        //  wrObjPipeLast.pxPos.x >= 0 
      //        //  && wrObjPipeLast.pxPos.x < params.lineMemSize
      //        //) 
      //        {
      //          objLineMemArr(idx).write(
      //            //address=wrObjPipeLast.getCntTilePxsCoordX(),
      //            //address=wrObjPipeLast.pxPos.x.asUInt.resized,
      //            address=wrObjPipeLast.pxPos.x.asUInt(
      //              log2Up(params.lineMemSize) - 1 downto 0
      //            ),
      //            data=wrObjPipeLast.wrLineMemEntry,
      //          )
      //        }
      //      }
      //    }
      //    //default {
      //    //  wrLineMemEntry := rPastWrLineMemEntry
      //    //}
      //  }
      //}
    }
    //--------
    def doSendIntoFifo(): Unit = {
      val stageData = DualPipeStageData[Stream[SendIntoFifoPipePayload]](
        pipeIn=sendIntoFifoPipeIn,
        pipeOut=sendIntoFifoPipeOut,
        pipeNumMainStages=sendIntoFifoPipeNumMainStages,
        pipeStageIdx=0,
      )
      HandleDualPipe(
        stageData=stageData.craft(0)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[SendIntoFifoPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          when (clockDomain.isResetActive) {
            tempOutp.stage0 := tempOutp.stage0.getZero
          } otherwise {
            //tempOutp.stage0 := tempInp.stage0
            tempOutp.lineMemArrIdx := tempInp.lineMemArrIdx
            tempOutp.cnt := tempInp.cnt
            tempOutp.bakCnt := tempInp.bakCnt
            switch (
              //rWrLineMemArrIdx
              //sendIntoFifoLineMemArrIdx
              tempInp.lineMemArrIdx
            ) {
              for (
                jdx <- 0 to (1 << tempInp.lineMemArrIdx.getWidth) - 1
              ) {
                is (jdx) {
                  tempOutp.bgLineMemEntry := bgLineMemArr(jdx).readAsync(
                    address=tempInp.cnt(
                      log2Up(params.lineMemSize) - 1 downto 0
                    )
                  )
                  //tempOutp.objLineMemEntry := objLineMemArr(jdx).readAsync(
                  //  address=tempInp.cnt(
                  //    log2Up(params.lineMemSize) - 1 downto 0
                  //  )
                  //)
                }
              }
            }
          }
          //tempOut.stage0 := tempInp.stage0
          //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[SendIntoFifoPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
          //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          when (clockDomain.isResetActive) {
            tempOutp.stage0 := tempOutp.stage0.getZero
          } otherwise {
            tempOutp.stage0 := tempInp.stage0
          }
        },
      )
      //HandleDualPipe(
      //  stageData=stageData.craft(1)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[SendIntoFifoPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    //when (clockDomain.isResetActive) {
      //    //} otherwise {
      //    //  //bgLineFifoPushPayload
      //    //  lineFifo
      //    //}
      //    //tempOut.stage0 := tempInp.stage0
      //    //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[SendIntoFifoPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
      //    //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
      //    //val tempInp = stageData.pipeIn(idx)
      //    //val tempOutp = stageData.pipeOut(idx)

      //    //when (clockDomain.isResetActive) {
      //    //  tempOutp.stage0 := tempOutp.stage0.getZero
      //    //} otherwise {
      //    //  tempOutp.stage0 := tempInp.stage0
      //    //}
      //  },
      //)
    }
    //def doSendIntoObjFifo(): Unit = {
    //  val stageData = (
    //    DualPipeStageData[Stream[SendIntoObjFifoPipePayload]](
    //      pipeIn=sendIntoObjFifoPipeIn,
    //      pipeOut=sendIntoObjFifoPipeOut,
    //      pipeNumMainStages=sendIntoFifoPipeNumMainStages,
    //      pipeStageIdx=0,
    //    )
    //  )
    //  HandleDualPipe(
    //    stageData=stageData.craft(0)
    //  )(
    //    pipeStageMainFunc=(
    //      stageData: DualPipeStageData[Stream[SendIntoObjFifoPipePayload]],
    //      idx: Int,
    //    ) => {
    //      val tempInp = stageData.pipeIn(idx)
    //      val tempOutp = stageData.pipeOut(idx)

    //      when (clockDomain.isResetActive) {
    //        tempOutp.stage0 := tempOutp.stage0.getZero
    //      } otherwise {
    //        //tempOutp.stage0 := tempInp.stage0
    //        tempOutp.lineMemArrIdx := tempInp.lineMemArrIdx
    //        tempOutp.cnt := tempInp.cnt
    //        tempOutp.bakCnt := tempInp.bakCnt
    //        switch (
    //          //rWrLineMemArrIdx
    //          //sendIntoFifoLineMemArrIdx
    //          tempInp.lineMemArrIdx
    //        ) {
    //          for (
    //            jdx <- 0 to (1 << tempInp.lineMemArrIdx.getWidth) - 1
    //          ) {
    //            is (jdx) {
    //              //tempOutp.bgLineMemEntry := bgLineMemArr(jdx).readAsync(
    //              //  address=tempInp.cnt(
    //              //    log2Up(params.lineMemSize) - 1 downto 0
    //              //  )
    //              //)
    //              tempOutp.objLineMemEntry := objLineMemArr(jdx).readAsync(
    //                address=tempInp.cnt(
    //                  log2Up(params.lineMemSize) - 1 downto 0
    //                )
    //              )
    //            }
    //          }
    //        }
    //      }
    //      //tempOut.stage0 := tempInp.stage0
    //      //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
    //    },
    //    copyOnlyFunc=(
    //      stageData: DualPipeStageData[Stream[SendIntoObjFifoPipePayload]],
    //      idx: Int,
    //    ) => {
    //      //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
    //      //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
    //      val tempInp = stageData.pipeIn(idx)
    //      val tempOutp = stageData.pipeOut(idx)

    //      when (clockDomain.isResetActive) {
    //        tempOutp.stage0 := tempOutp.stage0.getZero
    //      } otherwise {
    //        tempOutp.stage0 := tempInp.stage0
    //      }
    //    },
    //  )
    //  //HandleDualPipe(
    //  //  stageData=stageData.craft(1)
    //  //)(
    //  //  pipeStageMainFunc=(
    //  //    stageData: DualPipeStageData[Stream[SendIntoFifoPipePayload]],
    //  //    idx: Int,
    //  //  ) => {
    //  //    val tempInp = stageData.pipeIn(idx)
    //  //    val tempOutp = stageData.pipeOut(idx)

    //  //    //when (clockDomain.isResetActive) {
    //  //    //} otherwise {
    //  //    //  //bgLineFifoPushPayload
    //  //    //  lineFifo
    //  //    //}
    //  //    //tempOut.stage0 := tempInp.stage0
    //  //    //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
    //  //  },
    //  //  copyOnlyFunc=(
    //  //    stageData: DualPipeStageData[Stream[SendIntoFifoPipePayload]],
    //  //    idx: Int,
    //  //  ) => {
    //  //    //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
    //  //    //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
    //  //    //val tempInp = stageData.pipeIn(idx)
    //  //    //val tempOutp = stageData.pipeOut(idx)

    //  //    //when (clockDomain.isResetActive) {
    //  //    //  tempOutp.stage0 := tempOutp.stage0.getZero
    //  //    //} otherwise {
    //  //    //  tempOutp.stage0 := tempInp.stage0
    //  //    //}
    //  //  },
    //  //)
    //}
    //--------
    def combineLineMemEntries(
      //someCombineLineMemArrIdx: Int
    ): Unit = {
      val stageData = DualPipeStageData[Stream[CombinePipePayload]](
        pipeIn=combinePipeIn,
        pipeOut=combinePipeOut,
        pipeNumMainStages=combinePipeNumMainStages,
        pipeStageIdx=0,
      )
      HandleDualPipe(
        stageData=stageData.craft(0)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          when (clockDomain.isResetActive) {
            tempOutp.stage0 := tempOutp.stage0.getZero
          } otherwise {
            tempOutp.stage0 := tempInp.stage0
          }
          //tempOut.stage0 := tempInp.stage0
          //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
          //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          when (clockDomain.isResetActive) {
            tempOutp.stage0 := tempOutp.stage0.getZero
          } otherwise {
            tempOutp.stage0 := tempInp.stage0
          }
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(1)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          //val tempCombineLineMemIdxArr = new ArrayBuffer[UInt]()
          //val tempCombineLineMemIdx  = UInt(
          //  log2Up(params.lineMemSize) bits
          //)
          //  .setName("tempCombineLineMemArrIdx")
          //tempCombineLineMemIdx := (
          //  tempInp.cnt(log2Up(params.lineMemSize) - 1 downto 0)
          //)

          when (clockDomain.isResetActive) {
            tempOutp.stage1 := tempOutp.stage1.getZero
            combinePipeStage1Busy := False
            lineFifo.io.pop.ready := True
            //lineFifo.io.pop.ready := False
            //objLineFifo.io.pop.ready := False
          } otherwise {
            //val rPastBgLineFifoPopFire = KeepAttribute(
            //  RegNext(lineFifo.io.pop.fire) init(False)
            //    .setName("rPastBgLineFifoPopFire")
            //)
            //val rPastObjLineFifoPopFire = KeepAttribute(
            //  RegNext(objLineFifo.io.pop.fire) init(False)
            //    .setName("rPastObjLineFifoPopFire")
            //)

            //val rSeenBgLineFifoPopValid = KeepAttribute(
            //  Reg(Bool()) init(False)
            //    .setName("rCombinePipeStage1SeenBgLineFifoPopValid")
            //)
            //val rSeenObjLineFifoPopValid = KeepAttribute(
            //  Reg(Bool()) init(False)
            //    .setName("rCombinePipeStage1SeenObjLineFifoPopValid")
            //)
            //combinePipeStage1Busy := False
            //combinePipeStage1Busy := (
            //  !(
            //    (lineFifo.io.pop.valid || rSeenBgLineFifoPopValid)
            //    && (objLineFifo.io.pop.valid || rSeenObjLineFifoPopValid)
            //  )
            //)
            //val rPastCombinePipeStage1Busy = (
            //  RegNext(combinePipeStage1Busy) init(False)
            //)

            ////lineFifo.io.pop.ready := !combinePipeStage1Busy
            ////objLineFifo.io.pop.ready := !combinePipeStage1Busy
            //lineFifo.io.pop.ready := True
            //objLineFifo.io.pop.ready := True
            ////combinePipeStage1Busy := False
            //combinePipeStage1Busy := !lineFifo.io.pop.fire

            ////when (!combinePipeStage1Busy) {
            ////  rSeenBgLineFifoPopValid := False
            ////  rSeenObjLineFifoPopValid := False
            ////} otherwise 
            //////when (rPastCombinePipeStage1Busy) 
            ////{
            ////  when (lineFifo.io.pop.valid) {
            ////    rSeenBgLineFifoPopValid := True
            ////  }
            ////  when (objLineFifo.io.pop.valid) {
            ////    rSeenObjLineFifoPopValid := True
            ////  }
            ////}
            //when (lineFifo.io.pop.fire) {
            //  tempOutp.bgRdLineMemEntry := lineFifo.io.pop.payload
            //} otherwise {
            //  tempOutp.bgRdLineMemEntry := (
            //    tempOutp.bgRdLineMemEntry.getZero
            //  )
            //}
            //when (objLineFifo.io.pop.fire) {
            //  tempOutp.objRdLineMemEntry := objLineFifo.io.pop.payload
            //} otherwise {
            //  tempOutp.objRdLineMemEntry := (
            //    tempOutp.objRdLineMemEntry.getZero
            //  )
            //}
            //tempOutp.objRdLineMemEntry := objLineFifo.io.pop.payload

            //when (!combinePipeStage1Busy) {
            //} otherwise {
            //}

            //switch (
            //  rCombineRdLineMemArrIdx
            //  //tempInp.rdLineMemArrIdx
            //) {
            //  for (
            //    jdx <- 0 to (1 << rCombineRdLineMemArrIdx.getWidth) - 1
            //    //jdx <- 0 to (1 << tempInp.rdLineMemArrIdx.getWidth) - 1
            //  ) {
            //    //tempCombineLineMemIdxArr += (
            //    //  UInt(log2Up(params.lineMemSize) bits)
            //    //  .setName(f"tempCombineLineMemIdxArr_$idx")
            //    //)
            //    //tempCombineLineMemIdxArr(jdx) := (
            //    //  tempInp.cnt(log2Up(params.lineMemSize) - 1 downto 0)
            //    //)
            //    is (jdx) {
            //      tempOutp.bgRdLineMemEntry := (
            //        bgLineMemArr(jdx).readAsync(
            //          address=tempCombineLineMemIdx
            //          //address=tempInp.bakCnt(
            //          //  log2Up(params.lineMemSize) - 1 downto 0
            //          //),
            //          //address=(tempInp.bakCnt + 1)(
            //          //  log2Up(params.lineMemSize) - 1 downto 0
            //          //),
            //          //address=tempInp.cnt(
            //          //  log2Up(params.lineMemSize) - 1 downto 0
            //          //),
            //          //address=(params.lineMemSize + 1 - tempInp.bakCnt)(
            //          //  log2Up(params.lineMemSize) - 1 downto 0
            //          //),
            //          //readUnderWrite=writeFirst,
            //        )
            //      )
            //      tempOutp.objRdLineMemEntry := (
            //        objLineMemArr(jdx).readAsync(
            //          address=tempCombineLineMemIdx
            //          //address=tempInp.bakCnt(
            //          //  log2Up(params.lineMemSize) - 1 downto 0
            //          //),
            //          //address=(tempInp.bakCnt + 1)(
            //          //  log2Up(params.lineMemSize) - 1 downto 0
            //          //),
            //          //address=tempInp.cnt(
            //          //  log2Up(params.lineMemSize) - 1 downto 0
            //          //),
            //          //address=(params.lineMemSize + 1 - tempInp.bakCnt)(
            //          //  log2Up(params.lineMemSize) - 1 downto 0
            //          //),
            //          //readUnderWrite=writeFirst,
            //        )
            //      )
            //    }
            //  }
            //}
          }

          //tempOut.stage0 := tempInp.stage0
          //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
          //stageData.pipeOut(idx).stage1 := stageData.pipeIn(idx).stage1

          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          when (clockDomain.isResetActive) {
            tempOutp.stage1 := tempOutp.stage1.getZero
          } otherwise {
            tempOutp.stage1 := tempInp.stage1
          }
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(2)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          //when (
          //  tempInp.bgRdLineMemEntry.col.a
          //  && tempInp.objRdLineMemEntry.col.a
          //) {
          //  when (
          //    // sprites take priority upon a tie, hence `<=`
          //    tempInp.objRdLineMemEntry.prio
          //    <= tempInp.bgRdLineMemEntry.prio
          //  ) {
          //    tempOutp.col := tempInp.objRdLineMemEntry.col.rgb
          //  } otherwise {
          //    tempOutp.col := tempInp.bgRdLineMemEntry.col.rgb
          //  }
          //} elsewhen (
          //  tempInp.bgRdLineMemEntry.col.a
          //) {
          //  tempOutp.col := tempInp.bgRdLineMemEntry.col.rgb
          //} elsewhen (
          //  tempInp.objRdLineMemEntry.col.a
          //) {
          //  tempOutp.col := tempInp.objRdLineMemEntry.col.rgb
          //} otherwise {
          //  tempOutp.col := tempOutp.col.getZero
          //}

          // BEGIN: Debug comment this out
          when (clockDomain.isResetActive) {
            tempOutp.stage2 := tempOutp.stage2.getZero
          } otherwise {
            tempOutp.objHiPrio := (
              // sprites take priority upon a tie, hence `<=`
              tempInp.objRdLineMemEntry.prio
              <= tempInp.bgRdLineMemEntry.prio
            )
          }
          // END: Debug comment this out

          // BEGIN: Debug test no sprites
          //tempOutp.objHiPrio := False
          // END: Debug test no sprites

          //tempOut.stage0 := tempInp.stage0
          //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
          //stageData.pipeOut(idx).stage2 := stageData.pipeIn(idx).stage2
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          when (clockDomain.isResetActive) {
            tempOutp.stage2 := tempOutp.stage2.getZero
          } otherwise {
            tempOutp.stage2 := tempInp.stage2
          }
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(3)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          // BEGIN: Debug comment this out
          when (clockDomain.isResetActive) {
            tempOutp.stage3 := tempOutp.stage3.getZero
          } otherwise {
            switch (Cat(
              tempInp.bgRdLineMemEntry.col.a,
              tempInp.objRdLineMemEntry.col.a,
              tempInp.objHiPrio
            )) {
              is (B"111") {
                tempOutp.col := tempInp.objRdLineMemEntry.col.rgb
              }
              is (B"110") {
                tempOutp.col := tempInp.bgRdLineMemEntry.col.rgb
              }
              is (M"10-") {
                tempOutp.col := tempInp.bgRdLineMemEntry.col.rgb
              }
              is (M"01-") {
                tempOutp.col := tempInp.objRdLineMemEntry.col.rgb
              }
              //is (M"00-")
              default {
                tempOutp.col := tempOutp.col.getZero
              }
            }
            // END: Debug comment this out
            //tempOutp.col := tempInp.bgRdLineMemEntry.col.rgb
            //tempOutp.col := tempInp.objRdLineMemEntry.col.rgb
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).stage3 := stageData.pipeIn(idx).stage3
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          when (clockDomain.isResetActive) {
            tempOutp.stage3 := tempOutp.stage3.getZero
          } otherwise {
            tempOutp.stage3 := tempInp.stage3
          }
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(4)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          when (clockDomain.isResetActive) {
            tempOutp.stage4 := tempOutp.stage4.getZero
          } otherwise {
            tempOutp.combineWrLineMemEntry.col.rgb := tempInp.col

            // not really necessary, but doing it anyway
            tempOutp.combineWrLineMemEntry.col.a := True

            tempOutp.combineWrLineMemEntry.prio := 0x0
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
          //stageData.pipeOut(idx).stage4 := stageData.pipeIn(idx).stage4
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          when (clockDomain.isResetActive) {
            tempOutp.stage4 := tempOutp.stage4.getZero
          } otherwise {
            tempOutp.stage4 := tempInp.stage4
          }
        },
      )

      when (
        //combinePipeLast.fire
        combinePipeOut.last.fire
      ) {
        //switch (
        //  //rCombineWrLineMemArrIdx
        //  rCombineWrLineMemArrIdx
        //  //combinePipeLast.wrLineMemArrIdx
        //) {
        //  for (
        //    idx <- 0 to (1 << rCombineWrLineMemArrIdx.getWidth) - 1
        //    //idx <- 0 
        //    //to (1 << combinePipeLast.wrLineMemArrIdx.getWidth) - 1
        //  ) {
        //    is (idx) {
        //      bgLineMemArr(idx).write(
        //        address=combinePipeLast.bakCnt(
        //          log2Up(params.lineMemSize) - 1 downto 0
        //        ),
        //        data=combinePipeLast.combineWrLineMemEntry,
        //      )
        //    }
        //  }
        //}
        //outp.col := combinePipeLast.combineWrLineMemEntry.col.rgb
        outp.col := combinePipeOut.last.combineWrLineMemEntry.col.rgb
        rdPhysCalcPosEn := True
      } otherwise {
        outp.col := rPastOutp.col
        rdPhysCalcPosEn := False
      }
    }
    //--------
    //val rdBgLineMemEntry = KeepAttribute(LineMemEntry())
    //val rPastRdBgLineMemEntry = RegNext(rdBgLineMemEntry)
    //  .init(rdBgLineMemEntry.getZero)
    //val rdObjLineMemEntry = KeepAttribute(LineMemEntry())
    //val rPastRdObjLineMemEntry = RegNext(rdObjLineMemEntry)
    //  .init(rdObjLineMemEntry.getZero)

    //val rdLineMemArrIdx = KeepAttribute(UInt(1 bits))
    //rdLineMemArrIdx(0) := !outp.bgPxsPosSlice.pos.y(0)
    //val rdLineMemIdx = KeepAttribute(cloneOf(outp.bgPxsPosSlice.pos.x))
    //rdLineMemIdx := outp.bgPxsPosSlice.pos.x

    //def readLineMemEntries(
    //  //someRdLineMemArrIdx: Int,
    //): Unit = {
    //  //val bgLineMem = bgLineMemArr(someRdLineMemArrIdx)
    //  //rdBgLineMemEntry := bgLineMem.readAsync(address=rdLineMemIdx)

    //  //val objLineMem = objLineMemArr(someRdLineMemArrIdx)
    //  //rdObjLineMemEntry := objLineMem.readAsync(address=rdLineMemIdx)

    //  //val combineLineMem = combineLineMemArr(someRdLineMemArrIdx)
    //  //rdCombineLineMemEntry := combineLineMem.readAsync(
    //  //  address=rdLineMemIdx
    //  //)
    //  //for (idx <- 0 to rdPipeIn.size - 1) {
    //  //  //rdPipeIn(idx).ready := True
    //  //  rdPipeOut(idx).ready := True
    //  //}

    //  val stageData = DualPipeStageData[Stream[RdPipePayload]](
    //    pipeIn=rdPipeIn,
    //    pipeOut=rdPipeOut,
    //    pipeNumMainStages=rdPipeNumMainStages,
    //    pipeStageIdx=0,
    //  )
    //  HandleDualPipe(
    //    stageData=stageData.craft(0)
    //  )(
    //    pipeStageMainFunc=(
    //      stageData: DualPipeStageData[Stream[RdPipePayload]],
    //      idx: Int,
    //    ) => {
    //      val tempInp = stageData.pipeIn(idx)
    //      val tempOutp = stageData.pipeOut(idx)

    //      when (clockDomain.isResetActive) {
    //        tempOutp.stage0 := tempOutp.stage0.getZero
    //      } otherwise {
    //        //tempOut.stage0 := tempInp.stage0
    //        //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
    //        //stageData.pipeIn(idx).ready := True
    //        //tempOutp.cnt := tempInp.cnt
    //        //tempOutp.bakCnt := tempInp.bakCnt
    //        //tempOutp.bakCntMinus1 := tempInp.bakCntMinus1
    //        ////tempOutp.tempCntSlice := (tempInp.cnt + 1)
    //        //tempOutp.tempCntSlice := tempInp.cnt(
    //        //  (tempOutp.tempCntSlice.high + params.physFbSize2dScalePow.x)
    //        //  downto params.physFbSize2dScalePow.x
    //        //);
    //        tempOutp.stage0 := tempInp.stage0
    //      } 
    //    },
    //    copyOnlyFunc=(
    //      stageData: DualPipeStageData[Stream[RdPipePayload]],
    //      idx: Int,
    //    ) => {
    //      ////stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
    //      //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
    //      ////stageData.pipeIn(idx).ready := True
    //      val tempInp = stageData.pipeIn(idx)
    //      val tempOutp = stageData.pipeOut(idx)

    //      when (clockDomain.isResetActive) {
    //        tempOutp.stage0 := tempOutp.stage0.getZero
    //      } otherwise {
    //        tempOutp.stage0 := tempInp.stage0
    //      }
    //    },
    //  )
    //  HandleDualPipe(
    //    stageData=stageData.craft(1)
    //  )(
    //    pipeStageMainFunc=(
    //      stageData: DualPipeStageData[Stream[RdPipePayload]],
    //      idx: Int,
    //    ) => {
    //      val tempInp = stageData.pipeIn(idx)
    //      val tempOutp = stageData.pipeOut(idx)

    //      when (clockDomain.isResetActive) {
    //        tempOutp.stage1 := tempOutp.stage1.getZero
    //      } otherwise {
    //        switch (
    //          rRdLineMemArrIdx
    //          //tempInp.lineMemArrIdx
    //        ) {
    //          for (
    //            jdx <- 0 to (1 << rRdLineMemArrIdx.getWidth) - 1
    //            //jdx <- 0 to (1 << tempInp.lineMemArrIdx.getWidth) - 1
    //          ) {
    //            is (jdx) {
    //              tempOutp.bgLineMemEntry := bgLineMemArr(jdx).readAsync(
    //                //address=tempInp.cnt(
    //                //  log2Up(params.lineMemSize) - 1 downto 0
    //                //)
    //                //address=tempInp.getCntPxPosX(),
    //                address=tempInp.tempCntSlice,
    //                //readUnderWrite=writeFirst,
    //              )
    //            }
    //          }
    //        }
    //        //stageData.pipeIn(idx).ready := True
    //      }
    //    },
    //    copyOnlyFunc=(
    //      stageData: DualPipeStageData[Stream[RdPipePayload]],
    //      idx: Int,
    //    ) => {
    //      ////stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
    //      //stageData.pipeOut(idx).stage1 := stageData.pipeIn(idx).stage1
    //      ////stageData.pipeIn(idx).ready := True
    //      val tempInp = stageData.pipeIn(idx)
    //      val tempOutp = stageData.pipeOut(idx)

    //      when (clockDomain.isResetActive) {
    //        tempOutp.stage1 := tempOutp.stage1.getZero
    //      } otherwise {
    //        tempOutp.stage1 := tempInp.stage1
    //      }
    //    },
    //  )

    //  //when (rdPipeLastIn.fire) {
    //  //  switch (rRdLineMemArrIdx) {
    //  //    for (idx <- 0 to (1 << rRdLineMemArrIdx.getWidth) - 1) {
    //  //      is (idx) {
    //  //        //bgLineMemArr(idx).write(
    //  //        //  address=combinePipeLast.cnt(
    //  //        //    log2Up(params.lineMemSize) - 1 downto 0
    //  //        //  ),
    //  //        //  data=combinePipeLast.combineWrLineMemEntry,
    //  //        //)
    //  //        outp.col := rdPipeLastIn.bgLineMemEntry.col.rgb

    //  //        objLineMemArr(idx).write(
    //  //          address=rdPipeLastIn.cnt,
    //  //          data=ObjLineMemEntry().getZero,
    //  //        )
    //  //      }
    //  //    }
    //  //  }
    //  //} otherwise {
    //  //  outp.col := rPastOutp.col
    //  //}

    //  //when (rdPipeLastIn.fire) {
    //  //  switch (rRdLineMemArrIdx) {
    //  //    for (idx <- 0 to (1 << rRdLineMemArrIdx.getWidth) - 1) {
    //  //      is (idx) {
    //  //        //bgLineMemArr(idx).write(
    //  //        //  address=combinePipeLast.cnt(
    //  //        //    log2Up(params.lineMemSize) - 1 downto 0
    //  //        //  ),
    //  //        //  data=combinePipeLast.combineWrLineMemEntry,
    //  //        //)
    //  //        outp.col := rdPipeLastIn.bgLineMemEntry.col.rgb
    //  //        //popPsbIoPrevPayload.col := (
    //  //        //  rdPipeLastPayload.bgLineMemEntry.col.rgb
    //  //        //)
    //  //        //popPsbIoPrevPayload.physPosInfo
    //  //        //popPsbIoPrevPayload := outp

    //  //        objLineMemArr(idx).write(
    //  //          address=rdPipeLastIn.bakCnt(
    //  //            log2Up(params.lineMemSize) - 1 downto 0
    //  //          ),
    //  //          data=ObjLineMemEntry().getZero,
    //  //        )
    //  //      }
    //  //    }
    //  //  }
    //  //} otherwise {
    //  //  outp.col := rPastOutp.col
    //  //}

    //  //when (rRdLineMemCnt < params.intnlFbSize2d.x) {
    //  //  val tempObjLineMemEntry = ObjLineMemEntry()
    //  //  tempObjLineMemEntry := tempObjLineMemEntry.getZero
    //  //  switch (rRdLineMemArrIdx) {
    //  //    for (idx <- 0 to (1 << rRdLineMemArrIdx.getWidth) - 1) {
    //  //      is (idx) {
    //  //        objLineMemArr(idx).write(
    //  //          address=rRdLineMemCnt,
    //  //          data=tempObjLineMemEntry,
    //  //        )
    //  //      }
    //  //    }
    //  //  }
    //  //}
    //}
    //clearObjLineMemEntries()

    //switch (rWrLineMemArrIdx) {
    //  for (idx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1) {
    //    is (idx) {
    //      writeBgLineMemEntries(someWrLineMemArrIdx=idx)
    //      writeObjLineMemEntries(someWrLineMemArrIdx=idx)
    //    }
    //  }
    //  //default {
    //  //  wrLineMemEntry := rPastWrLineMemEntry
    //  //}
    //}
    writeBgLineMemEntries()
    writeObjLineMemEntries()

    //switch (rCombineLineMemArrIdx) {
    //  for (idx <- 0 to (1 << rCombineLineMemArrIdx.getWidth) - 1) {
    //    is (idx) {
    //      combineLineMemEntries(someCombineLineMemArrIdx=idx)
    //    }
    //  }
    //  //default {
    //  //  wrLineMemEntry := rPastWrLineMemEntry
    //  //}
    //}
    doSendIntoFifo()
    //doSendIntoObjFifo()
    combineLineMemEntries()
    ////when (pop.fire) {
    //readLineMemEntries()

    // BEGIN: add this back later
    //switch (rRdLineMemArrIdx) {
    //  for (idx <- 0 to (1 << rRdLineMemArrIdx.getWidth) - 1) {
    //    is (idx) {
    //      readLineMemEntries(someRdLineMemArrIdx=idx)
    //    }
    //  }
    //  //default {
    //  //  //rdBgLineMemEntry := rPastRdBgLineMemEntry
    //  //  outp.col := rPastOutp.col
    //  //}
    //}
    // END: add this back later
    //outp.col := rPastOutp.col

    ////}
    //--------
    //for (idx <- 0 to params.numBgs - 1) {
    //  //bgColVec(idx) := palEntryMem.readAsync(
    //  //  address=
    //}
    //--------
    //--------
  }
}
