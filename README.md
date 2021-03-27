# WebSocket
WebSocket
---
1. ## SpringBoot WebSocket
- [Spring Boot - 스프링 부트 WebSocket!](https://kouzie.github.io/spring/Spring-Boot-%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B6%80%ED%8A%B8-WebSocket/#)

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

    - ## [WebRTC 정리 잘되어있음 But Node.js](https://jinn-blog.tistory.com/112)

    2. ### Project Setting
    ```java
    * jdk11
    * springboot 2.4.4
    * dependency
     - lombok
     - springdatajpa
     - h2db
    ```
    3. ### Websocket dependency
    ```gradle
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    ```

    4. ### WebRTC Code 다운 [참조 : Benkoff/WebRTC-SS](https://github.com/Benkoff/WebRTC-SS/)

    

3. ## 