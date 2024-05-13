//package libcheesevoyage.gfx
//
//import libcheesevoyage.general.PipeHelper
//import libcheesevoyage.general.Vec2
//import libcheesevoyage.general.DualTypeNumVec2
//import libcheesevoyage.general.ElabVec2
//
//import spinal.core._
//import spinal.lib._
//import spinal.lib.graphic.Rgb
//import spinal.lib.graphic.RgbConfig
//import spinal.lib.misc.pipeline._
//import spinal.core.formal._
//import scala.collection.mutable.ArrayBuffer
////import scala.collection.immutable._
//import scala.math._
//
//case class Gpu2dScaleXIo(
//  params: Gpu2dParams,
//) extends Bundle {
//  val push = slave(Stream(Gpu2dPopPayload(params=params)))
//  val pop = master(Stream(Gpu2dPopPayload(params=params)))
//}
//case class Gpu2dScaleX(
//  params: Gpu2dParams,
//) extends Component {
//  //--------
//  val io = Gpu2dScaleXIo(params=params)
//  //--------
//  //when (io.pop.fire) {
//  //}
//  val linkArr = PipeHelper.mkLinkArr()
//  val front = new Area {
//    val pipe = PipeHelper(linkArr=linkArr)
//
//    val cFront = pipe.addStage(
//      name="Front"
//    )
//    val cBack = pipe.addStage(
//      name="Back"
//    )
//    val cLast = pipe.addStage(
//      name="Last",
//      finish=true,
//    )
//    //def mkScaleCnt2d() = (
//    //  DualTypeNumVec2(
//    //    dataTypeX=UInt(log2Up(params.physFbSize2dScale.x) bits),
//    //    dataTypeY=UInt(log2Up(params.physFbSize2dScale.x) bits),
//    //  )
//    //)
//    def mkScaleXCnt() = (
//      SInt(log2Up(params.physFbSize2dScale.x) + 2 bits)
//    )
//    //val scaleXCnt = Payload(mkScaleXCnt())
//    //val scaleCnt2d = Payload(mkScaleCnt2d())
//    //val rScaleCnt2d = Reg(mkScaleCnt2d()) init(mkScaleCnt2d().getZero)
//    val myPushPayload = Payload(cloneOf(io.push.payload))
//    val myPopPayload = Payload(cloneOf(io.pop.payload))
//    pipe.first.up.driveFrom(
//      io.push
//    )(
//      con=(node, payload) => {
//        node(myPushPayload) := payload
//      }
//    )
//    pipe.last.down.driveTo(
//      io.pop
//    )(
//      con=(payload, node) => {
//        payload := node(myPopPayload)
//      }
//    )
//    //--------
//    val cFrontArea = new cFront.Area {
//      val nextDuplicateIt = Bool()
//      val rDuplicateIt = RegNext(nextDuplicateIt, init=False)
//      nextDuplicateIt := rDuplicateIt
//      val nextScaleXCnt = mkScaleXCnt()
//      val rScaleXCnt = (
//        RegNext(nextScaleXCnt/*, init=nextScaleXCnt.getZero*/)
//        //init(params.physFbSize2d.x - 1)
//        init(-1)
//      )
//      nextScaleXCnt := rScaleXCnt
//      when (!rDuplicateIt) {
//        when (up.isValid) {
//          when (
//            rScaleXCnt.msb
//          ) {
//            duplicateIt()
//            nextDuplicateIt := True
//            nextScaleXCnt := params.physFbSize2dScale.x - 2
//          }
//        }
//      } otherwise { // when (rDuplicateIt)
//        when (down.isFiring) {
//          nextScaleXCnt := rScaleXCnt - 1
//        }
//        when (nextScaleXCnt.msb) {
//          nextDuplicateIt := False
//        } otherwise {
//          duplicateIt()
//        }
//      }
//      val tempPopPayload = cloneOf(io.pop.payload)
//      tempPopPayload := (
//        RegNext(tempPopPayload, init=tempPopPayload.getZero)
//      )
//      when (up.isValid) {
//        tempPopPayload := up(myPushPayload)
//      }
//      up(myPopPayload) := tempPopPayload
//    }
//    val cBackArea = new cBack.Area {
//    }
//  }
//  Builder(linkArr.toSeq)
//  //--------
//}
