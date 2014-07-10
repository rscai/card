/**
 *
 */
package me.firecloud.utils.logging

import org.slf4j.LoggerFactory
import play.api.Logger


/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Mar 13, 2014
 *
 */
trait Logging {
    private[this] val logger = Logger.logger
    def debug(message: => String) =  logger.debug(message)
    def debug(message: => String, ex: Throwable) = logger.debug(message, ex)
    def debugValue[T](valueName: String, value: => T): T = {
        val result: T = value
        debug(valueName + " == " + result.toString)
        result
    }

    def info(message: => String) = logger.info(message)
    def info(message: => String, ex: Throwable) = logger.info(message, ex)

    def warn(message: => String) = logger.warn(message)
    def warn(message: => String, ex: Throwable) = logger.warn(message, ex)

    def error(ex: Throwable) = logger.error(ex.toString, ex)
    def error(message: => String) = logger.error(message)
    def error(message: => String, ex: Throwable) = logger.error(message, ex)

}