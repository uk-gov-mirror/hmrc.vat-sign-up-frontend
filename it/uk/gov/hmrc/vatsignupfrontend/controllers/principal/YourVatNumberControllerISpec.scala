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

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.KnownFactsJourney
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreVatNumberStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class YourVatNumberControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /your-vat-number" when {
    "the vat number is on the profile" should {
      "return an OK" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

        val res = get("/your-vat-number")

        res should have(
          httpStatus(OK)
        )
      }
    }
    "the vat number is not on the profile" when {
      "the KnownFactsJourney feature switch is enabled" should {
        "redirect to resolve VAT number controller" in {
          enable(KnownFactsJourney)

          stubAuth(OK, successfulAuthResponse())
          stubStoreVatNumberSuccess()

          val res = get("/your-vat-number")

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ResolveVatNumberController.resolve().url)
          )
        }
      }

      "the KnownFactsJourney feature switch is disabled" should {
        "redirect to resolve VAT number controller" in {
          stubAuth(OK, successfulAuthResponse())
          stubStoreVatNumberSuccess()

          val res = get("/your-vat-number")

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ResolveVatNumberController.resolve().url)
          )
        }
      }
    }
  }

  "POST /your-vat-number" when {
    "the vat number is on the profile" should {
      "redirect to the capture client business entity page" when {
        "the vat number is successfully stored" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubStoreVatNumberSuccess()

          val res = post("/your-vat-number")()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureBusinessEntityController.show().url)
          )
        }
      }

      "redirect to the already signed up page" when {
        "the vat number has already been signed up" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubStoreVatNumberAlreadySignedUp()

          val res = post("/your-vat-number")()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.AlreadySignedUpController.show().url)
          )
        }
      }

      "redirect to the cannot use service page" when {
        "the vat number is ineligible" in {
          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubStoreVatNumberIneligible()

          val res = post("/your-vat-number")()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CannotUseServiceController.show().url)
          )
        }
      }
    }

    "the vat number is not on the profile" when {
      "the KnownFactsJourney feature switch is enabled" should {
        "redirect to resolve VAT number controller" in {
          enable(KnownFactsJourney)

          stubAuth(OK, successfulAuthResponse())
          stubStoreVatNumberSuccess()

          val res = post("/your-vat-number")()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ResolveVatNumberController.resolve().url)
          )
        }
      }

      "the KnownFactsJourney feature switch is disabled" should {
        "redirect to resolve VAT number controller" in {
          stubAuth(OK, successfulAuthResponse())
          stubStoreVatNumberSuccess()

          val res = post("/your-vat-number")()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ResolveVatNumberController.resolve().url)
          )
        }
      }
    }
  }
}
