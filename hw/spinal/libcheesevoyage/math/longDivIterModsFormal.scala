//package libcheesevoyage
//
//import spinal.core._
//import spinal.core.formal._
//
//object LongUdivIterFormal extends App {
//  val params = LongDivParams(
//    mainWidth=4,
//    denomWidth=4,
//    chunkWidth=2,
//    tagWidth=8,
//    pipelined=false,
//    usePipeSkidBuf=false,
//  )
//  new SpinalFormalConfig(_keepDebugInfo=true)
//    .withBMC(10)
//    .withProve(10)
//    .doVerify(new Component {
//      val dut = FormalDut(LongUdivIter(params=params))
//      val itdIn = dut.io.itdIn
//      val chunkStart = dut.io.chunkStart
//      val itdOut = dut.io.itdOut
//
//      assumeInitial(clockDomain.isResetActive)
//      //when (clockDomain.isResetActive) {
//      //assumeInitial(itdIn === itdIn.getZero)
//      assumeInitial(itdIn.tempNumer === itdIn.tempNumer.getZero)
//      assumeInitial(itdIn.tempDenom === 1)
//      //assumeInitial(
//      //  itdIn.tempQuot === (itdIn.tempNumer / itdIn.tempDenom))
//      //assumeInitial(
//      //  itdIn.tempRema === (itdIn.tempNumer % itdIn.tempDenom)
//      //)
//      for (idx <- 0 to params.radix - 1) {
//        //val tempIdx: BigInt = idx
//        itdIn.denomMultLut(idx) := (
//          itdIn.tempDenom * idx
//        ).resized
//      }
//      assumeInitial(chunkStart === chunkStart.getZero)
//      itdIn.formal.oracleQuot := (itdIn.tempNumer / itdIn.tempDenom)
//      itdIn.formal.oracleRema := (itdIn.tempNumer % itdIn.tempDenom)
//        //assume(itdIn === itdIn.getZero)
//        //assume(chunkStart === chunkStart.getZero)
//      //} 
//      //otherwise 
//      //when (~clockDomain.isResetActive) {
//        //anyseq(itdIn)
//        anyseq(itdIn.tempNumer)
//        anyseq(itdIn.tempDenom)
//        //anyseq(itdIn.tempQuot)
//        //anyseq(itdIn.tempRema)
//        //anyseq(itdIn.formal)
//        //anyseq(itdIn.denomMultLut)
//        //anyseq(itdIn.formal)
//        //for (idx <- 0 to params.radix - 1) {
//        //  itdIn.denomMultLut
//        //}
//        anyseq(chunkStart)
//      //}
//    })
//}
