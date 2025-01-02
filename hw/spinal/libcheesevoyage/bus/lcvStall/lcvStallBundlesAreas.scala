package libcheesevoyage.bus.lcvStall

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

//--------
class LcvStallIo[
  HostPayloadT <: Data,
  DevPayloadT <: Data,
](
  hostPayloadType: Option[HardType[HostPayloadT]],
  devPayloadType: Option[HardType[DevPayloadT]],
  //hostDataType: (Boolean, HardType[HostDataT]),
  //devDataType: (Boolean, HardType[HostDataT]),
) extends Bundle with IMasterSlave {
  //--------
  val myHostPayloadType: (Boolean, HardType[HostPayloadT]) = (
    hostPayloadType match {
      case Some(innerHostPayloadType) => {
        (true, innerHostPayloadType())
      }
      case None => {
        (false, null)
      }
    }
  )
  val myDevPayloadType: (Boolean, HardType[DevPayloadT]) = (
    devPayloadType match {
      case Some(innerDevPayloadType) => {
        (true, innerDevPayloadType())
      }
      case None => {
        (false, null)
      }
    }
  )
  //--------
  val nextValid = in(Bool())
  //val rValid = in(Bool())
  def rValid = RegNext(
    next=nextValid,
    init=nextValid.getZero,
  )
  val hostData = (myHostPayloadType._1) generate (
    in(myHostPayloadType._2())
  )
  val ready = out(Bool())
  val devData = (myDevPayloadType._1) generate (
    out(myDevPayloadType._2())
  )
  //--------
  def asMaster(): Unit = {
    //--------
    out(nextValid)
    //out(rValid)
    if (myHostPayloadType._1) {
      out(hostData)
    }
    //--------
    in(ready)
    if (myDevPayloadType._1) {
      in(devData)
    }
    //--------
  }
}
//--------
case class LcvStallHost[
  HostPayloadT <: Data,
  DevPayloadT <: Data,
](
  // I'm not sure whether or not these can be made into
  // `Option[HardType[...DataType]]`s. It might be possible?
  //hostDataType: Option[HardType[HostDataT]],
  //devDataType: Option[HardType[DevDataT]],
  //--------
  //hostDataType: (Boolean, HardType[HostDataT]),
  //devDataType: (Boolean, HardType[DevDataT]),
  stallIo: Option[LcvStallIo[
    HostPayloadT,
    DevPayloadT,
  ]],
  //--------
  optFormalJustHost: Boolean=false
  //--------
) extends Area {
  //--------
  val myStallIo: (Boolean, LcvStallIo[HostPayloadT, DevPayloadT]) = {
    stallIo match {
      case Some(innerStallIo) => (true, innerStallIo)
      case None => (false, null)
    }
  }
  if (myStallIo._1) {
    require(
      //myStallIo._2.isMasterInterface
      !myStallIo._2.isSlaveInterface
    )
  }
  //--------
  val nextValid: Bool = (
    if (myStallIo._1) (
      myStallIo._2.nextValid
    ) else (
      Bool()
    )
  )
  val rValid = (
    if (myStallIo._1) (
      myStallIo._2.rValid
    ) else (
      RegNext(
        next=nextValid,
        init=nextValid.getZero,
      )
    )
  )
  nextValid := rValid
  val ready: Bool = (
    if (myStallIo._1) (
      myStallIo._2.ready
    ) else (
      Bool()
    )
  )
  val fire = (
    rValid && ready
  )
  //--------
  val rHadFirstFire = (
    (optFormalJustHost) generate (
      RegNextWhen(
        next=True,
        cond=fire,
        init=False,
      )
    )
  )
  val rHadSecondFire = (
    (optFormalJustHost) generate (
      RegNextWhen(
        next=rHadFirstFire,
        cond=fire,
        init=False,
      )
    )
  )
  //nextReady := rReady
  if (
    optFormalJustHost
    //&& (
    //  if (myStallIo._1) (
    //    myStallIo._2.
    //  ) else (
    //    true
    //  )
    //)
  ) {
    //anyseq(
    //  //nextReady
    //  ready
    //)
    when (pastValidAfterReset) {
      //when (fire) {
      //  assume(!RegNext(ready))
      //}
      //--------
      when (
        !rValid
        ////&& ready
        //!valid
      ) {
        assume(
          //!nextReady
          !ready
        )
        //--------
        //when (!RegNext(rValid)) {
        //  assume(!RegNext(ready))
        //}
        //--------
        //assume(!RegNext(ready))
        //assume(!ready)
      }
      when (
        //past(rValid) init(False)
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
            //nextReady
            ready
            && (
              !(
                past(ready) init(False)
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
case class LcvStallHostSaved[
  HostPayloadT <: Data,
  DevPayloadT <: Data,
](
  stallHost: LcvStallHost[
    HostPayloadT,
    DevPayloadT,
  ],
  someLink: CtrlLink,
) extends Area {
  //--------
  val nextSavedFire = (
    KeepAttribute(
      Bool()
    )
  )
  val rSavedFire = (
    KeepAttribute(
      RegNext(
        next=nextSavedFire,
        init=nextSavedFire.getZero,
      )
    )
  )
  nextSavedFire := rSavedFire
  //--------
  val nextHadDownFire = (
    KeepAttribute (
      Bool()
    )
  )
  val rHadDownFire = (
    KeepAttribute (
      RegNext(
        next=nextHadDownFire,
        init=nextHadDownFire.getZero,
      )
    )
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
          stallHost.fire
          && someLink.down.isFiring
        ) || (
          stallHost.fire
          && rHadDownFire
        ) || (
          rSavedFire
          && someLink.down.isFiring
        )
      )
    )
  )
  //--------
  when (stallHost.fire) {
    nextSavedFire := True
  }
  when (someLink.down.isFiring) {
    nextHadDownFire := True
  }
  when (someLink.up.isFiring) {
    nextSavedFire := False
    nextHadDownFire := False
  }
  //--------
}
