/**
 * 2008-1-29
 */
package com.trs.web2frame.eventhandler;

import com.trs.web2frame.Web2frameClientException;
import com.trs.web2frame.dispatch.Dispatch;

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

public interface ICallbackFailureHandler extends ICallbackEventHandler {
    public void onFailure(Dispatch oDispatch)
            throws Web2frameClientException;
}
