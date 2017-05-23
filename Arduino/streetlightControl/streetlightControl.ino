#include <ESP8266WiFi.h>

//////////////////////
// WiFi Definitions //
//////////////////////
const char WiFiAPPSK[] = "esp8266-12e";

/////////////////////
// Pin Definitions //
/////////////////////
const int LED_PIN = 16; // Thing's onboard, green LED
const int LED_PIN2 = 15;
const int LED_PIN3 = 14;

WiFiServer server(80);

void setupWiFi()
{
  WiFi.mode(WIFI_AP);

  // Do a little work to get a unique-ish name. Append the
  // last two bytes of the MAC (HEX'd) to "ThingDev-":
  uint8_t mac[WL_MAC_ADDR_LENGTH];
  WiFi.softAPmacAddress(mac);
  String macID = String(mac[WL_MAC_ADDR_LENGTH - 2], HEX) +
                 String(mac[WL_MAC_ADDR_LENGTH - 1], HEX);
  macID.toUpperCase();
  String AP_NameString = "ESP-" + macID;

  char AP_NameChar[AP_NameString.length() + 1];
  memset(AP_NameChar, 0, AP_NameString.length() + 1);

  for (int i = 0; i < AP_NameString.length(); i++)
    AP_NameChar[i] = AP_NameString.charAt(i);

  WiFi.softAP(AP_NameChar, WiFiAPPSK);
}

void initHardware()
{
  Serial.begin(115200);
  pinMode(LED_PIN, OUTPUT);
  pinMode(LED_PIN2, OUTPUT);
  pinMode(LED_PIN3, OUTPUT);
  digitalWrite(LED_PIN, LOW);
  // Don't need to set ANALOG_PIN as input,
  // that's all it can be.
}

void setup()
{
  initHardware();
  setupWiFi();
  server.begin();
}

void loop()
{
  // Check if a client has connected
  WiFiClient client = server.available();
  if (!client) {
    return;
  }

  // Read the first line of the request
  String req = client.readStringUntil('\r');
  Serial.println(req);
  client.flush();

  //Sample: GET /led/255 HTTP/1.1

  int start_idx = req.indexOf("led/") + 4;
  if (start_idx < 9)
  {
    return;
  }
  Serial.print("Start index: ");
  Serial.println(start_idx);

  int end_idx = req.indexOf(" ", start_idx);
  Serial.print("End index: ");
  Serial.println(end_idx);

  String valStr = req.substring(start_idx, end_idx);
  Serial.println("Value String: " + valStr);
  int valF = valStr.toInt();
  Serial.print("Intensity: ");
  Serial.println(valF);
  // Set GPIO5 according to the request
  if (valF >= 0)
  {
    analogWrite(LED_PIN, valF);
    analogWrite(LED_PIN2, valF);
    analogWrite(LED_PIN3, valF);
  }

  client.flush();

  // Prepare the response. Start with the common header:
  String s = "HTTP/1.1 200 OK\r\n";
  s += "Content-Type: text/html\r\n\r\n";
  s += "<!DOCTYPE HTML>\r\n<html>\r\n";
  // If we're setting the LED, print out a message saying we did
  if (valF >= 0)
  {
    s += "LED is now ";
    //s += (val)?"off":"on";
    s += valF;
  }
  else
  {
    s += "Invalid Request.<br> Try /led/1, /led/0, or /read.";
  }
  s += "</html>\n";

  // Send the response to the client
  client.print(s);
  delay(1);
  Serial.println("Client disonnected");

  // The client will actually be disconnected
  // when the function returns and 'client' object is detroyed
}



