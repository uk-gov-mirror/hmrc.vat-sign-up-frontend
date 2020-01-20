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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.error

import play.api.http.Status.{OK, SEE_OTHER}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.testCompanyName
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub.{stubAuth, successfulAuthResponse}
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.LimitedCompany

class DissolvedCompanyControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /error/dissolved-company" should {
    "return an OK if there is a company name in session" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/error/dissolved-company", Map(
        SessionKeys.companyNameKey -> testCompanyName
      ))

      res should have(
        httpStatus(OK)
      )
    }
    "return a SEE OTHER with a redirect to the correct capture company number page if company name is not in session" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/error/dissolved-company", Map(
        SessionKeys.businessEntityKey -> LimitedCompany.toString
      ))

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(principalRoutes.CaptureCompanyNumberController.show().url)
      )
    }
    "return a SEE OTHER with a redirect to business entity page if there is no company name and entity in session" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/error/dissolved-company")

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(principalRoutes.CaptureBusinessEntityController.show().url)
      )
    }
  }
}
