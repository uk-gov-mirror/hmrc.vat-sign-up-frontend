
package uk.gov.hmrc.vatsignupfrontend.controllers.principal.eligibility

import play.mvc.Http.Status.OK
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.eligibility
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class MakingTaxDigitalSoftwareControllerISpec extends ComponentSpecBase with CustomMatchers {

  s"GET ${eligibility.routes.MakingTaxDigitalSoftwareController.show().url}" should {
    s"return $OK" in {
      val res = get("/interruption/making-tax-digital-software")

      res should have(
        httpStatus(OK)
      )
    }
  }
}
