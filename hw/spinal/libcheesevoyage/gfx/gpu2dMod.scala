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
  physFbSize2dLslScale: ElabVec2[Int],  // the integer value to logical
                              // left shift `intnlFbSize2d` by to obtain
                              // the physical resolution of the video
                              // signal.
                              // This is used to duplicate generated pixels 
                              // so that the generated video signal can 
                              // fill the screen.
  tileSize2dPow: ElabVec2[Int],  // power of two for width/height of a tile 
                              // (in pixels)
  numTilesPow: Int,           // power of two for total number of tiles
                              // (how much memory to reserve for tiles)
  bgSize2dInTilesPow: ElabVec2[Int], // power of two for width/height of a
                              // background
                              // (in number of tiles)
  numBgsPow: Int,             // power of two for the total number of
                              // backgrounds
  numObjsPow: Int,            // power of two for the total number of 
                              // sprites
  //numObjsPerScanline: Int,  // how many sprites to process in one
  //                          // scanline (possibly one per cycle?)
  numColsInPalPow: Int,       // power of two for how many colors in the
                              // palette
  //--------
  tileArrRamStyle: String="block",
  bgEntryArrRamStyle: String="block",
  //bgAttrsArrRamStyle: String="block",
  //objAttrsArrRamStyle: String="block",
  palEntryArrRamStyle: String="block",
  lineArrRamStyle: String="block",
) {
  //--------
  def tileSize2d = ElabVec2[Int](
    x=1 << tileSize2dPow.x,
    y=1 << tileSize2dPow.y,
  )
  def numTiles = 1 << numTilesPow
  def numBgs = 1 << numBgsPow
  def numObjs = 1 << numObjsPow
  def numColsInPal = 1 << numColsInPalPow
  //--------
  def physFbSize2d = ElabVec2[Int](
    //x=intnlFbSize2d.x * physFbSize2dScale.x,
    //y=intnlFbSize2d.y * physFbSize2dScale.y,
    x=intnlFbSize2d.x << physFbSize2dLslScale.x,
    y=intnlFbSize2d.y << physFbSize2dLslScale.y,
  )
  //def lineMemSize = physFbSize2dScale.y * params.physFbSize2d.x

  //def halfLineMemSize = intnlFbSize2d.x
  //def lineMemSize = intnlFbSize2d.x * 2
  def lineMemSize = intnlFbSize2d.x
  def numLineMems = 2
  //--------
  def bgSize2dInTiles = ElabVec2[Int](
    x=1 << bgSize2dInTilesPow.x,
    y=1 << bgSize2dInTilesPow.y,
  )
  def bgSize2dInPxs = ElabVec2[Int](
    x=bgSize2dInTiles.x * tileSize2d.x,
    y=bgSize2dInTiles.y * tileSize2d.y,
  )
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
  //def objAttrsMemIdxWidth = log2Up(numObjs)
  //def objAttrsVecIdxWidth = log2Up(numObjs)
  def objAttrsVecIdxWidth = numObjsPow
  //def bgMemIdxWidth = log2Up(numBgs)
  //def tileIdxWidth = log2Up(numTiles)
  //def tileMemIdxWidth = log2Up(numTiles)
  def tileMemIdxWidth = numTilesPow

  //def tileMemIdxWidth = log2Up(numPxsForAllTiles)
  //def tileMemIdxWidth = log2Up(numTiles)

  //def bgEntryMemIdxWidth = log2Up(numTilesPerBg)
  //def bgAttrsMemIdxWidth = log2Up(numBgs)
  def bgEntryMemIdxWidth = bgSize2dInTiles.x + bgSize2dInTiles.y

  //def palEntryMemIdxWidth = log2Up(numColsInPal)
  def palEntryMemIdxWidth = numColsInPalPow
  //def tilePixelIdxWidth = log2Up(numPxsForAllTiles)
  //--------
  def coordT(
    someSize2d: ElabVec2[Int],
  ) = LcvVgaCtrlMiscIo.coordT(fbSize2d=someSize2d)
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
      x=(1920 / 4).toInt, // 480
      y=(1080 / 4).toInt, // 270
    ),
    //physFbSize2dScale: ElabVec2[Int]=ElabVec2[Int](
    //  //x=3, // 1920 / 4 = 640
    //  //y=3, // 1080 / 3 = 360
    //  x=4, // 1920 / 4 = 480
    //  y=4, // 1080 / 4 = 270
    //),
    physFbSize2dLslScale: ElabVec2[Int]=ElabVec2[Int](
      x=log2Up(4), // 1920 / 4 = 480
      y=log2Up(4), // 1080 / 4 = 270
    ),
    //tileSize2d: ElabVec2[Int]=ElabVec2[Int](x=8, y=8),
    tileSize2dPow: ElabVec2[Int]=ElabVec2[Int](x=log2Up(8), y=log2Up(8)),
    numBgsPow: Int=log2Up(4),
    numObjsPow: Int=log2Up(256),
    //numObjsPerScanline: Int=64,
    numColsInPalPow: Int=log2Up(256),
    //--------
    tileArrRamStyle: String="block",
    bgEntryArrRamStyle: String="block",
    //bgAttrsArrRamStyle: String="block",
    //objAttrsArrRamStyle: String="block",
    palEntryArrRamStyle: String="block",
    lineArrRamStyle: String="block",
    //--------
  ) = {
    def tileSize2d = ElabVec2[Int](
      x=1 << tileSize2dPow.x,
      y=1 << tileSize2dPow.y,
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
      physFbSize2dLslScale=physFbSize2dLslScale,
      tileSize2dPow=tileSize2dPow,
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
      //bgSize2dInTiles=ElabVec2[Int](
      //  x=1 << log2Up(intnlFbSize2d.x / tileSize2d.x),
      //  y=1 << log2Up(intnlFbSize2d.y / tileSize2d.y),
      //),
      bgSize2dInTilesPow=ElabVec2[Int](
        x=log2Up(intnlFbSize2d.x / tileSize2d.x),
        y=log2Up(intnlFbSize2d.y / tileSize2d.y),
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
      tileArrRamStyle=tileArrRamStyle,
      bgEntryArrRamStyle=bgEntryArrRamStyle,
      //bgAttrsArrRamStyle=bgAttrsArrRamStyle,
      //objAttrsArrRamStyle=objAttrsArrRamStyle,
      palEntryArrRamStyle=palEntryArrRamStyle,
      lineArrRamStyle=lineArrRamStyle,
      //--------
    )
  }
}

case class Gpu2dTile(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  //val colIdx = UInt(params.palEntryMemIdxWidth bits)
  def colIdxWidth = params.palEntryMemIdxWidth

  //// the BG priority for for the whole tile
  //val prio = UInt(log2Up(params.numBgs) bits)

  // indices into `Gpu2d.loc.palEntryMem`
  val colIdxV2d = Vec.fill(params.tileSize2d.y)(
    Vec.fill(params.tileSize2d.x)(UInt(colIdxWidth bits))
  )
  //--------
}
case class Gpu2dTileStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  //val rgb = Rgb(params.rgbConfig)
  val tile = Gpu2dTile(params=params)

  // `Mem` index, so in units of pixels
  //val idx = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  //val idx = UInt(16 bits)
  val memIdx = UInt(params.tileMemIdxWidth bits)
  //--------
}

case class Gpu2dBgEntry(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  // The index, in tiles, of the tile represented by this tilemap entry
  val tileIdx = UInt(params.tileMemIdxWidth bits)

  //// The priority for this tilemap entry
  //val prio = UInt(log2Up(params.numBgs) bits)

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
  // The index, in tiles, of the tile represented by this sprite
  val tileIdx = UInt(params.tileMemIdxWidth bits)
  //val pos = params.coordT(fbSize2d=params.bgSize2dInPxs)

  // position within the tilemap, in pixels
  val pos = params.bgPxsCoordT()

  // whether or not to flip x/y 
  val visibFlip = Vec2(dataType=Bool())
  //--------
}

case class Gpu2dObjAttrsStmPayload(
  params: Gpu2dParams,
) extends Bundle {
  //--------
  val objAttrs = Gpu2dObjAttrs(params=params)
  // `Vec` index
  val vecIdx = UInt(params.objAttrsVecIdxWidth bits)
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
  //val tilePush = slave Stream()
  //val tilePush = slave Stream()
  val tilePush = slave Stream(Gpu2dTileStmPayload(params=params))
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
  val objAttrsPush = slave Stream(Gpu2dObjAttrsStmPayload(params=params))
  val palEntryPush = slave Stream(Gpu2dPalEntryStmPayload(params=params))
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
  val tilePush = io.tilePush
  tilePush.ready := True
  val bgEntryPushArr = io.bgEntryPushArr
  val bgAttrsPushArr = io.bgAttrsPushArr
  for (idx <- 0 to params.numBgs - 1) {
    bgEntryPushArr(idx).ready := True
    bgAttrsPushArr(idx).ready := True
  }
  val objAttrsPush = io.objAttrsPush
  objAttrsPush.ready := True
  val palEntryPush = io.palEntryPush
  palEntryPush.ready := True
  //--------
  val pop = io.pop
  val rPopValid = Reg(Bool()) init(False)
  //rPopValid := True
  pop.valid := rPopValid
  //val rOutp = Reg(cloneOf(pop.payload))

  //val nextOutp = cloneOf(pop.payload)
  //val rOutp = RegNext(nextOutp) init(nextOutp.getZero)
  ////rOutp.init(rOutp.getZero)
  //pop.payload := rOutp
  val outp = cloneOf(pop.payload)
  pop.payload := outp


  //pop.valid := True
  //val col = pop.payload.col
  //val physPxPos = pop.payload.physPxPos
  //val intnlPxPos = pop.payload.intnlPxPos
  //val tilePos = pop.payload.tilePos
  //--------
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
    val tileMem = Mem(
      //wordType=UInt(params.palEntryMemIdxWidth bits),
      wordType=Gpu2dTile(params=params),
      wordCount=params.numPxsForAllTiles,
    )
      .initBigInt(Array.fill(params.numPxsForAllTiles)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.tileArrRamStyle)
    when (tilePush.fire) {
      tileMem.write(
        address=tilePush.payload.memIdx,
        data=tilePush.payload.tile,
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
    //val objAttrsMem = Mem(
    //  wordType=Gpu2dObjAttrs(params=params),
    //  wordCount=params.numObjs,
    //)
    //  .initBigInt(Array.fill(params.numObjs)(BigInt(0)).toSeq)
    //  .addAttribute("ram_style", params.objAttrsArrRamStyle)
    //when (objAttrsPush.fire) {
    //  objAttrsMem.write(
    //    address=objAttrsPush.payload.memIdx,
    //    data=objAttrsPush.payload.objAttrs,
    //  )
    //}
    val objAttrsVec = Vec.fill(params.numObjs)(
      Reg(Gpu2dObjAttrs(params=params))
    )
    for (idx <- 0 to objAttrsVec.size - 1) {
      objAttrsVec(idx).init(objAttrsVec(idx).getZero)
    }
    when (objAttrsPush.fire) {
      objAttrsVec(objAttrsPush.payload.vecIdx) := (
        objAttrsPush.payload.objAttrs
      )
    }
    //--------
    val palEntryMem = Mem(
      wordType=Gpu2dPalEntry(params=params),
      wordCount=params.numColsInPal,
    )
      .initBigInt(Array.fill(params.numColsInPal)(BigInt(0)).toSeq)
      .addAttribute("ram_style", params.palEntryArrRamStyle)
    when (palEntryPush.fire) {
      palEntryMem.write(
        address=palEntryPush.payload.memIdx,
        data=palEntryPush.payload.palEntry,
      )
    }
    //--------
    val physCalcPos = LcvVideoCalcPos(
      someSize2d=params.physFbSize2d
    )
    physCalcPos.io.en := pop.fire
    outp.physPosInfo := physCalcPos.io.info
    outp.bgPxsPosSlice := outp.physPosInfo.posSlice(
      thisSize2dPow=ElabVec2[Int](
        x=log2Up(params.physFbSize2d.x),
        y=log2Up(params.physFbSize2d.y),
      ),
      thatSize2dPow=ElabVec2[Int](
        x=log2Up(params.bgSize2dInPxs.x),
        y=log2Up(params.bgSize2dInPxs.y),
      ),
      //thatSomeSize2d=params.bg
    )
    outp.bgTilesPosSlice := outp.physPosInfo.posSlice(
      thisSize2dPow=ElabVec2[Int](
        x=log2Up(params.physFbSize2d.x),
        y=log2Up(params.physFbSize2d.y),
      ),
      thatSize2dPow=ElabVec2[Int](
        x=log2Up(params.bgSize2dInTiles.x),
        y=log2Up(params.bgSize2dInTiles.y),
      ),
      //thatSomeSize2d=params.bg
    )

    //val bgPxsCalcPos = LcvVideoCalcPos(
    //  someSize2d=params.bgSize2dInPxs
    //)
    //val rChangingIntnlPxs = Reg(Bool()) init(False)

    // This should be fine because there's no output from
    // `LcvVideoCalcPos` for the valid/ready transaction 

    //when (pop.fire) {
    //  rChangingIntnlPxs := (
    //    outp.physPosInfo.pos.x(
    //      params.physToBgPxsSliceWidth - 1 downto 0
    //    ).asBits === B(default -> True)
    //  )
    //} otherwise {
    //}
    //bgPxsCalcPos.io.en := (
    //  pop.fire && rChangingIntnlPxs
    //)

    //bgPxsCalcPos.io.en := pop.fire && outp.physPosInfo
    //val bgTilesCalcPos = LcvVideoCalcPos(
    //  someSize2d=params.bgSize2dInTiles
    //)
    //outp.bgTilesPosInfo := bgTilesCalcPos.io.info

    val lineMemArr = new ArrayBuffer[Mem[Rgb]]()
    for (idx <- 0 to params.numLineMems - 1) {
      lineMemArr += Mem(
        wordType=Rgb(params.rgbConfig),
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
    //val nextPhysPosCnt = params.physPxCoordT()
    //val rPhysPosCnt = RegNext(nextPhysPosCnt) init(nextPhysPosCnt.getZero)

    //val rPhysPosCntPlus1Overflow = Reg(Vec2(Bool()))
    //rPhysPosCntPlus1Overflow.init(rPhysPosCntPlus1Overflow.getZero)
    ////val physPosCntPlus1OverflowAsBits = rPhysPosCntPlus1Overflow.asBits
    //when (pop.fire) {
    //  //rPhysPosCntPlus1Overflow := rPhysPosCnt.x === params.physFbSize2d.x - 2
    //  switch (rPhysPosCntPlus1Overflow.asBits.reversed) {
    //    is (M"-0") {
    //    }
    //    is (B"01") {
    //    }
    //    is (B"11") {
    //    }
    //    default {
    //    }
    //  }
    //  //when (rPhysPosCnt.x + 2 === params.physFbSize2d.x) {
    //  //} otherwise {
    //  //}
    //}

    //--------
  }
  //--------
  // Handle backgrounds
  //--------
}
