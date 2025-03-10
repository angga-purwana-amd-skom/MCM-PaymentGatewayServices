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
\.


--
-- Data for Name: api_event_logs; Type: TABLE DATA; Schema: public; Owner: user123
--

COPY public.api_event_logs (id, endpoint_id, request_timestamp, request_method, request_headers, request_body, response_timestamp, response_status_code, response_headers, response_body, error_message) FROM stdin;
3	1	2025-02-25 08:52:15.604229	POST	{"X-SIGNATURE": "RUzn1upquq9WDQ0Z7dx7hi/0BA746K9R10PLNHXSeAg=", "X-TIMESTAMP": "2025-02-25T08:52:15+00:00", "Content-Type": "application/json", "X-CLIENT-KEY": "sample_client_key_0001"}	{"grantType": "client_credentials"}	2025-02-25 08:52:15.604233	200	{"Date": "Tue, 25 Feb 2025 08:52:14 GMT", "Server": "uvicorn", "Content-Type": "application/json", "Content-Length": "811"}	{"expiresIn": "900", "tokenType": "Bearer", "accessToken": "eyJraWQiOiJzc29zIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJiOTU3YWE0NC04ZWZmLTQ0NjYtOWEyMy0xNDg5MzgyOWExZjAiLCJhdWQiOiJqd3QtYXVkIiwiY2xpZW50SWQiOiI5YjZkYjRmMy1iMzYyLTQ5YzEtOTZhZC1jNjMyNTQxNjI5ZTIiLCJpc3MiOiJqd3QtaXNzdWVyIiwicGFydG5lcklkIjoiVUFUQ09SUEFZIiwiZXhwIjoxNTk1MTY3MTQ5LCJpYXQiOjE1ODkxNjcxNDl9.ZzacwbT08Ax7fMQDRwtaFxDz2dpn3cwz58tMI3QonysPXpr0bHRT6W8ZtI_Q0oTjm17A7NsHqwyadbpGPIq38Lw9h5G7kNGmpVzWKAYZZPQlTwuC7zxHdXYwBQXIs7qYD_wIiPQvYIqPwt6p390oBpUiZk7IsVQ_8ucmXbuBaa9-7uWuvdRL72AQr2cFn89Jjaz6fsQy92dapLAQo9iU7k3wgRk3bxQPQjIbbC-osg-OBzQ1Tyx-NuPaqg5Z0y4ccrVfR2Nv9-jt4wEYFdy4EFgLdj7XKmx8MexfGdtrt9Vn4mi5scCiNOGzEoSKqb27SwdAtdL-caOH7czCPwg", "responseCode": "2007300", "responseMessage": "Success"}	\N
\.


--
-- Data for Name: api_tokens; Type: TABLE DATA; Schema: public; Owner: user123
--

COPY public.api_tokens (id, endpoint_id, access_token, token_type, expires_in, issued_at, expires_at) FROM stdin;
3	1	eyJraWQiOiJzc29zIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJiOTU3YWE0NC04ZWZmLTQ0NjYtOWEyMy0xNDg5MzgyOWExZjAiLCJhdWQiOiJqd3QtYXVkIiwiY2xpZW50SWQiOiI5YjZkYjRmMy1iMzYyLTQ5YzEtOTZhZC1jNjMyNTQxNjI5ZTIiLCJpc3MiOiJqd3QtaXNzdWVyIiwicGFydG5lcklkIjoiVUFUQ09SUEFZIiwiZXhwIjoxNTk1MTY3MTQ5LCJpYXQiOjE1ODkxNjcxNDl9.ZzacwbT08Ax7fMQDRwtaFxDz2dpn3cwz58tMI3QonysPXpr0bHRT6W8ZtI_Q0oTjm17A7NsHqwyadbpGPIq38Lw9h5G7kNGmpVzWKAYZZPQlTwuC7zxHdXYwBQXIs7qYD_wIiPQvYIqPwt6p390oBpUiZk7IsVQ_8ucmXbuBaa9-7uWuvdRL72AQr2cFn89Jjaz6fsQy92dapLAQo9iU7k3wgRk3bxQPQjIbbC-osg-OBzQ1Tyx-NuPaqg5Z0y4ccrVfR2Nv9-jt4wEYFdy4EFgLdj7XKmx8MexfGdtrt9Vn4mi5scCiNOGzEoSKqb27SwdAtdL-caOH7czCPwg	Bearer	900	2025-02-25 08:52:15.61659	2025-02-25 09:07:15.599498
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
/openapi/auth/v2.0/access-token/b2b	{\r\n"responseCode":"2007300",\r\n"responseMessage":"Success",\r\n"accessToken":\r\n"eyJraWQiOiJzc29zIiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJiOTU3YWE0NC04ZWZmLTQ0NjYtOWEyMy0xNDg5MzgyOWExZj\r\nAiLCJhdWQiOiJqd3QtYXVkIiwiY2xpZW50SWQiOiI5YjZkYjRmMy1iMzYyLTQ5YzEtOTZhZC1jNjMyNTQxNjI5ZTIiLCJpc3MiO\r\niJqd3QtaXNzdWVyIiwicGFydG5lcklkIjoiVUFUQ09SUEFZIiwiZXhwIjoxNTk1MTY3MTQ5LCJpYXQiOjE1ODkxNjcxNDl9.Zzac\r\nwbT08Ax7fMQDRwtaFxDz2dpn3cwz58tMI3QonysPXpr0bHRT6W8ZtI_Q0oTjm17A7NsHqwyadbpGPIq38Lw9h5G7kNGmpVzWKAYZZPQlTwuC7zxHdXYwBQXIs7qYD_\r\nwIiPQvYIqPwt6p390oBpUiZk7IsVQ_8ucmXbuBaa9-7uWuvdRL72AQr2cFn89Jjaz6fsQy92dapLAQo9iU7k3wgRk3bxQPQjIbbC-osg-OBzQ1Tyx-NuPaqg5Z0y4ccrVfR2Nv9-jt4wEYFdy4EFgLdj7XKmx8MexfGdtrt9Vn4mi5scCiNOGzEoSKqb27SwdAtdL-caOH7czCPwg",\r\n "tokenType": "Bearer",\r\n "expiresIn": "900"\r\n}	200  
\.


--
-- Name: api_credentials_id_seq; Type: SEQUENCE SET; Schema: public; Owner: user123
--

SELECT pg_catalog.setval('public.api_credentials_id_seq', 1, true);


--
-- Name: api_endpoints_id_seq; Type: SEQUENCE SET; Schema: public; Owner: user123
--

SELECT pg_catalog.setval('public.api_endpoints_id_seq', 1, true);


--
-- Name: api_event_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: user123
--

SELECT pg_catalog.setval('public.api_event_logs_id_seq', 3, true);


--
-- Name: api_tokens_id_seq; Type: SEQUENCE SET; Schema: public; Owner: user123
--

SELECT pg_catalog.setval('public.api_tokens_id_seq', 3, true);


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

