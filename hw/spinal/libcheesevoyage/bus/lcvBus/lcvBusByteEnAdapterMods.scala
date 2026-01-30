package libcheesevoyage.bus.lcvBus

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


case class CalcLcvBusH2dWrShiftedDataAndByteEnIoIo(
  busCfg: LcvBusConfig
) extends Bundle {
  require(!busCfg.haveByteEn)

  val h2dPayload = in(LcvBusH2dPayload(cfg=busCfg))
  val byteEn = out(UInt(busCfg.byteEnWidth bits))
  val shiftedData = out(UInt(busCfg.dataWidth bits))
}

case class CalcLcvBusH2dWrShiftedDataAndByteEn(
  busCfg: LcvBusConfig
) extends Component {
  val io = CalcLcvBusH2dWrShiftedDataAndByteEnIoIo(busCfg=busCfg)
  io.byteEn := U(io.byteEn.getWidth bits, default -> True)
  io.shiftedData := io.h2dPayload.data
  switch (
    io.h2dPayload.haveFullWord
    ## io.h2dPayload.byteSize
    ## io.h2dPayload.addr(busCfg.byteSizeWidth - 1 downto 0)
  ) {
    for (idx <- 0 until (1 << (busCfg.byteSizeWidth * 2))) {
      val myAddrLo = idx & ((1 << busCfg.byteSizeWidth) - 1)
      // myAddrLo represents the low bits of the address 
      val myByteSize = (
        (
          idx & (
            ((1 << busCfg.byteSizeWidth) - 1)
            << busCfg.byteSizeWidth
          )
        ) >> busCfg.byteSizeWidth
      )
      // myByteSize represents the value of `byteSize + 1`
      val myByteSizeMask = (
        (1 << (myByteSize + 1)) - 1
      )
      val myByteSizeShiftedMask = (
        myByteSizeMask << myAddrLo
      )
      println(
        s"idx:${idx} myByteSize:${myByteSize} myAddrLo:${myAddrLo} "
        + s"myByteSizeMask:${myByteSizeMask} "
        + s"myByteSizeShiftedMask:${myByteSizeShiftedMask}"
      )
      is (idx) {
        io.byteEn := (
          U(s"32'd${myByteSizeShiftedMask}")
          .resize(io.byteEn.getWidth)
        )
        ////switch (io.h2dPayload.addr(idx downto 0)) {
        ////  for (jdx <- 0 until busCfg.byteEnWidth) {
        ////  }
        ////}
        //val tempByteEn = cloneOf(io.byteEn)
        //tempByteEn := (1 << (idx + 1)) - 1
        //io.byteEn := Cat(
        //  tempByteEn,
        //  U(s"${idx}'d0")
        //).asUInt.resize(io.byteEn.getWidth)
        ////io.shiftedData := (
        ////  io.h2dPayload.data << (8 * idx)
        ////).resize(io.shiftedData.getWidth)

        //io.shiftedData := Cat(
        //  io.h2dPayload.data,
        //  U(s"${8 * idx}'d0")
        //).asUInt.resize(io.shiftedData.getWidth)
      }
    }
    default {
      //io.byteEn := 0x0
      io.byteEn := U(io.byteEn.getWidth bits, default -> True)
      //io.shiftedData := io.h2dPayload.data
    }
  }
  switch (
    io.h2dPayload.haveFullWord
    ## io.h2dPayload.addr(busCfg.byteSizeWidth - 1 downto 0)
  ) {
    for (idx <- 0 until (1 << busCfg.byteSizeWidth)) {
      val myAddrLo = idx & ((1 << busCfg.byteSizeWidth) - 1)
      // myAddrLo represents the low bits of the address 
      is (idx) {
        io.shiftedData := Cat(
          io.h2dPayload.data,
          U(s"${8 * idx}'d0")
        ).asUInt.resize(io.shiftedData.getWidth)
      }
    }
    default {
      io.shiftedData := io.h2dPayload.data
    }
  }
}

case class LcvBusH2dToWrByteEnStreamAdapterConfig(
  loBusCfg: LcvBusConfig
) {
  require(!loBusCfg.haveByteEn)
  val hiBusCfg = LcvBusConfig(
    mainCfg=loBusCfg.mainCfg.mkCopyWithByteEn(),
    cacheCfg=loBusCfg.cacheCfg
  )
}

case class LcvBusH2dToWrByteEnStreamAdapterIo(
  cfg: LcvBusH2dToWrByteEnStreamAdapterConfig
) extends Bundle {
  val loH2dBus = slave(Stream(LcvBusH2dPayload(cfg=cfg.loBusCfg)))
  val hiH2dBus = master(Stream(LcvBusH2dPayload(cfg=cfg.hiBusCfg)))
}

case class LcvBusH2dToWrByteEnStreamAdapter(
  cfg: LcvBusH2dToWrByteEnStreamAdapterConfig
) extends Component {
  //--------
  val io = LcvBusH2dToWrByteEnStreamAdapterIo(cfg=cfg)
  //--------
  val myCalc = CalcLcvBusH2dWrShiftedDataAndByteEn(busCfg=cfg.loBusCfg)
  //--------
  io.loH2dBus.translateInto(io.hiH2dBus)(
    dataAssignment=(outp, inp) => {
      myCalc.io.h2dPayload := inp
      //outp := inp
      outp.mainNonBurstInfo.infoShared := inp.mainNonBurstInfo.infoShared

      //outp.byteEn.allowOverride
      outp.data.allowOverride
      outp.byteEn := myCalc.io.byteEn
      outp.data := myCalc.io.shiftedData
      
      if (outp.mainBurstInfo != null) {
        outp.mainBurstInfo := inp.mainBurstInfo
      }
      if (outp.cacheInfo != null) {
        outp.cacheInfo := inp.cacheInfo
      }
    }
  )
}


//case class LcvBusByteEnAdapterConfig(
//  loBusCfg: LcvBusConfig,
//) {
//  require(!loBusCfg.haveByteEn)
//  val hiBusCfg = LcvBusConfig(
//    mainCfg=(
//      loBusCfg.mainCfg.mkCopyWithByteEn()
//      //if (!loBusCfg.haveByteEn) (
//      //  loBusCfg.mainCfg.mkCopyWithByteEn()
//      //) else (
//      //  loBusCfg.mainCfg.mkCopyWithoutByteEn()
//      //)
//    ),
//    cacheCfg=loBusCfg.cacheCfg
//  )
//}
//
//case class LcvBusByteEnAdapterIo(
//  cfg: LcvBusByteEnAdapterConfig
//) extends Bundle {
//  //val loBus = slave(LcvBusIo(cfg=cfg.loBusCfg))
//  //val hiBus = master(LcvBusIo(cfg=cfg.hiBusCfg))
//  val loH2dBus = slave(Stream(LcvBusH2dPayload(cfg=cfg.loBusCfg)))
//  val hiH2dBus = master(Stream(LcvBusH2dPayload(cfg=cfg.hiBusCfg)))
//}
//
////private[libcheesevoyage] case class LcvBusByteEnToNonByteEnAdapter(
////  cfg: LcvBusByteEnAdapterConfig
////) extends Component {
////  //--------
////  val io = LcvBusByteEnAdapterIo(cfg=cfg)
////  //--------
////}
//
////private[libcheesevoyage] case class LcvBusNonByteEnToByteEnAdapter(
////  cfg: LcvBusByteEnAdapterConfig
////) extends Component {
////  //--------
////  val io = LcvBusByteEnAdapterIo(cfg=cfg)
////  //--------
////}
//
//case class LcvBusNonBurstByteEnAdapter(
//  cfg: LcvBusByteEnAdapterConfig
//) extends Component {
//  //--------
//  val io = LcvBusByteEnAdapterIo(cfg=cfg)
//  //--------
//  object State
//  extends SpinalEnum(defaultEncoding=binaryOneHot) {
//    val
//      IDLE_OR_HAVE_FULL_WORD,
//      SMALL_READ_WAIT_LO_H2D_FIRE,
//      = newElement();
//  }
//  val rState = (
//    Reg(State())
//    init(State.IDLE_OR_HAVE_FULL_WORD)
//  )
//  switch (rState) {
//    when 
//  }
//}
