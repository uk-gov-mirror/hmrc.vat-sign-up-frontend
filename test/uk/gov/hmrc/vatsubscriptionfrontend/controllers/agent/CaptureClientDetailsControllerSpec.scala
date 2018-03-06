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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.agent

import java.time.LocalDate
import java.util.UUID

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.forms.UserDetailsForm._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsubscriptionfrontend.models.{DateModel, UserDetailsModel}

class CaptureClientDetailsControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestCaptureClientDetailsController extends CaptureClientDetailsController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/client-details")

  def testPostRequest(userDetails: UserDetailsModel): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/client-details").withFormUrlEncodedBody(
      userFirstName -> userDetails.firstName,
      userLastName -> userDetails.lastName,
      userNino -> userDetails.nino,
      userDateOfBirth + ".day" -> userDetails.dateOfBirth.day,
      userDateOfBirth + ".month" -> userDetails.dateOfBirth.month,
      userDateOfBirth + ".year" -> userDetails.dateOfBirth.year
    )

  val testUserDetails = UserDetailsModel(
    firstName = UUID.randomUUID().toString,
    lastName = UUID.randomUUID().toString,
    nino = testNino,
    dateOfBirth = DateModel.dateConvert(LocalDate.now())
  )

  "Calling the show action of the Capture Client Details controller" should {
    "go to the Capture Client Details page" in {
      mockAuthRetrieveAgentEnrolment()

      val result = TestCaptureClientDetailsController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }


  "Calling the submit action of the Capture Client Details controller" when {
    "form successfully submitted" should {
      "redirect to Confirm Client Details page" in {
        mockAuthRetrieveAgentEnrolment()

        val request = testPostRequest(testUserDetails)

        val result = TestCaptureClientDetailsController.submit(request)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.ConfirmClientDetailsController.show().url)

        Json.parse(result.session(request).get(SessionKeys.userDetailsKey).get).validate[UserDetailsModel].get shouldBe testUserDetails
      }
    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCaptureClientDetailsController.submit(testPostRequest(testUserDetails.copy(nino = "")))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}