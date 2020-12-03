package models

import javax.inject.Inject
import play.api.mvc.{ActionBuilderImpl, BodyParsers}

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import play.api.mvc.Results._

class AuthenticateAction @Inject() (parser: BodyParsers.Default)(implicit ec: ExecutionContext)
  extends ActionBuilderImpl(parser) {

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
  }
}
