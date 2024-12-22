package libcheesevoyage.general
import spinal.core._
import spinal.lib._

case class VecIntfElem[
  DataT <: Data
](
  //_dataType: => DataT,
  dataType: () => DataT,
  generics: Option[(VecIntfElem[DataT]) => String]=None,
) extends Interface with DataCarrier[DataT] {
  val v = dataType()
  def payload: DataT = v
  def fire: Bool = False
  def valid: Bool = False
  def freeRun(): this.type = this
  generics match {
    case Some(myGenerics) => {
      myGenerics(this)
    }
    case None =>
  }
  //def :=(that: DataT) = {
  //  v := that
  //}
  //def apply(
  //  index: UInt
  //) = {
  //  v match {
  //    case v: Vec[_] => v(index)
  //    //case u: BitVector => u(index)
  //    case _ => {
  //      assert(false)
  //      null
  //    }
  //  }
  //}
  //def apply(
  //  index: Int
  //) = {
  //  v match {
  //    case v: Vec[_] => v(index)
  //    case _ => {
  //      assert(false)
  //      null
  //    }
  //  }
  //}
  //def apply(
  //  index: Int
  //) = {
  //  v match {
  //    case v: Vec[_] => v(index)
  //    case u: BitVector => u(index)
  //    case _ => {
  //      assert(false)
  //      null
  //    }
  //  }
  //}
  //def apply(
  //  indexEtc: Range
  //) = {
  //  v(indexEtc)
  //}
  //def size: Int = {
  //  v match {
  //    case v: Vec[_] => v.size
  //    case _ => -1
  //  }
  //}
  //def getWidth: Int = {
  //  v match {
  //    case v: BitVector => v.getWidth
  //    case _ => -1
  //  }
  //}
}
object VecIntf {
  def fill[T <: Data](_size: Int)(_dataType: () => T): Vec[
    VecIntfElem[T]
    //T
  ] = {
    Vec.fill(_size)(
      new VecIntfElem[T](_dataType)
      //_dataType
      
    )
  }
}
//case class VecIntf[
//  DataT <: Data
//](
//  dataType: HardType[DataT],
//  val size: Int,
//) extends Interface {
//  assert(size > 0)
//  val v = Vec.fill(size)(dataType())
//  //def size = v.size
//  def apply(
//    index: UInt
//  ) = {
//    v(index)
//  }
//  def apply(
//    index: Int
//  ) = {
//    v(index)
//  }
//}
