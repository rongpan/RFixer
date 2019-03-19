//
// src/main/typescript/mouseover-field.ts
// RegEx Frontend
//
// Created on 5/7/17
//

import { Hoverable } from './hoverable'
import { MouseoverZone } from './mouseover-zone'

export class MouseoverField extends Hoverable {
  private highlightZones: MouseoverZone[]
  private selectionZones: MouseoverZone[]
  private activeZone: MouseoverZone

  constructor () {
    super()
    this.clearZones()

    this.on('move', this.defaultMouseMove.bind(this))
    this.on('out', this.defaultMouseOut.bind(this))
  }

  private defaultMouseMove (x: number, y: number): void {
    const hoverCallback = (zone) => {
      let inZone = zone.contains(x, y)

      if (inZone) {
        if (this.activeZone !== null) {
          if (this.activeZone.equals(zone) === false) {
            this.activeZone.trigger('out')
            this.activeZone = null
          }
        }

        this.activeZone = zone
        this.activeZone.trigger('over')
        return true
      }

      return false
    }

    if (this.selectionZones.some(hoverCallback)) {
      return
    } else if (this.highlightZones.some(hoverCallback)) {
      return
    } else {
      if (this.activeZone !== null) {
        this.activeZone.trigger('out')
        this.activeZone = null
      }
    }
  }

  private defaultMouseOut (x: number, y: number): void {
    if (this.activeZone !== null) {
      if (this.activeZone.contains(x, y) === false) {
        this.activeZone.trigger('out')
        this.activeZone = null
      }
    }
  }

  addZone (type: 'highlight' | 'selection', zone: MouseoverZone): void {
    switch (type) {
      case 'highlight':
        this.highlightZones.push(zone)
        break
      case 'selection':
        this.selectionZones.push(zone)
        break
    }
  }

  clearZones (): void {
    this.clearHighlightZones()
    this.clearSelectionZones()
  }

  clearHighlightZones () {
    this.highlightZones = []
    this.activeZone = null
  }

  clearSelectionZones (): void {
    this.selectionZones = []
    this.activeZone = null
  }
}
