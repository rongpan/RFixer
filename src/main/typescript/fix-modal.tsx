//
// src/main/typescript/fix-modal.tsx
// RegEx Frontend
//
// Created on 4/9/17
//

import { PureComponent } from 'react'

interface Props {
  regex: string
}

export class FixModal extends PureComponent<Props, {}> {
  render () {
    return (
      <div className="fix-modal">
        <div className="triangle" />
        <div className="header">
          <div className="regex">{this.props.regex}</div>
          {this.props.children}
        </div>
      </div>
    )
  }
}
