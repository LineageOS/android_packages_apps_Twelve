package org.lineageos.twelve.services

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.ShuffleOrder
import kotlin.random.Random

@UnstableApi
class TwelveShuffleOrder(
    private val length: Int,
    private val firstIndex: Int,
    private val random: Random = Random.Default
) : ShuffleOrder {
    private val shuffledIndices = IntArray(length) { it }

    init {
        if (length > 1) {
            shuffleExceptFirst()
        }
    }

    private fun shuffleExceptFirst() {
        val list = buildList {
            addAll(shuffledIndices.toTypedArray())
            remove(firstIndex)
            shuffle(random)
            add(0, firstIndex)
        }
        for (i in list.indices) {
            shuffledIndices[i] = list[i]
        }
    }

    override fun getLength() = length

    override fun getNextIndex(index: Int): Int {
        val pos = shuffledIndices.indexOf(index)
        return if (pos == length - 1) -1 else shuffledIndices[pos + 1]
    }

    override fun getPreviousIndex(index: Int): Int {
        val pos = shuffledIndices.indexOf(index)
        return if (pos == 0) -1 else shuffledIndices[pos - 1]
    }

    override fun getLastIndex() = shuffledIndices.last()

    override fun getFirstIndex() = shuffledIndices.first()

    override fun cloneAndInsert(index: Int, insertCount: Int) =
        TwelveShuffleOrder(length + insertCount, firstIndex, random)

    override fun cloneAndRemove(index: Int, indexToExclusive: Int) =
        TwelveShuffleOrder(length - (indexToExclusive - index), firstIndex, random)

    override fun cloneAndClear() = TwelveShuffleOrder(0, firstIndex, random)
}
