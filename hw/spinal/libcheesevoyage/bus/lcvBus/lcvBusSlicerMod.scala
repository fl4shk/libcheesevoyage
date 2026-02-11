package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

case class LcvBusSlicerConfig(
  //busCfg: LcvBusConfig,
  //addrSliceWidth: Int,
  mmapCfg: LcvBusMemMapConfig,
  //addrSliceStart: Int,
  //addrSliceEnd: Int,
  //optNumDevs: Option[Int]=None,
  maxNumOutstandingTxns: Int=8,
) {
  def busCfg = mmapCfg.busCfg
  def numDevs = mmapCfg.addrSliceSize
  def addrSliceStart = mmapCfg.addrSliceLo
  def addrSliceEnd = mmapCfg.addrSliceHi
  def addrSliceRange = mmapCfg.addrSliceRange
}

case class LcvBusSlicerIo(
  cfg: LcvBusSlicerConfig,
) extends Bundle {
  val host = slave(LcvBusIo(cfg=cfg.busCfg))
  val devVec = (
    Vec[LcvBusIo]{
      val tempArr = new ArrayBuffer[LcvBusIo]()
      for (idx <- 0 until cfg.numDevs) {
        tempArr += LcvBusIo(cfg=cfg.busCfg)
      }
      tempArr
    }
  )
  for (dev <- devVec.view) {
    master(dev)
  }
}
case class LcvBusSlicer(
  cfg: LcvBusSlicerConfig,
) extends Component {
  require(
    !cfg.busCfg.allowBurst,
    s"`LcvBusSlicer` doesn't support bursts "
    + s"(at least as of this writing), "
    + s"so perhaps you could try `LcvBusDeburster`."
  )
  val io = LcvBusSlicerIo(cfg=cfg)

  io.host.h2dBus.ready := False
  io.host.d2hBus.valid := False
  io.host.d2hBus.payload := io.host.d2hBus.payload.getZero

  for (
    //devIdx <- 0 until cfg.numDevs
    dev <- io.devVec
  ) {
    //io.host.h2dBus.ready := False
    //io.host.d2hBus.valid := False
    //io.host.d2hBus.payload := (
    //  io.host.d2hBus.payload.getZero
    //)

    //val dev.h2dBus = io.devVec(devIdx).h2dBus
    //val dev.d2hBus = io.devVec(devIdx).d2hBus
    dev.h2dBus.valid := False
    dev.h2dBus.payload := (
      dev.h2dBus.payload.getZero
    )
    dev.d2hBus.ready := False
  }
  //val stickyH2dAddrSlice = (
  //  UInt(cfg.mmapCfg.addrSliceWidth bits)
  //)
  //stickyH2dAddrSlice := (
  //  RegNext(stickyH2dAddrSlice, init=stickyH2dAddrSlice.getZero)
  //)
  //when (io.host.h2dBus.valid) {
  //  stickyH2dAddrSlice := (
  //    io.host.h2dBus.payload.addr(cfg.addrSliceRange)
  //  )
  //}
  val rSavedH2dAddrSlice = (
    Reg(UInt(cfg.mmapCfg.addrSliceWidth bits))
    init(0x0)
  )

  val rTxnCnt = (
    Reg(UInt(log2Up(cfg.maxNumOutstandingTxns + 1) bits))
    init(0x0)
  )

  object State
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      START_NEW_ADDR_SLICE,
      SAME_ADDR_SLICE,
      CHANGED_ADDR_SLICE_WAIT_REMAINING_D2H_RESPONSES
      = newElement();
  }
  val rState = (
    Reg(State())
    init(State.START_NEW_ADDR_SLICE)
  )

  switch (
    (
      rState === State.SAME_ADDR_SLICE
      && io.host.h2dBus.valid
    )
    ## (rSavedH2dAddrSlice === io.host.h2dBus.addr(cfg.addrSliceRange))
    ## rSavedH2dAddrSlice
  ) {
    for (devIdx <- 0 until cfg.numDevs) {
      is (
        //M"11"
        //MaskedLiteral(
        //  str={
        //    var temp: String = "11"
        //    for (idx <- 0 until rSavedH2dAddrSlice.getWidth) {
        //      temp += "-"
        //    }
        //    temp
        //  }
        //)
        B"11"
        ## U(s"${rSavedH2dAddrSlice.getWidth}'d${devIdx}")
      ) {
        //val dev.h2dBus = io.devVec(devIdx).h2dBus
        def dev = io.devVec(devIdx)
        dev.h2dBus << io.host.h2dBus

        //val dev.d2hBus = io.devVec(devIdx).d2hBus
        io.host.d2hBus << dev.d2hBus
      }
    }
    is (
      //M"110"
      MaskedLiteral(
        str={
          var temp: String = "10"
          for (idx <- 0 until rSavedH2dAddrSlice.getWidth) {
            temp += "-"
          }
          temp
        }
      )
    ) {
    }
    default {
    }
  }
  switch (rState) {
    is (State.START_NEW_ADDR_SLICE) {
      when (io.host.h2dBus.valid) {
        rSavedH2dAddrSlice := io.host.h2dBus.addr(cfg.addrSliceRange)
      }
    }
    is (State.SAME_ADDR_SLICE) {
      //switch (
      //  rSavedH2dAddrSlice === io.host.h2dBus.addr(cfg.addrSliceRange)
      //) {
      //}
    }
    is (State.CHANGED_ADDR_SLICE_WAIT_REMAINING_D2H_RESPONSES) {
      //switch (
      //  //io.devVec()
      //  ## (rTxnCnt > 0)
      //) {
      //}
      //when (rTxnCnt === 0) {
      //}
    }
  }
  //when (io.host.h2dBus.valid) {
  //  switch (
  //    //io.host.h2dBus.payload.addr(cfg.addrSliceRange)
  //    stickyH2dAddrSlice
  //  ) {
  //    for (devIdx <- 0 until cfg.numDevs) {
  //      is (devIdx) {
  //        //val dev.h2dBus = io.devVec(devIdx).h2dBus
  //        def dev = io.devVec(devIdx)
  //        dev.h2dBus << io.host.h2dBus

  //        //val dev.d2hBus = io.devVec(devIdx).d2hBus
  //        io.host.d2hBus << dev.d2hBus
  //      }
  //    }
  //  }
  //}
}
//case class LcvBusSlicer(
//  cfg: LcvBusSlicerConfig,
//) extends Component {
//  val myBusSlicerImpl = LcvBusSlicerImpl(cfg=cfg)
//  val myDeburster = (
//    cfg.busCfg.allowBurst
//  ) generate (LcvBusDeburster(cfg=LcvBusDebursterConfig(
//    loBusCfg=cfg.busCfg
//  )))
//  if (cfg.busCfg.allowBurst) {
//    io.bus <> myDeburster.io.loBus
//    //myDeburster.io.hiBus <> myMemImpl.io.bus
//    myDeburster.io.hiBus.h2dBus.translateInto(
//      myMemImpl.io.bus.h2dBus
//    )(
//      dataAssignment=(
//        outp, inp
//      ) => {
//        outp.mainNonBurstInfo := inp.mainNonBurstInfo
//        outp.mainBurstInfo := outp.mainBurstInfo.getZero
//      }
//    )
//    myMemImpl.io.bus.d2hBus.translateInto(
//      myDeburster.io.hiBus.d2hBus
//    )(
//      dataAssignment=(
//        outp, inp
//      ) => {
//        outp.mainNonBurstInfo := inp.mainNonBurstInfo
//      }
//    )
//  } else {
//    io.bus <> myMemImpl.io.bus
//  }
//}
