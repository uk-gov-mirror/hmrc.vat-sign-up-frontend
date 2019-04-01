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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.OptionalSautrJourney
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.{GeneralPartnership, LimitedCompany, LimitedPartnership}

class ResolvePartnershipControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /resolve-partnership" when {
    "the user is a general partnership and the Optional SA UTR feature switch is disabled" should {
      "redirect to the capture partnership utr page" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        disable(OptionalSautrJourney)

        val res = get("/client/resolve-partnership", Map(
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership)
        ))

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CapturePartnershipUtrController.show().url)
        )
      }
    }
    "the user is a general partnership and the Optional SA UTR feature switch is enabled" should {
      "redirect to the does your client have a UTR page" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))
        enable(OptionalSautrJourney)

        val res = get("/client/resolve-partnership", Map(
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership)
        ))

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.DoesYourClientHaveAUtrController.show().url)
        )
      }
    }
    "user is a limited partnership " should {
      "redirect to the capture partnership company number page" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get("/client/resolve-partnership", Map(
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedPartnership)
        ))

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.AgentCapturePartnershipCompanyNumberController.show().url)
        )
      }
    }
    "user is not a partnership entity" should {
      "throw internal server exception" in {
        stubAuth(OK, successfulAuthResponse(agentEnrolment))

        val res = get("/client/resolve-partnership", Map(
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedCompany)
        ))

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }
}
