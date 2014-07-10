/**
 *
 */
package me.firecloud.gamecenter.dao

import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.Play.current
import me.firecloud.gamecenter.model.Game
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import se.radley.plugin.salat._
import com.mongodb.casbah.commons.MongoDBObject
import me.firecloud.gamecenter.dao.mongodbContext._
import me.firecloud.gamecenter.model.Game

/**
 * @author kkppccdd
 *
 */
object GameDao {
  val dao = new SalatDAO[Game, String](collection = mongoCollection("games")) {}

  def create(game: Game): Game = {
    dao.save(game)

    game
  }

  def update(game: Game): Game = {
    dao.save(game)

    game
  }

  def get(id: String): Option[Game] = {
    dao.findOneById(id)
  }

  def get(criteria: (String, Any)*): Option[Game] = {
    val cursor = dao.find(MongoDBObject(criteria.toList)).limit(1)
    if (cursor.hasNext) {
      Some(cursor.next)
    } else {
      None
    }
  }

  def query(criteria: (String, Any)*): List[Game] = {
    dao.find(MongoDBObject(criteria.toList)).toList
  }
}