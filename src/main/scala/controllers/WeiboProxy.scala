/**
 *
 */
package controllers

import me.firecloud.utils.logging.Logging
import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.WrappedArray
import scala.concurrent._

/**
 * @author kkppccdd
 *
 */
object WeiboProxy extends Controller with Logging {

  def post = Action.async(parse.tolerantFormUrlEncoded) {
    request =>

      val method = request.body.get("method").map(method => method.head).getOrElse(null)
      val url = request.body.get("url").map(url => url.head).getOrElse(null)
      val params = request.body.filterNot { case (k, v) => k == "method" || k == "url" }//.mapValues(v=>v.head)


      if (method == null || url == null) {
        Future { BadRequest("miss parameter") }
      } else {

        if (method.equals("POST")) {
          WS.url(url).post(params).map {
            response =>
              if (response.status == 200) {
                Ok(response.body)
              } else {
                BadRequest("External service is unavailable:" + response.statusText)
              }
          }
        } else if (method.equals("GET")) {
          WS.url(url).withQueryString(params.mapValues(v => v(0)).toSeq: _*).get.map {
            response =>
              if (response.status == 200) {
                Ok(response.body)
              } else {
                BadRequest("External service is unavailable:" + response.statusText)
              }
          }
        } else {
          Future { BadRequest("invalid paramter method:"+method) }
        }
      }

  }

}