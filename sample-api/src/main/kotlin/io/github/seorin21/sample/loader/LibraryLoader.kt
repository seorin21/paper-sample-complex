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
    private fun <T> loadClass(className: String, type: Class<T>, vararg initArgs: Any?): T {
        val parameterTypes = initArgs.map { it?.javaClass }.toTypedArray()

        return try {
            val clazz = Class.forName(className, true, type.classLoader).asSubclass(type)
            val constructor = clazz.getConstructor(*parameterTypes)
            constructor.newInstance(*initArgs) as T
        } catch (e: ClassNotFoundException) {
            throw UnsupportedOperationException("${type.name} does not have implement", e)
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
    fun <T> loadImplement(type: Class<T>, vararg initArgs: Any?): T {
        val packageName = type.`package`.name
        val className = "${type.simpleName}Impl"
        return loadClass("$packageName.internal.$className", type, *initArgs)
    }

    /**
     * net.minecraft.server 를 지원하는 라이브러리 인스턴스를 로드합니다.
     *
     * 패키지는 <[type]의 패키지>.[minecraftVersion].NMS + <[type]의 이름> 입니다.
     *
     *
     * ex) `io.github.sample.Sample -> io.github.sample.v1_18.NMSSample`
     */
    fun <T> loadNMS(type: Class<T>, vararg initArgs: Any?): T {
        val packageName = type.`package`.name
        val className = "NMS${type.simpleName}"
        val candidates = listOf(
            "$packageName.$libraryVersion.$className",
            "${
                packageName.substringBeforeLast('.', "") + ".$libraryVersion." +
                        packageName.substringAfterLast('.')
            }.$className"
        )

        candidates.forEach { candidate ->
            runCatching { return loadClass(candidate, type, *initArgs) }
        }

        throw UnsupportedOperationException("${type.name} does not support this version: $libraryVersion")
    }

    private val minecraftVersion by lazy { Bukkit.getServer().minecraftVersion }
    private val libraryVersion by lazy { "v${minecraftVersion.replace('.', '_')}" }
}