
package uk.gov.hmrc.vatsignupfrontend.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.helpers.ComponentSpecBase
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.connectors.NewStoreEmailAddressConnector._
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreEmailAddressStub._

class NewStoreEmailAddressConnectorISpec extends ComponentSpecBase {

  val connector = app.injector.instanceOf[NewStoreEmailAddressConnector]
  implicit val hc = HeaderCarrier()

  val reasonKey = "reason"
  val transactionEmailKey = "transactionEmail"
  val passcodeKey = "passCode"

  val requestJson = Json.obj(transactionEmailKey -> testEmail, passcodeKey -> testPasscode)

  "New store email address connector" should {
    "return NewStoreEmailAddressSuccess" in {
      stubStoreTransactionEmailVerified(requestJson)(CREATED, Json.obj())

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail, testPasscode))

      res shouldBe Right(NewStoreEmailAddressSuccess)
    }
    "return PasscodeMismatch" in {
      val responseJson = Json.obj("reason" -> "PASSCODE_MISMATCH")
      stubStoreTransactionEmailVerified(requestJson)(BAD_REQUEST, responseJson)

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail, testPasscode))

      res shouldBe Left(PasscodeMismatch)
    }
    "return PasscodeNotFound" in {
      val responseJson = Json.obj("reason" -> "PASSCODE_NOT_FOUND")
      stubStoreTransactionEmailVerified(requestJson)(NOT_FOUND, responseJson)

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail, testPasscode))

      res shouldBe Left(PasscodeNotFound)
    }
    "return MaxAttemptsExceeded" in {
      val responseJson = Json.obj("reason" -> "MAX_PASSCODE_ATTEMPTS_EXCEEDED")
      stubStoreTransactionEmailVerified(requestJson)(BAD_REQUEST, responseJson)

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail, testPasscode))

      res shouldBe Left(MaxAttemptsExceeded)
    }
    "return NewStoreEmailAddressFailureStatus with the specific status code" in {
      stubStoreTransactionEmailVerified(requestJson)(INTERNAL_SERVER_ERROR, Json.obj())

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail, testPasscode))

      res shouldBe Left(NewStoreEmailAddressFailureStatus(INTERNAL_SERVER_ERROR))
    }
  }

}
