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

package uk.gov.hmrc.vatsignupfrontend.views.principal

import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup.{Base, SignUpCompleteClient => messages}
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.models.SoleTrader
import uk.gov.hmrc.vatsignupfrontend.views.ViewSpec


class SignUpCompleteClientViewSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  lazy val page = uk.gov.hmrc.vatsignupfrontend.views.html.principal.sign_up_complete_client(SoleTrader)(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  lazy val document = Jsoup.parse(page.body)

  "The information received view" should {

    s"have the title '${messages.title}'" in {
      document.title() should be(messages.title)
    }

    "have the business entity type on a data attribute embedded exactly once" in {
      document.select(s"""[data-entity-type="${SoleTrader.toString}"]""").size() shouldBe 1
    }

    "have a confirmation banner" which {

      s"has a heading (H1)" which {

        s"has the text '${messages.heading}'" in {
          document.select("h1").text() shouldBe messages.heading
        }
      }

    }

    "have a 'What happens next' section" which {

      s"has the section heading '${messages.subheadingWhn}'" in {
        document.select("#what-happens-next h2").text() shouldBe messages.subheadingWhn
      }

      s"has a paragraph ${messages.whnLine1}'" in {
        document.select("#what-happens-next p").text() should include(messages.whnLine1)
      }

      s"has a paragraph '${messages.whnLine2}'" in {
        document.select("#what-happens-next p").text() should include(messages.whnLine2)
      }

    }

    "have a 'business tax account' section" which {

      s"has the section heading '${messages.subheadingBta}'" in {
        document.select("#business-tax-account h2").text() shouldBe messages.subheadingBta
      }

      s"has a paragraph that contains '${messages.btaLine1}'" in {
        document.select("#business-tax-account p").text() should include(messages.btaLine1)
      }

      s"has a paragraph that contains '${messages.btaLine2}'" in {
        document.select("#business-tax-account p").text() should include(messages.btaLine2)
      }

      s"has a bullet point '${messages.bullet1}'" in {
        document.select("#business-tax-account li").text() should include(messages.bullet1)
      }

      s"has a bullet point '${messages.bullet2}'" in {
        document.select("#business-tax-account li").text() should include(messages.bullet2)
      }

      s"has a bullet point '${messages.bullet3}'" in {
        document.select("#business-tax-account li").text() should include(messages.bullet3)
      }

      s"has a bullet point '${messages.bullet4}'" in {
        document.select("#business-tax-account li").text() should include(messages.bullet4)
      }

    }

    "have a sign out button" in {
      val b = document.getElementById("sign-out-button")
      b.text shouldBe Base.signOut
    }

  }
}
