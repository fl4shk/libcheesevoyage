package libcheesevoyage.gfx
import libcheesevoyage.general.FifoMiscIo
import libcheesevoyage.general.AsyncReadFifo
import libcheesevoyage.general.FifoIo
import libcheesevoyage.general.Vec2
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

case class LcvGpu2dParams(
  //vgaTimingInfo: LcvVgaTimingInfo,
  rgbConfig: RgbConfig,       // Bits per RGB channel
                              // (from the GPU's perspective. This may
                              // differ from the physical video signal if
                              // `LcvVideoDitherer` is used.)
  internalFbSize2d: ElabVec2[Int],  // size of the internal resolution used 
                              // by the 2D GPU
                              // (in PIXELS, NOT tiles, though it is
                              // perhaps a good idea to ensure `fbSize2d`
                              // is integer multiples of `numTiles`, as
                              // that is how I'm used to 2D GPUs working)
  physFbSize2dMult: ElabVec2[Int],  // the integer multiple of 
                              // `internalFbSize2d` to obtain the physical
                              // resolution of the video signal.
                              // This is used to duplicate generated pixels 
                              // so that the generated video signal can 
                              // fill the screen.
  tileSize2d: ElabVec2[Int],  // width/height of a tile (in pixels)
  numTiles: Int,              // total number of tiles
                              // (how much memory to reserve for tiles)
  bgSizeInTiles2d: ElabVec2[Int],    // width/height of a background
                              // (in number of tiles)
  numBgs: Int,                // number of backgrounds
  numObjs: Int,               // number of sprites
  numObjsPerScanline: Int,    // how many sprites to process in one
                              // scanline
) {
  //--------
  def numPixelsPerTile = tileSize2d.x * tileSize2d.y
  def numPixelsForAllTiles = numTiles * numPixelsPerTile
  //--------
  def numPixelsPerBg = (
    bgSizeInTiles2d.x * bgSizeInTiles2d.y * numPixelsPerTile
  )
  def numPixelsForAllBgs = numBgs * numPixelsPerBg
  //--------
  // widths of indexes into the internal `Mem`s that contain
  def objMemIdxWidth = log2Up(numObjs)
  def bgMemIdxWidth = log2Up(numBgs)
  def tileIdxWidth = log2Up(numTiles)
  //def tilePixelIdxWidth = log2Up(numPixelsForAllTiles)
  //--------
}

//case class LcvGpu2dTile(
//  //rgbConfig: RgbConfig,
//  ////fbSize2d: ElabVec2[Int],
//  //tileSize2d: ElabVec2[Int],
//  params: LcvGpu2dParams,
//) extends Bundle {
//  //--------
//  //--------
//}
//case class LcvGpu2dTilemap(
//  //rgbConfig: RgbConfig,
//  //tileSize2d: ElabVec2[Int],
//  params: LcvGpu2dParams,
//) extends Bundle {
//  //--------
//  //--------
//}

//case class LcvGpu2dSprite(
//  //rgbConfig: RgbConfig ,
//  //fbSize2d: ElabVec2[Int],
//  params: LcvGpu2dParams,
//) extends Bundle {
//  //--------
//  //--------
//}

case class LcvGpu2dTilePushPayload(
  params: LcvGpu2dParams,
) extends Bundle {
  //--------
  val rgb = Rgb(params.rgbConfig)

  // `Mem` index
  //val idx = Vec2(LcvVgaCtrlMiscIo.coordElemT())
  //val idx = UInt(16 bits)
  val idx = UInt(params.tileIdxWidth bits)
  //--------
}
case class LcvGpu2dObjPushPayload(
  params: LcvGpu2dParams,
) extends Bundle {
  //--------
  //--------
}

case class LcvGpu2dIo(
  params: LcvGpu2dParams,
) extends Bundle {
  //--------
  //val tilePush = slave Stream()
  //val tilePush = slave Stream()
  val tilePush = slave Stream(LcvGpu2dTilePushPayload(params=params))
  //val tilePush = slave Stream(LcvGpu2dTilePushPayload(params=params))
  //--------
}
case class LcvGpu2d(
  params: LcvGpu2dParams,
) extends Component {
  //--------
  val io = LcvGpu2dIo(params=params)
  //--------
}
