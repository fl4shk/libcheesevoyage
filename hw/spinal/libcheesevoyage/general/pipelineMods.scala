package libcheesevoyage.general
import libcheesevoyage._

import spinal.core._
import spinal.lib._
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer
//import scala.language.experimental.macros

//import scala.reflect.macros.blackbox
//object StmExtractPayloadField {
//  def fromHost[
//    FieldT <: Data,
//
//  ](
//  ) = {
//  }
//}
object StreamOptHsh {
  def craftFromHostStream[
    FieldT <: Data,
    HostStmPayloadT <: Data,
  ](
    fieldType: HardType[FieldT],
    optIncludeHsh: Boolean,
    hostStm: Stream[HostStmPayloadT],
  )(
    getHostStmFieldFunc: (
      Stream[HostStmPayloadT]
    ) => FieldT
  ): StreamOptHsh[FieldT] = {
    val ret = StreamOptHsh(
      fieldType=fieldType(),
      optIncludeHsh=optIncludeHsh
    )
    if (optIncludeHsh) {
      ret.valid := hostStm.valid
      hostStm.ready := ret.ready
    }
    ret.field := getHostStmFieldFunc(hostStm)

    ret
  }
  def craftFromDeviceStream[
    FieldT <: Data,
    DeviceStmPayloadT <: Data,
  ](
    fieldType: HardType[FieldT],
    optIncludeHsh: Boolean,
    deviceStm: Stream[DeviceStmPayloadT],
  )(
    getDeviceStmFieldFunc: (
      Stream[DeviceStmPayloadT]
    ) => FieldT
  ): StreamOptHsh[FieldT] = {
    val ret = StreamOptHsh(
      fieldType=fieldType(),
      optIncludeHsh=optIncludeHsh
    )
    if (optIncludeHsh) {
      deviceStm.valid := ret.valid
      ret.ready := deviceStm.ready
    }
    getDeviceStmFieldFunc(deviceStm) := ret.field

    ret
  }
}

case class StreamOptHsh[
  FieldT <: Data
](
  fieldType: HardType[FieldT],

  // whether to include `valid`/`ready`, which are handshaking signals
  optIncludeHsh: Boolean,
) extends Bundle with IMasterSlave {
  val valid = (optIncludeHsh) generate Bool()
  val ready = (optIncludeHsh) generate Bool()
  val field = fieldType()

  def asMaster(): Unit = {
    out(
      valid,
      field
    )
    in(ready)
  }

  def toHostTranslateInto[
    HostStmPayloadT <: Data,
  ](
    //hostStmPayload
    hostStm: Stream[HostStmPayloadT]
  )(
    hostStmPayloadFieldDriveFunc: (
      FieldT,
      //Stream[HostStmPayloadT],
      HostStmPayloadT,
    ) => FieldT
  ): Unit = {
    if (optIncludeHsh) {
      hostStm.valid := this.valid
      this.ready := hostStm.ready
    }
    hostStmPayloadFieldDriveFunc(
      this.field,
      hostStm.payload,
    )
  }
  def toDeviceTranslateInto[
    DeviceStmPayloadT <: Data,
  ](
    //deviceStmPayload
    deviceStm: Stream[DeviceStmPayloadT]
  )(
    deviceStmPayloadFieldDriveFunc: (
      //StreamOptHsh[FieldT],
      FieldT,
      //Stream[DeviceStmPayloadT],
      DeviceStmPayloadT,
    ) => FieldT
  ): Unit = {
    if (optIncludeHsh) {
      this.valid := deviceStm.valid
      deviceStm.ready := this.ready
    }
    deviceStmPayloadFieldDriveFunc(
      this.field,
      deviceStm.payload,
    )
  }
}

//object GenericHandlePipe {
//  def apply[
//    PipeElemT <: Data,
//    //ExtDataT,
//  ](
//    pipe: Vec[PipeElemT],
//    pipeStageIdx: Int,
//    pipeNumMainStages: Int,
//    //someExtData: ExtDataT,
//  )(
//    idxEqStageIdxFunc: (
//      Vec[PipeElemT],   // `pipe`
//      Int,              // `pipeStageIdx`
//      Int,              // `pipeNumMainStages`
//      Int,              // `idx`
//      //ExtDataT,         // `someExtData`
//    ) => Unit,
//    idxLtStageIdxFunc: (
//      Vec[PipeElemT],   // `pipe`
//      Int,              // `pipeStageIdx`
//      Int,              // `pipeNumMainStages`
//      Int,              // `idx`
//      //ExtDataT,         // `someExtData`
//    ) => Unit,
//    postMainFunc: (
//      Vec[PipeElemT],   // `pipe`
//      Int,              // `pipeStageIdx`
//      Int,              // `pipeNumMainStages`
//      Int,              // `idx`
//      //ExtDataT,         // `someExtData`
//    ) => Unit,
//  ): Unit = {
//    for (idx <- 0 to pipe.size - 1) {
//      //val mbrIdx = idx - someNumMembers
//      //if (mbrIdx >= 0 && mbrIdx < someNumMembers) {
//      //}
//      //val stageIdx = idx - someWrPipeNumStages
//      if (idx < pipeNumMainStages) {
//        if (idx == pipeStageIdx) {
//          idxEqStageIdxFunc(
//            pipe,
//            pipeStageIdx,
//            pipeNumMainStages,
//            idx,
//            //someExtData
//          )
//        } else if (idx < pipeStageIdx) {
//          idxLtStageIdxFunc(
//            pipe,
//            pipeStageIdx,
//            pipeNumMainStages,
//            idx,
//            //someExtData
//          )
//        } else {
//          postMainFunc(
//            pipe,
//            pipeStageIdx,
//            pipeNumMainStages,
//            idx,
//            //someExtData
//          )
//        }
//      } else { // if (idx >= pipeNumMainStages)
//        //pipe(idx) := pipe(idx - 1)
//        postMainFunc(
//          pipe,
//          pipeStageIdx,
//          pipeNumMainStages,
//          idx,
//          //someExtData
//        )
//      }
//    }
//  }
//}
case class DualPipeStageData[
  PipeElemT <: Data,
](
  pipeIn: Vec[PipeElemT],
  pipeOut: Vec[PipeElemT],
  pipeNumMainStages: Int,
  pipeStageIdx: Int,
  //pipeFieldName: String,
  ////idx: Int,
  ////setOutToPast: Option[Boolean],
  //setOutToPast: Boolean=false,
  ////rPastPipeOut: 
) {
  //def idxLtStageIdxMacro(
  //  c: blackbox.Context,
  //  //idx: Int,
  //)(
  //  number: c.Expr[Int]
  //): c.Expr[String] = {
  //  import c.universe._

  //  val Literal(Constant(s_number: Int)) = number.tree
  //  val result = s_number % 2 match {
  //    case 0 => Literal(Constant("even"))
  //    case _ => Literal(Constant("odd"))
  //  }
  //  c.Expr[String](result)
  //}
  def craft(
    nPipeStageIdx: Int,
  ) = DualPipeStageData[PipeElemT](
    pipeIn=this.pipeIn,
    pipeOut=this.pipeOut,
    pipeNumMainStages=this.pipeNumMainStages,
    pipeStageIdx=nPipeStageIdx,
  )
}

//object DualPipeStageData {
//  //def craftStmFlowArgs[
//  //  PipeElemT <: Data
//  //](
//  //  stageData: DualPipeStageData[PipeElemT],
//  //  //idx: Int,
//  //  setOutToPast: Boolean,
//  //): DualPipeStageData[PipeElemT] = (
//  //  DualPipeStageData[PipeElemT](
//  //    pipeIn=stageData.pipeIn,
//  //    pipeOut=stageData.pipeOut,
//  //    pipeStageIdx=stageData.pipeStageIdx,
//  //    pipeNumMainStages=stageData.pipeNumMainStages,
//  //    //idx=stageData.idx,
//  //    //idx=idx,
//  //    //setOutToPast=setOutToPast,
//  //  )
//  //)
//  //def getPipeElemT[PipeElemT <: Data](obj: PipeElemT): String = macro getTypeImplRef[PipeElemT]
//  //def idxLtStageIdxHelperMacro[PipeElemT]
//  
//
//  //def nonWorkStageImpl[
//  //  PipeElemT: c.WeakTypeTag
//  //](
//  //  c: blackbox.Context,
//  //)(
//  //  //self: c.Val[PipeElemT],
//  //  self: c.Expr[DualPipeStageData[PipeElemT]],
//  //  pipeFieldName: c.Expr[String],
//  //  idx: c.Expr[Int],
//  //): c.Expr[Unit] = {
//  //  import c.universe._
//
//  //  //val Literal(Constant(s_self: DualPipeStageData[PipeElemT])) = (
//  //  //  self.tree
//  //  //)
//
//  //  val pipeFieldTermName: TermName = pipeFieldName match {
//  //    case Literal(Constant(s: String)) => TermName(s)
//  //    case _ => c.abort(c.enclosingPosition, "Not a string literal")
//  //  }
//  //  // do a copy
//  //  c.Expr[Unit](
//  //    q"""
//  //    $self.pipeOut($idx).$pipeFieldTermName \
//  //    := (
//  //      $self.pipeIn($idx).$pipeFieldTermName 
//  //    );
//  //    """
//  //  )
//  //}
//
//  //def nonWorkStageFunc[
//  //  PipeElemT
//  //](
//  //  self: DualPipeStageData[PipeElemT],
//  //  pipeFieldName: String,
//  //  idx: Int,
//  //): Unit = macro nonWorkStageImpl[PipeElemT]
//  def nonWorkStageImpl [
//    //WeakPipeElemT: c.WeakTypeTag
//    //PipeElemT: c.WeakTypeTag
//    PipeElemT <: Data
//  ](
//    c: blackbox.Context,
//  )(
//    //self: c.Val[PipeElemT],
//    self: c.Expr[DualPipeStageData[PipeElemT]],
//    pipeFieldName: c.Expr[String],
//    idx: c.Expr[Int],
//  ): c.Expr[Unit] = {
//    import c.universe._
//
//    //val Literal(Constant(s_self: DualPipeStageData[PipeElemT])) = (
//    //  self.tree
//    //)
//
//    val pipeFieldTermName: TermName = pipeFieldName match {
//      case Literal(Constant(s: String)) => TermName(s)
//      case _ => c.abort(c.enclosingPosition, "Not a string literal")
//    }
//    // do a copy
//    c.Expr[Unit](
//      q"""
//      $self.pipeOut($idx).$pipeFieldTermName \
//      := (
//        $self.pipeIn($idx).$pipeFieldTermName 
//      );
//      """
//    )
//  }
//  def nonWorkStageFunc[
//    PipeElemT <: Data
//  ](
//    self: DualPipeStageData[PipeElemT],
//    pipeFieldName: String,
//    idx: Int,
//  ): Unit = macro nonWorkStageImpl[PipeElemT]
//}
object HandleDualPipe {
  def apply[
    PipeElemT <: Data,
    //ExtDataT,
  ](
    stageData: DualPipeStageData[PipeElemT],
  )(
    pipeStageMainFunc: (
      DualPipeStageData[PipeElemT],  // `stageData`
      Int,                              // `idx`
    ) => Unit,
    copyOnlyFunc: (
      DualPipeStageData[PipeElemT],  // `stageData`
      Int,                              // `idx`
    ) => Unit,
  ): Unit = {
    assert(stageData.pipeIn.size == stageData.pipeOut.size)
    for (idx <- 0 to stageData.pipeIn.size - 1) {
      if (idx < stageData.pipeNumMainStages) {
        if (idx == stageData.pipeStageIdx) {
          pipeStageMainFunc(
            stageData,
            idx,
          )
        } else {
          //DualPipeStageData.nonWorkStageFunc[PipeElemT](
          //  stageData,
          //  stageData.pipeFieldName,
          //  idx,
          //)
          copyOnlyFunc(
            stageData,
            idx,
          )
        }
      } else { // if (idx >= stageData.pipeNumMainStages)
        //DualPipeStageData.nonWorkStageFunc[PipeElemT](
        //  stageData,
        //  stageData.pipeFieldName,
        //  idx,
        //)
        copyOnlyFunc(
          stageData,
          idx,
        )
      }
    }
  }
}
//object HandleStmPipe {
//  def apply[
//    PipeElemT <: Data,
//  ](
//    stageData: DualPipeStageData[PipeElemT],
//  )
//}
//object HandleStmPipe {
//  def apply[
//    PipeElemT <: Data,
//  ](
//    pipeIn: Vec[Stream[PipeElemT]],
//    pipeOut: Vec[Stream[PipeElemT]],
//    pipeStageIdx: Int,
//    pipeNumMainStages: Int,
//  )(
//    idxEqStageIdxFunc: (
//      DualPipeStageData[Stream[PipeElemT]],    // `stageData`
//      Int,                                        // `idx`
//    ) => Unit,
//    idxLtStageIdxFunc: (
//      DualPipeStageData[Stream[PipeElemT]],    // `stageData`
//      Int,                                        // `idx`
//    ) => Unit,
//    postMainFunc: (
//      DualPipeStageData[Stream[PipeElemT]],    // `stageData`
//      Int,                                        // `idx`
//    ) => Unit,
//  ): Unit = {
//    val tempDualPipeFuncStageData = DualPipeStageData[Stream[PipeElemT]](
//      pipeIn=pipeIn,
//      pipeOut=pipeOut,
//      pipeStageIdx=pipeStageIdx,
//      pipeNumMainStages=pipeNumMainStages,
//      //idx=-1,
//      //setOutToPast=false,
//    )
//    //--------
//    def genericIdxEqStageIdxFunc(
//      genericStageData: DualPipeStageData[Stream[PipeElemT]],
//      genericIdx: Int,
//    ): Unit = {
//      when (genericStageData.pipeIn(genericIdx).fire) {
//        idxEqStageIdxFunc(
//          DualPipeStageData.craftStmFlowArgs(
//            //stageData=tempDualPipeFuncStageData,
//            stageData=genericStageData,
//            setOutToPast=false,
//          ),
//          genericIdx,
//        )
//      } otherwise {
//        idxEqStageIdxFunc(
//          DualPipeStageData.craftStmFlowArgs(
//            //stageData=tempDualPipeFuncStageData,
//            stageData=genericStageData,
//            setOutToPast=true,
//          ),
//          genericIdx,
//        )
//      }
//    }
//    def genericIdxLtStageIdxFunc(
//      genericStageData: DualPipeStageData[Stream[PipeElemT]],
//      genericIdx: Int,
//    ): Unit = {
//      when (genericStageData.pipeIn(genericIdx).fire) {
//        idxLtStageIdxFunc(
//          DualPipeStageData.craftStmFlowArgs(
//            //stageData=tempDualPipeFuncStageData,
//            stageData=genericStageData,
//            setOutToPast=false,
//          ),
//          genericIdx,
//        )
//      } otherwise {
//        idxLtStageIdxFunc(
//          DualPipeStageData.craftStmFlowArgs(
//            //stageData=tempDualPipeFuncStageData,
//            stageData=genericStageData,
//            setOutToPast=true,
//          ),
//          genericIdx,
//        )
//      }
//    }
//    def genericPostMainFunc(
//      genericStageData: DualPipeStageData[Stream[PipeElemT]],
//      genericIdx: Int,
//    ): Unit = {
//      when (genericStageData.pipeIn(genericIdx).fire) {
//        postMainFunc(
//          DualPipeStageData.craftStmFlowArgs(
//            //stageData=tempDualPipeFuncStageData,
//            stageData=genericStageData,
//            setOutToPast=false,
//          ),
//          genericIdx,
//        )
//      } otherwise {
//        postMainFunc(
//          DualPipeStageData.craftStmFlowArgs(
//            //stageData=tempDualPipeFuncStageData,
//            stageData=genericStageData,
//            setOutToPast=true,
//          ),
//          genericIdx,
//        )
//      }
//    }
//    //--------
//    GenericHandleDualPipe(
//      stageData=tempDualPipeFuncStageData
//    )(
//      idxEqStageIdxFunc=genericIdxEqStageIdxFunc,
//      idxLtStageIdxFunc=genericIdxLtStageIdxFunc,
//      postMainFunc=genericPostMainFunc,
//    )
//  }
//}
//object HandleFlowPipe {
//  def apply[
//    PipeElemT <: Data,
//  ](
//    pipeIn: Vec[Flow[PipeElemT]],
//    pipeOut: Vec[Flow[PipeElemT]],
//    pipeStageIdx: Int,
//    pipeNumMainStages: Int,
//  )(
//    idxEqStageIdxFunc: (
//      DualPipeStageData[Flow[PipeElemT]],      // `stageData`
//      Int,                                        // `idx`
//    ) => Unit,
//    //idxLtStageIdxFunc: (
//    //  DualPipeStageData[Flow[PipeElemT]],      // `stageData`
//    //  Int,                                        // `idx`
//    //) => Unit,
//    //postMainFunc: (
//    //  DualPipeStageData[Flow[PipeElemT]],      // `stageData`
//    //  Int,                                        // `idx`
//    //) => Unit,
//    //doSetOutToPast: (
//    //  DualPipeStageData
//    //)
//  ): Unit = {
//    val tempDualPipeFuncStageData = DualPipeStageData[Flow[PipeElemT]](
//      pipeIn=pipeIn,
//      pipeOut=pipeOut,
//      pipeStageIdx=pipeStageIdx,
//      pipeNumMainStages=pipeNumMainStages,
//      //idx=-1,
//      //setOutToPast=false,
//    )
//    //--------
//    def genericIdxEqStageIdxFunc(
//      genericStageData: DualPipeStageData[Flow[PipeElemT]],
//      genericIdx: Int,
//    ): Unit = {
//      when (genericStageData.pipeIn(genericIdx).fire) {
//        idxEqStageIdxFunc(
//          //DualPipeStageData.craftStmFlowArgs(
//          //  //stageData=tempDualPipeFuncStageData,
//          //  stageData=genericStageData,
//          //  setOutToPast=false,
//          //),
//          genericIdx,
//        )
//      } otherwise {
//        idxEqStageIdxFunc(
//          //DualPipeStageData.craftStmFlowArgs(
//          //  //stageData=tempDualPipeFuncStageData,
//          //  stageData=genericStageData,
//          //  setOutToPast=true,
//          //),
//          genericIdx,
//        )
//      }
//    }
//    //def genericIdxLtStageIdxFunc(
//    //  genericStageData: DualPipeStageData[Flow[PipeElemT]],
//    //  genericIdx: Int,
//    //): Unit = {
//    //  when (genericStageData.pipeIn(genericIdx).fire) {
//    //    idxLtStageIdxFunc(
//    //      DualPipeStageData.craftStmFlowArgs(
//    //        //stageData=tempDualPipeFuncStageData,
//    //        stageData=genericStageData,
//    //        setOutToPast=false,
//    //      ),
//    //      genericIdx,
//    //    )
//    //  } otherwise {
//    //    idxLtStageIdxFunc(
//    //      DualPipeStageData.craftStmFlowArgs(
//    //        //stageData=tempDualPipeFuncStageData,
//    //        stageData=genericStageData,
//    //        setOutToPast=true,
//    //      ),
//    //      genericIdx,
//    //    )
//    //  }
//    //}
//    //def genericPostMainFunc(
//    //  genericStageData: DualPipeStageData[Flow[PipeElemT]],
//    //  genericIdx: Int,
//    //): Unit = {
//    //  when (genericStageData.pipeIn(genericIdx).fire) {
//    //    postMainFunc(
//    //      DualPipeStageData.craftStmFlowArgs(
//    //        //stageData=tempDualPipeFuncStageData,
//    //        stageData=genericStageData,
//    //        setOutToPast=false,
//    //      ),
//    //      genericIdx,
//    //    )
//    //  } otherwise {
//    //    postMainFunc(
//    //      DualPipeStageData.craftStmFlowArgs(
//    //        //stageData=tempDualPipeFuncStageData,
//    //        stageData=genericStageData,
//    //        setOutToPast=true,
//    //      ),
//    //      genericIdx,
//    //    )
//    //  }
//    //}
//    //--------
//    //GenericHandleDualPipe(
//    //  stageData=tempDualPipeFuncStageData
//    //)(
//    //  idxEqStageIdxFunc=genericIdxEqStageIdxFunc,
//    //  idxLtStageIdxFunc=genericIdxLtStageIdxFunc,
//    //  postMainFunc=genericPostMainFunc,
//    //)
//  }
//}

//case class PstageFwdIoOneDir[
//  T <: Data
//](
//  dataType: HardType[T]
//) extends Bundle {
//  val valid = Bool()
//  val data = dataType()
//}
//case class PstageBakIoOneDir() extends Bundle {
//  val ready = Bool()
//}
//case class PipeSkidBufSideIo[
//  T <: Data
//]() extends Bundle with IMasterSlave {
//  val fwd = PstageFwdIoOneDir[T=T]()
//  val bak = PstageBakIoOneDir()
//
//  override def asMaster(): Unit = {
//    out(fwd)
//    in(bak)
//  }
//}
//case class PipeSkidBufSideIo[
//  T <: Data
//]() extends 

case class PipeSkidBufMiscIo(
  //optIncludeValidBusy: Boolean,
  //optIncludeReadyBusy: Boolean,
  optIncludeBusy: Boolean,
) extends Bundle {
  ////if (optIncludeValidBusy) {
  //  val validBusy = Bool()
  ////}
  ////if (optIncludeReadyBusy) {
  //  val readyBusy = Bool()
  ////}
  val busy = (optIncludeBusy) generate Bool()
  val clear = Bool()
}

case class PsbIoParentData[
  T <: Data
](
  val fromChild: T,
  val toOut: T,
) {
}

object PipeSkidBufIo {
  def connectParallel[
    T <: Data,
  ](
    //dataType: HardType[T],
    sbIoList: List[PipeSkidBufIo[T]],
    tieFirstIfwdValid: Boolean=true,
    tieLastIbakReady: Boolean=true,
  ): Unit = {
    //assert len(sbBusLst) >= 2

    for (idx <- 0 to sbIoList.size - 2) {
      val sbIo = sbIoList(idx)
      val sbIoNext = sbIoList(idx + 1)
      sbIoNext.prev.connectFrom(sbIo.next)
    }
    if (tieFirstIfwdValid) {
      //sbBusLst(0).inp.fwd.valid := 0b1
      sbIoList(0).prev.valid := True
    }
    if (tieLastIbakReady) {
      //sbBusLst[-1].inp.bak.ready := 0b1
      sbIoList.last.next.ready := True
    }
  }
}
case class PipeSkidBufIo[
  T <: Data
](
  dataType: HardType[T],
  //optIncludeValidBusy: Boolean=false,
  //optIncludeReadyBusy: Boolean=false,
  optIncludeBusy: Boolean=false,
) extends Bundle //with IMasterSlave
{
  val next = master Stream(dataType())
  val prev = slave Stream(dataType())
  //val next = slave port Stream(dataType())
  //val prev = master port Stream(dataType())
  val misc = in(PipeSkidBufMiscIo(
    //optIncludeValidBusy=optIncludeValidBusy,
    //optIncludeReadyBusy=optIncludeReadyBusy,
    optIncludeBusy=optIncludeBusy,
  ))
  //val busy = (optIncludeBusy) generate in port Bool()
  //def asMaster(): Unit = {
  //  slave(next)
  //  master(prev)
  //  out(misc)
  //}
  //def asSlave(): Unit = {
  //  master(next)
  //  slave(prev)
  //  in(misc)
  //}
  //def asMaster(): Unit = {
  //  slave(next)
  //  master(prev)
  //  out(misc)
  //}

  def connectParentStreams[
    pushPayloadT <: Data,
    //popPayloadT <: Data
  ](
    push: Stream[pushPayloadT],
    pop: Stream[T],
    //parentData: Option[PsbIoParentData[T]]=null,
  )(
    payloadConnFunc: (
      T, // this.prev.payload
      pushPayloadT, // push.payload
    ) => Unit
  ): Unit = {
    //connFunc(
    //  push.payload,
    //  this.prev.payload
    //)
    //parentData match {
    //  case None => {
    //  }
    //}

    //childSbIo.prev << this.prev
    this.prev.valid := push.valid
    push.ready := this.prev.ready
    pop << this.next

    payloadConnFunc(
      this.prev.payload,
      push.payload,
    )
  }

  def connectChild(
    //childSbIo: //this.type,
    childSbIo: PipeSkidBufIo[T],
    //parentDataFromChild: Option[HardType[T]]=null,
    //parentDataToChild: Option[HardType[T]]=null,
    parentData: Option[PsbIoParentData[T]]=null,
  ): Unit = {
    //--------
    //childSbIo.prev.valid := this.prev.valid
    //childSbIo.prev.payload := this.prev.payload
    //this.prev.ready := childSbIo.prev.ready
    childSbIo.prev << this.prev

    this.next.valid := childSbIo.next.valid
    //this.next.payload := childSbIo.next.payload
    childSbIo.next.ready := this.next.ready

    //this.misc <> childSbIo.misc
    childSbIo.misc := this.misc
    //if (optIncludeBusy) {
    //  childSbIo.busy := this.busy
    //}

    parentData match {
      case None => {
        this.next.payload := childSbIo.next.payload
      }
      case Some(haveParentData) => {
        haveParentData.fromChild := childSbIo.next.payload
        this.next.payload := haveParentData.toOut
      }
    }
    //--------
  }
}
case class PipeSkidBuf[
  T <: Data
](
  dataType: HardType[T],
  //optIncludeValidBusy: Boolean=false,
  //optIncludeReadyBusy: Boolean=false,
  optIncludeBusy: Boolean=false,
  optPassthrough: Boolean=false,
  optUseOldCode: Boolean=false,
  //optUseOldCode: Boolean=true,
  //optTieIfwdValid: Boolean=false,
  //optFormal: Boolean=false,
) extends Component {
  //--------
  val io = PipeSkidBufIo(
    dataType=dataType(),
    //optIncludeValidBusy=optIncludeValidBusy,
    //optIncludeReadyBusy=optIncludeReadyBusy,
    optIncludeBusy=optIncludeBusy,
  ) addAttribute("keep")
  //--------
  if (optPassthrough) {
    //--------
    val ifwdPayload = io.prev.payload
    val ifwdValid = io.prev.valid
    val obakReady = io.prev.ready

    //val ofwdPayload = io.next.payload
    val ofwdPayload = Reg(dataType()) init(Reg(dataType()).getZero)
    io.next.payload := ofwdPayload

    val ofwdValid = io.next.valid
    val ibakReady = io.next.ready

    //val misc = io.misc
    //--------
    ofwdValid := ifwdValid
    when(ifwdValid & ibakReady) {
      ofwdPayload := ifwdPayload
    }
    obakReady := ibakReady
    //--------
  } else { // if (!optPassthrough)
    //--------
    val ifwdPayload = io.prev.payload
    val ifwdValid = io.prev.valid
    val obakReady = io.prev.ready

    val ofwdPayload = io.next.payload
    //val ofwdPayload = Reg(dataType()) //init(U(0))
    //io.next.payload := ofwdPayload

    val ofwdValid = io.next.valid
    val ibakReady = io.next.ready

    val misc = io.misc
    val tempValidBusy = Bool()
    val tempReadyBusy = Bool()

    if (!optUseOldCode) {
      //val psbStm = (
      //  if (!optIncludeBusy) {
      //    new Stream(dataType())
      //  } else { // if (optIncludeBusy)
      //    new Stream(dataType()).haltWhen(
      //      //io.busy
      //      io.misc.busy
      //    )
      //  }
      //) addAttribute("keep")
      val tempIoPrev = (
        if (!optIncludeBusy) {
          //new Stream(dataType())
          io.prev
        } else { // if (optIncludeBusy)
          io.prev.haltWhen(
            //io.busy
            io.misc.busy
          )
        }
      ) addAttribute("keep")
      val psbStm = new Stream(dataType()) addAttribute("keep")

      //io.prev >/-> psbStm
      //val s2mPipe = psbStm.s2mPipe()
      //val s2mM2sPipe = s2mPipe.m2sPipe()
      //io.prev << s2mM2sPipe
      //io.prev >/-> psbStm
      //val s2mPipe = io.prev.s2mPipe()

      //val s2mPipe = tempIoPrev.s2mPipe()
      //val s2mM2sPipe = s2mPipe.m2sPipe()
      //psbStm << s2mM2sPipe
      psbStm <-/< tempIoPrev

      //psbStm <-/< io.prev
      //psbStm.translateInto(io.next){
      //  //(oPayload, psbPayload) => oPayload := psbPayload
      //  //o.payload := psb.payload; 
      //  (o, i) => {
      //    when (clockDomain.isResetActive) {
      //      o := o.getZero
      //    } elsewhen (
      //      //io.prev.fire
      //      tempIoPrev.fire
      //    ) {
      //      o := i
      //    } otherwise {
      //      o := o.getZero
      //    }
      //  }
      //}
      psbStm >> io.next
      GenerationFlags.formal {
        when (pastValidAfterReset) {
          psbStm.formalAssertsMaster()
          //psbStm.formalAssumesSlave()
          io.next.formalAssumesSlave()
          //psbStm.formalAssumesSlave()
          //io.next.formalAssertsMaster()
        }
      }
    } else { // if (optUseOldCode)
    //--------
      //val loc = Reg(Flow(dataType())) init(Flow(dataType()).getZero)
      //val loc = Reg(Flow(dataType())) init(Flow(dataType()).clearAll())
      val locS = Reg(Flow(dataType()))
      locS.init(locS.getZero)
      locS.addAttribute("keep")
      //val locC = locS.wrapNext()
      val locC = Flow(dataType())
      tempValidBusy := (
        //if (!optIncludeValidBusy)
        if (!optIncludeBusy) {
          False
        } else {
          //misc.validBusy
          misc.busy
        }
      )
      tempReadyBusy := (
        //if (!optIncludeReadyBusy)
        if (!optIncludeBusy) {
          False
        } else {
          //misc.readyBusy
          misc.busy
        }
      )

      when (
        clockDomain.isResetActive
        | misc.clear

        // TODO: not sure if the below is going to work, so might
        // need to comment it out
        //| (
        //  // Use a Scala "mux" instead of an SpinalHDL `Mux`
        //  0b0
        //  //if not optIncludeValidBusy
        //  //else misc.validBusy
        //  if not optIncludeBusy
        //  else misc.busy
        //)
        //| (
        //  if (!optIncludeValidBusy) {
        //    True
        //  } else { // if (optIncludeValidBusy)
        //    misc.validBusy
        //  }
        //)
        //| (
        //  if (!optIncludeBusy) {
        //    False
        //  } else {
        //    misc.busy
        //  }
        //)
      ) {
        locC.valid := False
        //locC.validNext := False
      } elsewhen (
        (ifwdValid & obakReady)
        & (ofwdValid & ~ibakReady)
      ) {
        locC.valid := True
        //locC.validNext := True
      } elsewhen (ibakReady) {
        locC.valid := False
        //locC.validNext := False
      } otherwise {
        //locC.validNext := locS.valid
        locC.valid := locS.valid
      }

      when (
        clockDomain.isResetActive
        | misc.clear
        //| (
        //  // Use a Scala "mux" instead of a SpinalHDL `Mux`
        //  0b0
        //  if not optIncludeBusy
        //  else misc.busy
        //)
      ) {
        locC.payload := locS.payload.getZero
      } elsewhen (obakReady) {
        locC.payload := ifwdPayload
      } otherwise {
        locC.payload := locS.payload
      }

      locS := locC


      //when (
      //  clockDomain.isResetActive
      //  | misc.clear
      //) {
      //  obakReady := False
      //  ofwdValid := False
      //} otherwise {
      //  obakReady := (
      //    ~locS.valid
      //    //& tempInvReadyBusy
      //  )
      //  // `ifwdValid` will be registered in the general case
      //  ofwdValid := (
      //    (ifwdValid | locS.valid)
      //    //& tempInvValidBusy
      //  )
      //}
      when (
        clockDomain.isResetActive
        | misc.clear
      ) {
        obakReady := False
        ofwdValid := False
      } otherwise {
        obakReady := (
          ~locS.valid
          & ~tempReadyBusy
          //locS.ready
          //& (
          //  0b1
          //  if not optIncludeReadyBusy
          //  // Use a Scala "mux" instead of a SpinalHDL
          //  // `Mux` AND the upstream `ready` with
          //  // ~`readyBusy`
          //  else ~misc.readyBusy
          //)
          ////& (
          ////  // Use a Scala "mux" instead of a SpinalHDL
          ////  // `Mux`
          ////  0b1
          ////  if not optIncludeBusy
          ////  else ~misc.busy
          ////)
        )
        ofwdValid := (
          // `ifwdValid` will be registered in the general case
          (ifwdValid | locS.valid)
          & ~tempValidBusy
          //& (
          //  0b1
          //  if not optIncludeValidBusy
          //  // Use a Scala "mux" instead of a SpinalHDL
          //  // `Mux` AND the downstream `valid` with
          //  // ~`validBusy`
          //  else ~misc.validBusy
          //)
          ////& (
          ////  // Use a Scala "mux" instead of a SpinalHDL
          ////  // `Mux`
          ////  0b1
          ////  if not optIncludeBusy
          ////  else ~misc.busy
          ////)
        )
      }

      //when(locS.valid) {
      //  ofwdPayload := locS.payload
      //} otherwise { // when (~locS.valid)
      //  ofwdPayload := ifwdPayload
      //}

      when (locS.valid) {
        ofwdPayload := locS.payload
      } otherwise { // when (locS.valid)
        ofwdPayload := ifwdPayload
      }

      GenerationFlags.formal {
      //if (optFormal) 
        //var testValidBusy: Boolean = false
        //var testReadyBusy: Boolean = false
        //if (optIncludeValidBusy) {
        //  testValidBusy = misc.validBusy
        //}
        //if (optIncludeValidBusy) {
        //  testValidBusy = misc.validBusy
        //}
        //// Ensure the formal test starts with a reset

        //assumeInitial(clockDomain.isResetActive)
        //assumeInitial(~misc.clear)
        //assumeInitial(~ifwdValid)
        //assumeInitial(ifwdPayload === ifwdPayload.getZero)
        //assumeInitial(~ibakReady)
        when (
          //pastValid
          pastValidAfterReset
          & ~misc.clear
          & ~tempValidBusy
          & ~tempReadyBusy
        ) {
          //when (clockDomain.isResetActive) {
          //  if (!optTieIfwdValid) {
          //    assume(~ifwdValid)
          //  }
          //  ////assert(~ifwdValid)
          //  ////assume(~locS.valid & ~ofwdValid)
          //  //assert(~locS.valid)
          //  assert(~psbStm.valid)
          //  assert(~ofwdValid)
          //} otherwise {
            when (past(ifwdValid) & ~past(obakReady)) {
              assume(ifwdValid)
              //assume(stable(ifwdPayload))
            }
            when (past(ofwdValid) & ~past(ibakReady)) {
              assert(ofwdValid)
              assert(stable(ofwdPayload))
            }
            when (
              past(ifwdValid) & past(obakReady)
              & past(ofwdValid) & ~past(ibakReady)
            ) {
              assert(locS.valid)
              assert(locS.payload === past(ifwdPayload))
              //assert(locC.payload === ifwdPayload)
              //assert(psbStm.valid)
              //assert(psbStm.payload === past(ifwdPayload))
            }
            when ((ifwdValid) & (obakReady)) {
              assert((ofwdValid))
              //assert(psbStm.valid)
            }
            when (~(ifwdValid) && ~(locS.valid) && (ibakReady)) {
            //when (~(ifwdValid) && ~(psbStm.valid) && (ibakReady)) 
              assert(~(ofwdValid))
            }
            when (past(locS.valid) & past(ibakReady)) {
            //when (past(psbStm.valid) & past(ibakReady)) 
              assert(~locS.valid)
              //assert(~psbStm.valid)
            }
          //}
          //when (clockDomain.isResetActive) {
          //  if (!optTieIfwdValid) {
          //    assert(~ifwdValid)
          //  }
          //  //assert(~ifwdValid)
          //  assert(~locS.valid & ~ofwdValid)
          //} otherwise {
          //  when (past(ifwdValid) & ~past(obakReady)) {
          //    assert(ifwdValid)
          //    //assert(stable(ifwdPayload))
          //    assert(stable(ifwdPayload))
          //  }
          //  when (past(ofwdValid) & ~past(ibakReady)) {
          //    assert(obakReady)
          //    //assert(stable(ofwdPayload))
          //    assert(stable(ofwdPayload))
          //  }
          //  when (
          //    past(ifwdValid) & past(obakReady)
          //    & past(ofwdValid) & ~past(ibakReady)
          //  ) {
          //    assert(locS.valid)
          //    //assert(locS.payload === past(ifwdPayload))
          //    //assert(locS.payload === past(ifwdPayload))
          //    assert(stable(locS.payload))
          //  }
          //  when (~past(ifwdValid) & ~locS.valid & past(ibakReady)) {
          //    assert(ofwdValid)
          //  }
          //  when (locS.valid & past(ibakReady)) {
          //    assert(~locS.valid)
          //  }
          //}
        }
      }
    }
    //--------
    //--------
  }
  //--------
}
