package dao

import models.User

import scala.collection.mutable

class UserDAO  {

  private val users = mutable.Map( // should be private
    "Werner" -> User("Werner", "werner"),
    "Daniel" -> User("Daniel", "daniel")
  )

  def getUser(username: String): Option[User] = {
    users.get(username)
  }
  def findUser(username: String, password: String): Boolean = true

  def getUserWOPassword(username: String): String = {
    users(username).username
  }

  def getAllUser(): (List[String], List[String]) = {
    (users.values.map(_.username).toList, users.values.map(_.password).toList)
  }

  def addUser(username: String, password: String): Option[User] = {
    if(users.contains(username)) {
      Option.empty
    } else {
      val user = User(username, password)
      users.put(username, user)
      Option(user)
    }
  }

}