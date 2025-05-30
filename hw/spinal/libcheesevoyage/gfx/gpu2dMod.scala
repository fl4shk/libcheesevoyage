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
import libcheesevoyage.general.PipeMemRmwConfig
import libcheesevoyage.general.PipeMemRmwIo
import libcheesevoyage.general.PipeMemRmwPayloadExt
import libcheesevoyage.general.SamplePipeMemRmwModType
import libcheesevoyage.general.PipeMemRmwDualRdTypeDisabled
import libcheesevoyage.general.PipeMemRmwPayloadBase
import libcheesevoyage.general.PipeMemRmwFwd
//import libcheesevoyage.general.VecIntf
//import libcheesevoyage.general.VecIntfElem
//import libcheesevoyage.general.VecIntf
//import libcheesevoyage.general.PipeMemRmwPayloadBaseFormalFwdFuncs
import spinal.lib.misc.pipeline._

//import scala.math._
import spinal.core._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import spinal.core.formal._
//import spinal.core.Interface
import scala.collection.mutable.ArrayBuffer
//import scala.collection.immutable._
import scala.math._

case class Gpu2dConfig(
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
  //objAffineTileMemInit: Option[ArrayBuffer[UInt]]=None,
  objAffineTileMemInit: Option[ArrayBuffer[BigInt]]=None,
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
    min(
      bgTileSize2d.x,
      4,
    )
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
  println(
    f"${objAffineTileSize2dPow.x} - ${objAffineTileWidthRshift}: "
    + f"${objAffineSliceTileWidthPow} -> ${objAffineSliceTileWidth}"
  )
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
  val numBgTilesPow = log2Up(numBgTiles)
  def numColsInBgPal = 1 << numColsInBgPalPow
  val numColorMathTilesPow = log2Up(numColorMathTiles)
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

  //def oneLineMemSize = physFbSize2dScale.y * cfg.physFbSize2d.x

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
  val bgSubLineMemArrSizePow = (
    log2Up(oneLineMemSize) - bgTileSize2dPow.x
  )
  val bgSubLineMemArrSize = 1 << bgSubLineMemArrSizePow
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
  //println(objSubLineMemArrSize)
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
  val bgSize2dInTiles = ElabVec2[Int](
    x=1 << bgSize2dInTilesPow.x,
    y=1 << bgSize2dInTilesPow.y,
  )
  val bgSize2dInPxs = ElabVec2[Int](
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
    //1 // to account for the extra cycle delay between pixels
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
    //1 // to account for the extra cycle delay between pixels
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
  val bgTileMemIdxWidth = numBgTilesPow + bgTileSize2dPow.y
  //def bgTileGridMemIdxWidth = (
  //  bgTileMemIdxWidth - 1
  //  //bgTileMemIdxWidth - bgTileSize2dPow.y 
  //  //bgTileMemIdxWidth - bgTileSize2dPow.x 
  //)
  val colorMathTileMemIdxWidth = numColorMathTilesPow + bgTileSize2dPow.y
  val objTileSliceMemIdxWidth = (
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
  val objAffineTilePxMemIdxWidth = (
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
  val bgEntryMemIdxWidth = bgSize2dInTilesPow.x + bgSize2dInTilesPow.y

  //def palEntryMemIdxWidth = log2Up(numColsInPal)
  //def palEntryMemIdxWidth = numColsInPalPow
  val bgPalEntryMemIdxWidth = numColsInBgPalPow
  val objPalEntryMemIdxWidth = numColsInObjPalPow
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
  val fbBaseAddrWidthPow = (
    log2Up(2) // for double buffering
  )
  val fbRdAddrMultWidthPow = (
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
object DefaultGpu2dConfig {
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
    //objAffineTileMemInit: Option[ArrayBuffer[UInt]]=None,
    objAffineTileMemInit: Option[ArrayBuffer[BigInt]]=None,
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
    Gpu2dConfig(
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

case class Gpu2dRgb(
  //cfg: Gpu2dParams,
  rgbConfig: RgbConfig,
) extends Bundle {
  def rWidth = rgbConfig.rWidth
  def gWidth = rgbConfig.gWidth
  def bWidth = rgbConfig.bWidth
  val r = UInt(rWidth bits)
  val g = UInt(gWidth bits)
  val b = UInt(bWidth bits)
  //val rgb = UInt((rWidth + gWidth + bWidth) bits)
  //def r = rgb(rWidth - 1 downto 0)
  //def g = rgb(rWidth + gWidth - 1 downto rWidth)
  //def b = rgb(rgb.high downto rWidth + gWidth)
  //--------
  //addGeneric(
  //  name="rWidth",
  //  that=rWidth,
  //  default="4",
  //)
  //tieGeneric(
  //  signal=r,
  //  generic="rWidth",
  //)
  //addGeneric(
  //  name="gWidth",
  //  that=gWidth,
  //  default="4",
  //)
  //tieGeneric(
  //  signal=g,
  //  generic="gWidth",
  //)
  //addGeneric(
  //  name="bWidth",
  //  that=bWidth,
  //  default="4",
  //)
  //tieGeneric(
  //  signal=b,
  //  generic="bWidth",
  //)
  //--------
  //notSVmodport()
  //notSVIF()
  //setAsSVstruct()
}
case class Gpu2dRgba(
  //cfg: Gpu2dParams,
  rgbConfig: RgbConfig,
) extends Bundle {
  //val rgb = Rgb(c=cfg.rgbConfig)
  val rgb = Gpu2dRgb(rgbConfig=rgbConfig)
  def r = rgb.r
  def g = rgb.g
  def b = rgb.b
  val a = Bool()
  def rWidth = rgbConfig.rWidth
  def gWidth = rgbConfig.gWidth
  def bWidth = rgbConfig.bWidth
  //addGeneric(
  //  name="rWidth",
  //  that=rWidth,
  //  default="4",
  //)
  //addGeneric(
  //  name="gWidth",
  //  that=gWidth,
  //  default="4",
  //)
  //addGeneric(
  //  name="bWidth",
  //  that=bWidth,
  //  default="4",
  //)
  //tieIFParameter(
  //  signal=rgb,
  //  signalParam="rWidth",
  //  inputParam="rWidth",
  //)
  //tieIFParameter(
  //  signal=rgb,
  //  signalParam="gWidth",
  //  inputParam="gWidth",
  //)
  //tieIFParameter(
  //  signal=rgb,
  //  signalParam="bWidth",
  //  inputParam="bWidth",
  //)
  //notSVIF()
  //notSVmodport()
  //setAsSVstruct()
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
    cfg: Gpu2dConfig,
    isObj: Boolean,
    doPipeMemRmw: Boolean
  ) = (
    if (!isObj) (
      cfg.bgPalEntryMemIdxWidth
    ) else (
      cfg.objPalEntryMemIdxWidth
      + Gpu2dTileSlice.myColIdxFracWidth(doPipeMemRmw=doPipeMemRmw)
    )
  )
  def pxsSliceWidth(
    cfg: Gpu2dConfig,
    isObj: Boolean,
    isAffine: Boolean,
  ) = (
    if (!isObj) {
      cfg.bgTileSize2d.x
    } else if (!isAffine) {
      //cfg.objTileSize2d
      cfg.objSliceTileWidth
    } else {
      //cfg.objAffineTileSize2d
      cfg.objAffineSliceTileWidth
    }
  )
}

//case class Gpu2dTileSliceRow(
//) extends Interface {
//}
//case class Gpu2dTileSlicePixel(
//  //pxsSliceWidth: Int,
//  colIdxWidth: Int,
//) extends Interface {
//  val data = UInt(colIdxWidth bits)
//  addGeneric(
//    name="colIdxWidth",
//    that=colIdxWidth,
//    default="6",
//  )
//  tieGeneric(
//    signal=data,
//    generic="colIdxWidth",
//  )
//}
case class Gpu2dTileSlice(
  cfg: Gpu2dConfig,
  isObj: Boolean,
  isAffine: Boolean,
  doPipeMemRmw: Boolean=false,
) extends Bundle {
  //--------
  //val colIdx = UInt(cfg.palEntryMemIdxWidth bits)
  val colIdxWidth = (
    Gpu2dTileSlice.colIdxWidth(
      cfg=cfg,
      isObj=isObj,
      doPipeMemRmw=doPipeMemRmw,
    )
  )
  val pxsSliceWidth = (
    Gpu2dTileSlice.pxsSliceWidth(
      cfg=cfg,
      isObj=isObj,
      isAffine=isAffine,
    )
  )
  def fullPxsSize2d = (
    if (!isObj) {
      cfg.bgTileSize2d
    } else if (!isAffine) {
      cfg.objTileSize2d
    } else {
      cfg.objAffineTileSize2d
    }
  )
  //println(pxsSliceWidth)
  val colIdxVec = Vec.fill(pxsSliceWidth){
    //VecIntfElem(
    //  () => 
      UInt(colIdxWidth bits),
      //generics=Some(
      //  (elem: VecIntfElem[UInt]) => {
      //    elem.addGeneric(
      //      name="colIdxWidth",
      //      that=colIdxWidth,
      //      default="8",
      //    )
      //  }
      //)
    //).//setAsSVstruct()
    //temp.//setAsSVstruct()
    //temp
    //Gpu2dTileSlicePixel(
    //  colIdxWidth=colIdxWidth
    //)
  }
  //setAsSVstruct()

  def setPx(
    pxCoordX: UInt,
    colIdx: UInt,
  ): Unit = {
    //println(pxCoordX.getWidth)
    colIdxVec(pxCoordX)/*.v*/ := colIdx
  }
  def setPx(
    pxCoordX: UInt,
    colIdx: Int
  ): Unit = {
    //assert(pxCoordX >= 0 && pxCoordX < pxsWidth)
    colIdxVec(pxCoordX)/*.v*/ := colIdx
  }
  def setPx(
    pxCoordX: Int,
    colIdx: UInt
  ): Unit = {
    //assert(pxCoordX >= 0 && pxCoordX < pxsWidth)
    colIdxVec(pxCoordX)/*.v*/ := colIdx
  }
  def setPx(
    pxCoordX: Int,
    colIdx: Int
  ): Unit = {
    //assert(pxCoordX >= 0 && pxCoordX < pxsWidth)
    colIdxVec(pxCoordX)/*.v*/ := colIdx
  }
  def getPx(
    pxCoordX: Int,
  ) = {
    colIdxVec(pxCoordX)//.v
  }
  def getPx(
    pxCoordX: UInt,
    //colIdx: UInt
  ) = {
    colIdxVec(pxCoordX)//.v
  }
  //addGeneric(
  //  name="colIdxWidth",
  //  that=colIdxWidth,
  //  default="8"
  //)
  //for (idx <- 0 until colIdxVec.size) {
  //  //tieGeneric(
  //  //  signal=colIdxVec(idx),
  //  //  generic="colIdxWidth",
  //  //)
  //  tieIFParameter(
  //    signal=colIdxVec(idx),
  //    signalParam="colIdxWidth",
  //    inputParam="colIdxWidth",
  //  )
  //}
  ////notSVIF()
}

case class Gpu2dTileFull(
  cfg: Gpu2dConfig,
  isObj: Boolean,
  isAffine: Boolean,
) extends Bundle {
  //--------
  //val colIdx = UInt(cfg.palEntryMemIdxWidth bits)
  def colIdxWidth = (
    if (!isObj) {
      cfg.bgPalEntryMemIdxWidth
    } else {
      cfg.objPalEntryMemIdxWidth
    }
  )

  def pxsSize2d = (
    if (!isObj) {
      cfg.bgTileSize2d
    } else if (!isAffine) {
      cfg.objTileSize2d
    } else {
      cfg.objAffineTileSize2d
    }
  )

  // indices into `Gpu2d.loc.palEntryMem`
  val colIdxRowVec = (
    //Vec.fill(pxsSize2d.y)(
    //  UInt((pxsSize2d.x * colIdxWidth) bits)
    //)
    Vec.fill(pxsSize2d.y)(
      Vec.fill(pxsSize2d.x)(
         UInt(colIdxWidth bits,
        )
      )
    )
    //if (!isObj) {
    //  //Vec.fill(cfg.bgTileSize2d.y)(
    //  //  Vec.fill(cfg.bgTileSize2d.x)(UInt(colIdxWidth bits))
    //  //)
    //  //UInt(
    //  //  (
    //  //    cfg.bgTileSize2dPow.y + cfg.bgTileSize2dPow.x
    //  //    + colIdxWidth
    //  //  )
    //  //  bits
    //  //)
    //  Vec.fill(cfg.bgTileSize2d.y)(
    //    UInt((cfg.bgTileSize2d.x * colIdxWidth) bits)
    //    //UInt((cfg.bgTileSize2d.x + (1 << colIdxWidth)) bits)
    //  )
    //} else { // if (isObj)
    //  //Vec.fill(cfg.objTileSize2d.y)(
    //  //  Vec.fill(cfg.objTileSize2d.x)(UInt(colIdxWidth bits))
    //  //)
    //  //UInt(
    //  //  (
    //  //    cfg.objTileSize2dPow.y + cfg.objTileSize2dPow.x
    //  //    + colIdxWidth
    //  //  ) bits
    //  //)
    //  Vec.fill(cfg.objTileSize2d.y)(
    //    //UInt((cfg.objTileSize2dPow.x + colIdxWidth) bits)
    //    UInt((cfg.objTileSize2d.x * colIdxWidth) bits)
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
  cfg: Gpu2dConfig,
  isColorMath: Boolean,
) extends Bundle {
  //--------
  //val rgb = Rgb(cfg.rgbConfig)
  //val tile = Gpu2dFullTile(
  //  cfg=cfg,
  //  isObj=false,
  //  isAffine=false,
  //)
  val tileSlice: Gpu2dTileSlice = Gpu2dTileSlice(
    cfg=cfg,
    isObj=false,
    isAffine=false,
  )

  // `Mem` index, so in units of tile slices
  //val idx = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  //val idx = UInt(16 bits)
  val memIdx = UInt(
    (
      if (!isColorMath) {
        cfg.bgTileMemIdxWidth
      } else {
        cfg.colorMathTileMemIdxWidth
      }
    ) bits
  )
  //addGeneric(
  //  name="memIdxWidth",
  //  that=(
  //    if (!isColorMath) {
  //      cfg.bgTileMemIdxWidth
  //    } else {
  //      cfg.colorMathTileMemIdxWidth
  //    }
  //  ),
  //  default="8"
  //)
  //tieGeneric(
  //  signal=memIdx,
  //  generic="memIdxWidth",
  //)
  //setAsSVstruct()
  //--------
}
case class Gpu2dObjTileStmPayload(
  cfg: Gpu2dConfig,
  isAffine: Boolean,
  dbgPipeMemRmw: Boolean,
) extends Bundle {
  //--------
  //val rgb = Rgb(cfg.rgbConfig)
  //val tile = Gpu2dFullTile(
  //  cfg=cfg,
  //  isObj=true,
  //  isAffine=isAffine,
  //)
  val tileSlice = (!isAffine) generate Gpu2dTileSlice(
    cfg=cfg,
    isObj=true,
    isAffine=isAffine,
  )
  val tilePx = (isAffine) generate (
    UInt(cfg.objPalEntryMemIdxWidth bits)
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
        cfg.objTileSliceMemIdxWidth 
      } else {
        cfg.objAffineTilePxMemIdxWidth
        //cfg.objAffineTileSliceMemIdxWidth
      }
    ) bits
  )
  //addGeneric(
  //  name="memIdxWidth",
  //  that=(
  //    if (!isAffine) {
  //      cfg.objTileSliceMemIdxWidth 
  //    } else {
  //      cfg.objAffineTilePxMemIdxWidth
  //      //cfg.objAffineTileSliceMemIdxWidth
  //    }
  //  ),
  //  default="8"
  //)
  //tieGeneric(
  //  signal=memIdx,
  //  generic="memIdxWidth",
  //)
  //notSVmodport()
  //setAsSVstruct()
  //--------
}

case class Gpu2dBgEntry(
  cfg: Gpu2dConfig,
  isColorMath: Boolean,
) extends Bundle {
  //--------
  // The index, in tiles, of the tile represented by this tilemap entry
  val tileIdx = UInt(
    (
      if (!isColorMath) {
        //cfg.bgTileMemIdxWidth
        cfg.numBgTilesPow
      } else {
        //cfg.colorMathTileMemIdxWidth
        cfg.numColorMathTilesPow
      }
    ) bits
  )
  //addGeneric(
  //  name="tileIdxWidth",
  //  that=(
  //    if (!isColorMath) {
  //      //cfg.bgTileMemIdxWidth
  //      cfg.numBgTilesPow
  //    } else {
  //      //cfg.colorMathTileMemIdxWidth
  //      cfg.numColorMathTilesPow
  //    }
  //  ),
  //  default="8"
  //)
  //tieGeneric(
  //  signal=tileIdx,
  //  generic="tileIdxWidth",
  //)

  // The priority for this tilemap entry
  //val prio = UInt(log2Up(cfg.numBgs) bits)

  // whether or not to visibly flip x/y during rendering
  val dispFlip = Vec2(dataType=Bool())
  //--------
  //notSVmodport()
  //setAsSVstruct()
}
case class Gpu2dBgEntryStmPayload(
  cfg: Gpu2dConfig,
  isColorMath: Boolean,
) extends Bundle {
  //--------
  val bgEntry = Gpu2dBgEntry(
    cfg=cfg,
    isColorMath=isColorMath,
  )
  //addGeneric(
  //  name="bgEntry_tileIdxWidth",
  //  that=(
  //    if (!isColorMath) {
  //      //cfg.bgTileMemIdxWidth
  //      cfg.numBgTilesPow
  //    } else {
  //      //cfg.colorMathTileMemIdxWidth
  //      cfg.numColorMathTilesPow
  //    }
  //  ),
  //  default="8",
  //)
  //tieIFParameter(
  //  signal=bgEntry,
  //  signalParam="tileIdxWidth",
  //  inputParam="bgEntry_tileIdxWidth",
  //)


  // `Mem` index
  val memIdx = UInt(
    cfg.bgEntryMemIdxWidth bits
  )
  //addGeneric(
  //  name="memIdxWidth",
  //  that=cfg.bgEntryMemIdxWidth,
  //  default="8",
  //)
  //tieGeneric(
  //  signal=memIdx,
  //  generic="memIdxWidth",
  //)
  //--------
  //notSVmodport()
  //setAsSVstruct()
  //--------
}

//case class Gpu2dColorMathAttrs(
//  cfg: Gpu2dParams
//) extends Interface {
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
  cfg: Gpu2dConfig
) extends Bundle {
  //--------
  val doIt = Bool()
  //--------
  val kind = Gpu2dColorMathKind()
  //--------
}
object Gpu2dAffine {
  def wholeWidth(
    cfg: Gpu2dConfig,
    isObj: Boolean,
  ) = (
    if (!isObj) {
      log2Up(
        cfg.intnlFbSize2d.x
        .max(cfg.intnlFbSize2d.y)
      )
    } else {
      (
        (
          //cfg.objTileSize2dPow.x
          //.max(cfg.objTileSize2dPow.y)
          cfg.objAffineDblTileSize2dPow.x
          .max(cfg.objAffineDblTileSize2dPow.y)
        )
      ).max(8)
    }
  )
  def fracWidth = 8
  def fullWidth(
    cfg: Gpu2dConfig,
    isObj: Boolean,
  ) = {
    wholeWidth(
      cfg=cfg,
      isObj=isObj,
    ) + fracWidth
  }
  def multSize2dPow(
    cfg: Gpu2dConfig,
    isObj: Boolean,
  ) = (
    //if (!isObj) {
      ElabVec2[Int](
        x=Gpu2dAffine.fullWidth(
          cfg=cfg,
          isObj=isObj,
        ) + fracWidth,
        y=Gpu2dAffine.fullWidth(
          cfg=cfg,
          isObj=isObj,
        ) + fracWidth,
      )
    //} else {
    //  ElabVec2[Int](
    //    x=cfg.objPxsCoordSize2dPow.x + fracWidth,
    //    y=cfg.objPxsCoordSize2dPow.y + fracWidth,
    //  )
    //}
  )
  def vecSize = 2
}
case class Gpu2dAffine(
  cfg: Gpu2dConfig,
  isObj: Boolean,
) extends Bundle {
  //--------
  val doIt = Bool()
  //--------
  def wholeWidth = Gpu2dAffine.wholeWidth(
    cfg=cfg,
    isObj=isObj,
  )
  def fracWidth = Gpu2dAffine.fracWidth
  def fullWidth = Gpu2dAffine.fullWidth(
    cfg=cfg,
    isObj=isObj,
  )
  def vecSize = Gpu2dAffine.vecSize
  def multSize2dPow = Gpu2dAffine.multSize2dPow(
    cfg=cfg,
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
  //notSVmodport()
  //setAsSVstruct()
  //--------
}

// Framebuffer Attributes
case class Gpu2dFbAttrs(
  cfg: Gpu2dConfig,
  isColorMath: Boolean,
) extends Bundle {
  // Whether to treat this background like a framebuffer
  val doIt = Bool()

  //def temp = (
  //  cfg.bgSize2dInTiles.y
  //  * cfg.bgSize2dInTiles.x
  //  * cfg.bgTileSize2d.y
  //)

  // which address in `bgTileMemArr` that the framebuffer starts from
  val tileMemBaseAddr = UInt(cfg.fbBaseAddrWidthPow bits)
  //def getTileMemAddr(
  //  //tilePxsCoordY: UInt,
  //) = {
  //  //assert(tilePxsCoordY.getWidth == cfg.bgTileSize2dPow.y)
  //  //Cat(tileMemBaseAddr, tilePxsCoordY).asUInt
  //  tileMemBaseAddr
  //}
  //notSVmodport()
  //setAsSVstruct()
  //--------
}
// Attributes for a whole background
case class Gpu2dBgAttrs(
  cfg: Gpu2dConfig,
  isColorMath: Boolean,
) extends Bundle {
  //--------
  // Whether or not to display this BG
  //val visib = Bool()

  val colorMathInfo = (
    (
      !isColorMath && !cfg.noColorMath
    ) generate Gpu2dColorMathInfo(cfg=cfg)
  )
  //val affine = Gpu2dAffine(
  //  cfg=cfg,
  //  isObj=false
  //)

  // How much to scroll this background
  val scroll = cfg.bgPxsCoordT()

  val fbAttrs = Gpu2dFbAttrs(
    cfg=cfg,
    isColorMath=isColorMath,
  )
  //--------
  //notSVmodport()
  //setAsSVstruct()
  //--------
}
case class Gpu2dBgAttrsStmPayload(
  cfg: Gpu2dConfig,
  isColorMath: Boolean,
) extends Bundle {
  //--------
  val bgAttrs = Gpu2dBgAttrs(
    cfg=cfg,
    isColorMath=isColorMath,
  )

  //val memIdx = UInt(cfg.bgAttrsMemIdxWidth bits)
  //--------
  //notSVmodport()
  //setAsSVstruct()
  //--------
}

case class Gpu2dObjAttrs(
  cfg: Gpu2dConfig,
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
        //cfg.objTileMemIdxWidth
        cfg.numObjTilesPow
      } else {
        //cfg.objAffineTileMemIdxWidth
        cfg.numObjAffineTilesPow
      }
    ) bits
  )
  //val pos = cfg.coordT(fbSize2d=cfg.bgSize2dInPxs)

  //// position within the tilemap, in pixels
  // position on screen, in pixels
  val pos = cfg.objPxsCoordT()

  // the priority for the OBJ
  val prio = UInt(log2Up(cfg.numBgs) bits)

  val colorMathInfo = (!cfg.noColorMath) generate (
    Gpu2dColorMathInfo(cfg=cfg)
  )
  val affine = (isAffine) generate Gpu2dAffine(
    cfg=cfg,
    isObj=true,
  )

  //val size2dMinus1x1 = cfg.objTilePxsCoordT()
  val size2d = (
    if (!isAffine) {
      cfg.objSize2dForAttrsT(
        //isAffine=isAffine
      )
    } else {
      cfg.objAffineSize2dForAttrsT()
    }
  )

  // whether or not to visibly flip x/y during rendering
  val dispFlip = Vec2(dataType=Bool())
  //--------
  //notSVmodport()
  //setAsSVstruct()
  //--------
}

case class Gpu2dObjAttrsStmPayload(
  cfg: Gpu2dConfig,
  isAffine: Boolean,
) extends Bundle {
  //--------
  val objAttrs = Gpu2dObjAttrs(
    cfg=cfg,
    isAffine=isAffine,
  )
  // `Mem` index
  val memIdx = UInt(
    (
      if (!isAffine) {
        cfg.objAttrsMemIdxWidth
      } else {
        cfg.objAffineAttrsMemIdxWidth
      }
    ) bits
  )
  // `Vec` index
  //val vecIdx = UInt(cfg.objAttrsMemIdxWidth bits)
  //--------
  //notSVmodport()
  //setAsSVstruct()
  //--------
}
case class Gpu2dPalEntry(
  cfg: Gpu2dConfig
) extends Bundle {
  //--------
  val col = Gpu2dRgba(rgbConfig=cfg.rgbConfig)
  //--------
  def rWidth = cfg.rgbConfig.rWidth
  def gWidth = cfg.rgbConfig.gWidth
  def bWidth = cfg.rgbConfig.bWidth
  //addGeneric(
  //  name="rWidth",
  //  that=rWidth,
  //  default="4",
  //)
  //addGeneric(
  //  name="gWidth",
  //  that=gWidth,
  //  default="4",
  //)
  //addGeneric(
  //  name="bWidth",
  //  that=bWidth,
  //  default="4",
  //)
  //tieIFParameter(
  //  signal=col,
  //  signalParam="rWidth",
  //  inputParam="rWidth",
  //)
  //tieIFParameter(
  //  signal=col,
  //  signalParam="gWidth",
  //  inputParam="gWidth",
  //)
  //tieIFParameter(
  //  signal=col,
  //  signalParam="bWidth",
  //  inputParam="bWidth",
  //)
  //notSVmodport()
  ////--------
  //notSVmodport()
  //setAsSVstruct()
  //--------
}
case class Gpu2dObjPalEntryStmPayload(
  cfg: Gpu2dConfig,
) extends Bundle {
  //--------
  val objPalEntry = Gpu2dPalEntry(cfg=cfg)
  val memIdx = UInt(memIdxWidth bits)
  //--------
  //--------
  def rWidth = cfg.rgbConfig.rWidth
  def gWidth = cfg.rgbConfig.gWidth
  def bWidth = cfg.rgbConfig.bWidth
  //addGeneric(
  //  name="rWidth",
  //  that=rWidth,
  //  default="4",
  //)
  //addGeneric(
  //  name="gWidth",
  //  that=gWidth,
  //  default="4",
  //)
  //addGeneric(
  //  name="bWidth",
  //  that=bWidth,
  //  default="4",
  //)
  //tieIFParameter(
  //  signal=objPalEntry,
  //  signalParam="rWidth",
  //  inputParam="rWidth",
  //)
  //tieIFParameter(
  //  signal=objPalEntry,
  //  signalParam="gWidth",
  //  inputParam="gWidth",
  //)
  //tieIFParameter(
  //  signal=objPalEntry,
  //  signalParam="bWidth",
  //  inputParam="bWidth",
  //)
  ////--------
  def memIdxWidth = cfg.objPalEntryMemIdxWidth
  //addGeneric(
  //  name="memIdxWidth",
  //  that=memIdxWidth,
  //  default="4",
  //)
  //tieGeneric(
  //  signal=memIdx,
  //  generic="memIdxWidth",
  //)
  //--------
  //notSVmodport()
  //setAsSVstruct()
  //--------
}

//case class Gpu2dPalEntry(
//  cfg: Gpu2dParams
//) extends Interface {
//  //--------
//  val col = Rgb(cfg.rgbConfig)
//  //--------
//}
case class Gpu2dBgPalEntryStmPayload(
  cfg: Gpu2dConfig,
) extends Bundle {
  //--------
  val bgPalEntry = Gpu2dPalEntry(cfg=cfg)
  val memIdx = UInt(memIdxWidth bits)
  //--------
  def rWidth = cfg.rgbConfig.rWidth
  def gWidth = cfg.rgbConfig.gWidth
  def bWidth = cfg.rgbConfig.bWidth
  //addGeneric(
  //  name="rWidth",
  //  that=rWidth,
  //  default="4",
  //)
  //addGeneric(
  //  name="gWidth",
  //  that=gWidth,
  //  default="4",
  //)
  //addGeneric(
  //  name="bWidth",
  //  that=bWidth,
  //  default="4",
  //)
  //tieIFParameter(
  //  signal=bgPalEntry,
  //  signalParam="rWidth",
  //  inputParam="rWidth",
  //)
  //tieIFParameter(
  //  signal=bgPalEntry,
  //  signalParam="gWidth",
  //  inputParam="gWidth",
  //)
  //tieIFParameter(
  //  signal=bgPalEntry,
  //  signalParam="bWidth",
  //  inputParam="bWidth",
  //)
  //--------
  def memIdxWidth = cfg.bgPalEntryMemIdxWidth
  //addGeneric(
  //  name="memIdxWidth",
  //  that=memIdxWidth,
  //  default="4",
  //)
  //tieGeneric(
  //  signal=memIdx,
  //  generic="memIdxWidth",
  //)
  //--------
  //notSVmodport()
  //setAsSVstruct()
  //--------
}
case class Gpu2dPopPayload(
  cfg: Gpu2dConfig,
) extends Bundle {
  //--------
  val ctrlEn = Bool()
  val col = Gpu2dRgba(rgbConfig=cfg.rgbConfig)
  //val physPxPos = cfg.physPxCoordT()
  //val intnlPxPos = cfg.bgPxsCoordT()
  //val tilePos = cfg.bgTilesCoordT()
  val physPosInfo = cfg.physPosInfoT()

  ////val bgPxsPos = cfg.bgPxsCoordT()
  ////val bgPxsPosInfo = cfg.bgPxsPosInfoT()
  //val bgPxsPosSlice = cfg.bgPxsPosSliceT()
  ////val bgTilesPos = cfg.bgTilesCoordT()
  //val bgTilesPosSlice = cfg.bgTilesPosSliceT()
  ////val bgTilesPosInfo = cfg.bgTilesPosInfoT()
  //--------
  //notSVmodport()
  //setAsSVstruct()
  //--------
}

//case class Gpu2dPushFlow[
//  DataT <: Data
//](
//  dataType: HardType[DataT],
//) extends Interface with IMasterSlave {
//  //--------
//  val valid = Bool()
//  val payload = dataType()
//  def fire = valid
//  //--------
//  override def asMaster(): Unit = mst
//  @modport
//  def mst = {
//    out(
//      valid,
//      payload,
//    )
//  }
//  //--------
//  @modport
//  def slv = {
//    in(
//      valid,
//      payload,
//    )
//  }
//  //--------
//  def <<(
//    that: Gpu2dPushFlow[DataT]
//  ) = {
//    this.valid := that.valid
//    this.payload := that.payload
//  }
//  def >>(
//    that: Gpu2dPushFlow[DataT]
//  ) = {
//    that.valid := this.valid
//    that.payload := this.payload
//  }
//}
//case class Gpu2dPushStream[
//  DataT <: Data
//](
//  dataType: HardType[DataT],
//) extends Interface with IMasterSlave {
//  //--------
//  val valid = Bool()
//  val payload = dataType()
//  val ready = Bool()
//  def fire = (valid && ready)
//  //--------
//  override def asMaster(): Unit = mst
//  @modport
//  def mst = {
//    out(
//      valid,
//      payload,
//    )
//    in(
//      ready
//    )
//  }
//  //--------
//  @modport
//  def slv = {
//    in(
//      valid,
//      payload,
//    )
//    out(
//      ready
//    )
//  }
//  //--------
//  def <<(
//    that: Gpu2dPushStream[DataT]
//  ) = {
//    this.valid := that.valid
//    this.payload := that.payload
//    that.ready := this.ready
//  }
//  def >>(
//    that: Gpu2dPushStream[DataT]
//  ) = {
//    that.valid := this.valid
//    that.payload := this.payload
//    this.ready := that.ready
//  }
//}
case class Gpu2dPushInp(
  cfg: Gpu2dConfig=DefaultGpu2dConfig(),
  dbgPipeMemRmw: Boolean,
) extends Bundle with IMasterSlave {
  //--------
  val colorMathTilePush = Flow(
    Gpu2dBgTileStmPayload(
      cfg=cfg,
      isColorMath=true,
    )
  )
  val colorMathEntryPush = Flow(
    Gpu2dBgEntryStmPayload(
      cfg=cfg,
      isColorMath=true,
    )
  )
  val colorMathAttrsPush = Flow(
    Gpu2dBgAttrsStmPayload(
      cfg=cfg,
      isColorMath=true,
    )
  )
  val colorMathPalEntryPush = Flow(
    Gpu2dBgPalEntryStmPayload(cfg=cfg)
  )
  //--------
  val bgTilePush = Flow(
    Gpu2dBgTileStmPayload(
      cfg=cfg,
      isColorMath=false,
    )
  )
  //val bgEntryPushArr = slave(
  //  Vec.fill(Flow(Gpu2dBgEntryStmPayload(
  //    cfg=cfg,
  //    isColorMath=false,
  //  )))
  //)
  val bgEntryPushVec = Vec.fill(cfg.numBgs)(
    Flow(Gpu2dBgEntryStmPayload(
      cfg=cfg,
      isColorMath=false,
    ))
      //.setName(f"bgEntryPushArr_$idx")
  )
  val bgAttrsPushVec = Vec.fill(cfg.numBgs)(
    Flow(Gpu2dBgAttrsStmPayload(
      cfg=cfg,
      isColorMath=false,
    ))
      //.setName(f"bgAttrsPushArr_$idx")
  )
  //val bgEntryPushVec = new ArrayBuffer[
  //  Flow[Gpu2dBgEntryStmPayload]
  //]()
  //val bgAttrsPushVec = new ArrayBuffer[
  //  Flow[Gpu2dBgAttrsStmPayload]
  //]()
  //for (idx <- 0 until cfg.numBgs) {
  //  //bgEntryPushVec += (
  //  //  Flow(Gpu2dBgEntryStmPayload(
  //  //    cfg=cfg,
  //  //    isColorMath=false,
  //  //  ))
  //  //    .setName(f"bgEntryPushArr_$idx")
  //  //)
  //  bgAttrsPushVec += (
  //    Flow(Gpu2dBgAttrsStmPayload(
  //      cfg=cfg,
  //      isColorMath=false,
  //    ))
  //      .setName(f"bgAttrsPushArr_$idx")
  //  )
  //}
  val bgPalEntryPush = Flow(Gpu2dBgPalEntryStmPayload(
    cfg=cfg
  ))
  val objTilePush = Stream(
    Gpu2dObjTileStmPayload(
      cfg=cfg,
      isAffine=false,
      dbgPipeMemRmw=dbgPipeMemRmw,
    )
  )
  val objAffineTilePush = Flow(
    Gpu2dObjTileStmPayload(
      cfg=cfg,
      isAffine=true,
      dbgPipeMemRmw=false,
    )
  )
  val objAttrsPush = Flow(
    Gpu2dObjAttrsStmPayload(
      cfg=cfg,
      isAffine=false,
    )
  )
  val objAffineAttrsPush = Flow(
    Gpu2dObjAttrsStmPayload(
      cfg=cfg,
      isAffine=true,
    )
  )
  val objPalEntryPush = Flow(Gpu2dObjPalEntryStmPayload(
    cfg=cfg
  ))
  //--------
  override def asMaster(): Unit = mst
  //@modport
  def mst = {
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
    for (idx <- 0 until cfg.numBgs) {
      master(
        bgEntryPushVec(idx),
        bgAttrsPushVec(idx),
      )
    }
  }
  //@modport
  def slv = {
    slave(
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
    for (idx <- 0 to cfg.numBgs - 1) {
      slave(
        bgEntryPushVec(idx),
        bgAttrsPushVec(idx),
      )
    }
  }
  slv
  //--------
  def <<(
    that: Gpu2dPushInp
  ): Unit = {
    this.colorMathTilePush << that.colorMathTilePush
    this.colorMathEntryPush << that.colorMathEntryPush
    this.colorMathAttrsPush << that.colorMathAttrsPush
    this.colorMathPalEntryPush << that.colorMathPalEntryPush
    this.bgTilePush << that.bgTilePush
    for (idx <- 0 to cfg.numBgs - 1) {
      this.bgEntryPushVec(idx) << that.bgEntryPushVec(idx)
      this.bgAttrsPushVec(idx) << that.bgAttrsPushVec(idx)
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
  //setDefinitionName(
  //  s"Gpu2dPushInp"
  //)
}
case class Gpu2dIo(
  cfg: Gpu2dConfig=DefaultGpu2dConfig(),
  dbgPipeMemRmw: Boolean,
) extends Bundle with IMasterSlave {
  //--------
  //val en = in Bool()
  //val blank = in(Vec2(Bool()))
  //val en = in Bool()
  //--------
  //val bgTilePush = slave Stream()
  //val bgTilePush = slave Stream()
  val push = slave(Gpu2dPushInp(
    cfg=cfg,
    dbgPipeMemRmw=dbgPipeMemRmw,
  ))
  def colorMathTilePush = push.colorMathTilePush
  def colorMathEntryPush = push.colorMathEntryPush 
  def colorMathAttrsPush = push.colorMathAttrsPush 
  def colorMathPalEntryPush = push.colorMathPalEntryPush 
  def bgTilePush = push.bgTilePush
  def bgEntryPushArr = push.bgEntryPushVec
  def bgAttrsPushArr = push.bgAttrsPushVec
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
  val pop = master(Stream(Gpu2dPopPayload(cfg=cfg)))
  //--------
  override def asMaster(): Unit = mst
  //@modport
  def mst = {
    slave(push)
    master(pop)
  }
  //@modport
  def slv = {
    master(push)
    slave(pop)
  }
  slv
  //notSVIF()
  //notSVmodport()
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
  //  for (idx <- 0 to cfg.numBgs - 1) {
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
  //setDefinitionName(
  //  s"Gpu2dIo"
  //)
}
case class Gpu2d(
  cfg: Gpu2dConfig=DefaultGpu2dConfig(),
  inSim: Boolean=false,
  vivadoDebug: Boolean=false,
  dbgPipeMemRmw: Boolean=false,
  //noAffineObjs: Boolean=false,
) extends Component {
  //--------
  def noColorMath = cfg.noColorMath
  def noAffineBgs = cfg.noAffineBgs
  def noAffineObjs = cfg.noAffineObjs
  //--------
  val io = master(Gpu2dIo(
    cfg=cfg,
    dbgPipeMemRmw=dbgPipeMemRmw,
  ))
  //io.notSVIF()
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
  //  //dataType=Rgb(cfg.rgbConfig),
  //  dataType=Gpu2dPopPayload(cfg=cfg),
  //  //depth=cfg.physFbSize2d.x,
  //  depth=cfg.physFbSize2d.x * 4 + 1,
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
  //  dataType=Gpu2dPopPayload(cfg=cfg)
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
    //  //wordType=UInt(cfg.palEntryMemIdxWidth bits),
    //  wordType=Gpu2dTile(cfg=cfg, isObj=false),
    //  //wordCount=cfg.numPxsForAllBgTiles,
    //  //wordCount=cfg.numTilesPerBg,
    //  wordCount=cfg.numBgTiles,
    //)
    //  .initBigInt(Array.fill(cfg.numBgTiles)(BigInt(0)))
    //  .addAttribute("ram_style", cfg.bgTileArrRamStyle)
    //val colorMathTileMemArr = new ArrayBuffer[
    //  FpgacpuRamSimpleDualPort[Gpu2dTile]
    //]()
    val myBgTileMemInit = Gpu2dTest.bgTileMemInit(
      cfg=cfg,
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
      jdx <- 0 until cfg.numBgMemsPerNonPalKind
      //jdx <- 0 until cfg.numBgTileMems
      // END: debug comment this out; later
      //--------
      //jdx <- 0 until cfg.bgTileSize2d.x
      //--------
    ) {
      def tempNumBgTileSlices = (
        1 << cfg.bgTileMemIdxWidth
        //1 << cfg.bgTileGridMemIdxWidth
      )
      bgTileMemArr += FpgacpuRamSimpleDualPort(
        //wordType=Gpu2dTileFull(
        //  cfg=cfg,
        //  isObj=false,
        //  isAffine=false,
        //),
        wordType=Gpu2dTileSlice(
          cfg=cfg,
          isObj=false,
          isAffine=false,
        ),
        depth=(
          //cfg.numBgTiles
          tempNumBgTileSlices
        ),
        //initBigInt=(
        //  Some(
        //    Array.fill(tempNumBgTileSlices)(
        //      //Gpu2dTileSlice(
        //      //  cfg=cfg,
        //      //  isObj=false,
        //      //  isAffine=false
        //      //).getZero
        //      BigInt(0)
        //    )
        //  )
        //),
        init={
          cfg.bgTileMemInit match {
            case Some(bgTileMemInit) => {
              Some(bgTileMemInit/*(jdx)*/)
            }
            case None => {
              Some(
                myBgTileMemInit//(jdx)
                ////Gpu2dTest.doSplitBgTileMemInit(
                ////  cfg=cfg,
                ////  //gridIdx=jdx,
                ////  //isColorMath=false,
                ////)(jdx)
                //Array.fill(tempNumBgTileSlices)(
                //  Gpu2dTileSlice(
                //    cfg=cfg,
                //    isObj=false,
                //    isAffine=false
                //  ).getZero
                //)
              )
              //None
            }
          }
        },
        arrRamStyle=cfg.bgTileArrRamStyle,
      )
        .setName(f"bgTileMemArr_$jdx")
      bgTileMemArr(jdx).io.wrEn := bgTilePush.fire
      bgTileMemArr(jdx).io.wrAddr := bgTilePush.payload.memIdx
      bgTileMemArr(jdx).io.wrData := bgTilePush.payload.tileSlice.asBits
      //when (
      //  bgTilePush.payload.memIdx(
      //    //jdx
      //    //cfg.bgTileGridMemIdxWidth
      //    cfg.bgTileSize2dPow.y
      //    downto cfg.bgTileSize2dPow.y
      //    //bgTilePush.payload.memIdx.high
      //    //downto bgTilePush.payload.memIdx.high
      //  ) === jdx //=== (if (jdx == 1) {True} else {False})
      //) {
      //  bgTileMemArr(jdx).io.wrEn := bgTilePush.fire
      //  bgTileMemArr(jdx).io.wrAddr := Cat(
      //    bgTilePush.payload.memIdx(
      //      //bgTilePush.payload.memIdx.high downto 1
      //      bgTilePush.payload.memIdx.high 
      //      downto cfg.bgTileSize2dPow.y + 1
      //    ),
      //    bgTilePush.payload.memIdx(
      //      cfg.bgTileSize2dPow.y
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
    //  //wordType=UInt(cfg.palEntryMemIdxWidth bits),
    //  wordType=Gpu2dTile(cfg=cfg, isObj=true),
    //  //wordCount=cfg.numPxsForAllObjTiles,
    //  wordCount=cfg.numObjTiles,
    //)
    //  .initBigInt(Array.fill(cfg.numObjTiles)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", cfg.objTileArrRamStyle)
    val objTileMemArr = new ArrayBuffer[
      //FpgacpuRamSimpleDualPort[Gpu2dTileFull]
      FpgacpuRamSimpleDualPort[Gpu2dTileSlice]
    ]()
    for (idx <- 0 until cfg.numObjMemsPerKind(isTileMem=true)) {
      def tempNumObjTileSlices = (
        //if (idx == 0) {
          //cfg.numObjTiles
          1 << cfg.objTileSliceMemIdxWidth
        //} else {
        //  //cfg.numObjAffineTiles
        //  1 << cfg.objAffineTileMemIdxWidth
        //}
      )
      objTileMemArr += FpgacpuRamSimpleDualPort(
        //wordType=Gpu2dTileFull(
        //  cfg=cfg,
        //  isObj=true,
        //  isAffine=idx != 0,
        //),
        wordType=Gpu2dTileSlice(
          cfg=cfg,
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
          cfg.objTileMemInit match {
            case Some(objTileMemInit) => {
              Some(objTileMemInit)
            }
            case None => {
              //val tempArr = new ArrayBuffer[Gpu2dTileSlice]()
              //for (kdx <- 0 until tempNumObjTileSlices) {
              //  tempArr += Gpu2dTileSlice(
              //    cfg=cfg,
              //    isObj=true,
              //    isAffine=idx != 0,
              //  ).getZero
              //}
              //Some(tempArr)
              //Some(Array.fill(tempNumObjTileSlices)((0)).toSeq)
              Some(Gpu2dTest.objTileMemInit(cfg=cfg))
              //None
            }
          }
        },
        arrRamStyle=cfg.objTileArrRamStyle,
      )
        .setName(f"objTileMemArr_$idx")
      //val rdAddrObjTileMem = UInt(cfg.numObjTilesPow bits)
      //val rdDataObjTileMem = Gpu2dTile(cfg=cfg, isObj=true)
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
        objTileMemArr(idx).io.wrData := (
          objTilePush.payload.tileSlice.asBits
        )
        //when (objTilePush.fire) {
        //  objTileMem.write(
        //    address=objTilePush.payload.memIdx,
        //    data=objTilePush.payload.tile,
        //  )
        //}
      }
      //else {
      //  //def wordType() = Gpu2dObjTileStmPayload(
      //  //  cfg=cfg,
      //  //  isAffine=false,
      //  //)
      //  def wordType() = Gpu2dTileSlice(
      //    cfg=cfg,
      //    isObj=true,
      //    isAffine=idx != 0,
      //    doPipeMemRmw=true,
      //  )
      //  def wordCount = (
      //    1 << cfg.objTileSliceMemIdxWidth
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
      //    // only up to `min(cfg.numObjTiles, N)` tiles
      //    min(cfg.numObjTiles, 4)
      //    * cfg.objTileSize2d.y * (1 << cfg.objTileWidthRshift)
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
      //    //  cfg=cfg,
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
    for (idx <- 0 until cfg.objAffineSliceTileWidth) {
      def tempNumObjTileSlices = (
        //if (idx == 0) {
        //  //cfg.numObjTiles
        //  1 << cfg.objTileSliceMemIdxWidth
        //} else {
        //  //cfg.numObjAffineTiles
          1 << cfg.objAffineTilePxMemIdxWidth
        //}
      )
      objAffineTileMemArr += FpgacpuRamSimpleDualPort(
        wordType=UInt(cfg.objPalEntryMemIdxWidth bits),
        depth=tempNumObjTileSlices,

        //initBigInt={
        //  Some(Array.fill(tempNumObjTileSlices)(BigInt(0)).toSeq)
        //},
        initBigInt={
          if (!cfg.noAffineObjs) {
            cfg.objAffineTileMemInit match {
              case Some(objAffineTileMemInit) => {
                Some(objAffineTileMemInit)
              }
              case None => {
                //val tempArr = new ArrayBuffer[Gpu2dTileSlice]()
                //for (kdx <- 0 until tempNumObjTileSlices) {
                //  tempArr += Gpu2dTileSlice(
                //    cfg=cfg,
                //    isObj=true,
                //    isAffine=idx != 0,
                //  ).getZero
                //}
                //Some(tempArr)
                //Some(Array.fill(tempNumObjTileSlices)((0)).toSeq)
                Some(Gpu2dTest.objAffineTileMemInit(cfg=cfg))
                //None
                //cfg.objAffineTileMemInit
              }
            }
          } else {
            cfg.objAffineTileMemInit
          }
        },

        //init={
        //  //val temp = new ArrayBuffer[]()
        //  //for (_ <- 0 until tempNumObjTileSlices) {
        //  //  temp += (0)
        //  //}
        //  //Some(temp)
        //  cfg.objAffineTileMemInit match {
        //    case Some(objAffineTileMemInit) => {
        //      Some(objAffineTileMemInit)
        //    }
        //    case None => {
        //      //Some(Array.fill(tempNumObjTileSlices)(
        //      //  (0)
        //      //).toSeq)
        //      //Gpu2dTest.objTileMemInit(cfg=cfg)
        //      val tempArr = new ArrayBuffer[UInt]()
        //      for (
        //        jdx <- 0
        //        until (
        //          cfg.objPalEntryMemIdxWidth
        //        )
        //      ) {
        //        for (
        //          kdx <- 0 until cfg.objPalEntryMemIdxWidth
        //        ) {
        //        }
        //      }
        //      Some(tempArr.toSeq)
        //    }
        //  }
        //  //Some(
        //  //  Array.fill(tempNumObjTileSlices)(
        //  //    U(s"${cfg.objPalEntryMemIdxWidth}'d0")
        //  //  ).toSeq
        //  //)
        //},
        arrRamStyle=cfg.objAffineTileArrRamStyle,
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
          //      //cfg.objAffineTileWidthRshift
          //      //cfg.objAffineTileSize2dPow.x
          //      cfg.objAffineSliceTileWidthPow
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
        objAffineTilePush.payload.tilePx.asBits
        //objAffineTilePush.payload.tileSlice.getPx(idx)
      )
    }
    //--------
    //val bgEntryMem = Mem(
    //  wordType=Gpu2dBgEntry(cfg=cfg),
    //  wordCount=cfg.numColsInPal,
    //)
    //  .addAttribute("ram_style", cfg.palEntryArrRamStyle)
    //--------
    val colorMathTileMemArr = new ArrayBuffer[
      //FpgacpuRamSimpleDualPort[Gpu2dTileFull]
      FpgacpuRamSimpleDualPort[Gpu2dTileSlice]
    ]()
    if (!noColorMath) {
      def tempNumColorMathTileSlices = (
        1 << cfg.colorMathTileMemIdxWidth
      )
      for (
        jdx <- 0 until cfg.numBgMemsPerNonPalKind
      ) {
        colorMathTileMemArr += FpgacpuRamSimpleDualPort(
          //dataType=CombinePipePayload(),
            //wordType=Gpu2dTileFull(
            //  cfg=cfg,
            //  isObj=false,
            //  isAffine=false,
            //),
            wordType=Gpu2dTileSlice(
              cfg=cfg,
              isObj=false,
              isAffine=false,
            ),
            depth=(
              //cfg.numColorMathTiles
              tempNumColorMathTileSlices
            ),
            init={
              //val temp = new ArrayBuffer[]()
              //for (
              //  //idx <- 0 until cfg.numColorMathTiles
              //  idx <- 0 until tempNumColorMathTileSlices
              //) {
              //  temp += (0)
              //}
              //Some(temp)
              //Some(Gpu2dTest.bgTileMemInit(cfg=cfg))
              cfg.colorMathTileMemInit match {
                case Some(colorMathTileMemInit) => {
                  Some(colorMathTileMemInit/*(jdx)*/)
                }
                case None => {
                  //Some(Array.fill(tempNumColorMathTileSlices)(
                  //  BigInt(0)
                  //))
                  //Some(Gpu2dTest.bgTileMemInit(cfg=cfg))
                  //Some(
                  //  //Gpu2dTest.doSplitBgTileMemInit(
                  //  //  cfg=cfg,
                  //  //  gridIdx=jdx,
                  //  //  //isColorMath=true,
                  //  //)
                  //  myBgTileMemInit//(jdx)
                  //)
                  //None
                  val temp = new ArrayBuffer[Gpu2dTileSlice]()
                  for (
                    //idx <- 0 until cfg.numColorMathTiles
                    idx <- 0 until tempNumColorMathTileSlices
                  ) {
                    //temp += BigInt(0)
                    temp += Gpu2dTileSlice(
                      cfg=cfg,
                      isObj=false,
                      isAffine=false,
                    ).getZero
                  }
                  Some(temp)
                }
              }
            },
            arrRamStyle=cfg.bgTileArrRamStyle,
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
        colorMathTileMem.io.wrAddr := colorMathTilePush.payload.memIdx
        //colorMathTileMem.io.wrData := colorMathTilePush.tile
        colorMathTileMem.io.wrData := (
          colorMathTilePush.payload.tileSlice.asBits
        )
        //colorMathTileMem.io.wrPulse.valid := colorMathTilePush.fire
        //colorMathTileMem.io.wrPulse.addr := colorMathTilePush.memIdx
        //colorMathTileMem.io.wrPulse.data := colorMathTilePush.tile
      }
    }

    val colorMathPalEntryMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dPalEntry]
    ]()
    if (!noColorMath) {
      for (jdx <- 0 until cfg.bgTileSize2d.x) {
        colorMathPalEntryMemArr += FpgacpuRamSimpleDualPort(
          //dataType=CombinePipePayload(),
          wordType=Gpu2dPalEntry(cfg=cfg),
          depth=cfg.numColsInBgPal,
          //initBigInt={
          //  val temp = new ArrayBuffer[BigInt]()
          //  for (idx <- 0 until cfg.numColsInBgPal) {
          //    temp += BigInt(0)
          //  }
          //  Some(temp)
          //},
          initBigInt={
            cfg.colorMathPalEntryMemInitBigInt match {
              case Some(colorMathPalEntryMemInitBigInt) => {
                Some(colorMathPalEntryMemInitBigInt)
              }
              case None => {
                //Some(Array.fill(cfg.numColsInBgPal)(
                //  (0)
                //).toSeq)
                Some(Gpu2dTest.bgPalMemInitBigInt(cfg=cfg))
                //Some(Gpu2dTest.somePalMemInit(
                //  cfg=cfg,
                //  someBigIntArr=(
                //    Gpu2dTest.bgPalMemInitBigInt(cfg=cfg)
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
          arrRamStyle=cfg.bgPalEntryArrRamStyle,
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
          colorMathPalEntryPush.payload.memIdx
        )
        colorMathPalEntryMemArr(jdx).io.wrData := (
          colorMathPalEntryPush.payload.bgPalEntry.asBits
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
        //jdx <- 0 until cfg.numColorMathMemsPerNonPalKind
        jdx <- 0 until cfg.numBgMemsPerNonPalKind
      ) {
        colorMathEntryMemArr += FpgacpuRamSimpleDualPort(
          //dataType=CombinePipePayload(),
          wordType=Gpu2dBgEntry(
            cfg=cfg,
            isColorMath=true,
          ),
          depth=(
            cfg.numTilesPerBg >> 1
          ),
          initBigInt={
            val temp = new ArrayBuffer[BigInt]()
            for (_ <- 0 until (cfg.numTilesPerBg >> 1)) {
              temp += BigInt(0)
            }
            Some(temp)
            //Some(Array.fill(cfg.numTilesPerBg)(BigInt(0)).toSeq)
          },
          arrRamStyle=cfg.bgEntryArrRamStyle,
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
          colorMathEntryPush.payload.memIdx(
            colorMathEntryPush.payload.memIdx.high downto 1
          )
        )
        colorMathEntryMemArr(jdx).io.wrData := (
          colorMathEntryPush.payload.bgEntry.asBits
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
      cfg=cfg,
      isColorMath=true,
    ))
    if (!noColorMath) {
      colorMathAttrs.init(colorMathAttrs.getZero)
      when (colorMathAttrsPush.fire) {
        colorMathAttrs := colorMathAttrsPush.payload.bgAttrs
      }
    }
    //--------
    //val bgEntryMemArr = new ArrayBuffer[Mem[Gpu2dBgEntry]]()
    val bgEntryMemA2d = new ArrayBuffer[ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dBgEntry]
    ]]()
    //val rdDataBgEntryMemArr = Vec.fill(cfg.numBgs)(Gpu2dBgEntry(
    //  cfg=cfg
    //))
    //val rdAddrBgEntryMemArr = Vec.fill(cfg.numBgs)(
    //  UInt(log2Up(cfg.numTilesPerBg) bits)
    //)
    //val rdBgEntryMemArr = new ArrayBuffer[MultiMemReadSync[Gpu2dBgEntry]]()
    //object RdBgEntryMemArrInfo {
    //  def wrBgIdx = 0
    //  def numReaders = 1
    //}
    val bgAttrsArr = new ArrayBuffer[Gpu2dBgAttrs]()
    for (idx <- 0 until cfg.numBgs) {
      //--------
      //bgEntryMemArr += Mem(
      //  wordType=Gpu2dBgEntry(cfg=cfg),
      //  wordCount=cfg.numTilesPerBg,
      //)
      //  .initBigInt(Array.fill(cfg.numTilesPerBg)(BigInt(0)).toSeq)
      //  .addAttribute("ram_style", cfg.bgEntryArrRamStyle)
      //  .setName(f"bgEntryMemArr_$idx")
      bgEntryMemA2d += new ArrayBuffer[
        FpgacpuRamSimpleDualPort[Gpu2dBgEntry]
      ]()
      for (
        //--------
        // BEGIN: debug comment this out; later
        jdx <- 0 until cfg.numBgMemsPerNonPalKind
        //jdx <- 0 until cfg.numBgEntryMems
        // END: debug comment this out; later
        //--------
        //jdx <- 0 until cfg.bgTileSize2d.x
        //--------
      ) {
        def bgEntryMemArr = bgEntryMemA2d(idx)
        bgEntryMemArr += FpgacpuRamSimpleDualPort(
          wordType=Gpu2dBgEntry(
            cfg=cfg,
            isColorMath=false,
          ),
          depth=(cfg.numTilesPerBg >> 1),
          initBigInt={
            //val temp = new ArrayBuffer[BigInt]()
            //for (_ <- 0 until (cfg.numTilesPerBg >> 1)) {
            //  temp += BigInt(0)
            //}
            //Some(temp)
            Some(Array.fill(cfg.numTilesPerBg >> 1)(BigInt(0)).toSeq)
          },
          arrRamStyle=cfg.bgEntryArrRamStyle,
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
            bgEntryPushArr(idx).payload.bgEntry.asBits
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
        cfg=cfg,
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
    //  wordType=Gpu2dBgAttrs(cfg=cfg),
    //  wordCount=cfg.numBgs,
    //)
    //  .initBigInt(Array.fill(cfg.numBgs)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", cfg.bgAttrsArrRamStyle)
    //--------
    //val objAttrsMem = Mem(
    //  wordType=Gpu2dObjAttrs(cfg=cfg),
    //  wordCount=cfg.numObjs,
    //)
    //  .initBigInt(Array.fill(cfg.numObjs)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", cfg.objAttrsArrRamStyle)
    val objAttrsMemArr = new ArrayBuffer[
      FpgacpuRamSimpleDualPort[Gpu2dObjAttrs]
    ]()
    for (idx <- 0 until cfg.numObjMemsPerKind(isTileMem=false)) {
      def tempNumObjs = (
        if (idx == 0) {
          cfg.numObjs
        } else {
          cfg.numObjsAffine
        }
      )
      objAttrsMemArr += FpgacpuRamSimpleDualPort(
        wordType=Gpu2dObjAttrs(
          cfg=cfg,
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
        arrRamStyle=cfg.objAttrsArrRamStyle,
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
      objAttrsMemArr(idx).io.wrData := (
        tempObjAttrsPush.payload.objAttrs.asBits
      )
      //when (objAttrsPush.fire) {
      //  objAttrsMem.write(
      //    address=objAttrsPush.payload.memIdx,
      //    data=objAttrsPush.payload.objAttrs,
      //  )
      //}
    }
    //val objAttrsVec = Vec.fill(cfg.numObjs)(
    //  Reg(Gpu2dObjAttrs(cfg=cfg))
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
    for (jdx <- 0 until cfg.bgTileSize2d.x) {
      bgPalEntryMemArr += FpgacpuRamSimpleDualPort(
        wordType=Gpu2dPalEntry(cfg=cfg),
        depth=cfg.numColsInBgPal,
        initBigInt={
          //val temp = new ArrayBuffer[]()
          //for (idx <- 0 until cfg.numColsInBgPal) {
          //  temp += (0)
          //}
          //Some(temp)
          cfg.bgPalEntryMemInitBigInt match {
            case Some(bgPalEntryMemInitBigInt) => {
              Some(bgPalEntryMemInitBigInt)
            }
            case None => {
              //Some(Array.fill(cfg.numColsInBgPal)((0)).toSeq)
              //Some(Gpu2dTest.somePalMemInit(
              //  cfg=cfg,
              //  someBigIntArr=Gpu2dTest.bgPalMemInitBigInt(
              //    cfg=cfg,
              //  )
              //))
              Some(Gpu2dTest.bgPalMemInitBigInt(cfg=cfg).toSeq)
            }
          }
        },
        arrRamStyle=cfg.bgPalEntryArrRamStyle,
      )
        .setName(f"bgPalEntryMemArr_$jdx")
      bgPalEntryMemArr(jdx).io.wrEn := bgPalEntryPush.fire
      bgPalEntryMemArr(jdx).io.wrAddr := bgPalEntryPush.payload.memIdx
      bgPalEntryMemArr(jdx).io.wrData := (
        bgPalEntryPush.payload.bgPalEntry.asBits
      )
    }
    //val bgPalEntryMem = Mem(
    //  wordType=Gpu2dPalEntry(cfg=cfg),
    //  wordCount=cfg.numColsInBgPal,
    //)
    //  .initBigInt(Array.fill(cfg.numColsInBgPal)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", cfg.bgPalEntryArrRamStyle)
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
    for (idx <- 0 until cfg.numObjMemsPerKind(isTileMem=false)) {
      objPalEntryMemA2d += new ArrayBuffer[
        FpgacpuRamSimpleDualPort[Gpu2dPalEntry]
      ]()
      def objPalEntryMemArr = objPalEntryMemA2d(idx) 
      for (
        x <- 0 until (
          //if (idx == 0) {
          //  cfg.objTileSize2d.x
          //} else {
          //  cfg.objAffineDblTileSize2d.x
          //}
          //cfg.tempObjTileWidth1(idx != 0)
          cfg.tempObjTileWidth(idx != 0)
        )
      ) {
        objPalEntryMemArr += FpgacpuRamSimpleDualPort(
          wordType=Gpu2dPalEntry(cfg=cfg),
          depth=cfg.numColsInObjPal,
          initBigInt={
            //val temp = new ArrayBuffer[]()
            //for (idx <- 0 until cfg.numColsInObjPal) {
            //  temp += (0)
            //}
            //Some(temp)
            cfg.objPalEntryMemInitBigInt match {
              case Some(objPalEntryMemInitBigInt) => {
                Some(objPalEntryMemInitBigInt)
              }
              case None => {
                //Some(Array.fill(cfg.numColsInObjPal)(BigInt(0)).toSeq)
                //Some(Array.fill(cfg.numColsInBgPal)((0)).toSeq)
                //Some(Gpu2dTest.somePalMemInit(
                //  cfg=cfg,
                //  someBigIntArr=Gpu2dTest.objPalMemInitBigInt(
                //    cfg=cfg,
                //  )
                //))
                Some(Gpu2dTest.objPalMemInitBigInt(cfg=cfg).toSeq)
              }
            }
          },
          arrRamStyle=cfg.objPalEntryArrRamStyle,
        )
          .setName(f"objPalEntryMemArr_$idx" + f"_$x")
        objPalEntryMemArr(x).io.wrEn := objPalEntryPush.fire
        objPalEntryMemArr(x).io.wrAddr := objPalEntryPush.payload.memIdx
        objPalEntryMemArr(x).io.wrData := (
          objPalEntryPush.payload.objPalEntry.asBits
        )
      }
    }
    //val objPalEntryMem = Mem(
    //  wordType=Gpu2dPalEntry(cfg=cfg),
    //  wordCount=cfg.numColsInObjPal,
    //)
    //  .initBigInt(Array.fill(cfg.numColsInObjPal)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", cfg.objPalEntryArrRamStyle)
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
    //  Reg(UInt(log2Up(cfg.oneLineMemSize) + 1 bits))
    //  init(cfg.oneLineMemSize - 1)
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
      //  rPastOutp.bgPxsPosSlice.pastPos.y === cfg.intnlFbSize2d.y - 1
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
      someSize2d=cfg.intnlFbSize2d
    )
    val wrObjCalcPos = LcvVideoCalcPos(
      someSize2d=cfg.intnlFbSize2d
    )
    val combineCalcPos = LcvVideoCalcPos(
      someSize2d=cfg.intnlFbSize2d
    )

    //val wrBgIntnlCalcPos = LcvVideoCalcPos(
    //  someSize2d=cfg.intnlFbSize2d
    //)
    //wrIntnlCalcPos.io.en := !rWrBgChangingRow

    val rdPhysCalcPos = LcvVideoCalcPos(
      someSize2d=cfg.physFbSize2d
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
    //  someSize2d=cfg.fbSize2dInPxs,
    //  someScalePow=cfg.physToIntnlScalePow,
    //  //thatSomeSize2d=cfg.bg
    //)
    //outp.bgTilesPosSlice := outp.physPosInfo.posSlice(
    //  someSize2d=cfg.fbSize2dInBgTiles,
    //  someScalePow=cfg.physToBgTilesScalePow,
    //)

    //case class LineMemEntry() extends Interface {
    //  val bgColVec = Vec(Gpu2dRgba(rgbConfig=cfg.rgbConfig), cfg.numBgs)
    //  val objColVec = Vec(Gpu2dRgba(rgbConfig=cfg.rgbConfig), cfg.numBgs)
    //}
    //case class LineMemEntry() extends Interface {
    //  val col = Vec.fill(cfg.numBgs)(Gpu2dRgba(rgbConfig=cfg.rgbConfig))
    //  //val prio = Vec.fill(cfg.numBgs)(UInt(cfg.numBgsPow bits))
    //  //val bgIdx = 
    //  val prio = Vec.fill(cfg.numBgs)(UInt(cfg.numBgsPow bits))
    //  //val col = Gpu2dRgba(rgbConfig=cfg.rgbConfig)
    //  //val prio = UInt(cfg.numBgsPow bits)
    //}
    //case class BgSubLineMemEntry() extends Interface {
    //  val col = Gpu2dRgba(rgbConfig=cfg.rgbConfig)
    //  val prio = UInt(cfg.numBgsPow bits)
    //}

    //case class ColorMathLineMemEntry() extends Interface {
    //  val col = Gpu2dRgba(rgbConfig=cfg.rgbConfig)
    //}
    def ColorMathSubLineMemEntry() = (!noColorMath) generate (
      Gpu2dRgba(rgbConfig=cfg.rgbConfig)
    )
    case class BgSubLineMemEntry() extends Bundle {
      val col = Gpu2dRgba(rgbConfig=cfg.rgbConfig)
      val prio = UInt(cfg.numBgsPow bits)
      //val addr = UInt(cfg.bgSubLineMemArrSizePow bits)
      val addr = UInt(log2Up(cfg.oneLineMemSize) bits)
      def getSubLineMemTempArrIdx() = (
        cfg.getBgSubLineMemArrIdx(addr=addr)
      )
      val colorMathInfo = (!noColorMath) generate (
        Gpu2dColorMathInfo(cfg=cfg)
      )
      val colorMathCol = (!noColorMath) generate (
        Gpu2dRgba(rgbConfig=cfg.rgbConfig)
      )
      //val addr = UInt(log2Up(cfg.oneLineMemSize) bits)
      //def getSubLineMemTempArrIdx() = cfg.getBgSubLineMemTempArrIdx(
      //  pxPosX=addr
      //)
      //def getSubLineMemTempAddr() = cfg.getBgSubLineMemTempAddr(
      //  pxPosX=addr
      //)
      //notSVmodport()
      //setAsSVstruct()
    }
    case class ObjSubLineMemEntry() extends Bundle {
      val col = Gpu2dRgba(rgbConfig=cfg.rgbConfig)
      //val rawPrio = UInt((cfg.numBgsPow + 1) bits)
      val written = Bool()
      val prio = UInt(cfg.numBgsPow bits)
      //def objAttrsMemIdx
      val addr = (inSim) generate UInt(log2Up(cfg.oneLineMemSize) bits)
      def getSubLineMemTempArrIdx() = (
        cfg.getObjSubLineMemArrIdx(addr=addr)
      )
      val colorMathInfo = (!noColorMath) generate (
        Gpu2dColorMathInfo(cfg=cfg)
      )
      val objIdx = UInt(
        (
          cfg.objAttrsMemIdxWidth
          .max(cfg.objAffineAttrsMemIdxWidth)
        )
        bits
      )
      //notSVmodport()
      //setAsSVstruct()
      //val addr = UInt(log2Up(cfg.oneLineMemSize) bits)
      //def getSubLineMemTempArrIdx() = cfg.getObjSubLineMemTempArrIdx(
      //  pxPosX=addr
      //)
      //def getSubLineMemTempAddr() = cfg.getObjSubLineMemTempAddr(
      //  pxPosX=addr
      //)
      //val written = Bool() // only used for prio
      //val prio = UInt((cfg.numBgsPow + 1) bits)
      //val objIdx = UInt(cfg.numObjsPow bits)
    }
    //// Which sprites have we drawn this scanline?
    //// This needs to be reset to 0x0 upon upon starting rendering the next 
    //// scanline. 
    //// Note that this may not scale well to very large numbers of sprites,
    //// but at that point you may be better off with a GPU for 3D graphics.
    //val rObjsDrawnThisLine = Reg(Bits(cfg.numObjs bits)) init(0x0)
    //val rObjDrawnPosXVec = Reg(
    //  //Vec(UInt(log2Up(cfg.intnlFbSize2d.x) bits), cfg.numObjs)
    //)
    //for (idx <- 0 to rObjDrawnPosXVec.size - 1) {
    //  rObjDrawnPosXVec(idx).init(rObjDrawnPosXVec(idx).getZero)
    //}

    //def LineMemEntry() = Vec.fill(cfg.numBgs)(Gpu2dRgba(rgbConfig=cfg.rgbConfig))
    //def LineMemEntry() = Gpu2dRgba(rgbConfig=cfg.rgbConfig)
    //val lineMemArr = new ArrayBuffer[Mem[BgSubLineMemEntry]]()

    //case class LineFifoEntry() extends Interface {
    //  val bgLineMemEntry = BgSubLineMemEntry()
    //  val objLineMemEntry = ObjSubLineMemEntry()
    //}
    //val lineFifo = AsyncReadFifo(
    //  dataType=LineFifoEntry(),
    //  depth=cfg.lineFifoDepth,
    //)

    //val lineFifo = AsyncReadFifo(
    //  dataType=BgSubLineMemEntry(),
    //  //depth=cfg.oneLineMemSize * 2 + 1,
    //  depth=cfg.lineFifoDepth,
    //)
    //val objLineFifo = AsyncReadFifo(
    //  dataType=ObjSubLineMemEntry(),
    //  //depth=cfg.oneLineMemSize * 2 + 1,
    //  depth=cfg.lineFifoDepth,
    //)


    //val bgLineMem = Mem(
    //  wordType=BgSubLineMemEntry(),
    //  wordCount=(
    //    cfg.oneLineMemSize
    //  )
    //)
    //def combineRdSubLineFifoDepth = 16
    //def combineRdSubLineFifoAmountCanPushStall = 8
    //val combineBgRdSubLineFifo = new AsyncReadFifo(
    //  dataType=Vec.fill(cfg.bgTileSize2d.x)(BgSubLineMemEntry()),
    //  depth=combineRdSubLineFifoDepth,
    //)
    //val combineObjRdSubLineFifo = new AsyncReadFifo(
    //  dataType=Vec.fill(cfg.bgTileSize2d.x)(ObjSubLineMemEntry()),
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
    // BEGIN: don't remove this
    //val wrObjSubLineMemArr = new ArrayBuffer[
    //  //FpgacpuRamSimpleDualPort[Vec[ObjSubLineMemEntry]]
    //  PipeMemRmw[
    //    Vec[ObjSubLineMemEntry],
    //    WrObjPipeSlmRmwHazardCmp,
    //    WrObjPipePayload,
    //    PipeMemRmwDualRdTypeDisabled[
    //      Vec[ObjSubLineMemEntry],
    //      WrObjPipeSlmRmwHazardCmp,
    //    ]
    //  ]
    //]()
    // END: don't remove this
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

    //val objSubLineMemIoV2d = Vec.fill(cfg.numLineMemsPerBgObjRenderer)(
    //  Vec.fill(RdObjSubLineMemArrInfo.numReaders)(
    //    WrPulseRdPipeSimpleDualPortMemIo(
    //      dataType=HardType.union(
    //        WrObjPipePayload(),
    //        CombinePipePayload(),
    //      ),
    //      wordType=Vec.fill(cfg.objTileSize2d.x)(ObjSubLineMemEntry()),
    //      depth=cfg.objSubLineMemArrSize,
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
      log2Up(cfg.numLineMemsPerBgObjRenderer)
    )
    def fullLineMemArrIdxWidth = (
      log2Up(cfg.numLineMemsPerBgObjRenderer)
      //+ log2Up(cfg.physFbSize2dScale.y)
    )
    ////def rdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    ////def combineWrLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd1")
    ////def combineRdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd2")
    ////def wrFullLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd3")
    //def combineRdLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")
    def combineFullLineMemArrIdxInit = (
      U({
        //val temp = 0 << log2Up(cfg.physFbSize2dScale.y)
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
        //val temp = 1 << log2Up(cfg.physFbSize2dScale.y)
        val temp = 1
        s"$fullLineMemArrIdxWidth'd$temp"
      })
    )
    //def wrFullLineMemArrIdxInit = U(f"$lineMemArrIdxWidth'd0")

    //val rSendIntoFifoLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(cfg.numLineMemsPerBgObjRenderer) bits))
    //  init(sendIntoFifoLineMemArrIdxInit)
    //)

    //val rCombineFullLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(cfg.numLineMemsPerBgObjRenderer) bits))
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
    //  cfg.physFbSize2dScale.y > 1
    //) generate (
    //  KeepAttribute(
    //    Vec.fill(32)(
    //      Reg(
    //        SInt(log2Up(cfg.physFbSize2dScale.y) + 1 bits),
    //        //init=0x0,
    //      ) init(cfg.physFbSize2dScale.y - 1)
    //    )
    //  )
    //)
    //--------
    val rWrObjWriterFullLineMemArrIdx = Vec.fill(
      cfg.numLineMemsPerBgObjRenderer
    )(
      mkFullLineMemIdx(wrFullLineMemArrIdxInit)
    )
    val wrObjWriterLineMemArrIdx = Vec.fill(
      cfg.numLineMemsPerBgObjRenderer
    )(
      //mkLineMemIdx(wrFullLineMemArrIdxInit)
      UInt(lineMemArrIdxWidth bits)
    )
    for (zdx <- 0 until cfg.numLineMemsPerBgObjRenderer) {
      wrObjWriterLineMemArrIdx(zdx) := (
        rWrObjWriterFullLineMemArrIdx(zdx)
        //(
        //  rWrObjWriterFullLineMemArrIdx(zdx).high
        //  downto log2Up(cfg.physFbSize2dScale.y)
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
        //  downto log2Up(cfg.physFbSize2dScale.y)
        //  //downto 0
        //)
      )
    }
    //--------
    val rWrFullBgPipeLineMemArrIdx = Vec.fill(
      cfg.numLineMemsPerBgObjRenderer
    )(
      mkFullLineMemIdx(wrFullLineMemArrIdxInit)
    )
    val wrBgPipeLineMemArrIdx = Vec.fill(
      cfg.numLineMemsPerBgObjRenderer
    )(
      UInt(lineMemArrIdxWidth bits)
    )
    for (zdx <- 0 until cfg.numLineMemsPerBgObjRenderer) {
      wrBgPipeLineMemArrIdx(zdx) := (
        rWrFullBgPipeLineMemArrIdx(zdx)
        //(
        //  rWrFullBgPipeLineMemArrIdx(zdx).high
        //  downto log2Up(cfg.physFbSize2dScale.y)
        //  //downto 0
        //)
      )
    }
    //--------
    //val rWrLineMemArrIdx = mkLineMemIdx(
    //  wrFullLineMemArrIdxInit
    //)
    //val rCombineFullLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(cfg.numLineMemsPerBgObjRenderer) bits))
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
      //  downto log2Up(cfg.physFbSize2dScale.y)
      //  //downto 0
      //)
    )
    //def wrLineNumInit = 0x3
    //def wrLineNumInit = 0x2
    def wrLineNumWidth = log2Up(cfg.intnlFbSize2d.y)
    def wrFullLineNumWidth = (
      wrLineNumWidth
      //log2Up(cfg.physFbSize2d.y)
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
        //val temp = 0 << log2Up(cfg.physFbSize2dScale.y)
        //val temp = 0
        //val temp = cfg.intnlFbSize2d.y - 1
        val temp = (
          (
            cfg.intnlFbSize2d.y /*- 1*/
            - cfg.physFbSize2dScale.y + 1
          ) % cfg.intnlFbSize2d.y
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
        //val temp = 0 << log2Up(cfg.physFbSize2dScale.y)
        //val temp = 0
        //val temp = cfg.intnlFbSize2d.y - 1
        val temp = (
          (
            cfg.intnlFbSize2d.y /*- 1*/ 
            - cfg.physFbSize2dScale.y + 1
          ) % cfg.intnlFbSize2d.y
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
    //    downto log2Up(cfg.physFbSize2dScale.y)
    //  )
    //)
    //def rGlobWrObjLineNum = (
    //  rGlobWrObjFullLineNum(
    //    rGlobWrObjFullLineNum.high
    //    downto log2Up(cfg.physFbSize2dScale.y)
    //  )
    //)

    //val rWrLineNumPassed
    //when (rWrLineNum >= wrLineNumInit + 3) {
    //  rCtrlEn := True
    //}

    //val rCombineRdLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(cfg.numLineMemsPerBgObjRenderer) bits))
    //  //init(0x2)
    //  init(combineRdLineMemArrIdxInit)
    //)
    //val rCombineWrLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(cfg.numLineMemsPerBgObjRenderer) bits))
    //  //init(0x1)
    //  init(combineWrLineMemArrIdxInit)
    //)

    ////val rCombineLineMemCnt = KeepAttribute(
    ////  Reg(UInt(log2Up(cfg.intnlFbSize2d.x + 1) bits)) init(0x0)
    ////)

    //val rRdLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(cfg.numLineMemsPerBgObjRenderer) bits))
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
        //cfg.physFbSize2dScale.y - 1
        cfg.physFbSize2dScale.y - 2
        //0x0
      )
      val fracCntRollover = (
        //cfg.physFbSize2dScale.y - 1
        cfg.physFbSize2dScale.y - 2
      )
      val rLineNumFracCnt = Reg(
        SInt(log2Up(cfg.physFbSize2dScale.y) + 3 bits)
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
      //  rLineNumFracCnt =/= cfg.physFbSize2dScale.y - 1
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
          cfg.intnlFbSize2d.y - 1
          //cfg.physFbSize2d.y - 1
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
      //  //if (cfg.physFbSize2dScale.y > 1) {
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
        //  //rLineNumFracCnt === cfg.physFbSize2dScale.y - 1
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
    def combineObjSubLineMemSetWordFunc(
      //io: WrPulseRdPipeSimpleDualPortMemIo[
      //  Bits,
      //  Vec[ObjSubLineMemEntry],
      //],
      unionIdx: UInt,
      outpPayload: CombinePipePayload,
      inpPayload: CombinePipePayload,
      objTileRow: Vec[ObjSubLineMemEntry],
    ): Unit = {
      outpPayload.stage2.rdObj := objTileRow
    }
    //val rGlobWrObjFullLineNumCheckPipe2 = Reg(Bool()) init(False)
    //val rGlobWrObjFullLineNumPlus1Pipe2 = Reg(
    //  UInt(rGlobWrObjFullLineNum.getWidth + 1 bits)
    //)
    //  .init(0x0)
    ////val rGlobWrObjFullLineNumPlus1Pipe2 := Reg(cloneOf(rGlobWrObjFullLineNum))
    ////  .init(0x0)
    //val rGlobWrObjFullLineNumFracCnt = Reg(
    //  UInt(log2Up(cfg.physFbSize2dScale.y) + 2 bits)
    //)
    //  .init(cfg.physFbSize2dScale.y - 1)
    //val rGlobWrObjFullLineNumPipe1 = Reg(
    //  UInt(rGlobWrObjFullLineNum.getWidth + 1 bits)
    //)
    //  .init(0x0)
    //rGlobWrObjFullLineNumCheckPipe2 := (
    //  rGlobWrObjFullLineNum.resized
    //  =/= (
    //    cfg.intnlFbSize2d.y - 1
    //    //cfg.physFbSize2d.y - 1
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
    ////  //if (cfg.physFbSize2dScale.y > 1) {
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

      //when (rWrLineNum =/= cfg.intnlFbSize2d.y - 1) {
      //  rWrLineNum := rWrLineNum + 1
      //} otherwise {
      //  rWrLineNum := 0
      //}
      //rWrLineNum := rWrLineNumPipe1(rWrLineNum.bitsRange)
      //rWrLineNum := rWrLineNum + 1
      //if (cfg.physFbSize2dScale.y > 1) {
      //  for (zdx <- 0 until rLineMemArrIdxIncCnt.size) {
      //    def tempCnt = rLineMemArrIdxIncCnt(zdx)
      //    when ((tempCnt - 1).msb) {
      //      tempCnt := cfg.physFbSize2dScale.y - 1
      //    } otherwise {
      //      tempCnt := tempCnt - 1
      //    }
      //  }
      //}

      for (zdx <- 0 until rWrObjWriterFullLineMemArrIdx.size) {
        //when (
        //  //if (
        //  //  cfg.physFbSize2dScale.y > 1
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
        //  //  cfg.physFbSize2dScale.y > 1
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
        //  //  cfg.physFbSize2dScale.y > 1
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
      //  //  cfg.physFbSize2dScale.y > 1
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
      //  //  cfg.physFbSize2dScale.y > 1
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
      idx <- 0 until cfg.numLineMemsPerBgObjRenderer
    ) {
      //lineMemArr += Mem(
      //  //wordType=Rgb(cfg.rgbConfig),
      //  wordType=LineMemEntry(),
      //  wordCount=cfg.oneLineMemSize,
      //)
      //  .initBigInt(Array.fill(cfg.oneLineMemSize)(BigInt(0)).toSeq)
      //  .addAttribute("ram_style", cfg.lineArrRamStyle)

      val bgSubLineMemInitBigInt = new ArrayBuffer[BigInt]()
      for (initIdx <- 0 until cfg.bgSubLineMemArrSize) {
        bgSubLineMemInitBigInt += BigInt(0)
      }

      //wrBgSubLineMemArr += Mem(
      //  //wordType=Rgb(cfg.rgbConfig),
      //  //wordType=BgSubLineMemEntry(),
      //  //wordCount=cfg.oneLineMemSize,
      //  wordType=Vec.fill(cfg.bgTileSize2d.x)(BgSubLineMemEntry()),
      //  wordCount=cfg.bgSubLineMemArrSize,
      //)
      //  .initBigInt(
      //    //Array.fill(cfg.oneLineMemSize)(BigInt(0)).toSeq
      //    //Array.fill(cfg.bgSubLineMemArrSize)(BigInt(0)).toSeq
      //    bgSubLineMemInitBigInt.toSeq
      //  )
      //  .addAttribute("ram_style", cfg.lineArrRamStyle)
      //  //.addAttribute("ram_mode", "tdp") // true dual-port
      //  .setName(f"bgLineMemArr_$idx")
      //--------
      //combineBgSubLineMemArr += PipeMemRmw[
      //  Vec[BgSubLineMemEntry],
      //  CombinePipePayload,
      //  CombinePipePayload,
      //](
      //  wordType=Vec.fill(cfg.bgTileSize2d.x)(BgSubLineMemEntry()),
      //  wordCount=cfg.bgSubLineMemArrSize,
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
      for (jdx <- 0 until cfg.combineBgSubLineMemArrSize) {
        combineBgSubLineMemA2d.last += WrPulseRdPipeSimpleDualPortMem(
          dataType=CombinePipePayload(),
          wordType=Vec.fill(
            //cfg.bgTileSize2d.x
            cfg.combineBgSubLineMemVecElemSize
          )(BgSubLineMemEntry()),
          wordCount=cfg.bgSubLineMemArrSize,
          pipeName=s"combineBgSubLineMemArr_${idx}",
          initBigInt=Some(Array.fill(1)(bgSubLineMemInitBigInt).toSeq),
          pmRmwModTypeName=(
            s"Gpu2dCombinePipePayload_Bg_PmRmwModType"
          ),
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
      for (initIdx <- 0 until cfg.objSubLineMemArrSize) {
        objSubLineMemInitBigInt += BigInt(0)
      }
      //val objSubLineMemInit = new ArrayBuffer[Vec[ObjSubLineMemEntry]]()
      //for (initIdx <- 0 until cfg.objSubLineMemArrSize) {
      //  val temp = Vec.fill(
      //    //cfg.objTileSize2d.x
      //    cfg.objSliceTileWidth
      //  )(ObjSubLineMemEntry())
      //  temp := temp.getZero
      //  objSubLineMemInit += temp
      //}

      //objSubLineMemA2d += new ArrayBuffer[
      //  PipeSimpleDualPortMem[Vec[ObjSubLineMemEntry]]
      //]()

      //wrObjSubLineMemArr += Mem(
      //  wordType=Vec.fill(cfg.objTileSize2d.x)(ObjSubLineMemEntry()),
      //  wordCount=cfg.objSubLineMemArrSize,
      //)
      //  .initBigInt(objSubLineMemInitBigInt.toSeq)
      //  .addAttribute("ram_style", cfg.lineArrRamStyle)
      //  .setName(f"wrObjSubLineMemArr_$idx")
      //wrObjSubLineMemArr += FpgacpuRamSimpleDualPort(
      //  wordType=Vec.fill(
      //    //cfg.objTileSize2d.x
      //    cfg.objSliceTileWidth
      //  )(ObjSubLineMemEntry()),
      //  depth=cfg.objSubLineMemArrSize,
      //  initBigInt=Some(objSubLineMemInitBigInt),
      //  //init=Some(objSubLineMemInit),
      //  arrRamStyle=cfg.lineArrRamStyle,
      //)
      //  .setName(f"wrObjSubLineMemArr_$idx")
      val wrObjSubLineMemPmCfg = (
        PipeMemRmwConfig[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
          //WrObjPipePayload,
          //PipeMemRmwDualRdTypeDisabled[
          //  Vec[ObjSubLineMemEntry],
          //  WrObjPipeSlmRmwHazardCmp,
          //],
        ](
          wordType=Vec.fill(
            //cfg.objTileSize2d.x
            cfg.objSliceTileWidth
          )(ObjSubLineMemEntry()),
          wordCountArr=Array.fill(1)(cfg.objSubLineMemArrSize).toSeq,
          hazardCmpType=WrObjPipeSlmRmwHazardCmp(isAffine=false),
          //modType=WrObjPipePayload(isAffine=false),
          modRdPortCnt=wrObjPipeSlmRmwModRdPortCnt,
          modStageCnt=wrObjPipeSlmRmwModStageCnt,
          pipeName=s"wrObjSubLineMemArr_${idx}",
          linkArr=Some(linkArr),
          memArrIdx=0,
          //dualRdType=PipeMemRmwDualRdTypeDisabled[
          //  Vec[ObjSubLineMemEntry],
          //  WrObjPipeSlmRmwHazardCmp,
          //],
          optDualRd=false,
          initBigInt=Some(
            Array.fill(1)(objSubLineMemInitBigInt.toSeq).toSeq
          ),
          optEnableClear=true,
          optModHazardKind=(
            //PipeMemRmw.ModHazardKind.Fwd
            PipeMemRmw.ModHazardKind.Dupl
          ),
          ////init=Some(objSubLineMemInit),
          //arrRamStyle=cfg.lineArrRamStyle,
          vivadoDebug=(
            if (idx == 0) (
              vivadoDebug
            ) else (
              false
            )
          ),
        )
      )
      wrObjSubLineMemArr += PipeMemRmw[
        Vec[ObjSubLineMemEntry],
        WrObjPipeSlmRmwHazardCmp,
        WrObjPipePayload,
        PipeMemRmwDualRdTypeDisabled[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ],
      ](
        cfg=wrObjSubLineMemPmCfg,
        modType=WrObjPipePayload(isAffine=false),
        dualRdType=PipeMemRmwDualRdTypeDisabled[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ],
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
            zdx: Int,
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
            //    //  modStageCnt=wrObjPipeSlmRmwModStageCnt,
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
      //    wordType=Vec.fill(cfg.objTileSize2d.x)(ObjSubLineMemEntry()),
      //    depth=cfg.objSubLineMemArrSize,
      //    initBigInt=Some(objSubLineMemInitBigInt),
      //    latency=1,
      //    arrRamStyle=cfg.lineArrRamStyle,
      //  )
      //}
      combineObjSubLineMemArr += WrPulseRdPipeSimpleDualPortMem(
        //dataType=HardType.union(
        //  WrObjPipePayload(),
        //  CombinePipePayload(),
        //),
        dataType=CombinePipePayload(),
        wordType=Vec.fill(
          //cfg.objTileSize2d.x
          cfg.objSliceTileWidth
        )(ObjSubLineMemEntry()),
        wordCount=cfg.objSubLineMemArrSize,
        pipeName=s"combineObjSubLineMemArr_${idx}",
        initBigInt=Some(Array.fill(1)(objSubLineMemInitBigInt).toSeq),
        pmRmwModTypeName=(
          s"Gpu2dCombinePipePayload_Obj_PmRmwModType"
        ),
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
        //  val ret = Vec.fill(cfg.objTileSize2d.x)(ObjSubLineMemEntry())
        //  switch (rWrLineMemArrIdx) {
        //    for (jdx <- 0 until cfg.numLineMemsPerBgObjRenderer) {
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
        setWordFunc=combineObjSubLineMemSetWordFunc
      )
        .setName(f"combineObjSubLineMemArr_$idx")
      //--------

      if (!noAffineObjs) {
        val objAffineSubLineMemInitBigInt = new ArrayBuffer[BigInt]()
        for (initIdx <- 0 until cfg.objAffineSubLineMemArrSize) {
          objAffineSubLineMemInitBigInt += BigInt(0)
        }
        wrObjAffineSubLineMemArr += FpgacpuRamSimpleDualPort(
          wordType=Vec.fill(
            cfg.objAffineSliceTileWidth
            //cfg.objAffineTileSize2d.x
            //cfg.objAffineDblTileSize2d.x
            //cfg.objAffineSliceTileWidth
            //cfg.myDbgObjAffineTileWidth
          )(
            ObjSubLineMemEntry()
          ),
          depth=cfg.objAffineSubLineMemArrSize,
          //initBigInt=Some(objAffineSubLineMemInitBigInt),
          arrRamStyle=cfg.lineArrRamStyle,
        )
          .setName(f"wrObjAffineSubLineMemArr_$idx")
        combineObjAffineSubLineMemArr += WrPulseRdPipeSimpleDualPortMem(
          //dataType=HardType.union(
          //  WrObjPipePayload(),
          //  CombinePipePayload(),
          //),
          dataType=CombinePipePayload(),
          wordType=Vec.fill(
            cfg.objAffineSliceTileWidth
            //cfg.objAffineTileSize2d.x
            //cfg.objAffineDblTileSize2d.x
            //cfg.objAffineSliceTileWidth
            //cfg.myDbgObjAffineTileWidth
          )(ObjSubLineMemEntry()),
          wordCount=cfg.objAffineSubLineMemArrSize,
          pipeName=s"combineObjAffineSubLineMemArr_${idx}",
          initBigInt=Some(
            Array.fill(1)(objAffineSubLineMemInitBigInt).toSeq
          ),
          pmRmwModTypeName=(
            s"Gpu2dCombinePipePayload_ObjAffine_PmRmwModType"
          ),
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
      //  //wordType=Rgb(cfg.rgbConfig),
      //  wordType=Vec.fill(cfg.objTileSize2d.x)(ObjSubLineMemEntry()),
      //  wordCount=cfg.objSubLineMemArrSize,
      //)
      //  .initBigInt(
      //    //Array.fill(cfg.oneLineMemSize)(BigInt(0)).toSeq
      //    Array.fill(cfg.objSubLineMemArrSize)(BigInt(0)).toSeq
      //  )
      //  .addAttribute("ram_style", cfg.lineArrRamStyle)
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
    //  //jdx <- 0 to cfg.objSubLineMemArrSize - 1
    //  jdx <- 0 to cfg.numLineMemsPerBgObjRenderer - 1
    //) {
    //  //bgSubLineMemA2d += ArrayBuffer[Mem[BgSubLineMemEntry]]()

    //  //for (idx <- 0 to cfg.bgSubLineMemSize - 1) {
    //  //  bgSubLineMemA2d.last += Mem(
    //  //    //wordType=Rgb(cfg.rgbConfig),
    //  //    wordType=BgSubLineMemEntry(),
    //  //    wordCount=cfg.bgSubLineMemSize,
    //  //  )
    //  //    .initBigInt(
    //  //      Array.fill(cfg.bgSubLineMemSize)(BigInt(0)).toSeq
    //  //    )
    //  //    .addAttribute("ram_style", cfg.lineArrRamStyle)
    //  //    .setName(f"bgSubLineMemA2d_$jdx" + "_" + f"$idx")
    //  //}

    //  //bgSubLineMemA2d += ArrayBuffer[Mem[BgSubLineMemEntry]]()

    //  //for (
    //  //  //idx <- 0 to cfg.bgSubLineMemSize - 1
    //  //  idx <- 0 to cfg.bgSubLineMemArrSize - 1
    //  //) {
    //  //  bgSubLineMemA2d.last += Mem(
    //  //    //wordType=Rgb(cfg.rgbConfig),
    //  //    wordType=BgSubLineMemEntry(),
    //  //    wordCount=cfg.bgSubLineMemSize,
    //  //  )
    //  //    .initBigInt(
    //  //      Array.fill(cfg.bgSubLineMemSize)(BigInt(0)).toSeq
    //  //    )
    //  //    .addAttribute("ram_style", cfg.lineArrRamStyle)
    //  //    .setName(f"bgSubLineMemA2d_$jdx" + "_" + f"$idx")
    //  //}

    //  objSubLineMemA2d += ArrayBuffer[Mem[ObjSubLineMemEntry]]()
    //  for (
    //    //idx <- 0 to cfg.objSubLineMemSize - 1
    //    idx <- 0 to cfg.objSubLineMemArrSize - 1
    //  ) {
    //    objSubLineMemA2d.last += Mem(
    //      //wordType=Rgb(cfg.rgbConfig),
    //      wordType=ObjSubLineMemEntry(),
    //      wordCount=cfg.objSubLineMemSize,
    //    )
    //      .initBigInt(
    //        Array.fill(cfg.objSubLineMemSize)(BigInt(0)).toSeq
    //      )
    //      .addAttribute("ram_style", cfg.lineArrRamStyle)
    //      .setName(f"objSubLineMemA2d_$jdx" + "_" + f"$idx")
    //  }
    //  //def tempSize1 = objSubLineMemA2d.last.size

    //  //def size0 = log2Up(cfg.oneLineMemSize)
    //  //def size1 = cfg.objSubLineMemSizePow
    //  //def size2 = cfg.objSubLineMemArrSizePow
    //  //def size3 = cfg.oneLineMemSize
    //  //println(f"objSubLineMemA2d: $jdx $size0 $size1 $size2 $size3")
    //}
    // END: old, non synthesizable code (too many write ports...)

    //val dbgBgLineMemVec = Reg(
    //  Vec.fill(cfg.numLineMemsPerBgObjRenderer)(
    //    Vec.fill(cfg.oneLineMemSize)(
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
    //  Vec.fill(cfg.numLineMemsPerBgObjRenderer)(
    //    Vec.fill(cfg.oneLineMemSize)(
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
    //  Vec.fill(cfg.numLineMemsPerBgObjRenderer)(
    //    Vec.fill(cfg.oneLineMemSize >> cfg.objTileSize2dPow.x)(
    //      Vec.fill(cfg.objTileSize2d.x)(
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

    //val nextLineIdx = KeepAttribute(UInt(log2Up(cfg.numLineMems) bits))
    //val rLineIdx = KeepAttribute(RegNext(nextLineIdx)) init(0x0)

    //val nextCol = Rgb(cfg.rgbConfig)
    //val rCol = RegNext(nextCol)
    //rCol.init(rCol.getZero)
    //--------
    //when (fifoPush.fire) {
    //  rPastOutp := outp
    //}

    // round-robin indexing into the `ArrayBuffer`s

    //val rPastWrLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(cfg.numLineMemsPerBgObjRenderer) bits)) init(0x1)
    //)
    // Used to clear the written OBJ pixels
    //val rObjClearLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(cfg.numLineMemsPerBgObjRenderer) bits)) init(0x1)
    //)
    //val rObjClearCnt = Reg(
    //  UInt(log2Up(cfg.intnlFbSize2d.x) bits)
    //) init(0x0)

    //val rWrBgCnt = Reg(UInt(log2Up(cfg.numBgs) + 1 bits)) init(0x0)
    //val rWrObjCnt = Reg(UInt(log2Up(cfg.numObjs) + 1 bits)) init(0x0)

    //val wrLineMemIdx = outp.bgPxsPosSlice.pos.x
    //val wrLineMemIdx = KeepAttribute(
    //  Reg(UInt(log2Up(cfg.intnlFbSize2d.x) bits)) init(0x0)
    //)

    // BEGIN: OLD NOTES
    // The background-writing pipeline iterates through the `cfg.numBgs`
    // backgrounds, and increments counters for the pixel x-position
    // (`WrBgPipePayload.pxPosXVec`). This pipeline also writes the pixels
    // into `bgSubLineMemArr(someWrLineMemArrIdx)`
    // END: OLD NOTES

    // The background-writing pipeline iterates through the
    // `cfg.numBgs` backgrounds, and draws them into `bgLineMem`
    def wrBgPipeCntWidth = (
      //log2Up(cfg.intnlFbSize2d.x + 1) + cfg.numBgsPow + 1

      log2Up(cfg.intnlFbSize2d.x + 1)
      - cfg.bgTileSize2dPow.x
      + cfg.numBgsPow
      + 1
    )
    def wrBgPipeBakCntStart: Int = (
      //(1 << (cfg.objTileSize2dPow.x + cfg.objAttrsMemIdxWidth))
      //- 1
      //(cfg.intnlFbSize2d.x * (1 << cfg.numBgsPow)) - 1
      //(
      //  (
      //    (
      //      cfg.intnlFbSize2d.x.toDouble * cfg.numBgs.toDouble
      //    ) / cfg.bgTileSize2d.x.toDouble
      //  ) - 1.toDouble
      //).toInt
      (
        if (cfg.numBgsPow <= cfg.bgTileSize2dPow.x) {
          (
            cfg.intnlFbSize2d.x
            >> (cfg.bgTileSize2dPow.x - cfg.numBgsPow)
          )
        } else { // if (cfg.numBgsPow > cfg.bgTileSize2dPow.x)
          (
            cfg.intnlFbSize2d.x
            << (cfg.numBgsPow - cfg.bgTileSize2dPow.x)
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
          "Affine_"
        }
      )
      def wrIdx = 0
      def combineIdx = 1
      def myTempObjSubLineMemArrSize = (
        if (!isAffine) {
          cfg.objSubLineMemArrSize
        } else {
          cfg.objAffineSubLineMemArrSize
        }
      )
      //def myTempObjTileWidth = (
      //  if (!isAffine) {
      //    cfg.objTileSize2d.x
      //  } else {
      //    cfg.objAffineDblTileSize2d.x
      //    //cfg.objAffineSliceTileWidth
      //    //cfg.myDbgObjAffineTileWidth
      //  }
      //)
      def myTempObjTileWidth = cfg.tempObjTileWidth(
        //if (!isAffine) {
        //  0
        //} else {
        //  1
        //}
        isAffine=isAffine
      )
      val addrVec = Vec.fill(cfg.numLineMemsPerBgObjRenderer)(
        //Reg(
          UInt(log2Up(myTempObjSubLineMemArrSize) bits)
        //) init(0x0)
      )
      val dataVec = Vec.fill(cfg.numLineMemsPerBgObjRenderer)(
        Vec.fill(myTempObjTileWidth)(
          //Reg(
            ObjSubLineMemEntry()
          //) init(ObjSubLineMemEntry().getZero)
        )
      )
      val enVec = Vec.fill(cfg.numLineMemsPerBgObjRenderer)(
        //Reg(Bool()) init(False)
        Bool()
      )
      val clearValidVec = Vec.fill(cfg.numLineMemsPerBgObjRenderer)(
        Bool()
      )
      clearValidVec(0) := False
      clearValidVec(1) := True
      for (idx <- 0 until cfg.numLineMemsPerBgObjRenderer) {
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
        for (jdx <- 0 until cfg.numLineMemsPerBgObjRenderer) {
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
              myWrObjSubLineMemArr(jdx).io.wrData := tempData.asBits
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
              .setName(s"wrObjWriter_${extName}rClear_${jdx}")
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
    //val objWriter = ObjSubLineMemWriter(
    //  someWrObjSubLineMemArr=(
    //    //wrObjSubLineMemArr
    //    None
    //  ),
    //  someCombineObjSubLineMemArr=combineObjSubLineMemArr,
    //  //extName=""
    //  isAffine=false,
    //)
    //val objAffineWriter = (!noAffineObjs) generate (
    //  ObjSubLineMemWriter(
    //    someWrObjSubLineMemArr=Some(wrObjAffineSubLineMemArr),
    //    someCombineObjSubLineMemArr=combineObjAffineSubLineMemArr,
    //    //extName="Affine"
    //    isAffine=true,
    //  )
    //)
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
    //def wrBgPipeBgIdxWidth = cfg.numBgsPow + 1
    def wrBgPipeBgIdxWidth = cfg.numBgsPow
    //val rWrBgPipeFrontCntWidth = KeepAttribute(
    //  Reg(UInt(wrBgPipeFrontCntWidth bits)) init(0x0)
    //)
    case class WrBgPipePayload() extends Bundle {
      // pixel x-position
      //val pxPosXVec = Vec.fill(cfg.numBgs)(
      //  UInt(log2Up(cfg.intnlFbSize2d.x) bits)
      //)
      case class Stage0() extends Bundle {
        //val lineMemArrIdx = cloneOf(rWrLineMemArrIdx)
        //val lineMemArrIdx = UInt(
        //  //log2Up(cfg.numLineMemsPerBgObjRenderer) bits
        //  lineMemArrIdxWidth bits
        //)
        //val lineNum = UInt(wrLineNumWidth bits)
        val fullLineNum = UInt(wrFullLineNumWidth bits)
        def lineNum = fullLineNum(
          //fullLineNum.high downto log2Up(cfg.physFbSize2dScale.y)
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
        //def bgIdx = cnt(cfg.numBgsPow - 1 downto 0)
        // background scroll

        def bgIdx = (
          bakCnt(
            //cnt.high
            //downto (cnt.high - wrBgPipeBgIdxWidth + 1)
            wrBgPipeBgIdxWidth - 1 downto 0
          )
        )
        //val scroll = cloneOf(bgAttrsArr(0).scroll)
        //val bgAttrs = Vec.fill(cfg.bgTileSize2d.x)(
        //  cloneOf(bgAttrsArr(0))
        //)
        val bgAttrs = cloneOf(bgAttrsArr(0))
        val colorMathAttrs = (!noColorMath) generate Gpu2dBgAttrs(
          cfg=cfg,
          isColorMath=true,
        )
        //val tilePxsCoord = cfg.bgTilePxsCoordT()

        //def cntWillBeDone() = (
        //  // With more than one background, this should work
        //  //cnt(cnt.high downto cfg.numBgsPow)
        //  getCntPxPosX() >= cfg.intnlFbSize2d.x - 1
        //)

        //def cntWillBeDone() = (
        //  //cnt === cfg.intnlFbSize2d.x * cfg.numBgs - 1
        //  cntMinus1.msb
        //)
        def bakCntWillBeDone() = (
          //bakCntMinus1.msb
          //cnt + 1 === cfg.oneLineMemSize
          //bakCntMinus1 === 0
          bakCnt === 0
          //bakCnt.msb
          //bakCnt === 0
        )

        //def cntDone() = (
        //  //cnt === cfg.intnlFbSize2d.x * cfg.numBgs
        //  cnt.msb
        //)

        // pixel x-position
        //def getCntPxPosX(
        //  x: Int,
        //) = cnt(
        //  //(log2Up(cfg.bgSize2dInPxs.x) + cfg.numBgsPow - 1)
        //  cnt.high
        //  downto cfg.numBgsPow
        //)
        def getCntPxPosX(
          x: Int,
        ) = {
          val tempGetCntPxPosX = UInt(
            //(cnt.getWidth - cfg.numBgsPow + cfg.bgTileSize2dPow.x)
            //(cnt.getWidth + cfg.bgTileSize2dPow.x)
            log2Up(cfg.intnlFbSize2d.x) bits
          )
          //println(tempGetCntPxPosX.high)

          //def tempCntGetPxPosXRange
          def tempTopRange = (
            //tempGetCntPxPosX.high downto cfg.bgTileSize2dPow.x
            tempGetCntPxPosX.high downto cfg.bgTileSize2dPow.x
          )
          def tempCntRange: Range = (
            //(log2Up(cfg.bgSize2dInPxs.x) + cfg.numBgsPow - 1)
            //cnt.high
            log2Up(cfg.intnlFbSize2d.x) + cfg.numBgsPow - 1
            downto cfg.numBgsPow + cfg.bgTileSize2dPow.x
          )
          def tempBotRange = (
            cfg.bgTileSize2dPow.x - 1 downto 0
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
        //  cfg.numBgs - 1 - cnt(cfg.numBgsPow - 1 downto 0)
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
        val bgEntryMemIdx = Vec.fill(cfg.bgTileSize2d.x)(
          UInt(cfg.bgEntryMemIdxWidth bits)
        )
        val pxPos = Vec.fill(cfg.bgTileSize2d.x)(
          cfg.bgPxsCoordT()
        )
        //--------
        //val haveTwoMyIdx = Vec.fill(
        //  //cfg.bgTileSize2d.x
        //  cfg.numBgMemsPerKind
        //)(
        //  Bool()
        //)
        //val haveTwoMyIdx = Bool()
        val pxPosXGridIdx = (
          Vec.fill(cfg.bgTileSize2d.x)(
            UInt(
              (
                log2Up(cfg.oneLineMemSize) - cfg.bgTileSize2dPow.x
                + 1
              )
              bits
            )
          )
        )
        val pxPosXGridIdxFindFirstSameAsFound = Bool()
        val pxPosXGridIdxFindFirstSameAsIdx = UInt(
          cfg.bgTileSize2dPow.x bits
        )
        val pxPosXGridIdxFindFirstDiffFound = Bool()
        val pxPosXGridIdxFindFirstDiffIdx = UInt(
          cfg.bgTileSize2dPow.x bits
        )

        //val pxPosXGridIdxFindFirstSameAsFound = Bool()
        //val pxPosXGridIdx
        //val myIdxVec = Vec.fill(cfg.bgTileSize2d.x)(
        //  //UInt(cfg.bgTileSize2dPow.x bits)
        //  //UInt(1 bits)
        //  //UInt(cfg.bgEntryMemIdxWidth bits)
        //  //UInt(1 bits)
        //  UInt(
        //    (log2Up(cfg.oneLineMemSize) - cfg.bgTileSize2dPow.x)
        //    bits
        //  )
        //)
        //--------
      }
      case class Stage2() extends Bundle {
        //--------
        //val pxPosXGridIdx = (
        //  Vec.fill(cfg.bgTileSize2d.x)(
        //    UInt(
        //      (log2Up(cfg.oneLineMemSize) - cfg.bgTileSize2dPow.x)
        //      bits
        //    )
        //  )
        //)
        //val pxPosXGridIdxLsb = (
        //  Vec.fill(cfg.bgTileSize2d.x)(
        //    //UInt(1 bits)
        //    Bool()
        //  )
        //)
        //val pxPosXGridIdxFindFirstSameAsFound = Bool()
        //val pxPosXGridIdxFindFirstSameAsIdx = UInt(
        //  cfg.objTileSize2dPow.x bits
        //)
        //val pxPosXGridIdxFindFirstDiffFound = Bool()
        //val pxPosXGridIdxFindFirstDiffIdx = UInt(
        //  cfg.objTileSize2dPow.x bits
        //)
        def numMyIdxVecs = 4 + cfg.bgTileSize2d.x
        val myIdxV2d = Vec.fill(cfg.bgTileSize2d.x)(
          Vec.fill(numMyIdxVecs)(
            UInt(cfg.bgTileSize2dPow.x bits)
          )
        )
        //--------
        val bgEntryMemIdxSameAs = UInt(log2Up(cfg.numTilesPerBg) bits)
        val bgEntryMemIdxDiff = UInt(log2Up(cfg.numTilesPerBg) bits)
        //--------
        val fbRdAddrMultTileMemBaseAddr = UInt(
          cfg.fbRdAddrMultWidthPow bits
        )
        val fbRdAddrMultPxPosY = UInt(
          cfg.fbRdAddrMultWidthPow bits
        )
        //--------
      }
      case class Stage3(
        isColorMath: Boolean,
      ) extends Bundle {
        val bgEntry = (
          Vec.fill(cfg.numBgMemsPerNonPalKind)(
            Vec.fill(cfg.bgTileSize2d.x)(
              Gpu2dBgEntry(
                cfg=cfg,
                isColorMath=isColorMath,
              )
            )
          )
        )
        val tempAddr = Vec.fill(cfg.bgTileSize2d.x)(
          UInt(log2Up(cfg.numTilesPerBg) bits)
        )
      }
      case class Stage4(
        isColorMath: Boolean
      ) extends Bundle {
        //--------
        // `Gpu2dBgEntry`s that have been read
        val bgEntry = Vec.fill(cfg.bgTileSize2d.x)(
          Gpu2dBgEntry(
            cfg=cfg,
            isColorMath=isColorMath,
          )
        )
        //--------
        val fbRdAddrMultPlus = UInt(cfg.fbRdAddrMultWidthPow bits)
        //--------
      }
      case class Stage5() extends Bundle {
        val fbRdAddrFinalPlus = Vec.fill(cfg.bgTileSize2d.x)(
          UInt(cfg.fbRdAddrMultWidthPow bits)
        )
      }
      case class Stage6(
        isColorMath: Boolean
      ) extends Bundle {
        //val tilePxsPosY = UInt(cfg.bgTileSize2dPow.y bits)
        //val tempTilePxsPos = Vec.fill(cfg.bgTileSize2d.x)(
        //  cfg.bgTilePxsCoordT()
        //)
        val tilePxsCoord = Vec.fill(cfg.bgTileSize2d.x)(
          cfg.bgTilePxsCoordT()
        )
        //val tileMemRdAddrSameAsGridIdx = UInt(1 bits)
        //val tileMemRdAddrDiffGridIdx = UInt(1 bits)
        val tileMemRdAddrFront = UInt(cfg.bgTileMemIdxWidth bits)
        val tileMemRdAddrBack = UInt(cfg.bgTileMemIdxWidth bits)
        def myTileIdxWidth = (
          if (!isColorMath) (
            cfg.numBgTilesPow
          ) else ( // if (isColorMath)
            cfg.numColorMathTilesPow
          )
        )
        val tileIdxFront = UInt(myTileIdxWidth bits)
        val tileIdxBack = UInt(myTileIdxWidth bits)
        //val tileIdxFrontLsb = Bool()
        //val tileIdxBackLsb = Bool()
        //--------
        //val tileGridIdx = (
        //  Vec.fill(cfg.bgTileSize2d.y)(
        //    UInt(
        //      (
        //        //log2Up(cfg.oneLineMemSize) - cfg.bgTileSize2dPow.y
        //        //+ 1
        //        //cfg.numBgTilesPow - 1 //bits
        //        (
        //          if (!isColorMath) (
        //            cfg.numBgTilesPow
        //          ) else ( // if (isColorMath)
        //            cfg.numColorMathTilesPow
        //          )
        //        ) + cfg.bgTileSize2dPow.y
        //      )
        //      bits
        //    )
        //  )
        //)
        //val tileGridIdxFindFirstSameAsFound = Bool()
        //val tileGridIdxFindFirstSameAsIdx = UInt(
        //  cfg.bgTileSize2dPow.x bits
        //)
        //val tileGridIdxFindFirstDiffFound = Bool()
        //val tileGridIdxFindFirstDiffIdx = UInt(
        //  cfg.bgTileSize2dPow.x bits
        //)
        //--------
      }
      case class Stage7() extends Bundle {
        //val tilePxsCoord = Vec.fill(cfg.bgTileSize2d.x)(
        //  cfg.bgTilePxsCoordT()
        //)
        // `Gpu2dTile`s that have been read
        //val tile = Vec.fill(cfg.bgTileSize2d.x)(
        //  Gpu2dTileFull(
        //    cfg=cfg,
        //    isObj=false,
        //    isAffine=false,
        //  )
        //)
        val tileSlice = Vec.fill(cfg.bgTileSize2d.x)(
          Gpu2dTileSlice(
            cfg=cfg,
            isObj=false,
            isAffine=false,
          )
        )
      }
      case class Stage8() extends Bundle {
        val palEntryMemIdx = Vec.fill(cfg.bgTileSize2d.x)(
          UInt(cfg.bgPalEntryMemIdxWidth bits)
        )
      }
      case class Stage9() extends Bundle {
        // Whether `palEntryMemIdx(someBgIdx)` is non-zero
        val palEntryNzMemIdx = Vec.fill(cfg.bgTileSize2d.x)(
          Bool()
        )
      }
      // The following BG pipeline stages are only performed when
      // `palEntryNzMemIdx(someBgIdx)` is `True`
      // `Gpu2dPalEntry`s that have been read
      case class Stage11() extends Bundle {
        val palEntry = Vec.fill(cfg.bgTileSize2d.x)(
          Gpu2dPalEntry(cfg=cfg)
        )
      }
      case class Stage12() extends Bundle {
        //val doWrite = Bool()
        val subLineMemEntry = Vec.fill(cfg.bgTileSize2d.x)(
          BgSubLineMemEntry()
        )
      }
      case class PostStage0(
        isColorMath: Boolean
      ) extends Bundle {
        //// scroll
        //val scroll = cloneOf(bgAttrsArr(0).scroll)
        // indices into `bgEntryMem`
        //val bgEntryMemIdxPart0 = Vec2(UInt(cfg.bgEntryMemIdxWidth bits))
        //val bgEntryMemIdxPart1 = UInt(cfg.bgEntryMemIdxWidth bits)
        val stage1 = Stage1()
        val stage2 = Stage2()
        val stage3 = Stage3(
          isColorMath=isColorMath
        )
        val stage4 = Stage4(
          isColorMath=isColorMath
        )
        //val tileMemIdx = UInt(cfg.bgTileMemIdxWidth bits)
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

    //def wrBgPipeSize = (wrBgPipeNumElems * cfg.numBgs)
    //def wrBgPipeSize = wrBgPipeNumElems + 1

    //def wrBgPipeSize = max(wrBgPipeNumElems, cfg.numBgs) + 1 
    //def wrBgPipeNumMainStages = wrBgPipeNumStagesPerBg + cfg.numBgs


    //def wrBgPipeNumElemsGtNumBgs = wrBgPipeNumElems > cfg.numBgs
    // The sprite-drawing pipeline iterates through the `cfg.numObjs`
    // sprites, and draws them into `objLineMem` if they're on the current
    // scanline 
    //def wrObjPipeObjAttrsMemIdxWidth = cfg.objAttrsMemIdxWidth + 1
    //val rWrObjPipeFrontObjAttrsMemIdx = KeepAttribute(
    //  Reg(SInt(wrObjPipeObjAttrsMemIdxWidth bits)) init(0x0)
    //)

    //def wrObjPipeCntWidth = (
    //  cfg.objTileSize2dPow.x + cfg.numObjsPow + 1
    //)
    def wrObjPipeCntWidth(
      isAffine: Boolean
    ) = (
      //cfg.objTileSize2dPow.x + cfg.objAttrsMemIdxWidth + 1
      //cfg.objTileSize2dPow.x + cfg.objAttrsMemIdxWidth
      //cfg.objAttrsMemIdxWidth + 1
      (
        if (!isAffine) {
          (
            //cfg.objAttrsMemIdxWidth
            //+ 1 // for the extra cycle delay between pixels
            cfg.objAttrsMemIdxTileCntWidth
          )
        } else {
          //cfg.objAffineAttrsMemIdxWidth
          //+ cfg.objAffineTileWidthRshift
          cfg.objAffineAttrsMemIdxTileCntWidth
        }
      ) + 1 + 1
      
    )
    def wrObjPipeBakCntStart(
      isAffine: Boolean
    ) = (
      //(1 << (cfg.objTileSize2dPow.x + cfg.objAttrsMemIdxWidth))
      //- 1
      //(1 << cfg.objAttrsMemIdxWidth)
      //- 1
      (
        1
        << (
          (
            if (!isAffine) {
              (
                //cfg.objAttrsMemIdxWidth + 1
                //+ 1 // for the extra cycle delay between pixels
                ////+ 1
                cfg.objAttrsMemIdxTileCntWidth
              )
            } else {
              //cfg.objAffineAttrsMemIdxWidth
              //+ cfg.objAffineTileWidthRshift
              cfg.objAffineAttrsMemIdxTileCntWidth
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
    //case class WrObjPipeStage5Fwd() extends Interface {
    //  //val pxPosX = SInt(cfg.objPxsCoordSize2dPow.x bits)
    //  //val pxPosX = SInt(cfg.objPxsCoordSize2dPow.x bits)
    //  val pxPos = cfg.objPxsCoordT()
    //  //val overwriteLineMemEntry = Bool()
    //}
    //def wrObjPipeStage6NumFwd = 3
    ////def wrObjPipeStage6NumFwd = 2
    //case class WrObjPipeStage6Fwd() extends Interface {
    //  //val pxPosX = SInt(cfg.objPxsCoordSize2dPow.x bits)
    //  val pxPosX = SInt(cfg.objPxsCoordSize2dPow.x bits)
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
    //) extends Interface {
    //  val pxPosXGridIdx = UInt(
    //    (
    //      cfg.objPxsCoordSize2dPow.x
    //      - (
    //        if (!isAffine) {
    //          cfg.objTileSize2dPow.x
    //        } else {
    //          cfg.objAffineDblTileSize2dPow.x
    //          //cfg.objAffineSliceTileWidthPow
    //          //cfg.myDbgObjAffineTileWidthPow
    //        }
    //      )
    //    ) bits
    //  )
    //  //val pxPosY = SInt(cfg.objPxsCoordSize2dPow.y bits)
    //  val pxPosYLsb = Bool()
    //  //val doFwd = Bool()
    //  //val pxPosX = SInt(cfg.objPxsCoordSize2dPow.x bits)
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
    //) extends Interface {
    //  //val pxPosX = SInt(cfg.objPxsCoordSize2dPow.x bits)
    //  //val pxPosInLine = Bool()
    //  //val palEntry = Gpu2dPalEntry(cfg=cfg)
    //  //val stage6 = Stage6()
    //  //val pxPosXIsSame = Bool()
    //  //val pxPosXIsSame = Bool()
    //  //val prio = UInt(cfg.numBgsPow bits)
    //  //--------
    //  // BEGIN: comment this out; move to stage 10
    //  //val objAttrsMemIdx = UInt(
    //  //  //(wrObjPipeCntWidth - cfg.objAttrsMemIdxWidth.x) bits
    //  //  cfg.objAttrsMemIdxWidth bits
    //  //)
    //  ////val pxPosY = SInt(cfg.objPxsCoordSize2dPow.y bits)
    //  //val pxPosXGridIdx = UInt(
    //  //  (cfg.objPxsCoordSize2dPow.x - cfg.objTileSize2dPow.x) bits
    //  //)
    //  //val pxPosYLsb = Bool()
    //  // END: comment this out; move to stage 10
    //  //--------
    //  //val stage10Fwd = Bool()
    //  //val pxPosXGridIdxLsb = Bool()

    //  //val pxPos = cfg.objPxsCoordT()
    //  //val prio = UInt(cfg.numBgsPow bits)
    //  //val overwriteLineMemEntry = Bool()
    //  //val wrLineMemEntry = ObjSubLineMemEntry()
    //  //val extSingle = WrObjPipe6ExtSingle()
    //  //val ext = WrObjPipe9Ext(useVec=false)
    //  val ext = WrObjPipe14Ext(
    //    isAffine=isAffine,
    //    useVec=false,
    //  )
    //  //val wholeWrLineMemEntry = Vec.fill(cfg.objTileSize2d.x)(
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
        //fullLineNum.high downto log2Up(cfg.physFbSize2dScale.y)
        fullLineNum.high downto 0x0
      )
      val cnt = UInt(wrObjPipeCntWidth(isAffine) bits)
      val bakCnt = UInt(wrObjPipeCntWidth(isAffine) bits)
      val bakCntMinus1 = UInt(wrObjPipeCntWidth(isAffine) bits)
      def myMemIdxWidth = (
        if (!isAffine) {
          cfg.objAttrsMemIdxWidth 
        } else {
          (
            cfg.objAffineAttrsMemIdxWidth
            //+ cfg.objAffineTileWidthRshift
            //cfg.objAffineAttrsMemIdxTileCntWidth
          )
        }
      )
      val dbgTestIdx = UInt(
        (
          //cfg.objAttrsMemIdxWidth + 1 + 1
          //cfg.objAttrsMemIdxWidth + 3
          //cfg.objAttrsMemIdxWidth + 2
          cfg.objAttrsMemIdxTileCntWidth
        ) bits
      )
      val dbgTestAffineIdx = UInt(
        cfg.objAffineAttrsMemIdxTileCntWidth bits
      )
      //val objAttrsMemIdx = UInt(
      //  //cfg.objAttrsMemIdxWidth bits
      //  (cfg.objAttrsMemIdxWidth + 1) bits
      //)
    }
    case class WrObjPipeSlmRmwHazardCmp(
      isAffine: Boolean,
    ) extends Bundle {
      //val cnt = UInt(wrObjPipeCntWidth(isAffine) bits),
      //val rawObjAttrsMemIdx
      //val objAttrsMemIdx = (!isAffine) generate (
      //  UInt(
      //    (cfg.objAttrsMemIdxTileCntWidth - cfg.objCntWidthShift)
      //    bits
      //  )
      //)
      //val affineObjAttrsMemIdx = (isAffine) generate (
      //  UInt(
      //    //(cfg.objAttrsMemIdxTileCntWidth - cfg.objCntWidthShift)
      //    cfg.objAffineAttrsMemIdxWidth
      //    bits
      //  )
      //)

      val objIdx = UInt(
        (
          cfg.objAttrsMemIdxWidth
          //.max(cfg.objAffineAttrsMemIdxWidth)
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
            //  modStageCnt=wrObjPipeSlmRmwModStageCnt,
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
      cfg.tempObjTileWidthPow(isAffine=isAffine)
    )
    def tempObjTileWidth(isAffine: Boolean) = (
      cfg.tempObjTileWidth(isAffine=isAffine)
    )

    def tempObjTileWidth1(isAffine: Boolean) = (
      cfg.tempObjTileWidth1(isAffine=isAffine)
    )
    def tempObjTileWidthPow1(isAffine: Boolean) = (
      cfg.tempObjTileWidthPow1(isAffine=isAffine)
    )

    def tempObjTileWidthPow2(isAffine: Boolean) = (
      cfg.tempObjTileWidthPow2(isAffine=isAffine)
    )
    def tempObjTileWidth2(isAffine: Boolean) = (
      cfg.tempObjTileWidth2(isAffine=isAffine)
    )

    def tempObjTileHeight(isAffine: Boolean) = (
      cfg.tempObjTileHeight(isAffine=isAffine)
    )
    def ObjTilePxsCoordT(isAffine: Boolean) = (
      cfg.anyObjTilePxsCoordT(isAffine=isAffine)
    )
    def numMyIdxVecs(isAffine: Boolean) = (
      //4 + tempObjTileWidth() * 2
      4 + tempObjTileWidth1(isAffine) * 2
      //4 + tempObjTileWidth2() * 2
    )
    case class WrObjPipePayloadStage0(isAffine: Boolean) extends Bundle {
      //val tilePxsCoordXCnt = UInt(cfg.objTileSize2dPow.x bits)
      //val tilePxsCoordXCntPlus1 = UInt(
      //  (cfg.objTileSize2dPow.x + 1) bits
      //)
      //val lineMemArrIdx = cloneOf(rWrLineMemArrIdx)
      //val lineMemArrIdx = UInt(
      //  //log2Up(cfg.numLineMemsPerBgObjRenderer) bits
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
      //def affineIdx = cnt(cfg.objAttrsMemIdxWidth - 1 downto 2)
      //def affineIdx = justCopy.affineIdx
      def rawIdx() = (
        justCopy.dbgTestIdx
      )
      def rawAffineIdx() = (
        justCopy.dbgTestAffineIdx
        //(
        //  cfg.objAttrsMemIdxWidth - 1 downto 0
        //)
      )

      def rawObjAttrsMemIdx() = (
        justCopy.dbgTestIdx(
          //cfg.objAttrsMemIdxWidth - 1 downto 0
          //cfg.objAttrsMemIdxWidth + 1 - 1 downto 0
          //cfg.objAttrsMemIdxWidth + 1 + 1 - 1 downto 0
          //cfg.objAttrsMemIdxWidth + 1 - 1 downto 1
          //cfg.objAttrsMemIdxWidth + 3 - 1 downto 0
          //cfg.objAttrsMemIdxWidth + 2 - 1 downto 0
          justCopy.dbgTestIdx.high downto 0
        )
      )
      def objAttrsMemIdx() = (
        justCopy.dbgTestIdx(
          //cfg.objAttrsMemIdxWidth - 1 downto 0
          //cfg.objAttrsMemIdxWidth + 1 - 1 downto 1
          //cfg.objAttrsMemIdxWidth + 1 + 1 - 1 downto 1 + 1
          //cfg.objAttrsMemIdxWidth + 3 - 1 downto 3
          //cfg.objAttrsMemIdxWidth + 2 - 1 downto 2
          justCopy.dbgTestIdx.high
          downto cfg.objCntWidthShift
        )
      )
      def objXStart() = {
        (
          //def tempShift = tempAffineShift
          //if (cfg.objTileWidthRshift > 0) {
            Cat(
              rawIdx()(
                (
                  ////objXStartRawWidthPow
                  ////cfg.objSliceTileWidthPow
                  //1 // to account for double size rendering
                  ////+ (
                  ////  if (cfg.objTileWidthRshift == 0) {
                  ////    1 // for the extra cycle delay between pixels
                  ////  } else {
                  ////    cfg.objTileWidthRshift
                  ////  }
                  ////)
                  //+
                  //tempAffineShift
                  cfg.objTileWidthRshift
                  //cfg.objSliceTileWidthPow
                  //+ 1 // account for the extra cycle delay
                  + 1 // account for grid index
                  - 1 
                )
                //downto cfg.objSliceTileWidthPow
                //downto cfg.objTileWidthRshift + 1
                downto (
                  //if (cfg.objTileWidthRshift == 0) {
                  //  1
                  //} else {
                  //  0
                  //}
                  //tempAffineShift
                  //0
                  //1 // account for double size rendering
                  //cfg.objTileWidthRshift
                  //+ 1 // account for the extra cycle delay
                  + 1 // account for grid index
                )
              ),
              B(
                cfg.objSliceTileWidthPow bits,
                //cfg.objTileWidthRshift bits,
                //cfg.objTileWidthRshift bits,
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
      //    if (cfg.fancyObjPrio) {
      //      U(2 bits, default -> False)
      //    } else {
      //      U(2 bits, default -> True)
      //    }
      //  )
      //)
      //def affineMultKindMult() = rawAffineIdx() 
      //def affineMultKindAdd() = rawAffineIdx()
      //def affineObjXStartRawWidthPow = (
      //  cfg.objAffineTileWidthRshift
      //  + 1
      //)
      def tempAffineShift = (
        //1 // account for the extra cycle delay between pixels
        ////(
        ////  //if (cfg.objAffineTileWidthRshift == 0) {
        ////  //  
        ////  //} else {
        ////  //}
        ////)
        //+ cfg.objAffineTileWidthRshift
        //+ 1 // account for double size rendering
        //+ 1 // account for the rendering grid
        cfg.objAffineCntWidthShift
      )
      def affineObjAttrsMemIdx() = {
        //def tempShift = tempAffineShift
        rawAffineIdx()(
          //cfg.objAttrsMemIdxWidth - 1 downto 2
          //justCopy.myMemIdxWidth - 1
          //downto cfg.objAffineTileWidthRshift
          //(
          //  justCopy.myMemIdxWidth
          //  //+ affineObjXStartRawWidthPow + 1 - 1 - 1
          //  + cfg.objAffineTileWidthRshift - 1
          //)
          ////downto affineObjXStartRawWidthPow + 1 - 1
          //downto cfg.objAffineTileWidthRshift
          (
            //affineObjXStartRawWidthPow
            justCopy.myMemIdxWidth
            + tempAffineShift
            //+ 1 // account for double size rendering
            //cfg.objAffineSliceTileWidthPow
            - 1 
          )
          downto (
            //cfg.objAffineSliceTileWidthPow
            //+ 
            //cfg.objAffineTileWidthRshift + 1
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
            //  cfg.objAffineTileWidthRshift
            //),
            rawAffineIdx()(
              //cfg.objAffineTileWidthRshift - 1 downto 0
              //cfg.objAffineTileWidthRshift downto 0
              //justCopy.myMemIdxWidth
              //(
              //  cfg.objAffineDblTileSize2dPow.x
              //  + cfg.objAffineAttrsMemIdxWidth
              //)
              //rawAffineIdx().high - 1 // account for 
              //(
              //  affineObjXStartRawPlus
              //  //cfg.objAffineSliceTileWidthPow
              //  + cfg.objAffineAttrsMemIdxWidth
              //)
              //downto cfg.objAffineAttrsMemIdxWidth
              (
                ////affineObjXStartRawWidthPow
                ////cfg.objAffineSliceTileWidthPow
                //1 // to account for double size rendering
                ////+ (
                ////  if (cfg.objAffineTileWidthRshift == 0) {
                ////    1 // for the extra cycle delay between pixels
                ////  } else {
                ////    cfg.objAffineTileWidthRshift
                ////  }
                ////)
                //+
                //tempAffineShift
                cfg.objAffineTileWidthRshift
                //cfg.objAffineSliceTileWidthPow
                + 1 // account for double size rendering
                //+ 1 // account for the extra cycle delay
                + 1 // account for grid index
                - 1 
              )
              //downto cfg.objAffineSliceTileWidthPow
              //downto cfg.objAffineTileWidthRshift + 1
              downto (
                //if (cfg.objAffineTileWidthRshift == 0) {
                //  1
                //} else {
                //  0
                //}
                //tempAffineShift
                //0
                //1 // account for double size rendering
                //cfg.objAffineTileWidthRshift
                //+ 1 // account for the extra cycle delay
                + 1 // account for grid index
              )
            ),
            B(
              cfg.objAffineSliceTileWidthPow bits,
              //cfg.objAffineTileWidthRshift bits,
              //cfg.objAffineTileWidthRshift bits,
              default -> False
            ),
          ).asUInt
          //(
          //  cfg.objAffineDblTileSize2dPow.x - 1 downto 0
          //  //cfg.objAffineSliceTileWidthPow - 1 downto 0
          //)
        )
      }
      //def objAttrsMemIdx = justCopy.objAttrsMemIdx

      //val objAttrsMemIdx

      //val objAttrsMemIdx = SInt(cfg.objAttrsMemIdxWidth + 1 bits)
      //val objAttrsMemIdx = cloneOf(rWrObjPipeFrontObjAttrsMemIdx)
      //val objAttrsMemIdx = UInt(cfg.objAttrsMemIdxWidth bits)

      //val innerObjAttrsMemIdx = UInt(cfg.objAttrsMemIdxWidth bits)

      ////innerObjAttrsMemIdx := bakCnt(
      ////  //bakCnt.high - 1
      ////  //downto (bakCnt.high - 1 - cfg.objAttrsMemIdxWidth + 1)
      ////  //bakCnt.high
      ////  //downto (bakCnt.high - (cfg.objAttrsMemIdxWidth - 1))
      ////  //(cfg.objAttrsMemIdxWidth + cfg.objTileSize2dPow.x - 1)
      ////  //downto cfg.objTileSize2dPow.x

      ////  cfg.objAttrsMemIdxWidth + cfg.objTileSize2dPow.x - 1
      ////  downto cfg.objTileSize2dPow.x
      ////)

      //def objAttrsMemIdx() = innerObjAttrsMemIdx
      //def invObjAttrsMemIdx() = (
      //  //cnt
      //  //(
      //  //  (1 << cfg.objAttrsMemIdxWidth) - 1
      //  //) - 
      //  cnt(
      //    // support needing to do two writes into `objSubLineMemArr`
      //    cfg.objAttrsMemIdxWidth - 1 downto 0
      //    //cfg.objAttrsMemIdxWidth downto 1
      //  )
      //)
      // which iteration are we on for
      //val gridIdxLsb = Bool()
      def calcNonAffineGridIdxLsb() = (
        //cnt
        cnt(
          // support needing to do two writes into `objSubLineMemArr`
          //0
          //cfg.objAttrsMemIdxWidth + 1 - 1
          //cfg.objTileSize2dPow.x - 1
          //1 // account for the extra cycle delay between pixels
          0
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
          //cfg.objAttrsMemIdxWidth + 1 + 1 - 1
          //cfg.objAffineAttrsMemIdxTileCntWidth
          //cfg.objAffineAttrsMemIdxTileCntWidth - 1
          //+ 1
          //cfg.objAffineSliceTileWidth
          (
            //if (cfg.objAffineTileWidthRshift == 0) {
            //  1 // account for the extra cycle delay between pixels
            //} else {
            //  cfg.objAffineTileWidthRshift
            //}
            //cfg.objAffineTileWidthRshift
            //+ 1 // to account for double size rendering
            //cfg.objAffineTileWidthRshift
            //+ 
            //1 // to account for the extra cycle delay
            0
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
      //  (bakCnt.high - 1 - cfg.objAttrsMemIdxWidth)
      //  downto 0
      //)
      //def getCntObjIdx() = cnt(
      //  cnt.high downto cfg.objTileSize2dPow.x
      //)

      //def getCntTilePxsCoordX() = cnt(
      //  //(cnt.high - 1 - cfg.objAttrsMemIdxWidth)
      //  cfg.objTileSize2dPow.x - 1
      //  downto 0
      //)
      //def getBakCntTilePxsCoordX() = bakCnt

      def bakCntWillBeDone() = (
        //bakCntMinus1.msb
        //cnt + 1 === cfg.oneLineMemSize
        //bakCntMinus1 === 0
        bakCnt === 0
        //bakCnt.msb
      )
    }
    case class WrObjPipePayloadStage2(isAffine: Boolean) extends Bundle {
      val objAttrs = Gpu2dObjAttrs(
        cfg=cfg,
        isAffine=isAffine,
      )
    }
    case class WrObjPipePayloadStage3(isAffine: Boolean) extends Bundle {
      def myTempObjTileWidth = tempObjTileWidth(isAffine)

      //// What are the `Gpu2dObjAttrs` of our sprite? 
      //val objAttrs = Gpu2dObjAttrs(
      //  cfg=cfg,
      //  isAffine=isAffine,
      //)

      val pxPos = Vec.fill(myTempObjTileWidth)(
        cfg.objPxsCoordT()
      )
      val myIdxPxPosX = Vec.fill(
        //myTempObjTileWidth
        tempObjTileWidth1(isAffine)
        //tempObjTileWidth2()
      )(
        SInt(cfg.objPxsCoordSize2dPow.x bits)
      )
      //val affinePxPos = (isAffine) generate Vec.fill(
      //  myTempObjTileWidth
      //)(
      //  cfg.objPxsCoordT()
      //)
    }
    case class WrObjPipePayloadStage4(isAffine: Boolean) extends Bundle {
      //--------
      // (a  b) × (x) = (ax+by)
      // (c  d)   (y)   (cx+dy)
      //--------
      //val affine = Gpu2dAffine(
      //  cfg=cfg,
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
        cfg=cfg,
        isObj=true,
      )
      // X coordinate
      val multAX = (isAffine) generate Vec.fill(
        //cfg.objAffineDblTileSize2d.x
        //cfg.objAffineDblTileSize2d.x
        myTempObjTileWidth
      )(
        SInt(multSize2dPow.x bits)
      )
      val multBY = (isAffine) generate Vec.fill(
        //cfg.objAffineDblTileSize2d.x
        //cfg.objAffineDblTileSize2d.x
        //tempObjTileWidth()
        myTempObjTileWidth
      )(
        SInt(multSize2dPow.x bits)
      )
      // Y coordinate
      val multCX = (isAffine) generate Vec.fill(
        //cfg.objAffineDblTileSize2d.y
        myTempObjTileWidth
      )(
        SInt(multSize2dPow.y bits)
      )
      val multDY = (isAffine) generate Vec.fill(
        //cfg.objAffineDblTileSize2d.y
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
        //cfg.objAffineDblTileSize2d.x
        myTempObjTileWidth
      )(
        DualTypeNumVec2(
          dataTypeX=SInt(
            (cfg.objAffineDblTileSize2dPow.x + 4 + fracWidth) bits
            //cfg.objTileSize2dPow.x bits
          ),
          dataTypeY=SInt(
            (cfg.objAffineDblTileSize2dPow.y + 4 + fracWidth) bits
            //cfg.objTileSize2dPow.y bits
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
      val fxTilePxsCoord = (isAffine) generate Vec.fill(
        myTempObjTileWidth
      )(
        DualTypeNumVec2(
          dataTypeX=SInt(
            (cfg.objAffineDblTileSize2dPow.x + 4 + fracWidth) bits
          ),
          dataTypeY=SInt(
            (cfg.objAffineDblTileSize2dPow.y + 4 + fracWidth) bits
          ),
        )
      )
      //--------
    }
    case class WrObjPipePayloadStage7(isAffine: Boolean) extends Bundle {
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      val tilePxsCoord = Vec.fill(myTempObjTileWidth)(
        ObjTilePxsCoordT(isAffine)
      )
      val oorTilePxsCoord = (isAffine) generate Vec.fill(
        myTempObjTileWidth
      )(Vec2(Bool()))
      val affineDoIt = (isAffine) generate Vec.fill(
        myTempObjTileWidth
      )(Bool())
      val objPosYShift = SInt(cfg.objPxsCoordSize2dPow.y bits)
    }
    case class WrObjPipePayloadStage8(isAffine: Boolean) extends Bundle {
      val tileSlice = (!isAffine) generate Gpu2dTileSlice(
        cfg=cfg,
        isObj=true,
        isAffine=isAffine,
      )
      val tilePx = (isAffine) generate Vec.fill(
        cfg.objAffineSliceTileWidth
      )(
        UInt(cfg.objPalEntryMemIdxWidth bits)
      )
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      def myTempObjTileWidthPow = tempObjTileWidthPow(isAffine)
      val affineDoIt = (isAffine) generate Vec.fill(
        myTempObjTileWidth
      )(Bool())
      val pxPosXGridIdx = (
        Vec.fill(myTempObjTileWidth)(
          UInt(
            (
              cfg.objPxsCoordSize2dPow.x
              - myTempObjTileWidthPow
            )
            bits
          )
        )
      )
      val pxPosXGridIdxLsb = (
        Vec.fill(myTempObjTileWidth)(
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
      val tilePxsCoordPalEntryCmpPipe1 = (!isAffine) generate (
        Vec.fill(myTempObjTileWidth)(
          Vec2(Bool())
        )
      )
    }
    case class WrObjPipePayloadStage9(isAffine: Boolean) extends Bundle {
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      val palEntryMemIdx = Vec.fill(myTempObjTileWidth)(
        UInt(cfg.objPalEntryMemIdxWidth bits)
      )
    }
    case class WrObjPipePayloadStage10(isAffine: Boolean) extends Bundle {
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      val pxPosXGridIdxMatches = Vec.fill(myTempObjTileWidth)(Bool())

      // Whether `palEntryMemIdx` is non-zero
      val palEntryNzMemIdx = Vec.fill(myTempObjTileWidth)(Bool())
      val pxPosRangeCheck = Vec.fill(myTempObjTileWidth)(
        Vec2(Bool())
      )
    }
    case class WrObjPipePayloadStage12(
      isAffine: Boolean
    ) extends Bundle {
      def myTempObjTileWidth = tempObjTileWidth(isAffine)
      val palEntry = Vec.fill(myTempObjTileWidth)(
        Gpu2dPalEntry(cfg=cfg)
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
      val myIdxV2d = Vec.fill(
        tempObjTileWidth1(isAffine)
      )(
        Vec.fill(numMyIdxVecs(isAffine))(
          UInt(tempObjTileWidthPow1(isAffine) bits)
        )
      )
      val pxPosYLsb = Bool()
      //val pxPosXGridIdxLsb = Bool()
      val pxPosXGridIdx = UInt(
        (
          cfg.objPxsCoordSize2dPow.x
          - myTempObjTileWidthPow
        )
        bits
      )
    }
    case class WrObjPipePayloadStage14(
      isAffine: Boolean
    ) extends Bundle {
      def myTempObjTileWidth1 = tempObjTileWidth1(isAffine)
      def myTempObjTileWidthPow1 = tempObjTileWidthPow1(isAffine)
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
      val rdSubLineMemEntry = Vec.fill(
        myTempObjTileWidth
      )(
        ObjSubLineMemEntry()
      )
      val inMainVec = (
        Vec.fill(
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
          myTempObjTileWidth1
        )(
          Bool()
        )
      )
      val ext = WrObjPipe14Ext(
        isAffine=isAffine,
        useVec=true,
      )
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
      val stage13 = WrObjPipePayloadStage13(isAffine=isAffine)
      val stage14 = WrObjPipePayloadStage14(isAffine=isAffine)
      val stage15 = WrObjPipePayloadStage15(isAffine=isAffine)
      val stage16 = WrObjPipePayloadStage16(isAffine=isAffine)
    }
    case class WrObjPipePayload(
      isAffine: Boolean
    )
      extends Bundle with PipeMemRmwPayloadBase[
        Vec[ObjSubLineMemEntry],
        WrObjPipeSlmRmwHazardCmp,
      ]
    {
      // TODO: temporary assert, fix later!
      //assert(
      //  !isAffine
      //)
      val myPmCfg = (
        PipeMemRmwConfig[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ](
          wordType=Vec.fill(
            if (!isAffine) (
              cfg.objSliceTileWidth
            ) else (
              cfg.objAffineSliceTileWidth
            )
          )(ObjSubLineMemEntry()),
          wordCountArr=Array.fill(1)(
            if (!isAffine) (
              cfg.objSubLineMemArrSize
            ) else (
              cfg.objAffineSubLineMemArrSize
            )
          ).toSeq,
          hazardCmpType=WrObjPipeSlmRmwHazardCmp(
            //isAffine=false
            isAffine=isAffine
          ),
          modRdPortCnt=(
            wrObjPipeSlmRmwModRdPortCnt
          ),
          modStageCnt=(
            wrObjPipeSlmRmwModStageCnt
          ),
          pipeName=(
            if (!isAffine) (
              f"WrObjPipePayload"
            ) else (
              f"WrObjPipePayload_Affine"
            )
          ),
          linkArr=None,
          memArrIdx=0,
          optDualRd=false,
          initBigInt=None,
          optEnableClear=true,
          optModHazardKind=(
            PipeMemRmw.ModHazardKind.Dupl
          ),
          vivadoDebug=(
            vivadoDebug
          ),
        )
      )
      val subLineMemEntryExt = PipeMemRmwPayloadExt(
        cfg=myPmCfg,
        wordCount=(
          if (!isAffine) (
            cfg.objSubLineMemArrSize,
          ) else (
            cfg.objAffineSubLineMemArrSize
          )
        ),
      )
      def setPipeMemRmwExt(
        inpExt: PipeMemRmwPayloadExt[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ],
        ydx: Int,
        memArrIdx: Int,
      ): Unit = {
        subLineMemEntryExt := inpExt
      }
      def getPipeMemRmwExt(
        outpExt: PipeMemRmwPayloadExt[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ],
        ydx: Int,
        memArrIdx: Int,
      ): Unit = {
        outpExt := subLineMemEntryExt
      }
      def formalSetPipeMemRmwFwd(
        inpFwd: PipeMemRmwFwd[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ],
        memArrIdx: Int,
      ): Unit = {
        //subLineMemEntryFwd := inpFwd
      }
      def formalGetPipeMemRmwFwd(
        outpFwd: PipeMemRmwFwd[
          Vec[ObjSubLineMemEntry],
          WrObjPipeSlmRmwHazardCmp,
        ],
        memArrIdx: Int,
      ): Unit = {
        //outpFwd := subLineMemEntryFwd
      }

      val stage0 = WrObjPipePayloadStage0(isAffine=isAffine)
      def lineNum = stage0.lineNum
      def fullLineNum = stage0.fullLineNum
      def cnt = stage0.cnt
      def bakCnt = stage0.bakCnt
      def bakCntMinus1 = stage0.bakCntMinus1
      def objAttrsMemIdx = stage0.objAttrsMemIdx()
      def bakCntWillBeDone = stage0.bakCntWillBeDone()
      def objXStart() = stage0.objXStart()
      def affineObjXStart() = stage0.affineObjXStart()

      val postStage0 = WrObjPipePayloadPostStage0(isAffine=isAffine)

      def stage2 = postStage0.stage2
      def objAttrs = stage2.objAttrs

      def stage3 = postStage0.stage3
      def pxPos = stage3.pxPos
      def myIdxPxPosX = stage3.myIdxPxPosX

      def stage4 = postStage0.stage4

      def stage5 = postStage0.stage5
      def stage6 = postStage0.stage6

      def stage7 = postStage0.stage7
      def tilePxsCoord = stage7.tilePxsCoord
      def getObjSubLineMemArrIdx(
        kind: Int,
        x: UInt,
      ): UInt = {
        def myPxPosX = pxPos(x).x.asUInt
        if (kind == 0) {
          cfg.getObjSubLineMemArrIdx(
            addr=myPxPosX
          )
        } else {
          cfg.getObjAffineSubLineMemArrIdx(
            addr=myPxPosX
          )
        }
      }
      def getObjSubLineMemArrElemIdx(
        kind: Int,
        x: UInt,
      ): UInt = {
        def myPxPosX = pxPos(x).x.asUInt
        if (kind == 0) {
          cfg.getObjSubLineMemArrElemIdx(
            addr=myPxPosX
          )
        } else {
          cfg.getObjAffineSubLineMemArrElemIdx(
            addr=myPxPosX
          )
        }
      }
      def objPosYShift = stage7.objPosYShift

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
      def pxPosRangeCheckGePipe1 = (
        stage8.pxPosRangeCheckGePipe1
      )
      def pxPosRangeCheckLtPipe1 = (
        stage8.pxPosRangeCheckLtPipe1
      )
      def stage9 = postStage0.stage9
      def palEntryMemIdx = stage9.palEntryMemIdx

      def stage10 = postStage0.stage10
      def pxPosXGridIdxMatches = (
        stage10.pxPosXGridIdxMatches
      )
      def palEntryNzMemIdx = stage10.palEntryNzMemIdx
      def pxPosRangeCheck = stage10.pxPosRangeCheck

      def stage12 = postStage0.stage12
      def palEntry = stage12.palEntry
      def pxPosInLine = stage12.pxPosInLine
      def pxPosCmpForOverwrite = (
        stage12.pxPosCmpForOverwrite
      )
      //--------
      def stage13 = postStage0.stage13
      def stage14 = postStage0.stage14

      def stage15 = postStage0.stage15
      def rdSubLineMemEntry = stage15.rdSubLineMemEntry

      def stage16 = postStage0.stage16
      def overwriteLineMemEntry = (
        stage16.ext.overwriteLineMemEntry
      )
      def wrLineMemEntry = stage16.ext.wrLineMemEntry
    }
    def doInitWrObjPipePayload(
      isAffine: Boolean,
      firstInit: Boolean,
    ): WrObjPipePayload = {
      val ret = WrObjPipePayload(isAffine)
      if (firstInit) {
        ret.fullLineNum := wrObjFullLineNumInit
      } else {
        ret.fullLineNum := wrObjGlobPipe.rLineNumPipe1(
          ret.fullLineNum.bitsRange
        )
      }
      ret.cnt := 0
      ret.bakCnt := wrObjPipeBakCntStart(isAffine)
      ret.bakCntMinus1 := wrObjPipeBakCntStart(isAffine) - 1
      ret.stage0.rawAffineIdx() := (
        if (cfg.fancyObjPrio) {
          (1 << cfg.objAffineAttrsMemIdxTileCntWidth) - 1
        } else {
          0
        }
      )
      ret.stage0.justCopy.dbgTestIdx := (
        if (cfg.fancyObjPrio) {
          (
            1 << cfg.objAttrsMemIdxTileCntWidth
          ) - 1
        } else {
          0
        }
      )
      ret.postStage0 := ret.postStage0.getZero
      ret.subLineMemEntryExt := ret.subLineMemEntryExt.getZero
      ret
    }

    def combinePipeCntWidth = (
      log2Up(cfg.intnlFbSize2d.x + 1) + 2
    )
    def combinePipeFullCntWidth = (
      log2Up(cfg.intnlFbSize2d.x)
      + log2Up(cfg.physFbSize2dScale.x)
    )
    def combinePipeCntRange = (
      combinePipeFullCntWidth - 1
      downto log2Up(cfg.physFbSize2dScale.x)
    )
    //println(
    //  "cnt stuff: "
    //  + s"${cfg.intnlFbSize2d.x} ${cfg.physFbSize2d.x} "
    //  + s"${combinePipeCntWidth} ${combinePipeFullCntWidth} "
    //  + s"${combinePipeCntRange}"
    //)
    def combinePipeFullBakCntStart = (
      (
        cfg.intnlFbSize2d.x
        * (1 << log2Up(cfg.physFbSize2dScale.x))
      ) - 1
    )
    case class CombinePipeOut3Ext() extends Bundle {
      val bgRdSubLineMemEntry = BgSubLineMemEntry()
      val objRdSubLineMemEntry = ObjSubLineMemEntry()
      val objAffineRdSubLineMemEntry = (!noAffineObjs) generate (
        ObjSubLineMemEntry()
      )
      //setAsSVstruct()
    }
    def combinePipeNumMainStages = (
      9 // old, working value
      + cfg.physFbSize2dScale.x
    )
    case class CombinePipePayload(
    ) extends Bundle {
      //--------
      //val bg = WrBgPipePayload()
      //val obj = WrObjPipePayload()
      case class Stage0() extends Bundle {
        // combining can be done backwards
        val changingRow = Bool()
        def cnt = (
          fullCnt(
            combinePipeCntRange
          )
        )
        def bakCnt = (
          fullBakCnt(combinePipeCntRange)
        )
        val fullCnt = UInt(combinePipeFullCntWidth bits)
        val fullBakCnt = UInt(combinePipeFullCntWidth bits)
        //--------
        def bakCntWillBeDone() = (
          bakCnt === 0
        )
        def fullBakCntWillBeDone() = {
          def myPow = log2Up(cfg.physFbSize2dScale.x)
          (
            if (cfg.physFbSize2dScale.x == 1) (
              fullBakCnt === 0
            ) else (
              (
                fullBakCnt(fullBakCnt.high downto myPow) === 0
              ) && (
                fullCnt(myPow - 1 downto 0)
                === cfg.physFbSize2dScale.x - 1
              )
            )
          ) 
        }
        //--------
        //setAsSVstruct()
      }
      val stage0 = Stage0()
      def changingRow = stage0.changingRow
      def cnt = stage0.cnt
      def lineMemIdx = stage0.cnt
      def bakCnt = stage0.bakCnt
      def fullCnt = stage0.fullCnt
      def fullBakCnt = stage0.fullBakCnt
      def bakCntWillBeDone() = stage0.bakCntWillBeDone()
      def fullBakCntWillBeDone() = stage0.fullBakCntWillBeDone()

      case class Stage2() extends Bundle {
        val rdBg = Vec.fill(cfg.bgTileSize2d.x)(
          BgSubLineMemEntry()
        )
        val rdObj = Vec.fill(
          cfg.objSliceTileWidth
        )(
          ObjSubLineMemEntry()
        )
        val rdObjAffine = (!noAffineObjs) generate Vec.fill(
          cfg.objAffineSliceTileWidth
        )(
          ObjSubLineMemEntry()
        )
        //setAsSVstruct()
      }
      case class Stage3() extends Bundle {
        val ext = CombinePipeOut3Ext()
        //setAsSVstruct()
      }
      case class Stage4() extends Bundle {
        val objPickSubLineMemEntry = ObjSubLineMemEntry()
        //setAsSVstruct()
      }
      case class Stage5() extends Bundle {
        val objHiPrio = Bool()
        //setAsSVstruct()
      }
      case class Stage6() extends Bundle {
        val col = Gpu2dRgba(rgbConfig=cfg.rgbConfig)
        val colorMathInfo = (!noColorMath) generate (
          Gpu2dColorMathInfo(cfg=cfg)
        )
        val colorMathCol = (!noColorMath) generate (
          Gpu2dRgba(rgbConfig=cfg.rgbConfig)
        )
        //setAsSVstruct()
      }
      case class Stage7() extends Bundle {
        val combineWrLineMemEntry = BgSubLineMemEntry()
        //setAsSVstruct()
      }
      case class PostStage0() extends Bundle {
        val stage2 = Stage2()
        val stage3 = Stage3()
        val stage4 = Stage4()
        val stage5 = Stage5()
        val stage6 = Stage6()
        val stage7 = Stage7()
        val stage8 = Stage7()
        //setAsSVstruct()
      }
      val postStage0 = PostStage0()

      def stage2 = postStage0.stage2
      def stage3 = postStage0.stage3

      def stage4 = postStage0.stage4
      def objPickSubLineMemEntry = stage4.objPickSubLineMemEntry

      def stage5 = postStage0.stage5
      def objHiPrio = stage5.objHiPrio

      def stage6 = postStage0.stage6
      def col = stage6.col
      def colorMathInfo = (!noColorMath) generate stage6.colorMathInfo
      def colorMathCol = (!noColorMath) generate stage6.colorMathCol

      def stage7 = postStage0.stage7

      def stage8 = postStage0.stage8
      def combineWrLineMemEntry = stage8.combineWrLineMemEntry
      //--------
      //setAsSVstruct()
    }
    def doInitCombinePipePayload(
      changingRow: Bool,
    ): CombinePipePayload = {
      val ret = CombinePipePayload()
      ret.changingRow := changingRow
      ret.fullCnt := (
        0
      )
      ret.fullBakCnt := combinePipeFullBakCntStart
      ret.postStage0 := ret.postStage0.getZero
      ret
    }

    //--------
    val combinePipeOverflow = Bool()
    val wrBgPipe = PipeHelper(linkArr=linkArr)
    def nWrBgArr = wrBgPipe.nArr
    def sWrBgArr = wrBgPipe.sArr
    def s2mWrBgArr = wrBgPipe.s2mArr
    def cWrBgArr = wrBgPipe.cArr
    for (idx <- 0 until wrBgObjPipeNumStages + 1) {
      wrBgPipe.addStage(
        name=(s"WrBgPipe_$idx"),
        optIncludeS2M=false,
      )
    }
    wrBgPipe.addStage(
      name="WrBgPipe_Last",
      optIncludeS2M=false,
      finish=true,
    )
    val nWrBgPipeLast = nWrBgArr.last
    val wrBgPipePayload = Payload(WrBgPipePayload())
    def wrBgPipeLast = nWrBgPipeLast(
      wrBgPipePayload
    )
    def initTempWrBgPipeOut(
      idx: Int,
    ): (WrBgPipePayload, WrBgPipePayload) = {
      // This function returns `(tempInp, tempOutp)`
      val pipeIn = (
        cWrBgArr(idx).up(wrBgPipePayload)
      )
      val pipeOut = WrBgPipePayload()

      pipeOut := pipeIn
      pipeOut.allowOverride
      cWrBgArr(idx).bypass(wrBgPipePayload) := pipeOut

      (pipeIn, pipeOut)
    }

    when (intnlChangingRowRe) {
      nextWrBgChangingRow := False
    } elsewhen (
      wrBgPipeLast.bakCntWillBeDone() && nWrBgPipeLast.isFiring
    ) {
      nextWrBgChangingRow := True
    } otherwise {
      nextWrBgChangingRow := rWrBgChangingRow
    }

    val nextWrBgPipeFrontValid = Bool()
    val rWrBgPipeFrontValid = RegNext(nextWrBgPipeFrontValid)
      .init(True)
    val rSavedWrBgPipeFrontValid = Reg(Bool()) init(False)
    val rWrBgPipeFrontPayload = Reg(WrBgPipePayload())
    rWrBgPipeFrontPayload.init(doInitWrBgPipePayload(
      firstInit=true
    ))
    val wrBgPipeFront = Flow(WrBgPipePayload())
    wrBgPipeFront.valid := rWrBgPipeFrontValid
    wrBgPipeFront.payload := rWrBgPipeFrontPayload

    nWrBgArr(0).driveFrom(wrBgPipeFront)(
      con=(node, payload) => {
        node(wrBgPipePayload) := payload
      },
    )

    //--------
    // Send data into the background pipeline
    rSavedWrBgPipeFrontValid := nextWrBgPipeFrontValid
    when (!rSavedWrBgPipeFrontValid) {
      when (intnlChangingRowRe) {
        nextWrBgPipeFrontValid := True
        rWrBgPipeFrontPayload := doInitWrBgPipePayload(
          firstInit=false
        )
      } otherwise {
        nextWrBgPipeFrontValid := rWrBgPipeFrontValid
      }
    } otherwise { // when (rSavedWrBgPipeFrontValid)
      when (nWrBgArr(0).isFiring) {
        // BEGIN: test logic
        when (rWrBgPipeFrontPayload.bakCntWillBeDone()) {
          nextWrBgPipeFrontValid := False
        } otherwise {
          nextWrBgPipeFrontValid := rWrBgPipeFrontValid
          rWrBgPipeFrontPayload.cnt := rWrBgPipeFrontPayload.cnt + 1
          rWrBgPipeFrontPayload.bakCnt := (
            rWrBgPipeFrontPayload.bakCnt - 1
          )
          rWrBgPipeFrontPayload.bakCntMinus1 := (
            rWrBgPipeFrontPayload.bakCntMinus1 - 1
          )
        }
        // END: test logic
      } otherwise { // when (!wrBgPipeIn(0).fire)
        nextWrBgPipeFrontValid := rWrBgPipeFrontValid
      }
    }

    val nWrObjArr = Array.fill(wrBgObjPipeNumStages + 1)(Node())
    val sWrObjArr = new ArrayBuffer[StageLink]()
    val cWrObjArr = new ArrayBuffer[CtrlLink]()
    def wrObjPipeIdxSlmRmwFront = 14
    def wrObjPipeIdxSlmRmwModFront = 15
    def wrObjPipeIdxSlmRmwModBack = 17
    def wrObjPipeIdxSlmRmwBack = 18
    def wrObjPipeSlmRmwModStageCnt = (
      wrObjPipeIdxSlmRmwModBack
      - wrObjPipeIdxSlmRmwModFront
      + 1
    )
    def wrObjPipeSlmRmwModRdPortCnt = (
      1
    )

    def wrObjPipeNumForkOrJoinRenderers = (
      2 // the object writing pipeline and the combining pipeline
    )
    //--------

    val wrObjPipePayloadSlmRmwModFrontInp = Array.fill(
      cfg.numLineMemsPerBgObjRenderer
    )(
      Payload(WrObjPipePayload(isAffine=false))
    )
    val wrObjPipePayloadSlmRmwModFrontOutp = (
      Payload(WrObjPipePayload(isAffine=false))
    )
    val wrObjPipePayloadSlmRmwBackInp = Array.fill(
      cfg.numLineMemsPerBgObjRenderer
    )(
      Payload(WrObjPipePayload(isAffine=false))
    )
    val wrObjPipePayloadSlmRmwBackOutp = (
      Payload(WrObjPipePayload(isAffine=false))
    )

    val wrObjPipePayloadMain = new ArrayBuffer[
      Payload[WrObjPipePayload]
    ]()
    for (idx <- 0 until wrBgObjPipeNumStages + 1) {
      wrObjPipePayloadMain += (
        Payload(WrObjPipePayload(isAffine=false))
        .setName(s"wrObjPipePayloadMain_${idx}")
      )
    }

    for (idx <- 0 until nWrObjArr.size - 1) {
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

        cWrObjArr += CtrlLink(
          up=sWrObjArr.last.down,
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
        val nfMyArr = new ArrayBuffer[Node]()
        for (jdx <- 0 until wrObjPipeNumForkOrJoinRenderers) {
          nfMyArr += (
            Node()
          )
          nfMyArr.last.setName(s"nfMyArr_front_$jdx")
        }
        val fMyDown = ForkLink(
          up=down,
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

          wrObjSubLineMemArr(jdx).io.front(
            wrObjSubLineMemArr(jdx).io.frontPayload
          ) := (
            s2mLink.down(wrObjPipePayloadMain(idx + 1))
          )
        }
      } else if (idx == wrObjPipeIdxSlmRmwModFront) {
        //println(s"idx == modFront: $idx")
        val njMyArr = new ArrayBuffer[Node]()
        for (jdx <- 0 until wrObjPipeNumForkOrJoinRenderers) {
          njMyArr += (
            Node()
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
          njMyArr(jdx)(
            wrObjPipePayloadSlmRmwModFrontInp(jdx)
          ) := (
            wrObjSubLineMemArr(
              jdx
            ).io.modFront(
              wrObjSubLineMemArr(jdx).io.modFrontPayload
            )
          )
          if (vivadoDebug) {
            //njMyArr(jdx)(
            //  wrObjPipePayloadSlmRmwModFrontInp(jdx)
            //).addAttribute("MARK_DEBUG", "TRUE")
          }
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
          wrObjSubLineMemArr(jdx).io.midModStages(0)(
            PipeMemRmw.extIdxUp
          ) := (
            RegNext(
              wrObjSubLineMemArr(jdx).io.midModStages(0)(
                PipeMemRmw.extIdxUp
              )
            )
            init(
              wrObjSubLineMemArr(jdx).io.midModStages(0)(
                PipeMemRmw.extIdxUp
              ).getZero
            )
          )
          when (dMyWrObj.up.isValid) {
            wrObjSubLineMemArr(
              jdx
            ).io.midModStages(0)(PipeMemRmw.extIdxUp) := (
              dMyWrObj.up(wrObjPipePayloadSlmRmwModFrontInp(
                jdx
              ))
            )
          }
          wrObjSubLineMemArr(jdx).io.midModStages(0)(
            PipeMemRmw.extIdxSaved
          ) := (
            RegNextWhen(
              wrObjSubLineMemArr(
                jdx
              ).io.midModStages(0)(PipeMemRmw.extIdxUp),
              dMyWrObj.up.isFiring
            )
            init(
              wrObjSubLineMemArr(jdx).io.midModStages(0)(
                PipeMemRmw.extIdxSaved
              )
              .getZero
            )
          )
        }
        switch ((wrObjPipeLineMemArrIdx(4) + 0)(0 downto 0)) {
          for (jdx <- 0 until (1 << wrObjPipeLineMemArrIdx(4).getWidth)) {
            is (jdx) {
              dMyWrObj.down(
                wrObjPipePayloadSlmRmwModFrontOutp
              ) := (
                dMyWrObj.up(
                  wrObjPipePayloadSlmRmwModFrontInp(jdx)
                )
              )
            }
          }
        }
        addMainLinks(
          up=Some(dMyWrObj.down),
          down=None,
        )
      } else if (idx == wrObjPipeIdxSlmRmwModFront + 1) {
        //println(s"idx == modFront + 1: ${idx}")
        for (jdx <- 0 until wrObjPipeNumForkOrJoinRenderers) {
          wrObjSubLineMemArr(jdx).io.midModStages(1)(
            PipeMemRmw.extIdxUp
          ) := (
            RegNext(wrObjSubLineMemArr(jdx).io.midModStages(1)(
              PipeMemRmw.extIdxUp
            ))
            init(
              wrObjSubLineMemArr(jdx).io.midModStages(1)(
                PipeMemRmw.extIdxUp
              )
              .getZero
            )
          )
          when (nWrObjArr(idx).isValid) {
            wrObjSubLineMemArr(jdx).io.midModStages(1)(
              PipeMemRmw.extIdxUp
            ) := (
              nWrObjArr(idx)
              (
                wrObjPipePayloadMain(idx /*+ 1*/)
              )
            )
          }
          wrObjSubLineMemArr(jdx).io.midModStages(1)(
            PipeMemRmw.extIdxSaved
          ) := (
            RegNextWhen(
              wrObjSubLineMemArr(jdx).io.midModStages(1)(
                PipeMemRmw.extIdxUp
              ),
              nWrObjArr(idx).isFiring
            )
          )
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
        val nfMyArr = new ArrayBuffer[Node]()
        for (jdx <- 0 until wrObjPipeNumForkOrJoinRenderers) {
          nfMyArr += (
            wrObjSubLineMemArr(jdx).io.modBack
          )
          nfMyArr.last.setName(s"nfMyArr_modBack_$jdx")
        }
        val fMyDown = ForkLink(
          up=down,
          downs=nfMyArr.toSeq,
          synchronous=true,
        )
        linkArr += fMyDown
        for (jdx <- 0 until nfMyArr.size) {
          wrObjSubLineMemArr(jdx).io.modBack(
            wrObjSubLineMemArr(jdx).io.modBackPayload
          ) := (
            nfMyArr(jdx)(
              wrObjPipePayloadMain(idx + 1)
            )
          )
          wrObjSubLineMemArr(jdx).io.midModStages(2)(
              PipeMemRmw.extIdxUp
          ):= (
            RegNext(
              wrObjSubLineMemArr(jdx).io.midModStages(2)(
                PipeMemRmw.extIdxUp
              )
            )
            init(
              wrObjSubLineMemArr(jdx).io.midModStages(2)(
                PipeMemRmw.extIdxUp
              )
              .getZero
            )
          )
          when (nWrObjArr(idx).isValid) {
            wrObjSubLineMemArr(jdx).io.midModStages(2)(
              PipeMemRmw.extIdxUp
            ) := (
              nWrObjArr(idx)
              /*down*/
              (
                wrObjPipePayloadMain(idx /*+ 1*/)
              )
            )
          }
          wrObjSubLineMemArr(jdx).io.midModStages(2)(
            PipeMemRmw.extIdxSaved
          ) := (
            RegNextWhen(
              wrObjSubLineMemArr(jdx).io.midModStages(2)(
                PipeMemRmw.extIdxUp
              ),
              nWrObjArr(idx).isFiring
            )
          )
          //--------
        }
      } else if (idx == wrObjPipeIdxSlmRmwBack) {
        //println(s"idx == back: $idx")
        //println("wrObjPipeIdxSlmRmwBack")

        val njMyArr = new ArrayBuffer[Node]()
        for (jdx <- 0 until wrObjPipeNumForkOrJoinRenderers) {
          njMyArr += (
            Node()
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
          njMyArr(jdx)(
            wrObjPipePayloadSlmRmwBackInp(jdx)
          ) := (
            wrObjSubLineMemArr(jdx).io.back(
              wrObjSubLineMemArr(jdx).io.backPayload
            )
          )
        }
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
            }
            default {
              //println(s"default ${jdx}")
            }
          }
        }
        addMainLinks(
          up=Some(dMyWrObj.down),
          down=None,
        )
      } else {
        addMainLinks()
      }
    }
    val nWrObjPipeLast = nWrObjArr.last
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
    ): (WrObjPipePayload, WrObjPipePayload) = {
      // This function returns `(tempInp, tempOutp)`
      def pipeIn = (
        if (idx == wrObjPipeIdxSlmRmwModFront) {
          cWrObjArr(idx).up(wrObjPipePayloadSlmRmwModFrontOutp)
        } 
        else if (idx == wrObjPipeIdxSlmRmwBack) {
          cWrObjArr(idx).up(wrObjPipePayloadSlmRmwBackOutp)
        } else {
          cWrObjArr(idx).up(wrObjPipePayloadMain(idx))
        }
      )
      def pipeOut = wrObjPipeOutArr(idx)
      pipeOut := pipeIn
      pipeOut.allowOverride

      def tempPipeOut = tempWrObjPipeOutArr(idx)
      tempPipeOut := (
        RegNext(tempPipeOut) init(tempPipeOut.getZero)
      )
      when (cWrObjArr(idx).up.isValid) {
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
    val wrObjAffinePipePayload = Payload(WrObjPipePayload(
      isAffine=true
    ))
    def wrObjAffinePipeLast = nWrObjAffinePipeLast(
      wrObjAffinePipePayload
    )
    def initTempWrObjAffinePipeOut(
      idx: Int,
    ): (WrObjPipePayload, WrObjPipePayload) = {
      // This function returns `(tempInp, tempOutp)`
      val pipeIn = (
        cWrObjAffineArr(idx).up(wrObjAffinePipePayload)
      )
      val pipeOut = WrObjPipePayload(isAffine=true)

      pipeOut := pipeIn
      pipeOut.allowOverride
      cWrObjAffineArr(idx).bypass(wrObjAffinePipePayload) := pipeOut

      (pipeIn, pipeOut)
    }

    case class WrObjPipe14Ext(
      isAffine: Boolean,
      useVec: Boolean//=true
    ) extends Bundle {
      def vecSize = (
        if (useVec) {
          cfg.tempObjTileWidth(isAffine=isAffine)
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
      //setAsSVstruct()
    }
    when (intnlChangingRowRe) {
      nextWrObjChangingRow := False
    } elsewhen (
      wrObjPipeLast.bakCntWillBeDone && nWrObjPipeLast.isFiring
    ) {
      nextWrObjChangingRow := True
    } otherwise {
      nextWrObjChangingRow := rWrObjChangingRow
    }

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
        node(wrObjPipePayloadMain(0)) := payload
      },
    )

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
            if (cfg.fancyObjPrio) {
              rWrObjPipeFrontPayload.stage0.rawAffineIdx() - 1
            } else {
              rWrObjPipeFrontPayload.stage0.rawAffineIdx() + 1
            }
          )
          rWrObjPipeFrontPayload.stage0.rawObjAttrsMemIdx() := (
            if (cfg.fancyObjPrio) {
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

    if (!noAffineObjs) {
      when (intnlChangingRowRe) {
        nextWrObjAffineChangingRow := False
      } elsewhen (
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
          node(wrObjAffinePipePayload) := payload
        },
      )

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
        when (nWrObjAffineArr(0).isFiring) {
          when (rWrObjAffinePipeFrontPayload.bakCntWillBeDone) {
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
              if (cfg.fancyObjPrio) {
                rWrObjAffinePipeFrontPayload.stage0.rawAffineIdx() - 1
              } else {
                rWrObjAffinePipeFrontPayload.stage0.rawAffineIdx() + 1
              }
            )
            rWrObjAffinePipeFrontPayload.stage0.rawObjAttrsMemIdx() := (
              if (cfg.fancyObjPrio) {
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

    def combineObjForkJoinMax = (
      if (!noAffineObjs) {
        2
      } else {
        1
      }
    )
    def combineBgObjForkJoinMax = (
      combineObjForkJoinMax + 1
    )
    def combineTotalForkOrJoinMax = (
      (
        cfg.numLineMemsPerBgObjRenderer
        * (
          combineObjForkJoinMax
          + cfg.combineBgSubLineMemArrSize
        )
      )
    )
    //--------
    val nCombineArr = Array.fill(combinePipeNumMainStages + 1)(Node())
    val sCombineArr = new ArrayBuffer[StageLink]()
    val s2mCombineArr = new ArrayBuffer[S2MLink]()
    val cCombineArr = new ArrayBuffer[CtrlLink]()

    val nfCombineArr = Array.fill(combineTotalForkOrJoinMax)(Node())
    val njCombineArr = Array.fill(combineTotalForkOrJoinMax)(Node())
    val fDriveToStmArr = Array.fill(combineTotalForkOrJoinMax)(
      Stream(CombinePipePayload())
    )
    val cfjCombineArr = new ArrayBuffer[CtrlLink]()

    val combinePipePreForkPayload = Payload(CombinePipePayload())
    val combinePipeJoinPayloadArr = (
      Array.fill(combineTotalForkOrJoinMax)(
        Payload(CombinePipePayload())
      )
    )
    val combinePipePostJoinPayload = Payload(CombinePipePayload())
    val combinePipeLast = nCombinePipeLast(
      combinePipePostJoinPayload
    )
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
        def theMax = combineObjForkJoinMax //combineBgObjForkJoinMax - 1
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
        for (jdx <- 0 until combineTotalForkOrJoinMax) {
          def zdx = (
            jdx
            - (
              cfg.numLineMemsPerBgObjRenderer
              * cfg.combineBgSubLineMemArrSize
            )
          )
          def nfMyCombine = (
            fMyCombine.downs(jdx)
          )
          def njMyCombine = (
            jMyCombine.ups(jdx)
          )
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
            false
          ) {
          } else {
            def myLineMem = {
              if (zdx < 0) {
                val kdx = (
                  jdx / cfg.numLineMemsPerBgObjRenderer
                )
                val tempJdx = (
                  jdx % cfg.numLineMemsPerBgObjRenderer
                )
                val tempSize = combineBgSubLineMemA2d(
                  tempJdx
                ).size
                combineBgSubLineMemA2d(
                  tempJdx
                )(
                  kdx
                )
              } else {
                if (zdx % theMax == 0) {
                  combineObjSubLineMemArr(zdx / theMax)
                } else {
                  if (!noAffineObjs) {
                    combineObjAffineSubLineMemArr(zdx / theMax)
                  } else {
                    combineObjSubLineMemArr(zdx / theMax)
                  }
                }
              }
            }
            fMyDriveToStm.translateInto(myLineMem.io.rdAddrPipe)(
              dataAssignment=(
                o,
                i,
              ) => {
                o.addr := (
                  if (zdx < 0) {
                    cfg.getBgSubLineMemArrIdx(
                      addr=i.cnt,
                    )
                  } else {
                    if (zdx % theMax == 0) {
                      cfg.getObjSubLineMemArrIdx(
                        addr=i.cnt,
                      )
                    } else {
                      if (!noAffineObjs) { // if (zdx % theMax1 == 2)
                        cfg.getObjAffineSubLineMemArrIdx(
                          addr=i.cnt,
                        )
                      } else {
                        cfg.getObjSubLineMemArrIdx(
                          addr=i.cnt,
                        )
                      }
                    }
                  }
                )
                o.data := i
              }
            )

            njMyCombine.driveFrom(myLineMem.io.rdDataPipe)(
              con=(
                node,
                payload,
              ) => {
                node(combinePipeJoinPayloadArr(jdx)) := payload
              }
            )
          }
        }

        //--------
        sCombineArr += StageLink(
          up=jMyCombine.down,
          down=Node(),
        )
        switch (combineLineMemArrIdx) {
          for (
            innerCombineIdx <- 0
            until cfg.numLineMemsPerBgObjRenderer
          ) {
            is (innerCombineIdx) {
              //--------
              def myOutpPayload = (
                jMyCombine.down(combinePipePostJoinPayload)
              )
              def myInpPayload(inpIdx: Int) = {
                jMyCombine.down(combinePipeJoinPayloadArr(inpIdx))
              }
              myOutpPayload.allowOverride
              myOutpPayload := myInpPayload(0)
              for (jdx <- 0 until cfg.bgTileSize2d.x) {
                def kdx = (
                  jdx
                  / cfg.combineBgSubLineMemVecElemSize
                )
                val tempKdx = (
                  (
                    kdx
                    * cfg.numLineMemsPerBgObjRenderer
                  )
                  + innerCombineIdx //* theMax
                )
                myOutpPayload.stage2.rdBg(jdx) := (
                  myInpPayload(
                    tempKdx
                  )
                    .stage2.rdBg(jdx)
                )
              }
              myOutpPayload.stage2.rdObj := {
                val tempIndex = (
                  (
                    cfg.combineBgSubLineMemArrSize
                    * cfg.numLineMemsPerBgObjRenderer
                  ) + (
                    innerCombineIdx * theMax //+ 1
                  )
                )
                myInpPayload(
                  tempIndex
                ).stage2.rdObj
              }
              if (!noAffineObjs) {
                myOutpPayload.stage2.rdObjAffine := {
                  val tempIndex = (
                    (
                      cfg.combineBgSubLineMemArrSize
                      * cfg.numLineMemsPerBgObjRenderer
                    ) + (
                      innerCombineIdx * theMax + 1 //+ 2
                    )
                  )
                  myInpPayload(
                    tempIndex
                  ).stage2.rdObjAffine
                }
              }
              //--------
            }
          }
        }
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
    def initCombinePipeOut(
      idx: Int,
      payload: Payload[CombinePipePayload],
    ): (CombinePipePayload, CombinePipePayload) = {
      // This function returns `(tempInp, tempOutp)`
      val pipeIn = (
        cCombineArr(idx).up(payload)
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
    nextCombineChangingRow := (
      combinePipeLast.changingRow
    )

    val nextCombinePipeFrontValid = Bool()
    val rCombinePipeFrontValid = RegNext(nextCombinePipeFrontValid)
      .init(True)
    val rSavedCombinePipeFrontValid = Reg(Bool()) init(False)
    val rCombinePipeFrontPayload = Reg(CombinePipePayload())
    rCombinePipeFrontPayload.init(doInitCombinePipePayload(
      changingRow=True,
    ))

    val haltCombinePipeVeryFront = Bool()
    val combinePipeInVeryFront = Stream(CombinePipePayload())
    val combinePipeInVeryFrontHaltWhen = combinePipeInVeryFront.haltWhen(
      haltCombinePipeVeryFront
    )
    combinePipeInVeryFront.valid := (
      rCombinePipeFrontValid //&& !haltCombinePipeVeryFront
    )
    combinePipeInVeryFront.payload := rCombinePipeFrontPayload
    nCombineArr(0).driveFrom(
      combinePipeInVeryFrontHaltWhen
    )(
      con=(node, payload) => {
        node(combinePipePreForkPayload) := payload
      }
    )

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
    // END: old combine code
    //--------
    val rPrevCombinePipeLastFullCnt = (
      Reg(UInt(combinePipeFullCntWidth bits))
      init(U(combinePipeFullCntWidth bits, default -> True))
    )
    val combinePipeLastPreThrown = Stream(CombinePipePayload())
    val combinePipeLastPreThrownDoThrow = KeepAttribute(
      rPrevCombinePipeLastFullCnt.resized
      === combinePipeLastPreThrown.fullCnt
    )
    val combinePipeLastThrown = (
      combinePipeLastPreThrown.throwWhen(
        combinePipeLastPreThrownDoThrow
      )
    )
    nCombinePipeLast.driveTo(combinePipeLastPreThrown)(
      con=(
        payload,
        node,
      ) => {
        payload := node(combinePipePostJoinPayload)
      }
    )
    when (combinePipeLastThrown.fire) {
      rPrevCombinePipeLastFullCnt := (
        combinePipeLastThrown.fullCnt//.resized
      )
    }
    val prePopStm = cloneOf(pop)
    pop <-/< prePopStm
    combinePipeLastThrown.translateInto(
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

    rSavedCombinePipeFrontValid := nextCombinePipeFrontValid
    nextCombinePipeFrontValid := True

    val rPastHaltCombinePipe = RegNext(haltCombinePipeVeryFront)
      .init(False)
    when (intnlChangingRowRe) {
      haltCombinePipeVeryFront := False
    } elsewhen (
      combinePipeInVeryFront.changingRow
    ) {
      haltCombinePipeVeryFront := True
    } otherwise {
      haltCombinePipeVeryFront := rPastHaltCombinePipe
    }
    when (combinePipeInVeryFront.fire) {
      // BEGIN: test logic

      when (
        rCombinePipeFrontPayload.fullBakCntWillBeDone()
      ) {
        rCombinePipeFrontPayload := doInitCombinePipePayload(
          changingRow=True,
        )
      } otherwise {
        def myPow = log2Up(cfg.physFbSize2dScale.x)
        def myCnt = rCombinePipeFrontPayload.fullCnt
        when (
          if (cfg.physFbSize2dScale.x > 1) (
            myCnt(myPow - 1 downto 0)
            === cfg.physFbSize2dScale.x - 1
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

        } otherwise {
          rCombinePipeFrontPayload.changingRow := False
          rCombinePipeFrontPayload.fullCnt := (
            rCombinePipeFrontPayload.fullCnt + 1
          )
          rCombinePipeFrontPayload.fullBakCnt := (
            rCombinePipeFrontPayload.fullBakCnt - 1
          )
        }
      }
      // END: test logic
    }


    def writeBgLineMemEntries(
    ): Unit = {
      // Handle backgrounds

      // BEGIN: stage 0
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) =>
      {
        def idx = 0
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx)

        switch (pipeIn.bgIdx) {
          for (tempBgIdx <- 0 until cfg.numBgs) {
            is (tempBgIdx) {
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
        pipeOut.fullLineNum := pipeIn.fullLineNum
        pipeOut.cnt := pipeIn.cnt
        pipeOut.bakCnt := pipeIn.bakCnt
        pipeOut.bakCntMinus1 := pipeIn.bakCntMinus1
        pipeOut.bgIdx := pipeIn.bgIdx
      }
      // END: Stage 0

      //// BEGIN: post stage 0
      // BEGIN: Stage 1
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 1
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (kind <- 0 until cfg.totalNumBgKinds) {
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

          val tempPxPosXGridIdx = cloneOf(tempOutp.pxPosXGridIdx)
          switch (pipeIn.bgIdx) {
            for (tempBgIdx <- 0 until cfg.numBgs) {
              is (tempBgIdx) {
                val dbgPxPosXGridIdxFindFirstSameAs: (Bool, UInt) = (
                  tempOutp.pxPosXGridIdx.sFindFirst(
                    _(0) === tempAttrs.scroll.x(
                      cfg.bgTileSize2dPow.x - 1
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
                    _(0) =/= tempAttrs.scroll.x(
                      cfg.bgTileSize2dPow.x - 1
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
          for (x <- 0 until cfg.bgTileSize2d.x) {
            tempOutp.pxPosXGridIdx(x) := tempOutp.pxPos(x).x(
              tempOutp.pxPos(x).x.high
              downto cfg.bgTileSize2dPow.x
            ).resized
            switch (pipeIn.bgIdx) {
              for (tempBgIdx <- 0 until cfg.numBgs) {
                is (tempBgIdx) {
                  def tempBgTileWidthPow = cfg.bgTileSize2dPow.x
                  tempOutp.pxPos(x).x := (
                    Cat(
                      pipeIn.cnt(
                        (
                          log2Up(cfg.intnlFbSize2d.x)
                          + cfg.numBgsPow
                          - cfg.bgTileSize2dPow.x
                          - 1
                        ) downto (
                          cfg.numBgsPow
                        )
                      ),
                      U(f"$tempBgTileWidthPow'd$x")
                    ).asUInt
                    - tempAttrs.scroll.x
                  )
                  tempOutp.pxPos(x).y := (
                    (
                      pipeIn.lineNum
                      - tempAttrs.scroll.y
                    )
                  )
                  def tempSliceRange = ElabVec2(
                    x=(
                      // BEGIN: old, pre-multi-pixel pipeline
                      (
                        cfg.bgSize2dInTilesPow.x
                        + cfg.bgTileSize2dPow.x - 1
                      )
                      downto cfg.bgTileSize2dPow.x
                      // END: old, pre-multi-pixel pipeline
                    )
                    ,
                    y=(
                      // BEGIN: old, pre-multi-pixel pipeline
                      (
                        cfg.bgSize2dInTilesPow.y
                        + cfg.bgTileSize2dPow.y - 1
                      )
                      downto cfg.bgTileSize2dPow.y
                      // END: old, pre-multi-pixel pipeline
                    )
                    ,
                  )
                  tempOutp.bgEntryMemIdx(x) := Cat(
                    tempOutp.pxPos(x).y(tempSliceRange.y),
                    tempOutp.pxPos(x).x(tempSliceRange.x),
                  ).asUInt
                }
              }
            }
          }
        }
      }
      // END: Stage 1

      // BEGIN: Stage 2
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 2
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (kind <- 0 until cfg.totalNumBgKinds) {
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
              cfg.intnlFbSize2d.y
              * (cfg.intnlFbSize2d.x / cfg.bgTileSize2d.x)
            )
          ).resized
          tempOutp.stage2.fbRdAddrMultPxPosY := (
            tempInp.pxPos(0).y(
              log2Up(cfg.intnlFbSize2d.y) - 1 downto 0
            ) * (
              cfg.intnlFbSize2d.x / cfg.bgTileSize2d.x
            )
          ).resized

          for (arrIdx <- 0 until cfg.numBgMemsPerNonPalKind) {
            for (tempBgIdx <- 0 until cfg.numBgs) {
              def arr = (
                if (kind == 0) {
                  bgEntryMemA2d(tempBgIdx)(
                    arrIdx
                  )
                } else {
                  colorMathEntryMemArr(arrIdx)
                }
              )
              arr.io.rdEn := True
              arr.io.rdAddr := 0
              if (kind != 0) {
                arr.io.rdEn.allowOverride
                arr.io.rdAddr.allowOverride
              }
            }
          }
          switch (pipeIn.stage0.bgIdx) {
            for (tempBgIdx <- 0 until cfg.numBgs) {
              is (tempBgIdx) {
                def setRdAddr(
                  someTempRdAddr: UInt,
                  someVecIdx: UInt,
                ): Unit = {
                  def tempMemArr = (
                    if (kind == 0) {
                      bgEntryMemA2d(tempBgIdx)
                    } else {
                      colorMathEntryMemArr
                    }
                  )
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
              }
            }
          }
        }
      }
      // END: Stage 2

      // BEGIN: Stage 3
      {
        def idx = 3
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (kind <- 0 until cfg.totalNumBgKinds) {
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
          for (x <- 0 until cfg.bgTileSize2d.x) {
            tempOutp.bgEntry(x) := tempOutp.bgEntry(x).getZero
            switch (pipeIn.bgIdx) {
              for (tempBgIdx <- 0 until cfg.numBgs) {
                is (tempBgIdx) {
                  def tempMemArr = (
                    if (kind == 0) {
                      bgEntryMemA2d(tempBgIdx)
                    } else {
                      colorMathEntryMemArr
                    }
                  )
                  for (
                    myTempIdx <- 0 until cfg.numBgMemsPerNonPalKind
                  ) {
                    tempOutp.stage3.bgEntry(myTempIdx)(x).assignFromBits(
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
                tempOutp.stage3.tempAddr(x) := (
                  tempInp.stage2.bgEntryMemIdxSameAs
                )
              }
              is (M"01") {
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
                    tempOutp.stage3.tempAddr(x) := (
                      tempInp.stage2.bgEntryMemIdxDiff
                    )
                  } otherwise {
                    tempOutp.stage3.tempAddr(x) := (
                      tempInp.stage2.bgEntryMemIdxSameAs
                    )
                  }
                } otherwise {
                  // this indicates `sameAsIdx < diffIdx`
                  when (x < diffIdx) {
                    tempOutp.stage3.tempAddr(x) := (
                      tempInp.stage2.bgEntryMemIdxSameAs
                    )
                  } otherwise {
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
      // END: Stage 3

      // BEGIN: Stage 4
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //    idx: Int,
      //  ) => 
      {
        def idx = 4
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until cfg.totalNumBgKinds
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

          for (x <- 0 until cfg.bgTileSize2d.x) {
            tempOutp.bgEntry(x) := tempOutp.bgEntry(x).getZero
            switch (pipeIn.bgIdx) {
              for (tempBgIdx <- 0 until cfg.numBgs) {
                is (tempBgIdx) {
                  //--------
                  def setBgEntry(
                    someTempRdAddr: UInt
                  ): Unit = {
                    switch (someTempRdAddr(0 downto 0)) {
                      for (myTempIdx <- 0 until 2) {
                        is (myTempIdx) {
                          tempOutp.bgEntry(x) := (
                            tempInp.stage3.bgEntry(myTempIdx)(x)
                          )
                        }
                      }
                    }
                  }
                  setBgEntry(
                    someTempRdAddr=tempInp.stage3.tempAddr(x)
                  )
                }
              }
            }
          }
        }
      }
      // END: Stage 4

      // BEGIN: Stage 5
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //    idx: Int,
      //) => 
      {
        def idx = 5
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (kind <- 0 until cfg.totalNumBgKinds) {
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

          for (x <- 0 until cfg.bgTileSize2d.x) {
            tempOutp.stage5.fbRdAddrFinalPlus(x) := (
              tempInp.stage4.fbRdAddrMultPlus
              + tempInp.pxPos(x).x(
                log2Up(cfg.intnlFbSize2d.x) - 1
                downto cfg.bgTileSize2dPow.x
              ).resized
            ).resized
          }

        }
      }
      // END: Stage 5

      // BEGIN: Stage 6
      //  pipeStageMainFunc=(
      //    stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //    idx: Int,
      //  ) => 
      {
        def idx = 6
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (
          kind <- 0 until cfg.totalNumBgKinds
        ) {
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
          def someTileMemArr = (
            if (kind == 0) {
              bgTileMemArr
            } else {
              colorMathTileMemArr
            }
          )
          val tempTilePxsPos = Vec.fill(cfg.bgTileSize2d.x)(
            cfg.bgTilePxsCoordT()
          )
          for (x <- 0 until cfg.bgTileSize2d.x) {
            switch (pipeIn.bgIdx) {
              for (tempBgIdx <- 0 until cfg.numBgs) {
                is (tempBgIdx) {
                  tempTilePxsPos(x).x := (
                    (
                      {
                        def tempTileWidth = cfg.bgTileSize2dPow.x
                        U(f"$tempTileWidth'd$x")
                      } - tempAttrs.scroll.x
                    )(tempTilePxsPos(x).x.bitsRange)
                  )
                  tempTilePxsPos(x).y := (
                    (
                      pipeIn.lineNum
                      - tempAttrs.scroll.y
                    ).resized
                  )

                  when (!tempInp.bgEntry(x).dispFlip.x) {
                    tempOutp.tilePxsCoord(x).x := tempTilePxsPos(x).x
                  } otherwise {
                    tempOutp.tilePxsCoord(x).x := (
                      cfg.bgTileSize2d.x - 1 - tempTilePxsPos(x).x
                    )
                  }
                  when (!tempInp.bgEntry(x).dispFlip.y) {
                    tempOutp.tilePxsCoord(x).y := tempTilePxsPos(x).y
                  } otherwise {
                    tempOutp.tilePxsCoord(x).y := (
                      cfg.bgTileSize2d.y - 1 - tempTilePxsPos(x).y
                    )
                  }
                }
              }
            }
          }
          for (arrIdx <- 0 until cfg.numBgMemsPerNonPalKind) {
            def arr = someTileMemArr(arrIdx)
            arr.io.rdEn := True
            arr.io.rdAddr := 0
            arr.io.rdAddr.allowOverride
          }
          def tempRdAddrWidth = (
            someTileMemArr(0).io.rdAddr.getWidth
          )
          def setRdAddr(
            someTempIdx: Int,
            someTempRdAddr: UInt,
            someVecIdx: Int,
          ): Unit = {
            someTempRdAddr := (
              Mux[UInt](
                !tempAttrs.fbAttrs.doIt,
                Cat(
                  tempInp.bgEntry(someVecIdx).tileIdx,
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
                //      log2Up(cfg.bgSize2dInPxs.x) - 1
                //      downto cfg.bgTileSize2dPow.x
                //    )
                //  ),
                //).asUInt,
                tempInp.stage5.fbRdAddrFinalPlus(someVecIdx),
              ).resized
            )
            someTileMemArr(
              someTempIdx
            ).io.rdAddr := (
              someTempRdAddr.resized
            )
          }
          def sameAsIdx = (
            tempInp.pxPosXGridIdxFindFirstSameAsIdx
          )
          def diffIdx = (
            tempInp.pxPosXGridIdxFindFirstDiffIdx
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
              cfg.bgTileSize2d.x - 1
            ),
          )
          tempOutp.tileIdxFront := (
            tempInp.bgEntry(
              0
            ).tileIdx//(0)
          )
          tempOutp.tileIdxBack := (
            tempInp.bgEntry(
              cfg.bgTileSize2d.x - 1
            ).tileIdx//(0)
          )
        }
      }
      // END: Stage 6

      // BEGIN: Stage 7
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 7
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (kind <- 0 until cfg.totalNumBgKinds) {
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
          for (x <- 0 until cfg.bgTileSize2d.x) {
            tempOutp.tileSlice(x) := tempOutp.tileSlice(x).getZero
            def setBgTile(
              tempPxPosIdx: Int,
            ): Unit = {
              def tempMemArr = (
                if (kind == 0) {
                  bgTileMemArr
                } else {
                  colorMathTileMemArr
                }
              )
              tempOutp.tileSlice(x).assignFromBits(
                tempMemArr(
                  tempPxPosIdx
                ).io.rdData
              )
            }
            switch (Cat(
              tempInp.pxPosXGridIdxFindFirstSameAsFound,
              tempInp.pxPosXGridIdxFindFirstDiffFound,
            )) {

              is (M"-0") {
                // At least one of them will be found, so
                // this indicates `SameAsFound`
                setBgTile(
                  tempPxPosIdx=0,
                )
              }
              is (M"01") {
                setBgTile(
                  tempPxPosIdx=0,
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
                    setBgTile(
                      tempPxPosIdx=0,
                    )
                  } otherwise {
                    setBgTile(
                      tempPxPosIdx=1,
                    )
                  }
                } otherwise {
                  // this indicates `sameAsIdx` < `diffIdx`
                  when (x < diffIdx) {
                    setBgTile(
                      tempPxPosIdx=0,
                    )
                  } otherwise {
                    setBgTile(
                      tempPxPosIdx=1,
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
      // END: Stage 7
      // BEGIN: Stage 8
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 8
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (kind <- 0 until cfg.totalNumBgKinds) {
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
          for (x <- 0 to cfg.bgTileSize2d.x - 1) {
            tempOutp.palEntryMemIdx(x) := tempInp.tileSlice(x).getPx(
              tempInp.tilePxsCoord(x).x
            )
          }
        }
      }
      // END: Stage 8
      // BEGIN: Stage 9
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 9
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (kind <- 0 until cfg.totalNumBgKinds) {
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

          for (x <- 0 to cfg.bgTileSize2d.x - 1) {
            tempOutp.palEntryNzMemIdx(x) := (
              tempInp.palEntryMemIdx(x) =/= 0
            )
          }
        }
      }
      // END: Stage 9
      // BEGIN: Stage 10
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 10
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (kind <- 0 until cfg.totalNumBgKinds) {
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

          for (x <- 0 to cfg.bgTileSize2d.x - 1) {
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
      // END: Stage 10
      // BEGIN: Stage 11
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 11
        val (pipeIn, pipeOut) = initTempWrBgPipeOut(idx=idx)
        for (kind <- 0 until cfg.totalNumBgKinds) {
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

          for (x <- 0 to cfg.bgTileSize2d.x - 1) {
            def myPalEntryMemArr = (
              if (kind == 0) {
                bgPalEntryMemArr
              } else {
                colorMathPalEntryMemArr
              }
            )
            tempOutp.palEntry(x).assignFromBits(
              myPalEntryMemArr(x).io.rdData
            )
          }
        }
      }
      // END: Stage 11
      // BEGIN: Stage 12
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrBgPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 12
        val (tempInp, tempOutp) = initTempWrBgPipeOut(idx=idx)
        val bgIdx = tempInp.bgIdx
        val rPastLineMemEntry = KeepAttribute(
          Vec.fill(cfg.bgTileSize2d.x)(
            Reg(BgSubLineMemEntry())
          )
        )
        rPastLineMemEntry.setName(f"rPastWrBgLineMemEntry")
        for (x <- 0 to cfg.bgTileSize2d.x - 1) {
          def tempLineMemEntry = tempOutp.postStage0.subLineMemEntry(x)
          rPastLineMemEntry(x).init(rPastLineMemEntry(x).getZero)
          //--------
          // BEGIN: fix this later
          when (cWrBgArr(idx).up.isFiring) {
            rPastLineMemEntry(x) := tempLineMemEntry
          }
          def setTempLineMemEntry(): Unit = {
            // Starting rendering a new pixel or overwrite the existing
            // pixel
            tempLineMemEntry.col.rgb := (
              tempInp.postStage0.palEntry(x).col.rgb
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
                tempInp.colorMath.palEntry(x).col.rgb
              )
              tempLineMemEntry.colorMathCol.a := (
                tempInp.colorMath.palEntryNzMemIdx(x)
              )
            }
          }
          tempLineMemEntry := rPastLineMemEntry(x)
          when (cWrBgArr(idx).up.isValid) {
            when (
              bgIdx === (1 << bgIdx.getWidth) - 1
            ) {
              setTempLineMemEntry()
            } otherwise {
              when (
                tempInp.postStage0.palEntryNzMemIdx(x)
              ) {
                setTempLineMemEntry()
              } 
              .otherwise {
                tempLineMemEntry := rPastLineMemEntry(x)
              }
            }
          }
          // END: fix this later
          //--------
        
        }
      }
      // END: Stage 12

      def tempArrIdx(kdx: Int) = (
        wrBgPipeLast.postStage0.subLineMemEntry(kdx)
        .getSubLineMemTempArrIdx()
      )
      val rValidPipe1 = Vec.fill(cfg.combineBgSubLineMemArrSize)(
        Vec.fill(wrBgPipeLineMemArrIdx.size)(
          Reg(Bool()) init(False)
        )
      )
        .setName("wrBgPipeLast_rValidPipe1")
      val rAddrPipe1 = Vec.fill(cfg.combineBgSubLineMemArrSize)(
        Vec.fill(wrBgPipeLineMemArrIdx.size)(
          Reg(cloneOf(tempArrIdx(0))) init(tempArrIdx(0).getZero)
        )
      )
        .setName("wrBgPipeLast_rAddrPipe1")
      val rDataPipe1 = Vec.fill(
        wrBgPipeLineMemArrIdx.size
      )(
        Reg(cloneOf(wrBgPipeLast.postStage0.subLineMemEntry))
      )
        .setName("wrBgPipeLast_rDataPipe1")
      for (jdx <- 0 until wrBgPipeLineMemArrIdx.size) {
        for (kdx <- 0 until cfg.combineBgSubLineMemArrSize) {
          def tempMem = combineBgSubLineMemA2d(jdx)(kdx)
          tempMem.io.wrPulse.valid := (
            rValidPipe1(kdx)(jdx)
          )
          tempMem.io.wrPulse.addr := (
            rAddrPipe1(kdx)(jdx)
          )
          for (zdx <- 0 until cfg.combineBgSubLineMemVecElemSize) {
            val tempIndex = (
              kdx * cfg.combineBgSubLineMemArrSize + zdx
            )
            tempMem.io.wrPulse.data(zdx) := (
              rDataPipe1(jdx)(
                tempIndex
              )
            )
          }
        }
      }
      when (RegNext(nWrBgPipeLast.isFiring) init(False)) {
        for (jdx <- 0 until wrBgPipeLineMemArrIdx.size) {
          for (
            kdx <- 0 until cfg.combineBgSubLineMemArrSize
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
      } otherwise { // when (!nWrBgPipeLast.isFiring)
        for (jdx <- 0 until wrBgPipeLineMemArrIdx.size) {
          for (kdx <- 0 until cfg.combineBgSubLineMemArrSize) {
            rValidPipe1(kdx)(jdx) := False
            rAddrPipe1(kdx)(jdx) := 0x0
          }
        }
      }
      // END: post stage 0

      //--------
      //--------
    }

    def writeObjLineMemEntries(
      //someWrLineMemArrIdx: Int,
      kind: Int,
    ): Unit = {
      // Handle sprites
      // BEGIN: stage 0
      //pipeStageMainFunc=(
      //  stageData: DualPipeStageData[Flow[WrObjPipePayload]],
      //  idx: Int,
      //) => 
      {
        def idx = 0
        val (pipeIn, pipeOut): (WrObjPipePayload, WrObjPipePayload) = (
          if (kind == 0) {
            initTempWrObjPipeOut(idx=idx)
          } else { // if (kind == 1)
            initTempWrObjAffinePipeOut(idx=idx)
          }
        )
        pipeOut.stage0.justCopy := pipeIn.stage0.justCopy
      }
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
        val tempInp = pipeIn
        val tempOutp = pipeOut

        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = (
          tempObjTileWidthPow(kind != 0)
        )
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
      // END: Stage 1

      // BEGIN: Stage 2
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
        tempOutp.objAttrs := (
          RegNext(tempOutp.objAttrs) init(tempOutp.objAttrs.getZero)
        )
        when (cWrObjArr(idx).up.isFiring) {
          tempOutp.objAttrs.assignFromBits(
            objAttrsMemArr(kind).io.rdData
          )
        }
      }
      // END: Stage 2

      // BEGIN: Stage 3
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
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = (
          tempObjTileWidthPow(kind != 0)
        )
        val tempObjXStart = (
          if (kind == 0) {
            tempInp.objXStart()
          } else {
            tempInp.affineObjXStart()
          }
        )
          .setName(f"wrObjPipe2_tempObjXStart_$kind")
        for (x <- 0 until tempObjTileWidth1(kind != 0)) {
          tempOutp.myIdxPxPosX(x) := (
            tempInp.objAttrs.pos.x.asUInt
            + x
            + (
              tempObjXStart
            )
          ).asSInt
        }

        for (x <- 0 until myTempObjTileWidth) {
          val tileX = (
            (
              tempObjXStart
            ) + x
          )
            .setName(f"wrObjPipe2_tileX_$kind" + f"_$x")
          tempOutp.pxPos(x).x := (
            tempInp.objAttrs.pos.x.asUInt
            + tileX
          ).asSInt
          tempOutp.pxPos(x).y(
            tempInp.lineNum.bitsRange
          ) := (
            tempInp.lineNum.asSInt
          )
          tempOutp.pxPos(x).y(
            tempOutp.pxPos(x).y.high
            downto tempInp.lineNum.getWidth
          ) := 0x0
        }
      }
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
            val tileX = (tempInp.affineObjXStart() + x)
              .setName(f"wrObjPipe3_tileX_$kind" + f"_$x")
            def myMultAX = tempOutp.stage4.multAX(x)
            def myMultBY = tempOutp.stage4.multBY(x)
            def myMultCX = tempOutp.stage4.multCX(x)
            def myMultDY = tempOutp.stage4.multDY(x)

            myMultAX := (
              (
                Cat(False, tileX).asSInt.resized
                - {
                  def tempTileWidth = (
                    cfg.objAffineTileSize2d.x
                  )
                  def tempWidth = tileX.getWidth + 1 + 1
                  S(f"$tempWidth'd$tempTileWidth")
                }
              ) * tempInp.objAttrs.affine.matA
            ).resized
            myMultBY := (
              (
                tempInp.lineNum.asSInt.resized
                - (
                  tempInp.objAttrs.pos.y
                ) - (
                  cfg.objAffineTileSize2d.y
                )
              ) * tempInp.objAttrs.affine.matB
            ).resized
            myMultCX := (
              (
                Cat(False, tileX).asSInt.resized
                - {
                  def tempTileWidth = (
                    cfg.objAffineTileSize2d.x
                  )
                  def tempWidth = tileX.getWidth + 1 + 1
                  S(f"$tempWidth'd$tempTileWidth")
                }
              ) * tempInp.objAttrs.affine.matC
            ).resized
            myMultDY := (
              (
                tempInp.pxPos(x).y
                - (
                  tempInp.objAttrs.pos.y
                ) - (
                  cfg.objAffineTileSize2d.y
                )
              ) * tempInp.objAttrs.affine.matD
            ).resized
          }
        }
      }
      // END: Stage 4

      // BEGIN: Stage 5
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
      // END: Stage 5

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
                  //(cfg.objTileSize2d.x / 2)
                  (cfg.objAffineTileSize2d.x / 2)
                  //cfg.objAffineTileSize2d.x
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
                  //(cfg.objTileSize2d.y / 2)
                  (cfg.objAffineTileSize2d.y / 2)
                  //cfg.objAffineTileSize2d.y
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

        if (kind == 0) {
          objTileMemArr(kind).io.rdEn := True
          objTileMemArr(kind).io.rdAddr := (
            RegNext(objTileMemArr(kind).io.rdAddr)
            init(0x0)
          )
          when (cWrObjArr(idx).up.isFiring) {
            objTileMemArr(kind).io.rdAddr := (
              if (
                (kind == 0 && cfg.objTileWidthRshift > 0)
              ) {
                Cat(
                  tempInp.objAttrs.tileIdx,
                  tempOutp.tilePxsCoord(0).y,
                  (
                    tempInp.objXStart()(
                      cfg.objTileSize2dPow.x - 1
                      downto cfg.objSliceTileWidthPow
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
            objAffineTileMemArr(x).io.rdAddr := (
              RegNext(objAffineTileMemArr(x).io.rdAddr)
              init(0x0)
            )
            when (cWrObjAffineArr(idx).up.isFiring) {
              objAffineTileMemArr(x).io.rdEn := True
              val fxTileX = (
                (
                  tempInp.stage6.fxTilePxsCoord(x).x
                ) >> (
                  Gpu2dAffine.fracWidth
                )
              )
              objAffineTileMemArr(x).io.rdAddr := (
                Cat(
                  tempInp.objAttrs.tileIdx,
                  tempOutp.tilePxsCoord(x).y,
                  fxTileX(
                    cfg.objAffineTileSize2dPow.x - 1 downto 0
                  ),
                ).asUInt
              )
            }
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
              (
                tempInp.lineNum.asSInt.resized
                - tempInp.objAttrs.pos.y
              ).asUInt
            )(
              tempOutp.tilePxsCoord(x).y.bitsRange
            ))
              .setName(s"wrObjPipe_6_tempY_${x}")
            when (!tempInp.objAttrs.dispFlip.x) {
              tempOutp.tilePxsCoord(x).x := tempX.resized
            } otherwise {
              tempOutp.tilePxsCoord(x).x := (
                cfg.objTileSize2d.x - 1 - tempX
              ).resized
            }
            when (!tempInp.objAttrs.dispFlip.y) {
              tempOutp.tilePxsCoord(x).y := tempY.resized
            } otherwise {
              tempOutp.tilePxsCoord(x).y := (
                cfg.objTileSize2d.y - 1 - tempY
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
            val tempX = (
              fxTileX
            )
              .setName(f"wrObjPipe5_tempX_$x")
            tileX := tempX.asUInt(tileX.bitsRange)
            
            tempOutp.stage7.oorTilePxsCoord(x).x := (
              (
                (
                  tempInp.objAttrs.size2d.x(
                    tempInp.objAttrs.size2d.x.high
                    downto tempInp.objAttrs.size2d.x.high - 1
                  ) =/= U"00"
                ) && (
                  tempX < 0
                  || tempX >= cfg.objAffineTileSize2d.x
                )
              ) || (
                (
                  tempInp.objAttrs.size2d.x(
                    tempInp.objAttrs.size2d.x.high
                    downto tempInp.objAttrs.size2d.x.high - 1
                  ) === U"00"
                ) && !(
                  (
                    (tempX << 1) - cfg.objAffineTileSize2d.x 
                    < tempInp.objAttrs.size2d.x.asSInt
                  ) && (
                    (tempX << 1) - cfg.objAffineTileSize2d.x 
                    >= -tempInp.objAttrs.size2d.x.asSInt
                  )
                )
              )
            )

            def tileY = tempOutp.tilePxsCoord(x).y
            val fxTileY = (
              (
                tempInp.stage6.fxTilePxsCoord(x).y
              ) >> (
                Gpu2dAffine.fracWidth
              )
            )
              .setName(f"dbgTestWrObjPipe5_fxTileY_$x")
            val tempY = (
              fxTileY
            )
              .setName(f"wrObjPipe5_tempY_$x")
            tileY := tempY.asUInt(tileY.bitsRange)
            
            tempOutp.stage7.oorTilePxsCoord(x).y := (
              (
                (
                  tempInp.objAttrs.size2d.y(
                    tempInp.objAttrs.size2d.y.high
                    downto tempInp.objAttrs.size2d.y.high - 1
                  ) =/= U"00"
                ) && (
                  tempY < 0
                  || tempY >= cfg.objAffineTileSize2d.y
                )
              ) || (
                (
                  tempInp.objAttrs.size2d.y(
                    tempInp.objAttrs.size2d.y.high
                    downto tempInp.objAttrs.size2d.y.high - 1
                  ) === U"00"
                ) && !(
                  (
                    (tempY << 1) - cfg.objAffineTileSize2d.y 
                    < tempInp.objAttrs.size2d.y.asSInt
                  ) && (
                    (tempY << 1) - cfg.objAffineTileSize2d.y 
                    >= -tempInp.objAttrs.size2d.y.asSInt
                  )
                )
              )
            )
            tempOutp.stage7.affineDoIt(x) := (
              if (kind == 0) {
                True
              } else {
                tempInp.objAttrs.affine.doIt
              }
            )
          }
        }

        tempOutp.objPosYShift := (
          tempInp.objAttrs.pos.y 
          + myTempObjTileHeight
        )
      }
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
            tempOutp.tileSlice.assignFromBits(
              objTileMemArr(kind).io.rdData
            )
          }
        } else {
          for (kdx <- 0 until myTempObjTileWidth) {
            tempOutp.tilePx(kdx).assignFromBits(
              objAffineTileMemArr(kdx).io.rdData
            )
          }
        }

        val dbgPxPosXGridIdxFindFirstSameAs: (Bool, UInt) = (
          tempOutp.pxPosXGridIdx.sFindFirst(
            _(0) === tempInp.stage0.calcGridIdxLsb(kind)
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
            _(0) =/= tempInp.stage0.calcGridIdxLsb(kind)
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
            tempOutp.pxPosXGridIdx(x)(0)
          )
          tempOutp.pxPosRangeCheckGePipe1(x).x := (
            tempInp.pxPos(x).x >= 0
          )
          tempOutp.pxPosRangeCheckGePipe1(x).y := (
            tempInp.pxPos(x).y >= tempInp.objAttrs.pos.y
          )

          tempOutp.pxPosRangeCheckLtPipe1(x).x := (
            tempInp.pxPos(x).x < cfg.intnlFbSize2d.x
          )
          tempOutp.pxPosRangeCheckLtPipe1(x).y := (
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
          if (kind == 1) {
            tempOutp.stage8.affineDoIt(x) := (
              tempInp.stage7.affineDoIt(x)
              && !tempInp.stage7.oorTilePxsCoord(x).x
              && !tempInp.stage7.oorTilePxsCoord(x).y
            )
          }
        }
      }
      // END: Stage 8

      // BEGIN: Stage 9
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
      // END: Stage 9

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
          tempOutp.pxPosXGridIdxMatches(x) := (
            tempInp.pxPosXGridIdxLsb(x)
            === (
              tempInp.stage0.calcGridIdxLsb(kind)
            )
          )
          tempOutp.palEntryNzMemIdx(x) := (
            tempInp.palEntryMemIdx(x) =/= 0
          )

          tempOutp.pxPosRangeCheck(x).x := (
            tempInp.pxPosRangeCheckGePipe1(x).x
            && tempInp.pxPosRangeCheckLtPipe1(x).x
          )
          tempOutp.pxPosRangeCheck(x).y := (
            tempInp.pxPosRangeCheckGePipe1(x).y
            && tempInp.pxPosRangeCheckLtPipe1(x).y
          )
        }
      }
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

        for (x <- 0 until myTempObjTileWidth) {
          tempOutp.palEntry(x) := (
            RegNext(tempOutp.palEntry(x))
            init(tempOutp.palEntry(x).getZero)
          )
          when (cWrObjArr(idx).up.isFiring) {
            tempOutp.palEntry(x).assignFromBits(
              objPalEntryMemA2d(kind)(x).io.rdData.asBits
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
          )
        dbgTestWrObjPipe9_tempObjArrIdx := tempObjArrIdx
      }


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

        tempOutp.stage13.pxPosYLsb := (
          tempInp.pxPos(0).y(0)
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
            val myIdx = UInt(myTempObjTileWidthPow1 bits)
              .setName(
                f"wrObjPipe10_myIdx_$kind" + f"_$x" + f"_$jdx"
              )
            if (kind == 0) {
              val myIdxFull = cloneOf(tempInp.myIdxPxPosX(x))
                .setName(
                  f"wrObjPipe10_myIdxFull_$kind" + f"_$x" + f"_$jdx"
                )
              myIdxFull := tempInp.myIdxPxPosX(x)
              myIdx := myIdxFull.asUInt(myIdx.bitsRange)
            } else { // if (kind == 1)
              val myIdxFull = cloneOf(tempInp.myIdxPxPosX(x))
                .setName(
                  f"wrObjPipe10_myIdxFull_$kind" + f"_$x" + f"_$jdx"
                )
              myIdxFull := tempInp.myIdxPxPosX(x)
              myIdx := myIdxFull.asUInt(myIdx.bitsRange)
            }
            myIdxVec(jdx) := myIdx
          }
        }

      }

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
        }
        def stage10MyIdxV2d = tempInp.stage13.myIdxV2d
        val tempStage10MyIdxVec = Vec.fill(myTempObjTileWidth1)(
          UInt(myTempObjTileWidthPow1 bits)
        )
          .setName(f"wrObjPipe11_tempStage10MyIdxVec_$kind")
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
          }
        )
          .setName(f"wrObjPipe11_firstMyIdxZero_$kind")
        for (x <- 0 until myTempObjTileWidth1) {
          def tempX = x
          def tempMyIdxVec = stage10MyIdxV2d(tempX)
          def tempMyIdx = tempMyIdxVec(x)
          tempStage10MyIdxVec(x) := tempMyIdx
          for (
            jdx <- 0 until tempOutp.stage14.myIdxV2d(x).size
          ) {
            def myIdx = tempOutp.stage14.myIdxV2d(x)(jdx)
            myIdx := (
              firstMyIdxZero._2 + x
            )
          }
        }
        switch (
          wrObjPipeLineMemArrIdx(1)
        ) {
          for (
            jdx <- 0 until (1 << wrObjPipeLineMemArrIdx(1).getWidth)
          ) {
            is (jdx) {
              //--------
              val tempRdAddr = (
                if (kind == 0) {
                  tempOutp.subLineMemEntryExt.memAddr(PipeMemRmw.modWrIdx)
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
              when (tempInp.pxPosXGridIdxFindFirstSameAsFound) {
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
                    cfg.getObjSubLineMemArrIdx(tempX)
                  } else {
                    cfg.getObjAffineSubLineMemArrIdx(tempX)
                  }
                )
              } otherwise {
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
                tempRdAddr := (
                  if (kind == 0) {
                    cfg.getObjSubLineMemArrIdx(tempX)
                  } else {
                    cfg.getObjAffineSubLineMemArrIdx(tempX)
                  }
                )//.resized
              }
              //--------
            }
          }
        }

      }

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
        tempOutp.subLineMemEntryExt := (
          tempInp.subLineMemEntryExt
        )
        tempOutp.subLineMemEntryExt.allowOverride
        def myTempObjTileWidth1 = tempObjTileWidth1(kind != 0)
        //--------
        switch (
          wrObjPipeLineMemArrIdx(2)
        ) {
          for (
            jdx <- 0 until (1 << wrObjPipeLineMemArrIdx(2).getWidth)
          ) {
            is (jdx) {
              //--------
              val tempRdData: Bits = KeepAttribute(
                if (kind == 0) {
                  //wrObjSubLineMemArr(jdx)
                  tempInp.subLineMemEntryExt.rdMemWord(
                    PipeMemRmw.modWrIdx
                  ).asBits
                } else {
                  wrObjAffineSubLineMemArr(jdx).io.rdData.asBits
                }
              )
                .setName(
                  if (kind == 0) (
                    s"wrObjPipe_13_tempRdData_${jdx}"
                  ) else (
                    s"wrObAffinejPipe_13_tempRdData_${jdx}"
                  )
                )
              tempOutp.stage15.rdSubLineMemEntry.assignFromBits(
                //temp.io.rdData
                tempRdData.asBits
              )
              //--------
            }
          }
        }
        //if (kind != 0) {
          for (
            x <- 0 until myTempObjTileWidth1
          ) {
            val tempObjXStart = (
              if (kind == 0) {
                tempInp.objXStart()
              } else {
                tempInp.affineObjXStart()(
                  tempInp.affineObjXStart().high - 1 downto 0
                )
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
              x 
              <= tempObjXStart
                + (
                  if (kind == 0) {
                    cfg.objSliceTileWidth
                  } else {
                    cfg.objAffineSliceTileWidth
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
            tempOutp.stage15.inMainVec(x) := (
              tempCmpGe && tempCmpLe
            )
          }
        //}
      }
      // END: Stage 15

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
              // `myIdx` should be used here since it's an index into
              // `objSubLineMemArr(jdx)`
              tempInp.stage13.myIdxV2d(x)(x)(
                //cfg.objAffineSliceTileWidthPow - 1 downto 0
                myTempObjTileWidthPow - 1 downto 0
              )
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
                  cfg.objAffineTileSize2dPow.x - 1 downto 0
                  //cfg.objAffineTileWidthRshift - 1 downto 0
                )
              )
            }
          )
        }
        for (x <- 0 until myTempObjTileWidth) {
          tempOutp.stage16.tempRdLineMemEntry(x) := (
            tempInp.rdSubLineMemEntry(
              // `myIdx` should be used here since it's an index into
              // `objSubLineMemArr(jdx)`
              tempInp.stage13.myIdxV2d(x)(x)(
                //cfg.objAffineSliceTileWidthPow - 1 downto 0
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
                  cfg.objAffineTileSize2dPow.x - 1 downto 0
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
        def myMainFunc(
          x: Int,
        ): Unit = {
          //--------
          def inMain = (
            //tempInp.stage12.inMainVec(x)
            if (kind == 0) {
              tempOutp.stage16.inMainVec(x)
            } else {
              tempOutp.stage16.inMainVec(x)
            }
          )
          //--------
          def sliceX = (
            if (kind == 0) (
              x
            ) else (
              x
            )
          )
          //--------
          val tileX = (
            if (kind == 0) {
              def tempWidth = cfg.objTileSize2dPow.x
              //U(f"$tempWidth'd$x")
              def tempX = (
                x & ((1 << tempWidth) - 1)
              )
              U(f"$tempWidth'd$tempX")
            } else {
              def tempWidth = (
                cfg.objAffineTileSize2dPow.x
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
              tileX(cfg.objSliceTileWidthPow - 1 downto 0)
            } else {
              tileX(cfg.objAffineSliceTileWidthPow - 1 downto 0)
            }
          )
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
            nonRotatedOutpExt.overwriteLineMemEntry(x)
          )
          // END: later
          //--------
          //--------
          // BEGIN: later
          def nonRotatedWrLineMemEntry = (
            nonRotatedOutpExt
            .wrLineMemEntry(x)
          )
          // END: later
          //--------
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
                  tempInp.pxPos(sliceX).x.asUInt(
                    someWrLineMemEntry.addr.bitsRange
                  )
                  //default -> False
                )
              }
              someWrLineMemEntry.col.rgb := (
                tempInp.palEntry(sliceX).col.rgb
              )
              someWrLineMemEntry.col.a := (
                tempInp.palEntryNzMemIdx(sliceX)
              )
              someWrLineMemEntry.prio := (
                tempInp.objAttrs.prio
              )
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
              if (kind == 0) (
                True
              ) else (
                tempInp.stage8.affineDoIt(sliceX)
              )
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
            if (cfg.fancyObjPrio) {
              if (cfg.numBgsPow == log2Up(2)) {
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
              } else if (cfg.numBgsPow == log2Up(4)) {
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
            } else { // if (!cfg.fancyObjPrio)
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
        for (x <- 0 until tempObjTileWidth(kind != 0)) {
          myMainFunc(x=x)
          val myWrLineMemEntry = (
            nonRotatedOutpExt.wrLineMemEntry(
              myIdxV2d(x)(
                //x * 2
                x
              )(
                myTempObjTileWidthPow - 1 downto 0
              )
            )
          )
          val myOverwriteLineMemEntry = (
            nonRotatedOutpExt.overwriteLineMemEntry(
              myIdxV2d(x)(
                //x * 2
                x
              )(
                myTempObjTileWidthPow - 1 downto 0
              )
            )
          )

          outpExt.wrLineMemEntry(x) := (
            myWrLineMemEntry
          )
          tempOutp.subLineMemEntryExt.modMemWord(x) := (
            myWrLineMemEntry
          )
          when (tempInp.subLineMemEntryExt.hazardId.msb) {
            outpExt.overwriteLineMemEntry(x) := (
              myOverwriteLineMemEntry
            )
            //when (outpExt.overwriteLineMemEntry(x)) {
            //  tempOutp.subLineMemEntryExt.modMemWord(x) := (
            //    outpExt.wrLineMemEntry(x)
            //  )
            //} otherwise {
            //  tempOutp.subLineMemEntryExt.modMemWord(x) := (
            //    tempInp.subLineMemEntryExt.rdMemWord(x)
            //  )
            //}
          } otherwise {
            outpExt.overwriteLineMemEntry(x) := False
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
        def myTempObjTileWidth = tempObjTileWidth(kind != 0)
        def myTempObjTileWidthPow = tempObjTileWidthPow(kind != 0)
      }
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
        //  cfg.objSubLineMemArrSizePow bits
        //)
        //  .setName("wrObjPipeLast_tempArrIdx")
        //tempObjSubLineMemArrIdx := cfg.getObjSubLineMemTempArrIdx(
        //  pxPosX=tempWrObjPipeLast.pxPos.x.asUInt
        //)
        //val tempAddr = UInt(
        //  //log2Up(cfg.oneLineMemSize) bits
        //  //log2Up(cfg.objSubLineMemSize) bits
        //  cfg.objSubLineMemSizePow bits
        //)
        //  .setName("wrObjPipeLast_tempAddr")

        //tempAddr := tempWrObjPipeLast.pxPos.x.asUInt(
        //  //log2Up(cfg.oneLineMemSize) - 1 downto 0
        //  //tempAddr.bitsRange
        //  //cfg.objSubLineMemSizePow
        //  //log2Up(cfg.objSubLineMemSizePow
        //)
        //tempAddr := cfg.getObjSubLineMemTempAddr(
        //  pxPosX=tempWrObjPipeLast.pxPos.x.asUInt
        //)
        // END: come back to this later
        //val tempSubLineMemEntry = Vec.fill(cfg.objTileSize2d.x)(
        //  ObjSubLineMemEntry()
        //)
        //for (x <- 0 to cfg.objTileSize2d.x - 1) {
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
                    //cfg.objTileSize2dPow.x - 1
                    cfg.objSliceTileWidthPow - 1
                  } else {
                    cfg.objAffineSliceTileWidthPow - 1
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
                  //&& tempWrObjPipeLast.wrLineMemEntry(jdx).col.a
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
              //!tempWrObjPipeLast.stage0.rawObjAttrsMemIdx()(
              //  //cfg.objAffineTileWidthRshift
              //  0
              //) && (
                tempFindFirstFound
              //)
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
    ): Unit = {
      {
        def idx = 0
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
      }
      {
        def idx = 3
        val (pipeIn, pipeOut) = initCombinePostJoinPipeOut(idx)
        def tempInp = pipeIn
        def tempOutp = pipeOut
        val rTempCnt = Reg(UInt(tempInp.cnt.getWidth bits)) init(0x0)
        def myLineMemIdx = (
          tempInp.lineMemIdx
        )
        def bgSubLineMemArrIdx = cfg.getBgSubLineMemArrIdx(
          addr=(
            myLineMemIdx
          )
        )
        val dbgTestCombinePipe2_bgSubLineMemArrIdx = UInt(
          bgSubLineMemArrIdx.getWidth bits
        )
          .setName("dbgTestCombinePipe2_bgSubLineMemArrIdx")
        dbgTestCombinePipe2_bgSubLineMemArrIdx := (
          bgSubLineMemArrIdx
        )

        def bgSubLineMemArrElemIdx = cfg.getBgSubLineMemArrElemIdx(
          addr=(
            myLineMemIdx
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
          cfg.getObjSubLineMemArrIdx(
            addr=(
              myLineMemIdx
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
          cfg.getObjSubLineMemArrElemIdx(
            addr=(
              myLineMemIdx
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
          cfg.getObjAffineSubLineMemArrElemIdx(
            addr=(
              myLineMemIdx
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
          tempOutp.stage3 := tempOutp.stage3.getZero
        } otherwise {
          switch (combineLineMemArrIdx) {
            for (jdx <- 0 until 1 << combineLineMemArrIdx.getWidth) {
              is (jdx) {
                //--------
                def tempRdBg = (
                  tempInp.stage2.rdBg
                )
                def tempRdObj = (
                  tempInp.stage2.rdObj
                )
                tempOutp.stage3.ext.bgRdSubLineMemEntry := (
                  tempRdBg(bgSubLineMemArrElemIdx)
                )
                tempOutp.stage3.ext.objRdSubLineMemEntry := (
                  tempRdObj(
                    objSubLineMemArrElemIdx
                  )
                )
                if (!noAffineObjs) {
                  def tempRdObjAffine = (
                    tempInp.stage2.rdObjAffine
                  )
                  tempOutp.stage3.ext.objAffineRdSubLineMemEntry := (
                    tempRdObjAffine(
                      objAffineSubLineMemArrElemIdx
                    )
                  )
                }
              }
            }
          }
        }
      }
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
      {
        def idx = 5
        val (pipeIn, pipeOut) = initCombinePostJoinPipeOut(idx)
        def tempInp = pipeIn
        def tempOutp = pipeOut

        def inpExt = (
          tempInp.stage3.ext
        )
        // BEGIN: Debug comment this out
        when (clockDomain.isResetActive) {
          tempOutp.stage5 := tempOutp.stage5.getZero
        } otherwise {
          when (
            cCombineArr(idx).up.isValid
          ) {
            tempOutp.objHiPrio := (
              // sprites take priority upon a tie, hence `<=`
              tempInp.objPickSubLineMemEntry.prio
              <= inpExt.bgRdSubLineMemEntry.prio
            )
          } otherwise {
            tempOutp.stage5 := RegNext(tempOutp.stage5)
          }
        }
      }
      {
        def idx = 6
        val (pipeIn, pipeOut) = initCombinePostJoinPipeOut(idx)
        def tempInp = pipeIn
        def tempOutp = pipeOut

        def inpExt = (
          tempInp.stage3.ext
        )

        when (clockDomain.isResetActive) {
          tempOutp.stage6 := tempOutp.stage6.getZero
        } otherwise {
          if (!noColorMath) {
            tempOutp.colorMathCol := (
              inpExt.bgRdSubLineMemEntry.colorMathCol
            )
            tempOutp.colorMathCol.allowOverride
          }
          switch (Cat(
            inpExt.bgRdSubLineMemEntry.col.a,
            tempInp.objPickSubLineMemEntry.col.a,
            tempInp.objHiPrio
          )) {
            is (B"111") {
              tempOutp.col := tempInp.objPickSubLineMemEntry.col
              if (!noColorMath) {
                tempOutp.colorMathInfo := (
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
              tempOutp.col := tempInp.objPickSubLineMemEntry.col
              if (!noColorMath) {
                tempOutp.colorMathInfo := (
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
        }
      }
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
      {
        def idx = 8
        val (pipeIn, pipeOut) = initCombinePostJoinPipeOut(idx)
        def tempInp = pipeIn
        def tempOutp = pipeOut

        when (clockDomain.isResetActive) {
          tempOutp.stage8 := tempOutp.stage8.getZero
        } otherwise {
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
            rWidth=cfg.rgbConfig.rWidth + extraWidth,
            gWidth=cfg.rgbConfig.gWidth + extraWidth,
            bWidth=cfg.rgbConfig.bWidth + extraWidth,
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
                  tempCol.r(cfg.rgbConfig.rWidth downto 0) := (
                    inpWrLineMemEntry.col.rgb.r.resized
                    + Cat(False, tempCmathCol.rgb.r).asUInt
                  )
                  tempCol.r(
                    tempCol.r.high downto cfg.rgbConfig.rWidth + 1
                  ) := 0
                  tempCol.g(cfg.rgbConfig.gWidth downto 0) := (
                    inpWrLineMemEntry.col.rgb.g.resized
                    + Cat(False, tempCmathCol.rgb.g).asUInt
                  )
                  tempCol.g(
                    tempCol.g.high downto cfg.rgbConfig.gWidth + 1
                  ) := 0
                  tempCol.b(cfg.rgbConfig.bWidth downto 0) := (
                    inpWrLineMemEntry.col.rgb.b.resized
                    + Cat(False, tempCmathCol.rgb.b).asUInt
                  )
                  tempCol.b(
                    tempCol.b.high downto cfg.rgbConfig.bWidth + 1
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
                  tempCol.r(cfg.rgbConfig.rWidth - 1 downto 0) := (
                    (
                      inpWrLineMemEntry.col.rgb.r.resized
                      + Cat(False, tempCmathCol.rgb.r).asUInt
                    ) >> 1
                  ).resized
                  tempCol.r(
                    tempCol.r.high downto cfg.rgbConfig.rWidth
                  ) := 0
                  tempCol.g(cfg.rgbConfig.gWidth - 1 downto 0) := (
                    (
                      inpWrLineMemEntry.col.rgb.g.resized
                      + Cat(False, tempCmathCol.rgb.g).asUInt
                    ) >> 1
                  ).resized
                  tempCol.g(
                    tempCol.g.high downto cfg.rgbConfig.gWidth
                  ) := 0
                  tempCol.b(cfg.rgbConfig.bWidth - 1 downto 0) := (
                    (
                      inpWrLineMemEntry.col.rgb.b.resized
                      + Cat(False, tempCmathCol.rgb.b).asUInt
                    ) >> 1
                  ).resized
                  tempCol.b(
                    tempCol.b.high downto cfg.rgbConfig.bWidth
                  ) := 0
                }
              }

              when (
                tempCol.r.asSInt >= (1 << cfg.rgbConfig.rWidth) - 1
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
                tempCol.g.asSInt >= (1 << cfg.rgbConfig.gWidth) - 1
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
                tempCol.b.asSInt >= (1 << cfg.rgbConfig.bWidth) - 1
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
              outpWrLineMemEntry := inpWrLineMemEntry
            }
          } else { // if (noColorMath)
            outpWrLineMemEntry.col.rgb := inpWrLineMemEntry.col.rgb
          }
        }
      }

      when (
        nCombinePipeLast.isFiring
      ) {
        def tempObjArrIdx = cfg.getObjSubLineMemArrIdx(
          addr=(
            combinePipeLast.cnt
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
          //cfg.objTileSize2d.x
          cfg.objSliceTileWidth
        )(
          ObjSubLineMemEntry()
        ).getZero
        objWriter.addrVec(1) := tempObjArrIdx
        objWriter.dataVec(1) := tempObjLineMemEntry
        objWriter.enVec(1) := True
        // END: new code, with muxing for single `.write()` call
        //--------
        if (!noAffineObjs) {
          def tempObjAffineArrIdx = cfg.getObjAffineSubLineMemArrIdx(
            addr=(
              combinePipeLast.cnt
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
              cfg.objAffineSliceTileWidth
            )(
              ObjSubLineMemEntry()
            ).getZero
          )
          objAffineWriter.addrVec(1) := tempObjAffineArrIdx
          objAffineWriter.dataVec(1) := tempObjAffineLineMemEntry
          objAffineWriter.enVec(1) := True
        }
        //--------
        outp.col := combinePipeLast.combineWrLineMemEntry.col
        //--------
      } otherwise {
        objWriter.enVec(1) := False
        if (!noAffineObjs) {
          objAffineWriter.enVec(1) := False
        }
        outp.col := rPastOutp.col
      }
      when (
        pop.fire
      ) {
        rdPhysCalcPosEn := True
      } otherwise {
        rdPhysCalcPosEn := False
      }
    }
    //--------
    writeBgLineMemEntries()
    if (!noAffineObjs) {
      for (kind <- 0 until 2) {
        writeObjLineMemEntries(kind=kind)
      }
    } else {
      writeObjLineMemEntries(kind=0)
    }

    combineLineMemEntries()
    //--------
    Builder(linkArr.toSeq)
    //--------
  }
}
