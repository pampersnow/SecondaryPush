/**
 * 2008-1-30
 */
package com.trs.web2frame.dispatch;

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

public class DispatchConvertException extends RuntimeException{

    private static final long serialVersionUID = 5174479536070849821L;

    public DispatchConvertException() {
        super();
    }

    public DispatchConvertException(String message) {
        super(message);
    }

    public DispatchConvertException(Throwable cause) {
        super(cause);
    }

    public DispatchConvertException(String message, Throwable cause) {
        super(message, cause);
    }

}
