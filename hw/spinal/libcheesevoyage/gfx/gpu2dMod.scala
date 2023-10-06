package libcheesevoyage.gfx
import libcheesevoyage.general.FifoMiscIo
import libcheesevoyage.general.AsyncReadFifo
import libcheesevoyage.general.FifoIo
import libcheesevoyage.general.Vec2
import libcheesevoyage.general.MkVec2
//import libcheesevoyage.general.MkVec2
import libcheesevoyage.general.ElabVec2
import libcheesevoyage.general.PipeSkidBuf
import libcheesevoyage.general.PipeSkidBufIo

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
  physFbSize2dScale: ElabVec2[Int],  // the integer multiple of 
                              // `intnlFbSize2d` to obtain the physical
                              // resolution of the video signal.
                              // This is used to duplicate generated pixels 
                              // so that the generated video signal can 
                              // fill the screen.
  tileSize2d: ElabVec2[Int],  // width/height of a tile (in pixels)
  numTilesPow: Int,           // power of two for total number of tiles
                              // (how much memory to reserve for tiles)
  bgSize2dInTiles: ElabVec2[Int], // width/height of a background
                              // (in number of tiles)
  numBgsPow: Int,             // power of two for the total number of
                              // backgrounds
  numObjsPow: Int,            // power of two for the total number of 
                              // sprites
  //numObjsPerScanline: Int,  // how many sprites to process in one
  //                          // scanline (possibly one per cycle?)
  numColsInPalPow: Int,       // power of two for how many colors in the palette
  //--------
  tileColArrRamStyle: String="block",
  bgEntryArrRamStyle: String="block",
  bgAttrsArrRamStyle: String="block",
  objAttrsArrRamStyle: String="block",
  palEntryArrRamStyle: String="block",
  lineArrRamStyle: String="block",
) {
  //--------
  def numTiles = 1 << numTilesPow
  def numBgs = 1 << numBgsPow
  def numObjs = 1 << numObjsPow
  def numColsInPal = 1 << numColsInPalPow
  //--------
  def physFbSize2d = ElabVec2[Int](
    x=intnlFbSize2d.x * physFbSize2dScale.x,
    y=intnlFbSize2d.y * physFbSize2dScale.y,
  )
  //def lineMemSize = physFbSize2dScale.y * params.physFbSize2d.x

  //def halfLineMemSize = intnlFbSize2d.x
  //def lineMemSize = intnlFbSize2d.x * 2
  //--------
  def numPxsPerTile = tileSize2d.x * tileSize2d.y
  def numPxsForAllTiles = numTiles * numPxsPerTile
  //--------
  def numTilesPerBg = bgSize2dInTiles.x * bgSize2dInTiles.y
  def numPxsPerBg = (
    numTilesPerBg * numPxsPerTile
  )
  def numPxsForAllBgs = numBgs * numPxsPerBg
  //--------
  def bgSize2dInPixels = ElabVec2[Int](
    x=bgSize2dInTiles.x * tileSize2d.x,
    y=bgSize2dInTiles.y * tileSize2d.y,
  )
  //--------
  def objAttrsMemIdxWidth = log2Up(numObjs)
  //def bgMemIdxWidth = log2Up(numBgs)
  def tileIdxWidth = log2Up(numTiles)

  def tileColMemIdxWidth = log2Up(numPxsForAllTiles)

  def bgEntryMemIdxWidth = log2Up(numTilesPerBg)
  def bgAttrsMemIdxWidth = log2Up(numBgs)

  def palEntryMemIdxWidth = log2Up(numColsInPal)
  //def tilePixelIdxWidth = log2Up(numPxsForAllTiles)
  //--------
  def coordT(
    someSize2d: ElabVec2[Int],
  ) = LcvVgaCtrlMiscIo.coordT(fbSize2d=someSize2d)
  def physPxCoordT() = coordT(someSize2d=physFbSize2d)
  def bgPxCoordT() = coordT(someSize2d=bgSize2dInPixels)
  def bgTilesCoordT() = coordT(someSize2d=bgSize2dInTiles)
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
      x=(1920 / 4).toInt, // 480
      y=(1080 / 4).toInt, // 270
    ),
    physFbSize2dScale: ElabVec2[Int]=ElabVec2[Int](
      //x=3, // 1920 / 4 = 640
      //y=3, // 1080 / 3 = 360
      x=4, // 1920 / 4 = 480
      y=4, // 1080 / 4 = 270
    ),
    tileSize2d: ElabVec2[Int]=ElabVec2[Int](x=8, y=8),
    numBgsPow: Int=log2Up(4),
    numObjsPow: Int=log2Up(256),
    //numObjsPerScanline: Int=64,
    numColsInPalPow: Int=log2Up(256),
    //--------
    tileColArrRamStyle: String="block",
    bgEntryArrRamStyle: String="block",
    bgAttrsArrRamStyle: String="block",
    objAttrsArrRamStyle: String="block",
    palEntryArrRamStyle: String="block",
    lineArrRamStyle: String="block",
    //--------
  ) = Gpu2dParams(
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
    tileSize2d=tileSize2d,
    numTilesPow=(
      //1024
      //2048 // (480 * 270) / (8 * 8) = 2025
      //1 <<
      log2Up(
        (intnlFbSize2d.x * intnlFbSize2d.y)
        / (tileSize2d.x * tileSize2d.y)
      )
    ),
    //bgSize2dInTiles=ElabVec2[Int](x=128, y=128),
    bgSize2dInTiles=ElabVec2[Int](
      x=(1 << log2Up(intnlFbSize2d.x / tileSize2d.x)),
      y=(1 << log2Up(intnlFbSize2d.y / tileSize2d.y)),
    ),
    //numBgs=4,
    //numObjs=256,
    ////numObjsPerScanline=64,
    //numColsInPal=256,
    numBgsPow=numBgsPow,
    numObjsPow=numObjsPow,
    //numObjsPerScanline=numObjsPerScanline,
    numColsInPalPow=numColsInPalPow,
    //--------
    tileColArrRamStyle=tileColArrRamStyle,
    bgEntryArrRamStyle=bgEntryArrRamStyle,
    bgAttrsArrRamStyle=bgAttrsArrRamStyle,
    objAttrsArrRamStyle=objAttrsArrRamStyle,
    palEntryArrRamStyle=palEntryArrRamStyle,
    lineArrRamStyle=lineArrRamStyle,
    //--------
  )
}

case class Gpu2dTileCol(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  // index into `Gpu2d.loc.palEntryMem`
  val colIdx = UInt(params.palEntryMemIdxWidth bits)
  //--------
}
case class Gpu2dTileColStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  //val rgb = Rgb(params.rgbConfig)
  val tileCol = Gpu2dTileCol(params=params)

  // `Mem` index, so in units of pixels
  //val idx = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  //val idx = UInt(16 bits)
  val memIdx = UInt(params.tileColMemIdxWidth bits)
  //--------
}

case class Gpu2dObjAttrs(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  // The index, in tiles, of the tile represented by this sprite
  val tileIdx = UInt(params.tileIdxWidth bits)
  //val pos = params.coordT(fbSize2d=params.bgSize2dInPixels)

  // position within the tilemap, in pixels
  val pos = params.bgPxCoordT()

  // whether or not to flip x/y 
  val visibFlip = Vec2(dataType=Bool())
  //--------
}

case class Gpu2dObjAttrsStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val objAttr = Gpu2dObjAttrs(params=params)
  // `Mem` index
  val memIdx = UInt(params.objAttrsMemIdxWidth bits)
  //--------
}

case class Gpu2dBgEntry(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  // The index, in tiles, of the tile represented by this tilemap entry
  val tileIdx = UInt(params.tileIdxWidth bits)
  //val scroll = params.bgPxCoordT()
  val visibFlip = Vec2(dataType=Bool())
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
  val scroll = params.bgPxCoordT()
  //--------
}
case class Gpu2dBgAttrsStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val attrs = Gpu2dBgAttrs(params=params)

  val memIdx = UInt(params.bgAttrsMemIdxWidth bits)
  //--------
}

case class Gpu2dPalEntry(
  params: Gpu2dParams
) extends Bundle {
  //--------
  val col = Rgb(params.rgbConfig)
  //--------
}
case class Gpu2dPalEntryStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val palEntry = Gpu2dPalEntry(params=params)
  val memIdx = UInt(params.palEntryMemIdxWidth bits)
  //--------
}
case class Gpu2dPopPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val col = Rgb(params.rgbConfig)
  val physPxPos = params.physPxCoordT()
  val intnlPxPos = params.bgPxCoordT()
  val tilePos = params.bgTilesCoordT()
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
  //val tilePush = slave Stream()
  //val tilePush = slave Stream()
  val tileColPush = slave Stream(Gpu2dTileColStmPayload(params=params))
  val bgEntryPush = slave Stream(Gpu2dBgEntryStmPayload(params=params))
  val bgAttrsPush = slave Stream(Gpu2dBgAttrsStmPayload(params=params))
  val objAttrsPush = slave Stream(Gpu2dObjAttrsStmPayload(params=params))
  val palEntryPush = slave Stream(Gpu2dPalEntryStmPayload(params=params))
  //--------
  val pop = master Stream(Gpu2dPopPayload(params=params))
  //--------
}
case class Gpu2d(
  params: Gpu2dParams,
) extends Component {
  //--------
  val io = Gpu2dIo(params=params)
  //--------
  val tileColPush = io.tileColPush
  val bgEntryPush = io.bgEntryPush
  val bgAttrsPush = io.bgAttrsPush
  val objAttrsPush = io.objAttrsPush
  val palEntryPush = io.palEntryPush
  //--------
  val pop = io.pop
  val col = pop.payload.col
  val physPxPos = pop.payload.physPxPos
  val intnlPxPos = pop.payload.intnlPxPos
  val tilePos = pop.payload.tilePos
  //--------
  val loc = new Area {
    //--------
    //val nextCol = KeepAttribute(cloneOf(.payload.col))
    val rPastCol = KeepAttribute(RegNext(col))
    rPastCol.init(rPastCol.getZero)
    //--------
    //val nextPhysPxPos = KeepAttribute(cloneOf(.payload.physPxPos))
    val rPastPhysPxPos = KeepAttribute(RegNext(physPxPos))
    rPastPhysPxPos.init(rPastPhysPxPos.getZero)
    //--------
    //val nextIntnlPxPos = KeepAttribute(cloneOf(IntnlPxPos))
    val rPastIntnlPxPos = KeepAttribute(RegNext(intnlPxPos))
    rPastIntnlPxPos.init(rPastIntnlPxPos.getZero)
    //--------
    //val nextTilePos = KeepAttribute(cloneOf(.payload.tilePos))
    val rPastTilePos = KeepAttribute(RegNext(tilePos))
    rPastTilePos.init(rPastTilePos.getZero)

    val nextTilePosPlus1Overflow = Vec2(Bool())
    val rTilePosPlus1Overflow = RegNext(nextTilePosPlus1Overflow).init( 
      MkVec2(
        dataType=Bool(),
        x=False,
        y=False,
      )
    )

    //--------
    val tileColMem = Mem(
      //wordType=UInt(params.palEntryMemIdxWidth bits),
      wordType=Gpu2dTileCol(params=params),
      wordCount=params.numPxsForAllTiles,
    )
      .initBigInt(Array.fill(params.numPxsForAllTiles)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.tileColArrRamStyle)
    //--------
    //val bgEntryMem = Mem(
    //  wordType=Gpu2dBgEntry(params=params),
    //  wordCount=params.numColsInPal,
    //)
    //  .addAttribute("ram_style", params.palEntryArrRamStyle)
    //--------
    val bgEntryMemArr = new ArrayBuffer[Mem[Gpu2dBgEntry]]()
    for (idx <- 0 to params.numBgs - 1) {
      bgEntryMemArr += Mem(
        wordType=Gpu2dBgEntry(params=params),
        wordCount=params.numTilesPerBg,
      )
        .initBigInt(Array.fill(params.numTilesPerBg)(BigInt(0)).toSeq)
        .addAttribute("ram_style", params.palEntryArrRamStyle)
        .setName(f"bgEntryMemArr_$idx")
    }
    val bgAttrsMem = Mem(
      wordType=Gpu2dBgAttrs(params=params),
      wordCount=params.numBgs,
    )
      .initBigInt(Array.fill(params.numBgs)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.bgAttrsArrRamStyle)
    //--------
    val objAttrsMem = Mem(
      wordType=Gpu2dObjAttrs(params=params),
      wordCount=params.numObjs,
    )
      .initBigInt(Array.fill(params.numObjs)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.objAttrsArrRamStyle)
    //--------
    val palEntryMem = Mem(
      wordType=Gpu2dPalEntry(params=params),
      wordCount=params.numColsInPal,
    )
      .initBigInt(Array.fill(params.numColsInPal)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.palEntryArrRamStyle)
    //--------
    //val lineMem = Mem(
    //  wordType=Rgb(params.rgbConfig),
    //  wordCount=params.lineMemSize,
    //)
    //  .initBigInt(Array.fill(params.lineMemSize)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", params.lineArrRamStyle)
    //val nextLineIdx = KeepAttribute(UInt(1 bits))
    //val rLineIdx = KeepAttribute(RegNext(nextLineIdx)) init(0x0)
    //--------
    //--------
  }
  //--------
  // Handle backgrounds
  //--------
}
