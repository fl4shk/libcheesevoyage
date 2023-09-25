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
  rgbConfig: RgbConfig,
  numSprites: Int,
  numBackgrounds: Int,
  numTiles: Int,
  bgSize2d: ElabVec2[Int],
  fbSize2d: ElabVec2[Int],
  tileSize2d: ElabVec2[Int],
  numSpritesPerScanline: ElabVec2[Int],
) {
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
  val idx = UInt(16 bits)
  //--------
}

case class LcvGpu2dIo(
  params: LcvGpu2dParams,
) extends Bundle {
  //--------
  //val tilePush = slave Stream()
  //val tilePush = slave Stream()
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
