package config

import (
	"github.com/spf13/viper"
)

type Config struct {
	DBHost     string
	DBPort     string
	DBUser     string
	DBPassword string
	DBName     string
	JWTSecret  string
	Port       string
}

func LoadConfig() (*Config, error) {
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")
	if err := viper.ReadInConfig(); err != nil {
		return nil, err
	}
	return &Config{
		DBHost:     viper.GetString("database.host"),
		DBPort:     viper.GetString("database.port"),
		DBUser:     viper.GetString("database.user"),
		DBPassword: viper.GetString("database.password"),
		DBName:     viper.GetString("database.name"),
		JWTSecret:  viper.GetString("jwt.secret"),
		Port:       viper.GetString("server.port"),
	}, nil
}
