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

package uk.gov.hmrc.vatsignupfrontend.views.agent.partnerships

import org.jsoup.Jsoup
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{AgentDoesYourClientHaveAUtr => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.forms.DoYouHaveAUtrForm._
import uk.gov.hmrc.vatsignupfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec


class DoesYourClientHaveAUtrViewSpec extends ViewSpec {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  lazy val page: HtmlFormat.Appendable = uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.does_your_client_have_a_utr(
    doYouHaveAUtrForm(isAgent = true),
    postAction = testCall)(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  "The Does Your Client Have A Utr view" should {

    val testPage = TestView(
      name = "Does Your Client Have A Utr View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )
    testPage.shouldHaveForm("Yes No Form")(actionCall = testCall)

    "have a set of radio inputs" which {
      lazy val doc = Jsoup.parse(page.body)

      "for the option 'Yes'" should {

        "have the text 'yes'" in {
          doc.select(s"label[for=${YesNoMapping.option_yes}]").text() shouldEqual MessageLookup.Base.yes
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#yes")

          "have the id 'yes'" in {
            optionLabel.attr("id") shouldEqual "yes"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'No'" should {

        "have the text 'no'" in {
          doc.select(s"label[for=${YesNoMapping.option_no}]").text() shouldEqual MessageLookup.Base.no
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#no")

          "have the id 'no" in {
            optionLabel.attr("id") shouldEqual "no"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }
    }

    testPage.shouldHavePara(messages.line)

    testPage.shouldHaveContinueButton()
  }
}
