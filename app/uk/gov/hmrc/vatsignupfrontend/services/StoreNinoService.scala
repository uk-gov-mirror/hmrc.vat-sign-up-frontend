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
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.connectors.StoreNinoConnector
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreNinoHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.{NinoSource, UserDetailsModel}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreNinoService @Inject()(applicationConfig: AppConfig,
                                 storeNinoConnector: StoreNinoConnector) {

  /* N.B. this is header update is to be used in conjunction with the test only route
  *  StubCitizenDetailsController
  *  the True-Client-IP must match the testId in in uk.gov.hmrc.vatsubscriptionfrontend.testonly.connectors.Request sent
  *  The hc must not be edited in production
  */
  def amendHCForTest(implicit hc: HeaderCarrier): HeaderCarrier =
    if (applicationConfig.hasEnabledTestOnlyRoutes) hc.copy(trueClientIp = Some("VATSUBSC"))
    else hc

  def storeNino(vatNumber:String, nino: String, ninoSource: NinoSource)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[StoreNinoResponse] =
    storeNinoConnector.storeNino(vatNumber, nino, ninoSource)(hc, ec)

}
