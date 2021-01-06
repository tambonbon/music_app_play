package models

import java.time.LocalTime

import controllers.CreateSongForm
import play.api.data.Form
import play.api.data.Forms.{localTime, mapping, text}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes, __}

case class Songs(id: Int, title: String, duration: LocalTime)

object Songs {
  implicit val songWrites: Writes[Songs] = Writes { song =>
    Json.obj(
      "title" -> song.title,
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
