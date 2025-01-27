package io.github.seorin21.sample

import io.github.seorin21.sample.loader.LibraryLoader

interface Sample {
    companion object: Sample by LibraryLoader.loadImplement(Sample::class.java)

    fun printCoreMessage()
}