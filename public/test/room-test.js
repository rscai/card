google.load("jquery", "1");

// constant
var fc = {
	serviceInfo : {
		host : "localhost",
		port : 9000
	},
	gameInfo : {
		projectLocation : "/assets/game/poker/project.json"
	},
	roomInfo : {
		id : "",
		name : ""
	}
};

//construct test players

var Player = function(playerId) {
	var player = new WebSocket("ws://" + fc.serviceInfo.host + ":"
			+ fc.serviceInfo.port + "/service/user/" + playerId);

	player.messageQueue = new Array();

	// set message handlers

	player.onopen = function() {
		console.log(playerId + " is connected.");
	}

	player.onclose = function() {
		console.log(playerId + " is disconnected.");
	}

	player.onerror = function(error) {
		console.debug(error);
	}

	player.onmessage = function(event) {
		console.debug(event.data);
		player.messageQueue.push(JSON.parse(event.data));
	}

	return player;
}




/******************************************
 * test cases
 */
asyncTest("Create room", function() {
	expect(4);
	// 
	var room = null;
	$.ajax({
		type : "POST",
		url : "/data/rest/room",
		contentType : "application/json",
		data : JSON.stringify({
			kind : "card",
			name : "test"
		}),
		success : function(result) {
			room = result;

		},
		dataType : "json"
	});

	setTimeout(function() {
		ok(room!=null);
		ok(room.kind == "card");
		ok(room.name == "test");
		ok(room.id);

		fc.roomInfo.id = room.id;

		start();
	}, 1000);
});

// join room

asyncTest("Player1 join room",function(){
	var player1 = new Player("test-player-1");
	var player2 = new Player("test-player-2");
	var player3 = new Player("test-player-3");
	
	var room = null;
	$.ajax({
		type : "POST",
		url : "/data/rest/room",
		contentType : "application/json",
		data : JSON.stringify({
			kind : "card",
			name : "test"
		}),
		success : function(result) {
			room = result;

		},
		dataType : "json"
	});

	setTimeout(function() {
		ok(room!=null);
		ok(room.kind == "card");
		ok(room.name == "test");
		ok(room.id);

		//start();
		
		
		var joinRoom = {"roomId":room.id,"userId":"test-player-1","cla":1,"id":"b09b2a17-1e07-470e-8d3f-ddaf6b8656ca","ins":1};
		player1.send(JSON.stringify(joinRoom));
		
		// assert
		setTimeout(function(){
			// player1 show received notification
			var notification = player1.messageQueue.pop();
			
			ok(notification, "received message.");
			
			equal(notification.cla,1,"recevied message is notification");
			equal(notification.ins,4,"recevied message is notification");
			equal(notification.msg.cla,1,"notification join room event");
			equal(notification.msg.ins,1,"notification join room event");
			equal(notification.msg.userId,"test-player-1","player1 join room");
			
			
			
			start();
		},1000);
		
	}, 1000);
	
	
});


