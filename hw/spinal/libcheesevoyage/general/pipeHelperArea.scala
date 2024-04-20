package libcheesevoyage.general

import spinal.core._
import spinal.lib._
import spinal.lib.misc.pipeline._
import scala.collection.mutable.ArrayBuffer

object PipeHelper {
  def mkLinkArr() = new ArrayBuffer[Link]()
}
case class PipeHelper(
  var linkArr: ArrayBuffer[Link]
) extends Area {
  //val linkArr = new ArrayBuffer[Link]()
  val nArr = new ArrayBuffer[Node]()
  val cArr = new ArrayBuffer[CtrlLink]()
  val sArr = new ArrayBuffer[StageLink]()
  val s2mArr = new ArrayBuffer[S2MLink]()

  def addStage(
    //cLast: Option[CtrlLink],
    //down: CtrlLink,
    //isLast: Boolean=false,
    //linkArr: ArrayBuffer[Link]
    name: String,
  ): CtrlLink = {
    //val isFirst: Boolean = (sArr.size == 0)
    //nArr += Node()

    //if (nArr.size == 0) {
    //  nArr += Node().setName("nArr_0")
    //}
    //val tempSArrSize = sArr.size
    //sArr += StageLink(
    //  //up=(
    //  //  if (tempSArrSize == 0) {
    //  //    Node()
    //  //  } else {
    //  //    cArr.last.down
    //  //  }
    //  //),
    //  up=nArr.last,
    //  down=Node()
    //).setName(f"s$name")
    ////if (tempSArrSize == 0) {
    ////  linkArr += sArr(0).up
    ////}
    //linkArr += sArr.last
    //s2mArr += S2MLink(
    //  up=sArr.last.down,
    //  down=Node(),
    //).setName(f"s2m$name")
    //linkArr += s2mArr.last
    //nArr += Node().setName(f"n$name")
    //cArr += CtrlLink(
    //  up=s2mArr.last.down,
    //  down=nArr.last,
    //).setName(f"c$name")
    //linkArr += cArr.last
    ////if (nArr.size == 2) {
    ////  cArr(0)
    ////} else {
    ////  cArr.last
    ////}
    //cArr.last
    ////last

    if (nArr.size == 0) {
      nArr += Node().setName(f"n$name")
    } else {
      nArr += last.down.setName(f"n$name")
    }
    cArr += CtrlLink(
      up={
        nArr.last
        //if (cArr.size == 0) {
        //  Node().setName(f"n$name")
        //} else {
        //  s2mArr.last.down
        //  //last.down//.setName(f"n$name")
        //}
      },
      down=Node()
    ).setName(f"c$name")
    linkArr += cArr.last

    sArr += StageLink(
      up=cArr.last.down,
      down=Node(),
    ).setName(f"s$name")
    linkArr += sArr.last

    s2mArr += S2MLink(
      up=sArr.last.down,
      down=Node(),
    ).setName(f"s2m$name")
    linkArr += s2mArr.last

    cArr.last
  }
  def first = cArr(0)
  def last = s2mArr.last
  //def first = sArr(0)
  //def last = cArr.last
  //def doBuilder(): Unit = {
  //  Builder(linkArr.toSeq)
  //}
}
