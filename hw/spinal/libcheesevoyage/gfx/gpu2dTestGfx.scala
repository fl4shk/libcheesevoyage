package libcheesevoyage.gfx

import libcheesevoyage.general.ElabVec2

import scala.collection.mutable.ArrayBuffer
import java.io.{FileInputStream, BufferedInputStream}
//import scala.io.Source


object Gpu2dTestGfx {
  //val palette = Array[Int](
  //  0xE0,0x7F,0x00,0x00,0x47,0x10,0x4E,0x14,0x96,0x14,0xFB,0x10,0xBF,0x05,0x9F,0x0E,
  //  0x5F,0x23,0xFF,0x03,0xDA,0x33,0x73,0x23,0x0B,0x1B,0x82,0x16,0xE3,0x1D,0x44,0x1D,
  //  0x82,0x10,0xC2,0x30,0x65,0x61,0x64,0x6E,0x44,0x63,0xF4,0x6F,0xFF,0x7F,0xDF,0x63,
  //  0x5F,0x5F,0x9E,0x4A,0xBD,0x39,0x37,0x4D,0xEF,0x40,0xC8,0x28,0x84,0x18,0x64,0x0C,
  //  0xA6,0x14,0x0E,0x1D,0xD7,0x21,0x9B,0x32,0x5E,0x4F,0x9B,0x77,0xF6,0x6A,0x51,0x56,
  //  0xCD,0x45,0x49,0x31,0xE6,0x20,0x88,0x18,0xCB,0x1C,0x51,0x29,0xD7,0x35,0xDD,0x52,
  //  0x9C,0x7F,0xF7,0x7E,0x70,0x72,0x2B,0x5E,0xE8,0x41,0x84,0x25,0x06,0x32,0xAB,0x46,
  //  0x72,0x5F,0xD9,0x73,0x5C,0x57,0xD8,0x46,0x14,0x32,0x8F,0x29,0x2B,0x21,0xE8,0x18,
  //)
  def doConvert(
    filename: String,
    //optPack: Boolean=false,
    optMap: Boolean=false,
  ) = {
    var tempArr = new ArrayBuffer[Short]()
    val bis = new BufferedInputStream(new FileInputStream(filename))
    var idx: Int = 0
    var prevB: Int = 0
    Iterator.continually(bis.read())
      .takeWhile(_ != -1)
      .foreach(
        b => {
          //tempArr += b.toInt
          //if (optPack) {
          //  if ((idx + 1) % 2 == 1) {
          //    //tempArr last = tempArr.last << 8
          //    tempArr += ((prevB << 8) | b.toInt).toShort
          //    //--------
          //    // little endian
          //    //tempArr += ((b.toInt << 8) | prevB).toShort
          //    //--------
          //  } 
          //  //else {
          //  //  //tempArr += b.toShort
          //  //}
          //  idx = (idx + 1) % 2
          //  prevB = b.toInt
          //} else {
          //  tempArr += b.toShort
          //}
          if (optMap) {
            if ((idx + 1) % 2 == 1) {
              ////tempArr += prevB.toShort
              ////tempArr += b.toShort
              //tempArr += b.toShort
              tempArr += (
                (prevB << 8) | b.toInt
                //(b.toInt << 8) | prevB
              ).toShort
            } 
            //tempArr += b.toShort
            //tempArr += prevB.toShort
            //else {
            //}
            idx = (idx + 1) % 2
            prevB = b.toInt
          } else {
            tempArr += b.toShort
          }
          //tempArr += (b.toInt & 0xff)
        }
      )
    bis.close
    tempArr
  }
  val palette = doConvert(
    filename="gfx/bmp/master.pal"
  )
  //val fgCommonTileArr = {
  //  val tempArr = new ArrayBuffer[Int]()
  //  //val file = new RandomAccessFile("gfx/foreground_common_gfx.raw", "r")
  //  //val buffer = java.io.file.Files.readAllBytes("gfx/foreground_common_gfx.raw")
  //  val filename = "gfx/bmp/foreground_common_gfx.raw"
  //  val bis = new BufferedInputStream(new FileInputStream(filename))
  //  Iterator.continually(bis.read())
  //    .takeWhile(_ != -1)
  //    .foreach(
  //      b => {
  //        //if (b.toInt > 0) {
  //        //  println(s"test: ${b.toInt}")
  //        //}
  //        tempArr += b.toInt
  //      }
  //    )
  //  bis.close
  //  tempArr
  //}
  val sampleBgMapSize2d = ElabVec2[Int](
    x=320 / 16,
    y=240 / 16,
  )
  val sampleBgMapArr = doConvert(
    filename="gfx/bmp/sample_background.map",
    //optPack=true,
    optMap=(
      //false
      true
    ),
  )
  val sampleBgTileArr = doConvert(
    filename="gfx/bmp/sample_background.raw"
  )
  //--------
  val sampleFgMapSize2d = ElabVec2[Int](
    x=320 / 16,
    y=240 / 16,
  )
  val sampleFgMapArr = doConvert(
    filename="gfx/bmp/sample_foreground.map",
    //optPack=true,
    optMap=(
      //false
      true
    ),
  )
  val sampleFgTileArr = doConvert(
    filename="gfx/bmp/sample_foreground.raw"
  )
  //--------
  val sampleFg2MapSize2d = ElabVec2[Int](
    x=320 / 16,
    y=240 / 16,
  )
  val sampleFg2MapArr = doConvert(
    filename="gfx/bmp/sample_foreground_2.map",
    //optPack=true,
    optMap=(
      //false
      true
    ),
  )
  val sampleFg2TileArr = doConvert(
    filename="gfx/bmp/sample_foreground_2.raw"
  )
  //--------
  val fgCommonTileArr = doConvert(
    filename="gfx/bmp/foreground_common_gfx.raw"
  )
  val dbgBlankingFgCommonTileArr = doConvert(
    filename="gfx/bmp/dbg_blanking_foreground_common_gfx.raw"
  )
  val fgGrasslandTileArr = doConvert(
    filename="gfx/bmp/foreground_grassland_gfx.raw"
  )

}
