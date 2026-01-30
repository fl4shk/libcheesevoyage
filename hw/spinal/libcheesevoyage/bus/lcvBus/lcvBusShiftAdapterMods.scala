package libcheesevoyage.bus.lcvBus

import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


case class CalcLcvBusShiftedDataEtcIo(
  busCfg: LcvBusConfig,
  isWrite: Boolean,
) extends Bundle {
  //--------
  require(!busCfg.haveByteEn)
  //--------
  //val h2dPayload = in(LcvBusH2dPayload(cfg=busCfg))
  //val addr = in(UInt(busCfg.addrWidth bits))
  val addrLoBits = in(UInt(busCfg.byteSizeWidth bits))
  val data = in(UInt(busCfg.dataWidth bits))
  val byteSize = in(UInt(busCfg.byteSizeWidth bits))

  val byteEn = (
    isWrite
  ) generate (
    out(UInt(busCfg.byteEnWidth bits))
  )
  val shiftedData = out(UInt(busCfg.dataWidth bits))
  //--------
}

case class CalcLcvBusShiftedDataEtc(
  busCfg: LcvBusConfig,
  isWrite: Boolean,
) extends Component {
  val io = CalcLcvBusShiftedDataEtcIo(
    busCfg=busCfg,
    isWrite=isWrite
  )
  //io.byteEn := U(io.byteEn.getWidth bits, default -> True)
  //io.shiftedData := io.data
  val myWriteArea = (
    isWrite
  ) generate (new Area {
    switch (
      //io.haveFullWord
      //## 
      io.byteSize
      //## io.addr(busCfg.byteSizeWidth - 1 downto 0)
      ## io.addrLoBits
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
        val tempByteSizeMask = (
          //(1 << (myByteSize + 1)) - 1
          ((1 << (myByteSize + 3)) / 8).toInt
        )
        val myByteSizeMask = (
          tempByteSizeMask | ((1 << tempByteSizeMask) - 1)
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
        }
      }
      //default {
      //  //io.byteEn := 0x0
      //  io.byteEn := U(io.byteEn.getWidth bits, default -> True)
      //  //io.shiftedData := io.data
      //}
    }
    switch (
      //io.haveFullWord
      //## 
      //io.addr(busCfg.byteSizeWidth - 1 downto 0)
      io.addrLoBits
    ) {
      for (idx <- 0 until (1 << busCfg.byteSizeWidth)) {
        val myAddrLo = idx & ((1 << busCfg.byteSizeWidth) - 1)
        // myAddrLo represents the low bits of the address 
        is (idx) {
          io.shiftedData := Cat(
            io.data,
            U(s"${8 * idx}'d0")
          ).asUInt.resize(io.shiftedData.getWidth)
        }
      }
      //default {
      //  io.shiftedData := io.data
      //}
    }
  })
  val myReadArea = (
    !isWrite
  ) generate (new Area {
    switch (
      io.byteSize
      ## io.addrLoBits
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
      }
    }
  })
}

case class LcvBusH2dShiftedDataEtcStreamAdapterConfig(
  loBusCfg: LcvBusConfig,
) {
  //--------
  require(!loBusCfg.haveByteEn)
  //--------
  val hiBusCfg = LcvBusConfig(
    mainCfg=loBusCfg.mainCfg.mkCopyWithByteEn(Some(true)),
    cacheCfg=loBusCfg.cacheCfg
  )
  //--------
}

case class LcvBusH2dShiftedDataEtcStreamAdapterIo(
  cfg: LcvBusH2dShiftedDataEtcStreamAdapterConfig
) extends Bundle {
  val loH2dBus = slave(Stream(LcvBusH2dPayload(cfg=cfg.loBusCfg)))
  val hiH2dBus = master(Stream(LcvBusH2dPayload(cfg=cfg.hiBusCfg)))
}

case class LcvBusH2dShiftedDataEtcStreamAdapter(
  cfg: LcvBusH2dShiftedDataEtcStreamAdapterConfig
) extends Component {
  //--------
  val io = LcvBusH2dShiftedDataEtcStreamAdapterIo(cfg=cfg)
  //--------
  val myCalc = CalcLcvBusShiftedDataEtc(
    busCfg=cfg.loBusCfg,
    isWrite=true,
  )
  //--------
  io.loH2dBus.translateInto(io.hiH2dBus)(
    dataAssignment=(outp, inp) => {
      //myCalc.io.h2dPayload := inp
      myCalc.io.addrLoBits := inp.addr(
        cfg.loBusCfg.byteSizeWidth - 1 downto 0
      )
      myCalc.io.data := inp.data
      myCalc.io.byteSize := inp.byteSize
      //outp := inp
      outp.mainNonBurstInfo.infoShared := inp.mainNonBurstInfo.infoShared
      outp.mainNonBurstInfo.infoByteSizeEtc := (
        inp.mainNonBurstInfo.infoByteSizeEtc
      )

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

case class LcvBusD2hShiftedDataEtcStreamAdapterConfig(
  busCfg: LcvBusConfig,
) {
  //--------
  require(!busCfg.haveByteEn)
  //--------
}

case class LcvBusD2hShiftedDataEtcStreamAdapterIo(
  cfg: LcvBusD2hShiftedDataEtcStreamAdapterConfig
) extends Bundle {
  //val addrLoBits = in(UInt(cfg.busCfg.byteSizeWidth bits))
  val addr = in(UInt(cfg.busCfg.addrWidth bits))
  val byteSize = in(UInt(cfg.busCfg.byteSizeWidth bits))
  val loD2hBus = slave(Stream(LcvBusD2hPayload(cfg=cfg.busCfg)))
  val hiD2hBus = master(Stream(LcvBusD2hPayload(cfg=cfg.busCfg)))
}

case class LcvBusD2hShiftedDataEtcStreamAdapter(
  cfg: LcvBusD2hShiftedDataEtcStreamAdapterConfig
) extends Component {
  //--------
  val io = LcvBusD2hShiftedDataEtcStreamAdapterIo(cfg=cfg)
  //--------
  val myCalc = CalcLcvBusShiftedDataEtc(
    busCfg=cfg.busCfg,
    isWrite=false,
  )
  //--------
  io.loD2hBus.translateInto(io.hiD2hBus)(
    dataAssignment=(outp, inp) => {
      //myCalc.io.h2dPayload := inp
      myCalc.io.addrLoBits := io.addr(
        //inp.addr
        cfg.busCfg.byteSizeWidth - 1 downto 0
      )
      myCalc.io.data := inp.data
      myCalc.io.byteSize := io.byteSize //inp.byteSize
      //outp := inp
      outp.mainNonBurstInfo.infoShared := inp.mainNonBurstInfo.infoShared

      //outp.byteEn.allowOverride
      outp.data.allowOverride
      //outp.byteEn := myCalc.io.byteEn
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

//case class LcvBusRdDataShiftIo(
//  busCfg: LcvBusConfig
//) extends Bundle {
//  //--------
//  require(!busCfg.haveByteEn)
//  //--------
//  val addr 
//  //--------
//}


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
