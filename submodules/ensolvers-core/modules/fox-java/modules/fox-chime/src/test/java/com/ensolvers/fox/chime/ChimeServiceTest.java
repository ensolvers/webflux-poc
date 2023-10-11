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
package com.ensolvers.fox.chime;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.chime.model.Attendee;
import com.amazonaws.services.chime.model.Meeting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ChimeServiceTest {

  /**
   * NOTE: test disabled since Localstack Chime integration is not available
   */

  @Test
  @Disabled("Integration not available")
  void testChime() {
    String accessKey = "";
    String secretKey = "";
    Regions region = Regions.US_EAST_1;
    String chatAccessKey = "";
    String chatSecretKey = "";
    String appArn = "";

    ChimeService service = new ChimeService(new BasicAWSCredentials(accessKey, secretKey), region, appArn,
        new BasicAWSCredentials(chatAccessKey, chatSecretKey));

    service.listChannels("userArn");
    Meeting meeting = service.createMeeting("token");
    service.getMeeting(meeting.getMeetingId());
    Attendee attendee = service.joinMeeting("userId", meeting.getMeetingId());
    service.listChannels("userArn");
    Assertions.assertSame(service.getMeeting(meeting.getMeetingId()), meeting);
  }
}
