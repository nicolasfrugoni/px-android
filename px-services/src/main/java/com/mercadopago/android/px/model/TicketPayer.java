package com.mercadopago.android.px.model;

public class TicketPayer extends Payer {

    public TicketPayer(final Identification identification, final String email, final String firstName,
        final String lastName) {
        setIdentification(identification);
        setEmail(email);
        setFirstName(firstName);
        setLastName(lastName);
    }
}
