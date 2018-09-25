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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{BTAClaimSubscription, FeatureSwitching}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreVatNumberStub.stubStoreVatNumberSubscriptionClaimed
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}


class ClaimSubscriptionControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  "GET /claim-subscription/:vat-number" when {
    "the user has a matching VATDEC enrolment" when {
      "store VAT number returns subscription claimed" should {
        "return a redirect to the confirmation page" in {
          enable(BTAClaimSubscription)

          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubStoreVatNumberSubscriptionClaimed()

          val res = get(s"/claim-subscription/$testVatNumber")

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.SignUpCompleteClientController.show().url)
          )
        }
      }
    }
    "the user does not have a VATDEC enrolment" when {
      "return a redirect to kown facts page" in {
        enable(BTAClaimSubscription)

        stubAuth(OK, successfulAuthResponse())

        val res = get(s"/claim-subscription/$testVatNumber")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatRegistrationDateController.show().url)
        )
      }
    }
  }

}
