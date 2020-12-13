package dao

import javax.inject.Inject
import models.{AlbumSong, Albums, Songs}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class AlbumDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext){

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private class AlbumTable(tag: Tag) extends Table[Albums](tag, "albums"){
    def id = column[Int]("id", O.PrimaryKey)
    def artist = column[String]("artist")
    def name = column[String]("name")
    def genre = column[String]("genre")

    def * = (id, artist, name, genre) <> ((Albums.apply _).tupled, Albums.unapply)
  }
  private val albums = TableQuery[AlbumTable]

  private class SongTable(tag: Tag) extends Table[Songs](tag, "songs"){
    def songId = column[Int]("songId", O.PrimaryKey)
    def title = column[String]("title")
    def duration = column[String]("duration")

    def * = (songId, title, duration) <> ((Songs.apply _).tupled, Songs.unapply)
  }
  private val songs  = TableQuery[SongTable]

  private class AlbumSongTable(tag: Tag) extends Table[AlbumSong](tag, "album_song") {
    def albumID = column[Int]("albumID")
    def songID = column[Int]("songID")
    def albumFK = foreignKey("album_fk", albumID, albums)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
    def songFK = foreignKey("song_fk", songID, songs)(_.songId, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Restrict)
    def pk = primaryKey("pk", (albumID, songID))

    def * = (albumID, songID) <> ((AlbumSong.apply _).tupled, AlbumSong.unapply)
  }
  private val albumSongs = TableQuery[AlbumSongTable]

//  val thriller = Albums(1, "MJ", "Thriller", "Pop")
//  val thrillerSongs = Songs(1, "Thriller", "03:20")
//
//  val setup = DBIO.seq(
//      (albums.schema ++ songs.schema).create,
//
//      albums += thriller,
//      songs  += thrillerSongs
//    )
//
//  val setupDB = db.run(setup)
  /*
  * List all of the albums in the database
  * */
  def allAlbums(): Future[Seq[Albums]] = dbConfig.db.run {
    albums.result
  }

  def allSongs(): Future[Seq[Songs]] = dbConfig.db.run{
    songs.result
  }

  def addAlbum(artist: String, name: String, genre: String): Future[Albums] = dbConfig.db.run{
    (albums.map(alb => (alb.artist, alb.name, alb.genre))
      returning albums.map(_.id)
      into ((theRest, id) => Albums(id, theRest._1, theRest._2, theRest._3))
      ) += (artist, name, genre)
  }
  def addSong( title: String, duration: String): Future[Songs] = dbConfig.db.run{
    (songs.map(sng => (sng.title, sng.duration))
      returning songs.map(_.songId)
      into ((theRest, id) => Songs(id, theRest._1, theRest._2))
      ) += (title, duration)
  }

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


  def get(artist: String, name: String): Future[Option[Albums]] =  dbConfig.db.run {
    albums.filter(alb => alb.artist === artist && alb.name === name).result.headOption
  }

  def getMostRecentSong: Future[Int] = dbConfig.db.run {
    songs.sortBy(_.songId.desc).take(1).map(_.songId).result.head
  }
}
