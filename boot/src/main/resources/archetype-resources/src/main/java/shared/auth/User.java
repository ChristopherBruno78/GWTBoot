package ${package}.shared.auth;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.Date;

public class User implements IsSerializable {
    public Long id;
    public String username;
    public String password;
    public Date dateCreated;
}
