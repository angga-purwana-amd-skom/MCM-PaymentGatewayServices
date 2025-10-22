package id.co.itasoft.pelni.mcm.plugin.dao;

import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;

public class EnvironmentVariableUtil {
    private static String getValueHiddenCategory = null;

    public static String getEnvironmentVariable(String variableId) {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        EnvironmentVariableDao envVar = (EnvironmentVariableDao) AppUtil.getApplicationContext().getBean("environmentVariableDao");

        if (appDef != null && envVar != null) {
            try {
                return envVar.loadById(variableId, appDef).getValue();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static String getHiddenCategory() {
        if (getValueHiddenCategory == null) {
            getValueHiddenCategory = getEnvironmentVariable("hiddenCategory");
        }
        return getValueHiddenCategory;
    }
}