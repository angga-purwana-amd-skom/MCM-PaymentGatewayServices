package id.co.itasoft.pelni.mcm.plugin;

import org.joget.api.annotations.Operation;
import org.joget.api.annotations.Param;
import org.joget.api.annotations.Response;
import org.joget.api.annotations.Responses;
import org.joget.api.model.ApiPluginAbstract;
import org.joget.api.model.ApiResponse;
import org.joget.commons.util.LogUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CekMasterBankEBS extends ApiPluginAbstract {
    public static final String PLUGIN_NAME = "MCM - Cek Master Bank EBS";
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

    @Operation(path = "/cek_master_bank_ebs", type = Operation.MethodType.GET, summary = "Cek Master Bank EBS", description = "Ambil data master bank dari EBS berdasarkan parameter bank_name, bank_num, dan bank_id")
    @Responses({
            @Response(responseCode = 200, description = "Data retrieved successfully", array = false),
            @Response(responseCode = 400, description = "Invalid request", array = false),
            @Response(responseCode = 404, description = "Not found", array = false),
            @Response(responseCode = 500, description = "Internal server error", array = false)
    })
    public ApiResponse getMethod(
            @Param(value = "bank_value", description = "Nama bank", required = false) String bankName,
            @Param(value = "bank_num", description = "Kode bank / nomor bank", required = false) String bankNum,
            @Param(value = "bank_id", description = "ID bank", required = false) String bankId) {
        try {
            String result = getMaster(bankName, bankNum, bankId);
            return new ApiResponse(200, result);
        } catch (Exception e) {
            LogUtil.error(PLUGIN_NAME, e, e.getMessage());
            return new ApiResponse(500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String getMaster(String bankName, String bankNum, String bankId) throws Exception {
        HttpURLConnection con = null;
        String endpoint = BASE_API_URL + "ebilling/bank_payment";

        // Build query string jika ada parameter
        StringBuilder query = new StringBuilder();
        if (bankName != null && !bankName.isEmpty()) {
            query.append("bank_name=").append(URLEncoder.encode(bankName, StandardCharsets.UTF_8.toString()));
        }
        if (bankNum != null && !bankNum.isEmpty()) {
            if (query.length() > 0)
                query.append("&");
            query.append("bank_num=").append(URLEncoder.encode(bankNum, StandardCharsets.UTF_8.toString()));
        }
        if (bankId != null && !bankId.isEmpty()) {
            if (query.length() > 0)
                query.append("&");
            query.append("bank_id=").append(URLEncoder.encode(bankId, StandardCharsets.UTF_8.toString()));
        }

        if (query.length() > 0) {
            endpoint += "?" + query;
        }

        LogUtil.info(PLUGIN_NAME, "Calling EBS endpoint: " + endpoint);

        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(endpoint);
            con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);

            int status = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            (status >= 200 && status < 300)
                                    ? con.getInputStream()
                                    : con.getErrorStream(),
                            StandardCharsets.UTF_8));

            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return response.toString();

        } catch (Exception e) {
            LogUtil.error(PLUGIN_NAME, e, e.getMessage());
            throw e;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}