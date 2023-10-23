package libcheesevoyage.gfx
import libcheesevoyage._

import libcheesevoyage.general.Vec2
import libcheesevoyage.general.ElabVec2

import spinal.core._
//import spinal.core.formal._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import scala.collection.mutable.ArrayBuffer
import scala.math._

object Gpu2dConfig {
  def spinal = SpinalConfig(
    targetDirectory="hw/gen",
    defaultConfigForClockDomains=ClockDomainConfig(
      resetActiveLevel=HIGH,
      //resetKind=BOOT,
    ),
    onlyStdLogicVectorAtTopLevelIo=true,
  )
}

object Gpu2dToVerilog extends App {
  Gpu2dConfig.spinal.generateVerilog(Gpu2d())
}

object Gpu2dToVhdl extends App {
  Gpu2dConfig.spinal.generateVhdl(Gpu2d())
  //val report = SpinalVhdl(new Gpu2dTo())
  //report.printPruned()
  //val test = PipeSkidBuf(UInt(3 bits))
}

