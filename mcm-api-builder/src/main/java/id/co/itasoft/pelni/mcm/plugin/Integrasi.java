/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.pelni.mcm.plugin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.joget.api.annotations.Operation;
import org.joget.api.annotations.Param;
import org.joget.api.annotations.Response;
import org.joget.api.annotations.Responses;
import org.joget.api.model.ApiPluginAbstract;
import org.joget.api.model.ApiResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joget.workflow.model.WorkflowProcessResult;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.service.WorkflowManager;
//
//import org.apache.commons.mail.Email;
//import org.apache.commons.mail.EmailException;
//import org.apache.commons.mail.SimpleEmail;

/**
 *
 * @author Cungkring25
 */
public class Integrasi extends ApiPluginAbstract {

    public static final String pluginName = "MCM - API Integrasi";

    @Override
    public String getName() {
        return pluginName;
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getDescription() {
        return pluginName;
    }

    @Override
    public String getLabel() {
        return pluginName;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
//        return AppUtil.readPluginResource(getClass().getName(), "/properties/api/SampleAPI.json", null, true, getResourceBundlePath());
        return "";
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-vial\"></i>";
    }

    @Override
    public String getTag() {
        return "mcm";
    }

    @Override
    public String getTagDesc() {
        return pluginName;
    }

    @Operation(
            path = "/integrasi",
            type = Operation.MethodType.GET,
            summary = "Integrasi",
            description = "Integrasi"
    )
    @Responses({
        @Response(responseCode = 200, description = "Inquiry retrieved successfully", array = false),
        @Response(responseCode = 400, description = "Invalid request", array = false),
        @Response(responseCode = 404, description = "not found", array = false),
        @Response(responseCode = 500, description = "Internal server error", array = false)
    })
    public ApiResponse getMethod(
            @Param(value = "http://localhost:5772/token", description = "Token URL", required = true) String token_url,
            @Param(value = "https://ms-ebilling-v8-dev.pelni.co.id/mcm/transfer_intrabank", description = "Intra Bank URL", required = false) String intra_bank_url,
            @Param(value = "https://ms-ebilling-v8-dev.pelni.co.id/mcm/transfer_interbank", description = "Inter Bank URL", required = false) String inter_bank_url,
            @Param(value = "https://ms-ebilling-v8-dev.pelni.co.id/mcm/transfer_skn", description = "SKN URL", required = false) String skn_url,
            @Param(value = "https://ms-ebilling-v8-dev.pelni.co.id/mcm/transfer_rtgs", description = "RTGS URL", required = false) String rtgs_url,
            @Param(value = "https://ms-ebilling-v8-dev.pelni.co.id/mcm/account_inquiry_internal", description = "Cek Rekening URL", required = true) String cek_rekening_url,
            @Param(value = "https://ms-ebilling-v8-dev.pelni.co.id/mcm/balance_inquiry", description = "Cek Saldo URL", required = true) String cek_saldo_url,
            @Param(value = "Intrabank/Interbank/RTGS/SKN", description = "Type Integrasi", required = true) String type_integrasi,
            @Param(value = "transaction_id", description = "transaction_id", required = true) String transaction_id,
            @Param(value = "channel_id", description = "channel_id", required = true) String channel_id,
            @Param(value = "partner_reference_no", description = "partner_reference_no", required = true) String partner_reference_no,
            @Param(value = "amount", description = "amount", required = true) String amount,
            @Param(value = "currency", description = "currency", required = true) String currency,
            @Param(value = "beneficiary_account_no", description = "beneficiary_account_no", required = true) String beneficiary_account_no,
            //                @Param(value = "beneficiary_email", description = "beneficiary_email",required = false) String beneficiary_email,
            //                @Param(value = "customer_reference", description = "customer_reference",required = false) String customer_reference,
            //                @Param(value = "fee_type", description = "fee_type",required = false) String fee_type,
            @Param(value = "remark", description = "remark", required = false) String remark,
            @Param(value = "source_account_no", description = "source_account_no", required = true) String source_account_no,
            //                @Param(value = "originator_customer_no", description = "originator_customer_no",required = false) String originator_customer_no,
            //                @Param(value = "originator_customer_name", description = "originator_customer_name",required = false) String originator_customer_name,
            //                @Param(value = "originator_bank_code", description = "originator_bank_code",required = false) String originator_bank_code,
            @Param(value = "beneficiary_account_name", description = "beneficiary_account_name", required = true) String beneficiary_account_name,
            //                @Param(value = "beneficiary_address", description = "beneficiary_address",required = false) String beneficiary_address,
            @Param(value = "beneficiary_bank_code", description = "beneficiary_bank_code", required = false) String beneficiary_bank_code,
            @Param(value = "beneficiary_bank_name", description = "beneficiary_bank_name", required = false) String beneficiary_bank_name,
            @Param(value = "account_no", description = "account_no", required = true) String account_no
    //                @Param(value = "beneficiary_customer_residence", description = "beneficiary_customer_residence",required = false) String beneficiary_customer_residence
    //                @Param(value = "beneficiary_customer_type", description = "beneficiary_customer_type",required = false) String beneficiary_customer_type
    //                @Param(value = "kodepos", description = "kodepos",required = false) String kodepos
    //                @Param(value = "receiver_phone", description = "receiver_phone",required = false) String receiver_phone
    //                @Param(value = "sender_customer_residence", description = "sender_customer_residence",required = false) String sender_customer_residence
    //                @Param(value = "sender_customer_type", description = "sender_customer_type",required = false) String sender_customer_type
    //                @Param(value = "sender_phone", description = "sender_phone",required = false) String sender_phone
    ) {
        String hasil = "";

        //        String channel_id = "95221";
//        String partner_reference_no = "2020102900000000000001";
//        String amount = "5000.00";
//        String currency = "USD";
//        String beneficiary_account_no = "2000100101";
//        String beneficiary_email = "test@mail.com";
//        String customer_reference = "10052019";
//        String fee_type = "OUR";
//        String remark = "remark test";
//        String source_account_no = "2000200202";
//        String originator_customer_no = "999901000003300;12345";
//        String originator_customer_name = "Customer1;Customer2";
//        String originator_bank_code = "001;002";
//
//        String beneficiary_account_name = "Yories Yolanda";
//        String beneficiary_address = "Jakarta";
//        String beneficiary_bank_code = "008";
//        String beneficiary_bank_name = "Bank BRI";
//
//
//        String beneficiary_customer_residence = "1";
//        String beneficiary_customer_type = "1";
//        String kodepos = "12345";
//        String receiver_phone = "08123456789";
//        String sender_customer_residence = "1";
//        String sender_customer_type = "1";
//        String sender_phone = "08123456789";
//            sendEmail();
       
        Map<String, String> datas = GetData(transaction_id);
        String c_no_payment = datas.get("c_no_payment");

        String token = getToken(token_url, c_no_payment);

        if (!"false".equals(token)) {
            String CekRekening = CekRekening(token, transaction_id, channel_id, partner_reference_no, beneficiary_account_no, c_no_payment, cek_rekening_url, beneficiary_account_name);
//            LogUtil.info(pluginName, "masuk cek rekening = " + CekRekening);
            if (!"ERROR".equals(CekRekening)) {
                String CekSaldo = CekSaldo(token, transaction_id, channel_id, partner_reference_no, account_no, c_no_payment, cek_saldo_url, amount);
//                LogUtil.info(pluginName, "masuk cek saldo = " + CekSaldo);
                if (!"ERROR".equals(CekSaldo)) {
                    if ("Intrabank".equals(type_integrasi)) {
//                        LogUtil.info(pluginName, "intra = ");
                        hasil = IntraBank(token, intra_bank_url, transaction_id, channel_id, partner_reference_no, amount,
                                currency, beneficiary_account_no, remark, source_account_no, c_no_payment);
                    } else if ("Interbank".equals(type_integrasi)) {
                        hasil = InterBank(token, inter_bank_url, transaction_id, channel_id, partner_reference_no, amount, currency,
                                beneficiary_account_name, beneficiary_account_no, beneficiary_bank_code, beneficiary_bank_name,
                                source_account_no, c_no_payment);

                    } else if ("RTGS".equals(type_integrasi)) {
                        hasil = Rtgs(token, rtgs_url, transaction_id, channel_id, partner_reference_no, amount, currency, beneficiary_account_name,
                                beneficiary_account_no, beneficiary_bank_code, beneficiary_bank_name,
                                remark,
                                source_account_no, c_no_payment);

                    } else {
                        hasil = Skn(token, skn_url, transaction_id, channel_id, partner_reference_no, amount, currency, beneficiary_account_name,
                                beneficiary_account_no, beneficiary_bank_code, beneficiary_bank_name,
                                remark,
                                source_account_no, c_no_payment);
                    }
                }

            }
        }

        return new ApiResponse(200, hasil);

    }

    private String getToken(String token_urls, String c_no_payment) {
        String val = "false";
        String log = "";
        HttpURLConnection con = null;
        BufferedReader reader = null;
        String token_url = token_urls;

        try {
            // Endpoint URL
            String url = token_url;

            // Buat objek URL
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();

            // Set method GET
            con.setRequestMethod("GET");

            // Tambahkan header
            con.setRequestProperty("accept", "application/json");

            // Ambil response code
            int responseCode = con.getResponseCode();

//            LogUtil.info(pluginName, "Response Code : " + responseCode);
            InputStream inputStream;

            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
                if (inputStream == null) {
                    throw new IOException("Error response with code " + responseCode + " and no body.");
                }
            }

            // Baca response body
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Jika sukses, parse token
                JSONObject json = new JSONObject(response.toString());
                val = json.getString("data");
                LogUtil.info(pluginName, "Token: " + val);
            }
            LogUtil.info(pluginName, "Response Body token: " + response.toString());
            log = response.toString();
        } catch (Exception e) {
            LogUtil.error(pluginName, e, e.getMessage());
            log = e.getMessage().toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LogUtil.error(pluginName, e, e.getMessage());
                }
            }
            if (con != null) {
                con.disconnect();
            }
        }
        insertLog(c_no_payment, log);
        return val;
    }

    private String IntraBank(String token, String intra_bank_url, String transaction_id, String channel_id, String partner_reference_no,
            String amount, String currency, String beneficiary_account_no,
            String remark, String source_account_no, String c_no_payment) {
//        LogUtil.info(pluginName, "dalem intra = ");
        JSONObject result = new JSONObject(); // root JSON
        JSONObject dataObj = new JSONObject(); // isi data
        String responseCodes = "";
        String responseMessage = "ERROR";
        String referenceNo = "";
        String log = "";
        String transactionDate = "";

        try {
            String url = intra_bank_url;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("accept", "application/json");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String urlParameters
                    = "token=" + URLEncoder.encode(token, "UTF-8")
                    + "&transaction_id=" + URLEncoder.encode(transaction_id, "UTF-8")
                    + "&channel_id=" + channel_id
                    + "&partner_reference_no=" + URLEncoder.encode(partner_reference_no, "UTF-8")
                    + "&amount=" + amount
                    + "&currency=" + currency
                    + "&beneficiary_account_no=" + beneficiary_account_no
                    + "&customer_reference="
                    + "&fee_type="
                    + "&remark=" + remark
                    + "&source_account_no=" + source_account_no;

            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();

                result.put("data", new JSONObject()
                        .put("responseMessage", "ERROR")
                );
                return result.toString();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
//                LogUtil.info(pluginName, "Response Body: " + response.toString());
            JSONObject jsonResponse = new JSONObject(response.toString());
            result.put("data", response.toString());

            if (jsonResponse.getBoolean("status")) {

                JSONObject data = jsonResponse.getJSONObject("data");
                if (data.getBoolean("status")) {
                    JSONObject innerData = data.getJSONObject("data");
                    responseCodes = innerData.getString("responseCode");
                    responseMessage = innerData.getString("responseMessage");
                    referenceNo = innerData.getString("referenceNo");
                    transactionDate = innerData.getString("transactionDate");

                    dataObj.put("responseCode", responseCodes);
                    dataObj.put("responseMessage", responseMessage);
                    dataObj.put("referenceNo", referenceNo);

                    result.put("data", dataObj);
                } else {
                    dataObj.put("responseMessage", "ERROR");
                    responseMessage = "ERROR";
                    result.put("data", dataObj);
                }
            } else {
                dataObj.put("responseMessage", "ERROR");
                responseMessage = "ERROR";
                result.put("data", dataObj);
            }
            log = response.toString();
        } catch (Exception e) {
            dataObj.put("responseMessage", "ERROR");
            responseMessage = "ERROR";

            result.put("data", dataObj);

            // Log stack trace (baris error)
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
//            LogUtil.info(pluginName, "Detail error lengkap:\n" + sw.toString());
            log = sw.toString();
        }
//        LogUtil.info(pluginName, "wwwwwwwww=");

        Boolean statusLog = cekLogData(transaction_id);
        LogUtil.info(pluginName, "statusLog = "+statusLog);
        if (statusLog) {
            UpdateLogIntegrasi(transaction_id, transactionDate, responseMessage);
        } else {
            
            insertDataTransfer(transaction_id, transactionDate, responseMessage);
        }
        UpdateStatusIntegrasi(transaction_id, responseMessage);

        insertLog(c_no_payment, log);
        return result.toString();
    }

    private String InterBank(String token, String inter_bank_url, String transaction_id, String channel_id,
            String partner_reference_no, String amount, String currency, String beneficiary_account_name,
            String beneficiary_account_no, String beneficiary_bank_code,
            String beneficiary_bank_name, String source_account_no, String c_no_payment) {

        JSONObject result = new JSONObject(); // root JSON
        JSONObject dataObj = new JSONObject(); // isi data
        String responseCodes = "";
        String responseMessage = "ERROR";
        String referenceNo = "";
        String log = "";
        String transactionDate = "";

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+07:00"));
        String formattedDate = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//        System.out.println(formattedDate);

        try {
            String url = inter_bank_url;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("accept", "application/json");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String urlParameters
                    = "token=" + URLEncoder.encode(token, "UTF-8")
                    + "&transaction_id=" + URLEncoder.encode(transaction_id, "UTF-8")
                    + "&channel_id=" + channel_id
                    + "&partner_reference_no=" + URLEncoder.encode(partner_reference_no, "UTF-8")
                    + "&amount=" + amount
                    + "&currency=" + currency
                    + "&beneficiary_account_name=" + beneficiary_account_name
                    + "&beneficiary_account_no=" + beneficiary_account_no
                    + "&beneficiary_bank_code=" + beneficiary_bank_code
                    + "&beneficiary_bank_name=" + beneficiary_bank_name
                    + "&source_account_no=" + source_account_no;

            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();

                result.put("data", new JSONObject()
                        .put("responseMessage", "ERROR")
                );
                return result.toString();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
//                LogUtil.info(pluginName, "Response Body: " + response.toString());
            JSONObject jsonResponse = new JSONObject(response.toString());
            result.put("data", response.toString());
            if (jsonResponse.getBoolean("status")) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data.getBoolean("status")) {
                    JSONObject innerData = data.getJSONObject("data");
                    responseCodes = innerData.getString("responseCode");
                    responseMessage = innerData.getString("responseMessage");
                    referenceNo = innerData.getString("referenceNo");
                    transactionDate = formattedDate;

                    dataObj.put("responseCode", responseCodes);
                    dataObj.put("responseMessage", responseMessage);
                    dataObj.put("referenceNo", referenceNo);
                    result.put("data", dataObj);
                } else {
                    dataObj.put("responseMessage", "ERROR");
                    responseMessage = "ERROR";
                    result.put("data", dataObj);
                }
            } else {
                dataObj.put("responseMessage", "ERROR");
                responseMessage = "ERROR";
                result.put("data", dataObj);
            }
            log = response.toString();
        } catch (Exception e) {

            dataObj.put("responseMessage", "ERROR");
            responseMessage = "ERROR";

            result.put("data", dataObj);

            // Log stack trace (baris error)
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            LogUtil.info(pluginName, "Detail error lengkap:\n" + sw.toString());
            log = sw.toString();
        }

        Boolean statusLog = cekLogData(transaction_id);
        if (statusLog) {
            
            UpdateLogIntegrasi(transaction_id, transactionDate, responseMessage);
        } else {
            insertDataTransfer(transaction_id, transactionDate, responseMessage);
        }

        UpdateStatusIntegrasi(transaction_id, responseMessage);
        insertLog(c_no_payment, log);
        return result.toString();
    }

    private String Rtgs(String token, String rtgs_bank_url, String transaction_id, String channel_id, String partner_reference_no,
            String amount, String currency, String beneficiary_account_name, String beneficiary_account_no,
            String beneficiary_bank_code, String beneficiary_bank_name,
            String remark,
            String source_account_no, String c_no_payment) {

        JSONObject result = new JSONObject(); // root JSON
        JSONObject dataObj = new JSONObject(); // isi data
        String responseCodes = "";
        String responseMessage = "ERROR";
        String referenceNo = "";
        String log = "";
        String transactionDate = "";

        try {
            String url = rtgs_bank_url;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("accept", "application/json");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String urlParameters
                    = "token=" + URLEncoder.encode(token, "UTF-8")
                    + "&transaction_id=" + URLEncoder.encode(transaction_id, "UTF-8")
                    + "&channel_id=" + channel_id
                    + "&partner_reference_no=" + URLEncoder.encode(partner_reference_no, "UTF-8")
                    + "&amount=" + amount
                    + "&currency=" + currency
                    + "&beneficiary_account_name=" + beneficiary_account_name
                    + "&beneficiary_account_no=" + beneficiary_account_no
                    + "&beneficiary_bank_code=" + beneficiary_bank_code
                    + "&beneficiary_bank_name=" + beneficiary_bank_name
                    + "&receiver_phone="
                    + "&remark=" + remark
                    + "&beneficiary_customer_residence=1"
                    + "&beneficiary_customer_type=2"
                    + "&customer_reference=-"
                    + "&source_account_no=" + source_account_no;

            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();

                result.put("data", new JSONObject()
                        .put("responseMessage", "ERROR")
                );
                return result.toString();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
//                LogUtil.info(pluginName, "Response Body: " + response.toString());
            JSONObject jsonResponse = new JSONObject(response.toString());
            result.put("data", response.toString());
//                a = response.toString();
            if (jsonResponse.getBoolean("status")) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data.getBoolean("status")) {
                    JSONObject innerData = data.getJSONObject("data");
                    responseCodes = innerData.getString("responseCode");
                    responseMessage = innerData.getString("responseMessage");
                    referenceNo = innerData.getString("referenceNo");
                    transactionDate = innerData.getString("transactionDate");

                    dataObj.put("responseCode", responseCodes);
                    dataObj.put("responseMessage", responseMessage);
                    dataObj.put("referenceNo", referenceNo);
                    result.put("data", dataObj);
                } else {
                    dataObj.put("responseMessage", "ERROR");
                    responseMessage = "ERROR";
                    result.put("data", dataObj);
                }
            } else {
                dataObj.put("responseMessage", "ERROR");
                responseMessage = "ERROR";
                result.put("data", dataObj);
            }
            log = response.toString();
        } catch (Exception e) {
            dataObj.put("responseMessage", "ERROR");
            responseMessage = "ERROR";

            result.put("data", dataObj);

            // Log stack trace (baris error)
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
//            LogUtil.info(pluginName, "Detail error lengkap:\n" + sw.toString());
            log = sw.toString();
        }

        Boolean statusLog = cekLogData(transaction_id);
        if (statusLog) {
            
           UpdateLogIntegrasi(transaction_id, transactionDate, responseMessage);
        } else {
            insertDataTransfer(transaction_id, transactionDate, responseMessage);
            
        }

        UpdateStatusIntegrasi(transaction_id, responseMessage);
        insertLog(c_no_payment, log);
        return result.toString();
    }

    private String Skn(String token, String skn_bank_url, String transaction_id, String channel_id, String partner_reference_no,
            String amount, String currency, String beneficiary_account_name, String beneficiary_account_no,
            String beneficiary_bank_code, String beneficiary_bank_name,
            String remark,
            String source_account_no, String c_no_payment) {

        JSONObject result = new JSONObject(); // root JSON
        JSONObject dataObj = new JSONObject(); // isi data
        String responseCodes = "";
        String responseMessage = "ERROR";
        String referenceNo = "";
        String log = "";
        String transactionDate = "";

        try {
            String url = skn_bank_url;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("accept", "application/json");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String urlParameters
                    = "token=" + URLEncoder.encode(token, "UTF-8")
                    + "&transaction_id=" + URLEncoder.encode(transaction_id, "UTF-8")
                    + "&channel_id=" + channel_id
                    + "&partner_reference_no=" + URLEncoder.encode(partner_reference_no, "UTF-8")
                    + "&amount=" + amount
                    + "&currency=" + currency
                    + "&beneficiary_account_name=" + beneficiary_account_name
                    + "&beneficiary_account_no=" + beneficiary_account_no
                    + "&beneficiary_bank_code=" + beneficiary_bank_code
                    + "&beneficiary_bank_name=" + beneficiary_bank_name
                    + "&remark=" + remark
                    + "&beneficiary_customer_residence=1"
                    + "&beneficiary_customer_type=2"
                    + "&customer_reference=-"
                    + "&source_account_no=" + source_account_no;

            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();

                result.put("data", new JSONObject()
                        .put("responseMessage", "ERROR")
                );
                return result.toString();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
//                LogUtil.info(pluginName, "Response Body: " + response.toString());
            JSONObject jsonResponse = new JSONObject(response.toString());
            result.put("data", response.toString());

//                a = response.toString();
            if (jsonResponse.getBoolean("status")) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data.getBoolean("status")) {

                    JSONObject innerData = data.getJSONObject("data");
                    responseCodes = innerData.getString("responseCode");
                    responseMessage = innerData.getString("responseMessage");
                    referenceNo = innerData.getString("referenceNo");
                    transactionDate = innerData.getString("transactionDate");

                    dataObj.put("responseCode", responseCodes);
                    dataObj.put("responseMessage", responseMessage);
                    dataObj.put("referenceNo", referenceNo);
                    result.put("data", dataObj);
                } else {
                    dataObj.put("responseMessage", "ERROR");
                    responseMessage = "ERROR";
                    result.put("data", dataObj);
                }
            } else {
                dataObj.put("responseMessage", "ERROR");
                responseMessage = "ERROR";
                result.put("data", dataObj);
            }
            log = response.toString();
        } catch (Exception e) {
            dataObj.put("responseMessage", "ERROR");
            responseMessage = "ERROR";

            result.put("data", dataObj);

            // Log stack trace (baris error)
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            LogUtil.info(pluginName, "Detail error lengkap:\n" + sw.toString());
            log = sw.toString();
        }

        Boolean statusLog = cekLogData(transaction_id);
        if (statusLog) {
            
            UpdateLogIntegrasi(transaction_id, transactionDate, responseMessage);
        } else {
            insertDataTransfer(transaction_id, transactionDate, responseMessage);
        }

        UpdateStatusIntegrasi(transaction_id, responseMessage);
        insertLog(c_no_payment, log);
        return result.toString();
    }

    private void UpdateStatusIntegrasi(String id, String status) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        String query = "UPDATE app_fd_ebill_payment_req SET c_status_integrasi =? "
                + "WHERE id =? ";
        try {
            con = ds.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, status);
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            LogUtil.error(pluginName, ex, query);
        } finally {
            query = null;
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ex) {
                LogUtil.error(pluginName, ex, query);
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                LogUtil.error(pluginName, ex, query);
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.error(pluginName, ex, query);
            }
        }
    }

    private void insertDataTransfer(String id, String tgl, String status) {

        Map<String, String> datas = GetData(id);
        String id_payment = datas.get("id");
        String c_kode_supplier = datas.get("c_kode_supplier");
        String c_nama_supplier = datas.get("c_nama_supplier");
        String c_supplier_bank_id = datas.get("c_supplier_bank_id");
        String c_supplier_bank_nama = datas.get("c_supplier_bank_nama");
        String c_no_payment = datas.get("c_no_payment");
        String c_mode_payment = datas.get("c_mode_payment");
        String c_metode_transfer = datas.get("c_metode_transfer");
        String from_account = datas.get("from_account");
        String to_account = datas.get("to_account");
        String c_grand_total = datas.get("c_grand_total");
        String c_payment_details = datas.get("c_payment_details");
        String c_remark = datas.get("c_remark");
        String c_email = datas.get("c_email");

        if ("Successful".equals(status)) {
            String kontenEmail = "<table align=\"center\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin:20px auto; background:#ffffff; border:1px solid #ddd; border-radius:6px; font-family:Arial, sans-serif; font-size:14px; color:#333;\">\n"
                    + "  <tr>\n"
                    + "    <td style=\"background-color:#2a6ebb; color:#ffffff; padding:12px 20px; font-size:16px; font-weight:bold; border-radius:6px 6px 0 0;\">\n"
                    + "      Transaction Execution Notification\n"
                    + "    </td>\n"
                    + "  </tr>\n"
                    + "  <tr>\n"
                    + "    <td style=\"padding:20px; line-height:1.6;\">\n"
                    + "      <p style=\"margin:0 0 15px;\">We would like to inform you that your transaction has been successfully processed.</p>\n"
                    + "\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Date/Time:</strong> "+tgl+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Company:</strong> PT PELNI (Persero)</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Transaction Type:</strong> "+c_metode_transfer+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">From Account:</strong> "+from_account+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">To Account:</strong> "+to_account+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Beneficiary Bank:</strong> "+c_supplier_bank_nama+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Amount:</strong> IDR "+c_grand_total+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Reference Number:</strong> "+c_no_payment+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Remark:</strong> "+c_remark+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Payment Details:</strong> "+c_payment_details+"</p>\n"
                    + "    </td>\n"
                    + "  </tr>\n"
                    + "  <tr>\n"
                    + "    <td style=\"padding:15px 20px; border-top:1px solid #ddd; font-size:13px; color:#555; border-radius:0 0 6px 6px;\">\n"
                    + "      Best regards,<br>\n"
                    + "      PT PELNI (Persero)\n"
                    + "    </td>\n"
                    + "  </tr>\n"
                    + "</table>";
            String subjectEmail = "Pembayaran Berhasil - "+c_no_payment;
            String toEmail = c_email;

            sendEmail(kontenEmail, subjectEmail, toEmail);
        }
        PreparedStatement ps = null;

        Connection con = null;
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        String queryInsert = "INSERT INTO app_fd_ebill_mcm_transfer (id, datecreated, datemodified, createdby, createdbyname, c_jumlah, c_nama_supplier, c_kode_bank, \n"
                + "	c_tanggal, c_nama_bank, c_no_payment, c_status, c_kode_supplier, c_from_account, c_jenis_transfer, c_remark, c_to_account,c_mode_payment,c_payment_details,c_email) \n"
                + "VALUES (?, now(), now(), ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?)";
        try {
            con = ds.getConnection();
            ps = con.prepareStatement(queryInsert);
            ps.setString(1, id_payment);
            ps.setString(2, "admin");
            ps.setString(3, "admin");
            ps.setString(4, c_grand_total);
            ps.setString(5, c_nama_supplier);
            ps.setString(6, c_supplier_bank_id);
            ps.setString(7, tgl);
            ps.setString(8, c_supplier_bank_nama);
            ps.setString(9, c_no_payment);
            ps.setString(10, status);
            ps.setString(11, c_kode_supplier);
            ps.setString(12, from_account);
            ps.setString(13, c_metode_transfer);
            ps.setString(14, c_remark);
            ps.setString(15, to_account);
            ps.setString(16, c_mode_payment);
            ps.setString(17, c_payment_details);
            ps.setString(18, c_email);
            ps.execute();
        } catch (SQLException ex) {
            LogUtil.error(Integrasi.class.getName(), ex, "Error : " + ex.getMessage());
        } finally {
            queryInsert = null;
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                LogUtil.error(this.getClass().getName(), e, "Error message ps : " + e.getMessage());
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                LogUtil.error(this.getClass().getName(), e, "Error message con : " + e.getMessage());
            }
        }
    }

    private void UpdateLogIntegrasi(String id, String tgl, String status) {
        Map<String, String> datas = GetData(id);
        String c_supplier_bank_nama = datas.get("c_supplier_bank_nama");
        String c_no_payment = datas.get("c_no_payment");
        String c_metode_transfer = datas.get("c_metode_transfer");
        String from_account = datas.get("from_account");
        String to_account = datas.get("to_account");
        String c_grand_total = datas.get("c_grand_total");
        String c_payment_details = datas.get("c_payment_details");
        String c_remark = datas.get("c_remark");
        String c_email = datas.get("c_email");

        if ("Successful".equals(status)) {
            String kontenEmail = "<table align=\"center\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin:20px auto; background:#ffffff; border:1px solid #ddd; border-radius:6px; font-family:Arial, sans-serif; font-size:14px; color:#333;\">\n"
                    + "  <tr>\n"
                    + "    <td style=\"background-color:#2a6ebb; color:#ffffff; padding:12px 20px; font-size:16px; font-weight:bold; border-radius:6px 6px 0 0;\">\n"
                    + "      Transaction Execution Notification\n"
                    + "    </td>\n"
                    + "  </tr>\n"
                    + "  <tr>\n"
                    + "    <td style=\"padding:20px; line-height:1.6;\">\n"
                    + "      <p style=\"margin:0 0 15px;\">We would like to inform you that your transaction has been successfully processed.</p>\n"
                    + "\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Date/Time:</strong> "+tgl+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Company:</strong> PT PELNI (Persero)</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Transaction Type:</strong> "+c_metode_transfer+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">From Account:</strong> "+from_account+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">To Account:</strong> "+to_account+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Beneficiary Bank:</strong> "+c_supplier_bank_nama+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Amount:</strong> IDR "+c_grand_total+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Reference Number:</strong> "+c_no_payment+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Remark:</strong> "+c_remark+"</p>\n"
                    + "      <p style=\"margin:0 0 8px;\"><strong style=\"display:inline-block; width:160px;\">Payment Details:</strong> "+c_payment_details+"</p>\n"
                    + "    </td>\n"
                    + "  </tr>\n"
                    + "  <tr>\n"
                    + "    <td style=\"padding:15px 20px; border-top:1px solid #ddd; font-size:13px; color:#555; border-radius:0 0 6px 6px;\">\n"
                    + "      Best regards,<br>\n"
                    + "      PT PELNI (Persero)\n"
                    + "    </td>\n"
                    + "  </tr>\n"
                    + "</table>";
            String subjectEmail = "Pembayaran Berhasil - "+c_no_payment;
            String toEmail = c_email;

            sendEmail(kontenEmail, subjectEmail, toEmail);
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        String query = "UPDATE app_fd_ebill_mcm_transfer SET c_tanggal=?,c_status=?, datemodified=now() "
                + "WHERE id =? ";
        try {
            con = ds.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, tgl);
            ps.setString(2, status);
            ps.setString(3, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            LogUtil.error(pluginName, ex, query);
        } finally {
            query = null;
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ex) {
                LogUtil.error(pluginName, ex, query);
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                LogUtil.error(pluginName, ex, query);
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                LogUtil.error(pluginName, ex, query);
            }
        }
    }

    public Map<String, String> GetData(String id) {
        Map<String, String> resultMap = new HashMap<>();
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        String query = "select DISTINCT b.id,d.c_kode_supplier,d.c_nama_supplier,d.c_supplier_bank_id,d.c_supplier_bank_nama, "
                + "b.c_no_payment,b.c_mode_payment,b.c_metode_transfer,CONCAT(b.c_num_bank, ' - ', b.c_currency,' - ',b.c_nama_bank) as from_account, "
                + "concat(d.c_supplier_bank_account_num,' - ',d.c_currency,' - ',d.c_nama_supplier) as to_account,\n"
                + "REPLACE(b.c_grand_total, ',', '.') as c_grand_total,b.c_payment_details,b.c_deskripsi,d.c_email\n"
                + "from app_fd_ebill_payment_req b \n"
                + "join app_fd_ebill_inv_payment c on b.id=c.c_id_pembayaran\n"
                + "join app_fd_ebill_req_header d on d.c_nomor_tagihan = c.c_nomor_tagihan\n"
                + "where b.id=? ";

        try (
                Connection con = ds.getConnection();
                PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultMap.put("id", rs.getString("id"));
                    resultMap.put("c_kode_supplier", rs.getString("c_kode_supplier"));
                    resultMap.put("c_nama_supplier", rs.getString("c_nama_supplier"));
                    resultMap.put("c_supplier_bank_id", rs.getString("c_supplier_bank_id"));
                    resultMap.put("c_supplier_bank_nama", rs.getString("c_supplier_bank_nama"));
                    resultMap.put("c_no_payment", rs.getString("c_no_payment"));
                    resultMap.put("c_mode_payment", rs.getString("c_mode_payment"));
                    resultMap.put("c_metode_transfer", rs.getString("c_metode_transfer"));
                    resultMap.put("from_account", rs.getString("from_account"));
                    resultMap.put("to_account", rs.getString("to_account"));
                    resultMap.put("c_grand_total", rs.getString("c_grand_total"));
                    resultMap.put("c_payment_details", rs.getString("c_payment_details"));
                    resultMap.put("c_remark", rs.getString("c_deskripsi"));
                    resultMap.put("c_email", rs.getString("c_email"));
                }
            } catch (SQLException e) {
                LogUtil.error(getClass().getName(), e, "Error : " + e.getMessage());
            }
        } catch (SQLException ex) {
            LogUtil.error(getClass().getName(), ex, "Error : " + ex.getMessage());
        }

        return resultMap;
    }

    private String CekRekening(String token, String transaction_id_val, String channel_id,
            String partner_reference_no_val, String beneficiary_account_no_val,
            String c_no_payment, String cek_rekening_url, String account_name) {

        String result = ""; // root JSON
        String log = "";
        JSONObject dataObj = new JSONObject(); // isi data

        try {
            String url = cek_rekening_url;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("accept", "application/json");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String urlParameters
                    = "token=" + token
                    + "&transaction_id=" + transaction_id_val
                    + "&channel_id=" + channel_id
                    + "&partner_reference_no=" + partner_reference_no_val
                    + "&beneficiary_account_no=" + beneficiary_account_no_val;

            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();

                result = "ERROR";
                return result;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            LogUtil.info(pluginName, "Response Body: " + response.toString());
            JSONObject jsonResponse = new JSONObject(response.toString());

            if (jsonResponse.getBoolean("status")) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data.getBoolean("status")) {
                    JSONObject innerData = data.getJSONObject("data");
                    String nama = innerData.getString("beneficiaryAccountName");
                    String status = innerData.getString("beneficiaryAccountStatus");

                    if ("Rekening aktif".equals(status)) {
                        if (nama.equals(account_name)) {
                            result = "Successful";
                            LogUtil.info(pluginName, " nama= " + nama);
                            LogUtil.info(pluginName, " account_name= " + account_name);
                        } else {
                            result = "ERROR";
                        }
                    } else {
                        result = "ERROR";
                    }

                } else {
                    result = "ERROR";
                }
            } else {
                result = "ERROR";
            }
            log = response.toString();
        } catch (Exception e) {
            result = "ERROR";

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            LogUtil.info(pluginName, "Detail error lengkap:\n" + sw.toString());
            log = sw.toString();
        }

        insertLog(c_no_payment, log);
        return result;
    }

    private void insertLog(String no_payment, String log) {

        PreparedStatement ps = null;

        Connection con = null;
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        String queryInsert = "INSERT INTO app_fd_log_integrasi (id,no_payment,log) \n"
                + "VALUES (gen_random_uuid(),?,?)";
        try {
            con = ds.getConnection();
            ps = con.prepareStatement(queryInsert);
            ps.setString(1, no_payment);
            ps.setString(2, log);
            ps.execute();
        } catch (SQLException ex) {
            LogUtil.error(Integrasi.class.getName(), ex, "Error : " + ex.getMessage());
        } finally {
            queryInsert = null;
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
                LogUtil.error(this.getClass().getName(), e, "Error message ps : " + e.getMessage());
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                LogUtil.error(this.getClass().getName(), e, "Error message con : " + e.getMessage());
            }
        }
    }

    private String CekSaldo(String token, String transaction_id_val, String channel_id,
            String partner_reference_no_val, String account_no,
            String c_no_payment, String cek_saldo_url, String amount) {

        String result = ""; // root JSON
        String log = "";
        JSONObject dataObj = new JSONObject(); // isi data

        try {
            String url = cek_saldo_url;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("accept", "application/json");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String urlParameters
                    = "token=" + token
                    + "&transaction_id=" + transaction_id_val
                    + "&channel_id=" + channel_id
                    + "&partner_reference_no=" + partner_reference_no_val
                    + "&bank_card_token=-"
                    + "&account_no=" + account_no;

            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();

                result = "ERROR";
                return result;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
//            LogUtil.info(pluginName, "Response Body: " + response.toString());
            JSONObject jsonResponse = new JSONObject(response.toString());

            if (jsonResponse.getBoolean("status")) {
                JSONObject data = jsonResponse.getJSONObject("data");

//                JSONObject root = new JSONObject(response);      // JSON utama
                JSONObject data1 = jsonResponse.getJSONObject("data");         // ambil "data" pertama
                JSONObject data2 = data1.getJSONObject("data");        // ambil "data" kedua di dalamnya

                JSONArray accountInfos = data2.getJSONArray("accountInfos");  // baru bisa ambil array
                JSONObject firstAccount = accountInfos.getJSONObject(0);
                JSONObject availableBalance = firstAccount.getJSONObject("availableBalance");
                String value = availableBalance.getString("value");
                String currency = availableBalance.getString("currency");

//                LogUtil.info(pluginName, "value = " + value);
//                LogUtil.info(pluginName, "amount total = " + amount);
                BigDecimal decimalValue = new BigDecimal(value);
                BigDecimal decimalAmount = new BigDecimal(amount);
                LogUtil.info(pluginName, "decimalValue = " + decimalValue);
                LogUtil.info(pluginName, "decimalAmount = " + decimalAmount);

                if (decimalValue.compareTo(decimalAmount) > 0 || decimalValue.compareTo(decimalAmount) == 0) {
                    result = "Successful";
                    LogUtil.info(pluginName, "masuk ");
                } else {
                    result = "ERROR";
                    LogUtil.info(pluginName, "keluar ");
                }

            } else {
                result = "ERROR";
            }
            log = response.toString();
        } catch (Exception e) {
            result = "ERROR";

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            LogUtil.info(pluginName, "Detail error lengkap:\n" + sw.toString());
            log = sw.toString();
        }

        insertLog(c_no_payment, log);
        return result;
    }

    public Boolean cekLogData(String id) {
        Boolean result = false;
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        String query = "select count(*) as jumlah from app_fd_ebill_mcm_transfer\n"
                + "where id = ? ";

        try (
                Connection con = ds.getConnection();
                PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (!"0".equals(rs.getString("jumlah"))) {
                        result = true;
                    }

                }
            } catch (SQLException e) {
                LogUtil.error(getClass().getName(), e, "Error : " + e.getMessage());
            }
        } catch (SQLException ex) {
            LogUtil.error(getClass().getName(), ex, "Error : " + ex.getMessage());
        }

        return result;
    }

    private void sendEmail(String kontenEmail, String subjectEmail, String toEmail) {
        LogUtil.info(pluginName, "start send email");
        WorkflowManager wm = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
        Map variableMap = new HashMap();
        variableMap.put("kontenEmail", kontenEmail);
        variableMap.put("subjectEmail", subjectEmail);
        variableMap.put("toEmail", toEmail);
        wm.processStart("ebilling_apps:latest:sendEmailSupplier", variableMap);
    }

}
