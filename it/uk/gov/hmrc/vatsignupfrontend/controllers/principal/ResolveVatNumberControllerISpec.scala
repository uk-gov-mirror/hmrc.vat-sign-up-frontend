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
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreVatNumberStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class ResolveVatNumberControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /resolve-vat-number" when {
    "the vat number is on the profile" should {
      "redirect to the Multiple Vat Check page" in {
        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))

        val res = get("/resolve-vat-number")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.MultipleVatCheckController.show().url)
        )
      }
    }
    "the vat number is not on the profile" when {
      "the KnownFactsJourney feature switch is enabled" should {
        "redirect to the capture VAT number page" in {
          stubAuth(OK, successfulAuthResponse())
          stubStoreVatNumberSuccess()

          val res = get("/resolve-vat-number")

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CaptureVatNumberController.show().url)
          )
        }
      }
    }
  }

}
