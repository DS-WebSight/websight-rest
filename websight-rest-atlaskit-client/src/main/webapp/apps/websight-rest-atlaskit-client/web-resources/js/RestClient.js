import FrameworkRestClient from 'websight-rest-esm-client/RestClient';

import { errorNotification, successNotification, warningNotification } from './Notification.js';

const extendActionWithNotifications = (action) => ({
    ...action,
    onSuccess: (data) => {
        if (data.message) {
            let notificationData = {};
            if (action.successNotificationDataProcessor) {
                notificationData = action.successNotificationDataProcessor(data);
            }
            if (notificationData) {
                const { message, messageDetails, actions } = {
                    message: data.message,
                    messageDetails: data.messageDetails,
                    ...notificationData
                };
                successNotification(message, messageDetails, actions);
            }
        }
        if (action.onSuccess) action.onSuccess(data);
    },
    onFailure: (data) => {
        if (data.message) warningNotification(data.message, data.messageDetails);
        if (action.onFailure) action.onFailure(data);
    },
    onValidationFailure: (data) => {
        // forms support - display notification only if onValidationFailure is not given
        // to avoid showing form validation messages and notification
        if (action.onValidationFailure) action.onValidationFailure(data);
        else warningNotification('Request is not valid',
            'Please try again. If this issue persists, please contact the site administrator.');
    },
    onError: (data) => {
        if (data.message) errorNotification(data.message, data.messageDetails);
        if (action.onError) action.onError(data);
    },
    onNonFrameworkError: (error) => {
        if (error.redirected && error.url) {
            const url = new URL(error.url);
            if (url.searchParams.get('j_reason') !== null) {
                warningNotification('Your session has been terminated', 'Please log in', [{
                    content: 'Go to login page',
                    href: url.origin + url.pathname + '?resource=' + window.location.pathname
                }]);
            }
        } else {
            console.warn('Atlaskit RestClient onNonFrameworkError', error);
            errorNotification('Unexpected error occurred',
                'Please try again. If this error persists, please contact the site administrator.');
            if (action.onNonFrameworkError) action.onNonFrameworkError(error);
        }
    }
});

export default class RestClient {
    constructor(bundleName) {
        this.restClient = new FrameworkRestClient(bundleName);
    }

    get(config) {
        this.restClient.get(extendActionWithNotifications(config));
    }

    post(config) {
        this.restClient.post(extendActionWithNotifications(config));
    }

    buildGetFetchParameters(action) {
        return this.restClient.buildGetFetchParameters(action);
    }

    buildPostFetchParameters(action) {
        return this.restClient.buildPostFetchParameters(action);
    }
}

export { AUTH_CONTEXT_UPDATED } from 'websight-rest-esm-client/RestClient';
