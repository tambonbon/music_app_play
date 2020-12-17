package models

import java.time.LocalTime

import controllers.CreateSongForm
import play.api.data.Form
import play.api.data.Forms.{localTime, mapping, text}
import play.api.libs.json.Json

case class Songs(id: Int, title: String, duration: LocalTime)

object Songs {
  implicit val songsFormat = Json.format[Songs]

  val songsForm: Form[CreateSongForm] = Form (
    mapping(
      "title" -> text,
      "duration" -> localTime,
    )(CreateSongForm.apply)(CreateSongForm.unapply)
  )
}
