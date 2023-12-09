package intro

import spinal.core._
import spinal.lib._

object DefaultClockDomain extends App {
  val report = SpinalConfig(
    defaultConfigForClockDomains = ClockDomainConfig(resetKind = BOOT, resetActiveLevel = HIGH)
    // also try
    //defaultConfigForClockDomains = ClockDomainConfig(resetKind = SYNC, resetActiveLevel = LOW),
  ).generateVerilog(Basics())
}

case class UsingEnableDomain() extends Component {
  val io = new Bundle {
    val blink = out port Bool().setAsReg()
  }
  val counter = CounterFreeRun(100)
  new ClockEnableArea(counter.willOverflow) {
    io.blink := !io.blink
  }
}

object UsingEnableDomain extends App {
  SpinalVerilog(UsingEnableDomain())
}

case class UsingSlowDomain(blinkrate: HertzNumber) extends Component {
  val io = new Bundle {
    val blink = out port Bool().setAsReg()
  }
  new SlowArea(blinkrate) {
    io.blink := io.blink
  }
}

object UsingSlowDomain extends App {
  val report = SpinalConfig(
    defaultConfigForClockDomains = ClockDomainConfig(resetKind = BOOT, resetActiveLevel = HIGH),
    // also try
    //defaultConfigForClockDomains = ClockDomainConfig(resetKind = SYNC, resetActiveLevel = LOW),

    // setting the frequency below does not directly change the code
    // when using, the user is required to pass a value matching real hardware
    // it can then be used by hardware generation when needed
    defaultClockDomainFrequency = FixedFrequency(24.MHz)
  ).generateVerilog(UsingSlowDomain(1.MHz))
}
