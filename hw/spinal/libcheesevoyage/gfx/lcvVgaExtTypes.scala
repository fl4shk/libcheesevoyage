package libcheesevoyage.gfx
import libcheesevoyage.general.ElabVec2

import spinal.core._
import spinal.lib._

case class LcvVgaTimingHv(
  visib: Int,
  front: Int,
  sync: Int,
  back: Int,
) {
  //--------
  // This is specifically the minimum width instead of like, 32-bit or
  // something
  def cntWidth(): Int = {
    val tmpList = List[Int](
      //log2Up(visib),
      //log2Up(front),
      //log2Up(sync),
      //log2Up(back),
      BigInt(visib).bitLength,
      BigInt(front).bitLength,
      BigInt(sync).bitLength,
      BigInt(back).bitLength,
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
  pixelClk: Double,
  htiming: LcvVgaTimingHv,
  vtiming: LcvVgaTimingHv,
) {
  def fbSize2d: ElabVec2[Int] = ElabVec2(x=htiming.visib, y=vtiming.visib)
}
