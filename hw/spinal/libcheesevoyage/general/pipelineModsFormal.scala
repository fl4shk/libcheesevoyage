package libcheesevoyage.general
import libcheesevoyage._

import spinal.core._
import spinal.core.formal._

case class RgbColor(
  width: Int=4
) extends Bundle {
  val r = UInt(width bits)
  val g = UInt(width bits)
  val b = UInt(width bits)
}
object PipelineModsFormal extends App {
  //val optIncludeValidBusy = false
  //val optIncludeReadyBusy = false
  val optIncludeBusy = false
  //val optTieIfwdValid = false
  val optPassthrough = false
  val optFormal = true
  //FormalConfig
  new SpinalFormalConfig(_keepDebugInfo=true)
    .withBMC(10)
    .withProve(10)
    .doVerify(new Component {
      //val optIncludeValidBusy = false
      //val optIncludeReadyBusy = false
      ////val optTieIfwdValid = false
      //val optPassthrough = true
      val dut = FormalDut(PipeSkidBuf(
        //dataType=Vec(RgbColor(width=5), 8),
        dataType=Bool(),
        //dataType=UInt(1 bit),
        //optIncludeValidBusy=true,
        //optIncludeValidBusy=optIncludeValidBusy,
        //optIncludeReadyBusy=optIncludeReadyBusy,
        optIncludeBusy=optIncludeBusy,
        //optTieIfwdValid=optTieIfwdValid,
        optPassthrough=optPassthrough,
      ))
      val ifwdPayload = dut.io.prev.payload
      val ifwdValid = dut.io.prev.valid
      val obakReady = dut.io.prev.ready

      val ofwdPayload = dut.io.next.payload
      val ofwdValid = dut.io.next.valid
      val ibakReady = dut.io.next.ready

      val misc = dut.io.misc

      assumeInitial(clockDomain.isResetActive)
      assumeInitial(ifwdPayload === ifwdPayload.getZero)
      assumeInitial(~ifwdValid)
      //assumeInitial(~obakReady)
      //assumeInitial(ofwdPayload === ofwdPayload.getZero)
      //assumeInitial(~ofwdValid)
      assumeInitial(~ibakReady)
      assumeInitial(misc === misc.getZero)
      //anyseq(ifwdValid)
      //anyseq(ifwdPayload)
      anyseq(ifwdValid)
      anyseq(ifwdPayload)
      anyseq(ibakReady)
      //anyseq(dut.io.misc)
      anyseq(misc)

      // Provide some stimulus
      ////anyseq(dut.io.next.valid)
      ////anyseq(dut.io.next.payload)
      //anyseq(dut.io.prev.valid)
      //anyseq(dut.io.prev.payload)
      //anyseq(dut.io.next.ready)
      //anyseq(dut.io.misc)

      //anyseq(dut.io.prev.valid)
      //anyseq(dut.io.prev.payload)
      //anyseq(dut.io.next.ready)
      //anyseq(dut.io.misc)
    })
    //.doVerify(new PipeSkidBuf(
    //    //dataType=new Bundle {
    //    //  val r = UInt(4 bits)
    //    //  val g = UInt(4 bits)
    //    //  val b = UInt(4 bits)
    //    //},
    //    dataType=UInt(1 bit),
    //    //optIncludeValidBusy=true,
    //    optIncludeValidBusy=optIncludeValidBusy,
    //    optIncludeReadyBusy=optIncludeReadyBusy,
    //    //optTieIfwdValid=optTieIfwdValid,
    //    optPassthrough=optPassthrough,
    //    //optFormal=optFormal,
    //  )
    //)
}
