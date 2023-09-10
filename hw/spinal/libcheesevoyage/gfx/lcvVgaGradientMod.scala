package libcheesevoyage.gfx
import libcheesevoyage.general.PipeSkidBufIo
import libcheesevoyage.general.PipeSkidBuf
import libcheesevoyage.general.PsbIoParentData

import spinal.core._
import spinal.lib._
import spinal.lib.graphic.Rgb
import spinal.lib.graphic.RgbConfig
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer
import scala.math._


case class LcvVgaGradientIo(
  rgbConfig: RgbConfig
) extends Bundle //with IMasterSlave
{
  //--------
  val vgaCtrlIo = master(LcvVgaCtrlIo(rgbConfig=rgbConfig))
  val vidDitherIo = master(LcvVideoDithererIo(rgbConfig=rgbConfig))
  //--------
  //def asMaster(): Unit = {
  //  master(vgaCtrlIo)
  //  slave(vidDitherIo)
  //}
  //def asSlave(): Unit = {
  //  slave(vgaCtrlIo)
  //  master(vidDitherIo)
  //}
  //--------
}
case class LcvVgaGradient(
  rgbConfig: RgbConfig
) extends Component {
  //--------
  //val io = slave(LcvVgaGradientIo(rgbConfig=rgbConfig))
  val io = LcvVgaGradientIo(rgbConfig=rgbConfig)
  val ctrlio = io.vgaCtrlIo
  val dithio = io.vidDitherIo
  //--------
  //val col = dithio.inpCol
  //val tempSbIo = PipeSkidBufIo(
  //  dataType=Rgb(rgbConfig),
  //  optIncludeBusy=false,
  //)
  val skidBuf = PipeSkidBuf(
    dataType=Rgb(rgbConfig),
    optIncludeBusy=false,
    optPassthrough=false,
    optTieIfwdValid=true,
  )
  val sbIo = skidBuf.io
  val sbPrevFire = sbIo.prev.fire
  val col = sbIo.prev.payload
  //--------
  dithio.push << sbIo.next
  val dithOutpCol = dithio.outpCol

  //val dithToCtrlStm = master Stream(Rgb(rgbConfig))
  //dithToCtrlStm.valid := dithio.pop.valid
  //dithToCtrlStm.payload := dithOutpCol
  //dithio.pop.ready := dithToCtrlStm.ready

  ctrlio.push.valid := dithio.pop.valid
  ctrlio.push.payload := dithOutpCol
  dithio.pop.ready := ctrlio.push.ready
  //--------
  // Enable VGA signal output
  ctrlio.en := True
  //--------
  when (sbPrevFire) {
    //m.d.sync += [
    //  drbus.inp.buf.prep.eq(0b1),
    //  dibus.inp.en.eq(0b1),
    //]

    when (dithio.outpPayload.nextPos.x === 0x0) {
      //m.d.sync += col.r.eq(0x0)
      col.r := 0x0
    } otherwise { // If(dibus.outp.next_pos.x > 0x0)
      //m.d.sync += col.r.eq(col.r + 0x1)
      col.r := col.r + 0x1
    }
    //m.d.sync += col.r.eq(col.r + 0x1)

    //m.d.sync += col.g.eq(0x0)
    //m.d.sync += col.b.eq(0x0)
    col.g := 0x0
    col.b := 0x0
  }
  //otherwise { // when (~sbPrevFire)
  //}
  //--------
}
