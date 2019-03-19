//
// src/main/typescript/util.ts
// RegEx Frontend
//
// Created on 2/20/17
//

import 'codemirror'

export function samePosition (a: CodeMirror.Position, b: CodeMirror.Position): boolean {
  return (a.line === b.line && a.ch === b.ch)
}

export function lessThanPosition (a: CodeMirror.Position, b: CodeMirror.Position): boolean {
  if (a.line === b.line) { return a.ch < b.ch }
  return a.line < b.line
}

export function greaterThanPosition (a: CodeMirror.Position, b: CodeMirror.Position): boolean {
  if (a.line === b.line) { return a.ch > b.ch }
  return a.line > b.line
}

export function getFirstPosition (doc: CodeMirror.Doc): CodeMirror.Position {
  let firstLine = doc.firstLine()
  let firstCh = 0

  return { line: firstLine, ch: firstCh }
}

export function getLastPosition (doc: CodeMirror.Doc): CodeMirror.Position {
  let lastLine = doc.lastLine()
  let lastCh = doc.getLine(lastLine).length

  return { line: lastLine, ch: lastCh }
}

export function getPositionBefore (doc: CodeMirror.Doc, pos: CodeMirror.Position): CodeMirror.Position {
  if (pos.ch > 0) {
    return { line: pos.line, ch: pos.ch - 1 }
  }

  if (pos.line > 0) {
    return { line: pos.line - 1, ch: doc.getLine(pos.line - 1).length }
  }

  throw new Error('cannot get position before (0:0)')
}

export function getPositionAfter (doc: CodeMirror.Doc, pos: CodeMirror.Position): CodeMirror.Position {
  if (pos.ch < doc.getLine(pos.line).length - 1) {
    return { line: pos.line, ch: pos.ch + 1 }
  }

  if (pos.line < doc.lastLine()) {
    return { line: pos.line + 1, ch: 0 }
  }

  let lastLine = doc.lastLine()
  let lastCh = doc.getLine(lastLine).length - 1
  throw new Error(`cannot get position after (${lastLine}:${lastCh})`)
}

export function onEvent (elem: Window | HTMLElement, eventName: string, cb: (MouseEvent) => void) {
  elem.addEventListener(eventName, cb)
}

export function onceEvent (elem: Window | HTMLElement, eventName: string, cb: (MouseEvent) => void) {
  let once = function (event: MouseEvent) {
    elem.removeEventListener(eventName, once)
    cb(event)
  }

  elem.addEventListener(eventName, once)
}

export function offEvent (elem: Window | HTMLElement, eventName: string, cb: (MouseEvent) => void) {
  elem.removeEventListener(eventName, cb)
}

/**
 * Other util functions.
 */

export function debounce (fn: () => void, wait: number): () => void {
  let timeout: number

  return () => {
    let later = () => {
      timeout = null
      fn.apply(fn)
    }

    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

export function formatBenchmarkFile (regex: string, matches: { start: number, end: number }[], corpus: string): string {
  return [
    regex,
    '---',
    matches.map((match) => {
      return `(${match.start}:${match.end})`
    }).join('\n'),
    '---',
    corpus,
  ].join('\n')
}
