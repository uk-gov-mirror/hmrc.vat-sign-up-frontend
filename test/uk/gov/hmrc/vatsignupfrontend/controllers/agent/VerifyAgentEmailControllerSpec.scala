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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

class VerifyAgentEmailControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestVerifyAgentEmailController extends VerifyAgentEmailController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/verify-your-email")

  "Calling the show action of the verify Agent Email controller" when {
    "there is a email in the session" should {
      "show the verify Agent Email page" in {
        mockAuthRetrieveAgentEnrolment()
        val request = testGetRequest.withSession(SessionKeys.transactionEmailKey -> testEmail)

        val result = TestVerifyAgentEmailController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there isn't a email in the session" should {
      "redirect to Capture Agent Email page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestVerifyAgentEmailController.show(testGetRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureAgentEmailController.show().url)
      }
    }
  }

}
