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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal.bta

import java.time.LocalDate

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.BTAClaimSubscription
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.{routes => principalRoutes}
import uk.gov.hmrc.vatsignupfrontend.forms.BusinessPostCodeForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.ClaimSubscriptionHttpParser.{InvalidVatNumber, KnownFactsMismatch, SubscriptionClaimed}
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, PostCode}
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockClaimSubscriptionService

import scala.concurrent.Future


class BtaBusinessPostCodeControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents with MockClaimSubscriptionService {

  object TestBusinessPostCodeController extends BtaBusinessPostCodeController(mockControllerComponents, mockClaimSubscriptionService)

  lazy val testGetRequest = FakeRequest("GET", "/bta/business-postcode")

  val testDate: DateModel = DateModel.dateConvert(LocalDate.now())

  def testPostRequest(postCode: PostCode = testBusinessPostcode,
                      vatNumber: Option[String] = Some(testVatNumber),
                      registrationDate: Option[DateModel] = Some(testDate)
                     ): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/bta/business-postcode").withSession(
      SessionKeys.vatNumberKey -> vatNumber.getOrElse(""),
      SessionKeys.vatRegistrationDateKey -> registrationDate.map(Json.toJson(_).toString()).getOrElse("")
    ).withFormUrlEncodedBody(
      businessPostCode -> postCode.postCode
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(BTAClaimSubscription)
  }

  "Calling the show action of the BTA Business PostCode controller" when {
    "go to the BTA Business PostCode page" in {
      mockAuthAdminRole()

      val result = TestBusinessPostCodeController.show(testGetRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Calling the submit action of the Business PostCode controller" when {
    "form successfully submitted" should {
      "goto confirmation page" in {
        mockAuthAdminRole()
        mockClaimSubscription(
          testVatNumber,
          testBusinessPostcode,
          testDate,
          isFromBta = true
        )(Future.successful(Right(SubscriptionClaimed)))

        val result = TestBusinessPostCodeController.submit(testPostRequest())

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(principalRoutes.SignUpCompleteClientController.show().url)
      }
    }

    "form unsuccessfully submitted" should {
      "reload the page with errors" in {
        mockAuthAdminRole()

        val result = TestBusinessPostCodeController.submit(testPostRequest(postCode = PostCode("1234567890")))
        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "form successfully submitted with non matching postcode" should {
      "goto BTA postcode non matching page" in {
        mockAuthAdminRole()

        mockClaimSubscription(testVatNumber, testBusinessPostcode, testDate, isFromBta = true)(Future.successful(Left(KnownFactsMismatch)))

        val result = TestBusinessPostCodeController.submit(testPostRequest())
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CouldNotConfirmBusinessController.show().url)
      }
    }

    "vat registration date is missing" should {
      "go to capture bta vat registration date page" in {
        mockAuthAdminRole()

        val result = TestBusinessPostCodeController.submit(testPostRequest(registrationDate = None))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CaptureBtaVatRegistrationDateController.show().url)
      }
    }

    "vat number is missing" should {
      "throw internal server error" in {
        mockAuthAdminRole()

        intercept[InternalServerException] {
          await(TestBusinessPostCodeController.submit(testPostRequest(vatNumber = None)))
        }
      }
    }

    "claim subscription returns a failure" should {
      "display a technical difficulties page" in {
        mockAuthAdminRole()

        mockClaimSubscription(testVatNumber, testBusinessPostcode, testDate, isFromBta = true)(Future.successful(Left(InvalidVatNumber)))

        intercept[InternalServerException] {
          await(TestBusinessPostCodeController.submit(testPostRequest()))
        }
      }
    }
  }
}


