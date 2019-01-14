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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.RegisteredSocietyJourney
import uk.gov.hmrc.vatsignupfrontend.forms.RegisteredSocietyUtrForm
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class CaptureRegisteredSocietyUtrControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(RegisteredSocietyJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(RegisteredSocietyJourney)
  }

  "GET /registered-society-utr" when {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/registered-society-utr")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /registered-society-utr" when {
    "redirect to No CT enrolment" in {
      stubAuth(OK, successfulAuthResponse())

      val res = post("/registered-society-utr")(RegisteredSocietyUtrForm.registeredSocietyUtr -> testCompanyUtr)

      res should have(
        httpStatus(NOT_IMPLEMENTED)
          //redirectUri(routes.RegisteredSocietyCheckYourAnswers.show().url)
          //TODO implement redirect to registered society check your answers
      )
    }
  }

}
