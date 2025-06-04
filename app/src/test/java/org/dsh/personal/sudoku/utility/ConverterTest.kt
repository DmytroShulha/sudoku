package org.dsh.personal.sudoku.utility


import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConverterKtTest {

    @Test
    fun `toBoard with empty array returns empty list`() {
        val inputArray = emptyArray<IntArray>()
        val expectedList = emptyList<List<Int>>()

        val resultList = inputArray.toBoard()

        assertEquals("Converting an empty IntArray array should result in an empty list.", expectedList, resultList)
    }

    @Test
    fun `toBoard with array of empty int arrays returns list of empty lists`() {
        val inputArray = arrayOf(
            intArrayOf(),
            intArrayOf()
        )
        val expectedList = listOf(
            emptyList<Int>(),
            emptyList<Int>()
        )

        val resultList = inputArray.toBoard()

        assertEquals("Array of empty IntArrays should convert to list of empty lists.", expectedList.size, resultList.size)
        resultList.forEachIndexed { index, innerList ->
            assertTrue("Inner list at index $index should be empty.", innerList.isEmpty())
            assertEquals("Inner list at index $index should match expected.", expectedList[index], innerList)
        }
    }

    @Test
    fun `toBoard with populated array returns correctly populated list`() {
        val inputArray = arrayOf(
            intArrayOf(1, 2, 3),
            intArrayOf(4, 5, 6),
            intArrayOf(7, 8, 9)
        )
        val expectedList = listOf(
            listOf(1, 2, 3),
            listOf(4, 5, 6),
            listOf(7, 8, 9)
        )

        val resultList = inputArray.toBoard()

        assertEquals("The size of the outer list should match the array size.", expectedList.size, resultList.size)
        resultList.forEachIndexed { rowIndex, innerResultList ->
            assertEquals("Size of inner list at row $rowIndex should match.", expectedList[rowIndex].size, innerResultList.size)
            innerResultList.forEachIndexed { colIndex, value ->
                assertEquals(
                    "Value at [$rowIndex][$colIndex] should match. Expected ${expectedList[rowIndex][colIndex]}, got $value",
                    expectedList[rowIndex][colIndex],
                    value
                )
            }
        }
    }

    @Test
    fun `toBoard with array containing mixed empty and populated int arrays`() {
        val inputArray = arrayOf(
            intArrayOf(1, 0, 5),
            intArrayOf(),
            intArrayOf(7, 2)
        )
        val expectedList = listOf(
            listOf(1, 0, 5),
            emptyList<Int>(),
            listOf(7, 2)
        )

        val resultList = inputArray.toBoard()

        assertEquals("Outer list size should match.", expectedList.size, resultList.size)
        resultList.forEachIndexed { index, innerList ->
            assertEquals("Inner list at index $index should match expected content and size.", expectedList[index], innerList)
        }
    }

    @Test
    fun `toBoard preserves element order`() {
        val inputArray = arrayOf(
            intArrayOf(9, 8, 7, 6),
            intArrayOf(5, 4, 3, 2, 1)
        )
        val expectedList = listOf(
            listOf(9, 8, 7, 6),
            listOf(5, 4, 3, 2, 1)
        )

        val resultList = inputArray.toBoard()
        assertEquals("Conversion should preserve element order.", expectedList, resultList)
    }
}