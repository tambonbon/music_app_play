package controllers

import java.time.LocalTime

import controllers.security.BasicAuthAction
import dao._
import javax.inject._
import models._
import play.api.Logging
import play.api.libs.json.{JsError, Json}
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
  private val WithBasicAuth = new BasicAuthAction(userDAO.setOfUser())(cc)

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

  //TODO Implement JSON parsed instead of filling in form
// 1st story
  def addAlbumJson = Action.async(parse.json) { request =>
    val response = request.body.validate[FullAlbum]
    response.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      newAlbum => { //TODO this should be NO id
        val m = albumDAO.addAlbum(newAlbum.artist, newAlbum.name, newAlbum.genre)
        for (
          song <- newAlbum.songs
        ) yield {
          val a = songDAO.addSong(song.name, song.duration)
          a
          val b = a.map(_.id)
          albumSongDAO.normalized(Await.result(m.map(_.id), 0.5 seconds), Await.result(b, 0.5 seconds))
        }
      Future.successful(Ok(Json.toJson(newAlbum)))
      }
    )

  }
  def all_albums() = Action.async { implicit request =>
    albumSongDAO.findAll().map(alb => Ok(Json.toJson(alb)))
  }

//  def artistAndSong() = Action.async{ implicit request =>
//    albumSongDAO.artistAndSong().map(play => Ok(Json.toJson(play)))
//  }
//  implicit val header = new MessagesRequestHeader
  def playingForm() = WithBasicAuth {implicit request =>
//  request.
    Ok(views.html.playingForm(WithBasicAuth, Playing.playingForm, albumSongDAO))
  }
  def playing() = Action.async { implicit request =>
    Playing.playingForm.bindFromRequest.fold(
      errorForm => {
        logger.warn(s"Form submission with error: ${errorForm.errors}")
        Future.successful(Ok(views.html.playingForm(WithBasicAuth, errorForm, albumSongDAO)))
      },
      data => {
        playingDAO.addPlaying(data.artist, data.song, WithBasicAuth.getUser(request).get)
        playingDAO.normalized(Await.result(albumDAO.getAlbumIdFromArtist(data.artist), 0.1 seconds),
                              Await.result(songDAO.getSongIdFromSong(data.song), 0.1 seconds),
                              Await.result(playingDAO.getMostRecentPlaying       , 0.1 seconds))
                                .map(_ => Redirect(routes.HomeController.playing()).flashing("success" -> "songs.played"))
      }
    )
  }

//  1st story
  def playingJson() = Action.async(parse.json) { implicit request =>
    val response = request.body.validate[Playing]
    response.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
       },
      data => {
        playingDAO.addPlaying(data.artist, data.song, data.user)
        Future.successful(Ok(Json.toJson(data)))
      }
    )
  }

  def timeListened() = Action.async { implicit request =>
    playingDAO.timeListened(WithBasicAuth.getUser(request).get).map(time => Ok(Json.toJson(time)))
  }

  def top5Personal() = Action.async { implicit request =>
    playingDAO.top5Personal(WithBasicAuth.getUser(request).get).map(times => Ok(Json.toJson(times)))
  }

  def top5All() = Action.async { implicit request =>
    playingDAO.top5All().map(times => Ok(Json.toJson(times)))
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
case class CreatePlayingForm(artist: String, song: String, user: String)
