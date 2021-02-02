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

package uk.gov.hmrc.vatsignupfrontend.connectors

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, StubEmailVerification}
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, WiremockHelper}
import uk.gov.hmrc.vatsignupfrontend.models._

class VerifyEmailPasscodeConnectorISpec extends ComponentSpecBase with FeatureSwitching {

  lazy val connector: VerifyEmailPasscodeConnector = app.injector.instanceOf[VerifyEmailPasscodeConnector]

  lazy val testPasscode: String = "123456"
  lazy val testEmail: String = "test@test.com"
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "verifyEmailVerificationPasscode" should {
    "return a successful response" when {
      "the feature switch is disabled and the email verification API returns Created" in {
        disable(StubEmailVerification)

        WiremockHelper.stubPost("/email-verification/verify-passcode", CREATED, "")

        val res = await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))

        res shouldBe EmailVerifiedSuccessfully
      }
    }

    "return already verified response" when {
      "the feature switch is disabled and the email verification API returns NoContent" in {
        disable(StubEmailVerification)

        WiremockHelper.stubPost("/email-verification/verify-passcode", NO_CONTENT, "")

        val res = await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))

        res shouldBe EmailAlreadyVerified
      }
    }

    "return PasscodeMismatch response" when {
      "the feature switch is disabled and the email verification API returns PASSCODE_MISMATCH" in {
        disable(StubEmailVerification)

        WiremockHelper.stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_MISMATCH").toString)

        val res = await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))

        res shouldBe PasscodeMismatch
      }
    }

    "return PasscodeNotFound response" when {
      "the feature switch is disabled and the email verification API returns PASSCODE_NOT_FOUND" in {
        disable(StubEmailVerification)

        WiremockHelper.stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("code" -> "PASSCODE_NOT_FOUND").toString)

        val res = await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))

        res shouldBe PasscodeNotFound
      }
    }

    "return MaxAttemptsExceeded response" when {
      "the feature switch is disabled and the email verification API returns MAX_PASSCODE_ATTEMPTS_EXCEEDED" in {
        disable(StubEmailVerification)

        WiremockHelper.stubPost("/email-verification/verify-passcode", FORBIDDEN, Json.obj("code" -> "MAX_PASSCODE_ATTEMPTS_EXCEEDED").toString)

        val res = await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))

        res shouldBe MaxAttemptsExceeded
      }
    }

    "return unexpected response" when {
      "the feature switch is disabled and the email verification API returns InternalServerException" in {
        disable(StubEmailVerification)

        WiremockHelper.stubPost("/email-verification/verify-passcode", INTERNAL_SERVER_ERROR, "")

        intercept[InternalServerException] {
          await(connector.verifyEmailVerificationPasscode(testEmail, testPasscode))
        }
      }
    }
  }

}
