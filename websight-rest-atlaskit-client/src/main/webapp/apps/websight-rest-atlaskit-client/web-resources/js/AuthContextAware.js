import React from 'react';

import AuthContextProvider, { AUTH_CONTEXT_UPDATED } from 'websight-rest-esm-client/AuthContextProvider';

const authContextAware = (WrappedComponent) => {

    class AuthContextAware extends React.Component {

        constructor(props) {
            super(props);
            this.onContextUpdate = this.onContextUpdate.bind(this);
        }

        componentDidMount() {
            window.addEventListener(AUTH_CONTEXT_UPDATED, this.onContextUpdate);
            AuthContextProvider.getOrFetchContext()
                .then(() => undefined); // do nothing, handle changes in event listener
        }

        componentWillUnmount() {
            window.removeEventListener(AUTH_CONTEXT_UPDATED, this.onContextUpdate);
        }

        onContextUpdate() {
            this.forceUpdate();
        }

        render() {
            const { forwardedRef, ...passThroughProps } = this.props;
            return (
                <WrappedComponent
                    ref={forwardedRef}
                    {...passThroughProps}
                    authContext={AuthContextProvider.getContext()}
                />
            )
        }
    }

    return React.forwardRef((props, ref) => {
        return <AuthContextAware {...props} forwardedRef={ref} />
    });
}

export default authContextAware;