package me.webrtc.baeldung.websocket;
import lombok.Data;

@Data
public class WebSocketMessage {
    private String event;
    private String data;
    private Object candidate;
    private Object sdp;

}
