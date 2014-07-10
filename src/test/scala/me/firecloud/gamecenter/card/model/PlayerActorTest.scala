/**
 *
 */
package me.firecloud.gamecenter.card.model

import org.junit._
import Assert._
import akka.testkit.TestActorRef
import akka.actor.ActorSystem
import me.firecloud.gamecenter.model.Player
import me.firecloud.gamecenter.model.PlayerActor
import akka.actor.Actor

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Mar 9, 2014
 *
 */
class PlayerActorTest {
    implicit val system = ActorSystem("unittest")

    @Test
    def testJoinRoom() {
    	// construct player actor
        val player1 = new Player("1","player1")
        val playerActor1 = TestActorRef(new PlayerActor(player1),name=player1.id)
        
        // construct mock room actor
        
        
    }
}