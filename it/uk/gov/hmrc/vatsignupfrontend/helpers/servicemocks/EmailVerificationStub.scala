
package uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._

object EmailVerificationStub extends WireMockMethods {

  def stubVerifyEmailPasscode(status: Int, body: JsValue): StubMapping =
    when(
      method = POST,
      uri = "/email-verification/verify-passcode",
      headers = Map(),
      body = Json.obj("email" -> testEmail, "passcode" -> testPasscode)
    ) thenReturn (status, body)

  def stubRequestPasscode(status: Int): StubMapping =
    when(
      method = POST,
      uri = "/email-verification/request-passcode",
      headers = Map(),
      body = Json.obj("email" -> testEmail, "serviceName" -> "VAT Signup", "lang" -> "en")
    ) thenReturn (status, Json.obj())

}