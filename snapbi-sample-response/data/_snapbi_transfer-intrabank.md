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
| Field Name             | Attrib. | Type                                  | Comment |
|------------------------|---------|---------------------------------------|---------|
| partnerReferenceNo    | M       | String(19)                           | Customer Reference Number. For a successful transaction, the same `partnerReferenceNo` cannot be used within 7 days. |
| amount               | M       | Object: {String(16,2), String(3)}    | Credit Amount, sample format: `{"value":"10000.00","currency":"IDR"}` |
| beneficiaryAccountNo  | M       | String(13)                           | Credit Account. Destination Account Number. |
| sourceAccountNo      | M       | String(13)                           | Debit Account. |
| feeType             | O       | String(3)                            | Specifies who will be charged the fee. Possible values: `OUR`/`BEN`, default is `OUR`. |
| remark              | M       | String(40)                           | Remark, displayed on MT940 reports and bank statement service. |
| transactionDate     | M       | String(25)                           | Transaction date in format: `'yyyy-MM-dd'T'HH:mm:ssTZD'`. |
| beneficiaryEmail    | O       | String(100)                          | Email address for sending notifications to the beneficiary. |
#### additionalInfo (object)
| Field Name                 | Attrib. | Type        | Comment |
|----------------------------|---------|------------|---------|
| reportCode                | C       | String(6)  | Mandatory for transactions from or to outside Indonesia. Value will be provided during onboarding. |
| senderInstrument         | O       | String(1)  | Origin source of fund instrument. Possible values: `1` (rekening), `2` (tunai). |
| senderAccountNo          | C       | String(32) | Mandatory for transactions from or to outside Indonesia. Account number of the origin source of funds. Example: <br> - Origin sender bank account <br> - Origin sender e-wallet account (phone number) <br> - Origin customer identification number <br> Can be left blank for `tunai` origin instrument. |
| senderCountry           | C       | String(2)  | Mandatory for transactions from or to outside Indonesia. Country of origin source of funds, e.g., `ID` (ISO 3166-1 standard). |
| senderName             | C       | String(70) | Mandatory for transactions from or to outside Indonesia. Name of the origin sender. |
| senderCustomerType     | O       | String(1)  | Type of origin sender. Possible values: `1` (perorangan/individual), `2` (korporasi/corporate). |
| beneficiaryAccountName | O       | String(100) | For reporting purposes. The transaction won’t be rejected if it does not exactly match the Bank’s information. |
| beneficiaryInstrument  | O       | String(8)  | Beneficiary address instrument. Possible values: `1` (Rekening/bank account number), `2` (Tunai). |
| beneficiaryCustomerType | O       | String(1)  | Type of beneficiary. Possible values: `1` (perorangan/individual), `2` (korporasi/corporate), `3` (Lembaga pemerintahan/government). |

---

## Output Parameter

### Body
| Field Name             | Attrib. | Type                                      | Comment                                                 |
|------------------------|---------|-------------------------------------------|---------------------------------------------------------|
| responseCode          | M       | String(7)                                | Response code.                                          |
| responseMessage       | M       | String(150)                              | Response description.                                   |
| sourceAccountNo       | M       | String(13)                               | Debit Account. Registered Partner Account Number.       |
| beneficiaryAccountNo  | M       | String(13)                               | Credit Account. Destination Account Number.            |
| amount               | M       | Object: {String(16,2), String(3)}        | Credit Amount, Sample format: {"value":"10000.00", "currency":"IDR"} |
| referenceNo          | C       | String(20)                               | Transaction identifier on service provider system. Must be filled upon successful transaction. |
| partnerReferenceNo   | M       | String(19)                               | From partner request.                                  |
| transactionDate      | M       | String(25)                               | Today's date, FORMAT: `'yyyy-MM-dd'T'HH:mm:ssTZD'`.   |


---

## Sample Request
```json
{
  "partnerReferenceNo": "814808435829070",
  "amount": {
    "value": "10000.00",
    "currency": "IDR"
  },
  "sourceAccountNo": "60004400184",
  "beneficiaryAccountNo": "1150097011284",
  "remark": "814808435829070",
  "transactionDate": "2022-03-23T14:57:34+07:00",
  "beneficiaryEmail": "rendi.matrido1995@gmail.com",
  "additionalInfo": {
    "reportCode": "",
    "senderInstrument": "",
    "senderAccountNo": "",
    "senderCountry": "",
    "senderCostumerType": "",
    "beneficiaryAccountName": "",
    "beneficiaryInstrument": "",
    "beneficiaryCustomerType": ""
  }
}
```
## Sample Response (on normal condition)
```json
{
  "responseCode": "2001700",
  "responseMessage": "Success",
  "sourceAccountNo": "60004400184",
  "beneficiaryAccountNo": "1150097011284",
  "amount": {
    "value": "10000.00",
    "currency": "IDR"
  },
  "referenceNo": "20220323150251228",
  "partnerReferenceNo": "814808435829070",
  "transactionDate": "2022-03-23T14:57:34+07:00"
}
```

## Response Message Table

| HTTP Code | Response Code | Response Message | Description |
|-----------|--------------|------------------|-------------|
| 200       | 2001700      | Success | Success |
| 400       | 4001702      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - SIGNATURE not exist in request |
| 400       | 4001702      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - PARTNER-ID not exist in request |
| 400       | 4001702      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - EXTERNAL-ID not exist in request |
| 400       | 4001702      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - TIMESTAMP not exist in request |
| 400       | 4001702      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field debitAccountNo not exist in request |
| 400       | 4001702      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field creditAccountNo not exist in request |
| 400       | 4001702      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field creditAmount not exist in request |
| 400       | 4001702      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field creditCurrency not exist in request |
| 400       | 4001702      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field valueDate not exist in request |
| 400       | 4001702      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field partnerReferenceNo not exist in request |
| 400       | 4001701      | Invalid Field Format {field name} | Currency code is not register in data source |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because field debitAccountNo value is zero or minus |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because field creditAccountNo value is zero or minus |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because field creditAmount value is zero or minus |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-EXTERNAL-ID format |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because invalid creditAccountNo format |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because invalid creditAmount format |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because invalid debitAccountNo format |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-EXTERNAL-ID format |
| 400       | 4001701      | Invalid Field Format {field name} |Transaction cannot be processed because invalid X-TIMESTAMP format |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because invalid debitAccountNo format |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because invalid creditAccountNo format |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because invalid partnerReferenceNo format |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because invalid valueDate format |
| 400       | 4001701      | Invalid Field Format {field name} | Transaction cannot be processed because invalid beneficiaryEmailAddress format |
| 401       | 4011701      | Invalid Token (B2B) | Invalid token |
| 401       | 4011700      | Unauthorized. {reason} | General unauthorized, for example due to invalid X-PARTNER-ID |
| 401       | 4011700      | Unauthorized. {reason} | Transaction cannot be processed because signature value is invalid |
| 403       | 4031715      | Transaction Not Permitted. {reason}. {reason} |Transaction rejected |
| 403       | 4031702      | Exceeds Transaction Amount Limit | The transaction was rejected because the transaction amount exceeds the limit |
| 403       | 4031720      | Merchant Limit Exceed | The transaction was rejected because the daily amount exceeds the limit for account |
| 403       | 4031714      | Insufficient Funds | Insufficient fund or balance less then minimum balance |
| 403       | 4031715      | Transaction Not Permitted. {reason} | Transaction cannot be processed because date value is back date |
| 403       | 4031715      | Transaction Not Permitted. {reason} | Transaction cannot be processed because date value is future date |
| 404       | 4041711      | Invalid Account | Account not found or not registered in Bank Mandiri system |
| 403       | 4031718      | Inactive Account | Account found in Bank Mandiri system but inactive |
| 403       | 4031715      | Transaction Not Permitted. {reason} | Credit account currency must be the same with credit currency |
| 409       | 4091700      | Conflict | Cannot use same X-EXTERNAL-ID in same day |
| 409       | 4091701      | Duplicate partnerReferenceNo | partnerReferenceNo being used has been recorded as succeed for previous request. Cannot use the same partnerReferenceNo if the transaction is successful within 7 days |
| 429       | 4291700      | Too many requests | Maximum transaction per minutes limit exceeded |
| 500       | 5001700      | General Error | Error in Bank Mandiri system. If you get this error or timeout, please do Transaction Status Inquiry to check the transaction status. |
| 500       | 5001701      | Internal Server Error | Error in Bank Mandiri system. If you get this error or timeout, please do Transaction Status Inquiry to check the transaction status. |
| 504       | 5041700      | Timeout | Error in Bank Mandiri system. If you get this error or timeout, please do Transaction Status Inquiry to check the transaction status. |
