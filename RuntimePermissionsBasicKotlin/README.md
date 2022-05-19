
Android RuntimePermissionsBasicKotlin Sample
============================================

This basic sample shows runtime permissions available in the Android M and above.
It shows how to use the new runtime permissions API to check and request permissions through the
support library.

Introduction
------------

Android M introduced runtime permissions. Applications targeting M and above need to request their
permissions at runtime.
This sample introduces the basic use of the runtime permissions API through the support library by
verifying permissions (ActivityCompat#checkSelfPermission(Context, String)), requesting permissions (ActivityCompat#requestPermissions(Activity, String[], int))
and handling the permission request callback (ActivityCompat.OnRequestPermissionsResultCallback).
An application can display additional context and justification for a permission after calling
ActivityCompat#shouldShowRequestPermissionRationale#shouldShowRequestPermissionRationale(Activity, String).

See the "RuntimePermissions" sample for a more complete description and reference implementation.

Pre-requisites
--------------

- Android SDK 30

Screenshots
-------------

<img src="screenshots/screenshot-1.png" height="400" alt="Screenshot"/> 

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Support
-------

- Stack Overflow: http://stackoverflow.com/questions/tagged/android

If you've found an error in this sample, please file an issue:
https://github.com/android/permissions-samples

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING.md for more details.
