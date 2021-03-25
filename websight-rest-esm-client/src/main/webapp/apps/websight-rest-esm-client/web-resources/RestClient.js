const VALID_REST_RESPONSE_STATUSES = ['SUCCESS', 'FAILURE', 'VALIDATION_FAILURE', 'ERROR'];

const REQUEST_HEADER_EXTENDERS = {
    Accept: {
        methods: ['POST', 'GET'],
        value: () => {
            return 'application/json';
        }
    }
};

class RestClient {

    constructor(bundleName) {
        this.bundleName = bundleName;
        this.handlers = handlersInstance;
    }

    /**
     * JSON framework response handler.
     * @callback RestClient~frameworkResponseHandler
     * @param {Object} data
     */

    /**
     * Non framework error response handler.
     * @callback RestClient~nonFrameworkErrorResponseHandler
     * @param {Response|any} data
     */

    /**
     * Executes action as a GET request.
     *
     * @param {Object} action - Action to execute.
     * @param {string} action.action - Action name. Ignored if `action.resourcePath` is present.
     * @param {string} action.resourcePath - Sling resource path.
     * @param {Object} action.parameters - GET request URL parameters, eg. `{ param1: value1, param2: value2 }`.
     * @param {RestClient~frameworkResponseHandler} action.onSuccess - Action success handler executed when response JSON `status` field
     *         is `SUCCESS`. If not set, then `RestClient.handlers.successHandler(data)` will be used by default.
     * @param {RestClient~frameworkResponseHandler} action.onFailure - Action failure handler executed when response JSON `status` field
     *         is `FAILURE`. If not set, then `RestClient.handlers.failureHandler(data)` will be used by default.
     * @param {RestClient~frameworkResponseHandler} action.onValidationFailure - Action validation failure handler executed when response
     *         JSON `status` field is `VALIDATION_FAILURE`. If not set, then first defined handler of the following is executed:
     *         <ul>
     *             <li>`RestClient.handlers.validationFailureHandler`</li>
     *             <li>`action.onFailure`</li>
     *             <li>`RestClient.handlers.failureHandler`</li>
     *         </ul>
     * @param {RestClient~frameworkResponseHandler} action.onError - Action error handler executed when response JSON `status` field is `ERROR`.
     *         If not set, then first defined handler of the following is executed:
     *         <ul>
     *             <li>`RestClient.handlers.errorHandler`</li>
     *             <li>`action.onFailure`</li>
     *             <li>`RestClient.handlers.failureHandler`</li>
     *         </ul>
     * @param {RestClient~nonFrameworkErrorResponseHandler} action.onNonFrameworkError - Non framework error handler executed when response
     *         is unsupported by `RestClient`. If not set, then `RestClient.handlers.nonFrameworkErrorHandler(data)` will be used by default.
     *         Response is unsupported when:
     *         <ul>
     *             <li>is not JSON</li>
     *             <li>is JSON, but `status` field is missing or is not in: `SUCCESS`, `FAILURE`, `VALIDATION_FAILURE`, `ERROR`</li>
     *             <li>is JSON, but doesn't have required properties (`status`, `authContext`, `authContext.userId`)</li>
     *             <li>communication error occurred</li>
     *         </ul>
     */
    get(action) {
        if (action.data) {
            console.warn('The formData value is ignored (GET method does not support data sent in request body');
        }
        request(action, this.buildGetFetchParameters(action), this.bundleName, this.handlers);
    }

    /**
     * Builds `fetch()` method parameters for a GET request.
     *
     * @param {Object} action - Action details to build parameters for.
     * @param {string} action.action - Action name. Ignored if `action.resourcePath` is present.
     * @param {string} action.resourcePath - Sling resource path.
     * @param {Object} action.parameters - GET request URL parameters, eg. `{ param1: value1, param2: value2 }`.
     * @returns {Object} - Object with `url` and `options` parameters ready for use in `fetch(result.url, result.options)`.
     */
    buildGetFetchParameters(action) {
        return {
            url: prepareUrl(action.action, action.resourcePath, this.bundleName) + buildQueryString(action.parameters),
            options: {
                method: 'GET',
                headers: getHeaders('GET'),
                credentials: getCredentialsConfig(),
                signal: action.signal
            }
        };
    }

    /**
     * Executes action as a POST request.
     *
     * @param {Object} action - Action to execute.
     * @param {string} action.action - Action name. Ignored if `action.resourcePath` is present.
     * @param {string} action.resourcePath - Sling resource path.
     * @param {(HTMLFormElement|FormData|Object)} action.data - An HTML `<form>` element or `FormData`
     *         or plain object, eg. `{ key1: value1, key2: value2 }`.
     * @param {RestClient~frameworkResponseHandler} action.onSuccess - Action success handler executed when response JSON `status` field
     *         is `SUCCESS`. If not set, then `RestClient.handlers.successHandler(data)` will be used by default.
     * @param {RestClient~frameworkResponseHandler} action.onFailure - Action failure handler executed when response JSON `status` field
     *         is `FAILURE`. If not set, then `RestClient.handlers.failureHandler(data)` will be used by default.
     * @param {RestClient~frameworkResponseHandler} action.onValidationFailure - Action validation failure handler executed when response
     *         JSON `status` field is `VALIDATION_FAILURE`. If not set, then first defined handler of the following is executed:
     *         <ul>
     *             <li>`RestClient.handlers.validationFailureHandler`</li>
     *             <li>`action.onFailure`</li>
     *             <li>`RestClient.handlers.failureHandler`</li>
     *         </ul>
     * @param {RestClient~frameworkResponseHandler} action.onError - Action error handler executed when response JSON `status` field is `ERROR`.
     *         If not set, then first defined handler of the following is executed:
     *         <ul>
     *             <li>`RestClient.handlers.errorHandler`</li>
     *             <li>`action.onFailure`</li>
     *             <li>`RestClient.handlers.failureHandler`</li>
     *         </ul>
     * @param {RestClient~nonFrameworkErrorResponseHandler} action.onNonFrameworkError - Non framework error handler executed when response
     *         is unsupported by `RestClient`. If not set, then `RestClient.handlers.nonFrameworkErrorHandler(data)` will be used by default.
     *         Response is unsupported when:
     *         <ul>
     *             <li>is not JSON</li>
     *             <li>is JSON, but `status` field is missing or is not in: `SUCCESS`, `FAILURE`, `VALIDATION_FAILURE`, `ERROR`</li>
     *             <li>is JSON, but doesn't have required properties (`status`, `authContext`, `authContext.userId`)</li>
     *             <li>communication error occurred</li>
     *         </ul>
     */
    post(action) {
        request(action, this.buildPostFetchParameters(action), this.bundleName, this.handlers);
    }

    /**
     * Builds `fetch()` method parameters for a POST request.
     *
     * @param {Object} action - Action details to build parameters for.
     * @param {string} action.action - Action name. Ignored if `action.resourcePath` is present.
     * @param {Object} action.resourcePath - Sling resource path.
     * @param {(HTMLFormElement|FormData|Object)} action.data - An HTML `<form>` element or `FormData`
     *         or plain object, eg. `{ key1: value1, key2: value2 }`.
     * @returns {Object} - Object with `url` and `options` parameters ready for use in `fetch(result.url, result.options)`.
     */
    buildPostFetchParameters(action) {
        return {
            url: prepareUrl(action.action, action.resourcePath, this.bundleName),
            options: {
                method: 'POST',
                headers: getHeaders('POST'),
                body: buildFormData(action.data),
                credentials: getCredentialsConfig(),
                signal: action.signal
            }
        };
    }

    setGlobalOnSuccess(value) {
        this.handlers.successHandler = value;
    }

    setGlobalOnFailure(value) {
        this.handlers.failureHandler = value;
    }

    setGlobalOnValidationFailure(value) {
        this.handlers.validationFailureHandler = value;
    }

    setGlobalOnError(value) {
        this.handlers.errorHandler = value;
    }

    setGlobalOnNonFrameworkError(value) {
        this.handlers.nonFrameworkErrorHandler = value;
    }
}

class Handlers {

    constructor() {
        this.successHandler = function (data) {
            console.warn('RestClient default success handler: ignoring response with data: %o', data);
        };
        this.failureHandler = function (data) {
            console.warn('RestClient default failure handler: ignoring response with data: %o', data);
        }
        this.validationFailureHandler = null;
        this.errorHandler = null;
        this.nonFrameworkErrorHandler = function (error) {
            console.warn('RestClient default non framework error handler: ignoring error: %o', error);
        };
    }
}

const handlersInstance = new Handlers();

function request(action, fetchParams, bundleName, handlers) {
    const always = () => {
        try {
            if (action.always) {
                action.always();
            }
        } catch (error) {
            console.error(error);
        }
    };
    fetch(fetchParams.url, fetchParams.options)
        .then(response => {
            always();
            let handler = null;
            const contentType = response.headers.get('content-type'); // fetch api returns headers names with lowercase
            // header contains also encoding: application/json;charset=utf-8
            if (contentType && contentType.startsWith('application/json')) {
                response.json().then(data => {
                    Object.freeze(data);
                    if (isValidRestResponse(data)) {
                        if (data.status === 'SUCCESS') {
                            handler = action.onSuccess || handlers.successHandler;
                        } else if (data.status === 'FAILURE') {
                            handler = action.onFailure || handlers.failureHandler;
                        } else if (data.status === 'VALIDATION_FAILURE') {
                            handler = action.onValidationFailure || handlers.validationFailureHandler || action.onFailure || handlers.failureHandler;
                        } else if (data.status === 'ERROR') {
                            handler = action.onError || handlers.errorHandler || action.onFailure || handlers.failureHandler;
                        }
                        setAuthContext(data.authContext);
                        handler(data);
                    } else {
                        handler = action.onNonFrameworkError || handlers.nonFrameworkErrorHandler;
                        handler(data);
                    }
                }).catch(error => {
                    handler = action.onNonFrameworkError || handlers.nonFrameworkErrorHandler;
                    handler(error);
                });
            } else {
                handler = action.onNonFrameworkError || handlers.nonFrameworkErrorHandler;
                handler(response);
            }
        })
        .catch(error => {
            always();
            const handler = action.onNonFrameworkError || handlers.nonFrameworkErrorHandler;
            handler(error);
        });
}

function prepareUrl(action, resourcePath, bundleName) {
    if (resourcePath) {
        return `${resourcePath}.${bundleName}.${action}.action`;
    }
    return `/apps/${bundleName}/bin/${action}.action`;
}

function buildFormData(data) {
    if (typeof data !== 'object') {
        console.warn('The data value need to have type of \'object\' (eg: {}, FormData, HTMLFormElement)');
        return;
    }

    let formData;
    if (data instanceof FormData) {
        formData = data;
    } else if (data instanceof HTMLFormElement) {
        formData = new FormData(data);
    } else {
        formData = new FormData();
        Object.keys(data).forEach(key => {
            if (typeof data[key] === 'boolean') {
                formData.append(key, JSON.stringify(data[key]))
            } else if (data[key] instanceof Array) {
                data[key].forEach(value =>
                    formData.append(key, value))
            } else if (data[key] !== undefined && data[key] !== null) {
                formData.append(key, data[key])
            }
        }
        );
    }
    return formData;
}

function buildQueryString(parameters) {
    if (parameters && Object.entries(parameters).length !== 0) {
        return '?' + objectToQueryString(parameters);
    }
    return '';
}

function objectToQueryString(obj) {
    return Object.entries(obj)
        .filter(([, value]) => value != null)
        .map(([key, value]) => {
            if (value instanceof Array) {
                if (!value.length) return;
                return value
                    .map(arrayItem => [key, arrayItem].join('='))
                    .join('&');
            } else {
                return [key, value].map(encodeURIComponent).join('=');
            }
        })
        .filter((item) => item)
        .join('&');
}

function getCredentialsConfig() {
    if (location.hostname === 'localhost') {
        return 'include'
    } else {
        return 'same-origin'
    }
}

function getHeaders(method) {
    const result = {};
    for (const property in REQUEST_HEADER_EXTENDERS) {
        const extender = REQUEST_HEADER_EXTENDERS[property];
        if (extender.methods.includes(method)) {
            result[property] = extender.value();
        }
    }
    return result;
}

function isValidRestResponse(data) {
    return data && VALID_REST_RESPONSE_STATUSES.includes(data.status) && data.authContext && data.authContext.userId;
}

const AUTH_CONTEXT_UPDATED = 'websight.rest.auth.contextUpdate';

const ANONYMOUS_USER_ID = 'anonymous';

function dispatchAuthContextUpdatedEvent(data) {
    const authContextUpdated = new CustomEvent(AUTH_CONTEXT_UPDATED, {
        detail: {
            authContext: data
        }
    });
    window.dispatchEvent(authContextUpdated);
}

function setAuthContext(newContextData) {
    const { userId } = newContextData;
    if (currentAuthContext.userId !== userId) {
        const newContext = new AuthContext(userId);
        Object.freeze(newContext);
        currentAuthContext = newContext;
        dispatchAuthContextUpdatedEvent(newContext);
    }
}

function getAuthContext() {
    return currentAuthContext;
}

class AuthContext {

    constructor(userId) {
        this.userId = userId;
    }

    /**
     * @returns {boolean} - true if userId is not empty and not equal to 'anonymous', false otherwise
     */
    isLoggedIn() {
        return (!!this.userId) && (this.userId !== ANONYMOUS_USER_ID);
    }

    /**
     * @returns {boolean} - true if userId is not empty, false otherwise
     */
    isInitialized() {
        return !!this.userId;
    }
}

let currentAuthContext = Object.freeze(new AuthContext());

export default RestClient;
export { REQUEST_HEADER_EXTENDERS };
export { getAuthContext };
export { AUTH_CONTEXT_UPDATED };
