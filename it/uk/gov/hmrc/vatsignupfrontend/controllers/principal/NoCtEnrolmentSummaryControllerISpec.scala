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
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, CtKnownFactsIdentityVerification}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreCompanyNumberStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.SoleTrader

class NoCtEnrolmentSummaryControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  override def beforeEach(): Unit = enable(CtKnownFactsIdentityVerification)

  override def afterEach(): Unit = disable(CtKnownFactsIdentityVerification)

  "GET /check-your-answers-company" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/check-your-answers-company",
        Map(
          SessionKeys.companyNumberKey -> testCompanyNumber,
          SessionKeys.companyUtrKey -> testCompanyUtr,
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
        )
      )

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /check-your-answers-company" when {
    "store CRN and CTUTR is successful" should {
      "redirect to agree capture email" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreCompanyNumberSuccess(testVatNumber, testCompanyNumber, Some(testCompanyUtr))

        val res = post("/check-your-answers-company",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.companyNumberKey -> testCompanyNumber,
            SessionKeys.companyUtrKey -> testCompanyUtr,
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.AgreeCaptureEmailController.show().url)
        )
      }
    }

    "Making a request to No CT Enrolment Summary when not enabled" should {
      "return NotFound" in {
        disable(CtKnownFactsIdentityVerification)

        stubAuth(OK, successfulAuthResponse())

        val res = get("/check-your-answers-company")

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }
  }
}
