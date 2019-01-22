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
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.RegisteredSociety

class CtEnrolmentDetailsDoNotMatchControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /error/details-do-not-match" should {
    "return an OK" in {

      stubAuth(OK, successfulAuthResponse())

      val res = get("/error/details-do-not-match")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /error/details-do-not-match" when {
    "Registered Society business entity is in session" should {
      "redirect to the capture vat number page" in {

        stubAuth(OK, successfulAuthResponse())

        val res = post("/error/details-do-not-match",
          Map(SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(RegisteredSociety))
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureRegisteredSocietyCompanyNumberController.show().url)
        )
      }
    }
  }
}
