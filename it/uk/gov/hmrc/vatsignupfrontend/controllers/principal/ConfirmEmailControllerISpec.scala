/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.emailKey
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.EmailVerification
import uk.gov.hmrc.vatsignupfrontend.forms.EmailForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreEmailAddressStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.EmailVerificationStub._

class ConfirmEmailControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /confirm-email" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/confirm-email", Map(SessionKeys.emailKey -> testEmail, SessionKeys.vatNumberKey -> testVatNumber))

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /confirm-email" when {
    "the EmailVerified FS is disabled" should {
      "redirect to verify email page" when {
        "the email is successfully stored and returned with email not verified on the contact preference journey" in {
          stubAuth(OK, successfulAuthResponse())
          stubStoreTransactionEmailAddressSuccess(emailVerified = false)

          val res = post("/confirm-email", Map(SessionKeys.emailKey -> testEmail, SessionKeys.vatNumberKey -> testVatNumber))(EmailForm.email -> testEmail)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.VerifyEmailController.show().url)
          )

          val session = getSessionMap(res)
          session.keys should contain(emailKey)
        }
      }

      "redirect to receive email notifications controller" when {
        "the email is successfully stored and returned with email verified flag on the contact preference journey" in {
          stubAuth(OK, successfulAuthResponse())
          stubStoreTransactionEmailAddressSuccess(emailVerified = true)

          val res = post("/confirm-email", Map(SessionKeys.emailKey -> testEmail, SessionKeys.vatNumberKey -> testVatNumber))(EmailForm.email -> testEmail)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ReceiveEmailNotificationsController.show().url)
          )

          val session = getSessionMap(res)
          session.keys should contain(emailKey)
        }
      }

      "throw an internal server error" when {
        "storing the email has been unsuccessful" in {
          stubAuth(OK, successfulAuthResponse())
          stubStoreEmailAddressFailure()

          val res = post("/confirm-email", Map(SessionKeys.emailKey -> testEmail, SessionKeys.vatNumberKey -> testVatNumber))(EmailForm.email -> testEmail)
          res should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )

        }
      }
    }
    "the EmailVerified FS is enabled" when {
      "the request passcode API returns RequestEmailPasscodeSuccessful" should {
        "redirect to the CaptureEmailPasscode page" in {
          enable(EmailVerification)
          stubAuth(OK, successfulAuthResponse())
          stubRequestPasscode(CREATED)

          val res = post("/confirm-email", Map(SessionKeys.emailKey -> testEmail, SessionKeys.vatNumberKey -> testVatNumber))()

          res should have (
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureEmailPasscodeController.show().url)
          )
        }
      }
      "the request passcode API returns AlreadyVerifiedEmailAddress" should {
        "store the email address and redirect to the EmailVerified page" in {
          enable(EmailVerification)
          stubAuth(OK, successfulAuthResponse())
          stubRequestPasscode(CONFLICT)
          stubStoreTransactionEmailVerified(CREATED)

          val res = post("/confirm-email", Map(SessionKeys.emailKey -> testEmail, SessionKeys.vatNumberKey -> testVatNumber))()

          res should have (
            httpStatus(SEE_OTHER),
            redirectUri(routes.EmailVerifiedController.show().url)
          )
        }
        "throw an exception if the store email call fails" in {
          enable(EmailVerification)
          stubAuth(OK, successfulAuthResponse())
          stubRequestPasscode(CONFLICT)
          stubStoreTransactionEmailVerified(INTERNAL_SERVER_ERROR)

          val res = post("/confirm-email", Map(SessionKeys.emailKey -> testEmail, SessionKeys.vatNumberKey -> testVatNumber))()

          res should have (
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }
      "the request passcode API call fails" should {
        "throw an exception" in {
          enable(EmailVerification)
          stubAuth(OK, successfulAuthResponse())
          stubRequestPasscode(INTERNAL_SERVER_ERROR)

          val res = post("/confirm-email", Map(SessionKeys.emailKey -> testEmail, SessionKeys.vatNumberKey -> testVatNumber))()

          res should have (
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }
    }
  }
}
