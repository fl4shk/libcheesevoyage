package libcheesevoyage.math
import libcheesevoyage.Config

import spinal.core._
import spinal.lib._
import spinal.core.formal._

object LongUdivIterTopLevelVerilog extends App {
  val cfg = LongDivConfig(
    mainWidth=4,
    denomWidth=4,
    chunkWidth=2,
    tagWidth=8,
    pipelined=false,
    usePipeSkidBuf=false,
  )
  Config.spinal.generateVerilog(LongUdivIter(cfg=cfg))
  //val report = SpinalVerilog(new LongUdivIter(cfg=cfg))
  //report.printPruned()
}
//object LongUdivIterSyncTopLevelVerilog extends App {
//  val cfg = LongDivConfig(
//    mainWidth=4,
//    denomWidth=4,
//    chunkWidth=2,
//    tagWidth=8,
//    pipelined=true,
//    usePipeSkidBuf=true,
//  )
//  Config.spinal.generateVerilog(LongUdivIterSync(
//    cfg=cfg,
//    chunkStartVal=0,
//  ))
//  //val report = SpinalVerilog(new LongUdivIter(cfg=cfg))
//  //report.printPruned()
//}
