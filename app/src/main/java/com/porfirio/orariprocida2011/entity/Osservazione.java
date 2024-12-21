package com.porfirio.orariprocida2011.entity;

import java.time.LocalDateTime;

public class Osservazione {

    public enum Direction {
        N, NE, E, SE, S, SW, W, NW
    }

    private Direction windDirection;
    private double windSpeed;
    private LocalDateTime time;

    public Osservazione() {
        this(0, Direction.N, LocalDateTime.now());
    }

    public Osservazione(double windSpeed, double windAngle, LocalDateTime time) {
        setWindDirection(windAngle);
        setWindSpeed(windSpeed);
        setTime(time);
    }

    public Osservazione(double windSpeed, Direction windDirection, LocalDateTime time) {
        setWindDirection(windDirection);
        setWindSpeed(windSpeed);
        setTime(time);
    }

    public double getWindBeaufort() {
        if (windSpeed <= 1)
            return 0;
        else if (windSpeed > 1 && windSpeed < 6)
            return (1 + (windSpeed - 3) / (5 - 1));
        else if (windSpeed >= 6 && windSpeed < 12)
            return (2 + (windSpeed - 8.5) / (11 - 6));
        else if (windSpeed >= 12 && windSpeed < 20)
            return (3 + (windSpeed - 15.5) / (19 - 12));
        else if (windSpeed >= 20 && windSpeed < 29)
            return (4 + (windSpeed - 24) / (28 - 20));
        else if (windSpeed >= 29 && windSpeed < 39)
            return (5 + (windSpeed - 33.5) / (38 - 29));
        else if (windSpeed >= 39 && windSpeed < 50)
            return (6 + (windSpeed - 44) / (49 - 39));
        else if (windSpeed >= 50 && windSpeed < 62)
            return (7 + (windSpeed - 55.5) / (61 - 50));
        else if (windSpeed >= 62 && windSpeed < 75)
            return (8 + (windSpeed - 68) / (74 - 62));
        else if (windSpeed >= 75 && windSpeed < 89)
            return (9 + (windSpeed - 81.5) / (88 - 75));
        else if (windSpeed >= 89 && windSpeed < 103)
            return (10 + (windSpeed - 95.5) / (102 - 89));
        else if (windSpeed >= 103 && windSpeed < 118)
            return (11 + (windSpeed - 110) / (117 - 103));
        else
            return 12.0;
    }

    public Direction getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(double windAngle) {
        int i = (int) (windAngle / 45) % 360;
        setWindDirection(Direction.values()[i]);
    }

    public void setWindDirection(Direction direction) {
        this.windDirection = direction;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeedKPH) {
        if (windSpeed == 0 || windSpeedKPH > 0)
            windSpeed = windSpeedKPH;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

}
