// sockjs 를 이용한 서버와 연결되는 객체
var ws = null;

function setConnected(connected) {
}

function showMessage(message) {
    console.log(message);
    var jsonMessage = JSON.parse(message);

    $("#chatArea").append(jsonMessage.name + ' : ' + jsonMessage.message + '\n');

    var textArea = $('#chatArea');
    textArea.scrollTop( textArea[0].scrollHeight - textArea.height()   );

}


function connect() {
    // SockJS라이브러리를 이용하여 서버에 연결
    ws = new SockJS('/ws');
    // 서버가 메시지를 보내주면 함수가 호출된다.
    ws.onmessage = function(message) {
        showMessage(message.data);
    }
}

function disconnect() {
    if (ws != null) {
        ws.close();
    }
    setConnected(false);
    console.log("Disconnected");
}

function send() {
    // 웹소켓 서버에 메시지를 전송
    ws.send(JSON.stringify({'message': $("#chatInput").val()}));
    // 채팅입력창을 지우고 포커싱하라.
    $("#chatInput").val('');
    $("#chatInput").focus();
}


// $(함수(){ 함수내용 });  // jquery에서 문서가 다 읽어들이면 함수()를 호출한다.
$(function () {

    connect();

    // 채팅입력창에서 키가 눌리면 함수가 호출
    // 엔터를 입력하면 send()함수가 호출
    $("#chatInput").keypress(function(e) {
        if (e.keyCode == 13){
            send();
        }
    });

    $( "#sendBtn" ).click(function() { send(); });
});