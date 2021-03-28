# WebSocket
WebSocket
---
1. ## SpringBoot WebSocket
- Spring Boot - 스프링 부트 WebSocket [참고](https://kouzie.github.io/spring/Spring-Boot-%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B6%80%ED%8A%B8-WebSocket/#)
- [SpringBoot WebSocket 참고](https://asfirstalways.tistory.com/359)
- [SpringBoot WebSocket 참고](https://m.blog.naver.com/PostView.nhn?blogId=kdy2353&logNo=221469476261&proxyReferer=https:%2F%2Fwww.google.com%2F)

    1. ## SpringBoot와 WebSocket을 이용한 간단한 채팅구현

        ```java
        * front에서는
        SockJs 와 Stomp 라이브러리 사용
        
        * 환경
        * jdk11
        * gradle
        * springboot 2.4.4
        * dependency
        - lombok
        - WebSocket
        - Thymeleaf
        - devtools
        ```

        1. ### 클라이언트에서 사용할 라이브러리 의존성 추가
        ```gradle
        compile("org.webjars:sockjs-client:1.0.2")
        compile("org.webjars:stomp-websocket:2.3.3")
        ```

        2. ### WebSocketConfig 클래스 생성
        ```java

        * Client에서 socket Url : "/ws" 로 보낸다

        @Configuration
        @EnableWebSocket
        public class WebSocketConfig implements WebSocketConfigurer {

            
            @Override
            public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
                registry.addHandler(new ChatSocketHandler(), "/ws").withSockJS();
            }
        }

        ```
        
        3. ### Message Model 생성
        ```java
        @Data
        public class ChatMessage {
            private String name;
            private String message;
        }
        ```

        4. ### SocketHandler 생성
        ```java
        public class ChatSocketHandler extends TextWebSocketHandler {

            ObjectMapper objectMapper = new ObjectMapper();
            List<WebSocketSession> list = Collections.synchronizedList(new ArrayList<>());

            /**
            * 웹 소켓 연결될 때 호출
            */
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                System.out.println("===========접속===========");
                System.out.println("session ID = "+ session.getId());
                System.out.println("session Accept Protocol = "+ session.getAcceptedProtocol());
                System.out.println("session LocalAddress = "+ session.getLocalAddress());
                System.out.println("session RemoteAddress = "+ session.getRemoteAddress());
                System.out.println("session Uri = "+ session.getUri());
                System.out.println("===========접속===========");
                list.add(session);
            }

            /**
            * 메시지를 전송받았을 때 호출되는 메소드
            */
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                System.out.println("====메세지 도착====");
                //payload는 전송되는 데이터를 의미
                System.out.println("session Id = "+session.getId()+", 받은 message payload = "+message.getPayload());
                System.out.println("====메세지 끝====");

                System.out.println("session ID = "+ session.getId());
                System.out.println("session Accept Protocol = "+ session.getAcceptedProtocol());
                System.out.println("session LocalAddress = "+ session.getLocalAddress());
                System.out.println("session RemoteAddress = "+ session.getRemoteAddress());
                System.out.println("session Uri = "+ session.getUri());




                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setName(message.toString());
                chatMessage.setMessage(message.getPayload());

                String json = objectMapper.writeValueAsString(chatMessage);

                // 세션에 존재하는 모든 client에게 message 전송 : echo
                for(WebSocketSession wss : list){
                    /**
                    * 아래 출려코드 사용시 에러발생 Closing session due to exception for WebSocketServerSockJsSession
                    * => 주석처리하니 해결됨
                    * => session은 생성해서 한번만 사용가능한건가..
                    */
        //            System.out.println("WebSocketSession List ["+wss.getId() + "]");
                    wss.sendMessage(new TextMessage(json));
                }
            }


            /**
            * 연결 종료시 호출되는 메소드
            */
            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                System.out.println("===========접속 Close===========");
                System.out.println("session ID = "+ session.getId());
                System.out.println("session Accept Protocol = "+ session.getAcceptedProtocol());
                System.out.println("session LocalAddress = "+ session.getLocalAddress());
                System.out.println("session RemoteAddress = "+ session.getRemoteAddress());
                System.out.println("session Uri = "+ session.getUri());
                System.out.println("===========접속 Close===========");

                list.remove(session);
            }
        }
        ```

        5. ### Controller 생성
        ```java
        @Controller
        public class ChatController {
            @GetMapping("/chatrooms")
            public String chatrooms(){
                return "chatrooms";
            }
        }
        ``` 

        6. ### chatroom.js 생성
        ```js
        // sockjs 를 이용한 서버와 연결되는 객체
        var ws = null;

        function setConnected(connected) {
        }

        function showMessage(message) {
            console.log(message);
            var jsonMessage = JSON.parse(message);

            $("#chatArea").append(jsonMessage.name + ' : ' + jsonMessage.message + '\n');

            var textArea = $('#chatArea');
            textArea.scrollTop( textArea[0].scrollHeight - textArea.height()   );

        }


        function connect() {
            // SockJS라이브러리를 이용하여 서버에 연결
            ws = new SockJS('/ws');
            // 서버가 메시지를 보내주면 함수가 호출된다.
            ws.onmessage = function(message) {
                showMessage(message.data);
            }
        }

        function disconnect() {
            if (ws != null) {
                ws.close();
            }
            setConnected(false);
            console.log("Disconnected");
        }

        function send() {
            // 웹소켓 서버에 메시지를 전송
            ws.send(JSON.stringify({'message': $("#chatInput").val()}));
            // 채팅입력창을 지우고 포커싱하라.
            $("#chatInput").val('');
            $("#chatInput").focus();
        }


        // $(함수(){ 함수내용 });  // jquery에서 문서가 다 읽어들이면 함수()를 호출한다.
        $(function () {

            connect();

            // 채팅입력창에서 키가 눌리면 함수가 호출
            // 엔터를 입력하면 send()함수가 호출
            $("#chatInput").keypress(function(e) {
                if (e.keyCode == 13){
                    send();
                }
            });

            $( "#sendBtn" ).click(function() { send(); });
        });
        ```

        7. ### chatroom.html 생성

        ```html
        <!DOCTYPE html>
        <html lang="ko" xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
        <head>
            <meta charset="utf-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"/>
            <script  type="text/javascript" src="https://code.jquery.com/jquery-3.3.1.min.js"></script>

            <!-- 의존성 추가 했던 라이브러리 사용 -->
            <script  type="text/javascript" src="/webjars/sockjs-client/1.0.2/sockjs.min.js"></script>

            <script src="/js/chatroom.js"></script>
            <title>chat room</title>
        </head>

        <body>

        <div class="jumbotron">
            <h1>chat room</h1>
        </div>

        <div class="container">

            <div class="col-sm-12 col-md-12">
                <textarea cols="80" rows="15" id="chatArea" class="form-control"></textarea>
            </div>
            <div class="col-sm-12 col-md-12">
                <input type="text" id="chatInput" class="form-control"/>
                <input type="button" id="sendBtn" value="전송" class="btn btn-primary btn-small"/>
            </div>

        </div>
        </body>
        </html>
        ```

2. ## WebRTC
    1. ### [WebRTC + SpringBoot](https://www.baeldung.com/webrtc)
    ```java
    Web RTC 기술은 P2P 통신에 최적화 되어있다.
    Web RTC에 사용되는 기술은 여러가지가 있지만 크게 3가지의 클래스에 의해서 실시간 데이터 교환이 일어난다.

    MediaStream - 카메라/마이크 등 데이터 스트림 접근
    RTCPeerConnection - 암호화 및 대역폭 관리 및 오디오 또는 비디오 연결
    RTCDataChannel - 일반적인 데이터 P2P통신

    이 3가지의 객체를 통해서 데이터 교환이 이뤄지며 RTCPeerConnection들이 적절하게 데이터를 교환할 수 있게 처리하는 과정을 시그널링(Signaling) 이라고 한다.
    ```
    - image<>
    ```
    * Peer To Peer 통신 순서
     1. 서로서로 통신을 할 수 있도록 만듭니다.
     2. 서로서로를 인식할 수 있고 네크워크 관련 정보를 공유합니다.
     3. 주고받을 데이터의 형식이나 프로토콜 등을 공유합니다.
     4. 데이터를 주고 받습니다.
    위 순서는 시그널링 하는 과정을 나타낸 것인데,PeerConnection은 두 명의 유저가 스트림을 주고 받는 것이므로 연결을 요청한 콜러(caller)와 연결을 받는 콜리(callee)가 존재한다. 콜러와 콜리가 통신을 하기 위해서는 중간 역할을 하는 서버가 필요하고 서버를 통해서 SessionDescription을 서로 주고 받아야 한다.
    ```

    - ### 용어 정리
    ```java
    https://developer.mozilla.org/ko/docs/Web/API/WebRTC_API/Protocols
    * Stun Server , Turn Server
    Web RTC는 P2P에 최적화 되어있다. 즉 Peer들간의 공인 네트워크 주소(ip)를 알아 데이터 교환을 해야하는데, 실제 개개인의 컴퓨터는 방화벽등 여러가지 보호장치들이 존재하고 있다.
    그래서 Peer들간의 연결이 쉽지 않은데, 이렇게 서로간의 연결을 위한 정보를 공유하여 P2P 통신을 가능하게 해주는 것이 Stun/Turn Server이다.

    (https://alnova2.tistory.com/1110 에 더 자세한 내용이 담겨있다.)

    * SDP (Session Description Protocol)
    세션 기술 프로토콜(Session Description Protocol, SDP)은 스트리밍 미디어의 초기화 인수를 기술하기 위한 포맷이다. 이 규격은 IETF의 RFC 4566로 규정되어 있다.
    실제로 WEB RTC는 SDP format 에 맞춰져 영상,음성 데이터를 교환하고 있다.
    PeerConnection 객체를 생성하게 되면 PeerConnection 객체에서 offer SDP, answer 

    * ICE (Interactive Connectivity Establishment)
    NAT환경에서 자신의 Public IP를 파악하고 상대방에게 데이터를 전송하기 위한 Peer간의 응답 프로토콜로 일반적으로 STUN/TURN을 이용해서 구축을 한다.
    간단하게 설명하면, 한쪽이 Offer를 보내면 다른 한쪽이 Answer함으로써 피어간 연결이 설정된다
    
    * stun : 두 피어 사이의 통신이 가능한 공공 IP를 알려주는 역할
        - 단순히 정보 제공을 위한 서버라 트래픽 발생이 현저히 낮다. 그래서인지 구글에서 무료로 제공하는 stun 서버만 이용해도 webrtc 구현에 별 문제가 없다
    
    * turn : 사용자의 NAT 타입 또는 방화벽의 제한이 있는지 확인하는 서버
        - 외부망을 통해 통신하는 피어들 사이에 통신 제한이 있으면 turn 서버가 피어 간의 통신 채널을 중계하는 역할
        - turn 서버는 중계 서버라 트래픽 발생이 높다보니 제가 생각하기에는 구글에서 무료로 제공하던 turn 서버가 개발자들의 무분별한 사용으로 현재 사용이 불가
        - https://gist.github.com/yetithefoot/7592580 를 참고하시면 'turn:numb.viagenie.ca'는 아직 사용이 가능
        - turn 서버 구축에 대해 오픈소스가 있고, 안정적인 서비스를 위해서는 개인소유의 turn 서버가 있는 것이 좋을 것 같습니다
    
    * coturn : coturn은 stun/turn 둘 다 동시에 제공
    ```

    ```java
    * WebRTC에 필요한 4가지 종류의 서버측 기능
    1. 사용자 탐색과 통신
    2. Signaling
    3. NAT/firewall 탐색
    4. P2P 실패시의 중계 서버들

    * springboot 서버에선 signalling만 구현
    signalling은 webRTC에 구현되어 있지 않아 따로 WebRTC와 별개로 따로 Signalling Server를 구현해야 한다. 
    많은 Cloud Message Platform이 존재 (Pusher, Kaazing, PubNub) 하긴하지만  Web socket 기능을 이용하여 직접 구현해도 된다. (SIP, XMPP/Jingle 기술도 가능)

    * signalling server 동작원리
    1. 통신을 원하는 사용자는 상대 사용자에게 Signalling Server를 통해 자신의 정보들을 제공한다 (ICE 사용가능)
    2. 상대 사용자는 그 정보들에 대해 자신의 정보를 담아 답장한다 (ICE 사용가능)
    ```

    - ## [WebRTC 정리 잘되어있음](https://jinn-blog.tistory.com/112)

    - ## [front : react + back : springboot으로 RTC](https://www.baeldung.com/webrtc)
        - ## 해당 Project의 [Github](https://github.com/sintinilorenzo/video-calling-app)
        - turn은 연결이 안되므로 같은 공유기 안에서만 가능

    2. ### Project Setting
    ```java
    * jdk11
    * springboot 2.4.4
    * dependency
     - lombok
     - springdatajpa
     - h2db
     - Thymeleaf
    ```
    3. ### Websocket dependency
    ```gradle
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    ```

    4. ### WebRTC Code 다운 [참조 : Benkoff/WebRTC-SS](https://github.com/Benkoff/WebRTC-SS/)

    

3. ## 