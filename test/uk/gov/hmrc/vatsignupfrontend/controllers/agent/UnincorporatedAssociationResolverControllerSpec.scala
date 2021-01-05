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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockVatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreUnincorporatedAssociationInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreUnincorporatedAssociationInformationService
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec

import scala.concurrent.Future

class UnincorporatedAssociationResolverControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockVatControllerComponents with MockStoreUnincorporatedAssociationInformationService {

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  object TestUnincorporatedAssociationResolverController extends UnincorporatedAssociationResolverController(mockStoreUnincorporatedAssociationInformationService)

  lazy val testGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/unincorporated-association-resolver")

  "calling the resolve method on UnincorporatedAssociationResolverController" when {
    "store unincorporated association information returns StoreUnincorporatedAssociationInformationSuccess" should {
      "go to the capture agent email page" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreUnincorporatedAssociationInformation(testVatNumber)(
          Future.successful(Right(StoreUnincorporatedAssociationInformationSuccess))
        )

        val res = TestUnincorporatedAssociationResolverController.resolve(testGetRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber
        ))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.CaptureAgentEmailController.show().url)
      }
    }
    "store unincorporated association information fails" should {
      "throw internal server exception" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreUnincorporatedAssociationInformation(testVatNumber)(
          Future.successful(Left(StoreUnincorporatedAssociationInformationFailureResponse(INTERNAL_SERVER_ERROR)))
        )

        intercept[InternalServerException] {
          TestUnincorporatedAssociationResolverController.resolve(testGetRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber
          ))
        }
      }
    }
    "vat number is not in session" should {
      "goto resolve vat number" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreUnincorporatedAssociationInformation(testVatNumber)(Future.successful(Right(StoreUnincorporatedAssociationInformationSuccess)))

        val res = TestUnincorporatedAssociationResolverController.resolve(testGetRequest)

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
  }

}
