// Well power

#define NOISE_LEVEL 100

int POWER_SENSOR = A0; // 0 - 4095
 
int powerLevel = 0;
int onLevel = 500; // Some reasonable emprical value 
bool isOn = false;
int onTimeDelta = 0;     // How long on (seconds)
int offTimeDelta = 0;    // How long off (seconds)

time_t lastTransitionTime = 0;

void setup() {
    pinMode(POWER_SENSOR, INPUT);
    Particle.variable("PowerLevel", powerLevel);
    Particle.variable("OnTimeDelta", onTimeDelta);
    Particle.variable("OffTimeDelta", offTimeDelta);
    Particle.variable("IsOne", isOn);
    Particle.variable("OnLevel", onLevel);

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
        if (!isOn && powerLevel >= onLevel) { 
            // Transition from off to on
            isOn = true;
            lastTransitionTime = Time.now();
            Particle.publish("On", "true", PRIVATE);
        } else if (isOn && powerLevel < onLevel) {
            // Transitioning from on to off
            isOn = false;
            lastTransitionTime = Time.now();
            Particle.publish("On", "false", PRIVATE);
        }

        if (isOn) onTimeDelta = Time.now() - lastTransitionTime;
        else offTimeDelta = Time.now() - lastTransitionTime;
    }

    delay(1s);
} 
