var stompClient = null;

$(document).ready(function() {
    console.log("Index page is ready");
    connectToOurWebSocket();
});

function connectToOurWebSocket() {
    var socket = new SockJS('/our-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/messages', function (message) {
            outputConsoleMessage(JSON.parse(message.body).content);
        });
    });
}

//open websocket connection
function openWebSocket() 
{
    const http = new XMLHttpRequest()

    http.open("GET", "http://localhost:8081/api/kraken/establish")
    http.send()

    http.onload = () =>
    {
         response = http.responseText
         if (response == "The Connection is already established")
        {
             alert(response)
        }
        else
        {
            outputConsoleMessage(http.responseText)
        }
    }
}

//function to send a message to the websocket server
function sendWebSocketMessage() 
{
     var webSocketMessage = document.getElementById("webSocketMessageInput").value;

     outputConsoleMessage("Sent Message To Server: " + webSocketMessage);

     const http = new XMLHttpRequest()
     http.open("GET", "http://localhost:8081/api/kraken/send")
     http.send()

     http.onload = () =>
         {
              response = http.responseText
              if (response == "You have to establish a connection first!!!")
             {
                  alert(response)
             }
             else
             {
                 outputConsoleMessage(http.responseText)
             }
         }
}

//close the websocket connection
function closeWebSocket() 
{
    const http = new XMLHttpRequest()

    http.open("GET", "http://localhost:8081/api/kraken/close")
    http.send()

    http.onload = () =>
    {
         response = http.responseText
         if (response == "The Connection is already closed")
        {
             alert(response)
        }
        else
        {
            outputConsoleMessage(http.responseText)
        }
    }
}

//output message to console
function outputConsoleMessage(message)
{
  var consoleOutput = document.getElementById("outputConsole");

  var d = new Date();
  var h = d.getHours();
  var m = d.getMinutes();
  var s = d.getSeconds();
  var msg = h + ":" + m + ":" + s + " " + message;

  consoleOutput.innerHTML +=  msg  + "<br/>";
  consoleOutput.scrollTop = consoleOutput.scrollHeight;
}



