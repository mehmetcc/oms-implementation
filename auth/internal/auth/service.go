package auth

import (
	"errors"
	"log"

	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
)

func HashPassword(password string) (string, error) {
	bytes, err := bcrypt.GenerateFromPassword([]byte(password), 14)
	return string(bytes), err
}

func CheckPasswordHash(password, hash string) bool {
	err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
	if err != nil {
		log.Printf("Password comparison failed: %v", err)
	}
	return err == nil
}

func AuthenticateUser(db *gorm.DB, username, password string) (*User, error) {
	var user User
	log.Printf("Attempting to find user: %s", username)
	if err := db.Where("username = ?", username).First(&user).Error; err != nil {
		log.Printf("User lookup failed for %s: %v", username, err)
		return nil, err
	}
	log.Printf("Found user %s with stored hash: %s", username, user.Password)
	if !CheckPasswordHash(password, user.Password) {
		log.Printf("Invalid credentials for user %s", username)
		return nil, errors.New("invalid credentials")
	}
	return &user, nil
}

func CreateUser(db *gorm.DB, user *User) error {
	hashedPassword, err := HashPassword(user.Password)
	if err != nil {
		return err
	}
	user.Password = hashedPassword
	return db.Create(user).Error
}
