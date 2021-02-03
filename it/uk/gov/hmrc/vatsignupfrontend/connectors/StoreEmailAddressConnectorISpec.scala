
package uk.gov.hmrc.vatsignupfrontend.connectors

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.helpers.ComponentSpecBase
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreEmailAddressStub._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreEmailAddressHttpParser._

import scala.concurrent.ExecutionContext.Implicits.global

class StoreEmailAddressConnectorISpec extends ComponentSpecBase {

  val transactionEmailKey = "transactionEmail"

  val connector = app.injector.instanceOf[StoreEmailAddressConnector]
  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val requestJson = Json.obj(transactionEmailKey -> testEmail)

  "storeTransactionEmailAddressVerified" should {
    "return StoreEmailAddressSuccess when the address has been verified and stored successfully" in {
      stubStoreTransactionEmailAddressSuccess(emailVerified = true)

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail))

      res shouldBe Right(StoreEmailAddressSuccess(emailVerified = true))
    }
    "return StoreEmailAddressFailure when the email is not stored" in {
      stubStoreTransactionEmailAddressFailure()

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail))

      res shouldBe Left(StoreEmailAddressFailureStatus(INTERNAL_SERVER_ERROR))
    }
  }

}
