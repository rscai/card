package me.firecloud.gamecenter.service

import akka.actor.Actor
import me.firecloud.utils.logging.Logging
import scala.collection.mutable.HashMap
import me.firecloud.gamecenter.model.Room
import me.firecloud.gamecenter.dao.Create
import me.firecloud.gamecenter.dao.Query
import me.firecloud.gamecenter.dao.Update
import me.firecloud.gamecenter.dao.Get
import me.firecloud.gamecenter.dao.Delete
import me.firecloud.gamecenter.dao.GameDao
import me.firecloud.gamecenter.dao.PlayerDao

class RoomSupervisor extends Actor with Logging {

  val freeRooms = new HashMap[String, Room]
  val runningRooms = new HashMap[String, Room]

  def receive = {
    case Create(description: Room) =>
      // create room actor
      debug("lookup factory for " + description.kind)

      val (props, roomDescription) = RoomFactory.build(description)

      val roomRef = context.actorOf(props, roomDescription.id)

      debug("create room:" + roomRef.toString)

      freeRooms.put(roomDescription.id, roomDescription)

      sender ! roomDescription
    case Query(criteria: String) =>
      sender ! freeRooms.values.toList.map(room => populateRoom(room))
    case Update(description: Room) =>
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
    case Delete(roomId) =>
      freeRooms.remove(roomId);
      runningRooms.remove(roomId)
    case _ =>
      error("unsupported message")
  }

  protected def populateRoom(room: Room): Room = {
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