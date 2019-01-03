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

package uk.gov.hmrc.vatsignupfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StorePartnershipInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.{PartnershipEntityType, PostCode}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StorePartnershipInformationConnector @Inject()(val http: HttpClient,
                                                     val applicationConfig: AppConfig
                                                    )(implicit ec: ExecutionContext) {
  val sautrKey = "sautr"
  val partnershipTypeKey = "partnershipType"
  val CompanyNumberKey = "crn"
  val postCodeKey = "postCode"

  def storePartnershipInformation(vatNumber: String,
                                  sautr: String,
                                  partnershipType: PartnershipEntityType,
                                  companyNumber: Option[String],
                                  postCode: Option[PostCode]
                                 )(implicit hc: HeaderCarrier): Future[StorePartnershipInformationResponse] = {
    val body = Json.obj(
      partnershipTypeKey -> partnershipType,
      sautrKey -> sautr
    ).++(
      companyNumber match {
        case Some(crn) => Json.obj(CompanyNumberKey -> crn)
        case _ => Json.obj()
      }
    ).++(
      postCode match {
        case Some(pc) => Json.obj(postCodeKey -> pc.postCode)
        case _ => Json.obj()
      }
    )
    http.POST[JsObject, StorePartnershipInformationResponse](applicationConfig.storePartnershipInformationUrl(vatNumber), body)
  }

}
