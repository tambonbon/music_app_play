package dao

import models.Users

import scala.collection.mutable

object UsersDAO {

  private val users = mutable.Map(
    "Werner" -> Users("Werner", "werner"),
    "Daniel" -> Users("Daniel", "daniel")
  )

  def getUser(username: String): Option[Users] = {
    users.get(username)
  }

  def addUser(username: String, password: String): Option[Users] = {
    if(users.contains(username)) {
      Option.empty
    } else {
      val user = Users(username, password)
      users.put(username, user)
      Option(user)
    }
  }

}