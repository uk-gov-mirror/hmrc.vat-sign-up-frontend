/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.partnerships

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.auth.core.{Admin, Enrolments}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.JointVenturePropertyJourney
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.{GeneralPartnership, LimitedPartnership}

import scala.concurrent.Future

class ResolvePartnershipUtrControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestResolvePartnershipUtrController extends ResolvePartnershipUtrController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/resolve-partnership-utr")

  "Calling the resolve action of the Resolve Partnership Sautr controller" when {
    "the user has a IR-SA-PART-ORG enrolment" when {
      "the user is a Limited Partnership" should {
        "redirect to confirm limited partnership page" in {
          mockAuthRetrievePartnershipEnrolment()

          val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedPartnership)
          ))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.ConfirmLimitedPartnershipController.show().url)
          session(result) get SessionKeys.partnershipSautrKey should contain(testSaUtr)
        }
      }
      "the user is a General Partnership" should {
        "redirect to confirm general partnership page" in {
          mockAuthRetrievePartnershipEnrolment()

          val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership)
          ))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.ConfirmGeneralPartnershipController.show().url)
          session(result) get SessionKeys.partnershipSautrKey should contain(testSaUtr)

        }
      }
    }
    "the user does not have a IR-SA-PART-ORG enrolment" when {
      "the joint venture feature switch is enabled" when {
        "the user is a General Partnership" should {
          "go to the joint venture page" in {
            enable(JointVenturePropertyJourney)

            mockAuthorise(
              retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
            )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

            val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
              SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(GeneralPartnership)
            ))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.JointVentureOrPropertyController.show().url)
          }
        }
        "the user is a Limited Partnership" should {
          "go to the capture partnership UTR page" in {
            enable(JointVenturePropertyJourney)

            mockAuthorise(
              retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
            )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

            val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
              SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedPartnership)
            ))

            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.CapturePartnershipUtrController.show().url)
          }
        }
      }
      "the joint venture feature switch is disabled" should {
        "go to the capture partnership UTR page" in {

          mockAuthorise(
            retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
          )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

          val result = TestResolvePartnershipUtrController.resolve(testGetRequest)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.CapturePartnershipUtrController.show().url)
        }
      }
    }
  }
}

