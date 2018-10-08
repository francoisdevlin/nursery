package nursery

import (
	"time"
)

type SessionInfo struct {
	Id               int
	RoleRequirements map[Role]int
	ActivityDate     time.Time
}
