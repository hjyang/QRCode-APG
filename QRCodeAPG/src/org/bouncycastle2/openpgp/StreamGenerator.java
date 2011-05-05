package org.bouncycastle2.openpgp;

import java.io.IOException;

interface StreamGenerator
{
    void close()
        throws IOException;
}
