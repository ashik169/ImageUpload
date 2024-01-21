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

### 3. Upload Image
  - Selected images will be uploaded to server through Background Service. 
  - Only maximum 10 images allowed to upload at a time. 
  - Images will be saved to cloud directory in the local storage.
[FileBackgroundService.kt](app%2Fsrc%2Fmain%2Fjava%2Fcom%2Fashik%2Fimageupload%2Fservice%2FFileBackgroundService.kt)

### 3. Delete Image
On Home Screen, By clicking long press of image selection mode will presence. You can either delete multiple images or share to other apps.
[FileBackgroundService.kt](app%2Fsrc%2Fmain%2Fjava%2Fcom%2Fashik%2Fimageupload%2Fservice%2FFileBackgroundService.kt) 
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240121_203433_multi_select.png" alt="drawing" width="250"/>

### 3. Receive Images from the other app
If you share any images from gallery app. App icon will be present receiving a images. 

### 3. Share Images to the other app
Can share saved images to other apps one or more images. 

#### Home Screen
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240121_183711_home.png" alt="drawing" width="250"/>

#### Choose an image Screen
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240121_183738_options.png" alt="drawing" width="250"/>

#### Preview/Upload Screen
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240121_183811_preview.png" alt="drawing" width="250"/>

#### Gallery Screen
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240121_183844_gallery.png" alt="drawing" width="250"/>


