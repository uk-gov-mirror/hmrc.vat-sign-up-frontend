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
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.vatsignupfrontend.Constants._

import scala.util.{Success, Try}

object GetCompanyNameHttpParser {
  type GetCompanyNameResponse = Either[GetCompanyNameFailure, GetCompanyNameSuccess]

  implicit object GetCompanyNameHttpReads extends HttpReads[GetCompanyNameResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetCompanyNameResponse = {

      def parseBody: Try[String] = Try((response.json \ GetCompanyNameCodeKey).as[String])

      (response.status, parseBody) match {
        case (OK, Success(companyName)) => Right(GetCompanyNameSuccess(companyName))
        case (NOT_FOUND, _) => Left(CompanyNumberNotFound)
        case (status, _) => Left(GetCompanyNameFailureResponse(status))
      }
    }
  }

  case class GetCompanyNameSuccess(companyName: String)

  sealed trait GetCompanyNameFailure

  case object CompanyNumberNotFound extends GetCompanyNameFailure

  case class GetCompanyNameFailureResponse(status: Int) extends GetCompanyNameFailure

}
