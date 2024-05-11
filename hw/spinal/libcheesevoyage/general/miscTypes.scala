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
object MkVec2 {
  //implicit def implFactory[T <: Data ](
  //  dataType: HardType[T],
  //  //x: T,
  //  //y: T,
  //): Vec2[T] = new Vec2[T](dataType=dataType())
  //{
  //  val ret = new Vec2[T](dataType=dataType())
  //  ////ret.x := x
  //  ////ret.y := y
  //  ////return ret
  //  //ret.x := x
  //  //ret.y := y
  //  ret
  //}
  //def apply[T <: Data](
  //  dataType: HardType[T],
  //): Vec2[T] = new Vec2[T](dataType=dataType())

  //def craft
  def apply[T <: Data](
    dataType: HardType[T],
    //x: HardType[T],
    //y: HardType[T],
    x: T,
    y: T,
  ) = {
    //val ret = new NumVec2[T](dataType=dataType())
    val ret = Vec2[T](dataType=dataType())
    ret.x := x
    ret.y := y
    ret
  }
}
case class Vec2[
  T <: Data
](
  dataType: HardType[T],
) extends Bundle {
  val x = dataType()
  val y = dataType()
}
object MkDualTypeNumVec2 {
  //implicit def implFactory[
  //  Tx <: Data with Num[Tx],
  //  Ty <: Data with Num[Ty],
  //](
  //  dataTypeX: HardType[Tx],
  //  dataTypeY: HardType[Ty],
  //  //x: T,
  //  //y: T,
  //): DualTypeNumVec2[Tx, Ty] = {
  //  //val ret = new DualTypeNumVec2[Tx, Ty](dataType=dataType())
  //  //ret.x := x
  //  //ret.y := y
  //  //ret
  //  return new DualTypeNumVec2[Tx, Ty](
  //    dataTypeX=dataTypeX(),
  //    dataTypeY=dataTypeY(),
  //  )
  //}
  //def apply[
  //  Tx <: Data with Num[Tx],
  //  Ty <: Data with Num[Ty],
  //](
  //  dataTypeX: HardType[Tx],
  //  dataTypeY: HardType[Ty],
  //) = (
  //  new DualTypeNumVec2[Tx, Ty](
  //    dataTypeX=dataTypeX(),
  //    dataTypeY=dataTypeY(),
  //  )
  //)
  def apply[
    Tx <: Data with Num[Tx],
    Ty <: Data with Num[Ty],
  ](
    dataTypeX: HardType[Tx],
    dataTypeY: HardType[Ty],
    //x: HardType[T],
    //y: HardType[T],
    x: Tx,
    y: Ty,
  ) = {
    //val ret = new DualTypeNumVec2[Tx, Ty](dataType=dataType())
    val ret = DualTypeNumVec2[Tx, Ty](
      dataTypeX=dataTypeX(),
      dataTypeY=dataTypeY(),
    )
    ret.x := x
    ret.y := y
    ret
  }
}
//case class DualTypeNumVec2PowCnt[
//  Tx <: Data with Num[Tx],
//  Ty <: Data with Num[Ty],
//](
//  dataTypeX: HardType[Tx],
//  dataTypeY: HardType[Ty],
//) extends Bundle {
//  val x = dataTypeX()
//  val y = dataTypeY()
//}
case class DualTypeNumVec2[
  Tx <: Data with Num[Tx],
  Ty <: Data with Num[Ty],
](
  dataTypeX: HardType[Tx],
  dataTypeY: HardType[Ty],
) extends Bundle {
  val x = dataTypeX()
  val y = dataTypeY()

  //def magSquared() = x * y
  def magSquared(
    castType: HardType[UInt],
  ) = {
    val tempX = castType()
    val tempY = castType()
    tempX := x.asBits.asUInt.resized
    tempY := y.asBits.asUInt.resized
    tempX * tempY
  }
  def magSquared(
    castType: HardType[SInt],
  ) = {
    val tempX = castType()
    val tempY = castType()
    tempX := x.asBits.asSInt.resized
    tempY := y.asBits.asSInt.resized
    tempX * tempY
  }
  //def magSquared[
  //  T <: SInt
  //](
  //) = {
  //  //val tempX = x.asUInt
  //  //val tempY = y.asUInt
  //  //tempX := x
  //  //tempY := y
  //  //tempX * tempY
  //  x.asBits.asSInt * y.asBits.asSInt
  //  //tempY := y
  //  //x * tempY
  //}
  //def magSquaredTy() = {
  //  val tempX = dataTypeY()
  //  tempX := x
  //  tempX * y
  //}


  def +(that: DualTypeNumVec2[Tx, Ty]) = MkDualTypeNumVec2[Tx, Ty](
    dataTypeX=dataTypeX(),
    dataTypeY=dataTypeY(),
    x=this.x + that.x,
    y=this.y + that.y,
  )
  def -(that: DualTypeNumVec2[Tx, Ty]) = MkDualTypeNumVec2[Tx, Ty](
    dataTypeX=dataTypeX(),
    dataTypeY=dataTypeY(),
    x=this.x - that.x,
    y=this.y - that.y,
  )
  def *(thatX: Tx, thatY: Ty) = MkDualTypeNumVec2[Tx, Ty](
    dataTypeX=dataTypeX(),
    dataTypeY=dataTypeY(),
    x=this.x * thatX,
    y=this.y * thatY,
  )
  def /(thatX: Tx, thatY: Ty) = MkDualTypeNumVec2[Tx, Ty](
    dataTypeX=dataTypeX(),
    dataTypeY=dataTypeY(),
    x=this.x / thatX,
    y=this.y / thatY,
  )
  def <(that: DualTypeNumVec2[Tx, Ty]): Bool = {
    val concat = Bits(2 bits)
    val ret = Bool()
    concat(1) := this.y < that.y
    concat(0) := this.x < that.x
    ret := concat === M"11"
    ret
  }
  def >(that: DualTypeNumVec2[Tx, Ty]): Bool = {
    val concat = Bits(2 bits)
    val ret = Bool()
    concat(1) := this.y > that.y
    concat(0) := this.x > that.x
    ret := concat === M"11"
    ret
  }
  def <=(that: DualTypeNumVec2[Tx, Ty]): Bool = {
    //val concat = Bits(2 bits)
    //val ret = Bool()
    //concat(1) := this.y <= that.y
    //concat(0) := this.x <= that.x
    //ret := concat === M"11"
    //ret
    return (this < that || this === that)
  }
  def >=(that: DualTypeNumVec2[Tx, Ty]): Bool = {
    //val concat = Bits(2 bits)
    //val ret = Bool()
    //concat(1) := this.y >= that.y
    //concat(0) := this.x >= that.x
    //ret := concat === M"11"
    //ret
    return (this > that || this === that)
  }
}
//trait HasMathForVec2[T <: Data] extends Data {
//  def +(that: T): T
//  def -(that: T): T
//  def *(that: T): T
//  def /(that: T): T
//  def <(that: T): Bool
//  def >(that: T): Bool
//  def <=(that: T): Bool
//  def >=(that: T): Bool
//}
object MkNumVec2 {
  //implicit def implFactory[
  //  T <: Data with Num[T]
  //](
  //  dataType: HardType[T],
  //  //x: T,
  //  //y: T,
  //): NumVec2[T] = {
  //  //val ret = new NumVec2[T](dataType=dataType())
  //  //ret.x := x
  //  //ret.y := y
  //  //ret
  //  return new NumVec2[T](dataType=dataType)
  //}
  //def apply[T <: Data with Num[T]](dataType: HardType[T]) = (
  //  new NumVec2[T](dataType=dataType)
  //)
  def apply[T <: Data with Num[T]](
    dataType: HardType[T],
    //x: HardType[T],
    //y: HardType[T],
    x: T,
    y: T,
  ) = {
    //val ret = new NumVec2[T](dataType=dataType())
    val ret = NumVec2[T](dataType=dataType())
    ret.x := x
    ret.y := y
    ret
  }
}
case class NumVec2[
  T <: Data with Num[T]
](
  dataType: HardType[T],
) extends Bundle {
  val x = dataType()
  val y = dataType()

  //def magSquared() = x * y

  def +(that: NumVec2[T]) = MkNumVec2[T](
    dataType=dataType(),
    x=this.x + that.x,
    y=this.y + that.y,
  )
  def -(that: NumVec2[T]) = MkNumVec2[T](
    dataType=dataType(),
    x=this.x - that.x,
    y=this.y - that.y,
  )
  def *(that: T) = MkNumVec2[T](
    dataType=dataType(),
    x=this.x * that,
    y=this.y * that,
  )
  def /(that: T) = MkNumVec2[T](
    dataType=dataType(),
    x=this.x / that,
    y=this.y / that,
  )
  def <(that: NumVec2[T]): Bool = {
    val concat = Bits(2 bits)
    val ret = Bool()
    concat(1) := this.y < that.y
    concat(0) := this.x < that.x
    ret := concat === M"11"
    ret
  }
  def >(that: NumVec2[T]): Bool = {
    val concat = Bits(2 bits)
    val ret = Bool()
    concat(1) := this.y > that.y
    concat(0) := this.x > that.x
    ret := concat === M"11"
    ret
  }
  def <=(that: NumVec2[T]): Bool = {
    //val concat = Bits(2 bits)
    //val ret = Bool()
    //concat(1) := this.y <= that.y
    //concat(0) := this.x <= that.x
    //ret := concat === M"11"
    //ret
    return (this < that || this === that)
  }
  def >=(that: NumVec2[T]): Bool = {
    //val concat = Bits(2 bits)
    //val ret = Bool()
    //concat(1) := this.y >= that.y
    //concat(0) := this.x >= that.x
    //ret := concat === M"11"
    //ret
    return (this > that || this === that)
  }
}
case class ElabDualTypeVec2[
  Tx,
  Ty,
](
  x: Tx,
  y: Ty,
)
//object ElabVec2 {
//  def magSquared/*[
//    T <: Int
//  ]*/(
//    someElabVec2: ElabVec2[Int]
//  ) = someElabVec2.x * someElabVec2.y
//}
case class ElabVec2[
  T
](
  x: T,
  y: T,
) {
  //def magSquared() = x * y
}
