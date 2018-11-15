/**
 * 2008-2-19
 */
package com.trs.web2frame.entity;

import java.util.HashMap;
import java.util.Map;

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

public class WBaseObj {
    private Map m_oContentBody = null;
    
    public WBaseObj() {
        m_oContentBody = new HashMap();
    }
   /**
     * @return the documentBody
     */
    public Map getContentBody() {
        return m_oContentBody;
    }

    public void setFieldValue(String _sFieldName, Object _oFieldValue) {
        m_oContentBody.put(_sFieldName.toUpperCase(), _oFieldValue.toString());
    }

    public String getFieldValue(String _sFieldName) {
        if (m_oContentBody.get(_sFieldName.toUpperCase()) == null)
            return null;
        return m_oContentBody.get(_sFieldName.toUpperCase()).toString();
    }
}
