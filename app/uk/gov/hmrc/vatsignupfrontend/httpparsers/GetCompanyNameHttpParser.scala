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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import play.api.http.Status._
import play.api.libs.json.{JsResult, JsSuccess, JsValue, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.vatsignupfrontend.Constants._
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse._

object GetCompanyNameHttpParser {
  type GetCompanyNameResponse = Either[GetCompanyNameFailure, GetCompanyNameSuccess]

  val LimitedPartnershipKey = "limited-partnership"
  val LimitedLiabilityPartnershipKey = "llp"
  val ScottishLimitedPartnershipKey = "scottish-partnership"

  implicit object GetCompanyNameHttpReads extends HttpReads[GetCompanyNameResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetCompanyNameResponse = {
      response.status match {
        case OK =>
          val optCompanyName = (response.json \ GetCompanyNameCodeKey).asOpt[String]
          val optCompanyType = (response.json \ GetCompanyTypeCodeKey).asOpt[CompanyType](new Reads[CompanyType] {
            override def reads(json: JsValue): JsResult[CompanyType] = json.validate[String] match {
              case JsSuccess(LimitedPartnershipKey, _) => JsSuccess(LimitedPartnership)
              case JsSuccess(LimitedLiabilityPartnershipKey, _) => JsSuccess(LimitedLiabilityPartnership)
              case JsSuccess(ScottishLimitedPartnershipKey, _) => JsSuccess(ScottishLimitedPartnership)
              case JsSuccess(_, _) => JsSuccess(NonPartnershipEntity)
            }
          })


          (optCompanyName, optCompanyType) match {
            case (Some(companyName), Some(companyType)) => Right(GetCompanyNameSuccess(companyName, companyType))
            case (_, _) => Left(GetCompanyNameFailureResponse(OK))
          }
        case NOT_FOUND => Left(CompanyNumberNotFound)
        case status => Left(GetCompanyNameFailureResponse(status))
      }
    }
  }

  case class GetCompanyNameSuccess(companyName: String, companyType: CompanyType)

  sealed trait GetCompanyNameFailure

  case object CompanyNumberNotFound extends GetCompanyNameFailure

  case class GetCompanyNameFailureResponse(status: Int) extends GetCompanyNameFailure

}
