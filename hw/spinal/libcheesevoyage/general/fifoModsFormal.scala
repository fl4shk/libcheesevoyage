package libcheesevoyage.general
import libcheesevoyage._

import spinal.core._
import spinal.core.formal._

//object FifoFormal extends App {
//  val depth = 4
//  new SpinalFormalConfig(_keepDebugInfo=true) 
//    .withBMC(20)
//    .withProve(20)
//    .doVerify(new Component {
//      val dut = FormalDut(Fifo(
//        dataType=UInt(4 bits),
//        depth=depth,
//      ))
//
//      //val inp = dut.io.inp
//      //val outp = dut.io.outp
//      val push = dut.io.push
//      val pop = dut.io.pop
//
//      assumeInitial(clockDomain.isResetActive)
//      //assumeInitial(~pastValid)
//      //assumeInitial(inp === inp.getZero)
//
//      assumeInitial(push.payload === push.payload.getZero)
//      assumeInitial(push.valid === push.valid.getZero)
//      anyseq(push.payload)
//      anyseq(push.valid)
//      assumeInitial(pop.ready === pop.ready.getZero)
//      anyseq(pop.ready)
//
//      //anyseq(inp)
//    })
//}
object AsyncReadFifoFormal extends App {
  val depth = 4
  new SpinalFormalConfig(_keepDebugInfo=true) 
    .withBMC(20)
    .withProve(20)
    .doVerify(new Component {
      val dut = FormalDut(AsyncReadFifo(
        dataType=UInt(4 bits),
        depth=depth,
      ))

      //val inp = dut.io.inp
      //val outp = dut.io.outp
      val push = dut.io.push
      val pop = dut.io.pop

      assumeInitial(clockDomain.isResetActive)
      //assumeInitial(~pastValid)
      //assumeInitial(inp === inp.getZero)

      assumeInitial(push.payload === push.payload.getZero)
      assumeInitial(push.valid === push.valid.getZero)
      anyseq(push.payload)
      anyseq(push.valid)
      assumeInitial(pop.ready === pop.ready.getZero)
      anyseq(pop.ready)
    })
}
