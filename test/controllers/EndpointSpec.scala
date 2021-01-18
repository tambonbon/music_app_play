package controllers

import dao.{AlbumDAOImpl, AlbumSongImpl, PlayingDAOImpl, SongDAOImpl, UserDAO}
import play.api.test.Helpers

class EndpointSpec extends UnitTest ("Endpoints") {
  val controllerComponents = Helpers.stubControllerComponents()
  val userDAO = mock[UserDAO]
  val albumDAO = mock[AlbumDAOImpl]
  val songDAO = mock[SongDAOImpl]
  val albumSongDAO = mock[AlbumSongImpl]
  val playingDAO = mock[PlayingDAOImpl]
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  val controller = new HomeController(controllerComponents, userDAO, albumDAO, songDAO, albumSongDAO, playingDAO)

  it must {

  }
}
