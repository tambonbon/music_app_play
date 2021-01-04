package controllers

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Injecting

abstract class UnitTest (components: String) extends PlaySpec with GuiceOneAppPerTest with Injecting with MockitoSugar{
}
