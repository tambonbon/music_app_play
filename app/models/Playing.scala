package models

import controllers.CreatePlayingForm
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json

case class Playing(playingId: Int, artist: String, song: String)

object Playing  {
  implicit val playingFormat = Json.format[Playing]

  val playingForm: Form[CreatePlayingForm] = Form (
    mapping(
      "artist" -> text,
      "song" -> text
    )(CreatePlayingForm.apply)(CreatePlayingForm.unapply)
  )
}
