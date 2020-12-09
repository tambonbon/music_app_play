package models

import controllers.CreateSongForm
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json

case class Songs(id: Int, title: String, duration: String)

object Songs {
  implicit val songsFormat = Json.format[Songs]

  val songsForm: Form[CreateSongForm] = Form (
    mapping(
      "title" -> text,
      "duration" -> text,
    )(CreateSongForm.apply)(CreateSongForm.unapply)
  )
}
