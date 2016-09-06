package org.zarroboogs.smartzpn.tunnel.shadowsocks;

import org.zarroboogs.smartzpn.tunnel.IEncryptor;

import java.util.HashMap;


public class EncryptorFactory {

    private static HashMap<String, IEncryptor> EncryptorCache = new HashMap<String, IEncryptor>();

    public static IEncryptor createEncryptorByConfig(ShadowsocksConfig config) throws Exception {
        if ("table".equals(config.EncryptMethod)) {
            IEncryptor tableEncryptor = EncryptorCache.get(config.toString());
            if (tableEncryptor == null) {
                tableEncryptor = new TableEncryptor(config.Password);
                if (EncryptorCache.size() > 2) {
                    EncryptorCache.clear();
                }
                EncryptorCache.put(config.toString(), tableEncryptor);
            }
            return tableEncryptor;
        }
        throw new Exception(String.format("Does not support the '%s' method. Only 'table' encrypt method was supported.", config.EncryptMethod));
    }
}
