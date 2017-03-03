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
 * Thrown to indicate that a test class has wrong structure.
 */
public class WrongStructureException extends RuntimeException {

    private static final long serialVersionUID = 1984415740791877695L;

    /**
     * Constructs a {@code WrongStructureException} with the given detail {@code message}.
     *
     * @param message the detail message
     */
    public WrongStructureException(final String message) {
        super(message);
    }

    /**
     * Asserts that the given {@code condition} is {@code true}. If it isn't, it throws a {@code
     * WrongStructureException} with the given {@code message}.
     *
     * @param condition condition to be checked
     * @param message   the detail message for the {@code WrongStructureException}
     */
    public static void ensure(final boolean condition, final String message) {
        if (!condition) {
            throw new WrongStructureException(message);
        }
    }
}
