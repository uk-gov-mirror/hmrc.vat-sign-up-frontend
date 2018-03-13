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
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.servicemocks.StoreIdentityVerificationStub._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsubscriptionfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsubscriptionfrontend.models.{LimitedCompany, SoleTrader}

class IdentityVerificationCallbackControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /identity-verified" when {
    "the user selected sole trader as their business entity" should {
      "return an SEE_OTHER to agree to capture email" in {
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
          redirectUri(routes.AgreeCaptureEmailController.show().url)
        )
      }
    }

    "the user selected sole trader as their business entity" should {
      "return an SEE_OTHER to capture company number" in {
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
          redirectUri(routes.CaptureCompanyNumberController.show().url)
        )
      }
    }
  }

}
