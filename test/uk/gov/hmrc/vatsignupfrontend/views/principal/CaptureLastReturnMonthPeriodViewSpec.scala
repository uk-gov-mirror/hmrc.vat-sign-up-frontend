/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.views.principal

import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{CaptureLastReturnMonthPeriod => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.forms.MonthForm._
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec


class CaptureLastReturnMonthPeriodViewSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)
  val error = "error.no_month_selected"

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.principal.capture_last_return_month_period(monthForm, postAction = testCall)(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  "The Capture Last Return Month Period view" should {

    val testPage = TestView(
      name = "Capture Last Return Month Period View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )
    testPage.shouldHaveForm("Month Form")(actionCall = testCall)

    "have a set of radio inputs" which {
      lazy val doc = Jsoup.parse(page.body)

      "the months should be capitalised" in {

        val monthsDoc = doc.body().getElementsByTag("label").text().split(" ").toList
        monthsDoc shouldEqual messages.months
      }
    }
    testPage.shouldHaveH2(messages.subHeading)
    testPage.shouldHaveH2(messages.subHeading2)

    testPage.shouldHavePara(messages.line)
    testPage.shouldHavePara(messages.line2)
    testPage.shouldHavePara(messages.line3)

    testPage.shouldHaveContinueButton()
  }
}
