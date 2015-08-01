var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io')(server);

server.listen(80, function () {
    console.log('listening on *:80');
});

server.setTimeout(10000, function () {

});

var users = [];
//General client connection
io.on('connection', function (socket) {
    var room = "";
    var course = "";


    socket.on('start course', function () {
        socket.broadcast.to(room).emit('start', {
            course : course
        });
    });

    socket.on('join room', function (roomName) {
        room = roomName;
        socket.join(room);
        console.log("joined room " + room);
    });

    socket.on('set course', function (data) {
        course = data;
    });

    //Passes positional information to other clients in the room
    socket.on('position change', function (data) {
        console.log(socket.username + ' changed position');

        socket.broadcast.to(room).emit('position changed', {
            data : data
        });  
    });


    socket.on('map joined', function (data) {
        console.log(socket.username + ' joined map and Room: ' + room);

        socket.broadcast.to(room).emit('player joined map', {
            data : data
        });
    });

    socket.on('add user', function (username) {
        console.log(username + ' logged in');

        // we store the username in the socket session for this client
        socket.username = username;

        // add the client's username to the global list
        users.push(username);
      
        // finds all other users connected to the same room
        var roomList = [];
        var clients_in_the_room = io.sockets.adapter.rooms[room];
        for (var clientId in clients_in_the_room) {
            var client_socket = io.sockets.connected[clientId];
            roomList.push(client_socket.username)
        }

        // echo globally (all clients) that a person has connected
        socket.broadcast.to(room).emit('user joined lobby', {
            username: socket.username
        });

        io.sockets.in(room).emit('login', roomList);
      
    });
    
    socket.on('finished', function (data) {
        socket.broadcast.to(room).emit('other user finished', data);
    })

    socket.on('disconnect', function () {
        console.log(socket.username + ' disconnected and left Room: ' + room);

        // remove the username from global usernames list
        for (var i = users.length; i >= 0; i--) {
            if (users[i] == socket.username) {
                users.splice(i, 1);
            }
        } 

        // echo globally that this client has left
        socket.broadcast.to(room).emit('user left', {
            username: socket.username
        });
    });

    socket.on('log', function (string) {
        console.log(string);
    });
        
});
