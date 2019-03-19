//
// src/main/typescript/button.tsx
// RegEx Frontend
//
// Created on 4/7/17
//

import { PureComponent } from 'react'

interface Props {
  glyph: string
  color: string
  arrow?: boolean
  onClick: () => void
}

export class Button extends PureComponent<Props, {}> {
  public static defaultProps: Partial<Props> = {
    arrow: false,
  }

  render () {
    return (
      <button
        className={'action' + ((this.props.arrow) ? ' arrow' : '')}
        data-color={this.props.color}
        onClick={this.props.onClick}>{this.props.glyph}</button>
    )
  }
}
