/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.error

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.VatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships.{routes => partnershipRoutes}
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.{routes => agentRoutes}
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.{LimitedCompany, LimitedPartnership, RegisteredSociety}
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.dissolved_company

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DissolvedCompanyController @Inject()(implicit ec: ExecutionContext,
                                             vcc: VatControllerComponents)
  extends AuthenticatedController(AgentEnrolmentPredicate) {

  val show: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        val optCompanyName = request.session.get(SessionKeys.companyNameKey).filter(_.nonEmpty)
        val optBusinessEntity = request.session.get(SessionKeys.businessEntityKey).flatMap(str => BusinessEntitySessionFormatter.fromString(str))
        val redirectRoute = optBusinessEntity match {
          case Some(LimitedCompany) =>
            agentRoutes.CaptureCompanyNumberController.show()
          case Some(LimitedPartnership) =>
            partnershipRoutes.AgentCapturePartnershipCompanyNumberController.show()
          case Some(RegisteredSociety) =>
            agentRoutes.CaptureRegisteredSocietyCompanyNumberController.show()
          case _ =>
            agentRoutes.CaptureBusinessEntityController.show()
        }

        optCompanyName match {
          case Some(companyName) =>
            Future.successful(Ok(dissolved_company(redirectUrl = redirectRoute.url, companyName = companyName)))
          case _ =>
            Future.successful(Redirect(redirectRoute).removingFromSession(SessionKeys.companyNameKey))
        }
      }
  }
}
