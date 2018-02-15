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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Configuration, Environment}
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class CaptureVatNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  object TestBusinessNameController extends CaptureVatNumberController(messagesApi)

  val testGetRequest = FakeRequest("GET", "/vat-number")

  def testPostRequest(value: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "/vat-number").withFormUrlEncodedBody("vrn" -> value)

  "Calling the show action of the Capture Vat Number controller" should {
    "go to the Capture Vat number page" in {
      val result = TestBusinessNameController.show(testGetRequest)
      status(result) shouldBe Status.NOT_IMPLEMENTED
      // TODO introduce when view in place
      // contentType(result) shouldBe Some("text/html")
      //charset(result) shouldBe Some("utf-8")
    }
  }


  "Calling the submit action of the Capture Vat Number controller" should {
    //todo update when next page played
    "go to a new page" when {
      "form successfully submitted" in {
        val result = TestBusinessNameController.submit(testPostRequest("123456789"))
        status(result) shouldBe Status.NOT_IMPLEMENTED
      }
    }

    "reload the page with erros" when {
      "form unsuccessfully submitted" in {
        val result = TestBusinessNameController.submit(testPostRequest("invalid"))
        status(result) shouldBe Status.BAD_REQUEST
      }
    }
  }
}