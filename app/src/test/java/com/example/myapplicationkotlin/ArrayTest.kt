package com.example.myapplicationkotlin

import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class ArrayTest {
    @Test
    fun testArray() {
        val myArr2 = arrayOf(
            arrayOf(1, 2, 3),
            arrayOf(4, 5, 6),
            arrayOf(7, 8, 9)
        )

        // Просто вывод в консоль
        myArr2.forEachIndexed { i, row ->
            row.forEachIndexed { j, item ->
                print("$item\t")
            }
            println()
        }
        assertEquals(1, myArr2[0][0])

        val flattened = myArr2.flatMap { it.toList() }
        assertEquals(listOf(1,2,3,4,5,6,7,8,9), flattened)
    }
}