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
import uk.gov.hmrc.vatsignupfrontend.models.{LimitedCompany, Other, SoleTrader}

class IdentityVerificationSuccessControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /confirmed-identity" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/confirmed-identity")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /confirmed-identity" when {
    "the user selected sole trader as their business entity" should {
      "return an SEE_OTHER to agree to receive emails" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreIdentityVerification(testVatNumber, testUri)(NO_CONTENT)

        val res = post(
          uri = "/confirmed-identity",
          cookies = Map(
            businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.AgreeCaptureEmailController.show().url)
        )
      }
    }

    "the user selected limited company as their business entity" should {
      "return an SEE_OTHER to capture company number" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreIdentityVerification(testVatNumber, testUri)(NO_CONTENT)

        val res = post(
          uri = "/confirmed-identity",
          cookies = Map(
            businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedCompany)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureCompanyNumberController.show().url)
        )
      }
    }

    "the user selected other as their business entity" should {
      "return an SEE_OTHER to capture business entity" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreIdentityVerification(testVatNumber, testUri)(FORBIDDEN)

        val res = post(
          uri = "/confirmed-identity",
          cookies = Map(
            businessEntityKey -> BusinessEntitySessionFormatter.toString(Other)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureBusinessEntityController.show().url)
        )
      }
    }
  }
}
