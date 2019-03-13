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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{JointVenturePropertyJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.forms.{HaveSoftwareForm, JointVentureOrPropertyForm}
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub.{stubAuth, successfulAuthResponse}
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers, SessionCookieCrumbler}

class JointVenturePropertyControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    stubAuth(OK, successfulAuthResponse())
    enable(JointVenturePropertyJourney)
  }

  "GET /joint-venture-or-property-partnership" should {

    "return an OK" in {

      val res = get("/joint-venture-or-property-partnership")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /joint-venture-or-property-partnership" when {

    "form value is YES" should {

      "redirect to Check Your Answers Partnership page" in {

        val res = post("/joint-venture-or-property-partnership", Map(SessionKeys.partnershipSautrKey -> "utr"))(JointVentureOrPropertyForm.yesNo -> option_yes)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersPartnershipsController.show().url)
        )

        SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.partnershipSautrKey) shouldBe None
      }
    }

    "form value is NO" should {

      "redirect to Choose Software error page" in {

        val res = post("/joint-venture-or-property-partnership")(JointVentureOrPropertyForm.yesNo -> option_no)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CapturePartnershipUtrController.show().url)
        )
      }
    }
  }
}
