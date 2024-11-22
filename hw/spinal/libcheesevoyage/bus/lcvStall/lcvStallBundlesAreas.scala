package libcheesevoyage.bus.lcvStall

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

//--------
case class LcvStallIo[
  HostDataT <: Data,
  DevDataT <: Data,
](
  hostDataType: Option[HardType[HostDataT]],
  devDataType: Option[HardType[DevDataT]],
  //hostDataType: (Boolean, HardType[HostDataT]),
  //devDataType: (Boolean, HardType[HostDataT]),
) extends Bundle with IMasterSlave {
  //--------
  val myHostDataType: (Boolean, HardType[Data]) = (
    hostDataType match {
      case Some(innerHostDataType) => {
        (true, innerHostDataType())
      }
      case None => {
        (false, Bool())
      }
    }
  )
  val myDevDataType: (Boolean, HardType[Data]) = (
    devDataType match {
      case Some(innerDevDataType) => {
        (true, innerDevDataType())
      }
      case None => {
        (false, Bool())
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
  val hostData = (myHostDataType._1) generate (
    in(myHostDataType._2())
  )
  val ready = out(Bool())
  val devData = (myDevDataType._1) generate (
    out(myDevDataType._2())
  )
  //--------
  def asMaster(): Unit = {
    //--------
    out(nextValid)
    //out(rValid)
    if (myHostDataType._1) {
      out(hostData)
    }
    //--------
    in(ready)
    if (myDevDataType._1) {
      in(devData)
    }
    //--------
  }
}
//--------
case class LcvStallHost[
  HostDataT <: Data,
  DevDataT <: Data,
](
  // I'm not sure whether or not these can be made into
  // `Option[HardType[...DataType]]`s. It might be possible?
  //hostDataType: Option[HardType[HostDataT]],
  //devDataType: Option[HardType[DevDataT]],
  //--------
  //hostDataType: (Boolean, HardType[HostDataT]),
  //devDataType: (Boolean, HardType[DevDataT]),
  stallIo: Option[LcvStallIo[
    HostDataT,
    DevDataT,
  ]],
  //--------
  optFormalJustHost: Boolean=false
  //--------
) extends Area {
  //--------
  val myStallIo: (Boolean, LcvStallIo[HostDataT, DevDataT]) = {
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
    //stallIo match {
    //  case Some(myStallIo) => {
    //    myStallIo.valid
    //  }
    //  case None => {
    //    Bool()
    //  }
    //}
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
    //stallIo match {
    //  case Some(myStallIo) => {
    //    Bool()
    //  }
    //}
    if (myStallIo._1) (
      myStallIo._2.ready
    ) else (
      Bool()
    )
  )
  //val rReady = (
  //  RegNext(
  //    next=nextReady,
  //    init=nextReady.getZero
  //  )
  //)
  //val rReady = (
  //  RegNext(nextReady)
  //  init(nextReady.getZero)
  //)
  val fire = (
    //rPastValid && ready
    //valid && nextReady
    //nextValid && rReady
    //rPastValid && nextReady
    //valid && rReady
    //rPastValid && ready
    //nextValid && ready
    rValid && ready
  )
  //val fireWithPastValid = (
  //  //fire
  //  rValid
  //  && rValid
  //)
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
    anyseq(
      //nextReady
      ready
    )
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
  HostDataT <: Data,
  DevDataT <: Data,
](
  stallHost: LcvStallHost[
    HostDataT,
    DevDataT,
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
