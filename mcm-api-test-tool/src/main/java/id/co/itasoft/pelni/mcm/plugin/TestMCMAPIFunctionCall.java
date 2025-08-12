/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.co.itasoft.pelni.mcm.plugin;

import id.co.itasoft.net.purwana.apiware.Apiware;
import id.co.itasoft.net.purwana.apiware.ApiwareRequest;
import id.co.itasoft.net.purwana.apiware.ApiwareResponse;
import id.co.itasoft.net.purwana.apiware.ApiwareTokenConfig;
import java.util.Map;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;

/**
 *
 * @author Angga Purwana
 */
public class TestMCMAPIFunctionCall extends DefaultApplicationPlugin {

    public static String pluginName = "PELNI - MCM - TestMCMAPIFunctionCall";
    private String BASE_API_URL = "https://ms-ebilling-v8-dev.pelni.co.id/mcm/";
    private String transactionId = "2020102900000000000001";

    ApiwareTokenConfig tokenConfig = new ApiwareTokenConfig("http://localhost:5772/token")
            .withMethod("GET")
            .withTokenField("token")
            .withTokenFieldFromJSON("data");
    String[] requiredFieldsForGenerateToken = {};
    ///
    Apiware testApiware = new Apiware(tokenConfig);

    @Override
    public Object execute(Map map) {
        try {

            //////////testBalanceInquiry
            // === 1. Create request ===
            ApiwareRequest request = new ApiwareRequest(BASE_API_URL + "balance_inquiry")
                    .withMethod("POST")
                    .withBodyType("form-data")
                    .withBodyParam("transaction_id", transactionId)
                    .withBodyParam("channel_id", "TBA")
                    .withBodyParam("partner_reference_no", "TBA")
                    .withBodyParam("bank_card_token", "TBA")
                    .withBodyParam("account_no", "2000100101");

            // === 2. Send ===
            ApiwareResponse response;

            response = testApiware.send(request, requiredFieldsForGenerateToken);
            
            debugMessage("with Token : "+testApiware.getCurrentToken()+"\n");
            debugMessage("BALANCE RESPONSE (testBalanceInquiry):\n" + response.getBody());

            ////////////// testAccountInquiryInternal
            ApiwareRequest requestAccountInquiryInternal = new ApiwareRequest(BASE_API_URL + "account_inquiry_internal")
                    .withMethod("POST")
                    .withBodyType("form-data")
                    .withBodyParam("transaction_id", transactionId)
                    .withBodyParam("channel_id", "TBA")
                    .withBodyParam("partner_reference_no", "2020102900000000000001")
                    .withBodyParam("beneficiary_account_no", "2000100101");

            ApiwareResponse responseAccountInquiryInternal = testApiware.send(requestAccountInquiryInternal, requiredFieldsForGenerateToken);
            debugMessage("INQUIRY RESPONSE (testAccountInquiryInternal):\n" + responseAccountInquiryInternal.getBody());

            //////testAccountInquiryExternal
            ApiwareRequest requestAccountInquiryExternal = new ApiwareRequest(BASE_API_URL + "account_inquiry_external")
                    .withMethod("POST")
                    .withBodyType("form-data")
                    .withBodyParam("transaction_id", transactionId)
                    .withBodyParam("channel_id", "TBA")
                    .withBodyParam("partner_reference_no", "2020102900000000000001")
                    .withBodyParam("beneficiary_account_no", "8000800808")
                    .withBodyParam("beneficiary_bank_code", "008");

            ApiwareResponse responseAccountInquiryExternal = testApiware.send(requestAccountInquiryExternal, requiredFieldsForGenerateToken);
            debugMessage("with Token : "+testApiware.getCurrentToken()+"\n");
            debugMessage("RESPONSE:\n" + responseAccountInquiryExternal.getBody());

            ///////////testBankStatement
            ApiwareRequest requestBankStatement = new ApiwareRequest(BASE_API_URL + "bank_statement")
                    .withMethod("POST")
                    .withBodyType("form-data")
                    .withBodyParam("transaction_id", transactionId)
                    .withBodyParam("channel_id", "TBA")
                    .withBodyParam("partner_reference_no", "TBA")
                    .withBodyParam("bank_card_token", "6d7963617264746f6b656e")
                    .withBodyParam("account_no", "2000200202");

            ApiwareResponse responseBankStatement = testApiware.send(requestBankStatement, requiredFieldsForGenerateToken);

            debugMessage("with Token : "+testApiware.getCurrentToken()+"\n");
            debugMessage("RESPONSE testBankStatement:\n" + responseBankStatement.getBody());

            ////////testTransferIntrabank
            // === 1. Create request ===
            ApiwareRequest requestTransferIntrabank = new ApiwareRequest(BASE_API_URL + "transfer_intrabank")
                    .withMethod("POST")
                    .withBodyType("form-data")
                    .withBodyParam("transaction_id", transactionId)
                    .withBodyParam("channel_id", "TBA")
                    .withBodyParam("partner_reference_no", "TBA")
                    .withBodyParam("amount", "5000.00")
                    .withBodyParam("currency", "USD")
                    .withBodyParam("beneficiary_account_no", "2000100101")
                    .withBodyParam("beneficiary_email", "test@mail.com")
                    .withBodyParam("customer_reference", "10052019")
                    .withBodyParam("fee_type", "OUR")
                    .withBodyParam("remark", "remark test")
                    .withBodyParam("source_account_no", "2000200202")
                    .withBodyParam("originator_customer_no", "999901000003300;12345")
                    .withBodyParam("originator_customer_name", "Customer1;Customer2")
                    .withBodyParam("originator_bank_code", "001;002");

            ApiwareResponse responseTransferIntrabank = testApiware.send(requestTransferIntrabank, requiredFieldsForGenerateToken);
            debugMessage("with Token : "+testApiware.getCurrentToken()+"\n");
            debugMessage("RESPONSE testTransferIntrabank:\n" + responseTransferIntrabank.getBody());

            ///////////////testTransferInterbank
            // === 1. Create request ===
            ApiwareRequest requestTransferInterbank = new ApiwareRequest(BASE_API_URL + "transfer_interbank")
                    .withMethod("POST")
                    .withBodyType("form-data")
                    .withBodyParam("transaction_id", transactionId)
                    .withBodyParam("channel_id", "TBA")
                    .withBodyParam("partner_reference_no", "2020102900000000000001")
                    .withBodyParam("amount", "5000.00")
                    .withBodyParam("currency", "USD")
                    .withBodyParam("beneficiary_account_name", "Yories Yolanda")
                    .withBodyParam("beneficiary_account_no", "8000800808")
                    .withBodyParam("beneficiary_address", "Jakarta")
                    .withBodyParam("beneficiary_bank_code", "008")
                    .withBodyParam("beneficiary_bank_name", "Bank BRI")
                    .withBodyParam("beneficiary_email", "test@mail.com")
                    .withBodyParam("customer_reference", "10052019")
                    .withBodyParam("fee_type", "OUR")
                    .withBodyParam("remark", "remark test")
                    .withBodyParam("source_account_no", "2000200202")
                    .withBodyParam("originator_customer_no", "999901000003300;12345")
                    .withBodyParam("originator_customer_name", "Customer1;Customer2")
                    .withBodyParam("originator_bank_code", "001;002");

            ApiwareResponse responseTransferInterbank = testApiware.send(requestTransferInterbank, requiredFieldsForGenerateToken);
            debugMessage("RESPONSE testTransferInterbank:\n" + responseTransferInterbank.getBody());

            ////////testTransferSKN
            // === 1. Create request ===
            ApiwareRequest requestTransferSKN = new ApiwareRequest(BASE_API_URL + "transfer_skn")
                    .withMethod("POST")
                    .withBodyType("form-data")
                    .withBodyParam("transaction_id", transactionId)
                    .withBodyParam("channel_id", "TBA")
                    .withBodyParam("partner_reference_no", "2020102900000000000001")
                    .withBodyParam("amount", "5000.00")
                    .withBodyParam("currency", "USD")
                    .withBodyParam("beneficiary_account_name", "Yories Yolanda")
                    .withBodyParam("beneficiary_account_no", "8000800808")
                    .withBodyParam("beneficiary_address", "Jakarta")
                    .withBodyParam("beneficiary_bank_code", "008")
                    .withBodyParam("beneficiary_bank_name", "Bank BRI")
                    .withBodyParam("beneficiary_customer_residence", "1")
                    .withBodyParam("beneficiary_customer_type", "1")
                    .withBodyParam("beneficiary_email", "test@mail.com")
                    .withBodyParam("customer_reference", "10052019")
                    .withBodyParam("fee_type", "OUR")
                    .withBodyParam("kodepos", "12345")
                    .withBodyParam("receiver_phone", "08123456789")
                    .withBodyParam("remark", "remark test")
                    .withBodyParam("sender_customer_residence", "1")
                    .withBodyParam("sender_customer_type", "1")
                    .withBodyParam("sender_phone", "6289656430684")
                    .withBodyParam("source_account_no", "2000200202")
                    .withBodyParam("originator_customer_no", "999901000003300;12345")
                    .withBodyParam("originator_customer_name", "Customer1;Customer2")
                    .withBodyParam("originator_bank_code", "001;002");

            ApiwareResponse responseTransferSKN = testApiware.send(requestTransferSKN, requiredFieldsForGenerateToken);
            debugMessage("with Token : "+testApiware.getCurrentToken()+"\n");
            debugMessage("RESPONSE testTransferSKN:\n" + responseTransferSKN.getBody());

            /////testTransferRTGS
            // === 1. Create request ===
            ApiwareRequest requestTransferRTGS = new ApiwareRequest(BASE_API_URL + "transfer_rtgs")
                    .withMethod("POST")
                    .withBodyType("form-data")
                    .withBodyParam("transaction_id", transactionId)
                    .withBodyParam("channel_id", "TBA")
                    .withBodyParam("partner_reference_no", "2020102900000000000001")
                    .withBodyParam("amount", "5000.00")
                    .withBodyParam("currency", "USD")
                    .withBodyParam("beneficiary_account_name", "Yories Yolanda")
                    .withBodyParam("beneficiary_account_no", "8000800808")
                    .withBodyParam("beneficiary_address", "Jakarta")
                    .withBodyParam("beneficiary_bank_code", "008")
                    .withBodyParam("beneficiary_bank_name", "Bank BRI")
                    .withBodyParam("beneficiary_customer_residence", "1")
                    .withBodyParam("beneficiary_customer_type", "1")
                    .withBodyParam("beneficiary_email", "test@mail.com")
                    .withBodyParam("customer_reference", "10052019")
                    .withBodyParam("fee_type", "OUR")
                    .withBodyParam("kodepos", "12345")
                    .withBodyParam("receiver_phone", "08123456789")
                    .withBodyParam("remark", "remark test")
                    .withBodyParam("sender_customer_residence", "1")
                    .withBodyParam("sender_customer_type", "1")
                    .withBodyParam("sender_phone", "6289656430684")
                    .withBodyParam("source_account_no", "2000200202")
                    .withBodyParam("originator_customer_no", "999901000003300;12345")
                    .withBodyParam("originator_customer_name", "Customer1;Customer2")
                    .withBodyParam("originator_bank_code", "001;002");

            ApiwareResponse responseTransferRTGS = testApiware.send(requestTransferRTGS, requiredFieldsForGenerateToken);
            debugMessage("with Token : "+testApiware.getCurrentToken()+"\n");
            debugMessage("RESPONSE testTransferRTGS:\n" + responseTransferRTGS.getBody());

            ///////testTransactionStatus
            // === 1. Create request ===
            ApiwareRequest requestTransactionStatus = new ApiwareRequest(BASE_API_URL + "transaction_status")
                    .withMethod("POST")
                    .withBodyType("form-data")
                    .withBodyParam("transaction_id", transactionId)
                    .withBodyParam("channel_id", "TBA")
                    .withBodyParam("partner_reference_no", "2020102900000000000001")
                    .withBodyParam("reference_no", "2020102977770000000009")
                    .withBodyParam("external_id", "30443786930722726463280097920912")
                    .withBodyParam("service_code", "17")
                    .withBodyParam("amount", "5000.00")
                    .withBodyParam("currency", "USD");

            ApiwareResponse responseTransactionStatus = testApiware.send(requestTransactionStatus, requiredFieldsForGenerateToken);
            debugMessage("with Token : "+testApiware.getCurrentToken()+"\n");
            debugMessage("RESPONSE testTransactionStatus :\n" + responseTransactionStatus.getBody());
        } catch (Exception ex) {
            LogUtil.debug(TestMCMAPIFunctionCall.class.getName(), ex.getMessage());
        }
        return null;
    }

    public String getName() {
        return pluginName;
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return pluginName;
    }

    public String getLabel() {
        return pluginName;
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }

    private void debugMessage(String message) {
        boolean debug = true;
        if (debug) {
            LogUtil.info("" + TestMCMAPIFunctionCall.class.getName(), "DEBUG MODE: " + message);
        }
    }

}
