//
// src/main/typescript/highlight-list.ts
// RegEx Frontend
//
// Created on 4/7/17
//

import { Highlight } from './highlight'

export class HighlightList {
  private head: Highlight
  private tail: Highlight

  constructor () {
    this.head = null
    this.tail = null
  }

  clone () {
    let other = new HighlightList()
    other.head = this.head
    other.tail = this.tail

    return other
  }

  getMatches (): { start: number, end: number }[] {
    return this.map((h) => {
      return { start: h.getStart().index, end: h.getEnd().index }
    })
  }

  insert (h: Highlight): HighlightList {
    if (this.head === null) {
      this.head = h
      this.tail = h
      return this.clone()
    }

    // The given Highlight comes before the current list head OR the list is
    // empty and the given Highlight is the first member of the list.
    if (h.getEnd().index <= this.head.getStart().index) {
      this.head.setPrev(h)
      h.setPrev(null)
      h.setNext(this.head)
      this.head = h
      return this.clone()
    }

    // The given Highlight comes after the last member of the list.
    if (h.getStart().index >= this.tail.getEnd().index) {
      this.tail.setNext(h)
      h.setPrev(this.tail)
      h.setNext(null)
      this.tail = h
      return this.clone()
    }

    // The given Highlight belongs somewhere in the middle of the list.
    let curr = this.head
    while (curr !== null && curr.getNext() !== null) {
      let lower = curr.getEnd().index
      let upper = curr.getNext().getStart().index

      // The given Highlight fits between the `curr` Highlight and
      // `curr.getNext()` so add it to the list.
      if (lower <= h.getStart().index && h.getEnd().index <= upper) {
        h.setPrev(curr)
        h.setNext(curr.getNext())
        curr.setNext(h)
        curr.getNext().getNext().setPrev(h)
        return this.clone()
      }

      curr = curr.getNext()
    }

    throw new Error('highlight does not fit in list')
  }

  remove (h: Highlight): HighlightList {
    if (this.head === h) {
      this.head = this.head.getNext()

      if (this.head !== null) {
        this.head.setPrev(null)
      }

      return this.clone()
    }

    if (this.tail === h) {
      this.tail.getPrev().setNext(null)
      this.tail = this.tail.getPrev()
      return this.clone()
    }

    let curr = this.head
    while (curr !== null) {
      if (curr.getNext() === h) {
        curr.setNext(h.getNext())
        h.getNext().setPrev(curr)
        return this.clone()
      }

      curr = curr.getNext()
    }

    throw new Error('highlight not found in list')
  }

  forEach (iteratee: (h: Highlight, i: number) => void): void {
    this.map(iteratee)
  }

  map<T> (iteratee: (h: Highlight, i: number) => T): T[] {
    let accum: T[] = []
    let curr = this.head
    let i = 0

    while (curr !== null) {
      accum.push(iteratee(curr, i++))
      curr = curr.getNext()
    }

    return accum
  }

  reduce<T> (iteratee: (accum: T, h: Highlight, i: number) => T, memo: T): T {
    let curr = this.head
    let i = 0

    while (curr !== null) {
      memo = iteratee(memo, curr, i++)
      curr = curr.getNext()
    }

    return memo
  }

  toString (): string {
    return this.reduce((s, h, i) => {
      return s + ((i > 0) ? ' -> ' : '') + h.toString()
    }, '')
  }
}
