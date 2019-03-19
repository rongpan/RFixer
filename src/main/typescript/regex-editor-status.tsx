//
// src/main/typescript/regex-editor-status.tsx
// RegEx Frontend
//
// Created on 4/9/17
//

import { PureComponent } from 'react'

interface Props {
  inError: boolean
}

export class RegexEditorStatus extends PureComponent<Props, {}> {
  render () {
    return (
      <div className="regex-editor-status" data-error={this.props.inError}>
        {this.props.children}
      </div>
    )
  }
}
