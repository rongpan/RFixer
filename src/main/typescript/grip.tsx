//
// src/main/typescript/grip.tsx
// RegEx Frontend
//
// Created on 4/10/17
//

import { PureComponent } from 'react'
import { Point } from './point'

const GRIP_SIZE = 8

interface Props {
  point: Point
  onDragStart: (offset: [number, number]) => void
}

class Grip extends PureComponent<Props, {}> {
  // The offset values are distances from grip character's center -> the actual
  // grip location on screen. Since the mouse position is near the grip's screen
  // position during a drag, these offsets normalize the mouse X/Y coordinates
  // to be closer to the center of a character on screen.
  handleMouseDown (offset: [number, number], event: MouseEvent) {
    event.preventDefault()
    event.stopPropagation()

    this.props.onDragStart(offset)
  }
}

export class StartGrip extends Grip {
  render () {
    let charCoords = this.props.point.coords
    let charWidth = charCoords.right - charCoords.left
    let charHeight = charCoords.bottom - charCoords.top
    let offsetX = -(charWidth / 2)
    let offsetY = -(charHeight / 2)

    let handleMouseDown = this.handleMouseDown.bind(this, [offsetX, offsetY])

    return (
      <div
        className="grip start-grip"
        onMouseDown={handleMouseDown}
        style={{
          top: this.props.point.coords.top - (GRIP_SIZE / 2),
          left: this.props.point.coords.left - (GRIP_SIZE / 2),
        }} />
    )
  }
}

export class EndGrip extends Grip {
  render () {
    let charCoords = this.props.point.coords
    let charWidth = charCoords.right - charCoords.left
    let charHeight = charCoords.bottom - charCoords.top
    let offsetX = +(charWidth / 2)
    let offsetY = +(charHeight / 2)

    let handleMouseDown = this.handleMouseDown.bind(this, [offsetX, offsetY])

    return (
      <div
        className="grip end-grip"
        onMouseDown={handleMouseDown}
        style={{
          top: this.props.point.coords.bottom - (GRIP_SIZE / 2),
          left: this.props.point.coords.left - (GRIP_SIZE / 2),
        }} />
    )
  }
}
