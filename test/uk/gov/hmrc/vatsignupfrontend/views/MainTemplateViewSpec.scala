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

package uk.gov.hmrc.vatsignupfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig

class MainTemplateViewSpec extends ViewSpec {
  val env = Environment.simple()
  val configuration = Configuration.load(env)
  lazy val messagesApi = app.injector.instanceOf[MessagesApi]
  val testCountdown = "1234"
  val testTimeOut = "87913"

  def page(enabledTimeout: Boolean = true, showSignOutLink: Boolean = true, hasAuthToken: Boolean = false) = uk.gov.hmrc.vatsignupfrontend.views.html.main_template(
    navTitle = None,
    title = "testTitle",
    sidebarLinks = None,
    contentHeader = None,
    bodyClasses = None,
    mainClass = None,
    scriptElem = None,
    showSignOutLink = showSignOutLink,
    isAgent = false,
    enableTimeout = enabledTimeout)(HtmlFormat.empty)(
    if (hasAuthToken) FakeRequest().withSession(SessionKeys.authToken -> "")
    else FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env) {
      override lazy val timeoutLength = testTimeOut
      override lazy val countdownLength = testCountdown
    }
  )

  class Setup(enabledTimeOut: Boolean = true, showSignOutLink: Boolean = true, hasAuthToken: Boolean = false) {
    val doc: Document = Jsoup.parse(page(enabledTimeOut, showSignOutLink, hasAuthToken).body)
  }

  "main template view" should {
    "display the title" in new Setup {
      doc.title shouldBe "testTitle"
    }

    "show timeout" in new Setup(true) {
      val timeoutScriptText = doc.getElementById("timeoutScript").html()
      timeoutScriptText.contains(testCountdown) shouldBe true
      timeoutScriptText.contains(testTimeOut) shouldBe true
    }

    "not show timeout" in new Setup(false) {
      Option(doc.getElementById("timeoutScript")) shouldBe None
    }

    "not show the sign out link when the user is logged out" in new Setup(showSignOutLink = false, hasAuthToken = false) {
      doc.getElementById("logOutNavHref") should be(null)
    }

    "show the sign out link when the user is logged out but the page has specified to always show it" in new Setup(showSignOutLink = true, hasAuthToken = false) {
      doc.getElementById("logOutNavHref") shouldNot be(null)
    }

    "show the sign out link when the user is logged in" in new Setup(showSignOutLink = false, hasAuthToken = true) {
      doc.getElementById("logOutNavHref") shouldNot be(null)
    }
  }

}
