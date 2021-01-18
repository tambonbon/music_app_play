package controllers

import dao.AlbumDAOImpl
import models.Albums
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Reads, __}
import play.api.test.Helpers.{GET, defaultAwaitTimeout}
import play.api.test._

import scala.language.postfixOps

class AlbumsSpec  extends UnitTest ("Albums") {
  "Albums page" must {
    implicit val albumReader: Reads[Albums] = (
      (__ \ "id").read[Int] and
        (__ \ "artist").read[String] and
        (__ \ "song").read[String] and
        (__ \ "genre").read[String]
      ) (Albums.apply _)
    "show registered albums" in  {
      val controller = inject[HomeController]
      val all_albums = controller.index().apply(FakeRequest(GET, "/all-albums"))
      val album = mock[AlbumDAOImpl]

      Helpers.contentAsJson(all_albums) mustBe()
    }
  }
}
