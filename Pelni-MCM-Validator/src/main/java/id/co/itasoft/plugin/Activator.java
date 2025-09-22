package id.co.itasoft.plugin;

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
        // registrationList.add(context.registerService(MyPlugin.class.getName(), new
        // MyPlugin(), null));
        registrationList.add(context.registerService(CekSaldo.class.getName(), new CekSaldo(), null));
        registrationList.add(context.registerService(CekRekening.class.getName(), new CekRekening(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}