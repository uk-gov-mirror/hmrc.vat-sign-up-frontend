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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.JointVenturePropertyJourney
import uk.gov.hmrc.vatsignupfrontend.forms.JointVentureOrPropertyForm
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers, SessionCookieCrumbler}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.testSaUtr

class JointVenturePropertyControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    stubAuth(OK, successfulAuthResponse(agentEnrolment))
    enable(JointVenturePropertyJourney)
  }

  "GET /client/joint-venture-or-property-partnership" should {
    "return an OK" in {
      val res = get("/client/joint-venture-or-property-partnership")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /joint-venture-or-property-partnership" when {
    "form value is YES" should {
      "redirect to Check Your Answers Partnership page" in {
        val res = post(
          uri = "/client/joint-venture-or-property-partnership",
          cookies = Map(SessionKeys.partnershipSautrKey -> testSaUtr)
        )(JointVentureOrPropertyForm.yesNo -> option_yes)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersPartnershipController.show().url)
        )

        SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.partnershipSautrKey) shouldBe None
        SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.jointVentureOrPropertyKey) shouldBe Some(true.toString)
      }
    }

    "form value is NO" should {
      "redirect to Choose Software error page" in {
        val res = post("/client/joint-venture-or-property-partnership")(
          JointVentureOrPropertyForm.yesNo -> option_no
        )

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CapturePartnershipUtrController.show().url)
        )

        SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.jointVentureOrPropertyKey) shouldBe Some(false.toString)
      }
    }
  }
}
