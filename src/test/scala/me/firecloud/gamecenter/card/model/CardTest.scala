/**
 *
 */
package me.firecloud.gamecenter.card.model

import org.junit.Test
import org.junit.Assert._

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date May 4, 2014
 *
 */
class CardTest {

    
    @Test
    def testPokerPack(){
        val cardPack = for(suit<-Suit.values; point<-(1 to 13)) yield new Card(suit.toString()+"-"+point,suit,point)
        
       assertEquals(54, cardPack.size);
    }
}