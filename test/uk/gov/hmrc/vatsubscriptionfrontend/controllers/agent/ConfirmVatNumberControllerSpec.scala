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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsubscriptionfrontend.services.mocks.MockStoreVatNumberService

class ConfirmVatNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreVatNumberService {

  object TestConfirmVatNumberController extends ConfirmVatNumberController(mockControllerComponents, mockStoreVatNumberService)

  lazy val testGetRequest = FakeRequest("GET", "/confirm-vat-number")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/confirm-vat-number")

  "Calling the show action of the Confirm Vat Number controller" when {
    "there is a vrn in the session" should {
      "go to the Confirm Vat number page" in {
        mockAuthRetrieveAgentEnrolment()
        val request = testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber)

        val result = TestConfirmVatNumberController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there isn't a vrn in the session" should {
      "redirect to Capture Vat number page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestConfirmVatNumberController.show(testGetRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
  }

  "Calling the submit action of the Confirm Vat Number controller" when {
    "vat number is in session and store vat is successful" should {
      "go to the business entity type page" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreVatNumberSuccess(vatNumber = testVatNumber)

        val result = TestConfirmVatNumberController.submit(testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.CaptureBusinessEntityController.show().url)
      }
    }
    "vat number is in session but store vat is unsuccessful as no agent client relationship" should {
      "go to the no agent client relationship page" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreVatNumberNoRelationship(vatNumber = testVatNumber)

        val result = TestConfirmVatNumberController.submit(testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.NoAgentClientRelationshipController.show().url)
      }
    }
    "vat number is in session but store vat is unsuccessful" should {
      "throw internal server exception" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreVatNumberFailure(vatNumber = testVatNumber)

        intercept[InternalServerException]{
          await(TestConfirmVatNumberController.submit(testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber)))
        }
      }
    }

    "store vat is unsuccessful" when {
      "vat number is already subscribed" should {
        "redirect to the already subscribed page" in {
          mockAuthRetrieveAgentEnrolment()
          mockStoreVatNumberAlreadySubscribed(vatNumber = testVatNumber)

         val result = TestConfirmVatNumberController.submit(testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber))
         status(result) shouldBe Status.SEE_OTHER
         redirectLocation(result) shouldBe Some(routes.AlreadySignedUpController.show().url)
      }
    }
  }

    "vat number is not in session" should {
      "redirect to capture vat number" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestConfirmVatNumberController.submit(testPostRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
  }

}