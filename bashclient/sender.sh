#! /bin/bash
# Author: Maximilian Schiedermeier
# maximilian.schiedermeier@mail.mcgill.ca

TESTURL=http://127.0.0.1:8446/online
# test if server is online
function test_server_alive ()
{
if curl --output /dev/null --silent --head --fail "$TESTURL"; then
  server_available=true
else
  server_available=false
fi
}

test_server_alive
if [[ $server_available = true ]]; then
        echo "Server up and running. To display received messages, use \"./receiver.sh\""
	echo "Go ahead and type your messages..."
else
	echo "Server not available"
fi

while [[ $server_available = true ]]; do
	read message

	# before actually sending the message, test again if the derver is still online:
	test_server_alive
	if [[ $server_available = false ]]; then
		echo "Seems like the server was shut down meanwhile. Exiting."
		exit -1
	fi

	# looks goot, lets send the message to the server
	curl --header "Content-Type: application/json"   --request POST   --data "{\"sender\":\"Bash\",\"line\":\"$message\"}"   http://127.0.0.1:8446/sendMessage
done

exit -1


{"sender":"tt","line":"test"}
