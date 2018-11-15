package com.trs.exchange.metadata;

import java.util.List;

import com.trs.components.metadata.center.MetaViewData;
import com.trs.exchange.MyDBManager;
import com.trs.infra.common.WCMException;
import com.trs.infra.persistent.WCMFilter;

public class MetaDataExchangeImpl extends MetaDataExchange {
    
    public MetaDataExchangeImpl(MyDBManager _dbMgr) {        
        super(_dbMgr);
    }

    protected WCMFilter[] makeFilterQueryAppendixOfDocument(
            MetaViewData _oMetaData) {
        return null;
    }

    protected List makeAppendixesOfDocument(MetaViewData _oMetaData)
            throws Exception {
        return null;
    }

    protected String makeResFullPathName(MetaViewData _oMetaData,
            String _sResName, boolean _bAppendix) throws Exception {
        return null;
    }

    protected String makeMedialPathName(MetaViewData _oMetaData,
            String _sMediaFileName) throws WCMException {
        // �ٶ�Mas���Է���һ���ļ����ļ���Ź��������������д��ʽ
        return _sMediaFileName;
    }

}
