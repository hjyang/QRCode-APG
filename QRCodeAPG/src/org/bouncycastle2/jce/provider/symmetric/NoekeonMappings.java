package org.bouncycastle2.jce.provider.symmetric;

import java.util.HashMap;

public class NoekeonMappings
    extends HashMap
{
    public NoekeonMappings()
    {
        put("AlgorithmParameters.NOEKEON", "org.bouncycastle2.jce.provider.symmetric.Noekeon$AlgParams");

        put("AlgorithmParameterGenerator.NOEKEON", "org.bouncycastle2.jce.provider.symmetric.Noekeon$AlgParamGen");
        
        put("Cipher.NOEKEON", "org.bouncycastle2.jce.provider.symmetric.Noekeon$ECB");

        put("KeyGenerator.NOEKEON", "org.bouncycastle2.jce.provider.symmetric.Noekeon$KeyGen");
    }
}
