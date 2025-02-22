/*
 * SPDX-FileCopyrightText: 2022-2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

/**
 * Get the next element in the list relative to the [current] element.
 *
 * If the element is the last in the list or it's not present in the list
 * it will return the first element.
 * If the list is empty, null will be returned.
 *
 * @param current The element to use as cursor
 *
 * @return [E] Either the next element, the first element or null
 */
fun <E> List<E>.next(current: E) = getOrElse(indexOf(current) + 1) { firstOrNull() }

/**
 * Get the previous element in the list relative to the [current] element.
 *
 * If the element is the first in the list or it's not present in the list
 * it will return the last element.
 * If the list is empty, null will be returned.
 *
 * @param current The element to use as cursor
 *
 * @return [E] Either the previous element, the last element or null
 */
fun <E> List<E>.previous(current: E) = getOrElse(indexOf(current) - 1) { lastOrNull() }

/**
 * @see List.drop
 */
fun <E> List<E>.dropAsView(n: Int) = object : List<E> {
    private val originalList = this@dropAsView

    override val size = (originalList.size - n).coerceAtLeast(0)

    override fun get(index: Int) = originalList[index + n]

    override fun isEmpty() = size == 0

    override fun iterator() = object : Iterator<E> {
        private val originalIterator = originalList.iterator().apply {
            repeat(n) {
                next()
            }
        }

        override fun hasNext() = originalIterator.hasNext()

        override fun next() = originalIterator.next()
    }

    override fun listIterator() = listIterator(0)

    override fun listIterator(index: Int) = object : ListIterator<E> {
        private val originalListIterator = originalList.listIterator(index + n)
        private var actualIndex = index

        override fun hasNext() = originalListIterator.hasNext()

        override fun hasPrevious() = actualIndex == 0

        override fun next() = originalListIterator.next().also {
            actualIndex += 1
        }

        override fun nextIndex() = originalListIterator.nextIndex() - n

        override fun previous() = when (actualIndex == 0) {
            true -> throw NoSuchElementException()
            false -> originalListIterator.previous().also {
                actualIndex -= 1
            }
        }

        override fun previousIndex() = originalListIterator.previousIndex() - n
    }

    override fun subList(
        fromIndex: Int, toIndex: Int
    ) = originalList.subList(fromIndex + n, toIndex + n)

    override fun lastIndexOf(element: E): Int {
        TODO("Not yet implemented")
    }

    override fun indexOf(element: E): Int {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        TODO("Not yet implemented")
    }

    override fun contains(element: E): Boolean {
        TODO("Not yet implemented")
    }
}
