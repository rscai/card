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

var Ready = function(userId) {
	var msg = new Message(userId, 0x01, 0x06);

	return msg;
}

var PutCard = function(userId, cards) {
	var msg = new Message(userId, 0x02, 0x01);
	msg.cards = cards;

	return msg;
}

var AppendCard = function(userId, cards) {
	var msg = new Message(userId, 0x02, 0x04);
	msg.cards = cards;

	return msg;
}

var Pass = function(userId) {
	var msg = new Message(userId, 0x02, 0x02);

	return msg;
}

var Bet = function(userId, amount) {
	var msg = new Message(userId, MESSAGE.BET.cla, MESSAGE.BET.ins);
	msg.amount = amount;

	return msg;
}

/**
 * Card
 */
/**
 * Object card, used to represent card
 */
var Card = cc.Node.extend({
	isFaceUp : true,
	isSelected : false,
	front : null,
	back : null,
	ctor : function(id, frontImage, backImage) {
		this._super();

		/**
		 * initialize
		 */

		this.id = id;
		// sprite which used to display front
		this.front = cc.Sprite.create(frontImage);

		// sprite which used to display back
		this.back = cc.Sprite.create(backImage);

		this.updateDisplay();
	},

	/***************************************************************************
	 * public functions
	 */

	/**
	 * turn card face up
	 */
	faceUp : function() {
		if (this.isFaceUp == true) {
			// do nothing
		} else {
			this.toggle();
		}
	},

	/**
	 * turn card face down
	 */
	faceDown : function() {
		if (this.isFaceUp == false) {
			// do nothing
		} else {
			this.toggle();
		}
	},

	/**
	 * turn card face up/down
	 */
	toggle : function() {
		this.isFaceUp = !this.isFaceUp;

		// update display
		this.updateDisplay();
	},

	/***************************************************************************
	 * private supported functions
	 */

	updateDisplay : function() {
		var height = this.getContentSize().height;
		if (this.isFaceUp == true) {
			// display front of card

			// clean display
			this.removeAllChildren(false);

			this.front.setAnchorPoint(new cc.Point(0, 0));
			this.addChild(this.front);
			if (this.isSelected) {
				this.front.setPosition(new cc.Point(0, height * 0.2));
			} else {
				this.front.setPosition(new cc.Point(0, 0));
			}
		} else {
			// display back of card
			// clean display
			this.removeAllChildren(false);

			this.back.setAnchorPoint(new cc.Point(0, 0));
			this.addChild(card.back);
			if (this.isSelected) {
				this.back.setPosition(new cc.Point(0, height * 0.2));
			} else {
				this.back.setPosition(new cc.Point(0, 0));
			}
		}
	},
	getContentSize : function() {
		return this.front.getContentSize();
	}

});

/**
 * abstract class
 */
var CardDock = cc.Node.extend({
	cards : null,
	size : null,
	ctor : function(size) {
		this._super();
		this.size = size;
		this.cards = new Array();
	},
	/***************************************************************************
	 * public functions
	 */
	putCard : function(cards) {
	},
	removeCard : function(cards) {
	},
	removeAll : function() {
	}
});

var HandCardDock = CardDock.extend({
	config : null,
	ctor : function(size, config) {
		this._super(size);
		this.config = config;

		this.updateDisplay();
	},
	/***************************************************************************
	 * public functions
	 */
	putCard : function(cards) {
		if (!(cards instanceof Array)) {
			cards = new Array(cards);
		}
		for (var i = 0; i < cards.length; i++) {
			this.cards.push(cards[i]);
		}

		// update display
		this.updateDisplay();
	},
	removeCard : function(cards) {
		if (!(cards instanceof Array)) {
			cards = new Array(cards);
		}

		var tmp = new Array();
		for (var i = 0; i < this.cards.length; i++) {
			var matched = false;
			for (var j = 0; j < cards.length; j++) {
				if (this.cards[i].id === cards[j].id) {
					matched = true;
					break;
				}
			}

			// matched card should be remove, others are left
			if (!matched) {
				tmp.push(this.cards[i]);
			}
		}

		this.cards = tmp;

		// update display

		this.updateDisplay();
	},
	removeAll : function() {
		this.cards = new Array();
		this.updateDisplay();
	},
	getSelected : function() {
		var selected = new Array();

		for (var i = 0; i < this.cards.length; i++) {
			if (this.cards[i].isSelected === true) {
				selected.push(this.cards[i]);
			}
		}

		return selected;
	},
	/***************************************************************************
	 * protected functions
	 */
	/**
	 * report card selected event, only invoked by card.
	 * 
	 * @param cardId
	 *            {string} the id of card which is selected
	 */
	_reportSelected : function(cardId) {
		for (var i = 0; i < this.cards.length; i++) {
			if (this.cards[i].id === cardId) {
				this.cards[i].isSelected = true;
				break;
			}
		}
	},

	/**
	 * report card unselected event, only invoked by card.
	 * 
	 * @param cardId
	 *            {string} the id of card which is unselected
	 */
	_reportUnselected : function(cardId) {
		for (var i = 0; i < this.cards.length; i++) {
			if (this.cards[i].id === cardId) {
				this.cards[i].isSelected = false;
				break;
			}
		}
	},
	/***************************************************************************
	 * private functions
	 */
	updateDisplay : function() {
		// clear all cards
		this.removeAllChildren(false);

		var touchEventListener = cc.EventListener.create({
			event : cc.EventListener.TOUCH_ONE_BY_ONE,
			swallowTouches : true,
			onTouchBegan : function(touch, event) {
				// event.getCurrentTarget() returns the *listener's*
				// sceneGraphPriority node.
				var target = event.getCurrentTarget();
				// console.debug(target);
				// Get the position of the current point relative to the
				// button
				var locationInNode = target.convertToNodeSpace(touch
						.getLocation());
				var s = target.getContentSize();
				var rect = cc.rect(0, 0, s.width, s.height);

				// Check the click area
				if (cc.rectContainsPoint(rect, locationInNode)) {

					target.isSelected = !target.isSelected;
					if (target.isSelected) {
						target.getParent()._reportSelected(target.id);
						console.debug(target.getParent().getSelected());
					} else {
						target.getParent()._reportUnselected(target.id);
						console.debug(target.getParent().getSelected());
					}
					target.updateDisplay();
					return true;
				}
			},
			// Trigger when moving touch
			onTouchMoved : function(touch, event) {
				// Move the position of current button sprite

			},
			// Process the touch end event
			onTouchEnded : function(touch, event) {

			}
		});

		//
		var leftPadding = this.config["left-padding"];
		var rightPadding = this.config["right-padding"];
		var topPadding = this.config["top-padding"];
		var bottomPadding = this.config["bottom-padding"];

		if (this.config.align === 'left') {
			// all cards align to left
			var offset = 28;

			var index = 0;
			for (var i = 0; i < this.cards.length; i++) {
				// get sprite for card
				var sprite = CardPack.cards[this.cards[i].id];

				// scale card sprite to fix size of card section

				sprite.setScale((this.size.height - topPadding - bottomPadding)
						/ sprite.getContentSize().height);

				sprite.setAnchorPoint(new cc.Point(0, 0));
				sprite.setPosition(new cc.Point(leftPadding + offset * i,
						bottomPadding));
				// card section is selectable, all card should add event
				// listener
				if (this.config.selectable == true) {
					cc.eventManager.addListener(touchEventListener.clone(),
							sprite);
				}
				this.addChild(sprite);
			}
		} else {
			// align to right
			var offset = 28;
			var index = 0;

			// calculate the left point
			leftPadding = this.size.width
					- ((this.cards.length - 1) * offset + rightPadding + 138/*
																			 * add
																			 * card's
																			 * width
																			 */);

			for (var i = 0; i < this.cards.length; i++) {
				// get sprite for card
				var sprite = CardPack.cards[this.cards[i].id];

				// scale card sprite to fix size of card section

				sprite.setScale((this.size.height - topPadding - bottomPadding)
						/ sprite.getContentSize().height);

				sprite.setAnchorPoint(new cc.Point(0, 0));
				sprite.setPosition(new cc.Point(leftPadding + offset * i,
						bottomPadding));

				// card section is selectable, all card should add event
				// listener
				if (this.config.selectable == true) {
					cc.eventManager.addListener(touchEventListener.clone(),
							sprite);
				}

				this.addChild(sprite);
			}
		}
	}
});

var NotifyCardDock = HandCardDock.extend({
	ctor : function(size, config) {
		config.selectable = false;
		this._super(size, config);

	},
	/***************************************************************************
	 * override functions
	 */
	putCard : function(cards) {
		this.cards = new Array();
		this._super(cards);
	}
});

/**
 * don't show actual cards information, only show the card amount
 */
var HiddenCardDock = HandCardDock.extend({
	ctor : function(size, config) {
		this._super(size, config);
	},
	updateDisplay : function() {
		/**
		 * sprite order and tag: card icon, 1,1 card amount 2,2
		 */
		// remove all sprite
		this.removeChildByTag(1);
		this.removeChildByTag(2);

		// create card icon
		var cardIcon = new cc.Sprite(cc.SpriteFrame.create(RES.image.cards,
				new cc.Rect(124 * 2, 168 * 4, 124, 168)));
		cardIcon.setScale(44 / 168);

		cardIcon.setAnchorPoint(new cc.Point(0, 0));

		if (this.config["align"] != undefined
				& this.config["align"] === "right") {
			cardIcon.setPosition(new cc.Point(this.size.width - 36, 4));

		} else {
			// default left
			cardIcon.setPosition(new cc.Point(4, 4));
		}

		this.addChild(cardIcon, 1, 1);
		// create card amount

		var cardAmount = new cc.LabelTTF.create(this.cards.length + "",
				"Arial", 44);
		cardAmount.setFontFillColor(new cc.Color(253, 173, 3, 255));

		cardAmount.setAnchorPoint(new cc.Point(0, 0));

		if (this.config["align"] != undefined
				& this.config["align"] === "right") {
			cardAmount.setPosition(new cc.Point(4, 4));

		} else {
			// default left
			cardAmount.setPosition(new cc.Point(40, 4));
		}

		this.addChild(cardAmount, 2, 2);
	}
});

var Tip = cc.Node.extend({
	ctor : function(text, align) {
		this._super();
		// construct border
		if (align == undefined | align === "left") {
			var backgroud = new cc.Sprite(RES.image.tip_left);
		} else {
			var backgroud = new cc.Sprite(RES.image.tip_right);
		}
		backgroud.setAnchorPoint(new cc.Point(0, 0));
		backgroud.setPosition(new cc.Point(0, 0));

		this.addChild(backgroud, 0, 0);
		// show text
		var text = new cc.LabelTTF.create(text + "", "Arial", 60);
		text.setFontFillColor(new cc.Color(253, 173, 3, 255));
		text.setAnchorPoint(new cc.Point(0.5, 0.5));
		if (align == undefined | align === "left") {
			text.setPosition(new cc.Point(96, 42));
		} else {
			text.setPosition(new cc.Point(64, 42));
		}

		this.addChild(text, 1, 1);
	}

});

/**
 * exclusive show section. in which only one child is visible
 */
var ShowSection = cc.Node.extend({
	ctor : function() {
		this._super();
	},
	/**
	 * protected functions
	 */
	// show only this node, hide all other children
	_show : function(node) {
		var allChildren = this.getChildren();
		for (var i = 0; i < allChildren.length; i++) {
			allChildren[i].setVisible(false);
		}

		node.setVisible(true);
	}
});

/*******************************************************************************
 * Timer
 * 
 * 
 */

var Timer = cc.Node
		.extend({
			tags : {
				timeLabel : 11
			},
			callback : null,
			timeout : 0,
			remaining : 0,
			ctor : function(timeout, callback) {
				this._super();

				this.callback = callback;
				this.timeout = timeout;
				this.remaining = this.timeout;

				// construct UI
				var timerSprite = new cc.Sprite(RES.image.timer);
				timerSprite.setAnchorPoint(new cc.Point(0, 0));
				timerSprite.setPosition(new cc.Point(0, 0));

				this.addChild(timerSprite);

				// create new time label
				var timeLabel = new cc.LabelTTF.create(this.remaining + "",
						"Arial", 24);
				timeLabel.setFontFillColor(new cc.Color(0, 0, 0, 255));

				timeLabel.setAnchorPoint(new cc.Point(0.5, 0.5));
				timeLabel.setPosition(new cc.Point(38, 38));

				this.addChild(timeLabel, 1, this.tags.timeLabel);

			},
			start : function() {
				var theTimer = this;
				this.scheduler.scheduleCallbackForTarget(this,
						function(elasped) {
							theTimer.remaining = Math.round(theTimer.remaining
									- elasped);
							theTimer.getChildByTag(theTimer.tags.timeLabel)
									.setString(theTimer.remaining + "");
							if (theTimer.remaining <= 0) {
								// call callback

								if (theTimer.callback != undefined
										& theTimer.callback != null) {
									setTimeout(theTimer.callback, 500);
								}
							}
						}, 1, this.timeout - 1, 0);

			}
		});

/**
 * the avatar section
 */
var AvatarSection = cc.Node.extend({
	ctor : function() {
		this._super();

		/**
		 * the z order top to bottom
		 * 
		 * hightlight {order:20,tag:20} border{order:10,tag:10}
		 * avatar{order:0:tag:0}
		 */

		var border = new cc.DrawNode();
		border.drawRect(new cc.Point(0, 0), new cc.Point(100, 5), new cc.Color(
				255, 255, 255, 255), 0, new cc.Color(255, 255, 255, 255));
		border.drawRect(new cc.Point(0, 0), new cc.Point(5, 100), new cc.Color(
				255, 255, 255, 255), 0, new cc.Color(255, 255, 255, 255));
		border.drawRect(new cc.Point(0, 95), new cc.Point(100, 100),
				new cc.Color(255, 255, 255, 255), 0, new cc.Color(255, 255,
						255, 255));
		border.drawRect(new cc.Point(95, 0), new cc.Point(100, 100),
				new cc.Color(255, 255, 255, 255), 0, new cc.Color(255, 255,
						255, 255));

		border.setAnchorPoint(new cc.Point(0, 0));
		border.setPosition(new cc.Point(0, 0));

		this.addChild(border, 10, 10);

	},
	setAvatar : function(image) {
		// remove existed avatar sprite
		this.removeChildByTag(0);

		// 
		var avatarSprite = new cc.Sprite(image);
		
		// scale avatar to 100x100
		console.debug("avatar width:"+avatarSprite.width+",height:"+avatarSprite.height);
		avatarSprite.setScaleX(100/180);
		avatarSprite.setScaleY(100/180);
		
		avatarSprite.setAnchorPoint(new cc.Point(0, 0));
		avatarSprite.setPosition(new cc.Point(0, 0));

		this.addChild(avatarSprite, 0, 0);

	}
});

/**
 * abstract class,
 */
var Seat = cc.Node.extend({
	player : null,
	cardDocks : {},
	ctor : function(player) {
		this._super();
		this.player = player;
	},
	/***************************************************************************
	 * public functions
	 */
	putCard : function(cardDockName, cards) {
		var cardDock = this.findCardDock(cardDockName);
		if (cardDock != undefined & cardDock != null) {
			cardDock.putCard(cards);
		} else {
			console.log("no card dock found by name:" + cardDockName);
		}
	},
	removeCard : function(cardDockName, cards) {
		var cardDock = this.findCardDock(cardDockName);
		if (cardDock != undefined & cardDock != null) {
			cardDock.removeCard(cards);
		} else {
			console.log("no card dock found by name:" + cardDockName);
		}
	},
	cleanCard : function(cardDockName) {
		var cardDock = this.findCardDock(cardDockName);
		if (cardDock != undefined & cardDock != null) {
			cardDock.removeAll();
		} else {
			console.log("no card dock found by name:" + cardDockName);
		}
	},
	showTip : function(text) {
	},
	setTimer : function(timeout) {
	},
	cleanTimer : function() {
	},
	cleanNotify : function() {
	},
	
	/***************************************************************************
	 * protected functions
	 */
	findCardDock : function(cardDockName) {
	}
});

var SelfSeat = Seat.extend({
	tags : {
		avatarSection : 1,
		putCardButton : 11,
		appendCardButton : 12,
		passButton : 13,
		handCardDock : 21,
		putCardDock : 22,
		tipBox : 31,
		timer : 41
	},
	ctor : function(player) {
		this._super(player);

		// construct child UI

		// construct avatar section

		var avatarSection = new AvatarSection();
		avatarSection.setAvatar(this.player.avatar);

		avatarSection.setAnchorPoint(new cc.Point(0, 0));
		avatarSection.setPosition(new cc.Point(14, 228));

		this.addChild(avatarSection, 0, this.tags.avatarSection);

		// construct buttons
		var theSelfSeat = this;
		// construct put card button
		var putCardBn = ccui.Button.create();
		putCardBn.loadTextures(RES.image.button, RES.image.button, "");
		putCardBn.setTitleText("出牌");
		putCardBn.setTitleFontSize(40);
		putCardBn.addTouchEventListener(function(sender, type) {
			switch (type) {
			case ccui.Widget.TOUCH_BEGAN:
				console.debug("Touch Down");
				break;

			case ccui.Widget.TOUCH_MOVED:
				console.debug("Touch Move");
				break;

			case ccui.Widget.TOUCH_ENDED:
				// construct put card message and send

				var handCardDock = theSelfSeat
						.getChildByTag(theSelfSeat.tags.handCardDock);
				var selectedCards = handCardDock.getSelected();

				var putCardMsg = new PutCard(fc.self.id, selectedCards);

				fc.room.send(putCardMsg);

				break;

			case ccui.Widget.TOUCH_CANCELED:
				console.debug("Touch Cancelled");
				break;

			default:
				break;
			}
		}, putCardBn);

		putCardBn.setVisible(false);
		putCardBn.setTouchEnabled(false);

		putCardBn.setAnchorPoint(new cc.Point(0, 0));
		putCardBn.setPosition(new cc.Point(128, 240));

		this.addChild(putCardBn, 0, this.tags.putCardButton);

		// construct append card button

		var appendCardBn = ccui.Button.create();
		appendCardBn.loadTextures(RES.image.button, RES.image.button, "");
		appendCardBn.setTitleText("出牌");
		appendCardBn.setTitleFontSize(40);
		appendCardBn.addTouchEventListener(function(sender, type) {
			switch (type) {
			case ccui.Widget.TOUCH_BEGAN:
				console.debug("Touch Down");
				break;

			case ccui.Widget.TOUCH_MOVED:
				console.debug("Touch Move");
				break;

			case ccui.Widget.TOUCH_ENDED:
				// construct put card message and send

				var handCardDock = theSelfSeat
						.getChildByTag(theSelfSeat.tags.handCardDock);
				var selectedCards = handCardDock.getSelected();

				var appendCardMsg = new AppendCard(fc.self.id, selectedCards);

				fc.room.send(appendCardMsg);

				break;

			case ccui.Widget.TOUCH_CANCELED:
				console.debug("Touch Cancelled");
				break;

			default:
				break;
			}
		}, appendCardBn);

		appendCardBn.setVisible(false);
		appendCardBn.setTouchEnabled(false);

		appendCardBn.setAnchorPoint(new cc.Point(0, 0));
		appendCardBn.setPosition(new cc.Point(128, 240));

		this.addChild(appendCardBn, 0, this.tags.appendCardButton);

		// construct pass button

		var passBn = ccui.Button.create();
		passBn.loadTextures(RES.image.button, RES.image.button, "");
		passBn.setTitleText("不出");
		passBn.setTitleFontSize(40);
		passBn.addTouchEventListener(function(sender, type) {
			switch (type) {
			case ccui.Widget.TOUCH_BEGAN:
				console.debug("Touch Down");
				break;

			case ccui.Widget.TOUCH_MOVED:
				console.debug("Touch Move");
				break;

			case ccui.Widget.TOUCH_ENDED:
				// construct pass message and send

				var passMsg = new Pass(fc.self.id);

				fc.room.send(passMsg);

				break;

			case ccui.Widget.TOUCH_CANCELED:
				console.debug("Touch Cancelled");
				break;

			default:
				break;
			}
		}, passBn);

		passBn.setVisible(false);
		passBn.setTouchEnabled(false);

		passBn.setAnchorPoint(new cc.Point(0, 0));
		passBn.setPosition(new cc.Point(296, 240));

		this.addChild(passBn, 0, this.tags.passButton);

		// construct card docks

		// construct hand card dock

		var handCardDock = new HandCardDock(new cc.Size(960, 228), {
			"align" : "left",
			"left-padding" : 10,
			"right-padding" : 10,
			"top-padding" : 54,
			"bottom-padding" : 10,
			selectable : true
		});

		handCardDock.setAnchorPoint(new cc.Point(0, 0));
		handCardDock.setPosition(new cc.Point(0, 0));

		this.addChild(handCardDock, 0, this.tags.handCardDock);

		// construct put card dock

		var putCardDock = new NotifyCardDock(
				new cc.Size(392, 126/* 3/4 of card size */), {
					"align" : "right",
					"left-padding" : 0,
					"right-padding" : 0,
					"top-padding" : 0,
					"bottom-padding" : 0,
					selectable : false
				});

		putCardDock.setAnchorPoint(new cc.Point(0, 0));
		putCardDock.setPosition(new cc.Point(440, 246));

		this.addChild(putCardDock, 0, this.tags.putCardDock);

	},
	/***************************************************************************
	 * override methods
	 */
	findCardDock : function(cardDockName) {
		if (cardDockName != undefined & cardDockName != null) {
			if (cardDockName === "handCardDock") {
				return this.getChildByTag(this.tags.handCardDock);
			} else if (cardDockName === "putCardDock") {
				return this.getChildByTag(this.tags.putCardDock);
			}
		}
	},
	showTip : function(text) {
		var tipBox = new Tip(text, "left");
		tipBox.setAnchorPoint(new cc.Point(0, 0));
		tipBox.setPosition(new cc.Point(440, 246));

		// remove old tip box
		this.removeChildByTag(this.tags.tipBox);

		this.addChild(tipBox, 0, this.tags.tipBox);

		// hide put card dock
		var putCardDock = this.getChildByTag(this.tags.putCardDock);
		putCardDock.setVisible(false);

	},
	putCard : function(cardDockName, cards) {
		this._super(cardDockName, cards);

		if (cardDockName === "putCardDock") {
			// remove tip box
			this.removeChildByTag(this.tags.tipBox);
			var putCardDock = this.getChildByTag(this.tags.putCardDock);
			if (putCardDock != undefined) {
				putCardDock.setVisible(true);
			}
		}
	},
	setTimer : function(timeout) {
		// remove old
		this.removeChildByTag(this.tags.timer);

		var theSeat = this;
		var timer = new Timer(timeout, function() {
			console.log("seat:" + theSeat.player.id + " timeout");
		});
		timer.setAnchorPoint(new cc.Point(0, 0));
		timer.setPosition(new cc.Point(832, 228));

		this.addChild(timer, 0, this.tags.timer);

		timer.start();
	},
	cleanTimer : function() {
		this.removeChildByTag(this.tags.timer);
	},
	cleanNotify : function() {
		// clean timer
		this.cleanTimer();
		// clean tip
		this.removeChildByTag(this.tags.tipBox);
		// clean put card dock
		this.cleanCard("putCardDock");
	}
});

var LeftSeat = Seat.extend({
	tags : {
		avatarSection : 1,
		nameBar : 11,
		titleBar : 12,
		handCardDock : 21,
		putCardDock : 22,
		tipBox : 31,
		timer : 41
	},
	ctor : function(player) {
		this._super(player);

		// construct UI

		// construct avatar section

		var avatarSection = new AvatarSection();
		avatarSection.setAvatar(this.player.avatar);

		avatarSection.setAnchorPoint(new cc.Point(0, 0));
		avatarSection.setPosition(new cc.Point(14, 112));

		this.addChild(avatarSection, 0, this.tags.avatarSection);

		// construct name bar
		var nameBar = cc.LabelTTF.create(this.player.name, "Arial", 24,
				new cc.Size(0, 0), cc.TEXT_ALIGNMENT_CENTER,
				cc.VERTICAL_TEXT_ALIGNMENT_CENTER);
		nameBar.setAnchorPoint(new cc.Point(0, 0));
		nameBar.setPosition(new cc.Point(14, 80));

		this.addChild(nameBar, 0, this.tags.nameBar);
		// construct title bar
		var titleBar = cc.LabelTTF.create("Mock title", "Arial", 24,
				new cc.Size(0, 0), cc.TEXT_ALIGNMENT_CENTER,
				cc.VERTICAL_TEXT_ALIGNMENT_CENTER);
		titleBar.setAnchorPoint(new cc.Point(0, 0));
		titleBar.setPosition(new cc.Point(14, 48));

		this.addChild(titleBar, 0, this.tags.titleBar);
		// construct card docks

		// construct hand card dock

		var handCardDock = new HiddenCardDock(new cc.Size(100, 48), {
			align : "left"
		});
		handCardDock.setAnchorPoint(new cc.Point(0, 0));
		handCardDock.setPosition(new cc.Point(14, 0));

		this.addChild(handCardDock, 0, this.tags.handCardDock);

		// construct put card dock

		var putCardDock = new NotifyCardDock(new cc.Size(334, 126), {
			"align" : "left",
			"left-padding" : 0,
			"right-padding" : 0,
			"top-padding" : 0,
			"bottom-padding" : 0,
			selectable : false
		});

		putCardDock.setAnchorPoint(new cc.Point(0, 0));
		putCardDock.setPosition(new cc.Point(132, -25));

		this.addChild(putCardDock, 0, this.tags.putCardDock);
	},
	/***************************************************************************
	 * override methods
	 */
	findCardDock : function(cardDockName) {
		if (cardDockName != undefined & cardDockName != null) {
			if (cardDockName === "handCardDock") {
				return this.getChildByTag(this.tags.handCardDock);
			} else if (cardDockName === "putCardDock") {
				return this.getChildByTag(this.tags.putCardDock);
			}
		}
	},
	showTip : function(text) {
		var tipBox = new Tip(text, "left");
		tipBox.setAnchorPoint(new cc.Point(0, 0));
		tipBox.setPosition(new cc.Point(132, -25));

		// remove old tip box
		this.removeChildByTag(this.tags.tipBox);

		this.addChild(tipBox, 0, this.tags.tipBox);

		// hide put card dock
		var putCardDock = this.getChildByTag(this.tags.putCardDock);
		putCardDock.setVisible(false);

	},
	putCard : function(cardDockName, cards) {
		this._super(cardDockName, cards);

		if (cardDockName === "putCardDock") {
			// remove tip box
			this.removeChildByTag(this.tags.tipBox);
			var putCardDock = this.getChildByTag(this.tags.putCardDock);
			if (putCardDock != undefined) {
				putCardDock.setVisible(true);
			}
		}
	},
	setTimer : function(timeout) {
		// remove old
		this.removeChildByTag(this.tags.timer);

		var theSeat = this;
		var timer = new Timer(timeout, function() {
			console.log("seat:" + theSeat.player.id + " timeout");
		});
		timer.setAnchorPoint(new cc.Point(0, 0));
		timer.setPosition(new cc.Point(14, -76));

		this.addChild(timer, 0, this.tags.timer);

		timer.start();
	},
	cleanTimer : function() {
		this.removeChildByTag(this.tags.timer);
	},
	cleanNotify : function() {
		// clean timer
		this.cleanTimer();
		// clean tip
		this.removeChildByTag(this.tags.tipBox);
		// clean put card dock
		this.cleanCard("putCardDock");
	}
});

var RightSeat = Seat.extend({
	tags : {
		avatarSection : 1,
		nameBar : 11,
		titleBar : 12,
		handCardDock : 21,
		putCardDock : 22,
		tipBox : 31,
		timer : 41
	},
	ctor : function(player) {
		this._super(player);

		// construct UI

		// construct avatar section

		var avatarSection = new AvatarSection();
		avatarSection.setAvatar(this.player.avatar);

		avatarSection.setAnchorPoint(new cc.Point(0, 0));
		avatarSection.setPosition(new cc.Point(14, 112));

		this.addChild(avatarSection, 0, this.tags.avatarSection);

		// construct name bar
		var nameBar = cc.LabelTTF.create(this.player.name, "Arial", 24,
				new cc.Size(0, 0), cc.TEXT_ALIGNMENT_CENTER,
				cc.VERTICAL_TEXT_ALIGNMENT_CENTER);
		nameBar.setAnchorPoint(new cc.Point(0, 0));
		nameBar.setPosition(new cc.Point(14, 80));

		this.addChild(nameBar, 0, this.tags.nameBar);
		// construct title bar
		var titleBar = cc.LabelTTF.create("Mock title", "Arial", 24,
				new cc.Size(0, 0), cc.TEXT_ALIGNMENT_CENTER,
				cc.VERTICAL_TEXT_ALIGNMENT_CENTER);
		titleBar.setAnchorPoint(new cc.Point(0, 0));
		titleBar.setPosition(new cc.Point(14, 48));

		this.addChild(titleBar, 0, this.tags.titleBar);
		// construct card docks

		// construct hand card dock

		var handCardDock = new HiddenCardDock(new cc.Size(100, 48), {
			align : "right"
		});
		handCardDock.setAnchorPoint(new cc.Point(0, 0));
		handCardDock.setPosition(new cc.Point(14, 0));

		this.addChild(handCardDock, 0, this.tags.handCardDock);

		// construct put card dock

		var putCardDock = new NotifyCardDock(new cc.Size(334, 126), {
			"align" : "right",
			"left-padding" : 0,
			"right-padding" : 0,
			"top-padding" : 0,
			"bottom-padding" : 0,
			selectable : false
		});

		putCardDock.setAnchorPoint(new cc.Point(0, 0));
		putCardDock.setPosition(new cc.Point(-334, -25));

		this.addChild(putCardDock, 0, this.tags.putCardDock);
	},
	/***************************************************************************
	 * override methods
	 */
	findCardDock : function(cardDockName) {
		if (cardDockName != undefined & cardDockName != null) {
			if (cardDockName === "handCardDock") {
				return this.getChildByTag(this.tags.handCardDock);
			} else if (cardDockName === "putCardDock") {
				return this.getChildByTag(this.tags.putCardDock);
			}
		}
	},
	showTip : function(text) {
		var tipBox = new Tip(text, "right");
		tipBox.setAnchorPoint(new cc.Point(0, 0));
		tipBox.setPosition(new cc.Point(-160, -25));

		// remove old tip box
		this.removeChildByTag(this.tags.tipBox);

		this.addChild(tipBox, 0, this.tags.tipBox);

		// hide put card dock
		var putCardDock = this.getChildByTag(this.tags.putCardDock);
		putCardDock.setVisible(false);

	},
	putCard : function(cardDockName, cards) {
		this._super(cardDockName, cards);

		if (cardDockName === "putCardDock") {
			// remove tip box
			this.removeChildByTag(this.tags.tipBox);
			var putCardDock = this.getChildByTag(this.tags.putCardDock);
			if (putCardDock != undefined) {
				putCardDock.setVisible(true);
			}
		}
	},
	setTimer : function(timeout) {
		// remove old
		this.removeChildByTag(this.tags.timer);

		var theSeat = this;
		var timer = new Timer(timeout, function() {
			console.log("seat:" + theSeat.player.id + " timeout");
		});
		timer.setAnchorPoint(new cc.Point(0, 0));
		timer.setPosition(new cc.Point(0, -76));

		this.addChild(timer, 0, this.tags.timer);

		timer.start();
	},
	cleanTimer : function() {
		this.removeChildByTag(this.tags.timer);
	},
	cleanNotify : function() {
		// clean timer
		this.cleanTimer();
		// clean tip
		this.removeChildByTag(this.tags.tipBox);
		// clean put card dock
		this.cleanCard("putCardDock");
	}
});

/*******************************************************************************
 * scenes
 */

/*******************************************************************************
 * waiting player scene
 */

var WaitSeatLayer = cc.LayerGradient.extend({
	ctor : function(seatSize) {
		this._super(new cc.Color(4, 48, 87, 255), new cc.Color(31, 106, 161,
				255));

		for (var i = 0; i < seatSize; i++) {
			var avatarSection1 = new AvatarSection();

			avatarSection1.setAnchorPoint(new cc.Point(0, 0));
			avatarSection1.setPosition(new cc.Point(50 + 128 * i, 50));

			this.addChild(avatarSection1, 0, i);
		}
	},
	addPlayer : function(player, position) {
		this.getChildByTag(position).setAvatar(player.avatar);
	}
});

var WaitScene = cc.Scene.extend({
	onEnter : function() {
		this._super();
		var seatLayer = new WaitSeatLayer(fc.roomInfo.seatSize);
		this.addChild(seatLayer, 1, 1);

		console.debug("enter eait scene:" + new Date());
	},
	onEnterTransitionDidFinish : function() {
		// send join room message

		var joinRoomMsg = new JoinRoom(fc.self.id, fc.roomInfo.id, -1);

		fc.room.send(joinRoomMsg);
	}
});

/*******************************************************************************
 * play scene
 */

var PlayLayer = cc.LayerGradient.extend({
	ctor : function(self, players) {
		this._super(new cc.Color(4, 48, 87, 255), new cc.Color(31, 106, 161,
				255));

		// find the position of self
		var selfPos = -1;
		for (var i = 0; i < players.length; i++) {
			if (players[i].id == self.id) {
				selfPos = i;
				break;
			}
		}

		for (var i = 0; i < players.length; i++) {
			if (i == (selfPos + players.length - 1) % players.length) {
				// players who on the left of self should use selfSeat
				// add left seat

				var leftSeat = new LeftSeat(players[i]);
				leftSeat.setAnchorPoint(new cc.Point(0, 0));
				leftSeat.setPosition(new cc.Point(0, 412));
				fc.room.seats.push(leftSeat);
				this.addChild(leftSeat);
			} else if (i == selfPos) {
				var selfSeat = new SelfSeat(players[i]);
				selfSeat.setAnchorPoint(new cc.Point(0, 0));
				selfSeat.setPosition(new cc.Point(0, 0));
				fc.room.seats.push(selfSeat);
				this.addChild(selfSeat, 0);
			} else if (i == (selfPos + 1) % players.length) {
				// add right seat

				var rightSeat = new RightSeat(players[i]);

				rightSeat.setAnchorPoint(new cc.Point(0, 0));
				rightSeat.setPosition(new cc.Point(832, 412));

				fc.room.seats.push(rightSeat);
				this.addChild(rightSeat);
			}
		}
	}
});

var PlayScene = cc.Scene.extend({
	self : null,
	players : null,
	ctor : function(self, players) {
		this._super();
		this.self = self;
		this.players = players;
	},
	onEnter : function() {
		this._super();
		var seatLayer = new PlayLayer(this.self, this.players);
		this.addChild(seatLayer, 1, 1);

		// send ready message

		var readyMsg = new Ready(fc.self.id);

		fc.room.send(readyMsg);
	}
});

var AskBetDialog = cc.Layer.extend({
	ctor : function(callback) {

		// ////////////////////////////
		// 1. super init first
		this._super();

		var size = cc.director.getWinSize();

		// this.init(cc.color(255,0,0,128), size.width/2,size.height/2);

		// add dialog background

		var bgNode = cc.DrawNode.create();
		bgNode.drawRect(new cc.Point((size.width - 384) / 2,
				(size.height - 279) / 2), new cc.Point(
				(size.width - 384) / 2 + 384, (size.height - 279) / 2 + 279),
				new cc.Color(255, 0, 0, 255), 1, new cc.Color(255, 0, 0, 255));

		// bgNode.setAnchorPoint(new cc.Point(0.5,0.5));
		// bgNode.setPosition(new cc.Point(size.width/2,size.height/2));

		this.addChild(bgNode);

		var textField = ccui.TextField.create();
		textField.setTouchEnabled(true);
		textField.fontName = "Marker Felt";
		textField.fontSize = 30;
		textField.placeHolder = "input words here";
		textField.x = size.width / 2.0;
		textField.y = size.height / 2.0;

		textField.addEventListenerTextField(function(sender, type) {
			var textField = sender;
			console.debug(type);
		}, this);
		this.addChild(textField, 1, 1);

		// add button
		var confirmBn = cc.Sprite.create(RES.image.button);
		var confirmLabel = cc.LabelTTF.create("OK", "FreeMono Bold", 32);
		confirmLabel.setFontFillColor(new cc.Color(0, 0, 0));
		confirmLabel.setPosition(new cc.Point(
				confirmBn.getContentSize().width / 2, confirmBn
						.getContentSize().height / 2));
		confirmBn.addChild(confirmLabel);
		confirmBn.setPosition(new cc.Point(size.width / 2, size.height / 2
				- (279 / 2 - 50)));
		this.addChild(confirmBn);

		var theDialog = this;
		var confirmBtEventListener = cc.EventListener.create({
			event : cc.EventListener.TOUCH_ONE_BY_ONE,
			swallowTouches : true,
			onTouchBegan : function(touch, event) {
				// event.getCurrentTarget() returns the *listener's*
				// sceneGraphPriority node.
				var target = event.getCurrentTarget();
				// console.debug(target);
				// Get the position of the current point relative to the
				// button
				var locationInNode = target.convertToNodeSpace(touch
						.getLocation());
				var s = target.getContentSize();
				var rect = cc.rect(0, 0, s.width, s.height);

				// Check the click area
				if (cc.rectContainsPoint(rect, locationInNode)) {

					console.debug("get input:" + textField.getStringValue());

					if (callback != undefined) {
						callback(textField.getStringValue());
					}

					// remove self
					theDialog.getParent().removeChild(theDialog, true);
					return true;
				}

			},
			// Trigger when moving touch
			onTouchMoved : function(touch, event) {
				// Move the position of current button sprite

			},
			// Process the touch end event
			onTouchEnded : function(touch, event) {

			}
		});

		cc.eventManager.addListener(confirmBtEventListener, confirmBn);

	}
});
/*******************************************************************************
 * room
 */
fc["room"] = {
	/***************************************************************************
	 * properties
	 */
	config : {
		wsEndPoint : "ws://" + fc.serviceInfo.host + ":" + fc.serviceInfo.port
				+ "/service/user"
	},
	webSocket : null,
	seats : new Array(),
	players : new Array(),
	seatPosOff : 0,
	/***************************************************************************
	 * runtime flags
	 */
	state : "INITIALIZED",

	/***************************************************************************
	 * UI
	 */
	scenes : {
		waitPlayerScene : null,
		mainScene : null
	},
	/***************************************************************************
	 * message handlers
	 */
	// handle incoming message
	messageHandlers : {},
	// handle ask actions message, enable actions for user performing
	actionEnablers : {},

	init : function() {
		// build message handlers
		this.buildMessageHandler();
		// build action handlers
		this.buildActionHandler();

		// init room, connect to server
		this.connect();

	},
	send : function(message) {
		this.webSocket.send(JSON.stringify(message));
	},

	/***************************************************************************
	 * private supported functions
	 */
	/**
	 * connect to server
	 */
	connect : function() {
		this.webSocket = new WebSocket(this.config.wsEndPoint + "/"
				+ fc.self.id);
		var thisRoom = this;
		// set web socket message handler
		this.webSocket.onopen = function() {
			thisRoom.onWsOpen();
		};

		this.webSocket.onclose = function() {
			thisRoom.onWsClose();
		};
		this.webSocket.onerror = function(error) {
			thisRoom.onWsError(error);
		};
		this.webSocket.onmessage = function(event) {
			thisRoom.onWsMessage(event);
		};
	},
	/**
	 * handle web socket open event
	 */
	onWsOpen : function() {
		console.debug("ws is connected." + this.webSocket);

		this.scenes.playScene = new PlayScene(fc.self,this.players);
		cc.director.runScene(this.scenes.playScene);
	},
	/**
	 * handle web socket close event
	 */
	onWsClose : function() {
		console.debug("ws is close.");
	},
	/**
	 * handle web socket error event
	 */
	onWsError : function(error) {
		console.debug("ws is error." + error);
	},
	/**
	 * handle web socket receive message event
	 */
	onWsMessage : function(event) {
		var msg = JSON.parse(event.data);
		console.debug(msg);
		var msgHandler = this.messageHandlers[msg.cla + "-" + msg.ins];
		if (msgHandler != undefined) {
			msgHandler(msg);
		} else {
			console.info("unhandled msg:" + JSON.stringify(msg));
		}
	},

	/***************************************************************************
	 * private supported functions
	 */
	lookupSeat : function(userId) {
		for (var i = 0; i < this.seats.length; i++) {
			if (this.seats[i].player.id === userId) {
				return this.seats[i];
			}
		}
		return undefined;
	},
	buildMessageHandler : function() {
		// add message handler
		var theHandlers = this.messageHandlers;

		// notification handler
		this.messageHandlers[MESSAGE.NOTIFICATION.key] = function(msg) {
			// 
			var event = msg.msg;

			var actualHandler = theHandlers[event.cla + "-" + event.ins];
			if (actualHandler != undefined) {
				actualHandler(event);
			} else {
				console.info("unhandled msg:" + JSON.stringify(event));
			}
		}

		var theRoom = this;
		// join room handler
		this.messageHandlers[MESSAGE.JOIN_ROOM.key] = function(msg) {

			console.debug("join message:" + new Date());
			// get user information
			var player = {
				id : msg.userId,
				avatar : "farmer.png"
			};
			// store it. seats should be rendered after all players joint room.
			theRoom.players[msg.position] = player;
			var playerBoardLayer = theRoom.scenes.waitPlayerScene
					.getChildByTag(1);
			playerBoardLayer.addPlayer(player, msg.position);

		};

		// start game handler
		this.messageHandlers[MESSAGE.START_GAME.key] = function(msg) {
			// init
			theRoom.scenes.mainScene = new PlayScene(fc.self, theRoom.players);
			cc.director.runScene(theRoom.scenes.mainScene);

		}
		// end game handler
		this.messageHandlers[MESSAGE.END_GAME.key] = function(msg) {
			// show result
			// TODO
			alert("Game Over");
		}

		// ask handler
		this.messageHandlers[MESSAGE.ASK.key] = function(msg) {
			if (msg.actions != undefined) {
				// disable all actions
				for ( var key in theRoom.actionEnablers) {
					if (theRoom.actionEnablers.hasOwnProperty(key)) {
						theRoom.actionEnablers[key].disable();
					}
				}
				if (msg.targetUserId === fc.self.id) {
					for (var i = 0; i < msg.actions.length; i++) {
						var actionEnabler = theRoom.actionEnablers[msg.actions[i][0]
								+ "-" + msg.actions[i][1]];
						if (actionEnabler != undefined & actionEnabler != null) {
							// perform action enabler
							actionEnabler.enable();
						}
					}
				}

				// if actions includes PUT_CARD

				for (var i = 0; i < msg.actions.length; i++) {
					if (msg.actions[i][0] + "-" + msg.actions[i][1] === MESSAGE.PUT_CARD.key) {
						// clean all notify of all seat

						for (var i = 0; i < fc.room.seats.length; i++) {
							fc.room.seats[i].cleanNotify();
						}
						break;
					}
				}

				// clean all old timers

				for (var i = 0; i < fc.room.seats.length; i++) {
					fc.room.seats[i].cleanTimer();
				}

				// set new timer for target seat

				var targetSeat = theRoom.lookupSeat(msg.targetUserId);

				if (targetSeat == null | targetSeat == undefined) {
					console.log("can't find seat for plauer id:"
							+ msg.targetUserId);
				} else {
					targetSeat.setTimer(msg.timeout);
				}
			}

		}

		// deal card handler
		this.messageHandlers[MESSAGE.DEAL_CARD.key] = function(msg) {
			// look up target seat

			for (var i = 0; i < theRoom.seats.length; i++) {
				if (theRoom.seats[i].player.id === msg.toUserId) {
					theRoom.seats[i].putCard("handCardDock", msg.cards);
					break;
				}
			}
		}

		// bet handler
		this.messageHandlers[MESSAGE.BET.key] = function(msg) {
			// look up target seat
			var targetSeat = theRoom.lookupSeat(msg.userId);
			if (targetSeat != undefined) {
				targetSeat.showTip(msg.amount + "");
			}

		}

		// put card and append card handler
		var putAppendCardHandler = function(msg) {
			// lookup target seat
			var targetSeat = theRoom.lookupSeat(msg.userId);
			if (targetSeat != undefined) {
				targetSeat.removeCard("handCardDock", msg.cards);
				targetSeat.putCard("putCardDock", msg.cards);
			}
		}

		this.messageHandlers[MESSAGE.PUT_CARD.key] = putAppendCardHandler;
		this.messageHandlers[MESSAGE.APPEND_CARD.key] = putAppendCardHandler;

		// pass handler
		this.messageHandlers[MESSAGE.PASS.key] = function(msg) {
			// lookup target seat
			var targetSeat = theRoom.lookupSeat(msg.userId);
			if (targetSeat != undefined) {
				// show message
				targetSeat.showTip("Pass");
			}

		}
	},

	buildActionHandler : function() {
		/***********************************************************************
		 * construct action enablers
		 */
		// bet action
		this.actionEnablers[MESSAGE.BET.key] = {
			enable : function() {
				// show dialog
				var askBetDialog = new AskBetDialog(function(amount) {
					var betMsg = new Bet(fc.self.id, amount);

					fc.room.send(betMsg);
				});

				cc.director.getRunningScene().addChild(askBetDialog, 128, 0);
			},
			disable : function() {
				// do nothing
			}
		};
		// put card action enabler
		this.actionEnablers[MESSAGE.PUT_CARD.key] = {
			enable : function() {
				// show put card button

				var selfSeat = fc.room.lookupSeat(fc.self.id);
				var putCardBt = selfSeat
						.getChildByTag(selfSeat.tags.putCardButton);

				putCardBt.setVisible(true);
				putCardBt.setTouchEnabled(true);

			},
			disable : function() {
				var selfSeat = fc.room.lookupSeat(fc.self.id);
				var putCardBt = selfSeat
						.getChildByTag(selfSeat.tags.putCardButton);

				putCardBt.setVisible(false);
				putCardBt.setTouchEnabled(true);
			}
		};

		// append card action enabler
		this.actionEnablers[MESSAGE.APPEND_CARD.key] = {
			enable : function() {
				// show put card button

				var selfSeat = fc.room.lookupSeat(fc.self.id);
				var appendCardBt = selfSeat
						.getChildByTag(selfSeat.tags.appendCardButton);

				appendCardBt.setVisible(true);
				appendCardBt.setTouchEnabled(true);
			},
			disable : function() {
				var selfSeat = fc.room.lookupSeat(fc.self.id);
				var appendCardBt = selfSeat
						.getChildByTag(selfSeat.tags.appendCardButton);

				appendCardBt.setVisible(false);
				appendCardBt.setTouchEnabled(false);
			}
		};

		// pass action enabler
		this.actionEnablers[MESSAGE.PASS.key] = {
			enable : function() {
				// show pass button
				var selfSeat = fc.room.lookupSeat(fc.self.id);
				var passBt = selfSeat.getChildByTag(selfSeat.tags.passButton);

				passBt.setVisible(true);
				passBt.setTouchEnabled(true);
			},
			disable : function() {
				var selfSeat = fc.room.lookupSeat(fc.self.id);
				var passBt = selfSeat.getChildByTag(selfSeat.tags.passButton);

				passBt.setVisible(false);
				passBt.setTouchEnabled(false);
			}
		};

	}
};


