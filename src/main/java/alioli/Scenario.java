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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.Nullable;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import java.util.*;

import static org.junit.runner.Description.createSuiteDescription;
import static org.junit.runner.Description.createTestDescription;

/**
 * Base class to write tests using a subject, action and test sections.
 *
 * <p>The {@link #subject subject} is entity being tested and also serves as the subject of the
 * sentences you write for each test. Subject can be followed by an {@link #action action}. For
 * example, you can describe a subject in varying situations using {@link #when when}. Lastly,
 * When you want to finish, you write a {@link #test test}. For example, you can assert the state
 * of the subject using {@link #should should}.
 *
 * <p>For example:<pre><code>
 * {@literal @}RunWith(Scenario.Runner.class)</code>{@code
 *  public class StackTest extends Scenario {{
 *      subject("stack", () -> {
 *          Stack<Object> stack = new Stack<>();
 *
 *          when("non-empty", () -> {
 *              Object value = new Object();
 *              stack.push(value);
 *
 *              should("return the head value on pop", () -> {
 *                  assertThat(stack.pop()).isEqualTo(value);
 *              });
 *          });
 *
 *          when("empty", () -> {
 *              should("complain on pop", () -> {
 *                  Exception failure = assertThrows(() -> stack.pop());
 *                  assertThat(failure).isInstanceOf(EmptyStackException.class);
 *              });
 *          });
 *      });
 *  }}
 * }</pre>
 */
public abstract class Scenario {

    private final Multimap<String, Task> mSubjects = LinkedHashMultimap.create();
    private final Map<String, Test> mTests = new LinkedHashMap<>();
    private final Deque<String> mPath = new LinkedList<>();

    @Nullable private Deque<String> mPathToRun;
    private boolean mHasSubject = false;

    /**
     * Starts a definition of new subject with given name.
     *
     * @param name       the subject's name
     * @param definition the subject's definition
     */
    protected void subject(final String name, final Task definition) {
        WrongStructureException.ensure(!mHasSubject, "subject inside another subject");
        mSubjects.put(name, definition);
        mPath.addLast(name);
        mHasSubject = true;
        try {
            run(definition);
        } finally {
            mPath.removeLast();
            mHasSubject = false;
        }
    }

    /**
     * Starts a new situation for the subject.
     *
     * @param description the description of the situation
     * @param definition  the definition of the situation
     */
    protected void when(final String description, final Task definition) {
        action("when " + description, definition);
    }

    /**
     * Starts an additional situation for the subject.
     *
     * @param description the description of the situation
     * @param definition  the definition of the situation
     */
    protected void and(final String description, final Task definition) {
        action("and " + description, definition);
    }

    /**
     * Starts a definition of a new action for the subject.
     *
     * @param description the description of the action
     * @param definition  the definition of the action
     */
    protected final void action(final String description, final Task definition) {
        if (mPathToRun == null) {
            WrongStructureException.ensure(mHasSubject, "action without subject");
            mPath.addLast(description);
            try {
                final int testsBefore = mTests.size();
                run(definition);
                WrongStructureException.ensure(
                        mTests.size() > testsBefore, "action without tests");
            } finally {
                mPath.removeLast();
            }
        } else if (!mPathToRun.isEmpty() && mPathToRun.getFirst().equals(description)) {
            mPathToRun.removeFirst();
            run(definition);
            if (!mPathToRun.isEmpty()) {
                mPathToRun.addFirst(description);
            }
        }
    }

    /**
     * Starts an assertion of the subject's state.
     *
     * @param description the description of the assertion
     * @param definition  the definition of the assertion
     */
    protected void should(final String description, final Task definition) {
        test("should " + description, definition);
    }

    /**
     * Starts a definition of a new test for the subject.
     *
     * @param description the description of the test
     * @param definition  the definition of the test
     */
    protected final void test(final String description, final Task definition) {
        if (mPathToRun == null) {
            WrongStructureException.ensure(mHasSubject, "test without subject");
            mPath.addLast(description);
            try {
                final Test test = new Test(mPath);
                final String name = test.getName();
                WrongStructureException.ensure(
                        !mTests.containsKey(name),
                        "test with identical name has been already defined");
                mTests.put(name, test);
            } finally {
                mPath.removeLast();
            }
        } else if (!mPathToRun.isEmpty() && mPathToRun.getFirst().equals(description)) {
            mPathToRun.removeFirst();
            run(definition);
        }
    }

    private List<Test> getTests() {
        return ImmutableList.copyOf(mTests.values());
    }

    private void run(final Collection<String> path) {
        mPathToRun = new LinkedList<>(path);
        try {
            for (final Task task : mSubjects.get(mPathToRun.removeFirst())) {
                run(task);
            }
        } finally {
            mPathToRun = null;
        }
    }

    private static void run(final Task task) {
        try {
            task.run();
        } catch (final RuntimeException failure) {
            throw failure;
        } catch (final Exception failure) {
            throw new Failure(failure);
        }
    }

    /**
     * JUnit {@link org.junit.runner.Runner} to run a {@link Scenario} tests.
     */
    public static final class Runner extends org.junit.runner.Runner {

        private final Class<? extends Scenario> mTestClass;
        private final Scenario mTestInstance;
        private final List<Test> mTests;

        public Runner(final Class<? extends Scenario> testClass)
                throws IllegalAccessException, InstantiationException {
            mTestClass = testClass;
            mTestInstance = testClass.newInstance();
            mTests = mTestInstance.getTests();
        }

        @Override
        public Description getDescription() {
            final Description description = createSuiteDescription(mTestClass);
            for (final Test test : mTests) {
                description.addChild(createTestDescription(mTestClass, test.getName()));
            }
            return description;
        }

        @Override
        public void run(final RunNotifier runNotifier) {
            for (final Test test : mTests) {
                run(runNotifier, test);
            }
        }

        private void run(final RunNotifier runNotifier, final Test test) {
            final EachTestNotifier testNotifier = new EachTestNotifier(
                    runNotifier, createTestDescription(mTestClass, test.getName()));
            testNotifier.fireTestStarted();
            try {
                test.run(mTestInstance);
            } catch (final Failure failure) {
                testNotifier.addFailure(failure.getCause());
            } catch (final Throwable failure) {
                testNotifier.addFailure(failure);
            } finally {
                testNotifier.fireTestFinished();
            }
        }
    }

    private static final class Test {

        private final List<String> mPath;
        private final String mName;

        private Test(final Collection<String> path) {
            mPath = ImmutableList.copyOf(path);

            final int size = mPath.stream().mapToInt(String::length).sum();
            final StringBuilder name = new StringBuilder(size + mPath.size());
            for (final String piece : path) {
                name.append(piece).append(' ');
            }
            mName = name.deleteCharAt(name.length() - 1).toString();
        }

        public String getName() {
            return mName;
        }

        public void run(final Scenario scenario) {
            scenario.run(mPath);
        }
    }

    private static final class Failure extends RuntimeException {

        private static final long serialVersionUID = -4761478263439494444L;

        private Failure(final Throwable cause) {
            super(cause);
        }
    }
}
