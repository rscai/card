/**
 *
 */
package me.firecloud.gamecenter.model

import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

/**
 * @author kkppccdd
 *
 */
case class Game(val kind: String, val icon: String) {

}

