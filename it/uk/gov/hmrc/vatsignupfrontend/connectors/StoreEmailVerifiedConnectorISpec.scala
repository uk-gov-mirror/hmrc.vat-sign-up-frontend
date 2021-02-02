
package uk.gov.hmrc.vatsignupfrontend.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.helpers.ComponentSpecBase
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreEmailAddressStub.stubStoreTransactionEmailVerified
import uk.gov.hmrc.vatsignupfrontend.models._

class StoreEmailVerifiedConnectorISpec extends ComponentSpecBase {

  val connector = app.injector.instanceOf[StoreEmailVerifiedConnector]

  implicit val hc = HeaderCarrier()

  "the store email verified connector" should {
    "return StoreEmailVerifiedSuccess when the API returns CREATED" in {
      stubStoreTransactionEmailVerified(CREATED)

      val res = await(connector.storeEmailVerified(testVatNumber, testEmail))

      res shouldBe StoreEmailVerifiedSuccess
    }
    "return NoVatNumber when the API returns NOT_FOUND" in {
      stubStoreTransactionEmailVerified(NOT_FOUND)

      val res = await(connector.storeEmailVerified(testVatNumber, testEmail))

      res shouldBe NoVatNumber
    }
    "return StoreEmailVerifiedFailed wwth the status when the API returns anything else" in {
      stubStoreTransactionEmailVerified(IM_A_TEAPOT)

      val res = await(connector.storeEmailVerified(testVatNumber, testEmail))

      res shouldBe StoreEmailVerifiedFailed(IM_A_TEAPOT)
    }
  }

}
