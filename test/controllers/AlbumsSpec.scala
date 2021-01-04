package controllers

import basicauth.AuthenticateAction
import dao.{AlbumDAOImpl, AlbumSongImpl, PlayingDAOImpl, SongDAOImpl, UserDAO}
import play.api.test.Helpers

class AlbumsSpec  extends UnitTest ("Albums") {
  it must {
    val controllerComponents = Helpers.stubMessagesControllerComponents()
    val userDAO = mock[UserDAO]
    val authenticateAction = mock[AuthenticateAction]
    val albumDAO = mock[AlbumDAOImpl]
    val songDAO = mock[SongDAOImpl]
    val albumSongDAO = mock[AlbumSongImpl]
    val playingDAO = mock[PlayingDAOImpl]
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    val controller = new HomeController(controllerComponents, userDAO, authenticateAction, albumDAO, songDAO, albumSongDAO, playingDAO)

    "add albums from the form" in {

    }
  }
}
