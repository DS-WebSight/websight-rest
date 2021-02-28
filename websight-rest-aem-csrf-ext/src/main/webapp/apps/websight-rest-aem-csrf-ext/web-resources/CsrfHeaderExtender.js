import { REQUEST_HEADER_EXTENDERS } from 'websight-rest-esm-client/RestClient';
import { AUTH_CONTEXT_UPDATED } from 'websight-rest-esm-client/AuthContextProvider';

const HEADER_NAME = 'CSRF-Token';
const TOKEN_SERVLET = '/libs/granite/csrf/token.json';
let token;
let firstAuthContextUpdate = true;

REQUEST_HEADER_EXTENDERS[HEADER_NAME] = {
    methods: ['POST'],
    value: () => {
        // return token or block request when waiting for initial token
        return token || getTokenSync();
    }
};

// If auth context is updated for the n-th time (where n>1), the token request is explicitly triggered.
window.addEventListener(AUTH_CONTEXT_UPDATED, () => {
    if(!firstAuthContextUpdate) {
        getTokenSync();
    } else {
        firstAuthContextUpdate = false;
    }
});

function getTokenAsync() {
    const xhr = new XMLHttpRequest();
    return new Promise((resolve, reject) => {
        xhr.onreadystatechange = function () {
            // 0 HTTP status code is returned on page refresh while waiting for a call
            if (xhr.readyState !== 4 || xhr.status === 0) return;
            if (xhr.status >= 200 && xhr.status < 300) {
                resolve(parseResponseAndSetToken(xhr));
            } else {
                window.console && console.warn('Cannot fetch CSRF token. Invalid HTTP code: ' + xhr.status);
                reject(xhr.responseText);
            }
        };
        xhr.open('GET', TOKEN_SERVLET, true);
        xhr.send();
    });
}

function getTokenSync() {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', TOKEN_SERVLET, false);
    xhr.send();
    return parseResponseAndSetToken(xhr);
}

function parseResponseAndSetToken(xhr) {
    try {
        token = JSON.parse(xhr.responseText).token;
        return token;
    } catch (ex) {
        window.console && console.warn('Cannot fetch CSRF token. Exception while parsing response.');
    }
}

// Invoke and schedule
getTokenAsync();
setInterval(getTokenAsync, 300000);