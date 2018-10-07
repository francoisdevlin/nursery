package main

import (
	"fmt"
	"nursery"
)

func main() {
	p := nursery.Person{Id: 1, Name: "Bob Barker", DaysBetweenServing: 28}
	fmt.Printf("Hello World, %v\n", p)
}

func GetPeopleForSession(availablePeople []nursery.Person) []nursery.Person {
	output := []nursery.Person{}
	return output
}
