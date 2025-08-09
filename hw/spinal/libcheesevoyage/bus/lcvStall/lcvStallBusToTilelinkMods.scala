//package libcheesevoyage.bus.lcvStall
//
//import spinal.core._
//import spinal.core.formal._
//import spinal.core.sim._
//import spinal.lib._
//import spinal.lib.misc.pipeline._
//import spinal.lib.bus.tilelink
//import libcheesevoyage.bus.tilelink.TlOpcode
//
//case class LcvStallBusToTilelinkConfig(
//  //addrWidth: Int,
//  //dataWidth: Int,
//  //sizeBytes: Int,
//  //srcWidth: Int,
//  //isDual: Boolean,
//  ////sinkWidth: Int,
//  ////optMemCoherency: Boolean=false,
//  ////withBCE: Boolean=false,
//  ////optAtomic: Boolean=false,
//  busCfg: LcvStallBusConfig,
//) {
//  // TODO: support full TL-C
//  val tlCfg = tilelink.BusParameter.simple(
//    addressWidth=busCfg.addrWidth,
//    dataWidth=busCfg.dataWidth,
//    // NOTE: `sizeBytes` is the maximal number of bytes in a burst
//    sizeBytes=busCfg.sizeBytes,
//    sourceWidth=srcWidth,
//  )
//  val dualFifoDepth = (
//    tlCfg.beatMax + 2
//  )
//  def doNeedIsDualEqTrue(): Unit = {
//    assert(
//      isDual,
//      s"need `isDual == true`, but have `isDual == false`"
//    )
//  }
//  def doNeedIsDualEqFalse(): Unit = {
//    assert(
//      !isDual,
//      s"need `isDual == false`, but have `isDual == true`"
//    )
//  }
//  //val tlCfg = tilelink.BusParameter(
//  //  addressWidth=addrWidth,
//  //  dataWidth=dataWidth,
//  //  sourceWidth=1,
//  //  sinkWidth=1,
//  //  withBCE=false,
//  //)
//  //val tlM2sCfg = tilelink.M2sParameters(
//  //)
//}
