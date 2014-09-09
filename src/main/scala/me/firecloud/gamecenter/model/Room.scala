/**
 *
 */
package me.firecloud.gamecenter.model

import scala.collection.immutable.Map

/**
 * @author kkppccdd
 *
 */
case class Room(val kind: String,val seatNum:Int,val parameters:Map[String,Any]) {
  var id: String = null
  var icon: String = null
  var seats: List[Tuple3[String, String, String]] = null

}