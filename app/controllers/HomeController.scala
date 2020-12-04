package controllers

import java.time.LocalDateTime

import dao.{SessionDAO, UserDAO}
import javax.inject._
import models.{AuthenticateAction, AuthenticateRequest, User}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject()(cc: MessagesControllerComponents,
                              userDAO: UserDAO,
                              auth: AuthenticateAction,
                              authenticateAction: AuthenticateAction)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  /**
   * Retreive user from request from in-memory Map of users
   * */
  private def retreiveUser(request: RequestHeader): Option[User] = {
    val tokenOpt = request.session.get("sessionToken") // get cookies to keep token for sessions
      (tokenOpt
      .flatMap(token => SessionDAO.getToken(token))
      .filter(_.expiration.isAfter(LocalDateTime.now()))
      .map(_.username)
      .flatMap(userDAO.getUser)
      )
  }
  /*
  * Check if login is valid
  * */
  private def isValidLogin(username: String, password: String): Boolean = {
    userDAO.getUser(username).exists(_.password == password)
  }

  /************
  * Actions
  ************* */
  def index = Action { implicit request =>
    Ok("Hello ")
  }

  def loginBA(username: String, password: String) = authenticateAction { implicit request =>
    privateRequest()
    if (isValidLogin(username, password)) {
      // Here it should redirect to where you want
      Redirect(routes.HomeController.index())
    } else {
      Unauthorized(views.html.defaultpages.unauthorized())
    }
  }

  def login(username: String, password: String) = Action { implicit request: Request[AnyContent] =>
    if (isValidLogin(username, password)) {
      val token = SessionDAO.generateToken(username)
      // Here it should redirect to where you want
      Redirect(routes.HomeController.index()).withSession(request.session + ("token" -> token))
    } else {
      Unauthorized(views.html.defaultpages.unauthorized()).withNewSession
    }
  }

  private def withUser[A](block: User => Result)(implicit request: Request[AnyContent]): Result = {
    val user = retreiveUser(request)
    user.map(block)
      .getOrElse(Unauthorized(views.html.defaultpages.unauthorized()))
  }

  def priv() = Action { implicit request: Request[AnyContent] =>
    withUser(user => Ok(views.html.prive(user)))
  }

  def privateRequest() = auth { authRequest: AuthenticateRequest[AnyContent] =>
    Ok(views.html.prive(authRequest.user.get)) // will put endpoint here
  }



  /*
 * Check authentication for simple auth
 * */
  def authenticateRequest(username: String, password: String)(func: User => Result) = Action {
    val user: Option[User] = userDAO.getUser(username).filter(_.checkPassword(password))
    user match {
      case Some(value) => func(value)
      case None        => Unauthorized("You are not authorised")
    }
  }
  // login for simple auth
  def loginAuth(username: String, password: String) = authenticateRequest(username, password) { user:User => Ok(s"hello ${user.username}") }
}

case class CreateLoginForm(username: String, password: String)