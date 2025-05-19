package org.timetable.algorithm.constraints;

import org.timetable.algorithm.model.LessonWithTime;
import org.timetable.algorithm.model.TableTimeSetting;
import org.timetable.algorithm.scheduling.DataForConstraint;

import java.util.*;

public class PenaltyChecker {
    private final List<Penalty> penalties;
    private final TableTimeSetting tableTimeSetting;

    public static PenaltyCheckerBuilder newBuilder(TableTimeSetting tableTimeSetting) {
        return new PenaltyCheckerBuilder(tableTimeSetting);
    }


    private PenaltyChecker(List<Penalty> penalties, TableTimeSetting tableTimeSetting) {
        this.penalties = penalties;
        this.tableTimeSetting = tableTimeSetting;
    }

    public static final class CheckResult {
        final Map<Penalty, PenaltyResult> penaltyToError;
        double total;

        public CheckResult(Map<Penalty, PenaltyResult> penaltyToError) {
            this.penaltyToError = penaltyToError;
            this.total = 0;
        }

        public Map<Penalty, PenaltyResult> penaltyToError() {
            return penaltyToError;
        }

        public double total() {
            return total;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (CheckResult) obj;
            return Objects.equals(this.penaltyToError, that.penaltyToError) &&
                    Double.doubleToLongBits(this.total) == Double.doubleToLongBits(that.total);
        }

        @Override
        public int hashCode() {
            return Objects.hash(penaltyToError, total);
        }

        @Override
        public String toString() {
            return "CheckResult[" +
                    "penaltyToError=" + penaltyToError + ", " +
                    "total=" + total + ']';
        }


    }

    public record ProblemLesson(LessonWithTime lesson, String message) {
        @Override
        public String toString() {
            return "{" +
                    "L=" + lesson +
                    ":'" + message +
                    '}';
        }
    }

    public static class PenaltyResult {
        double summaryPenalty = 0;
        List<ProblemLesson> problemLessons = new ArrayList<>();

        public double getSummaryPenalty() {
            return summaryPenalty;
        }

        public List<ProblemLesson> getProblemLessons() {
            return problemLessons;
        }

        @Override
        public String toString() {
            return "PR{" +
                    "P=" + summaryPenalty +
                    "L=" + problemLessons +
                    '}';
        }
    }

    public CheckResult calculatePenalty(DataForConstraint dataForOneLesson) {
        Map<Penalty, PenaltyResult> penaltyToError = new HashMap<>();
        var checkResult = new CheckResult(penaltyToError);
        penalties.forEach(penalty -> {
            var calculateResult = penalty.penaltyFunction.apply(dataForOneLesson);
            var penaltyValue = calculateResult.value();
            if (penaltyValue < 0) {
                if (!penaltyToError.containsKey(penalty)) {
                    penaltyToError.put(penalty, new PenaltyResult());
                }
                PenaltyResult result = penaltyToError.get(penalty);
                result.summaryPenalty += penaltyValue;
                checkResult.total += penaltyValue;
                result.problemLessons.add(new ProblemLesson(dataForOneLesson.currentLesson(), calculateResult.message()));
            }
        });
        return checkResult;
    }

    public CheckResult calculatePenalty(List<LessonWithTime> lessonWithTimes) {
        return calculatePenalty(lessonWithTimes, DataForConstraint.generate(lessonWithTimes, tableTimeSetting));
    }

    public CheckResult calculatePenalty(List<LessonWithTime> lessonWithTimes,
                                        List<List<List<LessonWithTime>>> allLessons) {
        Map<Penalty, PenaltyResult> penaltyToError = new HashMap<>();
        var checkResult = new CheckResult(penaltyToError);
        lessonWithTimes.forEach(lesson -> {
            var data = new DataForConstraint(lessonWithTimes, lesson, allLessons, tableTimeSetting);
            penalties.forEach(penalty -> {
                var calculateResult = penalty.penaltyFunction.apply(data);
                var value = calculateResult.value();
                if (value < 0) {
                    if (!penaltyToError.containsKey(penalty)) {
                        penaltyToError.put(penalty, new PenaltyResult());
                    }
                    PenaltyResult result = penaltyToError.get(penalty);
                    result.summaryPenalty += value;
                    checkResult.total += value;
                    result.problemLessons.add(new ProblemLesson(data.currentLesson(), calculateResult.message()));
                }
            });
                }
        );
        return checkResult;
    }


    public static class PenaltyCheckerBuilder {
        private final List<Penalty> penalties = new ArrayList<>();
        private final Set<Penalty> penaltySet = new HashSet<>();
        private final TableTimeSetting tableTimeSetting;

        public PenaltyCheckerBuilder(TableTimeSetting tableTimeSetting) {
            this.tableTimeSetting = tableTimeSetting;
        }

        public PenaltyCheckerBuilder addPenalty(Penalty penalty) {
            if (!penaltySet.contains(penalty)) {
                penaltySet.add(penalty);
                penalties.add(penalty);
            }
            return this;
        }

        public PenaltyCheckerBuilder addPenalties(Collection<Penalty> penalties) {
            penalties.stream().forEach(this::addPenalty);
            return this;
        }


        public PenaltyChecker build() {
            return new PenaltyChecker(penalties, tableTimeSetting);
        }
    }
}
