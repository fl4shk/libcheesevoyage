//package libcheesevoyage
//
//import spinal.core._
//import spinal.lib._
//import spinal.core.formal._

//case class PipelineModsTopLevel[
//  T <: Data
//](
//  //dataType: HardType[T]=new Bundle {
//  //  val r = UInt(4 bits)
//  //  val g = UInt(4 bits)
//  //  val b = UInt(4 bits)
//  //},
//  dataType: HardType[T]=UInt(1 bits),
//  optIncludeValidBusy: Boolean=false,
//  optIncludeReadyBusy: Boolean=false,
//  optPassthrough: Boolean=false,
//  optTieIfwdValid: Boolean=false,
//  //optFormal: Boolean=true,
//) extends Component {
//  val io = PipeSkidBufIo(
//    dataType=dataType(),
//    optIncludeValidBusy=optIncludeValidBusy,
//    optIncludeReadyBusy=optIncludeReadyBusy,
//  )
//  val dut = PipeSkidBuf(
//    dataType=dataType(),
//    optIncludeValidBusy=optIncludeValidBusy,
//    optIncludeReadyBusy=optIncludeReadyBusy,
//    optPassthrough=optPassthrough,
//    optTieIfwdValid=optTieIfwdValid,
//    //optFormal=optFormal,
//  )
//  io <> dut.io
//
//  if (optFormal) {
//    val ifwdPayload = io.prev.payload
//    val ifwdValid = io.prev.valid
//    val obakReady = io.prev.ready
//
//    val ofwdPayload = io.next.payload
//    val ofwdValid = io.next.valid
//    val ibakReady = io.next.ready
//
//    val misc = io.misc
//
//    assumeInitial(clockDomain.isResetActive)
//    assumeInitial(ifwdPayload === ifwdPayload.getZero)
//    assumeInitial(~ifwdValid)
//    //assumeInitial(~obakReady)
//    //assumeInitial(ofwdPayload === ofwdPayload.getZero)
//    //assumeInitial(~ofwdValid)
//    assumeInitial(~ibakReady)
//    ////anyseq(ifwdValid)
//    ////anyseq(ifwdPayload)
//    //anyseq(ifwdValid)
//    //anyseq(ifwdPayload)
//    //anyseq(ibakReady)
//    ////anyseq(dut.io.misc)
//    //anyseq(misc)
//  }
//}
//
//object PipelineModsTopLevelVerilog extends App {
//  //Config.spinal.generateVerilog(PipelineModsTopLevel())
//  //Config.spinal.generateVerilog(PipelineModsTopLevel())
//  val report = SpinalVerilog(new PipelineModsTopLevel())
//  report.printPruned()
//}
//
//object PipelineModsTopLevelVhdl extends App {
//  //Config.spinal.generateVhdl(PipelineModsTopLevel())
//  val report = SpinalVhdl(new PipelineModsTopLevel())
//  report.printPruned()
//}
