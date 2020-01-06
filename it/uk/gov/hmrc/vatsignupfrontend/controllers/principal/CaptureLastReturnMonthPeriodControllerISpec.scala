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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.AdditionalKnownFacts
import uk.gov.hmrc.vatsignupfrontend.forms.MonthForm
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.February

class CaptureLastReturnMonthPeriodControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(AdditionalKnownFacts)
  }

  "GET /last-vat-return-date" when {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/last-vat-return-date")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /last-vat-return-date" when {
    "redirect to Check Your Answers page" in {
      stubAuth(OK, successfulAuthResponse())

      val res = post("/last-vat-return-date")(MonthForm.month -> February.toString)

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CheckYourAnswersController.show().url)
      )
    }
  }

  "the AdditionalKNownFacts feature switch is disabled" should {
    "return a Not Found" in {
      disable(AdditionalKnownFacts)
      stubAuth(OK, successfulAuthResponse())

      val res = get("/last-vat-return-date")

      res should have(
        httpStatus(NOT_FOUND)
      )
    }
  }

}
