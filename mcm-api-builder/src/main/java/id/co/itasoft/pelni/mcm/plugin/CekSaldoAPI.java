package id.co.itasoft.pelni.mcm.plugin;


import org.joget.api.annotations.Operation;
import org.joget.api.annotations.Param;
import org.joget.api.annotations.Response;
import org.joget.api.annotations.Responses;
import org.joget.api.model.ApiPluginAbstract;
import org.joget.api.model.ApiResponse;
import org.joget.commons.util.LogUtil;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class CekSaldoAPI extends ApiPluginAbstract {

    public static final String PLUGIN_NAME = "MCM - Cek Saldo API";
    private String BASE_API_URL = "https://ms-ebilling-v8-dev.pelni.co.id/";

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-vial\"></i>";
    }

    @Override
    public String getTag() {
        return "mcm";
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return PLUGIN_NAME;
    }

    @Override
    public String getLabel() {
        return PLUGIN_NAME;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return null;
    }

    @Operation(path = "/cek_saldo", type = Operation.MethodType.GET, summary = "Cek Saldo", description = "Cek Saldo")
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
            @Param(value = "bank_card_token", description = "bank_card_token") String bank_card_token,
            @Param(value = "account_no", description = "account_no") String account_no) {

        return new ApiResponse(200,
                CekSaldo(getToken(), transaction_id, channel_id, partner_reference_no, bank_card_token, account_no));

    }

    private String getToken() {
        String val = "false";
        HttpURLConnection con = null;
        BufferedReader reader = null;
        String token_url = "http://localhost:5772/token";
        // String token_url = BASE_API_URL + "token";
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

            LogUtil.info(PLUGIN_NAME, "Response Code : " + responseCode);
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

            // LogUtil.info(PLUGIN_NAME, "Response Body: " + response.toString());
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Jika sukses, parse token
                JSONObject json = new JSONObject(response.toString());
                val = json.getString("data");
                LogUtil.info(PLUGIN_NAME, "Token: " + val);
            }

        } catch (Exception e) {
            LogUtil.error(PLUGIN_NAME, e, e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LogUtil.error(PLUGIN_NAME, e, e.getMessage());
                }
            }
            if (con != null) {
                con.disconnect();
            }
        }

        return val;
    }

    private String CekSaldo(String token, String transaction_id_val, String channel_id, String partner_reference_no_val,
                            String bank_card_token_val, String account_no) {
        String val = "";

        if (!"false".equals(token)) {
            try {
                // Endpoint API
                String url = BASE_API_URL + "/mcm/balance_inquiry";
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // Set method POST
                con.setRequestMethod("POST");

                // Tambahkan header
                con.setRequestProperty("accept", "application/json");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Data yang dikirim (body request)
                String urlParameters = "token=" + token
                        + "&transaction_id=" + transaction_id_val
                        + "&channel_id=" + channel_id
                        + "&partner_reference_no=" + partner_reference_no_val
                        + "&bank_card_token=" + bank_card_token_val
                        + "&account_no=" + account_no;

                // Kirim request body
                con.setDoOutput(true);
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.writeBytes(urlParameters);
                    wr.flush();
                }

                // Ambil response code
                int responseCode = con.getResponseCode();

                if (responseCode != 200) {
                    // error handling
                    BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(con.getErrorStream(), "UTF-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();

                    LogUtil.info(PLUGIN_NAME, "Error Body: " + errorResponse.toString());
                    val = errorResponse.toString(); // << return the actual error body too
                    return val;
                }

                // Success → baca response body
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Assign ke val supaya dikembalikan ke ApiResponse
                val = response.toString();

                LogUtil.info(PLUGIN_NAME, "Response Body: " + val);

            } catch (Exception e) {
                LogUtil.error(PLUGIN_NAME, e, e.getMessage());
                val = "EXCEPTION: " + e.getMessage();
            }
        } else {
            val = "ERROR: invalid token";
        }

        return val;
    }

}
