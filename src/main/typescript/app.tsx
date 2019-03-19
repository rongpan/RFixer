//
// src/main/typescript/app.tsx
// RegEx Frontend
//
// Created on 4/7/17
//

import { post } from 'superagent'
import { Component } from 'react'
import { RegexEditor } from './regex-editor'
import { RegexEditorControls } from './regex-editor-controls'
import { RegexEditorStatus } from './regex-editor-status'
import { FixModal } from './fix-modal'
import { CorpusEditor } from './corpus-editor'
import { Button } from './button'
import { DownloadButton } from './download-button'
import { Notif } from './notif'
import { Notifs } from './notifs'
import { formatBenchmarkFile } from './util'

interface Props {
  regex: string
  corpus: string
  colors: string[]
}

interface State {
  regex: string
  corpus: string
  matches: { start: number, end: number }[]
  hasFix: boolean
  fixedRegex: string
  inError: boolean
  message: string
  notifs: {
    health: 'green' | 'yellow' | 'red',
    message: string
  }[]
}

export class App extends Component<Props, State> {
  constructor (props) {
    super(props)

    this.state = {
      regex: this.props.regex,
      corpus: this.props.corpus,
      matches: [],
      hasFix: false,
      fixedRegex: '',
      inError: false,
      message: '',
      notifs: [],
    }

    this.handleRegexChange     = this.handleRegexChange.bind(this)
    this.handleRequestFix      = this.handleRequestFix.bind(this)
    this.handleAcceptFix       = this.handleAcceptFix.bind(this)
    this.handleRejectFix       = this.handleRejectFix.bind(this)
    this.handleCorpusChange    = this.handleCorpusChange.bind(this)
    this.handleMatchesChange   = this.handleMatchesChange.bind(this)
    this.handleEmptyRegex      = this.handleEmptyRegex.bind(this)
    this.handleInfiniteMatches = this.handleInfiniteMatches.bind(this)
    this.handleBrokenRegex     = this.handleBrokenRegex.bind(this)
  }

  handleRegexChange (newRegex: string): void {
    this.setState({
      regex: newRegex,
    })
  }

  handleRequestFix () {
    let matches = this.state.matches.map((match) => ({
      left: match.start,
      right: match.end,
    }))

    this.setState({
      notifs: [{
        health: 'yellow',
        message: 'Working',
      }],
    })

    post('/api/fix').send({
      regex: this.state.regex,
      ranges: matches,
      corpus: this.state.corpus,
    }).end((err, res) => {
      if (err != null || res.status !== 200) {
        let message = 'Server error'

        switch (res.status) {
          case 400:
            message = 'Malformed request'
            break
          case 408:
            message = 'Repair timeout'
            break
        }

        this.setState({
          notifs: [{
            health: 'red',
            message: message,
          }]
        })
        throw new Error('did not receive fix from server')
      } else {
        let fixed = JSON.parse(res.text).fix
        if (typeof fixed !== 'string') {
          this.setState({
            notifs: [{
              health: 'red',
              message: 'Unable to make repair',
            }],
          })
        }

        fixed = fixed.replace(/\\\\/g, '\\')

        this.setState({
          hasFix: true,
          fixedRegex: fixed,
          notifs: [],
        })
      }
    })
  }

  handleAcceptFix () {
    this.setState({
      regex: this.state.fixedRegex,
      hasFix: false,
      fixedRegex: '',
    })
  }

  handleRejectFix () {
    this.setState({
      hasFix: false,
      fixedRegex: '',
    })
  }

  handleCorpusChange (newCorpus: string): void {
    this.setState({
      corpus: newCorpus,
    })
  }

  handleMatchesChange (matches: { start: number, end: number }[]): void {
    this.setState({
      matches: matches,
      inError: false,
      message: `${matches.length} matches`,
    })
  }

  handleEmptyRegex (): void {
    this.setState({
      inError: false,
      message: 'Empty',
    })
  }

  handleInfiniteMatches (): void {
    this.setState({
      inError: true,
      message: 'Infinite',
    })
  }

  handleBrokenRegex (): void {
    this.setState({
      inError: true,
      message: 'Error',
    })
  }

  createDownloadLink (): string {
    const benchmarkContents = formatBenchmarkFile(this.state.regex, this.state.matches, this.state.corpus)
    const encodedContents = btoa(benchmarkContents)
    return `data:text/plain;base64,${encodedContents}`
  }

  render () {
    const downloadLink = this.createDownloadLink()

    return (
      <div>
        <RegexEditor regex={this.state.regex} onRegexChange={this.handleRegexChange}>
          <RegexEditorControls>
            <Button glyph="?" color="blue" onClick={this.handleRequestFix} />
            <DownloadButton glyph="\u2193" color="blue" href={downloadLink} name="benchmark.txt" />
            <RegexEditorStatus inError={this.state.inError}>
              {this.state.message}
            </RegexEditorStatus>
          </RegexEditorControls>
          {(this.state.hasFix) && (
            <FixModal regex={this.state.fixedRegex}>
              <Button glyph="\u2713" color="green" onClick={this.handleAcceptFix} />
              <Button glyph="\u2717" color="red" onClick={this.handleRejectFix} />
            </FixModal>
          )}
        </RegexEditor>
        <CorpusEditor
          regex={this.state.regex}
          corpus={this.state.corpus}
          colors={this.props.colors}
          onCorpusChange={this.handleCorpusChange}
          onMatchesChange={this.handleMatchesChange}
          onEmptyRegex={this.handleEmptyRegex}
          onInfiniteMatches={this.handleInfiniteMatches}
          onBrokenRegex={this.handleBrokenRegex} />
        <Notifs>
          {this.state.notifs.map(notif => {
            return <Notif health={notif.health} message={notif.message} />
          })}
        </Notifs>
      </div>
    )
  }
}
