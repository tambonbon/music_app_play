package models

import java.time.LocalDateTime

import dao.{TokenDAO, UsersDAO}
import javax.inject.Inject
import play.api.mvc.{BodyParsers, _}

import scala.concurrent.{ExecutionContext, Future}

class AuthenticateRequest[A](val users: Option[Users], request: Request[A])
  extends WrappedRequest[A](request)

class AuthenticateAction @Inject() (val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[AuthenticateRequest, AnyContent] // take Request as input, and thus can build actions
    with ActionTransformer[Request, AuthenticateRequest]  { // change the request, for example by adding additional information

  private val logger = play.api.Logger(this.getClass)

  def transform[A](request: Request[A]) = Future.successful {
    logger.info("Calling Action")
    val tokenOpt = request.session.get("Username")
    val user = tokenOpt
      .flatMap(tk => TokenDAO.getToken(tk))
      .filter(_.expiration.isAfter(LocalDateTime.now()))
      .map(_.username)
      .flatMap(UsersDAO.getUser)

    new AuthenticateRequest(user, request)
//    userOpt match {
//      case None => {
//        logger.info("Invalid username")
//        Future.successful(Forbidden("Unsuccessful login"))
//      }
//      case Some(u) => {
//        logger.info("Valid username")
//        val res: Future[Result] = block(request)
//        res
//      }
//    }
  }
}

//https://www.playframework.com/documentation/2.8.x/ScalaActionsComposition