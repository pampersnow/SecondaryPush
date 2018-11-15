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
    // // 置标在站点首页中
    // case WebSite.OBJ_TYPE:
    // WebSite currPageIsSite = (WebSite) pageElement.getSubstance();
    // sWho = "站点【"+currPageIsSite.getName()+"】";
    // break;
    // // 置标在栏目首页中
    // case Channel.OBJ_TYPE:
    // Channel currPageIsChannel = (Channel) pageElement.getSubstance();
    // sWho = "栏目【"+currPageIsChannel.getName()+"】";
    // break;
    // // 置标在文档细览中
    // case Document.OBJ_TYPE:
    // Document currPageIsDocuemnt = (Document) pageElement.getSubstance();
    // sWho = "文档【"+currPageIsDocuemnt.getTitle()+"】";
    // break;
    // }
    //
    // return new String[] { "Hello " + sWho + "!" };
    // }

    public String[] parse(PublishTagContext _context) throws WCMException {
        // 获取当前相关的对象并且判断是否是文档对象，如果不是抛出异常
        IPublishElement upperHost = _context.getUpperHost();
        if (upperHost.getType() != Document.OBJ_TYPE) {
            throw new WCMException("当前置标只能用于显示文档相关信息，请修改模板");
        }

        // 获取相关的文档对象
        Document currDocuemnt = (Document) upperHost.getSubstance();
        // 获取文档作者
        User oCrUser = currDocuemnt.getCrUser();
        if (oCrUser == null)
            return null;

        // 构造解析后的结果
        return new String[] { "Hello, author's telephone is "
                + oCrUser.getTel() + "!" };
    }

}
