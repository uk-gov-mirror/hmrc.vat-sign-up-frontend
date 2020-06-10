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
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.bta.{routes => btaRoutes}
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.error.{routes => errorRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.ClaimSubscriptionStub.stubClaimSubscription
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class ClaimSubscriptionControllerISpec extends ComponentSpecBase with CustomMatchers{

  "GET /claim-subscription/:vat-number" when {
    "the user has an MTD VRN" should {
      "return a redirect to already claimed error page" in {

        stubAuth(OK, successfulAuthResponse(mtdVatEnrolment))

        val res = get(s"/claim-subscription/$testVatNumber")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.AlreadySignedUpController.show().url)
        )
      }
    }
    "the user has a matching VATDEC enrolment" when {
      "Claim Subscription Service returns subscription claimed" should {
        "return a redirect to the confirmation page" in {

          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubClaimSubscription(testVatNumber, isFromBta = true)(NO_CONTENT)

          val res = get(s"/claim-subscription/$testVatNumber")

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(appConfig.btaRedirectUrl)
          )
        }
      }
      "Claim Subscription Service returns AlreadyEnrolledOnAnotherCredential" should {
        "return a redirect to the business already signed up page" in {

          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubClaimSubscription(testVatNumber, isFromBta = true)(CONFLICT)

          val res = get(s"/claim-subscription/$testVatNumber")

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(errorRoutes.BusinessAlreadySignedUpController.show().url)
          )
        }
      }
    }
    "the user does not have a VATDEC enrolment" when {
      "return a redirect to known facts page" in {

        stubAuth(OK, successfulAuthResponse())

        val res = get(s"/claim-subscription/$testVatNumber")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(btaRoutes.CaptureBtaVatRegistrationDateController.show().url)
        )
      }
    }
  }

}
