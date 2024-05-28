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
import libcheesevoyage.general.PipeHelper
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
//import libcheesevoyage.general.FpgacpuPipeForkBlocking
//import libcheesevoyage.general.FpgacpuPipeForkEager
//import libcheesevoyage.general.FpgacpuPipeJoin
import libcheesevoyage.general.FpgacpuRamSimpleDualPort
//import libcheesevoyage.general.PipeMemSimpleDualPort
//import libcheesevoyage.general.PipeMemSimpleDualPortIo
import libcheesevoyage.general.PipeMemRmw
import libcheesevoyage.general.PipeMemRmwIo
import libcheesevoyage.general.PipeMemRmwPayloadExt
import libcheesevoyage.general.SamplePipeMemRmwModType
import libcheesevoyage.general.PipeMemRmwDualRdTypeDisabled
import libcheesevoyage.general.PipeMemRmwPayloadBase
import spinal.lib.misc.pipeline._

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
  //physFbSize2dScalePow: ElabVec2[Int],  // the integer value to logical
  //                            // left shift `intnlFbSize2d` by to obtain
  //                            // the physical resolution of the video
  //                            // signal.
  //                            // This is used to duplicate generated
  //                            // pixels so that the generated video
  //                            // signal can fill the screen.
  physFbSize2dScale: ElabVec2[Int], // the `ElabVec2[Int]` to elementwise
                              // multiply `intnlFbSize2d` by to obtain
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
  objTileWidthRshift: Int,
  objAffineTileSize2dPow: ElabVec2[Int],
  objAffineTileWidthRshift: Int,
  //numBgTilesPow: Int,         // power of two for total number of
  //                            // background tiles
  //                            // (how much memory to reserve for BG tiles)
  //numObjTilesPow: Int,        // power of two for total number of
  //                            // sprite tiles
  //                            // (how much memory to reserve for sprite
  //                            // tiles)
  numBgTiles: Int,
  numColorMathTiles: Int,
  numObjTiles: Int,
  numObjAffineTiles: Int,
  bgSize2dInTilesPow: ElabVec2[Int], // power of two for width/height of a
                              // background
                              // (in number of tiles)
  numBgsPow: Int,             // power of two for the total number of
                              // backgrounds
  numObjsPow: Int,            // power of two for the total number of 
                              // sprites
  numObjsAffinePow: Int,
  //numObjsPerScanline: Int,    // how many sprites to process in one
  //                            // scanline (possibly one per cycle?)
  //numColsInPalPow: Int,       // power of two for how many colors in the
  //                            // palette
  numColsInBgPalPow: Int,     // power of two for how many colors in the
                              // background palette
  numColsInObjPalPow: Int,    // power of two for how many colors in the
                              // sprite palette
  //--------
  noColorMath: Boolean=false,
  noAffineBgs: Boolean=false,
  noAffineObjs: Boolean=false,
  fancyObjPrio: Boolean=true, // whether sprite-sprite priority
                              // comparisons should take sprite-BG
                              // priority into account
  //--------
  //bgTileMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
  //colorMathTileMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
  //objTileMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
  bgTileMemInit: Option[ArrayBuffer[Gpu2dTileSlice]]=None,
  colorMathTileMemInit: Option[ArrayBuffer[Gpu2dTileSlice]]=None,
  objTileMemInit: Option[ArrayBuffer[Gpu2dTileSlice]]=None,
  objAffineTileMemInit: Option[ArrayBuffer[UInt]]=None,
  //objAffineTileMemInit: Option[ArrayBuffer[BigInt]]=None,
  bgPalEntryMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
  colorMathPalEntryMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
  objPalEntryMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
  //--------
  bgTileArrRamStyle: String="block",
  objTileArrRamStyle: String="block",
  objAffineTileArrRamStyle: String="block",
  bgEntryArrRamStyle: String="block",
  //bgAttrsArrRamStyle: String="block",
  objAttrsArrRamStyle: String="auto",
  bgPalEntryArrRamStyle: String="auto",
  objPalEntryArrRamStyle: String="auto",

  //bgLineArrRamStyle: String="block",
  //objLineArrRamStyle: String="block",
  //combineLineArrRamStyle: String="block",
  lineArrRamStyle: String="block",
  //--------
  //--------
) {
  assert(
    //physFbSize2dScale.x == (1 << log2Up(physFbSize2dScale.x))
    physFbSize2dScale.x > 0
  )
  assert(
    //physFbSize2dScale.y == (1 << log2Up(physFbSize2dScale.y))
    physFbSize2dScale.y > 0
  )
  //--------
  def combineBgSubLineMemVecElemSize = (
    //min(
      bgTileSize2d.x,
    //  4,
    //)
  )
  def combineBgSubLineMemArrSize = (
    //numLineMemsPerBgObjRenderer
    max(
      1,
      bgTileSize2d.x / combineBgSubLineMemVecElemSize
    )
  )
  //println(
  //  s"${combineBgSubLineMemVecElemSize} "
  //  + s"${combineBgSubLineMemArrSize}"
  //)
  //--------
  //def bgAffineTileSize2dPow = ElabVec2[Int](
  //  x=bgTileSize2dPow.x + 1,
  //  y=bgTileSize2dPow.y + 1,
  //)
  //def objAffineTileSize2dPow = ElabVec2[Int](
  //  x=objTileSize2dPow.x + 1,
  //  y=objTileSize2dPow.y + 1,
  //)
  //def objAffineTileSize2d = ElabVec2[Int](
  //  x=1 << (objTileSize2dPow.x + 1),
  //  y=1 << (objTileSize2dPow.y + 1),
  //)
  def objSliceTileWidthPow = (
    objTileSize2dPow.x - objTileWidthRshift
    //objTileSize2dPow.x
  )
  def objSliceTileWidth = 1 << objSliceTileWidthPow
  //def objTileSize2d = ElabVec2[Int](
  //  x=1 << objTileSize2dPow.x,
  //  y=1 << objTileSize2dPow.y,
  //)
  def objAffineSliceTileWidthPow = (
    objAffineTileSize2dPow.x - objAffineTileWidthRshift
    //objAffineTileSize2dPow.x
  )
  def objAffineSliceTileWidth = 1 << objAffineSliceTileWidthPow
  def objAffineTileSize2d = ElabVec2[Int](
    x=1 << objAffineTileSize2dPow.x,
    y=1 << objAffineTileSize2dPow.y,
  )
  def objAffineDblTileSize2dPow = ElabVec2[Int](
    x=objAffineTileSize2dPow.x + 1,
    y=objAffineTileSize2dPow.y + 1,
  )
  def objAffineDblTileSize2d = ElabVec2[Int](
    //x=1 << (objAffineTileSize2dPow.x + 1),
    //y=1 << (objAffineTileSize2dPow.y + 1),
    x=1 << objAffineDblTileSize2dPow.x,
    y=1 << objAffineDblTileSize2dPow.y,
  )
  //--------
  //def numBgTileMems = 3
  //def colorMathBgTileMemIdx = numBgTileMems - 1
  //def numBgEntryMems = 2

  // need to increase this to 3 for affine backgrounds
  def totalNumBgKinds = (
    1
    + (
      if (!noColorMath) {
        1
      } else {
        0
      }
    ) + (
      //if (!noAffineBgs) {
      //  1
      //} else {
        0
      //}
    )
  )
  def numBgMemsPerNonPalKind = (
    2
    //+ (
    //  if (!noColorMath) {
    //    1
    //  } else {
    //    0
    //  }
    //)
    + (
      //if (!noAffineBgs) {
      //  1
      //} else {
        0
      //}
    )
  )

  //def numObjMemsPerKind = 2
  def numObjMemsPerKind(
    isTileMem: Boolean,
  ) = (
    1
    + (
      if (
        !noAffineObjs
        && !isTileMem
      ) {
        1
      } else {
        0
      }
    )
  )
  //def numObjPalMems = 2
  //def numColorMathMemsPerNonPalKind = 2
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
  ////def numTiles = 1 << numTilesPow
  //def numBgTiles = 1 << numBgTilesPow
  def numBgTilesPow = log2Up(numBgTiles)
  def numColsInBgPal = 1 << numColsInBgPalPow
  def numColorMathTilesPow = log2Up(numColorMathTiles)
  def numObjs = 1 << numObjsPow
  def numObjsAffine = 1 << numObjsAffinePow
  //def numObjTiles = 1 << numObjTilesPow
  def numObjTilesPow = log2Up(numObjTiles)
  def numObjAffineTilesPow = log2Up(numObjAffineTiles)
  def numColsInObjPal = 1 << numColsInObjPalPow
  //--------
  //def physToIntnlScalePow = ElabVec2[Int](
  //  x=physFbSize2dScalePow.x,
  //  y=physFbSize2dScalePow.y,
  //)
  //def physToIntnlScalePow = physFbSize2dScalePow
  //def physFbSize2d = ElabVec2[Int](
  //  //x=intnlFbSize2d.x * physFbSize2dScale.x,
  //  //y=intnlFbSize2d.y * physFbSize2dScale.y,
  //  //x=intnlFbSize2d.x << physFbSize2dScalePow.x,
  //  //y=intnlFbSize2d.y << physFbSize2dScalePow.y,
  //  x=intnlFbSize2d.x << physFbSize2dScalePow.x,
  //  y=intnlFbSize2d.y << physFbSize2dScalePow.y,
  //)
  def physFbSize2d = ElabVec2[Int](
    x=intnlFbSize2d.x * physFbSize2dScale.x,
    y=intnlFbSize2d.y * physFbSize2dScale.y,
  )
  //def fbSize2dInPxs = ElabVec2[Int](
  //  x=intnlFbSize2d.x,
  //  y=intnlFbSize2d.y,
  //)
  def fbSize2dInPxs = intnlFbSize2d
  //def physToBgTilesScalePow = ElabVec2[Int](
  //  x=physToIntnlScalePow.x + bgTileSize2dPow.x,
  //  y=physToIntnlScalePow.y + bgTileSize2dPow.y,
  //)

  //def fbSize2dInBgTiles = ElabVec2[Int](
  //  x=intnlFbSize2d.x >> bgTileSize2dPow.x,
  //  y=intnlFbSize2d.y >> bgTileSize2dPow.y,
  //)

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
    //log2Up(oneLineMemSize) - objTileSize2dPow.x
    log2Up(oneLineMemSize) - objSliceTileWidthPow
    //log2Up(oneLineMemSize) - objTileWidthRshift
  )
  def objSubLineMemArrSize = 1 << objSubLineMemArrSizePow
  def getObjSubLineMemArrIdx(
    addr: UInt
  ): UInt = {
    assert(addr.getWidth >= log2Up(oneLineMemSize))
    //addr(log2Up(oneLineMemSize) - 1 downto objTileSize2dPow.x)
    addr(log2Up(oneLineMemSize) - 1 downto objSliceTileWidthPow)
    //addr(log2Up(oneLineMemSize) - 1 downto objTileWidthRshift)
  }
  //def getObjSubLineMemArrGridIdx(
  //  addr: UInt
  //): Bool = {
  //  assert(addr.getWidth >= log2Up(oneLineMemSize))
  //  //addr(objTileSize2dPow.x downto objTileSize2dPow.x)
  //  addr(objTileSize2dPow.x)
  //}
  def getObjSubLineMemArrElemIdx(
    addr: UInt
  ): UInt = {
    assert(addr.getWidth >= log2Up(oneLineMemSize))
    //addr(objTileSize2dPow.x - 1 downto 0)
    addr(objSliceTileWidthPow - 1 downto 0)
  }

  def myDbgObjAffineTileWidthPow = (
    //objAffineDblTileSize2dPow.x
    objAffineSliceTileWidthPow
  )
  def myDbgObjAffineTileWidth = (
    //objAffineDblTileSize2d.x
    objAffineSliceTileWidth
  )
  def objAffineSubLineMemArrSizePow = (
    //log2Up(oneLineMemSize) - objAffineDblTileSize2dPow.x
    //log2Up(oneLineMemSize) - objAffineTileSize2dPow.x
    log2Up(oneLineMemSize) - objAffineSliceTileWidthPow
    //log2Up(oneLineMemSize) - objAffineTileWidthRshift //(objAffineTileWidthRshift + 1)
  )
  def objAffineSubLineMemArrSize = 1 << objAffineSubLineMemArrSizePow
  def getObjAffineSubLineMemArrIdx(
    addr: UInt
  ): UInt = {
    assert(addr.getWidth >= log2Up(oneLineMemSize))
    //addr(log2Up(oneLineMemSize) - 1 downto objAffineDblTileSize2dPow.x)
    //addr(log2Up(oneLineMemSize) - 1 downto objAffineTileSize2dPow.x)
    addr(log2Up(oneLineMemSize) - 1 downto objAffineSliceTileWidthPow)
    //addr(
    //  log2Up(oneLineMemSize) - 1
    //  downto objAffineTileWidthRshift //+ 1
    //)
  }
  //def getObjAffineSubLineMemArrGridIdx(
  //  addr: UInt
  //): Bool = {
  //  assert(addr.getWidth >= log2Up(oneLineMemSize))
  //  addr(objAffineDblTileSize2dPow.x)
  //}
  def getObjAffineSubLineMemArrElemIdx(
    addr: UInt
  ): UInt = {
    assert(addr.getWidth >= log2Up(oneLineMemSize))
    //addr(objAffineDblTileSize2dPow.x - 1 downto 0)
    //addr(objAffineTileSize2dPow.x - 1 downto 0)
    addr(objAffineSliceTileWidthPow - 1 downto 0)
  }
  //--------
  def tempObjTileWidthPow(isAffine: Boolean) = (
    if (!isAffine) {
      //objTileSize2dPow.x
      objSliceTileWidthPow
    } else {
      //objAffineDblTileSize2dPow
      //ElabVec2[Int](
      //  x=objAffineSliceTileWidthPow,
      //  y=objAffineDblTileSize2dPow.y,
      //)
      //objAffineSliceTileWidthPow
      myDbgObjAffineTileWidthPow
    }
  )
  def tempObjTileWidth(isAffine: Boolean) = (
    if (!isAffine) {
      //objTileSize2d.x
      objSliceTileWidth
    } else {
      //objAffineDblTileSize2d
      //objAffineSliceTileWidth
      myDbgObjAffineTileWidth
    }
  )

  def tempObjTileWidthPow1(isAffine: Boolean) = (
    if (!isAffine) {
      objTileSize2dPow.x
    } else {
      objAffineTileSize2dPow.x
      //ElabVec2[Int](
      //  x=objAffineSliceTileWidthPow,
      //  y=objAffineDblTileSize2dPow.y,
      //)
      //objAffineSliceTileWidthPow
      //myDbgObjAffineTileWidthPow
    }
  )
  def tempObjTileWidth1(isAffine: Boolean) = (
    if (!isAffine) {
      objTileSize2d.x
    } else {
      objAffineTileSize2d.x
      //objAffineSliceTileWidth
      //myDbgObjAffineTileWidth
    }
  )

  def tempObjTileWidthPow2(isAffine: Boolean) = (
    if (!isAffine) {
      objTileSize2dPow.x
    } else {
      objAffineDblTileSize2dPow.x
      //ElabVec2[Int](
      //  x=objAffineSliceTileWidthPow,
      //  y=objAffineDblTileSize2dPow.y,
      //)
      //objAffineSliceTileWidthPow
      //myDbgObjAffineTileWidthPow
    }
  )
  def tempObjTileWidth2(isAffine: Boolean) = (
    if (!isAffine) {
      objTileSize2d.x
    } else {
      objAffineDblTileSize2d.x
      //objAffineSliceTileWidth
      //myDbgObjAffineTileWidth
    }
  )

  def tempObjTileHeight(isAffine: Boolean) = (
    if (!isAffine) {
      objTileSize2d.y
    } else {
      objAffineDblTileSize2d.y
    }
  )
  def anyObjTilePxsCoordT(isAffine: Boolean) = ( if (!isAffine) {
      objTilePxsCoordT()
    } else {
      objAffineTilePxsCoordT()
    }
  )
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
  //println(bgSize2dInTiles)
  //println(bgSize2dInPxs)
  //--------
  def numPxsPerBgTile = bgTileSize2d.x * bgTileSize2d.y
  def numPxsPerObjTile = objTileSize2d.x * objTileSize2d.y
  //def numPxsForAllBgTiles = numBgTiles * numPxsPerBgTile
  //def numPxsForAllObjTiles = numObjTiles * numPxsPerObjTile
  //--------
  def numTilesPerBg = bgSize2dInTiles.x * bgSize2dInTiles.y
  def numPxsPerBg = numTilesPerBg * numPxsPerBgTile
  def numPxsForAllBgs = numBgs * numPxsPerBg
  //--------
  //def objAttrsMemIdxWidth = log2Up(numObjs)
  //def objAttrsVecIdxWidth = log2Up(numObjs)
  def objAttrsMemIdxWidth = numObjsPow
  def objCntWidthShift = (
    1 // to account for the extra cycle delay between pixels
    //(
    //  if (objTileWidthRshift == 0) {
    //    1
    //  } else {
    //    objTileWidthRshift
    //  }
    //)
    + objTileWidthRshift
    //+ objSliceTileWidthPow
    + 1 // to account for the rendering grid
  )
  def objAttrsMemIdxTileCntWidth = (
    objCntWidthShift
    + objAttrsMemIdxWidth
    //+ objSliceTileWidthPow
  )
  //--------
  def objAffineAttrsMemIdxWidth = numObjsAffinePow
  def objAffineCntWidthShift = (
    1 // to account for the extra cycle delay between pixels
    //(
    //  if (objAffineTileWidthRshift == 0) {
    //    1
    //  } else {
    //    objAffineTileWidthRshift
    //  }
    //)
    + objAffineTileWidthRshift
    //+ objAffineSliceTileWidthPow // old thing
    + 1 // to account for double size rendering
    + 1 // to account for the rendering grid
  )
  def objAffineAttrsMemIdxTileCntWidth = (
    objAffineCntWidthShift
    + objAffineAttrsMemIdxWidth
    //+ objAffineSliceTileWidthPow
  )
  //def bgMemIdxWidth = log2Up(numBgs)
  //def tileIdxWidth = log2Up(numTiles)
  //def tileMemIdxWidth = log2Up(numTiles)
  //def tileMemIdxWidth = numTilesPow

  //def bgTileMemIdxWidth = numBgTilesPow
  //def colorMathTileMemIdxWidth = numColorMathTilesPow
  //def objTileMemIdxWidth = numObjTilesPow
  //def objAffineTileMemIdxWidth = numObjAffineTilesPow
  def bgTileMemIdxWidth = numBgTilesPow + bgTileSize2dPow.y
  //def bgTileGridMemIdxWidth = (
  //  bgTileMemIdxWidth - 1
  //  //bgTileMemIdxWidth - bgTileSize2dPow.y 
  //  //bgTileMemIdxWidth - bgTileSize2dPow.x 
  //)
  def colorMathTileMemIdxWidth = numColorMathTilesPow + bgTileSize2dPow.y
  def objTileSliceMemIdxWidth = (
    numObjTilesPow
    + objTileSize2dPow.y
    + objTileWidthRshift
    //+ objSliceTileWidthPow
  )
  //def objAffineTileMemIdxWidth = (
  //  numObjAffineTilesPow
  //  + objAffineTileSize2dPow.y
  //  + objAffineTileWidthRshift
  //  //+ objAffineSliceTileWidthPow
  //)
  def objAffineTilePxMemIdxWidth = (
    numObjAffineTilesPow
    + objAffineTileSize2dPow.y
    + objAffineTileSize2dPow.x
  )
  //def objAffineTileSliceMemIdxWidth = (
  //  numObjAffineTilesPow
  //  + objAffineTileSize2dPow.y
  //  //+ objAffineTileSize2dPow.x
  //  + objAffineTileWidthRshift
  //)

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
  def objTilePxsCoordT() = coordT(
    //someSize2d=objTileSize2d
    someSize2d=ElabVec2[Int](
      x=(
        objSliceTileWidth
        //objTileSize2d.x
      ),
      y=objTileSize2d.y,
    )
  )
  def objAffineTilePxsCoordT() = coordT(
    //someSize2d=objAffineTileSize2d
    someSize2d=ElabVec2[Int](
      x=(
        objAffineSliceTileWidth
      ),
      //x=objAffineTileSize2d.x,
      y=objAffineTileSize2d.y,
    ),
  )

  def objSize2dForAttrs = ElabVec2[Int](
    x=objTileSize2d.x + 1,
    y=objTileSize2d.y + 1,
  )
  def objSize2dForAttrsPow = ElabVec2[Int](
    x=log2Up(objSize2dForAttrs.x),
    y=log2Up(objSize2dForAttrs.y),
  )
  //println(s"attrs: $objSize2dForAttrs")
  //println(s"attrs pow: $objSize2dForAttrsPow")
  def objSize2dForAttrsT() = coordT(someSize2d=objSize2dForAttrs)
  //def objAffineSize2dForAttrs = ElabVec2[Int](
  //  x=(objAffineTileSize2d.x + 1) * 2,
  //  y=(objAffineTileSize2d.y + 1) * 2,
  //)
  def objAffineSize2dForAttrsPow = ElabVec2[Int](
    //x=log2Up(objAffineSize2dForAttrs.x),
    //y=log2Up(objAffineSize2dForAttrs.y),
    x=log2Up(objAffineTileSize2d.x + 1) + 1,
    y=log2Up(objAffineTileSize2d.y + 1) + 1,
  )
  def objAffineSize2dForAttrs= ElabVec2[Int](
    //x=log2Up(objAffineSize2dForAttrs.x),
    //y=log2Up(objAffineSize2dForAttrs.y),
    x=1 << objAffineSize2dForAttrsPow.x,
    y=1 << objAffineSize2dForAttrsPow.y,
  )
  def objAffineSize2dForAttrsT() = coordT(
    someSize2d=objAffineSize2dForAttrs
  )
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
  //--------
  def fbBaseAddrWidthPow = (
    log2Up(2) // for double buffering
  )
  def fbRdAddrMultWidthPow = (
    fbBaseAddrWidthPow
    + log2Up(intnlFbSize2d.y)
    + log2Up(intnlFbSize2d.x / bgTileSize2d.x)
  )
  //def fbTileMemAddrWidthPow = (
  //  //bgSize2dInTilesPow.y
  //  //+ bgSize2dInTilesPow.x
  //  log2Up(bgSize2dInPxs.y)
  //  + log2Up(bgSize2dInPxs.x)
  //  //--------
  //  //+ bgTileSize2dPow.y
  //  //--------
  //  // we're indexing into `bgTileMemArr(...)`, which is in units of
  //  // `Gpu2dTileSlice`s, so we need this 
  //  - bgTileSize2dPow.x
  //  //--------
  //  + log2Up(2) // for double buffering
  //  //--------
  //)
}
object DefaultGpu2dParams {
  def apply(
    //--------
    rgbConfig: RgbConfig=RgbConfig(rWidth=6, gWidth=6, bWidth=6),
    intnlFbSize2d: ElabVec2[Int]=ElabVec2[Int](
      x=640,
      y=480,
      //x=(1920 / 3).toInt, // 640
      //y=(1080 / 3).toInt, // 360
      //x=(1920 / 4).toInt, // 480
      //y=(1080 / 4).toInt, // 270
      //x=(1920 / 4).toInt, // 480
      //y=(1080 / 4).toInt, // 270
      //x=(1600 >> log2Up(4)).toInt, // 400
      //y=(900 >> log2Up(4)).toInt, // 225
    ),
    physFbSize2dScale: ElabVec2[Int]=ElabVec2[Int](
      //x=3, // 1920 / 4 = 640
      //y=3, // 1080 / 3 = 360
      //x=4, // 1920 / 4 = 480
      //y=4, // 1080 / 4 = 270
      x=5, // 1920 / 4 = 384
      y=5, // 1080 / 4 = 216
    ),
    //physFbSize2dScalePow: ElabVec2[Int]=ElabVec2[Int](
    //  x=log2Up(4), // 1920 / 4 = 480
    //  y=log2Up(4), // 1080 / 4 = 270
    //  //x=log2Up(4), // 1600 / 4 = 400
    //  //y=log2Up(4), // 900 / 4 = 225
    //),
    //tileSize2d: ElabVec2[Int]=ElabVec2[Int](x=8, y=8),
    bgTileSize2dPow: ElabVec2[Int]=ElabVec2[Int](
      x=log2Up(16), // tile width: 16
      y=log2Up(16), // tile height: 16
      //x=log2Up(8), // tile width: 8
      //y=log2Up(8), // tile height: 8
    ),
    objTileSize2dPow: ElabVec2[Int]=ElabVec2[Int](
      x=log2Up(16), // tile width: 16
      y=log2Up(16), // tile height: 16
      //x=log2Up(8), // tile width: 8
      //y=log2Up(8), // tile height: 8
    ),
    objTileWidthRshift: Int=0,
    objAffineTileSize2dPow: ElabVec2[Int]=ElabVec2[Int](
      x=log2Up(64), // tile width: 64
      y=log2Up(64), // tile height: 64
    ),
    //objAffineTileWidthRshift: Int=log2Up(8),
    objAffineTileWidthRshift: Int=log2Up(16),
    numBgsPow: Int=log2Up(4), // 4 BGs
    numObjsPow: Int=(
      //log2Up(256) // 256 OBJs
      log2Up(16)
    ),
    numObjsAffinePow: Int=log2Up(32), // 32 affine OBJs
    //numBgTilesPow: Option[Int]=None,
    //numObjTilesPow: Option[Int]=None,
    numBgTiles: Option[Int]=None,
    numColorMathTiles: Option[Int]=None,
    numObjTiles: Option[Int]=None,
    numObjAffineTiles: Option[Int]=None,
    //numObjsPow: Int=log2Up(128), // 128 OBJs
    //numObjsPerScanline: Int=64,
    numColsInBgPalPow: Int=log2Up(256), // 256 colors per BG palette
    numColsInObjPalPow: Int=log2Up(256), // 256 colors per OBJ palette
    //--------
    noColorMath: Boolean=false,
    noAffineBgs: Boolean=false,
    noAffineObjs: Boolean=false,
    fancyObjPrio: Boolean=true,
    //--------
    //bgTileMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    //colorMathTileMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    //objTileMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    //objAffineTileMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    //bgPalEntryMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    //colorMathPalEntryMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    //objPalEntryMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    //bgTileMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    //colorMathTileMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    //objTileMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    bgTileMemInit: Option[ArrayBuffer[Gpu2dTileSlice]]=None,
    colorMathTileMemInit: Option[ArrayBuffer[Gpu2dTileSlice]]=None,
    objTileMemInit: Option[ArrayBuffer[Gpu2dTileSlice]]=None,
    objAffineTileMemInit: Option[ArrayBuffer[UInt]]=None,
    //objAffineTileMemInit: Option[ArrayBuffer[BigInt]]=None,
    bgPalEntryMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    colorMathPalEntryMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    objPalEntryMemInitBigInt: Option[ArrayBuffer[BigInt]]=None,
    //--------
    bgTileArrRamStyle: String="block",
    objTileArrRamStyle: String="block",
    objAffineTileArrRamStyle: String="block",
    bgEntryArrRamStyle: String="block",
    //bgAttrsArrRamStyle: String="block",
    objAttrsArrRamStyle: String="auto",
    bgPalEntryArrRamStyle: String="auto",
    objPalEntryArrRamStyle: String="auto",
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
    val tempNumBgTiles = numBgTiles match {
      case Some(myTempNumBgTiles) => myTempNumBgTiles
      case None => (
        //1024
        //2048 // (480 * 270) / (8 * 8) = 2025
        //1 <<
        //log2Up
        (
          (intnlFbSize2d.x * intnlFbSize2d.y)
          / (bgTileSize2d.x.toDouble * bgTileSize2d.y.toDouble)
        ).toInt
      )
    }
    val tempNumColorMathTiles = numColorMathTiles match {
      case Some(myTempNumColorMathTiles) => myTempNumColorMathTiles
      case None => (
        (intnlFbSize2d.x * intnlFbSize2d.y)
        / (bgTileSize2d.x * bgTileSize2d.y)
      )
    }
    val tempNumObjTiles = numObjTiles match {
      case Some(myTempNumObjTiles) => myTempNumObjTiles
      case None => (
        //log2Up(1024)
        1 << numObjsPow
      )
    }
    val tempNumObjAffineTiles = numObjAffineTiles match {
      case Some(myTempNumObjAffineTiles) => myTempNumObjAffineTiles
      case None => (
        //log2Up(1024)
        1 << numObjsAffinePow
      )
    }
    val tempBgSize2dInTilesPow=ElabVec2[Int](
      //x=log2Up(
      //  (intnlFbSize2d.x.toDouble / bgTileSize2d.x.toDouble) + 1
      //),
      //y=log2Up(
      //  (intnlFbSize2d.y.toDouble / bgTileSize2d.y.toDouble).toInt
      //),
      x=log2Up(intnlFbSize2d.x) - log2Up(bgTileSize2d.x),
      y=log2Up(intnlFbSize2d.y) - log2Up(bgTileSize2d.y),
    )
    //println(s"bgSize2dInTilesPow: $tempBgSize2dInTilesPow")
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
      physFbSize2dScale=physFbSize2dScale,
      //physFbSize2dScalePow=physFbSize2dScalePow,
      bgTileSize2dPow=bgTileSize2dPow,
      objTileSize2dPow=objTileSize2dPow,
      objTileWidthRshift=objTileWidthRshift,
      objAffineTileSize2dPow=objAffineTileSize2dPow,
      objAffineTileWidthRshift=objAffineTileWidthRshift,
      //numBgTilesPow=tempNumBgTilesPow,
      //numObjTilesPow=tempNumObjTilesPow,
      numBgTiles=tempNumBgTiles,
      numColorMathTiles=tempNumColorMathTiles,
      numObjTiles=tempNumObjTiles,
      numObjAffineTiles=tempNumObjAffineTiles,
      //bgSize2dInTiles=ElabVec2[Int](x=128, y=128),
      //bgSize2dInTiles=ElabVec2[Int](
      //  x=1 << log2Up(intnlFbSize2d.x / tileSize2d.x),
      //  y=1 << log2Up(intnlFbSize2d.y / tileSize2d.y),
      //),
      //bgSize2dInTilesPow=ElabVec2[Int](
      //  x=log2Up(intnlFbSize2d.x / bgTileSize2d.x),
      //  y=log2Up(intnlFbSize2d.y / bgTileSize2d.y),
      //),
      bgSize2dInTilesPow=tempBgSize2dInTilesPow,
      //numBgs=4,
      //numObjs=256,
      ////numObjsPerScanline=64,
      //numColsInPal=256,
      numBgsPow=numBgsPow,
      numObjsPow=numObjsPow,
      numObjsAffinePow=numObjsAffinePow,
      //numObjsPerScanline=numObjsPerScanline,
      //numColsInPalPow=numColsInPalPow,
      numColsInBgPalPow=numColsInBgPalPow,
      numColsInObjPalPow=numColsInObjPalPow,
      //--------
      noColorMath=noColorMath,
      noAffineBgs=noAffineBgs,
      noAffineObjs=noAffineObjs,
      fancyObjPrio=fancyObjPrio,
      //--------
      bgTileMemInit=bgTileMemInit,
      colorMathTileMemInit=colorMathTileMemInit,
      objTileMemInit=objTileMemInit,
      objAffineTileMemInit=objAffineTileMemInit,
      bgPalEntryMemInitBigInt=bgPalEntryMemInitBigInt,
      colorMathPalEntryMemInitBigInt=colorMathPalEntryMemInitBigInt,
      objPalEntryMemInitBigInt=objPalEntryMemInitBigInt,
      //--------
      bgTileArrRamStyle=bgTileArrRamStyle,
      objTileArrRamStyle=objTileArrRamStyle,
      objAffineTileArrRamStyle=objAffineTileArrRamStyle,
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

object Gpu2dTileSlice {
  def myColIdxFracWidth(
    doPipeMemRmw: Boolean
  ) = (
    if (!doPipeMemRmw) (
      0
    ) else (
      0
      //1
      //4
      //16
    )
  )
  def colIdxWidth(
    params: Gpu2dParams,
    isObj: Boolean,
    doPipeMemRmw: Boolean
  ) = (
    if (!isObj) (
      params.bgPalEntryMemIdxWidth
    ) else (
      params.objPalEntryMemIdxWidth
      + Gpu2dTileSlice.myColIdxFracWidth(doPipeMemRmw=doPipeMemRmw)
    )
  )
  def pxsSliceWidth(
    params: Gpu2dParams,
    isObj: Boolean,
    isAffine: Boolean,
  ) = (
    if (!isObj) {
      params.bgTileSize2d.x
    } else if (!isAffine) {
      //params.objTileSize2d
      params.objSliceTileWidth
    } else {
      //params.objAffineTileSize2d
      params.objAffineSliceTileWidth
    }
  )
}

case class Gpu2dTileSlice(
  params: Gpu2dParams,
  isObj: Boolean,
  isAffine: Boolean,
  doPipeMemRmw: Boolean=false,
) extends Bundle {
  //--------
  //val colIdx = UInt(params.palEntryMemIdxWidth bits)
  def colIdxWidth = (
    Gpu2dTileSlice.colIdxWidth(
      params=params,
      isObj=isObj,
      doPipeMemRmw=doPipeMemRmw,
    )
  )
  def pxsSliceWidth = (
    Gpu2dTileSlice.pxsSliceWidth(
      params=params,
      isObj=isObj,
      isAffine=isAffine,
    )
  )
  def fullPxsSize2d = (
    if (!isObj) {
      params.bgTileSize2d
    } else if (!isAffine) {
      params.objTileSize2d
    } else {
      params.objAffineTileSize2d
    }
  )
  //println(pxsSliceWidth)
  val colIdxVec = Vec.fill(pxsSliceWidth)(
    UInt(colIdxWidth bits)
  )

  def setPx(
    pxCoordX: UInt,
    colIdx: UInt,
  ): Unit = {
    //println(pxCoordX.getWidth)
    colIdxVec(pxCoordX) := colIdx
  }
  def setPx(
    pxCoordX: UInt,
    colIdx: Int
  ): Unit = {
    //assert(pxCoordX >= 0 && pxCoordX < pxsWidth)
    colIdxVec(pxCoordX) := colIdx
  }
  def setPx(
    pxCoordX: Int,
    colIdx: UInt
  ): Unit = {
    //assert(pxCoordX >= 0 && pxCoordX < pxsWidth)
    colIdxVec(pxCoordX) := colIdx
  }
  def setPx(
    pxCoordX: Int,
    colIdx: Int
  ): Unit = {
    //assert(pxCoordX >= 0 && pxCoordX < pxsWidth)
    colIdxVec(pxCoordX) := colIdx
  }
  def getPx(
    pxCoordX: Int,
  ) = {
    colIdxVec(pxCoordX)
  }
  def getPx(
    pxCoordX: UInt,
    //colIdx: UInt
  ) = {
    colIdxVec(pxCoordX)
  }
}

case class Gpu2dTileFull(
  params: Gpu2dParams,
  isObj: Boolean,
  isAffine: Boolean,
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
    } else if (!isAffine) {
      params.objTileSize2d
    } else {
      params.objAffineTileSize2d
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
    //assert(pxCoord.x >= 0 && pxCoord.x < pxsSize2d.x)
    //assert(pxCoord.y >= 0 && pxCoord.y < pxsSize2d.y)
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
  isColorMath: Boolean,
) extends Bundle {
  //--------
  //val rgb = Rgb(params.rgbConfig)
  //val tile = Gpu2dFullTile(
  //  params=params,
  //  isObj=false,
  //  isAffine=false,
  //)
  val tileSlice = Gpu2dTileSlice(
    params=params,
    isObj=false,
    isAffine=false,
  )

  // `Mem` index, so in units of tile slices
  //val idx = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  //val idx = UInt(16 bits)
  val memIdx = UInt(
    (
      if (!isColorMath) {
        params.bgTileMemIdxWidth
      } else {
        params.colorMathTileMemIdxWidth
      }
    ) bits
  )
  //--------
}
case class Gpu2dObjTileStmPayload(
  params: Gpu2dParams,
  isAffine: Boolean,
  dbgPipeMemRmw: Boolean,
) extends Bundle {
  //--------
  //val rgb = Rgb(params.rgbConfig)
  //val tile = Gpu2dFullTile(
  //  params=params,
  //  isObj=true,
  //  isAffine=isAffine,
  //)
  val tileSlice = (!isAffine) generate Gpu2dTileSlice(
    params=params,
    isObj=true,
    isAffine=isAffine,
  )
  val tilePx = (isAffine) generate (
    UInt(params.objPalEntryMemIdxWidth bits)
  )
  val forceWr = (dbgPipeMemRmw) generate (
    Bool()
  )

  // `Mem` index, so in units of tile slices
  //val idx = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  //val idx = UInt(16 bits)
  val memIdx = UInt(
    (
      if (!isAffine) {
        params.objTileSliceMemIdxWidth 
      } else {
        params.objAffineTilePxMemIdxWidth
        //params.objAffineTileSliceMemIdxWidth
      }
    ) bits
  )
  //--------
}

case class Gpu2dBgEntry(
  params: Gpu2dParams,
  isColorMath: Boolean,
) extends Bundle {
  //--------
  // The index, in tiles, of the tile represented by this tilemap entry
  val tileIdx = UInt(
    (
      if (!isColorMath) {
        //params.bgTileMemIdxWidth
        params.numBgTilesPow
      } else {
        //params.colorMathTileMemIdxWidth
        params.numColorMathTilesPow
      }
    ) bits
  )

  // The priority for this tilemap entry
  //val prio = UInt(log2Up(params.numBgs) bits)

  // whether or not to visibly flip x/y during rendering
  val dispFlip = Vec2(dataType=Bool())
  //--------
}
case class Gpu2dBgEntryStmPayload(
  params: Gpu2dParams,
  isColorMath: Boolean,
) extends Bundle {
  //--------
  val bgEntry = Gpu2dBgEntry(
    params=params,
    isColorMath=isColorMath,
  )

  // `Mem` index
  val memIdx = UInt(
    params.bgEntryMemIdxWidth bits
  )
  //--------
}

//case class Gpu2dColorMathAttrs(
//  params: Gpu2dParams
//) extends Bundle {
//}
object Gpu2dColorMathKind extends SpinalEnum(
  defaultEncoding=binarySequential
) {
  //--------
  val
    add,
    sub,  // subtract
    avg   // average
    = newElement();
  //--------
}
case class Gpu2dColorMathInfo(
  params: Gpu2dParams
) extends Bundle {
  //--------
  val doIt = Bool()
  //--------
  val kind = Gpu2dColorMathKind()
  //--------
}
object Gpu2dAffine {
  def wholeWidth(
    params: Gpu2dParams,
    isObj: Boolean,
  ) = (
    if (!isObj) {
      log2Up(
        params.intnlFbSize2d.x
        .max(params.intnlFbSize2d.y)
      )
    } else {
      (
        (
          //params.objTileSize2dPow.x
          //.max(params.objTileSize2dPow.y)
          params.objAffineDblTileSize2dPow.x
          .max(params.objAffineDblTileSize2dPow.y)
        )
      ).max(8)
    }
  )
  def fracWidth = 8
  def fullWidth(
    params: Gpu2dParams,
    isObj: Boolean,
  ) = {
    wholeWidth(
      params=params,
      isObj=isObj,
    ) + fracWidth
  }
  def multSize2dPow(
    params: Gpu2dParams,
    isObj: Boolean,
  ) = (
    //if (!isObj) {
      ElabVec2[Int](
        x=Gpu2dAffine.fullWidth(
          params=params,
          isObj=isObj,
        ) + fracWidth,
        y=Gpu2dAffine.fullWidth(
          params=params,
          isObj=isObj,
        ) + fracWidth,
      )
    //} else {
    //  ElabVec2[Int](
    //    x=params.objPxsCoordSize2dPow.x + fracWidth,
    //    y=params.objPxsCoordSize2dPow.y + fracWidth,
    //  )
    //}
  )
  def vecSize = 2
}
case class Gpu2dAffine(
  params: Gpu2dParams,
  isObj: Boolean,
) extends Bundle {
  //--------
  val doIt = Bool()
  //--------
  def wholeWidth = Gpu2dAffine.wholeWidth(
    params=params,
    isObj=isObj,
  )
  def fracWidth = Gpu2dAffine.fracWidth
  def fullWidth = Gpu2dAffine.fullWidth(
    params=params,
    isObj=isObj,
  )
  def vecSize = Gpu2dAffine.vecSize
  def multSize2dPow = Gpu2dAffine.multSize2dPow(
    params=params,
    isObj=isObj,
  )

  // y is outer index
  // x is inner index
  // I think this is row major
  val mat = Vec.fill(vecSize)(
    Vec.fill(vecSize)(
      SInt(fullWidth bits)
    )
  )
  // (a  b) × (x) = (ax+by)
  // (c  d)   (y)   (cx+dy)
  def matA = mat(0)(0)
  def matB = mat(0)(1)
  def matC = mat(1)(0)
  def matD = mat(1)(1)
  //--------
}

// Framebuffer Attributes
case class Gpu2dFbAttrs(
  params: Gpu2dParams,
  isColorMath: Boolean,
) extends Bundle {
  // Whether to treat this background like a framebuffer
  val doIt = Bool()

  //def temp = (
  //  params.bgSize2dInTiles.y
  //  * params.bgSize2dInTiles.x
  //  * params.bgTileSize2d.y
  //)

  // which address in `bgTileMemArr` that the framebuffer starts from
  val tileMemBaseAddr = UInt(params.fbBaseAddrWidthPow bits)
  //def getTileMemAddr(
  //  //tilePxsCoordY: UInt,
  //) = {
  //  //assert(tilePxsCoordY.getWidth == params.bgTileSize2dPow.y)
  //  //Cat(tileMemBaseAddr, tilePxsCoordY).asUInt
  //  tileMemBaseAddr
  //}
}
// Attributes for a whole background
case class Gpu2dBgAttrs(
  params: Gpu2dParams,
  isColorMath: Boolean,
) extends Bundle {
  //--------
  // Whether or not to display this BG
  //val visib = Bool()

  val colorMathInfo = (
    (
      !isColorMath && !params.noColorMath
    ) generate Gpu2dColorMathInfo(params=params)
  )
  //val affine = Gpu2dAffine(
  //  params=params,
  //  isObj=false
  //)

  // How much to scroll this background
  val scroll = params.bgPxsCoordT()

  val fbAttrs = Gpu2dFbAttrs(
    params=params,
    isColorMath=isColorMath,
  )
  //--------
}
case class Gpu2dBgAttrsStmPayload(
  params: Gpu2dParams,
  isColorMath: Boolean,
) extends Bundle {
  //--------
  val bgAttrs = Gpu2dBgAttrs(
    params=params,
    isColorMath=isColorMath,
  )

  //val memIdx = UInt(params.bgAttrsMemIdxWidth bits)
  //--------
}

case class Gpu2dObjAttrs(
  params: Gpu2dParams,
  isAffine: Boolean,
) extends Bundle {
  //--------
  // Actually this is handled with sufficiently negative `pos.x`
  ///// Whether or not to display this OBJ
  //val visib = Bool()

  // The index, in tiles, of the tile represented by this sprite
  val tileIdx = UInt(
    (
      if (!isAffine) {
        //params.objTileMemIdxWidth
        params.numObjTilesPow
      } else {
        //params.objAffineTileMemIdxWidth
        params.numObjAffineTilesPow
      }
    ) bits
  )
  //val pos = params.coordT(fbSize2d=params.bgSize2dInPxs)

  //// position within the tilemap, in pixels
  // position on screen, in pixels
  val pos = params.objPxsCoordT()

  // the priority for the OBJ
  val prio = UInt(log2Up(params.numBgs) bits)

  val colorMathInfo = (!params.noColorMath) generate (
    Gpu2dColorMathInfo(params=params)
  )
  val affine = (isAffine) generate Gpu2dAffine(
    params=params,
    isObj=true,
  )

  //val size2dMinus1x1 = params.objTilePxsCoordT()
  val size2d = (
    if (!isAffine) {
      params.objSize2dForAttrsT(
        //isAffine=isAffine
      )
    } else {
      params.objAffineSize2dForAttrsT()
    }
  )

  // whether or not to visibly flip x/y during rendering
  val dispFlip = Vec2(dataType=Bool())
  //--------
}

case class Gpu2dObjAttrsStmPayload(
  params: Gpu2dParams,
  isAffine: Boolean,
) extends Bundle {
  //--------
  val objAttrs = Gpu2dObjAttrs(
    params=params,
    isAffine=isAffine,
  )
  // `Mem` index
  val memIdx = UInt(
    (
      if (!isAffine) {
        params.objAttrsMemIdxWidth
      } else {
        params.objAffineAttrsMemIdxWidth
      }
    ) bits
  )
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
  val ctrlEn = Bool()
  val col = Rgb(params.rgbConfig)
  //val physPxPos = params.physPxCoordT()
  //val intnlPxPos = params.bgPxsCoordT()
  //val tilePos = params.bgTilesCoordT()
  val physPosInfo = params.physPosInfoT()

  ////val bgPxsPos = params.bgPxsCoordT()
  ////val bgPxsPosInfo = params.bgPxsPosInfoT()
  //val bgPxsPosSlice = params.bgPxsPosSliceT()
  ////val bgTilesPos = params.bgTilesCoordT()
  //val bgTilesPosSlice = params.bgTilesPosSliceT()
  ////val bgTilesPosInfo = params.bgTilesPosInfoT()
  //--------
}

case class Gpu2dPushInp(
  params: Gpu2dParams=DefaultGpu2dParams(),
  dbgPipeMemRmw: Boolean,
) extends Bundle with IMasterSlave {
  //--------
  val colorMathTilePush = slave Flow(
    Gpu2dBgTileStmPayload(
      params=params,
      isColorMath=true,
    )
  )
  val colorMathEntryPush = slave Flow(
    Gpu2dBgEntryStmPayload(
      params=params,
      isColorMath=true,
    )
  )
  val colorMathAttrsPush = slave Flow(
    Gpu2dBgAttrsStmPayload(
      params=params,
      isColorMath=true,
    )
  )
  val colorMathPalEntryPush = slave Flow(
    Gpu2dBgPalEntryStmPayload(params=params)
  )
  //--------
  val bgTilePush = slave Flow(
    Gpu2dBgTileStmPayload(
      params=params,
      isColorMath=false,
    )
  )
  //val bgEntryPushArr = slave(
  //  Vec.fill(Flow(Gpu2dBgEntryStmPayload(
  //    params=params,
  //    isColorMath=false,
  //  )))
  //)
  val bgEntryPushArr = new ArrayBuffer[Flow[Gpu2dBgEntryStmPayload]]()
  val bgAttrsPushArr = new ArrayBuffer[Flow[Gpu2dBgAttrsStmPayload]]()
  for (idx <- 0 to params.numBgs - 1) {
    bgEntryPushArr += (
      slave Flow(Gpu2dBgEntryStmPayload(
        params=params,
        isColorMath=false,
      ))
        .setName(f"bgEntryPushArr_$idx")
    )
    bgAttrsPushArr += (
      slave Flow(Gpu2dBgAttrsStmPayload(
        params=params,
        isColorMath=false,
      ))
        .setName(f"bgAttrsPushArr_$idx")
    )
  }
  val bgPalEntryPush = slave Flow(Gpu2dBgPalEntryStmPayload(
    params=params
  ))
  val objTilePush = slave Stream(
    Gpu2dObjTileStmPayload(
      params=params,
      isAffine=false,
      dbgPipeMemRmw=dbgPipeMemRmw,
    )
  )
  val objAffineTilePush = slave Flow(
    Gpu2dObjTileStmPayload(
      params=params,
      isAffine=true,
      dbgPipeMemRmw=false,
    )
  )
  val objAttrsPush = slave Flow(
    Gpu2dObjAttrsStmPayload(
      params=params,
      isAffine=false,
    )
  )
  val objAffineAttrsPush = slave Flow(
    Gpu2dObjAttrsStmPayload(
      params=params,
      isAffine=true,
    )
  )
  val objPalEntryPush = slave Flow(Gpu2dObjPalEntryStmPayload(
    params=params
  ))
  //--------
  def asMaster(): Unit = {
    master(
      colorMathTilePush,
      colorMathEntryPush,
      colorMathAttrsPush,
      colorMathPalEntryPush,
      bgTilePush,
      //bgEntryPushArr,
      //bgAttrsPushArr,
      bgPalEntryPush,
      objTilePush,
      objAffineTilePush,
      objAttrsPush,
      objAffineAttrsPush,
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
    this.colorMathTilePush << that.colorMathTilePush
    this.colorMathEntryPush << that.colorMathEntryPush
    this.colorMathAttrsPush << that.colorMathAttrsPush
    this.colorMathPalEntryPush << that.colorMathPalEntryPush
    this.bgTilePush << that.bgTilePush
    for (idx <- 0 to params.numBgs - 1) {
      this.bgEntryPushArr(idx) << that.bgEntryPushArr(idx)
      this.bgAttrsPushArr(idx) << that.bgAttrsPushArr(idx)
    }
    this.bgPalEntryPush << that.bgPalEntryPush
    this.objTilePush << that.objTilePush
    this.objAffineTilePush << that.objAffineTilePush
    this.objAttrsPush << that.objAttrsPush
    this.objAffineAttrsPush << that.objAffineAttrsPush
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
  dbgPipeMemRmw: Boolean,
) extends Bundle {
  //--------
  //val en = in Bool()
  //val blank = in(Vec2(Bool()))
  //val en = in Bool()
  //--------
  //val bgTilePush = slave Stream()
  //val bgTilePush = slave Stream()
  val push = Gpu2dPushInp(
    params=params,
    dbgPipeMemRmw=dbgPipeMemRmw,
  )
  def colorMathTilePush = push.colorMathTilePush
  def colorMathEntryPush = push.colorMathEntryPush 
  def colorMathAttrsPush = push.colorMathAttrsPush 
  def colorMathPalEntryPush = push.colorMathPalEntryPush 
  def bgTilePush = push.bgTilePush
  def bgEntryPushArr = push.bgEntryPushArr
  def bgAttrsPushArr = push.bgAttrsPushArr
  def bgPalEntryPush = push.bgPalEntryPush
  def objTilePush = push.objTilePush
  def objAffineTilePush = push.objAffineTilePush
  def objAttrsPush = push.objAttrsPush
  def objAffineAttrsPush = push.objAffineAttrsPush
  def objPalEntryPush = push.objPalEntryPush
  //--------
  //val ctrlEn = (
  //  //out Bool()
  //  
  //)
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
  inSim: Boolean=false,
  vivadoDebug: Boolean=false,
  dbgPipeMemRmw: Boolean=false,
  //noAffineObjs: Boolean=false,
) extends Component {
  //--------
  def noColorMath = params.noColorMath
  def noAffineBgs = params.noAffineBgs
  def noAffineObjs = params.noAffineObjs
  //--------
  val io = Gpu2dIo(
    params=params,
    dbgPipeMemRmw=dbgPipeMemRmw,
  )
  //--------
  def colorMathTilePush = io.colorMathTilePush
  def colorMathEntryPush = io.colorMathEntryPush 
  def colorMathAttrsPush = io.colorMathAttrsPush 
  def colorMathPalEntryPush = io.colorMathPalEntryPush
  //--------
  def bgTilePush = io.bgTilePush
  def bgEntryPushArr = io.bgEntryPushArr
  def bgAttrsPushArr = io.bgAttrsPushArr
  def objTilePush = io.objTilePush
  def objAffineTilePush = io.objAffineTilePush
  def objAttrsPush = io.objAttrsPush
  def objAffineAttrsPush = io.objAffineAttrsPush
  def bgPalEntryPush = io.bgPalEntryPush
  def objPalEntryPush = io.objPalEntryPush
  //--------
  //val ctrlEn = io.ctrlEn
  //val rPastCtrlEn = RegNext(ctrlEn) init(False)
  //val rCtrlEn = Reg(Bool()) init(False)
  //ctrlEn := rCtrlEn
  //ctrlEn := True

  val pop = io.pop
  //pop.ctrlEn := True
  //val rPopValid = Reg(Bool()) init(True) //init(False)
  //rPopValid := True
  //pop.valid := rPopValid
  //////val rOutp = Reg(cloneOf(pop.payload))

  ////val nextOutp = cloneOf(pop.payload)
  ////val rOutp = RegNext(nextOutp) init(nextOutp.getZero)
  //////rOutp.init(rOutp.getZero)
  ////pop.payload := rOutp
  val outp = cloneOf(pop.payload)
  val rPastOutp = RegNext(outp) //init(outp.getZero)
  rPastOutp.ctrlEn.init(True)
  rPastOutp.col.init(rPastOutp.col.getZero)
  rPastOutp.physPosInfo.init(rPastOutp.physPosInfo.getZero)
  //outp := rPastOutp
  outp.ctrlEn.allowOverride
  outp.ctrlEn := True
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
    val linkArr = PipeHelper.mkLinkArr() //new ArrayBuffer[Link]()
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
    //val colorMathTileMemArr = new ArrayBuffer[
    //  FpgacpuRamSimpleDualPort[Gpu2dTile]
    //]()
    val myBgTileMemInit = Gpu2dTest.bgTileMemInit(
      params=params,
      //gridIdx=jdx,
      //isColorMath=false,
    )

    val bgTileMemArr = new ArrayBuffer[
      //FpgacpuRamSimpleDualPort[Gpu2dTileFull]
      FpgacpuRamSimpleDualPort[Gpu2dTileSlice]
    ]()
    for (
      //--------
      // BEGIN: debug comment this out; later
      jdx <- 0 until params.numBgMemsPerNonPalKind
      //jdx <- 0 until params.numBgTileMems
      // END: debug comment this out; later
      //--------
      //jdx <- 0 until params.bgTileSize2d.x
      //--------
    ) {
      def tempNumBgTileSlices = (
        1 << params.bgTileMemIdxWidth
        //1 << params.bgTileGridMemIdxWidth
      )
      bgTileMemArr += FpgacpuRamSimpleDualPort(
        //wordType=Gpu2dTileFull(
        //  params=params,
        //  isObj=false,
        //  isAffine=false,
        //),
        wordType=Gpu2dTileSlice(
          params=params,
          isObj=false,
          isAffine=false,
        ),
        depth=(
          //params.numBgTiles
          tempNumBgTileSlices
        ),
        //initBigInt=(
        //  Some(
        //    Array.fill(tempNumBgTileSlices)(
        //      //Gpu2dTileSlice(
        //      //  params=params,
        //      //  isObj=false,
        //      //  isAffine=false
        //      //).getZero
        //      BigInt(0)
        //    )
        //  )
        //),
        init={
          params.bgTileMemInit match {
            case Some(bgTileMemInit) => {
              Some(bgTileMemInit/*(jdx)*/)
            }
            case None => {
              Some(
                myBgTileMemInit//(jdx)
                ////Gpu2dTest.doSplitBgTileMemInit(
                ////  params=params,
                ////  //gridIdx=jdx,
                ////  //isColorMath=false,
                ////)(jdx)
                //Array.fill(tempNumBgTileSlices)(
                //  Gpu2dTileSlice(
                //    params=params,
                //    isObj=false,
                //    isAffine=false
                //  ).getZero
                //)
              )
              //None
            }
          }
        },
        arrRamStyle=params.bgTileArrRamStyle,
      )
        .setName(f"bgTileMemArr_$jdx")
      bgTileMemArr(jdx).io.wrEn := bgTilePush.fire
      bgTileMemArr(jdx).io.wrAddr := bgTilePush.payload.memIdx
      bgTileMemArr(jdx).io.wrData := bgTilePush.payload.tileSlice
      //when (
      //  bgTilePush.payload.memIdx(
      //    //jdx
      //    //params.bgTileGridMemIdxWidth
      //    params.bgTileSize2dPow.y
      //    downto params.bgTileSize2dPow.y
      //    //bgTilePush.payload.memIdx.high
      //    //downto bgTilePush.payload.memIdx.high
      //  ) === jdx //=== (if (jdx == 1) {True} else {False})
      //) {
      //  bgTileMemArr(jdx).io.wrEn := bgTilePush.fire
      //  bgTileMemArr(jdx).io.wrAddr := Cat(
      //    bgTilePush.payload.memIdx(
      //      //bgTilePush.payload.memIdx.high downto 1
      //      bgTilePush.payload.memIdx.high 
      //      downto params.bgTileSize2dPow.y + 1
      //    ),
      //    bgTilePush.payload.memIdx(
      //      params.bgTileSize2dPow.y
      //      - 1
      //      //- 2
      //      downto 0
      //    ),
      //  ).asUInt
      //  //bgTileMemArr(jdx).io.wrAddr := (
      //  //  bgTilePush.payload.memIdx(
      //  //    //bgTilePush.payload.memIdx.high downto 1
      //  //    bgTilePush.payload.memIdx.high - 1 downto 0
      //  //  ),
      //  //)
      //  //bgTileMemArr(jdx).io.wrData := bgTilePush.payload.tile
      //  bgTileMemArr(jdx).io.wrData := bgTilePush.payload.tileSlice
      //} otherwise {
      //  bgTileMemArr(jdx).io.wrEn := False
      //  bgTileMemArr(jdx).io.wrAddr := bgTileMemArr(jdx).io.wrAddr.getZero
      //  //bgTileMemArr(jdx).io.wrData := bgTilePush.payload.tile
      //  bgTileMemArr(jdx).io.wrData := bgTileMemArr(jdx).io.wrData.getZero
      //}
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
    val objTileMemArr = new ArrayBuffer[
      //FpgacpuRamSimpleDualPort[Gpu2dTileFull]
      FpgacpuRamSimpleDualPort[Gpu2dTileSlice]
    ]()
    for (idx <- 0 until params.numObjMemsPerKind(isTileMem=true)) {
      def tempNumObjTileSlices = (
        //if (idx == 0) {
          //params.numObjTiles
          1 << params.objTileSliceMemIdxWidth
        //} else {
        //  //params.numObjAffineTiles
        //  1 << params.objAffineTileMemIdxWidth
        //}
      )
      objTileMemArr += FpgacpuRamSimpleDualPort(
        //wordType=Gpu2dTileFull(
        //  params=params,
        //  isObj=true,
        //  isAffine=idx != 0,
        //),
        wordType=Gpu2dTileSlice(
          params=params,
          isObj=true,
          isAffine=idx != 0,
        ),
        depth=tempNumObjTileSlices,
        init={
          //val temp = new ArrayBuffer[]()
          //for (_ <- 0 until tempNumObjTileSlices) {
          //  temp += (0)
          //}
          //Some(temp)
          params.objTileMemInit match {
            case Some(objTileMemInit) => {
              Some(objTileMemInit)
            }
            case None => {
              //val tempArr = new ArrayBuffer[Gpu2dTileSlice]()
              //for (kdx <- 0 until tempNumObjTileSlices) {
              //  tempArr += Gpu2dTileSlice(
              //    params=params,
              //    isObj=true,
              //    isAffine=idx != 0,
              //  ).getZero
              //}
              //Some(tempArr)
              //Some(Array.fill(tempNumObjTileSlices)((0)).toSeq)
              Some(Gpu2dTest.objTileMemInit(params=params))
              //None
            }
          }
        },
        arrRamStyle=params.objTileArrRamStyle,
      )
        .setName(f"objTileMemArr_$idx")
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
      //def tempObjTilePush = (
      //  ////if (idx == 0) {
      //  //  objTilePush
      //  ////} else {
      //  ////  objAffineTilePush
      //  ////}
      //  if (!dbgPipeMemRmw) {
      //    objTilePush
      //  } else { // if (dbgPipeMemRmw)
      //    myPipeMemRmw.io.back
      //  }
      //)
      //if (!dbgPipeMemRmw) {
      //  tempObjTilePush.ready := True
      //} else {
      //  tempObjTilePush.ready := io.pop.ready
      //}

      //--------
      // BEGIN: new test code
      //println(f"dbgPipeMemRmw: $dbgPipeMemRmw")
      val dbgSeen0x20 = (
        objTileMemArr(idx).io.wrEn
        && objTileMemArr(idx).io.wrAddr === 0x20
      )
        .setName("dbgSeen0x20")
        .addAttribute("keep")
      if (
        //!dbgPipeMemRmw
        //|| idx != 0
        true
      ) {
        objTilePush.ready := True
        objTileMemArr(idx).io.wrEn := objTilePush.fire
        objTileMemArr(idx).io.wrAddr := objTilePush.payload.memIdx
        //objTileMemArr(idx).io.wrData := objTilePush.payload.tile
        objTileMemArr(idx).io.wrData := objTilePush.payload.tileSlice
        //when (objTilePush.fire) {
        //  objTileMem.write(
        //    address=objTilePush.payload.memIdx,
        //    data=objTilePush.payload.tile,
        //  )
        //}
      }
      //else {
      //  //def wordType() = Gpu2dObjTileStmPayload(
      //  //  params=params,
      //  //  isAffine=false,
      //  //)
      //  def wordType() = Gpu2dTileSlice(
      //    params=params,
      //    isObj=true,
      //    isAffine=idx != 0,
      //    doPipeMemRmw=true,
      //  )
      //  def wordCount = (
      //    1 << params.objTileSliceMemIdxWidth
      //    //4
      //  )
      //  def modStageCnt = (
      //    //1
      //    2
      //  )
      //  def modType() = SamplePipeMemRmwModType(
      //    wordType=wordType(),
      //    wordCount=wordCount,
      //    modStageCnt=modStageCnt,
      //  )
      //  val myPipeMemRmw = (
      //    PipeMemRmw[
      //      Gpu2dTileSlice,
      //      SamplePipeMemRmwModType[Gpu2dTileSlice],
      //      PipeMemRmwDualRdTypeDisabled[Gpu2dTileSlice]
      //    ](
      //      wordType=wordType(),
      //      wordCount=wordCount,
      //      modType=modType(),
      //      modStageCnt=modStageCnt,
      //      initBigInt=Some(Array.fill(wordCount)(BigInt(0)).toSeq),
      //      forFmax=(
      //        true
      //        //false
      //      ),
      //      //forFmax=false,
      //    )
      //      .setName(f"myPipeMemRmw_objTileMemArr_$idx")
      //  )
      //  def front = myPipeMemRmw.io.front
      //  def modFront = myPipeMemRmw.io.modFront
      //  def modBack = myPipeMemRmw.io.modBack
      //  def back = myPipeMemRmw.io.back
      //  def myFracWidth = (
      //    Gpu2dTileSlice.myColIdxFracWidth(
      //      doPipeMemRmw=true,
      //    )
      //  )
      //  objTilePush.translateInto(
      //    into=front,
      //  )(
      //    dataAssignment=(
      //      frontPayload,
      //      objTilePushPayload,
      //    ) => {
      //      def myExt = frontPayload.myExt
      //      myExt.helperForceWr := objTilePushPayload.forceWr
      //      //when (myExt.helperForceWr) {
      //        myExt.rdMemWord := (
      //          //myExt.modMemWord
      //          myExt.rdMemWord.getZero
      //        )
      //      //} otherwise {
      //      //  myExt.rdMemWord := myExt.rdMemWord.getZero
      //      //}
      //      //for (
      //      //  jdx <- 0 until myExt.modMemWord.colIdxVec.size
      //      //) {
      //      //  myExt.modMemWord.colIdxVec(jdx) := (
      //      //    objTilePushPayload.tileSlice.colIdxVec(jdx).resized
      //      //    //<< myFracWidth
      //      //  )
      //      //}
      //      myExt.modMemWord.colIdxVec := (
      //        objTilePushPayload.tileSlice.colIdxVec
      //      )
      //      //myExt.memAddr := (
      //      //  Cat(U"2'b00", objTilePushPayload.memIdx >> 2).asUInt
      //      //)
      //      myExt.memAddr := objTilePushPayload.memIdx
      //      //myExt.hazardId := -1
      //      myExt.doInitHazardId()
      //    },
      //  )

      //  val modFrontStm = Stream(modType())
      //    .setName("dbgPipeMemRmw_modFrontStm")
      //    .addAttribute("keep")
      //  val modBackStm = Stream(modType())
      //    .setName("dbgPipeMemRmw_modBackStm")
      //    .addAttribute("keep")
      //  //val didInitMem = Vec.fill(wordCount)(
      //  //  Reg(Bool()) init(False)
      //  //)
      //  //  .setName("dbgPipeMemRmw_didInitMem")
      //  //  //.addAttribute("keep")
      //  //def didInitMemElemWidth = 16 + 1
      //  def cntMemWordCount = (
      //    // only up to `min(params.numObjTiles, N)` tiles
      //    min(params.numObjTiles, 4)
      //    * params.objTileSize2d.y * (1 << params.objTileWidthRshift)
      //  )
      //  def cntMemElemWidth = 4 //1//16 //+ 1
      //  val cntMem = Mem(
      //    wordType=(
      //      UInt(cntMemElemWidth bits)
      //    ),
      //    wordCount=cntMemWordCount,
      //  )
      //    .setName("dbgPipeMemRmw_cntMem")
      //    .initBigInt(Array.fill(cntMemWordCount)(BigInt(0)).toSeq)
      //  val rdCntMemElem = cntMem.readSync(
      //    address=modFront.myExt.memAddr(
      //      log2Up(cntMemWordCount) - 1 downto 0
      //    ),
      //    enable=modFront.fire,
      //  )
      //    .setName("dbgPipeMemRmw_rdCntMemElem")
      //    .addAttribute("keep")

      //  val tempCntMemElem = UInt(rdCntMemElem.getWidth bits)
      //    .setName("dbgPipeMemRmw_tempCntMemElem")
      //    .addAttribute("keep")

      //  tempCntMemElem := (
      //    RegNext(tempCntMemElem) init(tempCntMemElem.getZero)
      //  )
      //  val tempCntCond = (
      //    if (log2Up(cntMemWordCount) < modFront.myExt.memAddr.getWidth) {
      //      modFront.myExt.memAddr <= cntMemWordCount
      //    } else {
      //      True
      //    }
      //  )
      //    .setName("dbgPipeMemRmw_tempCntCond")
      //    .addAttribute("keep")
      //  when (tempCntCond) {
      //    //val tempRdCntPlusOne = UInt((cntMemElemWidth + 1) bits)
      //    val tempRdCntPlusOne = (Cat(B"1'b0", rdCntMemElem).asUInt + 1)
      //      .setName("dbgPipeMemRmw_tempRdCntPlusOne")
      //      .addAttribute("keep")
      //    when (
      //      //(rRdCntMemElem + 1).msb
      //      tempRdCntPlusOne.msb
      //    ) {
      //      tempCntMemElem := 0
      //    } otherwise {
      //      //tempCntMemElem := rdCntMemElem + 1
      //      tempCntMemElem := tempRdCntPlusOne(tempCntMemElem.bitsRange)
      //    }
      //  }
      //  cntMem.write(
      //    address=modBack.myExt.memAddr(
      //      log2Up(cntMemWordCount) - 1 downto 0
      //    ),
      //    data=tempCntMemElem,
      //    enable=modBack.fire && tempCntCond,
      //  )

      //  val didInitMem = Mem(
      //    wordType=(
      //      Bool()
      //      //UInt(3 bits)
      //    ),
      //    wordCount=wordCount,
      //  )
      //    .setName("dbgPipeMemRmw_didInitMem")
      //    .initBigInt(Array.fill(wordCount)(BigInt(0)).toSeq)
      //  val rDidInitMemRdElem = (
      //    //Reg(UInt(3 bits)) init(0x0)
      //    Reg(Bool()) init(False)
      //  )
      //  //rDidInitMemRdElem := didInitMem
      //  rDidInitMemRdElem := didInitMem.readSync(
      //    address=modFront.myExt.memAddr,
      //    enable=(
      //      modFront.fire
      //    )
      //  )
      //  didInitMem.write(
      //    address=modBack.myExt.memAddr,
      //    data=True,
      //    //data=(
      //    //  Mux[UInt](
      //    //    !rDidInitMemRdElem.msb,
      //    //    rDidInitMemRdElem + 1,
      //    //    rDidInitMemRdElem
      //    //  )
      //    //),
      //    enable=(
      //      modBack.fire
      //    ),
      //  )
      //  //when (!rDidInitMemRdElem) {
      //  //}

      //  modFrontStm <-/< modFront
      //  modFrontStm.translateInto(
      //    into=modBackStm
      //  )(
      //    dataAssignment=(
      //      modBackPayload,
      //      modFrontPayload
      //    ) => {
      //      modBackPayload := modFrontPayload
      //      def modFrontExt = modFrontPayload.myExt
      //      def modBackExt = modBackPayload.myExt
      //      modBackExt.allowOverride
      //      //modBackExt := modFrontExt
      //      modBackExt.modMemWord.allowOverride
      //      //modBackExt := modFrontExt
      //      //--------
      //      //when (
      //      //  back.fire
      //      //  //&& 
      //      //  //modBackStm.fire
      //      //  //!didInitMem(modFrontExt.memAddr)
      //      //  && 
      //      //  (
      //      //    RegNextWhen(
      //      //      !didInitMem(back.myExt.memAddr),
      //      //      back.fire
      //      //    ) init(didInitMem(back.myExt.memAddr).getZero)
      //      //  )

      //      //) {
      //      //  //didInitMem(modFrontExt.memAddr) := True
      //      //  didInitMem(back.myExt.memAddr) := True
      //      //}
      //      val tempMemWord = Mux[Gpu2dTileSlice](
      //        //modFrontExt.helperForceWr,
      //        //!didInitMem.readAsync(modFrontExt.memAddr),
      //        //!didInitMem(modFrontExt.memAddr),
      //        //!didInitMem(back.myExt.memAddr),
      //        !rDidInitMemRdElem,
      //        //rDidInitMemRdElem.msb,
      //        modFrontExt.modMemWord,
      //        modFrontExt.rdMemWord,
      //      )
      //        .setName("dbgPipeMemRmw_tempMemWord")
      //        .addAttribute("keep")
      //      //val tempMemWord = modFrontExt.modMemWord
      //      //val tempMemWord = modFrontExt.modMemWord
      //      //--------
      //      //when (
      //      //  !didInitMem.readAsync(modFrontExt.memAddr)
      //      //) {
      //      //  didInitMem.write(
      //      //    address=modFrontExt.memAddr,
      //      //    data=True,
      //      //  )
      //      //}
      //      when (
      //        //!rDidModVec(modBackExt.memAddr)
      //        True
      //        //rDidModMem.readAsync
      //      ) {
      //        //rDidModVec(modBackExt.memAddr) := True

      //        //frontExt.modMemWord := modFrontPayload.myExt.modMemWord
      //        //modBackExt.modMemWord := (
      //        //  //modFrontExt.modMemWord
      //        //  //modFrontExt.rdMemWord
      //        //  tempMemWord
      //        //)
      //        //--------
      //        when (rdCntMemElem === 1) {
      //          for (
      //            pxCoordX <- 0
      //            //until modFrontExt.modMemWord.pxsSliceWidth
      //            until tempMemWord.pxsSliceWidth
      //          ) {
      //            //val myPx = modFrontExt.modMemWord.getPx(pxCoordX=pxCoordX)
      //            val myPx = tempMemWord.getPx(pxCoordX=pxCoordX)
      //            //when (
      //            //  myPx =/= 0x0
      //            //) {
      //              modBackExt.modMemWord.colIdxVec(pxCoordX) := (
      //                //(
      //                //  (myPx + 1)
      //                //  & ((8 << myFracWidth) - 1)
      //                //) + (2 << myFracWidth)
      //                //myPx + (1 << myFracWidth)
      //                Mux[UInt](
      //                  myPx =/= 0,
      //                  Mux[UInt](
      //                    (myPx + 1 === 0x0),
      //                    myPx + 2,
      //                    myPx + 1
      //                  ),
      //                  myPx.getZero
      //                )
      //              )
      //              //modBackExt.modMemWord := tempMemWord
      //              //modBackExt.modMemWord.setPx(
      //              //  pxCoordX=pxCoordX,
      //              //  colIdx=(
      //              //    //myPx + 1
      //              //    //3
      //              //    //(myPx << 1).resized
      //              //  )
      //              //)
      //              //switch (myPx) {
      //              //  for (
      //              //    pxIdx <- 0 until (1 << myPx.getWidth)
      //              //  ) {
      //              //    is (pxIdx) {
      //              //      modBackExt.modMemWord.setPx(
      //              //        pxCoordX=pxCoordX,
      //              //        colIdx=(
      //              //          //(pxIdx % 2) + 1
      //              //          {
      //              //            //val tempPxIdx = (pxIdx + 1) % 4
      //              //            ////if (
      //              //            ////  tempPxIdx
      //              //            ////  >= 5 //(1 << myPx.getWidth)
      //              //            ////) {
      //              //            ////  pxIdx //1
      //              //            ////} else {
      //              //            ////  tempPxIdx
      //              //            ////}
      //              //            //if (tempPxIdx == 1) {
      //              //            //  tempPxIdx + 1
      //              //            //} else {
      //              //            //  tempPxIdx
      //              //            //}
      //              //            //(pxIdx % 4) + 1
      //              //            //pxIdx % 4
      //              //            //val (pxIdx + 1) % (1 << myPx.getWidth)
      //              //            //if (
      //              //            //  pxIdx == 0
      //              //            //)
      //              //            //(pxIdx % 5) + 2
      //              //            //--------
      //              //            (
      //              //              (
      //              //                pxIdx
      //              //                & ((8 << myFracWidth) - 1)
      //              //              ) + (2 << myFracWidth)
      //              //            )
      //              //            //((pxIdx + 1) % (6) + 1
      //              //            //--------
      //              //            //(pxIdx + 1) % 5
      //              //            //--------
      //              //            //pxIdx
      //              //          }
      //              //          //3
      //              //          //(pxIdx << 1) % 2
      //              //          //(pxIdx % 2) + 1
      //              //        )
      //              //      )
      //              //    }
      //              //  }
      //              //}
      //            //}
      //          }
      //        }
      //      }
      //      //--------
      //    }
      //  )
      //  //modBackStm << modFrontStm
      //  modBack <-/< modBackStm
      //  //modBack << modBackStm
      //  val nextBackReadyCnt = UInt(4 bits)
      //    .setName(f"dbgPipeMemRmw_nextBackReadyCnt_$idx")
      //  val rBackReadyCnt = (
      //    //RegNextWhen(nextBackReadyCnt, front.fire)
      //    //init(nextBackReadyCnt.getZero)
      //    RegNext(nextBackReadyCnt) init(nextBackReadyCnt.getZero)
      //  )
      //    .setName(f"dbgPipeMemRmw_rBackReadyCnt_$idx")
      //  nextBackReadyCnt := rBackReadyCnt + 1
      //  val myBackReady = !rBackReadyCnt(1)
      //  //val myBackReady = True
      //  //--------
      //  // BEGIN: debug
      //  //back.ready := True
      //  // END: debug
      //  //--------
      //  back.ready := (
      //    //rBackReadyCnt(1 downto 0) =/= U"2'b10" 
      //    myBackReady
      //  )
      //  when (!myBackReady) {
      //    // set `back.ready` with the pattern ^^_^^_^^_
      //    // where "^" is high and "_" is low
      //    // I could have used "1" and "0" but it's easier to visualize
      //    // `back.ready` this way
      //    nextBackReadyCnt := 0x0
      //  }
      //  //val rDbgSeen = Reg(Bool()) init(False)
      //  //when (
      //  //  objTileMemArr(idx).io.wrEn
      //  //  && objTileMemArr(idx).io.wrAddr === 0x20
      //  //) {
      //  //  rDbgSeen := True
      //  //}

      //  //objTilePush.ready := True
      //  objTileMemArr(idx).io.wrEn := (
      //    back.fire
      //    //back.valid
      //    //&& back.myExt.hazardId.msb
      //  )
      //  objTileMemArr(idx).io.wrAddr := back.payload.myExt.memAddr
      //  //objTileMemArr(idx).io.wrData := objTilePush.payload.tile

      //  //--------
      //  //objTileMemArr(idx).io.wrData := (
      //  //  back.payload.myExt.modMemWord
      //  //)
      //  def myWrData = objTileMemArr(idx).io.wrData

      //  for (
      //    //jdx <- 0 until Gpu2dTileSlice.myColIdxFracWidth(true)
      //    //jdx <- 0 until Gpu2dTileSlice.colIdxWidth(
      //    //  params=params,
      //    //  isObj=true,
      //    //  doPipeMemRmw=true,
      //    //)
      //    jdx <- 0 until myWrData.colIdxVec.size
      //  ) {
      //    val myColIdx = back.payload.myExt.modMemWord.colIdxVec(jdx)
      //    myWrData.colIdxVec(jdx) := (
      //      (
      //        //myColIdx(myColIdx.high downto myFracWidth)
      //        myColIdx(myColIdx.high - myFracWidth downto 0)
      //      )
      //    )
      //  }
      //  //--------

      //  //when (objTilePush.fire) {
      //  //  objTileMem.write(
      //  //    address=objTilePush.payload.memIdx,
      //  //    data=objTilePush.payload.tile,
      //  //  )
      //  //}
      //  //tempObjTilePush.io.back.ready := io.pop.ready
      //}
      // END: new test code
      //--------
    }
    val objAffineTileMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[UInt]
    ]()
    for (idx <- 0 until params.objAffineSliceTileWidth) {
      def tempNumObjTileSlices = (
        //if (idx == 0) {
        //  //params.numObjTiles
        //  1 << params.objTileSliceMemIdxWidth
        //} else {
        //  //params.numObjAffineTiles
          1 << params.objAffineTilePxMemIdxWidth
        //}
      )
      objAffineTileMemArr += FpgacpuRamSimpleDualPort(
        wordType=UInt(params.objPalEntryMemIdxWidth bits),
        depth=tempNumObjTileSlices,
        initBigInt={
          Some(Array.fill(tempNumObjTileSlices)(BigInt(0)).toSeq)
        },
        //init={
        //  //val temp = new ArrayBuffer[]()
        //  //for (_ <- 0 until tempNumObjTileSlices) {
        //  //  temp += (0)
        //  //}
        //  //Some(temp)
        //  params.objAffineTileMemInit match {
        //    case Some(objAffineTileMemInit) => {
        //      Some(objAffineTileMemInit)
        //    }
        //    case None => {
        //      //Some(Array.fill(tempNumObjTileSlices)(
        //      //  (0)
        //      //).toSeq)
        //      //Gpu2dTest.objTileMemInit(params=params)
        //      val tempArr = new ArrayBuffer[UInt]()
        //      for (
        //        jdx <- 0
        //        until (
        //          params.objPalEntryMemIdxWidth
        //        )
        //      ) {
        //        for (
        //          kdx <- 0 until params.objPalEntryMemIdxWidth
        //        ) {
        //        }
        //      }
        //      Some(tempArr.toSeq)
        //    }
        //  }
        //  //Some(
        //  //  Array.fill(tempNumObjTileSlices)(
        //  //    U(s"${params.objPalEntryMemIdxWidth}'d0")
        //  //  ).toSeq
        //  //)
        //},
        arrRamStyle=params.objAffineTileArrRamStyle,
      )
        .setName(f"objAffineTileMemArr_$idx")
      objAffineTileMemArr(idx).io.wrEn := (
        objAffineTilePush.fire
      )
      objAffineTileMemArr(idx).io.wrAddr := {
        def tempX = (
          //Cat(
            objAffineTilePush.payload.memIdx,
          //  U{
          //    def tempWidthPow = (
          //      //params.objAffineTileWidthRshift
          //      //params.objAffineTileSize2dPow.x
          //      params.objAffineSliceTileWidthPow
          //    )
          //    f"$tempWidthPow'd$idx"
          //  },
          //).asUInt
        )
        //def tempXWidth = tempX.getWidth
        //def tempWrAddrWidth = objAffineTileMemArr(idx).io.wrAddr.getWidth
        //println(f"$tempXWidth $tempWrAddrWidth")
        tempX
      }
      objAffineTileMemArr(idx).io.wrData := (
        objAffineTilePush.payload.tilePx
        //objAffineTilePush.payload.tileSlice.getPx(idx)
      )
    }
    //--------
    //val bgEntryMem = Mem(
    //  wordType=Gpu2dBgEntry(params=params),
    //  wordCount=params.numColsInPal,
    //)
    //  .addAttribute("ram_style", params.palEntryArrRamStyle)
    //--------
    val colorMathTileMemArr = new ArrayBuffer[
      //FpgacpuRamSimpleDualPort[Gpu2dTileFull]
      FpgacpuRamSimpleDualPort[Gpu2dTileSlice]
    ]()
    if (!noColorMath) {
      def tempNumColorMathTileSlices = (
        1 << params.colorMathTileMemIdxWidth
      )
      for (
        jdx <- 0 until params.numBgMemsPerNonPalKind
      ) {
        colorMathTileMemArr += FpgacpuRamSimpleDualPort(
          //dataType=CombinePipePayload(),
            //wordType=Gpu2dTileFull(
            //  params=params,
            //  isObj=false,
            //  isAffine=false,
            //),
            wordType=Gpu2dTileSlice(
              params=params,
              isObj=false,
              isAffine=false,
            ),
            depth=(
              //params.numColorMathTiles
              tempNumColorMathTileSlices
            ),
            init={
              //val temp = new ArrayBuffer[]()
              //for (
              //  //idx <- 0 until params.numColorMathTiles
              //  idx <- 0 until tempNumColorMathTileSlices
              //) {
              //  temp += (0)
              //}
              //Some(temp)
              //Some(Gpu2dTest.bgTileMemInit(params=params))
              params.colorMathTileMemInit match {
                case Some(colorMathTileMemInit) => {
                  Some(colorMathTileMemInit/*(jdx)*/)
                }
                case None => {
                  //Some(Array.fill(tempNumColorMathTileSlices)(
                  //  BigInt(0)
                  //))
                  //Some(Gpu2dTest.bgTileMemInit(params=params))
                  //Some(
                  //  //Gpu2dTest.doSplitBgTileMemInit(
                  //  //  params=params,
                  //  //  gridIdx=jdx,
                  //  //  //isColorMath=true,
                  //  //)
                  //  myBgTileMemInit//(jdx)
                  //)
                  //None
                  val temp = new ArrayBuffer[Gpu2dTileSlice]()
                  for (
                    //idx <- 0 until params.numColorMathTiles
                    idx <- 0 until tempNumColorMathTileSlices
                  ) {
                    //temp += BigInt(0)
                    temp += Gpu2dTileSlice(
                      params=params,
                      isObj=false,
                      isAffine=false,
                    ).getZero
                  }
                  Some(temp)
                }
              }
            },
            arrRamStyle=params.bgTileArrRamStyle,
          )
            .setName(f"colorMathTileMemArr_$jdx")
        //(
        //  setWordFunc=(
        //    unionIdx: UInt,
        //    o: CombinePipePayload,
        //    i: CombinePipePayload,
        //    memWord: Gpu2dTile,
        //  ) => {
        //  },
        //)
        def colorMathTileMem = colorMathTileMemArr(jdx)
        colorMathTileMem.io.wrEn := colorMathTilePush.fire
        colorMathTileMem.io.wrAddr := colorMathTilePush.memIdx
        //colorMathTileMem.io.wrData := colorMathTilePush.tile
        colorMathTileMem.io.wrData := colorMathTilePush.tileSlice
        //colorMathTileMem.io.wrPulse.valid := colorMathTilePush.fire
        //colorMathTileMem.io.wrPulse.addr := colorMathTilePush.memIdx
        //colorMathTileMem.io.wrPulse.data := colorMathTilePush.tile
      }
    }

    val colorMathPalEntryMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dPalEntry]
    ]()
    if (!noColorMath) {
      for (jdx <- 0 until params.bgTileSize2d.x) {
        colorMathPalEntryMemArr += FpgacpuRamSimpleDualPort(
          //dataType=CombinePipePayload(),
          wordType=Gpu2dPalEntry(params=params),
          depth=params.numColsInBgPal,
          //initBigInt={
          //  val temp = new ArrayBuffer[BigInt]()
          //  for (idx <- 0 until params.numColsInBgPal) {
          //    temp += BigInt(0)
          //  }
          //  Some(temp)
          //},
          initBigInt={
            params.colorMathPalEntryMemInitBigInt match {
              case Some(colorMathPalEntryMemInitBigInt) => {
                Some(colorMathPalEntryMemInitBigInt)
              }
              case None => {
                //Some(Array.fill(params.numColsInBgPal)(
                //  (0)
                //).toSeq)
                Some(Gpu2dTest.bgPalMemInitBigInt(params=params))
                //Some(Gpu2dTest.somePalMemInit(
                //  params=params,
                //  someBigIntArr=(
                //    Gpu2dTest.bgPalMemInitBigInt(params=params)
                //  ),
                //))
                //match {
                //  case Some(mySeq) => {
                //    Some(mySeq)
                //  }
                //  case None => {
                //    None
                //  }
                //}
                //None
              }
            }
          },
          arrRamStyle=params.bgPalEntryArrRamStyle,
        )
          .setName(f"colorMathPalEntryMemArr_$jdx")
        //(
        //  setWordFunc=(
        //    unionIdx: UInt,
        //    o: CombinePipePayload,
        //    i: CombinePipePayload,
        //    memWord: Gpu2dPalEntry,
        //  ) => {
        //  },
        //)
        colorMathPalEntryMemArr(jdx).io.wrEn := (
          colorMathPalEntryPush.fire
        )
        colorMathPalEntryMemArr(jdx).io.wrAddr := (
          colorMathPalEntryPush.memIdx
        )
        colorMathPalEntryMemArr(jdx).io.wrData := (
          colorMathPalEntryPush.bgPalEntry
        )
        //colorMathPalEntryMem.io.wrPulse.valid := (
        //  colorMathPalEntryPush.fire
        //)
        //colorMathPalEntryMem.io.wrPulse.addr := (
        //  colorMathPalEntryPush.memIdx
        //)
        //colorMathPalEntryMem.io.wrPulse.data := (
        //  colorMathPalEntryPush.bgPalEntry
        //)
      }
    }
    val colorMathEntryMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dBgEntry]
      //WrPulseRdPipeSimpleDualPortMem[
      //  CombinePipePayload,
      //  Gpu2dBgEntry,
      //]
    ]()
    if (!noColorMath) {
      for (
        //jdx <- 0 until params.numColorMathMemsPerNonPalKind
        jdx <- 0 until params.numBgMemsPerNonPalKind
      ) {
        colorMathEntryMemArr += FpgacpuRamSimpleDualPort(
          //dataType=CombinePipePayload(),
          wordType=Gpu2dBgEntry(
            params=params,
            isColorMath=true,
          ),
          depth=(
            params.numTilesPerBg >> 1
          ),
          initBigInt={
            val temp = new ArrayBuffer[BigInt]()
            for (_ <- 0 until (params.numTilesPerBg >> 1)) {
              temp += BigInt(0)
            }
            Some(temp)
            //Some(Array.fill(params.numTilesPerBg)(BigInt(0)).toSeq)
          },
          arrRamStyle=params.bgEntryArrRamStyle,
        )
          .setName(f"colorMathEntryMemArr_$jdx")
        //(
        //  setWordFunc=(
        //    unionIdx: UInt,
        //    o: CombinePipePayload,
        //    i: CombinePipePayload,
        //    memWord: Gpu2dBgEntry,
        //  ) => {
        //  },
        //)
          .setName(f"colorMathEntryMemArr_$jdx")
        colorMathEntryMemArr(jdx).io.wrEn := (
          colorMathEntryPush.fire
        )
        colorMathEntryMemArr(jdx).io.wrAddr := (
          colorMathEntryPush.memIdx(
            colorMathEntryPush.memIdx.high downto 1
          )
        )
        colorMathEntryMemArr(jdx).io.wrData := (
          colorMathEntryPush.bgEntry
        )
        //colorMathEntryMem.io.wrPulse.valid := (
        //  colorMathEntryPush.fire
        //)
        //colorMathEntryMem.io.wrPulse.addr := (
        //  colorMathEntryPush.memIdx
        //)
        //colorMathEntryMem.io.wrPulse.data := (
        //  colorMathEntryPush.bgEntry
        //)
      }
    }
    val colorMathAttrs = (!noColorMath) generate Reg(Gpu2dBgAttrs(
      params=params,
      isColorMath=true,
    ))
    if (!noColorMath) {
      colorMathAttrs.init(colorMathAttrs.getZero)
      when (colorMathAttrsPush.fire) {
        colorMathAttrs := colorMathAttrsPush.bgAttrs
      }
    }
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
      for (
        //--------
        // BEGIN: debug comment this out; later
        jdx <- 0 until params.numBgMemsPerNonPalKind
        //jdx <- 0 until params.numBgEntryMems
        // END: debug comment this out; later
        //--------
        //jdx <- 0 until params.bgTileSize2d.x
        //--------
      ) {
        def bgEntryMemArr = bgEntryMemA2d(idx)
        bgEntryMemArr += FpgacpuRamSimpleDualPort(
          wordType=Gpu2dBgEntry(
            params=params,
            isColorMath=false,
          ),
          depth=(params.numTilesPerBg >> 1),
          initBigInt={
            //val temp = new ArrayBuffer[BigInt]()
            //for (_ <- 0 until (params.numTilesPerBg >> 1)) {
            //  temp += BigInt(0)
            //}
            //Some(temp)
            Some(Array.fill(params.numTilesPerBg >> 1)(BigInt(0)).toSeq)
          },
          arrRamStyle=params.bgEntryArrRamStyle,
        )
          .setName(f"bgEntryMemA2d_$idx" + f"_$jdx")
        when (bgEntryPushArr(idx).payload.memIdx(0 downto 0) === jdx) {
          bgEntryMemArr(jdx).io.wrEn := bgEntryPushArr(idx).fire
          bgEntryMemArr(jdx).io.wrAddr := (
            bgEntryPushArr(idx).payload.memIdx(
              bgEntryPushArr(idx).payload.memIdx.high downto 1
            )
          )
          bgEntryMemArr(jdx).io.wrData := (
            bgEntryPushArr(idx).payload.bgEntry
          )
        } otherwise { // when (`jdx` not active)
          bgEntryMemArr(jdx).io.wrEn := False
          bgEntryMemArr(jdx).io.wrAddr := 0
          bgEntryMemArr(jdx).io.wrData := (
            bgEntryMemArr(jdx).io.wrData.getZero
          )
        }
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
      bgAttrsArr += Reg(Gpu2dBgAttrs(
        params=params,
        isColorMath=false,
      ))
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
    val objAttrsMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dObjAttrs]
    ]()
    for (idx <- 0 until params.numObjMemsPerKind(isTileMem=false)) {
      def tempNumObjs = (
        if (idx == 0) {
          params.numObjs
        } else {
          params.numObjsAffine
        }
      )
      objAttrsMemArr += FpgacpuRamSimpleDualPort(
        wordType=Gpu2dObjAttrs(
          params=params,
          isAffine=idx != 0,
        ),
        depth=tempNumObjs,
        //initBigInt={
        //  //val temp = new ArrayBuffer[BigInt]()
        //  //for (_ <- 0 until tempNumObjs) {
        //  //  temp += BigInt(0)
        //  //}
        //  //Some(temp)
        //  Some(Array.fill(tempNumObjs)(BigInt(0)).toSeq)
        //},
        arrRamStyle=params.objAttrsArrRamStyle,
      )
        .setName(f"objAttrsMemArr_$idx")
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
      def tempObjAttrsPush = (
        if (idx == 0) {
          objAttrsPush
        } else {
          objAffineAttrsPush
        }
      )
      objAttrsMemArr(idx).io.wrEn := tempObjAttrsPush.fire
      objAttrsMemArr(idx).io.wrAddr := tempObjAttrsPush.payload.memIdx
      objAttrsMemArr(idx).io.wrData := tempObjAttrsPush.payload.objAttrs
      //when (objAttrsPush.fire) {
      //  objAttrsMem.write(
      //    address=objAttrsPush.payload.memIdx,
      //    data=objAttrsPush.payload.objAttrs,
      //  )
      //}
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
    val bgPalEntryMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dPalEntry]
    ]()
    for (jdx <- 0 until params.bgTileSize2d.x) {
      bgPalEntryMemArr += FpgacpuRamSimpleDualPort(
        wordType=Gpu2dPalEntry(params=params),
        depth=params.numColsInBgPal,
        initBigInt={
          //val temp = new ArrayBuffer[]()
          //for (idx <- 0 until params.numColsInBgPal) {
          //  temp += (0)
          //}
          //Some(temp)
          params.bgPalEntryMemInitBigInt match {
            case Some(bgPalEntryMemInitBigInt) => {
              Some(bgPalEntryMemInitBigInt)
            }
            case None => {
              //Some(Array.fill(params.numColsInBgPal)((0)).toSeq)
              //Some(Gpu2dTest.somePalMemInit(
              //  params=params,
              //  someBigIntArr=Gpu2dTest.bgPalMemInitBigInt(
              //    params=params,
              //  )
              //))
              Some(Gpu2dTest.bgPalMemInitBigInt(params=params).toSeq)
            }
          }
        },
        arrRamStyle=params.bgPalEntryArrRamStyle,
      )
        .setName(f"bgPalEntryMemArr_$jdx")
      bgPalEntryMemArr(jdx).io.wrEn := bgPalEntryPush.fire
      bgPalEntryMemArr(jdx).io.wrAddr := bgPalEntryPush.payload.memIdx
      bgPalEntryMemArr(jdx).io.wrData := bgPalEntryPush.payload.bgPalEntry
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

    val objPalEntryMemA2d = new ArrayBuffer[ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dPalEntry]
    ]]()
    for (idx <- 0 until params.numObjMemsPerKind(isTileMem=false)) {
      objPalEntryMemA2d += new ArrayBuffer[
        FpgacpuRamSimpleDualPort[Gpu2dPalEntry]
      ]()
      def objPalEntryMemArr = objPalEntryMemA2d(idx) 
      for (
        x <- 0 until (
          //if (idx == 0) {
          //  params.objTileSize2d.x
          //} else {
          //  params.objAffineDblTileSize2d.x
          //}
          //params.tempObjTileWidth1(idx != 0)
          params.tempObjTileWidth(idx != 0)
        )
      ) {
        objPalEntryMemArr += FpgacpuRamSimpleDualPort(
          wordType=Gpu2dPalEntry(params=params),
          depth=params.numColsInObjPal,
          initBigInt={
            //val temp = new ArrayBuffer[]()
            //for (idx <- 0 until params.numColsInObjPal) {
            //  temp += (0)
            //}
            //Some(temp)
            params.objPalEntryMemInitBigInt match {
              case Some(objPalEntryMemInitBigInt) => {
                Some(objPalEntryMemInitBigInt)
              }
              case None => {
                //Some(Array.fill(params.numColsInObjPal)(BigInt(0)).toSeq)
                //Some(Array.fill(params.numColsInBgPal)((0)).toSeq)
                //Some(Gpu2dTest.somePalMemInit(
                //  params=params,
                //  someBigIntArr=Gpu2dTest.objPalMemInitBigInt(
                //    params=params,
                //  )
                //))
                Some(Gpu2dTest.objPalMemInitBigInt(params=params).toSeq)
              }
            }
          },
          arrRamStyle=params.objPalEntryArrRamStyle,
        )
          .setName(f"objPalEntryMemArr_$idx" + f"_$x")
        objPalEntryMemArr(x).io.wrEn := objPalEntryPush.fire
        objPalEntryMemArr(x).io.wrAddr := objPalEntryPush.payload.memIdx
        objPalEntryMemArr(x).io.wrData := (
          objPalEntryPush.payload.objPalEntry
        )
      }
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
    val nextWrObjAffineChangingRow = (!noAffineObjs) generate (
      Bool()
    )
    val rWrObjAffineChangingRow = (!noAffineObjs) generate (
      RegNext(
        nextWrObjAffineChangingRow
      ) init(False)
    )
    val nextCombineChangingRow = Bool()
    val rCombineChangingRow = RegNext(nextCombineChangingRow) init(False)
    //val nextRdChangingRow = Bool()
    //val rRdChangingRow = RegNext(nextRdChangingRow) init(False)
    //val nextRdChangingRowRe = Bool()

    val rPastIntnlChangingRow = RegNext(rIntnlChangingRow) init(False)
    val intnlChangingRowRe = KeepAttribute(Bool())
    intnlChangingRowRe := rIntnlChangingRow && !rPastIntnlChangingRow
    //intnlChangingRowRe := nextIntnlChangingRow && !rIntnlChangingRow
    //intnlChangingRowRe := (
    //  RegNext(rIntnlChangingRow && !rPastIntnlChangingRow)
    //  init(intnlChangingRowRe.getZero)
    //)
    //intnlChangingRowRe := RegNextWhen(
    //  rIntnlChangingRow && !rPastIntnlChangingRow,
    //  nWrBgArr(0).isFiring,
    //) init(False)
    //intnlChangingRowRe := (
    //  //RegNext(rIntnlChangingRow && !rPastIntnlChangingRow)
    //  //init(False)
    //  nextIntnlChangingRow && !rIntnlChangingRow
    //)
    //intnlChangingRowRe := r
    //val nextIntnlChangingRowRe = KeepAttribute(Bool())
    //nextIntnlChangingRowRe := nextIntnlChangingRow && !rIntnlChangingRow
    nextIntnlChangingRow := (
      // all of the pipelines must have finished the current scanline
      //outp.physPosInfo.nextPos.x === 0
      //outp.physPosInfo.changingRow
      rWrBgChangingRow
      && rWrObjChangingRow
      && (
        if (!noAffineObjs) {
          rWrObjAffineChangingRow
        } else {
          True
        }
      )
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

    outp.physPosInfo := (
      //rdPhysCalcPos.io.info
      rdPhysCalcPos.io.info
    )
    //outp.bgPxsPosSlice := outp.physPosInfo.posSlice(
    //  someSize2d=params.fbSize2dInPxs,
    //  someScalePow=params.physToIntnlScalePow,
    //  //thatSomeSize2d=params.bg
    //)
    //outp.bgTilesPosSlice := outp.physPosInfo.posSlice(
    //  someSize2d=params.fbSize2dInBgTiles,
    //  someScalePow=params.physToBgTilesScalePow,
    //)

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

    //case class ColorMathLineMemEntry() extends Bundle {
    //  val col = Gpu2dRgba(params=params)
    //}
    def ColorMathSubLineMemEntry() = (!noColorMath) generate (
      Gpu2dRgba(params=params)
    )
    case class BgSubLineMemEntry() extends Bundle {
      val col = Gpu2dRgba(params=params)
      val prio = UInt(params.numBgsPow bits)
      //val addr = UInt(params.bgSubLineMemArrSizePow bits)
      val addr = UInt(log2Up(params.oneLineMemSize) bits)
      def getSubLineMemTempArrIdx() = (
        params.getBgSubLineMemArrIdx(addr=addr)
      )
      val colorMathInfo = (!noColorMath) generate (
        Gpu2dColorMathInfo(params=params)
      )
      val colorMathCol = (!noColorMath) generate (
        Gpu2dRgba(params=params)
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
      val addr = (inSim) generate UInt(log2Up(params.oneLineMemSize) bits)
      def getSubLineMemTempArrIdx() = (
        params.getObjSubLineMemArrIdx(addr=addr)
      )
      val colorMathInfo = (!noColorMath) generate (
        Gpu2dColorMathInfo(params=params)
      )
      val objIdx = UInt(
        (
          params.objAttrsMemIdxWidth
          .max(params.objAffineAttrsMemIdxWidth)
        )
        bits
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
    //val wrBgSubLineMemArr = new ArrayBuffer[Mem[Vec[BgSubLineMemEntry]]]()
    val combineBgSubLineMemA2d = (
      new ArrayBuffer[
        ArrayBuffer[
          WrPulseRdPipeSimpleDualPortMem[
            //Bits,
            CombinePipePayload,
            Vec[BgSubLineMemEntry]
          ]
          //PipeMemRmw[
          //  Vec[BgSubLineMemEntry],
          //  CombinePipePayload,
          //  CombinePipePayload,
          //]
        ]
      ]()
    )
    //def mkCombineBgSubLineMemArrPipeExt() = {
    //}
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
      //FpgacpuRamSimpleDualPort[Vec[ObjSubLineMemEntry]]
      PipeMemRmw[
        Vec[ObjSubLineMemEntry],
        WrObjPipeSlmRmwHazardCmp,
        WrObjPipePayload,
        PipeMemRmwDualRdTypeDisabled[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ]
      ]
    ]()
    val combineObjSubLineMemArr = new ArrayBuffer[
      WrPulseRdPipeSimpleDualPortMem[
        //Bits,
        CombinePipePayload,
        Vec[ObjSubLineMemEntry]
      ]
    ]()
    val wrObjAffineSubLineMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[Vec[ObjSubLineMemEntry]]
    ]()
    val combineObjAffineSubLineMemArr = new ArrayBuffer[
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
    def lineMemArrIdxWidth = (
      log2Up(params.numLineMemsPerBgObjRenderer)
    )
    def fullLineMemArrIdxWidth = (
      log2Up(params.numLineMemsPerBgObjRenderer)
      //+ log2Up(params.physFbSize2dScale.y)
    )
    ////def rdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    ////def combineWrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd1")
    ////def combineRdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd2")
    ////def wrFullLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd3")
    //def combineRdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    def combineFullLineMemArrIdxInit = (
      U({
        //val temp = 0 << log2Up(params.physFbSize2dScale.y)
        val temp = 0
        s"$fullLineMemArrIdxWidth'd$temp"
      })
    )
    //def combineFullLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd3")
    //def wrFullLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd1")
    //def sendIntoFifoLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    def wrFullLineMemArrIdxInit = (
      //U(s"$lineMemArrIdxWidth'd$1")
      U({
        //val temp = 1 << log2Up(params.physFbSize2dScale.y)
        val temp = 1
        s"$fullLineMemArrIdxWidth'd$temp"
      })
    )
    //def wrFullLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")

    //val rSendIntoFifoLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
    //  init(sendIntoFifoLineMemArrIdxInit)
    //)

    //val rCombineFullLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
    //  //init(0x3)
    //  init(combineFullLineMemArrIdxInit)
    //)
    def mkFullLineMemIdx(
      someInit: UInt
    ) = (
      KeepAttribute(
        Reg(
          UInt(fullLineMemArrIdxWidth bits),
          init=someInit
        )
        //init(0x3)
        //init(wrFullLineMemArrIdxInit)
        //init(someInit)
      )
    )
    //val rLineMemArrIdxIncCnt = (
    //  params.physFbSize2dScale.y > 1
    //) generate (
    //  KeepAttribute(
    //    Vec.fill(32)(
    //      Reg(
    //        SInt(log2Up(params.physFbSize2dScale.y) + 1 bits),
    //        //init=0x0,
    //      ) init(params.physFbSize2dScale.y - 1)
    //    )
    //  )
    //)
    //--------
    val rWrObjWriterFullLineMemArrIdx = Vec.fill(
      params.numLineMemsPerBgObjRenderer
    )(
      mkFullLineMemIdx(wrFullLineMemArrIdxInit)
    )
    val wrObjWriterLineMemArrIdx = Vec.fill(
      params.numLineMemsPerBgObjRenderer
    )(
      //mkLineMemIdx(wrFullLineMemArrIdxInit)
      UInt(lineMemArrIdxWidth bits)
    )
    for (zdx <- 0 until params.numLineMemsPerBgObjRenderer) {
      wrObjWriterLineMemArrIdx(zdx) := (
        rWrObjWriterFullLineMemArrIdx(zdx)
        //(
        //  rWrObjWriterFullLineMemArrIdx(zdx).high
        //  downto log2Up(params.physFbSize2dScale.y)
        //  //downto 0
        //)
      )
    }
    //--------
    val rWrFullObjPipeLineMemArrIdx = Vec.fill(8)(
      mkFullLineMemIdx(wrFullLineMemArrIdxInit)
    )
    val wrObjPipeLineMemArrIdx = Vec.fill(8)(
      //mkLineMemIdx(wrFullLineMemArrIdxInit)
      UInt(lineMemArrIdxWidth bits)
    )
    for (zdx <- 0 until wrObjPipeLineMemArrIdx.size) {
      wrObjPipeLineMemArrIdx(zdx) := (
        rWrFullObjPipeLineMemArrIdx(zdx)
        //(
        // rWrFullObjPipeLineMemArrIdx(zdx).high
        //  downto log2Up(params.physFbSize2dScale.y)
        //  //downto 0
        //)
      )
    }
    //--------
    val rWrFullBgPipeLineMemArrIdx = Vec.fill(
      params.numLineMemsPerBgObjRenderer
    )(
      mkFullLineMemIdx(wrFullLineMemArrIdxInit)
    )
    val wrBgPipeLineMemArrIdx = Vec.fill(
      params.numLineMemsPerBgObjRenderer
    )(
      UInt(lineMemArrIdxWidth bits)
    )
    for (zdx <- 0 until params.numLineMemsPerBgObjRenderer) {
      wrBgPipeLineMemArrIdx(zdx) := (
        rWrFullBgPipeLineMemArrIdx(zdx)
        //(
        //  rWrFullBgPipeLineMemArrIdx(zdx).high
        //  downto log2Up(params.physFbSize2dScale.y)
        //  //downto 0
        //)
      )
    }
    //--------
    //val rWrLineMemArrIdx = mkLineMemIdx(
    //  wrFullLineMemArrIdxInit
    //)
    //val rCombineFullLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits))
    //  init(combineFullLineMemArrIdxInit)
    //)
    val combineLineMemArrIdx = (
      UInt(lineMemArrIdxWidth bits)
    )
    val rCombineFullLineMemArrIdx = mkFullLineMemIdx(
      combineFullLineMemArrIdxInit
    )
    combineLineMemArrIdx := (
      rCombineFullLineMemArrIdx
      //(
      //  rCombineFullLineMemArrIdx.high
      //  downto log2Up(params.physFbSize2dScale.y)
      //  //downto 0
      //)
    )
    //def wrLineNumInit = 0x3
    //def wrLineNumInit = 0x2
    def wrLineNumWidth = log2Up(params.intnlFbSize2d.y)
    def wrFullLineNumWidth = (
      wrLineNumWidth
      //log2Up(params.physFbSize2d.y)
      //wrLineNumWidth
    )
    //def wrLineNumInit = 0x1
    //def wrLineNumInit = U(f"$wrLineNumWidth'd1")
    //def wrLineNumInit = U(f"$wrLineNumWidth'd0")
    //def wrLineNumInit = U(f"$wrLineNumWidth'd2")
    //def wrBgLineNumInit = U(f"$wrLineNumWidth'd2")
    def wrBgLineNumInit = U(f"$wrLineNumWidth'd0")
    def wrBgFullLineNumInit = (
      U({
        //val temp = 0 << log2Up(params.physFbSize2dScale.y)
        //val temp = 0
        //val temp = params.intnlFbSize2d.y - 1
        val temp = (
          (
            params.intnlFbSize2d.y /*- 1*/
            - params.physFbSize2dScale.y + 1
          ) % params.intnlFbSize2d.y
        )
        s"$wrFullLineNumWidth'd$temp"
      })
    )
    //def wrBgLineNumInit = U(f"$wrLineNumWidth'd1")
    //def wrObjLineNumInit = U(f"$wrLineNumWidth'd1")
    //def wrObjLineNumInit = U(f"$wrLineNumWidth'd2")
    def wrObjLineNumInit = U(f"$wrLineNumWidth'd0")
    def wrObjFullLineNumInit = (
      U({
        //val temp = 0 << log2Up(params.physFbSize2dScale.y)
        //val temp = 0
        //val temp = params.intnlFbSize2d.y - 1
        val temp = (
          (
            params.intnlFbSize2d.y /*- 1*/ 
            - params.physFbSize2dScale.y + 1
          ) % params.intnlFbSize2d.y
        )
        s"$wrFullLineNumWidth'd$temp"
      })
    )
    //val rGlobWrBgLineNum = KeepAttribute(
    //  Reg(UInt(wrLineNumWidth bits))
    //  //init(0x2)
    //  //init(0x3)
    //  //init(wrLineNumInit)
    //  init(wrBgLineNumInit)
    //)
    //val rGlobWrObjLineNum = KeepAttribute(
    //  Reg(UInt(wrLineNumWidth bits))
    //  //init(0x2)
    //  //init(0x3)
    //  //init(wrLineNumInit)
    //  init(wrObjLineNumInit)
    //)
    //val rGlobWrBgFullLineNum = KeepAttribute(
    //  Reg(UInt(wrFullLineNumWidth bits))
    //  init(wrBgFullLineNumInit)
    //)
    //val rGlobWrObjFullLineNum = KeepAttribute(
    //  Reg(UInt(wrFullLineNumWidth bits))
    //  init(wrObjFullLineNumInit)
    //)
    //def rGlobWrBgLineNum = (
    //  rGlobWrBgFullLineNum(
    //    rGlobWrBgFullLineNum.high
    //    downto log2Up(params.physFbSize2dScale.y)
    //  )
    //)
    //def rGlobWrObjLineNum = (
    //  rGlobWrObjFullLineNum(
    //    rGlobWrObjFullLineNum.high
    //    downto log2Up(params.physFbSize2dScale.y)
    //  )
    //)

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
    //--------
    case class GlobFullLineNumPipe(
      //wordWidth: Int,
      someFullLineNumInit: UInt,
      //rSomeFullLineNum: UInt,
    ) extends Area {
      val fracCntInit = (
        //params.physFbSize2dScale.y - 1
        params.physFbSize2dScale.y - 2
        //0x0
      )
      val fracCntRollover = (
        //params.physFbSize2dScale.y - 1
        params.physFbSize2dScale.y - 2
      )
      val rLineNumFracCnt = Reg(
        SInt(log2Up(params.physFbSize2dScale.y) + 3 bits)
        //UInt(wordWidth + 2 bits)
      )
        .init(
          fracCntInit
          //0x0
          //-1
        )
      val rLineNumFracCntPipe1 = Reg(
        cloneOf(rLineNumFracCnt)
      )
        .init(
          fracCntInit
          //0x0
          //-1
        )
      val rLineNumFracCntPipe2 = Reg(
        cloneOf(rLineNumFracCnt)
      )
        .init(
          fracCntInit
          //0x0
          //-1
        )
      //when (
      //  rLineNumFracCnt =/= params.physFbSize2dScale.y - 1
      //) {
      //  rLineNumFracCntPipe2 := rLineNumFracCnt + 1
      //} otherwise {
      //  rLineNumFracCntPipe2 := 0x0
      //}
      when ((rLineNumFracCnt /*- 1*/).msb) {
        rLineNumFracCntPipe2 := fracCntRollover
      } otherwise {
        rLineNumFracCntPipe2 := rLineNumFracCnt - 1
      }
      rLineNumFracCntPipe1 := rLineNumFracCntPipe2

      val rLineNumFracCntMsbPipe2 = Reg(Bool()) init(False)
      val rLineNumFracCntMsbPipe1 = Reg(Bool()) init(False)
      rLineNumFracCntMsbPipe2 := (rLineNumFracCnt /*- 1*/).msb
      rLineNumFracCntMsbPipe1 := rLineNumFracCntMsbPipe2
      //rLineNumFracCnt := 
      //--------

      val rLineNum = KeepAttribute(
        Reg(UInt(wrFullLineNumWidth bits))
        init(
          //wrBgFullLineNumInit
          someFullLineNumInit
        )
      )
      //val rGlobWrObjFullLineNum = KeepAttribute(
      //  Reg(UInt(wrFullLineNumWidth bits))
      //  init(wrObjFullLineNumInit)
      //)
      val rLineNumCheckPipe2 = Reg(Bool()) init(False)
      val rLineNumPlus1Pipe2 = Reg(
        UInt(rLineNum.getWidth + 1 bits)
      )
        .init(0x0)
      //val rLineNumPlus1Pipe2 := Reg(cloneOf(rLineNum))
      //  .init(0x0)
      val rLineNumPipe1 = Reg(
        UInt(rLineNum.getWidth + 1 bits)
      )
        .init(0x0)
      rLineNumCheckPipe2 := (
        rLineNum.resized
        =/= (
          params.intnlFbSize2d.y - 1
          //params.physFbSize2d.y - 1
        ) && (
          //(rLineNumFracCnt - 1).msb
          //rLineNumFracCntPipe1.msb
          True
        )
      )
      def tempFullLineNumPipeWidth = (
        rLineNumPlus1Pipe2.getWidth
      )
      //rLineNumPlus1Pipe2 := rLineNum.resized + 1
      rLineNumPlus1Pipe2 := (
        rLineNum.resized
        + U(f"$tempFullLineNumPipeWidth'd1")
      )
      //when (
      //  //if (params.physFbSize2dScale.y > 1) {
      //  //  rLineMemArrIdxIncCnt(0).msb
      //  //} else {
      //    True
      //  //}
      //) {
        when (rLineNumFracCntMsbPipe2) {
          when (
            rLineNumCheckPipe2
          ) {
            rLineNumPipe1 := rLineNumPlus1Pipe2
          } otherwise {
            rLineNumPipe1 := 0
          }
        }
      //}
      //--------
      //--------
      when (
        intnlChangingRowRe
      ) {
        //rGlobWrBgFullLineNumFracCnt := (
        //  rGlobWrBgFullLineNumFracCntPipe1(
        //    rGlobWrBgFullLineNumFracCnt.bitsRange
        //  )
        //)
        rLineNumFracCnt := rLineNumFracCntPipe1(rLineNumFracCnt.bitsRange)
        //when (
        //  //rLineNumFracCnt === params.physFbSize2dScale.y - 1
        //  //(rLineNumFracCntPipe1 - 1).msb
        //  //rLineNumFracCnt.msb
        //  rLineNumFracCntPipe1.msb
        //  //rLineNumFracCntMsbPipe1
        //) {
          rLineNum := rLineNumPipe1(rLineNum.bitsRange)
        //}
        //rGlobWrBgFullLineNum := (
        //  rGlobWrBgFullLineNumPipe1(rGlobWrBgFullLineNum.bitsRange)
        //)
        //rGlobWrObjFullLineNum := (
        //  rGlobWrObjFullLineNumPipe1(rGlobWrObjFullLineNum.bitsRange)
        //)
      }
      //--------
    }
    val wrBgGlobPipe = GlobFullLineNumPipe(
      someFullLineNumInit=wrBgFullLineNumInit,
    )
    val wrObjGlobPipe = GlobFullLineNumPipe(
      someFullLineNumInit=wrObjFullLineNumInit,
    )
    //val rGlobWrObjFullLineNumCheckPipe2 = Reg(Bool()) init(False)
    //val rGlobWrObjFullLineNumPlus1Pipe2 = Reg(
    //  UInt(rGlobWrObjFullLineNum.getWidth + 1 bits)
    //)
    //  .init(0x0)
    ////val rGlobWrObjFullLineNumPlus1Pipe2 := Reg(cloneOf(rGlobWrObjFullLineNum))
    ////  .init(0x0)
    //val rGlobWrObjFullLineNumFracCnt = Reg(
    //  UInt(log2Up(params.physFbSize2dScale.y) + 2 bits)
    //)
    //  .init(params.physFbSize2dScale.y - 1)
    //val rGlobWrObjFullLineNumPipe1 = Reg(
    //  UInt(rGlobWrObjFullLineNum.getWidth + 1 bits)
    //)
    //  .init(0x0)
    //rGlobWrObjFullLineNumCheckPipe2 := (
    //  rGlobWrObjFullLineNum.resized
    //  =/= (
    //    params.intnlFbSize2d.y - 1
    //    //params.physFbSize2d.y - 1
    //  ) && (
    //    rGlobWrObjFullLineNumFracCnt.msb
    //  )
    //)
    //def tempWrObjFullLineNumPipeWidth = (
    //  rGlobWrObjFullLineNumPlus1Pipe2.getWidth
    //)
    ////rGlobWrObjFullLineNumPlus1Pipe2 := rGlobWrObjFullLineNum.resized + 1
    //rGlobWrObjFullLineNumPlus1Pipe2 := (
    //  rGlobWrObjFullLineNum.resized
    //  + U(f"$tempWrObjFullLineNumPipeWidth'd1")
    //)
    ////when (
    ////  //if (params.physFbSize2dScale.y > 1) {
    ////  //  rLineMemArrIdxIncCnt(0).msb
    ////  //} else {
    ////    True
    ////  //}
    ////) {
    //  when (rGlobWrObjFullLineNumCheckPipe2) {
    //    rGlobWrObjFullLineNumPipe1 := rGlobWrObjFullLineNumPlus1Pipe2
    //  } otherwise {
    //    rGlobWrObjFullLineNumPipe1 := 0
    //  }
    ////}

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
      //if (params.physFbSize2dScale.y > 1) {
      //  for (zdx <- 0 until rLineMemArrIdxIncCnt.size) {
      //    def tempCnt = rLineMemArrIdxIncCnt(zdx)
      //    when ((tempCnt - 1).msb) {
      //      tempCnt := params.physFbSize2dScale.y - 1
      //    } otherwise {
      //      tempCnt := tempCnt - 1
      //    }
      //  }
      //}

      for (zdx <- 0 until rWrObjWriterFullLineMemArrIdx.size) {
        //when (
        //  //if (
        //  //  params.physFbSize2dScale.y > 1
        //  //) {
        //  //  rLineMemArrIdxIncCnt(
        //  //    4 + zdx
        //  //  ).msb
        //  //} else {
        //    True
        //  //}
        //) {
          rWrObjWriterFullLineMemArrIdx(zdx) := (
            rWrObjWriterFullLineMemArrIdx(zdx) + 1
          )
        //}
      }
      for (zdx <- 0 until rWrFullObjPipeLineMemArrIdx.size) {
        //when (
        //  //if (
        //  //  params.physFbSize2dScale.y > 1
        //  //) {
        //  //  rLineMemArrIdxIncCnt(
        //  //    4 + wrObjWriterLineMemArrIdx.size + zdx
        //  //  ).msb
        //  //} else {
        //    True
        //  //}
        //) {
          //wrObjPipeLineMemArrIdx(zdx) := wrObjPipeLineMemArrIdx(zdx) + 1
          rWrFullObjPipeLineMemArrIdx(zdx) := (
            rWrFullObjPipeLineMemArrIdx(zdx) + 1
          )
        //}
      }
      for (zdx <- 0 until rWrFullBgPipeLineMemArrIdx.size) {
        //when (
        //  //if (
        //  //  params.physFbSize2dScale.y > 1
        //  //) {
        //  //  rLineMemArrIdxIncCnt(
        //  //    4
        //  //    + wrObjWriterLineMemArrIdx.size 
        //  //    + wrObjPipeLineMemArrIdx.size
        //  //    + zdx
        //  //  ).msb
        //  //} else {
        //    True
        //  //}
        //) {
          //rWrBgPipeLineMemArrIdx(zdx) := rWrBgPipeLineMemArrIdx(zdx) + 1
          rWrFullBgPipeLineMemArrIdx(zdx) := (
            rWrFullBgPipeLineMemArrIdx(zdx) + 1
          )
        //}
      }
      //when (
      //  //if (
      //  //  params.physFbSize2dScale.y > 1
      //  //) {
      //  //  rLineMemArrIdxIncCnt(2).msb
      //  //} else {
      //    True
      //  //}
      //) {
        //rWrLineMemArrIdx := rWrLineMemArrIdx + 1
      //}
      //when (
      //  //if (
      //  //  params.physFbSize2dScale.y > 1
      //  //) {
      //  //  rLineMemArrIdxIncCnt(3).msb
      //  //} else {
      //    True
      //  //}
      //) {
        rCombineFullLineMemArrIdx := rCombineFullLineMemArrIdx + 1
      //}
      //rGlobWrBgFullLineNumFracCnt := (
      //  rGlobWrBgFullLineNumFracCntPipe1(
      //    rGlobWrBgFullLineNumFracCnt.bitsRange
      //  )
      //)
      //rGlobWrBgFullLineNum := (
      //  rGlobWrBgFullLineNumPipe1(rGlobWrBgFullLineNum.bitsRange)
      //)
      //rGlobWrObjFullLineNum := (
      //  rGlobWrObjFullLineNumPipe1(rGlobWrObjFullLineNum.bitsRange)
      //)
      //rCombineFullLineMemArrIdx := rCombineFullLineMemArrIdx + 1
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

    def combineBgSetWordFunc(
      unionIdx: UInt,
      outpPayload: CombinePipePayload,
      inpPayload: CombinePipePayload,
      bgTileRow: Vec[BgSubLineMemEntry],
    ): Unit = {
      //outpPayload := outpPayload.getZero
      outpPayload.allowOverride
      outpPayload.stage0.changingRow := (
        inpPayload.stage0.changingRow
      )
      //outpPayload.stage0.bakCnt
      //outpPayload.stage0.fracCnt := inpPayload.stage0.fracCnt
      outpPayload.stage0.fullCnt := inpPayload.stage0.fullCnt
      outpPayload.stage0.fullBakCnt := inpPayload.stage0.fullBakCnt
      //outpPayload.stage0.fullBakCntMinus1 := (
      //  inpPayload.stage0.fullBakCntMinus1
      //)
      outpPayload.stage2.rdBg := bgTileRow
    }
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

      //wrBgSubLineMemArr += Mem(
      //  //wordType=Rgb(params.rgbConfig),
      //  //wordType=BgSubLineMemEntry(),
      //  //wordCount=params.oneLineMemSize,
      //  wordType=Vec.fill(params.bgTileSize2d.x)(BgSubLineMemEntry()),
      //  wordCount=params.bgSubLineMemArrSize,
      //)
      //  .initBigInt(
      //    //Array.fill(params.oneLineMemSize)(BigInt(0)).toSeq
      //    //Array.fill(params.bgSubLineMemArrSize)(BigInt(0)).toSeq
      //    bgSubLineMemInitBigInt.toSeq
      //  )
      //  .addAttribute("ram_style", params.lineArrRamStyle)
      //  //.addAttribute("ram_mode", "tdp") // true dual-port
      //  .setName(f"bgLineMemArr_$idx")
      //--------
      //combineBgSubLineMemArr += PipeMemRmw[
      //  Vec[BgSubLineMemEntry],
      //  CombinePipePayload,
      //  CombinePipePayload,
      //](
      //  wordType=Vec.fill(params.bgTileSize2d.x)(BgSubLineMemEntry()),
      //  wordCount=params.bgSubLineMemArrSize,
      //  modType=CombinePipePayload(),
      //  modStageCnt=1,
      //  memArrIdx=idx,
      //  dualRdType=CombinePipePayload(),
      //  optDualRd=true,
      //  initBigInt=Some(bgSubLineMemInitBigInt),
      //  //forFmax=true,
      //  optEnableModDuplicate=false, // to prevent writing stalls
      //)
      //  .setName(f"combineBgSubLineMemArr_$idx")
      //def combineBgLast = combineBgSubLineMemArr.last
      //combineBgLast.io.modBack <-/< combineBgLast.io.modFront
      //combineBgLast.io.back.ready := True
      //combineBgLast
      //val combineBgModFrontStm = (
      //  cloneOf(combineBgSubLineMemArr.last.io.modFront)
      //)
      //val combineBgModBackStm = (
      //  cloneOf(combineBgSubLineMemArr.last.io.modBack)
      //)
      combineBgSubLineMemA2d += new ArrayBuffer[
        WrPulseRdPipeSimpleDualPortMem[
          CombinePipePayload,
          Vec[BgSubLineMemEntry],
        ]
      ]()
      for (jdx <- 0 until params.combineBgSubLineMemArrSize) {
        combineBgSubLineMemA2d.last += WrPulseRdPipeSimpleDualPortMem(
          dataType=CombinePipePayload(),
          wordType=Vec.fill(
            //params.bgTileSize2d.x
            params.combineBgSubLineMemVecElemSize
          )(BgSubLineMemEntry()),
          wordCount=params.bgSubLineMemArrSize,
          pipeName=s"combineBgSubLineMemArr_${idx}",
          initBigInt=Some(bgSubLineMemInitBigInt),
          vivadoDebug=vivadoDebug,
        )(
          setWordFunc=(
            //io: WrPulseRdPipeSimpleDualPortMemIo[
            //  Bits,
            //  Vec[BgSubLineMemEntry],
            //],
            unionIdx: UInt,
            outpPayload: CombinePipePayload,
            inpPayload: CombinePipePayload,
            bgTileRowSlice: Vec[BgSubLineMemEntry],
          ) => {
            outpPayload := outpPayload.getZero
            outpPayload.allowOverride
            outpPayload.stage0.changingRow := (
              inpPayload.stage0.changingRow
            )
            //outpPayload.stage0.fracCnt := (
            //  inpPayload.stage0.fracCnt
            //)
            outpPayload.stage0.fullCnt := (
              inpPayload.stage0.fullCnt
            )
            outpPayload.stage0.fullBakCnt := (
              inpPayload.stage0.fullBakCnt
            )
            //outpPayload.stage0.fullBakCntMinus1 := (
            //  inpPayload.stage0.fullBakCntMinus1
            //)
            ////tempOutpData.allowOverride
            ////outpPayload.stage2.rdBg.removeAssignments()
            //outpPayload.allowOverride
            for (kdx <- 0 until bgTileRowSlice.size) {
              outpPayload.stage2.rdBg(
                jdx * bgTileRowSlice.size + kdx
              ) := (
                bgTileRowSlice(kdx)
              )
            }
          }
        )
        .setName(s"combineBgSubLineMemA2d_${idx}_${jdx}")
      }
      //--------

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
      //val objSubLineMemInit = new ArrayBuffer[Vec[ObjSubLineMemEntry]]()
      //for (initIdx <- 0 until params.objSubLineMemArrSize) {
      //  val temp = Vec.fill(
      //    //params.objTileSize2d.x
      //    params.objSliceTileWidth
      //  )(ObjSubLineMemEntry())
      //  temp := temp.getZero
      //  objSubLineMemInit += temp
      //}

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
      //wrObjSubLineMemArr += FpgacpuRamSimpleDualPort(
      //  wordType=Vec.fill(
      //    //params.objTileSize2d.x
      //    params.objSliceTileWidth
      //  )(ObjSubLineMemEntry()),
      //  depth=params.objSubLineMemArrSize,
      //  initBigInt=Some(objSubLineMemInitBigInt),
      //  //init=Some(objSubLineMemInit),
      //  arrRamStyle=params.lineArrRamStyle,
      //)
      //  .setName(f"wrObjSubLineMemArr_$idx")
      wrObjSubLineMemArr += PipeMemRmw[
        Vec[ObjSubLineMemEntry],
        WrObjPipeSlmRmwHazardCmp,
        WrObjPipePayload,
        PipeMemRmwDualRdTypeDisabled[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ],
      ](
        wordType=Vec.fill(
          //params.objTileSize2d.x
          params.objSliceTileWidth
        )(ObjSubLineMemEntry()),
        wordCount=params.objSubLineMemArrSize,
        hazardCmpType=WrObjPipeSlmRmwHazardCmp(isAffine=false),
        modType=WrObjPipePayload(isAffine=false),
        modStageCnt=wrObjPipeIdxSlmRmwModStageCnt,
        pipeName=s"wrObjSubLineMemArr_${idx}",
        linkArr=Some(linkArr),
        memArrIdx=idx,
        dualRdType=PipeMemRmwDualRdTypeDisabled[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ],
        optDualRd=false,
        initBigInt=Some(objSubLineMemInitBigInt.toSeq),
        optEnableClear=true,
        //init=Some(objSubLineMemInit),
        //arrRamStyle=params.lineArrRamStyle,
        vivadoDebug=(
          if (idx == 0) (
            vivadoDebug
          ) else (
            false
          )
        ),
      )(
        doHazardCmpFunc=Some(
          (
            curr: PipeMemRmwPayloadExt[
              Vec[ObjSubLineMemEntry],
              WrObjPipeSlmRmwHazardCmp,
            ],
            prev: PipeMemRmwPayloadExt[
              Vec[ObjSubLineMemEntry],
              WrObjPipeSlmRmwHazardCmp,
            ],
            //idx: Int,
            //kind: Boolean
            isPostDelay: Boolean,
          ) => (
            curr.hazardCmp.cmp(
              prev=prev.hazardCmp,
              isPostDelay=isPostDelay,
            )
            //&& (
            //  if (
            //    //idx 
            //    //== PipeMemRmw.numPostFrontStages(
            //    //  modStageCnt=wrObjPipeIdxSlmRmwModStageCnt,
            //    //) - 1
            //    isPostDelay
            //  ) (
            //  ) else (
            //    
            //  )
            //)
            //--------
            //&& 
            //PipeMemRmwPayloadExt.defaultDoHazardCmpFunc(
            //  curr=curr,
            //  prev=prev,
            //)
            //curr.hazardCmp.objAttrsMemIdx
            //=/= prev.hazardCmp.objAttrsMemIdx
          )
        ),
      )
        //.setName(f"wrObjSubLineMemArr_$idx")

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
        wordType=Vec.fill(
          //params.objTileSize2d.x
          params.objSliceTileWidth
        )(ObjSubLineMemEntry()),
        wordCount=params.objSubLineMemArrSize,
        pipeName=s"combineObjSubLineMemArr_${idx}",
        initBigInt=Some(objSubLineMemInitBigInt),
        vivadoDebug=(
          if (idx == 0) (
            vivadoDebug
          ) else (
            false
          )
        ),
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
      //--------

      if (!noAffineObjs) {
        val objAffineSubLineMemInitBigInt = new ArrayBuffer[BigInt]()
        for (initIdx <- 0 until params.objAffineSubLineMemArrSize) {
          objAffineSubLineMemInitBigInt += BigInt(0)
        }
        wrObjAffineSubLineMemArr += FpgacpuRamSimpleDualPort(
          wordType=Vec.fill(
            params.objAffineSliceTileWidth
            //params.objAffineTileSize2d.x
            //params.objAffineDblTileSize2d.x
            //params.objAffineSliceTileWidth
            //params.myDbgObjAffineTileWidth
          )(
            ObjSubLineMemEntry()
          ),
          depth=params.objAffineSubLineMemArrSize,
          //initBigInt=Some(objAffineSubLineMemInitBigInt),
          arrRamStyle=params.lineArrRamStyle,
        )
          .setName(f"wrObjAffineSubLineMemArr_$idx")
        combineObjAffineSubLineMemArr += WrPulseRdPipeSimpleDualPortMem(
          //dataType=HardType.union(
          //  WrObjPipePayload(),
          //  CombinePipePayload(),
          //),
          dataType=CombinePipePayload(),
          wordType=Vec.fill(
            params.objAffineSliceTileWidth
            //params.objAffineTileSize2d.x
            //params.objAffineDblTileSize2d.x
            //params.objAffineSliceTileWidth
            //params.myDbgObjAffineTileWidth
          )(ObjSubLineMemEntry()),
          wordCount=params.objAffineSubLineMemArrSize,
          pipeName=s"combineObjAffineSubLineMemArr_${idx}",
          initBigInt=Some(objAffineSubLineMemInitBigInt),
          vivadoDebug=false,
        )(
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
            outpPayload.stage2.rdObjAffine := objTileRow
          }
        )
        .setName(f"combineObjAffineSubLineMemArr_$idx")
      }

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
    //println(
    //  s"wrBgPipeBakCntStart: $wrBgPipeBakCntStart"
    //)
    //--------
    // BEGIN: for muxing writes into `objSubLineMemArr`; later 
    case class ObjSubLineMemWriter(
      someWrObjSubLineMemArr: Option[ArrayBuffer[
        FpgacpuRamSimpleDualPort[Vec[ObjSubLineMemEntry]]
      ]],
      someCombineObjSubLineMemArr: ArrayBuffer[
        WrPulseRdPipeSimpleDualPortMem[
          //Bits,
          CombinePipePayload,
          Vec[ObjSubLineMemEntry]
        ]
      ],
      //extName: String,
      isAffine: Boolean,
    ) extends Area {
      def extName = (
        if (!isAffine) {
          ""
        } else {
          "Affine"
        }
      )
      def wrIdx = 0
      def combineIdx = 1
      def myTempObjSubLineMemArrSize = (
        if (!isAffine) {
          params.objSubLineMemArrSize
        } else {
          params.objAffineSubLineMemArrSize
        }
      )
      //def myTempObjTileWidth = (
      //  if (!isAffine) {
      //    params.objTileSize2d.x
      //  } else {
      //    params.objAffineDblTileSize2d.x
      //    //params.objAffineSliceTileWidth
      //    //params.myDbgObjAffineTileWidth
      //  }
      //)
      def myTempObjTileWidth = params.tempObjTileWidth(
        //if (!isAffine) {
        //  0
        //} else {
        //  1
        //}
        isAffine=isAffine
      )
      val addrVec = Vec.fill(params.numLineMemsPerBgObjRenderer)(
        //Reg(
          UInt(log2Up(myTempObjSubLineMemArrSize) bits)
        //) init(0x0)
      )
      val dataVec = Vec.fill(params.numLineMemsPerBgObjRenderer)(
        Vec.fill(myTempObjTileWidth)(
          //Reg(
            ObjSubLineMemEntry()
          //) init(ObjSubLineMemEntry().getZero)
        )
      )
      val enVec = Vec.fill(params.numLineMemsPerBgObjRenderer)(
        //Reg(Bool()) init(False)
        Bool()
      )
      val clearValidVec = Vec.fill(params.numLineMemsPerBgObjRenderer)(
        Bool()
      )
      clearValidVec(0) := False
      clearValidVec(1) := True
      for (idx <- 0 until params.numLineMemsPerBgObjRenderer) {
        addrVec(idx) := addrVec(idx).getZero
        addrVec(idx).allowOverride
        dataVec(idx) := dataVec(idx).getZero
        dataVec(idx).allowOverride
        enVec(idx) := enVec(idx).getZero
        enVec(idx).allowOverride
      }
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
        //--------
        for (jdx <- 0 until params.numLineMemsPerBgObjRenderer) {
          //def vecIdx0 = (rWrLineMemArrIdx + jdx)(0 downto 0)
          val vecIdx = UInt(1 bits)
            .setName(f"obj" + extName + f"Writer_doWrite_vecIdx_$jdx")
          vecIdx := (
            //(rWrLineMemArrIdx + jdx)(0 downto 0)
            (wrObjWriterLineMemArrIdx(jdx) + jdx)(0 downto 0)
            //(rWrLineMemArrIdx + jdx + 1)(0 downto 0)
          )
          val tempAddr = cloneOf(addrVec(jdx))
            .setName(f"obj" + extName + f"Writer_doWrite_tempAddr_$jdx")
          val tempData = cloneOf(dataVec(jdx))
            .setName(f"obj" + extName + f"Writer_doWrite_tempData_$jdx")
          val tempEn = cloneOf(enVec(jdx))
            .setName(f"obj" + extName + f"Writer_doWrite_tempEn_$jdx")
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
          //someCombineObjSubLineMemArr(jdx).io.unionIdx := rWrLineMemArrIdx
          someCombineObjSubLineMemArr(jdx).io.unionIdx := (
            wrObjWriterLineMemArrIdx(jdx)
          )
          someCombineObjSubLineMemArr(jdx).io.wrPulse.valid := (
            //objWriter.enVec(vecIdx)
            tempEn
          )
          someCombineObjSubLineMemArr(jdx).io.wrPulse.addr := (
            //objWriter.addrVec(vecIdx)
            tempAddr
          )
          //println({
          //  def wrDataSize = (
          //    someCombineObjSubLineMemArr(jdx).io.wrPulse.data.size
          //  )
          //  def tempDataSize = tempData.size
          //  f"sizes: $wrDataSize $tempDataSize"
          //})
          someCombineObjSubLineMemArr(jdx).io.wrPulse.data := (
            //objWriter.dataVec(vecIdx)
            tempData
          )
          //wrObjSubLineMemArr(jdx).write(
          //  address=tempAddr,
          //  data=tempData,
          //  enable=tempEn,
          //)
          someWrObjSubLineMemArr match {
            case Some(myWrObjSubLineMemArr) => {
              myWrObjSubLineMemArr(jdx).io.wrEn := tempEn
              myWrObjSubLineMemArr(jdx).io.wrAddr := tempAddr
              myWrObjSubLineMemArr(jdx).io.wrData := tempData
            }
            case None => {
            }
          }
          //if (!isAffine) {
          //  //when (tempEn) {
          //    //switch (rCombineFullLineMemArrIdx) {
          //    //}
          //    when (rCombineFullLineMemArrIdx === jdx) {
          //    }
          //  //}
          //}
          if (!isAffine) {
            //switch (rCombineFullLineMemArrIdx) {
            //  for (jdx <- 0 until ) {
            //  }
            //}
            val clearVecIdx = (
              (wrObjWriterLineMemArrIdx(jdx) + jdx)(0 downto 0)
            )
            val tempClearAddr = (
              //Mux[UInt](
              //  rWrLineMemArrIdx === jdx,
              //  addrVec(jdx)
              //  //addrVec(vecIdx0),
              //  //addrVec(vecIdx1),
              //)
              addrVec(clearVecIdx)
              //addrVec(jdx)
              //addrVec(wrIdx)
              //addrVec(rWrLineMemArrIdx)
            )
            val tempClearValid = (
              clearValidVec(clearVecIdx)
            )
            //val tempClearData = (
            //  //Mux[Vec[ObjSubLineMemEntry]](
            //  //  rWrLineMemArrIdx === jdx,
            //  //  dataVec(vecIdx0),
            //  //  dataVec(vecIdx1),
            //  //)
            //  dataVec(clearVecIdx)
            //  //dataVec(jdx)
            //  //dataVec(wrIdx)
            //  //dataVec(rWrLineMemArrIdx)
            //)
            //val tempClearEn = (
            //  //Mux[Bool](
            //  //  rWrLineMemArrIdx === jdx,
            //  //  enVec(vecIdx0),
            //  //  enVec(vecIdx1),
            //  //)
            //  enVec(clearVecIdx)
            //  //enVec(jdx)
            //)
            val rClear = (
              Reg(cloneOf(wrObjSubLineMemArr(jdx).io.clear))
              .setName(s"wrObjWriter_${extName}_rClear_${jdx}")
            )
            wrObjSubLineMemArr(jdx).io.clear <-< rClear
            rClear.valid := (
              //clearVecIdx =/= jdx
              tempClearValid
            )
            rClear.payload := tempClearAddr
            //switch (clearVecIdx) {
            //  for (kdx <- 0 until (1 << clearVecIdx.getWidth)) {
            //    is (kdx) {
            //      wrObjSubLineMemArr(jdx).io.clear.valid := (
            //        kdx == jdx
            //      )
            //    }
            //  }
            //}
          }

          //objSubLineMemArr(jdx).io.wrPulse.valid := tempEn
          //objSubLineMemArr(jdx).io.wrPulse.addr := tempAddr
          //objSubLineMemArr(jdx).io.wrPulse.data := tempData

        }

        //}
        //when (objWriter.enVec(tempCombineLineMemArrIdx)) {
        //}
      }
    }
    val objWriter = ObjSubLineMemWriter(
      someWrObjSubLineMemArr=(
        //wrObjSubLineMemArr
        None
      ),
      someCombineObjSubLineMemArr=combineObjSubLineMemArr,
      //extName=""
      isAffine=false,
    )
    val objAffineWriter = (!noAffineObjs) generate (
      ObjSubLineMemWriter(
        someWrObjSubLineMemArr=Some(wrObjAffineSubLineMemArr),
        someCombineObjSubLineMemArr=combineObjAffineSubLineMemArr,
        //extName="Affine"
        isAffine=true,
      )
    )
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
    if (!noAffineObjs) {
      objAffineWriter.doWrite()
    }
    //--------
    //def wrBgPipeBgIdxWidth = params.numBgsPow + 1
    def wrBgPipeBgIdxWidth = params.numBgsPow
    //val rWrBgPipeFrontCntWidth = KeepAttribute(
    //  Reg(UInt(wrBgPipeFrontCntWidth bits)) init(0x0)
    //)
    case class WrBgPipePayload() extends Bundle {
      // pixel x-position
      //val pxPosXVec = Vec.fill(params.numBgs)(
      //  UInt(log2Up(params.intnlFbSize2d.x) bits)
      //)
      case class Stage0() extends Bundle {
        //val lineMemArrIdx = cloneOf(rWrLineMemArrIdx)
        //val lineMemArrIdx = UInt(
        //  //log2Up(params.numLineMemsPerBgObjRenderer) bits
        //  lineMemArrIdxWidth bits
        //)
        //val lineNum = UInt(wrLineNumWidth bits)
        val fullLineNum = UInt(wrFullLineNumWidth bits)
        def lineNum = fullLineNum(
          //fullLineNum.high downto log2Up(params.physFbSize2dScale.y)
          fullLineNum.high downto 0
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
            wrBgPipeBgIdxWidth - 1 downto 0
          )
        )
        //val scroll = cloneOf(bgAttrsArr(0).scroll)
        //val bgAttrs = Vec.fill(params.bgTileSize2d.x)(
        //  cloneOf(bgAttrsArr(0))
        //)
        val bgAttrs = cloneOf(bgAttrsArr(0))
        val colorMathAttrs = (!noColorMath) generate Gpu2dBgAttrs(
          params=params,
          isColorMath=true,
        )
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
      val stage0 = Stage0()
      def fullLineNum = stage0.fullLineNum
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
      def colorMathAttrs = stage0.colorMathAttrs
      //def scroll = bgAttrs.scroll
      //def visib = stage0.bgAttrs.visib
      //def tilePxsCoord = stage0.tilePxsCoord

      // Stages after stage 0
      // NOTE: these pipeline stages are still separate
      case class Stage1() extends Bundle {
        //--------
        val bgEntryMemIdx = Vec.fill(params.bgTileSize2d.x)(
          UInt(params.bgEntryMemIdxWidth bits)
        )
        val pxPos = Vec.fill(params.bgTileSize2d.x)(
          params.bgPxsCoordT()
        )
        //--------
        //val haveTwoMyIdx = Vec.fill(
        //  //params.bgTileSize2d.x
        //  params.numBgMemsPerKind
        //)(
        //  Bool()
        //)
        //val haveTwoMyIdx = Bool()
        val pxPosXGridIdx = (
          Vec.fill(params.bgTileSize2d.x)(
            UInt(
              (
                log2Up(params.oneLineMemSize) - params.bgTileSize2dPow.x
                + 1
              )
              bits
            )
          )
        )
        val pxPosXGridIdxFindFirstSameAsFound = Bool()
        val pxPosXGridIdxFindFirstSameAsIdx = UInt(
          params.bgTileSize2dPow.x bits
        )
        val pxPosXGridIdxFindFirstDiffFound = Bool()
        val pxPosXGridIdxFindFirstDiffIdx = UInt(
          params.bgTileSize2dPow.x bits
        )

        //val pxPosXGridIdxFindFirstSameAsFound = Bool()
        //val pxPosXGridIdx
        //val myIdxVec = Vec.fill(params.bgTileSize2d.x)(
        //  //UInt(params.bgTileSize2dPow.x bits)
        //  //UInt(1 bits)
        //  //UInt(params.bgEntryMemIdxWidth bits)
        //  //UInt(1 bits)
        //  UInt(
        //    (log2Up(params.oneLineMemSize) - params.bgTileSize2dPow.x)
        //    bits
        //  )
        //)
        //--------
      }
      case class Stage2() extends Bundle {
        //--------
        //val pxPosXGridIdx = (
        //  Vec.fill(params.bgTileSize2d.x)(
        //    UInt(
        //      (log2Up(params.oneLineMemSize) - params.bgTileSize2dPow.x)
        //      bits
        //    )
        //  )
        //)
        //val pxPosXGridIdxLsb = (
        //  Vec.fill(params.bgTileSize2d.x)(
        //    //UInt(1 bits)
        //    Bool()
        //  )
        //)
        //val pxPosXGridIdxFindFirstSameAsFound = Bool()
        //val pxPosXGridIdxFindFirstSameAsIdx = UInt(
        //  params.objTileSize2dPow.x bits
        //)
        //val pxPosXGridIdxFindFirstDiffFound = Bool()
        //val pxPosXGridIdxFindFirstDiffIdx = UInt(
        //  params.objTileSize2dPow.x bits
        //)
        def numMyIdxVecs = 4 + params.bgTileSize2d.x
        val myIdxV2d = Vec.fill(params.bgTileSize2d.x)(
          Vec.fill(numMyIdxVecs)(
            UInt(params.bgTileSize2dPow.x bits)
          )
        )
        //--------
        val bgEntryMemIdxSameAs = UInt(log2Up(params.numTilesPerBg) bits)
        val bgEntryMemIdxDiff = UInt(log2Up(params.numTilesPerBg) bits)
        //--------
        val fbRdAddrMultTileMemBaseAddr = UInt(
          params.fbRdAddrMultWidthPow bits
        )
        val fbRdAddrMultPxPosY = UInt(
          params.fbRdAddrMultWidthPow bits
        )
        //--------
      }
      case class Stage3(
        isColorMath: Boolean,
      ) extends Bundle {
        val bgEntry = (
          Vec.fill(params.numBgMemsPerNonPalKind)(
            Vec.fill(params.bgTileSize2d.x)(
              Gpu2dBgEntry(
                params=params,
                isColorMath=isColorMath,
              )
            )
          )
        )
        val tempAddr = Vec.fill(params.bgTileSize2d.x)(
          UInt(log2Up(params.numTilesPerBg) bits)
        )
      }
      case class Stage4(
        isColorMath: Boolean
      ) extends Bundle {
        //--------
        // `Gpu2dBgEntry`s that have been read
        val bgEntry = Vec.fill(params.bgTileSize2d.x)(
          Gpu2dBgEntry(
            params=params,
            isColorMath=isColorMath,
          )
        )
        //--------
        val fbRdAddrMultPlus = UInt(params.fbRdAddrMultWidthPow bits)
        //--------
      }
      case class Stage5() extends Bundle {
        val fbRdAddrFinalPlus = Vec.fill(params.bgTileSize2d.x)(
          UInt(params.fbRdAddrMultWidthPow bits)
        )
      }
      case class Stage6(
        isColorMath: Boolean
      ) extends Bundle {
        //val tilePxsPosY = UInt(params.bgTileSize2dPow.y bits)
        //val tempTilePxsPos = Vec.fill(params.bgTileSize2d.x)(
        //  params.bgTilePxsCoordT()
        //)
        val tilePxsCoord = Vec.fill(params.bgTileSize2d.x)(
          params.bgTilePxsCoordT()
        )
        //val tileMemRdAddrSameAsGridIdx = UInt(1 bits)
        //val tileMemRdAddrDiffGridIdx = UInt(1 bits)
        val tileMemRdAddrFront = UInt(params.bgTileMemIdxWidth bits)
        val tileMemRdAddrBack = UInt(params.bgTileMemIdxWidth bits)
        def myTileIdxWidth = (
          if (!isColorMath) (
            params.numBgTilesPow
          ) else ( // if (isColorMath)
            params.numColorMathTilesPow
          )
        )
        val tileIdxFront = UInt(myTileIdxWidth bits)
        val tileIdxBack = UInt(myTileIdxWidth bits)
        //val tileIdxFrontLsb = Bool()
        //val tileIdxBackLsb = Bool()
        //--------
        //val tileGridIdx = (
        //  Vec.fill(params.bgTileSize2d.y)(
        //    UInt(
        //      (
        //        //log2Up(params.oneLineMemSize) - params.bgTileSize2dPow.y
        //        //+ 1
        //        //params.numBgTilesPow - 1 //bits
        //        (
        //          if (!isColorMath) (
        //            params.numBgTilesPow
        //          ) else ( // if (isColorMath)
        //            params.numColorMathTilesPow
        //          )
        //        ) + params.bgTileSize2dPow.y
        //      )
        //      bits
        //    )
        //  )
        //)
        //val tileGridIdxFindFirstSameAsFound = Bool()
        //val tileGridIdxFindFirstSameAsIdx = UInt(
        //  params.bgTileSize2dPow.x bits
        //)
        //val tileGridIdxFindFirstDiffFound = Bool()
        //val tileGridIdxFindFirstDiffIdx = UInt(
        //  params.bgTileSize2dPow.x bits
        //)
        //--------
      }
      case class Stage7() extends Bundle {
        //val tilePxsCoord = Vec.fill(params.bgTileSize2d.x)(
        //  params.bgTilePxsCoordT()
        //)
        // `Gpu2dTile`s that have been read
        //val tile = Vec.fill(params.bgTileSize2d.x)(
        //  Gpu2dTileFull(
        //    params=params,
        //    isObj=false,
        //    isAffine=false,
        //  )
        //)
        val tileSlice = Vec.fill(params.bgTileSize2d.x)(
          Gpu2dTileSlice(
            params=params,
            isObj=false,
            isAffine=false,
          )
        )
      }
      case class Stage8() extends Bundle {
        val palEntryMemIdx = Vec.fill(params.bgTileSize2d.x)(
          UInt(params.bgPalEntryMemIdxWidth bits)
        )
      }
      case class Stage9() extends Bundle {
        // Whether `palEntryMemIdx(someBgIdx)` is non-zero
        val palEntryNzMemIdx = Vec.fill(params.bgTileSize2d.x)(
          Bool()
        )
      }
      // The following BG pipeline stages are only performed when
      // `palEntryNzMemIdx(someBgIdx)` is `True`
      // `Gpu2dPalEntry`s that have been read
      case class Stage11() extends Bundle {
        val palEntry = Vec.fill(params.bgTileSize2d.x)(
          Gpu2dPalEntry(params=params)
        )
      }
      case class Stage12() extends Bundle {
        //val doWrite = Bool()
        val subLineMemEntry = Vec.fill(params.bgTileSize2d.x)(
          BgSubLineMemEntry()
        )
      }
      case class PostStage0(
        isColorMath: Boolean
      ) extends Bundle {
        //// scroll
        //val scroll = cloneOf(bgAttrsArr(0).scroll)
        // indices into `bgEntryMem`
        //val bgEntryMemIdxPart0 = Vec2(UInt(params.bgEntryMemIdxWidth bits))
        //val bgEntryMemIdxPart1 = UInt(params.bgEntryMemIdxWidth bits)
        val stage1 = Stage1()
        val stage2 = Stage2()
        val stage3 = Stage3(
          isColorMath=isColorMath
        )
        val stage4 = Stage4(
          isColorMath=isColorMath
        )
        //val tileMemIdx = UInt(params.bgTileMemIdxWidth bits)
        val stage5 = Stage5()
        val stage6 = Stage6(
          isColorMath=isColorMath
        )
        val stage7 = Stage7()
        val stage8 = Stage8()
        val stage9 = Stage9()
        val stage11 = Stage11()
        val stage12 = Stage12()
        //--------
        def bgEntryMemIdx = stage1.bgEntryMemIdx
        def pxPos = stage1.pxPos
        //def bgEntryMemIdxPart0 = postStage0.bgEntryMemIdxPart0
        //def bgEntryMemIdxPart1 = postStage0.bgEntryMemIdxPart1

        def pxPosXGridIdx = stage1.pxPosXGridIdx
        def pxPosXGridIdxFindFirstSameAsFound = (
          stage1.pxPosXGridIdxFindFirstSameAsFound
        )
        def pxPosXGridIdxFindFirstSameAsIdx = (
          stage1.pxPosXGridIdxFindFirstSameAsIdx
        )
        def pxPosXGridIdxFindFirstDiffFound = (
          stage1.pxPosXGridIdxFindFirstDiffFound
        )
        def pxPosXGridIdxFindFirstDiffIdx = (
          stage1.pxPosXGridIdxFindFirstDiffIdx 
        )
        //--------
        def myIdxV2d = stage2.myIdxV2d
        //--------
        def bgEntry = stage4.bgEntry
        //--------
        //def tempTilePxsPos = stage4.tempTilePxsPos
        def tilePxsCoord = stage6.tilePxsCoord
        def tileIdxFront = stage6.tileIdxFront
        def tileIdxBack = stage6.tileIdxBack
        //def tileIdxFrontLsb = stage5.tileIdxFrontLsb
        //def tileIdxBackLsb = stage5.tileIdxBackLsb
        //--------
        def tileSlice = stage7.tileSlice
        //--------
        def palEntryMemIdx = stage8.palEntryMemIdx
        //--------
        def palEntryNzMemIdx = stage9.palEntryNzMemIdx
        //--------
        def palEntry = stage11.palEntry
        //--------
        def subLineMemEntry = stage12.subLineMemEntry
        //--------
      }
      val postStage0 = PostStage0(isColorMath=false)
      val colorMath = (!noColorMath) generate (
        PostStage0(isColorMath=true)
      )

      //def scroll = postStage0.scroll
      def cmath1 = (!noColorMath) generate colorMath.stage1
      def stage1 = postStage0.stage1

      def cmath2 = (!noColorMath) generate colorMath.stage2
      def stage2 = postStage0.stage2

      def cmath3 = (!noColorMath) generate colorMath.stage3
      def stage3 = postStage0.stage3

      def cmath4 = (!noColorMath) generate colorMath.stage4
      def stage4 = postStage0.stage4

      def cmath5 = (!noColorMath) generate colorMath.stage5
      def stage5 = postStage0.stage5

      def cmath6 = (!noColorMath) generate colorMath.stage6
      def stage6 = postStage0.stage6

      //def stage4 = postStage0.stage4

      def cmath7 = (!noColorMath) generate colorMath.stage7
      def stage7 = postStage0.stage7

      def cmath8 = (!noColorMath) generate colorMath.stage8
      def stage8 = postStage0.stage8

      def cmath11 = (!noColorMath) generate colorMath.stage11
      def stage11 = postStage0.stage11

      def cmath12 = (!noColorMath) generate colorMath.stage12
      def stage12 = postStage0.stage12
      //def doWrite = stage7.doWrite
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
        ////ret.lineNum := wrLineNumInit
        //ret.lineNum := wrBgLineNumInit
        ret.fullLineNum := wrBgFullLineNumInit
      } else {
        //ret.lineNum := rGlobWrBgLineNumPipe1(ret.lineNum.bitsRange)
        ret.fullLineNum := wrBgGlobPipe.rLineNumPipe1(
          //rGlobWrBgFullLineNumPipe1
          ret.fullLineNum.bitsRange
        )
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
      if (!noColorMath) {
        ret.colorMathAttrs := ret.colorMathAttrs.getZero
        ret.colorMath := ret.colorMath.getZero
      }
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

    def wrBgPipeNumMainStages = (
      //7
      //11
      //12
      13
      //14
      //9
    )

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
    def wrObjPipeCntWidth(
      isAffine: Boolean
    ) = (
      //params.objTileSize2dPow.x + params.objAttrsMemIdxWidth + 1
      //params.objTileSize2dPow.x + params.objAttrsMemIdxWidth
      //params.objAttrsMemIdxWidth + 1
      (
        if (!isAffine) {
          (
            //params.objAttrsMemIdxWidth
            //+ 1 // for the extra cycle delay between pixels
            params.objAttrsMemIdxTileCntWidth
          )
        } else {
          //params.objAffineAttrsMemIdxWidth
          //+ params.objAffineTileWidthRshift
          params.objAffineAttrsMemIdxTileCntWidth
        }
      ) + 1 + 1
      
    )
    def wrObjPipeBakCntStart(
      isAffine: Boolean
    ) = (
      //(1 << (params.objTileSize2dPow.x + params.objAttrsMemIdxWidth))
      //- 1
      //(1 << params.objAttrsMemIdxWidth)
      //- 1
      (
        1
        << (
          (
            if (!isAffine) {
              (
                //params.objAttrsMemIdxWidth + 1
                //+ 1 // for the extra cycle delay between pixels
                ////+ 1
                params.objAttrsMemIdxTileCntWidth
              )
            } else {
              //params.objAffineAttrsMemIdxWidth
              //+ params.objAffineTileWidthRshift
              params.objAffineAttrsMemIdxTileCntWidth
            }
          )
        )
      )
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
      //12
      //14
      //15
      //16
      //17
      20
    )
    def wrBgObjPipeNumStages = max(
      wrBgPipeNumMainStages,
      wrObjPipeNumMainStages
    ) //+ 1
    //case class WrObjPipeStage12Fwd(
    //  isAffine: Boolean
    //) extends Bundle {
    //  val pxPosXGridIdx = UInt(
    //    (
    //      params.objPxsCoordSize2dPow.x
    //      - (
    //        if (!isAffine) {
    //          params.objTileSize2dPow.x
    //        } else {
    //          params.objAffineDblTileSize2dPow.x
    //          //params.objAffineSliceTileWidthPow
    //          //params.myDbgObjAffineTileWidthPow
    //        }
    //      )
    //    ) bits
    //  )
    //  //val pxPosY = SInt(params.objPxsCoordSize2dPow.y bits)
    //  val pxPosYLsb = Bool()
    //  //val doFwd = Bool()
    //  //val pxPosX = SInt(params.objPxsCoordSize2dPow.x bits)
    //}

    ////def wrObjPipeStage6NumFwd = 3
    ////def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6
    ////def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6 + 1
    ////def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6 + 1 + 1
    ////def wrObjPipeStage6NumFwd = wrBgObjPipeNumStages - 6 + 1 + 1
    ////def wrObjPipeStage9NumFwd = wrBgObjPipeNumStages - 9 + 1 + 1
    ////def wrObjPipeStage9NumFwd = wrBgObjPipeNumStages - 9 + 1 + 1
    ////def wrObjPipeStage12NumFwd = wrBgObjPipeNumStages - 11 + 1 + 1
    //def wrObjPipeStage14NumFwd = (
    //  wrBgObjPipeNumStages - 14 - 1 + 1 + 1
    //)
    ////def wrObjPipeStage14NumFwd = wrBgObjPipeNumStages - 14 + 1 + 1
    ////def wrObjPipeStage6NumFwd = 2
    ////def wrObjPipeStage6NumFwd = 1
    //def wrObjPipeStage12NumFwd = wrObjPipeStage14NumFwd //+ 1
    //case class WrObjPipeStage14Fwd(
    //  isAffine: Boolean,
    //) extends Bundle {
    //  //val pxPosX = SInt(params.objPxsCoordSize2dPow.x bits)
    //  //val pxPosInLine = Bool()
    //  //val palEntry = Gpu2dPalEntry(params=params)
    //  //val stage6 = Stage6()
    //  //val pxPosXIsSame = Bool()
    //  //val pxPosXIsSame = Bool()
    //  //val prio = UInt(params.numBgsPow bits)
    //  //--------
    //  // BEGIN: comment this out; move to stage 10
    //  //val objAttrsMemIdx = UInt(
    //  //  //(wrObjPipeCntWidth - params.objAttrsMemIdxWidth.x) bits
    //  //  params.objAttrsMemIdxWidth bits
    //  //)
    //  ////val pxPosY = SInt(params.objPxsCoordSize2dPow.y bits)
    //  //val pxPosXGridIdx = UInt(
    //  //  (params.objPxsCoordSize2dPow.x - params.objTileSize2dPow.x) bits
    //  //)
    //  //val pxPosYLsb = Bool()
    //  // END: comment this out; move to stage 10
    //  //--------
    //  //val stage10Fwd = Bool()
    //  //val pxPosXGridIdxLsb = Bool()

    //  //val pxPos = params.objPxsCoordT()
    //  //val prio = UInt(params.numBgsPow bits)
    //  //val overwriteLineMemEntry = Bool()
    //  //val wrLineMemEntry = ObjSubLineMemEntry()
    //  //val extSingle = WrObjPipe6ExtSingle()
    //  //val ext = WrObjPipe9Ext(useVec=false)
    //  val ext = WrObjPipe14Ext(
    //    isAffine=isAffine,
    //    useVec=false,
    //  )
    //  //val wholeWrLineMemEntry = Vec.fill(params.objTileSize2d.x)(
    //  //  ObjSubLineMemEntry()
    //  //)
    //  def overwriteLineMemEntry = ext.overwriteLineMemEntry
    //  def wrLineMemEntry = ext.wrLineMemEntry
    //}
    case class WrObjPipeStage0JustCopy(
      isAffine: Boolean
    ) extends Bundle {
      //val lineNum = UInt(wrLineNumWidth bits)
      val fullLineNum = UInt(wrFullLineNumWidth bits)
      def lineNum = fullLineNum(
        //fullLineNum.high downto log2Up(params.physFbSize2dScale.y)
        fullLineNum.high downto 0x0
      )
      val cnt = UInt(wrObjPipeCntWidth(isAffine) bits)
      val bakCnt = UInt(wrObjPipeCntWidth(isAffine) bits)
      val bakCntMinus1 = UInt(wrObjPipeCntWidth(isAffine) bits)
      def myMemIdxWidth = (
        if (!isAffine) {
          params.objAttrsMemIdxWidth 
        } else {
          (
            params.objAffineAttrsMemIdxWidth
            //+ params.objAffineTileWidthRshift
            //params.objAffineAttrsMemIdxTileCntWidth
          )
        }
      )
      val dbgTestIdx = UInt(
        (
          //params.objAttrsMemIdxWidth + 1 + 1
          //params.objAttrsMemIdxWidth + 3
          //params.objAttrsMemIdxWidth + 2
          params.objAttrsMemIdxTileCntWidth
        ) bits
      )
      val dbgTestAffineIdx = UInt(
        params.objAffineAttrsMemIdxTileCntWidth bits
      )
      //val objAttrsMemIdx = UInt(
      //  //params.objAttrsMemIdxWidth bits
      //  (params.objAttrsMemIdxWidth + 1) bits
      //)
    }
    case class WrObjPipeSlmRmwHazardCmp(
      isAffine: Boolean,
    ) extends Bundle {
      //val cnt = UInt(wrObjPipeCntWidth(isAffine) bits),
      //val rawObjAttrsMemIdx
      //val objAttrsMemIdx = (!isAffine) generate (
      //  UInt(
      //    (params.objAttrsMemIdxTileCntWidth - params.objCntWidthShift)
      //    bits
      //  )
      //)
      //val affineObjAttrsMemIdx = (isAffine) generate (
      //  UInt(
      //    //(params.objAttrsMemIdxTileCntWidth - params.objCntWidthShift)
      //    params.objAffineAttrsMemIdxWidth
      //    bits
      //  )
      //)

      val objIdx = UInt(
        (
          params.objAttrsMemIdxWidth
          //.max(params.objAffineAttrsMemIdxWidth)
        )
        bits
      )
      val objIdxPlus1 = cloneOf(objIdx)

      val anyPxPosInLine = Bool()
      def cmp(
        prev: WrObjPipeSlmRmwHazardCmp,
        isPostDelay: Boolean
      ) = {
        //println(s"cmp(): isPostDelay: $isPostDelay")
        (
          if (
            //idx 
            //== PipeMemRmw.numPostFrontStages(
            //  modStageCnt=wrObjPipeIdxSlmRmwModStageCnt,
            //) - 1
            !isPostDelay
          ) (
            (
              //if (!isAffine) (
              //  this.objAttrsMemIdx
              //  =/= prev.objAttrsMemIdx
              //) else (
              //  this.affineObjAttrsMemIdx
              //  =/= prev.affineObjAttrsMemIdx
              //)
              True
              //pxPosXGridIdxLsb
              //=== prev.pxPosXGridIdxLsb
            ) && (
              anyPxPosInLine
              //&& prev.anyPxPosInLine
            )
          ) else ( // if (isPostDelay)
            //anyPxPosInLine
            //&& prev.anyPxPosInLine
            //&&
            (
              //objIdx + 1 =/= prev.objIdx
              objIdxPlus1 =/= prev.objIdx
            )
          )
        )
      }
    }
    def tempObjTileWidthPow(isAffine: Boolean) = (
      params.tempObjTileWidthPow(isAffine=isAffine)
    )
    def tempObjTileWidth(isAffine: Boolean) = (
      params.tempObjTileWidth(isAffine=isAffine)
    )

    def tempObjTileWidth1(isAffine: Boolean) = (
      params.tempObjTileWidth1(isAffine=isAffine)
    )
    def tempObjTileWidthPow1(isAffine: Boolean) = (
      params.tempObjTileWidthPow1(isAffine=isAffine)
    )

    def tempObjTileWidthPow2(isAffine: Boolean) = (
      params.tempObjTileWidthPow2(isAffine=isAffine)
    )
    def tempObjTileWidth2(isAffine: Boolean) = (
      params.tempObjTileWidth2(isAffine=isAffine)
    )

    def tempObjTileHeight(isAffine: Boolean) = (
      params.tempObjTileHeight(isAffine=isAffine)
    )
    def ObjTilePxsCoordT(isAffine: Boolean) = (
      params.anyObjTilePxsCoordT(isAffine=isAffine)
    )
    def numMyIdxVecs(isAffine: Boolean) = (
      //4 + tempObjTileWidth() * 2
      4 + tempObjTileWidth1(isAffine) * 2
      //4 + tempObjTileWidth2() * 2
    )
    case class WrObjPipePayloadStage0(isAffine: Boolean) extends Bundle {
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
      val justCopy = WrObjPipeStage0JustCopy(isAffine=isAffine)
      def fullLineNum = justCopy.fullLineNum
      def lineNum = justCopy.lineNum
      def cnt = justCopy.cnt
      def bakCnt = justCopy.bakCnt
      def bakCntMinus1 = justCopy.bakCntMinus1
      //def affineIdx = cnt(params.objAttrsMemIdxWidth - 1 downto 2)
      //def affineIdx = justCopy.affineIdx
      def rawIdx() = (
        justCopy.dbgTestIdx
      )
      def rawAffineIdx() = (
        justCopy.dbgTestAffineIdx
        //(
        //  params.objAttrsMemIdxWidth - 1 downto 0
        //)
      )

      def rawObjAttrsMemIdx() = (
        justCopy.dbgTestIdx(
          //params.objAttrsMemIdxWidth - 1 downto 0
          //params.objAttrsMemIdxWidth + 1 - 1 downto 0
          //params.objAttrsMemIdxWidth + 1 + 1 - 1 downto 0
          //params.objAttrsMemIdxWidth + 1 - 1 downto 1
          //params.objAttrsMemIdxWidth + 3 - 1 downto 0
          //params.objAttrsMemIdxWidth + 2 - 1 downto 0
          justCopy.dbgTestIdx.high downto 0
        )
      )
      def objAttrsMemIdx() = (
        justCopy.dbgTestIdx(
          //params.objAttrsMemIdxWidth - 1 downto 0
          //params.objAttrsMemIdxWidth + 1 - 1 downto 1
          //params.objAttrsMemIdxWidth + 1 + 1 - 1 downto 1 + 1
          //params.objAttrsMemIdxWidth + 3 - 1 downto 3
          //params.objAttrsMemIdxWidth + 2 - 1 downto 2
          justCopy.dbgTestIdx.high
          downto params.objCntWidthShift
        )
      )
      def objXStart() = {
        (
          //def tempShift = tempAffineShift
          //if (params.objTileWidthRshift > 0) {
            Cat(
              rawIdx()(
                (
                  ////objXStartRawWidthPow
                  ////params.objSliceTileWidthPow
                  //1 // to account for double size rendering
                  ////+ (
                  ////  if (params.objTileWidthRshift == 0) {
                  ////    1 // for the extra cycle delay between pixels
                  ////  } else {
                  ////    params.objTileWidthRshift
                  ////  }
                  ////)
                  //+
                  //tempAffineShift
                  params.objTileWidthRshift
                  //params.objSliceTileWidthPow
                  + 1 // account for the extra cycle delay
                  + 1 // account for grid index
                  - 1 
                )
                //downto params.objSliceTileWidthPow
                //downto params.objTileWidthRshift + 1
                downto (
                  //if (params.objTileWidthRshift == 0) {
                  //  1
                  //} else {
                  //  0
                  //}
                  //tempAffineShift
                  //0
                  //1 // account for double size rendering
                  //params.objTileWidthRshift
                  + 1 // account for the extra cycle delay
                  + 1 // account for grid index
                )
              ),
              B(
                params.objSliceTileWidthPow bits,
                //params.objTileWidthRshift bits,
                //params.objTileWidthRshift bits,
                default -> False
              ),
            ).asUInt
          //} else {
          //  U"00".resized
          //}
        )
      }

      //def affineMultKind() = rawAffineIdx()(1 downto 0)

      // (a  b) × (x) = (ax+by)
      // (c  d)   (y)   (cx+dy)

      //def affineMultIdxX() = rawAffineIdx()(0 downto 0)
      //def affineMultIdxY() = rawAffineIdx()(1 downto 1)
      //def affineActive = (
      //  rawAffineIdx()(1 downto 0)
      //  === (
      //    if (params.fancyObjPrio) {
      //      U(2 bits, default -> False)
      //    } else {
      //      U(2 bits, default -> True)
      //    }
      //  )
      //)
      //def affineMultKindMult() = rawAffineIdx() 
      //def affineMultKindAdd() = rawAffineIdx()
      //def affineObjXStartRawWidthPow = (
      //  params.objAffineTileWidthRshift
      //  + 1
      //)
      def tempAffineShift = (
        //1 // account for the extra cycle delay between pixels
        ////(
        ////  //if (params.objAffineTileWidthRshift == 0) {
        ////  //  
        ////  //} else {
        ////  //}
        ////)
        //+ params.objAffineTileWidthRshift
        //+ 1 // account for double size rendering
        //+ 1 // account for the rendering grid
        params.objAffineCntWidthShift
      )
      def affineObjAttrsMemIdx() = {
        //def tempShift = tempAffineShift
        rawAffineIdx()(
          //params.objAttrsMemIdxWidth - 1 downto 2
          //justCopy.myMemIdxWidth - 1
          //downto params.objAffineTileWidthRshift
          //(
          //  justCopy.myMemIdxWidth
          //  //+ affineObjXStartRawWidthPow + 1 - 1 - 1
          //  + params.objAffineTileWidthRshift - 1
          //)
          ////downto affineObjXStartRawWidthPow + 1 - 1
          //downto params.objAffineTileWidthRshift
          (
            //affineObjXStartRawWidthPow
            justCopy.myMemIdxWidth
            + tempAffineShift
            //+ 1 // account for double size rendering
            //params.objAffineSliceTileWidthPow
            - 1 
          )
          downto (
            //params.objAffineSliceTileWidthPow
            //+ 
            //params.objAffineTileWidthRshift + 1
            tempAffineShift
            //+ 1 // account for double size rendering
          )
        )
        //rawAffineIdx
      }
      def affineObjXStart() = {
        (
          //def tempShift = tempAffineShift
          Cat(
            //calcGridIdxLsb(
            //  if (!isAffine) {
            //    0
            //  } else {
            //    1
            //  }
            //),
            //rawAffineIdx()(
            //  params.objAffineTileWidthRshift
            //),
            rawAffineIdx()(
              //params.objAffineTileWidthRshift - 1 downto 0
              //params.objAffineTileWidthRshift downto 0
              //justCopy.myMemIdxWidth
              //(
              //  params.objAffineDblTileSize2dPow.x
              //  + params.objAffineAttrsMemIdxWidth
              //)
              //rawAffineIdx().high - 1 // account for 
              //(
              //  affineObjXStartRawPlus
              //  //params.objAffineSliceTileWidthPow
              //  + params.objAffineAttrsMemIdxWidth
              //)
              //downto params.objAffineAttrsMemIdxWidth
              (
                ////affineObjXStartRawWidthPow
                ////params.objAffineSliceTileWidthPow
                //1 // to account for double size rendering
                ////+ (
                ////  if (params.objAffineTileWidthRshift == 0) {
                ////    1 // for the extra cycle delay between pixels
                ////  } else {
                ////    params.objAffineTileWidthRshift
                ////  }
                ////)
                //+
                //tempAffineShift
                params.objAffineTileWidthRshift
                //params.objAffineSliceTileWidthPow
                + 1 // account for double size rendering
                + 1 // account for the extra cycle delay
                + 1 // account for grid index
                - 1 
              )
              //downto params.objAffineSliceTileWidthPow
              //downto params.objAffineTileWidthRshift + 1
              downto (
                //if (params.objAffineTileWidthRshift == 0) {
                //  1
                //} else {
                //  0
                //}
                //tempAffineShift
                //0
                //1 // account for double size rendering
                //params.objAffineTileWidthRshift
                + 1 // account for the extra cycle delay
                + 1 // account for grid index
              )
            ),
            B(
              params.objAffineSliceTileWidthPow bits,
              //params.objAffineTileWidthRshift bits,
              //params.objAffineTileWidthRshift bits,
              default -> False
            ),
          ).asUInt
          //(
          //  params.objAffineDblTileSize2dPow.x - 1 downto 0
          //  //params.objAffineSliceTileWidthPow - 1 downto 0
          //)
        )
      }
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

      //def objAttrsMemIdx() = innerObjAttrsMemIdx
      //def invObjAttrsMemIdx() = (
      //  //cnt
      //  //(
      //  //  (1 << params.objAttrsMemIdxWidth) - 1
      //  //) - 
      //  cnt(
      //    // support needing to do two writes into `objSubLineMemArr`
      //    params.objAttrsMemIdxWidth - 1 downto 0
      //    //params.objAttrsMemIdxWidth downto 1
      //  )
      //)
      // which iteration are we on for
      //val gridIdxLsb = Bool()
      def calcNonAffineGridIdxLsb() = (
        //cnt
        cnt(
          // support needing to do two writes into `objSubLineMemArr`
          //0
          //params.objAttrsMemIdxWidth + 1 - 1
          //params.objTileSize2dPow.x - 1
          1 // account for the extra cycle delay between pixels
        )
      )
      def calcAffineGridIdxLsb() = (
        cnt(
          //// this should be + 1 as compared to
          //// `calcNonAffineGridIdxLsb()`
          //// because we have divided the number of grid elements by 2
          //// for affine sprites
          ////// this should also be + 2 as well because we take four cycles
          ////// per grid element
          //params.objAttrsMemIdxWidth + 1 + 1 - 1
          //params.objAffineAttrsMemIdxTileCntWidth
          //params.objAffineAttrsMemIdxTileCntWidth - 1
          //+ 1
          //params.objAffineSliceTileWidth
          (
            //if (params.objAffineTileWidthRshift == 0) {
            //  1 // account for the extra cycle delay between pixels
            //} else {
            //  params.objAffineTileWidthRshift
            //}
            //params.objAffineTileWidthRshift
            //+ 1 // to account for double size rendering
            //params.objAffineTileWidthRshift
            //+ 
            1 // to account for the extra cycle delay
          )
        )
      )
      def calcGridIdxLsb(
        kind: Int
      ) = {
        if (kind == 0) {
          calcNonAffineGridIdxLsb()
        } else {
          calcAffineGridIdxLsb()
        }
      }

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
    case class WrObjPipePayloadStage2(isAffine: Boolean) extends Bundle {
      val objAttrs = Gpu2dObjAttrs(
        params=params,
        isAffine=isAffine,
      )
    }
    case class WrObjPipePayloadStage3(isAffine: Boolean) extends Bundle {
      def myTempObjTileWidth = tempObjTileWidth(isAffine)

      //// What are the `Gpu2dObjAttrs` of our sprite? 
      //val objAttrs = Gpu2dObjAttrs(
      //  params=params,
      //  isAffine=isAffine,
      //)

      val pxPos = Vec.fill(myTempObjTileWidth)(
        params.objPxsCoordT()
      )
      val myIdxPxPosX = Vec.fill(
        //myTempObjTileWidth
        tempObjTileWidth1(isAffine)
        //tempObjTileWidth2()
      )(
        SInt(params.objPxsCoordSize2dPow.x bits)
      )
      //val affinePxPos = (isAffine) generate Vec.fill(
      //  myTempObjTileWidth
      //)(
      //  params.objPxsCoordT()
      //)
    }
    case class WrObjPipePayloadStage4(isAffine: Boolean) extends Bundle {
      //--------
      // (a  b) × (x) = (ax+by)
      // (c  d)   (y)   (cx+dy)
      //--------
      //val affine = Gpu2dAffine(
      //  params=params,
      //  isObj=true,
      //)
      //def vecSize = 2
      //val mult = Vec.fill(vecSize)(
      //  Vec.fill(
      //    SInt()
      //  )
      //)
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      def fracWidth = Gpu2dAffine.fracWidth
      def multSize2dPow = Gpu2dAffine.multSize2dPow(
        params=params,
        isObj=true,
      )
      // X coordinate
      val multAX = (isAffine) generate Vec.fill(
        //params.objAffineDblTileSize2d.x
        //params.objAffineDblTileSize2d.x
        myTempObjTileWidth
      )(
        SInt(multSize2dPow.x bits)
      )
      val multBY = (isAffine) generate Vec.fill(
        //params.objAffineDblTileSize2d.x
        //params.objAffineDblTileSize2d.x
        //tempObjTileWidth()
        myTempObjTileWidth
      )(
        SInt(multSize2dPow.x bits)
      )
      // Y coordinate
      val multCX = (isAffine) generate Vec.fill(
        //params.objAffineDblTileSize2d.y
        myTempObjTileWidth
      )(
        SInt(multSize2dPow.y bits)
      )
      val multDY = (isAffine) generate Vec.fill(
        //params.objAffineDblTileSize2d.y
        myTempObjTileWidth
      )(
        SInt(multSize2dPow.y bits)
      )
      //--------
    }
    case class WrObjPipePayloadStage5(isAffine: Boolean) extends Bundle {
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      def fracWidth = Gpu2dAffine.fracWidth
      val dbgTestFxTilePxsCoord = (isAffine) generate Vec.fill(
        //params.objAffineDblTileSize2d.x
        myTempObjTileWidth
      )(
        DualTypeNumVec2(
          dataTypeX=SInt(
            (params.objAffineDblTileSize2dPow.x + 4 + fracWidth) bits
            //params.objTileSize2dPow.x bits
          ),
          dataTypeY=SInt(
            (params.objAffineDblTileSize2dPow.y + 4 + fracWidth) bits
            //params.objTileSize2dPow.y bits
          ),
        )
      )
    }
    case class WrObjPipePayloadStage6(isAffine: Boolean) extends Bundle {
      //--------
      // (a  b) × (x) = (ax+by)
      // (c  d)   (y)   (cx+dy)
      //--------
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      def fracWidth = Gpu2dAffine.fracWidth
      //val fxTilePxsCoord = DualTypeNumVec2(
      //  dataTypeX=
      //)
      val fxTilePxsCoord = (isAffine) generate Vec.fill(
        //params.objAffineDblTileSize2d.x
        myTempObjTileWidth
      )(
        DualTypeNumVec2(
          dataTypeX=SInt(
            (params.objAffineDblTileSize2dPow.x + 4 + fracWidth) bits
            //params.objTileSize2dPow.x bits
          ),
          dataTypeY=SInt(
            (params.objAffineDblTileSize2dPow.y + 4 + fracWidth) bits
            //params.objTileSize2dPow.y bits
          ),
        )
      )
      //--------
    }
    case class WrObjPipePayloadStage7(isAffine: Boolean) extends Bundle {
      //val tilePxsCoordYPipe1 = UInt(params.objTileSize2dPow.y bits)
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      //def myTempObjTilePxsCoordT() = tempObjTilePxsCoordT()
      val tilePxsCoord = Vec.fill(myTempObjTileWidth)(
        //myTempObjTilePxsCoordT()
        //params.objTilePxsCoordT()
        ObjTilePxsCoordT(isAffine)
      )
      val oorTilePxsCoord = (isAffine) generate Vec.fill(
        myTempObjTileWidth
      )(Vec2(Bool()))
      //val pxPos = Vec.fill(myTempObjTileWidth)(
      //  params.objPxsCoordT()
      //)
      val affineDoIt = (isAffine) generate Vec.fill(
        myTempObjTileWidth
      )(Bool())
      val objPosYShift = SInt(params.objPxsCoordSize2dPow.y bits)
      //val pxPosShiftTopLeft = params.objPxsCoordT()
      //val tile = Gpu2dTileFull(
      //  params=params,
      //  isObj=true,
      //  isAffine=isAffine,
      //)
      //val tileSlice = Gpu2dTileSlice(
      //  params=params,
      //  isObj=true,
      //  isAffine=isAffine,
      //)
    }
    case class WrObjPipePayloadStage8(isAffine: Boolean) extends Bundle {
      val tileSlice = (!isAffine) generate Gpu2dTileSlice(
        params=params,
        isObj=true,
        isAffine=isAffine,
      )
      val tilePx = (isAffine) generate Vec.fill(
        params.objAffineSliceTileWidth
      )(
        UInt(params.objPalEntryMemIdxWidth bits)
      )
      //val tilePxsCoord = params.objTilePxsCoordT()
      //val pxsCoordYRangeCheck = Bool()
      //val pxPosRangeCheck = Vec2(Bool())
      //val pxPosXGridStraddleVec = Vec.fill(
      //  params.objTileSize2d
      //)
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      def myTempObjTileWidthPow = tempObjTileWidthPow(isAffine)
      val affineDoIt = (isAffine) generate Vec.fill(
        myTempObjTileWidth
      )(Bool())
      val pxPosXGridIdx = (
        Vec.fill(myTempObjTileWidth)(
          UInt(
            (
              params.objPxsCoordSize2dPow.x
              - myTempObjTileWidthPow
              //- params.objAffineDblTileSize2dPow.x
              //- tempObjTileWidthPow1()
              //- tempObjTileWidthPow2()
            )
            bits
          )
        )
      )
      val pxPosXGridIdxLsb = (
        Vec.fill(myTempObjTileWidth)(
          //UInt(1 bits)
          Bool()
        )
      )
      val pxPosXGridIdxFindFirstSameAsFound = Bool()
      val pxPosXGridIdxFindFirstSameAsIdx = UInt(
        myTempObjTileWidthPow bits
      )
      val pxPosXGridIdxFindFirstDiffFound = Bool()
      val pxPosXGridIdxFindFirstDiffIdx = UInt(
        myTempObjTileWidthPow bits
      )
      val pxPosRangeCheckGePipe1 = Vec.fill(myTempObjTileWidth)(
        Vec2(Bool())
      )
      val pxPosRangeCheckLtPipe1 = Vec.fill(myTempObjTileWidth)(
        Vec2(Bool())
      )
      //val palEntryMemIdx = Vec.fill(myTempObjTileWidth)(
      //  UInt(params.objPalEntryMemIdxWidth bits)
      //)
      val tilePxsCoordPalEntryCmpPipe1 = (!isAffine) generate (
        Vec.fill(myTempObjTileWidth)(
          Vec2(Bool())
        )
      )
    }
    case class WrObjPipePayloadStage9(isAffine: Boolean) extends Bundle {
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      val palEntryMemIdx = Vec.fill(myTempObjTileWidth)(
        UInt(params.objPalEntryMemIdxWidth bits)
      )
    }
    case class WrObjPipePayloadStage10(isAffine: Boolean) extends Bundle {
      //val oldPxPosInLineCheckGePipe1 = Vec2(Bool())
      //val oldPxPosInLineCheckLtPipe1 = Vec2(Bool())
      //val pxPosXGridIdxFlip = Vec.fill(params.objTileSize2d.x)(Bool())
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      val pxPosXGridIdxMatches = Vec.fill(myTempObjTileWidth)(Bool())

      // Whether `palEntryMemIdx` is non-zero
      val palEntryNzMemIdx = Vec.fill(myTempObjTileWidth)(Bool())
      val pxPosRangeCheck = Vec.fill(myTempObjTileWidth)(
        Vec2(Bool())
      )

      //val oldPxPosInLineCheck = Vec2(Bool())
    }
    case class WrObjPipePayloadStage12(
      isAffine: Boolean
    ) extends Bundle {
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      val palEntry = Vec.fill(myTempObjTileWidth)(
        Gpu2dPalEntry(params=params)
      )
      val pxPosInLine = Vec.fill(myTempObjTileWidth)(Bool())
      val pxPosCmpForOverwrite = Vec.fill(myTempObjTileWidth)(
        Bool()
      )
    }
    case class WrObjPipePayloadStage13(
      isAffine: Boolean
    ) extends Bundle {
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      def myTempObjTileWidthPow = tempObjTileWidthPow(isAffine)
      //def myTempObjTileWidth2 = tempObjTileWidth2()
      //def myTempObjTileWidthPow2 = tempObjTileWidthPow2()
      val myIdxV2d = Vec.fill(
        //myTempObjTileWidth
        tempObjTileWidth1(isAffine)
      )(
        Vec.fill(numMyIdxVecs(isAffine))(
          UInt(
            //myTempObjTileWidthPow bits
            tempObjTileWidthPow1(isAffine) bits
          )
        )
      )
      val pxPosYLsb = Bool()
      //val pxPosXGridIdxLsb = Bool()
      val pxPosXGridIdx = UInt(
        (
          params.objPxsCoordSize2dPow.x
          - myTempObjTileWidthPow
          //- params.objAffineDblTileSize2dPow.x
        )
        bits
      )
    }
    case class WrObjPipePayloadStage14(
      isAffine: Boolean
    ) extends Bundle {
      def myTempObjTileWidth1 = tempObjTileWidth1(isAffine)
      def myTempObjTileWidthPow1 = tempObjTileWidthPow1(isAffine)
      //def myTempObjTileWidthPow = tempObjTileWidthPow()
      //def myTempObjTileWidth2 = tempObjTileWidth2()
      //def myTempObjTileWidthPow2 = tempObjTileWidthPow2()
      val myIdxV2d = Vec.fill(
        myTempObjTileWidth1
      )(
        Vec.fill(numMyIdxVecs(isAffine))(
          UInt(
            myTempObjTileWidthPow1 bits
            //myTempObjTileWidthPow bits
          )
        )
      )
    }
    case class WrObjPipePayloadStage15(
      isAffine: Boolean
    ) extends Bundle {
      // "rd..." here means it's been read from `objSubLineMemArr`
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      def myTempObjTileWidth1 = tempObjTileWidth1(isAffine)
      //def myTempObjTileWidth2 = tempObjTileWidth2()
      val rdSubLineMemEntry = Vec.fill(
        //params.objAffineDblTileSize2d.x
        myTempObjTileWidth
      )(
        ObjSubLineMemEntry()
      )
      //def numFwd = wrObjPipeStage12NumFwd
      //val fwdV2d = Vec.fill(
      //  //myTempObjTileWidth
      //  //params.objAffineDblTileSize2d.x
      //  myTempObjTileWidth2
      //)(
      //  Vec.fill(numFwd)(WrObjPipeStage12Fwd(isAffine))
      //)
      //val doFwd = Vec.fill(
      //  //myTempObjTileWidth
      //  //params.objAffineDblTileSize2d.x
      //  myTempObjTileWidth2
      //)(
      //  Vec.fill(numFwd - 1)(Bool())
      //)
      val inMainVec = (
        Vec.fill(
          //myTempObjTileWidth
          myTempObjTileWidth1
        )(
          Bool()
        )
      )
    }
    case class WrObjPipePayloadStage16(
      isAffine: Boolean,
    ) extends Bundle {
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      def myTempObjTileWidth1 = tempObjTileWidth1(isAffine)
      val tempRdLineMemEntry = Vec.fill(
        myTempObjTileWidth
      )(
        ObjSubLineMemEntry()
      )
      val inMainVec = (
        Vec.fill(
          //myTempObjTileWidth
          myTempObjTileWidth1
        )(
          Bool()
        )
      )
      val ext = WrObjPipe14Ext(
        isAffine=isAffine,
        useVec=true,
      )
      //val affineInMainVec = (
      //  Vec.fill(
      //    //myTempObjTileWidth
      //    myTempObjTileWidth1
      //  )(
      //    Bool()
      //  )
      //)
      //.setName(f"wrObjPipe15_tempRdLineMemEntry_$kind" + f"_$x")
    }
    case class WrObjPipePayloadPostStage0(
      isAffine: Boolean
    ) extends Bundle {
      val stage2 = WrObjPipePayloadStage2(isAffine=isAffine)
      val stage3 = WrObjPipePayloadStage3(isAffine=isAffine)
      val stage4 = WrObjPipePayloadStage4(isAffine=isAffine)
      val stage5 = WrObjPipePayloadStage5(isAffine=isAffine)
      val stage6 = WrObjPipePayloadStage6(isAffine=isAffine)
      val stage7 = WrObjPipePayloadStage7(isAffine=isAffine)
      val stage8 = WrObjPipePayloadStage8(isAffine=isAffine)
      val stage9 = WrObjPipePayloadStage9(isAffine=isAffine)
      val stage10 = WrObjPipePayloadStage10(isAffine=isAffine)
      val stage12 = WrObjPipePayloadStage12(isAffine=isAffine)
      //val stage6 = WrObjPipePayloadStage6()
      //val stage7 = WrObjPipePayloadStage7()
      val stage13 = WrObjPipePayloadStage13(isAffine=isAffine)
      val stage14 = WrObjPipePayloadStage14(isAffine=isAffine)
      val stage15 = WrObjPipePayloadStage15(isAffine=isAffine)
      val stage16 = WrObjPipePayloadStage16(isAffine=isAffine)
      //val stage15 = WrObjPipePayloadStage15()
      //val stage7 = WrObjPipePayloadStage7()
    }
    case class WrObjPipePayload(
      isAffine: Boolean
    )
      extends Bundle 
      with PipeMemRmwPayloadBase[
        Vec[ObjSubLineMemEntry],
        WrObjPipeSlmRmwHazardCmp
      ]
    {
      val subLineMemEntryExt = PipeMemRmwPayloadExt(
        wordType=(
          Vec.fill(params.tempObjTileWidth(isAffine=isAffine))(
            ObjSubLineMemEntry()
          )
        ),
        wordCount=(
          if (!isAffine) (
            params.objSubLineMemArrSize,
          ) else (
            params.objAffineSubLineMemArrSize
          )
        ),
        hazardCmpType=WrObjPipeSlmRmwHazardCmp(isAffine=isAffine),
        modStageCnt=wrObjPipeIdxSlmRmwModStageCnt,
        optEnableModDuplicate=true,
      )
      def setPipeMemRmwExt(
        inpExt: PipeMemRmwPayloadExt[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ],
        memArrIdx: Int,
      ): Unit = {
        subLineMemEntryExt := inpExt
      }
      def getPipeMemRmwExt(
        outpExt: PipeMemRmwPayloadExt[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ],
        memArrIdx: Int,
      ): Unit = {
        outpExt := subLineMemEntryExt
      }
      //def tempObjTileSize2dPow(isAffine: Boolean) = (
      //  if (!isAffine) {
      //    params.objTileSize2dPow
      //  } else {
      //    //params.objAffineDblTileSize2dPow
      //    ElabVec2[Int](
      //      x=params.objAffineSliceTileWidthPow,
      //      y=params.objAffineDblTileSize2dPow.y,
      //    )
      //  }
      //)
      //def tempObjTileSize2d(isAffine: Boolean) = (
      //  if (!isAffine) {
      //    params.objTileSize2d
      //  } else {
      //    //params.objAffineDblTileSize2d
      //    ElabVec2[Int](
      //      x=1 << params.objAffineSliceTileWidthPow,
      //      y=params.objAffineDblTileSize2d.y,
      //    )
      //  }
      //)
      //def tempObjTileWidthPow() = (
      //  if (!isAffine) {
      //    params.objTileSize2dPow.x
      //  } else {
      //    //params.objAffineDblTileSize2dPow
      //    //ElabVec2[Int](
      //    //  x=params.objAffineSliceTileWidthPow,
      //    //  y=params.objAffineDblTileSize2dPow.y,
      //    //)
      //    //params.objAffineSliceTileWidthPow
      //    params.myDbgObjAffineTileWidthPow
      //  }
      //)
      //def tempObjTileWidth() = (
      //  if (!isAffine) {
      //    params.objTileSize2d.x
      //  } else {
      //    //params.objAffineDblTileSize2d
      //    //params.objAffineSliceTileWidth
      //    params.myDbgObjAffineTileWidth
      //  }
      //)

      //def tempObjTileWidthPow2() = (
      //  if (!isAffine) {
      //    params.objTileSize2dPow.x
      //  } else {
      //    params.objAffineDblTileSize2dPow.x
      //    //ElabVec2[Int](
      //    //  x=params.objAffineSliceTileWidthPow,
      //    //  y=params.objAffineDblTileSize2dPow.y,
      //    //)
      //    //params.objAffineSliceTileWidthPow
      //    //params.myDbgObjAffineTileWidthPow
      //  }
      //)
      //def tempObjTileWidth2() = (
      //  if (!isAffine) {
      //    params.objTileSize2d.x
      //  } else {
      //    params.objAffineDblTileSize2d.x
      //    //params.objAffineSliceTileWidth
      //    //params.myDbgObjAffineTileWidth
      //  }
      //)

      //def tempObjTileHeight() = (
      //  if (!isAffine) {
      //    params.objTileSize2d.y
      //  } else {
      //    params.objAffineDblTileSize2d.y
      //  }
      //)
      //def TilePxsCoordT() = (
      //  if (!isAffine) {
      //    params.objTilePxsCoordT()
      //  } else {
      //    params.objAffineTilePxsCoordT()
      //  }
      //)


      //def tempObjTilePxsCoordT(isAffine: Boolean) = (
      //  if (!isAffine) {
      //    params.objTilePxsCoordT()
      //  } else {
      //    params.objAffineTilePxsCoordT()
      //  }
      //)

      //def objAttrsMemIdxMinus1 = stage0.objAttrsMemIdxMinus1
      //def objAttrsMemIdxWillUnderflow() = (
      //  stage0.objAttrsMemIdxWillUnderflow()
      //)




      // This is similar to a strictly in-order CPU pipeline's
      // ALU operand forwarding
      //def wrObjPipeStage5NumFwd
      //def stage7NumFwd = 2
      //case class WrObjPipePayloadStage15() extends Bundle {
      //  //val overwriteLineMemEntry = Vec.fill(params.objTileSize2d.x)(
      //  //  Bool()
      //  //)
      //  ////def numFwd = stage6NumFwd
      //  //val wrLineMemEntry = Vec.fill(params.objTileSize2d.x)(
      //  //  ObjSubLineMemEntry()
      //  //)
      //  val ext = WrObjPipe14Ext(
      //    isAffine=isAffine,
      //    useVec=true,
      //  )

      //  //def numFwd = wrObjPipeStage14NumFwd
      //  //val fwdV2d = Vec.fill(
      //  //  //params.objTileSize2d.x
      //  //  //tempObjTileWidth()
      //  //  tempObjTileWidth2()
      //  //  //params.objAffineDblTileSize2d.x
      //  //)(
      //  //  Vec.fill(numFwd)(WrObjPipeStage14Fwd(isAffine=isAffine))
      //  //)
      //  //val fwdWrLineMemEntryV2d = Vec.fill(numFwd)(
      //  //  Vec.fill(params.objTileSize2d.x)(
      //  //    ObjSubLineMemEntry()
      //  //  )
      //  //)
      //  //val savedWrLineMemEntryVec = Vec.fill(numFwd)(ObjSubLineMemEntry())

      //  //def numSaved = 3
      //  //val savedWrLineMemEntryVec = Vec.fill(numSaved)(ObjSubLineMemEntry())
      //}
      //case class WrObjPipePayloadStage14() extends Bundle {
      //  val haveAnyWritten = (isAffine) generate Bool()
      //}
      //case class WrObjPipePayloadStage7() extends Bundle {
      //  //val ext = WrObjPipeOut6ExtData()
      //  val ext = WrObjPipe6Ext()
      //}

      val stage0 = WrObjPipePayloadStage0(isAffine=isAffine)
      //val nonAffineStage0 = WrObjPipePayloadStage0()
      //val affineStage0 = WrObjPipePayloadStage0()
      //def stage0(
      //  kind: Int
      //): WrObjPipePayloadStage0 = {
      //  if (kind == 0) {
      //    nonAffineStage0
      //  } else {
      //    affineStage0
      //  }
      //}
      //def lineMemArrIdx = stage0.lineMemArrIdx
      def lineNum = stage0.lineNum
      def fullLineNum = stage0.fullLineNum
      def cnt = stage0.cnt
      def bakCnt = stage0.bakCnt
      def bakCntMinus1 = stage0.bakCntMinus1
      def objAttrsMemIdx = stage0.objAttrsMemIdx()
      //def getBakCntTilePxsCoordX() = stage0.getBakCntTilePxsCoordX()
      //def getCntTilePxsCoordX() = stage0.getCntTilePxsCoordX()
      def bakCntWillBeDone = stage0.bakCntWillBeDone()
      //def gridIdxLsb = stage0.gridIdxLsb
      def objXStart() = stage0.objXStart()
      def affineObjXStart() = stage0.affineObjXStart()

      val postStage0 = WrObjPipePayloadPostStage0(isAffine=isAffine)
      //val affine = PostStage0(
      //  isAffine=true
      //)
      //def getPostStage0(
      //  kind: Int
      //): PostStage0 = {
      //  if (kind == 0) {
      //    postStage0
      //  } else {
      //    affine
      //  }
      //}

      def stage2 = postStage0.stage2
      def objAttrs = stage2.objAttrs

      def stage3 = postStage0.stage3
      def pxPos = stage3.pxPos
      def myIdxPxPosX = stage3.myIdxPxPosX
      //def affinePxPos = stage3(kind=1).affinePxPos

      def stage4 = postStage0.stage4

      def stage5 = postStage0.stage5
      def stage6 = postStage0.stage6

      def stage7 = postStage0.stage7
      //def tilePxsCoordYPipe1 = stage5.tilePxsCoordYPipe1
      //def pxsCoordYRangeCheckPipe1 = stage5.pxsCoordYRangeCheckPipe1
      def tilePxsCoord = stage7.tilePxsCoord
      //def pxPosRangeCheckPipe1 = stage5.pxPosRangeCheckPipe1

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
        kind: Int,
        x: UInt,
      ): UInt = {
        def myPxPosX = pxPos(x).x.asUInt
        if (kind == 0) {
          params.getObjSubLineMemArrIdx(
            addr=myPxPosX
          )
        } else {
          params.getObjAffineSubLineMemArrIdx(
            addr=myPxPosX
          )
        }
      }
      //def getObjSubLineMemArrGridIdx(
      //  kind: Int,
      //  x: UInt,
      //): Bool = {
      //  //params.getObjSubLineMemArrGridIdx(
      //  //  addr=pxPos(x).x.asUInt
      //  //)
      //  def myPxPosX = pxPos(x).x.asUInt
      //  if (kind == 0) {
      //    params.getObjSubLineMemArrGridIdx(
      //      addr=myPxPosX
      //    )
      //  } else {
      //    params.getObjAffineSubLineMemArrGridIdx(
      //      addr=myPxPosX
      //    )
      //  }
      //}
      def getObjSubLineMemArrElemIdx(
        kind: Int,
        x: UInt,
      ): UInt = {
        //params.getObjSubLineMemArrElemIdx(
        //  addr=pxPos(x).x.asUInt
        //)
        def myPxPosX = pxPos(x).x.asUInt
        if (kind == 0) {
          params.getObjSubLineMemArrElemIdx(
            addr=myPxPosX
          )
        } else {
          params.getObjAffineSubLineMemArrElemIdx(
            addr=myPxPosX
          )
        }
      }
      //def pxPosYMinusTileHeightMinus1 = stage2.pxPosYMinusTileHeightMinus1
      //def pxPosShiftTopLeft = stage2.pxPosShiftTopLeft
      def objPosYShift = stage7.objPosYShift
      //def tileSlice = stage5.tileSlice

      def stage8 = postStage0.stage8
      def tileSlice = stage8.tileSlice
      def tilePx = stage8.tilePx
      def pxPosXGridIdx = stage8.pxPosXGridIdx
      def pxPosXGridIdxLsb = stage8.pxPosXGridIdxLsb
      def pxPosXGridIdxFindFirstSameAsFound = (
        stage8.pxPosXGridIdxFindFirstSameAsFound
      )
      def pxPosXGridIdxFindFirstSameAsIdx = (
        stage8.pxPosXGridIdxFindFirstSameAsIdx
      )
      def pxPosXGridIdxFindFirstDiffIdx = (
        stage8.pxPosXGridIdxFindFirstDiffIdx
      )
      def pxPosXGridIdxFindFirstDiffFound = (
        stage8.pxPosXGridIdxFindFirstDiffFound
      )
      //def pxPosRangeCheck = stage6.pxPosRangeCheck
      //def tilePxsCoord = stage6.tilePxsCoord
      def pxPosRangeCheckGePipe1 = (
        stage8.pxPosRangeCheckGePipe1
      )
      def pxPosRangeCheckLtPipe1 = (
        stage8.pxPosRangeCheckLtPipe1
      )
      def stage9 = postStage0.stage9
      def palEntryMemIdx = stage9.palEntryMemIdx

      def stage10 = postStage0.stage10
      //def pxPosXGridIdxFlip = stage7.pxPosXGridIdxFlip
      def pxPosXGridIdxMatches = (
        stage10.pxPosXGridIdxMatches
      )
      //def oldPxPosInLineCheckGePipe1 = stage7.oldPxPosInLineCheckGePipe1
      //def oldPxPosInLineCheckLtPipe1 = stage7.oldPxPosInLineCheckLtPipe1
      //def palEntryMemIdx = stage7.palEntryMemIdx
      def palEntryNzMemIdx = stage10.palEntryNzMemIdx
      def pxPosRangeCheck = stage10.pxPosRangeCheck

      def stage12 = postStage0.stage12
      //def palEntryNzMemIdx = stage9.palEntryNzMemIdx
      //def oldPxPosInLineCheck = stage9.oldPxPosInLineCheck
      def palEntry = stage12.palEntry
      //def pxPosXConcat = stage9.pxPosXConcat
      //def pxPosConcat = stage9.pxPosConcat
      def pxPosInLine = stage12.pxPosInLine
      def pxPosCmpForOverwrite = (
        stage12.pxPosCmpForOverwrite
      )
      //def pxPosXChangingGridIdx = stage9.pxPosXChangingGridIdx
      //--------
      //def rdSubLineMemEntry = stage5.rdSubLineMemEntry
      //--------
      //def dbgRdLineMemEntry = stage5.dbgRdLineMemEntry

      //def stage6 = postStage0.stage6
      def stage13 = postStage0.stage13
      def stage14 = postStage0.stage14

      def stage15 = postStage0.stage15
      def rdSubLineMemEntry = stage15.rdSubLineMemEntry


      //def stage14 = postStage0.stage14

      def stage16 = postStage0.stage16
      def overwriteLineMemEntry = (
        stage16.ext.overwriteLineMemEntry
      )
      def wrLineMemEntry = stage16.ext.wrLineMemEntry

      //def stage14 = postStage0.stage14
      //def haveAnyWritten = stage14.haveAnyWritten
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
      //kind: Int,
      isAffine: Boolean,
      firstInit: Boolean,
    ): WrObjPipePayload = {
      val ret = WrObjPipePayload(isAffine)
      ////ret.lineMemArrIdx := rWrLineMemArrIdx
      //ret.lineMemArrIdx := prevLineMemArrIdx + 1
      //ret.lineNum := prevLineNum + 1

      //ret.lineNum := rGlobWrObjLineNumPipe1(ret.lineNum.bitsRange)
      //for (kind <- 0 until 2) {
        if (firstInit) {
          //ret.lineNum := wrLineNumInit
          //ret.lineNum := wrObjLineNumInit
          ret.fullLineNum := wrObjFullLineNumInit
        } else {
          //ret.lineNum := rGlobWrObjLineNumPipe1(
          //  ret.lineNum.bitsRange
          //)
          ret.fullLineNum := wrObjGlobPipe.rLineNumPipe1/*rGlobWrObjFullLineNumPipe1*/(
            ret.fullLineNum.bitsRange
          )
        }
        //ret.lineNum := prevLineNum + 1
        //ret.lineNum := prevLineNum + U(f"$wrLineNumWidth'd1")
        ret.cnt := 0
        ret.bakCnt := wrObjPipeBakCntStart(isAffine)
        ret.bakCntMinus1 := wrObjPipeBakCntStart(isAffine) - 1
        ret.stage0.rawAffineIdx() := (
          if (params.fancyObjPrio) {
            //(1 << params.objAttrsMemIdxWidth) - 1
            (1 << params.objAffineAttrsMemIdxTileCntWidth) - 1
          } else {
            0
          }
        )
        ret.stage0.justCopy.dbgTestIdx := (
          if (params.fancyObjPrio) {
            //(1 << params.objAttrsMemIdxWidth) - 1
            (
              1 
              << (
                //params.objAttrsMemIdxWidth + 1
                //params.objAttrsMemIdxWidth + 3
                //params.objAttrsMemIdxWidth + 2
                params.objAttrsMemIdxTileCntWidth
              )
            ) - 1
          } else {
            0
          }
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
        ret.subLineMemEntryExt := ret.subLineMemEntryExt.getZero
      //}
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
      //log2Up(params.physFbSize2d.x + 1) + 2
    )
    def combinePipeFullCntWidth = (
      //log2Up(params.physFbSize2d.x + 1) + 2
      //combinePipeCntWidth
      log2Up(params.intnlFbSize2d.x)
      + log2Up(params.physFbSize2dScale.x)
    )
    def combinePipeCntRange = (
      ////combinePipeFullCntWidth + log2Up(params.physFbSize2dScale.x) - 1
      combinePipeFullCntWidth - 1
      downto log2Up(params.physFbSize2dScale.x)
      //downto 0
    )
    //println(
    //  "cnt stuff: "
    //  + s"${params.intnlFbSize2d.x} ${params.physFbSize2d.x} "
    //  + s"${combinePipeCntWidth} ${combinePipeFullCntWidth} "
    //  + s"${combinePipeCntRange}"
    //)
    //def combinePipeBakCntStart = (
    //  params.intnlFbSize2d.x - 1
    //  //params.physFbSize2d.x - 1
    //)
    def combinePipeFullBakCntStart = (
      //params.intnlFbSize2d.x - 1
      //params.physFbSize2d.x - 1
      (
        params.intnlFbSize2d.x
        * (1 << log2Up(params.physFbSize2dScale.x))
        //(1 << combinePipeFullCntWidth) - 1
      ) - 1
      //params.physFbSize2d.x - params.
    )
    case class CombinePipeOut3Ext() extends Bundle {
      val bgRdSubLineMemEntry = BgSubLineMemEntry()
      val objRdSubLineMemEntry = ObjSubLineMemEntry()
      val objAffineRdSubLineMemEntry = (!noAffineObjs) generate (
        ObjSubLineMemEntry()
      )
    }
    //val rCombinePipeOut1Ext = Reg(CombinePipeOut1Ext())
    //rCombinePipeOut1Ext.init(rCombinePipeOut1Ext.getZero)
    def combinePipeNumMainStages = (
      //5
      //6
      //7
      //8
      9 // old, working value
      + params.physFbSize2dScale.x
      //10
      //10
    )
    //def combinePipeNumMainStages = wrBgObjPipeNumStages + 5
    case class CombinePipePayload(
      //haveBgExt: Boolean=false,
      //haveObjExt: Boolean=false,
    ) extends Bundle
      //with PipeMemRmwPayloadBase[Vec[BgSubLineMemEntry]]
      //with PipeMemRmwPayloadBase[Vec[ObjSubLineMemEntry]]
    {
      //val bgExt = PipeMemRmwPayloadExt(
      //  wordType=Vec.fill(params.bgTileSize2d.x)(BgSubLineMemEntry()),
      //  wordCount=params.bgSubLineMemArrSize,
      //  modStageCnt=1,
      //  optEnableModDuplicate=false,
      //)
      //def setPipeMemRmwExt(
      //  inpExt: PipeMemRmwPayloadExt[Vec[BgSubLineMemEntry]],
      //  memArrIdx: Int
      //): Unit = {
      //  bgExt := inpExt
      //}
      //def getPipeMemRmwExt(
      //  outpExt: PipeMemRmwPayloadExt[Vec[BgSubLineMemEntry]],
      //  memArrIdx: Int
      //): Unit = {
      //  outpExt := bgExt
      //}
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
        //val cnt = UInt(combinePipeCntWidth bits)
        //val bakCnt = UInt(combinePipeCntWidth bits)
        //val bakCntMinus1 = UInt(combinePipeCntWidth bits)
        //val fracCnt = SInt(
        //  (log2Up(params.physFbSize2dScale.x) + 3) bits
        //)
        def cnt = (
          fullCnt(
            //combinePipeCntWidth - 1 downto 0
            combinePipeCntRange
          )
        )
        def bakCnt = (
          fullBakCnt(combinePipeCntRange)
        )
        //def bakCntMinus1 = (
        //  fullBakCntMinus1(combinePipeCntRange)
        //)

        val fullCnt = UInt(combinePipeFullCntWidth bits)
        val fullBakCnt = UInt(combinePipeFullCntWidth bits)
        //val fullBakCntMinus1 = UInt(combinePipeFullCntWidth bits)

        //val scaleXCnt = (
        //  SInt(log2Up(params.physFbSize2dScale.x) + 2 bits)
        //)
        //--------
        def bakCntWillBeDone() = (
          //bakCntMinus1.msb
          //bakCntMinus1 === 0
          bakCnt === 0
          //bakCnt.msb
          //cnt + 1 === params.oneLineMemSize
        )
        def fullBakCntWillBeDone() = {
          //fullBakCntMinus1.msb
          //fullBakCntMinus1 === 0
          def myPow = log2Up(params.physFbSize2dScale.x)
          (
            if (params.physFbSize2dScale.x == 1) (
              fullBakCnt === 0
            ) else (
              (
                fullBakCnt(fullBakCnt.high downto myPow) === 0
              ) && (
                //fullBakCnt(myPow - 1 downto 0)
                //=== (1 << myPow) - params.physFbSize2dScale.x
                fullCnt(myPow - 1 downto 0)
                === params.physFbSize2dScale.x - 1
              )
            )
          ) 
          //&& (
          //  //(fracCnt - 1).msb
          //  fracCnt.msb
          //)
          //{
          //  val myPow = log2Up(params.physFbSize2dScale.x)
          //  (
          //    bakCnt === 0
          //    && (
          //      //fullBakCnt(myPow - 1 downto 0)
          //      //=== (
          //      //  (1 << myPow) - params.physFbSize2dScale.x - 1
          //      //)
          //      //(1 << myPow) - fullBakCnt(myPow - 1 downto 0) - 1
          //      //=== params.physFbSize2dScale.x
          //      fullCnt(myPow - 1 downto 0)
          //      === params.physFbSize2dScale.x - 1
          //    )
          //  )
          //}
          //fullBakCnt.msb
          //cnt + 1 === params.oneLineMemSize
        }
        //--------
      }
      val stage0 = Stage0()
      def changingRow = stage0.changingRow
      //def rdLineMemArrIdx = stage0.rdLineMemArrIdx
      //def wrLineMemArrIdx = stage0.wrLineMemArrIdx
      //def lineMemArrIdx = stage0.lineMemArrIdx
      //def fracCnt = stage0.fracCnt
      def cnt = stage0.cnt
      def lineMemIdx = stage0.cnt
      def bakCnt = stage0.bakCnt
      //def bakCntMinus1 = stage0.bakCntMinus1
      def fullCnt = stage0.fullCnt
      def fullBakCnt = stage0.fullBakCnt
      //def fullBakCntMinus1 = stage0.fullBakCntMinus1
      def bakCntWillBeDone() = stage0.bakCntWillBeDone()
      def fullBakCntWillBeDone() = stage0.fullBakCntWillBeDone()

      case class Stage2() extends Bundle {
        //val bgRdLineMemEntry = BgSubLineMemEntry()
        //val objRdLineMemEntry = ObjSubLineMemEntry()
        //val ext = CombinePipeOut1Ext()
        //val rdColorMath = ColorMathSubLineMemEntry()
        val rdBg = Vec.fill(params.bgTileSize2d.x)(
          BgSubLineMemEntry()
        )
        val rdObj = Vec.fill(
          //params.objTileSize2d.x
          params.objSliceTileWidth
        )(
          ObjSubLineMemEntry()
        )
        val rdObjAffine = (!noAffineObjs) generate Vec.fill(
          params.objAffineSliceTileWidth
          //params.objAffineTileSize2d.x
          //params.objAffineDblTileSize2d.x
          //params.objAffineSliceTileWidth
          //params.myDbgObjAffineTileWidth
        )(
          ObjSubLineMemEntry()
        )
      }
      case class Stage3() extends Bundle {
        val ext = CombinePipeOut3Ext()
      }
      //case class Stage3() extends Bundle {
      //}
      case class Stage4() extends Bundle {
        val objPickSubLineMemEntry = ObjSubLineMemEntry()
      }
      case class Stage5() extends Bundle {
        val objHiPrio = Bool()
        //val ext = CombinePipeOut3Ext()
      }
      case class Stage6() extends Bundle {
        //val isObj = Bool()
        //val col = Rgb(params.rgbConfig)
        val col = Gpu2dRgba(params=params)
        val colorMathInfo = (!noColorMath) generate (
          Gpu2dColorMathInfo(params=params)
        )
        val colorMathCol = (!noColorMath) generate (
          Gpu2dRgba(params=params)
        )
      }
      case class Stage7() extends Bundle {
        val combineWrLineMemEntry = BgSubLineMemEntry()
        //val objWrLineMemEntry = ObjSubLineMemEntry()
      }
      case class PostStage0() extends Bundle {
        val stage2 = Stage2()
        val stage3 = Stage3()
        val stage4 = Stage4()
        //val stage3 = Stage3()
        val stage5 = Stage5()
        val stage6 = Stage6()
        val stage7 = Stage7()
        val stage8 = Stage7()
      }
      val postStage0 = PostStage0()

      def stage2 = postStage0.stage2
      //def lineMemIdx = stage1.lineMemIdx
      //def bgRdLineMemEntry = stage1.bgRdLineMemEntry
      //def objRdLineMemEntry = stage1.objRdLineMemEntry
      def stage3 = postStage0.stage3
      //def stage3 = postStage0.stage3

      def stage4 = postStage0.stage4
      def objPickSubLineMemEntry = stage4.objPickSubLineMemEntry

      def stage5 = postStage0.stage5
      def objHiPrio = stage5.objHiPrio
      //def bgRdLineMemEntry = stage5.ext.bgRdLineMemEntry
      //def objRdLineMemEntry = stage5.ext.objRdLineMemEntry

      def stage6 = postStage0.stage6
      def col = stage6.col
      def colorMathInfo = (!noColorMath) generate stage6.colorMathInfo
      def colorMathCol = (!noColorMath) generate stage6.colorMathCol

      def stage7 = postStage0.stage7
      //def combineWrLineMemEntry = stage7.combineWrLineMemEntry

      def stage8 = postStage0.stage8
      def combineWrLineMemEntry = stage8.combineWrLineMemEntry
      //--------
    }
    def doInitCombinePipePayload(
      changingRow: Bool,
      //prevRdLineMemArrIdx: UInt,
      //prevWrLineMemArrIdx: UInt,
      //prevLineMemArrIdx: UInt,
    ): CombinePipePayload = {
      val ret = CombinePipePayload()
      //ret := ret.getZero
      //ret.allowOverride
      ret.changingRow := changingRow
      ////ret.rdLineMemArrIdx := rCombineRdLineMemArrIdx
      ////ret.wrLineMemArrIdx := rCombineWrLineMemArrIdx
      //ret.rdLineMemArrIdx := prevRdLineMemArrIdx + 1
      //ret.wrLineMemArrIdx := prevWrLineMemArrIdx + 1
      //ret.lineMemArrIdx := prevLineMemArrIdx + 1
      //ret.stage0.scaleXCnt := params.physFbSize2dScale.x - 2
      //ret.fracCnt := (
      //  params.physFbSize2dScale.x - 2
      //)
      ret.fullCnt := (
        //(default -> True)
        0
      )
      ret.fullBakCnt := combinePipeFullBakCntStart
      //ret.fullBakCntMinus1 := combinePipeFullBakCntStart - 1
      ret.postStage0 := ret.postStage0.getZero
      //ret.bgExt := ret.bgExt.getZero
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
    //--------
    val combinePipeOverflow = Bool()
    //val rPastCombinePipeOverflow = RegNext(combinePipeOverflow)
    //  .init(False)
    //val combinePipeOverflowState = Reg(Bool()) init(False)

    //def wrPipeSize = wrBgPipeSize + wrObjPipeSize

    //val wrBgPipeIn = KeepAttribute(
    //  //Vec.fill(wrBgObjPipeNumStages)(Reg(WrBgPipePayload()))
    //  Vec.fill(
    //    wrBgObjPipeNumStages
    //    //anyPipeNumStages
    //  )(
    //    //Flow(WrBgPipePayload())
    //    Flow(WrBgPipePayload())
    //  )
    //)
    //val wrBgPipeOut = KeepAttribute(
    //  //Vec.fill(wrBgObjPipeNumStages)(Reg(WrBgPipePayload()))
    //  Vec.fill(
    //    wrBgObjPipeNumStages
    //    //anyPipeNumStages
    //  )(
    //    //Flow(WrBgPipePayload())
    //    Flow(WrBgPipePayload())
    //  )
    //)
    //val nWrBgArr = Array.fill(wrBgObjPipeNumStages + 1)(Node())
    ////intnlChangingRowRe := RegNextWhen(
    ////  //rIntnlChangingRow && !rPastIntnlChangingRow,
    ////  rIntnlChangingRow, //&& !rPastIntnlChangingRow,
    ////  nWrBgArr(0).isFiring,
    ////) init(True)
    ////intnlChangingRowRe := (
    ////  rIntnlChangingRow && !rPastIntnlChangingRow
    ////)
    //val sWrBgArr = new ArrayBuffer[StageLink]()
    //val cWrBgArr = new ArrayBuffer[CtrlLink]()
    val wrBgPipe = PipeHelper(linkArr=linkArr)
    def nWrBgArr = wrBgPipe.nArr
    def sWrBgArr = wrBgPipe.sArr
    def s2mWrBgArr = wrBgPipe.s2mArr
    def cWrBgArr = wrBgPipe.cArr
    for (
      //idx <- 0 until nWrBgArr.size - 1
      idx <- 0 until wrBgObjPipeNumStages + 1
    ) {
      wrBgPipe.addStage(
        name=(s"WrBgPipe_$idx"),
        optIncludeS2M=false,
      )
      //sWrBgArr += StageLink(
      //  up=nWrBgArr(idx),
      //  down=Node(),
      //)
      //linkArr += sWrBgArr.last

      //cWrBgArr += CtrlLink(
      //  up=sWrBgArr.last.down,
      //  down=nWrBgArr(idx + 1),
      //)
      //linkArr += cWrBgArr.last
    }
    wrBgPipe.addStage(
      name="WrBgPipe_Last",
      optIncludeS2M=false,
      finish=true,
    )
    val nWrBgPipeLast = nWrBgArr.last
    //val wrBgPipePayloadArr = Array.fill(nWrBgArr.size)(
    //  Payload(WrBgPipePayload())
    //)
    val wrBgPipePayload = Payload(WrBgPipePayload())
    def wrBgPipeLast = nWrBgPipeLast(
      //wrBgPipePayloadArr.last
      wrBgPipePayload
    )
    def initTempWrBgPipeOut(
      idx: Int,
      //tempOutp: WrBgPipePayload,
    ): (WrBgPipePayload, WrBgPipePayload) = {
      // This function returns `(tempInp, tempOutp)`
      val pipeIn = (
        //cWrBgArr(idx).down(wrBgPipePayload)
        cWrBgArr(idx).up(wrBgPipePayload)
        //nWrBgArr(idx)(wrBgPipePayload)
      )
      val pipeOut = WrBgPipePayload()

      pipeOut := pipeIn
      pipeOut.allowOverride
      cWrBgArr(idx).bypass(wrBgPipePayload) := pipeOut

      (pipeIn, pipeOut)
    }
    //def setWrBgPipeOut(
    //  idx: Int, 
    //  somePayload: WrBgPipePayload,
    //): Unit = {
    //  //sWrBgArr(idx)()
    //  //nWrBgArr(idx)(wrBgPipePayloadArr(idx)) := somePayload
    //}
    //def getWrBgPipeOut(idx: Int): Payload[WrBgPipePayload] = {
    //  //sWrBgArr(idx)()
    //}

    //wrBgPipeOut.last.ready := (
    //  // This is a heuristic!
    //  //lineFifo.io.misc.amountCanPush > 8
    //  lineFifo.io.misc.amountCanPush
    //  > params.wrBgObjStallFifoAmountCanPush
    //)

    //val wrBgPipeLast = KeepAttribute(
    //  //Flow(WrBgPipePayload())
    //  Flow(WrBgPipePayload())
    //)

    //when (nextIntnlChangingRow)
    //when (rIntnlChangingRow)

    when (intnlChangingRowRe)
    {
      nextWrBgChangingRow := False
    } elsewhen (
      //wrBgPipeLast.bakCntWillBeDone() && wrBgPipeLast.fire
      wrBgPipeLast.bakCntWillBeDone() && nWrBgPipeLast.isFiring
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
    //val nextWrBgPipeFrontValid = Bool()
    //val rWrBgPipeFrontValid = RegNext(nextWrBgPipeFrontValid)
    //  .init(True)
    //val nextWrBgPipeFront = Flow(WrBgPipePayload())
    //val nextWrBgPipeFrontValid = Bool()
    ////val rWrBgPipeFrontValid = RegNext(nextWrBgPipeFrontValid) init(False)
    //val rWrBgPipeFront = Reg(Flow(WrBgPipePayload()))
    //rWrBgPipeFront.init(rWrBgPipeFront.getZero)

    //val rSavedWrBgPipeFrontValid = Reg(Bool()) init(False)
    ////val rWrBgPipeFrontPayload = Reg(WrBgPipePayload())
    ////rWrBgPipeFrontPayload.init(rWrBgPipeFrontPayload.getZero)
    //rWrBgPipeFrontPayload.init(doInitWrBgPipePayload(
    //  //prevLineMemArrIdx=wrFullLineMemArrIdxInit
    //  //prevLineNum=wrLineNumInit - 1
    //  //prevLineNum=wrLineNumInit
    //  firstInit=true
    //))
    val nextWrBgPipeFrontValid = Bool()
    val rWrBgPipeFrontValid = RegNext(nextWrBgPipeFrontValid)
      .init(True)
    val rSavedWrBgPipeFrontValid = Reg(Bool()) init(False)
    val rWrBgPipeFrontPayload = Reg(WrBgPipePayload())
    //rWrBgPipeFrontPayload.init(rWrBgPipeFrontPayload.getZero)
    rWrBgPipeFrontPayload.init(doInitWrBgPipePayload(
      //prevLineMemArrIdx=wrFullLineMemArrIdxInit
      //prevLineNum=wrLineNumInit - 1
      //prevLineNum=wrLineNumInit
      firstInit=true
    ))
    val wrBgPipeFront = Flow(WrBgPipePayload())
    wrBgPipeFront.valid := rWrBgPipeFrontValid
    wrBgPipeFront.payload := rWrBgPipeFrontPayload

    nWrBgArr(0).driveFrom(wrBgPipeFront)(
      con=(node, payload) => {
        //node(wrBgPipePayloadArr(0)) := payload
        node(wrBgPipePayload) := payload
      },
    )

    //--------
    //wrBgPipeIn(0).valid := rWrBgPipeFrontValid
    //wrBgPipeIn(0).payload := rWrBgPipeFrontPayload
    //for (idx <- 1 to wrBgPipeIn.size - 1) {
    //  // Create pipeline registering
    //  wrBgPipeIn(idx) <-< wrBgPipeOut(idx - 1)
    //  //wrBgPipeIn(idx) <-/< wrBgPipeOut(idx - 1)
    //}
    //for (idx <- 0 to wrBgPipeOut.size - 1) {
    //  // Connect output `valid` to input `valid`
    //  wrBgPipeOut(idx).valid := wrBgPipeIn(idx).valid
    //  //wrBgPipeIn(idx).ready := wrBgPipeOut(idx).ready
    //}
    //--------

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
    //// add one final register
    //wrBgPipeLast <-< wrBgPipeOut.last
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
        when (nWrBgArr(0).isFiring) {
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

    val nWrObjArr = Array.fill(wrBgObjPipeNumStages + 1)(Node())
    val sWrObjArr = new ArrayBuffer[StageLink]()
    //val s2mWrObjArr = new ArrayBuffer[S2MLink]()
    val cWrObjArr = new ArrayBuffer[CtrlLink]()
    def wrObjPipeIdxSlmRmwFront = 14
    def wrObjPipeIdxSlmRmwModFront = 15
    def wrObjPipeIdxSlmRmwModBack = 17
    def wrObjPipeIdxSlmRmwBack = 18
    def wrObjPipeIdxSlmRmwModStageCnt = (
      wrObjPipeIdxSlmRmwModBack
      - wrObjPipeIdxSlmRmwModFront
      + 1
      //+ 1
    )

    def wrObjPipeNumForkOrJoinRenderers = (
      2 // the object writing pipeline and the combining pipeline
    )
    //def wrObjPipeNumForkOrJoinPerRendererPipeStage = (
    //  params.numLineMemsPerBgObjRenderer
    //)

    //val nfWrObjSlmRmwFrontA2d = Array.fill(
    //  wrObjPipeNumForkOrJoinRenderers 
    //)(
    //  Array.fill(
    //    wrObjPipeNumForkOrJoinPerRendererPipeStage
    //  )(
    //    Node()
    //  )
    //)
    //val njWrObjSlmRmwModFrontA2d = Array.fill(
    //  wrObjPipeNumForkOrJoinRenderers 
    //)(
    //  Array.fill(
    //    wrObjPipeNumForkOrJoinPerRendererPipeStage
    //  )(
    //    Node()
    //  )
    //)
    //val nfWrObjSlmRmwModBackA2d = Array.fill(
    //  wrObjPipeNumForkOrJoinRenderers 
    //)(
    //  Array.fill(
    //    wrObjPipeNumForkOrJoinPerRendererPipeStage
    //  )(
    //    Node()
    //  )
    //)

    //val wrObjSlmRmwStmMuxArr = Array.fill(
    //  wrObjPipeNumForkOrJoinPerRendererPipeStage
    //)(
    //  StreamMux(
    //    //WrObjPipePayload(isAffine=false)
    //  )
    //)
    //--------
    //def wrObjStmMuxDemuxArrIdxFront = 0
    //def wrObjStmMuxDemuxArrIdxModFront = 1
    //def wrObjStmMuxDemuxArrIdxModBack = 2
    //def wrObjStmMuxDemuxArrSize = 3

    //def wrObjStmMuxDemuxArrIdxWrObj = 0
    //def wrObjStmMuxDemuxArrIdxCombine = 1
    //def wrObjStmMuxDemuxArrSize = 2
    //val wrObjSlmRmwStmMuxInpArr = new ArrayBuffer[
    //  Vec[Stream[WrObjPipePayload]]
    //]()
    //val wrObjSlmRmwStmMuxSelArr = new ArrayBuffer[
    //  //UInt
    //  Stream[UInt]
    //]()
    //val wrObjSlmRmwStmMuxArr = new ArrayBuffer[
    //  Stream[WrObjPipePayload]
    //]()

    //val wrObjSlmRmwStmDemuxInpArr = new ArrayBuffer[
    //  Stream[WrObjPipePayload]
    //]()
    //val wrObjSlmRmwStmDemuxSelArr = new ArrayBuffer[
    //  //UInt
    //  Stream[UInt]
    //]()
    //val wrObjSlmRmwStmDemuxArr = new ArrayBuffer[
    //  Vec[Stream[WrObjPipePayload]]
    //]()

    //--------
    //val wrObjSlmRmwStmMuxInpFront = Vec.fill(
    //  wrObjPipeNumForkOrJoinPerRendererPipeStage
    //)(
    //  Stream(WrObjPipePayload(isAffine=false))
    //)
    //val wrObjSlmRmwStmMuxSelFront = (
    //  Stream(UInt(
    //    log2Up(wrObjPipeNumForkOrJoinPerRendererPipeStage) bits
    //  ))
    //)
    //val wrObjSlmRmwStmMuxArr = StreamMux(
    //  select=wrObjSlmRmwStmMuxSelFront,
    //  inputs=wrObjSlmRmwStmMuxInpFront,
    //)
    //--------

    //for (jdx <- 0 until wrObjStmMuxDemuxArrSize) {
    //  wrObjSlmRmwStmMuxInpA2d += new ArrayBuffer[
    //    Vec[Stream[WrObjPipePayload]]
    //  ]()
    //  wrObjSlmRmwStmMuxSelA2d += new ArrayBuffer[
    //    //UInt
    //    Stream[UInt]
    //  ]()
    //  wrObjSlmRmwStmMuxA2d += new ArrayBuffer[
    //    Stream[WrObjPipePayload]
    //  ]()

    //  wrObjSlmRmwStmDemuxInpA2d += new ArrayBuffer[
    //    Stream[WrObjPipePayload]
    //  ]()
    //  wrObjSlmRmwStmDemuxSelA2d += new ArrayBuffer[
    //    //UInt
    //    Stream[UInt]
    //  ]()
    //  wrObjSlmRmwStmDemuxA2d += new ArrayBuffer[
    //    Vec[Stream[WrObjPipePayload]]
    //  ]()

    //  def wrObjSlmRmwStmMuxInpArr = wrObjSlmRmwStmMuxInpA2d.last
    //  def wrObjSlmRmwStmMuxSelArr = wrObjSlmRmwStmMuxSelA2d.last
    //  def wrObjSlmRmwStmMuxArr = wrObjSlmRmwStmMuxA2d.last

    //  def wrObjSlmRmwStmDemuxInpArr = wrObjSlmRmwStmDemuxInpA2d.last
    //  def wrObjSlmRmwStmDemuxSelArr = wrObjSlmRmwStmDemuxSelA2d.last
    //  def wrObjSlmRmwStmDemuxArr = wrObjSlmRmwStmDemuxA2d.last
    //  for (
    //    idx <- 0 until wrObjStmMuxDemuxArrSize
    //  ) {
    //    wrObjSlmRmwStmMuxInpArr += Vec.fill(
    //      wrObjPipeNumForkOrJoinPerRendererPipeStage
    //    )(
    //      Stream(WrObjPipePayload(isAffine=false))
    //    )
    //    wrObjSlmRmwStmMuxSelArr += (
    //      Stream(UInt(
    //        log2Up(wrObjPipeNumForkOrJoinPerRendererPipeStage) bits
    //      ))
    //    )
    //    wrObjSlmRmwStmMuxArr += StreamMux(
    //      select=wrObjSlmRmwStmMuxSelArr.last,
    //      inputs=wrObjSlmRmwStmMuxInpArr.last,
    //    )

    //    wrObjSlmRmwStmDemuxInpArr += (
    //      Stream(WrObjPipePayload(isAffine=false))
    //    )
    //    wrObjSlmRmwStmDemuxSelArr += (
    //      Stream(UInt(
    //        log2Up(wrObjPipeNumForkOrJoinPerRendererPipeStage) bits
    //      ))
    //    )
    //    wrObjSlmRmwStmDemuxArr += StreamDemux(
    //      input=wrObjSlmRmwStmDemuxInpArr.last,
    //      select=wrObjSlmRmwStmDemuxSelArr.last,
    //      portCount=wrObjPipeNumForkOrJoinPerRendererPipeStage,
    //    )
    //  }
    //}
    //--------

    val wrObjPipePayloadSlmRmwModFrontInp = Array.fill(
      params.numLineMemsPerBgObjRenderer
    )(
      Payload(WrObjPipePayload(isAffine=false))
    )
    val wrObjPipePayloadSlmRmwModFrontOutp = (
      Payload(WrObjPipePayload(isAffine=false))
    )
    val wrObjPipePayloadSlmRmwBackInp = Array.fill(
      params.numLineMemsPerBgObjRenderer
    )(
      Payload(WrObjPipePayload(isAffine=false))
    )
    val wrObjPipePayloadSlmRmwBackOutp = (
      Payload(WrObjPipePayload(isAffine=false))
    )
    //val wrObjPipePayloadFront = Payload(WrObjPipePayload(isAffine=false))
    //val wrObjPipePayloadMain = Array.fill(wrBgObjPipeNumStages + 1)(
    //  Payload(WrObjPipePayload(isAffine=false))
    //)
    val wrObjPipePayloadMain = new ArrayBuffer[
      Payload[WrObjPipePayload]
    ]()
    for (idx <- 0 until wrBgObjPipeNumStages + 1) {
      wrObjPipePayloadMain += (
        Payload(WrObjPipePayload(isAffine=false))
        .setName(s"wrObjPipePayloadMain_${idx}")
      )
    }
    //val wrObjPipePayloadSlmRmwModFront = (
    //  WrObjPipePayload(isAffine=false)
    //)

    //val wrObjPipe = PipeHelper(linkArr=linkArr)
    //def nWrObjArr = wrObjPipe.nArr
    //def sWrObjArr = wrObjPipe.sArr
    //def s2mWrObjArr = wrObjPipe.s2mArr
    //def cWrObjArr = wrObjPipe.cArr
    for (
      idx <- 0 until nWrObjArr.size - 1
      //idx <- 0 until wrBgObjPipeNumStages + 1
      //idx <- 0 until wrBgObjPipeNumStages - 1
    ) {
      //wrObjPipe.addStage(
      //  name=(s"WrObjPipe_$idx"),
      //  optIncludeS2M=(
      //    //false
      //    idx >=
      //  ),
      //)
      def addMainLinks(
        up: Option[Node]=None,
        down: Option[Node]=None,
      ): Unit = {
        sWrObjArr += StageLink(
          up={
            up match {
              case Some(myUp) => {
                myUp
              }
              case None => {
                nWrObjArr(idx)
              }
            }
          },
          down=Node(),
        )
        linkArr += sWrObjArr.last

        //s2mWrObjArr += S2MLink(
        //  up=sWrObjArr.last.down,
        //  down=Node(),
        //)
        //linkArr += s2mWrObjArr.last

        cWrObjArr += CtrlLink(
          up=(
            sWrObjArr.last.down
            //s2mWrObjArr.last.down
            //lastNode
          ),
          down={
            down match {
              case Some(myDown) => {
                myDown
              }
              case None => {
                nWrObjArr(idx + 1),
              }
            }
          },
        )
        linkArr += cWrObjArr.last
      }
      if (idx == wrObjPipeIdxSlmRmwFront) {
        //println("wrObjPipeIdxSlmRmwFront")
        //println(s"idx == front: $idx")
        val down = Node()
          .setName(s"wrObjPipeSlmRmw_front_down_$idx")

        addMainLinks(
          up=None,
          down=Some(down),
        )
        //val nfMyArr = Array.fill(
        //  wrObjPipeNumForkOrJoinRenderers
        //)(
        //  Node()
        //)
        val nfMyArr = new ArrayBuffer[Node]()
        for (jdx <- 0 until wrObjPipeNumForkOrJoinRenderers) {
          nfMyArr += (
            Node()
          )
          nfMyArr.last.setName(s"nfMyArr_front_$jdx")
        }
        val fMyDown = ForkLink(
          up=(
            //nWrObjArr(idx)
            down
          ),
          downs=nfMyArr.toSeq,
          synchronous=true,
        )
          .setName("fMyDown_front")
        linkArr += fMyDown
        for (jdx <- 0 until nfMyArr.size) {
          val sLink = StageLink(
            up=nfMyArr(jdx),
            down=Node(),
          )
          linkArr += sLink
          val s2mLink = S2MLink(
            up=sLink.down,
            down=(
              //Node()
              wrObjSubLineMemArr(jdx).io.front
            ),
          )
          linkArr += s2mLink


          //s2mLink.down//sLink.down
          ///*nfMyArr(jdx)*/.driveTo(
          //  wrObjSubLineMemArr(jdx).io.front
          //)(
          //  con=(payload, node) => {
          //    payload := (
          //      //node(wrObjPipePayloadMain)
          //      node(wrObjPipePayloadMain(idx + 1))
          //    )
          //  }
          //)
          wrObjSubLineMemArr(jdx).io.front(
            wrObjSubLineMemArr(jdx).io.frontPayload
          ) := (
            s2mLink.down(wrObjPipePayloadMain(idx + 1))
          )
        }
      } else if (idx == wrObjPipeIdxSlmRmwModFront) {
        //println(s"idx == modFront: $idx")
        //val njMyArr = Array.fill(
        //  wrObjPipeNumForkOrJoinRenderers
        //)(
        //  Node()
        //)
        val njMyArr = new ArrayBuffer[Node]()
        for (jdx <- 0 until wrObjPipeNumForkOrJoinRenderers) {
          njMyArr += (
            Node()
            //wrObjSubLineMemArr(jdx).io.modFront
          )
          njMyArr.last.setName(s"njMyArr_modFront_$jdx")
          //println(njMyArr.last.getName())
        }
        val jMyWrObj = JoinLink(
          ups=njMyArr.toSeq,
          down=(
            Node()
            .setName("jMyWrObj_modFront_down")
          ),
        )
          .setName("jMyWrObj_modFront")
        linkArr += jMyWrObj 
        for (jdx <- 0 until njMyArr.size) {
          //njMyArr(jdx).driveFrom(
          //  wrObjSubLineMemArr(
          //    jdx
          //  ).io.modFront
          //)(
          //  con=(node, payload) => {
          //    node(wrObjPipePayloadSlmRmwModFrontInp(
          //      jdx
          //    )) := payload
          //    ////node(wrObjPipePayloadMain) := payload
          //  }
          //)
          njMyArr(jdx)(
            wrObjPipePayloadSlmRmwModFrontInp(jdx)
          ) := (
            wrObjSubLineMemArr(
              jdx
            ).io.modFront(
              wrObjSubLineMemArr(jdx).io.modFrontPayload
            )
          )
          //if (vivadoDebug) {
            njMyArr(jdx)(
              wrObjPipePayloadSlmRmwModFrontInp(jdx)
            ).addAttribute("MARK_DEBUG", "TRUE")
          //}
        }
        val dMyWrObj = DirectLink(
          up=jMyWrObj.down,
          down=(
            Node()
              .setName(s"dMyWrObj_modFront_down_$idx")
          ),
        )
          .setName(s"dMyWrObj_modFront_$idx")
        linkArr += dMyWrObj
        for (jdx <- 0 until njMyArr.size) {
          wrObjSubLineMemArr(jdx).io.midModStages(0) := (
            RegNext(
              wrObjSubLineMemArr(jdx).io.midModStages(0)
            )
            init(
              wrObjSubLineMemArr(jdx).io.midModStages(0)
              .getZero
            )
          )
          when (dMyWrObj.up.isValid) {
            wrObjSubLineMemArr(
              jdx
            ).io.midModStages(0) := (
              dMyWrObj.up(wrObjPipePayloadSlmRmwModFrontInp(
                jdx
              ))
            )
          }
        }
        //cWrObjArr(idx).up
        switch ((wrObjPipeLineMemArrIdx(4) + 0)(0 downto 0)) {
          for (
            jdx <- 0
            until (1 << wrObjPipeLineMemArrIdx(4).getWidth)
          ) {
            is (jdx) {
              dMyWrObj.down(
                wrObjPipePayloadSlmRmwModFrontOutp
                //wrObjPipePayloadMain(idx)
              ) := (
                dMyWrObj.up(
                  wrObjPipePayloadSlmRmwModFrontInp(jdx)
                )
              )
            }
          }
        }
        ////switch (rWrFullObjPipeLineMemArrIdx(3)) {
        ////  for (
        ////    jdx <- 0
        ////    until (1 << rWrFullObjPipeLineMemArrIdx(3).getWidth)
        ////  ) {
        ////    is (jdx) {
        ////      jMyWrObj.down(wrObjPipePayloadSlmRmwModFrontOutp) := (
        ////        jMyWrObj.ups(jdx)(
        ////          wrObjPipePayloadSlmRmwModFrontInp(jdx)
        ////          //wrObjPipePayloadMain
        ////        )
        ////      )
        ////      for (kdx <- 0 until wrObjSubLineMemArr.size) {
        ////        wrObjSubLineMemArr(kdx).io.midModStages(0) := (
        ////          jMyWrObj.ups(jdx)(
        ////            wrObjPipePayloadSlmRmwModFrontInp(jdx)
        ////            //wrObjPipePayloadMain
        ////          )
        ////          //nWrObjArr(
        ////          //  idx //+ 1
        ////          //)(
        ////          //  //wrObjPipePayloadSlmRmwModFrontOutp
        ////          //  wrObjPipePayloadMain
        ////          //)
        ////        )
        ////      }
        ////    }
        ////  }
        ////}
        addMainLinks(
          up=Some(
            //jMyWrObj.down
            dMyWrObj.down
          ),
          down=None,
        )
      } else if (idx == wrObjPipeIdxSlmRmwModFront + 1) {
        //println(s"idx == modFront + 1: ${idx}")
        for (jdx <- 0 until wrObjPipeNumForkOrJoinRenderers) {
          wrObjSubLineMemArr(jdx).io.midModStages(1) := (
            RegNext(wrObjSubLineMemArr(jdx).io.midModStages(1))
            init(
              wrObjSubLineMemArr(jdx).io.midModStages(1)
              .getZero
            )
          )
          when (nWrObjArr(idx).isValid) {
            wrObjSubLineMemArr(jdx).io.midModStages(1) := (
              nWrObjArr(idx)
              (
                wrObjPipePayloadMain(idx /*+ 1*/)
              )
            )
          }
        }
        addMainLinks(
          up=None,
          down=None,
        )
      } else if (idx == wrObjPipeIdxSlmRmwModBack) {
        //println("wrObjPipeIdxSlmRmwModBack")
        //println(s"idx == modBack: $idx")
        val down = Node()
          .setName(s"wrObjPipeSlmRmw_modBack_down_$idx")
        addMainLinks(
          up=None,
          down=Some(down),
        )
        //val nfMyArr = Array.fill(
        //  wrObjPipeNumForkOrJoinRenderers
        //)(
        //  Node()
        //)
        val nfMyArr = new ArrayBuffer[Node]()
        for (jdx <- 0 until wrObjPipeNumForkOrJoinRenderers) {
          nfMyArr += (
            //Node()
            wrObjSubLineMemArr(jdx).io.modBack
          )
          nfMyArr.last.setName(s"nfMyArr_modBack_$jdx")
        }
        val fMyDown = ForkLink(
          up=(
            //nWrObjArr(idx)
            down
          ),
          downs=nfMyArr.toSeq,
          synchronous=true,
        )
        linkArr += fMyDown
        for (jdx <- 0 until nfMyArr.size) {
          //val sLink = StageLink(
          //  up=nfMyArr(jdx),
          //  down=Node(),
          //)
          //linkArr += sLink

          //val s2mLink = S2MLink(
          //  up=sLink.down,
          //  down=Node(),
          //)
          //linkArr += s2mLink
          /*s2mLink*/
          //
          //nfMyArr(jdx)
          ////sLink
          ///*s2mLink.down*/.driveTo(
          //  wrObjSubLineMemArr(jdx).io.modBack
          //)(
          //  con=(payload, node) => {
          //    payload := node(
          //      //wrObjPipePayloadMain
          //      //wrObjPipePayloadSlmRmwModFrontOutp
          //      wrObjPipePayloadMain(idx + 1)
          //    )
          //  }
          //)

          wrObjSubLineMemArr(jdx).io.modBack(
            wrObjSubLineMemArr(jdx).io.modBackPayload
          ) := (
            nfMyArr(jdx)(
              wrObjPipePayloadMain(idx + 1)
            )
          )
          wrObjSubLineMemArr(jdx).io.midModStages(2) := (
            RegNext(wrObjSubLineMemArr(jdx).io.midModStages(2))
            init(
              wrObjSubLineMemArr(jdx).io.midModStages(2)
              .getZero
            )
          )
          when (nWrObjArr(idx).isValid) {
            wrObjSubLineMemArr(jdx).io.midModStages(2) := (
              //node(wrObjPipePayloadMain)
              //nfMyArr(jdx)(wrObjPipePayloadMain)
              nWrObjArr(idx)
              /*down*/
              (
                //wrObjPipePayloadMain
                //wrObjPipePayloadSlmRmwModFrontOutp
                wrObjPipePayloadMain(idx /*+ 1*/)
              )
              //nWrObjArr(idx)(
              //  //wrObjPipePayloadMain
              //  //wrObjPipePayloadSlmRmwModFrontOutp
              //  wrObjPipePayloadMain(idx + 1)
              //)
            )
          }
          //--------
          //wrObjSubLineMemArr(jdx).io.midModStages(2) := (
          //  RegNext(
          //    wrObjSubLineMemArr(jdx).io.midModStages(2)
          //  )
          //  init(
          //    wrObjSubLineMemArr(jdx).io.midModStages(2).getZero
          //  )
          //)
          //when (down.isValid) {
          //  wrObjSubLineMemArr(jdx).io.midModStages(2) := (
          //    /*s2mLink.*/down(
          //      wrObjPipePayloadMain(idx + 1)
          //    )
          //  )
          //}
          //--------
          //wrObjSubLineMemArr(jdx).io.midModStages(1) := (
          //  nWrObjArr(idx)(
          //    wrObjPipePayloadMain
          //  )
          //  ////sLink.down(wrObjPipePayloadMain)
          //  //nfMyArr(jdx)(
          //  //  wrObjPipePayloadMain
          //  //  //wrObjPipePayloadSlmRmwModFrontOutp
          //  //)
          //  ////down(wrObjPipePayloadMain)
          //  ////nWrObjArr(idx)(wrObjPipePayloadMain)
          //)
        }
      } else if (idx == wrObjPipeIdxSlmRmwBack) {
        //println(s"idx == back: $idx")
        //println("wrObjPipeIdxSlmRmwBack")
        //val njMyArr = Array.fill(
        //  wrObjPipeNumForkOrJoinRenderers
        //)(
        //  Node()
        //)

        val njMyArr = new ArrayBuffer[Node]()
        for (jdx <- 0 until wrObjPipeNumForkOrJoinRenderers) {
          njMyArr += (
            Node()
            //wrObjSubLineMemArr(jdx).io.back
          )
          njMyArr.last.setName(s"njMyArr_back_$jdx")
        }
        val jMyWrObj = JoinLink(
          ups=njMyArr.toSeq,
          down=(
            Node()
            .setName("jMyWrObj_back_down")
          ),
        )
          .setName("jMyWrObj_back")
        linkArr += jMyWrObj 
        for (jdx <- 0 until njMyArr.size) {
          //njMyArr(jdx).driveFrom(
          //  wrObjSubLineMemArr(jdx).io.back
          //)(
          //  con=(node, payload) => {
          //    node(wrObjPipePayloadSlmRmwBackInp(jdx)) := (
          //      payload
          //    )
          //    //node(wrObjPipePayloadMain) := payload
          //    //switch (rWrFullObjPipeLineMemArrIdx(5)) {
          //    //  for (
          //    //    kdx <- 0
          //    //    until (1 << rWrFullObjPipeLineMemArrIdx(5).getWidth)
          //    //  ) {
          //    //    is (kdx) {
          //    //      node(wrObjPipePayloadSlmRmwBackOutp) := payload
          //    //    }
          //    //  }
          //    //}
          //  }
          //)
          njMyArr(jdx)(
            wrObjPipePayloadSlmRmwBackInp(jdx)
          ) := (
            wrObjSubLineMemArr(jdx).io.back(
              wrObjSubLineMemArr(jdx).io.backPayload
            )
          )
        }
        //val dMyWrObj = DirectLink(
        //  up=jMyWrObj.down,
        //  down=Node()
        //)
        val dMyWrObj = DirectLink(
          up=jMyWrObj.down,
          down=(
            Node()
              .setName(s"dMyWrObj_back_down_$idx")
          ),
        )
          .setName(s"dMyWrObj_back_$idx")
        linkArr += dMyWrObj
        switch ((wrObjPipeLineMemArrIdx(5) + 0)(0 downto 0)) {
          for (
            jdx <- 0
            until (1 << wrObjPipeLineMemArrIdx(5).getWidth)
          ) {
            is (jdx) {
              //println(s"is (${jdx})")
              dMyWrObj.down(wrObjPipePayloadSlmRmwBackOutp) := (
                dMyWrObj.up(wrObjPipePayloadSlmRmwBackInp(jdx))
              )
              //dMyWrObj.down(wrObjPipePayloadMain(idx)) := (
              //  dMyWrObj.up(wrObjPipePayloadSlmRmwBackInp(jdx))
              //)
              //wrObjSubLineMemArr(jdx).io.midModStages(0) := (
              //  dMyWrObj.up(wrObjPipePayloadSlmRmwModFrontInp(jdx))
              //)
            }
            default {
              //println(s"default ${jdx}")
            }
          }
        }
        //switch (rWrFullObjPipeLineMemArrIdx(3)) {
        //  for (
        //    jdx <- 0
        //    until (1 << rWrFullObjPipeLineMemArrIdx(3).getWidth)
        //  ) {
        //    is (jdx) {
        //      dMyWrObj.down(wrObjPipePayloadSlmRmwBackOutp) := (
        //        dMyWrObj.up(wrObjPipePayloadSlmRmwBackInp(jdx))
        //      )
        //      //jMyWrObj.down(wrObjPipePayloadSlmRmwBackOutp) := (
        //      //  //jMyWrObj.ups(jdx)(
        //      //  //  wrObjPipePayloadSlmRmwBackInp(jdx)
        //      //  //  //wrObjPipePayloadMain
        //      //  //)
        //      //)
        //    }
        //  }
        //}
        addMainLinks(
          up=(
            Some(
              //jMyWrObj.down
              dMyWrObj.down
            )
            //None
          ),
          down=None,
        )
      }
      else {
        //println(idx)
        addMainLinks()
      }
      //addLinks()
      //sWrObjArr += StageLink(
      //  up=nWrObjArr(idx),
      //  down=Node(),
      //)
      //linkArr += sWrObjArr.last

      //cWrObjArr += CtrlLink(
      //  up=sWrObjArr.last.down,
      //  down=nWrObjArr(idx + 1),
      //)
      //linkArr += cWrObjArr.last
    }
    //wrObjPipe.addStage(
    //  name="WrObjPipe_Last",
    //  optIncludeS2M=true,
    //  finish=true,
    //)
    //for (idx <- 0 until nWrObjArr.size - 1) {
    //  sWrObjArr += StageLink(
    //    up=nWrObjArr(idx),
    //    down=Node(),
    //  )
    //  linkArr += sWrObjArr.last

    //  cWrObjArr += CtrlLink(
    //    up=sWrObjArr.last.down,
    //    down=nWrObjArr(idx + 1),
    //  )
    //  linkArr += cWrObjArr.last
    //}
    val nWrObjPipeLast = nWrObjArr.last
    //val wrObjPipePayloadArr = Array.fill(nWrObjArr.size)(
    //  Payload(WrObjPipePayload())
    //)
    def wrObjPipeLast = nWrObjPipeLast(
      //wrObjPipePayloadArr.last
      wrObjPipePayloadMain(wrObjPipePayloadMain.size - 1)
    )
    val wrObjPipeOutArr = Array.fill(wrBgObjPipeNumStages)(
      WrObjPipePayload(isAffine=false)
    )
    val tempWrObjPipeOutArr = Array.fill(wrBgObjPipeNumStages)(
      WrObjPipePayload(isAffine=false)
    )
    def initTempWrObjPipeOut(
      idx: Int,
      //tempOutp: WrObjPipePayload,
    ): (WrObjPipePayload, WrObjPipePayload) = {
      // This function returns `(tempInp, tempOutp)`
      def pipeIn = (
        if (idx == wrObjPipeIdxSlmRmwModFront) {
          cWrObjArr(idx).up(wrObjPipePayloadSlmRmwModFrontOutp)
        } 
        else if (idx == wrObjPipeIdxSlmRmwBack) {
          //cWrObjArr(idx).up(wrObjPipePayloadSlmRmwBackOutp)
          cWrObjArr(idx).up(wrObjPipePayloadSlmRmwBackOutp)
        } else {
          cWrObjArr(idx).up(wrObjPipePayloadMain(idx))
        }
        //if (idx == 0) (
        //  cWrObjArr(idx).up(wrObjPipePayloadFront)
        //) else ( // if (idx > 0)
          //cWrObjArr(idx).up(wrObjPipePayloadMain(idx - 1))
        //  cWrObjArr(idx).up(wrObjPipePayloadMain(idx))
        //)
      )
      //val pipeOut = WrObjPipePayload(isAffine=false)
      ////pipeOut := (
      ////  RegNext(pipeOut) init(pipeOut.getZero)
      ////)
      ////when (cWrObjArr(idx).up.isValid) {
      ////  pipeOut := pipeIn
      ////}
      def pipeOut = wrObjPipeOutArr(idx)
      pipeOut := pipeIn
      pipeOut.allowOverride
      //if (idx == wrObjPipeIdxSlmRmwModFront + 1) {
      //  cWrObjArr(idx).bypass(wrObjPipePayloadSlmRmwModFrontOutp) := (
      //    pipeOut
      //  )
      //} else if (idx == wrObjPipeIdxSlmRmwBack + 1) {
      //  cWrObjArr(idx).bypass(wrObjPipePayloadSlmRmwBackOutp) := (
      //    pipeOut
      //  )
      //} else {
      //  cWrObjArr(idx).bypass(wrObjPipePayloadMain) := (
      //    pipeOut
      //  )
      //}

      //val tempPipeOut = WrObjPipePayload(isAffine=false)
      def tempPipeOut = tempWrObjPipeOutArr(idx)
      tempPipeOut := (
        RegNext(tempPipeOut) init(tempPipeOut.getZero)
      )
      when (cWrObjArr(idx).up.isValid) {
        //cWrObjArr(idx).bypass(wrObjPipePayloadMain(idx + 1)) := (
        //  pipeOut
        //)
        tempPipeOut := pipeOut
      }
      cWrObjArr(idx).up(wrObjPipePayloadMain(idx + 1)) := (
        tempPipeOut
      )

      (pipeIn, pipeOut)
    }

    val nWrObjAffineArr = Array.fill(wrBgObjPipeNumStages + 1)(Node())
    val sWrObjAffineArr = new ArrayBuffer[StageLink]()
    val cWrObjAffineArr = new ArrayBuffer[CtrlLink]()
    for (idx <- 0 until nWrObjAffineArr.size - 1) {
      sWrObjAffineArr += StageLink(
        up=nWrObjAffineArr(idx),
        down=Node(),
      )
      linkArr += sWrObjAffineArr.last

      cWrObjAffineArr += CtrlLink(
        up=sWrObjAffineArr.last.down,
        down=nWrObjAffineArr(idx + 1),
      )
      linkArr += cWrObjAffineArr.last
    }
    val nWrObjAffinePipeLast = nWrObjAffineArr.last
    //val wrObjAffinePipePayloadArr = Array.fill(nWrObjAffineArr.size)(
    //  Payload(WrObjAffinePipePayload())
    //)
    val wrObjAffinePipePayload = Payload(WrObjPipePayload(
      isAffine=true
    ))
    def wrObjAffinePipeLast = nWrObjAffinePipeLast(
      //wrObjAffinePipePayloadArr.last
      wrObjAffinePipePayload
    )
    def initTempWrObjAffinePipeOut(
      idx: Int,
      //tempOutp: WrObjAffinePipePayload,
    ): (WrObjPipePayload, WrObjPipePayload) = {
      // This function returns `(tempInp, tempOutp)`
      val pipeIn = (
        //cWrObjAffineArr(idx).down(wrObjAffinePipePayload)
        cWrObjAffineArr(idx).up(wrObjAffinePipePayload)
        //nWrObjAffineArr(idx)(wrObjAffinePipePayload)
      )
      val pipeOut = WrObjPipePayload(isAffine=true)

      pipeOut := pipeIn
      pipeOut.allowOverride
      cWrObjAffineArr(idx).bypass(wrObjAffinePipePayload) := pipeOut

      (pipeIn, pipeOut)
    }

    //val wrObjPipeIn = KeepAttribute(
    //  //Vec.fill(wrBgObjPipeNumStages)(Reg(WrObjPipePayload()))
    //  Vec.fill(
    //    wrBgObjPipeNumStages
    //    //anyPipeNumStages
    //  )(
    //    //Flow(WrObjPipePayload())
    //    Flow(WrObjPipePayload(isAffine=false))
    //  )
    //)
    //val wrObjPipeOut = KeepAttribute(
    //  //Vec.fill(wrBgObjPipeNumStages)(Reg(WrObjPipePayload()))
    //  Vec.fill(
    //    wrBgObjPipeNumStages
    //    //anyPipeNumStages
    //  )(
    //    //Flow(WrObjPipePayload())
    //    Flow(WrObjPipePayload(isAffine=false))
    //  )
    //)
    //val wrObjAffinePipeIn = (!noAffineObjs) generate KeepAttribute(
    //  //Vec.fill(wrBgObjPipeNumStages)(Reg(WrObjPipePayload()))
    //  Vec.fill(
    //    wrBgObjPipeNumStages
    //    //anyPipeNumStages
    //  )(
    //    //Flow(WrObjPipePayload())
    //    Flow(WrObjPipePayload(isAffine=true))
    //  )
    //)
    //val wrObjAffinePipeOut = (!noAffineObjs) generate KeepAttribute(
    //  //Vec.fill(wrBgObjPipeNumStages)(Reg(WrObjPipePayload()))
    //  Vec.fill(
    //    wrBgObjPipeNumStages
    //    //anyPipeNumStages
    //  )(
    //    //Flow(WrObjPipePayload())
    //    Flow(WrObjPipePayload(isAffine=true))
    //  )
    //)
    //case class WrObjPipe6ExtSingle() extends Bundle {
    //  val wrLineMemEntry = ObjSubLineMemEntry()
    //  val overwriteLineMemEntry = Bool()
    //}
    case class WrObjPipe14Ext(
      isAffine: Boolean,
      useVec: Boolean//=true
    ) extends Bundle {
      def vecSize = (
        if (useVec) {
          //if (!isAffine) {
          //  params.objTileSize2d.x
          //} else {
          //  params.objAffineDblTileSize2d.x
          //  //params.objAffineSliceTileWidth
          //  //params.myDbgObjAffineTileWidth
          //}
          //params.tempObjTileWidth1(isAffine=isAffine)
          params.tempObjTileWidth(isAffine=isAffine)
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
    }
    //val rWrObjPipeOut6Ext = Reg(WrObjPipe6Ext(useVec=true))
    //rWrObjPipeOut6Ext.init(rWrObjPipeOut6Ext.getZero)

    //wrObjPipeOut.last.ready := (
    //  // This is a heuristic!
    //  objLineFifo.io.misc.amountCanPush
    //  > params.wrBgObjStallFifoAmountCanPush
    //)


    //val wrObjPipeLast = KeepAttribute(
    //  //Flow(WrObjPipePayload())
    //  Flow(WrObjPipePayload(isAffine=false))
    //)
    when (intnlChangingRowRe) {
      nextWrObjChangingRow := False
    } elsewhen (
      //wrObjPipeLast.bakCntWillBeDone && wrObjPipeLast.fire
      wrObjPipeLast.bakCntWillBeDone && nWrObjPipeLast.isFiring
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
    val rWrObjPipeFrontPayload = Reg(WrObjPipePayload(isAffine=false))
    rWrObjPipeFrontPayload.init(doInitWrObjPipePayload(
      isAffine=false,
      firstInit=true
    ))

    val wrObjPipeFront = Flow(WrObjPipePayload(isAffine=false))
    wrObjPipeFront.valid := rWrObjPipeFrontValid
    wrObjPipeFront.payload := rWrObjPipeFrontPayload

    nWrObjArr(0).driveFrom(wrObjPipeFront)(
      con=(node, payload) => {
        //node(wrObjPipePayloadArr(0)) := payload
        //node(wrObjPipePayloadMain) := payload
        node(wrObjPipePayloadMain(0)) := payload
      },
    )

    //wrObjPipeIn(0).valid := rWrObjPipeFrontValid
    //wrObjPipeIn(0).payload := rWrObjPipeFrontPayload
    //for (idx <- 1 to wrObjPipeIn.size - 1) {
    //  // Create pipeline registering
    //  //when (idx - 1 != 6) {
    //    wrObjPipeIn(idx) <-< wrObjPipeOut(idx - 1)
    //    //wrObjPipeIn(idx) <-/< wrObjPipeOut(idx - 1)
    //  //} otherwise {
    //  //}
    //  //wrObjPipeIn(idx) <-/< wrObjPipeOut(idx - 1)
    //}
    //for (idx <- 0 to wrObjPipeOut.size - 1) {
    //  // Connect output `valid` to input `valid`
    //  wrObjPipeOut(idx).valid := wrObjPipeIn(idx).valid
    //  //wrObjPipeIn(idx).ready := wrObjPipeOut(idx).ready
    //}
    ////wrObjPipeOut(6).payload.setAsReg()
    //// add one final register
    //wrObjPipeLast <-< wrObjPipeOut.last
    rSavedWrObjPipeFrontValid := nextWrObjPipeFrontValid

    when (!rSavedWrObjPipeFrontValid) {
      when (
        intnlChangingRowRe
      ) {
        nextWrObjPipeFrontValid := True
        rWrObjPipeFrontPayload := doInitWrObjPipePayload(
          isAffine=false,
          firstInit=false
        )
      } otherwise {
        nextWrObjPipeFrontValid := rWrObjPipeFrontValid
      }
    } otherwise { // when (rSavedWrObjPipeFrontValid)
      when (
        nWrObjArr(0).isFiring
      ) {
        when (
          rWrObjPipeFrontPayload.bakCntWillBeDone
        ) {
          nextWrObjPipeFrontValid := False
        } otherwise {
          nextWrObjPipeFrontValid := rWrObjPipeFrontValid
          rWrObjPipeFrontPayload.cnt := (
            rWrObjPipeFrontPayload.cnt + 1
          )
          rWrObjPipeFrontPayload.bakCnt := (
            rWrObjPipeFrontPayload.bakCnt - 1
          )
          rWrObjPipeFrontPayload.bakCntMinus1 := (
            rWrObjPipeFrontPayload.bakCntMinus1 - 1
          )
          rWrObjPipeFrontPayload.stage0.rawAffineIdx() := (
            if (params.fancyObjPrio) {
              rWrObjPipeFrontPayload.stage0.rawAffineIdx() - 1
            } else {
              rWrObjPipeFrontPayload.stage0.rawAffineIdx() + 1
            }
          )
          rWrObjPipeFrontPayload.stage0.rawObjAttrsMemIdx() := (
            if (params.fancyObjPrio) {
              rWrObjPipeFrontPayload.stage0.rawObjAttrsMemIdx() - 1
            } else {
              rWrObjPipeFrontPayload.stage0.rawObjAttrsMemIdx() + 1
            }
          )
        }
      } otherwise { // when (!nWrObjArr(0).isFiring)
        nextWrObjPipeFrontValid := rWrObjPipeFrontValid
      }
    }

    //val wrObjAffinePipeLast = (!noAffineObjs) generate KeepAttribute(
    //  //Flow(WrObjPipePayload())
    //  Flow(WrObjPipePayload(isAffine=true))
    //)
    if (!noAffineObjs) {
      when (intnlChangingRowRe) {
        nextWrObjAffineChangingRow := False
      } elsewhen (
        //wrObjAffinePipeLast.bakCntWillBeDone && wrObjAffinePipeLast.fire
        wrObjAffinePipeLast.bakCntWillBeDone 
        && nWrObjAffinePipeLast.isFiring
      ) {
        nextWrObjAffineChangingRow := True
      } otherwise {
        nextWrObjAffineChangingRow := rWrObjAffineChangingRow
      }
    }
    val nextWrObjAffinePipeFrontValid = (!noAffineObjs) generate (
      Bool()
    )
    val rWrObjAffinePipeFrontValid = (!noAffineObjs) generate (
      RegNext(
        nextWrObjAffinePipeFrontValid
      )
      .init(True)
    )
    val rSavedWrObjAffinePipeFrontValid = (!noAffineObjs) generate (
      Reg(Bool()) init(False)
    )
    val rWrObjAffinePipeFrontPayload = (!noAffineObjs) generate Reg(
      WrObjPipePayload(isAffine=true)
    )
    if (!noAffineObjs) {
      rWrObjAffinePipeFrontPayload.init(doInitWrObjPipePayload(
        isAffine=true,
        firstInit=true
      ))

      val wrObjAffinePipeFront = Flow(WrObjPipePayload(isAffine=true))
      wrObjAffinePipeFront.valid := rWrObjAffinePipeFrontValid
      wrObjAffinePipeFront.payload := rWrObjAffinePipeFrontPayload

      nWrObjAffineArr(0).driveFrom(wrObjAffinePipeFront)(
        con=(node, payload) => {
          //node(wrObjAffinePipePayloadArr(0)) := payload
          node(wrObjAffinePipePayload) := payload
        },
      )

      //wrObjAffinePipeIn(0).valid := rWrObjAffinePipeFrontValid
      //wrObjAffinePipeIn(0).payload := rWrObjAffinePipeFrontPayload
      //for (idx <- 1 to wrObjAffinePipeIn.size - 1) {
      //  // Create pipeline registering
      //  wrObjAffinePipeIn(idx) <-< wrObjAffinePipeOut(idx - 1)
      //}
      //for (idx <- 0 to wrObjAffinePipeOut.size - 1) {
      //  // Connect output `valid` to input `valid`
      //  wrObjAffinePipeOut(idx).valid := wrObjAffinePipeIn(idx).valid
      //  //wrObjAffinePipeIn(idx).ready := wrObjAffinePipeOut(idx).ready
      //}
      ////wrObjAffinePipeOut(6).payload.setAsReg()
      //// add one final register
      //wrObjAffinePipeLast <-< wrObjAffinePipeOut.last
      rSavedWrObjAffinePipeFrontValid := nextWrObjAffinePipeFrontValid

      when (!rSavedWrObjAffinePipeFrontValid) {
        when (
          intnlChangingRowRe
        ) {
          nextWrObjAffinePipeFrontValid := True
          rWrObjAffinePipeFrontPayload := doInitWrObjPipePayload(
            isAffine=true,
            firstInit=false
          )
        } otherwise {
          nextWrObjAffinePipeFrontValid := rWrObjAffinePipeFrontValid
        }
      } otherwise { // when (rSavedWrObjAffinePipeFrontValid)
        when (
          //wrObjAffinePipeIn(0).fire
          nWrObjAffineArr(0).isFiring
        ) {
          when (
            rWrObjAffinePipeFrontPayload.bakCntWillBeDone
          ) {
            nextWrObjAffinePipeFrontValid := False
          } otherwise {
            nextWrObjAffinePipeFrontValid := rWrObjAffinePipeFrontValid
            rWrObjAffinePipeFrontPayload.cnt := (
              rWrObjAffinePipeFrontPayload.cnt + 1
            )
            rWrObjAffinePipeFrontPayload.bakCnt := (
              rWrObjAffinePipeFrontPayload.bakCnt - 1
            )
            rWrObjAffinePipeFrontPayload.bakCntMinus1 := (
              rWrObjAffinePipeFrontPayload.bakCntMinus1 - 1
            )
            rWrObjAffinePipeFrontPayload.stage0.rawAffineIdx() := (
              if (params.fancyObjPrio) {
                rWrObjAffinePipeFrontPayload.stage0.rawAffineIdx() - 1
              } else {
                rWrObjAffinePipeFrontPayload.stage0.rawAffineIdx() + 1
              }
            )
            rWrObjAffinePipeFrontPayload.stage0.rawObjAttrsMemIdx() := (
              if (params.fancyObjPrio) {
                (
                  rWrObjAffinePipeFrontPayload.stage0.rawObjAttrsMemIdx() 
                  - 1
                )
              } else {
                (
                  rWrObjAffinePipeFrontPayload.stage0.rawObjAttrsMemIdx() 
                  + 1
                )
              }
            )
          }
        } otherwise { // when (!wrObjAffinePipeIn(0).fire)
          nextWrObjAffinePipeFrontValid := rWrObjAffinePipeFrontValid
        }
      }
    }

    //val combinePipeIn = KeepAttribute(
    //  Vec.fill(
    //    combinePipeNumMainStages
    //    //anyPipeNumStages
    //  )(
    //    //Stream(CombinePipePayload())
    //    Stream(CombinePipePayload())
    //  )
    //)
    //val combinePipeOut = KeepAttribute(
    //  Vec.fill(
    //    combinePipeNumMainStages
    //    //anyPipeNumStages
    //  )(
    //    //Stream(CombinePipePayload())
    //    Stream(CombinePipePayload())
    //  )
    //)
    //val combinePipeLast = KeepAttribute(
    //  //Stream(CombinePipePayload())
    //  Stream(CombinePipePayload())
    //)
    //--------
    //def combineBgObjForkJoinMax = (
    //  if (!noAffineObjs) {
    //    3
    //  } else {
    //    2
    //  }
    //)
    //def combineTotalForkOrJoinMax = (
    //  params.numLineMemsPerBgObjRenderer * combineBgObjForkJoinMax
    //)
    def combineObjForkJoinMax = (
      if (!noAffineObjs) {
        2
      } else {
        1
      }
    )
    def combineBgObjForkJoinMax = (
      //if (!noAffineObjs) {
      //  3
      //} else {
      //  2
      //}
      combineObjForkJoinMax + 1
    )
    def combineTotalForkOrJoinMax = (
      (
        params.numLineMemsPerBgObjRenderer
        //* combineBgObjForkJoinMax
        * (
          //combineBgObjForkJoinMax - 1
          combineObjForkJoinMax
          //+ params.bgTileSize2d.x
          + params.combineBgSubLineMemArrSize
          //combineBgObjForkJoinMax
        )
      )
      //* params.bgTileSize2d.x
    )
    //println(s"combineTotalForkOrJoinMax: $combineTotalForkOrJoinMax")
    //--------
    //val combineBgObjRdPipeFork = FpgacpuPipeForkBlocking(
    //  dataType=CombinePipePayload(),
    //  // BEGIN: add in BGs; later
    //  oSize=params.numLineMemsPerBgObjRenderer * combineBgObjForkJoinMax,
    //  // END: add in BGs; later
    //)
    //val combineBgObjRdPipeJoin = FpgacpuPipeJoin(
    //  dataType=CombinePipePayload(),
    //  // BEGIN: add in BGs; later
    //  size=params.numLineMemsPerBgObjRenderer * combineBgObjForkJoinMax,
    //  // END: add in BGs; later
    //)
    val nCombineArr = Array.fill(combinePipeNumMainStages + 1)(Node())
    val sCombineArr = new ArrayBuffer[StageLink]()
    val s2mCombineArr = new ArrayBuffer[S2MLink]()
    val cCombineArr = new ArrayBuffer[CtrlLink]()

    val nfCombineArr = Array.fill(combineTotalForkOrJoinMax)(Node())
    val njCombineArr = Array.fill(combineTotalForkOrJoinMax)(Node())
    //val nfCombineBgA2d = (
    //  Array.fill(
    //    params.numLineMemsPerBgObjRenderer
    //  )(
    //    Array.fill(
    //      params.combineBgSubLineMemVecElemSize
    //      * params.combineBgSubLineMemArrSize
    //    )(
    //      Node()
    //    )
    //  )
    //)
    //val njCombineBgA2d = (
    //  Array.fill(
    //    params.numLineMemsPerBgObjRenderer
    //  )(
    //    Array.fill(
    //      params.combineBgSubLineMemVecElemSize
    //      * params.combineBgSubLineMemArrSize
    //    )(
    //      Node()
    //    )
    //  )
    //)
    //val cfCombineArr = new ArrayBuffer[CtrlLink]()
    val fDriveToStmArr = Array.fill(combineTotalForkOrJoinMax)(
      Stream(CombinePipePayload())
    )
    val cfjCombineArr = new ArrayBuffer[CtrlLink]()

    //val cCombineFork = new ArrayBuffer
    //val fCombineArr = new ArrayBuffer[ForkLink]()
    //val jCombineArr = new ArrayBuffer[JoinLink]()
    //var fMyCombine: ForkLink;
    //var jMyCombine: JoinLink;

    val combinePipePreForkPayload = Payload(CombinePipePayload())
    //val combinePostJoinPipePayloadPair = (
    //  Payload(CombinePipePayload()),
    //  Payload(CombinePipePayload()),
    //)
    //val combineJoinPipePayload = Payload(CombinePipePayload())
    val combinePipeJoinPayloadArr = (
      Array.fill(combineTotalForkOrJoinMax)(
        Payload(CombinePipePayload())
      )
    )
    val combinePipePostJoinPayload = Payload(CombinePipePayload())
    val combinePipeLast = nCombinePipeLast(
      //combinePipePayload
      combinePipePostJoinPayload
    )
    //val combinePipePayloadArr = new ArrayBuffer[CombinePipePayload]()
    def nCombinePipeLast = nCombineArr.last
    for (idx <- 0 until nCombineArr.size - 1) {
      //--------
      if (idx != 1) {
        //--------
        sCombineArr += StageLink(
          up=nCombineArr(idx),
          down=Node(),
        )
      } else { // if (idx == 1)
        //--------
        //def theMax = (
        //  if (!noAffineObjs) {
        //    3
        //  } else {
        //    2
        //  }
        //)
        def theMax = combineObjForkJoinMax //combineBgObjForkJoinMax - 1
        //def theMax = combineBgObjForkJoinMax * params.bgTileSize2d.x
        val fMyCombine = ForkLink(
          up=nCombineArr(idx),
          downs=nfCombineArr.toSeq,
          synchronous=true,
        )
        linkArr += fMyCombine
        val jMyCombine = JoinLink(
          ups=njCombineArr.toSeq,
          down=Node(),
        )
        linkArr += jMyCombine
        //for (
        //  kdx <- 0 until params.bgTileSize2d.x
        //) {
          for (
            jdx <- 0
            //until params.numLineMemsPerBgObjRenderer * theMax
            until combineTotalForkOrJoinMax
          ) {
            def zdx = (
              jdx
              - (
                params.numLineMemsPerBgObjRenderer
                * (
                  //params.bgTileSize2d.x
                  params.combineBgSubLineMemArrSize
                )
              )
            )
            //def combineIdx = (
            //  jdx * params.bgTileSize2d.x + kdx
            //)
            //combineObjSubLineMemArr(combineIdx).io.rdAddrPipe << (
            //  combineBgObjRdPipeFork.io.pipeOutVec//(combineIdx)
            //);
            //println("combineIdx: " + f"$combineIdx")
            def nfMyCombine = (
              //combineBgObjRdPipeFork.io.pipeOutVec(combineIdx)
              fMyCombine.downs(jdx)
            )
            def njMyCombine = (
              //combineBgObjRdPipeJoin.io.pipeInVec(combineIdx)
              //njCombineArr(combineIdx)
              jMyCombine.ups(jdx)
            )
            //val cfjMyCombine = CtrlLink(
            //  //up=nfMyCombine,
            //  //down=njMyCombine,
            //  up=nfMyCombine,
            //  down=njMyCombine,
            //)
            //linkArr += cfjMyCombine
            //def cfjMyCombine = (
            //  cfjCombineArr.last
            //)
            def fMyDriveToStm = (
              fDriveToStmArr(jdx)
            )
            nfMyCombine.driveTo(fMyDriveToStm)(
              con=(
                payload,
                node,
              ) => {
                payload := node(combinePipePreForkPayload)
              }
            )

            if (
              //combineIdx % theMax == 0
              false
            ) {
              //def myLineMem = (
              //  combineBgSubLineMemA2d(combineIdx / theMax)
              //)
              //fMyDriveToStm.translateInto(myLineMem.io.dualRdFront)(
              //  dataAssignment=(
              //    o, i
              //  ) => {
              //    o := i
              //    o.allowOverride
              //    o.bgExt := o.bgExt.getZero
              //    o.bgExt.memAddr := (
              //      params.getBgSubLineMemArrIdx(
              //        addr=i.cnt,
              //      )
              //    )
              //  }
              //)
              //njMyCombine.driveFrom(myLineMem.io.dualRdBack)(
              //  con=(
              //    node,
              //    payload,
              //  ) => {
              //    //node(combinePipePayload) := payload
              //    //node(combinePipePostJoinPayload) := payload
              //    def o = node(combinePipeJoinPayloadArr(combineIdx))
              //    def i = payload
              //    o := i
              //    combineBgSetWordFunc(
              //      unionIdx=0,
              //      outpPayload=o,
              //      inpPayload=i,
              //      bgTileRow=i.bgExt.modMemWord,
              //    )
              //    //--------
              //    //node(combinePipeJoinPayloadArr(combineIdx)) := payload
              //    //--------
              //    //node(combineJoinPipePayload) := payload
              //    //def myPayload = node(combinePipePostJoinPayload)
              //    //myPayload := payload
              //    //myPayload.stage2.allowOverride
              //  }
              //)
            } else {
              def myLineMem = {
                if (
                  //zdx % theMax1 == 0
                  zdx < 0
                  //jdx < 
                ) {
                  //println("combineIdx % theMax == 0: " + f"$combineIdx")
                  //combineBgSubLineMemArr(combineIdx / theMax)
                  //val tempJdx = jdx / theMax
                  val kdx = (
                    jdx / params.numLineMemsPerBgObjRenderer
                  )
                  val tempJdx = (
                    jdx % params.numLineMemsPerBgObjRenderer
                  )
                  val tempSize = combineBgSubLineMemA2d(
                    //kdx
                    tempJdx
                  ).size
                  //println(
                  //  s"kdx, tempJdx, tempSize: $kdx $tempJdx $tempSize"
                  //)
                  //println(combineBgSubLineMemA2d.size)
                  combineBgSubLineMemA2d(
                    //kdx
                    tempJdx
                  )(
                    //tempJdx
                    kdx
                  )
                  //combineObjSubLineMemAr//r(combineIdx / theMax)
                } else {
                  if (zdx % theMax == 0) {
                    //println("combineIdx % theMax == 1: " + f"$combineIdx")
                    combineObjSubLineMemArr(zdx / theMax)
                  } else {
                    //println("combineIdx % theMax >= 2: " + f"$combineIdx")
                    if (!noAffineObjs) {
                      combineObjAffineSubLineMemArr(zdx / theMax)
                    } else {
                      combineObjSubLineMemArr(zdx / theMax)
                    }
                  }
                }
              }
              //if (
              //  //(
              //  //  jdx % theMax1 == 0
              //  //) || (
              //  //  kdx == 0
              //  //  && (
              //  //    (jdx % theMax1 == 1)
              //  //    || (jdx % theMax == 2)
              //  //  )
              //  //)
              //) {
                fMyDriveToStm.translateInto(myLineMem.io.rdAddrPipe)(
                  dataAssignment=(
                    o,
                    i,
                  ) => {
                    //o.addr := i.cnt(
                    //  log2Up(params.objSubLineMemArrSize) - 1 downto 0
                    //)
                    o.addr := (
                      if (
                        //jdx % theMax1 == 0
                        zdx < 0
                      ) {
                        params.getBgSubLineMemArrIdx(
                          addr=i.cnt,
                        )
                        //params.getObjSubLineMemArrIdx(
                        //  addr=i.cnt,
                        //)
                      } else {
                        if (zdx % theMax == 0) {
                          params.getObjSubLineMemArrIdx(
                            addr=i.cnt,
                          )
                        } else {
                          if (!noAffineObjs) { // if (zdx % theMax1 == 2)
                            params.getObjAffineSubLineMemArrIdx(
                              addr=i.cnt,
                            )
                          } else {
                            params.getObjSubLineMemArrIdx(
                              addr=i.cnt,
                            )
                          }
                        }
                      }
                    )
                    o.data := i
                  }
                )

                //njMyCombine << myLineMem.io.rdDataPipe
                njMyCombine.driveFrom(myLineMem.io.rdDataPipe)(
                  con=(
                    node,
                    payload,
                  ) => {
                    //node(combinePipePayload) := payload
                    //node(combinePipePostJoinPayload) := payload
                    node(combinePipeJoinPayloadArr(jdx)) := payload
                    //node(combineJoinPipePayload) := payload
                    //def myPayload = node(combinePipePostJoinPayload)
                    //myPayload := payload
                    //myPayload.stage2.allowOverride
                  }
                )
              //}
            }
          }

          //--------
          sCombineArr += StageLink(
            //up=nCombineArr(idx),
            up=jMyCombine.down,
            down=Node(),
          )
          switch (combineLineMemArrIdx) {
            for (
              innerCombineIdx <- 0
              until params.numLineMemsPerBgObjRenderer
            ) {
              is (innerCombineIdx) {
                //--------
                //val myPayload = CombinePipePayload()
                def myOutpPayload = (
                  jMyCombine.down(combinePipePostJoinPayload)
                )
                def myInpPayload(inpIdx: Int) = {
                  jMyCombine.down(combinePipeJoinPayloadArr(inpIdx))
                }
                myOutpPayload.allowOverride
                myOutpPayload := myInpPayload(0)
                //def myOutpPayload = 
                //cfjMyCombine.bypass(combinePipePostJoinPayload) := (
                //  myOutpPayload
                //)
                //if (zdx < 0) {
                //for (jdx <- 0 until params.bgTileSize2d.x) {
                //  myOutpPayload.stage2.rdBg(jdx) := (
                //    //combineBgObjRdPipeJoin.io.pipeOut.payload(
                //    //  innerCombineIdx * theMax //+ 0
                //    //)
                //    //  .stage2.rdBg
                //    myInpPayload(
                //      (
                //        jdx 
                //        * params.numLineMemsPerBgObjRenderer
                //      )
                //      + (
                //        innerCombineIdx * theMax //+ 0
                //      )
                //    )
                //      .stage2.rdBg(jdx)
                //  )
                //}
                //for (
                //  jdx <- 0 until params.combineBgSubLineMemArrSize
                //) {
                //  for (
                //    kdx <- 0 until params.combineBgSubLineMemVecElemSize
                //  ) {
                //    myOutpPayload.stage2.rdBg(
                //      jdx * params.combineBgSubLineMemVecElemSize + kdx
                //    ) := (
                //      myInpPayload(
                //        (
                //          //jdx 
                //          (
                //            jdx * params.combineBgSubLineMemVecElemSize
                //            + kdx
                //          )
                //          * params.numLineMemsPerBgObjRenderer
                //        ) + (
                //          innerCombineIdx * theMax //+ 0
                //        )
                //      )
                //        .stage2.rdBg(kdx)
                //    )
                //  }
                //}
                for (
                  jdx <- 0 until params.bgTileSize2d.x
                ) {
                  def kdx = (
                    jdx
                    / (
                      params.combineBgSubLineMemVecElemSize
                      //params.numLineMemsPerBgObjRenderer
                    )
                  )
                  val tempKdx = (
                    (
                      //jdx 
                      kdx
                      * (
                        //params.combineBgSubLineMemVecElemSize
                        params.numLineMemsPerBgObjRenderer

                        //params.combineBgSubLineMemVecElemSize
                      )
                    )
                    + innerCombineIdx //* theMax
                  )
                  //println(s"jdx, kdx, tempKdx: ${jdx} ${kdx} ${tempKdx}")
                  //println(
                  //  s"rdBg: $innerCombineIdx $jdx $kdx $tempKdx"
                  //)
                  myOutpPayload.stage2.rdBg(jdx) := (
                    myInpPayload(
                      //tempJdx
                      tempKdx
                    )
                      .stage2.rdBg(jdx)
                  )
                }
                //combinePipeOut(idx).payload.stage2.rdObj.allowOverride
                myOutpPayload.stage2.rdObj := {
                  val tempIndex = (
                    (
                      //params.bgTileSize2d.x 
                      params.combineBgSubLineMemArrSize
                      * params.numLineMemsPerBgObjRenderer
                    ) + (
                      innerCombineIdx * theMax //+ 1
                    )
                  )
                  //println(
                  //  s"rdObj: ${innerCombineIdx} ${tempIndex}"
                  //)
                  myInpPayload(
                    tempIndex
                  ).stage2.rdObj
                }
                if (!noAffineObjs) {
                  myOutpPayload.stage2.rdObjAffine := {
                    val tempIndex = (
                      (
                        //params.bgTileSize2d.x 
                        params.combineBgSubLineMemArrSize
                        * params.numLineMemsPerBgObjRenderer
                      ) + (
                        innerCombineIdx * theMax + 1 //+ 2
                      )
                    )
                    //println(
                    //  s"rdObjAffine: ${innerCombineIdx} ${tempIndex}"
                    //)
                    myInpPayload(
                      tempIndex
                    ).stage2.rdObjAffine
                  }
                }
                //--------

                //combinePipeOut(idx).payload.stage2.rdBg := (
                //  combineBgObjRdPipeJoin.io.pipeOut.payload(
                //    innerCombineIdx * theMax //+ 0
                //  )
                //    .stage2.rdBg
                //)
                ////combinePipeOut(idx).payload.stage2.rdObj.allowOverride
                //combinePipeOut(idx).payload.stage2.rdObj := (
                //  combineBgObjRdPipeJoin.io.pipeOut.payload(
                //    innerCombineIdx * theMax + 1
                //  ).stage2.rdObj
                //)
                //if (!noAffineObjs) {
                //  combinePipeOut(idx).payload.stage2.rdObjAffine := (
                //    combineBgObjRdPipeJoin.io.pipeOut.payload(
                //      innerCombineIdx * theMax + 2
                //    ).stage2.rdObjAffine
                //  )
                //}
                // END: add in BGs; later
              }
            }
          }
        //}
      }
      //--------
      linkArr += sCombineArr.last

      s2mCombineArr += S2MLink(
        up=sCombineArr.last.down,
        down=Node(),
      )
      linkArr += s2mCombineArr.last

      cCombineArr += CtrlLink(
        up=s2mCombineArr.last.down,
        down=nCombineArr(idx + 1),
      )
      linkArr += cCombineArr.last
      //--------
    }
    //for (idx <- 0 until nCombineArr.size - 1) {
    //  //--------
    //  if (idx != 1) {
    //    //--------
    //    sCombineArr += StageLink(
    //      up=nCombineArr(idx),
    //      down=Node(),
    //    )
    //  } else { // if (idx == 1)
    //    //--------
    //    //def theMax = (
    //    //  if (!noAffineObjs) {
    //    //    3
    //    //  } else {
    //    //    2
    //    //  }
    //    //)
    //    def theMax = combineBgObjForkJoinMax
    //    val fMyCombine = ForkLink(
    //      up=nCombineArr(idx),
    //      downs=nfCombineArr.toSeq,
    //      synchronous=true,
    //    )
    //    linkArr += fMyCombine
    //    val jMyCombine = JoinLink(
    //      ups=njCombineArr.toSeq,
    //      down=Node(),
    //    )
    //    linkArr += jMyCombine
    //    for (
    //      combineIdx <- 0
    //      //until params.numLineMemsPerBgObjRenderer * theMax
    //      until combineTotalForkOrJoinMax
    //    ) {
    //      //combineObjSubLineMemArr(combineIdx).io.rdAddrPipe << (
    //      //  combineBgObjRdPipeFork.io.pipeOutVec//(combineIdx)
    //      //);
    //      //println("combineIdx: " + f"$combineIdx")
    //      def nfMyCombine = (
    //        //combineBgObjRdPipeFork.io.pipeOutVec(combineIdx)
    //        fMyCombine.downs(combineIdx)
    //      )
    //      def njMyCombine = (
    //        //combineBgObjRdPipeJoin.io.pipeInVec(combineIdx)
    //        //njCombineArr(combineIdx)
    //        jMyCombine.ups(combineIdx)
    //      )
    //      //val cfjMyCombine = CtrlLink(
    //      //  //up=nfMyCombine,
    //      //  //down=njMyCombine,
    //      //  up=nfMyCombine,
    //      //  down=njMyCombine,
    //      //)
    //      //linkArr += cfjMyCombine
    //      //def cfjMyCombine = (
    //      //  cfjCombineArr.last
    //      //)
    //      def fMyDriveToStm = (
    //        fDriveToStmArr(combineIdx)
    //      )
    //      nfMyCombine.driveTo(fMyDriveToStm)(
    //        con=(
    //          payload,
    //          node,
    //        ) => {
    //          payload := node(combinePipePreForkPayload)
    //        }
    //      )

    //      if (
    //        //combineIdx % theMax == 0
    //        false
    //      ) {
    //        def myLineMem = (
    //          combineBgSubLineMemArr(combineIdx / theMax)
    //        )
    //        //fMyDriveToStm.translateInto(myLineMem.io.dualRdFront)(
    //        //  dataAssignment=(
    //        //    o, i
    //        //  ) => {
    //        //    o := i
    //        //    o.allowOverride
    //        //    o.bgExt := o.bgExt.getZero
    //        //    o.bgExt.memAddr := (
    //        //      params.getBgSubLineMemArrIdx(
    //        //        addr=i.cnt,
    //        //      )
    //        //    )
    //        //  }
    //        //)
    //        //njMyCombine.driveFrom(myLineMem.io.dualRdBack)(
    //        //  con=(
    //        //    node,
    //        //    payload,
    //        //  ) => {
    //        //    //node(combinePipePayload) := payload
    //        //    //node(combinePipePostJoinPayload) := payload
    //        //    def o = node(combinePipeJoinPayloadArr(combineIdx))
    //        //    def i = payload
    //        //    o := i
    //        //    combineBgSetWordFunc(
    //        //      unionIdx=0,
    //        //      outpPayload=o,
    //        //      inpPayload=i,
    //        //      bgTileRow=i.bgExt.modMemWord,
    //        //    )
    //        //    //--------
    //        //    //node(combinePipeJoinPayloadArr(combineIdx)) := payload
    //        //    //--------
    //        //    //node(combineJoinPipePayload) := payload
    //        //    //def myPayload = node(combinePipePostJoinPayload)
    //        //    //myPayload := payload
    //        //    //myPayload.stage2.allowOverride
    //        //  }
    //        //)
    //      } else {
    //        def myLineMem = {
    //          if (combineIdx % theMax == 0) {
    //            //println("combineIdx % theMax == 0: " + f"$combineIdx")
    //            combineBgSubLineMemArr(combineIdx / theMax)
    //            //combineObjSubLineMemAr//r(combineIdx / theMax)
    //          } else if (combineIdx % theMax == 1) {
    //            //println("combineIdx % theMax == 1: " + f"$combineIdx")
    //            combineObjSubLineMemArr(combineIdx / theMax)
    //          } else {
    //            //println("combineIdx % theMax >= 2: " + f"$combineIdx")
    //            if (!noAffineObjs) {
    //              combineObjAffineSubLineMemArr(combineIdx / theMax)
    //            } else {
    //              combineObjSubLineMemArr(combineIdx / theMax)
    //            }
    //          }
    //        }
    //        fMyDriveToStm.translateInto(myLineMem.io.rdAddrPipe)(
    //          dataAssignment=(
    //            o,
    //            i,
    //          ) => {
    //            //o.addr := i.cnt(
    //            //  log2Up(params.objSubLineMemArrSize) - 1 downto 0
    //            //)
    //            o.addr := (
    //              if (combineIdx % theMax == 0) {
    //                params.getBgSubLineMemArrIdx(
    //                  addr=i.cnt,
    //                )
    //                //params.getObjSubLineMemArrIdx(
    //                //  addr=i.cnt,
    //                //)
    //              } else if (combineIdx % theMax == 1) {
    //                params.getObjSubLineMemArrIdx(
    //                  addr=i.cnt,
    //                )
    //              } else {
    //                if (!noAffineObjs) { // if (combineIdx % 3 == 2)
    //                  params.getObjAffineSubLineMemArrIdx(
    //                    addr=i.cnt,
    //                  )
    //                } else {
    //                  params.getObjSubLineMemArrIdx(
    //                    addr=i.cnt,
    //                  )
    //                }
    //              }
    //            )
    //            o.data := i
    //          }
    //        )

    //        //njMyCombine << myLineMem.io.rdDataPipe
    //        njMyCombine.driveFrom(myLineMem.io.rdDataPipe)(
    //          con=(
    //            node,
    //            payload,
    //          ) => {
    //            //node(combinePipePayload) := payload
    //            //node(combinePipePostJoinPayload) := payload
    //            node(combinePipeJoinPayloadArr(combineIdx)) := payload
    //            //node(combineJoinPipePayload) := payload
    //            //def myPayload = node(combinePipePostJoinPayload)
    //            //myPayload := payload
    //            //myPayload.stage2.allowOverride
    //          }
    //        )
    //      }

    //      //--------
    //      sCombineArr += StageLink(
    //        //up=nCombineArr(idx),
    //        up=jMyCombine.down,
    //        down=Node(),
    //      )
    //      switch (combineLineMemArrIdx) {
    //        for (
    //          innerCombineIdx <- 0
    //          until params.numLineMemsPerBgObjRenderer
    //        ) {
    //          is (innerCombineIdx) {
    //            // BEGIN: add in BGs; later
    //            //combinePipeOut(idx).payload := (
    //            //  combineBgObjRdPipeJoin.io.pipeOut.payload(innerCombineIdx * 2)
    //            //)
    //            //def theMax = (
    //            //  if (!noAffineObjs) {
    //            //    3
    //            //  } else {
    //            //    2
    //            //  }
    //            //)
    //            //--------
    //            //val myPayload = CombinePipePayload()
    //            def myOutpPayload = (
    //              jMyCombine.down(combinePipePostJoinPayload)
    //            )
    //            def myInpPayload(inpIdx: Int) = {
    //              jMyCombine.down(combinePipeJoinPayloadArr(inpIdx))
    //            }
    //            myOutpPayload.allowOverride
    //            myOutpPayload := myInpPayload(0)
    //            //def myOutpPayload = 
    //            //cfjMyCombine.bypass(combinePipePostJoinPayload) := (
    //            //  myOutpPayload
    //            //)
    //            myOutpPayload.stage2.rdBg := (
    //              //combineBgObjRdPipeJoin.io.pipeOut.payload(
    //              //  innerCombineIdx * theMax //+ 0
    //              //)
    //              //  .stage2.rdBg
    //              myInpPayload(
    //                innerCombineIdx * theMax //+ 0
    //              )
    //                .stage2.rdBg
    //            )
    //            //combinePipeOut(idx).payload.stage2.rdObj.allowOverride
    //            myOutpPayload.stage2.rdObj := (
    //              myInpPayload(
    //                innerCombineIdx * theMax + 1
    //              ).stage2.rdObj
    //            )
    //            if (!noAffineObjs) {
    //              myOutpPayload.stage2.rdObjAffine := (
    //                myInpPayload(
    //                  innerCombineIdx * theMax + 2
    //                ).stage2.rdObjAffine
    //              )
    //            }
    //            //--------

    //            //combinePipeOut(idx).payload.stage2.rdBg := (
    //            //  combineBgObjRdPipeJoin.io.pipeOut.payload(
    //            //    innerCombineIdx * theMax //+ 0
    //            //  )
    //            //    .stage2.rdBg
    //            //)
    //            ////combinePipeOut(idx).payload.stage2.rdObj.allowOverride
    //            //combinePipeOut(idx).payload.stage2.rdObj := (
    //            //  combineBgObjRdPipeJoin.io.pipeOut.payload(
    //            //    innerCombineIdx * theMax + 1
    //            //  ).stage2.rdObj
    //            //)
    //            //if (!noAffineObjs) {
    //            //  combinePipeOut(idx).payload.stage2.rdObjAffine := (
    //            //    combineBgObjRdPipeJoin.io.pipeOut.payload(
    //            //      innerCombineIdx * theMax + 2
    //            //    ).stage2.rdObjAffine
    //            //  )
    //            //}
    //            // END: add in BGs; later
    //          }
    //        }
    //      }
    //    }
    //  }
    //  //--------
    //  linkArr += sCombineArr.last

    //  s2mCombineArr += S2MLink(
    //    up=sCombineArr.last.down,
    //    down=Node(),
    //  )
    //  linkArr += s2mCombineArr.last

    //  cCombineArr += CtrlLink(
    //    up=s2mCombineArr.last.down,
    //    down=nCombineArr(idx + 1),
    //  )
    //  linkArr += cCombineArr.last
    //  //--------
    //}
    def initCombinePipeOut(
      idx: Int,
      payload: Payload[CombinePipePayload],
      //tempOutp: CombinePipePayload,
    ): (CombinePipePayload, CombinePipePayload) = {
      // This function returns `(tempInp, tempOutp)`
      val pipeIn = (
        //cCombineArr(idx).down(combinePipePayload)
        //cCombineArr(idx).up(combinePipePostJoinPayload)
        cCombineArr(idx).up(payload)
        //nCombineArr(idx)(combinePipePayload)
      )
      val pipeOut = CombinePipePayload()

      pipeOut := pipeIn
      pipeOut.allowOverride
      cCombineArr(idx).bypass(payload) := pipeOut

      (pipeIn, pipeOut)
    }
    def initCombinePostJoinPipeOut(
      idx: Int,
    ): (CombinePipePayload, CombinePipePayload) = {
      initCombinePipeOut(
        idx=idx,
        payload=combinePipePostJoinPayload,
      )
    }

    //--------
    // The logic is identical, so we only need one `valid` or `ready`
    // signal for the below two assignments
    //combinePipeIn(1).ready := combinePipeIn1BgVec(
    //  rCombineFullLineMemArrIdx
    //).ready
    //combinePipeOut(1).valid := combinePipeOut1BgVec(
    //  rCombineFullLineMemArrIdx
    //).valid
    //--------
    nextCombineChangingRow := (
      combinePipeLast.changingRow
      //&& combinePipeLast.fullBakCntWillBeDone
    )

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
      //prevLineMemArrIdx=combineFullLineMemArrIdxInit
    ))

    val haltCombinePipeVeryFront = Bool()
    val combinePipeInVeryFront = Stream(CombinePipePayload())
    val combinePipeInVeryFrontHaltWhen = combinePipeInVeryFront.haltWhen(
      haltCombinePipeVeryFront
    )
    ////combinePipeIn(0) << combinePipeInVeryFrontHaltWhen
    //combinePipeIn(0) <-/< combinePipeInVeryFrontHaltWhen
    ////combinePipeIn(0).valid := rCombinePipeFrontValid
    ////combinePipeIn(0).payload := rCombinePipeFrontPayload
    combinePipeInVeryFront.valid := (
      rCombinePipeFrontValid //&& !haltCombinePipeVeryFront
    )
    combinePipeInVeryFront.payload := rCombinePipeFrontPayload
    nCombineArr(0).driveFrom(
      //combinePipeInVeryFrontHaltWhen
      //combinePipeInVeryFront
      combinePipeInVeryFrontHaltWhen
    )(
      con=(node, payload) => {
        node(combinePipePreForkPayload) := payload
      }
    )

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
    // BEGIN: old combine code
    //for (idx <- 0 until combinePipeIn.size) {
    //  //psbCombinePipeArr += PipeSkidBuf(
    //  //  dataType=CombinePipePayload(),
    //  //  //optIncludeBusy=(idx == 2),
    //  //)
    //  //def psb = psbCombinePipeArr(idx)

    //  //if (idx == 2) {
    //  //  psb.io.misc.busy := haltCombinePipe2
    //  //  //psb.io.misc.busy := False
    //  //}
    //  if (idx > 0) {
    //    // `valid`, `payload`, and `ready` are all cut by a register
    //    // stage
    //    combinePipeIn(idx) <-/< combinePipeOut(idx - 1)
    //    //--------
    //  }
    //  if (
    //    idx
    //    != 1
    //    //!= 2
    //  ) {
    //    combinePipeOut(idx).valid := combinePipeIn(idx).valid
    //    combinePipeIn(idx).ready := combinePipeOut(idx).ready
    //  } else {
    //    //combinePipeOut(idx)
    //    combineBgObjRdPipeFork.io.pipeIn << combinePipeIn(idx)
    //    def theMax = (
    //      if (!noAffineObjs) {
    //        3
    //      } else {
    //        2
    //      }
    //    )
    //    for (
    //      combineIdx <- 0
    //      until params.numLineMemsPerBgObjRenderer * theMax
    //    ) {
    //      //combineObjSubLineMemArr(combineIdx).io.rdAddrPipe << (
    //      //  combineBgObjRdPipeFork.io.pipeOutVec//(combineIdx)
    //      //);
    //      def myLineMem = {
    //        
    //        if (combineIdx % theMax == 0) {
    //          combineBgSubLineMemArr(combineIdx / theMax)
    //        } else if (combineIdx % theMax == 1) {
    //          combineObjSubLineMemArr(combineIdx / theMax)
    //        } else {
    //          if (!noAffineObjs) {
    //            combineObjAffineSubLineMemArr(combineIdx / theMax)
    //          } else {
    //            combineObjSubLineMemArr(combineIdx / theMax)
    //          }
    //        }
    //      }
    //      def myForkPipeOut = (
    //        combineBgObjRdPipeFork.io.pipeOutVec(combineIdx)
    //      )
    //      def myJoinPipeIn = (
    //        combineBgObjRdPipeJoin.io.pipeInVec(combineIdx)
    //      )
    //      myForkPipeOut.translateInto(
    //        myLineMem.io.rdAddrPipe
    //      )(
    //        dataAssignment=(
    //          o,
    //          i,
    //        ) => {
    //          //o.addr := i.cnt(
    //          //  log2Up(params.objSubLineMemArrSize) - 1 downto 0
    //          //)
    //          o.addr := (
    //            if (combineIdx % theMax == 0) {
    //              params.getBgSubLineMemArrIdx(
    //                addr=i.cnt,
    //              )
    //            } else if (combineIdx % theMax == 1) {
    //              params.getObjSubLineMemArrIdx(
    //                addr=i.cnt,
    //              )
    //            } else {
    //              if (!noAffineObjs) { // if (combineIdx % 3 == 2)
    //                params.getObjAffineSubLineMemArrIdx(
    //                  addr=i.cnt,
    //                )
    //              } else {
    //                params.getObjSubLineMemArrIdx(
    //                  addr=i.cnt,
    //                )
    //              }
    //            }
    //          )
    //          o.data := i
    //        }
    //      )
    //      myJoinPipeIn << myLineMem.io.rdDataPipe
    //    }
    //    combinePipeOut(idx).valid := (
    //      combineBgObjRdPipeJoin.io.pipeOut.valid
    //    )
    //    combineBgObjRdPipeJoin.io.pipeOut.ready := (
    //      combinePipeOut(idx).ready
    //    )
    //    switch (rCombineFullLineMemArrIdx) {
    //      for (
    //        combineIdx <- 0 until params.numLineMemsPerBgObjRenderer
    //      ) {
    //        is (combineIdx) {
    //          // BEGIN: add in BGs; later
    //          //combinePipeOut(idx).payload := (
    //          //  combineBgObjRdPipeJoin.io.pipeOut.payload(combineIdx * 2)
    //          //)
    //          def theMax = (
    //            if (!noAffineObjs) {
    //              3
    //            } else {
    //              2
    //            }
    //          )
    //          combinePipeOut(idx).payload.stage2.rdBg := (
    //            combineBgObjRdPipeJoin.io.pipeOut.payload(
    //              combineIdx * theMax //+ 0
    //            )
    //              .stage2.rdBg
    //          )
    //          //combinePipeOut(idx).payload.stage2.rdObj.allowOverride
    //          combinePipeOut(idx).payload.stage2.rdObj := (
    //            combineBgObjRdPipeJoin.io.pipeOut.payload(
    //              combineIdx * theMax + 1
    //            ).stage2.rdObj
    //          )
    //          if (!noAffineObjs) {
    //            combinePipeOut(idx).payload.stage2.rdObjAffine := (
    //              combineBgObjRdPipeJoin.io.pipeOut.payload(
    //                combineIdx * theMax + 2
    //              ).stage2.rdObjAffine
    //            )
    //          }
    //          // END: add in BGs; later
    //        }
    //      }
    //    }
    //    //for (lineMemIdx <- 0 until combineObjSubLineMemArr.size) {
    //    //  def myLineMem = combineObjSubLineMemArr(lineMemIdx)
    //    //  myLineMem.io.rdAddrPipe.valid := combinePipeIn(idx).valid
    //    //  myLineMem.io.rdAddrPipe.data := combinePipeIn(idx)
    //    //  myLineMem.io.rdAddrPipe.addr := params.getObjSubLineMemArrIdx(
    //    //    addr=combinePipeIn(idx).cnt,
    //    //  )
    //    //  myLineMem.io.rdDataPipe.ready := combinePipeOut(idx).ready
    //    //}
    //    //switch (rCombineFullLineMemArrIdx) {
    //    //  for (combineIdx <- 0 until params.numLineMemsPerBgObjRenderer) {
    //    //    def myLineMem = combineObjSubLineMemArr(combineIdx)
    //    //    is (combineIdx) {
    //    //      combinePipeIn(idx).ready := myLineMem.io.rdAddrPipe.ready
    //    //      combinePipeOut(idx).valid := myLineMem.io.rdDataPipe.valid
    //    //      combinePipeOut(idx).payload := (
    //    //        myLineMem.io.rdDataPipe.payload
    //    //      )
    //    //    }
    //    //  }
    //    //}
    //  }
    //}
    // END: old combine code
    //--------

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

    //--------
    // add one final register
    //combinePipeLast <-< combinePipeOut.last
    //combinePipeLast <-/< combinePipeOut.last
    //--------
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
    //val rCombineScaleXCnt = (
    //  KeepAttribute(
    //    Reg(
    //      SInt(log2Up(params.physFbSize2dScale.x) + 2 bits)
    //    ) init(params.physFbSize2dScale.x - 2)
    //  )
    //  //.addAttribute("keep")
    //  //.setName("rCombineScaleXCnt")
    //)
    val rPrevCombinePipeLastFullCnt = Reg(
      //SInt(combinePipeCntWidth bits)
      UInt(combinePipeFullCntWidth bits)
      //rCombineScaleXCnt.msb,
    )
      .init(
        //S(combinePipeCntWidth bits, default -> True)
        U(combinePipeFullCntWidth bits, default -> True)
      )
    //val rPrevCombinePipeLastFracCnt = (
    //  //Reg(Bool()) init(False)
    //  Reg(SInt(log2Up(params.physFbSize2dScale.x) + 3 bits))
    //  init(params.physFbSize2dScale.x - 2)
    //)
    //val rPrevCombinePipeLastScaleXCnt = (
    //  //Reg(Bool()) init(False)
    //  Reg(
    //    cloneOf(rCombineScaleXCnt)
    //  ) init(params.physFbSize2dScale.x - 2)
    //)
    val combinePipeLastPreThrown = Stream(CombinePipePayload())
    val combinePipeLastPreThrownDoThrow = KeepAttribute(
      ///*Cat*/(
      //  rPrevCombinePipeLastFullCnt.asUInt,
      //  //rPrevCombinePipeLastFracCnt
      //)/*.asUInt*/ === /*Cat*/(
      //  combinePipeLastPreThrown.fullCnt,
      //  //combinePipeLastPreThrown.fracCnt,
      //)/*.asUInt*/
      //&& (
      //  rPrevCombinePipeLastFracCnt
      //  === combinePipeLastPreThrown.fracCnt
      //)
      //rPrevCombinePipeLastExtCnt.asUInt
      //=== 
      //True
      //False
      rPrevCombinePipeLastFullCnt.resized
      === combinePipeLastPreThrown.fullCnt
      //&& pop.ready
    )
    val combinePipeLastThrown = (
      //Stream(CombinePipePayload())
      combinePipeLastPreThrown.throwWhen(
        combinePipeLastPreThrownDoThrow
        //(rCombineScaleXCnt - 1).msb
        ///*RegNext*/(rCombineScaleXCnt.msb)
        //(
        //  //rPrevCombinePipeLast.stage0.scaleXCnt.msb
        //  //rPrevCombinePipeLastScaleXCnt
        //  //=== combinePipeLastPreThrown.stage0.scaleXCnt
        //  rPrevCombinePipeLastScaleXCnt.msb
        //) && (
        //  combinePipeLastPreThrown.stage0.scaleXCnt
        //  === params.physFbSize2dScale.x - 2
        //) && (
        //)
        //--------
        //(
        //  //rPrevCombinePipeLastCnt.asUInt === combinePipeLastPreThrown.cnt
        //  rPrevCombinePipeLastFullCnt.asUInt
        //  === combinePipeLastPreThrown.fullCnt
        //  //True
        //) && (
        //  //rPrevCombinePipeLastFracCnt.msb
        //  //&& (
        //  //  combinePipeLastPreThrown.fracCnt
        //  //  === params.physFbSize2dScale.x - 2
        //  //)
        //  //--------
        //  //&& rPrevCombinePipeLastFracCnt.msb
        //  //&& combinePipeLastPreThrown.fracCnt.msb
        //  //--------
        //  //rPrevCombinePipeLastFracCnt
        //  //=== combinePipeLastPreThrown.fracCnt
        //  rPrevCombinePipeLastFracCnt === 0x0
        //  && combinePipeLastPreThrown.fracCnt.msb
        //  //rPrevCombinePipeLastFracCnt.msb
        //  //&& !combinePipeLastPreThrown.fracCnt.msb
        //  //True
        //)
        //--------
        //|| (
        //  !pop.ready
        //)
      )
      //combinePipeLastPreThrown
    )
    //combinePipeLastThrown << combinePipeLast.throwWhen(
    //  rPrevCombinePipeLastCnt.asUInt === combinePipeLastThrown.cnt
    //)
    nCombinePipeLast.driveTo(combinePipeLastPreThrown)(
      con=(
        payload,
        node,
      ) => {
        payload := node(combinePipePostJoinPayload)
      }
    )
    when (
      combinePipeLastThrown.fire
      //&& rCombineScaleXCnt.msb
    ) {
      //rPrevCombinePipeLastCnt := combinePipeLastThrown.cnt.asSInt
      //rPrevCombinePipeLastFullCnt := combinePipeLastThrown.fullCnt.asSInt
      rPrevCombinePipeLastFullCnt := (
        combinePipeLastThrown.fullCnt//.resized
      )
      //rPrevCombinePipeLastFracCnt := combinePipeLastThrown.fracCnt
      //rPrevCombinePipeLastScaleXCnt := (
      //  combinePipeLastThrown.stage0.scaleXCnt
      //)
    }
    val prePopStm = cloneOf(pop)
    pop << prePopStm
    combinePipeLastThrown.translateInto(
      //pop
      prePopStm
    )(
      dataAssignment=(
        popPayload,
        combinePipeLastPayload,
      ) => {
        popPayload.ctrlEn := outp.ctrlEn
        popPayload.col := outp.col
        popPayload.physPosInfo := (
          popPayload.physPosInfo.getZero
        )
      }
    )
    pop.physPosInfo.allowOverride
    pop.physPosInfo := outp.physPosInfo

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
          //combinePipeInVeryFrontHaltWhen.changingRow
          combinePipeInVeryFront.changingRow
        ) {
          haltCombinePipeVeryFront := True
        } otherwise {
          //haltCombinePipeVeryFront := False
          haltCombinePipeVeryFront := rPastHaltCombinePipe
        }
        when (
          //combinePipeIn(0).fire
          //combinePipeInVeryFrontHaltWhen.fire
          combinePipeInVeryFront.fire
        ) {
          //rCombinePipeFrontPayload.cnt := rCombinePipeFrontPayload.cnt + 1
          //rCombinePipeFrontPayload.bakCnt := (
          //  rCombinePipeFrontPayload.bakCnt - 1
          //)
          //rCombinePipeFrontPayload.bakCntMinus1 := (
          //  rCombinePipeFrontPayload.bakCntMinus1 - 1
          //)
          // BEGIN: test logic

          //when ((rCombineScaleXCnt /*- 1*/).msb) {
          //  rCombineScaleXCnt := params.physFbSize2dScale.x - 2
          //  rCombinePipeFrontPayload.stage0.scaleXCnt := (
          //    params.physFbSize2dScale.x - 2
          //  )
          //} otherwise {
          //  rCombineScaleXCnt := rCombineScaleXCnt - 1
          //  rCombinePipeFrontPayload.stage0.scaleXCnt := (
          //    rCombineScaleXCnt - 1
          //  )
          //}
          //when ((rCombineScaleXCnt /*- 1*/).msb) {
            //when (
            //  //tempFullCnt === params.physFbSize2dScale.x - 1
            //  //True
            //  //rCombinePipeFrontPayload.fullCnt(
            //  //  myPow - 1 downto 0
            //  //) === (
            //  //  params.physFbSize2dScale.x - 1
            //  //)
            //  //(rCombinePipeFrontPayload.fracCnt - 1).msb
            //) {
              when (
                rCombinePipeFrontPayload.fullBakCntWillBeDone()
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
              } 
              //elsewhen (
              //  //!(rCombinePipeFrontPayload.fracCnt - 1).msb
              //  //!rCombinePipeFrontPayload.fracCnt.msb
              //) {
              //  //rCombinePipeFrontPayload.fracCnt := (
              //  //  rCombinePipeFrontPayload.fracCnt - 1
              //  //)
              //} 
              .otherwise {
                def myPow = log2Up(params.physFbSize2dScale.x)
                //def myBakCnt = rCombinePipeFrontPayload.fullBakCnt
                def myCnt = rCombinePipeFrontPayload.fullCnt
                when (
                  if (params.physFbSize2dScale.x > 1) (
                    //myBakCnt(myPow - 1 downto 0)
                    //=== (
                    //  (1 << myPow) - params.physFbSize2dScale.x
                    //)
                    myCnt(myPow - 1 downto 0)
                    === params.physFbSize2dScale.x - 1
                  ) else (
                    False
                  )
                ) {
                  rCombinePipeFrontPayload.cnt := (
                    rCombinePipeFrontPayload.cnt + 1
                  )
                  rCombinePipeFrontPayload.fullCnt(
                    myPow - 1 downto 0
                  ) := 0x0
                  rCombinePipeFrontPayload.bakCnt := (
                    rCombinePipeFrontPayload.bakCnt - 1
                  )
                  rCombinePipeFrontPayload.fullBakCnt(
                    myPow - 1 downto 0
                  ) := (default -> True)

                  //rCombinePipeFrontPayload.bakCntMinus1 := (
                  //  rCombinePipeFrontPayload.bakCntMinus1 - 1
                  //)
                  //rCombinePipeFrontPayload.fullBakCntMinus1(
                  //  myPow - 1 downto 0
                  //) := (default -> True)
                } otherwise {
                  //nextCombineChangingRow := False
                  //nextCombinePipeFrontValid := rCombinePipeFrontValid
                  rCombinePipeFrontPayload.changingRow := False
                  //val myPow = log2Up(params.physFbSize2dScale.x)
                  //val tempFullBakCnt = (
                  //  rCombinePipeFrontPayload.fullBakCnt(
                  //    tempRange - 1 downto 0
                  //  ) + 1
                  //)
                  //} otherwise {
                  rCombinePipeFrontPayload.fullCnt := (
                    //rCombinePipeFrontPayload.fullCnt + 1
                    rCombinePipeFrontPayload.fullCnt + 1
                  )
                  rCombinePipeFrontPayload.fullBakCnt := (
                    rCombinePipeFrontPayload.fullBakCnt - 1
                  )
                  //rCombinePipeFrontPayload.fullBakCntMinus1 := (
                  //  rCombinePipeFrontPayload.fullBakCntMinus1 - 1
                  //)
                  //rCombinePipeFrontPayload.fracCnt := (
                  //  params.physFbSize2dScale.x - 2
                  //)
                }
              }
            //}
          //} otherwise {
          //  rCombinePipeFrontPayload.fracCnt := (
          //    rCombinePipeFrontPayload.fracCnt - 1
          //  )
          //}
          //}
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

      //val stageData = DualPipeStageData[Flow[WrBgPipePayload]](
      //  pipeIn=wrBgPipeIn,
      //  pipeOut=wrBgPipeOut,
      //  pipeNumMainStages=wrBgPipeNumMainStages,
      //  pipeStageIdx=0,
      //)

      // BEGIN: stage 0
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) =>
      {
        def idx = 0
        //val tempInp = stageData.pipeIn(idx)
        //val tempOutp = stageData.pipeOut(idx)
        //val tempInp = sWrBgArr(idx).up(wrBgPipePayload)
        //val tempOutp = WrBgPipePayload()
        //tempOutp := tempInp
        //tempOutp.allowOverride
        //sWrBgArr(idx).down
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx)
        //val tempInp = tempIoPair._1
        //val tempOutp = tempIoPair._2

        switch (pipeIn.bgIdx) {
          for (tempBgIdx <- 0 until params.numBgs) {
            is (tempBgIdx) {
              //pipeOut.scroll := bgAttrsArr(tempBgIdx).scroll
              pipeOut.bgAttrs := bgAttrsArr(tempBgIdx)
              pipeOut.bgAttrs.scroll.allowOverride
              when (pipeOut.bgAttrs.fbAttrs.doIt) {
                // Only allow scrolling with tilemaps
                pipeOut.bgAttrs.scroll := (
                  pipeOut.bgAttrs.scroll.getZero
                )
              }
              if (!noColorMath) {
                pipeOut.colorMathAttrs := colorMathAttrs
              }
            }
          }
        }
        //pipeOut.lineMemArrIdx := pipeIn.lineMemArrIdx
        //pipeOut.lineNum := pipeIn.lineNum
        pipeOut.fullLineNum := pipeIn.fullLineNum
        pipeOut.cnt := pipeIn.cnt
        pipeOut.bakCnt := pipeIn.bakCnt
        pipeOut.bakCntMinus1 := pipeIn.bakCntMinus1
        pipeOut.bgIdx := pipeIn.bgIdx
        //tempOut.stage0 := pipeIn.stage0
      }

      //// BEGIN: post stage 0
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 1
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until params.totalNumBgKinds
          //kind <- 0 until 1
        ) {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          def tempInp = (
            if (kind == 0) {
              pipeIn.postStage0
            } else {
              pipeIn.colorMath
            }
          )
          def tempAttrs = (
            if (kind == 0) {
              pipeIn.bgAttrs
            } else {
              pipeIn.colorMathAttrs
            }
          )
          def tempOutp = (
            if (kind == 0) {
              pipeOut.postStage0
            } else {
              pipeOut.colorMath
            }
          )

          //val dbgTestPxPosX = Vec.fill(params.bgTileSize2d.x)(
          //)
          val tempPxPosXGridIdx = cloneOf(tempOutp.pxPosXGridIdx)
          switch (pipeIn.bgIdx) {
            for (tempBgIdx <- 0 until params.numBgs) {
              is (tempBgIdx) {
                val dbgPxPosXGridIdxFindFirstSameAs: (Bool, UInt) = (
                  tempOutp.pxPosXGridIdx.sFindFirst(
                    //_(0) === tempInp.bgAttrs.scroll.x(
                    //  params.bgTileSize2dPow.x + 1
                    //  downto params.bgTileSize2dPow.x
                    //)(0)
                    _(0) === tempAttrs.scroll.x(
                      params.bgTileSize2dPow.x - 1
                    )
                  )
                )
                tempOutp.pxPosXGridIdxFindFirstSameAsFound := (
                  dbgPxPosXGridIdxFindFirstSameAs._1
                )
                tempOutp.pxPosXGridIdxFindFirstSameAsIdx := (
                  dbgPxPosXGridIdxFindFirstSameAs._2.resized
                )
                val dbgPxPosXGridIdxFindFirstDiff: (Bool, UInt) = (
                  tempOutp.pxPosXGridIdx.sFindFirst(
                    //_(0) =/= tempInp.bgAttrs.scroll.x(
                    //  params.bgTileSize2dPow.x + 1
                    //  downto params.bgTileSize2dPow.x
                    //)(0)
                    _(0) =/= tempAttrs.scroll.x(
                      params.bgTileSize2dPow.x - 1
                    )
                  )
                )
                tempOutp.pxPosXGridIdxFindFirstDiffFound := (
                  dbgPxPosXGridIdxFindFirstDiff._1
                )
                tempOutp.pxPosXGridIdxFindFirstDiffIdx := (
                  dbgPxPosXGridIdxFindFirstDiff._2.resized
                )
              }
            }
          }
          for (x <- 0 until params.bgTileSize2d.x) {
            tempOutp.pxPosXGridIdx(x) := tempOutp.pxPos(x).x(
              tempOutp.pxPos(x).x.high
              //downto params.bgTileSize2dPow.x - 1
              downto params.bgTileSize2dPow.x
            ).resized
            switch (pipeIn.bgIdx) {
              for (tempBgIdx <- 0 until params.numBgs) {
                is (tempBgIdx) {
                  def tempBgTileWidthPow = params.bgTileSize2dPow.x
                  tempOutp.pxPos(x).x := (
                    Cat(
                      pipeIn.cnt(
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
                    - tempAttrs.scroll.x

                    //+ tempInp.bgAttrs.scroll.x
                  )
                  tempOutp.pxPos(x).y := (
                    (
                      pipeIn.lineNum
                      - tempAttrs.scroll.y
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
        }
      }

      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 2
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until params.totalNumBgKinds
          //kind <- 0 until 1
        ) {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          def tempInp = (
            if (kind == 0) {
              pipeIn.postStage0
            } else {
              pipeIn.colorMath
            }
          )
          def tempAttrs = (
            if (kind == 0) {
              pipeIn.bgAttrs
            } else {
              pipeIn.colorMathAttrs
            }
          )
          def tempOutp = (
            if (kind == 0) {
              pipeOut.postStage0
            } else {
              pipeOut.colorMath
            }
          )
          tempOutp.stage2.fbRdAddrMultTileMemBaseAddr := (
            tempAttrs.fbAttrs.tileMemBaseAddr
            * (
              params.intnlFbSize2d.y
              * (params.intnlFbSize2d.x / params.bgTileSize2d.x)
            )
          ).resized
          tempOutp.stage2.fbRdAddrMultPxPosY := (
            tempInp.pxPos(0).y(
              log2Up(params.intnlFbSize2d.y) - 1 downto 0
            )
            * (params.intnlFbSize2d.x / params.bgTileSize2d.x)
          ).resized

          for (
            //x <- 0 until params.bgTileSize2d.x
            arrIdx <- 0 until params.numBgMemsPerNonPalKind
            //arrIdx <- 0 until params.numBgEntryMems
          ) {
            for (tempBgIdx <- 0 until params.numBgs) {
              def arr = (
                if (kind == 0) {
                  bgEntryMemA2d(tempBgIdx)(
                    arrIdx
                    //x
                  )
                } else {
                  colorMathEntryMemArr(arrIdx)
                }
              )
              arr.io.rdEn := True
              //arr.io.rdEn.allowOverride
              arr.io.rdAddr := 0
              //arr.io.rdAddr.allowOverride
              if (kind != 0) {
                arr.io.rdEn.allowOverride
                arr.io.rdAddr.allowOverride
              }
            }
          }
          switch (pipeIn.stage0.bgIdx) {
            for (tempBgIdx <- 0 until params.numBgs) {
              is (tempBgIdx) {
                //switch (
                //  tempInp.pxPosXGridIdx(
                //    tempInp.pxPosXGridIdxFindFirstSameAsIdx
                //  )(0 downto 0)
                //) {
                //  for (
                //    tempPxPosIdx <- 0
                //    until params.numBgMemsPerNonPalKind
                //  ) {
                //    is (tempPxPosIdx) {
                      //bgEntryMemA2d(tempBgIdx)
                      //def arr = (
                      //  if (kind == 0) {
                      //    bgEntryMemA2d(tempBgIdx)
                      //  } else {
                      //    colorMathEntryMemArr
                      //  }
                      //)
                      def setRdAddr(
                        someTempRdAddr: UInt,
                        someVecIdx: UInt,
                        //someMemArrIdx: Int,
                      ): Unit = {
                        //arr
                        def tempMemArr = (
                          if (kind == 0) {
                            bgEntryMemA2d(tempBgIdx)
                            //(
                            //  //(tempPxPosIdx + plusAmount) % 2
                            //  someMemArrIdx
                            //)
                          } else {
                            colorMathEntryMemArr
                            //(
                            //  //(tempPxPosIdx + plusAmount) % 2
                            //  someMemArrIdx
                            //)
                          }
                        )//.io.rdAddr
                        ////bgEntryMemA2d()(
                        ////  tempBgIdx
                        ////).io.rdAddr 
                        //tempMem.io.rdAddr := (
                        //  tempInp.bgEntryMemIdx(
                        //    //tempInp.pxPosXGridIdxFindFirstSameAsIdx
                        //    //x
                        //    //0
                        //    someVecIdx
                        //  )
                        //)
                        someTempRdAddr := (
                          tempInp.bgEntryMemIdx(someVecIdx)
                        )

                        switch (someTempRdAddr(0 downto 0)) {
                          for (myTempIdx <- 0 until 2) {
                            is (myTempIdx) {
                              tempMemArr(myTempIdx).io.rdAddr := (
                                someTempRdAddr(
                                  someTempRdAddr.high downto 1
                                )
                              )
                            }
                          }
                        }
                      }
                      //def sameAsIdx = (
                      //  tempInp.pxPosXGridIdxFindFirstSameAsIdx
                      //)
                      //def diffIdx = (
                      //  tempInp.pxPosXGridIdxFindFirstDiffIdx
                      //)
                      ////if (tempPxPosIdx == 0) {
                      //  setRdAddr(
                      //    someVecIdx=(
                      //      U{
                      //        def tempWidth = params.bgTileSize2dPow.x
                      //        def tempVal = 0
                      //        f"$tempWidth'd$tempVal"
                      //      }
                      //    ),
                      //    someMemArrIdx=0,
                      //  )
                      ////} else {
                      //  setRdAddr(
                      //    someVecIdx=(
                      //      //1 << bgEntryMemA2d(tempBgIdx)(
                      //      //  //(tempPxPosIdx + plusAmount) % 2
                      //      //  0
                      //      //).io.rdAddr.getWidth
                      //      U{
                      //        def tempWidth = params.bgTileSize2dPow.x
                      //        def tempVal = params.bgTileSize2d.x - 1
                      //        f"$tempWidth'd$tempVal"
                      //      }
                      //    ),
                      //    someMemArrIdx=1,
                      //  )
                      ////}
                      //when (!tempInp.pxPosXGridIdxFindFirstDiffFound) {
                      //  setRdAddr(
                      //    someIdx=sameAsIdx,
                      //    plusAmount=0,
                      //  )
                      //} otherwise {
                      //  if (tempPxPosIdx == 0) {
                      //    when (sameAsIdx < diffIdx) {
                      //    }
                      //    //when (x < diffIdx) {
                      //    //  setBgEntry(1)
                      //    //} otherwise {
                      //    //  setBgEntry(0)
                      //    //}
                      //  } else { //if (!noColorMath)
                      //    //when (x < sameAsIdx) {
                      //    //  setBgEntry(0)
                      //    //} otherwise {
                      //    //  setBgEntry(1)
                      //    //}
                      //  }
                      //}
                      //--------
                      setRdAddr(
                        someTempRdAddr=(
                          tempOutp.stage2.bgEntryMemIdxSameAs
                        ),
                        someVecIdx=(
                          tempInp.pxPosXGridIdxFindFirstSameAsIdx
                        ),
                      )
                      setRdAddr(
                        someTempRdAddr=(
                          tempOutp.stage2.bgEntryMemIdxDiff
                        ),
                        someVecIdx=(
                          tempInp.pxPosXGridIdxFindFirstDiffIdx
                        ),
                      )
                      //switch (Cat(
                      //  tempInp.pxPosXGridIdxFindFirstSameAsFound,
                      //  tempInp.pxPosXGridIdxFindFirstDiffFound,
                      //)) {
                      //  is (M"-0") {
                      //    // At least one of them will be found, so this
                      //    // indicates `SameAsFound`
                      //    setRdAddr(
                      //      someIdx=sameAsIdx,
                      //      plusAmount=0,
                      //    )
                      //  }
                      //  is (M"01") {
                      //    setRdAddr(
                      //      someIdx=diffIdx,
                      //      plusAmount=1,
                      //    )
                      //  }
                      //  is (M"11") {
                      //    //when (sameAsIdx < diffIdx) {
                      //    //  setRdAddr(
                      //    //    someIdx=sameAsIdx,
                      //    //    plusAmount=tempPxPosIdx % 2,
                      //    //  )
                      //    //} otherwise {
                      //    //  // this indicates `sameAsIdx > diffIdx`
                      //    //  setRdAddr(
                      //    //    someIdx=diffIdx,
                      //    //    plusAmount=(tempPxPosIdx + 1) % 2,
                      //    //  )
                      //    //}
                      //    if (tempPxPosIdx == 0) {
                      //      when (sameAsIdx < diffIdx) {
                      //        setRdAddr(
                      //          someIdx=sameAsIdx,
                      //          plusAmount=0,
                      //        )
                      //      } otherwise {
                      //        // this indicates `sameAsIdx > diffIdx`
                      //        setRdAddr(
                      //          someIdx=diffIdx,
                      //          plusAmount=1,
                      //        )
                      //      }
                      //      //when (x < diffIdx) {
                      //      //  setBgEntry(1)
                      //      //} otherwise {
                      //      //  setBgEntry(0)
                      //      //}
                      //    } else { //if (!tempPxPosIdx == 1)
                      //      when (sameAsIdx < diffIdx) {
                      //        setRdAddr(
                      //          someIdx=sameAsIdx,
                      //          plusAmount=1,
                      //        )
                      //      } otherwise {
                      //        // this indicates `sameAsIdx > diffIdx`
                      //        setRdAddr(
                      //          someIdx=diffIdx,
                      //          plusAmount=0,
                      //        )
                      //      }
                      //      //when (x < sameAsIdx) {
                      //      //  setBgEntry(0)
                      //      //} otherwise {
                      //      //  setBgEntry(1)
                      //      //}
                      //    }
                      //  }
                      //  default {
                      //  }
                      //}
                      //--------
                      //arr(tempPxPosIdx % 2).io.rdAddr := (
                      //  tempInp.bgEntryMemIdx(
                      //    //tempInp.pxPosXGridIdxFindFirstSameAsIdx
                      //    //x
                      //    0
                      //  )
                      //)
                      ////if (!noColorMath) {
                      //  when (tempInp.pxPosXGridIdxFindFirstDiffFound) {
                      //    arr((tempPxPosIdx + 1) % 2).io.rdAddr := (
                      //      tempInp.bgEntryMemIdx(
                      //        //tempInp.pxPosXGridIdxFindFirstDiffIdx
                      //        //x
                      //        //(1 << tempInp.bgEntryMemIdx.getWidth) - 1
                      //        tempInp.bgEntryMemIdx.size - 1
                      //      )
                      //    )
                      //  }
                      ////}
                //    }
                //  }
                //}
              }
            }
          }
        }
        //for (x <- 0 until params.bgTileSize2d.x) {
        //  for (jdx <- 0 until tempOutp.stage2.numMyIdxVecs) {
        //    def myIdxVec = tempOutp.myIdxV2d(x)
        //    val myIdxFull = cloneOf(tempInp.pxPos(x).x)
        //      .setName(f"wrBgPipe2_myIdxFull_$x" + f"_$jdx")
        //    myIdxFull := tempInp.pxPos(x).x
        //    //myIdxFull := tempInp.pxPos(0).x + x
        //    val myIdx = UInt(params.bgTileSize2dPow.x bits)
        //      .setName(f"wrBgPipe2_myIdx_$x" + f"_$jdx")
        //    myIdx := myIdxFull(myIdx.bitsRange)
        //    //myIdxVec(x) := myIdx
        //    myIdxVec(jdx) := myIdx
        //  }
        //}
      }
      {
        def idx = 3
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until params.totalNumBgKinds
          //kind <- 0 until 1
        ) {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          def tempInp = (
            if (kind == 0) {
              pipeIn.postStage0
            } else {
              pipeIn.colorMath
            }
          )
          def tempAttrs = (
            if (kind == 0) {
              pipeIn.bgAttrs
            } else {
              pipeIn.colorMathAttrs
            }
          )
          def tempOutp = (
            if (kind == 0) {
              pipeOut.postStage0
            } else {
              pipeOut.colorMath
            }
          )
          for (x <- 0 until params.bgTileSize2d.x) {
            tempOutp.bgEntry(x) := tempOutp.bgEntry(x).getZero
            //def myIdxVec = tempInp.myIdxV2d(x)
            //def myIdx = tempInp.myIdxV2d(x)(x)
            switch (pipeIn.bgIdx) {
              for (tempBgIdx <- 0 until params.numBgs) {
                is (tempBgIdx) {
                  //def sameAsIdx = (
                  //  tempInp.pxPosXGridIdxFindFirstSameAsIdx
                  //)
                  //def diffIdx = (
                  //  tempInp.pxPosXGridIdxFindFirstDiffIdx
                  //)
                  def tempMemArr = (
                    if (kind == 0) {
                      bgEntryMemA2d(tempBgIdx)
                      //(
                      //  //(tempPxPosIdx + plusAmount) % 2
                      //  someMemIdx
                      //)
                    } else {
                      colorMathEntryMemArr
                      //(
                      //  //(tempPxPosIdx + plusAmount) % 2
                      //  someMemIdx
                      //)
                    }
                  )
                  for (
                    myTempIdx <- 0 until params.numBgMemsPerNonPalKind
                  ) {
                    tempOutp.stage3.bgEntry(myTempIdx)(x) := (
                      tempMemArr(myTempIdx).io.rdData
                    )
                  }
                }
              }
            }
            switch (Cat(
              tempInp.pxPosXGridIdxFindFirstSameAsFound,
              tempInp.pxPosXGridIdxFindFirstDiffFound,
            )) {
              is (M"-0") {
                // At least one of them will be found, so
                // this indicates `SameAsFound`
                //setRdAddr(
                //  someIdx=sameAsIdx,
                //  plusAmount=0,
                //)
                //setBgEntry(
                //  //0
                //  tempInp.stage2.bgEntryMemIdxSameAs
                //)
                tempOutp.stage3.tempAddr(x) := (
                  tempInp.stage2.bgEntryMemIdxSameAs
                )
              }
              is (M"01") {
                //setRdAddr(
                //  someIdx=diffIdx,
                //  plusAmount=1,
                //)

                //setBgEntry(
                //  //1
                //  //0
                //  tempInp.stage2.bgEntryMemIdxDiff
                //)
                tempOutp.stage3.tempAddr(x) := (
                  tempInp.stage2.bgEntryMemIdxDiff
                )
              }
              is (M"11") {
                def sameAsIdx = (
                  tempInp.pxPosXGridIdxFindFirstSameAsIdx
                )
                def diffIdx = (
                  tempInp.pxPosXGridIdxFindFirstDiffIdx
                )
                when (sameAsIdx > diffIdx) {
                  when (x < sameAsIdx) {
                    //setBgEntry(
                    //  //0
                    //  tempInp.stage2.bgEntryMemIdxDiff
                    //)
                    tempOutp.stage3.tempAddr(x) := (
                      tempInp.stage2.bgEntryMemIdxDiff
                    )
                  } otherwise {
                    //setBgEntry(
                    //  //1
                    //  tempInp.stage2.bgEntryMemIdxSameAs
                    //)
                    tempOutp.stage3.tempAddr(x) := (
                      tempInp.stage2.bgEntryMemIdxSameAs
                    )
                  }
                } otherwise {
                  // this indicates `sameAsIdx < diffIdx`
                  when (x < diffIdx) {
                    //setBgEntry(
                    //  //0
                    //  tempInp.stage2.bgEntryMemIdxSameAs
                    //)
                    tempOutp.stage3.tempAddr(x) := (
                      tempInp.stage2.bgEntryMemIdxSameAs
                    )
                  } otherwise {
                    //setBgEntry(
                    //  //1
                    //  tempInp.stage2.bgEntryMemIdxDiff
                    //)
                    tempOutp.stage3.tempAddr(x) := (
                      tempInp.stage2.bgEntryMemIdxDiff
                    )
                  }
                }
              }
              default {
              }
            }
          }
        }
      }
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //    idx: Int,
      //  ) => 
      {
        def idx = 4
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until params.totalNumBgKinds
          //kind <- 0 until 1
        ) {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          def tempInp = (
            if (kind == 0) {
              pipeIn.postStage0
            } else {
              pipeIn.colorMath
            }
          )
          def tempAttrs = (
            if (kind == 0) {
              pipeIn.bgAttrs
            } else {
              pipeIn.colorMathAttrs
            }
          )
          def tempOutp = (
            if (kind == 0) {
              pipeOut.postStage0
            } else {
              pipeOut.colorMath
            }
          )
          tempOutp.stage4.fbRdAddrMultPlus := (
            tempInp.stage2.fbRdAddrMultTileMemBaseAddr
            + tempInp.stage2.fbRdAddrMultPxPosY
          )

          for (x <- 0 until params.bgTileSize2d.x) {
            tempOutp.bgEntry(x) := tempOutp.bgEntry(x).getZero
            //def myIdxVec = tempInp.myIdxV2d(x)
            //def myIdx = tempInp.myIdxV2d(x)(x)
            switch (pipeIn.bgIdx) {
              for (tempBgIdx <- 0 until params.numBgs) {
                is (tempBgIdx) {
                  //--------
                  def setBgEntry(
                    //someMemIdx: Int
                    someTempRdAddr: UInt
                  ): Unit = {
                    //def tempMemArr = (
                    //  if (kind == 0) {
                    //    bgEntryMemA2d(tempBgIdx)
                    //    //(
                    //    //  //(tempPxPosIdx + plusAmount) % 2
                    //    //  someMemIdx
                    //    //)
                    //  } else {
                    //    colorMathEntryMemArr
                    //    //(
                    //    //  //(tempPxPosIdx + plusAmount) % 2
                    //    //  someMemIdx
                    //    //)
                    //  }
                    //)
                    //tempOutp.bgEntry(x) := (
                    //  tempMem.io.rdData
                    //)
                    switch (someTempRdAddr(0 downto 0)) {
                      for (myTempIdx <- 0 until 2) {
                        is (myTempIdx) {
                          tempOutp.bgEntry(x) := (
                            //tempMemArr(myTempIdx).io.rdData
                            tempInp.stage3.bgEntry(myTempIdx)(x)
                          )
                        }
                      }
                    }
                  }
                  setBgEntry(
                    someTempRdAddr=tempInp.stage3.tempAddr(x)
                  )
                  //def sameAsIdx = (
                  //  tempInp.pxPosXGridIdxFindFirstSameAsIdx
                  //)
                  //def diffIdx = (
                  //  tempInp.pxPosXGridIdxFindFirstDiffIdx
                  //)
                  //switch (Cat(
                  //  tempInp.pxPosXGridIdxFindFirstSameAsFound,
                  //  tempInp.pxPosXGridIdxFindFirstDiffFound,
                  //)) {
                  //  is (M"-0") {
                  //    // At least one of them will be found, so
                  //    // this indicates `SameAsFound`
                  //    //setRdAddr(
                  //    //  someIdx=sameAsIdx,
                  //    //  plusAmount=0,
                  //    //)
                  //    setBgEntry(
                  //      //0
                  //      tempInp.stage2.bgEntryMemIdxSameAs
                  //    )
                  //  }
                  //  is (M"01") {
                  //    //setRdAddr(
                  //    //  someIdx=diffIdx,
                  //    //  plusAmount=1,
                  //    //)
                  //    setBgEntry(
                  //      //1
                  //      //0
                  //      tempInp.stage2.bgEntryMemIdxDiff
                  //    )
                  //  }
                  //  is (M"11") {
                  //    def sameAsIdx = (
                  //      tempInp.pxPosXGridIdxFindFirstSameAsIdx
                  //    )
                  //    def diffIdx = (
                  //      tempInp.pxPosXGridIdxFindFirstDiffIdx
                  //    )
                  //    //if (tempPxPosIdx == 0) {
                  //    //  when (x < diffIdx) {
                  //    //    setBgEntry(1)
                  //    //  } otherwise {
                  //    //    setBgEntry(0)
                  //    //  }
                  //    //} else { //if (!noColorMath)
                  //    //  when (x < sameAsIdx) {
                  //    //    setBgEntry(0)
                  //    //  } otherwise {
                  //    //    setBgEntry(1)
                  //    //  }
                  //    //}
                  //    when (sameAsIdx > diffIdx) {
                  //      when (x < sameAsIdx) {
                  //        setBgEntry(
                  //          //0
                  //          tempInp.stage2.bgEntryMemIdxDiff
                  //        )
                  //      } otherwise {
                  //        setBgEntry(
                  //          //1
                  //          tempInp.stage2.bgEntryMemIdxSameAs
                  //        )
                  //      }
                  //    } otherwise {
                  //      // this indicates `sameAsIdx < diffIdx`
                  //      when (x < diffIdx) {
                  //        setBgEntry(
                  //          //0
                  //          tempInp.stage2.bgEntryMemIdxSameAs
                  //        )
                  //      } otherwise {
                  //        setBgEntry(
                  //          //1
                  //          tempInp.stage2.bgEntryMemIdxDiff
                  //        )
                  //      }
                  //    }
                  //  }
                  //  default {
                  //  }
                  //}
                  //--------
                  //tempOutp.bgEntry(x) := (
                  //  bgEntryMemA2d(tempBgIdx)(
                  //    x
                  //  ).io.rdData
                  //)
                  //--------
                }
              }
            }
          }
        }
      }
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //    idx: Int,
      //) => 
      {
        def idx = 5
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until params.totalNumBgKinds
          //kind <- 0 until 1
        ) {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          def tempInp = (
            if (kind == 0) {
              pipeIn.postStage0
            } else {
              pipeIn.colorMath
            }
          )
          def tempAttrs = (
            if (kind == 0) {
              pipeIn.bgAttrs
            } else {
              pipeIn.colorMathAttrs
            }
          )
          def tempOutp = (
            if (kind == 0) {
              pipeOut.postStage0
            } else {
              pipeOut.colorMath
            }
          )

          for (x <- 0 until params.bgTileSize2d.x) {
            tempOutp.stage5.fbRdAddrFinalPlus(x) := (
              tempInp.stage4.fbRdAddrMultPlus
              + tempInp.pxPos(x).x(
                log2Up(params.intnlFbSize2d.x) - 1
                downto params.bgTileSize2dPow.x
              ).resized
            ).resized
          }

        }
        //val myTilePxPosGridIdxFindFirstSameAs: (Bool, UInt) = (
        //  tempInp.bgEntry.tileIdx
        //)
      }
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //    idx: Int,
      //  ) => 
      {
        def idx = 6
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until params.totalNumBgKinds
        ) {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          def tempInp = (
            if (kind == 0) {
              pipeIn.postStage0
            } else {
              pipeIn.colorMath
            }
          )
          def tempAttrs = (
            if (kind == 0) {
              pipeIn.bgAttrs
            } else {
              pipeIn.colorMathAttrs
            }
          )
          def tempOutp = (
            if (kind == 0) {
              pipeOut.postStage0
            } else {
              pipeOut.colorMath
            }
          )
          //--------
          //val tempTileIdxVec = Vec.fill(params.bgTileSize2d.x)(
          //  //cloneOf(tempInp.bgEntry(0).tileIdx)
          //  UInt(
          //    (
          //      (
          //        if (kind == 0) (
          //          params.numBgTilesPow
          //        ) else (
          //          params.numColorMathTilesPow
          //        )
          //      ) //+ params.bgTileSize2dPow.y
          //    )
          //    //params.bgTileSize2dPow
          //    bits
          //  )
          //)

          //val tempTileGridIdxFindFirstSameAs: (Bool, UInt) = (
          //  tempTileIdxVec.sFindFirst(
          //    //_(0) === tempInp.pxPos(0).x(
          //    //)
          //    //_(params.bgTileSize2dPow.y - 1 downto 0)
          //    //=== tempOutp.tilePxsCoord(0).y
          //    //_(0)
          //    //=== tempOutp.tilePxsCoord(
          //    //  tempInp.pxPosXGridIdxFindFirstSameAsIdx
          //    //)(params.bgTileSize2dPow.y)
          //    _(0) === tempOutp.pxPosXGridIdx(
          //      tempInp.pxPosXGridIdxFindFirstSameAsIdx
          //    )(0)
          //  )
          //)
          //tempOutp.stage5.tileGridIdxFindFirstSameAsFound := (
          //  //tempTileGridIdxFindFirstSameAs._1
          //  tempInp.pxPosXGridIdxFindFirstSameAsFound
          //)
          //tempOutp.stage5.tileGridIdxFindFirstSameAsIdx := (
          //  tempTileGridIdxFindFirstSameAs._2
          //)
          //val tempTileGridIdxFindFirstDiff: (Bool, UInt) = (
          //  tempTileIdxVec.sFindFirst(
          //    //_(params.bgTileSize2dPow.y - 1 downto 0)
          //    //=/= tempOutp.tilePxsCoord(0).y
          //    _(0) === tempOutp.pxPosXGridIdx(
          //      tempInp.pxPosXGridIdxFindFirstDiffIdx
          //    )(0)
          //  )
          //)
          //tempOutp.stage5.tileGridIdxFindFirstDiffFound := (
          //  //tempTileGridIdxFindFirstDiff._1
          //  tempInp.pxPosXGridIdxFindFirstDiffFound
          //)
          //tempOutp.stage5.tileGridIdxFindFirstDiffIdx := (
          //  tempTileGridIdxFindFirstDiff._2
          //)
          //for (x <- 0 until params.bgTileSize2d.x) {
          //  tempTileIdxVec(x) := Cat(
          //    tempInp.bgEntry(x).tileIdx,
          //    //U(
          //    //  s"${params.bgTileSize2dPow.y}'d${y}"
          //    //),
          //  ).asUInt
          //  //tempOutp.stage4.tileGridIdx(x) := (
          //  //  tempInp.bgEntry(x).tileIdx(
          //  //    //0 downto 0
          //  //    //tempOutp.stage4.tileGridIdx(x).high downto 1
          //  //    params.numBgTilesPow - 1 downto 1
          //  //  )
          //  //)
          //  //tempOutp.stage4.tileGridIdxFindFirstSameAs
          //}
          //--------
          def someTileMemArr = (
            if (kind == 0) {
              bgTileMemArr
            } else {
              colorMathTileMemArr
            }
          )
          val tempTilePxsPos = Vec.fill(params.bgTileSize2d.x)(
            params.bgTilePxsCoordT()
          )
          for (x <- 0 until params.bgTileSize2d.x) {
            switch (pipeIn.bgIdx) {
              for (tempBgIdx <- 0 until params.numBgs) {
                is (tempBgIdx) {
                  tempTilePxsPos(x).x := (
                    (
                      {
                        def tempTileWidth = params.bgTileSize2dPow.x
                        U(f"$tempTileWidth'd$x")
                      } - tempAttrs.scroll.x
                    )(tempTilePxsPos(x).x.bitsRange)
                  )
                  tempTilePxsPos(x).y := (
                    (
                      pipeIn.lineNum
                      //+ tempAttrs.scroll.y
                      - tempAttrs.scroll.y
                    ).resized
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
          }
          for (
            //x <- 0 until params.bgTileSize2d.x
            arrIdx <- 0 until params.numBgMemsPerNonPalKind
            //arrIdx <- 0 until params.numBgTileMems - 1
          ) {
            def arr = someTileMemArr(arrIdx)
            arr.io.rdEn := True
            arr.io.rdAddr := 0
            arr.io.rdAddr.allowOverride
          }
          //switch (
          //  tempInp.pxPosXGridIdx(
          //    tempInp.pxPosXGridIdxFindFirstSameAsIdx
          //  )(0 downto 0)
          //) {
          //  for (
          //    tempPxPosIdx <- 0 until params.numBgMemsPerNonPalKind
          //  ) {
              //def someTileMemArr = (
              //  if (kind == 0) {
              //    bgTileMemArr
              //  } else {
              //    colorMathTileMemArr
              //  }
              //)
              def tempRdAddrWidth = (
                someTileMemArr(0).io.rdAddr.getWidth
              )
              //tempOutp.stage4.tileMemRdAddrFront := 0
              //tempOutp.stage4.tileMemRdAddrBack := 0
              //is (tempPxPosIdx) {
                //val tempRdAddrSameAs = UInt(
                //  params.bgTileMemIdxWidth bits
                //)
                //--------
                //--------
                def setRdAddr(
                  someTempIdx: Int,
                  someTempRdAddr: UInt,
                  someVecIdx: Int,
                  //someMemArrIdx: Int,
                ): Unit = {
                  someTempRdAddr := (
                    Mux[UInt](
                      !tempAttrs.fbAttrs.doIt,
                      Cat(
                        tempInp.bgEntry(
                          //x
                          //tempInp.pxPosXGridIdxFindFirstSameAsIdx
                          someVecIdx
                        ).tileIdx,
                        tempOutp.tilePxsCoord(0).y,
                      ).asUInt,

                      // add non-scrolling framebuffer stuff back in
                      // later (see output Verilog from lost Spinal
                      // code)
                      //Cat(
                      //  tempAttrs.fbAttrs.tileMemBaseAddr,
                      //  tempInp.pxPos(0).y,
                      //  (
                      //    tempInp.pxPos(
                      //      //tempInp.pxPosXGridIdxFindFirstSameAsIdx
                      //      someVecIdx
                      //    ).x(
                      //      log2Up(params.bgSize2dInPxs.x) - 1
                      //      downto params.bgTileSize2dPow.x
                      //    )
                      //  ),
                      //).asUInt,
                      tempInp.stage5.fbRdAddrFinalPlus(someVecIdx),
                    ).resized
                  )
                  //switch (
                  //  ////Mux[UInt](
                  //  ////  !tempAttrs.fbAttrs.doIt,
                  //    //--------
                  //    someTempRdAddr(
                  //      //someTempRdAddr.high
                  //      //downto someTempRdAddr.high
                  //      params.bgTileSize2dPow.y
                  //      downto params.bgTileSize2dPow.y
                  //    ),
                  //    //tempInp.bgEntry(
                  //    //  someVecIdx
                  //    //).tileIdx(0 downto 0)
                  //    //--------
                  //  ////  {
                  //  ////    val slicePos = (
                  //  ////      log2Up(params.bgSize2dInPxs.y)
                  //  ////      + log2Up(params.bgSize2dInPxs.x)
                  //  ////      - params.bgTileSize2dPow.x
                  //  ////      + 1
                  //  ////      //- 1
                  //  ////    )
                  //  ////    someTempRdAddr(
                  //  ////      slicePos downto slicePos
                  //  ////    )
                  //  ////  },
                  //  ////)
                  //  //someTempIdx
                  //) {
                  //  for (myTempIdx <- 0 until 2) {
                  //    is (myTempIdx) {
                        someTileMemArr(
                          //tempPxPosIdx % 2
                          //someMemArrIdx
                          //myTempIdx
                          someTempIdx
                        ).io.rdAddr := (
                          //someTempRdAddr.resized
                          //Mux[UInt](
                          //  !tempAttrs.fbAttrs.doIt,
                            //Cat(
                            //  someTempRdAddr(
                            //    someTempRdAddr.high
                            //    downto params.bgTileSize2dPow.y + 1
                            //  ),
                            //  someTempRdAddr(
                            //    params.bgTileSize2dPow.y - 1
                            //    downto 0
                            //  ),
                            //).asUInt.resized,
                            someTempRdAddr.resized
                          //  {
                          //    val slicePos = (
                          //      log2Up(params.bgSize2dInPxs.y)
                          //      + log2Up(params.bgSize2dInPxs.x)
                          //      - params.bgTileSize2dPow.x
                          //      + 1
                          //      //- 1
                          //    )
                          //    Cat(
                          //      someTempRdAddr(
                          //        someTempRdAddr.high
                          //        downto slicePos + 1
                          //      ),
                          //      someTempRdAddr(
                          //        slicePos - 1
                          //        downto 0
                          //      ),
                          //    ).asUInt
                          //  }
                            //someTempRdAddr,
                          //).resized
                        )
                   //   }
                  //  }
                  //}
                }
                def sameAsIdx = (
                  tempInp.pxPosXGridIdxFindFirstSameAsIdx
                  //tempInp.stage5.tileMemRdAddrFront
                  //tempOutp.stage5.tileGridIdxFindFirstSameAsIdx
                )
                def diffIdx = (
                  tempInp.pxPosXGridIdxFindFirstDiffIdx
                  //tempInp.stage5.tileMemRdAddrBack
                  //tempOutp.stage5.tileGridIdxFindFirstDiffIdx
                )
                setRdAddr(
                  someTempIdx=0,
                  someTempRdAddr=(
                    tempOutp.stage6.tileMemRdAddrFront
                  ),
                  someVecIdx=(
                    //tempInp.pxPosXGridIdxFindFirstSameAsIdx
                    //sameAsIdx
                    0
                  ),
                )
                setRdAddr(
                  someTempIdx=1,
                  someTempRdAddr=(
                    tempOutp.stage6.tileMemRdAddrBack
                  ),
                  someVecIdx=(
                    //tempInp.pxPosXGridIdxFindFirstDiffIdx
                    //diffIdx
                    params.bgTileSize2d.x - 1
                  ),
                )
                tempOutp.tileIdxFront := (
                  tempInp.bgEntry(
                    //sameAsIdx
                    ////tempInp.pxPosXGridIdxFindFirstSameAsIdx
                    0
                  ).tileIdx//(0)
                )
                tempOutp.tileIdxBack := (
                  tempInp.bgEntry(
                    //diffIdx
                    ////tempInp.pxPosXGridIdxFindFirstDiffIdx
                    params.bgTileSize2d.x - 1
                  ).tileIdx//(0)
                )
              //}
          //  }
          //}
        }
      }
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 7
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until params.totalNumBgKinds
          //kind <- 0 until 1
        ) {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          def tempInp = (
            if (kind == 0) {
              pipeIn.postStage0
            } else {
              pipeIn.colorMath
            }
          )
          def tempAttrs = (
            if (kind == 0) {
              pipeIn.bgAttrs
            } else {
              pipeIn.colorMathAttrs
            }
          )
          def tempOutp = (
            if (kind == 0) {
              pipeOut.postStage0
            } else {
              pipeOut.colorMath
            }
          )
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)

          //val tempTilePxsPos = Vec.fill(params.bgTileSize2d.x)(
          //  params.bgTilePxsCoordT()
          //)
          for (x <- 0 until params.bgTileSize2d.x) {
            tempOutp.tileSlice(x) := tempOutp.tileSlice(x).getZero
            //tempOutp.tile(x) := bgTileMemArr(x).io.rdData
            //--------
            //def myBgEntryMemArr = bgEntryMemA2d(tempBgIdx)
            //when (!tempInp.pxPosXGridIdxFindFirstDiffFound) {
            //} otherwise {
            //}
            def setBgTile(
              tempPxPosIdx: Int,
              //tempPxPosIdx: UInt,
              //plusAmount: Int,
            ): Unit = {
              //tempOutp.bgEntry(x) := (
              //  myBgEntryMemArr(
              //    (tempPxPosIdx + plusAmount) % 2
              //  ).io.rdData
              //)
              def tempMemArr = (
                if (kind == 0) {
                  bgTileMemArr
                  //(
                  //  //plusAmount
                  //  (tempPxPosIdx + plusAmount) % 2
                  //)
                } else {
                  colorMathTileMemArr
                  //(
                  //  //plusAmount
                  //  (tempPxPosIdx + plusAmount) % 2
                  //)
                }
              )
              //tempOutp.tileSlice(x) := myTileMemArr(
              //  //x
              //  (tempPxPosIdx + someMemIdx) % 2
              //).io.rdData
              //tempOutp.tileSlice(x) := tempMemArr(
              //  //(myTempPxPosIdx + plusAmount) % 2   
              //  //myTempPxPosIdx % 2
              //  tempPxPosIdx
              //).io.rdData
              //switch (tempPxPosIdx) {
              //  for (myTempPxPosIdx <- 0 until 2) {
              //    is (myTempPxPosIdx) {
                    tempOutp.tileSlice(x) := tempMemArr(
                      //(myTempPxPosIdx + plusAmount) % 2   
                      //myTempPxPosIdx % 2
                      tempPxPosIdx
                    ).io.rdData
              //    }
              //  }
              //}
            }
            switch (Cat(
              tempInp.pxPosXGridIdxFindFirstSameAsFound,
              tempInp.pxPosXGridIdxFindFirstDiffFound,
              //tempInp.stage5.tileGridIdxFindFirstSameAsFound,
              //tempInp.stage5.tileGridIdxFindFirstDiffFound,
            )) {
              //val tempRdAddrSameAsSliced = KeepAttribute(
              //  tempInp.stage5.tileMemRdAddrFront(
              //    //tempInp.stage4.tileMemRdAddrFront.high
              //    //downto tempInp.stage4.tileMemRdAddrFront.high
              //    params.bgTileSize2dPow.y
              //    downto params.bgTileSize2dPow.y
              //  )
              //)
              //  .setName(s"wrBgPipe_6_tempRdAddrSameAsSliced_$x")
              //val tempRdAddrDiffSliced = KeepAttribute(
              //  tempInp.stage5.tileMemRdAddrBack(
              //    //tempInp.stage4.tileMemRdAddrBack.high
              //    //downto tempInp.stage4.tileMemRdAddrBack.high
              //    params.bgTileSize2dPow.y
              //    downto params.bgTileSize2dPow.y
              //  )
              //)
              //  .setName(s"wrBgPipe_6_tempRdAddrDiffSliced_$x")

              is (M"-0") {
                // At least one of them will be found, so
                // this indicates `SameAsFound`
                //setRdAddr(
                //  someIdx=sameAsIdx,
                //  plusAmount=0,
                //)
                setBgTile(
                  //tempPxPosIdx=tempRdAddrSameAsSliced,
                  tempPxPosIdx=0,
                  //tempPxPosIdx=0,
                  //plusAmount=0,
                )
              }
              is (M"01") {
                //setRdAddr(
                //  someIdx=diffIdx,
                //  plusAmount=1,
                //)
                //setBgTile(
                //  tempPxPosIdx=tempRdAddrDiffSliced,
                //  //tempPxPosIdx=1,
                //  ////plusAmount=1
                //  //plusAmount=0, // TODO: verify that this works
                //  //0
                //)
                setBgTile(
                  tempPxPosIdx=0,
                )
              }
              is (M"11") {
                //def sameAsIdx = (
                //  tempInp.pxPosXGridIdxFindFirstSameAsIdx
                //  //tempInp.stage5.tileMemRdAddrFront
                //)
                //def diffIdx = (
                //  tempInp.pxPosXGridIdxFindFirstDiffIdx
                //  //tempInp.stage5.tileMemRdAddrBack
                //)
                def sameAsIdx = (
                  tempInp.pxPosXGridIdxFindFirstSameAsIdx
                  //tempInp.stage5.tileMemRdAddrFront
                  //tempInp.stage5.tileGridIdxFindFirstSameAsIdx
                )
                def diffIdx = (
                  tempInp.pxPosXGridIdxFindFirstDiffIdx
                  //tempInp.stage5.tileMemRdAddrBack
                  //tempInp.stage5.tileGridIdxFindFirstDiffIdx
                )
                //def sameAsAddr = (
                //  tempInp.stage5.tileMemRdAddrFront
                //)
                //def diffAddr = (
                //  tempInp.stage5.tileMemRdAddrBack
                //)
                //val tempCondX_sameAsAddrSlice_gt_diffSlice = (
                //  KeepAttribute(
                //    Flow(Bool())
                //  )
                //  .setName(
                //    s"wrBgPipe_6_tempCondX_sameAsIdx_gt_diffIdx_$x"
                //  )
                //)
                //tempCondX_sameAsAddrSlice_gt_diffSlice := (
                //  tempCondX_sameAsAddrSlice_gt_diffSlice.getZero
                //)
                //val tempCondX_sameAsAddrSlice_le_diffAddrSlice = (
                //  KeepAttribute(
                //    Flow(Bool())
                //  )
                //  .setName(
                //    s"wrBgPipe_6_tempCondX_sameAsIdx_le_diffIdx_$x"
                //  )
                //)
                //tempCondX_sameAsAddrSlice_le_diffAddrSlice := (
                //  tempCondX_sameAsAddrSlice_le_diffAddrSlice.getZero
                //)

                //val myTempRdAddrSliced = KeepAttribute(
                //  //Bool()
                //  UInt(1 bits)
                //)
                //  .setName(s"wrBgPipe_6_myTempRdAddrSliced_$x")
                //setBgTile(
                //  tempPxPosIdx=myTempRdAddrSliced,
                //  //tempPxPosIdx=1,
                //  //plusAmount=0,
                //)

                when (
                  sameAsIdx > diffIdx
                  //sameAsAddr(
                  //  params.bgTileSize2dPow.y
                  //  //sameAsAddr.high
                  //  downto params.bgTileSize2dPow.y
                  //) > diffAddr(
                  //  params.bgTileSize2dPow.y
                  //  //diffAddr.high
                  //  downto params.bgTileSize2dPow.y
                  //)
                  //tempInp.bgEntry(sameAsIdx).tileIdx(0 downto 0)
                  //> tempInp.bgEntry(diffIdx).tileIdx(0 downto 0)
                  //Cat(tempInp.tileIdxSameAsLsb).asUInt
                  //> Cat(tempInp.tileIdxDiffLsb).asUInt
                ) {
                  //tempCondX_sameAsAddrSlice_gt_diffSlice.valid := True
                  //tempCondX_sameAsAddrSlice_gt_diffSlice.payload := (
                  //  x < sameAsIdx
                  //)
                  //myTempRdAddrSliced := 0
                  when (x < sameAsIdx) {
                    //when (
                    //  //Cat(tempInp.tileIdxSameAsLsb).asUInt === 1
                    //  //> Cat(tempInp.tileIdxDiffLsb).asUInt
                    //  tempInp.tileIdxSameAsLsb
                    //) {
                    //  myTempRdAddrSliced := 1
                    //} otherwise {
                    //  myTempRdAddrSliced := 0
                    //}

                    //--------
                    //myTempRdAddrSliced(0) := (
                    //  False
                    //  //tempInp.tileIdxFront(0)
                    //  //tempInp.tileIdxBackLsb
                    //  //tempInp.tileIdxSameAsLsb
                    //  //0
                    //)
                    setBgTile(
                      tempPxPosIdx=0,
                    )
                    //--------
                    //myTempRdAddrSliced := 1
                    //--------
                    //myTempRdAddrSliced := tempInp.pxPosXGridIdx(
                    //  diffIdx
                    //)(0 downto 0)
                    //--------
                    //myTempRdAddrSliced(0) := (
                    //  tempInp.tileIdxDiffLsb
                    //)

                    //myTempRdAddrSliced := tempRdAddrDiffSliced
                    ////setBgTile(
                    ////  tempPxPosIdx=tempRdAddrDiffSliced,
                    ////  //tempPxPosIdx=1,
                    ////  //plusAmount=0,
                    ////)
                  } otherwise {
                    //--------
                    //myTempRdAddrSliced := tempInp.pxPosXGridIdx(
                    //  sameAsIdx
                    //)(0 downto 0)
                    //--------
                    //myTempRdAddrSliced := 0
                    //--------
                    //myTempRdAddrSliced(0 downto 0) := (
                    //  //tempInp.tileIdxFrontLsb
                    //  //tempInp.tileIdxDiffLsb
                    //  //tempInp.tileIdxBackLsb
                    //  //params.bgTileSize2d.x - 1
                    //  //tempInp.tileIdxBack(0)
                    //  1
                    //)
                    setBgTile(
                      tempPxPosIdx=1,
                    )
                    //myTempRdAddrSliced := 0
                    //--------
                    //myTempRdAddrSliced := tempRdAddrSameAsSliced
                    ////setBgTile(
                    ////  tempPxPosIdx=tempRdAddrSameAsSliced,
                    ////  //tempPxPosIdx=0,
                    ////  //plusAmount=1,
                    ////)
                  }
                } otherwise {
                  //tempCondX_sameAsAddrSlice_le_diffAddrSlice.valid := True
                  //tempCondX_sameAsAddrSlice_le_diffAddrSlice.payload := (
                  //  x < diffIdx
                  //)
                  // this indicates `sameAsIdx` < `diffIdx`
                  when (x < diffIdx) {
                    //--------
                    //myTempRdAddrSliced(0) := (
                    //  //tempInp.tileIdxFrontLsb
                    //  //0
                    //  tempInp.tileIdxFront(0)
                    //)
                    setBgTile(
                      tempPxPosIdx=0,
                    )
                    //myTempRdAddrSliced := 0

                    //--------
                    //myTempRdAddrSliced := tempInp.pxPosXGridIdx(
                    //  sameAsIdx
                    //)(0 downto 0)
                    //--------
                    //myTempRdAddrSliced := 0
                    //myTempRdAddrSliced := tempRdAddrSameAsSliced
                    ////myTempRdAddrSliced := tempRdAddrDiffSliced
                    ////setBgTile(
                    ////  tempPxPosIdx=tempRdAddrSameAsSliced,
                    ////  //tempPxPosIdx=0,
                    ////  //plusAmount=0,
                    ////)
                  } otherwise {
                    //myTempRdAddrSliced := 1
                    //--------
                    //myTempRdAddrSliced(0) := (
                    //  tempInp.tileIdxBack(0)
                    //)
                    setBgTile(
                      tempPxPosIdx=1,
                    )
                    //myTempRdAddrSliced := 1
                    //--------
                    //myTempRdAddrSliced := tempInp.pxPosXGridIdx(
                    //  diffIdx
                    //)(0 downto 0)
                    //--------
                    //myTempRdAddrSliced := tempRdAddrDiffSliced
                    ////myTempRdAddrSliced := tempRdAddrSameAsSliced
                    ////setBgTile(
                    ////  tempPxPosIdx=tempRdAddrDiffSliced,
                    ////  //tempPxPosIdx=1,
                    ////  //plusAmount=1,
                    ////)
                  }
                }
              }
              default {
              }
            }
            //switch (
            //  //tempInp.pxPosXGridIdx(
            //  //  tempInp.pxPosXGridIdxFindFirstSameAsIdx
            //  //)(0 downto 0)
            //  //tempInp.stage4.tileMemRdAddrSameAsGridIdx
            //  Mux[UInt](
            //    tempInp.pxPosXGridIdxFindFirstSameAsFound,
            //    tempInp.stage4.tileMemRdAddrFront(
            //      tempInp.stage4.tileMemRdAddrFront.high
            //      downto tempInp.stage4.tileMemRdAddrFront.high
            //    ),
            //    tempInp.stage4.tileMemRdAddrBack(
            //      tempInp.stage4.tileMemRdAddrBack.high
            //      downto tempInp.stage4.tileMemRdAddrBack.high
            //    )
            //  )
            //) {
            //  for (
            //    tempPxPosIdx <- 0
            //    //until (
            //    //  1
            //    //  << tempInp.stage4.tileMemRdAddrSameAsGridIdx.getWidth
            //    //)
            //    until 2
            //    //until params.numBgMemsPerNonPalKind
            //  ) {
            //    //def setBgTile(
            //    //  plusAmount: Int
            //    //): Unit = {
            //    //  //tempOutp.bgEntry(x) := (
            //    //  //  myBgEntryMemArr(
            //    //  //    (tempPxPosIdx + plusAmount) % 2
            //    //  //  ).io.rdData
            //    //  //)
            //    //  def myTileMemArr = (
            //    //    if (kind == 0) {
            //    //      bgTileMemArr
            //    //    } else {
            //    //      colorMathTileMemArr
            //    //    }
            //    //  )
            //    //  tempOutp.tileSlice(x) := myTileMemArr(
            //    //    //x
            //    //    (tempPxPosIdx + plusAmount) % 2
            //    //  ).io.rdData
            //    //}
            //    def setBgTile(
            //      plusAmount: Int
            //    ): Unit = {
            //      //tempOutp.bgEntry(x) := (
            //      //  myBgEntryMemArr(
            //      //    (tempPxPosIdx + plusAmount) % 2
            //      //  ).io.rdData
            //      //)
            //      def tempMem = (
            //        if (kind == 0) {
            //          bgTileMemArr(
            //            //plusAmount
            //            (tempPxPosIdx + plusAmount) % 2
            //          )
            //        } else {
            //          colorMathTileMemArr(
            //            //plusAmount
            //            (tempPxPosIdx + plusAmount) % 2
            //          )
            //        }
            //      )
            //      //tempOutp.tileSlice(x) := myTileMemArr(
            //      //  //x
            //      //  (tempPxPosIdx + someMemIdx) % 2
            //      //).io.rdData
            //      tempOutp.tileSlice(x) := tempMem.io.rdData
            //    }
            //    is (tempPxPosIdx) {
            //      //when (!tempInp.pxPosXGridIdxFindFirstDiffFound) {
            //      //  setBgTile(0)
            //      //} otherwise {
            //      //  def sameAsIdx = (
            //      //    tempInp.pxPosXGridIdxFindFirstSameAsIdx
            //      //  )
            //      //  def diffIdx = (
            //      //    tempInp.pxPosXGridIdxFindFirstDiffIdx
            //      //  )
            //      //  if (tempPxPosIdx == 0) {
            //      //    when (x < diffIdx) {
            //      //      setBgTile(1)
            //      //    } otherwise {
            //      //      setBgTile(0)
            //      //    }
            //      //  } else {
            //      //    when (x < sameAsIdx) {
            //      //      setBgTile(0)
            //      //    } otherwise {
            //      //      setBgTile(1)
            //      //    }
            //      //  }
            //      //}
            //      switch (Cat(
            //        tempInp.pxPosXGridIdxFindFirstSameAsFound,
            //        tempInp.pxPosXGridIdxFindFirstDiffFound,
            //      )) {
            //        is (M"-0") {
            //          // At least one of them will be found, so
            //          // this indicates `SameAsFound`
            //          //setRdAddr(
            //          //  someIdx=sameAsIdx,
            //          //  plusAmount=0,
            //          //)
            //          setBgTile(0)
            //        }
            //        is (M"01") {
            //          //setRdAddr(
            //          //  someIdx=diffIdx,
            //          //  plusAmount=1,
            //          //)
            //          setBgTile(
            //            1
            //            //0
            //          )
            //        }
            //        is (M"11") {
            //          def sameAsIdx = (
            //            tempInp.pxPosXGridIdxFindFirstSameAsIdx
            //          )
            //          def diffIdx = (
            //            tempInp.pxPosXGridIdxFindFirstDiffIdx
            //          )
            //          //if (tempPxPosIdx == 0) {
            //          //  when (x < diffIdx) {
            //          //    setBgEntry(1)
            //          //  } otherwise {
            //          //    setBgEntry(0)
            //          //  }
            //          //} else { //if (!noColorMath)
            //          //  when (x < sameAsIdx) {
            //          //    setBgEntry(0)
            //          //  } otherwise {
            //          //    setBgEntry(1)
            //          //  }
            //          //}
            //          when (sameAsIdx > diffIdx) {
            //            when (x < sameAsIdx) {
            //              setBgTile(0)
            //            } otherwise {
            //              setBgTile(1)
            //            }
            //          } otherwise {
            //            // this indicates `sameAsIdx < diffIdx`
            //            when (x < diffIdx) {
            //              setBgTile(0)
            //            } otherwise {
            //              setBgTile(1)
            //            }
            //          }
            //        }
            //        default {
            //        }
            //      }
            //      //when (!tempInp.pxPosXGridIdxFindFirstDiffFound) {
            //      //  setBgTile(0)
            //      //} otherwise {
            //      //  def sameAsIdx = (
            //      //    tempInp.pxPosXGridIdxFindFirstSameAsIdx
            //      //  )
            //      //  def diffIdx = (
            //      //    tempInp.pxPosXGridIdxFindFirstDiffIdx
            //      //  )
            //      //  if (tempPxPosIdx == 0) {
            //      //    when (x < diffIdx) {
            //      //      setBgTile(1)
            //      //    } otherwise {
            //      //      setBgTile(0)
            //      //    }
            //      //  } else {
            //      //    when (x < sameAsIdx) {
            //      //      setBgTile(0)
            //      //    } otherwise {
            //      //      setBgTile(1)
            //      //    }
            //      //  }
            //      //}
            //    }
            //  }
            //}
            //tempOutp.tile(x) := (
            //  bgTileMemArr.readAsync(address=tempInp.bgEntry(x).tileMemIdx)
            //)
          }
        }
      }
      ////HandleDualPipe(
      ////  stageData=stageData.craft(3)
      ////)(
      ////  pipeStageMainFunc=(
      ////    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    val tempInp = stageData.pipeIn(idx)
      ////    val tempOutp = stageData.pipeOut(idx)

      ////    tempOutp.tile := (
      ////      bgTileMemArr.readAsync(address=tempInp.bgEntry.tileMemIdx)
      ////    )
      ////  },
      ////  copyOnlyFunc=(
      ////    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    stageData.pipeOut(idx).tile := (
      ////      stageData.pipeIn(idx).tile
      ////    )
      ////  },
      ////)
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 8
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until params.totalNumBgKinds
          //kind <- 0 until 1
        ) {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          def tempInp = (
            if (kind == 0) {
              pipeIn.postStage0
            } else {
              pipeIn.colorMath
            }
          )
          def tempAttrs = (
            if (kind == 0) {
              pipeIn.bgAttrs
            } else {
              pipeIn.colorMathAttrs
            }
          )
          def tempOutp = (
            if (kind == 0) {
              pipeOut.postStage0
            } else {
              pipeOut.colorMath
            }
          )
          for (x <- 0 to params.bgTileSize2d.x - 1) {
            tempOutp.palEntryMemIdx(x) := tempInp.tileSlice(x).getPx(
              //tempInp.tilePxsCoord(x)
              tempInp.tilePxsCoord(x).x
            )
          }
        }
      }
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 9
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until params.totalNumBgKinds
          //kind <- 0 until 1
        ) {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          def tempInp = (
            if (kind == 0) {
              pipeIn.postStage0
            } else {
              pipeIn.colorMath
            }
          )
          def tempAttrs = (
            if (kind == 0) {
              pipeIn.bgAttrs
            } else {
              pipeIn.colorMathAttrs
            }
          )
          def tempOutp = (
            if (kind == 0) {
              pipeOut.postStage0
            } else {
              pipeOut.colorMath
            }
          )

          //tempOutp.palEntry := bgPalEntryMem.readAsync(
          //  address=tempInp.palEntryMemIdx
          //)
          for (x <- 0 to params.bgTileSize2d.x - 1) {
            tempOutp.palEntryNzMemIdx(x) := (
              tempInp.palEntryMemIdx(x) =/= 0
            )
          }
        }
      }
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 10
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until params.totalNumBgKinds
          //kind <- 0 until 1
        ) {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          def tempInp = (
            if (kind == 0) {
              pipeIn.postStage0
            } else {
              pipeIn.colorMath
            }
          )
          def tempAttrs = (
            if (kind == 0) {
              pipeIn.bgAttrs
            } else {
              pipeIn.colorMathAttrs
            }
          )
          def tempOutp = (
            if (kind == 0) {
              pipeOut.postStage0
            } else {
              pipeOut.colorMath
            }
          )

          for (x <- 0 to params.bgTileSize2d.x - 1) {
            //tempOutp.palEntry(x) := bgPalEntryMemArr.readAsync(
            //  address=tempInp.palEntryMemIdx(x)
            //)
            //tempOutp.palEntry(x) := bgPalEntryMemArr(x).io.rdData
            def myPalEntryMemArr = (
              if (kind == 0) {
                bgPalEntryMemArr
              } else {
                colorMathPalEntryMemArr
              }
            )
            myPalEntryMemArr(x).io.rdEn := True
            myPalEntryMemArr(x).io.rdAddr := tempInp.palEntryMemIdx(x)
          }
        }
      }
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 11
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until params.totalNumBgKinds
          //kind <- 0 until 1
        ) {
          //val tempInp = stageData.pipeIn(idx)
          //val tempOutp = stageData.pipeOut(idx)
          def tempInp = (
            if (kind == 0) {
              pipeIn.postStage0
            } else {
              pipeIn.colorMath
            }
          )
          def tempAttrs = (
            if (kind == 0) {
              pipeIn.bgAttrs
            } else {
              pipeIn.colorMathAttrs
            }
          )
          def tempOutp = (
            if (kind == 0) {
              pipeOut.postStage0
            } else {
              pipeOut.colorMath
            }
          )

          for (x <- 0 to params.bgTileSize2d.x - 1) {
            //tempOutp.palEntry(x) := bgPalEntryMemArr.readAsync(
            //  address=tempInp.palEntryMemIdx(x)
            //)
            def myPalEntryMemArr = (
              if (kind == 0) {
                bgPalEntryMemArr
              } else {
                colorMathPalEntryMemArr
              }
            )
            tempOutp.palEntry(x) := myPalEntryMemArr(x).io.rdData
          }
        }
      }
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 12
        val (tempInp, tempOutp) = initTempWrBgPipeOut(idx=idx)
        //val tempInp = stageData.pipeIn(idx)
        //val tempOutp = stageData.pipeOut(idx)

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
          def tempLineMemEntry = tempOutp.postStage0.subLineMemEntry(x)
          rPastLineMemEntry(x).init(rPastLineMemEntry(x).getZero)
          //when (
          //  (bgIdx === (1 << bgIdx.getWidth) - 1)
          //  //&& !tempInp.bakCnt.msb
          //) {
          //  rPastLineMemEntry(x) := rPastLineMemEntry(x).getZero
          //} otherwise {
          //  rPastLineMemEntry(x) := tempLineMemEntry
          //}
          //--------
          // BEGIN: fix this later
          when (cWrBgArr(idx).up.isFiring) {
            rPastLineMemEntry(x) := tempLineMemEntry
          }
          def setTempLineMemEntry(): Unit = {
            //rPastLineMemEntry(x) := tempLineMemEntry
            // Starting rendering a new pixel or overwrite the existing
            // pixel
            tempLineMemEntry.col.rgb := (
              tempInp.postStage0.palEntry(x).col
            )
            tempLineMemEntry.col.a := (
              tempInp.postStage0.palEntryNzMemIdx(x)
            )
            tempLineMemEntry.prio := bgIdx
            tempLineMemEntry.addr := (
              tempInp.postStage0.pxPos(x).x
              + tempInp.bgAttrs.scroll.x
            )
            if (!noColorMath) {
              tempLineMemEntry.colorMathInfo := (
                tempInp.bgAttrs.colorMathInfo
              )
              tempLineMemEntry.colorMathCol.rgb := (
                tempInp.colorMath.palEntry(x).col
              )
              tempLineMemEntry.colorMathCol.a := (
                tempInp.colorMath.palEntryNzMemIdx(x)
              )
            }
            //rPastLineMemEntry(x) := tempLineMemEntry
          }
          tempLineMemEntry := rPastLineMemEntry(x)
          when (cWrBgArr(idx).up.isValid) {
            when (
              bgIdx === (1 << bgIdx.getWidth) - 1
            ) {
              setTempLineMemEntry()
            } otherwise {
              when (
                //!rPastLineMemEntry(x).col.a
                tempInp.postStage0.palEntryNzMemIdx(x)
              ) {
                setTempLineMemEntry()
              } 
              //elsewhen (
              //  //rPastLineMemEntry(x).col.a
              //) {
              //}
              .otherwise {
                tempLineMemEntry := rPastLineMemEntry(x)
              }
            }
          }
          //when (
          //  // This could be split into more pipeline stages, but it
          //  // might not be necessary with 4 or fewer backgrounds
          //  (
          //    (
          //      (bgIdx === (1 << bgIdx.getWidth) - 1)
          //      || (
          //        //rPastLineMemEntry(x).col.a === False
          //        !rPastLineMemEntry(x).col.a
          //        //&& pastLineMemEntry(x).prio === 
          //        //--------
          //        //False
          //        //--------
          //      )
          //      //&& 
          //    ) && (
          //      tempInp.postStage0.palEntryNzMemIdx(x)
          //    ) && (
          //      !tempInp.bakCnt.msb
          //    )
          //  ) //&& tempAttrs.visib
          //  //bgIdx === 0
          //) {
          //  setTempLineMemEntry()
          //} 
          ////elsewhen (
          ////  bgIdx =/= (1 << bgIdx.getWidth) - 1
          ////) {
          ////  //tempLineMemEntry := rPastLineMemEntry(x)
          ////  tempLineMemEntry := tempLineMemEntry.getZero
          ////} 
          //  .otherwise {
          //  tempLineMemEntry := rPastLineMemEntry(x)
          //  //tempLineMemEntry := tempLineMemEntry.getZero
          //}
          // END: fix this later
          //--------
        
        //tempOutp.doWrite := (bgIdx === 0)
        }
      }
      ////HandleDualPipe(
      ////  stageData=stageData.craft(8)
      ////)(
      ////  pipeStageMainFunc=(
      ////    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    val tempInp = stageData.pipeIn(idx)
      ////    val tempOutp = stageData.pipeOut(idx)
      ////    //when (tempOutp.doWrite) {
      ////    //} otherwise {
      ////    //}
      ////    switch (
      ////      //rWrLineMemArrIdx
      ////      //wrBgPipeLast.lineMemArrIdx
      ////      tempInp.lineMemArrIdx
      ////    ) {
      ////      for (
      ////        //idx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1
      ////        idx <- 0 to (1 << tempInp.lineMemArrIdx.getWidth) - 1
      ////        //idx <- 0 to (1 << wrBgPipeLast.lineMemArrIdx.getWidth) - 1
      ////      ) {
      ////        is (idx) {
      ////          bgSubLineMemArr(idx).write(
      ////            //address=wrBgPipeLast.getCntPxPosX()(
      ////            //  log2Up(params.oneLineMemSize) - 1 downto 0
      ////            //),
      ////            address=tempWrBgPipeLineMemAddr,
      ////            data=tempOutp.lineMemEntry,
      ////          )
      ////        }
      ////      }
      ////      //default {
      ////      //  wrLineMemEntry := rPastWrLineMemEntry
      ////      //}
      ////    }
      ////    tempWrBgPipeLineMemAddr := tempInp.getCntPxPosX()(
      ////      log2Up(params.oneLineMemSize) - 1 downto 0
      ////    )
      ////  },
      ////  copyOnlyFunc=(
      ////    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    //stageData.pipeOut(idx).stage7 := (
      ////    //  stageData.pipeIn(idx).stage7
      ////    //)
      ////  },
      ////)
      ////for (x <- 0 until params.bgTileSize2d.x) {
      ////  tempWrBgPipeLineMemAddr(x) := wrBgPipeLast.getCntPxPosX()(
      ////    log2Up(
      ////      //params.oneLineMemSize
      ////      params.oneLineMemSize
      ////    ) - 1 downto 0
      ////  )
      ////}
      //for (jdx <- 0 until (1 << rWrLineMemArrIdx.getWidth)) {
      //  def tempCombine(myIdx: Int) = combineBgSubLineMemArr(myIdx)
      //  tempCombine(jdx).io.front.valid := True
      //  tempCombine(jdx).io.front.bgExt := (
      //    tempCombine(jdx).io.front.bgExt.getZero
      //  )
      //  //tempCombine(jdx).io.front.bgExt.allowOverride
      //  //tempCombine(jdx).io.front.bgExt.memAddr := (
      //  //  tempArrIdx
      //  //)
      //  //--------
      //}
      def tempArrIdx(kdx: Int) = (
        wrBgPipeLast.postStage0.subLineMemEntry(kdx)
        .getSubLineMemTempArrIdx()
      )
      val rValidPipe1 = Vec.fill(params.combineBgSubLineMemArrSize)(
        Vec.fill(wrBgPipeLineMemArrIdx.size)(
          Reg(Bool()) init(False)
        )
      )
        .setName("wrBgPipeLast_rValidPipe1")
      val rAddrPipe1 = Vec.fill(params.combineBgSubLineMemArrSize)(
        Vec.fill(wrBgPipeLineMemArrIdx.size)(
          //RegNext(tempArrIdx) init(tempArrIdx.getZero)
          Reg(cloneOf(tempArrIdx(0))) init(tempArrIdx(0).getZero)
        )
      )
        .setName("wrBgPipeLast_rAddrPipe1")
      val rDataPipe1 = Vec.fill(
        wrBgPipeLineMemArrIdx.size
        //params.combineBgSubLineMemArrSize
      )(
        //RegNext(wrBgPipeLast.postStage0.subLineMemEntry)
        Reg(cloneOf(wrBgPipeLast.postStage0.subLineMemEntry))
      )
        .setName("wrBgPipeLast_rDataPipe1")
      for (jdx <- 0 until wrBgPipeLineMemArrIdx.size) {
        for (
          kdx <- 0 until params.combineBgSubLineMemArrSize
          //params.bgTileSize2d.x
        ) {
          //println(s"jdx, kdx: ${jdx} ${kdx}")
          def tempMem = combineBgSubLineMemA2d(jdx)(kdx)
          //println(s"${rDataPipe1.size} ${rDataPipe1(0).size}")
          //println(
          //  s"tempMem: "
          //  + s"${combineBgSubLineMemA2d.size} "
          //  + s"${combineBgSubLineMemA2d(jdx).size} "
          //  //+ s"${tempMem.size}"
          //)
          tempMem.io.wrPulse.valid := (
            //True
            rValidPipe1(kdx)(jdx)
          )
          tempMem.io.wrPulse.addr := (
            //tempArrIdx
            rAddrPipe1(kdx)(jdx)
          )
          for (zdx <- 0 until params.combineBgSubLineMemVecElemSize) {
            val tempIndex = (
              kdx * params.combineBgSubLineMemArrSize + zdx
            )
            //println(s"tempIndex: ${jdx} ${kdx} ${zdx} ${tempIndex}")
            tempMem.io.wrPulse.data(zdx) := (
              //wrBgPipeLast.postStage0.subLineMemEntry
              rDataPipe1(jdx)(
                tempIndex
              )
            )
          }
        }
      }
      when (RegNext(nWrBgPipeLast.isFiring) init(False)) {
        //for (jdx <- 0 until rValidPipe1.size) {
        //  for (kdx <- 0 until rValidPipe1(jdx).size) {
        //    rValidPipe1(jdx)(kdx) := True
        //  }
        //}
        for (jdx <- 0 until wrBgPipeLineMemArrIdx.size) {
          for (
            kdx <- 0 until params.combineBgSubLineMemArrSize
            //params.bgTileSize2d.x
          ) {
            rValidPipe1(kdx)(jdx) := (
              wrBgPipeLineMemArrIdx(jdx) === jdx
            )
            rAddrPipe1(kdx)(jdx) := RegNext(tempArrIdx(kdx=kdx))
          }
          rDataPipe1(jdx) := (
            RegNext(wrBgPipeLast.postStage0.subLineMemEntry)
          )
        }
        //val tempLineMemEntry = LineMemEntry()
        //val bgIdx = wrBgPipeLast.bgIdx
        //def tempArrIdx = (
        //  wrBgPipeLast.postStage0.subLineMemEntry(0)
        //  .getSubLineMemTempArrIdx()
        //)

        //val tempFlowVec = Vec.fill(wrBgPipeLineMemArrIdx.size)(
        //  cloneOf(combineBgSubLineMemArr(0).io.wrPulse)
        //)
        //val rValidPipe1 = Vec.fill(wrBgPipeLineMemArrIdx.size)(
        //  Reg(Bool()) init(False)
        //)
        //val rAddrPipe1 = Vec.fill(rValidPipe1.size)(
        //  RegNext(tempArrIdx) init(tempArrIdx.getZero)
        //)
        //val rDataPipe1 = Vec.fill(rValidPipe1.size)(
        //  RegNext(wrBgPipeLast.postStage0.subLineMemEntry)
        //)

        //for (jdx <- 0 until wrBgPipeLineMemArrIdx.size) {
        //  rValidPipe1(jdx) := (
        //    wrBgPipeLineMemArrIdx(jdx) === jdx
        //  )
        //  combineBgSubLineMemArr(jdx).io.wrPulse.valid := (
        //    //True
        //    rValidPipe1(jdx)
        //  )
        //  combineBgSubLineMemArr(jdx).io.wrPulse.addr := (
        //    //tempArrIdx
        //    rAddrPipe1(jdx)
        //  )
        //  combineBgSubLineMemArr(jdx).io.wrPulse.data := (
        //    //wrBgPipeLast.postStage0.subLineMemEntry
        //    rDataPipe1(jdx)
        //  )
        //}
          //when (rValidPipe1(jdx)) {
          //  combineBgSubLineMemArr(jdx).io.wrPulse.valid := True
          //  combineBgSubLineMemArr(jdx).io.wrPulse.addr := (
          //    tempArrIdx
          //  )
          //  combineBgSubLineMemArr(jdx).io.wrPulse.data := (
          //    wrBgPipeLast.postStage0.subLineMemEntry
          //  )
          //} otherwise {
          //  combineBgSubLineMemArr(jdx).io.wrPulse.valid := False
          //  combineBgSubLineMemArr(jdx).io.wrPulse.addr := 0x0
          //  combineBgSubLineMemArr(jdx).io.wrPulse.data := (
          //    wrBgPipeLast.postStage0.subLineMemEntry.getZero
          //  )
          //}
        //switch (wrBgPipeLineMemArrIdx(0)) {
        //  for (jdx <- 0 until (1 << wrBgPipeLineMemArrIdx(0).getWidth)) {
        //    is (jdx) {
        //      //wrBgSubLineMemArr(jdx).write(
        //      //  address=tempArrIdx,
        //      //  data=wrBgPipeLast.postStage0.subLineMemEntry,
        //      //)
        //      //--------
        //      //// BEGIN: old `WrPulseRdPipeSimpleDualPortMem` code
        //      combineBgSubLineMemArr(jdx).io.wrPulse.valid := True
        //      combineBgSubLineMemArr(jdx).io.wrPulse.addr := (
        //        tempArrIdx
        //      )
        //      combineBgSubLineMemArr(jdx).io.wrPulse.data := (
        //        wrBgPipeLast.postStage0.subLineMemEntry
        //      )
        //      for (kdx <- 0 until combineBgSubLineMemArr.size) {
        //        if (kdx != jdx) {
        //          combineBgSubLineMemArr(kdx).io.wrPulse.valid := False
        //          combineBgSubLineMemArr(kdx).io.wrPulse.addr := 0x0
        //          combineBgSubLineMemArr(kdx).io.wrPulse.data := (
        //            wrBgPipeLast.postStage0.subLineMemEntry.getZero
        //          )
        //        }
        //      }
        //      //// END: old `WrPulseRdPipeSimpleDualPortMem` code
        //      //--------
        //      //def tempCombine(myIdx: Int) = combineBgSubLineMemArr(myIdx)
        //      //tempCombine(jdx).io.front.valid := True
        //      //tempCombine(jdx).io.front.bgExt := (
        //      //  tempCombine(jdx).io.front.bgExt.getZero
        //      //)
        //      //tempCombine(jdx).io.front.bgExt.allowOverride
        //      //tempCombine(jdx).io.front.bgExt.memAddr := (
        //      //  tempArrIdx
        //      //)
        //      //tempCombine(jdx).io.front.bgExt.modMemWord := (
        //      //  wrBgPipeLast.postStage0.subLineMemEntry
        //      //)
        //      //for (kdx <- 0 until combineBgSubLineMemArr.size) {
        //      //  if (kdx != jdx) {
        //      //    tempCombine(kdx).io.front.valid := False
        //      //    tempCombine(kdx).io.front.bgExt := (
        //      //      tempCombine(kdx).io.front.bgExt.getZero
        //      //    )
        //      //    //tempCombine(kdx).io.front.bgExt.memAddr := 0
        //      //    //tempCombine(kdx).io.front.bgExt.modMemWord := (
        //      //    //  wrBgPipeLast.postStage0.subLineMemEntry.getZero
        //      //    //)
        //      //  }
        //      //}
        //      //--------
        //    }
        //  }
        //  //default {
        //  //  for (jdx <- 0 until (1 << rWrLineMemArrIdx.getWidth)) {
        //  //    def tempCombine(myIdx: Int) = combineBgSubLineMemArr(myIdx)
        //  //    tempCombine(jdx).io.front.valid := True
        //  //    tempCombine(jdx).io.front.bgExt := (
        //  //      tempCombine(jdx).io.front.bgExt.getZero
        //  //    )
        //  //    //tempCombine(jdx).io.front.bgExt.allowOverride
        //  //    //tempCombine(jdx).io.front.bgExt.memAddr := (
        //  //    //  tempArrIdx
        //  //    //)
        //  //    //--------
        //  //  }
        //  //}
        //}
        // BEGIN: old, non-synthesizable code
        //for (x <- 0 to params.bgTileSize2d.x - 1) {
        //  //when (!wrBgPipeLast.bakCnt.msb) {
        //  //  dbgBgLineMemVec(
        //  //    //(
        //  //      rWrLineMemArrIdx
        //  //    //  + tempAttrs.scroll.y
        //  //    //)(rWrLineMemArrIdx.bitsRange)
        //  //  )(
        //  //    //wrBgPipeLast.pxPos(x).x
        //  //    //wrBgPipeLast.lineMemEntry(x).addr
        //  //    //(
        //  //      //wrBgPipeLast.pxPos(x).x
        //  //      //+ tempAttrs.scroll.x
        //  //      //- tempAttrs.scroll.x
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
        //        //  //+ tempAttrs.scroll.x
        //        //  //- tempAttrs.scroll.x
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
        //      //  //+ tempAttrs.scroll.x
        //      //  //- tempAttrs.scroll.x
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
      } otherwise { // when (!nWrBgPipeLast.isFiring)
        for (jdx <- 0 until wrBgPipeLineMemArrIdx.size) {
          for (
            kdx <- 0 until params.combineBgSubLineMemArrSize
            //params.bgTileSize2d.x
          ) {
            //println(s"jdx, kdx: ${jdx} ${kdx}")
            rValidPipe1(kdx)(jdx) := False
            rAddrPipe1(kdx)(jdx) := 0x0
            //rDataPipe1(jdx) := 
          }
        }
        //for (jdx <- 0 until combineBgSubLineMemArr.size) {
        //  //--------
        //  // BEGIN: old `WrPulseRdPipeSimpleDualPortMem` code
        //  combineBgSubLineMemArr(jdx).io.wrPulse.valid := False
        //  combineBgSubLineMemArr(jdx).io.wrPulse.addr := 0x0
        //  combineBgSubLineMemArr(jdx).io.wrPulse.data := (
        //    wrBgPipeLast.postStage0.subLineMemEntry.getZero
        //  )
        //  // END: old `WrPulseRdPipeSimpleDualPortMem` code
        //  //--------
        //  //combineBgSubLineMemArr(jdx).io.front.valid := False
        //  //combineBgSubLineMemArr(jdx).io.front.bgExt := (
        //  //  combineBgSubLineMemArr(jdx).io.front.bgExt.getZero
        //  //)
        //  //combineBgSubLineMemArr(jdx).io.front.bgExt.memAddr := 0x0
        //  //combineBgSubLineMemArr(jdx).io.front.bgExt.modMemWord := (
        //  //  wrBgPipeLast.postStage0.subLineMemEntry.getZero
        //  //)
        //  //--------
        //}
      }
      //// END: post stage 0

      //--------
      //--------
    }
    //val wrObjLineMemEntry = LineMemEntry()
    //val rPastWrObjLineMemEntry = Reg(LineMemEntry())
    //rPastWrObjLineMemEntry.init(rPastWrObjLineMemEntry.getZero)

    def writeObjLineMemEntries(
      //someWrLineMemArrIdx: Int,
      kind: Int,
    ): Unit = {
      // Handle sprites
      //val stageData = DualPipeStageData[Flow[WrObjPipePayload]](
      //  pipeIn=(
      //    if (kind == 0) {wrObjPipeIn} else {wrObjAffinePipeIn}
      //  ),
      //  pipeOut=(
      //    if (kind == 0) {wrObjPipeOut} else {wrObjAffinePipeOut}
      //  ),
      //  pipeNumMainStages=wrObjPipeNumMainStages,
      //  pipeStageIdx=0,
      //)

      //val rStage5FwdVec = Vec.fill(wrObjPipeStage5NumFwd)(
      //  Reg(WrObjPipeStage5Fwd())
      //)
      //for (fwdIdx <- 0 to rStage5FwdVec.size - 1) {
      //  rStage5FwdVec(fwdIdx).setName(f"rWrObjPipeStage5FwdVec_$fwdIdx")
      //}
      //def tempObjTileWidth = stageData.pipeIn(0).tempObjTileWidth()
      //def fwdTileWidth = (
      //  if (kind == 0) {
      //    params.objTileSize2d.x
      //  } else {
      //    params.objAffineDblTileSize2d.x
      //  }
      //)
      //val rStage12FwdV2d = Vec.fill(
      //  //params.objTileSize2d.x
      //  //tempObjTileWidth
      //  fwdTileWidth
      //)(
      //  Vec.fill(wrObjPipeStage12NumFwd)(
      //    Reg(WrObjPipeStage12Fwd(isAffine=kind != 0))
      //  )
      //)
      //for (
      //  //x <- 0 until params.objTileSize2d.x
      //  x <- 0 until fwdTileWidth
      //) {
      //  for (fwdIdx <- 0 to rStage12FwdV2d(x).size - 1) {
      //    rStage12FwdV2d(x)(fwdIdx).setName(
      //      f"rWrObj"
      //      + (
      //        if (kind == 0) {
      //          ""
      //        } else {
      //          "Affine"
      //        }
      //      )
      //      + f"PipeStage12FwdV2d_$x" + f"_$fwdIdx"
      //    )
      //  }
      //}
      //val rStage14FwdV2d = Vec.fill(
      //  //params.objTileSize2d.x
      //  fwdTileWidth
      //)(
      //  Vec.fill(wrObjPipeStage14NumFwd)(
      //    Reg(WrObjPipeStage14Fwd(isAffine=kind != 0))
      //  )
      //)
      //for (
      //  //x <- 0 until params.objTileSize2d.x
      //  x <- 0 until fwdTileWidth
      //) {
      //  for (fwdIdx <- 0 to rStage14FwdV2d(x).size - 1) {
      //    rStage14FwdV2d(x)(fwdIdx).setName(
      //      f"rWrObj"
      //      + (
      //        if (kind == 0) {
      //          ""
      //        } else {
      //          "Affine"
      //        }
      //      )
      //      + f"PipeStage14FwdV2d_$x" + f"_$fwdIdx"
      //    )
      //  }
      //}
      //val rNonAffineStage11FwdV2d = Vec.fill(params.objTileSize2d.x)(
      //  Vec.fill(wrObjPipeStage12NumFwd)(
      //    Reg(WrObjPipeStage12Fwd(false))
      //  )
      //)
      //for (x <- 0 until params.objTileSize2d.x) {
      //  for (fwdIdx <- 0 to rNonAffineStage11FwdV2d(x).size - 1) {
      //    rNonAffineStage11FwdV2d(x)(fwdIdx).setName(
      //      f"rWrObjPipeStage11FwdV2d_$kind" + f"_$x" + f"_$fwdIdx"
      //    )
      //  }
      //}
      //val rNonAffineStage14FwdV2d = Vec.fill(params.objTileSize2d.x)(
      //  Vec.fill(wrObjPipeStage14NumFwd)(
      //    Reg(WrObjPipeStage14Fwd(false))
      //  )
      //)
      //for (x <- 0 until params.objTileSize2d.x) {
      //  for (fwdIdx <- 0 to rNonAffineStage14FwdV2d(x).size - 1) {
      //    rNonAffineStage14FwdV2d(x)(fwdIdx).setName(
      //      f"rWrObjPipeStage14FwdV2d_$kind" + f"_$x" + f"_$fwdIdx"
      //    )
      //  }
      //}
      //val rAffineStage11FwdV2d = Vec.fill(
      //  //params.objAffineDblTileSize2d.x
      //  params.objAffineSliceTileWidth
      //)(
      //  Vec.fill(wrObjPipeStage12NumFwd)(
      //    Reg(WrObjPipeStage12Fwd(true))
      //  )
      //)
      //for (
      //  //x <- 0 until params.objAffineDblTileSize2d.x
      //  x <- 0 until params.objAffineSliceTileWidth
      //) {
      //  for (fwdIdx <- 0 to rAffineStage11FwdV2d(x).size - 1) {
      //    rAffineStage11FwdV2d(x)(fwdIdx).setName(
      //      f"rWrObjAffinePipeStage11FwdV2d_$kind" + f"_$x" + f"_$fwdIdx"
      //    )
      //  }
      //}
      //val rAffineStage14FwdV2d = Vec.fill(
      //  //params.objAffineDblTileSize2d.x
      //  params.objAffineSliceTileWidth
      //)(
      //  Vec.fill(wrObjPipeStage14NumFwd)(
      //    Reg(WrObjPipeStage14Fwd(true))
      //  )
      //)
      //for (
      //  //x <- 0 until params.objAffineDblTileSize2d.x
      //  x <- 0 until params.objAffineSliceTileWidth
      //) {
      //  for (fwdIdx <- 0 to rAffineStage13FwdV2d(x).size - 1) {
      //    rAffineStage13FwdV2d(x)(fwdIdx).setName(
      //      f"rWrObjAffinePipeStage13FwdV2d_$kind" + f"_$x" + f"_$fwdIdx"
      //    )
      //  }
      //}
      //def rStage11FwdV2d(kind: Int) = (
      //  if (kind == 0) {
      //    rNonAffineStage11FwdV2d
      //  } else {
      //    rAffineStage11FwdV2d
      //  }
      //)
      //def rStage13FwdV2d(kind: Int) = (
      //  if (kind == 0) {
      //    rNonAffineStage13FwdV2d
      //  } else {
      //    rAffineStage13FwdV2d
      //  }
      //)
      // BEGIN: stage 0
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 0
        //stageData.pipeOut(idx).cnt := (
        //  stageData.pipeIn(idx).cnt
        //)
        //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
        //def tempInp = stageData.pipeIn(0).stage0
        //def tempOutp = stageData.pipeOut(0).stage0
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        //val tempInp: WrObjPipePayload = pipeIn.payload
        //val tempOutp: WrObjPipePayload = pipeOut.payload
        //def tempInp = pipeIn
        //def tempOutp = pipeOut
        pipeOut.stage0.justCopy := pipeIn.stage0.justCopy
        //tempOutp.gridIdxLsb := tempInp.calcGridIdxLsb()

        //tempOutp.innerObjAttrsMemIdx := tempInp.bakCnt(
        //  //bakCnt.high - 1
        //  //downto (bakCnt.high - 1 - params.objAttrsMemIdxWidth + 1)
        //  //bakCnt.high
        //  //downto (bakCnt.high - (params.objAttrsMemIdxWidth - 1))
        //  //(params.objAttrsMemIdxWidth + myTempObjTileWidthPow - 1)
        //  //downto myTempObjTileWidthPow

        //  params.objAttrsMemIdxWidth + myTempObjTileWidthPow - 1
        //  downto myTempObjTileWidthPow
        //)
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(0)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).cnt := (
      //    //  stageData.pipeIn(idx).cnt
      //    //)
      //    //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
      //    def tempInp = stageData.pipeIn(0).stage0
      //    def tempOutp = stageData.pipeOut(0).stage0
      //    tempOutp.justCopy := tempInp.justCopy
      //    //tempOutp.gridIdxLsb := tempInp.calcGridIdxLsb()

      //    //tempOutp.innerObjAttrsMemIdx := tempInp.bakCnt(
      //    //  //bakCnt.high - 1
      //    //  //downto (bakCnt.high - 1 - params.objAttrsMemIdxWidth + 1)
      //    //  //bakCnt.high
      //    //  //downto (bakCnt.high - (params.objAttrsMemIdxWidth - 1))
      //    //  //(params.objAttrsMemIdxWidth + myTempObjTileWidthPow - 1)
      //    //  //downto myTempObjTileWidthPow

      //    //  params.objAttrsMemIdxWidth + myTempObjTileWidthPow - 1
      //    //  downto myTempObjTileWidthPow
      //    //)
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).cnt := (
      //    //  stageData.pipeIn(idx).cnt
      //    //)
      //    stageData.pipeOut(idx).stage0 := (
      //      stageData.pipeIn(idx).stage0
      //    )
      //  },
      //)
      ////HandleDualPipe(
      ////  stageData=stageData.craft(0)
      ////)(
      ////  pipeStageMainFunc=(
      ////    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    stageData.pipeOut(idx).cntMinus1 := (
      ////      stageData.pipeIn(idx).cntMinus1
      ////    )
      ////  },
      ////  copyOnlyFunc=(
      ////    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    stageData.pipeOut(idx).cntMinus1 := (
      ////      stageData.pipeIn(idx).cntMinus1
      ////    )
      ////  },
      ////)
      // END: Stage 0

      // BEGIN: Post stage 0
      // BEGIN: Stage 1
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 1
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        //val tempInp = stageData.pipeIn(idx)
        //val tempOutp = stageData.pipeOut(idx)
        val tempInp = pipeIn
        val tempOutp = pipeOut

        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = (
          tempObjTileWidthPow(kind != 0)
        )
        //val objAttrsMemIdxIn = tempInp.objAttrsMemIdx
        //tempOutp.objAttrs := objAttrsMem.readAsync(
        //  address=tempInp.objAttrsMemIdx(),
        //)
        objAttrsMemArr(kind).io.rdEn := True
        objAttrsMemArr(kind).io.rdAddr := (
          RegNext(objAttrsMemArr(kind).io.rdAddr) init(0x0)
        )
        when (cWrObjArr(idx).up.isFiring) {
          objAttrsMemArr(kind).io.rdAddr := (
            if (kind == 0) {
              tempInp.objAttrsMemIdx.resized
            } else {
              tempInp.stage0.affineObjAttrsMemIdx().resized
            }
          )
        }
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(1)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      //    def myTempObjTileWidthPow = (
      //      tempObjTileWidthPow(kind != 0)
      //    )
      //    //val objAttrsMemIdxIn = tempInp.objAttrsMemIdx
      //    //tempOutp.objAttrs := objAttrsMem.readAsync(
      //    //  address=tempInp.objAttrsMemIdx(),
      //    //)
      //    objAttrsMemArr(kind).io.rdEn := True
      //    objAttrsMemArr(kind).io.rdAddr := (
      //      if (kind == 0) {
      //        tempInp.objAttrsMemIdx.resized
      //      } else {
      //        tempInp.stage0.affineObjAttrsMemIdx().resized
      //      }
      //    )
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).objAttrs := (
      //    //  stageData.pipeIn(idx).objAttrs
      //    //)
      //    //stageData.pipeOut(idx).stage2 := stageData.pipeIn(idx).stage2
      //  },
      //)
      {
        def idx = 2
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        //val tempInp = stageData.pipeIn(idx)
        //val tempOutp = stageData.pipeOut(idx)
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = (
          tempObjTileWidthPow(kind != 0)
        )
        //val objAttrsMemIdxIn = tempInp.objAttrsMemIdx
        //tempOutp.objAttrs := objAttrsMem.readAsync(
        //  address=tempInp.objAttrsMemIdx(),
        //)
        //objAttrsMem.io.rdEn := True
        //objAttrsMem.io.rdAddr := tempInp.objAttrsMemIdx()
        tempOutp.objAttrs := (
          RegNext(tempOutp.objAttrs) init(tempOutp.objAttrs.getZero)
        )
        when (cWrObjArr(idx).up.isFiring) {
          tempOutp.objAttrs := objAttrsMemArr(kind).io.rdData
        }
      }
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 3
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        //val tempInp = stageData.pipeIn(idx)
        //val tempOutp = stageData.pipeOut(idx)
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = (
          tempObjTileWidthPow(kind != 0)
        )
        //val objAttrsMemIdxIn = tempInp.objAttrsMemIdx
        //tempInp.objAttrs := objAttrsMem.readAsync(
        //  address=tempInp.objAttrsMemIdx(),
        //)
        //objAttrsMem.io.rdEn := True
        //objAttrsMem.io.rdAddr := tempInp.objAttrsMemIdx()
        //tempInp.objAttrs := (
        //  RegNext(tempInp.objAttrs) init(tempInp.objAttrs.getZero)
        //)
        //when (cWrObjArr(idx).up.isFiring) {
        //  tempInp.objAttrs := objAttrsMemArr(kind).io.rdData
        //}
        val tempObjXStart = (
          if (kind == 0) {
            tempInp.objXStart()
          } else {
            tempInp.affineObjXStart()
          }
        )
          .setName(f"wrObjPipe2_tempObjXStart_$kind")
        //if (kind == 1) {
          for (x <- 0 until tempObjTileWidth1(kind != 0)) {
            tempOutp.myIdxPxPosX(x) := (
              tempInp.objAttrs.pos.x.asUInt
              + x
              + (
                //if (kind == 0) {
                //  tempInp.objXStart()
                //} else {
                //  tempInp.affineObjXStart()
                //}
                tempObjXStart
              )
            ).asSInt
          }
        //}

        for (x <- 0 until myTempObjTileWidth) {
          val tileX = (
            (
              //if (kind == 0) {
              //  val tempWidth = tempInp.affineObjXStart().getWidth
              //  U(f"$tempWidth'd0")
              //} else {
                //tempInp.affineObjXStart()
                tempObjXStart
              //}
            ) + x
          )
            .setName(f"wrObjPipe2_tileX_$kind" + f"_$x")
          tempOutp.pxPos(x).x := (
            tempInp.objAttrs.pos.x.asUInt
            //+ tempOutp.tilePxsCoord(x).x
            + tileX
            //- (
            //  if (kind == 0) {
            //    0
            //  } else { // if (kind == 1)
            //    //-(params.objAffineTileSize2d.x / 2)
            //    (params.objTileSize2d.x / 2)
            //    //-params.objTileSize2d.x
            //  }
            //)
          ).asSInt
          tempOutp.pxPos(x).y(
            tempInp.lineNum.bitsRange
          ) := (
            tempInp.lineNum.asSInt
            //+ (
            //  if (kind == 0) {
            //    0
            //  } else {
            //    -(params.objTileSize2d.y / 2)
            //    //-params.objTileSize2d.y
            //  }
            //)
          )//(tempInp.lineNum.bitsRange)
          tempOutp.pxPos(x).y(
            //tempOutp.pxPos.y.high downto tempInp.lineNum.high
            tempOutp.pxPos(x).y.high
            downto tempInp.lineNum.getWidth
          ) := 0x0
          //if (kind == 1) {
          //  tempOutp.affinePxPos(x).x := (
          //    tempOutp.pxPos(x).x
          //    - (params.objTileSize2d.x / 2)
          //    //- params.objTileSize2d.x
          //  )
          //  tempOutp.affinePxPos(x).y := (
          //    tempOutp.pxPos(x).y
          //    - (params.objTileSize2d.y / 2)
          //    //-params.objTileSize2d.y
          //  )
          //}
        }
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(2)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      //    def myTempObjTileWidthPow = (
      //      tempObjTileWidthPow(kind != 0)
      //    )
      //    //val objAttrsMemIdxIn = tempInp.objAttrsMemIdx
      //    //tempOutp.objAttrs := objAttrsMem.readAsync(
      //    //  address=tempInp.objAttrsMemIdx(),
      //    //)
      //    //objAttrsMem.io.rdEn := True
      //    //objAttrsMem.io.rdAddr := tempInp.objAttrsMemIdx()
      //    tempOutp.objAttrs := objAttrsMemArr(kind).io.rdData
      //    val tempObjXStart = (
      //      if (kind == 0) {
      //        tempInp.objXStart()
      //      } else {
      //        tempInp.affineObjXStart()
      //      }
      //    )
      //      .setName(f"wrObjPipe2_tempObjXStart_$kind")
      //    //if (kind == 1) {
      //      for (x <- 0 until tempObjTileWidth1(kind != 0)) {
      //        tempOutp.myIdxPxPosX(x) := (
      //          tempOutp.objAttrs.pos.x.asUInt
      //          + x
      //          + (
      //            //if (kind == 0) {
      //            //  tempInp.objXStart()
      //            //} else {
      //            //  tempInp.affineObjXStart()
      //            //}
      //            tempObjXStart
      //          )
      //        ).asSInt
      //      }
      //    //}

      //    for (x <- 0 until myTempObjTileWidth) {
      //      val tileX = (
      //        (
      //          //if (kind == 0) {
      //          //  val tempWidth = tempInp.affineObjXStart().getWidth
      //          //  U(f"$tempWidth'd0")
      //          //} else {
      //            //tempInp.affineObjXStart()
      //            tempObjXStart
      //          //}
      //        ) + x
      //      )
      //        .setName(f"wrObjPipe2_tileX_$kind" + f"_$x")
      //      tempOutp.pxPos(x).x := (
      //        tempOutp.objAttrs.pos.x.asUInt
      //        //+ tempOutp.tilePxsCoord(x).x
      //        + tileX
      //        //- (
      //        //  if (kind == 0) {
      //        //    0
      //        //  } else { // if (kind == 1)
      //        //    //-(params.objAffineTileSize2d.x / 2)
      //        //    (params.objTileSize2d.x / 2)
      //        //    //-params.objTileSize2d.x
      //        //  }
      //        //)
      //      ).asSInt
      //      tempOutp.pxPos(x).y(
      //        tempInp.lineNum.bitsRange
      //      ) := (
      //        tempInp.lineNum.asSInt
      //        //+ (
      //        //  if (kind == 0) {
      //        //    0
      //        //  } else {
      //        //    -(params.objTileSize2d.y / 2)
      //        //    //-params.objTileSize2d.y
      //        //  }
      //        //)
      //      )//(tempInp.lineNum.bitsRange)
      //      tempOutp.pxPos(x).y(
      //        //tempOutp.pxPos.y.high downto tempInp.lineNum.high
      //        tempOutp.pxPos(x).y.high
      //        downto tempInp.lineNum.getWidth
      //      ) := 0x0
      //      //if (kind == 1) {
      //      //  tempOutp.affinePxPos(x).x := (
      //      //    tempOutp.pxPos(x).x
      //      //    - (params.objTileSize2d.x / 2)
      //      //    //- params.objTileSize2d.x
      //      //  )
      //      //  tempOutp.affinePxPos(x).y := (
      //      //    tempOutp.pxPos(x).y
      //      //    - (params.objTileSize2d.y / 2)
      //      //    //-params.objTileSize2d.y
      //      //  )
      //      //}
      //    }
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).objAttrs := (
      //    //  stageData.pipeIn(idx).objAttrs
      //    //)
      //    //stageData.pipeOut(idx).stage2 := stageData.pipeIn(idx).stage2
      //    def pipeIn = stageData.pipeIn(idx)
      //    def pipeOut = stageData.pipeOut(idx)
      //    pipeOut.stage2 := pipeIn.stage2
      //  },
      //)
      // END: Stage 3

      // BEGIN: Stage 4
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 4
        //def kind = 1
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = (
          tempObjTileWidthPow(kind != 0)
        )

        if (kind == 1) {
          for (x <- 0 until myTempObjTileWidth) {
            //def tileX = tempInp.affineObjXStart() + x
            val tileX = (tempInp.affineObjXStart() + x)
              .setName(f"wrObjPipe3_tileX_$kind" + f"_$x")
            def myMultAX = tempOutp.stage4.multAX(x)
            def myMultBY = tempOutp.stage4.multBY(x)
            def myMultCX = tempOutp.stage4.multCX(x)
            def myMultDY = tempOutp.stage4.multDY(x)

            myMultAX := (
              //tempInp.pxPos(x).x - 
              (
                Cat(False, tileX).asSInt.resized
                - {
                  //def tempTileWidth = params.objAffineTileSize2d.x
                  def tempTileWidth = (
                    //params.objAffineTileSize2d.x / 2
                    params.objAffineTileSize2d.x
                  )
                  def tempWidth = tileX.getWidth + 1 + 1
                  S(f"$tempWidth'd$tempTileWidth")
                }
              ) * tempInp.objAttrs.affine.matA
            ).resized
            myMultBY := (
              //tempInp.pxPos(x).y
              (
                //tempInp.pxPos(x).y
                tempInp.lineNum.asSInt.resized
                - (
                  tempInp.objAttrs.pos.y
                  //- (params.objAffineTileSize2d.y / 2)
                ) - (
                  //params.objAffineTileSize2d.y / 2
                  params.objAffineTileSize2d.y
                )
              ) * tempInp.objAttrs.affine.matB
            ).resized
            myMultCX := (
              //tempInp.pxPos(x).x
              (
                //tileX.asSInt - params.objAffineTileSize2d.x
                //tileX.asSInt.resized
                Cat(False, tileX).asSInt.resized
                - {
                  //def tempTileWidth = (
                  //  params.objAffineTileSize2d.x / 2
                  //)
                  def tempTileWidth = (
                    //params.objAffineTileSize2d.x / 2
                    params.objAffineTileSize2d.x
                  )
                  def tempWidth = tileX.getWidth + 1 + 1
                  S(f"$tempWidth'd$tempTileWidth")
                }
              ) * tempInp.objAttrs.affine.matC
            ).resized
            myMultDY := (
              //tempInp.pxPos(x).y
              (
                tempInp.pxPos(x).y
                - (
                  tempInp.objAttrs.pos.y
                  //- (params.objAffineTileSize2d.y / 2)
                ) - (
                  //params.objAffineTileSize2d.y / 2
                  params.objAffineTileSize2d.y
                )
              ) * tempInp.objAttrs.affine.matD
            ).resized
          }
        }
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(3)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //def kind = 1
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      //    def myTempObjTileWidthPow = (
      //      tempObjTileWidthPow(kind != 0)
      //    )

      //    if (kind == 1) {
      //      for (x <- 0 until myTempObjTileWidth) {
      //        //def tileX = tempInp.affineObjXStart() + x
      //        val tileX = (tempInp.affineObjXStart() + x)
      //          .setName(f"wrObjPipe3_tileX_$kind" + f"_$x")
      //        def myMultAX = tempOutp.stage3.multAX(x)
      //        def myMultBY = tempOutp.stage3.multBY(x)
      //        def myMultCX = tempOutp.stage3.multCX(x)
      //        def myMultDY = tempOutp.stage3.multDY(x)

      //        myMultAX := (
      //          //tempInp.pxPos(x).x - 
      //          (
      //            Cat(False, tileX).asSInt.resized
      //            - {
      //              //def tempTileWidth = params.objAffineTileSize2d.x
      //              def tempTileWidth = (
      //                //params.objAffineTileSize2d.x / 2
      //                params.objAffineTileSize2d.x
      //              )
      //              def tempWidth = tileX.getWidth + 1
      //              S(f"$tempWidth'd$tempTileWidth")
      //            }
      //          ) * tempInp.objAttrs.affine.matA
      //        ).resized
      //        myMultBY := (
      //          //tempInp.pxPos(x).y
      //          (
      //            //tempInp.pxPos(x).y
      //            tempInp.lineNum.asSInt.resized
      //            - (
      //              tempInp.objAttrs.pos.y
      //              //- (params.objAffineTileSize2d.y / 2)
      //            ) - (
      //              //params.objAffineTileSize2d.y / 2
      //              params.objAffineTileSize2d.y
      //            )
      //          ) * tempInp.objAttrs.affine.matB
      //        ).resized
      //        myMultCX := (
      //          //tempInp.pxPos(x).x
      //          (
      //            //tileX.asSInt - params.objAffineTileSize2d.x
      //            //tileX.asSInt.resized
      //            Cat(False, tileX).asSInt.resized
      //            - {
      //              //def tempTileWidth = (
      //              //  params.objAffineTileSize2d.x / 2
      //              //)
      //              def tempTileWidth = (
      //                //params.objAffineTileSize2d.x / 2
      //                params.objAffineTileSize2d.x
      //              )
      //              def tempWidth = tileX.getWidth + 1
      //              S(f"$tempWidth'd$tempTileWidth")
      //            }
      //          ) * tempInp.objAttrs.affine.matC
      //        ).resized
      //        myMultDY := (
      //          //tempInp.pxPos(x).y
      //          (
      //            tempInp.pxPos(x).y
      //            - (
      //              tempInp.objAttrs.pos.y
      //              //- (params.objAffineTileSize2d.y / 2)
      //            ) - (
      //              //params.objAffineTileSize2d.y / 2
      //              params.objAffineTileSize2d.y
      //            )
      //          ) * tempInp.objAttrs.affine.matD
      //        ).resized
      //      }
      //    }
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    def pipeIn = stageData.pipeIn(idx)
      //    def pipeOut = stageData.pipeOut(idx)
      //    pipeOut.stage3 := pipeIn.stage3
      //  }
      //)
      // END: Stage 4

      {
        def idx = 5
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = (
          tempObjTileWidthPow(kind != 0)
        )
        if (kind == 1) {
          for (x <- 0 until myTempObjTileWidth) {
            //val dbgTestFxTilePxsCoord = cloneOf(
            //  tempOutp.stage5.fxTilePxsCoord(x)
            //)
            //  .setName(f"dbgTestWrObjPipe4_fxTilePxsCoord_$x")
            tempOutp.stage5.dbgTestFxTilePxsCoord(x).x := (
              tempInp.stage4.multAX(x)
              + tempInp.stage4.multBY(x)
            ).resized
            tempOutp.stage5.dbgTestFxTilePxsCoord(x).y := (
              tempInp.stage4.multCX(x)
              + tempInp.stage4.multDY(x)
            ).resized
          }
        }
      }

      // BEGIN: Stage 6
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 6
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = (
          tempObjTileWidthPow(kind != 0)
        )

        //objTileMemArr(kind).io.rdEn := True
        //objTileMemArr(kind).io.rdAddr := (
        //  tempInp.objAttrs.tileIdx
        //)
        if (kind == 1) {
          for (x <- 0 until myTempObjTileWidth) {
            //val dbgTestFxTilePxsCoord = cloneOf(
            //  tempOutp.stage5.fxTilePxsCoord(x)
            //)
            //  .setName(f"dbgTestWrObjPipe4_fxTilePxsCoord_$x")
            //dbgTestFxTilePxsCoord.x := (
            //  tempInp.stage3.multAX(x)
            //  + tempInp.stage3.multBY(x)
            //).resized
            //dbgTestFxTilePxsCoord.y := (
            //  tempInp.stage3.multCX(x)
            //  + tempInp.stage3.multDY(x)
            //).resized
            //when (tempInp.stage0.affineActive) {
              tempOutp.stage6.fxTilePxsCoord(x).x := (
                tempInp.stage5.dbgTestFxTilePxsCoord(x).x
                + (
                  //(params.objTileSize2d.x / 2)
                  (params.objAffineTileSize2d.x / 2)
                  //params.objAffineTileSize2d.x
                  << (
                    Gpu2dAffine.fracWidth //+ 1//2
                  )
                )
                //Cat(
                //  //False,
                //  //dbgTestFxTilePxsCoord.x >> 1
                //  B"00",
                //  dbgTestFxTilePxsCoord.x >> 2
                //).asUInt
                //+ Mux[SInt](
                //  dbgTestFxTilePxsCoord.x < 0,
                //  //S(
                //  //  dbgTestFxTilePxsCoord.x.getWidth bits,
                //    1 << (Gpu2dAffine.fracWidth - 1)
                //  //)
                //  ,
                //  //S(
                //  //  dbgTestFxTilePxsCoord.x.getWidth bits,
                //    -1 << (Gpu2dAffine.fracWidth - 1)
                //  //)
                //)
              )
              tempOutp.stage6.fxTilePxsCoord(x).y := (
                //dbgTestFxTilePxsCoord.y
                tempInp.stage5.dbgTestFxTilePxsCoord(x).y
                + (
                  //(params.objTileSize2d.y / 2)
                  (params.objAffineTileSize2d.y / 2)
                  //params.objAffineTileSize2d.y
                  << (
                    Gpu2dAffine.fracWidth //+ 1//2
                  )
                )
                //Cat(
                //  B"00",
                //  //dbgTestFxTilePxsCoord.y >> 1
                //  dbgTestFxTilePxsCoord.y >> 2
                //).asUInt
                //+ (1 << (Gpu2dAffine.fracWidth - 1))
                //+ Mux[SInt](
                //  dbgTestFxTilePxsCoord.y < 0,
                //  //S(
                //  //  dbgTestFxTilePxsCoord.x.getWidth bits,
                //    1 << (Gpu2dAffine.fracWidth - 1)
                //  //)
                //  ,
                //  //S(
                //  //  dbgTestFxTilePxsCoord.x.getWidth bits,
                //    -1 << (Gpu2dAffine.fracWidth - 1)
                //  //)
                //)
              )
            //} otherwise {
            //  tempOutp.stage5.fxTilePxsCoord(x).x := 0
            //  tempOutp.stage5.fxTilePxsCoord(x).y := 0
            //}
          }
        }
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(5)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      //    def myTempObjTileWidthPow = (
      //      tempObjTileWidthPow(kind != 0)
      //    )

      //    //objTileMemArr(kind).io.rdEn := True
      //    //objTileMemArr(kind).io.rdAddr := (
      //    //  tempInp.objAttrs.tileIdx
      //    //)
      //    if (kind == 1) {
      //      for (x <- 0 until myTempObjTileWidth) {
      //        val dbgTestFxTilePxsCoord = cloneOf(
      //          tempOutp.stage5.fxTilePxsCoord(x)
      //        )
      //          .setName(f"dbgTestWrObjPipe4_fxTilePxsCoord_$x")
      //        dbgTestFxTilePxsCoord.x := (
      //          tempInp.stage3.multAX(x)
      //          + tempInp.stage3.multBY(x)
      //        ).resized
      //        dbgTestFxTilePxsCoord.y := (
      //          tempInp.stage3.multCX(x)
      //          + tempInp.stage3.multDY(x)
      //        ).resized
      //        //when (tempInp.stage0.affineActive) {
      //          tempOutp.stage5.fxTilePxsCoord(x).x := (
      //            dbgTestFxTilePxsCoord.x
      //            + (
      //              //(params.objTileSize2d.x / 2)
      //              (params.objAffineTileSize2d.x / 2)
      //              //params.objAffineTileSize2d.x
      //              << (
      //                Gpu2dAffine.fracWidth //+ 1//2
      //              )
      //            )
      //            //Cat(
      //            //  //False,
      //            //  //dbgTestFxTilePxsCoord.x >> 1
      //            //  B"00",
      //            //  dbgTestFxTilePxsCoord.x >> 2
      //            //).asUInt
      //            //+ Mux[SInt](
      //            //  dbgTestFxTilePxsCoord.x < 0,
      //            //  //S(
      //            //  //  dbgTestFxTilePxsCoord.x.getWidth bits,
      //            //    1 << (Gpu2dAffine.fracWidth - 1)
      //            //  //)
      //            //  ,
      //            //  //S(
      //            //  //  dbgTestFxTilePxsCoord.x.getWidth bits,
      //            //    -1 << (Gpu2dAffine.fracWidth - 1)
      //            //  //)
      //            //)
      //          )
      //          tempOutp.stage5.fxTilePxsCoord(x).y := (
      //            dbgTestFxTilePxsCoord.y
      //            + (
      //              //(params.objTileSize2d.y / 2)
      //              (params.objAffineTileSize2d.y / 2)
      //              //params.objAffineTileSize2d.y
      //              << (
      //                Gpu2dAffine.fracWidth //+ 1//2
      //              )
      //            )
      //            //Cat(
      //            //  B"00",
      //            //  //dbgTestFxTilePxsCoord.y >> 1
      //            //  dbgTestFxTilePxsCoord.y >> 2
      //            //).asUInt
      //            //+ (1 << (Gpu2dAffine.fracWidth - 1))
      //            //+ Mux[SInt](
      //            //  dbgTestFxTilePxsCoord.y < 0,
      //            //  //S(
      //            //  //  dbgTestFxTilePxsCoord.x.getWidth bits,
      //            //    1 << (Gpu2dAffine.fracWidth - 1)
      //            //  //)
      //            //  ,
      //            //  //S(
      //            //  //  dbgTestFxTilePxsCoord.x.getWidth bits,
      //            //    -1 << (Gpu2dAffine.fracWidth - 1)
      //            //  //)
      //            //)
      //          )
      //        //} otherwise {
      //        //  tempOutp.stage5.fxTilePxsCoord(x).x := 0
      //        //  tempOutp.stage5.fxTilePxsCoord(x).y := 0
      //        //}
      //      }
      //    }
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    def pipeIn = stageData.pipeIn(idx)
      //    def pipeOut = stageData.pipeOut(idx)
      //    pipeOut.stage4 := pipeIn.stage4
      //  }
      //)
      // END: Stage 6

      // BEGIN: Stage 7
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 7
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileHeight = tempObjTileHeight(kind != 0)
        def myTempObjTileWidthPow = (
          tempObjTileWidthPow(kind != 0)
        )

        //tempOutp.tilePxsCoord.x := tempInp.getCntTilePxsCoordX()
        //tempOutp.tilePxsCoord.y := tempInp.objAttrs.pos
        //when (tempInp.pxPosInLine) {
        //  //tempOutp.tilePxsCoord.x 
        //} otherwise {
        //  tempOutp.tilePxsCoord := tempInp.tilePxsCoord
        //}
        if (kind == 0) {
          objTileMemArr(kind).io.rdEn := True
          objTileMemArr(kind).io.rdAddr := (
            RegNext(objTileMemArr(kind).io.rdAddr)
            //init(objTileMemArr(kind).io.rdAddr.getZero)
            init(0x0)
          )
          when (cWrObjArr(idx).up.isFiring) {
            objTileMemArr(kind).io.rdAddr := (
              if (
                (kind == 0 && params.objTileWidthRshift > 0)
                //|| (kind == 1 && params.objAffineTileWidthRshift > 0)
              ) {
                Cat(
                  tempInp.objAttrs.tileIdx,
                  tempOutp.tilePxsCoord(0).y,
                  (
                    tempInp.objXStart()(
                      //tempInp.objXStart().high
                      params.objTileSize2dPow.x - 1
                      downto params.objSliceTileWidthPow
                      //downto params.objTileWidthRshift
                      //params.objTileWidthRshift - 1 downto 0
                    )
                  ),
                ).asUInt
              } else {
                Cat(
                  tempInp.objAttrs.tileIdx,
                  tempOutp.tilePxsCoord(0).y,
                ).asUInt
              }
            )
          }
        } else { // if (kind == 1)
          for (x <- 0 until myTempObjTileWidth) {
            objAffineTileMemArr(x).io.rdEn := True
            //def tempX = (
            //  U{
            //    def tempWidthPow = (
            //      //params.objAffineTileWidthRshift
            //      //params.objAffineTileSize2dPow.x
            //      params.objAffineSliceTileWidthPow
            //    )
            //    f"$tempWidthPow'd$x"
            //  }
            //)
            val fxTileX = (
              (
                tempInp.stage6.fxTilePxsCoord(x).x
                //+ (1 << (Gpu2dAffine.fracWidth - 1))
              ) >> (
                Gpu2dAffine.fracWidth
              )
            )
            objAffineTileMemArr(x).io.rdAddr := (
              //if (
              //  //(kind == 0 && params.objTileWidthRshift > 0)
              //  //|| 
              //  (kind == 1 && params.objAffineTileWidthRshift > 0)
              //) {
              //  Cat(
              //    tempInp.objAttrs.tileIdx,
              //    tempOutp.tilePxsCoord(x).y,
              //    //(
              //    //  tempInp.affineObjXStart()(
              //    //    ////tempInp.affineObjXStart().high
              //    //    params.objAffineTileSize2dPow.x - 1
              //    //    downto params.objAffineSliceTileWidthPow
              //    //    //downto params.objAffineTileWidthRshift
              //    //    //params.objAffineTileWidthRshift - 1 downto 0
              //    //  )
              //    //),
              //    //tempX,
              //    tempOutp.tilePxsCoord(x).x,
              //  ).asUInt
              //} else {
                Cat(
                  tempInp.objAttrs.tileIdx,
                  tempOutp.tilePxsCoord(x).y,
                  //tempX,
                  //(
                  //  tempInp.affineObjXStart()(
                  //    params.objAffineTileSize2dPow.x - 1 downto 0
                  //  )
                  //  | tempOutp.tilePxsCoord(x).x.resized
                  //),
                  //(
                  //  tempInp.affineObjXStart()(
                  //    ////tempInp.affineObjXStart().high
                  //    params.objAffineTileSize2dPow.x - 1
                  //    downto params.objAffineSliceTileWidthPow
                  //    //downto params.objAffineTileWidthRshift
                  //    //params.objAffineTileWidthRshift - 1 downto 0
                  //  )
                  //),
                  fxTileX(
                    params.objAffineTileSize2dPow.x - 1 downto 0
                  ),
                ).asUInt
              //}
            )
          }
        }
        for (x <- 0 until myTempObjTileWidth) {
          if (kind == 0) {
            val tempX = KeepAttribute(
              x
              + tempInp.objXStart()
            )
              .setName(s"wrObjPipe_6_tempX_${x}")
            val tempY = KeepAttribute((
              //rWrLineNum - tempInp.objAttrs.pos.y.asUInt
              (
                tempInp.lineNum.asSInt.resized
                - (
                  tempInp.objAttrs.pos.y
                  //- (params.objTileSize2d.y >> 1)
                  ////- params.objTileSize2d.y
                )
              ).asUInt
            )(
              tempOutp.tilePxsCoord(x).y.bitsRange
            ))
              .setName(s"wrObjPipe_6_tempY_${x}")
            //tempOutp.tilePxsCoord(x).x := (
            //  x
            //  + tempInp.objXStart()
            //).resized
            //tempOutp.tilePxsCoord(x).y := (
            //  //rWrLineNum - tempInp.objAttrs.pos.y.asUInt
            //  (
            //    tempInp.lineNum.asSInt.resized
            //    - (
            //      tempInp.objAttrs.pos.y
            //      //- (params.objTileSize2d.y >> 1)
            //      ////- params.objTileSize2d.y
            //    )
            //  ).asUInt
            //)(
            //  tempOutp.tilePxsCoord(x).y.bitsRange
            //)
            when (!tempInp.objAttrs.dispFlip.x) {
              tempOutp.tilePxsCoord(x).x := tempX.resized
            } otherwise {
              tempOutp.tilePxsCoord(x).x := (
                params.objTileSize2d.x - 1 - tempX
              ).resized
            }
            when (!tempInp.objAttrs.dispFlip.y) {
              tempOutp.tilePxsCoord(x).y := tempY.resized
            } otherwise {
              tempOutp.tilePxsCoord(x).y := (
                params.objTileSize2d.y - 1 - tempY
              ).resized
            }

          } else { // if (kind == 1)
            def shiftPlus = 1 //-1 //1 // 2
            def tileX = tempOutp.tilePxsCoord(x).x
            val fxTileX = (
              (
                tempInp.stage6.fxTilePxsCoord(x).x
                //+ (1 << (Gpu2dAffine.fracWidth - 1))
              ) >> (
                Gpu2dAffine.fracWidth
              )
            )
              .setName(f"dbgTestWrObjPipe5_fxTileX_$x")
            //val fxTileX1 = (
            //  (
            //    tempInp.stage5.fxTilePxsCoord(x).x
            //    //+ (1 << (Gpu2dAffine.fracWidth - 1))
            //  ) >> (
            //    Gpu2dAffine.fracWidth + shiftPlus
            //  )
            //)
            //  .setName(f"dbgTestWrObjPipe5_fxTileX1_$x")
            //(
            //  fxTileX.high
            //  downto tempInp.stage4.fracWidth + 1//2
            //)
            val tempX = (
              //x
              (
                fxTileX
                ////(
                ////  //tileX.high + 
                ////  //tileX.high + 1
                ////  //downto 1
                ////)
                ////fxTileX
                ////+ (params.objAffineTileSize2d.x / 2)
                //+ params.objAffineTileSize2d.x
              )
              //>> 1 //2
            )
              .setName(f"wrObjPipe5_tempX_$x")
            tileX := tempX.asUInt(tileX.bitsRange)
            
            //tempOutp.stage5.oorTilePxsCoord(x).x := (
            //  (
            //    //(
            //    //  tempInp.objAttrs.size2d.x(
            //    //    tempInp.objAttrs.size2d.x.high
            //    //    downto tempInp.objAttrs.size2d.x.high - 1
            //    //  ) =/= U"00"
            //    //) 
            //    (
            //      tempInp.objAttrs.size2d.x
            //      === params.objAffineTileSize2d.x
            //    ) && (
            //      tempX < 0
            //      || tempX >= params.objAffineTileSize2d.x
            //    )
            //  ) || (
            //    (
            //      tempInp.objAttrs.size2d.x
            //      < params.objAffineTileSize2d.x
            //    ) && !(
            //      (
            //        (tempX << 1) - params.objAffineTileSize2d.x 
            //        //((tempX - (params.objAffineTileSize2d.x >> 1)) << 1)
            //        < tempInp.objAttrs.size2d.x.asSInt
            //      ) && (
            //        (tempX << 1) - params.objAffineTileSize2d.x 
            //        //((tempX - (params.objAffineTileSize2d.x >> 1)) << 1)
            //        >= -tempInp.objAttrs.size2d.x.asSInt
            //      )
            //    )
            //  )

            //  //tempX(tempX.high downto tileX.high) =/= 0
            //  //fxTileX + (params.objAffineTileSize2d.x / 2) < 0
            //  ////< (-params.objAffineTileSize2d.x / 2)
            //  //|| (
            //  //  //fxTileX >= (params.objAffineTileSize2d.x / 2)
            //  //  fxTileX + (params.objAffineTileSize2d.x / 2)
            //  //  >= params.objAffineTileSize2d.x
            //  //)
            //)
            tempOutp.stage7.oorTilePxsCoord(x).x := (
              (
                //(
                //  tempInp.objAttrs.size2d.x(
                //    tempInp.objAttrs.size2d.x.high
                //    downto tempInp.objAttrs.size2d.x.high - 1
                //  ) =/= U"00"
                //)
                (
                  //tempInp.objAttrs.size2d.y
                  //=== params.objAffineTileSize2d.y
                  tempInp.objAttrs.size2d.x(
                    tempInp.objAttrs.size2d.x.high
                    downto tempInp.objAttrs.size2d.x.high - 1
                  ) =/= U"00"
                ) && (
                  tempX < 0
                  || tempX >= params.objAffineTileSize2d.x
                )
              ) || (
                (
                  //tempInp.objAttrs.size2d.x
                  //< params.objAffineTileSize2d.x
                  tempInp.objAttrs.size2d.x(
                    tempInp.objAttrs.size2d.x.high
                    downto tempInp.objAttrs.size2d.x.high - 1
                  ) === U"00"
                ) && !(
                  (
                    (tempX << 1) - params.objAffineTileSize2d.x 
                    //((tempX - (params.objAffineTileSize2d.x >> 1)) << 1)
                    < tempInp.objAttrs.size2d.x.asSInt
                  ) && (
                    (tempX << 1) - params.objAffineTileSize2d.x 
                    //((tempX - (params.objAffineTileSize2d.x >> 1)) << 1)
                    >= -tempInp.objAttrs.size2d.x.asSInt
                  )
                )
              )
              //tempX(tempX.high downto tileY.high) =/= 0
              ////tempX(tempX.high downto tileY.getWidth) =/= 0
              ////tempX(tempX.high downto tileY.getWidth) < 0
              ////|| tempX(tempX.high downto tileY.getWidth)
              ////  > params.objAffineTileSize2d.x - 1
              ////fxTileY < (-params.objAffineTileSize2d.x / 2)
              ////|| fxTileY >= (params.objAffineTileSize2d.x / 2)
              //fxTileY + (params.objAffineTileSize2d.x / 2) < 0
              ////< (-params.objAffineTileSize2d.x / 2)
              //|| (
              //  //fxTileY >= (params.objAffineTileSize2d.x / 2)
              //  fxTileY + (params.objAffineTileSize2d.x / 2)
              //  >= params.objAffineTileSize2d.x
              //)
            )

            def tileY = tempOutp.tilePxsCoord(x).y
            val fxTileY = (
              (
                tempInp.stage6.fxTilePxsCoord(x).y
                //+ (1 << (Gpu2dAffine.fracWidth - 1))
              ) >> (
                Gpu2dAffine.fracWidth
              )
            )
              .setName(f"dbgTestWrObjPipe5_fxTileY_$x")
            //val fxTileY1 = (
            //  //fxTileY1
            //  (
            //    tempInp.stage5.fxTilePxsCoord(x).y
            //    //+ (1 << (Gpu2dAffine.fracWidth - 1))
            //  ) >> (
            //    Gpu2dAffine.fracWidth + shiftPlus
            //  )
            //)
            //  .setName(f"dbgTestWrObjPipe5_fxTileY1_$x")
            val tempY = (
              ////x
              //fxTileY1
              ////fxTileY
              //+ (params.objAffineTileSize2d.y / 2)
              ////+ params.objAffineTileSize2d.y
              (
                fxTileY
                //+ (params.objAffineTileSize2d.y / 2)
                //+ params.objAffineTileSize2d.y
              )
              //>> 1 //2
            )
              .setName(f"wrObjPipe5_tempY_$x")
            //(
            //  fxTileY.high
            //  downto tempInp.stage4.fracWidth + 1//2
            //)
            tileY := tempY.asUInt(tileY.bitsRange)
            
            tempOutp.stage7.oorTilePxsCoord(x).y := (
              (
                //(
                //  tempInp.objAttrs.size2d.y(
                //    tempInp.objAttrs.size2d.y.high
                //    downto tempInp.objAttrs.size2d.y.high - 1
                //  ) =/= U"00"
                //)
                (
                  //tempInp.objAttrs.size2d.y
                  //=== params.objAffineTileSize2d.y
                  tempInp.objAttrs.size2d.y(
                    tempInp.objAttrs.size2d.y.high
                    downto tempInp.objAttrs.size2d.y.high - 1
                  ) =/= U"00"
                ) && (
                  tempY < 0
                  || tempY >= params.objAffineTileSize2d.y
                )
              ) || (
                (
                  tempInp.objAttrs.size2d.y(
                    tempInp.objAttrs.size2d.y.high
                    downto tempInp.objAttrs.size2d.y.high - 1
                  ) === U"00"
                  //tempInp.objAttrs.size2d.y
                  //< params.objAffineTileSize2d.y
                ) && !(
                  (
                    (tempY << 1) - params.objAffineTileSize2d.y 
                    //((tempY - (params.objAffineTileSize2d.y >> 1)) << 1)
                    < tempInp.objAttrs.size2d.y.asSInt
                  ) && (
                    (tempY << 1) - params.objAffineTileSize2d.y 
                    //((tempY - (params.objAffineTileSize2d.y >> 1)) << 1)
                    >= -tempInp.objAttrs.size2d.y.asSInt
                  )
                )
              )
              //tempY(tempY.high downto tileY.high) =/= 0
              ////tempY(tempY.high downto tileY.getWidth) =/= 0
              ////tempY(tempY.high downto tileY.getWidth) < 0
              ////|| tempY(tempY.high downto tileY.getWidth)
              ////  > params.objAffineTileSize2d.y - 1
              ////fxTileY < (-params.objAffineTileSize2d.y / 2)
              ////|| fxTileY >= (params.objAffineTileSize2d.y / 2)
              //fxTileY + (params.objAffineTileSize2d.y / 2) < 0
              ////< (-params.objAffineTileSize2d.y / 2)
              //|| (
              //  //fxTileY >= (params.objAffineTileSize2d.y / 2)
              //  fxTileY + (params.objAffineTileSize2d.y / 2)
              //  >= params.objAffineTileSize2d.y
              //)
            )
            tempOutp.stage7.affineDoIt(x) := (
              if (kind == 0) {
                True
              } else {
                tempInp.objAttrs.affine.doIt
              }
              //&& tempInp.stage0.affineActive
              //&& tempInp.stage0.affineActive
              //&& !tempOutp.stage5.oorTilePxsCoord(x).x
              //&& !tempOutp.stage5.oorTilePxsCoord(x).y
            )
            //tempOutp.tilePxsCoord(x).y := (
            //  tempInp.stage5.fxTilePxsCoord(x).y 
            //)(
            //  tempInp.stage5.fxTilePxsCoord(x).y.high
            //  downto tempInp.stage4.fracWidth + 2
            //)(
            //  tempOutp.tilePxsCoord(x).y.bitsRange
            //)
          }
        }

        //def tempPxsCoordSizeYPow = params.objPxsCoordSize2dPow.y
        //def tempMinusAmountY = myTempObjTileWidth.y
        ////tempOutp.pxPosShiftTopLeft.y := (
        ////  rWrLineNum.asSInt.resized
        ////  - S(f"$tempMinusSizeYPow'd$tempPlusAmountY")
        ////)
        tempOutp.objPosYShift := (
          //rWrLineNum.asSInt.resized
          //+ S(f"$tempPxsCoordSizeYPow'd$tempMinusAmountY")
          tempInp.objAttrs.pos.y 
          //- (params.objTileSize2d.y >> 1)
          ////- params.objTileSize2d.y
          + myTempObjTileHeight
        )
        //if (kind == 0) {
        //  tempOutp.objPosYShift := (
        //    //rWrLineNum.asSInt.resized
        //    //+ S(f"$tempPxsCoordSizeYPow'd$tempMinusAmountY")
        //    tempInp.objAttrs.pos.y 
        //    //- (params.objTileSize2d.y >> 1)
        //    ////- params.objTileSize2d.y
        //    + myTempObjTileHeight
        //  )
        //} else { // if (kind == 1)
        //  tempOutp.objPosYShift := (
        //    //rWrLineNum.asSInt.resized
        //    //+ S(f"$tempPxsCoordSizeYPow'd$tempMinusAmountY")
        //    tempInp.objAttrs.pos.y 
        //    //- (params.objTileSize2d.y >> 1)
        //    ////- params.objTileSize2d.y
        //    //- (myTempObjTileWidth.y >> 1)
        //    //- (params.objTileSize2d.y >> 1)
        //    + myTempObjTileHeight
        //  )
        //}
      
        //tempOutp.tile := objTileMem.readAsync(
        //  address=tempInp.objAttrs.tileMemIdx
        //)
        //tempOutp.tileSlice := objTileMemArr(kind).io.rdData
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(6)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      //    def myTempObjTileHeight = tempObjTileHeight(kind != 0)
      //    def myTempObjTileWidthPow = (
      //      tempObjTileWidthPow(kind != 0)
      //    )

      //    //tempOutp.tilePxsCoord.x := tempInp.getCntTilePxsCoordX()
      //    //tempOutp.tilePxsCoord.y := tempInp.objAttrs.pos
      //    //when (tempInp.pxPosInLine) {
      //    //  //tempOutp.tilePxsCoord.x 
      //    //} otherwise {
      //    //  tempOutp.tilePxsCoord := tempInp.tilePxsCoord
      //    //}
      //    if (kind == 0) {
      //      objTileMemArr(kind).io.rdEn := True
      //      objTileMemArr(kind).io.rdAddr := (
      //        if (
      //          (kind == 0 && params.objTileWidthRshift > 0)
      //          //|| (kind == 1 && params.objAffineTileWidthRshift > 0)
      //        ) {
      //          Cat(
      //            tempInp.objAttrs.tileIdx,
      //            tempOutp.tilePxsCoord(0).y,
      //            (
      //              tempInp.objXStart()(
      //                //tempInp.objXStart().high
      //                params.objTileSize2dPow.x - 1
      //                downto params.objSliceTileWidthPow
      //                //downto params.objTileWidthRshift
      //                //params.objTileWidthRshift - 1 downto 0
      //              )
      //            ),
      //          ).asUInt
      //        } else {
      //          Cat(
      //            tempInp.objAttrs.tileIdx,
      //            tempOutp.tilePxsCoord(0).y,
      //          ).asUInt
      //        }
      //      )
      //    } else { // if (kind == 1)
      //      for (x <- 0 until myTempObjTileWidth) {
      //        objAffineTileMemArr(x).io.rdEn := True
      //        //def tempX = (
      //        //  U{
      //        //    def tempWidthPow = (
      //        //      //params.objAffineTileWidthRshift
      //        //      //params.objAffineTileSize2dPow.x
      //        //      params.objAffineSliceTileWidthPow
      //        //    )
      //        //    f"$tempWidthPow'd$x"
      //        //  }
      //        //)
      //        val fxTileX = (
      //          (
      //            tempInp.stage5.fxTilePxsCoord(x).x
      //            //+ (1 << (Gpu2dAffine.fracWidth - 1))
      //          ) >> (
      //            Gpu2dAffine.fracWidth
      //          )
      //        )
      //        objAffineTileMemArr(x).io.rdAddr := (
      //          //if (
      //          //  //(kind == 0 && params.objTileWidthRshift > 0)
      //          //  //|| 
      //          //  (kind == 1 && params.objAffineTileWidthRshift > 0)
      //          //) {
      //          //  Cat(
      //          //    tempInp.objAttrs.tileIdx,
      //          //    tempOutp.tilePxsCoord(x).y,
      //          //    //(
      //          //    //  tempInp.affineObjXStart()(
      //          //    //    ////tempInp.affineObjXStart().high
      //          //    //    params.objAffineTileSize2dPow.x - 1
      //          //    //    downto params.objAffineSliceTileWidthPow
      //          //    //    //downto params.objAffineTileWidthRshift
      //          //    //    //params.objAffineTileWidthRshift - 1 downto 0
      //          //    //  )
      //          //    //),
      //          //    //tempX,
      //          //    tempOutp.tilePxsCoord(x).x,
      //          //  ).asUInt
      //          //} else {
      //            Cat(
      //              tempInp.objAttrs.tileIdx,
      //              tempOutp.tilePxsCoord(x).y,
      //              //tempX,
      //              //(
      //              //  tempInp.affineObjXStart()(
      //              //    params.objAffineTileSize2dPow.x - 1 downto 0
      //              //  )
      //              //  | tempOutp.tilePxsCoord(x).x.resized
      //              //),
      //              //(
      //              //  tempInp.affineObjXStart()(
      //              //    ////tempInp.affineObjXStart().high
      //              //    params.objAffineTileSize2dPow.x - 1
      //              //    downto params.objAffineSliceTileWidthPow
      //              //    //downto params.objAffineTileWidthRshift
      //              //    //params.objAffineTileWidthRshift - 1 downto 0
      //              //  )
      //              //),
      //              fxTileX(
      //                params.objAffineTileSize2dPow.x - 1 downto 0
      //              ),
      //            ).asUInt
      //          //}
      //        )
      //      }
      //    }
      //    for (x <- 0 until myTempObjTileWidth) {
      //      if (kind == 0) {
      //        val tempX = (
      //          x
      //          + tempInp.objXStart()
      //        )
      //        val tempY = (
      //          //rWrLineNum - tempInp.objAttrs.pos.y.asUInt
      //          (
      //            tempInp.lineNum.asSInt.resized
      //            - (
      //              tempInp.objAttrs.pos.y
      //              //- (params.objTileSize2d.y >> 1)
      //              ////- params.objTileSize2d.y
      //            )
      //          ).asUInt
      //        )(
      //          tempOutp.tilePxsCoord(x).y.bitsRange
      //        )
      //        //tempOutp.tilePxsCoord(x).x := (
      //        //  x
      //        //  + tempInp.objXStart()
      //        //).resized
      //        //tempOutp.tilePxsCoord(x).y := (
      //        //  //rWrLineNum - tempInp.objAttrs.pos.y.asUInt
      //        //  (
      //        //    tempInp.lineNum.asSInt.resized
      //        //    - (
      //        //      tempInp.objAttrs.pos.y
      //        //      //- (params.objTileSize2d.y >> 1)
      //        //      ////- params.objTileSize2d.y
      //        //    )
      //        //  ).asUInt
      //        //)(
      //        //  tempOutp.tilePxsCoord(x).y.bitsRange
      //        //)
      //        when (!tempInp.objAttrs.dispFlip.x) {
      //          tempOutp.tilePxsCoord(x).x := tempX.resized
      //        } otherwise {
      //          tempOutp.tilePxsCoord(x).x := (
      //            params.objTileSize2d.x - 1 - tempX
      //          ).resized
      //        }
      //        when (!tempInp.objAttrs.dispFlip.y) {
      //          tempOutp.tilePxsCoord(x).y := tempY.resized
      //        } otherwise {
      //          tempOutp.tilePxsCoord(x).y := (
      //            params.objTileSize2d.y - 1 - tempY
      //          ).resized
      //        }

      //      } else { // if (kind == 1)
      //        def shiftPlus = 1 //-1 //1 // 2
      //        def tileX = tempOutp.tilePxsCoord(x).x
      //        val fxTileX = (
      //          (
      //            tempInp.stage5.fxTilePxsCoord(x).x
      //            //+ (1 << (Gpu2dAffine.fracWidth - 1))
      //          ) >> (
      //            Gpu2dAffine.fracWidth
      //          )
      //        )
      //          .setName(f"dbgTestWrObjPipe5_fxTileX_$x")
      //        //val fxTileX1 = (
      //        //  (
      //        //    tempInp.stage5.fxTilePxsCoord(x).x
      //        //    //+ (1 << (Gpu2dAffine.fracWidth - 1))
      //        //  ) >> (
      //        //    Gpu2dAffine.fracWidth + shiftPlus
      //        //  )
      //        //)
      //        //  .setName(f"dbgTestWrObjPipe5_fxTileX1_$x")
      //        //(
      //        //  fxTileX.high
      //        //  downto tempInp.stage4.fracWidth + 1//2
      //        //)
      //        val tempX = (
      //          //x
      //          (
      //            fxTileX
      //            ////(
      //            ////  //tileX.high + 
      //            ////  //tileX.high + 1
      //            ////  //downto 1
      //            ////)
      //            ////fxTileX
      //            ////+ (params.objAffineTileSize2d.x / 2)
      //            //+ params.objAffineTileSize2d.x
      //          )
      //          //>> 1 //2
      //        )
      //          .setName(f"wrObjPipe5_tempX_$x")
      //        tileX := tempX.asUInt(tileX.bitsRange)
      //        
      //        //tempOutp.stage5.oorTilePxsCoord(x).x := (
      //        //  (
      //        //    //(
      //        //    //  tempInp.objAttrs.size2d.x(
      //        //    //    tempInp.objAttrs.size2d.x.high
      //        //    //    downto tempInp.objAttrs.size2d.x.high - 1
      //        //    //  ) =/= U"00"
      //        //    //) 
      //        //    (
      //        //      tempInp.objAttrs.size2d.x
      //        //      === params.objAffineTileSize2d.x
      //        //    ) && (
      //        //      tempX < 0
      //        //      || tempX >= params.objAffineTileSize2d.x
      //        //    )
      //        //  ) || (
      //        //    (
      //        //      tempInp.objAttrs.size2d.x
      //        //      < params.objAffineTileSize2d.x
      //        //    ) && !(
      //        //      (
      //        //        (tempX << 1) - params.objAffineTileSize2d.x 
      //        //        //((tempX - (params.objAffineTileSize2d.x >> 1)) << 1)
      //        //        < tempInp.objAttrs.size2d.x.asSInt
      //        //      ) && (
      //        //        (tempX << 1) - params.objAffineTileSize2d.x 
      //        //        //((tempX - (params.objAffineTileSize2d.x >> 1)) << 1)
      //        //        >= -tempInp.objAttrs.size2d.x.asSInt
      //        //      )
      //        //    )
      //        //  )

      //        //  //tempX(tempX.high downto tileX.high) =/= 0
      //        //  //fxTileX + (params.objAffineTileSize2d.x / 2) < 0
      //        //  ////< (-params.objAffineTileSize2d.x / 2)
      //        //  //|| (
      //        //  //  //fxTileX >= (params.objAffineTileSize2d.x / 2)
      //        //  //  fxTileX + (params.objAffineTileSize2d.x / 2)
      //        //  //  >= params.objAffineTileSize2d.x
      //        //  //)
      //        //)
      //        tempOutp.stage5.oorTilePxsCoord(x).x := (
      //          (
      //            //(
      //            //  tempInp.objAttrs.size2d.x(
      //            //    tempInp.objAttrs.size2d.x.high
      //            //    downto tempInp.objAttrs.size2d.x.high - 1
      //            //  ) =/= U"00"
      //            //)
      //            (
      //              //tempInp.objAttrs.size2d.y
      //              //=== params.objAffineTileSize2d.y
      //              tempInp.objAttrs.size2d.x(
      //                tempInp.objAttrs.size2d.x.high
      //                downto tempInp.objAttrs.size2d.x.high - 1
      //              ) =/= U"00"
      //            ) && (
      //              tempX < 0
      //              || tempX >= params.objAffineTileSize2d.x
      //            )
      //          ) || (
      //            (
      //              //tempInp.objAttrs.size2d.x
      //              //< params.objAffineTileSize2d.x
      //              tempInp.objAttrs.size2d.x(
      //                tempInp.objAttrs.size2d.x.high
      //                downto tempInp.objAttrs.size2d.x.high - 1
      //              ) === U"00"
      //            ) && !(
      //              (
      //                (tempX << 1) - params.objAffineTileSize2d.x 
      //                //((tempX - (params.objAffineTileSize2d.x >> 1)) << 1)
      //                < tempInp.objAttrs.size2d.x.asSInt
      //              ) && (
      //                (tempX << 1) - params.objAffineTileSize2d.x 
      //                //((tempX - (params.objAffineTileSize2d.x >> 1)) << 1)
      //                >= -tempInp.objAttrs.size2d.x.asSInt
      //              )
      //            )
      //          )
      //          //tempX(tempX.high downto tileY.high) =/= 0
      //          ////tempX(tempX.high downto tileY.getWidth) =/= 0
      //          ////tempX(tempX.high downto tileY.getWidth) < 0
      //          ////|| tempX(tempX.high downto tileY.getWidth)
      //          ////  > params.objAffineTileSize2d.x - 1
      //          ////fxTileY < (-params.objAffineTileSize2d.x / 2)
      //          ////|| fxTileY >= (params.objAffineTileSize2d.x / 2)
      //          //fxTileY + (params.objAffineTileSize2d.x / 2) < 0
      //          ////< (-params.objAffineTileSize2d.x / 2)
      //          //|| (
      //          //  //fxTileY >= (params.objAffineTileSize2d.x / 2)
      //          //  fxTileY + (params.objAffineTileSize2d.x / 2)
      //          //  >= params.objAffineTileSize2d.x
      //          //)
      //        )

      //        def tileY = tempOutp.tilePxsCoord(x).y
      //        val fxTileY = (
      //          (
      //            tempInp.stage5.fxTilePxsCoord(x).y
      //            //+ (1 << (Gpu2dAffine.fracWidth - 1))
      //          ) >> (
      //            Gpu2dAffine.fracWidth
      //          )
      //        )
      //          .setName(f"dbgTestWrObjPipe5_fxTileY_$x")
      //        //val fxTileY1 = (
      //        //  //fxTileY1
      //        //  (
      //        //    tempInp.stage5.fxTilePxsCoord(x).y
      //        //    //+ (1 << (Gpu2dAffine.fracWidth - 1))
      //        //  ) >> (
      //        //    Gpu2dAffine.fracWidth + shiftPlus
      //        //  )
      //        //)
      //        //  .setName(f"dbgTestWrObjPipe5_fxTileY1_$x")
      //        val tempY = (
      //          ////x
      //          //fxTileY1
      //          ////fxTileY
      //          //+ (params.objAffineTileSize2d.y / 2)
      //          ////+ params.objAffineTileSize2d.y
      //          (
      //            fxTileY
      //            //+ (params.objAffineTileSize2d.y / 2)
      //            //+ params.objAffineTileSize2d.y
      //          )
      //          //>> 1 //2
      //        )
      //          .setName(f"wrObjPipe5_tempY_$x")
      //        //(
      //        //  fxTileY.high
      //        //  downto tempInp.stage4.fracWidth + 1//2
      //        //)
      //        tileY := tempY.asUInt(tileY.bitsRange)
      //        
      //        tempOutp.stage5.oorTilePxsCoord(x).y := (
      //          (
      //            //(
      //            //  tempInp.objAttrs.size2d.y(
      //            //    tempInp.objAttrs.size2d.y.high
      //            //    downto tempInp.objAttrs.size2d.y.high - 1
      //            //  ) =/= U"00"
      //            //)
      //            (
      //              //tempInp.objAttrs.size2d.y
      //              //=== params.objAffineTileSize2d.y
      //              tempInp.objAttrs.size2d.y(
      //                tempInp.objAttrs.size2d.y.high
      //                downto tempInp.objAttrs.size2d.y.high - 1
      //              ) =/= U"00"
      //            ) && (
      //              tempY < 0
      //              || tempY >= params.objAffineTileSize2d.y
      //            )
      //          ) || (
      //            (
      //              tempInp.objAttrs.size2d.y(
      //                tempInp.objAttrs.size2d.y.high
      //                downto tempInp.objAttrs.size2d.y.high - 1
      //              ) === U"00"
      //              //tempInp.objAttrs.size2d.y
      //              //< params.objAffineTileSize2d.y
      //            ) && !(
      //              (
      //                (tempY << 1) - params.objAffineTileSize2d.y 
      //                //((tempY - (params.objAffineTileSize2d.y >> 1)) << 1)
      //                < tempInp.objAttrs.size2d.y.asSInt
      //              ) && (
      //                (tempY << 1) - params.objAffineTileSize2d.y 
      //                //((tempY - (params.objAffineTileSize2d.y >> 1)) << 1)
      //                >= -tempInp.objAttrs.size2d.y.asSInt
      //              )
      //            )
      //          )
      //          //tempY(tempY.high downto tileY.high) =/= 0
      //          ////tempY(tempY.high downto tileY.getWidth) =/= 0
      //          ////tempY(tempY.high downto tileY.getWidth) < 0
      //          ////|| tempY(tempY.high downto tileY.getWidth)
      //          ////  > params.objAffineTileSize2d.y - 1
      //          ////fxTileY < (-params.objAffineTileSize2d.y / 2)
      //          ////|| fxTileY >= (params.objAffineTileSize2d.y / 2)
      //          //fxTileY + (params.objAffineTileSize2d.y / 2) < 0
      //          ////< (-params.objAffineTileSize2d.y / 2)
      //          //|| (
      //          //  //fxTileY >= (params.objAffineTileSize2d.y / 2)
      //          //  fxTileY + (params.objAffineTileSize2d.y / 2)
      //          //  >= params.objAffineTileSize2d.y
      //          //)
      //        )
      //        tempOutp.stage5.affineDoIt(x) := (
      //          if (kind == 0) {
      //            True
      //          } else {
      //            tempInp.objAttrs.affine.doIt
      //          }
      //          //&& tempInp.stage0.affineActive
      //          //&& tempInp.stage0.affineActive
      //          //&& !tempOutp.stage5.oorTilePxsCoord(x).x
      //          //&& !tempOutp.stage5.oorTilePxsCoord(x).y
      //        )
      //        //tempOutp.tilePxsCoord(x).y := (
      //        //  tempInp.stage5.fxTilePxsCoord(x).y 
      //        //)(
      //        //  tempInp.stage5.fxTilePxsCoord(x).y.high
      //        //  downto tempInp.stage4.fracWidth + 2
      //        //)(
      //        //  tempOutp.tilePxsCoord(x).y.bitsRange
      //        //)
      //      }
      //    }

      //    //def tempPxsCoordSizeYPow = params.objPxsCoordSize2dPow.y
      //    //def tempMinusAmountY = myTempObjTileWidth.y
      //    ////tempOutp.pxPosShiftTopLeft.y := (
      //    ////  rWrLineNum.asSInt.resized
      //    ////  - S(f"$tempMinusSizeYPow'd$tempPlusAmountY")
      //    ////)
      //    tempOutp.objPosYShift := (
      //      //rWrLineNum.asSInt.resized
      //      //+ S(f"$tempPxsCoordSizeYPow'd$tempMinusAmountY")
      //      tempInp.objAttrs.pos.y 
      //      //- (params.objTileSize2d.y >> 1)
      //      ////- params.objTileSize2d.y
      //      + myTempObjTileHeight
      //    )
      //    //if (kind == 0) {
      //    //  tempOutp.objPosYShift := (
      //    //    //rWrLineNum.asSInt.resized
      //    //    //+ S(f"$tempPxsCoordSizeYPow'd$tempMinusAmountY")
      //    //    tempInp.objAttrs.pos.y 
      //    //    //- (params.objTileSize2d.y >> 1)
      //    //    ////- params.objTileSize2d.y
      //    //    + myTempObjTileHeight
      //    //  )
      //    //} else { // if (kind == 1)
      //    //  tempOutp.objPosYShift := (
      //    //    //rWrLineNum.asSInt.resized
      //    //    //+ S(f"$tempPxsCoordSizeYPow'd$tempMinusAmountY")
      //    //    tempInp.objAttrs.pos.y 
      //    //    //- (params.objTileSize2d.y >> 1)
      //    //    ////- params.objTileSize2d.y
      //    //    //- (myTempObjTileWidth.y >> 1)
      //    //    //- (params.objTileSize2d.y >> 1)
      //    //    + myTempObjTileHeight
      //    //  )
      //    //}
      //  
      //    //tempOutp.tile := objTileMem.readAsync(
      //    //  address=tempInp.objAttrs.tileMemIdx
      //    //)
      //    //tempOutp.tileSlice := objTileMemArr(kind).io.rdData
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).stage5 := stageData.pipeIn(idx).stage5
      //    //stageData.pipeOut(idx).tilePxsCoordYPipe1 := (
      //    //  stageData.pipeIn(idx).tilePxsCoordYPipe1
      //    //)
      //    def pipeIn = stageData.pipeIn(idx)
      //    def pipeOut = stageData.pipeOut(idx)
      //    pipeOut.stage5 := pipeIn.stage5
      //  },
      //)
      // END: Stage 7

      // BEGIN: Stage 8
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 8
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = (
          tempObjTileWidthPow(kind != 0)
        )
        if (kind == 0) {
          tempOutp.tileSlice := (
            RegNext(tempOutp.tileSlice) init(tempOutp.tileSlice.getZero)
          )
          when (cWrObjArr(idx).up.isFiring) {
            tempOutp.tileSlice := objTileMemArr(kind).io.rdData
          }
        } else {
          for (kdx <- 0 until myTempObjTileWidth) {
            tempOutp.tilePx(kdx) := objAffineTileMemArr(kdx).io.rdData
          }
        }

        //tempOutp.oldPxPosInLineCheckGePipe1.x := (
        //  tempInp.objAttrs.pos.x + myTempObjTileWidth - 1 >= 0
        //)
        //tempOutp.oldPxPosInLineCheckGePipe1.y := (
        //  outp.bgPxsPosSlice.pos.y.asSInt.resized
        //  >= tempInp.objAttrs.pos.y
        //)
        //tempOutp.tilePxs
        //tempOutp.tilePxsCoord.y := tempInp.tilePxsCoordYPipe1
        //tempOutp.pxPosRangeCheck.y := 
        //val dbgTestWrObjPipe3_pxPosXGridIdx = (
        //  Vec.fill(myTempObjTileWidth)(
        //    UInt(
        //      (tempInp.pxPos(0).x.getWidth - myTempObjTileWidthPow)
        //      bits
        //    )
        //  )
        //)

        //tempOutp.pxPosXGridIdxFindFirstSameAs :=
        val dbgPxPosXGridIdxFindFirstSameAs: (Bool, UInt) = (
          tempOutp.pxPosXGridIdx.sFindFirst(
            //condition=(
              //myBool => //(
                _(0) === tempInp.stage0.calcGridIdxLsb(kind)
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
        tempOutp.stage8.pxPosXGridIdxFindFirstSameAsFound := (
          dbgPxPosXGridIdxFindFirstSameAs._1
        )
        tempOutp.stage8.pxPosXGridIdxFindFirstSameAsIdx := (
          dbgPxPosXGridIdxFindFirstSameAs._2.resized
        )
        val dbgPxPosXGridIdxFindFirstDiff: (Bool, UInt) = (
          tempOutp.pxPosXGridIdx.sFindFirst(
            //condition=(
              //myBool => //(
                _(0) =/= tempInp.stage0.calcGridIdxLsb(kind)
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
        tempOutp.stage8.pxPosXGridIdxFindFirstDiffFound := (
          dbgPxPosXGridIdxFindFirstDiff._1
        )
        tempOutp.stage8.pxPosXGridIdxFindFirstDiffIdx := (
          dbgPxPosXGridIdxFindFirstDiff._2.resized
        )
        for (x <- 0 until myTempObjTileWidth) {
          tempOutp.pxPosXGridIdx(x) := (
            tempInp.pxPos(x).x.asUInt(
              tempInp.pxPos(x).x.asUInt.high
              //downto params.objAffineDblTileSize2dPow.x
              //downto tempObjTileWidthPow2(kind != 0)
              downto tempObjTileWidthPow(kind != 0)
            )
          )
          val dbgTestWrObjPipe6_pxPosXGridIdx = UInt(
            tempOutp.pxPosXGridIdx(x).getWidth bits
          )
            .setName(
              f"dbgTestWrObjPipe6_pxPosXGridIdx_$kind" + f"_$x"
            )
          dbgTestWrObjPipe6_pxPosXGridIdx := (
            tempOutp.pxPosXGridIdx(x)
          )

          tempOutp.pxPosXGridIdxLsb(x) := (
            //tempOutp.pxPosXGridIdx(x)(0 downto 0)
            tempOutp.pxPosXGridIdx(x)(0)
          )
          tempOutp.pxPosRangeCheckGePipe1(x).x := (
            //tempInp.objAttrs.pos.x + myTempObjTileWidth - 1 >= 0
            tempInp.pxPos(x).x >= 0
          )
          tempOutp.pxPosRangeCheckGePipe1(x).y := (
            ////outp.bgPxsPosSlice.pos.y.asSInt.resized
            ////>= tempInp.objAttrs.pos.y
            //tempInp.pxPos.y >= 0
            ////tempInp.pxPos.y >= tempInp.objAttrs.pos.y
            tempInp.pxPos(x).y
            >= (
              tempInp.objAttrs.pos.y
              //+ (
              //  if (kind == 0) {
              //    0
              //  } else {
              //    -(params.objTileSize2d.y >> 1)
              //    //-params.objTileSize2d.y
              //  }
              //)
            )
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
          if (kind == 0) {
            tempOutp.stage8.tilePxsCoordPalEntryCmpPipe1(x).x := (
              tempInp.tilePxsCoord(x).x + tempInp.objXStart()
                < tempInp.objAttrs.size2d.x
            )
            tempOutp.stage8.tilePxsCoordPalEntryCmpPipe1(x).y := (
              tempInp.tilePxsCoord(x).y
                < tempInp.objAttrs.size2d.y
            )
          }
          //when (
          //  //tempInp.tilePxsCoord(x).x
          //  //  < tempInp.objAttrs.size2d.x
          //  //&& tempInp.tilePxsCoord(x).y
          //  //  < tempInp.objAttrs.size2d.y
          //  //&& (
          //  //  if (kind == 0) {
          //  //    True //!tempInp.objAttrs.affine.doIt
          //  //  } else { // if (kind == 1)
          //  //    (
          //  //      //tempInp.objAttrs.affine.doIt
          //  //      ////&& tempInp.stage0.affineActive
          //  //      //&& tempInp.stage0.affineActive
          //  //      //&& !tempInp.stage5.oorTilePxsCoord(x).x
          //  //      //&& !tempInp.stage5.oorTilePxsCoord(x).y
          //  //      tempInp.stage5.affineDoIt(x)
          //  //    )
          //  //  }
          //  //)
          //  if (kind == 0) {
          //    (
          //      tempInp.tilePxsCoord(x).x + tempInp.objXStart()
          //        < tempInp.objAttrs.size2d.x
          //      && tempInp.tilePxsCoord(x).y
          //        < tempInp.objAttrs.size2d.y
          //    )
          //  } else {
          //    (
          //      //--------
          //      //tempInp.tilePxsCoord(x).x
          //      //  + (params.objAffineTileSize2d.x / 2)
          //      //  < tempInp.objAttrs.size2d.x
          //      //&& tempInp.tilePxsCoord(x).x.asSInt.resized
          //      //  - (params.objAffineTileSize2d.x / 2)
          //      //  >= 0
          //      //&& tempInp.tilePxsCoord(x).y
          //      //  + (params.objAffineTileSize2d.y / 2)
          //      //  < tempInp.objAttrs.size2d.y
          //      //--------
          //      // do some fixed point math (with one fractional bit)
          //      // this centers the coordinates
          //      //(
          //      //  (tempInp.tilePxsCoord(x).x << 1)
          //      //    - params.objAffineTileSize2d.x 
          //      //    < tempInp.objAttrs.size2d.x
          //      //) && (
          //      //  (tempInp.tilePxsCoord(x).x << 1).asSInt
          //      //    - params.objAffineTileSize2d.x
          //      //    >= -tempInp.objAttrs.size2d.x.asSInt
          //      //) && (
          //      //  (tempInp.tilePxsCoord(x).y << 1).asSInt
          //      //    - params.objAffineTileSize2d.y 
          //      //    < tempInp.objAttrs.size2d.y.asSInt
          //      //) && (
          //      //  (tempInp.tilePxsCoord(x).y << 1).asSInt
          //      //    + params.objAffineTileSize2d.y 
          //      //    >= -tempInp.objAttrs.size2d.y.asSInt
          //      //) && (
          //        //tempInp.stage5.affineDoIt(x)
          //        //&& tempInp.stage5.oorTilePxsCoord(x).x
          //        //&& tempInp.stage5.oorTilePxsCoord(x).y
          //        tempOutp.stage8.affineDoIt(x)
          //      //)
          //    )
          //  }
          //) {
          //  tempOutp.palEntryMemIdx(x) := (
          //    if (kind == 0) {
          //      tempOutp.tileSlice.getPx(
          //        tempInp.tilePxsCoord(x).x
          //        //x
          //      )
          //    } else {
          //      tempOutp.tilePx(x)
          //    }
          //  )
          //} otherwise {
          //  tempOutp.palEntryMemIdx(x) := 0
          //}
          if (kind == 1) {
            tempOutp.stage8.affineDoIt(x) := (
              tempInp.stage7.affineDoIt(x)
              && !tempInp.stage7.oorTilePxsCoord(x).x
              && !tempInp.stage7.oorTilePxsCoord(x).y
            )
          }
        }
      }
      {
        def idx = 9
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = (
          tempObjTileWidthPow(kind != 0)
        )
        for (x <- 0 until myTempObjTileWidth) {
          when (
            if (kind == 0) {
              (
                //tempInp.tilePxsCoord(x).x + tempInp.objXStart()
                //  < tempInp.objAttrs.size2d.x
                //&& tempInp.tilePxsCoord(x).y
                //  < tempInp.objAttrs.size2d.y
                tempInp.stage8.tilePxsCoordPalEntryCmpPipe1(x).x
                && tempInp.stage8.tilePxsCoordPalEntryCmpPipe1(x).y
              )
            } else {
              tempInp.stage8.affineDoIt(x)
            }
          ) {
            tempOutp.palEntryMemIdx(x) := (
              if (kind == 0) {
                tempInp.tileSlice.getPx(
                  tempInp.tilePxsCoord(x).x
                  //x
                )
              } else {
                tempInp.tilePx(x)
              }
            )
          } otherwise {
            tempOutp.palEntryMemIdx(x) := 0
          }
        }
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(7)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      //    def myTempObjTileWidthPow = (
      //      tempObjTileWidthPow(kind != 0)
      //    )
      //    if (kind == 0) {
      //      tempOutp.tileSlice := objTileMemArr(kind).io.rdData
      //    } else {
      //      for (kdx <- 0 until myTempObjTileWidth) {
      //        tempOutp.tilePx(kdx) := objAffineTileMemArr(kdx).io.rdData
      //      }
      //    }

      //    //tempOutp.oldPxPosInLineCheckGePipe1.x := (
      //    //  tempInp.objAttrs.pos.x + myTempObjTileWidth - 1 >= 0
      //    //)
      //    //tempOutp.oldPxPosInLineCheckGePipe1.y := (
      //    //  outp.bgPxsPosSlice.pos.y.asSInt.resized
      //    //  >= tempInp.objAttrs.pos.y
      //    //)
      //    //tempOutp.tilePxs
      //    //tempOutp.tilePxsCoord.y := tempInp.tilePxsCoordYPipe1
      //    //tempOutp.pxPosRangeCheck.y := 
      //    //val dbgTestWrObjPipe3_pxPosXGridIdx = (
      //    //  Vec.fill(myTempObjTileWidth)(
      //    //    UInt(
      //    //      (tempInp.pxPos(0).x.getWidth - myTempObjTileWidthPow)
      //    //      bits
      //    //    )
      //    //  )
      //    //)

      //    //tempOutp.pxPosXGridIdxFindFirstSameAs :=
      //    val dbgPxPosXGridIdxFindFirstSameAs: (Bool, UInt) = (
      //      tempOutp.pxPosXGridIdx.sFindFirst(
      //        //condition=(
      //          //myBool => //(
      //            _(0) === tempInp.stage0.calcGridIdxLsb(kind)
      //            //(
      //            //  //Mux[UInt](
      //            //  //  tempInp.stage0.gridIdxLsb === 1,
      //            //  //  U("1'd1"),
      //            //  //  U("1'd0"),
      //            //  //)
      //            //)
      //          //)
      //        //)
      //      )
      //    )
      //    tempOutp.stage6.pxPosXGridIdxFindFirstSameAsFound := (
      //      dbgPxPosXGridIdxFindFirstSameAs._1
      //    )
      //    tempOutp.stage6.pxPosXGridIdxFindFirstSameAsIdx := (
      //      dbgPxPosXGridIdxFindFirstSameAs._2.resized
      //    )
      //    val dbgPxPosXGridIdxFindFirstDiff: (Bool, UInt) = (
      //      tempOutp.pxPosXGridIdx.sFindFirst(
      //        //condition=(
      //          //myBool => //(
      //            _(0) =/= tempInp.stage0.calcGridIdxLsb(kind)
      //            //(
      //            //  //Mux[UInt](
      //            //  //  tempInp.stage0.gridIdxLsb === 1,
      //            //  //  U("1'd1"),
      //            //  //  U("1'd0"),
      //            //  //)
      //            //)
      //          //)
      //        //)
      //      )
      //    )
      //    tempOutp.stage6.pxPosXGridIdxFindFirstDiffFound := (
      //      dbgPxPosXGridIdxFindFirstDiff._1
      //    )
      //    tempOutp.stage6.pxPosXGridIdxFindFirstDiffIdx := (
      //      dbgPxPosXGridIdxFindFirstDiff._2.resized
      //    )
      //    for (x <- 0 until myTempObjTileWidth) {
      //      tempOutp.pxPosXGridIdx(x) := (
      //        tempInp.pxPos(x).x.asUInt(
      //          tempInp.pxPos(x).x.asUInt.high
      //          //downto params.objAffineDblTileSize2dPow.x
      //          //downto tempObjTileWidthPow2(kind != 0)
      //          downto tempObjTileWidthPow(kind != 0)
      //        )
      //      )
      //      val dbgTestWrObjPipe6_pxPosXGridIdx = UInt(
      //        tempOutp.pxPosXGridIdx(x).getWidth bits
      //      )
      //        .setName(
      //          f"dbgTestWrObjPipe6_pxPosXGridIdx_$kind" + f"_$x"
      //        )
      //      dbgTestWrObjPipe6_pxPosXGridIdx := (
      //        tempOutp.pxPosXGridIdx(x)
      //      )

      //      tempOutp.pxPosXGridIdxLsb(x) := (
      //        //tempOutp.pxPosXGridIdx(x)(0 downto 0)
      //        tempOutp.pxPosXGridIdx(x)(0)
      //      )
      //      tempOutp.pxPosRangeCheckGePipe1(x).x := (
      //        //tempInp.objAttrs.pos.x + myTempObjTileWidth - 1 >= 0
      //        tempInp.pxPos(x).x >= 0
      //      )
      //      tempOutp.pxPosRangeCheckGePipe1(x).y := (
      //        ////outp.bgPxsPosSlice.pos.y.asSInt.resized
      //        ////>= tempInp.objAttrs.pos.y
      //        //tempInp.pxPos.y >= 0
      //        ////tempInp.pxPos.y >= tempInp.objAttrs.pos.y
      //        tempInp.pxPos(x).y
      //        >= (
      //          tempInp.objAttrs.pos.y
      //          //+ (
      //          //  if (kind == 0) {
      //          //    0
      //          //  } else {
      //          //    -(params.objTileSize2d.y >> 1)
      //          //    //-params.objTileSize2d.y
      //          //  }
      //          //)
      //        )
      //      )

      //      tempOutp.pxPosRangeCheckLtPipe1(x).x := (
      //        tempInp.pxPos(x).x < params.intnlFbSize2d.x
      //      )
      //      tempOutp.pxPosRangeCheckLtPipe1(x).y := (
      //        ////tempInp.pxPos.y >= 0
      //        //tempInp.pxPos.y < params.intnlFbSize2d.y
      //        ////tempInp.pxPosMinusTileSize2d
      //        ////< params.intnlFbSize2d.y.resized
      //        //tempInp.pxPos.y <= 

      //        //tempInp.objPosYShift < params.intnlFbSize2d.y
      //        tempInp.pxPos(x).y < tempInp.objPosYShift
      //      )
      //      when (
      //        //tempInp.tilePxsCoord(x).x
      //        //  < tempInp.objAttrs.size2d.x
      //        //&& tempInp.tilePxsCoord(x).y
      //        //  < tempInp.objAttrs.size2d.y
      //        //&& (
      //        //  if (kind == 0) {
      //        //    True //!tempInp.objAttrs.affine.doIt
      //        //  } else { // if (kind == 1)
      //        //    (
      //        //      //tempInp.objAttrs.affine.doIt
      //        //      ////&& tempInp.stage0.affineActive
      //        //      //&& tempInp.stage0.affineActive
      //        //      //&& !tempInp.stage5.oorTilePxsCoord(x).x
      //        //      //&& !tempInp.stage5.oorTilePxsCoord(x).y
      //        //      tempInp.stage5.affineDoIt(x)
      //        //    )
      //        //  }
      //        //)
      //        if (kind == 0) {
      //          (
      //            tempInp.tilePxsCoord(x).x
      //              < tempInp.objAttrs.size2d.x
      //            && tempInp.tilePxsCoord(x).y
      //              < tempInp.objAttrs.size2d.y
      //          )
      //        } else {
      //          (
      //            //--------
      //            //tempInp.tilePxsCoord(x).x
      //            //  + (params.objAffineTileSize2d.x / 2)
      //            //  < tempInp.objAttrs.size2d.x
      //            //&& tempInp.tilePxsCoord(x).x.asSInt.resized
      //            //  - (params.objAffineTileSize2d.x / 2)
      //            //  >= 0
      //            //&& tempInp.tilePxsCoord(x).y
      //            //  + (params.objAffineTileSize2d.y / 2)
      //            //  < tempInp.objAttrs.size2d.y
      //            //--------
      //            // do some fixed point math (with one fractional bit)
      //            // this centers the coordinates
      //            //(
      //            //  (tempInp.tilePxsCoord(x).x << 1)
      //            //    - params.objAffineTileSize2d.x 
      //            //    < tempInp.objAttrs.size2d.x
      //            //) && (
      //            //  (tempInp.tilePxsCoord(x).x << 1).asSInt
      //            //    - params.objAffineTileSize2d.x
      //            //    >= -tempInp.objAttrs.size2d.x.asSInt
      //            //) && (
      //            //  (tempInp.tilePxsCoord(x).y << 1).asSInt
      //            //    - params.objAffineTileSize2d.y 
      //            //    < tempInp.objAttrs.size2d.y.asSInt
      //            //) && (
      //            //  (tempInp.tilePxsCoord(x).y << 1).asSInt
      //            //    + params.objAffineTileSize2d.y 
      //            //    >= -tempInp.objAttrs.size2d.y.asSInt
      //            //) && (
      //              //tempInp.stage5.affineDoIt(x)
      //              //&& tempInp.stage5.oorTilePxsCoord(x).x
      //              //&& tempInp.stage5.oorTilePxsCoord(x).y
      //              tempOutp.stage6.affineDoIt(x)
      //            //)
      //          )
      //        }
      //      ) {
      //        tempOutp.palEntryMemIdx(x) := (
      //          if (kind == 0) {
      //            tempOutp.tileSlice.getPx(
      //              tempInp.tilePxsCoord(x).x
      //              //x
      //            )
      //          } else {
      //            tempOutp.tilePx(x)
      //          }
      //        )
      //      } otherwise {
      //        tempOutp.palEntryMemIdx(x) := 0
      //      }
      //      if (kind == 1) {
      //        tempOutp.stage6.affineDoIt(x) := (
      //          tempInp.stage5.affineDoIt(x)
      //          && !tempInp.stage5.oorTilePxsCoord(x).x
      //          && !tempInp.stage5.oorTilePxsCoord(x).y
      //        )
      //      }
      //    }
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).stage6 := stageData.pipeIn(idx).stage6
      //    def pipeIn = stageData.pipeIn(idx)
      //    def pipeOut = stageData.pipeOut(idx)
      //    pipeOut.stage6 := pipeIn.stage6
      //  },
      //)
      // BEGIN: Stage 10
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 10
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)

        for (x <- 0 until myTempObjTileWidth) {
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
              tempInp.stage0.calcGridIdxLsb(kind)
            )
          )
          tempOutp.palEntryNzMemIdx(x) := (
            tempInp.palEntryMemIdx(x) =/= 0
          )

          tempOutp.pxPosRangeCheck(x).x := (
            //(tempInp.objAttrs.pos.x + myTempObjTileWidth - 1 >= 0)
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
            //  < tempInp.objAttrs.pos.y + myTempObjTileWidth.y
            //)
            tempInp.pxPosRangeCheckGePipe1(x).y
            && tempInp.pxPosRangeCheckLtPipe1(x).y
          )
        }
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(8)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    def myTempObjTileWidth = tempObjTileWidth(kind != 0)

      //    for (x <- 0 until myTempObjTileWidth) {
      //      //tempOutp.pxPosXGridIdxFlip(x) := (
      //      //  //Mux[Bool](
      //      //  //  //tempInp.pxPosXGridIdxMatches(x)
      //      //  //)
      //      //  //tempInp.pxPosXGridIdxLsb(x)

      //      //  //tempInp.pxPosXGridIdxFindFirstSameAsIdx(x)
      //      //  //&&
      //      //  tempOutp.pxPosXGridIdxMatches(x)
      //      //)
      //      tempOutp.pxPosXGridIdxMatches(x) := (
      //        tempInp.pxPosXGridIdxLsb(x)
      //        === (
      //          //Mux[UInt](tempInp.gridIdxLsb(0), U"1'd1", U"1'd0")
      //          tempInp.stage0.calcGridIdxLsb(kind)
      //        )
      //      )
      //      tempOutp.palEntryNzMemIdx(x) := (
      //        tempInp.palEntryMemIdx(x) =/= 0
      //      )

      //      tempOutp.pxPosRangeCheck(x).x := (
      //        //(tempInp.objAttrs.pos.x + myTempObjTileWidth - 1 >= 0)
      //        //&& (tempInp.objAttrs.pos.x < params.intnlFbSize2d.x)
      //        tempInp.pxPosRangeCheckGePipe1(x).x
      //        && tempInp.pxPosRangeCheckLtPipe1(x).x
      //      )
      //      tempOutp.pxPosRangeCheck(x).y := (
      //        //(
      //        //  outp.bgPxsPosSlice.pos.y.asSInt.resized
      //        //  >= tempInp.objAttrs.pos.y
      //        //) && (
      //        //  outp.bgPxsPosSlice.pos.y.asSInt.resized
      //        //  < tempInp.objAttrs.pos.y + myTempObjTileWidth.y
      //        //)
      //        tempInp.pxPosRangeCheckGePipe1(x).y
      //        && tempInp.pxPosRangeCheckLtPipe1(x).y
      //      )
      //    }
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).stage6 := stageData.pipeIn(idx).stage6
      //    def pipeIn = stageData.pipeIn(idx)
      //    def pipeOut = stageData.pipeOut(idx)
      //    pipeOut.stage7 := pipeIn.stage7
      //  },
      //)
      // END: Stage 10

      // BEGIN: Stage 11
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 11
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)

        for (x <- 0 until myTempObjTileWidth) {
          objPalEntryMemA2d(kind)(x).io.rdEn := True
          objPalEntryMemA2d(kind)(x).io.rdAddr := (
            RegNext(objPalEntryMemA2d(kind)(x).io.rdAddr) init(0x0)
          )
          when (cWrObjArr(idx).up.isFiring) {
            objPalEntryMemA2d(kind)(x).io.rdAddr := (
              tempInp.palEntryMemIdx(x)
            )
          }
        }
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(9)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    def myTempObjTileWidth = tempObjTileWidth(kind != 0)

      //    for (x <- 0 until myTempObjTileWidth) {
      //      objPalEntryMemA2d(kind)(x).io.rdEn := True
      //      objPalEntryMemA2d(kind)(x).io.rdAddr := (
      //        tempInp.palEntryMemIdx(x)
      //      )
      //    }
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //  }
      //)
      // END: Stage 11

      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 12
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        //def tempObjArrElemIdx = params.getObjSubLineMemArrElemIdx(
        //  addr=combinePipeLast.cnt
        //)
        //val dbgTestWrObjPipe5_tempObjArrElemIdx = UInt(
        //  tempObjArrElemIdx.getWidth bits
        //)
        //  .setName("dbgTestWrObjPipe5_tempObjArrElemIdx")

        for (x <- 0 until myTempObjTileWidth) {
          //tempOutp.palEntry(x) := objPalEntryMemArr.readAsync(
          //  address=tempInp.palEntryMemIdx(x)
          //)
          tempOutp.palEntry(x) := (
            RegNext(tempOutp.palEntry(x))
            init(tempOutp.palEntry(x).getZero)
          )
          when (cWrObjArr(idx).up.isFiring) {
            tempOutp.palEntry(x) := (
              objPalEntryMemA2d(kind)(x).io.rdData
            )
          }
          tempOutp.pxPosInLine(x) := (
            tempInp.pxPosRangeCheck(x).x
            && tempInp.pxPosRangeCheck(x).y
          )
          tempOutp.pxPosCmpForOverwrite(x) := (
            tempOutp.pxPosInLine(x)
            && tempInp.pxPosXGridIdxFindFirstSameAsFound
            && tempInp.pxPosXGridIdxMatches(x)
          )
        }
        def tempObjArrIdx = tempInp.getObjSubLineMemArrIdx(
          kind=kind,
          x=tempInp.pxPosXGridIdxFindFirstSameAsIdx,
        )
        val dbgTestWrObjPipe9_tempObjArrIdx = UInt(
          tempObjArrIdx.getWidth bits
        )
          .setName(
            f"dbgTestWrObjPipe9_tempObjArrIdx_$kind"
            //+ f"_$jdx"
            //+ f"_$x"
          )
        dbgTestWrObjPipe9_tempObjArrIdx := tempObjArrIdx

        //for (jdx <- 0 until 1 << rWrLineMemArrIdx.getWidth) {
        //  // We no longer need the `switch` statement here since we are
        //  // just reading
        //  //rdObjSubLineMemArr(jdx).addrVec(
        //  //  RdObjSubLineMemArrInfo.wrObjIdx
        //  //) := tempObjArrIdx
        //}
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(10)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      //    //def tempObjArrElemIdx = params.getObjSubLineMemArrElemIdx(
      //    //  addr=combinePipeLast.cnt
      //    //)
      //    //val dbgTestWrObjPipe5_tempObjArrElemIdx = UInt(
      //    //  tempObjArrElemIdx.getWidth bits
      //    //)
      //    //  .setName("dbgTestWrObjPipe5_tempObjArrElemIdx")

      //    for (x <- 0 until myTempObjTileWidth) {
      //      //tempOutp.palEntry(x) := objPalEntryMemArr.readAsync(
      //      //  address=tempInp.palEntryMemIdx(x)
      //      //)
      //      tempOutp.palEntry(x) := (
      //        objPalEntryMemA2d(kind)(x).io.rdData
      //      )
      //      tempOutp.pxPosInLine(x) := (
      //        tempInp.pxPosRangeCheck(x).x
      //        && tempInp.pxPosRangeCheck(x).y
      //      )
      //      tempOutp.pxPosCmpForOverwrite(x) := (
      //        tempOutp.pxPosInLine(x)
      //        && tempInp.pxPosXGridIdxFindFirstSameAsFound
      //        && tempInp.pxPosXGridIdxMatches(x)
      //      )
      //    }
      //    def tempObjArrIdx = tempInp.getObjSubLineMemArrIdx(
      //      kind=kind,
      //      x=tempInp.pxPosXGridIdxFindFirstSameAsIdx,
      //    )
      //    val dbgTestWrObjPipe9_tempObjArrIdx = UInt(
      //      tempObjArrIdx.getWidth bits
      //    )
      //      .setName(
      //        f"dbgTestWrObjPipe9_tempObjArrIdx_$kind"
      //        //+ f"_$jdx"
      //        //+ f"_$x"
      //      )
      //    dbgTestWrObjPipe9_tempObjArrIdx := tempObjArrIdx

      //    //for (jdx <- 0 until 1 << rWrLineMemArrIdx.getWidth) {
      //    //  // We no longer need the `switch` statement here since we are
      //    //  // just reading
      //    //  //rdObjSubLineMemArr(jdx).addrVec(
      //    //  //  RdObjSubLineMemArrInfo.wrObjIdx
      //    //  //) := tempObjArrIdx
      //    //}
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).stage9 := stageData.pipeIn(idx).stage9
      //    def pipeIn = stageData.pipeIn(idx)
      //    def pipeOut = stageData.pipeOut(idx)
      //    pipeOut.stage9 := pipeIn.stage9
      //  },
      //)

      // BEGIN: Stage 13
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 13
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = tempObjTileWidthPow(kind != 0)
        def myTempObjTileWidth1 = tempObjTileWidth1(kind != 0)
        def myTempObjTileWidthPow1 = tempObjTileWidthPow1(kind != 0)

        //for (jdx <- 0 until (1 << rWrLineMemArrIdx.getWidth)) {
        //  //val temp = wrObjSubLineMemArr(jdx)

        //  val temp = (
        //    if (kind == 0) {
        //      wrObjSubLineMemArr(jdx)
        //    } else {
        //      wrObjAffineSubLineMemArr(jdx)
        //    }
        //  )
        //  temp.io.rdEn := True
        //  temp.io.rdAddr := 0
        //  temp.io.rdAddr.allowOverride
        //}
        tempOutp.stage13.pxPosYLsb := (
          tempInp.pxPos(
            //tempInp.pxPosXGridIdxFindFirstSameAsIdx
            0
          ).y(0)
        )
        tempOutp.stage13.pxPosXGridIdx := (
          tempInp.pxPosXGridIdx(
            tempInp.pxPosXGridIdxFindFirstSameAsIdx
          )
        )
        for (x <- 0 until myTempObjTileWidth1) {
          for (
            jdx <- 0 until tempOutp.stage13.myIdxV2d(x).size
          ) {
            def myIdxVec = tempOutp.stage13.myIdxV2d(x)
            //val myIdx = UInt((myTempObjTileWidthPow + 1) bits)
            //  .setName(f"wrObjPipe10_myIdx_$x")
            //def sliceX = (
            //  if (kind == 0) {
            //    x
            //  } else {
            //    (
            //      x
            //      & ((
            //        1
            //        << params.objAffineSliceTileWidthPow
            //        //<< params.objAffineTileWidthRshift
            //      ) - 1)
            //    )
            //  }
            //)
            val myIdx = UInt(myTempObjTileWidthPow1 bits)
              .setName(
                f"wrObjPipe10_myIdx_$kind" + f"_$x" + f"_$jdx"
              )
            if (kind == 0) {
              //val myIdxFull = cloneOf(tempInp.pxPos(x).x)
              //  .setName(
              //    f"wrObjPipe10_myIdxFull_$kind" + f"_$x" + f"_$jdx"
              //  )
              val myIdxFull = cloneOf(tempInp.myIdxPxPosX(x))
                .setName(
                  f"wrObjPipe10_myIdxFull_$kind" + f"_$x" + f"_$jdx"
                )
              //myIdxFull := tempInp.pxPos(sliceX).x
              //myIdxFull := tempInp.pxPos(0).x + x
              //myIdxFull := tempInp.pxPos(x).x
              myIdxFull := tempInp.myIdxPxPosX(x)
              myIdx := myIdxFull.asUInt(myIdx.bitsRange)
              //myIdxVec(x) := myIdx
              //myIdxVec(jdx) := myIdx
            } else { // if (kind == 1)
              val myIdxFull = cloneOf(tempInp.myIdxPxPosX(x))
                .setName(
                  f"wrObjPipe10_myIdxFull_$kind" + f"_$x" + f"_$jdx"
                )
              //myIdxFull := tempInp.pxPos(sliceX).x
              //myIdxFull := tempInp.pxPos(0).x + x
              myIdxFull := tempInp.myIdxPxPosX(x)
              myIdx := myIdxFull.asUInt(myIdx.bitsRange)
              //myIdxVec(x) := myIdx
            }
            myIdxVec(jdx) := myIdx
          }
        }

      }
      //HandleDualPipe(
      //  stageData=stageData.craft(11)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      //    def myTempObjTileWidthPow = tempObjTileWidthPow(kind != 0)
      //    def myTempObjTileWidth1 = tempObjTileWidth1(kind != 0)
      //    def myTempObjTileWidthPow1 = tempObjTileWidthPow1(kind != 0)

      //    //for (jdx <- 0 until (1 << rWrLineMemArrIdx.getWidth)) {
      //    //  //val temp = wrObjSubLineMemArr(jdx)

      //    //  val temp = (
      //    //    if (kind == 0) {
      //    //      wrObjSubLineMemArr(jdx)
      //    //    } else {
      //    //      wrObjAffineSubLineMemArr(jdx)
      //    //    }
      //    //  )
      //    //  temp.io.rdEn := True
      //    //  temp.io.rdAddr := 0
      //    //  temp.io.rdAddr.allowOverride
      //    //}
      //    tempOutp.stage10.pxPosYLsb := (
      //      tempInp.pxPos(
      //        //tempInp.pxPosXGridIdxFindFirstSameAsIdx
      //        0
      //      ).y(0)
      //    )
      //    tempOutp.stage10.pxPosXGridIdx := (
      //      tempInp.pxPosXGridIdx(
      //        tempInp.pxPosXGridIdxFindFirstSameAsIdx
      //      )
      //    )
      //    for (x <- 0 until myTempObjTileWidth1) {
      //      for (
      //        jdx <- 0 until tempOutp.stage10.myIdxV2d(x).size
      //      ) {
      //        def myIdxVec = tempOutp.stage10.myIdxV2d(x)
      //        //val myIdx = UInt((myTempObjTileWidthPow + 1) bits)
      //        //  .setName(f"wrObjPipe10_myIdx_$x")
      //        //def sliceX = (
      //        //  if (kind == 0) {
      //        //    x
      //        //  } else {
      //        //    (
      //        //      x
      //        //      & ((
      //        //        1
      //        //        << params.objAffineSliceTileWidthPow
      //        //        //<< params.objAffineTileWidthRshift
      //        //      ) - 1)
      //        //    )
      //        //  }
      //        //)
      //        val myIdx = UInt(myTempObjTileWidthPow1 bits)
      //          .setName(
      //            f"wrObjPipe10_myIdx_$kind" + f"_$x" + f"_$jdx"
      //          )
      //        if (kind == 0) {
      //          //val myIdxFull = cloneOf(tempInp.pxPos(x).x)
      //          //  .setName(
      //          //    f"wrObjPipe10_myIdxFull_$kind" + f"_$x" + f"_$jdx"
      //          //  )
      //          val myIdxFull = cloneOf(tempInp.myIdxPxPosX(x))
      //            .setName(
      //              f"wrObjPipe10_myIdxFull_$kind" + f"_$x" + f"_$jdx"
      //            )
      //          //myIdxFull := tempInp.pxPos(sliceX).x
      //          //myIdxFull := tempInp.pxPos(0).x + x
      //          //myIdxFull := tempInp.pxPos(x).x
      //          myIdxFull := tempInp.myIdxPxPosX(x)
      //          myIdx := myIdxFull.asUInt(myIdx.bitsRange)
      //          //myIdxVec(x) := myIdx
      //          //myIdxVec(jdx) := myIdx
      //        } else { // if (kind == 1)
      //          val myIdxFull = cloneOf(tempInp.myIdxPxPosX(x))
      //            .setName(
      //              f"wrObjPipe10_myIdxFull_$kind" + f"_$x" + f"_$jdx"
      //            )
      //          //myIdxFull := tempInp.pxPos(sliceX).x
      //          //myIdxFull := tempInp.pxPos(0).x + x
      //          myIdxFull := tempInp.myIdxPxPosX(x)
      //          myIdx := myIdxFull.asUInt(myIdx.bitsRange)
      //          //myIdxVec(x) := myIdx
      //        }
      //        myIdxVec(jdx) := myIdx
      //      }
      //    }

      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    def pipeIn = stageData.pipeIn(idx)
      //    def pipeOut = stageData.pipeOut(idx)
      //    pipeOut.stage10 := pipeIn.stage10
      //  },
      //)
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 14
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        def myTempObjTileWidth1 = tempObjTileWidth1(kind != 0)
        def myTempObjTileWidthPow1 = tempObjTileWidthPow1(kind != 0)
        def myTempObjTileWidthPow = tempObjTileWidthPow(kind != 0)
        tempOutp.subLineMemEntryExt := (
          tempOutp.subLineMemEntryExt.getZero
        )
        tempOutp.subLineMemEntryExt.allowOverride
        tempOutp.subLineMemEntryExt.hazardId := (
          tempOutp.subLineMemEntryExt.getHazardIdIdleVal()
        )

        for (jdx <- 0 until (1 << wrObjPipeLineMemArrIdx(0).getWidth)) {
          //val temp = wrObjSubLineMemArr(jdx)

          //val temp = (
            if (kind == 0) {
              //wrObjSubLineMemArr(jdx)
            } else {
              val temp = wrObjAffineSubLineMemArr(jdx)
              temp.io.rdEn := True
              temp.io.rdAddr := 0
              temp.io.rdAddr.allowOverride
            }
          //)
          //temp.io.rdEn := True
          //temp.io.rdAddr := 0
          //temp.io.rdAddr.allowOverride
        }
        def stage10MyIdxV2d = tempInp.stage13.myIdxV2d
        val tempStage10MyIdxVec = Vec.fill(myTempObjTileWidth1)(
          UInt(myTempObjTileWidthPow1 bits)
        )
          .setName(f"wrObjPipe11_tempStage10MyIdxVec_$kind")
        //val tempObjXStart = (kind == 1) generate (
        //  tempInp.affineObjXStart()
        //    .setName(f"wrObjPipe11_tempAffineObjXStart_$kind")
        //)
        val tempObjXStart = (
          if (kind == 0) {
            tempInp.objXStart()
          } else {
            tempInp.affineObjXStart()
          }
        )

        val firstMyIdxZero = (
          if (kind == 0) {
            // we are guaranteed to find a zero
            tempStage10MyIdxVec.sFindFirst(
              _ === 0
            )
          } else {
            tempStage10MyIdxVec.sFindFirst(
              _ === 0
            )
            //tempStage10MyIdxVec.sFindFirst(
            //  _ === tempAffineObjXStart(
            //    tempAffineObjXStart.high - 1 downto 0
            //  )
            //)
            //tempStage10MyIdxVec.sFindFirst(
            //  _(
            //    params.objAffineTileSize2dPow.x - 1 downto 0
            //  ) === (
            //    tempInp.affineObjXStart()(
            //      //params.objAffineSliceTileWidthPow - 1 downto 0
            //      params.objAffineTileSize2dPow.x - 1 downto 0
            //    )
            //  )
            //)
          }
        )
          .setName(f"wrObjPipe11_firstMyIdxZero_$kind")
        //println(f"$myTempObjTileWidth1")
        for (x <- 0 until myTempObjTileWidth1) {
          def tempX = x
          def tempMyIdxVec = stage10MyIdxV2d(tempX)
          def tempMyIdx = tempMyIdxVec(x)
          tempStage10MyIdxVec(x) := tempMyIdx
          for (
            jdx <- 0 until tempOutp.stage14.myIdxV2d(x).size
          ) {
            def myIdx = tempOutp.stage14.myIdxV2d(x)(jdx)
            //myIdx := tempStage10MyIdxVec(
            //  (
            //    firstMyIdxZero._2
            //    //+ U(f"$myTempObjTileWidthPow'd$x")
            //    + x
            //  )(
            //    myTempObjTileWidthPow - 1 downto 0
            //  )
            //)
            myIdx := (
              firstMyIdxZero._2
              //+ U(f"$myTempObjTileWidthPow'd$x")
              + x
              //+ (
              //  if (kind == 0) {
              //    0
              //  } else {
              //    tempInp.affineObjXStart()
              //  }
              //)
            )
            //(
            //  //myTempObjTileWidthPow1 - 1 downto 0
            //  myTempObjTileWidthPow - 1 downto 0
            //)
            //myIdxVec(jdx) := 
          }
        }
        switch (
          //rWrLineMemArrIdx
          wrObjPipeLineMemArrIdx(1)
          //tempInp.lineMemArrIdx
        ) {
          for (
            jdx <- 0 until (1 << wrObjPipeLineMemArrIdx(1).getWidth)
          ) {
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
              val tempRdAddr = (
                if (kind == 0) {
                  //wrObjSubLineMemArr(jdx)
                  tempOutp.subLineMemEntryExt.memAddr
                } else {
                  wrObjAffineSubLineMemArr(jdx).io.rdAddr
                }
              )
                .setName({
                  if (kind == 0) (
                    s"wrObjPipe_12_tempRdAddr_${jdx}"
                  ) else (
                    s"wrObjAffinePipe_12_tempRdAddr_${jdx}"
                  )
                })
              val myHazardCmp = tempOutp.subLineMemEntryExt.hazardCmp
              //myHazardCmp.pxPosXGridIdxLsb := tempInp.pxPosXGridIdxLsb
              //if (kind == 0) {
              //  //--------
              //  //val prevTempRdAddr = KeepAttribute(
              //  //  RegNextWhen(
              //  //    tempRdAddr,
              //  //    cWrObjArr(idx).down.isFiring,
              //  //  ) init(tempRdAddr.getZero)
              //  //)
              //  //  .setName(s"wrObjPipe_12_prevTempRdAddr_${jdx}")
              //  //val prevCnt = KeepAttribute(
              //  //  RegNextWhen(
              //  //    tempInp.cnt,
              //  //    cWrObjArr(idx).down.isFiring,
              //  //  ) init(tempInp.cnt.getZero)
              //  //)
              //  //  .setName(s"wrObjPipe_12_prevCnt_${jdx}")
              //  //--------
              //  //tempOutp.subLineMemEntryExt.doHazardCheck := (
              //  //  (tempRdAddr === prevTempRdAddr)
              //  //)
              //  //--------
              //  //myHazardCmp.objAttrsMemIdx := (
              //  //  tempInp.stage0.objAttrsMemIdx()
              //  //)
              //  //--------

              //  //myHazardCmp.cnt := tempInp.stage0.rawObjAttrsMemIdx
              //  //for (kdx <- 0 until wrObjSubLineMemArr.size) {
              //  //  //wrObjSubLineMemArr(kdx).io.doHazardCheck := (
              //  //  //  tempRdAddr === prevTempRdAddr
              //  //  //  && tempInp.cnt =/= prevCnt
              //  //  //)
              //  //  //wrObjSubLineMemArr(kdx).io.doHazardCheck := (
              //  //  //  tempRdAddr === prevTempRdAddr
              //  //  //  && tempInp.cnt =/= prevCnt
              //  //  //)
              //  //}
              //} else { // if (kind == 1)
              //  myHazardCmp.affineObjAttrsMemIdx := (
              //    tempInp.stage0.affineObjAttrsMemIdx()
              //  )
              //}
              myHazardCmp.objIdx := (
                tempInp.objAttrsMemIdx
              )
              myHazardCmp.objIdxPlus1 := (
                myHazardCmp.objIdx + 1
              )
              myHazardCmp.anyPxPosInLine := (
                tempInp.pxPosInLine(0)
                || tempInp.pxPosInLine(tempInp.pxPosInLine.size - 1)
              )
              //tempOutp.stage10.rdSubLineMemEntry := (
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
                val tempX = tempInp.pxPos(
                  tempInp.pxPosXGridIdxFindFirstSameAsIdx
                ).x.asUInt
                  .setName{
                    def kindName = (
                      if (kind == 0) {
                        ""
                      } else { // if (kind == 1)
                        "Affine"
                      }
                    )
                    f"wrObj$kindName" + f"Pipe11_tempXSameAs_$jdx"
                  }
                tempRdAddr := (
                  if (kind == 0) {
                    params.getObjSubLineMemArrIdx(
                      //tempInp.cnt
                      //tempInp.pxPos(0).x.asUInt
                      tempX
                    )
                  } else {
                    params.getObjAffineSubLineMemArrIdx(
                      //tempInp.cnt
                      //tempInp.pxPos(0).x.asUInt
                      tempX
                    )
                  }
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
                val tempX = tempInp.pxPos(
                  tempInp.pxPosXGridIdxFindFirstDiffIdx
                ).x.asUInt
                  .setName{
                    def kindName = (
                      if (kind == 0) {
                        ""
                      } else { // if (kind == 1)
                        "Affine"
                      }
                    )
                    f"wrObj$kindName" + f"Pipe11_tempXDiff_$jdx"
                  }
                //temp.io.rdAddr := params.getObjSubLineMemArrIdx(
                //  //tempInp.cnt
                //  //tempInp.pxPos(0).x.asUInt
                //)
                tempRdAddr := (
                  if (kind == 0) {
                    params.getObjSubLineMemArrIdx(
                      //tempInp.cnt
                      //tempInp.pxPos(0).x.asUInt
                      tempX
                    )
                  } else {
                    params.getObjAffineSubLineMemArrIdx(
                      //tempInp.cnt
                      //tempInp.pxPos(0).x.asUInt
                      tempX
                    )
                  }
                )//.resized
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

      }
      //HandleDualPipe(
      //  stageData=stageData.craft(12)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)
      //    def myTempObjTileWidth1 = tempObjTileWidth1(kind != 0)
      //    def myTempObjTileWidthPow1 = tempObjTileWidthPow1(kind != 0)
      //    def myTempObjTileWidthPow = tempObjTileWidthPow(kind != 0)

      //    for (jdx <- 0 until (1 << rWrLineMemArrIdx.getWidth)) {
      //      //val temp = wrObjSubLineMemArr(jdx)

      //      val temp = (
      //        if (kind == 0) {
      //          wrObjSubLineMemArr(jdx)
      //        } else {
      //          wrObjAffineSubLineMemArr(jdx)
      //        }
      //      )
      //      temp.io.rdEn := True
      //      temp.io.rdAddr := 0
      //      temp.io.rdAddr.allowOverride
      //    }
      //    def stage10MyIdxV2d = tempInp.stage10.myIdxV2d
      //    val tempStage10MyIdxVec = Vec.fill(myTempObjTileWidth1)(
      //      UInt(myTempObjTileWidthPow1 bits)
      //    )
      //      .setName(f"wrObjPipe11_tempStage10MyIdxVec_$kind")
      //    //val tempObjXStart = (kind == 1) generate (
      //    //  tempInp.affineObjXStart()
      //    //    .setName(f"wrObjPipe11_tempAffineObjXStart_$kind")
      //    //)
      //    val tempObjXStart = (
      //      if (kind == 0) {
      //        tempInp.objXStart()
      //      } else {
      //        tempInp.affineObjXStart()
      //      }
      //    )

      //    val firstMyIdxZero = (
      //      if (kind == 0) {
      //        // we are guaranteed to find a zero
      //        tempStage10MyIdxVec.sFindFirst(
      //          _ === 0
      //        )
      //      } else {
      //        tempStage10MyIdxVec.sFindFirst(
      //          _ === 0
      //        )
      //        //tempStage10MyIdxVec.sFindFirst(
      //        //  _ === tempAffineObjXStart(
      //        //    tempAffineObjXStart.high - 1 downto 0
      //        //  )
      //        //)
      //        //tempStage10MyIdxVec.sFindFirst(
      //        //  _(
      //        //    params.objAffineTileSize2dPow.x - 1 downto 0
      //        //  ) === (
      //        //    tempInp.affineObjXStart()(
      //        //      //params.objAffineSliceTileWidthPow - 1 downto 0
      //        //      params.objAffineTileSize2dPow.x - 1 downto 0
      //        //    )
      //        //  )
      //        //)
      //      }
      //    )
      //      .setName(f"wrObjPipe11_firstMyIdxZero_$kind")
      //    //println(f"$myTempObjTileWidth1")
      //    for (x <- 0 until myTempObjTileWidth1) {
      //      def tempX = x
      //      def tempMyIdxVec = stage10MyIdxV2d(tempX)
      //      def tempMyIdx = tempMyIdxVec(x)
      //      tempStage10MyIdxVec(x) := tempMyIdx
      //      for (
      //        jdx <- 0 until tempOutp.stage11.myIdxV2d(x).size
      //      ) {
      //        def myIdx = tempOutp.stage11.myIdxV2d(x)(jdx)
      //        //myIdx := tempStage10MyIdxVec(
      //        //  (
      //        //    firstMyIdxZero._2
      //        //    //+ U(f"$myTempObjTileWidthPow'd$x")
      //        //    + x
      //        //  )(
      //        //    myTempObjTileWidthPow - 1 downto 0
      //        //  )
      //        //)
      //        myIdx := (
      //          firstMyIdxZero._2
      //          //+ U(f"$myTempObjTileWidthPow'd$x")
      //          + x
      //          //+ (
      //          //  if (kind == 0) {
      //          //    0
      //          //  } else {
      //          //    tempInp.affineObjXStart()
      //          //  }
      //          //)
      //        )
      //        //(
      //        //  //myTempObjTileWidthPow1 - 1 downto 0
      //        //  myTempObjTileWidthPow - 1 downto 0
      //        //)
      //        //myIdxVec(jdx) := 
      //      }
      //    }
      //    switch (
      //      rWrLineMemArrIdx
      //      //tempInp.lineMemArrIdx
      //    ) {
      //      for (jdx <- 0 until (1 << rWrLineMemArrIdx.getWidth)) {
      //        is (jdx) {
      //          //println(f"testificate: $jdx")
      //          //println({
      //          //  def size = wrObjSubLineMemArr.size
      //          //  f"$size"
      //          //})
      //          //val temp = wrObjSubLineMemArr(jdx).readAsync(
      //          //  params.getObjSubLineMemArrIdx(
      //          //    addr=tempInp.cnt
      //          //  )
      //          //)
      //          //--------
      //          val temp = (
      //            if (kind == 0) {
      //              wrObjSubLineMemArr(jdx)
      //            } else {
      //              wrObjAffineSubLineMemArr(jdx)
      //            }
      //          )
      //          //tempOutp.stage10.rdSubLineMemEntry := (
      //          //  temp.rdData
      //          //)
      //          when (tempInp.pxPosXGridIdxFindFirstSameAsFound) {
      //            //tempOutp.stage6.rdSubLineMemEntry := (
      //            //  //temp.readAsync(
      //            //  //  address=params.getObjSubLineMemArrIdx(
      //            //  //    //tempInp.cnt
      //            //  //    //tempInp.pxPos(0).x.asUInt
      //            //  //    tempInp.pxPos(
      //            //  //      tempInp.pxPosXGridIdxFindFirstSameAsIdx
      //            //  //    ).x.asUInt
      //            //  //  )
      //            //  //)
      //            //)
      //            def tempX = tempInp.pxPos(
      //              tempInp.pxPosXGridIdxFindFirstSameAsIdx
      //            ).x.asUInt
      //              .setName{
      //                def kindName = (
      //                  if (kind == 0) {
      //                    ""
      //                  } else { // if (kind == 1)
      //                    "Affine"
      //                  }
      //                )
      //                f"wrObj$kindName" + f"Pipe11_tempXSameAs_$jdx"
      //              }
      //            temp.io.rdAddr := (
      //              if (kind == 0) {
      //                params.getObjSubLineMemArrIdx(
      //                  //tempInp.cnt
      //                  //tempInp.pxPos(0).x.asUInt
      //                  tempX
      //                )
      //              } else {
      //                params.getObjAffineSubLineMemArrIdx(
      //                  //tempInp.cnt
      //                  //tempInp.pxPos(0).x.asUInt
      //                  tempX
      //                )
      //              }
      //            )
      //          } otherwise {
      //            //tempOutp.stage6.rdSubLineMemEntry := (
      //            //  //tempOutp.stage6.rdSubLineMemEntry.getZero
      //            //  temp.readAsync(
      //            //    address=params.getObjSubLineMemArrIdx(
      //            //      //tempInp.cnt
      //            //      //tempInp.pxPos(0).x.asUInt
      //            //      tempInp.pxPos(
      //            //        tempInp.pxPosXGridIdxFindFirstDiffIdx
      //            //      ).x.asUInt
      //            //    )
      //            //  )
      //            //)
      //            def tempX = tempInp.pxPos(
      //              tempInp.pxPosXGridIdxFindFirstDiffIdx
      //            ).x.asUInt
      //              .setName{
      //                def kindName = (
      //                  if (kind == 0) {
      //                    ""
      //                  } else { // if (kind == 1)
      //                    "Affine"
      //                  }
      //                )
      //                f"wrObj$kindName" + f"Pipe11_tempXDiff_$jdx"
      //              }
      //            //temp.io.rdAddr := params.getObjSubLineMemArrIdx(
      //            //  //tempInp.cnt
      //            //  //tempInp.pxPos(0).x.asUInt
      //            //)
      //            temp.io.rdAddr := (
      //              if (kind == 0) {
      //                params.getObjSubLineMemArrIdx(
      //                  //tempInp.cnt
      //                  //tempInp.pxPos(0).x.asUInt
      //                  tempX
      //                )
      //              } else {
      //                params.getObjAffineSubLineMemArrIdx(
      //                  //tempInp.cnt
      //                  //tempInp.pxPos(0).x.asUInt
      //                  tempX
      //                )
      //              }
      //            )//.resized
      //          }
      //          //--------
      //          //tempOutp.stage6.rdSubLineMemEntry := (
      //          //  //rdObjSubLineMemArr(jdx).dataVec(
      //          //  //  RdObjSubLineMemArrInfo.wrObjIdx
      //          //  //)
      //          //  wrObjSubLineMemArr(jdx).readAsync(
      //          //    params.getObjSubLineMemArrIdx(
      //          //      addr=tempInp.cnt
      //          //    )
      //          //  )
      //          //)
      //        }
      //      }
      //    }

      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //    idx: Int,
      //  ) => {
      //    def pipeIn = stageData.pipeIn(idx)
      //    def pipeOut = stageData.pipeOut(idx)
      //    pipeOut.stage11 := pipeIn.stage11
      //  },
      //)
      // END: Stage 14

      // BEGIN: Stage 15
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 15
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        //def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        tempOutp.subLineMemEntryExt := (
          //tempOutp.subLineMemEntryExt.getZero
          tempInp.subLineMemEntryExt
        )
        tempOutp.subLineMemEntryExt.allowOverride
        def myTempObjTileWidth1 = tempObjTileWidth1(kind != 0)
        //--------
        switch (
          //rWrLineMemArrIdx
          wrObjPipeLineMemArrIdx(2)
          //tempInp.lineMemArrIdx
        ) {
          for (
            jdx <- 0 until (1 << wrObjPipeLineMemArrIdx(2).getWidth)
          ) {
            is (jdx) {
              //--------
              val tempRdData = KeepAttribute(
                if (kind == 0) {
                  //wrObjSubLineMemArr(jdx)
                  tempInp.subLineMemEntryExt.rdMemWord
                } else {
                  wrObjAffineSubLineMemArr(jdx).io.rdData
                }
              )
                .setName(s"wrObjPipe_13_tempRdData_$jdx")
              tempOutp.stage15.rdSubLineMemEntry := (
                //temp.io.rdData
                tempRdData
              )
              //--------
            }
          }
        }
        //if (kind != 0) {
          for (
            //x <- 0 until myTempObjTileWidth
            x <- 0 until myTempObjTileWidth1
          ) {
            val tempObjXStart = (
              if (kind == 0) {
                //tempInp.objXStart()(
                //  params.objTileSize2dPow.x - 1
                //  downto //0
                //  params.objTileWidthRshift
                //)
                //Cat(
                  tempInp.objXStart()
                //).asUInt
              } else {
                //tempInp.affineObjXStart()(
                //  //myTempObjTileWidth1 - 1 downto 0
                //  (
                //    //tempInp.affineObjXStart().high - 1
                //    //- params.objAffineTileWidthRshift
                //    //params.objAffineTileSize2dPow.x - 1
                //    params.objAffineDblTileSize2dPow.x - 1
                //  )
                //  downto //0
                //  params.objAffineTileWidthRshift
                //)
                //Cat(
                //  tempInp.affineObjXStart()(
                //    tempInp.affineObjXStart().high
                //    downto 
                //      tempInp.affineObjXStart().high
                //      - params.objAffineTileWidthRshift + 1
                //  )
                //).asUInt
                //Cat(
                //  tempInp.affineObjXStart()(
                //    tempInp.affineObjXStart().high
                //    downto tempInp.affineObjXStart().high
                //  ),
                //  {
                //    def tempWidthPow = params.objAffineTileSize2dPow.x
                //    U(f"$tempWidthPow'd0")
                //  },
                //).asUInt
                tempInp.affineObjXStart()(
                  tempInp.affineObjXStart().high - 1 downto 0
                  //downto tempInp.affineObjXStart().high
                )
                //tempInp.affineObjXStart()(
              }
            )
              .setName{
                def kindName = (
                  if (kind == 0) {
                    ""
                  } else { // if (kind == 1)
                    "Affine"
                  }
                )
                f"wrObj$kindName" + f"Pipe13_tempObjXStart_$x"
              }
            //println(f"$kind")
            //def tempXPlusAmount = (
            //  if (kind == 0) {
            //    //U"1'd0".resized
            //    tempInp.stage0.objXStart()(
            //      tempInp.stage0.objXStart().high
            //      - params.objTileWidthRshift
            //      downto 0
            //    )
            //  } else {
            //    //def tempSliceIdx = (
            //    //  params.objAffineTileWidthRshift
            //    //  + 1 // account for double size rendering
            //    //  + 1 // account for the extra cycle delay
            //    //  + 1 // account for grid index
            //    //  - 1
            //    //)
            //    //def tempWidthPow = (
            //    //  params.objAffineTileSize2dPow.x - 1
            //    //)
            //    //Cat(
            //    //  tempInp.stage0.rawAffineIdx()(
            //    //    tempSliceIdx
            //    //  ),
            //    //  U(f"$tempWidthPow'd0"),
            //    //).asUInt
            //    tempInp.stage0.affineObjXStart()(
            //      tempInp.stage0.affineObjXStart().high
            //      - params.objAffineTileWidthRshift
            //      downto 0
            //    )
            //  }
            //)
            val tempCmpGe = (
              (
                x //+ tempXPlusAmount
              ) >= tempObjXStart
            )
              .setName{
                def kindName = (
                  if (kind == 0) {
                    ""
                  } else { // if (kind == 1)
                    "Affine"
                  }
                )
                f"wrObj$kindName" + f"Pipe13_tempCmpGe_$x"
              }
            val tempCmpLe = (
              x //+ tempXPlusAmount
              <= tempObjXStart
                + (
                  if (kind == 0) {
                    params.objSliceTileWidth
                  } else {
                    params.objAffineSliceTileWidth
                  }
                )
                - 1
            )
              .setName{
                def kindName = (
                  if (kind == 0) {
                    ""
                  } else { // if (kind == 1)
                    "Affine"
                  }
                )
                f"wrObj$kindName" + f"Pipe13_tempCmpLe_$x"
              }
            tempOutp.stage15.inMainVec(
              x
              //tempInp.stage11.myIdxV2d(x)(x)
            ) := (
              tempCmpGe && tempCmpLe
              //True
            )
          }
        //}
      }
      // END: Stage 15
      //{
      //  def idx = 15
      //  val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
      //    if (kind == 0) {
      //      initTempWrObjPipeOut(idx=idx)
      //    } else { // if (kind == 1)
      //      initTempWrObjAffinePipeOut(idx=idx)
      //    }
      //  )
      //  val tempInp = pipeIn
      //  val tempOutp = pipeOut
      //  //def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      //  //def myTempObjTileWidth2 = tempObjTileWidth2(kind != 0)
      //  def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      //  def myTempObjTileWidthPow = tempObjTileWidthPow(kind != 0)
      //  def myIdxV2d = tempInp.stage12.myIdxV2d
      //  for (x <- 0 until myTempObjTileWidth) {
      //    tempOutp.stage14.tempRdLineMemEntry(x) := (
      //      tempInp.rdSubLineMemEntry(
      //        //x
      //        // `myIdx` should be used here since it's an index into
      //        // `objSubLineMemArr(jdx)`
      //        //myIdx
      //        //myIdxVec(1)
      //        //x
      //        tempInp.stage11.myIdxV2d(x)(x)(
      //          //params.objAffineSliceTileWidthPow - 1 downto 0
      //          myTempObjTileWidthPow - 1 downto 0
      //        )
      //        //tempInp.stage11.myIdxV2d(x)(x)
      //      )
      //    )
      //    tempOutp.stage14.inMainVec(x) := (
      //      if (kind == 0) {
      //        tempInp.stage13.inMainVec(
      //          x + tempInp.objXStart()
      //        )
      //      } else { // if (kind == 1)
      //        tempInp.stage13.inMainVec(
      //          (x + tempInp.affineObjXStart())(
      //            params.objAffineTileSize2dPow.x - 1 downto 0
      //            //params.objAffineTileWidthRshift - 1 downto 0
      //          )
      //        )
      //      }
      //    )
      //  }
      //}

      // BEGIN: Stage 16
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 16
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        tempOutp.subLineMemEntryExt := (
          //tempOutp.subLineMemEntryExt.getZero
          tempInp.subLineMemEntryExt
        )
        tempOutp.subLineMemEntryExt.allowOverride
        //def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        //def myTempObjTileWidth2 = tempObjTileWidth2(kind != 0)
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = tempObjTileWidthPow(kind != 0)
        for (x <- 0 until myTempObjTileWidth) {
          tempOutp.stage16.tempRdLineMemEntry(x) := (
            tempInp.rdSubLineMemEntry(
              //x
              // `myIdx` should be used here since it's an index into
              // `objSubLineMemArr(jdx)`
              //myIdx
              //myIdxVec(1)
              //x
              tempInp.stage13.myIdxV2d(x)(x)(
                //params.objAffineSliceTileWidthPow - 1 downto 0
                myTempObjTileWidthPow - 1 downto 0
              )
              //tempInp.stage11.myIdxV2d(x)(x)
            )
          )
          tempOutp.stage16.inMainVec(x) := (
            if (kind == 0) {
              tempInp.stage15.inMainVec(
                x + tempInp.objXStart()
              )
            } else { // if (kind == 1)
              tempInp.stage15.inMainVec(
                (x + tempInp.affineObjXStart())(
                  params.objAffineTileSize2dPow.x - 1 downto 0
                  //params.objAffineTileWidthRshift - 1 downto 0
                )
              )
            }
          )
        }
        for (x <- 0 until myTempObjTileWidth) {
          tempOutp.stage16.tempRdLineMemEntry(x) := (
            tempInp.rdSubLineMemEntry(
              //x
              // `myIdx` should be used here since it's an index into
              // `objSubLineMemArr(jdx)`
              //myIdx
              //myIdxVec(1)
              //x
              tempInp.stage13.myIdxV2d(x)(x)(
                //params.objAffineSliceTileWidthPow - 1 downto 0
                myTempObjTileWidthPow - 1 downto 0
              )
              //tempInp.stage11.myIdxV2d(x)(x)
            )
          )
          tempOutp.stage16.inMainVec(x) := (
            if (kind == 0) {
              tempInp.stage15.inMainVec(
                x + tempInp.objXStart()
              )
            } else { // if (kind == 1)
              tempInp.stage15.inMainVec(
                (x + tempInp.affineObjXStart())(
                  params.objAffineTileSize2dPow.x - 1 downto 0
                  //params.objAffineTileWidthRshift - 1 downto 0
                )
              )
            }
          )
        }
        def outpExt = (
          //rWrObjPipeOut6Ext
          tempOutp.stage16.ext
          //cloneOf(tempOutp.stage14.ext)
        )
        val nonRotatedOutpExt = cloneOf(tempOutp.stage16.ext)
          .setName(f"wrObjPipe15_nonRotatedOutpExt_$kind")
        def myIdxV2d = tempInp.stage14.myIdxV2d

        outpExt := outpExt.getZero
        outpExt.allowOverride
        nonRotatedOutpExt := nonRotatedOutpExt.getZero
        nonRotatedOutpExt.allowOverride
        //val myIdxVec = Vec.fill(myTempObjTileWidth)(
        //  UInt(myTempObjTileWidthPow bits)
        //)
        //  .setName("wrObjPipe15_myIdxVec")
        def myMainFunc(
          x: Int,
          //inMain: Bool,
          //myIdx: UInt
          //myIdx: Int
        ): Unit = {
          //--------
          def inMain = (
            //tempInp.stage12.inMainVec(x)
            if (kind == 0) {
              //True
              //tempInp.stage13.inMainVec(
              //  x + tempInp.objXStart()
              //)
              tempOutp.stage16.inMainVec(x)
            } else {
              ////tempInp.stage12.inMainVec(
              ////  x
              ////  //tempInp.stage11.myIdxV2d(x)(x)
              ////  + tempInp.affineObjXStart()(
              ////    tempInp.affineObjXStart().high - 1 downto 0
              ////  )
              ////)
              ////True
              ////tempInp.stage13.inMainVec(
              ////  (x + tempInp.affineObjXStart())(
              ////    params.objAffineTileSize2dPow.x - 1 downto 0
              ////  )
              ////)
              //tempInp.stage13.inMainVec(
              //  (x + tempInp.affineObjXStart())(
              //    params.objAffineTileSize2dPow.x - 1 downto 0
              //    //params.objAffineTileWidthRshift - 1 downto 0
              //  )
              //)
              tempOutp.stage16.inMainVec(x)
            }
          )
          //--------
          //def sliceX = (
          //  x & ((1 << params.objAffineSliceTileWidthPow) - 1)
          //)
          def sliceX = (
            if (kind == 0) {
              x
            } else {
              (
                x
                //& ((
                //  1
                //  << params.objAffineSliceTileWidthPow
                //  //<< params.objAffineTileWidthRshift
                //) - 1)
              )
            }
          )
          //if (kind == 1) {
          //  println(f"sliceX: $sliceX")
          //}
          //val tempSliceX = {
          //  def width = params.objAffineSliceTileWidthPow
          //  (kind == 1) generate KeepAttribute(
          //    U(f"$width'd$sliceX")
          //      .setName(
          //        f"wrObjPipe15_tempSliceX_$kind" + f"_$x"
          //      )
          //  )
          //}
          //--------
          val tileX = (
            if (kind == 0) {
              def tempWidth = params.objTileSize2dPow.x
              //U(f"$tempWidth'd$x")
              def tempX = (
                x & ((1 << tempWidth) - 1)
              )
              U(f"$tempWidth'd$tempX")
            } else {
              //tempInp.affineObjXStart()
              def tempWidth = (
                //params.objAffineDblTileSize2dPow.x
                params.objAffineTileSize2dPow.x
              )
              def tempX = (
                x & ((1 << tempWidth) - 1)
              )
              U(f"$tempWidth'd$tempX")
            }
          )
            .setName(f"wrObjPipe15_tileX_$kind" + f"_$x")
          def tileSliceX = (
            if (kind == 0) {
              tileX(params.objSliceTileWidthPow - 1 downto 0)
            } else {
              tileX(params.objAffineSliceTileWidthPow - 1 downto 0)
            }
          )
          //def myIdxVec = myIdxV2d(
          //  U{
          //    def tempWidthPow = tempObjTileWidthPow1(kind != 0)
          //    f"$tempWidthPow'd$x"
          //  }
          //  //tileX
          //  //tileSliceX
          //)
          //def myIdxVec(someX: Int) = {
          //  myIdxV2d(
          //    U{
          //      def tempWidthPow = tempObjTileWidthPow(kind != 0)
          //      f"$tempWidthPow'd$x"
          //    }
          //    //tileX
          //    //tileSliceX
          //  )(someX)(
          //    myTempObjTileWidthPow - 1 downto 0
          //  )
          //}
          //--------
          val tempOverwriteLineMemEntry = (
            Bool()
          )
            .setName(
              f"wrObjPipe15_tempOverwriteLineMemEntry_$kind" + f"$x"
            )
          //--------
          // BEGIN: later
          def nonRotatedOverwriteLineMemEntry = (
            //rWrObjPipeOut15ExtData
            //outpExt
            nonRotatedOutpExt
            .overwriteLineMemEntry(
              //myIdx
              x
              //tileX
              //tileSliceX
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
          //val tempWrLineMemEntry = (
          //  cloneOf(
          //    //rWrObjPipeOut15ExtData
          //    nonRotatedOutpExt
          //    .wrLineMemEntry(myIdx)
          //  )
          //)
          //  .setName("dbgTempWrObjPipe15_tempWrLineMemEntry")
          //--------
          // BEGIN: later
          def nonRotatedWrLineMemEntry = (
            //rotatedWrLineMemEntry(
            //  //myIdx(tempMyIdxRange)
            //  myIdx
            //)
            //rWrObjPipeOut15ExtData
            //outpExt
            nonRotatedOutpExt
            .wrLineMemEntry(
              //// `myIdx` should be used here because `x` is not an index
              //// into an `ObjSubLineMemEntry`
              //myIdx
              x
              //tileX
              //tileSliceX
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
          //  rWrObjPipeOut15ExtData.wrLineMemEntry(x)
          //)

          //val tempOverwriteLineMemEntry = Bool()
          //val tempConcat = Bits(tempInp.numFwd + 1 bits)
          //--------
          // BEGIN: debug comment this out; later
          ////--------
          def calcTempWrLineMemEntry(
            someOverwriteLineMemEntry: Bool,
            someWrLineMemEntry: ObjSubLineMemEntry,
            someRdLineMemEntry: ObjSubLineMemEntry,
          ): Unit = {
            // BEGIN: debug comment this out; later
            when (
              someOverwriteLineMemEntry
              //&& inMain
            ) {
            // END: debug comment this out; later
            //--------
              // Here it should be `x` (not `myIdx`) here because `myIdx`
              // is just an index into `ObjSubLineMemEntry`s, rather than
              // an index into sprite tiles themselves
              if (inSim) {
                someWrLineMemEntry.addr := (
                  tempInp.pxPos(
                    //x
                    sliceX
                    //myIdx
                    //myIdxVec(0)
                    //myIdx(tempMyIdxRange)
                  ).x.asUInt(
                    someWrLineMemEntry.addr.bitsRange
                  )
                  //default -> False
                )
              }
              someWrLineMemEntry.col.rgb := (
                tempInp.palEntry(
                  //x
                  sliceX
                  //myIdx
                  //myIdx(tempMyIdxRange)
                ).col
                //tempWrLineMemEntry.col.rgb.getZero
              )
              //tempOutp.wrLineMemEntry.col.a := True
              someWrLineMemEntry.col.a := (
                tempInp.palEntryNzMemIdx(
                  //x
                  sliceX
                  //myIdx
                  //myIdx(tempMyIdxRange)
                )
                //False
              )
              //tempOutp.wrLineMemEntry.prio(
              //  tempInp.objAttrs.prio.bitsRange
              //) := tempInp.objAttrs.prio
              someWrLineMemEntry.prio := (
                tempInp.objAttrs.prio
              )
              //tempOutp.wrLineMemEntry.prio.msb := True
              someWrLineMemEntry.written := True
              if (!noColorMath) {
                someWrLineMemEntry.colorMathInfo := (
                  tempInp.objAttrs.colorMathInfo
                )
              }
              someWrLineMemEntry.objIdx := (
                if (kind == 0) {
                  tempInp.objAttrsMemIdx.resized
                } else { // if (kind == 1)
                  tempInp.stage0.affineObjAttrsMemIdx().resized
                }
              )
            //--------
            // BEGIN: debug comment this out; later
            } otherwise {
              //tempOutp.wrLineMemEntry := tempInp.rdSubLineMemEntry
              someWrLineMemEntry := someRdLineMemEntry
            }
          }
          def calcTempOverwiteLineMemEntry(
            somePxPosCmp: Bool,
            someLineMemEntry: ObjSubLineMemEntry,
            someOverwriteLineMemEntry: Bool,
          ): Unit = {
            val myOverwriteLineMemEntry = Bool()
            when (
              if (kind == 0) {
                //!tempInp.objAttrs.affine.doIt
                True
                //tempInp.stage0.rawObjAttrsMemIdx()(0)
              } else {
                (
                  //tempInp.objAttrs.affine.doIt
                  tempInp.stage8.affineDoIt(
                    //x
                    sliceX
                  )
                  //&& tempInp.stage5.oorTilePxsCoord(sliceX).x
                  //&& tempInp.stage5.oorTilePxsCoord(sliceX).y
                  //&& tempInp.stage0.affineActive
                  //&& inMain
                )
              }
            ) {
              someOverwriteLineMemEntry := myOverwriteLineMemEntry
            } otherwise {
              someOverwriteLineMemEntry := False
            }
            def doPrioGe() = (
              tempInp.palEntryNzMemIdx(
                //x
                sliceX
                //myIdx
                //myIdx(tempMyIdxRange)
              )
            )
            def doPrioLt() = (
              !someLineMemEntry.col.a
              && tempInp.palEntryNzMemIdx(
                //x
                sliceX
                //myIdx
                //myIdx(tempMyIdxRange)
              )
            )
            if (params.fancyObjPrio) {
              if (params.numBgsPow == log2Up(2)) {
                def width = 4
                switch (Cat(
                  somePxPosCmp,
                  !someLineMemEntry.written,
                  someLineMemEntry.prio,
                  tempInp.objAttrs.prio,
                )) {
                  is (
                    new MaskedLiteral(
                      value=0,
                      //careAbout=(1 << width) - 1,
                      careAbout=1 << (width - 1),
                      width=width,
                    )
                  ) {
                    myOverwriteLineMemEntry := False
                  }
                  is (
                    new MaskedLiteral(
                      value=(
                        (1 << (width - 1))
                        | (1 << (width - 2))
                      ),
                      careAbout=(
                        (1 << (width - 1))
                        | (1 << (width - 2))
                      ),
                      width=width,
                    ),
                  ) {
                    myOverwriteLineMemEntry := True
                  }
                  is (M"1000") { // 0 == 0
                    myOverwriteLineMemEntry := doPrioGe()
                  }
                  is (M"1011") { // 1 == 1
                    myOverwriteLineMemEntry := doPrioGe()
                  }
                  is (M"1001") { // 0 < 1
                    myOverwriteLineMemEntry := doPrioLt()
                  }
                  default {
                    //myOverwriteLineMemEntry := True
                    myOverwriteLineMemEntry := doPrioGe()
                  }
                }
              } else if (params.numBgsPow == log2Up(4)) {
                def width = 6
                switch (Cat(
                  somePxPosCmp,
                  !someLineMemEntry.written,
                  someLineMemEntry.prio,
                  tempInp.objAttrs.prio,
                )) {
                  is (
                    new MaskedLiteral(
                      value=0,
                      //careAbout=(1 << width) - 1,
                      careAbout=1 << (width - 1),
                      width=width,
                    )
                  ) {
                    myOverwriteLineMemEntry := False
                  }
                  is (
                    new MaskedLiteral(
                      value=(
                        (1 << (width - 1))
                        | (1 << (width - 2))
                      ),
                      careAbout=(
                        (1 << (width - 1))
                        | (1 << (width - 2))
                      ),
                      width=width,
                    ),
                  ) {
                    myOverwriteLineMemEntry := True
                  }
                  //is (
                  //  new MaskedLiteral(
                  //    value=(
                  //      (1 << (width - 1))
                  //      //| (1 << (width - 2))
                  //    ),
                  //    careAbout=(
                  //      (1 << (width - 1))
                  //      | (1 << (width - 2))
                  //    ),
                  //    width=width,
                  //  ),
                  //) {
                  //}
                  is (M"100001") { // 0 < 1
                    myOverwriteLineMemEntry := doPrioLt()
                  }
                  is (M"100-1-") { // 0 < 2, 0 < 3; 1 < 2, 1 < 3
                    myOverwriteLineMemEntry := doPrioLt()
                  }
                  //is (M"10001-") { // 0 < 2, 0 < 3
                  //  myOverwriteLineMemEntry := doPrioLt()
                  //}
                  //is (M"10011-") { // 1 < 2, 1 < 3
                  //  myOverwriteLineMemEntry := doPrioLt()
                  //}
                  is (M"101011") { // 2 < 3
                    myOverwriteLineMemEntry := doPrioLt()
                  }

                  is (M"100000") { // 0 === 0
                    myOverwriteLineMemEntry := doPrioGe()
                  }
                  is (M"100101") { // 1 === 1
                    myOverwriteLineMemEntry := doPrioGe()
                  }
                  is (M"101010") { // 2 === 2
                    myOverwriteLineMemEntry := doPrioGe()
                  }
                  is (M"101111") { // 3 === 3
                    myOverwriteLineMemEntry := doPrioGe()
                  }
                  default {
                    //myOverwriteLineMemEntry := True
                    myOverwriteLineMemEntry := doPrioGe()
                  }
                }
              } else {
                switch (Cat(
                  somePxPosCmp,
                  !someLineMemEntry.written,
                  //someLineMemEntry.prio,
                  //tempInp.objAttrs.prio,
                  someLineMemEntry.prio
                    < tempInp.objAttrs.prio,
                  //someLineMemEntry.prio
                  //  === tempInp.objAttrs.prio,
                  //someLineMemEntry.prio
                  //  > tempInp.objAttrs.prio,
                )) {
                  //is (M"0----")
                  is (M"0-") {
                    myOverwriteLineMemEntry := False
                  }
                  //is (M"11---")
                  is (M"11-") {
                    myOverwriteLineMemEntry := True
                  }
                  //is (M"101--") 
                  is (M"101") {
                    myOverwriteLineMemEntry := doPrioLt()
                  }
                  default {
                    myOverwriteLineMemEntry := doPrioGe()
                  }
                  //is (M"1001-") {
                  //  myOverwriteLineMemEntry := doPrioGe()
                  //}
                  //default {
                  //  myOverwriteLineMemEntry := doPrioGe()
                  //}
                }
              }
              //when (
              //  //--------
              //  // BEGIN: move this to prior pipeline stage; later
              //  somePxPosCmp
              //  // END: move this to prior pipeline stage; later
              //  //--------
              //) {
              //  // BEGIN: debug comment this out
              //  when (
              //    !someLineMemEntry.written
              //  ) {
              //    //dbgTestificate := 0
              //    myOverwriteLineMemEntry := True
              //  } otherwise {
              //    when (
              //      //someLineMemEntry.prio < tempInp.objAttrs.prio
              //      someLineMemEntry.prio < tempInp.objAttrs.prio
              //    ) {
              //      myOverwriteLineMemEntry := doPrioLt()
              //    }
              //    //elsewhen (
              //    //  //tempLineMemEntryPrio === tempInp.objAttrs.prio
              //    //  //someLineMemEntry.prio === tempInp.objAttrs.prio
              //    //) {
              //    //  //myOverwriteLineMemEntry := doPrioLt()
              //    //  //myOverwriteLineMemEntry := True
              //    //  myOverwriteLineMemEntry := doPrioGe()
              //    //}
              //    .otherwise {
              //      myOverwriteLineMemEntry := True
              //      myOverwriteLineMemEntry := doPrioGe()
              //    }
              //  }
              //} otherwise {
              //  // END: debug comment this out
              //  myOverwriteLineMemEntry := False
              //}
            } else { // if (!params.fancyObjPrio)
              switch (Cat(
                somePxPosCmp,
                !someLineMemEntry.written,
              )) {
                is (M"0-") {
                  myOverwriteLineMemEntry := False
                }
                is (M"11") {
                  myOverwriteLineMemEntry := True
                }
                default {
                  //myOverwriteLineMemEntry := doPrioLt()
                  myOverwriteLineMemEntry := doPrioGe()
                }
              }
            }
          }
          //--------
          //val tempRdLineMemEntry = ObjSubLineMemEntry()
          //  .setName(f"wrObjPipeStage15_tempRdLineMemEntry_$x")
          def tempRdLineMemEntry = (
            tempOutp.stage16.tempRdLineMemEntry(x)
          )
          //--------
          // BEGIN: debug comment this out; later
          //val tempConcat = Bits(fwdVec.size bits)

          //val tempOverwriteLineMemEntry = Bool()
          val tempWrLineMemEntry = (
            cloneOf(
              //rWrObjPipeOut15ExtData
              nonRotatedOutpExt
              .wrLineMemEntry(
                //myIdxVec(0)
                0
              )
            )
          )
          //val tempRdLineMemEntry = ObjSubLineMemEntry()
          //  .setName(f"wrObjPipe15_tempRdLineMemEntry_$kind" + f"_$x")
          //tempRdLineMemEntry := tempInp.rdSubLineMemEntry(
          //  //x
          //  // `myIdx` should be used here since it's an index into
          //  // `objSubLineMemArr(jdx)`
          //  //myIdx
          //  //myIdxVec(1)
          //  //x
          //  tempInp.stage11.myIdxV2d(x)(x)(
          //    //params.objAffineSliceTileWidthPow - 1 downto 0
          //    myTempObjTileWidthPow - 1 downto 0
          //  )
          //  //tempInp.stage11.myIdxV2d(x)(x)
          //)
          when (inMain) {
            calcTempOverwiteLineMemEntry(
              somePxPosCmp=(
                tempInp.pxPosCmpForOverwrite(
                  // this should be `x` because it's an index from the
                  // sprite's perspective
                  //x
                  sliceX
                )
              ),
              //someLineMemEntry=tempInp.rdSubLineMemEntry,
              someLineMemEntry=tempRdLineMemEntry,
              someOverwriteLineMemEntry=(
                //tempOutp.overwriteLineMemEntry(x)
                tempOverwriteLineMemEntry
              )
            )
            calcTempWrLineMemEntry(
              someOverwriteLineMemEntry=tempOverwriteLineMemEntry,
              someWrLineMemEntry=tempWrLineMemEntry,
              someRdLineMemEntry=tempRdLineMemEntry,
            )
          } otherwise {
            tempOverwriteLineMemEntry := True //False //True
            tempWrLineMemEntry := tempRdLineMemEntry
          }
          nonRotatedOverwriteLineMemEntry := (
            tempOverwriteLineMemEntry
          )
          nonRotatedWrLineMemEntry := (
            tempWrLineMemEntry
          )
          // END: debug comment this out; later
          //--------
        }
        for (
          x <- 0
          until tempObjTileWidth(kind != 0)
          //until tempObjTileWidth1(kind != 0)
        ) {
          //tempFunc(someX=x)
          myMainFunc(
            x=x,
            //inMain=tempInp.stage12.inMainVec(x),
          )

          outpExt.wrLineMemEntry(x) := (
            nonRotatedOutpExt.wrLineMemEntry(
              myIdxV2d(x)(
                //x * 2
                x
              )(
                myTempObjTileWidthPow - 1 downto 0
              )
            )
          )
          outpExt.overwriteLineMemEntry(x) := (
            nonRotatedOutpExt.overwriteLineMemEntry(
              myIdxV2d(x)(
                //x * 2
                x
              )(
                myTempObjTileWidthPow - 1 downto 0
              )
            )
          )
          when (outpExt.overwriteLineMemEntry(x)) {
            tempOutp.subLineMemEntryExt.modMemWord(x) := (
              outpExt.wrLineMemEntry(x)
            )
          } otherwise {
            tempOutp.subLineMemEntryExt.modMemWord(x) := (
              tempInp.subLineMemEntryExt.rdMemWord(x)
            )
          }
        }
        //myMainFunc()
      }
      {
        def idx = 17
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        //def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        //def myTempObjTileWidth2 = tempObjTileWidth2(kind != 0)
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = tempObjTileWidthPow(kind != 0)
      }
      {
        def idx = 18
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        //def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        //def myTempObjTileWidth2 = tempObjTileWidth2(kind != 0)
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = tempObjTileWidthPow(kind != 0)
      }
      {
        def idx = 19
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        val tempInp = pipeIn
        val tempOutp = pipeOut
        //def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        //def myTempObjTileWidth2 = tempObjTileWidth2(kind != 0)
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = tempObjTileWidthPow(kind != 0)
      }

      // BEGIN: Stage 15
      ////HandleDualPipe(
      ////  stageData=stageData.craft(15)
      ////)(
      ////  pipeStageMainFunc=(
      ////    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    val tempInp = stageData.pipeIn(idx)
      ////    val tempOutp = stageData.pipeOut(idx)
      ////    //def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      ////    //def myTempObjTileWidth2 = tempObjTileWidth2(kind != 0)
      ////    def myTempObjTileWidth = tempObjTileWidth(kind != 0)
      ////    def myTempObjTileWidthPow = tempObjTileWidthPow(kind != 0)

      ////    if (kind == 1) {
      ////      val tempVec = Vec.fill(myTempObjTileWidth)(Bool())

      ////      for (jdx <- 0 until myTempObjTileWidth) {
      ////        tempVec(jdx) := tempInp.wrLineMemEntry(jdx).written
      ////        //tempVec(jdx) := tempInp.overwriteLineMemEntry(jdx)
      ////      }
      ////      tempOutp.haveAnyWritten := (
      ////        //tempVec.sFindFirst(
      ////        //  _ === True
      ////        //)._1
      ////        True
      ////      )
      ////    }
      ////  },
      ////  copyOnlyFunc=(
      ////    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    def pipeIn = stageData.pipeIn(idx)
      ////    def pipeOut = stageData.pipeOut(idx)
      ////    pipeOut.stage15 := pipeIn.stage15
      ////  },
      ////)
      //// END: Stage 15
      ////--------
      //// BEGIN: Stage 7
      ////HandleDualPipe(
      ////  stageData=stageData.craft(7)
      ////)(
      ////  pipeStageMainFunc=(
      ////    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    val tempInp = stageData.pipeIn(idx)
      ////    val tempOutp = stageData.pipeOut(idx)

      ////    //when (tempOutp.fire) {
      ////    tempOutp.stage7.ext := rWrObjPipeOut6ExtData
      ////    //} otherwise {
      ////    //  tempOutp.stage7
      ////    //}
      ////  },
      ////  copyOnlyFunc=(
      ////    stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      ////    idx: Int,
      ////  ) => {
      ////    stageData.pipeOut(idx).stage7 := stageData.pipeIn(idx).stage7
      ////  },
      ////)
      //// END: Stage 7
      //--------

      //val objLineMem = objSubLineMemArr(someWrLineMemArrIdx)
      def tempNWrObjPipeLast = (
        if (kind == 0) {
          //wrObjPipeLast
          nWrObjArr.last
        } else {
          //wrObjAffinePipeLast
          nWrObjAffineArr.last
        }
      )
      def tempWrObjPipeLast = (
        if (kind == 0) {
          tempNWrObjPipeLast(
            //wrObjPipePayloadMain
            //wrObjPipePayloadSlmRmwBackOutp
            wrObjPipePayloadMain(wrObjPipePayloadMain.size - 1)
          )
        } else {
          tempNWrObjPipeLast(wrObjAffinePipePayload)
        }
      )
      def tempObjWriter = (
        if (kind == 0) {
          objWriter
        } else {
          objAffineWriter
        }
      )
      when (tempNWrObjPipeLast.isFiring) {
        //val tempLineMemEntry = LineMemEntry()
        //val objIdx = tempWrObjPipeLast.objIdx

        // BEGIN: come back to this later

        //val tempObjSubLineMemArrIdx = UInt(
        //  params.objSubLineMemArrSizePow bits
        //)
        //  .setName("wrObjPipeLast_tempArrIdx")
        //tempObjSubLineMemArrIdx := params.getObjSubLineMemTempArrIdx(
        //  pxPosX=tempWrObjPipeLast.pxPos.x.asUInt
        //)
        //val tempAddr = UInt(
        //  //log2Up(params.oneLineMemSize) bits
        //  //log2Up(params.objSubLineMemSize) bits
        //  params.objSubLineMemSizePow bits
        //)
        //  .setName("wrObjPipeLast_tempAddr")

        //tempAddr := tempWrObjPipeLast.pxPos.x.asUInt(
        //  //log2Up(params.oneLineMemSize) - 1 downto 0
        //  //tempAddr.bitsRange
        //  //params.objSubLineMemSizePow
        //  //log2Up(params.objSubLineMemSizePow
        //)
        //tempAddr := params.getObjSubLineMemTempAddr(
        //  pxPosX=tempWrObjPipeLast.pxPos.x.asUInt
        //)
        // END: come back to this later
        //val tempSubLineMemEntry = Vec.fill(params.objTileSize2d.x)(
        //  ObjSubLineMemEntry()
        //)
        //for (x <- 0 to params.objTileSize2d.x - 1) {
          val tempAffineObjAttrsMemIdx = (kind == 1) generate (
            KeepAttribute(
              tempWrObjPipeLast.stage0.affineObjAttrsMemIdx()
            )
              .setName("wrObjAffinePipeLast_affineObjAttrsMemIdx")
          )
          val tempAffineObjXStart = (kind == 1) generate (
            KeepAttribute(tempWrObjPipeLast.affineObjXStart())
              .setName("wrObjAffinePipeLast_affineObjXStart")
          )
          val tempAffineObjGridIdx = (kind == 1) generate (
            KeepAttribute(
              tempWrObjPipeLast.stage0.calcGridIdxLsb(kind=kind)
            )
              .setName("wrObjAffinePipeLast_affineObjGridIdx")
          )
          def tempObjArrIdx = tempWrObjPipeLast.getObjSubLineMemArrIdx(
            kind=kind,
            x=(
              (
                tempWrObjPipeLast.pxPosXGridIdxFindFirstSameAsIdx
                + (
                  if (kind == 0) {
                    //U("0").resized
                    tempWrObjPipeLast.objXStart()
                  } else {
                    tempWrObjPipeLast.affineObjXStart()
                  }
                )
              )
              (
                (
                  if (kind == 0) {
                    //params.objTileSize2dPow.x - 1
                    params.objSliceTileWidthPow - 1
                  } else {
                    params.objAffineSliceTileWidthPow - 1
                  }
                )
                downto 0
              )
            )
          )
          val dbgTestWrObjPipeLast_tempObjArrIdx = UInt(
            tempObjArrIdx.getWidth bits
          )
            .setName(
              f"dbgTestWrObj"
              + (
                if (kind == 0) {
                  ""
                } else {
                  "Affine"
                }
              )
              + f"PipeLast_tempObjArrIdx" //+ f"_$jdx" //+ f"_$x"
            )
          //--------
          // BEGIN: new code, with muxing for single `.write()` call
          dbgTestWrObjPipeLast_tempObjArrIdx := tempObjArrIdx
          tempObjWriter.addrVec(0) := tempObjArrIdx
          tempObjWriter.dataVec(0) := (
            tempWrObjPipeLast.wrLineMemEntry
          )
          tempObjWriter.enVec(0) := (
              //when (
              //  if (kind == 0) {
              //    !tempInp.objAttrs(kind).affine.doIt
              //  } else {
              //    (
              //    )
              //  }
              //) {
            //True
            {
              //--------
              def myTempObjTileWidth = (
                tempObjTileWidth(kind != 0)
              )
              val tempVec = Vec.fill(myTempObjTileWidth)(Bool())
                .setName(
                  f"dbgTestWrObj"
                  + (
                    if (kind == 0) {
                      ""
                    } else {
                      "Affine"
                    }
                  )
                  + f"PipeLast_tempVec"
                )

              for (jdx <- 0 until myTempObjTileWidth) {
                tempVec(jdx) := (
                  //tempWrObjPipeLast.wrLineMemEntry(jdx).written
                  //tempWrObjPipeLast.wrLineMemEntry(jdx).written
                  tempWrObjPipeLast.overwriteLineMemEntry(jdx)
                )
                //tempVec(jdx) := tempInp.overwriteLineMemEntry(jdx)
              }
              //--------
              val tempFindFirstFound = (
                tempVec.sFindFirst(
                  _ === True
                )._1
              )
                .setName(
                  f"dbgTestWrObj"
                  + (
                    if (kind == 0) {
                      ""
                    } else {
                      "Affine"
                    }
                  )
                  + f"PipeLast_tempFindFirstFound"
                )
              !tempWrObjPipeLast.stage0.rawObjAttrsMemIdx()(
                //params.objAffineTileWidthRshift
                0
              ) && (
                tempFindFirstFound
              )
            //!tempWrObjPipeLast.objAttrs.affine.doIt
            //tempWrObjPipeLast.stage10.ext.overwriteLineMemEntry
            //  .reduceBalancedTree(_ || _)
            }
          )
          ////--------
          //--------
        //}
      } otherwise { // when (!tempNWrObjPipeLast.isFiring)
        //objWriter.enVec(rWrLineMemArrIdx) := False
        tempObjWriter.enVec(0) := False
        //if (kind == 0) {
        //  objWriter.enVec(0) := False
        //} else {
        //  objAffineWriter.enVec(0) := False
        //}
      }
    }
    //--------
    def combineLineMemEntries(
      //someCombineLineMemArrIdx: Int
    ): Unit = {
      //val stageData = DualPipeStageData[Stream[CombinePipePayload]](
      //  pipeIn=combinePipeIn,
      //  pipeOut=combinePipeOut,
      //  pipeNumMainStages=combinePipeNumMainStages,
      //  pipeStageIdx=0,
      //)
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 0
        //val tempInp = stageData.pipeIn(idx)
        //val tempOutp = stageData.pipeOut(idx)

        //when (clockDomain.isResetActive) {
        //  tempOutp.stage0 := tempOutp.stage0.getZero
        //} otherwise {
        //  tempOutp.stage0 := tempInp.stage0
        //}
        val (pipeIn, pipeOut) = initCombinePipeOut(
          idx=idx,
          payload=combinePipePreForkPayload,
        )
        def tempInp = pipeIn
        def tempOutp = pipeOut

        when (clockDomain.isResetActive) {
          tempOutp.stage0 := tempOutp.stage0.getZero
        } otherwise {
          tempOutp.stage0 := tempInp.stage0
        }

        //tempOut.stage0 := tempInp.stage0
        //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(0)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage0 := tempOutp.stage0.getZero
      //    } otherwise {
      //      tempOutp.stage0 := tempInp.stage0
      //    }
      //    //tempOut.stage0 := tempInp.stage0
      //    //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
      //    //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage0 := tempOutp.stage0.getZero
      //    } otherwise {
      //      if (idx != 1) {
      //        tempOutp.stage0 := tempInp.stage0
      //      } else {
      //        //switch (rCombineFullLineMemArrIdx) {
      //        //  for (
      //        //    combineIdx <- 0 until params.numLineMemsPerBgObjRenderer
      //        //  ) {
      //        //    is (combineIdx) {
      //              tempOutp.stage0 := (
      //                combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage0
      //              )
      //        //    }
      //        //  }
      //        //}
      //      }
      //    }
      //  },
      //)
      //HandleDualPipe(
      //  stageData=stageData.craft(
      //    1
      //    //2
      //    //wrBgObjPipeNumStages + 1
      //  )
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    //val tempInp = stageData.pipeIn(idx)
      //    //val tempOutp = stageData.pipeOut(idx)
      //    //tempOutp.stage2 := (
      //    //  combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage2
      //    //)

      //    ////val tempCombineLineMemIdx = createTempCnt(
      //    ////  tempOutp=tempOutp,
      //    ////  pipeStageIdx=1
      //    ////)

      //    ////def outpExt = (
      //    ////  tempOutp.stage1.ext
      //    ////  //rCombinePipeOut1Ext
      //    ////)
      //    ////def cntFifoDepth = 3
      //    ////def cntFifoStallGeAmountCanPop = 1
      //    ////val cntFifo = AsyncReadFifo(
      //    ////  dataType=UInt(tempInp.cnt.getWidth bits),
      //    ////  depth=cntFifoDepth,
      //    ////)
      //    ////when (tempInp.cnt + 1 >= params.oneLineMemSize) {
      //    ////}
      //    ////val rTempCombineLineMemIdx = KeepAttribute(
      //    ////  Reg(
      //    ////    UInt(
      //    ////      log2Up(
      //    ////        //params.oneLineMemSize
      //    ////        params.oneLineMemSize
      //    ////      ) bits
      //    ////    )
      //    ////  )
      //    ////    .init(S"2'd-1".resized.asUInt)
      //    ////    .setName("dbgTestCombinePipe1_tempCombineLineMemIdx")
      //    ////    //.setName("tempCombineLineMemIdx")
      //    ////)
      //    //val tempCombineLineMemIdx = UInt(
      //    //  log2Up(params.oneLineMemSize) bits
      //    //)
      //    //  .setName("dbgTestCombinePipe1_tempCombineLineMemIdx")
      //    //tempCombineLineMemIdx := tempInp.cnt(
      //    //  tempCombineLineMemIdx.bitsRange
      //    //)
      //    ////val rTempCombineLineMemIdx = Reg(cloneOf(tempOutp.lineMemIdx))
      //    ////  .init(0x0)
      //    ////--------
      //    ////val nextTempCombineLineMemIdx = cloneOf(tempOutp.lineMemIdx)
      //    ////val rTempCombineLineMemIdx = RegNext(nextTempCombineLineMemIdx)
      //    ////  .init(0x0)
      //    ////--------
      //    ////when (intnlChangingRowRe) {
      //    ////  tempCombineLineMemIdx := 0
      //    ////} elsewhen (tempOutp.fire) {
      //    ////  tempCombineLineMemIdx := tempCombineLineMemIdx + 1
      //    ////}

      //    ////when (!(tempOutp.valid && !tempOutp.ready)) {
      //    ////  tempCombineLineMemIdx := tempInp.cnt(
      //    ////    tempCombineLineMemIdx.bitsRange
      //    ////  )
      //    ////} otherwise {
      //    ////  tempCombineLineMemIdx := RegNext(tempCombineLineMemIdx)
      //    ////}

      //    ////when (
      //    ////  tempInp.fire
      //    ////  &&
      //    ////  tempInp.bakCntWillBeDone()
      //    ////) {
      //    ////  //tempCombineLineMemIdx := 0
      //    ////  rTempCombineLineMemIdx := 0
      //    ////} elsewhen (tempOutp.fire) {
      //    ////  rTempCombineLineMemIdx := rTempCombineLineMemIdx + 1
      //    ////}
      //    ////--------
      //    ////when (tempOutp.fire) {
      //    ////  when (tempInp.bakCntWillBeDone()) {
      //    ////    nextTempCombineLineMemIdx := 0x0
      //    ////  } otherwise {
      //    ////    nextTempCombineLineMemIdx := rTempCombineLineMemIdx + 1
      //    ////  }
      //    ////}
      //    ////--------

      //    ////when (
      //    ////  //tempOutp.fire
      //    ////  //tempOutp.ready
      //    ////  //tempOutp.ready
      //    ////  !(tempOutp.valid && !tempOutp.ready)
      //    ////) {
      //    ////  tempCombineLineMemIdx := (
      //    ////    tempOutp.cnt(
      //    ////      //log2Up(
      //    ////      //  //params.oneLineMemSize
      //    ////      //  params.oneLineMemSize
      //    ////      //) - 1
      //    ////      //downto 0
      //    ////      tempCombineLineMemIdx.bitsRange
      //    ////    )
      //    ////  )
      //    ////} otherwise {
      //    ////  tempCombineLineMemIdx := RegNext(tempCombineLineMemIdx)
      //    ////}
      //    //def bgSubLineMemArrIdx = params.getBgSubLineMemArrIdx(
      //    //  addr=(
      //    //    //rTempCombineLineMemIdx
      //    //    tempCombineLineMemIdx
      //    //  )
      //    //)
      //    //val dbgTestCombinePipe1_bgSubLineMemArrIdx = UInt(
      //    //  bgSubLineMemArrIdx.getWidth bits
      //    //)
      //    //  .setName("dbgTestCombinePipe1_bgSubLineMemArrIdx")
      //    //dbgTestCombinePipe1_bgSubLineMemArrIdx := (
      //    //  bgSubLineMemArrIdx
      //    //)

      //    //def objSubLineMemArrIdx = (
      //    //  params.getObjSubLineMemArrIdx(
      //    //    addr=(
      //    //      //rTempCombineLineMemIdx
      //    //      tempCombineLineMemIdx
      //    //    )
      //    //  )
      //    //)
      //    //val dbgTestCombinePipe1_objSubLineMemArrIdx = UInt(
      //    //  objSubLineMemArrIdx.getWidth bits
      //    //)
      //    //  .setName("dbgTestCombinePipe1_objSubLineMemArrIdx")
      //    //dbgTestCombinePipe1_objSubLineMemArrIdx := (
      //    //  objSubLineMemArrIdx
      //    //)

      //    //when (clockDomain.isResetActive) {
      //    //  tempOutp.stage2 := tempOutp.stage2.getZero
      //    //  ////combinePipeStage1Busy := False
      //    //  ////lineFifo.io.pop.ready := True
      //    //  ////lineFifo.io.pop.ready := False
      //    //  ////objLineFifo.io.pop.ready := False
      //    //  //tempOutp.lineMemIdx := 0
      //    //  for (jdx <- 0 until 1 << rCombineFullLineMemArrIdx.getWidth) {
      //    //    rdBgSubLineMemArr(jdx).addrVec(
      //    //      RdBgSubLineMemArrInfo.combineIdx
      //    //    ) := 0x0
      //    //    rdObjSubLineMemArr(jdx).addrVec(
      //    //      RdObjSubLineMemArrInfo.combineIdx
      //    //    ) := 0x0
      //    //  }
      //    //} otherwise {
      //    //  //tempOutp.lineMemIdx := (
      //    //  //  //rTempCombineLineMemIdx
      //    //  //  tempCombineLineMemIdx
      //    //  //  //tempOutp.cnt(
      //    //  //  //  tempOutp.lineMemIdx.bitsRange
      //    //  //  //)
      //    //  //)
      //    //  //tempOutp.stage1.lineMemIdx := 0
      //    //  //when (
      //    //  //  //tempOutp.fire
      //    //  //  //tempOutp.valid
      //    //  //  tempInp.fire
      //    //  //) {
      //    //  //--------
      //    //  // BEGIN: new code, with `readSync`
      //    //  switch (rCombineFullLineMemArrIdx) {
      //    //    for (jdx <- 0 until rdBgSubLineMemArr.size) {
      //    //      is (jdx) {
      //    //        tempOutp.stage2.rdBg := (
      //    //          combinePipeOut1BgVec(jdx).stage2.rdBg
      //    //        )
      //    //        tempOutp.stage2.rdObj := (
      //    //          combinePipeOut1ObjVec(jdx).stage2.rdObj
      //    //        )
      //    //      }
      //    //    }
      //    //  }
      //    //  // END: new code, with `readSync`
      //    //  //for (jdx <- 0 until 1 << rCombineFullLineMemArrIdx.getWidth) {
      //    //  ////haltCombinePipe2FifoPush := 
      //    //  //  //when (!haltCombinePipe2) {
      //    //  //  //  haltCombinePipe2FifoPush 
      //    //  //  //}

      //    //  //  //when (
      //    //  //  //  //True
      //    //  //  //  //tempOutp.fire
      //    //  //  //  //tempInp.fire
      //    //  //  //  //!haltCombinePipe2
      //    //  //  //  //&& combinePipe2Fifo.push.fire
      //    //  //  //  //tempOutp.ready
      //    //  //  //  //tempInp.fire
      //    //  //  //  tempOutp.fire
      //    //  //  //) {
      //    //  //    //--------
      //    //  //    // We no longer need the `switch` statement here since we
      //    //  //    // are just reading
      //    //  //    //rdBgSubLineMemArr(jdx).addrVec(
      //    //  //    //  RdBgSubLineMemArrInfo.combineIdx
      //    //  //    //) := bgSubLineMemArrIdx
      //    //  //    //rdObjSubLineMemArr(jdx).addrVec(
      //    //  //    //  RdObjSubLineMemArrInfo.combineIdx
      //    //  //    //) := objSubLineMemArrIdx
      //    //  //    //--------
      //    //  //  //} otherwise {
      //    //  //  //  rdBgSubLineMemArr(jdx).addrVec(
      //    //  //  //    RdBgSubLineMemArrInfo.combineIdx
      //    //  //  //  ) := RegNext(
      //    //  //  //    rdBgSubLineMemArr(jdx).addrVec(
      //    //  //  //      RdBgSubLineMemArrInfo.combineIdx
      //    //  //  //    )
      //    //  //  //  )
      //    //  //  //  rdObjSubLineMemArr(jdx).addrVec(
      //    //  //  //    RdObjSubLineMemArrInfo.combineIdx
      //    //  //  //  ) := RegNext(
      //    //  //  //    rdObjSubLineMemArr(jdx).addrVec(
      //    //  //  //      RdObjSubLineMemArrInfo.combineIdx
      //    //  //  //    )
      //    //  //  //  )
      //    //  //  //}
      //    //  //}
      //    //  //--------
      //    //}
      //    ////tempOut.stage0 := tempInp.stage0
      //    ////stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
      //    //stageData.pipeOut(idx).stage1 := stageData.pipeIn(idx).stage1

      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage2 := tempOutp.stage2.getZero
      //    } otherwise {
      //      if (idx != 1) {
      //        tempOutp.stage2 := tempInp.stage2
      //      } 
      //      //else {
      //      //  tempOutp.stage2 := (
      //      //    combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage2
      //      //  )
      //      //}
      //    }
      //  },
      //)
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 3
        val (pipeIn, pipeOut) = initCombinePostJoinPipeOut(idx)
        def tempInp = pipeIn
        def tempOutp = pipeOut

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

        def objAffineSubLineMemArrElemIdx = (
          params.getObjAffineSubLineMemArrElemIdx(
            addr=(
              myLineMemIdx
              //tempInp.lineMemIdx
              //tempCombineLineMemIdx
            )
          )
        )
        val dbgTestCombinePipe2_objAffineSubLineMemArrElemIdx = UInt(
          objAffineSubLineMemArrElemIdx.getWidth bits
        )
          .setName("dbgTestCombinePipe2_objAffineSubLineMemArrElemIdx")
        dbgTestCombinePipe2_objAffineSubLineMemArrElemIdx := (
          objAffineSubLineMemArrElemIdx
        )

        when (clockDomain.isResetActive) {
          //tempOutp.stage1 := tempOutp.stage1.getZero
          ////combinePipeStage1Busy := False
          ////lineFifo.io.pop.ready := True
          ////lineFifo.io.pop.ready := False
          ////objLineFifo.io.pop.ready := False
          tempOutp.stage3 := tempOutp.stage3.getZero
        } otherwise {
          switch (combineLineMemArrIdx) {
            for (jdx <- 0 until 1 << combineLineMemArrIdx.getWidth) {
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
                    //tempInp.stage2.rdObjAffine
                  )
                  tempOutp.stage3.ext.bgRdSubLineMemEntry := (
                    tempRdBg(bgSubLineMemArrElemIdx)
                  )
                  tempOutp.stage3.ext.objRdSubLineMemEntry := (
                    tempRdObj(
                      objSubLineMemArrElemIdx
                      //objAffineSubLineMemArrElemIdx
                    )
                  )
                  if (!noAffineObjs) {
                    def tempRdObjAffine = (
                      tempInp.stage2.rdObjAffine
                    )
                    tempOutp.stage3.ext.objAffineRdSubLineMemEntry := (
                      tempRdObjAffine(
                        //objSubLineMemArrElemIdx
                        objAffineSubLineMemArrElemIdx
                      )
                    )
                  }
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
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(
      //    3
      //    //4
      //  )
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    //val tempCombineLineMemIdx = createTempCnt(
      //    //  tempOutp=tempOutp,
      //    //  pipeStageIdx=idx
      //    //)

      //    //val tempCombineLineMemIdxArr = new ArrayBuffer[UInt]()
      //    //val combinePipe2_tempCombineLineMemIdx = KeepAttribute(
      //    //  Reg(
      //    //    UInt(
      //    //      log2Up(
      //    //        //params.oneLineMemSize
      //    //        params.oneLineMemSize
      //    //      ) bits
      //    //    )
      //    //  )
      //    //    .init(0x0)
      //    //    .setName("dbgTestCombinePipe2_tempCombineLineMemIdx")
      //    //)
      //    //val tempCombineLineMemIdx = UInt(
      //    //  log2Up(params.oneLineMemSize) bits
      //    //)
      //    //  .setName("dbgTestCombinePipe2_tempCombineLineMemIdx")
      //    //tempCombineLineMemIdx := (
      //    //  tempInp.cnt(tempCombineLineMemIdx.bitsRange)
      //    //)
      //    //fifo.io.push.valid := tempInp.valid
      //    //val tempCombineLineMemIdx = KeepAttribute(
      //    //  //Reg(
      //    //    UInt(
      //    //      log2Up(
      //    //        //params.oneLineMemSize
      //    //        params.oneLineMemSize
      //    //      ) bits
      //    //    )
      //    //  //)
      //    //    //.init(0x0)
      //    //    .setName("dbgTestCombinePipe2_tempCombineLineMemIdx")
      //    //    //.setName("tempCombineLineMemIdx")
      //    //)
      //    ////when (!(tempOutp.valid && !tempOutp.ready)) {
      //    //  tempCombineLineMemIdx := tempInp.cnt(
      //    //    tempCombineLineMemIdx.bitsRange
      //    //  )
      //    ////} otherwise {
      //    ////  tempCombineLineMemIdx := RegNext(tempCombineLineMemIdx)
      //    ////}

      //    //when (intnlChangingRowRe) {
      //    //  tempCombineLineMemIdx := 0
      //    //} elsewhen (tempOutp.fire) {
      //    //  tempCombineLineMemIdx := tempCombineLineMemIdx + 1
      //    //}

      //    //combinePipe2_tempCombineLineMemIdx := (
      //    //  //tempInp.cnt(
      //    //  //  log2Up(
      //    //  //    //params.oneLineMemSize
      //    //  //    params.oneLineMemSize
      //    //  //  ) - 1
      //    //  //  downto 0
      //    //  //)
      //    //  //tempInp.stage1.lineMemIdx
      //    //  tempCombineLineMemIdx
      //    //)
      //    //val combinePipe2Fifo = AsyncReadFifo
      //    //when (tempInp.fire) {
      //    //}
      //    //val combinePipe2_tempCombineLineMemIdx = (
      //    //  //RegNext(tempCombineLineMemIdx)
      //    //  //  .init(0x0)
      //    //  //  .setName("dbgTestCombinePipe2_tempCombineLineMemIdx")
      //    //  UInt(tempCombineLineMemIdx.getWidth bits)
      //    //    .setName("dbgTestCombinePipe2_tempCombineLineMemIdx")
      //    //)
      //    //when (tempInp.fire) {
      //    //  combinePipe2_tempCombineLineMemIdx := (
      //    //    tempInp.cnt(
      //    //      log2Up(
      //    //        //params.oneLineMemSize
      //    //        params.oneLineMemSize
      //    //      ) - 1
      //    //      downto 0
      //    //    )
      //    //    //tempCombineLineMemIdx
      //    //  )
      //    //} otherwise {
      //    //  combinePipe2_tempCombineLineMemIdx := RegNext(
      //    //    combinePipe2_tempCombineLineMemIdx
      //    //  )
      //    //}
      //    val rTempCnt = Reg(UInt(tempInp.cnt.getWidth bits)) init(0x0)
      //    def myLineMemIdx = (
      //      //combinePipeIn2PxReadFifo.io.pop.lineMemIdx
      //      tempInp.lineMemIdx
      //      //tempInp.cnt
      //    )
      //    def bgSubLineMemArrIdx = params.getBgSubLineMemArrIdx(
      //      addr=(
      //        myLineMemIdx
      //        //tempInp.lineMemIdx
      //        //tempCombineLineMemIdx
      //      )
      //    )
      //    val dbgTestCombinePipe2_bgSubLineMemArrIdx = UInt(
      //      bgSubLineMemArrIdx.getWidth bits
      //    )
      //      .setName("dbgTestCombinePipe2_bgSubLineMemArrIdx")
      //    dbgTestCombinePipe2_bgSubLineMemArrIdx := (
      //      bgSubLineMemArrIdx
      //    )

      //    def bgSubLineMemArrElemIdx = params.getBgSubLineMemArrElemIdx(
      //      addr=(
      //        myLineMemIdx
      //        //tempInp.lineMemIdx
      //        //tempCombineLineMemIdx
      //      )
      //    )
      //    val dbgTestCombinePipe2_bgSubLineMemArrElemIdx = UInt(
      //      bgSubLineMemArrElemIdx.getWidth bits
      //    )
      //      .setName("dbgTestCombinePipe2_bgSubLineMemArrElemIdx")
      //    dbgTestCombinePipe2_bgSubLineMemArrElemIdx := (
      //      bgSubLineMemArrElemIdx
      //    )

      //    def objSubLineMemArrIdx = (
      //      params.getObjSubLineMemArrIdx(
      //        addr=(
      //          myLineMemIdx
      //          //tempInp.lineMemIdx
      //          //tempCombineLineMemIdx
      //        )
      //      )
      //    )
      //    val dbgTestCombinePipe2_objSubLineMemArrIdx = UInt(
      //      objSubLineMemArrIdx.getWidth bits
      //    )
      //      .setName("dbgTestCombinePipe2_objSubLineMemArrIdx")
      //    dbgTestCombinePipe2_objSubLineMemArrIdx := (
      //      objSubLineMemArrIdx
      //    )

      //    def objSubLineMemArrElemIdx = (
      //      params.getObjSubLineMemArrElemIdx(
      //        addr=(
      //          myLineMemIdx
      //          //tempInp.lineMemIdx
      //          //tempCombineLineMemIdx
      //        )
      //      )
      //    )
      //    val dbgTestCombinePipe2_objSubLineMemArrElemIdx = UInt(
      //      objSubLineMemArrElemIdx.getWidth bits
      //    )
      //      .setName("dbgTestCombinePipe2_objSubLineMemArrElemIdx")
      //    dbgTestCombinePipe2_objSubLineMemArrElemIdx := (
      //      objSubLineMemArrElemIdx
      //    )

      //    def objAffineSubLineMemArrElemIdx = (
      //      params.getObjAffineSubLineMemArrElemIdx(
      //        addr=(
      //          myLineMemIdx
      //          //tempInp.lineMemIdx
      //          //tempCombineLineMemIdx
      //        )
      //      )
      //    )
      //    val dbgTestCombinePipe2_objAffineSubLineMemArrElemIdx = UInt(
      //      objAffineSubLineMemArrElemIdx.getWidth bits
      //    )
      //      .setName("dbgTestCombinePipe2_objAffineSubLineMemArrElemIdx")
      //    dbgTestCombinePipe2_objAffineSubLineMemArrElemIdx := (
      //      objAffineSubLineMemArrElemIdx
      //    )

      //    when (clockDomain.isResetActive) {
      //      //tempOutp.stage1 := tempOutp.stage1.getZero
      //      ////combinePipeStage1Busy := False
      //      ////lineFifo.io.pop.ready := True
      //      ////lineFifo.io.pop.ready := False
      //      ////objLineFifo.io.pop.ready := False
      //      tempOutp.stage3 := tempOutp.stage3.getZero
      //    } otherwise {
      //      switch (rCombineFullLineMemArrIdx) {
      //        for (jdx <- 0 until 1 << rCombineFullLineMemArrIdx.getWidth) {
      //          is (jdx) {
      //            //--------
      //            //case class BufFifoElem() extends Bundle {
      //            //  val bgRd = Vec.fill(params.bgTileSize2d.x)(
      //            //    BgSubLineMemEntry()
      //            //  )
      //            //  val objRd = Vec.fill(params.objTileSize2d.x)(
      //            //    ObjSubLineMemEntry()
      //            //  )
      //            //  val lineMemIdx = cloneOf(tempInp.lineMemIdx)
      //            //}
      //            //def bufFifoDepth = (
      //            //  8
      //            //)
      //            //def bufFifoPushStallGeAmountCanPop = (
      //            //  4
      //            //)
      //            //val bufFifo = AsyncReadFifo(
      //            //  dataType=BufFifoElem(),
      //            //  depth=bufFifoDepth,
      //            //)
      //            //bufFifo.io.push.valid := tempInp.valid
      //            //haltCombinePipeIn2 := (
      //            //  bufFifo.io.misc.amountCanPop
      //            //  >= bufFifoPushStallGeAmountCanPop
      //            //)
      //            //bufFifo.io.pop.ready := True
      //            // BEGIN: new code, for reading synchronously
      //            //when (
      //            //  //tempOutp.fire
      //            //  //tempInp.fire
      //            //  //tempOutp.fire
      //            //  combinePipeIn2PxReadFifo.io.pop.fire
      //            //) {
      //              //--------
      //              def tempRdBg = (
      //                //rdBgSubLineMemArr(jdx).dataVec(
      //                //  RdBgSubLineMemArrInfo.combineIdx
      //                //)
      //                ////combinePipeIn2PxReadFifo.io.pop.rdBg
      //                tempInp.stage2.rdBg
      //              )
      //              def tempRdObj = (
      //                //rdObjSubLineMemArr(jdx).dataVec(
      //                //  RdObjSubLineMemArrInfo.combineIdx
      //                //)
      //                ////combinePipeIn2PxReadFifo.io.pop.rdObj
      //                //combinePipeIn1ObjMemReadSyncArr
      //                tempInp.stage2.rdObj
      //                //tempInp.stage2.rdObjAffine
      //              )
      //              tempOutp.stage3.ext.bgRdSubLineMemEntry := (
      //                tempRdBg(bgSubLineMemArrElemIdx)
      //              )
      //              tempOutp.stage3.ext.objRdSubLineMemEntry := (
      //                tempRdObj(
      //                  objSubLineMemArrElemIdx
      //                  //objAffineSubLineMemArrElemIdx
      //                )
      //              )
      //              if (!noAffineObjs) {
      //                def tempRdObjAffine = (
      //                  tempInp.stage2.rdObjAffine
      //                )
      //                tempOutp.stage3.ext.objAffineRdSubLineMemEntry := (
      //                  tempRdObjAffine(
      //                    //objSubLineMemArrElemIdx
      //                    objAffineSubLineMemArrElemIdx
      //                  )
      //                )
      //              }
      //              //--------
      //            //} otherwise {
      //            //  tempOutp.stage2.ext.bgRdSubLineMemEntry := (
      //            //    //tempOutp.stage2.ext.bgRdSubLineMemEntry.getZero
      //            //    RegNext(tempOutp.stage2.ext.bgRdSubLineMemEntry)
      //            //  )
      //            //  tempOutp.stage2.ext.objRdSubLineMemEntry := (
      //            //    //tempOutp.stage2.ext.objRdSubLineMemEntry.getZero
      //            //    RegNext(tempOutp.stage2.ext.objRdSubLineMemEntry)
      //            //  )
      //            //}
      //            // END: new code, for reading synchronously
      //            //--------
      //            // BEGIN: old-style code, for reading asynchronously
      //            //tempOutp.stage3.ext.bgRdSubLineMemEntry := (
      //            //  bgSubLineMemArr(jdx).readAsync(
      //            //    bgSubLineMemArrIdx
      //            //  )(bgSubLineMemArrElemIdx)
      //            //)
      //            //tempOutp.stage3.ext.objRdSubLineMemEntry := (
      //            //  objSubLineMemArr(jdx).readAsync(
      //            //    objSubLineMemArrIdx
      //            //  )(objSubLineMemArrElemIdx)
      //            //)
      //            // END: old-style code, for reading asynchronously
      //            //--------
      //          }
      //        }
      //      }
      //    }
      //    //tempOut.stage0 := tempInp.stage0
      //    //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
      //    //stageData.pipeOut(idx).stage1 := stageData.pipeIn(idx).stage1

      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage3 := tempOutp.stage3.getZero
      //    } otherwise {
      //      //tempOutp.stage3 := tempInp.stage3
      //      if (idx != 1) {
      //        tempOutp.stage3 := tempInp.stage3
      //      } else {
      //        tempOutp.stage3 := (
      //          combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage3
      //        )
      //      }
      //    }
      //  },
      //)
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 4
        val (pipeIn, pipeOut) = initCombinePostJoinPipeOut(idx)
        def tempInp = pipeIn
        def tempOutp = pipeOut

        def objRdSubLineMemEntry = (
          tempInp.stage3.ext.objRdSubLineMemEntry
        )
        def objAffineRdSubLineMemEntry = (
          tempInp.stage3.ext.objAffineRdSubLineMemEntry
        )

        if (noAffineObjs) {
          tempOutp.objPickSubLineMemEntry := objRdSubLineMemEntry
        } else {
          switch (Cat(
            objRdSubLineMemEntry.col.a,
            objAffineRdSubLineMemEntry.col.a,
            objRdSubLineMemEntry.prio < objAffineRdSubLineMemEntry.prio
          )) {
            //is (B"000") {
            //  tempOutp.objPickSubLineMemEntry := (
            //    objAffineRdSubLineMemEntry
            //  )
            //}
            is (M"10-") {
              tempOutp.objPickSubLineMemEntry := (
                objRdSubLineMemEntry
              )
            }
            is (M"01-") {
              tempOutp.objPickSubLineMemEntry := (
                objAffineRdSubLineMemEntry
              )
            }
            is (M"110") {
              tempOutp.objPickSubLineMemEntry := (
                objAffineRdSubLineMemEntry
              )
            }
            is (M"111") {
              tempOutp.objPickSubLineMemEntry := (
                objRdSubLineMemEntry
              )
            }
            default {
              tempOutp.objPickSubLineMemEntry := (
                objAffineRdSubLineMemEntry
              )
            }
          }
        }
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(4)
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    def tempInp = stageData.pipeIn(idx)
      //    def tempOutp = stageData.pipeOut(idx)

      //    def objRdSubLineMemEntry = (
      //      tempInp.stage3.ext.objRdSubLineMemEntry
      //    )
      //    def objAffineRdSubLineMemEntry = (
      //      tempInp.stage3.ext.objAffineRdSubLineMemEntry
      //    )

      //    if (noAffineObjs) {
      //      tempOutp.objPickSubLineMemEntry := objRdSubLineMemEntry
      //    } else {
      //      switch (Cat(
      //        objRdSubLineMemEntry.col.a,
      //        objAffineRdSubLineMemEntry.col.a,
      //        objRdSubLineMemEntry.prio < objAffineRdSubLineMemEntry.prio
      //      )) {
      //        //is (B"000") {
      //        //  tempOutp.objPickSubLineMemEntry := (
      //        //    objAffineRdSubLineMemEntry
      //        //  )
      //        //}
      //        is (M"10-") {
      //          tempOutp.objPickSubLineMemEntry := (
      //            objRdSubLineMemEntry
      //          )
      //        }
      //        is (M"01-") {
      //          tempOutp.objPickSubLineMemEntry := (
      //            objAffineRdSubLineMemEntry
      //          )
      //        }
      //        is (M"110") {
      //          tempOutp.objPickSubLineMemEntry := (
      //            objAffineRdSubLineMemEntry
      //          )
      //        }
      //        is (M"111") {
      //          tempOutp.objPickSubLineMemEntry := (
      //            objRdSubLineMemEntry
      //          )
      //        }
      //        default {
      //          tempOutp.objPickSubLineMemEntry := (
      //            objAffineRdSubLineMemEntry
      //          )
      //        }
      //      }
      //    }
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    def tempInp = stageData.pipeIn(idx)
      //    def tempOutp = stageData.pipeOut(idx)

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage4 := tempOutp.stage4.getZero
      //    } otherwise {
      //      if (idx != 1) {
      //        tempOutp.stage4 := tempInp.stage4
      //      } else {
      //        tempOutp.stage4 := (
      //          combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage4
      //        )
      //      }
      //    }
      //  },
      //)
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 5
        val (pipeIn, pipeOut) = initCombinePostJoinPipeOut(idx)
        def tempInp = pipeIn
        def tempOutp = pipeOut

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
            //tempInp.valid
            cCombineArr(idx).up.isValid
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

              //inpExt.objRdSubLineMemEntry.prio
              //<= inpExt.bgRdSubLineMemEntry.prio
              tempInp.objPickSubLineMemEntry.prio
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
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(
      //    //2
      //    //3
      //    5
      //    //wrBgObjPipeNumStages + 2
      //  )
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    def inpExt = (
      //      //tempInp.stage1.ext
      //      tempInp.stage3.ext
      //    )


      //    //when (
      //    //  tempInp.bgRdLineMemEntry.col.a
      //    //  && tempInp.objRdLineMemEntry.col.a
      //    //) {
      //    //  when (
      //    //    // sprites take priority upon a tie, hence `<=`
      //    //    tempInp.objRdLineMemEntry.prio
      //    //    <= tempInp.bgRdLineMemEntry.prio
      //    //  ) {
      //    //    tempOutp.col := tempInp.objRdLineMemEntry.col.rgb
      //    //  } otherwise {
      //    //    tempOutp.col := tempInp.bgRdLineMemEntry.col.rgb
      //    //  }
      //    //} elsewhen (
      //    //  tempInp.bgRdLineMemEntry.col.a
      //    //) {
      //    //  tempOutp.col := tempInp.bgRdLineMemEntry.col.rgb
      //    //} elsewhen (
      //    //  tempInp.objRdLineMemEntry.col.a
      //    //) {
      //    //  tempOutp.col := tempInp.objRdLineMemEntry.col.rgb
      //    //} otherwise {
      //    //  tempOutp.col := tempOutp.col.getZero
      //    //}

      //    // BEGIN: Debug comment this out
      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage5 := tempOutp.stage5.getZero
      //    } otherwise {
      //      when (
      //        tempInp.valid
      //      ) {
      //        //tempOutp.stage5.ext := (
      //        //  rCombinePipeOut1Ext
      //        //)
      //        tempOutp.objHiPrio := (
      //          // sprites take priority upon a tie, hence `<=`
      //          //tempInp.objRdLineMemEntry.prio(
      //          //  tempInp.bgRdLineMemEntry.prio.bitsRange
      //          //) <= tempInp.bgRdLineMemEntry.prio

      //          //tempInp.objRdLineMemEntry.prio
      //          //<= tempInp.bgRdLineMemEntry.prio

      //          //inpExt.objRdSubLineMemEntry.prio
      //          //<= inpExt.bgRdSubLineMemEntry.prio
      //          tempInp.objPickSubLineMemEntry.prio
      //          <= inpExt.bgRdSubLineMemEntry.prio

      //          //rCombinePipeOut1Ext.objRdLineMemEntry.prio
      //          //<= rCombinePipeOut1Ext.bgRdLineMemEntry.prio
      //        )
      //      } otherwise {
      //        tempOutp.stage5 := RegNext(tempOutp.stage5)
      //      }
      //    }
      //    // END: Debug comment this out

      //    // BEGIN: Debug test no sprites
      //    //tempOutp.objHiPrio := False
      //    // END: Debug test no sprites

      //    //tempOut.stage0 := tempInp.stage0
      //    //stageData.pipeOut(idx).stage0 := stageData.pipeIn(idx).stage0
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
      //    //stageData.pipeOut(idx).stage2 := stageData.pipeIn(idx).stage2
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage5 := tempOutp.stage5.getZero
      //    } otherwise {
      //      //tempOutp.stage5 := tempInp.stage5
      //      if (idx != 1) {
      //        tempOutp.stage5 := tempInp.stage5
      //      } else {
      //        tempOutp.stage5 := (
      //          combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage5
      //        )
      //      }
      //    }
      //  },
      //)
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 6
        val (pipeIn, pipeOut) = initCombinePostJoinPipeOut(idx)
        def tempInp = pipeIn
        def tempOutp = pipeOut

        def inpExt = (
          //tempInp.stage1.ext
          tempInp.stage3.ext
        )

        when (clockDomain.isResetActive) {
          tempOutp.stage6 := tempOutp.stage6.getZero
        } otherwise {
          // BEGIN: Debug comment this out; later
          if (!noColorMath) {
            tempOutp.colorMathCol := (
              inpExt.bgRdSubLineMemEntry.colorMathCol
            )
            tempOutp.colorMathCol.allowOverride
          }
          switch (Cat(
            inpExt.bgRdSubLineMemEntry.col.a,
            //inpExt.objRdSubLineMemEntry.col.a,
            tempInp.objPickSubLineMemEntry.col.a,
            tempInp.objHiPrio
          )) {
            is (B"111") {
              //tempOutp.col := inpExt.objRdSubLineMemEntry.col
              tempOutp.col := tempInp.objPickSubLineMemEntry.col
              if (!noColorMath) {
                tempOutp.colorMathInfo := (
                  //inpExt.objRdSubLineMemEntry.colorMathInfo
                  tempInp.objPickSubLineMemEntry.colorMathInfo
                )
              }
            }
            is (B"110") {
              tempOutp.col := inpExt.bgRdSubLineMemEntry.col
              if (!noColorMath) {
                tempOutp.colorMathInfo := (
                  inpExt.bgRdSubLineMemEntry.colorMathInfo
                )
              }
            }
            is (M"10-") {
              tempOutp.col := inpExt.bgRdSubLineMemEntry.col
              if (!noColorMath) {
                tempOutp.colorMathInfo := (
                  inpExt.bgRdSubLineMemEntry.colorMathInfo
                )
              }
            }
            is (M"01-") {
              //tempOutp.col := inpExt.objRdSubLineMemEntry.col
              tempOutp.col := tempInp.objPickSubLineMemEntry.col
              if (!noColorMath) {
                tempOutp.colorMathInfo := (
                  //inpExt.objRdSubLineMemEntry.colorMathInfo
                  tempInp.objPickSubLineMemEntry.colorMathInfo
                )
              }
            }
            //is (M"00-")
            default {
              tempOutp.col := tempOutp.col.getZero
              if (!noColorMath) {
                tempOutp.colorMathInfo := (
                  tempOutp.colorMathInfo.getZero
                )
                tempOutp.colorMathCol := (
                  tempOutp.colorMathCol.getZero
                )
              }
            }
          }
          // END: Debug comment this out; later
          //tempOutp.col := inpExt.bgRdSubLineMemEntry.col.rgb
          //tempOutp.col := inpExt.objRdSubLineMemEntry.col.rgb
        }
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(
      //    //3
      //    //4
      //    6
      //    //wrBgObjPipeNumStages + 3
      //  )
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    def inpExt = (
      //      //tempInp.stage1.ext
      //      tempInp.stage3.ext
      //    )

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage6 := tempOutp.stage6.getZero
      //    } otherwise {
      //      // BEGIN: Debug comment this out; later
      //      if (!noColorMath) {
      //        tempOutp.colorMathCol := (
      //          inpExt.bgRdSubLineMemEntry.colorMathCol
      //        )
      //        tempOutp.colorMathCol.allowOverride
      //      }
      //      switch (Cat(
      //        inpExt.bgRdSubLineMemEntry.col.a,
      //        //inpExt.objRdSubLineMemEntry.col.a,
      //        tempInp.objPickSubLineMemEntry.col.a,
      //        tempInp.objHiPrio
      //      )) {
      //        is (B"111") {
      //          //tempOutp.col := inpExt.objRdSubLineMemEntry.col
      //          tempOutp.col := tempInp.objPickSubLineMemEntry.col
      //          if (!noColorMath) {
      //            tempOutp.colorMathInfo := (
      //              //inpExt.objRdSubLineMemEntry.colorMathInfo
      //              tempInp.objPickSubLineMemEntry.colorMathInfo
      //            )
      //          }
      //        }
      //        is (B"110") {
      //          tempOutp.col := inpExt.bgRdSubLineMemEntry.col
      //          if (!noColorMath) {
      //            tempOutp.colorMathInfo := (
      //              inpExt.bgRdSubLineMemEntry.colorMathInfo
      //            )
      //          }
      //        }
      //        is (M"10-") {
      //          tempOutp.col := inpExt.bgRdSubLineMemEntry.col
      //          if (!noColorMath) {
      //            tempOutp.colorMathInfo := (
      //              inpExt.bgRdSubLineMemEntry.colorMathInfo
      //            )
      //          }
      //        }
      //        is (M"01-") {
      //          //tempOutp.col := inpExt.objRdSubLineMemEntry.col
      //          tempOutp.col := tempInp.objPickSubLineMemEntry.col
      //          if (!noColorMath) {
      //            tempOutp.colorMathInfo := (
      //              //inpExt.objRdSubLineMemEntry.colorMathInfo
      //              tempInp.objPickSubLineMemEntry.colorMathInfo
      //            )
      //          }
      //        }
      //        //is (M"00-")
      //        default {
      //          tempOutp.col := tempOutp.col.getZero
      //          if (!noColorMath) {
      //            tempOutp.colorMathInfo := (
      //              tempOutp.colorMathInfo.getZero
      //            )
      //            tempOutp.colorMathCol := (
      //              tempOutp.colorMathCol.getZero
      //            )
      //          }
      //        }
      //      }
      //      // END: Debug comment this out; later
      //      //tempOutp.col := inpExt.bgRdSubLineMemEntry.col.rgb
      //      //tempOutp.col := inpExt.objRdSubLineMemEntry.col.rgb
      //    }
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).stage3 := stageData.pipeIn(idx).stage3
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage6 := tempOutp.stage6.getZero
      //    } otherwise {
      //      //tempOutp.stage6 := tempInp.stage6
      //      if (idx != 1) {
      //        tempOutp.stage6 := tempInp.stage6
      //      } else {
      //        tempOutp.stage6 := (
      //          combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage6
      //        )
      //      }
      //    }
      //  },
      //)
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 7
        val (pipeIn, pipeOut) = initCombinePostJoinPipeOut(idx)
        def tempInp = pipeIn
        def tempOutp = pipeOut

        when (clockDomain.isResetActive) {
          tempOutp.stage7 := tempOutp.stage7.getZero
        } otherwise {
          //if (inSim) {
            tempOutp.stage7.combineWrLineMemEntry.addr := (
              //tempInp.cnt
              //tempInp.stage1.lineMemIdx
              tempInp.cnt
              (
                tempOutp.stage7.combineWrLineMemEntry.addr.bitsRange
              )
            )
          //}
          tempOutp.stage7.combineWrLineMemEntry.col := tempInp.col

          //// not really necessary, but doing it anyway
          //tempOutp.stage7.combineWrLineMemEntry.col.a := True
          //tempOutp.stage7.combineWrLineMemEntry.col.a := 

          tempOutp.stage7.combineWrLineMemEntry.prio := 0x0
          if (!noColorMath) {
            tempOutp.stage7.combineWrLineMemEntry.colorMathInfo := (
              tempInp.colorMathInfo
            )
            tempOutp.stage7.combineWrLineMemEntry.colorMathCol := (
              tempInp.colorMathCol
            )
          }
        }
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(
      //    //4
      //    //5
      //    7
      //    //wrBgObjPipeNumStages + 4
      //  )
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage7 := tempOutp.stage7.getZero
      //    } otherwise {
      //      //if (inSim) {
      //        tempOutp.stage7.combineWrLineMemEntry.addr := (
      //          //tempInp.cnt
      //          //tempInp.stage1.lineMemIdx
      //          tempInp.cnt
      //          (
      //            tempOutp.stage7.combineWrLineMemEntry.addr.bitsRange
      //          )
      //        )
      //      //}
      //      tempOutp.stage7.combineWrLineMemEntry.col := tempInp.col

      //      //// not really necessary, but doing it anyway
      //      //tempOutp.stage7.combineWrLineMemEntry.col.a := True
      //      //tempOutp.stage7.combineWrLineMemEntry.col.a := 

      //      tempOutp.stage7.combineWrLineMemEntry.prio := 0x0
      //      if (!noColorMath) {
      //        tempOutp.stage7.combineWrLineMemEntry.colorMathInfo := (
      //          tempInp.colorMathInfo
      //        )
      //        tempOutp.stage7.combineWrLineMemEntry.colorMathCol := (
      //          tempInp.colorMathCol
      //        )
      //      }
      //    }
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
      //    //stageData.pipeOut(idx).stage4 := stageData.pipeIn(idx).stage4
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage7 := tempOutp.stage7.getZero
      //    } otherwise {
      //      //tempOutp.stage7 := tempInp.stage7
      //      if (idx != 1) {
      //        tempOutp.stage7 := tempInp.stage7
      //      } else {
      //        tempOutp.stage7 := (
      //          combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage7
      //        )
      //      }
      //    }
      //  },
      //)
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 8
        //val tempInp = stageData.pipeIn(idx)
        //val tempOutp = stageData.pipeOut(idx)
        val (pipeIn, pipeOut) = initCombinePostJoinPipeOut(idx)
        def tempInp = pipeIn
        def tempOutp = pipeOut

        when (clockDomain.isResetActive) {
          tempOutp.stage8 := tempOutp.stage8.getZero
        } otherwise {
          ////if (inSim) {
          //  tempOutp.stage8.combineWrLineMemEntry.addr := (
          //    //tempInp.cnt
          //    //tempInp.stage1.lineMemIdx
          //    tempInp.cnt
          //    (
          //      tempOutp.stage8.combineWrLineMemEntry.addr.bitsRange
          //    )
          //  )
          ////}
          //tempOutp.stage8.combineWrLineMemEntry.col := tempInp.col

          ////// not really necessary, but doing it anyway
          ////tempOutp.stage8.combineWrLineMemEntry.col.a := True
          ////tempOutp.stage8.combineWrLineMemEntry.col.a := 

          //tempOutp.stage8.combineWrLineMemEntry.prio := 0x0
          //tempOutp.stage8.combineWrLineMemEntry.colorMathInfo := (
          //  tempInp.colorMathInfo
          //)
          //tempOutp.stage8.combineWrLineMemEntry.colorMathCol := (
          //  tempInp.colorMathCol
          //)
          val tempCmathInfo = (!noColorMath) generate (
            tempInp.stage7.combineWrLineMemEntry.colorMathInfo
          )
          val tempCmathCol = (!noColorMath) generate (
            tempInp.stage7.combineWrLineMemEntry.colorMathCol
          )
          def inpWrLineMemEntry = tempInp.stage7.combineWrLineMemEntry
          def outpWrLineMemEntry = tempOutp.combineWrLineMemEntry
          outpWrLineMemEntry := outpWrLineMemEntry.getZero
          outpWrLineMemEntry.col.rgb.allowOverride
          def extraWidth = 4
          def tempRgbConfig = RgbConfig(
            rWidth=params.rgbConfig.rWidth + extraWidth,
            gWidth=params.rgbConfig.gWidth + extraWidth,
            bWidth=params.rgbConfig.bWidth + extraWidth,
          )
          val tempCol = Rgb(tempRgbConfig)
          tempCol := tempCol.getZero
          tempCol.allowOverride
          if (!noColorMath) {
            when (
              tempCmathInfo.doIt
            ) {
              switch (tempCmathInfo.kind.asBits) {
                is (Gpu2dColorMathKind.add.asBits) {
                  tempCol.r(params.rgbConfig.rWidth downto 0) := (
                    inpWrLineMemEntry.col.rgb.r.resized
                    + Cat(False, tempCmathCol.rgb.r).asUInt
                  )
                  tempCol.r(
                    tempCol.r.high downto params.rgbConfig.rWidth + 1
                  ) := 0
                  tempCol.g(params.rgbConfig.gWidth downto 0) := (
                    inpWrLineMemEntry.col.rgb.g.resized
                    + Cat(False, tempCmathCol.rgb.g).asUInt
                  )
                  tempCol.g(
                    tempCol.g.high downto params.rgbConfig.gWidth + 1
                  ) := 0
                  tempCol.b(params.rgbConfig.bWidth downto 0) := (
                    inpWrLineMemEntry.col.rgb.b.resized
                    + Cat(False, tempCmathCol.rgb.b).asUInt
                  )
                  tempCol.b(
                    tempCol.b.high downto params.rgbConfig.bWidth + 1
                  ) := 0
                }
                is (Gpu2dColorMathKind.sub.asBits) {
                  tempCol.r := (
                    inpWrLineMemEntry.col.rgb.r.resized
                    - Cat(
                      B(
                        tempCol.r.getWidth - tempCmathCol.rgb.r.getWidth
                          bits,
                        default -> tempCmathCol.rgb.r.msb),
                      tempCmathCol.rgb.r
                    ).asUInt
                  )
                  tempCol.g := (
                    inpWrLineMemEntry.col.rgb.g.resized
                    - Cat(
                      B(
                        tempCol.g.getWidth - tempCmathCol.rgb.g.getWidth
                          bits,
                        default -> tempCmathCol.rgb.g.msb),
                      tempCmathCol.rgb.g
                    ).asUInt
                  )
                  tempCol.b := (
                    inpWrLineMemEntry.col.rgb.b.resized
                    - Cat(
                      B(
                        tempCol.b.getWidth - tempCmathCol.rgb.b.getWidth
                          bits,
                        default -> tempCmathCol.rgb.b.msb),
                      tempCmathCol.rgb.b
                    ).asUInt
                  )
                }
                is (Gpu2dColorMathKind.avg.asBits) {
                  tempCol.r(params.rgbConfig.rWidth - 1 downto 0) := (
                    (
                      inpWrLineMemEntry.col.rgb.r.resized
                      + Cat(False, tempCmathCol.rgb.r).asUInt
                    ) >> 1
                  ).resized
                  tempCol.r(
                    tempCol.r.high downto params.rgbConfig.rWidth
                  ) := 0
                  tempCol.g(params.rgbConfig.gWidth - 1 downto 0) := (
                    (
                      inpWrLineMemEntry.col.rgb.g.resized
                      + Cat(False, tempCmathCol.rgb.g).asUInt
                    ) >> 1
                  ).resized
                  tempCol.g(
                    tempCol.g.high downto params.rgbConfig.gWidth
                  ) := 0
                  tempCol.b(params.rgbConfig.bWidth - 1 downto 0) := (
                    (
                      inpWrLineMemEntry.col.rgb.b.resized
                      + Cat(False, tempCmathCol.rgb.b).asUInt
                    ) >> 1
                  ).resized
                  tempCol.b(
                    tempCol.b.high downto params.rgbConfig.bWidth
                  ) := 0
                  //tempCol.r := (
                  //  (
                  //    inpWrLineMemEntry.col.rgb.r + tempCmathCol.rgb.r
                  //  ) >> 1
                  //).resized
                  //tempCol.g := (
                  //  (
                  //    inpWrLineMemEntry.col.rgb.g + tempCmathCol.rgb.g
                  //  ) >> 1
                  //).resized
                  //tempCol.b := (
                  //  (
                  //    inpWrLineMemEntry.col.rgb.b + tempCmathCol.rgb.b
                  //  ) >> 1
                  //).resized
                }
              }

              when (
                tempCol.r.asSInt >= (1 << params.rgbConfig.rWidth) - 1
              ) {
                outpWrLineMemEntry.col.rgb.r := (default -> True)
              } elsewhen (tempCol.r.asSInt < 0) {
                outpWrLineMemEntry.col.rgb.r := (default -> False)
              } otherwise {
                outpWrLineMemEntry.col.rgb.r := tempCol.r(
                  outpWrLineMemEntry.col.rgb.r.bitsRange
                )
              }
              when (
                tempCol.g.asSInt >= (1 << params.rgbConfig.gWidth) - 1
              ) {
                outpWrLineMemEntry.col.rgb.g := (default -> True)
              } elsewhen (tempCol.g.asSInt < 0) {
                outpWrLineMemEntry.col.rgb.g := (default -> False)
              } otherwise {
                outpWrLineMemEntry.col.rgb.g := tempCol.g(
                  outpWrLineMemEntry.col.rgb.g.bitsRange
                )
              }
              when (
                tempCol.b.asSInt >= (1 << params.rgbConfig.bWidth) - 1
              ) {
                outpWrLineMemEntry.col.rgb.b := (default -> True)
              } elsewhen (tempCol.b.asSInt < 0) {
                outpWrLineMemEntry.col.rgb.b := (default -> False)
              } otherwise {
                outpWrLineMemEntry.col.rgb.b := tempCol.b(
                  outpWrLineMemEntry.col.rgb.b.bitsRange
                )
              }
            } otherwise {
              //tempCol := tempCol.getZero
              outpWrLineMemEntry := inpWrLineMemEntry
            }
          } else { // if (noColorMath)
            outpWrLineMemEntry.col.rgb := inpWrLineMemEntry.col.rgb
          }
        }
      }
      //HandleDualPipe(
      //  stageData=stageData.craft(
      //    //4
      //    //5
      //    8
      //    //wrBgObjPipeNumStages + 4
      //  )
      //)(
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage8 := tempOutp.stage8.getZero
      //    } otherwise {
      //      ////if (inSim) {
      //      //  tempOutp.stage8.combineWrLineMemEntry.addr := (
      //      //    //tempInp.cnt
      //      //    //tempInp.stage1.lineMemIdx
      //      //    tempInp.cnt
      //      //    (
      //      //      tempOutp.stage8.combineWrLineMemEntry.addr.bitsRange
      //      //    )
      //      //  )
      //      ////}
      //      //tempOutp.stage8.combineWrLineMemEntry.col := tempInp.col

      //      ////// not really necessary, but doing it anyway
      //      ////tempOutp.stage8.combineWrLineMemEntry.col.a := True
      //      ////tempOutp.stage8.combineWrLineMemEntry.col.a := 

      //      //tempOutp.stage8.combineWrLineMemEntry.prio := 0x0
      //      //tempOutp.stage8.combineWrLineMemEntry.colorMathInfo := (
      //      //  tempInp.colorMathInfo
      //      //)
      //      //tempOutp.stage8.combineWrLineMemEntry.colorMathCol := (
      //      //  tempInp.colorMathCol
      //      //)
      //      val tempCmathInfo = (!noColorMath) generate (
      //        tempInp.stage7.combineWrLineMemEntry.colorMathInfo
      //      )
      //      val tempCmathCol = (!noColorMath) generate (
      //        tempInp.stage7.combineWrLineMemEntry.colorMathCol
      //      )
      //      def inpWrLineMemEntry = tempInp.stage7.combineWrLineMemEntry
      //      def outpWrLineMemEntry = tempOutp.combineWrLineMemEntry
      //      outpWrLineMemEntry := outpWrLineMemEntry.getZero
      //      outpWrLineMemEntry.col.rgb.allowOverride
      //      def extraWidth = 4
      //      def tempRgbConfig = RgbConfig(
      //        rWidth=params.rgbConfig.rWidth + extraWidth,
      //        gWidth=params.rgbConfig.gWidth + extraWidth,
      //        bWidth=params.rgbConfig.bWidth + extraWidth,
      //      )
      //      val tempCol = Rgb(tempRgbConfig)
      //      tempCol := tempCol.getZero
      //      tempCol.allowOverride
      //      if (!noColorMath) {
      //        when (
      //          tempCmathInfo.doIt
      //        ) {
      //          switch (tempCmathInfo.kind.asBits) {
      //            is (Gpu2dColorMathKind.add.asBits) {
      //              tempCol.r(params.rgbConfig.rWidth downto 0) := (
      //                inpWrLineMemEntry.col.rgb.r.resized
      //                + Cat(False, tempCmathCol.rgb.r).asUInt
      //              )
      //              tempCol.r(
      //                tempCol.r.high downto params.rgbConfig.rWidth + 1
      //              ) := 0
      //              tempCol.g(params.rgbConfig.gWidth downto 0) := (
      //                inpWrLineMemEntry.col.rgb.g.resized
      //                + Cat(False, tempCmathCol.rgb.g).asUInt
      //              )
      //              tempCol.g(
      //                tempCol.g.high downto params.rgbConfig.gWidth + 1
      //              ) := 0
      //              tempCol.b(params.rgbConfig.bWidth downto 0) := (
      //                inpWrLineMemEntry.col.rgb.b.resized
      //                + Cat(False, tempCmathCol.rgb.b).asUInt
      //              )
      //              tempCol.b(
      //                tempCol.b.high downto params.rgbConfig.bWidth + 1
      //              ) := 0
      //            }
      //            is (Gpu2dColorMathKind.sub.asBits) {
      //              tempCol.r := (
      //                inpWrLineMemEntry.col.rgb.r.resized
      //                - Cat(
      //                  B(
      //                    tempCol.r.getWidth - tempCmathCol.rgb.r.getWidth
      //                      bits,
      //                    default -> tempCmathCol.rgb.r.msb),
      //                  tempCmathCol.rgb.r
      //                ).asUInt
      //              )
      //              tempCol.g := (
      //                inpWrLineMemEntry.col.rgb.g.resized
      //                - Cat(
      //                  B(
      //                    tempCol.g.getWidth - tempCmathCol.rgb.g.getWidth
      //                      bits,
      //                    default -> tempCmathCol.rgb.g.msb),
      //                  tempCmathCol.rgb.g
      //                ).asUInt
      //              )
      //              tempCol.b := (
      //                inpWrLineMemEntry.col.rgb.b.resized
      //                - Cat(
      //                  B(
      //                    tempCol.b.getWidth - tempCmathCol.rgb.b.getWidth
      //                      bits,
      //                    default -> tempCmathCol.rgb.b.msb),
      //                  tempCmathCol.rgb.b
      //                ).asUInt
      //              )
      //            }
      //            is (Gpu2dColorMathKind.avg.asBits) {
      //              tempCol.r(params.rgbConfig.rWidth - 1 downto 0) := (
      //                (
      //                  inpWrLineMemEntry.col.rgb.r.resized
      //                  + Cat(False, tempCmathCol.rgb.r).asUInt
      //                ) >> 1
      //              ).resized
      //              tempCol.r(
      //                tempCol.r.high downto params.rgbConfig.rWidth
      //              ) := 0
      //              tempCol.g(params.rgbConfig.gWidth - 1 downto 0) := (
      //                (
      //                  inpWrLineMemEntry.col.rgb.g.resized
      //                  + Cat(False, tempCmathCol.rgb.g).asUInt
      //                ) >> 1
      //              ).resized
      //              tempCol.g(
      //                tempCol.g.high downto params.rgbConfig.gWidth
      //              ) := 0
      //              tempCol.b(params.rgbConfig.bWidth - 1 downto 0) := (
      //                (
      //                  inpWrLineMemEntry.col.rgb.b.resized
      //                  + Cat(False, tempCmathCol.rgb.b).asUInt
      //                ) >> 1
      //              ).resized
      //              tempCol.b(
      //                tempCol.b.high downto params.rgbConfig.bWidth
      //              ) := 0
      //              //tempCol.r := (
      //              //  (
      //              //    inpWrLineMemEntry.col.rgb.r + tempCmathCol.rgb.r
      //              //  ) >> 1
      //              //).resized
      //              //tempCol.g := (
      //              //  (
      //              //    inpWrLineMemEntry.col.rgb.g + tempCmathCol.rgb.g
      //              //  ) >> 1
      //              //).resized
      //              //tempCol.b := (
      //              //  (
      //              //    inpWrLineMemEntry.col.rgb.b + tempCmathCol.rgb.b
      //              //  ) >> 1
      //              //).resized
      //            }
      //          }

      //          when (
      //            tempCol.r.asSInt >= (1 << params.rgbConfig.rWidth) - 1
      //          ) {
      //            outpWrLineMemEntry.col.rgb.r := (default -> True)
      //          } elsewhen (tempCol.r.asSInt < 0) {
      //            outpWrLineMemEntry.col.rgb.r := (default -> False)
      //          } otherwise {
      //            outpWrLineMemEntry.col.rgb.r := tempCol.r(
      //              outpWrLineMemEntry.col.rgb.r.bitsRange
      //            )
      //          }
      //          when (
      //            tempCol.g.asSInt >= (1 << params.rgbConfig.gWidth) - 1
      //          ) {
      //            outpWrLineMemEntry.col.rgb.g := (default -> True)
      //          } elsewhen (tempCol.g.asSInt < 0) {
      //            outpWrLineMemEntry.col.rgb.g := (default -> False)
      //          } otherwise {
      //            outpWrLineMemEntry.col.rgb.g := tempCol.g(
      //              outpWrLineMemEntry.col.rgb.g.bitsRange
      //            )
      //          }
      //          when (
      //            tempCol.b.asSInt >= (1 << params.rgbConfig.bWidth) - 1
      //          ) {
      //            outpWrLineMemEntry.col.rgb.b := (default -> True)
      //          } elsewhen (tempCol.b.asSInt < 0) {
      //            outpWrLineMemEntry.col.rgb.b := (default -> False)
      //          } otherwise {
      //            outpWrLineMemEntry.col.rgb.b := tempCol.b(
      //              outpWrLineMemEntry.col.rgb.b.bitsRange
      //            )
      //          }
      //        } otherwise {
      //          //tempCol := tempCol.getZero
      //          outpWrLineMemEntry := inpWrLineMemEntry
      //        }
      //      } else { // if (noColorMath)
      //        outpWrLineMemEntry.col.rgb := inpWrLineMemEntry.col.rgb
      //      }
      //    }
      //  },
      //  copyOnlyFunc=(
      //    stageData: DualPipeStageData[Stream[CombinePipePayload]],
      //    idx: Int,
      //  ) => {
      //    //stageData.pipeOut(idx).cnt := stageData.pipeIn(idx).cnt
      //    //stageData.pipeOut(idx).stage4 := stageData.pipeIn(idx).stage4
      //    val tempInp = stageData.pipeIn(idx)
      //    val tempOutp = stageData.pipeOut(idx)

      //    when (clockDomain.isResetActive) {
      //      tempOutp.stage8 := tempOutp.stage8.getZero
      //    } otherwise {
      //      //tempOutp.stage8 := tempInp.stage8
      //      if (idx != 1) {
      //        tempOutp.stage8 := tempInp.stage8
      //      } else {
      //        tempOutp.stage8 := (
      //          combineBgObjRdPipeJoin.io.pipeOut.payload(0).stage8
      //        )
      //      }
      //    }
      //  },
      //)

      when (
        nCombinePipeLast.isFiring
        //combinePipeOut.last.fire
      ) {
        def tempObjArrIdx = params.getObjSubLineMemArrIdx(
          addr=(
            combinePipeLast.cnt
            //combinePipeLast.stage1.lineMemIdx
          )
        )
        val dbgTestCombinePipeLast_tempObjArrIdx = UInt(
          tempObjArrIdx.getWidth bits
        )
          .setName("dbgTestCombinePipeLast_tempObjArrIdx")
        dbgTestCombinePipeLast_tempObjArrIdx := tempObjArrIdx
        //--------
        // BEGIN: new code, with muxing for single `.write()` call
        val tempObjLineMemEntry = Vec.fill(
          //params.objTileSize2d.x
          params.objSliceTileWidth
        )(
          ObjSubLineMemEntry()
        ).getZero
        objWriter.addrVec(1) := tempObjArrIdx
        objWriter.dataVec(1) := tempObjLineMemEntry
        objWriter.enVec(1) := True
        // END: new code, with muxing for single `.write()` call
        //--------
        if (!noAffineObjs) {
          def tempObjAffineArrIdx = params.getObjAffineSubLineMemArrIdx(
            addr=(
              combinePipeLast.cnt
              //combinePipeLast.stage1.lineMemIdx
            )
          )
          val dbgTestCombinePipeLast_tempObjAffineArrIdx = UInt(
            tempObjAffineArrIdx.getWidth bits
          )
            .setName("dbgTestCombinePipeLast_tempObjAffineArrIdx")
          dbgTestCombinePipeLast_tempObjAffineArrIdx := (
            tempObjAffineArrIdx
          )
          //--------
          val tempObjAffineLineMemEntry = (
            Vec.fill(
              params.objAffineSliceTileWidth
              //params.objAffineTileSize2d.x
              //params.objAffineDblTileSize2d.x
              //params.objAffineSliceTileWidth
              //params.myDbgObjAffineTileWidth
            )(
              ObjSubLineMemEntry()
            ).getZero
          )
          objAffineWriter.addrVec(1) := tempObjAffineArrIdx
          objAffineWriter.dataVec(1) := tempObjAffineLineMemEntry
          objAffineWriter.enVec(1) := True
        }
        //--------
        outp.col := combinePipeLast.combineWrLineMemEntry.col.rgb
        //outp.col := combinePipeOut.last.combineWrLineMemEntry.col.rgb
        //rdPhysCalcPosEn := True
        //--------
      } otherwise {
        //objWriter.enVec(rCombineFullLineMemArrIdx) := False
        objWriter.enVec(1) := False
        if (!noAffineObjs) {
          objAffineWriter.enVec(1) := False
        }
        outp.col := rPastOutp.col
        //rdPhysCalcPosEn := False
      }
      when (
        //combinePipeLastThrown.fire
        pop.fire
      ) {
        rdPhysCalcPosEn := True
        //outp.col := combinePipeLastThrown.combineWrLineMemEntry.col.rgb
      } otherwise {
        rdPhysCalcPosEn := False
        //outp.col := rPastOutp.col
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
    if (!noAffineObjs) {
      for (kind <- 0 until 2) {
        writeObjLineMemEntries(kind=kind)
      }
    } else {
      writeObjLineMemEntries(kind=0)
    }

    //switch (rCombineFullLineMemArrIdx) {
    //  for (idx <- 0 to (1 << rCombineFullLineMemArrIdx.getWidth) - 1) {
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
    Builder(linkArr.toSeq)
    //--------
  }
}
