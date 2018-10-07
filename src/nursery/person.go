package nursery

import "fmt"

type Person struct {
	Id                 int
	DaysBetweenServing int
	Name               string
}

func (p Person) String() string {
	return fmt.Sprintf("%s(%d)", p.Name, p.Id)
}
