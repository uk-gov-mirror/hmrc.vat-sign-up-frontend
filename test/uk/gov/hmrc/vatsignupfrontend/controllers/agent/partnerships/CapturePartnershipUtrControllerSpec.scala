/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.forms.PartnershipUtrForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

import scala.util.Random

class CapturePartnershipUtrControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  override def beforeEach(): Unit = {
    super.beforeEach()
    val enableGeneral = Random.nextBoolean()
    if (enableGeneral) enable(GeneralPartnershipJourney)
    if (!enableGeneral | Random.nextBoolean()) enable(LimitedPartnershipJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(GeneralPartnershipJourney)
    disable(LimitedPartnershipJourney)
  }

  object TestCapturePartnershipUtrController extends CapturePartnershipUtrController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/partnership-utr")

  def testPostRequest(utr: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/partnership-utr").withFormUrlEncodedBody(partnershipUtr -> utr)

  "Calling the show action of the Partnership utr controller" when {
    "go to the Partnership utr page" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestCapturePartnershipUtrController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Partnership utr controller" when {
    "form successfully submitted" should {
      "go to the partnership ppob page" in {
        mockAuthRetrieveAgentEnrolment()

        implicit val request = testPostRequest(testSaUtr)
        val result = TestCapturePartnershipUtrController.submit(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.PartnershipPostCodeController.show().url)
      }
    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCapturePartnershipUtrController.submit(testPostRequest(""))

        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

}
