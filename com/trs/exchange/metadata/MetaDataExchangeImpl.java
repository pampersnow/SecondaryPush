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
        // 假定Mas可以访问一下文件，文件存放规则就是数据中所写方式
        return _sMediaFileName;
    }

}
