package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


case class LcvBusDataWidthAdapterConfig(
  //--------
  loBusMainCfg: LcvBusMainConfig,
  hiBusDataWidth: Int,
  //--------
  loBusCacheCfg: Option[LcvBusCacheConfig],
  hiBusCacheCfg: Option[LcvBusCacheConfig],
  //--------
) {
  val loBusCfg = LcvBusConfig(
    mainCfg=loBusMainCfg,
    cacheCfg=loBusCacheCfg
  )
  val hiBusCfg = LcvBusConfig(
    mainCfg=loBusMainCfg.mkCopyWithNewDataWidth(dataWidth=hiBusDataWidth),
    cacheCfg=hiBusCacheCfg,
  )
  require(
    loBusCfg.dataWidth
    != hiBusCfg.dataWidth
  )
}

case class LcvBusDataWidthAdapterIo(
  cfg: LcvBusDataWidthAdapterConfig
) extends Bundle {
  //--------
  val loBus = slave(
    LcvBusIo(cfg=cfg.loBusCfg)
  )
  val hiBus = master(
    LcvBusIo(cfg=cfg.hiBusCfg)
  )
  //--------
}

case class LcvBusSimpleBurstOnlyDataWidthDownAdapter(
  cfg: LcvBusDataWidthAdapterConfig
) extends Component {
  //--------
  val io = LcvBusDataWidthAdapterIo(cfg=cfg)
  //--------
  require(
    cfg.loBusCfg.dataWidth > cfg.hiBusCfg.dataWidth
  )
  def myDataWidthRatio = (
    (cfg.loBusCfg.dataWidth / cfg.hiBusCfg.dataWidth).toInt
  )
  require(
    myDataWidthRatio == 2
  )
  //--------
  io.loBus.h2dBus.ready := False
  io.loBus.d2hBus.valid := False
  io.loBus.d2hBus.payload := (
    io.loBus.d2hBus.payload.getZero
  )
  io.loBus.d2hBus.data.allowOverride
  io.loBus.d2hBus.data := (
    RegNext(io.loBus.d2hBus.data)
  )

  io.hiBus.d2hBus.ready := False
  io.hiBus.h2dBus.valid := False
  io.hiBus.h2dBus.payload := (
    io.hiBus.h2dBus.payload.getZero
  )
  //--------
  object State
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      //READ_NON_BURST,
      //WRITE_NON_BURST,
      READ_BURST,
      WRITE_BURST
      //START_HI_READ_BURST,
      = newElement()
  }
  val rState = (
    Reg(State())
    init(State.IDLE)
  )
  //--------
  val rSavedLoH2dPayload = (
    Reg(cloneOf(io.loBus.h2dBus.payload))
    init(io.loBus.h2dBus.payload.getZero)
  )
  val rSavedHiH2dBurstCnt = (
    Reg(cloneOf(io.hiBus.h2dBus.burstCnt))
    init(io.hiBus.h2dBus.burstCnt.getZero)
  )

  val hiH2dFifo = (
    StreamFifo(
      dataType=cloneOf(io.hiBus.h2dBus.payload),
      depth=(
        1 << cfg.hiBusCfg.burstCntWidth
      ),
      latency=(
        2
      ),
      forFMax=true,
    )
  )
  //io.hiBus.h2dBus << hiH2dFifo.io.pop
  hiH2dFifo.io.push.valid := False
  hiH2dFifo.io.push.payload := hiH2dFifo.io.push.payload.getZero
  hiH2dFifo.io.pop.ready := False

  val hiD2hFifo = (
    StreamFifo(
      dataType=cloneOf(io.hiBus.d2hBus.payload),
      depth=(
        1 << cfg.hiBusCfg.burstCntWidth
      ),
      latency=(
        2
      ),
      forFMax=true,
    )
  )

  //hiD2hFifo.io.push << io.hiBus.d2hBus
  hiD2hFifo.io.push.valid := False
  hiD2hFifo.io.push.payload := hiD2hFifo.io.push.payload.getZero
  hiD2hFifo.io.pop.ready := False

  val rSeenLoH2dFire = Reg(Bool())
  val rSeenLoD2hFire = Reg(Bool())
  val rSeenHiH2dFire = Reg(Bool())
  val rSeenHiD2hFire = Reg(Bool())

  val rLoBurstCnt = Reg(UInt(cfg.loBusCfg.burstCntWidth bits))
  val rHiBurstCnt = Reg(UInt(cfg.hiBusCfg.burstCntWidth bits))

  switch (rState) {
    is (State.IDLE) {
      rSavedLoH2dPayload := io.loBus.h2dBus.payload
      rSavedHiH2dBurstCnt := (
        (
          (io.loBus.h2dBus.burstCnt + 1) << log2Up(myDataWidthRatio)
        )
        - 1
      )

      rSeenLoH2dFire := False
      rSeenLoD2hFire := False
      rSeenHiH2dFire := False
      rSeenHiD2hFire := False
      rLoBurstCnt := (1 << cfg.loBusCfg.burstCntWidth) - 1
      rHiBurstCnt := (1 << cfg.hiBusCfg.burstCntWidth) - 1

      //when (
      //  io.loBus.h2dBus.valid
      //) {
      //  rState := State.READ_BURST
      //}
      switch (
        io.loBus.h2dBus.valid
        ## io.loBus.h2dBus.isWrite
      ) {
        is (M"10") {
          io.loBus.h2dBus.ready := True
          rState := State.READ_BURST
        }
        is (M"11") {
          rState := State.WRITE_BURST
        }
        default {
        }
      }
    }

    is (State.READ_BURST) {
      hiD2hFifo.io.push << io.hiBus.d2hBus
      io.hiBus.h2dBus.burstFirst := rSavedLoH2dPayload.burstFirst
      io.hiBus.h2dBus.burstLast := rSavedLoH2dPayload.burstLast
      io.hiBus.h2dBus.burstCnt := rSavedHiH2dBurstCnt
      io.hiBus.h2dBus.isWrite := False
      io.hiBus.h2dBus.data := 0x0
      io.hiBus.h2dBus.addr := rSavedLoH2dPayload.addr
      io.hiBus.h2dBus.src := rSavedLoH2dPayload.src
      if (io.hiBus.h2dBus.mainNonBurstInfo.infoByteEn != null) {
        io.hiBus.h2dBus.byteEn := 0x0//rSavedLoH2dPayload.byteEn
      }
      if (io.hiBus.h2dBus.mainNonBurstInfo.infoByteSizeEtc != null) {
        io.hiBus.h2dBus.byteSize := rSavedLoH2dPayload.byteSize
      }

      when (!rSeenHiH2dFire) {
        io.hiBus.h2dBus.valid := True
      }
      when (io.hiBus.h2dBus.ready) {
        rSeenHiH2dFire := True
      }

      val myTempMaybeThrownD2hStmVec = Vec.fill(2)(
        cloneOf(io.loBus.d2hBus)
      )

      hiD2hFifo.io.pop.translateInto(
        myTempMaybeThrownD2hStmVec.head
      )(
        dataAssignment=(outp, inp) => {
          outp.mainBurstInfo := outp.mainBurstInfo.getZero
          outp.src := inp.src
          outp.data.allowOverride
          outp.data := RegNext(outp.data)
          if (outp.mainNonBurstInfo.infoByteSizeEtc != null) {
            outp.mainNonBurstInfo.infoByteSizeEtc := (
              inp.mainNonBurstInfo.infoByteSizeEtc
            )
          }

          switch (rHiBurstCnt.lsb) {
            is (True) {
              outp.data(
                cfg.hiBusCfg.dataWidth - 1
                downto 0
              ) := (
                inp.data
              )
            }
            is (False) {
              outp.data(
                cfg.loBusCfg.dataWidth - 1
                downto cfg.hiBusCfg.dataWidth
              ) := (
                inp.data
              )
            }
          }
        }
      )

      when (hiD2hFifo.io.pop.fire) {
        rHiBurstCnt := rHiBurstCnt - 1
      }

      myTempMaybeThrownD2hStmVec.last <-/< (
        myTempMaybeThrownD2hStmVec.head.throwWhen(
          rHiBurstCnt.lsb
        )
      )
      myTempMaybeThrownD2hStmVec.last.translateInto(io.loBus.d2hBus)(
        dataAssignment=(outp, inp) => {
          outp := inp
          outp.mainBurstInfo.allowOverride
          outp.burstFirst := rLoBurstCnt.andR
          outp.burstLast := !rLoBurstCnt.orR
          outp.burstCnt := rLoBurstCnt
        }
      )

      when (io.loBus.d2hBus.fire) {
        rLoBurstCnt := rLoBurstCnt - 1
      }

      when (
        io.loBus.d2hBus.fire
        && !rLoBurstCnt.orR
      ) {
        rState := State.IDLE
      }
    }

    is (State.WRITE_BURST) {
      io.hiBus.h2dBus << hiH2dFifo.io.pop

      val myPushStmVec = Vec.fill(2)(
        cloneOf(io.loBus.h2dBus)
      )

      //hiH2dFifo.io.push <-/< myPushStmVec.last
      myPushStmVec.head <-/< io.loBus.h2dBus.haltWhen(
        //rSeenHiH2dFire
        rSeenLoH2dFire
        && rLoBurstCnt.andR
      )
      when (io.loBus.h2dBus.fire) {
        rSeenLoH2dFire := True
        rLoBurstCnt := rLoBurstCnt - 1
      }

      myPushStmVec.last <-/< myPushStmVec.head.repeat(
        times=myDataWidthRatio
      )._1
      myPushStmVec.last.translateInto(
        hiH2dFifo.io.push
      )(
        dataAssignment=(outp, inp) => {
          outp.burstFirst := !rSeenHiH2dFire
          outp.burstLast := !rHiBurstCnt.orR
          outp.burstCnt := rSavedHiH2dBurstCnt
          outp.isWrite := True
          //outp.addr := inp.addr
          //outp.addr := inp.addr(
          //  outp.addr
          //)

          outp.addr(
            outp.addr.high
            downto cfg.loBusCfg.addrLoWidth //rHiBurstCnt.getWidth
          ) := (
            inp.addr(
              outp.addr.high
              downto cfg.loBusCfg.addrLoWidth //rHiBurstCnt.getWidth
            )
          )
          outp.addr(cfg.loBusCfg.addrLoWidth - 1) := (
            !rHiBurstCnt.lsb
          )
          //if (cfg.hiBusCfg.addrLoWidth > 1) {
          //  outp.addr(cfg.hiBusCfg.addrLoWidth - 2 downto 0) := 0x0
          //}

          switch (rHiBurstCnt.lsb) {
            is (True) {
              if (outp.mainNonBurstInfo.infoByteEn != null) {
                outp.byteEn := inp.byteEn(
                  outp.byteEn.getWidth - 1
                  downto 0
                )
              }
              //inp.addr
              //outp.addr := rSavedLoH2dPayload.burstAddr(
              //)
              outp.data := inp.data(
                cfg.hiBusCfg.dataWidth - 1
                downto 0
              )
            }
            is (False) {
              if (outp.mainNonBurstInfo.infoByteEn != null) {
                outp.byteEn := inp.byteEn(
                  inp.byteEn.getWidth - 1
                  downto outp.byteEn.getWidth
                )
              }
              outp.data := inp.data(
                cfg.loBusCfg.dataWidth - 1
                downto cfg.hiBusCfg.dataWidth
              )
            }
          }
          outp.src := inp.src
          //if (outp.mainNonBurstInfo.infoByteEn != null) {
          //  outp.byteEn := inp.byteEn//0x0//rSavedLoH2dPayload.byteEn
          //}
          if (outp.mainNonBurstInfo.infoByteSizeEtc != null) {
            outp.byteSize := inp.byteSize//rSavedLoH2dPayload.byteSize
          }
        }
      )

      when (
        !rSeenHiH2dFire
        && hiH2dFifo.io.push.fire
      ) {
        rSeenHiH2dFire := True
      }

      when (hiH2dFifo.io.push.fire) {
        rHiBurstCnt := rHiBurstCnt - 1
      }

      //when (
      //  !rSeenLoD2hFire
      //) {
      //}
      //io.loBus.d2hBus << io.hiBus.d2hBus
      io.hiBus.d2hBus.translateInto(io.loBus.d2hBus)(
        dataAssignment=(outp, inp) => {
          outp.burstFirst := inp.burstFirst
          outp.burstLast := inp.burstLast
          outp.burstCnt := 0x0
          outp.data := 0x0
          outp.src := inp.src
        }
      )

      when (io.loBus.d2hBus.fire) {
        rSeenLoD2hFire := True
      }

      when (
        //io.loBus.d2hBus.fire
        //&& !rLoBurstCnt.orR
        rSeenLoD2hFire
        //&& rSeenHiH2dFire
        && !hiH2dFifo.io.pop.valid
        && rHiBurstCnt.andR
      ) {
        rState := State.IDLE
      }
    }
  }
  //--------
}
