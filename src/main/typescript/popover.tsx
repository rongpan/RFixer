//
// src/main/typescript/popover.tsx
// RegEx Frontend
//
// Created on 4/10/17
//

import { PureComponent, MouseEvent } from 'react'
import { PointPair } from './point'

const POPOVER_WIDTH = 32

interface Props {
  pair: PointPair
  onMouseOver: (event: MouseEvent<HTMLDivElement>) => void
  onMouseOut: (event: MouseEvent<HTMLDivElement>) => void
}

export class Popover extends PureComponent<Props, {}> {
  render () {
    let rangeLeft = this.props.pair.start.coords.left
    let rangeWidth = this.props.pair.end.coords.left - rangeLeft

    let top = this.props.pair.end.coords.bottom
    let left = (rangeLeft + (rangeWidth / 2)) - (POPOVER_WIDTH / 2)

    return (
      <div
        className="popover"
        onMouseOver={this.props.onMouseOver}
        onMouseOut={this.props.onMouseOut}
        style={{ top: top, left: left }}>
        {this.props.children}
      </div>
    )
  }
}
