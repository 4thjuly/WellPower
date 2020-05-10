// Well power

#define NOISE_LEVEL 100

int POWER_SENSOR = A0; // 0 - 4095
 
int powerLevel = 0;
int onLevel = INT_MAX;

time_t onTime = 0;
time_t offTime = 0;

void setup() {
    pinMode(POWER_SENSOR, INPUT);
    Particle.variable("PowerLevel", powerLevel);
    Particle.variable("OnTime", onTime);
    Particle.variable("OffTime", offTime);

    Particle.function("SetOnLevel", setOnLevel);
 
    Particle.publish("Setup", "done", PRIVATE);
}

int setOnLevel(String level)
{
    onLevel = level.toInt();
    if (onLevel == 0) {
        onLevel = INT_MAX;
        return false;
    }

    return true;
}

void loop() {
    int currentPowerLevel = analogRead(POWER_SENSOR);

    // denoise a bit
    if (abs(currentPowerLevel - powerLevel) > NOISE_LEVEL) {
        powerLevel = currentPowerLevel;
        if (powerLevel >= onLevel) onTime = Time.now();
        else offTime = Time.now();
    }

    delay(1s);
} 