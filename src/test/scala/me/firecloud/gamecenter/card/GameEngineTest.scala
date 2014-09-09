/**
 *
 */
package me.firecloud.gamecenter.card

import org.junit.Test
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.HttpEntity
import org.apache.http.entity.StringEntity
import org.apache.http.entity.ContentType
import org.junit.Before
import org.junit.After
import org.apache.http.util.EntityUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.core.`type`.TypeReference
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import me.firecloud.gamecenter.model.JoinRoom
import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.websocket.WebSocketUpgradeHandler
import com.ning.http.client.websocket.WebSocketTextListener
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import com.ning.http.client.websocket.WebSocket
import me.firecloud.utils.logging.Logging
import java.util.concurrent.TimeUnit
import me.firecloud.gamecenter.model.Room

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Mar 17, 2014
 *
 */

class WSReceiver(messageQueue: BlockingQueue[String]) extends WebSocketTextListener with Logging {
    override def onMessage(message: String) = {
        this.messageQueue.put(message)
    }

    override def onFragment(message: String, last: Boolean) = {
        //this.messageQueue.put(message)
    }

    override def onClose(ws: WebSocket) = {
        info("web socket is closed")
    }

    override def onError(ex: Throwable) = {
        error(ex.getMessage())
    }

    override def onOpen(ws: WebSocket) = {
        info("web socket is opened")
    }
}
class GameEngineTest {
    var httpClient: HttpClient = null
    var host = "http://localhost:9000"
    var wsHost = "ws://localhost:9000"

    var mapper: ObjectMapper = null;

    @Before
    def setUp() {
        httpClient = new DefaultHttpClient()
        mapper = new ObjectMapper()
        mapper.registerModule(DefaultScalaModule)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @After
    def tearDown() {
        httpClient.getConnectionManager().shutdown();
    }

    protected def post(endpoint: String, content: String): String = {
        val request = new HttpPost(host + endpoint)
        request.setHeader("Content-Type", "application/json")
        request.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));

        val response = httpClient.execute(request)

        EntityUtils.toString(response.getEntity())
    }

    @Test
    def testJoinRoom {
        // create room

        val createJson = "{\"kind\":\"card\",\"name\":\"test\"}"

        val response = post("/rest/room", createJson)
        val roomDescription = mapper.readValue(response, typeReference[Room]).asInstanceOf[Room]

        Assert.assertNotNull(roomDescription.id)
        Assert.assertEquals("card", roomDescription.kind)
        

        // create three websocket

        val asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().build());

        val msgQueue1 = new LinkedBlockingQueue[String]()
        val ws1 = asyncHttpClient.prepareGet(wsHost + "/connect/1").execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
            new WSReceiver(msgQueue1)).build()).get()

        val msgQueue2 = new LinkedBlockingQueue[String]()
        val ws2 = asyncHttpClient.prepareGet(wsHost + "/connect/2").execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
            new WSReceiver(msgQueue2)).build()).get()
        val msgQueue3 = new LinkedBlockingQueue[String]()
        val ws3 = asyncHttpClient.prepareGet(wsHost + "/connect/3").execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
            new WSReceiver(msgQueue3)).build()).get()

        // create three join room message
        val joinRoom1 = new JoinRoom("1", roomDescription.id)
        val joinRoom2 = new JoinRoom("2", roomDescription.id)
        val joinRoom3 = new JoinRoom("3", roomDescription.id)
        
        // player1 join room

        ws1.sendTextMessage(mapper.writeValueAsString(joinRoom1))

        var resp1 = msgQueue1.poll(5,TimeUnit.SECONDS)

        Assert.assertEquals(joinRoom1.userId,mapper.readTree(resp1).findValue("msg").findValue("userId").asText())
        Assert.assertEquals(joinRoom1.roomId,mapper.readTree(resp1).findValue("msg").findValue("roomId").asText())
        
        //player 2 join room
        ws2.sendTextMessage(mapper.writeValueAsString(joinRoom2))
        
        resp1 = msgQueue1.poll(5,TimeUnit.SECONDS)

        Assert.assertEquals(joinRoom2.userId,mapper.readTree(resp1).findValue("msg").findValue("userId").asText())
        Assert.assertEquals(joinRoom2.roomId,mapper.readTree(resp1).findValue("msg").findValue("roomId").asText())
        
        var resp2 =msgQueue2.poll(5,TimeUnit.SECONDS)
        
        Assert.assertEquals(joinRoom2.userId,mapper.readTree(resp2).findValue("msg").findValue("userId").asText())
        Assert.assertEquals(joinRoom2.roomId,mapper.readTree(resp2).findValue("msg").findValue("roomId").asText())
        
        // player3 join room
        ws3.sendTextMessage(mapper.writeValueAsString(joinRoom3))
        
        resp1 = msgQueue1.poll(5,TimeUnit.SECONDS)

        Assert.assertEquals(joinRoom3.userId,mapper.readTree(resp1).findValue("msg").findValue("userId").asText())
        Assert.assertEquals(joinRoom3.roomId,mapper.readTree(resp1).findValue("msg").findValue("roomId").asText())
        
        resp2 =msgQueue2.poll(5,TimeUnit.SECONDS)
        
        Assert.assertEquals(joinRoom3.userId,mapper.readTree(resp2).findValue("msg").findValue("userId").asText())
        Assert.assertEquals(joinRoom3.roomId,mapper.readTree(resp2).findValue("msg").findValue("roomId").asText())
        
        
        var resp3 =msgQueue3.poll(5,TimeUnit.SECONDS)
        
        Assert.assertEquals(joinRoom3.userId,mapper.readTree(resp3).findValue("msg").findValue("userId").asText())
        Assert.assertEquals(joinRoom3.roomId,mapper.readTree(resp3).findValue("msg").findValue("roomId").asText())

    }

    protected[this] def typeReference[T: Manifest] = new TypeReference[T] {
        override def getType = typeFromManifest(manifest[T])

    }

    protected[this] def typeFromManifest(m: Manifest[_]): Type = {
        if (m.typeArguments.isEmpty) { m.runtimeClass }
        else new ParameterizedType {
            def getRawType = m.runtimeClass
            def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray
            def getOwnerType = null
        }
    }
}