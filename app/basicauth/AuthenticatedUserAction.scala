package basicauth

import javax.inject.Inject
import play.api.mvc.Results.Forbidden
import play.api.mvc.{ActionBuilderImpl, BodyParsers, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedUserAction @Inject() (parser: BodyParsers.Default)(implicit ec: ExecutionContext)
  extends ActionBuilderImpl(parser) {

  private val logger = play.api.Logger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    logger.debug("ENTERED AuthenticatedUserAction::invokeBlock")
    val userOpt = request.session.get("USERNAME")
        userOpt match {
          case None => {
            logger.info("Invalid username")
            Future.successful(Forbidden("Unsuccessful login"))
          }
          case Some(u) => {
            logger.info("Valid username")
            val res: Future[Result] = block(request)
            res
          }
        }
    }

}