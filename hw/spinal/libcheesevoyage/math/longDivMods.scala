package libcheesevoyage.math
import libcheesevoyage.general._

import scala.math._
import spinal.core._
import spinal.lib._
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer

case class LongDivInp(cfg: LongDivConfig) extends Bundle {
  val valid = (!cfg.pipelined) generate (Bool())
  val numer = UInt(cfg.mainWidth bits)
  val denom = UInt(cfg.denomWidth bits)
  val signed = Bool()
  val tag = (cfg.pipelined) generate (UInt(cfg.tagWidth bits))
}
case class LongDivOutp(cfg: LongDivConfig) extends Bundle {
  val ready = (!cfg.pipelined) generate (Bool())
  val quot = UInt(cfg.mainWidth bits)
  val rema = UInt(cfg.mainWidth bits)
  val tag = (cfg.pipelined) generate (UInt(cfg.tagWidth bits))
}

case class LongDivIo(cfg: LongDivConfig) extends Bundle {
  val inp = in(LongDivInp(cfg=cfg))
  val outp = out(LongDivOutp(cfg=cfg))
}

case class LongDivMultiCycleMultiChunkArea(
  io: LongDivIo,
  cfg: LongDivConfig,
) extends Area {
  if (cfg.formal) {
  }
  def inp = io.inp
  def outp = io.outp
  val udivIter = LongUdivIter(cfg=cfg)

  //--------
  def itdIn = udivIter.io.itdIn
  def chunkStart = udivIter.io.chunkStart
  //--------
  def itdOut = udivIter.io.itdOut
  def quotDigit = udivIter.io.quotDigit
  def shiftInRema = udivIter.io.shiftInRema
  def gtVec = udivIter.io.gtVec
  object State extends SpinalEnum(defaultEncoding=binarySequential) {
    val
      IDLE,
      RUNNING
      = newElement()
  }
  val rState = (
    Reg(State())
    init(State.IDLE)
  )
  val tempNumer = cfg.buildTempShape()
  //println(
  //  s"chunkWidth:${chunkWidth} "
  //  + s"numChunks:${cfg.numChunks()} "
  //  + s"tempShapeWidth:${cfg.tempShapeWidth} "
  //  + s"tempNumer.getWidth:${tempNumer.getWidth}"
  //)
  val tempDenom = cfg.buildTempShape()
  val rTempQuot = Reg(cfg.buildTempShape()) init(0x0)
  val rTempRema = Reg(cfg.buildTempShape()) init(0x0)
  tempNumer := RegNext(next=tempNumer, init=tempNumer.getZero)
  tempDenom := RegNext(next=tempDenom, init=tempDenom.getZero)
  //tempQuot := RegNext(next=tempQuot, init=tempQuot.getZero)
  //tempRema := RegNext(next=tempRema, init=tempRema.getZero)
  itdIn.tempNumer := tempNumer.resized
  itdIn.tempDenom := tempDenom.resized
  itdIn.tempQuot := rTempQuot
  itdIn.tempRema := rTempRema
  val chunkStartBegin = cfg.numChunks() //- 1
  val rPrevChunkStart = RegNext(next=chunkStart, init=chunkStart.getZero)
  chunkStart := rPrevChunkStart
  val denomMultLut = /*Reg*/(
    Vec.fill(cfg.dmlSize())(
      UInt(cfg.dmlElemWidth() bits)
    )
  )
  for (idx <- 0 until cfg.dmlSize()) {
    //rDenomMultLut(idx).init(rDenomMultLut(idx).getZero)
    denomMultLut(idx) := (
      RegNext(next=denomMultLut(idx), init=denomMultLut(idx).getZero)
    )
    itdIn.denomMultLut(idx) := denomMultLut(idx)
  }
  val rInpSigned = Reg(Bool(), init=False)
  val nextNumerWasSgnLtz = Bool()
  val rNumerWasSgnLtz = RegNext(nextNumerWasSgnLtz, init=False)
  nextNumerWasSgnLtz := rNumerWasSgnLtz
  val nextDenomWasSgnLtz = Bool()
  val rDenomWasSgnLtz = RegNext(nextDenomWasSgnLtz, init=False)
  nextDenomWasSgnLtz := rDenomWasSgnLtz

  switch (rState) {
    is (State.IDLE) {
      //when (RegNext(rState) === State.RUNNING) {
      //  outp.ready := True
      //}
      when (inp.valid) {
        //nextNumerWasSgnLtz := inp.numer.
        rTempQuot := 0x0
        rTempRema := 0x0
        rInpSigned := inp.signed
        when (!inp.signed) {
          tempNumer := inp.numer.resized
          tempDenom := inp.denom.resized
          nextNumerWasSgnLtz := False
          nextDenomWasSgnLtz := False
        } otherwise {
          //--------
          val tempSignedNumer = Vec.fill(2)(
            SInt(cfg.tempShapeWidth bits)
          )
          val tempSignedDenom = Vec.fill(2)(
            SInt(cfg.tempShapeWidth bits)
          )
          tempSignedNumer(0) := inp.numer.asSInt.resized
          tempSignedDenom(0) := inp.denom.asSInt.resized
          when (nextNumerWasSgnLtz) {
            tempSignedNumer(1) := ((~tempSignedNumer(0)) + 1)
          } otherwise {
            tempSignedNumer(1) := tempSignedNumer(0)
          }
          when (nextDenomWasSgnLtz) {
            tempSignedDenom(1) := ((~tempSignedDenom(0)) + 1)
          } otherwise {
            tempSignedDenom(1) := tempSignedDenom(0)
          }
          tempNumer := tempSignedNumer.last.asUInt
          tempDenom := tempSignedDenom.last.asUInt
          //--------
          nextNumerWasSgnLtz := inp.numer.msb
          nextDenomWasSgnLtz := inp.denom.msb
          //--------
        }
        chunkStart := chunkStartBegin
        outp.ready := False
        rState := State.RUNNING
        rPrevChunkStart := chunkStartBegin
        for (idx <- 0 until cfg.dmlSize()) {
          denomMultLut(idx) := (tempDenom * idx).resized
        }
      }
      if (cfg.formal) {
        when (pastValidAfterReset) {
          cover(past(rState) === State.RUNNING)
          when (past(rState) === State.RUNNING) {
            assert(outp.ready)
            when (
              //past(tempNumer) =/= 0x0
              //&& 
              past(tempDenom) =/= 0x0
            ) {
              when (!rInpSigned) {
                cover(
                  outp.quot
                  === (past(tempNumer) / past(tempDenom)).resized
                )
                cover(
                  outp.rema
                  === (past(tempNumer) % past(tempDenom)).resized
                )
                assert(
                  outp.quot
                  === (past(tempNumer) / past(tempDenom)).resized
                )
                assert(
                  outp.rema
                  === (past(tempNumer) % past(tempDenom)).resized
                )
              } otherwise {
                //cover(
                //  outp.quot.asSInt
                //  === (
                //    past(tempNumer.asSInt) / past(tempDenom.asSInt)
                //  ).resized
                //)
                //cover(
                //  outp.rema.asSInt
                //  === (
                //    past(tempNumer.asSInt) % past(tempDenom.asSInt)
                //  ).resized
                //)
                val myTempNumer = (
                  Mux[SInt](
                    past(rNumerWasSgnLtz),
                    past((~tempNumer) + 1).asSInt,
                    past(tempNumer).asSInt,
                  )
                )
                val myTempDenom = (
                  Mux[SInt](
                    past(rDenomWasSgnLtz),
                    past((~tempDenom) + 1).asSInt,
                    past(tempDenom.asSInt),
                  )
                )
                assert(
                  Mux[UInt](
                    (past(rNumerWasSgnLtz) =/= past(rDenomWasSgnLtz)),
                    ((~past((itdOut.tempQuot))) + 1),
                    past(itdOut.tempQuot),
                  )(cfg.mainWidth - 1 downto 0)
                  //outp.quot.asSInt.resized
                  === (
                    (myTempNumer / myTempDenom).asUInt
                    (cfg.mainWidth - 1 downto 0)
                  )
                )
                assert(
                  //outp.rema.asSInt.resized
                  //past(rTempRema)
                  Mux[UInt](
                    past(rNumerWasSgnLtz),
                    ((~past((itdOut.tempRema))) + 1),
                    past(itdOut.tempRema),
                  )(cfg.mainWidth - 1 downto 0)
                  === (
                    (myTempNumer % myTempDenom).asUInt
                    (cfg.mainWidth - 1 downto 0)
                  )
                )
                //when (rNumerWasSgnLtz =/= rDenomWasSgnLtz) {
                //  //outp.quot := ((~myTempQuot) + 1).resized
                //} otherwise {
                //  //assert(
                //  //  outp.quot
                //  //)
                //}
                //when (rNumerWasSgnLtz) {
                //  // This is C's rule for signed remainder
                //  //outp.rema := ((~myTempRema) + 1).resized
                //}
                //assert(
                //  outp.quot.asSInt
                //  === (
                //    past(tempNumer.asSInt) / past(tempDenom.asSInt)
                //  ).resized
                //)
                //assert(
                //  outp.rema.asSInt
                //  === (
                //    past(tempNumer.asSInt) % past(tempDenom.asSInt)
                //  ).resized
                //)
              }
            }
          }
        }
      }
    }
    is (State.RUNNING) {
      if (cfg.formal) {
        when (pastValidAfterReset) {
          assume(stable(tempNumer))
          assume(stable(tempDenom))
          //assume(stable(inp.signed))
          //when (stable(rState)) {
            for (idx <- 0 until cfg.dmlSize()) {
              //assert(stable(rDenomMultLut(idx)))
              assert(denomMultLut(idx) === (tempDenom * idx).resized)
            }
          //}
        }
      }
      //when (inp.valid) {
        when (chunkStart > 0) {
          rTempQuot := itdOut.tempQuot
          rTempRema := itdOut.tempRema
        } otherwise {
          outp.ready := True
          val myTempQuot = itdOut.tempQuot
          val myTempRema = itdOut.tempRema
          outp.quot := myTempQuot.resized
          outp.rema := myTempRema.resized
          when (rInpSigned) {
            when (rNumerWasSgnLtz =/= rDenomWasSgnLtz) {
              outp.quot := ((~myTempQuot) + 1).resized
            }
            when (rNumerWasSgnLtz) {
              // This is C's rule for signed remainder
              outp.rema := ((~myTempRema) + 1).resized
            }
          }

          rState := State.IDLE
        }
        chunkStart := rPrevChunkStart - 1
      //}
    }
  }
}
case class LongDivMultiCycle(
  //cfg: LongDivParams
  mainWidth: Int,
  denomWidth: Int,
  chunkWidth: Int,
  signedReset: BigInt=0x0,
  formal: Boolean=false,
) extends Component {
  val cfg = LongDivConfig(
    mainWidth=mainWidth,
    denomWidth=denomWidth,
    chunkWidth=chunkWidth,
    tagWidth=1,
    pipelined=false,
    usePipeSkidBuf=false,
    formal=formal,
  )
  val io = LongDivIo(cfg=cfg)
  def inp = io.inp
  def outp = io.outp

  outp.ready.setAsReg
  outp.ready.init(False)
  //:= RegNext(next=outp.ready, init=outp.ready.getZero)
  outp.quot := RegNext(next=outp.quot, init=outp.quot.getZero)
  outp.rema := RegNext(next=outp.rema, init=outp.rema.getZero)
  //outp.quot.setAsReg
  //outp.quot.init(0x0)
  //outp.rema.setAsReg
  //outp.rema.init(0x0)

  val singleChunkWidthArea: Area = (
    chunkWidth == 1
  ) generate new Area {
    //val rTempNumer = Reg(cfg.buildTempShape()) init(0x0)
    val rTempNumer = Reg(
      Vec.fill(cfg.tempShapeWidth)(
        Bool()
      )
    )
    //println(
    //  s"chunkWidth:${chunkWidth} "
    //  + s"numChunks:${cfg.numChunks()} "
    //  + s"tempShapeWidth:${cfg.tempShapeWidth} "
    //  + s"tempNumer.getWidth:${tempNumer.getWidth}"
    //)
    val rTempDenom = Reg(cfg.buildTempShape()) init(0x0)
    //val rTempDenom = Reg(
    //  Vec.fill(cfg.tempShapeWidth)(
    //    Bool()
    //  )
    //)
    //val rTempQuot = Reg(cfg.buildTempShape()) init(0x0)
    val rTempQuot = Reg(
      Vec.fill(cfg.tempShapeWidth)(
        Bool()
      )
    )
    val rTempRema = Reg(cfg.buildTempShape()) init(0x0)
    //val rTempRema = Reg(
    //  Vec.fill(cfg.tempShapeWidth)(
    //    Bool()
    //  )
    //)
    for (idx <- 0 until cfg.tempShapeWidth) {
      rTempNumer(idx).init(rTempNumer(idx).getZero)
      //rTempDenom(idx).init(rTempDenom(idx).getZero)
      rTempQuot(idx).init(rTempQuot(idx).getZero)
      //rTempRema(idx).init(rTempRema(idx).getZero)
    }
    //tempNumer := RegNext(next=tempNumer, init=tempNumer.getZero)
    //tempDenom := RegNext(next=tempDenom, init=tempDenom.getZero)
    val rInpSigned = Reg(Bool(), init=False)
    val nextNumerWasSgnLtz = Bool()
    val rNumerWasSgnLtz = RegNext(nextNumerWasSgnLtz, init=False)
    nextNumerWasSgnLtz := rNumerWasSgnLtz
    val nextDenomWasSgnLtz = Bool()
    val rDenomWasSgnLtz = RegNext(nextDenomWasSgnLtz, init=False)
    nextDenomWasSgnLtz := rDenomWasSgnLtz
    //val rCnt = Reg(UInt(log2Up(rTempNumer.getWidth) + 2 bits)) init(0x0)
    val rCnt = (
      Reg(SInt(log2Up(rTempNumer.size) + 2 bits)) 
      init(-1)
    )
    object State
    extends SpinalEnum(defaultEncoding=binaryOneHot) {
      val
        IDLE,
        CAPTURE_INPUTS_PIPE,
        RUNNING,
        YIELD_RESULT_PIPE_1,
        YIELD_RESULT
        = newElement()
    }
    val rState = (
      Reg(State())
      init(State.IDLE)
    )
    switch (rState) {
      is (State.IDLE) {
        when (inp.valid) {
          //rTempQuot := 0x0
          rTempQuot.foreach(myQuotBit => {
            myQuotBit := False
          })
          rTempRema := 0x0
          rInpSigned := inp.signed
          //rTempNumer := inp.numer
          rTempNumer.assignFromBits(inp.numer.asBits)
          rTempDenom := inp.denom.resized
          //rTempDenom.assignFromBits(inp.denom.asBits)
          rState := State.CAPTURE_INPUTS_PIPE
          outp.ready := False
        }
      }
      is (State.CAPTURE_INPUTS_PIPE) {
        rCnt := (
          //rTempNumer.getWidth - 1
          rTempNumer.size - 1
        )
        when (!rInpSigned) {
          //tempNumer := inp.numer.resized
          //tempDenom := inp.denom.resized
          nextNumerWasSgnLtz := False
          nextDenomWasSgnLtz := False
        } otherwise {
          //--------
          val tempSignedNumer = Vec.fill(2)(
            SInt(cfg.tempShapeWidth bits)
          )
          val tempSignedDenom = Vec.fill(2)(
            SInt(cfg.tempShapeWidth bits)
          )
          tempSignedNumer(0) := rTempNumer.asBits.asSInt.resized//inp.numer.asSInt.resized
          tempSignedDenom(0) := rTempDenom.asBits.asSInt.resized//inp.denom.asSInt.resized
          when (nextNumerWasSgnLtz) {
            tempSignedNumer(1) := ((~tempSignedNumer(0)) + 1)
          } otherwise {
            tempSignedNumer(1) := tempSignedNumer(0)
          }
          when (nextDenomWasSgnLtz) {
            tempSignedDenom(1) := ((~tempSignedDenom(0)) + 1)
          } otherwise {
            tempSignedDenom(1) := tempSignedDenom(0)
          }
          //rTempNumer := tempSignedNumer.last.asUInt
          rTempNumer.assignFromBits(tempSignedNumer.last.asBits)
          rTempDenom := tempSignedDenom.last.asUInt
          //rTempDenom.assignFromBits(tempSignedDenom.last.asBits)
          //--------
          nextNumerWasSgnLtz := (
            //rTempNumer.msb//inp.numer.msb
            rTempNumer.last
          )
          nextDenomWasSgnLtz := (
            rTempDenom.msb//inp.denom.msb
            //rTempDenom.last
          )
          //--------
        }
        rState := State.RUNNING
      }
      is (State.RUNNING) {
        //rCnt := rCnt - 1
        //when (!rCnt.msb) {
        //  ////switch (rCnt) {
        //  ////  for (myCnt <- 0 until rTempNumer.getWidth) {
        //  ////    is (myCnt) {
        //  //      val nextTempRema = Vec.fill(2)(
        //  //        UInt(rTempRema.getWidth bits)
        //  //      )
        //  //      nextTempRema(0) := Cat(
        //  //        rTempRema,
        //  //        rTempNumer(rCnt.resized),
        //  //      ).asUInt(rTempRema.bitsRange)
        //  //      nextTempRema(1) := nextTempRema(0)
        //  //      when (nextTempRema(0) >= rTempDenom) {
        //  //        nextTempRema(1) := nextTempRema(0) - rTempDenom
        //  //        rTempQuot(rCnt.resized) := True
        //  //      }
        //  //      rTempRema := nextTempRema(1)
        //  ////    }
        //  ////  }
        //  ////}
        //} otherwise {
        //}
        when (rCnt.msb) {
          rState := State.YIELD_RESULT_PIPE_1
        }
      }
      is (State.YIELD_RESULT_PIPE_1) {
        when (rInpSigned) {
          when (rNumerWasSgnLtz =/= rDenomWasSgnLtz) {
            //rTempQuot := ((~rTempQuot) + 1).resized
            rTempQuot.assignFromBits(
              ((~rTempQuot.asBits.asUInt) + 1).resized.asBits
            )
          }
          when (rNumerWasSgnLtz) {
            // This is C's rule for signed remainder
            rTempRema := ((~rTempRema) + 1).resized
          }
        }
        rState := State.YIELD_RESULT
      }
      is (State.YIELD_RESULT) {
        outp.ready := True
        //outp.quot := rTempQuot
        outp.quot := rTempQuot.asBits.asUInt
        outp.rema := rTempRema
        rState := State.IDLE
      }
    }
    when (!rCnt.msb) {
      rCnt := rCnt - 1
      //switch (rCnt) {
      //  for (myCnt <- 0 until rTempNumer.getWidth) {
      //    is (myCnt) {
            val nextTempRema = Vec.fill(2)(
              UInt(rTempRema.getWidth bits)
            )
            nextTempRema(0) := Cat(
              rTempRema,
              rTempNumer(rCnt.asUInt.resized),
            ).asUInt(rTempRema.bitsRange)
            nextTempRema(1) := nextTempRema(0)
            when (nextTempRema(0) >= rTempDenom) {
              nextTempRema(1) := nextTempRema(0) - rTempDenom
              rTempQuot(rCnt.asUInt.resized) := True
            }
            rTempRema := nextTempRema(1)
      //    }
      //  }
      //}
    }
  }

  val multiChunkWidthArea = (
    chunkWidth > 1
  ) generate (
    LongDivMultiCycleMultiChunkArea(
      io=io,
      cfg=cfg,
    )
  )
}

//case class LongDivMultiCycle(
//  //cfg: LongDivParams
//  mainWidth: Int,
//  denomWidth: Int,
//  chunkWidth: Int,
//  signedReset: BigInt=0x0,
//  formal: Boolean=false,
//) extends Component {
//  val cfg = LongDivConfig(
//    mainWidth=mainWidth,
//    denomWidth=denomWidth,
//    chunkWidth=chunkWidth,
//    tagWidth=1,
//    pipelined=false,
//    usePipeSkidBuf=false,
//    formal=formal,
//  )
//  val io = LongDivIo(cfg=cfg)
//  if (cfg.formal) {
//    when (!clockDomain.isResetActive) {
//      cover(io.inp.valid)
//    }
//    io.outp.ready := False
//    io.outp.quot := 0x0
//    io.outp.rema := 0x0 
//  }
//
//  object LocState extends SpinalEnum(
//    defaultEncoding=binarySequential
//    //defaultEncoding=binaryOneHot
//  ) {
//    val
//      IDLE,
//      RUNNING
//      = newElement();
//  }
//  def inp = io.inp
//  def outp = io.outp
//  val zeroD = inp.denom === 0
//  //class Loc
//  val loc = new Area {
//    val rState = Reg(LocState()) init(LocState.IDLE)
//    val m = LongUdivIter(cfg=cfg)
//
//    //val itdOutReg = Reg(LongUdivIterData(cfg=cfg))
//    //val pastValid = (cfg.formal) generate Reg(Bool()) init(False)
//
//    //val tempNumer = cfg.buildTempShape()
//    //val tempDenom = cfg.buildTempShape()
//    val rTempNumer = Reg(cfg.buildTempShape()) init(0x0)
//    //val tempNumer = tempNumerReg.wrapNext()
//    val tempNumer = cfg.buildTempShape()
//    val rTempDenom = Reg(cfg.buildTempShape()) init(0x0)
//    //val tempDenom = tempDenomReg.wrapNext()
//    val tempDenom = cfg.buildTempShape()
//    val rTempQuot = Reg(cfg.buildTempShape()) init(0x0)
//    val tempQuot = cfg.buildTempShape()
//    //val tempQuot = tempQuotReg.wrapNext()
//    val rTempRema = Reg(cfg.buildTempShape()) init(0x0)
//    val tempRema = cfg.buildTempShape()
//    //val tempRema = tempRemaReg.wrapNext()
//    val rDenomMultLut = Reg(
//      Vec.fill(cfg.dmlSize())(
//        UInt(cfg.dmlElemWidth() bits)
//      )
//    )
//    for (idx <- 0 until cfg.dmlSize()) {
//      rDenomMultLut(idx).init(rDenomMultLut(idx).getZero)
//    }
//    val rOracleQuot = (cfg.formal) generate (
//      Reg(
//        cfg.buildTempShape()
//      ) init(0x0)
//    )
//    val rOracleRema = (cfg.formal) generate (
//      Reg(
//        cfg.buildTempShape()
//      ) init(0x0)
//    )
//
//    val numerWasLez = Reg(Bool()) init(False)
//    val denomWasLez = Reg(Bool()) init(False)
//
//    val quotWillBeLez = Bool()
//    val remaWillBeLez = Bool()
//
//    val ioSzdTempQ = UInt(outp.quot.getWidth bits)
//    val ioSzdTempR = UInt(outp.rema.getWidth bits)
//    val lezIoSzdTempQ = UInt(outp.quot.getWidth bits)
//    val lezIoSzdTempR = UInt(outp.rema.getWidth bits)
//
//    //val tempIoQuot = UInt(outp.quot.getWidth bits)
//    //val tempIoRema = UInt(outp.rema.getWidth bits)
//    val rTempIoQuot = Reg(UInt(outp.quot.getWidth bits)) init(0x0)
//    //val tempIoQuot = tempIoQuotReg.wrapNext()
//    val tempIoQuot = UInt(outp.quot.getWidth bits)
//    val rTempIoRema = Reg(UInt(outp.rema.getWidth bits)) init(0x0)
//    //val tempIoRema = tempIoRemaReg.wrapNext()
//    val tempIoRema = UInt(outp.rema.getWidth bits)
//    val rTempIoReady = Reg(Bool()) init(False)
//
//    //val chunkStartBegin = cfg.buildTempShape()
//    val rChunkStartBegin = Reg(cfg.buildTempShape()) init(0x0)
//    val chunkStartBegin = cfg.buildTempShape()
//    //chunkStartBeginReg.init(chunkStartBeginReg.getZero)
//    //val chunkStartBegin = chunkStartBeginReg.wrapNext()
//    //val tempDbg = Bool()
//  }
//  //val loc = new Loc()
//  def itIo = loc.m.io
//  def chunkStart = itIo.chunkStart
//  def itdIn = itIo.itdIn
//  def itdOut = itIo.itdOut
//  val skipCond = itdIn.tempDenom === 0
//  //if (cfg.formal) {
//  //  
//  //}
//  //loc.tempNumer := Mux(
//  //  inp.signed & inp.numer.msb,
//  //  (~inp.numer) + 1, inp.numer)
//  //)
//  //if (cfg.formal) {
//  //  assume(!inp.signed)
//  //}
//  loc.tempNumer := (
//    //Mux(
//    //  inp.signed && inp.numer.msb, (~inp.numer) + 1, inp.numer
//    //)
//    inp.numer
//  )
//  loc.tempDenom := (
//    //Mux(
//    //  inp.signed && inp.denom.msb, (~inp.denom) + 1, inp.denom
//    //).resized
//    inp.denom.resized
//  )
//  //loc.tempNumerReg := loc.tempNumer
//  //loc.tempDenomReg := loc.tempDenom
//
//  loc.quotWillBeLez := loc.numerWasLez =/= loc.denomWasLez
//  // Implement C's rules for modulo's sign
//  loc.remaWillBeLez := loc.numerWasLez
//
//  //loc.ioSzdTempQ := itdOut.tempQuot(outp.quot.bitsRange)
//  //loc.ioSzdTempR := itdOut.tempRema(outp.rema.bitsRange)
//  loc.ioSzdTempQ := loc.rTempQuot(outp.quot.bitsRange)
//  loc.ioSzdTempR := loc.rTempRema(outp.rema.bitsRange)
//
//  loc.lezIoSzdTempQ := (~loc.ioSzdTempQ) + 1
//  loc.lezIoSzdTempR := (~loc.ioSzdTempR) + 1
//
//  loc.tempIoQuot := Mux(
//    loc.quotWillBeLez, loc.lezIoSzdTempQ, loc.ioSzdTempQ
//  )
//  loc.tempIoRema := Mux(
//    loc.remaWillBeLez, loc.lezIoSzdTempR, loc.ioSzdTempR
//  )
//  //loc.tempIoQuot := (loc.ioSzdTempQ)
//  //loc.tempIoRema := (loc.ioSzdTempR)
//
//  //loc.chunkStartBegin := (chunkStart
//  // === (cfg.numChunks() - 1))
//  loc.chunkStartBegin := (cfg.numChunks() - 1)
//  println(
//    (cfg.numChunks() - 1)
//  )
//  //loc.chunkStartBeginReg := loc.chunkStartBegin
//
//  chunkStart := loc.rChunkStartBegin.asSInt.resized //S(loc.rChunkStartBegin).resized
//  if (cfg.formal) {
//    cover(chunkStart === 0)
//  }
//  itdIn.tempNumer := loc.rTempNumer
//  itdIn.tempDenom := loc.rTempDenom.resized
//  itdIn.tempQuot := loc.rTempQuot
//  itdIn.tempRema := loc.rTempRema
//  for (idx <- 0 until itdIn.denomMultLut.size) {
//    itdIn.denomMultLut(idx) := loc.rDenomMultLut(idx)
//  }
//
//  //outp.quot := loc.rTempIoQuot
//  //outp.rema := loc.rTempIoRema
//  ////outp.ready := True
//  //outp.ready := loc.rTempIoReady
//
//  if (cfg.formal) 
//  //GenerationFlags.formal
//  {
//    itdIn.formal.oracleQuot := loc.rOracleQuot
//    itdIn.formal.oracleRema := loc.rOracleRema
//  }
//
//  //if (cfg.formal) 
//  //GenerationFlags.formal
//  {
//    //when (ClockDomain.isResetActive) {
//    //  //loc.tempNumerReg := 0x0
//    //  //loc.tempDenomReg := 0x0
//    //  //loc.tempQuotReg := 0x0
//    //  //loc.tempRemaReg := 0x0
//    //  loc.pastValid := True
//    //}
//    //loc.pastValid := True
//    //loc.pastValid := True
//  }
//  if (cfg.formal) {
//    when (inp.valid) {
//      cover(True)
//    }
//    //cover(inp.valid)
//  }
//  //when (!clockDomain.isResetActive) {
//    //if (cfg.formal) {
//    //  cover(loc.rState === LocState.IDLE)
//    //  cover(loc.rState === LocState.RUNNING)
//    //}
//    //switch (loc.rState) {
//    //  is (LocState.IDLE) {
//    //    //--------
//    //    // Need to check for `inp.signed` so that unsigned
//    //    // divides still work properly.
//    //    loc.numerWasLez := inp.signed & inp.numer.msb
//    //    loc.denomWasLez := inp.signed & inp.denom.msb
//
//    //    //chunkStart := cfg.numChunks() - 1
//    //    //chunkStart := S(loc.chunkStartBeginReg).resized
//    //    loc.rChunkStartBegin := loc.chunkStartBegin
//
//    //    loc.rTempNumer := loc.tempNumer
//    //    loc.rTempDenom := loc.tempDenom
//    //    loc.rTempQuot := 0x0
//    //    loc.rTempRema := 0x0
//
//    //    //itdIn.tempNumer := loc.tempNumerReg
//    //    //itdIn.tempDenom := loc.tempDenomReg
//    //    //itdIn.tempQuot := 0x0
//    //    //itdIn.tempRema := 0x0
//    //    for (idx <- 0 to cfg.dmlSize() - 1) {
//    //      loc.rDenomMultLut(idx) := (loc.tempDenom * idx).resized
//    //    }
//    //    //--------
//    //    when (inp.valid) {
//    //      //outp.quot := 0x0
//    //      //outp.rema := 0x0
//    //      //outp.ready := False
//    //      loc.rTempIoQuot := 0x0
//    //      loc.rTempIoRema := 0x0
//    //      loc.rTempIoReady := False
//
//    //      loc.rState := LocState.RUNNING
//    //    }
//    //    //--------
//    //  }
//    //  is (LocState.RUNNING) {
//    //    //--------
//    //    when (chunkStart > 0) {
//    //      // Since `itdIn` and `itdOut` are `Splitrec`s, we
//    //      // can do a simple `.eq()` regardless of whether or
//    //      // not `FORMAL` is true.
//    //      //m.d.sync += itdIn.eq(itdOut)
//    //      //itdIn := loc.itdOutReg
//    //      loc.rTempNumer := itdOut.tempNumer
//    //      loc.rTempDenom := itdOut.tempDenom.resized
//    //      loc.rTempQuot := itdOut.tempQuot
//    //      loc.rTempRema := itdOut.tempRema
//    //      loc.rDenomMultLut := itdOut.denomMultLut
//    //    } otherwise { // when(chunkStart <= 0)
//    //      //outp.quot := loc.tempIoQuotReg
//    //      //outp.rema := loc.tempIoRemaReg
//    //      ////outp.ready := True
//    //      //outp.ready := loc.tempIoReadyReg
//    //      loc.rTempIoQuot := loc.tempIoQuot
//    //      loc.rTempIoRema := loc.tempIoRema
//    //      loc.rTempIoReady := True
//
//    //      loc.rState := LocState.IDLE
//    //    }
//    //    //chunkStart := chunkStart - 1
//    //    loc.rChunkStartBegin := loc.rChunkStartBegin - 1
//    //    //--------
//    //  }
//    //} // switch (loc.state)
//    //if (cfg.formal) 
//    ////GenerationFlags.formal 
//    //{
//    //  //assume(~skipCond)
//    //  when (pastValidAfterReset()) {
//    //    //--------
//    //    //assume(stable(inp.signed))
//    //    //assume(inp.numer === 8)
//    //    //assume(inp.denom === 4)
//    //    //--------
//    //    //assume(stable(loc.tempNumer))
//    //    //assume(stable(loc.tempDenom))
//    //    //--------
//    //  }
//    //  switch (loc.rState) {
//    //    is (LocState.IDLE) {
//    //      loc.rOracleQuot := loc.tempNumer / loc.tempDenom
//    //      loc.rOracleRema := loc.tempNumer % loc.tempDenom
//    //      when (pastValidAfterReset() && (!stable(loc.rState))) {
//    //        //--------
//    //        //assert(~skipCond)
//    //        when (!skipCond) {
//    //          //assert((outp.quot === past(loc.tempIoQuot)))
//    //          //assert((outp.rema === past(loc.tempIoRema)))
//    //          //assert((outp.quot === loc.rOracleQuot))
//    //          //assert((outp.rema === loc.rOracleRema))
//    //          assert(outp.ready)
//    //          cover(outp.quot =/= 0x0)
//    //        }
//    //        //--------
//    //      }
//    //      //elsewhen (pastValidAfterReset() & stable(loc.state)):
//    //    }
//    //    //is (LocState.RUNNING) {
//    //    //  when (
//    //    //    pastValidAfterReset() & (past(loc.state) === LocState.IDLE)
//    //    //  ) {
//    //    //    //--------
//    //    //    assert(loc.numerWasLez
//    //    //      === (past(inp.signed)
//    //    //        & past(inp.numer).msb))
//    //    //    assert(loc.denomWasLez
//    //    //      === (past(inp.signed)
//    //    //        & past(inp.denom).msb))
//    //    //    //--------
//    //    //    //assert(chunkStart
//    //    //    // === (cfg.numChunks() - 1)),
//    //    //    //assert(chunkStart[:len(chunkStart) - 1]
//    //    //    // === (cfg.numChunks() - 1)),
//    //    //    //assert(chunkStart
//    //    //    //  [:len(loc.chunkStartBegin)]
//    //    //    //  === loc.chunkStartBegin)
//    //    //    assert(U(chunkStart)
//    //    //      === loc.chunkStartBegin(chunkStart.bitsRange))
//    //    //    //assert(loc.chunkStartBegin)
//    //    //    //--------
//    //    //    //assert(itdIn.tempNumer
//    //    //    //  === past(loc.tempNumerReg))
//    //    //    assert(itdIn.tempQuot === 0x0)
//    //    //    assert(itdIn.tempRema === 0x0)
//    //    //    //--------
//    //    //    assert(itdIn.tempNumer
//    //    //      === past(loc.tempNumer))
//    //    //    assert(itdIn.tempDenom
//    //    //      === past(loc.tempDenom))
//
//    //    //    assert(itdIn.formal.oracleQuot
//    //    //      === (past(loc.tempNumer)
//    //    //        / past(loc.tempDenom)))
//    //    //    assert(itdIn.formal.oracleRema
//    //    //      === (past(loc.tempNumer)
//    //    //        % past(loc.tempDenom)))
//    //    //    //--------
//    //    //  } elsewhen (pastValidAfterReset() & stable(loc.state)) {
//    //    //    //--------
//    //    //    assert(stable(loc.numerWasLez))
//    //    //    assert(stable(loc.denomWasLez))
//    //    //    //--------
//    //    //    assert(chunkStart
//    //    //      === (past(chunkStart) - 1))
//    //    //    //--------
//    //    //    assert(itdOut.tempNumer
//    //    //      === past(itdIn.tempNumer))
//    //    //    //--------
//    //    //    assert(itdOut.denomMultLut
//    //    //      === past(itdIn.denomMultLut))
//    //    //    //--------
//    //    //    assert(itdOut.tempNumer
//    //    //      === past(itdIn.tempNumer))
//    //    //    assert(itdOut.tempDenom
//    //    //      === past(itdIn.tempDenom))
//
//    //    //    assert(itdOut.formal.oracleQuot
//    //    //      === past(itdIn.formal.oracleQuot))
//    //    //    assert(itdOut.formal.oracleRema
//    //    //      === past(itdIn.formal.oracleRema))
//    //    //    //--------
//    //    //    assert(itdOut.denomMultLut
//    //    //      === past(itdIn.denomMultLut))
//    //    //    //--------
//    //    //  }
//    //    //  when (pastValidAfterReset()) {
//    //    //    //assert(itdIn.denomMultLut[i]
//    //    //    //  === (
//    //    //    //    itdIn.tempDenom
//    //    //    //    * i
//    //    //    //  ))
//    //    //    //  for i in range(cfg.dmlSize())
//    //    //    for (idx <- 0 to cfg.dmlSize() - 1) {
//    //    //      assert(
//    //    //        itdIn.denomMultLut(idx)
//    //    //        === (
//    //    //          itdIn.tempDenom * idx
//    //    //        )
//    //    //      )
//    //    //    }
//    //    //  }
//    //    //}
//    //  }
//    //}
//  //}
//}


//case class LongDivPipelined(
//  mainWidth: Int,
//  denomWidth: Int,
//  chunkWidth: Int,
//  signedReset: BigInt=0x0,
//  usePipeSkidBuf: Boolean=false,
//) extends Component {
//  val cfg = LongDivParams(
//    mainWidth=mainWidth,
//    denomWidth=denomWidth,
//    chunkWidth=chunkWidth,
//    tagWidth=(chunkWidth * ((mainWidth / chunkWidth).toInt + 1)).toInt,
//    pipelined=true,
//    usePipeSkidBuf=usePipeSkidBuf,
//  )
//  val io = LongDivIo(cfg=cfg)
//  val inp = io.inp
//  val outp = io.outp
//
//  //numPstages = cfg.numChunks() + 1
//  val numPstages = cfg.numChunks()
//  val numPsElems = numPstages + 1
//
//  val loc = new Area {
//    //val m = Array.fill(LongUdivIterSync(
//    //  cfg=cfg,
//    //  chunkStartVal=
//    //)){ 
//    //}
//    val m = new ArrayBuffer[LongUdivIterSync]()
//    for (idx <- 0 to numPstages - 1) {
//      m += new LongUdivIterSync(
//        cfg=cfg,
//        chunkStartVal=(numPstages - 1) - idx
//      )
//      val lastM = m.last
//      lastM.setName(f"LongUdivIterSync_$idx")
//      val lastSbIo = lastM.io.sbIo
//      val lastMisc = lastSbIo.misc
//      lastMisc := lastMisc.getZero
//      if (!usePipeSkidBuf) {
//        lastSbIo.prev.valid := True
//        lastSbIo.next.ready := True
//      } else if (idx == 0) {
//        lastSbIo.prev.valid := True
//        //lastSbIo.prev.ready := True
//        //lastSbIo.prev.stage()
//        //lastSbIo.prev.s2mPipe()
//      } else if (idx == numPstages - 1) {
//        lastSbIo.next.ready := True
//        //lastSbIo.next.valid := True
//        //lastSbIo.next.stage()
//        //lastSbIo.next.m2sPipe()
//      }
//    }
//
//    case class TempSync() extends Bundle {
//      val tempNumer = UInt(cfg.mainWidth bits)
//      val tempDenom = UInt(cfg.denomWidth bits)
//
//      val numerWasLez = UInt(numPsElems bits)
//      val denomWasLez = UInt(numPsElems bits)
//
//      val quotWillBeLez = UInt(numPsElems bits)
//      val remaWillBeLez = UInt(numPsElems bits)
//      //--------
//    }
//    case class TempComb() extends Bundle {
//      val busSzdTempQ = UInt(outp.quot.getWidth bits)
//      val busSzdTempR = UInt(outp.rema.getWidth bits)
//      val lezBusSzdTempQ = UInt(outp.quot.getWidth bits)
//      val lezBusSzdTempR = UInt(outp.rema.getWidth bits)
//
//      val tempBusQuot = UInt(outp.quot.getWidth bits)
//      val tempBusRema = UInt(outp.rema.getWidth bits)
//    }
//    val tSync = Reg(TempSync()) init(TempSync().getZero)
//    val tSyncPrev = Reg(TempSync()) init(TempSync().getZero)
//    val tComb = TempComb()
//    val tCombPrev = Reg(TempComb()) init(TempComb().getZero)
//
//    val rItdIn0 = Reg(LongUdivIterData(cfg=cfg))
//    rItdIn0.init(rItdIn0.getZero)
//  }
//
//  // Wrapper variables
//  val itsIo = ArrayBuffer[LongUdivIterSyncIo]()
//  //val ifwdPayload = ArrayBuffer[LongUdivIterData]()
//  val ifwdValid = ArrayBuffer[Bool]()
//  val ibakReady = ArrayBuffer[Bool]()
//  //val ofwdPayload = ArrayBuffer[LongUdivIterData]()
//  val ofwdValid = ArrayBuffer[Bool]()
//  val obakReady = ArrayBuffer[Bool]()
//
//  val itdIn = ArrayBuffer[LongUdivIterData]()
//  val itdOut = ArrayBuffer[LongUdivIterData]()
//
//  val ifwdMove = ArrayBuffer[Bool]()
//  val ofwdMove = ArrayBuffer[Bool]()
//  val ifwdMvp = ArrayBuffer[Bool]()
//  val ofwdMvp = ArrayBuffer[Bool]()
//
//  for (idx <- 0 to loc.m.size - 1) {
//    itsIo += loc.m(idx).io
//    //itsIo.last.sbIo
//    //itsFormal += itsIo(idx).formal
//    //ifwd += itsIo(idx).sbIo.inp.fwd
//    //ibak += itsIo(idx).sbIo.inp.bak
//    //ofwd += itsIo(idx).sbIo.outp.fwd
//    //obak += itsIo(idx).sbIo.outp.bak
//    ifwdValid += itsIo(idx).sbIo.prev.valid
//    obakReady += itsIo(idx).sbIo.prev.ready
//    ofwdValid += itsIo(idx).sbIo.next.valid
//    ibakReady += itsIo(idx).sbIo.next.ready
//    itdIn += itsIo(idx).sbIo.prev.payload
//    itdOut += itsIo(idx).sbIo.next.payload
//
//    ifwdMove += (
//      //(1 if Bool(!usePipeSkidBuf) else 0)
//      Bool(!usePipeSkidBuf)
//      | (ifwdValid(idx) & obakReady(idx))
//    )
//    ofwdMove += (
//      Bool(!usePipeSkidBuf)
//      | (ofwdValid(idx) & ibakReady(idx))
//    )
//    ifwdMvp += (
//      //(1 if Bool(!usePipeSkidBuf) else 0)
//      Bool(!usePipeSkidBuf)
//      | (past(ifwdValid(idx)) & past(obakReady(idx)))
//    )
//    ofwdMvp += (
//      Bool(!usePipeSkidBuf)
//      | (past(ofwdValid(idx)) & past(ibakReady(idx)))
//    )
//  }
//  //itdIn(0) := loc.itdIn0Reg
//  //itsIo(0).sbIo.prev.payload := loc.itdIn0Reg
//  itdIn(0) := loc.rItdIn0
//  //--------
//  // Connect the pipeline stages together
//  println(
//    s"div pipelined: ${usePipeSkidBuf}"
//  )
//  if (!usePipeSkidBuf) {
//    for (idx <- 0 to loc.m.size - 2) {
//      itdIn(idx + 1) := itdOut(idx)
//    } 
//  } else { // if (usePipeSkidBuf)
//    PipeSkidBufIo.connectParallel(
//      sbIoList=(
//        for (idx <- 0 to itsIo.size - 1)
//          yield itsIo(idx).sbIo
//      ).toList,
//      tieFirstIfwdValid=true,
//      tieLastIbakReady=true,
//    )
//  }
//  //--------
//  when (ifwdMove(0)) {
//    //--------
//    loc.tSync.tempNumer := Mux(
//      inp.signed & inp.numer.msb,
//      (~inp.numer) + 1, inp.numer
//    )
//    loc.tSync.tempDenom := Mux(
//      inp.signed & inp.denom.msb,
//      (~inp.denom) + 1, inp.denom
//    )
//    //--------
//    loc.tSync.numerWasLez(0) := inp.signed & inp.numer.msb
//    loc.tSync.denomWasLez(0) := inp.signed & inp.denom.msb
//
//    loc.tSync.quotWillBeLez(0) := (
//      loc.tSync.numerWasLez(0) =/= loc.tSync.denomWasLez(0)
//    )
//    // Implement C's rules for modulo's sign
//    loc.tSync.remaWillBeLez(0) := loc.tSync.numerWasLez(0)
//    //--------
//  }
//  loc.tSyncPrev := loc.tSync
//  loc.tCombPrev := loc.tComb
//  //--------
//  //--------
//  loc.tComb.busSzdTempQ := Mux(
//    ofwdMove.last,
//    itdOut.last.tempQuot(outp.quot.bitsRange),
//    loc.tCombPrev.busSzdTempQ,
//  )
//  loc.tComb.busSzdTempR := Mux(
//    ofwdMove.last,
//    itdOut.last.tempRema(outp.rema.bitsRange),
//    loc.tCombPrev.busSzdTempR,
//  )
//
//  loc.tComb.lezBusSzdTempQ := Mux(
//    ofwdMove.last,
//    (~loc.tComb.busSzdTempQ) + 1,
//    loc.tCombPrev.lezBusSzdTempQ,
//  )
//  loc.tComb.lezBusSzdTempR := Mux(
//    ofwdMove.last,
//    (~loc.tComb.busSzdTempR) + 1,
//    loc.tCombPrev.lezBusSzdTempR,
//  )
//
//  loc.tComb.tempBusQuot := Mux(
//    ofwdMove.last,
//    Mux(
//      !loc.tSync.quotWillBeLez(loc.tSync.quotWillBeLez.getWidth - 1),
//      loc.tComb.lezBusSzdTempQ, loc.tComb.busSzdTempQ
//    ),
//    loc.tCombPrev.tempBusQuot,
//  )
//  loc.tComb.tempBusRema := Mux(
//    ofwdMove.last,
//    Mux(
//      !loc.tSync.remaWillBeLez(loc.tSync.remaWillBeLez.getWidth - 1),
//      loc.tComb.lezBusSzdTempR, loc.tComb.busSzdTempR
//    ),
//    loc.tCombPrev.tempBusRema,
//  )
//  //--------
//  for (idx <- 0 to loc.m.size - 1) {
//    when (ofwdMove(idx)) {
//      loc.tSync.numerWasLez(idx + 1) := loc.tSync.numerWasLez(idx)
//      loc.tSync.denomWasLez(idx + 1) := loc.tSync.denomWasLez(idx)
//      loc.tSync.quotWillBeLez(idx + 1) := loc.tSync.quotWillBeLez(idx)
//      loc.tSync.remaWillBeLez(idx + 1) := loc.tSync.remaWillBeLez(idx)
//    }
//  }
//  when (ifwdMove(0)) {
//    loc.rItdIn0.tempNumer := loc.tSync.tempNumer
//    loc.rItdIn0.tempDenom := loc.tSync.tempDenom
//
//    loc.rItdIn0.tempQuot := 0x0
//    loc.rItdIn0.tempRema := 0x0
//
//    if (loc.rItdIn0.tag != null) {
//      loc.rItdIn0.tag := inp.tag
//    }
//  }
//  for (idx <- 0 to cfg.dmlSize() - 1) {
//    loc.rItdIn0.denomMultLut(idx) := (loc.tSync.tempDenom * idx).resized
//  }
//  outp.quot := loc.tComb.tempBusQuot
//  outp.rema := loc.tComb.tempBusRema
//
//  outp.tag := itdOut.last.tag
//  //--------
//  if (cfg.formal) 
//  //GenerationFlags.formal 
//  {
//    val skipCond = itdOut.last.tempDenom === 0
//    //--------
//    when (ifwdMove(0)) {
//      //--------
//      loc.rItdIn0.formal.oracleQuot := (
//        loc.tSync.tempNumer / loc.tSync.tempDenom
//      )
//      loc.rItdIn0.formal.oracleRema := (
//        loc.tSync.tempNumer % loc.tSync.tempDenom
//      )
//      //--------
//    }
//
//    when(pastValidAfterReset) {
//      //--------
//      when (ifwdMvp(0)) {
//        //--------
//        assert(
//          loc.tSync.tempNumer
//          === Mux(past(inp.signed) & past(inp.numer).msb,
//            (~past(inp.numer)) + 1, past(inp.numer))
//          )
//        assert(
//          loc.tSync.tempDenom
//          === Mux(past(inp.signed) & past(inp.denom).msb,
//            (~past(inp.denom)) + 1, past(inp.denom))
//          )
//        //--------
//        assert(
//          loc.tSync.numerWasLez(0)
//          === (past(inp.signed) & past(inp.numer).msb)
//        )
//        assert(
//          loc.tSync.denomWasLez(0)
//          === (past(inp.signed) & past(inp.denom).msb)
//        )
//
//        assert(
//          loc.tSync.quotWillBeLez(0)
//          === (past(loc.tSync.numerWasLez)(0)
//            =/= past(loc.tSync.denomWasLez)(0))
//          )
//        // implement C's rules for modulo's sign
//        assert(
//          loc.tSync.remaWillBeLez(0)
//          === past(loc.tSync.numerWasLez)(0)
//        )
//        //--------
//      }
//      //--------
//      for (idx <- 0 until loc.m.size) {
//        when (ofwdMove(idx)) {
//          //--------
//          assert(
//            loc.tSync.numerWasLez(idx + 1)
//            === past(loc.tSync.numerWasLez)(idx)
//          )
//          assert(
//            loc.tSync.denomWasLez(idx + 1)
//            === past(loc.tSync.denomWasLez)(idx)
//          )
//          assert(
//            loc.tSync.quotWillBeLez(idx + 1)
//            === past(loc.tSync.quotWillBeLez)(idx)
//          )
//          assert(
//            loc.tSync.remaWillBeLez(idx + 1)
//            === past(loc.tSync.remaWillBeLez)(idx)
//          )
//          //--------
//        }
//      }
//      //--------
//      //when (ifwdMove(0)):
//      when (ifwdMvp(0)) {
//        //m.d.sync += (
//          //--------
//          assert(
//            itdIn(0).tempNumer === past(loc.tSync.tempNumer)
//          )
//          assert(
//            itdIn(0).tempQuot === 0x0
//          )
//          assert(
//            itdIn(0).tempRema === 0x0
//          )
//          //--------
//          assert(
//            itdIn(0).tempNumer
//              === past(loc.tSync.tempNumer)
//          )
//          assert(
//            itdIn(0).tempDenom
//              === past(loc.tSync.tempDenom)
//          )
//
//          assert(
//            itdIn(0).formal.oracleQuot
//            === (
//              past(loc.tSync.tempNumer) / past(loc.tSync.tempDenom)
//            )
//          )
//          assert(
//            itdIn(0).formal.oracleRema
//            === (
//              past(loc.tSync.tempNumer)
//              % past(loc.tSync.tempDenom)
//            )
//          )
//        //)
//      }
//      //when (ofwdMove.last):
//      when (ofwdMvp.last) {
//        //m.d.comb += (
//          //--------
//          assert(
//            skipCond
//            | (
//              itdOut.last.tempQuot
//                (outp.quot.bitsRange)
//              === itdOut.last.formal.oracleQuot
//                (outp.quot.bitsRange)
//            )
//          )
//          //assert((skipCond)
//          // | (itdOut.last.tempRema
//          //   === itdOut.last.formal.oracleRema)),
//          assert(
//            skipCond
//            | (
//              itdOut.last.tempRema(outp.rema.bitsRange)
//              === itdOut.last.formal.oracleRema(outp.rema.bitsRange)
//            )
//          )
//          //--------
//        //)
//      //--------
//      }
//    }
//  }
//  //--------
////--------
//
//}
