package com.example.webrtc.controller;

import com.example.webrtc.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@ControllerAdvice
public class MainController {
    private final MainService mainService;

    @Autowired
    public MainController(final MainService mainService) {
        this.mainService = mainService;
    }

    /**
     * home 접속시 실행
     */
    @GetMapping({"", "/", "/index", "/home"})
    public ModelAndView displayMainPage(final Long id, final String uuid) {
        return this.mainService.displayMainPage(id, uuid);
    }

    /**
     * room 만들때 실행됨
     * @param sid
     * @param uuid
     * @param binding
     * @return
     */
    @PostMapping(value = "/room", params = "action=create")
    public ModelAndView processRoomSelection(@ModelAttribute("id") final String sid, @ModelAttribute("uuid") final String uuid, final BindingResult binding) {
        return this.mainService.processRoomSelection(sid, uuid, binding);
    }

    /**
     * 채팅방에 입장할때 실행됨 (채팅방 id 와 입장 uuid 필요)
     */
    @GetMapping("/room/{sid}/user/{uuid}")
    public ModelAndView displaySelectedRoom(@PathVariable("sid") final String sid, @PathVariable("uuid") final String uuid) {
        return this.mainService.displaySelectedRoom(sid, uuid);
    }

    /**
     * 채팅방에서 나갈때 실행됨
     */
    @GetMapping("/room/{sid}/user/{uuid}/exit")
    public ModelAndView processRoomExit(@PathVariable("sid") final String sid, @PathVariable("uuid") final String uuid) {
        return this.mainService.processRoomExit(sid, uuid);
    }

    @GetMapping("/room/random")
    public ModelAndView requestRandomRoomNumber(@ModelAttribute("uuid") final String uuid) {
        return mainService.requestRandomRoomNumber(uuid);
    }

    /**
     * client에서 sdp offer 할때 실행되며
     * sdp_offer html 반환
     */
    @GetMapping("/offer")
    public ModelAndView displaySampleSdpOffer() {
        return new ModelAndView("sdp_offer");
    }


    @GetMapping("/stream")
    public ModelAndView displaySampleStreaming() {
        return new ModelAndView("streaming");
    }
}
