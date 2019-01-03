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
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

import scala.concurrent.Future

class ResolvePartnershipUtrControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestResolvePartnershipUtrController extends ResolvePartnershipUtrController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/resolve-partnership-utr")

  "Calling the resolve action of the Resolve Partnership Sautr controller" when {
    "the user has a IR-SA-PART-ORG enrolment and Limited Partnership FS is enabled" should {
      "redirect to confirm limited partnership page" in {
        enable(LimitedPartnershipJourney)
        mockAuthRetrievePartnershipEnrolment()

        val result = TestResolvePartnershipUtrController.resolve(testGetRequest.withSession(
          SessionKeys.companyNameKey -> testCompanyName,
          SessionKeys.companyNumberKey -> testCompanyNumber,
          SessionKeys.partnershipTypeKey -> testPartnershipType
        ))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.ConfirmLimitedPartnershipController.show().url)
        session(result) get SessionKeys.partnershipSautrKey should contain(testSaUtr)
        session(result) get SessionKeys.companyNameKey should contain(testCompanyName)
        session(result) get SessionKeys.companyNumberKey should contain(testCompanyNumber)
        session(result) get SessionKeys.partnershipTypeKey should contain(testPartnershipType)
      }
    }
    "the user has a IR-SA-PART-ORG enrolment and General Partnership FS is enabled" should {
      "redirect to confirm general partnership page" in {
        enable(GeneralPartnershipJourney)
        mockAuthRetrievePartnershipEnrolment()

        val result = TestResolvePartnershipUtrController.resolve(testGetRequest)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.ConfirmGeneralPartnershipController.show().url)
        session(result) get SessionKeys.partnershipSautrKey should contain(testSaUtr)

      }
    }
    "the user has a IR-SA-PART-ORG enrolment but both FS are disabled" should {
      "Return technical difficulties" in {
        disable(GeneralPartnershipJourney)
        disable(LimitedPartnershipJourney)
        mockAuthRetrievePartnershipEnrolment()

        intercept[InternalServerException](await(TestResolvePartnershipUtrController.resolve(testGetRequest)))
      }
    }
  }
  "the user does not have a IR-SA-PART-ORG enrolment and General Partnership FS is enabled" should {
    "go to Capture Partnership SAUTR" in {
      enable(GeneralPartnershipJourney)
      mockAuthorise(
        retrievals = Retrievals.credentialRole and Retrievals.allEnrolments
      )(Future.successful(new ~(Some(Admin), Enrolments(Set()))))

      val result = TestResolvePartnershipUtrController.resolve(testGetRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) should contain(routes.CapturePartnershipUtrController.show().url)
      }
    }

  }

