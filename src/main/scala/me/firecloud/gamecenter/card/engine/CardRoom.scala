/**
 *
 */
package me.firecloud.gamecenter.card.engine

import java.util.UUID
import akka.actor.FSM
import scala.concurrent.duration._
import me.firecloud.gamecenter.model.StartGame
import me.firecloud.gamecenter.model.Dealer
import me.firecloud.gamecenter.model.JoinRoom
import akka.actor.Props
import me.firecloud.gamecenter.model.Message
import me.firecloud.gamecenter.model.EndGame
import me.firecloud.utils.logging.Logging
import me.firecloud.gamecenter.model.Notification
import me.firecloud.gamecenter.model.Ask
import me.firecloud.gamecenter.model.Ready
import me.firecloud.gamecenter.model.PlayerPropertyChange
import me.firecloud.gamecenter.dao.Update
import me.firecloud.gamecenter.dao.Delete
import me.firecloud.gamecenter.model.SeatCycle
import akka.actor.ActorSelection.toScala
import akka.actor.actorRef2Scala
import me.firecloud.gamecenter.card.model.AppendCard
import me.firecloud.gamecenter.card.model.Bet
import me.firecloud.gamecenter.card.model.Card
import me.firecloud.gamecenter.card.model.DealCard
import me.firecloud.gamecenter.card.model.Pass
import me.firecloud.gamecenter.card.model.PokerPack
import me.firecloud.gamecenter.card.model.PutCard
import scala.Option.option2Iterable
import me.firecloud.gamecenter.engine.Engine
import me.firecloud.gamecenter.model.Seat
import me.firecloud.gamecenter.card.model.CardSeat
import me.firecloud.gamecenter.model.Room

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Mar 8, 2014
 *
 */




class Unmatched extends Exception("Unmatched")

// states
sealed trait State
case object Idle extends State
case object Startting extends State
case object BetState extends State
case object StartPutCard extends State
case object AppendPutCard extends State

sealed trait Data
case object Uninitialized extends Data

class CardRoom[SeatType <: CardSeat](id: String, kind: String,parameters: Map[String,Any]) extends Engine[SeatType](id, kind,parameters) with FSM[State, Data] with Logging {
 
  

  var lastAppendSeat: SeatType=_

  /**
   * *************************
   * functional members
   */

  var reservedCards: List[Card] = List()

  var lastPerformSeatIndex: Int = -1
  /**
   * **************************
   * action performers.
   */


  protected def bet(seat: SeatType, amount: Int) = {
    seat.bet = amount

    // notify all
    val betMsg = new Bet(seat.playerId, amount);

    notifyAll(betMsg)
  }

  protected def init: Unit = {
    // init cycles
    defaultCycle = new SeatCycle(seats)

    reservedCards = PokerPack.cards

  }

  protected def dealCard(seats: List[SeatType], cardsGetter: () => List[Card]): Unit = {
    //val cardBatchSize = 17
    val messages = seats.map(seat => {
      // construct deal card message
      val cards = cardsGetter()
      val dealCard = new DealCard(Dealer.id, seat.playerId, cards)
      //reservedCards = reservedCards.drop(cardBatchSize)
      seat.hand = seat.hand ++ cards.toSet[Card]

      debug("seat " + seat.playerId + " hand:" + seat.hand.size)
      dealCard
    })

    messages.foreach(msg => {
      notifyAll(msg)
    })

  }

  protected def join(playerId: String): Unit = {

    val jointSeats = seats.zipWithIndex.filter(_._1.state == "OCCUPIED")
    // check the player if already joint
    val isJoint = seats.zipWithIndex.find(x => x._1.playerId != null && x._1.playerId.equals(playerId))
    isJoint.map {
      seat =>

        // notify new player the players whom had joint
        jointSeats.foreach(x => {
          val msg = new JoinRoom(x._1.playerId, id, x._2)
          notify(seat._1, msg)
        })
        // construct join room message
        val joinRoom = new JoinRoom(playerId, id, seat._2)

        notifyAll(joinRoom)
    }.getOrElse {

      val (seat, index) = seats.zipWithIndex.find(_._1.state == "FREE").get

      seat.playerId = playerId
      seat.occupy

      // report status to supervisor
      reportStatus

      // notify new player the players whom had joint
      jointSeats.foreach(x => {
        val msg = new JoinRoom(x._1.playerId, id, x._2)
        notify(seat, msg)
      })

      // construct join room message
      val joinRoom = new JoinRoom(playerId, id, index)

      notifyAll(joinRoom)
    }
  }

  protected def end: Unit = {
    val endMsg = new EndGame(Dealer.id)
    notifyAll(endMsg)

    // close room actor
    // report status to supervisor
    context.parent ! new Delete(this.id)

    // 
    context.stop(self)
  }

  protected def reportStatus: Unit = {
    val roomDescription = new Room(this.kind, this.seats.size, this.parameters)
    roomDescription.id = this.id
    roomDescription.seats = this.seats.map((seat: Seat) => (seat.playerId, null, null))

    context.parent ! new Update(roomDescription)
  }

  /**
   * notify all players state changes of game include state changes of involved players
   */
  protected def notifyAll(msg: Message): Unit = {
    seats.foreach(seat => {
      if (seat.state != "FREE") {
        context.actorSelection("/user/player/" + seat.playerId) ! new Notification(msg)
      }
    })
  }

  protected def notify(seat: Seat, msg: Message): Unit = {
    context.actorSelection("/user/player/" + seat.playerId) ! new Notification(msg)
  }

  /**
   * send ask message to specific player for asking he to do acceptable actions
   */
  protected def ask(seat: Seat, actions: List[Tuple2[Long, Long]]): Unit = {

    // ask message should send to all players
    val askMsg = new Ask(seat.playerId, actions, timeout)
    seats.foreach(seat =>
      context.actorSelection("/user/player/" + seat.playerId) ! askMsg)
  }

  /**
   * check if there is any one player run out his hand card
   */
  protected def anyoneWin: Boolean = {
    seats.exists(seat => {
      seat.hand.size == 0
    })
  }

  protected var putCardRules: List[Function2[List[Card], List[Card], Boolean]] = List(
    (previousCards, putCards) => empty(previousCards) && single(putCards) != null,
    (previousCards, putCards) => empty(previousCards) && pair(putCards) != null,
    (previousCards, putCards) => empty(previousCards) && triple(putCards) != null,
    (previousCards, putCards) => empty(previousCards) && boomb(putCards) != null,
    (previousCards, putCards) => empty(previousCards) && sequence(putCards) != null,
    (previousCards, putCards) => empty(previousCards) && pairSequence(putCards) != null,
    (previousCards, putCards) => empty(previousCards) && tripleSequence(putCards) != null,
    (previousCards, putCards) => empty(previousCards) && tripleWithPair(putCards) != null,

    (previousCards, putCards) => (single(previousCards).point + 10) % 13 < (single(putCards).point + 10) % 13,
    (previousCards, putCards) => (pair(previousCards).point + 10) % 13 < (pair(putCards).point + 10) % 13,
    (previousCards, putCards) => (triple(previousCards).point + 10) % 13 < (triple(putCards).point + 10) % 13,

    // triple with pair
    (previousCards, putCards) => {
      val previousCombin = tripleWithPair(previousCards)
      val putCombin = tripleWithPair(putCards)

      if (pointOffset(previousCombin._1.point) < pointOffset(putCombin._1.point) && pointOffset(previousCombin._2.point) < pointOffset(putCombin._2.point)) {
        true
      } else {
        false
      }
    },
    // sequence
    (previousCards, putCards) => {
      val previousSequence = sequence(previousCards)
      val putSequence = sequence(putCards)
      if (previousSequence.size == putSequence.size && (previousSequence(0).point + 10) % 13 < (putSequence(0).point + 10) % 13) {
        true
      } else {
        false
      }
    },
    // pair sequence
    (previousCards, putCards) => {
      val previousSequence = pairSequence(previousCards)
      val putSequence = pairSequence(putCards)
      if (previousSequence.size == putSequence.size && (previousSequence(0).point + 10) % 13 < (putSequence(0).point + 10) % 13) {
        true
      } else {
        false
      }
    },

    // triple sequence
    (previousCards, putCards) => {
      val previousSequence = tripleSequence(previousCards)
      val putSequence = tripleSequence(putCards)
      if (previousSequence.size == putSequence.size && (previousSequence(0).point + 10) % 13 < (putSequence(0).point + 10) % 13) {
        true
      } else {
        false
      }
    },

    // boomb
    (previousCards, putCards) => (boomb(previousCards).point + 10) % 13 < (boomb(putCards).point + 10) % 13,
    (previousCards, putCards) => single(previousCards) != null && boomb(putCards) != null,
    (previousCards, putCards) => pair(previousCards) != null && boomb(putCards) != null,
    (previousCards, putCards) => triple(previousCards) != null && boomb(putCards) != null,
    (previousCards, putCards) => tripleWithPair(previousCards) != null && boomb(putCards) != null,
    (previousCards, putCards) => sequence(previousCards) != null && boomb(putCards) != null)

  def matchPutCardRule(previousCards: List[Card], putCards: List[Card]): Boolean = {

    putCardRules.find(p =>
      try {
        p(previousCards, putCards)
      } catch {
        case ex: Unmatched =>
          //ignore it
          false
      }).size > 0
  }

  /**
   * *************************************
   * Card Combinations matcher
   */

  protected def pointOffset(point: Int): Int = (point + 10) % 13

  protected def empty(cards: List[Card]): Boolean = {
    if (cards == null || cards.size == 0) {
      return true
    } else {
      return false
    }
  }
  /**
   * single card pattern matcher
   */
  protected def single(cards: List[Card]): Card = {
    if (cards.size == 1) {
      return cards(0)
    } else {
      throw new Unmatched()
    }
  }

  /**
   * pair cards pattern matcher
   */
  protected def pair(cards: List[Card]): Card = {
    if (cards.size == 2 && !cards.exists(card => card.point != cards(0).point)) {
      return cards(0)
    } else {
      throw new Unmatched()
    }
  }
  /**
   * triple cards pattern matcher
   */
  protected def triple(cards: List[Card]): Card = {
    if (cards.size == 3 && !cards.exists(card => card.point != cards(0).point)) {
      return cards(0)
    } else {
      throw new Unmatched()
    }
  }
  /**
   * four same cards pattern matcher
   */
  protected def boomb(cards: List[Card]): Card = {
    if (cards.size == 4 && !cards.exists(card => card.point != cards(0).point)) {
      return cards(0)
    } else {
      throw new Unmatched()
    }
  }

  protected def sequence(cards: List[Card]): List[Card] = {
    // a sequence at least have five cards
    if (cards.size > 4) {
      // sort cards
      val sortedCards = cards.sortWith((left, right) => (left.point + 10) % 13 < (right.point + 10) % 13)

      if ((sortedCards.last.point + 10) % 13 - (sortedCards.head.point + 10) % 13 == sortedCards.size - 1) {
        sortedCards
      } else {
        throw new Unmatched()
      }
    } else {
      throw new Unmatched()
    }
  }

  /**
   * triple with pair combination pattern matcher
   */
  protected def tripleWithPair(cards: List[Card]): Tuple2[Card, Card] = {
    if (cards.size == 5) {
      val cards1 = cards.filter(card => card.point == cards(0).point)
      val cards2 = cards.filter(card => card.point != cards1(0).point)

      if (!cards2.exists(card => card.point != cards2(0).point) && ((cards1.size == 3 && cards2.size == 2) || (cards1.size == 2 && cards2.size == 3))) {
        if (cards1.size == 3) {
          (cards1(0), cards2(0))
        } else {
          (cards2(0), cards1(0))
        }
      } else {
        throw new Unmatched()
      }
    } else {
      throw new Unmatched()
    }
  }

  /**
   * pair sequence pattern matcher
   */
  protected def pairSequence(cards: List[Card]): List[Card] = {
    val groupedCards = cards.groupBy(card => card.point)
    if (!groupedCards.exists(_._2.size != 2) && groupedCards.keySet.size >= 3) {
      val keyList = groupedCards.keySet.toList
      val sortedKeyList = keyList.sortWith((leftPoint, rightPoint) => pointOffset(leftPoint) < pointOffset(rightPoint))

      if (pointOffset(sortedKeyList.last) - pointOffset(sortedKeyList.head) == sortedKeyList.size - 1) {
        sortedKeyList.map(key => groupedCards.get(key).get(0))
      } else {
        throw new Unmatched()
      }
    } else {
      throw new Unmatched()
    }
  }

  /**
   * triple sequence pattern matcher
   */
  protected def tripleSequence(cards: List[Card]): List[Card] = {
    val groupedCards = cards.groupBy(card => card.point)
    if (!groupedCards.exists(_._2.size != 3) && groupedCards.keySet.size >= 2) {
      val keyList = groupedCards.keySet.toList
      val sortedKeyList = keyList.sortWith((leftPoint, rightPoint) => pointOffset(leftPoint) < pointOffset(rightPoint))

      if (pointOffset(sortedKeyList.last) - pointOffset(sortedKeyList.head) == sortedKeyList.size - 1) {
        sortedKeyList.map(key => groupedCards.get(key).get(0))
      } else {
        throw new Unmatched()
      }
    } else {
      throw new Unmatched()
    }
  }
}