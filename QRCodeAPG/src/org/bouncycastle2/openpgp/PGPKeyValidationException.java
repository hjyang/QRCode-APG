package org.bouncycastle2.openpgp;

/**
 * Thrown if the key checksum is invalid.
 */
public class PGPKeyValidationException 
    extends PGPException
{
    /**
     * @param message
     */
    public PGPKeyValidationException(String message)
    {
        super(message);
    }
}
