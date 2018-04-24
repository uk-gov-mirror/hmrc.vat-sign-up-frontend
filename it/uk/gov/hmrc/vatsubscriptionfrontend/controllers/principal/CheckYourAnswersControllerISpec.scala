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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.principal

import java.time.LocalDate

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.featureswitch.{FeatureSwitching, KnownFactsJourney}
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsubscriptionfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsubscriptionfrontend.models.{DateModel, SoleTrader}

class CheckYourAnswersControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  override def beforeEach(): Unit = enable(KnownFactsJourney)

  override def afterEach(): Unit = disable(KnownFactsJourney)

  val testDate: DateModel = DateModel.dateConvert(LocalDate.now())

  "GET /check-your-answers" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/check-your-answers",
        Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.vatRegistrationDateKey -> Json.toJson(testDate).toString(),
          SessionKeys.businessPostCodeKey -> testBusinessPostCode,
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
        )
      )

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /check-your-answersr" should {
    // TODO update when known facts check is implemented
    "redirect to capture your details" in {
      stubAuth(OK, successfulAuthResponse())

      val res = post("/check-your-answers",
        Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.vatRegistrationDateKey -> Json.toJson(testDate).toString(),
          SessionKeys.businessPostCodeKey -> testBusinessPostCode,
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
        )
      )()

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureYourDetailsController.show().url)
      )

    }
  }


  "Making a request to /check-your-answers when not enabled" should {
    "return NotFound" in {
      disable(KnownFactsJourney)

      stubAuth(OK, successfulAuthResponse())

      val res = get("/check-your-answers")

      res should have(
        httpStatus(NOT_FOUND)
      )

    }
  }

}
