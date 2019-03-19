//
// src/main/typescript/underlay.tsx
// RegEx Frontend
//
// Created on 5/1/17
//

import { Component } from 'react'
import { MouseoverZone } from './mouseover-zone'
import { Highlight } from './highlight'
import { HighlightList } from './highlight-list'

const CM_PADDING = 4

interface Props {
  highlightList: HighlightList
  colors: string[]
  onNewPopoverZone: (zone: MouseoverZone, highlight: Highlight) => void
}

export class Underlay extends Component<Props, {}> {
  private canvas: HTMLCanvasElement
  private ctx: CanvasRenderingContext2D

  componentDidMount () {
    this.ctx = this.canvas.getContext('2d')
  }

  componentWillReceiveProps (newProps: Props) {
    let colorIndex = 0
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height)

    newProps.highlightList.forEach((highlight) => {
      this.ctx.fillStyle = this.props.colors[colorIndex]
      colorIndex = (colorIndex + 1) % this.props.colors.length

      let start = highlight.getStart().coords
      let end = highlight.getEnd().coords

      if (start.top === end.top) {
        let x = start.left
        let y = start.top
        let w = end.left - x
        let h = end.bottom - y
        this.ctx.fillRect(x, y, w, h)
      } else {
        let x = start.left
        let y = start.top
        let w = this.canvas.width - x
        let h = start.bottom - y
        this.ctx.fillRect(x, y, w, h)

        x = CM_PADDING
        y = start.bottom
        w = this.canvas.width
        h = end.top - y
        this.ctx.fillRect(x, y, w, h)

        x = CM_PADDING
        y = end.top
        w = end.left
        h = end.bottom - y
        this.ctx.fillRect(x, y, w, h)
      }
    })
  }

  render () {
    return (
      <div className="corpus-editor-underlay">
        <canvas className="canvas" width="1000" height="500" ref={(elem) => {
          this.canvas = elem
        }} />
      </div>
    )
  }
}
