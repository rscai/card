package me.firecloud.gamecenter.dao

import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.Play.current
import me.firecloud.gamecenter.model.Player
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import se.radley.plugin.salat._
import com.mongodb.casbah.commons.MongoDBObject
import me.firecloud.gamecenter.dao.mongodbContext._
import scala.util.Random

object PlayerDao {
  val dao = new SalatDAO[Player, String](collection = mongoCollection("players")) {}

  def create(player: Player): Player = {
    dao.save(player)

    player
  }

  def update(player: Player): Player = {
    dao.save(player)

    player
  }

  def get(id: String): Option[Player] = {
    //dao.findOneById(id)
    val rn =id.hashCode().abs %  7+1
    Some(new Player(id,"player "+rn,"/assets/images/avatars/"+rn+".png"))
  }

  def get(criteria: (String, Any)*): Option[Player] = {
    val cursor = dao.find(MongoDBObject(criteria.toList)).limit(1)
    if (cursor.hasNext) {
      Some(cursor.next)
    } else {
      None
    }
  }

  def query(criteria: (String, Any)*): List[Player] = {
    dao.find(MongoDBObject(criteria.toList)).toList
  }
}
