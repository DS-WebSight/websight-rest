import RestClient, { getAuthContext } from './RestClient.js';

const IS_AUTHENTICATED_CALL_TIMEOUT = 100;

class AuthContextProvider {
    constructor() {
        this.client = new RestClient('websight-rest-framework');
    }

    /**
     * Returns AuthContext currently available in RestClient. Returned context may not be initialized (not fetched from the Rest endpoint yet).
     * Client code is responsible for checking if the context is valid (call context.isInitialized()).
     */
    getContext() {
        return getAuthContext();
    }

    /**
     * Returns Promise with AuthContext. If no valid (initialized) context is available at the time of calling, dummy rest call is made to ensure its presence.
     * AuthContext may not be initialized only if it's not possible to make a valid rest call (i.e. when network error occurs).
     */
    getOrFetchContext() {
        return new Promise((resolve) => {
            let authContext = getAuthContext();
            if (authContext.isInitialized()) {
                resolve(authContext);
                return;
            }

            const callActionIfStillNotPresent = () => {
                authContext = getAuthContext();
                if (authContext.isInitialized()) {
                    resolve(authContext);
                    return;
                }

                const onComplete = () => {
                    authContext = getAuthContext();
                    resolve(authContext);
                };
                this.checkAuthentication(onComplete, onComplete);
            }

            setTimeout(callActionIfStillNotPresent, IS_AUTHENTICATED_CALL_TIMEOUT);
        });
    }

    checkAuthentication(onSuccess, onFailure) {
        this.client.get({
            action: 'check-authentication',
            onSuccess: onSuccess,
            onValidationFailure: onFailure,
            onFailure: onFailure,
            onError: onFailure,
            onNonFrameworkError: onFailure
        })
    }
}

export default new AuthContextProvider();
export { AUTH_CONTEXT_UPDATED } from './RestClient.js';
