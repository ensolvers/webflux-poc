/* Copyright (c) 2021 Ensolvers
 * All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2 alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to the project.
 *
 * You may obtain a copy of the LGPL License at: http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at: http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */
package com.ensolvers.fox.email;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * A Test case for {@link EmailService}
 *
 * @author Esteban Robles Luna
 */
class EmailServiceTest {

  @Test
  @Disabled("disabled")
  void testEmail() throws Exception {
    EmailService service = new EmailService("host", 465, "username", "password", "info@ensolvers.com");
    service.sendMailTo("esteban.roblesluna@gmail.com", "hola", "Hola esteban como estas?");

    Assertions.assertFalse(service.toString().isEmpty());
  }
}
