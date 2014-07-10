/**
 *
 */
package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits._
import play.libs.Akka
import akka.actor.ActorRef
import me.firecloud.gamecenter.web.MessageCodecFilter
import me.firecloud.utils.logging.Logging
import akka.actor.Props
import me.firecloud.gamecenter.model.ClientConnectted

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Jan 1, 2014
 *
 */

object Connector extends Controller with Logging{
    val messageCodecFilter = new MessageCodecFilter()

    def communicate(userId: String) = WebSocket.using[String] {
        request => //Concurernt.broadcast returns (Enumerator, Concurrent.Channel)
            val (out, channel) = Concurrent.broadcast[String]
            
            // construct player actor
            
            
            val playerSupervisor =Akka.system().actorSelection("user/player")
            
            playerSupervisor!new ClientConnectted(userId,channel)
            
            //log the message to stdout and send response back to client
            val in = Iteratee.foreach[String] {
                msg =>
                    info(msg)
                    val decodedMesg = messageCodecFilter.decode(msg)
                    if (decodedMesg.isDefined) {
                        //the Enumerator returned by Concurrent.broadcast subscribes to the channel and will 
                        //receive the pushed messages
                        val playerActorRef=Akka.system().actorSelection("user/player/"+userId)
                        playerActorRef! decodedMesg.get                        
                        //channel push ("RESPONSE: " + decodedMesg.get.cla)
                    } else {
                        channel push ("RESPONSE: ERROR MESSAGE")
                    }
            }
            (in, out)
    }

}