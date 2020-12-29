package models

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Reads, Writes, __}
import slick.jdbc.GetResult

case class NumbersPlaying(artist: String, song: String, number: Int)

object NumbersPlaying {
  implicit val numbersPlayingWrites: Writes[NumbersPlaying] = (
    (__ \ "artist").write[String] and
      (__ \ "song").write[String] and
      (__ \ "numbers of playing").write[Int]
    )(unlift(NumbersPlaying.unapply))
  implicit val numbersPlayingReads: Reads[NumbersPlaying] = (
    (__ \ "artist").read[String] and
    (__ \ "song").read[String] and
    (__ \ "numbers of playing").read[Int]
  )(NumbersPlaying.apply _)
  implicit val getResult = GetResult(r => NumbersPlaying(r.<<, r.<<, r.<<))
}
