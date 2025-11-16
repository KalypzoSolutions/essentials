package net.wandoria.essentials.environment.name;

import lombok.extern.slf4j.Slf4j;
import net.wandoria.essentials.util.InternalServerName;

@Slf4j

public class DefaultServerNameProvider implements ServerNameProvider {

    @Override
    public String getServerName() {
        return InternalServerName.get();
    }
}
