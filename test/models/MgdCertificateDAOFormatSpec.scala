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

package models

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.Instant

class MgdCertificateDAOFormatSpec extends AnyWordSpec with Matchers {

  "MgdCertificateDAO JSON format" should {

    "serialize and deserialize correctly" in {

      val now = Instant.parse("2026-04-21T10:15:30Z")

      val certificate = MgdCertificate(
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

      val dao = MgdCertificateDAO(
        id          = "MGD12345",
        lastUpdated = now,
        certificate = certificate
      )

      val json = Json.toJson(dao)
      val result = json.as[MgdCertificateDAO]

      result mustBe dao
    }

    "write JSON with expected fields" in {

      val now = Instant.parse("2026-04-21T10:15:30Z")

      val certificate = MgdCertificate(
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

      val dao = MgdCertificateDAO(
        id          = "MGD12345",
        lastUpdated = now,
        certificate = certificate
      )

      val json = Json.toJson(dao)

      val result = json.as[MgdCertificateDAO]

      result mustBe dao
    }
  }
}
