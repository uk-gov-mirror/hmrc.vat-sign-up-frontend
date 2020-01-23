/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.Constants.Enrolments.VatDecEnrolmentKey
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.vatNumberKey
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec

class SessionTimeoutControllerSpec extends UnitSpec with GuiceOneAppPerSuite {

  object TestSessionTimeoutController extends SessionTimeoutController(
    app.injector.instanceOf[AppConfig],
    app.injector.instanceOf[Configuration],
    app.injector.instanceOf[Environment],
    app.injector.instanceOf[MessagesControllerComponents])

  "timeout" should {
    "stay on current page with current session" when {
      "the keep alive method is used" in {
        val fakeRequest: Request[AnyContent] = FakeRequest().withSession(vatNumberKey -> VatDecEnrolmentKey)
        val res = TestSessionTimeoutController.keepAlive(fakeRequest)

        session(res).get(SessionKeys.vatNumberKey) shouldBe Some(VatDecEnrolmentKey)

      }
    }

    "redirect a individual user to login with a new session " when {
      "the timeout method is" in {
        val fakeRequest: Request[AnyContent] = FakeRequest().withSession(vatNumberKey -> VatDecEnrolmentKey)
        val res = TestSessionTimeoutController.timeout(fakeRequest)

        session(res).get(SessionKeys.vatNumberKey) shouldBe None
        redirectLocation(res) shouldBe Some("/gg/sign-in?continue=%2Fvat-through-software%2Fsign-up%2Fresolve-vat-number&origin=vat-sign-up-frontend")

      }
    }

  }
}
