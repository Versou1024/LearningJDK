package sun.net.www.protocol.http;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class NegotiateCallbackHandler
  implements CallbackHandler
{
  private String username;
  private char[] password;

  public void handle(Callback[] paramArrayOfCallback)
    throws UnsupportedCallbackException, IOException
  {
    for (int i = 0; i < paramArrayOfCallback.length; ++i)
    {
      Object localObject;
      Callback localCallback = paramArrayOfCallback[i];
      if (localCallback instanceof NameCallback)
      {
        if (this.username == null)
        {
          localObject = Authenticator.requestPasswordAuthentication(null, null, 0, null, null, "Negotiate");
          this.username = ((PasswordAuthentication)localObject).getUserName();
          this.password = ((PasswordAuthentication)localObject).getPassword();
        }
        localObject = (NameCallback)localCallback;
        ((NameCallback)localObject).setName(this.username);
      }
      else if (localCallback instanceof PasswordCallback)
      {
        localObject = (PasswordCallback)localCallback;
        if (this.password == null)
        {
          PasswordAuthentication localPasswordAuthentication = Authenticator.requestPasswordAuthentication(null, null, 0, null, null, "Negotiate");
          this.username = localPasswordAuthentication.getUserName();
          this.password = localPasswordAuthentication.getPassword();
        }
        ((PasswordCallback)localObject).setPassword(this.password);
        Arrays.fill(this.password, ' ');
      }
      else
      {
        throw new UnsupportedCallbackException(localCallback, "Call back not supported");
      }
    }
  }
}