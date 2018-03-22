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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.forms.BusinessEntityForm._

class CaptureBusinessEntityControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents{
  
  object TestCaptureBusinessEntityController extends CaptureBusinessEntityController(mockControllerComponents)

  val testGetRequest = FakeRequest("GET", "/business-entity")

  def testPostRequest(entityTypeVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/business-entity").withFormUrlEncodedBody(businessEntity -> entityTypeVal)

  "Calling the show action of the Capture Entity Type controller" should {
    "go to the Capture Entity Type page" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestCaptureBusinessEntityController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }


  "Calling the submit action of the Capture Business Entity controller" when {
    "form successfully submitted" should {

      "go to the capture company number page" when {
        "the business entity is limited company" in {
          mockAuthRetrieveAgentEnrolment()

          val result = TestCaptureBusinessEntityController.submit(testPostRequest(limitedCompany))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CaptureCompanyNumberController.show().url)
        }
      }

      "go to the capture client details page" when {
        "the business entity is sole trader" in {
          mockAuthRetrieveAgentEnrolment()

          val result = TestCaptureBusinessEntityController.submit(testPostRequest(soleTrader))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CaptureClientDetailsController.show().url)
        }
      }

      "go to the cannot use service yet page" when {
        "the business entity is other" in {
          mockAuthRetrieveAgentEnrolment()

          val result = TestCaptureBusinessEntityController.submit(testPostRequest(other))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CannotUseServiceController.show().url)
        }
      }
    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCaptureBusinessEntityController.submit(testPostRequest("invalid"))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}