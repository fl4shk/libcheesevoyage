package libcheesevoyage.math
import libcheesevoyage.general.PsbIoParentData
import libcheesevoyage.general.PipeSkidBufIo
import libcheesevoyage.general.PipeSkidBuf

import scala.math._
import spinal.core._
import spinal.lib._
import spinal.core.formal._
//import scala.collection.mutable.ArrayBuffer

case class LongDivConfig(
  mainWidth: Int,
  denomWidth: Int,
  chunkWidth: Int,
  tagWidth: Int=1,
  pipelined: Boolean=false,
  usePipeSkidBuf: Boolean=true,
  formal: Boolean=false,
) {
  //def formal: Boolean = {
  //  GenerationFlags.formal {
  //    return true
  //  }
  //  return false
  //}
  def tempTWidth(): Int = {
    return chunkWidth * scala.math.ceil(
      mainWidth / chunkWidth
    ).toInt
  }
  //def buildTempShape(): Vec[UInt] = {
  //  return Vec(UInt(chunkWidth bits), numChunks)
  //}
  def buildTempShape(): UInt = {
    return UInt((chunkWidth * numChunks()) bits)
    //return Vec(UInt(chunkWidth bits), numChunks)
  }
  def buildChunkStartShape(): SInt = {
    //return SInt((chunkWidth + 1) bits)
    return SInt((log2Up(numChunks()) + 2) bits)
  }

  def numChunks(): Int = {
    return (tempTWidth() / chunkWidth)
  }

  def radix(): Int = {
    return (1 << chunkWidth)
  }

  def dmlElemWidth(): Int = {
    return (denomWidth + chunkWidth)
  }
  def dmlSize(): Int = {
    return radix()
  }
}
case class LongUdivIterDataFormal(cfg: LongDivConfig) extends Bundle {
  //new Bundle {
    //val formalNumer = cfg.buildTempShape()
    //val formalDenom = cfg.buildTempShape()
    val oracleQuot = cfg.buildTempShape()
    val oracleRema = cfg.buildTempShape()
    //val formalDenomMultLut = Vec(
    //  UInt(cfg.dmlElemWidth() bits), cfg.dmlSize()
    //)
  //}
}
case class LongUdivIterData(cfg: LongDivConfig) extends Bundle {
  val tempNumer = cfg.buildTempShape()
  val tempDenom = UInt(cfg.denomWidth bits)
  var tempQuot = cfg.buildTempShape()
  val tempRema = cfg.buildTempShape()
  val denomMultLut = Vec.fill(cfg.dmlSize())(
    UInt(cfg.dmlElemWidth() bits)
  )

  val tag = (cfg.pipelined) generate UInt(cfg.tagWidth bits)
  //if (!cfg.pipelined) {
  //  tag := null
  //}
  //val formal = (cfg.formal) generate LongUdivIterDataFormal(cfg)
  ////val formal = (cfg.formal) generate new Bundle {
  ////  //val formalNumer = cfg.buildTempShape()
  ////  //val formalDenom = cfg.buildTempShape()
  ////  val oracleQuot = cfg.buildTempShape()
  ////  val oracleRema = cfg.buildTempShape()
  ////  //val formalDenomMultLut = Vec(
  ////  //  UInt(cfg.dmlElemWidth() bits), cfg.dmlSize()
  ////  //)
  ////}
  ////if (cfg.formal) {
  ////  formal := null
  ////}
}
case class LongUdivIterIo(cfg: LongDivConfig) extends Bundle {
  //--------
  // Inputs
  val ofwdMvp = in port Bool()
  //if (cfg.formal && cfg.pipelined) {
  //  if (cfg.usePipeSkidBuf) {
  //    ofwdMvp := null
  //  }
  //}
  //val ofwdMvp = (
  //  cfg.formal && cfg.pipelined && cfg.usePipeSkidBuf
  //) generate in port Bool()

  val itdIn = in(LongUdivIterData(cfg=cfg))

  val chunkStart = in(cfg.buildChunkStartShape())
  //--------
  // Outputs
  val itdOut = out(LongUdivIterData(cfg=cfg))

  // Current quotient digit
  val quotDigit = out(UInt(cfg.chunkWidth bits))

  // Remainder with the current chunk of `itdIn.tempNumer` shifted in
  val shiftInRema = out(cfg.buildTempShape())

  // The vector of greater than comparison values
  val gtVec = out(UInt(cfg.radix() bits))
  //--------
}

case class LongUdivIter(cfg: LongDivConfig) extends Component {
  val io = LongUdivIterIo(cfg=cfg)

  def itdIn = io.itdIn
  def itdOut = io.itdOut
  //val tempNumerVec = Vec(UInt(cfg.chunkWidth bits), cfg.numChunks())

  //val siRemaTempNumer = itdIn.tempNumer.asBits
  //val siRemaTempRema = itdIn.tempRema.asBits
  val bcNumChunks: BitCount = cfg.numChunks() bits

  // Shift in the current chunk of `itd_in.temp_numer`
  //val tempNumerInSlices = (
  //  itdIn.tempNumer.subdivideIn(
  //    cfg.numChunks() slices
  //    //cfg.numChunks() bits
  //    //cfg.chunkWidth bits
  //  )(io.chunkStart.asUInt)
  //)
  val chunkStartSliceIdx = UInt(log2Up(cfg.numChunks()) bits)
  chunkStartSliceIdx := io.chunkStart.asUInt(
    log2Up(cfg.numChunks()) - 1 downto 0
  )
  //chunkStartSliceIdx.addAttribute("keep")

  val tempNumerInSlices = (
    //itdIn.tempNumer(
    //  io.chunkStart.asUInt * cfg.chunkWidth,
    //  cfg.chunkWidth bits
    //)
    itdIn.tempNumer.subdivideIn(cfg.numChunks() slices)
    //itdIn.tempNumer.subdivideIn(cfg.chunkWidth bits)
  )
  tempNumerInSlices.addAttribute("keep")
  io.shiftInRema.assignFromBits(Cat(
    //tempNumerInSlices(io.chunkStart.asUInt),
    itdIn.tempRema(cfg.tempTWidth() - cfg.chunkWidth - 1 downto 0),
    tempNumerInSlices(
      //io.chunkStart.asUInt(log2Up(cfg.numChunks()) - 1 downto 0)
      chunkStartSliceIdx
    ),
  )//(io.shiftInRema.bitsRange)
  )
  // Compare every element of the computed `denom * digit` array to
  // `shiftInRema`, computing `gtVec`.
  // This creates perhaps a single LUT delay for the greater-than
  // comparisons given the existence of hard carry chains in FPGAs.
  for (idx <- 0 to cfg.radix - 1) {
    io.gtVec(idx) := (
      itdIn.denomMultLut(idx) > io.shiftInRema
    )
  }

  // Find the current quotient digit with something resembling a
  // priority encoder.
  // This implements binary search.
  val gtVecWidth: Int = io.gtVec.getWidth
  switch (io.gtVec) {
    for (idx <- 0 to gtVecWidth - 1) {
      is (U(
        ("1" * (gtVecWidth - (idx + 1))) + ("0" * (idx + 1))
      )) {
        //io.quotDigit := idx
        io.quotDigit := idx
      }
    }
    default {
      io.quotDigit := 0
    }

    //// Here is an example of the expanded form of this `switch ()`
    //is (U"4'b1110") {
    //  io.quotDigit := 0
    //}
    //is (U"4'b1100") {
    //  io.quotDigit := 1
    //}
    //is (U"4'b1000") {
    //  io.quotDigit := 2
    //}
    //is (U"4'b0000") {
    //  io.quotDigit := 3
    //}
    //default {
    //  io.quotDigit := 0
    //}
  }

  // Drive `itdOut.tempQuot`
  //val tempQuotVec = Vec.fill(cfg.numChunks())(UInt(cfg.chunkWidth bits))
  ////val tempQuotInSlices = itdIn.tempQuot.subdivideIn(
  ////  cfg.numChunks() slices
  ////)
  ////tempQuotVec.assignFromBits(itdIn.tempQuot.asBits)
  ////tempQuotVec.addAttribute("keep")
  ////tempQuotVec(
  ////  //io.chunkStart.asUInt(log2Up(cfg.numChunks) - 1 downto 0)
  ////  chunkStartSliceIdx
  ////).assignFromBits(io.quotDigit.asBits)

  //////itdOut.tempQuot := itdIn.tempQuot
  //////for (idx <- 0 to cfg.numChunks() - 1) {
  //////  when (IntToUInt(idx) === chunkStartSliceIdx) {
  //////    tempQuotVec(idx) := io.quotDigit
  //////  } otherwise {
  //////    //tempQuotVec(idx) := itdIn.tempQuot(
  //////    //  chunkStartSliceIdx * cfg.chunkWidth, cfg.chunkWidth bits
  //////    //)
  //////    tempQuotVec(idx) := tempQuotInSlices(chunkStartSliceIdx)
  //////  }
  //////}
  itdOut.tempQuot := itdIn.tempQuot
  switch (chunkStartSliceIdx) {
    for (idx <- 0 until cfg.numChunks()) {
      is (idx) {
        val tempRange = (
          ((idx + 1) * cfg.chunkWidth) - 1 downto idx * cfg.chunkWidth
        )
        itdOut.tempQuot(
          tempRange
        ) := (
          //chunkStartSliceIdx
          //itdIn.tempQuot(tempRange)
          io.quotDigit
        )
      }
    }
  }
  //itdOut.tempQuot.assignFromBits(tempQuotVec.asBits)
  //--------
  itdOut.tempNumer := itdIn.tempNumer
  itdOut.tempDenom := itdIn.tempDenom
  itdOut.tempRema := (
    io.shiftInRema
    - itdIn.denomMultLut(io.quotDigit)
    //- itdIn.denomMultLut.as_value().word_select
    //	(io.quotDigit, io.chunkWidth())
  )(itdOut.tempRema.bitsRange)
  //--------
  itdOut.denomMultLut := itdIn.denomMultLut
  //--------
  //if (cfg.pipelined)
  //if (
  //  //itdOut.tag != null
  //  cfg.pipelined
  //) {
  //  itdOut.tag := itdIn.tag
  //}
  ////--------
  //if (
  //  //itdOut.formal != null
  //  cfg.formal
  //) {
  //  //println("have cfg.formal")
  //  //--------
  //  itdOut.formal := itdIn.formal
  //  //val formalNumerIn = itdIn.formal.formalNumer
  //  //val formalDenomIn = itdIn.formal.formalDenom
  //  val skipCond = (
  //    //(formalDenomIn === 0)
  //    (itdIn.tempDenom === 0)
  //    | (io.chunkStart < 0)
  //    //| (io.chunkStart >= cfg.numChunks())
  //  )
  //  assume(
  //    itdIn.tempDenom =/= 0
  //  )
  //  //val skipCondOfwdMvp = 1
  //  val skipCondOfwdMvp = Bool()
  //  //domain = m.d.sync
  //  //if constants.PIPELINED():
  //  //  if constants.USE_PIPE_SKID_BUF():
  //  //    skipCondOfwdMvp = io.ofwdMvp
  //  //    domain = m.d.comb
  //  if (cfg.pipelined && cfg.usePipeSkidBuf) {
  //    skipCondOfwdMvp := io.ofwdMvp
  //  } else {
  //    skipCondOfwdMvp := True
  //  }
  //  val fullSkipCond = skipCond | skipCondOfwdMvp

  //  val oracleQuotIn = itdIn.formal.oracleQuot
  //  val oracleRemaIn = itdIn.formal.oracleRema
  //  val oracleQuotInSliced = oracleQuotIn.subdivideIn(
  //    cfg.numChunks() slices
  //  )
  //  //oracleQuotInSliced.addAttribute("keep")
  //  //oracleQuotInSliced.assignFromBits(oracleQuotIn.asBits)

  //  //val formalDenomMultLutIn = itdIn.formal.formalDenomMultLut

  //  //val formalNumerOut = itdOut.formal.formalNumer
  //  //val formalDenomOut = itdOut.formal.formalDenom

  //  val oracleQuotOut = itdOut.formal.oracleQuot
  //  val oracleRemaOut = itdOut.formal.oracleRema

  //  ////val formalDenomMultLutOut = itdOut.formal.formalDenomMultLut
  //  ////--------
  //  ////m.d.comb += (
  //  ////	//assume(formalDenomIn =/= 0),
  //  ////	//assume(formalDenomOut =/= 0),
  //  ////	assume(~skipCond),
  //  ////)
  //  ////assume(~skipCond)
  //  ////assume(~skipCond)
  //  ////--------
  //  ////assume(itdIn.tempNumer === itdIn.tempNumer)
  //  //assert(
  //  //  //skipCond
  //  //  //| (!skipCondOfwdMvp)
  //  //    //skipCondOfwdMvp
  //  //  //& 
  //  //  //fullSkipCond
  //  //  //| (
  //  //    oracleQuotIn === (itdIn.tempNumer / itdIn.tempDenom)
  //  //  //)
  //  //)
  //  //assert(
  //  //  //skipCond
  //  //  //fullSkipCond
  //  //  //| (
  //  //    //(!skipCondOfwdMvp)
  //  //    //& 
  //  //    //|
  //  //    (
  //  //      oracleRemaIn === (itdIn.tempNumer % itdIn.tempDenom)
  //  //    )
  //  //  //)
  //  //)
  //  ////--------

  //  //for (idx <- 0 to cfg.radix - 1) {
  //  //  //assume(
  //  //  //  skipCond
  //  //  //  | (
  //  //  //    skipCondOfwdMvp
  //  //  //    & (
  //  //  //      //itdIn.shape().formal_dml_elem(
  //  //  //      //  itdIn, i, FORMAL
  //  //  //      //) === (itdIn.tempDenom * i)
  //  //  //      formalDenomMultLutIn(idx) === itdIn.tempDenom * idx
  //  //  //    )
  //  //  //  )
  //  //  //)
  //  //  assert(
  //  //    //skipCond
  //  //    //fullSkipCond
  //  //    //| (
  //  //      //(!skipCondOfwdMvp)
  //  //      //& 
  //  //      //| 
  //  //      (
  //  //        itdIn.denomMultLut(idx) === itdIn.tempDenom * idx
  //  //      )
  //  //    //)
  //  //  )
  //  //}
  //  ////when (clockDomain.isResetActive) {
  //  ////  //assumeInitial(
  //  ////  //  itdIn === itdIn.getZero
  //  ////  //)
  //  ////} 
  //  when (!clockDomain.isResetActive) {
  //    //--------
  //    //--------
  //    //itdOut.tempNumer := itdIn.tempNumer
  //    //itdOut.tempDenom := itdIn.tempDenom

  //    oracleQuotOut := oracleQuotIn
  //    oracleRemaOut := oracleRemaIn

  //    //val oracleQuotInSliceInd = io.chunkStart.asUInt(
  //    //  log2Up(cfg.numChunks()) - 1 downto 0
  //    //)
  //    //--------
  //    //itdOut.formal.formalDenomMultLut := formalDenomMultLutIn
  //    //--------
  //    //when (itdIn.tempDenom =/= 0) {
  //    //  assert(
  //    //    //skipCond
  //    //    //fullSkipCond
  //    //    //| (
  //    //      //skipCondOfwdMvp
  //    //      //(!skipCondOfwdMvp)
  //    //      //& 
  //    //      //| 
  //    //      (
  //    //        io.quotDigit
  //    //        === oracleQuotInSliced(
  //    //          //io.chunkStart.asUInt(
  //    //          //  log2Up(cfg.numChunks) - 1 downto 0
  //    //          //)
  //    //          chunkStartSliceIdx
  //    //        )
  //    //        //=== oracleQuotIn(io.chunkStart.asUInt, cfg.chunkWidth bits)
  //    //        //=== oracleQuotIn(
  //    //        //  io.chunkStart.asUInt * cfg.chunkWidth,
  //    //        //  cfg.chunkWidth bits
  //    //        //)
  //    //        //=== oracleQuotIn.word_select
  //    //        //	(io.chunkStart, io.CHUNK_WIDTH())
  //    //      )
  //    //    //)
  //    //  )
  //    //}
  //    ////--------

  //    //// If we are the last pipeline stage (or if we are
  //    //// multi-cycle and computing the last chunk of quotient and
  //    //// final remainder), check to see if our answer is correct  
  //    //when (io.chunkStart <= 0x0) {
  //    //  when (itdIn.tempDenom =/= 0) {
  //    //    assert(
  //    //      //skipCond
  //    //      //fullSkipCond
  //    //      //| (
  //    //        //skipCondOfwdMvp
  //    //        //(!skipCondOfwdMvp)
  //    //        //& 
  //    //        //| 
  //    //        (itdOut.tempQuot === (itdIn.tempNumer / itdIn.tempDenom))
  //    //      //)
  //    //    )
  //    //    assert(
  //    //      //skipCond
  //    //      //fullSkipCond
  //    //      //| (
  //    //        //skipCondOfwdMvp
  //    //        //(!skipCondOfwdMvp)
  //    //        //& 
  //    //        //| 
  //    //        (itdOut.tempRema === (itdIn.tempNumer % itdIn.tempDenom))
  //    //      //)
  //    //    )
  //    //  }
  //    //  //--------
  //    //}
  //    ////--------
  //  }
  //  //--------
  ////--------
  //}
}

//case class LongUdivIterSyncIo(cfg: LongDivConfig) extends Bundle {
//  val sbIo = PipeSkidBufIo(
//    dataType=LongUdivIterData(cfg=cfg),
//    //optIncludeValidBusy=false,
//    //optIncludeReadyBusy=false,
//    optIncludeBusy=false,
//  )
//}

//case class LongUdivIterSync(
//  cfg: LongDivConfig,
//  chunkStartVal: BigInt,
//) extends Component {
//  val io = LongUdivIterSyncIo(cfg=cfg)
//
//  //def dval[T >: Null](x: T): T = { null }
//
//  val it = LongUdivIter(cfg=cfg)
//  //if (cfg.usePipeSkidBuf) {
//  //  io.sbIo.prev >/-> io.sbIo.next
//  //}
//
//  val skidBuf = (cfg.usePipeSkidBuf) generate PipeSkidBuf(
//    dataType=LongUdivIterData(cfg=cfg),
//    //optIncludeValidBusy=false,
//    //optIncludeReadyBusy=false,
//    optIncludeBusy=false,
//    //optPassthrough=false,
//    //optTieIfwdValid=(chunkStartVal == cfg.numChunks() - 1),
//  )
//  val sbIo = (cfg.usePipeSkidBuf) generate skidBuf.io 
//  //if (sbIo != null) {
//  //  io.sbIo <> sbIo
//  //}
//
//  //val loc = new Area {
//  //}
//  val itIo = it.io
//  val itdIn = itIo.itdIn
//  val itdOut = itIo.itdOut
//  //ifwdPayload = io.sbIo.inp.fwd
//  //ibak = io.sbIo.inp.bak
//  //ofwd = io.sbIo.outp.fwd
//  //obak = io.sbIo.outp.bak
//  //val ifwdPayload = (cfg.usePipeSkidBuf) generate io.sbIo.prev.payload
//  val ifwdPayload = io.sbIo.prev.payload
//  val ifwdValid = io.sbIo.prev.valid
//  val obakReady = io.sbIo.prev.ready
//
//  val ofwdPayload = io.sbIo.next.payload
//  val ofwdValid = io.sbIo.next.valid
//  val ibakReady = io.sbIo.next.ready
//
//  //val psbStm = (cfg.usePipeSkidBuf) generate new Stream(
//  //  LongUdivIterData(cfg=cfg)
//  //)
//  //if (cfg.usePipeSkidBuf) {
//  //  io.sbIo.prev >/-> psbStm
//  //  psbStm.translateInto(io.sbIo.next){
//  //    //(o, i) => o := i
//  //    (o, i) => o := itdOutSync
//  //  }
//  //}
//  val itdInSync = (
//    //if (!cfg.usePipeSkidBuf) {
//      ifwdPayload
//    //} else { // if (cfg.usePipeSkidBuf)
//    //  //psbStm.payload
//    //}
//  )
//  val itdOutSync = (
//    if (!cfg.usePipeSkidBuf) {
//      Reg(LongUdivIterData(cfg=cfg))
//    } else { // if (cfg.usePipeSkidBuf)
//      //LongUdivIterData(cfg=cfg)
//      ofwdPayload
//      //psbStm.payload
//    }
//  )
//
//  if (!cfg.usePipeSkidBuf) {
//    //io.sbIo.prev.valid := False
//    //io.sbIo.prev.ready := False
//    //io.sbIo.next.valid := False
//    obakReady := True
//    ofwdValid := True
//    //ofwdPayload := ofwdPayload.getZero
//    //io.sbIo.next.ready := False
//    //io.sbIo
//  }
//
//  val ifwdMove = (
//    (
//      Bool(!cfg.usePipeSkidBuf)
//    ) | (ifwdValid & obakReady)
//  )
//  val ofwdMove = (
//    (
//      Bool(!cfg.usePipeSkidBuf)
//    ) | (ofwdValid & ibakReady)
//  )
//  val iofwdMov = (
//    (
//      Bool(!cfg.usePipeSkidBuf)
//    ) | (
//      ifwdValid & obakReady & ofwdValid & ibakReady
//    )
//  )
//  //val itdOutSync = ofwdPayload
//  //val itdOutSync = (
//  //  if (!cfg.usePipeSkidBuf) {
//  //    Reg(LongUdivIterData(cfg=cfg))
//  //  } else { // if (cfg.usePipeSkidBuf)
//  //    ofwdPayload
//  //    //psbStm.payload
//  //  }
//  //)
//  if (!cfg.usePipeSkidBuf) {
//    itdOutSync.init(itdOutSync.getZero)
//    ofwdPayload := itdOutSync
//    //ofwdPayload := ofwdPayload.getZero
//    //ofwdPayload := ofwdPayload.getZero
//    //ofwdPayload.tempNumer := 3
//  }
//
//  //if (cfg.usePipeSkidBuf) {
//  //  ofwdPayload := itdOutSync
//  //}
//  //val itdOutSync = Reg(ofwdPayload.type)
//  //itdOutSync.init(itdOutSync.getZero)
//  val ifwdMvp = (
//    (
//      Bool(!cfg.usePipeSkidBuf)
//    ) | (
//      past(ifwdValid) & past(obakReady)
//      //ifwdValid & obakReady
//    )
//  )
//  val ofwdMvp = (
//    (
//      Bool(!cfg.usePipeSkidBuf)
//    ) | (
//      past(ofwdValid) & past(ibakReady)
//      //ofwdValid & ibakReady
//    )
//  )
//  val iofwdMvp = (
//    (
//      Bool(!cfg.usePipeSkidBuf)
//    ) | (
//      past(ifwdValid) & past(obakReady)
//      & past(ofwdValid) & past(ibakReady)
//    )
//  )
//
//  val locFormal = (cfg.formal) generate new Area {
//    val eqQuot = Bool() addAttribute("keep")
//    //tempQuotOutSync = Signal(
//    //  len(Value.cast(itdOutSync.tempQuot)),
//    //  name="locFormalTempQuotOutSync", attrs=sigKeep(),
//    //)
//    
//    val tempQuotOutSync = cfg.buildTempShape() addAttribute("keep")
//    val tempQuotOut = cfg.buildTempShape() addAttribute("keep")
//
//    tempQuotOutSync := itdOutSync.tempQuot
//    tempQuotOut := itdOut.tempQuot
//    eqQuot := tempQuotOutSync === tempQuotOut
//
//    val eqRema = Bool() addAttribute("keep")
//    val tempRemaOutSync = cfg.buildTempShape() addAttribute("keep")
//    val tempRemaOut = cfg.buildTempShape() addAttribute("keep")
//
//    tempRemaOutSync := itdOutSync.tempRema
//    tempRemaOut := itdOut.tempRema
//    eqRema := tempRemaOutSync === tempRemaOut
//
//    val tagIn = UInt(cfg.tagWidth bits) addAttribute("keep")
//    val tagOutSync = UInt(cfg.tagWidth bits) addAttribute("keep")
//    val eqTag = Bool() addAttribute("keep")
//
//    tagIn := itdIn.tag
//    tagOutSync := itdOutSync.tag
//    eqTag := tagIn === tagOutSync
//
//    if (cfg.usePipeSkidBuf) {
//      //itIo.ofwdMvp := ofwdMvp
//      itIo.ofwdMvp := io.sbIo.prev.fire
//    }
//  }
//  //--------
//  itIo.chunkStart := chunkStartVal
//  if (!cfg.usePipeSkidBuf) {
//    //ofwdValid := True
//    //obakReady := True
//    itdIn := itdInSync
//    //m.d.sync += itdOutSync.eq(itdOut)
//    itdOutSync := itdOut
//  } else { // if (cfg.usePipeSkidBuf)
//    //itdIn := itdInSync
//    //ofwdPayload := itdOut
//    //sbIo.misc.clear := False
//    //itdIn := cfg.
//    //itdIn :=
//    //itdIn := 
//    //var parentData = PsbIoParentData()
//    //parentData.fromChild = 
//    //var parentData: Option[PsbIoParentData] = Some(PsbIoParentData[LongUdivIterData])
//    //var parentData = 
//
//    io.sbIo.connectChild(
//      childSbIo=sbIo,
//      //parentData=Option[PsbIoParentData](PsbIoParentData[LongUdivIterData(cfg=cfg)](fromChild=itdIn,
//      //toOut=itdOut))
//      parentData=Some(
//        PsbIoParentData(fromChild=itdIn, toOut=itdOut)
//      )
//      //parentData=None,
//    )
//  }
//  //--------
//  if (cfg.formal) {
//    //--------
//    //skipCond = itdIn.formal.formalDenom.asValue() === 0
//    val skipCond = itdIn.tempDenom === 0
//    assume(!skipCond)
//    //--------
//    when (pastValidAfterReset()) {
//      //--------
//      //--------
//      assert(
//        if (!cfg.usePipeSkidBuf) {
//          itdIn.tempNumer === itdInSync.tempNumer
//        } else {
//          //itdIn.tempNumer === itdInSync.tempNumer
//          True
//        }
//      )
//
//      assert(
//        if (!cfg.usePipeSkidBuf) {
//          itdIn.tempQuot === itdInSync.tempQuot
//        } else {
//            //itdIn.tempQuot === itdInSync.tempQuot
//          True
//        }
//      )
//      assert(
//        if (!cfg.usePipeSkidBuf) {
//          itdIn.tempRema === itdInSync.tempRema
//        } else {
//          //itdIn.tempRema
//          //	=== past(itdInSync.tempRema)
//          //itdIn.tempRema
//          //	=== itdOutSync.tempRema
//          True
//        }
//      )
//
//      assert(
//        if (!cfg.usePipeSkidBuf) {
//          itdIn.tag === itdInSync.tag
//        } else {
//          //itdIn.tag === past(itdInSync.tag)
//          //itdIn.tag === itdOutSync.tag
//          locFormal.eqTag
//        }
//      )
//
//      assert(
//        if (!cfg.usePipeSkidBuf) {
//          //itdIn.formal.formalNumer === itdInSync.formal.formalNumer
//          itdIn.tempNumer === itdInSync.tempNumer
//        } else {
//          //itdIn.formal.formalNumer
//          //=== past(itdInSync.formal.formalNumer
//          //	)
//          //itdIn.formal.formalNumer
//          //=== itdOutSync.formal.formalNumer
//          //itdIn.tempNumer === itdOutSync.tempNumer
//          itdOut.tempNumer === itdOutSync.tempNumer
//        }
//      )
//      assert(
//        if (!cfg.usePipeSkidBuf) {
//          //itdIn.formal.formalDenom
//          //  === itdInSync.formal.formalDenom
//          itdIn.tempDenom === itdInSync.tempDenom
//        } else {
//          //itdIn.formal.formalDenom
//          //=== past(itdInSync.formal.formalDenom)
//          //itdIn.formal.formalDenom
//          //=== itdOutSync.formal.formalDenom
//          //itdIn.tempDenom === itdOutSync.tempDenom
//          itdOut.tempDenom === itdOutSync.tempDenom
//        }
//      )
//
//      assert(
//        if (!cfg.usePipeSkidBuf) {
//          (
//            //skipCond
//            //| (
//              itdIn.formal.oracleQuot
//              === itdInSync.formal.oracleQuot
//            //)
//          )
//        } else {
//          (
//            //skipCond
//            //| (
//              //itdIn.formal.oracleQuot
//              ////=== past(itdInSync.formal.oracleQuot
//              ////	)
//              //=== itdOutSync.formal.oracleQuot
//              itdOut.formal.oracleQuot
//              === itdOutSync.formal.oracleQuot
//            //)
//          )
//        }
//      )
//      assert(
//        if (!cfg.usePipeSkidBuf) {
//          (
//            //skipCond
//            //| 
//            (itdIn.formal.oracleRema
//              === itdInSync.formal.oracleRema)
//          )
//        } else {
//          (
//            //skipCond
//            //| (
//              //itdIn.formal.oracleRema
//              //=== past(itdInSync.formal.oracleRema)
//              //itdIn.formal.oracleRema
//              //=== itdOutSync.formal.oracleRema
//              itdOut.formal.oracleRema
//                === itdOutSync.formal.oracleRema
//            //)
//          )
//        }
//      )
//    when (ofwdMvp) {
//      //--------
//      assert(
//        //Mux(
//        //ofwdMvp,
//        if (!cfg.usePipeSkidBuf) {
//          itdOutSync.tempNumer === past(itdInSync.tempNumer)
//        } else {
//          //itdOutSync.tempNumer === itdIn.tempNumer
//          itdOutSync.tempNumer === itdOut.tempNumer
//        //1,
//        //)
//        }
//      )
//      //assert(
//      //	itdOutSync.formal === past(itdIn.formal)),
//      assert(
//        if (!cfg.usePipeSkidBuf) {
//          itdOutSync.tempNumer === past(itdInSync.tempNumer)
//        } else {
//          //itdOutSync.tempNumer === itdIn.tempNumer
//          itdOutSync.tempNumer === itdOut.tempNumer
//        }
//      )
//      assert(
//        //Mux(
//        //ofwdMvp,
//        if (!cfg.usePipeSkidBuf) {
//          itdOutSync.tempDenom === past(itdInSync.tempDenom)
//        } else {
//          //itdOutSync.tempDenom === itdIn.tempDenom
//          itdOutSync.tempDenom === itdOut.tempDenom
//        }
//        //1,
//        //)
//      )
//
//      assert(
//        if (!cfg.usePipeSkidBuf) {
//          (
//            itdOutSync.formal.oracleQuot
//            === past(itdInSync.formal.oracleQuot)
//          )
//        } else {
//          //itdOutSync.formal.oracleQuot === itdIn.formal.oracleQuot
//          itdOutSync.formal.oracleQuot === itdOut.formal.oracleQuot
//        }
//      )
//      assert(
//        if (!cfg.usePipeSkidBuf) {
//          (itdOutSync.formal.oracleRema
//            === past(itdInSync.formal.oracleRema))
//        }
//        else {
//          //(itdOutSync.formal.oracleRema
//          //=== itdIn.formal.oracleRema)
//          (itdOutSync.formal.oracleRema
//          === itdOut.formal.oracleRema)
//        }
//      )
//
//      assert(
//        //Mux(
//        //ofwdMvp,
//        if (!cfg.usePipeSkidBuf) {
//          (itdOutSync.denomMultLut
//            === past(itdInSync.denomMultLut))
//        }
//        else {
//          //(itdOutSync.denomMultLut === itdIn.denomMultLut)
//          itdOutSync.denomMultLut === itdOut.denomMultLut
//        }
//        //1,
//        //)
//      )
//      //--------
//      assert(
//        //Mux(
//        //ofwdMvp,
//        ////Mux(
//        ////	(!cfg.usePipeSkidBuf),
//        if (!cfg.usePipeSkidBuf) {
//          itdOutSync.tempNumer === past(itdInSync.tempNumer)
//        }
//        else {
//          //itdOutSync.tempNumer === itdIn.tempNumer
//          itdOutSync.tempNumer === itdOut.tempNumer
//        }
//        ////	////(itdOutSync.tempNumer
//        ////	////	=== past(itdOut.tempNumer)),
//        ////	//(itdOutSync.tempNumer
//        ////	//	=== past(itdIn.tempNumer)),
//        ////	1,
//        ////),
//        //1,
//        //)
//      )
//    }
//    assert(
//      if (!cfg.usePipeSkidBuf) {
//        //Mux(
//        ////ofwdMvp,
//        ////iofwdMvp,
//        //(1 if (!cfg.usePipeSkidBuf) else 0),
//        (itdOutSync.tempQuot
//          === past(itdOut.tempQuot))
//        ////(itdOutSync.tempQuot
//        ////	=== past(itdInSync.tempQuot)),
//        //itdOutSync.tempQuot === itdOut.tempQuot,
//        ////1,
//        //)
//      } else { // if (cfg.usePipeSkidBuf)
//        //itdOutSync.tempQuot === itdOut.tempQuot
//        locFormal.eqQuot
//      }
//    )
//    assert(
//      if (!cfg.usePipeSkidBuf) {
//        //Mux(
//          //ofwdMvp,
//          //iofwdMvp,
//          (
//            //(1 if (!cfg.usePipeSkidBuf) else 0)
//            //&
//            (itdOutSync.tempRema
//              === past(itdOut.tempRema))
//          )
//          //(itdOutSync.tempRema
//          //	=== past(itdInSync.tempRema)),
//          //| (
//          //	(0 if (!cfg.usePipeSkidBuf) else 1)
//          //	& (itdOutSync.tempRema
//          //		=== itdOut.tempRema)
//          //)
//          //1,
//        //)
//      } else { // if (cfg.usePipeSkidBuf)
//        //(itdOutSync.tempRema
//        //	=== itdOut.tempRema)
//        locFormal.eqRema
//      }
//    )
//      
//    //--------
//    assert(
//      //Mux(
//      //ofwdMvp,
//      //iofwdMvp,
//      //Mux(
//      //	(!cfg.usePipeSkidBuf),
//        if (!cfg.usePipeSkidBuf) {
//          itdOutSync.denomMultLut === past(itdInSync.denomMultLut)
//        } else {
//          //itdOutSync.denomMultLut === itdInSync.denomMultLut
//          itdOut.denomMultLut === itdIn.denomMultLut
//        }
//      //	////(itdOutSync.denomMultLut
//      //	////	=== past(itdOut.denomMultLut)),
//      //	//(itdOutSync.denomMultLut
//      //	//	=== past(itdIn.denomMultLut)),
//      //	1,
//      //),
//      //1,
//      //)
//    )
//      //--------
//    assert(
//      if (!cfg.usePipeSkidBuf) {
//        itdOutSync.tempNumer === past(itdInSync.tempNumer)
//      } else {
//        //itdOutSync.tempNumer === itdIn.tempNumer
//        itdOut.tempNumer === itdIn.tempNumer
//      }
//    )
//    assert(
//      if (!cfg.usePipeSkidBuf) {
//        itdOutSync.tempDenom === past(itdInSync.tempDenom)
//      } else {
//        //itdOutSync.tempDenom === itdIn.tempDenom
//        itdOut.tempDenom === itdIn.tempDenom
//      }
//    )
//
//    assert(
//      if (!cfg.usePipeSkidBuf) {
//        (itdOutSync.formal.oracleQuot
//          === past(itdInSync.formal.oracleQuot))
//      } else {
//        //itdOutSync.formal.oracleQuot === itdIn.formal.oracleQuot
//        itdOut.formal.oracleQuot === itdIn.formal.oracleQuot
//      }
//    )
//    assert(
//      if (!cfg.usePipeSkidBuf) {
//        (itdOutSync.formal.oracleRema
//          === past(itdInSync.formal.oracleRema))
//      } else {
//        //itdOutSync.formal.oracleRema === itdIn.formal.oracleRema
//        itdOut.formal.oracleRema === itdIn.formal.oracleRema
//      }
//    )
//    //--------
//    assert(
//      if (!cfg.usePipeSkidBuf) {
//        itdOutSync.denomMultLut === past(itdInSync.denomMultLut)
//      } else {
//        //itdOutSync.denomMultLut === itdIn.denomMultLut
//        itdOut.denomMultLut === itdIn.denomMultLut
//      }
//    )
//    //--------
//    }
//    //--------
//  }
//}
