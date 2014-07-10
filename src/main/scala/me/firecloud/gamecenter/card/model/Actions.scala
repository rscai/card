/**
 *
 */
package me.firecloud.gamecenter.card.model

import me.firecloud.gamecenter.model.Message

/**
 * @author kkppccdd
 * @email kkppccdd@gmail.com
 * @date Jan 4, 2014
 * requests
 */

object PutCard extends Message("0") {
    def cla: Long = 0x02; //1
    def ins: Long = 0x01; //1
}

case class PutCard(userId: String, cards: List[Card]) extends Message(userId) {
    def cla: Long = PutCard.cla
    def ins: Long = PutCard.ins
}

object Pass extends Message("0") {
    def cla: Long = 0x02; //1
    def ins: Long = 0x02; //1
}
case class Pass(userId: String) extends Message(userId) {
    def cla: Long = Pass.cla
    def ins: Long = Pass.ins
}
object DealCard extends Message("0") {
    def cla: Long = 0x02; //1
    def ins: Long = 0x03; //1
}
case class DealCard(userId: String, toUserId: String, cards: List[Card]) extends Message(userId) {
    def cla: Long = DealCard.cla
    def ins: Long = DealCard.ins
}

object AppendCard extends Message("0") {
    def cla: Long = 0x02; //1
    def ins: Long = 0x04; //1
}

case class AppendCard(userId: String, cards: List[Card]) extends Message(userId) {
    def cla: Long = AppendCard.cla
    def ins: Long = AppendCard.ins
}

object Bet extends Message("0") {
    def cla: Long = 0x02; //1
    def ins: Long = 0x05; //1
}

case class Bet(userId: String, amount: Int) extends Message(userId) {
    def cla: Long = Bet.cla
    def ins: Long = Bet.ins
}