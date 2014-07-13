/**
 *
 */
package controllers

import play.api._
import play.api.mvc._
import weibo4j.util.WeiboConfig
import weibo4j.Oauth
import me.firecloud.utils.logging.Logging
import me.firecloud.gamecenter.dao.PlayerDao
import me.firecloud.gamecenter.model.Player
import java.util.UUID
import me.firecloud.gamecenter.model.SocialAccount
import me.firecloud.gamecenter.model.SocialAccount
import weibo4j.Weibo
import weibo4j.Users

/**
 * @author kkppccdd
 *
 */
object Authentication extends Controller with Logging {
  // initialize configuration of sina weibo

  WeiboConfig.updateProperties("client_ID", "357713296");
  WeiboConfig.updateProperties("client_SERCRET", "7c4edf948c95062dcf5948cbe734e495");
  WeiboConfig.updateProperties("redirect_URI", "http://kkppccdd-game-center.herokuapp.com/authentication/callback/sina");
  WeiboConfig.updateProperties("baseURL", "https://api.weibo.com/2/");
  WeiboConfig.updateProperties("accessTokenURL", "https://api.weibo.com/oauth2/access_token");
  WeiboConfig.updateProperties("authorizeURL", "https://api.weibo.com/oauth2/authorize");
  WeiboConfig.updateProperties("rmURL", "https://rm.api.weibo.com/2/");

  val oauth: Oauth = new Oauth

  def callback(kind: String) = Action {
    request =>
      kind match {
        case "sina" =>
          //try{
          //validate 
          val code = request.getQueryString("code").get
          val accessToken = oauth.getAccessTokenByCode(code)

          // extract social id, social token and social expire

          val socialId = accessToken.getUid()
          val socialToken = accessToken.getAccessToken()
          // if expire return by sina weibo is shorter than 1 day (86400000), then set it as 1 day
          val socialExpire =  if(accessToken.getExpireIn().toLong < 86400000){System.currentTimeMillis() + 86400000}else{ System.currentTimeMillis()+accessToken.getExpireIn().toLong}

          debug("expire:" + socialExpire + ",social token expire in:" + accessToken.getExpireIn())

          // associate user 
          var associatedPlayer: Player = null
          PlayerDao.get("socialAccounts.socialId" -> socialId).map { player =>
            associatedPlayer = player

          }.getOrElse {
            // create new player record
             
            // retrieve user name from social service
            val users= new Users
            users.setToken(socialToken)
            val socialUser = users.showUserById(socialId)
            
            associatedPlayer =
              PlayerDao.create(new Player(UUID.randomUUID().toString(), socialUser.getScreenName(), socialUser.getAvatarLarge(), List(new SocialAccount(socialId, "sina"))))
          }

          // 
          val requestTarget = request.session.get("request-target").getOrElse { "/" }

          Redirect(requestTarget, 302).withNewSession.withSession("player-id"->associatedPlayer.id,"social-id" -> socialId, "socialToken" -> socialToken, "expire" -> String.valueOf(socialExpire)).withCookies(new Cookie("social-token",socialToken,httpOnly=false),new Cookie("social-id",socialId,httpOnly=false))

        //}catch(NoSuchElementException ex){
        //  Status(401)("Authentication failed.")
        //}
        case _ =>
          InternalServerError("Unsupportted")
      }
  }

}