package dao

import javax.inject.Inject
import models.{Albums, Songs}
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
    def id = column[Int]("id", O.PrimaryKey)
    def title = column[String]("title")
    def duration = column[String]("duration")

    def * = (id, title, duration) <> ((Songs.apply _).tupled, Songs.unapply)
  }
  private val songs  = TableQuery[SongTable]

  val thriller = Albums(1, "MJ", "Thriller", "Pop")
  val thrillerSongs = Songs(1, "Thriller", "03:20")

  val setup = DBIO.seq(
      (albums.schema ++ songs.schema).create,

      albums += thriller,
      songs  += thrillerSongs
    )

  val setupDB = db.run(setup)
  /*
  * List all of the albums in the database
  * */
  def all(): Future[Seq[Albums]] = dbConfig.db.run {
    albums.result
  }

  def allSongs(): Future[Seq[Songs]] = dbConfig.db.run{
    songs.result
  }

  def add(artist: String, name: String, genre: String): Future[Albums] = dbConfig.db.run{
    (albums.map(alb => (alb.artist, alb.name, alb.genre))
      returning albums.map(_.id)
      into ((theRest, id) => Albums(id, theRest._1, theRest._2, theRest._3))
      ) += (artist, name, genre)
//    (albums += album).map(result => "Album successfully added")
  }

  def get(artist: String, name: String): Future[Option[Albums]] =  dbConfig.db.run {
    albums.filter(alb => alb.artist === artist && alb.name === name).result.headOption
  }
}
