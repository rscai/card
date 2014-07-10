/**
 *
 */
package me.firecloud.gamecenter.dao

/**
 * @author kkppccdd
 *
 */
case class Create(obj:Any)

case class Update(obj:Any)

case class Delete(id:String)

case class Get(id:String)

case class Query(criteria:String)
