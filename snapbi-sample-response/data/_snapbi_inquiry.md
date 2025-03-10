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
| accountNo | M       | String (13) | Numeric string. Registered partner account number. |

---

## Output Parameter

### Body
| Field Name       | Attrib. | Type          | Comment |
|-----------------|---------|--------------|---------|
| responseCode    | M       | String (7)   | Response Code |
| responseMessage | M       | String (150) | Response description |
| accountNo       | M       | String (32)  | Numeric String. Registered Partner account number. |
| name           | M       | String (140) | Account name |
| accountInfos   |         | Array        | List of account information |

#### accountInfos Fields
| Field Name       | Attrib. | Type        | Comment |
|-----------------|---------|------------|---------|
| amount         | M       | Object     | Hold Amount + Available Balance |
| ├── value      |         | String(16,2) | Example: 10000.00 |
| ├── currency   |         | String (3) | Example: IDR |
| holdAmount     | C       | Object     | Hold amount that cannot be used. |
| ├── value      |         | String(16,2) | Example: 10000.00 |
| ├── currency   |         | String (3) | Example: IDR |
| availableBalance | M       | Object     | Account balance that can be used for financial transactions |
| ├── value      |         | String(16,2) | Example: 10000.00 |
| ├── currency   |         | String (3) | Example: IDR |
| ledgerBalance  | O       | Object     | Account balance at the beginning of each day |
| ├── value      |         | String(16,2) | Example: 10000.00 |
| ├── currency   |         | String (3) | Example: IDR |

---

## Sample Request
```json
{
 "accountNo": "1150010883959"
}
```
## Sample Response (on normal condition)
```json
{
	"responseCode": "2001100",
	"responseMessage": "Success",
	"accountNo": "12977881200001",
	"name": "Monica Ester",
	"accountInfos": [{
		"amount": {
			"value": "2000000",
			"currency": "IDR"
		},
		"holdAmount": {
			"value": "1000000",
			"currency": "IDR"
		},
		"availableBalance": {
			"value": "1000000",
			"currency": "IDR"
		},
		"ledgerBalance": {
			"value": "2000000",
			"currency": "IDR"
		}
	}]
}
```

## Response Message Table

| HTTP Code | Response Code | Response Message | Description |
|-----------|--------------|------------------|-------------|
| 200       | 2001100      | Success | Success |
| 400       | 4001102      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field or value X-TIMESTAMP not exist in request |
| 400       | 4001102      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field or value X-SIGNATURE not exist in request |
| 400       | 4001102      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field or value X-PARTNER-ID not exist in request |
| 400       | 4001102      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field or value X-EXTERNAL-ID not exist in request |
| 400       | 4001102      | Invalid Mandatory Field {field name} | Transaction cannot be processed because field or value accountNo not exist in request |
| 400       | 4001101      | Invalid Field Format {field name} | Transaction cannot be processed because field accountNo value is zero or minus |
| 400       | 4001101      | Invalid Field Format {field name} | Transaction cannot be processed because invalid accountNo format |
| 400       | 4001101      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-EXTERNAL-ID format |
| 400       | 4001101      | Invalid Field Format {field name} | Transaction cannot be processed because invalid X-TIMESTAMP format |
| 400       | 4001101      | Invalid Field Format {field name} | Value of accountNo have special character. Invalid accountNo format |
| 401       | 4011101      | Invalid Token (B2B) | Invalid token |
| 401       | 4011100      | Unauthorized. {reason} | General unauthorized, for example due to invalid X-PARTNER-ID |
| 401       | 4011100      | Unauthorized. {reason} | Transaction cannot be processed because signature value is invalid |
| 404       | 4041111      | Invalid Account | Account not found or not registered in Bank Mandiri system |
| 403       | 4031118      | Inactive Account | Account found in Bank Mandiri system but inactive |
| 409       | 4091100      | Conflict | Cannot use same X-EXTERNAL-ID in same day |
| 429       | 4291100      | Too many requests | Maximum transaction per minutes limit exceeded |
| 500       | 5001101      | Internal Server Error | Error in Bank Mandiri system |
| 504       | 5041100      | Timeout | Error in Bank Mandiri system |
