package com.custom.payment.db.projection;

import java.time.LocalDate;

public interface CommonUserProjection {

    Long getId();

    String getName();

    LocalDate getDateOfBirth();
}
