/**
 *
 */
package me.firecloud.gamecenter.card.model

import me.firecloud.gamecenter.model.Seat

/**
 * @author kkppccdd
 *
 */
class CardSeat extends Seat {
  var hand: Set[Card]=Set()
  var putCards:List[Card]=List()
}