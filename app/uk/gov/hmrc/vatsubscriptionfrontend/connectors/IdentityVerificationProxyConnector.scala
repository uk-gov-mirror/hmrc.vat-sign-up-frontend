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

package uk.gov.hmrc.vatsubscriptionfrontend.connectors

import javax.inject.{Inject, Singleton}

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.vatsubscriptionfrontend.config.AppConfig
import uk.gov.hmrc.vatsubscriptionfrontend.controllers.principal.routes
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.IdentityVerificationProxyHttpParser._

import scala.concurrent.Future

@Singleton
class IdentityVerificationProxyConnector @Inject()(val http: HttpClient,
                                                   val applicationConfig: AppConfig) {

  def start()(implicit hc: HeaderCarrier): Future[IdentityVerificationProxyResponse] =
    http.POST[JsObject, IdentityVerificationProxyResponse](
      applicationConfig.identityVerificationStartUrl,
      IdentityVerificationProxyConnector.startIdentityVerificationRequest
    )

}

object IdentityVerificationProxyConnector {
  val startIdentityVerificationRequest: JsObject = Json.obj(
    "origin" -> "mtd-vat",
    "confidenceLevel" -> 200,
    "completionURL" -> routes.IdentityVerificationCallbackController.continue().url,
    "failureURL" -> routes.IdentityVerificationCallbackController.continue().url
  )
}
