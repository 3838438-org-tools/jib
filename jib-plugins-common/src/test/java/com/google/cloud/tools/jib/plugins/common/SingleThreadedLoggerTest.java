/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.jib.plugins.common;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link SingleThreadedLogger}. */
public class SingleThreadedLoggerTest {

  private static final Duration FUTURE_TIMEOUT = Duration.ofSeconds(1);

  private final StringBuilder logBuilder = new StringBuilder();
  private final SingleThreadedLogger testSingleThreadedLogger =
      new SingleThreadedLogger(logBuilder::append);

  @Test
  public void testLog_noFooter() throws InterruptedException, ExecutionException, TimeoutException {
    testSingleThreadedLogger
        .log(() -> logBuilder.append("message\n"))
        .get(FUTURE_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

    Assert.assertEquals("\033[0Jmessage\n", logBuilder.toString());
  }

  @Test
  public void testLog_sameFooter()
      throws InterruptedException, ExecutionException, TimeoutException {
    testSingleThreadedLogger
        .setFooter("footer", 1)
        .get(FUTURE_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    testSingleThreadedLogger
        .log(() -> logBuilder.append("message\n"))
        .get(FUTURE_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    testSingleThreadedLogger
        .log(() -> logBuilder.append("another message\n"))
        .get(FUTURE_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

    Assert.assertEquals(
        "\033[0Jfooter\033[1A\033[0Jmessage\nfooter\033[1A\033[0Janother message\nfooter",
        logBuilder.toString());
  }

  @Test
  public void testLog_changingFooter()
      throws InterruptedException, ExecutionException, TimeoutException {
    testSingleThreadedLogger
        .setFooter("footer", 1)
        .get(FUTURE_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    testSingleThreadedLogger
        .log(() -> logBuilder.append("message\n"))
        .get(FUTURE_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    testSingleThreadedLogger
        .setFooter("two line\nfooter", 2)
        .get(FUTURE_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    testSingleThreadedLogger
        .log(() -> logBuilder.append("another message\n"))
        .get(FUTURE_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

    Assert.assertEquals(
        "\033[0Jfooter\033[1A\033[0Jmessage\nfooter\033[1A\033[0Jtwo line\nfooter"
            + "\033[1A\033[1A\033[0Janother message\ntwo line\nfooter",
        logBuilder.toString());
  }
}
