package me.webrtc.baeldung.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {

    List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();


    //TODO : watingRoom 만들기 -> sessionId
    private Map<String,String> watingSession = new HashMap<>();


    /**
     * 클라이언트로부터 메시지를 받으면 목록의 모든 클라이언트 세션을 반복하고
     * 보낸 사람의 세션 ID를 비교하여 보낸 사람을 제외한 다른 모든 클라이언트에게 메시지를 보낸다.
     *  Client가 Offer하는 경우 실행 됨
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {
        for (WebSocketSession webSocketSession : sessions) {
            //TODO : MESSAGE
            if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                webSocketSession.sendMessage(message);
            }
        }
    }

    /**
     * 모든 클라이언트를 추적 할 수 있도록 수신 된 세션을 세션 목록에 추가
     * 클라이언트가 index.html 접속 하면 socket 보내는데 그 때 해당 메소드 실행됨
     * 즉, 클라이언트가 localhost:8080 접속 -> index.html -> client.js의 socket 요청 -> 해당메소드 실행
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }


    //
}

