package com.example.Securitate.Cars;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "masini")
public class Car {

    @Id
    @SequenceGenerator(
            name = "car_sequence",
            sequenceName = "car_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "car_sequence"
    )
    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String color;
    private String transmission;
    private String fuel_type;
    private Integer price_per_day;
    @JsonProperty("emissions")
    private float emissions;
    private float fuel_consumption;

    @Lob
    private byte[] image;
    private String imageName;
    private String imageType;

    public Car() {}

    public Car(Long id, String brand, String model, Integer year, String color,
               String transmission, String fuel_type, Integer price_per_day,
               float emissions, float fuel_consumption,
               byte[] image, String imageName, String imageType) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.color = color;
        this.transmission = transmission;
        this.fuel_type = fuel_type;
        this.price_per_day = price_per_day;
        this.emissions = emissions;
        this.fuel_consumption = fuel_consumption;
        this.image = image;
        this.imageName = imageName;
        this.imageType = imageType;
    }

    public Car(String brand, String model, Integer year, String color,
               String transmission, String fuel_type, Integer price_per_day,
               float emissions, float fuel_consumption,
               byte[] image, String imageName, String imageType) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.color = color;
        this.transmission = transmission;
        this.fuel_type = fuel_type;
        this.price_per_day = price_per_day;
        this.emissions = emissions;
        this.fuel_consumption = fuel_consumption;
        this.image = image;
        this.imageName = imageName;
        this.imageType = imageType;
    }



    public float getCO2_emissions() {
        return emissions;
    }

    public void setCO2_emissions(float emissions) {
        this.emissions = emissions;
    }

    public float getFuel_consumption() {
        return fuel_consumption;
    }

    public void setFuel_consumption(float fuel_consumption) {
        this.fuel_consumption = fuel_consumption;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getFuel_type() {
        return fuel_type;
    }

    public void setFuel_type(String fuel_type) {
        this.fuel_type = fuel_type;
    }

    public Integer getPrice_per_day() {
        return price_per_day;
    }

    public void setPrice_per_day(Integer price_per_day) {
        this.price_per_day = price_per_day;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
}
