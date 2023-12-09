package intro

import spinal.core._

case class Basics() extends Component {
  val io = new Bundle {
    val i = in port Bool()
    val o = out port Bool()
    val o2 = out port Bool()
  }
  val delayed = Reg(Bool()) init False
  delayed := io.i
  io.o := delayed

  // equivalent to above
  io.o2 := RegNext(io.i)
}

object Basics extends App {
  SpinalVerilog(Basics())
  SpinalVhdl(Basics())
}
