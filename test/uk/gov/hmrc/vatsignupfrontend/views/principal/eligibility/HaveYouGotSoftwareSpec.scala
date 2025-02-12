/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.views.principal.eligibility

import org.jsoup.Jsoup
import play.api.i18n.MessagesApi
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalHaveYouGotSoftware => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.forms.HaveYouGotSoftwareForm._
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec

class HaveYouGotSoftwareSpec extends ViewSpec {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  lazy val page: HtmlFormat.Appendable = uk.gov.hmrc.vatsignupfrontend.views.html.principal.eligibility.have_you_got_software(
    haveYouGotSoftwareForm, postAction = testCall)(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  "The Have You Got Software view" should {
    val testPage = TestView(
      name = "Have You Got Software View",
      title = messages.title,
      heading = messages.heading,
      page = page,
      haveSignOutInBanner = false
    )
    testPage.shouldHaveForm("HaveSoftware Form")(actionCall = testCall)

    "have a set of radio inputs" which {
      lazy val doc = Jsoup.parse(page.body)
      "for the option Accounting Software" in {
        val result = doc.select(s"label[for=${messages.accounting_software}]").text()
        result shouldEqual messages.accounting_software
      }
      "for the option Spreadsheets" in {
        val result = doc.select(s"label[for=${messages.spreadsheets}]").text()
        result shouldEqual messages.spreadsheets
      }
      "for the option Neither" in {
        val result = doc.select(s"label[for=${messages.neither}]").text()
        result shouldEqual messages.neither
      }
    }
    testPage.shouldHaveContinueButton()
  }
}
