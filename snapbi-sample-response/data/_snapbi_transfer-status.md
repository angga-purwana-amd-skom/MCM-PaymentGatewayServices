# API Documentation

## Input Parameter

### HTTP Header
| Field Name       | Attrib. | Type        | Comment |
|-----------------|---------|------------|---------|
| Authorization   | M       | String     | Bearer {Access Token} |
| X-TIMESTAMP    | M       | String     | Transaction timestamp FORMAT: 'yyyy-MM-dd'T'HH:mm:ssTZD' |
| X-SIGNATURE    | M       | String     | Transaction signature. |
| X-PARTNER-ID   | M       | String(16) | Partner ID given by Mandiri |
| X-EXTERNAL-ID  | M       | String(19) | Numeric String. Reference number that should be unique for any request in the same day. |
| CHANNEL-ID     | O       | String(4)  | Channel is mandatory in SNAP, however Mandiri will not validate this value |

### Body
| Field Name                 | Attrib. | Type                                  | Comment |
|----------------------------|---------|---------------------------------------|---------|
| originalExternalID         | M       | String(19)                            | Numeric String. Original External ID |
| originalPartnerReferenceNo | M       | String(19)                            | Original Customer Reference Number |
| serviceCode                | M       | String(2)                             | 17 à transfer intrabank, 18 à transfer interbank, 22 à transfer RTGS, 23 à transfer SKN, 33 à transfer to VA |


---

## Output Parameter

### Body
| Field Name             | Attrib. | Type                                      | Comment                                                 |
|------------------------|---------|-------------------------------------------|---------------------------------------------------------|
| responseCode           | M       | String(7)                                | Response code.                                          |
| responseMessage        | M       | String(150)                              | Response description.                                   |
| serviceCode            | M       | String(2)                                | 17 - transfer intrabank, 18 - transfer interbank, 22 - transfer RTGS, 23 - transfer SKN, 33 - transfer to VA       |
| referenceNumber        | M       | String(20)                               | Reference number generate by system                     |
| previouseResponseCode  | M       | String(7)                                | Response Code from the transaction being inquire        |
| latestTransactionStatus | M       | String(2)                               | 00 : Success, 03 : Pending. |
| sourceAccountNo        | M       | String(19)                               | Source Account Number.                                  |
| BeneficiaryAccountNo   | M       | String(34)                               | Beneficiary Account.                                    |
| OriginalPartnerReferenceNo   | M       | String(19)                         | Original Partner Reference No from transaction          |
| OriginalExternalId     | M       | String(19)                               | Original External Id from transaction                   |
| Amount                 | M       | Object {String(16,2),String(3)}          | Credit Amount, Sample format : {"value":"10000.00","currency":"IDR"} |
| OriginalReferenceNo    | C       | String(64)                               | Transaction identifier on service provider system. Must be filled upon successful transaction |
| transactionDate        | M       | String(25)                               | Today Date. FORMAT: 'yyyy-MM-dd'T'HH:mm:ssTZD' |

---

## Sample Request
```json
{
  "originalExternalId": "20200501115310311",
  "originalPartnerReferenceNo ": "9920200501115310311",
  "serviceCode ": "17"
}
```

## Sample Response (on normal condition)
```json
{
  "responseCode": "2003600",
  "responseMessage": "Success",
  "serviceCode": "17",
  "referenceNumber": "20210202118000131",
  "transactionDate": "2021-02-02T18:00:01+07:00",
  "previousResponseCode": "2000000",
  "latestTransactionStatus": "00",
  "sourceAccountNo": "1150006399259",
  "BeneficiaryAccountNo": "123123878652",
  "amount": {
    "value": "50000",
    "currency": "IDR"
  },
  "OriginalReferenceNo": "9912499570502",
  "OriginalPartnerReferenceNo ": "9920200501115310311",
  "OriginalExternalId": "123123"
}
```


## Response Message Table

| HTTP Code | Response Code | Response Message | Description |
|-----------|--------------|------------------|-------------|
| 200       | 2001800      | Success | Success |
| 400       | 4003602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - SIGNATURE not exist in request |
| 400       | 4003602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - PARTNER-ID not exist in request |
| 400       | 4003602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - EXTERNAL-ID not exist in request |
| 400       | 4003602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - TIMESTAMP not exist in request |
| 400       | 4003602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field originalExternalID not exist in request |
| 400       | 4003602      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field originalPartnerReferenceNo not exist in request |
| 400       | 4003601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-EXTERNAL-ID format |
| 400       | 4003601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-TIMESTAMP format |
| 400       | 4003601      | Invalid Field Format {field name} | Transaction cannot be processed because invalid originalExternalID format |
| 401       | 4013601      | Invalid Token (B2B) | Invalid token |
| 401       | 4013600      | Unauthorized. {reason} | General unauthorized, for example due to invalid X-PARTNER-ID |
| 401       | 4013600      | Unauthorized. {reason} | Transaction cannot be processed because signature value is invalid |
| 404       | 4043601      | Transaction not found  | The transaction is not found |
| 409       | 4093600      | Conflict | Cannot use same X-EXTERNAL-ID in same day |
| 429       | 4293600      | Too many requests | Maximum transaction per minutes limit exceeded |
| 500       | 5003600      | General Error | Error in Bank Mandiri system |
| 504       | 5043600      | Timeout | Error in Bank Mandiri system |




