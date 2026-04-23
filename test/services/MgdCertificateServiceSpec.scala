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
import models.MgdCertificate
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Request}
import repositories.MgdCertificateCacheRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class MgdCertificateServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: Request[AnyContent] = mock[Request[AnyContent]]

  private val connector = mock[GamblingConnector]
  private val repo = mock[MgdCertificateCacheRepository]

  private val service = new MgdCertificateService(connector, repo)

  private val mgdRegNumber = "MGD12345"

  private val certificate =
    MgdCertificate(
      mgdRegNumber         = mgdRegNumber,
      registrationDate     = None,
      individualName       = None,
      businessName         = None,
      tradingName          = None,
      repMemName           = None,
      busAddrLine1         = None,
      busAddrLine2         = None,
      busAddrLine3         = None,
      busAddrLine4         = None,
      busPostcode          = None,
      busCountry           = None,
      busAdi               = None,
      repMemLine1          = None,
      repMemLine2          = None,
      repMemLine3          = None,
      repMemLine4          = None,
      repMemPostcode       = None,
      repMemAdi            = None,
      typeOfBusiness       = None,
      businessTradeClass   = None,
      noOfPartners         = None,
      groupReg             = "N",
      noOfGroupMems        = None,
      dateCertIssued       = None,
      partMembers          = Seq.empty,
      groupMembers         = Seq.empty,
      returnPeriodEndDates = Seq.empty
    )

  "MgdCertificateService#retrieveCertificate" should {

    "return cached certificate when present" in {

      when(repo.getCertificate(mgdRegNumber))
        .thenReturn(Future.successful(Some(certificate)))

      val result = service.retrieveCertificate(mgdRegNumber).futureValue

      result mustBe certificate

      verifyNoInteractions(connector)
      verify(repo, never()).cacheCertificate(certificate)
    }

    "fetch from connector and cache when missing" in {

      when(repo.getCertificate(mgdRegNumber))
        .thenReturn(Future.successful(None))

      when(connector.getCertificate(mgdRegNumber)(hc))
        .thenReturn(Future.successful(certificate))

      when(repo.cacheCertificate(certificate))
        .thenReturn(Future.successful(true))

      val result = service.retrieveCertificate(mgdRegNumber).futureValue

      result mustBe certificate

      verify(connector).getCertificate(mgdRegNumber)(hc)
      verify(repo).cacheCertificate(certificate)
    }

    "fail when connector fails and do not cache" in {

      when(repo.getCertificate(mgdRegNumber))
        .thenReturn(Future.successful(None))

      doReturn(Future.failed(new RuntimeException("backend failure")))
        .when(connector)
        .getCertificate(mgdRegNumber)(hc)

      val ex = service.retrieveCertificate(mgdRegNumber).failed.futureValue

      ex.getMessage mustBe "backend failure"
    }
  }
}
