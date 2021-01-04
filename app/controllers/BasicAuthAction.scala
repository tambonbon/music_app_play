package controllers

import play.api.mvc._

import scala.concurrent.Future
import org.apache.commons.codec.binary.Base64.decodeBase64

class BasicAuthAction(username: String, password: String) extends ActionBuilder[Request, AnyContent] with ActionFilter[Request] {
  private val unauthorized =
    Results.Unauthorized.withHeaders("WWW-Authenticate" -> "Basic realm=Unauthorized")

  def filter[A](request: Request[A]): Future[Option[Result]] = {
    val result = request.headers.get("Authorization") map { authHeader =>
      val (user, pass) = decodeBasicAuth(authHeader)
      if (user == username && pass == password) None else Some(unauthorized)
    } getOrElse Some(unauthorized)

    Future.successful(result)
  }

  private [this] def decodeBasicAuth(authHeader: String): (String, String) = {
    val baStr = authHeader.replaceFirst("Basic ", "")
//    val decoded = new decodeBase64
    val Array(user, password) = new String(decodeBase64(baStr)).split(":")
    (user, password)
  }
}