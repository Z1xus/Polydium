package org.polyfrost.polydium.client

import net.fabricmc.api.ClientModInitializer
//? if > 1.8.9 {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.polyfrost.polydium.client.perf.PerfClock
//?}

class PolydiumClient : ClientModInitializer {
    override fun onInitializeClient() {
        //? if > 1.8.9
        ClientTickEvents.END_CLIENT_TICK.register { PerfClock.tick += 1 }

        PolydiumConfig.preload()
    }
}
