package libcheesevoyage.general

import spinal.core._

case class DualTypeVec2[
  Tx <: Data,
  Ty <: Data,
](
  dataTypeX: HardType[Tx],
  dataTypeY: HardType[Ty],
) extends Bundle {
  val x = dataTypeX()
  val y = dataTypeY()
}
//case class Vec2[
//  T <: Data
//](
//  dataType: HardType[T],
//) extends Bundle {
//  val x = dataType()
//  val y = dataType()
//}
case class Vec2[
  T <: Data
](
  dataType: HardType[T],
) extends Bundle{
  val x = dataType()
  val y = dataType()
}
case class ElabDualTypeVec2[
  Tx,
  Ty,
](
  x: Tx,
  y: Ty,
)
case class ElabVec2[
  T
](
  x: T,
  y: T,
)
