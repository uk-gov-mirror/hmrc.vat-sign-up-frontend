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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.soletrader

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.SkipCidCheck
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.testNino
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.{routes => agentRoutes}
import uk.gov.hmrc.vatsignupfrontend.forms.NinoForm.nino

class CaptureNinoControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestCaptureNinoController extends CaptureNinoController(mockControllerComponents)

  val testGetRequest = FakeRequest("GET", "/client/national-insurance-number")

  private def testPostRequest(postNino: String = testNino): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/client/national-insurance-number").withFormUrlEncodedBody( nino -> postNino)

  override def beforeEach(): Unit = enable(SkipCidCheck)

  "Calling the Show method of the Capture NINO controller for agent" when {
    "the SkipIdCheck feature switch is enabled" should {
      "show the Capture NINO page" in {
        mockAuthRetrieveAgentEnrolment()
        val res = await(TestCaptureNinoController.show(testGetRequest))
        status(res) shouldBe OK
        contentType(res) shouldBe Some("text/html")
        charset(res) shouldBe Some("utf-8")
      }
    }
    "the SkipIdCheck feature switch is disabled" should {
      "throw an internal server exception" in {
        disable(SkipCidCheck)
        intercept[NotFoundException] {
          await(TestCaptureNinoController.show(testGetRequest))
        }
      }
    }
  }
  "Calling the Submit method of the Capture NINO controller for agent" when {
    "A valid Nino has been submitted" should {
      "Redirect page not implemented " in {
        mockAuthRetrieveAgentEnrolment()
        val res = await(TestCaptureNinoController.submit(testPostRequest()))
        // TODO to test the redirect
        status(res) shouldBe NOT_IMPLEMENTED
      }
    }
    "An invalid NINO has not been submitted" should {
      "Display the Capture NINO page with error message" in {
        mockAuthRetrieveAgentEnrolment()
        val res = TestCaptureNinoController.submit(testPostRequest("QQ123456C"))
        status(res) shouldBe BAD_REQUEST
        contentType(res) shouldBe Some("text/html")
        charset(res) shouldBe Some("utf-8")
      }
    }
  }

}
