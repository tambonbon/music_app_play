package controllers

import java.time.LocalDateTime

import basicauth.AuthenticateAction
import dao.{AlbumDAO, SessionDAO, UserDAO}
import javax.inject._
import models.{Albums, Songs, User}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


class HomeController @Inject()(cc: MessagesControllerComponents,
                              userDAO: UserDAO,
                              authenticateAction: AuthenticateAction,
                              albumDAO: AlbumDAO)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) with Logging {


  /**
   * Retreive user from request from in-memory Map of users
   * */
  private def retreiveUser(request: RequestHeader): Option[User] = {
    val tokenOpt = request.session.get("sessionToken") // get cookies to keep token for sessions
      (tokenOpt
      .flatMap(token => SessionDAO.getToken(token))
      .filter(_.expiration.isAfter(LocalDateTime.now()))
      .map(_.username)
      .flatMap(userDAO.getUser)
      )
  }
  /*
  * Check if login is valid
  * */
  private def isValidLogin(username: String, password: String): Boolean = {
    userDAO.getUser(username).exists(_.password == password)
  }

  /************
  * Actions
  ************* */
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

//  def loginBA(username: String, password: String) = authenticateAction { implicit request =>
//    privateRequest()
//    if (isValidLogin(username, password)) {
//      // Here it should redirect to where you want
//      Redirect(routes.HomeController.index())
//    } else {
//      Unauthorized(views.html.defaultpages.unauthorized())
//    }
//  }

  def login(username: String, password: String) = Action { implicit request: Request[AnyContent] =>
    if (isValidLogin(username, password)) {
      val token = SessionDAO.generateToken(username)
      // Here it should redirect to where you want
      Redirect(routes.HomeController.index()).withSession(request.session + ("Username" -> token))
    } else {
      Unauthorized(views.html.defaultpages.unauthorized()).withNewSession
    }
  }

  private def withUser[A](block: User => Result)(implicit request: Request[AnyContent]): Result = {
    val user = retreiveUser(request)
    user.map(block)
      .getOrElse(Unauthorized(views.html.defaultpages.unauthorized()))
  }

//  def priv() = Action { implicit request: Request[AnyContent] =>
//    withUser(user => Ok(views.html.prive(user)))
//  }
//
//  def privateRequest() = authenticateAction { authRequest: AuthenticateRequest[AnyContent] =>
//    Ok(views.html.prive(authRequest.user.get)) // will put endpoint here
//  }
//TODO Add authenticate again:
// - Actions
// - Forms
  /*
 * Check authentication for simple auth
 * */
  def authenticateRequest(username: String, password: String)(func: User => Result) = Action {
    val user: Option[User] = userDAO.getUser(username).filter(_.checkPassword(password))
    user match {
      case Some(value) => func(value)
      case None        => Unauthorized("You are not authorised")
    }
  }
  // login for simple auth
  def loginAuth(username: String, password: String) = authenticateRequest(username, password) { user:User => Ok(s"hello ${user.username}") }

  /**
   * Actions for users
   * */

  def addAlbumForm() = Action { implicit request =>
    Ok(views.html.albumForm(Albums.albumsForm))
  }
  def addAlbum() = {
    Action.async { implicit request =>
      Albums.albumsForm.bindFromRequest.fold(
        errorForm => {
          logger.warn(s"Form submission with error: ${errorForm.errors}")
          Future.successful(Ok(views.html.albumForm(errorForm)))
        },
        data => {
          albumDAO.addAlbum(data.artist, data.name, data.genre)
          .map(_ => Redirect(routes.HomeController.index()).flashing("success" -> "album.created"))
        }
      )
    }
  }

  def addSongForm() = Action { implicit request =>
    Ok(views.html.songForm(Songs.songsForm))
  }
  def addSong() = {
    Action.async { implicit request =>
      Songs.songsForm.bindFromRequest.fold(
        errorForm => {
          logger.warn(s"Form submission with error: ${errorForm.errors}")
          Future.successful(Ok(views.html.songForm(errorForm)))
        },
        data => {
          albumDAO.addSong(data.title, data.duration)
            .map(_ => Redirect(routes.HomeController.index()).flashing("success" -> "songs.created"))
        }
      )
    }
  }


  def getAlbums() = Action.async { implicit request =>
    albumDAO.allAlbums().map (alb => Ok(Json.toJson(alb)))
//    albumDAO.allSongs().map (alb => Ok(Json.toJson(alb)))
  }
  def getSongs() = Action.async { implicit request =>
    albumDAO.allSongs().map (alb => Ok(Json.toJson(alb)))
  }
}

case class CreateLoginForm(username: String, password: String)
case class CreateAlbumForm(artist: String, name: String, genre: String )
case class CreateSongForm(title: String, duration: String )