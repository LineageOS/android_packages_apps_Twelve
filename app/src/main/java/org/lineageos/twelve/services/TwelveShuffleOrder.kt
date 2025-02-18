package org.lineageos.twelve.services

import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.ShuffleOrder
import kotlin.random.Random

@UnstableApi
class TwelveShuffleOrder(
    private val length: Int,
    private val firstIndex: Int,
    private val random: Random = Random.Default
) : ShuffleOrder {
    // Initialize the shuffled indices
    private val indices = IntArray(length) { it }

    init {
        // If there are more than 1 elements, shuffle all except the firstIndex
        if (length > 1) {
            // Move firstIndex to the start
            var temp = indices[0]
            indices[0] = firstIndex
            indices[firstIndex] = temp

            // Shuffle remaining elements (if any)
            for (i in 1 until length) {
                val randomIndex = random.nextInt(i, length)
                temp = indices[i]
                indices[i] = indices[randomIndex]
                indices[randomIndex] = temp
            }
        }
    }

    override fun getLength() = length

    override fun getNextIndex(index: Int) =
        indices.indexOf(index).let { currentPosition ->
            if (currentPosition == length - 1) C.INDEX_UNSET else indices[currentPosition + 1]
        }

    override fun getPreviousIndex(index: Int) =
        indices.indexOf(index).let { currentPosition ->
            if (currentPosition == 0) C.INDEX_UNSET else indices[currentPosition - 1]
        }

    override fun getLastIndex() = if (length == 0) C.INDEX_UNSET else indices.last()

    override fun getFirstIndex() = if (length == 0) C.INDEX_UNSET else indices.first()

    override fun cloneAndInsert(insertionIndex: Int, insertionCount: Int): TwelveShuffleOrder {
        val newLength = length + insertionCount
        if (insertionCount == 0) return this

        val newFirstIndex = when {
            length == 0 -> 0
            insertionIndex <= firstIndex -> firstIndex + insertionCount
            else -> firstIndex
        }

        return TwelveShuffleOrder(
            length = newLength,
            firstIndex = newFirstIndex,
            random = Random(random.nextLong())
        )
    }

    override fun cloneAndRemove(indexFrom: Int, indexToExclusive: Int): TwelveShuffleOrder {
        val removeCount = indexToExclusive - indexFrom
        if (removeCount == 0) return this

        val newFirstIndex = when {
            firstIndex < indexFrom -> firstIndex
            firstIndex >= indexToExclusive -> firstIndex - removeCount
            else -> 0
        }

        return TwelveShuffleOrder(
            length = length - removeCount,
            firstIndex = newFirstIndex,
            random = Random(random.nextLong())
        )
    }

    override fun cloneAndClear() = TwelveShuffleOrder(
        length = 0,
        firstIndex = 0,
        random = Random(random.nextLong())
    )
}
