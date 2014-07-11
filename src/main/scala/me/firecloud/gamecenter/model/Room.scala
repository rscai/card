/**
 *
 */
package me.firecloud.gamecenter.model

import scala.collection.mutable.Map
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Props
import me.firecloud.gamecenter.card.model.Card
import me.firecloud.utils.logging.Logging
import play.libs.Akka
import me.firecloud.gamecenter.dao.Create
import scala.collection.mutable.HashMap
import me.firecloud.gamecenter.dao.Query
import me.firecloud.gamecenter.dao.Update
import me.firecloud.gamecenter.dao.Get
import me.firecloud.gamecenter.dao.GameDao
import me.firecloud.gamecenter.dao.PlayerDao

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Mar 8, 2014
 *
 */
case class RoomDescription(val kind: String, val name: String, val seatNum: Int) {
  var id: String = null
  var icon: String = null
  var seats: List[Tuple3[String, String, String]] = null

}

class Seat {
  /**
   * FREE,OCCUPIED,READY,ACTIVE,MISSED,TERMINATED
   */
  private var _state: String = "FREE"
  var playerId: String = null // only player id
  var bet: Int = 0
  var role: String = "FARMER"
  var hand: Set[Card] = Set()
  var putCards:List[Card]=List()

  /**
   * *****************************************
   * properties accessors
   */
  def state: String = _state
  /**
   * *******************************
   * state change functions
   */
  def occupy: String = {
    // check precondition
    if (_state.equals("FREE")) {
      _state = "OCCUPIED"
      return _state
    } else {
      return _state
    }
  }

  def ready: String = {
    // check precondition
    if (_state.equals("OCCUPIED")) {
      _state = "READY"
      return _state
    } else {
      return _state
    }
  }

  def activate: String = {
    // check precondition
    if (_state.equals("READY")) {
      _state = "ACTIVE"
      return _state
    } else {
      return _state
    }
  }

  def miss: String = {
    // check precondition
    if (_state.equals("ACTIVE")) {
      _state = "MISSED"
      return _state
    } else {
      return _state
    }
  }

  def comeBack: String = {
    // check precondition
    if (_state.equals("MISSED")) {
      _state = "ACTIVE"
      return _state
    } else {
      return _state
    }
  }

  override def equals(obj: Any): Boolean = {
    return obj.isInstanceOf[Seat] && obj.asInstanceOf[Seat].playerId == this.playerId
  }
}

abstract class Room(val id: String, val kind: String, val name: String, val seatNum: Int) extends Actor {
  var timeout: Long = 45 // seconds
  val seats: List[Seat] = (for (i <- (1 to seatNum)) yield new Seat).toList
}

trait RoomFactory {
  def kind: String
  def build(description: RoomDescription): Tuple2[RoomDescription, Props]
}

object RoomFactoryManager {
  private val _registeredFactories = Map[String, RoomFactory]()

  def registerFactory(factory: RoomFactory) = _registeredFactories.put(factory.kind, factory);

  def unregisterFactory(kind: String): Option[RoomFactory] = _registeredFactories.remove(kind)

  def unregisterFactory(factory: RoomFactory): Option[RoomFactory] = unregisterFactory(factory.kind);

  def getFactory(kind: String): Option[RoomFactory] = _registeredFactories.get(kind)

}

class RoomSupervisor extends Actor with Logging {

  val freeRooms = new HashMap[String, RoomDescription]
  val runningRooms = new HashMap[String, RoomDescription]

  def receive = {
    case Create(description: RoomDescription) =>
      // create room actor
      debug("lookup factory for " + description.kind)
      val factory = RoomFactoryManager.getFactory(description.kind).get
      val (roomDescription, props) = factory.build(description)

      val roomRef = context.actorOf(props, roomDescription.id)

      debug("create room:" + roomRef.toString)

      freeRooms.put(roomDescription.id, roomDescription)

      sender ! roomDescription
    case Query(criteria: String) =>
      sender ! freeRooms.values.toList.map(room=>populateRoom(room))
    case Update(description: RoomDescription) =>
      // 
      if (description.seats.size == description.seatNum && !description.seats.exists((x: Tuple3[String, String, String]) => x._1 == null)) {
        // room is full
        freeRooms.remove(description.id).map {
          room =>
            runningRooms.put(description.id, description);
        }
      } else {
        freeRooms.update(description.id, description)
      }
    case Get(roomId) =>
      freeRooms.get(roomId).map {
        room =>
         sender ! populateRoom(room)
      }.getOrElse {
        runningRooms.get(roomId).map {
          room =>
            sender ! populateRoom(room)
        }.getOrElse {
          sender ! None
        }
      }
    case _ =>
      error("unsupported message")
  }
  
  protected def populateRoom(room:RoomDescription):RoomDescription={
    // populate icon of game

          val gameIcon = GameDao.get("kind" -> room.kind).map {
            game =>
              game.icon
          }.getOrElse {
            ""
          }

          room.icon = gameIcon

          // populate name and avatar of user

          if (room.seats != null) {
            val seats = room.seats.map(x => {
              // load user
              if (x._1 != null) {
                PlayerDao.get(x._1).map { player =>
                  (player.id, player.name, player.avatar)
                }.getOrElse {
                  (null, null, null)
                }
              } else {
                (null, null, null)
              }
            })
            room.seats = seats
          }
          
          room;
  }
}
