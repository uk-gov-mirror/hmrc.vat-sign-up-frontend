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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockSubmissionService

class TermsControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents
  with MockSubmissionService {

  object TestTermsController extends TermsController(mockControllerComponents, mockSubmissionService)

  lazy val testGetRequest = FakeRequest("GET", "/terms-of-participation")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/terms-of-participation").withSession(SessionKeys.vatNumberKey -> testVatNumber)

  "Calling the show action of the Terms controller" should {
    "show the Terms page" in {
      mockAuthAdminRole()
      val request = testGetRequest

      val result = TestTermsController.show(request)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Terms controller" when {
    "submission is successful" should {
      "goto information received" in {
        mockAuthAdminRole()
        mockSubmitSuccess(testVatNumber)

        val result = TestTermsController.submit(testPostRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.InformationReceivedController.show().url)
      }
    }

    "submission is unsuccessful" should {
      "throw internal server exception" in {
        mockAuthAdminRole()
        mockSubmitFailure(testVatNumber)

        intercept[InternalServerException] {
          await(TestTermsController.submit(testPostRequest))
        }
      }
    }
  }

}
