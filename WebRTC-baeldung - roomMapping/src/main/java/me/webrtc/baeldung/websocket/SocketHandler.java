package me.webrtc.baeldung.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
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
    // private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // message types, used in signalling:
    // text message
    // SDP Offer message
    private static final String MSG_TYPE_OFFER = "offer";
    // SDP Answer message
    private static final String MSG_TYPE_ANSWER = "answer";
    // New ICE Candidate message
    private static final String MSG_TYPE_ICE = "ice";
    // join room data message
    private static final String MSG_TYPE_JOIN = "join";

    private final ObjectMapper objectMapper = new ObjectMapper();


    //TODO : watingRoom 만들기 -> sessionId
    private Map<String,String> watingSession = new HashMap<>();


    /**
     * 클라이언트로부터 메시지를 받으면 목록의 모든 클라이언트 세션을 반복하고
     * 보낸 사람의 세션 ID를 비교하여 보낸 사람을 제외한 다른 모든 클라이언트에게 메시지를 보낸다.
     *  Client가 Offer하는 경우 실행 됨
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage textMessage)
            throws InterruptedException, IOException {

  //      WebSocketMessage message = objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class);
        //String userName = message.getFrom(); // origin of the message
        //String data = message.getData(); // payload // room id가 들어온다




        for (WebSocketSession webSocketSession : sessions) {
            //TODO : MESSAGE
            if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                webSocketSession.sendMessage(textMessage);
         //       logger.debug("[ws] Type of the received message {} is undefined!", message.getPayload());
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



    /**
     * 브라우저가 연결을 닫으면 이 메서드가 호출되고 세션이 세션 목록에서 제거
     */
    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
    //    logger.debug("[ws] The received message {} is undefined!", "remove");
        sessions.remove(session.getId());
    }
}

