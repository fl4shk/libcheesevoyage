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
//import spinal.core.sim._
//import spinal.lib.sim.{
//  StreamMonitor, StreamDriver, StreamReadyRandomizer,ScoreboardInOrder
//}
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
//case class TlHelperPipePayloadExtras(
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
//  //val bus = new Bundle {
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
//      //val bus = Flow(tilelink.ChannelA(p=p))
//      val bus = tilelink.ChannelA(p=p)
//      //val burstCnt = 
//    }
//    val d = (isHost != isSend) generate new Bundle {
//      //val bus = Flow(tilelink.ChannelD(p=p))
//      val bus = tilelink.ChannelD(p=p)
//      //val beat = p.beat()
//    }
//    //val a = (isHost == isSend) generate new Bundle {
//    //  // `PUT` burst information is included in `front`
//    //  // `GET` burst information is included in `front`
//    //  val outp = tilelink.ChannelA(p=p)
//
//    //  //val back = tilelink.ChannelA(p=p)
//
//    //  //val frontBurst = p.withDataA generate new Bundle {
//    //  //  val mask = p.mask()
//    //  //  val data = p.data()
//    //  //}
//    //  //def mkInpFlow(
//    //  //  valid: Bool=True,
//    //  //) = {
//    //  //  val ret = Flow(tilelink.ChannelA(p=p))
//    //  //  ret.valid := valid
//    //  //  ret.payload := inp
//    //  //  ret
//    //  //}
//    //  //val rd, wr = Flow(tilelink.ChannelA(p=p))
//    //  //val outp = new Bundle {
//    //  //}
//    //}
//    //val d = (isHost != isSend) generate new Bundle {
//    //  val inp = tilelink.ChannelD(p=p)
//
//    //  //def mkInpFlow(
//    //  //  valid: Bool=True,
//    //  //) = {
//    //  //  val ret = Flow(tilelink.ChannelD(p=p))
//    //  //  ret.valid := valid
//    //  //  ret.payload := inp
//    //  //  ret
//    //  //}
//
//    //  //val outp = new Bundle {
//    //  //  // `opcode` is included just in case the main host wants it
//    //  //  val opcode = tilelink.Opcode.D() 
//    //  //  // `param` must be zero, so we ignore it for `outp`
//    //  //  val source = p.source()
//    //  //  val size = p.size()
//    //  //  // `sink` is ignored, so we ignore it for `outp`
//    //  //  val denied = Bool()
//    //  //  val data = p.withDataD generate p.data()
//    //  //  val corrupt = p.withDataD generate Bool()
//    //  //}
//    //  //val back = tilelink.ChannelD(p=p)
//
//    //  //val rd, wr = Flow(tilelink.ChannelD(p=p))
//    //}
//    //val a = (isHost == isSend) generate Flow(tilelink.ChannelA(p=p))
//    //val d = (isHost == isSend) generate Flow(tilelink.ChannelD(p=p))
//  //}
//  //val valid = Bool()
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
//object TlHelperPipePayloadExtras {
//  def mkHostSend(
//    helperP: TlHelperBusParams,
//    txnSourceWidth: Int,
//  ) = new TlHelperPipePayloadExtras(
//    helperP=helperP,
//    isHost=true,
//    isSend=true,
//    txnSourceWidth=txnSourceWidth,
//  )
//  def mkHostRecv(
//    helperP: TlHelperBusParams,
//    txnSourceWidth: Int,
//  ) = new TlHelperPipePayloadExtras(
//    helperP=helperP,
//    isHost=true,
//    isSend=false,
//    txnSourceWidth=txnSourceWidth,
//  )
//  def mkDeviceSend(
//    helperP: TlHelperBusParams,
//    txnSourceWidth: Int,
//  ) = new TlHelperPipePayloadExtras(
//    helperP=helperP,
//    isHost=false,
//    isSend=true,
//    txnSourceWidth=txnSourceWidth,
//  )
//  def mkDeviceRecv(
//    helperP: TlHelperBusParams,
//    txnSourceWidth: Int,
//  ) = new TlHelperPipePayloadExtras(
//    helperP=helperP,
//    isHost=false,
//    isSend=false,
//    txnSourceWidth=txnSourceWidth,
//  )
//}
//
////object TlHostLink {
////  def apply(
////    upA: Node, upD: Node, down: Node
////  ): TlHostLink = (
////    new TlHostLink(
////      upA=upA,
////      upD=upD,
////      down=down,
////    )
////  )
////}
//
//case class TlHostLink(
//  //val payloadA: Payload[PayloadT],
//  //val payloadD: Payload[PayloadT],
//  val upA: Node,
//  val upD: Node,
//  val down: Node,
//)(
//  compareFunc: //Option[
//    (
//      Node,     // upA
//      Node,     // upD
//      //Boolean,  // checkDValid
//    ) => Bool
//  //]=None
//)
//extends Link {
//  this.ups.foreach(_.down = this)
//  down.up = this
//
//  //val rPrevGnt = Reg(UInt(1 bits)) init(0x0)
//  object GntKind extends SpinalEnum(defaultEncoding=binarySequential) {
//    val
//      A,
//      D
//      = newElement();
//  }
//  //val rGnt = Reg(GntKind()) init(GntKind.A)
//  val currGnt = GntKind()
//  val nextGnt = GntKind()
//  //val rGnt = RegNext(nextGnt) init(GntKind.A)
//  val rGnt = (
//    RegNextWhen(nextGnt, upA.isFiring || upD.isFiring)
//    init(GntKind.A)
//  )
//  currGnt := rGnt
//  nextGnt := rGnt
//  //def doSwitch(
//  //  myCat: Bits,
//  //  isCurr: Boolean
//  //): Unit = {
//  //  switch (myCat) {
//  //    is (M"00-") {
//  //    }
//  //    is (M"10-") {
//  //      if (isCurr) {
//  //        currGnt := GntKind.A
//  //      } else {
//  //        nextGnt := GntKind.D
//  //      }
//  //    }
//  //    is (M"01-") {
//  //      if (isCurr) {
//  //        currGnt := GntKind.D
//  //      } else {
//  //        nextGnt := GntKind.A
//  //      }
//  //    }
//  //    is (M"110") {
//  //      when (doCompare()) {
//  //        if (isCurr) {
//  //          currGnt := GntKind.A
//  //        } else {
//  //          nextGnt := GntKind.D
//  //        }
//  //      } otherwise {
//  //        if (isCurr) {
//  //          currGnt := GntKind.D
//  //        } else {
//  //          nextGnt := GntKind.A
//  //        }
//  //      }
//  //    }
//  //    is (M"111") {
//  //      when (doCompare()) {
//  //        if (isCurr) {
//  //          currGnt := GntKind.D
//  //        } else {
//  //          nextGnt := GntKind.A
//  //        }
//  //      } otherwise {
//  //        if (isCurr) {
//  //          currGnt := GntKind.A
//  //        } else {
//  //          nextGnt := GntKind.D
//  //        }
//  //      }
//  //    }
//  //    //is (M"111") {
//  //    //  //nextGnt := GntKind.A
//  //    //  nextGnt := Mux[SpinalEnum](
//  //    //    doCompare(), GntKind.A, GntKind.D
//  //    //  )
//  //    //}
//  //  }
//  //}
//  switch (Cat(upA.valid, upD.valid, rGnt.asBits)) {
//    is (M"00-") {
//    }
//    is (M"10-") {
//      currGnt := GntKind.A
//      nextGnt := GntKind.D
//    }
//    is (M"01-") {
//      currGnt := GntKind.D
//      nextGnt := GntKind.A
//    }
//    is (M"110") {
//      when (doCompare()) {
//        currGnt := GntKind.A
//        nextGnt := GntKind.D
//      } otherwise {
//        currGnt := GntKind.D
//        nextGnt := GntKind.A
//      }
//    }
//    is (M"111") {
//      when (doCompare()) {
//        currGnt := GntKind.D
//        nextGnt := GntKind.A
//      } otherwise {
//        currGnt := GntKind.A
//        nextGnt := GntKind.D
//      }
//    }
//    //is (M"111") {
//    //  //nextGnt := GntKind.A
//    //  nextGnt := Mux[SpinalEnum](
//    //    doCompare(), GntKind.A, GntKind.D
//    //  )
//    //}
//  }
//  //doSwitch(
//  //  myCat=(
//  //    //Cat(upA.isFiring, upD.isFiring, rGnt.asBits)
//  //    Cat(upA.valid, upD.valid, rGnt.asBits)
//  //  ),
//  //  isCurr=false,
//  //)
//  //doSwitch(
//  //  myCat=(
//  //    Cat(upA.valid, upD.valid, rGnt.asBits)
//  //  ),
//  //  isCurr=true,
//  //)
//  //switch (Cat(upA.isFiring, upD.isFiring, rGnt.asBits)) {
//  //  is (M"00-") {
//  //  }
//  //  is (M"10-") {
//  //    //currGnt := GntKind.A
//  //    nextGnt := GntKind.D
//  //  }
//  //  is (M"01-") {
//  //    //currGnt := GntKind.D
//  //    nextGnt := GntKind.A
//  //  }
//  //  is (M"110") {
//  //    when (doCompare()) {
//  //      //currGnt := GntKind.A
//  //      nextGnt := GntKind.D
//  //    } otherwise {
//  //      //currGnt := GntKind.D
//  //      nextGnt := GntKind.A
//  //    }
//  //  }
//  //  is (M"111") {
//  //    when (doCompare()) {
//  //      //currGnt := GntKind.D
//  //      nextGnt := GntKind.A
//  //    } otherwise {
//  //      //currGnt := GntKind.A
//  //      nextGnt := GntKind.D
//  //    }
//  //  }
//  //  //is (M"111") {
//  //  //  //nextGnt := GntKind.A
//  //  //  nextGnt := Mux[SpinalEnum](
//  //  //    doCompare(), GntKind.A, GntKind.D
//  //  //  )
//  //  //}
//  //}
//  //switch (Cat(upA.isValid, upD.isValid, rGnt.asBits)) {
//  //}
//
//  def doCompare(
//    //checkDValid: Boolean
//  ): Bool = (
//    //upD.isValid
//    //&& 
//    //(
//    //  compareFunc match {
//    //    case Some(myCompareFunc) => (
//    //      myCompareFunc(upA, upD)
//    //    )
//    //    case None => (
//    //      True
//    //    )
//    //  }
//    //)
//    compareFunc(upA, upD/*, checkDValid*/)
//    //Mux(
//    //  nextGnt === GntKind.A,
//    //  upA.isValid,
//    //  upD.isValid,
//    //)
//  )
//
//  override def ups: Seq[Node] = List(upA, upD)
//  override def downs: Seq[Node] = List(down)
//
//  override def propagateDown(): Unit = {
//    propagateDownAll()
//    //down.valid
//    //when (upD.isValid) {
//    //} otherwise {
//    //}
//    down.valid
//  }
//  override def propagateUp(): Unit = {
//    propagateUpAll()
//    //for (up <- ups) {
//    //  
//    //}
//
//    Mux[Bool](
//      //upD.isValid, 
//      //doCompare(true), upD.ready, upA.ready
//      //nextGnt === GntKind.A, upA.ready, upD.ready
//      currGnt === GntKind.A, upA.ready, upD.ready
//    )
//
//    //when (upD.isValid) {
//    //  upD.ready
//    //} otherwise {
//    //  upA.ready
//    //}
//    //ups.foreach(_.ready)
//  }
//  override def build(): Unit = {
//    assert(down.ctrl.forgetOne.isEmpty)
//    down.valid := ups.map(_.isValid).orR
//
//    upA.ready := (
//      down.isValid
//      && down.isReady
//      //&& !doCompare(true)//upD.isValid
//      && (currGnt === GntKind.A)
//    )
//    upD.ready := (
//      down.isValid
//      && down.isReady
//      //&& doCompare(false)
//      && (currGnt === GntKind.D)
//    )
//
//    for (key <- down.fromUp.payload) {
//      //val filtered = ups.filter(
//      //  up => up.keyToData.contains(key)
//      //  || up.fromUp.payload.contains(key)
//      //)
//      ////filtered.size match {
//      ////  case 1 => down(key) := filtered(0)(key)
//      ////}
//      when (
//        //upD.isFiring
//        //upD.isValid
//        //doCompare(true)
//        //doCompare()
//        currGnt === GntKind.A
//      ) {
//        val filtered = List(upA).filter(
//          up => up.keyToData.contains(key)
//          || up.fromUp.payload.contains(key)
//        )
//        down(key) := filtered(0)(key)
//      } otherwise {
//        val filtered = List(upD).filter(
//          up => up.keyToData.contains(key)
//          || up.fromUp.payload.contains(key)
//        )
//        down(key) := filtered(0)(key)
//      }
//    }
//  }
//}
//case class TlHostLinkTesterPayload[
//  T <: Data,
//](
//  dataType: HardType[T],
//) extends Bundle {
//  val data = dataType()
//  val source = UInt(2 bits)
//  //val burstCnt = UInt(3 bits)
//  //val isA = Bool()
//}
//case class TlHostLinkTesterIo[
//  T <: Data,
//](
//  dataType: HardType[T],
//) extends Bundle {
//  val pushA = slave(Stream(TlHostLinkTesterPayload(dataType())))
//  val pushD = slave(Stream(TlHostLinkTesterPayload(dataType())))
//  val pop = master(Stream(TlHostLinkTesterPayload(dataType())))
//}
//case class TlHostLinkTester[
//  T <: Data,
//](
//  dataType: HardType[T],
//) extends Component {
//  //--------
//  val io = TlHostLinkTesterIo(
//    dataType=dataType(),
//  )
//  //--------
//  val linkArr = PipeHelper.mkLinkArr()
//  //val pipeA = PipeHelper(linkArr=linkArr)
//  //val pipeD = PipeHelper(linkArr=linkArr)
//  //val pipePop = PipeHelper(linkArr=linkArr)
//
//  val pipePayload = Payload(TlHostLinkTesterPayload(dataType()))
//  //val pipeDPayload = Payload(MyPayload())
//  //val pipePopPayload = Payload(MyPayload())
//
//  //val cAFront = pipeA.addStage(
//  //  name="AFront",
//  //)
//  //val cALast = pipeA.addStage(
//  //  name="ALast",
//  //  finish=true,
//  //)
//  //val cDFront = pipeD.addStage(
//  //  name="DFront",
//  //)
//  //val cDLast = pipeD.addStage(
//  //  name="DLast",
//  //  finish=true,
//  //)
//  //val cPopFront = pipePop.addStage(
//  //  name="PopFront",
//  //)
//  //val cPopLast = pipePop.addStage(
//  //  name="PopLast",
//  //  finish=true,
//  //)
//  //--------
//  val nUpA = Node()
//  val nUpD = Node()
//  nUpA.driveFrom(io.pushA)(
//    con=(node, payload) => {
//      node(pipePayload) := payload
//      //node(pipePayload).isA := True
//    }
//  )
//  nUpD.driveFrom(io.pushD)(
//    con=(node, payload) => {
//      node(pipePayload) := payload
//      //node(pipePayload).isA := False
//    }
//  )
//  //pipePop.last.down.driveTo(io.pop)(
//  //  con=(payload, node) => {
//  //    payload := node(pipePopPayload)
//  //  }
//  //)
//
//  //val nUpA = pipeA.last.down
//  //val nUpD = pipeD.last.down
//  //val nUpA = Node()
//  //val nUpD = Node()
//  //val nUpPop = pipePop.first.up
//  ////--------
//  //val arb = StreamArbiterFactory.roundRobin.roundRobin.on(
//  //  inputs=List(io.pushA, io.pushD)
//  //)
//  //io.pop << arb
//  // BEGIN: old code
//  val lTlHost = TlHostLink(
//    upA=nUpA,
//    upD=nUpD,
//    down=Node(),
//  )(
//    //compareFunc=None
//    compareFunc=/*Some*/(
//      (
//        upA: Node,
//        upD: Node,
//        //checkDValid: Boolean,
//      ) => (
//        //upA(pipePayload).source
//        //=== upD(pipePayload).source
//        True
//      )
//    )
//  )
//  linkArr += lTlHost
//  lTlHost.down.driveTo(io.pop)(
//    con=(payload, node) => {
//      payload := node(pipePayload)
//    }
//  )
//  // END: old code
//  //--------
//  //when (nUpD.isValid) {
//  //  //cPopFront.up(pipePopPayload) := lTlHost.down(pipeDPayload)
//  //} otherwise {
//  //  //cPopFront.up(pipePopPayload) := lTlHost.down(pipeAPayload)
//  //}
//  //lTlHost.upA.driveFrom
//  //--------
//  Builder(linkArr.toSeq)
//  //--------
//}
//object TlHostLinkTesterSim extends App {
//  //--------
//  def clkRate = 25.0 MHz
//  val simSpinalConfig = SpinalConfig(
//    defaultClockDomainFrequency=FixedFrequency(clkRate)
//  )
//  //--------
//  def dataWidth = 8
//  def dataType() = UInt(dataWidth bits)
//  //--------
//  SimConfig
//    .withConfig(config=simSpinalConfig)
//    .withFstWave
//    .compile(
//      TlHostLinkTester(dataType=dataType())
//    )
//    .doSim({ dut =>
//      def simNumClks = (
//        100
//      )
//      StreamDriver(dut.io.pushA, dut.clockDomain) { payload =>
//        payload.randomize()
//        true
//      }
//      StreamDriver(dut.io.pushD, dut.clockDomain) { payload =>
//        payload.randomize()
//        true
//      }
//      //StreamReadyRandomizer(dut.io.pop, dut.clockDomain)
//      dut.io.pop.ready #= true
//
//      dut.clockDomain.forkStimulus(period=10)
//
//      for (idx <- 0 until simNumClks) {
//        dut.clockDomain.waitRisingEdge()
//      }
//
//      simSuccess()
//    })
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
//  //txnSourcePmRmwDepthPow: Int,
//  txnSourcePmRmwOuterRange: (Int, Int),
//  txnSourcePmRmwInnerRange: (Int, Int),
//  tlName: String,
//) extends Area {
//  //--------
//  assert(txnSourceWidth <= helperP.sourceWidth)
//  assert(txnSourceWidth >= 2)
//  assert(txnSourcePmRmwOuterRange._2 > txnSourcePmRmwInnerRange._1)
//  assert(txnSourcePmRmwOuterRange._1 >= txnSourcePmRmwOuterRange._2)
//  assert(txnSourcePmRmwOuterRange._1 >= 0)
//  assert(txnSourcePmRmwOuterRange._2 >= 0)
//  assert(txnSourcePmRmwInnerRange._1 >= txnSourcePmRmwInnerRange._2)
//  assert(txnSourcePmRmwInnerRange._1 >= 0)
//  assert(txnSourcePmRmwInnerRange._2 >= 0)
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
//  //val loc = new Area {
//    def pipeMemArrDepth = (
//      1 << (txnSourcePmRmwOuterRange._1 - txnSourcePmRmwOuterRange._2 + 1)
//    )
//    def pipeMemDepth = (
//      //(1 << txnSourceWidth)
//      //(1 << txnSourcePmRmwDepthPow)
//      1 << (txnSourcePmRmwInnerRange._1 - txnSourcePmRmwInnerRange._2 + 1)
//    )
//    //val busAMem = Mem(
//    //  wordType=tilelink.ChannelA(p=p),
//    //  wordCount=pipeMemDepth,
//    //)
//    //val busDMem = Mem(
//    //  wordType=tilelink.ChannelD(p=p),
//    //  wordCount=pipeMemDepth
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
//      //def numSendRdWrStages = 2
//      def pipeMemWordType() = Flow(tilelink.ChannelA(p=p))
//      def pipeMemModStageCnt = 1 // Tentative!
//      //def sendPipeMemModType() = (
//      //  SamplePipeMemRmwModType[Flow[tilelink.ChannelA], Bool](
//      //    wordType=sendPipeMemWordType(),
//      //    wordCount=sendPipeMemDepth,
//      //    hazardCmpType=Bool(),
//      //    modStageCnt=sendPipeMemModStageCnt,
//      //  )
//      //)
//      def mkSendPipePayloadExtras() = (
//        TlHelperPipePayloadExtras.mkHostSend(
//          helperP=helperP,
//          txnSourceWidth=txnSourceWidth,
//        )
//      )
//      def mkRecvPipePayloadExtras() = (
//        TlHelperPipePayloadExtras.mkHostRecv(
//          helperP=helperP,
//          txnSourceWidth=txnSourceWidth,
//        )
//      )
//      def mkExt() = {
//        val ret = PipeMemRmwPayloadExt(
//          wordType=pipeMemWordType(),
//          wordCount=pipeMemDepth,
//          hazardCmpType=Bool(),
//          modStageCnt=pipeMemModStageCnt,
//          optEnableModDuplicate=true,
//        )
//        //if (vivadoDebug) {
//        //  ret.addAttribute("MARK_DEBUG", "TRUE")
//        //}
//        ret
//      }
//      case class PipePayload(
//      ) extends Bundle
//        with PipeMemRmwPayloadBase[
//          Flow[tilelink.ChannelA], Bool
//        ]
//      {
//        //val send = mkSendPipePayloadExtras()
//        val isSend = Bool()
//        //val burstCnt = UInt()
//        val send = mkSendPipePayloadExtras()
//        val recv = mkRecvPipePayloadExtras()
//        val myExt = mkExt()
//        def setPipeMemRmwExt(
//          inpExt: PipeMemRmwPayloadExt[Flow[tilelink.ChannelA], Bool],
//          memArrIdx: Int,
//        ): Unit = {
//          myExt := inpExt
//        }
//        def getPipeMemRmwExt(
//          outpExt: PipeMemRmwPayloadExt[Flow[tilelink.ChannelA], Bool],
//          memArrIdx: Int,
//        ): Unit = {
//          outpExt := myExt
//        }
//      }
//      //--------
//      val io = new Area {
//        //val send = Stream(TlHelperPipePayloadExtras.mkHostSend(
//        //  helperP=helperP,
//        //  txnSourceWidth=txnSourceWidth,
//        //))
//        //val send = Stream(tilelink.ChannelA(p=p))
//        //val recv = Stream(TlHelperPipePayloadExtras.mkHostRecv(
//        //  helperP=helperP,
//        //  txnSourceWidth=txnSourceWidth,
//        //))
//        //val recv = Stream(tilelink.ChannelD(p=p))
//        //val send = Stream(PipePayload())
//        //val recv = Stream(PipePayloaPipePayload())
//        val send = Stream(mkSendPipePayloadExtras())
//        val recv = Stream(mkRecvPipePayloadExtras())
//      }
//      //--------
//      //def pipeMemWordType() = pipeMemWordType()
//      //def pipeMemModStageCnt = pipeMemModStageCnt
//      val pipeMem = { // for keeping track of outstanding transactions
//        ///*Array.fill(numSendRdWrStages)*/(
//        PipeMemRmw[
//          Flow[tilelink.ChannelA],
//          Bool,
//          //SamplePipeMemRmwModType[Flow[tilelink.ChannelA], Bool],
//          PipePayload,
//          PipeMemRmwDualRdTypeDisabled[Flow[tilelink.ChannelA], Bool],
//        ](
//          wordType=pipeMemWordType(),
//          wordCount=pipeMemDepth,
//          hazardCmpType=Bool(),
//          modType=PipePayload(),
//          modStageCnt=pipeMemModStageCnt,
//          pipeName=tlName + "_HostSend",
//          linkArr=Some(linkArr),
//          dualRdType=PipeMemRmwDualRdTypeDisabled[
//            Flow[tilelink.ChannelA], Bool
//          ](),
//          optDualRd=false,
//        )(
//          //doHazardCmpFunc=None,
//        )
//        //)
//      }
//      def addDirectLink(
//        //toAdd: Link
//        up: Node,
//        down: Node,
//      ): DirectLink = {
//        val ret = DirectLink(
//          up=up,
//          down=down,
//        )
//        linkArr += ret
//        ret
//      }
//      val sendFrontPipe = PipeHelper(linkArr=linkArr)
//      val recvFrontPipe = PipeHelper(linkArr=linkArr)
//      val recvBackPipe = PipeHelper(linkArr=linkArr)
//      val pmRmwFrontPipe = PipeHelper(linkArr=linkArr)
//      val pmRmwModPipe = PipeHelper(linkArr=linkArr)
//      val pmRmwBackPipe = PipeHelper(linkArr=linkArr)
//
//      // `frontPayload` is shared by `sendFrontPipe` and `recvFrontPipe`
//      // due to how `TlHostLink` works
//      val frontPayload = Payload(PipePayload())
//      //val recvFrontPayload = Payload(PipePayload())
//      val recvBackPayload = Payload(PipePayload())
//
//      sendFrontPipe.first.up.driveFrom(io.send)(
//        con=(node, payload) => {
//          val tempPayload = PipePayload()
//
//          tempPayload := tempPayload.getZero
//          tempPayload.allowOverride
//          tempPayload.send := payload
//          tempPayload.isSend := True
//
//          node(frontPayload) := tempPayload
//        }
//      )
//      recvFrontPipe.first.up.driveFrom(bus.d)(
//        con=(node, payload) => {
//          val tempPayload = PipePayload()
//
//          tempPayload := tempPayload.getZero
//          tempPayload.allowOverride
//          tempPayload.recv.d.bus := payload
//          //tempPayload.recv.d.valid := True
//          tempPayload.isSend := False
//
//          node(frontPayload) := tempPayload
//        }
//      )
//      def doDriveBusA(): Unit = {
//        pmRmwBackPipe.last.down.driveTo(bus.a)(
//          con=(payload, node) => {
//            payload := node(frontPayload).send.a.bus
//          }
//        )
//      }
//      def doDriveIoRecv(): Unit = {
//        recvBackPipe.last.down.driveTo(io.recv)(
//          con=(payload, node) => {
//            payload := node(recvBackPayload).recv
//          }
//        )
//      }
//      //--------
//      // `sendFront`
//      val cSendFrontFront = sendFrontPipe.addStage(
//        name=tlName + "_SendFrontFront",
//      )
//      val cSendFrontLast = sendFrontPipe.addStage(
//        name=tlName + "_SendFrontLast",
//        finish=true,
//      )
//      val cSendFrontFrontArea = new cSendFrontFront.Area {
//        when (up.isFiring) {
//          // the `sendFront` pipeline will need to have 
//        }
//      }
//      //--------
//      // `recvFront`
//      val cRecvFrontFront = recvFrontPipe.addStage(
//        name=tlName + "_RecvFrontFront",
//      )
//      val cRecvFrontLast = recvFrontPipe.addStage(
//        name=tlName + "_RecvFrontLast",
//        finish=true,
//      )
//      val cRecvFrontFrontArea = new cRecvFrontFront.Area {
//        when (up.isFiring) {
//        }
//      }
//      //--------
//      // `recvBack`
//      val cRecvBackFront = recvFrontPipe.addStage(
//        name=tlName + "_RecvBackFront",
//      )
//      val cRecvBackLast = recvFrontPipe.addStage(
//        name=tlName + "_RecvBackLast",
//        finish=true,
//      )
//      val cRecvBackFrontArea = new cRecvBackFront.Area {
//        when (up.isFiring) {
//        }
//      }
//      //--------
//      // This is the part where the two pipelines, A and D, meet
//      val nfRecvFrontArr = Array.fill(2)(Node())
//      val fRecvFront = ForkLink(
//        up=recvFrontPipe.last.down,
//        downs=nfRecvFrontArr.toSeq,
//        // this needs to be set to `false` so that something akin to
//        // `ready` round-robin arbitration can be done
//        synchronous=false, 
//      )
//      linkArr += fRecvFront
//      val dRecvFrontToBack = addDirectLink(
//        up=nfRecvFrontArr(0),
//        down=recvBackPipe.first.up,
//      )
//
//      val lTlHost = TlHostLink(
//        upA=sendFrontPipe.last.down,
//        upD=(
//          //recvFrontPipe.last.down
//          nfRecvFrontArr(1)
//        ),
//        down=Node(),
//      )(
//        compareFunc=(
//          //None
//          /*Some*/(
//            (
//              upA: Node,
//              upD: Node,
//              //checkDValid: Boolean,
//            ) => (
//              //(
//              //  if (checkDValid) (
//              //    upD.isValid
//              //  ) else (
//              //    True
//              //  )
//              //)
//              //&& 
//              (
//                //upA(frontPayload).send.a.bus.source
//                //=== upD(frontPayload).recv.d.bus.source
//                (
//                  upA(frontPayload).send.a.bus.source(
//                    txnSourcePmRmwOuterRange._1
//                    downto txnSourcePmRmwOuterRange._2
//                  )
//                ) === (
//                  upD(frontPayload).recv.d.bus.source(
//                    txnSourcePmRmwOuterRange._1
//                    downto txnSourcePmRmwOuterRange._2
//                  )
//                )
//              )
//            )
//          )
//        )
//      )
//      linkArr += lTlHost
//      //--------
//      // `pmRmwFront`
//      val cPmRmwFrontFront = pmRmwFrontPipe.addStage(
//        name=tlName + "_PmRmwFrontFront",
//      )
//      val cPmRmwFrontLast = pmRmwFrontPipe.addStage(
//        name=tlName + "_PmRmwFrontLast",
//        finish=true,
//      )
//      val cPmRmFrontFrontArea = new cPmRmwFrontFront.Area {
//        val myUpMod = PipePayload()
//      }
//      val dPmRmwFrontFront = addDirectLink(
//        up=(
//          //lTlHost.down
//          Node()
//        ),
//        down=pmRmwFrontPipe.first.up,
//      )
//
//      val dPmRmwFront = addDirectLink(
//        up=pmRmwFrontPipe.last.down,
//        down=pipeMem.io.front,
//      )
//      //--------
//      // `pmRmwModPipe`
//      val cPmRmwModFront = pmRmwModPipe.addStage(
//        name=tlName + "_PmRmwModFront",
//      )
//      val cPmRmwModLast = pmRmwModPipe.addStage(
//        name=tlName + "_PmRmwModLast",
//        finish=true,
//      )
//
//      pipeMem.io.midModStages(0) := (
//        RegNext(pipeMem.io.midModStages(0).getZero)
//        init(pipeMem.io.midModStages(0).getZero)
//      )
//      val cPmRmwModFrontArea = new cPmRmwModFront.Area {
//
//        val tempInp = PipePayload()
//        val tempOutp = PipePayload()
//        tempInp := up(pipeMem.io.modFrontPayload)
//        when (up.isFiring) {
//        }
//      }
//
//      val dPmRmwModFront = addDirectLink(
//        up=pipeMem.io.modFront,
//        down=pmRmwModPipe.first.up,
//      )
//
//      val dPmRmwModBack = addDirectLink(
//        up=pmRmwModPipe.last.down,
//        down=pipeMem.io.modBack,
//      )
//      //--------
//      // `pmRmwBackPipe`
//      val cPmRmwBackFront = pmRmwBackPipe.addStage(
//        name=tlName + "_PmRmwBackFront",
//      )
//      val cPmRmwBackLast = pmRmwBackPipe.addStage(
//        name=tlName + "_PmRmwBackLast",
//        finish=true,
//      )
//      val dPmRmwBack = addDirectLink(
//        up=pipeMem.io.back,
//        down=pmRmwBackPipe.first.up,
//      )
//      val cPmRmwBackFrontArea = new cPmRmwBackFront.Area {
//        val tempInp = PipePayload()
//        tempInp := up(pipeMem.io.backPayload)
//        when (up.isValid) {
//          when (!tempInp.isSend) {
//            // only payloads that originate from `io.send` should make it
//            // to `bus.a`
//            throwIt()
//          }
//        }
//      }
//      //--------
//      doDriveBusA()
//      doDriveIoRecv()
//      //--------
//      //val nSendFront = Node()
//      //val nSend = Node()
//      //val nPmRwmModFront = Node()
//      //val nPmRwmModBack = Node()
//      //--------
//      //val send = new Area {
//      //  def busChan = bus.a
//      //  def pipeMemWordType() = sendPipeMemWordType()
//      //  def pipeMemModStageCnt = sendPipeMemModStageCnt
//      //  /*val pipeMemArr = Array.fill(numMemArrs)*/
//      //  val pipeMem = {
//      //    ///*Array.fill(numSendRdWrStages)*/(
//      //    PipeMemRmw[
//      //      Flow[tilelink.ChannelA],
//      //      Bool,
//      //      //SamplePipeMemRmwModType[Flow[tilelink.ChannelA], Bool],
//      //      PipePayload,
//      //      PipeMemRmwDualRdTypeDisabled[Flow[tilelink.ChannelA], Bool],
//      //    ](
//      //      wordType=pipeMemWordType(),
//      //      wordCount=pipeMemDepth,
//      //      hazardCmpType=Bool(),
//      //      modType=PipePayload(),
//      //      modStageCnt=pipeMemModStageCnt,
//      //      pipeName=tlName + "_HostSend",
//      //      linkArr=Some(linkArr),
//      //      dualRdType=PipeMemRmwDualRdTypeDisabled[
//      //        Flow[tilelink.ChannelA], Bool
//      //      ](),
//      //      optDualRd=false,
//      //    )(
//      //      //doHazardCmpFunc=None,
//      //    )
//      //    //)
//      //  }
//      //  val frontPipe = PipeHelper(linkArr=linkArr)
//      //  //val modPipe = PipeHelper(linkArr=linkArr)
//      //  val backPipe = PipeHelper(linkArr=linkArr)
//
//      //  val frontPayload = Payload(PipePayload())
//      //  //val modPayload = Payload(PipePayload())
//      //  val backPayload = Payload(PipePayload())
//
//      //  //val cReadBusMem = pipe.addStage("HostSendReadBusMem")
//      //  ////val cReadBusMemFf1 = pipe.addPipeStage()
//      //  //val cWriteBusMem = pipe.addStage("HostSendWriteBusMem")
//      //  ////val cWriteBusMemFf1 = pipe.addPipeStage()
//      //  val cFrontFront = frontPipe.addStage(
//      //    name=tlName + "_HostSendFrontFront"
//      //  )
//      //  val cFrontLast = frontPipe.addStage(
//      //    name=tlName + "_HostSendFrontLast",
//      //    finish=true,
//      //  )
//
//      //  frontPipe.first.up.driveFrom(io.send)(
//      //    con=(node, payload) => {
//      //      node(frontPayload) := node(frontPayload).getZero
//      //      node(frontPayload).extras := payload
//      //    }
//      //  )
//      //  val dFrontLast = DirectLink(
//      //    up=frontPipe.last.down,
//      //    down=pipeMem.io.front,
//      //  )
//      //  linkArr += dFrontLast
//
//      //  //val cModFront = modPipe.addStage(
//      //  //  name=tlName + "_HostSendModFront"
//      //  //)
//
//      //  //val cModBack = modPipe.addStage(
//      //  //  name=
//      //  //)
//
//
//      //  //pipe.last.down.driveTo(bus.a)(
//      //  //  con=(payload, node) => {
//      //  //    payload := node(frontPayload).bus.a
//      //  //  }
//      //  //)
//      //}
//      //val recv = new Area {
//      //  def busChan = bus.d
//      //  //val pipeMemArr = Array.fill(numMemArrs)(
//      //  //  Mem(
//      //  //    wordType=Flow(tilelink.ChannelD(p=p)),
//      //  //    wordCount=pipeMemDepth,
//      //  //  )
//      //  //)
//      //  def pipeMemWordType() = Flow(tilelink.ChannelD(p=p))
//      //  def pipeMemModStageCnt = 1 // Tentative!
//      //  def pipeMemModType() = (
//      //    SamplePipeMemRmwModType[Flow[tilelink.ChannelD], Bool](
//      //      wordType=pipeMemWordType(),
//      //      wordCount=pipeMemDepth,
//      //      hazardCmpType=Bool(),
//      //      modStageCnt=pipeMemModStageCnt,
//      //    )
//      //  )
//      //  val pipeMem = /*Array.fill(numMemArrs)*/({
//      //    /*Array.fill(numSendRdWrStages)*/(
//      //      PipeMemRmw[
//      //        Flow[tilelink.ChannelD],
//      //        Bool,
//      //        SamplePipeMemRmwModType[Flow[tilelink.ChannelD], Bool],
//      //        PipeMemRmwDualRdTypeDisabled[Flow[tilelink.ChannelD], Bool],
//      //      ](
//      //        wordType=pipeMemWordType(),
//      //        wordCount=pipeMemDepth,
//      //        hazardCmpType=Bool(),
//      //        modType=pipeMemModType(),
//      //        modStageCnt=pipeMemModStageCnt,
//      //        pipeName="HostSend",
//      //        linkArr=Some(linkArr),
//      //        dualRdType=PipeMemRmwDualRdTypeDisabled[
//      //          Flow[tilelink.ChannelD], Bool
//      //        ](),
//      //        optDualRd=false,
//      //      )(
//      //        //doHazardCmpFunc=None,
//      //      )
//      //    )
//      //  })
//      //  val frontPipe = PipeHelper(linkArr=linkArr)
//      //  val modPipe = PipeHelper(linkArr=linkArr)
//      //  val backPipe = PipeHelper(linkArr=linkArr)
//      //  def mkPipePayload() = TlHelperPipePayloadExtras.mkHostRecv(
//      //    helperP=helperP,
//      //    txnSourceWidth=txnSourceWidth,
//      //  )
//      //  //val back = Stream(mkPipePayload())
//      //  val frontPayload = Payload(mkPipePayload())
//      //  val modPayload = Payload(mkPipePayload())
//      //  val backPayload = Payload(mkPipePayload())
//
//      //  //val cReadBusMem = pipe.addStage("HostRecvReadBusMem")
//      //  ////val cReadBusMemFf1 = pipe.addPipeStage()
//      //  //val cWriteBusMem = pipe.addStage("HostRecvWriteBusMem")
//      //  ////val cWriteBusMemFf1 = pipe.addPipeStage()
//
//      //  frontPipe.first.up.driveFrom(bus.d)(
//      //    con=(node, payload) => {
//      //      ////node(frontPayload) := node(frontPayload).getZero
//      //      ////node(frontPayload).allowOverride
//      //      //node(frontPayload).d.valid := False
//      //      //node(frontPayload).d.payload := payload
//      //      ////node(frontPayload).bus.d := payload
//      //    }
//      //  )
//      //  val dFrontLast = DirectLink(
//      //    up=frontPipe.last.down,
//      //    down=pipeMem.io.front,
//      //  )
//      //  linkArr += dFrontLast
//      //  //when (dFrontLast.up.isValid) {
//      //  //}
//      //  backPipe.last.down.driveTo(io.recv)(
//      //    con=(payload, node) => {
//      //      payload := node(backPayload)
//      //    }
//      //  )
//
//      //  //def intf = pipe.last
//
//      //  //pipe.first.up.driveFrom(front)(
//      //  //  con=(node, payload) => {
//      //  //    node(pipePayload) := payload
//      //  //  }
//      //  //)
//      //}
//      //--------
//      // BEGIN: new code
//      //val sendReadBusMem = new send.cReadBusMem.Area {
//      //  def myBus = send.pipePayload.bus
//      //  //for (idx <- 0 until numSendRdWrStages) {
//      //  //  myBus.a.rd := (
//      //  //    send.pipeMemA2d(sendMemArrIdx)(idx).readSync(
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
//      //    //send.pipeMemA2d(sendMemArrIdx)(stageIdx).write(
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
//    //    val pipeMemArr = Array.fill(numMemArrs)(
//    //      Mem(
//    //        wordType=Flow(tilelink.ChannelD(p=p)),
//    //        wordCount=pipeMemDepth,
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
//    //    val pipeMemArr = Array.fill(numMemArrs)(
//    //      Mem(
//    //        wordType=Flow(tilelink.ChannelA(p=p)),
//    //        wordCount=pipeMemDepth,
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
//  //}
//  def doBuilder() = {
//    //Builder((sendPipe.linkArr + recvPipe.linkArr).toSeq)
//    Builder(linkArr.toSeq)
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
//  //  def pipeMemDepth = (1 << txnSourceWidth)
//  //  val busAMem = Mem(
//  //    wordType=tilelink.ChannelA(p=p),
//  //    wordCount=pipeMemDepth,
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
//  //    wordCount=pipeMemDepth,
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
//  //def run(
//  //  //sendDoIt: Bool,
//  //  //opcode: Bits,
//  //  //address: UInt,
//  //  //data: Bits,
//  //  //size: UInt,
//  //  //source: UInt,
//  //): Unit = {
//  //  //assert(isHost)
//  //  //--------
//  //  //--------
//  //  //def doIt[
//  //  //  SendPayloadT <: Data,
//  //  //  RecvPayloadT <: Data,
//  //  //](
//  //  //  //sendState: State.C,
//  //  //  sendStm: Stream[SendPayloadT],
//  //  //  //recvState: State.C,
//  //  //  recvStm: Stream[RecvPayloadT],
//  //  //): Unit = {
//  //  //  //var myOpcodeA: tilelink.Opcode.A.E = tilelink.Opcode.A.PUT_FULL_DATA
//  //  //  //def myOpcodeD = (
//  //  //  //  tilelink.Opcode.hostToDevMap(myOpcodeA)
//  //  //  //)
//  //  //  //val hostToDevMap = Map[tilelink.Opcode.A.E, tilelink.Opcode.D.E](
//  //  //  //  tilelink.Opcode.A.PUT_FULL_DATA -> tilelink.Opcode.D.ACCESS_ACK,
//  //  //  //  tilelink.Opcode.A.PUT_PARTIAL_DATA -> tilelink.Opcode.D.ACCESS_ACK,
//  //  //  //  tilelink.Opcode.A.GET -> tilelink.Opcode.D.ACCESS_ACK_DATA,
//  //  //  //)
//  //  //  //val devToHostMap = Map[tilelink.Opcode.D.E, tilelink.Opcode.A.E](
//  //  //  //  tilelink.Opcode.D.ACCESS_ACK -> tilelink.Opcode.A.PUT_FULL_DATA,
//  //  //  //  tilelink.Opcode.D.ACCESS_ACK -> tilelink.Opcode.A.PUT_PARTIAL_DATA,
//  //  //  //  tilelink.Opcode.D.ACCESS_ACK_DATA -> tilelink.Opcode.A.GET,
//  //  //  //)
//
//  //  //  //switch (sendState) {
//  //  //  //  is (State.idle) {
//  //  //  //    when (sendDoIt) {
//  //  //  //      sendState := State.doPipe
//  //  //  //      sendStm.valid := True
//  //  //  //    }
//  //  //  //  }
//  //  //  //  is (State.doPipe) {
//  //  //      //when (bus.a.fire) {
//  //  //      //}
//  //  //      var myOpcodeA: tilelink.Opcode.A.E = (
//  //  //        tilelink.Opcode.A.PUT_FULL_DATA
//  //  //      )
//  //  //      def myOpcodeD = (
//  //  //        TlOpcode.tlUlHostToDevMap(myOpcodeA)
//  //  //      )
//  //  //      def setBusOpcode(): Unit = {
//  //  //        if (isHost) {
//  //  //          //bus.a.opcode.assignFromBits(myOpcodeA.asBits)
//  //  //          bus.a.opcode := myOpcodeA
//  //  //        } else { // if (!isHost)
//  //  //          //bus.d.opcode.assignFromBits(myOpcodeD.asBits)
//  //  //          bus.d.opcode := myOpcodeD
//  //  //        }
//  //  //      }
//  //  //      when (sendStm.fire) {
//  //  //        //loc.sendBusMem.write(
//  //  //        //  addr=
//  //  //        //)
//  //  //        switch (
//  //  //          loc.opcodeA
//  //  //        ) {
//  //  //          is (tilelink.Opcode.A.PUT_FULL_DATA) {
//  //  //            myOpcodeA = tilelink.Opcode.A.PUT_FULL_DATA
//  //  //            setBusOpcode()
//  //  //          }
//  //  //          is (tilelink.Opcode.A.PUT_PARTIAL_DATA) {
//  //  //            myOpcodeA = tilelink.Opcode.A.PUT_PARTIAL_DATA
//  //  //            setBusOpcode()
//  //  //          }
//  //  //          //is (tilelink.Opcode.A.ARITHMETIC_DATA) {
//  //  //          //  myOpcodeA = tilelink.Opcode.A.ARITHMETIC_DATA
//  //  //          //  setBusOpcode()
//  //  //          //}
//  //  //          //is (tilelink.Opcode.A.LOGICAL_DATA) {
//  //  //          //  myOpcodeA = tilelink.Opcode.A.LOGICAL_DATA
//  //  //          //  setBusOpcode()
//  //  //          //}
//  //  //          is (tilelink.Opcode.A.GET) {
//  //  //            myOpcodeA = tilelink.Opcode.A.GET
//  //  //            setBusOpcode()
//  //  //          }
//  //  //          //is (tilelink.Opcode.A.INTENT) {
//  //  //          //  myOpcodeA = tilelink.Opcode.A.INTENT
//  //  //          //  setBusOpcode()
//  //  //          //}
//  //  //          default {
//  //  //            if (isHost) {
//  //  //              bus.a.opcode := bus.a.opcode.getZero
//  //  //            } else { // if (!isHost)
//  //  //              bus.d.opcode := bus.d.opcode.getZero
//  //  //            }
//  //  //          }
//  //  //        }
//  //  //      }
//  //  //  //  }
//  //  //  //}
//  //  //  //switch (recvState) {
//  //  //  //  is (State.idle) {
//  //  //  //    when (recvStm.valid) {
//  //  //  //    } otherwise { // when (!recvStm.valid)
//  //  //  //    }
//  //  //  //  }
//  //  //  //  is (State.doPipe) {
//  //  //  when (recvStm.valid) {
//  //  //    switch (
//  //  //      loc.opcodeA // TODO: this needs changed!
//  //  //    ) {
//  //  //      is (tilelink.Opcode.A.PUT_FULL_DATA) {
//  //  //        myOpcodeA = tilelink.Opcode.A.PUT_FULL_DATA
//  //  //        if (isHost) {
//  //  //        } else { // if (!isHost)
//  //  //        }
//  //  //      }
//  //  //      is (tilelink.Opcode.A.PUT_PARTIAL_DATA) {
//  //  //        myOpcodeA = tilelink.Opcode.A.PUT_PARTIAL_DATA
//  //  //        if (isHost) {
//  //  //        } else { // if (!isHost)
//  //  //        }
//  //  //      }
//  //  //      //is (tilelink.Opcode.A.ARITHMETIC_DATA) {
//  //  //      //  myOpcodeA = tilelink.Opcode.A.ARITHMETIC_DATA
//  //  //      //  if (isHost) {
//  //  //      //  } else { // if (!isHost)
//  //  //      //  }
//  //  //      //}
//  //  //      //is (tilelink.Opcode.A.LOGICAL_DATA) {
//  //  //      //  myOpcodeA = tilelink.Opcode.A.LOGICAL_DATA
//  //  //      //  if (isHost) {
//  //  //      //  } else { // if (!isHost)
//  //  //      //  }
//  //  //      //}
//  //  //      is (tilelink.Opcode.A.GET) {
//  //  //        myOpcodeA = tilelink.Opcode.A.GET
//  //  //        if (isHost) {
//  //  //        } else { // if (!isHost)
//  //  //        }
//  //  //      }
//  //  //      //is (tilelink.Opcode.A.INTENT) {
//  //  //      //  myOpcodeA = tilelink.Opcode.A.INTENT
//  //  //      //  if (isHost) {
//  //  //      //  } else { // if (!isHost)
//  //  //      //  }
//  //  //      //}
//  //  //      default {
//  //  //      }
//  //  //    }
//  //  //  }
//  //  //  //  }
//  //  //  //}
//  //  //}
//  //  //if (isHost) {
//  //  //  doIt(
//  //  //    //sendState=rStateA,
//  //  //    sendStm=bus.a,
//  //  //    //recvState=rStateD,
//  //  //    recvStm=bus.d,
//  //  //  )
//  //  //} else { // if (!isHost)
//  //  //  doIt(
//  //  //    //sendState=rStateD,
//  //  //    sendStm=bus.d,
//  //  //    //recvState=rStateA,
//  //  //    recvStm=bus.a,
//  //  //  )
//  //  //}
//  //  //--------
//  //}
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
