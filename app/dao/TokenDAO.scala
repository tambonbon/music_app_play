package dao

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

import scala.collection.mutable

case class Token(token: String, username: String, expiration: LocalDateTime)

object TokenDAO  {

  private val tokens = mutable.Map.empty[String, Token]

  def getToken(token: String): Option[Token] = {
    tokens.get(token)
  }

  def generateToken(username: String): String = {
    val token = s"$username-token-${UUID.randomUUID().toString}"
    tokens.put(token, Token(token, username, LocalDateTime.now(ZoneOffset.UTC).plusSeconds(30)))

    token
  }
}
