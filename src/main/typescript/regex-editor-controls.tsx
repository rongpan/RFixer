//
// src/main/typescript/regex-editor-controls.tsx
// RegEx Frontend
//
// Created on 4/9/17
//

import { PureComponent } from 'react'

export class RegexEditorControls extends PureComponent<{}, {}> {
  render () {
    return (
      <div className="regex-editor-controls">
        {this.props.children}
      </div>
    )
  }
}
