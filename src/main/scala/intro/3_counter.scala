package intro

import spinal.core._
import spinal.core.sim._

case class Counter() extends Component {
  val io = new Bundle {
    val cnt = out port UInt(5 bit)
  }
  val cnt = Reg(UInt(5 bit)) init 0
  cnt := cnt + 1
  io.cnt := cnt
}

object CounterSim extends App {
  val dut = SimConfig.withWave.compile(Counter())
  dut.doSim("simple") { dut =>
    dut.clockDomain.forkStimulus(10)
    dut.clockDomain.waitSampling(200)
  }
}
