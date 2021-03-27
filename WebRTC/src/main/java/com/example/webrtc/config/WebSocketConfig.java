package com.example.webrtc.config;

import com.example.webrtc.socket.SignalHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket // 웹소켓에 대해 대부분 자동설정을 한다.
public class WebSocketConfig implements WebSocketConfigurer {
    // WebSocketConfigurer를 구현하여 추가적인 설정을 한다.

    @Autowired
    private SignalHandler signalHandler;

    /**
     * WebSocketHandler를 추가한다.
     * 내가 만든 webSocketHandler를 사용할 수 있게 registry에 등록
     * 1. 클라이언트가 접속을 했을 때 특정 메소드가 호출
     * 2. 클라이언트가 접속을 close했을 때 특정 메소드가 호출
     * 3. 클라이언트가 메시지를 보냈을 때 특정 메소드 호출
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Client에서는 GET /signal를 호출해서 Server의 정보를 취득하며,
        // WebSocketHandlerRegistry에 WebSocketHandler의 구현체를 등록한다.
        // 등록된 Handler는 특정 endpoint("/signal")로 handshake를 완료한 후 맺어진 connection의 관리
        registry.addHandler(signalHandler, "/signal")
                .setAllowedOrigins("*"); // allow all origins <-pub,sub의 sub
    }


    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }
}