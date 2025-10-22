package id.co.itasoft.pelni.mcm.plugin;

import java.util.ArrayList;
import java.util.Collection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        // Register plugin here
        registrationList.add(context.registerService(MainRoot.class.getName(), new MainRoot(), null));
        registrationList.add(context.registerService(CekRekening.class.getName(), new CekRekening(), null));
        registrationList.add(context.registerService(CekSaldoAPI.class.getName(), new CekSaldoAPI(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}