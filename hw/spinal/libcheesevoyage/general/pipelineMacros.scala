//package libcheesevoyage.general
//import libcheesevoyage._
//
//import spinal.core._
//import spinal.lib._
//import spinal.core.formal._
//import scala.collection.mutable.ArrayBuffer
//import scala.language.experimental.macros
//import scala.reflect.macros.blackbox
//
//case class DualPipeFuncMostArgs[
//  PipeElemT <: Data,
//](
//  pipeIn: Vec[PipeElemT],
//  pipeOut: Vec[PipeElemT],
//  pipeStageIdx: Int,
//  pipeNumMainStages: Int,
//  pipeFieldName: String,
//  ////idx: Int,
//  ////setOutToPast: Option[Boolean],
//  //setOutToPast: Boolean=false,
//  ////rPastPipeOut: 
//) {
//  //def idxLtStageIdxMacro(
//  //  c: blackbox.Context,
//  //  //idx: Int,
//  //)(
//  //  number: c.Expr[Int]
//  //): c.Expr[String] = {
//  //  import c.universe._
//
//  //  val Literal(Constant(s_number: Int)) = number.tree
//  //  val result = s_number % 2 match {
//  //    case 0 => Literal(Constant("even"))
//  //    case _ => Literal(Constant("odd"))
//  //  }
//  //  c.Expr[String](result)
//  //}
//}
//
//object DualPipeFuncMostArgs {
//  //def craftStmFlowArgs[
//  //  PipeElemT <: Data
//  //](
//  //  mostArgs: DualPipeFuncMostArgs[PipeElemT],
//  //  //idx: Int,
//  //  setOutToPast: Boolean,
//  //): DualPipeFuncMostArgs[PipeElemT] = (
//  //  DualPipeFuncMostArgs[PipeElemT](
//  //    pipeIn=mostArgs.pipeIn,
//  //    pipeOut=mostArgs.pipeOut,
//  //    pipeStageIdx=mostArgs.pipeStageIdx,
//  //    pipeNumMainStages=mostArgs.pipeNumMainStages,
//  //    //idx=mostArgs.idx,
//  //    //idx=idx,
//  //    //setOutToPast=setOutToPast,
//  //  )
//  //)
//  //def getPipeElemT[PipeElemT <: Data](obj: PipeElemT): String = macro getTypeImplRef[PipeElemT]
//  //def idxLtStageIdxHelperMacro[PipeElemT]
//  
//
//  //def nonWorkStageImpl[
//  //  PipeElemT: c.WeakTypeTag
//  //](
//  //  c: blackbox.Context,
//  //)(
//  //  //self: c.Val[PipeElemT],
//  //  self: c.Expr[DualPipeFuncMostArgs[PipeElemT]],
//  //  pipeFieldName: c.Expr[String],
//  //  idx: c.Expr[Int],
//  //): c.Expr[Unit] = {
//  //  import c.universe._
//
//  //  //val Literal(Constant(s_self: DualPipeFuncMostArgs[PipeElemT])) = (
//  //  //  self.tree
//  //  //)
//
//  //  val pipeFieldTermName: TermName = pipeFieldName match {
//  //    case Literal(Constant(s: String)) => TermName(s)
//  //    case _ => c.abort(c.enclosingPosition, "Not a string literal")
//  //  }
//  //  // do a copy
//  //  c.Expr[Unit](
//  //    q"""
//  //    $self.pipeOut($idx).$pipeFieldTermName \
//  //    := (
//  //      $self.pipeIn($idx).$pipeFieldTermName 
//  //    );
//  //    """
//  //  )
//  //}
//
//  //def nonWorkStageFunc[
//  //  PipeElemT
//  //](
//  //  self: DualPipeFuncMostArgs[PipeElemT],
//  //  pipeFieldName: String,
//  //  idx: Int,
//  //): Unit = macro nonWorkStageImpl[PipeElemT]
//  def nonWorkStageImpl[
//    //WeakPipeElemT: c.WeakTypeTag
//    //PipeElemT: c.WeakTypeTag
//    PipeElemT <: Data
//  ](
//    c: blackbox.Context,
//  )(
//    //self: c.Val[PipeElemT],
//    self: c.Expr[DualPipeFuncMostArgs[PipeElemT]],
//    pipeFieldName: c.Expr[String],
//    idx: c.Expr[Int],
//  ): c.Expr[Unit] = {
//    import c.universe._
//
//    //val Literal(Constant(s_self: DualPipeFuncMostArgs[PipeElemT])) = (
//    //  self.tree
//    //)
//
//    val pipeFieldTermName: TermName = pipeFieldName match {
//      case Literal(Constant(s: String)) => TermName(s)
//      case _ => c.abort(c.enclosingPosition, "Not a string literal")
//    }
//    // do a copy
//    c.Expr[Unit](
//      q"""
//      $self.pipeOut($idx).$pipeFieldTermName \
//      := (
//        $self.pipeIn($idx).$pipeFieldTermName 
//      );
//      """
//    )
//  }
//  def nonWorkStageFunc[
//    PipeElemT <: Data
//  ](
//    self: DualPipeFuncMostArgs[PipeElemT],
//    pipeFieldName: String,
//    idx: Int,
//  ): Unit = macro nonWorkStageImpl[PipeElemT]
//}
