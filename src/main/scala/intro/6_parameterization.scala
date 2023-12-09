package intro

import spinal.core._

case class Config(bitWidth: Int) extends Bundle {
  val value = UInt(bitWidth bit)
  val en = Bool()
}

case class Generics(bitWidth: Int, channels: Int)

case class Parameterization(g: Generics) extends Component {
  val io = new Bundle {
    val configs = Vec(Config(g.bitWidth), g.channels)
    val outs = Bits(g.channels bit)
  }
  for (i <- 0 until g.channels) {
    val cnt = UInt(g.bitWidth + 1 bit)
    io.outs(i) := cnt.msb
    when(io.configs(i).en) {
      cnt := cnt + io.configs(i).value
    } otherwise {
      cnt := 0
    }
  }
}

object Parameterization extends App {
  SpinalVerilog(Parameterization(Generics(bitWidth = 8, channels = 3)))
}
