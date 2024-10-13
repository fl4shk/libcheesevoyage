package libcheesevoyage.general

import spinal.core._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._
import scala.collection.mutable.ArrayBuffer
import scala.math._

import libcheesevoyage.Config

object LcvFormal extends Object {
  case class DelayStopAnyseq[
    WordT <: Data,
  ](
    signal: WordT,
    reset: WordT,
    delay: UInt,
    cntWidth: Int,
  ) extends Area {
    val rCnt = KeepAttribute(
      Reg(UInt(cntWidth bits))
      init(0x0)
    )
    when (rCnt < delay) {
      rCnt := rCnt + 1
    } otherwise {
      signal := reset
    }
  }
}
