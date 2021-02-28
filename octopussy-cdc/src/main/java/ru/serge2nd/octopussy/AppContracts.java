package ru.serge2nd.octopussy;

public class AppContracts {
    public static final String DATA_KITS = "dataKits";
    public static final String KIT_ID    = "kitId"   , P_KIT_ID  = '{'+KIT_ID+'}';
    public static final String QUERY     = "query";
    public static final String UPDATE    = "update";

    public static final String DATA_KITS_PATH       = '/'+DATA_KITS;
    public static final String DATA_KIT_PATH        = '/'+DATA_KITS+'/'+P_KIT_ID;
    public static final String DATA_KIT_QUERY_PATH  = '/'+DATA_KITS+'/'+P_KIT_ID+'/'+QUERY;
    public static final String DATA_KIT_UPDATE_PATH = '/'+DATA_KITS+'/'+P_KIT_ID+'/'+UPDATE;
}
