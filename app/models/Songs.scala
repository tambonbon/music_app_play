package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, Writes}

case class Songs(title: String, duration: String)

object Songs {
  implicit val songsRead: Reads[Songs] = (
    (JsPath \ "title").read[String] and
      (JsPath \ "duration").read[String]
    )(Songs.apply _)

  implicit val songsWrite: Writes[Songs] = (
    (JsPath \ "title").write[String] and
      (JsPath \ "duration").write[String]
    )(unlift(Songs.unapply))
}
