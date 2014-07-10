/*******************************************************************************
 * event handlers
 * 
 */
var refreshRoomList = function() {
	var roomList = dijit.registry.byId("roomList");
	// construct mock item

	var headTpl = '<div class="room-item"><div class="item-head"><img alt="{{kind}}" src="{{icon}}" /><h1>{{name}}</h1></div><div class="seat-list">';
	var seatTpl = '<div class="seat"><img alt="{{seat.name}}" src="{{seat.avatar}}"></div>';
	var freeSeatTpl = '<div class="seat free-seat" onclick="javascript:enterRoom(\'{{roomId}}\',\'{{roomName}}\');"><img alt="free seat" src="http://portrait3.sinaimg.cn/1250323962/blog/180"></div>';
	var footTpl = '</div></div>';

	// get free room list

	var xhrArgs = {
		url : "/data/rest/room",
		handleAs : "json",
		preventCache : false,
		load : function(data) {
			console.debug(data);
			var rooms = data;
			// remove all old items
			while (roomList.hasChildren()) {
				roomList.removeChild(0);
			}

			for (var i = 0; i < rooms.length; i++) {
				var room = rooms[i];

				var itemHTML = headTpl.replace("{{kind}}", room.kind).replace(
						"{{icon}}", room.icon).replace("{{name}}", room.name);
				if (room.seats == undefined | room.seats == null) {
					room.seats = new Array();
					for (var k = 0; k < room.seatNum; k++) {
						room.seats.push([ null, null, null ]);
					}
				}
				for (var j = 0; j < room.seats.length; j++) {
					var seat = room.seats[j];
					if (seat[0] != undefined & seat[0] != null) {
						itemHTML = itemHTML
								+ seatTpl.replace("{{seat.name}}", seat[1])
										.replace("{{seat.avatar}}", seat[2]);
					} else {
						itemHTML = itemHTML
								+ freeSeatTpl.replace("{{roomId}}", room.id)
										.replace("{{roomName}}", room.name);
					}
				}

				itemHTML = itemHTML + footTpl;

				// construct item
				var item = new dojox.mobile.ListItem({
					"class" : "roomListItem",
					"data-dojo-props" : "variableHeight:true"
				}).placeAt(roomList, "first");

				item.containerNode.innerHTML = itemHTML;
			}
		},
		error : function(error) {
			console.error(error)
		}
	}

	// Call the asynchronous xhrGet
	var deferred = dojo.xhrGet(xhrArgs);
	/*
	 * var rooms = [ { id : "id1", name : "test1", kind : "card",
	 * icon:"http://www.baidu.com/img/baidu_jgylogo3.gif", seats : [ [ "test1",
	 * "test1", "http://portrait3.sinaimg.cn/1250323962/blog/180" ], [ "test1",
	 * "test1", "http://portrait3.sinaimg.cn/1250323962/blog/180" ], [ "test1",
	 * "test1", "http://portrait3.sinaimg.cn/1250323962/blog/180" ], [ "test1",
	 * "test1", "http://portrait3.sinaimg.cn/1250323962/blog/180" ], [ "test1",
	 * "test1", "http://portrait3.sinaimg.cn/1250323962/blog/180" ] ] } ];
	 * 
	 */

};

var enterRoom = function(roomId, roomName) {

	// show confirm dialog

	var dlg = new dojox.mobile.SimpleDialog();
	dojo.body().appendChild(dlg.domNode);
	var msgBox = dojo.create("div", {
		class : "mblSimpleDialogText",
		innerHTML : "确定加入房间：" + roomName + "?"
	}, dlg.domNode);
	var piBox = dojo.create("div", {
		class : "mblSimpleDialogText"
	}, dlg.domNode);
	var cancelBtn = new dojox.mobile.Button({
		class : "mblSimpleDialogButton mblRedButton",
		innerHTML : "Ok"
	});
	cancelBtn.connect(cancelBtn.domNode, "click", function(e) {
		//dlg.hide();
		var url = "/service/room/" + roomId;

		window.location = url;
	});
	cancelBtn.placeAt(dlg.domNode);
	
	var cancelBtn = new dojox.mobile.Button({
		class : "mblSimpleDialogButton mblRedButton",
		innerHTML : "Cancel"
	});
	cancelBtn.connect(cancelBtn.domNode, "click", function(e) {
		dlg.hide();
	});
	cancelBtn.placeAt(dlg.domNode);
	dlg.show();
	/*
	 * 
	 */
}