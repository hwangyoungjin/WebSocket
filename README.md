# WebSocket

WebSocket
---
1. ## [Java Socket](https://github.com/hwangyoungjin/WebSocket#java-socket-1)
2. ## [SpringBoot WebSocket - ChatRoom](https://github.com/hwangyoungjin/WebSocket#springboot%EC%99%80-websocket%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EA%B0%84%EB%8B%A8%ED%95%9C-%EC%B1%84%ED%8C%85%EA%B5%AC%ED%98%84)
3. ## [WebRTC + SpringBoot = baeldung](https://github.com/hwangyoungjin/WebSocket#webrtc--springboot--baeldung-1)
4. ## [WebRTC + SpringBoot = Benkoff/WebRTC-SS](https://github.com/hwangyoungjin/WebSocket#webrtc--springboot--benkoffwebrtc-ss-1) - 추후 진행
---
1. ## Java Socket
    1. ### Java Socket Project
    2. ### Java Socket Chat Project

    
2. ## SpringBoot와 WebSocket을 이용한 간단한 채팅구현

    - Spring Boot - 스프링 부트 WebSocket [참고](https://kouzie.github.io/spring/Spring-Boot-%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B6%80%ED%8A%B8-WebSocket/#)
    - [SpringBoot WebSocket 참고](https://asfirstalways.tistory.com/359)
    - [SpringBoot WebSocket 참고](https://m.blog.naver.com/PostView.nhn?blogId=kdy2353&logNo=221469476261&proxyReferer=https:%2F%2Fwww.google.com%2F)


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
        * html 렌터링되면서 js 호출되고 이를 통해 socket 연결되면서 호출
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

- ## WebRTC
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

    - ### Signalling
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

    - ### 생각하는 순서
    ```java
    * WebRTC 순서 정리

    * WebRTC가 P2P연결할 수 없을때 사용 하는 Turn 서버는 사용하지 않는다고 가정

    ClientA
    1. stun을 통해 자신의 Public IP를 알아내고
    2. RTCPeerConnection 객체 를 생성할 때 이를 사용

    ClientB
    1. stun을 통해 자신의 Public IP를 알아내고
    2. RTCPeerConnection 객체 를 생성할 때 이를 사용

    ============================

    3. ClientA가 signaling 서버에 candidate를 전송
    4. signaling 서버는 해당 candidate를 다른 클라이언트에게 전송

    5. ClientB가 해당 candidate를 받고 응답하기위해 signalling서버에 cadidate 발송

    ==========연결완료===========

    이후에 미디어 스트림을 가져와서 SIP를 통해 ClientA, ClientB끼리 통신

    ※※※※※※※※※※※※질문※※※※※※※※※※※

    Q1. 연결된 이후에는 signalling 서버와의 통신은 없는건지? 

    Q2. WebRTC가 P2P연결할 수없을때 사용 하는 Turn 서버는 어느방법을 통해 구현해야하는건지?
    ```

    - ### [turn 서버의 필요성](https://withseungryu.tistory.com/138)

    - ### [WebRTC 정리 잘되어있음](https://jinn-blog.tistory.com/112)

    - ### [front : react + back : springboot으로 RTC](https://www.baeldung.com/webrtc)
        - ### 해당 Project의 [Github](https://github.com/sintinilorenzo/video-calling-app)
        - turn은 연결이 안되므로 같은 공유기 안에서만 가능

    
3. ## [WebRTC + SpringBoot = baeldung](https://www.baeldung.com/webrtc)
     ```js
    * WebRTC란?
    WebRTC(Web Real-Time Communications)란, 웹 어플리케이션(최근에는 Android 및 IOS도 지원) 및 사이트들이 
    별도의 소프트웨어 없이 음성, 영상 미디어 혹은 텍스트, 파일 같은 데이터를 브라우져끼리 주고 받을 수 있게 만든 *기술
    
    * WebRTC는 브라우저와 함께 기본 제공되는 솔루션이므로 브라우저에 외부 플러그인을 설치할 필요X

    * WebRTC 핵심 클래스
    MediaStream — 카메라와 마이크 등의 데이터 스트림 접근
    RTCPeerConnection — 암호화 및 대역폭 관리 및 오디오, 비디오의 연결
    RTCDataChannel — 일반적인 데이터의 P2P 통신

    * WebRTC가 내부적으로 처리하는 문제 문제
     1. Packet-loss concealment
     2. Echo cancellation
     3. Bandwidth adaptivity
     4. Dynamic jitter buffering
     5. Automatic gain control
     6. Noise reduction and suppression
     7. Image “cleaning”

    * 클라이언트가 서로를 검색하고 네트워크 세부 정보를 공유 한 다음 데이터 형식을 공유하기 위해 WebRTC는 Signaling 라는 메커니즘을 사용

    * Signaling server는 Springboot를 통해 구현
    ```

    1. ### Project Setting
    ```java
    * 환경
    * jdk11
    * gradle
    * springboot 2.4.4
    * dependency
    - lombok
    - WebSocket *signaling server 구축을 위해 필요*
    - Thymeleaf
    - devtools
    ```

    2. ### WebSocketConfigurer 구현한 클래스 생성
    ```java
    @Configuration
    @EnableWebSocket
    public class WebSocketConfiguration implements WebSocketConfigurer {

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(new SocketHandler(), "/socket")
                    .setAllowedOrigins("*");
        }
    }
    ```

    3. ### message Handler 생성
    ```java
    * 해당 클래스는 서로 다른 클라이언트 간의 메타 데이터 교환을 지원하는 데 필수
    * 해당 클래스는 클라이언트로부터 메시지를받을 때 자신을 제외한 다른 모든 클라이언트에게 메시지를 보낸다

    @Component
    public class SocketHandler extends TextWebSocketHandler {

        List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

        /**
        * 클라이언트로부터 메시지를 받으면 목록의 모든 클라이언트 세션을 반복하고
        * 보낸 사람의 세션 ID를 비교하여 보낸 사람을 제외한 다른 모든 클라이언트에게 메시지를 보낸다.
        * Client가 Offer하는 경우 실행 됨
        */
        @Override
        public void handleTextMessage(WebSocketSession session, TextMessage message)
                throws InterruptedException, IOException {
            for (WebSocketSession webSocketSession : sessions) {
                if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                    webSocketSession.sendMessage(message);
                }
            }
        }

        /**
        * 모든 클라이언트를 추적 할 수 있도록 수신 된 세션을 세션 목록에 추가
        * 클라이언트가 index.html 접속 하면 socket 보내는데 그 때 해당 메소드 실행됨
        * 즉, 클라이언트가 localhost:8080 접속 -> index.html -> client.js의 socket 요청 -> 해당메소드 실행
        */
        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            sessions.add(session);
        }
    }
    ```

    4. ### MetaData 교환 단계
    ```java
    * P2P 연결에서 클라이언트는 서로 다를 수 있다 (EX. Android <-> ios)
    * 따라서 미디어 유형 및 코덱에 동의하는 피어 간의 핸드 셰이크가 필수적
    * 해당단계에서 WebRTC는 SDP (Session Description Protocol)를 사용하여 클라이언트 간의 메타 데이터에 동의합니다
    * 이를 달성하기 위해 PeerA는 다른 피어(PeerB)가 원격 설명 자로 설정해야하는 Offer를 작성합니다. 또한 다른 피어(PeerB)는 PeerA가 원격 설명 자로 수락하는 answer을 생성합니다.
    * 위 과정을 통해 PeerA와 PeerB는 연결이 완료
    ```
    5. ### Client 설정
    
    ```js
    * WebRTC 핵심 클래스
    MediaStream — 카메라와 마이크 등의 데이터 스트림 접근
    RTCPeerConnection — 암호화 및 대역폭 관리 및 오디오, 비디오의 연결
    RTCDataChannel — 일반적인 데이터의 P2P 통신
    ```

    ```js
    //connecting to our signaling server
    //우리가 구축 한 Spring Boot 시그널링 서버가 http : // localhost : 8080 에서 실행되고 있음
    var conn = new WebSocket('ws://localhost:8080/socket');
    ```

    ```js
    /**
    * signaling server로 메세지 보내기 위해 send 메소드 설정
    */
    function send(message) {
        conn.send(JSON.stringify(message));
    }
    ```

    ```js
     /**
     * 간단한 RTCDataChannel 설정
     * Configuration에는 Stun turn 이 들어가지만 이번 예제에서는 null로 충분
     */
    peerConnection = new RTCPeerConnection(configuration);

    /**
     * Peer간 메시지 전달에 사용할 dataChannel
     */
    // creating data channel
    dataChannel = peerConnection.createDataChannel("dataChannel", {
        reliable : true
    });

    /**
     * 데이터 채널의 다양한 이벤트에 대한 리스너
     */
    dataChannel.onerror = function(error) {
        console.log("Error occured on datachannel:", error);
    };

    dataChannel.onclose = function() {
        console.log("data channel is closed");
    };
    ```

    6. ### ICE 연결 설정
    ```java
    * 해당 단계는 ICE (Interactive Connection Establishment) 및 SDP 프로토콜을 포함하며
    * 여기서 피어의 세션 설명이 두 Peer에서 교환되고 수락 된다.
    ```
    
    - #### step1. PeerA offer 생성
    ```js
    /**
    * 1. offer를 생성하고 이를 peerConnection 의 localDescription으로 설정
    * 2. 이후 offer 을 다른 PeerB 에게 보낸다.
    */
    function createOffer() {
        peerConnection.createOffer(function(offer) {
            //send 메소드는 offer 정보 를 전달하기 위해 Signaling Server를 호출
            send({
                event : "offer",
                data : offer
            });
            peerConnection.setLocalDescription(offer);
        }, function(error) {
            alert("Error creating an offer");
        });
    }
    ```

    - #### step2. ICE candidate 처리
    ```js
    /**
     * WebRTC는 ICE (Interactive Connection Establishment) 프로토콜을 사용하여 Peer를 검색하고 연결을 설정
     * peerConnection 에 localDescription을 설정하면 icecandidate 이벤트가 트리거된다
     * 상대 PeerB가 Set of remote candidates에 Candidate를 추가 할 수 있도록 candidate를 상대 PeerB에게 전송
     * 이를 위해 onicecandidate 이벤트에 대한 리스너를 만든다.
     *
     * ICE candidate의 모든 candidate가 수집 될 때 이벤트는 빈 후보 문자열을 다시 트리거
     * 그 이유는 빈 문자열을 remote peer에게 전달하여 모든 icecandidate 객체가 수집 되었음을 알리기 위해
     */
    // Setup ice handling
    peerConnection.onicecandidate = function(event) {
        if (event.candidate) {
            send({
                event : "candidate",
                data : event.candidate
            });
        }
    };
    ```

    - #### step3. PeerA가 보낸 ICE candidate 받기
    ```js
    /**
    * PeerA가 보낸 ICE candidate를 처리해야 하는데
    * 이 candidate를 받은 PeerB는 해당 candidate를 candidate pool의 추가
    */
    function handleCandidate(candidate) {
        peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
    };
    ```

    - #### step4. PeerB가 offer 받고 PeerA에게 answer 보내기
    ```js
    /**
    * offer를 받은 PeerB는 이를 Remotedescription으로 설정하고
    * answer를 생성하여 PeerA 에게 보낸다.
    * @param offer d
    */
    function handleOffer(offer) {
        peerConnection.setRemoteDescription(new RTCSessionDescription(offer));

        // create and send an answer to an offer
        peerConnection.createAnswer(function(answer) {
            peerConnection.setLocalDescription(answer);
            send({
                event : "answer",
                data : answer
            });
        }, function(error) {
            alert("Error creating an answer");
        });
    };
    ```

    - #### step5. PeerA가 answer 받기
    ```js
    /**
    * 처음 PeerA는 anwser를 받고 setRemoteDescription 으로 설정
    */
    function handleAnswer(answer) {
        peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
        console.log("connection established successfully!!");
    };
    ```

    - #### 연결 완료

    7. ### message 보내기
    ```js
    /**
    * 연결 되었으므로 dataChannel 의 send 메서드를 사용하여 피어간에 메시지를 보낼 수 있다.
    */
    function sendMessage() {
        dataChannel.send(input.value);
        input.value = "";
    }
    ```
    ```js
    /**
     * 데이터 채널에서 메시지를 수신하기위해 peerConnection 객체 에 콜백을 추가
     */
    peerConnection.ondatachannel = function (event) {
        dataChannel = event.channel;
    };
    ```
    
    - ## 여기까지 Brower Console Test 완료


    8. ### Video and Audio Channels 추가해보기
    - WebRTC가 P2P 연결을 설정하면 오디오 및 비디오 스트림을 직접 쉽게 전송할 수 있다.

    - #### step1. Media Stream 얻기
    ```js
    /**
    * 브라우저에서 미디어 스트림을 가져오기
    * WebRTC는이를위한 API를 제공
    */
    const constraints = {
        video: true,audio : true
    };

    navigator.mediaDevices.getUserMedia(constraints).
    then(function(stream) { /* use the stream */  })
        .catch(function(err) { /* handle the error */  });
    ```

    ```js
    /**
     * constraints 객체를 사용하여 비디오의 프레임 속도, 너비 및 높이를 지정
     */
    var constraints = {
        video : {
            frameRate : {
                ideal : 10,
                max : 15
            },
            width : 1280,
            height : 720,
            facingMode : "user"
        }
    };
    ```
     - #### step2. Stream 보내기
    ```js
    /**
     * WebRTC WebRTC peerconnection object에 스트림을 추가
     * peerconnection에 스트림을 추가하면 연결된 피어 에서 addstream 이벤트가 트리거
     */
    peerConnection.addStream(stream);
    ```

     - #### step3. Stream 받기
    ```js
    /**
     * remote peer 에서 listener를 통해 스트림을 수신
     * 해당 스트림은  HTML 비디오 요소로 설정
     */
    peerConnection.onaddstream = function(event) {
        videoElement.srcObject = event.stream;
    };
    ```

     - NAT 문제를 위해 STUN 추가 설정
    ```js
    var configuration = {
        "iceServers" : [ {
            "url" : "stun:stun2.1.google.com:19302"
        } ]
    };
    ```

4. ## [WebRTC + SpringBoot = Benkoff/WebRTC-SS](https://github.com/Benkoff/WebRTC-SS/)
   

    1. ### Project Setting
    ```java
    * jdk11
    * springboot 2.4.4
    * dependency
     - lombok
     - springdatajpa
     - h2db
     - Thymeleaf
    ```

    2. ### Websocket dependency
    ```gradle
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    ```
