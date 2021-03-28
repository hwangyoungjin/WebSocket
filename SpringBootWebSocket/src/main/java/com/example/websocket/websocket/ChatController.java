package com.example.websocket.websocket;

import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {
    @GetMapping("/chatrooms")
    public String chatrooms(){
        return "chatrooms";
    }
}
