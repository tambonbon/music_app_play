package controllers

import controllers.security.BasicAuthAction
import org.apache.commons.codec.binary.Base64.decodeBase64
import play.api.mvc._
import play.api.test.Helpers
class AuthSpec extends UnitTest("Auth") {
  "Basic Authencitation action" must {
    "authenticate a valid user" in { implicit request: Request[AnyContent] =>
      val cc = Helpers.stubControllerComponents()
      val setOfUsers = Set(
        ("Werner", "werner"),
        ("Daniel", "daniel")
      )
      val basic = new BasicAuthAction(setOfUsers)(cc)
      val decoded = new String(decodeBase64("Werner:werner")).split(":")

      val req = Results.Unauthorized.withHeaders("Authorization" -> "decoded")

    }
  }
}
