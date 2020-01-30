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

package uk.gov.hmrc.vatsignupfrontend.services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, MigratableDates, PostCode}
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService._

import scala.concurrent.Future


trait MockStoreVatNumberService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockStoreVatNumberService: StoreVatNumberService = mock[StoreVatNumberService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockStoreVatNumberService)
  }

  def mockStoreVatNumberDelegated(vatNumber: String)(returnValue: Future[DelegatedStoreVatNumberResponse]): Unit =
    when(mockStoreVatNumberService.storeVatNumberDelegated(
      ArgumentMatchers.eq(vatNumber)
    )(ArgumentMatchers.any[HeaderCarrier]))
      .thenReturn(returnValue)

  def mockStoreVatNumber(vatNumber: String, isFromBta: Boolean)(returnValue: Future[StoreVatNumberWithEnrolmentResponse]): Unit =
    when(mockStoreVatNumberService.storeVatNumber(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(isFromBta)
    )(ArgumentMatchers.any[HeaderCarrier]))
      .thenReturn(returnValue)

  def mockStoreVatNumberSuccess(vatNumber: String, isFromBta: Boolean): Unit =
    mockStoreVatNumber(vatNumber, isFromBta)(Future.successful(Right(VatNumberStored(isOverseas = false, isDirectDebit = false))))

  def mockStoreVatNumberOverseasSuccess(vatNumber: String, isFromBta: Boolean): Unit =
    mockStoreVatNumber(vatNumber, isFromBta)(Future.successful(Right(VatNumberStored(isOverseas = true, isDirectDebit = false))))

  def mockStoreVatNumberDirectDebitSuccess(vatNumber: String, isFromBta: Boolean): Unit =
    mockStoreVatNumber(vatNumber, isFromBta)(Future.successful(Right(VatNumberStored(isOverseas = false, isDirectDebit = true))))

  def mockStoreVatNumberSubscriptionClaimed(vatNumber: String, isFromBta: Boolean): Unit =
    mockStoreVatNumber(vatNumber, isFromBta)(Future.successful(Right(SubscriptionClaimed)))

  def mockStoreVatNumberMigrationInProgress(vatNumber: String, isFromBta: Boolean): Unit =
    mockStoreVatNumber(vatNumber, isFromBta)(Future.successful(Left(VatMigrationInProgress)))

  def mockStoreVatNumberAlreadyEnrolled(vatNumber: String, isFromBta: Boolean): Unit =
    mockStoreVatNumber(vatNumber, isFromBta)(Future.successful(Left(VatNumberAlreadyEnrolled)))

  def mockStoreVatNumberIneligible(vatNumber: String, isFromBta: Boolean, migratableDates: MigratableDates): Unit =
    mockStoreVatNumber(vatNumber, isFromBta)(Future.successful(Left(IneligibleVatNumber(migratableDates))))

  def mockStoreVatNumber(vatNumber: String,
                         optPostCode: Option[PostCode],
                         registrationDate: DateModel,
                         optBox5Figure: Option[String],
                         optLastReturnMonth: Option[String],
                         isFromBta: Boolean
                        )(returnValue: Future[StoreVatNumberWithKnownFactsResponse]): Unit =
    when(mockStoreVatNumberService.storeVatNumber(
      ArgumentMatchers.eq(vatNumber),
      ArgumentMatchers.eq(optPostCode),
      ArgumentMatchers.eq(registrationDate),
      ArgumentMatchers.eq(optBox5Figure),
      ArgumentMatchers.eq(optLastReturnMonth),
      ArgumentMatchers.eq(isFromBta)
    )(ArgumentMatchers.any[HeaderCarrier]))
      .thenReturn(returnValue)

  def mockStoreVatNumberSuccess(vatNumber: String,
                                optPostCode: Option[PostCode],
                                registrationDate: DateModel,
                                optBox5Figure: Option[String],
                                optLastReturnMonth: Option[String],
                                isFromBta: Boolean
                               ): Unit =
    mockStoreVatNumber(
      vatNumber, optPostCode, registrationDate, optBox5Figure, optLastReturnMonth, isFromBta
    )(Future.successful(Right(VatNumberStored(isOverseas = false, isDirectDebit = false))))

  def mockStoreVatNumberDirectDebitSuccess(vatNumber: String,
                                optPostCode: Option[PostCode],
                                registrationDate: DateModel,
                                optBox5Figure: Option[String],
                                optLastReturnMonth: Option[String],
                                isFromBta: Boolean
                               ): Unit =
    mockStoreVatNumber(
      vatNumber, optPostCode, registrationDate, optBox5Figure, optLastReturnMonth, isFromBta
    )(Future.successful(Right(VatNumberStored(isOverseas = false, isDirectDebit = true))))

  def mockStoreVatNumberSubscriptionClaimed(vatNumber: String,
                                            optPostCode: Option[PostCode],
                                            registrationDate: DateModel,
                                            optBox5Figure: Option[String],
                                            optLastReturnMonth: Option[String],
                                            isFromBta: Boolean
                                           ): Unit =
    mockStoreVatNumber(
      vatNumber, optPostCode, registrationDate, optBox5Figure, optLastReturnMonth, isFromBta
    )(Future.successful(Right(SubscriptionClaimed)))

  def mockStoreVatNumberMigrationInProgress(vatNumber: String,
                                            optPostCode: Option[PostCode],
                                            registrationDate: DateModel,
                                            optBox5Figure: Option[String],
                                            optLastReturnMonth: Option[String],
                                            isFromBta: Boolean
                                           ): Unit =
    mockStoreVatNumber(
      vatNumber, optPostCode, registrationDate, optBox5Figure, optLastReturnMonth, isFromBta
    )(Future.successful(Left(VatMigrationInProgress)))

  def mockStoreVatNumberFailure(vatNumber: String,
                                optPostCode: Option[PostCode],
                                registrationDate: DateModel,
                                optBox5Figure: Option[String],
                                optLastReturnMonth: Option[String],
                                isFromBta: Boolean
                               ): Unit =
    mockStoreVatNumber(
      vatNumber, optPostCode, registrationDate, optBox5Figure, optLastReturnMonth, isFromBta
    )(Future.successful(Left(StoreVatNumberFailureResponse(INTERNAL_SERVER_ERROR))))

  def mockStoreVatNumberKnownFactsMismatch(vatNumber: String,
                                           optPostCode: Option[PostCode],
                                           registrationDate: DateModel,
                                           optBox5Figure: Option[String],
                                           optLastReturnMonth: Option[String],
                                           isFromBta: Boolean
                                          ): Unit =
    mockStoreVatNumber(
      vatNumber, optPostCode, registrationDate, optBox5Figure, optLastReturnMonth, isFromBta
    )(Future.successful(Left(KnownFactsMismatch)))

  def mockStoreVatNumberInvalid(vatNumber: String,
                                optPostCode: Option[PostCode],
                                registrationDate: DateModel,
                                optBox5Figure: Option[String],
                                optLastReturnMonth: Option[String],
                                isFromBta: Boolean
                               ): Unit =
    mockStoreVatNumber(
      vatNumber, optPostCode, registrationDate, optBox5Figure, optLastReturnMonth, isFromBta
    )(Future.successful(Left(InvalidVatNumber)))

  def mockStoreVatNumberAlreadyEnrolled(vatNumber: String,
                                        optPostCode: Option[PostCode],
                                        registrationDate: DateModel,
                                        optBox5Figure: Option[String],
                                        optLastReturnMonth: Option[String],
                                        isFromBta: Boolean
                                       ): Unit =
    mockStoreVatNumber(
      vatNumber, optPostCode, registrationDate, optBox5Figure, optLastReturnMonth, isFromBta
    )(Future.successful(Left(VatNumberAlreadyEnrolled)))

  def mockStoreVatNumberIneligible(vatNumber: String,
                                   optPostCode: Option[PostCode],
                                   registrationDate: DateModel,
                                   optBox5Figure: Option[String],
                                   optLastReturnMonth: Option[String],
                                   isFromBta: Boolean,
                                   migratableDates: MigratableDates = MigratableDates()
                                  ): Unit =
    mockStoreVatNumber(
      vatNumber, optPostCode, registrationDate, optBox5Figure, optLastReturnMonth, isFromBta
    )(Future.successful(Left(IneligibleVatNumber(migratableDates))))

}
