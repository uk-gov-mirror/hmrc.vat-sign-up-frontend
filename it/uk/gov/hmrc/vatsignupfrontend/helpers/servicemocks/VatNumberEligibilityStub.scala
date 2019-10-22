
package uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.httpparsers.VatNumberEligibilityHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.MigratableDates

object VatNumberEligibilityStub extends WireMockMethods {

  def stubMigratedVatNumberEligibilitySuccess(vatNumber: String)
                                             (mtdStatusResponse: String,
                                              migratableDates: Option[MigratableDates] = None,
                                              eligibilityDetails: Option[Eligible] = None): Unit = {
    when(method = GET, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber/new-mtdfb-eligibility")
      .thenReturn(status = OK,
        Json.obj(
          MtdStatusKey -> mtdStatusResponse,
          MigratableDatesKey -> migratableDates,
          EligibilityDetailsKey -> eligibilityDetails
        ))
  }
}
