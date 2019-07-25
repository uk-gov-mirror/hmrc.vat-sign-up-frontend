
package uk.gov.hmrc.vatsignupfrontend.controllers.principal.eligibility

import play.mvc.Http.Status._
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.eligibility
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class GotSoftwareControllerISpec extends ComponentSpecBase with CustomMatchers {

  s"GET ${eligibility.routes.GotSoftwareController.show().url}" should {
    s"return $OK" in {
      val res = get("/interruption/got-software")

      res should have(
        httpStatus(OK)
      )
    }
  }
}
