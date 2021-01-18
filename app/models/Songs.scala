package models

import java.time.LocalTime

import controllers.CreateSongForm
import play.api.data.Form
import play.api.data.Forms.{localTime, mapping, text}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Json, Reads, Writes, __}

case class Songs(id: Int, title: String, duration: LocalTime)

object Songs {
  implicit val songWrites: Writes[Songs] = Writes { song =>
    Json.obj(
      "name" -> song.title,
      "duration" -> song.duration
    )
  }

  implicit val songReads: Reads[Songs] =  (
    (__ \ "id").read[Int] and
      (__ \ "title").read[String] and
      (__ \ "duration").read[LocalTime]
    )(Songs.apply _)

  val songsForm: Form[CreateSongForm] = Form (
    mapping(
      "title" -> text,
      "duration" -> localTime,
    )(CreateSongForm.apply)(CreateSongForm.unapply)
  )
}
case class SongNoID(name: String, duration: LocalTime)
object SongNoID {
  implicit val songNoIDFormat = Json.format[SongNoID]
}
case class FullAlbum(artist: String, name: String, genre: String, songs: Seq[SongNoID])

object FullAlbum {
  implicit val fullAlbumWrite: Writes[FullAlbum] =
  (
    (__ \ "artist").write[String] and
      (__ \ "name").write[String] and
      (__ \ "genre").write[String] and
      (__ \ "songs").write[Seq[SongNoID]]
    )(unlift(FullAlbum.unapply _)
  )

  implicit val fullAlbumRead: Reads[FullAlbum] =
     (
      (__ \ "artist").read[String] and
      (__ \ "name").read[String] and
      (__ \ "genre").read[String] and
      (__ \ "songs").read[Seq[SongNoID]]
      )(FullAlbum.apply _)
}