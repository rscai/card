/**
 *
 */
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.http.HeaderNames._
import me.firecloud.gamecenter.model.RoomFactoryManager
import me.firecloud.gamecenter.card.model.CardRoomFactory
import play.libs.Akka
import me.firecloud.gamecenter.model.PlayerSupervisor
import akka.actor.Props
import play.api.libs.concurrent.Execution.Implicits._
import me.firecloud.gamecenter.model.RoomSupervisor
import scala.concurrent.Future
import me.firecloud.utils.logging.Logging

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Mar 16, 2014
 *
 */

object AuthenticationFilter extends Filter with Logging{
  override def apply(next: (RequestHeader) => Future[SimpleResult])(request: RequestHeader): Future[SimpleResult] = {

    if (request.path.startsWith("/authentication") || request.path.startsWith("/assets") || request.path.startsWith("/favicon.ico")) {
      // pass
      val result = next(request)
      result
    } else {
      // check whether authenticated
      request.session.get("expire").map { expire =>
        // validate expire
        if (expire.toLong < System.currentTimeMillis()) {
          // invalid
          debug("expire:"+expire+",current timestamp:"+System.currentTimeMillis())
          
          Future.successful(Results.Redirect("/authentication/login.html", 302).withSession("request-target"->request.path))
        } else {
          next(request)
        }
      }.getOrElse {
        debug("don't find expire")
        
        Future.successful(Results.Redirect("/authentication/login.html", 302).withSession("request-target"->request.path))
      }

    }
  }
}
object Global extends WithFilters(AuthenticationFilter) {
  override def onStart(app: Application) {
    Logger.info("Application has started")
    RoomFactoryManager.registerFactory(new CardRoomFactory)

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