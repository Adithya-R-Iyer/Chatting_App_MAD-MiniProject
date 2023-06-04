const { ExpressPeerServer } = require('peer');
const express = require('express');
const app = express();
const server = require('http').Server(app);
const io = require('socket.io')(server);
const { v4: uuidv4 } = require('uuid');


const peerServer = ExpressPeerServer(server, {
    debug: true,
  });
  app.use('/peerjs', peerServer);
  

const port = 9000;
server.listen(port, () => {
console.log(`Server is running on port ${port}`);
});


// // Generate a random user ID
// const userId = uuidv4();

function init(userId) {
    // Connect to the PeerJS server
    peer = new Peer(userId, {
        host: '192.168.1.9',
        port: 9000,
        path: '/peerjs',
    });
    
    // Listen for the 'open' event to know when the connection is established
    peer.on('open', () => {
        console.log('Connected to PeerJS server');

        localVideo = document.getElementById('local-video');
        remoteVideo = document.getElementById('remote-video');

        localVideo.style.opacity = 0;
        remoteVideo.style.opacity = 0;

        localVideo.onplaying = ()=>{
            localVideo.style.opacity = 1;
        }
        remoteVideo.onplaying = ()=>{
            remoteVideo.style.opacity = 1;
        }

        AndroidInterface.onPeerConnected();
        
    });
    
    listen();
}

function listen() {

    // Listen for incoming calls
    peer.on('call', (call) => {
      // Answer the call and get the user media
      navigator.mediaDevices.getUserMedia({ video: true, audio: true })
        .then((stream) => {
          // Assign the stream to the local video element
          localVideo.srcObject = stream;
          localStream = stream;
            
            console.log(localVideo);

          // Answer the call by sending our stream
          call.answer(stream);
    
          // Listen for the 'stream' event to get the remote stream
          call.on('stream', (remoteStream) => {
            // Assign the remote stream to the remote video element
            remoteVideo.srcObject = remoteStream;
            
            remoteVideo.className = "primary-video";
            localVideo.className = "secondary-video";
          });
        })
        .catch((error) => {
          console.error('Error accessing user media:', error);
        });
    });
}

// Make a call to another user
function startCall(otherUserId) {

  // Get the user media
  navigator.mediaDevices.getUserMedia({ video: true, audio: true })
    .then((stream) => {
      // Assign the stream to the local video element
      localVideo.srcObject = stream;
      localStream = stream;

      // Make the call
      const call = peer.call(otherUserId, stream);

      // Listen for the 'stream' event to get the remote stream
      call.on('stream', (remoteStream) => {
        // Assign the remote stream to the remote video element
        remoteVideo.srcObject = remoteStream;

        remoteVideo.className = "primary-video";
        localVideo.className = "secondary-video";
      });
    })
    .catch((error) => {
      console.error('Error accessing user media:', error);
    });
}

function toggleVideo(b) {
    if(b == "true") {
        localStream.getVideoTracks()[0].enabled = true;
    } else {
        localStream.getVideoTracks()[0].enabled = false;
    }
}

function toggleAudio(b) {
    if(b == "true") {
        localStream.getAudioTracks()[0].enabled = true;
    } else {
        localStream.getAudioTracks()[0].enabled = false;
    }
}
  