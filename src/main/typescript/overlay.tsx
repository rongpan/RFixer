//
// src/main/typescript/overlay.tsx
// RegEx Frontend
//
// Created on 4/7/17
//

import { Component } from 'react'
import { Button } from './button'

export class Overlay extends Component<{}, {}> {
  render () {
    return (
      <div className="corpus-editor-overlay">
        {this.props.children}
      </div>
    )
  }
}
