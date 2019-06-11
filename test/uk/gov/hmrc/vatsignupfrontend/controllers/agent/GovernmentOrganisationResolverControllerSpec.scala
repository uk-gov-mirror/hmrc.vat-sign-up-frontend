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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreGovernmentOrganisationInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreGovernmentOrganisationInformationService

import scala.concurrent.Future

class GovernmentOrganisationResolverControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreGovernmentOrganisationInformationService {

  object TestGovernmentOrganisationResolverController extends GovernmentOrganisationResolverController(
    mockControllerComponents,
    mockStoreGovernmentOrganisationInformationService
  )

  lazy val testGetRequest = FakeRequest("GET", "/government-organisation-resolver")

  "calling the resolve method on GovernmentOrganisationController" when {
    "store government organisation information returns StoreGovernmentOrganisationSuccess" should {
      "go to the capture agent email page" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreGovernmentOrganisationInformation(testVatNumber)(Future.successful(Right(StoreGovernmentOrganisationInformationSuccess)))

        val res = await(TestGovernmentOrganisationResolverController.resolve(testGetRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber
        )))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.CaptureAgentEmailController.show().url)
      }
    }
    "store government organisation information returns StoreGovernmentOrganisationFailureResponse" should {
      "throw internal server exception" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreGovernmentOrganisationInformation(testVatNumber)(
          Future.successful(Left(StoreGovernmentOrganisationInformationFailureResponse(INTERNAL_SERVER_ERROR)))
        )

        intercept[InternalServerException] {
          await(TestGovernmentOrganisationResolverController.resolve(testGetRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber
          )))
        }
      }
    }
    "vat number is not in session" should {
      "goto capture vat number" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreGovernmentOrganisationInformation(testVatNumber)(Future.successful(Right(StoreGovernmentOrganisationInformationSuccess)))

        val res = await(TestGovernmentOrganisationResolverController.resolve(testGetRequest))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
  }
}

