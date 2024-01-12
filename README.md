# Image Upload Application

To upload images to server from either gallery or camera. Below is a general overview of the steps involved in uploading an image in an Android app:

### 1. Permissions

Requires the following permissions picking gallery and take picture:
  - Camera - Take a picture
  - Read External Storage - Android 32 and below
  - Read Media Image - Android 33 and higher

### 2. Pick or Capture Image

Allow the user to either pick an image from the gallery or capture a new one using the camera.

Used Activity Result API picking an image. 

Parse gallery Uri using FileUtils finding file path.

#### Home Screen
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240112_home.png" alt="drawing" width="250"/>

#### Preview/Upload Screen
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240112_preview.png" alt="drawing" width="250"/>


