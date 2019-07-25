
package uk.gov.hmrc.vatsignupfrontend.controllers.principal.eligibility

import play.mvc.Http.Status._
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.eligibility
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class NotGotSoftwareControllerISpec extends ComponentSpecBase with CustomMatchers {

  s"GET ${eligibility.routes.NotGotSoftwareController.show().url}" should {
    s"return $OK" in {
      val res = get("/interruption/not-got-software")

      res should have(
        httpStatus(OK)
      )
    }
  }
}
