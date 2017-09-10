/*
 * Copyright 2017 Sanjin Sehic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alioli;

import org.junit.Assert;
import org.junit.runner.RunWith;

import static alioli.Asserts.assertThrows;
import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for {@link Asserts}.
 */
@RunWith(Scenario.Runner.class)
public class AssertsTest extends Scenario {
    {
        subject("assertThrows", () -> {
            when("given a task that throws an exception", () -> {
                should("return the exception", () -> {
                    final Exception expected = new Exception();
                    final Exception actual = assertThrows(() -> {
                        throw expected;
                    });

                    assertThat(actual).isSameAs(expected);
                });
            });

            when("given a task that does not throw an exception", () -> {
                should("throw an AssertionError", () -> {
                    try {
                        assertThrows(() -> {});
                        Assert.fail("AssertionError should have been thrown");
                    } catch (final AssertionError expected) {}
                });
            });
        });
    }
}
