/**
 * Functionality related to the actual long-polling (Commmuniucation with the AsyncRestLib-enabled backend)
 */

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
function recursiveLongPoll(serverurl, successFunction, errorFunction, currentContent) {
    console.log("Polling: "+serverurl+md5(currentContent, false, false));
    fetch(serverurl+md5(currentContent, false, false))
        .then((response) => {
            return response.json();
        })
        .then((text) => {
            console.log("Yay!");
        })
        .catch((e) => {
            console.log("Nooooo!");
            // error in e.message
        });

        // .then(reply => {
        //     console.log("1");
        //     verifyReturnCode(reply, 200);
        //     console.log("2");
        //     verifyJsonContentType(reply);
        //     console.log("3");
        //     return extractJsonObject(reply);
        // })
        // .catch(error => {
        //     console.log("...")
        //     handleNon200(error.status, serverurl, successFunction, errorFunction, currentContent)
        // })
        // .then(payloadAsObject => {
        //     currentContent = JSON.stringify(payloadAsObject);
        //     successFunction(payloadAsObject);
        //     // Next line leads to infinite loop. Server replies although hashes should mismatch.
        //    recursiveLongPoll(serverurl, successFunction, errorFunction, currentContent);
        // })
}

/**
 * Error handling for non200 return codes. In case of a timeout, keep polling. In case of any other return code, stop.
 */
function handleNon200(returnCode, serverurl, successFunction, errorFunction, currentContent) {

    console.log("Server replied with a non 200... -> "+returnCode);

    // In cas of a 408 (timeout) continue polling / wait for next update
    if (returnCode == 408) {
        recursiveLongPoll(serverurl, successFunction, errorFunction, currentContent);
    } else {
    // In any other case, stop. Something went wrong.
        errorFunction(returnCode);
    }
}
