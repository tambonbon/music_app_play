//package models
//
//import play.api.libs.json.Json
//
//case class User(username: String, password: String) {
//  def toColonSeparatedString = s"$username:$password"
//  def checkPassword(password: String): Boolean = this.password == password
//}
//
//object User {
//  implicit val format = Json.format[User]
//
//}