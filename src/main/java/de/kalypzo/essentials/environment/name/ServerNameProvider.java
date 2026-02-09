package de.kalypzo.essentials.environment.name;

import de.kalypzo.essentials.util.servername.InternalServerName;

/**
 * @deprecated Use {@link InternalServerName} instead. This interface is no longer used and will be removed in a future version.
 */
@Deprecated(forRemoval = true)
public interface ServerNameProvider {

    String getServerName();

}
