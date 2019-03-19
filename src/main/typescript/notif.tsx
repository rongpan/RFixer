//
// src/main/typescript/notif.tsx
// RegEx Frontend
//
// Created on 5/30/17
//

import { PureComponent } from 'react'
import { NotifHealth } from './notif-health'
import { NotifMessage } from './notif-message'

interface Props {
  health: 'green' | 'yellow' | 'red'
  message: string
}

export class Notif extends PureComponent<Props, {}> {
  render () {
    return (
      <div className="notif">
        <NotifHealth health={this.props.health} />
        <NotifMessage message={this.props.message} />
      </div>
    )
  }
}
