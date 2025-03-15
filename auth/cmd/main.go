package main

import (
	"log"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/mehmetcc/oms-implementation/auth/internal/auth"
	"github.com/mehmetcc/oms-implementation/auth/internal/db"
	"github.com/mehmetcc/oms-implementation/auth/pkg/config"
	jwtPkg "github.com/mehmetcc/oms-implementation/auth/pkg/jwt"
)

func main() {
	cfg, err := config.LoadConfig()
	if err != nil {
		log.Fatalf("failed to load config: %v", err)
	}
	if cfg.JWTSecret == "" {
		cfg.JWTSecret = "your-secret-key"
	}
	jwtPkg.SecretKey = cfg.JWTSecret

	database, err := db.InitDB(cfg)
	if err != nil {
		log.Fatalf("could not connect to database: %v", err)
	}
	if err := database.AutoMigrate(&auth.User{}); err != nil {
		log.Fatalf("failed to migrate database: %v", err)
	}

	router := gin.Default()

	router.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"http://localhost:669"},
		AllowMethods:     []string{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Origin", "Content-Type", "Accept", "Authorization"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
	}))

	router.POST("/register", auth.Register)
	router.POST("/login", auth.Login)
	router.GET("/users", auth.ListUsers)

	port := cfg.Port
	if port == "" {
		port = "668"
	}
	router.Run(":" + port)
}
