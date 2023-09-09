package libcheesevoyage.general

import spinal.core._

case class Vec2[
  T <: Data
](
  dataType: HardType[T],
) extends Bundle {
  val x = dataType()
  val y = dataType()
}
case class ElabVec2[
  T
](
  x: T,
  y: T,
)
