package dao

import java.time.LocalTime

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import models.Songs
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait SongComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class SongTable(tag: Tag) extends Table[Songs](tag, "songs"){
    def songId = column[Int]("songId", O.PrimaryKey)
    def title = column[String]("title")
    def duration = column[LocalTime]("duration")

    def * = (songId, title, duration) <> ((Songs.apply _).tupled, Songs.unapply)
  }
}

@ImplementedBy(classOf[SongDAOImpl])
trait SongDAO {
  def allSongs(): Future[Seq[Songs]]
  def addSong( title: String, duration: LocalTime): Future[Songs]
  def getMostRecentSong: Future[Int]
}

@Singleton
class SongDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with SongComponent with SongDAO  {
  import profile.api._

  private val songs  = TableQuery[SongTable]

  def allSongs(): Future[Seq[Songs]] = dbConfig.db.run{
    songs.result
  }
  def addSong( title: String, duration: LocalTime): Future[Songs] = dbConfig.db.run{
    (songs.map(sng => (sng.title, sng.duration))
      returning songs.map(_.songId)
      into ((theRest, id) => Songs(id, theRest._1, theRest._2))
      ) += (title, duration)
  }
  def getMostRecentSong: Future[Int] = dbConfig.db.run {
    songs.sortBy(_.songId.desc).take(1).map(_.songId).result.head
  }
}
