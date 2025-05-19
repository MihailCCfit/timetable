# TimeTable

## Описание

Java: `Java 17`, используя gradle 8.8.

### dependency

```groovy
dependencies {
    testImplementation platform('org.junit:junit-bom:5.10.0')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    implementation 'io.jenetics:jenetics:7.2.0'
    implementation 'io.jenetics:prngine:2.0.0'
    compileOnly 'org.projectlombok:lombok:1.18.20'
    annotationProcessor 'org.projectlombok:lombok:1.18.20'
}
```

`org.projectlombok:lombok` - для удобства и уменьшение повторяющегося кода.
`io.jenetics:jenetics` - библиотека, использующаяся для генетического алгоритма (отбор, классы хромосом, фенотипов,
генов и т.д.), частично как фреймворк.

## Запуск проекта для примера

Пример использовавния на множественных данных лежит
в [AlgSimulation.java](src%2Fmain%2Fjava%2Forg%2Ftimetable%2Falgorithm%2Fexperementations%2FAlgSimulation.java)

Предварительно собрав исполняемы файл c помощью `build` или `jar`. Запустить можно запустив jar-файл :
```shell
java -jar ./build/timetable-1.0-SNAPSHOT.jar
```


## Устройство проекта

### Модель

Основные классы находятся в [model](src%2Fmain%2Fjava%2Forg%2Ftimetable%2Falgorithm%2Fmodel).

### Штрафы

В [constraints](src%2Fmain%2Fjava%2Forg%2Ftimetable%2Falgorithm%2Fconstraints) содержатся классы предзназначенные для
подсчета штрафов.

### Алгоритм

В [scheduling](src%2Fmain%2Fjava%2Forg%2Ftimetable%2Falgorithm%2Fscheduling) находится основной код алгоритмов

### Эксперименты

В [experementations](src%2Fmain%2Fjava%2Forg%2Ftimetable%2Falgorithm%2Fexperementations) вы можете увидеть примеры
запуска. Здесь заполняются данные разными многочисленными значениями, используетс в основном ради тестов, перебора
разных вариантов.

## Использование

### На вход

Входной точкой использования является
класс [AlgorithmScheduler.java](src%2Fmain%2Fjava%2Forg%2Ftimetable%2Falgorithm%2Fscheduling%2FAlgorithmScheduler.java)
Или можно использовать
сразу
класс [AlgorithmProcessingStatus.java](src%2Fmain%2Fjava%2Forg%2Ftimetable%2Falgorithm%2Fscheduling%2FAlgorithmProcessingStatus.java)
и его метод asyncStart.
На вход необходимо передать сгруппированную информацию: TableTimeSetting - сколько дней и временных ячеек в каждом дне.
audiences - список аудиторий и их тип для предметов. studyPlanEvolves - список учебных программ.
Учебная программа состоит из групп (для которых она распространяется), а так же предметов данного учебного плана.
Предметы учебного плана состоят из количества семинаров, количества лекций, и распределения преподавателей для групп.

FYI: почему преподаватели задаются заранее а не алгоритмом? Потому что зачастую идет преемственность по разным
предметам,
нередко у группы повторятся один и тот же преподаватель на разны предметах. А для алгоритма возможность перебирать
преподавателей на самом деле не добавляет практически новых возможностей.

### Выходные данные

В зависимости от значения satisfiedScheduleAmount (базовое 3), буде возвращен лист из данного числа результатов. Каждый
результат - готовое расписание, вместе с информацией о нарушенных ограничениях. Передаются лучшие из найденных, если
таковых несколько, или не было найдено абсолютного решения.

### Асинхронно

Есть встроенная возможность запустить выполнение алгоритма асинхронно - asyncStart.
AlgorithmProcessingStatus - возвращает информацию о состоянии алгоритма. Процент выполнения - псевдопоказатель, в
эвристическом алгоритме нельзя одновременно настроить гарантированное время и гарантированное качество. Поэтому процент
показывает прогресс выполнени относительно изначального состояния.

### Добавление своих штрафов для мягких и жестких ограничений, или использование

Генетический алгоритм оптимизирует расписание относительно подсчитанных штрафов. При создании класса
GeneticAlgorithmScheduler, так же указывается PenaltyChecker, состоящий из Penalty - штрафов и их характеристики. Чтобы
добавить новый штраф, необходимо добавить Penalty и реализовать
PenaltyFunction[PenaltyFunction.java](src%2Fmain%2Fjava%2Forg%2Ftimetable%2Falgorithm%2Fconstraints%2FPenaltyFunction.java) -
интерфейс функции подсчета штрафа по предоставленной алгоритмом информацией, с небольшим пояснением.
Примеры реализации такий функций, можете найти в
[PenaltyEnum.java](src%2Fmain%2Fjava%2Forg%2Ftimetable%2Falgorithm%2Fconstraints%2FPenaltyEnum.java). Например
жесткий штраф, если тип аудитории и преподавателя различаются:

```java
var lambdaPenalty = (data) -> {
    LessonWithTime lesson = data.currentLesson();
    if (!lesson.audience().auditoryType()
            .equals(lesson.teacher().teacherType())) {
        return problem(-40.0, "Teacher and audience has different types "
                + lesson.teacher() + " " + lesson.audience());
    }
    return ok();
};
```

### Использование в коде

В начале создаем скедулер

```java
var penalties = Arrays.stream(PenaltyEnum.values()).map(PenaltyEnum::toPenalty).toList();
penalties.

add(...); // Добавляем свои ограничения

var scheduler = new GeneticAlgorithmScheduler(
        PenaltyChecker.newBuilder(timeSetting)
                .addPenalties(penalties).build(),
        timeSetting);
//Подготавливаем данные
var algorithmStatus = geneticAlgorithmScheduler.asyncStart(plansList, audienceEvolves, service, timeSetting);
algorithmStatus.getResult().thenAccept(...); // Асинхронно выполним после завершения

var result = algorithmStatus.getResult().join(); // Получаем результат с блокировкой
result.get(0).allLessons(); // Получаем список всех занятий по времени для всех групп и планов.
```

## Дальнейшее перспективы

### Улучшение мутаторов
Текущие мутаторы быстры в исполнение, но можно создать более сложные мутаторы, которые перепроверяют и оптимизируют 
варианты учитывая все занятия, а не малую часть

### Устройство генотипа и фенотипа
Чтобы добавить новые мутаторы, и многое другое - необходимо изменить хромосому. Чтобы быстро получать информацию
о использующихся аудиториях и классах - необходимо добавить обратный индекс и дерево. Это позволит быстрее применять
различные операции пригодные для мутации. 

### Усложнение отбора
Существуют различные способы проведения отбора, некоторые из которых более эффективны при определённых задачах
(например пул из трёх популяций, которые обмениваются лучшими видами). 
