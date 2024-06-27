package libcheesevoyage.math
import libcheesevoyage._

import spinal.core._
import spinal.lib._
import spinal.core.formal._

object LongDivMultiCycleTopLevelSystemVerilog extends App {
  val mainWidth = 4
  val denomWidth = 4
  val chunkWidth = 2
  Config.spinal.generateSystemVerilog(LongDivMultiCycle(
    mainWidth=mainWidth,
    denomWidth=denomWidth,
    chunkWidth=chunkWidth,
  ))
  //val report = SpinalSystemVerilog(new LongUdivIter(params=params))
  //report.printPruned()
}
object LongDivPipelinedTopLevelSystemVerilog extends App {
  val mainWidth = 4
  val denomWidth = 4
  val chunkWidth = 2
  val usePipeSkidBuf = true
  Config.spinal.generateSystemVerilog(LongDivPipelined(
    mainWidth=mainWidth,
    denomWidth=denomWidth,
    chunkWidth=chunkWidth,
    usePipeSkidBuf=usePipeSkidBuf,
  ))
  //val report = SpinalSystemVerilog(new LongUdivIter(params=params))
  //report.printPruned()
}
