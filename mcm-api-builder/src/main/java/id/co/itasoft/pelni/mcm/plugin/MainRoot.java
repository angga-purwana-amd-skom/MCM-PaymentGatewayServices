package id.co.itasoft.pelni.mcm.plugin;

import id.co.itasoft.net.purwana.apiware.Apiware;
import id.co.itasoft.net.purwana.apiware.ApiwareRequest;
import id.co.itasoft.net.purwana.apiware.ApiwareResponse;
import id.co.itasoft.net.purwana.apiware.ApiwareTokenConfig;
import org.joget.api.annotations.Operation;
import org.joget.api.annotations.Param;
import org.joget.api.annotations.Response;
import org.joget.api.annotations.Responses;
import org.joget.api.model.ApiPluginAbstract;
import org.joget.api.model.ApiResponse;
import org.joget.commons.util.LogUtil;
import org.json.JSONObject;

public class MainRoot extends ApiPluginAbstract {

    public static final String PLUGIN_NAME = "MCM - API Connect";
    private String BASE_API_URL = "https://ms-ebilling-v8-dev.pelni.co.id/mcm/";
    private String TOKEN_API_URL = "http://localhost:5772/token";

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

    @Operation(
            path = "/account_inquiry_internal",
            type = Operation.MethodType.POST,
            summary = "Mengakses informasi beneficiary akun (Internal)",
            description = "Mengakses informasi beneficiary akun (Internal)"
    )
    @Responses({
        @Response(responseCode = 200, description = "Inquiry retrieved successfully", array = false),
        @Response(responseCode = 400, description = "Invalid request", array = false),
        @Response(responseCode = 404, description = "not found", array = false),
        @Response(responseCode = 500, description = "Internal server error", array = false)
    })
    // Endpoint to get menus based on appId and appVersion
    public ApiResponse mcmAccountInquiryInternal(
            @Param(value = "transaction_id", description = "transaction_id") String transaction_id,
            @Param(value = "channel_id", description = "channel_id") String channel_id,
            @Param(value = "partner_reference_no", description = "partner_reference_no") String partner_reference_no,
            @Param(value = "beneficiary_account_no", description = "beneficiary_account_no") String beneficiary_account_no
    ) {
        try {

            if (transaction_id == null || transaction_id.trim().isEmpty()) {
                return new ApiResponse(400, createErrorJson("Missing or empty 'transaction_id' parameter"));
            }

            if (channel_id == null || channel_id.trim().isEmpty()) {
                return new ApiResponse(400, createErrorJson("Missing or empty 'channel_id' parameter"));
            }

            if (partner_reference_no == null || partner_reference_no.trim().isEmpty()) {
                return new ApiResponse(400, createErrorJson("Missing or empty 'partner_reference_no' parameter"));
            }

            if (beneficiary_account_no == null || beneficiary_account_no.trim().isEmpty()) {
                return new ApiResponse(400, createErrorJson("Missing or empty 'beneficiary_account_no' parameter"));
            }

            ApiwareTokenConfig tokenConfig = new ApiwareTokenConfig(TOKEN_API_URL)
                    .withMethod("GET")
                    .withTokenField("token")
                    .withTokenFieldFromJSON("data");
            
            String[] requiredFieldsForGenerateToken = {};
            ///
            Apiware testApiware = new Apiware(tokenConfig);

            ApiwareRequest requestAccountInquiryInternal = new ApiwareRequest(BASE_API_URL + "account_inquiry_internal")
                    .withMethod("POST")
                    .withBodyType("form-data")
                    .withBodyParam("transaction_id", transaction_id)
                    .withBodyParam("channel_id", channel_id)
                    .withBodyParam("partner_reference_no", partner_reference_no)
                    .withBodyParam("beneficiary_account_no", beneficiary_account_no);

            ApiwareResponse responseAccountInquiryInternal = testApiware.send(requestAccountInquiryInternal, requiredFieldsForGenerateToken);


            JSONObject response = responseAccountInquiryInternal.getBodyJSON();

            return new ApiResponse(200, response);

        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Error in AccountInquiryInternal : " + e.getMessage());
            return new ApiResponse(500, createErrorJson("Internal server error"));
        }
    }
    
    ///////////////////////
    
    @Operation(
            path = "/account_inquiry_external",
            type = Operation.MethodType.POST,
            summary = "Mengakses informasi beneficiary akun (External)",
            description = "Mengakses informasi beneficiary akun (External)"
    )
    @Responses({
        @Response(responseCode = 200, description = "Inquiry retrieved successfully", array = false),
        @Response(responseCode = 400, description = "Invalid request", array = false),
        @Response(responseCode = 404, description = "not found", array = false),
        @Response(responseCode = 500, description = "Internal server error", array = false)
    })
    // Endpoint to get menus based on appId and appVersion
    public ApiResponse mcmAccountInquiryExternal(
            @Param(value = "transaction_id", description = "transaction_id") String transaction_id,
            @Param(value = "channel_id", description = "channel_id") String channel_id,
            @Param(value = "partner_reference_no", description = "partner_reference_no") String partner_reference_no,
            @Param(value = "beneficiary_account_no", description = "beneficiary_account_no") String beneficiary_account_no,
            @Param(value = "beneficiary_bank_code", description = "beneficiary_bank_code") String beneficiary_bank_code     
    ) {
        try {

            if (transaction_id == null || transaction_id.trim().isEmpty()) {
                return new ApiResponse(400, createErrorJson("Missing or empty 'transaction_id' parameter"));
            }

            if (channel_id == null || channel_id.trim().isEmpty()) {
                return new ApiResponse(400, createErrorJson("Missing or empty 'channel_id' parameter"));
            }

            if (partner_reference_no == null || partner_reference_no.trim().isEmpty()) {
                return new ApiResponse(400, createErrorJson("Missing or empty 'partner_reference_no' parameter"));
            }

            if (beneficiary_account_no == null || beneficiary_account_no.trim().isEmpty()) {
                return new ApiResponse(400, createErrorJson("Missing or empty 'beneficiary_account_no' parameter"));
            }
            
            if (beneficiary_bank_code == null || beneficiary_bank_code.trim().isEmpty()) {
                return new ApiResponse(400, createErrorJson("Missing or empty 'beneficiary_bank_code' parameter"));
            }            

            ApiwareTokenConfig tokenConfig = new ApiwareTokenConfig(TOKEN_API_URL)
                    .withMethod("GET")
                    .withTokenField("token")
                    .withTokenFieldFromJSON("data");
            
            String[] requiredFieldsForGenerateToken = {};
            ///
            Apiware testApiware = new Apiware(tokenConfig);

            ApiwareRequest requestAccountInquiryExternal = new ApiwareRequest(BASE_API_URL + "account_inquiry_external")
                    .withMethod("POST")
                    .withBodyType("form-data")
                    .withBodyParam("transaction_id", transaction_id)
                    .withBodyParam("channel_id", channel_id)
                    .withBodyParam("partner_reference_no", partner_reference_no)
                    .withBodyParam("beneficiary_account_no", beneficiary_account_no)
                    .withBodyParam("beneficiary_bank_code", beneficiary_bank_code);

            ApiwareResponse responseAccountInquiryExternal = testApiware.send(requestAccountInquiryExternal, requiredFieldsForGenerateToken);


            JSONObject response = responseAccountInquiryExternal.getBodyJSON();

            return new ApiResponse(200, response);

        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Error in AccountInquiryExternal : " + e.getMessage());
            return new ApiResponse(500, createErrorJson("Internal server error"));
        }
    }    

    private JSONObject createErrorJson(String message) {
        JSONObject obj = new JSONObject();
        obj.put("error", message);
        return obj;
    }
}
