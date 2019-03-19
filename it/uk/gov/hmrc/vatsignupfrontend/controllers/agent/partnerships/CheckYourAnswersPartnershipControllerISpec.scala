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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, GeneralPartnershipJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.{routes => agentRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StorePartnershipInformationStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType.CompanyTypeSessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.{GeneralPartnership, LimitedPartnership, PartnershipEntityType, PostCode}
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.jsonSessionFormatter

class CheckYourAnswersPartnershipControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(GeneralPartnershipJourney)
    enable(LimitedPartnershipJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(GeneralPartnershipJourney)
    disable(LimitedPartnershipJourney)
  }

  "GET /client/check-your-answers" should {
    "return an OK for general partnership" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/check-your-answers",
        Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.partnershipSautrKey -> testSaUtr,
          SessionKeys.businessEntityKey -> GeneralPartnership.toString,
          SessionKeys.partnershipPostCodeKey -> jsonSessionFormatter[PostCode].toString(testBusinessPostCode)
        )
      )

      res should have(
        httpStatus(OK)
      )
    }

    "return an OK for limited partnership" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/check-your-answers",
        Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.partnershipSautrKey -> testSaUtr,
          SessionKeys.partnershipPostCodeKey -> jsonSessionFormatter[PostCode].toString(testBusinessPostCode),
          SessionKeys.companyNumberKey -> testCompanyNumber,
          SessionKeys.businessEntityKey -> LimitedPartnership.toString,
          SessionKeys.partnershipTypeKey -> CompanyTypeSessionFormatter.toString(PartnershipEntityType.LimitedPartnership)
        )
      )

      res should have(
        httpStatus(OK)
      )
    }

    "return an NOT FOUND" in {
      disable(GeneralPartnershipJourney)
      disable(LimitedPartnershipJourney)

      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/check-your-answers")

      res should have(
        httpStatus(NOT_FOUND)
      )
    }
  }

  "POST /client/partnership-check-your-answers" when {
    "store partnership is successful" should {
      "redirect to capture email" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStorePartnershipInformation(
          testVatNumber,
          testSaUtr,
          PartnershipEntityType.GeneralPartnership,
          None,
          Some(testBusinessPostCode)
        )(NO_CONTENT)

        val res = post("/client/check-your-answers",
          Map(
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership),
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.partnershipSautrKey -> testSaUtr,
            SessionKeys.partnershipPostCodeKey -> jsonSessionFormatter[PostCode].toString(testBusinessPostCode)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(agentRoutes.EmailRoutingController.route().url)
        )
      }
    }

    "store partnership returned forbidden" should {
      "redirect to capture email" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStorePartnershipInformation(
          testVatNumber,
          testSaUtr,
          PartnershipEntityType.GeneralPartnership,
          None,
          Some(testBusinessPostCode)
        )(FORBIDDEN)

        val res = post("/client/check-your-answers",
          Map(
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership),
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.partnershipSautrKey -> testSaUtr,
            SessionKeys.partnershipPostCodeKey -> jsonSessionFormatter[PostCode].toString(testBusinessPostCode)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CouldNotConfirmPartnershipController.show().url)
        )
      }
    }

    "store partnership is successful for a limited partnership" should {
      "redirect to capture email" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStorePartnershipInformation(
          testVatNumber,
          testSaUtr,
          PartnershipEntityType.LimitedPartnership,
          Some(testCompanyNumber),
          Some(testBusinessPostCode)
        )(NO_CONTENT)

        val res = post("/client/check-your-answers",
          Map(
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedPartnership),
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.partnershipSautrKey -> testSaUtr,
            SessionKeys.partnershipPostCodeKey -> jsonSessionFormatter[PostCode].toString(testBusinessPostCode),
            SessionKeys.companyNumberKey -> testCompanyNumber,
            SessionKeys.partnershipTypeKey -> CompanyTypeSessionFormatter.toString(PartnershipEntityType.LimitedPartnership)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(agentRoutes.EmailRoutingController.route().url)
        )
      }
    }
  }

}
