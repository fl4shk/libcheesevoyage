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
  hostDataType: Option[HardType[HostDataT]],
  devDataType: Option[HardType[DevDataT]],
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
  //val rReady = (
  //  RegNext(nextReady)
  //  init(nextReady.getZero)
  //)
  val fire = rValid && ready
  //--------
  val rHadFirstFire = (
    (optFormal) generate (
      RegNextWhen(
        True,
        fire,
      )
      init(False)
    )
  )
  val rHadSecondFire = (
    (optFormal) generate (
      RegNextWhen(
        rHadFirstFire,
        fire,
      )
      init(False)
    )
  )
  if (optFormal) {
    anyseq(ready)
    when (pastValidAfterReset) {
      //when (fire) {
      //  assume(!RegNext(ready))
      //}
      //--------
      when (
        !rValid
        //&& ready
      ) {
        assume(!ready)
        //--------
        //when (!RegNext(rValid)) {
        //  assume(!RegNext(ready))
        //}
        //--------
        //assume(!RegNext(ready))
        //assume(!ready)
      }
      when (
        past(rValid) init(False)
      ) {
        when (
          //RegNextWhen(
          //  fire,
          //)
          //init(False)
          //rHadFirstFire
          //&& 
          rHadSecondFire
          //True
        ) {
          cover(
            //!ready
            //&& (
            //  RegNext(ready)
            //  init(False)
            //)
            ready
            && (
              !(
                past(ready)
                init(False)
              )
            )
          )
        }
      }
      //--------
    }
  }
  //--------
  //def mkSaved(
  //  someLink: CtrlLink,
  //  myName: String,
  //  //optIncludeOneStageStall: Boolean=false,
  //) = LcvStallIoSaved(
  //  stallIo=this,
  //  someLink=someLink,
  //  myName=myName,
  //)
}
case class LcvStallIoSaved[
  HostDataT <: Data,
  DevDataT <: Data,
](
  stallIo: LcvStallIo[
    HostDataT,
    DevDataT,
  ],
  someLink: CtrlLink,
  //myName: String,
) extends Area {
  //println(
  //  s"${myName}"
  //)
  //--------
  //--------
  val nextSavedFire = (
    //KeepAttribute(
      Bool()
    //)
    //.setName(
    //  s"${myName}_"
    //  + s"nextSavedFire"
    //)
  )
  val rSavedFire = (
    //KeepAttribute(
      RegNext(
        nextSavedFire
      )
      init(
        nextSavedFire.getZero
      )
    //)
    //.setName(
    //  s"${myName}_"
    //  + s"rSavedFire"
    //)
  )
  nextSavedFire := rSavedFire
  //--------
  val nextHadDownFire = (
    //KeepAttribute (
      Bool()
    //)
    //.setName(
    //  s"${myName}_"
    //  + s"nextHadDownFire"
    //)
  )
  val rHadDownFire = (
    //KeepAttribute (
      RegNext(
        nextHadDownFire
      )
      init(
        nextHadDownFire.getZero
      )
    //)
    //.setName(
    //  s"${myName}_"
    //  + s"rHadDownFire"
    //)
  )
  nextHadDownFire := rHadDownFire
  //--------
  val myDuplicateIt = (
    KeepAttribute(
      // We should only call `someLink.duplicateIt()` until the first cycle 
      // that we haven't seen `stallIo.fire` and `someLink.down.isFiring` 
      // since seeing
      // `someLink.up.isValid`
      !(
        (
          stallIo.fire
          && someLink.down.isFiring
        ) || (
          stallIo.fire
          && rHadDownFire
        ) || (
          rSavedFire
          && someLink.down.isFiring
        )
      )
    )
    //.setName(
    //  s"${myName}_"
    //  + s"myDuplicateIt"
    //)
  )
  //--------
  when (
    stallIo.fire
  ) {
    nextSavedFire := True
  }
  when (
    someLink.down.isFiring
  ) {
    nextHadDownFire := True
  }
  when (
    someLink.up.isFiring
  ) {
    nextSavedFire := False
    nextHadDownFire := False
  }
  //val eitherSavedFire = (
  //  KeepAttribute(
  //    Bool()
  //  )
  //  .setName(
  //    s"${myName}_"
  //    + s"eitherSavedFire"
  //  )
  //)
  //--------
}
