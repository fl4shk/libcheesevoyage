package libcheesevoyage

import spinal.core._
import spinal.lib._
import spinal.core.formal._

object LongUdivIterTopLevelVerilog extends App {
  val params = LongDivParams(
    mainWidth=4,
    denomWidth=4,
    chunkWidth=2,
    tagWidth=8,
    pipelined=false,
    usePipeSkidBuf=false,
  )
  Config.spinal.generateVerilog(LongUdivIter(params=params))
  //val report = SpinalVerilog(new LongUdivIter(params=params))
  //report.printPruned()
}
object LongUdivIterSyncTopLevelVerilog extends App {
  val params = LongDivParams(
    mainWidth=4,
    denomWidth=4,
    chunkWidth=2,
    tagWidth=8,
    pipelined=true,
    usePipeSkidBuf=true,
  )
  Config.spinal.generateVerilog(LongUdivIterSync(
    params=params,
    chunkStartVal=0,
  ))
  //val report = SpinalVerilog(new LongUdivIter(params=params))
  //report.printPruned()
}
