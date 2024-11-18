package libcheesevoyage.bus.lcvStall

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

case class LcvStallIo[
  HostDataT <: Data,
  DevDataT <: Data,
](
  // I'm not sure whether or not these can be made into
  // `Option[HardType[...DataType]]`s. It might be possible?
  hostDataType: (Boolean, HardType[HostDataT]),
  devDataType: (Boolean, HardType[DevDataT]),
  optFormal: Boolean=false
) //extends Bundle with IMasterSlave 
extends Area
{
  ////--------
  //val valid = in(Bool())
  //val hostData = (hostDataType._1) generate (
  //  in(hostDataType._2())
  //)
  //val ready = out(Bool())
  //val devData = (devDataType._1) generate (
  //  out(devDataType._2())
  //)
  ////--------
  //def asMaster(): Unit = {
  //  //--------
  //  out(valid)
  //  //if (hostDataType._1) {
  //    out(hostData)
  //  //}
  //  //--------
  //  in(ready)
  //  //if (devDataType._1) {
  //    in(devData)
  //  //}
  //  //--------
  //}
  ////--------
  val nextValid = Bool()
  val rValid = (
    RegNext(nextValid)
    init(nextValid.getZero)
  )
  nextValid := rValid
  val ready = Bool()
  val fire = rValid && ready
  //if (optFormal) {
  //}
  def mkSaved(
    someLink: CtrlLink,
    myName: String,
    optIncludeOneStageStall: Boolean=false,
  ) = new Area {
    //println(
    //  s"${myName}"
    //)
    //--------
    val rSavedFire = (
      KeepAttribute(
        Reg(
          Bool()
        )
        init(
          False
        )
      )
      .setName(
        s"${myName}_"
        + s"rSavedFire"
      )
    )
    val eitherFire = (
      KeepAttribute(
        fire
        || rSavedFire
      )
      .setName(
        s"${myName}_"
        + s"eitherFire"
      )
    )
    //--------
    when (
      someLink.up.isValid 
    ) {
      //--------
      when (fire) {
        rSavedFire := True
      }
      when (someLink.up.isFiring) {
        rSavedFire := False
      }
      //--------
    }
    //--------
    //--------
  }
  if (optFormal) {
    anyseq(ready)
    when (pastValidAfterReset) {
      when (fire) {
        assume(!RegNext(ready))
      }
      when (
        !rValid
        //&& ready
      ) {
        //assume(!ready)
        assume(!RegNext(ready))
      }
      //when (!nextValid) {
      //  assume(!RegNext(ready))
      //}
      when (rValid) {
        cover(
          (
            RegNext(rValid)
          ) && (
            !ready
          ) && (
            RegNext(ready)
          )
        )
      }
      //--------
    }
  }
}
