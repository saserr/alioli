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

/**
 * Useful assertions when writing tests.
 */
public final class Asserts {

    /**
     * Asserts that given {@link Task} will throw an {@link Exception}.
     *
     * @param task the {@link Task} that is supposed to throw an {@link Exception}
     * @return the thrown {@link Exception}
     * @throws AssertionError if {@link Task} does not throw an {@link Exception}
     */
    public static Exception assertThrows(final Task task) {
        try {
            task.run();
        } catch (final Exception failure) {
            return failure;
        }
        throw new AssertionError("task did not throw an exception");
    }

    private Asserts() {}
}
