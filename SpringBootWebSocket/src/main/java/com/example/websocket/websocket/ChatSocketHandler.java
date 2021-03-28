package com.example.websocket.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatSocketHandler extends TextWebSocketHandler {

    ObjectMapper objectMapper = new ObjectMapper();
    List<WebSocketSession> list = Collections.synchronizedList(new ArrayList<>());

    /**
     * 웹 소켓 연결될 때 호출
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("===========접속===========");
        System.out.println("session ID = "+ session.getId());
        System.out.println("session Accept Protocol = "+ session.getAcceptedProtocol());
        System.out.println("session LocalAddress = "+ session.getLocalAddress());
        System.out.println("session RemoteAddress = "+ session.getRemoteAddress());
        System.out.println("session Uri = "+ session.getUri());
        System.out.println("===========접속===========");
        list.add(session);
    }

    /**
     * 메시지를 전송받았을 때 호출되는 메소드
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("====메세지 도착====");
        //payload는 전송되는 데이터를 의미
        System.out.println("session Id = "+session.getId()+", 받은 message payload = "+message.getPayload());
        System.out.println("====메세지 끝====");

        System.out.println("session ID = "+ session.getId());
        System.out.println("session Accept Protocol = "+ session.getAcceptedProtocol());
        System.out.println("session LocalAddress = "+ session.getLocalAddress());
        System.out.println("session RemoteAddress = "+ session.getRemoteAddress());
        System.out.println("session Uri = "+ session.getUri());




        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setName(message.toString());
        chatMessage.setMessage(message.getPayload());

        String json = objectMapper.writeValueAsString(chatMessage);

        // 세션에 존재하는 모든 client에게 message 전송 : echo
        for(WebSocketSession wss : list){
            /**
             * 아래 출려코드 사용시 에러발생 Closing session due to exception for WebSocketServerSockJsSession
             * => 주석처리하니 해결됨
             * => session은 생성해서 한번만 사용가능한건가..
             */
//            System.out.println("WebSocketSession List ["+wss.getId() + "]");
            wss.sendMessage(new TextMessage(json));
        }
    }


    /**
     * 연결 종료시 호출되는 메소드
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("===========접속 Close===========");
        System.out.println("session ID = "+ session.getId());
        System.out.println("session Accept Protocol = "+ session.getAcceptedProtocol());
        System.out.println("session LocalAddress = "+ session.getLocalAddress());
        System.out.println("session RemoteAddress = "+ session.getRemoteAddress());
        System.out.println("session Uri = "+ session.getUri());
        System.out.println("===========접속 Close===========");

        list.remove(session);
    }
}
