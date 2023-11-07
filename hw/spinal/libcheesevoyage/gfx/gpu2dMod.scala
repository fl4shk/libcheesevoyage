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
//import libcheesevoyage.general.MultiMemReadSync
//import libcheesevoyage.general.MemReadSyncIntoPipe
import libcheesevoyage.general.WrPulseRdPipeSimpleDualPortMem
import libcheesevoyage.general.WrPulseRdPipeSimpleDualPortMemIo
//import libcheesevoyage.general.MemReadSyncIntoStreamHaltVecs
//import libcheesevoyage.general.MemReadSyncIntoStream
//import libcheesevoyage.general.FpgacpuPipeForkLazy
import libcheesevoyage.general.FpgacpuPipeForkBlocking
//import libcheesevoyage.general.FpgacpuPipeForkEager
import libcheesevoyage.general.FpgacpuPipeJoin
import libcheesevoyage.general.FpgacpuRamSimpleDualPort

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
  val pop = master(Stream(Gpu2dPopPayload(params=params)))
  //--------
  //def asHost(): Unit = {
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
    //val bgTileMemArr = Mem(
    //  //wordType=UInt(params.palEntryMemIdxWidth bits),
    //  wordType=Gpu2dTile(params=params, isObj=false),
    //  //wordCount=params.numPxsForAllBgTiles,
    //  //wordCount=params.numTilesPerBg,
    //  wordCount=params.numBgTiles,
    //)
    //  .initBigInt(Array.fill(params.numBgTiles)(BigInt(0)))
    //  .addAttribute("ram_style", params.bgTileArrRamStyle)

    val bgTileMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dTile]
    ]()
    for (x <- 0 until params.bgTileSize2d.x) {
      bgTileMemArr += FpgacpuRamSimpleDualPort(
        wordType=Gpu2dTile(params=params, isObj=false),
        depth=params.numBgTiles,
        initBigInt={
          val temp = new ArrayBuffer[BigInt]()
          for (idx <- 0 until params.numBgTiles) {
            temp += BigInt(0)
          }
          Some(temp)
        },
        arrRamStyle=params.bgTileArrRamStyle,
      )
      bgTileMemArr(x).io.wrEn := bgTilePush.fire
      bgTileMemArr(x).io.wrAddr := bgTilePush.payload.memIdx
      bgTileMemArr(x).io.wrData := bgTilePush.payload.tile
    }

    //object RdBgTileMemInfo {
    //  def wrBgIdx = 0
    //  def numReaders = 1
    //}
    //val rdBgTileMem = MultiMemReadSync(
    //  someMem=bgTileMemArr,
    //  numReaders=RdBgTileMemInfo.numReaders,
    //)
    //when (bgTilePush.fire) {
    //  //bgTileMemArr.write(
    //  //  address=bgTilePush.payload.memIdx,
    //  //  data=bgTilePush.payload.tile,
    //  //)
    //}

    //val objTileMem = Mem(
    //  //wordType=UInt(params.palEntryMemIdxWidth bits),
    //  wordType=Gpu2dTile(params=params, isObj=true),
    //  //wordCount=params.numPxsForAllObjTiles,
    //  wordCount=params.numObjTiles,
    //)
    //  .initBigInt(Array.fill(params.numObjTiles)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", params.objTileArrRamStyle)
    val objTileMem = FpgacpuRamSimpleDualPort(
      wordType=Gpu2dTile(params=params, isObj=true),
      depth=params.numObjTiles,
      initBigInt={
        val temp = new ArrayBuffer[BigInt]()
        for (idx <- 0 until params.numObjTiles) {
          temp += BigInt(0)
        }
        Some(temp)
      },
      arrRamStyle=params.objTileArrRamStyle,
    )
    //val rdAddrObjTileMem = UInt(params.numObjTilesPow bits)
    //val rdDataObjTileMem = Gpu2dTile(params=params, isObj=true)
    //object RdObjTileMemInfo {
    //  def wrObjIdx = 0
    //  def numReaders = 1
    //}
    //val rdObjTileMem = MultiMemReadSync(
    //  someMem=objTileMem,
    //  numReaders=RdObjTileMemInfo.numReaders,
    //)
    objTileMem.io.wrEn := objTilePush.fire
    objTileMem.io.wrAddr := objTilePush.payload.memIdx
    objTileMem.io.wrData := objTilePush.payload.tile
    //when (objTilePush.fire) {
    //  objTileMem.write(
    //    address=objTilePush.payload.memIdx,
    //    data=objTilePush.payload.tile,
    //  )
    //}
    //--------
    //val bgEntryMem = Mem(
    //  wordType=Gpu2dBgEntry(params=params),
    //  wordCount=params.numColsInPal,
    //)
    //  .addAttribute("ram_style", params.palEntryArrRamStyle)
    //--------
    //val bgEntryMemArr = new ArrayBuffer[Mem[Gpu2dBgEntry]]()
    val bgEntryMemA2d = new ArrayBuffer[ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dBgEntry]
    ]]()
    //val rdDataBgEntryMemArr = Vec.fill(params.numBgs)(Gpu2dBgEntry(
    //  params=params
    //))
    //val rdAddrBgEntryMemArr = Vec.fill(params.numBgs)(
    //  UInt(log2Up(params.numTilesPerBg) bits)
    //)
    //val rdBgEntryMemArr = new ArrayBuffer[MultiMemReadSync[Gpu2dBgEntry]]()
    //object RdBgEntryMemArrInfo {
    //  def wrBgIdx = 0
    //  def numReaders = 1
    //}
    val bgAttrsArr = new ArrayBuffer[Gpu2dBgAttrs]()
    for (idx <- 0 until params.numBgs) {
      //--------
      //bgEntryMemArr += Mem(
      //  wordType=Gpu2dBgEntry(params=params),
      //  wordCount=params.numTilesPerBg,
      //)
      //  .initBigInt(Array.fill(params.numTilesPerBg)(BigInt(0)).toSeq)
      //  .addAttribute("ram_style", params.bgEntryArrRamStyle)
      //  .setName(f"bgEntryMemArr_$idx")
      bgEntryMemA2d += new ArrayBuffer[
        FpgacpuRamSimpleDualPort[Gpu2dBgEntry]
      ]()
      for (x <- 0 until params.bgTileSize2d.x) {
        def bgEntryMemArr = bgEntryMemA2d(idx)
        bgEntryMemArr += FpgacpuRamSimpleDualPort(
          wordType=Gpu2dBgEntry(params=params),
          depth=params.numTilesPerBg,
          initBigInt={
            val temp = new ArrayBuffer[BigInt]()
            for (_ <- 0 until params.numTilesPerBg) {
              temp += BigInt(0)
            }
            Some(temp)
          },
          arrRamStyle=params.bgEntryArrRamStyle,
        )
        bgEntryMemArr(x).io.wrEn := bgEntryPushArr(idx).fire
        bgEntryMemArr(x).io.wrAddr := bgEntryPushArr(idx).payload.memIdx
        bgEntryMemArr(x).io.wrData := bgEntryPushArr(idx).payload.bgEntry
      }
      //when (bgEntryPushArr(idx).fire) {
      //  bgEntryMemArr(idx).write(
      //    address=bgEntryPushArr(idx).payload.memIdx,
      //    data=bgEntryPushArr(idx).payload.bgEntry,
      //  )
      //}
      //rdBgEntryMemArr += MultiMemReadSync(
      //  someMem=bgEntryMemArr(idx),
      //  numReaders=RdBgEntryMemArrInfo.numReaders,
      //)
      //for (rdIdx <- 0 until rdBgEntryMemArr(idx).numReaders) {
      //  rdBgEntryMemArr(idx).readSync(rdIdx)
      //}
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
    //val objAttrsMem = Mem(
    //  wordType=Gpu2dObjAttrs(params=params),
    //  wordCount=params.numObjs,
    //)
    //  .initBigInt(Array.fill(params.numObjs)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", params.objAttrsArrRamStyle)
    val objAttrsMem = FpgacpuRamSimpleDualPort(
      wordType=Gpu2dObjAttrs(params=params),
      depth=params.numObjs,
      initBigInt={
        val temp = new ArrayBuffer[BigInt]()
        for (idx <- 0 until params.numObjs) {
          temp += BigInt(0)
        }
        Some(temp)
      },
      arrRamStyle=params.objAttrsArrRamStyle,
    )
    //object RdObjAttrsMemInfo {
    //  def wrObjIdx = 0
    //  def numReaders = 1
    //}
    //val rdObjAttrsMem = MultiMemReadSync(
    //  someMem=objAttrsMem,
    //  numReaders=RdObjAttrsMemInfo.numReaders,
    //)
    //for (rdIdx <- 0 until rdObjAttrsMem.numReaders) {
    //  rdObjAttrsMem.readSync(rdIdx)
    //}
    objAttrsMem.io.wrEn := objAttrsPush.fire
    objAttrsMem.io.wrAddr := objAttrsPush.payload.memIdx
    objAttrsMem.io.wrData := objAttrsPush.payload.objAttrs
    //when (objAttrsPush.fire) {
    //  objAttrsMem.write(
    //    address=objAttrsPush.payload.memIdx,
    //    data=objAttrsPush.payload.objAttrs,
    //  )
    //}
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
    val bgPalEntryMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dPalEntry]
    ]()
    for (x <- 0 until params.bgTileSize2d.x) {
      bgPalEntryMemArr += FpgacpuRamSimpleDualPort(
        wordType=Gpu2dPalEntry(params=params),
        depth=params.numColsInBgPal,
        initBigInt={
          val temp = new ArrayBuffer[BigInt]()
          for (idx <- 0 until params.numColsInBgPal) {
            temp += BigInt(0)
          }
          Some(temp)
        },
        arrRamStyle=params.bgPalEntryArrRamStyle,
      )
      bgPalEntryMemArr(x).io.wrEn := bgPalEntryPush.fire
      bgPalEntryMemArr(x).io.wrAddr := bgPalEntryPush.payload.memIdx
      bgPalEntryMemArr(x).io.wrData := bgPalEntryPush.payload.bgPalEntry
    }
    //val bgPalEntryMem = Mem(
    //  wordType=Gpu2dPalEntry(params=params),
    //  wordCount=params.numColsInBgPal,
    //)
    //  .initBigInt(Array.fill(params.numColsInBgPal)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", params.bgPalEntryArrRamStyle)
    //  .addAttribute("keep")
    //object RdBgPalEntryMemInfo {
    //  def wrBgIdx = 0
    //  def numReaders = 1
    //}
    //val rdBgPalEntryMem = MultiMemReadSync(
    //  someMem=bgPalEntryMem,
    //  numReaders=RdBgPalEntryMemInfo.numReaders,
    //)
    //for (rdIdx <- 0 until rdBgPalEntryMem.numReaders) {
    //  rdBgPalEntryMem.readSync(rdIdx)
    //}
    //when (bgPalEntryPush.fire) {
    //  bgPalEntryMem.write(
    //    address=bgPalEntryPush.payload.memIdx,
    //    data=bgPalEntryPush.payload.bgPalEntry,
    //  )
    //}

    val objPalEntryMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dPalEntry]
    ]()
    for (x <- 0 until params.objTileSize2d.x) {
      objPalEntryMemArr += FpgacpuRamSimpleDualPort(
        wordType=Gpu2dPalEntry(params=params),
        depth=params.numColsInObjPal,
        initBigInt={
          val temp = new ArrayBuffer[BigInt]()
          for (idx <- 0 until params.numColsInObjPal) {
            temp += BigInt(0)
          }
          Some(temp)
        },
        arrRamStyle=params.objPalEntryArrRamStyle,
      )
      objPalEntryMemArr(x).io.wrEn := objPalEntryPush.fire
      objPalEntryMemArr(x).io.wrAddr := objPalEntryPush.payload.memIdx
      objPalEntryMemArr(x).io.wrData := objPalEntryPush.payload.objPalEntry
    }
    //val objPalEntryMem = Mem(
    //  wordType=Gpu2dPalEntry(params=params),
    //  wordCount=params.numColsInObjPal,
    //)
    //  .initBigInt(Array.fill(params.numColsInObjPal)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", params.objPalEntryArrRamStyle)
    //object RdObjPalEntryMemInfo {
    //  def wrObjIdx = 0
    //  def numReaders = 1
    //}
    //val rdObjPalEntryMem = MultiMemReadSync(
    //  someMem=objPalEntryMem,
    //  numReaders=RdObjPalEntryMemInfo.numReaders,
    //)
    //for (rdIdx <- 0 until rdObjPalEntryMem.numReaders) {
    //  rdObjPalEntryMem.readSync(rdIdx)
    //}
    //when (objPalEntryPush.fire) {
    //  objPalEntryMem.write(
    //    address=objPalEntryPush.payload.memIdx,
    //    data=objPalEntryPush.payload.objPalEntry,
    //  )
    //}
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
    //def combineRdSubLineFifoDepth = 16
    //def combineRdSubLineFifoAmountCanPushStall = 8
    //val combineBgRdSubLineFifo = new AsyncReadFifo(
    //  dataType=Vec.fill(params.bgTileSize2d.x)(BgSubLineMemEntry()),
    //  depth=combineRdSubLineFifoDepth,
    //)
    //val combineObjRdSubLineFifo = new AsyncReadFifo(
    //  dataType=Vec.fill(params.bgTileSize2d.x)(ObjSubLineMemEntry()),
    //  depth=combineRdSubLineFifoDepth,
    //)
    val wrBgSubLineMemArr = new ArrayBuffer[Mem[Vec[BgSubLineMemEntry]]]()
    val combineBgSubLineMemArr = new ArrayBuffer[
      WrPulseRdPipeSimpleDualPortMem[
        //Bits,
        CombinePipePayload,
        Vec[BgSubLineMemEntry]
      ]
    ]()
    //val rdBgSubLineMemArr = (
    //  new ArrayBuffer[MultiMemReadSync[Vec[BgSubLineMemEntry]]]()
    //)
    //object RdBgSubLineMemArrInfo {
    //  //def wrBgIdx = 0
    //  def combineIdx = 0
    //  def numReaders = 1
    //}

    //--------
    // BEGIN: new `objSubLineMemA2d`
    //val objSubLineMemA2d = new ArrayBuffer[ArrayBuffer[
    //  PipeSimpleDualPortMem[Vec[ObjSubLineMemEntry]]
    //]]()
    //val wrObjSubLineMemArr = new ArrayBuffer[
    //  Mem[Vec[ObjSubLineMemEntry]]
    //]()
    val wrObjSubLineMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[Vec[ObjSubLineMemEntry]]
    ]()
    val combineObjSubLineMemArr = new ArrayBuffer[
      WrPulseRdPipeSimpleDualPortMem[
        //Bits,
        CombinePipePayload,
        Vec[ObjSubLineMemEntry]
      ]
    ]()
    //object RdObjSubLineMemArrInfo {
    //  def wrObjIdx = 0
    //  //def numReaders = 1
    //  def combineIdx = 1
    //  def numReaders = 2
    //}

    //val objSubLineMemIoV2d = Vec.fill(params.numLineMemsPerBgObjRenderer)(
    //  Vec.fill(RdObjSubLineMemArrInfo.numReaders)(
    //    WrPulseRdPipeSimpleDualPortMemIo(
    //      dataType=HardType.union(
    //        WrObjPipePayload(),
    //        CombinePipePayload(),
    //      ),
    //      wordType=Vec.fill(params.objTileSize2d.x)(ObjSubLineMemEntry()),
    //      depth=params.objSubLineMemArrSize,
    //    )
    //  )
    //)

    // END: new `objSubLineMemA2d`
    //--------
    // BEGIN: old `objSubLineMemArr`
    //val objSubLineMemArr = new ArrayBuffer[Mem[Vec[ObjSubLineMemEntry]]]()
    //val rdObjSubLineMemArr = (
    //  new ArrayBuffer[MultiMemReadSync[Vec[ObjSubLineMemEntry]]]()
    //)

    //object RdObjSubLineMemArrInfo {
    //  def wrObjIdx = 0
    //  def numReaders = 1
    //  //def combineIdx = 1
    //  //def numReaders = 2
    //}
    // END: old `objSubLineMemArr`
    //--------
    def lineMemArrIdxWidth = log2Up(params.numLineMemsPerBgObjRenderer)
    ////def rdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    ////def combineWrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd1")
    ////def combineRdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd2")
    ////def wrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd3")
    //def combineRdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    def combineLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    //def combineLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd3")
    //def wrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd1")
    //def sendIntoFifoLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    def wrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd1")
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
    //--------

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
      idx <- 0 until params.numLineMemsPerBgObjRenderer
    ) {
      //lineMemArr += Mem(
      //  //wordType=Rgb(params.rgbConfig),
      //  wordType=LineMemEntry(),
      //  wordCount=params.oneLineMemSize,
      //)
      //  .initBigInt(Array.fill(params.oneLineMemSize)(BigInt(0)).toSeq)
      //  .addAttribute("ram_style", params.lineArrRamStyle)

      val bgSubLineMemInitBigInt = new ArrayBuffer[BigInt]()
      for (initIdx <- 0 until params.bgSubLineMemArrSize) {
        bgSubLineMemInitBigInt += BigInt(0)
      }

      wrBgSubLineMemArr += Mem(
        //wordType=Rgb(params.rgbConfig),
        //wordType=BgSubLineMemEntry(),
        //wordCount=params.oneLineMemSize,
        wordType=Vec.fill(params.bgTileSize2d.x)(BgSubLineMemEntry()),
        wordCount=params.bgSubLineMemArrSize,
      )
        .initBigInt(
          //Array.fill(params.oneLineMemSize)(BigInt(0)).toSeq
          //Array.fill(params.bgSubLineMemArrSize)(BigInt(0)).toSeq
          bgSubLineMemInitBigInt.toSeq
        )
        .addAttribute("ram_style", params.lineArrRamStyle)
        //.addAttribute("ram_mode", "tdp") // true dual-port
        .setName(f"bgLineMemArr_$idx")
      combineBgSubLineMemArr += WrPulseRdPipeSimpleDualPortMem(
        dataType=CombinePipePayload(),
        wordType=Vec.fill(params.bgTileSize2d.x)(BgSubLineMemEntry()),
        depth=params.bgSubLineMemArrSize,
        initBigInt=Some(bgSubLineMemInitBigInt),
      )(
        //getWordFunc=(
        //  inpPayload: Bits,
        //) => {
        //  val ret = Vec.fill(params.bgTileSize2d.x)(BgSubLineMemEntry())
        //  switch (rWrLineMemArrIdx) {
        //    for (jdx <- 0 until params.numLineMemsPerBgBgRenderer) {
        //      is (jdx) {
        //        if (idx == jdx) {
        //          val tempInpData = (
        //            inpPayload.aliasAs(WrBgPipePayload())
        //          )
        //          //ret := tempInpData.stage5.rdSubLineMemEntry
        //          ret := tempInpData.stage7.ext.wrLineMemEntry
        //        } else {
        //          val tempInpData = (
        //            inpPayload.aliasAs(CombinePipePayload())
        //          )
        //          //ret := tempInpData.stage2.rdBg
        //          //ret := cloneOf(ret).getZero
        //          ret := ret.getZero
        //        }
        //      }
        //    }
        //    //is (0) {
        //    //  if (idx == 0) {
        //    //    val tempInpData = inpPayload.aliasAs(WrBgPipePayload())
        //    //  } else { // if (idx == 1)
        //    //    val tempInpData = inpPayload
        //    //  }
        //    //}
        //    //is (1) {
        //    //  if (idx == 0) {
        //    //  } else { // if (idx == 1)
        //    //  }
        //    //}
        //  }
        //  ret
        //},
        setWordFunc=(
          //io: WrPulseRdPipeSimpleDualPortMemIo[
          //  Bits,
          //  Vec[BgSubLineMemEntry],
          //],
          unionIdx: UInt,
          outpPayload: CombinePipePayload,
          inpPayload: CombinePipePayload,
          bgTileRow: Vec[BgSubLineMemEntry],
        ) => {
          //switch (
          //  //rWrLineMemArrIdx
          //  unionIdx
          //) {
            //for (jdx <- 0 until params.numLineMemsPerBgBgRenderer) {
              //is (jdx) {
              //  if (idx == jdx) {
              //    val tempOutpData = (
              //      outpPayload.aliasAs(WrBgPipePayload())
              //    )
              //    val tempInpData = (
              //      inpPayload.aliasAs(WrBgPipePayload())
              //    )
              //    //ret := tempInpData.stage7.ext.wrLineMemEntry
              //    tempOutpData := tempInpData
              //    //tempOutpData.allowOverride
              //    tempOutpData.stage5.rdSubLineMemEntry.removeAssignments()
              //    tempOutpData.stage5.rdSubLineMemEntry := bgTileRow
              //  } else {
                  //val tempOutpData = (
                  //  outpPayload.aliasAs(CombinePipePayload())
                  //)
                  //val tempInpData = (
                  //  inpPayload.aliasAs(CombinePipePayload())
                  //)
                  //ret := ret.getZero
                  //outpPayload := inpPayload
                  outpPayload.stage0.changingRow := (
                    inpPayload.stage0.changingRow
                  )
                  outpPayload.stage0.cnt := inpPayload.stage0.cnt
                  outpPayload.stage0.bakCnt := inpPayload.stage0.bakCnt
                  outpPayload.stage0.bakCntMinus1 := (
                    inpPayload.stage0.bakCntMinus1
                  )
                  ////tempOutpData.allowOverride
                  ////outpPayload.stage2.rdBg.removeAssignments()
                  //outpPayload.allowOverride
                  outpPayload.stage2.rdBg := bgTileRow
                //}
              //}
            //}
          //}
        }
      )
        .setName(f"combineBgSubLineMemArr_$idx")

      //rdBgSubLineMemArr += MultiMemReadSync(
      //  someMem=bgSubLineMemArr(idx),
      //  numReaders=RdBgSubLineMemArrInfo.numReaders,
      //)
      //  .setName(f"rdBgSubLineMemArr_$idx")
      //for (rdIdx <- 0 until rdBgSubLineMemArr(idx).numReaders) {
      //  if (rdIdx != RdBgSubLineMemArrInfo.combineIdx) {
      //    rdBgSubLineMemArr(idx).readSync(idx=rdIdx)
      //    rdBgSubLineMemArr(idx).rdAllowedVec(rdIdx) := True
      //    rdBgSubLineMemArr(idx).enVec(rdIdx) := True
      //  }
      //}
      val objSubLineMemInitBigInt = new ArrayBuffer[BigInt]()
      for (initIdx <- 0 until params.objSubLineMemArrSize) {
        objSubLineMemInitBigInt += BigInt(0)
      }

      //objSubLineMemA2d += new ArrayBuffer[
      //  PipeSimpleDualPortMem[Vec[ObjSubLineMemEntry]]
      //]()

      //wrObjSubLineMemArr += Mem(
      //  wordType=Vec.fill(params.objTileSize2d.x)(ObjSubLineMemEntry()),
      //  wordCount=params.objSubLineMemArrSize,
      //)
      //  .initBigInt(objSubLineMemInitBigInt.toSeq)
      //  .addAttribute("ram_style", params.lineArrRamStyle)
      //  .setName(f"wrObjSubLineMemArr_$idx")
      wrObjSubLineMemArr += FpgacpuRamSimpleDualPort(
        wordType=Vec.fill(params.objTileSize2d.x)(ObjSubLineMemEntry()),
        depth=params.objSubLineMemArrSize,
        initBigInt=Some(objSubLineMemInitBigInt),
        arrRamStyle=params.lineArrRamStyle,
      )

      //for (rdIdx <- 0 until RdObjSubLineMemArrInfo.numReaders) {
      //  objSubLineMemA2d.last += PipeSimpleDualPortMem(
      //    wordType=Vec.fill(params.objTileSize2d.x)(ObjSubLineMemEntry()),
      //    depth=params.objSubLineMemArrSize,
      //    initBigInt=Some(objSubLineMemInitBigInt),
      //    latency=1,
      //    arrRamStyle=params.lineArrRamStyle,
      //  )
      //}
      combineObjSubLineMemArr += WrPulseRdPipeSimpleDualPortMem(
        //dataType=HardType.union(
        //  WrObjPipePayload(),
        //  CombinePipePayload(),
        //),
        dataType=CombinePipePayload(),
        wordType=Vec.fill(params.objTileSize2d.x)(ObjSubLineMemEntry()),
        depth=params.objSubLineMemArrSize,
        initBigInt=Some(objSubLineMemInitBigInt),
      )(
        //getWordFunc=(
        //  inpPayload: Bits,
        //) => {
        //  val ret = Vec.fill(params.objTileSize2d.x)(ObjSubLineMemEntry())
        //  switch (rWrLineMemArrIdx) {
        //    for (jdx <- 0 until params.numLineMemsPerBgObjRenderer) {
        //      is (jdx) {
        //        if (idx == jdx) {
        //          val tempInpData = (
        //            inpPayload.aliasAs(WrObjPipePayload())
        //          )
        //          //ret := tempInpData.stage5.rdSubLineMemEntry
        //          ret := tempInpData.stage7.ext.wrLineMemEntry
        //        } else {
        //          val tempInpData = (
        //            inpPayload.aliasAs(CombinePipePayload())
        //          )
        //          //ret := tempInpData.stage2.rdObj
        //          //ret := cloneOf(ret).getZero
        //          ret := ret.getZero
        //        }
        //      }
        //    }
        //    //is (0) {
        //    //  if (idx == 0) {
        //    //    val tempInpData = inpPayload.aliasAs(WrObjPipePayload())
        //    //  } else { // if (idx == 1)
        //    //    val tempInpData = inpPayload
        //    //  }
        //    //}
        //    //is (1) {
        //    //  if (idx == 0) {
        //    //  } else { // if (idx == 1)
        //    //  }
        //    //}
        //  }
        //  ret
        //},
        setWordFunc=(
          //io: WrPulseRdPipeSimpleDualPortMemIo[
          //  Bits,
          //  Vec[ObjSubLineMemEntry],
          //],
          unionIdx: UInt,
          outpPayload: CombinePipePayload,
          inpPayload: CombinePipePayload,
          objTileRow: Vec[ObjSubLineMemEntry],
        ) => {
          //switch (
          //  //rWrLineMemArrIdx
          //  unionIdx
          //) {
            //for (jdx <- 0 until params.numLineMemsPerBgObjRenderer) {
              //is (jdx) {
              //  if (idx == jdx) {
              //    val tempOutpData = (
              //      outpPayload.aliasAs(WrObjPipePayload())
              //    )
              //    val tempInpData = (
              //      inpPayload.aliasAs(WrObjPipePayload())
              //    )
              //    //ret := tempInpData.stage7.ext.wrLineMemEntry
              //    tempOutpData := tempInpData
              //    //tempOutpData.allowOverride
              //    tempOutpData.stage5.rdSubLineMemEntry.removeAssignments()
              //    tempOutpData.stage5.rdSubLineMemEntry := objTileRow
              //  } else {
                  //val tempOutpData = (
                  //  outpPayload.aliasAs(CombinePipePayload())
                  //)
                  //val tempInpData = (
                  //  inpPayload.aliasAs(CombinePipePayload())
                  //)
                  //ret := ret.getZero
                  //outpPayload := inpPayload
                  ////tempOutpData.allowOverride
                  ////outpPayload.stage2.rdObj.removeAssignments()
                  //outpPayload.allowOverride
                  outpPayload.stage2.rdObj := objTileRow
                //}
              //}
            //}
          //}
        }
      )
        .setName(f"combineObjSubLineMemArr_$idx")

      //objSubLineMemArr += Mem(
      //  //wordType=Rgb(params.rgbConfig),
      //  wordType=Vec.fill(params.objTileSize2d.x)(ObjSubLineMemEntry()),
      //  wordCount=params.objSubLineMemArrSize,
      //)
      //  .initBigInt(
      //    //Array.fill(params.oneLineMemSize)(BigInt(0)).toSeq
      //    Array.fill(params.objSubLineMemArrSize)(BigInt(0)).toSeq
      //  )
      //  .addAttribute("ram_style", params.lineArrRamStyle)
      //
      //  // true dual-port RAM;
      //  // needed because of clearing one of the non-active RAMs during
      //  // the combine pipeline
      //  // while also writing actual data to another RAM during the OBJ
      //  // writing pipeline (though the OBJ writing pipeline will
      //  // do two writes per sprite because of the grid structure)
      //  //.addAttribute("ram_mode", "tdp")
      //
      //  .setName(f"objLineMemArr_$idx")
      //rdObjSubLineMemArr += MultiMemReadSync(
      //  someMem=objSubLineMemArr(idx),
      //  numReaders=RdObjSubLineMemArrInfo.numReaders,
      //)
      //  .setName(f"rdObjSubLineMemArr_$idx")
      //for (rdIdx <- 0 until rdObjSubLineMemArr(idx).numReaders) {
      //  //if (rdIdx != RdObjSubLineMemArrInfo.combineIdx) {
      //    rdObjSubLineMemArr(idx).readSync(idx=rdIdx)
      //    //rdObjSubLineMemArr(idx).rdAllowedVec(rdIdx) := True
      //    rdObjSubLineMemArr(idx).enVec(rdIdx) := True
      //  //}
      //}
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
    //val dbgObjSubLineMemVec = Reg(
    //  Vec.fill(params.numLineMemsPerBgObjRenderer)(
    //    Vec.fill(params.oneLineMemSize >> params.objTileSize2dPow.x)(
    //      Vec.fill(params.objTileSize2d.x)(
    //        ObjSubLineMemEntry()
    //      )
    //    )
    //  )
    //)
    //for (jdx <- 0 until dbgObjSubLineMemVec.size) {
    //  for (kdx <- 0 until dbgObjSubLineMemVec(jdx).size) {
    //    for (idx <- 0 until dbgObjSubLineMemVec(jdx)(kdx).size) {
    //      dbgObjSubLineMemVec(jdx)(kdx)(idx) := (
    //        ObjSubLineMemEntry().getZero
    //      )
    //    }
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
    case class ObjSubLineMemWriter() extends Area {
      def wrIdx = 0
      def combineIdx = 1
      val addrVec = Vec.fill(params.numLineMemsPerBgObjRenderer)(
        Reg(UInt(log2Up(params.objSubLineMemArrSize) bits)) init(0x0)
      )
      val dataVec = Vec.fill(params.numLineMemsPerBgObjRenderer)(
        Vec.fill(params.objTileSize2d.x)(
          Reg(ObjSubLineMemEntry()) init(ObjSubLineMemEntry().getZero)
        )
      )
      val enVec = Vec.fill(params.numLineMemsPerBgObjRenderer)(
        Reg(Bool()) init(False)
      )
      //val combineErase = Bool()

      def doWrite(
        //wrIdx: Int
      ): Unit = {

        //def tempWrLineMemArrIdx = wrIdx % 2
        //def tempCombineLineMemArrIdx = (wrIdx + 1) % 2

        //objSubLineMemArr(wrIdx) {
        //}
        //when (objWriter.enVec(tempWrLineMemArrIdx)) {

        //objSubLineMemArr(tempWrLineMemArrIdx).write(
        //  address=objWriter.addrVec(tempWrLineMemArrIdx),
        //  data=objWriter.dataVec(tempWrLineMemArrIdx),
        //  enable=objWriter.enVec(tempWrLineMemArrIdx),
        //)
        //objSubLineMemArr(tempCombineLineMemArrIdx).write(
        //  address=objWriter.addrVec(tempCombineLineMemArrIdx),
        //  data=objWriter.dataVec(tempCombineLineMemArrIdx),
        //  enable=objWriter.enVec(tempCombineLineMemArrIdx),
        //)
        for (jdx <- 0 until params.numLineMemsPerBgObjRenderer) {
          //def vecIdx0 = (rWrLineMemArrIdx + jdx)(0 downto 0)
          val vecIdx = UInt(1 bits)
            .setName(f"rObjWriter_doWrite_vecIdx_$jdx")
          vecIdx := (
            (rWrLineMemArrIdx + jdx)(0 downto 0)
            //(rWrLineMemArrIdx + jdx + 1)(0 downto 0)
          )
          val tempAddr = cloneOf(addrVec(jdx))
            .setName(f"rObjWriter_doWrite_tempAddr_$jdx")
          val tempData = cloneOf(dataVec(jdx))
            .setName(f"rObjWriter_doWrite_tempData_$jdx")
          val tempEn = cloneOf(enVec(jdx))
            .setName(f"rObjWriter_doWrite_tempEn_$jdx")
          tempAddr := (
            //Mux[UInt](
            //  rWrLineMemArrIdx === jdx,
            //  addrVec(jdx)
            //  //addrVec(vecIdx0),
            //  //addrVec(vecIdx1),
            //)
            addrVec(vecIdx)
            //addrVec(jdx)
            //addrVec(wrIdx)
            //addrVec(rWrLineMemArrIdx)
          )
          tempData := (
            //Mux[Vec[ObjSubLineMemEntry]](
            //  rWrLineMemArrIdx === jdx,
            //  dataVec(vecIdx0),
            //  dataVec(vecIdx1),
            //)
            dataVec(vecIdx)
            //dataVec(jdx)
            //dataVec(wrIdx)
            //dataVec(rWrLineMemArrIdx)
          )
          tempEn := (
            //Mux[Bool](
            //  rWrLineMemArrIdx === jdx,
            //  enVec(vecIdx0),
            //  enVec(vecIdx1),
            //)
            enVec(vecIdx)
            //enVec(jdx)
          )
          combineObjSubLineMemArr(jdx).io.unionIdx := rWrLineMemArrIdx
          combineObjSubLineMemArr(jdx).io.wrPulse.valid := (
            //objWriter.enVec(vecIdx)
            tempEn
          )
          combineObjSubLineMemArr(jdx).io.wrPulse.addr := (
            //objWriter.addrVec(vecIdx)
            tempAddr
          )
          combineObjSubLineMemArr(jdx).io.wrPulse.data := (
            //objWriter.dataVec(vecIdx)
            tempData
          )
          //wrObjSubLineMemArr(jdx).write(
          //  address=tempAddr,
          //  data=tempData,
          //  enable=tempEn,
          //)
          wrObjSubLineMemArr(jdx).io.wrEn := tempEn
          wrObjSubLineMemArr(jdx).io.wrAddr := tempAddr
          wrObjSubLineMemArr(jdx).io.wrData := tempData

          //objSubLineMemArr(jdx).io.wrPulse.valid := tempEn
          //objSubLineMemArr(jdx).io.wrPulse.addr := tempAddr
          //objSubLineMemArr(jdx).io.wrPulse.data := tempData

          //when (rWrLineMemArrIdx === 0) {
          //  def tempIdx = jdx
          //  //if (jdx == 0) {
          //  //  tempAddr := addrVec(0)
          //  //} else { // if (jdx == 1)
          //  //  tempAddr := addrVec(1)
          //  //}
          //  tempAddr := addrVec(tempIdx)
          //  tempData := dataVec(tempIdx)
          //  tempEn := enVec(tempIdx)
          //} otherwise { // when (rWrLineMemArrIdx === 1)
          //  def tempIdx = jdx
          //  //def tempIdx = (
          //  //  if (jdx == 0) {
          //  //    //tempAddr := addrVec(1)
          //  //    1
          //  //  } else { // if (jdx == 1)
          //  //    //tempAddr := addrVec(0)
          //  //    0
          //  //  }
          //  //)
          //  tempAddr := addrVec(tempIdx)
          //  tempData := dataVec(tempIdx)
          //  tempEn := enVec(tempIdx)
          //}
          //when (rWrLineMemArrIdx === jdx) {
            //objSubLineMemArr(jdx).write(
            //  address=tempAddr,
            //  data=tempData,
            //)
            //--------
            //objSubLineMemArr(jdx).write(
            //  address=addrVec(jdx),
            //  data=dataVec(jdx),
            //  enable=enVec(jdx),
            //)
            //--------

            //--------
            //when (enVec(jdx)) {
            //  dbgObjSubLineMemVec(jdx)(addrVec(jdx)) := dataVec(jdx)
            //}
            //--------
          //}
          //when (rWrLineMemArrIdx === jdx) {
            //objSubLineMemArr(jdx).write(
            //  address=tempAddr,
            //  //Mux[UInt](
            //  //  rWrLineMemArrIdx === jdx,
            //  //  addrVec(jdx),
            //  //  addrVec((jdx + 1) % 2),
            //  //)
            //  data=tempData,
            //  //Mux[Vec[ObjSubLineMemEntry]](
            //  //  rWrLineMemArrIdx === jdx,
            //  //  dataVec(jdx),
            //  //  dataVec((jdx + 1) % 2),
            //  //)
            //  //enable=tempEn
            //  //enable=True
            //  //Mux[Bool](
            //  //  rWrLineMemArrIdx === jdx,
            //  //  enVec(jdx),
            //  //  enVec((jdx + 1) % 2),
            //  //),
            //  //,
            //)
          //}
        }

        //}
        //when (objWriter.enVec(tempCombineLineMemArrIdx)) {
        //}
      }
    }
    val objWriter = ObjSubLineMemWriter()
    //for (jdx <- 0 until objSubLineMemArr.size) {
    //  //def vecIdx0 = (rWrLineMemArrIdx + jdx)(0 downto 0)
    //  def vecIdx = (
    //    (rWrLineMemArrIdx + jdx)(0 downto 0)
    //    //(rWrLineMemArrIdx + jdx + 1)(0 downto 0)
    //  )
    //  //val tempAddr = cloneOf(objWriter.addrVec(jdx))
    //  //  .setName(f"rObjWriter_doWrite_tempAddr_$jdx")
    //  //val tempData = cloneOf(objWriter.dataVec(jdx))
    //  //  .setName(f"rObjWriter_doWrite_tempData_$jdx")
    //  //val tempEn = cloneOf(objWriter.enVec(jdx))
    //  //  .setName(f"rObjWriter_doWrite_tempEn_$jdx")
    //  //tempAddr := (
    //  //  //Mux[UInt](
    //  //  //  rWrLineMemArrIdx === jdx,
    //  //  //  addrVec(jdx)
    //  //  //  //addrVec(vecIdx0),
    //  //  //  //addrVec(vecIdx1),
    //  //  //)
    //  //  objWriter.addrVec(vecIdx)
    //  //  //addrVec(wrIdx)
    //  //  //addrVec(rWrLineMemArrIdx)
    //  //)
    //  //tempData := (
    //  //  //Mux[Vec[ObjSubLineMemEntry]](
    //  //  //  rWrLineMemArrIdx === jdx,
    //  //  //  dataVec(vecIdx0),
    //  //  //  dataVec(vecIdx1),
    //  //  //)
    //  //  objWriter.dataVec(vecIdx)
    //  //  //dataVec(wrIdx)
    //  //  //dataVec(rWrLineMemArrIdx)
    //  //)
    //  //tempEn := (
    //  //  //Mux[Bool](
    //  //  //  rWrLineMemArrIdx === jdx,
    //  //  //  enVec(vecIdx0),
    //  //  //  enVec(vecIdx1),
    //  //  //)
    //  //  objWriter.enVec(vecIdx)
    //  //)

    //  objSubLineMemArr(jdx).io.unionIdx := rWrLineMemArrIdx
    //  objSubLineMemArr(jdx).io.wrPulse.valid := objWriter.enVec(vecIdx)
    //  objSubLineMemArr(jdx).io.wrPulse.addr := objWriter.addrVec(vecIdx)
    //  objSubLineMemArr(jdx).io.wrPulse.data := objWriter.dataVec(vecIdx)
    //}
    //objWriter.init(objWriter.getZero)
    objWriter.doWrite()
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
      case class Stage3() extends Bundle {
        // `Gpu2dBgEntry`s that have been read
        val bgEntry = Vec.fill(params.bgTileSize2d.x)(
          Gpu2dBgEntry(params=params)
        )
      }
      case class Stage5() extends Bundle {
        val tilePxsCoord = Vec.fill(params.bgTileSize2d.x)(
          params.bgTilePxsCoordT()
        )
        // `Gpu2dTile`s that have been read
        val tile = Vec.fill(params.bgTileSize2d.x)(
          Gpu2dTile(params=params, isObj=false)
        )
      }
      case class Stage6() extends Bundle {
        val palEntryMemIdx = Vec.fill(params.bgTileSize2d.x)(
          UInt(params.objPalEntryMemIdxWidth bits)
        )
      }
      case class Stage7() extends Bundle {
        // Whether `palEntryMemIdx(someBgIdx)` is non-zero
        val palEntryNzMemIdx = Vec.fill(params.bgTileSize2d.x)(
          Bool()
        )
      }
      // The following BG pipeline stages are only performed when
      // `palEntryNzMemIdx(someBgIdx)` is `True`
      // `Gpu2dPalEntry`s that have been read
      case class Stage9() extends Bundle {
        val palEntry = Vec.fill(params.bgTileSize2d.x)(
          Gpu2dPalEntry(params=params)
        )
      }
      case class Stage10() extends Bundle {
        //val doWrite = Bool()
        val subLineMemEntry = Vec.fill(params.bgTileSize2d.x)(
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
        val stage3 = Stage3()
        //val tileMemIdx = UInt(params.bgTileMemIdxWidth bits)
        val stage5 = Stage5()
        val stage6 = Stage6()
        val stage7 = Stage7()
        val stage9 = Stage9()
        val stage10 = Stage10()
      }
      val postStage0 = PostStage0()

      //def scroll = postStage0.scroll
      def stage1 = postStage0.stage1
      def bgEntryMemIdx = stage1.bgEntryMemIdx
      def pxPos = stage1.pxPos
      //def bgEntryMemIdxPart0 = postStage0.bgEntryMemIdxPart0
      //def bgEntryMemIdxPart1 = postStage0.bgEntryMemIdxPart1

      def stage3 = postStage0.stage3
      def bgEntry = stage3.bgEntry

      def stage5 = postStage0.stage5
      def tilePxsCoord = stage5.tilePxsCoord
      def tile = stage5.tile

      //def stage4 = postStage0.stage4

      def stage6 = postStage0.stage6
      def palEntryMemIdx = stage6.palEntryMemIdx

      def stage7 = postStage0.stage7
      def palEntryNzMemIdx = stage7.palEntryNzMemIdx

      def stage9 = postStage0.stage9
      def palEntry = stage9.palEntry

      def stage10 = postStage0.stage10
      //def doWrite = stage7.doWrite
      def subLineMemEntry = stage10.subLineMemEntry
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
    def wrBgPipeNumMainStages = 11
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
      //8
      //9
      //10
      //11
      12
    )
    def wrBgObjPipeNumStages = max(
      wrBgPipeNumMainStages,
      wrObjPipeNumMainStages
    ) //+ 1

    //def wrObjPipeStage6NumFwd = 3
    //def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6
    //def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6 + 1
    //def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6 + 1 + 1
    //def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6 + 1 + 1
    //def wrObjPipeStage9NumFwd = wrBgObjPipeNumStages - 9 + 1 + 1
    //def wrObjPipeStage9NumFwd = wrBgObjPipeNumStages - 9 + 1 + 1
    def wrObjPipeStage11NumFwd = wrBgObjPipeNumStages - 11 + 1 + 1
    //def wrObjPipeStage6NumFwd = 2
    //def wrObjPipeStage6NumFwd = 1
    case class WrObjPipeStage11Fwd() extends Bundle {
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
      //val overwriteLineMemEntry = Bool()
      //val wrLineMemEntry = ObjSubLineMemEntry()
      //val extSingle = WrObjPipe6ExtSingle()
      //val ext = WrObjPipe9Ext(useVec=false)
      val ext = WrObjPipe11Ext(useVec=false)
      //val wholeWrLineMemEntry = Vec.fill(params.objTileSize2d.x)(
      //  ObjSubLineMemEntry()
      //)
      def overwriteLineMemEntry = ext.overwriteLineMemEntry
      def wrLineMemEntry = ext.wrLineMemEntry
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
        def calcGridIdxLsb() = (
          //cnt
          cnt(
            // support needing to do two writes into `objSubLineMemArr`
            //0
            params.objAttrsMemIdxWidth + 1 - 1
            //downto params.objAttrsMemIdxWidth + 1 - 1
          )
        )

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
      case class Stage2() extends Bundle {
        // What are the `Gpu2dObjAttrs` of our sprite? 
        val objAttrs = Gpu2dObjAttrs(params=params)
      }
      case class Stage4() extends Bundle {
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
      case class Stage5() extends Bundle {
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
      case class Stage6() extends Bundle {
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

      case class Stage8() extends Bundle {
        val palEntry = Vec.fill(params.objTileSize2d.x)(
          Gpu2dPalEntry(params=params)
        )
        val pxPosInLine = Vec.fill(params.objTileSize2d.x)(Bool())
        val pxPosCmpForOverwrite = Vec.fill(params.objTileSize2d.x)(Bool())
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
        //--------
        // "rd..." here means it's been read from `objSubLineMemArr`
        //val rdSubLineMemEntry = Vec.fill(params.objTileSize2d.x)(
        //  ObjSubLineMemEntry()
        //)
        //--------
        //val dbgRdLineMemEntry = ObjSubLineMemEntry()
      }
      //case class Stage5Fwd() extends Bundle {
      //  val pastStage2 = Stage2()
      //  val pastRdLineMemEntry = ObjSubLineMemEntry()
      //}
      //case class Stage6() extends Bundle {
      //  //val rdSubLineMemEntry = ObjSubLineMemEntry()
      //  val overwriteLineMemEntry = Bool()
      //  //val pxPosXIsSame = Bool()
      //  //val pxPosIsSame = Bool()
      //  //val pxPosIsSame = Vec2(Bool())


      //  //def numFwd = wrObjPipeStage6NumFwd
      //  //val fwdVec = Vec.fill(numFwd)(WrObjPipeStage6Fwd())

      //  //val pxPosXSameAsPrevious = Bool()
      //  //val prevPxPosX = SInt(params.objPxsCoordSize2dPow.x bits)
      //}
      //case class Stage6() extends Bundle {
      //}
      case class Stage10() extends Bundle {
        // "rd..." here means it's been read from `objSubLineMemArr`
        val rdSubLineMemEntry = Vec.fill(params.objTileSize2d.x)(
          ObjSubLineMemEntry()
        )
      }

      // This is similar to a strictly in-order CPU pipeline's
      // ALU operand forwarding
      //def wrObjPipeStage5NumFwd
      //def stage7NumFwd = 2
      case class Stage11() extends Bundle {
        //val overwriteLineMemEntry = Vec.fill(params.objTileSize2d.x)(
        //  Bool()
        //)
        ////def numFwd = stage6NumFwd
        //val wrLineMemEntry = Vec.fill(params.objTileSize2d.x)(
        //  ObjSubLineMemEntry()
        //)
        val ext = WrObjPipe11Ext(useVec=true)

        def numFwd = wrObjPipeStage11NumFwd
        val fwdV2d = Vec.fill(params.objTileSize2d.x)(
          Vec.fill(numFwd)(WrObjPipeStage11Fwd())
        )
        //val fwdWrLineMemEntryV2d = Vec.fill(numFwd)(
        //  Vec.fill(params.objTileSize2d.x)(
        //    ObjSubLineMemEntry()
        //  )
        //)
        //val savedWrLineMemEntryVec = Vec.fill(numFwd)(ObjSubLineMemEntry())

        //def numSaved = 3
        //val savedWrLineMemEntryVec = Vec.fill(numSaved)(ObjSubLineMemEntry())
      }
      //case class Stage7() extends Bundle {
      //  //val ext = WrObjPipeOut6ExtData()
      //  val ext = WrObjPipe6Ext()
      //}
      case class PostStage0() extends Bundle {
        val stage2 = Stage2()
        val stage4 = Stage4()
        val stage5 = Stage5()
        val stage6 = Stage6()
        val stage8 = Stage8()
        //val stage6 = Stage6()
        //val stage7 = Stage7()
        val stage10 = Stage10()
        val stage11 = Stage11()
        //val stage7 = Stage7()
      }

      val stage0 = Stage0()
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

      def stage2 = postStage0.stage2
      def objAttrs = stage2.objAttrs

      def stage4 = postStage0.stage4
      //def tilePxsCoordYPipe1 = stage4.tilePxsCoordYPipe1
      //def pxsCoordYRangeCheckPipe1 = stage4.pxsCoordYRangeCheckPipe1
      def tilePxsCoord = stage4.tilePxsCoord
      //def pxPosRangeCheckPipe1 = stage4.pxPosRangeCheckPipe1
      def pxPos = stage4.pxPos

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
      def objPosYShift = stage4.objPosYShift
      def tile = stage4.tile

      def stage5 = postStage0.stage5
      def pxPosXGridIdx = stage5.pxPosXGridIdx
      def pxPosXGridIdxLsb = stage5.pxPosXGridIdxLsb
      def pxPosXGridIdxFindFirstSameAsFound = (
        stage5.pxPosXGridIdxFindFirstSameAsFound
      )
      def pxPosXGridIdxFindFirstSameAsIdx = (
        stage5.pxPosXGridIdxFindFirstSameAsIdx
      )
      def pxPosXGridIdxFindFirstDiffIdx = (
        stage5.pxPosXGridIdxFindFirstDiffIdx
      )
      def pxPosXGridIdxFindFirstDiffFound = (
        stage5.pxPosXGridIdxFindFirstDiffFound
      )
      //def pxPosRangeCheck = stage5.pxPosRangeCheck
      //def tilePxsCoord = stage5.tilePxsCoord
      def pxPosRangeCheckGePipe1 = stage5.pxPosRangeCheckGePipe1
      def pxPosRangeCheckLtPipe1 = stage5.pxPosRangeCheckLtPipe1
      def palEntryMemIdx = stage5.palEntryMemIdx

      def stage6 = postStage0.stage6
      //def pxPosXGridIdxFlip = stage6.pxPosXGridIdxFlip
      def pxPosXGridIdxMatches = stage6.pxPosXGridIdxMatches
      //def oldPxPosInLineCheckGePipe1 = stage6.oldPxPosInLineCheckGePipe1
      //def oldPxPosInLineCheckLtPipe1 = stage6.oldPxPosInLineCheckLtPipe1
      //def palEntryMemIdx = stage6.palEntryMemIdx
      def palEntryNzMemIdx = stage6.palEntryNzMemIdx
      def pxPosRangeCheck = stage6.pxPosRangeCheck

      def stage8 = postStage0.stage8
      //def palEntryNzMemIdx = stage8.palEntryNzMemIdx
      //def oldPxPosInLineCheck = stage8.oldPxPosInLineCheck
      def palEntry = stage8.palEntry
      //def pxPosXConcat = stage8.pxPosXConcat
      //def pxPosConcat = stage8.pxPosConcat
      def pxPosInLine = stage8.pxPosInLine
      def pxPosCmpForOverwrite = stage8.pxPosCmpForOverwrite
      //def pxPosXChangingGridIdx = stage8.pxPosXChangingGridIdx
      //--------
      //def rdSubLineMemEntry = stage5.rdSubLineMemEntry
      //--------
      //def dbgRdLineMemEntry = stage5.dbgRdLineMemEntry

      //def stage6 = postStage0.stage6

      def stage10 = postStage0.stage10
      def rdSubLineMemEntry = stage10.rdSubLineMemEntry

      def stage11 = postStage0.stage11
      def overwriteLineMemEntry = stage11.ext.overwriteLineMemEntry
      def wrLineMemEntry = stage11.ext.wrLineMemEntry
      //def overwriteLineMemEntry = stage6.overwriteLineMemEntry
      ////def pxPosXIsSame = stage6.pxPosXIsSame
      ////def pxPosIsSame = stage6.pxPosIsSame
      ////def rdSubLineMemEntry = stage6.rdSubLineMemEntry

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
      ////def rdSubLineMemEntry = stage6.rdSubLineMemEntry

      ////def stage7 = postStage0.stage7
      ////def wrLineMemEntry = stage7.wrLineMemEntry

      ////def rdSubLineMemEntry = postStage0.rdSubLineMemEntry
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
    case class CombinePipeOut2Ext() extends Bundle {
      val bgRdSubLineMemEntry = BgSubLineMemEntry()
      val objRdSubLineMemEntry = ObjSubLineMemEntry()
    }
    //val rCombinePipeOut1Ext = Reg(CombinePipeOut1Ext())
    //rCombinePipeOut1Ext.init(rCombinePipeOut1Ext.getZero)
    def combinePipeNumMainStages = (
      //5
      //6
      //7
      8
    )
    //def combinePipeNumMainStages = wrBgObjPipeNumStages + 5
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
      def lineMemIdx = stage0.cnt
      def bakCnt = stage0.bakCnt
      def bakCntMinus1 = stage0.bakCntMinus1
      def bakCntWillBeDone() = stage0.bakCntWillBeDone()

      case class Stage2() extends Bundle {
        //val bgRdLineMemEntry = BgSubLineMemEntry()
        //val objRdLineMemEntry = ObjSubLineMemEntry()
        //val ext = CombinePipeOut1Ext()
        val rdBg = Vec.fill(params.bgTileSize2d.x)(
          BgSubLineMemEntry()
        )
        val rdObj = Vec.fill(params.objTileSize2d.x)(
          ObjSubLineMemEntry()
        )
      }
      case class Stage3() extends Bundle {
        val ext = CombinePipeOut2Ext()
      }
      //case class Stage3() extends Bundle {
      //}
      case class Stage5() extends Bundle {
        val objHiPrio = Bool()
        //val ext = CombinePipeOut2Ext()
      }
      case class Stage6() extends Bundle {
        //val isObj = Bool()
        val col = Rgb(params.rgbConfig)
      }
      case class Stage7() extends Bundle {
        val combineWrLineMemEntry = BgSubLineMemEntry()
        //val objWrLineMemEntry = ObjSubLineMemEntry()
      }
      case class PostStage0() extends Bundle {
        val stage2 = Stage2()
        val stage3 = Stage3()
        //val stage3 = Stage3()
        val stage5 = Stage5()
        val stage6 = Stage6()
        val stage7 = Stage7()
      }
      val postStage0 = PostStage0()

      def stage2 = postStage0.stage2
      //def lineMemIdx = stage1.lineMemIdx
      //def bgRdLineMemEntry = stage1.bgRdLineMemEntry
      //def objRdLineMemEntry = stage1.objRdLineMemEntry
      def stage3 = postStage0.stage3
      //def stage3 = postStage0.stage3

      def stage5 = postStage0.stage5
      def objHiPrio = stage5.objHiPrio
      //def bgRdLineMemEntry = stage5.ext.bgRdLineMemEntry
      //def objRdLineMemEntry = stage5.ext.objRdLineMemEntry

      def stage6 = postStage0.stage6
      def col = stage6.col

      def stage7 = postStage0.stage7
      def combineWrLineMemEntry = stage7.combineWrLineMemEntry
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
    //case class WrObjPipe6ExtSingle() extends Bundle {
    //  val wrLineMemEntry = ObjSubLineMemEntry()
    //  val overwriteLineMemEntry = Bool()
    //}
    case class WrObjPipe11Ext(
      useVec: Boolean//=true
    ) extends Bundle {
      def vecSize = (
        if (useVec) {
          params.objTileSize2d.x
        } else {
          1
        }
      )
      val wrLineMemEntry = Vec.fill(vecSize)(
        ObjSubLineMemEntry()
      )
      val overwriteLineMemEntry = Vec.fill(vecSize)(
        Bool()
      )
      //def wrLineMemEntry = (
      //  if (useVec) {
      //    rawWrLineMemEntryVec
      //  } else {
      //    rawWrLineMemEntryVec(0)
      //  }
      //)
      //def overwriteLineMemEntry = (
      //  if (useVec) {
      //    rawOverwriteLineMemEntryVec
      //  } else {
      //    rawOverwriteLineMemEntryVec(0)
      //  }
      //)
    }
    //val rWrObjPipeOut6Ext = Reg(WrObjPipe6Ext(useVec=true))
    //rWrObjPipeOut6Ext.init(rWrObjPipeOut6Ext.getZero)

    //wrObjPipeOut.last.ready := (
    //  // This is a heuristic!
    //  objLineFifo.io.misc.amountCanPush
    //  > params.wrBgObjStallFifoAmountCanPush
    //)


    val wrObjPipeLast = KeepAttribute(
      //Flow(WrObjPipePayload())
      Flow(WrObjPipePayload())
    )
    //wrObjPipeLast.ready := True
    //when (nextIntnlChangingRow && rWrObjChangingRow) 
    //when (rIntnlChangingRow)
    when (intnlChangingRowRe) {
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
        //wrObjPipeIn(idx) <-/< wrObjPipeOut(idx - 1)
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
    val combineBgObjRdPipeFork = FpgacpuPipeForkBlocking(
      dataType=CombinePipePayload(),
      // BEGIN: add in BGs; later
      oSize=params.numLineMemsPerBgObjRenderer * 2,
      // END: add in BGs; later
    )
    val combineBgObjRdPipeJoin = FpgacpuPipeJoin(
      dataType=CombinePipePayload(),
      // BEGIN: add in BGs; later
      size=params.numLineMemsPerBgObjRenderer * 2,
      // END: add in BGs; later
    )

    //def combinePipe1BgObjVecSize = (
    //  params.numLineMemsPerBgObjRenderer
    //)
    //val combinePipeIn1BgVec = Vec.fill(combinePipe1BgObjVecSize)(
    //  cloneOf(combinePipeIn(1))
    //)
    //val combinePipeIn1ObjVec = Vec.fill(combinePipe1BgObjVecSize)(
    //  cloneOf(combinePipeIn(1))
    //)
    //val combinePipeOut1BgVec = Vec.fill(combinePipe1BgObjVecSize)(
    //  cloneOf(combinePipeOut(1))
    //)
    //val combinePipeOut1ObjVec = Vec.fill(combinePipe1BgObjVecSize)(
    //  cloneOf(combinePipeOut(1))
    //)

    //val combinePipeIn1BgMemReadSyncArr = new ArrayBuffer[
    //  MemReadSyncIntoPipe[
    //    CombinePipePayload,
    //    CombinePipePayload,
    //    Vec[BgSubLineMemEntry],
    //  ]
    //]()
    //val combinePipeIn1ObjMemReadSyncArr = new ArrayBuffer[
    //  MemReadSyncIntoPipe[
    //    CombinePipePayload,
    //    CombinePipePayload,
    //    Vec[ObjSubLineMemEntry],
    //  ]
    //]()

    //--------
    // The logic is identical, so we only need one `valid` or `ready`
    // signal for the below two assignments
    //combinePipeIn(1).ready := combinePipeIn1BgVec(
    //  rCombineLineMemArrIdx
    //).ready
    //combinePipeOut(1).valid := combinePipeOut1BgVec(
    //  rCombineLineMemArrIdx
    //).valid
    //--------

    //combinePipeOut(1).payload := combinePipeIn(1).payload
    //combinePipeOut(1).payload.allowOverride

    //for (jdx <- 0 until combinePipe1BgObjVecSize) {
    //  combinePipeIn1BgVec(jdx).valid := combinePipeIn(1).valid
    //  combinePipeIn1ObjVec(jdx).valid := combinePipeIn(1).valid

    //  combinePipeIn1BgVec(jdx).payload := combinePipeIn(1).payload
    //  combinePipeIn1ObjVec(jdx).payload := combinePipeIn(1).payload

    //  combinePipeOut1BgVec(jdx).payload := combinePipeIn(1).payload
    //  combinePipeOut1BgVec(jdx).payload.allowOverride
    //  combinePipeOut1ObjVec(jdx).payload := combinePipeIn(1).payload
    //  combinePipeOut1ObjVec(jdx).payload.allowOverride

    //  combinePipeOut1BgVec(jdx).ready := combinePipeOut(1).ready
    //  combinePipeOut1ObjVec(jdx).ready := combinePipeOut(1).ready

    //  //combinePipeIn1BgMemReadSyncArr += MemReadSyncIntoPipe(
    //  //  //pipeIn=combinePipeIn(1),
    //  //  pipeIn=combinePipeIn1BgVec(jdx),
    //  //  //inpAddr=(
    //  //  //  params.getBgSubLineMemArrIdx(
    //  //  //    addr=(
    //  //  //      combinePipeIn1BgVec(jdx).cnt
    //  //  //      //combinePipeIn(1)
    //  //  //      //combinePipeIn1BgVec(jdx).cnt
    //  //  //      (
    //  //  //        log2Up(params.oneLineMemSize) - 1 downto 0
    //  //  //      ),
    //  //  //    )
    //  //  //  )
    //  //  //),
    //  //  pipeOut=combinePipeOut1BgVec(jdx),
    //  //  //outpRdData=combinePipeOut1BgVec(jdx).stage2.rdBg,
    //  //  //multiRd=rdBgSubLineMemArr(jdx),
    //  //  //rdIdx=RdBgSubLineMemArrInfo.combineIdx,
    //  //  someMem=bgSubLineMemArr(jdx),
    //  //)(
    //  //  getInpAddrFunc=(
    //  //    payload => params.getBgSubLineMemArrIdx(
    //  //      addr=payload.cnt(
    //  //        log2Up(params.oneLineMemSize) - 1 downto 0
    //  //      )
    //  //    )
    //  //  ),
    //  //  getOutpRdDataFunc=(
    //  //    payload => payload.stage2.rdBg
    //  //  )
    //  //)
    //  //  .setName(f"combinePipeIn1BgMemReadSyncArr_$jdx")
    //  //rdBgSubLineMemArr(jdx).rdAllowedVec(
    //  //  RdBgSubLineMemArrInfo.combineIdx,
    //  //) := True

    //  //combinePipeIn1ObjMemReadSyncArr += MemReadSyncIntoPipe(
    //  //  //pipeIn=combinePipeIn(1),
    //  //  pipeIn=combinePipeIn1ObjVec(jdx),
    //  //  //inpAddr=combinePipeIn1Obj.lineMemIdx,
    //  //  //inpAddr=params.getObjSubLineMemArrIdx(
    //  //  //  addr=(
    //  //  //    //combinePipeIn1ObjVec(jdx).cnt
    //  //  //    combinePipeIn(1).cnt
    //  //  //    (
    //  //  //      log2Up(params.oneLineMemSize) - 1 downto 0
    //  //  //    ),
    //  //  //  )
    //  //  //),
    //  //  pipeOut=combinePipeOut1ObjVec(jdx),
    //  //  //outpRdData=combinePipeOut1ObjVec(jdx).stage2.rdObj,
    //  //  //multiRd=rdObjSubLineMemArr(jdx),
    //  //  //rdIdx=RdObjSubLineMemArrInfo.combineIdx,
    //  //  someMem=objSubLineMemArr(jdx),
    //  //)(
    //  //  getInpAddrFunc=(
    //  //    payload => params.getObjSubLineMemArrIdx(
    //  //      addr=payload.cnt(
    //  //        log2Up(params.oneLineMemSize) - 1 downto 0
    //  //      )
    //  //    )
    //  //  ),
    //  //  getOutpRdDataFunc=(
    //  //    payload => payload.stage2.rdObj
    //  //  )
    //  //)
    //  //  .setName(f"combinePipeIn1ObjMemReadSyncArr_$jdx")

    //  //rdObjSubLineMemArr(jdx).rdAllowedVec(
    //  //  RdObjSubLineMemArrInfo.combineIdx,
    //  //) := True
    //}
    //switch (rCombineLineMemArrIdx) {
    //  for (jdx <- 0 until combinePipe1BgObjVecSize) {
    //    is (jdx) {
    //      combinePipeOut(1).stage2.rdBg := (
    //        combinePipeOut1BgVec(jdx).stage2.rdBg
    //      )
    //      combinePipeOut(1).stage2.rdObj := (
    //        combinePipeOut1ObjVec(jdx).stage2.rdObj
    //      )
    //    }
    //  }
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

    val haltCombinePipeVeryFront = Bool()
    val combinePipeInVeryFront = Stream(CombinePipePayload())
    val combinePipeInVeryFrontHaltWhen = combinePipeInVeryFront.haltWhen(
      haltCombinePipeVeryFront
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
    //val psbCombinePipe2 = PipeSkidBuf(
    //  dataType=CombinePipePayload(),
    //  optIncludeBusy=true,
    //)
    //psbCombinePipe2.io.misc.busy := haltCombinePipe2

    //--------
    // you need to log that data is "in flight" from the ram, and if there 
    // is backpressure. If there is backpressure from your output
    // skid buffer and there is data in flight, then the pipeline is "full"
    //--------
    // you can use that to then backpressure the main pipeline
    //--------
    // it will depend on what you're doing, but should work something
    // like this
    //--------
    // if you have a fifo you can use the "almost_full" signal for this
    // purpose
    //--------
    // the almost full is set so that it can always absorb all the data
    // from the pipeline
    //--------
    //def combinePipe2FifoDepth = (
    //  //8
    //  3
    //)
    //def combinePipe2FifoHaltWhenGeAmountCanPop = (
    //  //4
    //  1
    //)
    //case class CombinePipe2FifoWordType() extends Bundle {
    //  val bgRd = BgSubLineMemEntry()
    //  val objRd = ObjSubLineMemEntry()
    //}
    //val combinePipe2Fifo = AsyncReadFifo(
    //  dataType=CombinePipe2FifoWordType(),
    //  depth=combinePipe2FifoDepth,
    //)

    //val myCombinePipe2FifoPush = cloneOf(combinePipe2Fifo.io.push)
    ////val myHaltCombinePipe2FifoPush = Bool()
    //combinePipe2Fifo.io.push << myCombinePipe2FifoPush.haltWhen(
    //  //myHaltCombinePipe2FifoPush
    //  haltCombinePipe2
    //)
    //myCombinePipe2FifoPush.valid := True
    //
    //val myCombinePipe2FifoPop = cloneOf(combinePipe2Fifo.io.pop)
    //val myHaltCombinePipe2FifoPop = Bool()
    //myCombinePipe2FifoPop << combinePipe2Fifo.io.pop.haltWhen(
    //  myHaltCombinePipe2FifoPop
    //)
    //myHaltCombinePipe2FifoPop := combinePipeIn(2).ready
    //myCombinePipe2FifoPop.ready := True

    //haltCombinePipe2 := (
    //  combinePipe2Fifo.io.misc.amountCanPop 
    //  >= combinePipe2FifoHaltWhenGeAmountCanPop
    //)
    //val anyCombinePipe2BgMemRdArrHaltPipeVec = Vec.fill(
    //  rdBgSubLineMemArr.size
    //)(Bool())
    //val anyCombinePipe2ObjMemRdArrHaltPipeVec = Vec.fill(
    //  rdObjSubLineMemArr.size
    //)(Bool())
    //val combinePipe2BgHaltPipeVec = Vec.fill(rdBgSubLineMemArr.size)(
    //  Bool()
    //)
    //def combinePipe2BgHaltVecsStartIdx = 0
    //def combinePipe2ObjHaltVecsStartIdx = (
    //  RdBgSubLineMemArrInfo.numReaders * 
    //)
    //object CombinePipe2HaltVecsInfo {
    //  def bgIdx = 0
    //  def objIdx = 1
    //  def size = 2
    //}
    ////val combinePipe2HaltV2d = Vec.fill(CombinePipe2HaltVecsInfo.numElems)(
    ////  MemReadSyncIntoStreamHaltVecs(
    ////    size=params.numLineMemsPerBgObjRenderer
    ////  )
    ////)
    //val combinePipe2HaltVecs = MemReadSyncIntoStreamHaltVecs(
    //  size=CombinePipe2HaltVecsInfo.size
    //)
    //// We only need to check one of the rows because the memory readers
    //// should all be synched up
    //haltCombinePipe2 := (
    //  //combinePipe2HaltV2d(CombinePipe2HaltV2dInfo.bgIdx).reducePipe()
    //  combinePipe2HaltV2
    //)

    //val combinePipe2BgMemRdArr = (
    //  new ArrayBuffer[MemReadSyncIntoStream[
    //    //CombinePipe2FifoWordType,
    //    Vec[BgSubLineMemEntry],
    //    //CombinePipePayload,
    //    //CombinePipePayload,
    //  ]]()
    //)
    //val combinePipe2ObjMemRdArr = (
    //  new ArrayBuffer[MemReadSyncIntoStream[
    //    //CombinePipe2FifoWordType,
    //    Vec[ObjSubLineMemEntry],
    //    //CombinePipePayload,
    //    //CombinePipePayload,
    //  ]]()
    //)
    //for (idx <- 0 until rdBgSubLineMemArr.size) {
    //  combinePipe2BgMemRdArr += MemReadSyncIntoStream(
    //    //fifoWordType=CombinePipe2FifoWordType(),
    //    memWordType=Vec.fill(rdBgSubLineMemArr.size)(
    //      BgSubLineMemEntry()
    //    ),
    //    multiRd=rdBgSubLineMemArr(idx),
    //    rdIdx=RdBgSubLineMemArrInfo.combineIdx,
    //    haltV2d=combinePipe2HaltV2d,
    //    haltJdx=CombinePipe2HaltV2dInfo.bgIdx,
    //    haltIdx=idx,
    //  )
    //  //anyCombinePipe2BgMemRdArrHaltPipeVec(jdx) := 
    //}
    //for (idx <- 0 until rdObjSubLineMemArr.size) {
    //  combinePipe2ObjMemRdArr += MemReadSyncIntoStream(
    //    //fifoWordType=CombinePipe2FifoWordType(),
    //    memWordType=Vec.fill(rdObjSubLineMemArr.size)(
    //      ObjSubLineMemEntry()
    //    ),
    //    multiRd=rdObjSubLineMemArr(idx),
    //    rdIdx=RdObjSubLineMemArrInfo.combineIdx,
    //    haltV2d=combinePipe2HaltV2d,
    //    haltJdx=CombinePipe2HaltV2dInfo.objIdx,
    //    haltIdx=idx,
    //  )
    //}

    //MemReadSyncIntoStream(
    //  memWordType=BgSubLineMemEntry(),
    //  multiRd=
    //)
    //haltCombinePipe2 := (
    //  
    //)
    //when (!haltCombinePipe2) {
    //}

    //val psbCombinePipeArr = (
    //  new ArrayBuffer[PipeSkidBuf[CombinePipePayload]]()
    //)

    //def combinePipeIn2FifoDepth = (
    //  //20
    //  3
    //  //2
    //)
    ////def combinePipe2FifoPushStallGeAmountCanPop = 4
    ////case class CombinePipe2FifoDataType() extends Bundle {
    ////  val bgRd = Vec.fill(params.bgTileSize2d.x)(BgSubLineMemEntry())
    ////  val objRd = Vec.fill(params.objTileSize2d.x)(ObjSubLineMemEntry())
    ////}
    //val combinePipeIn2Fifo = AsyncReadFifo(
    //  //dataType=CombinePipe2FifoDataType(),
    //  dataType=CombinePipePayload(),
    //  depth=combinePipeIn2FifoDepth,
    //)
    ////val combinePipeIn2Fifo = StreamFifo(
    ////  dataType=CombinePipePayload(),
    ////  depth=combinePipeIn2FifoDepth,
    ////  //latency=1,
    ////  //forFMax=true,
    ////)
    for (idx <- 0 until combinePipeIn.size) {
      //psbCombinePipeArr += PipeSkidBuf(
      //  dataType=CombinePipePayload(),
      //  //optIncludeBusy=(idx == 2),
      //)
      //def psb = psbCombinePipeArr(idx)

      //if (idx == 2) {
      //  psb.io.misc.busy := haltCombinePipe2
      //  //psb.io.misc.busy := False
      //}
      if (idx > 0) {
        //combinePipeIn(idx - 1) <, 
        //combinePipeIn(idx) << psb.io.next
        //psb.io.prev << combinePipeOut(idx - 1)
        //if (idx + 1 == 2) {
        //  combinePipeIn2Fifo.io.push << combinePipeOut(idx)
        //} else if (idx == 2) { 
        //  combinePipeIn(idx) << combinePipeIn2Fifo.io.pop
        //if (idx + 1 == 2) {
        //  combinePipeIn2Fifo.io.push << combinePipeOut(idx + 1)
        //} else if (idx == 2) {
        //  combinePipeIn2Fifo.io.pop >> combinePipeIn(idx)
        //--------
        //if (idx == 2) {
        //  //combinePipeIn2Fifo.io.push <-/< combinePipeOut(idx - 1)
        //  //combinePipeIn(idx) <-/< combinePipeIn2Fifo.io.pop
        //  //combinePipeIn(idx) <-/< combinePipeOut(idx - 1).haltWhen(
        //  //  haltCombinePipeIn2
        //  //)
        //}
        //if (
        //  (
        //    idx == 1
        //  ) || (
        //    idx - 1 == 1
        //  )
        //) 

        //if (idx == 1) {
        //  // `valid` and `payload` are cut by a register stage
        //  //combinePipeIn(idx) <-< combinePipeOut(idx - 1)
        //  ////combinePipeIn(idx) <-/< combinePipeOut(idx - 1)
        //  ////combinePipeIn(idx) <-/< combinePipeOut(idx - 1)
        //  ////combinePipeIn(idx) << combinePipeOut(idx - 1)
        //  //combinePipeIn(idx) << combineBgObjRdPipe
        //  //combinePipeIn(idx) <-/< 
        //} else if (idx - 1 == 1) {
        //  // `ready` is cut by a register stage
        //  //combinePipeIn(idx) </< combinePipeOut(idx - 1)
        //  ////combinePipeIn(idx) <-/< combinePipeOut(idx - 1)
        //  ////combinePipeIn(idx) << combinePipeOut(idx - 1)
        //} else {
          // `valid`, `payload`, and `ready` are all cut by a register
          // stage
          combinePipeIn(idx) <-/< combinePipeOut(idx - 1)
        //}
        //--------
      }
      if (
        idx
        != 1
        //!= 2
      ) {
        combinePipeOut(idx).valid := combinePipeIn(idx).valid
        combinePipeIn(idx).ready := combinePipeOut(idx).ready
      } else {
        //combinePipeOut(idx)
        combineBgObjRdPipeFork.io.pipeIn << combinePipeIn(idx)
        for (
          combineIdx <- 0 until params.numLineMemsPerBgObjRenderer * 2
        ) {
          //combineObjSubLineMemArr(combineIdx).io.rdAddrPipe << (
          //  combineBgObjRdPipeFork.io.pipeOutVec//(combineIdx)
          //);
          def myLineMem = (
            if (combineIdx % 2 == 0) {
              combineBgSubLineMemArr(combineIdx >> 1)
            } else { // if (combineIdx % 2 == 1)
              combineObjSubLineMemArr(combineIdx >> 1)
            }
          )
          def myForkPipeOut = (
            combineBgObjRdPipeFork.io.pipeOutVec(combineIdx)
          )
          def myJoinPipeIn = (
            combineBgObjRdPipeJoin.io.pipeInVec(combineIdx)
          )
          myForkPipeOut.translateInto(
            myLineMem.io.rdAddrPipe
          )(
            dataAssignment=(
              o,
              i,
            ) => {
              //o.addr := i.cnt(
              //  log2Up(params.objSubLineMemArrSize) - 1 downto 0
              //)
              o.addr := (
                if (combineIdx % 2 == 0) {
                  params.getBgSubLineMemArrIdx(
                    addr=i.cnt,
                  )
                } else { // if (combineIdx % 2 == 1)
                  params.getObjSubLineMemArrIdx(
                    addr=i.cnt,
                  )
                }
              )
              o.data := i
            }
          )
          myJoinPipeIn << myLineMem.io.rdDataPipe
        }
        combinePipeOut(idx).valid := (
          combineBgObjRdPipeJoin.io.pipeOut.valid
        )
        combineBgObjRdPipeJoin.io.pipeOut.ready := (
          combinePipeOut(idx).ready
        )
        switch (rCombineLineMemArrIdx) {
          for (
            combineIdx <- 0 until params.numLineMemsPerBgObjRenderer
          ) {
            is (combineIdx) {
              // BEGIN: add in BGs; later
              //combinePipeOut(idx).payload := (
              //  combineBgObjRdPipeJoin.io.pipeOut.payload(combineIdx * 2)
              //)
              combinePipeOut(idx).payload.stage2.rdBg := (
                combineBgObjRdPipeJoin.io.pipeOut.payload(combineIdx * 2)
                  .stage2.rdBg
              )
              //combinePipeOut(idx).payload.stage2.rdObj.allowOverride
              combinePipeOut(idx).payload.stage2.rdObj := (
                combineBgObjRdPipeJoin.io.pipeOut.payload(
                  combineIdx * 2 + 1
                ).stage2.rdObj
              )
              // END: add in BGs; later
            }
          }
        }
        //for (lineMemIdx <- 0 until combineObjSubLineMemArr.size) {
        //  def myLineMem = combineObjSubLineMemArr(lineMemIdx)
        //  myLineMem.io.rdAddrPipe.valid := combinePipeIn(idx).valid
        //  myLineMem.io.rdAddrPipe.data := combinePipeIn(idx)
        //  myLineMem.io.rdAddrPipe.addr := params.getObjSubLineMemArrIdx(
        //    addr=combinePipeIn(idx).cnt,
        //  )
        //  myLineMem.io.rdDataPipe.ready := combinePipeOut(idx).ready
        //}
        //switch (rCombineLineMemArrIdx) {
        //  for (combineIdx <- 0 until params.numLineMemsPerBgObjRenderer) {
        //    def myLineMem = combineObjSubLineMemArr(combineIdx)
        //    is (combineIdx) {
        //      combinePipeIn(idx).ready := myLineMem.io.rdAddrPipe.ready
        //      combinePipeOut(idx).valid := myLineMem.io.rdDataPipe.valid
        //      combinePipeOut(idx).payload := (
        //        myLineMem.io.rdDataPipe.payload
        //      )
        //    }
        //  }
        //}
      }
    }

    //for (idx <- 1 to combinePipeIn.size - 1) {
    //  // Create pipeline registering
    //  //combinePipeIn(idx) <-< combinePipeOut(idx - 1)
    //  //if (
    //  //  idx - 1 + 1 != 2
    //  //  && idx - 1 != 2
    //  //) {
    //  //if (idx == 2) {
    //  //  psbCombinePipe2.io.prev << combinePipeIn(idx)
    //  //} else if (idx - 1 == 2) {
    //  //  combinePipeOut(idx) << psbCombinePipe2.io.next
    //  //}
    //  if (idx - 1 == 2) {
    //    combinePipeIn(idx) <-/< (
    //      combinePipeOut(idx - 1).haltWhen(haltCombinePipe2)
    //    )
    //  } else if (idx == 3) {
    //  } else {
    //    combinePipeIn(idx) <-/< combinePipeOut(idx - 1)
    //  }
    //}
    //for (idx <- 0 to combinePipeOut.size - 1) {
    //  // Connect output `valid` to input `valid`
    //  if (idx != 2) {
    //    combinePipeOut(idx).valid := combinePipeIn(idx).valid
    //    combinePipeIn(idx).ready := combinePipeOut(idx).ready
    //  }
    //}

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
    //val rPrevCombinePipeLast = Reg(cloneOf(combinePipeLast))
    //rPrevCombinePipeLast.init(rPrevCombinePipeLast.getZero)
    val rPrevCombinePipeLastCnt = Reg(SInt(combinePipeCntWidth bits))
      .init(S(combinePipeCntWidth bits, default -> True))
    val combinePipeLastThrown = Stream(CombinePipePayload())
    combinePipeLastThrown << combinePipeLast.throwWhen(
      rPrevCombinePipeLastCnt.asUInt === combinePipeLastThrown.cnt
    )
    when (combinePipeLastThrown.fire) {
      rPrevCombinePipeLastCnt := combinePipeLastThrown.cnt.asSInt
    }
    combinePipeLastThrown.translateInto(
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

        val rPastHaltCombinePipe = RegNext(haltCombinePipeVeryFront)
          .init(False)
        when (intnlChangingRowRe) {
          haltCombinePipeVeryFront := False
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
          haltCombinePipeVeryFront := True
        } otherwise {
          //haltCombinePipeVeryFront := False
          haltCombinePipeVeryFront := rPastHaltCombinePipe
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
          for (x <- 0 until params.bgTileSize2d.x) {
            switch (tempInp.bgIdx) {
              for (tempBgIdx <- 0 until params.numBgs) {
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

          for (x <- 0 until params.bgTileSize2d.x) {
            for (tempBgIdx <- 0 until params.numBgs) {
              def arr = bgEntryMemA2d(tempBgIdx)(x)
              arr.io.rdEn := True
              arr.io.rdAddr := 0
              arr.io.rdAddr.allowOverride
            }
            switch (tempInp.bgIdx) {
              for (tempBgIdx <- 0 until params.numBgs) {
                is (tempBgIdx) {
                  //tempOutp.bgEntry(x) := (
                  //  bgEntryMemArr(tempBgIdx).readAsync(
                  //    address=tempInp.bgEntryMemIdx(x)
                  //  )
                  //)
                  bgEntryMemA2d(tempBgIdx)(x).io.rdAddr := (
                    tempInp.bgEntryMemIdx(x)
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
          //stageData.pipeOut(idx).stage2 := stageData.pipeIn(idx).stage2
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

          for (x <- 0 until params.bgTileSize2d.x) {
            //for (tempBgIdx <- 0 until params.numBgs) {
            //  def arr = bgEntryMemA2d(tempBgIdx)(x)
            //  arr.io.rdEn := True
            //  arr.io.rdAddr := 0
            //  arr.io.rdAddr.allowOverride
            //}
            switch (tempInp.bgIdx) {
              for (tempBgIdx <- 0 until params.numBgs) {
                is (tempBgIdx) {
                  //tempOutp.bgEntry(x) := (
                  //  bgEntryMemArr(tempBgIdx).readAsync(
                  //    address=tempInp.bgEntryMemIdx(x)
                  //  )
                  //)
                  //bgEntryMemA2d(tempBgIdx)(x).io.rdAddr := (
                  //  tempInp.bgEntryMemIdx(x)
                  //)
                  tempOutp.bgEntry(x) := (
                    bgEntryMemA2d(tempBgIdx)(x).io.rdData
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
          stageData.pipeOut(idx).stage3 := stageData.pipeIn(idx).stage3
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(4)
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
          for (x <- 0 until params.bgTileSize2d.x) {
            //switch (tempInp.bgIdx) {
            //  for (tempBgIdx <- 0 to params.numBgs - 1) {
            //    is (tempBgIdx) {
            //    }
            //  }
            //}
            bgTileMemArr(x).io.rdEn := True
            bgTileMemArr(x).io.rdAddr := tempInp.bgEntry(x).tileMemIdx
            //tempOutp.tile(x) := (
            //  bgTileMemArr.readAsync(address=tempInp.bgEntry(x).tileMemIdx)
            //)
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).tilePxsCoord := (
          //  stageData.pipeIn(idx).tilePxsCoord
          //)
          //stageData.pipeOut(idx).stage5 := stageData.pipeIn(idx).stage5
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
            tempOutp.tile(x) := bgTileMemArr(x).io.rdData
            //tempOutp.tile(x) := (
            //  bgTileMemArr.readAsync(address=tempInp.bgEntry(x).tileMemIdx)
            //)
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).tilePxsCoord := (
          //  stageData.pipeIn(idx).tilePxsCoord
          //)
          stageData.pipeOut(idx).stage5 := stageData.pipeIn(idx).stage5
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
      //      bgTileMemArr.readAsync(address=tempInp.bgEntry.tileMemIdx)
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
        stageData=stageData.craft(6)
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
          stageData.pipeOut(idx).stage7 := (
            stageData.pipeIn(idx).stage7
          )
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(8)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          for (x <- 0 to params.bgTileSize2d.x - 1) {
            //tempOutp.palEntry(x) := bgPalEntryMemArr.readAsync(
            //  address=tempInp.palEntryMemIdx(x)
            //)
            //tempOutp.palEntry(x) := bgPalEntryMemArr(x).io.rdData
            bgPalEntryMemArr(x).io.rdEn := True
            bgPalEntryMemArr(x).io.rdAddr := tempInp.palEntryMemIdx(x)
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).stage11 := (
          //  stageData.pipeIn(idx).stage11
          //)
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(9)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          for (x <- 0 to params.bgTileSize2d.x - 1) {
            //tempOutp.palEntry(x) := bgPalEntryMemArr.readAsync(
            //  address=tempInp.palEntryMemIdx(x)
            //)
            tempOutp.palEntry(x) := bgPalEntryMemArr(x).io.rdData
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrBgPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage9 := (
            stageData.pipeIn(idx).stage9
          )
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(10)
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
            def tempLineMemEntry = tempOutp.subLineMemEntry(x)
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
          stageData.pipeOut(idx).stage10 := (
            stageData.pipeIn(idx).stage10
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
          wrBgPipeLast.subLineMemEntry(0).getSubLineMemTempArrIdx()
        )

        switch (rWrLineMemArrIdx) {
          for (jdx <- 0 until (1 << rWrLineMemArrIdx.getWidth)) {
            is (jdx) {
              wrBgSubLineMemArr(jdx).write(
                address=tempArrIdx,
                data=wrBgPipeLast.subLineMemEntry,
              )
              combineBgSubLineMemArr(jdx).io.wrPulse.valid := True
              combineBgSubLineMemArr(jdx).io.wrPulse.addr := (
                tempArrIdx
              )
              combineBgSubLineMemArr(jdx).io.wrPulse.data := (
                wrBgPipeLast.subLineMemEntry
              )
              for (kdx <- 0 until combineBgSubLineMemArr.size) {
                if (kdx != jdx) {
                  combineBgSubLineMemArr(kdx).io.wrPulse.valid := False
                  combineBgSubLineMemArr(kdx).io.wrPulse.addr := 0x0
                  combineBgSubLineMemArr(kdx).io.wrPulse.data := (
                    wrBgPipeLast.subLineMemEntry.getZero
                  )
                }
              }
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
      } otherwise { // when (!wrBgPipeLast.fire)
        for (jdx <- 0 until combineBgSubLineMemArr.size) {
          combineBgSubLineMemArr(jdx).io.wrPulse.valid := False
          combineBgSubLineMemArr(jdx).io.wrPulse.addr := 0x0
          combineBgSubLineMemArr(jdx).io.wrPulse.data := (
            wrBgPipeLast.subLineMemEntry.getZero
          )
        }
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
      val rStage11FwdV2d = Vec.fill(params.objTileSize2d.x)(
        Vec.fill(wrObjPipeStage11NumFwd)(
          Reg(WrObjPipeStage11Fwd())
        )
      )
      for (x <- 0 to params.objTileSize2d.x - 1) {
        for (fwdIdx <- 0 to rStage11FwdV2d(x).size - 1) {
          rStage11FwdV2d(x)(fwdIdx).setName(
            f"rWrObjPipeStage11FwdV2d_$x" + f"_$fwdIdx"
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
          //tempOutp.gridIdxLsb := tempInp.calcGridIdxLsb()

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
          //tempOutp.objAttrs := objAttrsMem.readAsync(
          //  address=tempInp.objAttrsMemIdx(),
          //)
          objAttrsMem.io.rdEn := True
          objAttrsMem.io.rdAddr := tempInp.objAttrsMemIdx()
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).objAttrs := (
          //  stageData.pipeIn(idx).objAttrs
          //)
          //stageData.pipeOut(idx).stage2 := stageData.pipeIn(idx).stage2
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(2)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)
          //val objAttrsMemIdxIn = tempInp.objAttrsMemIdx
          //tempOutp.objAttrs := objAttrsMem.readAsync(
          //  address=tempInp.objAttrsMemIdx(),
          //)
          //objAttrsMem.io.rdEn := True
          //objAttrsMem.io.rdAddr := tempInp.objAttrsMemIdx()
          tempOutp.objAttrs := objAttrsMem.io.rdData
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          //stageData.pipeOut(idx).objAttrs := (
          //  stageData.pipeIn(idx).objAttrs
          //)
          stageData.pipeOut(idx).stage2 := stageData.pipeIn(idx).stage2
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

          objTileMem.io.rdEn := True
          objTileMem.io.rdAddr := tempInp.objAttrs.tileMemIdx
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
        }
      )
      // END: Stage 3

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
        
          //tempOutp.tile := objTileMem.readAsync(
          //  address=tempInp.objAttrs.tileMemIdx
          //)
          tempOutp.tile := objTileMem.io.rdData
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage4 := stageData.pipeIn(idx).stage4
          //stageData.pipeOut(idx).tilePxsCoordYPipe1 := (
          //  stageData.pipeIn(idx).tilePxsCoordYPipe1
          //)
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
                  _(0) === tempInp.stage0.calcGridIdxLsb() 
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
          tempOutp.stage5.pxPosXGridIdxFindFirstSameAsFound := (
            dbgPxPosXGridIdxFindFirstSameAs._1
          )
          tempOutp.stage5.pxPosXGridIdxFindFirstSameAsIdx := (
            dbgPxPosXGridIdxFindFirstSameAs._2.resized
          )
          val dbgPxPosXGridIdxFindFirstDiff: (Bool, UInt) = (
            tempOutp.pxPosXGridIdx.sFindFirst(
              //condition=(
                //myBool => //(
                  _(0) =/= tempInp.stage0.calcGridIdxLsb() 
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
          tempOutp.stage5.pxPosXGridIdxFindFirstDiffFound := (
            dbgPxPosXGridIdxFindFirstDiff._1
          )
          tempOutp.stage5.pxPosXGridIdxFindFirstDiffIdx := (
            dbgPxPosXGridIdxFindFirstDiff._2.resized
          )
          for (x <- 0 to params.objTileSize2d.x - 1) {
            tempOutp.pxPosXGridIdx(x) := tempInp.pxPos(x).x.asUInt(
              tempInp.pxPos(x).x.asUInt.high
              downto params.objTileSize2dPow.x
            )
            val dbgTestWrObjPipe5_pxPosXGridIdx = UInt(
              tempOutp.pxPosXGridIdx(x).getWidth bits
            )
              .setName(f"dbgTestWrObjPipe5_pxPosXGridIdx_$x")
            dbgTestWrObjPipe5_pxPosXGridIdx := tempOutp.pxPosXGridIdx(x)

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
          stageData.pipeOut(idx).stage5 := stageData.pipeIn(idx).stage5
        },
      )
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
                tempInp.stage0.calcGridIdxLsb()
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
          stageData.pipeOut(idx).stage6 := stageData.pipeIn(idx).stage6
        },
      )
      // END: Stage 6

      // BEGIN: Stage 7
      HandleDualPipe(
        stageData=stageData.craft(7)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          for (x <- 0 to params.objTileSize2d.x - 1) {
            objPalEntryMemArr(x).io.rdEn := True
            objPalEntryMemArr(x).io.rdAddr := tempInp.palEntryMemIdx(x)
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
        }
      )
      // END: Stage 7

      HandleDualPipe(
        stageData=stageData.craft(8)
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

          for (x <- 0 to params.objTileSize2d.x - 1) {
            //tempOutp.palEntry(x) := objPalEntryMemArr.readAsync(
            //  address=tempInp.palEntryMemIdx(x)
            //)
            tempOutp.palEntry(x) := objPalEntryMemArr(x).io.rdData
            tempOutp.pxPosInLine(x) := (
              tempInp.pxPosRangeCheck(x).x && tempInp.pxPosRangeCheck(x).y
            )
            tempOutp.pxPosCmpForOverwrite(x) := (
              tempOutp.pxPosInLine(x)
              && tempInp.pxPosXGridIdxFindFirstSameAsFound
              && tempInp.pxPosXGridIdxMatches(x)
            )
          }
          def tempObjArrIdx = tempInp.getObjSubLineMemArrIdx(
            x=tempInp.pxPosXGridIdxFindFirstSameAsIdx
          )
          val dbgTestWrObjPipe5_tempObjArrIdx = UInt(
            tempObjArrIdx.getWidth bits
          )
            .setName(
              f"dbgTestWrObjPipe5_tempObjArrIdx"
              //+ f"_$jdx"
              //+ f"_$x"
            )
          dbgTestWrObjPipe5_tempObjArrIdx := tempObjArrIdx

          //for (jdx <- 0 until 1 << rWrLineMemArrIdx.getWidth) {
          //  // We no longer need the `switch` statement here since we are
          //  // just reading
          //  //rdObjSubLineMemArr(jdx).addrVec(
          //  //  RdObjSubLineMemArrInfo.wrObjIdx
          //  //) := tempObjArrIdx
          //}
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage8 := stageData.pipeIn(idx).stage8
        },
      )

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

      //    //when (tempInp.rdSubLineMemEntry.col.a) {
      //    //when (tempInp.rdSubLineMemEntry.prio === 0) {
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
      //      //      tempOutp.rdSubLineMemEntry := (
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
      //          tempInp.rdSubLineMemEntry.prio < tempInp.objAttrs.prio
      //          //tempOutp.rdSubLineMemEntry.prio < tempInp.objAttrs.prio
      //        ) {
      //          tempOutp.overwriteLineMemEntry := True
      //        } otherwise {
      //          tempOutp.overwriteLineMemEntry := (
      //            !tempInp.rdSubLineMemEntry.col.a
      //            //!tempOutp.rdSubLineMemEntry.col.a
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
      //    //  //!tempInp.rdSubLineMemEntry.prio.msb
      //    //  //tempInp.rdSubLineMemEntry.written
      //    //  //(
      //    //  //  !tempInp.rdSubLineMemEntry.col.a
      //    //  //  //tempInp.objAttrs.prio
      //    //  //  //|| (tempInp.rdSubLineMemEntry.prio < tempInp.objAttrs.prio)

      //    //  //  //&& tempInp.objAttrs.prio <= tempInp.rdSubLineMemEntry.prio
      //    //  //  //&& tempInp.pxPosInLine
      //    //  //)
      //    //  (
      //    //    tempInp.rdSubLineMemEntry.col.a
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

      // BEGIN: Stage 9
      HandleDualPipe(
        stageData=stageData.craft(9)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)
          for (jdx <- 0 until (1 << rWrLineMemArrIdx.getWidth)) {
            val temp = wrObjSubLineMemArr(jdx)
            temp.io.rdEn := True
            temp.io.rdAddr := 0
            temp.io.rdAddr.allowOverride
          }

          switch (
            rWrLineMemArrIdx
            //tempInp.lineMemArrIdx
          ) {
            for (jdx <- 0 until (1 << rWrLineMemArrIdx.getWidth)) {
              is (jdx) {
                //println(f"testificate: $jdx")
                //println({
                //  def size = wrObjSubLineMemArr.size
                //  f"$size"
                //})
                //val temp = wrObjSubLineMemArr(jdx).readAsync(
                //  params.getObjSubLineMemArrIdx(
                //    addr=tempInp.cnt
                //  )
                //)
                //--------
                val temp = wrObjSubLineMemArr(jdx)
                //tempOutp.stage9.rdSubLineMemEntry := (
                //  temp.rdData
                //)
                when (tempInp.pxPosXGridIdxFindFirstSameAsFound) {
                  //tempOutp.stage6.rdSubLineMemEntry := (
                  //  //temp.readAsync(
                  //  //  address=params.getObjSubLineMemArrIdx(
                  //  //    //tempInp.cnt
                  //  //    //tempInp.pxPos(0).x.asUInt
                  //  //    tempInp.pxPos(
                  //  //      tempInp.pxPosXGridIdxFindFirstSameAsIdx
                  //  //    ).x.asUInt
                  //  //  )
                  //  //)
                  //)
                  temp.io.rdAddr := params.getObjSubLineMemArrIdx(
                    //tempInp.cnt
                    //tempInp.pxPos(0).x.asUInt
                    tempInp.pxPos(
                      tempInp.pxPosXGridIdxFindFirstSameAsIdx
                    ).x.asUInt
                  )
                } otherwise {
                  //tempOutp.stage6.rdSubLineMemEntry := (
                  //  //tempOutp.stage6.rdSubLineMemEntry.getZero
                  //  temp.readAsync(
                  //    address=params.getObjSubLineMemArrIdx(
                  //      //tempInp.cnt
                  //      //tempInp.pxPos(0).x.asUInt
                  //      tempInp.pxPos(
                  //        tempInp.pxPosXGridIdxFindFirstDiffIdx
                  //      ).x.asUInt
                  //    )
                  //  )
                  //)
                  temp.io.rdAddr := params.getObjSubLineMemArrIdx(
                    //tempInp.cnt
                    //tempInp.pxPos(0).x.asUInt
                    tempInp.pxPos(
                      tempInp.pxPosXGridIdxFindFirstDiffIdx
                    ).x.asUInt
                  )
                }
                //--------
                //tempOutp.stage6.rdSubLineMemEntry := (
                //  //rdObjSubLineMemArr(jdx).dataVec(
                //  //  RdObjSubLineMemArrInfo.wrObjIdx
                //  //)
                //  wrObjSubLineMemArr(jdx).readAsync(
                //    params.getObjSubLineMemArrIdx(
                //      addr=tempInp.cnt
                //    )
                //  )
                //)
              }
            }
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
        },
      )
      // END: Stage 9

      // BEGIN: Stage 10
      HandleDualPipe(
        stageData=stageData.craft(10)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          switch (
            rWrLineMemArrIdx
            //tempInp.lineMemArrIdx
          ) {
            for (jdx <- 0 until (1 << rWrLineMemArrIdx.getWidth)) {
              is (jdx) {
                //println(f"testificate: $jdx")
                //println({
                //  def size = wrObjSubLineMemArr.size
                //  f"$size"
                //})
                //val temp = wrObjSubLineMemArr(jdx).readAsync(
                //  params.getObjSubLineMemArrIdx(
                //    addr=tempInp.cnt
                //  )
                //)
                //--------
                val temp = wrObjSubLineMemArr(jdx)
                tempOutp.stage10.rdSubLineMemEntry := (
                  temp.io.rdData
                )
                //when (tempInp.pxPosXGridIdxFindFirstSameAsFound) {
                //  tempOutp.stage10.rdSubLineMemEntry := (
                //    //temp.readAsync(
                //    //  address=params.getObjSubLineMemArrIdx(
                //    //    //tempInp.cnt
                //    //    //tempInp.pxPos(0).x.asUInt
                //    //    tempInp.pxPos(
                //    //      tempInp.pxPosXGridIdxFindFirstSameAsIdx
                //    //    ).x.asUInt
                //    //  )
                //    //)
                //  )
                //} otherwise {
                //  tempOutp.stage10.rdSubLineMemEntry := (
                //    //tempOutp.stage10.rdSubLineMemEntry.getZero
                //    temp.readAsync(
                //      address=params.getObjSubLineMemArrIdx(
                //        //tempInp.cnt
                //        //tempInp.pxPos(0).x.asUInt
                //        tempInp.pxPos(
                //          tempInp.pxPosXGridIdxFindFirstDiffIdx
                //        ).x.asUInt
                //      )
                //    )
                //  )
                //}
                //--------
                //tempOutp.stage10.rdSubLineMemEntry := (
                //  //rdObjSubLineMemArr(jdx).dataVec(
                //  //  RdObjSubLineMemArrInfo.wrObjIdx
                //  //)
                //  wrObjSubLineMemArr(jdx).readAsync(
                //    params.getObjSubLineMemArrIdx(
                //      addr=tempInp.cnt
                //    )
                //  )
                //)
              }
            }
          }
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage10 := stageData.pipeIn(idx).stage10
        },
      )
      // END: Stage 10

      // BEGIN: Stage 11
      HandleDualPipe(
        stageData=stageData.craft(11)
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          def outpExt = (
            //rWrObjPipeOut6Ext
            tempOutp.stage11.ext
            //cloneOf(tempOutp.stage11.ext)
          )
          val nonRotatedOutpExt = cloneOf(tempOutp.stage11.ext)
            .setName("wrObjPipe11_nonRotatedOutpExt")
          val myIdxVec = Vec.fill(params.objTileSize2d.x)(
            UInt(params.objTileSize2dPow.x bits)
          )
            .setName("wrObjPipe11_myIdxVec")

          def myMainFunc(
            x: Int
            //myIdx: UInt
            //myIdx: Int
          ): Unit = {
            //--------
            // BEGIN: debug comment this out; later
            //val myIdx = UInt((params.objTileSize2dPow.x + 1) bits)
            //  .setName(f"wrObjPipe11_myIdx_$x")
            val myIdxFull = cloneOf(tempInp.pxPos(x).x)
              .setName(f"wrObjPipe11_myIdxFull_$x")
            myIdxFull := tempInp.pxPos(x).x
            val myIdx = UInt(params.objTileSize2dPow.x bits)
              .setName(f"wrObjPipe11_myIdx_$x")
            myIdx := myIdxFull.asUInt(myIdx.bitsRange)
            myIdxVec(x) := myIdx

            //outpExt.overwriteLineMemEntry(myIdx) := False
            //outpExt.overwriteLineMemEntry(myIdx).allowOverride
            //outpExt.wrLineMemEntry(myIdx).assignFromBits(
            //  tempInp.rdSubLineMemEntry(myIdx).asBits
            //)
            //outpExt.wrLineMemEntry(myIdx).allowOverride
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
              .setName(f"wrObjPipe11_tempOverwriteLineMemEntry_$x")
            //--------
            // BEGIN: later
            def nonRotatedOverwriteLineMemEntry = (
              //rWrObjPipeOut11ExtData
              //outpExt
              nonRotatedOutpExt
              .overwriteLineMemEntry(
                //myIdx
                x
              )
            )
            // END: later
            //--------
            //val rotatedWrLineMemEntry = Vec(
            //  //tempOutp.wrLineMemEntry(x)
            //  //tempOutp.wrLineMemEntry(myIdx)
            //  tempOutp.wrLineMemEntry.drop(x).appendedAll(
            //    tempOutp.wrLineMemEntry.take(x)
            //  )
            //)//.addTag(noLatchCheck)
            val tempWrLineMemEntry = (
              cloneOf(
                //rWrObjPipeOut11ExtData
                nonRotatedOutpExt
                .wrLineMemEntry(myIdx)
              )
            )
              .setName("dbgTempWrObjPipe11_tempWrLineMemEntry")
            //--------
            // BEGIN: later
            def nonRotatedWrLineMemEntry = (
              //rotatedWrLineMemEntry(
              //  //myIdx(tempMyIdxRange)
              //  myIdx
              //)
              //rWrObjPipeOut11ExtData
              //outpExt
              nonRotatedOutpExt
              .wrLineMemEntry(
                //// `myIdx` should be used here because `x` is not an index
                //// into an `ObjSubLineMemEntry`
                //myIdx
                x
              )
              //tempOutp.wrLineMemEntry(
              //  //x
              //  myIdx
              //)
              //tempOutp.wrLineMemEntry(myIdx)
            )
            // END: later
            //--------
            //def toFwdWrLineMemEntry = (
            //  rWrObjPipeOut11ExtData.wrLineMemEntry(x)
            //)

            //val tempOverwriteLineMemEntry = Bool()
            //val tempConcat = Bits(tempInp.numFwd + 1 bits)
            //--------
            // BEGIN: debug comment this out; later
            def fwdVec = tempOutp.stage11.fwdV2d(
              x
              //myIdx
            )
            //val rFwdVec = Reg(cloneOf(fwdVec)) //init(fwdVec.getZero)
            def rFwdVec = rStage11FwdV2d(
              //x
              myIdx
            )
            //rFwdVec(x).init(rFwdVec(x).getZero)
            for (fwdIdx <- 0 until rFwdVec.size) {
              rStage11FwdV2d(x)(fwdIdx).init(
                rStage11FwdV2d(x)(fwdIdx).getZero
              )
            }

            for (fwdIdx <- 0 to rFwdVec.size - 1) {
              //rFwdVec(fwdIdx).init(rFwdVec(fwdIdx).getZero)
              when (
                tempOutp.fire
                //tempInp.fire
              ) {
                rFwdVec(fwdIdx) := fwdVec(fwdIdx)
              }
              //rFwdVec(fwdIdx) := fwdVec(fwdIdx)
              if (fwdIdx > 0) {
                fwdVec(fwdIdx) := rFwdVec(fwdIdx - 1)
              }
            }
            // END: debug comment this out; later
            //val dbgTestWrObjPipeOut11_sameAsFound = cloneOf(
            //  tempInp.pxPosXGridIdxFindFirstSameAsFound
            //)
            //  .setName(f"dbgTestWrObjPipeOut11_sameAsFound_$x")
            //dbgTestWrObjPipeOut11_sameAsFound := (
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
                    //tempInp.rdSubLineMemEntry.prio < tempInp.objAttrs.prio
                    //tempOutp.rdSubLineMemEntry.prio < tempInp.objAttrs.prio
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
              .setName(f"wrObjPipeStage11_tempRdLineMemEntry_$x")
            //--------
            // BEGIN: debug comment this out; later
            //val tempConcat = Bits(fwdVec.size bits)
            val tempConcat = Bits((fwdVec.size - 1) bits)
              .setName(f"wrObjPipeStage11TempConcat_$x")

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
                  fwdVec(fwdIdx).overwriteLineMemEntry(0) := (
                    //tempOutp.overwriteLineMemEntry(x)
                    tempOverwriteLineMemEntry
                  )
                  //fwdVec(fwdIdx).wrLineMemEntry := tempWrLineMemEntry
                  fwdVec(fwdIdx).wrLineMemEntry(0) := (
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
                  ) //&& tempInp.pxPosXGridIdxFindFirstSameAsFound
                  && fwdVec(fwdIdx).overwriteLineMemEntry(0)
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
                  tempRdLineMemEntry := (
                    fwdVec(fwdIdx + 1).wrLineMemEntry(
                      0
                      //x
                    )
                  )
                }
              }
              default 
              //.otherwise 
              {
                tempRdLineMemEntry := tempInp.rdSubLineMemEntry(
                  //x
                  // `myIdx` should be used here since it's an index into
                  // `objSubLineMemArr(jdx)`
                  myIdx
                )
              }
            }
            // END: debug comment this out; later
            //--------
            //tempRdLineMemEntry := tempInp.rdSubLineMemEntry(
            //  //x
            //  myIdx
            //)

            //tempRdLineMemEntry := tempInp.rdSubLineMemEntry

            //val tempX = UInt(params.objTileSize2dPow.x bits)
            //  .setName(f"wrObjPipe7_tempX_$x")
            //tempX := (tempInp.pxPos(0).x.asUInt + x)(tempX.bitsRange)
            //val rotatedGridIdxMatches = (
            //  input.drop(x).appendedAll(input.take(x))
            //)

            nonRotatedOverwriteLineMemEntry := tempOverwriteLineMemEntry
            nonRotatedWrLineMemEntry := tempWrLineMemEntry

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
                //tempOutp.wrLineMemEntry := tempInp.rdSubLineMemEntry
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
              //someLineMemEntry=tempInp.rdSubLineMemEntry,
              someLineMemEntry=tempRdLineMemEntry,
              someOverwriteLineMemEntry=(
                //tempOutp.overwriteLineMemEntry(x)
                tempOverwriteLineMemEntry
              )
            )
          }

          for (x <- 0 until params.objTileSize2d.x) {
            myMainFunc(x=x)

            //val rotatedOverwriteLineMemEntry = Vec(
            //  tempOutp.overwriteLineMemEntry.drop(x).appendedAll(
            //    tempOutp.overwriteLineMemEntry.take(x)
            //  )
            //)//.addTag(noLatchCheck)
            outpExt.wrLineMemEntry(x) := (
              nonRotatedOutpExt.wrLineMemEntry(myIdxVec(x))
            )
            outpExt.overwriteLineMemEntry(x) := (
              nonRotatedOutpExt.overwriteLineMemEntry(myIdxVec(x))
            )
          }
          //myMainFunc()
        },
        copyOnlyFunc=(
          stageData: DualPipeStageData[Flow[WrObjPipePayload]],
          idx: Int,
        ) => {
          stageData.pipeOut(idx).stage11 := stageData.pipeIn(idx).stage11
        },
      )
      // END: Stage 11
      //--------
      // BEGIN: Stage 7
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
      //    tempOutp.stage7.ext := rWrObjPipeOut6ExtData
      //    //} otherwise {
      //    //  tempOutp.stage7
      //    //}
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    stageData.pipeOut(idx).stage7 := stageData.pipeIn(idx).stage7
      //  },
      //)
      // END: Stage 7
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
          def tempObjArrIdx = wrObjPipeLast.getObjSubLineMemArrIdx(
            x=wrObjPipeLast.pxPosXGridIdxFindFirstSameAsIdx
          )
          val dbgTestWrObjPipeLast_tempObjArrIdx = UInt(
            tempObjArrIdx.getWidth bits
          )
            .setName(
              f"dbgTestWrObjPipeLast_tempObjArrIdx" //+ f"_$jdx" //+ f"_$x"
            )
          //--------
          // BEGIN: new code, with muxing for single `.write()` call
          dbgTestWrObjPipeLast_tempObjArrIdx := tempObjArrIdx
          //objWriter.addrVec(rWrLineMemArrIdx) := tempObjArrIdx
          //objWriter.dataVec(rWrLineMemArrIdx) := (
          //  wrObjPipeLast.wrLineMemEntry
          //)
          //objWriter.enVec(rWrLineMemArrIdx) := (
          //  True
          //  //wrObjPipeLast.stage9.ext.overwriteLineMemEntry
          //  //  .reduceBalancedTree(_ || _)
          //)
          objWriter.addrVec(0) := tempObjArrIdx
          objWriter.dataVec(0) := (
            wrObjPipeLast.wrLineMemEntry
          )
          objWriter.enVec(0) := (
            True
            //wrObjPipeLast.stage9.ext.overwriteLineMemEntry
            //  .reduceBalancedTree(_ || _)
          )
          // END: new code, with muxing for single `.write()` call
          //--------
          // BEGIN: old code, no muxing for single `.write()` call
          //switch (
          //  rWrLineMemArrIdx
          //  //wrObjPipeLast.lineMemArrIdx
          //) {
          //  for (
          //    jdx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1
          //    //jdx <- 0 to (1 << wrObjPipeLast.lineMemArrIdx.getWidth) - 1
          //  ) {
          //    is (jdx) {
          //      //def tempObjArrIdx = params.getObjSubLineMemArrIdx(
          //      //  //addr=combinePipeLast.cnt
          //      //  //addr=Cat(
          //      //  //  wrObjPipeLast.pxPos(0).x.asUInt(
          //      //  //    wrObjPipeLast.pxPos(0).x.high downto 1
          //      //  //  ),
          //      //  //  wrObjPipeLast.gridIdxLsb
          //      //  //).asUInt
          //      //  addr=wrObjPipeLast.getObjSubLineMemArrIdxAddr(),
          //      //)

          //      def tempObjArrIdx = wrObjPipeLast.getObjSubLineMemArrIdx(
          //        x=wrObjPipeLast.pxPosXGridIdxFindFirstSameAsIdx
          //      )
          //      //def tempObjArrIdx = (
          //      //  wrObjPipeLast.pxPosXGridIdx(
          //      //    wrObjPipeLast.pxPosXGridIdxFindFirstSameAs
          //      //  )(
          //      //  )
          //      //)
          //      val dbgTestWrObjPipeLast_tempObjArrIdx = UInt(
          //        tempObjArrIdx.getWidth bits
          //      )
          //        .setName(
          //          f"dbgTestWrObjPipeLast_tempObjArrIdx_$jdx" //+ f"_$x"
          //        )
          //      dbgTestWrObjPipeLast_tempObjArrIdx := tempObjArrIdx

          //      //def tempObjArrElemIdx = (
          //      //  params.getObjSubLineMemArrElemIdx(
          //      //    //addr=combinePipeLast.cnt
          //      //    addr=wrObjPipeLast.pxPos(x).x.asUInt
          //      //  )
          //      //)
          //      //val dbgTestWrObjPipeLast_tempObjArrElemIdx = UInt(
          //      //  tempObjArrElemIdx.getWidth bits
          //      //)
          //      //  .setName(
          //      //    f"dbgTestWrObjPipeLast_tempObjArrElemIdx_$jdx"
          //      //    + f"_$x"
          //      //  )
          //      //dbgTestWrObjPipeLast_tempObjArrElemIdx := (
          //      //  tempObjArrElemIdx
          //      //)
          //      //when (
          //      //  wrObjPipeLast.pxPosXGridIdxFindFirstSameAsFound
          //      //) {
          //        //--------
          //        objSubLineMemArr(jdx).write(
          //          address=tempObjArrIdx
          //          //(
          //          //  wrObjPipeLast.getObjSubLineMemArrIdx_tempRange()
          //          //)
          //          ,
          //          data=wrObjPipeLast.wrLineMemEntry,
          //          //data=tempWrLineMemEntry
          //        )
          //        //--------
          //      //}
          //      //--------
          //      //--------

          //      //when (
          //      //  wrObjPipeLast.pxPos.x >= 0 
          //      //  && wrObjPipeLast.pxPos.x < params.oneLineMemSize
          //      //)
          //      //for (x <- 0 to params.objTileSize2d.x - 1) {
          //        //when (
          //        //  //tempAddr.resized === wrObjPipeLast.pxPos.x.asUInt
          //        //  !wrObjPipeLast.pxPos.x.msb
          //        //) {
          //        //when (wrObjPipeLast.overwriteLineMemEntry) {
          //          // BEGIN: come back to this later
          //          //objSubLineMemArr(jdx).write(
          //          //  //address=wrObjPipeLast.getCntTilePxsCoordX(),
          //          //  address=wrObjPipeLast.pxPos.x.asUInt.resized,
          //          //  //address=tempAddr,
          //          //  //data=wrObjPipeLast.wrLineMemEntry,
          //          //  data=tempWrLineMemEntry,
          //          //)
          //          // END: come back to this later
          //          //dbgObjLineMemVec(jdx)(tempAddr) := (
          //          //  wrObjPipeLast.wrLineMemEntry
          //          //)
          //        //switch (
          //        //  //tempWrLineMemEntry.getSubLineMemTempArrIdx()
          //        //  dbgTestWrObjPipeLastTempArrIdx
          //        //) {
          //        //  for (kdx <- 0 to (1 << tempArrIdxWidth) - 1) {
          //        //    //val dbgTestWrObjPipeLast = UInt(
          //        //    //  params.objTileSize2dPow.x bits
          //        //    //)
          //        //    //dbgTestWrObjPipeLast(
          //        //    //  dbgTestWrObjPipeLast.high downto
          //        //    //)
          //        //    //dbgTestWrObjPipeLast(tempWidth - 1 downto 0) := kdx
          //        //    is (kdx) {
          //        //      // BEGIN: later
          //        //      //objSubLineMemA2d(jdx)(kdx).write(
          //        //      //  address=tempWrLineMemEntry.getSubLineMemTempAddr(),
          //        //      //  data=tempWrLineMemEntry
          //        //      //)
          //        //      //when (
          //        //      //  Cat(
          //        //      //    dbgTestWrObjPipeLastTempArrIdx,
          //        //      //    dbgTestWrObjPipeLastTempAddr
          //        //      //  ).asUInt <= params.oneLineMemSize - 1
          //        //      //) {
          //        //        // BEGIN: debug comment this out; need this
          //        //        when (!wrBgPipeLast.bakCnt.msb) {
          //        //          objSubLineMemA2d(jdx)(kdx).write(
          //        //            //address=tempWrLineMemEntry.getSubLineMemTempAddr(),
          //        //            address=dbgTestWrObjPipeLastTempAddr,
          //        //            data=tempWrLineMemEntry,
          //        //          )
          //        //        }
          //        //        // END: debug comment this out; need this
          //        //        //objSubLineMemA2d(jdx)(x).write(
          //        //        //  //address=tempWrLineMemEntry.getSubLineMemTempAddr(),
          //        //        //  address=dbgTestWrObjPipeLastTempAddr,
          //        //        //  data=tempWrLineMemEntry,
          //        //        //)
          //        //      //}
          //        //      // END: later
          //        //    }
          //        //  }
          //        //}
          //        ////}
          //        ////}
          //      //}
          //    }
          //  }
          //  //default {
          //  //  wrLineMemEntry := rPastWrLineMemEntry
          //  //}
          //}
          // END: old code, no muxing for single `.write()` call
          //--------
        //}
      } otherwise { // when (!wrObjPipeLast.fire)
        //objWriter.enVec(rWrLineMemArrIdx) := False
        objWriter.enVec(0) := False
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
      //def createTempCnt(
      //  //tempInp: Stream[CombinePipePayload],
      //  tempOutp: Stream[CombinePipePayload],
      //  pipeStageIdx: Int
      //): UInt = {
      //  val tempCombineLineMemIdx = KeepAttribute(
      //    Reg(
      //      UInt(
      //        log2Up(
      //          //params.oneLineMemSize
      //          params.oneLineMemSize
      //        ) bits
      //      )
      //    )
      //      .init(0x0)
      //      .setName(
      //        f"dbgTestCombinePipe$pipeStageIdx"
      //        + f"_tempCombineLineMemIdx"
      //      )
      //      //.setName("tempCombineLineMemIdx")
      //  )
      //  //when (intnlChangingRowRe) {
      //  //  rTempCombineLineMemIdx := 0
      //  //} elsewhen (tempOutp.fire) {
      //  //  rTempCombineLineMemIdx := rTempCombineLineMemIdx + 1
      //  //}
      //  when (tempOutp.fire) {
      //    tempCombineLineMemIdx := tempOutp.cnt(
      //      tempCombineLineMemIdx
      //    )
      //  } otherwise {
      //  }
      //}
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
            if (idx != 1) {
              tempOutp.stage0 := tempInp.stage0
            } else {
              //switch (rCombineLineMemArrIdx) {
              //  for (
              //    combineIdx <- 0 until params.numLineMemsPerBgObjRenderer
              //  ) {
              //    is (combineIdx) {
                    tempOutp.stage0 := (
                      combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage0
                    )
              //    }
              //  }
              //}
            }
          }
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(
          1
          //2
          //wrBgObjPipeNumStages + 1
        )
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          //tempOutp.stage2 := (
          //  combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage2
          //)

          ////val tempCombineLineMemIdx = createTempCnt(
          ////  tempOutp=tempOutp,
          ////  pipeStageIdx=1
          ////)

          ////def outpExt = (
          ////  tempOutp.stage1.ext
          ////  //rCombinePipeOut1Ext
          ////)
          ////def cntFifoDepth = 3
          ////def cntFifoStallGeAmountCanPop = 1
          ////val cntFifo = AsyncReadFifo(
          ////  dataType=UInt(tempInp.cnt.getWidth bits),
          ////  depth=cntFifoDepth,
          ////)
          ////when (tempInp.cnt + 1 >= params.oneLineMemSize) {
          ////}
          ////val rTempCombineLineMemIdx = KeepAttribute(
          ////  Reg(
          ////    UInt(
          ////      log2Up(
          ////        //params.oneLineMemSize
          ////        params.oneLineMemSize
          ////      ) bits
          ////    )
          ////  )
          ////    .init(S"2'd-1".resized.asUInt)
          ////    .setName("dbgTestCombinePipe1_tempCombineLineMemIdx")
          ////    //.setName("tempCombineLineMemIdx")
          ////)
          //val tempCombineLineMemIdx = UInt(
          //  log2Up(params.oneLineMemSize) bits
          //)
          //  .setName("dbgTestCombinePipe1_tempCombineLineMemIdx")
          //tempCombineLineMemIdx := tempInp.cnt(
          //  tempCombineLineMemIdx.bitsRange
          //)
          ////val rTempCombineLineMemIdx = Reg(cloneOf(tempOutp.lineMemIdx))
          ////  .init(0x0)
          ////--------
          ////val nextTempCombineLineMemIdx = cloneOf(tempOutp.lineMemIdx)
          ////val rTempCombineLineMemIdx = RegNext(nextTempCombineLineMemIdx)
          ////  .init(0x0)
          ////--------
          ////when (intnlChangingRowRe) {
          ////  tempCombineLineMemIdx := 0
          ////} elsewhen (tempOutp.fire) {
          ////  tempCombineLineMemIdx := tempCombineLineMemIdx + 1
          ////}

          ////when (!(tempOutp.valid && !tempOutp.ready)) {
          ////  tempCombineLineMemIdx := tempInp.cnt(
          ////    tempCombineLineMemIdx.bitsRange
          ////  )
          ////} otherwise {
          ////  tempCombineLineMemIdx := RegNext(tempCombineLineMemIdx)
          ////}

          ////when (
          ////  tempInp.fire
          ////  &&
          ////  tempInp.bakCntWillBeDone()
          ////) {
          ////  //tempCombineLineMemIdx := 0
          ////  rTempCombineLineMemIdx := 0
          ////} elsewhen (tempOutp.fire) {
          ////  rTempCombineLineMemIdx := rTempCombineLineMemIdx + 1
          ////}
          ////--------
          ////when (tempOutp.fire) {
          ////  when (tempInp.bakCntWillBeDone()) {
          ////    nextTempCombineLineMemIdx := 0x0
          ////  } otherwise {
          ////    nextTempCombineLineMemIdx := rTempCombineLineMemIdx + 1
          ////  }
          ////}
          ////--------

          ////when (
          ////  //tempOutp.fire
          ////  //tempOutp.ready
          ////  //tempOutp.ready
          ////  !(tempOutp.valid && !tempOutp.ready)
          ////) {
          ////  tempCombineLineMemIdx := (
          ////    tempOutp.cnt(
          ////      //log2Up(
          ////      //  //params.oneLineMemSize
          ////      //  params.oneLineMemSize
          ////      //) - 1
          ////      //downto 0
          ////      tempCombineLineMemIdx.bitsRange
          ////    )
          ////  )
          ////} otherwise {
          ////  tempCombineLineMemIdx := RegNext(tempCombineLineMemIdx)
          ////}
          //def bgSubLineMemArrIdx = params.getBgSubLineMemArrIdx(
          //  addr=(
          //    //rTempCombineLineMemIdx
          //    tempCombineLineMemIdx
          //  )
          //)
          //val dbgTestCombinePipe1_bgSubLineMemArrIdx = UInt(
          //  bgSubLineMemArrIdx.getWidth bits
          //)
          //  .setName("dbgTestCombinePipe1_bgSubLineMemArrIdx")
          //dbgTestCombinePipe1_bgSubLineMemArrIdx := (
          //  bgSubLineMemArrIdx
          //)

          //def objSubLineMemArrIdx = (
          //  params.getObjSubLineMemArrIdx(
          //    addr=(
          //      //rTempCombineLineMemIdx
          //      tempCombineLineMemIdx
          //    )
          //  )
          //)
          //val dbgTestCombinePipe1_objSubLineMemArrIdx = UInt(
          //  objSubLineMemArrIdx.getWidth bits
          //)
          //  .setName("dbgTestCombinePipe1_objSubLineMemArrIdx")
          //dbgTestCombinePipe1_objSubLineMemArrIdx := (
          //  objSubLineMemArrIdx
          //)

          //when (clockDomain.isResetActive) {
          //  tempOutp.stage2 := tempOutp.stage2.getZero
          //  ////combinePipeStage1Busy := False
          //  ////lineFifo.io.pop.ready := True
          //  ////lineFifo.io.pop.ready := False
          //  ////objLineFifo.io.pop.ready := False
          //  //tempOutp.lineMemIdx := 0
          //  for (jdx <- 0 until 1 << rCombineLineMemArrIdx.getWidth) {
          //    rdBgSubLineMemArr(jdx).addrVec(
          //      RdBgSubLineMemArrInfo.combineIdx
          //    ) := 0x0
          //    rdObjSubLineMemArr(jdx).addrVec(
          //      RdObjSubLineMemArrInfo.combineIdx
          //    ) := 0x0
          //  }
          //} otherwise {
          //  //tempOutp.lineMemIdx := (
          //  //  //rTempCombineLineMemIdx
          //  //  tempCombineLineMemIdx
          //  //  //tempOutp.cnt(
          //  //  //  tempOutp.lineMemIdx.bitsRange
          //  //  //)
          //  //)
          //  //tempOutp.stage1.lineMemIdx := 0
          //  //when (
          //  //  //tempOutp.fire
          //  //  //tempOutp.valid
          //  //  tempInp.fire
          //  //) {
          //  //--------
          //  // BEGIN: new code, with `readSync`
          //  switch (rCombineLineMemArrIdx) {
          //    for (jdx <- 0 until rdBgSubLineMemArr.size) {
          //      is (jdx) {
          //        tempOutp.stage2.rdBg := (
          //          combinePipeOut1BgVec(jdx).stage2.rdBg
          //        )
          //        tempOutp.stage2.rdObj := (
          //          combinePipeOut1ObjVec(jdx).stage2.rdObj
          //        )
          //      }
          //    }
          //  }
          //  // END: new code, with `readSync`
          //  //for (jdx <- 0 until 1 << rCombineLineMemArrIdx.getWidth) {
          //  ////haltCombinePipe2FifoPush := 
          //  //  //when (!haltCombinePipe2) {
          //  //  //  haltCombinePipe2FifoPush 
          //  //  //}

          //  //  //when (
          //  //  //  //True
          //  //  //  //tempOutp.fire
          //  //  //  //tempInp.fire
          //  //  //  //!haltCombinePipe2
          //  //  //  //&& combinePipe2Fifo.push.fire
          //  //  //  //tempOutp.ready
          //  //  //  //tempInp.fire
          //  //  //  tempOutp.fire
          //  //  //) {
          //  //    //--------
          //  //    // We no longer need the `switch` statement here since we
          //  //    // are just reading
          //  //    //rdBgSubLineMemArr(jdx).addrVec(
          //  //    //  RdBgSubLineMemArrInfo.combineIdx
          //  //    //) := bgSubLineMemArrIdx
          //  //    //rdObjSubLineMemArr(jdx).addrVec(
          //  //    //  RdObjSubLineMemArrInfo.combineIdx
          //  //    //) := objSubLineMemArrIdx
          //  //    //--------
          //  //  //} otherwise {
          //  //  //  rdBgSubLineMemArr(jdx).addrVec(
          //  //  //    RdBgSubLineMemArrInfo.combineIdx
          //  //  //  ) := RegNext(
          //  //  //    rdBgSubLineMemArr(jdx).addrVec(
          //  //  //      RdBgSubLineMemArrInfo.combineIdx
          //  //  //    )
          //  //  //  )
          //  //  //  rdObjSubLineMemArr(jdx).addrVec(
          //  //  //    RdObjSubLineMemArrInfo.combineIdx
          //  //  //  ) := RegNext(
          //  //  //    rdObjSubLineMemArr(jdx).addrVec(
          //  //  //      RdObjSubLineMemArrInfo.combineIdx
          //  //  //    )
          //  //  //  )
          //  //  //}
          //  //}
          //  //--------
          //}
          ////tempOut.stage0 := tempInp.stage0
          ////stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
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
            tempOutp.stage2 := tempOutp.stage2.getZero
          } otherwise {
            if (idx != 1) {
              tempOutp.stage2 := tempInp.stage2
            } 
            //else {
            //  tempOutp.stage2 := (
            //    combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage2
            //  )
            //}
          }
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(
          3
          //4
        )
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          //val tempCombineLineMemIdx = createTempCnt(
          //  tempOutp=tempOutp,
          //  pipeStageIdx=idx
          //)

          //val tempCombineLineMemIdxArr = new ArrayBuffer[UInt]()
          //val combinePipe2_tempCombineLineMemIdx = KeepAttribute(
          //  Reg(
          //    UInt(
          //      log2Up(
          //        //params.oneLineMemSize
          //        params.oneLineMemSize
          //      ) bits
          //    )
          //  )
          //    .init(0x0)
          //    .setName("dbgTestCombinePipe2_tempCombineLineMemIdx")
          //)
          //val tempCombineLineMemIdx = UInt(
          //  log2Up(params.oneLineMemSize) bits
          //)
          //  .setName("dbgTestCombinePipe2_tempCombineLineMemIdx")
          //tempCombineLineMemIdx := (
          //  tempInp.cnt(tempCombineLineMemIdx.bitsRange)
          //)
          //fifo.io.push.valid := tempInp.valid
          //val tempCombineLineMemIdx = KeepAttribute(
          //  //Reg(
          //    UInt(
          //      log2Up(
          //        //params.oneLineMemSize
          //        params.oneLineMemSize
          //      ) bits
          //    )
          //  //)
          //    //.init(0x0)
          //    .setName("dbgTestCombinePipe2_tempCombineLineMemIdx")
          //    //.setName("tempCombineLineMemIdx")
          //)
          ////when (!(tempOutp.valid && !tempOutp.ready)) {
          //  tempCombineLineMemIdx := tempInp.cnt(
          //    tempCombineLineMemIdx.bitsRange
          //  )
          ////} otherwise {
          ////  tempCombineLineMemIdx := RegNext(tempCombineLineMemIdx)
          ////}

          //when (intnlChangingRowRe) {
          //  tempCombineLineMemIdx := 0
          //} elsewhen (tempOutp.fire) {
          //  tempCombineLineMemIdx := tempCombineLineMemIdx + 1
          //}

          //combinePipe2_tempCombineLineMemIdx := (
          //  //tempInp.cnt(
          //  //  log2Up(
          //  //    //params.oneLineMemSize
          //  //    params.oneLineMemSize
          //  //  ) - 1
          //  //  downto 0
          //  //)
          //  //tempInp.stage1.lineMemIdx
          //  tempCombineLineMemIdx
          //)
          //val combinePipe2Fifo = AsyncReadFifo
          //when (tempInp.fire) {
          //}
          //val combinePipe2_tempCombineLineMemIdx = (
          //  //RegNext(tempCombineLineMemIdx)
          //  //  .init(0x0)
          //  //  .setName("dbgTestCombinePipe2_tempCombineLineMemIdx")
          //  UInt(tempCombineLineMemIdx.getWidth bits)
          //    .setName("dbgTestCombinePipe2_tempCombineLineMemIdx")
          //)
          //when (tempInp.fire) {
          //  combinePipe2_tempCombineLineMemIdx := (
          //    tempInp.cnt(
          //      log2Up(
          //        //params.oneLineMemSize
          //        params.oneLineMemSize
          //      ) - 1
          //      downto 0
          //    )
          //    //tempCombineLineMemIdx
          //  )
          //} otherwise {
          //  combinePipe2_tempCombineLineMemIdx := RegNext(
          //    combinePipe2_tempCombineLineMemIdx
          //  )
          //}
          val rTempCnt = Reg(UInt(tempInp.cnt.getWidth bits)) init(0x0)
          def myLineMemIdx = (
            //combinePipeIn2PxReadFifo.io.pop.lineMemIdx
            tempInp.lineMemIdx
            //tempInp.cnt
          )
          def bgSubLineMemArrIdx = params.getBgSubLineMemArrIdx(
            addr=(
              myLineMemIdx
              //tempInp.lineMemIdx
              //tempCombineLineMemIdx
            )
          )
          val dbgTestCombinePipe2_bgSubLineMemArrIdx = UInt(
            bgSubLineMemArrIdx.getWidth bits
          )
            .setName("dbgTestCombinePipe2_bgSubLineMemArrIdx")
          dbgTestCombinePipe2_bgSubLineMemArrIdx := (
            bgSubLineMemArrIdx
          )

          def bgSubLineMemArrElemIdx = params.getBgSubLineMemArrElemIdx(
            addr=(
              myLineMemIdx
              //tempInp.lineMemIdx
              //tempCombineLineMemIdx
            )
          )
          val dbgTestCombinePipe2_bgSubLineMemArrElemIdx = UInt(
            bgSubLineMemArrElemIdx.getWidth bits
          )
            .setName("dbgTestCombinePipe2_bgSubLineMemArrElemIdx")
          dbgTestCombinePipe2_bgSubLineMemArrElemIdx := (
            bgSubLineMemArrElemIdx
          )

          def objSubLineMemArrIdx = (
            params.getObjSubLineMemArrIdx(
              addr=(
                myLineMemIdx
                //tempInp.lineMemIdx
                //tempCombineLineMemIdx
              )
            )
          )
          val dbgTestCombinePipe2_objSubLineMemArrIdx = UInt(
            objSubLineMemArrIdx.getWidth bits
          )
            .setName("dbgTestCombinePipe2_objSubLineMemArrIdx")
          dbgTestCombinePipe2_objSubLineMemArrIdx := (
            objSubLineMemArrIdx
          )

          def objSubLineMemArrElemIdx = (
            params.getObjSubLineMemArrElemIdx(
              addr=(
                myLineMemIdx
                //tempInp.lineMemIdx
                //tempCombineLineMemIdx
              )
            )
          )
          val dbgTestCombinePipe2_objSubLineMemArrElemIdx = UInt(
            objSubLineMemArrElemIdx.getWidth bits
          )
            .setName("dbgTestCombinePipe2_objSubLineMemArrElemIdx")
          dbgTestCombinePipe2_objSubLineMemArrElemIdx := (
            objSubLineMemArrElemIdx
          )

          when (clockDomain.isResetActive) {
            //tempOutp.stage1 := tempOutp.stage1.getZero
            ////combinePipeStage1Busy := False
            ////lineFifo.io.pop.ready := True
            ////lineFifo.io.pop.ready := False
            ////objLineFifo.io.pop.ready := False
            tempOutp.stage3 := tempOutp.stage3.getZero
          } otherwise {
            switch (rCombineLineMemArrIdx) {
              for (jdx <- 0 until 1 << rCombineLineMemArrIdx.getWidth) {
                is (jdx) {
                  //--------
                  //case class BufFifoElem() extends Bundle {
                  //  val bgRd = Vec.fill(params.bgTileSize2d.x)(
                  //    BgSubLineMemEntry()
                  //  )
                  //  val objRd = Vec.fill(params.objTileSize2d.x)(
                  //    ObjSubLineMemEntry()
                  //  )
                  //  val lineMemIdx = cloneOf(tempInp.lineMemIdx)
                  //}
                  //def bufFifoDepth = (
                  //  8
                  //)
                  //def bufFifoPushStallGeAmountCanPop = (
                  //  4
                  //)
                  //val bufFifo = AsyncReadFifo(
                  //  dataType=BufFifoElem(),
                  //  depth=bufFifoDepth,
                  //)
                  //bufFifo.io.push.valid := tempInp.valid
                  //haltCombinePipeIn2 := (
                  //  bufFifo.io.misc.amountCanPop
                  //  >= bufFifoPushStallGeAmountCanPop
                  //)
                  //bufFifo.io.pop.ready := True
                  // BEGIN: new code, for reading synchronously
                  //when (
                  //  //tempOutp.fire
                  //  //tempInp.fire
                  //  //tempOutp.fire
                  //  combinePipeIn2PxReadFifo.io.pop.fire
                  //) {
                    //--------
                    def tempRdBg = (
                      //rdBgSubLineMemArr(jdx).dataVec(
                      //  RdBgSubLineMemArrInfo.combineIdx
                      //)
                      ////combinePipeIn2PxReadFifo.io.pop.rdBg
                      tempInp.stage2.rdBg
                    )
                    def tempRdObj = (
                      //rdObjSubLineMemArr(jdx).dataVec(
                      //  RdObjSubLineMemArrInfo.combineIdx
                      //)
                      ////combinePipeIn2PxReadFifo.io.pop.rdObj
                      //combinePipeIn1ObjMemReadSyncArr
                      tempInp.stage2.rdObj
                    )
                    tempOutp.stage3.ext.bgRdSubLineMemEntry := (
                      tempRdBg(bgSubLineMemArrElemIdx)
                    )
                    tempOutp.stage3.ext.objRdSubLineMemEntry := (
                      tempRdObj(objSubLineMemArrElemIdx)
                    )
                    //--------
                  //} otherwise {
                  //  tempOutp.stage2.ext.bgRdSubLineMemEntry := (
                  //    //tempOutp.stage2.ext.bgRdSubLineMemEntry.getZero
                  //    RegNext(tempOutp.stage2.ext.bgRdSubLineMemEntry)
                  //  )
                  //  tempOutp.stage2.ext.objRdSubLineMemEntry := (
                  //    //tempOutp.stage2.ext.objRdSubLineMemEntry.getZero
                  //    RegNext(tempOutp.stage2.ext.objRdSubLineMemEntry)
                  //  )
                  //}
                  // END: new code, for reading synchronously
                  //--------
                  // BEGIN: old-style code, for reading asynchronously
                  //tempOutp.stage3.ext.bgRdSubLineMemEntry := (
                  //  bgSubLineMemArr(jdx).readAsync(
                  //    bgSubLineMemArrIdx
                  //  )(bgSubLineMemArrElemIdx)
                  //)
                  //tempOutp.stage3.ext.objRdSubLineMemEntry := (
                  //  objSubLineMemArr(jdx).readAsync(
                  //    objSubLineMemArrIdx
                  //  )(objSubLineMemArrElemIdx)
                  //)
                  // END: old-style code, for reading asynchronously
                  //--------
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
            tempOutp.stage3 := tempOutp.stage3.getZero
          } otherwise {
            //tempOutp.stage3 := tempInp.stage3
            if (idx != 1) {
              tempOutp.stage3 := tempInp.stage3
            } else {
              tempOutp.stage3 := (
                combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage3
              )
            }
          }
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(
          //2
          //3
          5
          //wrBgObjPipeNumStages + 2
        )
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          def inpExt = (
            //tempInp.stage1.ext
            tempInp.stage3.ext
          )


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
            tempOutp.stage5 := tempOutp.stage5.getZero
          } otherwise {
            when (
              tempInp.valid
            ) {
              //tempOutp.stage5.ext := (
              //  rCombinePipeOut1Ext
              //)
              tempOutp.objHiPrio := (
                // sprites take priority upon a tie, hence `<=`
                //tempInp.objRdLineMemEntry.prio(
                //  tempInp.bgRdLineMemEntry.prio.bitsRange
                //) <= tempInp.bgRdLineMemEntry.prio

                //tempInp.objRdLineMemEntry.prio
                //<= tempInp.bgRdLineMemEntry.prio
                inpExt.objRdSubLineMemEntry.prio
                <= inpExt.bgRdSubLineMemEntry.prio
                //rCombinePipeOut1Ext.objRdLineMemEntry.prio
                //<= rCombinePipeOut1Ext.bgRdLineMemEntry.prio
              )
            } otherwise {
              tempOutp.stage5 := RegNext(tempOutp.stage5)
            }
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
            tempOutp.stage5 := tempOutp.stage5.getZero
          } otherwise {
            //tempOutp.stage5 := tempInp.stage5
            if (idx != 1) {
              tempOutp.stage5 := tempInp.stage5
            } else {
              tempOutp.stage5 := (
                combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage5
              )
            }
          }
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(
          //3
          //4
          6
          //wrBgObjPipeNumStages + 3
        )
      )(
        pipeStageMainFunc=(
          stageData: DualPipeStageData[Stream[CombinePipePayload]],
          idx: Int,
        ) => {
          val tempInp = stageData.pipeIn(idx)
          val tempOutp = stageData.pipeOut(idx)

          def inpExt = (
            //tempInp.stage1.ext
            tempInp.stage3.ext
          )

          when (clockDomain.isResetActive) {
            tempOutp.stage6 := tempOutp.stage6.getZero
          } otherwise {
            // BEGIN: Debug comment this out; later
            switch (Cat(
              inpExt.bgRdSubLineMemEntry.col.a,
              inpExt.objRdSubLineMemEntry.col.a,
              tempInp.objHiPrio
            )) {
              is (B"111") {
                tempOutp.col := inpExt.objRdSubLineMemEntry.col.rgb
              }
              is (B"110") {
                tempOutp.col := inpExt.bgRdSubLineMemEntry.col.rgb
              }
              is (M"10-") {
                tempOutp.col := inpExt.bgRdSubLineMemEntry.col.rgb
              }
              is (M"01-") {
                tempOutp.col := inpExt.objRdSubLineMemEntry.col.rgb
              }
              //is (M"00-")
              default {
                tempOutp.col := tempOutp.col.getZero
              }
            }
            // END: Debug comment this out; later
            //tempOutp.col := inpExt.bgRdSubLineMemEntry.col.rgb
            //tempOutp.col := inpExt.objRdSubLineMemEntry.col.rgb
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
            tempOutp.stage6 := tempOutp.stage6.getZero
          } otherwise {
            //tempOutp.stage6 := tempInp.stage6
            if (idx != 1) {
              tempOutp.stage6 := tempInp.stage6
            } else {
              tempOutp.stage6 := (
                combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage6
              )
            }
          }
        },
      )
      HandleDualPipe(
        stageData=stageData.craft(
          //4
          //5
          7
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
            tempOutp.stage7 := tempOutp.stage7.getZero
          } otherwise {
            tempOutp.combineWrLineMemEntry.addr := (
              //tempInp.cnt
              //tempInp.stage1.lineMemIdx
              tempInp.cnt
              (
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
            tempOutp.stage7 := tempOutp.stage7.getZero
          } otherwise {
            //tempOutp.stage7 := tempInp.stage7
            if (idx != 1) {
              tempOutp.stage7 := tempInp.stage7
            } else {
              tempOutp.stage7 := (
                combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage7
              )
            }
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
          addr=(
            combinePipeLast.cnt
            //combinePipeLast.stage1.lineMemIdx
          )
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
        //--------
        // BEGIN: new code, with muxing for single `.write()` call
        val tempObjLineMemEntry = Vec.fill(params.objTileSize2d.x)(
          ObjSubLineMemEntry()
        ).getZero
        //objWriter.addrVec(rCombineLineMemArrIdx) := tempObjArrIdx
        //objWriter.dataVec(rCombineLineMemArrIdx) := tempObjLineMemEntry
        //objWriter.enVec(rCombineLineMemArrIdx) := True
        objWriter.addrVec(1) := tempObjArrIdx
        objWriter.dataVec(1) := tempObjLineMemEntry
        objWriter.enVec(1) := True
        // END: new code, with muxing for single `.write()` call
        //--------
        // BEGIN: old code, no muxing for single `.write()` call
        //switch (
        //  //rCombineWrLineMemArrIdx
        //  //rCombineWrLineMemArrIdx
        //  rCombineLineMemArrIdx
        //  //combinePipeLast.lineMemArrIdx
        //) {
        //  for (
        //    //jdx <- 0 to (1 << rCombineWrLineMemArrIdx.getWidth) - 1
        //    jdx <- 0 to (1 << rCombineLineMemArrIdx.getWidth) - 1
        //    //jdx <- 0 
        //    //to (1 << combinePipeLast.lineMemArrIdx.getWidth) - 1
        //  ) {
        //    // BEGIN: come back to this later
        //    val tempObjLineMemEntry = Vec.fill(params.objTileSize2d.x)(
        //      ObjSubLineMemEntry()
        //    ).getZero
        //    //def tempAddr = combinePipeLast.cnt(
        //    //  log2Up(params.oneLineMemSize) - 1 downto 0
        //    //)
        //    //def tempAddrSingle = combinePipeLast.cnt(
        //    //  log2Up(params.oneLineMemSize) - 1 downto 0
        //    //)

        //    // END: come back to this later
        //    is (jdx) {
        //      val tempIdx = (
        //        if (jdx == 0) {
        //          params.numLineMemsPerBgObjRenderer - 1
        //        } else {
        //          jdx - 1
        //        }
        //      )
        //      // BEGIN: come back to this later
        //      //objSubLineMemArr(tempIdx).write(
        //      //  //address=combinePipeLast.bakCnt(
        //      //  //  log2Up(params.oneLineMemSize) - 1 downto 0
        //      //  //),
        //      //  //data=combinePipeLast.combineWrLineMemEntry,
        //      //  address=tempAddrSingle,
        //      //  data=tempObjLineMemEntry
        //      //)
        //      objSubLineMemArr(jdx).write(
        //        address=tempObjArrIdx,
        //        data=tempObjLineMemEntry,
        //      )
        //      //switch (tempArrIdx) {
        //      //  for (kdx <- 0 to (1 << tempArrIdx.getWidth) - 1) {
        //      //    is (kdx) {
        //      //      //when (
        //      //      //  Cat(
        //      //      //    dbgTestCombinePipeLastTempArrIdx,
        //      //      //    tempAddr
        //      //      //  ).asUInt <= params.oneLineMemSize - 1
        //      //      //) {
        //      //        objSubLineMemA2d(jdx)(kdx).write(
        //      //          address=tempAddr,
        //      //          data=tempObjLineMemEntry,
        //      //        )
        //      //      //}
        //      //    }
        //      //  }
        //      //}
        //      // END: come back to this later
        //      //dbgObjLineMemVec(tempIdx)(tempAddr) := tempObjLineMemEntry
        //    }
        //  }
        //}
        // END: old code, no muxing for single `.write()` call
        //--------
        outp.col := combinePipeLast.combineWrLineMemEntry.col.rgb
        //outp.col := combinePipeOut.last.combineWrLineMemEntry.col.rgb
        //rdPhysCalcPosEn := True
        //--------
      } otherwise {
        //objWriter.enVec(rCombineLineMemArrIdx) := False
        objWriter.enVec(1) := False
        outp.col := rPastOutp.col
        //rdPhysCalcPosEn := False
      }
      when (combinePipeLastThrown.fire) {
        rdPhysCalcPosEn := True
      } otherwise {
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
