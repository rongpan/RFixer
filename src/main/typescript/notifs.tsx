import { PureComponent } from 'react'
import { Notif } from './notif'

export class Notifs extends PureComponent<{}, {}> {
  render () {
    return (
      <div className="notifs">
        {this.props.children}
      </div>
    )
  }
}
