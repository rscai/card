/**
 *
 */
package me.firecloud.gamecenter.card.engine

import me.firecloud.gamecenter.model.JoinRoom
import me.firecloud.gamecenter.model.StartGame
import me.firecloud.gamecenter.model.Dealer
import me.firecloud.gamecenter.model.Ready
import me.firecloud.gamecenter.card.model.Bet
import me.firecloud.gamecenter.model.PlayerPropertyChange
import me.firecloud.gamecenter.card.model.PutCard
import me.firecloud.gamecenter.card.model.AppendCard
import me.firecloud.gamecenter.card.model.Pass
import scala.collection.immutable.Map
import me.firecloud.gamecenter.card.model.CardSeat

/**
 * @author kkppccdd
 *
 */
class FightlordCardEngine(id: String, parameters:Map[String,Any]) extends CardRoom[CardSeat](id, "Fightlord",parameters) {
 startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(JoinRoom(userId, roomId, position), Uninitialized) =>

      // 1. perform action and notify state changes
      info("received join room message")
      join(userId)

      // 2. perform check

      // 3. calculate acceptable subsequent actions
      // check whether it's able to start
      if (!seats.exists(_.state == "FREE")) {
        // ask start game
        //ask(seats(0), List(StartGame.key))

        receive(new StartGame(Dealer.id))
      }
      // 4. transform FSM state
      stay
    case Event(StartGame(userId), Uninitialized) => {

      // send startGame message to all players
      val startGameMsg = new StartGame(userId);

      notifyAll(startGameMsg);
      // 
      goto(Startting) // for waiting ready messages from all players
    }
  }

  when(Startting) {
    case Event(Ready(userId), Uninitialized) => {
      // lookup seat
      seats.find(seat => seat.playerId == userId).map { seat =>
        ready(seat)

        // check whether all players are ready

        if (seats.exists(seat => seat.state != "READY") == false) {
          // all players are ready
          // 
          init
          dealCard(seats, () => {
            val cardBatchSize = 17
            val cards = reservedCards.take(cardBatchSize)
            reservedCards = reservedCards.drop(cardBatchSize)
            cards
          })
          // send ask

          ask(seats(0), List(Bet.key))

          //

          goto(BetState)
        } else {
          stay
        }
      }.getOrElse {
        // requester is not in this room, ignore
        stay
      }

    }
  }

  when(BetState) {
    case Event(Bet(userId, amount), Uninitialized) if defaultCycle.onturn(userId) => {
      seats.find(seat => seat.playerId == userId).map {
        seat =>
          bet(seat, amount)

          defaultCycle.move

          // check whether all players have bet

          if (defaultCycle.current.playerId == defaultCycle.head.playerId) {
            // all players have bet

            // decide who is the landlord

            val maxBetSeat = seats.maxBy(_.bet)
            maxBetSeat.role = "LANDLORD"

            notifyAll(new PlayerPropertyChange(maxBetSeat.playerId, List(("role", "LANDLORD"))))

            // deal last three cards

            dealCard(List(maxBetSeat), () => {
              var cards = reservedCards.take(4)
              reservedCards = reservedCards.drop(4)
              cards
            })

            // move default turn to maxBetSeat
            while (defaultCycle.current.playerId != maxBetSeat.playerId) {
              defaultCycle.move
            }

            // ask landlord put card

            ask(maxBetSeat, List(PutCard.key))

            goto(StartPutCard)
          } else {
            // ask next seat to bet
            ask(defaultCycle.current, List(Bet.key))
            stay
          }
      }.getOrElse {
        stay
      }
    }
  }

  when(StartPutCard) {
    case Event(PutCard(userId, cards), Uninitialized) if defaultCycle.onturn(userId) /*&& matchPutCardRule(List[Card](), cards)*/ =>
      // lookup seat
      seats.find(_.playerId == userId).map {
        seat =>
          // 1. perform action and notify state changes
          debug(userId + " put cards " + cards.size)
          cards.foreach(card => {
            seat.hand = seat.hand - card
          })

          lastAppendSeat = seat
          lastAppendSeat.putCards = cards

          defaultCycle.move
          // notify
          val putCardMsg = new PutCard(userId, cards)
          notifyAll(putCardMsg)

          // 2. perform check

          // check if finish
          if (anyoneWin) {
            //
            end
          }

          // 3. calculate acceptable subsequent actions
          ask(defaultCycle.current, List(AppendCard.key, Pass.key))

          // 4. transform FSM state
          goto(AppendPutCard)
      }.getOrElse {
        stay
      }
  }

  when(AppendPutCard) {
    case Event(AppendCard(userId, cards), Uninitialized) if defaultCycle.onturn(userId) /*&& matchPutCardRule(lastAppendSeat.putCards, cards)*/ => {
      // lookup seat
      seats.find(_.playerId == userId).map {
        seat =>

          debug(userId + " append cards " + cards.size)
          cards.foreach(card => {
            seat.hand = seat.hand - card
          })

          lastAppendSeat = seat
          lastAppendSeat.putCards = cards

          defaultCycle.move
          // notify
          val appendCardMsg = new AppendCard(userId, cards)
          notifyAll(appendCardMsg)

          // 2. perform check

          // check if finish
          if (anyoneWin) {
            //
            end
          }

          // 3. calculate acceptable subsequent actions
          ask(defaultCycle.current, List(AppendCard.key, Pass.key))

          stay
      }.getOrElse {
        stay
      }
    }
    case Event(Pass(userId), Uninitialized) if defaultCycle.onturn(userId) => {
      // lookup seat
      seats.find(_.playerId == userId).map {
        seat =>

          // 1. perform action and notify state changes
          defaultCycle.move
          // notify
          val passMsg = new Pass(userId)
          notifyAll(passMsg)

          // 2. perform check
          // check if no one append
          if (lastAppendSeat.equals(defaultCycle.current)) {
            ask(defaultCycle.current, List(PutCard.key))
            goto(StartPutCard)
          } else {
            // 3. calculate acceptable subsequent actions
            ask(defaultCycle.current, List(AppendCard.key, Pass.key))
            // 4. transform FSM state
            stay
          }
      }.getOrElse {
        stay
      }
    }
  }

  initialize

}