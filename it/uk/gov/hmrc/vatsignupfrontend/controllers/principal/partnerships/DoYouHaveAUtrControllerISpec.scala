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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.OptionalSautrJourney
import uk.gov.hmrc.vatsignupfrontend.forms.DoYouHaveAUtrForm
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping._
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.{testBusinessPostCode, testSaUtr}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub.{stubAuth, successfulAuthResponse}
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class DoYouHaveAUtrControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(OptionalSautrJourney)
    stubAuth(OK, successfulAuthResponse())
  }

  "GET /do-you-have-a-utr" should {
    "return an OK" in {
      val res = get("/do-you-have-a-utr")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /do-you-have-a-utr" when {
    "form value is Yes" should {
      "redirect to CapturePartnershipUtr page" in {
        val res = post("/do-you-have-a-utr")(DoYouHaveAUtrForm.yesNo -> option_yes)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CapturePartnershipUtrController.show().url)
        )

        getSessionMap(res).get(SessionKeys.hasOptionalSautrKey) shouldBe Some(true.toString)
      }
    }

    "form value is NO" should {
      "redirect to Check Your Answers Partnership page" in {
        val res = post(
          uri = "/do-you-have-a-utr",
          cookies = Map(SessionKeys.partnershipSautrKey -> testSaUtr,
            SessionKeys.partnershipPostCodeKey -> testBusinessPostCode.postCode
          )
        )(DoYouHaveAUtrForm.yesNo -> option_no)

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersPartnershipsController.show().url)
        )

        getSessionMap(res).get(SessionKeys.partnershipSautrKey) shouldBe None
        getSessionMap(res).get(SessionKeys.partnershipPostCodeKey) shouldBe None
        getSessionMap(res).get(SessionKeys.hasOptionalSautrKey) shouldBe Some(false.toString)
      }
    }
  }
}
