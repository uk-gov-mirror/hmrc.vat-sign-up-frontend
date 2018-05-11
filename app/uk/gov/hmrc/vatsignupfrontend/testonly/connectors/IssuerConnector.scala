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

//$COVERAGE-OFF$Disabling scoverage

package uk.gov.hmrc.vatsignupfrontend.testonly.connectors

import javax.inject.{Inject, Singleton}

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.vatsignupfrontend.Constants.TaxEnrolments._
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.testonly.httpparsers.IssuerHttpParser._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IssuerConnector @Inject()(val http: HttpClient,
                                val applicationConfig: AppConfig) {

  private def issuerUrl(safeId: String) = s"${applicationConfig.taxEnrolmentsUrl}/tax-enrolments/subscriptions/$safeId/issuer"

  def issuerSuccess(vatNumber: String, postCode: String, registrationDate: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IssuerResponse] =
    http.PUT[JsObject, IssuerResponse](issuerUrl(vatNumber),
      Json.obj(
        "serviceName" -> serviceName,
        "identifiers" -> Json.arr(
          Json.obj(
            "key" -> "VRN",
            "value" -> vatNumber
          )
        ),
        "verifiers" -> Json.arr(
          Json.obj(
            "key" -> "Postcode",
            "value" -> postCode
          ),
          Json.obj(
            "key" -> "VATRegistrationDate",
            "value" -> registrationDate
          )
        ),
        "subscriptionState" -> "SUCCEEDED"
      )
    )

  def issuerFail(vatNumber: String, reason: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IssuerResponse] =
    http.PUT[JsObject, IssuerResponse](issuerUrl(vatNumber),
      Json.obj(
        "serviceName" -> serviceName,
        "subscriptionState" -> "ERROR",
        "subscriptionError" -> reason
      )
    )

}

// $COVERAGE-ON$
