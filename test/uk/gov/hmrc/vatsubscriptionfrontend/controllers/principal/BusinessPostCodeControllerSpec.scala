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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.featureswitch.{FeatureSwitching, KnownFactsJourney}
import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.forms.BusinessPostCodeForm._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstants._

class BusinessPostCodeControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestBusinessPostCodeController extends BusinessPostCodeController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/business-postcode")

  def testPostRequest(businessPostCodeVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/business-postcode").withFormUrlEncodedBody(businessPostCode -> businessPostCodeVal)

  "Calling the show action of the Business PostCode controller" when {
    "the known facts journey feature switch is enabled" should {
      "go to the Business PostCode page" in {
        enable(KnownFactsJourney)

        mockAuthEmptyRetrieval()

        val result = TestBusinessPostCodeController.show(testGetRequest)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
    "the known facts journey feature switch is disabled" should {
      "throw a NotFoundException" in {
        disable(KnownFactsJourney)

        intercept[NotFoundException](await(TestBusinessPostCodeController.show(testGetRequest)))

      }
    }
  }


  "Calling the submit action of the Business PostCode controller" when {
    "the known facts journey feature switch is enabled" when {
      "form successfully submitted" should {
        "goto capture Business Entity page" in {
          enable(KnownFactsJourney)

          mockAuthEmptyRetrieval()

          val request = testPostRequest(testBusinessPostcode)
          val result = TestBusinessPostCodeController.submit(request)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CaptureBusinessEntityController.show().url)
        }
      }

      "form unsuccessfully submitted" should {
        "reload the page with errors" in {
          enable(KnownFactsJourney)

          mockAuthEmptyRetrieval()

          val result = TestBusinessPostCodeController.submit(testPostRequest(""))
          status(result) shouldBe Status.BAD_REQUEST
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }
    "the known facts journey feature switch is disabled" should {
      "throw a NotFoundException" in {
        disable(KnownFactsJourney)

        intercept[NotFoundException](await(TestBusinessPostCodeController.submit(testPostRequest(""))))

      }
    }
  }
}