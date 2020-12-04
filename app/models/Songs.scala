package models

import play.api.libs.json.Json

case class Songs(name: String, duration: String)

object Songs {
  implicit val songsFormat = Json.format[Songs]
}
