package db

import (
	"fmt"
	"log"

	"github.com/mehmetcc/oms-implementation/auth/pkg/config"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

var database *gorm.DB

func InitDB(cfg *config.Config) (*gorm.DB, error) {
	host := cfg.DBHost
	if host == "" {
		host = "localhost"
	}
	port := cfg.DBPort
	if port == "" {
		port = "5434"
	}
	user := cfg.DBUser
	if user == "" {
		user = "postgres"
	}
	password := cfg.DBPassword
	if password == "" {
		password = "postgres"
	}
	dbname := cfg.DBName
	if dbname == "" {
		dbname = "postgres"
	}
	dsn := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=disable", host, port, user, password, dbname)
	var err error
	database, err = gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		log.Fatalf("failed to connect to database: %v", err)
	}
	return database, nil
}

func GetDB() *gorm.DB {
	return database
}
