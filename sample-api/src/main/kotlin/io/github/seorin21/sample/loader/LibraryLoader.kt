/*
 * Copyright (C) 2022 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.seorin21.sample.loader

import org.bukkit.Bukkit
import java.lang.reflect.InvocationTargetException

/*
 * `LibraryLoader.loadClass` 함수를 만들어서 `LibraryLoader.loadImplement`와 `LibraryLoader.loadImplement`의 공통 로직을 부분 최적화하였습니다.
 */
@Suppress("unused")
object LibraryLoader {
    private fun <T> loadClass(
        type: Class<T>,
        classNameProvider: (Class<T>) -> List<String>,
        vararg initArgs: Any?
    ): T {
        val parameterTypes = initArgs.map { it?.javaClass }.toTypedArray()
        val candidates = classNameProvider(type)

        val loadedClass = candidates.firstNotNullOfOrNull { candidate ->
            try {
                Class.forName(candidate, true, type.classLoader).asSubclass(type)
            } catch (e: ClassNotFoundException) {
                null
            }
        } ?: throw UnsupportedOperationException("${type.name} does not support this version or implementation", ClassNotFoundException())

        try {
            val constructor = loadedClass.getConstructor(*parameterTypes)
                ?: throw UnsupportedOperationException("${type.name} does not have Constructor for [${parameterTypes.joinToString()}]")
            return constructor.newInstance(*initArgs) as T
        } catch (e: NoSuchMethodException) {
            throw UnsupportedOperationException("${type.name} does not have Constructor for [${parameterTypes.joinToString()}]", e)
        } catch (e: IllegalAccessException) {
            throw UnsupportedOperationException("${type.name} constructor is not visible", e)
        } catch (e: InstantiationException) {
            throw UnsupportedOperationException("${type.name} is abstract class", e)
        } catch (e: InvocationTargetException) {
            throw UnsupportedOperationException("${type.name} has an error occurred while creating the instance", e)
        }
    }

    /**
     * 구현 라이브러리 인스턴스를 로드합니다.
     *
     * 패키지는 `<[type]의 패키지>.internal.<[type]의 이름>+Impl` 입니다.
     *
     * ex) `io.github.sample.Sample -> io.github.sample.internal.SampleImpl`
     */
    fun <T> loadImplement(type: Class<T>, vararg initArgs: Any? = emptyArray()): T {
        return loadClass(type, { clazz ->
            val packageName = clazz.`package`.name
            val className = "${clazz.simpleName}Impl"
            listOf("$packageName.internal.$className")
        }, *initArgs)
    }

    /**
     * net.minecraft.server 를 지원하는 라이브러리 인스턴스를 로드합니다.
     *
     * 패키지는 <[type]의 패키지>.[minecraftVersion].NMS + <[type]의 이름> 입니다.
     *
     *
     * ex) `io.github.sample.Sample -> io.github.sample.v1_18.NMSSample`
     */
    fun <T> loadNMS(type: Class<T>, vararg initArgs: Any? = emptyArray()): T {
        return loadClass(type, { clazz ->
            val packageName = clazz.`package`.name
            val className = "NMS${clazz.simpleName}"
            buildList {
                add("$packageName.$libraryVersion.$className")
                val lastDot = packageName.lastIndexOf('.')
                if (lastDot > 0) {
                    val superPackageName = packageName.substring(0, lastDot)
                    val subPackageName = packageName.substring(lastDot + 1)
                    add("$superPackageName.$libraryVersion.$subPackageName.$className")
                }
            }
        }, *initArgs)
    }

    val minecraftVersion by lazy { Bukkit.getServer().minecraftVersion }
    val libraryVersion by lazy { "v${minecraftVersion.replace('.', '_')}" }
}