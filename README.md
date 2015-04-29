Windows Azure Notification Hubs plugin for Apache Cordova
==================================
Exposes Windows Azure [Notification Hubs](http://www.windowsazure.com/en-us/services/notification-hubs/) functionality as Apache Cordova Plugin for PhoneGap Build. Windows Phone8, iOS and Android.

It's based on: https://github.com/PhonegapProjects/cordova-plugin-azure-notificationhub
And https://github.com/sgrebnov/cordova-plugin-azure-notificationhub

But uses and updated version of Azure Notification Hubs SDK and works on iOS8 x64

### Sample usage ###


Create some javascript in the Device On Ready event of PhoneGap/Cordava

    var connectionString = "Endpoint=sb://[service bus name space].servicebus.windows.net/;SharedAccessKeyName=DefaultFullSharedAccessSignature;SharedAccessKey=[notification hub full key]",
        notificationHubPath = "[notification hub name]";

    var hub = new WindowsAzure.Messaging.NotificationHub(notificationHubPath, connectionString);
    string tag = "tag";

    hub.registerApplicationAsync(tag).then(function (result) {
        console.log("Registration successful");
    });

Reference the plugin in the Phonegap Config XML as mentioned on https://build.phonegap.com/plugins/2810


Push messages are generated/triggered automatically in the native code parts. No need to do that via Javascript.

For debugging you can use the built in debug option in Azure Notification Hubs in the portal website

### Platform Quirks ###
**IOS**
Working
Expects a message in the format {"aps":{"alert":"Some Message"}}

Second registration attempt might generat error. But it is already registered.

**Android**
Working
Needs a default icon in common resolutions

Notification expects a title and msg (message) field in the json like:
{"data":{"title":"some title","msg":"Notification Hub test notification"}}

~~~~
### Copyrights ###
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
