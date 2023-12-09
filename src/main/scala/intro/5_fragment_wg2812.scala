package intro

import spinal.core._
import spinal.lib._
import spinal.core.sim._
import spinal.lib.sim._

case class FragmentWG2812() extends Component {
  val io = new Bundle {
    val colors = slave port Stream(Fragment(UInt(8 bit)))
    val idle = out port Bool()
    val dout = out port Bool().setAsReg().init(False)
  }

  val timing = new Area {
    val counter = Reg(UInt(10 bit)) init 0
    val shortCntReached = counter === 7
    val longCntReached = counter === 15
    val rstCntReached = counter === 999

    when(!rstCntReached) {
      counter := counter + 1
    }
  }

  val wasLast = RegNextWhen(io.colors.last, io.colors.fire)
  io.colors.setBlocked()
  io.dout := False

  val busy = Reg(Bool()) init False
  io.idle := !busy
  val shiftReg = Reg(Bits(8 bit))
  val bitCnt = Reg(UInt(8 bit))
  when(!busy && io.colors.valid && timing.rstCntReached) {
    shiftReg := io.colors.fragment.reversed.asBits
    io.colors.ready := True
    busy := True
    timing.counter := 0
    bitCnt := 8

    io.dout := True
  }
  val highDone = shiftReg(0).mux(True -> timing.longCntReached, False -> timing.shortCntReached)
  val lowDone = shiftReg(0) ? timing.shortCntReached | timing.longCntReached
  when(busy) {
    when(io.dout && highDone) {
      timing.counter := 0
      io.dout := False
    } elsewhen(!io.dout && lowDone) {
      shiftReg := True ## shiftReg.dropLow(1)
      timing.counter := 0
      bitCnt := bitCnt - 1
      when(bitCnt =/= 0) {
        io.dout := True
      } elsewhen(bitCnt === 0 && !wasLast) {
        io.dout := True
        // we will ignore the underflow case here and just document
        // that the user must not generate one ;-)
        shiftReg := io.colors.fragment.reversed.asBits
        io.colors.ready := True
        timing.counter := 0
        bitCnt := 8
      } otherwise {
        busy := False
      }
    } otherwise {
      io.dout := io.dout
    }
  }
}

object FragmentWG2812 extends App {
  val dut = SpinalSimConfig().withWave.compile(FragmentWG2812())
  dut.doSim("simple") { dut =>
    SimTimeout(50000)

    //val scoreboard = ScoreboardInOrder[Int]()
    var remaining = 9
    StreamDriver(dut.io.colors, dut.clockDomain) { payload =>
      remaining = remaining - 1
      payload.fragment.randomize()
      payload.last #= remaining == 0
      remaining >= 0
    }.transactionDelay = () => 0
    //StreamMonitor(dut.io.colors, dut.clockDomain) { payload =>
    //  scoreboard.pushRef(payload.fragment.toInt)
    //}
    // here should be a simulated WG2812 device for real checking...
    //WG2812SimRx(dut.io.dout, 100, 60) { i =>
    //  println(f"DUT ${i}")
    //  scoreboard.pushDut(i)
    //}

    dut.clockDomain.forkStimulus(10)
    dut.clockDomain.waitSamplingWhere(dut.io.colors.ready.toBoolean)
    dut.clockDomain.waitSamplingWhere(dut.io.idle.toBoolean)
    sleep(100)

    //print(scoreboard.matches)
    //assert(scoreboard.matches == 9)
    //scoreboard.checkEmptyness()
  }
}
