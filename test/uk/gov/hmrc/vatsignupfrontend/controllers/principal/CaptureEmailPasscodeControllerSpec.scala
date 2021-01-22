/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockVatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.error.{routes => errorRoutes}
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.forms.EmailPasscodeForm
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.{MockEmailVerificationService, MockStoreEmailAddressService}
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.capture_email_passcode

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CaptureEmailPasscodeControllerSpec extends UnitSpec
  with GuiceOneAppPerSuite
  with MockVatControllerComponents
  with MockEmailVerificationService
  with MockStoreEmailAddressService {

  class Setup {
    val view = app.injector.instanceOf[capture_email_passcode]

    object Controller extends CaptureEmailPasscodeController(
      mockEmailVerificationService,
      mockStoreEmailAddressService,
      view
    )
  }

  val testEmail = "testEmail"
  val testVrn = "123456782"
  val testPassCode = "123456"

  "CaptureEmailPasscode controller show" when {
    "a new passcode is requested" should {
      "return OK if the user's email is in session" in new Setup {
        mockAuthAdminRole()
        mockRequestPasscode(testEmail)(Future.successful(RequestEmailPasscodeSuccessful))

        val res = Controller.show()(FakeRequest().withSession(SessionKeys.emailKey -> testEmail))

        status(res) shouldBe OK
      }
      "throw an exception if the email is not in session" in new Setup {
        mockAuthAdminRole()
        mockRequestPasscode(testEmail)(Future.successful(RequestEmailPasscodeSuccessful))

        intercept[InternalServerException] {
          await(Controller.show()(FakeRequest()))
        }
      }
    }
    "the email address is already verified" should {
      "redirect to the email verified page" in new Setup {
        mockAuthAdminRole()
        mockRequestPasscode(testEmail)(Future.successful(AlreadyVerifiedEmailAddress))

        val res = Controller.show()(FakeRequest().withSession(SessionKeys.emailKey -> testEmail))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(principalRoutes.EmailVerifiedController.show().url)
      }
    }
  }

  "CaptureEmailPasscode controller submit" when {
    "the relevant data is in session" when {
      "the passcode is valid" should {
        "redirect to the EmailVerified page if the email is stored" in new Setup {
          mockAuthAdminRole()
          mockVerifyPasscode(testEmail, testPassCode)(Future.successful(EmailVerifiedSuccessfully))
          mockStoreEmailAddressSuccess(testVrn, testEmail)(true)

          val res = Controller.submit().apply(FakeRequest("POST", "/")
            .withSession(
              SessionKeys.vatNumberKey -> testVrn,
              SessionKeys.emailKey -> testEmail
            )
            .withFormUrlEncodedBody(
              EmailPasscodeForm.code -> testPassCode
            ))


          status(res) shouldBe SEE_OTHER
          redirectLocation(res) shouldBe Some(principalRoutes.EmailVerifiedController.show().url)
        }
        "throw an exception if store email fails" in new Setup {
          mockAuthAdminRole()
          mockVerifyPasscode(testEmail, testPassCode)(Future.successful(EmailVerifiedSuccessfully))
          mockStoreEmailAddressFailure(testVrn, testEmail)

          intercept[InternalServerException] {
            await(Controller.submit().apply(FakeRequest()
              .withSession(
                SessionKeys.vatNumberKey -> testVrn,
                SessionKeys.emailKey -> testEmail
              )
              .withFormUrlEncodedBody(
                EmailPasscodeForm.code -> testPassCode
              )))
          }
        }
      }
      "the user has already verified" should {
        "redirect to the EmailVerified page if the email is stored" in new Setup {
          mockAuthAdminRole()
          mockVerifyPasscode(testEmail, testPassCode)(Future.successful(EmailAlreadyVerified))
          mockStoreEmailAddressSuccess(testVrn, testEmail)(true)

          val res = Controller.submit().apply(FakeRequest()
            .withSession(
              SessionKeys.vatNumberKey -> testVrn,
              SessionKeys.emailKey -> testEmail
            )
            .withFormUrlEncodedBody(
              EmailPasscodeForm.code -> testPassCode
            ))

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) shouldBe Some(principalRoutes.EmailVerifiedController.show().url)
        }
        "throw an exception if store email fails" in new Setup {
          mockAuthAdminRole()
          mockVerifyPasscode(testEmail, testPassCode)(Future.successful(EmailAlreadyVerified))
          mockStoreEmailAddressFailure(testVrn, testEmail)

          intercept[InternalServerException] {
            await(Controller.submit().apply(FakeRequest()
              .withSession(
                SessionKeys.vatNumberKey -> testVrn,
                SessionKeys.emailKey -> testEmail
              )
              .withFormUrlEncodedBody(
                EmailPasscodeForm.code -> testPassCode
              )))
          }
        }
      }
      "the passcode is invalid" should {
        "return BAD_REQUEST" in new Setup {
          mockAuthAdminRole()
          mockVerifyPasscode(testEmail, testPassCode)(Future.successful(PasscodeMismatch))

          val res = Controller.submit().apply(FakeRequest()
            .withSession(
              SessionKeys.vatNumberKey -> testVrn,
              SessionKeys.emailKey -> testEmail
            )
            .withFormUrlEncodedBody(
              EmailPasscodeForm.code -> testPassCode
            ))

          status(res) shouldBe BAD_REQUEST
        }
      }
      "the user has exceeded the number of allowed attempts" should {
        "redirect to the Max Attempts page" in new Setup {
          mockAuthAdminRole()
          mockVerifyPasscode(testEmail, testPassCode)(Future.successful(MaxAttemptsExceeded))

          val res = Controller.submit().apply(FakeRequest()
            .withSession(
              SessionKeys.vatNumberKey -> testVrn,
              SessionKeys.emailKey -> testEmail
            )
            .withFormUrlEncodedBody(
              EmailPasscodeForm.code -> testPassCode
            ))

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) shouldBe Some(errorRoutes.MaxEmailPasscodeAttemptsExceededController.show().url)
        }
      }
      "the passcode is not found" should {
        "Redirect to the PasscodeNotFound page" in new Setup {
          mockAuthAdminRole()
          mockVerifyPasscode(testEmail, testPassCode)(Future.successful(PasscodeNotFound))

          val res = Controller.submit().apply(FakeRequest()
            .withSession(
              SessionKeys.vatNumberKey -> testVrn,
              SessionKeys.emailKey -> testEmail
            )
            .withFormUrlEncodedBody(
              EmailPasscodeForm.code -> testPassCode
            ))

          status(res) shouldBe SEE_OTHER
          redirectLocation(res) shouldBe Some(errorRoutes.PasscodeNotFoundController.show().url)
        }
      }
    }
    "the VRN is missing from session" should {
      "throw an exception" in new Setup {
        mockAuthAdminRole()

        intercept[InternalServerException] {
          await(Controller.submit().apply(FakeRequest()
            .withFormUrlEncodedBody(
              EmailPasscodeForm.code -> testPassCode
            )))
        }
      }
    }
    "the email is missing from session" should {
      "throw an exception" in new Setup {
        mockAuthAdminRole()

        intercept[InternalServerException] {
          await(Controller.submit().apply(FakeRequest()
            .withFormUrlEncodedBody(
              EmailPasscodeForm.code -> testPassCode
            )))
        }
      }
    }
  }

}
