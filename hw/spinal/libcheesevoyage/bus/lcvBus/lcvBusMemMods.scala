package libcheesevoyage.bus.lcvBus

import scala.collection.immutable
import scala.collection.mutable._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._


import libcheesevoyage.general.RamSdpPipeConfig
import libcheesevoyage.general.RamSdpPipeIo
import libcheesevoyage.general.RamSdpPipe

// A bus-attached BRAM, LUTRAM, etc.
case class LcvBusMemConfig(
  busCfg: LcvBusConfig,
  //mmapCfg: LcvBusMemMapConfig,
  depth: Int,
  init: Option[Seq[Bits]]=None,
  initBigInt: Option[Seq[BigInt]]=None,
  arrRamStyleAltera: String="no_rw_check, M10K",
  arrRamStyleXilinx: String="block",
  arrRwAddrCollisionXilinx: String="",
  busD2hFifoLatency: Int=2,
) {
  val ramCfg = RamSdpPipeConfig(
    wordType=Bits(busCfg.dataWidth bits),
    depth=depth,
    optIncludeWrByteEn=true,
    init=init,
    initBigInt=initBigInt,
    arrRamStyleAltera=arrRamStyleAltera,
    arrRamStyleXilinx=arrRamStyleXilinx,
    arrRwAddrCollisionXilinx=arrRwAddrCollisionXilinx,
  )
  //def busCfg = mmapCfg.busCfg
  //def depth = mmapCfg.addrSliceSize
}

case class LcvBusMemIo(
  cfg: LcvBusMemConfig,
) extends Bundle {
  val bus = slave(LcvBusIo(cfg=cfg.busCfg))
}

private[libcheesevoyage] case class LcvBusMemImpl(
  cfg: LcvBusMemConfig,
  //io: LcvBusMemIo
) extends Component {
  //--------
  def busCfg = cfg.busCfg
  //require(!busCfg.allowBurst)
  //--------
  val io = LcvBusMemIo(cfg=cfg)
  //--------
  val ram = RamSdpPipe(
    cfg=cfg.ramCfg
  )
  //--------
  val rdLineWord = UInt(busCfg.dataWidth bits)
  rdLineWord := ram.io.rdData.asUInt

  ram.io.wrEn := False

  val myH2dDoStallFifoThing = LcvBusDoStallFifoThing(
    busCfg=busCfg,
  )
  //myH2dDoStallFifoThing.io.push << io.bus.h2dBus
  io.bus.h2dBus.translateInto(
    myH2dDoStallFifoThing.io.push
  )(
    dataAssignment=(
      outp, inp
    ) => {
      outp.cnt := (
        (
          RegNextWhen(
            (outp.cnt.asSInt + 1),
            cond=myH2dDoStallFifoThing.io.push.fire,
          )
          init(-2)
        ).asUInt
      )
      outp.busPayload := inp
    }
  )
  //myH2dDoStallFifoThing.io.push << 
  //myH2dDoStallFifoThing.io.doStallCacheMiss := False
  val myFifoThingDoStall = (
    //Reg(Bool(), init=False)
    //Vec.fill(2)(
    //  Bool()
    //)
    //Vec[Bool]{
    //  List(
    //    Bool(),
    //    //Reg(Bool(), init=False)
    //    Bool()
    //  )
    //}
    Bool()
  )
  myFifoThingDoStall := (
    RegNext(myFifoThingDoStall, init=myFifoThingDoStall.getZero)
  )
  //myFifoThingDoStall.head := (
  //  RegNext(myFifoThingDoStall.head, init=False)
  //)
  //myFifoThingDoStall.last := (
  //  RegNext(myFifoThingDoStall.last, init=False)
  //)
  //myFifoThingDoStall := RegNext(myFifoThingDoStall, init=False)
  //myH2dDoStallFifoThing.io.doStallCacheMiss := myFifoThingDoStall.head
  //myH2dDoStallFifoThing.io.doStallNotYetD2hFire := myFifoThingDoStall.last
  //myH2dDoStallFifoThing.io.doStall := myFifoThingDoStall
  //myH2dDoStallFifoThing.io.doStall.head := myFifoThingDoStall.head
  //myH2dDoStallFifoThing.io.doStall.last := myFifoThingDoStall.last
  myH2dDoStallFifoThing.io.doStall := myFifoThingDoStall
  //rFifoThingCacheMiss := False


  val myH2dPopStm = (
    //myH2dDoStallFifoThing.io.pop
    cloneOf(myH2dDoStallFifoThing.io.pop)
  )
  def myH2dPopPayload = (
    myH2dPopStm.busPayload
  )

  //def myH2dPopStm = io.bus.h2dBus
  //def myD2hPushStm = io.bus.d2hBus
  myH2dPopStm.ready := False

  val myH2dPopThrowArea = new Area {
    val myH2dThrowCond = Bool()
    val myTempH2dPopStm = myH2dDoStallFifoThing.io.pop.throwWhen(
      myH2dThrowCond
    )
    myH2dThrowCond := False
    //myH2dPopStm.ready := False
    myH2dPopStm << myTempH2dPopStm
  }

  val rH2dPayload = (
    RegNextWhen(
      myH2dPopStm.payload,
      cond=myH2dPopStm.fire,
      init=myH2dPopStm.payload.getZero,
    )
  )
  def rBusAddr = rH2dPayload.busPayload.addr
  val rDel2H2dPayload = (
    RegNext/*When*/(
      next=rH2dPayload,
      //cond=myH2dPopStm.fire,
      init=rH2dPayload.getZero,
    )
  )
  def rDel2BusAddr = rDel2H2dPayload.busPayload.addr

  val rSavedH2dPayload = (
    Reg(cloneOf(rH2dPayload))
    init(rH2dPayload.getZero)
  )
  def rSavedBusAddr = rSavedH2dPayload.busPayload.addr

  val myD2hShiftedDataStmAdapter = (
    !cfg.busCfg.haveByteEn
  ) generate (
    LcvBusD2hShiftedDataEtcStreamAdapter(
      cfg=LcvBusD2hShiftedDataEtcStreamAdapterConfig(busCfg=cfg.busCfg)
    )
  )
  val myD2hPushStm = Stream(
    LcvBusDoStallFifoThingPayload(
      LcvBusD2hPayload(cfg=cfg.busCfg),
      //optByteEnWidth=None,
    )
  )

  myD2hPushStm.valid := False

  //val myD2hPushStm = Stream(
  //  LcvBusDoStallFifoThingPayload(
  //    LcvBusD2hPayload(cfg=busCfg),
  //  )
  //)
  val myD2hFifo = StreamFifo(
    dataType=(
      cloneOf(io.bus.d2hBus.payload)
    ),
    depth=(
      busCfg.maxBurstSizeMinus1 + 1
    ),
    latency=cfg.busD2hFifoLatency,
    forFMax=true,
  )
  io.bus.d2hBus << myD2hFifo.io.pop 
  myD2hPushStm.translateInto(
    if (cfg.busCfg.haveByteEn) (
      //io.bus.d2hBus
      myD2hFifo.io.push
    ) else (
      myD2hShiftedDataStmAdapter.io.loD2hBus
    )
  )(
    dataAssignment=(outp, inp) => {
      outp := inp.busPayload
    }
  )

  if (!cfg.busCfg.haveByteEn) {
    //myD2hShiftedDataStmAdapter.io.byteSize := (
    //  rDel2H2dPayload.busPayload.byteSize
    //)
    //myD2hShiftedDataStmAdapter.io.addr := (
    //  rDel2H2dPayload.busPayload.addr
    //)
    //io.bus.d2hBus << myD2hShiftedDataStmAdapter.io.hiD2hBus
    myD2hFifo.io.push << myD2hShiftedDataStmAdapter.io.hiD2hBus
  }

  //myD2hPushStm.translateInto(
  //  //io.bus.d2hBus
  //  //io.bus.d2hBus
  //  myD2hFifo.io.push
  //)(
  //  dataAssignment=(
  //    outp, inp
  //  ) => {
  //    outp := inp.busPayload
  //  }
  //)


  def myRamAddrRshift = log2Up(busCfg.dataWidth / 8)
  def doRamReadSync(
    busAddr: UInt,
    setEn: Int=0,
  ): Unit = {
    if (setEn == 1) {
      ram.io.rdEn := True
    } else if (setEn == 2) {
      ram.io.rdEn := (
        //True
        RegNext(
          next=(
            //myH2dPopStm.valid
            myH2dPopStm.fire
          ),
          init=False,
        )
      )
    } 
    //rMyRamRdEn := True
    ram.io.rdAddr := {
      //(busAddr >> myRamAddrRshift)
      println(
        s"test info: busAddr("
        + s"${busAddr.high} downto ${myRamAddrRshift}"
        + s")"
      )
      (
        (
          busAddr(busAddr.high downto myRamAddrRshift)
        )
        .resize(ram.io.rdAddr.getWidth)
      )
    }
  }
  def doRamWrite(
    busAddr: UInt,
    lineWord: UInt,
    byteEn: Option[UInt],
    setEn: Boolean=true,
  ): Unit = {
    if (setEn) {
      ram.io.wrEn := True
    }
    ram.io.wrAddr := (
      //(busAddr >> myRamAddrRshift)
      (busAddr(busAddr.high downto myRamAddrRshift))
      .resize(ram.io.wrAddr.getWidth)
    )
    ram.io.wrData := lineWord.asBits
    //lineWord match {
    //  case Some(lineWord) => {
    //    ram.io.wrData := lineWord.asBits
    //  }
    //  case None => {
    //    ram.io.wrData := (
    //      //myD2hBus.sendData.data.asBits
    //      io.hiBus.d2hBus.data
    //    )
    //  }
    //}
    byteEn match {
      case Some(byteEn) => {
        ram.io.wrByteEn := byteEn.asBits
      }
      case None => {
        ram.io.wrByteEn := (
          B(ram.io.wrByteEn.getWidth bits, default -> True)
        )
      }
    }
  }
  doRamReadSync(
    busAddr={
      //rH2dPayload.addr
      //println(
      //  s"testificate: ${io.bus.h2dBus.addr.bitsRange}"
      //)
      //io.bus.h2dBus.addr
      myH2dPopPayload.addr
    },
    setEn=2,
  )
  doRamWrite(
    busAddr=(
      RegNext(
        RegNext(
          myH2dPopPayload.addr, init=myH2dPopPayload.addr.getZero
        ),
        init=myH2dPopPayload.addr.getZero,
      )
    ),
    lineWord=(
      RegNext(
        RegNext(
          myH2dPopPayload.data,
          init=myH2dPopPayload.data.getZero
        ),
        init=myH2dPopPayload.data.getZero,
      )
    ),
    byteEn=Some(
      RegNext(
        RegNext(
          myH2dPopPayload.byteEn,
          init=myH2dPopPayload.byteEn.getZero
        ),
        init=myH2dPopPayload.byteEn.getZero,
      ),
    ),
    setEn=false,
  )
  //--------
  myD2hPushStm.payload := (
    RegNext(myD2hPushStm.payload, init=myD2hPushStm.payload.getZero)
  )
  //--------
  object State extends SpinalEnum(
    defaultEncoding=(
      //binarySequential
      binaryOneHot
    )
  ) {
    val
      IDLE,
      LOAD_NON_BURST_DO_STALL_PIPE_2,
      LOAD_NON_BURST_DO_STALL_PIPE_1,
      LOAD_NON_BURST_DO_STALL,
      LOAD_NON_BURST_DO_STALL_POST,
      STORE_NON_BURST_DO_STALL_PIPE_1,
      STORE_NON_BURST_DO_STALL,
      WAIT_D2H_FIFO_EMPTY

      //LOAD_BURST_PIPE_2,
      //LOAD_BURST_PIPE_1,
      //LOAD_BURST,
      //LOAD_BURST_POST,
      //STORE_BURST_PIPE_1,
      //STORE_BURST

      //LOAD_BURST_PIPE_2,
      //LOAD_BURST_PIPE_1,
      //LOAD_BURST,
      //LOAD_BURST_POST_1,
      //STORE_BURST,
      //STORE_BURST_POST_1,
      = newElement();
  }
  val rState = (
    Reg(State())
    init(State.IDLE)
  )

  def doPopH2dFifo(): Unit = {
    myH2dPopStm.ready := True
  }
  val myTempIgnoreDupCntCond = (
    Mux[Bool](
      rState === State.IDLE,
      RegNext(
        (
          //rState === State.RECV_LINE_FROM_HI_BUS_POST
          rState =/= State.IDLE
        ),
        init=False
      ),
      True//False
    )
  )
  val myOtherFullTempIgnoreDupCntCond = (
    myH2dDoStallFifoThing.io.pop.valid
    //myH2dDoStallFifoThing.io.pop.fire
    && (
      myH2dDoStallFifoThing.io.pop.cnt.asSInt
      =/= (
        RegNextWhen(
          (myH2dDoStallFifoThing.io.pop.cnt + 1).asSInt,
          //(myD2hPushStm.cnt + 1).asSInt,
          cond=(
            //myD2hPushStm.fire
            myH2dDoStallFifoThing.io.pop.fire
            //&& !myH2dPopThrowArea.myH2dThrowCond
          ),
          //cond=base.loH2dDoStallFifoThing.io.pop.fire,
          ////init=base.loH2dDoStallFifoThing.io.pop.src.getZero,
        )
        init(-2)
      )
    )
  )
  val myFullTempIgnoreDupCntCond = (
    //(
    //  base.myFifoThingDoStall.head
    //  //|| base.myFifoThingDoStall.last
    //)
    //&& 
    myH2dDoStallFifoThing.io.pop.valid
    //myH2dDoStallFifoThing.io.pop.fire
    && (
      myH2dDoStallFifoThing.io.pop.cnt.asSInt
      =/= (
        RegNextWhen(
          (myH2dDoStallFifoThing.io.pop.cnt + 1).asSInt,
          //(myD2hPushStm.cnt + 1).asSInt,
          cond=(
            //myD2hPushStm.fire
            myH2dDoStallFifoThing.io.pop.fire
            //&& !myH2dPopThrowArea.myH2dThrowCond
            && !myOtherFullTempIgnoreDupCntCond
          ),
          //cond=base.loH2dDoStallFifoThing.io.pop.fire,
          ////init=base.loH2dDoStallFifoThing.io.pop.src.getZero,
        )
        init(-2)
      )
    )
    //&& (
    //  base.loH2dDoStallFifoThing.io.pop.src.asSInt
    //  =/= (
    //    RegNextWhen(
    //      //(base.loH2dDoStallFifoThing.io.pop.src + 1).asSInt,
    //      (myLoD2hStm.src - 1).asSInt,
    //      cond=myLoD2hStm.fire,
    //      //cond=base.loH2dDoStallFifoThing.io.pop.fire,
    //      ////init=base.loH2dDoStallFifoThing.io.pop.src.getZero,
    //    )
    //    init(-2)
    //  )
    //)

    //&& (
    //  myTempIgnoreDupCntCond
    //  //&& RegNext(myTempIgnoreDupCntCond, init=False)
    //)
    && History[Bool](
      that=True,
      when=(
        myH2dPopStm.fire
        //myD2hPushStm.fire
        //myH2dDoStallFifoThing.io.pop.fire
        //&& !myH2dPopThrowArea.myH2dThrowCond
      ),
      length=(
        //2
        //4
        //3
        5
      ),
      init=False,
    ).last
  )
  def doIgnoreInvalidFifoThingPopCnt(
  ): Unit = {
    when (myFullTempIgnoreDupCntCond) {
      //loH2dPopStm.ready := True
      myH2dPopThrowArea.myH2dThrowCond := True
    }
  }
  //doIgnoreInvalidFifoThingPopCnt()

  //def doNotIgnoreInvalidFifoThingPopCnt(
  //): Unit = {
  //  myH2dPopThrowArea.myH2dThrowCond := False
  //}

  val rMyTempDoSaveCond = (
    RegNext(
      RegNext(
        (
          myH2dPopStm.fire
          //&& !myFullTempIgnoreDupCntCond
        ),
        init=False
      ),
      init=False
    )
  )
  val rHadRamWritePastTwoCycles = Vec.fill(2)(
    RegNext(
      ram.io.wrEn
      || RegNext(ram.io.wrEn, init=False),
      init=False
    )
  )

  switch (rState) {
    is (State.IDLE) {
      doIgnoreInvalidFifoThingPopCnt()
      myFifoThingDoStall := False
      myD2hPushStm.valid := False
      doPopH2dFifo()

      //rSavedH2dPayload := rDel2H2dPayload
      //myD2hPushStm.busPayload.src := rDel2H2dPayload.busPayload.src

      when (
        //RegNext(ram.io.rdEn, init=False)
        //RegNext(
        //  RegNext(
        //    (
        //      myH2dPopStm.fire
        //      && !myFullTempIgnoreDupCntCond
        //    ),
        //    init=False
        //  ),
        //  init=False
        //)
        rMyTempDoSaveCond
      ) {
        rSavedH2dPayload := (
          rH2dPayload
          //rDel2H2dPayload
          //RegNext(
          //  rDel2H2dPayload,
          //  init=rDel2H2dPayload.getZero
          //)
        )
      }
      when (
        RegNext(
          RegNext(myH2dPopStm.fire, init=False),
          init=False
        )
      ) {
        myD2hPushStm.busPayload.src := (
          //RegNext(
            //rH2dPayload.busPayload.src
            rDel2H2dPayload.busPayload.src
            //rDel2H2dPayload.busPayload.src//,
          //  init=rDel2H2dPayload.busPayload.src.getZero
          //)
        )
        myD2hPushStm.cnt := (
          //RegNext(
            rDel2H2dPayload.cnt//,
            //rH2dPayload.cnt
          //  init=rDel2H2dPayload.cnt.getZero,
          //)
        )
      }
      switch (
        //RegNext(ram.io.rdEn, init=False)
        //RegNext(myH2dPopStm.fire, init=False)
        RegNext(
          RegNext(myH2dPopStm.fire, init=False),
          init=False
        )
        //## rDel2H2dPayload.busPayload.burstFirst
        ## rDel2H2dPayload.busPayload.isWrite
      ) {
        is (
          //M"100"
          M"10"
        ) {
          // non-burst, load
          myH2dPopStm.ready := True
          myD2hPushStm.valid := True
          myD2hPushStm.busPayload.data := rdLineWord

          when (rHadRamWritePastTwoCycles.head) {
            myD2hPushStm.valid := False
          }
          when (
            rHadRamWritePastTwoCycles.last
            || !myD2hPushStm.ready
          ) {
            myH2dPopStm.ready := False
            myFifoThingDoStall := True
            rState := State.LOAD_NON_BURST_DO_STALL_PIPE_2
          }
        }
        is (
          //M"101"
          M"11"
        ) {
          // non-burst, store
          ram.io.wrEn := True
          myD2hPushStm.valid := True

          when (!myD2hPushStm.ready) {
            myH2dPopStm.ready := False
            myFifoThingDoStall := True
            rState := State.STORE_NON_BURST_DO_STALL_PIPE_1
          }
        }
        //is (M"110") {
        //  // burst, load
        //  //rState := State.LOAD_BURST_PIPE_2
        //  myFifoThingDoStall := True
        //}
        //is (M"111") {
        //  // burst, store
        //  //rState := State.STORE_BURST_PIPE_1
        //  myFifoThingDoStall := True
        //}
        default {
        }
      }
    }
    is (State.LOAD_NON_BURST_DO_STALL_PIPE_2) {
      rState := State.LOAD_NON_BURST_DO_STALL_PIPE_1
      myD2hPushStm.valid := False
      //myLoD2hStm.valid := False
      myH2dPopStm.ready := False
      //lineAttrsRam.io.rdEn := False
      //base.myFifoThingDoStall.last := False
      doRamReadSync(
        busAddr=rSavedH2dPayload.busPayload.addr,
        setEn=0,
      )
    }
    is (State.LOAD_NON_BURST_DO_STALL_PIPE_1) {
      rState := State.LOAD_NON_BURST_DO_STALL
      //lineAttrsRam.io.rdEn := False
      ram.io.rdEn := True
      myD2hPushStm.valid := False
      myH2dPopStm.ready := False
    }
    is (State.LOAD_NON_BURST_DO_STALL) {
      //lineAttrsRam.io.rdEn := False
      ram.io.rdEn := False
      myH2dPopStm.ready := False
      myD2hPushStm.busPayload.data := rdLineWord
      myD2hPushStm.valid := True
      when (myD2hPushStm.fire) {
        //base.myFifoThingDoStall.last := False
        myFifoThingDoStall := False
        rState := (
          //State.IDLE
          State.LOAD_NON_BURST_DO_STALL_POST
        )
        //doPopLoH2dFifo()
        //rSeenStateIdle := False
      }
    }
    is (State.LOAD_NON_BURST_DO_STALL_POST) {
      ram.io.rdEn := False
      //rState := State.IDLE
      rState := State.WAIT_D2H_FIFO_EMPTY
    }
    is (State.STORE_NON_BURST_DO_STALL_PIPE_1) {
      myD2hPushStm.valid := False
      //lineAttrsRam.io.rdEn := False
      ram.io.rdEn := False
      myH2dPopStm.ready := False
      //myLoD2hStm.valid := False
      rState := State.STORE_NON_BURST_DO_STALL
    }
    is (State.STORE_NON_BURST_DO_STALL) {
      //lineAttrsRam.io.rdEn := False
      ram.io.rdEn := False
      myH2dPopStm.ready := False
      myD2hPushStm.valid := True
      when (myD2hPushStm.ready) {
        //base.myFifoThingDoStall.last := False
        //rState := State.IDLE
        //rSeenStateIdle := False
        rState := State.WAIT_D2H_FIFO_EMPTY
      }
    }
    is (State.WAIT_D2H_FIFO_EMPTY) {
      ram.io.rdEn := False
      when (!myD2hFifo.io.pop.valid) {
        rState := State.IDLE
      }
    }
  }
}

case class LcvBusMem(
  cfg: LcvBusMemConfig
) extends Component {
  val io = LcvBusMemIo(cfg=cfg)
  val myMemImpl = LcvBusMemImpl(cfg=cfg)
  val myDeburster = (
    cfg.busCfg.allowBurst
  ) generate (LcvBusDeburster(cfg=LcvBusDebursterConfig(
    loBusCfg=cfg.busCfg
  )))
  if (cfg.busCfg.allowBurst) {
    io.bus <> myDeburster.io.loBus
    //myDeburster.io.hiBus <> myMemImpl.io.bus
    myDeburster.io.hiBus.h2dBus.translateInto(
      myMemImpl.io.bus.h2dBus
    )(
      dataAssignment=(
        outp, inp
      ) => {
        outp.mainNonBurstInfo := inp.mainNonBurstInfo
        outp.mainBurstInfo := outp.mainBurstInfo.getZero
      }
    )
    myMemImpl.io.bus.d2hBus.translateInto(
      myDeburster.io.hiBus.d2hBus
    )(
      dataAssignment=(
        outp, inp
      ) => {
        outp.mainNonBurstInfo := inp.mainNonBurstInfo
      }
    )
  } else {
    io.bus <> myMemImpl.io.bus
  }
}

case class LcvBusMemSlowWhenBurst(
  cfg: LcvBusMemConfig,
) extends Component {
  //--------
  def busCfg = cfg.busCfg
  require(busCfg.allowBurst)
  //require(busCfg.haveByteEn)
  //--------
  val io = LcvBusMemIo(cfg=cfg)
  //--------
  val ram = RamSdpPipe(
    cfg=cfg.ramCfg
  )
  //--------
  object State
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      NON_BURST_READ_PIPE_1,
      NON_BURST,
      READ_BURST_PIPE_2,
      READ_BURST_PIPE_1,
      READ_BURST,
      READ_BURST_POST_1,
      WRITE_BURST,
      WRITE_BURST_POST_1,
      WAIT_D2H_FIFO_EMPTY
      = newElement()
  }
  val rState = (
    Reg(State())
    init(State.IDLE)
  )
  //--------
  //val rH2dPayload = (
  //  RegNext(io.bus.h2dBus.payload, init=io.bus.h2dBus.payload.getZero)
  //)
  val myTempBusCfg = (
    if (!busCfg.haveByteEn) (
      LcvBusConfig(
        mainCfg=busCfg.mainCfg.mkCopyWithByteEn(Some(true)),
        cacheCfg=busCfg.cacheCfg,
      )
    ) else (
      busCfg
    )
  )
  val myH2dStm = (
    //cloneOf(io.bus.h2dBus)
    Stream(LcvBusH2dPayload(cfg=myTempBusCfg))
  )
  //def myH2dPayload = myH2dStm.payload
  val rSavedH2dPayload = (
    Reg(cloneOf(myH2dStm.payload))
    init(myH2dStm.payload.getZero)
  )
  //myH2dStm.ready.setAsReg() init(False)
  //io.bus.d2hBus.valid.setAsReg() init(False)
  //io.bus.d2hBus.payload.setAsReg() init(io.bus.d2hBus.payload.getZero)
  val rH2dReady = Reg(Bool(), init=False)
  val rD2hValid = Reg(Bool(), init=False)
  val rD2hPayload = (
    Reg(cloneOf(io.bus.d2hBus.payload), init=io.bus.d2hBus.payload.getZero)
  )
  myH2dStm.ready := rH2dReady
  //io.bus.d2hBus.valid := rD2hValid
  //io.bus.d2hBus.payload := rD2hPayload
  val myH2dShiftedDataStmAdapter = (
    !busCfg.haveByteEn
  ) generate (
    LcvBusH2dShiftedDataEtcStreamAdapter(
      cfg=LcvBusH2dShiftedDataEtcStreamAdapterConfig(
        loBusCfg=busCfg
      )
    )
  )
  val myD2hShiftedDataStmAdapter = (
    !busCfg.haveByteEn
  ) generate (
    LcvBusD2hShiftedDataEtcStreamAdapter(
      cfg=LcvBusD2hShiftedDataEtcStreamAdapterConfig(
        busCfg=busCfg
      )
    )
  )

  val myD2hFifo = (
    StreamFifo(
      dataType=(
        //UInt(busCfg.dataWidth bits)
        cloneOf(io.bus.d2hBus.payload)
      ),
      depth=(busCfg.maxBurstSizeMinus1 + 1),
      latency=cfg.busD2hFifoLatency,
      forFMax=true,
    )
  )
  io.bus.d2hBus << myD2hFifo.io.pop
  val myD2hStm = cloneOf(myD2hFifo.io.push)

  myD2hStm.valid := rD2hValid
  myD2hStm.payload := rD2hPayload
  if (!busCfg.haveByteEn) {
    //myH2dShiftedDataStmAdapter
    myH2dShiftedDataStmAdapter.io.loH2dBus << io.bus.h2dBus
    myH2dStm << myH2dShiftedDataStmAdapter.io.hiH2dBus

    //myD2hShiftedDataStmAdapter.io.addr := rSavedH2dPayload.addr
    //myD2hShiftedDataStmAdapter.io.byteSize := rSavedH2dPayload.byteSize
    myD2hShiftedDataStmAdapter.io.loD2hBus << myD2hStm
    myD2hFifo.io.push << myD2hShiftedDataStmAdapter.io.hiD2hBus
  } else {
    myH2dStm << io.bus.h2dBus
    myD2hFifo.io.push.valid := rD2hValid
    myD2hFifo.io.push.payload := rD2hPayload
  }

  def myRamAddrRshift = log2Up(busCfg.dataWidth / 8)
  def myRamAddrRange = (
    ram.io.rdAddr.high + myRamAddrRshift downto myRamAddrRshift
  )

  //val rRamRdEn = Reg(Bool(), init=True)
  //ram.io.rdEn := rRamRdEn

  ram.io.rdEn := False
  ram.io.rdAddr := myH2dStm.addr(myRamAddrRange)

  ram.io.wrEn := False
  ram.io.wrAddr := myH2dStm.addr(myRamAddrRange)
  ram.io.wrData := myH2dStm.data.asBits
  ram.io.wrByteEn := myH2dStm.byteEn.asBits
  //val rRamWrEn = Reg(Bool(), init=False)
  //val rRamWrAddr = (
  //  Reg(cloneOf(ram.io.wrAddr), init=ram.io.wrAddr.getZero)
  //)
  //val rRamWrData = (
  //  Reg(cloneOf(ram.io.wrData), init=ram.io.wrData.getZero)
  //)
  //val rRamWrByteEn = (
  //  Reg(cloneOf(ram.io.wrByteEn), init=ram.io.wrByteEn.getZero)
  //)
  //ram.io.wrEn := rRamWrEn
  //ram.io.wrAddr := rRamWrAddr
  //ram.io.wrData := rRamWrData
  //ram.io.wrByteEn := rRamWrByteEn

  //println(
  //  s"testificate: ${busCfg.burstCntWidth}"
  //)
  def myRdBurstCnt0InitVal = (
    // -2
    //U(rRdBurstCnt(1).getWidth bits, 0 -> False, default -> True)
    //U(busCfg.burstCntWidth bits, 0 -> False, default -> True)
    //(-S(s"${busCfg.burstCntWidth}'d2")).asUInt
    -2
  )
  val rRdBurstCnt = (
    Vec[SInt](List(
      (Reg(SInt(busCfg.burstCntWidth bits)) init(myRdBurstCnt0InitVal)),
      (Reg(SInt(busCfg.burstCntWidth bits)) init(0x0)),
    ))
  )
  def doInitRdBurstCnt(): Unit = {
    // `rRdBurstCnt(0)` is the lagging counter, used to determine when
    // we're done filling `myD2hFifo`
    // `rRdBurstCnt(0)` is lagging behind by two because it takes two
    // cycles to read a value from a `RamSdpPipe`, and this allows us to
    // pipeline said reads during a an `LcvBus` read burst.
    rRdBurstCnt(0) := myRdBurstCnt0InitVal
    // `rRdBurstCnt(1)` is the leading counter, used to determine when
    // we're done reading from `ram` during an `LcvBus` read burst.
    rRdBurstCnt(1) := 0x0
  }
  def doIncrRdBurstCnt(): Unit = {
    rRdBurstCnt.foreach(item => {
      item := item + 1
    })
  }

  val rWrBurstCnt = (
    Reg(UInt(busCfg.burstCntWidth bits))
    init(0x0)
  )
  rD2hPayload.src.allowOverride

  switch (rState) {
    is (State.IDLE) {
      rSavedH2dPayload := myH2dStm.payload

      rWrBurstCnt := 0x0

      rD2hValid := False
      rD2hPayload := rD2hPayload.getZero
      //when (myH2dStm.valid) {
      //  rD2hPayload.src := myH2dStm.src
      //}

      switch (
        myH2dStm.valid
        ## myH2dStm.isWrite
        ## myH2dStm.burstFirst
      ) {
        is (B"100") {
          // read, non-burst
          rState := State.NON_BURST_READ_PIPE_1
          //myH2dStm.ready := True
          //ram.io.rdEn := (
          //  //False
          //  //True
          //  False
          //)
          rH2dReady := (
            //True
            False
          )
        }
        is (B"101") {
          // read, burst
          rState := (
            State.READ_BURST_PIPE_2
            //State.READ_BURST_PIPE_1
          )
          //myH2dStm.ready := True
          ram.io.rdAddr := (
            myH2dStm.burstAddr(
              rRdBurstCnt(1).asUInt.getZero,
              incrBurstCnt=false,
            )(myRamAddrRange)
          )
          //rRdBurstCnt := 1
          doIncrRdBurstCnt()
          rH2dReady := True
          when (myH2dStm.valid) {
            rD2hPayload.src := myH2dStm.src
          }
          //rD2hPayload.burstFirst := False
        }
        is (B"110") {
          // write, non-burst
          rState := State.NON_BURST
          //myH2dStm.ready := True
          rH2dReady := True
          //when (myH2dStm.valid) {
          //  rD2hPayload.src := myH2dStm.src
          //}
          ram.io.rdEn := False
          ram.io.wrEn := True
        }
        is (B"111") {
          // write, burst
          rState := State.WRITE_BURST
          //myH2dStm.ready := False
          rH2dReady := False
          //when (myH2dStm.valid) {
          //  rD2hPayload.src := myH2dStm.src
          //}
        }
        default {
          // no active transaction
          //myH2dStm.ready := False
          rH2dReady := False
        }
      }
    }
    is (State.NON_BURST_READ_PIPE_1) {
      ram.io.rdEn := True
      rH2dReady := True
      rState := State.NON_BURST
    }
    is (State.NON_BURST) {
      //myH2dStm.ready := False
      //when (myH2dStm.fire) {
      //  rD2hPayload.src := myH2dStm.src
      //}
      ram.io.rdEn := False
      ram.io.wrEn := False
      when (myH2dStm.fire) {
        rD2hPayload.src := myH2dStm.src
        rH2dReady := False
      }
      when (
        //!rH2dReady
        //||
        myH2dStm.fire
      ) {
        rD2hValid := True
        rD2hPayload.data.assignFromBits(ram.io.rdData)
      }
      when (myD2hFifo.io.push.fire) {
        rD2hValid := False
        rState := State.WAIT_D2H_FIFO_EMPTY
      }
    }
    is (State.READ_BURST_PIPE_2) {
      rState := State.READ_BURST_PIPE_1
      rH2dReady := False

      ram.io.rdEn := True
      ram.io.rdAddr := (
        rSavedH2dPayload.burstAddr(
          rRdBurstCnt(1).asUInt,
          incrBurstCnt=false
        )(myRamAddrRange)
      )
      //rRdBurstCnt.foreach(item => item := item + 1)

      doIncrRdBurstCnt()
    }
    is (State.READ_BURST_PIPE_1) {
      //myH2dStm.ready := False
      rState := State.READ_BURST
      ram.io.rdEn := True
      ram.io.rdAddr := (
        rSavedH2dPayload.burstAddr(
          rRdBurstCnt(1).asUInt,
          incrBurstCnt=false,
        )(myRamAddrRange)
      )
      //rRdBurstCnt := rRdBurstCnt + 1
      doIncrRdBurstCnt()

      rD2hValid := True
      rD2hPayload.burstFirst := True
      rD2hPayload.data := ram.io.rdData.asUInt
    }
    is (State.READ_BURST) {
      //myH2dStm.ready := False
      //rH2dReady := False
      ram.io.rdEn := RegNext(ram.io.rdEn, init=ram.io.rdEn.getZero)
      ram.io.rdAddr := (
        rSavedH2dPayload.burstAddr(
          rRdBurstCnt(1).asUInt,
          incrBurstCnt=false,
        )(myRamAddrRange)
      )
      //rD2hPayload.burstFirst := False
      //rRdBurstCnt := rRdBurstCnt + 1

      doIncrRdBurstCnt()
      rD2hPayload.data := ram.io.rdData.asUInt

      when (rD2hPayload.burstFirst) {
        rD2hPayload.burstFirst := False
      }
      //when (rRdBurstCnt === (1 << rRdBurstCnt.getWidth) - 2) {
      //  rD2hPayload.burstLast := True
      //}
      when (rRdBurstCnt(1) === 0x1) {
        ram.io.rdEn := False
      }
      when (
        rRdBurstCnt(1) === 0x1
        ////=== -1
        //=== U(rRdBurstCnt(0).getWidth bits, default -> True)
      ) {
        rD2hPayload.burstLast := True
      }
      when (rRdBurstCnt(1) === 0x2) {
        rD2hValid := False
        rD2hPayload.burstLast := False
        rState := State.WAIT_D2H_FIFO_EMPTY
      } otherwise {
      }
      //when (rD2hPayload.burstLast) {
      //  rD2hValid := False
      //  rD2hPayload.burstLast := False
      //  rState := State.WAIT_D2H_FIFO_EMPTY
      //}
    }
    //is (State.READ_BURST_POST_1) {
    //  
    //}
    is (State.WRITE_BURST) {
      when (myH2dStm.fire) {
        rD2hPayload.src := myH2dStm.src
      }
      rH2dReady := True
      ram.io.wrAddr := (
        rSavedH2dPayload.burstAddr(
          rWrBurstCnt,
          incrBurstCnt=false,
        )(myRamAddrRange)
      )
      ram.io.wrData := myH2dStm.data.asBits
      ram.io.wrByteEn := myH2dStm.byteEn.asBits

      when (myH2dStm.fire) {
        ram.io.wrEn := True
        rWrBurstCnt := rWrBurstCnt + 1
        when (myH2dStm.burstLast) {
          rState := State.WRITE_BURST_POST_1
          rH2dReady := False
        }
      }
    }
    is (State.WRITE_BURST_POST_1) {
      //myH2dStm.ready := False
      rH2dReady := False
      rD2hValid := True
      rWrBurstCnt := 0x0
      when (myD2hFifo.io.push.fire) {
        rD2hValid := False
        rState := State.WAIT_D2H_FIFO_EMPTY
      }
    }
    is (State.WAIT_D2H_FIFO_EMPTY) {
      doInitRdBurstCnt()
      rD2hValid := False
      when (myD2hFifo.io.occupancy === 0) {
        rState := State.IDLE
      }
    }
  }
}

object LcvBusMemSpinalConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    )
  )
}


case class LcvBusMemSimDut(
  cfg: LcvBusMemConfig=LcvBusMemTestConfig.cfg,
) extends Component {
  //--------
  val io = new Bundle {
  }
  //--------
  //val memCfg = LcvBusMemTestConfig.cfg

  val myMem = LcvBusMem(cfg=cfg)
  val myMemTester = LcvBusDeviceRamTester(
    cfg=LcvBusDeviceRamTesterConfig(
      busCfg=cfg.busCfg,
      kind=(
        LcvBusDeviceRamTesterKind.DualBurstRandDataSemiRandAddr
        //(
        //  optNonCoherentCacheSetWidth=(
        //    //None
        //    Some(8)
        //  )
        //)
        //LcvBusDeviceRamTesterKind.DualBurstRandData
        //LcvBusDeviceRamTesterKind.NoBurstRandData
      ),
    )
  )
  myMem.io.bus <> myMemTester.io.busVec.head
}

object LcvBusMemTestConfig {
  val depth = 1024
  val cfg = LcvBusMemConfig(
    busCfg=LcvBusConfig(
      mainCfg=LcvBusMainConfig(
        dataWidth=32,
        addrWidth=32,
        allowBurst=true,
        burstAlwaysMaxSize=true,
        srcWidth=1,
        haveByteEn=false,
        keepByteSize=false,
      ),
      cacheCfg=None,
    ),
    depth=depth,
    initBigInt=Some(Array.fill(depth)(BigInt(0)))
  )
}
object LcvBusMemToVerilog extends App {
  LcvBusMemSpinalConfig.spinal.generateVerilog{
    LcvBusMem(cfg=LcvBusMemTestConfig.cfg)
  }
}

object LcvBusMemSim extends App {
  def clkRate = 100.0 MHz
  val simSpinalConfig = SpinalConfig(
    defaultClockDomainFrequency=FixedFrequency(clkRate)
  )
  SimConfig
    .withConfig(config=simSpinalConfig)
    .withFstWave
    .compile(
      //LcvSdramSimDut(clkRate=clkRate)
      LcvBusMemSimDut()
    )
    .doSim { dut =>
      dut.clockDomain.forkStimulus(period=10)
      def simNumClks = (
        200000
      )
      for (idx <- 0 until simNumClks) {
        dut.clockDomain.waitRisingEdge()
      }
      simSuccess()
    }
}
