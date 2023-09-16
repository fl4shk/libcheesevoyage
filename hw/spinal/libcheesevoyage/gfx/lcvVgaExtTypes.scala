package libcheesevoyage.gfx

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
      log2Up(visib),
      log2Up(front),
      log2Up(sync),
      log2Up(back),
    ) 
    val tmp = tmpList.zipWithIndex.maxBy(_._1)._2
    println(f"cntWidth(): $tmp")
    //return tmpList()
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
}
