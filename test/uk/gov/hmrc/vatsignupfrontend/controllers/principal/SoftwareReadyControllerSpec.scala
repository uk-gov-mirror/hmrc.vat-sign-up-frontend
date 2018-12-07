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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.forms.MultipleVatCheckForm._

class SoftwareReadyControllerSpec extends UnitSpec with MockControllerComponents {

  object TestSoftwareReadyController extends SoftwareReadyController(mockControllerComponents)

  val testGetRequest = FakeRequest("GET", "/software-ready")

  def testPostRequest(entityTypeVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/software-ready").withFormUrlEncodedBody(yesNo -> entityTypeVal)

  "Calling the show action of the Software Ready controller" should {
    "go to the Software Ready page" in {

      val result = TestSoftwareReadyController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Software Ready controller" when {
    "form successfully submitted" when {
      "the choice is YES" should {
        "go to resolve vat number" in {

          val result = await(TestSoftwareReadyController.submit(testPostRequest(entityTypeVal = "yes")))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ResolveVatNumberController.resolve().url)
        }
      }

      "the choice is NO" should {
        "go to verify software error page" in {

          val result = await(TestSoftwareReadyController.submit(testPostRequest(entityTypeVal = "no")))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.VerifySoftwareErrorController.show().url)
        }
      }
    }
  }

  "form unsuccessfully submitted" should {
    "reload the page with errors" in {

      val result = await(TestSoftwareReadyController.submit(testPostRequest("invalid")))
      status(result) shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

}
