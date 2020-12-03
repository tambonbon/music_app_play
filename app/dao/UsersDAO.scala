package dao

import javax.inject.Inject
import models.Users
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class UsersDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private class UsersTable(tag: Tag) extends Table[Users] (tag, "users") {
   def username = column[String]("username", O.PrimaryKey, O.Unique)
   def password = column[String]("password")

   def * = (username, password) <> ((Users.apply _).tupled, Users.unapply)
  }
  private val users = TableQuery[UsersTable]

  def getUser(username: String): Future[Option[Users]] = {
    dbConfig.db.run(users.filter(_.username === username).result.headOption)
  }
  def addUser(username: String, password: String): Future[Users] = dbConfig.db.run {
    (users += Users(username, password)).map(_ => Users(username, password))
  }
}
