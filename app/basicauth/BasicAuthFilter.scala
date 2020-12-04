//package basicauth
//
//import java.nio.charset.StandardCharsets
//import java.util.Base64
//
//import javax.inject.Inject
//import play.api.mvc.Results._
//import play.api.mvc.{RequestHeader, Result}
//import play.mvc.Controller
//
//import scala.concurrent.Future
//
//class BasicAuthFilter @Inject()(conf: BasicAuthFilterConfig) extends Controller{
//
//  private val users =
//    conf.userCredentials.map(_.toColonSeparatedString).map(base64Encode)
//
//  private val unauthorized = Future.successful {
//    Unauthorized("Unauthorized").withHeaders("WWW-Authenticate" -> s"""Basic realm="${conf.realm}"""")
//  }
//
//  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] =
//    requestHeader.headers.get("Authorization") match {
//      case Some(authHeader) if validUser(users, authHeader) =>
//        nextFilter(requestHeader)
//      case _ if conf.freePaths.contains(requestHeader.path) => nextFilter(requestHeader)
//      case _                                                => unauthorized
//    }
//
//  private def validUser(users: List[String], authHeader: String) =
//    users.contains(authHeader.stripPrefix("Basic "))
//
//  private def base64Encode(input: String) =
//    Base64.getUrlEncoder.encodeToString(input.getBytes(StandardCharsets.UTF_8))
//}