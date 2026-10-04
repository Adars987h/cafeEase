package com.inn.cafe.constants;

public class CafeConstants {

    public static final String SOMETHING_WENT_WRONG ="Something Went Wrong...";

    public static final String INVALID_PAYLOAD ="Invalid Payload";

    public static final String UNAUTHORISED_ACCESS ="Access Denied - User is not authorized";

    // GST applied to every order. The cart total is the item subtotal; tax is added when
    // the order is placed, and the bill shows subtotal, tax and total separately.
    public static final float TAX_RATE = 0.05f;
}
