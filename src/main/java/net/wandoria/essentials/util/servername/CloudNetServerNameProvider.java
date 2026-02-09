package net.wandoria.essentials.util.servername;

import dev.derklaro.aerogel.Injector;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.wrapper.holder.ServiceInfoHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.wandoria.essentials.environment.name.ServerNameProvider;

/**
 * Uses the cloudnet api to retrieve the server name.
 */
@Slf4j
@Getter
class CloudNetServerNameProvider implements ServerNameProvider {
    private final InjectionLayer<Injector> injector = InjectionLayer.ext();
    private final String serverName;

    public CloudNetServerNameProvider() {
        this.serverName = getWrapperServiceInfoHolder().serviceInfo().serviceId().name();
        log.info("Detected server name: {}", serverName);
    }

    public ServiceInfoHolder getWrapperServiceInfoHolder() {
        return injector.instance(ServiceInfoHolder.class);
    }


    public static boolean isAvailable() {
        try {
            Class.forName("eu.cloudnetservice.wrapper.holder.ServiceInfoHolder");
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            return false;
        }
        return true;
    }

}
