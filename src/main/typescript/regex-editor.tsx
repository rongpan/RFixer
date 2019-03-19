//
// src/main/typescript/regex-editor.tsx
// RegEx Frontend
//
// Created on 4/7/17
//

import { Component } from 'react'
import { Editor } from './editor'

interface Props {
  regex: string
  onRegexChange: (newRegex: string) => void
}

export class RegexEditor extends Component<Props, {}> {
  render () {
    return (
      <div className="regex-editor">
        <Editor
          value={this.props.regex}
          onChange={this.props.onRegexChange} />
        {this.props.children}
      </div>
    )
  }
}
