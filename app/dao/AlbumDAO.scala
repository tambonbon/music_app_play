package dao

import com.google.inject.ImplementedBy
import javax.inject.{Inject,Singleton}
import models.Albums
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait AlbumComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
//  import dbConfig._
  import profile.api._

  class AlbumTable(tag: Tag) extends Table[Albums](tag, "albums"){
    def id = column[Int]("id", O.PrimaryKey)
    def artist = column[String]("artist")
    def name = column[String]("name")
    def genre = column[String]("genre")

    def * = (id, artist, name, genre) <> ((Albums.apply _).tupled, Albums.unapply)
  }
}

@ImplementedBy(classOf[AlbumDAOImpl])
trait AlbumDAO {
  def allAlbums(): Future[Seq[Albums]]
  def addAlbum(artist: String, name: String, genre: String): Future[Albums]
  def getAlbum(artist: String, name: String): Future[Option[Albums]]
}

@Singleton()
class AlbumDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with  AlbumComponent with AlbumDAO {

//  private val dbConfig = dbConfigProvider.get[JdbcProfile]
//  import dbConfig._
  import profile.api._


  private val albums = TableQuery[AlbumTable]

  /*
  * List all of the albums in the database
  * */
  def allAlbums(): Future[Seq[Albums]] = dbConfig.db.run {
    albums.result
  }



  def addAlbum(artist: String, name: String, genre: String): Future[Albums] = dbConfig.db.run{
    (albums.map(alb => (alb.artist, alb.name, alb.genre))
      returning albums.map(_.id)
      into ((theRest, id) => Albums(id, theRest._1, theRest._2, theRest._3))
      ) += (artist, name, genre)
  }




  def getAlbum(artist: String, name: String): Future[Option[Albums]] =  dbConfig.db.run {
    albums.filter(alb => alb.artist === artist && alb.name === name).result.headOption
  }


}
