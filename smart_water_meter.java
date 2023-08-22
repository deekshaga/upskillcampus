 float pH;
 float waterTurbidity;
 int waterLevel;
 bool manual_mode;
 bool waterPump;
 Variables which are marked as READ/WRITE in the Cloud Thing will also have functions
 which are called when their values are changed from the Dashboard.
 These functions are generated with the Thing and added at the end of this sketch.
*/
#include <ESP8266WiFi.h>;
#include <WiFiClient.h>;
#include "thingProperties.h"
const char* ssid = "OnePlus Nord"; // Your Network SSID 18122002
const char* password = "Shrikrishna"; // Your Network Password
#define Turbidity 16
//#define pH A0
#define pump 15
#define trig 4
#define echo 5
#define wLevel 0
#define sensorPower 14
int lowerThreshold = 420;
int upperThreshold = 520;
int val = 0; // Value for storing water level
unsigned long int avgValue; // Store the average value of the sensor feedback
long duration;
int distCM;

float b;
int buf[10], temp;
float volt;
float ntu;
int height=100;
void setup() {
 // Initialize serial and wait for port to open:
 Serial.begin(9600);
 // This delay gives the chance to wait for a Serial Monitor without blocking if none
is found
 delay(1500);
 // Defined in thingProperties.h
 initProperties();
 //pinMode(13, OUTPUT);
 Serial.begin(9600);
 Serial.println("Ready"); // Test the serial monitor
 pinMode(pump, OUTPUT);
 pinMode(echo, INPUT);
 pinMode(trig, OUTPUT);
 pinMode(sensorPower, OUTPUT);
 digitalWrite(sensorPower, LOW);
 pinMode(wLevel,INPUT);
 pinMode(Turbidity, INPUT);
 
 WiFi.begin(ssid, password);
 // Connect to Arduino IoT Cloud
 ArduinoCloud.begin(ArduinoIoTPreferredConnection);
 
 /*
 The following function allows you to obtain more information
 related to the state of network and IoT Cloud connection and errors
 the higher number the more granular information you’ll get.
 The default is 0 (only errors).
 Maximum is 4
*/
 setDebugMessageLevel(2);
 ArduinoCloud.printDebugInfo();
}
void loop() {
 ArduinoCloud.update();
 // Your code here
 onManualModeChange();

delay(100);
}
/*
 Since ManualMode is READ_WRITE variable, onManualModeChange() is
 executed every time a new value is received from IoT Cloud.
*/
void onManualModeChange() {
 // Add your code here to act upon ManualMode change
 if(manual_mode){
 onWaterPumpChange();
 }
 else{
 automate();
 }
}
/*
 Since WaterPump is READ_WRITE variable, onWaterPumpChange() is
 executed every time a new value is received from IoT Cloud.
*/
void onWaterPumpChange() {
 // Add your code here to act upon WaterPump change
 if(waterPump){
 digitalWrite(pump, LOW);
 }
 else{
 digitalWrite(pump, HIGH);
 }
}
void automate()
{
 //Automatic or manual
 // digitalWrite(RelayPin,LOW);
 // ULTRASONIC SENSOR
 digitalWrite(pump, LOW);
 digitalWrite(trig, LOW);
 digitalWrite(trig, HIGH);
 digitalWrite(trig, LOW);
 duration = pulseIn(echo, HIGH);
 distCM = duration * 0.034 / 2;
 
 //waterLevel=distCM; 
 if (distCM <= 20 && distCM >= 15){
 Serial.println("Sufficient water");
 Serial.println(distCM);
 }

 else if (distCM <= 15 && distCM >= 10){
 Serial.println("Tank only half filled");
 Serial.println(distCM);
 }
 else if (distCM < 7){
 Serial.println("Refill tank");
 Serial.println(distCM);
 }
 else if (distCM < 3){
 Serial.println("Tank Empty");
 Serial.println(distCM);
 digitalWrite(pump, HIGH);
 }
 // pH SENSOR
 
for (int i = 0; i < 10; i++) //Get 10 sample value from the sensor for smooth the value
 {
 buf[i] = analogRead(pH);
 delay(10);
 }
 for (int i = 0; i < 9; i++) // sort the analog from small to large{
 for (int j = i + 1; j < 10; j++){
 if (buf[i] > buf[j]){
 temp = buf[i];
 buf[i] = buf[j];
 buf[j] = temp;
 }
 }
 }
 avgValue = 0;
 for (int i = 2; i < 8; i++) // take the average value of 6 center sample
 avgValue += buf[i];
 float phValue = (float)avgValue * 5.0 / 1024 / 6; // convert the analog int
o millivolt
 phValue = 3.5 * phValue;// convert the millivolt into pH value
 pH=phValue
 Serial.print(" pH:");
 Serial.print(phValue, 2);
 Serial.println(" ");
 //digitalWrite(13, HIGH);
 delay(800);
 //digitalWrite(13, LOW);
 if (phValue > 8.5 || phValue < 6.5){
 Serial.print("WARNING: pH level - Exterme");
 digitalWrite(pump, LOW);

 }
 // TURBIDITY SENSOR
 volt = 0;
 for (int i = 0; i < 800; i++){
 volt += ((float)analogRead(Turbidity) / 1023) * 5;
 }
 volt = volt / 800;
 volt = round_to_dp(volt, 2);
 if (volt < 2.5) {
 ntu =0.5;
 }
 else{
 ntu = (-1120.4 * sq(volt) + 5742.3 * volt - 4353.8);
 }
 Serial.print(ntu);
 Serial.println(" NTU");
 waterTurbidity=ntu;
 delay(100);
 if (ntu >= 1){
 Serial.println("Warning: turbity level- Harmful ");
 digitalWrite(pump, HIGH);
 }
 // Water level Sensor
 digitalWrite(sensorPower, HIGH);
 delay(100);
 val = analogRead(wLevel);
 digitalWrite(sensorPower, LOW);
 int level = val;
 waterLevel=level;
 if (level == 0){
 Serial.println("Water Level: Empty");
 Serial.println(level);
 }
 else if (level > 0 && level <= lowerThreshold){
 Serial.println("Water Level: Low");
 Serial.println(level);
 }
 else if (level > lowerThreshold && level <= upperThreshold){
 Serial.println("Water Level: Medium");
 Serial.println(level);
 }
 else if (level > upperThreshold){



 Serial.println("Water Level: High");
 Serial.println(level);
 }
 Serial.println("");
 delay(3000);
}
float round_to_dp(float in_value, int decimal_place){
 float multiplier = powf(10.0f, decimal_place);
 in_value = roundf(in_value * multiplier) / multiplier;
 return in_value;
}