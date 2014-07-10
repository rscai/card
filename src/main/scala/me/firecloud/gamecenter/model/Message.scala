/**
 *
 */
package me.firecloud.gamecenter.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Jan 4, 2014
 *
 */
abstract class Message(userId:String){
    @JsonProperty("cla")
    def cla:Long
    @JsonProperty("ins")
    def ins:Long
    
    @JsonIgnore
    def key=(cla,ins)
    
    val id=java.util.UUID.randomUUID().toString();
}
object JoinRoom extends Message("0"){
    def cla:Long=0x01
    def ins:Long=0x01
    
}

case class JoinRoom(userId:String,roomId:String,position:Int = -1) extends Message(userId){
    def cla:Long=JoinRoom.cla
    def ins:Long=JoinRoom.ins
}
object StartGame extends Message("0"){
    def cla:Long=0x01
    def ins:Long=0x02
}
case class StartGame(userId:String) extends Message(userId){
    def cla:Long=StartGame.cla
    def ins:Long=StartGame.ins
}


object EndGame extends Message("0"){
    def cla:Long=0x01
    def ins:Long=0x03
}

case class EndGame(userId:String) extends Message(userId){
    def cla:Long=EndGame.cla
    def ins:Long=EndGame.ins
}

object Notification extends Message("0"){
    def cla:Long=0x01
    def ins:Long=0x04
}

case class Notification(msg:Message) extends Message(Dealer.id){
    def cla:Long=Notification.cla
    def ins:Long=Notification.ins
}

object Ask extends Message("0"){
    def cla:Long=0x01
    def ins:Long=0x05
}
case class Ask(targetUserId:String,actions:List[Tuple2[Long,Long]],timeout:Long=0) extends Message(Dealer.id){
    def cla:Long=Ask.cla
    def ins:Long=Ask.ins
}


object Ready extends Message("0"){
    def cla:Long=0x01
    def ins:Long=0x06
}

case class Ready(userId:String) extends Message(userId){
    def cla:Long=Ready.cla
    def ins:Long=Ready.ins
}

object PlayerPropertyChange extends Message("0"){
    def cla:Long=0x01
    def ins:Long=0x07
}

case class PlayerPropertyChange(userId:String,changes:List[Tuple2[String,String]]) extends Message(userId){
    def cla:Long=PlayerPropertyChange.cla
    def ins:Long=PlayerPropertyChange.ins
}