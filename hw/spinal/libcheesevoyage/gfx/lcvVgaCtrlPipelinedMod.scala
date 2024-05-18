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

case class LcvVgaCtrlPipelined(
  clkRate: HertzNumber,
  rgbConfig: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  fifoDepth: Int,
  fifoArrRamStyle: String="auto",
  //fifoArrRamStyle: String="block",
  vivadoDebug: Boolean=false,
) extends Component {
  //--------
  val io = LcvVgaCtrlIo(
    clkRate=clkRate,
    rgbConfig=rgbConfig,
    vgaTimingInfo=vgaTimingInfo,
    fifoDepth=fifoDepth,
    optIncludeMiscVgaStates=true,
  )
  //--------
  def push = io.push
  def phys = io.phys
  def misc = io.misc
  phys := (
    RegNext(phys) init(phys.getZero)
  )
  phys.allowOverride
  misc := (
    RegNext(misc) //init(misc.getZero)
  )
  misc.allowOverride
  misc.visib := (
    RegNext(misc.visib) init(misc.visib.getZero)
  )
  //misc.drawPos := (
  //  RegNext(misc.drawPos) init(misc.drawPos.getZero)
  //)
  misc.pixelEn := (
    RegNext(misc.pixelEn) init(misc.pixelEn.getZero)
  )
  //--------
  def cpp = LcvVgaCtrl.cpp(clkRate=clkRate, vgaTimingInfo=vgaTimingInfo)
  def htiming: LcvVgaTimingHv = {
    return vgaTimingInfo.htiming
  }
  def vtiming: LcvVgaTimingHv = {
    return vgaTimingInfo.vtiming
  }
  //--------
  def fbSize2d: ElabVec2[Int] = vgaTimingInfo.fbSize2d
  //def clkCntWidth: Int = {
  //  return log2Up(cpp)
  //}
  def clkCntWidth = (
    LcvVgaCtrl.clkCntWidth(
      clkRate=clkRate,
      vgaTimingInfo=vgaTimingInfo
    ) + 2
  )
  //--------
  val loc = new Area {
    //--------
    val fifo = AsyncReadFifo(
      dataType=Rgb(c=rgbConfig),
      depth=fifoDepth,
      arrRamStyle=fifoArrRamStyle,
    )

    def fifoPush = fifo.io.push
    def fifoPop = fifo.io.pop
    def fifoEmpty = fifo.io.misc.empty
    def fifoFull = fifo.io.misc.full
    def fifoAmountCanPush = fifo.io.misc.amountCanPush
    def fifoAmountCanPop = fifo.io.misc.amountCanPop
    fifoPush <-/< push
    //--------
    //case class PipePayload() extends Bundle {
    //  val col = Rgb(c=rgbConfig)
    //}
    val linkArr = PipeHelper.mkLinkArr()
    //val hpipe = PipeHelper(linkArr=linkArr)
    //val vpipe = PipeHelper(linkArr=linkArr)
    //val fifoPipe = new Area {
    //  val pipe = PipeHelper(linkArr=linkArr)
    //  val calcPos = LcvVideoCalcPos(
    //    someSize2d=ElabVec2[Int](
    //      x=vgaTimingInfo.htiming.calcSum(),
    //      y=vgaTimingInfo.vtiming.calcSum(),
    //    )
    //  )
    //  val payload = new Area {
    //    //--------
    //    val col = Payload(Rgb(c=rgbConfig))
    //    //--------
    //  }
    //  val cFifoFront = pipe.addStage(
    //    name="FifoFront",
    //  )
    //  val cFifoBack = pipe.addStage(
    //    name="FifoBack",
    //  )
    //  val cFifoLast = pipe.addStage(
    //    name="FifoLast",
    //    finish=true,
    //  )
    //  pipe.first.up.driveFrom(
    //    push
    //  )(
    //    con=(node, pushPayload) => {
    //      node(payload.col) := pushPayload
    //    }
    //  )
    //  val cFifoFrontArea = new cFifoFront.Area {
    //    //calcPos.io.en := up.isFiring
    //  }
    //  val cFifoBackArea = new cFifoBack.Area {
    //    
    //  }
    //}
    val mainArea = new Area {
      val pipe = PipeHelper(linkArr=linkArr)

      val payload = new Area {
        //--------
        val col = Payload(Rgb(c=rgbConfig))
        val hsync = Payload(Bool())
        val vsync = Payload(Bool())
        val clkCnt = Payload(SInt(clkCntWidth bits))
        //--------
        val hCnt = Payload(SInt(
          log2Up(
            vgaTimingInfo.htiming.visib
            + vgaTimingInfo.htiming.front
            + vgaTimingInfo.htiming.sync
            + vgaTimingInfo.htiming.back
          ) + 2 bits
        ))
        val hCntOverflow = Payload(Bool())
        val hIsFront = Payload(Bool())
        val hIsSync = Payload(Bool())
        val hIsBack = Payload(Bool())
        val hIsVisib = Payload(Bool())
        val hState = Payload(LcvVgaState())
        //--------
        val vCnt = Payload(SInt(
          log2Up(
            vgaTimingInfo.vtiming.visib
            + vgaTimingInfo.vtiming.front
            + vgaTimingInfo.vtiming.sync
            + vgaTimingInfo.vtiming.back
          ) + 2 bits
        ))
        val vIsFront = Payload(Bool())
        val vIsSync = Payload(Bool())
        val vIsBack = Payload(Bool())
        val vIsVisib = Payload(Bool())
        val vState = Payload(LcvVgaState())
        //--------
        //val misc = Payload(LcvVgaCtrlMiscIo(
        //  clkRate=clkRate,
        //  vgaTimingInfo=vgaTimingInfo,
        //  fifoDepth=fifoDepth
        //))
        //--------
      }
      val cFront = pipe.addStage(
        name="Front"
      )
      val cHIncCnt = pipe.addStage(
        name="cHIncCnt",
        optIncludeS2M=false,
      )
      val cHCntCmp = pipe.addStage(
        name="HCntCmp",
        optIncludeS2M=false,
      )
      val cHFsm = pipe.addStage(
        name="HFsm",
        optIncludeS2M=false,
      )
      val cVIncCnt = pipe.addStage(
        name="cVIncCnt",
        optIncludeS2M=false,
      )
      val cVCntCmp = pipe.addStage(
        name="VCntCmp",
        optIncludeS2M=false,
      )
      val cVFsm = pipe.addStage(
        name="VFsm",
        optIncludeS2M=false,
      )
      val cLast = pipe.addStage(
        name="Last",
        optIncludeS2M=false,
        finish=true,
      )
      pipe.first.up.driveFrom(fifoPop)(
        con=(node, popPayload) => {
          node(payload.col) := popPayload
        }
      )
      //pipe.last.down.driveTo()(
      //)
      //--------
      val cFrontArea = new cFront.Area {
        //val rClkCnt = Reg(SInt(clkCntWidth bits)) init(cpp)
        val nextClkCnt = SInt(clkCntWidth bits)
        val rClkCnt = RegNext(nextClkCnt) init(cpp - 2)
        nextClkCnt := rClkCnt
        up(payload.clkCnt) := rClkCnt

        val nextDuplicateIt = Bool()
        val rDuplicateIt = (
          RegNext(nextDuplicateIt) init(False)
        )
        //val rHState = Reg(LcvVgaState()) init(LcvVgaState.front)
        //val rVState = Reg(LcvVgaState()) init(LcvVgaState.front)
        nextDuplicateIt := rDuplicateIt
        //if (cpp > 1) {
          when (!rDuplicateIt) {
            when (up.isValid) {
              if (cpp > 1) {
                duplicateIt()
                nextDuplicateIt := True
              }
              nextClkCnt := cpp - 2
            }
          } otherwise { // when (rDuplicateIt)
            when (down.isFiring) {
              nextClkCnt := rClkCnt - 1
            }
            when (nextClkCnt.msb) {
              nextDuplicateIt := False
            } otherwise {
              duplicateIt()
            }
          }
        //}
      }
      val cHIncCntArea = new cHIncCnt.Area {
        val nextHCnt = SInt(up(payload.hCnt).getWidth bits)
        val rHCnt = RegNext(nextHCnt) init(
          //nextHCnt.getZero
          -1
        )
        nextHCnt := rHCnt

        //up(payload.hCntOverflow) := down(payload.hCntOverflow)
        //up(payload.hCnt) := down(payload.hCnt)
        up(payload.hCntOverflow) := (
          rHCnt + 1
          >= (
            vgaTimingInfo.htiming.visib
            + vgaTimingInfo.htiming.front
            + vgaTimingInfo.htiming.sync
            + vgaTimingInfo.htiming.back
          )
        )

        up(payload.hCnt) := nextHCnt

        when (
          up.isFiring
          && up(payload.clkCnt).msb
        ) {
          when (
            rHCnt + 1
            >= (
              vgaTimingInfo.htiming.visib
              + vgaTimingInfo.htiming.front
              + vgaTimingInfo.htiming.sync
              + vgaTimingInfo.htiming.back
            )
          ) {
            nextHCnt := 0
          } otherwise {
            nextHCnt := rHCnt + 1
          }
        }
        //when (up.isFiring) {
        //}
      }
      val cHCntCmpArea = new cHCntCmp.Area {
        //up(payload.hIsFront) := down(payload.hIsFront)
        //up(payload.hIsSync) := down(payload.hIsSync)
        //up(payload.hIsBack) := down(payload.hIsBack)
        //up(payload.hIsVisib) := down(payload.hIsVisib)
        val myHIsFront = Bool()
        myHIsFront := RegNext(myHIsFront) init(False)
        val myHIsSync = Bool()
        myHIsSync := RegNext(myHIsSync) init(False)
        val myHIsBack = Bool()
        myHIsBack := RegNext(myHIsBack) init(False)
        val myHIsVisib = Bool()
        myHIsVisib := RegNext(myHIsVisib) init(False)

        up(payload.hIsFront) := myHIsFront
        up(payload.hIsSync) := myHIsSync
        up(payload.hIsBack) := myHIsBack
        up(payload.hIsVisib) := myHIsVisib

        when (
          up.isValid
          && up(payload.clkCnt).msb
        ) {
          //--------
          var limGe: Int = 0
          def limLe(plusAmount: Int): Int = limGe + plusAmount - 1

          myHIsFront := (
            up(payload.hCnt) >= limGe
            && up(payload.hCnt) <= limLe(vgaTimingInfo.htiming.front)
          )
          limGe = limLe(vgaTimingInfo.htiming.front) + 1

          myHIsSync := (
            up(payload.hCnt) >= limGe
            && up(payload.hCnt) <= limLe(vgaTimingInfo.htiming.sync)
          )
          limGe = limLe(vgaTimingInfo.htiming.sync) + 1

          myHIsBack := (
            up(payload.hCnt) >= limGe
            && up(payload.hCnt) <= limLe(vgaTimingInfo.htiming.back)
          )
          limGe = limLe(vgaTimingInfo.htiming.back) + 1

          myHIsVisib := (
            up(payload.hCnt) >= limGe
            && up(payload.hCnt) <= limLe(vgaTimingInfo.htiming.visib)
          )
          limGe = limLe(vgaTimingInfo.htiming.visib) + 1
        }
      }
      val cHFsmArea = new cHFsm.Area {
        //val nextHstate = LcvVgaState()
        //val rHstate = (
        //  RegNext(nextHstate) init(LcvVgaState.front)
        //)
        //up(payload.hState) := down(payload.hState)
        //up(payload.hsync) := down(payload.hsync)
        val myHState = LcvVgaState()
        myHState := RegNext(myHState) init(LcvVgaState.front)
        val myHsync = Bool()
        myHsync := RegNext(myHsync) init(False)
        up(payload.hState) := myHState
        up(payload.hsync) := myHsync
        when (
          up.isValid
          && up(payload.clkCnt).msb
        ) {
          switch (
            Cat(
              up(payload.hIsFront),
              up(payload.hIsSync),
              up(payload.hIsBack),
              up(payload.hIsVisib),
            )
          ) {
            is (M"1000") {
              myHState := LcvVgaState.front
              myHsync := True
            }
            is (M"-100") {
              myHState := LcvVgaState.sync
              myHsync := False
            }
            is (M"--10") {
              myHState := LcvVgaState.back
              myHsync := True
            }
            //is (M"---1") {
            //  myHState := LcvVgaState.visib
            //  myHsync := True
            //}
            //is (M"---1")
            default {
              myHState := LcvVgaState.visib
              myHsync := True
            }
          }
        }
      }
      val cVIncCntArea = new cVIncCnt.Area {
        val nextVCnt = SInt(up(payload.vCnt).getWidth bits)
        val rVCnt = RegNext(nextVCnt) init(nextVCnt.getZero)
        nextVCnt := rVCnt
        up(payload.vCnt) := nextVCnt //down(payload.vCnt)
        when (
          up.isFiring
          && up(payload.clkCnt).msb
        ) {
          when (up(payload.hCntOverflow)) {
            when (
              rVCnt + 1
              >= (
                vgaTimingInfo.vtiming.visib
                + vgaTimingInfo.vtiming.front
                + vgaTimingInfo.vtiming.sync
                + vgaTimingInfo.vtiming.back
              )
            ) {
              nextVCnt := 0
            } otherwise {
              nextVCnt := rVCnt + 1
            }
          }
        }
        //when (up.isValid) {
        //  //up(payload.vCntOverflow) := (nextVCnt === 0)
        //  up(payload.vCnt) := nextVCnt
        //}
      }
      val cVCntCmpArea = new cVCntCmp.Area {
        //up(payload.vIsFront) := down(payload.vIsFront)
        //up(payload.vIsSync) := down(payload.vIsSync)
        //up(payload.vIsBack) := down(payload.vIsBack)
        //up(payload.vIsVisib) := down(payload.vIsVisib)
        val myVIsFront = Bool()
        myVIsFront := RegNext(myVIsFront) init(False)
        val myVIsSync = Bool()
        myVIsSync := RegNext(myVIsSync) init(False)
        val myVIsBack = Bool()
        myVIsBack := RegNext(myVIsBack) init(False)
        val myVIsVisib = Bool()
        myVIsVisib := RegNext(myVIsVisib) init(False)

        up(payload.vIsFront) := myVIsFront
        up(payload.vIsSync) := myVIsSync
        up(payload.vIsBack) := myVIsBack
        up(payload.vIsVisib) := myVIsVisib

        when (
          up.isValid
          && up(payload.clkCnt).msb
        ) {
          //--------
          var limGe: Int = 0
          def limLe(plusAmount: Int): Int = limGe + plusAmount - 1

          myVIsFront := (
            up(payload.vCnt) >= limGe
            && up(payload.vCnt) <= limLe(vgaTimingInfo.vtiming.front)
          )
          limGe = limLe(vgaTimingInfo.vtiming.front) + 1

          myVIsSync := (
            up(payload.vCnt) >= limGe
            && up(payload.vCnt) <= limLe(vgaTimingInfo.vtiming.sync)
          )
          limGe = limLe(vgaTimingInfo.vtiming.sync) + 1

          myVIsBack := (
            up(payload.vCnt) >= limGe
            && up(payload.vCnt) <= limLe(vgaTimingInfo.vtiming.back)
          )
          limGe = limLe(vgaTimingInfo.vtiming.back) + 1

          myVIsVisib := (
            up(payload.vCnt) >= limGe
            && up(payload.vCnt) <= limLe(vgaTimingInfo.vtiming.visib)
          )
          limGe = limLe(vgaTimingInfo.vtiming.visib) + 1
        }
      }
      val cVFsmArea = new cVFsm.Area {
        //up(payload.vState) := down(payload.vState)
        //up(payload.vsync) := down(payload.vsync)
        val myVState = LcvVgaState()
        myVState := RegNext(myVState) init(LcvVgaState.front)
        val myVsync = Bool()
        myVsync := RegNext(myVsync) init(False)
        up(payload.vState) := myVState
        up(payload.vsync) := myVsync
        when (
          up.isValid
          && up(payload.clkCnt).msb
        ) {
          //when (up(payload.clkCnt).msb) {
          //}
          switch (
            Cat(
              up(payload.vIsFront),
              up(payload.vIsSync),
              up(payload.vIsBack),
              up(payload.vIsVisib),
            )
          ) {
            is (M"1000") {
              myVState := LcvVgaState.front
              myVsync := True
            }
            is (M"-100") {
              myVState := LcvVgaState.sync
              myVsync := False
            }
            is (M"--10") {
              myVState := LcvVgaState.back
              myVsync := True
            }
            //is (M"---1") {
            //  myVState := LcvVgaState.visib
            //  myVsync := True
            //}
            //is (M"---1")
            default {
              myVState := LcvVgaState.visib
              myVsync := True
            }
          }
        }
      }
      val cLastArea = new cLast.Area {
        //when (up.isFiring) {
          when (misc.visib) {
            phys.col := up(payload.col)
            misc.drawPos.x := (
              up(payload.hCnt)
              - vgaTimingInfo.htiming.calcNonVisibSum()
            )(
              misc.drawPos.x.bitsRange
            ).asUInt
            misc.drawPos.y := (
              up(payload.vCnt)
              - vgaTimingInfo.vtiming.calcNonVisibSum()
            )(
              misc.drawPos.y.bitsRange
            ).asUInt
          } otherwise {
            phys.col := phys.col.getZero
            misc.drawPos := misc.drawPos.getZero
          }
        //}
        //when (up.isValid) {
        //  phys.hsync := up(payload.hsync)
        //  phys.vsync := up(payload.vsync)
        //}
        //misc.visib := up(payload.hIsVisib) && up(payload.vIsVisib)
        //misc.pixelEn := up(payload.clkCnt).msb
        when (up.isValid) {
          phys.hsync := up(payload.hsync)
          phys.vsync := up(payload.vsync)
          misc.visib := up(payload.hIsVisib) && up(payload.vIsVisib)
          misc.pixelEn := up(payload.clkCnt).msb
        }
      }
    }
    //--------
    Builder(linkArr.toSeq)
  }
  //--------
}
