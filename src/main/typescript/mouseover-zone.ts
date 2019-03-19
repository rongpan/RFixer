//
// src/main/typescript/mouseover-zone.ts
// RegEx Frontend
//
// Created on 5/7/17
//

export class MouseoverZone {
  protected x: number
  protected y: number
  protected width: number
  protected height: number
  private onMouseOver: ((...args) => void)[]
  private onMouseOut: ((...args) => void)[]

  constructor (x: number, y: number, width: number, height: number) {
    this.x = x
    this.y = y
    this.width = width
    this.height = height
    this.onMouseOver = []
    this.onMouseOut = []
  }

  equals (other: MouseoverZone): boolean {
    return (other !== null) &&
           (this.x === other.x) &&
           (this.y === other.y) &&
           (this.width === other.width) &&
           (this.height === other.height)
  }

  contains (x: number, y: number): boolean {
    let fitsHoriz = (this.x <= x && x <= (this.x + this.width))
    let fitsVert  = (this.y <= y && y <= (this.y + this.height))
    return (fitsHoriz && fitsVert)
  }

  on (event: 'over' | 'out', callback: (...args) => void): void {
    switch (event) {
      case 'over':
        this.onMouseOver.push(callback)
        break
      case 'out':
        this.onMouseOut.push(callback)
        break
    }
  }

  trigger (event: 'over' | 'out', ...args): void {
    let callbacks: ((...args) => void)[] = []

    switch (event) {
      case 'over':
        callbacks = this.onMouseOver
        break
      case 'out':
        callbacks = this.onMouseOut
        break
    }

    callbacks.forEach((cb) => {
      cb.apply(cb, args)
    })
  }
}
