package controllers

import java.time.LocalTime

import controllers.security.BasicAuthAction
import dao._
import javax.inject._
import models._
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationDouble
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                              userDAO: UserDAO,
                              albumDAO: AlbumDAOImpl,
                              songDAO: SongDAOImpl,
                              albumSongDAO: AlbumSongImpl,
                              playingDAO: PlayingDAOImpl)
  extends AbstractController(cc) with Logging with play.api.i18n.I18nSupport  {

  /************
  * Actions
  ************* */
  private val WithBasicAuth = new BasicAuthAction(userDAO.users.keys.toList, userDAO.users.values.toList)(cc)

  def index = Action {
    Ok(views.html.index())
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
//  implicit val header = new MessagesRequestHeader
  def playingForm() = WithBasicAuth {implicit request =>
    Ok(views.html.playingForm(Playing.playingForm, albumSongDAO))
  }
  def playing() = Action.async { implicit request =>
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
