//
// src/main/typescript/hoverable.ts
// RegEx Frontend
//
// Created on 5/7/17
//

type EventType = 'over' | 'move' | 'out'
type EventListener = (...args: any[]) => void

export class Hoverable {
  private onMouseOver: EventListener[]
  private onMouseMove: EventListener[]
  private onMouseOut : EventListener[]

  constructor () {
    this.onMouseOver = []
    this.onMouseMove = []
    this.onMouseOut  = []
  }

  on (event: EventType, callback: EventListener): void {
    this.getListeners(event).push(callback)
  }

  off (event: EventType, callback: EventListener): void {
    // TODO
  }

  trigger (event: EventType, ...args): void {
    this.getListeners(event).forEach((callback) => {
      callback.apply(callback, args)
    })
  }

  private getListeners (event: EventType): EventListener[] {
    switch (event) {
      case 'over':
        return this.onMouseOver
      case 'move':
        return this.onMouseMove
      case 'out':
        return this.onMouseOut
      default:
        return []
    }
  }
}
