import RestClient from "./RestClient.js";
import fetchMock from "jest-fetch-mock";

global.fetch = fetchMock;

beforeEach(() => {
    fetch.mockClear();
});

test("Post request with custom handler", () => {
    const formData = prepareFormData("Test");
    fetch.mockResolvedValue(mock200Response("Test"));

    const client = new RestClient("websight-rest-exampleactions");
    client.post({
        action: "hello",
        data: formData,
        onSuccess: customSuccessHandler
    });
});

test("Post request with global handler", () => {
    const formData = prepareFormData("Test");
    fetch.mockResolvedValue(mock200Response("Test"));

    const client = new RestClient();
    client.setGlobalOnSuccess(customSuccessHandler);

    client.post({
        action: "hello",
        data: formData
    });
});

test("Post success request with HTML form", () => {
    const myForm = createHtmlForm("Test");
    fetch.mockResolvedValue(mock200Response("Test"));
    const client = new RestClient("websight-rest-exampleactions");

    client.post({
        action: "hello",
        data: myForm,
        onSuccess: customSuccessHandler
    })
});

test("Post success request with HTML form and resource type", () => {
    const myForm = createHtmlForm("Test");
    fetch.mockResolvedValue(mock200Response("Test"));
    const client = new RestClient("websight-rest-exampleactions");

    client.post({
        action: "hello",
        resourcePath: "content/home/page",
        data: myForm,
        onSuccess: customSuccessHandler
    });
});

test("Get success request with resource type", () => {
    const client = new RestClient("websight-rest-exampleactions");
    fetch.mockResolvedValue(mock200Response("Test"));

    client.get({
        action: "hello",
        resourcePath: "content/home/page",
        onSuccess: customSuccessHandler
    });
});

test("Get success request with path", () => {
    const client = new RestClient("websight-rest-exampleactions");
    fetch.mockResolvedValue(mock200Response("Test"));

    client.get({
        action: "hello",
        onSuccess: customSuccessHandler
    });
});

test("Get success request with path and parameter", () => {
    const client = new RestClient("websight-rest-exampleactions");
    fetch.mockResolvedValue(mock200Response("Test"));

    client.get({
        action: "hello",
        onSuccess: customSuccessHandler,
        data: {
            name: "Janusz"
        }
    });
});

test("Get success request with custom handler", () => {
    const client = new RestClient("websight-rest-exampleactions");
    fetch.mockResolvedValue(mock200Response("Test"));

    client.get({
        action: "hello",
        onSuccess: customSuccessHandler,
        onValidationFailure: customValidationFailureHandler
    });
});

test("Post request with validation failure handler", () => {
    const client = new RestClient("websight-rest-exampleactions");
    client.setGlobalOnValidationFailure(customValidationFailureHandler );
    const formData = prepareFormData("test");
    fetch.mockResolvedValue(mock200ResponseValidationFailure());

    client.post({
        name: "hello",
        data: formData
    });
});

test("Sending files example", () => {
    fetch.mockResolvedValue(mock201Response());
    const formData = new FormData();

    const photos = document.createElement("INPUT");
    photos.setAttribute("type", "file");
    photos.setAttribute("multiple", "");

    formData.append('title', 'My Vegas Vacation');
    Array.from(photos.files).forEach(file => {
        formData.append('photos', file);
    });

    const client = new RestClient("websight-rest-exampleactions");
    client.post({
        name: "hello",
        data: formData,
        onSuccess: customSuccessHandlerFilesUploaded
    });
});

test("Executing custom error handler", () => {
    fetch.mockResolvedValue(mock200ResponseError());

    const client = new RestClient("websight-rest-exampleactions");
    client.post({
        action: "hello",
        onError: customErrorHandler
    });
});

test("Executing custom global error handler", () => {
    fetch.mockResolvedValue(mock200ResponseError());

    const client = new RestClient("websight-rest-exampleactions");
    client.setGlobalOnError(customErrorHandler);
    client.post({
        action: "hello"
    });
});

test("Executing custom unexpected error handler", () => {
    fetch.mockResolvedValue(mock400ResponseNonFrameworkError());

    const client = new RestClient("websight-rest-exampleactions");
    client.post({
        action: "hello",
        onNonFrameworkError: customNonFrameworkErrorHandler
    });
});

test("Executing custom global unexpected error handler", () => {
    fetch.mockResolvedValue(mock400ResponseNonFrameworkError());

    const client = new RestClient("websight-rest-exampleactions");
    client.setGlobalOnNonFrameworkError(customNonFrameworkErrorHandler);
    client.post({
        action: "hello"
    });
});

function createHtmlForm(name) {
    const input = document.createElement("INPUT");
    input.setAttribute("type", "text");
    input.setAttribute("name", "name");
    input.setAttribute("value", name);
    const form = document.createElement("FORM");
    form.appendChild(input);
    return form;
}

function customSuccessHandler(data) {
    try {
        expect(data.message).toEqual("Hello Test");
    } catch (error) {
        console.log(error);
        fail('It should not reach here');
    }
}

function customSuccessHandlerFilesUploaded(data) {
    try {
        expect(data.message).toEqual("Files uploaded");
    } catch (error) {
        console.log(error);
        fail('It should not reach here');
    }
}

function customValidationFailureHandler (data) {
    try {
        expect(data.message).toEqual("Name is not valid");
    } catch (error) {
        console.log(error);
        fail('It should not reach here');
    }
}

function customErrorHandler(data) {
    try {
        expect(data.message).toEqual("You have no privileges");
    } catch (error) {
        console.log(error);
        fail('It should not reach here');
    }
}

function customNonFrameworkErrorHandler(error) {
    try {
        expect(error.status).toEqual(400);
    } catch (error) {
        console.log(error);
        fail('It should not reach here');
    }
}

function mock200Response(name) {
    return new Response(JSON.stringify({
        status: "SUCCESS",
        message: "Hello " + name
    }), {
        status: 200,
        statusText: "OK",
        headers: {
            "Content-Type": "application/json"
        }
    })
}

function mock201Response() {
    return new Response(JSON.stringify({
        status: "SUCCESS",
        message: "Files uploaded"
    }), {
        status: 201,
        statusText: "Created",
        headers: {
            "Content-Type": "application/json"
        }
    })
}

function mock200ResponseError() {
    return new Response(JSON.stringify({
        status: "ERROR",
        message: "You have no privileges"
    }), {
        status: 200,
        statusText: "OK",
        headers: {
            "Content-Type": "application/json"
        }
    })
}

function mock200ResponseValidationFailure() {
    return new Response(JSON.stringify({
        status: "VALIDATION_FAILURE",
        message: "Name is not valid"
    }), {
        status: 200,
        statusText: "OK",
        headers: {
            "Content-Type": "application/json"
        }
    })
}

function mock400ResponseNonFrameworkError() {
    return new Response({}, {
        status: 400,
        statusText: "Bad Request"
    })
}

function prepareFormData(name) {
    const formData = new FormData();
    formData.append("name", name);
    return formData;
}
