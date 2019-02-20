/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.connectors.GetCompanyNameConnector
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.CompanyNumber._
import uk.gov.hmrc.vatsignupfrontend.forms.validation.utils.Patterns.numericRegex
import uk.gov.hmrc.vatsignupfrontend.httpparsers.GetCompanyNameHttpParser.GetCompanyNameResponse

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class GetCompanyNameService @Inject()(val getCompanyNameConnector: GetCompanyNameConnector) {

  def getCompanyName(companyNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetCompanyNameResponse] =
    getCompanyNameConnector.getCompanyName(padCompanyNumber(companyNumber))

  private[services] def padCompanyNumber(companyNumber: String): String =
    companyNumber match {
      case allNumbersRegex(numbers) if numbers.length <= 8 => f"${numbers.toInt}%08d"
      case withPrefixRegex(prefix, numbers) if numbers.matches(numericRegex) && numbers.length <= 6 => f"$prefix${numbers.toInt}%06d"
      case withPrefixRegex(prefix, numbers) => f"$prefix$numbers" // Allows a CRN with a suffix
      case _ => throw new IllegalArgumentException("unexpected malformed company number")
    }

}
