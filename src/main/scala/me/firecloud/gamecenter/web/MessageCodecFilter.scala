/**
 *
 */
package me.firecloud.gamecenter.web

import me.firecloud.gamecenter.model.Message
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import me.firecloud.gamecenter.card.web.CardMessageCodec
import java.util.logging.Logging
import org.slf4j.LoggerFactory

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Jan 4, 2014
 *
 */
class MessageCodecFilter {
    val log = LoggerFactory.getLogger(this.getClass())

    var messageCodecs = Map[Tuple2[Long,Long], MessageCodec]()

    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    {
        //val commonMessageCodec = new CommonMessageCodec()
        //commonMessageCodec.supportedMessageCodes.foreach(code => messageCodecs += { code -> commonMessageCodec })

        val cardMessageCodec = new CardMessageCodec()
        cardMessageCodec.supportedMessageCodes.foreach(code => messageCodecs += { code -> cardMessageCodec })
    }

    def encode(message: Message): Option[String] = {
        try {
            val messageCodec = messageCodecs((message.cla,message.ins))
            Some(messageCodec.encode(message))
        } catch {
            case ex =>
                log.warn(ex.getMessage(), ex)
                None
        }
    }

    def decode(json: String): Option[Message] = {
        try {
        	val key = (mapper.readTree(json).findValue("cla").asLong(),mapper.readTree(json).findValue("ins").asLong())
            val messageCodec = messageCodecs(key)
            Some(messageCodec.decode(json))
        } catch {
            case ex =>
                log.warn(ex.getMessage(), ex)
                None
        }
    }
}