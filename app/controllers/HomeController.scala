package controllers

import dao.UsersDAO
import javax.inject._
import models.AuthenticateAction
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: MessagesControllerComponents,
                              users: UsersDAO,
                              auth: AuthenticateAction)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

//  def check(username: String, password: String): Boolean = {
//    (username == "werner" && password == "1234")
//    (username == "daniel" && password == "1234")
//    (username == "tammy" && password == "1234")
//  }
//
//  val loginForm: Form[CreateLoginForm] = Form (
//    tuple(
//      "username" -> text,
//      "password" -> text
//    )
//    verifying ("Unrecognised username or password", result => result match {
//      case (username, password) => check(username, password)
//    })
//  )
//  def authenticate = Action {implicit request =>
//    loginForm.bindFromRequest.fold(
//      formWithErrors => BadRequest(views.html.login(formWithErrors)),
//      user => Redirect(routes.HomeController.index()).withSession(Security.userinfo -> user._1)
//    )
//  }
//  def index() = Action { implicit request: Request[AnyContent] =>
//    Ok(views.html.index())
//  }

//  def login()
}

case class CreateLoginForm(username: String, password: String)