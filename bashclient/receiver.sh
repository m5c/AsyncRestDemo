#! /bin/bash
# Author: Maximilian Schiedermeier
# maximilian.schiedermeier@mail.mcgill.ca

# test if MD5 command is installed on system:
command -v md5 >/dev/null 2>&1 || { echo "md5 command not installed on your system! Exiting." >&2; exit 1; }

TESTURL=http://127.0.0.1:8446/online
URL=http://127.0.0.1:8446/getupdate
# Note: To enable a filtertransformer that on server side omits all messages not containing a specific string, e.g. "xyz", switch to the following URL:
# URLhttp://127.0.0.1:8446/getupdate/xyz

# For every update request, we join the hash of the last received message as additional parameter. This ensures that redundant messages are filtered by the server, and that no update is missed.
# Init hash with the MD5 of the server's default content. The effect is that this client will not display the dummy init message: "[INIT]"
HASH=aa03c97192bd912b2bd40c1094123089

# test if server is online
if curl --output /dev/null --silent --head --fail "$TESTURL"; then
  echo "Server up and running, this Bash-client will print arising updates. To send messages, use \"./sender.sh\""
else
  echo "Server unreachable at $URL :-("
  exit
fi

# in loop check for updates. Each iteration runs until new timeout or new content delivered
http_status=200
while [[ $http_status = 200 || $http_status = 408 ]]; do
    out=$(curl -k --silent -L -w "\n%{http_code}" $URL?hash=$HASH)
    
    # store retrieved status code and message payload in variables
    http_status="${out##*$'\n'}"
    http_content="${out%$'\n'*}"

    # only update hash / show actual server updates if the server replied with "200/OK" 
    if [[ $http_status = 200 ]]; then
    
        # update HASH so we only get status updates that are actually new content
        HASH=$(md5 -qs "$http_content")

	# print content
        echo $http_content | sed  's/:/\'$'\n/' | grep -A1 line | grep -v line | sed 's/}//' | sed s/\"//g
    fi
done
	
# server shot down or requested to stop polling.
echo "No more updates. Exiting."
exit -1
