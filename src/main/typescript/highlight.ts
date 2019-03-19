//
// src/main/typescript/highlight.ts
// RegEx Frontend
//
// Created on 4/7/17
//

import 'codemirror'
import { Point, PointPair } from './point'

export class Highlight {
  private prev: Highlight = null
  private next: Highlight = null
  private pair: PointPair

  constructor (pair: PointPair) {
    this.pair = pair
  }

  getPair (): PointPair {
    return this.pair
  }

  getStart (): Point {
    return this.pair.start
  }

  setStart (p: Point) {
    this.pair.start = p
  }

  getEnd (): Point {
    return this.pair.end
  }

  setEnd (p: Point) {
    this.pair.end = p
  }

  getPrev (): Highlight {
    return this.prev
  }

  setPrev (h: Highlight): void {
    this.prev = h
  }

  getNext (): Highlight {
    return this.next
  }

  setNext (h: Highlight): void {
    this.next = h
  }

  toString (): string {
    return `(${this.pair.start.index}:${this.pair.end.index})`
  }
}
