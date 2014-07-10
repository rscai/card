package controllers

import play.api._
import play.api.mvc._
import me.firecloud.gamecenter.model.RoomFactoryManager
import me.firecloud.gamecenter.card.model.CardRoomFactory

object Application extends Controller {

   
    
  def index = Action {
    Ok("Your new application is ready.")
  }

}