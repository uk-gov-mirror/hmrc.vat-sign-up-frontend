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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.principal

import java.time.LocalDate

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.featureswitch.KnownFactsJourney
import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsubscriptionfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsubscriptionfrontend.models.{BusinessEntity, DateModel, Other, SoleTrader}

class CheckYourAnswersControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestCheckYourAnswersController extends CheckYourAnswersController(mockControllerComponents)

  val testDate: DateModel = DateModel.dateConvert(LocalDate.now())

  def testGetRequest(vatNumber: Option[String] = Some(testVatNumber),
                     registrationDate: Option[DateModel] = Some(testDate),
                     postCode: Option[String] = Some(testBusinessPostcode),
                     businessType: Option[BusinessEntity] = Some(SoleTrader)
                    ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/check-your-answers").withSession(
      SessionKeys.vatNumberKey -> vatNumber.getOrElse(""),
      SessionKeys.vatRegistrationDateKey -> registrationDate.map(Json.toJson(_).toString()).getOrElse(""),
      SessionKeys.businessPostCodeKey -> postCode.getOrElse(""),
      SessionKeys.businessEntityKey -> businessType.map(BusinessEntitySessionFormatter.toString).getOrElse("")
    )

  def testPostRequest(vatNumber: Option[String] = Some(testVatNumber),
                      registrationDate: Option[DateModel] = Some(testDate),
                      postCode: Option[String] = Some(testBusinessPostcode),
                      businessType: Option[BusinessEntity] = Some(SoleTrader)
                     ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/check-your-answers").withSession(
      SessionKeys.vatNumberKey -> vatNumber.getOrElse(""),
      SessionKeys.vatRegistrationDateKey -> registrationDate.map(Json.toJson(_).toString()).getOrElse(""),
      SessionKeys.businessPostCodeKey -> postCode.getOrElse(""),
      SessionKeys.businessEntityKey -> businessType.map(BusinessEntitySessionFormatter.toString).getOrElse("")
    )

  override def beforeEach(): Unit = enable(KnownFactsJourney)

  override def afterEach(): Unit = disable(KnownFactsJourney)

  "Calling the show action of the Check your answers controller" when {
    "all prerequisite data are in session" should {
      "go to the Check your answers page" in {
        mockAuthEmptyRetrieval()

        val result = TestCheckYourAnswersController.show(testGetRequest())
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
    "vat number is missing" should {
      "go to capture vat number page" in {
        mockAuthEmptyRetrieval()

        val result = TestCheckYourAnswersController.show(testGetRequest(vatNumber = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
    "vat registration date is missing" should {
      "go to capture vat registration date page" in {
        mockAuthEmptyRetrieval()

        val result = TestCheckYourAnswersController.show(testGetRequest(registrationDate = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatRegistrationDateController.show().url)
      }
    }
    "post code is missing" should {
      "go to business post code page" in {
        mockAuthEmptyRetrieval()

        val result = TestCheckYourAnswersController.show(testGetRequest(postCode = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.BusinessPostCodeController.show().url)
      }
    }
    "business entity is missing" should {
      "go to business entity page" in {
        mockAuthEmptyRetrieval()

        val result = TestCheckYourAnswersController.show(testGetRequest(businessType = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureBusinessEntityController.show().url)
      }
    }
    "business entity is Other" should {
      "go to business entity page" in {
        mockAuthEmptyRetrieval()

        val result = TestCheckYourAnswersController.show(testGetRequest(businessType = Some(Other)))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureBusinessEntityController.show().url)
      }
    }
  }


  "Calling the submit action of the Check your answers controller" when {
    "all prerequisite data are in" should {
      // TODO update for when elligibility is done
      "goto capture your details controller" in {
        mockAuthEmptyRetrieval()

        val result = await(TestCheckYourAnswersController.submit(testPostRequest()))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.CaptureYourDetailsController.show().url)
      }
    }
    "vat number is missing" should {
      "go to capture vat number page" in {
        mockAuthEmptyRetrieval()

        val result = await(TestCheckYourAnswersController.submit(testPostRequest(vatNumber = None)))

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
    "vat registration date is missing" should {
      "go to capture vat registration date page" in {
        mockAuthEmptyRetrieval()

        val result = TestCheckYourAnswersController.submit(testPostRequest(registrationDate = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureVatRegistrationDateController.show().url)
      }
    }
    "post code is missing" should {
      "go to business post code page" in {
        mockAuthEmptyRetrieval()

        val result = TestCheckYourAnswersController.submit(testPostRequest(postCode = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.BusinessPostCodeController.show().url)
      }
    }
    "business entity is missing" should {
      "go to business entity page" in {
        mockAuthEmptyRetrieval()

        val result = TestCheckYourAnswersController.submit(testPostRequest(businessType = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureBusinessEntityController.show().url)
      }
    }
    "business entity is Other" should {
      "go to business entity page" in {
        mockAuthEmptyRetrieval()

        val result = TestCheckYourAnswersController.submit(testPostRequest(businessType = Some(Other)))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureBusinessEntityController.show().url)
      }
    }
  }

}