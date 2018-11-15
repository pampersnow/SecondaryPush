package com.trs.example.publish;

import com.trs.cms.auth.persistent.User;
import com.trs.components.common.publish.domain.publisher.PublishTagContext;
import com.trs.components.common.publish.domain.tagparser.ITagParser;
import com.trs.components.common.publish.domain.tagparser.TagBeanInfo;
import com.trs.components.common.publish.domain.tagparser.TagItem;
import com.trs.components.common.publish.persistent.element.IPublishElement;
import com.trs.components.common.publish.persistent.element.IPublishFolder;
import com.trs.components.common.publish.persistent.template.TemplateQuote;
import com.trs.components.wcm.content.persistent.Document;
import com.trs.infra.common.WCMException;

public class XWCMTagParserHello implements ITagParser {

    public XWCMTagParserHello() {
        // TODO Auto-generated constructor stub
    }

    public void clear() {
        // TODO Auto-generated method stub

    }

    public TagBeanInfo getBeanInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setTagItem(TagItem _item) {
    }

    public TemplateQuote[] analyzeQuotes(IPublishFolder _o_root)
            throws WCMException {
        // TODO Auto-generated method stub
        return null;
    }

    // public String[] parse(PublishTagContext _context) throws WCMException {
    // String sWho = null;
    // IPublishElement pageElement = _context.getPageElement();
    // switch (pageElement.getType()) {
    // // �ñ���վ����ҳ��
    // case WebSite.OBJ_TYPE:
    // WebSite currPageIsSite = (WebSite) pageElement.getSubstance();
    // sWho = "վ�㡾"+currPageIsSite.getName()+"��";
    // break;
    // // �ñ�����Ŀ��ҳ��
    // case Channel.OBJ_TYPE:
    // Channel currPageIsChannel = (Channel) pageElement.getSubstance();
    // sWho = "��Ŀ��"+currPageIsChannel.getName()+"��";
    // break;
    // // �ñ����ĵ�ϸ����
    // case Document.OBJ_TYPE:
    // Document currPageIsDocuemnt = (Document) pageElement.getSubstance();
    // sWho = "�ĵ���"+currPageIsDocuemnt.getTitle()+"��";
    // break;
    // }
    //
    // return new String[] { "Hello " + sWho + "!" };
    // }

    public String[] parse(PublishTagContext _context) throws WCMException {
        // ��ȡ��ǰ��صĶ������ж��Ƿ����ĵ�������������׳��쳣
        IPublishElement upperHost = _context.getUpperHost();
        if (upperHost.getType() != Document.OBJ_TYPE) {
            throw new WCMException("��ǰ�ñ�ֻ��������ʾ�ĵ������Ϣ�����޸�ģ��");
        }

        // ��ȡ��ص��ĵ�����
        Document currDocuemnt = (Document) upperHost.getSubstance();
        // ��ȡ�ĵ�����
        User oCrUser = currDocuemnt.getCrUser();
        if (oCrUser == null)
            return null;

        // ���������Ľ��
        return new String[] { "Hello, author's telephone is "
                + oCrUser.getTel() + "!" };
    }

}
