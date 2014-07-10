package me.firecloud.gamecenter.dao

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import me.firecloud.gamecenter.model.Player
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import me.firecloud.gamecenter.model.SocialAccount
import me.firecloud.gamecenter.model.SocialAccount

@RunWith(classOf[JUnitRunner])
class TestPlayerDaoSpec extends Specification {
  "Player Dao" should {

    "create player" in {
      running(FakeApplication(additionalPlugins=List("se.radley.plugin.salat.SalatPlugin"))) {

        val player = new Player("3","test1","http://test/avatar",List(new SocialAccount("12345","sina"),new SocialAccount("24678","wechat")))

        val createdPlayer = PlayerDao.create(player)

        createdPlayer.id must equalTo("3")
      }
    }
    
    "retrieve by id" in{
      running(FakeApplication(additionalPlugins=List("se.radley.plugin.salat.SalatPlugin"))) {
        val player = PlayerDao.get("3").get
        
        player.name must equalTo("test1")
        player.socialAccounts must have size(2)
      }
    }
    
    "query by social id" in{
      running(FakeApplication(additionalPlugins=List("se.radley.plugin.salat.SalatPlugin"))) {
        val players = PlayerDao.query("socialAccounts.socialId"->"12345")
        
        players must have size(1)
        players(0).socialAccounts must have size(2)
        players(0).socialAccounts(0).socialType must equalTo("sina")
      }
    }
  }
}