import React from 'react';
import { AutoDismissFlag } from '@atlaskit/flag';

export default class NotificationFlag extends React.Component {

    constructor(props) {
        super(props);
        const { id, icon, title, message, actions, iconStyle } = this.props;
        this.state = {
            id: id,
            icon: icon,
            iconStyle: iconStyle,
            title: title || '',
            message: message || '',
            actions: actions || []
        };
    }

    getIcon() {
        return <i className='material-icons' style={this.state.iconStyle}>{this.state.icon}</i>
    }

    render() {
        const actions = this.state.actions.map(action => {
            const onClick = action.onClick;
            action.onClick = () => {
                onClick();
                this.props.handleDismiss();
            }
            return action;
        });
        
        return (
            <AutoDismissFlag
                id={this.state.id}
                title={this.state.title}
                description={<div style={{ overflow: 'auto', 'max-height': '100px' }}>{this.state.message}</div>}
                key={this.state.id}
                icon={this.getIcon()}
                actions={actions}
            />
        );
    }
}