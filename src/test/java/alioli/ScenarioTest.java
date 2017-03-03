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

import org.jetbrains.annotations.Nullable;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.junit.runner.Description.createTestDescription;

/**
 * Tests for {@link Scenario}.
 */
@RunWith(Scenario.Runner.class)
public class ScenarioTest extends Scenario {
    {
        subject("scenario", () -> {
            without("any tests", () -> {
                final Runner runner = new Runner(NoTests.class);

                should("have a description without tests", () -> {
                    final Description description = runner.getDescription();
                    assertThat(description.getTestClass()).isEqualTo(NoTests.class);
                    assertThat(description.getChildren()).isEmpty();
                });

                should("be runnable", () -> {
                    runner.run(new RunNotifier());
                });
            });

            with("a test", () -> {
                SingleTest.sTestExecuted = false;
                final Runner runner = new Runner(SingleTest.class);
                final Description testDescription =
                        createTestDescription(SingleTest.class, "test should be testable");

                should("have a description with the test", () -> {
                    final Description description = runner.getDescription();
                    assertThat(description.getTestClass()).isEqualTo(SingleTest.class);
                    assertThat(description.getChildren()).containsExactly(
                            testDescription);
                });

                when("not executed", () -> {
                    should("not execute the test", () -> {
                        assertThat(SingleTest.sTestExecuted).isFalse();
                    });
                });

                when("executed", () -> {
                    final SpyListener listener = new SpyListener(testDescription);
                    final RunNotifier runNotifier = new RunNotifier();
                    runNotifier.addListener(listener);
                    runner.run(runNotifier);

                    should("execute the test", () -> {
                        assertThat(SingleTest.sTestExecuted).isTrue();
                    });

                    should("notify a successful execution", () -> {
                        assertThat(listener.isStarted()).isTrue();
                        assertThat(listener.isFinished()).isTrue();
                        assertThat(listener.getFailure()).isNull();
                    });
                });
            });

            with("a test", () -> {
                final RunNotifier runNotifier = new RunNotifier();
                final Runner runner = new Runner(TestsThatFail.class);

                that("fails an assertion", () -> {
                    final SpyListener listener = new SpyListener(
                            createTestDescription(TestsThatFail.class, "test should fail an " +
                                    "assertion"));
                    runNotifier.addListener(listener);
                    runner.run(runNotifier);

                    should("notify a failed execution", () -> {
                        assertThat(listener.isStarted()).isTrue();
                        assertThat(listener.isFinished()).isTrue();
                        assertThat(listener.getFailure()).isNotNull();
                    });
                });

                that("throws an exception", () -> {
                    final SpyListener listener = new SpyListener(
                            createTestDescription(TestsThatFail.class, "test should throw an " +
                                    "exception"));
                    runNotifier.addListener(listener);
                    runner.run(runNotifier);

                    should("notify a failed execution", () -> {
                        assertThat(listener.isStarted()).isTrue();
                        assertThat(listener.isFinished()).isTrue();
                        assertThat(listener.getFailure()).isNotNull();
                    });
                });
            });

            with("more than one tests", () -> {
                final Runner runner = new Runner(IsolationTest.class);

                should("execute the tests in isolation", () -> {
                    runner.run(new RunNotifier());
                    assertThat(IsolationTest.sWereFresh).isTrue();
                });
            });

            with("two subjects", () -> {
                that("have same name", () -> {
                    final Runner runner = new Runner(SubjectsWithSameName.class);

                    should("execute all their tests once", () -> {
                        runner.run(new RunNotifier());
                        assertThat(SubjectsWithSameName.sFirstTestExecuted).isEqualTo(1);
                        assertThat(SubjectsWithSameName.sSecondTestExecuted).isEqualTo(1);
                    });
                });
            });

            with("two sections", () -> {
                that("have same name", () -> {
                    final Runner runner = new Runner(SectionsWithSameName.class);

                    should("execute all their tests once", () -> {
                        runner.run(new RunNotifier());
                        assertThat(SectionsWithSameName.sFirstTestExecuted).isEqualTo(1);
                        assertThat(SectionsWithSameName.sSecondTestExecuted).isEqualTo(1);
                    });
                });
            });

            with("two tests", () -> {
                that("have identical names", () -> {
                    should("throw IllegalStructureException", () -> {
                        try {
                            new Runner(TestsWithSameName.class);
                            fail("IllegalStructureException should have been thrown");
                        } catch (final WrongStructureException expected) {
                            assertThat(expected).hasMessageThat().contains(
                                    "test with identical name has been already defined");
                        }
                    });
                });
            });

            with("subject inside another subject", () -> {
                should("throw IllegalStructureException", () -> {
                    try {
                        new Runner(SubjectInSubject.class);
                        fail("IllegalStructureException should have been thrown");
                    } catch (final WrongStructureException expected) {
                        assertThat(expected).hasMessageThat().contains(
                                "subject inside another subject");
                    }
                });
            });

            with("a test", () -> {
                without("subject", () -> {
                    should("throw IllegalStructureException", () -> {
                        try {
                            new Runner(TestWithoutSubject.class);
                            fail("IllegalStructureException should have been thrown");
                        } catch (final WrongStructureException expected) {
                            assertThat(expected).hasMessageThat().contains("test without subject");
                        }
                    });
                });
            });

            with("a section", () -> {
                without("subject", () -> {
                    should("throw IllegalStructureException", () -> {
                        try {
                            new Runner(SectionWithoutSubject.class);
                            fail("IllegalStructureException should have been thrown");
                        } catch (final WrongStructureException expected) {
                            assertThat(expected).hasMessageThat().contains(
                                    "action without subject");
                        }
                    });
                });

                without("tests", () -> {
                    should("throw IllegalStructureException", () -> {
                        try {
                            new Runner(SectionWithoutTests.class);
                            fail("IllegalStructureException should have been thrown");
                        } catch (final WrongStructureException expected) {
                            assertThat(expected).hasMessageThat().contains("action without tests");
                        }
                    });
                });
            });
        });
    }

    private void with(final String name, final Task task) {
        action("with " + name, task);
    }

    private void without(final String name, final Task task) {
        action("without " + name, task);
    }

    private void that(final String description, final Task task) {
        action("that " + description, task);
    }

    public static final class NoTests extends Scenario {}

    public static final class SingleTest extends Scenario {

        public static boolean sTestExecuted = false;

        {
            subject("test", () -> {
                should("be testable", () -> {
                    sTestExecuted = true;
                });
            });
        }
    }

    public static final class TestsThatFail extends Scenario {
        {
            subject("test", () -> {
                should("fail an assertion", () -> {
                    assertThat(true).isFalse();
                });

                should("throw an exception", () -> {
                    throw new IOException();
                });
            });
        }
    }

    public static final class IsolationTest extends Scenario {

        public static boolean sWereFresh = true;

        {
            subject("isolation test", () -> {
                final AtomicBoolean fresh = new AtomicBoolean(true);

                when("first test is executed", () -> {
                    should("not see changes made by the second test", () -> {
                        sWereFresh = sWereFresh && fresh.get();
                        fresh.set(false);
                    });
                });

                when("second test is executed", () -> {
                    should("not see changes made by the first test", () -> {
                        sWereFresh = sWereFresh && fresh.get();
                        fresh.set(false);
                    });
                });
            });
        }
    }

    public static final class SubjectsWithSameName extends Scenario {

        public static int sFirstTestExecuted = 0;
        public static int sSecondTestExecuted = 0;

        {
            subject("test", () -> {
                should("run first test", () -> {
                    sFirstTestExecuted++;
                });
            });

            subject("test", () -> {
                should("run second test", () -> {
                    sSecondTestExecuted++;
                });
            });
        }
    }

    public static final class SectionsWithSameName extends Scenario {

        public static int sFirstTestExecuted = 0;
        public static int sSecondTestExecuted = 0;

        {
            subject("test", () -> {
                when("it has two sections with same name", () -> {
                    should("run first test", () -> {
                        sFirstTestExecuted++;
                    });
                });

                when("it has two sections with same name", () -> {
                    should("run second test", () -> {
                        sSecondTestExecuted++;
                    });
                });
            });
        }
    }

    public static final class SubjectInSubject extends Scenario {
        {
            subject("test", () -> {
                subject("failure", () -> {});
            });
        }
    }

    public static final class TestsWithSameName extends Scenario {
        {
            subject("test", () -> {
                should("fail", () -> {});
                should("fail", () -> {});
            });
        }
    }

    public static final class TestWithoutSubject extends Scenario {
        {
            should("fail", () -> {});
        }
    }

    public static final class SectionWithoutSubject extends Scenario {
        {
            when("failed", () -> {});
        }
    }

    public static final class SectionWithoutTests extends Scenario {
        {
            subject("test", () -> {
                when("without any tests", () -> {});
            });
        }
    }

    public static final class SpyListener extends RunListener {

        private final Description mToObserve;

        private boolean mStarted = false;
        private boolean mFinished = false;
        @Nullable private Failure mFailure;

        private SpyListener(final Description toObserve) {
            mToObserve = toObserve;
        }

        public boolean isStarted() {
            return mStarted;
        }

        public boolean isFinished() {
            return mFinished;
        }

        public Failure getFailure() {
            return mFailure;
        }

        @Override
        public void testStarted(final Description description) {
            if (mToObserve.equals(description)) {
                mStarted = true;
            }
        }

        @Override
        public void testFinished(final Description description) {
            if (mToObserve.equals(description)) {
                mFinished = true;
            }
        }

        @Override
        public void testFailure(final Failure failure) {
            if (mToObserve.equals(failure.getDescription())) {
                mFailure = failure;
            }
        }
    }
}
