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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.{routes => agentRoutes}

class CouldNotConfirmPartnershipControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def afterEach(): Unit = {
    super.afterEach()
    disable(LimitedPartnershipJourney)
    disable(GeneralPartnershipJourney)
  }

  "GET /client/error/could-not-confirm-partnership" should {
    "return an OK" in {
      enable(LimitedPartnershipJourney)
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/error/could-not-confirm-partnership")

      res should have(
        httpStatus(OK)
      )
    }

    "return NOT_FOUND if both feature switches are off" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = get("/client/error/could-not-confirm-partnership")

      res should have(
        httpStatus(NOT_FOUND)
      )
    }
  }

  "POST /client/error/could-not-confirm-partnership" should {
    "redirect to the capture vat number page" in {
      enable(LimitedPartnershipJourney)

      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = post("/client/error/could-not-confirm-partnership")()

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(agentRoutes.CaptureBusinessEntityController.show().url)
      )
    }

    "return NOT_FOUND if both feature switches are off" in {
      stubAuth(OK, successfulAuthResponse(agentEnrolment))

      val res = post("/client/error/could-not-confirm-partnership")()

      res should have(
        httpStatus(NOT_FOUND)
      )
    }
  }

}
