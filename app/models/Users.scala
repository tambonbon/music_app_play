package models

import play.api.libs.json.Json

case class Users(username: String, password: String) {
  def toColonSeparatedString = s"$name:$password"
}

object Users {
  implicit val format = Json.format[Users]
}