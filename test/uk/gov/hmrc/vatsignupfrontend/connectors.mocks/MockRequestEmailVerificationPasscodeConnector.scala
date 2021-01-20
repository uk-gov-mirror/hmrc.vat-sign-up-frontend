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

package uk.gov.hmrc.vatsignupfrontend.connectors.mocks

import org.mockito.ArgumentMatchers
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.connectors.RequestEmailVerificationPasscodeConnector
import uk.gov.hmrc.vatsignupfrontend.models.RequestEmailPasscodeResult

import scala.concurrent.Future

trait MockRequestEmailVerificationPasscodeConnector extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRequestEmailVerificationPasscodeConnector)
  }

  val mockRequestEmailVerificationPasscodeConnector: RequestEmailVerificationPasscodeConnector = mock[RequestEmailVerificationPasscodeConnector]

  def mockRequestEmailVerificationPasscode(email: String)(response: Future[RequestEmailPasscodeResult]): Unit =
    when(mockRequestEmailVerificationPasscodeConnector.requestEmailVerificationPasscode(ArgumentMatchers.eq(email))(ArgumentMatchers.any[HeaderCarrier]))
      .thenReturn(response)

}
