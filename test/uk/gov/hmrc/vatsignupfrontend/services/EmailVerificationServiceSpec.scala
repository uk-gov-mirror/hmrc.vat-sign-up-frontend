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

package uk.gov.hmrc.vatsignupfrontend.services

import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.connectors.mocks.MockRequestEmailVerificationPasscodeConnector
import uk.gov.hmrc.vatsignupfrontend.models.{AlreadyVerifiedEmailAddress, RequestEmailPasscodeSuccessful}
import uk.gov.hmrc.vatsignupfrontend.utils.UnitSpec

import scala.concurrent.Future

class EmailVerificationServiceSpec extends UnitSpec with MockitoSugar with MockRequestEmailVerificationPasscodeConnector {

  object testEmailVerificationService extends EmailVerificationService(mockRequestEmailVerificationPasscodeConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  lazy val testEmail = "test@test.com"

  "requestEmailVerificationPasscode" should {
    "return RequestEmailPasscodeSuccessful" in {
      mockRequestEmailVerificationPasscode(testEmail)(Future.successful(RequestEmailPasscodeSuccessful))

      val res = await(testEmailVerificationService.requestEmailVerificationPasscode(testEmail, "en"))

      res shouldBe RequestEmailPasscodeSuccessful
    }
    "return AlreadyVerifiedEmailAddress" in {
      mockRequestEmailVerificationPasscode(testEmail)(Future.successful(AlreadyVerifiedEmailAddress))

      val res = await(testEmailVerificationService.requestEmailVerificationPasscode(testEmail, "en"))

      res shouldBe AlreadyVerifiedEmailAddress
    }
  }

}
