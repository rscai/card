/*******************************************************************************
 * Poker
 */
var CardPack = {
	information : {
		suits : [ "spades", "hearts", "diamonds", "clubs" ]
	},
	cards : {},
	init : function() {
		/***********************************************************************
		 * the ordinate of rect start from left-top corner to right-bottom
		 * corner
		 */
		var backImage = cc.SpriteFrame.create(RES.image.cards, new cc.Rect(
				124 * 2, 168 * 4, 124, 168));

		for (var s = 0; s < this.information.suits.length; s++) {
			for (var p = 1; p <= 13; p++) {
				var frontImage = cc.SpriteFrame.create(RES.image.cards,
						new cc.Rect(124 * (p - 1),
								168 * (this.information.suits.length - s - 1),
								124, 168));

				this.cards[this.information.suits[s] + "-" + p] = new Card(
						this.information.suits[s] + "-" + p, frontImage,
						backImage);
			}
		}

		// senior-joker
		this.cards["senior-joker"] = new Card("senior-joker", cc.SpriteFrame
				.create(RES.image.cards,
						new cc.Rect(124 * 0, 168 * 4, 124, 168)), backImage)
		// junior-joker
		this.cards["junior-joker"] = new Card("junior-joker", cc.SpriteFrame
				.create(RES.image.cards,
						new cc.Rect(124 * 1, 168 * 4, 124, 168)), backImage)
	}
};