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

package uk.gov.hmrc.vatsignupfrontend.connectors

import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, StubEmailVerification}
import uk.gov.hmrc.vatsignupfrontend.helpers.ComponentSpecBase
import uk.gov.hmrc.vatsignupfrontend.helpers.WiremockHelper.stubPost
import uk.gov.hmrc.vatsignupfrontend.models.{AlreadyVerifiedEmailAddress, RequestEmailPasscodeSuccessful}

class RequestEmailVerificationPasscodeConnectorISpec extends ComponentSpecBase with FeatureSwitching {

  lazy val connector: RequestEmailVerificationPasscodeConnector = app.injector.instanceOf[RequestEmailVerificationPasscodeConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  lazy val testEmail: String = "test@test.com"

  "requestEmailVerificationPasscode" should {
    "return a success response" when {
      "the feature switch is enabled and successfully calls the stub" in {
        enable(StubEmailVerification)

        stubPost("/vat-through-software/sign-up/test-only/email-verification/request-passcode", CREATED, "")

        val res = await(connector.requestEmailVerificationPasscode(testEmail, "en"))

        res shouldBe RequestEmailPasscodeSuccessful
      }
    }

    "return a already verified response" when {
      "the feature switch is enabled and the stub returns Conflict" in {
        enable(StubEmailVerification)

        stubPost("/vat-through-software/sign-up/test-only/email-verification/request-passcode", CONFLICT, "")

        val res = await(connector.requestEmailVerificationPasscode(testEmail, "en"))

        res shouldBe AlreadyVerifiedEmailAddress
      }
    }

    "return a success response" when {
      "the feature switch is disabled and the request email passcode API is successful" in {
        disable(StubEmailVerification)

        stubPost("/email-verification/request-passcode", CREATED, "")

        val res = await(connector.requestEmailVerificationPasscode(testEmail, "en"))

        res shouldBe RequestEmailPasscodeSuccessful
      }
    }

    "return a already verified response" when {
      "the feature switch is disabled and the request email passcode API returns Conflict" in {
        disable(StubEmailVerification)

        stubPost("/email-verification/request-passcode", CONFLICT, "")

        val res = await(connector.requestEmailVerificationPasscode(testEmail, "en"))

        res shouldBe AlreadyVerifiedEmailAddress
      }
    }
  }
}
