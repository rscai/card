/**
 *
 */
package me.firecloud.gamecenter.model

import scala.language.dynamics
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
/**
 * @author kkppccdd
 *
 */
class Seat {
  /**
   * FREE,OCCUPIED,READY,ACTIVE,MISSED,TERMINATED
   */
  private var _state: String = "FREE"
  var playerId: String = null // only player id
  var bet: Int = 0
  var role: String = "FARMER"
    

  /**
   * *****************************************
   * properties accessors
   */
  def state: String = _state
  /**
   * *******************************
   * state change functions
   */
  def occupy: String = {
    // check precondition
    if (_state.equals("FREE")) {
      _state = "OCCUPIED"
      return _state
    } else {
      return _state
    }
  }

  def ready: String = {
    // check precondition
    if (_state.equals("OCCUPIED")) {
      _state = "READY"
      return _state
    } else {
      return _state
    }
  }

  def activate: String = {
    // check precondition
    if (_state.equals("READY")) {
      _state = "ACTIVE"
      return _state
    } else {
      return _state
    }
  }

  def miss: String = {
    // check precondition
    if (_state.equals("ACTIVE")) {
      _state = "MISSED"
      return _state
    } else {
      return _state
    }
  }

  def comeBack: String = {
    // check precondition
    if (_state.equals("MISSED")) {
      _state = "ACTIVE"
      return _state
    } else {
      return _state
    }
  }

  override def equals(obj: Any): Boolean = {
    return obj.isInstanceOf[Seat] && obj.asInstanceOf[Seat].playerId == this.playerId
  }


}