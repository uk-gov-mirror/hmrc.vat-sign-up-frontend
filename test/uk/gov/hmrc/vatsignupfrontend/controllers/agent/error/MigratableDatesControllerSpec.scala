/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.error

import java.time.LocalDate

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockVatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.{routes => agentRoutes}
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates._
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.{sign_up_after_this_date, sign_up_between_these_dates}

class MigratableDatesControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockVatControllerComponents {

  object TestMigratableDatesController extends MigratableDatesController

  def testGetRequest(date: Option[LocalDate] = None, cutoffDate: Option[LocalDate] = None): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/client/error/sign-up-later").withSession(migratableDatesKey -> Json.toJson(MigratableDates(date, cutoffDate)).toString())

  "Calling the show action of the migratable dates controller" should {
    "redirect to the capture vat number page" when {
      "no migratable dates are available" in {
        mockAuthRetrieveAgentEnrolment()
        val request = testGetRequest()

        val result = TestMigratableDatesController.show(request)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(agentRoutes.CaptureVatNumberController.show().url)
      }
    }

    "show the sign up after this date page" when {
      "one migratable date is available" in {
        mockAuthRetrieveAgentEnrolment()
        val testDate = LocalDate.now()
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = testGetRequest(date = Some(testDate))
        implicit val messages: Messages = mockVatControllerComponents.controllerComponents.messagesApi.preferred(request)

        val result = TestMigratableDatesController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")

        contentAsString(result) shouldBe sign_up_after_this_date(testDate).body
      }
    }

    "show the sign up between these dates page" when {
      "two migratable dates are available" in {
        mockAuthRetrieveAgentEnrolment()
        val testDate = LocalDate.now()
        val testDate2 = testDate.plusDays(1)

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = testGetRequest(Some(testDate), Some(testDate2))
        implicit val messages: Messages = mockVatControllerComponents.controllerComponents.messagesApi.preferred(request)

        val result = TestMigratableDatesController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")

        contentAsString(result) shouldBe sign_up_between_these_dates(testDate, testDate2).body
      }
    }
  }

}
