package models

import java.time.LocalDateTime

import dao.{SessionDAO, UsersDAO}
import javax.inject.Inject
import play.api.mvc.Results.Forbidden
import play.api.mvc.{BodyParsers, _}

import scala.concurrent.{ExecutionContext, Future}

class AuthenticateRequest[A](val users: Option[Users], request: Request[A])
  extends WrappedRequest[A](request)

class AuthenticateAction @Inject() (val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[AuthenticateRequest, AnyContent] // take Request as input, and thus can build actions
    with ActionTransformer[Request, AuthenticateRequest]  { // change the request, for example by adding additional information

  private val logger = play.api.Logger(this.getClass)
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    logger.debug("ENTERED AuthenticatedUserAction::invokeBlock")
    val maybeUsername = request.session.get("USERNAME")
    maybeUsername match {
      case None => {
        logger.debug("CAME INTO 'NONE'")
        Future.successful(Forbidden("Dude, you’re not logged in."))
      }
      case Some(u) => {
        logger.debug("CAME INTO 'SOME'")
        val res: Future[Result] = block(request)
        res
      }
    }
//  def transform[A](request: Request[A]) = Future.successful {
//    logger.info("Calling Action")
//    val tokenOpt = request.session.get("Username")
//    val user = tokenOpt
//      .flatMap(tk => SessionDAO.getToken(tk))
//      .filter(_.expiration.isAfter(LocalDateTime.now()))
//      .map(_.username)
//      .flatMap(UsersDAO.getUser)
//
//    new AuthenticateRequest(user, request)
//  }
}
  }

//https://www.playframework.com/documentation/2.8.x/ScalaActionsComposition