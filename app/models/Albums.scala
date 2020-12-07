package models

import play.api.data.Form
import play.api.data.Forms.{mapping, seq, text}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, Writes}

case class Albums(artist: String, name: String, genre: String, songs: Seq[Songs])

object Albums {
  implicit val albumsReads: Reads[Albums] = (
    (JsPath \ "artist").read[String] and
    (JsPath \ "name").read[String] and
    (JsPath \ "genre").read[String] and
    (JsPath \ "songs").read[Seq[Songs]]
  )(Albums.apply _)

  implicit val albumsWrites: Writes[Albums] = (
    (JsPath \ "artist").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "genre").write[String] and
      (JsPath \ "songs").write[Seq[Songs]]
    )(unlift((Albums.unapply)))

  val albumsForm: Form[Albums] = Form (
    mapping(
      "artist" -> text,
      "name" -> text,
      "genre" -> text,
      "songs" -> seq(
        mapping(
          "title" -> text,
          "duration" -> text
        )(Songs.apply)(Songs.unapply)
       )
    )(Albums.apply)(Albums.unapply)
  )

}