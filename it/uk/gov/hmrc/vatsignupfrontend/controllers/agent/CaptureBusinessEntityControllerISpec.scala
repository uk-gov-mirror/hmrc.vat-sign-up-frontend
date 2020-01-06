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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch._
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessEntityForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreOverseasInformationStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers, IntegrationTestConstants, SessionCookieCrumbler}
import uk.gov.hmrc.vatsignupfrontend.models.Overseas


class CaptureBusinessEntityControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def afterAll(): Unit = {
    super.afterAll()
    disable(OptionalSautrJourney)
  }


  "GET /business-type" should {
    "return See Other" when {
      "the user is overseas" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        stubStoreOverseasInformation(testVatNumber)(NO_CONTENT)

        val res = get("/client/business-type", Map(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.businessEntityKey -> Overseas.toString))

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.OverseasResolverController.resolve().url)
        )

        SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.businessEntityKey) should contain(Overseas.toString)

      }
      "the session VRN is an Administrative division" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get("/client/business-type", Map(SessionKeys.vatNumberKey -> administrativeDivisionVRN))

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.DivisionResolverController.resolve().url)
        )
      }
      "no VRN in session" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get("/client/business-type")

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatNumberController.show().url)
        )
      }
    }

    "return an OK" when {
      "the session VRN is not an Administrative division" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get("/client/business-type", Map(SessionKeys.vatNumberKey -> IntegrationTestConstants.testVatNumber))

        res should have(
          httpStatus(OK)
        )
      }
    }

  }

  "POST /business-type" should {
    "redirect to capture company number name" when {
      "the business entity is limited company" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-type")(BusinessEntityForm.businessEntity -> limitedCompany)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureCompanyNumberController.show().url)
        )
      }
    }

    "redirect to capture NINO" when {
      "the business entity is sole trader" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-type")(BusinessEntityForm.businessEntity -> soleTrader)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(soletrader.routes.CaptureNinoController.show().url)
        )
      }
    }

    "redirect to resolve partnership" when {
      "the business entity is general partnership" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-type")(BusinessEntityForm.businessEntity -> generalPartnership)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(partnerships.routes.ResolvePartnershipController.resolve().url)
        )
      }
    }

    "redirect to the resolve partnership page" when {
      "the business entity is limited partnership" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-type")(BusinessEntityForm.businessEntity -> limitedPartnership)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(partnerships.routes.ResolvePartnershipController.resolve().url)
        )
      }
    }

    "redirect to business entity other page" when {
      "the business entity is other" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = post("/client/business-type ")(BusinessEntityForm.businessEntity -> other)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureBusinessEntityOtherController.show().url)
        )
      }
    }
  }
}
