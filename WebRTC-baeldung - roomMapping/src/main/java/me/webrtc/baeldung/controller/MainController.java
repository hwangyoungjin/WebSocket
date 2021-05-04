package me.webrtc.baeldung.controller;


import me.webrtc.baeldung.model.Account;
import me.webrtc.baeldung.service.MatchingService;

import java.util.HashMap;
import java.util.Map;

public class MainController {

    /**
     * 매칭 버튼 클릭시
     *
     * @Param : 본인의 Email, 제외할 학과, 학년을 넘겨줘야함 + (SpringSecurity에서 확인 할 Login Cookie
     * @Return : 매칭된 2명의 정보
     */
    MatchingService matchingService;

    public Account matching(String email, String major, String grade) throws InterruptedException {

        //매칭이 된다면 매칭완료된 요청자의 Account 객체가 넘어온다.
        Account account = matchingService.wating(email,major,grade);
        /** 매칭 결과 확인 **/
        if(account == null){
            //실패
            return null;
        }

        //TODO : 성공했으므로 매칭된 2명의 Account 모두 보내야 한다.
        return account;
    }





}
