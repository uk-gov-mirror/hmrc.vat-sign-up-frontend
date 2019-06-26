
package uk.gov.hmrc.vatsignupfrontend.connectors

import play.api.http.Status._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.helpers.ComponentSpecBase
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.SubscriptionRequestSummaryStub
import uk.gov.hmrc.vatsignupfrontend.httpparsers.SubscriptionRequestSummaryHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.{Digital, GeneralPartnership, SubscriptionRequestSummary}


class SubscriptionRequestSummaryConnectorISpec extends ComponentSpecBase {

  lazy val connector: SubscriptionRequestSummaryConnector = app.injector.instanceOf[SubscriptionRequestSummaryConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

 "getSubscriptionRequest" should {
   "return Right SubscriptionRequestSummary for an OK response" in {
     val model =  SubscriptionRequestSummary(
       vatNumber = "vatNumber",
       businessEntity = GeneralPartnership,
       optSignUpEmail = Some("fooEmail"),
       transactionEmail = "barEmail",
       contactPreference = Digital
     )

     SubscriptionRequestSummaryStub.stubGetSubscriptionRequest("vatNumber")(OK, Some(model))
     val res = connector.getSubscriptionRequest("vatNumber")

     await(res).right.get shouldBe model
   }
   "return Left SubscriptionRequestDoesNotExist for an OK response with invalid json" in {
     SubscriptionRequestSummaryStub.stubGetSubscriptionRequestInvalidJson("vatNumber")(OK)
     val res = connector.getSubscriptionRequest("vatNumber")

     await(res).left.get shouldBe SubscriptionRequestUnexpectedError(OK,"JSON does not meet read requirements of SubscriptionRequestSummary")
   }
   "return Left SubscriptionRequestExistsButNotComplete for BAD_REQUEST response" in {
     SubscriptionRequestSummaryStub.stubGetSubscriptionRequest("vatNumber")(BAD_REQUEST, None)
     val res = connector.getSubscriptionRequest("vatNumber")

     await(res).left.get shouldBe SubscriptionRequestExistsButNotComplete
   }
   "return Left SubscriptionRequestDoesNotExist for NOT_FOUND response" in {
     SubscriptionRequestSummaryStub.stubGetSubscriptionRequest("vatNumber")(NOT_FOUND, None)
     val res = connector.getSubscriptionRequest("vatNumber")

     await(res).left.get shouldBe SubscriptionRequestDoesNotExist
   }
   "return Left SubscriptionRequestUnexpectedError for INTERNAL_SERVER_ERROR response" in {
     SubscriptionRequestSummaryStub.stubGetSubscriptionRequest("vatNumber")(INTERNAL_SERVER_ERROR, None)
     val res = connector.getSubscriptionRequest("vatNumber")

     await(res).left.get shouldBe SubscriptionRequestUnexpectedError(INTERNAL_SERVER_ERROR,"Unexpected status from Backend")
   }
 }
}
