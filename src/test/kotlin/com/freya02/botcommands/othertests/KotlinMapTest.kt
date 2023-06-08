package com.freya02.botcommands.othertests

import java.util.concurrent.ThreadLocalRandom

object KotlinMapTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val warmups = 10000
        val iterations = 10000
        val mapSize = 10000

        val kotlinMap = makeKotlinMap(mapSize)
        val javaMap = makeJavaMap(mapSize)

        repeat(2) {
            TestUtils.measureTime("buildMap", warmups, iterations) {
                val map = makeKotlinMap(mapSize)

                if (Math.random() > 1.12) {
                    println(map)
                }
            }

            TestUtils.measureTime("HashMap", warmups, iterations) {
                val map = makeJavaMap(mapSize)

                if (Math.random() > 1.12) {
                    println(map)
                }
            }
        }

        val random = ThreadLocalRandom.current()
        repeat(2) {
            TestUtils.measureTime("HashMap get", warmups, iterations) {
                repeat(mapSize) {
                    val value = javaMap[random.nextInt(0, mapSize)]
                    if (Math.random() > 1.12) {
                        println(value)
                    }
                }
            }

            TestUtils.measureTime("Kotlin Map get", warmups, iterations) {
                repeat(mapSize) {
                    val value = kotlinMap[random.nextInt(0, mapSize)]
                    if (Math.random() > 1.12) {
                        println(value)
                    }
                }
            }
        }
    }

    private fun makeKotlinMap(mapSize: Int) = buildMap(mapSize) {
        for (i in 0..mapSize) {
            this[i] = i
        }
    }

    private fun makeJavaMap(mapSize: Int) = LinkedHashMap<Int, Int>(mapSize).apply {
        for (i in 0..mapSize) {
            this[i] = i
        }
    }
}