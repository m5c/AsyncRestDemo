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

    // start long polling for incoming chat messages
    recursiveLongPoll("getupdate?hash=", addMessageToLogAndBuffer, printErrorLog);
}

/**
 * reads the content of the message-input field, then clears it and sends the message to the server.
 */
function readThenClearAndSend() {
    let name = $('#clientName');
    let field = $('#messageField');
    sendMessage(name.val() + ": " + field.val())

    // clear the field after the message was sent
    field.val('')
}

/**
 * sends a chat text-string to the server, using the dedicated "/sendMessage" endpoint (POST)
 */
function sendMessage(message) {
    let xhr = new XMLHttpRequest();
    xhr.open("POST", "/sendMessage", true);
    xhr.setRequestHeader('Content-Type', 'text/plain');
    xhr.send(message);
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
function addMessageToLogAndBuffer(data) {
    let logger = $('#message-log');
    var newmessage = JSON.parse(data);
    logger.text(newmessage.line + "\n" + logger.text());
}

/**
 * removes all messages from log.
 */
function clearHistory() {
    let logger = $('#message-log');
    logger.text('');
}
