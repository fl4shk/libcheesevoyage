package libcheesevoyage.general

import spinal.core._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.misc.pipeline._
import scala.collection.mutable.ArrayBuffer
import scala.math._

import libcheesevoyage.Config


//case class PipeMemWrPayload[
//  T <: Data
//](
//  wordType: HardType[T],
//  wordCount: Int,
//) extends Bundle {
//  val data = wordType()
//  val addr = UInt(log2Up(wordCount) bits)
//}
//case class PipeMemRd[
//  T <: Data
//](
//  wordType: HardType[T],
//  wordCount: Int,
//) extends Bundle with IMasterSlave {
//  val addr = in UInt(log2Up(wordCount) bits)
//  val data = out(wordType())
//
//  def asMaster(): Unit = {
//    out(addr)
//    in(data)
//  }
//}
//
//case class PipeMemIo[
//  T <: Data
//](
//  wordType: HardType[T],
//  wordCount: Int,
//) extends Bundle with IMasterSlave {
//  val wr = slave(
//    Stream(
//      PipeMemWrPayload(
//        wordType=wordType,
//        wordCount=wordCount
//      )
//    )
//  )
//  //val rdPush = slave(Stream(UInt(log2Up(wordCount) bits)))
//  //val rdPop = master(Stream(wordType()))
//  val rd = master(
//    PipeMemRd(
//      wordType=wordType(),
//      wordCount=wordCount,
//    )
//  )
//
//  def asMaster(): Unit = {
//    master(wr)
//    slave(rd)
//    //master(rdPush)
//    //slave(rdPop)
//  }
//}
//
//case class PipeMem[
//  T <: Data
//](
//  wordType: HardType[T],
//  wordCount: Int,
//) extends Component {
//  val io = slave(
//    PipeMemIo(
//      wordType=wordType(),
//      wordCount=wordCount,
//    )
//  )
//  val pipe = PipeHelper(linkArr=PipeHelper.mkLinkArr())
//
//  val mem = Mem(
//    wordType=wordType(),
//    wordCount=wordCount,
//  )
//  mem.write(
//    address=io.wr.addr,
//    data=io.wr.data,
//    enable=io.wr.fire,
//  )
//  //io.rdPop.valid := True
//  io.rd.data := mem.readSync(
//    address=io.rd.addr,
//  )
//
//  Builder(pipe.linkArr.toSeq)
//}
//case class PipeMemTestWordType(
//  wordWidth: Int,
//  wordCount: Int,
//  idWidth: Int,
//) extends Bundle {
//}

case class PipeMemTestFrontPayload
//[
//  T <: Data
//]
(
  //wordType: HardType[T],
  wordWidth: Int,
  wordCount: Int,
  //memCount: Int,
) extends Bundle {
  //val addr = DualTypeNumVec2(
  //  dataTypeX=UInt(log2Up(wordCount) bits),
  //  dataTypeY=UInt(log2Up(memCount) bits),
  //)
  val addr = UInt(log2Up(wordCount) bits)
  val data = UInt(wordWidth bits) ////wordType()
}
//case class PipeMemRd[
//  T <: Data
//](
//  wordType: HardType[T],
//  wordCount: Int,
//) extends Bundle with IMasterSlave {
//  val addr = in UInt(log2Up(wordCount) bits)
//  val data = out(wordType())
//
//  def asMaster(): Unit = {
//    out(addr)
//    in(data)
//  }
//}
case class PipeMemTestBackPayload(
  wordWidth: Int,
  wordCount: Int,
  //memCount: Int,
  debug: Boolean=false,
) extends Bundle {
  val dbgFront = (debug) generate PipeMemTestFrontPayload(
    wordWidth=wordWidth,
    wordCount=wordCount,
  )
  val dbgRd = (debug) generate PipeMemTest.wordType()
  val sum = (
    //UInt((wordWidth + log2Up(memCount)) bits)
    //UInt((wordWidth + 1) bits)
    UInt((wordWidth) bits)
  )
  
}

case class PipeMemTestIo
//[
//  T <: Data
//]
(
  //wordType: HardType[T],
  wordCount: Int,
  //memCount: Int,
  debug: Boolean=false,
) extends Bundle /*with IMasterSlave*/ {

  def wordWidth = PipeMemTest.wordWidth
  def wordType() = PipeMemTest.wordType()

  val front = slave(Stream(
    PipeMemTestFrontPayload(
      //wordType=wordType(),
      wordWidth=wordWidth,
      wordCount=wordCount,
      //memCount=memCount,
    )
  ))
  val back = master(Stream(
    PipeMemTestBackPayload(
      wordWidth=wordWidth,
      wordCount=wordCount,
      //memCount=memCount,
      debug=debug,
    )
  ))

  //def asMaster(): Unit = {
  //  master(front)
  //}
}

object PipeMemTest {
  def wordWidth = 8
  def wordType() = UInt(wordWidth bits)
}

case class PipeMemTest
//[
//  T <: Data
//]
(
  //wordType: HardType[T],
  //wordWidth: Int,
  wordCount: Int,
  //memCount: Int,
) extends Component {
  //--------
  def debug: Boolean = {
    GenerationFlags.formal {
      return true
    }
    return false
  }
  //--------
  val io = PipeMemTestIo(
    //wordType=wordType(),
    //wordWidth=wordWidth,
    wordCount=wordCount,
    //memCount=memCount,
    debug=debug
  )
  //val memArr = Array.fill(memCount)(
  //  Mem(
  //    wordType=PipeMemTest.wordType(),
  //    wordCount=wordCount,
  //  )
  //)
  val mem = Mem(
    wordType=PipeMemTest.wordType(),
    wordCount=wordCount,
  )
  //val formal = new Area {
  //  val vec = Vec.fill(wordCount)(PipeMemTest.wordType())
  //}
  //--------
  val linkArr = PipeHelper.mkLinkArr()
  val pipe = PipeHelper(linkArr=linkArr)
  //case class PipePayload() {
  //  //--------
  //  val front = Payload(PipeMemTestFrontPayload(
  //    wordWidth=PipeMemTest.wordWidth,
  //    wordCount=wordCount,
  //    //memCount=memCount,
  //  ))

  //  //val rd = Vec.fill(memCount)(PipeMemTest.wordType())
  //  val rd = Payload(PipeMemTest.wordType())

  //  val back = Payload(PipeMemTestBackPayload(
  //    wordWidth=PipeMemTest.wordWidth,
  //    wordCount=wordCount,
  //    //memCount=memCount,
  //  ))
  //  //--------
  //}
  //val pipePayload = (
  //  //Payload(PipePayload())
  //  PipePayload()
  //)
  val pipePayload = new Area {
    //--------
    def mkFront() = PipeMemTestFrontPayload(
      wordWidth=PipeMemTest.wordWidth,
      wordCount=wordCount,
      //memCount=memCount,
    )
    val front = Payload(mkFront())

    //val rd = Vec.fill(memCount)(PipeMemTest.wordType())
    def mkRd() = PipeMemTest.wordType()
    val rd = Payload(mkRd())

    def mkBack() = PipeMemTestBackPayload(
      wordWidth=PipeMemTest.wordWidth,
      wordCount=wordCount,
      //memCount=memCount,
      debug=debug,
    )
    val back = Payload(mkBack())
  }

  //def printSize(): Unit = {
  //  println({
  //    val tempSize = pipe.cArr.size
  //    f"$tempSize"
  //  })
  //}


  //val cArr = new ArrayBuffer[CtrlLink]()
  def cArr = pipe.cArr
  val cFront = pipe.addStage("Front")
  //printSize()

  //for (stageIdx <- 0 until memCount) {
  //  cArr += pipe.addStage()
  //}
  //def frontPipePayload = cFront.down(pipePayload)

  val cSum = pipe.addStage("Sum")
  //printSize()
  //def wrPipePayload = cSum.down(pipePayload)

  val cBack = pipe.addStage("Back")
  //printSize()
  //val testificate = pipe.addStage()
  //def backPipePayload = cBack.down(pipePayload)

  val rdValid = (
    //Array.fill(cArr.size - 1)(Payload(Bool()))
    Payload(Bool())
  )

  //GenerationFlags.formal {
  //  when (pastValidAfterReset) {
  //    io.front.formalAssumesSlave(payloadInvariance=false)
  //    io.back.formalAssertsMaster(payloadInvariance=false)
  //  }
  //}

  pipe.first.up.driveFrom(io.front)(
    con=(node, payload) => {
      node(pipePayload.front) := payload
      //node(pipePayload).rd := node(pipePayload).rd.getZero
      //node(pipePayload).back := node(pipePayload).back.getZero
    }
  )
  val tempBackStm = cloneOf(io.back)
  pipe.last.down.driveTo(tempBackStm)(
    con=(payload, node) => {
      payload := node(pipePayload.back)
    }
  )
  io.back << tempBackStm.haltWhen(
    !(RegNextWhen(True, io.front.fire) init(False))
  )
  //--------
  val cFrontArea = new cFront.Area {
    //throwWhen(
    //  !(RegNextWhen(True, io.front.fire) init(False))
    //)
    //--------
    //def upFront = up(pipePayload.front)
    //def upRd = up(pipePayload.rd)
    //def upBack = up(pipePayload.back)
    //def upRdValid = up(rdValid)

    ////def downPipePayload = down(pipePayload)
    //def downFront = down(pipePayload.front)
    //def downRd = down(pipePayload.rd)
    //def downBack = down(pipePayload.back)
    //def downRdValid = down(rdValid)
    //--------
    //when (isValid) {
      //switch (pipePayload.front.addr.y) {
      //  for (memIdx <- 0 until memArr.size) {
      //    is (memIdx) {
      //      memArr(memIdx).write(
      //        address=pipePayload.front.addr.x,
      //        data=pipePayload.front.data,
      //      )
      //    }
      //  }
      //  default {
      //  }
      //}
      //def myPipePayload = cFront.down(pipePayload)
      //def front = myPipePayload.front
      //def rd = pipePayload.rd
      //mem.write(
      //  address=front.addr,
      //  data=front.data,
      //)
      val tempRdValid = (
        cSum.up(pipePayload.front).addr =/= up(pipePayload.front).addr
      )
      val upRdValid = Bool()
      //val rSavedUpRdValid = Reg(Bool()) init(True)
      //val rPastUpRdValid = RegNext(upRdValid) init(True)
      //haltWhen(!downRdValid)
      //terminateWhen(!downRdValid)

      //upRdValid := rPastUpRdValid
      //upRdValid := RegNext(upRdValid) init(True)
      up(rdValid) := (
        //tempRdValid
        upRdValid
      )
      duplicateWhen(
        //!cSum.up(rdValid)
        //!up(rdValid)
        //!down(rdValid)
        //!rPastUpRdValid
        //!upRdValid
        //!down(rdValid)
        //!cSum.up(rdValid)
        //!down(rdValid)
        //!rSavedUpRdValid
        //!down(rdValid)
        !upRdValid
      )
      upRdValid := tempRdValid
      //when (
      //  cBack.up(pipePayload.front).addr
      //  === down(pipePayload.front).addr
      //) {
      //  up(pipePayload.rd) := (
      //    cBack.up(pipePayload.back).sum
      //  )
      //} otherwise {
      //  up(pipePayload.rd) := (
      //    //mem.readSync
      //    mem.readAsync
      //    (
      //      address=up(pipePayload.front).addr,
      //    )
      //  )
      //}
      when (
        //cBack.up.isFiring
        //&& (
          cBack.up(pipePayload.front).addr
          === up(pipePayload.front).addr
        //)
      ) {
        up(pipePayload.rd) := (
          cBack.up(pipePayload.back).sum
        )
      } otherwise {
        up(pipePayload.rd) := (
          mem.readSync
          //mem.readAsync
          (
            address=up(pipePayload.front).addr,
          )
        )
      }
      val rTempCnt = (debug) generate (
        Reg(UInt(8 bits)) init(0x0)
      )

      //val rHadTempRdValid = (debug) generate (
      //  Reg(Bool()) init(False)
      //)
      //if (debug) {
      //  when (tempRdValid) {
      //    rHadTempRdValid := True
      //  }
      //}
      //when (cSum.up.isFiring) {
      //  upRdValid := True
      //}
      //when (tempRdValid) {
      //} otherwise {
      //}

      //when (up.isFiring) {
      //  //when (
      //  //  //!rPastUpRdValid
      //  //  !rSavedUpRdValid
      //  //  //&& !downRdValid
      //  //  //!down(rdValid)
      //  //) {
      //  //  upRdValid := True
      //  //  //rSavedUpRdValid := True

      //  //  ////terminateIt() // clear down.valid
      //  //  //haltIt()
      //  //  ////throwIt()
      //  //  ////downRdValid := (
      //  //  ////  
      //  //  ////)
      //  //} otherwise {
      //  //  upRdValid := tempRdValid
      //  //  //rSavedUpRdValid := tempRdValid
      //  //}
      //  //when (
      //  //  !rSavedUpRdValid
      //  //) {
      //  //  upRdValid := True
      //  //  rSavedUpRdValid := True
      //  //} otherwise {
      //  //}
      //  //when (
      //  //  !rSavedUpRdValid
      //  //) {
      //  //} otherwise {
      //  //}
      //  when (!down(rdValid)) {
      //    upRdValid := True
      //  } otherwise {
      //    upRdValid := tempRdValid
      //  }
      //} otherwise {
      //  upRdValid := False
      //}
      //elsewhen (
      //  down.isFiring
      //) {
      //}

      {
        if (debug) {
          when (
            //down.isFiring
            ////&& tempRdValid
            //&& 
            //up(rdValid)
            //tempRdValid
            //|| past(tempRdValid)
            //&& rHadTempRdValid
            //&& 
            upRdValid
            && up(pipePayload.front).data > 0
          ) {
            //rHadTempRdValid := False
            rTempCnt := rTempCnt + 1
          }
        }
        //{
        //  //haltWhen(
        //  //  //(
        //  //  //  front.addr === wrPipePayload.front.addr
        //  //  //) || (
        //  //  //  front.addr === backPipePayload.front.addr
        //  //  //)
        //  //  //!wrPipePayload.rd.valid
        //  //  //!rd.valid
        //  //  !cSum.up(rdValid)
        //  //  //|| !cBack.up(rdValid)
        //  //)
        //  //when (cSum.up.valid) {
        //  //}
        //}
      }
    //}
    val rIsFiringCnt = (debug) generate (
      Reg(UInt(8 bits)) init(0x0)
    )
    //--------
    GenerationFlags.formal {
      //--------
      //assumeInitial(
      //  down(pipePayload.rd) === 0x0
      //)
      //assumeInitial(downRdValid === downRdValid.getZero)
      //--------
      //cover(
      //  cSum.down(rdValid)
      //  && Mux[Bool](
      //    pastValidAfterReset,
      //    !past(cSum.down(rdValid)),
      //    True
      //  ) && Mux[Bool](
      //    past(pastValidAfterReset),
      //    past(past(cSum.down(rdValid))),
      //    True
      //  )
      //)
      //val rCnt = Reg(UInt(8 bits)) init(0x0)
      //--------
      //--------
      when (up.isFiring) {
        rIsFiringCnt := rIsFiringCnt + 1
      }
      when (pastValidAfterReset) {
        //when (
        //  past(up.isFiring)
        //) {
        //  when (
        //    past(cBack.up(pipePayload.front).addr)
        //    === cSum.up(pipePayload.front).addr
        //  ) {
        //    assert(
        //      past(cSum.up(pipePayload.rd))
        //      === past(cBack.up(pipePayload.rd))
        //    )
        //  } otherwise {
        //    assert(
        //      cSum.up(pipePayload.rd) === past(
        //        mem.readAsync(
        //          address=down(pipePayload.front).addr
        //        )
        //      )
        //    )
        //  }
        //}
        //when (past(cSum.up.isFiring)) {
        //  when (
        //    cBack.up
        //  ) {
        //  } otherwise {
        //  }
        //}
        val rPrevCSumFront = Reg(pipePayload.mkFront())
        rPrevCSumFront.init(rPrevCSumFront.getZero)
        val rPrevCBackFront = Reg(pipePayload.mkFront())
        rPrevCBackFront.init(rPrevCBackFront.getZero)
        //when (cSum.up.isFiring) {
        //}

        when (
          //past(cSum.up.isFiring)
          //&& past(cBack.up.isFiring)
          //&& 
          (
            (RegNextWhen(True, io.front.fire) init(False))
            || io.front.fire
          ) && (
            (RegNextWhen(True, cFront.up.isFiring) init(False))
            || cFront.up.isFiring
          ) && (
            (RegNextWhen(True, cSum.up.isFiring) init(False))
            || cSum.up.isFiring
          ) && (
            (RegNextWhen(True, cBack.up.isFiring) init(False))
            || cBack.up.isFiring
          ) && (
            //(RegNextWhen(True, io.back.fire) init(False))
            //|| io.back.fire
            True
          ) && (
            //cSum.up(pipePayload.front).addr
            cFront.up(pipePayload.front).addr
            === cBack.up(pipePayload.front).addr
          )
        ) {
          assert(
            //cSum.up(pipePayload.rd)
            //=== cBack.up(pipePayload.back).sum
            //past(cFront.down(pipePayload.rd))
            //=== past(cSum.down(pipePayload.back).sum)
            (
              //RegNextWhen(
              //  cSum.up(pipePayload.rd), cSum.up.isFiring
              //) init(cSum.up(pipePayload.rd).getZero)
              //cBack.up(pipePayload.rd)
              //cSum.down(pipePayload.rd)
              cFront.down(pipePayload.rd)
            )
            === (
              cBack.up(pipePayload.back).sum
            )
          )
        }
      }
      cover(
        //up.isFiring
        //&& 
        //rTempCnt === 1
        //rIsFiringCnt === 2
        rIsFiringCnt >= 5
        && rTempCnt >= 5
        //&& rTempCnt > 3
      )
    }
    //--------
  }
  //--------
  val cSumArea = new cSum.Area {
    //--------
    haltWhen(
      !(RegNextWhen(True, io.front.fire) init(False))
    )
    //when (isValid) {
    //  def front = pipePayload.front
    //  def rd = pipePayload.rd
    //  def back = pipePayload.back
    //  //back
    //  back.sum := 
    //}
    //val rReadyCnt = Reg(SInt(4 bits)) init(0x0)
    //def upPipePayload = up(pipePayload)
    def upFront = up(pipePayload.front)
    def upRd = up(pipePayload.rd)
    //def upBack = up(pipePayload.back)
    val upBack = pipePayload.mkBack()
    def upRdValid = up(rdValid)

    ////def downPipePayload = down(pipePayload)
    //def downFront = down(pipePayload.front)
    //def downRd = down(pipePayload.rd)
    //def downBack = down(pipePayload.back)
    ////def downRdValid = down(rdValid)

    //when (
    //  isValid
    //  //&& !myRdValid
    //) {
    //}
    //downRdValid := downFront.addr =/= upFront.addr
    //val rPastDownRdValid = RegNext(downRdValid) init(False)
    ////haltWhen(!downRdValid)
    //terminateWhen(!downRdValid)
    //when (
    //  rPastDownRdValid
    //  //&& !downRdValid
    //) {
    //  downRdValid := downFront.addr =/= upFront.addr

    //  ////terminateIt() // clear down.valid
    //  //haltIt()
    //  ////throwIt()
    //  ////downRdValid := (
    //  ////  
    //  ////)
    //} otherwise {
    //  downRdValid := True
    //}
    //throwWhen(!downRdValid)
    val tempSum = up(pipePayload.rd) + up(pipePayload.front).data
    when (
      up.isValid
      //downRdValid
    ) {
      //up(pipePayload.back).sum 
      upBack.sum := (
        tempSum
      )
    } otherwise {
      //up(pipePayload.back).sum 
      upBack.sum := (
        RegNext(upBack.sum) init(upBack.sum.getZero)
        //upRd
      )
    }
    up(pipePayload.back) := upBack
    //val rDidFirstFire = Reg(Bool()) init(False)
    val rDidFirstFire = (
      RegNextWhen(True, up.isFiring) init(False)
    )
    //val rPastIsFiring = Reg(Bool()) init(False)

    //rPastIsFiring := down.isFiring

    val rPrevUpFront = (
      Reg(pipePayload.mkFront()) init(pipePayload.mkFront().getZero)
    )
    val rPrevDownFront = (
      Reg(pipePayload.mkFront()) init(pipePayload.mkFront().getZero)
    )
    val rPrevUpRd = (
      Reg(pipePayload.mkRd()) init(pipePayload.mkRd().getZero)
    )
    val rPrevDownRd = (
      Reg(pipePayload.mkRd()) init(pipePayload.mkRd().getZero)
    )
    val rPrevUpBack = (
      Reg(pipePayload.mkBack()) init(pipePayload.mkBack().getZero)
    )
    //val rPrevDownBack = (
    //  Reg(pipePayload.mkBack()) init(pipePayload.mkBack().getZero)
    //)
    //val rPrevDownRdValid = (
    //  Reg(Bool()) init(downRdValid.getZero)
    //)
    //--------
    GenerationFlags.formal {
      //--------
      if (debug) {
        upBack.dbgFront := up(pipePayload.front)
        upBack.dbgRd := up(pipePayload.rd)
      }
      //--------
      //assumeInitial(downFront === downFront.getZero)
      //assumeInitial(downRd === downRd.getZero)
      //assumeInitial(downBack === downBack.getZero)
      //assumeInitial(downRdValid === downRdValid.getZero)
      //--------

      when (pastValidAfterReset) {
        //when (up.isFiring) {
        //  rDidFirstFire := True
        //  //rPrevUpFront := up(pipePayload.front)
        //  ////rPrevDownFront := down(pipePayload.front)
        //  //rPrevUpRd := up(pipePayload.rd)
        //  ////rPrevDownRd := down(pipePayload.rd)
        //  ////rPrevUpBack := up(pipePayload.back)
        //  ////rPrevDownBack := down(pipePayload.back)
        //  ////rPrevDownRdValid := down(rdValid)
        //}
        //when (rDidFirstFire) {
          //when (
          //  //isValid
          //  past(down.isFiring)
          //) {
          //  assert(
          //    downBack.sum === past(upRd) + past(upFront.data)
          //  )
          //} otherwise {
          //  assert(
          //    downBack.sum === past(downBack.sum)
          //  )
          //}
          when (past(
            up.isFiring
            //up.isValid
          )) {
            //assert(
            //  down(pipePayload.back).sum
            //    === past(up(pipePayload.rd))
            //    + past(up(pipePayload.front).data)
            //)
            assert(
              //upBack.sum === rPrevUpRd + rPrevUpFront.data
              //past(
              //upBack.sum
              //)
              past(upBack.sum)
              === (
                past(upBack.dbgFront.data)
                + past(upBack.dbgRd)
              )
              //cBack.up(pipePayload.back).sum
              //=== (
              //  //upBack.dbgFront.data
              //  //+ upBack.dbgRd
              //  //past(tempSum)
              //  cBack.up(pipePayload.back).dbgFront.data
              //  + cBack.up(pipePayload.back).dbgRd
              //)
            )
          } otherwise {
            //assert(stable(downBack.sum))
          }
        //}
      }
    }
    //--------
  }
  //--------
  val cBackArea = new cBack.Area {
    haltWhen(
      !(RegNextWhen(True, io.front.fire) init(False))
    )
    //--------
    //def downFront = down(pipePayload.front)
    //def downRd = down(pipePayload.rd)
    //def downBack = down(pipePayload.back)
    //assumeInitial(downFront === downFront.getZero)
    //assumeInitial(downRd === downRd.getZero)
    //assumeInitial(downBack === downBack.getZero)
    //--------
    //def downRdValid = down(rdValid)

    //downRdValid := (
    //  downFront.addr =/= up(pipePayload).front.addr
    //)

    when (isValid) {
      //def front = pipePayload.front
      //def rd = pipePayload.rd
      //def back = pipePayload.back
      mem.write(
        address=up(pipePayload.front).addr,
        data=up(pipePayload.back).sum,
      )
    }
    //val rCoverInvIsFiringCnt = Reg(UInt(8 bits)) init(0x0)
    val rCoverDiffData = Vec.fill(8)(
      Reg(PipeMemTest.wordType()) init(0x0)
    )
    val rCoverDiffDataCnt = Reg(UInt(8 bits)) init(0x0)
    val rCoverInvIsFiring = Reg(Bool()) init(False)
    val rCoverInvCnt = Reg(UInt(8 bits)) init(0x0)
    val rCoverSameIsFiring = Reg(Bool()) init(False)
    val rCoverSameCnt = Reg(UInt(8 bits)) init(0x0)

    GenerationFlags.formal {
      when (pastValidAfterReset) {
        //when (
        //  past(pastValidAfterReset)
        //) {
          when (up.isFiring) {
            rCoverInvIsFiring := True
            rCoverSameIsFiring := True
          }
          //when (
          //  down(rdValid)
          //  && !past(down(rdValid))
          //) {
          //  //when (down.isFiring /*|| rCoverInvIsFiring*/) {
          //    //cover(past(down(rdValid)))
          //    rCoverInvCnt := rCoverInvCnt + 1
          //    //rCoverInvIsFiring := False
          //  //}
          //}
          when (
            up.isFiring
            //&& 
            //io.back.valid
            //up.valid
            && rCoverDiffDataCnt < rCoverDiffData.size
          ) {
            val firstSame = rCoverDiffData.sFindFirst(
              _ === up(pipePayload.front).data
            )
            when (!firstSame._1) {
              rCoverDiffData(rCoverDiffDataCnt.resized) := (
                up(pipePayload.front).data
              )
              rCoverDiffDataCnt := rCoverDiffDataCnt + 1
            }
          }
          cover(
            rCoverDiffDataCnt === rCoverDiffData.size
            //rCoverDiffDataCnt === 1
          )
          //val rHadNonZeroData = Vec.fill(2){
          //  val temp = Reg(Flow(PipeMemTest.wordType()))
          //  temp.init(temp.getZero)
          //  temp
          //}

          //when (
          //  io.front.fire
          //  && (
          //    io.front.data =/= 0
          //  )
          //) {
          //  rHadNonZeroData.valid := True
          //  rHadNonZeroData.payload := io.front.data
          //}
          //cover(
          //  rHadNonZeroData.valid
          //  //&& io.back.valid
          //  //&& io.back.sum === rHadNonZeroData.payload
          //  && up.valid
          //  && up(pipePayload.front).data === rHadNonZeroData.payload
          //)

          //when (
          //  up(rdValid)
          //  //&& past(up(rdValid))
          //  //&& stable(up(pipePayload.front).addr)
          //  //&& !stable(up(pipePayload.front).data)
          //) {
          //  //when (up.isFiring /*|| rCoverSameIsFiring*/) {
          //    rCoverSameCnt := rCoverSameCnt + 1
          //  //}
          //}
          //cover(rCoverInvCnt === 3)
          //cover(rCoverSameCnt === 3)
          //cover(
          //  //rCoverInvCnt === 3
          //  //&& 
          //  rCoverSameCnt === 3
          //)
          //cover(!stable(up(rdValid)))
          //cover(!up(rdValid))
          //cover(up(rdValid))
        //}
      }
    }
  }
  //--------
  Builder(linkArr.toSeq)
  //--------
}
//--------
object PipeMemTestToVerilog extends App {
  Config.spinal.generateVerilog(
    PipeMemTest(
      wordCount=8
    )
  )
}
//--------
case class PipeMemTestSimDutIo(
  wordCount: Int,
) extends Bundle {
  val sum = out(PipeMemTest.wordType())
}
object PipeMemTestSimDut {
  def tempWidth = 32.max(PipeMemTest.wordWidth * 4)
  def tempType() = UInt(tempWidth bits)
  def mkTempUInt[
    T
  ](
    value: T,
  ) = U(f"$tempWidth'd$value")
}
case class PipeMemTestSimDut(
  wordCount: Int,
) extends Component {
  //--------
  //val io = new Bundle {
  //  //val sum = out(PipeMemTest.wordType())
  //}
  //--------
  val loc = new Area {
    val nextCnt = PipeMemTestSimDut.tempType()
    val rCnt = RegNext(nextCnt) init(0x0)
  }
  //--------
}
object PipeMemTestSim extends App {
}
