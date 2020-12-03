package models

import play.api.libs.json.Json

case class Users(username: String, password: String)

object Users {
  implicit val format = Json.format[Users]
}