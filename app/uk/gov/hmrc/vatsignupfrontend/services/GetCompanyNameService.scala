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
import uk.gov.hmrc.vatsignupfrontend.httpparsers.GetCompanyNameHttpParser.GetCompanyNameResponse
import uk.gov.hmrc.vatsignupfrontend.utils.StringPaddingUtil
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetCompanyNameService @Inject()(val getCompanyNameConnector: GetCompanyNameConnector) extends StringPaddingUtil {

  private val crnMaxLength = 8
  private val crnWithoutPrefixLength = 6
  private val zero = "0"
  private val prefixRegex = "[0-9]{2}"

  def getCompanyName(companyNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetCompanyNameResponse] = {

    getCompanyNameConnector.getCompanyName(padCrn(companyNumber))
  }

  private def padCrn(companyNumber: String): String = {
    val (prefix, remainder) = companyNumber splitAt 2

    if (prefix matches prefixRegex) leftPad(companyNumber, crnMaxLength, zero)
    else prefix + leftPad(remainder, crnWithoutPrefixLength, zero)
  }

}
