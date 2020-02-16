/**
 * Functionality related to the actual long-polling (Commmuniucation with the AsyncRestLib-enabled backend)
 */
// We initialize the last message to the same default string as used on server side, to start with the identical
// broadcastContentHash on both ends. This naturally hides the dummy-init message.
var lastPolledObjectJsonString = "{\"line\":\"[INIT]\"}";

/**
 * **************************************************************************************************************
 * ** THIS IS THE ASYNC LONG POLL MAIN CONTROL LOOP THAT YOU ARE INTERESTED IN IF YOU ARE AN ASYNCRESTLIB USER **
 * **************************************************************************************************************
 *
 * Main recursive poll for updates, until the server relied with a non-200/408 return code.
 * The recursiveness is, the function considers 200 and 408 a success, that motivates enchaining with a self-call.
 * The fetch includes various other error handling. Credit for a good error-handling basis goes to:
 * -> https://css-tricks.com/using-fetch/
 * @param serverurl as the remore REST enpoint on which you want to repeatedly long-poll
 * @param successFunction as the function to execute upon every success (RT:200)
 * @param errorFunction as the function you want to execute on every non-200/non-408
 */
function recursiveLongPoll(serverurl, successFunction, errorFunction) {
    console.log("Polling: "+serverurl+md5(lastPolledObjectJsonString, false, false));
    fetch(serverurl+md5(lastPolledObjectJsonString, false, false))
        .then(handleResponse)
        .then(data => {
            // buffer the extracted message so a redundancy-omitting hash can be sent alongside future update requests
            lastPolledObjectJsonString = data;
            successFunction(data);
            recursiveLongPoll(serverurl, successFunction, errorFunction)
        })
        .catch(error => {
            handleNon200(error.status, serverurl, successFunction, errorFunction)
        })
}

/**
 * Error handling for non200 return codes. In case of a timeout, keep polling. In case of any other return code, stop.
 */
function handleNon200(returnCode, serverurl, successFunction, errorFunction) {
    if (returnCode == 408) {
        //console.log("Received a 408, commencing long polls.")
        recursiveLongPoll(serverurl, successFunction, errorFunction);
    } else {
        //console.log("Received a non 408 error message. Stopped polling.")
        errorFunction(returnCode);
    }
}

/**
 * Looks at the server-message payload and verifies if it is encoded in a recognized data-format.
 * @param response
 */
function handleResponse(response) {
    console.log(response);
    let contentType = response.headers.get('content-type')
    if (contentType.includes('application/json')) {
        return handleJSONResponse(response)
    } else if (contentType.includes('text/html')) {
        return handleTextResponse(response)
    } else if (contentType.includes('text/plain')) {
        return handleTextResponse(response)
    } else {
        // Other response types as necessary. I haven't found a need for them yet though.
        throw new Error(`Sorry, content-type ${contentType} not supported`)
    }
}

/**
 * Extract the actual HTTP message payload, assuming the reply came in JSON format.
 * @param response as the HTTP payload
 * @returns {Promise<T | never>}
 */
function handleJSONResponse(response) {
    return response.json()
        .then(json => {
            if (response.ok) {
                return json
            } else {
                return Promise.reject(Object.assign({}, json, {
                    status: response.status,
                    statusText: response.statusText
                }))
            }
        })
}

/**
 * Extract the actual HTTP message payload, assuming the reply came in http/text format.
 * @param response as the HTTP payload
 * @returns {Promise<T | never>}
 */
function handleTextResponse(response) {
    return response.text()
        .then(text => {
            if (response.ok) {
                return text
            } else {
                return Promise.reject({
                    status: response.status,
                    statusText: response.statusText,
                    err: text
                })
            }
        })
}