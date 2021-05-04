package me.webrtc.baeldung.service;

import me.webrtc.baeldung.model.Account;
import me.webrtc.baeldung.repository.AccountRepository;

import java.util.HashMap;
import java.util.Map;

public class MatchingService {

    /**매칭 기다리는 대기자 목록 <이메일,Account>**/
    static Map<String,Account> waitingRoom = new HashMap<>();

    /*repository 대체용*/
    private AccountRepository accountRepository = new AccountRepository();

    /**
     * 0. 기본적으로 매칭하는 사람의 정보 가져오기
     * 1. 매칭 상대 찾고
     *  1.1 찾으면 해당 상대 map에서 꺼내고 2명의 정보 같이 반환 - 이때 꺼내진 사람은 sleep에서 깨어나서 map에서 자신을 제거
     * 2. 못찾으면 map에 넣고 3분 대기
     * 3. 3분 동안 대기 했는데 매칭 안된다면 실패 반환
     */
    public Account wating(String email, String major, String grade) throws InterruptedException {

        /** 0. 매칭하고자 하는 사람의 정보 꺼내기**/
        Account account = accountRepository.getUser(email);

        /** 1. 목록에서 매칭상대 찾기 **/
        Account peer = this.matching(email,major,grade);

        /**1.1. 찾으면 나의 매칭정보와 상대 매칭정보 수정하고 리턴 **/
        if(peer != null){
            //상대방의 연결된 peer정보에 나의 이메일 입력
            peer.setPeerEmail(account.getEmail());

            //나의 peer정보에 상대방 입력
            account.setPeerEmail(peer.getEmail());

            return account;
        }

        /**2. 매칭할 상대를 없으면 map에 넣고 3분 대기 **/
        waitingRoom.put(email,account);
        Thread.sleep(36000l); //3분

        /** 3분 지났으니 매칭 결과 확인**/

        /** 매칭 됬으므로 map에서 나와서 **/
        if(account.isMatching()){
            waitingRoom.remove(account.getEmail());
            String peerEmail = account.getPeerEmail();
            return account;
        }

        /**3. 3분 기다렸는데 매칭 안됨**/
        return null;
    }


    public Account matching(String email, String major, String grade){

        /**매칭하기**/

        return null;
    }
}
