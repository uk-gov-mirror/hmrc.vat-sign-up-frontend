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

package uk.gov.hmrc.vatsignupfrontend.helpers

import java.time.LocalDate
import java.util.UUID

import play.api.libs.json.Json
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, PostCode, UserDetailsModel}

import scala.util.Random

object IntegrationTestConstants {
  val testVatNumber: String = IntegrationTestConstantsGenerator.randomVatNumber
  val testInvalidVatNumber: String = "999999999"
  val testCompanyNumber: String = IntegrationTestConstantsGenerator.randomCompanyNumber
  val testCompanyName: String = Random.alphanumeric.take(10).mkString
  val testCompanyUtr: String = IntegrationTestConstantsGenerator.randomUtrNumeric()
  val testBusinessPostCode: PostCode = PostCode(IntegrationTestConstantsGenerator.randomPostCode.toUpperCase.filterNot(_.isWhitespace))
  val testNino: String = new Generator().nextNino.nino
  val testSaUtr: String = f"${Math.abs(Random.nextLong() % 10000000000L)}%010d"
  val testEmail: String = IntegrationTestConstantsGenerator.randomEmail
  val testArn: String = UUID.randomUUID().toString
  val testUri: String = "/test/url"
  val testUserDetails: UserDetailsModel =
    UserDetailsModel(
      firstName = UUID.randomUUID().toString,
      lastName = UUID.randomUUID().toString,
      nino = testNino,
      dateOfBirth = DateModel.dateConvert(LocalDate.now())
    )
  val testUserDetailsJson: String = Json.toJson(testUserDetails).toString()
  val testStartDate: LocalDate = LocalDate.now()
  val testEndDate: LocalDate = testStartDate.plusMonths(3)
  val testPartnershipType: String = "limitedPartnership"
  val testBox5Figure: String = "1234.56"
}
