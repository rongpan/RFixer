//
// src/main/typescript/editor.tsx
// RegEx Frontend
//
// Created on 4/7/17
//

import 'codemirror'
import { Component } from 'react'

interface EditorProps {
  value: string
  onChange?: (value: string) => void
  onFocus?: () => void
  onBlur?: () => void
}

export class Editor extends Component<EditorProps, {}> {
  textarea: HTMLTextAreaElement
  instance: CodeMirror.Editor

  static defaultProps: Partial<EditorProps> = {
    onChange: (() => {}),
    onFocus: (() => {}),
    onBlur: (() => {}),
  }

  componentDidMount () {
    this.instance = window['CodeMirror'].fromTextArea(this.textarea)
    this.instance.on('change', this.editorValueChanged.bind(this))
    this.instance.on('focus', this.editorFocusChanged.bind(this, true))
    this.instance.on('blur', this.editorFocusChanged.bind(this, false))
    this.instance.on('scroll', this.editorScrollChanged.bind(this))
    this.instance.setValue(this.props.value)
  }

  componentWillReceiveProps (newProps: EditorProps) {
    if (this.instance.getValue() !== newProps.value) {
      this.instance.setValue(newProps.value)
    }
  }

  editorValueChanged (cm: CodeMirror.Editor, change: CodeMirror.EditorChange) {
    if (change.origin !== 'setValue') {
      this.props.onChange(cm.getValue())
    }
  }

  editorFocusChanged (focused: boolean) {
    this.setState({
      isFocused: focused,
    })

    if (focused) {
      this.props.onFocus()
    } else {
      this.props.onBlur()
    }
  }

  editorScrollChanged (cm: CodeMirror.Editor) {
    console.log('scroll')
  }

  render () {
    return (
      <textarea ref={(input) => { this.textarea = input }} />
    )
  }
}
