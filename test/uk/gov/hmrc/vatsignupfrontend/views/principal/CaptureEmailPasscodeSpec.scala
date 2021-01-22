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

package uk.gov.hmrc.vatsignupfrontend.views.principal

import org.jsoup.Jsoup
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.forms.EmailPasscodeForm
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.capture_email_passcode


class CaptureEmailPasscodeSpec extends ViewSpec {

  val request = FakeRequest()
  lazy val messages = app.injector.instanceOf[MessagesApi].preferred(request)
  lazy val appConfig = app.injector.instanceOf[AppConfig]
  lazy val view = app.injector.instanceOf[capture_email_passcode]

  val testEmail = "email@email.com"

  lazy val appliedView = view(EmailPasscodeForm(), testEmail, Call("POST", "/test/post/route"))(request, messages, appConfig)
  lazy val doc = Jsoup.parse(appliedView.body)

  object ExpectedContent {
    val h1 = "Enter the code to confirm your email address"
    val p1 = s"We have sent a code to: $testEmail"
    val p2 = "If you use a browser to access your email, you may need to open a new window or tab to see the code."
    val label = "Confirmation code"
    val hint = "For example, DNCLRK"
  }

  "the capture email verification code view" should {
    "have a H1 heading" in {
      doc.select("h1").first.text shouldBe ExpectedContent.h1
    }
    "have a first paragraph" in {
      doc.select("main p").get(2).text shouldBe ExpectedContent.p1
    }
    "have an inset panel" in {
      doc.select(".panel-border-wide p").first.text shouldBe ExpectedContent.p2
    }
    "have a label corresponding to the text input" in {
      val label = doc.select("label").first
      label.text shouldBe ExpectedContent.label
      label.attr("for") shouldBe "verificationCode"
    }
    "have a field hint" in {
      doc.select(".form-hint").first.text shouldBe ExpectedContent.hint
    }
  }

}
