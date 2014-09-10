/**
 *
 */
package me.firecloud.gamecenter.service

import akka.actor.Props
import me.firecloud.gamecenter.model.Room
import me.firecloud.gamecenter.dao.GameDao
import java.util.UUID
import me.firecloud.gamecenter.engine.Engine
import me.firecloud.gamecenter.model.Seat
import akka.actor.Actor

/**
 * @author kkppccdd
 *
 */
object RoomFactory {

  def build(room:Room):Tuple2[Props,Room]={
    GameDao.get("kind" -> room.kind).map{
      game=>
      val id = UUID.randomUUID().toString()
      
      val props = Props(Class.forName(game.engineClass), id,room.seatNum,room.parameters)
      room.id=id
      
      (props,room)
      
    }.getOrElse{
      throw new Exception("game not found")
    }
  }
}