package controllers

import java.time.LocalTime

import basicauth.{AuthenticateAction, AuthenticateRequest}
import dao._
import javax.inject._
import models.{Albums, Playing, Songs, User}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.duration.DurationDouble
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

class HomeController @Inject()(cc: MessagesControllerComponents,
                              userDAO: UserDAO,
                              authenticateAction: AuthenticateAction,
                              albumDAO: AlbumDAOImpl,
                              songDAO: SongDAOImpl,
                              albumSongDAO: AlbumSongImpl,
                              playingDAO: PlayingDAOImpl)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) with Logging {

  /************
  * Actions
  ************* */
  private val WithBasicAuth = new BasicAuthAction(userDAO.getAllUser()._1, userDAO.getAllUser()._2)(cc)

  def index = WithBasicAuth {
    Ok("Correct basic auth credentials")
  }

  def login() = {
    Action { implicit request =>
      User.loginForm.bindFromRequest.fold(
        errorForm => {
          logger.warn(s"Form submission with error: ${errorForm.errors}")
          (Ok(views.html.index(errorForm)))
        },
        data => {
          if (isValidLogin(data.username, data.password)) {
            val token = SessionDAO.generateToken(data.username)
            // Here it should redirect to where you want
            Redirect(routes.HomeController.privateRequest()).withSession(request.session + ("Username" -> token))
          } else {
            Unauthorized(views.html.defaultpages.unauthorized()).withNewSession
          }
        }
      )
    }
  }


  /*
* Check if login is valid
* */
  private def isValidLogin(username: String, password: String): Boolean = {
    userDAO.getUser(username).exists(_.password == password)
  }

  /**
   * Retreive user from request from in-memory Map of users
   * */

  def privateRequest() = authenticateAction { implicit authRequest: AuthenticateRequest[AnyContent] =>
    authRequest
      .user
      .map(user => Ok(views.html.prive(user)))
      .getOrElse(Unauthorized(views.html.defaultpages.unauthorized()))
//    Ok(views.html.prive(authRequest.user.get)) // will put endpoint here
  }

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
          .map(_ => Redirect(routes.HomeController.addSong()).flashing("success" -> "album.created"))
        }
      )
    }
  }

  def addSongForm() = Action { implicit request =>
    Ok(views.html.songForm(Songs.songsForm))
  }
  def addSong = {
    Action.async { implicit request =>
      Songs.songsForm.bindFromRequest.fold(
        errorForm => {
          logger.warn(s"Form submission with error: ${errorForm.errors}")
          Future.successful(Ok(views.html.songForm(errorForm)))
        },
        data => {
          songDAO.addSong(data.title, data.duration)
          albumSongDAO.normalized(Await.result(albumDAO.getMostRecentAlbum, 0.1 seconds),
            Await.result(songDAO.getMostRecentSong ,0.1 seconds)) // THIS IS NOT RECOMMENDED
            .map(_ => Redirect(routes.HomeController.addSong()).flashing("success" -> "songs.created"))
        }
      )
    }
  }

  //TODO implement basic auth
  // - make private db/space for each user

  def all_albums() = Action.async { implicit request =>
    albumSongDAO.findAll().map(alb => Ok(Json.toJson(alb)))
  }

//  def artistAndSong() = Action.async{ implicit request =>
//    albumSongDAO.artistAndSong().map(play => Ok(Json.toJson(play)))
//  }

  def playingForm() = Action {implicit request =>
    Ok(views.html.playingForm(Playing.playingForm, albumSongDAO))
  }
  def playing() = Action.async{ implicit request =>
    Playing.playingForm.bindFromRequest.fold(
      errorForm => {
        logger.warn(s"Form submission with error: ${errorForm.errors}")
        Future.successful(Ok(views.html.playingForm(errorForm, albumSongDAO)))
      },
      data => {
        playingDAO.addPlaying(data.artist, data.song)
        playingDAO.normalized(Await.result(albumDAO.getAlbumIdFromArtist(data.artist), 0.1 seconds),
                              Await.result(songDAO.getSongIdFromSong(data.song), 0.1 seconds),
                              Await.result(playingDAO.getMostRecentPlaying       , 0.1 seconds))
                                .map(_ => Redirect(routes.HomeController.playing()).flashing("success" -> "songs.played"))
      }
    )
  }

  // TODO: implement playing songs
  //  - make songs mapping with artist accordingly

  def timeListened() = Action.async { implicit request =>
    playingDAO.timeListened().map(time => Ok(Json.toJson(time)))
  }

  def numbersOfPlaying = Action.async { implicit request =>
    playingDAO.numbersOfPlaying().map(times => Ok(Json.toJson(times)))
  }

  def getAlbums() = Action.async { implicit request =>
    albumDAO.allAlbums().map (alb => Ok(Json.toJson(alb)))
  }

  def getSongs() = Action.async { implicit request =>
    songDAO.allSongs().map (alb => Ok(Json.toJson(alb)))
  }

  def getPlayings() = Action.async { implicit request =>
    playingDAO.allPlaying().map (alb => Ok(Json.toJson(alb)))
  }
}

case class CreateLoginForm(username: String, password: String)
case class CreateAlbumForm(artist: String, name: String, genre: String )
case class CreateSongForm(title: String, duration: LocalTime)
case class CreatePlayingForm(artist: String, song: String)
