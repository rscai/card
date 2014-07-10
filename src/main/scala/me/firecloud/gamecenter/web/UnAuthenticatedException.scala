/**
 *
 */
package me.firecloud.gamecenter.web

import java.lang.Throwable

/**
 * @author kkppccdd
 *
 */
class UnAuthenticatedException(msg: String, cause: Throwable) extends Exception(msg, cause) {
  def this() = this(null, null)
  def this(msg: String) = this(msg, null)
}