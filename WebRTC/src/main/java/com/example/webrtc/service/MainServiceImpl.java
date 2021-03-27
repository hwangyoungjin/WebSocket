package com.example.webrtc.service;

import com.example.webrtc.model.Room;
import com.example.webrtc.model.RoomService;
import com.example.webrtc.util.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MainServiceImpl implements MainService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String REDIRECT = "redirect:/";

    private final RoomService roomService;
    private final Parser parser;

    @Autowired
    public MainServiceImpl(final RoomService roomService, final Parser parser) {
        this.roomService = roomService;
        this.parser = parser;
    }

    /**
     * main 화면 return
     */
    @Override
    public ModelAndView displayMainPage(final Long id, final String uuid) {
        final ModelAndView modelAndView = new ModelAndView("main");
        modelAndView.addObject("id", id);
        modelAndView.addObject("rooms", roomService.getRooms());
        modelAndView.addObject("uuid", uuid);

        return modelAndView;
    }

    /**
     * 룸 만들기 요청
     * 륨 만들어진 main 화면 return
     */
    @Override
    public ModelAndView processRoomSelection(final String sid, final String uuid, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // simplified version, no errors processing
            return new ModelAndView(REDIRECT);
        }
        Optional<Long> optionalId = parser.parseId(sid);
        optionalId.ifPresent(id -> Optional.ofNullable(uuid).ifPresent(name -> roomService.addRoom(new Room(id))));

        return this.displayMainPage(optionalId.orElse(null), uuid);
    }


    /**
     * main화면에서 room 클릭시 실행되며
     * char_room.html Return
     */
    @Override
    public ModelAndView displaySelectedRoom(final String sid, final String uuid) {
        // redirect to main page if provided data is invalid
        ModelAndView modelAndView = new ModelAndView(REDIRECT);

        if (parser.parseId(sid).isPresent()) {
            Room room = roomService.findRoomByStringId(sid).orElse(null);
            if(room != null && uuid != null && !uuid.isEmpty()) {
                logger.debug("User {} is going to join Room #{}", uuid, sid);
                // open the chat room
                modelAndView = new ModelAndView("chat_room", "id", sid);
                modelAndView.addObject("uuid", uuid);
            }
        }

        return modelAndView;
    }

    /**
     * room에서 나갈때 실행되며
     * redirect:/ 되서  main 화면 return 된다.
     */
    @Override
    public ModelAndView processRoomExit(final String sid, final String uuid) {
        if(sid != null && uuid != null) {
            logger.debug("User {} has left Room #{}", uuid, sid);
            // implement any logic you need
        }
        return new ModelAndView(REDIRECT);
    }

    /**
     * RoomNumber Random으로 요청시 발생
     * 랜덤넘버의 room number 생성하여 main화면 return
     */
    @Override
    public ModelAndView requestRandomRoomNumber(final String uuid) {
        return this.displayMainPage(randomValue(), uuid);
    }

    private Long randomValue() {
        return ThreadLocalRandom.current().nextLong(0, 100);
    }
}
