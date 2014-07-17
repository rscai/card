cc.game.onStart = function() {
	var designSize = cc.size(960, 640);
	var screenSize = cc.view.getFrameSize();

	cc.loader.resPath = "/assets/game/poker/res/HD";
	// cc.FileUtils.getInstance().setSearchResolutionsOrder(resDirOrders);
	// director.setContentScaleFactor(resourceSize.width / designSize.width);
	cc.view.setDesignResolutionSize(designSize.width, designSize.height,
			cc.ResolutionPolicy.EXACT_FIT);
	cc.view.resizeWithBrowserSize(true);

	// turn on display FPS
	cc.director.setDisplayStats(this.config['showFPS']);

	// set FPS. the default value is 1.0/60 if you don't call this
	cc.director.setAnimationInterval(1.0 / this.config['frameRate']);

	// load resources
	cc.LoaderScene.preload(g_resources, function() {
		//room.scenes.waitPlayerScene= new WaitPlayerScene();
		//room.scenes.mainScene=new MainScene();
		//cc.director.runScene(room.scenes.waitPlayerScene);
		
		// for test 
		//fc.roomInfo.seatSize=3;
		
		
		CardPack.init();
		fc.room.players=fc.roomInfo.seats;
		
		// transform avatar url with crosproxy to go pass CROS restriction 
		for(var i=0;i<fc.room.players.length;i++){
			var originalAvatarUrl=fc.room.players[i].avatar;
			originalAvatarUrl="http://www.corsproxy.com/"+originalAvatarUrl.replace("http://","");
			fc.room.players[i].avatar=originalAvatarUrl;
		}
		
		//fc.room.init();
		
		cc.director.runScene(new TestScene());
		
	}, this);

};
cc.game.run();
