package libcheesevoyage.gfx
import libcheesevoyage.general.PipeHelper
import libcheesevoyage.general.FifoMiscIo
import libcheesevoyage.general.FifoIo
//import libcheesevoyage.general.Fifo
import libcheesevoyage.general.AsyncReadFifo
//import libcheesevoyage.general.Vec2
import libcheesevoyage.general.DualTypeVec2
import libcheesevoyage.general.DualTypeNumVec2
import libcheesevoyage.general.ElabVec2
import libcheesevoyage.general.PipeSkidBuf
import libcheesevoyage.general.PipeSkidBufIo

import spinal.core._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import spinal.core.formal._
import spinal.lib.misc.pipeline._
import scala.collection.mutable.ArrayBuffer
import scala.math._

case class Gpu2dBlankingIo(
  params: Gpu2dParams,
  vgaTimingInfo: LcvVgaTimingInfo,
) extends Bundle {
  //--------
  val push = slave Stream(
    Rgb(c=params.rgbConfig)
  )
  val pop = master Stream(Rgb(c=params.rgbConfig))
  //--------
}
case class Gpu2dBlanking(
  params: Gpu2dParams,
  vgaTimingInfo: LcvVgaTimingInfo,
) extends Component {
  //--------
  val io = Gpu2dBlankingIo(
    params=params,
    vgaTimingInfo=vgaTimingInfo
  )
  //--------
  val calcPos = LcvVideoCalcPos(
    someSize2d=ElabVec2[Int](
      x=vgaTimingInfo.htiming.calcSum(),
      y=vgaTimingInfo.vtiming.calcSum(),
    )
  )
  val linkArr = PipeHelper.mkLinkArr()
  val pipe = PipeHelper(linkArr=linkArr)
  //--------
  val cFront = pipe.addStage(
    name="Front",
  )
  val cBack = pipe.addStage(
    name="Back",
  )
  val cLast = pipe.addStage(
    name="Last",
    finish=true,
  )
  //def clkCntWidth = (
  //  LcvVgaCtrl.clkCntWidth(
  //    clkRate=clkRate,
  //    vgaTimingInfo=vgaTimingInfo
  //  ) + 2
  //)
  val payload = new Area {
    val inpCol = Payload(Rgb(c=params.rgbConfig))
    val outpCol = Payload(Rgb(c=params.rgbConfig))
    //val doBlankCol = Payload(Bool())

    //val clkCnt = Payload(SInt(clkCntWidth bits))
  }
  pipe.first.up.driveFrom(
    io.push
  )(
    con=(node, pushPayload) => {
      node(payload.inpCol) := pushPayload
    }
  )
  pipe.last.down.driveTo(
    io.pop
  )(
    con=(popPayload, node) => {
      popPayload := node(payload.outpCol)
    }
  )
  val cFrontArea = new cFront.Area {
    calcPos.io.en := down.isFiring
    //calcPos.io.en := pipe.last.down.isFiring
    //calcPos.io.en := cBack.up.isFiring
    //calcPos.io.en := cBack.down.isFiring
    //calcPos.io.en := RegNext(up.isFiring) init(False)
    //calcPos.io.en := down.isFiring

    val nextDuplicateIt = Bool()
    val rDuplicateIt = RegNext(nextDuplicateIt) init(False)
    nextDuplicateIt := rDuplicateIt

    //up(payload.doBlankCol) := nextDuplicateIt
    //when (up.isValid) {
    up(payload.outpCol) := up(payload.inpCol)
    //}

    //val nextZeroIt = Bool()
    //val rZeroIt = RegNext(nextZeroIt) init(nextZeroIt.getZero)
    //nextZeroIt := rZeroIt
    //when (!rZeroIt) {
    //  when (
    //    up.isValid
    //    && (
    //      (
    //        calcPos.io.info.nextPos.x + 1
    //          < vgaTimingInfo.htiming.calcNonVisibSum() + 1
    //        || calcPos.info.pos.y < vgaTimingInfo.vtiming.calcNonVisibSum()
    //      )
    //    )
    //  ) {
    //    nextZeroIt := True
    //    up(payload.outpCol) := up(payload.inpCol).getZero
    //  }
    //} otherwise { // when (rZeroIt)
    //  when (
    //    //calcPos.io.info.nextPos.x + 1
    //    calcPos.io.info.nextPos.x + 1
    //      >= vgaTimingInfo.htiming.calcNonVisibSum() + 1
    //    && calcPos.io.info.pos.y >= vgaTimingInfo.vtiming.calcNonVisibSum()
    //  ) {
    //    nextZeroIt := False
    //  } otherwise {
    //    up(payload.outpCol) := up(payload.inpCol).getZero
    //  }
    //}

    when (!rDuplicateIt) {
      when (up.isValid) {
        when (
          //--------
          //calcPos.io.info.nextPos.x + 1
          //--------
          //calcPos.io.info.nextPos.x
          //  < vgaTimingInfo.htiming.calcNonVisibSum()
          //(
          //  calcPos.io.info.pos.x
          //    < vgaTimingInfo.htiming.calcNonVisibSum()
          //) || (
          //  calcPos.info.pos.y < vgaTimingInfo.vtiming.calcNonVisibSum()
          //)

          //(
          //  calcPos.io.info.pos.x
          //    >= vgaTimingInfo.htiming.visib
          //) || (
          //  calcPos.info.pos.y >= vgaTimingInfo.vtiming.visib
          //)
          (
            RegNext(calcPos.io.info.posWillOverflow.x) init(False)
          )
          //&& (
          //  calcPos.io.info.posWillOverflow.y
          //)
          || (
            calcPos.info.pos.y < vgaTimingInfo.vtiming.calcNonVisibSum()
          )
          //--------
        ) {
          duplicateIt()
          nextDuplicateIt := True
          //up(payload.outpCol) := up(payload.inpCol).getZero
        }
      }
    } otherwise { // when (rDuplicateIt)
      //when (calcPos.io.en) {
      //}
      when (
        //--------
        //calcPos.io.info.nextPos.x + 1
        //--------
        //(
        //  calcPos.io.info.nextPos.x
        //    >= vgaTimingInfo.htiming.calcNonVisibSum()
        //) 
        //&& 
        (
          calcPos.io.info.pos.x
            >= vgaTimingInfo.htiming.calcNonVisibSum()
          //calcPos.io.info.pos.x
          //< vgaTimingInfo.htiming.visib
        ) && (
          calcPos.io.info.pos.y >= vgaTimingInfo.vtiming.calcNonVisibSum()
          //calcPos.io.info.pos.y < vgaTimingInfo.vtiming.visib
        )
        //--------
      ) {
        nextDuplicateIt := False
      } otherwise {
        //up(payload.outpCol) := up(payload.inpCol).getZero
        duplicateIt()
      }
    }
  }
  //val cBackArea = new cBack.Area {
  //  val tempOutpCol = Rgb(c=params.rgbConfig)
  //  tempOutpCol := RegNext(tempOutpCol) init(tempOutpCol.getZero)
  //  up(payload.outpCol) := tempOutpCol
  //  //when (up.isValid) {
  //  //  when (up(payload.doBlankCol)) {
  //  //    tempOutpCol := tempOutpCol.getZero
  //  //  } otherwise {
  //  //    tempOutpCol := up(payload.inpCol)
  //  //  }
  //  //}
  //}
  //--------
  Builder(linkArr.toSeq)
  //--------
}
