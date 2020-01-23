/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.VatControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreOverseasInformationHttpParser.{StoreOverseasInformationFailureResponse, StoreOverseasInformationSuccess}
import uk.gov.hmrc.vatsignupfrontend.models.Overseas
import uk.gov.hmrc.vatsignupfrontend.services.StoreOverseasInformationService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OverseasResolverController @Inject()(storeOverseasInformationService: StoreOverseasInformationService)
                                          (implicit ec: ExecutionContext,
                                           vcc: VatControllerComponents)
  extends AuthenticatedController(AgentEnrolmentPredicate) {

  val resolve: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)

      optVatNumber match {
        case Some(vatNumber) =>
          storeOverseasInformationService.storeOverseasInformation(vatNumber) map {
            case Right(StoreOverseasInformationSuccess) =>
              Redirect(routes.CaptureAgentEmailController.show())
                .addingToSession(SessionKeys.businessEntityKey -> Overseas.toString)
            case Left(StoreOverseasInformationFailureResponse(status)) =>
              throw new InternalServerException("store overseas information failed: status =" + status)
          }
        case _ =>
          Future.successful(Redirect(routes.CaptureVatNumberController.show()))
      }
    }
  }

}
