package libcheesevoyage.gfx
import libcheesevoyage._

import libcheesevoyage.general.PipeMemRmw
import libcheesevoyage.general.PipeMemRmwIo
import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2
import libcheesevoyage.hwdev.SnesCtrlIo
import libcheesevoyage.hwdev.SnesButtons

import spinal.core._
//import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.graphic.vga._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import scala.collection.mutable.ArrayBuffer
import scala.math._

case class Gpu2dSimDut(
  clkRate: HertzNumber,
  rgbConfig: RgbConfig,
  vgaTimingInfo: LcvVgaTimingInfo,
  gpu2dParams: Gpu2dParams,
  ctrlFifoDepth: Int,
  optRawSnesButtons: Boolean=false,
  dbgPipeMemRmw: Boolean=(
    //true
    false
  ),
) extends Component {
  val io = new Bundle {
    val snesCtrl = (!optRawSnesButtons) generate SnesCtrlIo()
    val rawSnesButtons = (optRawSnesButtons) generate (
      slave Stream(UInt(SnesButtons.rawButtonsWidth bits))
    )
    //val phys = out(LcvVgaPhys(rgbConfig=physRgbConfig))
    val phys = out(LcvVgaPhys(rgbConfig=rgbConfig))
    val misc = out(LcvVgaCtrlMiscIo(
      clkRate=clkRate,
      vgaTimingInfo=vgaTimingInfo,
      fifoDepth=ctrlFifoDepth,
    ))
  }
  def myVgaTimingsWidth = 12
  //--------
  val vgaCtrl = LcvVgaCtrlPipelined(
    clkRate=clkRate,
    //rgbConfig=physRgbConfig,
    rgbConfig=rgbConfig,
    vgaTimingInfo=vgaTimingInfo,
    fifoDepth=ctrlFifoDepth,
  )
  //val vgaCtrl = VgaCtrl(
  //  rgbConfig=gpu2dParams.rgbConfig
  //)
  def ctrlIo = vgaCtrl.io
  //--------
  // BEGIN: `VgaCtrl` stuff
  //ctrlIo.softReset := RegNext(False) init(True)
  //vgaTimingInfo.driveSpinalVgaTimings(
  //  clkRate=clkRate,
  //  spinalVgaTimings=ctrlIo.timings,
  //)
  // END: `VgaCtrl` stuff
  //--------
  //ctrlIo.timings.h.colorStart := 0
  //ctrlIo.timings.h.colorEnd := vgaTimingInfo.htiming.visib - 1
  //ctrlIo.timings.h.syncStart := vgaTimingInfo.htiming.visib
  //ctrlIo.timings.h.syncEnd := (
  //  vgaTimingInfo.htiming.visib
  //  + vgaTimingInfo.htiming.front
  //  + vgaTimingInfo.htiming.sync
  //  + vgaTimingInfo.htiming.back
  //  - 1
  //)
  //ctrlIo.timings.h.polarity := True
  //ctrlIo.timings.v.colorStart := 0
  //ctrlIo.timings.v.colorEnd := vgaTimingInfo.vtiming.visib - 1
  //ctrlIo.timings.v.syncStart := vgaTimingInfo.vtiming.visib
  //ctrlIo.timings.v.syncEnd := (
  //  vgaTimingInfo.vtiming.visib
  //  + vgaTimingInfo.vtiming.front
  //  + vgaTimingInfo.vtiming.sync
  //  + vgaTimingInfo.vtiming.back
  //  - 1
  //)
  //ctrlIo.timings.v.polarity := True
  //--------
  //val vgaCtrl = VgaCtrl(
  //  rgbConfig=rgbConfig,
  //  timingsWidth=myVgaTimingsWidth,
  //)
  //val vidDith = LcvVideoDitherer(
  //  //fbSize2d=fbSize2d,
  //  rgbConfig=rgbConfig,
  //  //vgaTimingInfo=vgaTimingInfo,
  //  //fbSize2d=vgaTimingInfo.fbSize2d,
  //  fbSize2d=vgaTimingInfo.fbSize2d,
  //)

  //val vgaCtrl = VgaCtrl(
  //  rgbConfig=rgbConfig,
  //  timingsWidth=myVgaTimingsWidth,
  //)
  //def dbgPipeMemRmw = (
  //  true
  //  //false
  //)
  val gpu2d = Gpu2d(
    params=gpu2dParams,
    inSim=true,
    dbgPipeMemRmw=dbgPipeMemRmw,
  )
  //val gpu2dScaleX = Gpu2dScaleX(
  //  params=gpu2dParams,
  //)
  val gpu2dTest = Gpu2dTest(
    clkRate=clkRate,
    params=gpu2dParams,
    optRawSnesButtons=optRawSnesButtons,
    dbgPipeMemRmw=dbgPipeMemRmw,
  )
  if (!optRawSnesButtons) {
    io.snesCtrl <> gpu2dTest.io.snesCtrl
  } else { // if (optRawSnesButtons)
    gpu2dTest.io.rawSnesButtons << io.rawSnesButtons
  }
  //gpu2dTest.io.snesCtrl.inpData := False
  //io.snesCtrl.outpClk := False
  //io.snesCtrl.outpLatch := False

  //val ctrlIo = vgaCtrl.io
  //val dithIo = vidDith.io
  val gpuIo = gpu2d.io
  //gpu2dTest.io.vgaPhys := ctrlIo.phys
  gpu2dTest.io.gpu2dPopFire := (
    //gpuIo.pop.ready
    gpuIo.pop.fire
    //gpu2dScaleX.io.pop.fire
  )
  val myGpuPopStm = cloneOf(gpuIo.pop)

  gpu2dTest.io.vgaSomeVpipeS := (
    RegNext(gpu2dTest.io.vgaSomeVpipeS) init(LcvVgaState.front)
  )
  when (
    myGpuPopStm.valid
  ) {
    when (
      //&& gpuIo.pop.physPosInfo.nextPos.x =/= 0
      myGpuPopStm.physPosInfo.nextPos.y === 0
    ) {
      gpu2dTest.io.vgaSomeVpipeS := (
        LcvVgaState.front
      )
    } otherwise {
      gpu2dTest.io.vgaSomeVpipeS := (
        LcvVgaState.visib
      )
    }
  }
  //gpu2dTest.io.vgaSomeVpipeS := ctrlIo.misc.vpipeSPipe2
  //--------

  //gpu2dTest.io.vgaSomeDrawPos := ctrlIo.misc.drawPos

  //val vgaTimingsH = VgaTimingsHV(timingsWidth=myVgaTimingsWidth)
  //vgaTimingsH.colorStart := vgaTimingInfo.htiming.
  //--------
  //vgaTimingInfo.driveSpinalVgaTimings(
  //  clkRate=clkRate,
  //  spinalVgaTimings=ctrlIo.timings,
  //)
  //--------

  //gpuIo <> gpu2dTest.io.gpuIo
  //gpuIo.push <> gpu2dTest.io.pop
  //gpu2dTest.io.pop <> gpuIo.push
  gpuIo.push << gpu2dTest.io.pop
  //--------
  // BEGIN: main code; later
  //ctrlIo.en := gpuIo.ctrlEn
  //ctrlIo.en := (
  //  //gpu2dScaleY.io.pop.fire
  //  //&& 
  //  //(
  //  //  RegNextWhen(
  //  //    True,
  //  //    gpu2dScaleY.io.pop.valid,
  //  //  ) init(False),
  //  //)
  //  //&& 
  //  //gpu2dScaleY.io.pop.ctrlEn
  //  True
  //  //True
  //  //gpu2dScaleY.io.pop.ctrlEn
  //  //gpuIo.pop.valid
  //  //&& 
  //  //gpuIo.pop.ctrlEn
  //)
  //ctrlIo.en := False

  //ctrlIo.push.valid := gpuIo.pop.valid
  //ctrlIo.push.payload := gpuIo.pop.payload.col
  //gpuIo.pop.ready := ctrlIo.push.ready
  //gpu2dScaleX.io.push << gpuIo.pop
  //gpuIo.pop.translateInto(
  //  //into=ctrlIo.push
  //  into=ctrlIo.pixels
  //)(
  //  dataAssignment=(
  //    ctrlPushPayload, gpuPopPayload
  //  ) => {
  //    ctrlPushPayload := gpuPopPayload.col
  //  }
  //)
  val gpu2dBlanking = Gpu2dBlanking(
    params=gpu2dParams,
    vgaTimingInfo=vgaTimingInfo,
  )
  myGpuPopStm <-/< gpuIo.pop
  //vgaCtrl.io.pixels <-/< myGpuPopStm
  //--------
  myGpuPopStm.translateInto(
    //into=vgaCtrl.io.pixels
    //into=vgaCtrl.io.push
    into=gpu2dBlanking.io.push,
  )(
    dataAssignment=(o, i) => {
      o := i.col
    }
  )
  vgaCtrl.io.push <-/< gpu2dBlanking.io.pop 
  //--------
  ////vgaCtrl.io.pixels << gpuIo.pop
  ////ctrlIo.pixels.valid := gpuIo.pop.valid
  ////ctrlIo.pixels.payload := gpuIo.pop.payload.col
  ////gpuIo.pop.ready := ctrlIo.pixels.ready
  // END: main code; later
  //--------
  //ctrlIo.pixels << gpuIo.pop
  //gpuIo.pop.translateInto(
  //  into=ctrlIo.pixels
  //)(
  //  dataAssignment=(pixelsPayload, gpuPopPayload) => {
  //    pixelsPayload := gpuPopPayload.col
  //  }
  //)

  //ctrlIo.push.valid := gpu2dTest.io.pop.valid
  //ctrlIo.push.payload := gpu2dTest.io.pop.payload.col
  //gpu2dTest.io.pop.ready := ctrlIo.push.ready
  //ctrlIo.push.valid := False
  //ctrlIo.push.payload := ctrlIo.push.payload.getZero
  //gpuIo.pop.ready := True

  //--------
  io.phys := ctrlIo.phys
  io.misc := ctrlIo.misc
  //--------
  // BEGIN: `VgaCtrl` stuff
  //io.phys.col := ctrlIo.vga.color
  //io.phys.hsync := ctrlIo.vga.hSync
  //io.phys.vsync := ctrlIo.vga.vSync
  //io.misc := io.misc.getZero
  //io.misc.allowOverride
  //io.misc.pastVisib := RegNext(io.misc.visib) init(False)
  //io.misc.visib := ctrlIo.vga.colorEn
  //val rPixelEnCnt = (
  //  Reg(UInt(log2Up((clkRate / vgaTimingInfo.pixelClk).toInt) + 1 bits))
  //  init(0x0)
  //)
  //when (rPixelEnCnt + 1 === (clkRate / vgaTimingInfo.pixelClk).toInt) {
  //  rPixelEnCnt := 0
  //} otherwise {
  //  rPixelEnCnt := rPixelEnCnt + 1
  //}
  //io.misc.pixelEn := rPixelEnCnt === 0x0
  // END: `VgaCtrl` stuff
  //--------
}

object Gpu2dSim extends App {
  //def clkRate = 125.0 MHz
  def clkRate = 50.0 MHz
  //def clkRate = 100.0 MHz
  //def clkRate = 100.7 MHz
  def pixelClk = 25.0 MHz
  //def ctrlFifoDepth = 20
  def ctrlFifoDepth = 256
  //def ctrlFifoDepth = 100
  //def ctrlFifoDepth = 128
  //def fbSize2d = ElabVec2[Int](640, 480)
  //def fbSize2d = ElabVec2[Int](1, 1)
  //def fbSize2d = ElabVec2[Int](20, 20)
  //def rgbConfig = RgbConfig(rWidth=6, gWidth=6, bWidth=6)
  def rgbConfig = RgbConfig(rWidth=4, gWidth=4, bWidth=4)
  ////def rgbConfig = RgbConfig(rWidth=4, gWidth=4, bWidth=4)
  //def physRgbConfig = LcvVideoDithererIo.outRgbConfig(rgbConfig=rgbConfig)
  ////def vgaTimingInfo = LcvVgaTimingInfoMap.map("640x480@60")
  def vgaTimingInfo=LcvVgaTimingInfo(
    pixelClk=pixelClk,
    //pixelClk=25.175 MHz,
    htiming=LcvVgaTimingHv(
      //visib=1 << 6,
      //visib=64,
      //visib=1 << 7,
      //visib=1 << 8,
      //visib=4,
      //visib=8,
      //--------
      //visib=1 << 6,
      //front=1,
      //sync=1,
      //back=1,
      //--------
      visib=(
        320
        //480
        //640
        //639
      ),
      //front=1,
      //sync=1,
      //back=1,
      front=40,
      //sync=40,
      //back=40,
      //front=4,
      sync=4,
      back=4,
      //--------
    ),
    vtiming=LcvVgaTimingHv(
      ////visib=1 << 3,
      ////visib=1 << 4,
      //visib=1 << 5,
      ////visib=1 << 7,
      ////visib=4,
      ////visib=8,
      //front=1,
      //sync=1,
      //back=1,
      visib=(
        //270
        //240
        //128
        //64
        //48
        32
        //16
      ),
      front=1,
      sync=1,
      back=1,
    ),
  )

  def fbSize2d = vgaTimingInfo.fbSize2d
  def gpu2dPhysFbSize2dScale = ElabVec2[Int](
    //x=1,
    //y=1,
    x=3,
    //x=1,
    //y=1,
    y=1,
    //y=1,
    //x=log2Up(2),
    ////y=log2Up(2),
    //y=log2Up(2),
  )
  def gpu2dIntnlFbSize2d = ElabVec2[Int](
      x=fbSize2d.x / gpu2dPhysFbSize2dScale.x,
      y=fbSize2d.y / gpu2dPhysFbSize2dScale.y,
    )
  def gpu2dParams = DefaultGpu2dParams(
    rgbConfig=rgbConfig,
    intnlFbSize2d=gpu2dIntnlFbSize2d,
    physFbSize2dScale=gpu2dPhysFbSize2dScale,
    //physFbSize2dScaleYPow=ElabVec2[Int](
    //  x=log2Up(1),
    //  y=log2Up(1),
    //  //x=log2Up(2),
    //  ////y=log2Up(2),
    //  //y=log2Up(2),
    //),
    bgTileSize2dPow=ElabVec2[Int](
      x=log2Up(16),
      y=log2Up(16),
      //x=log2Up(8),
      //y=log2Up(8),
      //x=log2Up(4),
      //y=log2Up(4),
      //x=log2Up(2),
      //y=log2Up(2),
    ),
    objTileSize2dPow=ElabVec2[Int](
      x=log2Up(16),
      y=log2Up(16),
      //x=log2Up(8),
      //y=log2Up(8),
      //x=log2Up(4),
      //y=log2Up(4),
      //x=log2Up(2),
      //y=log2Up(2),
    ),
    objTileWidthRshift=(
      0
      //1
    ),
    //objTileWidthRshift=1,
    objAffineTileSize2dPow=ElabVec2[Int](
      //x=log2Up(16),
      //y=log2Up(16),
      x=log2Up(8),
      y=log2Up(8),
      //x=log2Up(4),
      //y=log2Up(4),
      //x=log2Up(2),
      //y=log2Up(2),
    ),
    objAffineTileWidthRshift=(
      //0
      1
    ),
    //objAffineTileWidthRshift=,
    //numBgsPow=log2Up(4),
    numBgsPow=log2Up(2),
    //numBgsPow=log2Up(1),
    //numObjsPow=log2Up(64),
    //numObjsPow=log2Up(32),
    //numObjsPow=log2Up(2),
    //numObjsPow=log2Up(32),
    //numObjsPow=log2Up(16),
    //numObjsPow=log2Up(2),
    //numObjsPow=log2Up(4),
    //numObjsPow=log2Up(8),
    numObjsPow=(
      //log2Up(16)
      log2Up(8)
    ),
    numObjsAffinePow=(
      //log2Up(16)
      log2Up(4)
    ),
    //numBgTilesPow=Some(log2Up(256)),
    //numBgTilesPow=Some(log2Up(2)),
    numBgTiles=Some(
      //16
      256
      //512
      //1024
    ),
    //numObjTilesPow=None,
    numObjTiles=Some(
      //16
      //64
      //512
      256
      //128
    ),
    numObjAffineTiles=Some(16),
    numColsInBgPalPow=(
      //log2Up(256)
      log2Up(64)
    ),
    numColsInObjPalPow=(
      log2Up(64)
      //log2Up(256)
    ),
    noColorMath=true,
    noAffineBgs=true,
    noAffineObjs=(
      true
      //false
    ),
    //--------
    bgTileMemInit=None,
    //--------
    //fancyObjPrio=false,
    fancyObjPrio=true,
  )
  //def gpu2dParams = DefaultGpu2dParams(
  //  rgbConfig=rgbConfig,
  //  intnlFbSize2d=ElabVec2[Int](
  //    x=vgaTimingInfo.fbSize2d.x,
  //    y=vgaTimingInfo.fbSize2d.y,
  //  ),
  //  physFbSize2dScaleY=ElabVec2[Int](
  //    x=1,
  //    y=1,
  //    //x=2,
  //    ////y=2,
  //    //y=2,
  //  ),
  //  //physFbSize2dScaleYPow=ElabVec2[Int](
  //  //  x=log2Up(1),
  //  //  y=log2Up(1),
  //  //  //x=log2Up(2),
  //  //  ////y=log2Up(2),
  //  //  //y=log2Up(2),
  //  //),
  //  bgTileSize2dPow=ElabVec2[Int](
  //    //x=log2Up(8),
  //    //y=log2Up(8),
  //    x=log2Up(4),
  //    y=log2Up(4),
  //    //x=log2Up(2),
  //    //y=log2Up(2),
  //  ),
  //  objTileSize2dPow=ElabVec2[Int](
  //    x=log2Up(8),
  //    y=log2Up(8),
  //    //x=log2Up(4),
  //    //y=log2Up(4),
  //    //x=log2Up(2),
  //    //y=log2Up(2),
  //  ),
  //  objTileWidthRshift=(
  //    //0
  //    1
  //  ),
  //  //objTileWidthRshift=1,
  //  objAffineTileSize2dPow=ElabVec2[Int](
  //    x=log2Up(16),
  //    y=log2Up(16),
  //    //x=log2Up(8),
  //    //y=log2Up(8),
  //    //x=log2Up(4),
  //    //y=log2Up(4),
  //    //x=log2Up(2),
  //    //y=log2Up(2),
  //  ),
  //  objAffineTileWidthRshift=(
  //    //0
  //    1
  //  ),
  //  //objAffineTileWidthRshift=,
  //  //numBgsPow=log2Up(4),
  //  numBgsPow=log2Up(2),
  //  //numObjsPow=log2Up(64),
  //  //numObjsPow=log2Up(32),
  //  //numObjsPow=log2Up(2),
  //  //numObjsPow=log2Up(32),
  //  //numObjsPow=log2Up(16),
  //  //numObjsPow=log2Up(2),
  //  //numObjsPow=log2Up(4),
  //  //numObjsPow=log2Up(8),
  //  numObjsPow=log2Up(16),
  //  numObjsAffinePow=(
  //    //log2Up(16)
  //    log2Up(4)
  //  ),
  //  //numBgTilesPow=Some(log2Up(256)),
  //  //numBgTilesPow=Some(log2Up(2)),
  //  numBgTiles=Some(
  //    //16
  //    //256
  //    //512
  //    1024
  //  ),
  //  //numObjTilesPow=None,
  //  numObjTiles=Some(16),
  //  numObjAffineTiles=Some(16),
  //  numColsInBgPalPow=log2Up(64),
  //  numColsInObjPalPow=log2Up(64),
  //  noColorMath=true,
  //  noAffineBgs=true,
  //  //noAffineObjs=true,
  //  noAffineObjs=false,
  //  //fancyObjPrio=false,
  //  fancyObjPrio=true,
  //)

  val simSpinalConfig = SpinalConfig(
    //defaultClockDomainFrequency=FixedFrequency(100 MHz)
    defaultClockDomainFrequency=FixedFrequency(clkRate)
  )
  SimConfig
    .withConfig(config=simSpinalConfig)
    .withVcdWave
    .compile(
    Gpu2dSimDut(
      clkRate=clkRate,
      rgbConfig=rgbConfig,
      vgaTimingInfo=vgaTimingInfo,
      gpu2dParams=gpu2dParams,
      ctrlFifoDepth=ctrlFifoDepth,
      optRawSnesButtons=true,
      dbgPipeMemRmw=(
        //true
        false
      ),
    )
      //Gpu2dSimDut(
      //  clkRate=Gpu2dSimDutParams.clkRate,
      //  rgbConfig=Gpu2dSimDutParams.rgbConfig,
      //  vgaTimingInfo=Gpu2dSimDutParams.vgaTimingInfo,
      //  gpu2dParams=Gpu2dSimDutParams.gpu2dParams,
      //  ctrlFifoDepth=Gpu2dSimDutParams.ctrlFifoDepth,
      //  optRawSnesButtons=true,
      //  dbgPipeMemRmw=(
      //    true
      //    //false
      //  ),
      //)
    )
    .doSim { dut =>
      dut.clockDomain.forkStimulus(period=10)
      //SimTimeout(1000)
      //for (idx <- 0 to 4000) {
      //  //sleep(1)
      //  dut.clockDomain.waitRisingEdge()
      //}
      dut.io.rawSnesButtons.valid #= true
      dut.io.rawSnesButtons.payload #= (
        (
          (1 << 16) - 1
        )
        //& ~(
        //  //1 << SnesButtons.DpadRight
        //  1 << SnesButtons.A
        //)
      )
      def simNumClks = (
        //16000
        //32000
        //vgaTimingInfo.fbSize2d.x * vgaTimingInfo.fbSize2d.y * 20 * 2
        //vgaTimingInfo.fbSize2d.x * vgaTimingInfo.fbSize2d.y * 3 * 2
        //vgaTimingInfo.fbSize2d.x * 16 * 9 * 3 * 2
        //vgaTimingInfo.fbSize2d.x * 16 * 4 * 3 * 2
        //vgaTimingInfo.fbSize2d.x * 4 * 4 * 3 * 2
        //vgaTimingInfo.fbSize2d.x * vgaTimingInfo.fbSize2d.y * 2 * 4
        (
          vgaTimingInfo.fbSize2d.x * vgaTimingInfo.fbSize2d.y
          * (
            //1.5
            2
            * (
              clkRate / vgaTimingInfo.pixelClk
            )
          )
        ).toInt
        //38400
        //38400 * 2
        //48000
      )
      for (idx <- 0 to simNumClks - 1) {
        dut.clockDomain.waitRisingEdge()
        //when (dut.io.misc.visib) {
        //  foundVisib := True
        //}
        //when (foundVisib) {
        //  
        //}
      }
      simSuccess()
    }
}
