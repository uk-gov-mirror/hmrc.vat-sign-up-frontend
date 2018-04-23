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

import play.api.http.Status._
import uk.gov.hmrc.vatsubscriptionfrontend.config.featureswitch.{FeatureSwitching, KnownFactsJourney}
import uk.gov.hmrc.vatsubscriptionfrontend.forms.VatNumberForm
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.servicemocks.VatEligibilityStub._

class CaptureVatNumberControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  override def beforeEach(): Unit = enable(KnownFactsJourney)

  override def afterEach(): Unit = disable(KnownFactsJourney)

  "GET /vat-number" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/vat-number")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /vat-number" should {
    "return NotImplemented" when {
      "the vat number is eligible" in {
        stubAuth(OK, successfulAuthResponse())
        stubVatNumberEligibilitySuccess(testVatNumber)

        val res = post("/vat-number")(VatNumberForm.vatNumber -> testVatNumber)

        res should have(
          httpStatus(NOT_IMPLEMENTED)
        )

      }
    }

    "redirect to the invalid vat number page" when {
      "the vat number is invalid" in {
        stubAuth(OK, successfulAuthResponse())
        stubVatNumberEligibilityInvalid(testVatNumber)

        val res = post("/vat-number")(VatNumberForm.vatNumber -> testVatNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.InvalidVatNumberController.show().url)
        )

      }
    }

    "redirect to the Already Signed up page" when {
      "the vat number is already signed up" in {
        stubAuth(OK, successfulAuthResponse())
        stubVatNumberEligibilityAlreadySubscribed(testVatNumber)

        val res = post("/vat-number")(VatNumberForm.vatNumber -> testVatNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.AlreadySignedUpController.show().url)
        )

      }
    }

    "redirect to the Cannot Use Service page" when {
      "the vat number is ineligible for mtd vat" in {
        stubAuth(OK, successfulAuthResponse())
        stubVatNumberIneligibleForMtd(testVatNumber)

        val res = post("/vat-number")(VatNumberForm.vatNumber -> testVatNumber)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CannotUseServiceController.show().url)
        )

      }
    }

    "throw an internal server error" when {
      "any other failure occurs" in {
        stubAuth(OK, successfulAuthResponse())
        stubVatNumberEligibilityFailure(testVatNumber)

        val res = post("/vat-number")(VatNumberForm.vatNumber -> testVatNumber)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )

      }
    }
  }


  "Making a request to /vat-number when not enabled" should {
    "return NotFound" in {
      disable(KnownFactsJourney)

      stubAuth(OK, successfulAuthResponse())

      val res = get("/vat-number")

      res should have(
        httpStatus(NOT_FOUND)
      )

    }
  }

}
