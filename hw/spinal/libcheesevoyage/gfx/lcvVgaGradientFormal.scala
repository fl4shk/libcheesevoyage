//package libcheesevoyage.gfx
//import libcheesevoyage._
//import libcheesevoyage.general.Vec2
//import libcheesevoyage.general.ElabVec2
//
//import spinal.core._
//import spinal.core.formal._
//import spinal.lib._
//import spinal.lib.graphic.Rgb
//import spinal.lib.graphic.RgbConfig
//import scala.collection.mutable.ArrayBuffer
//import scala.math._
//
//object LcvVgaGradientFormal extends App {
//  val ctrlFifoDepth = 20
//  //val ctrlFifoDepth = 100
//  val fbSize2d = ElabVec2[Int](640, 480)
//  //val fbSize2d = ElabVec2[Int](1, 1)
//  val rgbConfig = RgbConfig(rWidth=6, gWidth=6, bWidth=6)
//  val physRgbConfig = LcvVideoDithererIo.outRgbConfig(rgbConfig=rgbConfig)
//  //val vgaTimingInfo = LcvVgaTimingInfoMap.map("640x480@60")
//  val vgaTimingInfo=LcvVgaTimingInfo(
//    pixelClk=25.0,
//    //pixelClk=25.175,
//    htiming=LcvVgaTimingHv(
//      visib=640,
//      front=16,
//      sync=96,
//      back=48
//      //visib=1,
//      //front=1,
//      //sync=1,
//      //back=1,
//    ),
//    vtiming=LcvVgaTimingHv(
//      visib=480,
//      front=10,
//      sync=2,
//      back=33
//      //visib=1,
//      //front=1,
//      //sync=1,
//      //back=1,
//    ),
//  )
//  case class Dut() extends Component {
//    assumeInitial(clockDomain.isResetActive)
//    //val vgaGrad = LcvVgaGradient(rgbConfig=rgbConfig)
//    val vgaCtrl = LcvVgaCtrl(
//      clkRate=100.0,
//      //clkRate=50.0,
//      //clkRate=100.7,
//      rgbConfig=physRgbConfig,
//      vgaTimingInfo=vgaTimingInfo,
//      //vgaTimingInfo=LcvVgaTimingInfoMap.map("640x480@60"),
//      //vgaTimingInfo=LcvVgaTimingInfo(
//      //  pixelClk=25.0,
//      //  //pixelClk=25.175,
//      //  htiming=LcvVgaTimingHv(
//      //    visib=640,
//      //    front=16,
//      //    sync=96,
//      //    back=48
//      //  ),
//      //  vtiming=LcvVgaTimingHv(
//      //    visib=480,
//      //    front=10,
//      //    sync=2,
//      //    back=33
//      //  ),
//      //),
//      fifoDepth=ctrlFifoDepth,
//    )
//    val vidDith = LcvVideoDitherer(
//      //fbSize2d=fbSize2d,
//      rgbConfig=rgbConfig,
//      //vgaTimingInfo=vgaTimingInfo,
//      fbSize2d=vgaTimingInfo.fbSize2d,
//    )
//    val vgaGrad = LcvVgaGradient(
//      rgbConfig=rgbConfig,
//      vgaTimingInfo=vgaTimingInfo,
//    )
//    //val vgaCtrlMisc = LcvVgaCtrlMiscIo()
//    val ctrlIo = vgaCtrl.io
//    val dithIo = vidDith.io
//    val gradIo = vgaGrad.io
//    //ctrlIo.en := True
//    //ctrlIo.push.valid := True
//    //val inpCol = ctrlIo.push.payload
//    ////val inpCol = ctrlIo.inpCol
//    //inpCol.r := (default -> True)
//    //inpCol.g := (default -> False)
//    //inpCol.b := (default -> False)
//    
//    //cover(ctrlIo.misc.visib)
//
//    ctrlIo.en := gradIo.vgaCtrlIo.en
//    ctrlIo.push << gradIo.vgaCtrlIo.push
//    dithIo.push << gradIo.vidDitherIo.push
//    dithIo.pop >> gradIo.vidDitherIo.pop
//
//    //val cntWidth = 8 + 1
//    val maxCnt = 6
//    //val maxCnt = 7
//    val cntWidth = log2Up(maxCnt) + 1
//    val ctrlPushFireCnt = Reg(UInt(cntWidth bits)) init(0x0)
//    assumeInitial(ctrlPushFireCnt === 0x0)
//    //val nextCtrlPushFireCnt = ctrlPushFireCnt.wrapNext()
//    val dithPushFireCnt = Reg(UInt(cntWidth bits)) init(0x0)
//    assumeInitial(dithPushFireCnt === 0x0)
//    //val nextDithPushFireCnt = dithPushFireCnt.wrapNext()
//    val dithPopFireCnt = Reg(UInt(cntWidth bits)) init(0x0)
//    assumeInitial(dithPopFireCnt === 0x0)
//    //val nextDithPopFireCnt = dithPopFireCnt.wrapNext()
//    //val arrSize = (1 << (cntWidth - 1))
//    //val ctrlPushValid = Reg(UInt(arrSize bits)) init(0x0)
//    //val ctrlPushReady = Reg(UInt(arrSize bits)) init(0x0)
//    //val ctrlPushFire = Reg(UInt(arrSize bits)) init(0x0)
//    //val ctrlFifoEmpty = Reg(UInt(arrSize bits)) init(0x0)
//    //val ctrlFifoFull = Reg(UInt(arrSize bits)) init(0x0)
//    //val dithPushValid = Reg(UInt(arrSize bits)) init(0x0)
//    //val dithPushReady = Reg(UInt(arrSize bits)) init(0x0)
//    //val dithPushFire = Reg(UInt(arrSize bits)) init(0x0)
//    //val dithPopValid = Reg(UInt(arrSize bits)) init(0x0)
//    //val dithPopReady = Reg(UInt(arrSize bits)) init(0x0)
//    //val dithPopFire = Reg(UInt(arrSize bits)) init(0x0)
//
//    //case class DbgElem() extends Bundle {
//    //  val ctrlPushValid = Bool()
//    //  val ctrlPushReady = Bool()
//    //  val ctrlPushFire = Bool()
//    //  val ctrlFifoEmpty = Bool()
//    //  val ctrlFifoFull = Bool()
//    //  val dithPushValid = Bool()
//    //  val dithPushReady = Bool()
//    //  val dithPushFire = Bool()
//    //  val dithPopValid = Bool()
//    //  val dithPopReady = Bool()
//    //  val dithPopFire = Bool()
//    //}
//    //val arr = Vec(Reg(DbgElem()), arrSize)
//    //val arr = ArrayBuffer[DbgElem]()
//
//    //for (idx <- 0 to arrSize - 1) {
//    //  val tempElem = Reg(DbgElem())
//    //  tempElem.init(tempElem.getZero)
//    //  arr += tempElem
//    //  //val tempElem = arr(idx)
//    //  //tempElem.init(tempElem.getZero)
//    //}
//    //val foundVisib = Bool()
//    //val rFoundVisib = Reg(Bool()) init(False)
//    ////val nextFoundVisib = rFoundVisib.wrapNext()
//    ////assumeInitial(nextFoundVisib === False)
//    //val nextFoundVisib = Bool()
//    ////nextFoundVisib := ctrlIo.misc.visib && ctrlIo.misc.pixelEn
//    ////nextFoundVisib := ctrlIo.misc.visib
//    //nextFoundVisib := ctrlIo.misc.nextVisib
//    val concurHscVisib = Bool()
//    val concurVscVisib = Bool()
//    concurHscVisib := ctrlIo.misc.dbgHscS === LcvVgaState.visib
//    concurVscVisib := ctrlIo.misc.dbgVscS === LcvVgaState.visib
//    val concurVisib = Bool()
//    concurVisib := concurHscVisib && concurVscVisib
//    val allCtrlPushFireCntWidth = 32
//    val allCtrlPushFireCnt = Reg(UInt(allCtrlPushFireCntWidth bits))
//    allCtrlPushFireCnt.init(0x0)
//    when (pastValidAfterReset) {
//      //rFoundVisib := nextFoundVisib
//      //cover(rFoundVisib)
//      //cover(nextFoundVisib)
//
//      //val ctrlFifoWasFull = Reg(Bool()) init(False)
//      //when (!ctrlFifoWasFull && ctrlIo.misc.dbgFifoFull) {
//      //  ctrlFifoWasFull := True
//      //  //cnt := cnt + 1
//      //  nextCtrlPushFireCnt := ctrlPushFireCnt + 1
//      //  nextDithPushFireCnt := dithPushFireCnt + 1
//      //  nextDithPopFireCnt := dithPopFireCnt + 1
//      //}
//      //cover(nextFoundVisib)
//      //cover(ctrlIo.phys.col.r =/= 0)
//      //cover(concurVisib)
//      //cover(ctrlIo.phys.col.r =/= 0)
//      cover(ctrlIo.push.payload.r =/= 0)
//      cover(ctrlIo.phys.hsync)
//      cover(ctrlIo.phys.vsync)
//
//      val rPhysCol = Reg(Rgb(physRgbConfig)) addAttribute("keep")
//      rPhysCol.init(rPhysCol.getZero)
//
//      when (
//        //ctrlIo.misc.visib && ctrlIo.misc.pixelEn
//        //nextFoundVisib
//        //rFoundVisib
//        concurVisib
//      ) {
//        rPhysCol := ctrlIo.phys.col
//        cover(
//          (rPhysCol.r(0) === True)
//          || (rPhysCol.r(0) === False)
//        )
//        //assert(rPhysCol.r(0) | ~rPhysCol.r(0))
//        //cover((rPhysCol.r(0) === False)
//        //  || (rPhysCol.r(0) === True))
//        //cover(ctrlIo.phys.col.r =/= 0)
//        //cover(ctrlIo.phys.col.r === 0)
//        //cover((ctrlIo.phys.col.r === 0) || (ctrlIo.phys.col.r =/= 0))
//        //assert(rFoundVisib)
//        //cover(rFoundVisib)
//        //when (!foundVisib)
//        //when (rFoundVisib) {
//          //when (!ctrlIo.push.fire) {
//          //  ctrlPushFireCnt := ctrlPushFireCnt + 1
//          //}
//          //when (!dithIo.push.fire) {
//          //  dithPushFireCnt := dithPushFireCnt + 1
//          //}
//          //when (!dithIo.pop.fire) {
//          //  dithPopFireCnt := dithPopFireCnt + 1
//          //}
//          //cover(dithPushFireCnt.resized === (maxCnt - 4))
//          ctrlPushFireCnt := ctrlPushFireCnt + 1
//          dithPushFireCnt := dithPushFireCnt + 1
//          dithPopFireCnt := dithPopFireCnt + 1
//          assert(ctrlPushFireCnt < maxCnt)
//          assert(dithPushFireCnt < maxCnt)
//          assert(dithPopFireCnt < maxCnt)
//        //}
//      }
//      when (!concurVisib || ctrlIo.push.fire) {
//        ctrlPushFireCnt := 0
//      }
//      when (!concurVisib || dithIo.push.fire) {
//        dithPushFireCnt := 0
//      }
//      when (!concurVisib || dithIo.pop.fire) {
//        dithPopFireCnt := 0
//      }
//      //when (ctrlIo.push.fire) {
//      //  ctrlPushFireCnt := 0
//      //} otherwise {
//      //  ctrlPushFireCnt := ctrlPushFireCnt + 1
//      //}
//      //when (dithIo.push.fire) {
//      //  dithPushFireCnt := 0
//      //} otherwise {
//      //  dithPushFireCnt := dithPushFireCnt + 1
//      //}
//      //when (dithIo.pop.fire) {
//      //  dithPopFireCnt := 0
//      //} otherwise {
//      //  dithPopFireCnt := dithPopFireCnt + 1
//      //}
//      //assert(ctrlPushFireCnt < maxCnt)
//      //assert(dithPushFireCnt < maxCnt)
//      //assert(dithPopFireCnt < maxCnt)
//
//      //when (!cnt.msb) {
//        //switch (cnt) {
//        //  //val tempElem = arr(cnt)
//        //  for (idx <- 0 to arrSize - 1) {
//        //    is (idx) {
//        //      val tempElem = arr(idx)
//        //      //tempElem.ctrlPushValid := ctrlIo.push.valid
//        //      //tempElem.ctrlPushReady := ctrlIo.push.ready
//        //      //tempElem.ctrlPushFire := ctrlIo.push.fire
//        //      //tempElem.ctrlFifoEmpty := ctrlIo.misc.dbgFifoEmpty
//        //      //tempElem.ctrlFifoFull := ctrlIo.misc.dbgFifoFull
//        //      //tempElem.dithPushValid := dithIo.push.valid
//        //      //tempElem.dithPushReady := dithIo.push.ready
//        //      //tempElem.dithPushFire := dithIo.push.fire
//        //      //tempElem.dithPopValid := dithIo.pop.valid
//        //      //tempElem.dithPopReady := dithIo.pop.ready
//        //      //tempElem.dithPopFire := dithIo.pop.fire
//        //    }
//        //    //default {
//        //    //}
//        //  }
//        //  for (idx <- 0 to arrSize - 1) {
//        //    val dist = 6
//        //    if (idx >= dist) {
//        //      when (cnt >= dist && cnt === idx) {
//        //        val tempCtrlPushFire = for (
//        //          jidx <- (idx - dist + 1) to idx
//        //        )
//        //          yield arr(jidx).ctrlPushFire
//        //        val tempDithPushFire = for (
//        //          jidx <- (idx - dist + 1) to idx
//        //        )
//        //          yield arr(jidx).dithPushFire
//        //        val tempDithPopFire = for (
//        //          jidx <- (idx - dist + 1) to idx
//        //        )
//        //          yield arr(jidx).dithPopFire
//        //        assert(tempCtrlPushFire.reduceLeft((l, r) => (l || r)))
//        //        //assert(
//        //        //  //for (jidx <- (idx - 6 + 1) to idx) {
//        //        //  //  //val tempElem = arr(idx)
//        //        //  //}
//        //        //  //yield 
//
//        //        //)
//        //      }
//        //    }
//        //  }
//        //}
//        //ctrlPushValidArr(cnt) := ctrlIo.push.valid
//        //ctrlPushReadyArr(cnt) := ctrlIo.push.ready
//        //dithPushValidArr(cnt) := ctrlIo.push.valid
//        //dithPushReadyArr(cnt) := ctrlIo.push.ready
//        //dithPopValidArr(cnt) := ctrlIo.push.valid
//        //dithPopReadyArr(cnt) := ctrlIo.push.ready
//        //when ((cnt % 5) === 0 && cnt =/= 0) {
//        //  assert()
//        //}
//        //when (ctrlIo.dbgFifoFull) {
//        //}
//        //cnt := cnt + 1
//      //}
//      //when (cnt === 0x1) {
//      //  assert(dithIo.push.ready)
//      //}
//
//      //assert(dithIo.push.valid)
//    }
//  }
//  new SpinalFormalConfig(_keepDebugInfo=true)
//    //.withBMC(20)
//    //.withProve(20)
//    .withBMC(ctrlFifoDepth)
//    .withProve(20)
//    //.withCover(20)
//    .withCover(ctrlFifoDepth * 2)
//    .doVerify(
//      rtl=new Component {
//        val dut = FormalDut(Dut())
//
//        //val vgaCtrl = dut.vgaCtrl
//        //val vidDith = dut.vidDith
//
//        //--------
//        assumeInitial(clockDomain.isResetActive)
//        //--------
//      },
//      name="LcvVgaGradientFormal"
//    )
//}
