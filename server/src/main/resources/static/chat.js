/**
 * Main function. Called when the page is loaded.
 * Does a couple of things:
 * -> Verify if the server API is reachable. If yes, hide the corresponding error message. By default the error message
 * is visible, this is a good practice as it indicates something went wrong if, e.g. the sideloading of the required
 * JS libraries failed.
 * -> Activate the send button, by attaching a function that reads out the input fields and sends the content to the
 * server.
 * -> Register the same functionality for "return"-key pressed in the charmessage input field.
 * -> Register a function to clear the message log, to the "clear"-button
 * -> Finally start looped long-polling for message updates.
 */
function enableJSElements() {
    fetch('online/')
        .then(error => $('#notonline').addClass('d-none'));

    // register callback function for message send button
    $('#send-button').on('click', readThenClearAndSend);

    // register same callback for enter on messagefield
    $('#messageField').keypress(function (event) {
        var keycode = (event.keyCode ? event.keyCode : event.which);
        if (keycode == '13') {
            readThenClearAndSend();
        }
    });

    // register clear button
    $('#clear-button').on('click', clearHistory);

    // observe the ARL resource for messages and register a handler for status changes.
    observeResource("getupdate?hash=", addMessageToLogAndBuffer, printErrorLog, "");
}

/**
 * reads the content of the message-input field, then clears it and sends the message to the server.
 */
function readThenClearAndSend() {
    let name = $('#clientName');
    let field = $('#messageField');
    let message = { "sender":name.val(), "line": field.val()};
    console.log(message);
    postToApi(message, "/sendMessage");

    // clear the field after the message was sent
    field.val('')
}

/**
 * This method gets called if a long poll was answered with a non-200/408. We therefore clear the messages and display
 * an error message.
 * @param returnCode
 */
function printErrorLog(returnCode) {
    console.log("Server replied with: " + returnCode);
    clearHistory();
    $('#notonline').removeClass('d-none');
}

/**
 * called if an update was retrieved from the server, successfully. The retrieved message is then interpreted as a JSON
 * and the embedded chat-message is added to the log.
 * @param data as the raw json string of the server reply-body
 */
function addMessageToLogAndBuffer(newmessage) {

    console.log("adding to buffer: "+newmessage);
    let logger = $('#message-log');
    if (logger.text() === "") {
        logger.text(newmessage.line);
    } else {
        logger.text(logger.text() + "\n" + newmessage.line);
    }

    // scroll to bottom
    $('#message-log').scrollTop($('#message-log')[0].scrollHeight);}

/**
 * removes all messages from log.
 */
function clearHistory() {
    let logger = $('#message-log');
    logger.text('');
}
