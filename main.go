package main

import (
	"fmt"
	"nursery"
	"time"
)

func main() {
	t1 := StartOfDay(2018, time.Month(10), 1)
	t2 := StartOfDay(2018, time.Month(10), 3)
	//p := nursery.Person{Id: 1, Name: "Bob Barker", DaysBetweenServing: 28}
	people := MockPeople(100)
	requirementsTemplate := map[nursery.Role]int{
		nursery.NewRole("Room 1"):     2,
		nursery.NewRole("Room 2"):     2,
		nursery.NewRole("Room 3"):     2,
		nursery.NewRole("Room 4"):     2,
		nursery.NewRole("Supervisor"): 1,
	}
	session := nursery.SessionInfo{
		Id:               1,
		ActivityDate:     StartOfDay(2018, time.Month(11), 1),
		RoleRequirements: requirementsTemplate,
	}
	people = GetAvailablePeople(people, session)
	fmt.Printf("Hello World, %v, %v, %v\n", people, t2.Sub(t1).Hours()/24, session)
}

func MockPeople(count int) []nursery.Person {
	output := []nursery.Person{}
	for iter1 := 0; iter1 < count; iter1++ {
		p := nursery.Person{
			Id:                 iter1,
			Name:               fmt.Sprintf("Person %d", iter1),
			DaysBetweenServing: 28,
			LastServedOn:       StartOfDay(2018, time.Month(10), 1),
		}
		output = append(output, p)
	}
	return output
}

func ItHasBeenLongEnough(person nursery.Person, session nursery.SessionInfo) bool {
	cutoff := 0.85
	return NormalizedIntervalSinceService(person, session) > cutoff
}

func NormalizedIntervalSinceService(person nursery.Person, session nursery.SessionInfo) float64 {
	daysSinceServing := session.ActivityDate.Sub(person.LastServedOn).Hours() / 24
	normalizedDaysSinceService := daysSinceServing / float64(person.DaysBetweenServing)
	return normalizedDaysSinceService
}

func GetAvailablePeople(allPeople []nursery.Person, session nursery.SessionInfo) []nursery.Person {
	output := []nursery.Person{}
	shouldServe := true
	predicates := []func(nursery.Person, nursery.SessionInfo) bool{}
	predicates = append(predicates, ItHasBeenLongEnough)
	for _, p := range allPeople {
		for _, predicate := range predicates {
			isStillQualified := predicate(p, session)
			shouldServe = shouldServe && isStillQualified
		}
		if shouldServe {
			output = append(output, p)
		}
	}
	return output
}

func GetPeopleForSession(availablePeople []nursery.Person, session nursery.SessionInfo) []nursery.Person {
	output := []nursery.Person{}
	return output
}

func StartOfDay(year int, month time.Month, day int) time.Time {
	return time.Date(year, month, day, 0, 0, 0, 0, time.UTC)
}
