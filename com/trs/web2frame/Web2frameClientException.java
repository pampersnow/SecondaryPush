/**
 * 2008-1-29
 */
package com.trs.web2frame;

/**
 * Title: TRS ����Э��ƽ̨��TRS WCM�� <BR>
 * Description: <BR>
 * TODO <BR>
 * Copyright: Copyright (c) 2004-2005 TRS��Ϣ�������޹�˾ <BR>
 * Company: TRS��Ϣ�������޹�˾(www.trs.com.cn) <BR>
 * 
 * @author TRS��Ϣ�������޹�˾ LY
 * @version 1.0
 */

public class Web2frameClientException extends RuntimeException {

    private static final long serialVersionUID = 5174479536070849821L;

    public Web2frameClientException() {
        super();
    }

    public Web2frameClientException(String message) {
        super(message);
    }

    public Web2frameClientException(Throwable cause) {
        super(cause);
    }

    public Web2frameClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
