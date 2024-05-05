//package libcheesevoyage.bus.tilelink
//
//import spinal.core._
//import spinal.core.formal._
//import spinal.lib._
//import spinal.lib.misc.pipeline._
//import spinal.lib.bus.tilelink
//import spinal.lib.bus.amba4.axi._
//import spinal.core.fiber.Fiber
//import scala.collection.mutable.ArrayBuffer
//import scala.math._
//import libcheesevoyage.general._
//
////case class TestTilelinkIo(
////  dataWidth: Int,
////  addrWidth: Int,
////) extends Bundle {
////}
////
////class TestRamFiber() extends Area {
////  val up = tilelink.fabric.Node.up()
////
////  val thread = Fiber build new Area {
////    // Here the supported parameters are function of what the host would
////    // like us to idealy support.
////    // okay, so Game Mode on this TV is even betterThe tilelink.Ram support
////    // all addressWidth / dataWidth / burst length / get / put accesses
////    // but doesn't support atomic / coherency.
////    // So okay, so Game Mode on this TV is even betterwe take what is proposed to use and restrict it to all sorts of
////    // get / put request
////    up.m2s.supported load up.m2s.proposed.intersect(
////      tilelink.M2sTransfers.allGetPut
////    )
////    up.s2m.none()
////
////    // Here we infer how many bytes our RAM needs to be, by looking at the
////    // memory mapping of the connected hosts
////    val bytes = up.ups.map(
////      e => e.mapping.value.highestBound - e.mapping.value.lowerBound + 1
////    ).max.toInt
////
////    // Then we finally generate the regular hardware
////    val logic = new tilelink.Ram(
////      p=up.bus.p.node,
////      bytes=bytes,
////    )
////    logic.io.up << up.bus
////  }
////}
////
////
////case class TestTilelink(
////  dataWidth: Int,
////  addrWidth: Int,
////) extends Component {
////}
//object TlHelperBusParams {
//  def paramWidth = 3
//  def blankParam = (
//    //Some(
//      B(f"$paramWidth'd0")
//    //)
//    //Some((false, B(f"$paramWidth'd0")))
//  )
//  def someBlankParam = (
//    Some(blankParam)
//  )
//  def blankData(
//    dataWidth: Int
//  ) = (
//    //Some(
//      B(f"$dataWidth'd0")
//    //)
//    //Some((false, B(f"$dataWidth'd0")))
//  )
//  def someBlankData(
//    dataWidth: Int
//  ) = (
//    Some(blankData(dataWidth=dataWidth))
//  )
//}
//case class TlHelperBusParams(
//  addressWidth: Int,
//  dataWidth: Int,
//  sizeBytes: Int,
//  sourceWidth: Int,
//  //sinkWidth: Int,
//  withDataA: Boolean=true,
//  withDataD: Boolean=true,
//) {
//  def mkBusParams: tilelink.BusParameter = tilelink.BusParameter(
//    addressWidth=addressWidth,
//    dataWidth=dataWidth,
//    sizeBytes=sizeBytes,
//    sourceWidth=sourceWidth,
//    sinkWidth=(
//      //sinkWidth
//      0
//    ),
//    withBCE=false,
//    withDataA=withDataA,
//    withDataB=false,
//    withDataC=false,
//    withDataD=withDataD,
//    node=null,
//  )
//  def fullMask = B(dataWidth / 8 bits, default -> True)
//  def paramWidth = (
//    //3
//    TlHelperBusParams.paramWidth
//  )
//  def blankParam = (
//    //Some(B(f"$paramWidth'd0"))
//    ////Some((false, B(f"$paramWidth'd0")))
//    TlHelperBusParams.blankParam
//  )
//  def someBlankParam = (
//    TlHelperBusParams.someBlankParam
//  )
//  def blankData = (
//    //Some(B(f"$dataWidth'd0"))
//    ////Some((false, B(f"$dataWidth'd0")))
//    TlHelperBusParams.blankData(dataWidth=dataWidth)
//  )
//  def someBlankData = (
//    TlHelperBusParams.someBlankData(dataWidth=dataWidth)
//  )
//}
////object MkTlUhHostOp {
////  def apply(
////    helperP: TlHelperBusParams,
////    opcode: Bits,
////    address: UInt,
////    size: UInt,
////    source: UInt,
////    mask: Bits,
////    alignAddress: Boolean,
////    corrupt: Bool=False,
////    param: Option[(Boolean, Bits)]=(
////      Some(false, TlHelperBusParams.blankParam)
////    ),
////    data: Option[(Boolean, Bits)]=Some(false, B"1'd0"),
////    //corrupt: Option[(Boolean, Bool)]=Some(false, False),
////  ) = {
////    TlHostOp(
////      opcode=opcode,
////      param=param match {
////        case Some((false, myParam)) => {
////          //Some(B"3'd0")
////          helperP.someBlankParam
////        }
////        case Some((true, myParam)) => {
////          Some(myParam)
////        }
////        case None => {
////          None
////        }
////      },
////      address=address,
////      size=size,
////      source=source,
////      mask=mask,
////      corrupt=corrupt,
////      //corrupt=corrupt match {
////      //  case Some((false, myCorrupt)) => {
////      //    Some(False),
////      //  }
////      //  case Some((true, myCorrupt)) => {
////      //    Some(myCorrupt)
////      //  }
////      //  case None => {
////      //    None
////      //  }
////      //},
////      data=data match {
////        case Some((false, myData)) => {
////          //Some(B{
////          //  def tempWidth = helperP.dataWidth
////          //  f"$tempWidth'd0"
////          //}),
////          helperP.someBlankData
////        }
////        case Some((true, myData)) => {
////          Some(myData)
////        }
////        case None => {
////          None
////        }
////      },
////      alignAddress=alignAddress,
////    )
////  }
////}
////case class TlHostOp(
////  //bus: tilelink.Bus,
////  //helperP: TlHelperBusParams,
////  opcode: Bits,
////  param: Option[Bits],
////  address: UInt,
////  size: UInt,
////  source: UInt,
////  mask: Bits,
////  corrupt: Bool,
////  data: Option[Bits],
////  alignAddress: Boolean,
////) {
////  def setBus(
////    bus: tilelink.Bus,
////    //opcode: Bits,
////    //alignAddress: Boolean,
////  ): Unit = {
////    bus.a.opcode.assignFromBits(opcode)
////    param match {
////      case Some(myParam) => {
////        bus.a.param := myParam
////      }
////      case None => {
////      }
////    }
////    bus.a.address := (
////      if (alignAddress) {
////        //((address >> size) << size).resized
////        //(address & ((1 << size) - 1)).resized
////        val tempMask = {
////          def addrWidth = address.getWidth
////          def tempOne = U{f"$addrWidth'd1"}
////          ~((tempOne << size) - tempOne)
////        }
////        address & tempMask
////        //(
////        //  address
////        //  //& Cat().asUInt
////        //  & (1 << size)
////        //)
////      } else { // if (!alignAddress)
////        address
////      }
////    )
////    bus.a.size := size
////    bus.a.source := source
////    bus.a.mask := mask
////    //corrupt match {
////    //  case Some(myCorrupt) => {
////    //    bus.a.corrupt := myCorrupt
////    //  }
////    //  case None => {
////    //  }
////    //}
////    bus.a.corrupt := corrupt
////    data match {
////      case Some(myData) => {
////        bus.a.data := myData
////      }
////      case None => {
////      }
////    }
////  }
////}
////object MkTlUhDeviceOp {
////  def apply(
////    helperP: TlHelperBusParams,
////    opcode: Bits,
////    size: UInt,
////    source: UInt,
////    sink: UInt,
////    denied: Bool,
////    corrupt: Bool=False,
////    param: Option[(Boolean, Bits)]=Some(false, B"3'd0"),
////    //corrupt: Option[(Boolean, Bool)]=Some(false, False),
////    data: Option[(Boolean, Bits)]=Some(false, B"1'd0"),
////  ) = {
////    TlDeviceOp(
////      opcode=opcode,
////      param=param match {
////        case Some((false, myParam)) => {
////          //Some(B"3'd0")
////          helperP.someBlankParam
////        }
////        case Some((true, myParam)) => {
////          Some(myParam)
////        }
////        case None => {
////          None
////        }
////      },
////      size=size,
////      source=source,
////      sink=sink,
////      denied=denied,
////      //corrupt=corrupt match {
////      //  case Some((false, myCorrupt)) => {
////      //    Some(False),
////      //  }
////      //  case Some((true, myCorrupt)) => {
////      //    Some(myCorrupt)
////      //  }
////      //  case None => {
////      //    None
////      //  }
////      //},
////      data=data match {
////        case Some((false, myData)) => {
////          //Some(B{
////          //  def tempWidth = helperP.dataWidth
////          //  f"$tempWidth'd0"
////          //}),
////          helperP.someBlankData
////        }
////        case Some((true, myData)) => {
////          Some(myData)
////        }
////        case None => {
////          None
////        }
////      },
////      corrupt=corrupt,
////    )
////  }
////}
////case class TlDeviceOp(
////  opcode: Bits,
////  param: Option[Bits],
////  size: UInt,
////  source: UInt,
////  sink: UInt,
////  denied: Bool,
////  data: Option[Bits],
////  corrupt: Bool,
////  //corrupt: Option[Bool],
////) {
////  def setBus(
////    bus: tilelink.Bus,
////  ): Unit = {
////    bus.d.opcode.assignFromBits(opcode)
////    param match {
////      case Some(myParam) => {
////        bus.d.param := myParam
////      }
////      case None => {
////      }
////    }
////    bus.d.size := size
////    bus.d.source := source
////    bus.d.sink := sink
////    bus.d.denied := denied
////    data match {
////      case Some(myData) => {
////        bus.d.data := myData
////      }
////      case None => {
////      }
////    }
////    bus.d.corrupt := corrupt
////  }
////}
//
//case class TlHelperPipePayload(
//  val helperP: TlHelperBusParams,
//  val isHost: Boolean,
//  val isSend: Boolean,
//  val txnSourceWidth: Int,
//) extends Bundle {
//  //--------
//  assert(txnSourceWidth <= helperP.sourceWidth)
//  assert(txnSourceWidth > 0)
//  //--------
//  val p = helperP.mkBusParams
//  //--------
//  val bus = new Bundle {
//    //val a = (
//    //  // (isHost && isSend)
//    //  // || (!isHost && !isSend)
//    //  isHost == isSend
//    //) generate {
//    //  tilelink.ChannelA(p=p)
//    //}
//    //val d = (
//    //  // (isHost && !isSend)
//    //  // || (!isHost && isSend)
//    //  isHost != isSend
//    //) generate {
//    //  tilelink.ChannelD(p=p)
//    //}
//    val a = (isHost == isSend) generate new Bundle {
//      val inp = tilelink.ChannelA(p=p)
//      def mkInpFlow(
//        valid: Bool=True,
//      ) = {
//        val ret = Flow(tilelink.ChannelA(p=p))
//        ret.valid := valid
//        ret.payload := inp
//        ret
//      }
//      val rd, wr = Flow(tilelink.ChannelA(p=p))
//    }
//    val d = (isHost != isSend) generate new Bundle {
//      val inp = tilelink.ChannelD(p=p)
//
//      def mkInpFlow(
//        valid: Bool=True,
//      ) = {
//        val ret = Flow(tilelink.ChannelD(p=p))
//        ret.valid := valid
//        ret.payload := inp
//        ret
//      }
//      val rd, wr = Flow(tilelink.ChannelD(p=p))
//    }
//  }
//  val valid = Bool()
//  //val host = (isHost) generate new Bundle {
//  //  val send = (isSend) generate new Bundle {
//  //    //def busChan = busPayload.inpA
//  //    def inpChan = busPayload.a.inp
//  //  }
//  //  val recv = (!isSend) generate new Bundle {
//  //    def busChan = busPayload.d.inp
//  //  }
//  //}
//  //val device = (!isHost) generate new Bundle {
//  //  val send = (isSend) generate new Bundle {
//  //    //def busChan = busPayload.d.inp
//  //  }
//  //  val recv = (!isSend) generate new Bundle {
//  //    def busChan = busPayload.a.inp
//  //  }
//  //}
//  //--------
//}
//
//object TlHelperPipePayload {
//  def mkHostSend(
//    helperP: TlHelperBusParams,
//    txnSourceWidth: Int,
//  ) = new TlHelperPipePayload(
//    helperP=helperP,
//    isHost=true,
//    isSend=true,
//    txnSourceWidth=txnSourceWidth,
//  )
//  def mkHostRecv(
//    helperP: TlHelperBusParams,
//    txnSourceWidth: Int,
//  ) = new TlHelperPipePayload(
//    helperP=helperP,
//    isHost=true,
//    isSend=false,
//    txnSourceWidth=txnSourceWidth,
//  )
//  def mkDeviceSend(
//    helperP: TlHelperBusParams,
//    txnSourceWidth: Int,
//  ) = new TlHelperPipePayload(
//    helperP=helperP,
//    isHost=false,
//    isSend=true,
//    txnSourceWidth=txnSourceWidth,
//  )
//  def mkDeviceRecv(
//    helperP: TlHelperBusParams,
//    txnSourceWidth: Int,
//  ) = new TlHelperPipePayload(
//    helperP=helperP,
//    isHost=false,
//    isSend=false,
//    txnSourceWidth=txnSourceWidth,
//  )
//}
//
//case class TlHelper(
//  //p: tilelink.BusParameter,
//  helperP: TlHelperBusParams,
//  isHost: Boolean,
//  //isSend: Boolean,
//  //source: Int,
//  //fifoDepth: Int,
//  //maxOutstandingTxns: Int,
//  txnSourceWidth: Int,
//) extends Area {
//  //--------
//  assert(txnSourceWidth <= helperP.sourceWidth)
//  assert(txnSourceWidth > 0)
//  //--------
//  val p = helperP.mkBusParams
//  //assert(!p.withBCE)
//  //val fire = Bool()
//  //val extSendValid = Bool() //Reg(Bool()) init(False)
//  //--------
//  //val testficate = Axi4ToTilelink
//  //val bridge = tilelink.Axi4Bridge
//  val bus = tilelink.Bus(p=p)
//  //if (isHost) {
//  //  bus.a.valid.setAsReg() init(False)
//  //  //bus.a.source.setAsReg() init(0x0)
//  //  bus.d.ready.setAsReg() init(False)
//  //} else { // if (!isHost)
//  //  bus.a.ready.setAsReg() init(False)
//  //  bus.d.valid.setAsReg() init(False)
//  //  //bus.d.source.setAsReg() init(0x0)
//  //}
//  //
//  val loc = new Area {
//    def busMemDepth = (1 << txnSourceWidth)
//    //val busAMem = Mem(
//    //  wordType=tilelink.ChannelA(p=p),
//    //  wordCount=busMemDepth,
//    //)
//    //val busDMem = Mem(
//    //  wordType=tilelink.ChannelD(p=p),
//    //  wordCount=busMemDepth
//    //)
//    //val opcodeA = tilelink.Opcode.A()
//    //val opcodeD = tilelink.Opcode.D()
//    def sendMemArrIdx = 0
//    def recvMemArrIdx = 1
//    def numMemArrs = 2
//
//    val linkArr = PipeHelper.mkLinkArr()
//
//    val host = (isHost) generate new Area {
//      def numSendRdWrStages = 2
//      val send = new Area {
//        def busChan = bus.a
//        //val busMemA2d = Array.fill(numMemArrs)(
//        //  Array.fill(numSendRdWrStages)(
//        //    Mem(
//        //      wordType=Flow(tilelink.ChannelA(p=p)),
//        //      wordCount=busMemDepth,
//        //    )
//        //  )
//        //)
//        def busMemWordType() = Flow(tilelink.ChannelA(p=p))
//        def busMemModStageCnt = 1 // Tentative!
//        def busMemModType() = (
//          SamplePipeMemRmwModType[Flow[tilelink.ChannelA]](
//            wordType=busMemWordType(),
//            wordCount=busMemDepth,
//            modStageCnt=busMemModStageCnt,
//          )
//        )
//        /*val busMemArr = Array.fill(numMemArrs)*/
//        val busMem = ({
//          /*Array.fill(numSendRdWrStages)*/(
//            PipeMemRmw[
//              Flow[tilelink.ChannelA],                          // WordT
//              SamplePipeMemRmwModType[Flow[tilelink.ChannelA]], // ModT
//              SamplePipeMemRmwModType[Flow[tilelink.ChannelA]], // DualRdT
//            ](
//              wordType=busMemWordType(),
//              wordCount=busMemDepth,
//              modType=busMemModType(),
//              modStageCnt=busMemModStageCnt,
//              //memArrIdx=0,
//              dualRdType=busMemModType(),
//              optDualRd=true,
//              forFmax=true,
//            )
//          )
//        })
//        val pipe = PipeHelper(linkArr=linkArr)
//        def mkPipePayload() = TlHelperPipePayload.mkHostSend(
//          helperP=helperP,
//          txnSourceWidth=txnSourceWidth,
//        )
//        val front = Stream(mkPipePayload())
//        val pipePayload = Payload(mkPipePayload())
//
//        //val cReadBusMem = pipe.addStage("HostSendReadBusMem")
//        ////val cReadBusMemFf1 = pipe.addPipeStage()
//        //val cWriteBusMem = pipe.addStage("HostSendWriteBusMem")
//        ////val cWriteBusMemFf1 = pipe.addPipeStage()
//
//        pipe.first.up.driveFrom(front)(
//          con=(node, payload) => {
//            node(pipePayload) := payload
//          }
//        )
//        pipe.last.down.driveTo(bus.a)(
//          con=(payload, node) => {
//            payload := node(pipePayload).bus.a.inp
//          }
//        )
//      }
//      val recv = new Area {
//        def busChan = bus.d
//        //val busMemArr = Array.fill(numMemArrs)(
//        //  Mem(
//        //    wordType=Flow(tilelink.ChannelD(p=p)),
//        //    wordCount=busMemDepth,
//        //  )
//        //)
//        def busMemWordType() = Flow(tilelink.ChannelD(p=p))
//        def busMemModStageCnt = 1 // Tentative!
//        def busMemModType() = (
//          SamplePipeMemRmwModType[Flow[tilelink.ChannelD]](
//            wordType=busMemWordType(),
//            wordCount=busMemDepth,
//            modStageCnt=busMemModStageCnt,
//          )
//        )
//        val busMemArr = Array.fill(numMemArrs)({
//          /*Array.fill(numSendRdWrStages)*/(
//            PipeMemRmw[
//              Flow[tilelink.ChannelD],                          // WordT
//              SamplePipeMemRmwModType[Flow[tilelink.ChannelD]], // ModT
//              SamplePipeMemRmwModType[Flow[tilelink.ChannelD]],
//                // DualRdT
//            ](
//              wordType=busMemWordType(),
//              wordCount=busMemDepth,
//              modType=busMemModType(),
//              modStageCnt=busMemModStageCnt,
//              //memArrIdx=0,
//              dualRdType=busMemModType(),
//              optDualRd=true,
//              forFmax=true,
//            )
//          )
//        })
//        val pipe = PipeHelper(linkArr=linkArr)
//        def mkPipePayload() = TlHelperPipePayload.mkHostRecv(
//          helperP=helperP,
//          txnSourceWidth=txnSourceWidth,
//        )
//        val back = Stream(mkPipePayload())
//        val pipePayload = Payload(mkPipePayload())
//
//        //val cReadBusMem = pipe.addStage("HostRecvReadBusMem")
//        ////val cReadBusMemFf1 = pipe.addPipeStage()
//        //val cWriteBusMem = pipe.addStage("HostRecvWriteBusMem")
//        ////val cWriteBusMemFf1 = pipe.addPipeStage()
//
//        pipe.first.up.driveFrom(bus.d)(
//          con=(node, payload) => {
//            node(pipePayload) := node(pipePayload).getZero
//            node(pipePayload).allowOverride
//            node(pipePayload).bus.d.inp := payload
//          }
//        )
//        pipe.last.down.driveTo(back)(
//          con=(payload, node) => {
//            payload := node(pipePayload)
//          }
//        )
//
//        //def intf = pipe.last
//
//        //pipe.first.up.driveFrom(front)(
//        //  con=(node, payload) => {
//        //    node(pipePayload) := payload
//        //  }
//        //)
//      }
//      //--------
//      // BEGIN: new code
//      //val sendReadBusMem = new send.cReadBusMem.Area {
//      //  def myBus = send.pipePayload.bus
//      //  //for (idx <- 0 until numSendRdWrStages) {
//      //  //  myBus.a.rd := (
//      //  //    send.busMemA2d(sendMemArrIdx)(idx).readSync(
//      //  //      address=myBus.a.inp.source,
//      //  //      //enable=isReady,
//      //  //    )
//      //  //  )
//      //  //}
//      //}
//      // END: new code
//      //--------
//      //val sendReadBusMemFf1 = new send.cReadBusMemFf1.Area {
//      //  //when (down.isFiring) {
//      //  //} otherwise { // if (!down.isFiring)
//      //  //}
//
//      //  //when (isValid) {
//      //  //} otherwise { // when (!isValid)
//      //  //}
//      //}
//      //--------
//      // BEGIN: new code
//      //val sendWriteBusMem = new send.cWriteBusMem.Area {
//      //  //when 
//      //  //when (down.isFiring) {
//      //  //} otherwise { // if (!down.isFiring)
//      //  //}
//      //  //when (isValid) {
//      //  //} otherwise { // when (!isValid)
//      //  //}
//      //  def myBus = send.pipePayload.bus
//      //  for (stageIdx <- 0 until numSendRdWrStages) {
//      //    //send.busMemA2d(sendMemArrIdx)(stageIdx).write(
//      //    //  address=myBus.a.inp.source,
//      //    //  data=myBus.a.mkInpFlow(),
//      //    //  enable=isValid && isReady,
//      //    //)
//      //  }
//      //}
//      // END: new code
//      //--------
//      //val sendWriteBusMemFf1 = new send.cWriteBusMemFf1.Area {
//      //  //when (down.isFiring) {
//      //  //} otherwise { // if (!down.isFiring)
//      //  //}
//      //  when (isValid) {
//      //  } otherwise { // when (!isValid)
//      //  }
//      //}
//      //val sendDecode = new send.cDecode.Area {
//      //}
//      //val sendExecute = new send.cExecute.Area {
//      //}
//      //--------
//      // BEGIN: new code
//      //val recvReadBusMem = new recv.cReadBusMem.Area {
//      //}
//      // END: new code
//      //--------
//      //val recvFetch = new recv.cFetch.Area {
//      //}
//      //val recvDecode = new recv.cDecode.Area {
//      //}
//      //val recvExecute = new recv.cExecute.Area {
//      //}
//    }
//    //val device = (!isHost) generate new Area {
//    //  val send = new Area {
//    //    def busChan = bus.d
//    //    val busMemArr = Array.fill(numMemArrs)(
//    //      Mem(
//    //        wordType=Flow(tilelink.ChannelD(p=p)),
//    //        wordCount=busMemDepth,
//    //      )
//    //    )
//    //    val pipe = PipeHelper(linkArr=linkArr)
//    //    def mkPipePayload() = TlHelperPipePayload.mkDeviceSend(
//    //      helperP=helperP,
//    //      txnSourceWidth=txnSourceWidth,
//    //    )
//    //    val front = Stream(mkPipePayload())
//    //    val pipePayload = Payload(mkPipePayload())
//
//    //    //val cReadBusMem = pipe.addStage("DeviceSendReadBusMem")
//    //    //val cWriteBusMem = pipe.addStage("DeviceSendWriteBusMem")
//
//    //    pipe.first.up.driveFrom(front)(
//    //      con=(node, payload) => {
//    //        node(pipePayload) := payload
//    //      }
//    //    )
//    //    pipe.last.down.driveTo(bus.d)(
//    //      con=(payload, node) => {
//    //        payload := node(pipePayload).bus.d.inp
//    //      }
//    //    )
//    //  }
//    //  val recv = new Area {
//    //    def busChan = bus.a
//    //    val busMemArr = Array.fill(numMemArrs)(
//    //      Mem(
//    //        wordType=Flow(tilelink.ChannelA(p=p)),
//    //        wordCount=busMemDepth,
//    //      )
//    //    )
//    //    val pipe = PipeHelper(linkArr=linkArr)
//    //    def mkPipePayload() = TlHelperPipePayload.mkDeviceRecv(
//    //      helperP=helperP,
//    //      txnSourceWidth=txnSourceWidth,
//    //    )
//    //    val back = Stream(mkPipePayload())
//    //    val pipePayload = Payload(mkPipePayload())
//
//    //    //val cReadBusMem = pipe.addStage("DeviceRecvReadBusMem")
//    //    //val cWriteBusMem = pipe.addStage("DeviceRecvWriteBusMem")
//
//    //    pipe.first.up.driveFrom(bus.a)(
//    //      con=(node, payload) => {
//    //        node(pipePayload) := node(pipePayload).getZero
//    //        node(pipePayload).allowOverride
//    //        node(pipePayload).bus.a.inp := payload
//    //      }
//    //    )
//    //    pipe.last.down.driveTo(back)(
//    //      con=(payload, node) => {
//    //        payload := node(pipePayload)
//    //      }
//    //    )
//    //  }
//    //}
//    //Builder(linkArr.toSeq)
//  }
//  def doBuilder() = {
//    //Builder((sendPipe.linkArr + recvPipe.linkArr).toSeq)
//    Builder(loc.linkArr.toSeq)
//  }
//  //def doBuilder() = {
//  //  if (isHost) {
//  //    //--------
//  //    def tempSend = loc.host.send
//  //    def tempRecv = loc.host.recv
//  //    //--------
//  //    Builder(
//  //      //--------
//  //      tempSend.cWaitfire,
//  //      tempSend.cDecode,
//  //      tempSend.cExecute,
//  //      tempSend.sWaitfireDecode,
//  //      tempSend.sDecodeExecute,
//  //      //--------
//  //      tempRecv.cFetch,
//  //      tempRecv.cDecode,
//  //      tempRecv.cExecute,
//  //      tempRecv.s0,
//  //      tempRecv.s1,
//  //      //--------
//  //    )
//  //    //--------
//  //  } else { // if (!isHost)
//  //    //--------
//  //    def tempSend = loc.device.send
//  //    def tempRecv = loc.device.recv
//  //    //--------
//  //    Builder(
//  //      //--------
//  //      tempSend.cFetch,
//  //      tempSend.cDecode,
//  //      tempSend.cExecute,
//  //      tempSend.sFetchDecode,
//  //      tempSend.sDecodeExecute,
//  //      //--------
//  //      tempRecv.cFetch,
//  //      tempRecv.cDecode,
//  //      tempRecv.cExecute,
//  //      tempRecv.sFetchDecode,
//  //      tempRecv.sDecodeExecute,
//  //      //--------
//  //    )
//  //    //--------
//  //  }
//  //}
//  //val locHost = (isHost) generate new Area {
//  //}
//  //val locDev = (!isHost) generate new Area {
//  //}
//  //--------
//  // BEGIN: need to translate
//  //val loc = new Area {
//  //  def busMemDepth = (1 << txnSourceWidth)
//  //  val busAMem = Mem(
//  //    wordType=tilelink.ChannelA(p=p),
//  //    wordCount=busMemDepth,
//  //  )
//
//  //  //when (bus.a.fire) {
//  //  //  busAMem.write(
//  //  //    enable=bus.a.fire
//  //  //  )
//  //  //}
//  //  //opcodeA.assignFromBits(bus.a.opcode.asBits)
//
//  //  //val rSavedBusA = Reg(cloneOf(bus.a.payload))
//  //  //rSavedBusA.init(rSavedBusA.getZero)
//  //  //def savedOpcodeA = {
//  //  //  tilelink.Opcode.A().assignFromBits(rSavedBusA.opcode.asBits)
//  //  //}
//
//  //  val busDMem = Mem(
//  //    wordType=tilelink.ChannelD(p=p),
//  //    wordCount=busMemDepth,
//  //  )
//  //  //if (isHost) {
//  //  //} else { // if (!isHost)
//  //  //}
//  //  val (sendBusMem, recvBusMem) = (
//  //    if (isHost) {
//  //      (busAMem, busDMem) // (sendBusMem, recvBusMem)
//  //    } else { // if (!isHost)
//  //      (busDMem, busAMem) // (sendBusMem, recvBusMem)
//  //    }
//  //  )
//  //  val (sendBus, recvBus) = (
//  //    if (isHost) {
//  //      (bus.a, bus.d) // (sendBus, recvBus)
//  //    } else { // if (!isHost)
//  //      (bus.d, bus.a) // (sendBus, recvBus)
//  //    }
//  //  )
//  //  //val recvBusMem = if (isHost) { busDMem } else { busAMem }
//  //  //val locSend = new Area {
//  //  //}
//  //  val opcodeA = tilelink.Opcode.A()
//  //  val opcodeD = tilelink.Opcode.D()
//  //  val (sendOpcode, recvOpcode) = (
//  //    if (isHost) {
//  //      (opcodeA, opcodeD)
//  //    } else {
//  //      (opcodeD, opcodeA)
//  //    }
//  //  )
//  //}
//  // END: need to translate
//  //--------
//  //opcodeD.assignFromBits(bus.d.opcode.asBits)
//
//  //savedBusDMem.write(
//  //  enable=bus.d.fire
//  //)
//  ////if (isHost) {
//  //} else { // if (!isHost)
//  //}
//
//  //val rSavedBusD = Reg(cloneOf(bus.d.payload))
//  //rSavedBusD.init(rSavedBusD.getZero)
//  //def savedOpcodeD = {
//  //  tilelink.Opcode.D().assignFromBits(rSavedBusD.opcode.asBits)
//  //}
//
//  //if (isHost) {
//  //  when (bus.d.fire) {
//  //    //rSavedOpcodeD := opcodeD
//  //    rSavedBusD := bus.d.payload
//  //  }
//  //} else { // if (!isHost)
//  //  when (bus.a.fire) {
//  //    //rSavedOpcodeA := opcodeA
//  //    rSavedBusA := bus.a.payload
//  //  }
//  //}
//  //--------
//  def connectBusHost(
//    hostBus: tilelink.Bus,
//  ): Unit = {
//    bus << hostBus
//  }
//  def connectBusDevice(
//    deviceBus: tilelink.Bus,
//  ): Unit = {
//    deviceBus << bus
//  }
//
//  //def chanAPipe
//  //object HostAPipe {
//  //  def start = 0
//  //  def assertValid = 1
//  //  //def waitFire = 2
//  //  def exec = 2
//  //  //def execAndWaitFire = 2
//  //  //def waitResp = 3
//  //  //def waitFire
//  //}
//  //object HostAState extends SpinalEnum(defaultEncoding=binarySequential) {
//  //  val
//  //    idle,
//  //    start,
//  //    checkBurst,
//  //    doBurst,
//  //    end
//  //    = newElement();
//  //}
//
//  //object State extends SpinalEnum(defaultEncoding=binarySequential) {
//  //  val
//  //    idle,
//  //    //assertValid,
//  //    doPipe
//  //    //doNonBurst,
//  //    //doBurst
//  //    //deassertValid
//  //    = newElement();
//  //}
//
//  //val rStateA = Reg(State()) init(State.idle)
//  //val rStateD = Reg(State()) init(State.idle)
//
//  //case class FifoAPayload() extends Bundle {
//  //  val a = tilelink.ChannelA(p=p)
//  //}
//  //val fifo = StreamFifo(
//  //  dataType=(
//  //    if (isHost) {
//  //      tilelink.ChannelA(p=p)
//  //    } else {
//  //      tilelink.ChannelD(p=p)
//  //    }
//  //  ),
//  //  depth=fifoDepth,
//  //  //latency=2,
//  //)
//  //val fifoD = StreamFifo(
//  //  dataType=tilelink.ChannelD(p=p),
//  //  depth=fifoDepth,
//  //  //latency=2,
//  //)
//
//  def run(
//    //sendDoIt: Bool,
//    //opcode: Bits,
//    //address: UInt,
//    //data: Bits,
//    //size: UInt,
//    //source: UInt,
//  ): Unit = {
//    //assert(isHost)
//    //--------
//    //--------
//    //def doIt[
//    //  SendPayloadT <: Data,
//    //  RecvPayloadT <: Data,
//    //](
//    //  //sendState: State.C,
//    //  sendStm: Stream[SendPayloadT],
//    //  //recvState: State.C,
//    //  recvStm: Stream[RecvPayloadT],
//    //): Unit = {
//    //  //var myOpcodeA: tilelink.Opcode.A.E = tilelink.Opcode.A.PUT_FULL_DATA
//    //  //def myOpcodeD = (
//    //  //  tilelink.Opcode.hostToDevMap(myOpcodeA)
//    //  //)
//    //  //val hostToDevMap = Map[tilelink.Opcode.A.E, tilelink.Opcode.D.E](
//    //  //  tilelink.Opcode.A.PUT_FULL_DATA -> tilelink.Opcode.D.ACCESS_ACK,
//    //  //  tilelink.Opcode.A.PUT_PARTIAL_DATA -> tilelink.Opcode.D.ACCESS_ACK,
//    //  //  tilelink.Opcode.A.GET -> tilelink.Opcode.D.ACCESS_ACK_DATA,
//    //  //)
//    //  //val devToHostMap = Map[tilelink.Opcode.D.E, tilelink.Opcode.A.E](
//    //  //  tilelink.Opcode.D.ACCESS_ACK -> tilelink.Opcode.A.PUT_FULL_DATA,
//    //  //  tilelink.Opcode.D.ACCESS_ACK -> tilelink.Opcode.A.PUT_PARTIAL_DATA,
//    //  //  tilelink.Opcode.D.ACCESS_ACK_DATA -> tilelink.Opcode.A.GET,
//    //  //)
//
//    //  //switch (sendState) {
//    //  //  is (State.idle) {
//    //  //    when (sendDoIt) {
//    //  //      sendState := State.doPipe
//    //  //      sendStm.valid := True
//    //  //    }
//    //  //  }
//    //  //  is (State.doPipe) {
//    //      //when (bus.a.fire) {
//    //      //}
//    //      var myOpcodeA: tilelink.Opcode.A.E = (
//    //        tilelink.Opcode.A.PUT_FULL_DATA
//    //      )
//    //      def myOpcodeD = (
//    //        TlOpcode.tlUlHostToDevMap(myOpcodeA)
//    //      )
//    //      def setBusOpcode(): Unit = {
//    //        if (isHost) {
//    //          //bus.a.opcode.assignFromBits(myOpcodeA.asBits)
//    //          bus.a.opcode := myOpcodeA
//    //        } else { // if (!isHost)
//    //          //bus.d.opcode.assignFromBits(myOpcodeD.asBits)
//    //          bus.d.opcode := myOpcodeD
//    //        }
//    //      }
//    //      when (sendStm.fire) {
//    //        //loc.sendBusMem.write(
//    //        //  addr=
//    //        //)
//    //        switch (
//    //          loc.opcodeA
//    //        ) {
//    //          is (tilelink.Opcode.A.PUT_FULL_DATA) {
//    //            myOpcodeA = tilelink.Opcode.A.PUT_FULL_DATA
//    //            setBusOpcode()
//    //          }
//    //          is (tilelink.Opcode.A.PUT_PARTIAL_DATA) {
//    //            myOpcodeA = tilelink.Opcode.A.PUT_PARTIAL_DATA
//    //            setBusOpcode()
//    //          }
//    //          //is (tilelink.Opcode.A.ARITHMETIC_DATA) {
//    //          //  myOpcodeA = tilelink.Opcode.A.ARITHMETIC_DATA
//    //          //  setBusOpcode()
//    //          //}
//    //          //is (tilelink.Opcode.A.LOGICAL_DATA) {
//    //          //  myOpcodeA = tilelink.Opcode.A.LOGICAL_DATA
//    //          //  setBusOpcode()
//    //          //}
//    //          is (tilelink.Opcode.A.GET) {
//    //            myOpcodeA = tilelink.Opcode.A.GET
//    //            setBusOpcode()
//    //          }
//    //          //is (tilelink.Opcode.A.INTENT) {
//    //          //  myOpcodeA = tilelink.Opcode.A.INTENT
//    //          //  setBusOpcode()
//    //          //}
//    //          default {
//    //            if (isHost) {
//    //              bus.a.opcode := bus.a.opcode.getZero
//    //            } else { // if (!isHost)
//    //              bus.d.opcode := bus.d.opcode.getZero
//    //            }
//    //          }
//    //        }
//    //      }
//    //  //  }
//    //  //}
//    //  //switch (recvState) {
//    //  //  is (State.idle) {
//    //  //    when (recvStm.valid) {
//    //  //    } otherwise { // when (!recvStm.valid)
//    //  //    }
//    //  //  }
//    //  //  is (State.doPipe) {
//    //  when (recvStm.valid) {
//    //    switch (
//    //      loc.opcodeA // TODO: this needs changed!
//    //    ) {
//    //      is (tilelink.Opcode.A.PUT_FULL_DATA) {
//    //        myOpcodeA = tilelink.Opcode.A.PUT_FULL_DATA
//    //        if (isHost) {
//    //        } else { // if (!isHost)
//    //        }
//    //      }
//    //      is (tilelink.Opcode.A.PUT_PARTIAL_DATA) {
//    //        myOpcodeA = tilelink.Opcode.A.PUT_PARTIAL_DATA
//    //        if (isHost) {
//    //        } else { // if (!isHost)
//    //        }
//    //      }
//    //      //is (tilelink.Opcode.A.ARITHMETIC_DATA) {
//    //      //  myOpcodeA = tilelink.Opcode.A.ARITHMETIC_DATA
//    //      //  if (isHost) {
//    //      //  } else { // if (!isHost)
//    //      //  }
//    //      //}
//    //      //is (tilelink.Opcode.A.LOGICAL_DATA) {
//    //      //  myOpcodeA = tilelink.Opcode.A.LOGICAL_DATA
//    //      //  if (isHost) {
//    //      //  } else { // if (!isHost)
//    //      //  }
//    //      //}
//    //      is (tilelink.Opcode.A.GET) {
//    //        myOpcodeA = tilelink.Opcode.A.GET
//    //        if (isHost) {
//    //        } else { // if (!isHost)
//    //        }
//    //      }
//    //      //is (tilelink.Opcode.A.INTENT) {
//    //      //  myOpcodeA = tilelink.Opcode.A.INTENT
//    //      //  if (isHost) {
//    //      //  } else { // if (!isHost)
//    //      //  }
//    //      //}
//    //      default {
//    //      }
//    //    }
//    //  }
//    //  //  }
//    //  //}
//    //}
//    //if (isHost) {
//    //  doIt(
//    //    //sendState=rStateA,
//    //    sendStm=bus.a,
//    //    //recvState=rStateD,
//    //    recvStm=bus.d,
//    //  )
//    //} else { // if (!isHost)
//    //  doIt(
//    //    //sendState=rStateD,
//    //    sendStm=bus.d,
//    //    //recvState=rStateA,
//    //    recvStm=bus.a,
//    //  )
//    //}
//    //--------
//  }
//  //def deviceBurst(
//  //  address: UInt,
//  //  data: Bits,
//  //  size: UInt,
//  //  source: UInt,
//  //  alignAddress: Boolean,
//  //)(
//  //): Unit = {
//  //  object BurstState extends SpinalEnum(
//  //    defaultEncoding=binarySequential
//  //  ) {
//  //    val
//  //      idle
//  //      = newElement;
//  //  }
//  //}
//}
