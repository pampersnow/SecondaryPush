package com.trs.example.flow.destroytask;

import com.trs.components.common.publish.domain.PublishServer;
import com.trs.components.common.publish.persistent.element.IPublishContent;
import com.trs.components.common.publish.persistent.element.IPublishFolder;
import com.trs.components.common.publish.persistent.element.PublishElementFactory;
import com.trs.components.wcm.content.persistent.Channel;
import com.trs.components.wcm.content.persistent.ChnlDoc;
import com.trs.components.wcm.content.persistent.Document;

public class DestroyTaskHelper {
    
    /**
     * 撤销指定的内容
     * @param _nObjectType
     * @param _nObjectId
     * @throws Exception
     */
    public static void destoryDirect(int _nObjectType, int _nObjectId)
            throws Exception {
        int nDocId = 0, nChannelId = 0;
        switch (_nObjectType) {
        case ChnlDoc.OBJ_TYPE:

            ChnlDoc chnlDoc = ChnlDoc.findById(_nObjectId);
            if (chnlDoc != null) {
                nDocId = chnlDoc.getDocId();
                nChannelId = chnlDoc.getChannelId();
            }
            break;
        case Document.OBJ_TYPE:
            nDocId = _nObjectId;
            break;
        default:
            break;
        }

        Document document = Document.findById(nDocId);
        if (document == null)
            return;

        if (nChannelId <= 0)
            nChannelId = document.getChannelId();
        Channel channel = Channel.findById(nChannelId);
        if (channel == null)
            return;

        IPublishFolder folder = (IPublishFolder) PublishElementFactory
                .makeElementFrom(channel);
        IPublishContent content = PublishElementFactory.makeContentFrom(
                document, folder);

        PublishServer.getInstance().deleteContent(content);

    }

}
