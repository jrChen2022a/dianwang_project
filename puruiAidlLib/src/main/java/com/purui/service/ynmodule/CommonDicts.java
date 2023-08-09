package com.purui.service.ynmodule;


import java.util.Dictionary;
import java.util.Hashtable;

class CommonDicts {
    static Dictionary<SwitchType, String> code2StrDict = new Hashtable<>();

    static{
        code2StrDict.put(SwitchType.dieLuo, "跌落保险");
        code2StrDict.put(SwitchType.daoZha, "刀闸（隔离开关）");
        code2StrDict.put(SwitchType.ZW32daoKai, "开关（断路器）");
        code2StrDict.put(SwitchType.ZW32wuDao, "开关（断路器）");
        code2StrDict.put(SwitchType.xinGaiNian, "unknown");
    }
}
