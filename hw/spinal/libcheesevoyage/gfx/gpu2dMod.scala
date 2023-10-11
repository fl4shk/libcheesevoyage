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
import libcheesevoyage.general.DualPipeFuncMostArgs
import libcheesevoyage.general.GenericHandleDualPipe
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
                              // This is used to duplicate generated pixels 
                              // so that the generated video signal can 
                              // fill the screen.
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
  //combinedLineArrRamStyle: String="block",
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
  //def numLineMems = 2
  //def numLineMems = 1 << physFbSize2dScalePow.y
  //def numLineMems = 4
  //def numLineMems = 2
  def numLineMemsPerBgObjRenderer = 4
  //def numLineMems = numBgs
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
  def objPxsCoordT() = sCoordT(someSize2d=ElabVec2[Int](
    x=intnlFbSize2d.x + 1,
    y=intnlFbSize2d.y,
  ))
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
    //combinedLineArrRamStyle: String="block",
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
      numBgTilesPow=(
        //1024
        //2048 // (480 * 270) / (8 * 8) = 2025
        //1 <<
        log2Up(
          (intnlFbSize2d.x * intnlFbSize2d.y)
          / (bgTileSize2d.x * bgTileSize2d.y)
        )
      ),
      numObjTilesPow=(
        //log2Up(1024)
        numObjsPow
      ),
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
      //combinedLineArrRamStyle=combinedLineArrRamStyle,
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

  // indices into `Gpu2d.loc.palEntryMem`
  val colIdxV2d = (
    if (!isObj) {
      Vec.fill(params.bgTileSize2d.y)(
        Vec.fill(params.bgTileSize2d.x)(UInt(colIdxWidth bits))
      )
    } else { // if (isObj)
      Vec.fill(params.objTileSize2d.y)(
        Vec.fill(params.objTileSize2d.x)(UInt(colIdxWidth bits))
      )
    }
  )
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
  val tileIdx = UInt(params.bgTileMemIdxWidth bits)

  //// The priority for this tilemap entry
  //val prio = UInt(log2Up(params.numBgs) bits)

  //val dispFlip = Vec2(dataType=Bool())
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
  val tileIdx = UInt(params.objTileMemIdxWidth bits)
  //val pos = params.coordT(fbSize2d=params.bgSize2dInPxs)

  //// position within the tilemap, in pixels
  // position on screen, in pixels
  //val pos = params.bgPxsCoordT()
  val pos = params.objPxsCoordT()

  // the priority for the OBJ
  val prio = UInt(log2Up(params.numBgs) bits)

  // whether or not to visibly flip x/y 
  //val dispFlip = Vec2(dataType=Bool())
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
case class Gpu2dObjPalEntry(
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
  val objPalEntry = Gpu2dObjPalEntry(params=params)
  val memIdx = UInt(params.objPalEntryMemIdxWidth bits)
  //--------
}

case class Gpu2dBgPalEntry(
  params: Gpu2dParams
) extends Bundle {
  //--------
  val col = Rgb(params.rgbConfig)
  //--------
}
case class Gpu2dBgPalEntryStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val bgPalEntry = Gpu2dBgPalEntry(params=params)
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
  val pop = io.pop
  val rPopValid = Reg(Bool()) init(True) //init(False)
  rPopValid := True
  pop.valid := rPopValid
  //val rOutp = Reg(cloneOf(pop.payload))

  //val nextOutp = cloneOf(pop.payload)
  //val rOutp = RegNext(nextOutp) init(nextOutp.getZero)
  ////rOutp.init(rOutp.getZero)
  //pop.payload := rOutp
  val outp = cloneOf(pop.payload)
  val rPastOutp = RegNext(outp) init(outp.getZero)
  pop.payload := outp


  //pop.valid := True
  //val col = pop.payload.col
  //val physPxPos = pop.payload.physPxPos
  //val intnlPxPos = pop.payload.intnlPxPos
  //val tilePos = pop.payload.tilePos
  //--------
  //object WrState extends SpinalEnum(
  //  //defaultEncoding=binarySequential
  //) {
  //  val
  //    //readBgAttrs,
  //    //readBgEntriesAndObjPals, // read BG tilemaps and OBJ tiles
  //    //readBgEntriesAndObjAttrs,
  //    //calcBgPositions,  // add `scroll` to the address we want to read from
  //    //calcBgAddrs,      // calculate `bgEntryMem` addresses to read from
  //    initBgAddrs,      // initialize BG scroll offsets
  //    readBgEntries,    // read background tilemap entries
  //    readBgTiles,      // read background tiles
  //    readBgColors,     // read background colors
  //    drawBgs,
  //    readObjTiles,
  //    drawObjs,
  //    combineDrawnPxs,
  //    waitForLineMemSwitch
  //    = newElement();
  //}
  val loc = new Area {
    //--------
    //val nextCol = KeepAttribute(cloneOf(.payload.col))
    //val rPastCol = KeepAttribute(RegNext(col))
    //rPastCol.init(rPastCol.getZero)
    ////--------
    ////val nextPhysPxPos = KeepAttribute(cloneOf(.payload.physPxPos))
    //val rPastPhysPxPos = KeepAttribute(RegNext(physPxPos))
    //rPastPhysPxPos.init(rPastPhysPxPos.getZero)
    ////--------
    ////val nextIntnlPxPos = KeepAttribute(cloneOf(IntnlPxPos))
    //val rPastIntnlPxPos = KeepAttribute(RegNext(intnlPxPos))
    //rPastIntnlPxPos.init(rPastIntnlPxPos.getZero)

    //val nextIntnlPxPosPlus1Overflow = Vec2(Bool())
    //val rIntnlPxPosPlus1Overflow = RegNext(nextIntnlPxPosPlus1Overflow)
    //  .init(MkVec2(
    //    Bool(),
    //    x=False,
    //    y=False
    //  ))
    ////--------
    ////val nextTilePos = KeepAttribute(cloneOf(tilePos))
    //val rPastTilePos = KeepAttribute(RegNext(tilePos))
    //rPastTilePos.init(rPastTilePos.getZero)

    //val nextTilePosPlus1Overflow = Vec2(Bool())
    //val rTilePosPlus1Overflow = RegNext(nextTilePosPlus1Overflow)
    //  .init(MkVec2(
    //    dataType=Bool(),
    //    x=False,
    //    y=False,
    //  ))
    //--------
    //val nextBgPxPosCnt = params.bgPxsCoordT()
    //val rBgPxPosCnt = RegNext(nextBgPxPosCnt)
    //rBgPxPosCnt.init(rBgPxPosCnt.getZero)
    //val nextPhysPosCnt = DualTypeNumVec2(
    //  dataTypeX=UInt(log2Up(params.physFbSize2dScale.x) bits),
    //  dataTypeY=UInt(log2Up(params.physFbSize2dScale.y) bits),
    //)

    //val nextBgPxPosCnt = DualTypeNumVec2(
    //  dataTypeX=UInt(log2Up(params.physFbSize2dScale.x) bits),
    //  dataTypeY=UInt(log2Up(params.physFbSize2dScale.y) bits),
    //)
    //val nextBgPxPosCnt = params.coordT(
    //  someSize2d=ElabVec2[Int](
    //    x=4,
    //    y=3,
    //  )
    //)
    //val rBgPxPosCnt = RegNext(nextBgPxPosCnt) init(nextBgPxPosCnt.getZero)
    //--------
    val bgTileMem = Mem(
      //wordType=UInt(params.palEntryMemIdxWidth bits),
      wordType=Gpu2dTile(params=params, isObj=false),
      wordCount=params.numPxsForAllBgTiles,
    )
      .initBigInt(Array.fill(params.numPxsForAllBgTiles)(BigInt(0)).toSeq)
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
      wordCount=params.numPxsForAllObjTiles,
    )
      .initBigInt(Array.fill(params.numPxsForAllObjTiles)(BigInt(0)).toSeq)
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
      bgAttrsArr += Gpu2dBgAttrs(params=params)
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
      wordType=Gpu2dBgPalEntry(params=params),
      wordCount=params.numColsInBgPal,
    )
      .initBigInt(Array.fill(params.numColsInBgPal)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.bgPalEntryArrRamStyle)
    when (bgPalEntryPush.fire) {
      bgPalEntryMem.write(
        address=bgPalEntryPush.payload.memIdx,
        data=bgPalEntryPush.payload.bgPalEntry,
      )
    }

    val objPalEntryMem = Mem(
      wordType=Gpu2dObjPalEntry(params=params),
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
    val physCalcPos = LcvVideoCalcPos(
      someSize2d=params.physFbSize2d
    )
    //physCalcPos.io.en := pop.fire
    //physCalcPos.io.en := True
    val rPhysCalcPosEn = Reg(Bool()) init(False) //init(True)
    //physCalcPos.io.en := rPhysCalcPosEn
    physCalcPos.io.en := pop.fire
    outp.physPosInfo := physCalcPos.io.info
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
    //def LineMemEntry() = Vec.fill(params.numBgs)(Gpu2dRgba(params=params))
    def LineMemEntry() = Gpu2dRgba(params=params)
    //val lineMemArr = new ArrayBuffer[Mem[Gpu2dRgba]]()
    val bgLineMemArr = new ArrayBuffer[Mem[Gpu2dRgba]]()
    val objLineMemArr = new ArrayBuffer[Mem[Gpu2dRgba]]()
    def combinedLineMemArr = bgLineMemArr
    //val combinedLineMemArr = new ArrayBuffer[Mem[Gpu2dRgba]]()
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
        wordType=LineMemEntry(),
        wordCount=params.lineMemSize,
      )
        .initBigInt(Array.fill(params.lineMemSize)(BigInt(0)).toSeq)
        .addAttribute("ram_style", params.lineArrRamStyle)
      objLineMemArr += Mem(
        //wordType=Rgb(params.rgbConfig),
        wordType=LineMemEntry(),
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
    // round-robin indexing into the `ArrayBuffer`s

    //val rPastWrLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits)) init(0x1)
    //)
    val rWrLineMemArrIdx = KeepAttribute(
      Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits)) init(0x2)
    )

    //val rPastCombinedLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits)) init(0x0)
    //)
    val rCombinedLineMemArrIdx = KeepAttribute(
      Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits)) init(0x1)
    )

    //val rPastRdLineMemArrIdx = KeepAttribute(
    //  Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits)) init(0x3)
    //)
    val rRdLineMemArrIdx = KeepAttribute(
      Reg(UInt(log2Up(params.numLineMemsPerBgObjRenderer) bits)) init(0x0)
    )
    //wrLineMemArrIdx := outp.bgPxsPosSlice.pos.y(wrLineMemArrIdx.bitsRange)
    ////rdLineMemArrIdx(0) := !outp.bgPxsPosSlice.pos.y(0)
    //combinedLineMemArrIdx := wrLineMemArrIdx + 1
    //rdLineMemArrIdx := wrLineMemArrIdx + 
    when (rIntnlChangingRow) {
      //rPastWrLineMemArrIdx := rPastWrLineMemArrIdx + 1

      //rPastWrLineMemArrIdx := rPastWrLineMemArrIdx + 1
      rWrLineMemArrIdx := rWrLineMemArrIdx + 1

      //rPastCombinedLineMemArrIdx := rPastCombinedLineMemArrIdx + 1
      rCombinedLineMemArrIdx := rCombinedLineMemArrIdx + 1

      //rPastRdLineMemArrIdx := rPastRdLineMemArrIdx + 1
      rRdLineMemArrIdx := rRdLineMemArrIdx + 1
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
    // into `bgLineMemArr(someWrLineMemArrIdx)`
    // END: OLD NOTES

    // The background-writing pipeline iterates through the
    // `params.numBgs` backgrounds, and draws them into `bgLineMem`
    def wrBgPipeCntWidth = (
      log2Up(params.intnlFbSize2d.x + 1) + params.numBgsPow
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
        //val cnt = UInt(wrBgPipeFrontCntWidth bits)
        val cnt = UInt(wrBgPipeCntWidth bits)
        val cntPlus1 = UInt(wrBgPipeCntWidth bits)
        //val cnt = cloneOf(rWrBgPipeFrontCntWidth)
        // which background are we processing?
        // NOTE: we process backgrounds in reverse order
        //val bgIdx = SInt(wrBgPipeCntWidth bits)
        def bgIdx = UInt(wrBgPipeBgIdxWidth bits)
        //def bgIdx = cnt(params.numBgsPow - 1 downto 0)

        //def cntWillBeDone() = (
        //  // With more than one background, this should work
        //  //cnt(cnt.high downto params.numBgsPow)
        //  getCntPxPosX() >= params.intnlFbSize2d.x - 1
        //)

        def cntWillBeDone() = (
          //cnt === params.intnlFbSize2d.x * params.numBgs - 1
          cntPlus1.msb
        )
        //def cntDone() = (
        //  //cnt === params.intnlFbSize2d.x * params.numBgs
        //  cnt.msb
        //)

        // pixel x-position
        def getCntPxPosX() = cnt(cnt.high downto params.numBgsPow)

        //def getCntBgIdx() = (
        //  params.numBgs - 1 - cnt(params.numBgsPow - 1 downto 0)
        //)
      }
      def cnt = stage0.cnt
      def cntPlus1 = stage0.cntPlus1
      def bgIdx = stage0.bgIdx
      def cntWillBeDone() = stage0.cntWillBeDone()
      //def cntDone() = stage0.cntDone()
      def getCntPxPosX() = stage0.getCntPxPosX()

      // Stages after stage 0
      // NOTE: these pipeline stages are still separate
      val postStage0 = new Bundle {
        // scroll
        val scroll = cloneOf(bgAttrsArr(0).scroll)
        // indices into `bgEntryMem`
        val bgEntryMemIdx = UInt(params.bgEntryMemIdxWidth bits)
        // `Gpu2dBgEntry`s that have been read
        val bgEntry = Gpu2dBgEntry(params=params)

        // `Gpu2dTile`s that have been read
        val tile = Gpu2dTile(params=params, isObj=false)
        val palEntryMemIdx = UInt(params.objPalEntryMemIdxWidth bits)
        // Whether `palEntryMemIdx(someBgIdx)` is non-zero
        val palEntryNzMemIdx = Bool()

        // The following BG pipeline stages are only performed when
        // `palEntryNzMemIdx(someBgIdx)` is `True`
        // `Gpu2dBgPalEntry`s that have been read
        val palEntry = Gpu2dBgPalEntry(params=params)
      }

      def scroll = postStage0.scroll
      def bgEntryMemIdx = postStage0.bgEntryMemIdx
      def bgEntry = postStage0.bgEntry
      def tile = postStage0.tile
      def palEntryMemIdx = postStage0.palEntryMemIdx
      def palEntryNzMemIdx = postStage0.palEntryNzMemIdx
      def palEntry = postStage0.palEntry
    }

    def doInitWrBgPipeElem(): WrBgPipePayload = {
      val ret = WrBgPipePayload()
      ret.cnt := 0
      ret.cntPlus1 := 1
      ret.bgIdx := (default -> True)
      ret.postStage0 := ret.postStage0.getZero
      ret
    }

    def wrBgPipeCntStageIdx = 0
    def wrBgPipeCntPlus1StageIdx = 0
    def wrBgPipeBgIdxStageIdx = 0

    def wrBgPipeScrollStageIdx = 1
    def wrBgPipeBgEntryMemIdxStageIdx = 2
    def wrBgPipeBgEntryStageIdx = 3
    def wrBgPipeTileStageIdx = 4
    def wrBgPipePalEntryMemIdxStageIdx = 5
    def wrBgPipePalEntryNzMemIdxStageIdx = 6
    def wrBgPipePalEntryStageIdx = 7

    def wrBgPipeNumStages = 8 

    //def wrBgPipeNumStagesPerBg = 8

    //def wrBgPipeSize = (wrBgPipeNumElems * params.numBgs)
    //def wrBgPipeSize = wrBgPipeNumElems + 1

    //def wrBgPipeSize = max(wrBgPipeNumElems, params.numBgs) + 1 
    //def wrBgPipeNumStages = wrBgPipeNumStagesPerBg + params.numBgs


    //def wrBgPipeNumElemsGtNumBgs = wrBgPipeNumElems > params.numBgs
    // The sprite-drawing pipeline iterates through the `params.numObjs`
    // sprites, and draws them into `objLineMem` if they're on the current
    // scanline 
    def wrObjPipeObjAttrsMemIdxWidth = params.objAttrsMemIdxWidth + 1
    //val rWrObjPipeFrontObjAttrsMemIdx = KeepAttribute(
    //  Reg(SInt(wrObjPipeObjAttrsMemIdxWidth bits)) init(0x0)
    //)
    case class WrObjPipePayload() extends Bundle {
      // Which sprite are we processing?
      //val objAttrsMemIdx = SInt(params.objAttrsMemIdxWidth + 1 bits)
      //val objAttrsMemIdx = cloneOf(rWrObjPipeFrontObjAttrsMemIdx)
      val objAttrsMemIdx = SInt(wrObjPipeObjAttrsMemIdxWidth bits)
      val objAttrsMemIdxMinus1 = SInt(wrObjPipeObjAttrsMemIdxWidth bits)
      //def objAttrsMemIdxDidUnderflow() = (
      //  //objAttrsMemIdx === 0
      //  objAttrsMemIdx.msb
      //)
      def objAttrsMemIdxWillUnderflow() = (
        objAttrsMemIdxMinus1.msb
      )
      val postStage0 = new Bundle {
        // What are the `Gpu2dObjAttrs` of our sprite? 
        val objAttrs = Gpu2dObjAttrs(params=params)
        val stage2 = new Bundle {
          // Are the X and Y positions of this sprite within the current
          // line?
          // NOTE: the x-pos could be out of range if it's negative
          val posInLine = Bool()
          // What's the tile this sprite represents? We read it 
          val tileMemIdx = UInt(params.objTileMemIdxWidth bits)
        }
        // The following OBJ pipeline stages are only performed (besides
        // just copying data around) when `stage2.posIsInLine` is `True`
        val tile = Gpu2dTile(params=params, isObj=true)
        val palEntryMemIdx = UInt(params.objPalEntryMemIdxWidth bits)
        // Whether `palEntryMemIdx` is non-zero
        val palEntryNzMemIdx = Bool()
        // The following OBJ pipeline stages are only performed when
        // `palEntryNzMemIdx` is `True`
        val palEntry = Gpu2dObjPalEntry(params=params)
      }
      def objAttrs = postStage0.objAttrs
      def stage2 = postStage0.stage2
      def tile = postStage0.tile
      def palEntryMemIdx = postStage0.palEntryMemIdx
      def palEntryNzMemIdx = postStage0.palEntryNzMemIdx
      def palEntry = postStage0.palEntry
    }
    def doInitWrObjPipeElem(): WrObjPipePayload = {
      val ret = WrObjPipePayload()
      ret.objAttrsMemIdx := (1 << (ret.objAttrsMemIdx.getWidth - 1)) - 1
      ret.objAttrsMemIdxMinus1 := (
        (1 << (ret.objAttrsMemIdx.getWidth - 1)) - 2
      )
      ret.postStage0 := ret.postStage0.getZero
      ret
    }

    def wrObjPipeObjAttrsMemIdxStageIdx = 0
    def wrObjPipeObjAttrsStageIdx = 1
    def wrObjPipeTileMemIdxStageIdx = 2
    def wrObjPipePosInLineStageIdx = 2
    def wrObjPipeTileStageIdx = 3
    def wrObjPipePalEntryMemIdxStageIdx = 4
    def wrObjPipePalEntryNzMemIdxStageIdx = 5
    def wrObjPipePalEntryStageIdx = 6
    def wrObjPipeNumStages = 7

    def wrMaxBgObjPipeNumStages = max(
      wrBgPipeNumStages,
      wrObjPipeNumStages
    )

    //def wrObjPipeSize = wrObjPipeNumElems * params.numObjsPow
    //def wrObjPipeSize = max(wrObjPipeNumElems, params.numObjsPow) + 1 
    //def wrObjPipeSize = wrObjPipeNumStages + params.numObjsPow
    //def wrObjPipeNumElemsGtNumObjsPow = (
    //  wrObjPipeNumElems > params.numObjsPow
    //)

    //case class WrCombinedPipeElem() extends Bundle {
    //  //--------
    //  //val bg = WrBgPipePayload()
    //  //val obj = WrObjPipePayload()
    //  //--------
    //}
    def rIntnlChangingRow = Reg(Bool()) init(False)
    rIntnlChangingRow := (
      rPastOutp.bgPxsPosSlice.pos.y
      === rPastOutp.bgPxsPosSlice.pastPos.y + 1
    )

    //def wrPipeSize = wrBgPipeSize + wrObjPipeSize
    val wrBgPipeIn = KeepAttribute(
      //Vec.fill(wrMaxBgObjPipeNumStages)(Reg(WrBgPipePayload()))
      Vec.fill(wrMaxBgObjPipeNumStages)(Flow(WrBgPipePayload()))
    )
    val wrBgPipeOut = KeepAttribute(
      //Vec.fill(wrMaxBgObjPipeNumStages)(Reg(WrBgPipePayload()))
      Vec.fill(wrMaxBgObjPipeNumStages)(Flow(WrBgPipePayload()))
    )
    //val rWrBgPipePayloadVec = KeepAttribute(
    //  Vec.fill(wrMaxBgObjPipeNumStages)(
    //    Reg(WrBgPipePayload()) init(wrBgPipe(0).payload.getZero)
    //  )
    //)
    //for (idx <- 0 to wrMaxBgObjPipeNumStages - 1) {
    //  //rWrBgPipe(idx).init(
    //  //  //rWrBgPipe(idx).getZero
    //  //  doInitWrBgPipeElem()
    //  //)
    //}
    //wrBgPipe(0).valid := True
    val rWrBgPipeFrontValid = Reg(Bool()) init(False)
    val rWrBgPipeFrontPayload = Reg(WrBgPipePayload())
    rWrBgPipeFrontPayload.init(rWrBgPipeFrontPayload.getZero)

    wrBgPipeIn(0).valid := rWrBgPipeFrontValid
    wrBgPipeIn(0).payload := rWrBgPipeFrontPayload
    for (idx <- 1 to wrBgPipeIn.size - 1) {
      // Create pipeline registering
      wrBgPipeIn(idx) <-< wrBgPipeOut(idx - 1)
    }
    for (idx <- 0 to wrBgPipeIn.size - 1) {
      // Connect output `valid` to input `valid`
      wrBgPipeOut(idx).valid := wrBgPipeIn(idx).valid
    }

    // Control the background pipeline
    when (rIntnlChangingRow) {
      rWrBgPipeFrontValid := True
      rWrBgPipeFrontPayload := doInitWrBgPipeElem()
    } otherwise { // when (!rIntnlChangingRow)
      when (wrBgPipeIn(0).fire) {
        rWrBgPipeFrontPayload.cnt := rWrBgPipeFrontPayload.cnt + 1
        rWrBgPipeFrontPayload.bgIdx := rWrBgPipeFrontPayload.bgIdx - 1
      }
      when (rWrBgPipeFrontPayload.cntWillBeDone()) {
        rWrBgPipeFrontValid := False
      }
    }

    val wrObjPipeOut = KeepAttribute(
      //Vec.fill(wrMaxBgObjPipeNumStages)(Reg(WrObjPipePayload()))
      Vec.fill(wrMaxBgObjPipeNumStages)(Flow(WrObjPipePayload()))
    )
    val wrObjPipeIn = KeepAttribute(
      //Vec.fill(wrMaxBgObjPipeNumStages)(Reg(WrObjPipePayload()))
      Vec.fill(wrMaxBgObjPipeNumStages)(Flow(WrObjPipePayload()))
    )
    //val rWrObjPipePayloadVec = KeepAttribute(
    //  Vec.fill(wrMaxBgObjPipeNumStages)(
    //    Reg(WrObjPipePayload()) init(wrObjPipe(0).payload.getZero)
    //  )
    //)
    //for (idx <- 0 to wrMaxBgObjPipeNumStages - 1) {
    //  //rWrObjPipe(idx).init(rWrObjPipe(idx).getZero)
    //}
    //wrObjPipe(0).valid := True
    val rWrObjPipeFrontValid = Reg(Bool()) init(True)
    //when (wrObjPipe(0).objAttrsMemIdxWillUnderflow()) {
    //  rWrObjPipeFrontValid := False
    //}
    val rWrObjPipeFrontPayload = Reg(WrObjPipePayload())
    rWrObjPipeFrontPayload.init(rWrObjPipeFrontPayload.getZero)

    wrObjPipeIn(0).valid := rWrObjPipeFrontValid
    wrObjPipeIn(0).payload := rWrObjPipeFrontPayload
    //for (idx <- 1 to wrObjPipeIn.size - 1) {
    //  wrObjPipeIn(idx) <-< wrObjPipeOut(idx - 1)
    //}
    for (idx <- 1 to wrObjPipeIn.size - 1) {
      // Create pipeline registering
      wrObjPipeIn(idx) <-< wrObjPipeOut(idx - 1)
    }
    for (idx <- 0 to wrObjPipeIn.size - 1) {
      // Connect output `valid` to input `valid`
      wrObjPipeOut(idx).valid := wrObjPipeIn(idx).valid
    }
    //wrObjPipe.last.ready := True

    // Control the sprite pipeline
    when (rIntnlChangingRow) {
      rWrObjPipeFrontValid := True
      rWrObjPipeFrontPayload := doInitWrObjPipeElem()
    } otherwise { // when (!rIntnlChangingRow)
      when (wrObjPipeIn(0).fire) {
        rWrObjPipeFrontPayload.objAttrsMemIdx := (
          rWrObjPipeFrontPayload.objAttrsMemIdx - 1
        )
        rWrObjPipeFrontPayload.objAttrsMemIdxMinus1 := (
          rWrObjPipeFrontPayload.objAttrsMemIdxMinus1 - 1
        )
      }
      when (rWrObjPipeFrontPayload.objAttrsMemIdxWillUnderflow()) {
        rWrObjPipeFrontValid := False
      }
    }

    //val wrCombinedPipe = KeepAttribute(
    //  Vec.fill()(Stream(WrCombinedPipeElem()))
    //)

    //if (!wrPipeBgNumElemsGtNumBgs) {
    //} else { // if (wrPipeBgNumElemsGtNumBgs)
    //}
    //if (!wrPipeObjNumElemsGtNumObjsPow) {
    //} else { // if (wrPipeObjNumElemsGtNumObjsPow)
    //}

    //def writeHandleBgObjPipeElem[
    //  WrPipeElemT <: Bundle
    //](
    //  someWrPipe: Vec[WrPipeElemT],     // `rWrBgPipe` or `rWrObjPipe`
    //  someWrPipeStageIdx: Int,
    //  someWrPipeNumMainStages: Int,     // `wrBgPipeNumStages`
    //                                    // or `wrObjPipeNumStages`
    //  someLineMem: Mem[Gpu2dRgba],      // `bgLineMem` or `objLineMem`
    //)(
    //  idxEqStageIdxFunc: (
    //    Vec[WrPipeElemT], // `someWrPipe`
    //    Int,              // `someWrPipeStageIdx`
    //    Int,              // `someWrPipeNumMainStages`
    //    Int,              // `idx`
    //    Mem[Gpu2dRgba],   // `someLineMem`: 
    //  ) => Unit,
    //  idxLtStageIdxFunc: (
    //    Vec[WrPipeElemT], // `someWrPipe`
    //    Int,              // `someWrPipeStageIdx`
    //    Int,              // `someWrPipeNumMainStages`
    //    Int,              // `idx`
    //    Mem[Gpu2dRgba],   // `someLineMem`: 
    //  ) => Unit,
    //  postMainFunc: (
    //    Vec[WrPipeElemT], // `someWrPipe`
    //    Int,              // `someWrPipeStageIdx`
    //    Int,              // `someWrPipeNumMainStages`
    //    Int,              // `idx`
    //    Mem[Gpu2dRgba],   // `someLineMem`: 
    //  ) => Unit,
    //): Unit = {
    //  GenericHandlePipeElem(
    //    somePipe=someWrPipe,
    //    somePipeStageIdx=someWrPipeStageIdx,
    //    somePipeNumMainStages=someWrPipeNumMainStages,
    //    someExtData=someLineMem,
    //  )(
    //    idxEqStageIdxFunc=idxEqStageIdxFunc,
    //    idxLtStageIdxFunc=idxLtStageIdxFunc,
    //    postMainFunc=postMainFunc,
    //  )
    //}

    def writeBgLineMemEntries(
      someWrLineMemArrIdx: Int,
    ): Unit = {
      // Handle backgrounds
      val bgLineMem = bgLineMemArr(someWrLineMemArrIdx)

      {
        //HandleFlowPipe(
        //  pipeIn=wrBgPipeIn,
        //  pipeOut=wrBgPipeOut,
        //  pipeStageIdx=wrBgPipeCntStageIdx,
        //  pipeNumMainStages=wrBgPipeNumStages,
        //)(
        //  idxEqStageIdxFunc=(
        //    mostArgs: DualPipeFuncMostArgs[Flow[WrBgPipePayload]],
        //    idx: Int,
        //  ) => {
        //    //if (mostArgs.setOutToPast) {
        //    //}
        //  },
        //  //idxLtStageIdxFunc=(
        //  //  mostArgs: DualPipeFuncMostArgs[Flow[WrBgPipePayload]],
        //  //  idx: Int,
        //  //) => {
        //  //},
        //  //postMainFunc=(
        //  //  mostArgs: DualPipeFuncMostArgs[Flow[WrBgPipePayload]],
        //  //  idx: Int,
        //  //) => {
        //  },
        //)
      }

      //writeHandleBgObjPipeElem(
      //  someWrPipe=
      //)
      //for (idx <- 0 to rWrBgPipe.size - 1) {
      //}
      //for (idx <- 0 to wrMaxBgObjPipeNumStages - 1) {
      //  //if (idx > 0) {
      //  //  rWrPipe(idx) := rWrPipe(idx - 1)
      //  //}

      //  //val bgScrollVecIdx = idx - wrBgPipeScrollVecIdx
      //  //if (bgScrollVecIdx >= 0 && bgScrollVecIdx < params.numBgs) {
      //  //  for (jdx <- 0 to bgScrollVecIdx) {
      //  //    if (jdx == bgScrollVecIdx) {
      //  //    } else { // if (jdx < bgScrollVecIdx)
      //  //      //rWrPipe(idx).scrollVec
      //  //    }
      //  //  }
      //  //}
      //  //val bgAddrVecIdx = idx - wrBgPipeAddrVecIdx
      //  //if (bgAddrVecIdx >= 0 && bgAddrVecIdx < params.numBgs) {
      //  //  for (jdx <- 0 to bgAddrVecIdx) {
      //  //    if (jdx == bgAddrVecIdx) {
      //  //    } else { // if (jdx < bgAddrVecIdx)
      //  //      //if (jdx == bgAddrVecIdx - 1) {
      //  //      //}
      //  //    }
      //  //  }
      //  //}
      //  //val bgEntryVecIdx = idx - wrBgPipeEntryVecIdx
      //  //if (bgEntryVecIdx >= 0 && bgEntryVecIdx < params.numBgs) {
      //  //  for (jdx <- 0 to bgEntryVecIdx) {
      //  //    if (jdx == bgEntryVecIdx) {
      //  //    } else { // if (jdx < bgEntryVecIdx)
      //  //      //rWrPipe(idx).bg.
      //  //    }
      //  //  }
      //  //}
      //  //val bgTileVecIdx = idx - wrBgPipeTileVecIdx
      //  //if (bgTileVecIdx >= 0 && bgTileVecIdx < params.numBgs) {
      //  //  for (jdx <- 0 to bgTileVecIdx) {
      //  //    if (jdx == bgTileVecIdx) {
      //  //    } else { // if (jdx < bgTileVecIdx)
      //  //    }
      //  //  }
      //  //}
      //  //val bgPalEntryVecIdx = idx - wrBgPipePalEntryVecIdx
      //  //if (bgPalEntryVecIdx >= 0 && bgPalEntryVecIdx < params.numBgs) {
      //  //  for (jdx <- 0 to bgPalEntryVecIdx) {
      //  //    if (jdx == bgPalEntryVecIdx) {
      //  //    } else { // if (jdx < bgPalEntryVecIdx)
      //  //    }
      //  //  }
      //  //}
      //}
      //--------
      //--------
    }
    def writeObjLineMemEntries(
      someWrLineMemArrIdx: Int,
    ): Unit = {
      // Handle sprites
      val objLineMem = objLineMemArr(someWrLineMemArrIdx)
      for (idx <- 0 to wrMaxBgObjPipeNumStages - 1) {
        {
          //val objAttrsVecIdx = idx - wrObjPipeAttrsIdx
          //if (objAttrsVecIdx >= 0 && objAttrsVecIdx < params.numObjsPow) {
          //  for (jdx <- 0 to objAttrsVecIdx) {
          //    if (jdx == objAttrsVecIdx) {
          //    } else { // if (jdx < objAttrsVecIdx)
          //    }
          //  }
          //}
          //val objTileVecIdx = idx - wrObjPipeTileIdx
          //if (objTileVecIdx >= 0 && objTileVecIdx < params.numObjsPow) {
          //  for (jdx <- 0 to objTileVecIdx) {
          //    if (jdx == objTileVecIdx) {
          //    } else { // if (jdx < objTileVecIdx)
          //    }
          //  }
          //}
          //val objPalEntryVecIdx = idx - wrObjPipePalEntryIdx
          //if (
          //  objPalEntryVecIdx >= 0 && objPalEntryVecIdx < params.numObjsPow
          //) {
          //  for (jdx <- 0 to objPalEntryVecIdx) {
          //    if (jdx == objPalEntryVecIdx) {
          //    } else { // if (jdx < objPalEntryVecIdx)
          //    }
          //  }
          //}

          //wrObjPipeObjAttrsMemIdxStageIdx
          //wrObjPipeObjAttrsStageIdx
          //wrObjPipeTileMemIdxStageIdx
          //wrObjPipePosInLineStageIdx
          //wrObjPipeTileStageIdx
          //wrObjPipePalEntryMemIdxStageIdx
          //wrObjPipePalEntryNzMemIdxStageIdx
          //wrObjPipePalEntryStageIdx
          //wrObjPipeNumStages
        }
      }
    }
    //--------
    def combineLineMemEntries(
      someCombinedLineMemArrIdx: Int
    ): Unit = {
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

    def readLineMemEntries(
      someRdLineMemArrIdx: Int,
    ): Unit = {
      //val bgLineMem = bgLineMemArr(someRdLineMemArrIdx)
      //rdBgLineMemEntry := bgLineMem.readAsync(address=rdLineMemIdx)

      //val objLineMem = objLineMemArr(someRdLineMemArrIdx)
      //rdObjLineMemEntry := objLineMem.readAsync(address=rdLineMemIdx)

      //val combinedLineMem = combinedLineMemArr(someRdLineMemArrIdx)
      //rdCombinedLineMemEntry := combinedLineMem.readAsync(
      //  address=rdLineMemIdx
      //)
    }

    switch (rWrLineMemArrIdx) {
      for (idx <- 0 to (1 << rWrLineMemArrIdx.getWidth) - 1) {
        is (idx) {
          writeBgLineMemEntries(someWrLineMemArrIdx=idx)
          writeObjLineMemEntries(someWrLineMemArrIdx=idx)
        }
      }
      //default {
      //  wrLineMemEntry := rPastWrLineMemEntry
      //}
    }
    switch (rCombinedLineMemArrIdx) {
      for (idx <- 0 to (1 << rCombinedLineMemArrIdx.getWidth) - 1) {
        is (idx) {
          combineLineMemEntries(someCombinedLineMemArrIdx=idx)
        }
      }
      //default {
      //  wrLineMemEntry := rPastWrLineMemEntry
      //}
    }
    //when (pop.fire) {
    switch (rRdLineMemArrIdx) {
      for (idx <- 0 to (1 << rRdLineMemArrIdx.getWidth) - 1) {
        is (idx) {
          readLineMemEntries(someRdLineMemArrIdx=idx)
        }
      }
      default {
        //rdBgLineMemEntry := rPastRdBgLineMemEntry
        outp.col := rPastOutp.col
      }
    }
    //}
    //--------
    //for (idx <- 0 to params.numBgs - 1) {
    //  //bgColVec(idx) := palEntryMem.readAsync(
    //  //  address=
    //}
    //--------
    //--------
  }
}
