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

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.play.test.UnitSpec
import assets.MessageLookup.{Base => common}

trait ViewSpec extends UnitSpec with GuiceOneAppPerSuite {

  val testBackUrl = "/test-back-url"
  val testCall = Call("POST", "/test-url")
  val viewTestRequest = FakeRequest("POST", "/test-url")

  trait ElementTest {
    val name: String

    val element: Element

    def shouldHavePara(paragraph: String): Unit =
      s"$name should have the paragraph (P) '$paragraph'" in {
        element.getElementsByTag("p").text() should include(paragraph)
      }

    def shouldHaveTextField(name: String,
                          label: String,
                          showLabel: Boolean = true,
                          maxLength: Option[Int] = None,
                          pattern: Option[String] = None,
                          inputMode: Option[String] = None,
                          hint: Option[String] = None
                         ): Unit = {

      s"${this.name} should have an input field '$name'" which {

        s"is a text field" in {
          import collection.JavaConversions._
          val eles = element.select(s"""input[name=$name]""")
          if (eles.isEmpty) fail(s"$name does not have an input field with name=$name\ncurrent list of inputs:\n[${element.select("input")}]")
          if (eles.size() > 1) fail(s"$name have multiple input fields with name=$name")
          val ele = eles.head
          ele.attr("type") shouldBe "text"
          maxLength.map {
            l => ele.attr("maxLength") shouldBe l.toString
          }
          pattern.map {
            p => ele.attr("pattern") shouldBe p
          }
          inputMode.map {
            m => ele.attr("inputMode") shouldBe m
          }
        }

        lazy val labelField = element.select(s"label[for=$name]")

        s"with the expected label label '$label'" in {
          labelField.text() shouldBe (label + hint.fold("")(" " + _))
        }

        if (!showLabel)
          s"and the label should be visuallyhidden" in
            withClue(s"$name does not have the class 'visuallyhidden'\n") {
              labelField.hasClass("visuallyhidden") shouldBe true
            }

      }

    }


    private def shouldHaveSubmitButton(text: String): Unit =
      s"$name should have the a submit button (Button) '$text'" in {
        import collection.JavaConversions._
        val submitButtons = element.select("button").filter(_.attr("type") == "submit")
        submitButtons.size shouldBe 1
        submitButtons.head.text() shouldBe text
      }

    val shouldHaveContinueButton = shouldHaveSubmitButton(common.continue)

    val shouldHaveContinueToSignUpButton = shouldHaveSubmitButton(common.continueToSignUp)

    val shouldHaveUpdateButton = shouldHaveSubmitButton(common.update)

    val shouldHaveGoBackButton = shouldHaveSubmitButton(common.goBack)
  }


  class TestView(override val name: String,
                 title: String,
                 heading: String,
                 page: => Html) extends ElementTest {

    lazy val document: Document = Jsoup.parse(page.body)
    override lazy val element: Element = document.getElementById("content")

    s"$name should have the title '$title'" in {
      document.title() shouldBe title
    }

    s"$name should have the heading (H1) '$heading'" in {
      val h1 = document.getElementsByTag("H1")
      h1.size() shouldBe 1
      h1.text() shouldBe heading
    }

  }

  object TestView {
    def apply(name: String,
              title: String,
              heading: String,
              page: => Html): TestView = new TestView(name, title, heading, page)
  }
}