package dao

import com.google.inject.ImplementedBy
import javax.inject.Inject
import models.{AlbumSong, Albums, Songs}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


trait AlbumSongComponent
  extends AlbumComponent
    with SongComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  val albums = TableQuery[AlbumTable]
  val songs = TableQuery[SongTable]

  class AlbumSongTable(tag: Tag) extends Table[AlbumSong](tag, "album_song") {
    def albumID = column[Int]("albumID")
    def songID = column[Int]("songID")
    def albumFK = foreignKey("album_fk", albumID, albums)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
    def songFK = foreignKey("song_fk", songID, songs)(_.songId, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
    def pk = primaryKey("pk", (albumID, songID))

    def * = (albumID, songID) <> ((AlbumSong.apply _).tupled, AlbumSong.unapply)
  }
}

@ImplementedBy(classOf[AlbumSongImpl])
trait AlbumSongDAO {
  def normalized(albumID: Int, songID: Int): Future[Unit]
  def findAll(): Future[Seq[(Albums, Seq[Songs])]]
}

class AlbumSongImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with AlbumSongComponent with AlbumSongDAO {
  import profile.api._

  private val albumSongs = TableQuery[AlbumSongTable]

  def normalized(albumID: Int, songID: Int): Future[Unit] = dbConfig.db.run{
    (albumSongs += AlbumSong(albumID, songID)).map(_ => ())
  }

  def findAll(): Future[Seq[(Albums, Seq[Songs])]] = {
    val query = albums
      .join(albumSongs).on(_.id === _.albumID)
      .join(songs).on(_._2.songID === _.songId)

    dbConfig.db.run(query.result).map { alb =>
      alb.groupBy(_._1._1.id).map { case (_, value) =>
        val ((album, albumSong), _) = value.head
        val songs = value.map(_._2)
        (album, songs)
      }.toSeq
    }
  }

}
