package net.wandoria.essentials.environment.name;

import dev.derklaro.aerogel.Injector;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.wrapper.holder.ServiceInfoHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class CloudNetServerNameProvider implements ServerNameProvider {
    private final InjectionLayer<Injector> injector = InjectionLayer.ext();
    private final String serverName;

    public CloudNetServerNameProvider() {
        this.serverName = getWrapperServiceInfoHolder().serviceInfo().serviceId().name();
        log.info("Detected server name: {}", serverName);
    }

    public ServiceInfoHolder getWrapperServiceInfoHolder() {
        return injector.instance(ServiceInfoHolder.class);
    }


}
