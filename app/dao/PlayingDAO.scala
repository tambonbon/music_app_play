package dao

import java.time.LocalTime

import com.google.inject.ImplementedBy
import javax.inject.Inject
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

trait PlayingComponent extends AlbumComponent with SongComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  val albums = TableQuery[AlbumTable]
  val songs = TableQuery[SongTable]
  val playings = TableQuery[PlayingTable]

  class PlayingTable(tag: Tag) extends Table[Playing](tag, "playing") {
    def playingId = column[Int]("playingId", O.PrimaryKey)
    def artist = column[String]("artist")
    def song = column[String]("song")
    def user = column[String]("user")

    def * = (playingId, artist, song, user) <> ((Playing.apply _).tupled, Playing.unapply)
  }

  class PlayingSongTable(tag: Tag) extends Table[PlayingSong](tag, "playing_song") {
    def playingId = column[Int]("playingId")
    def songId = column[Int]("songId")
    def albumId = column[Int]("albumId")
    def playingFK = foreignKey("playing_fk", playingId, playings)(_.playingId, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
    def songFK = foreignKey("song_fk", songId, songs)(_.songId, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
    def albumFK = foreignKey("album_fk", albumId, albums)(_.id,onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)

    def * = (albumId, songId, playingId) <> ((PlayingSong.apply _).tupled, PlayingSong.unapply)

  }
}

@ImplementedBy(classOf[PlayingDAOImpl])
trait PlayingDAO {
  def addPlaying(artist: String, title: String, user: String): Future[Playing]
  def allPlaying(): Future[Seq[Playing]]
}

class PlayingDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with PlayingComponent with PlayingDAO {
  import profile.api._

  override val playings = TableQuery[PlayingTable]
  private val playingSongs = TableQuery[PlayingSongTable]

  def addPlaying(artist: String, title: String, user: String): Future[Playing] = dbConfig.db.run {
    (playings.map(plg => (plg.artist, plg.song, plg.user))
      returning playings.map(_.playingId)
      into ((theRest, id) => Playing(id, theRest._1, theRest._2, theRest._3))
      ) += (artist, title, user)
  }

  def allPlaying(): Future[Seq[Playing]] = dbConfig.db.run {
    playings.result
  }

  def normalized(albumID: Int, songID: Int, playingID: Int): Future[Unit] = dbConfig.db.run{
    (playingSongs += PlayingSong(albumID, songID, playingID)).map(_ => ())
  }

  def getMostRecentPlaying: Future[Int] = dbConfig.db.run {
    playings.sortBy(_.playingId.desc).take(1).map(_.playingId).result.head
  }

  // TODO: filter to current user only
  def timeListened(): Future[Seq[(String, LocalTime)]] = {
    val query = playings
      .join(playingSongs).on(_.playingId ===_.playingId )
      .join(albums).on(_._2.albumId === _.id)
      .join(songs).on(_._1._2.songId === _.songId)

    dbConfig.db.run(query.result).map { alb =>
      alb.groupBy(_._1._2.genre).map { case (str, value) =>
        val duration = value.map(_._2.duration)
        val a = duration.reduce( (p,q) =>
          p.plusHours(q.getHour).plusMinutes(q.getMinute).plusSeconds(q.getSecond)
        )
        (str, a)
      }.toSeq
    }
  }

  // TODO: The following queries are plain sql
  //  - It is recommended to revert back to slick dsl

  def top5Personal(user: String): Future[Vector[NumbersPlaying]] =  {
    val query =
      sql"""SELECT p."artist", "title", COUNT(*)
     FROM playing_song ps
     JOIN albums
     ON "albumId" = "id"
     JOIN songs s
     ON ps."songId" = s."songId"
     JOIN playing p
     ON p."playingId" = ps."playingId"
     GROUP BY "albumId", "title", p."artist", p."user"
     HAVING COUNT(*) >= 1 AND p."user" = $user
     ORDER BY COUNT(*) DESC""".as[NumbersPlaying]

    dbConfig.db.run(query).map(_.take(5))
  }

  def top5All(): Future[Vector[NumbersPlaying]] =  {
    val query =
sql"""SELECT "artist", "title", COUNT(*)
     FROM playing_song ps
     JOIN albums
     ON "albumId" = "id"
     JOIN songs s
     ON ps."songId" = s."songId"
     GROUP BY "albumId", "title", "artist"
     HAVING COUNT(*) >= 1
     ORDER BY COUNT(*) DESC""".as[NumbersPlaying]

    dbConfig.db.run(query).map(_.take(5))
  }
}
