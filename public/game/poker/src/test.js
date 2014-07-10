var TestLayer=cc.LayerGradient.extend({
	init:function(){
		this._super(new cc.Color(4,48,87,255),new cc.Color(31,106,161,255));
		
		/*
		for(var i=0;i<3;i++){
			var avatarSection1 = new AvatarSection();
			
			avatarSection1.setAnchorPoint(new cc.Point(0,0));
			avatarSection1.setPosition(new cc.Point(50+128*i,50));
			
			this.addChild(avatarSection1,0,i);
			
			avatarSection1.setAvatar(RES.image.farmer);
		}
		*/
		/*
		for (var s = CardPack.information.suits.length-1; s >=0; s--) {
			for (var p = 1; p <= 13; p++) {

				var card=CardPack.cards[CardPack.information.suits[s] + "-"
								+ p];
				card.setAnchorPoint(new cc.Point(0,0));
				card.setPosition(new cc.Point(p*50,s*50+178));
				
				this.addChild(card);
			}
		}
		*/
		/*
		var handCardDock = new HiddenCardDock(new cc.Size(100,48),{
			"align" : "left",
			"left-padding" : 10,
			"right-padding" : 10,
			"top-padding" : 54,
			"bottom-padding" : 10,
			selectable : false
		});
		
		handCardDock.setAnchorPoint(new cc.Point(0,0));
		handCardDock.setPosition(new cc.Point(100,200));
		
		this.addChild(handCardDock);
		*/
		/*
		for (var p = 1; p <= 13; p++) {

			//var card=CardPack.cards[CardPack.information.suits[1] + "-"
			//				+ p];
			//card.setAnchorPoint(new cc.Point(0,0));
			//card.setPosition(new cc.Point(p*50,));
			
			handCardDock.putCard({id:"spades-"+p});
		}
		
		var leftTip = new Tip("不出");
		
		leftTip.setPosition(new cc.Point(100,400));
		
		this.addChild(leftTip);
		
		var rightTip = new Tip("12","right");
		rightTip.setPosition(new cc.Point(300,400));
		
		this.addChild(rightTip);
		*/
		
		// test self seat

		var selfSeat = new SelfSeat({id:"test-player-1",avatar:"farmer.png",name:"test player 1"});
		
		selfSeat.setAnchorPoint(new cc.Point(0,0));
		selfSeat.setPosition(new cc.Point(0,0));
		
		this.addChild(selfSeat);
		
		selfSeat.putCard("handCardDock",[{id:"spades-1",suite:"spades",point:1},{id:"spades-2",suite:"spades",point:2}]);
		selfSeat.putCard("putCardDock",[{id:"hearts-1",suite:"hearts",point:1},{id:"hearts-2",suite:"hearts",point:2}]);
		selfSeat.showTip("不出");
		
		selfSeat.putCard("putCardDock",[{id:"hearts-3",suite:"hearts",point:3},{id:"hearts-4",suite:"hearts",point:4}]);
		
		
		// test left seat
		
		var leftSeat = new LeftSeat({id:"test-player-2",avatar:"farmer.png",name:"test player 2"});
		leftSeat.setAnchorPoint(new cc.Point(0,0));
		leftSeat.setPosition(new cc.Point(0,412));
		
		this.addChild(leftSeat);
		
		leftSeat.putCard("handCardDock",[{id:"clubs-1",suite:"clubs",point:1},{id:"clubs-2",suite:"clubs",point:2}]);
		leftSeat.putCard("putCardDock",[{id:"diamonds-1",suite:"diamonds",point:1},{id:"diamonds-2",suite:"diamonds",point:2}]);
		leftSeat.showTip("不出");
		// test right seat
		
		var rightSeat = new RightSeat({id:"test-player-3",avatar:"farmer.png",name:"test player 3"});
		rightSeat.setAnchorPoint(new cc.Point(0,0));
		rightSeat.setPosition(new cc.Point(832,412));
		
		this.addChild(rightSeat);
		
		rightSeat.putCard("handCardDock",[{id:"diamonds-3",suite:"diamonds",point:3},{id:"diamonds-4",suite:"diamonds",point:4}]);
		rightSeat.putCard("putCardDock",[{id:"clubs-3",suite:"clubs",point:3},{id:"clubs-4",suite:"clubs",point:4}]);
		rightSeat.showTip("不出");
		
		
		var askBetDialog = new AskBetDialog(function(amount) {
			var betMsg = new Bet(fc.self.id,amount);

			fc.room.send(betMsg);
		});

		//cc.director.getRunningScene().addChild(askBetDialog,128,0);
		
		// test timer
		
		var timer = new Timer(9,function(){alert("timeout");});
		timer.setPosition(new cc.Point(14,400));
		this.addChild(timer)
		
		timer.start();
		// test button
		/*
		var bn = ccui.Button.create();
		bn.loadTextures(RES.image.button,RES.image.button,"");
		
		bn.setTitleText("出牌");
		bn.setTitleFontSize(60);
		bn.setTouchEnabled(true);
		bn.addTouchEventListener(function(sender,type){
			switch (type) {
            case ccui.Widget.TOUCH_BEGAN:
                console.debug("Touch Down");
                break;

            case ccui.Widget.TOUCH_MOVED:
                console.debug("Touch Move");
                break;

            case ccui.Widget.TOUCH_ENDED:
                console.debug("Touch Up");
                break;

            case ccui.Widget.TOUCH_CANCELED:
                console.debug("Touch Cancelled");
                break;

            default:
                break;
        }
		},bn);
		
		bn.setAnchorPoint(new cc.Point(0,0));
		bn.setPosition(new cc.Point(100,200));
		
		this.addChild(bn);
	*/
	}
});

var TestScene = cc.Scene.extend({
	onEnter : function() {
		this._super();
		var layer = new TestLayer();
		this.addChild(layer, 0);
		layer.init();
	}
});