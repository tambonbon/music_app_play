package dao

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

import scala.collection.mutable

case class Session(token: String, username: String, expiration: LocalDateTime)

object SessionDAO  {

  private val tokens = mutable.Map.empty[String, Session]

  def getToken(token: String): Option[Session] = {
    tokens.get(token)
  }

  def generateToken(username: String): String = {
    val token = s"$username-token-${UUID.randomUUID().toString}"
    tokens.put(token, Session(token, username, LocalDateTime.now(ZoneOffset.UTC).plusSeconds(30)))

    token
  }
}
