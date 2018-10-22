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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.LimitedPartnershipJourney
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.forms.ConfirmGeneralPartnershipForm.yesNo
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping.{option_no, option_yes}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StorePartnershipInformationStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType.LimitedPartnership


class ConfirmLimitedPartnershipControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(LimitedPartnershipJourney)
  }

  "GET /confirm-partnership" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/confirm-partnership",
        Map(
          SessionKeys.partnershipSautrKey -> testSaUtr,
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.companyNumberKey -> testCompanyNumber,
          SessionKeys.companyNameKey -> testCompanyName,
          SessionKeys.partnershipTypeKey -> testPartnershipType
        ))

      res should have(
        httpStatus(OK)
      )
    }
  }

  "if feature switch is disabled" should {
    "return a not found" in {
      disable(LimitedPartnershipJourney)

      val res = get("/confirm-partnership")

      res should have(
        httpStatus(NOT_FOUND)
      )
    }
  }

  "POST /confirm-partnership" when {
    "the user answered No" should {
      // TOOD goto the error page once it's defined
      "return INTERNAL_SERVER_ERROR" in {
        stubAuth(OK, successfulAuthResponse())

        val res = post("/confirm-partnership",
          Map(
            SessionKeys.partnershipSautrKey -> testSaUtr,
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.companyNumberKey -> testCompanyNumber,
            SessionKeys.companyNameKey -> testCompanyName,
            SessionKeys.partnershipTypeKey -> testPartnershipType
          ))(yesNo -> option_no)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
    "the user answered Yes" when {
      "the partnership sautr is successfully stored" when {
        "redirect to agree to receive email page" in {
          stubAuth(OK, successfulAuthResponse())
          stubStorePartnershipInformation(testVatNumber, testSaUtr, LimitedPartnership, companyNumber = Some(testCompanyNumber))(NO_CONTENT)

          val res = post("/confirm-partnership",
            Map(
              SessionKeys.partnershipSautrKey -> testSaUtr,
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.companyNumberKey -> testCompanyNumber,
              SessionKeys.companyNameKey -> testCompanyName,
              SessionKeys.partnershipTypeKey -> testPartnershipType
            ))(yesNo -> option_yes)

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(principalRoutes.AgreeCaptureEmailController.show().url)
          )
        }
      }
    }
  }

  "store partnership sautr service returned an error" should {
    "INTERNAL_SERVER_ERROR" in {
      stubAuth(OK, successfulAuthResponse())
      stubStorePartnershipInformation(testVatNumber, testSaUtr, LimitedPartnership, companyNumber = Some(testCompanyNumber))(BAD_REQUEST)

      val res = post("/confirm-partnership",
        Map(
          SessionKeys.partnershipSautrKey -> testSaUtr,
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.companyNumberKey -> testCompanyNumber,
          SessionKeys.companyNameKey -> testCompanyName,
          SessionKeys.partnershipTypeKey -> testPartnershipType
        ))(yesNo -> option_yes)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }
  }

}
