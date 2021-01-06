package models

import controllers.CreatePlayingForm
import play.api.data.Form
import play.api.data.Forms.{ignored, mapping, text}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes, __}

case class Playing(playingId: Int,  artist: String, song: String, user: String)
case class PlayingSong(albumId: Int, songId:Int, playingId: Int) // to normalize database

object Playing  {
  implicit val playingWrite: Writes[Playing] = Writes { play =>
    Json.obj(
      "artist" -> play.artist,
      "song" -> play.song
    )
  }

  implicit val playingRead: Reads[Playing] =  (
    (__ \ "id").read[Int] and
      (__ \ "artist").read[String] and
      (__ \ "song").read[String] and
        (__ \ "user").read[String]
    )(Playing.apply _)

  val playingForm: Form[CreatePlayingForm] = Form (
    mapping(
      "artist" -> text,
      "song" -> text,
      "user" -> ignored(
        "User"
      )
    )(CreatePlayingForm.apply)(CreatePlayingForm.unapply)
  )
}

object PlayingSong {
  implicit val playingSongFormat = Json.format[PlayingSong]
}

