/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.vatsignupfrontend.config.filters

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results._
import play.api.mvc.{Action, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, UnplannedShutter}
import uk.gov.hmrc.vatsignupfrontend.views.html.unplanned_outage
import uk.gov.hmrc.whitelist.AkamaiWhitelistFilter

class UnplannedOutageFilterSpec extends UnitSpec with GuiceOneServerPerSuite with FeatureSwitching with BeforeAndAfterEach with I18nSupport {

  val testString = "success"

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .bindings(bind(classOf[AkamaiWhitelistFilter]).to(classOf[WhiteListFilter]))
    .routes({
      case ("GET", "/vat-through-software/sign-up/index") => Action(Ok(testString))
      case ("GET", "/index") => Action(Ok(testString))
      case _ => Action(Ok("failure"))
    }).build()

  implicit lazy val appConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val request = FakeRequest("GET", "/vat-through-software/sign-up/index")

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UnplannedShutter)
  }

  override def afterEach(): Unit = {
    super.beforeEach()
    disable(UnplannedShutter)
  }

  "UnplannedOutageFilter" when {
    "unplanned shutter is false" should {
      "permit traffic" in {
        disable(UnplannedShutter)

        Call(request.method, request.uri)

        val Some(result) = route(app, request)

        status(result) shouldBe OK
        contentAsString(result) shouldBe testString
      }
    }
    "unplanned shutter is true" should {
      "permit traffic if the path doesn't contain /vat-through-software/sign-up" in {
        enable(UnplannedShutter)

        val Some(result) = route(app, FakeRequest("GET", "/index"))

        status(result) shouldBe OK
        contentAsString(result) shouldBe testString
      }

      "display the unplanned outage page otherwise" in {
        enable(UnplannedShutter)

        val Some(result) = route(app, request)

        status(result) shouldBe OK
        contentAsString(result) shouldBe unplanned_outage(isAgent = false).toString()
      }
    }
  }

}
