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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.api.annotations.Operation;
import org.joget.api.annotations.Param;
import org.joget.api.annotations.Response;
import org.joget.api.annotations.Responses;
import org.joget.api.model.ApiDefinition;
import org.joget.api.model.ApiPluginAbstract;
import org.joget.api.model.ApiResponse;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONObject;

/**
 *
 * @author Cungkring25
 */
public class CekRekening extends ApiPluginAbstract {

    public static final String pluginName = "MCM - API Cek Rekening";

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
            path = "/cekRekening",
            type = Operation.MethodType.GET,
            summary = "Cek Rekening",
            description = "Cek Rekening"
    )
    @Responses({
        @Response(responseCode = 200, description = "Inquiry retrieved successfully", array = false),
        @Response(responseCode = 400, description = "Invalid request", array = false),
        @Response(responseCode = 404, description = "not found", array = false),
        @Response(responseCode = 500, description = "Internal server error", array = false)
    })
    public ApiResponse getMethod(
            @Param(value = "transaction_id", description = "transaction_id") String transaction_id,
            @Param(value = "channel_id", description = "channel_id") String channel_id,
            @Param(value = "partner_reference_no", description = "partner_reference_no") String partner_reference_no,
            @Param(value = "beneficiary_account_no", description = "beneficiary_account_no") String beneficiary_account_no,
            @Param(value = "account_name", description = "account_name") String account_name
    ) {

       
        return new ApiResponse(200,  CekRekening(getToken(),transaction_id,channel_id,partner_reference_no,beneficiary_account_no,account_name));

    }
    
     private String getToken() {
        String val = "false";
        HttpURLConnection con = null;
        BufferedReader reader = null;
        String token_url = "http://localhost:5772/token";

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

//            LogUtil.info(pluginName, "Response Body: " + response.toString());
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Jika sukses, parse token
                JSONObject json = new JSONObject(response.toString());
                val = json.getString("data");
                LogUtil.info(pluginName, "Token: " + val);
            }

        } catch (Exception e) {
            LogUtil.error(pluginName, e, e.getMessage());
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

        return val;
    }
     
     private String CekRekening(String token, String transaction_id_val,String channel_id,String partner_reference_no_val,String beneficiary_account_no_val,String account_name) {
        String val = "";

        LogUtil.info(pluginName, "transaction_id_val: " + transaction_id_val);
        LogUtil.info(pluginName, "channel_id: " + channel_id);
        LogUtil.info(pluginName, "partner_reference_no_val: " + partner_reference_no_val);
        LogUtil.info(pluginName, "beneficiary_account_no_val: " + beneficiary_account_no_val);

        if (!"false".equals(token)) {

            try {
                // Endpoint API
                String url = "https://ms-ebilling-v8-dev.pelni.co.id/mcm/account_inquiry_internal";
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // Set method POST
                con.setRequestMethod("POST");

                // Tambahkan header
                con.setRequestProperty("accept", "application/json");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Data yang dikirim (body request)
                String urlParameters
                        = "token=" + token
                        + "&transaction_id=" + transaction_id_val
                        + "&channel_id=" + channel_id
                        + "&partner_reference_no=" + partner_reference_no_val
                        + "&beneficiary_account_no=" + beneficiary_account_no_val;

                // Kirim request body
                con.setDoOutput(true);
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.writeBytes(urlParameters);
                    wr.flush();
                }

                // Ambil response code
                int responseCode = con.getResponseCode();
//        LogUtil.info(pluginName, "Response Code : " + responseCode);

                // Jika response bukan 200, baca error stream dan log
                if (responseCode != 200) {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();

                    LogUtil.info(pluginName, errorResponse.toString());
                    val = "ERROR"; // atau isi sesuai kebutuhan
                    return val;
                }

                // Baca response body jika responseCode == 200
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Log response
                LogUtil.info(pluginName, "Response Body: " + response.toString());

                // Parsing JSON untuk ambil beneficiaryAccountStatus
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getBoolean("status")) {
                    JSONObject data = jsonResponse.getJSONObject("data");
                    if (data.getBoolean("status")) {
                        JSONObject innerData = data.getJSONObject("data");
                        val = innerData.getString("beneficiaryAccountStatus");
                        String nama = innerData.getString("beneficiaryAccountName");
                        LogUtil.info(pluginName, "nama : " + nama);
                        if (nama.equals(account_name)) {
                            val = val + "-Sesuai";
                        } else {
                            val = val + "-Tidak Sesuai";
                        }
                    } else {
                        LogUtil.warn(pluginName, "Status false pada level data JSON");
                        val = "STATUS_FALSE";
                    }
                } else {
                    LogUtil.warn(pluginName, "Status false dari response JSON utama");
                    val = "STATUS_FALSE";
                }

            } catch (Exception e) {
                LogUtil.error(pluginName, e, e.getMessage());
                e.printStackTrace();
                val = "EXCEPTION";
            }
        } else {
            val = "ERROR";
        }

        return val;
    }
    
    
}
