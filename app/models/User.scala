package models

import controllers.CreateLoginForm
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json

case class User(username: String, password: String) {
  def toColonSeparatedString = s"$username:$password"
  def checkPassword(password: String): Boolean = this.password == password
}

object User {
  implicit val format = Json.format[User]

  val loginForm: Form[CreateLoginForm] = Form (
    mapping(
      "username" -> text,
      "password" -> text
    )(CreateLoginForm.apply)(CreateLoginForm.unapply)
  )
}