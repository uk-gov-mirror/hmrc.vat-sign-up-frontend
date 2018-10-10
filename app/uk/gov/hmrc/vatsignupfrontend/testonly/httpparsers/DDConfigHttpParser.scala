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

package uk.gov.hmrc.vatsignupfrontend.testonly.httpparsers

import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}


object DDConfigHttpParser {
  type DDConfigHttpResponse = Either[DDConfigFailure, JsValue]

  implicit object DDConfigHttpReads extends HttpReads[DDConfigHttpResponse] {
    override def read(method: String, url: String, response: HttpResponse): DDConfigHttpResponse = {
      response.status match {
        case OK => Right(response.json)
        case status => Left(DDConfigFailure(status, response.body))
      }
    }
  }

  case class DDConfigFailure(status: Int, body: String)

  object DDConfigFailure {
    implicit val format = Json.format[DDConfigFailure]
  }

}

// $COVERAGE-ON$

