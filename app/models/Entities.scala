package models

import play.api.libs.json.Json

case class Credentials(user: User, password: Password)
case class User(value: String) extends AnyVal
case class Password(value: String) extends AnyVal

object User {
  implicit val format = Json.format[User]
}
object Password {
  implicit val format = Json.format[Password]
}
object Credentials {
  implicit val format = Json.format[Credentials]
}
