/**
 *
 */
package me.firecloud.gamecenter.engine

import scala.collection.immutable.Map
import akka.actor.Actor
import akka.actor.Props
import me.firecloud.gamecenter.card.model.Card
import me.firecloud.utils.logging.Logging
import me.firecloud.gamecenter.dao.Create
import scala.collection.mutable.HashMap
import me.firecloud.gamecenter.dao.Query
import me.firecloud.gamecenter.dao.Update
import me.firecloud.gamecenter.dao.Get
import me.firecloud.gamecenter.dao.GameDao
import me.firecloud.gamecenter.dao.PlayerDao
import me.firecloud.gamecenter.dao.Delete
import akka.actor.actorRef2Scala
import me.firecloud.gamecenter.model.Seat
import me.firecloud.gamecenter.model.SeatCycle

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Mar 8, 2014
 *
 */






abstract class Engine[SeatType <: Seat](val id: String, val seatNum:Int, val kind: String, val parameters: Map[String, Any]) extends Actor {
  /**
   * *****
   * constants
   */
  val KEY_SEATNUM: String = "seatNum"

  var timeout: Long = 45 // seconds
  var defaultCycle: SeatCycle[SeatType] = null;


  /**
   * **************************
   * action performers.
   */

  protected def ready(seat: Seat): Unit = {
    seat.ready

    // notify all participant
  }
}




