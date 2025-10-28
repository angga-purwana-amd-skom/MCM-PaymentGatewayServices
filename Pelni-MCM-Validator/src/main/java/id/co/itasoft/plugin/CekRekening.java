/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.TextField;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.joget.apps.form.model.Form;
import org.joget.workflow.model.service.WorkflowUserManager;

/**
 *
 * @author Cungkring25
 */
public class CekRekening extends FormValidator {

    private final String pluginName = "Pelni - Validator Cek Rekening ";
    private final String pluginClassName = this.getClass().getName();

    @Override
    public boolean validate(Element element, FormData fd, String[] values) {
        String message = "";
        boolean stat = false;

        String message_error_conection = getPropertyString("message_error_conection");
        String message_error_endpoint = getPropertyString("message_error_endpoint");
        String message_rek_aktif_tdk_sesuai = getPropertyString("message_rek_aktif_tdk_sesuai");
        String message_rek_tdk_aktif_tdk_sesuai = getPropertyString("message_rek_tdk_aktif_tdk_sesuai");
        String message_rek_tdk_aktif_sesuai = getPropertyString("message_rekening_non_aktif");

        Form form = FormUtil.findRootForm(element);

        String email = getPropertyString("email");
        Element emailElement = FormUtil.findElement(email, form, fd);
        String email_val = FormUtil.getElementPropertyValue(emailElement, fd);
        
        String bank_name = getPropertyString("bank_name");
        Element bank_nameElement = FormUtil.findElement(bank_name, form, fd);
        String bank_name_val = FormUtil.getElementPropertyValue(bank_nameElement, fd);
        
        
        String status ="";
        
        String bank_code = getBank_code(bank_name_val);
        String cekRekening = "";

        if ("008".equals(bank_code)) {
            status = CekRekeningInternal(getToken(), fd, element);
        } else {
            status = CekRekeningExternal(getToken(), fd, element,bank_code);
        }


//        String status = CekRekening(getToken(), fd, element);
//         status = CekRekeningInternal(getToken(), fd, element);
        LogUtil.info(message, "status = " + status);
        if (!"".equals(email_val)) {
            if ("ERROR".equals(status)) {
                LogUtil.info(pluginClassName, "masuk1");
                message = message_error_conection;
                fd.addFormError(FormUtil.getElementParameterName(element), message);
                stat = false;
            } else if ("STATUS_FALSE".equals(status)) {
                LogUtil.info(pluginClassName, "masuk2");
                message = message_error_endpoint;
                fd.addFormError(FormUtil.getElementParameterName(element), message);
                stat = false;
            } else if ("Rekening aktif-Sesuai".equals(status)) {
                LogUtil.info(pluginClassName, "masuk3");
//                message = "true";
                stat = true;
            } else if ("Rekening aktif-Tidak Sesuai".equals(status)) {
                LogUtil.info(pluginClassName, "masuk4");
                message = message_rek_aktif_tdk_sesuai;
                fd.addFormError(FormUtil.getElementParameterName(element), message);
                stat = false;
            } else if ("Tidak Aktif-Sesuai".equals(status)) {
                LogUtil.info(pluginClassName, "masuk5");
                message = message_rek_tdk_aktif_sesuai;
                fd.addFormError(FormUtil.getElementParameterName(element), message);
                stat = false;
            } else {
                LogUtil.info(pluginClassName, "masuk6");
                message = message_rek_tdk_aktif_tdk_sesuai;
                fd.addFormError(FormUtil.getElementParameterName(element), message);
                stat = false;
            }
        } else {
            LogUtil.info(pluginClassName, "masuk7");
            message = "Suplier Tidak Memiliki Email";
            fd.addFormError(FormUtil.getElementParameterName(element), message);
            stat = false;
        }
        LogUtil.info(pluginClassName, "statusssss = " + stat);
//
        
        return stat;
    }

    @Override
    public String getName() {
        return pluginName;
    }

    @Override
    public String getVersion() {
        return "8.0.0";
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
        return pluginClassName;
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(pluginClassName, "/properties/CekRekening.json");
    }

    protected boolean validateMandatory(FormData data, String id, String[] values, String message) {
        boolean result = true;
        message = "Missing required value";
        if (values == null || values.length == 0) {
            result = false;
            if (id != null) {
                data.addFormError(id, message);
            }
        } else {

            for (String val : values) {

                if (val == null || val.trim().length() == 0) {
                    result = false;
                    data.addFormError(id, message);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String getElementDecoration() {
        String decoration = "";
        decoration += " * ";
        if (decoration.trim().length() > 0) {
            decoration = decoration.trim();
        }
        return decoration;
    }

    private String getToken() {
        String val = "false";
        HttpURLConnection con = null;
        BufferedReader reader = null;
        String token_url = getPropertyString("token_url");

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

    private String CekRekeningInternal(String token, FormData fd, Element element) {
        String val = "";
        Form form = FormUtil.findRootForm(element);
        String account_inquiry_internal_url = getPropertyString("account_inquiry_internal_url");

        String transaction_id = getPropertyString("transaction_id");
        Element transaction_idElement = FormUtil.findElement(transaction_id, form, fd);
        String transaction_id_val = FormUtil.getElementPropertyValue(transaction_idElement, fd);

        String channel_id = getPropertyString("channel_id");

        String partner_reference_no = getPropertyString("partner_reference_no");
        Element partner_reference_noElement = FormUtil.findElement(partner_reference_no, form, fd);
        String partner_reference_no_val = FormUtil.getElementPropertyValue(partner_reference_noElement, fd);

        String beneficiary_account_no = getPropertyString("beneficiary_account_no");
        Element beneficiary_account_noElement = FormUtil.findElement(beneficiary_account_no, form, fd);
        String beneficiary_account_no_val = FormUtil.getElementPropertyValue(beneficiary_account_noElement, fd);

        String account_name = getPropertyString("account_name");
        Element account_nameElement = FormUtil.findElement(account_name, form, fd);
        String account_nameval = FormUtil.getElementPropertyValue(account_nameElement, fd);

        LogUtil.info(pluginName, "account_inquiry_internal_url: " + account_inquiry_internal_url);
        LogUtil.info(pluginName, "transaction_id_val: " + transaction_id_val);
        LogUtil.info(pluginName, "channel_id: " + channel_id);
        LogUtil.info(pluginName, "partner_reference_no_val: " + partner_reference_no_val);
        LogUtil.info(pluginName, "beneficiary_account_no_val: " + beneficiary_account_no_val);
        LogUtil.info(pluginName, "account_nameval: " + account_nameval);

        if (!"false".equals(token)) {

            try {
                // Endpoint API
                String url = account_inquiry_internal_url;
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
                        if (nama.equals(account_nameval)) {
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
    
    private String CekRekeningExternal(String token, FormData fd, Element element,String bank_code) {
        String val = "";
        Form form = FormUtil.findRootForm(element);
        String account_inquiry_external_url = getPropertyString("account_inquiry_external_url");

        String transaction_id = getPropertyString("transaction_id");
        Element transaction_idElement = FormUtil.findElement(transaction_id, form, fd);
        String transaction_id_val = FormUtil.getElementPropertyValue(transaction_idElement, fd);

        String channel_id = getPropertyString("channel_id");

        String partner_reference_no = getPropertyString("partner_reference_no");
        Element partner_reference_noElement = FormUtil.findElement(partner_reference_no, form, fd);
        String partner_reference_no_val = FormUtil.getElementPropertyValue(partner_reference_noElement, fd);

        String beneficiary_account_no = getPropertyString("beneficiary_account_no");
        Element beneficiary_account_noElement = FormUtil.findElement(beneficiary_account_no, form, fd);
        String beneficiary_account_no_val = FormUtil.getElementPropertyValue(beneficiary_account_noElement, fd);

        String account_name = getPropertyString("account_name");
        Element account_nameElement = FormUtil.findElement(account_name, form, fd);
        String account_nameval = FormUtil.getElementPropertyValue(account_nameElement, fd);

        LogUtil.info(pluginName, "account_inquiry_external_url: " + account_inquiry_external_url);
        LogUtil.info(pluginName, "transaction_id_val: " + transaction_id_val);
        LogUtil.info(pluginName, "channel_id: " + channel_id);
        LogUtil.info(pluginName, "partner_reference_no_val: " + partner_reference_no_val);
        LogUtil.info(pluginName, "beneficiary_account_no_val: " + beneficiary_account_no_val);
        LogUtil.info(pluginName, "account_nameval: " + account_nameval);

        if (!"false".equals(token)) {

            try {
                // Endpoint API
                String url = account_inquiry_external_url;
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
                        + "&beneficiary_bank_code=" + bank_code
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
                        if (nama.equals(account_nameval)) {
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
    
    private String getBank_code(String bank) {
        String ret = "";
        WorkflowUserManager wum = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        String currentUser = wum.getCurrentUsername();
        String query = "SELECT c_bank_code\n"
                + "FROM app_fd_ebill_mcm_banks\n"
                + "WHERE c_bank_name = ? \n"
                + "LIMIT 1";
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");

        try (Connection con = ds.getConnection();
                PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, bank);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret = rs.getString("c_bank_code");
                }
            }

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "Error executing query: " + query);
        }
//LogUtil.info(getClass().getName(), "ret = "+ret);
        return ret;
    }
}
