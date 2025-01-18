package libcheesevoyage.bus.lcvStall

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

//--------
class LcvStallIo[
  SendPayloadT <: Data,
  RecvPayloadT <: Data,
](
  sendPayloadType: Option[HardType[SendPayloadT]],
  recvPayloadType: Option[HardType[RecvPayloadT]],
  //hostDataType: (Boolean, HardType[HostDataT]),
  //devDataType: (Boolean, HardType[HostDataT]),
) extends Bundle with IMasterSlave {
  //--------
  val mySendPayloadType: (Boolean, HardType[SendPayloadT]) = (
    sendPayloadType match {
      case Some(innerSendPayloadType) => {
        (true, innerSendPayloadType())
      }
      case None => {
        (false, null)
      }
    }
  )
  val myRecvPayloadType: (Boolean, HardType[RecvPayloadT]) = (
    recvPayloadType match {
      case Some(innerRecvPayloadType) => {
        (true, innerRecvPayloadType())
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
  val sendData = (mySendPayloadType._1) generate (
    in(mySendPayloadType._2())
  )
  val ready = out(Bool())
  val recvData = (myRecvPayloadType._1) generate (
    out(myRecvPayloadType._2())
  )
  def fire = (
    rValid && ready
  )
  //--------
  def asMaster(): Unit = {
    //--------
    out(nextValid)
    //out(rValid)
    if (mySendPayloadType._1) {
      out(sendData)
    }
    //--------
    in(ready)
    if (myRecvPayloadType._1) {
      in(recvData)
    }
    //--------
  }
}
//--------
case class LcvStallHost[
  SendPayloadT <: Data,
  RecvPayloadT <: Data,
](
  // I'm not sure whether or not these can be made into
  // `Option[HardType[...DataType]]`s. It might be possible?
  //hostDataType: Option[HardType[HostDataT]],
  //devDataType: Option[HardType[DevDataT]],
  //--------
  //hostDataType: (Boolean, HardType[HostDataT]),
  //devDataType: (Boolean, HardType[DevDataT]),
  stallIo: Option[LcvStallIo[
    SendPayloadT,
    RecvPayloadT,
  ]],
  //--------
  optFormalJustHost: Boolean=false
  //--------
) extends Area {
  //--------
  val myStallIo: (Boolean, LcvStallIo[SendPayloadT, RecvPayloadT]) = {
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
