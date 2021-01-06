package controllers.security

import javax.inject.Inject
import org.apache.commons.codec.binary.Base64.decodeBase64
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class BasicAuthAction @Inject()(username: List[String], password: List[String])(val cc: ControllerComponents)
  extends ActionBuilder[Request, AnyContent] with ActionFilter[Request]  {
  override protected def executionContext: ExecutionContext = cc.executionContext
  override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

  private val unauthorized =
    Results.Unauthorized.withHeaders("WWW-Authenticate" -> "Basic realm=Unauthorized")

  def filter[A](request: Request[A]): Future[Option[Result]] = {
    val result = request.headers.get("Authorization") map { authHeader =>
      val (user, pass) = decodeBasicAuth(authHeader)
      if (username.contains(user) && password.contains(pass)) None else Some(unauthorized)
    } getOrElse Some(unauthorized)

    Future.successful(result)
  }

  private def decodeBasicAuth(authHeader: String): (String, String) = {
    val baString = authHeader.replaceFirst("Basic ", "")
    val Array(user, password) = new String(decodeBase64(baString)).split(":")
    (user, password)
  }

  def getUser[A](request: Request[A]): Option[String] = {
    val result = request.headers.get("Authorization") map { authHeader =>
      val (user, pass) = decodeBasicAuth(authHeader)
//      if (username.contains(user) && password.contains(pass)) None else Some(unauthorized)
      user
    }
    result
  }
}
