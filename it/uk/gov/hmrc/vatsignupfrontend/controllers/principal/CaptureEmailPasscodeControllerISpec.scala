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

import play.api.http.Status.NOT_FOUND
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.error.{routes => errorRoutes}
import uk.gov.hmrc.vatsignupfrontend.forms.EmailPasscodeForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.EmailVerificationStub.stubVerifyEmailPasscode
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreEmailAddressStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class CaptureEmailPasscodeControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /email-address-passcode" when {
    "the passcode request is creates" when {
      "the email is in session" should {
        "return OK" in {
          stubAuth(OK, successfulAuthResponse())

          val res = get("/email-address-passcode", Map(
            SessionKeys.emailKey -> testEmail
          ))

          res.status shouldBe OK
        }
      }
    }
    "the email is not in session" should {
      "throw an InternalServerException" in {
        stubAuth(OK, successfulAuthResponse())

        val res = get("/email-address-passcode")

        res.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /email-address-passcode" when {
    "the relevant data is in session" when {
      "the passcode is valid" should {
        "redirect to the EmailVerified page if the email is stored" in {
          stubAuth(OK, successfulAuthResponse())
          stubVerifyEmailPasscode(CREATED, Json.obj())
          stubStoreTransactionEmailVerified(CREATED)

          val res = post(
            uri = "/email-address-passcode",
            cookies = Map(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.emailKey -> testEmail
            ))(EmailPasscodeForm.code -> testPasscode)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.EmailVerifiedController.show().url)
          )
        }
        "throw an exception if store email fails" in {
          stubAuth(OK, successfulAuthResponse())
          stubVerifyEmailPasscode(CREATED, Json.obj())
          stubStoreTransactionEmailVerified(INTERNAL_SERVER_ERROR)

          val res = post(
            uri = "/email-address-passcode",
            cookies = Map(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.emailKey -> testEmail
            ))(EmailPasscodeForm.code -> testPasscode)

          res should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }
      "the user has already verified" should {
        "redirect to the EmailVerified page if the email is stored" in {
          stubAuth(OK, successfulAuthResponse())
          stubVerifyEmailPasscode(NO_CONTENT, Json.obj())
          stubStoreTransactionEmailVerified(CREATED)

          val res = post(
            uri = "/email-address-passcode",
            cookies = Map(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.emailKey -> testEmail
            ))(EmailPasscodeForm.code -> testPasscode)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.EmailVerifiedController.show().url)
          )
        }
        "throw an exception if store email fails" in {
          stubAuth(OK, successfulAuthResponse())
          stubVerifyEmailPasscode(NO_CONTENT, Json.obj())
          stubStoreTransactionEmailVerified(INTERNAL_SERVER_ERROR)

          val res = post(
            uri = "/email-address-passcode",
            cookies = Map(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.emailKey -> testEmail
            ))(EmailPasscodeForm.code -> testPasscode)

          res should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }
      "the passcode is invalid" should {
        "return BAD_REQUEST" in {
          stubAuth(OK, successfulAuthResponse())
          stubVerifyEmailPasscode(NOT_FOUND, Json.obj("code" -> "PASSCODE_MISMATCH"))

          val res = post(
            uri = "/email-address-passcode",
            cookies = Map(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.emailKey -> testEmail
            ))(EmailPasscodeForm.code -> testPasscode)

          res should have(
            httpStatus(BAD_REQUEST)
          )
        }
      }
      "the user has exceeded the number of allowed attempts" should {
        "redirect to the Max Attempts page" in {
          stubAuth(OK, successfulAuthResponse())
          stubVerifyEmailPasscode(FORBIDDEN, Json.obj("code" -> "MAX_PASSCODE_ATTEMPTS_EXCEEDED"))

          val res = post(
            uri = "/email-address-passcode",
            cookies = Map(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.emailKey -> testEmail
            ))(EmailPasscodeForm.code -> testPasscode)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(errorRoutes.MaxEmailPasscodeAttemptsExceededController.show().url)
          )
        }
      }
      "the passcode is not found" should {
        "Redirect to the PasscodeNotFound page" in {
          stubAuth(OK, successfulAuthResponse())
          stubVerifyEmailPasscode(NOT_FOUND, Json.obj("code" -> "PASSCODE_NOT_FOUND"))

          val res = post(
            uri = "/email-address-passcode",
            cookies = Map(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.emailKey -> testEmail
            ))(EmailPasscodeForm.code -> testPasscode)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(errorRoutes.PasscodeNotFoundController.show().url)
          )
        }
      }
    }
    "the VRN is missing from session" should {
      "throw an exception" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post(
          uri = "/email-address-passcode",
          cookies = Map(
            SessionKeys.emailKey -> testEmail
          ))(EmailPasscodeForm.code -> testPasscode)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
    "the email is missing from session" should {
      "throw an exception" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post(
          uri = "/email-address-passcode",
          cookies = Map(
            SessionKeys.vatNumberKey -> testVatNumber
          ))(EmailPasscodeForm.code -> testPasscode
        )

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }

}