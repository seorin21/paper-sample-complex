package io.github.seorin21.sample.plugin

import io.github.seorin21.sample.Sample
import org.bukkit.plugin.java.JavaPlugin

class SamplePlugin : JavaPlugin() {
    override fun onEnable() {
        Sample.printCoreMessage()
    }
}
