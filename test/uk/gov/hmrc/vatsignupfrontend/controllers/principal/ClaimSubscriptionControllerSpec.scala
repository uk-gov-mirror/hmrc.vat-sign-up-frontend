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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.auth.core.{Admin, Enrolments}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.vatNumberKey
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.BTAClaimSubscription
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstantsGenerator
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreVatNumberService
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.bta.{routes => btaRoutes}

import scala.concurrent.Future

class ClaimSubscriptionControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents with MockStoreVatNumberService {

  object TestClaimSubscriptionController extends ClaimSubscriptionController(mockControllerComponents, mockStoreVatNumberService)

  lazy val testGetRequest = FakeRequest("GET", "/claim-subscription")

  "show" when {
    "the BTA claim subscription feature switch is enabled" when {
      "the user has a VATDEC enrolment" when {
        "the VAT number on the enrolment matches the one provided in the URL" when {
          "store VAT returns SubscriptionClaimed" should {
            "redirect to SignUpCompleteClientController" in {
              enable(BTAClaimSubscription)

              mockAuthRetrieveVatDecEnrolment()
              mockStoreVatNumberSubscriptionClaimed(testVatNumber, isFromBta = Some(true))

              val result = TestClaimSubscriptionController.show(testVatNumber)(testGetRequest)

              status(result) shouldBe SEE_OTHER
              redirectLocation(result) should contain(mockAppConfig.btaRedirectUrl)
            }
          }
          "store VAT returns anything else" should {
            "throw an Internal Server Exception" in {
              enable(BTAClaimSubscription)

              mockAuthRetrieveVatDecEnrolment()
              mockStoreVatNumberInvalid(testVatNumber, isFromBta = Some(true))

              val result = TestClaimSubscriptionController.show(testVatNumber)(testGetRequest)

              intercept[InternalServerException](await(result))
            }
          }
        }
        "the VAT number on the enrolment does not match the one provided in the URL" should {
          "throw an Internal Server Exception" in {
            enable(BTAClaimSubscription)

            mockAuthRetrieveVatDecEnrolment()

            val nonMatchingVatNumber = TestConstantsGenerator.randomVatNumber

            val result = TestClaimSubscriptionController.show(nonMatchingVatNumber)(testGetRequest)

            intercept[InternalServerException](await(result))
          }
        }
      }
      "the user does not have a VATDEC enrolment" when {
        "the VAT number is valid" should {
          "redirect to BTA Capture VAT registration date" in {
            enable(BTAClaimSubscription)

            mockAuthorise(
              retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
            )(Future.successful(new ~(Some(Admin), Enrolments(Set.empty))))

            val result = TestClaimSubscriptionController.show(testVatNumber)(testGetRequest)

            status(result) shouldBe SEE_OTHER
            redirectLocation(result) should contain(btaRoutes.CaptureBtaVatRegistrationDateController.show().url)

            val session = await(result).session(testGetRequest)

            session.get(vatNumberKey) should contain(testVatNumber)
          }
        }
        "the VAT number is invalid" should {
          "throw an Internal Server Exception" in {
            enable(BTAClaimSubscription)

            mockAuthorise(
              retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
            )(Future.successful(new ~(Some(Admin), Enrolments(Set.empty))))

            val invalidVatNumber = "1"

            val result = TestClaimSubscriptionController.show(invalidVatNumber)(testGetRequest)

            intercept[InternalServerException](await(result))
          }
        }
      }
    }
  }

}
