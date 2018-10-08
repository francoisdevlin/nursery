package nursery

import (
	"fmt"
)

type Role struct {
	Id   int
	Name string
}

func NewRole(name string) Role {
	return Role{
		Id:   0,
		Name: name,
	}
}

func (r Role) String() string {
	return fmt.Sprintf("%s(%d)", r.Name, r.Id)
}
