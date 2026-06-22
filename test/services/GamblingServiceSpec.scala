/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import connectors.GamblingConnector
import models.{SubmittedReturns, SubmittedReturnsItem}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class GamblingServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val regNumber = "XWM00003102200"

  "GamblingService#getSubmittedReturns" should {

    val submittedReturnsResponse = SubmittedReturns(
      items = Seq(
        SubmittedReturnsItem(consec_no      = 12345,
                             mgd_period     = "01/01/2025 - 30/03/2025",
                             submitted_date = LocalDate.of(2025, 4, 1),
                             ack_ref        = "123456789012345"
                            )
      )
    )
    val sortBy = 2
    val orderBy = "DESC"

    "must delegate to the connector with the correct arguments and return its result" in {
      val mockConnector = mock[GamblingConnector]
      when(mockConnector.getSubmittedReturns(eqTo(regNumber), eqTo(sortBy), eqTo(orderBy))(using any[HeaderCarrier]()))
        .thenReturn(Future.successful(submittedReturnsResponse))

      val service = new GamblingService(mockConnector)
      val result = service.getSubmittedReturns(regNumber, sortBy, orderBy).futureValue

      result mustEqual submittedReturnsResponse
    }

    "must propagate failures from the connector" in {
      val mockConnector = mock[GamblingConnector]
      val exception = new RuntimeException("upstream failure")
      when(mockConnector.getSubmittedReturns(eqTo(regNumber), eqTo(sortBy), eqTo(orderBy))(using any[HeaderCarrier]()))
        .thenReturn(Future.failed(exception))

      val service = new GamblingService(mockConnector)
      val result = service.getSubmittedReturns(regNumber, sortBy, orderBy).failed.futureValue

      result mustEqual exception
    }
  }
}
