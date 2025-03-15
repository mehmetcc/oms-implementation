package auth

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
)

type User struct {
	ID       string `gorm:"type:uuid;primaryKey" json:"id"`
	Username string `gorm:"type:varchar(255);uniqueIndex" json:"username"`
	Password string `json:"password"`
	Role     string `gorm:"type:varchar(50)" json:"role"`
}

func (u *User) BeforeCreate(tx *gorm.DB) (err error) {
	if u.ID == "" {
		u.ID = uuid.New().String()
	}
	return nil
}
