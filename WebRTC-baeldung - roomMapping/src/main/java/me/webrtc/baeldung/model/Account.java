package me.webrtc.baeldung.model;

import lombok.Data;
import org.springframework.web.util.pattern.PathPattern;

@Data
public class Account {
    private String email;
    private String grade;
    private String major;

    //TODO : 매칭여부 확인
    private boolean matching;

    //TODO : 연결된 상대방
    private String peerEmail;
}
