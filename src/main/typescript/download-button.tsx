//
// src/main/typescript/download-button.tsx
// RegEx Frontend
//
// Created on 5/30/17
//

import { PureComponent } from 'react'

interface Props {
  glyph: string
  color: string
  arrow?: boolean
  name: string
  href: string
}

export class DownloadButton extends PureComponent<Props, {}> {
  public static defaultProps: Partial<Props> = {
    arrow: false,
  }

  render () {
    return (
      <a
        className={'action' + ((this.props.arrow) ? ' arrow' : '')}
        data-color={this.props.color}
        download={this.props.name}
        href={this.props.href}>{this.props.glyph}</a>
    )
  }
}
