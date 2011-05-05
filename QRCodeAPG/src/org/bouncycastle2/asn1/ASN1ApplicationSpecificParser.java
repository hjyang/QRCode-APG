package org.bouncycastle2.asn1;

import java.io.IOException;

public interface ASN1ApplicationSpecificParser
    extends DEREncodable
{
    DEREncodable readObject()
        throws IOException;
}
