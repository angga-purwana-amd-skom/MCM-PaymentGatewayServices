package id.co.itasoft.pelni.mcm.plugin;

import org.joget.api.annotations.Operation;
import org.joget.api.annotations.Param;
import org.joget.api.annotations.Response;
import org.joget.api.annotations.Responses;
import org.joget.api.model.ApiPluginAbstract;
import org.joget.api.model.ApiResponse;
import org.joget.commons.util.LogUtil;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.plugin.base.PluginManager;
import org.json.JSONObject;
import java.util.Map;
import javax.sql.DataSource;
import java.sql.*;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.model.EnvironmentVariable;
import org.springframework.context.ApplicationContext;
import java.util.ArrayList;
import java.util.Collection;
import org.joget.apps.app.service.AppUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import java.util.*;
import org.joget.workflow.model.WorkflowProcess;
import java.util.regex.Pattern;

public class EbillPaymentOTP extends ApiPluginAbstract {

    public static final String PLUGIN_NAME = "EBILL - Payment OTP";
    private static final String TABLE = "app_fd_ebill_payment_req";

    // Kebijakan
    private static long TTL_MS = 5L * 60_000L;         // OTP expire 5 menit
    private static final long WINDOW_MS = 15L * 60_000L;     // Window resend 15 menit
    private static long COOLDOWN_MS = 15L * 60_000L;   // Lock resend 15 menit
    private static final long MIN_INTERVAL_MS = 2L * 60_000L; // Jeda minimal antar kirim 2 menit
    private static final int RESEND_LIMIT = 3;                // Max 3x di dalam window

    private final SecureRandom rnd = new SecureRandom();

    ///
    private static final int MAX_ERROR_ATTEMPTS = 3;
    private static final Pattern OTP_PATTERN = Pattern.compile("^[A-Za-z0-9]{6}$");

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-vial\"></i>";
    }

    @Override
    public String getTag() {
        return "ebill-otp";
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getVersion() {
        return "1.1.0";
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

    private static List<String> findActiveAssigneeEmailsByFormRecordId(DataSource ds, String formRecordId) throws SQLException {
        if (formRecordId == null || formRecordId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        final String sql
                = "SELECT DISTINCT du.email "
                + "FROM wf_process_link wpl "
                + "JOIN SHKActivities sact ON sact.ProcessId = wpl.processId "
                + "JOIN SHKActivityStates ssta ON ssta.oid = sact.State "
                + "JOIN SHKAssignmentsTable sass ON sass.ActivityId = sact.Id "
                + "JOIN dir_user du ON du.username = sass.ResourceId "
                + "WHERE wpl.originProcessId = ? "
                + "  AND (ssta.KeyValue LIKE 'open.%' OR ssta.KeyValue = 'open')";

        try (Connection con = ds.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, formRecordId);
            try (ResultSet rs = ps.executeQuery()) {
                LinkedHashSet<String> out = new LinkedHashSet<>();
                while (rs.next()) {
                    String email = rs.getString(1);
                    if (email != null && !email.trim().isEmpty()) {
                        out.add(email.trim());
                    }
                }
                return new ArrayList<>(out);
            }
        }
    }

    private static List<String> getAllEmailsByCurrentUsername(DataSource ds) throws SQLException {
        List<String> emails = new ArrayList<>();
        String username = WorkflowUtil.getCurrentUsername();

        if (username == null || username.isEmpty()) {
            LogUtil.warn("EBILL-OTP", "Current username kosong; skip deactivation.");
            return emails; // return empty list
        }

        // Example query: get emails of users related to a record or process
        String sql = "SELECT u.email "
                + "FROM dir_user u "
                + "WHERE u.username = ? "
                + "AND u.email IS NOT NULL "
                + "AND u.email <> ''";

        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    emails.add(rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            LogUtil.error("EBILL-OTP", e, "Gagal mendapatkan email user: " + username);
            throw e;
        }

        // Log result
        LogUtil.info("EBILL-OTP", "getAllEmailsByCurrentUsername -> username=" + username + ", total=" + emails.size());

        return emails;
    }

    @Operation(
            path = "/resend_otp",
            type = Operation.MethodType.POST,
            summary = "Generate & Resend OTP via Email",
            description = "Resend OTP dengan limitasi waktu & counter"
    )
    @Responses({
        @Response(responseCode = 200, description = "OK"),
        @Response(responseCode = 400, description = "Bad Request"),
        @Response(responseCode = 404, description = "Not Found"),
        @Response(responseCode = 500, description = "Internal Error")
    })
    public ApiResponse resendOtp(
            @Param(value = "record_id", description = "Primary Key form record") String recordId
    ) {
        try {
            if (recordId == null || recordId.trim().isEmpty()) {
                return new ApiResponse(400, error("Missing 'record_id'"));
            }

            long now = System.currentTimeMillis();

            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            ApplicationContext appContext = AppUtil.getApplicationContext();
            EnvironmentVariableDao environmentVariableDao = (EnvironmentVariableDao) appContext.getBean("environmentVariableDao");
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();

            String str_otp_expire_time = "";
            String str_otp_resend_cooldown_time = "";
            Collection<EnvironmentVariable> environmentVariableList = environmentVariableDao.getEnvironmentVariableList(null, appDef, null, null, null, null);
            // Iterasi melalui daftar untuk menemukan variabel yang diinginkan
            if (environmentVariableList != null && !environmentVariableList.isEmpty()) {
                int varFoundCount = 0;
                for (EnvironmentVariable envVar : environmentVariableList) {
                    if (envVar.getId().equals("otp_expire_time")) {
                        str_otp_expire_time = envVar.getValue();
                        varFoundCount++;
                        if (varFoundCount >= 2) {
                            break;
                        }
                    }
                    if (envVar.getId().equals("otp_resend_cooldown_time")) {
                        str_otp_resend_cooldown_time = envVar.getValue();
                        varFoundCount++;
                        if (varFoundCount >= 2) {
                            break;
                        }
                    }
                }
            }
            try (Connection con = ds.getConnection()) {

                // Ambil state terkini
                String selectSql
                        = "SELECT c_otp_status, c_otp_data, c_otp_expiry_at, c_otp_last_sent_time, "
                        + "       c_otp_resend_count, c_otp_error_count, c_otp_first_sent_time, "
                        + "       c_otp_resend_inactive_until, c_otp_participant_id "
                        + "FROM " + TABLE + " WHERE id = ?";
                String status;
                String otpData;
                long expiryAt, lastSent, firstSent, inactiveUntil;
                int resendCount, errorCount;
                String participantId;

                try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                    ps.setString(1, recordId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            return new ApiResponse(404, error("Record not found"));
                        }

                        status = nz(rs.getString(1), "");
                        otpData = rs.getString(2);
                        expiryAt = toLong(rs.getString(3), 0L);
                        lastSent = toLong(rs.getString(4), 0L);
                        resendCount = toInt(rs.getString(5), 0);
                        errorCount = toInt(rs.getString(6), 0);
                        firstSent = toLong(rs.getString(7), 0L);
                        inactiveUntil = toLong(rs.getString(8), 0L);
                        participantId = rs.getString(9);
                    }
                }

                // 1) Cek inaktif (cooldown)
                if (inactiveUntil > now) {
                    long sisa = (inactiveUntil - now) / 1000L;
                    return new ApiResponse(200, fail("RESEND_INACTIVE", "Resend OTP sedang tidak aktif. Coba lagi "
                            + "dalam " + sisa + " detik."));
                }

                // 2) Jeda minimal 2 menit dari lastSent
                if (lastSent > 0L && (now - lastSent) <= MIN_INTERVAL_MS) {
                    return new ApiResponse(200, fail("TOO_FREQUENT",
                            "Terlalu sering. Minimal jeda 2 menit antar kirim."));
                }

                // 3) Window 15 menit & counter
                if (firstSent == 0L || (now - firstSent) > WINDOW_MS) {
                    // Reset window: mulai yang baru, hitung percobaan ini sebagai 1
                    firstSent = now;
                    resendCount = 1;
                } else {
                    // Masih dalam window
                    if (resendCount >= RESEND_LIMIT) {
                        if (str_otp_resend_cooldown_time != null && !str_otp_resend_cooldown_time.isEmpty()) {
                            long cooldownMinutes = Long.parseLong(str_otp_resend_cooldown_time);
                            COOLDOWN_MS = cooldownMinutes * 60_000L;
                        }
                        // Lock resend colldown menit ke depan
                        long until = now + COOLDOWN_MS;
                        String lockSql
                                = "UPDATE " + TABLE + " SET c_otp_resend_inactive_until=?, c_otp_status=? WHERE id=?";
                        try (PreparedStatement ps = con.prepareStatement(lockSql)) {
                            ps.setString(1, String.valueOf(until));
                            ps.setString(2, "INVALID"); // opsional: tandai invalid saat cooldown
                            ps.setString(3, recordId);
                            ps.executeUpdate();
                        }
                        return new ApiResponse(200, fail("RESEND_LIMIT_REACHED",
                                "Batas resend 3x dalam 15 menit tercapai. Resend dinonaktifkan 15 menit."));
                    } else {
                        resendCount += 1;
                    }
                }

                // 4) Generate OTP baru + hash & set expiry 5 menit
                String otpPlain = genOtp(6);
                String otpHash = sha256B64(otpPlain);

                if (str_otp_expire_time != null && !str_otp_expire_time.isEmpty()) {
                    long expireMinutes = Long.parseLong(str_otp_expire_time);
                    TTL_MS = expireMinutes * 60_000L;
                }

                long expiryNew = now + TTL_MS;

                // 5) Update DB
                String updateSql
                        = "UPDATE " + TABLE + " "
                        + "SET c_otp_data=?, c_otp_expiry_at=?, c_otp_last_sent_time=?, "
                        + "    c_otp_status=?, c_otp_error_count=?, c_otp_resend_count=?, "
                        + "    c_otp_first_sent_time=?, c_otp_resend_inactive_until=? "
                        + "WHERE id=?";
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setString(1, otpHash);
                    ps.setString(2, String.valueOf(expiryNew));
                    ps.setString(3, String.valueOf(now));
                    ps.setString(4, "ACTIVE");
                    ps.setString(5, "0");
                    ps.setString(6, String.valueOf(resendCount));
                    ps.setString(7, String.valueOf(firstSent));
                    ps.setString(8, "0"); // aktif kembali
                    ps.setString(9, recordId);
                    ps.executeUpdate();
                }

                // 6) Kirim email via sub-modul
                String subject = "[OTP] Kode verifikasi Anda";

                ZonedDateTime nowEmail = ZonedDateTime.now();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm:ss (z,XXX)", new Locale("id", "ID"));
                String formattedNow = nowEmail.format(fmt);

                LogUtil.info(getClass().getName(), "Tanggal sekarang: " + formattedNow);

                String body = "<html lang=\"id\">\n"
                        + "<head>\n"
                        + "    <meta charset=\"UTF-8\">\n"
                        + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                        + "    <title>Notifikasi OTP</title>\n"
                        + "    <style>\n"
                        + "        body {\n"
                        + "            font-family: Arial, sans-serif;\n"
                        + "            text-align: center;\n"
                        + "            padding: 20px;\n"
                        + "            background-color: #f8f9fa;\n"
                        + "        }\n"
                        + "        .container {\n"
                        + "            max-width: 400px;\n"
                        + "            background: white;\n"
                        + "            padding: 20px;\n"
                        + "            border-radius: 8px;\n"
                        + "            box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);\n"
                        + "            margin: auto;\n"
                        + "        }\n"
                        + "        h1 {\n"
                        + "            color: #333;\n"
                        + "            font-size: 24px;\n"
                        + "        }\n"
                        + "        .otp {\n"
                        + "            font-size: 22px;\n"
                        + "            font-weight: bold;\n"
                        + "            color: #d9534f;\n"
                        + "        }\n"
                        + "        .important {\n"
                        + "            font-weight: bold;\n"
                        + "        }\n"
                        + "        .footer {\n"
                        + "            font-size: 14px;\n"
                        + "            color: #777;\n"
                        + "            margin-top: 20px;\n"
                        + "        }\n"
                        + "    </style>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "    <div class=\"container\">\n"
                        + "        <h1>Halo,</h1>\n"
                        + "        <p>Untuk melanjutkan transaksi, silakan gunakan kode OTP berikut:</p>\n"
                        + "        <p class=\"otp\">" + otpPlain + "</p>\n"
                        + "        <p>Kode ini berlaku selama <span class=\"important\">5 menit</span>. Mohon untuk tidak membagikan kode ini kepada siapa pun, termasuk pihak Pelni.</p>\n"
                        + "        <p><span class=\"important\">Tanggal/Jam:</span> " + formattedNow + "</p>\n"
                        + "        <p class=\"footer\">Email ini dikirim secara otomatis, mohon untuk tidak membalas.<br>\n"
                        + "        Jika Anda tidak merasa melakukan transaksi ini, abaikan pemberitahuan ini.</p>\n"
                        + "        <p class=\"footer\" style=\"font-size: 16px;\"><strong>Terima kasih,</strong><br>\n"
                        + "        Pelni</p>\n"
                        + "    </div>\n"
                        + "</body>\n"
                        + "</html>";

                LogUtil.info(getClass().getName(), "Kode OTP Anda: " + otpPlain);

                // recordId kamu = processId
                List<String> emails = findActiveAssigneeEmailsByFormRecordId(ds, recordId);

                if (emails.isEmpty()) {
                    LogUtil.warn(getClass().getName(), "[OTP] Tidak ada assignee aktif untuk processId=" + recordId);
                    return new ApiResponse(500, error("Internal server error"));
                } else {
                    for (String e : emails) {
                        LogUtil.info(getClass().getName(), "Participant email = " + e);
                        sendOtpEmail(e, subject, body);
                    }
                }

                // 7) JSON sukses
                JSONObject ok = new JSONObject();
                ok.put("success", true);
                ok.put("message", "OTP terkirim");
                ok.put("resend_count", resendCount);
                ok.put("window_first_sent_at", firstSent);
                ok.put("expires_at", expiryNew);
                return new ApiResponse(200, ok);
            } catch (SQLException e) {
                LogUtil.error(getClass().getName(), e, "Resend OTP error: " + e.getMessage());
                return new ApiResponse(500, error("Internal server error"));
            } catch (NumberFormatException e) {
                LogUtil.error(getClass().getName(), e, "Resend OTP error: " + e.getMessage());
                return new ApiResponse(500, error("Internal server error"));
            }

        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Resend OTP error: " + e.getMessage());
            return new ApiResponse(500, error("Internal server error"));
        }
    }

    @Operation(
            path = "/extend_expired_otp",
            type = Operation.MethodType.POST,
            summary = "Extend Expired OTP",
            description = "force Extend Expired OTP"
    )
    @Responses({
        @Response(responseCode = 200, description = "OK"),
        @Response(responseCode = 400, description = "Bad Request"),
        @Response(responseCode = 404, description = "Not Found"),
        @Response(responseCode = 500, description = "Internal Error")
    })
    public ApiResponse extendExpiredOtp(
            @Param(value = "record_id", description = "Primary Key form record") String recordId
    ) {
        try {
            if (recordId == null || recordId.trim().isEmpty()) {
                return new ApiResponse(400, error("Missing record_id"));
            }

            long now = System.currentTimeMillis();

            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            ApplicationContext appContext = AppUtil.getApplicationContext();
            EnvironmentVariableDao environmentVariableDao = (EnvironmentVariableDao) appContext.getBean("environmentVariableDao");
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();

            String str_otp_expire_time = "";
            Collection<EnvironmentVariable> environmentVariableList = environmentVariableDao.getEnvironmentVariableList(null, appDef, null, null, null, null);
            // Iterasi melalui daftar untuk menemukan variabel yang diinginkan
            if (environmentVariableList != null && !environmentVariableList.isEmpty()) {

                for (EnvironmentVariable envVar : environmentVariableList) {
                    if (envVar.getId().equals("otp_expire_time")) {
                        str_otp_expire_time = envVar.getValue();
                        break;
                    }
                }
            }
            try (Connection con = ds.getConnection()) {

                String status;
                String otpData;
                long firstSent;
                int resendCount;

                // Reset window: mulai yang baru, hitung percobaan ini sebagai 1
                firstSent = now;
                resendCount = 1;

                // 4) Generate OTP baru + hash & set expiry 5 menit
                //String otpPlain = genOtp(6);
                //String otpHash = sha256B64(otpPlain);
                if (str_otp_expire_time != null && !str_otp_expire_time.isEmpty()) {
                    long expireMinutes = Long.parseLong(str_otp_expire_time);
                    TTL_MS = expireMinutes * 60_000L;
                }

                long expiryNew = now + TTL_MS;

                // 5) Update DB
                String updateSql
                        = "UPDATE " + TABLE + " "
                        + "SET c_otp_expiry_at=?, c_otp_last_sent_time=?, "
                        + "    c_otp_status=?, c_otp_error_count=?, c_otp_resend_count=?, "
                        + "    c_otp_first_sent_time=?, c_otp_resend_inactive_until=? "
                        + "WHERE id=?";
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setString(1, String.valueOf(expiryNew));
                    ps.setString(2, String.valueOf(now));
                    ps.setString(3, "ACTIVE");
                    ps.setString(4, "0");
                    ps.setString(5, String.valueOf(resendCount));
                    ps.setString(6, String.valueOf(firstSent));
                    ps.setString(7, "0"); // aktif kembali
                    ps.setString(8, recordId);
                    ps.executeUpdate();
                }

                // 7) JSON sukses
                JSONObject ok = new JSONObject();
                ok.put("success", true);
                ok.put("message", "OTP terkirim");
                ok.put("resend_count", resendCount);
                ok.put("window_first_sent_at", firstSent);
                ok.put("expires_at", expiryNew);
                return new ApiResponse(200, ok);
            } catch (SQLException e) {
                LogUtil.error(getClass().getName(), e, "Resend OTP error: " + e.getMessage());
                return new ApiResponse(500, error("Internal server error"));
            } catch (NumberFormatException e) {
                LogUtil.error(getClass().getName(), e, "Resend OTP error: " + e.getMessage());
                return new ApiResponse(500, error("Internal server error"));
            }

        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Resend OTP error: " + e.getMessage());
            return new ApiResponse(500, error("Internal server error"));
        }
    }

    @Operation(
            path = "/validate_otp_on_payment_release",
            type = Operation.MethodType.POST,
            summary = "Validate OTP on Payment Release",
            description = "Validate OTP for app_fd_ebill_payment_req with lock and user deactivation logic"
    )
    @Responses({
        @Response(responseCode = 200, description = "OK"),
        @Response(responseCode = 400, description = "Bad Request"),
        @Response(responseCode = 404, description = "Not Found"),
        @Response(responseCode = 500, description = "Internal Error")
    })
    public ApiResponse validateOTPOnPaymentRelease(
            @Param(value = "record_id", description = "Primary Key of the form record") String recordId,
            @Param(value = "otp", description = "User input OTP (6 alphanumeric chars)") String inputOtp
    ) {
        long nowMs = System.currentTimeMillis();

        try {
            // --- Basic input checks
            if (recordId == null || recordId.trim().isEmpty()) {
                return new ApiResponse(400, error("Missing 'record_id'"));
            }
            if (inputOtp == null || inputOtp.trim().isEmpty()) {
                return new ApiResponse(400, fail("OTP_REQUIRED", "Kode OTP wajib diisi."));
            }
            inputOtp = inputOtp.trim();
            if (!OTP_PATTERN.matcher(inputOtp).matches()) {
                return new ApiResponse(400, fail("OTP_FORMAT", "Format OTP tidak valid. Gunakan 6 karakter alfanumerik."));
            }

            LogUtil.info(getClass().getName(), "recordId : " + recordId + ", inputOtp : " + inputOtp);

            // --- DB setup
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");

            // --- Load OTP row
            String status;
            String storedOtpHashB64;
            long expiryAt;
            int errorCount;

            try (Connection con = ds.getConnection()) {
                // 1) Select current OTP data
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT c_otp_status, c_otp_data, c_otp_expiry_at, c_otp_error_count "
                        + "FROM " + TABLE + " WHERE c_no_payment = ?")) {
                    ps.setString(1, recordId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            return new ApiResponse(404, fail("NOT_FOUND", "Data OTP tidak ditemukan."));
                        }
                        status = nz(rs.getString("c_otp_status"), "");
                        storedOtpHashB64 = rs.getString("c_otp_data");
                        expiryAt = toLong(rs.getString("c_otp_expiry_at"), 0L);
                        errorCount = toInt(rs.getString("c_otp_error_count"), 0);

                        LogUtil.info(getClass().getName(), "c_otp_status : " + status + ", c_otp_data : " + storedOtpHashB64 + ", c_otp_expiry_at : " + expiryAt
                                + "\n, c_otp_error_count : " + errorCount);
                    }
                }

                // 2) Locks & expiry checks (same semantics as BeanShell)
                if ("INVALID".equalsIgnoreCase(status)) {
                    return new ApiResponse(200, fail("LOCKED", "Akun OTP terkunci. Silakan hubungi admin."));
                }

                if ("EXPIRED".equalsIgnoreCase(status) || (expiryAt > 0 && nowMs > expiryAt)) {
                    // mark expired (idempotent)
                    LinkedHashMap<String, String> m = new LinkedHashMap<>();
                    m.put("otp_status", "EXPIRED");
                    doPartialUpdate(con, "ebill_payment_req", recordId, m);

                    return new ApiResponse(200, fail("EXPIRED", "OTP telah kedaluwarsa. Silakan minta kode baru."));
                }

                if (storedOtpHashB64 == null || storedOtpHashB64.trim().isEmpty()) {
                    return new ApiResponse(200, fail("NOT_ISSUED", "Kode OTP belum diterbitkan."));
                }

                // 3) Validate hash match
                boolean match;
                try {
                    match = storedOtpHashB64.equals(sha256B64(inputOtp));
                } catch (Exception e) {
                    LogUtil.error(getClass().getName(), e, "Gagal menghitung hash SHA-256.");
                    return new ApiResponse(500, error("Kesalahan validasi OTP."));
                }

                if (!match) {
                    errorCount++;
                    LinkedHashMap<String, String> m = new LinkedHashMap<>();
                    m.put("otp_error_count", String.valueOf(errorCount));
                    if (errorCount >= MAX_ERROR_ATTEMPTS) {
                        m.put("otp_status", "INVALID");
                    }
                    doPartialUpdate(con, "ebill_payment_req", recordId, m);

                    if (errorCount >= MAX_ERROR_ATTEMPTS) {
                        // Deactivate current user unless ROLE_ADMIN
                        deactivateCurrentUserIfNotAdmin(con);
                        JSONObject body = fail("LOCKED", "OTP salah 3 kali. Akun Anda dinonaktifkan. Hubungi admin.");
                        body.put("attempts", errorCount);
                        return new ApiResponse(200, body);
                    } else {
                        int sisa = MAX_ERROR_ATTEMPTS - errorCount;
                        JSONObject body = fail("OTP_MISMATCH", "OTP salah. Kesempatan tersisa: " + sisa + " kali.");
                        body.put("attempts", errorCount);
                        return new ApiResponse(200, body);
                    }
                }

                // 4) Match -> success
                LinkedHashMap<String, String> ok = new LinkedHashMap<>();
                ok.put("otp_status", "VALID");
                ok.put("otp_error_count", "0");
                doPartialUpdate(con, "ebill_payment_req", recordId, ok);

                JSONObject res = new JSONObject();
                res.put("success", true);
                res.put("message", "OTP valid.");
                res.put("status", "VALID");
                res.put("record_id", recordId);
                return new ApiResponse(200, res);
            }
        } catch (SQLException e) {
            LogUtil.error(getClass().getName(), e, "Validate OTP error (SQL): " + e.getMessage());
            return new ApiResponse(500, error("Internal server error"));
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Validate OTP error: " + e.getMessage());
            return new ApiResponse(500, error("Internal server error"));
        }
    }

    @Operation(
            path = "/resend_otp_on_release_payment",
            type = Operation.MethodType.POST,
            summary = "Generate & Resend OTP via Email on Release",
            description = "Resend OTP dengan limitasi waktu & counter on Release"
    )
    @Responses({
        @Response(responseCode = 200, description = "OK"),
        @Response(responseCode = 400, description = "Bad Request"),
        @Response(responseCode = 404, description = "Not Found"),
        @Response(responseCode = 500, description = "Internal Error")
    })
    public ApiResponse resendOtpOnReleasePayment(
            @Param(value = "record_id", description = "Primary Key form record") String recordId
    ) {
        try {
            if (recordId == null || recordId.trim().isEmpty()) {
                return new ApiResponse(400, error("Missing 'record_id'"));
            }

            long now = System.currentTimeMillis();

            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            ApplicationContext appContext = AppUtil.getApplicationContext();
            EnvironmentVariableDao environmentVariableDao = (EnvironmentVariableDao) appContext.getBean("environmentVariableDao");
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();

            String str_otp_expire_time = "";
            String str_otp_resend_cooldown_time = "";
            Collection<EnvironmentVariable> environmentVariableList = environmentVariableDao.getEnvironmentVariableList(null, appDef, null, null, null, null);
            // Iterasi melalui daftar untuk menemukan variabel yang diinginkan
            if (environmentVariableList != null && !environmentVariableList.isEmpty()) {
                int varFoundCount = 0;
                for (EnvironmentVariable envVar : environmentVariableList) {
                    if (envVar.getId().equals("otp_expire_time")) {
                        str_otp_expire_time = envVar.getValue();
                        varFoundCount++;
                        if (varFoundCount >= 2) {
                            break;
                        }
                    }
                    if (envVar.getId().equals("otp_resend_cooldown_time")) {
                        str_otp_resend_cooldown_time = envVar.getValue();
                        varFoundCount++;
                        if (varFoundCount >= 2) {
                            break;
                        }
                    }
                }
            }
            try (Connection con = ds.getConnection()) {

                // Ambil state terkini
                String selectSql
                        = "SELECT c_otp_status, c_otp_data, c_otp_expiry_at, c_otp_last_sent_time, "
                        + "       c_otp_resend_count, c_otp_error_count, c_otp_first_sent_time, "
                        + "       c_otp_resend_inactive_until "
                        + "FROM " + TABLE + " WHERE c_no_payment = ?";
                String status;
                String otpData;
                long expiryAt, lastSent, firstSent, inactiveUntil;
                int resendCount, errorCount;

                try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                    ps.setString(1, recordId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            return new ApiResponse(404, error("Record not found"));
                        }

                        status = nz(rs.getString(1), "");
                        otpData = rs.getString(2);
                        expiryAt = toLong(rs.getString(3), 0L);
                        lastSent = toLong(rs.getString(4), 0L);
                        resendCount = toInt(rs.getString(5), 0);
                        errorCount = toInt(rs.getString(6), 0);
                        firstSent = toLong(rs.getString(7), 0L);
                        inactiveUntil = toLong(rs.getString(8), 0L);

                    }
                }

                // 1) Cek inaktif (cooldown)
                if (inactiveUntil > now) {
                    long sisa = (inactiveUntil - now) / 1000L;
                    return new ApiResponse(200, fail("RESEND_INACTIVE", "Resend OTP sedang tidak aktif. Coba lagi "
                            + "dalam " + sisa + " detik."));
                }

                // 2) Jeda minimal 2 menit dari lastSent
                if (lastSent > 0L && (now - lastSent) <= MIN_INTERVAL_MS) {
                    return new ApiResponse(200, fail("TOO_FREQUENT",
                            "Terlalu sering. Minimal jeda 2 menit antar kirim."));
                }

                // 3) Window 15 menit & counter
                if (firstSent == 0L || (now - firstSent) > WINDOW_MS) {
                    // Reset window: mulai yang baru, hitung percobaan ini sebagai 1
                    firstSent = now;
                    resendCount = 1;
                } else {
                    // Masih dalam window
                    if (resendCount >= RESEND_LIMIT) {
                        if (str_otp_resend_cooldown_time != null && !str_otp_resend_cooldown_time.isEmpty()) {
                            long cooldownMinutes = Long.parseLong(str_otp_resend_cooldown_time);
                            COOLDOWN_MS = cooldownMinutes * 60_000L;
                        }
                        // Lock resend colldown menit ke depan
                        long until = now + COOLDOWN_MS;
                        String lockSql
                                = "UPDATE " + TABLE + " SET c_otp_resend_inactive_until=?, c_otp_status=? WHERE c_no_payment=?";
                        try (PreparedStatement ps = con.prepareStatement(lockSql)) {
                            ps.setString(1, String.valueOf(until));
                            ps.setString(2, "INVALID"); // opsional: tandai invalid saat cooldown
                            ps.setString(3, recordId);
                            ps.executeUpdate();
                        }
                        return new ApiResponse(200, fail("RESEND_LIMIT_REACHED",
                                "Batas resend 3x dalam 15 menit tercapai. Resend dinonaktifkan 15 menit."));
                    } else {
                        resendCount += 1;
                    }
                }

                // 4) Generate OTP baru + hash & set expiry 5 menit
                String otpPlain = genOtp(6);
                String otpHash = sha256B64(otpPlain);

                if (str_otp_expire_time != null && !str_otp_expire_time.isEmpty()) {
                    long expireMinutes = Long.parseLong(str_otp_expire_time);
                    TTL_MS = expireMinutes * 60_000L;
                }

                long expiryNew = now + TTL_MS;

                // 5) Update DB
                String updateSql
                        = "UPDATE " + TABLE + " "
                        + "SET c_otp_data=?, c_otp_expiry_at=?, c_otp_last_sent_time=?, "
                        + "    c_otp_status=?, c_otp_error_count=?, c_otp_resend_count=?, "
                        + "    c_otp_first_sent_time=?, c_otp_resend_inactive_until=? "
                        + "WHERE c_no_payment=?";
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setString(1, otpHash);
                    ps.setString(2, String.valueOf(expiryNew));
                    ps.setString(3, String.valueOf(now));
                    ps.setString(4, "ACTIVE");
                    ps.setString(5, "0");
                    ps.setString(6, String.valueOf(resendCount));
                    ps.setString(7, String.valueOf(firstSent));
                    ps.setString(8, "0"); // aktif kembali
                    ps.setString(9, recordId);
                    ps.executeUpdate();
                }

                // 6) Kirim email via sub-modul
                String subject = "[OTP] Kode verifikasi Anda";

                ZonedDateTime nowEmail = ZonedDateTime.now();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm:ss (z,XXX)", new Locale("id", "ID"));
                String formattedNow = nowEmail.format(fmt);

                LogUtil.info(getClass().getName(), "Tanggal sekarang: " + formattedNow);

                String body = "<html lang=\"id\">\n"
                        + "<head>\n"
                        + "    <meta charset=\"UTF-8\">\n"
                        + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                        + "    <title>Notifikasi OTP</title>\n"
                        + "    <style>\n"
                        + "        body {\n"
                        + "            font-family: Arial, sans-serif;\n"
                        + "            text-align: center;\n"
                        + "            padding: 20px;\n"
                        + "            background-color: #f8f9fa;\n"
                        + "        }\n"
                        + "        .container {\n"
                        + "            max-width: 400px;\n"
                        + "            background: white;\n"
                        + "            padding: 20px;\n"
                        + "            border-radius: 8px;\n"
                        + "            box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);\n"
                        + "            margin: auto;\n"
                        + "        }\n"
                        + "        h1 {\n"
                        + "            color: #333;\n"
                        + "            font-size: 24px;\n"
                        + "        }\n"
                        + "        .otp {\n"
                        + "            font-size: 22px;\n"
                        + "            font-weight: bold;\n"
                        + "            color: #d9534f;\n"
                        + "        }\n"
                        + "        .important {\n"
                        + "            font-weight: bold;\n"
                        + "        }\n"
                        + "        .footer {\n"
                        + "            font-size: 14px;\n"
                        + "            color: #777;\n"
                        + "            margin-top: 20px;\n"
                        + "        }\n"
                        + "    </style>\n"
                        + "</head>\n"
                        + "<body>\n"
                        + "    <div class=\"container\">\n"
                        + "        <h1>Halo,</h1>\n"
                        + "        <p>Untuk melanjutkan transaksi, silakan gunakan kode OTP berikut:</p>\n"
                        + "        <p class=\"otp\">" + otpPlain + "</p>\n"
                        + "        <p>Kode ini berlaku selama <span class=\"important\">5 menit</span>. Mohon untuk tidak membagikan kode ini kepada siapa pun, termasuk pihak Pelni.</p>\n"
                        + "        <p><span class=\"important\">Tanggal/Jam:</span> " + formattedNow + "</p>\n"
                        + "        <p class=\"footer\">Email ini dikirim secara otomatis, mohon untuk tidak membalas.<br>\n"
                        + "        Jika Anda tidak merasa melakukan transaksi ini, abaikan pemberitahuan ini.</p>\n"
                        + "        <p class=\"footer\" style=\"font-size: 16px;\"><strong>Terima kasih,</strong><br>\n"
                        + "        Pelni</p>\n"
                        + "    </div>\n"
                        + "</body>\n"
                        + "</html>";

                LogUtil.info(getClass().getName(), "Kode OTP Anda: " + otpPlain);

                // recordId kamu = processId
                List<String> emails = getAllEmailsByCurrentUsername(ds);

                if (emails.isEmpty()) {
                    LogUtil.warn(getClass().getName(), "[OTP] Tidak ada assignee aktif untuk processId=" + recordId);
                    return new ApiResponse(500, error("Internal server error"));
                } else {
                    for (String e : emails) {
                        LogUtil.info(getClass().getName(), "Participant OTP rilis email = " + e);
                        sendOtpEmail(e, subject, body);
                    }
                }

                // 7) JSON sukses
                JSONObject ok = new JSONObject();
                ok.put("success", true);
                ok.put("message", "OTP terkirim");
                ok.put("resend_count", resendCount);
                ok.put("window_first_sent_at", firstSent);
                ok.put("expires_at", expiryNew);
                return new ApiResponse(200, ok);
            } catch (SQLException e) {
                LogUtil.error(getClass().getName(), e, "Resend OTP Rilis error: " + e.getMessage());
                return new ApiResponse(500, error("Internal server error"));
            } catch (NumberFormatException e) {
                LogUtil.error(getClass().getName(), e, "Resend OTP Rilis error: " + e.getMessage());
                return new ApiResponse(500, error("Internal server error"));
            }

        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Resend OTP Rilis error: " + e.getMessage());
            return new ApiResponse(500, error("Internal server error"));
        }
    }

    // =========================================================
    // ================== INTERNAL HELPERS =====================
    // =========================================================
    private static void doPartialUpdate(Connection con, String tableId, String recordId, LinkedHashMap<String, String> map) throws SQLException {
        if (map == null || map.isEmpty()) {
            return;
        }

        StringBuilder sql = new StringBuilder("UPDATE app_fd_").append(tableId).append(" SET ");
        int i = 0;
        for (String k : map.keySet()) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("c_").append(k).append("=?");
            i++;
        }
        sql.append(" WHERE c_no_payment = ?");

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Map.Entry<String, String> e : map.entrySet()) {
                ps.setString(idx++, e.getValue());
            }
            ps.setString(idx, recordId);
            ps.executeUpdate();
        }
    }

    private static void deactivateCurrentUserIfNotAdmin(Connection con) {
        String username = WorkflowUtil.getCurrentUsername();
        if (username == null || username.isEmpty()) {
            LogUtil.warn("EBILL-OTP", "Current username kosong; skip deactivation.");
            return;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 1) userId
            String userId = null;
            ps = con.prepareStatement("SELECT id FROM dir_user WHERE username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                userId = rs.getString(1);
            }
            rs.close();
            rs = null;
            ps.close();
            ps = null;

            if (userId == null) {
                LogUtil.warn("EBILL-OTP", "Username tidak ditemukan di dir_user: " + username);
                return;
            }

            // 2) check ROLE_ADMIN
            int cnt = 0;
            ps = con.prepareStatement("SELECT COUNT(*) FROM dir_user_role WHERE userId = ? AND roleId = 'ROLE_ADMIN'");
            ps.setString(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                cnt = rs.getInt(1);
            }
            rs.close();
            rs = null;
            ps.close();
            ps = null;

            if (cnt > 0) {
                LogUtil.warn("EBILL-OTP", "Lewati penonaktifan: user ROLE_ADMIN (" + username + ")");
                return;
            }

            // 3) deactivate
            ps = con.prepareStatement("UPDATE dir_user SET active = 0 WHERE id = ?");
            ps.setString(1, userId);
            int upd = ps.executeUpdate();
            LogUtil.info("EBILL-OTP", "Deactivate user " + username + " => " + upd + " row(s).");
        } catch (Exception e) {
            LogUtil.error("EBILL-OTP", e, "Exception saat menonaktifkan user setelah limit OTP.");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ignore) {
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ignore) {
            }
        }
    }

    // === UTILITIES ===
    private static JSONObject error(String msg) {
        JSONObject o = new JSONObject();
        o.put("success", false);
        o.put("error", msg);
        return o;
    }

    private static JSONObject fail(String code, String msg) {
        JSONObject o = new JSONObject();
        o.put("success", false);
        o.put("code", code);
        o.put("message", msg);
        return o;
    }

    private static String nz(String v, String d) {
        return v == null ? d : v;
    }

    private static long toLong(String v, long d) {
        try {
            if (v == null || v.trim().isEmpty()) {
                return d;
            }
            return Long.parseLong(v.trim());
        } catch (Exception e) {
            return d;
        }
    }

    private static int toInt(String v, int d) {
        try {
            if (v == null || v.trim().isEmpty()) {
                return d;
            }
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            return d;
        }
    }

    private String genOtp(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static String sha256B64(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(s.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(dig);
    }

    // === Sub-modul email (lihat bagian 2) ===
    private void sendOtpEmail(String toSpecificEmail, String subject, String message) throws Exception {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();

        org.joget.plugin.base.Plugin plugin = pluginManager.getPlugin("org.joget.apps.app.lib.EmailTool");

        // Start from default props bound to this appDef
        Map<String, Object> props = AppPluginUtil.getDefaultProperties(plugin, null, appDef);
        props.put("pluginManager", pluginManager);

        // IMPORTANT: EmailTool expects the AppDefinition object, not its ID
        props.put("appDef", appDef);

        // Recipient by participant (adjust if you use direct emails)
        props.put("toSpecific", toSpecificEmail);

        // Content
        props.put("subject", subject);
        props.put("message", message);
        // If your message contains HTML, uncomment:
        props.put("isHtml", "true");

        // Execute the tool
        org.joget.plugin.base.ApplicationPlugin emailTool
                = (org.joget.plugin.base.ApplicationPlugin) plugin;
        ((org.joget.plugin.property.model.PropertyEditable) emailTool).setProperties(props);
        emailTool.execute(props);
    }
}
