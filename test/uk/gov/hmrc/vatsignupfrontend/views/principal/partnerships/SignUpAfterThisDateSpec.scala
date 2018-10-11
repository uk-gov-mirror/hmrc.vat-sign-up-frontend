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

package uk.gov.hmrc.vatsignupfrontend.views.principal.partnerships

import java.time.LocalDate

import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{SignUpAfterThisDate => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.models.DateModel

class SignUpAfterThisDateSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  val testDate: LocalDate = LocalDate.now()
  val expectedFormattedDate: String = DateModel.dateConvert(testDate).toOutputDateFormat

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.principal.partnerships.sign_up_after_this_date(
    date = testDate)(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  "The Sign Up After This Date view" should {

    val testPage = TestView(
      name = "Sign Up After This Date view",
      title = messages.title,
      heading = messages.heading,
      page = page,
      haveSignOutInBanner = false
    )

    testPage.shouldHaveParaSeq(
      messages.line1,
      messages.line2(expectedFormattedDate)
    )

    testPage.shouldHaveSignOutButton(isAgent = false)

  }

}

