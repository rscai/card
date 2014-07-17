/**
 *
 */
package me.firecloud.gamecenter.card.model

import org.junit._
import org.junit.Assert._
import akka.testkit.TestActorRef
import akka.actor.ActorSystem
import me.firecloud.gamecenter.model.JoinRoom
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import akka.testkit.TestProbe
import me.firecloud.gamecenter.model.StartGame
import me.firecloud.gamecenter.model.Dealer
import me.firecloud.gamecenter.model.RoomFactory
import me.firecloud.gamecenter.model.RoomDescription
import akka.actor._
import akka.actor.Actor._
import me.firecloud.gamecenter.model.EndGame
import me.firecloud.gamecenter.model.Message
import me.firecloud.gamecenter.model.PlayerActor
import me.firecloud.gamecenter.model.Notification
import me.firecloud.gamecenter.model.Ask
import me.firecloud.gamecenter.model.Ready
import me.firecloud.gamecenter.model.PlayerPropertyChange
import akka.testkit._
import me.firecloud.gamecenter.model.PlayerSupervisor

class MockSupervisorActor extends Actor {
  def receive = {
    case byPassActors: List[Tuple2[String, ActorRef]] =>
      byPassActors.foreach(byPassActor =>
        context.actorOf(Props(new ByPassActor(byPassActor._2)), name = byPassActor._1))
  }
}

class ByPassActor(targetActorRef: ActorRef) extends Actor {
  def receive = {
    case msg => targetActorRef ! msg
  }
}
/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Mar 9, 2014
 *
 */
class CardEngineTest extends TestKit(ActorSystem("unittest")) with ImplicitSender {

  var roomFactory: RoomFactory = null
  var description: RoomDescription = null
  var roomProps: Props = null
  var roomActorRef: TestActorRef[CardRoom] = null

  // construct mock player

  var player1: TestProbe = null

  var player2: TestProbe = null

  var player3: TestProbe = null

  var hand1: List[Card] = null;
  var hand2: List[Card] = null;
  var hand3: List[Card] = null;

  @Before
  def setUp() {
    roomFactory = new CardRoomFactory()
    val tuple = roomFactory.build(new RoomDescription("card", "test-room", 3))
    description = tuple._1
    roomProps = tuple._2
    roomActorRef = TestActorRef(roomProps, name = description.id)

    // construct mock player

    player1 = TestProbe()

    player2 = TestProbe()

    player3 = TestProbe()

    // construct by pass actors
    val playerSupervisor = system.actorOf(Props[MockSupervisorActor], name = "player")

    val byPassActors = List(("1", player1.ref), ("2", player2.ref), ("3", player3.ref))

    playerSupervisor ! byPassActors

  }

  @After
  def tearDown() {
    roomActorRef.stop
  }

  @Test
  def testPutCardRule() {

    val emptyCards = List()
    val singleAce = List(new Card("spades-1", Suit.spades, 1))
    val singleTwo = List(new Card("spades-2", Suit.spades, 2))
    val singleThree = List(new Card("spades-3", Suit.spades, 3))
    val singleKing = List(new Card("spades-13", Suit.spades, 13))

    val pairAce = List(new Card("spades-1", Suit.spades, 1), new Card("clubs-1", Suit.clubs, 1))
    val pairTwo = List(new Card("spades-2", Suit.spades, 2), new Card("clubs-2", Suit.clubs, 2))
    val pairThree = List(new Card("spades-3", Suit.spades, 3), new Card("clubs-3", Suit.clubs, 3))
    val pairKing = List(new Card("spades-13", Suit.spades, 13), new Card("clubs-13", Suit.clubs, 13))

    val tripleAce = List(new Card("spades-1", Suit.spades, 1), new Card("clubs-1", Suit.clubs, 1), new Card("diamonds-1", Suit.diamonds, 1))
    val tripleTwo = List(new Card("spades-2", Suit.spades, 2), new Card("clubs-2", Suit.clubs, 2), new Card("diamonds-2", Suit.diamonds, 2))
    val tripleThree = List(new Card("spades-3", Suit.spades, 3), new Card("clubs-3", Suit.clubs, 3), new Card("diamonds-3", Suit.diamonds, 3))
    val tripleKing = List(new Card("spades-13", Suit.spades, 13), new Card("clubs-13", Suit.clubs, 13), new Card("diamonds-13", Suit.diamonds, 13))

    val boombAce = List(new Card("spades-1", Suit.spades, 1), new Card("clubs-1", Suit.clubs, 1), new Card("diamonds-1", Suit.diamonds, 1), new Card("hearts-1", Suit.hearts, 1))
    val boombTwo = List(new Card("spades-2", Suit.spades, 2), new Card("clubs-2", Suit.clubs, 2), new Card("diamonds-2", Suit.diamonds, 2), new Card("hearts-2", Suit.hearts, 2))
    val boombThree = List(new Card("spades-3", Suit.spades, 3), new Card("clubs-3", Suit.clubs, 3), new Card("diamonds-3", Suit.diamonds, 3), new Card("hearts-3", Suit.hearts, 3))
    val boombKing = List(new Card("spades-13", Suit.spades, 13), new Card("clubs-13", Suit.clubs, 13), new Card("diamonds-13", Suit.diamonds, 13), new Card("hearts-13", Suit.hearts, 13))

    val sequenceThreeToSeven = List(new Card("spades-3", Suit.spades, 3), new Card("hearts-4", Suit.hearts, 4), new Card("spades-5", Suit.spades, 5), new Card("spades-6", Suit.spades, 6), new Card("spades-7", Suit.spades, 7))
    val sequenceFiveToNine = List(new Card("spades-5", Suit.spades, 5), new Card("spades-6", Suit.spades, 6), new Card("spades-7", Suit.spades, 7), new Card("spades-8", Suit.spades, 8), new Card("spades-9", Suit.spades, 9))
    val sequenceTenToAce = List(new Card("spades-10", Suit.spades, 10), new Card("spades-11", Suit.spades, 11), new Card("spades-12", Suit.spades, 12), new Card("spades-13", Suit.spades, 13), new Card("spades-1", Suit.spades, 1))

    val sequenceThreeToEight = List(new Card("spades-3", Suit.spades, 3), new Card("hearts-4", Suit.hearts, 4), new Card("spades-5", Suit.spades, 5), new Card("spades-6", Suit.spades, 6), new Card("spades-7", Suit.spades, 7), new Card("spades-8", Suit.spades, 8))
    val sequenceFiveToTen = List(new Card("spades-5", Suit.spades, 5), new Card("spades-6", Suit.spades, 6), new Card("spades-7", Suit.spades, 7), new Card("spades-8", Suit.spades, 8), new Card("spades-9", Suit.spades, 9), new Card("spades-10", Suit.spades, 10))
    val sequenceNineToAce = List(new Card("spades-9", Suit.spades, 9), new Card("spades-10", Suit.spades, 10), new Card("spades-11", Suit.spades, 11), new Card("spades-12", Suit.spades, 12), new Card("spades-13", Suit.spades, 13), new Card("spades-1", Suit.spades, 1))

    val tripleFiveWithPairSeven = List(new Card("spades-5", Suit.spades, 5), new Card("clubs-5", Suit.clubs, 5), new Card("diamonds-5", Suit.diamonds, 5), new Card("spades-7", Suit.spades, 7), new Card("hearts-7", Suit.hearts, 7))
    val tripleEightWithPairSix = List(new Card("spades-8", Suit.spades, 8), new Card("clubs-8", Suit.clubs, 8), new Card("hearts-8", Suit.hearts, 8), new Card("spades-6", Suit.spades, 6), new Card("diamonds-6", Suit.diamonds, 6))
    val tripleFourWithPairKing = List(new Card("spades-4", Suit.spades, 4), new Card("clubs-4", Suit.clubs, 4), new Card("hearts-4", Suit.hearts, 4), new Card("spades-13", Suit.spades, 13), new Card("diamonds-13", Suit.diamonds, 13))
    val tripleNineWithPairJack = List(new Card("spades-9", Suit.spades, 9), new Card("clubs-9", Suit.clubs, 9), new Card("hearts-9", Suit.hearts, 9), new Card("spades-11", Suit.spades, 11), new Card("diamonds-11", Suit.diamonds, 11))

    val pairSequenceFiveToSeven =List(new Card("clubs-5",Suit.clubs,5), Card("diamonds-5",Suit.diamonds,5), Card("clubs-6",Suit.clubs,6), Card("spades-7",Suit.spades,7), Card("spades-6",Suit.spades,6), Card("clubs-7",Suit.clubs,7))
    
     val pairSequenceSevenToNine =List(new Card("clubs-8",Suit.clubs,8), Card("diamonds-8",Suit.diamonds,8), Card("clubs-9",Suit.clubs,9), Card("spades-7",Suit.spades,7), Card("spades-9",Suit.spades,9), Card("clubs-7",Suit.clubs,7))

        
    assertTrue("start put single ace", roomActorRef.underlyingActor.matchPutCardRule(emptyCards, singleAce));

    assertTrue("single king is greater than single three", roomActorRef.underlyingActor.matchPutCardRule(singleThree, singleKing));
    assertTrue("single ace is greater than single king", roomActorRef.underlyingActor.matchPutCardRule(singleKing, singleAce));
    assertFalse("single three is not greater than single two", roomActorRef.underlyingActor.matchPutCardRule(singleTwo, singleThree))

    assertTrue("pair ace is greater than paire king", roomActorRef.underlyingActor.matchPutCardRule(pairKing, pairAce))
    assertFalse("pair ace is not greater than paire two", roomActorRef.underlyingActor.matchPutCardRule(pairTwo, pairAce))
    assertTrue("pair two is greater than paire three", roomActorRef.underlyingActor.matchPutCardRule(pairThree, pairTwo))
    assertFalse("pair ace is not greater than single king", roomActorRef.underlyingActor.matchPutCardRule(singleKing, pairAce))

    assertTrue("sequenceFiveToNine is greater than sequenceThreeToSeven", roomActorRef.underlyingActor.matchPutCardRule(sequenceThreeToSeven, sequenceFiveToNine))
    assertFalse("sequenceFiveToTen not greater than sequenceThreeToSeven", roomActorRef.underlyingActor.matchPutCardRule(sequenceThreeToSeven, sequenceFiveToTen))

    assertTrue("sequenceFiveToTen is greater than sequenceThreeToEight", roomActorRef.underlyingActor.matchPutCardRule(sequenceThreeToEight, sequenceFiveToTen))
    assertTrue("sequenceNineToAce is greater than sequenceFiveToTen", roomActorRef.underlyingActor.matchPutCardRule(sequenceFiveToTen, sequenceNineToAce))

    assertFalse("triple ace is not greater than triple two", roomActorRef.underlyingActor.matchPutCardRule(tripleTwo, tripleAce))

    assertTrue("tripleNineWithPairJack is greater than tripleFiveWithPairSeven", roomActorRef.underlyingActor.matchPutCardRule(tripleFiveWithPairSeven, tripleNineWithPairJack))
    assertFalse("tripleEightWithPairSix is not greater than tripleFiveWithPairSeven", roomActorRef.underlyingActor.matchPutCardRule(tripleFiveWithPairSeven, tripleEightWithPairSix))
    assertFalse("tripleFourWithPairKing is not greater than tripleFiveWithPairSeven", roomActorRef.underlyingActor.matchPutCardRule(tripleFiveWithPairSeven, tripleFourWithPairKing))

    assertTrue("boomb three is greater than single ace", roomActorRef.underlyingActor.matchPutCardRule(singleAce, boombThree))
    assertTrue("boomb three is greater than pair ace", roomActorRef.underlyingActor.matchPutCardRule(pairAce, boombThree))
    assertTrue("boomb three is greater than triple ace", roomActorRef.underlyingActor.matchPutCardRule(tripleAce, boombThree))

    assertTrue("boomb king is greater than boomb three", roomActorRef.underlyingActor.matchPutCardRule(boombThree, boombKing))
    assertTrue("boomb Ace is greater than boomb king", roomActorRef.underlyingActor.matchPutCardRule(boombKing, boombAce))

    assertTrue("boomb three is greater than any tripleWithPair", roomActorRef.underlyingActor.matchPutCardRule(tripleNineWithPairJack, boombThree))
    
    assertTrue("boomb three is greater than sequence", roomActorRef.underlyingActor.matchPutCardRule(sequenceNineToAce, boombThree))
    
    assertTrue("start put pair seqeunce",roomActorRef.underlyingActor.matchPutCardRule(emptyCards, pairSequenceFiveToSeven))
    assertTrue("pair sequence 7 to 9 is greater than pair sequence f to 7",roomActorRef.underlyingActor.matchPutCardRule(pairSequenceFiveToSeven, pairSequenceSevenToNine))
  }

  @Test
  def testJoinRoom() {

    // construct join room message
    val joinRoom1 = new JoinRoom("1", description.id)

    val joinRoom2 = new JoinRoom("2", description.id)

    val joinRoom3 = new JoinRoom("3", description.id)

    player1.send(roomActorRef, joinRoom1)

    var resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player1 joint", joinRoom1.userId, resp1.userId)

    player2.send(roomActorRef, joinRoom2)
    resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    var resp2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]

    assertEquals("player2 joint", joinRoom2.userId, resp1.userId)
    assertEquals("player2 joint", "1", resp2.userId)

    resp2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player2 joint", joinRoom2.userId, resp2.userId)

    player3.send(roomActorRef, joinRoom3)
    resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    resp2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    var resp3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]

    assertEquals("player3 joint", joinRoom3.userId, resp1.userId)
    assertEquals("player3 joint", joinRoom3.userId, resp2.userId)
    assertEquals("player3 joint", "1", resp3.userId)

    resp3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player3 joint", "2", resp3.userId)

    resp3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player3 joint", joinRoom3.userId, resp3.userId)

    val startGame = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[StartGame]

  }

  @Test
  def testRejoinRoom() {
    // construct join room message
    val joinRoom1 = new JoinRoom("1", description.id)

    val joinRoom2 = new JoinRoom("2", description.id)

    val joinRoom3 = new JoinRoom("3", description.id)

    player1.send(roomActorRef, joinRoom1)

    var resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player1 joint", joinRoom1.userId, resp1.userId)
    assertEquals("player1 joint", 0, resp1.position)

    player2.send(roomActorRef, joinRoom2)
    resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    var resp2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]

    assertEquals("player2 joint", joinRoom2.userId, resp1.userId)
    assertEquals("player2 joint", 1, resp1.position)
    assertEquals("player1 has aready joint", "1", resp2.userId)
    assertEquals("player1 has aready joint", 0, resp2.position)

    resp2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player2 joint", joinRoom2.userId, resp2.userId)
    assertEquals("player2 joint", 1, resp1.position)

    // player1 rejoin 

    player1.send(roomActorRef, joinRoom1)
    // receive notications of aready joint players (include itself)
    resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player1 joint", "1", resp1.userId)
    assertEquals("player1 joint", 0, resp1.position)

    resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player2 joint", "2", resp1.userId)
    assertEquals("player2 joint", 1, resp1.position)

    resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player1 joint", "1", resp1.userId)
    assertEquals("player1 joint", 0, resp1.position)

    resp2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player1 rejoint", "1", resp2.userId)
    assertEquals("player1 rejoint", 0, resp2.position)

    player3.send(roomActorRef, joinRoom3)
    resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    resp2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    var resp3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]

    assertEquals("player3 joint", joinRoom3.userId, resp1.userId)
    assertEquals("player3 joint", joinRoom3.userId, resp2.userId)
    assertEquals("player3 joint", "1", resp3.userId)

    resp3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player3 joint", "2", resp3.userId)

    resp3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player3 joint", joinRoom3.userId, resp3.userId)

    val startGame = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[StartGame]
  }

  @Test
  def testStartGame() {

    // construct join room message
    val joinRoom1 = new JoinRoom("1", description.id)

    val joinRoom2 = new JoinRoom("2", description.id)

    val joinRoom3 = new JoinRoom("3", description.id)

    player1.send(roomActorRef, joinRoom1)

    var resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player1 joint", joinRoom1.userId, resp1.userId)

    player2.send(roomActorRef, joinRoom2)
    resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    var resp2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]

    assertEquals("player2 joint", joinRoom2.userId, resp1.userId)
    assertEquals("player2 joint", "1", resp2.userId)

    resp2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player2 joint", joinRoom2.userId, resp2.userId)

    player3.send(roomActorRef, joinRoom3)
    resp1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    resp2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    var resp3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]

    assertEquals("player3 joint", joinRoom3.userId, resp1.userId)
    assertEquals("player3 joint", joinRoom3.userId, resp2.userId)
    assertEquals("player3 joint", "1", resp3.userId)

    resp3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player3 joint", "2", resp3.userId)

    resp3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[JoinRoom]
    assertEquals("player3 joint", joinRoom3.userId, resp3.userId)

    //val startGame = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[StartGame]

    // start game

    //val startGameMsg = new StartGame(Dealer.id)

    //roomActorRef ! startGameMsg

    // player1 received start game message
    player1.expectMsgClass(classOf[Notification]).msg.isInstanceOf[StartGame]
    // player2 received start game message
    player2.expectMsgClass(classOf[Notification]).msg.isInstanceOf[StartGame]
    // player3 received start game message
    player3.expectMsgClass(classOf[Notification]).msg.isInstanceOf[StartGame]

    // player1 send ready message
    val ready1 = new Ready("1");
    player1.send(roomActorRef, ready1);
    // player2 send ready message
    val ready2 = new Ready("2");
    player2.send(roomActorRef, ready2);
    //player3 send ready message
    val ready3 = new Ready("3");
    player3.send(roomActorRef, ready3);

    // player1 received three deal card messages
    val msgs1 = player1.receiveN(3)
    assertEquals(3, msgs1.size)
    msgs1.foreach(msg => {
      assertTrue(msg.asInstanceOf[Notification].msg.isInstanceOf[DealCard])
      assertEquals(17, msg.asInstanceOf[Notification].msg.asInstanceOf[DealCard].cards.size)
      if (msg.asInstanceOf[Notification].msg.asInstanceOf[DealCard].toUserId == "1") {
        hand1 = msg.asInstanceOf[Notification].msg.asInstanceOf[DealCard].cards
      }
    })

    // player2 received three deal card messages
    val msgs2 = player2.receiveN(3)
    assertEquals(3, msgs2.size)
    msgs2.foreach(msg => {
      assertTrue(msg.asInstanceOf[Notification].msg.isInstanceOf[DealCard])
      assertEquals(17, msg.asInstanceOf[Notification].msg.asInstanceOf[DealCard].cards.size)
      if (msg.asInstanceOf[Notification].msg.asInstanceOf[DealCard].toUserId == "2") {
        hand2 = msg.asInstanceOf[Notification].msg.asInstanceOf[DealCard].cards
      }
    })

    // player3 received three deal card messages
    val msgs3 = player3.receiveN(3)
    assertEquals(3, msgs3.size)
    msgs3.foreach(msg => {
      assertTrue(msg.asInstanceOf[Notification].msg.isInstanceOf[DealCard])
      assertEquals(17, msg.asInstanceOf[Notification].msg.asInstanceOf[DealCard].cards.size)
      if (msg.asInstanceOf[Notification].msg.asInstanceOf[DealCard].toUserId == "3") {
        hand3 = msg.asInstanceOf[Notification].msg.asInstanceOf[DealCard].cards
      }
    })

    val askActions1 = player1.expectMsgClass(classOf[Ask]).actions

    assertEquals(List(Bet.key), askActions1)

  }

  @Test
  def testPutCardAppendCardPass() {
    //
    testStartGame()

    // check hands
    assertEquals(17, hand1.size)
    assertEquals(17, hand2.size)
    assertEquals(17, hand3.size)

    // all players will receive message of asking player1 to bet

    var askBet2 = player2.expectMsgClass(classOf[Ask])

    assertEquals("1", askBet2.targetUserId)
    assertEquals(List(Bet.key), askBet2.actions)

    var askBet3 = player3.expectMsgClass(classOf[Ask])

    assertEquals("1", askBet3.targetUserId)
    assertEquals(List(Bet.key), askBet3.actions)

    // bet start by player1

    // player1 bet 
    var bet1 = new Bet("1", 32)
    player1.send(roomActorRef, bet1)

    // player1 received notification
    var betNotify1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[Bet]

    assertEquals("1", betNotify1.userId)
    assertEquals(32, betNotify1.amount)

    // player2 received notification
    var betNotify2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[Bet]

    assertEquals("1", betNotify2.userId)
    assertEquals(32, betNotify2.amount)

    // player3 received notification
    var betNotify3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[Bet]

    assertEquals("1", betNotify3.userId)
    assertEquals(32, betNotify3.amount)

    // all players will receive message of asking player2 to bet

    var askBet1 = player1.expectMsgClass(classOf[Ask])

    assertEquals("2", askBet1.targetUserId)
    assertEquals(List(Bet.key), askBet1.actions)

    askBet2 = player2.expectMsgClass(classOf[Ask])

    assertEquals("2", askBet2.targetUserId)
    assertEquals(List(Bet.key), askBet2.actions)

    askBet3 = player3.expectMsgClass(classOf[Ask])

    assertEquals("2", askBet3.targetUserId)
    assertEquals(List(Bet.key), askBet3.actions)

    // player2 bet 
    var bet2 = new Bet("2", 22)
    player2.send(roomActorRef, bet2)

    // player1 received notification
    betNotify1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[Bet]

    assertEquals("2", betNotify1.userId)
    assertEquals(22, betNotify1.amount)

    // player2 received notification
    betNotify2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[Bet]

    assertEquals("2", betNotify2.userId)
    assertEquals(22, betNotify2.amount)

    // player3 received notification
    betNotify3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[Bet]

    assertEquals("2", betNotify3.userId)
    assertEquals(22, betNotify3.amount)

    // all players will receive message of asking player3 to bet

    askBet1 = player1.expectMsgClass(classOf[Ask])

    assertEquals("3", askBet1.targetUserId)
    assertEquals(List(Bet.key), askBet1.actions)

    askBet2 = player2.expectMsgClass(classOf[Ask])

    assertEquals("3", askBet2.targetUserId)
    assertEquals(List(Bet.key), askBet2.actions)

    askBet3 = player3.expectMsgClass(classOf[Ask])

    assertEquals("3", askBet3.targetUserId)
    assertEquals(List(Bet.key), askBet3.actions)

    // player3 bet 
    var bet3 = new Bet("3", 12)
    player3.send(roomActorRef, bet3)

    // player1 received notification
    betNotify1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[Bet]

    assertEquals("3", betNotify1.userId)
    assertEquals(12, betNotify1.amount)

    // player2 received notification
    betNotify2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[Bet]

    assertEquals("3", betNotify2.userId)
    assertEquals(12, betNotify2.amount)

    // player3 received notification
    betNotify3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[Bet]

    assertEquals("3", betNotify3.userId)
    assertEquals(12, betNotify3.amount)

    // all players received message which player1 has been the landlord

    var propsChange1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[PlayerPropertyChange]
    assertEquals("1", propsChange1.userId)
    assertEquals(List(("role", "LANDLORD")), propsChange1.changes)

    var propsChange2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[PlayerPropertyChange]
    assertEquals("1", propsChange2.userId)
    assertEquals(List(("role", "LANDLORD")), propsChange2.changes)

    var propsChange3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[PlayerPropertyChange]
    assertEquals("1", propsChange3.userId)
    assertEquals(List(("role", "LANDLORD")), propsChange3.changes)

    // all players received message which dealer deal last 3 cards to the landlord
    var lastDealMsg = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[DealCard]
    assertEquals("1", lastDealMsg.toUserId)

    hand1 = hand1 ++ lastDealMsg.cards

    assertEquals("1", player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[DealCard].toUserId)
    assertEquals("1", player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[DealCard].toUserId)

    // all players receive message of asking  player1 to put card 
    var msg1: Message = player1.expectMsgClass(classOf[Ask])

    assertEquals("1", msg1.asInstanceOf[Ask].targetUserId)
    assertEquals(List(PutCard.key), msg1.asInstanceOf[Ask].actions)

    var msg2: Message = player2.expectMsgClass(classOf[Ask])

    assertEquals("1", msg2.asInstanceOf[Ask].targetUserId)
    assertEquals(List(PutCard.key), msg2.asInstanceOf[Ask].actions)

    var msg3: Message = player3.expectMsgClass(classOf[Ask])

    assertEquals("1", msg3.asInstanceOf[Ask].targetUserId)
    assertEquals(List(PutCard.key), msg3.asInstanceOf[Ask].actions)

    // put card start by player1

    
    // player1 put 5 cards
    var card1 = hand1.take(5)
    hand1 = hand1.drop(5)
    var putCard1 = new PutCard("1", card1)

    player1.send(roomActorRef, putCard1)
    msg1 = player1.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg1.asInstanceOf[PutCard].cards)

    msg2 = player2.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg2.asInstanceOf[PutCard].cards)

    msg3 = player3.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg3.asInstanceOf[PutCard].cards)

    // all players receive message of asking  player2 to append card or pass 
    msg1 = player1.expectMsgClass(classOf[Ask])

    assertEquals("2", msg1.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg1.asInstanceOf[Ask].actions)

    msg2 = player2.expectMsgClass(classOf[Ask])

    assertEquals("2", msg2.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg2.asInstanceOf[Ask].actions)

    msg3 = player3.expectMsgClass(classOf[Ask])

    assertEquals("2", msg3.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg3.asInstanceOf[Ask].actions)

    // other players put card on turn

    // player2 put 4 cards
    var card2 = hand2.take(4)
    hand2 = hand2.drop(4)
    var putCard2 = new AppendCard("2", card2)

    player2.send(roomActorRef, putCard2)

    msg1 = player1.expectMsgClass(classOf[Notification]).msg
    assertEquals(card2, msg1.asInstanceOf[AppendCard].cards)

    msg2 = player2.expectMsgClass(classOf[Notification]).msg
    assertEquals(card2, msg2.asInstanceOf[AppendCard].cards)

    msg3 = player3.expectMsgClass(classOf[Notification]).msg
    assertEquals(card2, msg3.asInstanceOf[AppendCard].cards)

    // all players receive message of asking  player3 to append card or pass 
    msg1 = player1.expectMsgClass(classOf[Ask])

    assertEquals("3", msg1.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg1.asInstanceOf[Ask].actions)

    msg2 = player2.expectMsgClass(classOf[Ask])

    assertEquals("3", msg2.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg2.asInstanceOf[Ask].actions)

    msg3 = player3.expectMsgClass(classOf[Ask])

    assertEquals("3", msg3.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg3.asInstanceOf[Ask].actions)

    // player3 put 3 cards

    var card3 = hand3.take(3)
    hand3 = hand2.drop(3)
    var putCard3 = new AppendCard("3", card3)
    player3.send(roomActorRef, putCard3)

    msg1 = player1.expectMsgClass(classOf[Notification]).msg
    assertEquals(card3, msg1.asInstanceOf[AppendCard].cards)

    msg2 = player2.expectMsgClass(classOf[Notification]).msg
    assertEquals(card3, msg2.asInstanceOf[AppendCard].cards)

    msg3 = player3.expectMsgClass(classOf[Notification]).msg
    assertEquals(card3, msg3.asInstanceOf[AppendCard].cards)

    // all players receive message of asking  player1 to append card or pass 
    msg1 = player1.expectMsgClass(classOf[Ask])

    assertEquals("1", msg1.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg1.asInstanceOf[Ask].actions)

    msg2 = player2.expectMsgClass(classOf[Ask])

    assertEquals("1", msg2.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg2.asInstanceOf[Ask].actions)

    msg3 = player3.expectMsgClass(classOf[Ask])

    assertEquals("1", msg3.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg3.asInstanceOf[Ask].actions)

    // player1 put 5 cards
    card1 = hand1.take(5)
    hand1 = hand1.drop(5)
    var appendCard1 = new AppendCard("1", card1)

    player1.send(roomActorRef, appendCard1)
    msg1 = player1.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg1.asInstanceOf[AppendCard].cards)

    msg2 = player2.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg2.asInstanceOf[AppendCard].cards)

    msg3 = player3.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg3.asInstanceOf[AppendCard].cards)

    // all players receive message of asking  player2 to append card or pass 
    msg1 = player1.expectMsgClass(classOf[Ask])

    assertEquals("2", msg1.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg1.asInstanceOf[Ask].actions)

    msg2 = player2.expectMsgClass(classOf[Ask])

    assertEquals("2", msg2.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg2.asInstanceOf[Ask].actions)

    msg3 = player3.expectMsgClass(classOf[Ask])

    assertEquals("2", msg3.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg3.asInstanceOf[Ask].actions)

    // player2 pass
    card2 = hand2.take(6)
    hand2 = hand2.drop(6)
    var pass2 = new Pass("2")

    player2.send(roomActorRef, pass2)

    msg1 = player1.expectMsgClass(classOf[Notification]).msg
    assertEquals("2", msg1.asInstanceOf[Pass].userId)

    msg2 = player2.expectMsgClass(classOf[Notification]).msg
    assertEquals("2", msg2.asInstanceOf[Pass].userId)

    msg3 = player3.expectMsgClass(classOf[Notification]).msg
    assertEquals("2", msg3.asInstanceOf[Pass].userId)

    // all players receive message of asking  player3 to append card or pass 
    msg1 = player1.expectMsgClass(classOf[Ask])

    assertEquals("3", msg1.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg1.asInstanceOf[Ask].actions)

    msg2 = player2.expectMsgClass(classOf[Ask])

    assertEquals("3", msg2.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg2.asInstanceOf[Ask].actions)

    msg3 = player3.expectMsgClass(classOf[Ask])

    assertEquals("3", msg3.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg3.asInstanceOf[Ask].actions)

    // player3 put 7 cards
    card3 = hand3.take(7)
    hand3 = hand2.drop(7)
    putCard3 = new AppendCard("3", card3)
    player3.send(roomActorRef, putCard3)

    msg1 = player1.expectMsgClass(classOf[Notification]).msg
    assertEquals(card3, msg1.asInstanceOf[AppendCard].cards)

    msg2 = player2.expectMsgClass(classOf[Notification]).msg
    assertEquals(card3, msg2.asInstanceOf[AppendCard].cards)

    msg3 = player3.expectMsgClass(classOf[Notification]).msg
    assertEquals(card3, msg3.asInstanceOf[AppendCard].cards)

    // all players receive message of asking  player1 to append card or pass 
    msg1 = player1.expectMsgClass(classOf[Ask])

    assertEquals("1", msg1.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg1.asInstanceOf[Ask].actions)

    msg2 = player2.expectMsgClass(classOf[Ask])

    assertEquals("1", msg2.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg2.asInstanceOf[Ask].actions)

    msg3 = player3.expectMsgClass(classOf[Ask])

    assertEquals("1", msg3.asInstanceOf[Ask].targetUserId)
    assertEquals(List(AppendCard.key, Pass.key), msg3.asInstanceOf[Ask].actions)

    // player1 put last 1 card

    card1 = hand1.take(10)
    hand1 = hand1.drop(10)
    appendCard1 = new AppendCard("1", card1)

    player1.send(roomActorRef, appendCard1)
    msg1 = player1.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg1.asInstanceOf[AppendCard].cards)

    msg2 = player2.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg2.asInstanceOf[AppendCard].cards)

    msg3 = player3.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg3.asInstanceOf[AppendCard].cards)

    // it should be finish

    msg1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[EndGame]

    msg2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[EndGame]

    msg3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[EndGame]

  }

  def testPass() {
    //
    testStartGame()

    // check hands
    assertEquals(17, hand1.size)
    assertEquals(17, hand2.size)
    assertEquals(17, hand3.size)

    // put card start by player1

    // player1 put 5 cards
    var card1 = hand1.take(5)
    hand1 = hand1.drop(5)
    var putCard1 = new PutCard("1", card1)

    player1.send(roomActorRef, putCard1)
    var msg1: Message = player1.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg1.asInstanceOf[PutCard].cards)

    var msg2: Message = player2.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg2.asInstanceOf[PutCard].cards)

    var msg3: Message = player3.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg3.asInstanceOf[PutCard].cards)

    // ask player 2 to append or pass

    msg2 = player2.expectMsgClass(classOf[Ask])
    assertEquals(List(AppendCard.key, Pass.key), msg2.asInstanceOf[Ask].actions)

    // other players put card on turn

    // player2 pass
    var pass2 = new Pass("2")
    player2.send(roomActorRef, pass2)

    msg1 = player1.expectMsgClass(classOf[Notification]).msg
    assertEquals("2", msg1.asInstanceOf[Pass].userId)

    msg2 = player2.expectMsgClass(classOf[Notification]).msg
    assertEquals("2", msg2.asInstanceOf[Pass].userId)

    msg3 = player3.expectMsgClass(classOf[Notification]).msg
    assertEquals("2", msg3.asInstanceOf[Pass].userId)

    // ask player 3 to append or pass
    msg3 = player3.expectMsgClass(classOf[Ask])
    assertEquals(List(AppendCard.key, Pass.key), msg3.asInstanceOf[Ask].actions)

    // player3 pass too

    var pass3 = new Pass("3")
    player3.send(roomActorRef, pass3)
    msg1 = player1.expectMsgClass(classOf[Notification]).msg
    assertEquals("3", msg1.asInstanceOf[Pass].userId)

    msg2 = player2.expectMsgClass(classOf[Notification]).msg
    assertEquals("3", msg2.asInstanceOf[Pass].userId)

    msg3 = player3.expectMsgClass(classOf[Notification]).msg
    assertEquals("3", msg3.asInstanceOf[Pass].userId)

    // ask player1 start put card
    msg1 = player1.expectMsgClass(classOf[Ask])
    assertEquals(List(PutCard.key), msg1.asInstanceOf[Ask].actions)

    // player1 put all cards
    card1 = hand1.take(12)
    hand1 = hand1.drop(12)
    putCard1 = new PutCard("1", card1)

    player1.send(roomActorRef, putCard1)
    msg1 = player1.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg1.asInstanceOf[PutCard].cards)

    msg2 = player2.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg2.asInstanceOf[PutCard].cards)

    msg3 = player3.expectMsgClass(classOf[Notification]).msg
    assertEquals(card1, msg3.asInstanceOf[PutCard].cards)

    // it should be finish

    msg1 = player1.expectMsgClass(classOf[Notification]).msg.asInstanceOf[EndGame]

    msg2 = player2.expectMsgClass(classOf[Notification]).msg.asInstanceOf[EndGame]

    msg3 = player3.expectMsgClass(classOf[Notification]).msg.asInstanceOf[EndGame]
  }
}