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
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreIdentityVerificationStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.{LimitedCompany, SoleTrader}
import uk.gov.hmrc.vatsignupfrontend.Constants.skipIvJourneyValue

class IdentityVerificationCallbackControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /identity-verified" when {
    "the user selected sole trader as their business entity" should {
      "return an SEE_OTHER to identity verification success" when {
        "user has went through Identity Verification" in {
          stubAuth(OK, successfulAuthResponse())
          stubStoreIdentityVerification(testVatNumber, testUri)(NO_CONTENT)

          val res = get(
            uri = "/identity-verified",
            cookies = Map(
              vatNumberKey -> testVatNumber,
              businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader),
              identityVerificationContinueUrlKey -> testUri
            )
          )

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.IdentityVerificationSuccessController.show().url)
          )
        }
      }
      "return an SEE_OTHER to identity verification success" when {
        "user has skipped Identity Verification" in {
          stubAuth(OK, successfulAuthResponse())
          stubStoreIdentityVerification(testVatNumber, skipIvJourneyValue)(NO_CONTENT)

          val res = get(
            uri = "/identity-verified",
            cookies = Map(
              vatNumberKey -> testVatNumber,
              businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader),
              identityVerificationContinueUrlKey -> skipIvJourneyValue
            )
          )

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.AgreeCaptureEmailController.show().url)
          )
        }
      }
    }

    "the user selected limited company as their business entity" should {
      "return an SEE_OTHER to identity verification success" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreIdentityVerification(testVatNumber, testUri)(NO_CONTENT)

        val res = get(
          uri = "/identity-verified",
          cookies = Map(
            vatNumberKey -> testVatNumber,
            businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedCompany),
            identityVerificationContinueUrlKey -> testUri
          )
        )

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.IdentityVerificationSuccessController.show().url)
        )
      }
    }

    "the user failed identity verification" should {
      "return an SEE_OTHER to failed identity verification" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreIdentityVerification(testVatNumber, testUri)(FORBIDDEN)

        val res = get(
          uri = "/identity-verified",
          cookies = Map(
            vatNumberKey -> testVatNumber,
            businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedCompany),
            identityVerificationContinueUrlKey -> testUri
          )
        )

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.FailedIdentityVerificationController.show().url)
        )
      }
    }
  }

}
