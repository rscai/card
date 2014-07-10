/**
 *
 */
package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import me.firecloud.gamecenter.model.Player
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.OptionModule
import com.fasterxml.jackson.module.scala.TupleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature

object UserDao extends ModelCompanion[Player, String] {
  val dao = new SalatDAO[Player, String](collection = mongoCollection("user")) {}

  def findOneByUsername(username: String): Option[Player] = dao.findOne(MongoDBObject("username" -> username))
  def findByCountry(country: String) = dao.find(MongoDBObject("address.country" -> country))
}



/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Apr 20, 2014
 *
 */
object RestService extends Controller {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    
    val module = new OptionModule with TupleModule {}
    
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(module)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)

    def getClass(kind:String):Class[_]={
        return classOf[Player]
    }
    
    def post(kind:String)=Action{
        request=>
            val entity = mapper.readValue[Player](request.body.asText.get)

            //val dao = getDao(kind)
            
            //dao.save(entity)
            
            Ok(mapper.writeValueAsString(entity))
    }

    
    
}