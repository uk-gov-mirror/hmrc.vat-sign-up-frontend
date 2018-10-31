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
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, GeneralPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StorePartnershipInformationStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType.GeneralPartnership

class CheckYourAnswersPartnershipsControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(GeneralPartnershipJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(GeneralPartnershipJourney)
  }

  "GET /check-your-answers-partnership" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/check-your-answers-partnership",
        Map(
          SessionKeys.partnershipTypeKey -> testPartnershipType,
          SessionKeys.partnershipSautrKey -> testSaUtr,
          SessionKeys.partnershipPostCodeKey -> Json.toJson(testBusinessPostCode).toString()
        )
      )

      res should have(
        httpStatus(OK)
      )
    }
    "if feature switch is disabled" should {
      "return a not found" in {
        disable(GeneralPartnershipJourney)

        val res = get("/confirm-partnership-utr")

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }
  }

  "POST /check-your-answers-partnership" when {
    "store partnership information is successful" should {
      "redirect to agree to receive emails" in {
        stubAuth(OK, successfulAuthResponse())
        stubStorePartnershipInformation(
          vatNumber = testVatNumber,
          partnershipEntityType = GeneralPartnership,
          sautr = testSaUtr,
          companyNumber = None,
          postCode = Some(testBusinessPostCode)
        )(NO_CONTENT)

        val res = post("/check-your-answers-partnership",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.partnershipTypeKey -> testPartnershipType,
            SessionKeys.partnershipSautrKey -> testSaUtr,
            SessionKeys.partnershipPostCodeKey -> Json.toJson(testBusinessPostCode).toString()
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(principalRoutes.AgreeCaptureEmailController.show().url)
        )
      }
    }

    "store partnership information failed" should {
      "throw internal server error" in {
        stubAuth(OK, successfulAuthResponse())
        stubStorePartnershipInformation(
          vatNumber = testVatNumber,
          partnershipEntityType = GeneralPartnership,
          sautr = testSaUtr,
          companyNumber = None,
          postCode = Some(testBusinessPostCode)
        )(BAD_REQUEST)

        val res = post("/check-your-answers-partnership",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.partnershipTypeKey -> testPartnershipType,
            SessionKeys.partnershipSautrKey -> testSaUtr,
            SessionKeys.partnershipPostCodeKey -> Json.toJson(testBusinessPostCode).toString()
          )
        )()

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }

  }

}
