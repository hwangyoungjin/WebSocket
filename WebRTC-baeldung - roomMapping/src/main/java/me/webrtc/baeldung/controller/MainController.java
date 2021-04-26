package me.webrtc.baeldung.controller;


import me.webrtc.baeldung.service.MatchingService;

import java.util.HashMap;
import java.util.Map;

public class MainController {


    /**
     * 매칭 버튼 클릭시
     * 본인의 Email
     * 제외할 학과
     * 제외할 학년
     * 을 넘겨줘야함
     * + (SpringSecurity에서 확인 할 Login Cookie)
     *
     */

    MatchingService matchingService;

    public String matching(String email, String major, String grade) throws InterruptedException {




        String result = matchingService.matching(email,major,grade);

        /**1. 해당 User의 매칭 상대를 찾고 없으면 3분 대기 **/
        if(result == null){
            Thread.sleep(36000l); //3분
        }

        /** 매칭 결과 확인 **/

        return result;
    }





}
