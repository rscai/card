/**
 *
 */
package me.firecloud.gamecenter.dao

import com.novus.salat._
import play.api.Play
/**
 * @author kkppccdd
 *
 */
package object mongodbContext {
  implicit val ctx = new Context {
    val name = "Custom_Classloader"
  }
  ctx.registerClassLoader(Play.classloader(Play.current))
}