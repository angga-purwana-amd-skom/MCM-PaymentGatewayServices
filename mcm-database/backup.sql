--
-- PostgreSQL database cluster dump
--

SET default_transaction_read_only = off;

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

--
-- Roles
--

CREATE ROLE user123;
ALTER ROLE user123 WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN REPLICATION BYPASSRLS PASSWORD 'md5235caa89166a4e95a1ba87fbc2493545';






\connect template1

--
-- PostgreSQL database dump
--

-- Dumped from database version 11.16 (Debian 11.16-1.pgdg90+1)
-- Dumped by pg_dump version 11.16 (Debian 11.16-1.pgdg90+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 11.16 (Debian 11.16-1.pgdg90+1)
-- Dumped by pg_dump version 11.16 (Debian 11.16-1.pgdg90+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: mcm_paygate_db; Type: DATABASE; Schema: -; Owner: user123
--

CREATE DATABASE mcm_paygate_db WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.utf8' LC_CTYPE = 'en_US.utf8';


ALTER DATABASE mcm_paygate_db OWNER TO user123;

\connect mcm_paygate_db

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: api_credentials; Type: TABLE; Schema: public; Owner: user123
--

CREATE TABLE public.api_credentials (
    id integer NOT NULL,
    client_key character varying(255) NOT NULL,
    private_key text NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    api_name character varying(100) NOT NULL
);


ALTER TABLE public.api_credentials OWNER TO user123;

--
-- Name: api_credentials_id_seq; Type: SEQUENCE; Schema: public; Owner: user123
--

CREATE SEQUENCE public.api_credentials_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.api_credentials_id_seq OWNER TO user123;

--
-- Name: api_credentials_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: user123
--

ALTER SEQUENCE public.api_credentials_id_seq OWNED BY public.api_credentials.id;


--
-- Name: api_endpoints; Type: TABLE; Schema: public; Owner: user123
--

CREATE TABLE public.api_endpoints (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    host character varying(255) NOT NULL,
    port integer,
    path character varying(255) NOT NULL,
    method character varying(10) NOT NULL,
    headers jsonb,
    request_body_template jsonb,
    auth_required boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    CONSTRAINT api_endpoints_method_check CHECK (((method)::text = ANY ((ARRAY['GET'::character varying, 'POST'::character varying, 'PUT'::character varying, 'DELETE'::character varying, 'PATCH'::character varying])::text[])))
);


ALTER TABLE public.api_endpoints OWNER TO user123;

--
-- Name: api_endpoints_id_seq; Type: SEQUENCE; Schema: public; Owner: user123
--

CREATE SEQUENCE public.api_endpoints_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.api_endpoints_id_seq OWNER TO user123;

--
-- Name: api_endpoints_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: user123
--

ALTER SEQUENCE public.api_endpoints_id_seq OWNED BY public.api_endpoints.id;


--
-- Name: api_event_logs; Type: TABLE; Schema: public; Owner: user123
--

CREATE TABLE public.api_event_logs (
    id integer NOT NULL,
    endpoint_id integer NOT NULL,
    request_timestamp timestamp without time zone DEFAULT now(),
    request_method character varying(10) NOT NULL,
    request_headers jsonb,
    request_body jsonb,
    response_timestamp timestamp without time zone,
    response_status_code integer,
    response_headers jsonb,
    response_body jsonb,
    error_message text,
    CONSTRAINT api_event_logs_request_method_check CHECK (((request_method)::text = ANY ((ARRAY['GET'::character varying, 'POST'::character varying, 'PUT'::character varying, 'DELETE'::character varying, 'PATCH'::character varying])::text[])))
);


ALTER TABLE public.api_event_logs OWNER TO user123;

--
-- Name: api_event_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: user123
--

CREATE SEQUENCE public.api_event_logs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.api_event_logs_id_seq OWNER TO user123;

--
-- Name: api_event_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: user123
--

ALTER SEQUENCE public.api_event_logs_id_seq OWNED BY public.api_event_logs.id;


--
-- Name: api_tokens; Type: TABLE; Schema: public; Owner: user123
--

CREATE TABLE public.api_tokens (
    id integer NOT NULL,
    endpoint_id integer NOT NULL,
    access_token text NOT NULL,
    token_type character varying(50) NOT NULL,
    expires_in integer NOT NULL,
    issued_at timestamp without time zone DEFAULT now(),
    expires_at timestamp without time zone NOT NULL
);


ALTER TABLE public.api_tokens OWNER TO user123;

--
-- Name: api_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: user123
--

CREATE SEQUENCE public.api_tokens_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.api_tokens_id_seq OWNER TO user123;

--
-- Name: api_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: user123
--

ALTER SEQUENCE public.api_tokens_id_seq OWNED BY public.api_tokens.id;


--
-- Name: mcm_clients; Type: TABLE; Schema: public; Owner: user123
--

CREATE TABLE public.mcm_clients (
    mcm_client_id character varying NOT NULL,
    mcm_client_secret character varying NOT NULL
);


ALTER TABLE public.mcm_clients OWNER TO user123;

--
-- Name: snapbi_sample_responses; Type: TABLE; Schema: public; Owner: user123
--

CREATE TABLE public.snapbi_sample_responses (
    path_url text NOT NULL,
    sample_response text NOT NULL,
    http_code character(5)
);


ALTER TABLE public.snapbi_sample_responses OWNER TO user123;

--
-- Name: api_credentials id; Type: DEFAULT; Schema: public; Owner: user123
--

ALTER TABLE ONLY public.api_credentials ALTER COLUMN id SET DEFAULT nextval('public.api_credentials_id_seq'::regclass);


--
-- Name: api_endpoints id; Type: DEFAULT; Schema: public; Owner: user123
--

ALTER TABLE ONLY public.api_endpoints ALTER COLUMN id SET DEFAULT nextval('public.api_endpoints_id_seq'::regclass);


--
-- Name: api_event_logs id; Type: DEFAULT; Schema: public; Owner: user123
--

ALTER TABLE ONLY public.api_event_logs ALTER COLUMN id SET DEFAULT nextval('public.api_event_logs_id_seq'::regclass);


--
-- Name: api_tokens id; Type: DEFAULT; Schema: public; Owner: user123
--

ALTER TABLE ONLY public.api_tokens ALTER COLUMN id SET DEFAULT nextval('public.api_tokens_id_seq'::regclass);


--
-- Data for Name: api_credentials; Type: TABLE DATA; Schema: public; Owner: user123
--

COPY public.api_credentials (id, client_key, private_key, created_at, updated_at, api_name) FROM stdin;
1	sample_client_key_0001	sample_private_key_0001	2025-02-24 09:00:28.104117	2025-02-24 09:00:28.104117	SnapBI
\.


--
-- Data for Name: api_endpoints; Type: TABLE DATA; Schema: public; Owner: user123
--

COPY public.api_endpoints (id, name, host, port, path, method, headers, request_body_template, auth_required, created_at, updated_at) FROM stdin;
1	SnapBI Get Token	snapbi-sample-response	8087	/openapi/auth/v2.0/access-token/b2b	POST	{"Content-Type": "application/json"}	{"grantType": "client_credentials"}	t	2025-02-24 03:42:57.550288	2025-02-24 03:42:57.550288
2	SnapBI Get Balance Inquiry	snapbi-sample-response	8087	/openapi/auth/v2.0/balance-inquiry	POST	{"CHANNEL-ID": "YOUR_CHANNEL_ID", "X-SIGNATURE": "signature_data", "X-TIMESTAMP": "timestamp_data", "Content-Type": "application/json", "X-PARTNER-ID": "YOUR_PARTNER_ID", "Authorization": "Bearer {access_token}", "X-EXTERNAL-ID": "YOUR_EXTERNAL_ID"}	{"accountNo": "1150010883959"}	t	2025-03-05 07:09:09.389048	2025-03-05 07:09:09.389048
3	SnapBI Get Balance Inquiry Internal	snapbi-sample-response	8087	/openapi/auth/v2.0/account-inquiry-internal	POST	{"CHANNEL-ID": "YOUR_CHANNEL_ID", "X-SIGNATURE": "signature_data", "X-TIMESTAMP": "timestamp_data", "Content-Type": "application/json", "X-PARTNER-ID": "YOUR_PARTNER_ID", "Authorization": "Bearer {access_token}", "X-EXTERNAL-ID": "YOUR_EXTERNAL_ID"}	{"beneficiaryAccountNo": "1297700000801"}	t	2025-03-12 03:19:55.485041	2025-03-12 03:19:55.485041
4	SnapBI Get Balance Inquiry External	snapbi-sample-response	8087	/openapi/auth/v2.0/account-inquiry-external	POST	{"CHANNEL-ID": "YOUR_CHANNEL_ID", "X-SIGNATURE": "signature_data", "X-TIMESTAMP": "timestamp_data", "Content-Type": "application/json", "X-PARTNER-ID": "YOUR_PARTNER_ID", "Authorization": "Bearer {access_token}", "X-EXTERNAL-ID": "YOUR_EXTERNAL_ID"}	{"additionalInfo": {"switcher": "BIFAST", "aliasType": "01", "aliasValue": "628111391849", "lookUpType": "PXRS", "inquiryType": "2", "debtorAccount": "385938593", "categoryPurpose": "01"}, "beneficiaryBankCode": "BNIAIDJA", "beneficiaryAccountNo": "111111"}	t	2025-03-12 03:19:55.485041	2025-03-12 03:19:55.485041
5	SnapBI Transfer Intrabank	snapbi-sample-response	8087	/openapi/auth/v2.0/transfer-intrabank	POST	{"CHANNEL-ID": "YOUR_CHANNEL_ID", "X-SIGNATURE": "signature_data", "X-TIMESTAMP": "timestamp_data", "Content-Type": "application/json", "X-PARTNER-ID": "YOUR_PARTNER_ID", "Authorization": "Bearer {access_token}", "X-EXTERNAL-ID": "YOUR_EXTERNAL_ID"}	{"amount": {"value": "10000.00", "currency": "IDR"}, "remark": "814808435829070", "additionalInfo": {"reportCode": "", "senderCountry": "", "senderAccountNo": "", "senderInstrument": "", "senderCostumerType": "", "beneficiaryInstrument": "", "beneficiaryAccountName": "", "beneficiaryCustomerType": ""}, "sourceAccountNo": "60004400184", "transactionDate": "2022-03-23T14:57:34+07:00", "beneficiaryEmail": "rendi.matrido1995@gmail.com", "partnerReferenceNo": "814808435829070", "beneficiaryAccountNo": "1150097011284"}	t	2025-03-12 03:19:55.485041	2025-03-12 03:19:55.485041
6	SnapBI Transfer Interbank	snapbi-sample-response	8087	/openapi/auth/v2.0/transfer-interbank	POST	{"CHANNEL-ID": "YOUR_CHANNEL_ID", "X-SIGNATURE": "signature_data", "X-TIMESTAMP": "timestamp_data", "Content-Type": "application/json", "X-PARTNER-ID": "YOUR_PARTNER_ID", "Authorization": "Bearer {access_token}", "X-EXTERNAL-ID": "YOUR_EXTERNAL_ID"}	{"amount": {"value": "20000", "currency": "IDR"}, "feeType": "OUR", "additionalInfo": {"switcher": "BIFAST", "aliasType": "01", "aliasValue": "628111391849", "aliasResolution": "PXRS", "categoryPurpose": "99", "paymentDescription": "TEST KDK PRX", "transactionIndicator": "2", "nationalIdentityNumber": "1323234"}, "sourceAccountNo": "60004400184", "transactionDate": "2024-04-25T14:26:16+07:00", "beneficiaryEmail": "", "beneficiaryAddress": "Majalengka", "partnerReferenceNo": "83522251342245260", "beneficiaryBankCode": "BNIAIDJA", "beneficiaryBankName": "Bank BCA", "beneficiaryAccountNo": "4701500458775", "beneficiaryAccountName": "MUHAMMAD ANWAR"}	t	2025-03-12 03:19:55.485041	2025-03-12 03:19:55.485041
7	SnapBI Transfer Status	snapbi-sample-response	8087	/openapi/auth/v2.0/transfer/status	POST	{"CHANNEL-ID": "YOUR_CHANNEL_ID", "X-SIGNATURE": "signature_data", "X-TIMESTAMP": "timestamp_data", "Content-Type": "application/json", "X-PARTNER-ID": "YOUR_PARTNER_ID", "Authorization": "Bearer {access_token}", "X-EXTERNAL-ID": "YOUR_EXTERNAL_ID"}	{"serviceCode ": "17", "originalExternalId": "20200501115310311", "originalPartnerReferenceNo ": "9920200501115310311"}	t	2025-03-12 03:19:55.485041	2025-03-12 03:19:55.485041
\.


--
-- Data for Name: api_event_logs; Type: TABLE DATA; Schema: public; Owner: user123
--

COPY public.api_event_logs (id, endpoint_id, request_timestamp, request_method, request_headers, request_body, response_timestamp, response_status_code, response_headers, response_body, error_message) FROM stdin;
15	4	2025-03-13 04:28:04.334268	POST	{"CHANNEL-ID": "999999999999", "X-SIGNATURE": "xdoLZ2QAv3loDjy1xCND/jD13b5ZOrzy5Lm2tPgxaPYk1agsljTz0WA4SSyvc8RxlKpsxxPu1yV9T3HPJqyljw==", "X-TIMESTAMP": "2025-03-13T04:28:04.120609Z", "Content-Type": "application/json", "X-PARTNER-ID": "000000000000", "Authorization": "Bearer eyJraWQiOiJzc29zIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJiOTU3YWE0NC04ZWZmLTQ0NjYtOWEyMy0xNDg5MzgyOWExZjAiLCJhdWQiOiJqd3QtYXVkIiwiY2xpZW50SWQiOiI5YjZkYjRmMy1iMzYyLTQ5YzEtOTZhZC1jNjMyNTQxNjI5ZTIiLCJpc3MiOiJqd3QtaXNzdWVyIiwicGFydG5lcklkIjoiVUFUQ09SUEFZIiwiZXhwIjoxNTk1MTY3MTQ5LCJpYXQiOjE1ODkxNjcxNDl9.ZzacwbT08Ax7fMQDRwtaFxDz2dpn3cwz58tMI3QonysPXpr0bHRT6W8ZtI_Q0oTjm17A7NsHqwyadbpGPIq38Lw9h5G7kNGmpVzWKAYZZPQlTwuC7zxHdXYwBQXIs7qYD_wIiPQvYIqPwt6p390oBpUiZk7IsVQ_8ucmXbuBaa9-7uWuvdRL72AQr2cFn89Jjaz6fsQy92dapLAQo9iU7k3wgRk3bxQPQjIbbC-osg-OBzQ1Tyx-NuPaqg5Z0y4ccrVfR2Nv9-jt4wEYFdy4EFgLdj7XKmx8MexfGdtrt9Vn4mi5scCiNOGzEoSKqb27SwdAtdL-caOH7czCPwg", "X-EXTERNAL-ID": "202503130084137692"}	{"beneficiaryAccountNo": "1297700000801"}	2025-03-13 04:28:04.334273	500	{"Date": "Thu, 13 Mar 2025 04:28:03 GMT", "Server": "uvicorn", "Content-Type": "application/json", "Content-Length": "747"}	{"responseCode": "2001600", "additionalInfo": {"accountInquiryInfo": {"responseIDs": {"trackingID": "20240419FASTIDJA510H9927418060", "correlationID": "20240419FASTIDJA51027418061"}, "responseReason": "BFSTU000", "responseStatus": "ACTC", "recipientFinInstnId": "BMRIIDJA", "originatorFinInstnId": "FASTIDJA", "accountInquiryInfoList": [{"accountInfo": {"accountNbr": "1000144412", "accountType": "SVGS", "aliasIdentifer": null, "aliasFinInstnId": null, "accountOwnerName": "AZWIN ANGGARA", "aliasDisplayName": null}, "customerInfo": {"type": "01", "cityName": null, "identifier": "3211226812990005", "residentStatus": "01"}}]}}, "responseMessage": "Success", "beneficiaryBankName": "PT. BNI 1946 (Persero) Tbk.", "beneficiaryAccountNo": "1000144412", "beneficiaryAccountName": "AZWIN ANGGARA"}	\N
16	5	2025-03-17 06:18:39.422885	POST	{"CHANNEL-ID": "999999999999", "X-SIGNATURE": "UV7FfTVFhSkv1XAMxYWa3TSGKROZ6/0/UKVeClYdjsFNNRVyA0IjP+qAYuUUYF1qSaUACzjW317E9C/nXYw9qw==", "X-TIMESTAMP": "2025-03-17T06:18:39.244070Z", "Content-Type": "application/json", "X-PARTNER-ID": "000000000000", "Authorization": "Bearer eyJraWQiOiJzc29zIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJiOTU3YWE0NC04ZWZmLTQ0NjYtOWEyMy0xNDg5MzgyOWExZjAiLCJhdWQiOiJqd3QtYXVkIiwiY2xpZW50SWQiOiI5YjZkYjRmMy1iMzYyLTQ5YzEtOTZhZC1jNjMyNTQxNjI5ZTIiLCJpc3MiOiJqd3QtaXNzdWVyIiwicGFydG5lcklkIjoiVUFUQ09SUEFZIiwiZXhwIjoxNTk1MTY3MTQ5LCJpYXQiOjE1ODkxNjcxNDl9.ZzacwbT08Ax7fMQDRwtaFxDz2dpn3cwz58tMI3QonysPXpr0bHRT6W8ZtI_Q0oTjm17A7NsHqwyadbpGPIq38Lw9h5G7kNGmpVzWKAYZZPQlTwuC7zxHdXYwBQXIs7qYD_wIiPQvYIqPwt6p390oBpUiZk7IsVQ_8ucmXbuBaa9-7uWuvdRL72AQr2cFn89Jjaz6fsQy92dapLAQo9iU7k3wgRk3bxQPQjIbbC-osg-OBzQ1Tyx-NuPaqg5Z0y4ccrVfR2Nv9-jt4wEYFdy4EFgLdj7XKmx8MexfGdtrt9Vn4mi5scCiNOGzEoSKqb27SwdAtdL-caOH7czCPwg", "X-EXTERNAL-ID": "202503172319251860"}	{"amount": {"value": "10000.00", "currency": "IDR"}, "remark": "814808435829070", "additionalInfo": {"reportCode": "", "senderCountry": "", "senderAccountNo": "", "senderInstrument": "", "senderCostumerType": "", "beneficiaryInstrument": "", "beneficiaryAccountName": "", "beneficiaryCustomerType": ""}, "sourceAccountNo": "60004400184", "transactionDate": "2022-03-23T14:57:34+07:00", "beneficiaryEmail": "rendi.matrido1995@gmail.com", "partnerReferenceNo": "814808435829070", "beneficiaryAccountNo": "1150097011284"}	2025-03-17 06:18:39.422892	500	{"Date": "Mon, 17 Mar 2025 06:18:39 GMT", "Server": "uvicorn", "Content-Type": "application/json", "Content-Length": "291"}	{"amount": {"value": "10000.00", "currency": "IDR"}, "referenceNo": "20220323150251228", "responseCode": "2001700", "responseMessage": "Success", "sourceAccountNo": "60004400184", "transactionDate": "2022-03-23T14:57:34+07:00", "partnerReferenceNo": "814808435829070", "beneficiaryAccountNo": "1150097011284"}	\N
17	6	2025-03-18 06:20:56.040277	POST	{"CHANNEL-ID": "999999999999", "X-SIGNATURE": "A0TjZhSJSoC4/bOSrudv94A8HZ5Vgt4964j3Xxk873CvYbg+nyo1j+k7OvRzDXzM7yB5MkM8e+fnqoB1No+nKQ==", "X-TIMESTAMP": "2025-03-18T06:20:55.861513Z", "Content-Type": "application/json", "X-PARTNER-ID": "000000000000", "Authorization": "Bearer eyJraWQiOiJzc29zIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJiOTU3YWE0NC04ZWZmLTQ0NjYtOWEyMy0xNDg5MzgyOWExZjAiLCJhdWQiOiJqd3QtYXVkIiwiY2xpZW50SWQiOiI5YjZkYjRmMy1iMzYyLTQ5YzEtOTZhZC1jNjMyNTQxNjI5ZTIiLCJpc3MiOiJqd3QtaXNzdWVyIiwicGFydG5lcklkIjoiVUFUQ09SUEFZIiwiZXhwIjoxNTk1MTY3MTQ5LCJpYXQiOjE1ODkxNjcxNDl9.ZzacwbT08Ax7fMQDRwtaFxDz2dpn3cwz58tMI3QonysPXpr0bHRT6W8ZtI_Q0oTjm17A7NsHqwyadbpGPIq38Lw9h5G7kNGmpVzWKAYZZPQlTwuC7zxHdXYwBQXIs7qYD_wIiPQvYIqPwt6p390oBpUiZk7IsVQ_8ucmXbuBaa9-7uWuvdRL72AQr2cFn89Jjaz6fsQy92dapLAQo9iU7k3wgRk3bxQPQjIbbC-osg-OBzQ1Tyx-NuPaqg5Z0y4ccrVfR2Nv9-jt4wEYFdy4EFgLdj7XKmx8MexfGdtrt9Vn4mi5scCiNOGzEoSKqb27SwdAtdL-caOH7czCPwg", "X-EXTERNAL-ID": "202503188855868672"}	{"amount": {"value": "10000.00", "currency": "IDR"}, "remark": "814808435829070", "additionalInfo": {"reportCode": "", "senderCountry": "", "senderAccountNo": "", "senderInstrument": "", "senderCostumerType": "", "beneficiaryInstrument": "", "beneficiaryAccountName": "", "beneficiaryCustomerType": ""}, "sourceAccountNo": "60004400184", "transactionDate": "2022-03-23T14:57:34+07:00", "beneficiaryEmail": "rendi.matrido1995@gmail.com", "partnerReferenceNo": "814808435829070", "beneficiaryAccountNo": "1150097011284"}	2025-03-18 06:20:56.040286	500	{"Date": "Tue, 18 Mar 2025 06:20:55 GMT", "Server": "uvicorn", "Content-Type": "application/json", "Content-Length": "318"}	{"amount": {"value": "20000.00", "currency": "IDR"}, "referenceNo": "20240425105251558", "responseCode": "2001800", "additionalInfo": {"remmittanceNumber": "20240425BMRIIDJA110O9940324469"}, "responseMessage": "Success", "sourceAccountNo": "60004400184", "partnerReferenceNo": "83522251342245260", "beneficiaryAccountNo": "628111391849"}	\N
18	7	2025-03-18 09:11:44.863218	POST	{"CHANNEL-ID": "999999999999", "X-SIGNATURE": "rI2XvcW2IyYx1tKGnKDJ7a9syEbMYgx+1wxu0SPHsYnXRqxLaLmylo3A8suUhrNfDzVAXw4kLN40BjVYpx3GFg==", "X-TIMESTAMP": "2025-03-18T09:11:44.667357Z", "Content-Type": "application/json", "X-PARTNER-ID": "000000000000", "Authorization": "Bearer eyJraWQiOiJzc29zIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJiOTU3YWE0NC04ZWZmLTQ0NjYtOWEyMy0xNDg5MzgyOWExZjAiLCJhdWQiOiJqd3QtYXVkIiwiY2xpZW50SWQiOiI5YjZkYjRmMy1iMzYyLTQ5YzEtOTZhZC1jNjMyNTQxNjI5ZTIiLCJpc3MiOiJqd3QtaXNzdWVyIiwicGFydG5lcklkIjoiVUFUQ09SUEFZIiwiZXhwIjoxNTk1MTY3MTQ5LCJpYXQiOjE1ODkxNjcxNDl9.ZzacwbT08Ax7fMQDRwtaFxDz2dpn3cwz58tMI3QonysPXpr0bHRT6W8ZtI_Q0oTjm17A7NsHqwyadbpGPIq38Lw9h5G7kNGmpVzWKAYZZPQlTwuC7zxHdXYwBQXIs7qYD_wIiPQvYIqPwt6p390oBpUiZk7IsVQ_8ucmXbuBaa9-7uWuvdRL72AQr2cFn89Jjaz6fsQy92dapLAQo9iU7k3wgRk3bxQPQjIbbC-osg-OBzQ1Tyx-NuPaqg5Z0y4ccrVfR2Nv9-jt4wEYFdy4EFgLdj7XKmx8MexfGdtrt9Vn4mi5scCiNOGzEoSKqb27SwdAtdL-caOH7czCPwg", "X-EXTERNAL-ID": "202503189104684498"}	{"serviceCode ": "17", "originalExternalId": "20200501115310311", "originalPartnerReferenceNo ": "9920200501115310311"}	2025-03-18 09:11:44.863222	500	{"Date": "Tue, 18 Mar 2025 09:11:43 GMT", "Server": "uvicorn", "Content-Type": "application/json", "Content-Length": "457"}	{"amount": {"value": "50000", "currency": "IDR"}, "serviceCode": "17", "responseCode": "2003600", "referenceNumber": "20210202118000131", "responseMessage": "Success", "sourceAccountNo": "1150006399259", "transactionDate": "2021-02-02T18:00:01+07:00", "OriginalExternalId": "123123", "OriginalReferenceNo": "9912499570502", "BeneficiaryAccountNo": "123123878652", "previousResponseCode": "2000000", "latestTransactionStatus": "00", "OriginalPartnerReferenceNo ": "9920200501115310311"}	\N
\.


--
-- Data for Name: api_tokens; Type: TABLE DATA; Schema: public; Owner: user123
--

COPY public.api_tokens (id, endpoint_id, access_token, token_type, expires_in, issued_at, expires_at) FROM stdin;
3	1	eyJraWQiOiJzc29zIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJiOTU3YWE0NC04ZWZmLTQ0NjYtOWEyMy0xNDg5MzgyOWExZjAiLCJhdWQiOiJqd3QtYXVkIiwiY2xpZW50SWQiOiI5YjZkYjRmMy1iMzYyLTQ5YzEtOTZhZC1jNjMyNTQxNjI5ZTIiLCJpc3MiOiJqd3QtaXNzdWVyIiwicGFydG5lcklkIjoiVUFUQ09SUEFZIiwiZXhwIjoxNTk1MTY3MTQ5LCJpYXQiOjE1ODkxNjcxNDl9.ZzacwbT08Ax7fMQDRwtaFxDz2dpn3cwz58tMI3QonysPXpr0bHRT6W8ZtI_Q0oTjm17A7NsHqwyadbpGPIq38Lw9h5G7kNGmpVzWKAYZZPQlTwuC7zxHdXYwBQXIs7qYD_wIiPQvYIqPwt6p390oBpUiZk7IsVQ_8ucmXbuBaa9-7uWuvdRL72AQr2cFn89Jjaz6fsQy92dapLAQo9iU7k3wgRk3bxQPQjIbbC-osg-OBzQ1Tyx-NuPaqg5Z0y4ccrVfR2Nv9-jt4wEYFdy4EFgLdj7XKmx8MexfGdtrt9Vn4mi5scCiNOGzEoSKqb27SwdAtdL-caOH7czCPwg	Bearer	900	2025-02-25 08:52:15.61659	2025-03-10 02:43:17.681125
\.


--
-- Data for Name: mcm_clients; Type: TABLE DATA; Schema: public; Owner: user123
--

COPY public.mcm_clients (mcm_client_id, mcm_client_secret) FROM stdin;
21da9959-72fa-4f12-9a8d-a9edc9d1c62f	$2b$12$hct0gED0MRza99U2qgmYN.DV1vGRgpC26mXRmHmOEJ3TXrUZbexo2
5f323f4b-2b8a-414b-9b12-0ba98ff5c034	$2b$12$95sMGwV2hwXG.k/Wm2DEs.FAA/gbYwGSIHh1KlIVfQ9rjpLy.5YEG
\.


--
-- Data for Name: snapbi_sample_responses; Type: TABLE DATA; Schema: public; Owner: user123
--

COPY public.snapbi_sample_responses (path_url, sample_response, http_code) FROM stdin;
hello	{\r\n"hello":"is hi",\r\n"hi":"is hello"\r\n}	\N
/openapi/auth/v2.0/transfer-interbank	{\r\n  "responseCode": "2001800",\r\n  "responseMessage": "Success",\r\n  "sourceAccountNo": "60004400184",\r\n  "beneficiaryAccountNo": "628111391849",\r\n  "referenceNo": "20240425105251558",\r\n  "partnerReferenceNo": "83522251342245260",\r\n  "amount": {\r\n    "value": "20000.00",\r\n    "currency": "IDR"\r\n  },\r\n  "additionalInfo": {\r\n    "remmittanceNumber": "20240425BMRIIDJA110O9940324469"\r\n  }\r\n}	200  
/openapi/auth/v2.0/transfer/status	{\r\n  "responseCode": "2003600",\r\n  "responseMessage": "Success",\r\n  "serviceCode": "17",\r\n  "referenceNumber": "20210202118000131",\r\n  "transactionDate": "2021-02-02T18:00:01+07:00",\r\n  "previousResponseCode": "2000000",\r\n  "latestTransactionStatus": "00",\r\n  "sourceAccountNo": "1150006399259",\r\n  "BeneficiaryAccountNo": "123123878652",\r\n  "amount": {\r\n    "value": "50000",\r\n    "currency": "IDR"\r\n  },\r\n  "OriginalReferenceNo": "9912499570502",\r\n  "OriginalPartnerReferenceNo ": "9920200501115310311",\r\n  "OriginalExternalId": "123123"\r\n}	200  
/openapi/auth/v2.0/access-token/b2b	{\r\n"responseCode":"2007300",\r\n"responseMessage":"Success",\r\n"accessToken":\r\n"eyJraWQiOiJzc29zIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJiOTU3YWE0NC04ZWZmLTQ0NjYtOWEyMy0xNDg5MzgyOWExZj\r\nAiLCJhdWQiOiJqd3QtYXVkIiwiY2xpZW50SWQiOiI5YjZkYjRmMy1iMzYyLTQ5YzEtOTZhZC1jNjMyNTQxNjI5ZTIiLCJpc3MiO\r\niJqd3QtaXNzdWVyIiwicGFydG5lcklkIjoiVUFUQ09SUEFZIiwiZXhwIjoxNTk1MTY3MTQ5LCJpYXQiOjE1ODkxNjcxNDl9.Zzac\r\nwbT08Ax7fMQDRwtaFxDz2dpn3cwz58tMI3QonysPXpr0bHRT6W8ZtI_Q0oTjm17A7NsHqwyadbpGPIq38Lw9h5G7kNGmpVzWKAYZZPQlTwuC7zxHdXYwBQXIs7qYD_\r\nwIiPQvYIqPwt6p390oBpUiZk7IsVQ_8ucmXbuBaa9-7uWuvdRL72AQr2cFn89Jjaz6fsQy92dapLAQo9iU7k3wgRk3bxQPQjIbbC-osg-OBzQ1Tyx-NuPaqg5Z0y4ccrVfR2Nv9-jt4wEYFdy4EFgLdj7XKmx8MexfGdtrt9Vn4mi5scCiNOGzEoSKqb27SwdAtdL-caOH7czCPwg",\r\n "tokenType": "Bearer",\r\n "expiresIn": "900"\r\n}	200  
/openapi/auth/v2.0/balance-inquiry	{\r\n\t"responseCode": "2001100",\r\n\t"responseMessage": "Success",\r\n\t"accountNo": "12977881200001",\r\n\t"name": "Monica Ester",\r\n\t"accountInfos": [{\r\n\t\t"amount": {\r\n\t\t\t"value": "2000000",\r\n\t\t\t"currency": "IDR"\r\n\t\t},\r\n\t\t"holdAmount": {\r\n\t\t\t"value": "1000000",\r\n\t\t\t"currency": "IDR"\r\n\t\t},\r\n\t\t"availableBalance": {\r\n\t\t\t"value": "1000000",\r\n\t\t\t"currency": "IDR"\r\n\t\t},\r\n\t\t"ledgerBalance": {\r\n\t\t\t"value": "2000000",\r\n\t\t\t"currency": "IDR"\r\n\t\t}\r\n\t}]\r\n}	200  
/openapi/auth/v2.0/account-inquiry-internal	{\r\n  "responseCode": "2001500",\r\n  "responseMessage": "Success",\r\n  "beneficiaryAccountName": "Monica Ester",\r\n  "beneficiaryAccountNo": "1297700000801",\r\n  "beneficiaryAccountType": "S",\r\n  "referenceNo": "20230106110051922",\r\n  "currency": "IDR"\r\n}	200  
/openapi/auth/v2.0/account-inquiry-external	{\r\n  "responseCode": "2001600",\r\n  "responseMessage": "Success",\r\n  "beneficiaryAccountNo": "1000144412",\r\n  "beneficiaryAccountName": "AZWIN ANGGARA",\r\n  "beneficiaryBankName": "PT. BNI 1946 (Persero) Tbk.",\r\n  "additionalInfo": {\r\n    "accountInquiryInfo": {\r\n      "originatorFinInstnId": "FASTIDJA",\r\n      "recipientFinInstnId": "BMRIIDJA",\r\n      "accountInquiryInfoList": [\r\n        {\r\n          "accountInfo": {\r\n            "aliasIdentifer": null,\r\n            "aliasDisplayName": null,\r\n            "aliasFinInstnId": null,\r\n            "accountNbr": "1000144412",\r\n            "accountType": "SVGS",\r\n            "accountOwnerName": "AZWIN ANGGARA"\r\n          },\r\n          "customerInfo": {\r\n            "type": "01",\r\n            "identifier": "3211226812990005",\r\n            "residentStatus": "01",\r\n            "cityName": null\r\n          }\r\n        }\r\n      ],\r\n      "responseStatus": "ACTC",\r\n      "responseReason": "BFSTU000",\r\n      "responseIDs": {\r\n        "trackingID": "20240419FASTIDJA510H9927418060",\r\n        "correlationID": "20240419FASTIDJA51027418061"\r\n      }\r\n    }\r\n  }\r\n}	200  
/openapi/auth/v2.0/transfer-intrabank	{\r\n  "responseCode": "2001700",\r\n  "responseMessage": "Success",\r\n  "sourceAccountNo": "60004400184",\r\n  "beneficiaryAccountNo": "1150097011284",\r\n  "amount": {\r\n    "value": "10000.00",\r\n    "currency": "IDR"\r\n  },\r\n  "referenceNo": "20220323150251228",\r\n  "partnerReferenceNo": "814808435829070",\r\n  "transactionDate": "2022-03-23T14:57:34+07:00"\r\n}	200  
\.


--
-- Name: api_credentials_id_seq; Type: SEQUENCE SET; Schema: public; Owner: user123
--

SELECT pg_catalog.setval('public.api_credentials_id_seq', 1, true);


--
-- Name: api_endpoints_id_seq; Type: SEQUENCE SET; Schema: public; Owner: user123
--

SELECT pg_catalog.setval('public.api_endpoints_id_seq', 7, true);


--
-- Name: api_event_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: user123
--

SELECT pg_catalog.setval('public.api_event_logs_id_seq', 50, true);


--
-- Name: api_tokens_id_seq; Type: SEQUENCE SET; Schema: public; Owner: user123
--

SELECT pg_catalog.setval('public.api_tokens_id_seq', 4, true);


--
-- Name: api_credentials api_credentials_pkey; Type: CONSTRAINT; Schema: public; Owner: user123
--

ALTER TABLE ONLY public.api_credentials
    ADD CONSTRAINT api_credentials_pkey PRIMARY KEY (id);


--
-- Name: api_endpoints api_endpoints_pkey; Type: CONSTRAINT; Schema: public; Owner: user123
--

ALTER TABLE ONLY public.api_endpoints
    ADD CONSTRAINT api_endpoints_pkey PRIMARY KEY (id);


--
-- Name: api_event_logs api_event_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: user123
--

ALTER TABLE ONLY public.api_event_logs
    ADD CONSTRAINT api_event_logs_pkey PRIMARY KEY (id);


--
-- Name: api_tokens api_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: user123
--

ALTER TABLE ONLY public.api_tokens
    ADD CONSTRAINT api_tokens_pkey PRIMARY KEY (id);


--
-- Name: mcm_clients mcm_clients_pkey; Type: CONSTRAINT; Schema: public; Owner: user123
--

ALTER TABLE ONLY public.mcm_clients
    ADD CONSTRAINT mcm_clients_pkey PRIMARY KEY (mcm_client_id);


--
-- Name: api_event_logs api_event_logs_endpoint_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: user123
--

ALTER TABLE ONLY public.api_event_logs
    ADD CONSTRAINT api_event_logs_endpoint_id_fkey FOREIGN KEY (endpoint_id) REFERENCES public.api_endpoints(id) ON DELETE CASCADE;


--
-- Name: api_tokens api_tokens_endpoint_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: user123
--

ALTER TABLE ONLY public.api_tokens
    ADD CONSTRAINT api_tokens_endpoint_id_fkey FOREIGN KEY (endpoint_id) REFERENCES public.api_endpoints(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\connect postgres

--
-- PostgreSQL database dump
--

-- Dumped from database version 11.16 (Debian 11.16-1.pgdg90+1)
-- Dumped by pg_dump version 11.16 (Debian 11.16-1.pgdg90+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database cluster dump complete
--

