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

///*
// * Copyright 2018 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.vatsubscriptionfrontend.controllers
//
//import org.scalatestplus.play.guice.GuiceOneAppPerSuite
//import play.api.http.Status
//import play.api.mvc.AnyContentAsFormUrlEncoded
//import play.api.test.FakeRequest
//import play.api.test.Helpers._
//import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
//import uk.gov.hmrc.play.test.UnitSpec
//import uk.gov.hmrc.vatsubscriptionfrontend.config.mocks.MockControllerComponents
//import uk.gov.hmrc.vatsubscriptionfrontend.forms.VatNumberForm._
//
//import scala.concurrent.Future
//
//class CaptureVatNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents{
//  object TestCaptureVatNumberController extends CaptureVatNumberController(mockControllerComponents)
//
//  val testGetRequest = FakeRequest("GET", "/vat-number")
//
//  def testPostRequest(vatNumberVal: String): FakeRequest[AnyContentAsFormUrlEncoded] =
//    FakeRequest("POST", "/vat-number").withFormUrlEncodedBody(vatNumber -> vatNumberVal)
//
//  "Calling the show action of the Capture Vat Number controller" should {
//    "go to the Capture Vat number page" in {
//      mockAuthorise(retrievals = EmptyRetrieval)(Future.successful(Unit))
//
//      val result = TestCaptureVatNumberController.show(testGetRequest)
//      status(result) shouldBe Status.OK
//      contentType(result) shouldBe Some("text/html")
//      charset(result) shouldBe Some("utf-8")
//    }
//  }
//
//
//  "Calling the submit action of the Capture Vat Number controller" when {
//    //todo update when next page played
//    "form successfully submitted" should {
//      "go to the new page" in {
//        mockAuthorise(retrievals = EmptyRetrieval)(Future.successful(Unit))
//
//        val result = TestCaptureVatNumberController.submit(testPostRequest("123456789"))
//        status(result) shouldBe Status.NOT_IMPLEMENTED
//      }
//    }
//
//    "form unsuccessfully submitted" should {
//      "reload the page with errors" in {
//        mockAuthorise(retrievals = EmptyRetrieval)(Future.successful(Unit))
//
//        val result = TestCaptureVatNumberController.submit(testPostRequest("invalid"))
//        status(result) shouldBe Status.BAD_REQUEST
//        contentType(result) shouldBe Some("text/html")
//        charset(result) shouldBe Some("utf-8")
//      }
//    }
//  }
//}