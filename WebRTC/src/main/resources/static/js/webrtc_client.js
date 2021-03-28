'use strict';
// create and run Web Socket connection
const socket = new WebSocket("ws://" + window.location.host + "/signal");

// UI elements
const videoButtonOff = document.querySelector('#video_off');
const videoButtonOn = document.querySelector('#video_on');
const audioButtonOff = document.querySelector('#audio_off');
const audioButtonOn = document.querySelector('#audio_on');
const exitButton = document.querySelector('#exit');
const localRoom = document.querySelector('input#id').value;
/**
 * localVideo가 Video 스트림 가져온다
 * remoteVideo가 원격의 video 스트림 가져온다
 */
const localVideo = document.getElementById('local_video');
const remoteVideo = document.getElementById('remote_video');
const localUserName = localStorage.getItem("uuid");

// WebRTC STUN servers
/**
 * STUN 서버는 두 클라이언트 모두 IP 주소를 결정하는 데 사용됩니다.
 *
 * rtc 중계가 끊어질 것을 대비한 임시 서버버 * @type {{iceServers: [{urls: string}, {urls: string}]}}
 */
const peerConnectionConfig = {
    'iceServers': [
        {'urls': 'stun:stun.stunprotocol.org:3478'},
        {'urls': 'stun:stun.l.google.com:19302'}, // P2P 연결의 중계서버는 구글에서 무료로 지원하는 Google STUN 서버
    ]
};

/**
 * 가져올 Stream
 */
// WebRTC media
const mediaConstraints = {
    audio: true,
    video: true
};

// WebRTC variables
let localStream;
let localVideoTracks;
let myPeerConnection;

/**
 * $(document).ready(function(){});와 동일한 의미
 * DOM(Document Object Model) 객체가 생성되어 준비되는 시점에서 호출된다는 의미
 */
// on page load runner
$(function(){
    start();
});

/**
 * 핵심 function
 * WebRTC 연결을 설정하는 다음 단계는 ICE (Interactive Connection Establishment) 및 SDP 프로토콜을 포함하며,
 * 여기서 피어의 세션 설명이 두 피어에서 교환되고 수락
 */
function start() {
    // add an event listener for a message being received
    socket.onmessage = function(msg) {
        let message = JSON.parse(msg.data);
        switch (message.type) {
            case "text":
                log('Text message from ' + message.from + ' received: ' + message.data);
                break;

            case "offer":
                log('Signal OFFER received');
                handleOfferMessage(message);
                break;

            /**
             * 그 후 다른 피어가 오퍼를 수신하면 이를 원격 설명으로 설정해야합니다 .
             * 또한 응답을 생성해야 하며 이는 시작 피어로 전송됩니다.
             */
            case "answer":
                log('Signal ANSWER received');
                handleAnswerMessage(message);
                break;

            /**
             * WebRTC는 ICE (Interactive Connection Establishment) 프로토콜을
             * 사용하여 피어를 검색하고 연결을 설정합니다.
             *
             * peerConnection 에 로컬 설명을 설정하면 icecandidate 이벤트가 트리거됩니다 .
             * 이 이벤트는 원격 피어가 원격 후보 세트에 후보를 추가 할 수 있도록 후보를 원격 피어로 전송해야합니다.
             * 이를 위해 onicecandidate 이벤트에 대한 리스너를 만듭니다 .
             */
            case "ice":
                log('Signal ICE Candidate received');
                handleNewICECandidateMessage(message);
                break;

            /**어 에게 보냅니다
             * 서버 측 기술로 send 메소
             * 먼저 오퍼를 생성하고 이를 peerConnection 의 로컬 설명으로 설정합니다 .
             * 그런 다음 제안 을 다른 피드의 로직을 자유롭게 구현할 수 있습니다.
             */
            case "join":
                log('Client is starting to ' + (message.data === "true)" ? 'negotiate' : 'wait for a peer'));
                handlePeerConnection(message);
                break;

            default:
                handleErrorMessage('Wrong type message received from server');
        }
    };

    /**
     * onopen을 통해 소켓이 연결된 경우에만 서버로 메세지 보낸다.
     */
    // add an event listener to get to know when a connection is open
    socket.onopen = function() {
        log('WebSocket connection opened to Room: #' + localRoom);
        // send a message to the server to join selected room with Web Socket
        sendToServer({
            from: localUserName, // uuid를 의미
            type: 'join',
            data: localRoom // room number를 의미
        });
    };

    // a listener for the socket being closed event
    socket.onclose = function(message) {
        log('Socket has been closed');
    };

    // an event listener to handle socket errors
    socket.onerror = function(message) {
        handleErrorMessage("Error: " + message);
    };
}

function stop() {
    // send a message to the server to remove this client from the room clients list
    log("Send 'leave' message to server");
    sendToServer({
        from: localUserName,
        type: 'leave',
        data: localRoom
    });

    if (myPeerConnection) {
        log('Close the RTCPeerConnection');

        // disconnect all our event listeners
        myPeerConnection.onicecandidate = null;
        myPeerConnection.ontrack = null;
        myPeerConnection.onnegotiationneeded = null;
        myPeerConnection.oniceconnectionstatechange = null;
        myPeerConnection.onsignalingstatechange = null;
        myPeerConnection.onicegatheringstatechange = null;
        myPeerConnection.onnotificationneeded = null;
        myPeerConnection.onremovetrack = null;

        // Stop the videos
        if (remoteVideo.srcObject) {
            remoteVideo.srcObject.getTracks().forEach(track => track.stop());
        }
        if (localVideo.srcObject) {
            localVideo.srcObject.getTracks().forEach(track => track.stop());
        }

        remoteVideo.src = null;
        localVideo.src = null;

        // close the peer connection
        myPeerConnection.close();
        myPeerConnection = null;

        log('Close the socket');
        if (socket != null) {
            socket.close();
        }
    }
}

/*
 UI Handlers
  */
// mute video buttons handler
videoButtonOff.onclick = () => {
    localVideoTracks = localStream.getVideoTracks();
    localVideoTracks.forEach(track => localStream.removeTrack(track));
    $(localVideo).css('display', 'none');
    log('Video Off');
};
videoButtonOn.onclick = () => {
    localVideoTracks.forEach(track => localStream.addTrack(track));
    $(localVideo).css('display', 'inline');
    log('Video On');
};

// mute audio buttons handler
audioButtonOff.onclick = () => {
    localVideo.muted = true;
    log('Audio Off');
};
audioButtonOn.onclick = () => {
    localVideo.muted = false;
    log('Audio On');
};

// room exit button handler
exitButton.onclick = () => {
    stop();
};

function log(message) {
    console.log(message);
}

function handleErrorMessage(message) {
    console.error(message);
}

// use JSON format to send WebSocket message
function sendToServer(msg) {
    let msgJSON = JSON.stringify(msg);
    socket.send(msgJSON);
}

// initialize media stream
function getMedia(constraints) {
    if (localStream) {
        localStream.getTracks().forEach(track => {
            track.stop();
        });
    }
    navigator.mediaDevices.getUserMedia(constraints)
        .then(getLocalMediaStream).catch(handleGetUserMediaError);
}

// create peer connection, get media, start negotiating when second participant appears
function handlePeerConnection(message) {
    createPeerConnection();
    getMedia(mediaConstraints);
    if (message.data === "true") {
        myPeerConnection.onnegotiationneeded = handleNegotiationNeededEvent;
    }
}

/**
 * RTCPeerConnection
 * 로컬 컴퓨터와 원격 피어 간의 WebRTC 연결을 나타낸다. 두 피어 간의 효율적인 데이터 스트리밍을 처리하는데 사용된다.
 */
function createPeerConnection() {
    myPeerConnection = new RTCPeerConnection(peerConnectionConfig);

    // event handlers for the ICE negotiation process
    myPeerConnection.onicecandidate = handleICECandidateEvent;
    myPeerConnection.ontrack = handleTrackEvent;

    // the following events are optional and could be realized later if needed
    // myPeerConnection.onremovetrack = handleRemoveTrackEvent;
    // myPeerConnection.oniceconnectionstatechange = handleICEConnectionStateChangeEvent;
    // myPeerConnection.onicegatheringstatechange = handleICEGatheringStateChangeEvent;
    // myPeerConnection.onsignalingstatechange = handleSignalingStateChangeEvent;
}

/**
 * 카메라/마이크 등 데이터 스트림 접근
 * @param mediaStream
 */
// add MediaStream to local video element and to the Peer
function getLocalMediaStream(mediaStream) {
    localStream = mediaStream;
    localVideo.srcObject = mediaStream;
    localStream.getTracks().forEach(track => myPeerConnection.addTrack(track, localStream));
}

// handle get media error
function handleGetUserMediaError(error) {
    log('navigator.getUserMedia error: ', error);
    switch(error.name) {
        case "NotFoundError":
            alert("Unable to open your call because no camera and/or microphone were found.");
            break;
        case "SecurityError":
        case "PermissionDeniedError":
            // Do nothing; this is the same as the user canceling the call.
            break;
        default:
            alert("Error opening your camera and/or microphone: " + error.message);
            break;
    }

    stop();
}

// send ICE candidate to the peer through the server
function handleICECandidateEvent(event) {
    if (event.candidate) {
        sendToServer({
            from: localUserName,
            type: 'ice',
            candidate: event.candidate
        });
        log('ICE Candidate Event: ICE candidate sent');
    }
}

function handleTrackEvent(event) {
    log('Track Event: set stream to remote video element');
    remoteVideo.srcObject = event.streams[0];
}

/**
 * Client1이 시그널링 서버를 호출 = createOffer()를 통해 SDP 생성
 * SDP와 함께 setLocalDescription() 호출
 */
// WebRTC called handler to begin ICE negotiation
// 1. create a WebRTC offer
// 2. set local media description
// 3. send the description as an offer on media format, resolution, etc
function handleNegotiationNeededEvent() {
    myPeerConnection.createOffer().then(function(offer) {
        return myPeerConnection.setLocalDescription(offer);
    })
        .then(function() {
            sendToServer({
                from: localUserName,
                type: 'offer',
                sdp: myPeerConnection.localDescription
            });
            log('Negotiation Needed Event: SDP offer sent');
        })
        .catch(function(reason) {
            // an error occurred, so handle the failure to connect
            handleErrorMessage('failure to connect error: ', reason);
        });
}

/**
 * RTCSessionDescription
 * 세션의 매개 변수를 나타냅니다.
 * 각 RTCSessionDescription는 세션의  SDP 기술자(descriptor)의 기술 제안
 * / 응답 협상 과정의 일부를 나타내는 설명  type으로 구성되어 있습니다.
 *
 * Client2 가 Client1의 SDP를 가지고 setRemoteDescription()를 호출
 * -> Client1은 Client2의 설정을 알게된다.
 */
function handleOfferMessage(message) {
    log('Accepting Offer Message');
    log(message);
    let desc = new RTCSessionDescription(message.sdp);
    //TODO test this
    if (desc != null && message.sdp != null) {
        log('RTC Signalling state: ' + myPeerConnection.signalingState);
        myPeerConnection.setRemoteDescription(desc).then(function () {
            log("Set up local media stream");
            return navigator.mediaDevices.getUserMedia(mediaConstraints);
        })
            .then(function (stream) {
                log("-- Local video stream obtained");
                localStream = stream;
                try {
                    localVideo.srcObject = localStream;
                } catch (error) {
                    localVideo.src = window.URL.createObjectURL(stream);
                }

                log("-- Adding stream to the RTCPeerConnection");
                localStream.getTracks().forEach(track => myPeerConnection.addTrack(track, localStream));
            })
            .then(function () {
                /**
                 * Client2는 응답을 인자로 전달하는 성공 콜백 함수 createAnswer()를 호출
                 */
                log("-- Creating answer");
                // Now that we've successfully set the remote description, we need to
                // start our stream up locally then create an SDP answer. This SDP
                // data describes the local end of our call, including the codec
                // information, options agreed upon, and so forth.
                return myPeerConnection.createAnswer();
            })
            .then(function (answer) {
                /**
                 * Client2는 setLocalDescription()의 호출을 통해
                 * Client2의 응답을 로컬 기술(Description)으로 설정합니다.
                 */
                log("-- Setting local description after creating answer");
                // We now have our answer, so establish that as the local description.
                // This actually configures our end of the call to match the settings
                // specified in the SDP.
                return myPeerConnection.setLocalDescription(answer);
            })
            .then(function () {
                /**
                 * Client2는 시그널링 메커니즘을 사용하여 자신의 문자열화된 응답을 Client1에게 다시 전송합니다.
                 */
                log("Sending answer packet back to other peer");
                sendToServer({
                    from: localUserName,
                    type: 'answer',
                    sdp: myPeerConnection.localDescription
                });

            })
            // .catch(handleGetUserMediaError);
            .catch(handleErrorMessage)
    }
}

/**
 * 시작피어는 응답을 받고 원격설명으로 설정
 * 이를 통해 WebRTC는 성공적인 연결을 설정합니다.
 * 이제 시그널링 서버없이 두 피어간에 직접 데이터를주고받을 수 있습니다 .
 * @param message
 *
 * Client1은 setRemoteDescription()을 사용하여 Client2의 응답을 원격 세션 기술(Description)으로 설정 */
function handleAnswerMessage(message) {
    log("The peer has accepted request");

    // Configure the remote description, which is the SDP payload
    // in our "video-answer" message.
    // myPeerConnection.setRemoteDescription(new RTCSessionDescription(message.sdp)).catch(handleErrorMessage);
    myPeerConnection.setRemoteDescription(message.sdp).catch(handleErrorMessage);
}

/**
 * RTCIceCandidate
 * RTCPeerConnection 설정을 위한 후보 인터넷 연결 설정
 * (ICE; internet connectivity establishment) 서버를 나타냅니다.
 *
 * 다른 피어가 보낸 ICE 후보를 처리해야합니다.
 * 이 후보 를 수신 한 원격 피어 는 후보를 후보 풀에 추가해야합니다.
 * @param message
 */
function handleNewICECandidateMessage(message) {
    let candidate = new RTCIceCandidate(message.candidate);
    log("Adding received ICE candidate: " + JSON.stringify(candidate));
    myPeerConnection.addIceCandidate(candidate).catch(handleErrorMessage);
}
