/**
 *
 */
package me.firecloud.gamecenter.model

/**
 * @author kkppccdd
 *
 */
class SeatCycle[T <: Seat](seats: List[T]) {
  private var currentPos: Int = 0;
  // move to next seat
  def move: T = {
    currentPos = (currentPos + 1) % seats.size

    return seats(currentPos)
  }

  // next seat on this cycle
  def next: T = seats((currentPos + 1) % seats.size)

  // previous seat on this cycle
  def previous: T = seats((currentPos + seats.size - 1) % seats.size)

  // get current seat
  def current: T = seats(currentPos)
  // check if on turn
  def onturn(playerId: String) = current.playerId == playerId

  def head: T = seats(0)

}