package org.timetable.algorithm.scheduling;

import org.timetable.algorithm.model.LessonWithTime;
import org.timetable.algorithm.model.TableTime;
import org.timetable.algorithm.model.TableTimeSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DataForConstraint {
        private final List<LessonWithTime> allLessons;
        private final LessonWithTime currentLesson;
        private final List<List<List<LessonWithTime>>> timeTable;
        private List<LessonWithTime> otherLessons = null;
        private TableTimeSetting tableTimeSetting;


        public DataForConstraint(List<LessonWithTime> allLessons,
                                 LessonWithTime currentLesson,
                                 List<List<List<LessonWithTime>>> timeTable,
                                 TableTimeSetting tableTimeSetting) {
            this.allLessons = allLessons;
            this.currentLesson = currentLesson;
            this.timeTable = timeTable;
            this.tableTimeSetting = tableTimeSetting;
        }

        public DataForConstraint(List<LessonWithTime> allLessons,
                                 LessonWithTime currentLesson,
                                 TableTimeSetting tableTimeSetting) {
            this(allLessons, currentLesson, generate(allLessons, tableTimeSetting), tableTimeSetting);
        }

        public List<LessonWithTime> getLessonsInDay(int day) {
            var dayCells = timeTable.get(day);
            List<LessonWithTime> lessons = new ArrayList<>();
            dayCells.forEach(lessons::addAll);
            return lessons;
        }

        public List<LessonWithTime> getLessonsInCell(LessonWithTime lesson) {
            return getLessonsInCell(lesson.cell().time());
        }

        public List<LessonWithTime> getLessonsInCell(TableTime tableTime) {
            return getLessonsInCell(tableTime.day(), tableTime.cellNumber());
        }

        public List<LessonWithTime> getLessonsInCell(int day, int cellNumber) {
            return new ArrayList<>(timeTable.get(day).get(cellNumber));
        }

        public List<LessonWithTime> getOtherLessons(LessonWithTime lesson) {
            if (otherLessons == null) {
                otherLessons = getLessonsInCell(lesson);
                otherLessons.remove(lesson);
            }
            return otherLessons;
        }

        public static List<List<List<LessonWithTime>>> generate(List<LessonWithTime> lessons,
                                                                TableTimeSetting tableTimeSetting) {
            List<List<List<LessonWithTime>>> timeTable = new ArrayList<>();
            if (lessons.isEmpty()) {
                return timeTable;
            }
            for (int dayNumber = 0; dayNumber < tableTimeSetting.maxDays(); dayNumber++) {
                List<List<LessonWithTime>> dayCells = new ArrayList<>();
                for (int cellNumber = 0; cellNumber < tableTimeSetting.maxCells(); cellNumber++) {
                    dayCells.add(new ArrayList<>());
                }
                timeTable.add(dayCells);
            }
            for (LessonWithTime lesson : lessons) {
                var timeData = lesson.cell().time();
                timeTable.get(timeData.day()).get(timeData.cellNumber()).add(lesson);
            }
            return timeTable;
        }

        public List<LessonWithTime> allLessons() {
            return allLessons;
        }

        public LessonWithTime currentLesson() {
            return currentLesson;
        }

        public List<List<List<LessonWithTime>>> timeTable() {
            return timeTable;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (DataForConstraint) obj;
            return Objects.equals(this.allLessons, that.allLessons) &&
                    Objects.equals(this.currentLesson, that.currentLesson) &&
                    Objects.equals(this.timeTable, that.timeTable);
        }

        @Override
        public int hashCode() {
            return Objects.hash(allLessons, currentLesson, timeTable);
        }

        @Override
        public String toString() {
            return "DataForConstraint[" +
                    "allLessons=" + allLessons + ", " +
                    "currentLesson=" + currentLesson + ", " +
                    "timeTable=" + timeTable + ']';
        }

    }