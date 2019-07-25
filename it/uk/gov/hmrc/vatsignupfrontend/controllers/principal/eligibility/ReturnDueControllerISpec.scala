package uk.gov.hmrc.vatsignupfrontend.controllers.principal.eligibility

import play.mvc.Http.Status.OK
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.eligibility
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class ReturnDueControllerISpec extends ComponentSpecBase with CustomMatchers {
  s"GET ${eligibility.routes.ReturnDueController.show().url}" should {
    s"return $OK" in {
      val res = get("/error/return-due")

      res should have(
        httpStatus(OK)
      )
    }
  }
}
