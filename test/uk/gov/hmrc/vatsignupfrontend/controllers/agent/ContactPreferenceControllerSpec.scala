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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.FinalCheckYourAnswer
import uk.gov.hmrc.vatsignupfrontend.controllers.ControllerSpec
import uk.gov.hmrc.vatsignupfrontend.forms.ContactPreferencesForm
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.{Digital, Paper}
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreContactPreferenceService

class ContactPreferenceControllerSpec extends ControllerSpec with MockStoreContactPreferenceService {

  object TestContactPreferenceController extends ContactPreferenceController(mockControllerComponents, mockStoreContactPreferenceService)

  lazy val testGetRequest = FakeRequest("GET", "/client/receive-email-notifications")
  lazy val testPostRequest = FakeRequest("POST", "/client/receive-email-notifications")

  "Calling the show action of the Contact Preference controller" should {

    lazy val result = TestContactPreferenceController.show(testGetRequest)

    "return status OK (200)" in {
      mockAuthRetrieveAgentEnrolment()
      status(result) shouldBe Status.OK
    }

    "have the html as the content type" in {
      contentType(result) shouldBe Some("text/html")
    }

    "return the charset utf-8" in {
      charset(result) shouldBe Some("utf-8")
    }

    "render the receive email notifications view" in {
      titleOf(result) shouldBe MessageLookup.AgentReceiveEmailNotifications.title
    }

    s"have a form which submits to '${routes.ContactPreferenceController.submit().url}'" in {
      formAction(result) shouldBe routes.ContactPreferenceController.submit().url
    }
  }

  "Calling the submit action of the Contact Preference controller" when {

    "vat number is in session" when {

      "call to store contact preference is successful" when {

        "contact preference is Digital" should {

          "redirect to Capture Client Email page" in {
            mockAuthRetrieveAgentEnrolment()
            mockStoreContactPreferenceSuccess(vatNumber = testVatNumber, contactPreference = Digital)

            val request = testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber)
              .withFormUrlEncodedBody(ContactPreferencesForm.contactPreference -> ContactPreferencesForm.digital)

            val result = TestContactPreferenceController.submit()(request)

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.CaptureClientEmailController.show().url)
            result.session(request).get(SessionKeys.contactPreferenceKey) shouldBe Some(Digital.toString)
          }
        }

        "contact preference is Paper" when {

          "user has direct debit" should {

            "redirect to Capture Client Email page" in {
              mockAuthRetrieveAgentEnrolment()
              mockStoreContactPreferenceSuccess(vatNumber = testVatNumber, contactPreference = Paper)

              val request = testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.hasDirectDebitKey -> "true")
                .withFormUrlEncodedBody(ContactPreferencesForm.contactPreference -> ContactPreferencesForm.paper)

              val result = TestContactPreferenceController.submit()(request)

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) shouldBe Some(routes.CaptureClientEmailController.show().url)
              result.session(request).get(SessionKeys.contactPreferenceKey) shouldBe Some(Paper.toString)
            }
          }

          "user does not have a direct debit" should {

            "redirect to AgentSendYourApplication page" when {
              "the final check your answers feature switch is disabled" in {
                disable(FinalCheckYourAnswer)
                mockAuthRetrieveAgentEnrolment()
                mockStoreContactPreferenceSuccess(vatNumber = testVatNumber, contactPreference = Paper)

                val request = testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber)
                  .withFormUrlEncodedBody(ContactPreferencesForm.contactPreference -> ContactPreferencesForm.paper)

                val result = TestContactPreferenceController.submit()(request)

                status(result) shouldBe Status.SEE_OTHER
                redirectLocation(result) shouldBe Some(routes.AgentSendYourApplicationController.show().url)
              }
            }

            "redirect to the final check your answers page when the feature switch is enabled" in {
              enable(FinalCheckYourAnswer)
              mockAuthRetrieveAgentEnrolment()
              mockStoreContactPreferenceSuccess(vatNumber = testVatNumber, contactPreference = Paper)

              val request = testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber)
                .withFormUrlEncodedBody(ContactPreferencesForm.contactPreference -> ContactPreferencesForm.paper)

              val result = TestContactPreferenceController.submit()(request)

              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) shouldBe Some(routes.CheckYourAnswersFinalController.show().url)
            }
          }

        }
      }

      "call to store contact preference is NOT successful" when {

        "redirect to Capture Client Email page" in {
          mockAuthRetrieveAgentEnrolment()
          mockStoreContactPreferenceFailure(vatNumber = testVatNumber, contactPreference = Digital)

          val request = testPostRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber)
            .withFormUrlEncodedBody(ContactPreferencesForm.contactPreference -> ContactPreferencesForm.digital)

          val result = TestContactPreferenceController.submit()(request)

          intercept[InternalServerException](await(TestContactPreferenceController.submit(request)))
        }
      }
    }

    "vat number is NOT in session" should {

      "redirect to Capture Client VRN page" in {
        mockAuthRetrieveAgentEnrolment()

        val request = testPostRequest.withFormUrlEncodedBody(ContactPreferencesForm.contactPreference -> ContactPreferencesForm.digital)

        val result = TestContactPreferenceController.submit()(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
  }
}
