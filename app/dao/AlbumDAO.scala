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
    def artist = column[String]("artist", O.PrimaryKey)
    def name = column[String]("name")
    def genre = column[String]("genre")

    def title = column[String]("title")
    def duration = column[String]("duration")

    def * = (
      artist, name, genre, (
        title, duration
      )
    ).shaped <> (
      { case (artist, name, genre, songs) => Albums(artist, name, genre, (Songs.apply _).tupled.apply(songs)) //Using .tupled method when companion object is in class
      },
      { alb: Albums =>
        def f(songs: Songs) = Songs.unapply(songs).get
        Some((alb.artist, alb.name, alb.genre, f(alb.songs)))
      }
    )
//    def songs = (title.?, duration.?).<>[Seq[Songs], String, String] (
//      { mappedSongs =>
//        mappedSongs match {
//          case (maybeString, maybeString1) => Seq.fill(1)(Songs(maybeString, maybeString1))
//          case _ => Seq.fill(1)(Songs(None,None))
//        }
//      },
//      {result => (result.map(_.name), result.map(_.duration))
//
//      }
//    )
  }

  private val albums = TableQuery[AlbumTable]

  /*
  * List all of the albums in the database
  * */
  def all(): Future[Seq[Albums]] = dbConfig.db.run {
    albums.result
  }

  def add(album: Albums): Future[Unit] = dbConfig.db.run{
    (albums += album).map(_ => ())
  }

  def get(artist: String, name: String): Future[Option[Albums]] =  dbConfig.db.run {
    albums.filter(alb => alb.artist === artist && alb.name === name).result.headOption
  }
}
