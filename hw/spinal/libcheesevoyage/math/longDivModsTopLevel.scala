package libcheesevoyage.math
import libcheesevoyage._

import spinal.core._
import spinal.lib._
import spinal.core.formal._

object LongDivMultiCycleTopLevelVerilog extends App {
  val mainWidth = 4
  val denomWidth = 4
  val chunkWidth = 2
  Config.spinal.generateVerilog(LongDivMultiCycle(
    mainWidth=mainWidth,
    denomWidth=denomWidth,
    chunkWidth=chunkWidth,
  ))
  //val report = SpinalVerilog(new LongUdivIter(params=params))
  //report.printPruned()
}
object LongDivPipelinedTopLevelVerilog extends App {
  val mainWidth = 4
  val denomWidth = 4
  val chunkWidth = 2
  val usePipeSkidBuf = true
  Config.spinal.generateVerilog(LongDivPipelined(
    mainWidth=mainWidth,
    denomWidth=denomWidth,
    chunkWidth=chunkWidth,
    usePipeSkidBuf=usePipeSkidBuf,
  ))
  //val report = SpinalVerilog(new LongUdivIter(params=params))
  //report.printPruned()
}
