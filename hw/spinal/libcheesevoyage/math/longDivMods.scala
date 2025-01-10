package libcheesevoyage.math
import libcheesevoyage.general._

import scala.math._
import spinal.core._
import spinal.lib._
import spinal.core.formal._
import scala.collection.mutable.ArrayBuffer

case class LongDivInp(cfg: LongDivConfig) extends Bundle {
  val valid = (!cfg.pipelined) generate Bool()
  val numer = UInt(cfg.mainWidth bits)
  val denom = UInt(cfg.denomWidth bits)
  val signed = Bool()
  val tag = (cfg.pipelined) generate UInt(cfg.tagWidth bits)
}
case class LongDivOutp(cfg: LongDivConfig) extends Bundle {
  val ready = (!cfg.pipelined) generate Bool()
  val quot = UInt(cfg.mainWidth bits)
  val rema = UInt(cfg.mainWidth bits)
  val tag = (cfg.pipelined) generate UInt(cfg.tagWidth bits)
}

case class LongDivIo(cfg: LongDivConfig) extends Bundle {
  val inp = in(LongDivInp(cfg=cfg))
  val outp = out(LongDivOutp(cfg=cfg))
}

case class LongDivMultiCycle(
  //params: LongDivParams
  mainWidth: Int,
  denomWidth: Int,
  chunkWidth: Int,
  signedReset: BigInt=0x0,
) extends Component {
  val cfg = LongDivConfig(
    mainWidth=mainWidth,
    denomWidth=denomWidth,
    chunkWidth=chunkWidth,
    tagWidth=1,
    pipelined=false,
    usePipeSkidBuf=false,
  )
  val io = LongDivIo(cfg=cfg)

  object LocState extends SpinalEnum(
    //defaultEncoding=binarySequential
    defaultEncoding=binaryOneHot
  ) {
    val
      IDLE,
      RUNNING
      = newElement();
  }
  val inp = io.inp
  val outp = io.outp
  val zeroD = inp.denom === 0
  //class Loc
  val loc = new Area {
    val state = Reg(LocState())
    val m = LongUdivIter(params=cfg)

    //val itdOutReg = Reg(LongUdivIterData(params=params))
    //val pastValid = (params.formal()) generate Reg(Bool()) init(False)

    //val tempNumer = params.buildTempShape()
    //val tempDenom = params.buildTempShape()
    val rTempNumer = Reg(cfg.buildTempShape()) init(0x0)
    //val tempNumer = tempNumerReg.wrapNext()
    val tempNumer = cfg.buildTempShape()
    val rTempDenom = Reg(cfg.buildTempShape()) init(0x0)
    //val tempDenom = tempDenomReg.wrapNext()
    val tempDenom = cfg.buildTempShape()
    val rTempQuot = Reg(cfg.buildTempShape()) init(0x0)
    val tempQuot = cfg.buildTempShape()
    //val tempQuot = tempQuotReg.wrapNext()
    val rTempRema = Reg(cfg.buildTempShape()) init(0x0)
    val tempRema = cfg.buildTempShape()
    //val tempRema = tempRemaReg.wrapNext()
    val rDenomMultLut = Reg(
      Vec(
        UInt(cfg.dmlElemWidth() bits),
        cfg.dmlSize()
      )
    )
    for (idx <- 0 until cfg.dmlSize()) {
      rDenomMultLut(idx).init(rDenomMultLut(idx).getZero)
    }
    val rOracleQuot = (cfg.formal()) generate (
      Reg(
        cfg.buildTempShape()
      ) init(0x0)
    )
    val rOracleRema = (cfg.formal()) generate (
      Reg(
        cfg.buildTempShape()
      ) init(0x0)
    )

    val numerWasLez = Reg(Bool()) init(False)
    val denomWasLez = Reg(Bool()) init(False)

    val quotWillBeLez = Bool()
    val remaWillBeLez = Bool()

    val ioSzdTempQ = UInt(outp.quot.getWidth bits)
    val ioSzdTempR = UInt(outp.rema.getWidth bits)
    val lezIoSzdTempQ = UInt(outp.quot.getWidth bits)
    val lezIoSzdTempR = UInt(outp.rema.getWidth bits)

    //val tempIoQuot = UInt(outp.quot.getWidth bits)
    //val tempIoRema = UInt(outp.rema.getWidth bits)
    val rTempIoQuot = Reg(UInt(outp.quot.getWidth bits)) init(0x0)
    //val tempIoQuot = tempIoQuotReg.wrapNext()
    val tempIoQuot = UInt(outp.quot.getWidth bits)
    val rTempIoRema = Reg(UInt(outp.rema.getWidth bits)) init(0x0)
    //val tempIoRema = tempIoRemaReg.wrapNext()
    val tempIoRema = UInt(outp.rema.getWidth bits)
    val rTempIoReady = Reg(Bool()) init(False)

    //val chunkStartBegin = params.buildTempShape()
    val rChunkStartBegin = Reg(cfg.buildTempShape()) init(0x0)
    val chunkStartBegin = cfg.buildTempShape()
    //chunkStartBeginReg.init(chunkStartBeginReg.getZero)
    //val chunkStartBegin = chunkStartBeginReg.wrapNext()
    //val tempDbg = Bool()
  }
  //val loc = new Loc()
  val itIo = loc.m.io
  val chunkStart = itIo.chunkStart
  val itdIn = itIo.itdIn
  val itdOut = itIo.itdOut
  val skipCond = itdIn.tempDenom === 0
  //if (params.formal()) {
  //  
  //}
  //loc.tempNumer := Mux(
  //  inp.signed & inp.numer.msb,
  //  (~inp.numer) + 1, inp.numer)
  //)
  loc.tempNumer := Mux(
    inp.signed & inp.numer.msb, (~inp.numer) + 1, inp.numer
  )
  loc.tempDenom := Mux(
    inp.signed & inp.denom.msb, (~inp.denom) + 1, inp.denom
  )
  //loc.tempNumerReg := loc.tempNumer
  //loc.tempDenomReg := loc.tempDenom

  loc.quotWillBeLez := loc.numerWasLez =/= loc.denomWasLez
  // Implement C's rules for modulo's sign
  loc.remaWillBeLez := loc.numerWasLez

  //loc.ioSzdTempQ := itdOut.tempQuot(outp.quot.bitsRange)
  //loc.ioSzdTempR := itdOut.tempRema(outp.rema.bitsRange)
  loc.ioSzdTempQ := loc.rTempQuot(outp.quot.bitsRange)
  loc.ioSzdTempR := loc.rTempRema(outp.rema.bitsRange)

  loc.lezIoSzdTempQ := (~loc.ioSzdTempQ) + 1
  loc.lezIoSzdTempR := (~loc.ioSzdTempR) + 1

  loc.tempIoQuot := Mux(
    loc.quotWillBeLez, loc.lezIoSzdTempQ, loc.ioSzdTempQ
  )
  loc.tempIoRema := Mux(
    loc.remaWillBeLez, loc.lezIoSzdTempR, loc.ioSzdTempR
  )
  //loc.tempIoQuot := (loc.ioSzdTempQ)
  //loc.tempIoRema := (loc.ioSzdTempR)

  //loc.chunkStartBegin := (chunkStart
  // === (params.numChunks() - 1))
  loc.chunkStartBegin := (cfg.numChunks() - 1)
  //loc.chunkStartBeginReg := loc.chunkStartBegin

  chunkStart := S(loc.rChunkStartBegin).resized
  itdIn.tempNumer := loc.rTempNumer
  itdIn.tempDenom := loc.rTempDenom
  itdIn.tempQuot := loc.rTempQuot
  itdIn.tempRema := loc.rTempRema
  itdIn.denomMultLut := loc.rDenomMultLut

  outp.quot := loc.rTempIoQuot
  outp.rema := loc.rTempIoRema
  //outp.ready := True
  outp.ready := loc.rTempIoReady

  //if (params.formal()) 
  GenerationFlags.formal {
    itdIn.formal.oracleQuot := loc.rOracleQuot
    itdIn.formal.oracleRema := loc.rOracleRema
  }

  //if (params.formal()) 
  GenerationFlags.formal {
    //when (clockDomain.isResetActive) {
    //  //loc.tempNumerReg := 0x0
    //  //loc.tempDenomReg := 0x0
    //  //loc.tempQuotReg := 0x0
    //  //loc.tempRemaReg := 0x0
    //  loc.pastValid := True
    //}
    //loc.pastValid := True
    //loc.pastValid := True
  }
  when (~clockDomain.isResetActive) {
    switch (loc.state) {
      is (LocState.IDLE) {
        //--------
        // Need to check for `inp.signed` so that unsigned
        // divides still work properly.
        loc.numerWasLez := inp.signed & inp.numer.msb
        loc.denomWasLez := inp.signed & inp.denom.msb

        //chunkStart := params.numChunks() - 1
        //chunkStart := S(loc.chunkStartBeginReg).resized
        loc.rChunkStartBegin := loc.chunkStartBegin

        loc.rTempNumer := loc.tempNumer
        loc.rTempDenom := loc.tempDenom
        loc.rTempQuot := 0x0
        loc.rTempRema := 0x0

        //itdIn.tempNumer := loc.tempNumerReg
        //itdIn.tempDenom := loc.tempDenomReg
        //itdIn.tempQuot := 0x0
        //itdIn.tempRema := 0x0
        for (idx <- 0 to cfg.dmlSize() - 1) {
          loc.rDenomMultLut(idx) := (loc.tempDenom * idx).resized
        }
        //--------
        when (inp.valid) {
          //outp.quot := 0x0
          //outp.rema := 0x0
          //outp.ready := False
          loc.rTempIoQuot := 0x0
          loc.rTempIoRema := 0x0
          loc.rTempIoReady := False

          loc.state := LocState.RUNNING
        }
        //--------
      }
      is (LocState.RUNNING) {
        //--------
        when (chunkStart > 0) {
          // Since `itdIn` and `itdOut` are `Splitrec`s, we
          // can do a simple `.eq()` regardless of whether or
          // not `FORMAL` is true.
          //m.d.sync += itdIn.eq(itdOut)
          //itdIn := loc.itdOutReg
          loc.rTempNumer := itdOut.tempNumer
          loc.rTempDenom := itdOut.tempDenom
          loc.rTempQuot := itdOut.tempQuot
          loc.rTempRema := itdOut.tempRema
          loc.rDenomMultLut := itdOut.denomMultLut
        } otherwise { // when(chunkStart <= 0)
          //outp.quot := loc.tempIoQuotReg
          //outp.rema := loc.tempIoRemaReg
          ////outp.ready := True
          //outp.ready := loc.tempIoReadyReg
          loc.rTempIoQuot := loc.tempIoQuot
          loc.rTempIoRema := loc.tempIoRema
          loc.rTempIoReady := True

          loc.state := LocState.IDLE
        }
        //chunkStart := chunkStart - 1
        loc.rChunkStartBegin := loc.rChunkStartBegin - 1
        //--------
      }
    } // switch (loc.state)
    //if (params.formal()) 
    GenerationFlags.formal {
      //assume(~skipCond)
      when (pastValidAfterReset()) {
        //--------
        assume(stable(inp.signed))
        //--------
        assume(stable(loc.tempNumer))
        assume(stable(loc.tempDenom))
        //--------
      }
      switch (loc.state) {
        is (LocState.IDLE) {
          loc.rOracleQuot := loc.tempNumer / loc.tempDenom
          loc.rOracleRema := loc.tempNumer % loc.tempDenom
          when (pastValidAfterReset() & (~stable(loc.state))) {
            //--------
            //assert(~skipCond)
            assert(skipCond | (outp.quot === past(loc.tempIoQuot)))
            assert(skipCond | (outp.rema === past(loc.tempIoRema)))
            //assert(skipCond | (outp.quot === loc.oracleQuotReg))
            //assert(skipCond | (outp.rema === loc.oracleRemaReg))
            assert(outp.ready)
            //--------
          }
          //elsewhen (pastValidAfterReset() & stable(loc.state)):
        }
        is (LocState.RUNNING) {
          when (
            pastValidAfterReset() & (past(loc.state) === LocState.IDLE)
          ) {
            //--------
            assert(loc.numerWasLez
              === (past(inp.signed)
                & past(inp.numer).msb))
            assert(loc.denomWasLez
              === (past(inp.signed)
                & past(inp.denom).msb))
            //--------
            //assert(chunkStart
            // === (params.numChunks() - 1)),
            //assert(chunkStart[:len(chunkStart) - 1]
            // === (params.numChunks() - 1)),
            //assert(chunkStart
            //  [:len(loc.chunkStartBegin)]
            //  === loc.chunkStartBegin)
            assert(U(chunkStart)
              === loc.chunkStartBegin(chunkStart.bitsRange))
            //assert(loc.chunkStartBegin)
            //--------
            //assert(itdIn.tempNumer
            //  === past(loc.tempNumerReg))
            assert(itdIn.tempQuot === 0x0)
            assert(itdIn.tempRema === 0x0)
            //--------
            assert(itdIn.tempNumer
              === past(loc.tempNumer))
            assert(itdIn.tempDenom
              === past(loc.tempDenom))

            assert(itdIn.formal.oracleQuot
              === (past(loc.tempNumer)
                / past(loc.tempDenom)))
            assert(itdIn.formal.oracleRema
              === (past(loc.tempNumer)
                % past(loc.tempDenom)))
            //--------
          } elsewhen (pastValidAfterReset() & stable(loc.state)) {
            //--------
            assert(stable(loc.numerWasLez))
            assert(stable(loc.denomWasLez))
            //--------
            assert(chunkStart
              === (past(chunkStart) - 1))
            //--------
            assert(itdOut.tempNumer
              === past(itdIn.tempNumer))
            //--------
            assert(itdOut.denomMultLut
              === past(itdIn.denomMultLut))
            //--------
            assert(itdOut.tempNumer
              === past(itdIn.tempNumer))
            assert(itdOut.tempDenom
              === past(itdIn.tempDenom))

            assert(itdOut.formal.oracleQuot
              === past(itdIn.formal.oracleQuot))
            assert(itdOut.formal.oracleRema
              === past(itdIn.formal.oracleRema))
            //--------
            assert(itdOut.denomMultLut
              === past(itdIn.denomMultLut))
            //--------
          }
          when (pastValidAfterReset()) {
            //assert(itdIn.denomMultLut[i]
            //  === (
            //    itdIn.tempDenom
            //    * i
            //  ))
            //  for i in range(params.dmlSize())
            for (idx <- 0 to cfg.dmlSize() - 1) {
              assert(
                itdIn.denomMultLut(idx)
                === (
                  itdIn.tempDenom * idx
                )
              )
            }
          }
        }
      }
    }
  }
}


//case class LongDivPipelined(
//  mainWidth: Int,
//  denomWidth: Int,
//  chunkWidth: Int,
//  signedReset: BigInt=0x0,
//  usePipeSkidBuf: Boolean=false,
//) extends Component {
//  val params = LongDivParams(
//    mainWidth=mainWidth,
//    denomWidth=denomWidth,
//    chunkWidth=chunkWidth,
//    tagWidth=(chunkWidth * ((mainWidth / chunkWidth).toInt + 1)).toInt,
//    pipelined=true,
//    usePipeSkidBuf=usePipeSkidBuf,
//  )
//  val io = LongDivIo(params=params)
//  val inp = io.inp
//  val outp = io.outp
//
//  //numPstages = params.numChunks() + 1
//  val numPstages = params.numChunks()
//  val numPsElems = numPstages + 1
//
//  val loc = new Area {
//    //val m = Array.fill(LongUdivIterSync(
//    //  params=params,
//    //  chunkStartVal=
//    //)){ 
//    //}
//    val m = new ArrayBuffer[LongUdivIterSync]()
//    for (idx <- 0 to numPstages - 1) {
//      m += new LongUdivIterSync(
//        params=params,
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
//      val tempNumer = UInt(params.mainWidth bits)
//      val tempDenom = UInt(params.denomWidth bits)
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
//    val rItdIn0 = Reg(LongUdivIterData(params=params))
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
//  for (idx <- 0 to params.dmlSize() - 1) {
//    loc.rItdIn0.denomMultLut(idx) := (loc.tSync.tempDenom * idx).resized
//  }
//  outp.quot := loc.tComb.tempBusQuot
//  outp.rema := loc.tComb.tempBusRema
//
//  outp.tag := itdOut.last.tag
//  //--------
//  //if (params.formal()) 
//  GenerationFlags.formal {
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
