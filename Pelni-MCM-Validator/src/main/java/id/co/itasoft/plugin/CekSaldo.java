package id.co.itasoft.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.math3.analysis.function.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CekSaldo extends FormValidator {

    // url di properties
    public static String pluginName = "PELNI - MCM - Cek Saldo";

    // ApiwareTokenConfig tokenConfig = new
    // ApiwareTokenConfig("http://localhost:5772/token")
    // .withMethod("GET")
    // .withTokenField("token")
    // .withTokenFieldFromJSON("data");
    // String[] requiredFieldsForGenerateToken = {};
    // ///
    // Apiware testApiware = new Apiware(tokenConfig);

    @Override
    public boolean validate(Element element, FormData fd, String[] values) {
        LogUtil.info(getClassName(), "cek saldo aja");
        String message = "";
        boolean stat = false;

        Form form = FormUtil.findRootForm(element);

        // Get grand_total field value
        Element grandTotalElement = FormUtil.findElement("grand_total", form, fd);
        String grandTotalStr = FormUtil.getElementPropertyValue(grandTotalElement, fd);
        LogUtil.info(getClassName(), "grandTotal: " + grandTotalStr);

        BigDecimal grandTotal = new BigDecimal(grandTotalStr.replace(",", "."));

        LogUtil.info(getClassName(), "grandTotal as BigDecimal: " + grandTotal);

        // Call balance inquiry
        BigDecimal availableBalance = CekSaldoRekening(getToken(), fd, element);
        LogUtil.info(getClassName(), "availableBalance: " + availableBalance);

        if (grandTotal.compareTo(availableBalance) > 0) {
            message = getPropertyString("message_saldo_tdk_cukup"); // saldo tidak cukup
            stat = false;
        } else {
            stat = true; // saldo cukup
        }

        if (!stat) {
            fd.addFormError(FormUtil.getElementParameterName(element), message);
        }

        return stat;
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
        LogUtil.info(pluginName, "Getting token from: " + token_url);

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

            // LogUtil.info(pluginName, "Response Code : " + responseCode);
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

            // LogUtil.info(pluginName, "Response Body: " + response.toString());
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

    private BigDecimal CekSaldoRekening(String token, FormData fd, Element element) {
        BigDecimal availableBalance = BigDecimal.ZERO;
        // Get account no from form (if needed)
        String balance_inquiry_url = "https://ms-ebilling-v8-dev.pelni.co.id/mcm/balance_inquiry";
        LogUtil.info(getClassName(), "Balance Inquiry URL: " + balance_inquiry_url);
        Form form = FormUtil.findRootForm(element);
        Element accountNumElement = FormUtil.findElement("chosen_account_num", form, fd);
        String accountNumStr = FormUtil.getElementPropertyValue(accountNumElement, fd);

        LogUtil.info(getClassName(), "CekSaldoRekening with token: " + token);
        LogUtil.info(getClassName(), "Available balance begin: " + availableBalance);

        if (!"false".equals(token)) {
            try {
                String url = balance_inquiry_url;
                URL obj = new URL(balance_inquiry_url);

                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("accept", "application/json");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String urlParameters = "token=" + token
                        + "&transaction_id=" + getPropertyString("transaction_id")
                        + "&channel_id=" + getPropertyString("channel_id")
                        + "&partner_reference_no=" + getPropertyString("partner_reference_no")
                        + "&bank_card_token=" + getPropertyString("bank_card_token")
                        + "&account_no=" + getPropertyString("account_no");

                con.setDoOutput(true);
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.writeBytes(urlParameters);
                    wr.flush();
                }

                int responseCode = con.getResponseCode();
                LogUtil.info(pluginName, "Balance inquiry response code: " + responseCode);

                if (responseCode != 200) {
                    LogUtil.warn(pluginName, "Balance inquiry failed with code " + responseCode);
                    BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(con.getErrorStream(), "UTF-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();

                    LogUtil.info(pluginName, errorResponse.toString());
                    return BigDecimal.ZERO;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                LogUtil.info(pluginName, "Balance inquiry raw response: " + response.toString());

                JSONObject jsonResponse = new JSONObject(response.toString());

                if (jsonResponse.getBoolean("status")) {
                    JSONObject outerData = jsonResponse.getJSONObject("data");
                    if (outerData.getBoolean("status")) {
                        JSONObject innerData = outerData.getJSONObject("data");
                        JSONArray accountInfos = innerData.getJSONArray("accountInfos");
                        if (accountInfos.length() > 0) {
                            JSONObject accountInfo = accountInfos.getJSONObject(0);
                            JSONObject availObj = accountInfo.getJSONObject("availableBalance");
                            String value = availObj.getString("value");
                            availableBalance = new BigDecimal(value);
                            LogUtil.info(pluginName, "Available balance: " + availableBalance);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error(pluginName, e, e.getMessage());
                e.printStackTrace();
                availableBalance = BigDecimal.ZERO;
            }
        }

        return availableBalance;
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
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/CekSaldo.json");
    }
}
