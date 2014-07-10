/************************************
 *
 *
 **/

/****************************************
 * messages functions
 */

/*******************************************************************************
 * message constructors
 */
var MESSAGE = {
	JOIN_ROOM : {
		key : "1-1",
		cla : 1,
		ins : 1
	},
	START_GAME : {
		key : "1-2",
		cla : 1,
		ins : 2
	},
	END_GAME : {
		key : "1-3",
		cla : 1,
		ins : 3
	},
	NOTIFICATION : {
		key : "1-4",
		cla : 1,
		ins : 4
	},
	ASK : {
		key : "1-5",
		cla : 1,
		ins : 5
	},
	READY : {
		key : "1-6",
		cla : 1,
		ins : 6
	},

	// POKER
	PUT_CARD : {
		key : "2-1",
		cla : 2,
		ins : 1
	},
	PASS : {
		key : "2-2",
		cla : 2,
		ins : 2
	},
	DEAL_CARD : {
		key : "2-3",
		cla : 2,
		ins : 3
	},
	APPEND_CARD : {
		key : "2-4",
		cla : 2,
		ins : 4
	},
	BET : {
		key : "2-5",
		cla : 2,
		ins : 5
	}
};
var Message = function(userId, cla, ins) {
	this.userId = userId;
	this.cla = cla;
	this.ins = ins;
}
var JoinRoom = function(userId, roomId, position) {
	var msg = new Message(userId, 0x01, 0x01);

	msg.roomId = roomId;
	if (position == undefined) {
		position = -1;
	}
	msg.position = position;

	return msg;
}

var leaveRoom = function() {
	console.debug("leave room");
}

var inviteFriends = function() {
	console.debug("invite friends");

	var friendList = dijit.registry.byId("friendList")

	var checkedFriendNames = new Array();
	
	for (var i = 0; i < friendList.getChildren().length; i++) {
		if (friendList.getChildren()[i].checked == true) {
			var item = friendList.getChildren()[i];
			console.debug(item.model.userId+":"+item.model.userName+" checked");
			
			checkedFriendNames.push(item.model.userName);
		}
	}
	
	require(["dojo/cookie","dojo/request/xhr"],function(cookie,xhr){
		// post
		var status = "測試 三缺一!!!http://gamecenter.firecloud.me/service/room/room-id ";
		for(var i=0;i<checkedFriendNames.length;i++){
			status = status + "@"+checkedFriendNames[i];
		}
		
		var method="POST";
		var targetUrl="https://api.weibo.com/2/statuses/update.json";
		var socialToken=cookie("social-token");
		var socialId=cookie("social-id");
		
		xhr("/tool/proxy/weibo",{
			method:"POST",
			data:{"method":method,"url":targetUrl,"access_token":socialToken,"uid":socialId,"status":status},
			handleAs:"json"
		}).then(function(data){
			alert("invite successfully");
			
			dijit.registry.byId("inviteView").performTransition("waittingView",-1,"slide",null);
		});
	});
}

var reloadFriendList = function() {
	var friendList = dijit.registry.byId("friendList")

	// remove all old items
	while (friendList.hasChildren()) {
		friendList.removeChild(0);
	}

	
	
	
	
	require(["dojo/cookie", "dojo/request/xhr"],function(cookie,xhr){
		
		// load friends list
		var method="GET";
		var targetUrl="https://api.weibo.com/2/friendships/followers.json";
		var socialToken=cookie("social-token");
		var socialId=cookie("social-id");
		
		xhr("/tool/proxy/weibo",{
			method:"POST",
			data:{"method":method,"url":targetUrl,"access_token":socialToken,"uid":socialId},
			handleAs : "json",
		}).then(function(data) {
			var friends = data.users;

			var itemTpl = "<img src='{{avatarUrl}}'/><div>>{{screenName}}</div>";
			for (var i = 0; i < friends.length; i++) {
				var friend = friends[i];
				var item = new dojox.mobile.ListItem({
					"icon" : friend.profile_image_url,
					"label" : friend.screen_name,
					"variableHeight" : true
				}).placeAt(friendList, "last");

				item.model = {
					userId : friend.id,
					userName : friend.screen_name
				};
				// item.containerNode.innerHTML =
				// itemTpl.replace("{{avatarUrl}}",friend.profile_image_url).replace("{{screenName}}",friend.screen_name);
			}
		});
	});

	

}

var joinRoom=function(){
	var webSocket = new WebSocket("ws://" + fc.serviceInfo.host+":"+fc.serviceInfo.port+"/service/user/"
			+ fc.self.id);
	
	webSocket.onopen = function() {
		console.debug("websocket opened");
		
		var joinRoom = new JoinRoom(fc.self.id,fc.roomInfo.id,-1);
		
		this.send(JSON.stringify(joinRoom));
	};

	webSocket.onclose = function() {
		console.debug("websocket closed");
	};
	webSocket.onerror = function(error) {
		console.debug("websocket error:");
		console.debug(error);
	};
	webSocket.onmessage = function(event) {
		console.debug(event);
		
	};
}

var refreshJointList = function(){
	console.debug("refreshing joint players list...");
	require(["dojo/request/xhr"],function(xhr){
		var target = "/data/rest/room/"+fc.roomInfo.id;
		
		xhr(target,{
			method:"GET",
			handleAs : "json",
		}).then(function(data) {
			console.debug(data);
			
			// check if all players are joint
			
			if(data.seats!=undefined & data.seats!=null){
				var flag=true;
				for(var i=0;i<data.seatNum;i++){
					if(data.seats.length<=i || data.seats[i][0]==null){
						flag=false;
					}
				}
				
				if(flag==true){
					// go to play scene, akka, reload 
					
					var notifyDlg=dijit.registry.byId("gotoPlayingSceneNotifyDlg");
					notifyDlg.show();
					window.setTimeout(function(){
					window.location="/service/room/"+fc.roomInfo.id;
					},2000);
				}
			}
			
			// update jointPlayerList
			
			var jointPlayerList = dijit.registry.byId("jointPlayerList");
			
			// remove all old items
			while (jointPlayerList.hasChildren()) {
				jointPlayerList.removeChild(0);
			}
			
			for(var i=0;i<data.seatNum;i++){
				if(data.seats!=undefined & data.seats!=null & data.seats[i][0]!=null){
					var item = new dojox.mobile.ListItem({
						"icon" : data.seats[i][2],
						"label" : data.seats[i][1],
						"variableHeight" : true
					}).placeAt(jointPlayerList, "last");
				}else{
					var item = new dojox.mobile.ListItem({
						"label" : "等待。。。",
						"variableHeight" : true
					}).placeAt(jointPlayerList, "last");
				}
			}
		});
	});
}

require(["dojox/timing"],function(timing){
	var t = new timing.Timer(5000);
	t.onTick = function(){
	 console.debug("Five second elapsed");
	 
	 refreshJointList();
	}
	t.onStart = function(){
	 console.info("Starting timer");
	}
	t.start();
	
});
