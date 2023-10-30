//package libcheesevoyage.general
//
//import spinal.core._
//import spinal.lib._
//import scala.collection.mutable.ArrayBuffer
//
//trait IHostDevice extends IMasterSlave {
//  final def asMaster(): Unit = asHost()
//  def asHost(): Unit
//}
//
//object host {
//  //def apply[T <: IMasterSlave](i: T*) = master(i)
//  //def apply[T <: IMasterSlave](i: T) = master(i)
//  //def applyIt[T <: IMasterSlave](i: T) = master(i)
//  //def apply[T <: IMasterSlave](i: T*) = {
//  //}
//  //def apply[T <: IMasterSlave](i: T*) = master(i:_*)
//  def applyIt[T <: IMasterSlave](i: T*) = {
//    val arr = new ArrayBuffer[T]()
//    for (idx <- 0 until i.length) {
//      arr += master(i(idx))
//    }
//    //arr.toSeq
//    arr
//  }
//
//  //def apply[T <: IHostDevice](i: T*) = {
//  //  val arr = new ArrayBuffer[T]()
//  //  for (idx <- 0 until i.length) {
//  //    arr += master(i(idx))
//  //  }
//  //  arr.toSeq
//  //}
//}
//object device {
//  //def apply[T <: IMasterSlave](i: T) = slave(i)
//  //def apply[T <: IMasterSlave](i: T*) = slave(i:_*)
//  def applyIt[T <: IHostDevice](i: T*) = {
//    val arr = new ArrayBuffer[T]()
//    for (idx <- 0 until i.length) {
//      arr += slave(i(idx))
//    }
//    //arr.toSeq
//    arr
//  }
//}
