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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships

import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch._
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.{routes => agentRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

class ConfirmPartnershipControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(LimitedPartnershipJourney)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    disable(LimitedPartnershipJourney)
  }

  object TestConfirmPartnershipController extends ConfirmPartnershipController(mockControllerComponents)

  val testGetRequest = FakeRequest("GET", "/confirm-partnership-company")

  val testPostRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/confirm-partnership-company")

  "Calling the show action of the Confirm Partnership controller" when {
    "there is a company name in the session" should {
      "go to the Confirm Partnership page" in {
        mockAuthRetrieveAgentEnrolment()
        val request = testGetRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.companyNumberKey -> testCompanyNumber,
          SessionKeys.companyNameKey -> testCompanyName,
          SessionKeys.partnershipTypeKey -> testPartnershipType
        )

        val result = TestConfirmPartnershipController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")

        val changeLink = Jsoup.parse(contentAsString(result)).getElementById("changeLink")
        changeLink.attr("href") shouldBe agentRoutes.CaptureBusinessEntityController.show().url
      }
    }

    "there is no vat number in session" should {
      "go redirect to capture vat number" in {
        mockAuthRetrieveAgentEnrolment()

        val request = testGetRequest.withSession(
          SessionKeys.companyNumberKey -> testCompanyNumber,
          SessionKeys.companyNameKey -> testCompanyName,
          SessionKeys.partnershipTypeKey -> testPartnershipType
        )

        val result = TestConfirmPartnershipController.show(request)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(agentRoutes.CaptureVatNumberController.show().url)
      }
    }

    "there isn't a company name in the session" should {
      "throw internal server error" in {
        mockAuthRetrieveAgentEnrolment()

        val request = testPostRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.companyNumberKey -> testCompanyNumber,
          SessionKeys.partnershipTypeKey -> testPartnershipType
        )

        val result = TestConfirmPartnershipController.show(request)
        intercept[InternalServerException] {
          await(result)
        }
        //TODO redirect to capture partnership crn
      }
    }


    "there isn't a company number in the session" should {
      "throw internal server error" in {
        mockAuthRetrieveAgentEnrolment()

        val request = testPostRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.companyNameKey -> testCompanyName,
          SessionKeys.partnershipTypeKey -> testPartnershipType
        )

        val result = TestConfirmPartnershipController.show(request)
        intercept[InternalServerException] {
          await(result)
        }
        //TODO redirect to capture partnership crn
      }
    }

    "there isn't a partnership type in the session" should {
      "throw internal server error" in {
        mockAuthRetrieveAgentEnrolment()

        val request = testPostRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.companyNumberKey -> testCompanyNumber,
          SessionKeys.companyNameKey -> testCompanyName
        )

        val result = TestConfirmPartnershipController.show(request)
        intercept[InternalServerException] {
          await(result)
        }
        //TODO redirect to capture partnership crn
      }
    }
  }

  "Calling the submit action of the Confirm Partnership controller" should {
    "redirect to Capture Partnership Utr" in {
      mockAuthRetrieveAgentEnrolment()

      val request = testPostRequest.withSession(
        SessionKeys.vatNumberKey -> testVatNumber,
        SessionKeys.companyNumberKey -> testCompanyNumber,
        SessionKeys.companyNameKey -> testCompanyName,
        SessionKeys.partnershipTypeKey -> testPartnershipType
      )

      val result = TestConfirmPartnershipController.submit(request)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
    }

    "go to the 'capture vat number' page if vat number is missing" in {
      mockAuthRetrieveAgentEnrolment()

      val request = testPostRequest.withSession(
        SessionKeys.companyNumberKey -> testCompanyNumber,
        SessionKeys.companyNameKey -> testCompanyName,
        SessionKeys.partnershipTypeKey -> testPartnershipType
      )

      val result = TestConfirmPartnershipController.submit(request)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(agentRoutes.CaptureVatNumberController.show().url)

    }

    "throw internal server error if company number is missing" in {
      mockAuthRetrieveAgentEnrolment()

      val request = testPostRequest.withSession(
        SessionKeys.vatNumberKey -> testVatNumber,
        SessionKeys.companyNameKey -> testCompanyName,
        SessionKeys.partnershipTypeKey -> testPartnershipType
      )

      val result = TestConfirmPartnershipController.submit(request)

      intercept[InternalServerException] {
        await(result)
      }
    }


    "throw internal server error if company name is missing" in {
      mockAuthRetrieveAgentEnrolment()

      val request = testPostRequest.withSession(
        SessionKeys.vatNumberKey -> testVatNumber,
        SessionKeys.companyNumberKey -> testCompanyNumber,
        SessionKeys.partnershipTypeKey -> testPartnershipType
      )

      val result = TestConfirmPartnershipController.submit(request)

      intercept[InternalServerException] {
        await(result)
      }
    }

    "throw internal server error if partnership type is missing" in {
      mockAuthRetrieveAgentEnrolment()

      val request = testPostRequest.withSession(
        SessionKeys.vatNumberKey -> testVatNumber,
        SessionKeys.companyNameKey -> testCompanyName,
        SessionKeys.companyNumberKey -> testCompanyNumber
      )

      val result = TestConfirmPartnershipController.submit(request)

      intercept[InternalServerException] {
        await(result)
      }
    }

  }
}
