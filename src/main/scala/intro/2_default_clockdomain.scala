package intro

import spinal.core._

object DefaultClockDomain extends App {
    val report = SpinalConfig(
      defaultConfigForClockDomains = ClockDomainConfig(resetKind = BOOT, resetActiveLevel = HIGH),
      // also try
      //defaultConfigForClockDomains = ClockDomainConfig(resetKind = SYNC, resetActiveLevel = LOW),

      // setting the frequency below does not directly change the code
      // when using, the user is required to pass a value matching real hardware
      // it can then be used by hardware generation when needed
      defaultClockDomainFrequency = FixedFrequency(24 MHz),
  ).generateVerilog { Basics() }
}
