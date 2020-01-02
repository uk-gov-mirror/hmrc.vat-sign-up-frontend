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

package uk.gov.hmrc.vatsignupfrontend.views.principal.eligibility

import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{PrincipalReturnDue => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec


class ReturnDueSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.principal.eligibility.return_due()(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  "The Return Due view" should {
    val testPage = TestView(
      name = "Return Due View",
      title = messages.title,
      heading = messages.heading,
      page = page
    )
    testPage.shouldHaveParaSeq(
      messages.line_1,
      messages.line_2,
      messages.line_3
    )
    testPage.shouldHaveALink(messages.link_id, messages.link_text, messages.link)

  }
}
