/**
 *
 */
package controllers

import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import play.api._
import play.api.mvc._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.core.`type`.TypeReference
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import me.firecloud.gamecenter.model.RoomDescription
import me.firecloud.gamecenter.model.RoomFactoryManager
import play.libs.Akka
import me.firecloud.utils.logging.Logging
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.OptionModule
import com.fasterxml.jackson.module.scala.TupleModule
import com.fasterxml.jackson.databind.MapperFeature
import me.firecloud.gamecenter.dao.Query
import me.firecloud.gamecenter.dao.Create
import me.firecloud.gamecenter.model.Game
import me.firecloud.gamecenter.model.Player
import me.firecloud.gamecenter.dao.GameDao
import me.firecloud.gamecenter.dao.PlayerDao
import me.firecloud.gamecenter.dao.Get

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Mar 8, 2014
 *
 */
object Hall extends Controller with Logging {
  val mapper = new ObjectMapper() with ScalaObjectMapper

  val module = new OptionModule with TupleModule {}

  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(module)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)

  def create = Action { request =>
    {
      // create room model
      val payload = request.body.asJson

      val roomConfig = mapper.readValue[RoomDescription](payload.get.toString)

      // populate icon

      GameDao.get("kind" -> roomConfig.kind).map { game =>

        roomConfig.icon = game.icon

        val roomSupervisor = Akka.system().actorSelection("user/room")

        implicit val timeout = Timeout(1 seconds)

        val future = roomSupervisor ? new Create(roomConfig)

        val roomDescription = Await.result(future, timeout.duration).asInstanceOf[RoomDescription]
        // return room
        Ok(mapper.writeValueAsString(roomDescription))
      }.getOrElse {
        BadRequest
      }
    }
  }

  def query = Action {
    request =>
      {
        val criteria: String = if (request.queryString.contains("criteria")) { request.queryString.get("criteria").get(0) } else {
          ""
        }

        val roomSupervisor = Akka.system().actorSelection("user/room")

        implicit val timeout = Timeout(1 seconds)

        val future = roomSupervisor ? new Query(criteria)

        val roomDescriptions = Await.result(future, timeout.duration).asInstanceOf[List[RoomDescription]]

        // return rooms
        Ok(mapper.writeValueAsString(roomDescriptions))
      }
  }
  
  def get(roomId:String)=Action{
    request =>
      {
        // check room status
        val roomSupervisor = Akka.system().actorSelection("user/room")

        implicit val timeout = Timeout(1 seconds)

        val future = roomSupervisor ? new Get(roomId)
        val room = Await.result(future, timeout.duration).asInstanceOf[RoomDescription]
        
        // return rooms
        Ok(mapper.writeValueAsString(room))
      }
  }

  def enterRoom(roomId: String) = Action {
    request =>
      {
        // check room status
        val roomSupervisor = Akka.system().actorSelection("user/room")

        implicit val timeout = Timeout(1 seconds)

        val future = roomSupervisor ? new Get(roomId)
        val room = Await.result(future, timeout.duration).asInstanceOf[RoomDescription]

        val onPlaying = room.seats!=null && room.seats.size == room.seatNum && !room.seats.exists((x: Tuple3[String, String, String]) => x._1 == null)
        if (onPlaying == true) {
          // get user id from cookies
          request.session.get("player-id").map { playerId =>

            Ok(views.html.room(room, playerId))
          }.getOrElse {
            BadRequest("Miss player-id")
          }
        } else {
          // response waiting pages
          // get user id from cookies
          request.session.get("player-id").map { playerId =>

            Ok(views.html.waitting(room, playerId))
          }.getOrElse {
            BadRequest("Miss player-id")
          }
        }
      }
  }

  protected[this] def typeReference[T: Manifest] = new TypeReference[T] {
    override def getType = typeFromManifest(manifest[T])

  }

  protected[this] def typeFromManifest(m: Manifest[_]): Type = {
    if (m.typeArguments.isEmpty) { m.runtimeClass }
    else new ParameterizedType {
      def getRawType = m.runtimeClass
      def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray
      def getOwnerType = null
    }
  }
}