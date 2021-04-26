package me.webrtc.baeldung.service;

import java.util.HashMap;
import java.util.Map;

public class MatchingService {

    /**매칭 기다리는 대기자 목록**/
    static Map<String,String> waitingRoom = new HashMap<>();

    /**
     * 1. 3분 동안 대기 했는데 매칭 안된다면 매칭 다시 요청 보내기
     * 2. 대기 중에 매칭 된다면 매칭된 상대방 email 보내기
     *
     */
    public String wating(String email, String major, String grade) throws InterruptedException {

        /** 1. 목록에서 매칭상대 찾기 **/
        String result = this.matching(email,major,grade);

        /**2. 해당 User의 매칭 상대를 찾고 없으면 3분 대기 **/
        if(result == null){

            /**없으면 대기자 목록에서 대기**/
            waitingRoom.put(email,"object");

            Thread.sleep(36000l); //3분
            /** 3분 지났으니 매칭 결과 확인 후 있으면 skik**/
            if(result == null){
                /**매칭 안됬으면 다시 시도하라는 메세지 보내기**/
                return null;
            }
        }

        /** 매칭 결과 있으면 **/
        //1. 상대방 대기목록에서 지우고
        waitingRoom.remove(email);
        //2. 연결된 상대방 이메일 보내기
        return result;
    }


    public String matching(String email, String major, String grade){

        /**매칭하기**/

        return null;
    }
}
