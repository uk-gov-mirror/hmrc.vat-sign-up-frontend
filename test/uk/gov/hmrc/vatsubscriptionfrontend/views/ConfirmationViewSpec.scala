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

package uk.gov.hmrc.vatsubscriptionfrontend.views

import uk.gov.hmrc.vatsubscriptionfrontend.assets.MessageLookup.{Confirmation => messages}
import org.jsoup.Jsoup
import play.api.{Configuration, Environment}
import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import uk.gov.hmrc.vatsubscriptionfrontend.config.AppConfig

class ConfirmationViewSpec extends ViewSpec {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  lazy val messagesApi = app.injector.instanceOf[MessagesApi]

  lazy val page = uk.gov.hmrc.vatsubscriptionfrontend.views.html.confirmation(
    postAction = testCall)(
    FakeRequest(),
    applicationMessages,
    new AppConfig(configuration, env)
  )

  lazy val document = Jsoup.parse(page.body)

  "The Confirmation view" should {

    s"have the title '${messages.title}'" in {
      document.title() should be(messages.title)
    }

    "have a confirmation banner" which {

      s"has a heading (H1)" which {

        s"has the text '${messages.heading}'" in {
          document.select("h1").text() shouldBe messages.heading
        }
      }

    }

    "have a 'What happens next' section" which {

      s"has the section heading '${messages.Section1.heading}'" in {
        document.select("#what-happens-next h2").text() shouldBe messages.Section1.heading
      }

      s"has a paragraph stating HMRC process '${messages.Section1.line1}'" in {
        document.select("#what-happens-next p").text() should include(messages.Section1.line1)
      }

    }

    "have a 'When your client’s information is approved' section" which {

      s"has the section heading '${messages.Section2.heading}'" in {
        document.select("#when-client-is-approved h2").text() shouldBe messages.Section2.heading
      }

      s"has a paragraph '${messages.Section2.line1}'" in {
        document.select("#when-client-is-approved p").text() should include(messages.Section2.line1)
      }

      s"has a bullet point '${messages.Section2.bullet1}'" in {
        document.select("#when-client-is-approved li").text() should include(messages.Section2.bullet1)
      }

      s"has a bullet point '${messages.Section2.bullet2}'" in {
        document.select("#when-client-is-approved li").text() should include(messages.Section2.bullet2)
      }

      s"has a bullet point '${messages.Section2.bullet3}'" in {
        document.select("#when-client-is-approved li").text() should include(messages.Section2.bullet3)
      }

      s"has a bullet point '${messages.Section2.bullet4}'" in {
        document.select("#when-client-is-approved li").text() should include(messages.Section2.bullet4)
      }

      s"has a paragraph '${messages.Section2.line2}'" in {
        document.select("#when-client-is-approved p").text() should include(messages.Section2.line2)
      }

    }

    "have a add another client button" in {
      val b = document.getElementById("add-another-button")
      b.attr("value") shouldBe messages.addAnother
    }

  }
}
