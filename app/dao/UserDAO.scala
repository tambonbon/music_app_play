package dao

import scala.collection.mutable

class UserDAO  {

  private val users = mutable.Map( // should be private
    "Werner" -> "werner",
    "Daniel" -> "daniel"
//    User("Daniel") -> Password("daniel")
  )

  def getUser(username: String): Option[String] = {
    users.keys.find(_.equals(username))
  }
  def findUser(username: String, password: String): Boolean = true
  def setOfUser(): Set[(String, String)] = users.toSet
//  def getAllUser(): Map[User, Password] = {
//    Map(users.values.toSeq -> users.keys)
////    (users.values.map(_.username).toList, users.values.map(_.password).toList)
//  }

//  def addUser(username: String, password: String): Option[User] = {
//    if(users.contains(username)) {
//      Option.empty
//    } else {
//      val user = User(username, password)
//      users.put(username, user)
//      Option(user)
//    }
//  }

}