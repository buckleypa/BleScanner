package com.paulbuckley.blescanner.exceptions;

/**
 * Created by paulb on 9/9/13.
 */
public class
IllegalAdvertisementDataException
        extends Exception
{
    public
    IllegalAdvertisementDataException()
    {
        super();
    }

    public
    IllegalAdvertisementDataException(
            String message
    )
    {
        super( message );
    }

    public
    IllegalAdvertisementDataException(
            String message,
            Throwable cause
    )
    {
        super( message, cause );
    }

    public
    IllegalAdvertisementDataException(
            Throwable cause
    )
    {
        super( cause );
    }
}
