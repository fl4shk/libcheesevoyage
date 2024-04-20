package libcheesevoyage.bus.tilelink

import scala.collection.mutable.Map
import spinal.core._
import spinal.core.formal._
import spinal.lib._
import spinal.lib.bus.tilelink

object TlOpcode extends AreaRoot {
  //val A = new SpinalEnum{
  //  val
  //    PUT_FULL_DATA,
  //    PUT_PARTIAL_DATA,
  //    ARITHMETIC_DATA,
  //    LOGICAL_DATA,
  //    GET,
  //    INTENT,
  //    ACQUIRE_BLOCK,
  //    ACQUIRE_PERM
  //    = newElement();
  //  defaultEncoding=SpinalEnumEncoding("enc")(
  //    PUT_FULL_DATA -> 0,
  //    PUT_PARTIAL_DATA -> 1,
  //    ARITHMETIC_DATA -> 2,
  //    LOGICAL_DATA -> 3,
  //    GET -> 4,
  //    INTENT -> 5,
  //    ACQUIRE_BLOCK -> 6,
  //    ACQUIRE_PERM -> 7,
  //  )
  //}
  //val D = new SpinalEnum{
  //  val
  //    ACCESS_ACK,
  //    ACCESS_ACK_DATA,
  //    HINT_ACK,
  //    GRANT,
  //    GRANT_DATA,
  //    RELEASE_ACK
  //    = newElement();
  //  defaultEncoding=SpinalEnumEncoding("enc")(
  //    ACCESS_ACK -> 0,
  //    ACCESS_ACK_DATA -> 1,
  //    HINT_ACK -> 2,
  //    GRANT -> 4,
  //    GRANT_DATA -> 5,
  //    RELEASE_ACK -> 6,
  //  )
  //}
  //val hostToDevMap = Map[A.E, D.E](
  //  A.PUT_FULL_DATA -> D.ACCESS_ACK,
  //  A.PUT_PARTIAL_DATA -> D.ACCESS_ACK,
  //  A.ARITHMETIC_DATA -> D.ACCESS_ACK_DATA,
  //  A.LOGICAL_DATA -> D.ACCESS_ACK_DATA,
  //  A.GET -> D.ACCESS_ACK_DATA,
  //  A.INTENT -> D.HINT_ACK,
  //)
  //val devToHostMap = Map[D.E, A.E](
  //  hostToDevMap(A.PUT_FULL_DATA) -> A.PUT_FULL_DATA,
  //  hostToDevMap(A.PUT_PARTIAL_DATA) -> A.PUT_PARTIAL_DATA,
  //  hostToDevMap(A.ARITHMETIC_DATA) -> A.ARITHMETIC_DATA,
  //  hostToDevMap(A.LOGICAL_DATA) -> A.LOGICAL_DATA,
  //  hostToDevMap(A.GET) -> A.GET,
  //  hostToDevMap(A.INTENT) -> A.INTENT,
  //)
  val tlUlHostToDevMap = Map[tilelink.Opcode.A.E, tilelink.Opcode.D.E](
    tilelink.Opcode.A.PUT_FULL_DATA -> tilelink.Opcode.D.ACCESS_ACK,
    tilelink.Opcode.A.PUT_PARTIAL_DATA -> tilelink.Opcode.D.ACCESS_ACK,
    tilelink.Opcode.A.GET -> tilelink.Opcode.D.ACCESS_ACK_DATA,
  )
  //val devToHostMap = Map[tilelink.Opcode.D.E, tilelink.Opcode.A.E](
  //  tilelink.Opcode.D.ACCESS_ACK -> tilelink.Opcode.A.PUT_FULL_DATA,
  //  tilelink.Opcode.D.ACCESS_ACK -> tilelink.Opcode.A.PUT_PARTIAL_DATA,
  //  tilelink.Opcode.D.ACCESS_ACK_DATA -> tilelink.Opcode.A.GET,
  //)
}
