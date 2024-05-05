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
  private var didFinish: Boolean = false

  def addStage(
    //cLast: Option[CtrlLink],
    //down: CtrlLink,
    //isLast: Boolean=false,
    //linkArr: ArrayBuffer[Link]
    name: String,
    finish: Boolean=false,
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
    assert(!didFinish)
    didFinish = finish

    def doFinish(
      nName: String
    ): Unit = {
      nArr += s2mArr.last.down.setName(f"n$nName")
      cArr += CtrlLink(
        up={
          nArr.last
        },
        down=Node()
      )
      linkArr += cArr.last
    }

    if (finish && nArr.size > 0) {
      doFinish(nName=name)
    } else { // if (!(finish && nArr.size > 0))
      if (nArr.size == 0) {
        nArr += Node().setName(f"n$name")
      } else {
        nArr += s2mArr.last.down.setName(f"n$name")
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
        down={
          val myDown = Node()
          myDown.setName(f"n$name" + "_down")
          myDown
        }
      )//.setName(f"c$name")
      linkArr += cArr.last

      sArr += StageLink(
        up=cArr.last.down,
        down=Node(),
      )//.setName(f"s$name")
      linkArr += sArr.last

      s2mArr += S2MLink(
        up=sArr.last.down,
        down=Node(),
      )//.setName(f"s2m$name")
      linkArr += s2mArr.last

      if (finish) {
        doFinish(nName=(name + "_Last"))
      }
    }

    cArr.last
  }
  //def finish(): Unit = {
  //  assert(nArr.size > 0)
  //  //cArr += CtrlLink(
  //  //  up={
  //  //    nArr.last
  //  //  }
  //  //)
  //}
  def first = cArr(0)
  def last = cArr.last //s2mArr.last
  //def first = sArr(0)
  //def last = cArr.last
  //def doBuilder(): Unit = {
  //  Builder(linkArr.toSeq)
  //}
}
