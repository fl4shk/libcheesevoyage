package libcheesevoyage.gfx
import libcheesevoyage.general.ElabVec2

import spinal.core._
import spinal.lib._
import spinal.lib.graphic.vga._

case class LcvVgaTimingHv(
  visib: Int,
  front: Int,
  sync: Int,
  back: Int,
) {
  //--------
  // This is specifically the minimum width instead of like, 32-bit or
  // something
  def asList(): List[Int] = {
    return List[Int](
      visib,
      front,
      sync,
      back,
    )
  }
  def cntWidth(
    offs: Int=0
  ): Int = {
    //val tmpList = List[Int](
    //  //log2Up(visib),
    //  //log2Up(front),
    //  //log2Up(sync),
    //  //log2Up(back),
    //  BigInt(visib).bitLength,
    //  BigInt(front).bitLength,
    //  BigInt(sync).bitLength,
    //  BigInt(back).bitLength,
    //)
    //val tmpIdx = tmpList.zipWithIndex.maxBy(_._1)._2
    //val tmp = tmpList(tmpIdx)
    ////println(f"cntWidth(): $tmp, $tmpIdx")
    ////return tmpList()
    ////return tmpList(tmpIdx)
    //return tmp
    return cntPlusOffsWidth(offs=offs)
  }
  def cntPlusOffsWidth(
    offs: Int,
  ): Int = {
    val tmpList = List[Int](
      //log2Up(visib),
      //log2Up(front),
      //log2Up(sync),
      //log2Up(back),
      BigInt(visib + offs).bitLength,
      BigInt(front + offs).bitLength,
      BigInt(sync + offs).bitLength,
      BigInt(back + offs).bitLength,
    )
    val tmpIdx = tmpList.zipWithIndex.maxBy(_._1)._2
    val tmp = tmpList(tmpIdx)
    //println(f"cntWidth(): $tmp, $tmpIdx")
    //return tmpList()
    //return tmpList(tmpIdx)
    return tmp
  }
  //--------

  //--------
}
case class LcvVgaTimingInfo(
  pixelClk: HertzNumber,
  htiming: LcvVgaTimingHv,
  vtiming: LcvVgaTimingHv,
) {
  def fbSize2d: ElabVec2[Int] = ElabVec2(x=htiming.visib, y=vtiming.visib)
  //def spinalVgaTimings: VgaTimings(
  //)
  def driveSpinalVgaTimings(
    clkRate: HertzNumber,
    spinalVgaTimings: VgaTimings,
  ): Unit = {
    spinalVgaTimings.setAs(
      hPixels=htiming.visib,// * (clkRate / pixelClk).toInt,
      hSync=htiming.sync,// * (clkRate / pixelClk).toInt,
      hFront=htiming.front,// * (clkRate / pixelClk).toInt,
      hBack=htiming.back,// * (clkRate / pixelClk).toInt,
      hPolarity=false,
      vPixels=vtiming.visib,// * (clkRate / pixelClk).toInt,
      vSync=vtiming.sync,// * (clkRate / pixelClk).toInt,
      vFront=vtiming.front,// * (clkRate / pixelClk).toInt,
      vBack=vtiming.back,// * (clkRate / pixelClk).toInt,
      vPolarity=false,
    )
  }
}
