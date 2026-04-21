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

package repositories

import models.{MgdCertificate, MgdCertificateDAO}
import org.mongodb.scala.model.Filters
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues.*
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

class MgdCertificateCacheRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[MgdCertificateDAO]
    with ScalaFutures
    with IntegrationPatience {

  private val instant =
    Instant.parse("2026-04-21T10:15:30Z")

  private val stubClock: Clock =
    Clock.fixed(instant, ZoneId.of("UTC"))

  override val repository: MgdCertificateCacheRepository =
    new MgdCertificateCacheRepository(
      mongoComponent = mongoComponent,
      clock          = stubClock
    )

  private val certificate =
    MgdCertificate(
      mgdRegNumber         = "MGD12345",
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

  ".cacheCertificate" - {

    "must store certificate in Mongo with timestamp" in {

      repository.cacheCertificate(certificate).futureValue mustBe true

      val result =
        find(Filters.equal("_id", "MGD12345")).futureValue.headOption.value

      result.certificate mustBe certificate
      result.id mustBe "MGD12345"
      result.lastUpdated mustBe instant
    }
  }

  ".getCertificate" - {

    "must return certificate when present" in {

      repository.cacheCertificate(certificate).futureValue

      val result =
        repository.getCertificate("MGD12345").futureValue

      result mustBe Some(certificate)
    }

    "must return None when not found" in {

      val result =
        repository.getCertificate("NOT_EXIST").futureValue

      result mustBe None
    }
  }
}
