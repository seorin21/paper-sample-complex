package io.github.seorin21.sample.internal

import io.github.seorin21.sample.loader.LibraryLoader
import io.github.seorin21.sample.Sample

class SampleImpl: Sample {
    private val version = LibraryLoader.loadNMS(Version::class.java)

    override fun printCoreMessage() {
        println("This is core, version = ${version.value}")
    }
}

interface Version {
    val value: String
}