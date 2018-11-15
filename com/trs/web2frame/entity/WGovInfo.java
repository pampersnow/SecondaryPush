/**
 * 2008-1-31
 */
package com.trs.web2frame.entity;

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

public class WGovInfo extends WBaseObj {
	private String m_sQuoteToChannelIds = null;

	public WGovInfo() {
		super();
	}

	public void setId(int _nGovInfoId) {
		this.setFieldValue("GovInfoId", String.valueOf(_nGovInfoId));
	}

	public int getId() {
		String sGovInfoId = this.getFieldValue("GovInfoId");
		if (sGovInfoId != null) {
			return Integer.parseInt(sGovInfoId);
		}
		return 0;
	}

	/**
	 * @return the quoteToChannelIds
	 */
	public String getQuoteToChannelIds() {
		return m_sQuoteToChannelIds;
	}

	/**
	 * @param _quoteToChannelIds
	 *            the quoteToChannelIds to set
	 */
	public void setQuoteToChannelIds(String _quoteToChannelIds) {
		m_sQuoteToChannelIds = _quoteToChannelIds;
	}

	public void setDocChannel(String _sDocChannelCode) {
		this.setFieldValue("DocChannel", _sDocChannelCode);
	}
}
