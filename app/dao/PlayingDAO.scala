package dao

import com.google.inject.ImplementedBy
import javax.inject.Inject
import models.{CreatePlayingForm, Playing}
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted
import scala.language.postfixOps
import scala.concurrent.duration.DurationDouble
import scala.concurrent.{Await, ExecutionContext, Future}

trait PlayingComponent extends AlbumComponent with SongComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  val albums = TableQuery[AlbumTable]
  val songs = TableQuery[SongTable]

  class PlayingTable(tag: Tag) extends Table[Playing](tag, "playing") {
    def playingId = column[Int]("playingId", O.PrimaryKey)
    def artist = column[String]("artist")
    def title = column[String]("title")

    def * = (playingId, artist, title) <> ((Playing.apply _).tupled, Playing.unapply)
  }
}

@ImplementedBy(classOf[PlayingDAOImpl])
trait PlayingDAO {
  def addPlaying(artist: String, title: String): Future[Playing]
}

class PlayingDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with PlayingComponent with PlayingDAO {
  import profile.api._

  private val playings = lifted.TableQuery[PlayingTable]

  def validate(artist: String, song: String) = Future.successful {
    if (Await.result(hasArtist(artist), 0.1 seconds)) { // AGAIN THIS IS NOT RECOMMENDED
      if (Await.result(hasSong(song), 0.1 seconds)) {
        Some(CreatePlayingForm(artist, song))
      }
      else None
    }
    else None
  }

  val playingForm: Form[CreatePlayingForm] = Form (
    mapping (
      "artist" -> text,
      "song" -> text// TODO: Add constraint so that it will only accept songs from the database
    )(CreatePlayingForm.apply)(CreatePlayingForm.unapply).verifying(
      fields => fields match {
        case data => validate(data.artist, data.song).isCompleted
      }
    )
  )

  def hasArtist(artist: String): Future[Boolean] = dbConfig.db.run {
    albums.map(art => art.artist === artist).result.head
  }
  def hasSong(song: String): Future[Boolean] = dbConfig.db.run {
    songs.map(sng => sng.title === song).result.head
  }

  // TODO: hasSong is more complicated than this
  //  - it should validate the artist too (a song with same name but different singer)

  def addPlaying(artist: String, title: String): Future[Playing] = dbConfig.db.run {
    (playings.map(plg => (plg.artist, plg.title))
      returning playings.map(_.playingId)
      into ((theRest, id) => Playing(id, theRest._1, theRest._2))
      ) += (artist, title)
  }

}
