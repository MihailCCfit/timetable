package org.timetable.algorithm.model;


public record AudienceEvolve(String id,
                             SubjectType auditoryType) {

    @Override
    public String toString() {
        return id + ":" + auditoryType.name().substring(0, 2);
    }
}
