/**
 *
 */
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.http.HeaderNames._
import play.libs.Akka
import me.firecloud.gamecenter.model.PlayerSupervisor
import akka.actor.Props
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import me.firecloud.utils.logging.Logging
import me.firecloud.gamecenter.service.RoomSupervisor
import java.util.UUID

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Mar 16, 2014
 *
 */

object AuthenticationFilter extends Filter with Logging {
  override def apply(next: (RequestHeader) => Future[SimpleResult])(request: RequestHeader): Future[SimpleResult] = {
    if (request.path.startsWith("/assets")) {
      next(request)
    } else {
      request.session.get("player-id").map {
        playerId =>
          next(request)
      }.getOrElse {
        Future.successful(Results.Redirect(request.path, 302).withSession("player-id" -> UUID.randomUUID().toString))
      }
    }
  }
}
object Global extends WithFilters(AuthenticationFilter) {
  override def onStart(app: Application) {
    Logger.info("Application has started")

    // initialize supervisor actors

    // players supervisor
    val playerSupervisor = Akka.system().actorOf(Props(new PlayerSupervisor()), name = "player")
    Logger.info("started player supervisor :" + playerSupervisor.toString)

    // room suppervisor

    val roomSupervisor = Akka.system().actorOf(Props(new RoomSupervisor()), name = "room");

    Logger.info("started room suppervisor:" + roomSupervisor.toString)
  }
  /*
    override def doFilter(action: EssentialAction): EssentialAction = EssentialAction { request =>
    action.apply(request).map(_.withHeaders(
      "Access-Control-Allow-Origin"->"*"
    ))
  }
  * */

}