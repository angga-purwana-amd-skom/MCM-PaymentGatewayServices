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
| beneficiaryAccountNo | M       | String (34) | Beneficiary Account Number |

---

## Output Parameter

### Body

| Field Name                | Attrib. | Type          | Comment |
|---------------------------|---------|--------------|---------|
| responseCode             | M       | String(7)    | Response code. |
| responseMessage          | M       | String(150)  | Response description. |
| beneficiaryAccountName   | M       | String(100)  | Beneficiary account name. |
| beneficiaryAccountNo     | O       | String(13)   | Beneficiary account number. |
| beneficiaryAccountType   | O       | String(1)    | Beneficiary account type: `"D"` for Current Account, `"S"` for Saving Account. |
| currency                | O       | String(3)    | Currency code. Example: `IDR`, `USD`, `GBP`. |
| referenceNo             | O       | String(20)   | Reference number. |

---

## Sample Request
```json
{
 "beneficiaryAccountNo": "1297700000801"
}
```
## Sample Response (on normal condition)
```json
{
  "responseCode": "2001500",
  "responseMessage": "Success",
  "beneficiaryAccountName": "Monica Ester",
  "beneficiaryAccountNo": "1297700000801",
  "beneficiaryAccountType": "S",
  "referenceNo": "20230106110051922",
  "currency": "IDR"
}

```

## Response Message Table

| HTTP Code | Response Code | Response Message | Description |
|-----------|--------------|------------------|-------------|
| 200       | 2001500      | Success | Success |
| 400       | 4001502      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field or value X-TIMESTAMP not exist in request |
| 400       | 4001502      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field or value X-SIGNATURE not exist in request |
| 400       | 4001502      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field or value X-PARTNER-ID not exist in request |
| 400       | 4001502      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field or value X-EXTERNAL-ID not exist in request |
| 400       | 4001502      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field or value accountNo not exist in request |
| 400       | 4001501      | Invalid Field Format {field name} | Transaction cannot be processed because field accountNo value is zero or minus |
| 400       | 4001501      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-EXTERNAL-ID format |
| 400       | 4001501      | Invalid Field Format {field name} | Transaction cannot be processed because invalid accountNo format |
| 400       | 4001501      | Invalid Field Format {field name} | Value of accountNo have special character. Invalid accountNo format |
| 400       | 4001501      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-EXTERNAL-ID format |
| 400       | 4001501      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-TIMESTAMP format |
| 401       | 4011501      | Invalid Token (B2B) | Invalid token |
| 401       | 4011500      | Unauthorized. {reason} | General unauthorized, for example due to invalid X-PARTNER-ID |
| 401       | 4011500      | Unauthorized. {reason} | Transaction cannot be processed because signature value is invalid |
| 404       | 4041511      | Invalid Account | Account not found or not registered in Bank Mandiri system |
| 403       | 4031518      | Inactive Account | Transaction cannot be processed because account is not active |
| 409       | 4091500      | Conflict | Cannot use same X-EXTERNAL-ID in same day |
| 429       | 4291500      | Too many requests | Maximum transaction per minutes limit exceeded |
| 500       | 5001501      | Internal Server Error | Error in Bank Mandiri system |
| 504       | 5041500      | Timeout | Error in Bank Mandiri system |

