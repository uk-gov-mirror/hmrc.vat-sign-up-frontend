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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.forms.MultipleVatCheckForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreVatNumberService

class MultipleVatCheckControllerSpec extends UnitSpec with MockControllerComponents with MockStoreVatNumberService {

  object TestMultipleVatCheckController extends MultipleVatCheckController(
    mockControllerComponents,
    mockStoreVatNumberService
  )

  val testGetRequest = FakeRequest("GET", "/more-than-one-vat-business")

  def testPostRequest(entityTypeVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/more-than-one-vat-business").withFormUrlEncodedBody(yesNo -> entityTypeVal)

  "Calling the show action of the Multiple Vat Check controller" should {
    "go to the Multiple Vat Check page" in {
      mockAuthAdminRole()

      val result = TestMultipleVatCheckController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Multiple Vat Check controller" when {
    "form successfully submitted" should {
      "the choice is YES" should {
        "go to vat number" in {
          mockAuthRetrieveVatDecEnrolment()

          val result = TestMultipleVatCheckController.submit(testPostRequest(entityTypeVal = "yes"))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CaptureVatNumberController.show().url)
        }
      }

      "the choice is NO" when {
        "the VAT number is stored successfully" should {
          "go to business-type" in {
            mockAuthRetrieveVatDecEnrolment()
            mockStoreVatNumberSuccess(testVatNumber, isFromBta = Some(false))

            val result = TestMultipleVatCheckController.submit(testPostRequest(entityTypeVal = "no"))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.CaptureBusinessEntityController.show().url)
            session(result) get SessionKeys.vatNumberKey should contain(testVatNumber)
          }
        }
        "the VAT subscription has been claimed" should {
          "go to sign-up-complete-client" in {
            mockAuthRetrieveVatDecEnrolment()
            mockStoreVatNumberSubscriptionClaimed(testVatNumber, isFromBta = Some(false))

            val result = TestMultipleVatCheckController.submit(testPostRequest(entityTypeVal = "no"))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.SignUpCompleteClientController.show().url)
          }
        }
        "vat number is already subscribed" should {
          "redirect to the already subscribed page" in {
            mockAuthRetrieveVatDecEnrolment()
            mockStoreVatNumberAlreadySubscribed(vatNumber = testVatNumber, isFromBta = Some(false))

            val result = TestMultipleVatCheckController.submit(testPostRequest(entityTypeVal = "no"))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.AlreadySignedUpController.show().url)
          }
        }
        "the vat number is ineligible" should {
          "redirect to the already subscribed page" in {
            mockAuthRetrieveVatDecEnrolment()
            mockStoreVatNumberIneligible(vatNumber = testVatNumber,
                                         isFromBta = Some(false),
                                         migratableDates = MigratableDates())

            val result = TestMultipleVatCheckController.submit(testPostRequest(entityTypeVal = "no"))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.CannotUseServiceController.show().url)
          }
        }

        "store vat returns any other error" should {
          "throw internal server exception" in {
            mockAuthRetrieveVatDecEnrolment()
            mockStoreVatNumberFailure(vatNumber = testVatNumber, isFromBta = Some(false))

            intercept[InternalServerException] {
              await(TestMultipleVatCheckController.submit(testPostRequest(entityTypeVal = "no")))
            }
          }
        }
      }
    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthRetrieveVatDecEnrolment()

        val result = TestMultipleVatCheckController.submit(testPostRequest("invalid"))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}
