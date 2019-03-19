//
// src/main/typescript/notif-message.tsx
// RegEx Frontend
//
// Created on 5/30/17
//

import { PureComponent } from 'react'

interface Props {
  message: string
}

export class NotifMessage extends PureComponent<Props, {}> {
  render () {
    return (
      <div className="message">{this.props.message}</div>
    )
  }
}
