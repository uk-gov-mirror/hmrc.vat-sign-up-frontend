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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch._
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreCompanyNumberStub.stubStoreCompanyNumberSuccess
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class ConfirmCompanyControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(CtKnownFactsIdentityVerification)
  }

  "GET /confirm-company" when {
    "CompanyNameJourney is disabled" should {
      "return an NOT_FOUND" in {
        disable(CompanyNameJourney)

        val res = get("/confirm-company")

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }
    "CompanyNameJourney is enabled" should {
      "return an OK" in {
        enable(CompanyNameJourney)

        stubAuth(OK, successfulAuthResponse())

        val res = get("/confirm-company", Map(SessionKeys.companyNameKey -> testCompanyName))

        res should have(
          httpStatus(OK)
        )
      }
    }
  }

  "POST /confirm-company" should {

    "the company number is successfully stored" when {
      "IRCT Journey is enabled" should {
        "redirect to agree to receive email page" in {
          enable(IRCTJourney)
          enable(CompanyNameJourney)

          stubAuth(OK, successfulAuthResponse(irctEnrolment))
          stubStoreCompanyNumberSuccess(
            vatNumber = testVatNumber,
            companyNumber = testCompanyNumber,
            companyUtr = Some(testSaUtr)
          )

          val res = post("/confirm-company",
            Map(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.companyNumberKey -> testCompanyNumber
            ))()

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.AgreeCaptureEmailController.show().url)
          )

        }

      }
      "IRCT Journey is disabled" when {
        "CtKnownFactsIdentityVerification is disabled" should {
          "redirect to agree to receive email page" in {
            disable(IRCTJourney)
            enable(CompanyNameJourney)
            disable(CtKnownFactsIdentityVerification)

            stubAuth(OK, successfulAuthResponse())
            stubStoreCompanyNumberSuccess(
              vatNumber = testVatNumber,
              companyNumber = testCompanyNumber,
              companyUtr = None
            )

            val res = post("/confirm-company",
              Map(
                SessionKeys.vatNumberKey -> testVatNumber,
                SessionKeys.companyNumberKey -> testCompanyNumber
              ))()

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.AgreeCaptureEmailController.show().url)
            )

          }
        }

        "CtKnownFactsIdentityVerification is enabled" should {
          "redirect to capture company UTR page" in {
            disable(IRCTJourney)
            enable(CompanyNameJourney)
            enable(CtKnownFactsIdentityVerification)

            stubAuth(OK, successfulAuthResponse())

            val res = post("/confirm-company",
              Map(
                SessionKeys.vatNumberKey -> testVatNumber,
                SessionKeys.companyNumberKey -> testCompanyNumber
              ))()

            res should have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.CaptureCompanyUtrController.show().url)
            )
          }
        }

      }
    }
  }

}
