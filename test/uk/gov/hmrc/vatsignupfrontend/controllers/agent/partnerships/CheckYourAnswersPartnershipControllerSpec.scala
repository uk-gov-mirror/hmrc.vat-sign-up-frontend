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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.GeneralPartnershipJourney
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.{routes => agentRoutes}
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.PartnershipEntityType.CompanyTypeSessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStorePartnershipInformationService
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils.jsonSessionFormatter

import scala.concurrent.Future

class CheckYourAnswersPartnershipControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents
  with MockStorePartnershipInformationService {

  object TestCheckYourAnswersPartnershipController extends CheckYourAnswersPartnershipController(
    mockControllerComponents, mockStorePartnershipInformationService
  )

  private def sessionValues(vatNumber: Option[String] = Some(testVatNumber),
                            sautr: Option[String] = Some(testSaUtr),
                            postCode: Option[PostCode] = Some(testBusinessPostcode),
                            entityType: Option[BusinessEntity] = None,
                            companyNumber: Option[String],
                            partnershipEntityType: Option[PartnershipEntityType]): Iterable[(String, String)] =
    (
      (vatNumber map (vatNumberKey -> _))
        ++ (sautr map (partnershipSautrKey -> _))
        ++ (postCode map jsonSessionFormatter[PostCode].toString map (partnershipPostCodeKey -> _))
        ++ (entityType map BusinessEntitySessionFormatter.toString map (businessEntityKey -> _))
        ++ (partnershipEntityType map CompanyTypeSessionFormatter.toString map (partnershipTypeKey -> _))
        ++ (companyNumber map (companyNumberKey -> _))
      )


  def testGetRequest(vatNumber: Option[String] = Some(testVatNumber),
                     sautr: Option[String] = Some(testSaUtr),
                     postCode: Option[PostCode] = Some(testBusinessPostcode),
                     entityType: Option[BusinessEntity] = Some(GeneralPartnership),
                     companyNumber: Option[String] = None
                    ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/check-your-answers").withSession(
      sessionValues(vatNumber, sautr, postCode, entityType, companyNumber, None).toSeq: _*
    )

  def testPostRequest(vatNumber: Option[String] = Some(testVatNumber),
                      sautr: Option[String] = Some(testSaUtr),
                      postCode: Option[PostCode] = Some(testBusinessPostcode),
                      entityType: Option[BusinessEntity] = Some(GeneralPartnership),
                      companyNumber: Option[String] = None,
                      partnershipEntityType: Option[PartnershipEntityType] = None
                     ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/check-your-answers").withSession(
      sessionValues(vatNumber, sautr, postCode, entityType, companyNumber, partnershipEntityType).toSeq: _*
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(GeneralPartnershipJourney)
  }

  "Calling the show action of the Check your answers controller" when {
    "all prerequisite data are in session for general partnership" should {
      "go to the Check your answers page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCheckYourAnswersPartnershipController.show(testGetRequest())
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
    "all prerequisite data are in session" should {
      "go to the Check your answers page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCheckYourAnswersPartnershipController.show(testGetRequest(companyNumber = Some(testCompanyNumber)))
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
    "vat number is missing" should {
      "go to capture vat number page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCheckYourAnswersPartnershipController.show(testGetRequest(vatNumber = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(agentRoutes.CaptureVatNumberController.show().url)
      }
    }
    "business entity is missing" should {
      "go to capture business entity page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCheckYourAnswersPartnershipController.show(testGetRequest(entityType = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(agentRoutes.CaptureBusinessEntityController.show().url)
      }
    }
    "saUtr is missing" should {
      "go to capture partnership utr page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCheckYourAnswersPartnershipController.show(testGetRequest(sautr = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
      }
    }
  }

  "calling the submit action of the Check Your Answers Partnership controller" when {
    "all prerequisite data are in" when {
      "store partnership info returned StorePartnershipInformationSuccess" should {
        "go to Capture Email Page" in {
          mockAuthRetrieveAgentEnrolment()

          mockStorePartnershipInformation(
            testVatNumber,
            testSaUtr,
            Some(testBusinessPostcode)
          )(
            Future.successful(Right(StorePartnershipInformationSuccess))
          )
          val result = TestCheckYourAnswersPartnershipController.submit(testPostRequest())
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(agentRoutes.EmailRoutingController.route().url)

        }
      }

      "store partnership info returned StorePartnershipInformationSuccess for a limited partnership" should {
        "go to Capture Email Page" in {
          mockAuthRetrieveAgentEnrolment()

          mockStorePartnershipInformation(
            testVatNumber,
            testSaUtr,
            testCompanyNumber,
            PartnershipEntityType.LimitedPartnership,
            Some(testBusinessPostcode)
          )(
            Future.successful(Right(StorePartnershipInformationSuccess))
          )
          val result = TestCheckYourAnswersPartnershipController.submit(testPostRequest(
            partnershipEntityType = Some(PartnershipEntityType.LimitedPartnership),
            companyNumber = Some(testCompanyNumber)
          ))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(agentRoutes.EmailRoutingController.route().url)

        }
      }
      "store partnership info returned StorePartnershipInformationFailureResponse" should {
        "throw Internal Server Exception" in {
          mockAuthRetrieveAgentEnrolment()

          mockStorePartnershipInformation(
            testVatNumber,
            testSaUtr,
            Some(testBusinessPostcode)
          )(
            Future.successful(Left(StorePartnershipInformationFailureResponse(500)))
          )
          intercept[InternalServerException](await(TestCheckYourAnswersPartnershipController.submit(testPostRequest())))
        }
      }
    }
    "vat number is missing" should {
      "go to capture vat number page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = await(TestCheckYourAnswersPartnershipController.submit(testPostRequest(vatNumber = None)))

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(agentRoutes.CaptureVatNumberController.show().url)
      }
    }
    "saUtr is missing" should {
      "go to capture partnership utr page" in {
        mockAuthRetrieveAgentEnrolment()

        val result = TestCheckYourAnswersPartnershipController.submit(testGetRequest(sautr = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CapturePartnershipUtrController.show().url)
      }
    }
  }

}