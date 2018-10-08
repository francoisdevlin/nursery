package nursery

import (
	"fmt"
	"time"
)

type Person struct {
	Id                 int
	DaysBetweenServing int
	Name               string
	LastServedOn       time.Time
	Qualifications     []Role
}

func (p Person) String() string {
	return fmt.Sprintf("%s(%d)", p.Name, p.Id)
}
