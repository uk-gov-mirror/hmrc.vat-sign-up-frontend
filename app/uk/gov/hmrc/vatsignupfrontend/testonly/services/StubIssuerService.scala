/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.testonly.services

import java.time.format.DateTimeFormatter

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.testonly.connectors.IssuerConnector
import uk.gov.hmrc.vatsignupfrontend.testonly.httpparsers.IssuerHttpParser.IssuerResponse
import uk.gov.hmrc.vatsignupfrontend.testonly.models.StubIssuerRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class StubIssuerService @Inject()(issuerConnector: IssuerConnector) {

  def callIssuer(stubIssuerRequest: StubIssuerRequest)(implicit hc: HeaderCarrier): Future[IssuerResponse] = {
    if (stubIssuerRequest.isSuccessful)
      issuerConnector.issuerSuccess(
        vatNumber = stubIssuerRequest.vatNumber.replaceAll(" ", "").toUpperCase,
        postCode = stubIssuerRequest.postCode.get.replaceAll(" ", "").toUpperCase,
        registrationDate = stubIssuerRequest.registrationDate.get.toLocalDate.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
      )
    else
      issuerConnector.issuerFail(
        vatNumber = stubIssuerRequest.vatNumber.replaceAll(" ", "").toUpperCase,
        reason = stubIssuerRequest.errorMessage.get
      )
  }

}
