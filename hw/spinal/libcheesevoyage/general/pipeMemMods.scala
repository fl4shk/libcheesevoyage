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

//--------
//case class PipeMemWrPayload[
//  T <: Data,
//](
//  wordType: HardType[T],
//  wordCount: Int,
//) extends Bundle {
//  val addr = UInt(log2Up(wordCount) bits)
//  val data = wordType()
//}
//case class PipeMemRdPushPayload[
//  T <: Data,
//](
//  wordType: HardType[T],
//  wordCount: Int,
//) extends Bundle {
//  val addr = UInt(log2Up(wordCount) bits)
//}
//case class PipeMemRdPopPayload[
//  T <: Data,
//](
//  wordType: HardType[T],
//  wordCount: Int,
//) extends Bundle {
//  val data = wordType() 
//}
//--------
//case class PipeMemIo[
//  T <: Data,
//](
//  wordType: HardType[T],
//  wordCount: Int,
//) extends Bundle with IMasterSlave {
//  //--------
//  val wr = slave(
//    Stream(PipeMemWrPayload(
//      wordType=wordType(),
//      wordCount=wordCount,
//    ))
//  )
//  val rdPush = slave(
//    Stream(PipeMemRdPushPayload(
//      wordType=wordType(),
//      wordCount=wordCount,
//    ))
//  )
//  val rdPop = master(
//    Stream(PipeMemRdPopPayload(
//      wordType=wordType(),
//      wordCount=wordCount,
//    ))
//  )
//  //--------
//  def asMaster(): Unit = {
//    master(wr)
//    master(rdPush)
//    slave(rdPop)
//  }
//  //--------
//}
//case class PipeMemRmwFrontPayload
//[
//  ModT <: Data
//]
//(
//  modType: HardType[ModT],
//  //wordType: HardType[WordT],
//  wordCount: Int,
//  //memCount: Int,
//) extends Bundle {
//  //val addr = DualTypeNumVec2(
//  //  dataTypeX=UInt(log2Up(wordCount) bits),
//  //  dataTypeY=UInt(log2Up(memCount) bits),
//  //)
//  val addr = UInt(log2Up(wordCount) bits)
//  //val data = wordType()
//}
case class PipeMemRmwPayloadExt[
  WordT <: Data
](
  wordType: HardType[WordT],
  wordCount: Int,
  modStageCnt: Int,
  //optUseModMemAddr: Boolean=false,
) extends Bundle {
  //--------
  def debug: Boolean = {
    GenerationFlags.formal {
      return true
    }
    return false
  }
  //--------
  val memAddr = UInt(PipeMemRmw.addrWidth(wordCount=wordCount) bits)
  //val modMemAddrRaw = (optUseModMemAddr) generate cloneOf(memAddr)
  //def modMemAddr = (
  //  if (optUseModMemAddr) {
  //    modMemAddrRaw
  //  } else { // if (!optUseModMemAddr)
  //    memAddr
  //  }
  //)
  val modMemWord = wordType()
  val rdMemWord = wordType()
  val rdValid = Bool()

  // hazard for when an address is already in the pipeline 
  val hazardId = (
    SInt(log2Up(modStageCnt) + 3 bits)
    //UInt(log2Up(modStageCnt) bits)
  )

  //val frontDuplicateIt = Bool()
  val dbgModMemWord = (debug) generate (
    wordType()
  )
  val dbgMemReadSync = (debug) generate (
    wordType()
  )
  //--------
}
trait PipeMemRmwPayloadBase[
  WordT <: Data,
] extends Bundle {
  //--------
  //// get the address of the memory to modify
  //def getMemAddr(): UInt
  //--------
  def setPipeMemRmwExt(
    inpExt: PipeMemRmwPayloadExt[WordT],
    memArrIdx: Int,
  ): Unit
  //--------
  def getPipeMemRmwExt(
    outpExt: PipeMemRmwPayloadExt[WordT],
      // this is essentially a return value
    memArrIdx: Int,
  ): Unit
  //--------
  //// function to set the `ModT`'s memory word
  //// sample functionality:
  //// mod.memWord := word
  //def setMemWord(
  //  word: WordT
  //): Unit
  ////--------
  //// function to get the `ModT`'s memory word
  //// sample functionality:
  //// word := mod.memWord
  //def getMemWord(
  //  word: WordT, // this is essentially a return value
  //): Unit
  ////--------
  //def setRdValid(
  //  rdValid: Bool,
  //): Unit
  ////--------
  //def getRdValid(
  //  rdValid: Bool,  // this is essentially a return value
  //): Unit
  ////--------
}
object PipeMemRmw {
  def addrWidth(
    wordCount: Int,
  ) = log2Up(wordCount)
}
case class PipeMemRmwMultiRdTypeDisabled[
  WordT <: Data
](
) extends Bundle with PipeMemRmwPayloadBase[WordT] {
  //--------
  def setPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[WordT],
    memArrIdx: Int,
  ): Unit = {
  }
  def getPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[WordT],
    memArrIdx: Int,
  ): Unit = {
  }
  //--------
  //def getMemAddr(): UInt = U"1'd0"
  //def setMemWord(
  //  word: WordT
  //): Unit = {
  //}
  //def getMemWord(
  //  word: WordT // this is essentially a return value
  //): Unit = {
  //}
  //def setRdValid(
  //  rdValid: Bool,
  //): Unit = {
  //}
  //def getRdValid(
  //  rdValid: Bool,
  //): Unit = {
  //}
}
case class PipeMemRmwIo[
  WordT <: Data,
  ModT <: PipeMemRmwPayloadBase[WordT],
  MultiRdT <: PipeMemRmwPayloadBase[WordT],
](
  wordType: HardType[WordT],
  wordCount: Int,
  modType: HardType[ModT],
  //optMultiRdType: Option[HardType[MultiRdT]]=None,
  //optMultiRdType: Option[HardType[MultiRdT]]={
  //  //Some(HardType[PipeMemRmwMultiRdTypeDisabled[WordT]]())
  //},
  multiRdType: HardType[MultiRdT]=PipeMemRmwMultiRdTypeDisabled[WordT](),
  //optMultiRdSize: Option[Int]=None,
  multiRdSize: Int=0,
) extends Bundle {
  //--------
  //val front = slave(
  //  Stream(PipeMemRmwFrontPayload(
  //    wordCount=wordCount,
  //  ))
  //)

  // front of the pipeline (push)
  val front = slave(Stream(modType()))

  // Use `modFront` and `modBack` to insert a pipeline stage for modifying
  // the`WordT`
  val modFront = master(Stream(modType()))
  val modBack = slave(Stream(modType()))

  // back of the pipeline (output)
  val back = master(Stream(modType()))
  //--------
  //val optMultiRd: Boolean = optMultiRdType match {
  //  case Some(myOptMultiRdType) => true
  //  case None => false
  //}
  //def multiRdType() = optMultiRdType match {
  //  case Some(myOptMultiRdType) => myOptMultiRdType()
  //  case None => PipeMemRmwMultiRdTypeDisabled[WordT]()
  //}
  //--------
  val optMultiRd = (multiRdSize > 0)
  val multiRdFront = (optMultiRd) generate (
    slave(
      Stream(multiRdType())
    )
  )
  val multiRdBack = (optMultiRd) generate (
    master(
      Stream(multiRdType())
    )
  )
  //--------
  //val (multiRdPush, multiRdPop) = optMultiRdType match {
  //  case Some(myMultiRdType) => {
  //    (
  //      slave(
  //        Stream(myMultiRdType())
  //      ), // multiRdPush
  //      master(
  //        Stream(myMultiRdType())
  //      ) // multiRdPop
  //    )
  //  }
  //  case None => (None, None)
  //}
}

// Read-Modify-Write
case class PipeMemRmw[
  WordT <: Data,
  ModT <: PipeMemRmwPayloadBase[WordT],
  MultiRdT <: PipeMemRmwPayloadBase[WordT],
](
  wordType: HardType[WordT],
  wordCount: Int,
  modType: HardType[ModT],
  modStageCnt: Int,
  memArrIdx: Int=0,
  //optMultiRdType: Option[HardType[MultiRdT]]=None,
  multiRdType: HardType[MultiRdT]=PipeMemRmwMultiRdTypeDisabled[WordT](),
  multiRdSize: Int=0,
  initBigInt: Option[Seq[BigInt]]=None,
  forFmax: Boolean=false,
)
//(
//  getModAddr: (
//    ModT,   // mod
//  ) => UInt,
//  setModWord: (
//    ModT,   // mod
//    WordT,  // word
//    //Stream[ModT],
//  ) => Unit,
//  getModWord: (
//    ModT,   // mod
//    WordT,  // word
//  ) => Unit,
//)
extends Component {
  //--------
  def debug: Boolean = {
    GenerationFlags.formal {
      return true
    }
    return false
  }
  //--------
  //val io = slave(
  //  PipeMemIo(
  //    wordType=wordType(),
  //    wordCount=wordCount,
  //  )
  //)
  //val io = PipeMemRmwIo(
  //  modType=modType(),
  //  wordCount=wordCount,
  //  optMultiRdType=optMultiRdType,
  //)
  val io = PipeMemRmwIo(
    wordType=wordType(),
    wordCount=wordCount,
    modType=modType(),
    multiRdType=multiRdType(),
    multiRdSize=multiRdSize,
  )
  //--------
  def mkMem() = {
    val ret = Mem(
      wordType=wordType(),
      wordCount=wordCount,
    )
    initBigInt match {
      case Some(myInitBigInt) => {
        assert(myInitBigInt.size == wordCount)
        ret.initBigInt(myInitBigInt)
      }
      case None => {
        //ret.initBigInt({
        //  //val tempArr = new ArrayBuffer[BigInt]()
        //  //for (idx <- 0 until wordCount) {
        //  //  tempArr += BigInt(0)
        //  //}
        //  //tempArr.toSeq
        //  Array.fill(wordCount)(BigInt(0)).toSeq
        //})
      }
    }
    ret
  }
  val modMem = mkMem()
  val multiRdMemArr = new ArrayBuffer[Mem[WordT]]()
  for (idx <- 0 until multiRdSize) {
    multiRdMemArr += mkMem()
  }
  def memWriteIterate(
    writeFunc: (Mem[WordT]) => Unit
  ): Unit = {
    writeFunc(modMem)
    for (idx <- 0 until multiRdSize) {
      writeFunc(multiRdMemArr(idx))
    }
  }
  def memWriteAll(
    address: UInt,
    data: WordT,
    //enable: Bool=null,
    //mask: Bits=null,
  ): Unit = {
    memWriteIterate(
      (item: Mem[WordT]) => {
        item.write(
          address=address,
          data=data,
          //enable=enable,
          //mask=mask,
        )
      }
    )
  }
  //def memIterate(
  //  readFunc: (Mem[WordT]) => WordT
  //): Unit = {
  //  readFunc(modMem)
  //  for (idx <- 0 until multiRdSize) {
  //    readFunc(multiRdMemArr(idx))
  //  }
  //}
  //--------
  def mkExt() = PipeMemRmwPayloadExt(
    wordType=wordType(),
    wordCount=wordCount,
    modStageCnt=modStageCnt,
  )
  val linkArr = PipeHelper.mkLinkArr()
  val mod = new Area {
    val front = new Area {
      val pipe = PipeHelper(linkArr=linkArr)
      val inpPipePayload = Payload(modType())
      val outpPipePayload = Payload(modType())

      val cFront = pipe.addStage("Front")
      val cLastFront = pipe.addStage(
        name="LastFront", 
        finish=true,
      )
      //val cModFront = pipe.addStage("ModFront")
      pipe.first.up.driveFrom(io.front)(
        con=(node, payload) => {
          node(inpPipePayload) := payload 
        }
      )
      //when (cFront.up.isValid) {
      //}

      //--------
      // This is equivalent to the following in `PipeMemTest`:
      //  cSum.terminateWhen(
      //    !cSum.up(rdValid)
      //  )
      val tempModFront = cloneOf(io.modFront)
      val modFrontTerminateCond = Bool()
      val modFrontTerminateMaybe = (
        //tempModFront.clearValidWhen(modFrontTerminateCond)
        //tempModFront.throwWhen(modFrontTerminateCond)
        tempModFront
      )
      io.modFront << modFrontTerminateMaybe
      //--------
      pipe.last.down.driveTo(
        //io.modFront
        //modFrontTerminateMaybe
        tempModFront
      )(
        con=(payload, node) => {
          payload := node(outpPipePayload)
        }
      )
      //--------
    }
    val back = new Area {
      val pipe = PipeHelper(linkArr=linkArr)
      val pipePayload = Payload(modType())

      //val cModBack = pipe.addStage("ModBack")
      val cBack = pipe.addStage("Back")
      val cLastBack = pipe.addStage(
        name="LastBack",
        finish=true,
      )
      pipe.first.up.driveFrom(io.modBack)(
        con=(node, payload) => {
          node(pipePayload) := payload 
        }
      )
      val rTempWord = (debug) generate (
        Reg(wordType()) addAttribute("keep")
      )
      if (debug) {
        rTempWord.init(rTempWord.getZero)
      }
      //when (cBack.up.isValid) {
      //}
      //val tempBackStm = cloneOf(io.back)
      pipe.last.down.driveTo(io.back)(
        con=(payload, node) => {
          payload := node(pipePayload)
        }
      )
      //io.back <-/< tempBackStm.haltWhen(
      //  !(RegNextWhen(True, io.front.fire) init(False))
      //)
      //io.back << tempBackStm
    }
    //--------
  }
  val cFront = mod.front.cFront
  //--------
  val cFrontArea = new cFront.Area {
    //--------
    val upExt = Vec.fill(2)(mkExt()).setName("cFrontArea_upExt")
    upExt(1) := upExt(0)
    upExt(1).allowOverride
    val lastUpExt = mkExt().setName("cFrontArea_lastUpExt")
    val backUpExt = mkExt().setName("cFrontArea_backUpExt")

    val tempUpMod = Vec.fill(2)(modType())
    tempUpMod(0).allowOverride
    tempUpMod(0) := up(mod.front.inpPipePayload)
    tempUpMod(0).getPipeMemRmwExt(
      outpExt=upExt(0),
      memArrIdx=memArrIdx,
    )
    val tempBackUpMod = modType()
    tempBackUpMod := mod.back.cBack.up(mod.back.pipePayload)
    tempBackUpMod.getPipeMemRmwExt(
      outpExt=backUpExt,
      memArrIdx=memArrIdx,
    )
    //--------
    mod.front.pipe.last.up(mod.front.inpPipePayload).getPipeMemRmwExt(
      outpExt=lastUpExt,
      memArrIdx=memArrIdx,
    )
    val tempRdValid = (
      lastUpExt.memAddr =/= upExt(0).memAddr
      ////&& !lastUpExt.hazardId.msb
      ////&& upExt(1).hazardId.msb
      //|| !upExt(1).hazardId.msb
    ).setName("cFrontArea_tempRdValid")
    //--------
    // This is equivalent to the following in `PipeMemTest`:
    //  cSum.terminateWhen(
    //    !cSum.up(rdValid)
    //  )
    //mod.front.modFrontTerminateCond := Mux[Bool](
    //  up.isValid,
    //  !lastUpExt.rdValid,
    //  True
    //)
    //mod.front.modFrontTerminateCond := False
    //val modFrontTerminateCond = mod.front.modFrontTerminateCond
    //modFrontTerminateCond := False
    //when (up.isValid) {
    //  modFrontTerminateCond := (
    //    !(
    //      //lastUpExt.rdValid
    //      //&& lastUpExt.hazardId === 1
    //      upExt(1).rdValid
    //      && upExt(1).hazardId === 0
    //    )
    //  )
    //}
    //--------
    val nextUpRdValid = Bool()
    val rUpRdValid = (
      RegNext(nextUpRdValid) init(False)
    )
    nextUpRdValid := rUpRdValid

    //when (isValid) {
      //up(rdValid) := (
      //  //tempRdValid
      //  //myUpRdValid
      //  //rPrevMyUpRdValid
      //  myUpRdValid
      //)
      upExt(1).rdValid := nextUpRdValid
      //upExt(1).rdValid := rUpRdValid
    //} otherwise {
    //  //up(rdValid) := rPrevMyUpRdValid
    //  upExt(1).rdValid := rPrevMyUpRdValid
    //}
    //--------
    //object DuplicateItState extends SpinalEnum(
    //  defaultEncoding=binarySequential
    //) {
    //  val
    //    IDLE,
    //    //START,
    //    WAIT_CNT
    //      = newElement();
    //}
    //val nextDuplicateItState = DuplicateItState()
    //val rDuplicateItState = (
    //  RegNext(nextDuplicateItState) init(nextDuplicateItState.getZero)
    //)

    //val doDuplicateIt = Bool().addAttribute("keep")
    //def setDoDuplicateIt(
    //  nDoDuplicateIt: Boolean
    //): Unit = {
    //  if (nDoDuplicateIt) {
    //    duplicateIt()
    //    doDuplicateIt := True
    //  } else {
    //    doDuplicateIt := False
    //  }
    //}
    ////val nextDuplicateIt = Bool()
    ////val rDuplicateIt = (
    ////  RegNext(nextDuplicateIt) init(nextDuplicateIt.getZero)
    ////)
    ////nextDuplicateIt := rDuplicateIt
    //nextDuplicateItState := rDuplicateItState
    ////up(frontDuplicateIt) := nextDuplicateIt
    ////upExt.frontDuplicateIt := nextDuplicateIt
    val nextHazardId = cloneOf(upExt(1).hazardId)
    val rHazardId = RegNext(nextHazardId) init(
      //S(nextHazardId.getWidth bits, default -> True)
      -1
    )
    //nextHazardId := rHazardId
    //nextHazardId := modStageCnt - 1
    //nextHazardId := S(nextHazardId.getWidth bits, default -> True)
    nextHazardId := -1
    //when (isValid) {
      upExt(1).hazardId := nextHazardId
    //} otherwise {
      //upExt(1).hazardId := rHazardId
    //}
    //--------
    val hazardIdMinusOne = rHazardId - 1
    val rUpMemAddrDel = Vec.fill(8)(
      Reg(cloneOf(upExt(1).memAddr))
        init(upExt(1).memAddr.getZero)
    )
    for (idx <- 0 until rUpMemAddrDel.size) {
      when (up.isFiring) {
        if (idx == 0) {
          rUpMemAddrDel(idx) := upExt(1).memAddr
        } else {
          rUpMemAddrDel(idx) := rUpMemAddrDel(idx - 1)
        }
      }
    }
    //when (
    //  //mod.front.cLastFront.up.isFiring
    //  down.isFiring
    //) {
      //when (!nextHazardId.msb) {
      //  //nextHazardId := hazardIdMinusOne
      //  duplicateIt()
      //  //myUpRdValid := tempRdValid
      //  //when (
      //  //  !tempRdValid
      //  //) {
      //  //  when (up.isFiring) {
      //  //    nextHazardId := hazardIdMinusOne
      //  //  }
      //  //}
      //}
      //when (up.isValid) {
      //}
      //--------
      //when (
      //  //!rHazardId.msb
      //  rHazardId.msb
      //  //!nextHazardId.msb
      //) {
      //  duplicateIt()
      //  //up.ready := False
      //}
      //--------
      //otherwise {
      //}
      //--------
      //val rTempHazardCnt = Reg(UInt(8 bits)) init(0x0)
      //--------
      // BEGIN: debug
      // END: debug
      //--------
      //nextHazardId := next
      //when (  
      //  //(
      //  //  RegNextWhen(True, io.modFront.fire) init(False)
      //  //) 
      //  True
      //) {
      //  //when (!tempRdValid) {
      //  //}
      //  when (
      //    //up.isFiring
      //    up.isValid
      //    && (
      //      upExt(0).memAddr
      //      === (
      //        RegNextWhen(upExt(0).memAddr, up.isFiring) init(0x0)
      //      )
      //    )
      //  ) {
      //    duplicateIt()
      //  }
      //}
      //--------
      val nextDuplicateIt = Bool()
      val rDuplicateIt = RegNext(nextDuplicateIt) init(False)
      nextDuplicateIt := rDuplicateIt
      when (!rDuplicateIt) {
        when (
          up.isValid
        ) {
          when (
            upExt(0).memAddr === rUpMemAddrDel(0)
          ) {
            duplicateIt()
            nextDuplicateIt := True
            nextHazardId := (
              modStageCnt
              - (
                if (!forFmax) (
                  1
                ) else (
                  0 
                )
              )
            )
          }
        }
      } otherwise { // when (rDuplicateIt)
        //when (!rHazardId.msb) {
          when (down.isFiring) {
            nextHazardId := hazardIdMinusOne
          }
          when (nextHazardId.msb) {
            nextDuplicateIt := False
          } otherwise {
            duplicateIt()
          }
        //}
      }
      //when (rHazardId.msb) {
      //  //when (
      //  //  down.isFiring
      //  //) {
      //  //}
      //  //--------
      //  //when (up.isFiring) {
      //    //myUpRdValid := tempRdValid
      //    when (
      //      //up.isValid
      //      ////up.isFiring
      //      ////io.modFront.fire
      //      //////up.isFiring
      //      up.isValid
      //    ) {
      //      when (
      //        //!tempRdValid
      //        upExt(0).memAddr
      //        === (
      //          //RegNextWhen(upExt(0).memAddr, up.isFiring) init(0x0)
      //          rUpMemAddrDel(0)
      //        )
      //      ) {
      //        duplicateIt()
      //        nextHazardId := modStageCnt - 1
      //        //nextHazardId := modStageCnt - 1
      //        //nextHazardId := 0
      //        //--------
      //        // BEGIN: debug
      //        //nextHazardId := 0
      //        // end: debug
      //        //--------
      //        //when (up.isValid) {
      //        //}
      //      }
      //    }
      //  //}
      //  //--------
      //} otherwise { // when (!rHazardId.msb)
      //  //when (nextHazardId.msb) {
      //  //  duplicateIt()
      //  //}
      //  //duplicateIt()
      //  //--------
      //  when (
      //    ////down.isFiring
      //    ////io.modFront.fire
      //    ////up.isFiring
      //    ////down.isFiring
      //    //--------
      //    //up.isFiring
      //    //io.modFront.fire
      //    down.isFiring
      //    //--------
      //  ) {
      //    nextHazardId := hazardIdMinusOne
      //  }
      //  //duplicateIt()
      //  when (
      //    //if (modStageCnt == 1) {
      //    //  False
      //    //} else {
      //    //  !hazardIdMinusOne.msb
      //    //}
      //    !nextHazardId.msb
      //  ) {
      //    duplicateIt()
      //  }
      //  //--------
      //  //when (
      //  //  mod.back.cBack.up.isFiring
      //  //  && backUpExt.hazardId === 0
      //  //) {
      //  //}
      //}
      ////--------
    //}
    //def setRdMemWord(): Unit = {
    //}
    //setDoDuplicateIt(false)

    //switch (rDuplicateItState) {
    //  //--------
    //  val hazardIdMinusOne = rHazardId - 1
    //  //--------
    //  is (DuplicateItState.IDLE) {
    //    myUpRdValid := tempRdValid
    //    nextHazardId := 0x0
    //    setDoDuplicateIt(false)
    //    when (
    //      !tempRdValid
    //    ) {
    //      when (
    //        up.isFiring
    //        //mod.front.cLastFront.up.isFiring
    //      ) {
    //        nextDuplicateItState := (
    //          //DuplicateItState.START
    //          DuplicateItState.WAIT_CNT
    //        )
    //        nextHazardId := modStageCnt
    //        //duplicateIt()
    //        setDoDuplicateIt(true)
    //      }
    //    }
    //    //otherwise {
    //    //  //setRdMemWord()
    //    //  setDoDuplicateIt(false)
    //    //}
    //  }
    //  is (DuplicateItState.WAIT_CNT) {
    //    //--------
    //    // BEGIN: debug
    //    //when (down.isFiring) {
    //    //  nextHazardId := hazardIdMinusOne
    //    //  setDoDuplicateIt(false)
    //    //  rDuplicateItState := DuplicateItState.IDLE
    //    //}
    //    // END: debug
    //    //--------
    //    //setDoDuplicateIt(true)
    //    setDoDuplicateIt(true)
    //    when (
    //      up.isFiring
    //      //mod.front.cLastFront.up.isFiring
    //    ) {
    //      //nextHazardId := hazardIdMinusOne
    //      when (
    //        //hazardIdMinusOne =/= 0
    //        rHazardId =/= 0
    //      ) {
    //        nextHazardId := hazardIdMinusOne
    //        //when (up.isFiring) {
    //        //  nextHazardId := hazardIdMinusOne
    //        //}
    //        //duplicateIt()
    //        //nextHazardId := hazardIdMinusOne
    //        //setDoDuplicateIt(true)
    //      } otherwise { // when (/*hazardIdMinusOne*/ rHazardId === 0)
    //        //when (up.isFiring) {
    //          nextDuplicateItState := DuplicateItState.IDLE
    //          setDoDuplicateIt(false)
    //          //setRdMemWord()
    //          //nextHazardId
    //        //}
    //        //when (up.isValid) {
    //        //}
    //        //--------
    //        //myUpRdValid := True
    //        ////setDoDuplicateIt(false)
    //        //--------
    //      }
    //    } 
    //    //otherwise {
    //    //  setDoDuplicateIt(true)
    //    //}
    //  }
    //}
    upExt(1).rdMemWord := (
      RegNext(upExt(1).rdMemWord) init(upExt(1).rdMemWord.getZero)
    )
    //when (
    //  //(
    //  //  RegNextWhen(
    //  //    True, io.front.fire
    //  //  ) init(False)
    //  //) && (
    //  //  RegNextWhen(
    //  //    True, io.modFront.fire
    //  //  ) init(False)
    //  //) && (
    //  //  RegNextWhen(
    //  //    True, io.modBack.fire
    //  //  ) init(False)
    //  //  //RegNextWhen(
    //  //  //  True, /*io.back.fire*/ mod.back.cBack.isValid
    //  //  //) init(False)
    //  //) && 
    //  //(
    //  //  RegNextWhen(
    //  //    True, io.back.fire
    //  //  ) init(False)
    //  //)
    //  //True
    //  !clockDomain.isResetActive
    //  //&& up.isValid
    //  && up.isFiring
    //  && nextUpRdValid
    //  && (upExt(1).hazardId) === 0
    //) {
    //  when (
    //    backUpExt.rdValid
    //    //////&& mod.back.cBack.up.isValid
    //    ////&& mod.back.cBack.up.isFiring
    //    && (backUpExt.hazardId) === 0

    //    //&& mod.back.cBack.up.isValid
    //    && mod.back.cBack.up.isFiring
    //    && upExt(1).memAddr === backUpExt.memAddr
    //  ) {
    //    upExt(1).rdMemWord := backUpExt.modMemWord
    //  } otherwise {
    //    upExt(1).rdMemWord := modMem.readSync(
    //      address=upExt(1).memAddr
    //    )
    //  }
    //  //when (
    //  //  backUpExt.rdValid
    //  //  //&& mod.back.cBack.up.isValid
    //  //  && mod.back.cBack.up.isFiring
    //  //) {
    //  //  when (
    //  //    upExt(1).memAddr === backUpExt.memAddr
    //  //    //&& backUpExt.rdValid
    //  //    //&& mod.back.cBack.up.isValid
    //  //  ) {
    //  //    upExt(1).rdMemWord := backUpExt.modMemWord
    //  //  } otherwise {
    //  //    upExt(1).rdMemWord := modMem.readSync(
    //  //      address=upExt(1).memAddr
    //  //    )
    //  //  }
    //  //}
    //} 
    when (
      !clockDomain.isResetActive
      //&& up.isFiring
      && up.isValid
    ) {
      if (!forFmax) {
        when (
          //backUpExt.hazardId === 0
          backUpExt.hazardId.msb
          //&& mod.back.cBack.up.isFiring
          && mod.back.cBack.up.isValid
          && upExt(1).memAddr === backUpExt.memAddr
        ) {
          upExt(1).rdMemWord := backUpExt.modMemWord
        } otherwise {
          upExt(1).rdMemWord := modMem.readSync(
            address=upExt(1).memAddr
          )
        }
      } else { // if (forFmax)
        //when (
        //  !nextHazardId
        //)
        //when (nextDuplicateIt) {
        //  upExt(1).rdMemWord := upExt(1).rdMemWord.getZero
        //} otherwise {
          //--------
          // BEGIN: debug
          upExt(1).rdMemWord := modMem.readSync(
            address=upExt(1).memAddr,
          )
          // END: debug
          //--------
        //}
      }
    }

    //otherwise {
    //}
    //when (
    //  isValid
    //  //&& myUpRdValid
    //) {
    //  when (myUpRdValid) {
    //  } otherwise {
    //  }
    //  //when (
    //  //  //upExt.hazardId
    //  //  nextHazardId === back.cBack.up
    //  //) {
    //  //}
    //} otherwise {
    //  upExt.rdMemWord := (
    //    RegNext(upExt.rdMemWord) init(upExt.rdMemWord.getZero)
    //  )
    //}

    //when (
    //  !rDuplicateIt
    //  //!cSum.up(frontDuplicateIt)
    //  //!up(frontDuplicateIt)
    //) {
    //  myUpRdValid := tempRdValid
    //  when (!tempRdValid) {
    //    nextDuplicateIt := True
    //    duplicateIt()
    //  }
    //} otherwise {
    //  myUpRdValid := True
    //  when (up.isFiring) {
    //    nextDuplicateIt := False
    //  } 
    //}
    //--------
    tempUpMod(1) := tempUpMod(0)
    tempUpMod(1).allowOverride
    tempUpMod(1).setPipeMemRmwExt(
      inpExt=upExt(1),
      memArrIdx=memArrIdx,
    )
    up(mod.front.outpPipePayload) := tempUpMod(1)
    //--------
    //bypass(mod.front.inpPipePayload).allowOverride
    //bypass(mod.front.inpPipePayload) := tempUpMod(1)
    //--------
    val rIsFiringCnt = (debug) generate (
      Reg(UInt(8 bits)) init(0x0)
    )
    val rMyUpRdValidDelVec = (debug) generate Vec.fill(8)(
      Reg(Bool()) init(False)
    )
    //--------
    GenerationFlags.formal {
      //--------
      when (up.isFiring) {
        for (idx <- 0 until rMyUpRdValidDelVec.size) {
          def tempUpRdValid = rMyUpRdValidDelVec(idx)
          if (idx == 0) {
            tempUpRdValid := nextUpRdValid
          } else {
            tempUpRdValid := rMyUpRdValidDelVec(idx - 1)
          }
        }
      }
      //--------
      val myDbgMemReadSync = wordType()
      when (up.isValid) {
        myDbgMemReadSync := (
          modMem.readSync
          //mem.readAsync
          (
            //address=up(pipePayload.front).addr,
            address=upExt(1).memAddr
          )
        )
      } otherwise {
        myDbgMemReadSync := (
          RegNext(myDbgMemReadSync) init(myDbgMemReadSync.getZero)
        )
      }
      //up(pipePayload.dbgMemReadSync) := myDbgMemReadSync
      upExt(1).dbgMemReadSync := myDbgMemReadSync
      //--------
      when (up.isFiring) {
        rIsFiringCnt := rIsFiringCnt + 1
      }
      when (pastValidAfterReset) {
        ////when (
        ////  past(up.isFiring)
        ////) {
        ////  when (
        ////    past(cBack.up(pipePayload.front).addr)
        ////    === cSum.up(pipePayload.front).addr
        ////  ) {
        ////    assert(
        ////      past(cSum.up(pipePayload.rd))
        ////      === past(cBack.up(pipePayload.rd))
        ////    )
        ////  } otherwise {
        ////    assert(
        ////      cSum.up(pipePayload.rd) === past(
        ////        mem.readAsync(
        ////          address=down(pipePayload.front).addr
        ////        )
        ////      )
        ////    )
        ////  }
        ////}
        ////when (past(cSum.up.isFiring)) {
        ////  when (
        ////    cBack.up
        ////  ) {
        ////  } otherwise {
        ////  }
        ////}
        //val rPrevCSumFront = Reg(pipePayload.mkFront())
        //rPrevCSumFront.init(rPrevCSumFront.getZero)
        //val rPrevCBackFront = Reg(pipePayload.mkFront())
        //rPrevCBackFront.init(rPrevCBackFront.getZero)
        ////when (cSum.up.isFiring) {
        ////}
        when (
          RegNextWhen(True, io.back.fire) init(False)
        ) {
          when (
            //up.isFiring
            up.isValid
          ) {
            if (!forFmax) {
              when (
                backUpExt.hazardId.msb
                //&& mod.back.cBack.up.isFiring
                && mod.back.cBack.up.isValid
                && upExt(1).memAddr === backUpExt.memAddr
              ) {
                assert(
                  upExt(1).rdMemWord === backUpExt.modMemWord
                )
              } otherwise {
                assert(
                  upExt(1).rdMemWord === modMem.readSync(
                    address=upExt(1).memAddr
                  )
                )
              }
            } else { // if (forFmax)
              assert(
                upExt(1).rdMemWord === modMem.readSync(
                  address=upExt(1).memAddr,
                )
              )
            }
          } otherwise {
            assert(
              /*past*/(upExt(1).rdMemWord)
              === /*past*/(RegNext(upExt(1).rdMemWord))
            )
          }
          //when (
          //  //(
          //  //  RegNextWhen(True, io.back.fire) init(False)
          //  //)
          //  //past(up.isFiring)
          //  //&& past(myUpRdValid)
          //  //&& past(backUpExt.rdValid)
          //  //&& past(mod.back.cBack.up.isValid)
          //  /*&&*/ /*past*/(
          //    up.isFiring
          //  )
          //  //&& past(upExt.rdValid)
          //  //--------
          //  && /*past*/(
          //    nextUpRdValid
          //  )
          //  //--------
          //  && (
          //    (upExt(1).hazardId) === 0
          //  )
          //  //&& past(backUpExt.rdValid)
          //  //&& past(mod.back.cBack.up.isValid)
          //) {
          //  when (
          //  //&& past(backUpExt.rdValid)
          //  //&& past(mod.back.cBack.up.isValid)
          //    /*past*/(backUpExt.rdValid)
          //    && ((backUpExt.hazardId) === 0)
          //    && /*past*/(mod.back.cBack.up.isValid)
          //    && /*past*/(upExt(1).memAddr) === /*past*/(backUpExt.memAddr)
          //  ) {
          //    assert(
          //      /*past*/(upExt(1).rdMemWord)
          //      === /*past*/(backUpExt.modMemWord)
          //    )
          //  } otherwise {
          //    assert(
          //      //past(
          //      upExt(1).rdMemWord
          //      //)
          //      === (
          //        //RegNextWhen(
          //        //  modMem.readSync(
          //        //    address=(
          //        //      RegNextWhen(
          //        //        upExt.memAddr,
          //        //        up.isFiring
          //        //      ) init(upExt.memAddr.getZero)
          //        //    ),
          //        //  ), up.isFiring
          //        //) init(myDbgMemReadSync.getZero)
          //        //past(
          //        //  modMem.readSync(
          //        //    address=(
          //        //      past(upExt.memAddr)
          //        //    )
          //        //  )
          //        //)
          //        //past(
          //          modMem.readSync(
          //            address=(
          //              /*past*/(upExt(0).memAddr)
          //            )
          //          )
          //        //)
          //        //past(
          //        //mod.back.rTempWord
          //        //)
          //      )
          //    )
          //  }
          //} otherwise /*(
          //  //(
          //  //  RegNextWhen(True, io.back.fire) init(False)
          //  //) 
          //)*/ {
          //  assert(
          //    /*past*/(upExt(1).rdMemWord)
          //    === /*past*/(RegNext(upExt(1).rdMemWord))
          //  )
          //}
        }

        //when (
        //  //past(cSum.up.isFiring)
        //  //&& past(cBack.up.isFiring)
        //  //&& 
        //  //myUpRdValid
        //  //--------
        //  up.isValid
        //  && myUpRdValid
        //  && mod.back.cBack.up.isValid
        //  //--------

        //  //&& backUpExt.rdValid //cBack.up(rdValid)

        //  //&& (
        //  //  (RegNextWhen(True, io.front.fire) init(False))
        //  //  || io.front.fire
        //  //) && (
        //  //  (RegNextWhen(True, cFront.up.isFiring) init(False))
        //  //  || cFront.up.isFiring
        //  //) && (
        //  //  (RegNextWhen(True, cSum.up.isFiring) init(False))
        //  //  || cSum.up.isFiring
        //  //) && (
        //  //  (RegNextWhen(True, cBack.up.isFiring) init(False))
        //  //  || cBack.up.isFiring
        //  //) && (
        //  //  //(RegNextWhen(True, io.back.fire) init(False))
        //  //  //|| io.back.fire
        //  //  True
        //  //)
        //) {
        //  when (
        //    //cSum.up(pipePayload.front).addr
        //    //cFront.up(pipePayload.front).addr
        //    //=== cBack.up(pipePayload.front).addr
        //    upExt.memAddr === backUpExt.memAddr
        //  ) {
        //    assert(
        //      //cSum.up(pipePayload.rd)
        //      //=== cBack.up(pipePayload.back).sum
        //      //past(cFront.down(pipePayload.rd))
        //      //=== past(cSum.down(pipePayload.back).sum)
        //      (
        //        //RegNextWhen(
        //        //  cSum.up(pipePayload.rd), cSum.up.isFiring
        //        //) init(cSum.up(pipePayload.rd).getZero)
        //        //cBack.up(pipePayload.rd)
        //        //cSum.down(pipePayload.rd)
        //        //cFront.down(pipePayload.rd)
        //        upExt.rdMemWord
        //      )
        //      === (
        //        //cBack.up(pipePayload.back).sum
        //        backUpExt.modMemWord
        //      )
        //    )
        //  } otherwise {
        //    //assert(
        //    //  //cFront.down(pipePayload.rd)
        //    //  //=== cBack.up(pipePayload.dbgMemReadSync)
        //    //  cFront.down(pipePayload.rd)
        //    //  === myDbgMemReadSync
        //    //)
        //    //assert(
        //    //)
        //    //when (
        //    //  //RegNextWhen(
        //    //  //  True,
        //    //  //  up.isFiring,
        //    //  //) init(False)
        //    //  past(up.isFiring)
        //    //) {
        //    //  assert(
        //    //    upExt.rdMemWord
        //    //    === (
        //    //      //RegNextWhen(
        //    //      //  modMem.readSync(
        //    //      //    address=(
        //    //      //      RegNextWhen(
        //    //      //        upExt.memAddr,
        //    //      //        up.isFiring
        //    //      //      ) init(upExt.memAddr.getZero)
        //    //      //    ),
        //    //      //  ), up.isFiring
        //    //      //) init(myDbgMemReadSync.getZero)
        //    //      past(
        //    //        modMem.readSync(
        //    //          address=(
        //    //            past(upExt.memAddr)
        //    //          )
        //    //        )
        //    //      )
        //    //    )
        //    //  )
        //    //}
        //  }
        //}
        //cover({
        //  val rSameAddr = (
        //    RegNextWhen(
        //      True,
        //      upExt.memAddr === backUpExt.memAddr
        //    )
        //  )
        //})
        def myCoverFunc(
          //cond: Boolean,
          kind: Int,
        ): Bool = {
          val rSameAddrCnt = Reg(UInt(8 bits)) init(0x0)
          val rDiffAddrCnt = Reg(UInt(8 bits)) init(0x0)
          //val rSomeDuplicateItCnt = Reg(UInt(8 bits)) init(0x0)
          //val rUpNotFiringCnt = (cond) generate (
          //  Reg(UInt(8 bits)) init(0x0)
          //)
          val myModMemWordCond = backUpExt.modMemWord.asBits.asUInt > 0 
          when (
            myModMemWordCond
            //&& up.isValid
            ////&& mod.back.cBack.up.isValid
          ) {
            //when (
            //  kind match {
            //    case 0 => (
            //      doDuplicateIt
            //      && !past(doDuplicateIt)
            //    )
            //    case 1 => (
            //      doDuplicateIt
            //    )
            //    case 2 => (
            //      //doDuplicateIt
            //      True
            //    )
            //    case _ => (
            //      True
            //    )
            //  }
            //  //if (cond) (
            //  //  && !past(past(doDuplicateIt))
            //  //) else (
            //  //  !past(doDuplicateIt)
            //  //)
            //  //&& !past(past(doDuplicateIt))
            //) {
            //  rSomeDuplicateItCnt := rSomeDuplicateItCnt + 1
            //}
            when (
              up.isFiring
              //up.isValid
              //down.isFiring
            ) {
              //val rUpMemAddrDel1 = RegNextWhen(
              //  upExt(1).memAddr,
              //  up.isFiring,
              //) init(upExt(1).memAddr.getZero)
              //val rUpMemAddrDel2 = RegNextWhen(
              //  rUpMemAddrDel1,
              //  up.isFiring,
              //) init(rUpMemAddrDel1.getZero)
              when (
                //upExt.memAddr === backUpExt.memAddr
                //&& 
                //upExt(1).memAddr === rUpMemAddrDel(0)
                //&& upExt.memAddr === rUpMemAddrDel2
                upExt(1).memAddr
                //=== (
                //  RegNextWhen(upExt(1).memAddr, up.isFiring) init(0x0)
                //)
                === rUpMemAddrDel(0)
              ) {
                kind match {
                  case 0 => {
                    when (past(up.isFiring)) {
                      when (
                        rSameAddrCnt(0)
                      ) {
                        rSameAddrCnt := rSameAddrCnt + 1
                      }
                    } otherwise {
                      rSameAddrCnt := rSameAddrCnt + 1
                    }
                    //when (
                    //  past(up.isFiring)
                    //  && !past(past(up.isFiring))
                    //) {
                    //  rUpNotFiringCnt := rUpNotFiringCnt + 1
                    //}
                    //rSameAddrCnt := rSameAddrCnt + 1
                  }
                  case 1 | 2 | 3 => {
                    rSameAddrCnt := rSameAddrCnt + 1
                  }
                  //case 3 => {
                  //  when (
                  //    rUpMemAddrDel(0) =/= rUpMemAddrDel(1)
                  //  ) {
                  //    rSameAddrCnt := rSameAddrCnt + 1
                  //  }
                  //}
                  case _ => {
                  }
                }
              }
              //otherwise 
              when (
                upExt(1).memAddr =/= rUpMemAddrDel(0)
                && (
                  kind match {
                    case 0 => (
                      True
                    )
                    //&& upExt(1).memAddr
                    case 1 => (
                      True
                    )
                    case 2 => (
                      rUpMemAddrDel(0) =/= rUpMemAddrDel(1)
                      //&& rUpMemAddrDel(1) =/= rUpMemAddrDel(2)
                    )
                    case 3 => (
                      rUpMemAddrDel(0) =/= rUpMemAddrDel(1)
                      && rUpMemAddrDel(1) =/= rUpMemAddrDel(2)
                    )
                    case _ => (
                      True
                    )
                  }
                )
              ) {
                rDiffAddrCnt := rDiffAddrCnt + 1
              }
            }
          }
          (
            //(
            //  RegNextWhen(
            //    True,
            //  ) init(False)
            //) && (
            //  RegNextWhen(
            //    True,
            //    (
            //      upExt.memAddr =/= backUpExt.memAddr
            //      && backUpExt.modMemWord.asBits.asUInt > 0 
            //      && up.isFiring
            //      && mod.back.cBack.up.isFiring
            //    )
            //  ) init(False)
            //) 
            (
              rSameAddrCnt > 8
              //True
            ) && (
              rDiffAddrCnt > 8
              //True
            ) && (
              RegNextWhen(True, io.front.fire) init(False)
            ) && (
              RegNextWhen(True, io.modFront.fire) init(False)
            ) && (
              RegNextWhen(True, io.modBack.fire) init(False)
            ) && (
              RegNextWhen(True, io.back.fire) init(False)
            ) && (
              kind match {
                case 0 | 1 => (
                  //rSomeDuplicateItCnt > 4
                  True
                )
                case 2 => (
                  True
                )
                case 3 => (
                  True
                )
                case _ => (
                  True
                )
              }
            )
          )
        }
        //cover(myCoverFunc(kind=0))
        //cover(myCoverFunc(kind=1))

        //cover(myCoverFunc(kind=2))
        cover(myCoverFunc(kind=3))
        //cover(io.back.fire)
      }
    }
    //--------
  }
  val cBack = mod.back.cBack
  val cBackArea = new cBack.Area {
    haltWhen(
      !(RegNextWhen(True, io.front.fire) init(False))
    )
    val upExt = Vec.fill(2)(mkExt()).setName("cBackArea_upExt")
    upExt(1) := upExt(0)
    upExt(1).allowOverride
    val tempUpMod = modType().setName("cBackArea_tempUpMod")
    tempUpMod.allowOverride
    tempUpMod := up(mod.back.pipePayload)
    tempUpMod.getPipeMemRmwExt(
      outpExt=upExt(0),
      memArrIdx=memArrIdx,
    )
    val dbgDoWrite = (debug) generate (
      Bool().addAttribute("keep")
    )
    if (debug) {
      dbgDoWrite := False
    }
    when (
      !clockDomain.isResetActive
      //&& isValid
      && up.isFiring
      //&& up.isValid
      //&& upExt.rdValid
      //&& up.isValid
      //&& (upExt(0).hazardId) === 0
      && upExt(0).hazardId.msb
      //&& upExt(0).rdValid
    ) {
      if (debug) {
        mod.back.rTempWord := upExt(0).modMemWord
        dbgDoWrite := True
      }
      memWriteAll(
        address=upExt(0).memAddr,
        data=upExt(0).modMemWord,
      )
      //modMem.write(
      //  address=upExt(0).memAddr,
      //  data=upExt(0).modMemWord,
      //)
    }
    //--------
    //tempUpMod(1) := tempUpMod(0)
    //tempUpMod(1).setPipeMemRmwExt(
    //  ext=upExt,
    //  memArrIdx=memArrIdx,
    //)
    ////--------
    //bypass(mod.back.pipePayload) := tempUpMod(1)
    //--------
  }
  //--------
  //val multiRd = (io.optMultiRd) generate new Area {
  //  val front = new Area {
  //    val pipe = PipeHelper(linkArr=linkArr)
  //    val pipePayload = Payload(multiRdType())
  //  }
  //  val back = new Area {
  //    val pipe = PipeHelper(linkArr=linkArr)
  //    val pipePayload = Payload(multiRdType())
  //  }
  //}
  //--------
  //--------
  Builder(linkArr.toSeq)
  //--------
}
case class SamplePipeMemRmwModType[
  WordT <: Data,
](
  //wordWidth: Int,
  wordType: HardType[WordT],
  wordCount: Int,
  modStageCnt: Int,
) extends Bundle with PipeMemRmwPayloadBase[WordT] {
  //--------
  val myExt = PipeMemRmwPayloadExt(
    wordType=wordType(),
    wordCount=wordCount,
    modStageCnt=modStageCnt,
  )
  //--------
  def setPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[WordT],
    memArrIdx: Int,
  ): Unit = {
    myExt := ext
  }
  def getPipeMemRmwExt(
    ext: PipeMemRmwPayloadExt[WordT],
    memArrIdx: Int,
  ): Unit = {
    ext := myExt
  }
  //--------
}
object PipeMemRmwToVerilog extends App {
  //--------
  def wordWidth = 8
  def wordType() = UInt(wordWidth bits)
  def wordCount = 4
  def modStageCnt = 1
  //--------
  Config.spinal.generateVerilog(
    PipeMemRmw[
      UInt,
      SamplePipeMemRmwModType[UInt],
      PipeMemRmwMultiRdTypeDisabled[UInt],
    ](
      wordType=wordType(),
      wordCount=wordCount,
      modType=SamplePipeMemRmwModType[UInt](
        wordType=wordType(),
        wordCount=wordCount,
        modStageCnt=modStageCnt,
      ),
      modStageCnt=modStageCnt,
    )
  )
  //--------
}
//--------

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
  //val dbgMemReadSync = (debug) generate PipeMemTest.wordType()
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
    val dbgMemReadSync = (debug) generate Payload(PipeMemTest.wordType())
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
  val cLast = pipe.addStage(
    name="Last",
    finish=true,
  )

  val rdValid = (
    //Array.fill(cArr.size - 1)(Payload(Bool()))
    Payload(Bool())
  )
  val frontDuplicateIt = (
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
      val myUpRdValid = Bool()
      val rPrevMyUpRdValid = (
        //RegNextWhen(myUpRdValid, up.isFiring) init(False)
        RegNext(myUpRdValid) init(False)
      )
      //--------
      //val nextFlagUpRdValid = Bool()
      //val rFlagUpRdValid = RegNext(nextFlagUpRdValid) init(False)
      //--------
      //val rUpRdValid = (
      //  //RegNextWhen(myUpRdValid, up.isFiring) init(False)
      //)
      //val rSavedUpRdValid = Reg(Bool()) init(True)
      //val rPastUpRdValid = RegNext(upRdValid) init(True)
      //haltWhen(!downRdValid)
      //terminateWhen(!downRdValid)

      //upRdValid := rPastUpRdValid
      //upRdValid := RegNext(upRdValid) init(True)
      //when (isValid) {
      //  myUpRdValid := tempRdValid
      //} otherwise {
      //  upRdValid := RegNext(upRdValid) init(upRdValid.getZero)
      //}
      myUpRdValid := rPrevMyUpRdValid
      //--------
      //when (isValid) {
      //  when (tempRdValid) {
      //    myUpRdValid := True
      //    //nextFlagUpRdValid := False
      //  } otherwise { // when (!tempRdValid)
      //    //myUpRdValid
      //    //val rSavedTempRdValid = (
      //    //  RegNextWhen(tempRdValid, up.isFiring) init(False)
      //    //)
      //    //when (up.isFiring) {
      //    //}
      //    //--------
      //    //when (
      //    //  //up.isFiring
      //    //  //up.isReady
      //    //  up.isReady
      //    //  //down.isFiring
      //    //) {
      //    //  //when (!rFlagUpRdValid) {
      //    //  //  rFlagUpRdValid := True
      //    //  //  //myUpRdValid := 
      //    //  //}
      //    //  //myUpRdValid := True
      //    //  nextFlagUpRdValid := !rFlagUpRdValid
      //    //  myUpRdValid := nextFlagUpRdValid
      //    //} otherwise { // when (!up.isFiring)
      //    //  nextFlagUpRdValid := rFlagUpRdValid
      //    //  myUpRdValid := RegNext(myUpRdValid) init(myUpRdValid.getZero)
      //    //}
      //    when (
      //      //cSum.up.isFiring
      //      up.isFiring
      //    ) {
      //      //--------
      //      //nextFlagUpRdValid := True
      //      //myUpRdValid := True
      //      //--------
      //      //myUpRdValid := False
      //      //--------
      //      //when (!rPrevMyUpRdValid) {
      //      //  myUpRdValid
      //      //}
      //      myUpRdValid := !rPrevMyUpRdValid
      //    }
      //    //otherwise {
      //    //  myUpRdValid := rPrevMyUpRdValid
      //    //}
      //    //when (rPrevMyUpRdValid) {
      //      //duplicateIt()
      //    //}
      //    //--------
      //  }
      //}
      //--------
      //otherwise {
      //  myUpRdValid := rPrevMyUpRdValid
      //}

      when (isValid) {
        up(rdValid) := (
          //tempRdValid
          //myUpRdValid
          //rPrevMyUpRdValid
          myUpRdValid
        )
      } otherwise {
        up(rdValid) := rPrevMyUpRdValid
      }
      //val rDuplicateIt = Reg(Bool()) init(False)
      //when (
      //  //!up(rdValid)
      //  //!rPrevMyUpRdValid
      //  !myUpRdValid
      //) {
      //  when (up.isFiring) {
      //    rDuplicateIt := True
      //    //duplicateIt()
      //  }
      //  when (rDuplicateIt) {
      //    duplicateIt()
      //    rDuplicateIt := False
      //  }
      //  //when (!down.isFiring) {
      //  //  duplicateIt()
      //  //}
      //}
      //--------
      //val rPrevTempRdValid = (
      //  RegNextWhen(tempRdValid, up.isFiring) init(tempRdValid.getZero)
      //)
      //val prevTempRdValidAddrs = new Area {
      //  def mkAddr() = (
      //    UInt(log2Up(wordCount) bits)
      //  )
      //  val rUpAddr = Reg(mkAddr()) init(0x0)
      //  val rSumUpAddr = Reg(mkAddr()) init(0x0)
      //}

      val nextDuplicateIt = Bool()
      val rDuplicateIt = (
        RegNext(nextDuplicateIt) init(nextDuplicateIt.getZero)
      )
      nextDuplicateIt := rDuplicateIt
      //up(frontDuplicateIt) := nextDuplicateIt

      //when (up.isValid) {
      //  //when (
      //  //  tempRdValid
      //  //  //&& rPrevTempRdValid
      //  //  && !rDidDuplicateIt
      //  //) {
      //  //  duplicateIt()
      //  //  rDidDuplicateIt := True
      //  //} otherwise {
      //  //}
      //}
      //when (isValid) {
        //--------
        when (
          !rDuplicateIt
          //!cSum.up(frontDuplicateIt)
          //!up(frontDuplicateIt)
        ) {
          myUpRdValid := tempRdValid
          when (!tempRdValid) {
            //nextDuplicateIt := True
            nextDuplicateIt := True
            duplicateIt()
          }
        } otherwise {
          //nextDuplicateIt := False
          //myUpRdValid := False
          //duplicateIt()
          myUpRdValid := True
          when (up.isFiring) {
            nextDuplicateIt := False
            //nextDuplicateIt := False
            //myUpRdValid := False
            //myUpRdValid := True
            //myUpRdValid := True
          } 
          //otherwise {
          //  //myUpRdValid := False
          //}
          //otherwise {
          //  duplicateIt()
          //}
        }
        //--------
      //}
      //--------

      //duplicateWhen(
      //  //!cSum.up(rdValid)
      //  //!up(rdValid)
      //  //!down(rdValid)
      //  //!rPastUpRdValid
      //  //!upRdValid
      //  //!down(rdValid)
      //  //!cSum.up(rdValid)
      //  //!down(rdValid)
      //  //!rSavedUpRdValid
      //  //!down(rdValid)
      //  //!up.isFiring
      //  !upRdValid
      //)
      //--------
      //when (!myUpRdValid) {
      //  //when (
      //  //  //!down.isReady
      //  //  //!RegNext(down.isFiring)
      //  //  //!down.isFiring
      //  //  //RegNext(up.isFiring)
      //  //  True
      //  //) {
      //    duplicateIt()
      //  //}
      //}
      //--------
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
      val upRd = pipePayload.mkRd()
      when (
        isValid
        && myUpRdValid
      ) {
        when (
          //cBack.up.isFiring
          //&& (
            cBack.up(pipePayload.front).addr
            === up(pipePayload.front).addr
          //)
        ) {
          upRd := (
            cBack.up(pipePayload.back).sum
          )
        } otherwise {
          upRd := (
            mem.readSync
            //mem.readAsync
            (
              address=up(pipePayload.front).addr,
            )
          )
        }
      } otherwise {
        upRd := RegNext(upRd) init(upRd.getZero)
      }
      up(pipePayload.rd) := upRd
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
            myUpRdValid
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
    val rMyUpRdValidDelVec = (debug) generate Vec.fill(8)(
      Reg(Bool()) init(False)
    )
    //--------
    GenerationFlags.formal {
      when (up.isFiring) {
        for (idx <- 0 until rMyUpRdValidDelVec.size) {
          def tempUpRdValid = rMyUpRdValidDelVec(idx)
          if (idx == 0) {
            tempUpRdValid := myUpRdValid
          } else {
            tempUpRdValid := rMyUpRdValidDelVec(idx - 1)
          }
        }
      }
      //val rMyUpRdValidDel1 = (
      //  RegNextWhen(myUpRdValid, up.isFiring)
      //  init(myUpRdValid.getZero)
      //)
      //val rMyUpRdValidDel2 = (
      //  RegNextWhen(rMyUpRdValidDel1, up.isFiring)
      //  init(myUpRdValid.getZero)
      //)
      //val rMyUpRdValidDel3 = (
      //  RegNextWhen(rMyUpRdValidDel2, up.isFiring)
      //  init(myUpRdValid.getZero)
      //)
      //cover(
      //  (
      //    RegNextWhen(True, io.back.fire) init(False)
      //  )
      //  //&& myUpRdValid
      //  //&& !rMyUpRdValidDel1
      //  //&& rMyUpRdValidDel2
      //  //&& !rMyUpRdValidDel1
      //)
      //cover(
      //  !(
      //    RegNextWhen(myUpRdValid, up.isFiring) init(myUpRdValid.getZero)
      //  )
      //  && myUpRdValid
      //  && !rMyUpRdValidDelVec(0)
      //  && rMyUpRdValidDelVec(1)
      //  && !rMyUpRdValidDelVec(2)
      //  && rMyUpRdValidDelVec(3)
      //  && !rMyUpRdValidDelVec(4)
      //)
      val myDbgMemReadSync = PipeMemTest.wordType()
      when (up.isValid) {
        myDbgMemReadSync := (
          mem.readSync
          //mem.readAsync
          (
            address=up(pipePayload.front).addr,
          )
        )
      } otherwise {
        myDbgMemReadSync := (
          RegNext(myDbgMemReadSync) init(myDbgMemReadSync.getZero)
        )
      }
      up(pipePayload.dbgMemReadSync) := myDbgMemReadSync
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
          //myUpRdValid
          up.isValid
          && myUpRdValid
          && cBack.up.isValid
          && cBack.up(rdValid)
          //&& (
          //  (RegNextWhen(True, io.front.fire) init(False))
          //  || io.front.fire
          //) && (
          //  (RegNextWhen(True, cFront.up.isFiring) init(False))
          //  || cFront.up.isFiring
          //) && (
          //  (RegNextWhen(True, cSum.up.isFiring) init(False))
          //  || cSum.up.isFiring
          //) && (
          //  (RegNextWhen(True, cBack.up.isFiring) init(False))
          //  || cBack.up.isFiring
          //) && (
          //  //(RegNextWhen(True, io.back.fire) init(False))
          //  //|| io.back.fire
          //  True
          //)
        ) {
          when (
            //cSum.up(pipePayload.front).addr
            cFront.up(pipePayload.front).addr
            === cBack.up(pipePayload.front).addr
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
          } otherwise {
            //assert(
            //  //cFront.down(pipePayload.rd)
            //  //=== cBack.up(pipePayload.dbgMemReadSync)
            //  cFront.down(pipePayload.rd)
            //  === myDbgMemReadSync
            //)
          }
        }
      }
      //cover(
      //  //up.isFiring
      //  //&& 
      //  //rTempCnt === 1
      //  //rIsFiringCnt === 2
      //  rIsFiringCnt >= 5
      //  && rTempCnt >= 5
      //  //&& rTempCnt > 3
      //)
    }
    //--------
  }
  //--------
  val cSumArea = new cSum.Area {
    //--------
    //haltWhen(
    //  !(RegNextWhen(True, io.front.fire) init(False))
    //)
    //val temp = up(rdValid)

    terminateWhen(
      //!pipe.nArr(1)(rdValid)
      //temp
      !up(rdValid)
    )
    //--------
    //val rDuplicateIt = Reg(Bool()) init(False)
    //when (
    //  !up(rdValid)
    //) {
    //  when (down.isFiring) {
    //    rDuplicateIt := True
    //    //duplicateIt()
    //  }
    //  when (rDuplicateIt) {
    //    duplicateIt()
    //    rDuplicateIt := False
    //  }
    //  //when (!down.isFiring) {
    //  //  duplicateIt()
    //  //}
    //}
    ////duplicateWhen(
    ////  !up(rdValid)
    ////  && 
    ////)
    //--------

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
    //def upRdValid = up(rdValid)

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
      && up(rdValid)
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
    val rDidFirstFire = (debug) generate (
      RegNextWhen(True, up.isFiring) init(False)
    )
    //val rPastIsFiring = Reg(Bool()) init(False)

    //rPastIsFiring := down.isFiring

    //val rPrevUpFront = (
    //  Reg(pipePayload.mkFront()) init(pipePayload.mkFront().getZero)
    //)
    //val rPrevDownFront = (
    //  Reg(pipePayload.mkFront()) init(pipePayload.mkFront().getZero)
    //)
    //val rPrevUpRd = (
    //  Reg(pipePayload.mkRd()) init(pipePayload.mkRd().getZero)
    //)
    //val rPrevDownRd = (
    //  Reg(pipePayload.mkRd()) init(pipePayload.mkRd().getZero)
    //)
    //val rPrevUpBack = (
    //  Reg(pipePayload.mkBack()) init(pipePayload.mkBack().getZero)
    //)
    ////val rPrevDownBack = (
    ////  Reg(pipePayload.mkBack()) init(pipePayload.mkBack().getZero)
    ////)
    ////val rPrevDownRdValid = (
    ////  Reg(Bool()) init(downRdValid.getZero)
    ////)
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
            && up(rdValid)
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
    val rCoverDiffData = (debug) generate Vec.fill(8)(
      Reg(PipeMemTest.wordType()) init(0x0)
    )
    val rCoverDiffDataCnt = (debug) generate (
      Reg(UInt(8 bits)) init(0x0)
    )
    val rCoverAddr = (debug) generate (
      Vec.fill(4.min(wordCount))(
        Reg(UInt(log2Up(wordCount) bits)) init(0x0)
      )
    )
    val rCoverAddrCnt = (debug) generate (
      Reg(UInt(8 bits)) init(0x0)
    )
    val rCoverAddrLeastCnt = (debug) generate (
      Reg(UInt(8 bits)) init(0x0)
    )
    //val rCoverAddrLastIdx = Reg(
    //  UInt(log2Up(rCoverAddr.size) bits) init(0x0)
    //) 
    //val rCoverInvIsFiring = Reg(Bool()) init(False)
    //val rCoverInvCnt = Reg(UInt(8 bits)) init(0x0)
    //val rCoverSameIsFiring = Reg(Bool()) init(False)
    //val rCoverSameCnt = Reg(UInt(8 bits)) init(0x0)

    //--------
    //case class DbgUp() extends Bundle {
    //}
    val rUpRdValidDelVec = (debug) generate (
      Vec.fill(8)(
        Reg(Bool()) init(False)
      )
    )
    //--------
    val myHadFlip = (debug) generate (
      RegNextWhen(
        True,
        (
          up(rdValid)
          && !rUpRdValidDelVec(0)
          && rUpRdValidDelVec(1)
          && !rUpRdValidDelVec(2)
          && rUpRdValidDelVec(3)
        )
      ) init(False)
    )

    GenerationFlags.formal {
      //--------
      when (up.isFiring) {
        for (idx <- 0 until rUpRdValidDelVec.size) {
          def tempUpRdValid = rUpRdValidDelVec(idx)
          if (idx == 0) {
            tempUpRdValid := up(rdValid)
          } else {
            tempUpRdValid := rUpRdValidDelVec(idx - 1)
          }
        }
      }
      //--------
      when (pastValidAfterReset) {
        //when (
        //  past(pastValidAfterReset)
        //) {
          //--------
          //when (up.isFiring) {
          //  rCoverInvIsFiring := True
          //  rCoverSameIsFiring := True
          //}
          //--------
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
          when (
            up.isFiring
            && rCoverAddrCnt < rCoverAddr.size
          ) {
            //--------
            val leastPlusOne = rCoverAddrLeastCnt + 1
            //--------
            //val firstSame = rCoverAddr.sFindFirst(
            //  _ === up(pipePayload.front).addr
            //)
            //when (!firstSame._1) {
            //  rCoverAddr(rCoverAddrCnt.resized) := (
            //    up(pipePayload.front).addr
            //  )
            //  rCoverAddrCnt := rCoverAddrCnt + 1
            //}
            //--------
            when (!leastPlusOne(1)) {
              //--------
              when (
                rCoverAddrCnt === 0
                && leastPlusOne === 0
              ) {
                rCoverAddrLeastCnt := leastPlusOne
                rCoverAddr(rCoverAddrCnt.resized) := (
                  up(pipePayload.front).addr
                )
              } elsewhen (
                rCoverAddr(rCoverAddrCnt.resized)
                === up(pipePayload.front).addr
              ) {
                rCoverAddrLeastCnt := leastPlusOne
              } otherwise {
              }
              //--------
            } otherwise {
              //--------
              rCoverAddrLeastCnt := 0
              //--------
              rCoverAddrCnt := rCoverAddrCnt + 1
              //--------
              rCoverAddr((rCoverAddrCnt + 1).resized) := (
                up(pipePayload.front).addr
              )
            }
            //--------
          }
          cover(
            rCoverDiffDataCnt === rCoverDiffData.size
            && myHadFlip
            //&& rCoverAddrCnt === rCoverAddr.size
            //rCoverDiffDataCnt === 1
          )
          cover(
            rCoverDiffDataCnt === rCoverDiffData.size
            && rCoverAddrCnt === rCoverAddr.size
            && myHadFlip
            //--------
            //up(rdValid)
            //&& !rUpRdValidDelVec(0)
            //&& rUpRdValidDelVec(1)
            //&& !rUpRdValidDelVec(2)
            //&& rUpRdValidDelVec(3)
            //--------
            //&& (RegNextWhen(True, up(rdValid)) init(False))
            //&& (RegNextWhen(True, !rUpRdValidDelVec(0)) init(False))
            //&& (RegNextWhen(True, rUpRdValidDelVec(1)) init(False))
            //&& (RegNextWhen(True, !rUpRdValidDelVec(2)) init(False))
            //&& (RegNextWhen(True, rUpRdValidDelVec(3)) init(False))
            //rCoverDiffDataCnt === 1
          )
          //--------
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
    //--------
  }
  //--------
  val cBackArea = new cBack.Area {
    haltWhen(
      !(RegNextWhen(True, io.front.fire) init(False))
    )
    //throwWhen(
    //  //!pipe.nArr(1)(rdValid)
    //  //temp
    //  !up(rdValid)
    //)
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

    when (
      isValid
      && up(rdValid)
    ) {
      //def front = pipePayload.front
      //def rd = pipePayload.rd
      //def back = pipePayload.back
      mem.write(
        address=up(pipePayload.front).addr,
        data=up(pipePayload.back).sum,
      )
    }
    //val rCoverInvIsFiringCnt = Reg(UInt(8 bits)) init(0x0)
    //val rCoverDiffData = Vec.fill(8)(
    //  Reg(PipeMemTest.wordType()) init(0x0)
    //)
    //val rCoverDiffDataCnt = Reg(UInt(8 bits)) init(0x0)
    //val rCoverAddr = Vec.fill(4.min(wordCount))(
    //  Reg(UInt(log2Up(wordCount) bits)) init(0x0)
    //)
    //val rCoverAddrCnt = Reg(UInt(8 bits)) init(0x0)
    //val rCoverAddrLeastCnt = Reg(UInt(8 bits)) init(0x0)
    ////val rCoverAddrLastIdx = Reg(
    ////  UInt(log2Up(rCoverAddr.size) bits) init(0x0)
    ////) 
    ////val rCoverInvIsFiring = Reg(Bool()) init(False)
    ////val rCoverInvCnt = Reg(UInt(8 bits)) init(0x0)
    ////val rCoverSameIsFiring = Reg(Bool()) init(False)
    ////val rCoverSameCnt = Reg(UInt(8 bits)) init(0x0)

    ////--------
    //val rUpRdValidDelVec = Vec.fill(8)(
    //  Reg(Bool()) init(False)
    //)
    //when (up.isFiring) {
    //  for (idx <- 0 until rUpRdValidDelVec.size) {
    //    def tempUpRdValid = rUpRdValidDelVec(idx)
    //    if (idx == 0) {
    //      tempUpRdValid := up(rdValid)
    //    } else {
    //      tempUpRdValid := rUpRdValidDelVec(idx - 1)
    //    }
    //  }
    //}
    ////--------

    //GenerationFlags.formal {
    //  when (pastValidAfterReset) {
    //    //when (
    //    //  past(pastValidAfterReset)
    //    //) {
    //      //--------
    //      //when (up.isFiring) {
    //      //  rCoverInvIsFiring := True
    //      //  rCoverSameIsFiring := True
    //      //}
    //      //--------
    //      //when (
    //      //  down(rdValid)
    //      //  && !past(down(rdValid))
    //      //) {
    //      //  //when (down.isFiring /*|| rCoverInvIsFiring*/) {
    //      //    //cover(past(down(rdValid)))
    //      //    rCoverInvCnt := rCoverInvCnt + 1
    //      //    //rCoverInvIsFiring := False
    //      //  //}
    //      //}
    //      when (
    //        up.isFiring
    //        //&& 
    //        //io.back.valid
    //        //up.valid
    //        && rCoverDiffDataCnt < rCoverDiffData.size
    //      ) {
    //        val firstSame = rCoverDiffData.sFindFirst(
    //          _ === up(pipePayload.front).data
    //        )
    //        when (!firstSame._1) {
    //          rCoverDiffData(rCoverDiffDataCnt.resized) := (
    //            up(pipePayload.front).data
    //          )
    //          rCoverDiffDataCnt := rCoverDiffDataCnt + 1
    //        }
    //      }
    //      when (
    //        up.isFiring
    //        && rCoverAddrCnt < rCoverAddr.size
    //      ) {
    //        val leastPlusOne = rCoverAddrLeastCnt + 1

    //        //val firstSame = rCoverAddr.sFindFirst(
    //        //  _ === up(pipePayload.front).addr
    //        //)
    //        //when (!firstSame._1) {
    //        //  rCoverAddr(rCoverAddrCnt.resized) := (
    //        //    up(pipePayload.front).addr
    //        //  )
    //        //  rCoverAddrCnt := rCoverAddrCnt + 1
    //        //}
    //        when (!leastPlusOne(1)) {
    //          //--------
    //          when (
    //            rCoverAddrCnt === 0
    //            && leastPlusOne === 0
    //          ) {
    //            rCoverAddrLeastCnt := leastPlusOne
    //            rCoverAddr(rCoverAddrCnt.resized) := (
    //              up(pipePayload.front).addr
    //            )
    //          } elsewhen (
    //            rCoverAddr(rCoverAddrCnt.resized)
    //            === up(pipePayload.front).addr
    //          ) {
    //            rCoverAddrLeastCnt := leastPlusOne
    //          }
    //          //--------
    //        } otherwise {
    //          //--------
    //          rCoverAddrLeastCnt := 0
    //          //--------
    //          rCoverAddrCnt := rCoverAddrCnt + 1
    //          //--------
    //          rCoverAddr((rCoverAddrCnt + 1).resized) := (
    //            up(pipePayload.front).addr
    //          )
    //        }
    //      }
    //      cover(
    //        rCoverDiffDataCnt === rCoverDiffData.size
    //        //&& rCoverAddrCnt === rCoverAddr.size
    //        //rCoverDiffDataCnt === 1
    //      )
    //      //val rHadFlip = RegNext(
    //      //  up(rdValid)
    //      //  && !rUpRdValidDelVec(0)
    //      //  && rUpRdValidDelVec(1)
    //      //  && !rUpRdValidDelVec(2)
    //      //  && rUpRdValidDelVec(3)
    //      //) init(False)
    //      cover(
    //        rCoverDiffDataCnt === rCoverDiffData.size
    //        && rCoverAddrCnt === rCoverAddr.size
    //        //&& rHadFlip
    //        //--------
    //        //up(rdValid)
    //        //&& !rUpRdValidDelVec(0)
    //        //&& rUpRdValidDelVec(1)
    //        //&& !rUpRdValidDelVec(2)
    //        //&& rUpRdValidDelVec(3)
    //        //--------
    //        //&& (RegNextWhen(True, up(rdValid)) init(False))
    //        //&& (RegNextWhen(True, !rUpRdValidDelVec(0)) init(False))
    //        //&& (RegNextWhen(True, rUpRdValidDelVec(1)) init(False))
    //        //&& (RegNextWhen(True, !rUpRdValidDelVec(2)) init(False))
    //        //&& (RegNextWhen(True, rUpRdValidDelVec(3)) init(False))
    //        //rCoverDiffDataCnt === 1
    //      )
    //      //val rHadNonZeroData = Vec.fill(2){
    //      //  val temp = Reg(Flow(PipeMemTest.wordType()))
    //      //  temp.init(temp.getZero)
    //      //  temp
    //      //}

    //      //when (
    //      //  io.front.fire
    //      //  && (
    //      //    io.front.data =/= 0
    //      //  )
    //      //) {
    //      //  rHadNonZeroData.valid := True
    //      //  rHadNonZeroData.payload := io.front.data
    //      //}
    //      //cover(
    //      //  rHadNonZeroData.valid
    //      //  //&& io.back.valid
    //      //  //&& io.back.sum === rHadNonZeroData.payload
    //      //  && up.valid
    //      //  && up(pipePayload.front).data === rHadNonZeroData.payload
    //      //)

    //      //when (
    //      //  up(rdValid)
    //      //  //&& past(up(rdValid))
    //      //  //&& stable(up(pipePayload.front).addr)
    //      //  //&& !stable(up(pipePayload.front).data)
    //      //) {
    //      //  //when (up.isFiring /*|| rCoverSameIsFiring*/) {
    //      //    rCoverSameCnt := rCoverSameCnt + 1
    //      //  //}
    //      //}
    //      //cover(rCoverInvCnt === 3)
    //      //cover(rCoverSameCnt === 3)
    //      //cover(
    //      //  //rCoverInvCnt === 3
    //      //  //&& 
    //      //  rCoverSameCnt === 3
    //      //)
    //      //cover(!stable(up(rdValid)))
    //      //cover(!up(rdValid))
    //      //cover(up(rdValid))
    //    //}
    //  }
    //}
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
