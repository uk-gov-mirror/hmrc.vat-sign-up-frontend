
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

  val reasonKey = "reason"
  val transactionEmailKey = "transactionEmail"
  val passcodeKey = "passcode"

  val connector = app.injector.instanceOf[StoreEmailAddressConnector]
  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val requestJson = Json.obj(transactionEmailKey -> testEmail, passcodeKey -> testPasscode)

  "storeTransactionEmailAddressVerified" should {
    "return StoreEmailAddressSuccess when the address has been verified and stored successfully" in {
      val responseJson = Json.obj("reason" -> "OK")
      stubStoreTransactionEmailVerified(requestJson)(OK, responseJson)

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail, testPasscode))

      res shouldBe Right(StoreEmailAddressSuccess(emailVerified = true))
    }
    "return PasscodeMismatch" in {
      val responseJson = Json.obj("reason" -> "PASSCODE_MISMATCH")
      stubStoreTransactionEmailVerified(requestJson)(BAD_REQUEST, responseJson)

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail, testPasscode))

      res shouldBe Left(PasscodeMismatch)
    }
    "return PasscodeNotFound" in {
      val responseJson = Json.obj("reason" -> "PASSCODE_NOT_FOUND")
      stubStoreTransactionEmailVerified(requestJson)(BAD_REQUEST, responseJson)

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail, testPasscode))

      res shouldBe Left(PasscodeNotFound)
    }
    "return MaxAttemptsExceeded" in {
      val responseJson = Json.obj("reason" -> "MAX_PASSCODE_ATTEMPTS_EXCEEDED")
      stubStoreTransactionEmailVerified(requestJson)(BAD_REQUEST, responseJson)

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail, testPasscode))

      res shouldBe Left(MaxAttemptsExceeded)
    }
    "return StoreEmailAddressFailure when we receive an unexpected reason" in {
      val responseJson = Json.obj("reason" -> "BIBBLE")
      stubStoreTransactionEmailVerified(requestJson)(BAD_REQUEST, responseJson)

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail, testPasscode))

      res shouldBe Left(StoreEmailAddressFailureStatus(BAD_REQUEST))
    }
    "return StoreEmailAddressFailure when we receive an unexpected status code" in {
      val responseJson = Json.obj()
      stubStoreTransactionEmailVerified(requestJson)(IM_A_TEAPOT, responseJson)

      val res = await(connector.storeTransactionEmailAddress(testVatNumber, testEmail, testPasscode))

      res shouldBe Left(StoreEmailAddressFailureStatus(IM_A_TEAPOT))
    }

  }

}
