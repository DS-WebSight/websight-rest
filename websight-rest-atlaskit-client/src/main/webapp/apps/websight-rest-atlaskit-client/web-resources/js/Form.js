import React from 'react';
import { default as AtlaskitForm, ErrorMessage, Field } from '@atlaskit/form';
import { TabItem } from '@atlaskit/tabs';
import { colors as atlaskitColors } from '@atlaskit/theme';
import styled from 'styled-components';

const ValidationFailureContainer = styled.div`
    & > * {
        color: ${atlaskitColors.R400};
    }
`;

const FormFooter = styled.div`
    display: flex;
    justify-content: flex-end;
    margin: 20px 0 30px;
`;

/**
 * It is a wrapper for Atlaskit form functionality, that takes care of validation errors.
 * It takes a list of inputs as children, eg: TextField, Select, Checkbox.
 * Each child should have attributes like: 'label', 'name', (optional) 'hideLabel'
 *
 * @param Function onSuccess    (Optional) A callback triggered on success response
 * @param Function onSubmit     A function that will be called on form submit.
 *                              onSubmit(...) is called with 4 attributes (formData, onSuccessHandler, onValidationFailureHandler, onCompleteHandler)
 *                              onCompleteHandler should be triggered always when the request finishes and is not handled either by onSuccessHandler or onValidationFailureHandler
 */
class Form extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            data: {},
            errors: {},
            submitted: false,
            refreshForm: false,
            tabs: {}
        };

        this.removeField = this.removeField.bind(this);
        this.onRequestSuccess = this.onRequestSuccess.bind(this);
        this.onRequestComplete = this.onRequestComplete.bind(this);
        this.onRequestValidationFailure = this.onRequestValidationFailure.bind(this);
        this.reset = this.reset.bind(this);
    }

    reset() {
        this.setState(prevState => ({
            data: {},
            refreshForm: !prevState.refreshForm,
            submitted: false,
            errors: {},
            tabs: {}
        }));
    }

    forceFormContentRerender() {
        this.setState((prevState) => ({ refreshForm: !prevState.refreshForm }));
    }

    onRequestSuccess(resolve, data) {
        this.onRequestComplete(resolve, true);
        if (this.props.onSuccess) {
            this.props.onSuccess(data);
        }
    }

    onRequestValidationFailure(resolve, data) {
        const errors = {};
        data.entity.forEach((field) =>
            errors[field.path] = field.message
        );
        this.onRequestComplete(resolve, false, errors);
    }

    onRequestComplete(resolve, isSuccessful, errors) {
        this.setState(
            { submitted: isSuccessful === true, errors: errors || {} },
            () => {
                resolve(errors);
                this.forceFormContentRerender();
            }
        );
    }

    onSubmit() {
        const { data } = this.state;
        const { onSubmit } = this.props;
        if (onSubmit instanceof Function) {
            this.setState({ submitted: true, errors: {} });
            return new Promise(resolve => {
                onSubmit(data,
                    (responseData) => this.onRequestSuccess(resolve, responseData),
                    (responseData) => this.onRequestValidationFailure(resolve, responseData),
                    () => this.onRequestComplete(resolve)
                );
            });
        }
        console.warn('Form should have onSubmit callback function');
    }

    removeField(name) {
        this.setState(prevState => {
            const previousData = { ...prevState.data };
            delete previousData[name];
            return { data: previousData }
        })
    }

    initStateFieldDataValue(child) {
        if (!(child.props.name in this.state.data)) {
            this.setState((prevState) => (
                { data: { [child.props.name]: child.props.defaultValue, ...prevState.data } }
            ));
        } else if (child.props.value && this.state.data[child.props.name] !== child.props.value) {
            this.setState((prevState) => (
                { data: { ...prevState.data, [child.props.name]: child.props.value } }
            ))
        }
    }

    setStateFieldDataValueAndCallOnChange(child, fieldProps, e) {
        const getValue = (event) => {
            if (event instanceof Array) {
                return event;
            } else if (event && event.target && ['checkbox', 'radio'].includes(event.target.type)) {
                return event.target.checked;
            } else if (event && event.target && ['file'].includes(event.target.type)) {
                return event.target.files[0];
            } else if (event && event.target) {
                return event.target.value;
            } else {
                return event;
            }
        }
        const value = getValue(e);
        const event = { ...e };
        this.setState(
            (prevState) => ({
                data: { ...prevState.data, [child.props.name]: value }
            }),
            () => {
                if (fieldProps.onChange) fieldProps.onChange(event);
                if (child.props.onChange) child.props.onChange(event);
                if (this.props.onFormDataChange) this.props.onFormDataChange();
            }
        )
    }

    fillStateTabsValueWithFieldsNames(child, tabLabel) {
        // will be triggered in case when Form will contain Tabs component
        if (tabLabel) {
            this.setState((prevState) => {
                if (!(prevState.tabs[tabLabel] || []).includes(child.props.name)) {
                    const children = prevState.tabs[tabLabel] || [];
                    return { tabs: { ...prevState.tabs, [tabLabel]: [...children, child.props.name] } };
                }
            })
        }
    }

    prepareField(child, tabLabel) {
        this.initStateFieldDataValue(child);
        this.fillStateTabsValueWithFieldsNames(child, tabLabel);

        return (
            <Field {...child.props} type='field' label={child.props.hideLabel ? '' : child.props.label}>
                {({ fieldProps, error }) => {
                    const stateValue = this.state.data[child.props.name];
                    let fieldTypeSpecificProps = {};
                    if (stateValue instanceof File) {
                        fieldTypeSpecificProps = { fileName: stateValue.name }
                    }
                    if (typeof stateValue == 'boolean') {
                        fieldTypeSpecificProps = { ...fieldTypeSpecificProps, isChecked: stateValue }
                    }
                    return (
                        <>
                            {
                                React.cloneElement(child, {
                                    ...fieldProps,
                                    ...fieldTypeSpecificProps,
                                    value: stateValue instanceof File ? '' : stateValue,
                                    isInvalid: error,
                                    validationState: error ? 'error' : 'default',
                                    onChange: (e) => {
                                        this.setStateFieldDataValueAndCallOnChange(child, fieldProps, e);
                                    },
                                    onCreateOption: (value) => {
                                        // will be triggered for CreatableSelect component create action
                                        this.setStateFieldDataValueAndCallOnChange(child, fieldProps, {
                                            label: value,
                                            value: value
                                        });
                                    }
                                })
                            }
                            {(error && <ErrorMessage>{error}</ErrorMessage>)}
                        </>
                    )
                }}
            </Field>
        )
    }

    setTabsValidationFailureStyle(object) {
        object.components = {
            Item: (props) => {
                if (Object.keys(this.state.errors).some(x => (this.state.tabs[props.data.label] || []).includes(x))) {
                    return <ValidationFailureContainer><TabItem {...props} /></ValidationFailureContainer>
                } else {
                    return <TabItem {...props} />
                }
            }
        }
    }

    mapArray(array, tabLabel) {
        return array.map((child) => {
            return this.mapObject(child, tabLabel || child.label);
        });
    }

    mapObject(object, tabLabel) {
        if (!object || object instanceof Function) {
            return object;
        }

        if (object.props && object.props.name && object.props.type !== 'field') {
            return React.cloneElement(this.prepareField(object, tabLabel));
        }

        const keysToCheck = ['tabs', 'content', 'props', 'children'];

        Object.keys(object).filter((key) => keysToCheck.includes(key)).forEach((key) => {
            const value = object[key];

            if (value instanceof Array) {
                object[key] = this.mapArray(value, tabLabel);
            } else {
                object[key] = this.mapObject(value, tabLabel);
            }

            if (key === 'tabs') {
                this.setTabsValidationFailureStyle(object);
            }
        });

        if (object.$$typeof === 'Symbol(react.element)') {
            return React.cloneElement(object, object.props);
        } else {
            return object
        }
    }

    submit() {
        this.form.dispatchEvent(new Event('submit', { cancelable: true }));
    }

    render() {
        return (
            <>
                <AtlaskitForm onSubmit={() => this.onSubmit()}>
                    {({ formProps }) => (
                        <form {...formProps} key={this.state.refreshForm} ref={(element) => this.form = element}>
                            {
                                React.Children.toArray(this.props.children({ submitted: this.state.submitted })).map((child) => {
                                    return this.mapObject(child);
                                })
                            }
                        </form>
                    )}
                </AtlaskitForm>
            </>
        );
    }
}

export default Form;
export { FormFooter };