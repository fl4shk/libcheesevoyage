//package libcheesevoyage.general
//
//import spinal.core._
//import spinal.core.formal._
//import spinal.lib._
//import spinal.lib.misc.pipeline._
//import spinal.lib.bus.bmb._
//import scala.collection.mutable.ArrayBuffer
//import scala.math._
//
//import libcheesevoyage.Config
//
//case class BmbHostCmdPipePayload(
//  p: BmbParameter,
//  //noCache: Boolean,
//) extends Bundle {
//  assert(p.access.canRead || p.access.canWrite)
//  //val param = BmbParameter(access=access)
//  val cmd = /*Stream*/(Fragment(BmbCmd(p=p)))
//}
//case class BmbHostRspPipePayload(
//  p: BmbParameter,
//  //noCache: Boolean,
//) extends Bundle {
//  assert(p.access.canRead || p.access.canWrite)
//  //val param = BmbParameter(access=access)
//  val rsp = /*Stream*/(Fragment(BmbRsp(p=p)))
//}
//// A pipelined Banana Memory Bus host
//case class BmbHostNoCache(
//  access: BmbAccessParameter,
//  //invalidation: Option[BmbInvalidationParameter]=None,
//  linkArr: Option[ArrayBuffer[Link]],
//) extends Area {
//  assert(access.canRead || access.canWrite)
//  //--------
//  val bus = /*invalidation match*/ {
//  //  case Some(myInvalidation) => (
//  //    Bmb(
//  //      access=access,
//  //      invalidation=myInvalidation,
//  //    )
//  //  )
//  //  case None => (
//      Bmb(
//        access=access,
//      )
//  //  )
//  }
//  //--------
//  val myLinkArr = linkArr match {
//    case Some(theLinkArr) => (
//      theLinkArr
//    )
//    case None => (
//      PipeHelper.mkLinkArr()
//    )
//  }
//  val io = new Area {
//    val cmd = Stream(BmbHostCmdPipePayload(
//      p=BmbParameter(access=access),
//      //noCache=true
//    ))
//    val rsp = Stream(BmbHostRspPipePayload(
//      p=BmbParameter(access=access),
//      //noCache=true
//    ))
//  }
//  //val linkArr = PipeHelper.mkLinkArr()
//  val cmd = new Area {
//    val pipe = PipeHelper(linkArr=myLinkArr)
//    val pipePayload = Payload(BmbHostCmdPipePayload(
//      p=BmbParameter(access=access),
//      //noCache=true,
//    ))
//    val cCmdFront = pipe.addStage(
//      name="CmdFront"
//    )
//    val cCmdLast = pipe.addStage(
//      name="CmdLast",
//      finish=true
//    )
//    pipe.first.up.driveFrom(io.cmd)(
//      con=(node, payload) => {
//        node(pipePayload) := payload
//      }
//    )
//    pipe.last.down.driveTo(bus.cmd)(
//      con=(payload, node) => {
//        payload := node(pipePayload).cmd
//      }
//    )
//  }
//  val rsp = new Area {
//    val pipe = PipeHelper(linkArr=myLinkArr)
//    val pipePayload = Payload(BmbHostRspPipePayload(
//      p=BmbParameter(access=access),
//      //noCache=true,
//    ))
//    val cRspFront = pipe.addStage(
//      name="RspFront"
//    )
//    val cRspLast = pipe.addStage(
//      name="RspLast",
//      finish=true
//    )
//    pipe.first.up.driveFrom(bus.rsp)(
//      con=(node, payload) => {
//        node(pipePayload).rsp := payload
//      }
//    )
//    pipe.last.down.driveTo(io.rsp)(
//      con=(payload, node) => {
//        payload := node(pipePayload)
//      }
//    )
//  }
//  val cCmdFrontArea = new cmd.cCmdFront.Area {
//    def pipePayload = cmd.pipePayload
//  }
//  val cRspFrontArea = new rsp.cRspFront.Area {
//    def pipePayload = rsp.pipePayload
//  }
//  //--------
//}
