package intro

import spinal.core._
import spinal.core.sim._

case class SimpleWG2812() extends Component {
  val io = new Bundle {
    val r = in port UInt(8 bit)
    val g = in port UInt(8 bit)
    val b = in port UInt(8 bit)
    val valid = in port Bool()
    val ready = out port Bool()

    val dout = out port Bool()
    val idle = out port Bool()
  }

  val dout = Bool()
  io.dout := RegNext(dout) init False

  val timing = new Area {
    // notice the === comparison -> generates hardware that compares
    // we don't need to specify any types - they are taken from the result
    // type of the comparison (which is Bool)
    val counter = Reg(UInt(10 bit)) init 0
    val shortCntReached = counter === 7
    val longCntReached = counter === 15
    val rstCntReached = counter === 999

    when(!rstCntReached) {
      counter := counter + 1
    }
  }

  io.ready := False
  dout := False

  val busy = Reg(Bool()) init False
  io.idle := !busy
  val shiftReg = Reg(Bits(3 * 8 bit))
  val bitCnt = Reg(UInt(8 bit))
  when(!busy && io.valid && timing.rstCntReached) {
    shiftReg := io.b.reversed ## io.r.reversed ## io.g.reversed
    io.ready := True
    busy := True
    timing.counter := 0
    bitCnt := 3*8-1

    dout := True
  }
  when(busy) {
    when(io.dout && ((shiftReg(0) && timing.longCntReached) || (!shiftReg(0) && timing.shortCntReached))) {
      timing.counter := 0
      dout := False
    } elsewhen(!io.dout && ((shiftReg(0) && timing.shortCntReached) || (!shiftReg(0) && timing.longCntReached))) {
      shiftReg := True ## shiftReg.dropLow(1)
      timing.counter := 0
      bitCnt := bitCnt - 1
      when(bitCnt =/= 0) {
        dout := True
      } otherwise {
        busy := False
      }
    } otherwise {
      dout := io.dout
    }
  }
}

object SimpleWG2812Test extends App {
  val dut = SpinalSimConfig().withWave.compile(SimpleWG2812())
  dut.doSim("simple") { dut =>
    SimTimeout(50000)
    dut.io.r #= 0x55
    dut.io.g #= 0xf0
    dut.io.b #= 0x78
    dut.io.valid #= true

    dut.clockDomain.forkStimulus(10)

    dut.clockDomain.waitSamplingWhere(dut.io.ready.toBoolean)
    dut.io.valid #= false
    dut.clockDomain.waitSampling()

    waitUntil(dut.io.idle.toBoolean)

    sleep(100)
  }
}
