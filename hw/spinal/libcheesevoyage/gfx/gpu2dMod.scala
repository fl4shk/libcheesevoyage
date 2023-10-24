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
//import scala.collection.immutable._
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
  //numObjsPerScanline: Int,    // how many sprites to process in one
  //                            // scanline (possibly one per cycle?)
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

  //def oneLineMemSize = physFbSize2dScale.y * params.physFbSize2d.x

  //def halfLineMemSize = intnlFbSize2d.x
  //def oneLineMemSize = intnlFbSize2d.x * 2
  //def oneLineMemSize = intnlFbSize2d.x
  //def oneLineMemSize = intnlFbSize2d.x
  def oneLineMemSize = intnlFbSize2d.x
  //def wholeLineMemSize = 1 << log2Up(intnlFbSize2d.x)

  // BEGIN: old, working but non-synthesizable code
  //def bgSubLineMemArrSizePow = bgTileSize2dPow.x
  //def bgSubLineMemArrSize = 1 << bgSubLineMemArrSizePow
  //def bgSubLineMemSizePow = (
  //  log2Up(oneLineMemSize) - bgSubLineMemArrSizePow
  //)
  ////def bgSubLineMemSize = oneLineMemSize >> bgSubLineMemArrSizePow
  //def bgSubLineMemSize = 1 << bgSubLineMemSizePow

  // BEGIN: old, working but non-synthesizable code; comment out later
  //def objSubLineMemArrSizePow = objTileSize2dPow.x
  //def objSubLineMemArrSize = 1 << objSubLineMemArrSizePow
  //def objSubLineMemSizePow = (
  //  log2Up(oneLineMemSize) - objSubLineMemArrSizePow
  //)
  ////def objSubLineMemSize = oneLineMemSize >> objSubLineMemArrSizePow
  //def objSubLineMemSize = 1 << objSubLineMemSizePow
  ////println(
  ////  f"oneLineMemSize:$oneLineMemSize "
  ////  + f"ArrSizePow:$objSubLineMemArrSizePow "
  ////  + f"objSubLineMemArrSize:$objSubLineMemArrSize "
  ////  + f"objSubLineMemSize:$objSubLineMemSize "
  ////)

  //def getEitherSubLineMemTempArrIdx(
  //  pxPosX: UInt,
  //  ////someSubLineMemArrSizePow: Int
  //  //isObj: Boolean,
  //) = {
  //  assert(pxPosX.getWidth >= log2Up(oneLineMemSize))
  //  //pxPosX(objTileSize2dPow.x - 1 downto 0)
  //  //pxPosX(objSubLineMemArrSizePow - 1 downto 0)
  //  //if (!isObj) {
  //  //} else {
  //    pxPosX(
  //      (
  //        //if (!isObj) bgSubLineMemArrSizePow else objSubLineMemArrSizePow
  //        objSubLineMemArrSizePow
  //      ) - 1 downto 0
  //    )
  //  //}
  //}

  ////def getBgSubLineMemTempArrIdx(
  ////  pxPosX: UInt
  ////) = getEitherSubLineMemTempArrIdx(pxPosX=pxPosX, isObj=false)
  //def getObjSubLineMemTempArrIdx(
  //  pxPosX: UInt
  //) = getEitherSubLineMemTempArrIdx(
  //  pxPosX=pxPosX,
  //  //isObj=true
  //)


  //def getEitherSubLineMemTempAddr(
  //  pxPosX: UInt,
  //  //isObj: Boolean,
  //) = {
  //  assert(pxPosX.getWidth >= log2Up(oneLineMemSize))

  //  pxPosX(
  //    log2Up(oneLineMemSize) - 1
  //    downto (
  //      //if (!isObj) {
  //      //  bgSubLineMemArrSizePow
  //      //} else 
  //      {
  //        objSubLineMemArrSizePow
  //      }
  //    )
  //    //(
  //    //  //objSubLineMemArrSizePow
  //    //  if (!isObj) bgSubLineMemArrSizePow else objSubLineMemArrSizePow
  //    //) - 1 downto 0
  //  )
  //}
  ////def getBgSubLineMemTempAddr(
  ////  pxPosX: UInt
  ////) = getEitherSubLineMemTempAddr(pxPosX, isObj=false)
  //def getObjSubLineMemTempAddr(
  //  pxPosX: UInt
  //) = getEitherSubLineMemTempAddr(
  //  pxPosX=pxPosX,
  //  //isObj=true
  //)
  // END: old, working but non-synthesizable code; comment out later
  //--------
  // BEGIN: new code, for implementing purely dual-port RAM for synthesis
  def bgSubLineMemArrSizePow = (
    log2Up(oneLineMemSize) - bgTileSize2dPow.x
  )
  def bgSubLineMemArrSize = 1 << bgSubLineMemArrSizePow
  def getBgSubLineMemArrIdx(
    addr: UInt
  ) = {
    assert(addr.getWidth >= log2Up(oneLineMemSize))
    addr(log2Up(oneLineMemSize) - 1 downto bgTileSize2dPow.x)
  }
  def getBgSubLineMemArrElemIdx(
    addr: UInt
  ) = {
    assert(addr.getWidth >= log2Up(oneLineMemSize))
    addr(bgTileSize2dPow.x - 1 downto 0)
  }

  def objSubLineMemArrSizePow = (
    log2Up(oneLineMemSize) - objTileSize2dPow.x
  )
  def objSubLineMemArrSize = 1 << objSubLineMemArrSizePow
  def getObjSubLineMemArrIdx(
    addr: UInt
  ): UInt = {
    assert(addr.getWidth >= log2Up(oneLineMemSize))
    addr(log2Up(oneLineMemSize) - 1 downto objTileSize2dPow.x)
  }
  def getObjSubLineMemArrGridIdx(
    addr: UInt
  ): Bool = {
    assert(addr.getWidth >= log2Up(oneLineMemSize))
    //addr(objTileSize2dPow.x downto objTileSize2dPow.x)
    addr(objTileSize2dPow.x)
  }
  def getObjSubLineMemArrElemIdx(
    addr: UInt
  ): UInt = {
    assert(addr.getWidth >= log2Up(oneLineMemSize))
    addr(objTileSize2dPow.x - 1 downto 0)
  }
  //--------
  //def lineFifoDepth = oneLineMemSize * 2 + 1
  //def lineFifoDepth = oneLineMemSize + 1
  //def numLineMems = 2
  //def numLineMems = 1 << physFbSize2dScalePow.y
  //def numLineMems = 4
  //def numLineMems = 2
  //def numLineMemsPerBgObjRenderer = 4
  def numLineMemsPerBgObjRenderer = 2
  //def numLineMems = numBgs
  //def wrBgObjStallFifoAmountCanPush = 8
  //def combinePipeOverflowFifoSize = oneLineMemSize
  //def combinePipeOverflowOccupancyAt = combinePipeOverflowFifoSize - 8
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

  def objSize2dForAttrs = ElabVec2[Int](
    x=objTileSize2d.x + 1,
    y=objTileSize2d.y + 1,
  )
  def objSize2dForAttrsPow = ElabVec2[Int](
    x=log2Up(objSize2dForAttrs.x),
    y=log2Up(objSize2dForAttrs.y),
  )
  def objSize2dForAttrsT() = coordT(someSize2d=objSize2dForAttrs)
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
    pxCoord: ElabVec2[Int],
    colIdx: UInt,
  ): Unit = {
    assert(pxCoord.x >= 0 && pxCoord.x < pxsSize2d.x)
    assert(pxCoord.y >= 0 && pxCoord.y < pxsSize2d.y)
    //val rowVec = getRowAsVec(pxCoord.y)
    //rowVec(pxCoord.x) := colIdx
    colIdxRowVec(pxCoord.y)(pxCoord.x) := colIdx
    def pxsCoordX = pxCoord.x
    def pxsCoordY = pxCoord.y
    //println(f"$pxsCoordX $pxsCoordY $colIdx")

    //colIdxRowVec(pxCoord.y)(
    //  ((pxCoord.x + 1) * colIdxWidth - 1)
    //  downto (pxCoord.x * colIdxWidth)
    //) := colIdx

    //colIdxRowVec(pxCoord.y)(
    //  pxCoord.x,
    //  //colIdxWidth bits
    //  log2Up(pxsSize2d.x) bits
    //) := colIdx

    //colIdxRowVec(pxCoord.y).assignFromBits(rowVec.asBits)
  }
  //def setPx(
  //  pxCoord: DualTypeNumVec2[UInt, UInt],
  //  colIdx: UInt,
  //): Unit = {
  //  //val row = colIdxRowVec(pxCoord.y)
  //  //val colIdxVec = row.subdivideIn(pxsSize2d.x slices)
  //  //colIdxVec(pxCoord.x) := colIdx
  //  //row.assignFromBits(colIdxVec.asBits)

  //  switch (pxCoord.y) {
  //    for (jdx <- 0 to pxsSize2d.y - 1) {
  //      is (jdx) {
  //        switch (pxCoord.x) {
  //          for (idx <- 0 to pxsSize2d.x - 1) {
  //            is (idx) {
  //              setPx(
  //                pxCoord=ElabVec2[Int](x=idx, y=jdx),
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
    pxCoord: ElabVec2[Int],
  ) = {
    assert(pxCoord.x >= 0 && pxCoord.x < pxsSize2d.x)
    assert(pxCoord.y >= 0 && pxCoord.y < pxsSize2d.y)
    val rowVec = getRowAsVec(pxCoord.y)
    //rowVec(pxCoord.x) := colIdx
    //colIdxRowVec(pxCoord.y).assignFromBits(rowVec.asBits)
    rowVec(pxCoord.x)
  }
  def getPx(
    pxCoord: DualTypeNumVec2[UInt, UInt]
  ) = {
    //val row = colIdxRowVec(pxCoord.y.resized)
    //val colIdxVec = row.subdivideIn(pxsSize2d.x slices)
    ////colIdxVec(pxCoord.x) := colIdx
    ////row.assignFromBits(colIdxVec.asBits)
    //colIdxVec(pxCoord.x.resized)
    colIdxRowVec(pxCoord.y)(pxCoord.x)
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
  // Whether or not to display this BG
  val visib = Bool()

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

  //val size2dMinus1x1 = params.objTilePxsCoordT()
  val size2d = params.objSize2dForAttrsT()

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

case class Gpu2dPushInp(
  params: Gpu2dParams=DefaultGpu2dParams()
) extends Bundle with IMasterSlave {
  //--------
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
  def asMaster(): Unit = {
    master(
      bgTilePush,
      //bgEntryPushArr,
      //bgAttrsPushArr,
      bgPalEntryPush,
      objTilePush,
      objAttrsPush,
      objPalEntryPush,
    )
    for (idx <- 0 to params.numBgs - 1) {
      master(
        bgEntryPushArr(idx),
        bgAttrsPushArr(idx),
      )
    }
  }
  //--------
  def <<(
    that: Gpu2dPushInp
  ): Unit = {
    this.bgTilePush << that.bgTilePush
    for (idx <- 0 to params.numBgs - 1) {
      this.bgEntryPushArr(idx) << that.bgEntryPushArr(idx)
      this.bgAttrsPushArr(idx) << that.bgAttrsPushArr(idx)
    }
    this.bgPalEntryPush << that.bgPalEntryPush
    this.objTilePush << that.objTilePush
    this.objAttrsPush << that.objAttrsPush
    this.objPalEntryPush << that.objPalEntryPush
  }
  def >>(
    that: Gpu2dPushInp
  ): Unit = {
    that << this 
  }
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
  val push = Gpu2dPushInp(params=params)
  def bgTilePush = push.bgTilePush
  def bgEntryPushArr = push.bgEntryPushArr
  def bgAttrsPushArr = push.bgAttrsPushArr
  def bgPalEntryPush = push.bgPalEntryPush
  def objTilePush = push.objTilePush
  def objAttrsPush = push.objAttrsPush
  def objPalEntryPush = push.objPalEntryPush
  //--------
  val ctrlEn = out Bool()
  val pop = master Stream(Gpu2dPopPayload(params=params))
  //--------
  //def asMaster(): Unit = {
  //  master(
  //    bgTilePush,
  //    //bgEntryPushArr,
  //    //bgAttrsPushArr,
  //    bgPalEntryPush,
  //    objTilePush,
  //    objAttrsPush,
  //    objPalEntryPush,
  //  )
  //  for (idx <- 0 to params.numBgs - 1) {
  //    master(
  //      bgEntryPushArr(idx),
  //      bgAttrsPushArr(idx),
  //    )
  //  }
  //  //in
  //  out(
  //    ctrlEn,
  //  )
  //  slave(
  //    pop
  //  )
  //}
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
  //val rPastCtrlEn = RegNext(ctrlEn) init(False)
  //val rCtrlEn = Reg(Bool()) init(False)
  //ctrlEn := rCtrlEn
  ctrlEn := True

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
  //when (pop.fire) {
  //  ctrlEn := True
  //  //rCtrlEn := True
  //} otherwise {
  //  ctrlEn := rPastCtrlEn
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
      //.addAttribute("ram_mode", "tdp")
    when (bgTilePush.fire) {
      bgTileMem.write(
        address=bgTilePush.payload.memIdx,
        data=bgTilePush.payload.tile,
      )
    }
    val objTileMem = Mem(
      //wordType=UInt(params.palEntryMemIdxWidth bits),
      wordType=Gpu2dTile(params=params, isObj=true),
      //wordCount=params.numPxsForAllObjTiles,
      wordCount=params.numObjTiles,
    )
      .initBigInt(Array.fill(params.numObjTiles)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.objTileArrRamStyle)
      //.addAttribute("ram_mode", "tdp")
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
        //.addAttribute("ram_mode", "tdp")
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
      //.addAttribute("ram_mode", "tdp")
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
      //.addAttribute("ram_mode", "tdp")
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
      //.addAttribute("ram_mode", "tdp")
    when (objPalEntryPush.fire) {
      objPalEntryMem.write(
        address=objPalEntryPush.payload.memIdx,
        data=objPalEntryPush.payload.objPalEntry,
      )
    }
    //--------
    //val rWrBgChangingRowCnt = KeepAttribute(
    //  Reg(UInt(log2Up(params.oneLineMemSize) + 1 bits))
    //  init(params.oneLineMemSize - 1)
    //)
    //val rIntnlChangingRow = Reg(Bool()) init(False)
    //val rIntnlChangingRow = Reg(Bool()) init(True)

    val nextIntnlChangingRow = Bool()
    //val rIntnlChangingRow = RegNext(nextIntnlChangingRow) init(True)
    val rIntnlChangingRow = RegNext(nextIntnlChangingRow) init(False)

    val nextWrBgChangingRow = Bool()
    val rWrBgChangingRow = RegNext(nextWrBgChangingRow) init(False)
    val nextWrObjChangingRow = Bool()
    val rWrObjChangingRow = RegNext(nextWrObjChangingRow) init(False)
    val nextCombineChangingRow = Bool()
    val rCombineChangingRow = RegNext(nextCombineChangingRow) init(False)
    //val nextRdChangingRow = Bool()
    //val rRdChangingRow = RegNext(nextRdChangingRow) init(False)
    //val nextRdChangingRowRe = Bool()

    val rPastIntnlChangingRow = RegNext(rIntnlChangingRow) init(False)
    val intnlChangingRowRe = KeepAttribute(Bool())
    intnlChangingRowRe := rIntnlChangingRow && !rPastIntnlChangingRow
    //val nextIntnlChangingRowRe = KeepAttribute(Bool())
    //nextIntnlChangingRowRe := nextIntnlChangingRow && !rIntnlChangingRow
    nextIntnlChangingRow := (
      // all of the pipelines must have finished the current scanline
      //outp.physPosInfo.nextPos.x === 0
      //outp.physPosInfo.changingRow
      rWrBgChangingRow
      && rWrObjChangingRow
      && rCombineChangingRow

      //&& rRdChangingRow

      //nextWrBgChangingRow
      //&& nextWrObjChangingRow
      //&& nextCombineChangingRow
      //&& nextRdChangingRow
      //(
      //  //rPastOutp.bgPxsPosSlice.pos.y
      //  //=/= rPastOutp.bgPxsPosSlice.pastPos.y
      //  //outp.physPosInfo.pos.y
      //  //=/= outp.physPosInfo.pastPos.y
      //  //outp.physPosInfo.changingRow
      //  outp.physPosInfo.nextPos.x === 0
      //)
      //&& (
      //  rPastOutp.bgPxsPosSlice.pos.y === 0
      //) && (
      //  rPastOutp.bgPxsPosSlice.pastPos.y === params.intnlFbSize2d.y - 1
      //)
      //(
      //  // since we're using `rPastOutp`, `nextPos` is registered, and thus
      //  // there is higher FMax
      //  //(
      //  //  rPastOutp.bgPxsPosSlice.nextPos.y
      //  //  =/= rPastOutp.bgPxsPosSlice.pos.y
      //  //) && (
      //  //  rPastOutp.bgPxsPosSlice.nonSliceChangingRow
      //  //)
      //  //rPastOutp.bgPxsPosSlice.nextPos.x === 0

      //  //pop.fire 
      //  ////fifoPush.fire
      //  rIntnlChangingRowCnt.msb
      //  && 
      //  rPastOutp.bgPxsPosSlice.pos.x === 0
      //  && rPastOutp.physPosInfo.changingRow

      //  ////rPastOutp.bgPxsPosSlice.pos.y
      //  ////=/= rPastOutp.bgPxsPosSlice.pastPos.y
      //  ////=== rPastOutp.bgPxsPosSlice.pastPos.y + 1
      //)
      //|| (
      //  rPastOutp.bgPxs
      //)
    )
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
    //case class BgSubLineMemEntry() extends Bundle {
    //  val col = Gpu2dRgba(params=params)
    //  val prio = UInt(params.numBgsPow bits)
    //}
    case class BgSubLineMemEntry() extends Bundle {
      val col = Gpu2dRgba(params=params)
      val prio = UInt(params.numBgsPow bits)
      //val addr = UInt(params.bgSubLineMemArrSizePow bits)
      val addr = UInt(log2Up(params.oneLineMemSize) bits)
      def getSubLineMemTempArrIdx() = (
        params.getBgSubLineMemArrIdx(addr=addr)
      )
      //val addr = UInt(log2Up(params.oneLineMemSize) bits)
      //def getSubLineMemTempArrIdx() = params.getBgSubLineMemTempArrIdx(
      //  pxPosX=addr
      //)
      //def getSubLineMemTempAddr() = params.getBgSubLineMemTempAddr(
      //  pxPosX=addr
      //)
    }
    case class ObjSubLineMemEntry() extends Bundle {
      val col = Gpu2dRgba(params=params)
      //val rawPrio = UInt((params.numBgsPow + 1) bits)
      val written = Bool()
      val prio = UInt(params.numBgsPow bits)
      //def objAttrsMemIdx
      val addr = UInt(log2Up(params.oneLineMemSize) bits)
      def getSubLineMemTempArrIdx() = (
        params.getObjSubLineMemArrIdx(addr=addr)
      )
      //val addr = UInt(log2Up(params.oneLineMemSize) bits)
      //def getSubLineMemTempArrIdx() = params.getObjSubLineMemTempArrIdx(
      //  pxPosX=addr
      //)
      //def getSubLineMemTempAddr() = params.getObjSubLineMemTempAddr(
      //  pxPosX=addr
      //)
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
    //val lineMemArr = new ArrayBuffer[Mem[BgSubLineMemEntry]]()

    //case class LineFifoEntry() extends Bundle {
    //  val bgLineMemEntry = BgSubLineMemEntry()
    //  val objLineMemEntry = ObjSubLineMemEntry()
    //}
    //val lineFifo = AsyncReadFifo(
    //  dataType=LineFifoEntry(),
    //  depth=params.lineFifoDepth,
    //)

    //val lineFifo = AsyncReadFifo(
    //  dataType=BgSubLineMemEntry(),
    //  //depth=params.oneLineMemSize * 2 + 1,
    //  depth=params.lineFifoDepth,
    //)
    //val objLineFifo = AsyncReadFifo(
    //  dataType=ObjSubLineMemEntry(),
    //  //depth=params.oneLineMemSize * 2 + 1,
    //  depth=params.lineFifoDepth,
    //)


    //val bgLineMem = Mem(
    //  wordType=BgSubLineMemEntry(),
    //  wordCount=(
    //    params.oneLineMemSize
    //  )
    //)
    val bgSubLineMemArr = new ArrayBuffer[Mem[Vec[BgSubLineMemEntry]]]()
    val objSubLineMemArr = new ArrayBuffer[Mem[Vec[ObjSubLineMemEntry]]]()

    //val bgSubLineMemA2d = (
    //  new ArrayBuffer[ArrayBuffer[Mem[BgSubLineMemEntry]]]()
    //)
    //val objSubLineMemA2d = (
    //  new ArrayBuffer[ArrayBuffer[Mem[ObjSubLineMemEntry]]]()
    //)

    ////def combineLineMemArr = bgSubLineMemArr
    ////val combineLineMemArr = new ArrayBuffer[Mem[Gpu2dRgba]]()
    ////val wrLineMemIdx

    for (
      idx <- 0 to params.numLineMemsPerBgObjRenderer - 1
    ) {
      //lineMemArr += Mem(
      //  //wordType=Rgb(params.rgbConfig),
      //  wordType=LineMemEntry(),
      //  wordCount=params.oneLineMemSize,
      //)
      //  .initBigInt(Array.fill(params.oneLineMemSize)(BigInt(0)).toSeq)
      //  .addAttribute("ram_style", params.lineArrRamStyle)

      bgSubLineMemArr += Mem(
        //wordType=Rgb(params.rgbConfig),
        //wordType=BgSubLineMemEntry(),
        //wordCount=params.oneLineMemSize,
        wordType=Vec.fill(params.bgTileSize2d.x)(BgSubLineMemEntry()),
        wordCount=params.bgSubLineMemArrSize,
      )
        .initBigInt(
          //Array.fill(params.oneLineMemSize)(BigInt(0)).toSeq
          Array.fill(params.bgSubLineMemArrSize)(BigInt(0)).toSeq
        )
        .addAttribute("ram_style", params.lineArrRamStyle)
        //.addAttribute("ram_mode", "tdp") // true dual-port
        .setName(f"bgLineMemArr_$idx")

      objSubLineMemArr += Mem(
        //wordType=Rgb(params.rgbConfig),
        wordType=Vec.fill(params.objTileSize2d.x)(ObjSubLineMemEntry()),
        wordCount=params.objSubLineMemArrSize,
      )
        .initBigInt(
          //Array.fill(params.oneLineMemSize)(BigInt(0)).toSeq
          Array.fill(params.objSubLineMemArrSize)(BigInt(0)).toSeq
        )
        .addAttribute("ram_style", params.lineArrRamStyle)
      
        //// true dual-port RAM;
        //// needed because of clearing one of the non-active RAMs during
        //// the combine pipeline
        //// while also writing actual data to another RAM during the OBJ
        //// writing pipeline (though the OBJ writing pipeline will
        //// do two writes per sprite because of the grid structure)
        //.addAttribute("ram_mode", "tdp")
      
        .setName(f"objLineMemArr_$idx")
    }
    // BEGIN: old, non synthesizable code (too many write ports...)
    //for (
    //  //jdx <- 0 to params.objSubLineMemArrSize - 1
    //  jdx <- 0 to params.numLineMemsPerBgObjRenderer - 1
    //) {
    //  //bgSubLineMemA2d += ArrayBuffer[Mem[BgSubLineMemEntry]]()

    //  //for (idx <- 0 to params.bgSubLineMemSize - 1) {
    //  //  bgSubLineMemA2d.last += Mem(
    //  //    //wordType=Rgb(params.rgbConfig),
    //  //    wordType=BgSubLineMemEntry(),
    //  //    wordCount=params.bgSubLineMemSize,
    //  //  )
    //  //    .initBigInt(
    //  //      Array.fill(params.bgSubLineMemSize)(BigInt(0)).toSeq
    //  //    )
    //  //    .addAttribute("ram_style", params.lineArrRamStyle)
    //  //    .setName(f"bgSubLineMemA2d_$jdx" + "_" + f"$idx")
    //  //}

    //  //bgSubLineMemA2d += ArrayBuffer[Mem[BgSubLineMemEntry]]()

    //  //for (
    //  //  //idx <- 0 to params.bgSubLineMemSize - 1
    //  //  idx <- 0 to params.bgSubLineMemArrSize - 1
    //  //) {
    //  //  bgSubLineMemA2d.last += Mem(
    //  //    //wordType=Rgb(params.rgbConfig),
    //  //    wordType=BgSubLineMemEntry(),
    //  //    wordCount=params.bgSubLineMemSize,
    //  //  )
    //  //    .initBigInt(
    //  //      Array.fill(params.bgSubLineMemSize)(BigInt(0)).toSeq
    //  //    )
    //  //    .addAttribute("ram_style", params.lineArrRamStyle)
    //  //    .setName(f"bgSubLineMemA2d_$jdx" + "_" + f"$idx")
    //  //}

    //  objSubLineMemA2d += ArrayBuffer[Mem[ObjSubLineMemEntry]]()
    //  for (
    //    //idx <- 0 to params.objSubLineMemSize - 1
    //    idx <- 0 to params.objSubLineMemArrSize - 1
    //  ) {
    //    objSubLineMemA2d.last += Mem(
    //      //wordType=Rgb(params.rgbConfig),
    //      wordType=ObjSubLineMemEntry(),
    //      wordCount=params.objSubLineMemSize,
    //    )
    //      .initBigInt(
    //        Array.fill(params.objSubLineMemSize)(BigInt(0)).toSeq
    //      )
    //      .addAttribute("ram_style", params.lineArrRamStyle)
    //      .setName(f"objSubLineMemA2d_$jdx" + "_" + f"$idx")
    //  }
    //  //def tempSize1 = objSubLineMemA2d.last.size

    //  //def size0 = log2Up(params.oneLineMemSize)
    //  //def size1 = params.objSubLineMemSizePow
    //  //def size2 = params.objSubLineMemArrSizePow
    //  //def size3 = params.oneLineMemSize
    //  //println(f"objSubLineMemA2d: $jdx $size0 $size1 $size2 $size3")
    //}
    // END: old, non synthesizable code (too many write ports...)

    //val dbgBgLineMemVec = Reg(
    //  Vec.fill(params.numLineMemsPerBgObjRenderer)(
    //    Vec.fill(params.oneLineMemSize)(
    //      BgSubLineMemEntry()
    //    )
    //  )
    //)
    //for (vecIdx <- 0 to dbgBgLineMemVec.size - 1) {
    //  for (memIdx <- 0 to dbgBgLineMemVec(vecIdx).size - 1) {
    //    dbgBgLineMemVec(vecIdx)(memIdx).init(BgSubLineMemEntry().getZero)
    //  }
    //}
    //val dbgObjLineMemVec = Reg(
    //  Vec.fill(params.numLineMemsPerBgObjRenderer)(
    //    Vec.fill(params.oneLineMemSize)(
    //      ObjSubLineMemEntry()
    //    )
    //  )
    //)
    //for (vecIdx <- 0 to dbgObjLineMemVec.size - 1) {
    //  for (memIdx <- 0 to dbgObjLineMemVec(vecIdx).size - 1) {
    //    dbgObjLineMemVec(vecIdx)(memIdx).init(ObjSubLineMemEntry().getZero)
    //  }
    //}

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
    //def rawClearObjLineMemArrIdxInit = 0
    //def clearObjLineMemArrIdxInit = U(
    //  f"$lineMemArrIdxWidth'd$rawClearObjLineMemArrIdxInit"
    //)
    def rawCombineLineMemArrIdxInit = (
      //1
      0
    )
    def combineLineMemArrIdxInit = U(
      f"$lineMemArrIdxWidth'd$rawCombineLineMemArrIdxInit"
    )
    //def combineLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd3")
    //def wrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd1")
    //def sendIntoFifoLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    def rawWrLineMemArrIdxInit = (
      //2
      1
    )
    def wrLineMemArrIdxInit = U(
      f"$lineMemArrIdxWidth'd$rawWrLineMemArrIdxInit"
    )
    //def wrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")

    //val rSendIntoFifoLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
    //  init(sendIntoFifoLineMemArrIdxInit)
    //)

    //val rCombineLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
    //  //init(0x3)
    //  init(combineLineMemArrIdxInit)
    //)
    val rWrLineMemArrIdx = KeepAttribute(
      Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
      //init(0x3)
      init(wrLineMemArrIdxInit)
    )
    val rCombineLineMemArrIdx = KeepAttribute(
      Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
      init(combineLineMemArrIdxInit)
    )
    //def wrLineNumInit = 0x3
    //def wrLineNumInit = 0x2
    def wrLineNumWidth = log2Up(params.intnlFbSize2d.y)
    //def wrLineNumInit = 0x1
    //def wrLineNumInit = U(f"$wrLineNumWidth'd1")
    //def wrLineNumInit = U(f"$wrLineNumWidth'd0")
    //def wrLineNumInit = U(f"$wrLineNumWidth'd2")
    //def wrBgLineNumInit = U(f"$wrLineNumWidth'd2")
    def wrBgLineNumInit = U(f"$wrLineNumWidth'd0")
    //def wrBgLineNumInit = U(f"$wrLineNumWidth'd1")
    //def wrObjLineNumInit = U(f"$wrLineNumWidth'd1")
    //def wrObjLineNumInit = U(f"$wrLineNumWidth'd2")
    def wrObjLineNumInit = U(f"$wrLineNumWidth'd0")
    val rGlobWrBgLineNum = KeepAttribute(
      Reg(UInt(wrLineNumWidth bits))
      //init(0x2)
      //init(0x3)
      //init(wrLineNumInit)
      init(wrBgLineNumInit)
    )
    val rGlobWrObjLineNum = KeepAttribute(
      Reg(UInt(wrLineNumWidth bits))
      //init(0x2)
      //init(0x3)
      //init(wrLineNumInit)
      init(wrObjLineNumInit)
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
    val rGlobWrBgLineNumCheckPipe2 = Reg(Bool()) init(False)
    val rGlobWrBgLineNumPlus1Pipe2 = Reg(
      UInt(rGlobWrBgLineNum.getWidth + 1 bits)
    )
      .init(0x0)
    //val rGlobWrBgLineNumPlus1Pipe2 := Reg(cloneOf(rGlobWrBgLineNum))
    //  .init(0x0)
    val rGlobWrBgLineNumPipe1 = Reg(
      UInt(rGlobWrBgLineNum.getWidth + 1 bits)
    )
      .init(0x0)
    rGlobWrBgLineNumCheckPipe2 := (
      rGlobWrBgLineNum.resized =/= params.intnlFbSize2d.y - 1
    )
    def tempWrBgLineNumPipeWidth = rGlobWrBgLineNumPlus1Pipe2.getWidth
    //rGlobWrBgLineNumPlus1Pipe2 := rGlobWrBgLineNum.resized + 1
    rGlobWrBgLineNumPlus1Pipe2 := (
      rGlobWrBgLineNum.resized + U(f"$tempWrBgLineNumPipeWidth'd1")
    )
    when (rGlobWrBgLineNumCheckPipe2) {
      rGlobWrBgLineNumPipe1 := rGlobWrBgLineNumPlus1Pipe2
    } otherwise {
      rGlobWrBgLineNumPipe1 := 0
    }

    val rGlobWrObjLineNumCheckPipe2 = Reg(Bool()) init(False)
    val rGlobWrObjLineNumPlus1Pipe2 = Reg(
      UInt(rGlobWrObjLineNum.getWidth + 1 bits)
    )
      .init(0x0)
    //val rGlobWrObjLineNumPlus1Pipe2 := Reg(cloneOf(rGlobWrObjLineNum))
    //  .init(0x0)
    val rGlobWrObjLineNumPipe1 = Reg(
      UInt(rGlobWrObjLineNum.getWidth + 1 bits)
    )
      .init(0x0)
    rGlobWrObjLineNumCheckPipe2 := (
      rGlobWrObjLineNum.resized =/= params.intnlFbSize2d.y - 1
    )
    def tempWrObjLineNumPipeWidth = rGlobWrObjLineNumPlus1Pipe2.getWidth
    //rGlobWrObjLineNumPlus1Pipe2 := rGlobWrObjLineNum.resized + 1
    rGlobWrObjLineNumPlus1Pipe2 := (
      rGlobWrObjLineNum.resized + U(f"$tempWrObjLineNumPipeWidth'd1")
    )
    when (rGlobWrObjLineNumCheckPipe2) {
      rGlobWrObjLineNumPipe1 := rGlobWrObjLineNumPlus1Pipe2
    } otherwise {
      rGlobWrObjLineNumPipe1 := 0
    }
    //val rPastChangingRow = RegNext(outp.physPosInfo.changingRow)
    //val changingRowRe = outp.physPosInfo.changingRow && !rPastChangingRow

    when (
      //rIntnlChangingRow
      intnlChangingRowRe
      //nextIntnlChangingRowRe
      //outp.physPosInfo.changingRow
      //changingRowRe
    ) {
      //rPastWrLineMemArrIdx := rPastWrLineMemArrIdx + 1
      //rObjClearLineMemArrIdx := rObjClearLineMemArrIdx + 1

      //rObjLineMemClearCnt := 0x0

      //rPastWrLineMemArrIdx := rPastWrLineMemArrIdx + 1

      // BEGIN: old logic, may not be working properly
      //rWrLineMemArrIdx := rWrLineMemArrIdx + 1
      // END: old logic, may not be working properly

      //when (rWrLineNum =/= params.intnlFbSize2d.y - 1) {
      //  rWrLineNum := rWrLineNum + 1
      //} otherwise {
      //  rWrLineNum := 0
      //}
      //rWrLineNum := rWrLineNumPipe1(rWrLineNum.bitsRange)
      //rWrLineNum := rWrLineNum + 1

      rWrLineMemArrIdx := rWrLineMemArrIdx + 1
      rCombineLineMemArrIdx := rCombineLineMemArrIdx + 1
      rGlobWrBgLineNum := (
        rGlobWrBgLineNumPipe1(rGlobWrBgLineNum.bitsRange)
      )
      rGlobWrObjLineNum := (
        rGlobWrObjLineNumPipe1(rGlobWrObjLineNum.bitsRange)
      )
      //rCombineLineMemArrIdx := rCombineLineMemArrIdx + 1
      // BEGIN: old logic, may not be working properly
      //rPastCombineLineMemArrIdx := rPastCombineLineMemArrIdx + 1
      //rCombineRdLineMemArrIdx := rCombineRdLineMemArrIdx + 1
      //rCombineWrLineMemArrIdx := rCombineWrLineMemArrIdx + 1
      ////rCombineLineMemCnt := 0

      ////rPastRdLineMemArrIdx := rPastRdLineMemArrIdx + 1
      //rRdLineMemArrIdx := rRdLineMemArrIdx + 1
      ////rRdLineMemCnt := 0
      //// END: old logic, may not be working properly
    }

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
    // into `bgSubLineMemArr(someWrLineMemArrIdx)`
    // END: OLD NOTES

    // The background-writing pipeline iterates through the
    // `params.numBgs` backgrounds, and draws them into `bgLineMem`
    def wrBgPipeCntWidth = (
      //log2Up(params.intnlFbSize2d.x + 1) + params.numBgsPow + 1

      log2Up(params.intnlFbSize2d.x + 1)
      - params.bgTileSize2dPow.x
      + params.numBgsPow
      + 1
    )
    def wrBgPipeBakCntStart: Int = (
      //(1 << (params.objTileSize2dPow.x + params.objAttrsMemIdxWidth))
      //- 1
      //(params.intnlFbSize2d.x * (1 << params.numBgsPow)) - 1
      //(
      //  (
      //    (
      //      params.intnlFbSize2d.x.toDouble * params.numBgs.toDouble
      //    ) / params.bgTileSize2d.x.toDouble
      //  ) - 1.toDouble
      //).toInt
      (
        if (params.numBgsPow <= params.bgTileSize2dPow.x) {
          (
            params.intnlFbSize2d.x
            >> (params.bgTileSize2dPow.x - params.numBgsPow)
          )
        } else { // if (params.numBgsPow > params.bgTileSize2dPow.x)
          (
            params.intnlFbSize2d.x
            << (params.numBgsPow - params.bgTileSize2dPow.x)
          )
        }
      ) - 1
    )
    //--------
    // BEGIN: for muxing writes into `objSubLineMemArr`; later 
    case class ObjSubLineMemWriter() extends Bundle {
      val idxVec = Vec.fill(params.numLineMemsPerBgObjRenderer)(
        UInt(log2Up(params.objSubLineMemArrSize) bits)
      )
      val dataVec = Vec.fill(params.numLineMemsPerBgObjRenderer)(
        Vec.fill(params.objTileSize2d.x)(ObjSubLineMemEntry())
      )
    }
    val objWriter = ObjSubLineMemWriter()
    //objWriter.idxVec(
    //for (idx <- objSubLineMemArr.indices) {
    //  val select = objWriter.dataVec.map(
    //    objWriter.idxVec(idx)
    //    === idx
    //  )
    //}
    for (jdx <- 0 until objSubLineMemArr.size) {
      //val select = objWriter.dataVec.map(
      //  objWriter.idxVec(jdx) === jdx
      //)
      val select = objWriter.idxVec.map(
        _ === jdx
      )
      val selected = MuxOH(select, objWriter.dataVec)
      println({
        def tempSelectSize = select.size
        def tempSelectedSize = selected.size
        f"sel sizes: select:$tempSelectSize selected:$tempSelectedSize"
      })
      //objSubLineMemArr(jdx).write(
      //  address=selected(jdx).addr,
      //  data=selected,
      //  enable=select.orR,
      //)
    }

    //for(i <- memArr.indices) {
    //  val select = arrayOfInputs.map(input.fpgaXXXIndex === i))
    //  val selected = MuxOH(select, arrayOfInputs)
    //  memArr.write(selected.address, selected.data, select.orR)
    //}
    // END: for muxing writes into `objSubLineMemArr`; later 
    //--------
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
        //val lineMemArrIdx = UInt(
        //  //log2Up(params.numLineMemsPerBgObjRenderer) bits
        //  lineMemArrIdxWidth bits
        //)
        val lineNum = UInt(wrLineNumWidth bits)
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
        //val bgAttrs = Vec.fill(params.bgTileSize2d.x)(
        //  cloneOf(bgAttrsArr(0))
        //)
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
          //cnt + 1 === params.oneLineMemSize
          //bakCntMinus1 === 0
          bakCnt === 0
          //bakCnt.msb
          //bakCnt === 0
        )

        //def cntDone() = (
        //  //cnt === params.intnlFbSize2d.x * params.numBgs
        //  cnt.msb
        //)

        // pixel x-position
        //def getCntPxPosX(
        //  x: Int,
        //) = cnt(
        //  //(log2Up(params.bgSize2dInPxs.x) + params.numBgsPow - 1)
        //  cnt.high
        //  downto params.numBgsPow
        //)
        def getCntPxPosX(
          x: Int,
        ) = {
          val tempGetCntPxPosX = UInt(
            //(cnt.getWidth - params.numBgsPow + params.bgTileSize2dPow.x)
            //(cnt.getWidth + params.bgTileSize2dPow.x)
            log2Up(params.intnlFbSize2d.x) bits
          )
          //println(tempGetCntPxPosX.high)

          //def tempCntGetPxPosXRange
          def tempTopRange = (
            //tempGetCntPxPosX.high downto params.bgTileSize2dPow.x
            tempGetCntPxPosX.high downto params.bgTileSize2dPow.x
          )
          def tempCntRange: Range = (
            //(log2Up(params.bgSize2dInPxs.x) + params.numBgsPow - 1)
            //cnt.high
            log2Up(params.intnlFbSize2d.x) + params.numBgsPow - 1
            downto params.numBgsPow + params.bgTileSize2dPow.x
          )
          def tempBotRange = (
            params.bgTileSize2dPow.x - 1 downto 0
          )
          //println(
          //  f"BG getCntPxPos(): "
          //  + f"top:$tempTopRange bot:$tempBotRange cnt:$tempCntRange "
          //  + f"x:$x"
          //)

          tempGetCntPxPosX(tempTopRange) := cnt(tempCntRange)
          tempGetCntPxPosX(tempBotRange) := x

          tempGetCntPxPosX
        }

        //def getCntBgIdx() = (
        //  params.numBgs - 1 - cnt(params.numBgsPow - 1 downto 0)
        //)
      }
      def lineNum = stage0.lineNum
      //def lineMemArrIdx = stage0.lineMemArrIdx
      def cnt = stage0.cnt
      //def cntMinus1 = stage0.cntMinus1
      def bakCnt = stage0.bakCnt
      def bakCntMinus1 = stage0.bakCntMinus1
      def bgIdx = stage0.bgIdx
      def bakCntWillBeDone() = stage0.bakCntWillBeDone()

      //def cntDone() = stage0.cntDone()
      //def getCntPxPosX() = stage0.getCntPxPosX()
      def bgAttrs = stage0.bgAttrs
      //def scroll = bgAttrs.scroll
      //def visib = stage0.bgAttrs.visib
      //def tilePxsCoord = stage0.tilePxsCoord

      // Stages after stage 0
      // NOTE: these pipeline stages are still separate
      case class Stage1() extends Bundle {
        val bgEntryMemIdx = Vec.fill(params.bgTileSize2d.x)(
          UInt(params.bgEntryMemIdxWidth bits)
        )
        val pxPos = Vec.fill(params.bgTileSize2d.x)(
          params.bgPxsCoordT()
        )
      }
      case class Stage2() extends Bundle {
        // `Gpu2dBgEntry`s that have been read
        val bgEntry = Vec.fill(params.bgTileSize2d.x)(
          Gpu2dBgEntry(params=params)
        )
      }
      case class Stage3() extends Bundle {
        val tilePxsCoord = Vec.fill(params.bgTileSize2d.x)(
          params.bgTilePxsCoordT()
        )
        // `Gpu2dTile`s that have been read
        val tile = Vec.fill(params.bgTileSize2d.x)(
          Gpu2dTile(params=params, isObj=false)
        )
      }
      case class Stage4() extends Bundle {
        val palEntryMemIdx = Vec.fill(params.bgTileSize2d.x)(
          UInt(params.objPalEntryMemIdxWidth bits)
        )
      }
      case class Stage5() extends Bundle {
        // Whether `palEntryMemIdx(someBgIdx)` is non-zero
        val palEntryNzMemIdx = Vec.fill(params.bgTileSize2d.x)(
          Bool()
        )
      }
      // The following BG pipeline stages are only performed when
      // `palEntryNzMemIdx(someBgIdx)` is `True`
      // `Gpu2dPalEntry`s that have been read
      case class Stage6() extends Bundle {
        val palEntry = Vec.fill(params.bgTileSize2d.x)(
          Gpu2dPalEntry(params=params)
        )
      }
      case class Stage7() extends Bundle {
        //val doWrite = Bool()
        val lineMemEntry = Vec.fill(params.bgTileSize2d.x)(
          BgSubLineMemEntry()
        )
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
      def pxPos = stage1.pxPos
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
      //prevLineMemArrIdx: UInt
      //prevLineNum: UInt,
      firstInit: Boolean
    ): WrBgPipePayload = {
      val ret = WrBgPipePayload()
      ////ret.lineMemArrIdx := rWrLineMemArrIdx
      //ret.lineMemArrIdx := prevLineMemArrIdx + 1
      if (firstInit) {
        //ret.lineNum := wrLineNumInit
        ret.lineNum := wrBgLineNumInit
      } else {
        ret.lineNum := rGlobWrBgLineNumPipe1(ret.lineNum.bitsRange)
      }
      //ret.lineNum := prevLineNum + 1
      //ret.lineNum := prevLineNum + U(f"$wrLineNumWidth'd1")

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

    //def wrBgPipeNumMainStages = 7
    def wrBgPipeNumMainStages = 8
    //def wrBgPipeNumMainStages = 9

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
    def wrObjPipeCntWidth = (
      //params.objTileSize2dPow.x + params.objAttrsMemIdxWidth + 1
      //params.objTileSize2dPow.x + params.objAttrsMemIdxWidth
      //params.objAttrsMemIdxWidth + 1
      params.objAttrsMemIdxWidth + 1 + 1
    )
    def wrObjPipeBakCntStart = (
      //(1 << (params.objTileSize2dPow.x + params.objAttrsMemIdxWidth))
      //- 1
      //(1 << params.objAttrsMemIdxWidth)
      //- 1
      (1 << (params.objAttrsMemIdxWidth + 1))
      - 1
    )
    //println(f"testificate: $wrObjPipeCntWidth $wrObjPipeBakCntStart")

    //def wrObjPipeStage5NumFwd = 5
    ////def wrObjPipeStage5NumFwd = 4
    ////def wrObjPipeStage5NumFwd = 3
    ////def wrObjPipeStage5NumFwd = 3
    ////def wrObjPipeStage5NumFwd = 2
    //case class WrObjPipeStage5Fwd() extends Bundle {
    //  //val pxPosX = SInt(params.objPxsCoordSize2dPow.x bits)
    //  //val pxPosX = SInt(params.objPxsCoordSize2dPow.x bits)
    //  val pxPos = params.objPxsCoordT()
    //  //val overwriteLineMemEntry = Bool()
    //}
    //def wrObjPipeStage6NumFwd = 3
    ////def wrObjPipeStage6NumFwd = 2
    //case class WrObjPipeStage6Fwd() extends Bundle {
    //  //val pxPosX = SInt(params.objPxsCoordSize2dPow.x bits)
    //  val pxPosX = SInt(params.objPxsCoordSize2dPow.x bits)
    //  //val overwriteLineMemEntry = Bool()
    //}

    def wrObjPipeNumMainStages = (
      //7
      8
    )
    def wrBgObjPipeNumStages = max(
      wrBgPipeNumMainStages,
      wrObjPipeNumMainStages
    ) //+ 1

    //def wrObjPipeStage6NumFwd = 3
    //def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6
    //def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6 + 1
    //def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6 + 1 + 1
    def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6 + 1 + 1
    //def wrObjPipeStage6NumFwd = 2
    //def wrObjPipeStage6NumFwd = 1
    case class WrObjPipeStage6Fwd() extends Bundle {
      //val pxPosX = SInt(params.objPxsCoordSize2dPow.x bits)
      //val pxPosInLine = Bool()
      //val palEntry = Gpu2dPalEntry(params=params)
      //val stage6 = Stage6()
      //val pxPosXIsSame = Bool()
      //val pxPosXIsSame = Bool()
      //val prio = UInt(params.numBgsPow bits)
      val objAttrsMemIdx = UInt(
        //(wrObjPipeCntWidth - params.objAttrsMemIdxWidth.x) bits
        params.objAttrsMemIdxWidth bits
      )
      val pxPosY = SInt(params.objPxsCoordSize2dPow.y bits)
      val pxPosXGridIdx = UInt(
        (params.objPxsCoordSize2dPow.x - params.objTileSize2dPow.x) bits
      )

      //val pxPos = params.objPxsCoordT()
      //val prio = UInt(params.numBgsPow bits)
      val overwriteLineMemEntry = Bool()
      val wrLineMemEntry = ObjSubLineMemEntry()
    }
    case class WrObjPipeStage0JustCopy() extends Bundle {
      val lineNum = UInt(wrLineNumWidth bits)
      val cnt = UInt(wrObjPipeCntWidth bits)
      val bakCnt = UInt(wrObjPipeCntWidth bits)
      val bakCntMinus1 = UInt(wrObjPipeCntWidth bits)
      //val objAttrsMemIdx = UInt(
      //  //params.objAttrsMemIdxWidth bits
      //  (params.objAttrsMemIdxWidth + 1) bits
      //)
      val dbgTestObjAttrsMemIdx = UInt(params.objAttrsMemIdxWidth bits)
    }
    case class WrObjPipeOut5Ext() extends Bundle {
      val rdLineMemEntry = Vec.fill(params.objTileSize2d.x)(
        ObjSubLineMemEntry()
      )
    }
    val rWrObjPipeOut5Ext = Reg(WrObjPipeOut5Ext())
    rWrObjPipeOut5Ext.init(rWrObjPipeOut5Ext.getZero)
    case class WrObjPipeOut6Ext() extends Bundle {
      val wrLineMemEntry = Vec.fill(params.objTileSize2d.x)(
        ObjSubLineMemEntry()
      )
      val overwriteLineMemEntry = Vec.fill(params.objTileSize2d.x)(Bool())
    }
    val rWrObjPipeOut6Ext = Reg(WrObjPipeOut6Ext())
    rWrObjPipeOut6Ext.init(rWrObjPipeOut6Ext.getZero)
    case class WrObjPipePayload() extends Bundle {
      // Which sprite are we processing?
      case class Stage0() extends Bundle {
        //val tilePxsCoordXCnt = UInt(params.objTileSize2dPow.x bits)
        //val tilePxsCoordXCntPlus1 = UInt(
        //  (params.objTileSize2dPow.x + 1) bits
        //)
        //val lineMemArrIdx = cloneOf(rWrLineMemArrIdx)
        //val lineMemArrIdx = UInt(
        //  //log2Up(params.numLineMemsPerBgObjRenderer) bits
        //  lineMemArrIdxWidth bits
        //)
        //val justCopy = new Bundle {
        //}
        val justCopy = WrObjPipeStage0JustCopy()
        def lineNum = justCopy.lineNum
        def cnt = justCopy.cnt
        def bakCnt = justCopy.bakCnt
        def bakCntMinus1 = justCopy.bakCntMinus1
        //def objAttrsMemIdx = justCopy.objAttrsMemIdx

        //val objAttrsMemIdx

        //val objAttrsMemIdx = SInt(params.objAttrsMemIdxWidth + 1 bits)
        //val objAttrsMemIdx = cloneOf(rWrObjPipeFrontObjAttrsMemIdx)
        //val objAttrsMemIdx = UInt(params.objAttrsMemIdxWidth bits)

        //val innerObjAttrsMemIdx = UInt(params.objAttrsMemIdxWidth bits)

        ////innerObjAttrsMemIdx := bakCnt(
        ////  //bakCnt.high - 1
        ////  //downto (bakCnt.high - 1 - params.objAttrsMemIdxWidth + 1)
        ////  //bakCnt.high
        ////  //downto (bakCnt.high - (params.objAttrsMemIdxWidth - 1))
        ////  //(params.objAttrsMemIdxWidth + params.objTileSize2dPow.x - 1)
        ////  //downto params.objTileSize2dPow.x

        ////  params.objAttrsMemIdxWidth + params.objTileSize2dPow.x - 1
        ////  downto params.objTileSize2dPow.x
        ////)
        def objAttrsMemIdx() = (
          justCopy.dbgTestObjAttrsMemIdx(
            params.objAttrsMemIdxWidth - 1 downto 0
          )
        )

        //def objAttrsMemIdx() = innerObjAttrsMemIdx
        def invObjAttrsMemIdx() = (
          //cnt
          //(
          //  (1 << params.objAttrsMemIdxWidth) - 1
          //) - 
          cnt(
            // support needing to do two writes into `objSubLineMemArr`
            params.objAttrsMemIdxWidth - 1 downto 0
            //params.objAttrsMemIdxWidth downto 1
          )
        )
        // which iteration are we on for
        //val gridIdxLsb = Bool()

        //def getBakCntTilePxsCoordX() = bakCnt(
        //  (bakCnt.high - 1 - params.objAttrsMemIdxWidth)
        //  downto 0
        //)
        //def getCntObjIdx() = cnt(
        //  cnt.high downto params.objTileSize2dPow.x
        //)

        //def getCntTilePxsCoordX() = cnt(
        //  //(cnt.high - 1 - params.objAttrsMemIdxWidth)
        //  params.objTileSize2dPow.x - 1
        //  downto 0
        //)
        //def getBakCntTilePxsCoordX() = bakCnt

        def bakCntWillBeDone() = (
          //bakCntMinus1.msb
          //cnt + 1 === params.oneLineMemSize
          //bakCntMinus1 === 0
          bakCnt === 0
          //bakCnt.msb
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
        val tilePxsCoord = Vec.fill(params.objTileSize2d.x)(
          params.objTilePxsCoordT()
        )
        //val pxsCoordYRangeCheckPipe1 = SInt(
        //  log2Up(params.objPxsCoordSize2dPow.y) bits
        //)
        //val pxPosRangeCheckPipe1 = params.objPxsCoordT()
        //val pxPos = params.objPxsCoordT()
        val pxPos = Vec.fill(params.objTileSize2d.x)(
          params.objPxsCoordT()
        )
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
        //val pxPosXGridStraddleVec = Vec.fill(
        //  params.objTileSize2d
        //)
        val pxPosXGridIdx = (
          Vec.fill(params.objTileSize2d.x)(
            UInt(
              (params.objPxsCoordSize2dPow.x - params.objTileSize2dPow.x)
              bits
            )
          )
        )
        val pxPosXGridIdxLsb = (
          Vec.fill(params.objTileSize2d.x)(
            //UInt(1 bits)
            Bool()
          )
        )
        val pxPosXGridIdxFindFirstSameAsFound = Bool()
        val pxPosXGridIdxFindFirstSameAsIdx = UInt(
          params.objTileSize2dPow.x bits
        )
        val pxPosXGridIdxFindFirstDiffFound = Bool()
        val pxPosXGridIdxFindFirstDiffIdx = UInt(
          params.objTileSize2dPow.x bits
        )
        val pxPosRangeCheckGePipe1 = Vec.fill(params.objTileSize2d.x)(
          Vec2(Bool())
        )
        val pxPosRangeCheckLtPipe1 = Vec.fill(params.objTileSize2d.x)(
          Vec2(Bool())
        )
        val palEntryMemIdx = Vec.fill(params.objTileSize2d.x)(
          UInt(params.objPalEntryMemIdxWidth bits)
        )
      }
      case class Stage4() extends Bundle {
        //val oldPxPosInLineCheckGePipe1 = Vec2(Bool())
        //val oldPxPosInLineCheckLtPipe1 = Vec2(Bool())
        //val pxPosXGridIdxFlip = Vec.fill(params.objTileSize2d.x)(Bool())
        val pxPosXGridIdxMatches = Vec.fill(params.objTileSize2d.x)(Bool())

        // Whether `palEntryMemIdx` is non-zero
        val palEntryNzMemIdx = Vec.fill(params.objTileSize2d.x)(Bool())
        val pxPosRangeCheck = Vec.fill(params.objTileSize2d.x)(
          Vec2(Bool())
        )

        //val oldPxPosInLineCheck = Vec2(Bool())
      }

      case class Stage5() extends Bundle {
        val nonExt = new Bundle {
          val palEntry = Vec.fill(params.objTileSize2d.x)(
            Gpu2dPalEntry(params=params)
          )
          val pxPosInLine = Vec.fill(params.objTileSize2d.x)(Bool())
          val pxPosCmpForOverwrite = Vec.fill(params.objTileSize2d.x)(
            Bool()
          )
        }
        //val pxPosXChangingGridIdx = (
        //  Vec.fill(params.objTileSize2d.x)(Bool())
        //)
        //val pxPosXGridIdxLsb = (
        //  Vec.fill(params.objTileSize2d.x)(UInt(1 bits))
        //)

        //def numFwd = wrObjPipeStage5NumFwd
        //val fwdVec = Vec.fill(numFwd)(WrObjPipeStage5Fwd())

        //val pxPosXConcat = Bits(
        //  //numFwd bits
        //  numFwd - 1 bits
        //)
        //val pxPosConcat = Vec2(Bits(
        //  (numFwd - 1) bits
        //))
        //val pxPosConcat = Vec.fill(numFwd - 1)(Vec2(Bool()))
        // "rd..." here means it's been read from `objSubLineMemArr`
        val ext = WrObjPipeOut5Ext()
        ////val rdLineMemEntry = Vec.fill(params.objTileSize2d.x)(
        ////  ObjSubLineMemEntry()
        ////)
        //def rdLineMemEntry = ext.rdLineMemEntry
        ////val dbgRdLineMemEntry = ObjSubLineMemEntry()
      }
      //case class Stage5Fwd() extends Bundle {
      //  val pastStage2 = Stage2()
      //  val pastRdLineMemEntry = ObjSubLineMemEntry()
      //}
      //case class Stage6() extends Bundle {
      //  //val rdLineMemEntry = ObjSubLineMemEntry()
      //  val overwriteLineMemEntry = Bool()
      //  //val pxPosXIsSame = Bool()
      //  //val pxPosIsSame = Bool()
      //  //val pxPosIsSame = Vec2(Bool())


      //  //def numFwd = wrObjPipeStage6NumFwd
      //  //val fwdVec = Vec.fill(numFwd)(WrObjPipeStage6Fwd())

      //  //val pxPosXSameAsPrevious = Bool()
      //  //val prevPxPosX = SInt(params.objPxsCoordSize2dPow.x bits)
      //}

      // This is similar to a strictly in-order CPU pipeline's
      // ALU operand forwarding
      //def wrObjPipeStage5NumFwd
      //def stage7NumFwd = 2
      case class Stage6() extends Bundle {
        //val overwriteLineMemEntry = Vec.fill(params.objTileSize2d.x)(
        //  Bool()
        //)
        ////def numFwd = stage6NumFwd
        //val wrLineMemEntry = Vec.fill(params.objTileSize2d.x)(
        //  ObjSubLineMemEntry()
        //)
        val ext = WrObjPipeOut6Ext()
        //def wrLineMemEntry = ext.wrLineMemEntry
        //def overwriteLineMemEntry = ext.overwriteLineMemEntry

        def numFwd = wrObjPipeStage6NumFwd
        val nonExt = new Bundle {
          val fwdVec = Vec.fill(params.objTileSize2d.x)(
            Vec.fill(numFwd)(WrObjPipeStage6Fwd())
          )
        }
        def fwdVec = nonExt.fwdVec
        //val savedWrLineMemEntryVec = Vec.fill(numFwd)(ObjSubLineMemEntry())

        //def numSaved = 3
        //val savedWrLineMemEntryVec = Vec.fill(numSaved)(ObjSubLineMemEntry())
      }
      //case class Stage7() extends Bundle {
      //  val ext = WrObjPipeOut6Ext()
      //}
      case class PostStage0() extends Bundle {
        val stage1 = Stage1()
        val stage2 = Stage2()
        val stage3 = Stage3()
        val stage4 = Stage4()
        val stage5 = Stage5()
        val stage6 = Stage6()
        //val stage7 = Stage7()
        //val stage7 = Stage7()
      }

      val stage0 = Stage0()
      def calcGridIdxLsb() = (
        //cnt
        stage0.cnt(
          // support needing to do two writes into `objSubLineMemArr`
          //0
          params.objAttrsMemIdxWidth + 1 - 1
          //downto params.objAttrsMemIdxWidth + 1 - 1
        )
      )

      //def lineMemArrIdx = stage0.lineMemArrIdx
      def lineNum = stage0.lineNum
      def cnt = stage0.cnt
      def bakCnt = stage0.bakCnt
      def bakCntMinus1 = stage0.bakCntMinus1
      def objAttrsMemIdx() = stage0.objAttrsMemIdx()
      //def getBakCntTilePxsCoordX() = stage0.getBakCntTilePxsCoordX()
      //def getCntTilePxsCoordX() = stage0.getCntTilePxsCoordX()
      def bakCntWillBeDone() = stage0.bakCntWillBeDone()
      //def gridIdxLsb = stage0.gridIdxLsb

      val postStage0 = PostStage0()

      def stage1 = postStage0.stage1
      def objAttrs = stage1.objAttrs

      def stage2 = postStage0.stage2
      //def tilePxsCoordYPipe1 = stage2.tilePxsCoordYPipe1
      //def pxsCoordYRangeCheckPipe1 = stage2.pxsCoordYRangeCheckPipe1
      def tilePxsCoord = stage2.tilePxsCoord
      //def pxPosRangeCheckPipe1 = stage2.pxPosRangeCheckPipe1
      def pxPos = stage2.pxPos

      //def getObjSubLineMemArrIdx_tempRange() = (
      //  //pxPos(0).x.high
      //  //log2Up(params.oneLineMemSize) - 1
      //  //downto params.objTileSize2dPow.x + 1 //gridIdx.getWidth
      //  log2Up(params.oneLineMemSize) - 1
      //  //downto params.objTileSize2dPow.x + 1
      //  downto params.objTileSize2dPow.x
      //)
      //def getObjSubLineMemArrIdx_tempRange() = (
      //  //pxPosXGridIdx(0).high - 2 downto 1
      //  //pxPosXGridIdx(0).high - 2 downto 1
      //  pxPosXGridIdx(0).high - 3 downto 0
      //)
      def getObjSubLineMemArrIdx(
        x: UInt
      ): UInt = {
        params.getObjSubLineMemArrIdx(
          addr=pxPos(x).x.asUInt
        )
        //pxPosXGridIdx(pxPosXGridIdxFindFirstSameAs)
        //(
        //  //pxPosXGridIdx(0).high - 2 downto 1
        //  //pxPosXGridIdx(0).high - 2 downto 1
        //  pxPosXGridIdx(0).high - 3 downto 0
        //)
        ////Cat(
        ////  pxPos(0).x.asUInt(pxPos(0).x.high downto gridIdx.getWidth),
        ////  gridIdx,
        ////).asUInt
        ////println(
        ////  f"getObjSubLineMemArrIdx(): $tempRange;  "
        ////  + {
        ////    def tempHigh = log2Up(params.oneLineMemSize) - 1
        ////    def tempTileWidth = params.objTileSize2dPow.x
        ////    def tempGridIdxWidth = gridIdx.getWidth
        ////    f"$tempHigh $tempTileWidth $tempGridIdxWidth"
        ////  }
        ////)
        //def tempRange = getObjSubLineMemArrIdx_tempRange()
        ////pxPos(0).x.asUInt(tempRange)
        //pxPos(x).x.asUInt(tempRange)
        // BEGIN: not quite working?
        //Cat(
        //  pxPos(0).x.asUInt(tempRange),
        //  gridIdx,
        //  //{
        //  //  def myWidthForZero = params.objTileSize2dPow.x + 1
        //  //  U(f"$myWidthForZero'd0")
        //  //},
        //).asUInt
        // END: not quite working?
        ////Cat(
        ////)
        //def tempRange = (
        //  //log2Up(params.oneLineMemSize) - 1
        //  //pxPosXGridIdx(0).high
        //  //log2Up(params.oneLineMemSize) - 1
        //  //pxPosXGridIdx(0).high

        //  //log2Up(params.oneLineMemSize) - 1 //- params.objTileSize2dPow.x - 1 
        //  //downto gridIdx.getWidth + params.objTileSize2dPow.x
        //  //log2Up(params.oneLineMemSize) - 1
        //  //downto 0
        //  pxPosXGridIdx(0).high - 3
        //  downto gridIdx.getWidth
        //)
        //println(f"tempRange: $tempRange")
        ////Cat(
        ////  pxPosXGridIdx(0)
        ////  (
        ////    //pxPosXGridIdx(0).high
        ////    //downto gridIdx.getWidth //+ params.objTileSize2dPow.x
        ////    tempRange
        ////  )
        ////  ,
        ////  gridIdx
        ////).asUInt
        //pxPosXGridIdx(0)(
        //  3 downto 3
        //)
      }
      def getObjSubLineMemArrGridIdx(
        x: UInt
      ): Bool = {
        params.getObjSubLineMemArrGridIdx(
          addr=pxPos(x).x.asUInt
        )
      }
      def getObjSubLineMemArrElemIdx(
        x: UInt
      ): UInt = {
        params.getObjSubLineMemArrElemIdx(
          addr=pxPos(x).x.asUInt
        )
      }
      //def pxPosYMinusTileHeightMinus1 = stage2.pxPosYMinusTileHeightMinus1
      //def pxPosShiftTopLeft = stage2.pxPosShiftTopLeft
      def objPosYShift = stage2.objPosYShift
      def tile = stage2.tile

      def stage3 = postStage0.stage3
      def pxPosXGridIdx = stage3.pxPosXGridIdx
      def pxPosXGridIdxLsb = stage3.pxPosXGridIdxLsb
      def pxPosXGridIdxFindFirstSameAsFound = (
        stage3.pxPosXGridIdxFindFirstSameAsFound
      )
      def pxPosXGridIdxFindFirstSameAsIdx = (
        stage3.pxPosXGridIdxFindFirstSameAsIdx
      )
      def pxPosXGridIdxFindFirstDiffIdx = (
        stage3.pxPosXGridIdxFindFirstDiffIdx
      )
      def pxPosXGridIdxFindFirstDiffFound = (
        stage3.pxPosXGridIdxFindFirstDiffFound
      )
      //def pxPosRangeCheck = stage3.pxPosRangeCheck
      //def tilePxsCoord = stage3.tilePxsCoord
      def pxPosRangeCheckGePipe1 = stage3.pxPosRangeCheckGePipe1
      def pxPosRangeCheckLtPipe1 = stage3.pxPosRangeCheckLtPipe1
      def palEntryMemIdx = stage3.palEntryMemIdx

      def stage4 = postStage0.stage4
      //def pxPosXGridIdxFlip = stage4.pxPosXGridIdxFlip
      def pxPosXGridIdxMatches = stage4.pxPosXGridIdxMatches
      //def oldPxPosInLineCheckGePipe1 = stage4.oldPxPosInLineCheckGePipe1
      //def oldPxPosInLineCheckLtPipe1 = stage4.oldPxPosInLineCheckLtPipe1
      //def palEntryMemIdx = stage4.palEntryMemIdx
      def palEntryNzMemIdx = stage4.palEntryNzMemIdx
      def pxPosRangeCheck = stage4.pxPosRangeCheck

      def stage5 = postStage0.stage5
      //def palEntryNzMemIdx = stage5.palEntryNzMemIdx
      //def oldPxPosInLineCheck = stage5.oldPxPosInLineCheck
      def palEntry = stage5.nonExt.palEntry
      //def pxPosXConcat = stage5.pxPosXConcat
      //def pxPosConcat = stage5.pxPosConcat
      def pxPosInLine = stage5.nonExt.pxPosInLine
      def pxPosCmpForOverwrite = stage5.nonExt.pxPosCmpForOverwrite
      //def pxPosXChangingGridIdx = stage5.pxPosXChangingGridIdx
      def rdLineMemEntry = stage5.ext.rdLineMemEntry
      //def dbgRdLineMemEntry = stage5.dbgRdLineMemEntry

      def stage6 = postStage0.stage6
      def wrLineMemEntry = stage6.ext.wrLineMemEntry
      def overwriteLineMemEntry = stage6.ext.overwriteLineMemEntry

      //def overwriteLineMemEntry = stage6.overwriteLineMemEntry
      ////def pxPosXIsSame = stage6.pxPosXIsSame
      ////def pxPosIsSame = stage6.pxPosIsSame
      ////def rdLineMemEntry = stage6.rdLineMemEntry

      ////def stage7 = postStage0.stage7
      ////def fwdPxPosXVec = stage7.fwdPxPosXVec
      ////def fwdPxPosInLineVec = stage7.fwdPxPosInLineVec
      ////def fwdOverwriteLineMemEntryVec = stage7.fwdOverwriteLineMemEntryVec
      ////def fwdLineMemEntryVec = stage7.fwdLineMemEntryVec
      ////def numFwd = stage7.numFwd
      ////def fwdVec = stage7.fwdVec
      //def wrLineMemEntry = stage6.wrLineMemEntry
      ////def palEntry = stage6.palEntry
      ////def pxPosInLine = stage6.pxPosInLine
      ////def rdLineMemEntry = stage6.rdLineMemEntry

      ////def stage7 = postStage0.stage7
      ////def wrLineMemEntry = stage7.wrLineMemEntry

      ////def rdLineMemEntry = postStage0.rdLineMemEntry
      ////def wrLineMemEntry = postStage0.wrLineMemEntry

      //def stage7 = postStage0.stage7
      //def overwriteLineMemEntry = stage7.ext.overwriteLineMemEntry
      //def wrLineMemEntry = stage7.ext.wrLineMemEntry
    }
    def doInitWrObjPipePayload(
      //prevLineMemArrIdx: UInt
      //prevLineNum: UInt
      firstInit: Boolean
    ): WrObjPipePayload = {
      val ret = WrObjPipePayload()
      ////ret.lineMemArrIdx := rWrLineMemArrIdx
      //ret.lineMemArrIdx := prevLineMemArrIdx + 1
      //ret.lineNum := prevLineNum + 1

      //ret.lineNum := rGlobWrObjLineNumPipe1(ret.lineNum.bitsRange)
      if (firstInit) {
        //ret.lineNum := wrLineNumInit
        ret.lineNum := wrObjLineNumInit
      } else {
        ret.lineNum := rGlobWrObjLineNumPipe1(ret.lineNum.bitsRange)
      }
      //ret.lineNum := prevLineNum + 1
      //ret.lineNum := prevLineNum + U(f"$wrLineNumWidth'd1")
      ret.cnt := 0
      ret.bakCnt := wrObjPipeBakCntStart
      ret.bakCntMinus1 := wrObjPipeBakCntStart - 1
      ret.stage0.justCopy.dbgTestObjAttrsMemIdx := (
        (1 << params.objAttrsMemIdxWidth) - 1
      )
      //ret.objAttrsMemIdx := wrObjPipeBakCntStart
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

    ////def wrObjPipeNumMainStages = 6
    //def wrObjPipeNumMainStages = 7
    ////def wrObjPipeNumMainStages = 8
    ////def wrObjPipeNumMainStages = 9

    //def wrBgObjPipeNumStages = max(
    //  wrBgPipeNumMainStages,
    //  wrObjPipeNumMainStages
    //) + 1

    //def wrObjPipeSize = wrObjPipeNumElems * params.numObjsPow
    //def wrObjPipeSize = max(wrObjPipeNumElems, params.numObjsPow) + 1 
    //def wrObjPipeSize = wrObjPipeNumMainStages + params.numObjsPow
    //def wrObjPipeNumElemsGtNumObjsPow = (
    //  wrObjPipeNumElems > params.numObjsPow
    //)

    //def sendIntoFifoCntWidth = log2Up(params.oneLineMemSize) + 2
    //def sendIntoFifoBakCntStart = params.oneLineMemSize - 1

    //case class SendIntoFifoPipePayload() extends Bundle {

    //  case class Stage0() extends Bundle {
    //    val lineMemArrIdx = UInt(
    //      //log2Up(params.numLineMemsPerBgObjRenderer) bits
    //      lineMemArrIdxWidth bits
    //    )
    //    val cnt = UInt(sendIntoFifoCntWidth bits)
    //    val bakCnt = UInt(sendIntoFifoCntWidth bits)
    //    val bgLineMemEntry = BgSubLineMemEntry()
    //    val objLineMemEntry = ObjSubLineMemEntry()

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
    //  def bgLineMemEntry = stage0.bgLineMemEntry
    //  def objLineMemEntry = stage0.objLineMemEntry

    //  def bakCntWillBeDone() = stage0.bakCntWillBeDone()
    //}

    ////case class SendIntoObjFifoPipePayload() extends Bundle {

    ////  case class Stage0() extends Bundle {
    ////    val lineMemArrIdx = UInt(
    ////      //log2Up(params.numLineMemsPerBgObjRenderer) bits
    ////      lineMemArrIdxWidth bits
    ////    )
    ////    val cnt = UInt(sendIntoFifoCntWidth bits)
    ////    val bakCnt = UInt(sendIntoFifoCntWidth bits)
    ////    //val bgLineMemEntry = BgSubLineMemEntry()
    ////    val objLineMemEntry = ObjSubLineMemEntry()

    ////    def bakCntWillBeDone() = (
    ////      bakCnt === 0
    ////    )
    ////  }
    ////  //case class Stage1() extends Bundle {
    ////  //}

    ////  //case class Stage1() extends Bundle {
    ////  //  val 
    ////  //}

    ////  //case class PostStage0() extends Bundle {
    ////  //  val stage1 = Stage1()
    ////  //}

    ////  val stage0 = Stage0()
    ////  def lineMemArrIdx = stage0.lineMemArrIdx
    ////  def cnt = stage0.cnt
    ////  def bakCnt = stage0.bakCnt
    ////  //val bgLineMemEntry = stage0.bgLineMemEntry
    ////  def objLineMemEntry = stage0.objLineMemEntry

    ////  def bakCntWillBeDone() = stage0.bakCntWillBeDone()
    ////}

    //def doInitSendIntoFifoPipePayload(
    //  prevLineMemArrIdx: UInt,
    //): SendIntoFifoPipePayload = {
    //  //val ret = SendIntoFifoPipePayload()
    //  val ret = SendIntoFifoPipePayload()
    //  ret.lineMemArrIdx := prevLineMemArrIdx + 1
    //  ret.cnt := 0
    //  ret.bakCnt := sendIntoFifoBakCntStart
    //  ret.bgLineMemEntry := ret.bgLineMemEntry.getZero
    //  //ret.objLineMemEntry := ret.objLineMemEntry.getZero
    //  ret
    //}
    ////def doInitSendIntoObjFifoPipePayload(
    ////  prevLineMemArrIdx: UInt,
    ////): SendIntoObjFifoPipePayload = {
    ////  //val ret = SendIntoObjFifoPipePayload()
    ////  val ret = SendIntoObjFifoPipePayload()
    ////  ret.lineMemArrIdx := prevLineMemArrIdx + 1
    ////  ret.cnt := 0
    ////  ret.bakCnt := sendIntoFifoBakCntStart
    ////  //ret.bgLineMemEntry := ret.bgLineMemEntry.getZero
    ////  ret.objLineMemEntry := ret.objLineMemEntry.getZero
    ////  ret
    ////}

    ////def sendIntoFifoPipeNumMainStages = 2
    //def sendIntoFifoPipeNumMainStages = 1


    def combinePipeCntWidth = (
      log2Up(params.intnlFbSize2d.x + 1) + 2
    )
    def combinePipeBakCntStart = (
      params.intnlFbSize2d.x - 1
    )
    case class CombinePipeOut1Ext() extends Bundle {
      val bgRdLineMemEntry = BgSubLineMemEntry()
      val objRdLineMemEntry = ObjSubLineMemEntry()
    }
    val rCombinePipeOut1Ext = Reg(CombinePipeOut1Ext())
    rCombinePipeOut1Ext.init(rCombinePipeOut1Ext.getZero)
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
        val changingRow = Bool()
        //val lineMemArrIdx = UInt(lineMemArrIdxWidth bits)
        val cnt = UInt(combinePipeCntWidth bits)
        val bakCnt = UInt(combinePipeCntWidth bits)
        val bakCntMinus1 = UInt(combinePipeCntWidth bits)
        //--------
        def bakCntWillBeDone() = (
          //bakCntMinus1.msb
          //bakCntMinus1 === 0
          bakCnt === 0
          //bakCnt.msb
          //cnt + 1 === params.oneLineMemSize
        )
        //--------
      }
      val stage0 = Stage0()
      def changingRow = stage0.changingRow
      //def rdLineMemArrIdx = stage0.rdLineMemArrIdx
      //def wrLineMemArrIdx = stage0.wrLineMemArrIdx
      //def lineMemArrIdx = stage0.lineMemArrIdx
      def cnt = stage0.cnt
      def bakCnt = stage0.bakCnt
      def bakCntMinus1 = stage0.bakCntMinus1
      def bakCntWillBeDone() = stage0.bakCntWillBeDone()

      case class Stage1() extends Bundle {
        val ext = CombinePipeOut1Ext()
      }

      case class Stage2() extends Bundle {
        val objHiPrio = Bool()
      }
      case class Stage3() extends Bundle {
        //val isObj = Bool()
        val col = Rgb(params.rgbConfig)
      }
      case class Stage4() extends Bundle {
        val combineWrLineMemEntry = BgSubLineMemEntry()
        //val objWrLineMemEntry = ObjSubLineMemEntry()
      }
      case class PostStage0() extends Bundle {
        val stage1 = Stage1()
        val stage2 = Stage2()
        val stage3 = Stage3()
        val stage4 = Stage4()
      }
      val postStage0 = PostStage0()

      def stage1 = postStage0.stage1
      def bgRdLineMemEntry = stage1.ext.bgRdLineMemEntry
      def objRdLineMemEntry = stage1.ext.objRdLineMemEntry

      def stage2 = postStage0.stage2
      def objHiPrio = stage2.objHiPrio

      def stage3 = postStage0.stage3
      def col = stage3.col

      def stage4 = postStage0.stage4
      def combineWrLineMemEntry = stage4.combineWrLineMemEntry
      //--------
    }
    def doInitCombinePipePayload(
      changingRow: Bool,
      //prevRdLineMemArrIdx: UInt,
      //prevWrLineMemArrIdx: UInt,
      //prevLineMemArrIdx: UInt,
    ): CombinePipePayload = {
      val ret = CombinePipePayload()
      ret.changingRow := changingRow
      ////ret.rdLineMemArrIdx := rCombineRdLineMemArrIdx
      ////ret.wrLineMemArrIdx := rCombineWrLineMemArrIdx
      //ret.rdLineMemArrIdx := prevRdLineMemArrIdx + 1
      //ret.wrLineMemArrIdx := prevWrLineMemArrIdx + 1
      //ret.lineMemArrIdx := prevLineMemArrIdx + 1
      ret.cnt := 0
      ret.bakCnt := combinePipeBakCntStart
      ret.bakCntMinus1 := combinePipeBakCntStart - 1
      ret.postStage0 := ret.postStage0.getZero
      ret
    }
    def combinePipeNumMainStages = 5
    //def combinePipeNumMainStages = wrBgObjPipeNumStages + 5

    //def anyPipeNumStages = (
    //  wrBgPipeNumMainStages
    //  .max(wrObjPipeNumMainStages)
    //  .max(combinePipeNumMainStages)
    //) + 1


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
    //    val tempCntSlice = UInt(log2Up(params.oneLineMemSize) bits)
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
    //      //val tempCntSlice = UInt(log2Up(params.oneLineMemSize) bits)
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
    //    //val bgRdLineMemEntry = BgSubLineMemEntry()
    //    //val objRdLineMemEntry = ObjSubLineMemEntry()
    //    val bgLineMemEntry = BgSubLineMemEntry()
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
    val combinePipeOverflow = Bool()
    //val rPastCombinePipeOverflow = RegNext(combinePipeOverflow)
    //  .init(False)
    //val combinePipeOverflowState = Reg(Bool()) init(False)

    //def wrPipeSize = wrBgPipeSize + wrObjPipeSize
    val wrBgPipeIn = KeepAttribute(
      //Vec.fill(wrBgObjPipeNumStages)(Reg(WrBgPipePayload()))
      Vec.fill(
        wrBgObjPipeNumStages
        //anyPipeNumStages
      )(
        //Flow(WrBgPipePayload())
        Flow(WrBgPipePayload())
      )
    )
    val wrBgPipeOut = KeepAttribute(
      //Vec.fill(wrBgObjPipeNumStages)(Reg(WrBgPipePayload()))
      Vec.fill(
        wrBgObjPipeNumStages
        //anyPipeNumStages
      )(
        //Flow(WrBgPipePayload())
        Flow(WrBgPipePayload())
      )
    )
    //wrBgPipeOut.last.ready := (
    //  // This is a heuristic!
    //  //lineFifo.io.misc.amountCanPush > 8
    //  lineFifo.io.misc.amountCanPush
    //  > params.wrBgObjStallFifoAmountCanPush
    //)

    val wrBgPipeLast = KeepAttribute(
      //Flow(WrBgPipePayload())
      Flow(WrBgPipePayload())
    )

    //when (nextIntnlChangingRow)
    //when (rIntnlChangingRow)

    when (intnlChangingRowRe)
    {
      nextWrBgChangingRow := False
    } elsewhen (
      wrBgPipeLast.bakCntWillBeDone() && wrBgPipeLast.fire
      //(wrBgPipeLast.cnt + 1 === params.oneLineMemSize) && wrBgPipeLast.fire
      //wrBgPipeLast.bakCnt.msb && wrBgPipeLast.fire
    ) {
      nextWrBgChangingRow := True
    } otherwise {
      nextWrBgChangingRow := rWrBgChangingRow
    }

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
    //val rWrBgPipeFrontValid = Reg(Bool()) init(True)
    val nextWrBgPipeFrontValid = Bool()
    val rWrBgPipeFrontValid = RegNext(nextWrBgPipeFrontValid)
      .init(True)
    val rSavedWrBgPipeFrontValid = Reg(Bool()) init(False)
    val rWrBgPipeFrontPayload = Reg(WrBgPipePayload())
    //rWrBgPipeFrontPayload.init(rWrBgPipeFrontPayload.getZero)
    rWrBgPipeFrontPayload.init(doInitWrBgPipePayload(
      //prevLineMemArrIdx=wrLineMemArrIdxInit
      //prevLineNum=wrLineNumInit - 1
      //prevLineNum=wrLineNumInit
      firstInit=true
    ))

    wrBgPipeIn(0).valid := rWrBgPipeFrontValid
    wrBgPipeIn(0).payload := rWrBgPipeFrontPayload
    for (idx <- 1 to wrBgPipeIn.size - 1) {
      // Create pipeline registering
      wrBgPipeIn(idx) <-< wrBgPipeOut(idx - 1)
      //wrBgPipeIn(idx) <-/< wrBgPipeOut(idx - 1)
    }
    for (idx <- 0 to wrBgPipeOut.size - 1) {
      // Connect output `valid` to input `valid`
      wrBgPipeOut(idx).valid := wrBgPipeIn(idx).valid
      //wrBgPipeIn(idx).ready := wrBgPipeOut(idx).ready
    }

    //val wrBgPop = PipeSkidBuf(
    //  //dataType=WrBgPipePayload(),
    //  dataType=BgSubLineMemEntry(),
    //  //optIncludeBusy=true,
    //  //optUseOldCode=true,
    //)

    //wrBgPop.io.misc.clear := clockDomain.isResetActive
    //val tempBgLineFifoPush = Stream(BgSubLineMemEntry())
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
    wrBgPipeLast <-< wrBgPipeOut.last
    //wrBgPipeLast <-/< wrBgPipeOut.last

    // Send data into the background pipeline
    //when (nextIntnlChangingRow) {
    //  rWrBgChangingRow := False
    //}
    //when (combinePipeOverflow) {
    //} 
    //val rSavedWrBgPipeFrontPayload = Reg(WrBgPipePayload())
    //rSavedWrBgPipeFrontPayload.init(rSavedWrBgPipeFrontPayload.getZero)
    //when (combinePipeOverflow) {
    //  nextWrBgPipeFrontValid := False
    //} otherwise {
      rSavedWrBgPipeFrontValid := nextWrBgPipeFrontValid
      //nextWrBgPipeFrontValid := True
      when (!rSavedWrBgPipeFrontValid) {
        when (
          //rIntnlChangingRow
          intnlChangingRowRe


          //nextIntnlChangingRowRe
          //nextWrBgChangingRow && !rWrBgChangingRow
          //outp.physPosInfo.changingRow
          //changingRowRe
        ) {
          nextWrBgPipeFrontValid := True
          rWrBgPipeFrontPayload := doInitWrBgPipePayload(
            //prevLineMemArrIdx=rWrBgPipeFrontPayload.lineMemArrIdx
            //prevLineMemArrIdx=0
            //prevLineNum=rWrBgPipeFrontPayload.lineNum
            firstInit=false
          )
          //rWrBgChangingRow := False
        } otherwise {
          nextWrBgPipeFrontValid := rWrBgPipeFrontValid
        }
      } otherwise { // when (rSavedWrBgPipeFrontValid)
        when (wrBgPipeIn(0).fire) {
          // BEGIN: test logic
          when (
            rWrBgPipeFrontPayload.bakCntWillBeDone()
            //rWrBgPipeFrontPayload.cnt + 1 === params.oneLineMemSize
          ) {
            nextWrBgPipeFrontValid := False
            ////rWrBgChangingRow := wrBgPipeLast.bakCntWillBeDone()
            ////rWrBgChangingRow := True
            //rWrBgPipeFrontPayload := doInitWrBgPipePayload(
            //  //prevLineMemArrIdx=rWrBgPipeFrontPayload.lineMemArrIdx,
            //)
            //rGlobWrBgLineNum := rGlobWrBgLineNumPipe1(
            //  rGlobWrBgLineNum.bitsRange
            //)
          } otherwise {
            nextWrBgPipeFrontValid := rWrBgPipeFrontValid
            rWrBgPipeFrontPayload.cnt := rWrBgPipeFrontPayload.cnt + 1
            rWrBgPipeFrontPayload.bakCnt := (
              rWrBgPipeFrontPayload.bakCnt - 1
            )
            rWrBgPipeFrontPayload.bakCntMinus1 := (
              rWrBgPipeFrontPayload.bakCntMinus1 - 1
            )

            //rSavedWrBgPipeFrontPayload.cnt := (
            //  rWrBgPipeFrontPayload.cnt + 1
            //)
            //rSavedWrBgPipeFrontPayload.bakCnt := (
            //  rWrBgPipeFrontPayload.bakCnt - 1
            //)
            //rSavedWrBgPipeFrontPayload.bakCntMinus1 := (
            //  rWrBgPipeFrontPayload.bakCntMinus1 - 1
            //)
          }
          // END: test logic
        } otherwise { // when (!wrBgPipeIn(0).fire)
          nextWrBgPipeFrontValid := rWrBgPipeFrontValid
        }
      }
    //}

    val wrObjPipeIn = KeepAttribute(
      //Vec.fill(wrBgObjPipeNumStages)(Reg(WrObjPipePayload()))
      Vec.fill(
        wrBgObjPipeNumStages
        //anyPipeNumStages
      )(
        //Flow(WrObjPipePayload())
        Flow(WrObjPipePayload())
      )
    )
    val wrObjPipeOut = KeepAttribute(
      //Vec.fill(wrBgObjPipeNumStages)(Reg(WrObjPipePayload()))
      Vec.fill(
        wrBgObjPipeNumStages
        //anyPipeNumStages
      )(
        //Flow(WrObjPipePayload())
        Flow(WrObjPipePayload())
      )
    )
    //wrObjPipeOut.last.ready := (
    //  // This is a heuristic!
    //  objLineFifo.io.misc.amountCanPush
    //  > params.wrBgObjStallFifoAmountCanPush
    //)


    val wrObjPipeLast = KeepAttribute(
      //Flow(WrObjPipePayload())
      Flow(WrObjPipePayload())
    )
    //when (nextIntnlChangingRow && rWrObjChangingRow) 
    //when (rIntnlChangingRow)
    when (intnlChangingRowRe) 
    {
      nextWrObjChangingRow := False
    } elsewhen (
      wrObjPipeLast.bakCntWillBeDone() && wrObjPipeLast.fire
      //(wrObjPipeLast.cnt + 1 === params.oneLineMemSize) && wrObjPipeLast.fire
      //wrObjPipeLast.bakCnt.msb && wrObjPipeLast.fire
      //wrObjPipeLast.bakCnt.msb
    ) {
      nextWrObjChangingRow := True
    } otherwise {
      nextWrObjChangingRow := rWrObjChangingRow
    }
    //val rWrObjPipePayloadVec = KeepAttribute(
    //  Vec.fill(wrBgObjPipeNumStages)(
    //    Reg(WrObjPipePayload()) init(wrObjPipe(0).payload.getZero)
    //  )
    //)

    //for (idx <- 0 to wrBgObjPipeNumStages - 1) {
    //  //rWrObjPipe(idx).init(rWrObjPipe(idx).getZero)
    //}
    //wrObjPipe(0).valid := True
    //val rWrObjPipeFrontValid = Reg(Bool()) init(True)
    val nextWrObjPipeFrontValid = Bool()
    val rWrObjPipeFrontValid = RegNext(nextWrObjPipeFrontValid)
      .init(True)
    val rSavedWrObjPipeFrontValid = Reg(Bool()) init(False)
    //when (wrObjPipe(0).objAttrsMemIdxWillUnderflow()) {
    //  rWrObjPipeFrontValid := False
    //}
    val rWrObjPipeFrontPayload = Reg(WrObjPipePayload())
    //rWrObjPipeFrontPayload.init(rWrObjPipeFrontPayload.getZero)
    rWrObjPipeFrontPayload.init(doInitWrObjPipePayload(
      //prevLineMemArrIdx=wrLineMemArrIdxInit
      //prevLineNum=wrLineNumInit - 1
      //prevLineNum=wrLineNumInit
      firstInit=true
    ))

    wrObjPipeIn(0).valid := rWrObjPipeFrontValid
    wrObjPipeIn(0).payload := rWrObjPipeFrontPayload
    //val rWrObjPipeOutPayload6 = Reg(cloneOf(wrObjPipeOut(6)))
    //  .init(wrObjPipeOut(6).getZero)
    //for (idx <- 1 to wrObjPipeIn.size - 1) {
    //  wrObjPipeIn(idx) <-< wrObjPipeOut(idx - 1)
    //}
    for (idx <- 1 to wrObjPipeIn.size - 1) {
      // Create pipeline registering
      //when (idx - 1 != 6) {
        wrObjPipeIn(idx) <-< wrObjPipeOut(idx - 1)
      //} otherwise {
      //}
      //wrObjPipeIn(idx) <-/< wrObjPipeOut(idx - 1)
    }
    for (idx <- 0 to wrObjPipeOut.size - 1) {
      // Connect output `valid` to input `valid`
      wrObjPipeOut(idx).valid := wrObjPipeIn(idx).valid
      //wrObjPipeIn(idx).ready := wrObjPipeOut(idx).ready
    }
    //wrObjPipeOut(6).payload.setAsReg()
    // add one final register
    wrObjPipeLast <-< wrObjPipeOut.last
    //wrObjPipeLast <-/< wrObjPipeOut.last
    //wrObjPipe.last.ready := True

    // Send data into the sprite pipeline
    //when (nextIntnlChangingRow) {
    //  rWrObjChangingRow := False
    //}
    //when (combinePipeOverflow) {
    //  nextWrObjPipeFrontValid := False
    //} otherwise {
      rSavedWrObjPipeFrontValid := nextWrObjPipeFrontValid
      //nextWrObjPipeFrontValid := True
      when (!rSavedWrObjPipeFrontValid) {
        when (
          //rIntnlChangingRow
          intnlChangingRowRe
          //outp.physPosInfo.changingRow
          //changingRowRe
          //nextIntnlChangingRowRe
          //nextWrObjChangingRow && !rWrObjChangingRow
        ) {
          nextWrObjPipeFrontValid := True
          rWrObjPipeFrontPayload := doInitWrObjPipePayload(
            //prevLineMemArrIdx=rWrObjPipeFrontPayload.lineMemArrIdx
            //prevLineMemArrIdx=0
            //prevLineNum=rWrObjPipeFrontPayload.lineNum
            firstInit=false
          )
          //rWrObjChangingRow := False
        } otherwise {
          nextWrObjPipeFrontValid := rWrObjPipeFrontValid
        }
      } otherwise { // when (rSavedWrObjPipeFrontValid)
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
            //rWrObjPipeFrontPayload.cnt + 1 === params.oneLineMemSize
          ) {
            nextWrObjPipeFrontValid := False
            //rWrObjChangingRow := True
            //rWrObjPipeFrontPayload := doInitWrObjPipePayload(
            //  //prevLineMemArrIdx=rWrObjPipeFrontPayload.lineMemArrIdx
            //)
            //rGlobWrObjLineNum := rGlobWrObjLineNumPipe1(
            //  rGlobWrObjLineNum.bitsRange
            //)
          } otherwise {
            nextWrObjPipeFrontValid := rWrObjPipeFrontValid
            rWrObjPipeFrontPayload.cnt := rWrObjPipeFrontPayload.cnt + 1
            rWrObjPipeFrontPayload.bakCnt := (
              rWrObjPipeFrontPayload.bakCnt - 1
            )
            rWrObjPipeFrontPayload.bakCntMinus1 := (
              rWrObjPipeFrontPayload.bakCntMinus1 - 1
            )
            rWrObjPipeFrontPayload.stage0.objAttrsMemIdx() := (
              rWrObjPipeFrontPayload.stage0.objAttrsMemIdx() - 1
            )
          }
        } otherwise { // when (!wrObjPipeIn(0).fire)
          nextWrObjPipeFrontValid := rWrObjPipeFrontValid
        }
      //when (rWrObjPipeFrontPayload.objAttrsMemIdxWillUnderflow()) {
      //  rWrObjPipeFrontValid := False
      //}
      }
    //}

    //val sendIntoFifoPipeIn = KeepAttribute(
    //  Vec.fill(sendIntoFifoPipeNumMainStages)(
    //    Stream(SendIntoFifoPipePayload())
    //  )
    //)
    //val sendIntoFifoPipeOut = KeepAttribute(
    //  Vec.fill(sendIntoFifoPipeNumMainStages)(
    //    Stream(SendIntoFifoPipePayload())
    //  )
    //)
    //for (idx <- 1 to sendIntoFifoPipeNumMainStages - 1) {
    //  sendIntoFifoPipeIn(idx) <-/< sendIntoFifoPipeOut(idx - 1)
    //}
    //for (idx <- 0 to sendIntoFifoPipeNumMainStages - 1) {
    //  sendIntoFifoPipeOut(idx).valid := sendIntoFifoPipeIn(idx).valid
    //  sendIntoFifoPipeIn(idx).ready := sendIntoFifoPipeOut(idx).ready
    //}
    //val rSendIntoFifoPipeFrontValid = Reg(Bool()) init(True)
    //val rSendIntoFifoPipeFrontPayload = Reg(SendIntoFifoPipePayload())
    //rSendIntoFifoPipeFrontPayload.init(
    //  //rSendIntoFifoPipeFrontPayload.getZero
    //  doInitSendIntoFifoPipePayload(
    //    prevLineMemArrIdx=sendIntoFifoLineMemArrIdxInit
    //  )
    //)

    //sendIntoFifoPipeIn(0).valid := rSendIntoFifoPipeFrontValid
    //sendIntoFifoPipeIn(0).payload := rSendIntoFifoPipeFrontPayload
    //sendIntoFifoPipeOut.last.translateInto(
    //  lineFifo.io.push
    //)(
    //  dataAssignment=(
    //    bgLineFifoPushPayload,
    //    sendIntoFifoPipePayload
    //  ) => {
    //    bgLineFifoPushPayload := (
    //      sendIntoFifoPipePayload.bgLineMemEntry
    //    )
    //  }
    //)
    ////lineFifo.io.
    ////lineFifo.io.

    //when (sendIntoFifoPipeIn(0).fire) {
    //  // BEGIN: test logic

    //  when (
    //    rSendIntoFifoPipeFrontPayload.bakCntWillBeDone()
    //    //rSendIntoFifoPipeFrontPayload.cnt + 1 === params.oneLineMemSize
    //  ) {
    //    //rSendIntoFifoPipeFrontValid := False
    //    //rSendIntoFifoChangingRow := True
    //    rSendIntoFifoPipeFrontPayload := doInitSendIntoFifoPipePayload(
    //      //prevLineMemArrIdx=0
    //      prevLineMemArrIdx=rSendIntoFifoPipeFrontPayload.lineMemArrIdx
    //    )
    //  } otherwise {
    //    rSendIntoFifoPipeFrontPayload.cnt := (
    //      rSendIntoFifoPipeFrontPayload.cnt + 1
    //    )
    //    rSendIntoFifoPipeFrontPayload.bakCnt := (
    //      rSendIntoFifoPipeFrontPayload.bakCnt - 1
    //    )
    //    rSendIntoFifoPipeFrontPayload.bgLineMemEntry := (
    //      rSendIntoFifoPipeFrontPayload.bgLineMemEntry.getZero
    //    )
    //    //rSendIntoFifoPipeFrontPayload.bakCntMinus1 := (
    //    //  rSendIntoFifoPipeFrontPayload.bakCntMinus1 - 1
    //    //)
    //  }
    //  // END: test logic
    //}

    //val sendIntoObjFifoPipeIn = KeepAttribute(
    //  Vec.fill(sendIntoFifoPipeNumMainStages)(
    //    Stream(SendIntoObjFifoPipePayload())
    //  )
    //)
    //val sendIntoObjFifoPipeOut = KeepAttribute(
    //  Vec.fill(sendIntoFifoPipeNumMainStages)(
    //    Stream(SendIntoObjFifoPipePayload())
    //  )
    //)
    //for (idx <- 1 to sendIntoFifoPipeNumMainStages - 1) {
    //  sendIntoObjFifoPipeIn(idx) <-/< sendIntoObjFifoPipeOut(idx - 1)
    //}
    //for (idx <- 0 to sendIntoFifoPipeNumMainStages - 1) {
    //  sendIntoObjFifoPipeOut(idx).valid := (
    //    sendIntoObjFifoPipeIn(idx).valid
    //  )
    //  sendIntoObjFifoPipeIn(idx).ready := (
    //    sendIntoObjFifoPipeOut(idx).ready
    //  )
    //}
    //val rSendIntoObjFifoPipeFrontValid = Reg(Bool()) init(True)
    //val rSendIntoObjFifoPipeFrontPayload = Reg(
    //  SendIntoObjFifoPipePayload()
    //)
    //rSendIntoObjFifoPipeFrontPayload.init(
    //  //rSendIntoObjFifoPipeFrontPayload.getZero
    //  doInitSendIntoObjFifoPipePayload(
    //    prevLineMemArrIdx=sendIntoFifoLineMemArrIdxInit
    //  )
    //)

    //sendIntoObjFifoPipeIn(0).valid := rSendIntoObjFifoPipeFrontValid
    //sendIntoObjFifoPipeIn(0).payload := rSendIntoObjFifoPipeFrontPayload
    //sendIntoObjFifoPipeOut.last.translateInto(
    //  objLineFifo.io.push
    //)(
    //  dataAssignment=(
    //    objLineFifoPushPayload,
    //    sendIntoObjFifoPipePayload
    //  ) => {
    //    objLineFifoPushPayload := (
    //      sendIntoObjFifoPipePayload.objLineMemEntry
    //    )
    //  }
    //)
    ////lineFifo.io.
    ////lineFifo.io.

    //when (sendIntoObjFifoPipeIn(0).fire) {
    //  // BEGIN: test logic

    //  when (
    //    rSendIntoObjFifoPipeFrontPayload.bakCntWillBeDone()
    //    //rSendIntoObjFifoPipeFrontPayload.cnt + 1 === params.oneLineMemSize
    //  ) {
    //    //rSendIntoObjFifoPipeFrontValid := False
    //    //rSendIntoObjFifoChangingRow := True
    //    rSendIntoObjFifoPipeFrontPayload := (
    //      doInitSendIntoObjFifoPipePayload(
    //        //prevLineMemArrIdx=0
    //        prevLineMemArrIdx=(
    //          rSendIntoObjFifoPipeFrontPayload.lineMemArrIdx
    //        )
    //      )
    //    )
    //  } otherwise {
    //    rSendIntoObjFifoPipeFrontPayload.cnt := (
    //      rSendIntoObjFifoPipeFrontPayload.cnt + 1
    //    )
    //    rSendIntoObjFifoPipeFrontPayload.bakCnt := (
    //      rSendIntoObjFifoPipeFrontPayload.bakCnt - 1
    //    )
    //    rSendIntoObjFifoPipeFrontPayload.objLineMemEntry := (
    //      rSendIntoObjFifoPipeFrontPayload.objLineMemEntry.getZero
    //    )
    //    //rSendIntoObjFifoPipeFrontPayload.bakCntMinus1 := (
    //    //  rSendIntoObjFifoPipeFrontPayload.bakCntMinus1 - 1
    //    //)
    //  }
    //  // END: test logic
    //}

    ////val wrCombinePipe = KeepAttribute(
    ////  Vec.fill()(Stream(WrCombinePipeElem()))
    ////)

    ////if (!wrPipeBgNumElemsGtNumBgs) {
    ////} else { // if (wrPipeBgNumElemsGtNumBgs)
    ////}
    ////if (!wrPipeObjNumElemsGtNumObjsPow) {
    ////} else { // if (wrPipeObjNumElemsGtNumObjsPow)
    ////}

    ////val wrBgLineMemEntry = LineMemEntry()
    ////val rPastWrBgLineMemEntry = Reg(LineMemEntry())
    ////rPastWrBgLineMemEntry.init(rPastWrBgLineMemEntry.getZero)

    val combinePipeIn = KeepAttribute(
      Vec.fill(
        combinePipeNumMainStages
        //anyPipeNumStages
      )(
        //Stream(CombinePipePayload())
        Stream(CombinePipePayload())
      )
    )
    val combinePipeOut = KeepAttribute(
      Vec.fill(
        combinePipeNumMainStages
        //anyPipeNumStages
      )(
        //Stream(CombinePipePayload())
        Stream(CombinePipePayload())
      )
    )
    val combinePipeLast = KeepAttribute(
      //Stream(CombinePipePayload())
      Stream(CombinePipePayload())
    )
    //rCombineChangingRow := combinePipeLast.bakCntWillBeDone()
    ////when (nextIntnlChangingRow)
    //when (rIntnlChangingRow) 
    val rPastPopFire = RegNext(pop.fire)

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
    //  //combinePipeLast.bakCntWillBeDone() && pop.fire
    //  //combinePipeLast.bakCntWillBeDone() && pop.fire
    //  combinePipeLast.bakCntWillBeDone() && pop.fire
    //  //&& !combinePipeOverflow

    //  //(combinePipeLast.bakCnt === 0) && rPastPopFire
    //  //(combinePipeLast.cnt + 1 === params.oneLineMemSize)
    //  //&& combinePipeLast.fire
    //  //combinePipeLast.bakCnt.msb && combinePipeLast.fire
    //  //combinePipeLast.bakCnt.msb
    //) {
    //  nextCombineChangingRow := True
    //} otherwise {
    //  nextCombineChangingRow := rCombineChangingRow
    //}

    //when (
    //  intnlChangingRowRe
    //) {
    //  nextCombineChangingRow := False
    //} elsewhen (combinePipeLast.fire) {
    //  nextCombineChangingRow := combinePipeLast.changingRow
    //} otherwise {
    //  nextCombineChangingRow := rCombineChangingRow
    //}
    nextCombineChangingRow := combinePipeLast.changingRow

    //val rCombinePipeFrontValid = Reg(Bool()) init(True)
    val nextCombinePipeFrontValid = Bool()
    val rCombinePipeFrontValid = RegNext(nextCombinePipeFrontValid)
      .init(True)
    val rSavedCombinePipeFrontValid = Reg(Bool()) init(False)
    val rCombinePipeFrontPayload = Reg(CombinePipePayload())
    //rCombinePipeFrontPayload.init(rCombinePipeFrontPayload.getZero)
    rCombinePipeFrontPayload.init(doInitCombinePipePayload(
      //changingRow=False,
      changingRow=True,
      //prevRdLineMemArrIdx=combineRdLineMemArrIdxInit,
      //prevWrLineMemArrIdx=combineWrLineMemArrIdxInit,
      //prevLineMemArrIdx=combineLineMemArrIdxInit
    ))

    val haltCombinePipe = Bool()
    val combinePipeInVeryFront = Stream(CombinePipePayload())
    val combinePipeInVeryFrontHaltWhen = combinePipeInVeryFront.haltWhen(
      haltCombinePipe
    )
    //combinePipeIn(0) << combinePipeInVeryFrontHaltWhen
    combinePipeIn(0) <-/< combinePipeInVeryFrontHaltWhen
    //combinePipeIn(0).valid := rCombinePipeFrontValid
    //combinePipeIn(0).payload := rCombinePipeFrontPayload
    combinePipeInVeryFront.valid := rCombinePipeFrontValid
    combinePipeInVeryFront.payload := rCombinePipeFrontPayload

    //val combinePipeStage1Busy = Reg(Bool()) init(False)
    //val combinePipeStage1Busy = Bool()
    //val combinePipeStage1HaltWhen = combinePipeIn(1).haltWhen(
    //  combinePipeStage1Busy
    //)
    //for (idx <- 0 to combinePipeNumMainStages - 1) {
    //  if (idx == 1) {
    //    combinePipeOut(idx).valid := combinePipeStage1HaltWhen.valid
    //    combinePipeStage1HaltWhen.ready := combinePipeOut(idx).ready
    //  } else {
    //    combinePipeOut(idx).valid := combinePipeIn(idx).valid
    //    combinePipeIn(idx).ready := combinePipeOut(idx).ready
    //  }
    //}
    //for (idx <- 1 to combinePipeNumMainStages - 1) {
    //  combinePipeIn(idx) <-/< combinePipeOut(idx - 1)
    //}
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

    //combinePipeOut.last.translateInto(
    //  pop
    //)(
    //  dataAssignment=(
    //    popPayload,
    //    combinePipeOutLastPayload,
    //  ) => {
    //    popPayload := outp
    //  }
    //)

    for (idx <- 1 to combinePipeIn.size - 1) {
      // Create pipeline registering
      //combinePipeIn(idx) <-< combinePipeOut(idx - 1)
      combinePipeIn(idx) <-/< combinePipeOut(idx - 1)
    }
    for (idx <- 0 to combinePipeOut.size - 1) {
      // Connect output `valid` to input `valid`
      combinePipeOut(idx).valid := combinePipeIn(idx).valid
      //if (idx != 1) {
        combinePipeIn(idx).ready := combinePipeOut(idx).ready
      //} otherwise {
      //  combinePipeIn(idx).ready := 
      //}
    }

    // add one final register
    //combinePipeLast <-< combinePipeOut.last
    combinePipeLast <-/< combinePipeOut.last
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
    //  push=combinePipeLast,
    //  //push=combinePipeOut.last,
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
    //pop.valid := False
    //pop.payload := outp

    
    //combinePipeLast.toStream(
    //  overflow=combinePipeOverflow,
    //  fifoSize=params.combinePipeOverflowFifoSize,
    //  overflowOccupancyAt=params.combinePipeOverflowOccupancyAt,
    //).translateInto(
    //  pop
    //)(
    //  dataAssignment=(
    //    popPayload,
    //    combinePipeLastPayload,
    //  ) => {
    //    popPayload := outp
    //  }
    //)
    combinePipeLast.translateInto(
      pop
    )(
      dataAssignment=(
        popPayload,
        combinePipeLastPayload,
      ) => {
        popPayload := outp
      }
    )

    //pop.valid := (
    //  wrBgPipeLast.valid && wrObjPipeLast.valid && combinePipeLast.valid
    //)
    //wrBgPipeLast.ready := pop.ready
    //wrObjPipeLast.ready := pop.ready
    //combinePipeLast.ready := pop.ready

    // Send data into the combine pipeline
    //when (nextIntnlChangingRow) {
    //  rCombineChangingRow := False
    //} elsewhen (rCombinePipeFrontPayload.bakCntWillBeDone()) {
    //  rCombineChangingRow := True
    //}

    //when (combinePipeOverflow) {
    //  nextCombinePipeFrontValid := False
    //} otherwise {
      rSavedCombinePipeFrontValid := nextCombinePipeFrontValid
      nextCombinePipeFrontValid := True
      //when (!rSavedCombinePipeFrontValid) {
      //  when (
      //    //rIntnlChangingRow
      //    intnlChangingRowRe
      //    //outp.physPosInfo.changingRow
      //    //changingRowRe
      //    //nextIntnlChangingRowRe
      //    //nextCombineChangingRow && !rCombineChangingRow
      //  ) {
      //    //rCombinePipeFrontValid := True
      //    //nextCombinePipeFrontValid := True
      //    //rCombinePipeFrontValid := False
      //    rCombinePipeFrontPayload := doInitCombinePipePayload(
      //      //prevRdLineMemArrIdx=rCombinePipeFrontPayload.rdLineMemArrIdx,
      //      //prevWrLineMemArrIdx=rCombinePipeFrontPayload.wrLineMemArrIdx,
      //      //prevRdLineMemArrIdx=0,
      //      //prevWrLineMemArrIdx=0,
      //      //prevLineMemArrIdx=rCombinePipeFrontPayload.lineMemArrIdx,
      //    )
      //    //rCombineChangingRow := False
      //  }
      //  //otherwise {
      //  //  //nextCombinePipeFrontValid := rCombinePipeFrontValid
      //  //}
      //} otherwise { // when (rSavedCombinePipeFrontValid)
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

        val rPastHaltCombinePipe = RegNext(haltCombinePipe) init(False)
        when (intnlChangingRowRe) {
          haltCombinePipe := False
        } elsewhen (
          //combinePipeIn(0).fire
          //&& combinePipeIn(0).changingRow
          //combinePipeInVeryFront.fire
          //&& combinePipeInVeryFront.changingRow
          //combinePipeInVeryFront.changingRow
          //combinePipeIn(0).changingRow
          //combinePipeInVeryFront.changingRow
          combinePipeInVeryFrontHaltWhen.changingRow
        ) {
          haltCombinePipe := True
        } otherwise {
          //haltCombinePipe := False
          haltCombinePipe := rPastHaltCombinePipe
        }
        when (
          //combinePipeIn(0).fire
          combinePipeInVeryFrontHaltWhen.fire
        ) {
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
            //rCombinePipeFrontPayload.cnt + 1 === params.oneLineMemSize
          ) {
            ////rCombinePipeFrontValid := False
            //nextCombinePipeFrontValid := False
            ////rCombineChangingRow := True
            //nextCombineChangingRow := True
            rCombinePipeFrontPayload := doInitCombinePipePayload(
              //prevLineMemArrIdx=0
              changingRow=True,
              //prevLineMemArrIdx=rCombinePipeFrontPayload.lineMemArrIdx
              //prevLineMemArrIdx=rCombinePipeFrontPayload.lineMemArrIdx
              //prevLineMemArrIdx=0
            )
          } otherwise {
            //nextCombineChangingRow := False
            //nextCombinePipeFrontValid := rCombinePipeFrontValid
            rCombinePipeFrontPayload.changingRow := False
            rCombinePipeFrontPayload.cnt := (
              rCombinePipeFrontPayload.cnt + 1
            )
            rCombinePipeFrontPayload.bakCnt := (
              rCombinePipeFrontPayload.bakCnt - 1
            )
            rCombinePipeFrontPayload.bakCntMinus1 := (
              rCombinePipeFrontPayload.bakCntMinus1 - 1
            )
          }
          // END: test logic
        } otherwise {
          //nextCombineChangingRow := rCombineChangingRow
        }
        //.otherwise {
        //  nextCombinePipeFrontValid := rCombinePipeFrontValid
        //}
        //when (rCombinePipeFrontPayload.bakCntWillBeDone()) {
        //  rCombinePipeFrontValid := False
        //  //rCombineChangingRow := True
        //}
      //}
    //}

    //def clearObjLineMemEntries(): Unit = {
    //  val tempObjLineMemEntry = ObjSubLineMemEntry()
    //  tempObjLineMemEntry := tempObjLineMemEntry.getZero
    //  switch (rRdLineMemArrIdx) {
    //    for (idx <- 0 to (1 << rRdLineMemArrIdx.getWidth) - 1) {
    //      is (idx) {
    //        objSubLineMemArr(idx).write(
    //          address=rRdLineMemCnt,
    //          data=tempObjLineMemEntry,
    //        )
    //      }
    //    }
    //  }
    //}
    //val tempWrBgPxsPos = KeepAttribute(params.bgPxsCoordT())
    //val tempWrBgPipeLineMemAddr = KeepAttribute(
    //  Vec.fill(params.bgTileSize2d.x)(
    //    UInt(log2Up(
    //      //params.oneLineMemSize
    //      params.oneLineMemSize
    //    ) bits)
    //  )
    //)

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
      //val bgLineMem = bgSubLineMemArr(someWrLineMemArrIdx)

      val stageData = DualPipeStageData[Flow[WrBgPipePayload]](
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
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
          //tempOutp.lineMemArrIdx := tempInp.lineMemArrIdx
          tempOutp.lineNum := tempInp.lineNum
          tempOutp.cnt := tempInp.cnt
          tempOutp.bakCnt := tempInp.bakCnt
          tempOutp.bakCntMinus1 := tempInp.bakCntMinus1
          tempOutp.bgIdx := tempInp.bgIdx
          //tempOut.stage0 := tempInp.stage0
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).cntMinus1 := (
      //      stageData.pipeIn(idx).cntMinus1
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).bgIdx := (
      //      stageData.pipeIn(idx).bgIdx
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          //val dbgTestPxPosX = Vec.fill(params.bgTileSize2d.x)(
          //)
          for (x <- 0 to params.bgTileSize2d.x - 1) {
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
                  def tempBgTileWidthPow = params.bgTileSize2dPow.x
                  tempOutp.pxPos(x).x := (
                    Cat(
                      tempInp.cnt(
                        (
                          log2Up(params.intnlFbSize2d.x)
                          + params.numBgsPow
                          - params.bgTileSize2dPow.x
                          - 1
                        ) downto (
                          params.numBgsPow
                          //+ params.bgTileSize2dPow.x
                        )
                      ),
                      U(f"$tempBgTileWidthPow'd$x")
                    ).asUInt
                    - tempInp.bgAttrs.scroll.x

                    //+ tempInp.bgAttrs.scroll.x
                  )
                  //tempOutp.pxPos(x).x := (
                  //  //--------
                  //  // BEGIN: old, possibly not working
                  //  //(
                  //  //  tempInp.stage0.getCntPxPosX(x=x)
                  //  //  //+ bgAttrsArr(tempBgIdx).scroll.x
                  //  //  - tempInp.bgAttrs.scroll.x
                  //  //)//.resized
                  //  // END: old, possibly not working
                  //  (
                  //    tempInp.cnt(
                  //      (
                  //        log2Up(params.intnlFbSize2d.x)
                  //        + params.numBgsPow
                  //        - 1
                  //      ) downto (
                  //        params.numBgsPow
                  //        + params.bgTileSize2dPow.x
                  //      )
                  //    ) << params.bgTileSize2dPow.x
                  //  ) | (
                  //    U(f"$tempBgTileWidthPow'd$x")
                  //  )
                  //  //(
                  //  //  x
                  //  //  - tempInp.bgAttrs.scroll.x
                  //  //).resized
                  //  //(
                  //  //  params.bgTileSize2dPow.x - 1 downto 0
                  //  //)
                  //)
                  tempOutp.pxPos(x).y := (
                    (
                      //rWrLineNum.resized
                      //- bgAttrsArr(tempBgIdx).scroll.y
                      //bgAttrsArr(tempBgIdx).scroll.y
                      //- 
                      //tempInp.lineNum.resized
                      //- tempInp.bgAttrs.scroll.y
                      tempInp.lineNum
                      - tempInp.bgAttrs.scroll.y
                      //+ tempInp.bgAttrs.scroll.y
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
                      // BEGIN: old, pre-multi-pixel pipeline
                      (
                        params.bgSize2dInTilesPow.x
                        + params.bgTileSize2dPow.x - 1
                      )
                      downto params.bgTileSize2dPow.x
                      // END: old, pre-multi-pixel pipeline
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

                      // BEGIN: old, pre-multi-pixel pipeline
                      (
                        params.bgSize2dInTilesPow.y
                        + params.bgTileSize2dPow.y - 1
                      )
                      downto params.bgTileSize2dPow.y
                      // END: old, pre-multi-pixel pipeline
                      //tempWrBgPxsPos.y.high
                      //downto params.bgTileSize2dPow.y
                    )
                    //(
                    //  params.bgTileSize2dPow.y - 1 downto 0
                    //)
                    ,
                  )
                  tempOutp.bgEntryMemIdx(x) := Cat(
                    //inpScroll.y(scrollSliceRange.y),
                    //inpScroll.x(scrollSliceRange.x),
                    tempOutp.pxPos(x).y(tempSliceRange.y),
                    tempOutp.pxPos(x).x(tempSliceRange.x),
                  ).asUInt
                }
              }
            }
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          for (x <- 0 to params.bgTileSize2d.x - 1) {
            switch (tempInp.bgIdx) {
              for (tempBgIdx <- 0 to params.numBgs - 1) {
                is (tempBgIdx) {
                  tempOutp.bgEntry(x) := (
                    bgEntryMemArr(tempBgIdx).readAsync(
                      address=tempInp.bgEntryMemIdx(x)
                    )
                  )
                }
              }
            }
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          val tempTilePxsPos = Vec.fill(params.bgTileSize2d.x)(
            params.bgTilePxsCoordT()
          )
          for (x <- 0 to params.bgTileSize2d.x - 1) {
            switch (tempInp.bgIdx) {
              for (tempBgIdx <- 0 to params.numBgs - 1) {
                is (tempBgIdx) {
                  tempTilePxsPos(x).x := (
                    //(
                    //  tempInp.getCntPxPosX()
                    //  //+ bgAttrsArr(tempBgIdx).scroll.x
                    //  - tempInp.bgAttrs.scroll.x
                    //)
                    //tempInp.pxPos(x).x
                    //(
                    //  params.bgTileSize2dPow.x - 1 downto 0
                    //)
                    (
                      {
                        def tempTileWidth = params.bgTileSize2dPow.x
                        U(f"$tempTileWidth'd$x")
                      } - tempInp.bgAttrs.scroll.x
                    )(tempTilePxsPos(x).x.bitsRange)
                  )
                  tempTilePxsPos(x).y := (
                    //(
                    //  //rWrLineNum.resized
                    //  //- bgAttrsArr(tempBgIdx).scroll.y
                    //  //bgAttrsArr(tempBgIdx).scroll.y
                    //  //- 
                    //  rWrLineNum.resized
                    //  - tempInp.bgAttrs.scroll.y
                    //)
                    (
                      tempInp.lineNum
                      //+ tempInp.bgAttrs.scroll.y
                      - tempInp.bgAttrs.scroll.y
                    ).resized
                    //tempInp.pxPos(x).y
                    //(
                    //  params.bgTileSize2dPow.y - 1 downto 0
                    //)
                  )
                  when (!tempInp.bgEntry(x).dispFlip.x) {
                    tempOutp.tilePxsCoord(x).x := tempTilePxsPos(x).x
                  } otherwise {
                    tempOutp.tilePxsCoord(x).x := (
                      params.bgTileSize2d.x - 1 - tempTilePxsPos(x).x
                    )
                  }
                  when (!tempInp.bgEntry(x).dispFlip.y) {
                    tempOutp.tilePxsCoord(x).y := tempTilePxsPos(x).y
                  } otherwise {
                    tempOutp.tilePxsCoord(x).y := (
                      params.bgTileSize2d.y - 1 - tempTilePxsPos(x).y
                    )
                  }
                }
              }
            }
            tempOutp.tile(x) := (
              bgTileMem.readAsync(address=tempInp.bgEntry(x).tileMemIdx)
            )
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    tempOutp.tile := (
      //      bgTileMem.readAsync(address=tempInp.bgEntry.tileMemIdx)
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
          for (x <- 0 to params.bgTileSize2d.x - 1) {
            tempOutp.palEntryMemIdx(x) := tempInp.payload.tile(x).getPx(
              tempInp.payload.tilePxsCoord(x)
            )
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          //tempOutp.palEntry := bgPalEntryMem.readAsync(
          //  address=tempInp.palEntryMemIdx
          //)
          for (x <- 0 to params.bgTileSize2d.x - 1) {
            tempOutp.palEntryNzMemIdx(x) := (
              tempInp.palEntryMemIdx(x) =/= 0
            )
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          // we can read from `bgPalEntryMem` even when
          for (x <- 0 to params.bgTileSize2d.x - 1) {
            tempOutp.palEntry(x) := bgPalEntryMem.readAsync(
              address=tempInp.palEntryMemIdx(x)
            )
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
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
          //rPastLineMemEntry := tempLineMemEntry
          val rPastLineMemEntry = KeepAttribute(
            //Reg(cloneOf(tempLineMemEntry))
            //Reg(cloneOf(tempOutp.lineMemEntry))
            Vec.fill(params.bgTileSize2d.x)(
              Reg(BgSubLineMemEntry())
            )
          )
          rPastLineMemEntry.setName(f"rPastWrBgLineMemEntry")
          for (x <- 0 to params.bgTileSize2d.x - 1) {
            def tempLineMemEntry = tempOutp.lineMemEntry(x)
            rPastLineMemEntry(x).init(rPastLineMemEntry(x).getZero)
            when (
              (bgIdx === (1 << bgIdx.getWidth) - 1)
              //&& !tempInp.bakCnt.msb
            ) {
              rPastLineMemEntry(x) := rPastLineMemEntry(x).getZero
            } otherwise {
              rPastLineMemEntry(x) := tempLineMemEntry
            }
            when (
              // This could be split into more pipeline stages, but it
              // might not be necessary with 4 or fewer backgrounds
              (
                (
                  (bgIdx === (1 << bgIdx.getWidth) - 1)
                  || (
                    //rPastLineMemEntry(x).col.a === False
                    !rPastLineMemEntry(x).col.a
                    //&& pastLineMemEntry(x).prio === 
                  )
                ) && (
                  !tempInp.bakCnt.msb
                )
              ) //&& tempInp.bgAttrs.visib
              //bgIdx === 0
            ) {
              // Starting rendering a new pixel or overwrite the existing
              // pixel
              tempLineMemEntry.col.rgb := tempInp.palEntry(x).col
              tempLineMemEntry.col.a := tempInp.palEntryNzMemIdx(x)
              tempLineMemEntry.prio := bgIdx
              tempLineMemEntry.addr := (
                tempInp.pxPos(x).x
                + tempInp.bgAttrs.scroll.x
              )
              //rPastLineMemEntry(x) := tempLineMemEntry
            } otherwise {
              tempLineMemEntry := rPastLineMemEntry(x)
            }
          
          //tempOutp.doWrite := (bgIdx === 0)
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage7 := (
            stageData.pipeIn(idx).stage7
          )
        },
      )
      //HandleDualPipe(
      //  stageData=stageData.craft(8)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    //when (tempOutp.doWrite) {
      //    //} otherwise {
      //    //}
      //    switch (
      //      //rWrLineMemArrIdx
      //      //wrBgPipeLast.lineMemArrIdx
      //      tempInp.lineMemArrIdx
      //    ) {
      //      for (
      //        //idx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1
      //        idx <- 0 to (1 << tempInp.lineMemArrIdx.getWidth) - 1
      //        //idx <- 0 to (1 << wrBgPipeLast.lineMemArrIdx.getWidth) - 1
      //      ) {
      //        is (idx) {
      //          bgSubLineMemArr(idx).write(
      //            //address=wrBgPipeLast.getCntPxPosX()(
      //            //  log2Up(params.oneLineMemSize) - 1 downto 0
      //            //),
      //            address=tempWrBgPipeLineMemAddr,
      //            data=tempOutp.lineMemEntry,
      //          )
      //        }
      //      }
      //      //default {
      //      //  wrLineMemEntry := rPastWrLineMemEntry
      //      //}
      //    }
      //    tempWrBgPipeLineMemAddr := tempInp.getCntPxPosX()(
      //      log2Up(params.oneLineMemSize) - 1 downto 0
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).stage7 := (
      //    //  stageData.pipeIn(idx).stage7
      //    //)
      //  },
      //)
      //for (x <- 0 until params.bgTileSize2d.x) {
      //  tempWrBgPipeLineMemAddr(x) := wrBgPipeLast.getCntPxPosX()(
      //    log2Up(
      //      //params.oneLineMemSize
      //      params.oneLineMemSize
      //    ) - 1 downto 0
      //  )
      //}
      when (wrBgPipeLast.fire) {
        //val tempLineMemEntry = LineMemEntry()
        //val bgIdx = wrBgPipeLast.bgIdx
        def tempArrIdx = (
          wrBgPipeLast.lineMemEntry(0).getSubLineMemTempArrIdx()
        )

        switch (rWrLineMemArrIdx) {
          for (jdx <- 0 until (1 << rWrLineMemArrIdx.getWidth)) {
            is (jdx) {
              bgSubLineMemArr(jdx).write(
                address=tempArrIdx,
                data=wrBgPipeLast.lineMemEntry,
              )
            }
          }
        }
        // BEGIN: old, non-synthesizable code
        //for (x <- 0 to params.bgTileSize2d.x - 1) {
        //  //when (!wrBgPipeLast.bakCnt.msb) {
        //  //  dbgBgLineMemVec(
        //  //    //(
        //  //      rWrLineMemArrIdx
        //  //    //  + wrBgPipeLast.bgAttrs.scroll.y
        //  //    //)(rWrLineMemArrIdx.bitsRange)
        //  //  )(
        //  //    //wrBgPipeLast.pxPos(x).x
        //  //    //wrBgPipeLast.lineMemEntry(x).addr
        //  //    //(
        //  //      //wrBgPipeLast.pxPos(x).x
        //  //      //+ wrBgPipeLast.bgAttrs.scroll.x
        //  //      //- wrBgPipeLast.bgAttrs.scroll.x
        //  //    //)(wrBgPipeLast.pxPos(x).x.bitsRange)
        //  //    wrBgPipeLast.lineMemEntry(x).addr
        //  //  ) := (
        //  //    wrBgPipeLast.lineMemEntry(x)
        //  //  )
        //  //}

        //  def dbgTestWrBgPipeLast_tempArrIdx = (
        //    params.getBgSubLineMemTempArrIdx(
        //      //pxPosX=tempWrBgPipeLineMemAddr,

        //      //pxPosX=wrBgPipeLast.pxPos(x).x,
        //      //pxPosX=wrBgPipeLast.lineMemEntry(x).addr
        //      pxPosX=(
        //        //(
        //        //  wrBgPipeLast.pxPos(x).x
        //        //  //+ wrBgPipeLast.bgAttrs.scroll.x
        //        //  //- wrBgPipeLast.bgAttrs.scroll.x
        //        //)//(wrBgPipeLast.pxPos(x).x.bitsRange)
        //        wrBgPipeLast.lineMemEntry(x).addr
        //      ),
        //    )
        //  )
        //  val tempArrIdx = UInt(
        //    //log2Up(params.oneLineMemSize) bits
        //    //params.bgTempAddr
        //    dbgTestWrBgPipeLast_tempArrIdx.getWidth bits
        //  )
        //    .setName(f"dbgTestWrBgPipeLast_tempArrIdx_$x")
        //  tempArrIdx := dbgTestWrBgPipeLast_tempArrIdx

        //  def dbgTestWrBgPipeLast_tempAddr = (
        //    params.getBgSubLineMemTempAddr(
        //      //pxPosX=tempWrBgPipeLineMemAddr,
        //      //pxPosX=(
        //      //  wrBgPipeLast.pxPos(x).x
        //      //  //+ wrBgPipeLast.bgAttrs.scroll.x
        //      //  //- wrBgPipeLast.bgAttrs.scroll.x
        //      //)//(wrBgPipeLast.pxPos(x).x.bitsRange),
        //      ////pxPosX=wrBgPipeLast.lineMemEntry(x).addr
        //      pxPosX=(
        //        wrBgPipeLast.lineMemEntry(x).addr
        //      ),
        //    )
        //  )
        //  val tempAddr = UInt(
        //    //log2Up(params.oneLineMemSize) bits
        //    //params.bgTempAddr
        //    dbgTestWrBgPipeLast_tempAddr.getWidth bits
        //  )
        //    .setName(f"dbgTestWrBgPipeLast_tempAddr_$x")
        //  tempAddr := dbgTestWrBgPipeLast_tempAddr

        //  switch (
        //    rWrLineMemArrIdx
        //    //wrBgPipeLast.lineMemArrIdx
        //  ) {
        //    for (
        //      jdx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1
        //      //jdx <- 0 to (1 << wrBgPipeLast.lineMemArrIdx.getWidth) - 1
        //    ) {
        //      is (jdx) {
        //        //bgSubLineMemArr(jdx).write(
        //        //  //address=wrBgPipeLast.getCntPxPosX()(
        //        //  //  log2Up(params.oneLineMemSize) - 1 downto 0
        //        //  //),
        //        //  address=tempWrBgPipeLineMemAddr,
        //        //  data=wrBgPipeLast.lineMemEntry,
        //        //)
        //        switch (
        //          //params.getBgSubLineMemTempArrIdx(
        //          //  //pxPosX=wrBgPipeLast.pxPos.x
        //          //  //pxPosX=tempWrBgPipeLineMemAddr
        //          //  pxPosX=wrBgPipeLast.pxPos(x).x
        //          //)
        //          tempArrIdx
        //        ) {
        //          for (
        //            //kdx <- 0 to params.bgSubLineMemArrSize - 1
        //            kdx <- 0 to (1 << tempArrIdx.getWidth) - 1
        //          ) {
        //            is (kdx) {
        //              when (!wrBgPipeLast.bakCnt.msb) {
        //                bgSubLineMemA2d(jdx)(kdx).write(
        //                  //address=wrBgPipeLast.getCntPxPosX()(
        //                  //  log2Up(params.oneLineMemSize) - 1 downto 0
        //                  //),
        //                  //address=tempWrBgPipeLineMemAddr,

        //                  //address=params.getBgSubLineMemTempAddr(
        //                  //  //pxPosX=tempWrBgPipeLineMemAddr,
        //                  //  pxPosX=wrBgPipeLast.pxPos(x).x,
        //                  //),
        //                  address=tempAddr,
        //                  data=wrBgPipeLast.lineMemEntry(x),
        //                )
        //              }
        //            }
        //          }
        //        }
        //      }
        //    }
        //    //default {
        //    //  wrLineMemEntry := rPastWrLineMemEntry
        //    //}
        //  }
        //}
        // END: old, non-synthesizable code
      }
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
      val stageData = DualPipeStageData[Flow[WrObjPipePayload]](
        pipeIn=wrObjPipeIn,
        pipeOut=wrObjPipeOut,
        pipeNumMainStages=wrObjPipeNumMainStages,
        pipeStageIdx=0,
      )
      //val rStage5FwdVec = Vec.fill(wrObjPipeStage5NumFwd)(
      //  Reg(WrObjPipeStage5Fwd())
      //)
      //for (fwdIdx <- 0 to rStage5FwdVec.size - 1) {
      //  rStage5FwdVec(fwdIdx).setName(f"rWrObjPipeStage5FwdVec_$fwdIdx")
      //}
      val rStage6FwdVec = Vec.fill(params.objTileSize2d.x)(
        Vec.fill(wrObjPipeStage6NumFwd)(
          Reg(WrObjPipeStage6Fwd())
        )
      )
      for (x <- 0 to params.objTileSize2d.x - 1) {
        for (fwdIdx <- 0 to rStage6FwdVec(x).size - 1) {
          rStage6FwdVec(x)(fwdIdx).setName(
            f"rWrObjPipeStage6FwdVec_$fwdIdx" + f"_$x"
          )
        }
      }
      // BEGIN: stage 0
      HandleDualPipe(
        stageData=stageData.craft(0)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).cnt := (
          //  stageData.pipeIn(idx).cnt
          //)
          //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
          def tempInp = stageData.pipeIn(0).stage0
          def tempOutp = stageData.pipeOut(0).stage0
          tempOutp.justCopy := tempInp.justCopy
          //tempOutp.gridIdxLsb := stageData.pipeIn(0).calcGridIdxLsb()
          //tempOutp.innerObjAttrsMemIdx := tempInp.bakCnt(
          //  //bakCnt.high - 1
          //  //downto (bakCnt.high - 1 - params.objAttrsMemIdxWidth + 1)
          //  //bakCnt.high
          //  //downto (bakCnt.high - (params.objAttrsMemIdxWidth - 1))
          //  //(params.objAttrsMemIdxWidth + params.objTileSize2dPow.x - 1)
          //  //downto params.objTileSize2dPow.x

          //  params.objAttrsMemIdxWidth + params.objTileSize2dPow.x - 1
          //  downto params.objTileSize2dPow.x
          //)
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
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
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).cntMinus1 := (
      //      stageData.pipeIn(idx).cntMinus1
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
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
          for (x <- 0 to params.objTileSize2d.x - 1) {
            tempOutp.tilePxsCoord(x).x := (
              ////tempInp.getBakCntTilePxsCoordX()
              ////tempInp.getCntTilePxsCoordX()
              //Mux[UInt](
              //  tempInp.gridIdxLsb,
              //  {
              //    def width = tempOutp.tilePxsCoord(x).x.getWidth
              //    U(f"$width'd-1")
              //  },
              //  {
              //    def width = tempOutp.tilePxsCoord(x).x.getWidth
              //    U(f"$width'd0")
              //  },
              //)
              //&
              x
            )
            tempOutp.tilePxsCoord(x).y := (
              //rWrLineNum - tempInp.objAttrs.pos.y.asUInt
              (
                tempInp.lineNum.asSInt.resized - tempInp.objAttrs.pos.y
              ).asUInt
            )(
              tempOutp.tilePxsCoord(x).y.bitsRange
            )

            tempOutp.pxPos(x).x := (
              tempInp.objAttrs.pos.x.asUInt
              //tempInp.objAttrs.pos.x
              //+ tempInp.getBakCntTilePxsCoordX().asSInt.resized
              //+ tempInp.getBakCntTilePxsCoordX().resized
              //- 1
              //+ tempOutp.tilePxsCoord.x.asSInt
              + tempOutp.tilePxsCoord(x).x
              //- tempOutp.tilePxsCoord.x
            ).asSInt
            tempOutp.pxPos(x).y(tempInp.lineNum.bitsRange) := (
              //outp.bgPxsPosSlice.pos.y - tempInp.objAttrs.pos
              //rWrLineNum.asSInt.resized - tempInp.objAttrs.pos.y
              //tempInp.objAttrs.pos.y 
              //rWrLineNum.asSInt.resized
              //tempInp.lineNum.asSInt.resized - tempInp.objAttrs.pos.y
              tempInp.lineNum.asSInt
            )//(tempInp.lineNum.bitsRange)
            tempOutp.pxPos(x).y(
              //tempOutp.pxPos.y.high downto tempInp.lineNum.high
              tempOutp.pxPos(x).y.high downto tempInp.lineNum.getWidth
            ) := 0x0
          }

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
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
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
          //val dbgTestWrObjPipe3_pxPosXGridIdx = (
          //  Vec.fill(params.objTileSize2d.x)(
          //    UInt(
          //      (tempInp.pxPos(0).x.getWidth - params.objTileSize2dPow.x)
          //      bits
          //    )
          //  )
          //)

          //tempOutp.pxPosXGridIdxFindFirstSameAs :=
          val dbgPxPosXGridIdxFindFirstSameAs: (Bool, UInt) = (
            tempOutp.pxPosXGridIdx.sFindFirst(
              //condition=(
                //myBool => //(
                  _(0) === tempInp.calcGridIdxLsb()
                  //(
                  //  //Mux[UInt](
                  //  //  tempInp.stage0.gridIdxLsb === 1,
                  //  //  U("1'd1"),
                  //  //  U("1'd0"),
                  //  //)
                  //)
                //)
              //)
            )
          )
          tempOutp.stage3.pxPosXGridIdxFindFirstSameAsFound := (
            dbgPxPosXGridIdxFindFirstSameAs._1
          )
          tempOutp.stage3.pxPosXGridIdxFindFirstSameAsIdx := (
            dbgPxPosXGridIdxFindFirstSameAs._2.resized
          )
          val dbgPxPosXGridIdxFindFirstDiff: (Bool, UInt) = (
            tempOutp.pxPosXGridIdx.sFindFirst(
              //condition=(
                //myBool => //(
                  _(0) =/= tempInp.calcGridIdxLsb()
                  //(
                  //  //Mux[UInt](
                  //  //  tempInp.stage0.gridIdxLsb === 1,
                  //  //  U("1'd1"),
                  //  //  U("1'd0"),
                  //  //)
                  //)
                //)
              //)
            )
          )
          tempOutp.stage3.pxPosXGridIdxFindFirstDiffFound := (
            dbgPxPosXGridIdxFindFirstDiff._1
          )
          tempOutp.stage3.pxPosXGridIdxFindFirstDiffIdx := (
            dbgPxPosXGridIdxFindFirstDiff._2.resized
          )
          for (x <- 0 to params.objTileSize2d.x - 1) {
            tempOutp.pxPosXGridIdx(x) := tempInp.pxPos(x).x.asUInt(
              tempInp.pxPos(x).x.asUInt.high
              downto params.objTileSize2dPow.x
            )
            val dbgTestWrObjPipe3_pxPosXGridIdx = UInt(
              tempOutp.pxPosXGridIdx(x).getWidth bits
            )
              .setName(f"dbgTestWrObjPipe3_pxPosXGridIdx_$x")
            dbgTestWrObjPipe3_pxPosXGridIdx := tempOutp.pxPosXGridIdx(x)

            tempOutp.pxPosXGridIdxLsb(x) := (
              //tempOutp.pxPosXGridIdx(x)(0 downto 0)
              tempOutp.pxPosXGridIdx(x)(0)
            )
            tempOutp.pxPosRangeCheckGePipe1(x).x := (
              //tempInp.objAttrs.pos.x + params.objTileSize2d.x - 1 >= 0
              tempInp.pxPos(x).x >= 0
            )
            tempOutp.pxPosRangeCheckGePipe1(x).y := (
              ////outp.bgPxsPosSlice.pos.y.asSInt.resized
              ////>= tempInp.objAttrs.pos.y
              //tempInp.pxPos.y >= 0
              ////tempInp.pxPos.y >= tempInp.objAttrs.pos.y
              tempInp.pxPos(x).y >= tempInp.objAttrs.pos.y
            )

            tempOutp.pxPosRangeCheckLtPipe1(x).x := (
              tempInp.pxPos(x).x < params.intnlFbSize2d.x
            )
            tempOutp.pxPosRangeCheckLtPipe1(x).y := (
              ////tempInp.pxPos.y >= 0
              //tempInp.pxPos.y < params.intnlFbSize2d.y
              ////tempInp.pxPosMinusTileSize2d
              ////< params.intnlFbSize2d.y.resized
              //tempInp.pxPos.y <= 

              //tempInp.objPosYShift < params.intnlFbSize2d.y
              tempInp.pxPos(x).y < tempInp.objPosYShift
            )
            when (
              tempInp.tilePxsCoord(x).x < tempInp.objAttrs.size2d.x
              && tempInp.tilePxsCoord(x).y < tempInp.objAttrs.size2d.y
            ) {
              tempOutp.palEntryMemIdx(x) := tempInp.tile.getPx(
                tempInp.tilePxsCoord(x)
                //x
              )
            } otherwise {
              tempOutp.palEntryMemIdx(x) := 0
            }
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          for (x <- 0 to params.objTileSize2d.x - 1) {
            //tempOutp.pxPosXGridIdxFlip(x) := (
            //  //Mux[Bool](
            //  //  //tempInp.pxPosXGridIdxMatches(x)
            //  //)
            //  //tempInp.pxPosXGridIdxLsb(x)

            //  //tempInp.pxPosXGridIdxFindFirstSameAsIdx(x)
            //  //&&
            //  tempOutp.pxPosXGridIdxMatches(x)
            //)
            tempOutp.pxPosXGridIdxMatches(x) := (
              tempInp.pxPosXGridIdxLsb(x)
              === (
                //Mux[UInt](tempInp.gridIdxLsb(0), U"1'd1", U"1'd0")
                tempInp.calcGridIdxLsb()
              )
            )
            tempOutp.palEntryNzMemIdx(x) := tempInp.palEntryMemIdx(x) =/= 0

            tempOutp.pxPosRangeCheck(x).x := (
              //(tempInp.objAttrs.pos.x + params.objTileSize2d.x - 1 >= 0)
              //&& (tempInp.objAttrs.pos.x < params.intnlFbSize2d.x)
              tempInp.pxPosRangeCheckGePipe1(x).x
              && tempInp.pxPosRangeCheckLtPipe1(x).x
            )
            tempOutp.pxPosRangeCheck(x).y := (
              //(
              //  outp.bgPxsPosSlice.pos.y.asSInt.resized
              //  >= tempInp.objAttrs.pos.y
              //) && (
              //  outp.bgPxsPosSlice.pos.y.asSInt.resized
              //  < tempInp.objAttrs.pos.y + params.objTileSize2d.y
              //)
              tempInp.pxPosRangeCheckGePipe1(x).y
              && tempInp.pxPosRangeCheckLtPipe1(x).y
            )
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
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
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)
          //def tempObjArrElemIdx = params.getObjSubLineMemArrElemIdx(
          //  addr=combinePipeLast.cnt
          //)
          //val dbgTestWrObjPipe5_tempObjArrElemIdx = UInt(
          //  tempObjArrElemIdx.getWidth bits
          //)
          //  .setName("dbgTestWrObjPipe5_tempObjArrElemIdx")
          //tempOutp.rdLineMemEntry := tempOutp.rdLineMemEntry.getZero
          tempOutp.stage5.ext := tempOutp.stage5.ext.getZero

          for (x <- 0 to params.objTileSize2d.x - 1) {
            tempOutp.palEntry(x) := objPalEntryMem.readAsync(
              address=tempInp.palEntryMemIdx(x)
            )
            tempOutp.pxPosInLine(x) := (
              tempInp.pxPosRangeCheck(x).x && tempInp.pxPosRangeCheck(x).y
            )
            tempOutp.pxPosCmpForOverwrite(x) := (
              tempOutp.pxPosInLine(x)
              && tempInp.pxPosXGridIdxFindFirstSameAsFound
              && tempInp.pxPosXGridIdxMatches(x)
            )
          }

            //dbgTestCombinePipeLast_tempObjArrElemIdx := tempObjArrElemIdx
            //tempOutp.pxPosXChangingGridIdx(x) := (
            //  tempInp.pxPos(x).x.asUInt(
            //    params.objTileSize2dPow.x - 1 downto 0
            //  ) === 0
            //)
            //def fwdVec = tempOutp.stage5.fwdVec
            //def rFwdVec = rStage5FwdVec

            //for (fwdIdx <- 0 to rFwdVec.size - 1) {
            //  rFwdVec(fwdIdx).init(rFwdVec(fwdIdx).getZero)
            //  when (tempInp.fire) {
            //    rFwdVec(fwdIdx) := fwdVec(fwdIdx)
            //  }
            //  if (fwdIdx > 0) {
            //    fwdVec(fwdIdx) := rFwdVec(fwdIdx - 1)
            //  }
            //}
            ////val tempConcat = Bits(fwdVec.size bits)
            //for (fwdIdx <- 0 to fwdVec.size - 1) {
            //  if (fwdIdx == 0) {
            //    //fwdVec(fwdIdx).pxPosXIsSame := (
            //    //  tempInp.pxPos.x === tempInp.objAttrs.pos
            //    //)
            //    //fwdVec(fwdIdx).pxPosX := tempInp.pxPos.x
            //    fwdVec(fwdIdx).pxPos := tempInp.pxPos
            //  } else {
            //    //fwdVec(fwdIdx) := fwdVec(fwdIdx - 1)
            //    tempOutp.pxPosConcat.x(fwdIdx - 1) := (
            //      //tempInp.pxPos.x === fwdVec(fwdIdx).pxPosX
            //      tempInp.pxPos.x === fwdVec(fwdIdx).pxPos.x
            //    )
            //    tempOutp.pxPosConcat.y(fwdIdx - 1) := (
            //      //tempInp.pxPos.x === fwdVec(fwdIdx).pxPosX
            //      tempInp.pxPos.y === fwdVec(fwdIdx).pxPos.y
            //    )
            //  }
            //}
            //val rPastRdLineMemEntry = Reg(cloneOf(tempOutp.rdLineMemEntry))
            //rPastRdLineMemEntry.init(rPastRdLineMemEntry.getZero)
            //rPastRdLineMemEntry := tempOutp.rdLineMemEntry
            //when (tempOutp.fire) {
              //val tempArrIdx = UInt(
              //  params.objSubLineMemArrSizePow bits
              //)
              //  .setName(f"wrObjPipeStage5_tempObjSubLineMemArrAddr_$x")
              //tempArrIdx := params.getObjSubLineMemTempArrIdx(
              //  pxPosX=tempInp.pxPos(x).x.asUInt
              //)
              //val tempAddr = UInt(
              //  //log2Up(params.objSubLineMemSize)
              //  //- params.objSubLineMemArrSizePow
              //  params.objSubLineMemSizePow bits
              //)
              //  .setName(f"wrObjPipeStage5_tempAddr_$x")
              //tempAddr := params.getObjSubLineMemTempAddr(
              //  pxPosX=tempInp.pxPos(x).x.asUInt
              //)
              //def tempArr
              // BEGIN: debug comment this out; later
              //val dbgTestWrObjPipe5_myDbgTempObjArrIdx = UInt(
              //)
              //  .setName("dbgTestWrObjPipe5_myDbgTempObjArrIdx")
              switch (
                rWrLineMemArrIdx
                //tempInp.lineMemArrIdx
              ) {
                for (
                  jdx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1
                  //jdx <- 0 to (1 << tempInp.lineMemArrIdx.getWidth) - 1
                ) {
                  is (jdx) {
                    //def tempObjArrIdx = params.getObjSubLineMemArrIdx(
                    //  //addr=combinePipeLast.cnt
                    //  //addr=tempInp.pxPos(x).x.asUInt
                    //  //addr=Cat(
                    //  //  wrObjPipeLast.pxPos(0).x.asUInt(
                    //  //    wrObjPipeLast.pxPos(0).x.high downto 1
                    //  //  ),
                    //  //  wrObjPipeLast.gridIdxLsb
                    //  //).asUInt
                    //  //addr=tempInp.getObjSubLineMemArrIdxAddr(),
                    //  addr=Cat(
                    //    wrObjPipeLast.pxPos(0).x.asUInt
                    //  ).asUInt
                    //)
                    //val dbgTestWrObjPipe5_Testificate = UInt(
                    //)
                    def tempObjArrIdx = tempInp.getObjSubLineMemArrIdx(
                      x=tempInp.pxPosXGridIdxFindFirstSameAsIdx
                    )
                    //def tempObjArrIdx = tempInp.pxPosXGridIdx(0).x
                    //def tempObjArrIdx = tempInp.pxPosXGridIdx(0)

                    //def tempObjArrIdx = (
                    //  tempOutp.pxPosXGridIdx(
                    //    tempInp.pxPosXGridIdxFindFirstSameAsIdx
                    //  )(
                    //    tempOutp.pxPosXGridIdx(0).bitsRange
                    //  )
                    //)

                    val dbgTestWrObjPipe5_tempObjArrIdx = UInt(
                      tempObjArrIdx.getWidth bits
                    )
                      .setName(
                        f"dbgTestWrObjPipe5_tempObjArrIdx_$jdx"
                        //+ f"_$x"
                      )
                    dbgTestWrObjPipe5_tempObjArrIdx := tempObjArrIdx

                    //def tempObjArrElemIdx = (
                    //  params.getObjSubLineMemArrElemIdx(
                    //    //addr=combinePipeLast.cnt
                    //    addr=tempInp.pxPos(x).x.asUInt
                    //  )
                    //)
                    //val dbgTestWrObjPipe5_tempObjArrElemIdx = UInt(
                    //  tempObjArrElemIdx.getWidth bits
                    //)
                    //  .setName(
                    //    f"dbgTestWrObjPipe5_tempObjArrElemIdx_$jdx"
                    //    + f"_$x"
                    //  )
                    //dbgTestWrObjPipe5_tempObjArrElemIdx := (
                    //  tempObjArrElemIdx
                    //)

                    //when (tempInp.pxPosXGridIdxFindFirstSameAsFound) {
                      //tempOutp.rdLineMemEntry := (
                      //  objSubLineMemArr(jdx).readAsync(
                      //    address=tempObjArrIdx
                      //    //(
                      //    //  tempInp.getObjSubLineMemArrIdx_tempRange()
                      //    //)
                      //  )
                      //)
                      rWrObjPipeOut5Ext.rdLineMemEntry := (
                        objSubLineMemArr(jdx).readSync(
                          address=tempObjArrIdx
                        )
                      )
                    //} otherwise {
                    //  for (x <- 0 until tempOutp.rdLineMemEntry.size) {
                    //    tempOutp.rdLineMemEntry(x).col.a := False 
                    //    tempOutp.rdLineMemEntry(x).col.rgb.r := (
                    //      default -> True
                    //    )
                    //    tempOutp.rdLineMemEntry(x).col.rgb.g := (
                    //      default -> True
                    //    )
                    //    tempOutp.rdLineMemEntry(x).col.rgb.b := (
                    //      default -> True
                    //    )
                    //    tempOutp.rdLineMemEntry(x).written := False
                    //    tempOutp.rdLineMemEntry(x).addr := (
                    //      default -> True
                    //    )
                    //    tempOutp.rdLineMemEntry(x).prio := 0
                    //  }
                    //}

                    //when (tempInp.pxPosXGridIdxMatches(x)) {
                    //  //tempOutp.rdLineMemEntry := (
                    //  //  objSubLineMemArr(jdx).readAsync(
                    //  //    address=tempInp
                    //  //  )
                    //  //)
                    //  tempOutp.rdLineMemEntry(x) := (
                    //    objSubLineMemArr(jdx).readAsync(
                    //      address=tempObjArrIdx
                    //    )(tempObjArrElemIdx)
                    //  )
                    //} otherwise {
                    //  tempOutp.rdLineMemEntry(x) := (
                    //    //tempOutp.rdLineMemEntry(x).getZero
                    //    objSubLineMemArr(jdx).readAsync(
                    //      address=Cat(
                    //        tempObjArrIdx(tempObjArrIdx.high downto 1),
                    //        tempInp.gridIdxLsb(0 downto 0),
                    //      ).asUInt
                    //    )(tempObjArrElemIdx)
                    //  )
                    //}
                    //tempOutp.rdLineMemEntry := (
                    //  objSubLineMemArr(jdx).readAsync(
                    //    //address=tempInp.pxPos.x.asUInt.resized,
                    //    address=tempInp.pxPos.x.asUInt(
                    //      log2Up(params.oneLineMemSize) - 1 downto 0
                    //    ),
                    //    //readUnderWrite=writeFirst,
                    //    //readUnderWrite=writeFirst,
                    //  )
                    //)
                    //tempAddr := tempInp.pxPos.x.asUInt
                    //def tempSize = objSubLineMemA2d(jdx).size
                    //println(
                    //  f"jdx, objSubLineMemA2d(jdx).size: $jdx, $tempSize"
                    //)
                    //switch (tempArrIdx) {
                    //  for (
                    //    //kdx <- 0
                    //    //to (1 << params.objSubLineMemArrSizePow) - 1
                    //    //kdx <- 0 to objSubLineMemA2d(jdx).size - 1
                    //    kdx <- 0 to (1 << tempArrIdx.getWidth) - 1
                    //  ) {
                    //    is (kdx) {
                    //      //val tempAddr = tempInp.pxPos.x.asUInt(
                    //      //  log2Up(params.objSubLineMemSize) - 1
                    //      //  downto params.objTileSize2dPow.x
                    //      //)

                    //      // BEGIN: later
                    //      tempOutp.rdLineMemEntry(x) := (
                    //        objSubLineMemA2d(jdx)(kdx).readAsync(
                    //          address=tempAddr
                    //        )
                    //      )
                    //      //tempOutp.dbgRdLineMemEntry := (
                    //      //  objSubLineMemA2d(jdx)(kdx).readAsync(
                    //      //    address=tempAddr
                    //      //  )
                    //      //)
                    //      //tempOutp.rdLineMemEntry := (
                    //      //  objSubLineMemA2d(jdx)(kdx).readAsync(
                    //      //    address=tempAddr,
                    //      //    //readUnderWrite=writeFirst,
                    //      //  )
                    //      //)
                    //      // END: later
                    //    }
                    //  }
                    //}
                  }
                }
              }
              // END: debug comment this out; later
              // BEGIN: debug
              //tempOutp.rdLineMemEntry := (
              //  tempOutp.rdLineMemEntry.getZero
              //)
              // END: debug
            //} otherwise {
            //  tempOutp.rdLineMemEntry := rPastRdLineMemEntry
            //}
          //}
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).stage5 := stageData.pipeIn(idx).stage5
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          if (idx == 6) {
            when (tempInp.fire) {
              tempOutp.stage5.nonExt := tempInp.stage5.nonExt
              tempOutp.stage5.ext := rWrObjPipeOut5Ext
            } otherwise {
              tempOutp.stage5 := RegNext(tempOutp.stage5)
              //tempOutp.stage5 := tempInp.stage5
            }
          } else {
            tempOutp.stage5 := tempInp.stage5
          }
        },
      )
      // END: Stage 5

      // BEGIN: Stage 6
      //HandleDualPipe(
      //  stageData=stageData.craft(6)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    //switch (rWrLineMemArrIdx) {
      //    //  for (jdx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1) {
      //    //    is (jdx) {
      //    //      
      //    //    }
      //    //  }
      //    //}

      //    //when (tempInp.rdLineMemEntry.col.a) {
      //    //when (tempInp.rdLineMemEntry.prio === 0) {
      //    //}
      //    //for (x <- 0 to params.objTileSize2d.x - 1) {
      //      //switch (
      //      //  rWrLineMemArrIdx
      //      //  //tempInp.lineMemArrIdx
      //      //) {
      //      //  for (
      //      //    jdx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1
      //      //    //jdx <- 0 to (1 << tempInp.lineMemArrIdx.getWidth) - 1
      //      //  ) {
      //      //    is (jdx) {
      //      //      tempOutp.rdLineMemEntry := (
      //      //        objSubLineMemArr(jdx).readAsync(
      //      //          //address=tempInp.pxPos.x.asUInt.resized,
      //      //          address=tempInp.pxPos.x.asUInt(
      //      //            log2Up(params.oneLineMemSize) - 1 downto 0
      //      //          ),
      //      //          readUnderWrite=writeFirst,
      //      //          //readUnderWrite=writeFirst,
      //      //        )
      //      //      )
      //      //    }
      //      //  }
      //      //}
      //      //tempOutp.pxPosXIsSame := tempConcat =/= 0

      //      //tempOutp.pxPosIsSame.x := (
      //      //  //tempInp.pxPosXConcat(tempInp.pxPosXConcat.high downto 1)
      //      //  tempInp.pxPosConcat.x =/= 0
      //      //)
      //      //tempOutp.pxPosIsSame.y := (
      //      //  //tempInp.pxPosXConcat(tempInp.pxPosXConcat.high downto 1)
      //      //  tempInp.pxPosConcat.y =/= 0
      //      //)
      //      when (tempInp.pxPosInLine) {
      //        // BEGIN: debug comment this out
      //        when (
      //          tempInp.rdLineMemEntry.prio < tempInp.objAttrs.prio
      //          //tempOutp.rdLineMemEntry.prio < tempInp.objAttrs.prio
      //        ) {
      //          tempOutp.overwriteLineMemEntry := True
      //        } otherwise {
      //          tempOutp.overwriteLineMemEntry := (
      //            !tempInp.rdLineMemEntry.col.a
      //            //!tempOutp.rdLineMemEntry.col.a
      //          )
      //        }
      //        // END: debug comment this out
      //        //tempOutp.overwriteLineMemEntry := True
      //      } otherwise {
      //        tempOutp.overwriteLineMemEntry := False
      //      }
      //    //}
      //    
      //    //tempOutp.overwriteLineMemEntry := (
      //    //  //!tempInp.rdLineMemEntry.prio.msb
      //    //  //tempInp.rdLineMemEntry.written
      //    //  //(
      //    //  //  !tempInp.rdLineMemEntry.col.a
      //    //  //  //tempInp.objAttrs.prio
      //    //  //  //|| (tempInp.rdLineMemEntry.prio < tempInp.objAttrs.prio)

      //    //  //  //&& tempInp.objAttrs.prio <= tempInp.rdLineMemEntry.prio
      //    //  //  //&& tempInp.pxPosInLine
      //    //  //)
      //    //  (
      //    //    tempInp.rdLineMemEntry.col.a
      //    //  )
      //    //  //|| (tempInp.pxPos.x < 0)
      //    //  //|| (tempInp.pxPos.x >= params.oneLineMemSize)
      //    //)
      //    //tempOutp.overwriteLineMemEntry := True
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).stage6 := stageData.pipeIn(idx).stage6
      //  },
      //)
      // END: Stage 6

      // BEGIN: Stage 6
      HandleDualPipe(
        stageData=stageData.craft(6)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)
          tempOutp.stage6.ext := tempOutp.stage6.ext.getZero

          def myMainFunc(
            x: Int
            //myIdx: UInt
            //myIdx: Int
          ): Unit = {
            //--------
            // BEGIN: debug comment this out; later
            //val myIdx = UInt((params.objTileSize2dPow.x + 1) bits)
            //  .setName(f"wrObjPipe6_myIdx_$x")
            val myIdxFull = cloneOf(tempInp.pxPos(x).x)
              .setName(f"wrObjPipe6_myIdxFull_$x")
            myIdxFull := tempInp.pxPos(x).x
            val myIdx = UInt(params.objTileSize2dPow.x bits)
              .setName(f"wrObjPipe6_myIdx_$x")
            myIdx := myIdxFull.asUInt(myIdx.bitsRange)
            // END: debug comment this out; later
            //--------
            //val x = UInt(params.objTileSize2dPow.x bits)
            ////x := myOtherIdx
            //x := 0

            //val rotatedOverwriteLineMemEntry = Vec(
            //  tempOutp.overwriteLineMemEntry.drop(x).appendedAll(
            //    tempOutp.overwriteLineMemEntry.take(x)
            //  )
            //)//.addTag(noLatchCheck)
            val tempOverwriteLineMemEntry = (
              //rotatedOverwriteLineMemEntry(
              //  //myIdx(tempMyIdxRange)
              //  myIdx
              //)
              //tempOutp.overwriteLineMemEntry(
              //  //x
              //  myIdx
              //)
              //tempOutp.overwriteLineMemEntry(myIdx)
              Bool()
            )
              .setName(f"wrObjPipe6_tempOverwriteLineMemEntry_$x")
            def outOverwriteLineMemEntry = (
              rWrObjPipeOut6Ext.overwriteLineMemEntry(
                myIdx
                //x
              )
            )
            //val rotatedWrLineMemEntry = Vec(
            //  //tempOutp.wrLineMemEntry(x)
            //  //tempOutp.wrLineMemEntry(myIdx)
            //  tempOutp.wrLineMemEntry.drop(x).appendedAll(
            //    tempOutp.wrLineMemEntry.take(x)
            //  )
            //)//.addTag(noLatchCheck)
            val tempWrLineMemEntry = (
              cloneOf(rWrObjPipeOut6Ext.wrLineMemEntry(myIdx))
            )
            def outWrLineMemEntry = (
              //rotatedWrLineMemEntry(
              //  //myIdx(tempMyIdxRange)
              //  myIdx
              //)
              rWrObjPipeOut6Ext.wrLineMemEntry(
                // `myIdx` should be used here because `x` is not an index
                // into an `ObjSubLineMemEntry`
                myIdx
              )
              //tempOutp.wrLineMemEntry(
              //  //x
              //  myIdx
              //)
              //tempOutp.wrLineMemEntry(myIdx)
            )
            //def toFwdWrLineMemEntry = (
            //  rWrObjPipeOut6Ext.wrLineMemEntry(x)
            //)

            //val tempOverwriteLineMemEntry = Bool()
            //val tempConcat = Bits(tempInp.numFwd + 1 bits)
            //--------
            // BEGIN: debug comment this out; later
            def fwdVec = tempOutp.stage6.fwdVec(
              x
              //myIdx
            )
            //val rFwdVec = Reg(cloneOf(fwdVec)) //init(fwdVec.getZero)
            def rFwdVec = rStage6FwdVec(
              x
              //myIdx
            )
            //rFwdVec(x).init(rFwdVec(x).getZero)
            for (fwdIdx <- 0 until rFwdVec.size) {
              rStage6FwdVec(x)(fwdIdx).init(
                rStage6FwdVec(x)(fwdIdx).getZero
              )
            }

            for (fwdIdx <- 0 to rFwdVec.size - 1) {
              //rFwdVec(fwdIdx).init(rFwdVec(fwdIdx).getZero)
              when (
                //tempOutp.fire
                tempInp.fire
              ) {
                rFwdVec(fwdIdx) := fwdVec(fwdIdx)
              }
              //rFwdVec(fwdIdx) := fwdVec(fwdIdx)
              if (fwdIdx > 0) {
                fwdVec(fwdIdx) := rFwdVec(fwdIdx - 1)
              }
            }
            // END: debug comment this out; later
            //val dbgTestWrObjPipeOut6_sameAsFound = cloneOf(
            //  tempInp.pxPosXGridIdxFindFirstSameAsFound
            //)
            //  .setName(f"dbgTestWrObjPipeOut6_sameAsFound_$x")
            //dbgTestWrObjPipeOut6_sameAsFound := (
            //  tempInp.pxPosXGridIdxFindFirstSameAsFound
            //)
            ////--------
            def calcTempOverwiteLineMemEntry(
              somePxPosCmp: Bool,
              someLineMemEntry: ObjSubLineMemEntry,
              someOverwriteLineMemEntry: Bool,
            ): Unit = {
              //val tempLineMemEntryPrio = (
              //  //someLineMemEntry.prio(tempInp.objAttrs.prio.bitsRange)
              //  //someLineMemEntry.rawPrio
              //  someLineMemEntry.prio
              //)

              //val input = tempInp.postStage0.stage4.pxPosXGridIdxMatches
              ////val rotatedGridIdxMatches = Vec(
              ////  //tempInp.pxPosXGridIdxMatches.toList.drop(x).appendedAll(
              ////  //  tempInp.pxPosXGridIdxMatches.toList.take(x)
              ////  //)
              ////  input.toList.drop(x).appendedAll(input.toList.take(x))
              ////)
              //val rotatedGridIdxMatches = Vec(
              //  input.drop(x).appendedAll(input.take(x))
              //)//.addTag(noLatchCheck)
              when (
                //--------
                // BEGIN: move this to prior pipeline stage; later
                somePxPosCmp
                // END: move this to prior pipeline stage; later
                //--------
                // BEGIN: debug comment this out; later
                //&& myIdx.msb
                // END: debug comment this out; later
                //--------
                //&& tempInp.pxPosXGridIdxMatches(x)
                //&& tempInp.
                //&& (
                //  tempInp.pxPos(x).x.asUInt(
                //    //tempWrLineMemEntry.addr.bitsRange
                //    tempInp.pxPos(x).x.asUInt.high
                //    downto 0
                //  )
                //  === 3
                //)
                //&& rotatedGridIdxMatches(myIdx)
              ) {
                // BEGIN: debug comment this out
                when (
                  //!someLineMemEntry.rawPrio.msb
                  !someLineMemEntry.written
                ) {
                  //dbgTestificate := 0
                  someOverwriteLineMemEntry := True
                } otherwise {
                  when (
                    //tempInp.rdLineMemEntry.prio < tempInp.objAttrs.prio
                    //tempOutp.rdLineMemEntry.prio < tempInp.objAttrs.prio
                    someLineMemEntry.prio < tempInp.objAttrs.prio
                    //tempLineMemEntryPrio < tempInp.objAttrs.prio
                  ) {
                    someOverwriteLineMemEntry := False
                  } elsewhen (
                    //tempLineMemEntryPrio === tempInp.objAttrs.prio
                    someLineMemEntry.prio === tempInp.objAttrs.prio
                  ) {
                    someOverwriteLineMemEntry := True
                    //someOverwriteLineMemEntry := (
                    //  !someLineMemEntry.col.a
                    //  && tempInp.palEntryNzMemIdx(
                    //    x
                    //    //myIdx
                    //    //myIdx(tempMyIdxRange)
                    //  )
                    //)
                  } otherwise {
                    //dbgTestificate := 3
                    //someOverwriteLineMemEntry := True
                    someOverwriteLineMemEntry := (
                      !someLineMemEntry.col.a
                      && tempInp.palEntryNzMemIdx(
                        x
                        //myIdx
                        //myIdx(tempMyIdxRange)
                      )
                    )
                  }
                }
              } otherwise {
                // END: debug comment this out
                //tempOutp.overwriteLineMemEntry := True } otherwise {
                someOverwriteLineMemEntry := False
              }
            }
            //--------
            val tempRdLineMemEntry = ObjSubLineMemEntry()
              .setName(f"wrObjPipeStage6_tempRdLineMemEntry_$x")
            //--------
            // BEGIN: debug comment this out; later
            //val tempConcat = Bits(fwdVec.size bits)
            val tempConcat = Bits((fwdVec.size - 1) bits)
              .setName(f"wrObjPipeStage6TempConcat_$x")

            for (fwdIdx <- 0 to fwdVec.size - 1) {
              if (fwdIdx == 0) {
                when (
                  !tempInp.bakCnt.msb
                  //&& tempInp.pxPosXGridIdxMatches(x)
                ){
                  //fwdVec(fwdIdx).pxPos := tempInp.pxPos(
                  //  x
                  //  //myIdx
                  //  //myIdx(tempMyIdxRange)
                  //)
                  fwdVec(fwdIdx).pxPosY := (
                    tempInp.pxPos(
                      //tempInp.pxPosXGridIdxFindFirstSameAsIdx
                      0
                    ).y
                  )
                  fwdVec(fwdIdx).pxPosXGridIdx := (
                    tempInp.pxPosXGridIdx(
                      //myIdx
                      tempInp.pxPosXGridIdxFindFirstSameAsIdx
                    )
                  )

                  //fwdVec(fwdIdx).overwriteLineMemEntry := (
                  //  tempOutp.overwriteLineMemEntry
                  //)
                  //fwdVec(fwdIdx).wrLineMemEntry := tempOutp.wrLineMemEntry
                  //fwdVec(fwdIdx).wrLineMemEntry := tempOutp.wrLineMemEntry
                  //fwdVec(fwdIdx).wrLineMemEntry := tempWrLineMemEntry
                  fwdVec(fwdIdx).objAttrsMemIdx := (
                    tempInp.objAttrsMemIdx()
                  )
                  fwdVec(fwdIdx).overwriteLineMemEntry := (
                    //tempOutp.overwriteLineMemEntry(x)
                    tempOverwriteLineMemEntry
                  )
                  //fwdVec(fwdIdx).wrLineMemEntry := tempWrLineMemEntry
                  fwdVec(fwdIdx).wrLineMemEntry := (
                    //tempOutp.wrLineMemEntry(x)
                    tempWrLineMemEntry
                    //toFwdWrLineMemEntry
                  )
                  //fwdVec(fwdIdx).prio := tempInp.objAttrs.prio
                  //fwdVec(fwdIdx).wrLineMemEntry := tempOutp.wrLineMemEntry
                } otherwise {
                  fwdVec(fwdIdx) := fwdVec(fwdIdx).getZero
                }
              }
              //else {
              //  //tempOutp.fwdVec(fwdIdx).pxPosX := tempOutp
              //  fwdVec(fwdIdx) := rFwdVec(fwdIdx - 1)
              //}
              else {
                //def tempPrio = (
                //  //fwdVec(fwdIdx).wrLineMemEntry.prio(
                //  //  tempInp.objAttrs.prio.bitsRange
                //  //)
                //  fwdVec(fwdIdx).wrLineMemEntry.prio
                //)
                //val fwdCheckOverwriteLineMemEntry = Bool()
                //  .setName(
                //    f"fwdCheckOverwriteLineMemEntry_$x" + f"_$fwdIdx"
                //  )
                //calcTempOverwiteLineMemEntry(
                //  somePxPosCmp=fwdVec(fwdIdx).overwriteLineMemEntry,
                //  someLineMemEntry=fwdVec(fwdIdx).wrLineMemEntry,
                //  someOverwriteLineMemEntry=fwdCheckOverwriteLineMemEntry,
                //)
                //fwdCheckOverwriteLineMemEntry := (
                //  fwdVec(fwdIdx).overwriteLineMemEntry
                //  && fwdVec(fwdIdx).wrLineMemEntry.prio
                //)
                tempConcat(fwdIdx - 1) := (
                  //fwdVec(fwdIdx).pxPos === tempInp.pxPos(
                  //  x
                  //  //myIdx
                  //  //myIdx(tempMyIdxRange)
                  //)
                  fwdVec(fwdIdx).pxPosY === tempInp.pxPos(0).y
                  && (
                    fwdVec(fwdIdx).pxPosXGridIdx
                    === (
                      tempInp.pxPosXGridIdx(
                        tempInp.pxPosXGridIdxFindFirstSameAsIdx
                      )
                    )
                  ) && fwdVec(fwdIdx).overwriteLineMemEntry
                  //&& fwdCheckOverwriteLineMemEntry
                  //&& !tempOverwriteLineMemEntry
                  //&& fwdVec
                )
              }
            }

            switch (
              tempConcat
              //(tempConcat.high downto 1)
            ) {
              for (
                //fwdIdx <- 0 to fwdVec.size - 1
                fwdIdx <- 0 to fwdVec.size - 2
                //fwdIdx <- 1 to fwdVec.size - 1
              ) {
                // We want to forward the most recent results
                //def myMask = M(
                //  ("0" * (tempInp.numFwd - fwdIdx))
                //  + "1"
                //  + ("-" * fwdIdx)
                //)
                def careAbout = (
                  ((1 << ((fwdVec.size - 1) - fwdIdx)) - 1) << fwdIdx
                )
                //def careAbout = (-1) << fwdIdx
                def value = (
                  1 << fwdIdx
                )
                //println(myCase)
                val tempCase = new MaskedLiteral(
                  //myCase
                  //value=tempConcat,
                  //value=(1 << tempConcat.getWidth) - 1,
                  value=value,
                  careAbout=careAbout,
                  width=tempConcat.getWidth,
                  //width=tempConcat.getWidth - 1,
                )
                //println(tempCase)
                is (tempCase) {
                  tempRdLineMemEntry := fwdVec(fwdIdx + 1).wrLineMemEntry
                }
              }
              default 
              //.otherwise 
              {
                tempRdLineMemEntry := tempInp.rdLineMemEntry(
                  //x
                  // `myIdx` should be used here since it's an index into
                  // `objSubLineMemArr(jdx)`
                  myIdx
                )
              }
            }
            // END: debug comment this out; later
            //--------
            //tempRdLineMemEntry := tempInp.rdLineMemEntry(
            //  //x
            //  myIdx
            //)

            //tempRdLineMemEntry := tempInp.rdLineMemEntry

            //val tempX = UInt(params.objTileSize2dPow.x bits)
            //  .setName(f"wrObjPipe6_tempX_$x")
            //tempX := (tempInp.pxPos(0).x.asUInt + x)(tempX.bitsRange)
            //val rotatedGridIdxMatches = (
            //  input.drop(x).appendedAll(input.take(x))
            //)
            outOverwriteLineMemEntry := tempOverwriteLineMemEntry
            outWrLineMemEntry := tempWrLineMemEntry
            //when (
            //  tempOutp.fire
            //  //tempInp.fire
            //) {
              //--------
              //--------
              // BEGIN: debug comment this out; later
              when (tempOverwriteLineMemEntry) {
              // END: debug comment this out; later
              //--------
                // Here it should be `x` (not `myIdx`) here because `myIdx`
                // is just an index into `ObjSubLineMemEntry`s, rather than
                // an index into sprite tiles themselves
                tempWrLineMemEntry.addr := (
                  tempInp.pxPos(
                    x
                    //myIdx
                    //myIdx(tempMyIdxRange)
                  ).x.asUInt(
                    tempWrLineMemEntry.addr.bitsRange
                  )
                  //default -> False
                )
                tempWrLineMemEntry.col.rgb := (
                  tempInp.palEntry(
                    x
                    //myIdx
                    //myIdx(tempMyIdxRange)
                  ).col
                  //tempWrLineMemEntry.col.rgb.getZero
                )
                //tempOutp.wrLineMemEntry.col.a := True
                tempWrLineMemEntry.col.a := (
                  tempInp.palEntryNzMemIdx(
                    x
                    //myIdx
                    //myIdx(tempMyIdxRange)
                  )
                  //False
                )
                //tempOutp.wrLineMemEntry.prio(
                //  tempInp.objAttrs.prio.bitsRange
                //) := tempInp.objAttrs.prio
                tempWrLineMemEntry.prio := (
                  tempInp.objAttrs.prio
                )
                //tempOutp.wrLineMemEntry.prio.msb := True
                tempWrLineMemEntry.written := True
              //--------
              // BEGIN: debug comment this out; later
              } otherwise {
                //tempOutp.wrLineMemEntry := tempInp.rdLineMemEntry
                tempWrLineMemEntry := tempRdLineMemEntry
              }
              // END: debug comment this out; later
              //--------
            //} otherwise {
            //  //tempWrLineMemEntry := (
            //  //  RegNext(tempWrLineMemEntry)
            //  //)
            //  tempWrLineMemEntry := (
            //    outWrLineMemEntry
            //  )
            //  //rotatedWrLineMemEntry := rotatedWrLineMemEntry.getZero
            //}
            calcTempOverwiteLineMemEntry(
              somePxPosCmp=(
                //tempInp.pxPosInLine(
                //  // this should be `x` because it's an index into the 
                //  x
                //)
                tempInp.pxPosCmpForOverwrite(
                  // this should be `x` because it's an index from sprite's
                  // perspective
                  x
                )
                //&& (
                //  tempRdLineMemEntry.addr === (
                //    tempInp.pxPos.x.asUInt(
                //      tempRdLineMemEntry.addr.bitsRange
                //    )
                //  )
                //)
              ),
              //someLineMemEntry=tempInp.rdLineMemEntry,
              someLineMemEntry=tempRdLineMemEntry,
              someOverwriteLineMemEntry=(
                //tempOutp.overwriteLineMemEntry(x)
                tempOverwriteLineMemEntry
              )
            )
          }

          for (x <- 0 until params.objTileSize2d.x) {
            myMainFunc(x=x)
          }
          //myMainFunc()
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).stage6 := stageData.pipeIn(idx).stage6
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          if (idx == 7) {
            when (tempInp.fire) {
              tempOutp.stage6.nonExt := tempInp.stage6.nonExt
              tempOutp.stage6.ext := rWrObjPipeOut6Ext
            } otherwise {
              tempOutp.stage6 := RegNext(tempOutp.stage6)
              //tempOutp.stage6 := tempInp.stage6
            }
          } else {
            tempOutp.stage6 := tempInp.stage6
          }
        },
      )
      // END: Stage 6
      //--------
      //// BEGIN: Stage 7
      //HandleDualPipe(
      //  stageData=stageData.craft(7)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    //when (tempOutp.fire) {
      //    //tempOutp.stage7.ext := rWrObjPipeOut6Ext
      //    //} otherwise {
      //    //  tempOutp.stage7
      //    //}
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).stage7 := stageData.pipeIn(idx).stage7
      //  },
      //)
      //// END: Stage 7
      //--------

      //val objLineMem = objSubLineMemArr(someWrLineMemArrIdx)
      when (wrObjPipeLast.fire) {
        //val tempLineMemEntry = LineMemEntry()
        //val objIdx = wrObjPipeLast.objIdx

        // BEGIN: come back to this later

        //val tempObjSubLineMemArrIdx = UInt(
        //  params.objSubLineMemArrSizePow bits
        //)
        //  .setName("wrObjPipeLast_tempArrIdx")
        //tempObjSubLineMemArrIdx := params.getObjSubLineMemTempArrIdx(
        //  pxPosX=wrObjPipeLast.pxPos.x.asUInt
        //)
        //val tempAddr = UInt(
        //  //log2Up(params.oneLineMemSize) bits
        //  //log2Up(params.objSubLineMemSize) bits
        //  params.objSubLineMemSizePow bits
        //)
        //  .setName("wrObjPipeLast_tempAddr")

        //tempAddr := wrObjPipeLast.pxPos.x.asUInt(
        //  //log2Up(params.oneLineMemSize) - 1 downto 0
        //  //tempAddr.bitsRange
        //  //params.objSubLineMemSizePow
        //  //log2Up(params.objSubLineMemSizePow
        //)
        //tempAddr := params.getObjSubLineMemTempAddr(
        //  pxPosX=wrObjPipeLast.pxPos.x.asUInt
        //)
        // END: come back to this later
        //val tempSubLineMemEntry = Vec.fill(params.objTileSize2d.x)(
        //  ObjSubLineMemEntry()
        //)
        //for (x <- 0 to params.objTileSize2d.x - 1) {
          //def tempWrLineMemEntry = wrObjPipeLast.wrLineMemEntry(x)
          //val dbgTestWrObjPipeLast = cloneOf(tempWrLineMemEntry)
          //  .setName(f"dbgTestWrObjPipeLast_$x")
          //dbgTestWrObjPipeLast := tempWrLineMemEntry
          //def tempArrIdxWidth = (
          //  tempWrLineMemEntry.getSubLineMemTempArrIdx().getWidth
          //)
          //val dbgTestWrObjPipeLastTempArrIdx = UInt(tempArrIdxWidth bits)
          //  .setName(f"dbgTestWrObjPipeLast_tempArrIdx_$x")
          //dbgTestWrObjPipeLastTempArrIdx := (
          //  tempWrLineMemEntry.getSubLineMemTempArrIdx()
          //)
          //def tempAddrWidth = (
          //  tempWrLineMemEntry.getSubLineMemTempAddr().getWidth
          //)
          //val dbgTestWrObjPipeLastTempAddr = UInt(tempAddrWidth bits)
          //  .setName(f"dbgTestWrObjPipeLast_tempAddr_$x")
          //dbgTestWrObjPipeLastTempAddr := (
          //  tempWrLineMemEntry.getSubLineMemTempAddr()
          //)
          //val tempGridIdx = UInt(wrObjPipeLast.gridIdxLsb.getWidth bits)
          //  .setName("dbgTestWrObjPipeLast_tempGridIdx")
          //tempGridIdx := wrObjPipeLast.gridIdxLsb
          //def tempObjSubLineMemArrIdx = wrBgPipeLast.get
          //val tempRdLineMemEntry = Vec.fill(params.objTileSize2d.x)(
          //  ObjSubLineMemEntry()
          //)
          switch (
            rWrLineMemArrIdx
            //wrObjPipeLast.lineMemArrIdx
          ) {
            for (
              jdx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1
              //jdx <- 0 to (1 << wrObjPipeLast.lineMemArrIdx.getWidth) - 1
            ) {
              is (jdx) {
                //def tempObjArrIdx = params.getObjSubLineMemArrIdx(
                //  //addr=combinePipeLast.cnt
                //  //addr=Cat(
                //  //  wrObjPipeLast.pxPos(0).x.asUInt(
                //  //    wrObjPipeLast.pxPos(0).x.high downto 1
                //  //  ),
                //  //  wrObjPipeLast.gridIdxLsb
                //  //).asUInt
                //  addr=wrObjPipeLast.getObjSubLineMemArrIdxAddr(),
                //)

                def tempObjArrIdx = wrObjPipeLast.getObjSubLineMemArrIdx(
                  x=wrObjPipeLast.pxPosXGridIdxFindFirstSameAsIdx
                )
                //def tempObjArrIdx = (
                //  wrObjPipeLast.pxPosXGridIdx(
                //    wrObjPipeLast.pxPosXGridIdxFindFirstSameAs
                //  )(
                //  )
                //)
                val dbgTestWrObjPipeLast_tempObjArrIdx = UInt(
                  tempObjArrIdx.getWidth bits
                )
                  .setName(
                    f"dbgTestWrObjPipeLast_tempObjArrIdx_$jdx" //+ f"_$x"
                  )
                dbgTestWrObjPipeLast_tempObjArrIdx := tempObjArrIdx

                //def tempObjArrElemIdx = (
                //  params.getObjSubLineMemArrElemIdx(
                //    //addr=combinePipeLast.cnt
                //    addr=wrObjPipeLast.pxPos(x).x.asUInt
                //  )
                //)
                //val dbgTestWrObjPipeLast_tempObjArrElemIdx = UInt(
                //  tempObjArrElemIdx.getWidth bits
                //)
                //  .setName(
                //    f"dbgTestWrObjPipeLast_tempObjArrElemIdx_$jdx"
                //    + f"_$x"
                //  )
                //dbgTestWrObjPipeLast_tempObjArrElemIdx := (
                //  tempObjArrElemIdx
                //)
                //when (
                //  wrObjPipeLast.pxPosXGridIdxFindFirstSameAsFound
                //) {
                  objSubLineMemArr(jdx).write(
                    address=tempObjArrIdx
                    //(
                    //  wrObjPipeLast.getObjSubLineMemArrIdx_tempRange()
                    //)
                    ,
                    data=wrObjPipeLast.wrLineMemEntry,
                    //data=tempWrLineMemEntry
                  )
                //}

                //when (
                //  wrObjPipeLast.pxPos.x >= 0 
                //  && wrObjPipeLast.pxPos.x < params.oneLineMemSize
                //)
                //for (x <- 0 to params.objTileSize2d.x - 1) {
                  //when (
                  //  //tempAddr.resized === wrObjPipeLast.pxPos.x.asUInt
                  //  !wrObjPipeLast.pxPos.x.msb
                  //) {
                  //when (wrObjPipeLast.overwriteLineMemEntry) {
                    // BEGIN: come back to this later
                    //objSubLineMemArr(jdx).write(
                    //  //address=wrObjPipeLast.getCntTilePxsCoordX(),
                    //  address=wrObjPipeLast.pxPos.x.asUInt.resized,
                    //  //address=tempAddr,
                    //  //data=wrObjPipeLast.wrLineMemEntry,
                    //  data=tempWrLineMemEntry,
                    //)
                    // END: come back to this later
                    //dbgObjLineMemVec(jdx)(tempAddr) := (
                    //  wrObjPipeLast.wrLineMemEntry
                    //)
                  //switch (
                  //  //tempWrLineMemEntry.getSubLineMemTempArrIdx()
                  //  dbgTestWrObjPipeLastTempArrIdx
                  //) {
                  //  for (kdx <- 0 to (1 << tempArrIdxWidth) - 1) {
                  //    //val dbgTestWrObjPipeLast = UInt(
                  //    //  params.objTileSize2dPow.x bits
                  //    //)
                  //    //dbgTestWrObjPipeLast(
                  //    //  dbgTestWrObjPipeLast.high downto
                  //    //)
                  //    //dbgTestWrObjPipeLast(tempWidth - 1 downto 0) := kdx
                  //    is (kdx) {
                  //      // BEGIN: later
                  //      //objSubLineMemA2d(jdx)(kdx).write(
                  //      //  address=tempWrLineMemEntry.getSubLineMemTempAddr(),
                  //      //  data=tempWrLineMemEntry
                  //      //)
                  //      //when (
                  //      //  Cat(
                  //      //    dbgTestWrObjPipeLastTempArrIdx,
                  //      //    dbgTestWrObjPipeLastTempAddr
                  //      //  ).asUInt <= params.oneLineMemSize - 1
                  //      //) {
                  //        // BEGIN: debug comment this out; need this
                  //        when (!wrBgPipeLast.bakCnt.msb) {
                  //          objSubLineMemA2d(jdx)(kdx).write(
                  //            //address=tempWrLineMemEntry.getSubLineMemTempAddr(),
                  //            address=dbgTestWrObjPipeLastTempAddr,
                  //            data=tempWrLineMemEntry,
                  //          )
                  //        }
                  //        // END: debug comment this out; need this
                  //        //objSubLineMemA2d(jdx)(x).write(
                  //        //  //address=tempWrLineMemEntry.getSubLineMemTempAddr(),
                  //        //  address=dbgTestWrObjPipeLastTempAddr,
                  //        //  data=tempWrLineMemEntry,
                  //        //)
                  //      //}
                  //      // END: later
                  //    }
                  //  }
                  //}
                  ////}
                  ////}
                //}
              }
            }
            //default {
            //  wrLineMemEntry := rPastWrLineMemEntry
            //}
          }
        //}
      }
    }
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
        stageData=stageData.craft(
          1
          //wrBgObjPipeNumStages + 1
        )
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          //val tempCombineLineMemIdxArr = new ArrayBuffer[UInt]()
          val tempCombineLineMemIdx = KeepAttribute(
            UInt(
              log2Up(
                //params.oneLineMemSize
                params.oneLineMemSize
              ) bits
            )
              .setName("tempCombineLineMemIdx")
          )
          tempCombineLineMemIdx := (
            tempInp.cnt(
              log2Up(
                //params.oneLineMemSize
                params.oneLineMemSize
              ) - 1
              downto 0
            )
          )

          when (clockDomain.isResetActive) {
            tempOutp.stage1 := tempOutp.stage1.getZero
            //combinePipeStage1Busy := False
            //lineFifo.io.pop.ready := True
            //lineFifo.io.pop.ready := False
            //objLineFifo.io.pop.ready := False
          } otherwise {
            tempOutp.stage1.ext := tempOutp.stage1.ext.getZero
            //val combineTestificate = Bool()
            //  .setName("combineTestificate")

            //def bgTempArrIdx = params.getBgSubLineMemTempArrIdx(
            //  //pxPosX=tempInp.cnt(
            //  //  log2Up(params.oneLineMemSize) - 1 downto 0
            //  //)
            //  pxPosX=tempCombineLineMemIdx
            //)
            //val dbgTestCombinePipe1_bgTempArrIdx = UInt(
            //  bgTempArrIdx.getWidth bits
            //)
            //  .setName("dbgTestCombinePipe1_bgTempArrIdx")
            //dbgTestCombinePipe1_bgTempArrIdx := bgTempArrIdx
            //def bgTempAddr = params.getBgSubLineMemTempAddr(
            //  //pxPosX=tempInp.cnt
            //  //pxPosX=tempInp.cnt(
            //  //  log2Up(params.oneLineMemSize) - 1 downto 0
            //  //)
            //  pxPosX=tempCombineLineMemIdx
            //)
            //val dbgTestCombinePipe1_bgTempAddr = UInt(
            //  bgTempAddr.getWidth bits
            //)
            //  .setName("dbgTestCombinePipe1_bgTempAddr")
            //dbgTestCombinePipe1_bgTempAddr := bgTempAddr

            //val dbgTestCombinePipe1_bgRdLineMemEntry = BgSubLineMemEntry()
            //  .setName("dbgTestCombinePipe1_bgRdLineMemEntry")
            //dbgTestCombinePipe1_bgRdLineMemEntry := (
            //  tempOutp.bgRdLineMemEntry
            //)
            //def objTempArrIdx = params.getObjSubLineMemTempArrIdx(
            //  //pxPosX=tempInp.cnt(
            //  //  log2Up(params.oneLineMemSize) - 1 downto 0
            //  //)
            //  pxPosX=tempCombineLineMemIdx
            //)
            //val dbgTestCombinePipe1_objTempArrIdx = UInt(
            //  objTempArrIdx.getWidth bits
            //)
            //  .setName("dbgTestCombinePipe1_objTempArrIdx")
            //dbgTestCombinePipe1_objTempArrIdx := objTempArrIdx
            //def objTempAddr = params.getObjSubLineMemTempAddr(
            //  //pxPosX=tempInp.cnt
            //  //pxPosX=tempInp.cnt(
            //  //  log2Up(params.oneLineMemSize) - 1 downto 0
            //  //)
            //  pxPosX=tempCombineLineMemIdx
            //)
            //val dbgTestCombinePipe1_objTempAddr = UInt(
            //  objTempAddr.getWidth bits
            //)
            //  .setName("dbgTestCombinePipe1_objTempAddr")
            //dbgTestCombinePipe1_objTempAddr := objTempAddr



            def bgSubLineMemArrIdx = params.getBgSubLineMemArrIdx(
              addr=tempCombineLineMemIdx
            )
            val dbgTestCombinePipe1_bgSubLineMemArrIdx = UInt(
              bgSubLineMemArrIdx.getWidth bits
            )
              .setName("dbgTestCombinePipe1_bgSubLineMemArrIdx")
            dbgTestCombinePipe1_bgSubLineMemArrIdx := (
              bgSubLineMemArrIdx
            )

            def bgSubLineMemArrElemIdx = params.getBgSubLineMemArrElemIdx(
              addr=tempCombineLineMemIdx
            )
            val dbgTestCombinePipe1_bgSubLineMemArrElemIdx = UInt(
              bgSubLineMemArrElemIdx.getWidth bits
            )
              .setName("dbgTestCombinePipe1_bgSubLineMemArrElemIdx")
            dbgTestCombinePipe1_bgSubLineMemArrElemIdx := (
              bgSubLineMemArrElemIdx
            )

            def objSubLineMemArrIdx = (
              params.getObjSubLineMemArrIdx(
                addr=tempCombineLineMemIdx
              )
            )
            val dbgTestCombinePipe1_objSubLineMemArrIdx = UInt(
              objSubLineMemArrIdx.getWidth bits
            )
              .setName("dbgTestCombinePipe1_objSubLineMemArrIdx")
            dbgTestCombinePipe1_objSubLineMemArrIdx := (
              objSubLineMemArrIdx
            )

            def objSubLineMemArrElemIdx = (
              params.getObjSubLineMemArrElemIdx(
                addr=tempCombineLineMemIdx
              )
            )
            val dbgTestCombinePipe1_objSubLineMemArrElemIdx = UInt(
              objSubLineMemArrElemIdx.getWidth bits
            )
              .setName("dbgTestCombinePipe1_objSubLineMemArrElemIdx")
            dbgTestCombinePipe1_objSubLineMemArrElemIdx := (
              objSubLineMemArrElemIdx
            )


            switch (
              //rCombineRdLineMemArrIdx
              rCombineLineMemArrIdx
              //tempInp.rdLineMemArrIdx
              //tempInp.lineMemArrIdx
            ) {
              for (
                //jdx <- 0 to (1 << rCombineRdLineMemArrIdx.getWidth) - 1
                jdx <- 0 to (1 << rCombineLineMemArrIdx.getWidth) - 1
                //jdx <- 0 to (1 << tempInp.rdLineMemArrIdx.getWidth) - 1
                //jdx <- 0 to (1 << tempInp.lineMemArrIdx.getWidth) - 1
              ) {
                //tempCombineLineMemIdxArr += (
                //  UInt(log2Up(params.oneLineMemSize) bits)
                //  .setName(f"tempCombineLineMemIdxArr_$idx")
                //)
                //tempCombineLineMemIdxArr(jdx) := (
                //  tempInp.cnt(log2Up(params.oneLineMemSize) - 1 downto 0)
                //)
                is (jdx) {
                  //tempOutp.bgRdLineMemEntry := (
                  //  bgSubLineMemArr(jdx).readAsync(
                  //    address=tempCombineLineMemIdx
                  //    //address=tempInp.bakCnt(
                  //    //  log2Up(params.oneLineMemSize) - 1 downto 0
                  //    //),
                  //    //address=(tempInp.bakCnt + 1)(
                  //    //  log2Up(params.oneLineMemSize) - 1 downto 0
                  //    //),
                  //    //address=tempInp.cnt(
                  //    //  log2Up(params.oneLineMemSize) - 1 downto 0
                  //    //),
                  //    //address=(params.oneLineMemSize + 1 - tempInp.bakCnt)(
                  //    //  log2Up(params.oneLineMemSize) - 1 downto 0
                  //    //),
                  //    //readUnderWrite=writeFirst,
                  //  )
                  //)
                  // BEGIN: old, non-synthesizable code
                  //switch (bgTempArrIdx) {
                  //  for (kdx <- 0 to (1 << bgTempArrIdx.getWidth) - 1) {
                  //    is (kdx) {
                  //      when (!tempInp.bakCnt.msb) {
                  //        tempOutp.bgRdLineMemEntry := (
                  //          bgSubLineMemA2d(jdx)(kdx).readAsync(
                  //            address=bgTempAddr
                  //          )
                  //        )
                  //      } otherwise {
                  //        tempOutp.bgRdLineMemEntry := (
                  //          tempOutp.bgRdLineMemEntry.getZero
                  //        )
                  //      }
                  //    }
                  //  }
                  //}
                  when (!tempInp.bakCnt.msb) {
                    //tempOutp.bgRdLineMemEntry := (
                    //  //bgSubLineMemA2d(jdx)(kdx).readAsync(
                    //  //  address=bgTempAddr
                    //  //)
                    //  bgSubLineMemArr(jdx).readAsync(
                    //    address=bgSubLineMemArrIdx
                    //  )(bgSubLineMemArrElemIdx)
                    //)
                    rCombinePipeOut1Ext.bgRdLineMemEntry := (
                      bgSubLineMemArr(jdx).readSync(
                        address=bgSubLineMemArrIdx
                      )(bgSubLineMemArrElemIdx)
                    )
                  } otherwise {
                    //tempOutp.bgRdLineMemEntry := (
                    //  tempOutp.bgRdLineMemEntry.getZero
                    //)
                    rCombinePipeOut1Ext.bgRdLineMemEntry := (
                      rCombinePipeOut1Ext.bgRdLineMemEntry.getZero
                    )
                  }
                  // END: old, non-synthesizable code
                  //tempOutp.bgRdLineMemEntry := (
                  //  dbgBgLineMemVec(jdx)(
                  //    tempCombineLineMemIdx
                  //  )
                  //)
                  //when (
                  //  (
                  //    if (jdx == 3) True else False 
                  //  ) && (
                  //    tempCombineLineMemIdx === 8
                  //    || tempCombineLineMemIdx === 16
                  //  )
                  //) {
                  //  combineTestificate := tempCombineLineMemIdx === 8
                  //} otherwise {
                  //  combineTestificate := False
                  //}
                  // BEGIN: debug, remove later
                  //tempOutp.objRdLineMemEntry := (
                  //  tempOutp.objRdLineMemEntry.getZero
                  //)
                  // END: debug, remove later
                  // BEGIN: come back to this later
                  //tempOutp.objRdLineMemEntry := (
                  //  objSubLineMemArr(jdx).readAsync(
                  //    address=tempCombineLineMemIdx
                  //    //address=tempInp.bakCnt(
                  //    //  log2Up(params.oneLineMemSize) - 1 downto 0
                  //    //),
                  //    //address=(tempInp.bakCnt + 1)(
                  //    //  log2Up(params.oneLineMemSize) - 1 downto 0
                  //    //),
                  //    //address=tempInp.cnt(
                  //    //  log2Up(params.oneLineMemSize) - 1 downto 0
                  //    //),
                  //    //address=(params.oneLineMemSize + 1 - tempInp.bakCnt)(
                  //    //  log2Up(params.oneLineMemSize) - 1 downto 0
                  //    //),
                  //    //readUnderWrite=writeFirst,
                  //  )
                  //  //dbgObjLineMemVec(jdx)(tempCombineLineMemIdx)
                  //)
                  when (!tempInp.bakCnt.msb) {
                    //tempOutp.objRdLineMemEntry := (
                    //  //objSubLineMemA2d(jdx)(kdx).readAsync(
                    //  //  address=objTempAddr
                    //  //)
                    //  objSubLineMemArr(jdx).readAsync(
                    //    address=objSubLineMemArrIdx
                    //  )(objSubLineMemArrElemIdx)
                    //)
                    rCombinePipeOut1Ext.objRdLineMemEntry := (
                      objSubLineMemArr(jdx).readSync(
                        address=objSubLineMemArrIdx
                      )(objSubLineMemArrElemIdx)
                    )
                  } otherwise {
                    //tempOutp.objRdLineMemEntry := (
                    //  tempOutp.objRdLineMemEntry.getZero
                    //)
                    rCombinePipeOut1Ext.objRdLineMemEntry := (
                      rCombinePipeOut1Ext.objRdLineMemEntry.getZero
                    )
                  }
                  //switch (objTempArrIdx) {
                  //  for (kdx <- 0 to (1 << objTempArrIdx.getWidth) - 1) {
                  //    is (kdx) {
                  //      when (!tempInp.bakCnt.msb) {
                  //        tempOutp.objRdLineMemEntry := (
                  //          objSubLineMemA2d(jdx)(kdx).readAsync(
                  //            address=objTempAddr
                  //          )
                  //        )
                  //      } otherwise {
                  //        tempOutp.objRdLineMemEntry := (
                  //          tempOutp.objRdLineMemEntry.getZero
                  //        )
                  //      }
                  //    }
                  //  }
                  //}
                  // END: come back to this later
                }
              }
            }
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
            if (idx == 2) {
              when (tempInp.fire) {
                //tempOutp.stage1 := rCombinePipeOut1Payload
                tempOutp.stage1.ext := rCombinePipeOut1Ext 
              } otherwise {
                tempOutp.stage1 := RegNext(tempOutp.stage1)
              }
            } else {
              tempOutp.stage1 := tempInp.stage1
            }
          }
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(
          2
          //wrBgObjPipeNumStages + 2
        )
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
              //tempInp.objRdLineMemEntry.prio(
              //  tempInp.bgRdLineMemEntry.prio.bitsRange
              //) <= tempInp.bgRdLineMemEntry.prio
              tempInp.objRdLineMemEntry.prio
              <= tempInp.bgRdLineMemEntry.prio
              //rCombinePipeOut1Payload.objRdLineMemEntry.prio
              //<= rCombinePipeOut1Payload.bgRdLineMemEntry.prio
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
        stageData=stageData.craft(
          3
          //wrBgObjPipeNumStages + 3
        )
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
        stageData=stageData.craft(
          4
          //wrBgObjPipeNumStages + 4
        )
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
            tempOutp.combineWrLineMemEntry.addr := (
              tempInp.cnt(
                tempOutp.combineWrLineMemEntry.addr.bitsRange
              )
            )
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
        combinePipeLast.fire
        //combinePipeOut.last.fire
      ) {
        //def tempArrIdx = params.getObjSubLineMemTempArrIdx(
        //  pxPosX=combinePipeLast.cnt
        //)
        //val dbgTestCombinePipeLastTempArrIdx = UInt(
        //  tempArrIdx.getWidth bits
        //)
        //  .setName("dbgTestCombinePipeLast_tempArrIdx")
        //dbgTestCombinePipeLastTempArrIdx := tempArrIdx

        //def tempAddr = params.getObjSubLineMemTempAddr(
        //  pxPosX=combinePipeLast.cnt
        //)
        //val dbgTestCombinePipeLastTempAddr = UInt(
        //  tempAddr.getWidth bits
        //)
        //  .setName("dbgTestCombinePipeLast_tempAddr")
        //dbgTestCombinePipeLastTempAddr := tempAddr

        def tempObjArrIdx = params.getObjSubLineMemArrIdx(
          addr=combinePipeLast.cnt
        )
        //def tempObjArrIdx = combinePipeLast.cnt(
        //  log2Up(params.oneLineMemSize) - 1
        //  downto (
        //    log2Up(params.oneLineMemSize)
        //    //- log2Up(params.objSubLineMemArrSize)
        //    - params.objTileSize2d.x
        //  )
        //)
        val dbgTestCombinePipeLast_tempObjArrIdx = UInt(
          tempObjArrIdx.getWidth bits
        )
          .setName("dbgTestCombinePipeLast_tempObjArrIdx")
        dbgTestCombinePipeLast_tempObjArrIdx := tempObjArrIdx

        //def tempObjArrElemIdx = params.getObjSubLineMemArrElemIdx(
        //  addr=combinePipeLast.cnt
        //)
        //val dbgTestCombinePipeLast_tempObjArrElemIdx = UInt(
        //  tempObjArrElemIdx.getWidth bits
        //)
        //  .setName("dbgTestCombinePipeLast_tempObjArrElemIdx")
        //dbgTestCombinePipeLast_tempObjArrElemIdx := tempObjArrElemIdx

        switch (
          //rCombineWrLineMemArrIdx
          //rCombineWrLineMemArrIdx
          rCombineLineMemArrIdx
          //combinePipeLast.lineMemArrIdx
        ) {
          for (
            //jdx <- 0 to (1 << rCombineWrLineMemArrIdx.getWidth) - 1
            jdx <- 0 to (1 << rCombineLineMemArrIdx.getWidth) - 1
            //jdx <- 0 
            //to (1 << combinePipeLast.lineMemArrIdx.getWidth) - 1
          ) {
            // BEGIN: come back to this later
            val tempObjLineMemEntry = Vec.fill(params.objTileSize2d.x)(
              ObjSubLineMemEntry()
            ).getZero
            //def tempAddr = combinePipeLast.cnt(
            //  log2Up(params.oneLineMemSize) - 1 downto 0
            //)
            //def tempAddrSingle = combinePipeLast.cnt(
            //  log2Up(params.oneLineMemSize) - 1 downto 0
            //)

            // END: come back to this later
            is (jdx) {
              val tempIdx = (
                if (jdx == 0) {
                  params.numLineMemsPerBgObjRenderer - 1
                } else {
                  jdx - 1
                }
              )
              // BEGIN: come back to this later
              //objSubLineMemArr(tempIdx).write(
              //  //address=combinePipeLast.bakCnt(
              //  //  log2Up(params.oneLineMemSize) - 1 downto 0
              //  //),
              //  //data=combinePipeLast.combineWrLineMemEntry,
              //  address=tempAddrSingle,
              //  data=tempObjLineMemEntry
              //)
              //--------
              // BEGIN: synth debug comment this out; later 
              objSubLineMemArr(jdx).write(
                address=tempObjArrIdx,
                data=tempObjLineMemEntry,
              )
              // END: synth debug comment this out; later 
              //--------
              //switch (tempArrIdx) {
              //  for (kdx <- 0 to (1 << tempArrIdx.getWidth) - 1) {
              //    is (kdx) {
              //      //when (
              //      //  Cat(
              //      //    dbgTestCombinePipeLastTempArrIdx,
              //      //    tempAddr
              //      //  ).asUInt <= params.oneLineMemSize - 1
              //      //) {
              //        objSubLineMemA2d(jdx)(kdx).write(
              //          address=tempAddr,
              //          data=tempObjLineMemEntry,
              //        )
              //      //}
              //    }
              //  }
              //}
              // END: come back to this later
              //dbgObjLineMemVec(tempIdx)(tempAddr) := tempObjLineMemEntry
            }
          }
        }
        outp.col := combinePipeLast.combineWrLineMemEntry.col.rgb
        //outp.col := combinePipeOut.last.combineWrLineMemEntry.col.rgb
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
    //doSendIntoFifo()
    //doSendIntoObjFifo()
    combineLineMemEntries()
    ////when (pop.fire) {
    //readLineMemEntries()

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
