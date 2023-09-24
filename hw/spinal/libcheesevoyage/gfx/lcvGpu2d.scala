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

case class LcvGpu2dTile(
  rgbConfig: RgbConfig,
  //fbSize2d: ElabVec2[Int],
  tileSize2d: ElabVec2[Int],
) extends Bundle {
  //--------
  //--------
}
case class LcvGpu2dTilemap(
  rgbConfig: RgbConfig,
  tileSize2d: ElabVec2[Int],
) extends Bundle {
  //--------
  //--------
}

case class LcvGpu2dSprite(
  rgbConfig: RgbConfig ,
  //fbSize2d: ElabVec2[Int],
) extends Bundle {
  //--------
  //--------
}

case class LcvGpu2dIo(
  //vgaTimingInfo: LcvVgaTimingInfo,
  rgbConfig: RgbConfig,
  fbSize2d: ElabVec2[Int],
  numSprites: Int,
  numBackgrounds: Int,
) extends Bundle {
  //--------
  //--------
}
