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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import play.api.http.Status._
import play.api.libs.json.{JsResult, JsSuccess, JsValue, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.vatsignupfrontend.Constants._
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{CrnDissolved, FeatureSwitching}
import uk.gov.hmrc.vatsignupfrontend.models.companieshouse._

object GetCompanyNameHttpParser extends FeatureSwitching {
  type GetCompanyNameResponse = Either[GetCompanyNameFailure, GetCompanyNameSuccess]

  val LimitedPartnershipKey = "limited-partnership"
  val LimitedLiabilityPartnershipKey = "llp"
  val ScottishLimitedPartnershipKey = "scottish-partnership"

  val DissolvedStatusKey = "dissolved"
  val ConvertedClosedStatusKey = "converted-closed"

  implicit object GetCompanyNameHttpReads extends HttpReads[GetCompanyNameResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetCompanyNameResponse = {
      response.status match {
        case OK =>
          val optCompanyStatus = (response.json \ GetCompanyStatusCodeKey).asOpt[String]
          val optCompanyName = (response.json \ GetCompanyNameCodeKey).asOpt[String]
          val optCompanyType = (response.json \ GetCompanyTypeCodeKey).asOpt[CompanyType](new Reads[CompanyType] {
            override def reads(json: JsValue): JsResult[CompanyType] = json.validate[String] match {
              case JsSuccess(LimitedPartnershipKey, _) => JsSuccess(LimitedPartnership)
              case JsSuccess(LimitedLiabilityPartnershipKey, _) => JsSuccess(LimitedLiabilityPartnership)
              case JsSuccess(ScottishLimitedPartnershipKey, _) => JsSuccess(ScottishLimitedPartnership)
              case JsSuccess(_, _) => JsSuccess(NonPartnershipEntity)
            }
          })

          (optCompanyName, optCompanyType, optCompanyStatus) match {
            case (Some(companyName), _, Some(DissolvedStatusKey) | Some(ConvertedClosedStatusKey)) if isEnabled(CrnDissolved) =>
              Right(CompanyClosed(companyName))
            case (Some(companyName), Some(companyType), _) =>
              Right(CompanyDetails(companyName, companyType))
            case _ =>
              Left(GetCompanyNameFailureResponse(OK))

          }
        case NOT_FOUND => Left(CompanyNumberNotFound)
        case status => Left(GetCompanyNameFailureResponse(status))
      }
    }
  }

  sealed trait GetCompanyNameSuccess

  case class CompanyClosed(companyName: String) extends GetCompanyNameSuccess

  case class CompanyDetails(companyName: String, companyType: CompanyType) extends GetCompanyNameSuccess

  sealed trait GetCompanyNameFailure

  case object CompanyNumberNotFound extends GetCompanyNameFailure

  case class GetCompanyNameFailureResponse(status: Int) extends GetCompanyNameFailure

}
