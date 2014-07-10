/**
 *
 */
package me.firecloud.gamecenter.card.web

import org.junit._
import Assert._
import me.firecloud.gamecenter.card.model.PutCard
import me.firecloud.gamecenter.card.model.Card
import me.firecloud.gamecenter.card.model.Suit
import me.firecloud.gamecenter.model.Notification
import me.firecloud.gamecenter.model.JoinRoom
import me.firecloud.gamecenter.model.Message
import me.firecloud.gamecenter.model.Ask
import me.firecloud.utils.logging.Logging
import me.firecloud.utils.logging.Logging
import me.firecloud.gamecenter.card.model.AppendCard
import me.firecloud.gamecenter.card.model.Pass
import me.firecloud.gamecenter.model.Dealer
import me.firecloud.gamecenter.model.StartGame
import me.firecloud.gamecenter.model.Ready
import me.firecloud.gamecenter.card.model.DealCard

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Jan 4, 2014
 *
 */
class CardMessageCodecTest {

    val codec = new CardMessageCodec()
    val messages = List[Tuple2[Message, String]](
        (
            new JoinRoom("test-player-1", "test-room-1"),
            """{"position":-1,"roomId":"test-room-1","userId":"test-player-1","cla":1,"ins":1}"""
        ),
        (
            new Notification(new JoinRoom("test-player-1", "test-room-1")),
            """{"msg":{"position":-1,"roomId":"test-room-1","userId":"test-player-1","cla":1,"ins":1},"cla":1,"ins":4}"""
        ),
        (
            new Notification(new StartGame("0")),
            """{"msg":{"userId":"0","cla":1,"ins":2},"cla":1,"ins":4}"""
        ),
        (
            new Ready("test-player-1"),
            """{"userId":"test-player-1","cla":1,"ins":6}"""
        ),
        (
            new Notification(new DealCard("0","test-player-1",List(new Card(Suit.spades + "-" + 1, Suit.spades, 1), new Card(Suit.spades + "-" + 2, Suit.spades, 2), new Card(Suit.spades + "-" + 3, Suit.spades, 3)))),
            """{"userId":"test-player-1","cla":1,"ins":6}"""
        ),
        (
            new Ask("test-player-1", List(PutCard.key)),
            """{"actions":[[2,1]],"targetUserId":"test-player-1","cla":1,"ins":5}"""
        ),
        (
            new PutCard("test-player-1", List(new Card(Suit.spades + "-" + 1, Suit.spades, 1), new Card(Suit.spades + "-" + 2, Suit.spades, 2), new Card(Suit.spades + "-" + 3, Suit.spades, 3))),
            """{"cards":[{"id":"spades-1"},{"id":"spades-2"},{"id":"spades-3"}],"userId":"test-player-1","cla":2,"ins":1}"""
        ),
        (
            new Notification(new PutCard("test-player-1", List(new Card(Suit.spades + "-" + 1, Suit.spades, 1), new Card(Suit.spades + "-" + 2, Suit.spades, 2), new Card(Suit.spades + "-" + 3, Suit.spades, 3)))),
            """{"msg":{"cards":[{"id":"spades-1"},{"id":"spades-2"},{"id":"spades-3"}],"userId":"test-player-1","cla":2,"ins":1},"cla":1,"ins":4}"""
        ),
        (
            new Ask("test-player-2", List(AppendCard.key, Pass.key)),
            """{"actions":[[2,4],[2,2]],"targetUserId":"test-player-2","cla":1,"ins":5}"""
        ),
        (
            new AppendCard("test-player-2", List(new Card(Suit.spades + "-" + 4, Suit.spades,4), new Card(Suit.spades + "-" + 5, Suit.spades, 5), new Card(Suit.spades + "-" + 6, Suit.spades, 6))),
            """{"cards":[{"id":"spades-4"},{"id":"spades-5"},{"id":"spades-6"}],"userId":"test-player-2","cla":2,"ins":4}"""
        ),
        (
            new Notification(new AppendCard("test-player-2", List(new Card(Suit.spades + "-" + 4, Suit.spades, 4), new Card(Suit.spades + "-" + 5, Suit.spades, 5), new Card(Suit.spades + "-" + 6, Suit.spades, 6)))),
            """{"msg":{"cards":[{"id":"spades-4"},{"id":"spades-5"},{"id":"spades-6"}],"userId":"test-player-2","cla":2,"ins":4},"cla":1,"ins":4}"""
        ),
        (
            new Pass("test-player-3"),
            """{"userId":"test-player-3","cla":2,"ins":2}"""
        ),
        (
            new Notification(new Pass("test-player-3")),
            """{"msg":{"userId":"test-player-3","cla":2,"ins":2},"cla":1,"ins":4}"""
        )
    )

    val decodeMessages = List[Tuple2[Message, String]](
        (
            new JoinRoom("test-player-1", "test-room-1"),
            """{"roomId":"test-room-1","userId":"test-player-1","cla":1,"ins":1}"""
        ), (
            new PutCard("test-player-1", List(new Card(Suit.spades + "-" + 1, Suit.spades, 1), new Card(Suit.spades + "-" + 2, Suit.spades, 2), new Card(Suit.spades + "-" + 3, Suit.spades, 3))),
            """{"cards":[{"id":"spades-1","suit":"spades","point":1},{"id":"spades-2","suit":"spades","point":2},{"id":"spades-3","suit":"spades","point":3}],"userId":"test-player-1","cla":2,"ins":1}"""
        ), (
            new AppendCard("test-player-2", List(new Card(Suit.spades + "-" + 4, Suit.spades, 4), new Card(Suit.spades + "-" + 5, Suit.spades, 5), new Card(Suit.spades + "-" + 6, Suit.spades, 6))),
            """{"cards":[{"id":"spades-4","suit":"spades","point":4},{"id":"spades-5","suit":"spades","point":5},{"id":"spades-6","suit":"spades","point":6}],"userId":"test-player-2","cla":2,"ins":4}"""
        ), (
            new Pass("test-player-3"),
            """{"userId":"test-player-3","cla":2,"ins":2}"""
        )
    )

    @Test
    def testEncode() {
        messages.foreach(pair => {
            val encodedMsg = codec.encode(pair._1);
            println(encodedMsg);
            //assertEquals(pair._2, encodedMsg.replaceAll(""""id":"[a-z0-9/-]*",""", ""));
        });
    }

    @Test
    def testDecode() {
        decodeMessages.foreach(pair => {
            assertEquals(pair._1, codec.decode(pair._2));
        });
    }

}