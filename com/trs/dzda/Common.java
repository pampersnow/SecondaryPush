package com.trs.dzda;

import com.trs.infra.util.CMyEncrypt;

public class Common
{
  public static final int USER_STATUS_APPLY = 0;
  public static final int USER_STATUS_REG = 30;
  public static final int USER_STATUS_DEL = 10;
  public static final int USER_STATUS_FORBID = 20;
  public static final int USER_STATUS_ALL = -1;

  public static String cryptPassword(String _sPassword)
  {
    if (_sPassword == null) {
      return null;
    }
    return new CMyEncrypt().getMD5OfStr(_sPassword).substring(0, 15);
  }

  public static String getNameByStatus(int nStatus) {
    if (nStatus == 30) {
      return "已开通";
    }
    if (nStatus == 0) {
      return "待开通";
    }
    if (nStatus == 20) {
      return "已停用";
    }
    if (nStatus == -1) {
      return "全部";
    }
    return "未知";
  }
}