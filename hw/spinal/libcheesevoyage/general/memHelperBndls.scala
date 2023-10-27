package libcheesevoyage.general

import spinal.core._
import spinal.lib._

case class MultiMemReadSync[
  T <: Data
](
  someMem: Mem[T],
  //dataType: HardType[T],
  //addrWidth: Int,
  numReaders: Int=1,
) extends Bundle {
  def dataType() = someMem.wordType()
  def addrWidth = log2Up(someMem.wordCount)
  val dataVec = Vec.fill(numReaders)(dataType())
  val addrVec = Vec.fill(numReaders)(UInt(addrWidth bits))

  def readSync(
    //addr: UInt,
    idx: Int,
  ): T = {
    //addrVec(idx) := addr
    dataVec(idx) := someMem.readSync(
      address=addrVec(idx)
    )
    dataVec(idx)
  }
}
