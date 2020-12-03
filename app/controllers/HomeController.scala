package controllers

import java.time.LocalDateTime

import dao.{TokenDAO, UsersDAO}
import javax.inject._
import models.{AuthenticateAction, AuthenticateRequest, Users}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject()(cc: MessagesControllerComponents,
                              auth: AuthenticateAction)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  /**
   * Retreive user from request from in-memory Map of users
   * */
  private def retreiveUser(request: RequestHeader): Option[Users] = {
    val tokenOpt = request.session.get("sessionToken") // get cookies to keep token for sessions
      (tokenOpt
      .flatMap(token => TokenDAO.getToken(token))
      .filter(_.expiration.isAfter(LocalDateTime.now()))
      .map(_.username)
      .flatMap(UsersDAO.getUser)
      )
  }
  /*
  * Check if login is valid
  * */
  private def isValidLogin(username: String, password: String): Boolean = {
    UsersDAO.getUser(username).exists(_.password == password)
  }

  /*
  * Actions
  * */
  def index = Action { implicit request =>
    Ok("Hello ")
  }

  def login(username: String, password: String) = Action { implicit request: Request[AnyContent] =>
    if (isValidLogin(username, password)) {
      val token = TokenDAO.generateToken(username)
      // Here it should redirect to where you want
      Redirect(routes.HomeController.index()).withSession(request.session + ("sessionToken" -> token))
    } else {
      Unauthorized(views.html.defaultpages.unauthorized()).withNewSession
    }
  }

  def privateRequest() = auth { authRequest: AuthenticateRequest[AnyContent] =>
    Ok(views.html.prive(authRequest.users.get)) // will put endpoint here
  }

}

case class CreateLoginForm(username: String, password: String)