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
| sourceAccountNo        | M       | String(19)                           | Source Account Number |
| beneficiaryAccountNo   | M       | String(19)                           | Beneficiary Account |
| amount                 | M       | Object : {String(16,2), String(3)}   | Credit Amount, Sample format{"value":"10000.00","currency":"IDR"} |
| transactionDate        | M       | String(25)                           | Today Date, format: `'yyyy-MM-dd'T'HH:mm:ssTZD'`. |
| partnerReferenceNo     | M       | String(19)                           | Transaction identifier on service consumer system |
| beneficiaryBankCode    | M       | String(11)                           | Beneficiary Bank Code/BIC Code (11 digit SWIFT or 3 digit Bank Code) |
| beneficiaryAccountName | M       | String(100)                          | Beneficiary Account Name |
| beneficiaryBankName    | O       | String(50)                           | Beneficiary Bank Name |
| beneficiaryEmail       | O       | String(50)                           | Beneficiary Email |
| beneficiaryAddress     | O       | String(100)                          | Beneficiary Address |
| feeType                | O       | String(3)                            | to whom the fee will be charged, value : OUR/BEN, default OUR |
#### additionalInfo (Object)
| Field Name              | Attrib. | Type                                  | Comment |
|-------------------------|---------|---------------------------------------|---------|
| switcher                | O       | String(4)                            | Default PRIMA, other ATMB & BIFAST |
| reportCode              | O       | String(6)                            | Value will be given during onboarding process |
| senderInstrument        | O       | String(8)                            | Origin source of fund instrument, can be filled with 2 value : 1. rekening, 2. tunai |
| senderAccountNo         | O       | String(32)                           | Account number from origin source of fund. e.g - origin sender bank account, - origin sender e-wallet account (phone number). Can be left as blank for tunai origin instrument |
| senderCountry           | O       | String(2)                            | Country of origin source of fund e.g ID following ISO 3166-1 |
| senderName              | O       | String(70)                           | Name of origin sender |
| senderCustomerType      | O       | String(10)                           | Type of origin sender, can be filled with 2 value : 1. perorangan (individual), 2. korporasi (corporate) |
| beneficiaryInstrument   | O       | String(1)                            | Beneficiary address instrument, can be filled with 2, value : 1. rekening, 2. tunai |
| beneficiaryCustomerType | O       | String(1)                            | Type of beneficiary, can be filled with 2 value : 1. perorangan (individual), 2. korporasi (corporate) |
| categoryPurpose         | C       | String(22)                           | Category Purpose of transaction, Transaction purpose must be 99=Others (for various purposes). Mandatory if switcher = BIFAST |
| paymentDescription      | C       | String(xx)                           | Payment Description For switcher BI Fast |
| nationalIdentityNumber  | C       | String                               | Debtor's National Identity Number. Fill "-" if you don't want to send it. Mandatory if switcher = BIFAST |
| transactionIndicator    | C       | String(1)                            | Transaction Indicator: - 1=Transaction with account, - 2=Transaction with proxy/alias (Phone Number/Email) Mandatory if switcher = BIFAST |
| aliasResolution         | C       | String(4)                            | Alias Resolution Lookup. Type of Look Up: PXRS=Proxy Resolution. Value: PXRS. Mandatory if switcher = BIFAST & transactionIndicator = 2 |
| aliasType               | C       | String                               | Proxy Type to resolve: 01=Mobile Phone number, 02=Email Address. Mandatory if switcher = BIFAST & transactionIndicator = 2 |
| aliasValue              | C       | String                               | Beneficiary Alias Value / Proxy. Phone number or email address. Mandatory if switcher = BIFAST & transactionIndicator = 2 |

---

## Output Parameter

### Body
| Field Name             | Attrib. | Type                                      | Comment                                                 |
|------------------------|---------|-------------------------------------------|---------------------------------------------------------|
| responseCode          | M       | String(7)                                | Response code.                                          |
| responseMessage       | M       | String(150)                              | Response description.                                   |
| sourceAccountNo       | M       | String(13)                               | Debit Account. Registered Partner Account Number.       |
| beneficiaryAccountNo  | M       | String(13)                               | Credit Account. Destination Account Number.             |
| amount                | M       | Object: {String(16,2), String(3)}        | Credit Amount, Sample format: {"value":"10000.00", "currency":"IDR"} |
| referenceNo           | C       | String(20)                               | Transaction identifier on service provider system. Must be filled upon successful transaction. |
| partnerReferenceNo    | M       | String(19)                               | From partner request.                                   |
#### additionalInfo (Object)
| remmittanceNumber     | M       | String                                   | If switcher = BIFAST                                    |

---

## Sample Request, switcher: PRIMA & ATMB
```json
{
  "sourceAccountNo": "1150006399259",
  "beneficiaryAccountNo": "1390094033549",
  "beneficiaryAccountName": "Monica Ester",
  "beneficiaryBankCode": "014 ",
  "beneficiaryBankName": "Bank BCA ",
  "amount": {
    "value": "50000",
    "currency": "IDR"
  },
  "transactionDate": "2021-02-02T18:00:01+07:00",
  "partnerReferenceNo": "ABC30032001",
  "beneficiaryEmail": "die.moelyadie@gmail.com",
  "additionalInfo": {
    "switcher": "ATMB"
  }
}
```
## Sample Request, switcher: BIFAST
```json
{
  "sourceAccountNo": "60004400184",
  "beneficiaryAccountNo": "4701500458775",
  "beneficiaryAccountName": "MUHAMMAD ANWAR",
  "beneficiaryBankCode": "BNIAIDJA",
  "beneficiaryBankName": "Bank BCA",
  "transactionDate": "2024-04-25T14:26:16+07:00",
  "partnerReferenceNo": "83522251342245260",
  "beneficiaryEmail": "",
  "beneficiaryAddress": "Majalengka",
  "feeType": "OUR",
  "amount": {
    "value": "20000",
    "currency": "IDR"
  },
  "additionalInfo": {
    "switcher": "BIFAST",
    "categoryPurpose": "99",
    "paymentDescription": "TEST KDK PRX",
    "nationalIdentityNumber": "1323234",
    "transactionIndicator": "2",
    "aliasResolution": "PXRS",
    "aliasType": "01",
    "aliasValue": "628111391849"
  }
}
```

## Sample Response (on normal condition), switcher: PRIMA & ATMB 
```json
{
  "responseCode": "2001800",
  "responseMessage": "Success",
  "sourceAccountNo": "1150006399259",
  "beneficiaryAccountNo": "1390094033549",
  "amount": {
    "value": "50000",
    "currency": "IDR"
  },
  "referenceNo": "16667899991666293912312",
  "partnerReferenceNo": "ABC30032001"
}
```
## Sample Response (on normal condition), switcher: BIFAST 
```json
{
  "responseCode": "2001800",
  "responseMessage": "Success",
  "sourceAccountNo": "60004400184",
  "beneficiaryAccountNo": "628111391849",
  "referenceNo": "20240425105251558",
  "partnerReferenceNo": "83522251342245260",
  "amount": {
    "value": "20000.00",
    "currency": "IDR"
  },
  "additionalInfo": {
    "remmittanceNumber": "20240425BMRIIDJA110O9940324469"
  }
}
```

## Response Message Table

| HTTP Code | Response Code | Response Message | Description |
|-----------|--------------|------------------|-------------|
| 200       | 2001800      | Success | Success |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - SIGNATURE not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - PARTNER-ID not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - EXTERNAL-ID not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field X - TIMESTAMP not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field sourceAccountNo not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field beneficiaryAccountNo not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field beneficiaryAccountName not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field beneficiaryBankCode not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field beneficiaryBankName not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field amountValue not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field amountCurrency not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field transactionDate not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field partnerReferenceNo not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field categoryPurpose not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field transactionIndicator not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field aliasResolution not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field aliasType not exist in request |
| 400       | 4001802      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field aliasValue not exist in request |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-EXTERNAL-ID format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-TIMESTAMP format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid sourceAccountNo format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid beneficiaryAccountNo format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid beneficiaryBankCode format |
| 400       | 4001801      | Invalid Field Format {field name} |Transaction cannot be processed because invalid currency format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid partnerReferenceNo format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid beneficiaryEmail format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid categoryPurpose format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid transactionIndicator format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid aliasResolution format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid aliasType format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid aliasValue format |
| 400       | 4001801      | Invalid Field Format {field name} | Transaction cannot be processed because invalid amountCurrency format |
| 401       | 4011800      | Unauthorized. {reason} | General unauthorized, for example due to invalid X-PARTNER-ID |
| 401       | 4011800      | Unauthorized. {reason} | Transaction cannot be processed because signature value is invalid |
| 401       | 4011801      | Invalid Token (B2B) | Invalid token |
| 403       | 4031802      | Exceeds Transaction Amount Limit | The transaction was rejected because the transaction amount exceeds the limit |
| 403       | 4031820      | Merchant Limit Exceed | The transaction was rejected because the daily amount exceeds the limit for account |
| 403       | 4031814      | Insufficient Funds | Insufficient fund or balance less then minimum balance |
| 403       | 4031815      | Transaction Not Permitted. {reason} | Transaction cannot be processed because date value is back date |
| 403       | 4031815      | Transaction Not Permitted. {reason} | Transaction cannot be processed because Transaction not permitted. Transaction Amount Less Than Minimum Limit or More Than Maximum Limit |
| 403       | 4031815      | Transaction Not Permitted. {reason} | Transaction cannot be processed because date value is future date |
| 403       | 4031815      | Transaction Not Permitted. {reason} | BI Fast transaction type has not been registered in Bank Mandiri |
| 403       | 4031815      | Transaction Not Permitted. {reason} | Debit account must be IDR |
| 403       | 4031815      | Transaction Not Permitted. {reason} | Beneficiary name is not same with inquiry |
| 403       | 4031815      | Transaction Not Permitted. {reason} | Transaction reject by destination bank |

| 404       | 4041811      | Invalid Account | Account not found or not registered in Bank Mandiri system or Destination Bank system |
| 409       | 4091800      | Conflict | Cannot use same X-EXTERNAL-ID in same day |
| 409       | 4091801      | Duplicate partnerReferenceNo | partnerReferenceNo being used has been recorded as succeed for previous request. Cannot use the same partnerReferenceNo if the transaction is successful within 7 days |
| 429       | 4291800      | Too many requests | Maximum transaction per minutes limit exceeded |
| 500       | 5001800      | General Error | Error in Bank Mandiri system. If you get this error or timeout, please do Transaction Status Inquiry to check the transaction status. |
| 500       | 5001801      | Internal Server Error | Error in Bank Mandiri system. If you get this error or timeout, please do Transaction Status Inquiry to check the transaction status. |
| 500       | 5001802      | External Server Error | Error in External system. If you get this error or timeout, please do Transaction Status Inquiry to check the transaction status. |
| 504       | 5041800      | Timeout | Error in Bank Mandiri system. If you get this error or timeout, please do Transaction Status Inquiry to check the transaction status. |






