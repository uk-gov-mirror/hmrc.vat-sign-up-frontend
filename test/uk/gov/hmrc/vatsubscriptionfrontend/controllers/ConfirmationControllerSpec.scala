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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstants._

class ConfirmationControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestConfirmationController extends ConfirmationController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/information-received")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/information-received")

  "Calling the show action of the Confirmation controller" should {
    "show the Confirmation page" in {
      mockAuthRetrieveAgentEnrolment()
      val request = testGetRequest

      val result = TestConfirmationController.show(request)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Confirmation controller" should {
    // todo
    "return not implemented" in {
      mockAuthRetrieveAgentEnrolment()

      val request = testPostRequest.withSession(
        SessionKeys.vatNumberKey -> testVatNumber,
        SessionKeys.companyNumberKey -> testCompanyNumber,
        SessionKeys.emailKey -> testEmail
      )

      val result = TestConfirmationController.submit(request)
      status(result) shouldBe Status.NOT_IMPLEMENTED

      val session = await(result).session(request)
      session.get(SessionKeys.vatNumberKey) shouldBe None
      session.get(SessionKeys.companyNumberKey) shouldBe None
      session.get(SessionKeys.emailKey) shouldBe None
    }
  }

}
