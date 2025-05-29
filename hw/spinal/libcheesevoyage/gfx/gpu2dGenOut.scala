package libcheesevoyage.gfx
import libcheesevoyage._

import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2
import libcheesevoyage.general._

import spinal.core._
//import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import scala.collection.mutable.ArrayBuffer
import scala.math._

object Gpu2dSimDutConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    ),
    //onlyStdLogicVectorAtTopLevelIo=true,
    svInterface=(
      //false
      true
    ),
    //svInterfaceIncludeModport=(
    //  false
    //  //true
    //),
  )
}
object Gpu2dSimDutParams {
  //def clkRate = 200.0 MHz
  //def clkRate = 150.0 MHz
  //def clkRate = 125.0 MHz
  //def clkRate = 1250.0 MHz
  //def clkRate = 50.0 MHz
  //--------
  //def clkRate = 75.0 MHz
  //def clkRate = 50.0 MHz
  def clkRate = 25.0 MHz
  //--------
  //def clkRate = 100.0 MHz
  //def clkRate = 100.7 MHz
  //def clkRate = 200.0 MHz
  //def clkRate = 25.0 MHz
  //--------
  def pixelClk = 25.0 MHz
  //--------
  //def pixelClk = 12.5 MHz
  //def pixelClk = 125.0 MHz
  //def ctrlFifoDepth = 20
  def ctrlFifoDepth = 256
  //def ctrlFifoDepth = 100
  //def ctrlFifoDepth = 128
  //def ctrlFifoDepth = 10
  //def fbSize2d = ElabVec2[Int](640, 480)
  //def fbSize2d = ElabVec2[Int](1, 1)
  //def fbSize2d = ElabVec2[Int](20, 20)
  //def rgbConfig = RgbConfig(rWidth=6, gWidth=6, bWidth=6)
  def rgbConfig = RgbConfig(rWidth=4, gWidth=4, bWidth=4)
  ////def rgbConfig = RgbConfig(rWidth=4, gWidth=4, bWidth=4)
  //def physRgbConfig = LcvVideoDithererIo.outRgbConfig(rgbConfig=rgbConfig)
  //def vgaTimingInfo = LcvVgaTimingInfoMap.map("640x480@60")
  def vgaTimingInfo=(
    //LcvVgaTimingInfoMap.map("640x480@60")
    //LcvVgaTimingInfoMap.map("1920x1080@60")
    LcvVgaTimingInfo(
      pixelClk=pixelClk,
      //pixelClk=25.175 MHz,
      htiming=LcvVgaTimingHv(
        //visib=1 << 6,
        //visib=64,
        //visib=1 << 7,
        visib=320,
        //visib=640,
        //visib=639,
        //visib=160,
        //visib=1 << 8,
        //visib=4,
        //visib=8,
        //front=1,
        //sync=1,
        //back=1,
        //front=4,
        //sync=4,
        //back=4,
        front=40,
        //sync=40,
        //back=40,
        //front=4,
        sync=4,
        back=4,
      ),
      vtiming=LcvVgaTimingHv(
        //visib=1 << 5,
        //visib=1 << 3,
        //visib=1 << 4,
        //visib=1 << 7,
        //visib=128,
        visib=240,
        //visib=480,
        //visib=32,
        //visib=4,
        //visib=8,
        //front=1,
        //sync=1,
        //back=1,
        //front=4,
        //sync=4,
        //back=4,
        //front=20,
        //front=40,
        //sync=40,
        //back=40,
        front=8,
        sync=4,
        back=4,
      ),
    )
  )
  //def pixelClk = vgaTimingInfo.pixelClk
  //def clkRate = pixelClk * 2

  //def fbSize2d = vgaTimingInfo.fbSize2d
  //--------
  //def fbSize2d = ElabVec2[Int](
  //  x=(
  //    vgaTimingInfo.fbSize2d.x / 2
  //    //320
  //    //640
  //  ),
  //  y=(
  //    vgaTimingInfo.fbSize2d.y
  //    //240
  //    //480
  //  ),
  //)
  //--------
  //def gpu2dParams = DefaultGpu2dParams(
  //  rgbConfig=rgbConfig,
  //  intnlFbSize2d=ElabVec2[Int](
  //    x=vgaTimingInfo.fbSize2d.x,
  //    y=vgaTimingInfo.fbSize2d.y,
  //  ),
  //  physFbSize2dScalePow=ElabVec2[Int](
  //    x=log2Up(1),
  //    y=log2Up(1),
  //    //x=log2Up(2),
  //    ////y=log2Up(2),
  //    //y=log2Up(2),
  //  ),
  //  bgTileSize2dPow=ElabVec2[Int](
  //    x=log2Up(8),
  //    y=log2Up(8),
  //    //x=log2Up(4),
  //    //y=log2Up(4),
  //    //x=log2Up(2),
  //    //y=log2Up(2),
  //  ),
  ////  objTileSize2dPow=ElabVec2[Int](
  //    x=log2Up(8),
  //    y=log2Up(8),
  //    //x=log2Up(4),
  //    //y=log2Up(4),
  //    //x=log2Up(2),
  //    //y=log2Up(2),
  //  ),
  //  objTileWidthRshift=1,
  //  objAffineTileSize2dPow=ElabVec2[Int](
  //    //x=log2Up(32),
  //    //y=log2Up(32),
  //    //x=log2Up(16),
  //    //y=log2Up(16),
  //    x=log2Up(8),
  //    y=log2Up(8),
  //  ),
  //  objAffineTileWidthRshift=0,
  //  //objAffineTileWidthRshift=log2Up(4),
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
  //  numObjsPow=log2Up(
  //    //16
  //    8
  //  ),
  //  numObjsAffinePow=log2Up(
  //    //32
  //    //8
  //    4
  //  ),
  //  //numBgTilesPow=Some(log2Up(256)),
  //  //numBgTilesPow=Some(log2Up(2)),
  //  numBgTiles=Some(
  //    //16
  //    64
  //  ),
  //  //numObjTilesPow=None,
  //  numObjTiles=Some(
  //    //8
  //    16
  //  ),
  //  numObjAffineTiles=Some(
  //    //32
  //    //8
  //    4
  //  ),
  //  numColsInBgPalPow=log2Up(64),
  //  numColsInObjPalPow=log2Up(64),
  //  //--------
  //  noColorMath=true,
  //  noAffineBgs=true,
  //  //noAffineObjs=true,
  //  noAffineObjs=false,
  //  fancyObjPrio=true,
  //  //--------
  //)
  def gpu2dBgTileSize2dPow = ElabVec2[Int](
    x=log2Up(16),
    y=log2Up(16),
    //x=log2Up(8),
    //y=log2Up(8),
    //x=log2Up(4),
    //y=log2Up(4),
    //x=log2Up(2),
    //y=log2Up(2),
  )
  def gpu2dPhysFbSize2dScale = ElabVec2[Int](
    x=1,
    y=1,
    //x=3,
    //y=2,
    //x=5,
    //y=5,
    //x=4,
    //y=4,
    //x=1,
    //y=1,
    //x=2,
    //y=2,
    //y=2,
    //y=3,
    //x=log2Up(2),
    ////y=log2Up(2),
    //y=log2Up(2),
  )
  def gpu2dIntnlFbSize2d = ElabVec2[Int](
    x=vgaTimingInfo.fbSize2d.x / gpu2dPhysFbSize2dScale.x,
    y=vgaTimingInfo.fbSize2d.y / gpu2dPhysFbSize2dScale.y,
  )
  //println(gpu2dIntnlFbSize2d)
  def gpu2dParams = DefaultGpu2dConfig(
    rgbConfig=rgbConfig,
    intnlFbSize2d=gpu2dIntnlFbSize2d,
    physFbSize2dScale=gpu2dPhysFbSize2dScale,
    //physFbSize2dScalePow=ElabVec2[Int](
    //  x=log2Up(1),
    //  y=log2Up(1),
    //  //x=log2Up(2),
    //  ////y=log2Up(2),
    //  //y=log2Up(2),
    //),
    bgTileSize2dPow=(
      gpu2dBgTileSize2dPow
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
      //0
      //1
      2
    ),
    //objTileWidthRshift=1,
    objAffineTileSize2dPow=ElabVec2[Int](
      //x=log2Up(64),
      //y=log2Up(64),
      //x=log2Up(32),
      //y=log2Up(32),
      //x=log2Up(16),
      //y=log2Up(16),
      //x=log2Up(8),
      //y=log2Up(8),
      x=log2Up(4),
      y=log2Up(4),
      //x=log2Up(2),
      //y=log2Up(2),
    ),
    objAffineTileWidthRshift=(
      //0
      //1
      2
      //3
      //4
    ),
    //numBgsPow=log2Up(4),
    numBgsPow=log2Up(2),
    //numObjsPow=log2Up(64),
    //numObjsPow=log2Up(32),
    //numObjsPow=log2Up(2),
    //numObjsPow=log2Up(32),
    //numObjsPow=log2Up(16),
    //numObjsPow=log2Up(2),
    //numObjsPow=log2Up(4),
    //numObjsPow=log2Up(8),
    numObjsPow=(
      log2Up(8)
      //log2Up(16)
      //log2Up(128)
    ),
    numObjsAffinePow=(
      log2Up(16)
      //log2Up(4)
      //log2Up(32)
    ),
    //numBgTilesPow=Some(log2Up(256)),
    //numBgTilesPow=Some(log2Up(2)),
    numBgTiles=({
      //Some(16)
      ////Some(320 * 240)
      val temp = (
        //(
        //  //vgaTimingInfo.fbSize2d.x
        //  //* vgaTimingInfo.fbSize2d.y
        //  gpu2dIntnlFbSize2d.x
        //  * gpu2dIntnlFbSize2d.y
        //  * 2
        //  //* 3
        //  / (1 << (gpu2dBgTileSize2dPow.x + gpu2dBgTileSize2dPow.y))
        //)
        256
        //* (
        //  //64
        //  2
        //)
        //1024
      )
      //println(temp)
      Some(
        temp
      )
      // for double buffering
      //Some(
      //  vgaTimingInfo.fbSize2d.x
      //  * vgaTimingInfo.fbSize2d.y
      //  * 2
      //  / (1 << (gpu2dBgTileSize2dPow.x + gpu2dBgTileSize2dPow.y))
      //)
    }),
    numColorMathTiles=(
      Some(32)
    ),
    //numObjTilesPow=None,
    numObjTiles=(
      //Some(8)
      //Some(16)
      //Some(4)
      Some(128)
      //Some(256)
      //Some(32)
    ),
    numObjAffineTiles=(
      Some(16)
      //Some(32)
    ),
    numColsInBgPalPow=(
      log2Up(64)
      //log2Up(256)
    ),
    numColsInObjPalPow=(
      log2Up(64)
      //log2Up(256)
    ),
    noColorMath=(
      true
      //false,
    ),
    noAffineBgs=(
      true
    ),
    noAffineObjs=(
      true
      //false
    ),
    //fancyObjPrio=false,
    fancyObjPrio=true,
  )
}

object Gpu2dSimDutToVerilog extends App {
  //Gpu2dSimDutConfig.spinal.generateVerilog(Gpu2dSimDut(
  //  clkRate=Gpu2dSimDutParams.clkRate,
  //  rgbConfig=Gpu2dSimDutParams.rgbConfig,
  //  vgaTimingInfo=Gpu2dSimDutParams.vgaTimingInfo,
  //  gpu2dParams=Gpu2dSimDutParams.gpu2dParams,
  //  ctrlFifoDepth=Gpu2dSimDutParams.ctrlFifoDepth,
  //  optRawSnesButtons=true,
  //  optUseLcvVgaCtrl=(
  //    //true
  //    false
  //  ),
  //  dbgPipeMemRmw=(
  //    //true
  //    false
  //  ),
  //))
  Gpu2dSimDutConfig.spinal.generateSystemVerilog{
    /*val top =*/ Gpu2dSimDut(
      clkRate=Gpu2dSimDutParams.clkRate,
      rgbConfig=Gpu2dSimDutParams.rgbConfig,
      vgaTimingInfo=Gpu2dSimDutParams.vgaTimingInfo,
      gpu2dCfg=Gpu2dSimDutParams.gpu2dParams,
      ctrlFifoDepth=Gpu2dSimDutParams.ctrlFifoDepth,
      optRawSnesButtons=true,
      optUseLcvVgaCtrl=(
        //true
        false
      ),
      dbgPipeMemRmw=(
        //true
        false
      ),
    )
    //top
  }
}
object Gpu2dToVerilog extends App {
  Gpu2dSimDutConfig.spinal.generateSystemVerilog{
    val top = new Component {
      val gpu2d = Gpu2d(
        cfg=Gpu2dSimDutParams.gpu2dParams,
      )
      //gpu2d.setName("Gpu2dInnards")
      val io = Gpu2dIo(
        cfg=Gpu2dSimDutParams.gpu2dParams,
        dbgPipeMemRmw=false,
      )
      //io.notSVIFthisLevel()
      //io.notSVIF()
      io <> gpu2d.io
    }
    top.setDefinitionName("Gpu2dTopLevel")
    top
  }
}

object Gpu2dSimDutToVhdl extends App {
  Gpu2dSimDutConfig.spinal.generateVhdl(Gpu2dSimDut(
    clkRate=Gpu2dSimDutParams.clkRate,
    rgbConfig=Gpu2dSimDutParams.rgbConfig,
    vgaTimingInfo=Gpu2dSimDutParams.vgaTimingInfo,
    gpu2dCfg=Gpu2dSimDutParams.gpu2dParams,
    ctrlFifoDepth=Gpu2dSimDutParams.ctrlFifoDepth,
    optRawSnesButtons=true,
    optUseLcvVgaCtrl=(
      //true
      false
    ),
    dbgPipeMemRmw=false,
  ))
  //val report = SpinalVhdl(new Gpu2dTo())
  //report.printPruned()
  //val test = PipeSkidBuf(UInt(3 bits))
}

object Gpu2dInterfaceTestToVerilog extends App {
  //case class InterfaceTestIo(
  //  wordWidth: Int,
  //) extends Bundle {
  //  //val inpWord = in(Vec.fill(mySize)(TestIntf()))
  //  //val outpWord = out(Vec.fill(mySize)(TestIntf()))
  //  val myIntf = Vec.fill(mySize)(TestIntf(wordWidth=wordWidth))
  //  addGeneric(
  //    name="wordWidth",
  //    that=wordWidth,
  //    default="8",
  //  )
  //  for (idx <- 0 until myIntf.size) {
  //    tieIFParameter(
  //      signal=myIntf(idx),
  //      signalParam="wordWidth",
  //      inputParam="wordWidth",
  //    )
  //  }
  //  notSVmodport()
  //}
  //case class InterfaceTest(
  //  wordWidth: Int
  //) extends Component {
  //  val io = InterfaceTestIo(wordWidth=wordWidth)
  //  //io.outpWord.myWord.setAsReg()
  //  //io.outpWord.myWord := io.inpWord.myWord + 1
  //  for (idx <- 0 until io.myIntf.size) {
  //    io.myIntf(idx).outpWord.setAsReg()
  //    io.myIntf(idx).outpWord := io.myIntf(idx).inpWord + 1
  //  }
  //}
  //--------
  //def testPmRmwWordWidth = 8
  //def testPmRmwWordType() = UInt(testPmRmwWordWidth bits)
  val testPmRmwWordCountArr = Array.fill(1)(8.toInt)
  //--------
  def testPmRmwCfg[
    WordT <: Data
  ](
    wordType: HardType[WordT],
  ): PipeMemRmwConfig[WordT, Bool] = PipeMemRmwConfig(
    wordType=wordType(),
    wordCountArr=testPmRmwWordCountArr,
    hazardCmpType=Bool(),
    modRdPortCnt=1,
    modStageCnt=1,
    pipeName="pipeMem_InterfaceDebug",
  )
  def mkTestExt[
    WordT <: Data
  ](
    wordType: HardType[WordT]
  ) = {
    val ret = PipeMemRmwPayloadExt(
      cfg=testPmRmwCfg(wordType=wordType()),
      wordCount=testPmRmwWordCountArr(0)
    )
    ret
  }
  case class TestPmRmwModType[
    WordT <: Data
  ](
    wordType: HardType[WordT],
  ) extends Bundle with PipeMemRmwPayloadBase[WordT, Bool] {
    //--------
    val myExt = mkTestExt(wordType=wordType())
    //--------
    /*override*/ def setPipeMemRmwExt(
      inpExt: PipeMemRmwPayloadExt[WordT, Bool],
      ydx: Int,
      memArrIdx: Int,
    ): Unit = {
      myExt := inpExt
    }
    /*override*/ def getPipeMemRmwExt(
      outpExt: PipeMemRmwPayloadExt[WordT, Bool],
      ydx: Int,
      memArrIdx: Int,
    ): Unit = {
      outpExt := myExt
    }
    //--------
    /*override*/ def formalSetPipeMemRmwFwd(
      outpFwd: PipeMemRmwFwd[WordT, Bool],
      memArrIdx: Int,
    ): Unit = {
    }
    /*override*/ def formalGetPipeMemRmwFwd(
      inpFwd: PipeMemRmwFwd[WordT, Bool],
      memArrIdx: Int,
    ): Unit = {
    }
    //--------
    //setDefinitionName(
    //  "TestPmRmwModType"
    //)
    //--------
  }
  case class TestVecPmRmwModType[
    WordT <: Data
  ](
    wordType: HardType[WordT],
    vecSize: Int,
  ) extends Bundle with PipeMemRmwPayloadBase[WordT, Bool] {
    //--------
    val myExt = Vec.fill(vecSize)(mkTestExt(wordType=wordType()))
    //--------
    /*override*/ def setPipeMemRmwExt(
      inpExt: PipeMemRmwPayloadExt[WordT, Bool],
      ydx: Int,
      memArrIdx: Int,
    ): Unit = {
      myExt(ydx) := inpExt
    }
    /*override*/ def getPipeMemRmwExt(
      outpExt: PipeMemRmwPayloadExt[WordT, Bool],
      ydx: Int,
      memArrIdx: Int,
    ): Unit = {
      outpExt := myExt(ydx)
    }
    //--------
    /*override*/ def formalSetPipeMemRmwFwd(
      outpFwd: PipeMemRmwFwd[WordT, Bool],
      memArrIdx: Int,
    ): Unit = {
    }
    /*override*/ def formalGetPipeMemRmwFwd(
      inpFwd: PipeMemRmwFwd[WordT, Bool],
      memArrIdx: Int,
    ): Unit = {
    }
    //--------
    //setDefinitionName(
    //  "TestPmRmwModType"
    //)
    //--------
  }
  case class TestPmRmwThing(
  ) extends Bundle {
    //--------
    val tempMod = TestPmRmwModType(UInt(8 bits))
    val tempVecMod = TestVecPmRmwModType(UInt(4 bits), 2)
    //--------
  }
  //--------
  val myDefaultWordWidth = 8
  //def wordType() = UInt(wordWidth bits)
  val myDefaultWordCount = (
    //1
    2
  )
  val outerWordCount = (
    //1
    3
  )
  //case class TestIntf(
  //  wordWidth: Int,
  //) extends Bundle {
  //  val inpWord = in(UInt(wordWidth bits))
  //  val outpWord = out(UInt(wordWidth bits))
  //  addGeneric(
  //    name="wordWidth",
  //    that=wordWidth,
  //    default="8",
  //  )
  //  tieGeneric(
  //    signal=inpWord,
  //    generic="wordWidth",
  //  )
  //  tieGeneric(
  //    signal=outpWord,
  //    generic="wordWidth",
  //  )
  //  notSVmodport()
  //}
  case class TestStreamPayloadInner(
    wordWidth: Int
  ) extends Bundle {
    val myData = UInt(wordWidth bits)
    //setAsSVstruct()
    //notSVmodport()
    //addGeneric(
    //  name="wordWidth",
    //  that=wordWidth,
    //  default="8",
    //)
    //tieGeneric(
    //  signal=myData,
    //  generic="wordWidth",
    //)
    //notSVmodportthisLevel()
    //setDefinitionName(
    //  s"TestStreamPayloadInner"
    //)
    //doConvertSVIFvec()
  }
  case class TestStreamPayload(
    wordWidth: Int,
    wordCount: Int,
  ) extends Bundle /*with IMasterSlave*/ {
    //setDefinitionName(
    //  s"TestStreamPayloadInner"
    //)
    //val inpData = UInt(wordWidth bits)
    //val outpData = UInt(wordWidth bits)
    val inpData = (Vec.fill(outerWordCount)(
      Vec.fill(wordCount)(
        TestStreamPayloadInner(wordWidth=wordWidth)
      )
    ))
    val outpData = (Vec.fill(outerWordCount)(
      Vec.fill(wordCount)(
        TestStreamPayloadInner(wordWidth=wordWidth)
      )
    ))
    //--------
    //notSVmodport()
    //setAsSVstruct()
    //--------
    //override def asMaster(): Unit = mst
    //@modport
    //def mst = {
    //  in(inpData)
    //  out(outpData)
    //}
    //@modport
    //def slv = {
    //  out(inpData)
    //  in(outpData)
    //}

    //setAsSVstruct()
    //notSVmodport()
    //setAsSVstruct()
    //addGeneric(
    //  name="wordWidth",
    //  that=wordWidth,
    //  default="8",
    //)
    ////addGeneric(
    ////  name="wordCount",
    ////  that=wordCount,
    ////  default="3",
    ////)
    //for (jdx <- 0 until outerWordCount) {
    //  for (idx <- 0 until wordCount) {
    //    tieIFParameter(
    //      signal=inpData(jdx)(idx),
    //      signalParam="wordWidth",
    //      inputParam="wordWidth",
    //    )
    //    tieIFParameter(
    //      signal=outpData(jdx)(idx),
    //      signalParam="wordWidth",
    //      inputParam="wordWidth",
    //    )
    //  }
    //}
    ////notSVmodport()
    //notSVmodportthisLevel()
    ////setDefinitionName(
    ////  s"TestStreamPayload_${wordCount}"
    ////)
    ////setDefinitionName(
    ////  s"TestStreamPayload"
    ////)
    //doConvertSVIFvec()
  }

  case class TopLevelIo(
    wordWidth: Int,
    wordCount: Int,
  ) extends Bundle with IMasterSlave {
    //val testThing = in(TestPmRmwThing())
    //println(
    //  this.getClass.getSimpleName
    //)
    //setName(
    //  "TopLevel_io"
    //)
    //setDefinitionName(
    //  s"TopLevelIo_${wordWidth}_${wordCount}"
    //)
    //val inpWord = in(
    //  Vec.fill(mySize)(
    //    //wordType()
    //    UInt(wordWidth bits)
    //  )
    //)
    //val outpWord = out(
    //  Vec.fill(mySize)(
    //    //wordType()
    //    UInt(wordWidth bits)
    //  )
    //)
    //--------
    //val tempStruct = /*out*/(
    //  TestStreamPayload(
    //    wordWidth=wordWidth,
    //    wordCount=wordCount,
    //  )
    //)
    //--------
    //val stm0 = (
    //  DebugStream(wordType=UInt(wordWidth bits))
    //)
    //val stm1 = /*in*/(
    //  DebugStream(wordType=UInt(wordWidth + 1 bits))
    //)
    val push = (
      /*slave*/(
        Stream(
          Vec.fill(wordCount)(
            TestStreamPayload(
              wordWidth=wordWidth,
              wordCount=wordCount,
            )
          )
        )
      )
    )
    val pop = (
      /*master*/(
        Stream(
          Vec.fill(wordCount)(
            TestStreamPayload(
              wordWidth=wordWidth,
              wordCount=wordCount,
            )
          )
        )
      )
    )
    //@modport
    def slv = {
      ////out(tempStruct)
      //slave(tempStruct)
      ////out(stm0)
      ////out(stm1)
      master(push)
      slave(pop)
      //for (idx <- 0 until wordCount) {
      //  master(push.payload(idx))
      //  slave(pop.payload(idx))
      //}
    }
    slv
    override def asMaster(): Unit = mst
    //@modport
    def mst = {
      ////in(tempStruct)
      //master(tempStruct)
      ////in(stm0)
      ////in(stm1)
      slave(push)
      master(pop)
      //for (idx <- 0 until wordCount) {
      //  slave(push.payload(idx))
      //  master(pop.payload(idx))
      //}
    }
    //notSVIF()
    //notSVmodport()
    //setDefinitionName(
    //  s"TopLevelIo_${wordWidth}_${wordCount}"
    //)
  }
  //case class DebugStream[
  //  WordT <: Data
  //](
  //  wordType: HardType[WordT],
  //  //wordWidth: Int
  //) extends Interface {
  //  val stmData = Stream(
  //    //UInt(wordWidth bits)
  //    wordType()
  //  )
  //  setDefinitionName(
  //    s"Stream"
  //  )
  //  notSVmodport()
  //}
  case class TopLevelInnards1(
    wordWidth: Int,
    wordCount: Int,
  ) extends Component {
    //--------
    val io = slave(TopLevelIo(
      wordWidth=wordWidth,
      wordCount=wordCount,
    ))
    //--------
  }
  case class TopLevelInnards(
    wordWidth: Int,
    wordCount: Int,
  ) extends Component {
    //--------
    val io = master(TopLevelIo(
      wordWidth=wordWidth,
      wordCount=wordCount,
    ))
    ////--------
    //val pushMidStm = (
    //  Stream(
    //    Vec.fill(wordCount)(
    //      TestStreamPayload(
    //        wordWidth=wordWidth,
    //        wordCount=wordCount,
    //      )
    //    )
    //  )
    //)
    ////pushMidStm.doConvertSVIFvec()
    ////pushMidStm.notSVmodport()
    //val popMidStm = (
    //  Stream(
    //    Vec.fill(wordCount)(
    //      TestStreamPayload(
    //        wordWidth=wordWidth,
    //        wordCount=wordCount,
    //      )
    //    )
    //  )
    //)
    //--------
    //case class MyInterface() extends Interface {
    //  val data = UInt(8 bits)
    //}
    //case class MyInterfaceOuter() extends Interface {
    //  val tempV2d = Vec.fill(3)(
    //    Vec.fill(2)(
    //      MyInterface()
    //    )
    //  )
    //}
    //val myOuter = MyInterfaceOuter()
    //--------

    //for ((elem, idx) <- popMidStm.payload.zipWithIndex) {
    //  elem.doConvertSVIFvec()
    //}
    //popMidStm.notSVmodport()
    ////val myIoPushS2mPipe = (
    ////  io.push.s2mPipe()
    ////  .setName(s"myIoPushS2mPipe")
    ////)
    ////val myIoPushM2sPipe = (
    ////  myIoPushS2mPipe.m2sPipe()
    ////  .setName(s"myIoPushM2sPipe")
    ////)
    ////pushMidStm << io.push //myIoPushM2sPipe
    //--------
    //pushMidStm <-/< io.push
    //////val myPopMidStmS2mPipe = (
    //////  popMidStm.s2mPipe()
    //////  .setName(s"myPopMidStmS2mPipe")
    //////)
    //////val myPopMidStmM2sPipe = (
    //////  myPopMidStmS2mPipe.m2sPipe()
    //////  .setName(s"myPopMidStmM2sPipe")
    //////)
    //////io.pop << popMidStm //myPopMidStmM2sPipe
    //io.pop <-/< popMidStm
    io.pop << io.push
    //--------
    //io.tempStruct.outpData := io.tempStruct.inpData
    //--------

    //pushMidStm.translateInto(into=popMidStm)(
    //  dataAssignment=(
    //    o,
    //    i
    //  ) => {
    //    for (kdx <- 0 until wordCount) {
    //      for (jdx <- 0 until outerWordCount) {
    //        for (idx <- 0 until wordCount) {
    //          o(kdx).inpData(jdx)(idx).myData := (
    //            i(kdx).inpData(jdx)(idx).myData
    //          )
    //          o(kdx).outpData(jdx)(idx).myData := (
    //            i(kdx).inpData(jdx)(idx).myData + 1
    //          )
    //        }
    //      }
    //    }
    //  }
    //)
    //--------
    //io.pop <-/< io.push
    ////val dut = InterfaceTest(wordWidth=wordWidth)
    //val tempStmVec = Vec.fill(mySize)(
    //  //Vec.fill(3)(
    //    Stream(TestStreamPayload(wordWidth=wordWidth))
    //  //)
    //)
    ////tempStm(0).valid := True
    ////tempStm(0).myData := 3
    ////tempStm(1) <-/< tempStm(0)
    ////tempStm(1).ready := True
    ////val tempMem = Mem(
    ////  wordType=TestStreamPayload(wordWidth=wordWidth),
    ////  wordCount=8
    ////)
    ////dut.io.outp
    ////for (idx <- 0 until mySize) {
    ////  dut.io.myIntf(idx).inpWord := io.inpWord(idx)
    ////  io.outpWord(idx) := dut.io.myIntf(idx).outpWord
    ////}
    //for (idx <- 0 until mySize) {
    //  def myStm = tempStmVec(idx)
    //  for (jdx <- 0 until myStm.size) {
    //    if (jdx == 0) {
    //      myStm(jdx) <-/< io.push(idx)
    //    } else if (jdx == myStm.size - 1) {
    //      //io.pop(idx) <-/< myStm(jdx)
    //      val between = Stream(TestStreamPayload(wordWidth=wordWidth))
    //      //between.translateFrom(that=myStm)
    //    } else {
    //      myStm(jdx) <-/< tempStmVec(idx)(jdx - 1)
    //    }
    //    
    //  }
    //}
    //--------
  }
  case class TopLevel(
    wordWidth: Int,
    wordCount: Int,
  ) extends Component {
    val io = master(TopLevelIo(
      wordWidth=wordWidth,
      wordCount=wordCount,
    ))
    //io.notSVIF()
    val dut = TopLevelInnards(
      wordWidth=wordWidth,
      wordCount=wordCount,
    )
    dut.io <> io
    //val rA = Reg(UInt(2 bits)) init(0x0)
    //val rB = Reg(UInt(3 bits)) init(0x0)
    //(rA, rB) := B"5'h3"
  }
  Gpu2dSimDutConfig.spinal.generateSystemVerilog{
    val top = TopLevel(
      //wordWidth=8
      wordWidth=myDefaultWordWidth,
      wordCount=myDefaultWordCount,
    )
    top.setName("TopLevel")
    //top.io.notSVIF()
    top
  }
}
