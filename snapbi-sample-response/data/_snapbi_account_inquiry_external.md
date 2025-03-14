# API Documentation

## Input Parameter

### HTTP Header
| Field Name       | Attrib. | Type        | Comment |
|-----------------|---------|------------|---------|
| Authorization   | M       | String     | Bearer {Access Token} |
| X-TIMESTAMP    | M       | String     | Transaction timestamp FORMAT: 'yyyy-MM-dd'T'HH:mm:ssTZD' |
| X-SIGNATURE    | M       | String     | Transaction signature. |
| X-PARTNER-ID   | M       | String(16) | Unique ID for a partner |
| X-EXTERNAL-ID  | M       | String(19) | Numeric String. Reference number that should be unique for any request in the same day. |
| CHANNEL-ID     | O       | String(4)  | Channel is mandatory in SNAP, however Mandiri will not validate this value |

### Body
| Field Name | Attrib. | Type         | Comment |
|-----------|---------|-------------|---------|
| beneficiaryBankCode | M       | String (8) | Beneficiary Bank Code (SWIFT Code/Bank Code) |
| beneficiaryAccountNo | M       | String (34) | Beneficiary Account Number |

### additionalInfo
| Field Name      | Attrib. | Type       | Comment |
|----------------|---------|-----------|---------|
| switcher       | O       | String(4)  | Default value: PRIMA. Other values: - ATMB - BIFAST |
| inquiryType    | C       | String(1)  | Mandatory if `switcher = BIFAST`. Account Inquiry Indicator values: 1 = Inquiry with account, 2 = Inquiry with proxy/alias |
| categoryPurpose | C       | String(2)  | Mandatory if `switcher = BIFAST`. Transaction purpose must be `99 = Others` (for various purposes) |
| debtorAccount  | C       | String(xx) | Mandatory if `switcher = BIFAST` & `inquiryType = 2` |
| lookUpType     | C       | String(4)  | Mandatory if `switcher = BIFAST` & `inquiryType = 2`. Value: `PRXY` |
| aliasType      | C       | String(2)  | Mandatory if `switcher = BIFAST` & `inquiryType = 2`. Proxy Type values: `01 = Mobile Phone number`, `02 = Email Address`, `03 = IPT ID` |
| aliasValue     | C       | String     | Mandatory if `switcher = BIFAST` & `inquiryType = 2`. Beneficiary Alias Value / Proxy (Phone Number or Email Address) |

---

## Output Parameter

### Body
| Field Name                | Attrib. | Type          | Comment |
|---------------------------|---------|--------------|---------|
| responseCode             | M       | String(7)    | Response code. “2000000” for Success. Moreover, you can check the responseMessage field |
| responseMessage          | M       | String(150)  | Response description. |
| beneficiaryAccountName   | M       | String(50)  |  Destination Account Name |
| beneficiaryAccountNo     | M       | String(34)   | Numeric String. Destination Account Number. |
| beneficiaryBankName      | M       | String(50)    | Beneficiary Bank Name |

### additionalInfo
| Field Name              | Attrib. | Type              | Comment |
|-------------------------|---------|------------------|---------|
| **accountInquiryInfo**  | C       | Object           |   |
| **originatorFinInstnId** | C       | String(8)        | Must be ‘FASTIDJA’: BIC (first 8 characters) of the system if switcher = BIFAST |
| **recipientFinInstnId**  | C       | String(8)        | Must be ‘BMRIIDJA’: BIC (first 8 characters) of the system if switcher = BIFAST |
| **accountInquiryInfoList** | C   | Array of Object  |   |
| **accountInfo**         | C       | Object           |   |
| **aliasIdentifer**      | C       | String           | Proxy Registration ID if switcher = BIFAST & inquiryType = 2 |
| **aliasDisplayName**    | C       | String           | Proxy Display Name if switcher = BIFAST & inquiryType = 2 |
| **aliasFinInstnId**     | C       | String           | ID (BIC) of the Bank in the proxy look-up results if switcher = BIFAST & inquiryType = 2 |
| **accountNbr**         | C       | String           | Creditor’s Account Number if switcher = BIFAST |
| **accountType**        | C       | String           | Creditor’s Account ID Type if switcher = BIFAST |
| **accountOwnerName**   | C       | String           | Creditor’s Name if switcher = BIFAST |
| **customerInfo**       | C       | Object           |   |
| **type**              | C       | String           |   |
| **identifier**        | C       | String           | Account Creditor's National Identity Number (for Mandiri’s account only) if switcher = BIFAST |
| **residentStatus**    | C       | String           | Creditor's Resident Status: Resident or Non-Resident - given as codes (e.g., 01 = Resident, 02 = Non-Resident) if switcher = BIFAST |
| **cityName**         | C       | String           | Creditor's Town Name - given as a city code (e.g., 0300 = Jakarta) if switcher = BIFAST |
| **responseStatus**    | C       | String           | Transaction Status - indicates whether it was accepted or rejected (ACTC or RJCT) if switcher = BIFAST |
| **responseReason**    | C       | String           | Reason code (e.g., U000 for accepted, U002 for Accepted [Deemed-accepted / stand-in]) if switcher = BIFAST |
| **responseIDs**      | C       | Object           |   |
| **trackingID**       | C       | String           | Business message identifier. Unique identifier for the message in the scheme. <br>**Format:** `YYYYMMDDBBBBBBBBTTTOCCSSSSSSSS` <br>**Where:** <br>- `YYYYMMDD` = Date (8 digits) <br>- `BBBBBBBB` = BIC Code (8 digits) of message creator <br>- `TTT` = Transaction Type (3 digits) <br>- `O` = Originator (1 alphanumeric) <br>- `CC` = Channel (2 alphanumeric) <br>- `SSSSSSSS` = Serial Number (8 digits) <br>**Note:** <br>- Originator can be one of: `O = OFI`, `H = CIHub`, `R = RFI` <br>- Channel Type: `01 = Internet Banking`, `02 = Mobile Banking`, `03 = Over the Counter`, `99 = Other` <br>If switcher = BIFAST |
| **correlationID**     | C       | String           | OFI's Unique Internal Transaction Reference Number. <br>**Format:** `YYYYMMDDBBBBBBBBTTTSSSSSSSS` <br>**Where:** <br>- `YYYYMMDD` = Date (8 digits) <br>- `BBBBBBBB` = First 8 characters of BIC code <br>- `TTT` = Transaction Type (3 digits) <br>- `SSSSSSSS` = Sequence Number (8 digits) <br>If switcher = BIFAST |


---

## Sample Request, switcher: PRIMA & ATMB
```json
{
  "beneficiaryAccountNo": "1150010884130",
  "beneficiaryBankCode": "SYBTIDJ1",
  "additionalInfo": {
    "switcher": "ATMB"
  }
}
```
## Sample Request, switcher: BIFAST
```json
{
  "beneficiaryBankCode": "BNIAIDJA",
  "beneficiaryAccountNo": "111111",
  "additionalInfo": {
    "switcher": "BIFAST",
    "inquiryType": "2",
    "categoryPurpose": "01",
    "debtorAccount": "385938593",
    "lookUpType": "PXRS",
    "aliasType": "01",
    "aliasValue": "628111391849"
  }
}
```
## Sample Response (on normal condition), switcher: PRIMA & ATMB
```json
{
  "responseCode": "2001600",
  "responseMessage": "Success",
  "beneficiaryAccountNo": "1150010884130",
  "destinationBankName": "PT. BTN (Persero) SYARIAH",
  "beneficiaryAccountName": "Monica Ester"
}
```
## Sample Response (on normal condition), switcher: BIFAST
```json
{
  "responseCode": "2001600",
  "responseMessage": "Success",
  "beneficiaryAccountNo": "1000144412",
  "beneficiaryAccountName": "AZWIN ANGGARA",
  "beneficiaryBankName": "PT. BNI 1946 (Persero) Tbk.",
  "additionalInfo": {
    "accountInquiryInfo": {
      "originatorFinInstnId": "FASTIDJA",
      "recipientFinInstnId": "BMRIIDJA",
      "accountInquiryInfoList": [
        {
          "accountInfo": {
            "aliasIdentifer": null,
            "aliasDisplayName": null,
            "aliasFinInstnId": null,
            "accountNbr": "1000144412",
            "accountType": "SVGS",
            "accountOwnerName": "AZWIN ANGGARA"
          },
          "customerInfo": {
            "type": "01",
            "identifier": "3211226812990005",
            "residentStatus": "01",
            "cityName": null
          }
        }
      ],
      "responseStatus": "ACTC",
      "responseReason": "BFSTU000",
      "responseIDs": {
        "trackingID": "20240419FASTIDJA510H9927418060",
        "correlationID": "20240419FASTIDJA51027418061"
      }
    }
  }
}
```

## Response Message Table

| HTTP Code | Response Code | Response Message | Description |
|-----------|--------------|------------------|-------------|
| 200       | 2001600      | Success | Success |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X-TIMESTAMP not exist in request |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X-SIGNATURE not exist in request |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X-PARTNER-ID not exist in request |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X-EXTERNAL-ID not exist in request |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field beneficiaryBankCode not exist in request |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field beneficiaryAccountNo not exist in request |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field inquiryType not exist in request with switcher=BIFAST |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field categoryPurpose not exist in request with switcher=BIFAST |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field debtorAccount not exist in request with switcher=BIFAST and inquiryType=2 |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field lookUpType not exist in request with switcher=BIFAST and inquiryType=2 |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field aliasType not exist in request with switcher=BIFAST and inquiryType=2 |
| 400       | 4001602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field aliasValue not exist in request with switcher=BIFAST and inquiryType=2 |
| 400       | 4001601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-EXTERNAL-ID format |
| 400       | 4001601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-EXTERNAL-ID format |
| 400       | 4001601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-TIMESTAMP format |
| 400       | 4001601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid beneficiaryBankCode format or have special character |
| 400       | 4001601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid beneficiaryAccountNo format |
| 400       | 4001601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid categoryPurpose format |
| 400       | 4001601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid inquiryType format |
| 400       | 4001601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid debtorAccount format |
| 400       | 4001601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid lookUpType format |
| 400       | 4001601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid aliasType format |
| 400       | 4001601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid aliasValue format |
| 401       | 4011601      | Invalid Token (B2B) | Invalid token |
| 401       | 4011600      | Unauthorized. {reason} | General unauthorized, for example due to invalid X-PARTNER-ID |
| 401       | 4011600      | Unauthorized. {reason} | Transaction cannot be processed because signature value is invalid |
| 403       | 4031615      | Transaction Not Permitted. {reason} | BI Fast transaction type has not been registered in Bank Mandiri |
| 404       | 4041611      | Invalid Account | Account not found or not registered in Bank Mandiri system or Destination Bank system |
| 409       | 4091600      | Conflict | Cannot use same X-EXTERNAL-ID in same day |
| 429       | 4291600      | Too many requests | Maximum transaction per minutes limit exceeded |
| 500       | 5001601      | Internal Server Error | Error in Bank Mandiri system |
| 500       | 5001602      | Internal Server Error | Destination or switcher error |
| 504       | 5041600      | Timeout | Error in Bank Mandiri system |