package libcheesevoyage.bus.lcvBus
import libcheesevoyage.bus.lcvStall._

import scala.collection.immutable
import scala.collection.mutable._
import scala.math._
import spinal.core._
import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.misc.pipeline._

case class LcvBusIrqCtrlConfig(
  busCfg: LcvBusConfig,
  depth: Int,
  optIrqEnableBusRegInitSeq: Option[Seq[BigInt]]=None,
) {
  require(!busCfg.allowBurst)
  //require(busCfg.haveByteEn)
  require(
    depth > 0
  )
  optIrqEnableBusRegInitSeq match {
    case Some(myIrqEnableBusRegInitSeq) => {
      require(
        myIrqEnableBusRegInitSeq.size == depth,
        s"need same size: "
        + s"initSeq.size:${myIrqEnableBusRegInitSeq.size} "
        + s"!= depth:${depth}"
      )
    }
    case None => {
    }
  }
  require(
    depth <= busCfg.dataWidth,
    s"for the time being, it is required that "
    + s"depth:${depth} <= busCfg.dataWidth:${busCfg.dataWidth}"
  )
  def myBusRegAddrMult = busCfg.byteEnWidth
  def numBusRegsPerKind = (
    ceil(depth.toDouble / busCfg.dataWidth.toDouble).toInt
  )
  def alignedNumBusRegsPerKind = (
    1 << log2Up(numBusRegsPerKind)
  )
  def totalNumBusRegs = (
    numBusRegsPerKind * 2
  )
  def alignedTotalNumBusRegs = (
    alignedNumBusRegsPerKind * 2
  )
  def myTempBusAddrRange = (
    log2Up(alignedTotalNumBusRegs * myBusRegAddrMult) - 1
    downto log2Up(myBusRegAddrMult)
  )

  def myIrqIdBusRegVecAddrStart = (
    0x0 * myBusRegAddrMult
  )
  def myIrqIdBusRegVecAddrEnd = (
    myIrqIdBusRegVecAddrStart
    + (numBusRegsPerKind - 1) * myBusRegAddrMult
  )
  def myIrqEnableBusRegVecAddrStart = (
    alignedNumBusRegsPerKind * myBusRegAddrMult
  )
  def myIrqEnableBusRegVecAddrEnd = (
    myIrqEnableBusRegVecAddrStart
    + (numBusRegsPerKind - 1) * myBusRegAddrMult
  )

  println(
    s"LcvBusIrqCtrlConfig: BEGIN: "
    + s"depth:${depth} busCfg.dataWidth:${busCfg.dataWidth}"
  )
  println(
    s"myBusRegAddrMult:${myBusRegAddrMult} "
    + s"numBusRegsPerKind:${numBusRegsPerKind} "
    + s"totalNumBusRegs:${totalNumBusRegs} "
    + s"myTempBusAddrRange:${myTempBusAddrRange}"
  )
  println(
    s"myIrqIdBusRegVecAddrStart:${myIrqIdBusRegVecAddrStart} "
    + s"myIrqIdBusRegVecAddrEnd:${myIrqIdBusRegVecAddrEnd}"
  )
  println(
    s"myIrqEnableBusRegVecAddrStart:${myIrqEnableBusRegVecAddrStart} "
    + s"myIrqEnableBusRegVecAddrEnd:${myIrqEnableBusRegVecAddrEnd}"
  )
  println(
    s"LcvBusIrqCtrlConfig: END: "
    + s"depth:${depth} busCfg.dataWidth:${busCfg.dataWidth}"
  )
}

case class LcvBusIrqCtrlIo(
  cfg: LcvBusIrqCtrlConfig
) extends Bundle {
  //--------
  val bus = slave(LcvBusIo(cfg=cfg.busCfg))
  //--------
  val dstIrq = master(
    new LcvStallIo[Bool, Bool](
      sendPayloadType=None,
      recvPayloadType=None,
    )
  )
  //val dstIrq = out(Bool())
  //--------
  //val srcIrqVec = Vec.fill(cfg.depth)(
  //  slave(
  //    new LcvStallIo[Bool, Bool](
  //      sendPayloadType=None,
  //      recvPayloadType=None,
  //    )
  //  )
  //)
  val srcIrqVec = in(
    //Vec.fill(cfg.numBusRegsPerKind)(
    //  UInt(cfg.busCfg.dataWidth bits)
    //)
    UInt(cfg.depth bits)
    //Vec.fill(cfg.depth)(
    //  Bool()
    //)
  )
  //--------
}

case class LcvBusIrqCtrl(
  cfg: LcvBusIrqCtrlConfig
) extends Component {
  //--------
  def busCfg = cfg.busCfg
  //--------
  val io = LcvBusIrqCtrlIo(cfg=cfg)
  //--------
  // bus-addressable registers
  def myBusRegAddrMult = cfg.myBusRegAddrMult
  def numBusRegsPerKind = cfg.numBusRegsPerKind

  //def myIrqSrcBusRegAddr = 0x0 * myBusRegAddrMult
  val rIrqIdBusRegVec = (
    Vec.fill(numBusRegsPerKind)(
      Reg(UInt(
        busCfg.dataWidth bits
        //cfg.depth bits
      ))
      init(0x0)
    )
  )
  val rIrqEnableBusRegVec = {
    val temp = Vec.fill(numBusRegsPerKind)(
      Reg(UInt(
        busCfg.dataWidth bits
        //cfg.depth bits
      ))
      //init(0x0)
    )
    cfg.optIrqEnableBusRegInitSeq match {
      case Some(myIrqEnableBusRegInitSeq) => {
        for (idx <- 0 until temp.size) {
          temp(idx).init(myIrqEnableBusRegInitSeq(idx))
        }
      }
      case None => {
        for (idx <- 0 until temp.size) {
          temp(idx).init(temp(idx).getZero)
        }
      }
    }
    temp
  }

  //def myIrqValidBusRegAddr = 0x1 * myBusRegAddrMult
  //val rIrqValidReg = (
  //  Reg(UInt(busCfg.dataWidth bits))
  //  init(0x0)
  //)

  //def myIrqReadyBusRegAddr = 0x1 * myBusRegAddrMult
  //val rIrqReadyReg = (
  //  // read/write
  //  Reg(UInt(busCfg.dataWidth bits))
  //  init(0x0)
  //)
  //--------
  io.bus.h2dBus.ready := False
  io.bus.d2hBus.payload := io.bus.d2hBus.payload.getZero
  io.bus.d2hBus.data.allowOverride
  io.bus.d2hBus.data := (
    RegNext(
      io.bus.d2hBus.data,
      init=io.bus.d2hBus.data.getZero
    )
  )
  io.bus.d2hBus.valid := False

  object DstIrqState
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      WAIT_DST_IRQ_READY
      = newElement();
  }
  val rDstIrqState = (
    Reg(DstIrqState())
    init(DstIrqState.IDLE)
  )
  //io.dstIrq.nextValid.setAsReg() init(False)
  io.dstIrq.nextValid := (
    RegNext(
      io.dstIrq.nextValid,
      init=io.dstIrq.nextValid.getZero
    )
  )

  switch (rDstIrqState) {
    is (DstIrqState.IDLE) {
      val tempOrReduce = (
        // this acts as a check for `=/= 0x0`,
        // i.e. it is a check for *any* 1 bit
        rIrqIdBusRegVec.asBits.orR
      )
      io.dstIrq.nextValid := (
        tempOrReduce
      )
      when (tempOrReduce) {
        rDstIrqState := DstIrqState.WAIT_DST_IRQ_READY
      }
    }

    is (DstIrqState.WAIT_DST_IRQ_READY) {
      when (
        RegNext(
          !io.dstIrq.nextValid,
          init=False
        )
        || io.dstIrq.ready
      ) {
        io.dstIrq.nextValid := False
        rDstIrqState := DstIrqState.IDLE
      }
    }
  }

  //--------
  object State
  extends SpinalEnum(defaultEncoding=binaryOneHot) {
    val
      IDLE,
      BUS_RD,
      BUS_WR
      = newElement();
  }
  val rState = (
    Reg(State())
    init(State.IDLE)
  )
  val rSavedH2dPayload = (
    Reg(
      cloneOf(io.bus.h2dBus.payload),
      init=io.bus.h2dBus.payload.getZero
    )
  )

  val myTempIrqIdAsBits = cloneOf(rIrqIdBusRegVec.asBits)
  myTempIrqIdAsBits := (
    RegNext(
      myTempIrqIdAsBits,
      init=myTempIrqIdAsBits.getZero
    )
  )

  switch (rState) {
    is (State.IDLE) {
      rSavedH2dPayload := io.bus.h2dBus.payload

      for (idx <- 0 until rIrqEnableBusRegVec.asBits.getWidth) {
        when (
          io.srcIrqVec.asBits.asUInt.resize(
            rIrqIdBusRegVec.asBits.getWidth
          ).asBits(idx)
          & rIrqEnableBusRegVec.asBits(idx)
        ) {
          myTempIrqIdAsBits(idx) := True
          //rIrqIdBusRegVec.assignFromBits(
          //  //io.srcIrqVec.asBits.asUInt.resize(
          //  //  rIrqIdBusRegVec.asBits.getWidth
          //  //).asBits
          //  //& rIrqEnableBusRegVec.asBits
          //  myTempIrqIdAsBits
          //)
        }
      }
      rIrqIdBusRegVec.assignFromBits(
        //io.srcIrqVec.asBits.asUInt.resize(
        //  rIrqIdBusRegVec.asBits.getWidth
        //).asBits
        //& rIrqEnableBusRegVec.asBits
        myTempIrqIdAsBits
      )

      switch (
        io.bus.h2dBus.valid
        ## io.bus.h2dBus.isWrite
        //## io.bus.h2dBus.addr(cfg.myTempBusAddrRange)
      ) {
        is (B"10") {
          io.bus.h2dBus.ready := True
          rState := State.BUS_RD
        }
        is (B"11") {
          io.bus.h2dBus.ready := True
          rState := State.BUS_WR
        }
        default {
        }
      }
    }
    is (State.BUS_RD) {
      val myRdCatToSwitch = (
        rose(rState === State.BUS_RD)
        ## rSavedH2dPayload.addr(cfg.myTempBusAddrRange)
      )
      switch (
        //rSavedH2dPayload.isWrite
        //## 
        Cat(
          //rose(rState === State.BUS_RD),
          //rSavedH2dPayload.addr(cfg.myTempBusAddrRange)
          myRdCatToSwitch
        )
      ) {
        //}
        for (idx <- 0 until numBusRegsPerKind) {
          val tempAddrWidth = (
            rSavedH2dPayload.addr(cfg.myTempBusAddrRange).getWidth
          )
          //val tempIdx = (
          //  U(s"${tempAddrWidth - 1}'d${idx}")
          //)
          val myIdx = (
            (
              0x2
              << (tempAddrWidth - 1)
            )
            | idx
          )
          is (
            //Cat(
            //  U"2'b10",    // IrqId
            //  tempIdx
            //)
            myIdx
          ) {
            io.bus.d2hBus.data := rIrqIdBusRegVec(
              //tempIdx
              idx
            )

            // need to clear this bus register upon it being read!
            rIrqIdBusRegVec(idx) := 0x0
            myTempIrqIdAsBits := 0x0

            io.dstIrq.nextValid := (
              False
            )
          }
        }
        for (idx <- 0 until numBusRegsPerKind) {
          val tempAddrWidth = (
            rSavedH2dPayload.addr(cfg.myTempBusAddrRange).getWidth
          )
          //val tempIdx = (
          //  U(s"${tempAddrWidth - 1}'d${idx}")
          //)
          val myIdx = (
            (
              0x3
              << (tempAddrWidth - 1)
            )
            | idx
          )
          is (
            //Cat(
            //  U"2'b11",    // IrqEnable
            //  tempIdx
            //)
            myIdx
          ) {
            io.bus.d2hBus.data := rIrqEnableBusRegVec(
              //tempIdx
              idx
            )
          }
        }
        default {
        }
      }
      
      io.bus.d2hBus.valid := True
      io.bus.d2hBus.src := rSavedH2dPayload.src
      when (io.bus.d2hBus.fire) {
        rState := State.IDLE
      }
    }
    is (State.BUS_WR) {
      switch (
        //rSavedH2dPayload.isWrite
        //## 
        rSavedH2dPayload.addr(cfg.myTempBusAddrRange)
      ) {
        //is (False) {
        //}
        //is (True) {
        //}
        for (idx <- 0 until numBusRegsPerKind) {
          val tempAddrWidth = (
            rSavedH2dPayload.addr(cfg.myTempBusAddrRange).getWidth
          )
          val tempIdx = (
            U(s"${tempAddrWidth - 1}'d${idx}")
          )
          //is (
          //  U"1'b0"    // IrqId
          //  ## tempIdx
          //) {
          //}
          is (
            (
              U"1'b1"    // IrqEnable
              ## tempIdx
            ).asUInt
          ) {
            rIrqEnableBusRegVec(tempIdx) := rSavedH2dPayload.data
          }
        }
        default {
        }
      }

      io.bus.d2hBus.valid := True
      io.bus.d2hBus.src := rSavedH2dPayload.src
      when (io.bus.d2hBus.fire) {
        rState := State.IDLE
      }
    }
    //is (State.BUS_WR) {
    //}
  }

  //if (cfg.kind == LcvBusArbiterKind.Priority) {
  //  when (io.en) {
  //    nextHostIdx := rHostIdx
  //  } otherwise {
  //    nextHostIdx := 0x0
  //  }
  //} else {
  //  nextHostIdx := rHostIdx
  //}
  //--------
  //for (idx <- 0 until cfg.depth) {
  //}
  //--------
}

object LcvBusIrqCtrlTestConfig {
  val cfg = LcvBusIrqCtrlConfig(
    busCfg=LcvBusConfig(
      mainCfg=LcvBusMainConfig(
        dataWidth=32,
        addrWidth=32,
        allowBurst=false,
        burstAlwaysMaxSize=false,
        srcWidth=1,
        haveByteEn=true,
        keepByteSize=false,
      ),
      cacheCfg=None,
    ),
    depth=(
      //8
      //33
      65
    ),
    //kind=LcvBusArbiterKind.Priority,
  )
}
object LcvBusIrqCtrlSpinalConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    )
  )
}
object LcvBusIrqCtrlToVerilog extends App {
  LcvBusIrqCtrlSpinalConfig.spinal.generateVerilog{
    LcvBusIrqCtrl(cfg=LcvBusIrqCtrlTestConfig.cfg)
  }
}
