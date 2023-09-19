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
 // val vgaCtrlIo = master(LcvVgaCtrlIo(rgbConfig=rgbConfig))
 // val vidDitherIo = master(LcvVideoDithererIo(rgbConfig=rgbConfig))
  val vgaCtrlIo = master(
    LcvVgaCtrlIo(rgbConfig=LcvVideoDithererIo.outRgbConfig)
  )
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
  val ctrlIo = io.vgaCtrlIo
  val dithIo = io.vidDitherIo
  //--------
  //val col = dithIo.inpCol
  //val tempSbIo = PipeSkidBufIo(
  //  dataType=Rgb(rgbConfig),
  //  optIncludeBusy=false,
  //)
  val skidBuf = PipeSkidBuf(
    dataType=Rgb(rgbConfig),
    optIncludeBusy=false,
    optPassthrough=false,
    //optTieIfwdValid=true,
  )
  val sbIo = skidBuf.io
  val sbPrevFire = sbIo.prev.fire
  sbIo.prev.valid := True
  val col = sbIo.prev.payload
  val rPastCol = Reg(Rgb(rgbConfig))
  rPastCol.init(rPastCol.getZero)
  //--------
  dithIo.push << sbIo.next
  val dithOutpCol = dithIo.outpCol

  //val dithToCtrlStm = master Stream(Rgb(rgbConfig))
  //dithToCtrlStm.valid := dithIo.pop.valid
  //dithToCtrlStm.payload := dithOutpCol
  //dithIo.pop.ready := dithToCtrlStm.ready

  ctrlIo.push.valid := dithIo.pop.valid
  ctrlIo.push.payload := dithOutpCol
  dithIo.pop.ready := ctrlIo.push.ready
  //--------
  // Enable VGA signal output
  ctrlIo.en := True
  //--------
  rPastCol := col
  when (sbPrevFire) {
    //m.d.sync += [
    //  drbus.inp.buf.prep.eq(0b1),
    //  dibus.inp.en.eq(0b1),
    //]

    //when (dithIo.outpPayload.nextPos.x === 0x0) 
    when (dithIo.outpPayload.pos.x === 0x0) {
      //m.d.sync += col.r.eq(0x0)
      col.r := 0x0
    } otherwise { // when (dithIo.outpPayload..pos.x > 0x0)
      //m.d.sync += col.r.eq(col.r + 0x1)
      col.r := rPastCol.r + 0x1
    }
    //m.d.sync += col.r.eq(col.r + 0x1)

    //m.d.sync += col.g.eq(0x0)
    //m.d.sync += col.b.eq(0x0)
    col.g := 0x0
    col.b := 0x0
  } otherwise { // when (~sbPrevFire)
    col := rPastCol
  }
  //--------
}
