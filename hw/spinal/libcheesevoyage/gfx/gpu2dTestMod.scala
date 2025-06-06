package libcheesevoyage.gfx
import libcheesevoyage._

import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2
//import libcheesevoyage.general.DualTypeNumVec2PowCnt
import libcheesevoyage.general.DualTypeNumVec2
import libcheesevoyage.general.FpgacpuRamSimpleDualPort
import libcheesevoyage.hwdev._

import spinal.core._
//import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import scala.collection.mutable.ArrayBuffer
import scala.math._

object Gpu2dTest {
  //def bgMapArrInit(
  //  
  //) = {
  //}
  def innerTileMemInit(
    wordType: HardType[Gpu2dTileSlice],
    //cfg: Gpu2dParams,
    someNumTileSlices: Int,
    somePxsSliceWidth: Int,
    //someTileHeight: Int,
    //wordCount: Int,
    //somePalEntryMemIdxWidth: Int,
    somePxsArr: ArrayBuffer[Short],
    tempArr: ArrayBuffer[Gpu2dTileSlice],
    //isObj: Boolean,
    //doPrint: Boolean=false,
    isBg: Boolean=false,
    finish: Boolean=false,
  ): Unit = {
    //var tempWord = BigInt(0)
    //val tempArr = new ArrayBuffer[Gpu2dTileSlice]()
    //var jdx = 0
    def myPx = (
      //2
      0
    )
    //if (somePxsArr.size <= someNumTileSlices) {
      for (
        idx <- 0
        until //min(
          somePxsArr.size//,
        //  someNumTileSlices * somePxsSliceWidth
        //)
      ) {
        if ((idx % somePxsSliceWidth) == 0) {
          tempArr += wordType()
        }
        //if ((idx + 1) % somePxsSliceWidth == 0) {
        //  jdx += 1
        //}
        def myTileSlice = tempArr.last
        myTileSlice.colIdxVec(idx % somePxsSliceWidth) := (
          //if (!isBg) (
          //  //0x0
          //  //2
          //  myPx
          //) else (
            somePxsArr(idx)
          //)
        )
      }
    //}
    if (finish) {
      val oldSize = tempArr.size
      for (jdx <- oldSize until someNumTileSlices) {
        val tempWord = wordType()
        for (idx <- 0 until tempWord.colIdxVec.size) {
          tempWord.colIdxVec(idx) := (
            //if (isBg) (
            //  myBgPx
            //) else (
              //somePxsArr(idx)
              0x0
            //)
          )
        }
        tempArr += tempWord
        //tempArr += wordType().getZero
      }
    }
    //if (somePxsArr.size < someNumTileSlices) {
    //  for (idx <- somePxsArr.size until someNumTileSlices) {
    //    tempArr += wordType().getZero
    //  }
    //}
    //--------
    //println("\n\n")
    //if (doPrint) {
    //  println(" ")
    //}
  }
  def mkBgEntryMapTempFbSize2d(
    cfg: Gpu2dConfig,
    someMapSize2d: ElabVec2[Int],
  ) = {
    ElabVec2[Int](
      x=min(
        //Gpu2dTestGfx.sampleBgMapSize2d.x * 16,
        someMapSize2d.x * 16,
        cfg.intnlFbSize2d.x,
      ),
      y=min(
        //Gpu2dTestGfx.sampleBgMapSize2d.y * 16,
        someMapSize2d.y * 16,
        cfg.intnlFbSize2d.y,
      ),
    )
  }
  def mkBgEntryMapArr(
    cfg: Gpu2dConfig,
    someMapArr: ArrayBuffer[Short],
    someMapSize2d: ElabVec2[Int],
  ) = {
    val tempFbSize2d = mkBgEntryMapTempFbSize2d(
      cfg=cfg,
      someMapSize2d=someMapSize2d,
    )
    Mem(
      wordType=(
        UInt(16 bits)
        //UInt(8 bits)
      ),
      wordCount=(
        //
        cfg.bgSize2dInTiles.x * cfg.bgSize2dInTiles.y
      )
    )
      .initBigInt({
        val tempArr = new ArrayBuffer[BigInt]()
        ////var jdx: Int = 0
        //for (jdx <- 0 until myMapArr.size) {
        //  //val tempY = 
        //  tempArr += BigInt(myMapArr(jdx))
        //}
        //var z: Int = 0
        for (jdx <- 0 until cfg.bgSize2dInTiles.y) {
          for (kdx <- 0 until cfg.bgSize2dInTiles.x) {
            if (
              jdx
              < (
                //cfg.intnlFbSize2d.y / cfg.bgTileSize2d.y
                //tempFbSize2d.y / cfg.bgTileSize2d.y
                //Gpu2dTestGfx.sampleBgMapSize2d.y
                someMapSize2d.y
              )
              && kdx
              < (
                //cfg.intnlFbSize2d.x / cfg.bgTileSize2d.x
                //tempFbSize2d.x / cfg.bgTileSize2d.x
                //Gpu2dTestGfx.sampleBgMapSize2d.x
                someMapSize2d.x
              )
            ) {
              //println(s"inner: ($kdx, $jdx)")
              if (
                (
                  jdx < tempFbSize2d.y / cfg.bgTileSize2d.y
                ) && (
                  kdx < tempFbSize2d.x / cfg.bgTileSize2d.x
                )
              ) {
                //tempArr += BigInt(someMapArr(z) & 0x3ff)
                //tempArr += BigInt(1)
                //tempArr += BigInt(z)
                //tempArr += BigInt(z)

                tempArr += BigInt(
                  someMapArr(
                    jdx * someMapSize2d.x
                    + kdx
                  ) 
                  & 0x0ff
                  //& 0x3ff
                )
                //tempArr += BigInt(0)
              } else {
                tempArr += BigInt(0)
                //tempArr += BigInt(1)
              }
              //z += 1
              //tempArr += BigInt(
              //  someMapArr(
              //    jdx * someMapSize2d.x
              //    + kdx
              //  ) & 0x3ff
              //)
            } else {
              //println(s"outer: ($kdx, $jdx)")
              //tempArr += BigInt(64)
              //tempArr += BigInt(1)
              tempArr += BigInt(0)
            }
          }
        }
        tempArr.toSeq
      })
  }
  def bgTileMemInit(
    cfg: Gpu2dConfig,
    //gridIdx: Int,
  ) = {
    def wordType() = Gpu2dTileSlice(
      cfg=cfg,
      isObj=false,
      isAffine=false,
      doPipeMemRmw=false
    )
    val rawArrFgCommon = Gpu2dTestGfx.objFgCommonTileArr
    val rawArrFgGrassland = Gpu2dTestGfx.objFgGrasslandTileArr
    val rawArrSampleBg = Gpu2dTestGfx.sampleBgTileArr
    val rawArrSampleFg = Gpu2dTestGfx.sampleFgTileArr
    //val rawArrSampleFg2 = Gpu2dTestGfx.sampleFg2TileArr
    val myPxsSliceWidth = (
      Gpu2dTileSlice.pxsSliceWidth(
        cfg=cfg,
        isObj=false,
        isAffine=false,
      )
    )
    //val wordCount = (
    //  //Gpu2dTestGfx.fgCommonTileArr.size << 1
    //  //(rawArr.size << 1) >> log2Up(myPxsSliceWidth)
    //  (rawArrFgCommon.size + rawArrFgGrassland.size)
    //  >> log2Up(myPxsSliceWidth)
    //  //rawArr.size / myColIdxWidth
    //  //rawArr.size << 1
    //)
    //println(wordCount, rawArr.size, myPxsSliceWidth)
    //Mem(
    //  wordType=wordType(),
    //  wordCount=wordCount,
    //)
    //.init(
   // {
      val tempArr = new ArrayBuffer[Gpu2dTileSlice]()
      //if (tempWord < 0) {
      //  println(s"${tempWord} ${idx}")
      //}
      val tempNumTileSlices = (
        //cfg.numBgTiles * cfg.bgTileSize2d.y 
        1 << cfg.bgTileMemIdxWidth
      )
      //println(f"BG num tile slices: $tempNumTileSlices")
      def doInnerTileMemInit(
        somePxsArr: ArrayBuffer[Short],
        finish: Boolean=false,
      ): Unit = {
        innerTileMemInit(
          wordType=wordType(),
          someNumTileSlices=tempNumTileSlices,
          somePxsSliceWidth=myPxsSliceWidth,
          somePxsArr=somePxsArr,
          tempArr=tempArr,
          isBg=true,
          finish=finish,
        )
      }
      //--------
      //val rawArrBlankBg = new ArrayBuffer[Short]()
      //for (idx <- 0 until cfg.bgTileSize2d.x * cfg.bgTileSize2d.y) {
      //  rawArrBlankBg += 0.toShort
      //}
      //doInnerTileMemInit(
      //  somePxsArr=rawArrBlankBg,
      //  finish=true,
      //)
      doInnerTileMemInit(
        somePxsArr=rawArrSampleBg,
        finish=false,
      )
      doInnerTileMemInit(
        somePxsArr=rawArrSampleFg,
        finish=true,
      )
      //--------
      //doInnerTileMemInit(
      //  somePxsArr=rawArrSampleFg2,
      //  finish=true,
      //)
      //doInnerTileMemInit(
      //  somePxsArr=rawArrFgCommon,
      //  finish=false,
      //)
      //doInnerTileMemInit(
      //  somePxsArr=rawArrFgGrassland,
      //  finish=true,
      //)
      //--------
      //val tempArrPair = Array.fill(2)(
      //  new ArrayBuffer[Gpu2dTileSlice]()
      //)
      //for (idx <- 0 until tempArrPair.size) {
      //  tempArrPair(idx % 2) += tempArr(idx >> 1)
      //}

      //var tempWord = BigInt(0)
      //for (idx <- 0 until rawArrFgCommon.size) {
      //  if (idx % myPxsSliceWidth == 0) {
      //    tempWord = BigInt(0)
      //  }
      //  //tempWord = tempWord << 
      //  if ((idx + 1) % myPxsSliceWidth == 0) {
      //    tempArr += tempWord
      //  }
      //  //val tempWord = wordType()
      //  //if ((idx % myPxsSliceWidth) == 0) {
      //  //  tempArr += wordType()
      //  //}
      //  //def myTileSlice = tempArr.last
      //  //myTileSlice.colIdxVec(idx % myPxsSliceWidth) := (
      //  //  rawArrFgCommon(idx)
      //  //)
      //}
      //for (idx <- 0 until rawArrFgGrassland.size) {
      //  //var tempWord
      //  //if ((idx % myPxsSliceWidth) == 0) {
      //  //  tempArr += wordType()
      //  //}
      //  //def myTileSlice = tempArr.last
      //  //myTileSlice.colIdxVec(idx % myPxsSliceWidth) := (
      //  //  rawArrFgGrassland(idx)
      //  //)
      //}
      //Some(tempArr.toSeq)
      tempArr
      //tempArrPair(gridIdx)
    //}
    //)
  }
  //def doSplitBgTileMemInit(
  //  cfg: Gpu2dParams,
  //  //tempArr: ArrayBuffer[Gpu2dTileSlice],
  //  //gridIdx: Int,
  //  //isColorMath: Boolean
  //) = {
  //  val tempArr = bgTileMemInit(cfg=cfg)
  //  val tempArrPair = Array.fill(2)(
  //    new ArrayBuffer[Gpu2dTileSlice]()
  //  )
  //  for (idx <- 0 until tempArr.size) {
  //    val tempIdx = (
  //      //idx % cfg.bgTileSize2dPow.y
  //      (idx >> cfg.bgTileSize2dPow.y) % 2
  //    )
  //    tempArrPair(
  //      //(
  //      //  idx % (cfg.bgTileMemIdxWidth)
  //      //)
  //      //% 2
  //      //tempIdx % 2
  //      tempIdx
  //    ) += tempArr(idx)
  //  }
  //  tempArrPair//(gridIdx)
  //}
  def bgPalMemInitBigInt(
    cfg: Gpu2dConfig
  ) = {
    def wordCount = (
      //Gpu2dTestGfx.palette.size >> 1
      cfg.numColsInBgPal
    )
    //Mem(
    //  wordType=Rgb(cfg.rgbConfig),
    //  wordCount=wordCount,
    //)
    //.initBigInt({
      val tempArr = new ArrayBuffer[BigInt]
      for (
        //idx <- 0 until (Gpu2dTestGfx.palette.size >> 1)
        idx <- 0 until wordCount
      ) {
        def rgbConfig = cfg.rgbConfig
        //val tempCol = Rgb(rgbConfig)
        val tempRawCol = (
          (Gpu2dTestGfx.bgPalette((idx << 1) + 1) << 8)
          | (Gpu2dTestGfx.bgPalette(idx << 1) << 0)
        )
        val myTempRawColR = (
          tempRawCol >> (5 - rgbConfig.rWidth)
        )
        val myTempRawColG = (
          //tempRawCol >> (10 - rgbConfig.gWidth)
          //myTempRawColR >> (10 - (
          //  5 + cfg.rgbConfig.gWidth
          //))
          (tempRawCol >> 5) >> (5 - rgbConfig.gWidth)
        )
        val myTempRawColB = (
          (tempRawCol >> 10) >> (5 - rgbConfig.bWidth)
        )
        tempArr += (
          //tempCol
          (
            (myTempRawColR & ((1 << rgbConfig.rWidth) - 1))
            //<< (rgbConfig.bWidth + rgbConfig.gWidth)
          ) | (
            (myTempRawColG & ((1 << rgbConfig.gWidth) - 1))
            << (rgbConfig.rWidth)
            //<< (rgbConfig.bWidth)
          ) | (
            (myTempRawColB & ((1 << rgbConfig.bWidth) - 1))
            << (rgbConfig.rWidth + rgbConfig.gWidth)
          )
        )
        //println(s"${idx} ${tempArr.last}")
      }
      //Some(tempArr.toSeq)
      tempArr
    //})
  }
  //def somePalMemInit(
  //  cfg: Gpu2dParams,
  //  someBigIntArr: ArrayBuffer[BigInt],
  //) = {
  //  def wordCount = (
  //    //Gpu2dTestGfx.palette.size >> 1
  //    cfg.numColsInBgPal
  //  )
  //  //val tempBigIntArr = bgPalMemInitBigInt(cfg=cfg)
  //  val tempArr = new ArrayBuffer[Gpu2dPalEntry]()
  //  for (idx <- 0 until someBigIntArr.size) {
  //    val myPalEntry = Gpu2dPalEntry(cfg=cfg)
  //    val tempBigInt = someBigIntArr(idx).toInt
  //    val rgbConfig = cfg.rgbConfig
  //    //myPalEntry := myPalEntry.getZero
  //    //myPalEntry.allowOverride
  //    //val tempColR = (
  //    //  tempBigInt & ((1 << rgbConfig.rWidth) - 1)
  //    //)
  //    //val tempColG = (
  //    //  (tempBigInt >> rgbConfig.gWidth) & ((1 << rgbConfig.gWidth) - 1)
  //    //)
  //    //val tempColB = (
  //    //  (
  //    //    tempBigInt >> (rgbConfig.gWidth + rgbConfig.bWidth) 
  //    //  ) & ((1 << rgbConfig.bWidth) - 1)
  //    //)
  //    myPalEntry.assignFromBits(B(
  //      s"${myPalEntry.asBits.getWidth}'d${tempBigInt}"
  //    ))
  //    //val tempPalEntry = cloneOf(myPalEntry)
  //    //tempPalEntry := myPalEntry
  //    tempArr += myPalEntry
  //  }
  //  tempArr
  //}
  def objTileMemInit(
    cfg: Gpu2dConfig
  ) = {
    def wordType() = Gpu2dTileSlice(
      cfg=cfg,
      isObj=true,
      isAffine=false,
      doPipeMemRmw=false
    )
    val rawArrFgCommon = Gpu2dTestGfx.objFgCommonTileArr
    //val rawArrDbgBlankingFgCommon = (
    //  Gpu2dTestGfx.objDbgBlankingFgCommonTileArr
    //)
    //val rawArrFgGrassland = Gpu2dTestGfx.fgGrasslandTileArr
    val myPxsSliceWidth = (
      Gpu2dTileSlice.pxsSliceWidth(
        cfg=cfg,
        isObj=true,
        isAffine=false,
      )
    )
    val tempArr = new ArrayBuffer[Gpu2dTileSlice]()
    val tempNumTileSlices = (
      1 << cfg.objTileSliceMemIdxWidth
    )
    innerTileMemInit(
      wordType=wordType(),
      someNumTileSlices=tempNumTileSlices,
      somePxsSliceWidth=myPxsSliceWidth,
      somePxsArr=(
        rawArrFgCommon
      ),
      tempArr=tempArr,
      finish=true,
    )
    tempArr
  }
  def objAffineTileMemInit(
    cfg: Gpu2dConfig
  ) = {
    def wordType() = Gpu2dTileSlice(
      cfg=cfg,
      isObj=true,
      isAffine=false,
      doPipeMemRmw=false
    )
    val rawArrFgCommon = Gpu2dTestGfx.objFgCommonTileArr
    //val rawArrDbgBlankingFgCommon = (
    //  Gpu2dTestGfx.objDbgBlankingFgCommonTileArr
    //)
    //val rawArrFgGrassland = Gpu2dTestGfx.fgGrasslandTileArr
    //val myPxsSliceWidth = (
    //  Gpu2dTileSlice.pxsSliceWidth(
    //    cfg=cfg,
    //    isObj=true,
    //    isAffine=true,
    //  )
    //)
    val tempArr = new ArrayBuffer[BigInt]()
    //val tempNumTileSlices = (
    //  1 << cfg.objAffineTilePxMemIdxWidth
    //)
    //innerTileMemInit(
    //  wordType=wordType(),
    //  someNumTileSlices=tempNumTileSlices,
    //  somePxsSliceWidth=myPxsSliceWidth,
    //  somePxsArr=(
    //    rawArrFgCommon
    //  ),
    //  tempArr=tempArr,
    //  finish=true,
    //)

    def tempNumObjTileSlices = (
      //if (idx == 0) {
      //  //cfg.numObjTiles
      //  1 << cfg.objTileSliceMemIdxWidth
      //} else {
      //  //cfg.numObjAffineTiles
        1 << cfg.objAffineTilePxMemIdxWidth
      //}
    )
    for (
      idx <- 0 until rawArrFgCommon.size.max(tempNumObjTileSlices)
    ) {
      if (idx < rawArrFgCommon.size) {
        tempArr += BigInt(rawArrFgCommon(idx))
      } else {
        tempArr += BigInt(0)
      }
    }
    tempArr
  }
  def objPalMemInitBigInt(
    cfg: Gpu2dConfig
  ) = {
    def wordCount = (
      //Gpu2dTestGfx.palette.size >> 1
      cfg.numColsInObjPal
    )
    //Mem(
    //  wordType=Rgb(cfg.rgbConfig),
    //  wordCount=wordCount,
    //)
    //.initBigInt({
      val tempArr = new ArrayBuffer[BigInt]
      for (
        //idx <- 0 until (Gpu2dTestGfx.palette.size >> 1)
        idx <- 0 until wordCount
      ) {
        def rgbConfig = cfg.rgbConfig
        //val tempCol = Rgb(rgbConfig)
        val tempRawCol = (
          (Gpu2dTestGfx.objPalette((idx << 1) + 1) << 8)
          | (Gpu2dTestGfx.objPalette(idx << 1) << 0)
        )
        val myTempRawColR = (
          tempRawCol >> (5 - rgbConfig.rWidth)
        )
        val myTempRawColG = (
          //tempRawCol >> (10 - rgbConfig.gWidth)
          //myTempRawColR >> (10 - (
          //  5 + cfg.rgbConfig.gWidth
          //))
          (tempRawCol >> 5) >> (5 - rgbConfig.gWidth)
        )
        val myTempRawColB = (
          (tempRawCol >> 10) >> (5 - rgbConfig.bWidth)
        )
        tempArr += (
          //tempCol
          (
            (myTempRawColR & ((1 << rgbConfig.rWidth) - 1))
            //<< (rgbConfig.bWidth + rgbConfig.gWidth)
          ) | (
            (myTempRawColG & ((1 << rgbConfig.gWidth) - 1))
            << (rgbConfig.rWidth)
            //<< (rgbConfig.bWidth)
          ) | (
            (myTempRawColB & ((1 << rgbConfig.bWidth) - 1))
            << (rgbConfig.rWidth + rgbConfig.gWidth)
          )
        )
        //println(s"${idx} ${tempArr.last}")
      }
      //Some(tempArr.toSeq)
      tempArr
    //})
  }
}
case class Gpu2dTestIo(
  //clkRate: HertzNumber,
  //vgaTimingInfo: LcvVgaTimingInfo,
  //fifoDepth: Int,
  cfg: Gpu2dConfig,
  optRawSnesButtons: Boolean=false,
  dbgPipeMemRmw: Boolean=false,
) extends /*Bundle*/ Bundle with IMasterSlave {
  //--------
  //val gpuIo = master(Gpu2dIo(cfg=cfg))
  val pop = /*master*/(Gpu2dPushInp(
    cfg=cfg,
    dbgPipeMemRmw=dbgPipeMemRmw,
  ))
  val gpu2dPopFire = /*in*/ Bool()
  //val vgaPhys = in(LcvVgaPhys(
  //  rgbConfig=cfg.rgbConfig
  //))
  val vgaSomeVpipeS = /*in*/(LcvVgaState())
  //val vgaSomeDrawPos = in(LcvVgaCtrlMiscIo.coordT(
  //  fbSize2d=cfg.intnlFbSize2d
  //))
  //val vgaMisc = in(LcvVgaCtrlMiscIo(
  //  clkRate=clkRate,
  //  vgaTimingInfo=vgaTimingInfo,
  //  fifoDepth=fifoDepth,
  //))
  //val dbgBg1EntryCntV2d = out(
  //  Vec2(dataType=UInt(16 bits))
  //)
  val snesCtrl = (!optRawSnesButtons) generate master(SnesCtrlIo())
  val rawSnesButtons = (optRawSnesButtons) generate (
    slave Stream(UInt(SnesButtons.rawButtonsWidth bits))
  )
  //--------
  //val ctrlEn = out Bool()
  //val pop = master Stream(Gpu2dPopPayload(cfg=cfg))
  //--------
  override def asMaster(): Unit = mst
  //@modport
  def mst = {
    master(pop)
    in(gpu2dPopFire)
    in(vgaSomeVpipeS)
    if (!optRawSnesButtons) {
      master(snesCtrl)
    } else { // if (optRawSnesButtons)
      slave(rawSnesButtons)
    }
  }

  //@modport
  def slv = {
    slave(pop)
    out(gpu2dPopFire)
    out(vgaSomeVpipeS)
    if (!optRawSnesButtons) {
      slave(snesCtrl)
    } else { // if (optRawSnesButtons)
      master(rawSnesButtons)
    }
  }
  //--------
}
case class Gpu2dTest(
  clkRate: HertzNumber,
  cfg: Gpu2dConfig,
  optRawSnesButtons: Boolean=false,
  dbgPipeMemRmw: Boolean=false,
) extends Component {
  //--------
  val io = master(Gpu2dTestIo(
    cfg=cfg,
    optRawSnesButtons=optRawSnesButtons,
    dbgPipeMemRmw=dbgPipeMemRmw,
  ))
  //def gpuIo = io.gpu2dPush
  //io.ctrlEn := gpuIo.ctrlEn
  ////io.pop <> gpuIo.pop
  ////io.pop <> gpuIo.pop
  //io.pop << io.gpuIo.pop
  def pop = io.pop
  ////--------
  //def palPush(
  //  numColsInPal: Int,
  //  rPalCnt: UInt,
  //  rPalEntry: Gpu2dPalEntry,
  //  rPalEntryPushValid: Bool,
  //  palPushFire: Bool,
  //  //rPalMemAddr: Option[UInt]=None,
  //  somePalMem: Mem[Rgb],
  //): Unit = {
  //  when (rPalCnt < numColsInPal) {
  //    when (palPushFire) {
  //      //when (rPalCnt + 1 === 1) {
  //      //  rPalEntry.col.r := U(
  //      //    //4
  //      //    //0
  //      //    rPalEntry.col.r.getWidth bits,
  //      //    default -> True
  //      //  )
  //      //  rPalEntry.col.g := (
  //      //    //2
  //      //    0
  //      //  )
  //      //  rPalEntry.col.b := U(
  //      //    //4
  //      //    //0
  //      //    rPalEntry.col.b.getWidth bits,
  //      //    default -> True
  //      //  )
  //      //} elsewhen (rPalCnt + 1 === 2) {
  //      //  rPalEntry.col.r := (default -> True)
  //      //  rPalEntry.col.g.msb := True
  //      //  rPalEntry.col.g(rPalEntry.col.g.high - 1 downto 0) := 0x0
  //      //  rPalEntry.col.b := (default -> False)
  //      //} elsewhen (rPalCnt + 1 === 3) {
  //      //  //rPalEntry.col.r := 0x0
  //      //  rPalEntry.col.r := (default -> True)
  //      //  rPalEntry.col.g := (default -> True)
  //      //  //rPalEntry.col.g := 0x3
  //      //  //rPalEntry.col.b := 0x0
  //      //  //rPalEntry.col.b := 0x3
  //      //  //rPalEntry.col.b := 0x6
  //      //} elsewhen (rPalCnt + 1 === 4) {
  //      //  rPalEntry.col.r := 0x0
  //      //  //rPalEntry.col.g := 0x0
  //      //  rPalEntry.col.g := (default -> True)
  //      //  rPalEntry.col.b := (default -> True)
  //      //} elsewhen (rPalCnt + 1 === 5) {
  //      //  rPalEntry.col.r.msb := True
  //      //  rPalEntry.col.r(rPalEntry.col.r.high - 1 downto 0) := 0x0
  //      //  rPalEntry.col.g := 0x0
  //      //  rPalEntry.col.b := (default -> True)
  //      //} elsewhen (rPalCnt + 1 === 6) {
  //      //  rPalEntry.col.r := 0x0
  //      //  rPalEntry.col.g.msb := True
  //      //  rPalEntry.col.g(rPalEntry.col.g.high - 1 downto 0) := 0x0
  //      //  rPalEntry.col.b := 5
  //      //} elsewhen (rPalCnt + 1 === 7) {
  //      //  rPalEntry.col.r := (default -> True)
  //      //  rPalEntry.col.g := (default -> True)
  //      //  rPalEntry.col.b := (default -> True)
  //      //} elsewhen (rPalCnt + 1 === 8) {
  //      //  rPalEntry.col.r := (default -> True)
  //      //  rPalEntry.col.g.msb := True
  //      //  rPalEntry.col.g(rPalEntry.col.g.high - 1 downto 0) := 0x0
  //      //  rPalEntry.col.b := (default -> True)
  //      //} elsewhen (rPalCnt + 1 === 9) {
  //      //  rPalEntry.col.r := (default -> True)
  //      //  rPalEntry.col.g := 0x0
  //      //  rPalEntry.col.b := (
  //      //    (rPalEntry.col.b.high downto rPalEntry.col.b.high - 1) -> True,
  //      //    default -> False
  //      //  )
  //      //} otherwise {
  //      //  rPalEntryPushValid := False
  //      //}
  //      val tempAddr = (rPalCnt + 1)(
  //        //Gpu2dTestGfx.palette.size
  //        //cfg.numColsInBgPalPow - 1 downto 0
  //        log2Up(numColsInPal) - 1 downto 0
  //      )
  //      rPalEntry.col := (
  //        //bgPalMem.readAsync(
  //        //  address=tempAddr
  //        //)
  //        somePalMem.readAsync(
  //          address=tempAddr
  //        )
  //      )
  //      //rPalMemAddr match {
  //      //  case Some(myRPalMemAddr) => {
  //      //    myRPalMemAddr := tempAddr
  //      //  }
  //      //  case None => {
  //      //  }
  //      //};
  //      rPalCnt := rPalCnt + 1
  //      when ((rPalCnt + 1).msb) {
  //        rPalEntryPushValid := False
  //      }
  //    }
  //  }
  //}

  //--------
  // BEGIN: test `mkTile()`
  //def mkTile(
  //  //tempTile: Gpu2dTileFull,
  //  tempTileSlice: Gpu2dTileSlice,
  //  //pxCoordStart: ElabVec2[Int],
  //  pxCoordXStart: UInt,
  //  pxCoordY: UInt,
  //  palEntryMemIdxWidth: Int,
  //  colIdx0: Int,
  //  colIdx1: Int,
  //  colIdx2: Option[Int]=None,
  //  colIdx3: Option[Int]=None,
  //  //onePx: Boolean=false,
  //): Unit = {
  //  switch (pxCoordY) {
  //    for (jdx <- 0 until tempTileSlice.fullPxsSize2d.y) {
  //      is (jdx) {
  //        //def jdx = pxCoordY
  //        //println(tempTileSlice.pxsSliceWidth)
  //        //println(tempTileSlice.pxsSliceWidth)
  //        for (
  //          //idx <- until to tempTileSlice.pxsSize2d.x
  //          idx <- 0 until tempTileSlice.pxsSliceWidth
  //        ) {
  //          //def pxCoord = ElabVec2[Int](idx, jdx)
  //          def pxCoordX = (
  //            //if (!onePx) {
  //              idx + pxCoordXStart
  //            //} else {
  //            //  //pxCoordXStart
  //            //  (
  //            //    idx
  //            //    + U{
  //            //      def tempWidthPow = pxCoordXStart.getWidth
  //            //      f"$tempWidthPow'd0"
  //            //    }
  //            //  )
  //            //}
  //          )
  //          //print(pxCoordX.getWidth)
  //          //print(" ")
  //          //println(f"$idx")
  //          if (jdx % 2 == 0) {
  //            val myColIdx = colIdx2 match {
  //              case Some(tempColIdx) => {
  //                if (idx % 2 == 0) {
  //                  colIdx0
  //                } else {
  //                  tempColIdx
  //                }
  //              }
  //              case None => colIdx0
  //            }
  //            tempTileSlice.setPx(
  //              pxCoordX=pxCoordX,//.resized,
  //              colIdx=U(f"$palEntryMemIdxWidth'd$myColIdx"),
  //            )
  //          } else { // if (jdx % 2 == 1)
  //            val myColIdx = colIdx3 match {
  //              case Some(tempColIdx) => {
  //                if (idx % 2 == 0) {
  //                  colIdx1
  //                } else {
  //                  tempColIdx
  //                }
  //              }
  //              case None => colIdx1
  //            }
  //            tempTileSlice.setPx(
  //              pxCoordX=pxCoordX,//.resized,
  //              colIdx=U(f"$palEntryMemIdxWidth'd$myColIdx"),
  //            )
  //          }
  //        }
  //      }
  //    }
  //  }
  //}
  // END: test `mkTile()`
  //--------
  // BEGIN: color math stuff
  //val nextColorMathTileCnt = SInt(
  //  //cfg.numColorMathTilesPow + 2 bits
  //  cfg.colorMathTileMemIdxWidth + 5 bits
  //)
  //val rColorMathTileCnt = RegNext(nextColorMathTileCnt) init(-1)
  //val rColorMathTilePushValid = Reg(Bool()) init(True)
  ////val tempColorMathTile = Gpu2dTileFull(
  ////  cfg=cfg,
  ////  isObj=false,
  ////  isAffine=false,
  ////)
  //val tempColorMathTileSlice = Gpu2dTileSlice(
  //  cfg=cfg,
  //  isObj=false,
  //  isAffine=false,
  //)
  //def mkColorMathTile(
  //  colIdx0: Int,
  //  colIdx1: Int,
  //  colIdx2: Option[Int]=None,
  //  colIdx3: Option[Int]=None,
  //): Unit = {
  //  mkTile(
  //    //tempTile=tempColorMathTile,
  //    tempTileSlice=tempColorMathTileSlice,
  //    pxCoordXStart={
  //      //rColorMathTileCnt(cfg.bgTileSize2dPow.x - 1 downto 0).asUInt
  //      def tempWidthPow = cfg.bgTileSize2dPow.x
  //      U(f"$tempWidthPow'd0")
  //    },
  //    pxCoordY=(
  //      rColorMathTileCnt(
  //        cfg.bgTileSize2dPow.y - 1
  //        //downto cfg.bgTileSize2dPow.x
  //        downto 0
  //      ).asUInt
  //    ),
  //    palEntryMemIdxWidth=cfg.bgPalEntryMemIdxWidth,
  //    colIdx0=colIdx0,
  //    colIdx1=colIdx1,
  //    colIdx2=colIdx2,
  //    colIdx3=colIdx3,
  //  )
  //}

  //def tempColorMathTileCnt = (
  //  rColorMathTileCnt
  //  //>> (cfg.bgTileSize2dPow.y + cfg.bgTileSize2dPow.x)
  //  >> cfg.bgTileSize2dPow.y
  //)
  //tempColorMathTileSlice := tempColorMathTileSlice.getZero
  //tempColorMathTileSlice.allowOverride
  //when (
  //  //rColorMathTileCnt < cfg.numColorMathTiles
  //  tempColorMathTileCnt < cfg.numColorMathTiles
  //) {
  //  when (pop.colorMathTilePush.fire) {
  //    when (tempColorMathTileCnt === 0) {
  //      mkColorMathTile(0, 0)
  //    } elsewhen (tempColorMathTileCnt === 1) {
  //      mkColorMathTile(1, 1)
  //    } elsewhen (tempColorMathTileCnt === 2) {
  //      mkColorMathTile(2, 2)
  //    } elsewhen (tempColorMathTileCnt === 3) {
  //      mkColorMathTile(3, 3)
  //    } elsewhen (tempColorMathTileCnt === 4) {
  //      mkColorMathTile(4, 4)
  //    } otherwise {
  //      //tempColorMathTileSlice := tempColorMathTileSlice.getZero
  //    }
  //    nextColorMathTileCnt := rColorMathTileCnt + 1
  //  } otherwise {
  //    //tempColorMathTileSlice := tempColorMathTileSlice.getZero
  //    nextColorMathTileCnt := rColorMathTileCnt
  //  }
  //} otherwise {
  //  //tempColorMathTileSlice := tempColorMathTileSlice.getZero
  //  nextColorMathTileCnt := rColorMathTileCnt
  //}
  //when (tempColorMathTileCnt >= cfg.numColorMathTiles - 1) {
  //  rColorMathTilePushValid := False
  //}

  //pop.colorMathTilePush.valid := rColorMathTilePushValid
  ////pop.colorMathTilePush.payload.tile := tempColorMathTile
  //pop.colorMathTilePush.payload.tileSlice := tempColorMathTileSlice
  //pop.colorMathTilePush.payload.memIdx := (
  //  rColorMathTileCnt.asUInt(
  //    pop.colorMathTilePush.payload.memIdx.bitsRange
  //  )
  //)
  ////pop.colorMathTilePush.valid := False
  ////pop.colorMathTilePush.payload := pop.colorMathTilePush.payload.getZero

  ////pop.colorMathEntryPush.valid := False
  ////pop.colorMathEntryPush.payload := pop.colorMathEntryPush.payload.getZero
  //val rColorMathEntryPushValid = Reg(Bool()) init(True)
  //val tempColorMathEntry = Gpu2dBgEntry(
  //  cfg=cfg,
  //  isColorMath=true,
  //)
  ////val colorMathEntryCntWidth = cfg.numColorMathsPow + 2
  //val colorMathEntryCntWidth = cfg.bgEntryMemIdxWidth + 2
  //val nextColorMathEntryCnt = SInt(colorMathEntryCntWidth bits)
  //val rColorMathEntryCnt = RegNext(nextColorMathEntryCnt) init(-1)
  ////val rColorMathEntryPushValid = Reg(Bool()) init(True)
  //// we're only changing one tile
  ////tempColorMathEntry.tileMemIdx := 1
  ////tempColorMathEntry.dispFlip.x := False
  ////tempColorMathEntry.dispFlip.y := False

  //val rColorMathEntryMemIdx = Reg(SInt(
  //  (cfg.bgEntryMemIdxWidth + 1) bits
  //))
  //  .init((1 << cfg.bgEntryMemIdxWidth) - 1)
  //when (rColorMathEntryCnt < (1 << cfg.bgEntryMemIdxWidth)) {
  //  when (pop.colorMathEntryPush.fire) {
  //    when (
  //      //rColorMathEntryCnt < 5
  //      rColorMathEntryCnt < cfg.bgSize2dInTiles.x
  //    ) {
  //      //tempColorMathEntry.tileIdx := rColorMathEntryCnt.asUInt.resized
  //      tempColorMathEntry.tileIdx := 1
  //      tempColorMathEntry.dispFlip.x := False
  //      tempColorMathEntry.dispFlip.y := False
  //    } otherwise {
  //      tempColorMathEntry := tempColorMathEntry.getZero
  //      //when (rColorMathEntryCnt >= cfg.numColorMathEntrys) {
  //      //  rColorMathEntryPushValid := False
  //      //}
  //    }
  //    //tempColorMathEntry.tileIdx := 1
  //    //tempColorMathEntry.dispFlip.x := False
  //    //tempColorMathEntry.dispFlip.y := False
  //    nextColorMathEntryCnt := rColorMathEntryCnt + 1
  //  } otherwise {
  //    tempColorMathEntry := tempColorMathEntry.getZero
  //    nextColorMathEntryCnt := rColorMathEntryCnt
  //  }
  //} otherwise {
  //  tempColorMathEntry := tempColorMathEntry.getZero
  //  nextColorMathEntryCnt := rColorMathEntryCnt
  //}
  //when (rColorMathEntryCnt + 1
  //  >= (1 << cfg.bgEntryMemIdxWidth)) {
  //  rColorMathEntryPushValid := False
  //}

  //pop.colorMathEntryPush.valid := rColorMathEntryPushValid
  ////tempColorMathEntryPush.payload.colorMathEntry.tileMemIdx := (
  ////  rColorMathEntryMemIdx.asUInt(
  ////    cfg.bgEntryMemIdxWidth - 1 downto 0
  ////  )
  ////)
  //pop.colorMathEntryPush.payload.bgEntry := tempColorMathEntry
  //pop.colorMathEntryPush.payload.memIdx := (
  //  rColorMathEntryCnt.asUInt(
  //    pop.colorMathEntryPush.payload.memIdx.bitsRange
  //  )
  //)

  //pop.colorMathAttrsPush.valid := True
  //pop.colorMathAttrsPush.payload := pop.colorMathAttrsPush.payload.getZero
  //pop.colorMathAttrsPush.payload.allowOverride
  //pop.colorMathAttrsPush.payload.bgAttrs.scroll.x := (
  //  //cfg.bgTileSize2d.x * 1
  //  0
  //)
  //pop.colorMathAttrsPush.payload.bgAttrs.scroll.y := (
  //  //cfg.bgTileSize2d.y * 1
  //  0
  //)
  // END: color math stuff
  //--------
  //def colorMathPalCntWidth = cfg.numColsInBgPalPow + 1
  //val rColorMathPalCnt = Reg(UInt(colorMathPalCntWidth bits)) init(0x0)
  //val rColorMathPalEntry = Reg(Gpu2dPalEntry(cfg=cfg))
  //rColorMathPalEntry.init(rColorMathPalEntry.getZero)
  //val rColorMathPalEntryPushValid = Reg(Bool()) init(True)

  ////pop.colorMathPalEntryPush.valid := True
  //pop.colorMathPalEntryPush.valid := rColorMathPalEntryPushValid
  //pop.colorMathPalEntryPush.payload.bgPalEntry := rColorMathPalEntry
  ////pop.colorMathPalEntryPush.payload.memIdx := 1
  //pop.colorMathPalEntryPush.payload.memIdx := rColorMathPalCnt.resized
  pop.colorMathPalEntryPush.valid := False
  pop.colorMathPalEntryPush.payload := (
    pop.colorMathPalEntryPush.payload.getZero
  )

  //otherwise {
  //}
  //palPush(
  //  numColsInPal=cfg.numColsInBgPal,
  //  rPalCnt=rColorMathPalCnt,
  //  rPalEntry=rColorMathPalEntry,
  //  rPalEntryPushValid=rColorMathPalEntryPushValid,
  //  palPushFire=pop.bgPalEntryPush.fire,
  //  somePalMem=bgPalMem,
  //)
  //pop.colorMathPalEntryPush.valid := False
  //pop.colorMathPalEntryPush.payload := (
  //  pop.colorMathPalEntryPush.payload.getZero
  //)
  //--------
  //val tempBgTile = Gpu2dTileFull(
  //  cfg=cfg,
  //  isObj=false,
  //  isAffine=false,
  //)
  val tempBgTileSlice = Gpu2dTileSlice(
    cfg=cfg,
    isObj=false,
    isAffine=false,
  )

  val nextBgTileCnt = SInt(
    //cfg.numBgTilesPow + 2 bits
    cfg.bgTileMemIdxWidth + 5 bits
  )
  val rBgTileCnt = RegNext(nextBgTileCnt) init(0)
  val rBgTilePushValid = Reg(Bool()) init(True)
  //--------
  //def mkBgTile(
  //  colIdx0: Int,
  //  colIdx1: Int,
  //  colIdx2: Option[Int]=None,
  //  colIdx3: Option[Int]=None,
  //): Unit = {
  //  //mkTile(
  //  //  //tempTile=tempBgTile,
  //  //  colIdx0=colIdx0,
  //  //  colIdx1=colIdx1,
  //  //  colIdx2=colIdx2,
  //  //  colIdx3=colIdx3,
  //  //)
  //  mkTile(
  //    //tempTile=tempColorMathTile,
  //    tempTileSlice=tempBgTileSlice,
  //    pxCoordXStart={
  //      //rBgTileCnt(cfg.bgTileSize2dPow.x - 1 downto 0).asUInt
  //      //0
  //      def tempWidthPow = cfg.bgTileSize2dPow.x
  //      U(f"$tempWidthPow'd0")
  //    },
  //    pxCoordY=(
  //      rBgTileCnt(
  //        cfg.bgTileSize2dPow.y - 1
  //        //downto cfg.bgTileSize2dPow.x
  //        downto 0
  //      ).asUInt
  //    ),
  //    palEntryMemIdxWidth=cfg.bgPalEntryMemIdxWidth,
  //    colIdx0=colIdx0,
  //    colIdx1=colIdx1,
  //    colIdx2=colIdx2,
  //    colIdx3=colIdx3,
  //  )
  //}
  //--------
  //val rawArrFgCommon = Gpu2dTestGfx.fgCommonTileArr
  //val rawArrFgGrassland = Gpu2dTestGfx.fgGrasslandTileArr
  //val myBgTileMemInit = (
  //  Gpu2dTest.bgTileMemInit(cfg=cfg)
  //)
  //val bgTileMem = Mem(
  //  wordType=Gpu2dTileSlice(
  //    cfg=cfg,
  //    isObj=false,
  //    isAffine=false,
  //    doPipeMemRmw=false
  //  ),
  //  wordCount={
  //    //val myPxsSliceWidth = (
  //    //  Gpu2dTileSlice.pxsSliceWidth(
  //    //    cfg=cfg,
  //    //    isObj=false,
  //    //    isAffine=false,
  //    //  )
  //    //)
  //    //(
  //    //  (rawArrFgCommon.size + rawArrFgGrassland.size)
  //    //  >> log2Up(myPxsSliceWidth)
  //    //)
  //    myBgTileMemInit.size
  //  }
  //)
  //  .init(myBgTileMemInit.toSeq)
  //  .setName("Gpu2dTest_bgTileMem")

  def tempBgTileCnt = (
    rBgTileCnt
    >> cfg.bgTileSize2dPow.y
  )
  ////tempBgTileSlice := tempBgTileSlice.getZero
  tempBgTileSlice := (
    RegNext(tempBgTileSlice) init(tempBgTileSlice.getZero)
  )
  tempBgTileSlice.allowOverride

  nextBgTileCnt := rBgTileCnt
  //when (
  //  //rBgTileCnt < cfg.numBgTiles
  //  tempBgTileCnt < cfg.numBgTiles
  //  //rBgTileCnt < 
  //) {
  //  when (pop.bgTilePush.fire) {
  //    //--------
  //    // BEGIN: old, geometrical shapes graphics
  //    when (tempBgTileCnt === 0) {
  //      mkBgTile(0, 0)
  //      //mkBgTile(0, 1, Some(0), Some(1))
  //    } elsewhen (tempBgTileCnt === 1) {
  //      //mkBgTile(1, 2)
  //      mkBgTile(1, 2, Some(1), Some(2))
  //      //mkBgTile(1, 2, Some(3), Some(4))
  //      //mkObjTile(2, 2)
  //      //mkBgTile(1, 1)
  //      //mkBgTile(3, 3)
  //      //mkBgTile(2, 3)
  //    } otherwise {
  //      mkBgTile(0, 0)
  //    }
  //    //elsewhen (tempBgTileCnt === 2) {
  //    //  //mkBgTile(2, 3)
  //    //  //mkBgTile(3, 4)
  //    //  mkBgTile(2, 2)
  //    //  //mkBgTile(3, 3)
  //    //  //mkBgTile(2, 2)
  //    //} elsewhen (tempBgTileCnt === 3) {
  //    //  //mkBgTile(3, 4)
  //    //  mkBgTile(3, 3)
  //    //  //mkBgTile(4, 4)
  //    //  //mkBgTile(0, 1)
  //    //} elsewhen (tempBgTileCnt === 4) {
  //    //  //mkBgTile(4, 5)
  //    //  mkBgTile(4, 4, Some(5), Some(5))
  //    //  //mkBgTile(5, 5)
  //    //} elsewhen (tempBgTileCnt === 5) {
  //    //  mkBgTile(5, 6, Some(7), Some(8))
  //    //  //mkBgTile(6, 6)
  //    //} elsewhen (tempBgTileCnt === 6) {
  //    //  mkBgTile(8, 9, Some(8), Some(9))
  //    //} elsewhen (tempBgTileCnt === 7) {
  //    //  mkBgTile(10, 11, Some(12), Some(13))
  //    //} elsewhen (tempBgTileCnt === 8) {
  //    //  mkBgTile(14, 15, Some(14), Some(15))
  //    //}
  //    // END: old, geometrical shapes graphics
  //    //--------
  //    //elsewhen (
  //    //  tempBgTileCnt
  //    //  === (
  //    //    //cfg.bgSize2dInTiles.x / cfg.bgTileSize2d.x
  //    //    //cfg.bgSize2dInPxs.x
  //    //    cfg.intnlFbSize2d.x
  //    //    / (
  //    //      cfg.bgTileSize2d.x * cfg.bgTileSize2d.y
  //    //    )
  //    //  )
  //    //) {
  //    //  // test that there's a tile at the second row
  //    //  //mkBgTile(1, 2, Some(3), Some(4))
  //    //  mkBgTile(1, 1)
  //    //} 
  //    //elsewhen (
  //    //  tempBgTileCnt
  //    //  === (
  //    //    //cfg.bgSize2dInTiles.x / cfg.bgTileSize2d.x
  //    //    (
  //    //      //cfg.bgSize2dInPxs.x
  //    //      cfg.intnlFbSize2d.x
  //    //      / (
  //    //        cfg.bgTileSize2d.x * cfg.bgTileSize2d.y
  //    //      )
  //    //    ) + 1
  //    //  )
  //    //) {
  //    //  // test that there's a tile at the second row
  //    //  //mkBgTile(1, 2, Some(3), Some(4))
  //    //  mkBgTile(2, 2)
  //    //} otherwise {
  //    //  //tempBgTileSlice := tempBgTileSlice.getZero
  //    //  //when (rBgTileCnt >= cfg.numBgTiles) {
  //    //  //  rBgTilePushValid := False
  //    //  //}
  //    //}
  //    //--------
  //    //when (
  //    //  //rBgTileCnt < bgTileMem.wordCount
  //    //  //tempBgTileCnt < bgTileMem.wordCount
  //    //  //if (
  //    //  //  (1 << tempBgTileCnt.getWidth) < bgTileMem.wordCount
  //    //  //) (
  //    //  //  True
  //    //  //) else (
  //    //    //tempBgTileCnt.resized < bgTileMem.wordCount
  //    //    Cat(
  //    //      U(s"${log2Up(bgTileMem.wordCount)}'d0"),
  //    //      tempBgTileCnt 
  //    //    ).asUInt < bgTileMem.wordCount
  //    //  //)
  //    //) {
  //    //  tempBgTileSlice := bgTileMem.readSync(
  //    //    address=nextBgTileCnt.asUInt.resized
  //    //  )
  //    //  //tempBgTileSlice := bgTileMem.readAsync(
  //    //  //  address=rBgTileCnt.asUInt.resized
  //    //  //)
  //    //} otherwise {
  //    //  tempBgTileSlice := tempBgTileSlice.getZero
  //    //}
  //    //--------
  //    nextBgTileCnt := rBgTileCnt + 1
  //  } otherwise {
  //    ////tempBgTileSlice := tempBgTileSlice.getZero
  //    //nextBgTileCnt := rBgTileCnt
  //  }
  //} otherwise {
  //  ////tempBgTileSlice := tempBgTileSlice.getZero
  //  //nextBgTileCnt := rBgTileCnt
  //}
  when (
    tempBgTileCnt >= cfg.numBgTiles - 1
  ) {
    rBgTilePushValid := False
  }

  pop.bgTilePush.valid := (
    //rBgTilePushValid
    False
    //True
  )
  pop.bgTilePush.payload := pop.bgTilePush.payload.getZero
  //pop.bgTilePush.tileSlice := tempBgTileSlice
  //pop.bgTilePush.memIdx := (
  //  rBgTileCnt.asUInt(pop.bgTilePush.payload.memIdx.bitsRange)
  //)
  ////--------
  //pop.bgTilePush.valid := rBgTilePushValid
  ////--------
  ////pop.bgTilePush.valid := True
  //// when (!rBgTileCnt.msb) {
  //  pop.bgTilePush.payload.tile := tempBgTileToPush
  //  pop.bgTilePush.payload.memIdx := (
  //    rBgTileCnt.asUInt(cfg.numBgTilesPow - 1 downto 0)
  //  )
  ////} otherwise {
  ////  pop.bgTilePush.payload.tile := rBgTile
  ////  pop.bgTilePush.payload.memIdx := (
  ////    rBgTileCnt(cfg.numBgTilesPow - 1 downto 0)
  ////  )
  ////}
  ////pop.bgTilePush.payload.memIdx := cfg.intnlFbSize2d.x
  //--------
  val tempBgAttrs = Gpu2dBgAttrs(
    cfg=cfg,
    isColorMath=false,
  )
  //tempBgAttrs.colorMathInfo := tempBgAttrs.colorMathInfo.getZero
  //tempBgAttrs.fbAttrs.doIt := True
  //tempBgAttrs.fbAttrs.tileMemBaseAddr := 0
  if (!cfg.noColorMath) {
    tempBgAttrs.colorMathInfo.doIt := True
    tempBgAttrs.colorMathInfo.kind := (
      //Gpu2dColorMathKind.add
      //Gpu2dColorMathKind.sub
      Gpu2dColorMathKind.avg
      //Gpu2dColorMathKind.avg
    )
  } else {
    //tempBgAttrs.colorMathInfo := tempBgAttrs.colorMathInfo.getZero
  }
  //val tempBgScroll = DualTypeNumVec2(
  //  dataTypeX=SInt(tempBgAttrs.scroll.x.getWidth bits),
  //  dataTypeY=SInt(tempBgAttrs.scroll.y.getWidth bits),
  //)
  //tempBgAttrs.scroll := tempBgAttrs.scroll.getZero
  //tempBgAttrs.scroll.x := 0
  //tempBgAttrs.scroll.x := 1
  //val myDefaultBgScroll = cloneOf(tempBgAttrs.scroll)
  val myDefaultBgScroll = ElabVec2[Int](
    x=(
      //0
      //////2
      ////cfg.bgTileSize2d.x
      ////+ 2
      //////+ 1
      //cfg.bgTileSize2d.x * 5
      //0
      //0

      //cfg.bgTileSize2d.x * cfg.bgSize2dInTiles
      //cfg.intnlFbSize2d.x - (cfg.bgTileSize2d.x * 3)
      //320 - 64

      ////0x29
      //(
      //  (1 << tempBgAttrs.scroll.x.getWidth) - 32
      //)
      4 * cfg.bgTileSize2d.x
    ),
    y=(
      //2
      //0
      //7 * cfg.bgTileSize2d.y
      //2 * cfg.bgTileSize2d.y
      1 * cfg.bgTileSize2d.y
      //1
    )
  )
  //tempBgAttrs.scroll.x := 3
  //tempBgScroll.x := (-cfg.bgTileSize2d.x) + 1
  //tempBgScroll.x := 1
  //tempBgAttrs.scroll.x := tempBgScroll.x.asUInt
  //tempBgAttrs.scroll.x := 0
  //tempBgAttrs.scroll.x := (default -> True)
  //myDefaultBgScroll.y := (
  //  //2
  //  0
  //  //1
  //)
  //tempBgAttrs.scroll.x := 6
  //tempBgAttrs.scroll.y := 5
  //tempBgAttrs.visib := True
  //tempBgAttrs.scroll.allowOverride

  tempBgAttrs.scroll.x := (
    RegNext(tempBgAttrs.scroll.x) init(myDefaultBgScroll.x)
  )
  tempBgAttrs.scroll.y := (
    RegNext(tempBgAttrs.scroll.y) init(myDefaultBgScroll.y)
  )
  //--------
  ////tempBgAttrs.visib := False
  //tempBgAttrs.fbAttrs := tempBgAttrs.fbAttrs.getZero
  tempBgAttrs.fbAttrs.doIt := (
    //True
    False
  )
  tempBgAttrs.fbAttrs.tileMemBaseAddr := 0
  val tempBgAttrs0PushValid = Bool()
  tempBgAttrs0PushValid := RegNext(tempBgAttrs0PushValid) init(True)
  for (idx <- 0 until pop.bgAttrsPushVec.size) {
    def tempBgAttrsPush = pop.bgAttrsPushVec(idx)
    if (idx == 0) {
      //tempBgAttrsPush.valid := True
      tempBgAttrsPush.valid := tempBgAttrs0PushValid
      tempBgAttrsPush.payload.bgAttrs := tempBgAttrs
    } else {
      //tempBgAttrsPush.valid := False
      tempBgAttrsPush.valid := True
      tempBgAttrsPush.payload.bgAttrs := tempBgAttrs.getZero
    }
  }

  val rBgEntryPushValid = Reg(Bool()) init(True)
  val tempBgEntry = Gpu2dBgEntry(
    cfg=cfg,
    isColorMath=false,
  )
  //val bgEntryCntWidth = cfg.numBgsPow + 2
  val bgEntryCntWidth = cfg.bgEntryMemIdxWidth + 2
  val nextBg0EntryCnt = SInt(bgEntryCntWidth bits)
  val rBg0EntryCnt = RegNext(nextBg0EntryCnt) init(-1)
  val nextBg1EntryCnt = SInt(bgEntryCntWidth bits)
  val rBg1EntryCnt = RegNext(nextBg1EntryCnt) init(-1)
  //val nextBg1EntryExtCntV2d = DualTypeNumVec2(
  //  dataTypeX=UInt(cfg.bgSize2dInTilesPow.x bits),
  //  dataTypeY=UInt(cfg.bgSize2dInTilesPow.y bits),
  //)
  //val rBg1EntryExtCntV2d = (
  //  RegNext(nextBg1EntryExtCntV2d) init(nextBg1EntryExtCntV2d.getZero)
  //)
  //val nextBg1EntryCntV2d = DualTypeNumVec2(
  //  dataTypeX=UInt(cfg.bgSize2dInTilesPow.x bits),
  //  //dataTypeY=UInt(cfg.bgSize2dInTilesPow.y bits),
  //  //dataTypeX=UInt(16 bits),
  //  dataTypeY=UInt(16 bits),
  //)
  //  .addAttribute("keep")
  //  .setName("nextBg1EntryCntV2d")
  //val nextBg1EntryCntV2d = new Bundle {
  //  val x = UInt(cfg.bgSize2dInTilesPow.x bits)
  //  val y = UInt(cfg.bgSize2dInTilesPow.y bits)
  //}
  //  .setName("nextBg1EntryCntV2d")
  //  .addAttribute("keep")
  //--------
  //val rBg1EntryCntV2d = (
  //  RegNext(nextBg1EntryCntV2d)
  //  //init(nextBg1EntryCntV2d.getZero)
  //  .addAttribute("keep")
  //  .setName("rBg1EntryCntV2d")
  //)
  //rBg1EntryCntV2d.x.init(
  //  0x0
  //)
  //val myBg1EntryCntYInit = (
  //  //8
  //  0x0
  //)
  //rBg1EntryCntV2d.y.init(
  //  myBg1EntryCntYInit
  //)
  //nextBg1EntryCntV2d := rBg1EntryCntV2d
  //nextBg1EntryExtCntV2d := rBg1EntryExtCntV2d
  //nextBg1EntryCntV2d.allowOverride
  //nextBg1EntryExtCntV2d.allowOverride
  //--------
  //val nextBg1EntryCnt = SInt(bgEntryCntWidth bits)
  //val rBg1EntryCnt = RegNext(nextBg1EntryCnt) init(-1)
  //val rBgEntryPushValid = Reg(Bool()) init(True)
  // we're only changing one tile
  //tempBgEntry.tileMemIdx := 1
  //tempBgEntry.dispFlip.x := False
  //tempBgEntry.dispFlip.y := False

  val rBgEntryMemIdx = Reg(SInt((cfg.bgEntryMemIdxWidth + 1) bits))
    .init((1 << cfg.bgEntryMemIdxWidth) - 1)

  //for (idx <- 0 to pop.bgEntryPushVec.size - 1) {
  //  val tempBgEntryPush = pop.bgEntryPushVec(idx)
  //  if (idx == 0) {
  //    when (rBgEntryMemIdx === 0) {
  //      tempBgEntryPush.payload.bgEntry := tempBgEntry
  //    } otherwise {
  //      tempBgEntryPush.payload.bgEntry := (
  //        tempBgEntryPush.payload.bgEntry.getZero
  //      )
  //    }
  //    //tempBgEntryPush.payload.memIdx := 0x1
  //    //tempBgEntryPush.payload.memIdx := 0x0
  //    tempBgEntryPush.payload.memIdx := rBgEntryMemIdx.asUInt.resized
  //    when (!rBgEntryMemIdx.msb) {
  //      tempBgEntryPush.valid := True
  //      when (tempBgEntryPush.fire) {
  //        rBgEntryMemIdx := rBgEntryMemIdx  - 1
  //      }
  //    } otherwise {
  //      tempBgEntryPush.valid := False
  //    }
  //  } else {
  //    //tempBgEntryPush.valid := False
  //    tempBgEntryPush.valid := True
  //    tempBgEntryPush.payload.bgEntry := tempBgEntry.getZero
  //    tempBgEntryPush.payload.memIdx := 0x0
  //  }
  //}
  //println(cfg.bgEntryMemIdxWidth)
  //tempBgEntry := tempBgEntry.getZero
  //nextBgEntryCnt := rBgEntryCnt
  //for (idx <- 0 until pop.bgEntryPushVec.size) {
  //  def tempBgEntryPush = pop.bgEntryPushVec(idx)
  //  if (idx == 0) {
  //    when (rBgEntryCnt < (1 << cfg.bgEntryMemIdxWidth)) {
  //      when (tempBgEntryPush.fire) {
  //        //when (rBgEntryCnt === 0) {
  //        //  //mkBgEntry(0, 1)
  //        //  //mkBgEntry(0, 0)
  //        //} elsewhen (rBgEntryCnt === 1) {
  //        //  //mkBgEntry(1, 2)
  //        //  //mkBgEntry(1, 1)
  //        //  //mkBgEntry(3, 3)
  //        //  //mkBgEntry(2, 3)
  //        //} elsewhen (rBgEntryCnt === 2) {
  //        //  //mkBgEntry(2, 3)
  //        //  //mkBgEntry(3, 4)
  //        //  //mkBgEntry(2, 2)
  //        //  //mkBgEntry(2, 2)
  //        //} elsewhen (rBgEntryCnt === 3) {
  //        //  //mkBgEntry(3, 4)
  //        //  //mkBgEntry(3, 3)
  //        //  //mkBgEntry(0, 1)
  //        //} elsewhen (rBgEntryCnt === 4) {
  //        //  //mkBgEntry(4, 5)
  //        //  //mkBgEntry(4, 4)
  //        //} otherwise 
  //        when (
  //          //rBgEntryCnt < 128
  //          //rBgEntryCnt < (1 << cfg.bgEntryMemIdxWidth)
  //          if (rBgEntryCnt.getWidth > tempBgEntry.tileIdx.getWidth) (
  //            rBgEntryCnt.resized
  //            < (1 << tempBgEntry.tileIdx.getWidth)
  //          ) else (
  //            True
  //          )
  //        ) {
  //          tempBgEntry.tileIdx := rBgEntryCnt.asUInt.resized
  //          tempBgEntry.dispFlip.x := False
  //          tempBgEntry.dispFlip.y := False
  //        } otherwise {
  //          tempBgEntry := tempBgEntry.getZero
  //          //when (rBgEntryCnt >= cfg.numBgEntrys) {
  //          //  rBgEntryPushValid := False
  //          //}
  //        }
  //        nextBgEntryCnt := rBgEntryCnt + 1
  //      } otherwise {
  //        //tempBgEntry := tempBgEntry.getZero
  //        //nextBgEntryCnt := rBgEntryCnt
  //      }
  //    } otherwise {
  //      //tempBgEntry := tempBgEntry.getZero
  //      //nextBgEntryCnt := rBgEntryCnt
  //    }
  //    when (rBgEntryCnt + 1 >= (1 << cfg.bgEntryMemIdxWidth)) {
  //      rBgEntryPushValid := False
  //    }


  //    tempBgEntryPush.valid := rBgEntryPushValid
  //    //tempBgEntryPush.payload.bgEntry.tileMemIdx := (
  //    //  rBgEntryMemIdx.asUInt(
  //    //    cfg.bgEntryMemIdxWidth - 1 downto 0
  //    //  )
  //    //)
  //    tempBgEntryPush.payload.bgEntry := tempBgEntry
  //    tempBgEntryPush.payload.memIdx := (
  //      rBgEntryCnt.asUInt(pop.bgEntryPushVec(0).payload.memIdx.bitsRange)
  //    )
  //  } else if (idx == 1) {
  //    tempBgEntryPush.valid := (
  //      //True
  //      False
  //    )
  //    val myPayload = (
  //      KeepAttribute(cloneOf(tempBgEntryPush.payload))
  //      .setName(f"myTempBgEntryPushPayload_$idx")
  //    )
  //    tempBgEntryPush.payload := myPayload

  //    myPayload.memIdx := (
  //      rBgEntryCnt.asUInt(pop.bgEntryPushVec(0).payload.memIdx.bitsRange)
  //    )
  //    myPayload.bgEntry := myPayload.bgEntry.getZero
  //    myPayload.bgEntry.allowOverride
  //    //myPayload.bgEntry.tileIdx := rBgEntryCnt.asUInt.resized
  //    myPayload.bgEntry.tileIdx := (
  //      //3
  //      //4
  //      if (cfg.bgTileSize2d.x == 8) (
  //        3 * 4
  //      ) else (
  //        3
  //      )
  //    )
  //    //myPayload.bgEntry.dispFlip.x := True
  //    //myPayload.bgEntry.dispFlip.y := True
  //    myPayload.bgEntry.dispFlip.x := rBgEntryCnt(0)
  //    myPayload.bgEntry.dispFlip.y := rBgEntryCnt(1)
  //  } else {
  //    tempBgEntryPush.valid := True
  //    tempBgEntryPush.payload.bgEntry := tempBgEntry.getZero
  //    //tempBgEntryPush.bgEntry := 
  //    //tempBgEntryPush.payload.memIdx := 0x0
  //  }
  //}
  tempBgEntry := tempBgEntry.getZero
  nextBg0EntryCnt := rBg0EntryCnt
  nextBg1EntryCnt := rBg1EntryCnt
  //nextBg1EntryCnt := rBg1EntryCnt
  for (idx <- 0 until pop.bgEntryPushVec.size) {
    def tempBgEntryPush = pop.bgEntryPushVec(idx)
    //if (idx == 0) {
    //  //tempBgEntryPush.valid := tempBgEntryPush.valid.getZero
    //  //tempBgEntryPush.payload := tempBgEntryPush.payload.getZero
    //  when (rBg0EntryCnt < (1 << cfg.bgEntryMemIdxWidth)) {
    //    when (tempBgEntryPush.fire) {
    //      //when (rBgEntryCnt === 0) {
    //      //  //mkBgEntry(0, 1)
    //      //  //mkBgEntry(0, 0)
    //      //} elsewhen (rBgEntryCnt === 1) {
    //      //  //mkBgEntry(1, 2)
    //      //  //mkBgEntry(1, 1)
    //      //  //mkBgEntry(3, 3)
    //      //  //mkBgEntry(2, 3)
    //      //} elsewhen (rBgEntryCnt === 2) {
    //      //  //mkBgEntry(2, 3)
    //      //  //mkBgEntry(3, 4)
    //      //  //mkBgEntry(2, 2)
    //      //  //mkBgEntry(2, 2)
    //      //} elsewhen (rBgEntryCnt === 3) {
    //      //  //mkBgEntry(3, 4)
    //      //  //mkBgEntry(3, 3)
    //      //  //mkBgEntry(0, 1)
    //      //} elsewhen (rBgEntryCnt === 4) {
    //      //  //mkBgEntry(4, 5)
    //      //  //mkBgEntry(4, 4)
    //      //} otherwise 
    //      when (
    //        //rBgEntryCnt < 128
    //        //rBgEntryCnt < (1 << cfg.bgEntryMemIdxWidth)
    //        if (rBg0EntryCnt.getWidth > tempBgEntry.tileIdx.getWidth) (
    //          rBg0EntryCnt.asUInt.resized
    //          < (1 << tempBgEntry.tileIdx.getWidth)
    //        ) else (
    //          True
    //        )
    //      ) {
    //        tempBgEntry.tileIdx := (
    //          ({
    //            val tempSize = (
    //              Gpu2dTestGfx.sampleBgTileArr.size
    //              / (
    //                //ElabVec2.magSquared(cfg.bgTileSize2d)
    //                cfg.bgTileSize2d.x * cfg.bgTileSize2d.y
    //              )
    //            )
    //            val tempCnt = (
    //              rBg0EntryCnt.asUInt//(tempBgEntry.tileIdx.bitsRange)
    //              + {
    //                //val temp = (
    //                //  /// cfg.numBgTiles
    //                //)
    //                ////println(f"tileIdx addend: $temp")
    //                //temp
    //                tempSize
    //              }
    //            )
    //            val tempCnt1 = UInt(
    //              max(
    //                //log2Up(Gpu2dTestGfx.sampleBgTileArr.size),
    //                tempBgEntry.tileIdx.getWidth,
    //                tempCnt.getWidth,
    //              ) bits
    //            )
    //            tempCnt1 := tempCnt.resized
    //            Mux[UInt](
    //              tempCnt1
    //              > (
    //                //Gpu2dTestGfx.sampleBgTileArr.size,
    //                tempSize
    //              ),
    //              tempCnt1,
    //              U(s"${tempCnt1.getWidth}'d0")
    //            )
    //            tempCnt1
    //          }).resized/*(tempBgEntry.tileIdx.bitsRange)*/ //.asUInt
    //        )
    //        tempBgEntry.dispFlip.x := False
    //        tempBgEntry.dispFlip.y := False
    //        //tempBgEntry := tempBgEntry.getZero
    //      } otherwise {
    //        tempBgEntry := tempBgEntry.getZero
    //        //when (rBgEntryCnt >= cfg.numBgEntrys) {
    //        //  rBgEntryPushValid := False
    //        //}
    //      }
    //      nextBg0EntryCnt := rBg0EntryCnt + 1
    //    } otherwise {
    //      //tempBgEntry := tempBgEntry.getZero
    //      //nextBgEntryCnt := rBgEntryCnt
    //    }
    //  } otherwise {
    //    //tempBgEntry := tempBgEntry.getZero
    //    //nextBgEntryCnt := rBgEntryCnt
    //  }
    //  when (
    //    rBg0EntryCnt + 1
    //    >= (
    //      //1 << cfg.bgEntryMemIdxWidth
    //      //(Gpu2dTestGfx.fgCommonTileArr.size / (16 * 16))
    //      //+ (Gpu2dTestGfx.fgGrasslandTileArr.size / (16 * 16))
    //      myBgTileMemInit.size / (16 * 16)
    //    )
    //  ) {
    //    rBgEntryPushValid := False
    //  }


    //  tempBgEntryPush.valid := rBgEntryPushValid
    //  //tempBgEntryPush.payload.bgEntry.tileMemIdx := (
    //  //  rBgEntryMemIdx.asUInt(
    //  //    cfg.bgEntryMemIdxWidth - 1 downto 0
    //  //  )
    //  //)
    //  tempBgEntryPush.payload.bgEntry := tempBgEntry
    //  tempBgEntryPush.payload.memIdx := (
    //    rBg0EntryCnt.asUInt(pop.bgEntryPushVec(0).payload.memIdx.bitsRange)
    //  )
    //}
    //else 
    if (idx == 0) {
      tempBgEntryPush.valid := (
        //True
        (rBg0EntryCnt + 1)
        < (cfg.bgSize2dInTiles.x * cfg.bgSize2dInTiles.y)
      )
      when (tempBgEntryPush.fire) {
        nextBg0EntryCnt := rBg0EntryCnt + 1
      }
      val myBg0MapArr = Gpu2dTest.mkBgEntryMapArr(
        cfg=cfg,
        someMapArr=Gpu2dTestGfx.sampleFgMapArr,
        someMapSize2d=Gpu2dTestGfx.sampleFgMapSize2d,
      )
        .setName("myBg0MapArr")
        .addAttribute("keep")
      val myPayload = (
        KeepAttribute(cloneOf(tempBgEntryPush.payload))
        .setName(f"myTempBgEntryPushPayload_$idx")
      )
      tempBgEntryPush.payload := /*RegNext*/(myPayload)
      myPayload.memIdx := RegNext(
        //rBg0EntryCnt.asUInt(pop.bgEntryPushVec(0).payload.memIdx.bitsRange)
        rBg0EntryCnt.asUInt(myPayload.memIdx.bitsRange)
      )
      myPayload.bgEntry := myPayload.bgEntry.getZero
      myPayload.bgEntry.allowOverride
      val myBg0MemAddr = KeepAttribute(
        rBg0EntryCnt.asUInt.resized
      )
        .setName("myBg0MemAddr")
      //val mySlice = (
      //  rBg0EntryCnt(1 downto 0)
      //)
      //when (
      //  rBg0EntryCnt(cfg.bgSize2dInTilesPow.x - 1)
      //) {
      //} otherwise {
      //  myPayload.bgEntry.tileIdx := (
      //  )
      //}
      //val myPosYBit = rBg0EntryCnt(cfg.bgSize2dInTilesPow.x)
      //myPayload.bgEntry.tileIdx := (
      //  //Cat(myPosYBit, rBg0EntryCnt(1 downto 0)).asUInt.resized
      //  Cat(myPosYBit, rBg0EntryCnt(2 downto 0)).asUInt.resized
      //)
      myPayload.bgEntry.tileIdx := (
        //(rBg0EntryCnt(3 downto 0) + 1).asUInt.resized
        (
          myBg0MapArr.readSync(
            //address=nextBg0EntryCnt.asUInt.resized//rBgEntryCnt.asUInt.resized
            address=(
              myBg0MemAddr
            ).resized,
            //enable=myReadSyncEnable,
          )
          + (
            Gpu2dTestGfx.sampleBgTileArr.size
            / (
              cfg.bgTileSize2d.x
              * cfg.bgTileSize2d.y
            )
          )
        ).resized
      )
    }
    else if (idx == 1) {
      //--------
      //tempBgEntryPush.valid := tempBgEntryPush.valid.getZero
      //tempBgEntryPush.payload := tempBgEntryPush.payload.getZero
      //val myMemAddrExtCalcPos = LcvVideoCalcPos(
      //  someSize2d=cfg.bgSize2dInTiles
      //)
      //  .addAttribute("keep")
      //  .setName("myBg1EntryMemAddrExtCalcPos")
      //myMemAddrExtCalcPos.io.en := (
      //  //True
      //  pop.bgEntryPushVec(0).fire
      //)
      //--------
      //val tempFbSize2d = ElabVec2[Int](
      //  x=min(
      //    Gpu2dTestGfx.sampleBgMapSize2d.x * 16,
      //    cfg.intnlFbSize2d.x,
      //  ),
      //  y=min(
      //    Gpu2dTestGfx.sampleBgMapSize2d.y * 16,
      //    cfg.intnlFbSize2d.y,
      //  ),
      //)
      //val tempFbSize2d = Gpu2dTest.mkBgEntryMapTempFbSize2d(
      //  cfg=cfg,
      //  someMapSize2d=Gpu2dTestGfx.sampleBgMapSize2d,
      //)
      //val myMemAddrCalcPos = LcvVideoCalcPos(
      //  someSize2d=ElabVec2[Int](
      //    x=cfg.intnlFbSize2d.x / cfg.bgTileSize2d.x,
      //    y=cfg.intnlFbSize2d.y / cfg.bgTileSize2d.y,
      //  )
      //)
      //  .addAttribute("keep")
      //  .setName("myBg1EntryMemAddrCalcPos")
      //val tempCalcPosEn = (
      //  /*RegNext*/(RegNext(
      //    (
      //      /*RegNext*/(RegNext(rBg1EntryExtCntV2d)).x //- 1
      //      < (
      //        cfg.intnlFbSize2d.x / cfg.bgTileSize2d.x
      //      )
      //    ) && (
      //      /*RegNext*/(RegNext(rBg1EntryExtCntV2d)).y 
      //      < (
      //        cfg.intnlFbSize2d.y / cfg.bgTileSize2d.y
      //      )
      //    ),
      //    init=False
      //  )) //init(False)
      //)
      //myMemAddrCalcPos.io.en := (
      //  //tempBgEntryPush.fire
      //  tempCalcPosEn
      //)
      //--------
      //nextBg1EntryCntV2d.x := (
      //  /*RegNext*/(RegNext(RegNext(myMemAddrCalcPos.io.info.pos))).x
      //  .resized
      //)
      //nextBg1EntryCntV2d.y := (
      //  /*RegNext*/(RegNext(RegNext(myMemAddrCalcPos.io.info.pos))).y
      //  .resized
      //)
      //nextBg1EntryExtCntV2d.x := (
      //  myMemAddrExtCalcPos.io.info.pos.x.resized
      //)
      //nextBg1EntryExtCntV2d.y := (
      //  myMemAddrExtCalcPos.io.info.pos.y.resized
      //)
      //--------
      tempBgEntryPush.valid := (
        (rBg1EntryCnt + 1)
        < (cfg.bgSize2dInTiles.x * cfg.bgSize2dInTiles.y)
        //(
        //  RegNextWhen(
        //    False, (
        //      myMemAddrExtCalcPos.io.info.posWillOverflow.x
        //      && myMemAddrExtCalcPos.io.info.posWillOverflow.y
        //    )
        //  ) init(True)
        //) && (
        //  RegNext(RegNext(RegNext(RegNext(
        //    RegNext(
        //      (RegNext(Cat(B"12'd0",rBg1EntryExtCntV2d.x).asUInt))
        //      //- 1
        //      < (
        //        //cfg.intnlFbSize2d.x / cfg.bgTileSize2d.x
        //        tempFbSize2d.x / cfg.bgTileSize2d.x
        //      )
        //    ) && RegNext(
        //      (RegNext(Cat(B"12'd0", rBg1EntryExtCntV2d.y).asUInt))
        //      < (
        //        //cfg.intnlFbSize2d.y / cfg.bgTileSize2d.y
        //        tempFbSize2d.y / cfg.bgTileSize2d.y
        //      )
        //    ))
        //  ))) //init(False)
        //)
      )
      when (tempBgEntryPush.fire) {
        nextBg1EntryCnt := rBg1EntryCnt + 1
      }
      //val nextBg1EntryCnt = cloneOf(nextBg0EntryCnt)
      //val rBg1EntryCnt = RegNext(nextBg1EntryCnt) init(0x0)
      //tempBgEntryPush.payload := (
      //  tempBgEntryPush.payload.getZero
      //)
      //val myBg1EntryCntMerged = (
      //  Cat(
      //    rBg1EntryCntV2d.y,
      //    rBg1EntryCntV2d.x,
      //  ).asUInt
      //)
      //when (
      //  //nextBg0EntryCnt === rBg0EntryCnt + 1
      //  tempBgEntryPush.fire
      //) {
      //  //nextBg1EntryCntV2d.assignFromBits(
      //  //  (rBg1EntryCntV2d.asBits.asUInt + 1).asBits
      //  //)
      //  //val tempCntV2d = (
      //  //  Cat(
      //  //    rBg1EntryCntV2d.y,
      //  //    rBg1EntryCntV2d.x,
      //  //  ).asUInt + 1
      //  //)
      //  //nextBg1EntryCntV2d.x := (
      //  //  tempCntV2d(nextBg1EntryCntV2d.x.bitsRange)
      //  //)
      //  //nextBg1EntryCntV2d.y := (
      //  //  tempCntV2d(
      //  //    tempCntV2d.high
      //  //    downto nextBg1EntryCntV2d.x.getWidth
      //  //  )
      //  //)
      //}
      //--------
      //--------
      //def myMapArr = Gpu2dTestGfx.sampleBgMapArr
      val myBg1MapArr = Gpu2dTest.mkBgEntryMapArr(
        cfg=cfg,
        someMapArr=Gpu2dTestGfx.sampleBgMapArr,
        someMapSize2d=Gpu2dTestGfx.sampleBgMapSize2d,
      )
        .setName("myBg1MapArr")
        .addAttribute("keep")
      //val myBg1MapArr = Mem(
      //  wordType=(
      //    UInt(16 bits)
      //    //UInt(8 bits)
      //  ),
      //  //wordCount=myMapArr.size,
      //  wordCount=(
      //    cfg.bgSize2dInTiles.y * cfg.bgSize2dInTiles.x
      //  ),
      //)
      //  .initBigInt({
      //    val tempArr = new ArrayBuffer[BigInt]()
      //    ////var jdx: Int = 0
      //    //for (jdx <- 0 until myMapArr.size) {
      //    //  //val tempY = 
      //    //  tempArr += BigInt(myMapArr(jdx))
      //    //}
      //    var z: Int = 0
      //    for (jdx <- 0 until cfg.bgSize2dInTiles.y) {
      //      for (kdx <- 0 until cfg.bgSize2dInTiles.x) {
      //        if (
      //          jdx
      //          < (
      //            //cfg.intnlFbSize2d.y / cfg.bgTileSize2d.y
      //            //tempFbSize2d.y / cfg.bgTileSize2d.y
      //            Gpu2dTestGfx.sampleBgMapSize2d.y
      //          )
      //          && kdx
      //          < (
      //            //cfg.intnlFbSize2d.x / cfg.bgTileSize2d.x
      //            //tempFbSize2d.x / cfg.bgTileSize2d.x
      //            Gpu2dTestGfx.sampleBgMapSize2d.x
      //          )
      //        ) {
      //          //println(s"inner: ($kdx, $jdx)")
      //          if (
      //            (
      //              jdx < tempFbSize2d.y / cfg.bgTileSize2d.y
      //            ) && (
      //              jdx < tempFbSize2d.x / cfg.bgTileSize2d.x
      //            )
      //          ) {
      //            tempArr += BigInt(myMapArr(z))
      //          } else {
      //            tempArr += BigInt(0)
      //          }
      //          z += 1
      //        } else {
      //          //println(s"outer: ($kdx, $jdx)")
      //          //tempArr += BigInt(64)
      //          tempArr += BigInt(0)
      //        }
      //      }
      //    }
      //    tempArr.toSeq
      //  })
      //  .setName("myBg1MapArr")
      //  .addAttribute("keep")
      val myPayload = (
        KeepAttribute(cloneOf(tempBgEntryPush.payload))
        .setName(f"myTempBgEntryPushPayload_$idx")
      )
      tempBgEntryPush.payload := /*RegNext*/(myPayload)
      //val myBg1MemIdx = (
      //  KeepAttribute(RegNext(rBg1EntryCntV2d).asBits.asUInt)
      //  //rBg0EntryCnt
      //  .setName("myBg1MemIdx")
      //)

      myPayload.memIdx := RegNext(
        //rBg0EntryCnt.asUInt(pop.bgEntryPushVec(0).payload.memIdx.bitsRange)
        rBg1EntryCnt.asUInt(myPayload.memIdx.bitsRange)
        //rBg1EntryCntMerged
        ///*RegNext*/(myBg1MemIdx(myPayload.memIdx.bitsRange))//.asUInt
        //(RegNext(rBg0EntryCnt).asUInt(myPayload.memIdx.bitsRange))//.asUInt
        //(
        //  RegNext(
        //    //rBg1EntryCntV2d
        //    rBg1EntryExtCntV2d
        //  ).y
        //  * (
        //    cfg.bgSize2dInTiles.x
        //    /// cfg.bgTileSize2d.x
        //  )
        //  + RegNext(
        //    //rBg1EntryCntV2d
        //    rBg1EntryExtCntV2d
        //  ).x
        //)/*.asUInt*/(myPayload.memIdx.bitsRange)
      )
      myPayload.bgEntry := myPayload.bgEntry.getZero
      myPayload.bgEntry.allowOverride
      //myPayload.bgEntry.tileIdx := rBgEntryCnt.asUInt.resized
      //val myReadSyncEnable = KeepAttribute(
      //  {
      //    val tempX = Cat(B"16'd0", /*RegNext*/(rBg1EntryCntV2d.x)).asUInt
      //    (
      //      tempX
      //      <= (cfg.intnlFbSize2d.x / cfg.bgTileSize2d.x)
      //    )
      //  } && {
      //    val tempY = Cat(B"16'd0", /*RegNext*/(rBg1EntryCntV2d).y).asUInt
      //    (
      //      //(
      //      //  //tempY >= myBg1EntryCntYInit
      //      //  True
      //      //) && (
      //        tempY
      //        <= (
      //          (cfg.intnlFbSize2d.y / cfg.bgTileSize2d.y)
      //          //+ myBg1EntryCntYInit - 1
      //        )
      //      //)
      //    )
      //  }
      //)
      //  .setName("myBg1ReadSyncEnable")
      val myBg1MemAddr = KeepAttribute(
        //RegNext(/*RegNext*/(
        //  (
        //    //Cat(
        //      //B"16'd0",
        //      /*RegNext*/(/*RegNext*/(rBg1EntryCntV2d)).y //- myBg1EntryCntYInit
        //    //).asUInt.resized
        //    * (
        //      //cfg.bgSize2dInTiles.x
        //      //cfg.intnlFbSize2d.x / cfg.bgTileSize2d.x
        //      Gpu2dTestGfx.sampleBgMapSize2d.x /// cfg.bgTileSize2d.x
        //    )
        //  )
        //  + /*RegNext*/(/*RegNext*/(rBg1EntryCntV2d)).x.resized
        //  //rBg0EntryCnt.asUInt.resized
        //))
        /*RegNext*/(
          rBg1EntryCnt.asUInt.resized
        )
      )
        .setName("myBg1MemAddr")
      //myPayload.bgEntry.tileIdx := (
      //  RegNext(myPayload.bgEntry.tileIdx)
      //  init(myPayload.bgEntry.tileIdx.getZero)
      //)
      //when (myReadSyncEnable) {
        myPayload.bgEntry.tileIdx := (
          //0x0
          //3
          //4
          //if (cfg.bgTileSize2d.x == 8) (
          //  3 * 4
          //) else (
          //  3
          //)
          //--------
          myBg1MapArr.readSync(
            //address=nextBg1EntryCnt.asUInt.resized//rBgEntryCnt.asUInt.resized
            address=(
              myBg1MemAddr
            ).resized,
            //enable=myReadSyncEnable,
          ).resized
          //--------
        )
      //}
      //myPayload.bgEntry.dispFlip.x := True
      //myPayload.bgEntry.dispFlip.y := True
      //myPayload.bgEntry.dispFlip.x := rBgEntryCnt(0)
      //myPayload.bgEntry.dispFlip.y := rBgEntryCnt(1)
    } 
    else {
      tempBgEntryPush.valid := True
      tempBgEntryPush.payload := tempBgEntryPush.payload.getZero
      //tempBgEntryPush.payload.bgEntry := tempBgEntry.getZero
      //tempBgEntryPush.bgEntry := 
      //tempBgEntryPush.payload.memIdx := 0x0
    }
  }
  //--------
  //def bgPalCntWidth = cfg.numColsInBgPalPow + 1
  //val rBgPalCnt = Reg(UInt(bgPalCntWidth bits)) init(0x0)
  //val rBgPalEntry = Reg(Gpu2dPalEntry(cfg=cfg))
  //rBgPalEntry.init(rBgPalEntry.getZero)
  //val rBgPalEntryPushValid = Reg(Bool()) init(True)

  //pop.bgPalEntryPush.valid := True
  pop.bgPalEntryPush.valid := (
    //rBgPalEntryPushValid
    False
  )
  pop.bgPalEntryPush.payload := pop.bgPalEntryPush.payload.getZero
  //pop.bgPalEntryPush.payload.bgPalEntry := rBgPalEntry
  ////pop.bgPalEntryPush.payload.memIdx := 1
  //pop.bgPalEntryPush.payload.memIdx := rBgPalCnt.resized

  ////otherwise {
  ////}
  //val rBgPalMemAddr = (
  //  Reg(UInt(cfg.numColsInBgPalPow bits)) init(0x0)
  //  addAttribute("keep")
  //)
  //palPush(
  //  numColsInPal=cfg.numColsInBgPal,
  //  rPalCnt=rBgPalCnt,
  //  rPalEntry=rBgPalEntry,
  //  rPalEntryPushValid=rBgPalEntryPushValid,
  //  palPushFire=pop.bgPalEntryPush.fire,
  //  //rPalMemAddr=Some(rBgPalMemAddr),
  //  somePalMem=bgPalMem,
  //)
  //--------
  val tempObjTileSlice = Gpu2dTileSlice(
    cfg=cfg,
    isObj=true,
    isAffine=false,
  )

  def extraObjTileCntWidth = (
    if (dbgPipeMemRmw) {
      //2
      0
      //2
      //1
    } else {
      0
    }
  )
  val nextObjTileCnt = SInt(
    //cfg.numObjTilesPow + 2 bits
    //cfg.objTileSliceMemIdxWidth + 5 bits
    cfg.objTileSliceMemIdxWidth + 5
    + extraObjTileCntWidth
    bits
  )
  val rObjTileCnt = (
    //RegNext(nextObjTileCnt) init(0x0)
    RegNextWhen(nextObjTileCnt, pop.objTilePush.fire) init(0)
  )
  nextObjTileCnt := rObjTileCnt
  val nextObjTilePushValid = Bool()
  val rObjTilePushValid = RegNext(nextObjTilePushValid) init(False)
  nextObjTilePushValid := rObjTilePushValid
  when (
    pop.objTilePush.ready
  ) {
    nextObjTilePushValid := True
  }
  val tempObjTileCntRshiftByExtra = (
    rObjTileCnt >> extraObjTileCntWidth
  )

  //def mkObjTile(
  //  colIdx0: Int,
  //  colIdx1: Int,
  //  colIdx2: Option[Int]=None,
  //  colIdx3: Option[Int]=None,
  //): Unit = {
  //  //mkTile(
  //  //  tempTile=tempObjTile,
  //  //  colIdx0=colIdx0,
  //  //  colIdx1=colIdx1,
  //  //  colIdx2=colIdx2,
  //  //  colIdx3=colIdx3,
  //  //)
  //  mkTile(
  //    //tempTile=tempColorMathTile,
  //    tempTileSlice=tempObjTileSlice,
  //    pxCoordXStart={
  //      if (cfg.objTileWidthRshift == 0) {
  //        def tempWidthPow = cfg.objTileSize2dPow.x
  //        U(f"$tempWidthPow'd0")
  //      } else {
  //        //rObjTileCnt(
  //        //  //cfg.objTileSize2dPow.x - 1
  //        //  ////downto 0
  //        //  ////downto cfg.objSliceTileWidthPow
  //        //  //downto cfg.objTileWidthRshift
  //        //  //cfg.objTileWidthRshift - 1 downto 0
  //        //  cfg.objSliceTileWidthPow - 1 downto 0
  //        //).asUInt
  //        //Cat(
  //        //  rObjTileCnt(
  //        //    cfg.objTileWidthRshift - 1 downto 0
  //        //  ),
  //        //  B(
  //        //    cfg.objSliceTileWidthPow bits,
  //        //    default -> False,
  //        //  )
  //        //).asUInt
  //        (
  //          //rObjTileCnt
  //          //>> extraObjTileCntWidth
  //          tempObjTileCntRshiftByExtra
  //        )(
  //          //cfg.objTileWidthRshift - 1 downto 0
  //          cfg.objTileSize2dPow.x - 1
  //          downto cfg.objTileWidthRshift
  //        ).asUInt
  //      }
  //    },
  //    pxCoordY=(
  //      (
  //        //rObjTileCnt
  //        //>> extraObjTileCntWidth
  //        tempObjTileCntRshiftByExtra
  //      )
  //      (
  //        (
  //          cfg.objTileSize2dPow.y
  //          //+ cfg.objSliceTileWidthPow
  //          + cfg.objTileWidthRshift
  //          - 1
  //        )
  //        downto (
  //          //cfg.objTileSize2dPow.x
  //          //cfg.objSliceTileWidthPow
  //          cfg.objTileWidthRshift
  //        )
  //      ).asUInt
  //    ),
  //    palEntryMemIdxWidth=cfg.objPalEntryMemIdxWidth,
  //    colIdx0=colIdx0,
  //    colIdx1=colIdx1,
  //    colIdx2=colIdx2,
  //    colIdx3=colIdx3,
  //  )
  //}
  val tempObjTileCnt = (
    (
      //rObjTileCnt
      //>> extraObjTileCntWidth
      tempObjTileCntRshiftByExtra
    )
    (
      (
        //rObjTileCnt.high - extraObjTileCntWidth
        tempObjTileCntRshiftByExtra.high
      ) downto (
        cfg.objTileSize2dPow.y
        //+ cfg.objSliceTileWidthPow
        + cfg.objTileWidthRshift
        ////+ extraObjTileCntWidth
        //+ extraObjTileCntWidth
      )
    )
    //>> (
    //  cfg.objTileSize2dPow.y
    //  + cfg.objSliceTileWidthPow
    //  //+ cfg.objTileWidthRshift
    //)
  )
  //val tempObjTileCntSliced
  //tempObjTileSlice := tempObjTileSlice.getZero
  //tempObjTileSlice.allowOverride

  //nextObjTileCnt := rObjTileCnt
  //when (
  //  tempObjTileCnt + 1
  //  //< (cfg.numObjTiles << extraObjTileCntWidth)
  //  < cfg.numObjTiles
  //) {
  //--------
  //  when (pop.objTilePush.fire) {
  //    //--------
  //    // BEGIN: old, geometrical shapes graphics
  //    switch (tempObjTileCnt) {
  //      for (idx <- 0 until 32) {
  //        is (idx) {
  //          if (idx == 1) {
  //            mkObjTile(idx - 1, idx - 1 + 1)
  //          } else {
  //            mkObjTile(idx, idx + 1)
  //          }
  //        }
  //      }
  //      default {
  //        tempObjTileSlice := tempObjTileSlice.getZero
  //      }
  //    }
  //    //when (tempObjTileCnt === 0) {
  //    //  //mkObjTile(0, 1)
  //    //  mkObjTile(0, 0)
  //    //  //tempObjTileSlice := tempObjTileSlice.getZero
  //    //} elsewhen (tempObjTileCnt === 1) {
  //    //  //tempObjTile := tempObjTile.getZero
  //    //  //mkObjTile(1, 2, Some(3), Some(4))
  //    //  mkObjTile(1, 1)
  //    //  //mkObjTile(3, 3)
  //    //  //mkObjTile(2, 3)
  //    //} elsewhen (tempObjTileCnt === 2) {
  //    //  //mkObjTile(2, 3)
  //    //  //mkObjTile(3, 4)
  //    //  mkObjTile(2, 3, Some(4), Some(5))
  //    //  //mkObjTile(2, 2)
  //    //} elsewhen (tempObjTileCnt === 3) {
  //    //  //mkObjTile(3, 4)
  //    //  mkObjTile(6, 7, Some(6), Some(7))
  //    //  //mkObjTile(0, 1)
  //    //} elsewhen (tempObjTileCnt === 4) {
  //    //  //mkObjTile(4, 5)
  //    //  //mkObjTile(4, 4)
  //    //  //mkObjTile(1, 2, Some(3), Some(4))
  //    //  mkObjTile(1, 2)
  //    //} otherwise {
  //    //  tempObjTileSlice := tempObjTileSlice.getZero
  //    //  //when (tempObjTileCnt >= cfg.numObjTiles) {
  //    //  //  rObjTilePushValid := False
  //    //  //}
  //    //}
  //    //when (
  //    //  //rObjTileCnt < objTileMem.wordCount
  //    //  //if (
  //    //  //  (1 << tempObjTileCnt.getWidth) < objTileMem.wordCount
  //    //  //) (
  //    //  //  True
  //    //  //) else (
  //    //    Cat(
  //    //      U(s"${log2Up(objTileMem.wordCount)}'d0"),
  //    //      tempObjTileCnt 
  //    //    ).asUInt < objTileMem.wordCount
  //    //  //)
  //    //) {
  //    //  tempObjTileSlice := objTileMem.readAsync(
  //    //    //address=rObjTileCnt.asUInt.resized
  //    //    address=tempObjTileCntRshiftByExtra.asUInt.resized
  //    //  )
  //    //} otherwise {
  //    //  tempObjTileSlice := tempObjTileSlice.getZero
  //    //}
  //    //when (
  //    //  //pop.objTilePush.fire
  //    //  ////pop.objTilePush.valid
  //    //  True
  //    //) {
  //      nextObjTileCnt := rObjTileCnt + 1
  //    //}
  //  } //otherwise {
  //  //  ////tempObjTileSlice := tempObjTileSlice.getZero
  //  //  //nextObjTileCnt := rObjTileCnt
  //  //}
  //--------
  //} otherwise {
  //  ////tempObjTileSlice := tempObjTileSlice.getZero
  //  //nextObjTileCnt := rObjTileCnt
  //}
  //val tempCondExtra = (
  //  //(rObjTileCnt + 1)
  //  //>> extraObjTileCntWidth
  //  tempObjTileCntRshiftByExtra + 1
  //)
  //  .addAttribute("keep")
  //val tempObjTileCond = (
  //  (
  //    tempCondExtra
  //  )
  //  (
  //    (
  //      //rObjTileCnt.high - extraObjTileCntWidth
  //      tempCondExtra.high
  //    ) downto (
  //      cfg.objTileSize2dPow.y
  //      //+ cfg.objSliceTileWidthPow
  //      + cfg.objTileWidthRshift
  //      ////+ extraObjTileCntWidth
  //      //+ extraObjTileCntWidth
  //    )
  //  ) >= cfg.numObjTiles
  //)
  //  .addAttribute("keep")

  //when (
  //  tempObjTileCond
  //  //tempObjTileCnt //+ 1
  //  //>= (
  //  //  cfg.numObjTiles - 1
  //  //  //cfg.numObjTiles
  //  //  //<< extraObjTileCntWidth
  //  //)
  //  //tempObjTileCnt + 1 === cfg.numObjTiles
  //  //(rObjTileCnt + 1)
  //  //>= (1 << cfg.objTileSliceMemIdxWidth)
  //) {
  //  nextObjTileCnt := 0x0
  //  //rObjTilePushValid := False
  //}
  //val rTempForceWrVec = Vec.fill(cfg.numObjTiles)(
  //  Reg(Bool()) init(True)
  //)
  //pop.objTilePush.forceWr := False

  //if (dbgPipeMemRmw) {
  //  pop.objTilePush.forceWr := False
  //  when (
  //    rObjTileCnt(
  //      extraObjTileCntWidth - 1
  //      downto 0
  //    ) === 0x0
  //  ) {
  //    def temp: Bool = (
  //      rTempForceWrVec(tempObjTileCnt.asUInt(
  //        cfg.numObjTilesPow - 1 downto 0
  //      ))
  //    )
  //    when (!temp) {
  //      pop.objTilePush.forceWr := True
  //      temp := True
  //    } otherwise {
  //      pop.objTilePush.forceWr := False
  //    }
  //  }
  //}
  pop.objTilePush.valid := (
    //nextObjTilePushValid
    //rObjTilePushValid
    False
  )
  //pop.objTilePush.payload := pop.objTilePush.payload.getZero
  pop.objTilePush.payload.tileSlice := (
    //tempObjTileSlice
    tempObjTileSlice.getZero
  )
  pop.objTilePush.payload.memIdx := (
    //rObjTileCnt.asUInt(pop.objTilePush.payload.memIdx.bitsRange)
    tempObjTileCntRshiftByExtra
      .asUInt(pop.objTilePush.payload.memIdx.bitsRange)
    //rObjTileCnt.asUInt(
    //  rObjTileCnt.high downto extraObjTileCntWidth
    //).resized
    //--------
    //(rObjTileCnt >> extraObjTileCntWidth).asUInt(
    //  pop.objTilePush.payload.memIdx.bitsRange
    //)
    //rObjTileCnt.asUInt
    //--------
    //tempObjTileCnt.asUInt(
    //  tempObjTileCnt.getWidth - 1
    //  .min(pop.objTilePush.payload.memIdx.getWidth)
    //  downto 0
    //)
    //(
    //  pop.objTilePush.payload.memIdx.bitsRange
    //)
      //.resized
  )
  //--------
  //def objPalCntWidth = cfg.numColsInObjPalPow + 1
  //val rObjPalCnt = Reg(UInt(objPalCntWidth bits)) init(0x0)
  //val rObjPalEntry = Reg(Gpu2dPalEntry(cfg=cfg))
  //rObjPalEntry.init(rObjPalEntry.getZero)
  //val rObjPalEntryPushValid = Reg(Bool()) init(True)

  ////pop.objPalEntryPush.valid := True
  //pop.objPalEntryPush.valid := rObjPalEntryPushValid
  //pop.objPalEntryPush.payload.objPalEntry := rObjPalEntry
  ////pop.objPalEntryPush.payload.memIdx := 1
  //pop.objPalEntryPush.payload.memIdx := rObjPalCnt.resized
  pop.objPalEntryPush.valid := False
  pop.objPalEntryPush.payload := pop.objPalEntryPush.payload.getZero

  //otherwise {
  //}
  //palPush(
  //  numColsInPal=cfg.numColsInObjPal,
  //  rPalCnt=rObjPalCnt,
  //  rPalEntry=rObjPalEntry,
  //  rPalEntryPushValid=rObjPalEntryPushValid,
  //  palPushFire=pop.objPalEntryPush.fire,
  //  somePalMem=objPalMem,
  //)


  //pop.objTilePush.valid := True
  ////pop.objTilePush.payload := pop.objTilePush.payload.getZero

  ////pop.objAttrsPush.valid := True
  ////pop.objAttrsPush.payload := pop.objAttrsPush.payload.getZero
  //pop.objPalEntryPush.valid := True
  //pop.objPalEntryPush.payload := pop.objPalEntryPush.payload.getZero


  val tempObjAttrs = Gpu2dObjAttrs(
    cfg=cfg,
    isAffine=false,
  )
  //tempBgAttrs.scroll := (
  //  RegNext(tempBgAttrs.scroll) init(tempBgAttrs.scroll.getZero)
  //)
  tempObjAttrs := RegNext(tempObjAttrs) init(tempObjAttrs.getZero)
  pop.objAttrsPush.valid := (
    RegNext(pop.objAttrsPush.valid)
    init(pop.objAttrsPush.valid.getZero)
  )
  pop.objAttrsPush.payload := (
    RegNext(pop.objAttrsPush.payload)
    init(pop.objAttrsPush.payload.getZero)
  )

  val objAttrsCntWidth = cfg.numObjsPow + 2
  //val rObjAttrsCnt = Reg(UInt(objAttrsCntWidth bits)) init(0x0)
  //val nextObjAttrsCnt = UInt(objAttrsCntWidth bits)
  //val rObjAttrsCnt = RegNext(nextObjAttrsCnt) init(0x0)
  val nextObjAttrsCnt = SInt(objAttrsCntWidth bits)
  val rObjAttrsCnt = RegNext(nextObjAttrsCnt) init(-1)
  nextObjAttrsCnt := rObjAttrsCnt
  //val rObjAttrs = Reg(Gpu2dObjAttrs(cfg=cfg))
  //rObjAttrs.init(rObjAttrs.getZero)
  val rObjAttrsEntryPushValid = Reg(Bool()) init(True)
  if (!cfg.noColorMath) {
    tempObjAttrs.colorMathInfo := tempObjAttrs.colorMathInfo.getZero
    tempObjAttrs.colorMathInfo.allowOverride
  }

  //val snesCtrlReader = SnesCtrlReader(
  //  clkRate=clkRate,
  //)
  //io.snesCtrl <> snesCtrlReader.io.snesCtrl
  val snesHelper = (!optRawSnesButtons) generate SnesCtrlReaderHelper(
    clkRate=clkRate
  )
  //snesHelper.io.pop.ready := True
  //snesHelper.io.pop.ready.setAsReg()
  val rSnesPopReady = Reg(Bool()) init(False)
  if (!optRawSnesButtons) {
    io.snesCtrl <> snesHelper.io.snesCtrl
    snesHelper.io.pop.ready := rSnesPopReady
  } else { // if (optRawSnesButtons)
    io.rawSnesButtons.ready := rSnesPopReady
  }
  val rHoldCnt = Reg(ClkCnt(
    clkRate=clkRate,
    //time=0.5 sec,
    //--------
    //time=
    //--------
    time=(
      if (!optRawSnesButtons) (
        //--------
        // BEGIN: actual SNES controller `time`
        0.01 sec
        // END: actual SNES controller `time`
        //--------
      ) else ( // if (optRawSnesButtons)
        // BEGIN: debug
        //1.0 us
        //10.0 us
        50.0 us
        // END: debug
        //--------
      )
    ),
    //--------
    //--------
    //time=0.0001 sec
    //time=10.00 ns,
    //time=50.00 ns,
    //time=1 us,
    //time=10 us,
  ))
  rHoldCnt.init(rHoldCnt.getZero)
  rHoldCnt.incr()

  def myBgScrollFracWidth = (
    0
    //1
    //2
    //16
    //log2Up(gpu2dParams.intnlFbSize2d.y)
    //8
  )
  def myObjPosFracWidth = (
    0
    //1
    //4
    //8
    //16
    //8
  )
  def myTileFracWidth = (
    //4
    1
  )
  val rBgScroll = (
    Reg(DualTypeNumVec2[UInt, UInt](
      dataTypeX=UInt(
        (tempBgAttrs.scroll.x.getWidth + myBgScrollFracWidth) bits
        //tempBgAttrs.scroll.x.getWidth bits
      ),
      dataTypeY=UInt(
        (tempBgAttrs.scroll.y.getWidth + myBgScrollFracWidth) bits
        //tempBgAttrs.scroll.y.getWidth bits
      ),
    ))
    .setName("rBgScroll")
  )
  rBgScroll.x.init(
    myDefaultBgScroll.x << myBgScrollFracWidth
  )
  rBgScroll.y.init(
    myDefaultBgScroll.y << myBgScrollFracWidth
  )
  //val rHoldCnt = Reg(
  //  UInt((log2Up(clkRate.toTime.toBigDecimal.toInt) + 1) bits)
  //) init(0x0)
  //val rPos = Reg(cloneOf(tempObjAttrs.pos))
  val rPlayerPos = Reg(DualTypeNumVec2[SInt, SInt](
    dataTypeX=SInt(
      (tempObjAttrs.pos.x.getWidth + myObjPosFracWidth) bits
    ),
    dataTypeY=SInt(
      (tempObjAttrs.pos.y.getWidth + myObjPosFracWidth) bits
    ),
    //dataTypeX=SInt((tempObjAttrs.pos.x.getWidth) bits),
    //dataTypeY=SInt((tempObjAttrs.pos.y.getWidth) bits),
  ))
  //rPos.init(rPos.getZero)
  rPlayerPos.x.init(
    //0x2
    //0x1
    //0x4
    //0x4 << myFracWidth
    //0x0 << myObjPosFracWidth
    5 << myObjPosFracWidth
    //18 << myObjPosFracWidth
    //(cfg.objTileSize2d.x * 2) << myObjPosFracWidth
  )
  rPlayerPos.y.init(
    0x0 << myObjPosFracWidth
    //(cfg.objTileSize2d.y * 2) << myObjPosFracWidth
  )
  val rPlayerTileIdx = (
    Reg(UInt((tempObjAttrs.tileIdx.getWidth + myTileFracWidth) bits))
    init(
      //4 << myTileFracWidth
      5 << myTileFracWidth
      //1 << myTileFracWidth
    )
  )
  //--------
  def playerObjAttrsIdx = 1
  val rPlayerObjAttrsPrio = (
    Reg(UInt(
      (cfg.numBgsPow + 2) bits
    )) init(0x0)
  )
  def doObjAttrsInit(): Unit = {
    //rSnesPopReady := True
    when (
      rObjAttrsCnt === playerObjAttrsIdx
    ) {
      //tempObjAttrs := tempObjAttrs.getZero
      //tempObjAttrs.allowOverride
      tempObjAttrs.tileIdx := 2
      tempObjAttrs.pos.x := (
        0
        //8
      )
      tempObjAttrs.pos.y := (
        //0
        8
      )
      tempObjAttrs.prio := (
        //1
        //1
        rPlayerObjAttrsPrio(
          rPlayerObjAttrsPrio.high
          downto rPlayerObjAttrsPrio.getWidth - cfg.numBgsPow
        )
      )
      tempObjAttrs.size2d.x := (
        cfg.objTileSize2d.x
      )
      tempObjAttrs.size2d.y := (
        cfg.objTileSize2d.y
      )
      tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      when (pop.objAttrsPush.fire) {
        nextObjAttrsCnt := rObjAttrsCnt + 1
      } otherwise {
        nextObjAttrsCnt := rObjAttrsCnt
      }
    } elsewhen (
      rObjAttrsCnt === 2
    ) {
      tempObjAttrs.tileIdx := 2
      tempObjAttrs.pos.x := (
        cfg.objTileSize2d.x * 1
        //8
      )
      tempObjAttrs.pos.y := (
        cfg.objTileSize2d.y * 1
        //0
      )
      tempObjAttrs.prio := (
        0
        //1
      )
      tempObjAttrs.size2d.x := (
        cfg.objTileSize2d.x
      )
      tempObjAttrs.size2d.y := (
        cfg.objTileSize2d.y
      )
      tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      when (pop.objAttrsPush.fire) {
        nextObjAttrsCnt := rObjAttrsCnt + 1
      } otherwise {
        nextObjAttrsCnt := rObjAttrsCnt
      }
    } elsewhen (
      //rObjAttrsCnt > 0
      //&& 
      rObjAttrsCnt < cfg.numObjs
    ) {
      //when (
      //  rObjAttrsCnt
      //  ===
      //  //1 //0
      //  0
      //) {
      //  tempObjAttrs.tileIdx := 1
      //  //tempObjAttrs.tileMemIdx := 2
      //  //tempObjAttrs.pos.x := 16
      //  //tempObjAttrs.pos.x := -1
      //  //tempObjAttrs.pos.x := 0
      //  //tempObjAttrs.pos.x := 1
      //  //tempObjAttrs.pos.x := 6
      //  //tempObjAttrs.pos.x := 7
      //  tempObjAttrs.pos.x := (
      //    //cfg.intnlFbSize2d.x - cfg.objTileSize2d.x //- 1
      //    //cfg.intnlFbSize2d.x - cfg.objTileSize2d.x - 5
      //    //cfg.intnlFbSize2d.x >> 1
      //    //0x3e
      //    //3
      //    //2
      //    //1
      //    //0
      //    7
      //    //8
      //  )
      //  //tempObjAttrs.pos.x := -1
      //  //tempObjAttrs.pos.x := 8
      //  //tempObjAttrs.pos.x := 3
      //  tempObjAttrs.pos.y := 8
      //  //tempObjAttrs.pos.y := 0
      //  //tempObjAttrs.prio := 0
      //  tempObjAttrs.prio := (
      //    1
      //    //0
      //  )
      //  tempObjAttrs.size2d.x := cfg.objTileSize2d.x
      //  tempObjAttrs.size2d.y := cfg.objTileSize2d.y
      //  //tempObjAttrs.size2d.y := cfg.objTileSize2d.y - 1
      //  tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //  //tempObjAttrs.affine := tempObjAttrs.affine.getZero
      //  //tempObjAttrs.affine.doIt := True
      //  //tempObjAttrs.affine.mat(0)(0) := (
      //  //  //1 << (tempObjAttrs.affine.fracWidth - 1)
      //  //  //2 << tempObjAttrs.affine.fracWidth
      //  //  (1 << Gpu2dAffine.fracWidth)
      //  //  //| (1 << (Gpu2dAffine.fracWidth - 1))
      //  //  | (1 << (Gpu2dAffine.fracWidth - 2))
      //  //)
      //  //tempObjAttrs.affine.mat(0)(1) := 0
      //  //tempObjAttrs.affine.mat(1)(0) := 0
      //  //tempObjAttrs.affine.mat(1)(1) := (
      //  //  //1 << (tempObjAttrs.affine.fracWidth - 1)
      //  //  //2 << tempObjAttrs.affine.fracWidth
      //  //  (1 << Gpu2dAffine.fracWidth)
      //  //  //| (1 << (Gpu2dAffine.fracWidth - 1))
      //  //  | (1 << (Gpu2dAffine.fracWidth - 2))
      //  //)
      //  //tempObjAttrs := tempObjAttrs.getZero
      //} elsewhen (
      //  rObjAttrsCnt
      //  ===
      //  1
      //  //2
      //) {
      //  //tempObjAttrs.tileMemIdx := 1
      //  tempObjAttrs.tileIdx := (
      //    //2
      //    3
      //  )
      //  //tempObjAttrs.tileMemIdx := 0
      //  //tempObjAttrs.pos.x := 1
      //  //tempObjAttrs.pos.x := 16
      //  //tempObjAttrs.pos.x := 2
      //  //tempObjAttrs.pos.x := 16
      //  tempObjAttrs.pos.x := (
      //    1
      //    //2
      //  )
      //  //tempObjAttrs.pos.x := 9
      //  //tempObjAttrs.pos.x := 9
      //  //tempObjAttrs.pos.y := -1
      //  //tempObjAttrs.pos.y := 8
      //  tempObjAttrs.pos.y := 9
      //  tempObjAttrs.prio := (
      //    //0
      //    1
      //  )
      //  tempObjAttrs.size2d.x := cfg.objTileSize2d.x
      //  tempObjAttrs.size2d.y := cfg.objTileSize2d.y
      //  tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //  //tempObjAttrs.affine := tempObjAttrs.affine.getZero
      //  //tempObjAttrs.affine.doIt := True
      //  //tempObjAttrs.affine.mat(0)(0) := (
      //  //  //1 << (tempObjAttrs.affine.fracWidth - 1)
      //  //  //2 << tempObjAttrs.affine.fracWidth
      //  //  //(1 << Gpu2dAffine.fracWidth)
      //  //  //| 
      //  //  (1 << (Gpu2dAffine.fracWidth - 1))
      //  //  //| (1 << (Gpu2dAffine.fracWidth - 2))
      //  //)
      //  //tempObjAttrs.affine.mat(0)(1) := 0
      //  //tempObjAttrs.affine.mat(1)(0) := 0
      //  //tempObjAttrs.affine.mat(1)(1) := (
      //  //  //1 << (tempObjAttrs.affine.fracWidth - 1)
      //  //  //2 << tempObjAttrs.affine.fracWidth
      //  //  //(1 << Gpu2dAffine.fracWidth)
      //  //  //| 
      //  //  (1 << (Gpu2dAffine.fracWidth - 1))
      //  //  //| (1 << (Gpu2dAffine.fracWidth - 2))
      //  //)
      //  //tempObjAttrs := tempObjAttrs.getZero
      //} elsewhen (rObjAttrsCnt === 2) {
      //  tempObjAttrs.tileIdx := (
      //    //3
      //    2
      //  )
      //  tempObjAttrs.pos.x := 2
      //  //tempObjAttrs.pos.x := 7
      //  tempObjAttrs.pos.y := 8
      //  tempObjAttrs.prio := (
      //    //0
      //    1
      //  )
      //  tempObjAttrs.size2d.x := cfg.objTileSize2d.x
      //  tempObjAttrs.size2d.y := cfg.objTileSize2d.y
      //  tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //  //tempObjAttrs.affine := tempObjAttrs.affine.getZero
      //  //tempObjAttrs := tempObjAttrs.getZero
      //////} elsewhen (rObjAttrsCnt === 3) {
      //////  //tempObjAttrs.tileMemIdx := 0
      //////  //tempObjAttrs.pos.x := 8
      //////  //tempObjAttrs.pos.y := 0 //+ cfg.objTileSize2d.y - 1
      //////  //tempObjAttrs.prio := 0
      //////  //tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
      //////  tempObjAttrs := tempObjAttrs.getZero
      //} otherwise 
      {
        //tempObjAttrs := tempObjAttrs.getZero
        tempObjAttrs.tileIdx := 0
        tempObjAttrs.pos.x := (
          -cfg.objTileSize2d.x
          //0
          //1
        )
        //tempObjAttrs.pos.x := 16
        tempObjAttrs.pos.y := 0
        tempObjAttrs.prio := (
          0
          //1
        )
        tempObjAttrs.size2d.x := cfg.objTileSize2d.x
        tempObjAttrs.size2d.y := cfg.objTileSize2d.y
        tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
        //tempObjAttrs.affine := tempObjAttrs.affine.getZero
        when (nextObjAttrsCnt >= cfg.numObjs) {
          rObjAttrsEntryPushValid := False
        }
        //rObjAttrsEntryPushValid := False
      }
      when (pop.objAttrsPush.fire) {
        nextObjAttrsCnt := rObjAttrsCnt + 1
      } otherwise {
        nextObjAttrsCnt := rObjAttrsCnt
      }
      //} otherwise {
      //  tempObjAttrs := tempObjAttrs.getZero
      //  nextObjAttrsCnt := rObjAttrsCnt
      //}
    } otherwise {
      tempObjAttrs := tempObjAttrs.getZero
      nextObjAttrsCnt := rObjAttrsCnt
    }
    pop.objAttrsPush.valid := rObjAttrsEntryPushValid
    //pop.objAttrsPush.payload.objAttrs := (
    //  Gpu2dObjAttrs(cfg=cfg).getZero
    //)
    pop.objAttrsPush.payload.objAttrs := tempObjAttrs
    pop.objAttrsPush.payload.memIdx := (
      rObjAttrsCnt.asUInt(cfg.objAttrsMemIdxWidth - 1 downto 0)
    )
  }

  def doSnesFire(): Unit = {
    nextObjAttrsCnt := RegNext(nextObjAttrsCnt) init(0x0)
    //--------
    //rSnesPopReady := False
    //--------
    //val rTileIdx = Reg(cloneOf(tempObjAttrs.tileIdx)) init(0x1)
    //tempObjAttrs.tileIdx := 1

    //tempObjAttrs.tileMemIdx := 2
    //tempObjAttrs.pos.x := 16
    //tempObjAttrs.pos.x := -1
    //tempObjAttrs.pos.x := 0
    //tempObjAttrs.pos.x := 1
    //tempObjAttrs.pos.x := 7
    //tempObjAttrs.pos.x := 7
    def buttons = (
      if (!optRawSnesButtons) {
        snesHelper.io.pop.payload
      } else { // if (optRawSnesButtons)
        io.rawSnesButtons.payload
      }
    );
    //--------
    //when (
    //  //rHoldCnt(rHoldCnt.high - 1 downto 0) === 0x0
    //  rHoldCnt.overflowPipe(0)
    //) 
    {
      //switch (
      //  Cat(buttons(SnesButtons.L), buttons(SnesButtons.R))
      //) {
      //  is (B"01") {
      //    // L button down, R button up
      //    rPlayerTileIdx := rPlayerTileIdx - 1
      //  }
      //  is (B"10") {
      //    // R button down, L button up
      //    rPlayerTileIdx := rPlayerTileIdx + 1
      //  }
      //  default {
      //  }
      //}
      when (!buttons(SnesButtons.R)) {
        rPlayerTileIdx := rPlayerTileIdx + 1
      }
      when (!buttons(SnesButtons.L)) {
        rPlayerObjAttrsPrio := rPlayerObjAttrsPrio + 1
      }
      //rBgScroll.x := rBgScroll.x + 1
      switch (
        Cat(buttons(SnesButtons.Y), buttons(SnesButtons.A))
      ) {
        is (B"01") {
          // Y button down
          // scroll left
          rBgScroll.x := rBgScroll.x - 1
          //rBgScroll.x := rBgScroll.x - myBgScrollFracWidth
        }
        is (B"10") {
          // A button down
          // scroll right
          rBgScroll.x := rBgScroll.x + 1
          //rBgScroll.x := rBgScroll.x + myBgScrollFracWidth
        }
        default {
        }
      }
      switch (
        Cat(buttons(SnesButtons.X), buttons(SnesButtons.B))
      ) {
        is (B"01") {
          // X button down
          // scroll up
          rBgScroll.y := rBgScroll.y - 1
          //rBgScroll.y := rBgScroll.y - myBgScrollFracWidth
        }
        is (B"10") {
          // B button down
          // scroll down
          rBgScroll.y := rBgScroll.y + 1
          //rBgScroll.y := rBgScroll.y + myBgScrollFracWidth
        }
        default {
        }
      }
      switch (
        Cat(buttons(SnesButtons.DpadLeft), buttons(SnesButtons.DpadRight))
      ) {
        is (B"01") {
          // move OBJ left
          rPlayerPos.x := rPlayerPos.x - 1
        }
        is (B"10") {
          // move OBJ right
          rPlayerPos.x := rPlayerPos.x + 1
        }
        default {
        }
      }
      switch (
        Cat(buttons(SnesButtons.DpadUp), buttons(SnesButtons.DpadDown))
      ) {
        is (B"01") {
          // move OBJ up
          rPlayerPos.y := rPlayerPos.y - 1
        }
        is (B"10") {
          // move OBJ down
          rPlayerPos.y := rPlayerPos.y + 1
        }
        default {
        }
      }
    }
  }
  def doSnesUpdate(): Unit = {
    //--------
    //--------
    tempObjAttrs.tileIdx := rPlayerTileIdx(
      rPlayerTileIdx.high downto myTileFracWidth
    )
    //--------
    // BEGIN: debug comment this out
    tempBgAttrs.scroll.x := (
      rBgScroll.x(rBgScroll.x.high downto myBgScrollFracWidth)
      //rBgScroll.x//(rBgScroll.x.high downto myBgScrollFracWidth)
    )
    tempBgAttrs.scroll.y := (
      rBgScroll.y(rBgScroll.y.high downto myBgScrollFracWidth)
      //rBgScroll.y//(rBgScroll.y.high downto myBgScrollFracWidth)
    )
    // END: debug comment this out
    //--------
    tempObjAttrs.pos.x := (
      rPlayerPos.x(rPlayerPos.x.high downto myObjPosFracWidth)
      //rPos.x(rPos.x.high downto 0)
    )
    tempObjAttrs.pos.y := (
      rPlayerPos.y(rPlayerPos.y.high downto myObjPosFracWidth)
      //rPos.y(rPos.y.high downto 0)
    )
    tempObjAttrs.prio := (
      rPlayerObjAttrsPrio(
        rPlayerObjAttrsPrio.high
        downto rPlayerObjAttrsPrio.getWidth - cfg.numBgsPow
      )
      //1
      //0
    )
    tempObjAttrs.size2d.x := cfg.objTileSize2d.x
    tempObjAttrs.size2d.y := cfg.objTileSize2d.y
    //tempObjAttrs.size2d.y := cfg.objTileSize2d.y - 1
    tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
    //tempObjAttrs.affine := tempObjAttrs.affine.getZero
    //tempObjAttrs.affine.doIt := True
    //tempObjAttrs.affine.mat(0)(0) := (
    //  //1 << (tempObjAttrs.affine.fracWidth - 1)
    //  //2 << tempObjAttrs.affine.fracWidth
    //  (1 << Gpu2dAffine.fracWidth)
    //  //| (1 << (Gpu2dAffine.fracWidth - 1))
    //  | (1 << (Gpu2dAffine.fracWidth - 2))
    //)
    //tempObjAttrs.affine.mat(0)(1) := 0
    //tempObjAttrs.affine.mat(1)(0) := 0
    //tempObjAttrs.affine.mat(1)(1) := (
    //  //1 << (tempObjAttrs.affine.fracWidth - 1)
    //  //2 << tempObjAttrs.affine.fracWidth
    //  (1 << Gpu2dAffine.fracWidth)
    //  //| (1 << (Gpu2dAffine.fracWidth - 1))
    //  | (1 << (Gpu2dAffine.fracWidth - 2))
    //)
    //tempObjAttrs := tempObjAttrs.getZero
    tempBgAttrs0PushValid := True
    pop.objAttrsPush.valid := True
    //pop.objAttrsPush.payload.objAttrs := (
    //  Gpu2dObjAttrs(cfg=cfg).getZero
    //)
    pop.objAttrsPush.payload.objAttrs := tempObjAttrs
    pop.objAttrsPush.payload.memIdx := (
      //rObjAttrsCnt.asUInt(cfg.objAttrsMemIdxWidth - 1 downto 0)
      //0x1
      playerObjAttrsIdx
    )
  }
  object SnesState extends SpinalEnum(
    defaultEncoding=binarySequential
  ) {
    val
      OBJ_ATTRS_INIT,
      WAIT_VBLANK,
      //WAIT_SNES_FIRE,
      SNES_UPDATE,
      WAIT_VISIB
      = newElement();
  }
  val rDidSnesFire = Reg(Bool()) init(False)
  val rSeenVblank = Reg(Bool()) init(False)
  val rSnesState = (
    Reg(SnesState()) init(SnesState.OBJ_ATTRS_INIT)
  )
  val someNextSeenVblank = (
    (
      //io.gpu2dPopFire
      //|| 
      //io.vgaSomeVpipeS === LcvVgaState.visib
      //&& io.vgaSomeDrawPos.x === 0x0
      //&& io.vgaSomeDrawPos.y === 0x0
      io.vgaSomeVpipeS === LcvVgaState.front
    )
  )
  when (
    !rDidSnesFire
  ) {
    when (
      if (!optRawSnesButtons) {
        !snesHelper.io.pop.fire
      } else { // if (optRawSnesButtons)
        !io.rawSnesButtons.fire
      }
    ) {
    } otherwise {
      when (
        rSeenVblank
        //|| someNextSeenVblank
      ) {
        rSnesPopReady := False
        rDidSnesFire := True
        doSnesFire()
        doSnesUpdate()
      }
    }
  }
  switch (rSnesState) {
    is (SnesState.OBJ_ATTRS_INIT) {
      rSnesPopReady := True
      rSeenVblank := False
      rDidSnesFire := False
      doObjAttrsInit()
      when (rObjAttrsCnt >= cfg.numObjs) {
        rSnesState := SnesState.WAIT_VBLANK
      }
    }
    is (SnesState.WAIT_VBLANK) {
      when (
        !rSeenVblank
        && someNextSeenVblank
      ) {
        rSeenVblank := True
      }
      //when (
      //  !rDidSnesFire
      //) {
      //  when (
      //    if (!optRawSnesButtons) {
      //      !snesHelper.io.pop.fire
      //    } else { // if (optRawSnesButtons)
      //      !io.rawSnesButtons.fire
      //    }
      //  ) {
      //    when (
      //      rSeenVblank
      //      //|| someNextSeenVblank
      //    ) {
      //      rSnesPopReady := False
      //      rDidSnesFire := True
      //      doSnesFire()
      //    }
      //  }
      //}
      when (rSeenVblank && rDidSnesFire) {
        rSnesState := SnesState.SNES_UPDATE
      }
    }
    is (SnesState.SNES_UPDATE) {
      //when (io.vgaSomeVpipeS === LcvVgaState.visib) {
        //rSnesState := SnesState.WAIT_VBLANK_AND_SNES_FIRE
        rSnesState := SnesState.WAIT_VISIB
      //} otherwise {
        rSeenVblank := False
        rDidSnesFire := False
        rSnesPopReady := True
      //}
    }
    is (SnesState.WAIT_VISIB) {
      when (
        io.vgaSomeVpipeS === LcvVgaState.visib
      ) {
        rSnesState := SnesState.WAIT_VBLANK
      }
    }
    //is (SnesState.WAIT_SNES_FIRE) {
    //}
    //is (SnesState.SNES_FIRE) {
    //}
  }
  //when (
  //  if (!optRawSnesButtons) {
  //    !snesHelper.io.pop.fire
  //  } else { // if (optRawSnesButtons)
  //    !io.rawSnesButtons.fire
  //  }
  //) {
  //  doObjAttrsInit()
  //} elsewhen (!rDidSnesFire) {
  //  doSnesFire()
  //  rDidSnesFire := True
  //}
  //when (
  //  //!io.vgaPhys.vsync
  //  //&& 
  //  io.gpu2dPopFire
  //  || io.vgaVpipeSPipe2 === LcvVgaState.visib
  //) {
  //  //tempBgAttrs0PushValid := False
  //  //pop.objAttrsPush.valid := False
  //} otherwise {
  //}

  //when (!rDidSnesFire) {
  //  tempBgAttrs0PushValid := False
  //  pop.objAttrsPush.valid := False
  //} otherwise {
  //  when (
  //    //!io.vgaPhys.vsync
  //    //&& 
  //    io.gpu2dPopFire
  //    || io.vgaVpipeSPipe2 === LcvVgaState.visib
  //  ) {
  //    tempBgAttrs0PushValid := False
  //    pop.objAttrsPush.valid := False
  //  } otherwise {
  //    //--------
  //    rDidSnesFire := False
  //    //--------
  //    tempObjAttrs.tileIdx := rObjTileIdx(
  //      rObjTileIdx.high downto myTileFracWidth
  //    )
  //    //--------
  //    // BEGIN: debug comment this out
  //    tempBgAttrs.scroll.x := (
  //      rBgScroll.x(rBgScroll.x.high downto myBgScrollFracWidth)
  //    )
  //    tempBgAttrs.scroll.y := (
  //      rBgScroll.y(rBgScroll.y.high downto myBgScrollFracWidth)
  //    )
  //    // END: debug comment this out
  //    //--------
  //    tempObjAttrs.pos.x := (
  //      rObjPos.x(rObjPos.x.high downto myObjPosFracWidth)
  //      //rPos.x(rPos.x.high downto 0)
  //    )
  //    tempObjAttrs.pos.y := (
  //      rObjPos.y(rObjPos.y.high downto myObjPosFracWidth)
  //      //rPos.y(rPos.y.high downto 0)
  //    )
  //    tempObjAttrs.prio := (
  //      //1
  //      0
  //    )
  //    tempObjAttrs.size2d.x := cfg.objTileSize2d.x
  //    tempObjAttrs.size2d.y := cfg.objTileSize2d.y
  //    //tempObjAttrs.size2d.y := cfg.objTileSize2d.y - 1
  //    tempObjAttrs.dispFlip := tempObjAttrs.dispFlip.getZero
  //    //tempObjAttrs.affine := tempObjAttrs.affine.getZero
  //    //tempObjAttrs.affine.doIt := True
  //    //tempObjAttrs.affine.mat(0)(0) := (
  //    //  //1 << (tempObjAttrs.affine.fracWidth - 1)
  //    //  //2 << tempObjAttrs.affine.fracWidth
  //    //  (1 << Gpu2dAffine.fracWidth)
  //    //  //| (1 << (Gpu2dAffine.fracWidth - 1))
  //    //  | (1 << (Gpu2dAffine.fracWidth - 2))
  //    //)
  //    //tempObjAttrs.affine.mat(0)(1) := 0
  //    //tempObjAttrs.affine.mat(1)(0) := 0
  //    //tempObjAttrs.affine.mat(1)(1) := (
  //    //  //1 << (tempObjAttrs.affine.fracWidth - 1)
  //    //  //2 << tempObjAttrs.affine.fracWidth
  //    //  (1 << Gpu2dAffine.fracWidth)
  //    //  //| (1 << (Gpu2dAffine.fracWidth - 1))
  //    //  | (1 << (Gpu2dAffine.fracWidth - 2))
  //    //)
  //    //tempObjAttrs := tempObjAttrs.getZero
  //    tempBgAttrs0PushValid := True
  //    pop.objAttrsPush.valid := True
  //    //pop.objAttrsPush.payload.objAttrs := (
  //    //  Gpu2dObjAttrs(cfg=cfg).getZero
  //    //)
  //    pop.objAttrsPush.payload.objAttrs := tempObjAttrs
  //    pop.objAttrsPush.payload.memIdx := (
  //      //rObjAttrsCnt.asUInt(cfg.objAttrsMemIdxWidth - 1 downto 0)
  //      0x1
  //    )
  //  }
  //}

  //--------
  //val tempObjAffineTileSlice = Gpu2dTileSlice(
  //  cfg=cfg,
  //  isObj=true,
  //  isAffine=true,
  //)

  //val nextObjAffineTileCnt = SInt(
  //  //cfg.numObjAffineTilesPow + 2 bits
  //  //cfg.objAffineTilePxMemIdxWidth + 5 bits
  //  (
  //    cfg.objAffineTilePxMemIdxWidth
  //    //+ cfg.objAffineSliceTileWidthPow
  //    + 5
  //  ) bits
  //)
  //val rObjAffineTileCnt = RegNext(nextObjAffineTileCnt) init(-1)
  val rObjAffineTilePushValid = Reg(Bool()) init(False)

  //def mkObjAffineTile(
  //  colIdx0: Int,
  //  colIdx1: Int,
  //  colIdx2: Option[Int]=None,
  //  colIdx3: Option[Int]=None,
  //): Unit = {
  //  //mkTile(
  //  //  tempTile=tempObjAffineTile,
  //  //  colIdx0=colIdx0,
  //  //  colIdx1=colIdx1,
  //  //  colIdx2=colIdx2,
  //  //  colIdx3=colIdx3,
  //  //)
  //  mkTile(
  //    //tempTile=tempColorMathTile,
  //    tempTileSlice=tempObjAffineTileSlice,
  //    pxCoordXStart={
  //      if (cfg.objAffineTileWidthRshift == 0) {
  //        def tempWidthPow = cfg.objAffineTileSize2dPow.x
  //        U(f"$tempWidthPow'd0")
  //      } else {
  //        def tempWidthPow = cfg.objAffineSliceTileWidthPow
  //        U(f"$tempWidthPow'd0")
  //        //rObjAffineTileCnt(
  //        //  //cfg.objAffineTileSize2dPow.x - 1
  //        //  ////downto cfg.objAffineSliceTileWidthPow
  //        //  //downto cfg.objAffineTileWidthRshift
  //        //  ////cfg.objAffineSliceTileWidthPow - 1 downto 0
  //        //  ////cfg.objAffineTileWidthRshift - 1 downto 0
  //        //  //cfg.objAffineTileSize2dPow.x - 1 downto 0
  //        //  cfg.objAffineSliceTileWidthPow - 1 downto 0
  //        //  //cfg.objAffineTileSize2dPow.x - 1
  //        //  //downto cfg.objAffineTileWidthRshift
  //        //).asUInt
  //      }
  //    },
  //    pxCoordY=(
  //      //rObjAffineTileCnt(
  //      //  cfg.objAffineTileSize2dPow.y - 1
  //      //  downto cfg.objAffineTileSize2dPow.x
  //      //).asUInt
  //      rObjAffineTileCnt(
  //        (
  //          cfg.objAffineTileSize2dPow.y
  //          ////+ cfg.objAffineSliceTileWidthPow
  //          //+ cfg.objAffineTileWidthRshift
  //          + cfg.objAffineTileSize2dPow.x
  //          - 1
  //        )
  //        downto (
  //          //cfg.objAffineTileSize2dPow.x
  //          //cfg.objAffineSliceTileWidthPow
  //          //cfg.objAffineTileWidthRshift
  //          cfg.objAffineTileSize2dPow.x
  //        )
  //      ).asUInt
  //    ),
  //    palEntryMemIdxWidth=cfg.objPalEntryMemIdxWidth,
  //    colIdx0=colIdx0,
  //    colIdx1=colIdx1,
  //    colIdx2=colIdx2,
  //    colIdx3=colIdx3,
  //    //onePx=true,
  //  )
  //}
  //def tempObjAffineTileCnt = (
  //  rObjAffineTileCnt(
  //    rObjAffineTileCnt.high
  //    downto (
  //      cfg.objAffineTileSize2dPow.y
  //      //+ cfg.objAffineSliceTileWidthPow
  //      //+ cfg.objAffineTileWidthRshift
  //      + cfg.objAffineTileSize2dPow.x
  //    )
  //  )
  //  //>> (
  //  //  cfg.objAffineTileSize2dPow.y
  //  //  + cfg.objAffineSliceTileWidthPow
  //  //  //+ cfg.objAffineTileWidthRshift
  //  //)
  //)
  //tempObjAffineTileSlice := tempObjAffineTileSlice.getZero
  //tempObjAffineTileSlice.allowOverride

  //when (tempObjAffineTileCnt < cfg.numObjAffineTiles) {
  //  when (pop.objAffineTilePush.fire) {
  //    when (tempObjAffineTileCnt === 0) {
  //      //mkObjAffineTile(0, 1)
  //      mkObjAffineTile(0, 0)
  //    } elsewhen (tempObjAffineTileCnt === 1) {
  //      //mkObjAffineTile(1, 2, Some(3), Some(4))
  //      mkObjAffineTile(1, 2, Some(3), Some(4))
  //      //mkObjAffineTile(1, 1, Some(3), Some(4))
  //      //mkObjAffineTile(1, 1)
  //      //mkObjAffineTile(3, 3)
  //      //mkObjAffineTile(2, 2)
  //    } elsewhen (tempObjAffineTileCnt === 2) {
  //      //mkObjAffineTile(2, 2)
  //      //mkObjAffineTile(2, 3)
  //      //mkObjAffineTile(3, 4)
  //      mkObjAffineTile(1, 2, Some(3), Some(4))
  //      //mkObjAffineTile(3, 3)
  //      //mkObjAffineTile(2, 2)
  //    } elsewhen (tempObjAffineTileCnt === 3) {
  //      //mkObjAffineTile(3, 4)
  //      mkObjAffineTile(3, 3)
  //      //mkObjAffineTile(0, 1)
  //    } elsewhen (tempObjAffineTileCnt === 4) {
  //      //mkObjAffineTile(4, 5)
  //      mkObjAffineTile(4, 4)
  //    } otherwise {
  //      //tempObjAffineTileSlice := tempObjAffineTileSlice.getZero
  //      //when (tempObjAffineTileCnt >= cfg.numObjAffineTiles) {
  //      //  rObjAffineTilePushValid := False
  //      //}
  //    }
  //    nextObjAffineTileCnt := rObjAffineTileCnt + 1
  //  } otherwise {
  //    //tempObjAffineTileSlice := tempObjAffineTileSlice.getZero
  //    nextObjAffineTileCnt := rObjAffineTileCnt
  //  }
  //} otherwise {
  //  //tempObjAffineTileSlice := tempObjAffineTileSlice.getZero
  //  nextObjAffineTileCnt := rObjAffineTileCnt
  //}
  //when (tempObjAffineTileCnt >= cfg.numObjAffineTiles - 1) {
  //  rObjAffineTilePushValid := False
  //}
  pop.objAffineTilePush.valid := rObjAffineTilePushValid
  pop.objAffineTilePush.payload := (
    pop.objAffineTilePush.payload.getZero
  )
  ////pop.objAffineTilePush.payload.tileSlice := tempObjAffineTileSlice
  //pop.objAffineTilePush.payload.tilePx := tempObjAffineTileSlice.getPx(
  //  rObjAffineTileCnt(
  //    //cfg.objAffineTileSize2dPow.x - 1
  //    ////downto 0
  //    //downto cfg.objAffineTileWidthRshift
  //    cfg.objAffineSliceTileWidthPow - 1 downto 0
  //  ).asUInt
  //)
  //pop.objAffineTilePush.payload.memIdx := (
  //  rObjAffineTileCnt.asUInt(
  //    //pop.objAffineTilePush.payload.memIdx.bitsRange
  //    (
  //      pop.objAffineTilePush.payload.memIdx.getWidth
  //      //+ cfg.objAffineTileSize2dPow.x
  //      - 1
  //    ) downto 0 //cfg.objAffineTileSize2dPow.x
  //  )
  //)
  //--------
  val tempObjAffineAttrs = Gpu2dObjAttrs(
    cfg=cfg,
    isAffine=true,
  )
  //val objAffineAttrsCntWidth = cfg.numObjsAffinePow + 2
  //val objAffineAttrsCntWidth = cfg.objAffineTilePxMemIdxWidth + 2
  val objAffineAttrsCntWidth = cfg.numObjsAffinePow + 2
  //val rObjAffineAttrsCnt = Reg(UInt(objAffineAttrsCntWidth bits)) init(0x0)
  //val nextObjAffineAttrsCnt = UInt(objAffineAttrsCntWidth bits)
  //val rObjAffineAttrsCnt = RegNext(nextObjAffineAttrsCnt) init(0x0)
  val nextObjAffineAttrsCnt = SInt(objAffineAttrsCntWidth bits)
  val rObjAffineAttrsCnt = RegNext(nextObjAffineAttrsCnt) init(-1)
  //val rObjAffineAttrs = Reg(Gpu2dObjAffineAttrs(cfg=cfg))
  //rObjAffineAttrs.init(rObjAffineAttrs.getZero)
  val rObjAffineAttrsEntryPushValid = Reg(Bool()) init(True)
  if (!cfg.noColorMath) {
    tempObjAffineAttrs.colorMathInfo := (
      tempObjAffineAttrs.colorMathInfo.getZero
    )
  }

  when (rObjAffineAttrsCnt < cfg.numObjsAffine) {
    when (rObjAffineAttrsCnt === 0) {
      tempObjAffineAttrs.tileIdx := (
        //1
        2
      )
      //tempObjAffineAttrs.tileMemIdx := 2
      tempObjAffineAttrs.pos.x := (
        //cfg.intnlFbSize2d.x - cfg.objAffineTileSize2d.x //- 1
        //cfg.intnlFbSize2d.x - cfg.objAffineTileSize2d.x - 5
        //cfg.intnlFbSize2d.x >> 1
        //0x3e
        //3
        //2
        //1
        //0
        //7
        //40
        //48
        //32
        //64
        //(cfg.objAffineTileSize2d.x.toDouble * 1.5).toInt
        48
        //- cfg.objAffineTileSize2d.x
        + (cfg.objAffineTileSize2d.x / 2)
        //- (cfg.objAffineTileSize2d.x / 2)
        //cfg.objAffineTileSize2d.x / 2
        //8
      )
      tempObjAffineAttrs.pos.y := (
        //8
        //0
        //-cfg.objAffineTileSize2d.y / 2
        //(cfg.objAffineTileSize2d.y.toDouble * 1.5).toInt
        //64
        //cfg.objAffineTileSize2d.y / 2
        8
      )
      //tempObjAffineAttrs.pos.y := 0
      //tempObjAffineAttrs.prio := 0
      tempObjAffineAttrs.prio := (
        1
        //0
      )
      tempObjAffineAttrs.size2d.x := cfg.objAffineTileSize2d.x
      tempObjAffineAttrs.size2d.y := cfg.objAffineTileSize2d.y
      //tempObjAffineAttrs.size2d.y := cfg.objAffineTileSize2d.y - 1
      tempObjAffineAttrs.dispFlip := tempObjAffineAttrs.dispFlip.getZero
      //tempObjAffineAttrs.affine := tempObjAffineAttrs.affine.getZero
      tempObjAffineAttrs.affine.doIt := True
      //tempObjAffineAttrs.affine.mat(0)(0) := (
      //  //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
      //  //2 << tempObjAffineAttrs.affine.fracWidth
      //  (1 << Gpu2dAffine.fracWidth)
      //  //|
      //  //(1 << (Gpu2dAffine.fracWidth - 1))
      //  //////| (1 << (Gpu2dAffine.fracWidth - 2))
      //)
      //tempObjAffineAttrs.affine.mat(0)(1) := 0
      //tempObjAffineAttrs.affine.mat(1)(0) := 0
      //tempObjAffineAttrs.affine.mat(1)(1) := (
      //  //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
      //  //2 << tempObjAffineAttrs.affine.fracWidth
      //  (1 << Gpu2dAffine.fracWidth)
      //  //|
      //  //(1 << (Gpu2dAffine.fracWidth - 1))
      //  //////| (1 << (Gpu2dAffine.fracWidth - 2))
      //)
      tempObjAffineAttrs.affine.mat(0)(0) := (
        //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
        //2 << tempObjAffineAttrs.affine.fracWidth
        //(1 << Gpu2dAffine.fracWidth)
        //| 
        //(1 << (Gpu2dAffine.fracWidth - 1))
        //| (1 << (Gpu2dAffine.fracWidth - 2))
        1
      ) * (255)
      tempObjAffineAttrs.affine.mat(0)(1) := (
        //0
        -5
      )
      tempObjAffineAttrs.affine.mat(1)(0) := (
        //0
        5
      )
      tempObjAffineAttrs.affine.mat(1)(1) := (
        //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
        //2 << tempObjAffineAttrs.affine.fracWidth
        //(1 << Gpu2dAffine.fracWidth)
        //| 
        //(1 << (Gpu2dAffine.fracWidth - 1))
        //| (1 << (Gpu2dAffine.fracWidth - 2))
        1
      ) * (255)
      //tempObjAffineAttrs.affine.mat(0)(0) := (
      //  //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
      //  //2 << tempObjAffineAttrs.affine.fracWidth
      //  //(1 << Gpu2dAffine.fracWidth)
      //  //| 
      //  //(1 << (Gpu2dAffine.fracWidth - 1))
      //  //| (1 << (Gpu2dAffine.fracWidth - 2))
      //  1
      //) * (255)
      //tempObjAffineAttrs.affine.mat(0)(1) := (
      //  //0
      //  -5
      //)
      //tempObjAffineAttrs.affine.mat(1)(0) := (
      //  //0
      //  5
      //)
      //tempObjAffineAttrs.affine.mat(1)(1) := (
      //  //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
      //  //2 << tempObjAffineAttrs.affine.fracWidth
      //  //(1 << Gpu2dAffine.fracWidth)
      //  //| 
      //  //(1 << (Gpu2dAffine.fracWidth - 1))
      //  //| (1 << (Gpu2dAffine.fracWidth - 2))
      //  1
      //) * (255)
      //tempObjAffineAttrs := tempObjAffineAttrs.getZero
    } elsewhen (rObjAffineAttrsCnt === 1) {
      //tempObjAffineAttrs.tileMemIdx := 1
      tempObjAffineAttrs.tileIdx := 2
      //tempObjAffineAttrs.tileMemIdx := 0
      //tempObjAffineAttrs.pos.x := 1
      //tempObjAffineAttrs.pos.x := 16
      //tempObjAffineAttrs.pos.x := 2
      //tempObjAffineAttrs.pos.x := 16
      tempObjAffineAttrs.pos.x := (
        //1
        //2
        //7
        32
        //48
        - (cfg.objAffineTileSize2d.x / 2)
        //-cfg.objAffineDblTileSize2d.x
      )
      //tempObjAffineAttrs.pos.x := 9
      //tempObjAffineAttrs.pos.x := 9
      //tempObjAffineAttrs.pos.y := -1
      //tempObjAffineAttrs.pos.y := 8
      tempObjAffineAttrs.pos.y := (
        9
        //0
        //-cfg.objAffineTileSize2d.y / 2
      )
      tempObjAffineAttrs.prio := (
        0
        //1
      )
      //tempObjAffineAttrs.size2d.x := cfg.objAffineTileSize2d.x / 2
      //tempObjAffineAttrs.size2d.y := cfg.objAffineTileSize2d.y / 2
      tempObjAffineAttrs.size2d.x := cfg.objAffineTileSize2d.x
      tempObjAffineAttrs.size2d.y := cfg.objAffineTileSize2d.y
      tempObjAffineAttrs.dispFlip := tempObjAffineAttrs.dispFlip.getZero
      //tempObjAffineAttrs.affine := tempObjAffineAttrs.affine.getZero
      tempObjAffineAttrs.affine.doIt := True
      tempObjAffineAttrs.affine.mat(0)(0) := (
        //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
        //2 << tempObjAffineAttrs.affine.fracWidth
        (1 << Gpu2dAffine.fracWidth)
        | 
        (1 << (Gpu2dAffine.fracWidth - 1))
        //| (1 << (Gpu2dAffine.fracWidth - 2))
        //1
      ) //* (255)
      tempObjAffineAttrs.affine.mat(0)(1) := (
        0
        //-5
      )
      tempObjAffineAttrs.affine.mat(1)(0) := (
        0
        //5
      )
      tempObjAffineAttrs.affine.mat(1)(1) := (
        //1 << (tempObjAffineAttrs.affine.fracWidth - 1)
        //2 << tempObjAffineAttrs.affine.fracWidth
        (1 << Gpu2dAffine.fracWidth)
        | 
        (1 << (Gpu2dAffine.fracWidth - 1))
        //| (1 << (Gpu2dAffine.fracWidth - 2))
        //1
      ) //* (255)
      //tempObjAffineAttrs := tempObjAffineAttrs.getZero
    //} elsewhen (rObjAffineAttrsCnt === 2) {
    ////  tempObjAffineAttrs.tileMemIdx := 3
    ////  tempObjAffineAttrs.pos.x := 8
    ////  //tempObjAffineAttrs.pos.x := 7
    ////  tempObjAffineAttrs.pos.y := 8
    ////  tempObjAffineAttrs.prio := (
    ////    0
    ////    //1
    ////  )
    ////  tempObjAffineAttrs.size2d.x := cfg.objAffineTileSize2d.x
    ////  tempObjAffineAttrs.size2d.y := cfg.objAffineTileSize2d.y
    ////  tempObjAffineAttrs.dispFlip := tempObjAffineAttrs.dispFlip.getZero
    ////  tempObjAffineAttrs.affine := tempObjAffineAttrs.affine.getZero
    ////  //tempObjAffineAttrs := tempObjAffineAttrs.getZero
    //////} elsewhen (rObjAffineAttrsCnt === 3) {
    //////  //tempObjAffineAttrs.tileMemIdx := 0
    //////  //tempObjAffineAttrs.pos.x := 8
    //////  //tempObjAffineAttrs.pos.y := 0 //+ cfg.objAffineTileSize2d.y - 1
    //////  //tempObjAffineAttrs.prio := 0
    //////  //tempObjAffineAttrs.dispFlip := tempObjAffineAttrs.dispFlip.getZero
    //////  tempObjAffineAttrs := tempObjAffineAttrs.getZero
    } otherwise {
      //tempObjAffineAttrs := tempObjAffineAttrs.getZero
      tempObjAffineAttrs.tileIdx := 0
      tempObjAffineAttrs.pos.x := -cfg.objAffineTileSize2d.x
      tempObjAffineAttrs.pos.y := (
        //0
        -cfg.objAffineTileSize2d.y
      )
      tempObjAffineAttrs.prio := (
        0
        //1
      )
      tempObjAffineAttrs.size2d.x := cfg.objAffineTileSize2d.x
      tempObjAffineAttrs.size2d.y := cfg.objAffineTileSize2d.y
      tempObjAffineAttrs.dispFlip := tempObjAffineAttrs.dispFlip.getZero
      tempObjAffineAttrs.affine := tempObjAffineAttrs.affine.getZero
      when (nextObjAffineAttrsCnt >= cfg.numObjsAffine) {
        rObjAffineAttrsEntryPushValid := False
      }
      //rObjAffineAttrsEntryPushValid := False
    }
    when (pop.objAffineAttrsPush.fire) {
      nextObjAffineAttrsCnt := rObjAffineAttrsCnt + 1
    } otherwise {
      nextObjAffineAttrsCnt := rObjAffineAttrsCnt
    }
    //} otherwise {
    //  tempObjAffineAttrs := tempObjAffineAttrs.getZero
    //  nextObjAffineAttrsCnt := rObjAffineAttrsCnt
    //}
  } otherwise {
    tempObjAffineAttrs := tempObjAffineAttrs.getZero
    nextObjAffineAttrsCnt := rObjAffineAttrsCnt
  }

  pop.objAffineAttrsPush.valid := rObjAffineAttrsEntryPushValid
  //pop.objAffineAttrsPush.payload.objAffineAttrs := (
  //  Gpu2dObjAffineAttrs(cfg=cfg).getZero
  //)
  pop.objAffineAttrsPush.payload.objAttrs := tempObjAffineAttrs
  pop.objAffineAttrsPush.payload.memIdx := (
    rObjAffineAttrsCnt.asUInt(cfg.objAffineAttrsMemIdxWidth - 1 downto 0)
  )

  ////--------
}
