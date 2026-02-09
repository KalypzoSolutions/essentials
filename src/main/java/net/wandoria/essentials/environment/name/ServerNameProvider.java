package net.wandoria.essentials.environment.name;

/**
 * @deprecated Use {@link net.wandoria.essentials.util.servername.InternalServerName} instead. This interface is no longer used and will be removed in a future version.
 */
@Deprecated(forRemoval = true)
public interface ServerNameProvider {

    String getServerName();

}
