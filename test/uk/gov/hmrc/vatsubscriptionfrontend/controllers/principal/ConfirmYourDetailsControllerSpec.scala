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

import java.time.LocalDate
import java.util.UUID

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.IdentityVerificationProxySuccessResponse
import uk.gov.hmrc.vatsubscriptionfrontend.models.{DateModel, UserDetailsModel}
import uk.gov.hmrc.vatsubscriptionfrontend.services.mocks.{MockIdentityVerificationService, MockStoreNinoService}

import scala.concurrent.Future

class ConfirmYourDetailsControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents with MockStoreNinoService with MockIdentityVerificationService {

  object TestConfirmYourDetailsController extends ConfirmYourDetailsController(
    mockControllerComponents,
    mockStoreNinoService,
    mockIdentityVerificationService
  )

  lazy val testGetRequest = FakeRequest("GET", "/confirm-details")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/confirm-details")

  val testUserDetails: UserDetailsModel =
    UserDetailsModel(
      firstName = UUID.randomUUID().toString,
      lastName = UUID.randomUUID().toString,
      nino = testNino,
      dateOfBirth = DateModel.dateConvert(LocalDate.now())
    )
  val testUserDetailsJson: String = Json.toJson(testUserDetails).toString()

  "Calling the show action of the Confirm Your Details controller" when {
    "Your Details in the session" should {
      "return an OK" in {
        mockAuthEmptyRetrieval()

        val request = testGetRequest.withSession(SessionKeys.userDetailsKey -> testUserDetailsJson)
        val result = TestConfirmYourDetailsController.show(request)

        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }


    "there isn't a user detail in the session" should {
      "redirect to Capture Your Details page" in {
        mockAuthEmptyRetrieval()

        val result = TestConfirmYourDetailsController.show(testGetRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureYourDetailsController.show().url)
      }
    }
  }

  "Calling the submit action of the Confirm Your Details controller" when {
    "vat number and your details are in session" when {
      implicit lazy val request = testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.userDetailsKey -> testUserDetailsJson)
      def callSubmit = TestConfirmYourDetailsController.submit(request)

      "and store nino is successful" when {
        "identity verification is successful" should {
          "redirect to agree capture email page" in {
            val testRedirectUrl = "/test/redirect/url"
            val testContinueUrl = "/test/continue/url"

            mockAuthEmptyRetrieval()
            mockStoreNinoSuccess(testVatNumber, testUserDetails)
            mockStart(Future.successful(Right(
              IdentityVerificationProxySuccessResponse(testRedirectUrl, testContinueUrl)
            )))

            val result = callSubmit

            val expectedRedirectUrl = mockAppConfig.identityVerificationFrontendRedirectionUrl(testRedirectUrl)

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(expectedRedirectUrl)

            result.session.get(SessionKeys.identityVerificationContinueUrlKey) should contain(testContinueUrl)
            result.session.get(SessionKeys.userDetailsKey) shouldBe empty
          }
        }
      }

      "but store nino returned no match" should {
        "throw internal server exception" in {
          mockAuthEmptyRetrieval()
          mockStoreNinoNoMatch(testVatNumber, testUserDetails)

          val result = callSubmit

          intercept[InternalServerException] {
            await(result)
          }
        }
      }

      "but store nino returned no vat" should {
        "throw internal server exception" in {
          mockAuthEmptyRetrieval()
          mockStoreNinoNoVatStored(testVatNumber, testUserDetails)

          val result = callSubmit

          intercept[InternalServerException] {
            await(result)
          }
        }
      }

      "but store nino returned failure" should {
        "throw internal server exception" in {
          mockAuthEmptyRetrieval()
          mockStoreNinoNoVatStored(testVatNumber, testUserDetails)

          val result = callSubmit

          intercept[InternalServerException] {
            await(result)
          }
        }
      }
    }

    "vat number is not in session" should {
      "redirect to capture vat number" in {
        mockAuthEmptyRetrieval()

        val result = TestConfirmYourDetailsController.submit(testPostRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.YourVatNumberController.show().url)
      }
    }

    "your details is not in session" should {
      "redirect to capture your details" in {
        mockAuthEmptyRetrieval()

        val result = TestConfirmYourDetailsController.submit(testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureYourDetailsController.show().url)
      }
    }
  }

}