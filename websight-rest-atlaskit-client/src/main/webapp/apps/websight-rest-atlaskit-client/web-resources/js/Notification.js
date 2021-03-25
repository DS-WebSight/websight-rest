import React from 'react';
import ReactDOM from 'react-dom';
import { FlagGroup } from '@atlaskit/flag';
import { colors } from '@atlaskit/theme';

import NotificationFlag from './components/NotificationFlag.js';

const handlers = [
    { type: 'success', icon: 'check_circle', iconStyle: { color: colors.G400 }, defaultTitle: 'Success' },
    { type: 'info', icon: 'info', iconStyle: { color: colors.N500 }, defaultTitle: 'Information' },
    { type: 'warning', icon: 'warning', iconStyle: { color: colors.Y200 }, defaultTitle: 'Oops, something went wrong' },
    {
        type: 'error', icon: 'error', iconStyle: { color: colors.R400 }, defaultTitle: 'We are having an issue',
        defaultMessage: 'Please try again. If this error persists, please contact the site administrator.'
    }
];

class Notification extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            flags: []
        }
        this.errorNotification = this.errorNotification.bind(this);
        this.successNotification = this.successNotification.bind(this);
        this.warningNotification = this.warningNotification.bind(this);
        this.infoNotification = this.infoNotification.bind(this);
        this.handleDismiss = this.handleDismiss.bind(this);
    }

    errorNotification(title, message, actions) {
        this.addFlag(title, message, actions, this.findHandler('error'));
    }

    warningNotification(title, message, actions) {
        this.addFlag(title, message, actions, this.findHandler('warning'));
    }

    successNotification(title, message, actions) {
        this.addFlag(title, message, actions, this.findHandler('success'));
    }

    infoNotification(title, message, actions) {
        this.addFlag(title, message, actions, this.findHandler('info'));
    }

    findHandler(handlerType) {
        return handlers.find(handler => handler.type === handlerType);
    }

    handleDismiss() {
        this.setState(prevState => ({
            flags: prevState.flags.slice(1)
        }));
    }

    addFlag(title, message, actions, handler) {
        const newFlagId = this.state.flags.length + 1;
        const flag = new NotificationFlag({
            id: newFlagId,
            icon: handler.icon,
            iconStyle: handler.iconStyle,
            title: title || handler.defaultTitle,
            message: message || handler.defaultMessage,
            actions: actions,
            handleDismiss: this.handleDismiss
        });

        this.setState((prevState) => {
            const flags = [flag, ...prevState.flags];
            return { flags };
        });
    }

    render() {
        return (
            <FlagGroup onDismissed={this.handleDismiss} >
                {this.state.flags.map(flag => flag.render())}
            </FlagGroup>
        );
    }
}

const NOTIFICATIONS_ELEMENT_ID = 'websight-rest-notifications';
let instanceReference = {};

const findNotificationsContainer = () => document.getElementById(NOTIFICATIONS_ELEMENT_ID);

const createNotificationsContainer = () => {
    const div = document.createElement('div');
    div.setAttribute('id', NOTIFICATIONS_ELEMENT_ID);
    document.body.appendChild(div);
    return div;
}

if (!findNotificationsContainer()) {
    const notificationDiv = createNotificationsContainer();
    ReactDOM.render(<Notification ref={(element) => instanceReference = element} />, notificationDiv);
}

export const { successNotification, infoNotification, warningNotification, errorNotification } = instanceReference;
