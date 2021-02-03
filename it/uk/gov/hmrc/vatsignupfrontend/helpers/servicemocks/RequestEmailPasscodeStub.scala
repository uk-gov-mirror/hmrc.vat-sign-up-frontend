
package uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants.testEmail

object RequestEmailPasscodeStub extends WireMockMethods {

  def stubRequestEmailPasscode(status: Int): StubMapping =
    when(
      uri = "/email-verification/request-passcode",
      method = POST,
      body = Json.obj("email" -> testEmail, "serviceName" -> "VAT Signup", "lang" -> "en")
    ).thenReturn(status)

}
