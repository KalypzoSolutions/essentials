package net.wandoria.essentials.rpc;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;

@Getter
@Setter
@NullMarked
public class GenericRPC {

    private final String receiverServerName;

    public GenericRPC(String receiverServerName) {
        this.receiverServerName = receiverServerName;
    }


}
